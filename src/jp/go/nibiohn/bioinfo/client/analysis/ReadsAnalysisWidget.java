package jp.go.nibiohn.bioinfo.client.analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.go.nibiohn.bioinfo.client.GutFloraResources;
import jp.go.nibiohn.bioinfo.client.ItemSelectionWidget;
import jp.go.nibiohn.bioinfo.client.SampleInfoWidget;
import jp.go.nibiohn.bioinfo.client.generic.ModifiedSimplePager;
import jp.go.nibiohn.bioinfo.shared.GutFloraAnalysisData;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.SearchResultData;
import jp.go.nibiohn.bioinfo.shared.TaxonEntry;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;

/**
 * 
 * @author chenyian
 *
 */
public class ReadsAnalysisWidget extends AnalysisWidget {

	private GutFloraResources resources = GWT.create(GutFloraResources.class);
	
	private ListBox readListBox = new ListBox();

	// the second rank list box which will synchronize with the other one, just for convenience (suggested by Yayoi) 
	private ListBox rankListBox2 = new ListBox();

	private ListBox correlationListBox = new ListBox();
	
	private List<String> currentColumns = new ArrayList<String>();
	private List<String> availableColumns = new ArrayList<String>();

	private CheckBox showOthersCb = new CheckBox("Show 'Others' column");
	
	private Map<String, String> diversityMap = new HashMap<String, String>();
	
	private PopupPanel loadingPopupPanel = new PopupPanel();
	
	public ReadsAnalysisWidget(Set<SampleEntry> selectedSamples, String lang) {
		this(selectedSamples, "phylum", "", lang);
	}
	
	public ReadsAnalysisWidget(Set<SampleEntry> selectedSamples, String initRank, final String suffix, String lang) {
		showOthersCb.setValue(true);
		
		this.selectedSamples = selectedSamples;
		this.currentLang = lang;

		HorizontalPanel topHp = new HorizontalPanel();

		DecoratorPanel itemSelectionDec = new DecoratorPanel();
		itemSelectionDec.setTitle("Selection");
		HorizontalPanel itemSelectionHp = new HorizontalPanel();
		itemSelectionDec.addStyleName("optionDec");
		itemSelectionHp.setSpacing(12);
		itemSelectionHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		readListBox.addItem("");
		readListBox.setWidth("200px");
		
		itemSelectionHp.add(new Label("Search parameters correlated with:"));
		rankListBox2.setWidth("80px");
		itemSelectionHp.add(rankListBox2);
		itemSelectionHp.add(readListBox);
		
		itemSelectionHp.add(new Label(" using "));
		correlationListBox.addItem(GutFloraConstant.CORRELATION_SPEARMAN, GutFloraConstant.CORRELATION_SPEARMAN_VALUE.toString());
		correlationListBox.addItem(GutFloraConstant.CORRELATION_PEARSON, GutFloraConstant.CORRELATION_PEARSON_VALUE.toString());
		itemSelectionHp.add(correlationListBox);
		
		itemSelectionHp.add(new Button("Search", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				String rank = getSelectedRank();
				String taxonName = readListBox.getValue(readListBox.getSelectedIndex());
				if (taxonName == null || taxonName.equals("")) {
					warnMessage("Please select a taxon first.");
					return;
				} 
				loadingPopupPanel.show();
				if (taxonName.equals(GutFloraConstant.ALL_ABOVE_MICROBIOTA)) {
					// Do multiple linear regression
					service.searchForSimilerProfiles(ReadsAnalysisWidget.this.selectedSamples, rank, currentColumns,
							currentLang,
							new AsyncCallback<SearchResultData>() {
						
						@Override
						public void onSuccess(SearchResultData result) {
							searchResultData = result;
							History.newItem(currentLang + GutFloraConstant.NAVI_LINK_SEARCH + GutFloraConstant.NAVI_LINK_MLR
									+ GutFloraConstant.NAVI_LINK_SUFFIX_READ + suffix);
							loadingPopupPanel.hide();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							warnMessage(SERVER_ERROR);
							loadingPopupPanel.hide();
						}
					});
					
				} else {
					service.searchForSimilerProfiles(ReadsAnalysisWidget.this.selectedSamples, rank, taxonName,
							Integer.valueOf(correlationListBox.getSelectedValue()), currentLang,
							new AsyncCallback<SearchResultData>() {
						
						@Override
						public void onSuccess(SearchResultData result) {
							searchResultData = result;
							History.newItem(currentLang + GutFloraConstant.NAVI_LINK_SEARCH
									+ GutFloraConstant.NAVI_LINK_SUFFIX_READ + suffix);
							loadingPopupPanel.hide();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							warnMessage(SERVER_ERROR);
							loadingPopupPanel.hide();
						}
					});
				}
			}
		}));

		itemSelectionDec.add(itemSelectionHp);

		VerticalPanel vp = new VerticalPanel();
		topHp.add(itemSelectionDec);
		vp.add(topHp);

		int index = 0;
		for (int k = 0; k < GutFloraConstant.RANK_LIST.size(); k++) {
			rankListBox.addItem(GutFloraConstant.RANK_LIST.get(k));
			rankListBox2.addItem(GutFloraConstant.RANK_LIST.get(k));
			if (GutFloraConstant.RANK_LIST.get(k).equals(initRank)) {
				index = k;
			}
		}
		rankListBox.setSelectedIndex(index);
		rankListBox2.setSelectedIndex(index);
		if (index == 0) {
			initRank = "kingdom";
		}

		rankListBox.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				String rank = getSelectedRank();
				rankListBox2.setSelectedIndex(rankListBox.getSelectedIndex());
				updateTable(rank);
			}
		});

		rankListBox2.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				int selectedIndex = rankListBox2.getSelectedIndex();
				rankListBox.setSelectedIndex(selectedIndex);
				updateTable(rankListBox2.getValue(selectedIndex));
			}
		});
		
		readListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				if (readListBox.getSelectedValue().equals(GutFloraConstant.ALL_ABOVE_MICROBIOTA)) {
					correlationListBox.clear();
					correlationListBox.addItem(GutFloraConstant.MULTIPLE_LINEAR_REGRESSION);
				} else if (correlationListBox.getItemCount() != 2) {
					correlationListBox.clear();
					correlationListBox.addItem(GutFloraConstant.CORRELATION_SPEARMAN, GutFloraConstant.CORRELATION_SPEARMAN_VALUE.toString());
					correlationListBox.addItem(GutFloraConstant.CORRELATION_PEARSON, GutFloraConstant.CORRELATION_PEARSON_VALUE.toString());
				}
			}
		});

		HorizontalPanel rankHp = new HorizontalPanel();
		rankHp.setSpacing(6);
		rankHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		// move the column management to here...
		Image changeColumnIcon = new Image(resources.getSettingIconImageResource());
		rankHp.add(changeColumnIcon);
		changeColumnIcon.addClickHandler(new ClickHandler() {

			private DialogBox dialogBox;

			@Override
			public void onClick(ClickEvent event) {
				if (currentColumns == null || availableColumns == null) {
					warnMessage("Unexpected error, please try again or contact us.");
					return;
				}

				String rank = getSelectedRank();

				dialogBox = createSelectColumnDialogBox(rank);
				dialogBox.setGlassEnabled(true);
				dialogBox.setAnimationEnabled(false);
				dialogBox.setAutoHideEnabled(true);

				dialogBox.center();
				dialogBox.show();
			}
		});
		changeColumnIcon.addStyleName("clickable");
		changeColumnIcon.setTitle("Column Management");
		rankHp.add(new HTML("&nbsp;&nbsp;"));

		
		rankHp.add(new Label("View at"));
		rankHp.add(rankListBox);
		rankListBox.setWidth("80px");
		rankHp.add(new Label("level"));
		vp.add(rankHp);
		
		rankHp.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;"));
		rankHp.add(new HTML("(counts / 10,000 reads)"));
		
		rankHp.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;"));
		Image barChartIcon = new Image(resources.getBarChartImageResource());
		rankHp.add(barChartIcon);
		barChartIcon.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(currentLang + GutFloraConstant.NAVI_LINK_VIEW_BARCHART + suffix);
			}
		});
		barChartIcon.addStyleName("clickable");
		Hyperlink barChartLink = new Hyperlink("Bar Chart", currentLang + GutFloraConstant.NAVI_LINK_VIEW_BARCHART + suffix);
		rankHp.add(barChartLink);
		barChartLink.addStyleName("fixlink");

		// heat map
		rankHp.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;"));
		Image heatMapIcon = new Image(resources.getHeatMapImageResource());
		rankHp.add(heatMapIcon);
		heatMapIcon.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(currentLang + GutFloraConstant.NAVI_LINK_VIEW_HEATMAP + suffix);
			}
		});
		heatMapIcon.addStyleName("clickable");
		Hyperlink heatMapLink = new Hyperlink("Heat Map", currentLang + GutFloraConstant.NAVI_LINK_VIEW_HEATMAP + suffix);
		rankHp.add(heatMapLink);
		heatMapLink.addStyleName("fixlink");

		// PCoA analysis
		rankHp.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;"));
		Image pcoaAnalysisIcon = new Image(resources.getPcoaImageResource());
		rankHp.add(pcoaAnalysisIcon);
		pcoaAnalysisIcon.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(currentLang + GutFloraConstant.NAVI_LINK_VIEW_PCOA + suffix);
			}
		});
		pcoaAnalysisIcon.addStyleName("clickable");
		Hyperlink pcoaAnalysisLink = new Hyperlink("PCoA Chart", currentLang + GutFloraConstant.NAVI_LINK_VIEW_PCOA + suffix);
		rankHp.add(pcoaAnalysisLink);
		pcoaAnalysisLink.addStyleName("fixlink");

		vp.add(analysisTabelPanel);

		initTable(initRank);

		// ajax loading ...
		loadingPopupPanel.setGlassEnabled(true);
		VerticalPanel loadingVp = new VerticalPanel();
		loadingVp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		Label loadingLabel = new Label("Please wait...");
		loadingPopupPanel.setStyleName("dataLoading");
		loadingVp.setStyleName("dataLoadingContainer");
		loadingLabel.setStyleName("dataLoadingLabel");
		loadingVp.add(loadingLabel);
		loadingPopupPanel.add(loadingVp);
		
		initWidget(vp);
	}

	private void initTable(final String initRank) {
		loadingPopupPanel.show();
		service.getSampleDiversity(selectedSamples, new AsyncCallback<Map<String,String>>() {
			
			@Override
			public void onSuccess(Map<String, String> result) {
				diversityMap = result;
				updateTable(initRank);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage(SERVER_ERROR);
				loadingPopupPanel.hide();
			}
		});
	}

	private void updateTable(String rank) {
		loadingPopupPanel.show();
		
		service.getReadsAnalysisData(selectedSamples, rank, new AsyncCallback<GutFloraAnalysisData>() {

			@Override
			public void onSuccess(GutFloraAnalysisData result) {
				Widget table = createTableContent(result);

				analysisTabelPanel.setWidget(table);

				readListBox.clear();
				readListBox.addItem("");
				for (String h : result.getReadsHeader()) {
					readListBox.addItem(h);
				}
				if (selectedSamples.size() > 15) {
					readListBox.addItem(GutFloraConstant.ALL_ABOVE_MICROBIOTA);
				}

				currentColumns = result.getReadsHeader();
				availableColumns = result.getAvailableReadsHeader();

				loadingPopupPanel.hide();
			}

			@Override
			public void onFailure(Throwable caught) {
				warnMessage(SERVER_ERROR);
				loadingPopupPanel.hide();
			}
		});
	}

	private Widget createTableContent(GutFloraAnalysisData data) {
		VerticalPanel vp = new VerticalPanel();

		final CellTable<String> cellTable = new CellTable<String>();

		cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		cellTable.setAutoHeaderRefreshDisabled(true);

		cellTable.setEmptyTableWidget(new Label("no data found"));

		final Map<String, Map<String, String>> rows = data.getRows();

		List<String> sampleIds = data.getSampleIds();

		ListDataProvider<String> dataProvider = new ListDataProvider<String>(sampleIds);

		ListHandler<String> sortHandler = new ListHandler<String>(dataProvider.getList()) {
			@Override
			public void onColumnSort(ColumnSortEvent event) {
				super.onColumnSort(event);
				cellTable.setPageStart(0);
			}
		};

		cellTable.addColumnSortHandler(sortHandler);

		TextColumn<String> colSampleId = new TextColumn<String>() {

			@Override
			public String getValue(String id) {
				return id;
			}

			@Override
			public String getCellStyleNames(Context context, String object) {
				return "buttonLabel spacer";
			}
		};
		TextHeader header = new TextHeader("Sample ID");
		header.setHeaderStyleNames("spacer");
		cellTable.addColumn(colSampleId, header);
		colSampleId.setSortable(true);
		sortHandler.setComparator(colSampleId, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		// add diversity columns
		for (int i = 0; i < 2; i++) {
			final Integer index = Integer.valueOf(i);
			TextColumn<String> diverColumn = new TextColumn<String>() {
				
				@Override
				public String getValue(String id) {
					String diverValue = diversityMap.get(id);
					if (diverValue != null) {
						return diverValue.split("\\|")[index.intValue()];
					} 
					return "-";
				}
				
			};
			cellTable.addColumn(diverColumn, GutFloraConstant.DIVERSITY_INDEX[i]);
			diverColumn.setSortable(true);
			sortHandler.setComparator(diverColumn, new Comparator<String>() {
				
				@Override
				public int compare(String o1, String o2) {
					return Double.valueOf(diversityMap.get(o1).split("\\|")[index.intValue()]).compareTo(Double.valueOf(diversityMap.get(o2).split("\\|")[index.intValue()]));
				}
			});
		}
		
		Column<String, ImageResource> chartImgCol = new Column<String, ImageResource>(new ImageResourceCell()) {
			@Override
			public ImageResource getValue(String entity) {
				return resources.getPieChartImageResource();
			}
		};
		cellTable.addColumn(chartImgCol);
		
		List<String> readsHeader = data.getReadsHeader();
		for (final String h : readsHeader) {
			
			TextColumn<String> column = new TextColumn<String>() {

				@Override
				public String getValue(String id) {
					String reads = rows.get(id).get(h);
					return reads == null ? "0" : reads;
				}

			};
			cellTable.addColumn(column, h);
			column.setSortable(true);
			sortHandler.setComparator(column, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					String r1 = rows.get(o1).get(h);
					r1 = r1 == null ? "0" : r1;
					String r2 = rows.get(o2).get(h);
					r2 = r2 == null ? "0" : r2;
					return Integer.valueOf(r1).compareTo(Integer.valueOf(r2));
				}
			});
		}
		
		// 'Others' column
		if (showOthersCb.getValue()) {
			TextColumn<String> column = new TextColumn<String>() {

				@Override
				public String getValue(String id) {
					String reads = rows.get(id).get(GutFloraConstant.COLUMN_HEADER_OTHERS);
					return reads == null ? "0" : reads;
				}

			};
			cellTable.addColumn(column, GutFloraConstant.COLUMN_HEADER_OTHERS);
			column.setSortable(true);
			sortHandler.setComparator(column, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					String r1 = rows.get(o1).get(GutFloraConstant.COLUMN_HEADER_OTHERS);
					r1 = r1 == null ? "0" : r1;
					String r2 = rows.get(o2).get(GutFloraConstant.COLUMN_HEADER_OTHERS);
					r2 = r2 == null ? "0" : r2;
					return Integer.valueOf(r1).compareTo(Integer.valueOf(r2));
				}
			});
		}


		cellTable.getColumnSortList().push(colSampleId);

		cellTable.setRowCount(sampleIds.size());
		cellTable.setPageSize(sampleIds.size());

		dataProvider.addDataDisplay(cellTable);

		cellTable.addStyleName("clickableTable");

		vp.add(cellTable);

		SimplePager pager = new ModifiedSimplePager();
		pager.setDisplay(cellTable);
		pager.setPageSize(PAGE_SIZE);
		vp.add(pager);

		cellTable.addCellPreviewHandler(new CellPreviewEvent.Handler<String>() {

			@Override
			public void onCellPreview(CellPreviewEvent<String> event) {
				boolean isClick = "click".equals(event.getNativeEvent().getType());
				if (isClick) {
					int column = event.getColumn();
					if (column == 0) {
						// initialize a dialog box
						DialogBox dialogBox = createSampleInfoDialogBox(event.getValue());
						dialogBox.setGlassEnabled(true);
						dialogBox.setAnimationEnabled(false);
						dialogBox.setAutoHideEnabled(true);

						int left = 10;
						if (Window.getClientWidth() > 610) {
							left = (Window.getClientWidth() - 600) / 2;
						}
						dialogBox.setPopupPosition(left, 70);
						dialogBox.show();
					} else if (column == 4) {
						DialogBox dialogBox = createReadsPieChart(event.getValue());
						dialogBox.setGlassEnabled(true);
						dialogBox.setAnimationEnabled(false);
						dialogBox.setAutoHideEnabled(true);

						dialogBox.center();
						dialogBox.show();

					} else if (column > 4) {
						int index = column - 5;
						if (index < currentColumns.size()) {
							String columnName = currentColumns.get(index);
							for (int i = 1; i < readListBox.getItemCount(); i++) {
								if (readListBox.getItemText(i).equals(columnName)) {
									readListBox.setSelectedIndex(i);
									break;
								}
							}
						}
					}
				}
			}
		});

		return vp;
	}

	private DialogBox createSelectColumnDialogBox(String rank) {
		// Create a dialog box and set the caption text
		final DialogBox dialogBox = new DialogBox();
		dialogBox.ensureDebugId("columnDialogBox");
		dialogBox.setText("Column managment");

		final ItemSelectionWidget columnWidget = new ItemSelectionWidget(availableColumns, currentColumns);
		// TODO put to a field
		columnWidget.setMaxiumSelectableItems(25);
		VerticalPanel dialogContents = new VerticalPanel();
		dialogContents.setSpacing(4);
		dialogBox.setWidget(dialogContents);
		dialogContents.add(columnWidget);
		
		dialogContents.add(showOthersCb);

		HorizontalPanel buttonHp = new HorizontalPanel();
		buttonHp.setSpacing(12);
		Button okButton = new Button("OK", new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				String rank = getSelectedRank();
				List<String> selectedColumns = columnWidget.getSelectedItems();
				updateTable(rank, selectedColumns);
			}
		});
		okButton.setWidth("80px");
		buttonHp.add(okButton);

		Button resetButton = new Button("Reset", new ClickHandler() {
			public void onClick(ClickEvent event) {
				columnWidget.resetSelectedColumns(GutFloraConstant.DEFAULT_NUM_OF_COLUMNS);
			}
		});
		resetButton.setWidth("80px");
		buttonHp.add(resetButton);
		
		// Add a close button at the bottom of the dialog
		Button closeButton = new Button("Close", new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});
		closeButton.setWidth("80px");
		buttonHp.add(closeButton);
		dialogContents.add(buttonHp);
		dialogContents.setCellHorizontalAlignment(buttonHp, HasHorizontalAlignment.ALIGN_CENTER);

		return dialogBox;
	}

	private void updateTable(String rank, List<String> selectedColumns) {
		loadingPopupPanel.show();

		service.getReadsAnalysisData(selectedSamples, rank, selectedColumns,
				new AsyncCallback<GutFloraAnalysisData>() {

			@Override
			public void onSuccess(GutFloraAnalysisData result) {
				Widget table = createTableContent(result);

				analysisTabelPanel.setWidget(table);

				readListBox.clear();
				readListBox.addItem("");
				for (String h : result.getReadsHeader()) {
					readListBox.addItem(h);
				}

				currentColumns = result.getReadsHeader();

				loadingPopupPanel.hide();
			}

			@Override
			public void onFailure(Throwable caught) {
				warnMessage(SERVER_ERROR);
				loadingPopupPanel.hide();
			}
		});
	}

	private DialogBox createSampleInfoDialogBox(String sampleId) {
		// Create a dialog box and set the caption text
		final DialogBox dialogBox = new DialogBox();
		dialogBox.ensureDebugId("sampleDialogBox");
		dialogBox.setText("Sample information");

		VerticalPanel dialogContents = new VerticalPanel();
		dialogContents.setSpacing(4);
		dialogBox.setWidget(dialogContents);

		SampleInfoWidget sampleInfoWidget = new SampleInfoWidget(sampleId, currentLang);
		dialogContents.add(sampleInfoWidget);

		// Add a close button at the bottom of the dialog
		Button closeButton = new Button("Close", new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});
		closeButton.setWidth("100px");
		dialogContents.add(closeButton);
		dialogContents.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_CENTER);

		return dialogBox;
	}

	private DialogBox createReadsPieChart(final String sampleId) {
		// Create a dialog box and set the caption text
		final DialogBox dialogBox = new DialogBox();
		dialogBox.ensureDebugId("sampleDialogBox");
		dialogBox.setText("Gut microbiota composition - Sample " + sampleId);

		VerticalPanel dialogContents = new VerticalPanel();
		dialogContents.setSpacing(4);
		dialogBox.setWidget(dialogContents);

		final SimplePanel taxonNaviPanel = new SimplePanel();
		dialogContents.add(taxonNaviPanel);
		
		final SimplePanel chartPanel = new SimplePanel();
		dialogContents.add(chartPanel);
		
		final String rank = getSelectedRank();
		service.getMicrobiota(sampleId, rank, new AsyncCallback<List<List<String>>>() {
			private List<TaxonEntry> taxonList = new ArrayList<TaxonEntry>();
			
			@Override
			public void onSuccess(final List<List<String>> result) {
				final PieChart pieChart = new PieChart(getChartDataTable(result), createOptions(rank));
				
				pieChart.addSelectHandler(new SelectHandler() {
					
					@Override
					public void onSelect(SelectEvent event) {
						JsArray<Selection> selections = pieChart.getSelections();
						Selection selection = selections.get(0);
						int row = selection.getRow();
						updatePieChart(sampleId, rank, result.get(row).get(2));
						
						taxonList.add(new TaxonEntry("[x]", "leader", "leader"));
						taxonList.add(new TaxonEntry(result.get(row).get(0), result.get(row).get(2), rank));
						setTaxonNaviBar();
					}
				});
				chartPanel.setWidget(pieChart);
				dialogBox.center();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage(SERVER_ERROR);
			}
			
			private void updatePieChart(final String sampleId, final String rank, String taxonId) {
				service.getSampleReads(sampleId, rank, taxonId, new AsyncCallback<List<List<String>>>() {

					@Override
					public void onSuccess(final List<List<String>> result) {
						final String childRank = GutFloraConstant.RANK_LIST.get(GutFloraConstant.RANK_MAP.get(rank).intValue());
						
						final PieChart pieChart = new PieChart(getChartDataTable(result), createOptions(childRank));
						
						if (!childRank.equals(GutFloraConstant.RANK_LIST.get(GutFloraConstant.RANK_LIST.size() - 1))) {
							pieChart.addSelectHandler(new SelectHandler() {
								
								@Override
								public void onSelect(SelectEvent event) {
									JsArray<Selection> selections = pieChart.getSelections();
									Selection selection = selections.get(0);
									int row = selection.getRow();
									updatePieChart(sampleId, childRank, result.get(row).get(2));
									
									taxonList.add(new TaxonEntry(result.get(row).get(0), result.get(row).get(2), childRank));
									setTaxonNaviBar();
								}
							});
						}
						
						chartPanel.setWidget(pieChart);
						dialogBox.center();
						
					}
					
					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						
					}

				});
			}

			private void setTaxonNaviBar() {
				HorizontalPanel naviBar = new HorizontalPanel();
				
				Iterator<TaxonEntry> iterator = taxonList.iterator();
				while (iterator.hasNext()) {
					final TaxonEntry ent = iterator.next();
					Label label = new Label(ent.getName());
					label.setStyleName("taxonNaviLink");
					naviBar.add(label);
					if (ent.getIdentifier().equals("leader")) {
						label.setTitle("Click to remove all filters");
						label.addClickHandler(new ClickHandler() {
							
							@Override
							public void onClick(ClickEvent event) {
								dialogBox.hide();
								
								DialogBox dialogBox = createReadsPieChart(sampleId);
								dialogBox.setGlassEnabled(true);
								dialogBox.setAnimationEnabled(false);
								dialogBox.setAutoHideEnabled(true);

								dialogBox.center();
								dialogBox.show();
							}
						});
						naviBar.add(new Label(": "));
						
					} else {
						label.setTitle(ent.getRank());
						label.addClickHandler(new ClickHandler() {
							
							@Override
							public void onClick(ClickEvent event) {
								updatePieChart(sampleId, ent.getRank(), ent.getIdentifier());
								
								List<TaxonEntry> newList = new ArrayList<TaxonEntry>();
								for (TaxonEntry te: taxonList) {
									newList.add(te);
									if (te.getRank().equals(ent.getRank())) {
										break;
									}
								}
								taxonList.clear();
								taxonList.addAll(newList);
								setTaxonNaviBar();

							}
						});
						if (iterator.hasNext()) {
							naviBar.add(new Label(">>"));
						}
						
					}
					
				}
				taxonNaviPanel.clear();
				taxonNaviPanel.add(naviBar);
				taxonNaviPanel.setVisible(true);
			}

		});
		
		// Add a close button at the bottom of the dialog
		Button closeButton = new Button("Close", new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});
		closeButton.setWidth("100px");
		dialogContents.add(closeButton);
		dialogContents.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_CENTER);

		return dialogBox;
	}
	
	private DataTable getChartDataTable(List<List<String>> result) {
		DataTable data = DataTable.create();
		data.addColumn(ColumnType.STRING, "Species");
		data.addColumn(ColumnType.NUMBER, "Reads"); // for display purpose
		data.addRows(result.size());
		
		for (int i = 0; i < result.size(); i++) {
			data.setValue(i, 0, result.get(i).get(0));
			data.setValue(i, 1, result.get(i).get(1));
		}

		return data;
	}

	private Options createOptions(String rank) {
		Options options = Options.create();
		options.setWidth(600);
		options.setHeight(400);
		options.setTitle("View at " + rank + " level");
		return options;
	}

	public String getSelectedRank() {
		return rankListBox.getValue(rankListBox.getSelectedIndex());
	}

	public List<String> getCurrentColumns() {
		return currentColumns;
	}
	
}
