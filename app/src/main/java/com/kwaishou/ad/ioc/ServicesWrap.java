package com.kwaishou.ad.ioc;


import static com.kwaishou.ad.ioc.IoCApplication.sContext;

import android.util.Log;
import android.widget.Toast;

import com.kuaishou.commercial.utility.ioc.core.Factory;

/**
 * 服务的一些实现，通常由上层声明
 */
public class ServicesWrap {
  public static class LogServiceFactory extends Factory<KCLogService> {

    @Override
    public KCLogService newInstance() {
      return new LogServiceImpl();
    }
  }

  public static class LogServiceImpl implements KCLogService {
    @Override
    public void logMessage(String message) {
      Log.i("DEMO", message);
    }
  }

  public static class ToastServiceFactory extends Factory<KCToastService> {

    @Override
    public KCToastService newInstance() {
      return new ToastServiceImpl();
    }
  }

  public static class ToastServiceImpl implements KCToastService {
    @Override
    public void toast(String message) {
      Toast.makeText(sContext, message, Toast.LENGTH_SHORT).show();
    }
  }
}
