package jp.go.nibiohn.bioinfo.client.manage;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.TabPanel;

import jp.go.nibiohn.bioinfo.client.FlowableWidget;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;

public class DataManageWidget extends FlowableWidget {

	private TabPanel tabPanel;
	private DisplayColumnSettingWidget displaySetWidget;
	private ParameterTypeSettingWidget typeSetWidget;

	public DataManageWidget(String lang) {
		super("Data Management", lang + GutFloraConstant.NAVI_LINK_UPLOAD);
		tabPanel = loadTabPanel();
	    initWidget(tabPanel);
	}

	private TabPanel loadTabPanel() {
		final TabPanel tabPanel = new TabPanel();
		tabPanel.setSize("100%", "100%");
		tabPanel.add(new UploadDataWidget(), "Data Upload", false);
		displaySetWidget = new DisplayColumnSettingWidget();
		tabPanel.add(displaySetWidget, "Display Column Setting", false);
		typeSetWidget = new ParameterTypeSettingWidget();
		tabPanel.add(typeSetWidget, "Parameter Setting", false);
		tabPanel.add(new DeleteAllContentsWidget(), "Danger zone", false);
		tabPanel.selectTab(0);
		
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if (event.getSelectedItem().equals(1)) {
					displaySetWidget.loadSettingTable();
				} else if (event.getSelectedItem().equals(2)) {
					typeSetWidget.loadParameterTable();
				}
			}
		});
		
		// the border around the tab panel is not very good looking
		tabPanel.addStyleName("noBorder");
		return tabPanel;
	}
	
}
