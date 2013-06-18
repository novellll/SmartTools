package com.smart.utils;

import java.io.UnsupportedEncodingException;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class Global {

	public static final String LANG_JA = "ja";
	public static final String LANG_ZH = "zh-cn";

	/**
	 * 推测给定文本的语言(日语识别率低)
	 * 
	 * @param text
	 * @return 返回语言码（ISO 639 Language Codes）
	 * @throws LangDetectException
	 */
	public static String getTextLang(String text) throws LangDetectException {

		if (text == null || text.length() <= 0) {
			return "";
		}

		DetectorFactory.loadProfile("cybozu");
		Detector detector = DetectorFactory.create();
		detector.append(text);

		return detector.detect();
	}

	/**
	 * 判断文字是否是日文。该方法做成的目的是问了分词是选用合适的分词器。<br>
	 * 假设只要是日语文章，就会包含假名，不考虑日文汉字。最好是用getTextLang等更精准的语言判断。<br>
	 * 含有假名，含有全角数字或字母，就认为是日语<br>
	 * 全角数値、全角英文字、半角かな FF10(０) - FF9D(ﾝ)<br>
	 * かな 3041(ぁ) - 30F6(ヶ)<br>
	 * 
	 * @param text
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Boolean isJapanese(String text)
			throws UnsupportedEncodingException {

		float kana = 0L;
		
		for (int i = 0; i < text.length(); i++) {
			int point = Character.codePointAt(text, i);
			if (point > 12353 && point < 12534) {
				kana++;
			}
			if (point > 65269 && point < 65437) {
				kana++;
			}
		}
		
		// 假名的个文字数占全体的％1以上，就认为是日语文章
		float p = kana / text.length();
		return p > 0.1;
	}

	/**
	 * Unicode文字列に変換する("あ" -> "\u3042")
	 * 
	 * @param original
	 * @return
	 */
	protected static String convertToUnicode(String original) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < original.length(); i++) {
			sb.append(String.format("\\u%04X",
					Character.codePointAt(original, i)));
		}
		String unicode = sb.toString();
		return unicode;
	}

	/**
	 * Unicode文字列から元の文字列に変換する ("\u3042" -> "あ")
	 * 
	 * @param unicode
	 * @return
	 */
	protected static String convertToOiginal(String unicode) {
		String[] codeStrs = unicode.split("\\\\u");
		int[] codePoints = new int[codeStrs.length - 1]; // 最初が空文字なのでそれを抜かす
		for (int i = 0; i < codePoints.length; i++) {
			codePoints[i] = Integer.parseInt(codeStrs[i + 1], 16);
		}
		String encodedText = new String(codePoints, 0, codePoints.length);
		return encodedText;
	}
}
