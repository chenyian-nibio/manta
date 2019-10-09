package jp.go.nibiohn.bioinfo.client.manage;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.TabPanel;

import jp.go.nibiohn.bioinfo.client.FlowableWidget;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;

public class DataManageWidget extends FlowableWidget {

	private TabPanel tabPanel;
	
	private ManageWidget[] widgetList = new ManageWidget[4];

	public DataManageWidget(String lang) {
		super("Data Management", lang + GutFloraConstant.NAVI_LINK_UPLOAD);
		tabPanel = loadTabPanel();
	    initWidget(tabPanel);
	}

	private TabPanel loadTabPanel() {
		final TabPanel tabPanel = new TabPanel();
		tabPanel.setSize("100%", "100%");
		// tab 1
		UploadDataWidget uploadDataWidget = new UploadDataWidget();
		widgetList[0] = uploadDataWidget;
		tabPanel.add(uploadDataWidget, "Data Upload", false);
		// tab 2
		DisplayColumnSettingWidget displaySetWidget = new DisplayColumnSettingWidget();
		widgetList[1] = displaySetWidget;
		tabPanel.add(displaySetWidget, "Display Column Setting", false);
		// tab 3
		ParameterTypeSettingWidget typeSetWidget = new ParameterTypeSettingWidget();
		widgetList[2] = typeSetWidget;
		tabPanel.add(typeSetWidget, "Parameter Setting", false);
		// tab 4
		DeleteAllContentsWidget deleteAllWidget = new DeleteAllContentsWidget();
		tabPanel.add(deleteAllWidget, "Danger zone", false);
		widgetList[3] = deleteAllWidget;
		
		tabPanel.selectTab(0);
		
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				Integer index = event.getSelectedItem();
				widgetList[index].updateContents();
			}
		});
		
		// the border around the tab panel is not very good looking
		tabPanel.addStyleName("noBorder");
		return tabPanel;
	}
	
}
