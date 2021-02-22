package jp.go.nibiohn.bioinfo.client;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import jp.go.nibiohn.bioinfo.client.generic.ModifiedSimplePager;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.SearchResultData;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;

public class SearchResultWidget extends BaseWidget {

	protected static final int PAGE_SIZE = 10;

	private SimplePanel visualPanel = new SimplePanel();

	private VisualizeResultWidget vrWidget;
	
	public SearchResultWidget(Set<SampleEntry> selectedSamples, SearchResultData searchResultData, String historyTag,
			String lang) {
		super("Search results", historyTag);
		this.currentLang = lang;
		
		HorizontalPanel thisWidget = new HorizontalPanel();
		
		thisWidget.add(createCorrelationTable(searchResultData.getCorreationList()));
		thisWidget.add(visualPanel);
		
		vrWidget = new VisualizeResultWidget(selectedSamples, searchResultData.getRank(), searchResultData.getReferenceType(),
				searchResultData.getReferenceName(), searchResultData.getCorrelationMethod(), currentLang);
		visualPanel.add(vrWidget);
		
		initWidget(thisWidget);
	}

	private Widget createCorrelationTable(List<List<String>> searchResults) {
		
		VerticalPanel vp = new VerticalPanel();
		
		final CellTable<List<String>> cellTable = new CellTable<List<String>>();

		cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		cellTable.setAutoHeaderRefreshDisabled(true);
		
		cellTable.setEmptyTableWidget(new Label("no data found"));
		
		ListDataProvider<List<String>> dataProvider = new ListDataProvider<List<String>>(searchResults);
		
		ListHandler<List<String>> sortHandler = new ListHandler<List<String>>(dataProvider.getList()){
			@Override
			public void onColumnSort(ColumnSortEvent event) {
				super.onColumnSort(event);
				cellTable.setPageStart(0);
			}
		};
		
		cellTable.addColumnSortHandler(sortHandler);
		
		TextColumn<List<String>> nameColumn = new TextColumn<List<String>>() {
			
			@Override
			public String getValue(List<String> value) {
				return value.get(0);
			}
			
		};
		nameColumn.setCellStyleNames("nameColumn");
		cellTable.addColumn(nameColumn, "Name");
		
		TextColumn<List<String>> column = new TextColumn<List<String>>() {
			
			@Override
			public String getValue(List<String> value) {
				return value.get(1);
			}
			
		};
		cellTable.addColumn(column, "Corr.");
		column.setSortable(true);
		column.setDefaultSortAscending(false);
		sortHandler.setComparator(column, new Comparator<List<String>>() {
			
			@Override
			public int compare(List<String> o1, List<String> o2) {
				return Double.valueOf(o1.get(1)).compareTo(Double.valueOf(o2.get(1)));
			}
		});
		
		cellTable.getColumnSortList().push(column);

		cellTable.setRowCount(searchResults.size());
		cellTable.setPageSize(searchResults.size());
		
		dataProvider.addDataDisplay(cellTable);
		
		cellTable.addStyleName("clickableTable");
		
		vp.add(cellTable);

		SimplePager pager = new ModifiedSimplePager();
		pager.setDisplay(cellTable);
		pager.setPageSize(PAGE_SIZE);
		vp.add(pager);
		
		cellTable.addCellPreviewHandler(new CellPreviewEvent.Handler<List<String>>() {

			@Override
			public void onCellPreview(CellPreviewEvent<List<String>> event) {
				boolean isClick = "click".equals(event.getNativeEvent().getType());
				if (isClick) {
					int column = event.getColumn();
					if (column == 0) {
					}
					List<String> value = event.getValue();
					vrWidget.setCorrelationValue(value.get(1), value.get(2));
					vrWidget.setSelectNameAndUpdate(value.get(0));
				}
			}
		});

		return vp;
	}

}
