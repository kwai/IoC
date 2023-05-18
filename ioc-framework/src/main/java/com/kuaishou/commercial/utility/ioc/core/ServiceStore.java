package com.kuaishou.commercial.utility.ioc.core;

import java.util.Map;

public class ServiceStore<Base> {
  private final Map<Class<? extends Base>, ServiceProperty<? extends Base>> mServiceMappings;

  public ServiceStore(
      Map<Class<? extends Base>, ServiceProperty<? extends Base>> serviceMappings) {
    mServiceMappings = serviceMappings;
  }

  public <T extends Base> T get(Class<T> serviceClass) {
    if (serviceClass == null) {
      return null;
    }
    ServiceProperty<? extends Base> serviceProperty = mServiceMappings.get(serviceClass);
    if (serviceProperty == null) {
      return null;
    }
    return (T) serviceProperty.getInstance();
  }

  public boolean registerServiceProperty(Class<? extends Base> tClass,
      ServiceProperty<? extends Base> property) {
    // 非接口类不能注册，实现类不能注册，需要Base接口的子类接口
    if (!tClass.isInterface()) {
      return false;
    }
    if (property == null) {
      return false;
    }
    ServiceProperty<? extends Base> getValue = mServiceMappings.get(tClass);
    if (getValue != null && getValue.getPriority() > property.getPriority()) {
      return false;
    }
    ServiceProperty<? extends Base> putValue = mServiceMappings.put(tClass, property);
    return putValue != null;
  }
}
