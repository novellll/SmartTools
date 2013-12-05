package com.smart.classifier;

import java.util.HashMap;
import java.util.Map;

public class Document {
	int id;
	
	public Document(int id) {
		this.id = id;
		termFreq = new HashMap<String, Integer>();
		termWeight = new HashMap<String, Float>();
	}

	/**
	 *某一词在该文档中的词频
	 */
	Map<String, Integer> termFreq;
	
	/**
	 * 某一词在该文档中的权重
	 */
	Map<String, Float> termWeight;

	/**
	 * 一篇文档中所有词出现的次数和
	 */
	int sumWordFreq;
}
