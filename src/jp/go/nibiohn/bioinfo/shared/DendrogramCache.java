package jp.go.nibiohn.bioinfo.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DendrogramCache implements IsSerializable {

	List<String> sequence;
	String svgDendrogram;
	int dendrogramWidth;
	int dendrogramHeight;

	public DendrogramCache() {
	}

	public DendrogramCache(List<String> sequence, String svgDendrogram, int dendrogramWidth, int dendrogramHeight) {
		this.sequence = sequence;
		this.svgDendrogram = svgDendrogram;
		this.dendrogramWidth = dendrogramWidth;
		this.dendrogramHeight = dendrogramHeight;
	}

	public List<String> getSequence() {
		return sequence;
	}

	public String getSvgDendrogram() {
		return svgDendrogram;
	}

	public int getDendrogramWidth() {
		return dendrogramWidth;
	}

	public int getDendrogramHeight() {
		return dendrogramHeight;
	}

}
