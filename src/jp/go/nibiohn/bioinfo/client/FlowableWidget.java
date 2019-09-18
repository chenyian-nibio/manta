package jp.go.nibiohn.bioinfo.client;

public abstract class FlowableWidget extends BaseWidget {

	protected String name;
	protected String link;

	// default is English
	protected String currentLang = "en_";
	
	public FlowableWidget(String name, String link) {
		this.name = name;
		this.link = link;
	}

}
