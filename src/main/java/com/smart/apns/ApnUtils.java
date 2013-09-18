package com.smart.apns;

import java.io.InputStream;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.smart.Configuration;
import com.smart.mongo.ModApn;
import com.smart.mongo.ModDevice;

public class ApnUtils {

	private static final Logger log = Logger.getLogger(ApnUtils.class); 

	/**
	 * 发送密码
	 * @param service
	 * @param target
	 * @param body
	 * @throws Exception 
	 */
	public void pushPassword(ApnsService service, String code, String uid, String body) throws Exception {
		String payload = APNS.newPayload().sound("default").alertBody(body).build();
		
		for (String token : new ModDevice(code).getToken(uid)) {
			service.push(token, payload);
			log.debug("token : " + token);
		}
		
		log.debug("uid id : " + uid);
		log.debug("push message : " + body);
	}
	/**
	 * 发送通知
	 * @param service
	 * @param target
	 * @param body
	 * @throws Exception 
	 */
	public void push(ApnsService service, String code, String target, String body) throws Exception {
		String payload = APNS.newPayload().sound("default").alertBody(body).build();
		
		for (String token : new ModDevice(code).getToken(target)) {
			service.push(token, payload);
			log.debug("token : " + token);
		}
		
		log.debug("target : " + target);
		log.debug("push message : " + body);
	}
	
	/**
	 * 生成服务实例
	 * @return
	 */
	public ApnsService createService() {
		
		ApnsService service = null;
				
		String certFile = Configuration.conf.getString("apn_cert_file");
		String certPass = Configuration.conf.getString("apn_cert_pass");
		String certFileDev = Configuration.conf.getString("apn_cert_file_dev");
		String certIsProd = Configuration.conf.getString("apn_cert_production");
		
		if ("yes".equals(certIsProd)) {
			// Production mode
			log.debug("file : " + certFile);
			InputStream is = getClass().getClassLoader().getResourceAsStream(certFile);
			service = APNS.newService()
				    .withCert(is, certPass)
				    .withProductionDestination()
				    .build();
		} else {
			// Development mod
			InputStream is = getClass().getClassLoader().getResourceAsStream(certFileDev);
			service = APNS.newService()
				    .withCert(is, certPass)
				    .withSandboxDestination()
				    .build();
		}
		
		return service;
	}
}
