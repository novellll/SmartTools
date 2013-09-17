package com.smart;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.smart.thumbnail.ImageJoinUtil;
import com.smart.thumbnail.ImageScissors;

/**
 * 启动器
 *
 */
public class Application {
	
	public static void main(String[] args) throws Exception {
		
		if (args.length < 2) {
			return;
		}
		
		Configuration.conf = new PropertiesConfiguration("server.properties");
		String type = args[0].trim().toLowerCase();
		String message = args[1];
		
		if ("join".equals(type)) {
			ImageJoinUtil.joinImage(message);
		}
		
		if ("cut".equals(type)) {
			ImageScissors.cut(message);
		}
	}
}
