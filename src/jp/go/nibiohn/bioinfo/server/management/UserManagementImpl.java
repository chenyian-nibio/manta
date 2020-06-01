package jp.go.nibiohn.bioinfo.server.management;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.mindrot.jbcrypt.BCrypt;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jp.go.nibiohn.bioinfo.client.management.UserManagement;
import jp.go.nibiohn.bioinfo.server.clustering.Dendrogram;
import jp.go.nibiohn.bioinfo.server.clustering.HierarchicalClustering;
import jp.go.nibiohn.bioinfo.server.clustering.HierarchicalClustering.LinkageType;
import jp.go.nibiohn.bioinfo.shared.DendrogramCache;
import jp.go.nibiohn.bioinfo.shared.GutFloraAnalysisData;
import jp.go.nibiohn.bioinfo.shared.GutFloraConfig;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.PairListData;
import jp.go.nibiohn.bioinfo.shared.ParameterEntry;
import jp.go.nibiohn.bioinfo.shared.PcoaResult;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.SearchResultData;
import jp.go.nibiohn.bioinfo.shared.TaxonEntry;
import jp.go.nibiohn.bioinfo.shared.VisualizationtResult;

import smile.mds.MDSTweak;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class UserManagementImpl extends RemoteServiceServlet implements UserManagement {

  private HikariConfig config;
  private static String CURRENT_USER = "currentUser";

  private HikariDataSource getHikariDataSource() {
    if (config == null) {
      Properties props = new Properties();
      try {
        Class.forName("org.postgresql.Driver");
        props.load(UserManagementImpl.class.getClassLoader()
            .getResourceAsStream(GutFloraConfig.PGSQL_PROP_FILE));
      } catch (IOException e) {
        throw new RuntimeException(
            "Problem loading properties '" + GutFloraConfig.PGSQL_PROP_FILE + "'", e);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      config = new HikariConfig(props);
    }
    return new HikariDataSource(config);
  }

  public String getLoginUserRole() {
    String currentUser = getUserForQuery();

    HikariDataSource ds = getHikariDataSource();
    Connection connection = null;
    try {
      connection = ds.getConnection();

      Statement statement = connection.createStatement();
      String sqlQuery = " SELECT ur.user_role " + " FROM user_role as ur "
          + " join dbuser as du on du.role_id = ur.id "
          + " where du.username = '" + currentUser + "'";

      ResultSet results = statement.executeQuery(sqlQuery);
      String userRole = (results.next()) ? results.getString("user_role") : "guest";

      connection.close();
      ds.close();
      return userRole;

    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    ds.close();

    return null;

  }

  private String getUserForQuery() {
    String currentUser = getCurrentUserFromSession();
    if (currentUser == null) {
      // TODO or use "guest"?
      currentUser = "demo";
    }
    return currentUser;
  }

  private String getCurrentUserFromSession() {
    return (String) this.getThreadLocalRequest().getSession().getAttribute(CURRENT_USER);
  }

}