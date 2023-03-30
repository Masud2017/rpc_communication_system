package org.rpcproject.dao;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

public class MongoDBSingleton {
    private static MongoClient mongoClient;

    private MongoDBSingleton() {
    }

    public static MongoClient getInstance(Integer port,boolean localHost) {
        if (mongoClient == null) {
            if (localHost) {
                mongoClient = new MongoClient("localhost",port);
            }
        }
        return mongoClient;
    }
}
