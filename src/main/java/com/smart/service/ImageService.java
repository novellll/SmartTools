package com.smart.service;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import com.smart.Configuration;
import com.smart.thumbnail.ImageScissors;

public class ImageService implements Daemon {

	@Override
	public void destroy() {
		System.out.println("destroy");

	}

	@Override
	public void init(DaemonContext arg0) throws DaemonInitException, Exception {
		Configuration.conf = new PropertiesConfiguration("server.properties");
	}

	@Override
	public void start() throws Exception {
		System.out.println("start");
		ImageScissors.main(null);
	}

	@Override
	public void stop() throws Exception {
		System.out.println("stop");

	}

}
