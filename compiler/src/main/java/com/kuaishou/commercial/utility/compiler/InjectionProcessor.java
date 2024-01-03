package com.kuaishou.commercial.utility.compiler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;
import com.kuaishou.commercial.utility.annotation.InjectFactory;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;

@AutoService(Processor.class)  //自动注册
public class InjectionProcessor extends AbstractProcessor {
  private Messager mMessager;
  private Elements mElementUtils;
  //当前注解的全类名作为key
  private final Map<String, FactoryClassCreatorProxy> mFactoryProxyMap = new HashMap<>();
  private RegisterClassCreatorProxy registerProxy = null;
  //代码生成路径，module可用annotationProcessorOptions自定义
  private String mModulePackageName = "com.kuaishou.commercial.build";

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    Map<String, String> options = processingEnv.getOptions();
    if (options != null) {
      mModulePackageName = options.get("iocModuleName");
    }
    mMessager = processingEnv.getMessager();
    mElementUtils = processingEnv.getElementUtils();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    HashSet<String> supportTypes = new LinkedHashSet<>();
    supportTypes.add(InjectFactory.class.getCanonicalName());
    return supportTypes;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    registerProxy = null;
    mMessager.printMessage(Diagnostic.Kind.NOTE, "processing..." + this);
    mFactoryProxyMap.clear();
    //得到所有的注解
    Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(InjectFactory.class);

    for (Element element : elements) {
      if (element instanceof TypeElement) {
        mMessager.printMessage(Diagnostic.Kind.NOTE,
            "process element:" + ((TypeElement) element).getQualifiedName());

        TypeElement classElement = (TypeElement) element;
        String fullClassName = classElement.getQualifiedName().toString();
        FactoryClassCreatorProxy factoryProxy = mFactoryProxyMap.get(fullClassName);
        if (factoryProxy == null) {
          factoryProxy = new FactoryClassCreatorProxy(classElement);
          mFactoryProxyMap.put(fullClassName, factoryProxy);
        }

        if (registerProxy == null) {
          registerProxy = new RegisterClassCreatorProxy();
        }
      }
    }
    //通过遍历mProxyMap，创建java文件
    for (String key : mFactoryProxyMap.keySet()) {
      FactoryClassCreatorProxy proxyInfo = mFactoryProxyMap.get(key);
      JavaFile javaFile = JavaFile.builder(mModulePackageName, proxyInfo
          .generateJavaCode())
          .build();
      try {
        //生成Factory文件
        javaFile.writeTo(processingEnv.getFiler());
      } catch (IOException e) {
        e.printStackTrace();
      }


      //获取factory的classname
      ClassName factoryClass = ClassName.get(javaFile.packageName, javaFile.typeSpec.name);
      mMessager.printMessage(Diagnostic.Kind.NOTE, "已生成的factory:" + factoryClass);
      mMessager.printMessage(Diagnostic.Kind.NOTE, "key:" + key);
      //获取factory的priority
      InjectFactory injectFactory = proxyInfo.getTypeElement().getAnnotation(InjectFactory
          .class);
      int priority = injectFactory.priority();
      //将factory和对应的priority放到RegisterProxy中，准备生成代码
      TypeName serviceInterface = Utils.getServiceInterface(proxyInfo.getTypeElement());
      registerProxy.putElement(factoryClass,
          new ServicePriorityWrap(serviceInterface, priority));
    }

    if (registerProxy != null) {
      JavaFile javaFile = JavaFile.builder(mModulePackageName, registerProxy
          .generateJavaCode()).build();
      try {
        //生成文件
        javaFile.writeTo(processingEnv.getFiler());
      } catch (IOException e) {
        e.printStackTrace();
      }

      mMessager.printMessage(Diagnostic.Kind.NOTE, "process finish ...");
    }
    return true;
  }
}
