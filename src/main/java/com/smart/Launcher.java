package com.smart;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Java程序启动器。在一个jar包内含有多个main函数时，可以用该Launcher加参数的方式启动。
 * Ex. java -jar smart.jar com.smart.mq.OperatePhoto
 *   在smart.jar里指定Main-Class为Launcher
 * @author 
 */
public class Launcher {
	
	private static void init() throws ConfigurationException {
		Configuration.conf = new PropertiesConfiguration("server.properties");
	}

	/**
	 * @param args
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws ConfigurationException {
		
		init();
		
		String[] sendArgs = new String[args.length - 1];
		for (int i = 0; i < sendArgs.length; i++) {
			sendArgs[i] = args[i + 1];
		}

		String className = args[0];

		try {
			Class<?> main = Class.forName(className);
			Object[] vargs = { sendArgs };
			main.getMethod("main", new Class[] { String[].class }).invoke(null, vargs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
