package jp.go.nibiohn.bioinfo.client.generic;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;

public class W3cCssSelectors {

	/**
	 * supported in Firefox 3.1+, IE8+ (only in IE8 standards mode), and Safari 3.1+.
	 * 
	 * @param selectors
	 * @return
	 */
	public static final native NodeList<Element> querySelectorAll(String selectors) /*-{
		return $doc.querySelectorAll(selectors);
	}-*/;

	public static final native Element querySelector(String selectors) /*-{
		return $doc.querySelector(selectors);
	}-*/;
	
}
