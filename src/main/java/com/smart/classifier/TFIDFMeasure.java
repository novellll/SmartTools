package com.smart.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smart.utils.Tokeniser;

/**
 * 计算所有词在每一篇文档中的权重，获得文档的特征向量。
 * 
 * @author ddd
 * 
 */
public class TFIDFMeasure {

	List<Document> docs = new ArrayList<Document>();
	TermLibrary library = new TermLibrary();

	public TFIDFMeasure(String[] documents) {
		library.terms = this.generateTerms(documents);
		this.generateTermWeight();
	}

	/**
	 * 遍历所有文档，逐个分词，并创建词库（即不重复的所有的单词集合）。 <br>
	 * 并且，计算词频及逆文档频率。
	 * 
	 * @param docNames
	 * @return
	 * @throws Exception
	 */
	private ArrayList<String> generateTerms(String[] docNames) {

		ArrayList<String> uniques = new ArrayList<String>();
		Tokeniser tokenizer = new Tokeniser();

		// 初始化词库
		library.docFreq = new HashMap<String, Integer>();
		int documentIndex = 0;

		for (String docName : docNames) {

			Document doc = new Document(documentIndex++);
			List<String> words = tokenizer.partition(docName);

			// 产生词频：词在文档中出现的次数
			Map<String, Integer> freq = new HashMap<String, Integer>();
			for (String word : words) {

				// 生成词库
				if (!uniques.contains(word)) {
					uniques.add(word);
				}

				// 词频计数
				String loverWord = word.toLowerCase();
				if (freq.containsKey(loverWord)) {
					freq.put(loverWord, freq.get(loverWord) + 1);
				} else {
					freq.put(loverWord, 1);
				}

				// 逆文档频率计数
				Integer val = library.docFreq.get(word);
				if (val == null) {
					library.docFreq.put(word, 1);
				} else {
					library.docFreq.put(word, val + 1);
				}
			}

			doc.termFreq = freq;
			doc.sumWordFreq = words.size();

			this.docs.add(doc);
		}

		return uniques;
	}

	/**
	 * 计算权重
	 */
	private void generateTermWeight() {
		for (int i = 0; i < library.terms.size(); i++) {
			for (int j = 0; j < this.docs.size(); j++) {
				String word = library.terms.get(i);
				this.docs.get(j).termWeight.put(word, computeTermWeight(word,
						j));
			}
		}
	}

	/**
	 * 计算TF
	 * 
	 * @param term
	 * @param doc
	 * @return
	 */
	private float GetTermFrequency(String term, int doc) {
		if (this.docs.get(doc).termFreq.get(term) == null) {
			return 0;
		}

		int freq = this.docs.get(doc).termFreq.get(term); // 词在某一文档里的词频
		int maxfreq = this.docs.get(doc).sumWordFreq;
		return ((float) freq / (float) maxfreq);
	}

	/**
	 * 逆文档频率，idf
	 * 
	 * @param term
	 * @return
	 */
	private float computeDocumentFrequency(String term) {
		float df = library.docFreq.get(term);
		float wordcount = this.docs.size();
		return (float) Math.log(wordcount / df);
	}

	/**
	 * 计算词在某一个文档中的权重
	 * 
	 * @param term
	 * @param doc
	 * @return
	 */
	private float computeTermWeight(String term, int doc) {
		float tf = GetTermFrequency(term, doc) + 1.0f;
		float idf = computeDocumentFrequency(term);
		return (float) Math.log(tf) * idf;
	}

	/**
	 * 计算文档的特征向量(TFIDF权重向量)
	 * 
	 * @param doc
	 * @return
	 */
	public double[] getDocumentVector(int doc) {
		double[] w = new double[library.terms.size()];
		for (int i = 0; i < library.terms.size(); i++) {
			w[i] = this.docs.get(doc).termWeight.get(library.terms.get(i));
		}

		return w;
	}

	/**
	 * 生成文档的向量矩阵。<br>
	 * 生成k-means的输入数据,是一个联合数组,<br>
	 * 第一维表示文档个数，第二维表示所有文档分出来的所有词。
	 * 
	 * @return
	 */
	public double[][] getMatrix() {
		double[][] data = new double[docs.size()][];
		for (int i = 0; i < docs.size(); i++) {
			data[i] = this.getDocumentVector(i); 
		}

		return data;
	}

	/**
	 * 计算相似度
	 * 
	 * @param doc_i
	 * @param doc_j
	 * @return
	 */
	public double getSimilarity(int doc_i, int doc_j) {
		double[] vector1 = getDocumentVector(doc_i);
		double[] vector2 = getDocumentVector(doc_j);
		return new TermVector().computeCosineSimilarity(vector1, vector2);
	}

	public Map<String, Integer> getWordFrequency(int doc) {
		return this.docs.get(doc).termFreq;
	}
}
