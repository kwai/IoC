package com.kwaishou.ad.ioc;

import com.kuaishou.commercial.utility.ioc.interfaces.Service;

public interface KCToastService extends Service {
  void toast(String message);
}