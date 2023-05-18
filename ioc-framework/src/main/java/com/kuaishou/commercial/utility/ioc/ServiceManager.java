package com.kuaishou.commercial.utility.ioc;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kuaishou.commercial.utility.ioc.core.Constants;
import com.kuaishou.commercial.utility.ioc.core.Factory;
import com.kuaishou.commercial.utility.ioc.core.InstanceManager;
import com.kuaishou.commercial.utility.ioc.core.ServiceProperty;
import com.kuaishou.commercial.utility.ioc.interfaces.Service;
import com.kuaishou.commercial.utility.ioc.register.ServiceRegisterCollection;

/**
 * 接口服务的管理类，用于注册或者过去服务接口实现类的对象。
 */
public class ServiceManager {
  public static final InstanceManager<Service> sManager = new InstanceManager<>(
      new HashMap<>(
          Constants.MAP_INIT_CAPACITY));

  @SuppressLint("StaticFieldLeak")
  private static Context mContext;

  private ServiceManager() {
  }

  public static void init(@NonNull Context context) {
    mContext = context;
    ServiceRegisterCollection.register();
  }

  @NonNull
  public static Context getContext() {
    return mContext;
  }

  /**
   * 拿到的service还是可能为空的
   */
  @Nullable
  public static <T extends Service> T get(@NonNull Class<T> serviceClass) {
    return sManager.get(serviceClass);
  }

  public static <T extends Service> void register(Class<T> clazz, Factory<T> factory) {
    sManager.registerFactory(clazz, factory);
  }

  public static <T extends Service> void register(Class<T> clazz, Factory<T> factory,
      int priority) {
    sManager.registerFactory(clazz, factory, priority);
  }

  /**
   * 可覆盖注册service，比如新注册的service优先级高于现有的，则会覆盖。
   */
  public static <T extends Service> void register(Class<T> clazz, ServiceProperty<T> property) {
    sManager.registerServiceProperty(clazz, property);
  }
}