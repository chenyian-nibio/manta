package jp.go.nibiohn.bioinfo.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DataEntry implements IsSerializable {

	private String identifier;
	
	private String name;
	
	public DataEntry() {
	}

	public DataEntry(String identifier, String name) {
		super();
		this.identifier = identifier;
		this.name = name;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public String getName() {
		return name;
	}
	
}
