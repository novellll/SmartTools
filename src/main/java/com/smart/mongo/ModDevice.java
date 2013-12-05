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

public class ModDevice {
	private Mongo m;
	private DB db;
	private DBCollection coll;
	public ModDevice(String code) throws Exception{
		String dbHost = Configuration.conf.getString("mongo_host");
		int dbPort = Configuration.conf.getInt("mongo_port");
		System.out.println(dbHost);
		System.out.println(dbPort);
		this.m = new MongoClient( dbHost , dbPort );
		this.db = m.getDB( code );
		this.coll = db.getCollection("apns");
	}
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
