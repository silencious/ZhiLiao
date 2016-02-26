package com.zhiliao.server.websocket.rest;

import com.zhiliao.server.model.rest.Resource;

import java.lang.annotation.*;

/**
 * Created by riaqn on 15-8-16.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    Class<? extends Resource>[] value();
}
