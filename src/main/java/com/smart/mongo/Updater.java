package com.smart.mongo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import com.smart.Configuration;

public class Updater {

	private static final Logger log = Logger.getLogger(Updater.class); 
	
	public void updateKeywords(String id, String collection, String[] keywords) throws Exception {
		
		List<String> params = new ArrayList<String>();
		for (String keyword : keywords) {
			params.add(keyword);
		}
		
		updateKeywords(id, collection, params);
	}
	
	/**
	 * 通过mongodb在应用程序之间传递参数<br>
	 * box里的数据由母程序负责删除
	 * @param key
	 * @param params
	 * @throws Exception
	 */
	public void transfer(String key, String params) throws Exception {

		String dbHost = Configuration.conf.getString("mongo_host");
		int dbPort = Configuration.conf.getInt("mongo_port");
		String dbName = Configuration.conf.getString("mongo_db");

		MongoClient m = new MongoClient( dbHost , dbPort );
		DB db = m.getDB( dbName );

		DBCollection templates = db.getCollection("box");
		
		// TODO: 内容大的时候，经过GridFS传递
		BasicDBObject doc = new BasicDBObject();
		doc.put("key", key);
		doc.put("params", JSON.parse(params));
		
		WriteResult res = templates.insert(doc);
		
        log.debug(res.toString());
        m.close();
	}

	public void updateIndex(String target, String type, String word, String count, String lang) throws Exception {

		String dbHost = Configuration.conf.getString("mongo_host");
		int dbPort = Configuration.conf.getInt("mongo_port");
		String dbName = Configuration.conf.getString("mongo_db");

		MongoClient m = new MongoClient( dbHost , dbPort );
		DB db = m.getDB( dbName );

		DBCollection templates = db.getCollection("fulltexts");
		
		BasicDBObject doc = new BasicDBObject();
		doc.put("type", type);
		doc.put("target", target);
		doc.put("word", word);
		doc.put("count", count);
		doc.put("lang", lang);
		doc.put("createby", "analyzer");
		doc.put("createat", new Date());
		
//		WriteResult res = templates.insert(doc);
		templates.insert(doc);
		
//        log.debug(res.toString());
        m.close();
	}

	/**
	 * 更新关键字
	 * @param id
	 * @param collection
	 * @param keywords
	 * @throws Exception
	 */
	public void updateKeywords(String id, String collection, List<String> keywords) throws Exception {
		
		String dbHost = Configuration.conf.getString("mongo_host");
		int dbPort = Configuration.conf.getInt("mongo_port");
		String dbName = Configuration.conf.getString("mongo_db");

		MongoClient m = new MongoClient( dbHost , dbPort );
		DB db = m.getDB( dbName );

		DBCollection templates = db.getCollection(collection);
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(id));
		
		BasicDBObject doc = (BasicDBObject) templates.findOne(query);
		doc.put("keywords", keywords);
        
        WriteResult res = templates.update(query, doc);
        log.debug(res.toString());
		
        m.close();
	}
	
	//create three kinds of photo and update user's or group's photo property
	public void updateUserPhoto(String id, String file, String collection) throws Exception{
		DBOperator dbop = new DBOperator();
		log.debug("connect to mongodb successfully.");
		
		File bigFile = new File(file + "big");
		File middleFile = new File(file + "middle");
		File smallFile = new File(file + "small");
		
		//save big photo to mongo
		String bigPhotoId = dbop.savePhoto(bigFile);
		log.debug("save bigFile successfully.");
		
		//save middle photo to mongo
		String middlePhotoId = dbop.savePhoto(middleFile);
		log.debug("save middleFile successfully.");
		
		//save small photo to mongo
		String smallPhotoId = dbop.savePhoto(smallFile);
		log.debug("save smallFile successfully.");
		
		//query user
		DBCollection users = dbop.getDB().getCollection(collection);
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(id));
		BasicDBObject user = (BasicDBObject) users.findOne(query);
		log.debug("find "+collection+" and id = " + user.getString("id"));
		
		//update user's photo
		BasicDBObject photos = new BasicDBObject();
		photos.put("big", bigPhotoId);
		photos.put("middle", middlePhotoId);
		photos.put("small", smallPhotoId);

        user.put("photo", photos);
        
        WriteResult res = users.update(query, user);
        log.debug("result: " + res.toString());
		
        bigFile.delete();
        middleFile.delete();
        smallFile.delete();
        
        dbop.DBClose();
	}
	
	public static void main(String[] args) throws Exception {
//		new Updater().updateKeywords("50375bd854aa400000000006", "templates", new String[]{"b", "a"});
	}
}
