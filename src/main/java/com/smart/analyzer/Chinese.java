package com.smart.analyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;

public class Chinese {

	private static final Logger log = Logger.getLogger(Chinese.class); 
	
	public List<String> analyzer(String content) throws IOException {
		Analyzer analyzer = new MMSegAnalyzer(); //new ComplexAnalyzer();

		List<String> result = new ArrayList<String>();
		log.debug("============================================");
		
		StringReader reader = new StringReader(content);
		TokenStream stream = analyzer.tokenStream("", reader);
		displayTokens(stream, result);
		stream.close();
		analyzer.close();
		
		return result;
	}

	private void displayTokens(TokenStream stream, List<String> result) throws IOException {
		
		log.debug("|テキスト\t|開始\t|終了\t|読み\t\t|品詞");
		log.debug("--------------------------------------------");
		while(stream.incrementToken()) {
			
			CharTermAttribute termAtt = stream.getAttribute(CharTermAttribute.class);
			TypeAttribute rAtt = stream.getAttribute(TypeAttribute.class);
			OffsetAttribute oAtt = stream.getAttribute(OffsetAttribute.class);
			TermToBytesRefAttribute psAtt = stream.getAttribute(TermToBytesRefAttribute.class);
			
			String text = termAtt.toString();
			String yomi = rAtt.type();
			int sOffset = oAtt.startOffset();
			int eOffset = oAtt.endOffset();
			String pos = psAtt.toString();
			
			log.debug(
					"|" + text + "\t\t" +
					"|" + Integer.toString(sOffset) + "\t" + 
					"|" + Integer.toString(eOffset) + "\t" +
					"|" + yomi + "\t\t" + 
					"|" + pos + "\t"
					);
			
			result.add(text);
		}
	}
}
