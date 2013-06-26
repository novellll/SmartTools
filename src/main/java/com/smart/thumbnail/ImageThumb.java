package com.smart.thumbnail;

import java.io.File;
import java.util.ArrayList;
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

public class ImageThumb {
	private static final Logger log = Logger.getLogger(ImageThumb.class);

	public static void main(String[] args) throws Exception {
		Configuration.conf = new PropertiesConfiguration("server.properties");

		String tmpPath = Configuration.conf.getString("tmp_filepath");
		String mqHost = Configuration.conf.getString("rabbit_host");
		String mqQueue = Configuration.conf.getString("rabbit_queue_thumb");
		System.out.println(mqQueue);

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(mqHost);
		// factory.setVirtualHost("/");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(mqQueue, false, false, false, null);
		log.info(" [*] Waiting for messages. To exit press CTRL+C");

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(mqQueue, true, consumer);

		Updater updater = new Updater();
		DBOperator dbop = new DBOperator();
		while (true) {
			QueueingConsumer.Delivery delivery = null;
			try {
				delivery = consumer.nextDelivery();
			} catch (Exception e) {
				log.error("get delivery error!\t" + e.getMessage());
			}
			if (null == delivery) {
				continue;
			}
			String message = new String(delivery.getBody(), "UTF-8");
			log.debug(" [x] Received '" + message + "'");

			Map<String, String> res = null;
			try {
				res = JSON.decode(message);
			} catch (Exception e) {
				log.error("parse json error!\t" + e.getMessage());
				continue;
			}
			if (null == res) {
				continue;
			}

			// file name stored in server
			String fids = res.get("fids");

			String messageId = res.get("msg_id");


			String collection = res.get("collection");
			log.debug("messageId:" + messageId + "fids:" + fids + "collection:"
					+ collection);
			int unionImageHeigth = 0;

			String[] fidArray = fids.split(",");

			ArrayList<String> tmpFile = new ArrayList<String>();
			for (String fid : fidArray) {
				String file = tmpPath + fid;
				tmpFile.add(file);
				try {
					dbop.getUserPhoto(fid, file);
				} catch (Exception e) {
					log.error("get user photo error!\t" + e.getMessage());
					continue;
				}

			}

			try {

				unionImageHeigth = ImageUnion.operation(tmpPath + messageId
						+ ".png", tmpFile);

				// 更新消息

			} catch (Exception e) {
				// TODO: handle exception
				log.error("union  error!\t" + e.getMessage());
				continue;
			}
			try {
				String savePhoto = dbop.savePhoto(new File(tmpPath + messageId
						+ ".png"));

				updater.updateMsgThumb(messageId, savePhoto, unionImageHeigth,
						500, collection);
			} catch (Exception e) {
				// TODO: handle exception
				log.error("inser  error!\t" + e.getMessage());
				continue;
			}
			// 清楚缓存
			try {

				for (String fid : fidArray) {
					String file = tmpPath + fid;
					try {
						new File(file).delete();
					} catch (Exception e) {
						log.error("delete photo 1 error!\t" + e.getMessage());
						continue;
					}

				}
				try {
					new File(tmpPath + messageId + ".png").delete();
				} catch (Exception e) {
					// TODO: handle exception
					log.error("delete photo 2 error!\t" + e.getMessage());
				}

			} catch (Exception e) {
				log.error("file delete error!\t" + e.getMessage());
				continue;
			}

		}
	}
}
