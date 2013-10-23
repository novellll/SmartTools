package com.smart.classifier;

import java.util.List;
import java.util.Map;

public class TermLibrary {
	
	/**
	 * 词库
	 */
	List<String> terms;

	/**
	 * 出现某一词的文档数
	 * 
	 */
	Map<String, Integer> docFreq;
}
