package com.zhiliao.server.websocket;

import org.eclipse.jetty.websocket.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebServlet;

/**
 * Created by riaqn on 15-8-14.
 */
@WebServlet(name="Message Websocket Servlet", urlPatterns = {"/msg"})
@Component
public class MessageServlet extends WebSocketServlet {
    private static final Logger logger = LoggerFactory.getLogger(MessageServlet.class);
    @Autowired
    private ApplicationContext ac;

    @Override
    public void configure(WebSocketServletFactory factory) {
        ac = (ApplicationContext)this.getServletContext().getAttribute("applicationContext");
        factory.setCreator(ac.getBean(SocketCreator.class));
    }
}
