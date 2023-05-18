package com.kuaishou.commercial.utility.ioc.compoment;

/**
 * 组件的生命周期接口，可用于注册自己的业务服务等。
 */
public interface ComponentLifecycle {
  /**
   * SDK 初始化接口
   */
  void onInit();
}
