package com.kuaishou.commercial.utility.ioc.core;

import com.kuaishou.commercial.utility.ioc.util.ProxyUtils;

public class ServiceProperty<T> {
  private Class<? extends T> mServiceClass;
  private Factory<? extends T> mServiceFactory;
  private int mPriority;

  public ServiceProperty(Class<? extends T> serviceClass, Factory<? extends T> factory,
      int priority) {
    if (serviceClass == null || factory == null) {
      throw new IllegalArgumentException("class or factory is null");
    }
    mServiceClass = serviceClass;
    mServiceFactory = factory;
    mPriority = priority;
  }

  public int getPriority() {
    return mPriority;
  }

  public Class<? extends T> getServiceClass() {
    return mServiceClass;
  }

  public Factory<? extends T> getServiceFactory() {
    return mServiceFactory;
  }

  public synchronized T getInstance() {
    T getInstance = (T) mServiceFactory.getInstance();
    if (getInstance == null) {
      // 当获取对象拿不到的时候instance通过动态代理去获取
      getInstance = mServiceClass != null ? ProxyUtils.getInstance(mServiceClass) : null;
    }
    if (getInstance == null) {
      try {
        // 当获取对象拿不到的时候instance通过反射去获取
        getInstance = mServiceClass != null ? mServiceClass.newInstance() : null;
      } catch (IllegalAccessException | InstantiationException e) {
        e.printStackTrace();
      }
    }
    return getInstance;
  }
}
