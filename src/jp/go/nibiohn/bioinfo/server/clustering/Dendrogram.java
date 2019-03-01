package jp.go.nibiohn.bioinfo.server.clustering;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class Dendrogram {
	public final static int P1_X = 0;
	public final static int P1_Y = 1;
	public final static int P2_X = 2;
	public final static int P2_Y = 3;
	
	public final static int LINE_HEIGHT = 16;
	public final static int LEVEL_HEIGHT = 10;
	// this unit will affect the resolution of the denfrogram
	public final static double LEVEL_UNIT = 100;

	private int fontSize = 16;

	private List<String> labels = new ArrayList<String>();
	
	private Dendrogram childA;
	private Dendrogram childB;
	
	// -- : lineA
	// |  : lineC
	// -- : lineB
	private List<Integer> lineA;
	private List<Integer> lineB;
	private List<Integer> lineC;
	
	// coordinate of 
	private int centralX = 0;
	private int centralY = 0;
	
	private Double distance;

	private List<String> sequence;
	
	public Dendrogram() {
	}

	public Dendrogram(String item) {
		this.sequence = Arrays.asList(item);
	}
	
	public Dendrogram(Dendrogram childA, Dendrogram childB, List<Integer> lineA, List<Integer> lineB,
			List<Integer> lineC, int centralX, int centralY, String seqString, Double distance) {
		super();
		this.childA = childA;
		this.childB = childB;
		this.lineA = lineA;
		this.lineB = lineB;
		this.lineC = lineC;
		this.centralX = centralX;
		this.centralY = centralY;
		this.distance = distance;
		this.sequence = Arrays.asList(seqString.split("="));
	}

	public List<String> getLabels() {
		return labels;
	}

	public Dendrogram getChildA() {
		return childA;
	}

	public Dendrogram getChildB() {
		return childB;
	}

	public List<Integer> getLineA() {
		return lineA;
	}

	public List<Integer> getLineB() {
		return lineB;
	}

	public List<Integer> getLineC() {
		return lineC;
	}

	public int getCentralX() {
		return centralX;
	}

	public int getCentralY() {
		return centralY;
	}

	public Double getDistance() {
		return distance;
	}

	public List<String> getSequence() {
		return sequence;
	}
	
	public boolean hasChildren() {
		// TODO more complicated check?
		if (childA != null && childB != null) {
			return true;
		}
		return false;
	}
	
	public List<Dendrogram> getChildren() {
		if (hasChildren()) {
			return Arrays.asList(childA, childB);
		}
		return null;
	}

	// TODO return a List or a Set?
	public List<List<Integer> >getAllLines() {
		List<List<Integer>> ret = new ArrayList<List<Integer>>();
		if (hasChildren()) {
			for(Dendrogram child: getChildren()) {
				ret.addAll(child.getAllLines());
			}
			ret.add(lineA);
			ret.add(lineB);
			ret.add(lineC);
		}
		return ret;
	}
	
	public void shiftY(int distance) {
		if (hasChildren()) {
			for(Dendrogram child: getChildren()) {
				child.shiftY(distance);
			}
			lineA.set(P1_Y, lineA.get(P1_Y) + distance);
			lineA.set(P2_Y, lineA.get(P2_Y) + distance);
			lineB.set(P1_Y, lineB.get(P1_Y) + distance);
			lineB.set(P2_Y, lineB.get(P2_Y) + distance);
			lineC.set(P1_Y, lineC.get(P1_Y) + distance);
			lineC.set(P2_Y, lineC.get(P2_Y) + distance);
		}
		centralY += distance;
	}

	public Map<List<Integer>, Dendrogram> getAllNodes() {
		Map<List<Integer>, Dendrogram> ret = new HashMap<List<Integer>, Dendrogram>();
		if (hasChildren()) {
			for(Dendrogram child: getChildren()) {
				Map<List<Integer>, Dendrogram> allNodes = child.getAllNodes();
				for (List<Integer> key : allNodes.keySet()) {
					ret.put(key, allNodes.get(key));
				}
			}
		}
		ret.put(Arrays.asList(centralX,centralY), this);
		return ret;
	}
	
	public int getNum() {
		return sequence.size();
	}
	
	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public BufferedImage getScaleUpBufferImage(int shiftX, int shiftY, double scaleX, double scaleY, double fixedHeight) {
		double newScaleX = fixedHeight / distance.doubleValue() / LEVEL_UNIT * scaleX;
		return getScaleUpBufferImage(shiftX, shiftY, newScaleX, scaleY);
	}
	
	public BufferedImage getScaleUpBufferImage(int shiftX, int shiftY, double scaleX, double scaleY) {
		
		int canvasWidth = (int) (- centralX * scaleX + 80 + shiftX);
		int canvasHeight = (int) (sequence.size() * LINE_HEIGHT * scaleY + 20 + shiftY);
		
		int correctionX = (int) (- centralX * scaleX + 10);
		int correctionY = 10;
		
		BufferedImage image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		graphics.setPaint(Color.WHITE);
		graphics.fillRect(0, 0, canvasWidth, canvasHeight);
		
		graphics.setPaint(Color.BLACK);
		for (List<Integer> line: getAllLines()) {
			graphics.drawLine((int) (line.get(P1_X) * scaleX + correctionX + shiftX), (int) (line.get(P1_Y) * scaleY + correctionY + shiftY),
					(int) (line.get(P2_X) * scaleX + correctionX + shiftX), (int) (line.get(P2_Y) * scaleY + correctionY + shiftY));
		}
		
		Font font = new Font("Consolas", Font.PLAIN, fontSize);
		graphics.setFont(font);
		if (labels == null || labels.isEmpty()) {
			for (int i = 0; i < sequence.size(); i++) {
				graphics.drawString(sequence.get(i), 2 + correctionX + shiftX , (int) (i * LINE_HEIGHT * scaleY  + 4 + correctionY + shiftY));
			}
		} else {
			for (int i = 0; i < sequence.size() && i < labels.size(); i++) {
				int index = Integer.valueOf(sequence.get(i)).intValue();
				graphics.drawString(labels.get(index), 6 + correctionX + shiftX , (int) (i * LINE_HEIGHT * scaleY  + 4 + correctionY + shiftY));
			}
		}
		
		// add nodes...
		Map<List<Integer>, Dendrogram> allNodes = getAllNodes();
		for (List<Integer> node: allNodes.keySet()) {
			int nX = (int) (node.get(P1_X) * scaleX + correctionX + shiftX);
			int nY = (int) (node.get(P1_Y) * scaleY + correctionY + shiftY);
			graphics.drawOval(nX - 3, nY - 3, 6, 6);
			List<String> members = allNodes.get(node).getSequence();
			graphics.drawString(members.toString(), nX, nY);
		}
		
		return image;
	}
	
	/**
	 * return a suggested width (with margins) for a fixed height
	 * 
	 * @param fixedHeight
	 * @return
	 */
	public int getDendrogramWidth(double fixedHeight) {
		return (int) fixedHeight + 20;
	}

	/**
	 * return a suggested width (with margins) for a specific scale of width (dendrogram height)
	 * 
	 * @param scaleX
	 * @return
	 */
	public int getDendrogramWidth(int scaleX) {
		return centralX * scaleX + 20;
	}

	/**
	 * return a suggested width (with margins) for a specific scale of height (dendrogram width/size)
	 * 
	 * @param scaleY
	 * @return
	 */
	public int getDendrogramHeight(int scaleY) {
		return sequence.size() * LINE_HEIGHT * scaleY + 20;
	}
	
	/**
	 * Plot the dendrogram at the left side with a fixed height. 
	 * Return only the xml contents (without the parent svg tag), suite for combining with other shapes.
	 * 
	 * @param shiftX
	 * @param shiftY
	 * @param scaleX
	 * @param scaleY
	 * @param fixedHeight
	 * @param withText
	 * @param labelIdPrefix To assign a prefix for the label (text elements) id. A null value will use the default 'label_left_'
	 * @return
	 */
	public String getScaleUpSvgImageContentAtLeft(int shiftX, int shiftY, double scaleX, double scaleY,
			double fixedHeight, boolean withText, String labelIdPrefix) {
		if (centralX == 0) {
			return getScaleUpSvgImageContentAtLeft((int) (shiftX + fixedHeight), shiftY, scaleX, scaleY, withText,
					labelIdPrefix);
		}
		double newScaleX = -fixedHeight / centralX * scaleX;
		return getScaleUpSvgImageContentAtLeft(shiftX, shiftY, newScaleX, scaleY, withText, labelIdPrefix);
	}

	/**
	 * xml contents without svg tag with a fixed height. suite for combining with other shapes. 
	 * 
	 * @param shiftX
	 * @param shiftY
	 * @param scaleX
	 * @param scaleY
	 * @param fixedHeight
	 * @param withText
	 * @return
	 */
	public String getScaleUpSvgImageContentAtLeft(int shiftX, int shiftY, double scaleX, double scaleY,
			double fixedHeight, boolean withText) {
		if (centralX == 0) {
			return getScaleUpSvgImageContentAtLeft((int) (shiftX + fixedHeight), shiftY, scaleX, scaleY, withText, null);
		}
		double newScaleX = - fixedHeight / centralX * scaleX;
		return getScaleUpSvgImageContentAtLeft(shiftX, shiftY, newScaleX, scaleY, withText, null);
	}

	/**
	 * xml contents without svg tag. suite for combining with other shapes.
	 * 
	 * @param shiftX
	 * @param shiftY
	 * @param scaleX
	 * @param scaleY
	 * @return
	 */
	public String getScaleUpSvgImageContentAtLeft(int shiftX, int shiftY, double scaleX, double scaleY, boolean withText) {
		return getScaleUpSvgImageContentAtLeft(shiftX, shiftY, scaleX, scaleY, withText, null);
	}

	/**
	 * Plot the dendrogram at the left side. 
	 * Return only the xml contents (without the parent svg tag), suite for combining with other shapes.
	 * 
	 * @param shiftX
	 * @param shiftY
	 * @param scaleX
	 * @param scaleY
	 * @param withText
	 * @param labelIdPrefix To assign a prefix for the label (text elements) id. A null value will use the default 'label_left_'
	 * @return
	 */
	public String getScaleUpSvgImageContentAtLeft(int shiftX, int shiftY, double scaleX, double scaleY,
			boolean withText, String labelIdPrefix) {
		
		StringBuffer ret = new StringBuffer();
		
		int correctionX = (int) (- centralX * scaleX + 10);
		int correctionY = 10;
		
		// draw lines
		ret.append("<g stroke=\"rgb(0,0,0)\" stroke-width=\"1\">\n");
		for (List<Integer> line : getAllLines()) {
			ret.append(String.format("\t<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" />\n", (int) (line.get(P1_X)
					* scaleX + correctionX + shiftX), (int) (line.get(P1_Y) * scaleY + correctionY + shiftY),
					(int) (line.get(P2_X) * scaleX + correctionX + shiftX), (int) (line.get(P2_Y) * scaleY
							+ correctionY + shiftY)));
		}
		ret.append(String.format("\t<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" />\n", (int) (centralX
				* scaleX + correctionX + shiftX), (int) (centralY * scaleY + correctionY + shiftY),
				(int) (centralX * scaleX + correctionX + shiftX -5), (int) (centralY * scaleY
						+ correctionY + shiftY)));
		ret.append("</g>\n");
		
		// the label id will be label_left_{sequence index} here 
		if (withText) {
			// TODO untested, to be confirmed
			ret.append(String.format("<g font-family=\"Arial\" font-size=\"%d\" fill=\"black\">\n", fontSize));
			if (labels == null || labels.isEmpty()) {
				for (int i = 0; i < sequence.size(); i++) {
					ret.append(String.format("\t<text x=\"%d\" y=\"%d\" id=\"label_left_%s\">%s</text>\n", 6 + correctionX + shiftX,
							(int) (i * LINE_HEIGHT * scaleY) + 4 + correctionY + shiftY, sequence.get(i), sequence.get(i)));
				}
			} else {
				for (int i = 0; i < sequence.size() && i < labels.size(); i++) {
					int index = Integer.valueOf(sequence.get(i)).intValue();
					String label = labels.get(index);
					ret.append(String.format("\t<text x=\"%d\" y=\"%d\" id=\"label_left_%s\">%s</text>\n", 6 + correctionX + shiftX,
							(int) (i * LINE_HEIGHT * scaleY) + 4 + correctionY + shiftY, sequence.get(i), label));
				}
			}
			ret.append("</g>\n");
		}
		
		ret.append("<g stroke-width=\"1\" stroke=\"none\" class=\"dendroNodes\" id=\"dendroNodesL\">\n");
		Map<List<Integer>, Dendrogram> allNodes = getAllNodes();
		for (List<Integer> node: allNodes.keySet()) {
			int nX = (int) (node.get(P1_X) * scaleX + correctionX + shiftX);
			int nY = (int) (node.get(P1_Y) * scaleY + correctionY + shiftY);
			List<String> members = allNodes.get(node).getSequence();

			ret.append(String.format("\t<circle cx=\"%d\" cy=\"%d\" r=\"5\" fill=\"none\" class=\"dendroNode\">\n", 
					nX, nY));
			if (labelIdPrefix == null) {
				labelIdPrefix = "label_left_";
			}
			ret.append("\t\t<desc>" + labelIdPrefix + StringUtils.join(members, ("," + labelIdPrefix)) + "</desc>\n");
			if (labels != null && !labels.isEmpty()) {
				List<String> memberLabels = new ArrayList<String>();
				for (String index : members) {
					memberLabels.add(labels.get(Integer.valueOf(index)));
				}
				ret.append("\t\t<desc>" + StringUtils.join(memberLabels, "///") + "</desc>\n");
			}
			ret.append("\t</circle>\n");
		}
		ret.append("</g>\n");

		// distance and the scale bar
		ret.append("<g>\n");
		int h = (int) (sequence.size() * LINE_HEIGHT * scaleY);
		ret.append(String.format(
				"\t<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" style=\"stroke:rgb(0,0,0);stroke-width:1\" />\n",
				10 + shiftX, h + correctionY + shiftY + 6, correctionX + shiftX, h + correctionY + shiftY + 6));
		ret.append(String.format(
				"\t<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" style=\"stroke:rgb(0,0,0);stroke-width:1\" />\n",
				10 + shiftX, h + correctionY + shiftY + 6, 10 + shiftX, h + correctionY + shiftY + 12));
		ret.append(String
				.format("\t<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" style=\"stroke:rgb(0,0,0);stroke-width:1\" />\n",
						correctionX + shiftX, h + correctionY + shiftY + 6, correctionX + shiftX, h + correctionY
								+ shiftY + 12));
		String dist = String.format("d = %.2f", distance);
		if (dist.equals("d = 0.00")) {
			dist = "distance < 0.005";
			ret.append(String
					.format("\t<text x=\"%d\" y=\"%d\" style=\"text-anchor: end; font-family: Arial; font-size: %d; fill: black; font-style: italic;\">%s</text>\n",
							(int) (centralX * scaleX) / 2 + correctionX + shiftX, h + correctionY + shiftY + 32, fontSize, dist));
		} else {
			ret.append(String
					.format("\t<text x=\"%d\" y=\"%d\" style=\"text-anchor: middle; font-family: Arial; font-size: %d; fill: black; font-style: italic;\">%s</text>\n",
							(int) (centralX * scaleX) / 2 + correctionX + shiftX, h + correctionY + shiftY + 32, fontSize, dist));
		}
		
		ret.append("</g>\n");

		return ret.toString();
	}
	
	/**
	 * Plot the dendrogram at the top side with a fixed height. 
	 * Return only the xml contents (without the parent svg tag), suite for combining with other shapes.
	 * 
	 * @param shiftX
	 * @param shiftY
	 * @param scaleX
	 * @param scaleY
	 * @param fixedHeight
	 * @param withText
	 * @param labelIdPrefix To assign a prefix for the label (text elements) id. A null value will use the default 'label_top_'
	 * @return
	 */
	public String getScaleUpSvgImageContentAtTop(int shiftX, int shiftY, double scaleX, double scaleY,
			double fixedHeight, boolean withText, String labelIdPrefix) {
		if (centralX == 0) {
			return getScaleUpSvgImageContentAtTop((int) (shiftX + fixedHeight), shiftY, scaleX, scaleY, withText,
					labelIdPrefix);
		}
		double newScaleX = - fixedHeight / centralX * scaleX;
		return getScaleUpSvgImageContentAtTop(shiftX, shiftY, newScaleX, scaleY, withText, labelIdPrefix);
	}

	public String getScaleUpSvgImageContentAtTop(int shiftX, int shiftY, double scaleX, double scaleY,
			double fixedHeight, boolean withText) {
		if (centralX == 0) {
			return getScaleUpSvgImageContentAtTop((int) (shiftX + fixedHeight), shiftY, scaleX, scaleY, withText, null);
		}
		double newScaleX = - fixedHeight / centralX * scaleX;
		return getScaleUpSvgImageContentAtTop(shiftX, shiftY, newScaleX, scaleY, withText, null);
	}

	public String getScaleUpSvgImageContentAtTop(int shiftX, int shiftY, double scaleX, double scaleY, boolean withText) {
		return getScaleUpSvgImageContentAtTop(shiftX, shiftY, scaleX, scaleY, withText, null);
	}
	
	/**
	 * Plot the dendrogram at the top side. 
	 * Return only the xml contents (without the parent svg tag), suite for combining with other shapes.
	 * 
	 * @param shiftX
	 * @param shiftY
	 * @param scaleX
	 * @param scaleY
	 * @param withText
	 * @param labelIdPrefix To assign a prefix for the label (text elements) id. A null value will use the default 'label_top_'
	 * @return
	 */
	public String getScaleUpSvgImageContentAtTop(int shiftX, int shiftY, double scaleX, double scaleY, boolean withText, String labelIdPrefix) {
		
		StringBuffer ret = new StringBuffer();
		
		int correctionX = (int) (- centralX * scaleX + 10);
		int correctionY = 10;

		// draw lines
		ret.append("<g stroke=\"rgb(0,0,0)\" stroke-width=\"1\">\n");
		for (List<Integer> line : getAllLines()) {
			ret.append(String.format("\t<line y1=\"%d\" x1=\"%d\" y2=\"%d\" x2=\"%d\" />\n", (int) (line.get(P1_X)
					* scaleX + correctionX + shiftX), (int) (line.get(P1_Y) * scaleY + correctionY + shiftY),
					(int) (line.get(P2_X) * scaleX + correctionX + shiftX), (int) (line.get(P2_Y) * scaleY
							+ correctionY + shiftY)));
		}
		ret.append(String.format("\t<line y1=\"%d\" x1=\"%d\" y2=\"%d\" x2=\"%d\" />\n", (int) (centralX
				* scaleX + correctionX + shiftX), (int) (centralY * scaleY + correctionY + shiftY),
				(int) (centralX * scaleX + correctionX + shiftX -5), (int) (centralY * scaleY
						+ correctionY + shiftY)));
		ret.append("</g>\n");
		
		// the label id will be label_top_{sequence index} here	
		if (withText) {
			// TODO untested, to be confirmed
			ret.append(String.format("<g font-family=\"Arial\" font-size=\"%d\" fill=\"black\">\n", fontSize));
			if (labels == null || labels.isEmpty()) {
				for (int i = 0; i < sequence.size(); i++) {
					int textX = 6 + correctionX + shiftX;
					int textY = (int) (i * LINE_HEIGHT * scaleY) + 4 + correctionY + shiftY;
					ret.append(String.format("\t<text x=\"%d\" y=\"%d\" transform=\"rotate(270, %d, %d)\" id=\"label_top_%s\">%s</text>\n",
							textY, textX, textY, textX, sequence.get(i), sequence.get(i)));
				}
			} else {
				for (int i = 0; i < sequence.size() && i < labels.size(); i++) {
					int index = Integer.valueOf(sequence.get(i)).intValue();
					String label = labels.get(index);
					int textX = 6 + correctionX + shiftX;
					int textY = (int) (i * LINE_HEIGHT * scaleY) + 4 + correctionY + shiftY;
					ret.append(String.format("\t<text x=\"%d\" y=\"%d\" transform=\"rotate(270, %d, %d)\" id=\"label_top_%s\">%s</text>\n",
							textY, textX, textY, textX, sequence.get(i), label));
				}
			}
			ret.append("</g>\n");
		}

		ret.append("<g stroke-width=\"1\" stroke=\"none\" class=\"dendroNodes\" id=\"dendroNodesT\">\n");
		Map<List<Integer>, Dendrogram> allNodes = getAllNodes();
		for (List<Integer> node: allNodes.keySet()) {
			int nY = (int) (node.get(P1_X) * scaleX + correctionX + shiftX);
			int nX = (int) (node.get(P1_Y) * scaleY + correctionY + shiftY);
			List<String> members = allNodes.get(node).getSequence();

			ret.append(String.format("\t<circle cx=\"%d\" cy=\"%d\" r=\"5\" fill=\"none\" class=\"dendroNode\">\n", 
					nX, nY));
			if (labelIdPrefix == null) {
				labelIdPrefix = "label_top_";
			}
			ret.append("\t\t<desc>" + labelIdPrefix + StringUtils.join(members, ("," + labelIdPrefix)) + "</desc>\n");
			if (labels != null && !labels.isEmpty()) {
				List<String> memberLabels = new ArrayList<String>();
				for (String index : members) {
					memberLabels.add(labels.get(Integer.valueOf(index)));
				}
				ret.append("\t\t<desc>" + StringUtils.join(memberLabels, "///") + "</desc>\n");
			}

			ret.append("\t</circle>\n");
		}
		ret.append("</g>\n");

		// distance and the scale bar
		ret.append("<g>\n");
		int h = (int) (sequence.size() * LINE_HEIGHT * scaleY);
		ret.append(String.format(
				"\t<line y1=\"%d\" x1=\"%d\" y2=\"%d\" x2=\"%d\" style=\"stroke:rgb(0,0,0);stroke-width:1\" />\n",
				10 + shiftX, h + correctionY + shiftY + 6, correctionX + shiftX, h + correctionY + shiftY + 6));
		ret.append(String.format(
				"\t<line y1=\"%d\" x1=\"%d\" y2=\"%d\" x2=\"%d\" style=\"stroke:rgb(0,0,0);stroke-width:1\" />\n",
				10 + shiftX, h + correctionY + shiftY + 6, 10 + shiftX, h + correctionY + shiftY + 12));
		ret.append(String
				.format("\t<line y1=\"%d\" x1=\"%d\" y2=\"%d\" x2=\"%d\" style=\"stroke:rgb(0,0,0);stroke-width:1\" />\n",
						correctionX + shiftX, h + correctionY + shiftY + 6, correctionX + shiftX, h + correctionY
								+ shiftY + 12));
		String dist = String.format("d = %.2f", distance);
		if (dist.equals("d = 0.00")) {
			dist = "distance < 0.005";
			// TODO unchecked!
			ret.append(String
					.format("\t<text y=\"%d\" x=\"%d\" style=\"text-anchor: start; font-family: Arial; font-size: %d; fill: black; font-style: italic;\">%s</text>\n",
							correctionX / 2 + shiftX, h + correctionY + shiftY + 16, fontSize, dist));
		} else {
			ret.append(String
					.format("\t<text y=\"%d\" x=\"%d\" style=\"text-anchor: start; font-family: Arial; font-size: %d; fill: black; font-style: italic;\">%s</text>\n",
							(int) (centralX * scaleX) / 2 + correctionX + shiftX, h + correctionY + shiftY + 16, fontSize, dist));
		}
		
		ret.append("</g>\n");
		
		return ret.toString();
	}

}
