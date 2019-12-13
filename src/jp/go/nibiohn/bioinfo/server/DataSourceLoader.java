package jp.go.nibiohn.bioinfo.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.net.URISyntaxException;

import org.apache.commons.lang.SystemUtils;

// For server version
// import jp.go.nibiohn.bioinfo.shared.GutFloraConfig;

public class DataSourceLoader{
    public static String backend;

    private static HikariConfig config;

    public static HikariDataSource getHikariDataSource() {
		// if (config == null) {
        //     /////////// Server version ///////////////
        //     backend = "postgresql";
        //     Properties props = new Properties();
		// 	try {
		// 		Class.forName("org.postgresql.Driver");
		// 		props.load(GutFloraServiceImpl.class.getClassLoader().getResourceAsStream(GutFloraConfig.PGSQL_PROP_FILE));
		// 	} catch (IOException e) {
		// 		throw new RuntimeException("Problem loading properties '" + GutFloraConfig.PGSQL_PROP_FILE + "'", e);
		// 	} catch (ClassNotFoundException e) {
		// 		e.printStackTrace();
		// 	}
        //     config = new HikariConfig(props);
        //     //////////////////////////////////////////
        // }

        ///////////// Desktop version ///////////////
        backend = "sqlite";
        config = new HikariConfig();
        try {
            String dbPath;
            String options = "";
            Path classPath = Paths.get(DataSourceLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (SystemUtils.IS_OS_MAC) {
                dbPath = Paths.get(System.getProperty("user.home") + "/Library/Manta/gutflora.db").toString();
            } else {
                dbPath = classPath.getParent().getParent().getParent().resolve("gutflora.db").toString();
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                options = "?journal_mode=WAL&synchronous=OFF";
            }
            if (dbPath == null) {
                dbPath = "gutflora.db";
            }
            config.setJdbcUrl("jdbc:sqlite:" + dbPath + options);
        } catch (InvalidPathException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        ////////////////////////////////////////////
        return new HikariDataSource(config);
    }
}