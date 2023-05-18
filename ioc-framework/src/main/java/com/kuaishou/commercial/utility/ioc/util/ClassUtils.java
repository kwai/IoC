package com.kuaishou.commercial.utility.ioc.util;

public class ClassUtils {
  private ClassUtils() {
  }

  public static boolean isNumber(Class<?> clss) {
    return isInteger(clss) || isFloatingPoint(clss);
  }

  public static boolean isInteger(Class<?> clss) {
    return clss == Byte.TYPE || clss == Character.TYPE || clss == Short.TYPE || clss == Integer.TYPE || clss == Long.TYPE || clss == Byte.class || clss == Character.class || clss == Short.class || clss == Integer.class || clss == Long.class;
  }

  public static boolean isFloatingPoint(Class<?> clss) {
    return clss == Float.TYPE || clss == Double.TYPE || clss == Float.class || clss == Double.class;
  }

  public static boolean isBoolean(Class<?> clss) {
    return clss == Boolean.TYPE || clss == Boolean.class;
  }

  public static boolean isVoid(Class<?> clss) {
    return clss == Void.TYPE || clss == Void.class;
  }

  public static boolean isString(Class<?> clss) {
    return clss == String.class;
  }
}

