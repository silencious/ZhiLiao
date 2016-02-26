package com.zhiliao.server.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by riaqn on 15-8-15.
 */
public class SpringApplicationContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ApplicationContext ac = new ClassPathXmlApplicationContext(sce.getServletContext()
                .getInitParameter("contextConfigLocation"));

        sce.getServletContext().setAttribute("applicationContext", ac);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().setAttribute("applicationContext", null);

    }
}
