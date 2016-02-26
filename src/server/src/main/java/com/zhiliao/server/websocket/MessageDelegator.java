package com.zhiliao.server.websocket;

import com.zhiliao.message.client.ClientMessage;
import com.zhiliao.message.server.ErrorResponse;
import com.zhiliao.server.websocket.annotation.MessageMapping;
import org.apache.commons.collections4.map.MultiValueMap;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by riaqn on 15-8-14.
 */
@Component
public class MessageDelegator {
    private static final Logger logger = LoggerFactory.getLogger(MessageDelegator.class);
    private MultiValueMap<Class<? extends ClientMessage>, AbstractMap.SimpleEntry<Object, Method>> handlers
            = new MultiValueMap<>();

    @Autowired
    ApplicationContext ac;

    @Autowired
    Reflections reflections;

    @PostConstruct
    public void setup() {
        Set<Method> resources = reflections.getMethodsAnnotatedWith(MessageMapping.class);
        for (Method method : resources) {
            MessageMapping messageMapping = method.getAnnotation(MessageMapping.class);
            AbstractMap.SimpleEntry<Object, Method> entry = new AbstractMap.SimpleEntry<Object, Method>(
                    ac.getBean(method.getDeclaringClass()),
                    method
            );
            handlers.put(messageMapping.value(), entry);
            logger.info("{} -> {}", messageMapping.value(), method);
        }
    }

    public boolean delegate(ClientMessage msg, MessageSocket socket) {
        for (Map.Entry<Object, Method> entry : handlers.getCollection(msg.getClass())) {
            try {
                entry.getValue().invoke(entry.getKey(), socket, msg);
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
