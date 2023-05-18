package com.kuaishou.commercial.utility.ioc.core;

/**
 * 生产service的工厂类
 *
 * @param <T> 要生产service的泛型
 */
public abstract class Factory<T> {
  T mInstance;

  protected abstract T newInstance();

  final T getInstance() {
    if (this.mInstance == null) {
      this.mInstance = this.create();
    }
    return this.mInstance;
  }

  private T create() {
    return this.newInstance();
  }
}
