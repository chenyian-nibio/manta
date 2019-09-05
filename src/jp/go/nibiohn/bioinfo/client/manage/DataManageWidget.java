package jp.go.nibiohn.bioinfo.client.manage;

import java.io.IOException;
import java.util.Properties;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;

import jp.go.nibiohn.bioinfo.client.BaseWidget;
import jp.go.nibiohn.bioinfo.server.GutFloraServiceImpl;
import jp.go.nibiohn.bioinfo.shared.GutFloraConfig;
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
		// TODO create new widgets
		tabPanel.add(new UploadDataWidget("sample"), "sample", false);
		tabPanel.add(new UploadDataWidget("microbiome"), "microbiome", false);
		tabPanel.add(new UploadDataWidget("parameter"), "parameter", false);
		tabPanel.add(new UploadDataWidget("parameter_value"), "parameter_value", false);
		tabPanel.add(new Label("should be a widget"), "Advance", false);
		tabPanel.selectTab(0);
		
		// the border around the tab panel is not very good looking
		tabPanel.addStyleName("noBorder");
		return tabPanel;
	}
	
	private void readTableHeaders() {
//		Properties props = new Properties();
//		try {
//			props.load(GutFloraServiceImpl.class.getClassLoader().getResourceAsStream(GutFloraConfig.TABLE_HEADER_FILE));
//
//			props.getProperty("");
//			
//		} catch (IOException e) {
//			// TODO maybe just response a error message on the info panel?
//			throw new RuntimeException("Problem loading properties '" + GutFloraConfig.TABLE_HEADER_FILE + "'", e);
//		}
	}

}
