package jp.go.nibiohn.bioinfo.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.net.URISyntaxException;

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
            Path classPath = Paths.get(DataSourceLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String dbPath = classPath.getParent().getParent().getParent().resolve("gutflora.db").toString();
            if (dbPath == null) {
                dbPath = "gutflora.db";
            }
            System.out.println("Database path: " + dbPath);
            config.setJdbcUrl("jdbc:sqlite:" + dbPath);
        } catch (InvalidPathException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        ////////////////////////////////////////////
        return new HikariDataSource(config);
    }
}