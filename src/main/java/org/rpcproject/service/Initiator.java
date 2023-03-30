package org.rpcproject.service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.rpcproject.dao.MongoDBDataImporter;
import org.rpcproject.dao.MongoDBSingleton;
import org.rpcproject.utils.Environment;

import java.io.IOException;

/**
 * <h1>Initiator</h1>
 * A class that performs initial operations that is necessary before any process.
 * */
public class Initiator {
    private MongoDBDataImporter importer;
    private MongoClient client;

    private Initiator(MongoDBDataImporter importer) throws IOException {
        this.importer = importer;
        this.client = MongoDBSingleton.getInstance(Integer.parseInt(Environment.getInstance().getByKey("db.port")),true);
    }

    /**
     * Checks whether the database has the collection named EduCostStat. Creates and Seed data
     * to collection if it does not exist before.
     * */
    public synchronized void checkDbStatus() throws IOException {
        MongoDatabase db = this.client.getDatabase(Environment.getInstance().getByKey("db.name"));
        if(db.getCollection(Environment.getInstance().getByKey("db.collection_name")) == null) {
            this.importer.startImporting();
        }
    }
}
