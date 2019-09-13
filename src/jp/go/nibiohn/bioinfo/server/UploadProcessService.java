package jp.go.nibiohn.bioinfo.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jp.go.nibiohn.bioinfo.shared.GutFloraConfig;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;

public class UploadProcessService {

	private HikariConfig config;
	
	private HikariDataSource getHikariDataSource() {
		if (config == null) {
			Properties props = new Properties();
			try {
				Class.forName("org.postgresql.Driver");
				props.load(GutFloraServiceImpl.class.getClassLoader().getResourceAsStream(GutFloraConfig.PGSQL_PROP_FILE));
			} catch (IOException e) {
				throw new RuntimeException("Problem loading properties '" + GutFloraConfig.PGSQL_PROP_FILE + "'", e);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			config = new HikariConfig(props);
		}
		return new HikariDataSource(config);
	}
	
	public boolean processAndSaveUploadData(String type, InputStream inputStream) throws IOException {
		
		if (type.equals(GutFloraConstant.UPLOAD_DATA_TYPE_PARAMETERS)) {
			return processAndSaveUploadParameterData(inputStream);
		} else if (type.equals(GutFloraConstant.UPLOAD_DATA_TYPE_MICROBIOTA)) {
			return processAndSaveUploadMicrobiotaData(inputStream);
		} else {
			// unexpected type
		}
		return false;
	}

	private boolean processAndSaveUploadParameterData(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		// first line is the headers
		String headerLine = reader.readLine();
		String[] headers = headerLine.split("\t");
		
		HikariDataSource ds = getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			// query for the existing parameters
			String getParaInfoSql = " SELECT id FROM parameter_info ";
			Statement statement = connection.createStatement();
			ResultSet results = statement.executeQuery(getParaInfoSql);
			Set<String> paraIdSet = new HashSet<String>();
			while (results.next()) {
				String paraId = results.getString("id");
				paraIdSet.add(paraId);
			}

			// store the parameters into parameter_info if it is not exist yet
			PreparedStatement prepareStatement = connection.prepareStatement(" INSERT INTO parameter_info (id, title, type_id, visible) VALUES (?, ?, 4, TRUE) ");
			for (int i = 1; i < headers.length; i++) {
				String para = headers[i];
				if (!paraIdSet.contains(para)) {
//					String addParaInfoSql = String.format(
//							" INSERT INTO parameter_info (id, title, type_id, visible) VALUES (%s, %s, %d, %s) ", para,
//							para, 4, "TRUE"); 
					prepareStatement.setString(1, para);
					prepareStatement.setString(2, para);
					prepareStatement.executeUpdate();
				}
			}

			// query for the existing sample
			String getSampleSql = " SELECT id FROM sample ";
			Statement statSample = connection.createStatement();
			ResultSet resSample = statSample.executeQuery(getSampleSql);
			Set<String> sampleIdSet = new HashSet<String>();
			while (resSample.next()) {
				String sampleId = resSample.getString("id");
				sampleIdSet.add(sampleId);
			}

			PreparedStatement psParaValue = connection.prepareStatement(" INSERT INTO parameter_value (sample_id, parameter_id, parameter_value) VALUES (?, ?, ?) ");
			PreparedStatement psSample = connection.prepareStatement(" INSERT INTO sample (id, create_date) VALUES (?, ?) ");
			Date date = Date.valueOf(LocalDate.now());
			String line = reader.readLine();
			while (line != null) {
				String[] cols = line.split("\t");
				String sampleId = cols[0];
				if (!sampleIdSet.contains(sampleId)) {
					psSample.setString(1, sampleId);
					psSample.setDate(2, date);
					psSample.executeUpdate();
				}
				
				for (int i = 1; i < cols.length; i++) {
					psParaValue.setString(1, sampleId);
					psParaValue.setString(2, headers[i]);
					psParaValue.setString(3, cols[i]);
					psParaValue.executeUpdate();
				}
				line = reader.readLine();
			}

			connection.close();
			ds.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		ds.close();

		return true;
	}

	private boolean processAndSaveUploadMicrobiotaData(InputStream inputStream) throws IOException {
		
		return false;
	}
}
