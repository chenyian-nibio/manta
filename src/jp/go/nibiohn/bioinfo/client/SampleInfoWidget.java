package jp.go.nibiohn.bioinfo.client;

import java.util.List;

import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SampleInfoWidget extends Composite {

	private GutFloraResources resources = GWT.create(GutFloraResources.class);

	private final GutFloraServiceAsync service = GWT.create(GutFloraService.class);
	
	private Label loadingLabel = new Label("Retrieving Sample info...");
	private Label noParaInfoLabel = new Label("No diet and fitness parameters.");
	private Label noReadInfoLabel = new Label("No gut microbiota data.");
	private SimplePanel sampleInfoPanel = new SimplePanel();
	private SimplePanel diversityPanel = new SimplePanel();
	private SimplePanel readPanel = new SimplePanel();
	private SimplePanel dietPanel = new SimplePanel();

	private ListBox rankListBox = new ListBox();
	private ListBox dietListBox = new ListBox();
	private ListBox profileGroupListBox = new ListBox();
	private HorizontalPanel dietListHP = new HorizontalPanel();
	
	private boolean readInfoDisplay = true;
	private boolean dietInfoDisplay = false;

	private RadioButton exp16SRb = new RadioButton("expMethod", "16S");
	private RadioButton expShotgunRb = new RadioButton("expMethod", "Shotgun");

	private String currentLang;

	public SampleInfoWidget(final String sampleId, String lang) {
		this.currentLang = lang;

		VerticalPanel thisWidget = new VerticalPanel();
		
		thisWidget.add(sampleInfoPanel);
		getSampleInfo(sampleId);

		for (String r: GutFloraConstant.RANK_LIST) {
			rankListBox.addItem(r);
		}
		rankListBox.setSelectedIndex(1);
		
		rankListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				getMicrobiotaInfo(sampleId);
			}
		});
		
		HorizontalPanel readHp = new HorizontalPanel();
		readHp.setSpacing(6);
		readHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		final Image readChevron = new Image(resources.getChevronDownImageResource());
		readChevron.addStyleName("clickable");
		readChevron.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (readInfoDisplay) {
					readChevron.setResource(resources.getChevronRightImageResource());
				} else {
					readChevron.setResource(resources.getChevronDownImageResource());
				}
				readInfoDisplay = !readInfoDisplay;
				diversityPanel.setVisible(readInfoDisplay);
				readPanel.setVisible(readInfoDisplay);
			}
		});
		
		HorizontalPanel expMethodHp = new HorizontalPanel();
		expMethodHp.setSpacing(12);
		expMethodHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		expMethodHp.add(exp16SRb);
		expMethodHp.add(expShotgunRb);
		exp16SRb.setValue(Boolean.TRUE);
		exp16SRb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				getMicrobiotaInfo(sampleId);
			}
		});
		expShotgunRb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				getMicrobiotaInfo(sampleId);
			}
		});
		
		readHp.add(readChevron);
		readHp.add(new HTML("<h3>Microbiota</h3>"));
		thisWidget.add(readHp);
		thisWidget.add(expMethodHp);
		thisWidget.add(diversityPanel);
		thisWidget.add(readPanel);
		readPanel.add(loadingLabel);
		rankListBox.setVisible(readInfoDisplay);
		readPanel.setVisible(readInfoDisplay);
		getMicrobiotaInfo(sampleId);

		HorizontalPanel dietHp = new HorizontalPanel();
		dietHp.setSpacing(6);
		dietHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		// default is closed (right)
		final Image dietChevron = new Image(resources.getChevronRightImageResource());
		dietChevron.addStyleName("clickable");
		dietChevron.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (dietInfoDisplay) {
					dietChevron.setResource(resources.getChevronRightImageResource());
				} else {
					dietChevron.setResource(resources.getChevronDownImageResource());
				}
				dietInfoDisplay = !dietInfoDisplay;
				dietListHP.setVisible(dietInfoDisplay);
				dietPanel.setVisible(dietInfoDisplay);
			}
		});
		dietHp.add(dietChevron);
		dietHp.add(new HTML("<h3>Phenotypic parameters</h3>"));
		thisWidget.add(dietHp);
		dietListHP.add(dietListBox);
		dietListHP.add(new HTML("&nbsp;"));
		dietListHP.add(profileGroupListBox);
		thisWidget.add(dietListHP);
		thisWidget.add(dietPanel);
		dietPanel.add(loadingLabel);
		dietListHP.setVisible(dietInfoDisplay);
		dietPanel.setVisible(dietInfoDisplay);

		service.getAllParameterGroupNames(currentLang, new AsyncCallback<List<List<String>>>() {
			
			@Override
			public void onSuccess(List<List<String>> result) {
				dietListBox.clear();
				for (List<String> item : result) {
					dietListBox.addItem(item.get(0), item.get(1));
				}
				getProfileData(sampleId);
				getProfileGroup();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				dietPanel.add(new Label(BaseWidget.SERVER_ERROR));				
			}
		});
		
		dietListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				String selectedValue = dietListBox.getSelectedValue();
				if (selectedValue != null) {
					getProfileData(sampleId);
					getProfileGroup();
				}
			}
		});
		profileGroupListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				getProfileData(sampleId);
			}
		});
		
		thisWidget.add(new HTML("<div>&nbsp;</div>"));
		initWidget(thisWidget);
	}
	
	private void getSampleInfo(String sampleId) {
		service.getSampleEntry(sampleId, currentLang, new AsyncCallback<SampleEntry>() {
			
			@Override
			public void onSuccess(SampleEntry entry) {
				StringBuffer sb = new StringBuffer();
				sb.append("<table class=\"sampleInfo normalCol\" >\n");
				sb.append("<tr><th>Project</th><td colspan=\"2\">" + entry.getProject() + "</td></tr>");
				sb.append("<tr><th>Sample ID</th><td>" + entry.getSampleId() + "</td>");
				if (currentLang.equals(GutFloraConstant.LANG_JP)) {
					sb.append("<td colspan=\"2\">年齢 " + entry.getAge() + " 歳, " + entry.getGender() + "</td></tr>\n");
				} else {
					sb.append("<td colspan=\"2\">Age " + entry.getAge() + ", " + entry.getGender() + "</td></tr>\n");
				}
				sb.append("</table>");
				sampleInfoPanel.setWidget(new HTML(sb.toString()));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				sampleInfoPanel.setWidget(new Label(BaseWidget.SERVER_ERROR));			}
		});
	}
	
	private void getMicrobiotaInfo(String sampleId) {
		diversityPanel.clear();
		readPanel.clear();
		
		Integer experimentMethod = GutFloraConstant.EXPERIMENT_METHOD_SHOTGUN;
		if (exp16SRb.getValue()) {
			experimentMethod = GutFloraConstant.EXPERIMENT_METHOD_16S;
		}
		
		String rank = rankListBox.getValue(rankListBox.getSelectedIndex());
		service.getMicrobiota(sampleId, rank, experimentMethod, new AsyncCallback<List<List<String>>>() {
			
			@Override
			public void onSuccess(List<List<String>> result) {
				if (result == null) {
					readPanel.setWidget(new Label(BaseWidget.SERVER_ERROR));
				}
				if (result.size() == 0) {
					noReadInfoLabel.addStyleName("noINfoLabel");
					readPanel.setWidget(noReadInfoLabel);
					rankListBox.setVisible(false);
				} else {
					StringBuffer sb = new StringBuffer();
					sb.append("<div class=\"longTable\">\n");
					sb.append("<table class=\"sampleInfo wideCol\">\n");
					sb.append("<tr><td>&nbsp;</td><th>counts / 10,000 reads</th></tr>\n");
					for (List<String> row: result) {
						sb.append("<tr><th title=\"TaxonID: " + row.get(2)
								+ "\">" + row.get(0) + "</th><td>" + row.get(1) + "</td></tr>\n");
					}
					sb.append("</table>\n");
					sb.append("</div>\n");
					readPanel.setWidget(new HTML(sb.toString()));
					
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				dietPanel.setWidget(new Label(BaseWidget.SERVER_ERROR));
			}
		});
		
		service.getSampleDiversity(sampleId, experimentMethod, new AsyncCallback<List<String>>() {
			
			@Override
			public void onSuccess(List<String> result) {
				if (result != null) {
					VerticalPanel vp = new VerticalPanel();
					vp.add(new HTML("<b>Diversity index:</b>"));
					StringBuffer sb = new StringBuffer();
					sb.append("<table class=\"sampleInfo diverCol\" >\n");
					sb.append("<tr>");
					sb.append("<th>" + GutFloraConstant.DIVERSITY_INDEX[0] + "</th><td>" + result.get(0) + "</td>");
					sb.append("<th>" + GutFloraConstant.DIVERSITY_INDEX[1] + "</th><td>" + result.get(1) + "</td>");
					sb.append("<th>" + GutFloraConstant.DIVERSITY_INDEX[2] + "</th><td>" + result.get(2) + "</td>");
					sb.append("</tr></table>");
					vp.add(new HTML(sb.toString()));
					vp.add(new HTML("&nbsp;"));
					vp.add(rankListBox);
					diversityPanel.setWidget(vp);
				} else {
					// just let it blank...
					
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				diversityPanel.setWidget(new Label(BaseWidget.SERVER_ERROR));
				
			}
		});

	}
	
	private void getProfileData(String sampleId) {
		String categoryId = dietListBox.getSelectedValue();
		String groupId = profileGroupListBox.getSelectedValue();
		service.getSampleProfile(sampleId, categoryId, groupId, currentLang, new AsyncCallback<List<List<String>>>() {
			
			@Override
			public void onSuccess(List<List<String>> result) {
				if (result == null) {
					dietPanel.setWidget(new Label(BaseWidget.SERVER_ERROR));
				}
				if (result.size() == 0) {
					noParaInfoLabel.addStyleName("noINfoLabel");
					dietPanel.setWidget(noParaInfoLabel);
				} else {
					StringBuffer sb = new StringBuffer();
					sb.append("<div class=\"longTable\">\n");
					sb.append("<table class=\"sampleInfo wideCol\">\n");
					for (List<String> row: result) {
						if (row.get(2) == null || row.get(2).equals("") || row.get(1).equals("-")) {
							if (row.get(1).startsWith("RGB#")) {
								sb.append("<tr><th>" + row.get(0).substring(0, row.get(0).length() - 6) + "</th><td colspan=\"2\"><div style=\"width: 36px; background: "
										+ row.get(1).substring(3) + "\">&nbsp;</div></td></tr>\n");
							} else {
								sb.append("<tr><th>" + row.get(0) + "</th><td colspan=\"2\">" + row.get(1) + "</td></tr>\n");
							}
						} else {
							sb.append("<tr><th>" + row.get(0) + "</th><td>" + row.get(1) + "</td><td>"
									+ row.get(2) + "</td></tr>\n");
						}
					}
					sb.append("</table>\n");
					sb.append("</div>\n");
					dietPanel.setWidget(new HTML(sb.toString()));
					
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				dietPanel.setWidget(new Label(BaseWidget.SERVER_ERROR));
			}
		});
	}

	protected void getProfileGroup() {
		String categoryId = dietListBox.getSelectedValue();
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
				// TODO Auto-generated method stub
			}
		});
	}

}
