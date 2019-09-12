package jp.go.nibiohn.bioinfo.client.manage;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;

import jp.go.nibiohn.bioinfo.client.BaseWidget;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;

public class DataManageWidget extends BaseWidget {

	private TabPanel tabPanel;

	public DataManageWidget(String lang) {
		super("Data Management", lang + GutFloraConstant.NAVI_LINK_UPLOAD);
		tabPanel = loadTabPanel();
	    initWidget(tabPanel);
	}

	private TabPanel loadTabPanel() {
		final TabPanel tabPanel = new TabPanel();
		tabPanel.setSize("100%", "100%");
		tabPanel.add(new UploadDataWidget(), "Data Upload", false);
		// TODO create a new widget for settings. 1. display columns, 2. delete all data button
		tabPanel.add(new Label("should be a widget"), "Settings", false);
		tabPanel.selectTab(0);
		
		// the border around the tab panel is not very good looking
		tabPanel.addStyleName("noBorder");
		return tabPanel;
	}
	
}
