package jp.go.nibiohn.bioinfo.client;

public abstract class FlowableWidget extends BaseWidget {

	protected String name;
	protected String link;

	public FlowableWidget(String name, String link) {
		this.name = name;
		this.link = link;
	}

}
