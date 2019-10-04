package jp.go.nibiohn.bioinfo.client.manage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import jp.go.nibiohn.bioinfo.client.GutFloraResources;

public class DeleteAllContentsWidget extends ManageWidget {

	private GutFloraResources resources = GWT.create(GutFloraResources.class);
	
	private DialogBox dialogBox = new DialogBox();
	
	private PopupPanel loadingPopupPanel = new PopupPanel();

	private TextBox textBox = new TextBox();
	
	public DeleteAllContentsWidget() {
		VerticalPanel thisWidget = new VerticalPanel();
		thisWidget.add(new HTML("<h3>Danger zone:</h3>"));
		
		DecoratorPanel deleteAllContentsDec = new DecoratorPanel();
		deleteAllContentsDec.setTitle("Delete all contents");
		deleteAllContentsDec.addStyleName("optionDec");
		
		HorizontalPanel deleteButtonHp = new HorizontalPanel();
		deleteButtonHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		deleteButtonHp.setSpacing(12);
		
		deleteButtonHp.add(new Image(resources.getWarningIconLargeImageResource()));
		deleteButtonHp.add(new Label("Delete all contents in the database: "));
		Button button = new Button("DELETE", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				clearMessage();
				
				createConfirmDialogBox();
				
				dialogBox.center();
			}
		});
		deleteButtonHp.add(button);
		
		deleteAllContentsDec.add(deleteButtonHp);
		thisWidget.add(deleteAllContentsDec);

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
	}
	
	private void createConfirmDialogBox() {
		dialogBox.ensureDebugId("previewDialogBox");
		dialogBox.setText("Warning");
		
		VerticalPanel dialogContents = new VerticalPanel();
		dialogContents.setSpacing(4);
		dialogBox.setWidget(dialogContents);
		
		Label warnlabel = new Label("This action is irreversible.");
		warnlabel.setStyleName("warningMessage");
		
		Label infoLabel = new Label("You are trying to delete the whole database.");
		infoLabel.setStyleName("altPanel");
		
		dialogContents.add(warnlabel);
		dialogContents.add(infoLabel);
		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(12);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hp.add(new Label("Are you sure?"));
		textBox.setWidth("80px");
		hp.add(textBox);
		dialogContents.add(hp);
		dialogContents.setCellHorizontalAlignment(hp, HasHorizontalAlignment.ALIGN_CENTER);
		
		HorizontalPanel buttonHp = new HorizontalPanel();
		buttonHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		buttonHp.setSpacing(12);
		
		Button cancelButton = new Button("Cancel", new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});
		cancelButton.setWidth("100px");
		buttonHp.add(cancelButton);
		
		Button okButton = new Button("OK", new ClickHandler() {
			public void onClick(ClickEvent event) {
				String ans = textBox.getText().toLowerCase();
				dialogBox.hide();
				if (ans.equals("y") || ans.equals("yes")) {
					loadingPopupPanel.show();
					service.deleteAllContents(new AsyncCallback<Boolean>() {
						
						@Override
						public void onSuccess(Boolean result) {
							if (result.booleanValue()) {
								infoMessage("The database is completely deleted!");
							} else {
								warnMessage("System problem! Failed to delete the contents.");
							}
							loadingPopupPanel.hide();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							warnMessage("System problem! Failed to delete the contents.");
							loadingPopupPanel.hide();
						}
					});
				} else {
					warnMessage("Process abort! Nothing changed.");
				}
			}
		});
		okButton.setWidth("100px");
		buttonHp.add(okButton);
		
		dialogContents.add(buttonHp);
		dialogContents.setCellHorizontalAlignment(buttonHp, HasHorizontalAlignment.ALIGN_CENTER);
		
		dialogBox.setGlassEnabled(true);
		dialogBox.setAnimationEnabled(false);
		dialogBox.setAutoHideEnabled(true);
	}
	
}
