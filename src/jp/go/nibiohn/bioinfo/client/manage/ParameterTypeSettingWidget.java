package jp.go.nibiohn.bioinfo.client.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import jp.go.nibiohn.bioinfo.client.generic.ModifiedSimplePager;
import jp.go.nibiohn.bioinfo.shared.ParameterEntry;

public class ParameterTypeSettingWidget extends ManageWidget {

	private static final int PAGE_SIZE = 16;

	private SimplePanel mainPanel = new SimplePanel();

	private CellTable<ParameterEntry> cellTable;
	
	private List<String> allTypes = new ArrayList<String>();
	
	private Map<String, Integer> typeMap = new HashMap<String, Integer>();

	public ParameterTypeSettingWidget() {
		VerticalPanel thisWidget = new VerticalPanel();
		
		service.getAllParameterTypes(new AsyncCallback<List<String>>() {
			
			@Override
			public void onSuccess(List<String> result) {
				allTypes = result;
				for (int i = 0; i < allTypes.size(); i++) {
					typeMap.put(allTypes.get(i), Integer.valueOf(i + 1));
				}
				loadParameterTable();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage("System error! Fail to Retrieve parameter types.");
			}
		});


		thisWidget.add(new HTML("<h3>Phenotypic parameter setting:</h3>"));
		thisWidget.add(mainPanel);
		initWidget(thisWidget);
		
	}
	
	private Widget createTableContent(List<ParameterEntry> parameters) {
		VerticalPanel vp = new VerticalPanel();
		cellTable = new CellTable<ParameterEntry>();
		cellTable.setWidth("640px");
		cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		
		final ListDataProvider<ParameterEntry> dataProvider = new ListDataProvider<ParameterEntry>(parameters);

		TextColumn<ParameterEntry> colColumnPosition = new TextColumn<ParameterEntry>() {

			@Override
			public String getValue(ParameterEntry object) {
				return object.getIdentifier();
			}

		};
		cellTable.addColumn(colColumnPosition, "Parameter name");
		
		SelectionCell categoryCell = new SelectionCell(allTypes);
		Column<ParameterEntry, String> categoryColumn = new Column<ParameterEntry, String>(categoryCell) {
			@Override
			public String getValue(ParameterEntry object) {
				return allTypes.get(object.getType().intValue() - 1);
			}
		};
		cellTable.addColumn(categoryColumn, "Parameter type");
		categoryColumn.setFieldUpdater(new FieldUpdater<ParameterEntry, String>() {
			@Override
			public void update(int index, ParameterEntry object, String value) {
				clearMessage();
//				GWT.log(object.getIdentifier() + " to " + typeMap.get(value));
				service.setParameterType(object.getIdentifier(), typeMap.get(value), new AsyncCallback<Boolean>() {
					
					@Override
					public void onSuccess(Boolean result) {
						if (result.booleanValue()) {
//							GWT.log("Update successfully.");
						} else {
							warnMessage("Update failed.");
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						warnMessage("System error!");
					}
				});
			}
		});
		cellTable.setColumnWidth(categoryColumn, 200, Unit.PX);

		cellTable.setRowCount(parameters.size(), true);
		cellTable.setPageSize(PAGE_SIZE);

		dataProvider.addDataDisplay(cellTable);

		vp.add(cellTable);

		SimplePager pager = new ModifiedSimplePager();
		pager.setDisplay(cellTable);
		pager.setPageSize(PAGE_SIZE);
		vp.add(pager);

		return vp;
	}

	private void loadParameterTable() {
		service.getAllParameterEntry(new AsyncCallback<List<ParameterEntry>>() {
			
			@Override
			public void onSuccess(List<ParameterEntry> result) {
				Widget table = createTableContent(result);
				
				mainPanel.setWidget(table);
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage("System error! Fail to load the parameters.");
			}
		});
	}
	
	@Override
	public void updateContents() {
		loadParameterTable();
	}
}
