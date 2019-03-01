package jp.go.nibiohn.bioinfo.shared;

import java.util.HashMap;
import java.util.Map;

/**
 * this is not a good implementation... to be improved
 * 
 */
public class GutFloraLanguagePack {

	public static final Map<String, Map<String, String>> DISPLAY_MAP = new HashMap<String, Map<String, String>>(); 
	public static final Map<String, String> DISPLAY_MAP_JP = new HashMap<String, String>(); 
	public static final Map<String, String> DISPLAY_MAP_EN = new HashMap<String, String>(); 
	static {
		DISPLAY_MAP_JP.put("select all", "すべで選択");
		DISPLAY_MAP_EN.put("select all", "Select all");
		DISPLAY_MAP_JP.put("select none", "すべて選択解除");
		DISPLAY_MAP_EN.put("select none", "Select none");
		DISPLAY_MAP_JP.put("select current page", "このページを選択");
		DISPLAY_MAP_EN.put("select current page", "Select current page");
		DISPLAY_MAP_JP.put("unselect current page", "このページを選択解除");
		DISPLAY_MAP_EN.put("unselect current page", "Unselect current page");
		DISPLAY_MAP_JP.put("start", "スタート");
		DISPLAY_MAP_EN.put("start", "Start");
		
		
		DISPLAY_MAP.put(GutFloraConstant.LANG_JP, DISPLAY_MAP_JP);
		DISPLAY_MAP.put(GutFloraConstant.LANG_EN, DISPLAY_MAP_EN);
	}

}
