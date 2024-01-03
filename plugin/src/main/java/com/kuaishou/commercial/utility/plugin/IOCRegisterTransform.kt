package com.kuaishou.commercial.utility.plugin

import com.android.build.api.transform.TransformInvocation
import groovyjarjarasm.asm.Opcodes.ASM7
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.zip.ZipEntry

open class IOCRegisterTransform : AbsInstrumentationTransform() {
  private var collectionFile: File? = null
  private var jarFile: File? = null
  private var jarFileName: String? = null


  private val registerList = mutableListOf<String>()
  private val registerCollectorName = mutableListOf<String>()
  override fun getName(): String {
    return "IOCRegisterTransform"
  }

  override fun getClassVisitor(classVisitor: ClassVisitor): ClassVisitor {
    //自定义ClassVisitor，将符合条件的扫描class添加到list中
    return RegisterListClassVisitor(registerList, registerCollectorName, ASM7, classVisitor)
  }


  override fun justScan(): Boolean {
    return true
  }


  override fun scanFileEnd(inputFile: File, destFile: File) {
    if (isRegisterFile(inputFile.name)) {
      collectionFile = destFile
    }
  }

  override fun scanJar(jarFile: JarFile, jarEntry: JarEntry) {
    if (isRegisterFile(jarEntry.name)) {
      jarFileName = jarFile.name
    }
  }

  override fun scanJarEnd(inputFile: File, destFile: File) {
    if (inputFile.absolutePath == jarFileName) {
      jarFile = destFile
    }
  }

  override fun transform(transformInvocation: TransformInvocation?) {
    val startTime = System.currentTimeMillis()
    super.transform(transformInvocation)
    println(
      ">>> IOCRegisterRegister transform: registerCollectorName:  " +
          "$registerCollectorName ,collectionFile:$collectionFile , registerList: $registerList ，" +
          " jarFile : $jarFile"
    )

    if (registerCollectorName.size > 0) {
      if (jarFile == null && collectionFile != null) {
        println(">>> IOCRegisterRegister transform collectionFile: ${collectionFile!!.name}}")
        //直接处理class文件
        val bytes = modifyRegisterByte(collectionFile!!.readBytes())
        val outputStream = FileOutputStream(collectionFile!!)
        outputStream.write(bytes)
        outputStream.close()
      } else if (jarFile != null) {
        println(">>> IOCRegisterRegister transform jarFile:${(jarFile!!.name)}")
        //处理jar
        Utils.dealJarFile(jarFile!!) { jarEntry, jos, bakJarFile ->
          if (jarEntry.name.contains(registerCollectorName[0])) {
            val inputStream = bakJarFile.getInputStream(jarEntry)
            jos.putNextEntry(ZipEntry(jarEntry.name))
            jos.write(modifyRegisterByte(Utils.readNBytes(inputStream, inputStream.available())))
            inputStream.close()
            return@dealJarFile true
          }
          return@dealJarFile false
        }
      }
    }
    println(">>> IOCRegisterTransform cost: ${System.currentTimeMillis() - startTime}ms")
  }

  //生成register代码
  private fun modifyRegisterByte(ins: ByteArray): ByteArray {
    val cr: ClassReader?
    try {
      cr = ClassReader(ins)
    } catch (e: Exception) {
      e.printStackTrace()
      throw e
    }
    println(">>> modifyRegisterByte registerList：${registerList}")

    val classWriter = ClassWriter(0)
    val cn = ClassNode()
    cr.accept(cn, 0)

    cn.methods.removeIf { it.name == "register" && "()V" == it.desc }

    val methodVisitor =
      classWriter.visitMethod(ACC_PUBLIC or ACC_STATIC, "register", "()V", null, null)
    methodVisitor.visitCode()

    registerList.forEach { name ->
      val label = Label()
      methodVisitor.visitLabel(label);
      methodVisitor.visitTypeInsn(NEW, name)
      methodVisitor.visitInsn(DUP)
      methodVisitor.visitMethodInsn(
        INVOKESPECIAL,
        name, "<init>", "()V", false
      )
      methodVisitor.visitMethodInsn(
        INVOKEVIRTUAL,
        name, "register", "()V",
        false
      )
    }

    val labelReturn = Label()
    methodVisitor.visitLabel(labelReturn)
    methodVisitor.visitInsn(RETURN)
    methodVisitor.visitEnd()
    cn.accept(classWriter)
    return classWriter.toByteArray()
  }


  private fun isRegisterFile(name: String): Boolean {
    if (name.contains("ServiceRegisterCollection")) {
      println("IOCRegisterRegister scanJar jarEntry.name : ${(name)} , registerCollectorName: $registerCollectorName")
    }

    return if (registerCollectorName.size > 0) {
      name.contains(
        registerCollectorName[0]
      )
    } else {
      false
    }
  }
}