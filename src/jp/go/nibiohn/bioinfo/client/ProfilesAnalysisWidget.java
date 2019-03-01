package jp.go.nibiohn.bioinfo.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.go.nibiohn.bioinfo.client.generic.ModifiedSimplePager;
import jp.go.nibiohn.bioinfo.shared.GutFloraAnalysisData;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.SearchResultData;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
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
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;

/**
 * 
 * @author chenyian
 *
 */
public class ProfilesAnalysisWidget extends AnalysisWidget {

	private GutFloraResources resources = GWT.create(GutFloraResources.class);
	
	private ListBox profileListBox = new ListBox();

	private ListBox refTypeListBox = new ListBox();

	private ListBox profileGroupListBox = new ListBox();
	
	private ListBox correlationListBox = new ListBox();
	
	private List<String> currentColumns = new ArrayList<String>();
	private List<String> availableColumns = new ArrayList<String>();
	private Map<String, String> unitMap = new HashMap<String, String>();

	private PopupPanel loadingPopupPanel = new PopupPanel();
	
	public ProfilesAnalysisWidget(Set<SampleEntry> selectedSamples, String lang) {
		this(selectedSamples, "", lang);
	}
	
	public ProfilesAnalysisWidget(Set<SampleEntry> selectedSamples, final String suffix, String lang) {
		this.selectedSamples = selectedSamples;
		this.currentLang = lang;
		
		HorizontalPanel topHp = new HorizontalPanel();
		
		DecoratorPanel itemSelectionDec = new DecoratorPanel();
		itemSelectionDec.setTitle("Selection");
		HorizontalPanel itemSelectionHp = new HorizontalPanel();
		itemSelectionDec.addStyleName("optionDec");
		itemSelectionHp.setSpacing(12);
		itemSelectionHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		profileListBox.addItem("");
		profileListBox.setWidth("200px");
		
		// 1) Search other diet and fitness parameters correlated with:"
		// 2) Search gut microbiota compositions at the *** level, correlated with:".
		itemSelectionHp.add(new Label("Search"));
		itemSelectionHp.add(refTypeListBox);
		refTypeListBox.addItem("gut microbiota compositions", "R");
		refTypeListBox.addItem("other diet and fitness parameters", "F");
		refTypeListBox.addItem("immunological parameters", "I");
		refTypeListBox.addItem("all other available parameters", "A");
		
		final Label rankLabel1 = new Label("at the");
		itemSelectionHp.add(rankLabel1);
		itemSelectionHp.add(rankListBox);
		final Label rankLabel2 = new Label("level, correlated with:");
		itemSelectionHp.add(rankLabel2);
		
		refTypeListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				if (refTypeListBox.getSelectedIndex() == 0) {
					rankLabel1.setVisible(true);
					rankListBox.setVisible(true);
					rankLabel2.setText("level, correlated with:");
				} else {
					rankLabel1.setVisible(false);
					rankListBox.setVisible(false);
					rankLabel2.setText("correlated with:");
				}
			}
		});
		
		itemSelectionHp.add(profileListBox);
		
		itemSelectionHp.add(new Label(" using "));
		correlationListBox.addItem(GutFloraConstant.CORRELATION_SPEARMAN, GutFloraConstant.CORRELATION_SPEARMAN_VALUE.toString());
		correlationListBox.addItem(GutFloraConstant.CORRELATION_PEARSON, GutFloraConstant.CORRELATION_PEARSON_VALUE.toString());
		itemSelectionHp.add(correlationListBox);
		
		itemSelectionHp.add(new Button("Search", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String targetName = profileListBox.getValue(profileListBox.getSelectedIndex());
				if (targetName == null || targetName.equals("")) {
					warnMessage("Please select a parameter first.");
					return;
				}
				loadingPopupPanel.show();
				if (refTypeListBox.getSelectedIndex() == 0) {
					String rank = rankListBox.getValue(rankListBox.getSelectedIndex());
					service.searchForSimilarReads(ProfilesAnalysisWidget.this.selectedSamples, rank, targetName,
							Integer.valueOf(correlationListBox.getSelectedValue()), currentLang,
							new AsyncCallback<SearchResultData>() {
						
						@Override
						public void onSuccess(SearchResultData result) {
							searchResultData = result;
							History.newItem(currentLang + GutFloraConstant.NAVI_LINK_SEARCH + GutFloraConstant.NAVI_LINK_SUFFIX_PROFILE + suffix);
							loadingPopupPanel.hide();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							warnMessage(BaseWidget.SERVER_ERROR);
							loadingPopupPanel.hide();
						}
					});
				} else {
					String refType = refTypeListBox.getValue(refTypeListBox.getSelectedIndex());
					service.searchForSimilerProfilesbyProfile(ProfilesAnalysisWidget.this.selectedSamples, targetName,
							refType, Integer.valueOf(correlationListBox.getSelectedValue()), currentLang, new AsyncCallback<SearchResultData>() {
						
						@Override
						public void onSuccess(SearchResultData result) {
							searchResultData = result;
							History.newItem(currentLang + GutFloraConstant.NAVI_LINK_SEARCH + GutFloraConstant.NAVI_LINK_SUFFIX_PROFILE + suffix);
							loadingPopupPanel.hide();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							warnMessage(BaseWidget.SERVER_ERROR);
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
		
		for (int k = 0; k < GutFloraConstant.RANK_LIST.size(); k++) {
			rankListBox.addItem(GutFloraConstant.RANK_LIST.get(k));
		}
		rankListBox.setSelectedIndex(1);

		profileCategoryListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				String categoryId = profileCategoryListBox.getSelectedValue();
				getProfileGroup(categoryId);
				updateTable(categoryId);
			}
		});

		profileGroupListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				String categoryId = profileCategoryListBox.getSelectedValue();
				updateTable(categoryId);
			}
		});
		
		HorizontalPanel profileGroupHp = new HorizontalPanel();
		profileGroupHp.setSpacing(6);
		profileGroupHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		// move the column management to here...
		Image changeColumnIcon = new Image(resources.getSettingIconImageResource());
		profileGroupHp.add(changeColumnIcon);
		changeColumnIcon.addClickHandler(new ClickHandler() {
			
			private DialogBox dialogBox;
			
			@Override
			public void onClick(ClickEvent event) {
				if (currentColumns == null || availableColumns == null) {
					warnMessage("Unexpected error, please try again or contact us.");
					return;
				}

				dialogBox = createSelectColumnDialogBox();
				dialogBox.setGlassEnabled(true);
				dialogBox.setAnimationEnabled(false);
				dialogBox.setAutoHideEnabled(true);
				
				dialogBox.center();
				dialogBox.show();
			}
		});
		changeColumnIcon.addStyleName("clickable");
		changeColumnIcon.setTitle("Column Management");
		profileGroupHp.add(new HTML("&nbsp;&nbsp;"));

		profileGroupHp.add(new Label("Category: "));
		profileGroupHp.add(profileCategoryListBox);
		profileGroupHp.add(profileGroupListBox);
		
		profileGroupHp.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;"));
		
		service.getProfileGroupNames(currentLang, new AsyncCallback<List<List<String>>>() {
			
			@Override
			public void onSuccess(List<List<String>> result) {
				profileCategoryListBox.clear();
				for (List<String> item: result) {
					profileCategoryListBox.addItem(item.get(0), item.get(1));
				}
				getProfileGroup(result.get(0).get(1));
				updateTable(result.get(0).get(1));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage(BaseWidget.SERVER_ERROR);
			}
		});
		
		vp.add(profileGroupHp);
		
		vp.add(analysisTabelPanel);

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
		
		service.getImmunologicalGroupNames(currentLang, new AsyncCallback<List<List<String>>>() {
			
			@Override
			public void onSuccess(List<List<String>> result) {
				if (result == null || result.size() == 0) {
					refTypeListBox.removeItem(3);
					refTypeListBox.removeItem(2);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}
		});


		initWidget(vp);
	}
	
	protected void getProfileGroup(String categoryId) {
		profileGroupListBox.clear();
		service.getProfileGroups(categoryId, currentLang, new AsyncCallback<List<List<String>>>() {
			
			@Override
			public void onSuccess(List<List<String>> result) {
				profileGroupListBox.addItem("", "");
				if (result.size() > 1) {
					for (List<String> item: result) {
						profileGroupListBox.addItem(item.get(0), item.get(1));
					}
					profileGroupListBox.setVisible(true);
				} else {
					profileGroupListBox.setVisible(false);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage(BaseWidget.SERVER_ERROR);
			}
		});
	}

	private void updateTable(String categoryId) {
		loadingPopupPanel.show();

		// TODO have to show non-value type data
		service.getProfilesAnalysisData(selectedSamples, categoryId, profileGroupListBox.getSelectedValue(), currentLang, new AsyncCallback<GutFloraAnalysisData>() {
			
			@Override
			public void onSuccess(GutFloraAnalysisData result) {
				unitMap = result.getMetadataMap();
				Widget table = createTableContent(result);

				analysisTabelPanel.setWidget(table);
				
				profileListBox.clear();
				profileListBox.addItem("");
				Set<String> numericProfilesHeader = result.getNumericProfilesHeader();
				for (String h: result.getProfilesHeader()) {
					if (numericProfilesHeader.contains(h)) {
						profileListBox.addItem(h);
					}
				}
				
				currentColumns = result.getProfilesHeader();
				availableColumns = result.getAvailableProfilesHeader();

				loadingPopupPanel.hide();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage(BaseWidget.SERVER_ERROR);
				loadingPopupPanel.hide();
			}
		});
	}

	private Widget createTableContent(GutFloraAnalysisData data) {
		VerticalPanel vp = new VerticalPanel();
		
		Set<String> numericProfiles = data.getNumericProfilesHeader();
		
		final CellTable<String> cellTable = new CellTable<String>();

		cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		cellTable.setAutoHeaderRefreshDisabled(true);
		
		cellTable.setEmptyTableWidget(new Label("no data found"));
		
		final Map<String, Map<String, String>> rows = data.getRows();
		
		List<String> sampleIds = data.getSampleIds();
		
		ListDataProvider<String> dataProvider = new ListDataProvider<String>(sampleIds);
		
		ListHandler<String> sortHandler = new ListHandler<String>(dataProvider.getList()){
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

		List<String> columnHeader = data.getProfilesHeader();
		for (final String h : columnHeader) {
			// TODO should be take cared by the Parameter class?
			if (h.endsWith("(RGB#)")) {
				Column<String, SafeHtml> colorCol = new Column<String, SafeHtml>(new SafeHtmlCell()) {

					@Override
					public SafeHtml getValue(String id) {
						SafeHtmlBuilder sb = new SafeHtmlBuilder();
						sb.appendHtmlConstant("<div style=\"background:" + rows.get(id).get(h).substring(3)
								+ ";\">&nbsp;</div>");
						return sb.toSafeHtml();
					}
				};
				String colHeader = h.substring(0, h.length() - 6);
				cellTable.addColumn(colorCol, colHeader);
				colorCol.setSortable(true);
				sortHandler.setComparator(colorCol, new Comparator<String>() {
					
					@Override
					public int compare(String o1, String o2) {
						String r1 = rows.get(o1).get(h);
						r1 = r1 == null? "-" : r1;
						String r2 = rows.get(o2).get(h);
						r2 = r2 == null? "-" : r2;
						return r1.compareTo(r2);
					}
				});

			} else {
				TextColumn<String> column = new TextColumn<String>() {
					
					@Override
					public String getValue(String id) {
						String reads = rows.get(id).get(h);
						return reads == null? "-" : reads;
					}
					
				};
				if (unitMap != null && unitMap.get(h) != null) {
					cellTable.addColumn(column, h + " (" + unitMap.get(h) + ")");
				} else {
					cellTable.addColumn(column, h);
				}
				column.setSortable(true);
				// TODO should depends on the data type ...
				
				if (numericProfiles.contains("columnHeader")) {
					sortHandler.setComparator(column, new Comparator<String>() {
						
						@Override
						public int compare(String o1, String o2) {
							String r1 = rows.get(o1).get(h);
							r1 = r1 == null? "0" : r1;
							String r2 = rows.get(o2).get(h);
							r2 = r2 == null? "0" : r2;
							return Float.valueOf(r1).compareTo(Float.valueOf(r2));
						}
					});
				} else {
					sortHandler.setComparator(column, new Comparator<String>() {
						
						@Override
						public int compare(String o1, String o2) {
							String r1 = rows.get(o1).get(h);
							r1 = r1 == null? "-" : r1;
							String r2 = rows.get(o2).get(h);
							r2 = r2 == null? "-" : r2;
							return r1.compareTo(r2);
						}
					});
				}
			}
			
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
					} else {
						int index = column - 1;
						if (index < currentColumns.size()) {
							String columnName = currentColumns.get(index);
							for (int i = 1; i < profileListBox.getItemCount(); i++) {
								if (profileListBox.getItemText(i).equals(columnName)) {
									profileListBox.setSelectedIndex(i);
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
	
	private DialogBox createSelectColumnDialogBox() {
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
		
		HorizontalPanel buttonHp = new HorizontalPanel();
		buttonHp.setSpacing(12);
		Button okButton = new Button("OK", new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				List<String> selectedColumns = columnWidget.getSelectedItems();
				updateTable(selectedColumns);
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

	private void updateTable(List<String> selectedColumns) {
		loadingPopupPanel.show();

		service.getProfilesAnalysisData(selectedSamples, selectedColumns, currentLang,
				new AsyncCallback<GutFloraAnalysisData>() {
			
			@Override
			public void onSuccess(GutFloraAnalysisData result) {
				Widget table = createTableContent(result);

				analysisTabelPanel.setWidget(table);
				
				profileListBox.clear();
				profileListBox.addItem("");
				Set<String> numericProfilesHeader = result.getNumericProfilesHeader();
				for (String h: result.getProfilesHeader()) {
					if (numericProfilesHeader.contains(h)) {
						profileListBox.addItem(h);
					}
				}
				
				currentColumns = result.getProfilesHeader();
				
				loadingPopupPanel.hide();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage(BaseWidget.SERVER_ERROR);
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

}
