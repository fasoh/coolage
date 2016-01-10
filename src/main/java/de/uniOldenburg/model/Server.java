package de.uniOldenburg.model;

import org.opencv.core.Core;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Server implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    }

    public void contextDestroyed(ServletContextEvent sce) {

    }
}
