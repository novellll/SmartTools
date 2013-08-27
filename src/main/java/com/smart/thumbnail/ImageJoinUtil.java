package com.smart.thumbnail;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.smart.Configuration;
import com.smart.mongo.DBOperator;
import com.smart.mongo.Updater;
import com.smart.utils.ImageUtil;

public class ImageJoinUtil {
	
	private static final Logger log = Logger.getLogger(ImageJoinUtil.class); 
	
	public static void main(String[] args) throws Exception {
		
		Configuration.conf = new PropertiesConfiguration("server.properties");
		
		// mq info
		String mqHost = Configuration.conf.getString("rabbit_host");
		String mqQueue = Configuration.conf.getString("rabbit_queue_join");

		// get channel
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(mqHost);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare(mqQueue, false, false, false, null);

		// get consumer
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(mqQueue, true, consumer);
		
		while (true) {
			
			// delivery message
			QueueingConsumer.Delivery delivery = null;
			try {
				delivery = consumer.nextDelivery();
			} catch (Exception e) {
				log.error("delivery error!\t" + e.getMessage());
			}
			
			if(null == delivery){
				continue;
			}
			
			String message = new String(delivery.getBody(), "UTF-8");
			joinImage(message);
		}
	}
	
	public static void joinImage(String message) throws Exception {

		Updater updater = new Updater();
		DBOperator dbop = new DBOperator();

		// decode json message
		log.debug("received message : '" + message + "'");

		Map<String, Object> jsonData = null;
		try {
			jsonData = JSON.decode(message);
		} catch (Exception e) {
			log.error("parse json error!\t" + e.getMessage());
			return;
		}

		if (null == jsonData) {
			return;
		}

		String filePath = "/tmp/tmp_";

		// file name stored in server
		String id = (String) jsonData.get("id");

		// sample : files [{fid: 5211deecf4d1e1b43f000008, x: '0', y:'0', w:'200', h:'200'}]
		@SuppressWarnings("unchecked")
		List<Map<String, String>> files = (List<Map<String, String>>) jsonData
				.get("files");

		try {
			// get file from gridfs
			for (Map<String, String> file : files) {
				String fid = file.get("fid");
				dbop.getUserPhoto(fid, filePath + fid);
				file.put("file", filePath + fid);
			}

			// final image size
			int width = Integer.parseInt((String) jsonData.get("width"));
			int height = Integer.parseInt((String) jsonData.get("height"));
			ImageUtil.join(files, filePath + id, width, height);

		} catch (Exception e) {
			log.error("get image error!\t" + e.getMessage());
			return;
		}

		// the collection that need to update
		String collection = (String) jsonData.get("collection");
		String key = (String) jsonData.get("key");

		try {
			updater.addImage(id, filePath + id, collection, key);
		} catch (Exception e) {
			log.error("update user photo error!\t" + e.getMessage());
			return;
		}

		log.debug("update user's photo successfully.");
		try {
			for (Map<String, String> file : files) {
				new File(filePath + file.get("fid")).delete();
			}
			new File(filePath + id).delete();
		} catch (Exception e) {
			log.error("file delete error!\t" + e.getMessage());
			return;
		}
	}
}
