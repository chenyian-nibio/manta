package jp.go.nibiohn.bioinfo.client.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jp.go.nibiohn.bioinfo.client.generic.ModifiedSimplePager;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.PairListData;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
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
 * 
 * @author chenyian
 *
 */
public class VisualizeResultWidget extends AnalysisWidget {
	
	private Label readNameLabel = new Label("TO BE SELECTED");
	private Label profileNameLabel = new Label("TO BE SELECTED");
	
	private SimplePanel corrValuePanel = new SimplePanel();

	private SimplePanel chartPanel = new SimplePanel();
	private SimplePanel tablePanel = new SimplePanel();
	
	private List<String> readsOriList = new ArrayList<String>();
	private List<String> profilesOriList = new ArrayList<String>();
	
	private DataTable chartDataTable;

	private ScatterChart chart;

	private List<String> sortedSampleIds;
	
	private List<List<String>> pairDataList = new ArrayList<List<String>>();
	
	private String referenceType;

	private String rank;

	public VisualizeResultWidget(Set<SampleEntry> selectedSamples, 
			String rank, String referenceType, String referenceName, String correlationMethod) {
		this.selectedSamples = selectedSamples;
		this.referenceType = referenceType;
		this.rank = rank;
		
		sortedSampleIds = getSortedSampleList(selectedSamples); 
				
		pairDataList = new ArrayList<List<String>>();
		
		HorizontalPanel topHp = new HorizontalPanel();
		
		DecoratorPanel pairSelectionDec = new DecoratorPanel();
		pairSelectionDec.setTitle("Paired correlation");
		pairSelectionDec.addStyleName("optionDec");
		SimplePanel pairSelectionSp = new SimplePanel();
		pairSelectionSp.setWidth("500px");
		pairSelectionDec.add(pairSelectionSp);
		
		if (referenceType.equals(GutFloraConstant.NAVI_LINK_SUFFIX_READ)) {
			readNameLabel.setText(referenceName);
			service.getReadsAndPctList(VisualizeResultWidget.this.selectedSamples, rank, referenceName, new AsyncCallback<PairListData>() {
				
				@Override
				public void onSuccess(PairListData result) {
					readsOriList = result.getOriginalList();
					updateResults();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					warnMessage(SERVER_ERROR);
				}
			});
		} else if (referenceType.equals(GutFloraConstant.NAVI_LINK_SUFFIX_PROFILE)) {
			profileNameLabel.setText(referenceName);
			service.getProfilesList(VisualizeResultWidget.this.selectedSamples, referenceName,
					new AsyncCallback<PairListData>() {

						@Override
						public void onSuccess(PairListData result) {
							profilesOriList = result.getOriginalList();
							updateResults();
						}

						@Override
						public void onFailure(Throwable caught) {
							warnMessage(SERVER_ERROR);
						}
					});
		} else {
			// TODO unchecked! should never happen
		}

		VerticalPanel compareInfoVp = new VerticalPanel();
		HorizontalPanel methodHp = new HorizontalPanel();
		methodHp.setSpacing(6);
		methodHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		methodHp.add(new HTML("<b>Correlation Method:</b> "));
		methodHp.add(new Label(correlationMethod));
		compareInfoVp.add(methodHp);
		
		HorizontalPanel readHp = new HorizontalPanel();
		readHp.setSpacing(6);
		readHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		if (rank.equals(GutFloraConstant.ANALYSIS_TYPE_PROFILE)) {
			readHp.add(new HTML("<b style=\"white-space:nowrap;\">Parameter (X):</b> "));
		} else {
			readHp.add(new HTML("<b style=\"white-space:nowrap;\">Organism (X):</b> "));
			Label rankLabel = new Label(rank);
			readHp.add(rankLabel);
			rankLabel.addStyleName("pairCorrLabel");
		}
		readHp.add(readNameLabel);
		readNameLabel.addStyleName("pairCorrLabel");

		HorizontalPanel profileHp = new HorizontalPanel();
		profileHp.setSpacing(6);
		profileHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		profileHp.add(new HTML("<b style=\"white-space:nowrap;\">Parameter (Y):</b> "));
		profileHp.add(profileNameLabel);
		profileNameLabel.addStyleName("pairCorrLabel");

		compareInfoVp.add(readHp);
		compareInfoVp.add(profileHp);
		pairSelectionSp.add(compareInfoVp);
		
		DecoratorPanel corrValueDec = new DecoratorPanel();
		corrValueDec.addStyleName("corrResultDec");
		HorizontalPanel selectHp = new HorizontalPanel();
		selectHp.setSpacing(12);
		VerticalPanel corrLablePanel = new VerticalPanel();
		corrLablePanel.setWidth("90px");
		corrLablePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		Label corrLabel = new Label("Correlation");
		corrLabel.setStyleName("corrLabel");
		corrLablePanel.add(corrLabel);
		corrLablePanel.add(corrValuePanel);
		corrValuePanel.setWidget(new Label("-.--"));
		corrValuePanel.setStyleName("corrValue");
		selectHp.add(corrLablePanel);
		corrValueDec.add(selectHp);

		VerticalPanel vp = new VerticalPanel();
		topHp.add(pairSelectionDec);
		topHp.add(corrValueDec);
		vp.add(topHp);
		
		HorizontalPanel radioBtnHp = new HorizontalPanel();
		radioBtnHp.setSpacing(12);
		radioBtnHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		RadioButton radioButton1 = new RadioButton("chartTable","Scatter plot");
		RadioButton radioButton2 = new RadioButton("chartTable","Table");
		radioBtnHp.add(radioButton1);
		radioBtnHp.add(radioButton2);
		vp.add(radioBtnHp);
		
		vp.add(chartPanel);
		vp.add(tablePanel);
		
		radioButton1.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				chartPanel.setVisible(true);
				tablePanel.setVisible(false);
			}
		});
		radioButton2.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				chartPanel.setVisible(false);
				tablePanel.setVisible(true);
			}
		});
		radioButton1.setValue(true);
		tablePanel.setVisible(false);

		VisualizationUtils.loadVisualizationApi(new Runnable() {
			public void run() {
				chartDataTable = getChartDataTable();
				chart = new ScatterChart(chartDataTable, createOptions("",""));
				chartPanel.setWidget(chart);
			}
		}, CoreChart.PACKAGE);

		updateTable();
		
		initWidget(vp);
	}

	private void updateResults() {
		drawLineChart();
		updateTable();
	}
		
	private void drawLineChart() {
		clearMessage();
		if (chart != null && readsOriList.size() == profilesOriList.size()) {
			String parameter = profileNameLabel.getText();
			String taxonName = readNameLabel.getText();
			if (GutFloraConstant.RANK_LIST.contains(rank)) {
				taxonName += " (%)";
			}
			chart.draw(getChartDataTable(), createOptions(taxonName, parameter));
		}
	}
	
	private void updateTable() {
		pairDataList.clear();
		for (int i = 0; i < sortedSampleIds.size(); i++) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(sortedSampleIds.get(i));
			if (readsOriList.size() == sortedSampleIds.size()) {
				arrayList.add(readsOriList.get(i));
			} else {
				arrayList.add("-");
			}
			if (profilesOriList.size() == sortedSampleIds.size()) {
				arrayList.add(profilesOriList.get(i));
			} else {
				arrayList.add("-");
			}
			pairDataList.add(arrayList);
		}
		tablePanel.setWidget(createCellTable());
	}

	/**
	 * data table for the chart
	 * @return
	 */
	private DataTable getChartDataTable() {
		DataTable data = DataTable.create();
		data.addColumn(ColumnType.NUMBER, "Read");
		data.addColumn(ColumnType.NUMBER, "Sample"); // for display purpose
		int sampleSize = selectedSamples.size();
		data.addRows(sampleSize);
		
		if (readsOriList.size() > 0 && profilesOriList.size() > 0) {
			for (int i = 0; i < readsOriList.size(); i++) {
				data.setValue(i, 0, readsOriList.get(i));
				data.setValue(i, 1, profilesOriList.get(i));
			}
		}

		return data;
	}

	private Options createOptions(String hAxisTitle, String vAxisTitle) {
		Options options = Options.create();
		options.setWidth(480);
		options.setHeight(480);
		options.setLegend("none");
		AxisOptions vAxis = AxisOptions.create();
		vAxis.setTitle(vAxisTitle);
		options.setVAxisOptions(vAxis);
		AxisOptions hAxis = AxisOptions.create();
		hAxis.setTitle(hAxisTitle);
		options.setHAxisOptions(hAxis);
		return options;
	}

	private Widget createCellTable() {
		
		VerticalPanel vp = new VerticalPanel();
		
		final CellTable<List<String>> cellTable = new CellTable<List<String>>();

		cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		cellTable.setAutoHeaderRefreshDisabled(true);
		
		cellTable.setEmptyTableWidget(new Label("no data"));
		
		ListDataProvider<List<String>> dataProvider = new ListDataProvider<List<String>>(pairDataList);
		
		ListHandler<List<String>> sortHandler = new ListHandler<List<String>>(dataProvider.getList()){
			@Override
			public void onColumnSort(ColumnSortEvent event) {
				super.onColumnSort(event);
				cellTable.setPageStart(0);
			}
		};
		
		cellTable.addColumnSortHandler(sortHandler);
		
		TextColumn<List<String>> sampleIdColumn = new TextColumn<List<String>>() {
			
			@Override
			public String getValue(List<String> value) {
				return value.get(0);
			}
			
		};
		sampleIdColumn.setCellStyleNames("wideColumn");
		cellTable.addColumn(sampleIdColumn, "Sample");
		
		TextColumn<List<String>> column1 = new TextColumn<List<String>>() {
			
			@Override
			public String getValue(List<String> value) {
				return value.get(1);
			}
			
		};
		column1.setCellStyleNames("wideColumn");
//		String head1 = readListBox.getItemText(readListBox.getSelectedIndex()) + " (%)";
		String head1 = readNameLabel.getText() + " (%)";
		if (head1.equals("")) head1 = "Read";
		cellTable.addColumn(column1, head1);
		
		TextColumn<List<String>> column2 = new TextColumn<List<String>>() {
			
			@Override
			public String getValue(List<String> value) {
				return value.get(2);
			}
			
		};
		column2.setCellStyleNames("wideColumn");
		String head2 = profileNameLabel.getText();
		if (head2.equals("")) head2 = "Parameter";
		cellTable.addColumn(column2, head2);
		
		cellTable.setRowCount(pairDataList.size());
		cellTable.setPageSize(pairDataList.size());
		
		dataProvider.addDataDisplay(cellTable);
		
		vp.add(cellTable);

		SimplePager pager = new ModifiedSimplePager();
		pager.setDisplay(cellTable);
		pager.setPageSize(10);
		vp.add(pager);
		
		cellTable.addCellPreviewHandler(new CellPreviewEvent.Handler<List<String>>() {

			@Override
			public void onCellPreview(CellPreviewEvent<List<String>> event) {
				boolean isClick = "click".equals(event.getNativeEvent().getType());
				if (isClick) {
					int column = event.getColumn();
					if (column == 0) {
					}
				}
			}
		});

		return vp;
	}

	private List<String> getSortedSampleList(Set<SampleEntry> selectedSamples) {
		List<String> sampleIdList = new ArrayList<String>();
		for (SampleEntry se : selectedSamples) {
			sampleIdList.add(se.getSampleId());
		}
		Collections.sort(sampleIdList);
		return sampleIdList;
	}

	public void setCorrelationValue(String value) {
		corrValuePanel.setWidget(new Label(value));		
	}

	public void setSelectNameAndUpdate(String name) {
		if (referenceType.equals(GutFloraConstant.NAVI_LINK_SUFFIX_READ)) {
			profileNameLabel.setText(name);
			service.getProfilesList(VisualizeResultWidget.this.selectedSamples, name,
					new AsyncCallback<PairListData>() {

						@Override
						public void onSuccess(PairListData result) {
							profilesOriList = result.getOriginalList();
							updateResults();
						}

						@Override
						public void onFailure(Throwable caught) {
							warnMessage(SERVER_ERROR);
						}
					});
		} else if (referenceType.equals(GutFloraConstant.NAVI_LINK_SUFFIX_PROFILE)) {
			readNameLabel.setText(name);
			if (rank.equals("profile")) {
				service.getProfilesList(VisualizeResultWidget.this.selectedSamples, name,
						new AsyncCallback<PairListData>() {

							@Override
							public void onSuccess(PairListData result) {
								readsOriList = result.getOriginalList();
								updateResults();
							}

							@Override
							public void onFailure(Throwable caught) {
								warnMessage(SERVER_ERROR);
							}
						});
			} else {
				service.getReadsAndPctList(VisualizeResultWidget.this.selectedSamples, VisualizeResultWidget.this.rank,
						name, new AsyncCallback<PairListData>() {

							@Override
							public void onSuccess(PairListData result) {
								readsOriList = result.getOriginalList();
								updateResults();
							}

							@Override
							public void onFailure(Throwable caught) {
								warnMessage(SERVER_ERROR);
							}
						});
			}
		} else {
			// TODO unchecked! should never happen
		}
		
	}
}
