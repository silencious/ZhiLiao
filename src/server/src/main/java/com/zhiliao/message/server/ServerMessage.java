package com.zhiliao.message.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ServerMessage {
    @Override
    public String toString() {
        return "ServerMessage{}";
    }
}
