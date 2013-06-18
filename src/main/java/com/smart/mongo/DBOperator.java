package com.smart.mongo;

import java.io.File;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.smart.Configuration;

public class DBOperator {
	private MongoClient m;
	private DB db;
	private GridFS fs;
	
	public DBOperator() throws Exception{
		String dbHost = Configuration.conf.getString("mongo_host");
		int dbPort = Configuration.conf.getInt("mongo_port");
		String dbName = Configuration.conf.getString("mongo_db");

		m = new MongoClient(dbHost , dbPort);
		db = m.getDB( dbName );
		fs = new GridFS(db);
	}
	
	public void getUserPhoto(String fid, String tmpFile)throws Exception{
		GridFSDBFile file = fs.find(new ObjectId(fid));
		file.writeTo(new File(tmpFile));
	}
	
	public String savePhoto(File f) throws Exception{
		GridFSInputFile inputFile = fs.createFile(f);
		inputFile.save();
		return inputFile.getId().toString();
	}
	
	public DB getDB(){
		return this.db;
	}
	
	public void DBClose(){
		this.m.close();
	}
}
