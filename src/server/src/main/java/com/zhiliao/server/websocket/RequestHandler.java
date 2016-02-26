package com.zhiliao.server.websocket;


import com.zhiliao.message.client.*;
import com.zhiliao.message.server.*;
import com.zhiliao.server.exception.ErrorCode;
import com.zhiliao.server.model.User;
import com.zhiliao.server.websocket.annotation.MessageMapping;
import com.zhiliao.server.websocket.rest.RequestMapping;
import org.apache.commons.collections4.IteratorUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import com.zhiliao.server.model.rest.Resource;
import com.zhiliao.server.exception.ErrorResponseException;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Controller
public class RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    @Autowired
    Reflections reflections;

    @Autowired
    ApplicationContext ac;

    Map<Class<? extends Request>, Map<Class<? extends Resource>, AbstractMap.SimpleEntry<Object, Method>>> controllers  =
            new HashMap<>();

    Set<Class<?>> classSet;

    @PostConstruct
    public void setup() {
        for (Class<? extends Request> method : reflections.getSubTypesOf(Request.class)) {
            controllers.put(method, new HashMap<>());
        }
        classSet = reflections.getTypesAnnotatedWith(RequestMapping.class);

        for (Class clazz : classSet) {
            RequestMapping rm = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
            Object object = ac.getBean(clazz);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isBridge()) {
                    for (Class<? extends Request> requestMethod : reflections.getSubTypesOf(Request.class)) {
                        if (requestMethod.getSimpleName().toLowerCase().startsWith(method.getName())) {
                            logger.info("{} {} -> {}", requestMethod.getSimpleName(), rm.value(), method);
                            for (Class resType : rm.value())
                                controllers.get(requestMethod).put(resType, new AbstractMap.SimpleEntry<Object, Method>(object, method));
                        }
                    }
                }
            }
        }
    }

    private void handle(MessageSocket socket, Class<? extends  Resource> resType, Request request, Map<Class, Object> context) {
        long mark = request.getMark();

        context.put(MessageSocket.class, socket);
        context.put(User.class, socket.getContext().get("user"));
        AbstractMap.SimpleEntry<Object, Method> entry = controllers.get(request.getClass()).get(resType);
        if (entry == null) {
            ErrorResponse response = new ErrorResponse(mark, ErrorCode.MethodNotSupported);
            socket.send(response);
            return ;
        }
        Object object = entry.getKey();
        Method method = entry.getValue();
        Object[] args = new Object[method.getParameterCount()];
        int idx = 0;
        for (Parameter parameter : method.getParameters()) {
            Object arg = null;
            for (Object value : context.values())
                if (parameter.getType().isInstance(value)) {
                    arg = value;
                    break;
                }
            if (arg == null && !parameter.isAnnotationPresent(Nullable.class)) {
                ErrorResponse response = new ErrorResponse();
                response.setMark(mark);
                response.setError(ErrorCode.BadRequest);
                socket.send(response);
                return ;
            }
            args[idx++] = arg;
        }
        Object ret = null;
        try {
            ret = method.invoke(object, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof ErrorResponseException) {
                ErrorResponseException e0 = (ErrorResponseException) e.getCause();
                ErrorResponse response = new ErrorResponse();
                response.setMark(mark);
                response.setError(e0.getError());
                response.setReason(e0.getReason());
                socket.send(response);
            } else
                e.printStackTrace();
            return ;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return ;
        }
        Response response = null;
        if (request instanceof GetRequest)
            response = new GetResponse(mark, (Resource) ret);
        else if (request instanceof  ListRequest) {
            response = new ListResponse(mark, IteratorUtils.toList(((Iterable<Resource>)ret).iterator()));
        } else if (request instanceof  PostRequest)
            response = new PostResponse(mark, (Long) ret);
        else if (request instanceof  DeleteRequest)
            response = new DeleteResponse(mark, ret);

        socket.send(response);
    }

    @MessageMapping(ListRequest.class)
    public void list(MessageSocket socket,
                     ListRequest request) {
        Map<Class, Object> context = new HashMap<>();
        context.put(Resource.class, request.getResource());
        handle(socket, request.getResource().getClass(), request, context);
    }

    @MessageMapping(DeleteRequest.class)
    public void delete(MessageSocket socket,
                       DeleteRequest request) {
        Map<Class, Object> context = new HashMap<>();
        context.put(Resource.class, request.getResource().getId());
        handle(socket, request.getResource().getClass(), request, context);
    }

   @MessageMapping(GetRequest.class)
    public void get(MessageSocket socket,
                    GetRequest request) {
        Map<Class, Object> context = new HashMap<>();
        context.put(Resource.class, request.getResource().getId());
       handle(socket, request.getResource().getClass(), request, context);
    }

    @MessageMapping(PostRequest.class)
    public void post(MessageSocket socket,
                       PostRequest request) {
        Map<Class, Object> context = new HashMap<>();
        context.put(Resource.class, request.getResource());
        handle(socket, request.getResource().getClass(), request, context);
    }
}
