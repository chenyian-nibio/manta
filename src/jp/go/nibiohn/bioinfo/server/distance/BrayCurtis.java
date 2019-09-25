package jp.go.nibiohn.bioinfo.server.distance;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BrayCurtis {

	public static double distance(Map<String, Integer> map1, Map<String, Integer> map2) {
		Set<String> keySet = new HashSet<String>();
		keySet.addAll(map1.keySet());
		keySet.addAll(map2.keySet());

		int allCount = 0;
		int lesserCount = 0;
		for (String key : keySet) {
			Integer int1 = map1.get(key);
			if (int1 == null)
				int1 = 0;
			Integer int2 = map2.get(key);
			if (int2 == null)
				int2 = 0;

			allCount += int1.intValue();
			allCount += int2.intValue();

			lesserCount += (int1 > int2 ? int2 : int1);
		}

		double d = 1 - 2d * lesserCount / allCount;

		return d;
	}

}
