package com.kuaishou.commercial.utility.ioc.register;

@RegisterCollector
public class ServiceRegisterCollection {
  /**
   * 宿主调用register，编译期会通过ASM插入register的代码
   */
  public static void register() {}
}
