package jp.go.nibiohn.bioinfo.client;

import java.util.List;

import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SampleInfoWidget extends Composite {

	private GutFloraResources resources = GWT.create(GutFloraResources.class);

	private final GutFloraServiceAsync service = GWT.create(GutFloraService.class);
	
	private Label loadingLabel = new Label("Retrieving Sample info...");
	private Label noParaInfoLabel = new Label("No phenotype parameter data.");
	private Label noReadInfoLabel = new Label("No gut microbiota data.");
	private SimplePanel sampleInfoPanel = new SimplePanel();
	private SimplePanel diversityPanel = new SimplePanel();
	private SimplePanel readPanel = new SimplePanel();
	private SimplePanel dietPanel = new SimplePanel();

	private ListBox rankListBox = new ListBox();
	
	private boolean readInfoDisplay = true;
	private boolean dietInfoDisplay = false;
	
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
		readHp.add(readChevron);
		readHp.add(new HTML("<h3>Gut microbiota composition</h3>"));
		thisWidget.add(readHp);
		thisWidget.add(diversityPanel);
		thisWidget.add(readPanel);
		readPanel.add(loadingLabel);
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
				dietPanel.setVisible(dietInfoDisplay);
			}
		});
		dietHp.add(dietChevron);
		dietHp.add(new HTML("<h3>Phenotype parameters</h3>"));
		thisWidget.add(dietHp);
		thisWidget.add(dietPanel);
		dietPanel.add(loadingLabel);
		dietPanel.setVisible(dietInfoDisplay);
		getProfileData(sampleId);

		thisWidget.add(new HTML("<div>&nbsp;</div>"));
		initWidget(thisWidget);
	}
	
	private void getSampleInfo(String sampleId) {
		service.getSampleEntry(sampleId, currentLang, new AsyncCallback<SampleEntry>() {
			
			@Override
			public void onSuccess(SampleEntry entry) {
				sampleInfoPanel.setWidget(new HTML("<h3 class=\"sampleInfoHeader\">Sample ID: " + entry.getSampleId() + "</h3>"));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				sampleInfoPanel.setWidget(new Label(FlowableWidget.SERVER_ERROR));			}
		});
		
		service.getSampleDiversity(sampleId, new AsyncCallback<List<String>>() {
			
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
					diversityPanel.setWidget(vp);
				} else {
					// just let it blank...
					diversityPanel.setWidget(new Label("No diversity data"));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				diversityPanel.setWidget(new Label(FlowableWidget.SERVER_ERROR));
				
			}
		});
	}

	private void getMicrobiotaInfo(String sampleId) {
		
		String rank = rankListBox.getValue(rankListBox.getSelectedIndex());
		service.getMicrobiota(sampleId, rank, new AsyncCallback<List<List<String>>>() {
			
			@Override
			public void onSuccess(List<List<String>> result) {
				if (result == null) {
					readPanel.setWidget(new Label(FlowableWidget.SERVER_ERROR));
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
					VerticalPanel vp = new VerticalPanel();
					vp.add(new HTML("&nbsp;"));
					vp.add(rankListBox);
					vp.add(new HTML(sb.toString()));
					readPanel.setWidget(vp);
					
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				dietPanel.setWidget(new Label(FlowableWidget.SERVER_ERROR));
			}
		});
	}
	
	private void getProfileData(String sampleId) {
		service.getSampleProfile(sampleId, currentLang, new AsyncCallback<List<List<String>>>() {
			
			@Override
			public void onSuccess(List<List<String>> result) {
				if (result == null) {
					dietPanel.setWidget(new Label(FlowableWidget.SERVER_ERROR));
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
				dietPanel.setWidget(new Label(FlowableWidget.SERVER_ERROR));
			}
		});
	}
}
