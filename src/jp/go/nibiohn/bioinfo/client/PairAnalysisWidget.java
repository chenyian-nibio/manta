package jp.go.nibiohn.bioinfo.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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
import com.google.gwt.user.client.ui.ListBox;
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

import jp.go.nibiohn.bioinfo.client.generic.ModifiedSimplePager;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.PairListData;
import jp.go.nibiohn.bioinfo.shared.ParameterEntry;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.TaxonEntry;

/**
 * 
 * @author chenyian
 *
 */
public class PairAnalysisWidget extends AnalysisWidget {
	
	private static final String OPTION_PARAMETER = "Parameter";
	private ListBox rankListBoxX = new ListBox();
	private ListBox rankListBoxY = new ListBox();
	private ListBox paraListBoxX = new ListBox();
	private ListBox paraListBoxY = new ListBox();
	private ListBox corrTypeListBox = new ListBox();
	
	private SimplePanel corrValuePanel = new SimplePanel();
	private SimplePanel pValuePanel = new SimplePanel();

	private SimplePanel chartPanel = new SimplePanel();
	private SimplePanel tablePanel = new SimplePanel();
	
	private List<String> valueXList = new ArrayList<String>();
	private List<String> valueYList = new ArrayList<String>();
	
	private Map<String, String> parameterUnitMap = new HashMap<String, String>();
	
	private ScatterChart chart;

	private List<String> sortedSampleIds;
	
	private List<List<String>> pairDataList = new ArrayList<List<String>>();
	
	public PairAnalysisWidget(Set<SampleEntry> selectedSamples, String lang) {
		this.selectedSamples = selectedSamples;
		this.currentLang = lang;
		sortedSampleIds = getSortedSampleList(selectedSamples);
		
		HorizontalPanel topHp = new HorizontalPanel();
		DecoratorPanel pairSelectionDec = new DecoratorPanel();
		pairSelectionDec.setTitle("Analysis pair selection");
		pairSelectionDec.addStyleName("optionDec");
		SimplePanel pairSelectionSp = new SimplePanel();
		pairSelectionSp.setWidth("500px");
		pairSelectionDec.add(pairSelectionSp);
		
		VerticalPanel parameterVp = new VerticalPanel();
		HorizontalPanel pairSelectionHp0 = new HorizontalPanel();
		pairSelectionHp0.setSpacing(6);
		pairSelectionHp0.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		pairSelectionHp0.add(new HTML("<b>Correlation Method:</b> "));
		corrTypeListBox.addItem(GutFloraConstant.CORRELATION_SPEARMAN, GutFloraConstant.CORRELATION_SPEARMAN_VALUE.toString());
		corrTypeListBox.addItem(GutFloraConstant.CORRELATION_PEARSON, GutFloraConstant.CORRELATION_PEARSON_VALUE.toString());
		pairSelectionHp0.add(corrTypeListBox);
		
		HorizontalPanel pairSelectionHp1 = new HorizontalPanel();
		pairSelectionHp1.setSpacing(6);
		pairSelectionHp1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		HorizontalPanel pairSelectionHp2 = new HorizontalPanel();
		pairSelectionHp2.setSpacing(6);
		pairSelectionHp2.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		rankListBoxX.setWidth("100px");
		rankListBoxY.setWidth("100px");
		for (int k = 0; k < GutFloraConstant.RANK_LIST.size(); k++) {
			rankListBoxX.addItem(GutFloraConstant.RANK_LIST.get(k));
			rankListBoxY.addItem(GutFloraConstant.RANK_LIST.get(k));
		}
		rankListBoxX.addItem(OPTION_PARAMETER);
		rankListBoxY.addItem(OPTION_PARAMETER);
		rankListBoxX.setSelectedIndex(1);
		rankListBoxY.setSelectedIndex(GutFloraConstant.RANK_LIST.size());
		rankListBoxY.setEnabled(true);

		rankListBoxX.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				updateReadListBox();
			}
		});
		rankListBoxY.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				updateProfileListBox();
			}
		});

		paraListBoxX.addItem("");
		paraListBoxX.setWidth("300px");
		
		pairSelectionHp1.add(new Label("X:"));
		pairSelectionHp1.add(rankListBoxX);
		pairSelectionHp1.add(paraListBoxX);
		
		paraListBoxY.addItem("");
		paraListBoxY.setWidth("300px");
		
		pairSelectionHp2.add(new Label("Y:"));
		pairSelectionHp2.add(rankListBoxY);
		pairSelectionHp2.add(paraListBoxY);

		parameterVp.add(pairSelectionHp0);
		parameterVp.add(pairSelectionHp1);
		parameterVp.add(pairSelectionHp2);
		pairSelectionSp.add(parameterVp);
		
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

		// p-value
		corrLablePanel.add(pValuePanel);
		pValuePanel.setWidget(new Label(""));
		pValuePanel.setStyleName("pvalueLabel");
		
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
		
		updateReadListBox();
		updateProfileListBox();
		
		paraListBoxX.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				String rank = rankListBoxX.getSelectedItemText();
				if (rank.equals(OPTION_PARAMETER)) {
					String paraId = paraListBoxX.getSelectedValue();
					if (paraId != null && !paraId.equals("")) {
						service.getNumericParameterValueById(PairAnalysisWidget.this.selectedSamples, paraId, new AsyncCallback<PairListData>() {
							
							@Override
							public void onSuccess(PairListData result) {
								valueXList = result.getOriginalList();
								updateResults();
							}
							
							@Override
							public void onFailure(Throwable caught) {
								warnMessage(BaseWidget.SERVER_ERROR);
							}
						});
					}
				} else {
					String taxonId = paraListBoxX.getSelectedValue();
					if (taxonId != null && !taxonId.equals("")) {
						service.getReadsAndPctListById(PairAnalysisWidget.this.selectedSamples, rank, taxonId, new AsyncCallback<PairListData>() {
							
							@Override
							public void onSuccess(PairListData result) {
								valueXList = result.getOriginalList();
								updateResults();
							}
							
							@Override
							public void onFailure(Throwable caught) {
								warnMessage(BaseWidget.SERVER_ERROR);
							}
						});
					}
				}
			}
		});
		
		paraListBoxY.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				String rank = rankListBoxY.getSelectedItemText();
				if (rank.equals(OPTION_PARAMETER)) {
					String paraId = paraListBoxY.getSelectedValue();
					if (paraId != null && !paraId.equals("")) {
						service.getNumericParameterValueById(PairAnalysisWidget.this.selectedSamples, paraId, new AsyncCallback<PairListData>() {
							
							@Override
							public void onSuccess(PairListData result) {
								valueYList = result.getOriginalList();
								updateResults();
							}
							
							@Override
							public void onFailure(Throwable caught) {
								warnMessage(BaseWidget.SERVER_ERROR);
							}
						});
					}
				} else {
					String taxonId = paraListBoxY.getSelectedValue();
					if (taxonId != null && !taxonId.equals("")) {
						service.getReadsAndPctListById(PairAnalysisWidget.this.selectedSamples, rank, taxonId, new AsyncCallback<PairListData>() {
							
							@Override
							public void onSuccess(PairListData result) {
								valueYList = result.getOriginalList();
								updateResults();
							}
							
							@Override
							public void onFailure(Throwable caught) {
								warnMessage(BaseWidget.SERVER_ERROR);
							}
						});
					}
				}
			}
		});

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
		
		corrTypeListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				updateResults();
			}
		});
		
		VisualizationUtils.loadVisualizationApi(new Runnable() {
			public void run() {
				chart = new ScatterChart(getEmptyChartDataTable(), createOptions("Taxon",OPTION_PARAMETER));
				chartPanel.setWidget(chart);
			}
		}, CoreChart.PACKAGE);

		updateTable();
		
		initWidget(vp);
	}

	private void updateReadListBox() {
		String rank = rankListBoxX.getSelectedItemText();
		if (rank.equals(OPTION_PARAMETER)) {
			service.getAllNumericParameterEntry(currentLang, new AsyncCallback<List<ParameterEntry>>() {
				
				@Override
				public void onSuccess(List<ParameterEntry> result) {
					paraListBoxX.clear();
					paraListBoxX.addItem("");
					for (ParameterEntry ent : result) {
						paraListBoxX.addItem(ent.getName(), ent.getIdentifier());
						parameterUnitMap.put(ent.getIdentifier(), ent.getUnit());
					}
					// reset the contents
					valueXList = new ArrayList<String>();
					updateResults();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					warnMessage(BaseWidget.SERVER_ERROR);
				}
			});
			
		} else {
			service.getAllTaxonEntries(selectedSamples, rank, new AsyncCallback<List<TaxonEntry>>() {
				
				@Override
				public void onSuccess(List<TaxonEntry> result) {
					paraListBoxX.clear();
					paraListBoxX.addItem("");
					for (TaxonEntry taxon : result) {
						paraListBoxX.addItem(taxon.getName(), taxon.getIdentifier());
					}
					// reset the contents
					valueXList = new ArrayList<String>();
					updateResults();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					warnMessage(BaseWidget.SERVER_ERROR);
				}
			});
		}
	}

	private void updateProfileListBox() {
		String rank = rankListBoxY.getSelectedItemText();
		if (rank.equals(OPTION_PARAMETER)) {
			service.getAllNumericParameterEntry(currentLang, new AsyncCallback<List<ParameterEntry>>() {
				
				@Override
				public void onSuccess(List<ParameterEntry> result) {
					paraListBoxY.clear();
					paraListBoxY.addItem("");
					for (ParameterEntry ent : result) {
						paraListBoxY.addItem(ent.getName(), ent.getIdentifier());
						parameterUnitMap.put(ent.getIdentifier(), ent.getUnit());
					}
					// reset the contents
					valueYList = new ArrayList<String>();
					updateResults();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					warnMessage(BaseWidget.SERVER_ERROR);
				}
			});
			
		} else {
			service.getAllTaxonEntries(selectedSamples, rank, new AsyncCallback<List<TaxonEntry>>() {
				
				@Override
				public void onSuccess(List<TaxonEntry> result) {
					paraListBoxY.clear();
					paraListBoxY.addItem("");
					for (TaxonEntry taxon : result) {
						paraListBoxY.addItem(taxon.getName(), taxon.getIdentifier());
					}
					// reset the contents
					valueYList = new ArrayList<String>();
					updateResults();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					warnMessage(BaseWidget.SERVER_ERROR);
				}
			});
		}
	}

	private void updateResults() {
		drawLineChart();
		getCorrection();
		updateTable();
	}
		
	private void drawLineChart() {
		clearMessage();
		String head1 = paraListBoxX.getSelectedItemText();
		if (rankListBoxX.getSelectedItemText().equals(OPTION_PARAMETER)) {
			if (head1.equals("")) {
				head1 = OPTION_PARAMETER;
			} else {
				head1 = head1 + " (" + parameterUnitMap.get(paraListBoxX.getSelectedValue()) + ")";
			}
		} else {
			if (head1.equals("")) {
				head1 = "Taxon";
			} else {
				head1 = head1 + " (%)";
			}
		}
		String head2 = paraListBoxY.getSelectedItemText();
		if (rankListBoxY.getSelectedItemText().equals(OPTION_PARAMETER)) {
			if (head2.equals("")) {
				head2 = OPTION_PARAMETER;
			} else {
				head2 = head2 + " (" + parameterUnitMap.get(paraListBoxY.getSelectedValue()) + ")";
			}
		} else {
			if (head2.equals("")) {
				head2 = "Taxon";
			} else {
				head2 = head2 + " (%)";
			}
		}
		if (chart != null) {
			if (valueXList.size() == valueYList.size()) {
				chart.draw(getChartDataTable(), createOptions(head1, head2));
			} else {
				chart.draw(getEmptyChartDataTable(), createOptions(head1, head2));
			}
		}
	}
	
	private void getCorrection() {
		if (valueXList.size() != 0 && valueXList.size() == valueYList.size()) {
			service.getCorrelationStringWithPvalue(Integer.valueOf(corrTypeListBox.getSelectedValue()), valueXList,
					valueYList, new AsyncCallback<List<String>>() {
				
				@Override
				public void onSuccess(List<String> result) {
					corrValuePanel.setWidget(new Label(result.get(0)));
					pValuePanel.setWidget(new HTML(result.get(1)));
				}
				
				@Override
				public void onFailure(Throwable caught) {
					warnMessage(BaseWidget.SERVER_ERROR);
				}
			});
		}
	}

	private void updateTable() {
		pairDataList.clear();
		for (int i = 0; i < sortedSampleIds.size(); i++) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(sortedSampleIds.get(i));
			if (valueXList.size() == sortedSampleIds.size()) {
				arrayList.add(valueXList.get(i));
			} else {
				arrayList.add("-");
			}
			if (valueYList.size() == sortedSampleIds.size()) {
				arrayList.add(valueYList.get(i));
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
		
		for (int i = 0; i < valueXList.size(); i++) {
			data.setValue(i, 0, valueXList.get(i));
			data.setValue(i, 1, valueYList.get(i));
		}

		return data;
	}
	
	private DataTable getEmptyChartDataTable() {
		DataTable data = DataTable.create();
		data.addColumn(ColumnType.NUMBER, "Read");
		data.addColumn(ColumnType.NUMBER, "Sample"); // for display purpose
		data.addRows(0);
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
		String head1 = paraListBoxX.getSelectedItemText();
		if (rankListBoxX.getSelectedItemText().equals(OPTION_PARAMETER)) {
			if (head1.equals("")) {
				head1 = OPTION_PARAMETER;
			} else {
				head1 = head1 + " (" + parameterUnitMap.get(paraListBoxX.getSelectedValue()) + ")";
			}
		} else {
			if (head1.equals("")) {
				head1 = "Taxon";
			} else {
				head1 = head1 + " (%)";
			}
		}
		cellTable.addColumn(column1, head1);

		TextColumn<List<String>> column2 = new TextColumn<List<String>>() {
			
			@Override
			public String getValue(List<String> value) {
				return value.get(2);
			}
			
		};
		column2.setCellStyleNames("wideColumn");
		String head2 = paraListBoxY.getSelectedItemText();
		if (rankListBoxY.getSelectedItemText().equals(OPTION_PARAMETER)) {
			if (head2.equals("")) {
				head2 = OPTION_PARAMETER;
			} else {
				head2 = head2 + " (" + parameterUnitMap.get(paraListBoxY.getSelectedValue()) + ")";
			}
		} else {
			if (head2.equals("")) {
				head2 = "Taxon";
			} else {
				head2 = head2 + " (%)";
			}
		}
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

}
