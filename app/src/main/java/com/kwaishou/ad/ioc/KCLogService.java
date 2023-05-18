package com.kwaishou.ad.ioc;

import com.kuaishou.commercial.utility.ioc.interfaces.Service;

public interface KCLogService extends Service {
  void logMessage(String message);
}