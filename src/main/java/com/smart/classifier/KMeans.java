package com.smart.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

/**
 * 聚类
 */
public class KMeans {
	
	private int k;

	private TFIDFMeasure tf;
	
	private TermVector tv;

	/**
	 * 第一维 分类个数k，第二维 是文档
	 */
	private List<List<Integer>> clusters;
	
	private List<Integer> centerPoint;

	private Random random;
	
	/**
	 * 构造函数，初始化变量
	 * @param k 分类个数
	 * @param docs 文档的路径
	 * @param tf 特征向量
	 */
	public KMeans(int k, String[] docs, TFIDFMeasure tf) {
		
		this.tf = tf;
		this.k = k;
		this.tv = new TermVector();
		
		this.centerPoint = new ArrayList<Integer>();
		this.clusters = new ArrayList<List<Integer>>();
		this.random = new Random(new Date().getTime());
		
		for (int i = 0; i < k; i++) {
			this.clusters.add(new ArrayList<Integer>());
		}

		this.initClusterPoint(tf);
	}

	/**
	 * 聚类
	 */
	public void cluster() {
		double[][] matrix = this.tf.getMatrix();

		// 计算每个文档和每个聚类中心的距离
		for (int i = 0; i < tf.docs.size(); i++) {

			// 计算某个文档离哪个聚类最近
			int nearest = 0;
			double min = Double.MAX_VALUE;
			for (int j = 0; j < centerPoint.size(); j++) {
				double distance = this.getDistance(matrix[i], matrix[centerPoint.get(j)]);
				if (distance < min) {
					min = distance;
					nearest = j;
				}
			}
			this.clusters.get(nearest).add(i);
		}
	}
	
	/**
	 * 计算某数据离聚类中心的距离
	 * 用余弦夹角来计算某数据离聚类中心的距离(欧式距离计算)
	 * @param coord
	 * @param center
	 * @return
	 */
	double getDistance(double[] coord, double[] center) {
		return 1 - this.tv.computeCosineSimilarity(coord, center);
	}
	
	/** 
	 * 初始化聚类中心。
	 * 第一个点随意选取，第二个点找离第一个点最远的，从第三个开始按照最小最大原则选取。
	 * 
	 * @param tf
	 */
	private void initClusterPoint(TFIDFMeasure tf) {

		// 用于存放除了聚类点文档之外的文档索引
		centerPoint.add(random.nextInt(tf.docs.size()));
		
		this.setFarthestPoint(centerPoint);
		
		// 采用最小最大原则选取其它聚类中心(2为 最小分类数)
		for (int category = 2; category < k; category++) {

			// 除了聚类中心以外的文档以及这些文档与各个聚类中心中的最小距离（最大最小原则）
			Map<Integer, Float> minDistance = new HashMap<Integer, Float>();
			
			for (int docIndex = 0; docIndex < this.tf.docs.size(); docIndex++) {
				minDistance.put(docIndex, 1F);
				
				// 取与中心点最近的文档号
				for (int centerPointIndex = 0; centerPointIndex < centerPoint.size(); centerPointIndex++) {
					float distance = 1F - (float)tf.getSimilarity(docIndex, centerPoint.get(centerPointIndex));
					
					if (distance < minDistance.get(docIndex)) {
						minDistance.put(docIndex, distance);
					}
				}
			}

			// 从这些最小距离里选取距离最大的文档添加到聚类中心
			int minMaxDocument = 0;
			float minMaxDistance = 0;
			Iterator<Entry<Integer, Float>> iterator = minDistance.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, Float> item = iterator.next();
				if (item.getValue() > minMaxDistance) {
					minMaxDistance = item.getValue();
					minMaxDocument = item.getKey();
				}
			}
			centerPoint.add(minMaxDocument);
		}
		
		 // 记录中心点文档属于哪个聚类
		for (int i = 0; i < centerPoint.size(); i++) {
			clusters.get(i).add(centerPoint.get(i));
		}
	}
	
	/**
	 * 设定第二个聚类中心。这个聚类中心是与第一个聚类中心最远的文档。
	 * @param points
	 */
	private void setFarthestPoint(List<Integer> points) {
		float maxDistance = 0F;
		int point = 0;
		
		for (int i = 0; i < tf.docs.size(); i++) {
			if ( 1F - (float)tf.getSimilarity(i, points.get(0)) > maxDistance) {
				maxDistance = 1F - (float)tf.getSimilarity(i, points.get(0));
				point = i;
			}
		}
		
		points.add(point);
	}
    public List<List<String>> getKeywords(){
    	List<List<String>> kw = new ArrayList<List<String>>();
    	for (int q = 0; q < clusters.size(); q++) {
    		List<Integer> members = clusters.get(q);
			Map<String, Integer> keywords = new HashMap<String, Integer>();
			List<Map<String, Integer>> wordfreq = new ArrayList<Map<String, Integer>>();
			for (int i : members) {
				wordfreq.add(tf.getWordFrequency(i));
			}
			
			for (int i = 0; i < wordfreq.size(); i++) {
				for (Iterator<String> e = wordfreq.get(i).keySet().iterator(); e
						.hasNext();) {
					String key = e.next();
					Integer val = (Integer) wordfreq.get(i).get(key);

					if (!keywords.containsKey(key)) {
						keywords.put(key, val);
					} else {
						Integer newval = keywords.get(key) + val;
						keywords.put(key, newval);
					}
				}
			}
			
			List<Map.Entry<String, Integer>> newkeywords = new ArrayList<Map.Entry<String, Integer>>(
					keywords.entrySet());
			Collections.sort(newkeywords,
					new Comparator<Map.Entry<String, Integer>>() {
						public int compare(Map.Entry<String, Integer> o1,
								Map.Entry<String, Integer> o2) {
							return (o2.getValue()).compareTo(o1.getValue());
						}
					});
						
			kw.add(new ArrayList<String>());
			for (int j = 0; j < 5; j++) {
				Entry<String, Integer> kw11 = newkeywords.get(j);
				kw.get(q).add(kw11.getKey());

			}
		}
    	return kw;

    }
	public List<List<Integer>> getClusters() {
		return clusters;

	}
}
