import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MantaLauncher {
 
    public static void main(String[] args) throws ServletException, LifecycleException, URISyntaxException, IOException{
        Path cp = Paths.get(MantaLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        System.out.println("Application path: " + cp.toString());

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(cp.toString());
        tomcat.setPort(8080);         
        tomcat.getHost().setAppBase(cp.toString());
        
        tomcat.addWebapp("/manta", cp.resolve("manta.war").toString());
        
        //Setting for exewrap
        TomcatURLStreamHandlerFactory.disable();

        tomcat.start();
    
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI("http://localhost:8080/manta"));
        }
        
        tomcat.getServer().await();
    }
}