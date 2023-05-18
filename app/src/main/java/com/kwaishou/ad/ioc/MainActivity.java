package com.kwaishou.ad.ioc;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.kuaishou.commercial.utility.ioc.ServiceManager;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    registerService();
    findViewById(R.id.show).setOnClickListener(v -> {
      //noinspection ConstantConditions
      ServiceManager.get(KCLogService.class).logMessage("点击了");
      //noinspection ConstantConditions
      ServiceManager.get(KCToastService.class).toast("点击了");
    });
  }

  /**
   * 注册服务
   */
  private void registerService() {
    ServiceManager.register(KCLogService.class, new ServicesWrap.LogServiceFactory());
    ServiceManager.register(KCToastService.class, new ServicesWrap.ToastServiceFactory());
  }
}