package com.kuaishou.commercial.utility.plugin

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class RegisterListClassVisitor(
  private val registerList: MutableList<String>,
  private val registerCollectorName: MutableList<String>,
  accessFlag: Int, classVisitor: ClassVisitor
) :
  ClassVisitor(accessFlag, classVisitor), Opcodes {

  private var mName: String = ""
  override fun visit(
    version: Int,
    access: Int,
    name: String,
    signature: String?,
    superName: String?,
    interfaces: Array<out String>?
  ) {
    super.visit(version, access, name, signature, superName, interfaces)
    mName = name
    if (interfaces == null
      || interfaces.isEmpty()
      || !name.endsWith("CommercialServiceRegister")
      || !interfaces.joinToString { it }
        .contains("com/kuaishou/commercial/utility/ioc/ICommercialServiceRegister")
    ) {
      return
    }
    println(">>> RegisterListClassVisitor,add name: $name , interfaces: ${interfaces[0]}")

    //将各个module的CommercialServiceRegister添加进来
    registerList.add(name)
  }

  override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {
    if (registerCollectorName.size <= 0 &&
      desc != null &&
      desc.endsWith("RegisterCollector;")
    ) {
      registerCollectorName.add(mName)
    }
    return super.visitAnnotation(desc, visible)
  }
}