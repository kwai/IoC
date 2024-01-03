package com.kuaishou.commercial.utility.compiler;

import com.squareup.javapoet.TypeName;

public class ServicePriorityWrap {
  public TypeName serviceClassName;
  public int priority;

  public ServicePriorityWrap(TypeName serviceClassName, int priority) {
    this.serviceClassName = serviceClassName;
    this.priority = priority;
  }
}
