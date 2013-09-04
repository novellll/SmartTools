package com.smart.apns;

import java.io.InputStream;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.smart.Configuration;
import com.smart.mongo.ModApn;

public class ApnUtils {

	private static final Logger log = Logger.getLogger(ApnUtils.class); 

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		Configuration.conf = new PropertiesConfiguration("server.properties");
		ApnUtils self = new ApnUtils();
		
		ApnsService service = self.createService();
		self.push(service, "5211deecf4d1e1b43f000031", "aaa");

//		String payload = APNS.newPayload().alertBody("日本語を送る").build();
//		String token = "7323839f022c6f7cb1ade9840143a53299315a065ece93a040e8e46486a93b24";
//		service.push(token, payload);
		
	}
	
	/**
	 * 发送通知
	 * @param service
	 * @param target
	 * @param body
	 * @throws Exception 
	 */
	public void push(ApnsService service, String target, String body) throws Exception {
		String payload = APNS.newPayload().sound("default").alertBody(body).build();
		
		for (String token : new ModApn().getToken(target)) {
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
