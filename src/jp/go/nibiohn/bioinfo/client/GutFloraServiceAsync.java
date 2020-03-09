package jp.go.nibiohn.bioinfo.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.go.nibiohn.bioinfo.shared.DendrogramCache;
import jp.go.nibiohn.bioinfo.shared.GutFloraAnalysisData;
import jp.go.nibiohn.bioinfo.shared.PairListData;
import jp.go.nibiohn.bioinfo.shared.ParameterEntry;
import jp.go.nibiohn.bioinfo.shared.PcoaResult;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.SearchResultData;
import jp.go.nibiohn.bioinfo.shared.TaxonEntry;
import jp.go.nibiohn.bioinfo.shared.VisualizationtResult;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GutFloraService</code>.
 */
public interface GutFloraServiceAsync {

	void getSampleEntryList(String lang, AsyncCallback<List<SampleEntry>> callback);

	void getSelectedSampleEntrySet(String sampleIdString, String lang, AsyncCallback<Set<SampleEntry>> callback);

	void getSampleEntry(String sampleId, String lang, AsyncCallback<SampleEntry> callback);
	
	void getSampleProfile(String sampleId, String categoryId, String lang, AsyncCallback<List<List<String>>> callback);

	void getSampleProfile(String sampleId, String categoryId, String groupId, String lang, AsyncCallback<List<List<String>>> callback);
	
	void getMicrobiota(String sampleId, String rank, AsyncCallback<List<List<String>>> callback);

	void getSampleReads(String sampleId, String rank, String taxonId, AsyncCallback<List<List<String>>> callback);
	
	void getReadsAnalysisData(Set<SampleEntry> selectedSamples, String rank, AsyncCallback<GutFloraAnalysisData> callback);

	void getReadsBarChart(Set<SampleEntry> selectedSamples, String rank, AsyncCallback<VisualizationtResult> callback);

	void getReadsBarChart(Set<SampleEntry> selectedSamples, String selectedRank, String parentRank,
			String parentTaxonId, AsyncCallback<VisualizationtResult> callback);
	
	// in-use by unifrac
	void getReadsClusteredBarChart(Set<SampleEntry> selectedSamples, String rank, int distanceType, int linkageType,
			Map<Integer, DendrogramCache> cacheMap, AsyncCallback<VisualizationtResult> callback);
	
	// in-use by unifrac
	void getReadsClusteredBarChart(Set<SampleEntry> selectedSamples, String selectedRank, String parentRank,
			String parentTaxonId, int distanceType, int linkageType, int numOfColumns, Map<Integer, DendrogramCache> cacheMap,
			AsyncCallback<VisualizationtResult> callback);

	void getProfilesAnalysisData(Set<SampleEntry> selectedSamples, String categoryId, String groupId, String lang,
			AsyncCallback<GutFloraAnalysisData> callback);
	
	void getReadsAnalysisData(Set<SampleEntry> selectedSamples, String rank, List<String> selectedcolumns,
			AsyncCallback<GutFloraAnalysisData> callback);

	void getProfilesAnalysisData(Set<SampleEntry> selectedSamples, List<String> selectedcolumns, String lang,
			AsyncCallback<GutFloraAnalysisData> callback);

	void getAllTaxonEntries(Set<SampleEntry> selectedSamples, String rank, AsyncCallback<List<TaxonEntry>> callback);

	void getAllNumericParameterEntry(String lang, AsyncCallback<List<ParameterEntry>> callback);

	void getProfileGroups(String categoryId, String lang, AsyncCallback<List<List<String>>> callback);

	void getDietFitnessGroupNames(String lang, AsyncCallback<List<List<String>>> callback);

	void getImmunologicalGroupNames(String lang, AsyncCallback<List<List<String>>> callback);

	void getProfileGroupNames(String lang, AsyncCallback<List<List<String>>> callback);

	void getAllParameterGroupNames(String lang, AsyncCallback<List<List<String>>> callback);
	
	void getReadsAndPctListById(Set<SampleEntry> selectedSamples, String rank, String taxonId,
			AsyncCallback<PairListData> callback);
	
	void getReadsAndPctList(Set<SampleEntry> selectedSamples, String rank, String taxonName,
			AsyncCallback<PairListData> callback);
	
	void getProfilesList(Set<SampleEntry> selectedSamples, String name, String lang, AsyncCallback<PairListData> callback);

	void getProfilesListById(Set<SampleEntry> selectedSamples, String paraId, AsyncCallback<PairListData> callback);

	void getCorrelationString(Integer correlationMethod, List<String> list1, List<String> list2,
			AsyncCallback<String> callback);
	
	void searchForSimilerProfiles(Set<SampleEntry> selectedSamples, String rank, String taxonName, String paraType,
			Integer correlationMethod, String lang, AsyncCallback<SearchResultData> callback);

	// for multiple linear regression
	void searchForSimilerProfiles(Set<SampleEntry> selectedSamples, String rank, List<String> taxonNames,
			String paraType, String lang, AsyncCallback<SearchResultData> callback);
	
	void searchForSimilerProfilesbyProfile(Set<SampleEntry> selectedSamples, String profileName, String paraType,
			Integer correlationMethod, String lang, AsyncCallback<SearchResultData> callback);

	void searchForSimilarReads(Set<SampleEntry> selectedSamples, String rank, String name, Integer correlationMethod,
			String lang, AsyncCallback<SearchResultData> callback);

	void getAllReadsPctList(Set<SampleEntry> selectedSamples, String rank, List<String> taxonNames,
			AsyncCallback<Map<String, Double[]>> callback);

	void getSampleDiversity(Set<SampleEntry> selectedSamples, AsyncCallback<Map<String, String>> callback);

	void getSampleDiversity(String sampleId, AsyncCallback<List<String>> callback);

	void getCurrentUser(AsyncCallback<String> callback);

	void createUser(String username, String password, String passwordConfirm, AsyncCallback<String> callback);

	void loginUser(String username, String password, AsyncCallback<String> callback);

	void logoutCurrentUser(AsyncCallback<Void> callback);

	void getReadsHeatmap(Set<SampleEntry> selectedSamples, String rank, AsyncCallback<VisualizationtResult> callback);

	void getClusteredReadsHeatmap(Set<SampleEntry> selectedSamples, String rank, int distanceType, int linkageType,
			Map<Integer, DendrogramCache> cacheMap, AsyncCallback<VisualizationtResult> callback);
	
	void getHeatmapLegend(AsyncCallback<String> callback);

	void getPCoAResult(List<String> sampleIdList, Integer distanceType, AsyncCallback<PcoaResult> callback);

	void getProfileNames(String categoryId, String groupId, String lang, AsyncCallback<List<String>> callback);

	void getPCoAScatterPlot(PcoaResult pcoaResult, AsyncCallback<String> callback);

	void getPCoAScatterPlot(PcoaResult pcoaResult, String profileName, String lang, AsyncCallback<String> callback);

	void getPCoAScatterPlot(PcoaResult pcoaResult, String customTagString, AsyncCallback<String> callback);

}
