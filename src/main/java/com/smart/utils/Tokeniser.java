package com.smart.utils;

import java.util.ArrayList;
import java.util.List;

import com.smart.analyzer.Chinese;
import com.smart.analyzer.Japanese;

/**
 * 分词类
 * 
 * @author ddd
 * 
 */
public class Tokeniser {

	/**
	 * 从文件提取文本，判断语言。并根据语言选择合适的分词器分词。
	 * 
	 * @param input 文件的路径
	 * @return
	 */
	public List<String> partition(String input) {

		List<String> result = null;
		try {
			String content = FileParser.parse(input).get(FileParser.CONTENTS);

			if (Global.isJapanese(content)) {
				result = new Japanese().analyzer(content);
			} else {
				result = new Chinese().analyzer(content);
			}
			
		} catch (Exception e) {
			// TODO: 添加系统LOG
			e.printStackTrace();
			
			// 如果分词错误，返回空
			result = new ArrayList<String>();
		}

		// TODO: 添加过滤用词
		List<String> filtered = new ArrayList<String>();
		for (String word: result) {
			if (word.equals("的") || word.equals("1") || word.equals("2")) {
				continue;
			}
			filtered.add(word);
		}
		
		return filtered;
	}
}
