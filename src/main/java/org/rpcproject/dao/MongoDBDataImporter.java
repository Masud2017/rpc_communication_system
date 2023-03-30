package org.rpcproject.dao;

import com.mongodb.MongoClient;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.rpcproject.utils.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * <h1>MongoDBDataImporter</h1>
 * A class that imports data to the mongo db database.
 * */
public class MongoDBDataImporter {
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private List<String> fileContentList;
    public MongoDBDataImporter() throws IOException {
        boolean flag = false;
        if (Environment.getInstance().getByKey("db.localhost") == "true") {
            flag = true;
        }

        this.mongoClient = MongoDBSingleton.getInstance(Integer.parseInt(Environment.getInstance().getByKey("db.port")),true);
        this.mongoDatabase = this.mongoClient.getDatabase(Environment.getInstance().getByKey("db.name"));
        this.mongoDatabase.createCollection(Environment.getInstance().getByKey("db.collection_name"));

        // reading data from csv file
        Scanner fileReader = new Scanner(new FileInputStream(new File("dataset/nces330_20.csv")));
        this.fileContentList = new ArrayList<>();
        while(fileReader.hasNext()) {
            this.fileContentList.add(fileReader.nextLine());
        }
    }

    public boolean startImporting() throws IOException {
        MongoCollection mongoCollection = this.mongoDatabase.getCollection(Environment.getInstance().getByKey("db.collection_name"));
        ClientSession session = this.mongoClient.startSession();

        List<WriteModel> writeModelList = new ArrayList<>();
        this.fileContentList.remove(0);
        for(String fileContentItem : this.fileContentList) {
            Document document = new Document();
            List<String> list = Arrays.asList(fileContentItem.split(","));

            document.append("Year",list.get(0));
            document.append("Stat",list.get(1));
            document.append("Type",list.get(2));
            document.append("Length",list.get(3));
            document.append("Expense",list.get(4));
            document.append("Value",list.get(5));

            WriteModel writeModel = new InsertOneModel(document);

            writeModelList.add(writeModel);
        }

        BulkWriteResult result = mongoCollection.bulkWrite(session,writeModelList);
        System.out.println("Number of data inserted : "+result.getInsertedCount());
        return true;
    }

    public static void main(String[] args) throws IOException {
        MongoDBDataImporter importer = new MongoDBDataImporter();
        importer.startImporting();
    }
}
