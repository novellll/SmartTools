package com.smart.apns;

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
		self.push(service, "", "日本語をプッシュする");
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
		}
		
		log.debug("target : " + target);
		log.debug("push message : " + body);
	}
	
	/**
	 * 生成服务实例
	 * @return
	 */
	public ApnsService createService() {
		String certFile = Configuration.conf.getString("apn_cert_file");
		String certPass = Configuration.conf.getString("apn_cert_pass");
		
		ApnsService service = APNS.newService()
			    .withCert(certFile, certPass)
			    .withSandboxDestination()
			    .build();
		
		return service;
	}
}
