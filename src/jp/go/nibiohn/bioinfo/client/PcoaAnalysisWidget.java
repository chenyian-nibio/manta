package jp.go.nibiohn.bioinfo.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.PcoaResult;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PcoaAnalysisWidget extends ReadVisualizeWidget {

	private PopupPanel loadingPopupPanel = new PopupPanel();
	
	private SimplePanel chartPanel = new SimplePanel();

	private Map<Integer, PcoaResult> pcoaResultMap = new HashMap<Integer, PcoaResult>();

	private ListBox profileCategoryListBox = new ListBox();
	private ListBox profileGroupListBox = new ListBox();
	private ListBox profileListBox = new ListBox();
	
	private ListBox sampleDistanceListBox = new ListBox();

	private List<String> sampleIdList = new ArrayList<String>();

	private Integer experimentMethod = GutFloraConstant.EXPERIMENT_METHOD_16S;
	
	public PcoaAnalysisWidget(Set<SampleEntry> selectedSamples, Integer experimentMethod, boolean isSubset, String lang) {
		this("PCoA Chart", GutFloraConstant.NAVI_LINK_VIEW_PCOA, selectedSamples, experimentMethod, isSubset, lang);
	}

	public PcoaAnalysisWidget(String expTag, Set<SampleEntry> selectedSamples, Integer experimentMethod, boolean isSubset, String lang) {
		this("PCoA Chart (" + expTag + ")", GutFloraConstant.NAVI_LINK_VIEW_PCOA, selectedSamples, experimentMethod, isSubset, lang);
	}

	public PcoaAnalysisWidget(String name, String link, Set<SampleEntry> selectedSamples, Integer experimentMethod,
			boolean isSubset, String lang) {
		super(name, lang + link);
		this.selectedSamples = selectedSamples;
		this.experimentMethod = experimentMethod;
		this.currentLang = lang;

		HorizontalPanel thisWidget = new HorizontalPanel();
		VerticalPanel chartVp = new VerticalPanel();
		
		chartVp.add(chartPanel);
		// just for occupying the space
		Label label = new Label("Loading...");
		label.setWidth("600px");
		chartPanel.setWidget(label);
		
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

		profileCategoryListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				getProfileGroup(profileCategoryListBox.getSelectedValue());
				getProfileList();
			}
		});
		profileGroupListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				getProfileList();
			}
		});
		profileListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				loadingPopupPanel.show();
				Integer distanceType = Integer.valueOf(sampleDistanceListBox.getSelectedValue());
				if (pcoaResultMap.get(distanceType) == null) {
					// TODO ??
				}
				service.getPCoAScatterPlot(pcoaResultMap.get(distanceType), profileListBox.getSelectedValue(), currentLang,
						new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String result) {
						chartPanel.setWidget(new HTML(result));
						loadingPopupPanel.hide();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						warnMessage(BaseWidget.SERVER_ERROR);
					}
				});
			}
		});
		
		if (experimentMethod.equals(GutFloraConstant.EXPERIMENT_METHOD_16S)) {
//			sampleDistanceListBox.addItem(GutFloraConstant.SAMPLE_DISTANCE_UNWEIGHTED_UNIFRAC, GutFloraConstant.SAMPLE_DISTANCE_UNWEIGHTED_UNIFRAC_VALUE.toString());
			sampleDistanceListBox.addItem(GutFloraConstant.SAMPLE_DISTANCE_WEIGHTED_UNIFRAC, GutFloraConstant.SAMPLE_DISTANCE_WEIGHTED_UNIFRAC_VALUE.toString());
//			sampleDistanceListBox.addItem(GutFloraConstant.SAMPLE_DISTANCE_BRAY_CURTIS_OTU, GutFloraConstant.SAMPLE_DISTANCE_BRAY_CURTIS_OTU_VALUE.toString());
			sampleDistanceListBox.addItem(GutFloraConstant.SAMPLE_DISTANCE_BRAY_CURTIS_GENUS, GutFloraConstant.SAMPLE_DISTANCE_BRAY_CURTIS_GENUS_VALUE.toString());
			sampleDistanceListBox.addItem(GutFloraConstant.SAMPLE_DISTANCE_JACCARD, GutFloraConstant.SAMPLE_DISTANCE_JACCARD_VALUE.toString());
		} else if (experimentMethod.equals(GutFloraConstant.EXPERIMENT_METHOD_SHOTGUN)) {
			sampleDistanceListBox.addItem(GutFloraConstant.SAMPLE_DISTANCE_BRAY_CURTIS_SPECIES, GutFloraConstant.SAMPLE_DISTANCE_BRAY_CURTIS_SPECIES_VALUE.toString());
			sampleDistanceListBox.addItem(GutFloraConstant.SAMPLE_DISTANCE_JACCARD, GutFloraConstant.SAMPLE_DISTANCE_JACCARD_VALUE.toString());
		} else {
			// should not happen!
		}
		
		sampleDistanceListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				Integer distanceType = Integer.valueOf(sampleDistanceListBox.getSelectedValue());
				if (pcoaResultMap.get(distanceType) == null) {
					loadingPopupPanel.show();
					service.getPCoAResult(sampleIdList, PcoaAnalysisWidget.this.experimentMethod, distanceType,
							new AsyncCallback<PcoaResult>() {
						
						@Override
						public void onSuccess(PcoaResult result) {
							pcoaResultMap.put(Integer.valueOf(sampleDistanceListBox.getSelectedValue()), result);
							service.getPCoAScatterPlot(result, new AsyncCallback<String>() {
								
								@Override
								public void onSuccess(String result) {
									chartPanel.setWidget(new HTML(result));
									loadingPopupPanel.hide();
								}
								
								@Override
								public void onFailure(Throwable caught) {
									warnMessage(BaseWidget.SERVER_ERROR);
									loadingPopupPanel.hide();
								}
							});
						}
						
						@Override
						public void onFailure(Throwable caught) {
							warnMessage(BaseWidget.SERVER_ERROR);
							loadingPopupPanel.hide();
						}
					});
				} else {
					service.getPCoAScatterPlot(pcoaResultMap.get(distanceType), new AsyncCallback<String>() {
						
						@Override
						public void onSuccess(String result) {
							chartPanel.setWidget(new HTML(result));
							loadingPopupPanel.hide();
							loadingPopupPanel.hide();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							warnMessage(BaseWidget.SERVER_ERROR);
							loadingPopupPanel.hide();
						}
					});
				}
				// remove the selection
				profileListBox.setItemSelected(profileListBox.getSelectedIndex(), false);
			}
		});

		
		VerticalPanel profileVp = new VerticalPanel();
		Label header1 = new Label("Distance type:");
		header1.setStyleName("pcoaListboxHeader");
		profileVp.add(header1);
		profileVp.add(sampleDistanceListBox);
		
		
		Label header2 = new Label("Diet and fitness parameters:");
		header2.setStyleName("pcoaListboxHeader");
		profileVp.add(header2);
		profileVp.add(profileCategoryListBox);
		profileVp.add(profileGroupListBox);
		profileVp.add(profileListBox);
		
		// customized tags
		Label label5 = new Label("Customized tags");
		label5.setStyleName("buttonLabel");
		label5.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (customTagDialogBox == null) {
					customTagDialogBox = createIdListDialogBox();
				}
				customTagDialogBox.center();
			}
		});
		profileVp.add(label5);

		final FormPanel formPanel = new FormPanel();
		profileVp.add(formPanel);
		Label label6 = new Label("Export current grouping");
		label6.setStyleName("buttonLabel");
		label6.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				formPanel.setAction(GWT.getModuleBaseURL() + "export");
				formPanel.setMethod(FormPanel.METHOD_POST);
				FlowPanel flowPanel = new FlowPanel();
				formPanel.setWidget(flowPanel);
				Hidden profileHidden = new Hidden("profile", profileListBox.getSelectedValue());
				flowPanel.add(profileHidden);
				Hidden currentLangHidden = new Hidden("currentLang", currentLang);
				flowPanel.add(currentLangHidden);
				Iterator<String> iterator = sampleIdList.iterator();
				StringBuffer sb = new StringBuffer();
				while (iterator.hasNext()) {
					sb.append(iterator.next());
					if (iterator.hasNext()) {
						sb.append(",");
					}
				}
				Hidden sampleIdsHidden = new Hidden("sampleIds", sb.toString());
				flowPanel.add(sampleIdsHidden);
				Hidden fileNameHidden = new Hidden("fileName", "group.txt");
				flowPanel.add(fileNameHidden);

				formPanel.submit();
			}
		});
		profileVp.add(label5);
		
		profileCategoryListBox.setWidth("300px");
		profileGroupListBox.setWidth("300px");
		profileListBox.setWidth("300px");
		profileListBox.setVisibleItemCount(10);
		
		service.getAllParameterGroupNames(currentLang, new AsyncCallback<List<List<String>>>() {
			
			@Override
			public void onSuccess(List<List<String>> result) {
				profileCategoryListBox.clear();
				for (List<String> item: result) {
					profileCategoryListBox.addItem(item.get(0), item.get(1));
				}
				getProfileGroup(result.get(0).get(1));
				getProfileList();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage(BaseWidget.SERVER_ERROR);
			}
		});

		thisWidget.add(chartVp);
		thisWidget.add(profileVp);

		initWidget(thisWidget);

		if (GutFloraConstant.EXPERIMENT_METHOD_16S.equals(experimentMethod)) {
			for (SampleEntry se : selectedSamples) {
				if (se.has16SData()) {
					sampleIdList.add(se.getSampleId());
				}
			}
		} else {
			for (SampleEntry se : selectedSamples) {
				if (se.hasShotgunData()) {
					sampleIdList.add(se.getSampleId());
				}
			}
		}

		loadingPopupPanel.show();
		service.getPCoAResult(sampleIdList, this.experimentMethod, Integer.valueOf(sampleDistanceListBox.getSelectedValue()),
				new AsyncCallback<PcoaResult>() {
			
			@Override
			public void onSuccess(PcoaResult result) {
				pcoaResultMap.put(Integer.valueOf(sampleDistanceListBox.getSelectedValue()), result);
				service.getPCoAScatterPlot(result, new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String result) {
						chartPanel.setWidget(new HTML(result));
						loadingPopupPanel.hide();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						warnMessage(BaseWidget.SERVER_ERROR);
					}
				});
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage(BaseWidget.SERVER_ERROR);
			}
		});
		
	}
	
	private DialogBox customTagDialogBox;
	private SimplePanel sampleSelectPanel = new SimplePanel();
	private TextArea textArea = new TextArea();
	private Label dialogBoxMessageLable = new Label("");
	private DialogBox createIdListDialogBox() {
		final DialogBox dialogBox = new DialogBox();
		dialogBox.ensureDebugId("idListDialogBox");
		dialogBox.setText("Customized tag coloring");

		VerticalPanel dialogContents = new VerticalPanel();
		dialogContents.setSpacing(4);
		dialogContents.setWidth("500px");
		dialogBox.setWidget(dialogContents);
		
		dialogContents.add(dialogBoxMessageLable);
		dialogBoxMessageLable.setVisible(false);
		dialogBoxMessageLable.setStyleName("warnMessage");
		
		HorizontalPanel headerHp = new HorizontalPanel();
		headerHp.add(new Label("Input/Paste the identifier-tag pairs: ("));
		Label showLabel = new Label("Show sample IDs");
		showLabel.setStyleName("buttonLabel");
		showLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Iterator<String> iterator = sampleIdList.iterator();
				StringBuffer sb = new StringBuffer();
				while (iterator.hasNext()) {
					sb.append(iterator.next() + "\n");
				}
				textArea.setValue(sb.toString());
			}
		});
		headerHp.add(showLabel);
		headerHp.add(new Label(")"));
		dialogContents.add(headerHp);
		textArea.setWidth("450px");
		textArea.setVisibleLines(10);
		sampleSelectPanel.setWidget(textArea);
		dialogContents.add(sampleSelectPanel);
		
		HorizontalPanel buttonHp = new HorizontalPanel();
		buttonHp.setSpacing(12);
		Button okButton = new Button("OK", new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (textArea.getText().equals("")) {
					dialogBoxMessageLable.setText("Please input or paste the identifier-tag pairs.");
					dialogBoxMessageLable.setVisible(true);
					return;
				}
				Integer distanceType = Integer.valueOf(sampleDistanceListBox.getSelectedValue());
				if (pcoaResultMap.get(distanceType) == null) {
					// TODO should not happen?
				}
				service.getPCoAScatterPlot(pcoaResultMap.get(distanceType), textArea.getText(), new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String result) {
						chartPanel.setWidget(new HTML(result));
						dialogBoxMessageLable.setVisible(false);
						dialogBox.hide();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						warnMessage(BaseWidget.SERVER_ERROR);
						dialogBox.hide();
					}
				});
			}
		});
		okButton.setWidth("80px");
		buttonHp.add(okButton);

		Button resetButton = new Button("Reset", new ClickHandler() {
			public void onClick(ClickEvent event) {
				textArea.setText("");
				dialogBoxMessageLable.setVisible(false);
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

		dialogBox.setGlassEnabled(true);
		dialogBox.setAnimationEnabled(false);
		dialogBox.setAutoHideEnabled(true);

		return dialogBox;
	}

	private void getProfileGroup(String categoryId) {
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
	
	private void getProfileList() {
		String categoryId = profileCategoryListBox.getSelectedValue();
		String groupId = profileGroupListBox.getSelectedValue();
		service.getProfileNames(categoryId, groupId, currentLang, new AsyncCallback<List<String>>() {
			
			@Override
			public void onSuccess(List<String> result) {
				profileListBox.clear();
				for (String string : result) {
					profileListBox.addItem(string);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage(BaseWidget.SERVER_ERROR);
			}
		});
	}

	/**
	 * unused method, return null
	 */
	@Override
	public String getSelectedRank() {
		return null;
	}

}
