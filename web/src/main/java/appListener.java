import core.Settings;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.io.IOException;

@WebListener
public class appListener implements ServletContextListener{

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        System.out.println("ServletContextListener destroyed");
        try {
            FileUtils.deleteDirectory(Settings.baseLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Run this before web application is started
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        System.out.println("ServletContextListener started");
        try {
            FileUtils.deleteDirectory(Settings.baseLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Settings.baseLocation.mkdir();
    }
}