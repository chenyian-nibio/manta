package jp.go.nibiohn.bioinfo.shared;

public class ParameterEntry extends DataEntry {

	private String unit;
	
	private Integer type;

	public ParameterEntry() {
	}

	public ParameterEntry(String identifier, String name) {
		super(identifier, name);
	}
	
	public ParameterEntry(String identifier, String name, String unit) {
		super(identifier, name);
		this.unit = unit;
	}

	public ParameterEntry(String identifier, String name, Integer type) {
		super(identifier, name);
		this.type = type;
	}

	public String getUnit() {
		return unit;
	}

	public Integer getType() {
		return type;
	}

}
