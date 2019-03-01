package jp.go.nibiohn.bioinfo.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class VisualizationtResult implements IsSerializable {

	private String svgChart;
	
	private Map<Integer, DendrogramCache> dendrogramMap;
	
	public VisualizationtResult() {
	}

	public VisualizationtResult(String svgChart) {
		this.svgChart = svgChart;
	}

	public String getSvgChart() {
		return svgChart;
	}

	public Map<Integer, DendrogramCache> getDendrogramMap() {
		return dendrogramMap;
	}

	public void setDendrogramMap(Map<Integer, DendrogramCache> dendrogramMap) {
		this.dendrogramMap = dendrogramMap;
	}
	
}
