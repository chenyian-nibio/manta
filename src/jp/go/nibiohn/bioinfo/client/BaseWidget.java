package jp.go.nibiohn.bioinfo.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public abstract class BaseWidget extends Composite {
	protected static final String SERVER_ERROR = "An error occurred while attempting to contact the server. "
			+ "Please check your network connection and try again.";

	protected final GutFloraServiceAsync service = GWT.create(GutFloraService.class);

	protected RootPanel mesgPanel = RootPanel.get("mesgPanel");
	protected RootPanel naviPanel = RootPanel.get("naviPanel");
	protected RootPanel mainPanel = RootPanel.get("mainPanel");
	protected RootPanel infoPanel = RootPanel.get("infoPanel");

	protected void infoMessage(String message) {
		Label label = (Label) ((HorizontalPanel) mesgPanel.getWidget(0)).getWidget(0);
		label.setText(message);
		mesgPanel.setStyleName("infoMessage");
		mesgPanel.setVisible(true);
	}

	protected void warnMessage(String message) {
		Label label = (Label) ((HorizontalPanel) mesgPanel.getWidget(0)).getWidget(0);
		label.setText(message);
		mesgPanel.setStyleName("warnMessage");
		mesgPanel.setVisible(true);
	}

	protected void clearMessage() {
		mesgPanel.setVisible(false);
	}

}
