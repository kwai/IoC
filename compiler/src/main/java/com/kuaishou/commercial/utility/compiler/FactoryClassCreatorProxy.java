package com.kuaishou.commercial.utility.compiler;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class FactoryClassCreatorProxy {

  private final String mBindingClassName;
  private final TypeElement mTypeElement;
  private final String mClassName;

  public FactoryClassCreatorProxy(TypeElement classElement) {
    this.mTypeElement = classElement;
    mClassName = mTypeElement.getSimpleName().toString();
    this.mBindingClassName = mClassName + "Factory";
  }

  public TypeElement getTypeElement() {
    return mTypeElement;
  }

  /**
   * 创建Java代码
   *
   * @return TypeSpec
   */
  public TypeSpec generateJavaCode() {
    TypeName serviceInterface = Utils.getServiceInterface(mTypeElement);
    return TypeSpec.classBuilder(mBindingClassName)
        .addModifiers(Modifier.PUBLIC)
        .superclass(ParameterizedTypeName.get(ClassName.get("com.kuaishou.commercial.utility.ioc.core","Factory"),
            serviceInterface))
        .addMethod(generateMethods())
        .build();
  }

  /**
   * 加入Method
   */
  private MethodSpec generateMethods() {
    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("newInstance")
        .addModifiers(Modifier.PROTECTED)
        .returns(ClassName.get(mTypeElement));
    methodBuilder.addCode("return new " + mClassName + "();");
    return methodBuilder.build();
  }
}
