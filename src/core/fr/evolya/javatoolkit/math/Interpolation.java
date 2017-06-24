package fr.evolya.javatoolkit.math;

/**
 * http://paulbourke.net/miscellaneous/interpolation/
 */
public class Interpolation {

	public static double CosineInterpolate(double y1, double y2, double mu) {
		double mu2 = (1 - Math.cos(mu * Math.PI)) / 2;
		return (y1 * (1 - mu2) + y2 * mu2);
	}

	public static double LinearInterpolate(double y1, double y2, double mu) {
		return (y1 * (1 - mu) + y2 * mu);
	}

	public static double CubicInterpolate(double y0, double y1, double y2, double y3,
			double mu) {
		double a0, a1, a2, a3, mu2;
		mu2 = mu * mu;
		a0 = y3 - y2 - y0 + y1;
		a1 = y0 - y1 - a0;
		a2 = y2 - y0;
		a3 = y1;
		return (a0 * mu * mu2 + a1 * mu2 + a2 * mu + a3);
	}

	/*
	 * Tension: 1 is high, 0 normal, -1 is low Bias: 0 is even, positive is
	 * towards first segment, negative towards the other
	 */
	public static double HermiteInterpolate(double y0, double y1, double y2,
			double y3, double mu, double tension, double bias) {
		double m0, m1, mu2, mu3;
		double a0, a1, a2, a3;
		mu2 = mu * mu;
		mu3 = mu2 * mu;
		m0 = (y1 - y0) * (1 + bias) * (1 - tension) / 2;
		m0 += (y2 - y1) * (1 - bias) * (1 - tension) / 2;
		m1 = (y2 - y1) * (1 + bias) * (1 - tension) / 2;
		m1 += (y3 - y2) * (1 - bias) * (1 - tension) / 2;
		a0 = 2 * mu3 - 3 * mu2 + 1;
		a1 = mu3 - 2 * mu2 + mu;
		a2 = mu3 - mu2;
		a3 = -2 * mu3 + 3 * mu2;
		return (a0 * y1 + a1 * m0 + a2 * m1 + a3 * y2);
	}

}
