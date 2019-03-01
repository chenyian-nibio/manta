package jp.go.nibiohn.bioinfo.client;

import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public abstract class BasePage implements EntryPoint, ValueChangeHandler<String> {

	protected static final String SERVER_ERROR = "An error occurred while attempting to contact the server. "
			+ "Please check your network connection and try again.";

	protected static Logger rootLogger = Logger.getLogger("");

	protected RootPanel mesgPanel = RootPanel.get("mesgPanel");
	protected RootPanel naviPanel = RootPanel.get("naviPanel");
	protected RootPanel mainPanel = RootPanel.get("mainPanel");
	protected RootPanel infoPanel = RootPanel.get("infoPanel");

	protected boolean hasMessage = false;
	protected Label info = new Label("Message will be shown here.");
	
	// default is English
	protected String currentLang = "en_";

	protected void init() {
		mesgPanel.setStyleName("infoMessage");
		mesgPanel.setVisible(false);
		HorizontalPanel hp = new HorizontalPanel();
		hp.setWidth("100%");
		hp.add(info);
		Label closeLable = new Label("close");
		closeLable.addStyleName("closeLink");
		closeLable.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				setMessageInvisible();
			}
		});
		hp.add(closeLable);
		mesgPanel.add(hp);
	}

	protected void infoMessage(String message) {
		info.setText(message);
		mesgPanel.setStyleName("infoMessage");
		mesgPanel.setVisible(true);
	}

	protected void warnMessage(String message) {
		info.setText(message);
		mesgPanel.setStyleName("warnMessage");
		mesgPanel.setVisible(true);
	}

	protected void setMessageInvisible() {
		mesgPanel.setVisible(false);
	}

	protected void setMessageVisible() {
		mesgPanel.setVisible(true);
	}

}
