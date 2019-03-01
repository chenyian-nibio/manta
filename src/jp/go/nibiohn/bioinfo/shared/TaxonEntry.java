package jp.go.nibiohn.bioinfo.shared;

public class TaxonEntry extends DataEntry {

	private String rank;

	public TaxonEntry() {
	}

	public TaxonEntry(String name, String identifier, String rank) {
		super(identifier, name);
		this.rank = rank;
	}

	public String getRank() {
		return rank;
	}

}
