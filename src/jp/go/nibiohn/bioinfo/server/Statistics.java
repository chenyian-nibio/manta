package jp.go.nibiohn.bioinfo.server;

import java.util.Arrays;
import java.util.List;

public class Statistics {
	private double[] data;
	private int size;

	public Statistics(double[] data) {
		this.data = data;
		size = data.length;
	}

	public Statistics(List<Double> data) {
		size = data.size();
		this.data = new double[size];
		for (int i = 0; i < data.size(); i++) {
			this.data[i] = data.get(i);
		}
	}

	double getMean() {
		double sum = 0.0;
		for (double a : data)
			sum += a;
		return sum / size;
	}

	double getVariance() {
		double mean = getMean();
		double temp = 0;
		for (double a : data)
			temp += (a - mean) * (a - mean);
		return temp / (size - 1);
	}

	public double getStdDev() {
		return Math.sqrt(getVariance());
	}

	public double median() {
		Arrays.sort(data);

		if (data.length % 2 == 0) {
			return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
		}
		return data[data.length / 2];
	}
}