package com.kuaishou.commercial.utility.compiler;

import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public class Utils {
  public static TypeName getServiceInterface(TypeElement element) {
    List<? extends TypeMirror> typeMirrors = element.getInterfaces();
    for (TypeMirror typeMirror : typeMirrors) {
      if (typeMirror.toString().startsWith("com.kuaishou.commercial.utility.ioc.interfaces")) {
        return ClassName.get(typeMirror);
      }
    }
    return ClassName.get(element);
  }
}
