package jp.go.nibiohn.bioinfo.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

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

	List<SampleEntry> getSampleEntryList();

	Set<SampleEntry> getSelectedSampleEntrySet(String sampleIdString);
	
	SampleEntry getSampleEntry(String sampleId);

	List<List<String>> getSampleProfile(String sampleId);

	List<List<String>> getMicrobiota(String sampleId, String rank);

	List<List<String>> getSampleReads(String sampleId, String rank, String taxonId);
	
	GutFloraAnalysisData getReadsAnalysisData(Set<SampleEntry> selectedSamples, String rank);

	VisualizationtResult getReadsBarChart(Set<SampleEntry> selectedSamples, String rank);
	
	VisualizationtResult getReadsBarChart(Set<SampleEntry> selectedSamples, String selectedRank, String parentRank, String parentTaxonId);
	
	VisualizationtResult getReadsClusteredBarChart(Set<SampleEntry> selectedSamples, String rank, int distanceType, int linkageType, Map<Integer, DendrogramCache> cacheMap);
	
	VisualizationtResult getReadsClusteredBarChart(Set<SampleEntry> selectedSamples, String selectedRank, String parentRank, String parentTaxonId, int distanceType, int linkageType, int numOfColumns, Map<Integer, DendrogramCache> cacheMap);
	
	GutFloraAnalysisData getReadsAnalysisData(Set<SampleEntry> selectedSamples, String rank, List<String> selectedcolumns);

	GutFloraAnalysisData getProfilesAnalysisData(Set<SampleEntry> selectedSamples);

	GutFloraAnalysisData getProfilesAnalysisData(Set<SampleEntry> selectedSamples, List<String> selectedcolumns);

	List<TaxonEntry> getAllTaxonEntries(Set<SampleEntry> selectedSamples, String rank);

	List<ParameterEntry> getAllNumericParameterEntry();
	
	List<ParameterEntry> getAllParameterEntry();
	
	PairListData getReadsAndPctListById(Set<SampleEntry> selectedSamples, String rank, String taxonId);
	
	PairListData getReadsAndPctList(Set<SampleEntry> selectedSamples, String rank, String taxonName);

	PairListData getProfilesList(Set<SampleEntry> selectedSamples, String name);

	PairListData getProfilesListById(Set<SampleEntry> selectedSamples, String paraId);
	
	String getCorrelationString(Integer correlationMethod, List<String> list1, List<String> list2);
	
	SearchResultData searchForSimilerProfiles(Set<SampleEntry> selectedSamples, String rank, String taxonName,
			Integer correlationMethod);

	SearchResultData searchForSimilerProfiles(Set<SampleEntry> selectedSamples, String rank, List<String> taxonNames);

	SearchResultData searchForSimilerProfilesbyProfile(Set<SampleEntry> selectedSamples, String profileName,
			String paraType, Integer correlationMethod);

	SearchResultData searchForSimilarReads(Set<SampleEntry> selectedSamples, String rank, String name,
			Integer correlationMethod);

	Map<String, Double[]> getAllReadsPctList(Set<SampleEntry> selectedSamples, String rank, List<String> taxonNames);
	
	Map<String, String> getSampleDiversity(Set<SampleEntry> selectedSamples);

	List<String> getSampleDiversity(String sampleId);
	
	VisualizationtResult getReadsHeatmap(Set<SampleEntry> selectedSamples, String rank);

	VisualizationtResult getClusteredReadsHeatmap(Set<SampleEntry> selectedSamples, String rank, int distanceType,
			int linkageType, Map<Integer, DendrogramCache> cacheMap);
	
	String getHeatmapLegend();
	
	PcoaResult getPCoAResult(List<String> sampleIdList, Integer distanceType);
	
	List<String> getProfileNames();

	String getPCoAScatterPlot(PcoaResult pcoaResult);
	
	String getPCoAScatterPlot(PcoaResult pcoaResult, String profileName);
	
	String getPCoAScatterPlotWithCustomTags(PcoaResult pcoaResult, String customTagString);
	
	List<String> getSampleDisplayColumn();
	
	Boolean setSampleDisplayColumn(int position, String parameterId); 

	List<String> getAllParameterTypes();
	
	Boolean setParameterType(String parameterId, Integer typeId); 

	Map<Integer, String> getAllDistanceTypes();
	
	Boolean deleteAllContents();
	
	String getDatabaseSummary();
}
