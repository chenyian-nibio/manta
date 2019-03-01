package jp.go.nibiohn.bioinfo.server.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Implement using bottom-up method, naive algorithm; the efficiency may not be good. <br/>
 * <h3>Note:</h3>
 * <ul>
 *   <li> A dendrogram will be plotted when doing the clustering. </li>
 *   <li> The height of the dendrogram reflect the real distance. </li>
 *   <li> Support single-linkage & complete linkage. </li>
 * </ul>
 * 
 * 
 * @author chenyian
 *
 */
public class HierarchicalClustering {
	private Map<String, Map<String, Double>> matrix;
	private List<Double> distanceList = new ArrayList<Double>();
	private List<String> pairList = new ArrayList<String>();
	private Map<String, Dendrogram> plotMap = new HashMap<String, Dendrogram>();

	public List<Double> getDistanceList() {
		return distanceList;
	}

	public List<String> getPairList() {
		return pairList;
	}

	public Map<String, Dendrogram> getPlotMap() {
		return plotMap;
	}
	
	private String resultString;

	private LinkageType linkageType;

	/**
	 * Map<String, Map<String, Double>
	 *      row         col     value  for a distance matrix
	 * @param matrix
	 */
	public HierarchicalClustering(Map<String, Map<String, Double>> matrix) {
		// TODO probably need to have some check mechanism, for example, 
		// matrix should not be empty, and equivalent dimension
		this.matrix = matrix;
	}

	public HierarchicalClustering(double[][] matrix) {
		// TODO probably need to have some check mechanism, for example, 
		// matrix should not be empty, and equivalent dimension
		this.matrix = new HashMap<String, Map<String, Double>>();
		for (int i = 0; i < matrix.length; i++) {
			this.matrix.put(String.valueOf(i), new HashMap<String, Double>());
			for (int j = 0; j < matrix.length; j++) {
				this.matrix.get(String.valueOf(i)).put(String.valueOf(j), matrix[i][j]);
			}
		}
	}
	
	private Double getAverageDistance(String tagA, String tagB) {
		if (tagA.equals(tagB)) {
			return Double.valueOf(0d);
		}
		String[] itemsA = tagA.split("=");
		String[] itemsB = tagB.split("=");
		double count = 0d;
		double sum = 0d;
		for (String ta : itemsA) {
			for (String tb : itemsB) {
				sum += matrix.get(ta).get(tb).doubleValue();
				count += 1d;
			}
		}
		return Double.valueOf(sum / count);
	}
	
	private Double getCompleteDistance(String tagA, String tagB) {
		if (tagA.equals(tagB)) {
			return Double.valueOf(0d);
		}
		String[] itemsA = tagA.split("=");
		String[] itemsB = tagB.split("=");
		List<Double> allDistance = new ArrayList<Double>();
		for (String ta : itemsA) {
			for (String tb : itemsB) {
				allDistance.add(matrix.get(ta).get(tb));
			}
		}
		Collections.sort(allDistance, new Comparator<Double>() {
			@Override
			public int compare(Double o1, Double o2) {
				return o2.compareTo(o1);
			}
		});
		return allDistance.get(0);
	}
	
	private Double getSingleDistance(String tagA, String tagB) {
		if (tagA.equals(tagB)) {
			return Double.valueOf(0d);
		}
		String[] itemsA = tagA.split("=");
		String[] itemsB = tagB.split("=");
		List<Double> allDistance = new ArrayList<Double>();
		for (String ta : itemsA) {
			for (String tb : itemsB) {
				allDistance.add(matrix.get(ta).get(tb));
			}
		}
		Collections.sort(allDistance, new Comparator<Double>() {
			@Override
			public int compare(Double o1, Double o2) {
				return o1.compareTo(o2);
			}
		});
		return allDistance.get(0);
	}
	
	private Double getDistance(LinkageType type, String tagA, String tagB){
		
		switch (type) {
		case AVERAGE:
			return getAverageDistance(tagA, tagB);
			
		case COMPLETE:
			return getCompleteDistance(tagA, tagB);

		case SINGLE:
			return getSingleDistance(tagA, tagB);

		default:
			return getAverageDistance(tagA, tagB);
		}

	}
	
	
	// TODO cutoff
	public String clusteringByAverageLinkage() {
		return doClustering(LinkageType.AVERAGE);
	}
	
	public String clusteringBySingleLinkage() {
		return doClustering(LinkageType.SINGLE);
	}
	
	public String clusteringByCompleteLinkage() {
		return doClustering(LinkageType.COMPLETE);
	}
	
	public String doClustering(LinkageType type) {
		if (type.equals(linkageType)) {
			return resultString;
		}
		
		distanceList.clear();
		pairList.clear();
		plotMap.clear();
		
		Map<String, Double> previousDistance = new HashMap<String, Double>();

		List<String> items = new ArrayList<String>(matrix.keySet());
		Collections.sort(items);
		
		for (String itm : items) {
			plotMap.put(itm, new Dendrogram(itm));
		}
		
		while (1 < items.size()) {
			final Map<String, Double> distance = new HashMap<String, Double>();

			for (int i = 1; i < items.size(); i++) {
				for (int j = 0; j < i; j++) {
					// TODO the separator dash(-) may not safe... if the original tag contains a dash
					String pair = String.format("%s-%s", items.get(i), items.get(j));
					if (previousDistance.containsKey(pair)) {
						distance.put(pair, previousDistance.get(pair));
					} else {
						distance.put(pair, getDistance(type, items.get(i), items.get(j)));
					}
				}
			}

			List<String> pairs = new ArrayList<String>(distance.keySet());
			Collections.sort(pairs, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return distance.get(o1).compareTo(distance.get(o2));
				}
			});
			String closestItems = pairs.get(0);
			Double shortestDistance = distance.get(closestItems);

			// TODO do corresponding change if the separator changed
			String[] tags = closestItems.split("-");
			items.remove(tags[0]);
			items.remove(tags[1]);

			String seqString = String.format("%s=%s", tags[0], tags[1]);
			items.add(seqString);
			// try to draw the dendrogram
			Dendrogram part0 = plotMap.get(tags[0]);
			Dendrogram part1 = plotMap.get(tags[1]);
			part1.shiftY(Dendrogram.LINE_HEIGHT * part0.getNum());
			
			int x2 = (int) (-Dendrogram.LEVEL_UNIT * shortestDistance);
			int y1 = part0.getCentralY();
			List<Integer> lineA = Arrays.asList(part0.getCentralX(), y1, x2, y1);
			
			int y2 = part1.getCentralY();
			List<Integer> lineB = Arrays.asList(part1.getCentralX(), y2, x2, y2);
			List<Integer> lineC = Arrays.asList(x2, y1, x2, y2);
			
			int nodeY = (y1 + y2)/2;
			int nodeX = x2;
			
			plotMap.put(seqString, new Dendrogram(part0, part1, 
					lineA, lineB, lineC, nodeX, nodeY, seqString, shortestDistance));
			
			distanceList.add(shortestDistance);
			pairList.add(String.format("%s--%s", tags[0], tags[1]));
			
			previousDistance = new HashMap<String, Double>(distance);
		}
		linkageType = type;
		resultString = items.get(0);
		return resultString;
	}

	public Dendrogram getDendrogram(LinkageType type) {
		if (!type.equals(linkageType)) {
			doClustering(type);
		}
		return plotMap.get(resultString);
	}
	
	public static enum LinkageType {
		AVERAGE, SINGLE, COMPLETE;
	}
}