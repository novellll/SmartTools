package com.smart.thumbnail;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.File;
import java.util.Map;

import javax.imageio.ImageIO;

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

public class ImageScissors {
	
	private static final Logger log = Logger.getLogger(ImageScissors.class); 
	
	public static void operation(String srcFile, String destFile, int x, int y, int destWidth, 
			int ratio, int finalWidth) throws Exception{
		
	    Image img;
	    ImageFilter cropFilter;
	    int destHeight = Math.round(destWidth*ratio);
	    int finalHeight = Math.round(finalWidth*ratio);

	    BufferedImage bi = ImageIO.read(new File(srcFile));
	    int srcWidth = bi.getWidth();
	    int srcHeight = bi.getHeight();

	    if (srcWidth >= destWidth + x && srcHeight >= destHeight + y) {
	    	//crop photo
	        cropFilter = new CropImageFilter(x, y, destWidth, destHeight);
	        img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(bi.getSource(), cropFilter));
	        //zoom photo
	        img = img.getScaledInstance(finalWidth, finalHeight, Image.SCALE_SMOOTH);
	        BufferedImage tag = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);
	        //draw photo
	        Graphics g = tag.getGraphics();
	        g.drawImage(img, 0, 0, null); 
	        g.dispose();
	        //write to file
	        ImageIO.write(tag, "JPEG", new File(destFile));
			log.debug("create photo successfully and file name is " + destFile);
	    }else {
	    	throw new Exception("parameters are not correct");
	    }
	}
	 
	public static void main(String[] args) throws Exception {
		
		Configuration.conf = new PropertiesConfiguration("server.properties");
		
		String mqHost = Configuration.conf.getString("rabbit_host");
		String mqQueue = Configuration.conf.getString("rabbit_queue_scissors");

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(mqHost);
//		factory.setVirtualHost("/");
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
			
			Map<String, String> res = null;
			try{
				res = JSON.decode(message);
			}catch(Exception e){
				log.error("parse json error!\t" + e.getMessage());
				continue;
			}
			if(null == res){
				continue;
			}
			//id
			String id = res.get("id");
			//file name stored in server
			String fid = res.get("fid");
			//the collection that need to update
			String collection = res.get("collection");
			if(null != id && "".equals(id) || null != fid && "".equals(fid)
					|| null != collection && "".equals(collection)){
				log.error("the parameter id, fid, collection can not be null or \"\"! ");
				continue;
			}
			
			int x = 0, y = 0, width=0;
			try{
				//the position of the x coordinate
				x = Integer.parseInt(res.get("x"));
				//the position of the y coordinate
				y = Integer.parseInt(res.get("y"));
				//Cutting width
				width = Integer.parseInt(res.get("width"));
			}catch(NumberFormatException e){
				log.error("NumberFormatException! x or y or width is not number! \t" + e.getMessage());
				continue;
			}

			if("users".equals(collection)||"groups".equals(collection)){
				String file = "/tmp/tmp" + fid;
				try{
					System.out.println(fid);
					System.out.println(file);
					dbop.getUserPhoto(fid, file);
				}catch(Exception e){
					log.error("get user photo error!\t" + e.getMessage());
					continue;
				}
				
				try{
					operation(file, file + "big", x, y, width, 1, 180);  //big size, 180x180
					operation(file, file + "middle", x, y, width, 1, 50);  //middle size, 50x50
					operation(file, file + "small", x, y, width, 1, 30);  //small size, 30x30
				}catch(Exception e){
					log.error("operation photo error!\t" + e.getMessage());
					continue;
				}
				
				try{
					updater.updateUserPhoto(id, file, collection);
				}catch(Exception e){
					log.error("update user photo error!\t" + e.getMessage());
					continue;
				}
				log.debug("update user's photo successfully.");
				try{
					new File(file).delete();
				}catch(Exception e){
					log.error("file delete error!\t" + e.getMessage());
					continue;
				}
			}
		}
	}
}
