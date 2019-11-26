package jp.go.nibiohn.bioinfo.client.readvis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.go.nibiohn.bioinfo.client.generic.W3cCssSelectors;
import jp.go.nibiohn.bioinfo.shared.DendrogramCache;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.VisualizationtResult;

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

public class MicrobiotaHeatmapWidget extends ReadVisualizeWidget {
	
	private SimplePanel taxonNaviPanel = new SimplePanel();
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
	
	private SimplePanel legendPanel = new SimplePanel();
	
	private Map<Integer, DendrogramCache> cacheMap = null;
	
	private PopupPanel loadingPopupPanel = new PopupPanel();
	
	public MicrobiotaHeatmapWidget(Set<SampleEntry> selectedSamples, String rank, boolean isSubset) {
		this("Heat Map", GutFloraConstant.NAVI_LINK_VIEW_HEATMAP, selectedSamples, rank, isSubset);
	}
	
	public MicrobiotaHeatmapWidget(String name, String link, Set<SampleEntry> selectedSamples, String rank,
			boolean isSubset) {
		super(name, link);
		this.selectedSamples = selectedSamples;
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
				if (clusteringRb.getValue()) {
					updateClusteredHeatmap();
				} else {
					drawNormalHeatmap();
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
				History.newItem(GutFloraConstant.NAVI_LINK_ANALYSIS + GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX);
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
		
		linkageListBox.addItem(GutFloraConstant.CLUSTERING_LINKAGE_AVERAGE, GutFloraConstant.CLUSTERING_LINKAGE_AVERAGE_VALUE.toString());
		linkageListBox.addItem(GutFloraConstant.CLUSTERING_LINKAGE_COMPLETE, GutFloraConstant.CLUSTERING_LINKAGE_COMPLETE_VALUE.toString());
		linkageListBox.addItem(GutFloraConstant.CLUSTERING_LINKAGE_SINGLE, GutFloraConstant.CLUSTERING_LINKAGE_SINGLE_VALUE.toString());
		linkageOptDp.add(linkageOptHp);
		
		distanceListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				updateClusteredHeatmap();
			}
		});
		linkageListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				updateClusteredHeatmap();
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
				updateClusteredHeatmap();
			}
		});
		normalRb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				distanceListBox.setEnabled(false);
				linkageListBox.setEnabled(false);
				drawNormalHeatmap();
			}
		});
		
		optHp.add(distanceOptDp);
		optHp.add(linkageOptDp);
		thisWidget.add(optHp);
		
		HorizontalPanel titleHp = new HorizontalPanel();
		titleHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		titleHp.add(new HTML("<h2>Gut microbiota composition heat map</h2>\n"));
		titleHp.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;"));
		titleHp.add(legendPanel);
		thisWidget.add(titleHp);
		
		service.getHeatmapLegend(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				legendPanel.setWidget(new HTML("<div style=\"margin-left: 120px;\">" + result + "</div>"));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				legendPanel.setWidget(new HTML("<div style=\"margin-left: 120px; border: 1px solid lightgrey;\">Cannot load legend.</div>"));
			}
		});
		
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
		
		loadDistance();
		drawChart();
	}
	
	private void drawChart() {
		// default is normal sorting
		distanceListBox.setEnabled(false);
		linkageListBox.setEnabled(false);
		drawNormalHeatmap();
	}
	
	private void loadDistance() {
		service.getAllDistanceTypes(new AsyncCallback<Map<Integer,String>>() {
			
			@Override
			public void onSuccess(Map<Integer, String> result) {
				List<Integer> list = new ArrayList<Integer>(result.keySet());
				Collections.sort(list);
				for (Integer value : list) {
					distanceListBox.addItem(result.get(value), value.toString());
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage("Failed to load the distance type.");
			}
		});
	}

	private void drawNormalHeatmap() {
		clearMessage();
		loadingPopupPanel.show();
		service.getReadsHeatmap(selectedSamples, getSelectedRank(), new AsyncCallback<VisualizationtResult>() {
			
			@Override
			public void onSuccess(VisualizationtResult result) {
				chartPanel.setWidget(new HTML(result.getSvgChart()));
				
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

	private void updateClusteredHeatmap() {
		clearMessage();
		loadingPopupPanel.show();
		service.getClusteredReadsHeatmap(selectedSamples, getSelectedRank(), getDistance(), getLinkage(), cacheMap, 
				new AsyncCallback<VisualizationtResult>() {

					@Override
					public void onSuccess(VisualizationtResult result) {
						chartPanel.setWidget(new HTML(result.getSvgChart()));
						addNodeClickEvents();
						
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
	
}
