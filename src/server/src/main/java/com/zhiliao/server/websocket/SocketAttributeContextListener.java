package com.zhiliao.server.websocket;

import com.zhiliao.server.websocket.annotation.SocketAttribute;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * Created by riaqn on 15-8-16.
 */
@Component
public class SocketAttributeContextListener implements  SocketContextListener {
    private static final Logger logger = LoggerFactory.getLogger(SocketAttributeContextListener.class);

    @Autowired
    Reflections reflections;

    @Autowired
    ApplicationContext ac;

    Set<Method> methods;

    @PostConstruct
    public void setup() {
        methods = reflections.getMethodsAnnotatedWith(SocketAttribute.class);
    }

    @Override
    public void contextInitialized(Map<Object, Object> context, ServletUpgradeRequest request, ServletUpgradeResponse response) {
        for (Method method : methods) {
            SocketAttribute sa = method.getAnnotation(SocketAttribute.class);
            Object object = ac.getBean(method.getDeclaringClass());
            try {
                context.put(sa.value(), method.invoke(object));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void contextDestroyed(Map<Object, Object> context, int statusCode, String reason) {
        //I guess the context will be destroyed
    }
}
