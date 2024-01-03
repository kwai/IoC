package com.kuaishou.commercial.utility.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import shadow.bundletool.com.android.SdkConstants
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile

abstract class AbsInstrumentationTransform : Transform() {
  private lateinit var outputProvider: TransformOutputProvider

  // 设置我们自定义的Transform对应的Task名称
  // 编译的时候可以在控制台看到 比如：Task :app:transformClassesWithAsmTransformForDebug
  abstract override fun getName(): String

  // 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型
  // 这样确保其他类型的文件不会传入
  override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
    return TransformManager.CONTENT_CLASS
  }

  override fun isIncremental(): Boolean = false

  // 指定Transform的作用范围
  override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
    return TransformManager.SCOPE_FULL_PROJECT
  }

  override fun transform(transformInvocation: TransformInvocation?) {
    super.transform(transformInvocation)
    println(">>> AbsInstrumentationTransform transform")

    //TransformOutputProvider管理输出路径,如果消费型输入为空,则outputProvider也为空
    outputProvider = transformInvocation?.outputProvider!!

    if (!transformInvocation.isIncremental) {
      outputProvider.deleteAll()
    }
    processTransform(transformInvocation)
  }

  private fun processTransform(transformInvocation: TransformInvocation) {
    println(">>> processTransform runBlocking")
    runBlocking {
      val deferredList: ArrayList<Deferred<Unit>> = ArrayList()
    // 拿到所有的class文件
    transformInvocation.inputs.forEach { transformInput ->
      // 遍历inputs Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
      transformInput.directoryInputs.forEach { directoryInput ->
        // 遍历directoryInputs(文件夹中的class文件) directoryInputs代表着以源码方式参与项目编译的所有目录结构及其目录下的源码文件
        // 比如我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
          val thread = async(Dispatchers.IO) {
        processDirectoryInput(directoryInput, transformInvocation.isIncremental)
          }
          deferredList.add(thread)
      }

      // 遍历jar包中的class文件 jarInputs代表以jar包方式参与项目编译的所有本地jar包或远程jar包
      transformInput.jarInputs.forEach { jarInput ->
          val thread = async(Dispatchers.IO) {
        // 处理jar包中的class文件
        processJarInput(
          jarInput,
          transformInvocation.isIncremental
        )
          }
          deferredList.add(thread)
      }
    }
      deferredList.awaitAll()
    }
  }

  /**
   * 处理源码文件
   *
   * <p>将修改的字节码拷贝到指定目录下，实现编译期干预字节码<p/>
   *
   * @param directoryInput 待处理的源码文件
   */
  private fun processDirectoryInput(directoryInput: DirectoryInput, incremental: Boolean) {

    val dest: File = outputProvider.getContentLocation(
      directoryInput.name,
      directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY
    )
    FileUtils.forceMkdir(dest)

    if (incremental) {
      directoryInput.changedFiles.forEach { changedFile ->
        val status: Status = changedFile.value
        val inputFile: File = changedFile.key
        val destFilePath: String = inputFile.absolutePath.replace(
          directoryInput.file.absolutePath,
          dest.absolutePath
        )
        val destFile = File(destFilePath)
        when (status) {
          Status.NOTCHANGED -> {
          }
          Status.ADDED, Status.CHANGED -> {
            FileUtils.touch(destFile)
            transformSingleFile(inputFile, destFile)
          }
          Status.REMOVED -> {
            if (destFile.exists()) {
              FileUtils.forceDelete(destFile)
            }
          }
        }
      }
    } else {
      transformDirectory(directoryInput, dest)
    }
  }

  private fun transformDirectory(directoryInput: DirectoryInput, dest: File) {
    if (directoryInput.file.isDirectory) {
      val extensions: Array<String> = arrayOf("class")
      // 列出目录所有文件（包含子文件夹，子文件夹内文件）
      FileUtils.listFiles(directoryInput.file, extensions, true).forEach { inputFile ->
        val destFilePath: String = inputFile.absolutePath.replace(
          directoryInput.file
            .absolutePath, dest.absolutePath
        )
        val destFile = File(destFilePath)
        FileUtils.touch(destFile)
        transformSingleFile(inputFile, destFile)
      }
    }
  }


  /**
   * 处理Jar文件
   *
   * <p>将修改的字节码拷贝到指定目录下，实现编译期干预字节码<p/>
   *
   * @param jarInput 待处理的Jar文件
   */
  private fun processJarInput(jarInput: JarInput, incremental: Boolean) {
    val dest: File = outputProvider.getContentLocation(
      jarInput.name,
      jarInput.contentTypes,
      jarInput.scopes,
      Format.JAR
    )
    val status: Status = jarInput.status
    if (incremental) {
      when (status) {
        Status.NOTCHANGED -> {
        }
        Status.ADDED, Status.CHANGED -> {
          transformJar(jarInput.file, dest)
        }
        Status.REMOVED -> {
          if (dest.exists()) {
            FileUtils.forceDelete(dest);
          }
        }
      }
    } else {
      transformJar(jarInput.file, dest)
    }
  }

  private fun transformSingleFile(inputFile: File, dest: File) {
    if (enableFileTrace(inputFile)) {
      val classReader = ClassReader(inputFile.readBytes())
      // 传入COMPUTE_MAXS，ASM会自动计算本地变量表和操作数栈
      val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
      // 创建类访问器，并交给它去处理
      classReader.accept(getClassVisitor(classWriter), ClassReader.EXPAND_FRAMES)
      scanFile(inputFile)
      if (justScan()) {
        FileUtils.copyFile(inputFile, dest)
      } else {
        val code: ByteArray = classWriter.toByteArray()
        val fos = FileOutputStream(dest)
        fos.write(code)
        fos.close()
      }
      scanFileEnd(inputFile, dest)

    } else {
      FileUtils.copyFile(inputFile, dest)
    }
  }

  private fun transformJar(inputFile: File, dest: File) {
    if (enableJarTrace(inputFile)) {
      val jar = JarFile(inputFile)
      //遍历jar文件内的.class文件
      jar.entries().iterator().forEach {
        if (it.name.endsWith(SdkConstants.DOT_CLASS)) {
          //ClassReader只能处理.class文件
          val classReader = ClassReader(jar.getInputStream(it))
          // 传入COMPUTE_MAXS，ASM会自动计算本地变量表和操作数栈
          val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
          // 创建类访问器，并交给它去处理
          classReader.accept(getClassVisitor(classWriter), ClassReader.EXPAND_FRAMES)
          scanJar(jar, it)
        }
      }
      scanJarEnd(inputFile, dest)
      if (justScan()) {
        FileUtils.copyFile(inputFile, dest)
      } else {
        //暂不支持jar代码注入
        FileUtils.copyFile(inputFile, dest)
      }
    } else {
      FileUtils.copyFile(inputFile, dest)
    }
  }


  /**
   * 获取插桩的ClassVisitor
   * @param classVisitor
   * @return {@see ClassVisitor}
   */
  protected abstract fun getClassVisitor(classVisitor: ClassVisitor): ClassVisitor

  /**
   * 判断该文件（包括jar文件）是否需要插桩
   * 项目可以通过project.extensions配置区分release/debug是否需要插桩
   * @param inputFile 文件对象
   * @return true表示需要插桩，false表示不需要插桩
   */
  protected fun enableSubTrace(inputFile: File): Boolean {
    return true
  }

  private fun enableFileTrace(inputFile: File): Boolean {
    val name = inputFile.name
    return !(!name.endsWith(".class")
        || name.startsWith("R.class")
        || name.startsWith("R$")
        || "BuildConfig.class" === name)
        && enableSubTrace(inputFile)
  }

  private fun enableJarTrace(jarFile: File): Boolean {
    return !jarFile.absolutePath.contains("com.android.support")
        && !jarFile.absolutePath.contains("/android/m2repository")
  }

  /**
   * 仅仅用来扫描，不插桩
   */
  protected open fun justScan(): Boolean {
    return false
  }

  protected fun scanFile(inputFile: File) {}
  protected open fun scanFileEnd(inputFile: File, destFile: File) {}
  protected open fun scanJar(jarFile: JarFile, jarEntry: JarEntry) {}
  protected open fun scanJarEnd(inputFile: File, destFile: File) {}

}