package com.kuaishou.commercial.utility.ioc.register;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * transform查找到该注解的类，插桩register
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@interface RegisterCollector {
}
