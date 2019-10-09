package jp.go.nibiohn.bioinfo.client.manage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;

public class UploadDataWidget extends ManageWidget {

	private ListBox typeListBox = new ListBox();

	private SimplePanel summaryPanel = new SimplePanel();

	private SimplePanel filePreviewPanel = new SimplePanel();
	
	private DialogBox dialogBox = new DialogBox();
	
	private FormPanel formPanel = new FormPanel();

	private PopupPanel loadingPopupPanel = new PopupPanel();

	public UploadDataWidget() {
		VerticalPanel thisWidget = new VerticalPanel();
		
		thisWidget.add(new HTML("<h3>Summary:</h3>"));
		thisWidget.add(summaryPanel);
		summaryPanel.add(new Label("Loading......"));
		
		thisWidget.add(new HTML("<h3>Upload files:</h3>"));
		
		formPanel.setAction(GWT.getModuleBaseURL() + "upload");
		formPanel.setMethod(FormPanel.METHOD_POST);
		formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
		
		DecoratorPanel fileUploadDec = new DecoratorPanel();
		fileUploadDec.setTitle("File upload");
		fileUploadDec.addStyleName("optionDec");
		
		HorizontalPanel fileUploadHp = new HorizontalPanel();
		fileUploadHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		fileUploadHp.setSpacing(12);
		fileUploadDec.add(formPanel);
		
		fileUploadHp.add(new Label("File type: "));
		
		typeListBox.setName("type");
		typeListBox.addItem(GutFloraConstant.UPLOAD_DATA_TYPE_PARAMETERS);
		typeListBox.addItem(GutFloraConstant.UPLOAD_DATA_TYPE_MICROBIOTA);
		fileUploadHp.add(typeListBox);
		
		final FileUpload fileUpload = new FileUpload();
		fileUploadHp.add(fileUpload);
		fileUpload.setName("uploadFile");
		fileUpload.setWidth("300px");
		
		Button uploadBtn = new Button("Upload", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				clearMessage();
				
				JavaScriptObject files = fileUpload.getElement().getPropertyJSO("files");
				readTextFile(UploadDataWidget.this, typeListBox.getSelectedValue(), files);
				
				createPreviewDialogBox();
			}
		});
		
		uploadBtn.setWidth("80px");
		formPanel.addSubmitHandler(new SubmitHandler() {
			
			@Override
			public void onSubmit(SubmitEvent event) {
				if (fileUpload.getFilename().equals("")) {
					warnMessage("No file is selected.");
					event.cancel();
				}
			}
		});
		formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				loadingPopupPanel.hide();
				updateDatabaseSummary();
				String results = event.getResults();
				if (results.startsWith("OK")) {
					infoMessage("Data successfully uploaded.");
					formPanel.reset();
				} else {
					warnMessage(results);
				}
			}
		});
		
		fileUploadHp.add(uploadBtn);
		formPanel.setWidget(fileUploadHp);
		
		thisWidget.add(fileUploadDec);
		
		updateDatabaseSummary();
		
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
	
	private void createPreviewDialogBox() {
		dialogBox.ensureDebugId("previewDialogBox");
		dialogBox.setText("Upload Preview");
		
		VerticalPanel dialogContents = new VerticalPanel();
		dialogContents.setSpacing(4);
		dialogBox.setWidget(dialogContents);

		dialogContents.add(filePreviewPanel);
		
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
				formPanel.submit();
				dialogBox.hide();
				loadingPopupPanel.show();
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

	// Use html5 file reader to read the file contents on client side 
	public static native void readTextFile(UploadDataWidget widget, String type, JavaScriptObject files)
	/*-{
	    var reader = new FileReader();

	    reader.onload = function(e) {
	        widget.@jp.go.nibiohn.bioinfo.client.manage.UploadDataWidget::showFileContents(*)(type, reader.result);
	    }

	    reader.readAsText(files[0]);
	}-*/;
	
	public void showFileContents(String type, String contents) {
		String[] lines = contents.split("[\r|\n]+");
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<p><b>File type:</b> " + type + "</p>\n");
		
		// For GutFloraConstant.UPLOAD_DATA_TYPE_PARAMETERS
		String columnType = "Parameters";
		String rowType = "Sample IDs";
		if (type.equals(GutFloraConstant.UPLOAD_DATA_TYPE_MICROBIOTA)) {
			columnType = "Sample IDs";
			rowType = "Taxonomy";
		}
		
		String[] headers = lines[0].split("\t");
		sb.append("<table class=\"paraPreview\"><thead>");
		
		sb.append("<tr><td>&nbsp;</td><th colspan=\"" + (headers.length > 11 ? 11 : headers.length - 1)
				+ "\" style=\"text-align: center;\">" + columnType + "</th></tr>\n");
		
		sb.append("<tr><th class=\"firstColumn\">" + rowType + "</th>\n");
		for (int i = 1; i < headers.length; i++) {
			if (i > 10) {
				sb.append("<th> ... </th>");
				break;
			}
			sb.append("<th>" + headers[i] + "</th>");
		}
		sb.append("</tr>\n");

		sb.append("</thead>\n");
		sb.append("<tbody>\n");
		for (int j = 1; j < lines.length; j++) {
			sb.append("<tr class=\"\">\n");
			String[] cols = lines[j].split("\t");
			if (j > 10) {
				sb.append("<th class=\"firstColumn\"> : </th>");
				for (int i = 1; i < cols.length; i++) {
					if (i > 10) {
						sb.append("<td>&nbsp;</td>");
						break;
					}
					sb.append("<td> : </td>");
				}
				break;
			} else {
				String firstColumn = cols[0];
				if (firstColumn.length() > 32) {
					firstColumn = firstColumn.substring(0, 31) + "...";
				}
				sb.append("<th class=\"firstColumn\">" + firstColumn + "</th>");
				for (int i = 1; i < cols.length; i++) {
					if (i > 10) {
						sb.append("<td> ... </td>");
						break;
					}
					String value = cols[i];
					if (value.length() > 12) {
						value = value.substring(0, 11) + "...";
					}
					sb.append("<td>" + value + "</td>");
				}
			}
			sb.append("</tr>\n");
		}
		sb.append("</tbody>\n");
		sb.append("</table>\n");
		
		filePreviewPanel.setWidget(new HTML(sb.toString()));
		dialogBox.center();
	}
	
	private void updateDatabaseSummary() {
		service.getDatabaseSummary(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				summaryPanel.setWidget(new HTML(result));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
	public void updateContents() {
		updateDatabaseSummary();
	}
}
