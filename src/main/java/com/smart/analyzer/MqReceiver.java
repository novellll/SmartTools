package com.smart.analyzer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.smart.Configuration;
import com.smart.mongo.Updater;
import com.smart.utils.FileParser;

public class MqReceiver {
	
	private static final Logger log = Logger.getLogger(MqReceiver.class); 
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		
		String mqHost = Configuration.conf.getString("rabbit_host");
		String mqQueue = Configuration.conf.getString("rabbit_queue_analyzer");

		log.debug("start");
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(mqHost);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(mqQueue, false, false, false, null);
		log.info(" [*] Waiting for messages. To exit press CTRL+C");

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(mqQueue, true, consumer);
		
		Chinese chinese = new Chinese();
		Japanese japanese = new Japanese();
		Updater updater = new Updater();

		while (true) {
			log.debug("delivery");
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			
			String message = new String(delivery.getBody(), "UTF-8");
			Map<String, String> res = JSON.decode(message);
			log.debug(message);
			
			String target = res.get("target");
			String type = res.get("type");
			String body = res.get("body");
			
			// 解析文件内容
			if (type.equals("4")) {
				body = FileParser.parse(body).get("Contents");
			}
			
			// 日文分词
			List<String> jpWordsList = japanese.analyzer(body);
			for (String word : jpWordsList) {
				updater.updateIndex(target, type, word, "1", "japanese");
			}

			// 中文分词
			List<String> cnWordsList = chinese.analyzer(body);
			for (String word : cnWordsList) {
				updater.updateIndex(target, type, word, "1", "chinese");
			}
			
			log.debug("Processing the next message");
		}	
	}
}
