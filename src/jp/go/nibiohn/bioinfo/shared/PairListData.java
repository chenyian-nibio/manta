package jp.go.nibiohn.bioinfo.shared;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PairListData implements IsSerializable {

	private List<String> originalList;
	private Map<String, String> metaDataMap;

	public PairListData() {
	}

	public PairListData(List<String> originalList) {
		this.originalList = originalList;
	}

	public PairListData(List<String> originalList, Map<String, String> metaDataMap) {
		this.originalList = originalList;
		this.metaDataMap = metaDataMap;
	}

	public List<String> getOriginalList() {
		return originalList;
	}

	public Map<String, String> getMetaDataMap() {
		return metaDataMap;
	}

}
