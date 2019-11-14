package jp.go.nibiohn.bioinfo.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import com.zaxxer.hikari.HikariDataSource;

import jp.go.nibiohn.bioinfo.client.GutFloraService;
import jp.go.nibiohn.bioinfo.server.DataSourceLoader;
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
public class GutFloraServiceImpl extends RemoteServiceServlet implements GutFloraService {

	// TODO to be improved; not a good idea to hard code here... 
	private LinkageType[] linkageTypes = new LinkageType[] { LinkageType.AVERAGE, LinkageType.COMPLETE,
			LinkageType.SINGLE };
	
	@Override
	public List<SampleEntry> getSampleEntryList(String lang) {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();

			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " SELECT distinct mb.sample_id " + " FROM microbiota as mb ";
			
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			Set<String> sampleSet = new HashSet<String>();
			while (results1.next()) {
				String sampleId = results1.getString("sample_id");
				sampleSet.add(sampleId);
			}

			Statement statement2 = connection.createStatement();
			String sqlQuery2 = " SELECT position, parameter_id FROM sample_display_columns ORDER BY position ";
			
			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			List<String> columnList = new ArrayList<String>();
			while (results2.next()) {
//				int index = results2.getInt("position");
				String value = results2.getString("parameter_id");
				columnList.add(value);
			}
			String[] displayColumns = columnList.toArray(new String[] {});
			
			Statement statement3 = connection.createStatement();
			String sqlQuery3 = "SELECT sample_id, parameter_id, parameter_value FROM parameter_value "
					+ "WHERE parameter_id IN ('" + StringUtils.join(columnList, "','") + "')";
			ResultSet results3 = statement3.executeQuery(sqlQuery3);
			Map<String, Map<String, String>> sampleDisplayMap = new HashMap<String, Map<String, String>>(); 
			while (results3.next()) {
				String sampleId = results3.getString("sample_id");
				if (sampleDisplayMap.get(sampleId) == null) {
					sampleDisplayMap.put(sampleId, new HashMap<String, String>());
				}
				sampleDisplayMap.get(sampleId).put(results3.getString("parameter_id"),
						results3.getString("parameter_value"));
			}

			Statement statement4 = connection.createStatement();
			String sqlQuery4 = " SELECT id, create_date FROM sample " + " ORDER BY id ";
			
			ResultSet results4 = statement4.executeQuery(sqlQuery4);
			List<SampleEntry> ret = new ArrayList<SampleEntry>();
			while (results4.next()) {
				String sampleId = results4.getString("id");
				String[] displayValues = new String[displayColumns.length];
				for (int i = 0; i < displayColumns.length; i++) {
					if (displayColumns[i] != null) {
						displayValues[i] = sampleDisplayMap.get(sampleId).get(displayColumns[i]);
					}
				}
				ret.add(new SampleEntry(sampleId, results4.getDate("create_date"), displayColumns, displayValues,
						sampleSet.contains(sampleId)));

			}
			
			connection.close();
			ds.close();
			return ret;
			
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

	@Override
	public List<List<String>> getSampleProfile(String sampleId, String lang) {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			
			String sqlQuery = " select title, parameter_value "  
					+ " from parameter_value as pv "
					+ " join parameter_info as pi on pi.id = pv.parameter_id "  
					+ " where sample_id = '" + sampleId + "'  "
					+ " order by pi.sysid ";
			
			ResultSet results = statement.executeQuery(sqlQuery);
			List<List<String>> ret = new ArrayList<List<String>>();
			while (results.next()) {
				String parameter;
				String valueString = results.getString("parameter_value");
				// TODO
				String unit = null;
				if (valueString == null) {
					parameter = "-";
				} else {
					// trim the decimal
					if (valueString.contains(".") && valueString.indexOf('.') + 3 < valueString.length()) {
						parameter = valueString.substring(0, valueString.indexOf('.') + 3);
					} else {
						parameter = valueString;
					}
				}
				ret.add(Arrays.asList(results.getString("title"), parameter, unit));
			}
			connection.close();
			ds.close();
			return ret;
			
		} catch (SQLException e) {
			e.printStackTrace();
			ds.close();
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
	
	/**
	 * return a list of (taxon_name, reads, taxon_id) list
	 */
	@Override
	public List<List<String>> getMicrobiota(String sampleId, String rank) {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			String sqlQuery = " select t.name as taxon_name, t.id as taxon_id, sum(read_num) as sum_reads "
					+ " from microbiota join taxonomy as t on t.id=" + rank + "_id "
					+ " where sample_id = '" + sampleId + "'" + "and " + rank
					+ "_id is not null group by t.name, t.id order by sum(read_num) desc ";
			
			ResultSet results = statement.executeQuery(sqlQuery);
			List<List<String>> ret = new ArrayList<List<String>>();
			while (results.next()) {
				List<String> asList = Arrays.asList(results.getString("taxon_name"), results.getString("sum_reads"),
						results.getString("taxon_id"));
				ret.add(asList);
			}
			connection.close();
			ds.close();
			return ret;
			
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

	@Override
	public List<List<String>> getSampleReads(String sampleId, String rank, String taxonId) {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			
			String childRank = GutFloraConstant.RANK_LIST.get(GutFloraConstant.RANK_MAP.get(rank).intValue());
			
			String sqlQuery = " select t.name as taxon_name, t.id as taxon_id, sum(read_num) as sum_reads "
					+ " from microbiota join taxonomy as t on t.id=" + childRank + "_id "
					+ " where sample_id = '" + sampleId + "'" 
					+ " and " + rank + "_id = '" + taxonId + "' " 
					+ " and " + childRank + "_id is not null group by t.name, t.id order by sum(read_num) desc ";
			
			ResultSet results = statement.executeQuery(sqlQuery);
			List<List<String>> ret = new ArrayList<List<String>>();
			while (results.next()) {
				List<String> asList = Arrays.asList(results.getString("taxon_name"), results.getString("sum_reads"),
						results.getString("taxon_id"));
				ret.add(asList);
			}
			connection.close();
			ds.close();
			
			return ret;
			
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
	
	@Override
	public Set<SampleEntry> getSelectedSampleEntrySet(String sampleIdString, String lang) {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			// processing the string...
			String[] ids = sampleIdString.split(",");
			
			// TODO should consider the displayed column
			Statement statement = connection.createStatement();
			String sqlQuery = " select id, create_date from sample where id in ('"
					+ StringUtils.join(ids, "','") + "')";
			
			ResultSet results = statement.executeQuery(sqlQuery);
			Set<SampleEntry> ret = new HashSet<SampleEntry>();
			while (results.next()) {
				String sampleId = results.getString("id");
				// TODO
				ret.add(new SampleEntry(sampleId, results.getDate("create_date"), null, null, true));
			}
			
			connection.close();
			ds.close();
			return ret;
			
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
	
	@Override
	public SampleEntry getSampleEntry(String sampleId, String lang) {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			String sqlQuery = " select s.id, create_date from sample as s where s.id ='" + sampleId + "' ";

			ResultSet results = statement.executeQuery(sqlQuery);
			SampleEntry ret = null;
			while (results.next()) {
				// TODO
				ret = new SampleEntry(results.getString("id"), results.getDate("create_date"), null, null, null);
			}
			
			connection.close();
			ds.close();
			return ret;
			
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

	private GutFloraAnalysisData getKingdomReadsAnalysisData(Set<SampleEntry> selectedSamples) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, t.name as taxon_name, " + "sum(read_num)" + " as all_reads "
					+ " from microbiota join taxonomy as t on t.id = kingdom_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " group by sample_id, t.name ";
			
			Map<String, Map<String, String>> rows = new HashMap<String, Map<String,String>>();
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				String name = results1.getString("taxon_name");
				double allReads = results1.getDouble("all_reads");
				if (rows.get(sid) == null) {
					rows.put(sid, new HashMap<String, String>());
				}
				String displayValue = String.valueOf((int) allReads);
				rows.get(sid).put(name, displayValue);
			}
			
			connection.close();
			ds.close();
			
			// temporary use a fix order for kingdom categories
			List<String> rhList = Arrays.asList("Bacteria", "Viruses", "Archaea", "Unclassified");
			
			GutFloraAnalysisData ret = new GutFloraAnalysisData(rows, sampleIdList);
			ret.setReadsData(rhList, rhList);
			return ret;
			
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
	
	private List<String> getSortedSampleList(Set<SampleEntry> selectedSamples) {
		List<String> sampleIdList = new ArrayList<String>();
		for (SampleEntry se : selectedSamples) {
			sampleIdList.add(se.getSampleId());
		}
		Collections.sort(sampleIdList);
		return sampleIdList;
	}

	@Override
	public GutFloraAnalysisData getReadsAnalysisData(Set<SampleEntry> selectedSamples, String rank) {
		return getReadsAnalysisData(selectedSamples, rank, GutFloraConstant.DEFAULT_NUM_OF_COLUMNS);
	}

	/**
	 * (unused)
	 */
	public GutFloraAnalysisData getReadsAnalysisData(Set<SampleEntry> selectedSamples, String rank, int numOfColumns) {
		// TODO
		if (rank.equals("")) {
			return getKingdomReadsAnalysisData(selectedSamples);
		} else {
			List<String> sampleIdList = getSortedSampleList(selectedSamples);
			
			HikariDataSource ds = DataSourceLoader.getHikariDataSource();
			Connection connection = null;
			try {
				connection = ds.getConnection();
				
				String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
				
				Statement statement0 = connection.createStatement();
				String sqlQuery0 = " select " + rank + "_id as taxon_id, t.name as taxon_name from microbiota "
						+ " join taxonomy as t on t.id = " + rank + "_id "
						+ " where sample_id in (" + sampleIdString + ") "
						+ " and " + rank + "_id is not null group by " + rank 
						+ "_id, t.name order by sum(read_num) desc ";
				
				ResultSet results0 = statement0.executeQuery(sqlQuery0);
				List<String> rankNameList = new ArrayList<String>();
				List<String> topNRankIdList = new ArrayList<String>();
				while (results0.next()) {
					String rid = results0.getString("taxon_id");
					String name = results0.getString("taxon_name");
					rankNameList.add(name);
					if (topNRankIdList.size() < numOfColumns) {
						topNRankIdList.add(rid);
					}
				}

				String rankIdString = "'" + StringUtils.join(topNRankIdList, "','") + "'";
				
				Statement statement1 = connection.createStatement();
				String sqlQuery1 = " select sample_id, t.id as taxon_id, t.name as taxon_name, " + "sum(read_num)" + " as all_reads "
						+ " from microbiota join taxonomy as t on t.id = " + rank + "_id "
						+ " where sample_id in (" + sampleIdString + ") "
						+ " and t.id in (" + rankIdString + ") "
						+ " group by sample_id, t.id, t.name ";
				
				Map<String, Map<String, String>> rows = new HashMap<String, Map<String,String>>();
				ResultSet results1 = statement1.executeQuery(sqlQuery1);
				Map<String, String> taxonomyMap = new HashMap<String, String>();
				while (results1.next()) {
					String sid = results1.getString("sample_id");
					String tid = results1.getString("taxon_id");
					String name = results1.getString("taxon_name");
					taxonomyMap.put(tid, name);
					double allReads = results1.getDouble("all_reads");
					if (rows.get(sid) == null) {
						rows.put(sid, new HashMap<String, String>());
					}
					String displayValue = String.valueOf((int) allReads);
					rows.get(sid).put(name, displayValue);
				}

				// add others column
				// TODO theoretically, total count should be 10,000. thus, no need to make a query? 
				Statement statement2 = connection.createStatement();
				String sqlQuery2 = " select sample_id, " + "sum(read_num)" + " as all_reads from microbiota "
						+ " where sample_id in (" + sampleIdString + ") "
						+ " group by sample_id ";
				
				ResultSet results2 = statement2.executeQuery(sqlQuery2);
				Map<String,Double> allReadsMap = new HashMap<String, Double>();
				while (results2.next()) {
					String sid = results2.getString("sample_id");
					double allReads = results2.getDouble("all_reads");
					allReadsMap.put(sid, Double.valueOf(allReads));
					Map<String, String> map = rows.get(sid);
					double sum = 0;
					for (String name: rankNameList) {
						String value = map.get(name);
						if (value != null) {
							sum += Double.valueOf(value);
						}
					}
					String displayValue = String.valueOf((int) (allReads - sum));
					rows.get(sid).put(GutFloraConstant.COLUMN_HEADER_OTHERS, displayValue);
				}

				connection.close();
				ds.close();
				
				List<String> rhList = new ArrayList<String>();
				for (String tid: topNRankIdList) {
					rhList.add(taxonomyMap.get(tid));
				}
				
				GutFloraAnalysisData ret = new GutFloraAnalysisData(rows, sampleIdList);
				ret.setReadsData(rhList, rankNameList);
				
				return ret;
				
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
			
		}

		return null;
	}

	@Override
	public VisualizationtResult getReadsBarChart(Set<SampleEntry> selectedSamples, String rank) {
		// suppose the minimal display number is 10
		int minimalDisplayNumber = 10;
		
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		String q0Rank = rank;
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";

			// retrieve taxonomy from dominant_taxon table
			Statement statementA = connection.createStatement();
			String sqlQueryA = " select distinct tx.id as taxon_id, tx.name as taxon_name"
					+ " from dominant_taxon as dt " 
					+ " join taxon_rank as tr on tr.id = dt.rank_id " 
					+ " join taxonomy as tx on tx.id = dt.taxon_id " 
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and tr.name = '" + q0Rank + "' ";
			
			List<String> taxonList = new ArrayList<String>();
			ResultSet resultsA = statementA.executeQuery(sqlQueryA);
			while (resultsA.next()) {
				String tid = resultsA.getString("taxon_id");
				String name = resultsA.getString("taxon_name");
				taxonList.add(tid + "::" + name);
			}

			Statement statement0 = connection.createStatement();
			String sqlQuery0 = " select " + rank + "_id as taxon_id, t.name as taxon_name from microbiota "
					+ " join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and " + rank + "_id is not null group by " + rank 
					+ "_id, t.name order by sum(read_num) desc ";
			
			ResultSet results0 = statement0.executeQuery(sqlQuery0);
			final Map<String, Integer> taxonOrderMap = new HashMap<String, Integer>();
			int order = 1;
			while (results0.next()) {
				String tid = results0.getString("taxon_id");
				String name = results0.getString("taxon_name");
				String taxonString = tid + "::" + name;
				taxonOrderMap.put(taxonString, Integer.valueOf(order));
				order++;
				// though list is inefficient doing contains, but the size is less than 10, probably fine 
				if (taxonList.size() < minimalDisplayNumber && !taxonList.contains(taxonString)) {
					taxonList.add(taxonString);
				}
			}
			
			Collections.sort(taxonList, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return taxonOrderMap.get(o1).compareTo(taxonOrderMap.get(o2));
				}
			});
			
			
			List<String> selectedcolumns = new ArrayList<String>();
			List<String> rankIdList = new ArrayList<String>();
			for (String taxonString: taxonList) {
				String[] split = taxonString.split("::");
				rankIdList.add(split[0]);
				selectedcolumns.add(split[1]);
			}

			String rankIdString = "'" + StringUtils.join(rankIdList, "','") + "'";
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, t.id as taxon_id, t.name as taxon_name, sum(read_num) as all_reads "
					+ " from microbiota join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and t.id in (" + rankIdString + ") "
					+ " group by sample_id, t.id, t.name ";
			
			Map<String, Map<String, String>> rows = new HashMap<String, Map<String,String>>();
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			Map<String, String> taxonomyMap = new HashMap<String, String>();
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				String tid = results1.getString("taxon_id");
				String name = results1.getString("taxon_name");
				taxonomyMap.put(tid, name);
				int allReads = results1.getInt("all_reads");
				if (rows.get(sid) == null) {
					rows.put(sid, new HashMap<String, String>());
				}
				rows.get(sid).put(name, String.valueOf(allReads));
			}
			
			Statement statement2 = connection.createStatement();
			String sqlQuery2 = " select sample_id, sum(read_num) as all_reads from microbiota "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " group by sample_id ";
			
			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			Map<String,Double> allReadsMap = new HashMap<String, Double>();
			while (results2.next()) {
				String sid = results2.getString("sample_id");
				int allReads = results2.getInt("all_reads");
				allReadsMap.put(sid, Double.valueOf(allReads));
				Map<String, String> map = rows.get(sid);
				int sum = 0;
				if (map == null) {
					rows.put(sid, new HashMap<String, String>());
				} else {
					for (String name: map.keySet()) {
						String value = map.get(name);
						if (value != null) {
							sum += Integer.valueOf(value);
						}
					}
				}
				rows.get(sid).put(GutFloraConstant.COLUMN_HEADER_OTHERS, String.valueOf(allReads - sum));
			}
			
			connection.close();
			ds.close();
			
			// add bar chart
			// shiftX depend on the sample id length
			int maxSampleLength = getMaxItemLength(sampleIdList);
			
			String svgBarChart = createSvgBarChart(rows, sampleIdList.toArray(new String[] {}), selectedcolumns,
					rankIdList, allReadsMap, 160 + maxSampleLength * 10);
			
			int maxItemLength = getMaxItemLength(selectedcolumns);
			int charWidth = 7;
			
			StringBuffer ret = new StringBuffer();
			
			int canvasHeight = sampleIdList.size() * Dendrogram.LINE_HEIGHT * 2 + 80;
			int canvasWidth = 830 + 40 + maxItemLength * charWidth;
			ret.append(String.format("<svg height=\"%d\" width=\"%d\">\n", canvasHeight, canvasWidth));
			ret.append(String.format("\t<rect x=\"1\" y=\"1\" fill=\"white\" id=\"chart_body\" height=\"%d\" width=\"%d\" />\n", canvasHeight - 2, canvasWidth - 2));
			
			ret.append(svgBarChart);
			
			// add sampleID
			int fontSize = 16;
			ret.append(String.format("<g font-family=\"Arial\" font-size=\"%d\" fill=\"black\">\n", fontSize));
			for (int i = 0; i < sampleIdList.size(); i++) {
				ret.append(String.format("\t<text x=\"%d\" y=\"%d\">%s</text>\n", 140,
						(int) (i * Dendrogram.LINE_HEIGHT * 2) + 20, sampleIdList.get(i)));
			}
			ret.append("</g>\n");

			
			ret.append("</svg>\n");
			
			return new VisualizationtResult(ret.toString());
			
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
	
	@Override
	public VisualizationtResult getReadsBarChart(Set<SampleEntry> selectedSamples, String selectedRank, String parentRank,
			String parentTaxonId) {
//		System.out.println(String.format("%s, %s, %s", selectedRank, parentRank, parentTaxonId));
		
		// suppose the maximal display number is 15
		int maximalDisplayNumber = 15;

		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			// limited the query from read to the previous rank & taxonId
			Statement statement0 = connection.createStatement();
			String sqlQuery0 = " select " + selectedRank + "_id as taxon_id, t.name as taxon_name "
					+ " from microbiota "
					+ " join taxonomy as t on t.id = " + selectedRank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and " + selectedRank + "_id is not null "
					+ " and " + parentRank + "_id ='" + parentTaxonId + "' "
					+ " group by " + selectedRank + "_id, t.name order by sum(read_num) desc ";
			
			ResultSet results0 = statement0.executeQuery(sqlQuery0);
			List<String> rankNameList = new ArrayList<String>();
			List<String> topNRankIdList = new ArrayList<String>();
			while (results0.next()) {
				String rid = results0.getString("taxon_id");
				String name = results0.getString("taxon_name");
				rankNameList.add(name);
				if (topNRankIdList.size() < maximalDisplayNumber) {
					topNRankIdList.add(rid);
				}
			}
			
			String rankIdString = "'" + StringUtils.join(topNRankIdList, "','") + "'";
			
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, t.id as taxon_id, t.name as taxon_name, sum(read_num) as all_reads "
					+ " from microbiota join taxonomy as t on t.id = " + selectedRank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and t.id in (" + rankIdString + ") "
					+ " and " + parentRank + "_id ='" + parentTaxonId + "' "
					+ " group by sample_id, t.id, t.name ";
			
			Map<String, Map<String, String>> rows = new HashMap<String, Map<String,String>>();
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			Map<String, String> taxonomyMap = new HashMap<String, String>();
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				String tid = results1.getString("taxon_id");
				String name = results1.getString("taxon_name");
				taxonomyMap.put(tid, name);
				int allReads = results1.getInt("all_reads");
				if (rows.get(sid) == null) {
					rows.put(sid, new HashMap<String, String>());
				}
				rows.get(sid).put(name, String.valueOf(allReads));
			}
			
			// add others column 
			Statement statement2 = connection.createStatement();
			String sqlQuery2 = " select sample_id, sum(read_num) as all_reads from microbiota "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and " + parentRank + "_id ='" + parentTaxonId + "' "
					+ " group by sample_id ";

			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			Map<String,Double> allReadsMap = new HashMap<String, Double>();
			while (results2.next()) {
				String sid = results2.getString("sample_id");
				int allReads = results2.getInt("all_reads");
				allReadsMap.put(sid, Double.valueOf(allReads));
				Map<String, String> map = rows.get(sid);
				int sum = 0;
				if (map == null) {
					rows.put(sid, new HashMap<String, String>());
				} else {
					for (String name: rankNameList) {
						String value = map.get(name);
						if (value != null) {
							sum += Integer.valueOf(value);
						}
					}
				}
				rows.get(sid).put(GutFloraConstant.COLUMN_HEADER_OTHERS, String.valueOf(allReads - sum));
			}
			
			connection.close();
			ds.close();
			
			List<String> rhList = new ArrayList<String>();
			for (String tid: topNRankIdList) {
				rhList.add(taxonomyMap.get(tid));
			}
			
			// add bar chart
			// shiftX depend on the sample id length
			int maxSampleLength = getMaxItemLength(sampleIdList);
			
			String svgBarChart = createSvgBarChart(rows, sampleIdList.toArray(new String[] {}), rhList, topNRankIdList,
					allReadsMap, 160 + maxSampleLength * 10);
			
			int maxItemLength = getMaxItemLength(rhList);
			int charWidth = 7;
			
			StringBuffer ret = new StringBuffer();
			
			int canvasHeight = sampleIdList.size() * Dendrogram.LINE_HEIGHT * 2 + 80;
			int canvasWidth = 830 + 40 + maxItemLength * charWidth;
			ret.append(String.format("<svg height=\"%d\" width=\"%d\">\n", canvasHeight, canvasWidth));
			ret.append(String.format("\t<rect x=\"1\" y=\"1\" fill=\"white\" id=\"chart_body\" height=\"%d\" width=\"%d\" />\n", canvasHeight - 2, canvasWidth - 2));
			
			ret.append(svgBarChart);
			
			// add sampleID
			int fontSize = 16;
			ret.append(String.format("<g font-family=\"Arial\" font-size=\"%d\" fill=\"black\">\n", fontSize));
			for (int i = 0; i < sampleIdList.size(); i++) {
				ret.append(String.format("\t<text x=\"%d\" y=\"%d\">%s</text>\n", 140,
						(int) (i * Dendrogram.LINE_HEIGHT * 2) + 20, sampleIdList.get(i)));
			}
			ret.append("</g>\n");

			
			ret.append("</svg>\n");
			
			return new VisualizationtResult(ret.toString());
			
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
	
	@Override
	public VisualizationtResult getReadsClusteredBarChart(Set<SampleEntry> selectedSamples, String rank,
			int distanceType, int linkageType, Map<Integer, DendrogramCache> cacheMap) {
		// suppose the minimal display number is 10
		int minimalDisplayNumber = 10;
		
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		String q0Rank = rank;
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";

			// retrieve taxonomy from dominant_taxon table
			Statement statementA = connection.createStatement();
			String sqlQueryA = " select distinct tx.id as taxon_id, tx.name as taxon_name"
					+ " from dominant_taxon as dt " 
					+ " join taxon_rank as tr on tr.id = dt.rank_id " 
					+ " join taxonomy as tx on tx.id = dt.taxon_id " 
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and tr.name = '" + q0Rank + "' ";
			
			List<String> taxonList = new ArrayList<String>();
			ResultSet resultsA = statementA.executeQuery(sqlQueryA);
			while (resultsA.next()) {
				String tid = resultsA.getString("taxon_id");
				String name = resultsA.getString("taxon_name");
				taxonList.add(tid + "::" + name);
			}

			Statement statement0 = connection.createStatement();
			String sqlQuery0 = " select " + rank + "_id as taxon_id, t.name as taxon_name from microbiota "
					+ " join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and " + rank + "_id is not null group by " + rank 
					+ "_id, t.name order by sum(read_num) desc ";
			
			ResultSet results0 = statement0.executeQuery(sqlQuery0);
			final Map<String, Integer> taxonOrderMap = new HashMap<String, Integer>();
			int order = 1;
			while (results0.next()) {
				String tid = results0.getString("taxon_id");
				String name = results0.getString("taxon_name");
				String taxonString = tid + "::" + name;
				taxonOrderMap.put(taxonString, Integer.valueOf(order));
				order++;
				// though list is inefficient doing contains, but the size is less than 10, probably fine 
				if (taxonList.size() < minimalDisplayNumber && !taxonList.contains(taxonString)) {
					taxonList.add(taxonString);
				}
			}
			
			Collections.sort(taxonList, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return taxonOrderMap.get(o1).compareTo(taxonOrderMap.get(o2));
				}
			});
			
			
			List<String> selectedcolumns = new ArrayList<String>();
			List<String> rankIdList = new ArrayList<String>();
			for (String taxonString: taxonList) {
				String[] split = taxonString.split("::");
				rankIdList.add(split[0]);
				selectedcolumns.add(split[1]);
			}

			String rankIdString = "'" + StringUtils.join(rankIdList, "','") + "'";
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, t.id as taxon_id, t.name as taxon_name, sum(read_num) as all_reads "
					+ " from microbiota join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and t.id in (" + rankIdString + ") "
					+ " group by sample_id, t.id, t.name ";
			
			Map<String, Map<String, String>> rows = new HashMap<String, Map<String,String>>();
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			Map<String, String> taxonomyMap = new HashMap<String, String>();
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				String tid = results1.getString("taxon_id");
				String name = results1.getString("taxon_name");
				taxonomyMap.put(tid, name);
				int allReads = results1.getInt("all_reads");
				if (rows.get(sid) == null) {
					rows.put(sid, new HashMap<String, String>());
				}
				rows.get(sid).put(name, String.valueOf(allReads));
			}
			
			Statement statement2 = connection.createStatement();
			String sqlQuery2 = " select sample_id, sum(read_num) as all_reads from microbiota "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " group by sample_id ";
			
			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			Map<String,Double> allReadsMap = new HashMap<String, Double>();
			while (results2.next()) {
				String sid = results2.getString("sample_id");
				int allReads = results2.getInt("all_reads");
				allReadsMap.put(sid, Double.valueOf(allReads));
				Map<String, String> map = rows.get(sid);
				int sum = 0;
				if (map == null) {
					rows.put(sid, new HashMap<String, String>());
				} else {
					for (String name: map.keySet()) {
						String value = map.get(name);
						if (value != null) {
							sum += Integer.valueOf(value);
						}
					}
				}
				rows.get(sid).put(GutFloraConstant.COLUMN_HEADER_OTHERS, String.valueOf(allReads - sum));
			}
			
			connection.close();
			ds.close();
			
			List<String> sequence;
			String svgDendrogram;
			int dendrogramWidth;
			int dendrogramHeight;
			if (cacheMap == null) {
				cacheMap = new HashMap<Integer, DendrogramCache>();
			}
			DendrogramCache cache = cacheMap.get(Integer.valueOf(distanceType * 10 + linkageType));
			if (cache == null) {
				Dendrogram dendrogram = sampleDistanceClustering(sampleIdList, distanceType, linkageTypes[linkageType]);
				
				sequence = dendrogram.getSequence();
				svgDendrogram = dendrogram.getScaleUpSvgImageContentAtLeft(0, 6, 1, 2, 130d, true);
				dendrogramWidth = dendrogram.getDendrogramWidth(130d);
				dendrogramHeight = dendrogram.getDendrogramHeight(2);
				
				cacheMap.put(Integer.valueOf(distanceType * 10 + linkageType),
						new DendrogramCache(sequence, svgDendrogram, dendrogramWidth, dendrogramHeight));
			} else {
				sequence = cache.getSequence();
				svgDendrogram = cache.getSvgDendrogram();
				dendrogramWidth = cache.getDendrogramWidth();
				dendrogramHeight = cache.getDendrogramHeight();
			}
			
			// add bar chart
			// shiftX depend on the sample id length
			int maxSampleLength = getMaxItemLength(sequence);
			
			String svgBarChart = createSvgBarChart(rows, sequence.toArray(new String[] {}), selectedcolumns, rankIdList,
					allReadsMap, dendrogramWidth + maxSampleLength * 10 + 10);
			
			int maxItemLength = getMaxItemLength(selectedcolumns);
			int charWidth = 7;
			
			StringBuffer ret = new StringBuffer();
			
			int canvasHeight = dendrogramHeight + 60;
			int canvasWidth = dendrogramWidth + 60 + 600 + 60 + maxItemLength * charWidth;
			ret.append(String.format("<svg height=\"%d\" width=\"%d\">\n", canvasHeight, canvasWidth));
			ret.append(String.format("\t<rect x=\"1\" y=\"1\" fill=\"white\" id=\"chart_body\" height=\"%d\" width=\"%d\" />\n", canvasHeight - 2, canvasWidth - 2));
			
			ret.append(svgBarChart);
			ret.append(svgDendrogram);
			
			ret.append("</svg>\n");
			 
			VisualizationtResult visualizationtResult = new VisualizationtResult(ret.toString());
			visualizationtResult.setDendrogramMap(cacheMap);
			return visualizationtResult;
			
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
		
		return null;
	}

	@Override
	public VisualizationtResult getReadsClusteredBarChart(Set<SampleEntry> selectedSamples, String selectedRank,
			String parentRank, String parentTaxonId, int distanceType, int linkageType, int numOfColumns,
			Map<Integer, DendrogramCache> cacheMap) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			// limited the query from read to the previous rank & taxonId
			Statement statement0 = connection.createStatement();
			String sqlQuery0 = " select " + selectedRank + "_id as taxon_id, t.name as taxon_name "
					+ " from microbiota "
					+ " join taxonomy as t on t.id = " + selectedRank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and " + selectedRank + "_id is not null "
					+ " and " + parentRank + "_id ='" + parentTaxonId + "' "
					+ " group by " + selectedRank + "_id, t.name order by sum(read_num) desc ";
			
			ResultSet results0 = statement0.executeQuery(sqlQuery0);
			List<String> rankNameList = new ArrayList<String>();
			List<String> topNRankIdList = new ArrayList<String>();
			while (results0.next()) {
				String rid = results0.getString("taxon_id");
				String name = results0.getString("taxon_name");
				rankNameList.add(name);
				if (topNRankIdList.size() < numOfColumns) {
					topNRankIdList.add(rid);
				}
			}
			
			String rankIdString = "'" + StringUtils.join(topNRankIdList, "','") + "'";
			
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, t.id as taxon_id, t.name as taxon_name, sum(read_num) as all_reads "
					+ " from microbiota join taxonomy as t on t.id = " + selectedRank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and " + parentRank + "_id ='" + parentTaxonId + "' "
					+ " and t.id in (" + rankIdString + ") "
					+ " group by sample_id, t.id, t.name ";
			
			Map<String, Map<String, String>> rows = new HashMap<String, Map<String,String>>();
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			Map<String, String> taxonomyMap = new HashMap<String, String>();
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				String tid = results1.getString("taxon_id");
				String name = results1.getString("taxon_name");
				taxonomyMap.put(tid, name);
				int allReads = results1.getInt("all_reads");
				if (rows.get(sid) == null) {
					rows.put(sid, new HashMap<String, String>());
				}
				rows.get(sid).put(name, String.valueOf(allReads));
			}
			
			// add others column 
			Statement statement2 = connection.createStatement();
			String sqlQuery2 = " select sample_id, sum(read_num) as all_reads from microbiota "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and " + parentRank + "_id ='" + parentTaxonId + "' "
					+ " group by sample_id ";

			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			Map<String,Double> allReadsMap = new HashMap<String, Double>();
			while (results2.next()) {
				String sid = results2.getString("sample_id");
				int allReads = results2.getInt("all_reads");
				allReadsMap.put(sid, Double.valueOf(allReads));
				Map<String, String> map = rows.get(sid);
				int sum = 0;
				if (map == null) {
					rows.put(sid, new HashMap<String, String>());
				} else {
					for (String name: rankNameList) {
						String value = map.get(name);
						if (value != null) {
							sum += Integer.valueOf(value);
						}
					}
				}
				rows.get(sid).put(GutFloraConstant.COLUMN_HEADER_OTHERS, String.valueOf(allReads - sum));
			}
			
			connection.close();
			ds.close();
			
			List<String> rhList = new ArrayList<String>();
			for (String tid: topNRankIdList) {
				rhList.add(taxonomyMap.get(tid));
			}
			
			List<String> sequence;
			String svgDendrogram;
			int dendrogramWidth;
			int dendrogramHeight;
			if (cacheMap == null) {
				cacheMap = new HashMap<Integer, DendrogramCache>();
			}
			DendrogramCache cache = cacheMap.get(Integer.valueOf(distanceType * 10 + linkageType));
			if (cache == null) {
				Dendrogram dendrogram = sampleDistanceClustering(sampleIdList, distanceType, linkageTypes[linkageType]);
				
				sequence = dendrogram.getSequence();
				svgDendrogram = dendrogram.getScaleUpSvgImageContentAtLeft(0, 6, 1, 2, 130d, true);
				dendrogramWidth = dendrogram.getDendrogramWidth(130d);
				dendrogramHeight = dendrogram.getDendrogramHeight(2);
				
				cacheMap.put(Integer.valueOf(distanceType * 10 + linkageType), new DendrogramCache(sequence, svgDendrogram,
						dendrogramWidth, dendrogramHeight));
			} else {
				sequence = cache.getSequence();
				svgDendrogram = cache.getSvgDendrogram();
				dendrogramWidth = cache.getDendrogramWidth();
				dendrogramHeight = cache.getDendrogramHeight();
			}

			// add bar chart
			// shiftX depend on the sample id length
			int maxSampleLength = getMaxItemLength(sequence);
			
			String svgBarChart = createSvgBarChart(rows, sequence.toArray(new String[] {}), rhList, topNRankIdList,
					allReadsMap, dendrogramWidth + maxSampleLength * 10 + 10);
			
			int maxItemLength = getMaxItemLength(rhList);
			int charWidth = 7;
			
			StringBuffer ret = new StringBuffer();
			
			int canvasHeight = dendrogramHeight + 60;
			int canvasWidth = dendrogramWidth + 60 + 600 + 60 + maxItemLength * charWidth;
			ret.append(String.format("<svg height=\"%d\" width=\"%d\">\n", canvasHeight, canvasWidth));
			ret.append(String.format("\t<rect x=\"1\" y=\"1\" fill=\"white\" id=\"chart_body\" height=\"%d\" width=\"%d\" />\n", canvasHeight - 2, canvasWidth - 2));
			
			ret.append(svgBarChart);
			ret.append(svgDendrogram);
			
			ret.append("</svg>\n");
			
			VisualizationtResult visualizationtResult = new VisualizationtResult(ret.toString());
			visualizationtResult.setDendrogramMap(cacheMap);
			return visualizationtResult;
			
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

		return null;
	}

	private Double calculateCorrelation(Integer correlationMethod, double[] list1, double[] list2) {
		double value;
		if (correlationMethod.equals(GutFloraConstant.CORRELATION_PEARSON_VALUE)) {
			PearsonsCorrelation correlation = new PearsonsCorrelation();
			value= correlation.correlation(list1, list2);
		} else {
			SpearmansCorrelation correlation = new SpearmansCorrelation();
			value= correlation.correlation(list1, list2);
		}
		return Double.valueOf(value);
	}

	private Double calculateOLSMultipleLinearRegression(double[][] x, double[] y) {

		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		regression.newSampleData(y, x);

		double value = regression.calculateAdjustedRSquared();
		
		return Double.valueOf(value);
	}
	
	private String createSvgBarChart(Map<String, Map<String, String>> rows, String[] clusteredSampleIdList,
			List<String> columnNames, List<String> rankIdList, Map<String, Double> allReadsMap, int shiftX) {
		StringBuffer ret = new StringBuffer();
		
		// these values should be adjustable
		float fullWidth = 600f;
		int barHeight = 18;
		int shiftY = 0;
		int lineHeight = 16;
		int scaleY = 2;

		for (int s = 0; s < clusteredSampleIdList.length; s++) {
			String sid = clusteredSampleIdList[s];
			Double allReads = allReadsMap.get(sid);
			int y = shiftY + s * lineHeight * scaleY + 6;
			int x = shiftX;
			ret.append("<g stroke-width=\"1\" stroke=\"none\" class=\"comboBar\">\n");
			Map<String, String> sampleReadData = rows.get(sid);
			// if sampleReadData == null, should print an empty bar
			if (sampleReadData == null) {
				ret.append(String.format("\t<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\">\n",
						x, y, (int)fullWidth - 1, barHeight, GutFloraConstant.BARCHART_COLORLESS));
				ret.append("\t<title>no reads</title>\n");
				ret.append("</rect>\n");
				ret.append("</g>\n");
			} else {
				for (int i = 0; i < columnNames.size(); i++) {
					String value = sampleReadData.get(columnNames.get(i));
					if (value == null) {
						value = "0";
						continue;
					}
					int width = Math.round(Float.valueOf(value).floatValue() / allReads.floatValue() * fullWidth);
					if (width < 2) {
						continue;
					}
					// in case we run out of the colors ... 
					int colorIndex = i;
					while (colorIndex > GutFloraConstant.BARCHART_COLOR.size() - 1) {
						colorIndex -= GutFloraConstant.BARCHART_COLOR.size();
					}
					ret.append(String.format("\t<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" class=\"taxonBar\">\n",
							x, y, width - 1, barHeight, GutFloraConstant.BARCHART_COLOR.get(colorIndex)));
					String reads = String.format("%,d", Integer.valueOf(value));
					ret.append(String.format("\t<title>%s: %s (%.2f%%)</title>\n", columnNames.get(i), reads, Float
							.valueOf(value).floatValue() / allReads.floatValue() * 100));
					ret.append(String.format("\t<desc>%s</desc>\n", rankIdList.get(i)));
					ret.append(String.format("\t<desc>%s</desc>\n", columnNames.get(i)));
					ret.append("</rect>\n");
					x += width;
				}
				String value = sampleReadData.get(GutFloraConstant.COLUMN_HEADER_OTHERS);
				if (value != null) {
					int colorIndex = columnNames.size();
					while (colorIndex > GutFloraConstant.BARCHART_COLOR.size() - 1) {
						colorIndex -= GutFloraConstant.BARCHART_COLOR.size();
					}
					int width = shiftX + (int) fullWidth - x;
					if (width > 1) {
						ret.append(String.format("\t<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\">\n",
								x, y, width, barHeight, GutFloraConstant.BARCHART_COLOR.get(colorIndex)));
						String reads = String.format("%,d", Integer.valueOf(value));
						ret.append(String.format("\t<title>%s: %s (%.2f%%)</title>\n", GutFloraConstant.COLUMN_HEADER_OTHERS,
								reads, Float.valueOf(value).floatValue() / allReads.floatValue() * 100));
						ret.append("</rect>\n");
					}
				}
				ret.append("</g>\n");
			}
		}
		
		// draw the legend
		ret.append("<g stroke-width=\"1\" stroke=\"none\" class=\"barLegend\">\n");
		int legendX = shiftX + 600 + 20;
		for (int i = 0; i < columnNames.size(); i++) {
			String colName = columnNames.get(i);
			int colorIndex = i;
			while (colorIndex > GutFloraConstant.BARCHART_COLOR.size() - 1) {
				colorIndex -= GutFloraConstant.BARCHART_COLOR.size();
			}
			ret.append(String.format("\t<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" />\n",
					legendX, i * 16, 12, 12, GutFloraConstant.BARCHART_COLOR.get(colorIndex)));
			
			ret.append(String.format("\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"12\" fill=\"black\">%s</text>\n", 
					legendX + 16, i * 16 + 12, colName));
		}
		int colorIndex = columnNames.size();
		while (colorIndex > GutFloraConstant.BARCHART_COLOR.size() - 1) {
			colorIndex -= GutFloraConstant.BARCHART_COLOR.size();
		}
		ret.append(String.format("\t<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" />\n",
				legendX, columnNames.size() * 16, 12, 12, GutFloraConstant.BARCHART_COLOR.get(colorIndex)));
		ret.append(String.format("\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"12\" fill=\"black\">%s</text>\n", 
				legendX + 16, columnNames.size() * 16 + 12, "Others"));
		ret.append("</g>\n");
		
		return ret.toString();
	}

	@Override
	public GutFloraAnalysisData getReadsAnalysisData(Set<SampleEntry> selectedSamples, String rank,
			List<String> selectedcolumns) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		String q0Rank = rank;
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			String columnString = "'" + StringUtils.join(selectedcolumns, "','") + "'";
			
			// run this SQL to get taxonomy id
			Statement statement0 = connection.createStatement();
			String sqlQuery0 = " select t.id from taxonomy as t " + " join taxon_rank as rank on rank.id = t.rank_id "
					+ " where rank.name = '" + q0Rank + "' and t.name in (" + columnString + ")";

			ResultSet results0 = statement0.executeQuery(sqlQuery0);
			List<String> rankIdList = new ArrayList<String>();
			while (results0.next()) {
				String rid = results0.getString(1);
				rankIdList.add(rid);
			}
			
			String rankIdString = "'" + StringUtils.join(rankIdList, "','") + "'";
 			
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, t.id as taxon_id, t.name as taxon_name, " + "sum(read_num)" + " as all_reads "
					+ " from microbiota join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and t.id in (" + rankIdString + ") "
					+ " group by sample_id, t.id, t.name ";
			
			Map<String, Map<String, String>> rows = new HashMap<String, Map<String,String>>();
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			Map<String, String> taxonomyMap = new HashMap<String, String>();
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				String tid = results1.getString("taxon_id");
				String name = results1.getString("taxon_name");
				taxonomyMap.put(tid, name);
				int allReads = results1.getInt("all_reads");
				if (rows.get(sid) == null) {
					rows.put(sid, new HashMap<String, String>());
				}
				String displayValue = String.valueOf((int) allReads);
				rows.get(sid).put(name, displayValue);
			}
 			
			Statement statement2 = connection.createStatement();
			String sqlQuery2 = " select sample_id, sum(read_num) as all_reads from microbiota "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " group by sample_id ";
			
			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			Map<String,Double> allReadsMap = new HashMap<String, Double>();
			while (results2.next()) {
				String sid = results2.getString("sample_id");
				int allReads = results2.getInt("all_reads");
				allReadsMap.put(sid, Double.valueOf(allReads));
				Map<String, String> map = rows.get(sid);
				int sum = 0;
				for (String name: selectedcolumns) {
					String value = map.get(name);
					if (value != null) {
						sum += Integer.valueOf(value);
					}
				}
				if (rows.get(sid) == null) {
					rows.put(sid, new HashMap<String, String>());
				}
				String displayValue = String.valueOf((int) (allReads - sum));
				rows.get(sid).put(GutFloraConstant.COLUMN_HEADER_OTHERS, displayValue);
			}

			connection.close();
			ds.close();
			
			GutFloraAnalysisData ret = new GutFloraAnalysisData(rows, sampleIdList);
			ret.setReadsData(selectedcolumns, null);
			return ret;
			
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

		
	private Dendrogram sampleDistanceClustering(List<String> sampleIdList, int distanceType, LinkageType type) {
		Map<String, Map<String, Double>> matrix = getSampleDistanceMatrix(sampleIdList, distanceType);
		
		HierarchicalClustering hc = new HierarchicalClustering(matrix);
		
		Dendrogram dendrogram = hc.getDendrogram(type);
		
		return dendrogram;
	}

	@Override
	public GutFloraAnalysisData getProfilesAnalysisData(Set<SampleEntry> selectedSamples, String lang) {
		return getProfilesAnalysisData(selectedSamples, GutFloraConstant.DEFAULT_NUM_OF_COLUMNS, lang);
	}
	// TODO consider to create a class for the parameter, which holds title, id and a boolean (isNumeric)
	private GutFloraAnalysisData getProfilesAnalysisData(Set<SampleEntry> selectedSamples, int numOfColumns,
			String lang) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Map<String, Map<String, String>> rows = new HashMap<String, Map<String,String>>();
			Statement statement0 = connection.createStatement();
			
			String sqlQuery0 = "select pi.id as id, pi.title as title, pi.unit as unit, pt.type_name as type "
					+ " from parameter_info as pi " + " join parameter_type as pt on pt.id = pi.type_id "
					+ " order by pi.sysid";

			ResultSet results0 = statement0.executeQuery(sqlQuery0);
			List<String> profileNameList = new ArrayList<String>();
			List<String> topNIdList = new ArrayList<String>();
			Set<String> numericProfiles = new HashSet<String>();
			Map<String, String> unitMap = new HashMap<String, String>();
			while (results0.next()) {
				String id = results0.getString("id");
				String name = results0.getString("title");
				String unit = results0.getString("unit");
				profileNameList.add(name);
				String type = results0.getString("type");
				if (type.equals(GutFloraConstant.PARA_TYPE_CONTINUOUS)) {
					numericProfiles.add(name);
				}
				if (unit != null) {
					unitMap.put(name, unit);
				}
				if (topNIdList.size() < numOfColumns) {
					topNIdList.add(id);
				}
			}
			
			String topIdString = "'" + StringUtils.join(topNIdList, "','") + "'";
			
			Statement statement2 = connection.createStatement();
			String sqlQuery2 = " select sample_id, pi.title as title, pv.parameter_value "
					+ " from parameter_value as pv " + " join parameter_info as pi on pi.id = pv.parameter_id "
					+ " where sample_id in (" + sampleIdString + ") " 
					+ " and pv.parameter_id in (" + topIdString + ") ";
			
			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			while (results2.next()) {
				String sid = results2.getString("sample_id");
				String name = results2.getString("title");
				String valueString = results2.getString("parameter_value");
				if (rows.get(sid) == null) {
					rows.put(sid, new HashMap<String, String>());
				}
				String para = "-";
				if (valueString != null) {
					if (valueString.contains(".") && valueString.indexOf('.') + 3 < valueString.length()) {
						para = valueString.substring(0, valueString.indexOf('.') + 3);
					} else {
						para = valueString;
					}
				}
				rows.get(sid).put(name, para);
			}
			
			connection.close();
			ds.close();
			
			List<String> phList = new ArrayList<String>();
			for (int i = 0; i < profileNameList.size() && 
					i < numOfColumns; i++) {
				phList.add(profileNameList.get(i));
			}
			
			GutFloraAnalysisData ret = new GutFloraAnalysisData(rows, sampleIdList);
			ret.setProfilesData(phList, profileNameList, numericProfiles);
			ret.setMetadataMap(unitMap);
			return ret;
			
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

	@Override
	public GutFloraAnalysisData getProfilesAnalysisData(Set<SampleEntry> selectedSamples, List<String> selectedcolumns,
			String lang) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			String columnString = "'" + StringUtils.join(selectedcolumns, "','") + "'";
			
			Map<String, Map<String, String>> rows = new HashMap<String, Map<String,String>>();
			Statement statement0 = connection.createStatement();
			// TODO: [to be improved] maybe it's better to use id instead of name
			// then we don'nt need this query...
			String sqlQuery0 = " select id " + " from parameter_info where title in ("
					+ columnString + ") ";
			
			ResultSet results0 = statement0.executeQuery(sqlQuery0);
			List<String> profileIdList = new ArrayList<String>();
			while (results0.next()) {
				String id = results0.getString("id");
				profileIdList.add(id);
			}
			
			String idString = "'" + StringUtils.join(profileIdList, "','") + "'";
			
			Statement statement2 = connection.createStatement();
			
			String queryFields2 = " select sample_id, pi.title as title, pv.parameter_value, pt.type_name as type ";
			String sqlQuery2 = queryFields2 + " from parameter_value as pv "
					+ " join parameter_info as pi on pi.id = parameter_id " 
					+ " join parameter_type as pt on pt.id = pi.type_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and pv.parameter_id in (" + idString + ") ";
			
			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			Set<String> numericParameters = new HashSet<String>();
			while (results2.next()) {
				String sid = results2.getString("sample_id");
				String name = results2.getString("title");
				String valueString = results2.getString("parameter_value");
				if (rows.get(sid) == null) {
					rows.put(sid, new HashMap<String, String>());
				}
				String para = "-";
				if (valueString != null) {
					if (valueString.contains(".") && valueString.indexOf('.') + 3 < valueString.length()) {
						para = valueString.substring(0, valueString.indexOf('.') + 3);
					} else {
						para = valueString;
					}
				}
				rows.get(sid).put(name, para);
				
				String type = results2.getString("type");
				if (type.equals(GutFloraConstant.PARA_TYPE_CONTINUOUS)) {
					numericParameters.add(name);
				}
			}
			
			connection.close();
			ds.close();
			
			GutFloraAnalysisData ret = new GutFloraAnalysisData(rows, sampleIdList);
			ret.setProfilesData(selectedcolumns, null, numericParameters);
			return ret;

			
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

	@Override
	public List<TaxonEntry> getAllTaxonEntries(Set<SampleEntry> selectedSamples, String rank) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			// run this SQL to limit to the top n taxonomy
			Statement statement0 = connection.createStatement();
			String sqlQuery0 = " select " + rank + "_id as taxon_id, t.name as taxon_name from microbiota "
					+ " join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and " + rank + "_id is not null group by " + rank 
					+ "_id, t.name order by sum(read_num) desc ";
			
			ResultSet results0 = statement0.executeQuery(sqlQuery0);
			List<TaxonEntry> rankNameList = new ArrayList<TaxonEntry>();
			while (results0.next()) {
				String id = results0.getString("taxon_id");
				String name = results0.getString("taxon_name");
				rankNameList.add(new TaxonEntry(name, id, rank));
			}
			
			connection.close();
			ds.close();
			
			return rankNameList;
			
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

	@Override
	public List<ParameterEntry> getAllNumericParameterEntry(String lang) {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			String sqlQuery = " select pi.id as id, pi.title as title " + " from parameter_info as pi "
					+ " join parameter_type as pt on pt.id = pi.type_id " + " where pt.type_name = '"
					+ GutFloraConstant.PARA_TYPE_CONTINUOUS + "' " + " order by pi.id ";
			
			ResultSet results = statement.executeQuery(sqlQuery);
			List<ParameterEntry> parameterList = new ArrayList<ParameterEntry>();
			while (results.next()) {
				String name = results.getString("title");
				String paraId = results.getString("id");
				parameterList.add(new ParameterEntry(paraId, name));
			}
			
			connection.close();
			ds.close();
			
			return parameterList;
			
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

	@Override
	public List<ParameterEntry> getAllParameterEntry() {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			String sqlQuery = " SELECT pi.id AS id, pi.title AS title, pi.type_id AS type "
					+ " FROM parameter_info AS pi ORDER BY sysid ";
			
			ResultSet results = statement.executeQuery(sqlQuery);
			List<ParameterEntry> parameterList = new ArrayList<ParameterEntry>();
			while (results.next()) {
				String name = results.getString("title");
				String paraId = results.getString("id");
				int type = results.getInt("type");
				parameterList.add(new ParameterEntry(paraId, name, Integer.valueOf(type)));
			}
			
			connection.close();
			ds.close();
			
			return parameterList;
			
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
	
	@Override
	public PairListData getReadsAndPctListById(Set<SampleEntry> selectedSamples, String rank, String taxonId) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, sum(read_pct) as all_reads_pct from microbiota "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and " + rank + "_id = '" + taxonId + "' " + " group by sample_id ";
			
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			Map<String, Double> readPctMap = new HashMap<String, Double>();
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				double allReadsPct = results1.getDouble("all_reads_pct");
				readPctMap.put(sid, Double.valueOf(allReadsPct));
			}

			Statement statement2 = connection.createStatement();
			String sqlQuery2 = " select sample_id, sum(read_num) as all_reads from microbiota "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and " + rank + "_id = '" + taxonId + "' " + " group by sample_id ";
			
			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			Map<String, Double> readMap = new HashMap<String, Double>();
			while (results2.next()) {
				String sid = results2.getString("sample_id");
				double allReads = results2.getDouble("all_reads");
				readMap.put(sid, Double.valueOf(allReads));
			}
			
			connection.close();
			ds.close();
			return new PairListData(getOriginalPctList(sampleIdList, readPctMap), getOriginalList(sampleIdList,
					readMap, true));
			
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

	@Override
	public PairListData getReadsAndPctList(Set<SampleEntry> selectedSamples, String rank, String taxonName) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Statement statement0 = connection.createStatement();
			String sqlQuery0 = " select sample_id, sum(read_num) as all_reads from microbiota "
					+ " join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and t.name = '" + taxonName + "' " + " group by sample_id ";
			
			ResultSet results0 = statement0.executeQuery(sqlQuery0);
			Map<String, Double> readMap = new HashMap<String, Double>();
			while (results0.next()) {
				String sid = results0.getString("sample_id");
				double allReads = results0.getDouble("all_reads");
				readMap.put(sid, Double.valueOf(allReads));
			}
			
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, sum(read_pct) as all_reads_pct from microbiota "
					+ " join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and t.name = '" + taxonName + "' " + " group by sample_id ";
			
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			Map<String, Double> readPctMap = new HashMap<String, Double>();
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				double allReadsPct = results1.getDouble("all_reads_pct");
				readPctMap.put(sid, Double.valueOf(allReadsPct));
			}
			
			connection.close();
			ds.close();
			return new PairListData(getOriginalPctList(sampleIdList, readPctMap),
					getOriginalList(sampleIdList, readMap, true));
			
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
	
	private List<String> getOriginalList(List<String> sampleIdList, Map<String, Double> valueMap, boolean isInteger) {
		List<String> ret = new ArrayList<String>();
		for (int i = 0; i < sampleIdList.size(); i++) {
			Double value = valueMap.get(sampleIdList.get(i));
			if (value == null) {
				value = Double.valueOf(0);
			}
			String valueString = String.valueOf(value);
			if (isInteger) {
				ret.add(valueString.substring(0, valueString.indexOf('.')));
			} else {
				valueString = valueString.indexOf('.') + 3 < valueString.length() ? valueString.substring(0,
						valueString.indexOf('.') + 3) : valueString;
				ret.add(valueString);
			}
		}
		return ret;
	}

	private List<String> getOriginalPctList(List<String> sampleIdList, Map<String, Double> valueMap) {
		List<String> ret = new ArrayList<String>();
		for (int i = 0; i < sampleIdList.size(); i++) {
			Double value = valueMap.get(sampleIdList.get(i));
			if (value == null) {
				value = Double.valueOf(0);
			}
			String valueString = String.valueOf(value);
			ret.add(valueString);
		}
		return ret;
	}

	private double[] getOrderedList(List<String> sampleIdList, Map<String, Double> valueMap) {
		double[] ret = new double[sampleIdList.size()];
		for (int i = 0; i < sampleIdList.size(); i++) {
			Double value = valueMap.get(sampleIdList.get(i));
			if (value == null) {
				// TODO should be able to deal with the missing values...
				value = Double.valueOf(0);
			}
			ret[i] = value.doubleValue();
		}
		return ret;
	}

	private double[][] getOrderedMatrix(List<String> sampleIdList, Map<String, Double[]> valueMap) {
		double[][] ret = new double[sampleIdList.size()][];
		for (int i = 0; i < sampleIdList.size(); i++) {
			Double[] values = valueMap.get(sampleIdList.get(i));
			ret[i] = new double[values.length];
			for (int j = 0; j < values.length; j++) {
				Double value = values[j];
				if (value == null) {
					// TODO should be able to deal with the missing values...
					value = Double.valueOf(0);
				}
				ret[i][j] = value.doubleValue();
			}
		}
		return ret;
	}

	@Override
	public PairListData getProfilesList(Set<SampleEntry> selectedSamples, String name, String lang) {
		// TODO [to be improved] should be better to use parameter id ...
		List<String> sampleIdList = getSortedSampleList(selectedSamples);

		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Statement statement2 = connection.createStatement();

			String queryFields2 = " and pi.title = '";
			
			String sqlQuery2 = " select sample_id, parameter_value " + " from parameter_value "
					+ " join parameter_info as pi on pi.id = parameter_id " 
					+ " where sample_id in (" + sampleIdString + ") "
					+ queryFields2 + name + "' ";
			
			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			Map<String, Double> readMap = new HashMap<String, Double>();
			while (results2.next()) {
				String sid = results2.getString("sample_id");
				double value = results2.getDouble("parameter_value");
				readMap.put(sid, Double.valueOf(value));
			}
			
			connection.close();
			ds.close();

			return new PairListData(getOriginalList(sampleIdList, readMap, false));
			
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
	
	@Override
	public PairListData getProfilesListById(Set<SampleEntry> selectedSamples, String paraId) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, parameter_value " + " from parameter_value "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and parameter_id = '" + paraId + "'";
			
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			Map<String, Double> readMap = new HashMap<String, Double>();
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				double value = results1.getDouble("parameter_value");
				readMap.put(sid, Double.valueOf(value));
			}
			
			connection.close();
			ds.close();
			
			PairListData ret = new PairListData(getOriginalList(sampleIdList, readMap, false));
			return ret;
			
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
	
	@Override
	public String getCorrelationString(Integer correlationMethod, List<String> list1, List<String> list2) {
		double[] doubleList1 = new double[list1.size()];
		for (int i = 0; i < list1.size(); i++) {
			doubleList1[i] = Double.valueOf(list1.get(i)).doubleValue();
		}
		double[] doubleList2 = new double[list2.size()];
		for (int i = 0; i < list2.size(); i++) {
			doubleList2[i] = Double.valueOf(list2.get(i)).doubleValue();
		}
		return String.format("%+.2f", calculateCorrelation(correlationMethod, doubleList1, doubleList2));
	}
	
	/**
	 * unfinished implementation ...
	 */
	@Override
	public SearchResultData searchForSimilerProfiles(Set<SampleEntry> selectedSamples, String rank,
			List<String> taxonNames, String lang) {
		// do multiple linear regression (MLR)
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Map<String, Double[]> readMap = new HashMap<String, Double[]>();
			for (int i = 0; i < taxonNames.size(); i++) {
				String taxonName = taxonNames.get(i);
				Statement statement1 = connection.createStatement();
				String sqlQuery1 = " select sample_id, sum(read_pct) as all_reads_pct "
						+ " from microbiota join taxonomy as t on t.id = " + rank + "_id "
						+ " where sample_id in (" + sampleIdString + ") "
						+ " and t.name = '" + taxonName + "' " + " group by sample_id ";
				
				
				ResultSet results1 = statement1.executeQuery(sqlQuery1);
				while (results1.next()) {
					String sid = results1.getString("sample_id");
					double allReadsPct = results1.getDouble("all_reads_pct");
					if (readMap.get(sid) == null) {
						readMap.put(sid, new Double[taxonNames.size()]);
					}
					readMap.get(sid)[i] = Double.valueOf(allReadsPct);
				}
			}
			
			Statement statement2 = connection.createStatement();
			
			String queryFields = " select sample_id, pi.title as title, parameter_value ";
			String sqlQuery2 = queryFields + " from parameter_value "
					+ " join parameter_info as pi on pi.id = parameter_id "
					+ " join parameter_type as pt on pt.id = pi.type_id " 
					+ " where sample_id in (" + sampleIdString + ") " 
					+ " and pt.type_name = '" + GutFloraConstant.PARA_TYPE_CONTINUOUS + "' ";
			
			// key: profile_name -> value: (key: sample_id -> value: profile_value)
			Map<String, Map<String, Double>> rows = new HashMap<String, Map<String,Double>>();
			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			while (results2.next()) {
				String sampleId = results2.getString("sample_id");
				String profileName = results2.getString("title");
				double value = results2.getDouble("parameter_value");
				if (rows.get(profileName) == null) {
					rows.put(profileName, new HashMap<String, Double>());
				}
				rows.get(profileName).put(sampleId, Double.valueOf(value));
			}
			connection.close();
			ds.close();
			
			double[][] referenceList = getOrderedMatrix(sampleIdList, readMap);
			
			final Map<String, Double> results = new HashMap<String, Double>();
			for (String key : rows.keySet()) {
				double[] arrayList = getOrderedList(sampleIdList, rows.get(key));
				results.put(key, calculateOLSMultipleLinearRegression(referenceList, arrayList));
			}
			
			List<String> nameList = new ArrayList<String>(rows.keySet());
			Collections.sort(nameList, new Comparator<String>() {
				
				@Override
				public int compare(String o1, String o2) {
					return results.get(o2).compareTo(results.get(o1));
				}
			});
			
			List<List<String>> ret = new ArrayList<List<String>>();
			for (String n : nameList) {
				// ignore those correction could nor be calculated (cause NaN)
				Double corrValue = results.get(n);
				if (!corrValue.equals(Double.NaN)) {
					ret.add(Arrays.asList(n, String.format("%+.2f", corrValue)));
				}
			}
			
			// TODO don't forget here... not finished yet
			return new SearchResultData(rank, GutFloraConstant.NAVI_LINK_SUFFIX_READ, "TO_BE_DEFINED", 
					GutFloraConstant.MULTIPLE_LINEAR_REGRESSION_VALUE, ret);
			
			
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
	
	@Override
	public SearchResultData searchForSimilerProfiles(Set<SampleEntry> selectedSamples, String rank, String taxonName,
			Integer correlationMethod, String lang) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, sum(read_pct) as all_reads_pct "
					+ " from microbiota join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and t.name = '" + taxonName + "' " + " group by sample_id ";
			
			
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			Map<String, Double> readMap = new HashMap<String, Double>();
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				double allReadsPct = results1.getDouble("all_reads_pct");
				readMap.put(sid, Double.valueOf(allReadsPct));
			}
			
			Statement statement2 = connection.createStatement();
			
			String queryFields = " select sample_id, pi.title as title, parameter_value ";
			String sqlQuery2 = queryFields + " from parameter_value "
					+ " join parameter_info as pi on pi.id = parameter_id "
					+ " join parameter_type as pt on pt.id = pi.type_id " 
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and pt.type_name = '" + GutFloraConstant.PARA_TYPE_CONTINUOUS + "' ";
			
			// key: profile_name -> value: (key: sample_id -> value: profile_value)
			Map<String, Map<String, Double>> rows = new HashMap<String, Map<String,Double>>();
			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			while (results2.next()) {
				String sampleId = results2.getString("sample_id");
				String profileName = results2.getString("title");
				double value = results2.getDouble("parameter_value");
				if (rows.get(profileName) == null) {
					rows.put(profileName, new HashMap<String, Double>());
				}
				rows.get(profileName).put(sampleId, Double.valueOf(value));
			}
			connection.close();
			ds.close();
			
			double[] referenceList = getOrderedList(sampleIdList, readMap);
			
			final Map<String, Double> results = new HashMap<String, Double>();
			for (String key : rows.keySet()) {
				double[] arrayList = getOrderedList(sampleIdList, rows.get(key));
				results.put(key, calculateCorrelation(correlationMethod, referenceList, arrayList));
			}
			
			List<String> nameList = new ArrayList<String>(rows.keySet());
			Collections.sort(nameList, new Comparator<String>() {
				
				@Override
				public int compare(String o1, String o2) {
					return results.get(o2).compareTo(results.get(o1));
				}
			});
			
			List<List<String>> ret = new ArrayList<List<String>>();
			for (String n : nameList) {
				// ignore those correction could nor be calculated (cause NaN)
				Double corrValue = results.get(n);
				if (!corrValue.equals(Double.NaN)) {
					ret.add(Arrays.asList(n, String.format("%+.2f", corrValue)));
				}
			}
			
			return new SearchResultData(rank, GutFloraConstant.NAVI_LINK_SUFFIX_READ, taxonName, correlationMethod, ret);
			
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
	
	@Override
	public SearchResultData searchForSimilarReads(Set<SampleEntry> selectedSamples, String rank, String name,
			Integer correlationMethod, String lang) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);

		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Statement statement2 = connection.createStatement();
			String constraint2 = " and pi.title = '";
			String sqlQuery2 = " select sample_id, parameter_value from parameter_value "
					+ " join parameter_info as pi on pi.id = parameter_id " + " where sample_id in (" + sampleIdString
					+ ") " + constraint2 + name + "' ";

			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			Map<String, Double> readMap = new HashMap<String, Double>();
			while (results2.next()) {
				String sid = results2.getString("sample_id");
				double value = results2.getDouble("parameter_value");
				readMap.put(sid, Double.valueOf(value));
			}
			
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, t.id as taxon_id, t.name as taxon_name, sum(read_pct) as all_reads_pct "
					+ " from microbiota join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " group by sample_id, t.id, t.name ";
			
			// key: taxon_name -> value: (key: sample_id -> value: read_value)
			Map<String, Map<String, Double>> readValueMaps = new HashMap<String, Map<String,Double>>();
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			Map<String, String> taxonomyMap = new HashMap<String, String>();
			while (results1.next()) {
				String sampleId = results1.getString("sample_id");
				String taxonId = results1.getString("taxon_id");
				String taxonName = results1.getString("taxon_name");
				taxonomyMap.put(taxonId, taxonName);
				double allReadsPct = results1.getDouble("all_reads_pct");
				if (readValueMaps.get(taxonId) == null) {
					readValueMaps.put(taxonId, new HashMap<String, Double>());
				}
				readValueMaps.get(taxonId).put(sampleId, Double.valueOf(allReadsPct));
			}

			connection.close();
			ds.close();

			double[] referenceList = getOrderedList(sampleIdList, readMap);
			
			final Map<String, Double> results = new HashMap<String, Double>();
			for (String key : readValueMaps.keySet()) {
				double[] arrayList = getOrderedList(sampleIdList, readValueMaps.get(key));
				results.put(key, calculateCorrelation(correlationMethod, referenceList, arrayList));
			}

			List<String> nameList = new ArrayList<String>(readValueMaps.keySet());
			Collections.sort(nameList, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return results.get(o2).compareTo(results.get(o1));
				}
			});
			
			List<List<String>> ret = new ArrayList<List<String>>();
			for (String n : nameList) {
				// ignore those correction could nor be calculated (cause NaN)
				Double corrValue = results.get(n);
				if (!corrValue.equals(Double.NaN)) {
					ret.add(Arrays.asList(taxonomyMap.get(n), String.format("%+.2f", corrValue)));
				}
			}

			return new SearchResultData(rank, GutFloraConstant.NAVI_LINK_SUFFIX_PROFILE, name, correlationMethod, ret);
			
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

	@Override
	public SearchResultData searchForSimilerProfilesbyProfile(Set<SampleEntry> selectedSamples, String name,
			String paraType, Integer correlationMethod, String lang) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Statement statement1 = connection.createStatement();
			String constraint1 = " and pi.title = '";
			String sqlQuery1 = " select sample_id, parameter_value from parameter_value "
					+ " join parameter_info as pi on pi.id = parameter_id " + " where sample_id in (" + sampleIdString
					+ ") " + constraint1 + name + "' ";
			
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			Map<String, Double> readMap = new HashMap<String, Double>();
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				double value = results1.getDouble("parameter_value");
				readMap.put(sid, Double.valueOf(value));
			}
			
			String queryFields = " select sample_id, pi.title as title, parameter_value ";
			Statement statement2 = connection.createStatement();
			String sqlQuery2 = queryFields + " from parameter_value "
					+ " join parameter_info as pi on pi.id = parameter_id "
					+ " join parameter_type as pt on pt.id = pi.type_id " 
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and pt.type_name = '" + GutFloraConstant.PARA_TYPE_CONTINUOUS + "' ";
			
			// key: profile_name -> value: (key: sample_id -> value: profile_value)
			Map<String, Map<String, Double>> rows = new HashMap<String, Map<String,Double>>();
			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			while (results2.next()) {
				String sampleId = results2.getString("sample_id");
				String profileName = results2.getString("title");
				double value = results2.getDouble("parameter_value");
				if (rows.get(profileName) == null) {
					rows.put(profileName, new HashMap<String, Double>());
				}
				rows.get(profileName).put(sampleId, Double.valueOf(value));
			}
			connection.close();
			ds.close();
			
			double[] referenceList = getOrderedList(sampleIdList, readMap);
			
			final Map<String, Double> results = new HashMap<String, Double>();
			List<String> nameList = new ArrayList<String>();
			for (String key : rows.keySet()) {
				if (key.equals(name)) {
					continue;
				}
				double[] arrayList = getOrderedList(sampleIdList, rows.get(key));
				results.put(key, calculateCorrelation(correlationMethod, referenceList, arrayList));
				nameList.add(key);
			}
			
			Collections.sort(nameList, new Comparator<String>() {
				
				@Override
				public int compare(String o1, String o2) {
					return results.get(o2).compareTo(results.get(o1));
				}
			});
			
			List<List<String>> ret = new ArrayList<List<String>>();
			for (String n : nameList) {
				Double corrValue = results.get(n);
				if (!corrValue.equals(Double.NaN)) {
					ret.add(Arrays.asList(n, String.format("%+.2f", corrValue)));
				}
			}
			
			return new SearchResultData(GutFloraConstant.ANALYSIS_TYPE_PROFILE,
					GutFloraConstant.NAVI_LINK_SUFFIX_PROFILE, name, correlationMethod, ret);
			
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
	
	@Override
	public Map<String, Double[]> getAllReadsPctList(Set<SampleEntry> selectedSamples, String rank,
			List<String> taxonNames) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Map<String, Double[]> readMap = new HashMap<String, Double[]>();
			for (int i = 0; i < taxonNames.size(); i++) {
				String taxonName = taxonNames.get(i);
				Statement statement1 = connection.createStatement();
				String sqlQuery1 = " select sample_id, sum(read_pct) as all_reads_pct "
						+ " from microbiota join taxonomy as t on t.id = " + rank + "_id "
						+ " where sample_id in (" + sampleIdString + ") "
						+ " and t.name = '" + taxonName + "' " + " group by sample_id ";
				
				
				ResultSet results1 = statement1.executeQuery(sqlQuery1);
				while (results1.next()) {
					String sid = results1.getString("sample_id");
					double allReadsPct = results1.getDouble("all_reads_pct");
					if (readMap.get(sid) == null) {
						readMap.put(sid, new Double[taxonNames.size()]);
					}
					readMap.get(sid)[i] = Double.valueOf(allReadsPct);
				}
			}
			
			connection.close();
			ds.close();
			
			return readMap;
			
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
	
	@Override
	public Map<String, String> getSampleDiversity(Set<SampleEntry> selectedSamples) {
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		Map<String,String> diverMap = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Statement statement3 = connection.createStatement();
			String sqlQuery3 = " select sample_id, shannon, simpson "
					+ " from sample_diversity "
					+ " where sample_id in (" + sampleIdString + ") ";
			
			ResultSet results3 = statement3.executeQuery(sqlQuery3);
			diverMap = new HashMap<String, String>();
			while (results3.next()) {
				String sid = results3.getString("sample_id");
				// TODO to be refined!
				String shannon = results3.getString("shannon").substring(0, 4);
				String simpson = results3.getString("simpson").substring(0, 4);
				diverMap.put(sid, String.format("%s|%s", shannon, simpson));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			if (ds != null) {
				ds.close();
			}
		}
		return diverMap;
	}

	@Override
	public List<String> getSampleDiversity(String sampleId) {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		List<String> ret = null;
		try {
			connection = ds.getConnection();
			
			Statement statement3 = connection.createStatement();
			String sqlQuery3 = " select shannon, simpson "
					+ " from sample_diversity "
					+ " where sample_id = '" + sampleId +"' ";
			
			ResultSet results3 = statement3.executeQuery(sqlQuery3);
			if (results3.next()) {
				// TODO to be refined!
				String shannon = results3.getString("shannon").substring(0, 4);
				String simpson = results3.getString("simpson").substring(0, 4);
				ret = Arrays.asList(shannon, simpson);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			if (ds != null) {
				ds.close();
			}
		}
		return ret;
	}
	
	@Override
	public VisualizationtResult getReadsHeatmap(Set<SampleEntry> selectedSamples, String rank) {
		// suppose the minimal display number is 10
		int minimalDisplayNumber = 10;
		
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		String q0Rank = rank;
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";

			// retrieve taxonomy from dominant_taxon table
			Statement statementA = connection.createStatement();
			String sqlQueryA = " select distinct tx.id as taxon_id, tx.name as taxon_name"
					+ " from dominant_taxon as dt " 
					+ " join taxon_rank as tr on tr.id = dt.rank_id " 
					+ " join taxonomy as tx on tx.id = dt.taxon_id " 
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and tr.name = '" + q0Rank + "' ";
			
			List<String> taxonList = new ArrayList<String>();
			ResultSet resultsA = statementA.executeQuery(sqlQueryA);
			while (resultsA.next()) {
				String tid = resultsA.getString("taxon_id");
				String name = resultsA.getString("taxon_name");
				taxonList.add(tid + "::" + name);
			}

			Statement statement0 = connection.createStatement();
			String sqlQuery0 = " select " + rank + "_id as taxon_id, t.name as taxon_name from microbiota "
					+ " join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and " + rank + "_id is not null group by " + rank 
					+ "_id, t.name order by sum(read_num) desc ";
			
			ResultSet results0 = statement0.executeQuery(sqlQuery0);
			final Map<String, Integer> taxonOrderMap = new HashMap<String, Integer>();
			int order = 1;
			while (results0.next()) {
				String tid = results0.getString("taxon_id");
				String name = results0.getString("taxon_name");
				String taxonString = tid + "::" + name;
				taxonOrderMap.put(taxonString, Integer.valueOf(order));
				order++;
				// though list is inefficient doing contains, but the size is less than 10, probably fine 
				if (taxonList.size() < minimalDisplayNumber && !taxonList.contains(taxonString)) {
					taxonList.add(taxonString);
				}
			}
			
			Collections.sort(taxonList, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return taxonOrderMap.get(o1).compareTo(taxonOrderMap.get(o2));
				}
			});
			
			
			List<String> selectedcolumns = new ArrayList<String>();
			List<String> rankIdList = new ArrayList<String>();
			for (String taxonString: taxonList) {
				String[] split = taxonString.split("::");
				rankIdList.add(split[0]);
				selectedcolumns.add(split[1]);
			}

			String rankIdString = "'" + StringUtils.join(rankIdList, "','") + "'";
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, t.id as taxon_id, t.name as taxon_name, sum(read_pct) as all_reads "
					+ " from microbiota join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and t.id in (" + rankIdString + ") "
					+ " group by sample_id, t.id, t.name ";
			
			Map<String, Map<String, Double>> rows = new HashMap<String, Map<String,Double>>();
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				String name = results1.getString("taxon_name");
				double allReads = results1.getDouble("all_reads");
				if (rows.get(sid) == null) {
					rows.put(sid, new HashMap<String, Double>());
				}
				rows.get(sid).put(name, Double.valueOf(allReads));
			}
			
			connection.close();
			ds.close();
			// shiftX depend on the sample id length
			int maxSampleLength = getMaxItemLength(sampleIdList);
			String heatMap = createSvgHeatMap(rows, sampleIdList, selectedcolumns, 150 + maxSampleLength * 10, 0, 200,
					true);
			StringBuffer ret = new StringBuffer();
			
			int canvasHeight = 200 + sampleIdList.size() * 32;
			int canvasWidth = 200 + taxonList.size() * 32 + 80;
			ret.append(String.format("<svg height=\"%d\" width=\"%d\">\n", canvasHeight, canvasWidth));
			ret.append(String.format("\t<rect x=\"1\" y=\"1\" fill=\"white\" id=\"chart_body\" height=\"%d\" width=\"%d\" />\n", canvasHeight - 2, canvasWidth - 2));
			
			ret.append(heatMap);
			
			ret.append("</svg>\n");
			
			return new VisualizationtResult(ret.toString());
			
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
	
	@Override
	public VisualizationtResult getClusteredReadsHeatmap(Set<SampleEntry> selectedSamples, String rank, int distanceType,
			int linkageType, Map<Integer, DendrogramCache> cacheMap) {
		// suppose the minimal display number is 10
		int minimalDisplayNumber = 10;
		
		List<String> sampleIdList = getSortedSampleList(selectedSamples);
		String q0Rank = rank;
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";

			// retrieve taxonomy from dominant_taxon table
			Statement statementA = connection.createStatement();
			String sqlQueryA = " select distinct tx.id as taxon_id, tx.name as taxon_name"
					+ " from dominant_taxon as dt " 
					+ " join taxon_rank as tr on tr.id = dt.rank_id " 
					+ " join taxonomy as tx on tx.id = dt.taxon_id " 
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and tr.name = '" + q0Rank + "' ";
			
			List<String> taxonList = new ArrayList<String>();
			ResultSet resultsA = statementA.executeQuery(sqlQueryA);
			while (resultsA.next()) {
				String tid = resultsA.getString("taxon_id");
				String name = resultsA.getString("taxon_name");
				taxonList.add(tid + "::" + name);
			}

			Statement statement0 = connection.createStatement();
			String sqlQuery0 = " select " + rank + "_id as taxon_id, t.name as taxon_name from microbiota "
					+ " join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and " + rank + "_id is not null group by " + rank 
					+ "_id, t.name order by sum(read_num) desc ";
			
			ResultSet results0 = statement0.executeQuery(sqlQuery0);
			final Map<String, Integer> taxonOrderMap = new HashMap<String, Integer>();
			int order = 1;
			while (results0.next()) {
				String tid = results0.getString("taxon_id");
				String name = results0.getString("taxon_name");
				String taxonString = tid + "::" + name;
				taxonOrderMap.put(taxonString, Integer.valueOf(order));
				order++;
				// though list is inefficient doing contains, but the size is less than 10, probably fine 
				if (taxonList.size() < minimalDisplayNumber && !taxonList.contains(taxonString)) {
					taxonList.add(taxonString);
				}
			}
			
			Collections.sort(taxonList, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return taxonOrderMap.get(o1).compareTo(taxonOrderMap.get(o2));
				}
			});
			
			
			List<String> selectedcolumns = new ArrayList<String>();
			List<String> rankIdList = new ArrayList<String>();
			for (String taxonString: taxonList) {
				String[] split = taxonString.split("::");
				rankIdList.add(split[0]);
				selectedcolumns.add(split[1]);
			}

			String rankIdString = "'" + StringUtils.join(rankIdList, "','") + "'";
			Statement statement1 = connection.createStatement();
			String sqlQuery1 = " select sample_id, t.id as taxon_id, t.name as taxon_name, sum(read_pct) as all_reads "
					+ " from microbiota join taxonomy as t on t.id = " + rank + "_id "
					+ " where sample_id in (" + sampleIdString + ") "
					+ " and t.id in (" + rankIdString + ") "
					+ " group by sample_id, t.id, t.name ";
			
			Map<String, Map<String, Double>> rows = new HashMap<String, Map<String,Double>>();
			ResultSet results1 = statement1.executeQuery(sqlQuery1);
			while (results1.next()) {
				String sid = results1.getString("sample_id");
				String name = results1.getString("taxon_name");
				double allReads = results1.getDouble("all_reads");
				if (rows.get(sid) == null) {
					rows.put(sid, new HashMap<String, Double>());
				}
				rows.get(sid).put(name, Double.valueOf(allReads));
			}
			
			connection.close();
			ds.close();
			
			List<String> sequence;
			String svgDendrogram;
			int dendrogramWidth;
			int dendrogramHeight;
			if (cacheMap == null) {
				cacheMap = new HashMap<Integer, DendrogramCache>();
			}
			DendrogramCache cache = cacheMap.get(Integer.valueOf(distanceType * 10 + linkageType));
			if (cache == null) {
				Dendrogram dendrogram = sampleDistanceClustering(sampleIdList, distanceType, linkageTypes[linkageType]);
				
				sequence = dendrogram.getSequence();
				svgDendrogram = dendrogram.getScaleUpSvgImageContentAtLeft(0, 210, 1, 2, 130d, true);
				dendrogramWidth = dendrogram.getDendrogramWidth(130d);
				dendrogramHeight = dendrogram.getDendrogramHeight(2);
				
				cacheMap.put(Integer.valueOf(distanceType * 10 + linkageType), new DendrogramCache(sequence, svgDendrogram,
						dendrogramWidth, dendrogramHeight));
			} else {
				sequence = cache.getSequence();
				svgDendrogram = cache.getSvgDendrogram();
				dendrogramWidth = cache.getDendrogramWidth();
				dendrogramHeight = cache.getDendrogramHeight();
			}

			// add heat map
			// shiftX depend on the sample id length
			int maxSampleLength = getMaxItemLength(sampleIdList);
			String heatMap = createSvgHeatMap(rows, sequence, selectedcolumns, 150 + maxSampleLength * 10, 0, 200,
					false);
			
			StringBuffer ret = new StringBuffer();
			
			int canvasHeight = dendrogramHeight + 260;
			int canvasWidth = 200 + taxonList.size() * 32 + 80;
			ret.append(String.format("<svg height=\"%d\" width=\"%d\">\n", canvasHeight, canvasWidth));
			ret.append(String.format("\t<rect x=\"1\" y=\"1\" fill=\"white\" id=\"chart_body\" height=\"%d\" width=\"%d\" />\n", canvasHeight - 2, canvasWidth - 2));
			
			ret.append(heatMap);
			ret.append(svgDendrogram);
			
			ret.append("</svg>\n");

			VisualizationtResult visualizationtResult = new VisualizationtResult(ret.toString());
			visualizationtResult.setDendrogramMap(cacheMap);
			return visualizationtResult;
			
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
	private String createSvgHeatMap(Map<String, Map<String, Double>> microbiotaPctMap,
			List<String> sortedSampleIds, List<String> sortedTaxons, int shiftX, int shiftY,
			int topLabelHeight, boolean containLeftLabel) {
		int cellWidth = 32;
		int cellHeight = 32;
		int maxSampleLength = getMaxItemLength(sortedSampleIds);
		
		StringBuffer ret = new StringBuffer();
		ret.append("<g stroke-width=\"1\" stroke=\"none\" id=\"heatmapgrid\" class=\"heatmap\">\n");
		for (int i = 0; i < sortedSampleIds.size(); i++) {
			String sampleLabel = sortedSampleIds.get(i);
			
			for (int j = 0; j < sortedTaxons.size(); j++) {
				String taxonLabel = sortedTaxons.get(j);

				int x = shiftX + j * cellWidth;
				int y = shiftY + topLabelHeight + i * cellHeight;
				
				Double readPct = microbiotaPctMap.get(sampleLabel).get(taxonLabel);
				if (readPct == null) {
					readPct = Double.valueOf("0");
				}
				int baseColor = 224;
				int rValue = baseColor + (int) (readPct.doubleValue() / 100 * (255- baseColor));
				int gValue = baseColor - (int) (readPct.doubleValue() / 100 * baseColor);
				String color = String.format("rgb(%d,%d,%d)", rValue, gValue, gValue);
				
				ret.append(String.format("\t<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" class=\"%s\">\n", x, y,
						cellWidth - 1, cellHeight - 1, color, "className"));
				ret.append(String.format("\t<title>%s; %s (%.2f%%)</title>\n", sampleLabel, taxonLabel, readPct.doubleValue()));
				ret.append("</rect>\n");
			}
			if (containLeftLabel) {
				// sampleX depend on the sample id length
				int sampleX = shiftX - maxSampleLength * 10 - 5;
				ret.append(String
						.format("\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"16\" fill=\"black\" class=\"heatmapLabel\" id=\"rowlabel_%s\">%s</text>\n",
								sampleX, shiftY + topLabelHeight + i * cellHeight + 24,
								String.valueOf(i), sampleLabel));
			}
		}
		
		for (int j = 0; j < sortedTaxons.size(); j++) {
			String taxonLable = sortedTaxons.get(j);
			String taxonTitle = sortedTaxons.get(j);

			int x = shiftX + j * cellWidth + 24;
			int y = shiftY + topLabelHeight - 6;
			ret.append(String
					.format("\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"16\" fill=\"black\" class=\"heatmapLabel\" transform=\"rotate(270, %d, %d)\" id=\"collabel_%s\">%s<title>%s</title></text>\n",
							x, y, x, y, String.valueOf(j),
							taxonLable, taxonTitle));
		}
		ret.append("</g>\n");
		
		return ret.toString();
	}
	
	@SuppressWarnings("unused")
	private String createHeatmapVerticalLegend(int x, int y) {
		StringBuffer ret = new StringBuffer();
		ret.append("<g stroke-width=\"1\" stroke=\"none\" id=\"heatmaplegend\" class=\"heatmapLegend\">\n");
		ret.append("<defs>\n" 
				+ "\t<linearGradient id=\"grad_v\" x1=\"0%\" y1=\"0%\" x2=\"0%\" y2=\"100%\">\n"
				+ "\t\t<stop offset=\"0%\" style=\"stop-color:rgb(255,0,0);stop-opacity:1\" />\n"
				+ "\t\t<stop offset=\"100%\" style=\"stop-color:rgb(224,224,224);stop-opacity:1\" />\n"
				+ "\t</linearGradient>\n" 
				+ "</defs>\n");

		ret.append(String.format("\t<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" class=\"%s\">\n", x, y,
				32, 128, "url(#grad_v)", "className"));
		ret.append("\t<title>Heat Map legend</title>\n");
		ret.append("</rect>\n");
		ret.append(String
				.format("\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"16\" fill=\"black\">%s</text>\n",
						x + 32, y + 16, "100%"));
		ret.append(String
				.format("\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"16\" fill=\"black\">%s</text>\n",
						x + 32, y + 16 + 128, "0%"));
		ret.append("</g>\n");
		
		return ret.toString();
	}

	private String createHeatmapHorizontalLegend(int x, int y) {
		StringBuffer ret = new StringBuffer();
		ret.append("<g stroke-width=\"1\" stroke=\"none\" id=\"heatmaplegend\" class=\"heatmapLegend\">\n");
		ret.append("<defs>\n" 
				+ "\t<linearGradient id=\"grad_h\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"0%\">\n"
				+ "\t\t<stop offset=\"0%\" style=\"stop-color:rgb(224,224,224);stop-opacity:1\" />\n"
				+ "\t\t<stop offset=\"100%\" style=\"stop-color:rgb(255,0,0);stop-opacity:1\" />\n"
				+ "\t</linearGradient>\n" 
				+ "</defs>\n");
		
		ret.append(String.format("\t<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" class=\"%s\">\n", x + 32, y,
				128, 32, "url(#grad_h)", "className"));
		ret.append("\t<title>Heat Map legend</title>\n");
		ret.append("</rect>\n");

		//quartile lines
		ret.append(String.format(
				"\t<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" style=\"stroke:rgb(255,255,255);stroke-width:1;stroke-opacity:0.8;\" />\n",
				x + 64, y, x + 64, y + 32));
		ret.append(String.format(
				"\t<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" style=\"stroke:rgb(255,255,255);stroke-width:1;stroke-opacity:0.8;\" />\n",
				x + 96, y, x + 96, y + 32));
		ret.append(String.format(
				"\t<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" style=\"stroke:rgb(255,255,255);stroke-width:1;stroke-opacity:0.8;\" />\n",
				x + 128, y, x + 128, y + 32));
		
		// labels
		ret.append(String
				.format("\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"16\" fill=\"black\">%s</text>\n",
						x, y + 24, "0%"));
		ret.append(String
				.format("\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"16\" fill=\"black\">%s</text>\n",
						x + 168, y + 24, "100%"));
		ret.append("</g>\n");
		
		return ret.toString();
	}

	@Override
	public String getHeatmapLegend() {
		StringBuffer ret = new StringBuffer();
		ret.append(String.format("<svg height=\"%d\" width=\"%d\">\n", 32 + 2, 224 + 2));
		ret.append(String.format("\t<rect x=\"1\" y=\"1\" fill=\"white\" id=\"chart_body\" height=\"%d\" width=\"%d\" />\n", 32, 224));
		ret.append(createHeatmapHorizontalLegend(0, 0));
		ret.append("</svg>\n");
		return ret.toString();
	}
	
	private Map<String, Map<String, Double>> getSampleDistanceMatrix(List<String> sampleIdList, Integer distanceType) {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		Map<String, Map<String, Double>> matrix = new HashMap<String, Map<String,Double>>();
		try {
			connection = ds.getConnection();
			
			String sampleIdString = "'" + StringUtils.join(sampleIdList, "','") + "'";
			
			Statement statement3 = connection.createStatement();
			String sqlQuery3 = " select * from sample_distance " 
					+ " where sample_id_1 in (" + sampleIdString + ") "
					+ " and sample_id_2 in (" + sampleIdString + ") "
					+ " and distance_type_id = " + distanceType;
			
			ResultSet results3 = statement3.executeQuery(sqlQuery3);
			while (results3.next()) {
				String sid1 = results3.getString("sample_id_1");
				String sid2 = results3.getString("sample_id_2");
				Double dist = results3.getDouble("distance");
				if (matrix.get(sid1) == null) {
					matrix.put(sid1, new HashMap<String, Double>());
				}
				matrix.get(sid1).put(sid2, dist);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			if (ds != null) {
				ds.close();
			}
		}
		return matrix;
	}
	
	@Override
	public PcoaResult getPCoAResult(List<String> sampleIdList, Integer distanceType) {
		Map<String, Map<String, Double>> matrix = getSampleDistanceMatrix(sampleIdList, distanceType);
		
		List<String> sampleList = new ArrayList<>(matrix.keySet());
		Collections.sort(sampleList);
		
		int len = sampleList.size();
		double[][] dmat = new double[len][len];
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < len; j++) {
				if (i==j) {
					dmat[i][j] = 0;
				} else {
					dmat[i][j] = matrix.get(sampleList.get(i)).get(sampleList.get(j)); 
				}
			}
		}
		
		MDSTweak mds = new MDSTweak(dmat, dmat.length - 1);

		double[][] coordinates = mds.getCoordinates();
		Map<String, List<Double>> coordinateMap = new HashMap<String, List<Double>>();
		List<Double> xList = new ArrayList<Double>();
		List<Double> yList = new ArrayList<Double>();
		for (int i = 0; i < coordinates.length; i++) {
			Double xCoord = Double.valueOf(coordinates[i][0]);
			Double yCoord = Double.valueOf(coordinates[i][1]);
			coordinateMap.put(sampleList.get(i), Arrays.asList(xCoord, yCoord));
			xList.add(xCoord);
			yList.add(yCoord);
		}
		
		Collections.sort(xList);
		double minX = xList.get(0).doubleValue();
		double maxX = xList.get(xList.size() - 1).doubleValue();
		Collections.sort(yList);
		double minY = yList.get(0).doubleValue();
		double maxY = yList.get(yList.size() - 1).doubleValue();
		
		double[] proportion = mds.getProportion();
		double a1Pct = proportion[0] * 100d;
		double a2Pct = proportion[1] * 100d;

		double intervalX = maxX - minX;
		double intervalY = maxY - minY;
		
		double interval = intervalX > intervalY? intervalX: intervalY;
		double tickIntv = interval / 3;
		double scale = 100 / tickIntv;

		double shiftX = 0;
		int zeroX = 0;
		if (tickIntv *2 > maxX && minX + tickIntv * 2 > 0) {
			zeroX = 2;
		} else {
			while (minX + shiftX < 0) {
				shiftX += tickIntv;
				zeroX++;
			}
		}
		
		double shiftY = 0;
		int zeroY = 9;
		if (tickIntv *2 > maxY && minY + tickIntv * 2 > 0) {
			zeroY = 7;
		} else {
			while (minY + shiftY < 0) {
				shiftY += tickIntv;
				zeroY--;
			}
		}
		
		StringBuffer gridSb = new StringBuffer();
		// TODO draw grid
		String gridStyle = "style=\"stroke:rgb(0,0,0);stroke-width:0.5;stroke-opacity:0.6;\"";
		String zeroAxisStyle = "style=\"stroke:rgb(0,0,0);stroke-width:1.0;stroke-opacity:0.8;\"";
		
		int[][] gridLines = new int[][] { { 100, 100, 100, 500 }, { 200, 100, 200, 500 }, { 300, 100, 300, 500 },
				{ 400, 100, 400, 500 }, { 500, 100, 500, 500 }, { 100, 100, 500, 100 }, { 100, 200, 500, 200 },
				{ 100, 300, 500, 300 }, { 100, 400, 500, 400 }, { 100, 500, 500, 500 } };
		gridSb.append("<g stroke-width=\"1\" stroke=\"none\" class=\"grid\" id=\"grid\">\n");
		// vertical
		for (int i = 0; i < 5; i++) {
			String style = gridStyle;
			if (i == zeroX) {
				style = zeroAxisStyle;
			}
			gridSb.append(String.format("\t<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" %s />\n", gridLines[i][0], gridLines[i][1],
					gridLines[i][2], gridLines[i][3], style));
		}
		// horizon
		for (int i = 5; i < gridLines.length; i++) {
			String style = gridStyle;
			if (i == zeroY) {
				style = zeroAxisStyle;
			}
			gridSb.append(String.format("\t<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" %s />\n", gridLines[i][0], gridLines[i][1],
					gridLines[i][2], gridLines[i][3], style));
		}
		// zero text
		gridSb.append(String
				.format("\t<text x=\"%d\" y=\"%d\" style=\"text-anchor: middle; font-family: Arial;"
						+ " font-size: %d; fill: black; font-style: italic;\">%s</text>\n",
						(100 + 100 * zeroX), 520, 16, "0"));
		gridSb.append(String
				.format("\t<text x=\"%d\" y=\"%d\" style=\"text-anchor: middle; font-family: Arial;"
						+ " font-size: %d; fill: black; font-style: italic;\">%s</text>\n",
						80, (105 + 100 * (zeroY -5)), 16, "0"));
		// axis label
		gridSb.append(String
				.format("\t<text x=\"%d\" y=\"%d\" style=\"text-anchor: middle; font-family: Arial;"
						+ " font-size: %d; fill: black; font-style: italic;\">%s</text>\n",
						300, 560, 16, String.format("PCo1 (%.2f%%)", a1Pct)));
		gridSb.append(String
				.format("\t<text x=\"%d\" y=\"%d\" transform=\"rotate(270, %d, %d)\" style=\"text-anchor: middle;"
						+ " font-family: Arial; font-size: %d; fill: black; font-style: italic;\">%s</text>\n",
						40, 300, 40, 305, 16, String.format("PCo2 (%.2f%%)", a2Pct)));
		gridSb.append("</g>\n");
		
		return new PcoaResult(coordinateMap, gridSb.toString(), Double.valueOf(scale), Integer.valueOf(zeroX), Integer.valueOf(zeroY));
	}
	
	/**
	 * only 3 types: PARA_TYPE_CONTINUOUS, PARA_TYPE_UNRANKED_CATEGORY and PARA_TYPE_RANKED_CATEGORY
	 */
	@Override
	public List<String> getProfileNames(String lang) {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement0 = connection.createStatement();
			String sqlQuery0 = " select pi.id as id, pi.title as title " 
					+ " from parameter_info as pi "
					+ " join parameter_type as pt on pt.id = pi.type_id "
					+ " where pt.type_name in ('" 
					+ GutFloraConstant.PARA_TYPE_CONTINUOUS + "','" 
					+ GutFloraConstant.PARA_TYPE_UNRANKED_CATEGORY + "','"
					+ GutFloraConstant.PARA_TYPE_RANKED_CATEGORY + "') ";
			
			ResultSet results0 = statement0.executeQuery(sqlQuery0);
			List<String> profileNameList = new ArrayList<String>();
			while (results0.next()) {
				String name = results0.getString("title");
				profileNameList.add(name);
			}
			
			connection.close();
			ds.close();
			
			return profileNameList;
			
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

	@Override
	public String getPCoAScatterPlot(PcoaResult pcoaResult) {
		Map<String, List<Double>> coordinates = pcoaResult.getCoordinates();
		
		StringBuffer ret = new StringBuffer();
		int canvasHeight = 600;
		int canvasWidth = 600;
		ret.append(String.format("<svg height=\"%d\" width=\"%d\">\n", canvasHeight, canvasWidth));
		ret.append(String.format("\t<rect x=\"1\" y=\"1\" fill=\"white\" id=\"chart_body\" height=\"%d\" width=\"%d\" />\n", canvasHeight - 2, canvasWidth - 2));
		
		ret.append(pcoaResult.getpCoAPlotGrid());
		
		ret.append("<g stroke-width=\"1\" stroke=\"none\" class=\"points\" id=\"points\">\n");
		for (String sampleId: coordinates.keySet()) {
			List<Double> coord = coordinates.get(sampleId);
			int nX = (int) (pcoaResult.getZeroXposition() + coord.get(0) * pcoaResult.getScale());
			int nY = (int) (pcoaResult.getZeroYposition() - coord.get(1) * pcoaResult.getScale());
			ret.append(String.format("\t<circle cx=\"%d\" cy=\"%d\" r=\"3\" style=\"fill: blue; stroke: none;\" class=\"point\">\n", 
					nX, nY));
			ret.append(String.format("<title>%s (%.2f, %.2f)</title>", sampleId, coord.get(0), coord.get(1)));
			ret.append("\t</circle>\n");
		}
		ret.append("</g>\n");
		
		ret.append("</svg>\n");
		
		return ret.toString();
	}

	@Override
	public String getPCoAScatterPlot(PcoaResult pcoaResult, String profileName, String lang) {
		Map<String, List<Double>> coordinates = pcoaResult.getCoordinates();
		Set<String> sampleIds = coordinates.keySet();
		
		// query profiles
		boolean isContinous = false;
		Map<String, String> paraValueMap = new HashMap<String, String>();
		List<String> categoryList = new ArrayList<String>();
		Map<String, Integer> colorMap = new HashMap<String, Integer>();
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			String sqlQuery0 = "select pi.id as id, pi.unit as unit " + ", pt.type_name as type "
					+ " from parameter_info as pi " + " join parameter_type as pt on pt.id = pi.type_id " + " where "
					+ " pi.title = '" + profileName + "' ";
			ResultSet results = statement.executeQuery(sqlQuery0);
			System.out.println(sqlQuery0);
			if (results.next()) {
				isContinous = GutFloraConstant.PARA_TYPE_CONTINUOUS.equals(results.getString("type"));
			} 

			Statement statement2 = connection.createStatement();
			String sqlQuery2 = " select sample_id, parameter_value " + " from parameter_value "
					+ " join parameter_info as pi on pi.id = parameter_id " 
					+ " where sample_id in (" + ("'" + StringUtils.join(sampleIds, "','") + "'") + ") "
					+ " and pi.title = '" + profileName + "' ";
			
			Set<String> usedOptions = new HashSet<String>();
			ResultSet results2 = statement2.executeQuery(sqlQuery2);
			while (results2.next()) {
				String sid = results2.getString("sample_id");
				String value = results2.getString("parameter_value");
				paraValueMap.put(sid, value);
				if (!isContinous) {
					usedOptions.add(value);
				}
			}
			
			if (!isContinous) {
				categoryList.addAll(usedOptions);
				Collections.sort(categoryList);
				for (int i = 0; i < categoryList.size(); i++) {
					colorMap.put(categoryList.get(i), i);
				}
			}
			
			connection.close();
			ds.close();
			
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

		double interval = 0;
		double max = 0;
		double min = 0;
		if (isContinous) {
			List<Double> valueList = new ArrayList<Double>();
			for (String v : paraValueMap.values()) {
				valueList.add(Double.valueOf(v));
			}
			Collections.sort(valueList);
			// TODO find the median
			Statistics stat = new Statistics(valueList);
			double median = stat.median();
			double stdDev = stat.getStdDev();
			interval = stdDev * 4;
			min = median - stdDev * 2;
			max = median + stdDev * 2;
		}
		
		int legendHeight = 50; 
		if (!isContinous) {
			legendHeight = 18 * colorMap.size() + 20;
		}
		
		// draw scatter plot
		StringBuffer ret = new StringBuffer();
		int canvasHeight = 600 + legendHeight;
		int canvasWidth = 600;
		ret.append(String.format("<svg height=\"%d\" width=\"%d\">\n", canvasHeight, canvasWidth));
		ret.append(String.format("\t<rect x=\"1\" y=\"1\" fill=\"white\" id=\"chart_body\" height=\"%d\" width=\"%d\" />\n", canvasHeight - 2, canvasWidth - 2));
		
		ret.append(pcoaResult.getpCoAPlotGrid());
		
		ret.append("<g stroke-width=\"1\" stroke=\"none\" class=\"points\" id=\"points\">\n");
		for (String sampleId: coordinates.keySet()) {
			List<Double> coord = coordinates.get(sampleId);
			int nX = (int) (pcoaResult.getZeroXposition() + coord.get(0) * pcoaResult.getScale());
			int nY = (int) (pcoaResult.getZeroYposition() - coord.get(1) * pcoaResult.getScale());

			String color;
			String stroke = "none";
			String paraValue = paraValueMap.get(sampleId);
			if (paraValue == null) {
				// skip the sample, which contains no data..
				continue;
			}
			if (isContinous) {
				double doubleValue = Double.valueOf(paraValue).doubleValue();
				if (doubleValue < min) {
					doubleValue = min;
					stroke = "#666";
				} else if (doubleValue > max) {
					doubleValue = max;
					stroke = "#666";
				}
				int cValue = (int) ((doubleValue - min) / interval * 255);
				color = String.format("rgb(%d,%d,%d)", cValue, 255 - cValue, 0);
			} else {
				color = GutFloraConstant.BARCHART_COLOR.get(colorMap.get(paraValue).intValue());
			}
			
			ret.append(String.format("\t<circle cx=\"%d\" cy=\"%d\" r=\"3\" style=\"fill: %s; stroke: %s;\" class=\"point\">\n", 
					nX, nY, color, stroke));
			ret.append(String.format("<title>%s (%.2f, %.2f), %s</title>", sampleId, coord.get(0), coord.get(1), paraValue));
			ret.append("\t</circle>\n");
		}
		ret.append("</g>\n");
		
		// draw the legend
		ret.append("<g stroke-width=\"1\" stroke=\"none\" class=\"pcoaLegend\">\n");
		if (isContinous) {
			// gradient bar
			int shiftX = 50;
			int shiftY = 600;
			int cellWidth = 28;
			int cellHeight = 28;
			ret.append("<g stroke-width=\"1\" stroke=\"none\">\n");
			ret.append(String.format("<defs><linearGradient id=\"MyGradient\">"
					+ "<stop offset=\"5%%\"  stop-color=\"%s\"/><stop offset=\"95%%\" stop-color=\"%s\"/>"
					+ "</linearGradient></defs>", "#0f0", "#f00"));
			ret.append(String.format("<rect fill=\"url(#MyGradient)\" x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\"/>",
					shiftX + 100, shiftY, cellWidth * 5, cellHeight - 1));
			ret.append(String
					.format("\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"14\" fill=\"black\" text-anchor=\"end\">%s</text>\n",
							shiftX + 100 - 6, shiftY + 16, String.format("%.0f", min)));
			ret.append(String
					.format("\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"14\" fill=\"black\" text-anchor=\"start\">%s</text>\n",
							shiftX + 100 + cellWidth * 5 + 6, shiftY + 16, String.format("%.0f", max)));
		} else {
			int shiftX = 100;
			int shiftY = 600;
			for (int i = 0; i < categoryList.size(); i++) {
				ret.append(String.format("\t<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" />\n",
						shiftX + 0, shiftY + i * 18, 12, 12, GutFloraConstant.BARCHART_COLOR.get(i)));

				ret.append(String.format(
						"\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"14\" fill=\"black\">%s</text>\n",
						shiftX + 20, shiftY + i * 18 + 10, categoryList.get(i)));
			}
		}
		ret.append("</g>\n");
		
		ret.append("</svg>\n");
		
		return ret.toString();
	}

	@Override
	public String getPCoAScatterPlot(PcoaResult pcoaResult, String customTagString) {
		Map<String, List<Double>> coordinates = pcoaResult.getCoordinates();
		String[] lines = customTagString.split("\n");
		
		Map<String, String> sampleGroupMap = new HashMap<String, String>();
		Map<String, Integer> groupMap = new HashMap<String, Integer>();
		int idx = 1;
		for (int i = 0; i < lines.length; i++) {
			String string = lines[i].replaceAll("[,|;]", " ");
			String[] cols = string.split("\\s+", 2);
			if (cols.length < 2) {
				continue;
			}
			sampleGroupMap.put(cols[0], cols[1]);
			if (groupMap.get(cols[1]) == null) {
				groupMap.put(cols[1], Integer.valueOf(idx));
				idx++;
			}
		}
		
		int legendHeight = 18 * groupMap.size() + 30;
		
		// draw scatter plot
		StringBuffer ret = new StringBuffer();
		int canvasHeight = 600 + legendHeight;
		int canvasWidth = 600;
		ret.append(String.format("<svg height=\"%d\" width=\"%d\">\n", canvasHeight, canvasWidth));
		ret.append(String.format("\t<rect x=\"1\" y=\"1\" fill=\"white\" id=\"chart_body\" height=\"%d\" width=\"%d\" />\n", canvasHeight - 2, canvasWidth - 2));
		
		ret.append(pcoaResult.getpCoAPlotGrid());
		
		boolean containsUnknownPoint = false;
		ret.append("<g stroke-width=\"1\" stroke=\"none\" class=\"points\" id=\"points\">\n");
		for (String sampleId: coordinates.keySet()) {
			List<Double> coord = coordinates.get(sampleId);
			int nX = (int) (pcoaResult.getZeroXposition() + coord.get(0) * pcoaResult.getScale());
			int nY = (int) (pcoaResult.getZeroYposition() - coord.get(1) * pcoaResult.getScale());
			String tag = sampleGroupMap.get(sampleId);
			if (tag != null) {
				String color = GutFloraConstant.BARCHART_COLOR.get(groupMap.get(tag).intValue());
				ret.append(String.format("\t<circle cx=\"%d\" cy=\"%d\" r=\"5\" style=\"fill: %s; stroke: none;\" class=\"point\">\n", 
						nX, nY, color));
				ret.append(String.format("<title>%s (%.2f, %.2f), %s</title>", sampleId, coord.get(0), coord.get(1), tag));
			} else {
				ret.append(String.format("\t<circle cx=\"%d\" cy=\"%d\" r=\"5\" style=\"fill: %s; stroke: none;\" class=\"point\">\n", 
						nX, nY, GutFloraConstant.BARCHART_COLORLESS));
				ret.append(String.format("<title>%s (%.2f, %.2f), %s</title>", sampleId, coord.get(0), coord.get(1), "Unknown"));
				containsUnknownPoint = true;
			}
			ret.append("\t</circle>\n");
		}
		ret.append("</g>\n");
		
		// draw the legend
		ret.append("<g stroke-width=\"1\" stroke=\"none\" class=\"pcoaLegend\">\n");
		int shiftX = 100;
		int shiftY = 600;
		List<String> choiceValues = new ArrayList<String>(groupMap.keySet());
		Collections.sort(choiceValues);
		for (int i = 0; i < choiceValues.size(); i++) {
			Integer colName = groupMap.get(choiceValues.get(i));
			int colorIndex = colName.intValue();
			ret.append(String.format("\t<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" />\n",
					shiftX + 0, shiftY + i * 18, 12, 12, GutFloraConstant.BARCHART_COLOR.get(colorIndex)));
			
			ret.append(String.format(
					"\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"14\" fill=\"black\">%s</text>\n",
					shiftX + 20, shiftY + i * 18 + 10, choiceValues.get(i)));
		}
		if (containsUnknownPoint) {
			ret.append(String.format("\t<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" />\n",
					shiftX + 0, shiftY + choiceValues.size() * 18, 12, 12, GutFloraConstant.BARCHART_COLORLESS));
			
			ret.append(String.format(
					"\t<text x=\"%d\" y=\"%d\" font-family=\"Arial\" font-size=\"14\" fill=\"black\">%s</text>\n",
					shiftX + 20, shiftY + choiceValues.size() * 18 + 10, "Unknown"));
		}
		ret.append("</g>\n");

		ret.append("</svg>\n");
		
		return ret.toString();
	}
	
	private int getMaxItemLength(List<String> itemStringList) {
		int maxItemLength = 0;
		for (String head : itemStringList) {
			if (head.length() > maxItemLength) {
				maxItemLength = head.length();
			}
		}
		return maxItemLength;
	}

	@Override
	public List<String> getSampleDisplayColumn() {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			
			String sqlQuery = " SELECT position, parameter_id FROM sample_display_columns ORDER BY position ";
			
			ResultSet results = statement.executeQuery(sqlQuery);
			List<String> ret = new ArrayList<String>();
			while (results.next()) {
				String pid = results.getString("parameter_id");
				if (pid == null) {
					pid = GutFloraConstant.CHOICE_NOT_SELECTED;
				}
				ret.add(pid);
			}
			connection.close();
			ds.close();
			return ret;
			
		} catch (SQLException e) {
			e.printStackTrace();
			ds.close();
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

	@Override
	public Boolean setSampleDisplayColumn(int position, String parameterId) {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			
			String value;
			if (parameterId.equals(GutFloraConstant.CHOICE_NOT_SELECTED)) {
				value = null;
			} else {
				value = "'" + parameterId + "'";
			}
			
			String sqlQuery = String.format(
					" UPDATE sample_display_columns SET parameter_id = %s " + " WHERE position = %d ", value,
					position);
			
			statement.executeUpdate(sqlQuery);
			connection.close();
			ds.close();
			
			return Boolean.TRUE;
			
		} catch (SQLException e) {
			e.printStackTrace();
			ds.close();
		}
		
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ds.close();
		
		return Boolean.FALSE;
	}

	@Override
	public List<String> getAllParameterTypes() {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			
			String sqlQuery = " SELECT description FROM parameter_type ORDER BY id ";
			
			ResultSet results = statement.executeQuery(sqlQuery);
			List<String> ret = new ArrayList<String>();
			while (results.next()) {
				String desc = results.getString("description");
				ret.add(desc);
			}
			connection.close();
			ds.close();
			return ret;
			
		} catch (SQLException e) {
			e.printStackTrace();
			ds.close();
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

	@Override
	public Boolean setParameterType(String parameterId, Integer typeId) {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			
			String sqlQuery = String.format(
					" UPDATE parameter_info SET type_id = %d " + " WHERE id = '%s' ", typeId,
					parameterId);
			
			statement.executeUpdate(sqlQuery);
			connection.close();
			ds.close();
			
			return Boolean.TRUE;
			
		} catch (SQLException e) {
			e.printStackTrace();
			ds.close();
		}
		
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ds.close();
		
		return Boolean.FALSE;
	}

	@Override
	public Map<Integer, String> getAllDistanceTypes() {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			
			String sqlQuery = " select id, type_name from distance_type order by id ";
			
			ResultSet results = statement.executeQuery(sqlQuery);
			Map<Integer, String> ret = new HashMap<Integer, String>();
			while (results.next()) {
				int id = results.getInt("id");
				String name = results.getString("type_name");
				ret.put(Integer.valueOf(id), name);
			}
			connection.close();
			ds.close();
			return ret;
			
		} catch (SQLException e) {
			e.printStackTrace();
			ds.close();
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

	@Override
	public Boolean deleteAllContents() {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			
			String sqlTruncateSample = " TRUNCATE TABLE sample CASCADE ";
			statement.executeUpdate(sqlTruncateSample);
			String sqlTruncateTaxonomy = " TRUNCATE TABLE taxonomy CASCADE ";
			statement.executeUpdate(sqlTruncateTaxonomy);
			String sqlTruncateParaInfo = " TRUNCATE TABLE parameter_info CASCADE ";
			statement.executeUpdate(sqlTruncateParaInfo);
			
			PreparedStatement psSampleDisplayColumn = connection.prepareStatement(
					" INSERT INTO sample_display_columns (position) VALUES (?) ");
			for (int i = 1; i < 4; i++) {
				psSampleDisplayColumn.setInt(1, i);
				psSampleDisplayColumn.executeUpdate();
			}
			
			connection.close();
			ds.close();
			
			return Boolean.TRUE;
			
		} catch (SQLException e) {
			e.printStackTrace();
			ds.close();
		}
		
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ds.close();
		
		return Boolean.FALSE;
	}

	@Override
	public String getDatabaseSummary() {
		HikariDataSource ds = DataSourceLoader.getHikariDataSource();
		Connection connection = null;
		try {
			int sampleCount = 0;
			int mbSampleCount = 0;
			int pvSampleCount = 0;
			
			connection = ds.getConnection();
			
			Statement statement = connection.createStatement();
			
			String sampleCountQuery = " SELECT count(distinct(id)) AS count FROM sample ";
			ResultSet scResults = statement.executeQuery(sampleCountQuery);
			while (scResults.next()) {
				sampleCount = scResults.getInt("count");
			}
			
			String mbCountQuery = " SELECT count(distinct(sample_id)) AS count FROM microbiota ";
			ResultSet mcResults = statement.executeQuery(mbCountQuery);
			while (mcResults.next()) {
				mbSampleCount = mcResults.getInt("count");
			}
			
			String pvCountQuery = " select count(distinct(sample_id)) AS count from parameter_value ";
			ResultSet pvResults = statement.executeQuery(pvCountQuery);
			while (pvResults.next()) {
				pvSampleCount = pvResults.getInt("count");
			}
			
			String ret;
			if (sampleCount == 0) {
				ret = "There is no sample in the database.";
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append(String.format("There are %d samples in the database. ", sampleCount));
				if (mbSampleCount == 0) {
					sb.append("None of them has microbiome data. ");
				} else {
					sb.append(String.format("%d of them have microbiome data. ", mbSampleCount));
				}
				if (pvSampleCount == 0) {
					sb.append("None of them has phenotype parameters. ");
				} else {
					sb.append(String.format("%d of them have phenotype parameters.", pvSampleCount));
				}
				ret = sb.toString();
			}
			
			connection.close();
			ds.close();
			
			return ret;
			
		} catch (SQLException e) {
			e.printStackTrace();
			ds.close();
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
	
}
