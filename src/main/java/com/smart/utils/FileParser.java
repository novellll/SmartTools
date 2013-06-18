package com.smart.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

public class FileParser {

	private static final Logger log = Logger.getLogger(FileParser.class); 
	public static final String CONTENTS = "Contents";
	
	/**
	 * 提取指定文件的纯文本内容，返回结果里包含文件的Metadata信息。文本内容用Key:Contents来保存。
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> parse(String file) throws Exception {
		
		Map<String, String> result = new HashMap<String, String>();
//		StringWriter out = new StringWriter();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		InputStream is = null;

		try {
			Metadata metadata = new Metadata();

			// read file
//			is = new BufferedInputStream(new FileInputStream(file));
			is = TikaInputStream.get(getURL(file), metadata);
			
			Detector detector = new DefaultDetector();
			Parser parser = new AutoDetectParser(detector);
			parser.parse(is, new BodyContentHandler(out), metadata, new ParseContext());

			// add metadata
			for (String name : metadata.names()) {
				String value = metadata.get(name);
				if (value != null) {
					result.put(name, value);
					log.debug("name: " + name + ", value: " + value);
				}
			}
			
			byte[] bytes = out.toByteArray();
			result.put(CONTENTS, new String(bytes, "UTF-8"));
		} finally {
			if (is != null) {
				is.close();
			}
		}
		
		return result;
	}
	
	protected static URL getURL(String urlstring) throws MalformedURLException {
		URL url;

        File file = new File(urlstring);
        if (file.isFile()) {
            url = file.toURI().toURL();
        } else {
            url = new URL(urlstring);
        }
        
        return url;
	}
	
	public static String pdfTextExtract(String file) throws FileNotFoundException, IOException {
		
		PDFParser pdfParser = new PDFParser(new FileInputStream(file));
		pdfParser.parse();

		PDDocument pdfDocument = pdfParser.getPDDocument();

		PDFTextStripper textStripper = new PDFTextStripper();
		return textStripper.getText(pdfDocument);
	}
}
