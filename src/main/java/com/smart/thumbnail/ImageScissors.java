package com.smart.thumbnail;

import java.io.File;
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

public class ImageScissors {
	
	private static final Logger log = Logger.getLogger(ImageScissors.class); 
	
	public static void main(String[] args) throws Exception {
		
		Configuration.conf = new PropertiesConfiguration("server.properties");
		
		String mqHost = Configuration.conf.getString("rabbit_host");
		String mqQueue = Configuration.conf.getString("rabbit_queue_thumb");

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(mqHost);
//		factory.setVirtualHost("/");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(mqQueue, false, false, false, null);
		log.info(" [*] Waiting for messages. To exit press CTRL+C");

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(mqQueue, true, consumer);
		

		while (true) {
			QueueingConsumer.Delivery delivery = null;
			try{
				delivery = consumer.nextDelivery();
			}catch(Exception e){
				log.error("get delivery error!\t" + e.getMessage());
			}
			if(null == delivery){
				continue;
			}
			String message = new String(delivery.getBody(), "UTF-8");
			log.debug(" [x] Received '" + message + "'");
			
			cut(message);
		}
	}
	
	public static void cut(String message) throws Exception {

		Updater updater = new Updater();
		DBOperator dbop = new DBOperator();

		Map<String, String> params = null;

		try {
			params = JSON.decode(message);
		} catch (Exception e) {
			log.error("parse json error!\t" + e.getMessage());
			return;
		}
		
		if (null == params) {
			return;
		}
		
		// id
		String id = params.get("id");
		
		// file name stored in server
		String fid = params.get("fid");
		
		// the collection that need to update
		String collection = params.get("collection");
		
		// the position of the x coordinate
		int x = Integer.parseInt(params.get("x"));
		
		// the position of the y coordinate
		int y = Integer.parseInt(params.get("y"));
		
		// cutting width
		int width = Integer.parseInt(params.get("width"));

		String file = "/tmp/tmp" + fid;
		try {
			dbop.getUserPhoto(fid, file);
		} catch (Exception e) {
			log.error("get user photo error!\t" + e.getMessage());
			return;
		}

		try {
			ImageUtil.cut(file, file + "big", x, y, width, 0, 1, 512, 0);
			ImageUtil.cut(file, file + "middle", x, y, width, 0, 1, 256, 0);
			ImageUtil.cut(file, file + "small", x, y, width, 0, 1, 128, 0);
		} catch (Exception e) {
			log.error("operation photo error!\t" + e.getMessage());
			return;
		}

		try {
			updater.updateUserPhoto(id, file, collection);
		} catch (Exception e) {
			log.error("update user photo error!\t" + e.getMessage());
			return;
		}
		log.debug("update user's photo successfully.");
		try {
			new File(file).delete();
		} catch (Exception e) {
			log.error("file delete error!\t" + e.getMessage());
			return;
		}
	}
}
