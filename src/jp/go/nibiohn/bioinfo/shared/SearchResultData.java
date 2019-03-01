package jp.go.nibiohn.bioinfo.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SearchResultData implements IsSerializable {

	private String rank;
	
	private String referenceType;
	
	private String referenceName;
	
	private List<List<String>> correationList;
	
	private String correlationMethod;
	
	public SearchResultData() {
	}

	public SearchResultData(String rank, String referenceType, String referenceName, 
			Integer correlationMethodValue, List<List<String>> correationList) {
		super();
		this.rank = rank;
		this.referenceType = referenceType;
		this.referenceName = referenceName;
		this.correationList = correationList;
		if (correlationMethodValue.equals(GutFloraConstant.CORRELATION_SPEARMAN_VALUE)) {
			this.correlationMethod = GutFloraConstant.CORRELATION_SPEARMAN;
		} else if (correlationMethodValue.equals(GutFloraConstant.CORRELATION_PEARSON_VALUE)) {
			this.correlationMethod = GutFloraConstant.CORRELATION_PEARSON;
		} else if (correlationMethodValue.equals(GutFloraConstant.MULTIPLE_LINEAR_REGRESSION_VALUE)) {
			this.correlationMethod = GutFloraConstant.MULTIPLE_LINEAR_REGRESSION;
		} else {
			// should not happened!
			this.correlationMethod = "unknown";
		}
	}

	public String getRank() {
		return rank;
	}

	public String getReferenceType() {
		return referenceType;
	}

	public String getReferenceName() {
		return referenceName;
	}

	public List<List<String>> getCorreationList() {
		return correationList;
	}

	public String getCorrelationMethod() {
		return correlationMethod;
	}

}
