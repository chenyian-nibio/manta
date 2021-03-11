package jp.go.nibiohn.bioinfo.shared;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GutFloraConstant {
	
	public static final String GUEST_USERNAME = "demo";
	
	// a short cut
	public static final Map<String, Integer> RANK_MAP = new HashMap<String, Integer>(); 
	static {
		RANK_MAP.put("kingdom", Integer.valueOf(1));
		RANK_MAP.put("superkingdom", Integer.valueOf(1));
		RANK_MAP.put("phylum", Integer.valueOf(2));
		RANK_MAP.put("class", Integer.valueOf(3));
		RANK_MAP.put("order", Integer.valueOf(4));
		RANK_MAP.put("family", Integer.valueOf(5));
		RANK_MAP.put("genus", Integer.valueOf(6));
		RANK_MAP.put("species", Integer.valueOf(7));
	}
	
	public static final int DEFAULT_NUM_OF_COLUMNS = 10;
	
	public static final List<String> RANK_LIST = Arrays.asList("kingdom", "phylum", "class", "order", "family", "genus", "species");

	public static final String[] DIVERSITY_INDEX = new String[]{"Shannon index", "Simpson index", "Chao1 index"};

	public static final String[] ANALYSIS_TAB_TITLES = new String[] { "Microbiota composition (16S)", "Phenotypic parameters",
			"Correlate two parameters", "Unranked categorical parameters", "Microbiota composition (Shotgun)"}; 

	public static final String NAVI_LINK_SAMPLE = "sample";
	public static final String NAVI_LINK_ANALYSIS = "analysis";
	public static final String NAVI_LINK_SEARCH = "search";
	public static final String NAVI_LINK_VIEW_BARCHART = "barchart";
	public static final String NAVI_LINK_VIEW_HEATMAP = "heatmap";
	public static final String NAVI_LINK_VIEW_PCOA = "pcoa";
	public static final String NAVI_LINK_SUFFIX_PROFILE = "-profile";
	public static final String NAVI_LINK_SUFFIX_READ = "-read";
//	public static final String NAVI_LINK_SUFFIX_IMMUN = "-immun";
	public static final String NAVI_LINK_SUBSET_SUFFIX = "-subset";
	public static final String NAVI_LINK_MLR = "-mlr";
	
	public static final String LANG_EN = "en_";
	public static final String LANG_JP = "jp_";

	public static final String COLUMN_HEADER_OTHERS = "Others";

//	public static final String USER_NAME_GUEST = "Guest";
	
	public static final String PARA_TYPE_CONTINUOUS = "continuous";
	public static final String PARA_TYPE_UNRANKED_CATEGORY = "category_u";
	public static final String PARA_TYPE_RANKED_CATEGORY= "category_r";
	
	public static final String ANALYSIS_TYPE_READ = "read";
	public static final String ANALYSIS_TYPE_PROFILE = "profile";

	public static final String CORRELATION_PEARSON = "Pearson's correlation";
	public static final Integer CORRELATION_PEARSON_VALUE = Integer.valueOf(0);
	public static final String CORRELATION_SPEARMAN = "Spearman's correlation";
	public static final Integer CORRELATION_SPEARMAN_VALUE = Integer.valueOf(1);
	public static final String MULTIPLE_LINEAR_REGRESSION= "Multiple linear regression";
	public static final Integer MULTIPLE_LINEAR_REGRESSION_VALUE= Integer.valueOf(9);
	// for multiple linear regression
	public static final String ALL_ABOVE_MICROBIOTA = "ALL ABOVE";

	public static final String SAMPLE_DISTANCE_UNWEIGHTED_UNIFRAC = "Unweighted UniFrac";
	public static final Integer SAMPLE_DISTANCE_UNWEIGHTED_UNIFRAC_VALUE = Integer.valueOf(1);
	public static final String SAMPLE_DISTANCE_WEIGHTED_UNIFRAC = "Weighted UniFrac";
	public static final Integer SAMPLE_DISTANCE_WEIGHTED_UNIFRAC_VALUE = Integer.valueOf(2);
	public static final String SAMPLE_DISTANCE_BRAY_CURTIS_OTU = "Bray-Curtis (by OTU)";
	public static final Integer SAMPLE_DISTANCE_BRAY_CURTIS_OTU_VALUE = Integer.valueOf(3);
	public static final String SAMPLE_DISTANCE_BRAY_CURTIS_GENUS = "Bray-Curtis (by genus)";
	public static final Integer SAMPLE_DISTANCE_BRAY_CURTIS_GENUS_VALUE = Integer.valueOf(4);
	public static final String SAMPLE_DISTANCE_BRAY_CURTIS_SPECIES = "Bray-Curtis (by species)";
	public static final Integer SAMPLE_DISTANCE_BRAY_CURTIS_SPECIES_VALUE = Integer.valueOf(5);
	public static final String SAMPLE_DISTANCE_JACCARD = "Jaccard";
	public static final Integer SAMPLE_DISTANCE_JACCARD_VALUE = Integer.valueOf(6);

	public static final String CLUSTERING_LINKAGE_AVERAGE = "Average";
	public static final Integer CLUSTERING_LINKAGE_AVERAGE_VALUE = Integer.valueOf(0);
	public static final String CLUSTERING_LINKAGE_COMPLETE = "Complete";
	public static final Integer CLUSTERING_LINKAGE_COMPLETE_VALUE = Integer.valueOf(1);
	public static final String CLUSTERING_LINKAGE_SINGLE = "Single";
	public static final Integer CLUSTERING_LINKAGE_SINGLE_VALUE = Integer.valueOf(2);

	public static final int PLURAL = 1;
	public static final int SINGULAR = 0;
	public static final String[] TREM_SAMPLE = new String[]{"sample", "samples"};
	public static final String[] TO_BE = new String[]{"is", "are"};

	public static final String BARCHART_COLORLESS= "#cccccc";
	public static final List<String> BARCHART_COLOR = Arrays.asList(
			"#3366cc", "#dc3912", "#ff9900", "#109618", "#990099", "#0099c6", "#dd4477", "#66aa00", 
			"#b82e2e", "#316395", "#994499", "#22aa99", "#aaaa11", "#6633cc", "#e67300", "#8b0707",
			"#4477bb", "#cb2823", "#eeaa11", "#21a729", "#aa1188", "#11aab5", "#ee5566", "#55bb11", 
			"#a73f3f", "#2074a6", "#8855aa", "#3399aa", "#99bb22", "#7744bb", "#d58411", "#7a1818",
			"#2277dd", "#cb4a23", "#ee8811", "#218529", "#8811aa", "#1188d7", "#ee3388", "#779911", 
			"#c91d3f", "#4252a6", "#aa5588", "#11bbaa", "#bb9922", "#5544dd", "#d56211", "#9c1818");
	
	public static final Integer EXPERIMENT_METHOD_16S = Integer.valueOf(1);
	public static final Integer EXPERIMENT_METHOD_SHOTGUN = Integer.valueOf(2);
}
