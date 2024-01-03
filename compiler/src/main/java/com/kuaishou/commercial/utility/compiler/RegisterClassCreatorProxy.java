package com.kuaishou.commercial.utility.compiler;

import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Modifier;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class RegisterClassCreatorProxy {

  private final Map<ClassName, ServicePriorityWrap> mFactoryMap = new HashMap<>();


  public RegisterClassCreatorProxy() {
  }


  public void putElement(ClassName className, ServicePriorityWrap wrap) {
    mFactoryMap.put(className, wrap);
  }

  /**
   * 创建Java代码
   *
   * @return TypeSpec
   */
  public TypeSpec generateJavaCode() {
    return TypeSpec.classBuilder("CommercialServiceRegister")
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(ClassName.get("com.kuaishou.commercial.utility.ioc","ICommercialServiceRegister"))
        .addMethod(generateMethods())
        .build();
  }

  /**
   * 加入Method
   */
  private MethodSpec generateMethods() {
    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("register")
        .addModifiers(Modifier.PUBLIC)
        .returns(void.class);
    for (ClassName key : mFactoryMap.keySet()) {
      methodBuilder
          .addStatement("$T.register($T.class, new $T(), " + mFactoryMap.get(key).priority + ")",
              ClassName.get("com.kuaishou.commercial.utility.ioc","ServiceManager"),
              mFactoryMap.get(key).serviceClassName,
              key);
    }
    return methodBuilder.build();
  }
}
