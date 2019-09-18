package jp.go.nibiohn.bioinfo.client.analysis;

import java.util.Set;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;

import jp.go.nibiohn.bioinfo.client.BaseWidget;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.SearchResultData;

/**
 * Hold common features for analysis widgets
 * 
 * @author chenyian
 *
 */
public class AnalysisWidget extends BaseWidget {
	
	protected static final int PAGE_SIZE = 20;

	protected Set<SampleEntry> selectedSamples;

	protected ListBox rankListBox = new ListBox();

	protected SimplePanel analysisTabelPanel = new SimplePanel();

	// default is English
	protected String currentLang = "en_";
	
	protected SearchResultData searchResultData;

	public SearchResultData getSearchResultData() {
		return searchResultData;
	}
	
	public Set<SampleEntry> getSelectedSamples() {
		return selectedSamples;
	}
	
}
