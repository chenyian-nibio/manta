package jp.go.nibiohn.bioinfo.server.distance;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Jaccard {

	public static double distance(Map<String, Integer> map1, Map<String, Integer> map2) {
		Set<String> uniSet = new HashSet<String>();
		uniSet.addAll(map1.keySet());
		uniSet.addAll(map2.keySet());

		int union = uniSet.size();

		Set<String> intSet = new HashSet<String>();
		for (String k1 : map1.keySet()) {
			if (map2.get(k1) != null && map2.get(k1) > 0) {
				intSet.add(k1);
			}
		}

		int intersect = intSet.size();

		double d = 1 - (double) intersect / union;

		return d;
	}
}
