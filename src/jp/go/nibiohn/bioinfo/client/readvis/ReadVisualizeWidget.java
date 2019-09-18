package jp.go.nibiohn.bioinfo.client.readvis;

import java.util.Set;

import jp.go.nibiohn.bioinfo.client.FlowableWidget;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;

public abstract class ReadVisualizeWidget extends FlowableWidget {

	protected Set<SampleEntry> selectedSamples;
	protected String subsetSampleIdString = "";

	public ReadVisualizeWidget(String name, String link) {
		super(name, link);
	}
	
	public String getSubsetSampleIdString() {
		return subsetSampleIdString;
	}

	public abstract String getSelectedRank();

	public Set<SampleEntry> getSelectedSamples() {
		return selectedSamples;
	}

}
