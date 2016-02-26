package com.zhiliao.server.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zhiliao.message.server.ServerMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiliao.message.client.ClientMessage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;


@Component
@Scope("prototype")
@WebSocket(maxTextMessageSize = 64 * 1024)
public class MessageSocket {
    private static Logger logger = LoggerFactory.getLogger(MessageSocket.class);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MessageDelegator delegator;

    private Session session;

    public Map<Object, Object> getContext() {
        return context;
    }

    public void setContext(Map<Object, Object> context) {
        this.context = context;
    }

    private Map<Object, Object> context = new HashMap<>();

    @Autowired
    Reflections reflections;

    @Autowired
    ApplicationContext ac;

    ServletUpgradeRequest request;
    ServletUpgradeResponse response;

    public MessageSocket(ServletUpgradeRequest request, ServletUpgradeResponse response) {
        this.request = request;
        this.response = response;
    }

    @PostConstruct
    public void setup() {
        for (Class<? extends SocketContextListener> clazz : reflections.getSubTypesOf(SocketContextListener.class)) {
            SocketContextListener listener = ac.getBean(clazz);
            listener.contextInitialized(context, request, response);
            if (response.getStatusCode() !=  HttpServletResponse.SC_OK) {
                response.setSuccess(false);
                throw new RuntimeException();
            }
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        for (Class<? extends SocketContextListener> clazz : reflections.getSubTypesOf(SocketContextListener.class)) {
            SocketContextListener listener = ac.getBean(clazz);
            listener.contextDestroyed(context, statusCode, reason);
        }
        this.session = null;
    }

    public boolean send(ServerMessage msg) {
        if (this.session == null)
            return false;
        logger.debug("{}", msg);
        try {
            this.session.getRemote().sendStringByFuture(mapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return true;
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        ClientMessage cMessage;
        try {
            cMessage = mapper.readValue(msg, ClientMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        logger.debug("{}", cMessage);

        if (!delegator.delegate(cMessage, this)) {
            logger.debug("bad request detected, closing the socket");
            this.session.close(500, "Bad Request");
        }
    }
}
