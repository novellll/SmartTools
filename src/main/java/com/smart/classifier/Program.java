package com.smart.classifier;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

public class Program {

	public static void main(String[] args) throws IOException {

		int k = 3; // 聚成3类
		String[] docs = fileList("C:/Users/dac/Desktop/new/");

		TFIDFMeasure tf = new TFIDFMeasure(docs);

		// 初始化k-means算法,第一个参数表示输入数据,第二个参数表示要聚成几类
		KMeans kmeans = new KMeans(k, docs, tf);

		// 开始迭代
		kmeans.cluster();

		// 获取聚类结果
		List<List<Integer>> clusters = kmeans.getClusters();
		List<List<String>> keywords = kmeans.getKeywords();
		for (int i = 0; i < clusters.size(); i++) {
			System.out.println(keywords.get(i));
			for (int j = 0; j < clusters.get(i).size(); j++)
				System.out.println(docs[j]);
		}

	}

	public static String[] fileList(String path) {
		String[] docs = new File(path).list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.matches(".*\\.pdf");
			}
		});

		String[] result = new String[docs.length];
		for (int i = 0; i < docs.length; i++) {
			result[i] = path + docs[i];
		}

		return result;
	}

}
