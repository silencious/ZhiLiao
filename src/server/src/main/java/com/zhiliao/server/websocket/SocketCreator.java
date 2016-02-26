package com.zhiliao.server.websocket;

import com.zhiliao.server.model.User;
import com.zhiliao.server.service.UserService;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by riaqn on 15-8-14.
 */
@Component
public class SocketCreator implements WebSocketCreator {
    private static final Logger logger = LoggerFactory.getLogger(SocketCreator.class);

    @Autowired
    private ApplicationContext ac;

    @Autowired
    private Reflections reflections;

    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        MessageSocket socket;
        try {
            socket = ac.getBean(MessageSocket.class, req, resp);
        } catch (Exception e) {
            resp.complete();
            //to get around a bug in jetty
            return 0;
        }
        return socket;
    }
}
