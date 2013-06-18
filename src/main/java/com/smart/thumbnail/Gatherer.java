package com.smart.thumbnail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Gatherer {

	/**
	 * phantomjs执行的JS文件
	 */
	private static final String FILE_JS = "src/com/smart/thumbnail/hello.js";
	
	/**
	 * phantomjs可执行文件的位置
	 */
	private static final String FILE_PHANTOM = "/Users/lilin/developer/phantomjs-1.7.0-macosx/bin/phantomjs";

	/**
	 * 
	 * 关于运行外部程序，参考：http://www.ne.jp/asahi/hishidama/home/tech/java/process.html
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public int takeHTML() throws IOException, InterruptedException {
		
		ProcessBuilder pb = new ProcessBuilder(FILE_PHANTOM, FILE_JS);
		
//		Map<String, String> env = pb.environment();	//環境変数を取得
//		env.put("PATH", "/Users/lilin/developer/phantomjs-1.7.0-macosx/bin");
//		System.out.println(env);
		
		Process p = pb.start();
		return p.waitFor();
		
//		InputStream is = p.getInputStream();	//標準出力
//		printStdout(is);
		
	}
	
	public int takeMSOffice() {
		
		// TODO: OpenOffice实现？
		return 0;
	}
	
	public int takePDFfile() {
		
		// TODO: PDFBox或OpenOffice实现？
		return 0;
	}
	
	public void printStdout(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			for (;;) {
				String line = br.readLine();
				if (line == null) break;
				System.out.println(line);
			}
		} finally {
			br.close();
		}
	}
}
