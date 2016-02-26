package com.zhiliao.server.websocket;

import com.zhiliao.server.model.User;
import com.zhiliao.server.service.UserService;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Map;

/**
 * Created by riaqn on 15-8-16.
 */
@Component
public class AuthenticationContextListener implements SocketContextListener {
    @Autowired
    UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationContextListener.class);
    private User authenticate(String authorization) {
        if (authorization == null)
            return null;
        String[] splited = authorization.split(" ");
        if (splited.length != 2)
            return null;

        //Currently only support basic method
        if (!splited[0].equals("Basic"))
            return null;

        String pair = new String(Base64.getDecoder().decode(splited[1]));
        splited = pair.split(":");
        if (splited.length != 2)
            return null;

        User user = userService.getUserByUsername(splited[0]);
        if (user == null)
            return null;

        if (!splited[1].equals(user.getPassword()))
            return null;

        return user;
    }


    @Override
    public void contextInitialized(Map<Object, Object> context, ServletUpgradeRequest request, ServletUpgradeResponse response) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null)
            return ;
        User user = authenticate(authorization);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ;
        }
        logger.debug("user = {}", user);
        context.put("user", user);
    }

    @Override
    public void contextDestroyed(Map<Object, Object> context, int statusCode, String reason) {
    }
}
