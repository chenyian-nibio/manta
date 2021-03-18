package jp.go.nibiohn.bioinfo.client;

import java.util.Set;

import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import jp.go.nibiohn.bioinfo.shared.DbUser;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.SearchResultData;

/**
 * 
 * @author chenyian
 *
 */
public class SampleAnalysisWidget extends BaseWidget {

	private Set<SampleEntry> selectedSamples;

	private TabPanel tabPanel;

	public SampleAnalysisWidget(Set<SampleEntry> selectedSamples, DbUser currentUser, String lang) {
		super("Analysis", lang + GutFloraConstant.NAVI_LINK_ANALYSIS);
		this.selectedSamples = selectedSamples;
		this.currentUser = currentUser;
		this.currentLang = lang;
		tabPanel = loadTabPanel(selectedSamples);
	    initWidget(tabPanel);
	}

	// for subset analysis
	public SampleAnalysisWidget(String name, String link, Set<SampleEntry> selectedSamples, String initRank, String lang) {
		super(name, lang + link);
		this.selectedSamples = selectedSamples;
		this.currentLang = lang;
		tabPanel = loadSubsetTabPanel(selectedSamples, initRank, GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX);
		initWidget(tabPanel);
	}
		
	private TabPanel loadTabPanel(Set<SampleEntry> selectedSamples) {
		final TabPanel tabPanel = new TabPanel();
		tabPanel.setSize("100%", "100%");
		if (currentUser.canSee16sData()) {
			tabPanel.add(new ReadsAnalysisWidget(selectedSamples, GutFloraConstant.EXPERIMENT_METHOD_16S, currentLang), GutFloraConstant.ANALYSIS_TAB_TITLES[0], false);
		}
		if (currentUser.canSeeShotgunData()) {
			tabPanel.add(new ReadsAnalysisWidget(selectedSamples, GutFloraConstant.EXPERIMENT_METHOD_SHOTGUN, currentLang), GutFloraConstant.ANALYSIS_TAB_TITLES[4], false);
		}
		tabPanel.add(new ProfilesAnalysisWidget(selectedSamples, currentUser.canSee16sData(), currentUser.canSeeShotgunData(), currentLang), GutFloraConstant.ANALYSIS_TAB_TITLES[1], false);
		tabPanel.add(new PairAnalysisWidget(selectedSamples, currentLang), GutFloraConstant.ANALYSIS_TAB_TITLES[2], false);
		tabPanel.add(new CategoricalAnalysisWidget(selectedSamples, currentLang), GutFloraConstant.ANALYSIS_TAB_TITLES[3], false);
		tabPanel.selectTab(0);
		
		// the border around the tab panel is not very good looking
		tabPanel.addStyleName("noBorder");
		return tabPanel;
	}

	private TabPanel loadSubsetTabPanel(Set<SampleEntry> selectedSamples, String initRank, String suffix) {
		final TabPanel tabPanel = new TabPanel();
		tabPanel.setSize("100%", "100%");
		if (currentUser.canSee16sData()) {
			tabPanel.add(new ReadsAnalysisWidget(selectedSamples, GutFloraConstant.EXPERIMENT_METHOD_16S, initRank, suffix, currentLang), GutFloraConstant.ANALYSIS_TAB_TITLES[0], false);
		}
		if (currentUser.canSeeShotgunData()) {
			tabPanel.add(new ReadsAnalysisWidget(selectedSamples, GutFloraConstant.EXPERIMENT_METHOD_SHOTGUN, initRank, suffix, currentLang), GutFloraConstant.ANALYSIS_TAB_TITLES[4], false);
		}
		tabPanel.add(new ProfilesAnalysisWidget(selectedSamples, currentUser.canSee16sData(), currentUser.canSeeShotgunData(), suffix, currentLang), GutFloraConstant.ANALYSIS_TAB_TITLES[1], false);
		tabPanel.add(new PairAnalysisWidget(selectedSamples, currentLang), GutFloraConstant.ANALYSIS_TAB_TITLES[2], false);
		tabPanel.selectTab(0);
		
		// the border around the tab panel is not very good looking
		tabPanel.addStyleName("noBorder");
		return tabPanel;
	}

	// TODO
	public SearchResultData getCorrectionResults(int tabIndex) {
		if (tabPanel != null) {
			Widget widget = tabPanel.getWidget(tabIndex);
			if (widget instanceof AnalysisWidget) {
				return ((AnalysisWidget) widget).getSearchResultData();
			}
		}
		return null;
	}
	
	public ReadsAnalysisWidget getReadsAnalysisWidgetByExpMethod(Integer expMethod) {
		if (tabPanel != null) {
			for (int i = 0; i < tabPanel.getWidgetCount(); i++) {
				Widget widget = tabPanel.getWidget(i);
				if (widget instanceof ReadsAnalysisWidget) {
					ReadsAnalysisWidget ret = (ReadsAnalysisWidget) widget;
					if (expMethod.equals(ret.getExperimentMethod())) {
						return ret;
					}
				}
			}
		}
		return null;
	}

	public ProfilesAnalysisWidget getProfilesAnalysisWidget() {
		if (tabPanel != null) {
			for (int i = 0; i < tabPanel.getWidgetCount(); i++) {
				Widget widget = tabPanel.getWidget(i);
				if (widget instanceof ProfilesAnalysisWidget) {
					ProfilesAnalysisWidget ret = (ProfilesAnalysisWidget) widget;
					return ret;
				}
			}
		}
		return null;
	}
	
	public Set<SampleEntry> getSelectedSamples() {
		return selectedSamples;
	}
	
}
