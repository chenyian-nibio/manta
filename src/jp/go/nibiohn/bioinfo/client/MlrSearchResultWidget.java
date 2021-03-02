package jp.go.nibiohn.bioinfo.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.go.nibiohn.bioinfo.client.generic.ModifiedSimplePager;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.PairListData;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.SearchResultData;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.ScatterChart;

/**
 * For displaying multiple linear regression(MLR) results
 * 
 * @author chenyian
 *
 */
public class MlrSearchResultWidget extends BaseWidget {

	protected static final int PAGE_SIZE = 10;

	private SimplePanel visualPanel = new SimplePanel();

	private Set<SampleEntry> selectedSamples;
	
	private List<String> sortedSampleIds;
	private Label profileNameLabel = new Label("TO BE SELECTED");
	private SimplePanel corrValuePanel = new SimplePanel();
	private SimplePanel chartPanel = new SimplePanel();
	private DataTable chartDataTable;
	private ScatterChart chart;
	private List<String> profilesOriList = new ArrayList<String>();
	private Map<String, Double[]> allReadsMap = new HashMap<String, Double[]>();

	private List<String> currentColumns;
	
	// maybe become an argument?
	private Integer experimentMethod = GutFloraConstant.EXPERIMENT_METHOD_16S;
	
	public MlrSearchResultWidget(Set<SampleEntry> selectedSamples, SearchResultData searchResultData,
			List<String> currentColumns, String historyTag, String lang) {
		super("Search results (MLR)", lang + historyTag);
		
		this.selectedSamples = selectedSamples;
		this.currentColumns = currentColumns;
		this.currentLang = lang;
		
		HorizontalPanel thisWidget = new HorizontalPanel();
		
		thisWidget.add(createCorrelationTable(searchResultData.getCorreationList()));
		thisWidget.add(visualPanel);

		// start to construct plot panel
		sortedSampleIds = getSortedSampleList(selectedSamples); 
		service.getAllReadsPctList(selectedSamples, searchResultData.getRank(), currentColumns, experimentMethod, 
				new AsyncCallback<Map<String, Double[]>>() {
			
			@Override
			public void onSuccess(Map<String, Double[]> result) {
				allReadsMap = result;
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage(BaseWidget.SERVER_ERROR);
			}
		});
		
		HorizontalPanel topHp = new HorizontalPanel();
		
		DecoratorPanel regressionInfoDec = new DecoratorPanel();
		regressionInfoDec.setTitle("Multiple linear regression");
		regressionInfoDec.addStyleName("optionDec");
		VerticalPanel regressionInfoVp = new VerticalPanel();
		regressionInfoDec.add(regressionInfoVp);
		
		// TODO add a style form the profileNameLabel
		HorizontalPanel profileHp = new HorizontalPanel();
		profileHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		profileHp.add(new HTML("<b>Profile:</b> "));
		profileHp.add(profileNameLabel);
		profileNameLabel.addStyleName("mlrMicrobiotaList");
		
		FlowPanel readFp = new FlowPanel();
		readFp.setWidth("500px");
		readFp.add(new HTML("<b>Microbiota</b>: "));
		for (String string : currentColumns) {
			Label label = new Label(string);
			label.addStyleName("mlrMicrobiotaList");
			readFp.add(label);
		}
		regressionInfoVp.add(profileHp);
		regressionInfoVp.add(readFp);
		
		DecoratorPanel corrValueDec = new DecoratorPanel();
		corrValueDec.addStyleName("corrResultDec");
		HorizontalPanel selectHp = new HorizontalPanel();
		selectHp.setSpacing(12);
		VerticalPanel corrLablePanel = new VerticalPanel();
		corrLablePanel.setWidth("90px");
		corrLablePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		Label corrLabel = new HTML("Coefficient of<br/> determination");
		corrLabel.setStyleName("corrLabel");
		corrLablePanel.add(corrLabel);
		corrLablePanel.add(corrValuePanel);
		corrValuePanel.setWidget(new Label("-.--"));
		corrValuePanel.setStyleName("corrValue");
		selectHp.add(corrLablePanel);
		corrValueDec.add(selectHp);

		VerticalPanel vp = new VerticalPanel();
		topHp.add(regressionInfoDec);
		topHp.add(corrValueDec);
		vp.add(topHp);
		
		vp.add(chartPanel);
		
		VisualizationUtils.loadVisualizationApi(new Runnable() {
			public void run() {
				chartDataTable = getChartDataTable();
				chart = new ScatterChart(chartDataTable, createOptions("",""));
				chartPanel.setWidget(chart);
			}
		}, CoreChart.PACKAGE);

		visualPanel.add(vp);
		
		initWidget(thisWidget);
	}
	
	/**
	 * data table for the chart
	 * @param currentColumns 
	 * @return
	 */
	private DataTable getChartDataTable() {
		DataTable data = DataTable.create();
		data.addColumn(ColumnType.NUMBER, "Profile");
		for (int i = 0; i < currentColumns.size(); i++) {
			data.addColumn(ColumnType.NUMBER, currentColumns.get(i));
		}
		int sampleSize = selectedSamples.size();
		data.addRows(sampleSize);
		if (allReadsMap.size() > 0) {
			for (int i = 0; i < sortedSampleIds.size(); i++) {
				data.setValue(i, 0, profilesOriList.get(i));
				String sid = sortedSampleIds.get(i);
				Double[] reads = allReadsMap.get(sid);
				for (int j = 0; j < reads.length; j++) {
					Double value = reads[j];
					if (value == null) {
						value = Double.valueOf("0");
					}
					data.setValue(i, j + 1, value);
				}
			}
		}

		return data;
	}

	private Options createOptions(String hAxisTitle, String vAxisTitle) {
		Options options = Options.create();
		options.setWidth(640);
		options.setHeight(480);
		AxisOptions vAxis = AxisOptions.create();
		vAxis.setTitle(vAxisTitle);
		options.setVAxisOptions(vAxis);
		AxisOptions hAxis = AxisOptions.create();
		hAxis.setTitle(hAxisTitle);
		options.setHAxisOptions(hAxis);
		return options;
	}

	private List<String> getSortedSampleList(Set<SampleEntry> selectedSamples) {
		List<String> sampleIdList = new ArrayList<String>();
		for (SampleEntry se : selectedSamples) {
			sampleIdList.add(se.getSampleId());
		}
		Collections.sort(sampleIdList);
		return sampleIdList;
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
		
		cellTable.addColumn(column, new SafeHtml() {
			private static final long serialVersionUID = 1037572553023203300L;

			@Override
			public String asString() {
				return "R<sup>2</sup>";
			}
		});
		
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
					corrValuePanel.setWidget(new Label(value.get(1)));
					final String profileName = value.get(0);
					profileNameLabel.setText(profileName);
					
					service.getProfilesList(selectedSamples, value.get(0), currentLang, 
							new AsyncCallback<PairListData>() {
						
						@Override
						public void onSuccess(PairListData result) {
							profilesOriList = result.getOriginalList();
							drawLineChart(profileName);
						}
						
						@Override
						public void onFailure(Throwable caught) {
							warnMessage(BaseWidget.SERVER_ERROR);
						}
					});

				}
			}
		});

		return vp;
	}
	
	private void drawLineChart(String profileName) {
		clearMessage();
		if (chart != null && allReadsMap.size() == profilesOriList.size()) {
			chart.draw(getChartDataTable(), createOptions(profileName, "Composition (%)"));
		}
	}


}
