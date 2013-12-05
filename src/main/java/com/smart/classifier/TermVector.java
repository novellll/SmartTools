package com.smart.classifier;

/**
 * 计算相似度（主要功能是算数）
 * @author ddd
 *
 */
public class TermVector {
	
	/**
	 * 计算相似度
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public double computeCosineSimilarity(double[] vector1,
			double[] vector2) {

		double denom = (vectorLength(vector1) * vectorLength(vector2));
		return (denom == 0D) ? 0D : (innerProduct(vector1, vector2) / denom);
	}

	/**
	 * 向量的内积
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public double innerProduct(double[] vector1, double[] vector2) {

		double result = 0D;
		for (int i = 0; i < vector1.length; i++) {
			result += vector1[i] * vector2[i];
		}

		return result;
	}

	/**
	 * 向量的长度
	 * @param vector
	 * @return
	 */
	public double vectorLength(double[] vector) {
		
		double sum = 0.0D;
		for (int i = 0; i < vector.length; i++) {
			sum = sum + (vector[i] * vector[i]);
		}

		return (double) Math.sqrt(sum);
	}

}
