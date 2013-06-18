package com.smart.service;

import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import com.smart.Configuration;
import com.smart.analyzer.Chinese;
import com.smart.analyzer.Japanese;
import com.smart.thumbnail.Gatherer;
import com.smart.utils.FileParser;
import com.smart.utils.Global;

public class ClassifyService implements Daemon {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(DaemonContext arg0) throws DaemonInitException, Exception {
		// TODO Auto-generated method stub
		Configuration.conf = new PropertiesConfiguration("server.properties");
	}

	@Override
	public void start() throws Exception {

		int res = new Gatherer().takeHTML();
		if (res > -1000) {
			return;
		}
		
		String f = "テスト工程の品質.xlsx";
//		f = "the-little-mongodb-book-ja.pdf";
//		f = "001 新人教育.pdf";
//		f = "数学需求.rtf";
		
		String content = FileParser.parse("/Users/lilin/Desktop/" + f).get(FileParser.CONTENTS);

		List<String> result;
		if (Global.isJapanese(content)) {
			result = new Japanese().analyzer(content);
		} else {
			result = new Chinese().analyzer(content);
		}

		System.out.println(result.size());

	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

}
