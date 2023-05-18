package com.kuaishou.commercial.utility.ioc.compoment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

/**
 * 组件的管理，负责注册生命周期等。
 */
public class ComponentManager {
  private final static ComponentManager mInstance = new ComponentManager();

  private ComponentManager() {}

  public static ComponentManager getInstance() {
    return mInstance;
  }

  private final List<ComponentLifecycle> mComponentLifecycles = new ArrayList<>();

  /**
   * @param componentLifecycle 组件的生命周期
   */
  @MainThread
  public void addComponentLifecycle(@NonNull ComponentLifecycle componentLifecycle) {
    mComponentLifecycles.add(componentLifecycle);
  }

  @MainThread
  public void init() {
    for (ComponentLifecycle componentLifecycle : mComponentLifecycles) {
      componentLifecycle.onInit();
    }
  }
}
