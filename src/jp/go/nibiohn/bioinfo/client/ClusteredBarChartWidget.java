package jp.go.nibiohn.bioinfo.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import jp.go.nibiohn.bioinfo.client.generic.W3cCssSelectors;
import jp.go.nibiohn.bioinfo.shared.DendrogramCache;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.TaxonEntry;
import jp.go.nibiohn.bioinfo.shared.VisualizationtResult;

public class ClusteredBarChartWidget extends ReadVisualizeWidget {
	
	private SimplePanel taxonNaviPanel = new SimplePanel();
	private List<TaxonEntry> taxonList = new ArrayList<TaxonEntry>();
	private SimplePanel chartPanel = new SimplePanel();

	private ListBox rankListBox = new ListBox();
	
	private HorizontalPanel viewSamplePanel = new HorizontalPanel();
	private Label viewSampleLabel = new Label("View the selected");
	// the text will be updated depends on the number of selected samples
	private Button viewSampleBtn = new Button("N samples");
	private boolean showViewSample = true;
	
	private RadioButton normalRb = new RadioButton("sortBy", "Sample ID");
	private RadioButton clusteringRb = new RadioButton("sortBy", "Hierarchical Clustering");
	
	private ListBox linkageListBox = new ListBox();
	private ListBox distanceListBox = new ListBox();

	private Map<Integer, DendrogramCache> cacheMap = null;

	private PopupPanel loadingPopupPanel = new PopupPanel();
	
	// maybe become an argument?
	private Integer experimentMethod = GutFloraConstant.EXPERIMENT_METHOD_16S;
	
	public ClusteredBarChartWidget(Set<SampleEntry> selectedSamples, String rank, 
			boolean isSubset, String lang) {
		this("Bar Chart", GutFloraConstant.NAVI_LINK_VIEW_BARCHART, selectedSamples, rank, isSubset, lang);
	}
	
	public ClusteredBarChartWidget(String name, String link, Set<SampleEntry> selectedSamples, String rank,
			boolean isSubset, String lang) {
		super(name, lang + link);
		this.selectedSamples = selectedSamples;
		this.currentLang = lang;
		if (isSubset) {
			showViewSample = false;
		}
		
		VerticalPanel thisWidget = new VerticalPanel();
		for (int i = 0; i < GutFloraConstant.RANK_LIST.size(); i++) {
			String item = GutFloraConstant.RANK_LIST.get(i);
			rankListBox.addItem(item);
			if (item.equals(rank)) {
				rankListBox.setSelectedIndex(i);
			}
		}

		rankListBox.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				if (!taxonList.isEmpty()) {
					String currentRank = getSelectedRank();
					if (GutFloraConstant.RANK_MAP.get(currentRank) <= GutFloraConstant.RANK_MAP.get(taxonList.get(0).getRank())) {
						subsetRank = null;
						subsetTaxonId = null;
						taxonList.clear();
						taxonNaviPanel.setVisible(false);
					} else {
						List<TaxonEntry> newList = new ArrayList<TaxonEntry>();
						for (TaxonEntry te: taxonList) {
							if (te.getRank().equals(currentRank)) {
								break;
							}
							newList.add(te);
							subsetRank = te.getRank();
							subsetTaxonId = te.getIdentifier();
						}
						taxonList.clear();
						taxonList.addAll(newList);
						setTaxonNaviBar();
					}
				}
				
				if (clusteringRb.getValue()) {
					updateClusteredChart();
				} else {
					drawNormalBarChart();
				}
			}
		});
		HorizontalPanel rankHp = new HorizontalPanel();
		rankHp.setSpacing(6);
		rankHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		rankHp.add(new Label("View at"));
		rankHp.add(rankListBox);
		rankListBox.setWidth("80px");
		rankHp.add(new Label("level"));
		
		rankHp.add(viewSamplePanel);
		viewSamplePanel.setSpacing(3);
		viewSamplePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		viewSamplePanel.setStyleName("viewSamplePanel");
		viewSamplePanel.add(viewSampleLabel);
		viewSamplePanel.add(viewSampleBtn);
		viewSampleBtn.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(currentLang + GutFloraConstant.NAVI_LINK_ANALYSIS + GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX);
			}
		});
		viewSamplePanel.setVisible(false);
		
		thisWidget.add(rankHp);
		
		// add clustering options
		HorizontalPanel optHp = new HorizontalPanel();
		
		DecoratorPanel linkageOptDp = new DecoratorPanel();
		linkageOptDp.setTitle("Linkage options");
		linkageOptDp.addStyleName("optionDec");
		HorizontalPanel linkageOptHp = new HorizontalPanel();
		linkageOptHp.setSpacing(12);
		linkageOptHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		linkageOptHp.add(new HTML("<b>Distance:</b>"));
		linkageOptHp.add(distanceListBox);
		linkageOptHp.add(new HTML("<b>Linkage:</b>"));
		linkageOptHp.add(linkageListBox);
		
		distanceListBox.addItem(GutFloraConstant.SAMPLE_DISTANCE_UNWEIGHTED_UNIFRAC, GutFloraConstant.SAMPLE_DISTANCE_UNWEIGHTED_UNIFRAC_VALUE.toString());
		distanceListBox.addItem(GutFloraConstant.SAMPLE_DISTANCE_WEIGHTED_UNIFRAC, GutFloraConstant.SAMPLE_DISTANCE_WEIGHTED_UNIFRAC_VALUE.toString());
		distanceListBox.addItem(GutFloraConstant.SAMPLE_DISTANCE_BRAY_CURTIS_OTU, GutFloraConstant.SAMPLE_DISTANCE_BRAY_CURTIS_OTU_VALUE.toString());
		distanceListBox.addItem(GutFloraConstant.SAMPLE_DISTANCE_BRAY_CURTIS_GENUS, GutFloraConstant.SAMPLE_DISTANCE_BRAY_CURTIS_GENUS_VALUE.toString());
		
		linkageListBox.addItem(GutFloraConstant.CLUSTERING_LINKAGE_AVERAGE, GutFloraConstant.CLUSTERING_LINKAGE_AVERAGE_VALUE.toString());
		linkageListBox.addItem(GutFloraConstant.CLUSTERING_LINKAGE_COMPLETE, GutFloraConstant.CLUSTERING_LINKAGE_COMPLETE_VALUE.toString());
		linkageListBox.addItem(GutFloraConstant.CLUSTERING_LINKAGE_SINGLE, GutFloraConstant.CLUSTERING_LINKAGE_SINGLE_VALUE.toString());
		
		linkageOptDp.add(linkageOptHp);
		
		distanceListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				updateClusteredChart();
			}
		});
		linkageListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				updateClusteredChart();
			}
		});
		
		DecoratorPanel distanceOptDp = new DecoratorPanel();
		distanceOptDp.setTitle("Distance options");
		distanceOptDp.addStyleName("optionDec");
		HorizontalPanel sortingOptHp = new HorizontalPanel();
		sortingOptHp.setSpacing(12);
		sortingOptHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		sortingOptHp.add(new HTML("<b>Sort by:</b>"));
		sortingOptHp.add(normalRb);
		sortingOptHp.add(clusteringRb);
		distanceOptDp.add(sortingOptHp);
		normalRb.setValue(Boolean.TRUE);
		
		clusteringRb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				distanceListBox.setEnabled(true);
				linkageListBox.setEnabled(true);
				updateClusteredChart();
			}
		});
		normalRb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				distanceListBox.setEnabled(false);
				linkageListBox.setEnabled(false);
				drawNormalBarChart();
			}
		});
		
		optHp.add(distanceOptDp);
		optHp.add(linkageOptDp);
		thisWidget.add(optHp);
		
		thisWidget.add(new HTML("<h2>Gut microbiota composition bar chart</h2>\n"));
		
		thisWidget.add(taxonNaviPanel);
		taxonNaviPanel.setStyleName("taxonNaviPanel");
		taxonNaviPanel.setVisible(false);
		thisWidget.add(chartPanel);

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

		initWidget(thisWidget);
		
		drawChart();
	}
	
	private void drawChart() {
		// default is normal sorting
		distanceListBox.setEnabled(false);
		linkageListBox.setEnabled(false);
		drawNormalBarChart();
	}
	
	private void drawNormalBarChart() {
		clearMessage();
		loadingPopupPanel.show();
		if (subsetRank != null && subsetTaxonId != null) {
			service.getReadsBarChart(selectedSamples, getSelectedRank(), subsetRank, subsetTaxonId, experimentMethod, 
					new AsyncCallback<VisualizationtResult>() {
				
				@Override
				public void onSuccess(VisualizationtResult result) {
					chartPanel.setWidget(new HTML(result.getSvgChart()));
					addBarClickEvents();
					
					viewSamplePanel.setVisible(false);
					loadingPopupPanel.hide();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					warnMessage(SERVER_ERROR);
					loadingPopupPanel.hide();
				}
			});
			
		} else {
			service.getReadsBarChart(selectedSamples, getSelectedRank(), experimentMethod,
					new AsyncCallback<VisualizationtResult>() {
				
				@Override
				public void onSuccess(VisualizationtResult result) {
					chartPanel.setWidget(new HTML(result.getSvgChart()));
					addBarClickEvents();
					
					viewSamplePanel.setVisible(false);
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
	
	private String subsetRank = null;
	private String subsetTaxonId = null;
	
	public void addBarClickEvents() {
		// do nothing if the selected rank is the last item
		if (rankListBox.getSelectedIndex() == rankListBox.getItemCount() - 1) {
			return;
		}
		
		NodeList<Element> nodeList = W3cCssSelectors.querySelectorAll(".taxonBar");
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Element item = nodeList.getItem(i);
			Event.sinkEvents(item, Event.ONCLICK);
			Event.setEventListener(item, new EventListener() {
				public void onBrowserEvent(Event event) {
					NodeList<Element> descs = item.getElementsByTagName("desc");
					subsetTaxonId = descs.getItem(0).getInnerText();
					
					if (taxonList.isEmpty()) {
						taxonList.add(new TaxonEntry("[x]", "leader", "original:" + getSelectedRank()));
					}
					
					String taxonName = descs.getItem(1).getInnerText();
					taxonList.add(new TaxonEntry(taxonName, subsetTaxonId, getSelectedRank()));
					setTaxonNaviBar();
					
					subsetRank = getSelectedRank();
					rankListBox.setSelectedIndex(rankListBox.getSelectedIndex() + 1);
					if (clusteringRb.getValue()) {
						updateClusteredChart();
					} else {
						drawNormalBarChart();
					}
				}
			});
		}

	}
	
	public void addNodeClickEvents() {
		// attache the node click events
		NodeList<Element> nodeList = W3cCssSelectors.querySelectorAll(".dendroNode");
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Element item = nodeList.getItem(i);
			Event.sinkEvents(item, Event.ONCLICK);
			Event.setEventListener(item, new EventListener() {
				public void onBrowserEvent(Event event) {
					NodeList<Element> labels = W3cCssSelectors.querySelectorAll(".selectedLabel");
					for (int j = 0; j < labels.getLength(); j++) {
						labels.getItem(j).setAttribute("class", "");
					}

					NodeList<Element> descs = item.getElementsByTagName("desc");
					String[] ids = descs.getItem(0).getInnerText().split(",");
					for (int k = 0; k < ids.length; k++) {
						Element ele = W3cCssSelectors.querySelector("#" + ids[k]);
						ele.setAttribute("class", "selectedLabel");
					}
					if (showViewSample) {
						String text = " " + ids.length + " sample" + (ids.length > 1?"s":"");
						viewSampleBtn.setText(text);
						viewSamplePanel.setVisible(true);
					}
					
					subsetSampleIdString = descs.getItem(0).getInnerText().replaceAll("label_left_", "");
				}
			});
		}

		Element element = W3cCssSelectors.querySelector("#chart_body");
		Event.sinkEvents(element, Event.ONCLICK);
		Event.setEventListener(element, new EventListener() {
				public void onBrowserEvent(Event event) {
					NodeList<Element> labels = W3cCssSelectors.querySelectorAll(".selectedLabel");
					for (int j = 0; j < labels.getLength(); j++) {
						labels.getItem(j).setAttribute("class", "");
					}
					viewSamplePanel.setVisible(false);
				}
		});
	}

	private void updateClusteredChart() {
		clearMessage();
		loadingPopupPanel.show();
		if (subsetRank != null && subsetTaxonId != null) {
			service.getReadsClusteredBarChart(selectedSamples, getSelectedRank(), subsetRank, subsetTaxonId,
					experimentMethod, getDistance(), getLinkage(), 15, cacheMap,
					new AsyncCallback<VisualizationtResult>() {
				
				@Override
				public void onSuccess(VisualizationtResult result) {
					chartPanel.setWidget(new HTML(result.getSvgChart()));
					addNodeClickEvents();
					addBarClickEvents();
					
					cacheMap = result.getDendrogramMap();
					
					viewSamplePanel.setVisible(false);
					loadingPopupPanel.hide();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					warnMessage(SERVER_ERROR);
					loadingPopupPanel.hide();
				}
			});
			
		} else {
			service.getReadsClusteredBarChart(selectedSamples, getSelectedRank(), experimentMethod, getDistance(),
					getLinkage(), cacheMap, new AsyncCallback<VisualizationtResult>() {
				
				@Override
				public void onSuccess(VisualizationtResult result) {
					chartPanel.setWidget(new HTML(result.getSvgChart()));
					addNodeClickEvents();
					addBarClickEvents();
					
					cacheMap = result.getDendrogramMap();
					
					viewSamplePanel.setVisible(false);
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

	@Override
	public String getSelectedRank() {
		return rankListBox.getValue(rankListBox.getSelectedIndex());
	}

	private int getLinkage() {
		return Integer.valueOf(linkageListBox.getSelectedValue()).intValue();
	}

	private int getDistance() {
		return Integer.valueOf(distanceListBox.getSelectedValue()).intValue();
	}
	
	private void setTaxonNaviBar() {
		
		HorizontalPanel naviBar = new HorizontalPanel();
		naviBar.add(new HTML("<div style=\"font-weight: bold; margin-left: 12px;\">Filters</div>"));
		
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
						subsetRank = null;
						subsetTaxonId = null;
						taxonList.clear();
						taxonNaviPanel.setVisible(false);
						
						String rank = ent.getRank();
						rank = rank.substring(rank.indexOf(":") + 1);
						rankListBox.setSelectedIndex(GutFloraConstant.RANK_MAP.get(rank).intValue() - 1);
						
						if (clusteringRb.getValue()) {
							updateClusteredChart();
						} else {
							drawNormalBarChart();
						}
					}
				});
				naviBar.add(new Label(": "));
				
			} else {
				label.setTitle(ent.getRank());
				label.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						String entityRank = ent.getRank();
						rankListBox.setSelectedIndex(GutFloraConstant.RANK_MAP.get(entityRank));
						subsetRank = entityRank;
						subsetTaxonId = ent.getIdentifier();
						
						List<TaxonEntry> newList = new ArrayList<TaxonEntry>();
						for (TaxonEntry te: taxonList) {
							newList.add(te);
							if (te.getRank().equals(entityRank)) {
								break;
							}
						}
						taxonList.clear();
						taxonList.addAll(newList);
						setTaxonNaviBar();

						if (clusteringRb.getValue()) {
							updateClusteredChart();
						} else {
							drawNormalBarChart();
						}
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
}
