package jp.go.nibiohn.bioinfo.client.manage;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import jp.go.nibiohn.bioinfo.client.BaseWidget;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.ParameterEntry;

public class DisplayColumnManageWidget extends BaseWidget {

	private SimplePanel mainPanel = new SimplePanel();

	private CellTable<TableItem> cellTable;

	public DisplayColumnManageWidget() {
		VerticalPanel thisWidget = new VerticalPanel();
		
		service.getAllParameterEntry(new AsyncCallback<List<ParameterEntry>>() {
			
			@Override
			public void onSuccess(List<ParameterEntry> result) {
				final List<String> parameters = new ArrayList<String>();
				parameters.add(GutFloraConstant.CHOICE_NOT_SELECTED);
				for (ParameterEntry parameterEntry : result) {
					parameters.add(parameterEntry.getIdentifier());
				}
				service.getSampleDisplayColumn(new AsyncCallback<List<String>>() {
					
					@Override
					public void onSuccess(List<String> result) {
						Widget table = createTableContent(result, parameters);
						
						mainPanel.setWidget(table);
						
					}
					
					@Override
					public void onFailure(Throwable caught) {
						warnMessage("System error! Fail to load the display settings.");
					}
				});
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage("System error! Fail to Retrieve parameters.");
			}
		});


		thisWidget.add(new HTML("<h3>Sample list display column setting:</h3>"));
		thisWidget.add(mainPanel);
		initWidget(thisWidget);
		
	}
	

	private Widget createTableContent(List<String> result, List<String> parameters) {
		VerticalPanel vp = new VerticalPanel();
		cellTable = new CellTable<TableItem>();
		cellTable.setWidth("640px");
		cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		
		List<TableItem> contents = new ArrayList<TableItem>();
		for (int i = 0; i < result.size(); i++) {
			contents.add(new TableItem(Integer.valueOf(i), result.get(i)));
		}

		final ListDataProvider<TableItem> dataProvider = new ListDataProvider<TableItem>(contents);

		TextColumn<TableItem> colColumnPosition = new TextColumn<TableItem>() {

			@Override
			public String getValue(TableItem object) {
				return object.getDisplayPosition();
			}

		};
		cellTable.addColumn(colColumnPosition, "Column Position");
		
		SelectionCell categoryCell = new SelectionCell(parameters);
		Column<TableItem, String> categoryColumn = new Column<TableItem, String>(categoryCell) {
			@Override
			public String getValue(TableItem object) {
				return object.getParaId();
			}
		};
		cellTable.addColumn(categoryColumn, "Displayed Parameter");
		categoryColumn.setFieldUpdater(new FieldUpdater<TableItem, String>() {
			@Override
			public void update(int index, TableItem object, String value) {
				clearMessage();
				service.setSampleDisplayColumn(index + 1, value, new AsyncCallback<Boolean>() {
					
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
		cellTable.setColumnWidth(categoryColumn, 160, Unit.PX);

		dataProvider.addDataDisplay(cellTable);

		vp.add(cellTable);
		
		return vp;
	}
	
	private class TableItem {
		private Integer position;
		private String paraId;
		public TableItem(Integer position, String paraId) {
			super();
			this.position = position;
			this.paraId = paraId;
		}
		public String getDisplayPosition() {
			return String.valueOf(position + 1);
		}
		public String getParaId() {
			return paraId;
		}
	}

}
