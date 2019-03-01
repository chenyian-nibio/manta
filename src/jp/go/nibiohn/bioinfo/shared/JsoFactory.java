package jp.go.nibiohn.bioinfo.shared;

import com.google.gwt.core.client.JavaScriptObject;

public class JsoFactory extends JavaScriptObject {

	protected JsoFactory() {}
	
	private final native void setChartArea(int left, int top, int width, int height)/*-{
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
	}-*/;

	public static JsoFactory createChartAreaOption(int height) {
		JsoFactory ret = (JsoFactory) JavaScriptObject.createObject();
		
		ret.setChartArea(120, 50, 600, height);
		
		return ret;
	}

}
