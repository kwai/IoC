package com.kuaishou.commercial.utility.ioc.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyUtils {
  private ProxyUtils() {
  }

  public static final InvocationHandler sInvocationHandler = new InvocationHandler() {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Class<?> retType = method.getReturnType();
      if (ClassUtils.isInteger(retType)) {
        return 0;
      } else if (ClassUtils.isBoolean(retType)) {
        return false;
      } else if (ClassUtils.isString(retType)) {
        return "";
      } else if (retType.isInterface()) {
        Object object;
        try {
          object = newProxy(retType, this);
        } catch (Exception ignore) {
          return null;
        }
        return object;
      } else {
        return null;
      }
    }
  };

  private static <T> T newProxy(Class<T> interfaceType, InvocationHandler handler) {
    Object object =
        Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, handler);
    return interfaceType.cast(object);
  }

  public static <T, B extends T> B getInstance(Class<B> tClass) {
    // 动态代理补充实现
    Object object =
        Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, sInvocationHandler);
    return tClass.cast(object);
  }
}
