package com.zhiliao.server.websocket;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;

import java.util.Map;

/**
 * Created by riaqn on 15-8-15.
 */
public interface SocketContextListener {
    void contextInitialized(Map<Object, Object> context, ServletUpgradeRequest request, ServletUpgradeResponse response);
    void contextDestroyed(Map<Object, Object> context, int statusCode, String reason);
}
