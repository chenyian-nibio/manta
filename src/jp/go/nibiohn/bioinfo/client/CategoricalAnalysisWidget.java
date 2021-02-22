package jp.go.nibiohn.bioinfo.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

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
public class CategoricalAnalysisWidget extends AnalysisWidget {
	
	private static final String OPTION_PARAMETER = "Parameter";
	private ListBox rankListBoxX = new ListBox();
	private ListBox rankListBoxY = new ListBox();
	private ListBox paraListBoxX = new ListBox();
	private ListBox paraListBoxY = new ListBox();
	
	private SimplePanel corrValuePanel = new SimplePanel();

	private SimplePanel chartInfoPanel = new SimplePanel();
	private SimplePanel chartPanel = new SimplePanel();
	
	private List<String> valueXList = new ArrayList<String>();
	private List<String> valueYList = new ArrayList<String>();
	private Map<String, String> choiceMap = new HashMap<String, String>();
	
	private Map<String, String> parameterUnitMap = new HashMap<String, String>();
	
	public CategoricalAnalysisWidget(Set<SampleEntry> selectedSamples, String lang) {
		this.selectedSamples = selectedSamples;
		this.currentLang = lang;
		
		HorizontalPanel topHp = new HorizontalPanel();
		DecoratorPanel pairSelectionDec = new DecoratorPanel();
		pairSelectionDec.setTitle("Analysis pair selection");
		pairSelectionDec.addStyleName("optionDec");
		SimplePanel pairSelectionSp = new SimplePanel();
		pairSelectionSp.setWidth("600px");
		pairSelectionDec.add(pairSelectionSp);
		
		VerticalPanel parameterVp = new VerticalPanel();
		
		HorizontalPanel pairSelectionHp1 = new HorizontalPanel();
		pairSelectionHp1.setSpacing(6);
		pairSelectionHp1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		HorizontalPanel pairSelectionHp2 = new HorizontalPanel();
		pairSelectionHp2.setSpacing(6);
		pairSelectionHp2.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		rankListBoxX.setWidth("200px");
		for (int k = 0; k < GutFloraConstant.RANK_LIST.size(); k++) {
			rankListBoxX.addItem(GutFloraConstant.RANK_LIST.get(k));
		}
		rankListBoxX.addItem(OPTION_PARAMETER);
		rankListBoxX.setSelectedIndex(1);
		
		rankListBoxY.setWidth("200px");
		rankListBoxY.addItem("Categorical parameter");
		rankListBoxY.setEnabled(false);

		rankListBoxX.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				chartPanel.clear();
				updateReadListBox();
			}
		});

		paraListBoxX.addItem("");
		paraListBoxX.setWidth("300px");

		pairSelectionHp1.add(rankListBoxX);
		pairSelectionHp1.add(paraListBoxX);
		
		paraListBoxY.addItem("");
		paraListBoxY.setWidth("300px");
		
		pairSelectionHp2.add(rankListBoxY);
		pairSelectionHp2.add(paraListBoxY);

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
		Label corrLabel = new Label("Difference");
		corrLabel.setStyleName("corrLabel");
		corrLablePanel.add(corrLabel);
		corrLablePanel.add(corrValuePanel);
		corrValuePanel.setWidget(new Label("-.--"));
		corrValuePanel.setStyleName("pvalueLabel");

		selectHp.add(corrLablePanel);
		corrValueDec.add(selectHp);

		VerticalPanel vp = new VerticalPanel();
		topHp.add(pairSelectionDec);
		topHp.add(corrValueDec);
		vp.add(topHp);
		
		vp.add(chartInfoPanel);
		vp.add(chartPanel);
		
		updateReadListBox();
		updateProfileListBox();
		
		paraListBoxX.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				String rank = rankListBoxX.getSelectedItemText();
				if (rank.equals(OPTION_PARAMETER)) {
					String paraId = paraListBoxX.getSelectedValue();
					if (paraId != null && !paraId.equals("")) {
						service.getNumericParameterValueById(CategoricalAnalysisWidget.this.selectedSamples, paraId, new AsyncCallback<PairListData>() {
							
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
						service.getReadsAndPctListById(CategoricalAnalysisWidget.this.selectedSamples, rank, taxonId, new AsyncCallback<PairListData>() {
							
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
				String paraId = paraListBoxY.getSelectedValue();
				if (paraId != null && !paraId.equals("")) {
					service.getStringParameterValueById(CategoricalAnalysisWidget.this.selectedSamples, paraId, new AsyncCallback<PairListData>() {
						
						@Override
						public void onSuccess(PairListData result) {
							valueYList = result.getOriginalList();
							choiceMap = result.getMetaDataMap();
							updateResults();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							warnMessage(BaseWidget.SERVER_ERROR);
						}
					});
				}
			}
		});

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
		service.getAllUnrankedCategoryParameterEntry(currentLang, new AsyncCallback<List<ParameterEntry>>() {
			
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
	}

	private void updateResults() {
		clearMessage();
		if (valueXList.size() != 0 && valueXList.size() == valueYList.size()) {
			Map<String, List<String>> groupValue = new HashMap<String, List<String>>();
			int numOfNull = 0;
			for (int i = 0; i < valueYList.size(); i++) {
				String y = valueYList.get(i);
				if (y != null && !y.equals("")) {
					if (groupValue.get(y) == null) {
						groupValue.put(y, new ArrayList<String>());
					}
					String x = valueXList.get(i);
					groupValue.get(y).add(x);
				} else {
					numOfNull++;
				}
			}
			chartInfoPanel.setWidget(
					new Label("Number of null values: " + numOfNull + " ( " + selectedSamples.size() + " total )"));
			service.getFormattedPvalueForUnrankedCategoricalParameter(groupValue, new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String result) {
					corrValuePanel.setWidget(new HTML(result));
				}
				
				@Override
				public void onFailure(Throwable caught) {
					warnMessage(BaseWidget.SERVER_ERROR);
				}
			});
			
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
			
			service.plotBarChartWithErrorBars(groupValue, choiceMap, head1, currentLang, new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String result) {
					chartPanel.setWidget(new HTML(result));
				}
				
				@Override
				public void onFailure(Throwable caught) {
					warnMessage(BaseWidget.SERVER_ERROR);
				}
			});
		}
	}

}
