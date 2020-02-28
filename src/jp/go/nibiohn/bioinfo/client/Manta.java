package jp.go.nibiohn.bioinfo.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

import jp.go.nibiohn.bioinfo.shared.GutFloraConfig;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import jp.go.nibiohn.bioinfo.shared.SearchResultData;

/**
 * 
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Manta extends BasePage {

	private static final String WELCOME_MSG = "Select samples and click the 'Start' button to start analysis.";

	private final GutFloraServiceAsync service = GWT.create(GutFloraService.class);

	private List<BaseWidget> widgetTrails = new ArrayList<BaseWidget>();

	private SampleListWidget sampleListWidget;

	private SampleAnalysisWidget analysisWidget;
	
	private SampleAnalysisWidget subsetAnalysisWidget;
	
	private ReadVisualizeWidget readVisualizeWidget;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		init();

		History.addValueChangeHandler(this);

		String initToken = History.getToken();
		if (initToken.length() == 0) {
			// use English by default
			History.newItem(GutFloraConstant.LANG_EN + GutFloraConstant.NAVI_LINK_SAMPLE);
		}
		
		History.fireCurrentHistoryState();

		VerticalPanel footer = new VerticalPanel();
		footer.setWidth("100%");
		footer.add(new HTML("<p><hr style=\"margin-top: 48px;\"/></p>"));
		footer.add(new HTML("<p style=\"text-align: center;\" class=\"fixlink\">" + GutFloraConfig.FOOTER + "</p>"));
		infoPanel.add(footer);
		
		getUserInfo();
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		if (hasMessage) {
			mesgPanel.setVisible(true);
			hasMessage = false;
		} else {
			setMessageInvisible();
		}

		String option = event.getValue();
		String value = option.substring(3);
		// TODO the language switch will be only appeared at sample selection page (home), so far...
		String lang = option.substring(0, 3);
		this.currentLang = lang;
		RootPanel langLabelPanel = RootPanel.get("topLang");
		langLabelPanel.clear();
		if (value.equals(GutFloraConstant.NAVI_LINK_SAMPLE)) {
			if (lang.equals(GutFloraConstant.LANG_EN)) {
				Hyperlink langLink = new Hyperlink("日本語", GutFloraConstant.LANG_JP + GutFloraConstant.NAVI_LINK_SAMPLE);
				langLink.addStyleName("langFlagJp");
				langLabelPanel.add(langLink);
			} else {
				Hyperlink langLink = new Hyperlink("English", GutFloraConstant.LANG_EN + GutFloraConstant.NAVI_LINK_SAMPLE);
				langLink.addStyleName("langFlagGb");
				langLabelPanel.add(langLink);
			}
			
			// TODO should provide different language
			infoMessage(WELCOME_MSG);
			service.getSampleEntryList(currentLang, new AsyncCallback<List<SampleEntry>>() {
				
				@Override
				public void onSuccess(List<SampleEntry> result) {
					widgetTrails.clear();
					
					sampleListWidget = new SampleListWidget(result, currentLang);
					widgetTrails.add(sampleListWidget);
					
					// force to new a analysisWidget
					analysisWidget = null;
					
					mainPanel.clear();
					mainPanel.add(sampleListWidget);
					
					infoPanel.setVisible(true);
					
					setNaviBar();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					warnMessage(SERVER_ERROR);
				}
			});
			
		// TODO to be improved
		} else if (value.endsWith(GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX)) {
			if (value.startsWith(GutFloraConstant.NAVI_LINK_ANALYSIS)) {
				if (readVisualizeWidget == null) {
					History.newItem(currentLang + GutFloraConstant.NAVI_LINK_ANALYSIS);
					return;
				} else {
					String sampleIdString = readVisualizeWidget.getSubsetSampleIdString();
					if (sampleIdString == null || sampleIdString.equals("")) {
						History.newItem(currentLang + GutFloraConstant.NAVI_LINK_ANALYSIS);
						return;
					}
					service.getSelectedSampleEntrySet(sampleIdString, currentLang, new AsyncCallback<Set<SampleEntry>>() {
						
						@Override
						public void onSuccess(Set<SampleEntry> result) {
							if (subsetAnalysisWidget == null) {
								String selectedRank = readVisualizeWidget.getSelectedRank();
								subsetAnalysisWidget = new SampleAnalysisWidget("Selected subset",
										GutFloraConstant.NAVI_LINK_ANALYSIS + GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX,
										result, selectedRank, currentLang);
							}

							Iterator<BaseWidget> iterator = widgetTrails.iterator();
							List<BaseWidget> wList = new ArrayList<BaseWidget>();
							while (iterator.hasNext()) {
								BaseWidget widget = iterator.next();
								if (widget.name.equals(subsetAnalysisWidget.name)){
									break;
								}
								wList.add(widget);
							}
							widgetTrails = wList;
							widgetTrails.add(subsetAnalysisWidget);
							
							mainPanel.clear();
							mainPanel.add(subsetAnalysisWidget);
							
							setNaviBar();
							
							infoPanel.setVisible(false);
						}
						
						@Override
						public void onFailure(Throwable caught) {
							warnMessage(SERVER_ERROR);
						}
					});
				}
				
			} else if (value.startsWith(GutFloraConstant.NAVI_LINK_SEARCH)) {
				if (subsetAnalysisWidget == null) {
					// TODO to be defined!
					History.newItem(currentLang + GutFloraConstant.NAVI_LINK_ANALYSIS + GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX);
					return;
				} else {
					int tabIndex = 0;
					if (value.endsWith(GutFloraConstant.NAVI_LINK_SUFFIX_PROFILE + GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX)) {
						tabIndex = 1;
					} else if (value.endsWith(GutFloraConstant.NAVI_LINK_SUFFIX_IMMUN + GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX)) {
						tabIndex = 2;
					}
					SearchResultData searchResultData = subsetAnalysisWidget.getCorrectionResults(tabIndex);
					
					if (searchResultData == null) {
						// TODO to be defined!
						History.newItem(currentLang + GutFloraConstant.NAVI_LINK_ANALYSIS + GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX);
						return;
					}
					// TODO uncheck!
					Set<SampleEntry> selectedSamples = subsetAnalysisWidget.getSelectedSamples();
					BaseWidget resultWidget;
					if (value.contains(GutFloraConstant.NAVI_LINK_MLR + "-")) {
						List<String> currentColumns = analysisWidget.getReadsAnalysisWidget().getCurrentColumns();
						resultWidget = new MlrSearchResultWidget(selectedSamples, searchResultData, currentColumns, value, currentLang);
					} else {
						resultWidget = new SearchResultWidget(selectedSamples, searchResultData, value, currentLang);
					}
					widgetTrails.add(resultWidget);

					mainPanel.clear();
					mainPanel.add(resultWidget);
					
					setNaviBar();
					
					infoPanel.setVisible(false);
				}
			} else if (value.startsWith(GutFloraConstant.NAVI_LINK_VIEW_BARCHART)) {
				if (subsetAnalysisWidget == null) {
					History.newItem(currentLang + GutFloraConstant.NAVI_LINK_ANALYSIS + GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX);
					return;
				} else {
					ReadsAnalysisWidget readWidget = subsetAnalysisWidget.getReadsAnalysisWidget();
					ClusteredBarChartWidget subsetClusteredBarChartWidget = new ClusteredBarChartWidget(
							"Subset Clustering", GutFloraConstant.NAVI_LINK_VIEW_BARCHART
									+ GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX,
									readWidget.getSelectedSamples(), readWidget.getSelectedRank(),
									true, currentLang);
					
					// subsetClusteredBarChart so far is always the last widget
					widgetTrails.add(subsetClusteredBarChartWidget);
					
					mainPanel.clear();
					mainPanel.add(subsetClusteredBarChartWidget);
					
					setNaviBar();
				}
			} else if (value.startsWith(GutFloraConstant.NAVI_LINK_VIEW_HEATMAP)) {
				if (subsetAnalysisWidget == null) {
					History.newItem(currentLang + GutFloraConstant.NAVI_LINK_ANALYSIS + GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX);
					return;
				} else {
					ReadsAnalysisWidget readWidget = subsetAnalysisWidget.getReadsAnalysisWidget();
					MicrobiotaHeatmapWidget subsetMicrobiotaHeatmapWidget = new MicrobiotaHeatmapWidget(
							"Subset Clustering", GutFloraConstant.NAVI_LINK_VIEW_BARCHART
							+ GutFloraConstant.NAVI_LINK_SUBSET_SUFFIX,
							readWidget.getSelectedSamples(), readWidget.getSelectedRank(),
							true, currentLang);
					
					// subsetClusteredBarChart so far is always the last widget
					widgetTrails.add(subsetMicrobiotaHeatmapWidget);
					
					mainPanel.clear();
					mainPanel.add(subsetMicrobiotaHeatmapWidget);
					
					setNaviBar();
				}
			} else {
				warnMessage("Illegal URL.");
				History.newItem(currentLang + GutFloraConstant.NAVI_LINK_SAMPLE);
			}	

		} else if (value.equals(GutFloraConstant.NAVI_LINK_ANALYSIS)) {
			if (analysisWidget == null) {
				if (sampleListWidget == null) {
					History.newItem(currentLang + GutFloraConstant.NAVI_LINK_SAMPLE);
					return;
				} else {
					Set<SampleEntry> selectedSamples = sampleListWidget.getSelectedSamples();
					// should not happen?
					if (selectedSamples == null || selectedSamples.size() == 0) {
						warnMessage("No sample was selected.");
						hasMessage = true;
						History.newItem(currentLang + GutFloraConstant.NAVI_LINK_SAMPLE);
						return;
					}
					
					analysisWidget = new SampleAnalysisWidget(selectedSamples, currentLang);
					
				}
			}
			Iterator<BaseWidget> iterator = widgetTrails.iterator();
			List<BaseWidget> wList = new ArrayList<BaseWidget>();
			while (iterator.hasNext()) {
				BaseWidget widget = iterator.next();
				if (widget.name.equals(analysisWidget.name)){
					break;
				}
				wList.add(widget);
			}
			widgetTrails = wList;
			widgetTrails.add(analysisWidget);
			
			mainPanel.clear();
			mainPanel.add(analysisWidget);
			
			setNaviBar();
			
			// force to new a bar chart widget when request
			readVisualizeWidget = null;
			subsetAnalysisWidget = null;
			
			infoPanel.setVisible(false);
		} else if (value.startsWith(GutFloraConstant.NAVI_LINK_SEARCH)) {
			if (analysisWidget == null) {
				History.newItem(currentLang + GutFloraConstant.NAVI_LINK_ANALYSIS);
				return;
			} else {
				int tabIndex = 0;
				if (value.endsWith(GutFloraConstant.NAVI_LINK_SUFFIX_PROFILE)) {
					tabIndex = 1;
				} else if (value.endsWith(GutFloraConstant.NAVI_LINK_SUFFIX_IMMUN)) {
					tabIndex = 2;
				}
				SearchResultData searchResultData = analysisWidget.getCorrectionResults(tabIndex);
				
				if (searchResultData == null) {
					History.newItem(currentLang + GutFloraConstant.NAVI_LINK_ANALYSIS);
					return;
				}
				// TODO uncheck!
				Set<SampleEntry> selectedSamples = analysisWidget.getSelectedSamples();
				BaseWidget resultWidget;
				if (value.contains(GutFloraConstant.NAVI_LINK_MLR + "-")) {
					List<String> currentColumns = analysisWidget.getReadsAnalysisWidget().getCurrentColumns();
					resultWidget = new MlrSearchResultWidget(selectedSamples, searchResultData, currentColumns, value, currentLang);
				} else {
					resultWidget = new SearchResultWidget(selectedSamples, searchResultData, value, currentLang);
				}
				
				widgetTrails.add(resultWidget);

				mainPanel.clear();
				mainPanel.add(resultWidget);
				
				setNaviBar();
				
				infoPanel.setVisible(false);
			}
		} else if (value.equals(GutFloraConstant.NAVI_LINK_VIEW_BARCHART)) {
			if (analysisWidget == null) {
				History.newItem(currentLang + GutFloraConstant.NAVI_LINK_ANALYSIS);
				return;
			} else {
				if (readVisualizeWidget == null) {
					ReadsAnalysisWidget readWidget = analysisWidget.getReadsAnalysisWidget();
					readVisualizeWidget = new ClusteredBarChartWidget(readWidget.getSelectedSamples(),
							readWidget.getSelectedRank(), false, currentLang);
				}
				
				Iterator<BaseWidget> iterator = widgetTrails.iterator();
				List<BaseWidget> wList = new ArrayList<BaseWidget>();
				while (iterator.hasNext()) {
					BaseWidget widget = iterator.next();
					if (widget.name.equals(readVisualizeWidget.name)){
						break;
					}
					wList.add(widget);
				}
				widgetTrails = wList;
				widgetTrails.add(readVisualizeWidget);
				
				mainPanel.clear();
				mainPanel.add(readVisualizeWidget);
				
				setNaviBar();
				
				subsetAnalysisWidget = null;
			}
		} else if (value.equals(GutFloraConstant.NAVI_LINK_VIEW_HEATMAP)) {
			if (analysisWidget == null) {
				History.newItem(currentLang + GutFloraConstant.NAVI_LINK_ANALYSIS);
				return;
			} else {
				if (readVisualizeWidget == null) {
					ReadsAnalysisWidget readWidget = analysisWidget.getReadsAnalysisWidget();
					readVisualizeWidget = new MicrobiotaHeatmapWidget(readWidget.getSelectedSamples(),
							readWidget.getSelectedRank(), false, currentLang);
				}
				
				Iterator<BaseWidget> iterator = widgetTrails.iterator();
				List<BaseWidget> wList = new ArrayList<BaseWidget>();
				while (iterator.hasNext()) {
					BaseWidget widget = iterator.next();
					if (widget.name.equals(readVisualizeWidget.name)){
						break;
					}
					wList.add(widget);
				}
				widgetTrails = wList;
				widgetTrails.add(readVisualizeWidget);
				
				mainPanel.clear();
				mainPanel.add(readVisualizeWidget);
				
				setNaviBar();
				
				subsetAnalysisWidget = null;
			}
		} else if (value.equals(GutFloraConstant.NAVI_LINK_VIEW_PCOA)) {
			if (analysisWidget == null) {
				History.newItem(currentLang + GutFloraConstant.NAVI_LINK_ANALYSIS);
				return;
			} else {
				if (readVisualizeWidget == null) {
					ReadsAnalysisWidget readWidget = analysisWidget.getReadsAnalysisWidget();
					readVisualizeWidget = new PcoaAnalysisWidget(readWidget.getSelectedSamples(), false, currentLang);
				}
				
				Iterator<BaseWidget> iterator = widgetTrails.iterator();
				List<BaseWidget> wList = new ArrayList<BaseWidget>();
				while (iterator.hasNext()) {
					BaseWidget widget = iterator.next();
					if (widget.name.equals(readVisualizeWidget.name)){
						break;
					}
					wList.add(widget);
				}
				widgetTrails = wList;
				widgetTrails.add(readVisualizeWidget);
				
				mainPanel.clear();
				mainPanel.add(readVisualizeWidget);
				
				setNaviBar();
				
				subsetAnalysisWidget = null;
			}
		} else {
			warnMessage("Illegal URL.");
			// choose English as default
			History.newItem(GutFloraConstant.LANG_EN + GutFloraConstant.NAVI_LINK_SAMPLE);
		}
		
	}

	private void setNaviBar() {
		HorizontalPanel naviBar = new HorizontalPanel();
		
		Iterator<BaseWidget> iterator = widgetTrails.iterator();
		while (iterator.hasNext()) {
			BaseWidget widget = iterator.next();
			Hyperlink link = new Hyperlink(widget.name, widget.link);
			link.setStyleName("naviLink");
			naviBar.add(link);
			if (iterator.hasNext()) {
				naviBar.add(new Label(">>"));
			}
		}
		naviPanel.clear();
		naviPanel.add(naviBar);
		
	}

	private void getUserInfo() {
		service.getCurrentUser(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String currentUser) {
				if(currentUser == "Guest") {
					createSignUpButton();
				} else {
					RootPanel.get("sighUp").clear();
				}
				RootPanel userInfo = RootPanel.get("userInfo");
				userInfo.clear(true);
				HTMLPanel userPanel = new HTMLPanel("");
				userInfo.add(userPanel);
				userPanel.add(new Label(currentUser));
			}

			@Override
			public void onFailure(Throwable caught) {
				warnMessage(SERVER_ERROR);
			}
		});
		createAuthButton();
	}

	private void createSignUpButton() {
		final Button signUpBtn = new Button("Sign Up", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DialogBox dialogBox = createAuthDialogBox("Sign Up");
				dialogBox.setGlassEnabled(true);
				dialogBox.setAnimationEnabled(true);
				dialogBox.setAutoHideEnabled(false);
				dialogBox.center();
				userIdTb.setFocus(true);
			}
		});
		RootPanel.get("sighUp").add(signUpBtn);
	}

	private void createAuthButton() {
		service.getCurrentUser(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String currentUser) {
				RootPanel.get("Auth").clear();
				if (currentUser != "Guest") {
					final Button LogoutBtn = new Button("Logout", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							DialogBox dialogBox = createLogoutDialogBox();
							dialogBox.setGlassEnabled(true);
							dialogBox.setAnimationEnabled(true);
							dialogBox.setAutoHideEnabled(false);
							dialogBox.center();
						}
					});
					RootPanel.get("Auth").add(LogoutBtn);
				} else {
					final Button LoginBtn = new Button("Login", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							DialogBox dialogBox = createAuthDialogBox("Login");
							dialogBox.setGlassEnabled(true);
							dialogBox.setAnimationEnabled(true);
							dialogBox.setAutoHideEnabled(false);
							dialogBox.center();
							userIdTb.setFocus(true);
						}
					});
					RootPanel.get("Auth").add(LoginBtn);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				warnMessage(SERVER_ERROR);
			}
		});
	}
	
	private TextBox userIdTb = new TextBox();
	private DialogBox createAuthDialogBox(final String type) {
		// Create a dialog box and set the caption text
		final DialogBox dialogBox = new DialogBox(true);
		dialogBox.setText(type);
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(6);
		vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		final Label infoLabel = new Label("Input your ID & password:");
		infoLabel.setStyleName("authInfo");
		vp.add(infoLabel);

		userIdTb.setText("");
		userIdTb.setSize("150px", "18px");
		final TextBox passwordTb = new PasswordTextBox();
		passwordTb.setSize("150px", "18px");
		final TextBox passwordConfirmTb = new PasswordTextBox();
		passwordConfirmTb.setSize("150px", "18px");

		Button authBtn = new Button(type, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				String username = userIdTb.getText();
				String password = passwordTb.getText();
				String passwordConfirm = passwordConfirmTb.getText();

				if (username == "" || password == "" || (type == "Sign Up" && passwordConfirm == "")) {
					infoLabel.setText("ERROR! Please fill in all input fields.");
					infoLabel.setStyleName("authError");
				} else if (password.length() < 8) {
					infoLabel.setText("password require at least 8 characters");
					infoLabel.setStyleName("authError");
				} else if (type == "Sign Up" && !password.equals(passwordConfirm)) {
					infoLabel.setText("Password and Password Confirm is Not Match");
					infoLabel.setStyleName("authError");
				} else {
					if (type == "Sign Up") {
						service.createUser(username, password, passwordConfirm, new AsyncCallback<String>() {
							@Override
							public void onSuccess(String result) {
								if (result.equals("success")) {
									getUserInfo();
									dialogBox.hide();
									History.newItem(currentLang + GutFloraConstant.NAVI_LINK_SAMPLE);
									History.fireCurrentHistoryState();
								} else {
									infoLabel.setText(result);
									infoLabel.setStyleName("authError");
								}
							}

							@Override
							public void onFailure(Throwable caught) {
								infoLabel.setText(SERVER_ERROR);
								infoLabel.setStyleName("authError");
							}
						});
					} else {
						service.loginUser(username, password, new AsyncCallback<Boolean>() {
							@Override
							public void onSuccess(Boolean result) {
								if (result.booleanValue()) {
									getUserInfo();
									dialogBox.hide();
									History.newItem(currentLang + GutFloraConstant.NAVI_LINK_SAMPLE);
									History.fireCurrentHistoryState();
								} else {
									infoLabel.setText("ERROR! Incorrect ID or password.");
									infoLabel.setStyleName("authError");
								}
							}

							@Override
							public void onFailure(Throwable caught) {
								infoLabel.setText(SERVER_ERROR);
								infoLabel.setStyleName("authError");
							}
						});
					}
				}
			}
		});
		Button cancelBtn = new Button("Cancel", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});
		authBtn.setWidth("80px");
		cancelBtn.setWidth("80px");

		int row_number = type == "Sign Up" ? 3 : 2;
		Grid grid = new Grid(row_number, 2);
		Label idLabel = new Label("User ID:");
		String setWidth = type == "Sign Up" ? "120px" : "80px";
		idLabel.setWidth(setWidth);
		idLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		grid.setWidget(0, 0, idLabel);
		grid.setWidget(0, 1, userIdTb);
		Label pwLabel = new Label("Password:");
		pwLabel.setWidth(setWidth);
		pwLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		grid.setWidget(1, 0, pwLabel);
		grid.setWidget(1, 1, passwordTb);
		if (type == "Sign Up") {
			Label pwConfirmLabel = new Label("Password Confirm:");
			pwConfirmLabel.setWidth(setWidth);
			pwConfirmLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			grid.setWidget(2, 0, pwConfirmLabel);
			grid.setWidget(2, 1, passwordConfirmTb);
		}

		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(6);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hp.add(authBtn);
		hp.add(cancelBtn);

		vp.add(grid);
		vp.add(hp);

		dialogBox.add(vp);
		// Return the dialog box
		return dialogBox;
	}
	
	private DialogBox createLogoutDialogBox() {
		// Create a dialog box and set the caption text
		final DialogBox dialogBox = new DialogBox(true);
		dialogBox.setText("Logout");
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(16);
		vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		final Label infoLabel = new Label("Logout current user?");
		infoLabel.setStyleName("authInfo");
		vp.add(infoLabel);

		Button logoutBtn = new Button("Logout", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				service.logoutCurrentUser(new AsyncCallback<Void>() {
					
					@Override
					public void onSuccess(Void result) {
						getUserInfo();
						dialogBox.hide();
						History.newItem(currentLang + GutFloraConstant.NAVI_LINK_SAMPLE);
						History.fireCurrentHistoryState();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						infoLabel.setText("System ERROR!");
						infoLabel.setStyleName("authError");
					}
				});
			}
		});
		Button cancelBtn = new Button("Cancel", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});
		logoutBtn.setWidth("80px");
		cancelBtn.setWidth("80px");
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(6);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hp.add(logoutBtn);
		hp.add(cancelBtn);
		
		vp.add(hp);
		
		dialogBox.add(vp);
		// Return the dialog box
		return dialogBox;
	}
}
