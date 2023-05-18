package com.kuaishou.commercial.utility.ioc.interfaces;

/**
 * IoC的服务接口基类，所有的服务必须实现这个接口
 */
public interface Service {
  /**
   * 是否可用，默认是true
   */
  default boolean isAvailable() {
    return true;
  }
}
