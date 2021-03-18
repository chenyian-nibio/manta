package jp.go.nibiohn.bioinfo.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import jp.go.nibiohn.bioinfo.shared.DbUser;
import jp.go.nibiohn.bioinfo.shared.DendrogramCache;
import jp.go.nibiohn.bioinfo.shared.GutFloraAnalysisData;
import jp.go.nibiohn.bioinfo.shared.PairListData;
import jp.go.nibiohn.bioinfo.shared.ParameterEntry;
import jp.go.nibiohn.bioinfo.shared.PcoaResult;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.SearchResultData;
import jp.go.nibiohn.bioinfo.shared.TaxonEntry;
import jp.go.nibiohn.bioinfo.shared.VisualizationtResult;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("analysis")
public interface GutFloraService extends RemoteService {

	List<SampleEntry> getSampleEntryList(String lang);

	Set<SampleEntry> getSelectedSampleEntrySet(String sampleIdString, String lang);
	
	SampleEntry getSampleEntry(String sampleId, String lang);

	List<List<String>> getSampleProfile(String sampleId, String categoryId, String lang);

	List<List<String>> getSampleProfile(String sampleId, String categoryId, String groupId, String lang);

	List<List<String>> getMicrobiota(String sampleId, String rank, Integer experimentMethod);

	List<List<String>> getSampleReads(String sampleId, String rank, String taxonId, Integer experimentMethod);

	GutFloraAnalysisData getReadsAnalysisData(Set<SampleEntry> selectedSamples, String rank, Integer experimentMethod);

	VisualizationtResult getReadsBarChart(Set<SampleEntry> selectedSamples, String rank, Integer experimentMethod);

	VisualizationtResult getReadsBarChart(Set<SampleEntry> selectedSamples, String selectedRank, String parentRank, String parentTaxonId, Integer experimentMethod);

	VisualizationtResult getReadsClusteredBarChart(Set<SampleEntry> selectedSamples, String rank, Integer experimentMethod, int distanceType, int linkageType, Map<Integer, DendrogramCache> cacheMap);

	VisualizationtResult getReadsClusteredBarChart(Set<SampleEntry> selectedSamples, String selectedRank, String parentRank, String parentTaxonId, Integer experimentMethod, int distanceType, int linkageType, int numOfColumns, Map<Integer, DendrogramCache> cacheMap);

	GutFloraAnalysisData getReadsAnalysisData(Set<SampleEntry> selectedSamples, String rank, Integer experimentMethod, List<String> selectedcolumns);

	GutFloraAnalysisData getProfilesAnalysisData(Set<SampleEntry> selectedSamples, String categoryId, String groupId, String lang);

	GutFloraAnalysisData getProfilesAnalysisData(Set<SampleEntry> selectedSamples, List<String> selectedcolumns, String lang);

	List<TaxonEntry> getAllTaxonEntries(Set<SampleEntry> selectedSamples, String rank, Integer experimentMethod);

	List<ParameterEntry> getAllNumericParameterEntry(String lang);

	List<ParameterEntry> getAllUnrankedCategoryParameterEntry(String lang);
	
	List<List<String>> getProfileGroups(String categoryId, String lang);

	List<List<String>> getAllParameterGroupNames(String lang);

	PairListData getReadsAndPctListById(Set<SampleEntry> selectedSamples, String rank, String taxonId, Integer experimentMethod);
	
	PairListData getReadsAndPctList(Set<SampleEntry> selectedSamples, String rank, String taxonName, Integer experimentMethod);

	PairListData getProfilesList(Set<SampleEntry> selectedSamples, String name, String lang);

	PairListData getProfilesList(List<String> sampleIdList, String name, String lang);

	PairListData getNumericParameterValueById(Set<SampleEntry> selectedSamples, String paraId);
	
	PairListData getStringParameterValueById(Set<SampleEntry> selectedSamples, String paraId);
	
	List<String> getCorrelationStringWithPvalue(Integer correlationMethod, List<String> list1, List<String> list2);
	
	String getFormattedPvalueForUnrankedCategoricalParameter(Map<String, List<String>> groupValue);
	
	SearchResultData searchForSimilerProfiles(Set<SampleEntry> selectedSamples, String rank, String taxonName, Integer experimentMethod,
			String paraType, Integer correlationMethod, String lang);

	SearchResultData searchForSimilerProfiles(Set<SampleEntry> selectedSamples, String rank, List<String> taxonNames, Integer experimentMethod,
			String paraType, String lang);

	SearchResultData searchForSimilerProfilesbyProfile(Set<SampleEntry> selectedSamples, String profileName,
			String paraType, Integer correlationMethod, String lang);

	SearchResultData searchForSimilarReads(Set<SampleEntry> selectedSamples, String rank, Integer experimentMethod, String name,
			Integer correlationMethod, String lang);

	Map<String, Double[]> getAllReadsPctList(Set<SampleEntry> selectedSamples, String rank, List<String> taxonNames, Integer experimentMethod);
	
	Map<String, String> getSampleDiversity(Set<SampleEntry> selectedSamples, Integer experimentMethod);

	List<String> getSampleDiversity(String sampleId, Integer experimentMethod);
	
	DbUser getCurrentUser();

	boolean loginUser(String username, String password);

	void logoutCurrentUser();

	VisualizationtResult getReadsHeatmap(Set<SampleEntry> selectedSamples, String rank, Integer experimentMethod);

	VisualizationtResult getClusteredReadsHeatmap(Set<SampleEntry> selectedSamples, String rank, Integer experimentMethod, int distanceType, int linkageType, Map<Integer, DendrogramCache> cacheMap);
	
	String getHeatmapLegend();
	
	PcoaResult getPCoAResult(List<String> sampleIdList, Integer experimentMethod, Integer distanceType);
	
	List<String> getProfileNames(String categoryId, String groupId, String lang);

	String getPCoAScatterPlot(PcoaResult pcoaResult);
	
	String getPCoAScatterPlot(PcoaResult pcoaResult, String profileName, String lang);
	
	String getPCoAScatterPlot(PcoaResult pcoaResult, String customTagString);

	boolean hasImmunologicalData();
	
	String plotBarChartWithErrorBars(Map<String, List<String>> groupValue, Map<String, String> choiceMap, String xAxisLabel, String lang);
}
