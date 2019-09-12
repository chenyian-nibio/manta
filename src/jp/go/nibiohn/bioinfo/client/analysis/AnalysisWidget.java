package jp.go.nibiohn.bioinfo.client.analysis;

import java.util.Set;
import java.util.logging.Logger;

import jp.go.nibiohn.bioinfo.client.GutFloraService;
import jp.go.nibiohn.bioinfo.client.GutFloraServiceAsync;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.SearchResultData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Hold common features for analysis widgets
 * 
 * @author chenyian
 *
 */
public class AnalysisWidget extends Composite {
	
	protected static Logger rootLogger = Logger.getLogger("");

	protected static final int PAGE_SIZE = 20;

	protected final GutFloraServiceAsync service = GWT.create(GutFloraService.class);
	
	protected Set<SampleEntry> selectedSamples;

	protected RootPanel mesgPanel = RootPanel.get("mesgPanel");

	protected ListBox rankListBox = new ListBox();

	protected SimplePanel analysisTabelPanel = new SimplePanel();

	// default is English
	protected String currentLang = "en_";
	
	protected void warnMessage(String message) {
		Label label = (Label) ((HorizontalPanel) mesgPanel.getWidget(0)).getWidget(0);
		label.setText(message);
		mesgPanel.setStyleName("warnMessage");
		mesgPanel.setVisible(true);
	}

	protected void clearMessage() {
		mesgPanel.setVisible(false);
	}

	protected SearchResultData searchResultData;

	public SearchResultData getSearchResultData() {
		return searchResultData;
	}
	
	public Set<SampleEntry> getSelectedSamples() {
		return selectedSamples;
	}
	
}
