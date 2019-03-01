package jp.go.nibiohn.bioinfo.shared;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PcoaResult implements IsSerializable {

	private Map<String, List<Double>> coordinates;
	
	private String pCoAPlotGrid;
	
	private Double scale;
	
	private Integer zeroX;
	private Integer zeroY;
	
	public PcoaResult() {
	}

	public PcoaResult(Map<String, List<Double>> coordinates, String pCoAPlotGrid, Double scale, Integer zeroX,
			Integer zeroY) {
		super();
		this.coordinates = coordinates;
		this.pCoAPlotGrid = pCoAPlotGrid;
		this.scale = scale;
		this.zeroX = zeroX;
		this.zeroY = zeroY;
	}

	public Map<String, List<Double>> getCoordinates() {
		return coordinates;
	}

	public String getpCoAPlotGrid() {
		return pCoAPlotGrid;
	}

	public Double getScale() {
		return scale;
	}

	public Integer getZeroX() {
		return zeroX;
	}

	public Integer getZeroY() {
		return zeroY;
	}

	public int getZeroXposition() {
		return 100 + 100 * zeroX;
	}

	public int getZeroYposition() {
		return 100 + 100 * (zeroY - 5);
	}
}
