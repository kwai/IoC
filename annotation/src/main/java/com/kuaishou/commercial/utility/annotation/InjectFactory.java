package com.kuaishou.commercial.utility.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface InjectFactory {
  /**
   * 优先级
   * NOTE: priority值越大优先级越高
   */
  int priority() default 0;
}