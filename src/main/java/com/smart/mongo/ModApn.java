package com.smart.mongo;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.smart.Configuration;

public class ModApn {

	private Mongo m;
	private DB db;
	private DBCollection coll;

	public ModApn() throws Exception{
		String dbHost = Configuration.conf.getString("mongo_host");
		int dbPort = Configuration.conf.getInt("mongo_port");
		String dbName = Configuration.conf.getString("mongo_db");

		this.m = new MongoClient( dbHost , dbPort );
		this.db = m.getDB( dbName );
		this.coll = db.getCollection("apns");
	}
	
	/**
	 * 用用户ID获取设备token
	 * @param uid
	 * @return
	 */
	public List<String> getToken(String uid) {
		
		BasicDBObject query = new BasicDBObject("deviceowner", uid);
		
		DBCursor cursor = coll.find(query);
		List<String> result = new ArrayList<String>();
		try {
		   while(cursor.hasNext()) {
			   DBObject row = cursor.next();
			   result.add((String)row.get("devicetoken"));
		   }
		} finally {
		   cursor.close();
		}
		
		System.out.println(result);
		return result;
	}
}
