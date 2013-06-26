package com.smart.service;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import com.smart.Configuration;
import com.smart.thumbnail.ImageScissors;
import com.smart.thumbnail.ImageThumb;

public class ThumbService implements Daemon {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		System.out.println("destroy");
	}

	@Override
	public void init(DaemonContext arg0) throws DaemonInitException, Exception {
		// TODO Auto-generated method stub
		Configuration.conf = new PropertiesConfiguration("server.properties");
	}

	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub
		ImageThumb.main(null);
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("stop");
	}

}
