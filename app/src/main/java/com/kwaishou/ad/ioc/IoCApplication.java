package com.kwaishou.ad.ioc;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class IoCApplication extends Application {
  @SuppressLint("StaticFieldLeak")
  public static Context sContext;

  @Override
  public void onCreate() {
    super.onCreate();
    sContext = getApplicationContext();
  }
}
