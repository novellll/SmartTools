package com.smart.analyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

public class Japanese {

	private static final Logger log = Logger.getLogger(Japanese.class); 
	private static final String[] stoptags = {
		"記号-一般", "記号-読点", "記号-句点", "記号-空白", "記号-括弧開", "記号-括弧閉", "記号-アルファベット"
	};
	
	private Analyzer analyzer = new JapaneseAnalyzer(Version.LUCENE_43, 
			null,  
			JapaneseTokenizer.DEFAULT_MODE, 
			CharArraySet.EMPTY_SET, new HashSet<String>(Arrays.asList(stoptags)));
	
	public List<String> analyzer(String content) throws IOException {
		
		List<String> result = new ArrayList<String>();

//		log.debug("\n" + content);
		log.debug("============================================");
		StringReader reader = new StringReader(content);
		TokenStream stream = analyzer.tokenStream("", reader);
		displayTokens(stream, result);
		stream.close();
		
		return result;
	}
	
	private void displayTokens(TokenStream stream, List<String> result) throws IOException {
		
		log.debug("|テキスト\t|開始\t|終了\t|読み\t\t|品詞");
		log.debug("--------------------------------------------");
		while(stream.incrementToken()) {
			CharTermAttribute termAtt = stream.getAttribute(CharTermAttribute.class);
//			ReadingAttribute rAtt = stream.getAttribute(ReadingAttribute.class);
//			OffsetAttribute oAtt = stream.getAttribute(OffsetAttribute.class);
//			PartOfSpeechAttribute psAtt = stream.getAttribute(PartOfSpeechAttribute.class);
			
			String text = termAtt.toString();
//			String yomi = rAtt.getReading();
//			int sOffset = oAtt.startOffset();
//			int eOffset = oAtt.endOffset();
//			String pos = psAtt.getPartOfSpeech();
			
//			log.debug(
//					"|" + text + "\t\t" +
//					"|" + Integer.toString(sOffset) + "\t" + 
//					"|" + Integer.toString(eOffset) + "\t" +
//					"|" + yomi + "\t\t" + 
//					"|" + pos + "\t"
//					);
			
			result.add(text);
		}
	}
}