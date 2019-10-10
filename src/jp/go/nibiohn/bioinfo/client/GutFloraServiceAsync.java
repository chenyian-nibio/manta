package jp.go.nibiohn.bioinfo.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

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
 * The async counterpart of <code>GutFloraService</code>.
 */
public interface GutFloraServiceAsync {

	void getSampleEntryList(AsyncCallback<List<SampleEntry>> callback);

	void getSelectedSampleEntrySet(String sampleIdString, AsyncCallback<Set<SampleEntry>> callback);

	void getSampleEntry(String sampleId, AsyncCallback<SampleEntry> callback);
	
	void getSampleProfile(String sampleId, AsyncCallback<List<List<String>>> callback);
	
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
	
	void getProfilesAnalysisData(Set<SampleEntry> selectedSamples, AsyncCallback<GutFloraAnalysisData> callback);
	
	void getReadsAnalysisData(Set<SampleEntry> selectedSamples, String rank, List<String> selectedcolumns,
			AsyncCallback<GutFloraAnalysisData> callback);

	void getProfilesAnalysisData(Set<SampleEntry> selectedSamples, List<String> selectedcolumns,
			AsyncCallback<GutFloraAnalysisData> callback);

	void getAllTaxonEntries(Set<SampleEntry> selectedSamples, String rank, AsyncCallback<List<TaxonEntry>> callback);

	void getAllNumericParameterEntry(AsyncCallback<List<ParameterEntry>> callback);

	void getAllParameterEntry(AsyncCallback<List<ParameterEntry>> callback);

	void getReadsAndPctListById(Set<SampleEntry> selectedSamples, String rank, String taxonId,
			AsyncCallback<PairListData> callback);
	
	void getReadsAndPctList(Set<SampleEntry> selectedSamples, String rank, String taxonName,
			AsyncCallback<PairListData> callback);
	
	void getProfilesList(Set<SampleEntry> selectedSamples, String name, AsyncCallback<PairListData> callback);

	void getProfilesListById(Set<SampleEntry> selectedSamples, String paraId, AsyncCallback<PairListData> callback);

	void getCorrelationString(Integer correlationMethod, List<String> list1, List<String> list2,
			AsyncCallback<String> callback);
	
	void searchForSimilerProfiles(Set<SampleEntry> selectedSamples, String rank, String taxonName,
			Integer correlationMethod, AsyncCallback<SearchResultData> callback);

	// for multiple linear regression
	void searchForSimilerProfiles(Set<SampleEntry> selectedSamples, String rank, List<String> taxonNames,
			AsyncCallback<SearchResultData> callback);
	
	void searchForSimilerProfilesbyProfile(Set<SampleEntry> selectedSamples, String profileName, String paraType,
			Integer correlationMethod, AsyncCallback<SearchResultData> callback);

	void searchForSimilarReads(Set<SampleEntry> selectedSamples, String rank, String name, Integer correlationMethod,
			AsyncCallback<SearchResultData> callback);

	void getAllReadsPctList(Set<SampleEntry> selectedSamples, String rank, List<String> taxonNames,
			AsyncCallback<Map<String, Double[]>> callback);

	void getSampleDiversity(Set<SampleEntry> selectedSamples, AsyncCallback<Map<String, String>> callback);

	void getSampleDiversity(String sampleId, AsyncCallback<List<String>> callback);

	void getReadsHeatmap(Set<SampleEntry> selectedSamples, String rank, AsyncCallback<VisualizationtResult> callback);

	void getClusteredReadsHeatmap(Set<SampleEntry> selectedSamples, String rank, int distanceType, int linkageType,
			Map<Integer, DendrogramCache> cacheMap, AsyncCallback<VisualizationtResult> callback);
	
	void getHeatmapLegend(AsyncCallback<String> callback);

	void getPCoAResult(List<String> sampleIdList, Integer distanceType, AsyncCallback<PcoaResult> callback);

	void getProfileNames(AsyncCallback<List<String>> callback);

	void getPCoAScatterPlot(PcoaResult pcoaResult, AsyncCallback<String> callback);

	void getPCoAScatterPlot(PcoaResult pcoaResult, String profileName, AsyncCallback<String> callback);

	void getPCoAScatterPlotWithCustomTags(PcoaResult pcoaResult, String customTagString, AsyncCallback<String> callback);

	void getSampleDisplayColumn(AsyncCallback<List<String>> callback);

	void setSampleDisplayColumn(int position, String parameterId, AsyncCallback<Boolean> callback);

	void getAllParameterTypes(AsyncCallback<List<String>> callback);

	void setParameterType(String parameterId, Integer typeId, AsyncCallback<Boolean> callback);

	void getAllDistanceTypes(AsyncCallback<Map<Integer, String>> callback);

	void deleteAllContents(AsyncCallback<Boolean> callback);

	void getDatabaseSummary(AsyncCallback<String> callback);
}
