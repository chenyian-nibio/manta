package jp.go.nibiohn.bioinfo.shared;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GutFloraAnalysisData implements IsSerializable {

	private Map<String, Map<String, String>> rows;
	
	private List<String> sampleIds;
	// TODO should prevent use taxon name... maybe use id instead and also pass a map ...
	private List<String> readsHeader;
	private List<String> profilesHeader;
	private List<String> availableReadsHeader;
	private List<String> availableProfilesHeader;
	private Set<String> numericProfilesHeader;
	// in terms of the parameter, this is the map for the 'unit'
	private Map<String, String> metadataMap;;
	
	public GutFloraAnalysisData() {
	}

	public GutFloraAnalysisData(Map<String, Map<String, String>> rows, List<String> sampleIds) {
		super();
		this.rows = rows;
		this.sampleIds = sampleIds;
	}

	public Map<String, Map<String, String>> getRows() {
		return rows;
	}

	public List<String> getSampleIds() {
		return sampleIds;
	}

	public List<String> getReadsHeader() {
		return readsHeader;
	}

	public List<String> getProfilesHeader() {
		return profilesHeader;
	}

	public List<String> getAvailableReadsHeader() {
		return availableReadsHeader;
	}

	public List<String> getAvailableProfilesHeader() {
		return availableProfilesHeader;
	}
	
	public Set<String> getNumericProfilesHeader() {
		return numericProfilesHeader;
	}

	public void setReadsData(List<String> readsHeader, List<String> availableReadsHeader) {
		this.readsHeader = readsHeader;
		this.availableReadsHeader = availableReadsHeader;
	}

	public void setProfilesData(List<String> profilesHeader, List<String> availableProfilesHeader) {
		this.profilesHeader = profilesHeader;
		this.availableProfilesHeader = availableProfilesHeader;
	}

	public void setProfilesData(List<String> profilesHeader, List<String> availableProfilesHeader,
			Set<String> numericProfilesHeader) {
		this.profilesHeader = profilesHeader;
		this.availableProfilesHeader = availableProfilesHeader;
		this.numericProfilesHeader = numericProfilesHeader;
	}

	public Map<String, String> getMetadataMap() {
		return metadataMap;
	}

	public void setMetadataMap(Map<String, String> metadataMap) {
		this.metadataMap = metadataMap;
	}

	public void setRows(Map<String, Map<String, String>> rows) {
		this.rows = rows;
	}

	public void setSampleIds(List<String> sampleIds) {
		this.sampleIds = sampleIds;
	}

	public void setReadsHeader(List<String> readsHeader) {
		this.readsHeader = readsHeader;
	}

	public void setProfilesHeader(List<String> profilesHeader) {
		this.profilesHeader = profilesHeader;
	}

	public void setAvailableReadsHeader(List<String> availableReadsHeader) {
		this.availableReadsHeader = availableReadsHeader;
	}

	public void setAvailableProfilesHeader(List<String> availableProfilesHeader) {
		this.availableProfilesHeader = availableProfilesHeader;
	}
	
}
