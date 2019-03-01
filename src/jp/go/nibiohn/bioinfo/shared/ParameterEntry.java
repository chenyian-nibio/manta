package jp.go.nibiohn.bioinfo.shared;

public class ParameterEntry extends DataEntry {

	private String unit;

	public ParameterEntry() {
	}

	public ParameterEntry(String identifier, String name, String unit) {
		super(identifier, name);
		this.unit = unit;
	}

	public String getUnit() {
		return unit;
	}

}
