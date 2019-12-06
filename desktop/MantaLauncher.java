import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.ServerSocket;
import java.net.DatagramSocket;

import org.apache.commons.lang3.SystemUtils;

public class MantaLauncher {

    public static void main(String[] args) throws ServletException, LifecycleException, URISyntaxException, IOException{
        Path appPath = Paths.get(MantaLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        System.out.println("Application path: " + appPath.toString());
        if (SystemUtils.IS_OS_MAC) {
            initForMac(appPath);
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(appPath.toString());

        if (SystemUtils.IS_OS_MAC) {
            Path libPath = Paths.get(System.getProperty("user.home") + "/Library/Manta");
            tomcat.getHost().setAppBase(libPath.toString());
        } else {
            tomcat.getHost().setAppBase(appPath.toString());
        }

        tomcat.addWebapp("/manta", appPath.resolve("manta.war").toString());

        if (SystemUtils.IS_OS_WINDOWS) {
            //Setting for exewrap
            TomcatURLStreamHandlerFactory.disable();
        }

        Integer port = 8080;
        Integer trial = 0;
        while (!available(port) && trial++ < 10){
            System.out.println("Port "+port.toString()+" is already in use. trying next port.");
            port++;
        }
        if (!available(port)){
            System.out.println("Port "+port.toString()+" is already in use.");
            System.out.println("Could not find available port " + trial.toString() + " times. MantaLauncher aborted to launch.");
            return;
        }
        tomcat.setPort(port);
        tomcat.start();
        String url = "http://localhost:"+port.toString()+"/manta";
        System.out.println("Manta started on " + url + " . ");

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
        }

        tomcat.getServer().await();
    }

    private static void initForMac(Path appPath) throws IOException {
        System.out.println("MantaLauncher: Initialize for MacOS.");
        Path dbPath = Paths.get(System.getProperty("user.home") + "/Library/Manta/gutflora.db");
        if (Files.notExists(dbPath)){
            Files.createDirectories(dbPath.getParent());
            Files.copy(appPath.resolve("gutflora.db"), dbPath);
            System.out.println("Database file is copied to " + dbPath.toString());
        }
    }

    /**
    * Checks to see if a specific port is available.
    *
    * @param port the port to check for availability
    */
    private static boolean available(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }
}
