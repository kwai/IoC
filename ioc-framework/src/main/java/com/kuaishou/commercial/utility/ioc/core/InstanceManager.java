package com.kuaishou.commercial.utility.ioc.core;

import java.util.Map;

public class InstanceManager<Base> {
  private final ServiceStore<Base> mServiceStore;

  public InstanceManager(
      Map<Class<? extends Base>, ServiceProperty<? extends Base>> serviceMappings) {
    mServiceStore = new ServiceStore<>(serviceMappings);
  }

  public <T extends Base> T get(Class<T> serviceClass) {
    return mServiceStore.get(serviceClass);
  }

  public boolean registerFactory(Class<? extends Base> tClass, Factory<? extends Base> factory) {
    return registerServiceProperty(tClass,
        new ServiceProperty<>(tClass, factory, Constants.MIN_PRIORITY));
  }

  public boolean registerFactory(Class<? extends Base> tClass, Factory<? extends Base> factory,
      int priority) {
    return registerServiceProperty(tClass, new ServiceProperty<>(tClass, factory, priority));
  }

  /**
   * 可覆盖注册service，比如新注册的service优先级高于现有的，则会覆盖。
   *
   * @return 注册是否成功。
   */
  public boolean registerServiceProperty(Class<? extends Base> tClass,
      ServiceProperty<? extends Base> property) {
    return mServiceStore.registerServiceProperty(tClass, property);
  }
}
