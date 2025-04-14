package pt.unl.fct.di.apdc.firstwebapp.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppInitializerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppInit.initRootUser();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
