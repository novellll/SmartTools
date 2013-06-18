package com.smart.apns;

import java.io.IOException;
import java.util.Map;

import net.arnx.jsonic.JSON;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.notnoop.apns.ApnsService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.smart.Configuration;

public class MqReceiver {
	
	private static final Logger log = Logger.getLogger(MqReceiver.class); 
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		
		Configuration.conf = new PropertiesConfiguration("server.properties");

		String mqHost = Configuration.conf.getString("rabbit_host");
		String mqQueue = Configuration.conf.getString("rabbit_queue_apn");

		log.debug("start");
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(mqHost);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(mqQueue, false, false, false, null);
		log.info(" [*] Waiting for messages. To exit press CTRL+C");

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(mqQueue, true, consumer);
		
		ApnUtils apn = new ApnUtils();
		ApnsService service = apn.createService();

		while (true) {
			log.debug("delivery");
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			
			String message = new String(delivery.getBody(), "UTF-8");
			Map<String, String> res = JSON.decode(message);
			log.debug(message);
			
			String target = res.get("target");
			String body = res.get("body");
			
			apn.push(service, target, body);

			log.debug("Processing the next message");
		}	
	}
}
