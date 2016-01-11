package de.uniOldenburg.model;

import org.opencv.core.Core;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class Server implements ServletContextListener {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public void contextInitialized(ServletContextEvent sce) {
    }

    public void contextDestroyed(ServletContextEvent sce) {

    }
}