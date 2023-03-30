package org.rpcproject.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.rpcproject.compiled_protobuf_classes.ExpensiveStateResponse;
import org.rpcproject.utils.Environment;

import java.io.IOException;
import java.util.*;

/**
 * <h1>RpcMongoDataRepository</h1>
 * A class that handles data query operations for mongoDb.
 * */
public class RpcMongoDataRepository {
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection mongoCollection;

    /**
     * Constructor.
     * @param dbName - Name of the database
     * @param collectionName - name of the collection name
     * */
    public RpcMongoDataRepository(final String dbName,
                                  final String collectionName)
            throws IOException {
        this.mongoClient = MongoDBSingleton.getInstance(Integer
                        .parseInt(Environment
                                .getInstance()
                                .getByKey("db.port")),
                true);
        this.mongoDatabase = this.mongoClient.getDatabase(dbName);
        this.mongoCollection = this.mongoDatabase.getCollection(collectionName);
    }

    public org.rpcproject.compiled_protobuf_classes.ResponseToQuery getEduCost(String year, String state, String type,
                                                                               String length, String expense) {

        Document doc = new Document();
        doc.append("Year",year);
        doc.append("Stat",state);
        doc.append("Type",type);
        doc.append("Length",length);
        doc.append("Expense",expense);
        Document result = (Document) this.mongoCollection.find(doc).first();

//        org.rpcproject.compiled_protobuf_classes.EduCost eduCost = org.rpcproject.compiled_protobuf_classes.EduCost
//                .newBuilder()
//                .setYear(result.getString("Year"))
//                .setStat(result.getString("Stat"))
//                .setType(result.getString("Type"))
//                .setLength(result.getString("Length"))
//                .setValue(result.getString("Value"))
//                .setExpense(result.getString("Expense"))
//                .build();

        org.rpcproject.compiled_protobuf_classes.ResponseToQuery response = org.rpcproject.compiled_protobuf_classes.ResponseToQuery
                .newBuilder()
                .setCost(result.getString("Value"))
                .build();
        return response;
    }

    public org.rpcproject.compiled_protobuf_classes.ExpensiveStateResponse getExpensiveStatList(String year, String type, String length) {

        Document doc = new Document();
        doc.append("Length",length);
        doc.append("Year",year);
        doc.append("Type",type);

        FindIterable<Document> resultList = this.mongoCollection.find(doc);



        List<String> statList = new ArrayList<>();
        for (Document documentItem : resultList) {
//            org.rpcproject.compiled_protobuf_classes.EduCost tempEducost = org.rpcproject.compiled_protobuf_classes.EduCost.newBuilder()
//                    .setExpense(documentItem.getString("Expense"))
//                    .setYear(documentItem.getString("Year"))
//                    .setStat(documentItem.getString("Stat"))
//                    .setType(documentItem.getString("Type"))
//                    .setLength(documentItem.getString("Length"))
//                    .setValue(documentItem.getString("Value"))
//                    .build();

            statList.add(documentItem.getString("Stat"));
        }

        ExpensiveStateResponse.Builder response = org.rpcproject.compiled_protobuf_classes.ExpensiveStateResponse
                .newBuilder()
                .addAllStates(statList);

        return response.build();
    }

    public List<org.rpcproject.compiled_protobuf_classes
            .EconomicStateResponse> queryEconomicStat(String year, String type, String length) {
        List<org.rpcproject.compiled_protobuf_classes
                .EconomicStateResponse> economicStateResponseList = new ArrayList<>();
        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$match", Filters
                .and(Filters.eq("Year", year), Filters
                        .eq("Type", type), Filters.eq("Length", length))));
        pipeline.add(new Document("$group", new Document("_id", "$Stat")
                .append("Expense", new Document("$sum", "$Value"))));
        pipeline.add(new Document("$sort", Sorts.ascending("Expense")));
        pipeline.add(new Document("$limit", 5));

        MongoCursor<Document> cursor = this.mongoCollection.aggregate(pipeline).iterator();

        while(cursor.hasNext()) {
            Document doc = cursor.next();

            String state = doc.getString("_id");
            int expense = doc.getInteger("Expense");

            org.rpcproject.compiled_protobuf_classes.EconomicStateResponse economicStateResponse = org.rpcproject.compiled_protobuf_classes.EconomicStateResponse
                    .newBuilder()
                    .setExpense(String.valueOf(expense))
                    .setLength(length)
                    .setType(type)
                    .setYear(year)
                    .build();
            economicStateResponseList.add(economicStateResponse);

        }

        return economicStateResponseList;
    }

    public List<org.rpcproject.compiled_protobuf_classes
            .HighestGrowthRateResponse> queryHighestGrowthRateList(String[] yearsStr, String type, String length) {
        List<org.rpcproject.compiled_protobuf_classes
                .HighestGrowthRateResponse> highestGrowthRateResponseArrayList = new ArrayList<>();
        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$match", Filters
                .and(Filters.in("Year", yearsStr), Filters
                        .eq("Type", type), Filters
                        .eq("Length", length))));
        pipeline.add(new Document("$group", new Document("_id", new Document("State", "$Stat")
                .append("Year", "$Year")).append("Expense", new Document("$sum", "$Value"))));
        pipeline.add(new Document("$sort", Sorts.ascending("_id.State", "_id.Year")));
        pipeline.add(new Document("$group", new Document("_id", "$_id.State")
                .append("Expense", new Document("$push", "$Expense"))));
        pipeline.add(new Document("$project", new Document("_id", 1)
                .append("GrowthRates", new Document("$map", new Document("input", "$Expense").append("as", "expense")
                        .append("in", new Document("$divide", Arrays.asList(new Document("$subtract", Arrays
                                .asList("$expense", new Document("$arrayElemAt", Arrays.asList("$Expense", -1)))),
                                new Document("$arrayElemAt", Arrays.asList("$Expense", -1)))))))));
        pipeline.add(new Document("$unwind", "$GrowthRates"));
        pipeline.add(new Document("$group", new Document("_id", "$_id").append("AverageGrowthRate",
                new Document("$avg", "$GrowthRates"))));
        pipeline.add(new Document("$sort", Sorts.descending("AverageGrowthRate")));
        pipeline.add(new Document("$limit", 5));

        MongoCursor<Document> cursor = this.mongoCollection.aggregate(pipeline).iterator();



        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String state = doc.getString("_id");
            double averageGrowthRate = doc.getDouble("AverageGrowthRate");
            org.rpcproject.compiled_protobuf_classes
                    .HighestGrowthRateResponse highestGrowthRateResponse = org.rpcproject.compiled_protobuf_classes
                    .HighestGrowthRateResponse
                    .newBuilder()
                    .setState(state)
                    .setExpense(String.valueOf(averageGrowthRate))
                    .build();
        }

        return highestGrowthRateResponseArrayList;
    }

    public org.rpcproject.compiled_protobuf_classes
            .AggragateRegionsOverallExpenseResponse queryRegionsAverageExpense(String type,
                                                                               String length,
                                                                               String region,
                                                                               String year) {
        List<String> statList = this.getRegionInfo().get(region);

        List<Document> resultList = new ArrayList<>();

        for(String statItem : statList) {
            Document tempdoc = new Document();
            tempdoc.append("State",statItem);
            Document filterInfo = new Document();
            filterInfo.append("Year",year);
            filterInfo.append("Length",length);
            filterInfo.append("Type",type);

            Document result = (Document) this.mongoCollection.find(tempdoc).filter(filterInfo).first();
            resultList.add(result);
        }

        org.rpcproject.compiled_protobuf_classes.AggragateRegionsOverallExpenseResponse respone =
                org.rpcproject.compiled_protobuf_classes.AggragateRegionsOverallExpenseResponse
                        .newBuilder()
                        .setYear(year)
                        .setOverallExpense(this.getOverAllExpenseFromRegionDataList(resultList))
                        .build();

        return respone;
    }

    public boolean createAndSaveDataToNewCollection(String collectionName, Document collectionData,Document uniqueDoc) {
        try {
            // if not exist
            this.mongoDatabase.createCollection(collectionName);
        } catch(MongoCommandException e) {

        }
        MongoCollection collection = this.mongoDatabase.getCollection(collectionName);
        if (collection.find(uniqueDoc).first() == null) {
            collection.insertOne(collectionData);
        }

        return false;
    }

    protected String getOverAllExpenseFromRegionDataList(List<Document> resultList) {
        Integer expense = 0;
        for (Document documentItem : resultList) {
            expense += Integer.parseInt(documentItem.getString("Value"));
        }

        return String.valueOf(expense);
    }

    protected Map<String,List<String>> getRegionInfo() {
        Map<String,List<String>> map = new HashMap<>();
        List<String> regionWestList = Arrays.asList( "California", "Colorado", "Hawaii", "Nevada", "Oregon", "Washington");
        map.put("West",regionWestList);

        List<String> regionNortheastList = Arrays.asList("Connecticut", "Maine", "Massachusetts", "New Hampshire", "New Jersey", "New York", "Pennsylvania", "Rhode Island", "Vermont");
        map.put("Northeast",regionNortheastList);

        List<String> regionSouthList = Arrays.asList("Alabama",
                "Arkansas", "Delaware", "District of Columbia", "Florida", "Georgia", "Kentucky", "Louisiana", "Maryland", "Mississippi", "North Carolina", "Oklahoma", "South Carolina", "Tennessee", "Texas", "Virginia", "West Virginia");
        map.put("South",regionSouthList);

        List<String> regionMidwestList = Arrays.asList( "Illinois", "Indiana", "Iowa", "Kansas", "Michigan", "Minnesota", "Missouri", "Nebraska", "North Dakota", "Ohio", "South Dakota", "Wisconsin");
        map.put("Midwest",regionMidwestList);

        return map;
    }

    public static void main (String[] args) throws IOException {
        RpcMongoDataRepository repository = new RpcMongoDataRepository("rpc_communication","EduCostStat");
//        org.rpcproject.compiled_protobuf_classes
//                .AggragateRegionsOverallExpenseResponse response = repository
//                .queryRegionsAverageExpense("Public In-State","4-year","South","2013");
//        System.out.println(response.getYear() + "  "+response.getOverallExpense());
//
//
//
//        Document tempDoc = new Document();
//        tempDoc.append("Name","Jack2");
//        tempDoc.append("Email","jack2@yopmail.com");
//
//        Document uniqueDoc = new Document();
//        uniqueDoc.append("Email","jack2@yopmail.com");
//        repository.createAndSaveDataToNewCollection("test2",tempDoc,uniqueDoc);


        org.rpcproject.compiled_protobuf_classes.ResponseToQuery res = repository.getEduCost("2013","Alabama","Private","4-year","Fees/Tuition");
        System.out.println(res.getCost());

    }
}
