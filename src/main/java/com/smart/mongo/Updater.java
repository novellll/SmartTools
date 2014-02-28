package com.smart.mongo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import com.smart.Configuration;

import com.mongodb.gridfs.GridFSDBFile;

public class Updater {

	private static final Logger log = Logger.getLogger(Updater.class);
	//更新素材内容，thumb字段，现在要添加到扩展字段里，这个方法可能要调用三次？还是调用一次
	public void updateMsgThumb(String code, String messageId ,String fileId,int height,int width,String collection) throws Exception{
		DBOperator dbop = new DBOperator(code);
		log.debug("connect to mongodb successfully.");
		
		DBCollection messages = dbop.getDB().getCollection(collection);
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(messageId));
		log.debug("query: " + query.toString());
		
		BasicDBObject message = (BasicDBObject) messages.findOne(query);
		BasicDBObject thumb = new BasicDBObject();
		thumb.put("fileid", fileId);
		thumb.put("height", height);
		thumb.put("width", width);
		message.put("thumb", thumb);
		WriteResult res = messages.update(query, message);
        log.debug("result: " + res.toString());
        
        dbop.DBClose();
	}
	
	public void updateKeywords(String code, String id, String collection, String[] keywords) throws Exception {
		
		List<String> params = new ArrayList<String>();
		for (String keyword : keywords) {
			params.add(keyword);
		}
		
		updateKeywords(code, id, collection, params);
	}
	
	/**
	 * 通过mongodb在应用程序之间传递参数<br>
	 * box里的数据由母程序负责删除
	 * @param key
	 * @param params
	 * @throws Exception
	 */
	public void transfer(String code, String key, String params) throws Exception {

		String dbHost = Configuration.conf.getString("mongo_host");
		int dbPort = Configuration.conf.getInt("mongo_port");

		MongoClient m = new MongoClient( dbHost , dbPort );
		DB db = m.getDB( code );

		DBCollection templates = db.getCollection("box");
		
		// TODO: 内容大的时候，经过GridFS传递
		BasicDBObject doc = new BasicDBObject();
		doc.put("key", key);
		doc.put("params", JSON.parse(params));
		
		WriteResult res = templates.insert(doc);
		
        log.debug(res.toString());
        m.close();
	}

	public void updateIndex(String code, String target, String type, String word, String count, String lang) throws Exception {

		String dbHost = Configuration.conf.getString("mongo_host");
		int dbPort = Configuration.conf.getInt("mongo_port");

		MongoClient m = new MongoClient( dbHost , dbPort );
		DB db = m.getDB( code );

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
	public void updateKeywords(String code, String id, String collection, List<String> keywords) throws Exception {
		
		String dbHost = Configuration.conf.getString("mongo_host");
		int dbPort = Configuration.conf.getInt("mongo_port");

		MongoClient m = new MongoClient( dbHost , dbPort );
		DB db = m.getDB( code );

		DBCollection templates = db.getCollection(collection);
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(id));
		
		BasicDBObject doc = (BasicDBObject) templates.findOne(query);
		doc.put("keywords", keywords);
        
        WriteResult res = templates.update(query, doc);
        log.debug(res.toString());
		
        m.close();
	}
	
	//for YUKARi 把图片信息加一个适合YUKARi的外套
	//把gridfs里的图片信息提取出来，按照smartcore的形式存入到files里,并返回新的fid
	//2014.1.20，吴擒龙
	private String translateFileId(DBOperator dbop, String gridId) throws Exception{
		
		DBCollection YUKARiFiles = dbop.getDB().getCollection("files");
		
		//get information of the big picture
		GridFSDBFile smartFile = dbop.getInfo(gridId);
		
		//create file like smartcore into files from the information of big photo
		BasicDBObject newSmartFile = new BasicDBObject(); 
		
		//add information into smartfile from the big photo inserted in to gridfs
		newSmartFile.put("fileId",smartFile.getId());
		newSmartFile.put("length",smartFile.getLength());
		newSmartFile.put("name",smartFile.getFilename());
		newSmartFile.put("contentType",smartFile.getContentType());
		newSmartFile.put("valid",1);
		newSmartFile.put("__v",0);
		
		//insert the smartfile into file
		YUKARiFiles.insert(newSmartFile);
		
		//find id of file by fileId
		BasicDBObject fileId = new BasicDBObject("fileId", smartFile.getId());
		newSmartFile = (BasicDBObject) YUKARiFiles.findOne(fileId);
		
		String newId = newSmartFile.getString("_id");
		
		return newId;
	}
	
	
	
	//create three kinds of photo and update user's or group's photo property
	public void updateUserPhoto(String code, String id, String file, String collection) throws Exception{
		DBOperator dbop = new DBOperator(code);
		log.debug("connect to mongodb successfully.");
	
		
		File bigFile = new File(file + "big");
		File middleFile = new File(file + "middle");
		File smallFile = new File(file + "small");
		
		//save big photo to mongo
		String bigPhotoId = dbop.savePhoto(bigFile);		
		bigPhotoId = translateFileId(dbop,bigPhotoId);
		
		log.debug("save bigFile successfully." + bigPhotoId);
		
		//save middle photo to mongo
		String middlePhotoId = dbop.savePhoto(middleFile);		
		middlePhotoId = translateFileId(dbop,middlePhotoId);
		
		log.debug("save middleFile successfully." + middlePhotoId);
		
		//save small photo to mongo
		String smallPhotoId = dbop.savePhoto(smallFile);	
		smallPhotoId = translateFileId(dbop,smallPhotoId);
		
		log.debug("save smallFile successfully." + smallPhotoId);
		
		//query user
		DBCollection users = dbop.getDB().getCollection(collection);
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(id));
		BasicDBObject user = (BasicDBObject) users.findOne(query);
		log.debug("find "+collection+" and id = " + user.getString("id"));
		
		//update user's photo
		BasicDBObject thumb = new BasicDBObject();
		thumb.put("big", bigPhotoId);
		thumb.put("middle", middlePhotoId);
		thumb.put("small", smallPhotoId);
		
		BasicDBObject extend = (BasicDBObject) user.get("extend");
		
		extend.put("thumb",thumb);
		
        user.put("extend", extend);
        
        WriteResult res = users.update(query, user);
        log.debug("result: " + res.toString());
		
        bigFile.delete();
        middleFile.delete();
        smallFile.delete();
        
        dbop.DBClose();
	}
	
	/**
	 * 追加图片到指定的collection中
	 * @param id record id
	 * @param file 物理文件（全路径）
	 * @param collection
	 * @param key
	 * @throws Exception
	 */
	public void addImage(String code, String id, String file, String collection, String key)
			throws Exception {
		
		DBOperator dbop = new DBOperator(code);
		log.debug("connect to mongodb successfully.");
		
		//save big picture into fs.file
		// save big photo to mongo
		String imageId = dbop.savePhoto(new File(file));
		log.debug("save bigFile successfully.");
		
		
		// query update data
		DBCollection rows = dbop.getDB().getCollection(collection);
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(id));
		BasicDBObject row = (BasicDBObject) rows.findOne(query);
		log.debug("find " + collection + " and id = " + row.getString("_id"));
		
		// update image
		setNestDBObject(row, key, translateFileId(dbop,imageId));
		WriteResult res = rows.update(query, row);
		log.debug("result: " + res.toString());

		dbop.DBClose();
	}

	/**
	 * 设定内嵌文档
	 * @param row 更新对象
	 * @param key 带点的key，如key1.key2.key3
	 * @param data 值
	 */
	private void setNestDBObject(BasicDBObject row, String key, String data) {
		
		String[] keys = key.split("\\.");

		BasicDBObject layout = (BasicDBObject)row.get(keys[0]);
		BasicDBObject image = (BasicDBObject)layout.get(keys[1]);
		if (image == null) {
			image = new BasicDBObject();
			layout.put(keys[1], image);
		}
		image.put(keys[2], data);
	}
	
	public static void main(String[] args) throws Exception {
		Configuration.conf = new PropertiesConfiguration("server.properties");
		new Updater().addImage("testdb", "5211deecf4d1e1b43f000008", "/Users/lilin/Desktop/logo.jpg", "users", "photo3.a.f");
	}
}
