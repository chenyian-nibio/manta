package jp.go.nibiohn.bioinfo.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
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

import com.zaxxer.hikari.HikariDataSource;

import jp.go.nibiohn.bioinfo.server.DataSourceLoader;
import jp.go.nibiohn.bioinfo.server.distance.BrayCurtis;
import jp.go.nibiohn.bioinfo.server.distance.Jaccard;
import jp.go.nibiohn.bioinfo.shared.GutFloraConfig;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;

public class UploadProcessService {
	
	private static Set<String> IGNORED_TAXON_NAMES = new HashSet<String>();

	{
		// silva
		IGNORED_TAXON_NAMES.add("Unassigned");
		IGNORED_TAXON_NAMES.add("Other");
		IGNORED_TAXON_NAMES.add("Ambiguous_taxa");
		// greengenes
		IGNORED_TAXON_NAMES.add("k__");
		IGNORED_TAXON_NAMES.add("p__");
		IGNORED_TAXON_NAMES.add("c__");
		IGNORED_TAXON_NAMES.add("o__");
		IGNORED_TAXON_NAMES.add("f__");
		IGNORED_TAXON_NAMES.add("g__");
		IGNORED_TAXON_NAMES.add("s__");
		IGNORED_TAXON_NAMES.add("__");
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
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
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

			// Should I prevent the user reload the parameter data?
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
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		// first line is the headers, from the 2nd column is the sample ids
		String headerLine = reader.readLine();
		String[] headers = headerLine.split("\t");
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			// query for the existing sample
			String getSampleSql = " SELECT id FROM sample ";
			Statement statSample = connection.createStatement();
			ResultSet resSample = statSample.executeQuery(getSampleSql);
			Set<String> sampleIdSet = new HashSet<String>();
			while (resSample.next()) {
				String sampleId = resSample.getString("id");
				sampleIdSet.add(sampleId);
			}
			
			// Should I prevent the user reload the microbiota data?
			// query for the existing sample with microbiota
			String getMbSampleSql = " SELECT distinct sample_id FROM microbiota ";
			Statement statMbSample = connection.createStatement();
			ResultSet resMbSample = statMbSample.executeQuery(getMbSampleSql);
			Set<String> mbSampleIdSet = new HashSet<String>();
			while (resMbSample.next()) {
				String sampleId = resMbSample.getString("sample_id");
				mbSampleIdSet.add(sampleId);
			}
			
			Map<String, Map<String, Double>> summaryMap = new HashMap<String, Map<String, Double>>();
			String line = reader.readLine();
			while (line != null) {
				
				String[] cols = line.split("\t");
				
				String taxonString = cols[0];
				for (int k = 1; k < cols.length; k++) {
					Double count = Double.valueOf(cols[k]);
					if (count.doubleValue() != 0) {
						if (summaryMap.get(headers[k]) == null) {
							summaryMap.put(headers[k], new HashMap<String, Double>());
						}
						if (summaryMap.get(headers[k]).get(taxonString) != null) {
							count += summaryMap.get(headers[k]).get(taxonString);
						}
						summaryMap.get(headers[k]).put(taxonString, count);
					}
				}
				line = reader.readLine();
			}

			PreparedStatement psSample = connection.prepareStatement(" INSERT INTO sample (id, create_date) VALUES (?, ?) ");
			Date date = Date.valueOf(LocalDate.now());
			// insert to the taxonomy table
			PreparedStatement psTaxonomy = connection.prepareStatement(" INSERT INTO taxonomy (id, rank_id, name) VALUES (?, ?, ?) ");

			// insert into the microbiota table
			PreparedStatement psMicrobiota = connection.prepareStatement(
					" INSERT INTO microbiota "
					+ " (sample_id, kingdom_id, phylum_id, class_id, order_id, family_id, genus_id, species_id, read_num, read_pct, taxonkey) "
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			
			// query to get the saved taxonomy
			Set<String> savedTaxonomy = new HashSet<String>();
			String getTaxonomySql = " SELECT id FROM taxonomy ";
			Statement statTaxonomy = connection.createStatement();
			ResultSet resTaxonomy = statTaxonomy.executeQuery(getTaxonomySql);
			while (resTaxonomy.next()) {
				String tid = resTaxonomy.getString("id");
				savedTaxonomy.add(tid);
			}

			// for dominant taxonomy
			Map<String,List<MbDataHolder>> mdMap = new HashMap<String, List<MbDataHolder>>();
			// for alpha diversity
			Map<String, Map<String, Integer>> sampleReadsMap = new HashMap<String, Map<String,Integer>>();
			for (int i = 1; i < headers.length; i++) {
				String sampleId = headers[i];
				if (!sampleIdSet.contains(sampleId)) {
					psSample.setString(1, sampleId);
					psSample.setDate(2, date);
					psSample.executeUpdate();
				} else if (mbSampleIdSet.contains(sampleId)) {
					// ignore? or remove all associated data of this sample
//					System.out.println("Micorbiota already exists in the database, skip this sample: " + sampleId);
					continue;
				}

				sampleReadsMap.put(sampleId, new HashMap<String, Integer>());
				for (String taxonString : summaryMap.get(sampleId).keySet()) {
					MbDataHolder mdh = new MbDataHolder();
					List<String> taxonList = Arrays.asList(taxonString.split(";"));
					String[] taxonomy = new String[7];
					for (int j = 0; j < taxonList.size(); j++) {
						// ignore uncertain taxon names
						if (IGNORED_TAXON_NAMES.contains(taxonList.get(j))) {
							continue;
						}
						
						String tid = String.valueOf(StringUtils.join(taxonList.subList(0, j + 1), ";").hashCode());
						if (!savedTaxonomy.contains(tid)) {
							psTaxonomy.setString(1, tid);
							psTaxonomy.setInt(2, j + 1);
							psTaxonomy.setString(3, taxonList.get(j));
							psTaxonomy.executeUpdate();
							
							savedTaxonomy.add(tid);
						}
						taxonomy[j] = tid;
					}
					psMicrobiota.setString(1, sampleId);
					for (int t = 0; t < 7; t++) {
						psMicrobiota.setString(t + 2, taxonomy[t]);
						mdh.taxonomy[t] = taxonomy[t];
					}
					double pct = summaryMap.get(sampleId).get(taxonString) * 100d;
					int count = (int) (summaryMap.get(sampleId).get(taxonString) * 10000 + 0.5);
					psMicrobiota.setInt(9, count);
					psMicrobiota.setDouble(10, pct);
					
					String taxonKey = md5Text(taxonString);
					psMicrobiota.setString(11, taxonKey);
					
					psMicrobiota.executeUpdate();
					
					mdh.pct = pct;
					
					if (mdMap.get(sampleId) == null) {
						mdMap.put(sampleId, new ArrayList<MbDataHolder>());
					}
					mdMap.get(sampleId).add(mdh);
					sampleReadsMap.get(sampleId).put(taxonKey, count);
				}
			}
			
			// calculate the distance; Bray-Curtis dissimilarity & Jaccard distance
			// To calculate all against all, truncate all distance and recalculate (for convenience)
			if (DataSourceLoader.backend == "sqlite"){
				Statement statTruncate = connection.createStatement();
				String sqlTruncate = " DELETE FROM sample_distance ; "; 
				statTruncate.executeUpdate(sqlTruncate);	
				statTruncate.execute("VACUUM");	
			} else {
				Statement statTruncate = connection.createStatement();
				String sqlTruncate = " TRUNCATE TABLE sample_distance "; 
				statTruncate.executeUpdate(sqlTruncate);	
			}
			
			Map<String, Map<String, Integer>> matrix = new HashMap<String, Map<String,Integer>>();
			Statement statMicrobiota = connection.createStatement();
			String getMicrobiotaSql = " SELECT sample_id, taxonkey, read_num FROM microbiota "; 
			
			ResultSet mbResults = statMicrobiota.executeQuery(getMicrobiotaSql);
			while (mbResults.next()) {
				String sid1 = mbResults.getString("sample_id");
				String sid2 = mbResults.getString("taxonkey");
				int dist = mbResults.getInt("read_num");
				if (matrix.get(sid1) == null) {
					matrix.put(sid1, new HashMap<String, Integer>());
				}
				matrix.get(sid1).put(sid2, Integer.valueOf(dist));
			}
			
			PreparedStatement psSampleDistance = connection.prepareStatement(
					" INSERT INTO sample_distance (sample_id_1, sample_id_2, distance, distance_type_id) VALUES (?, ?, ?, ?) ");
			
			List<String> sampleList = new ArrayList<String>(matrix.keySet());
			for (int i = 0; i < sampleList.size(); i++) {
				String sid1 = sampleList.get(i);
				for (int j = 0; j < sampleList.size(); j++) {
					String sid2 = sampleList.get(j);
					if (sid2.equals(sid1)) {
						continue;
					}
					double bcDist = BrayCurtis.distance(matrix.get(sid1), matrix.get(sid2));
					psSampleDistance.setString(1, sid1);
					psSampleDistance.setString(2, sid2);
					psSampleDistance.setDouble(3, bcDist);
					psSampleDistance.setInt(4, GutFloraConstant.SAMPLE_DISTANCE_BRAY_CURTIS);
					psSampleDistance.executeUpdate();
					
					double jcDist = Jaccard.distance(matrix.get(sid1), matrix.get(sid2));
					psSampleDistance.setString(1, sid1);
					psSampleDistance.setString(2, sid2);
					psSampleDistance.setDouble(3, jcDist);
					psSampleDistance.setInt(4, GutFloraConstant.SAMPLE_DISTANCE_JACCARD);
					psSampleDistance.executeUpdate();
				}
			}
			
			// calculate dominant taxonomy
			PreparedStatement psDominantTaxon = connection.prepareStatement(
					" INSERT INTO dominant_taxon (sample_id, rank_id, taxon_id) VALUES (?, ?, ?) ");
			for (String sid: mdMap.keySet()) {
				List<MbDataHolder> list = mdMap.get(sid);
				Collections.sort(list, new Comparator<MbDataHolder>() {

					@Override
					public int compare(MbDataHolder o1, MbDataHolder o2) {
						return Double.valueOf(o2.pct).compareTo(Double.valueOf(o1.pct));
					}
				});
				double sum = 0d;
				Map<Integer,Set<String>> rankMap = new HashMap<Integer, Set<String>>();
				for (MbDataHolder mdh : list) {
					for (int i = 1; i < 8; i++) {
						if (rankMap.get(Integer.valueOf(i)) == null) {
							rankMap.put(Integer.valueOf(i), new HashSet<String>());
						}
						rankMap.get(Integer.valueOf(i)).add(mdh.taxonomy[i - 1]);
					}
					sum += Double.valueOf(mdh.pct).doubleValue();
					if (sum > 90d) {
						break;
					}
				}
				
				for (int i = 1; i < 8; i++) {
					for (String taxonId: rankMap.get(Integer.valueOf(i))) {
						if (taxonId != null) {
							psDominantTaxon.setString(1, sid);
							psDominantTaxon.setInt(2, i);
							psDominantTaxon.setString(3, taxonId);
							psDominantTaxon.executeUpdate();
						}
					}
				}
				
			}
			
			// calculate alpha diversity
			PreparedStatement psSampleDiversity = connection.prepareStatement(
					" INSERT INTO sample_diversity (sample_id, shannon, simpson) VALUES (?, ?, ?) ");
			for (String sid : sampleReadsMap.keySet()) {
				Map<String, Integer> map = sampleReadsMap.get(sid);
				double simpson = 0d;
				double shannon = 0d;
				for (Integer value : map.values()) {
					double p = value / 10000d;
					if (p == 0)
						continue;
					simpson += p * p;
					shannon += p * Math.log(p);
				}
				psSampleDiversity.setString(1, sid);
				psSampleDiversity.setDouble(2, -shannon);
				psSampleDiversity.setDouble(3, 1 - simpson);
				psSampleDiversity.executeUpdate();
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

	private static String md5Text(String target) {
		StringBuffer sb = new StringBuffer();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(target.getBytes());
			byte[] digest = md.digest();
			for (int i = 0; i < digest.length; i++) {
				sb.append(String.format("%02x", digest[i]));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	private class MbDataHolder {
		String[] taxonomy = new String[7];
		double pct;
	}

}
