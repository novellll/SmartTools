package com.smart.service;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import com.smart.Configuration;
import com.smart.analyzer.MqReceiver;

public class AnalyzerService implements Daemon {

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
		MqReceiver.main(null);

	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

}
