package jp.go.nibiohn.bioinfo.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PairListData implements IsSerializable {

	private List<String> originalList;

	private List<String> supplementList;
	
	public PairListData() {
	}

	public PairListData(List<String> originalList) {
		this.originalList = originalList;
		this.supplementList = new ArrayList<String>();
	}

	public PairListData(List<String> originalList, List<String> supplementList) {
		this.originalList = originalList;
		this.supplementList = supplementList;
	}

	public List<String> getOriginalList() {
		return originalList;
	}

	public List<String> getSupplementList() {
		return supplementList;
	}

}
