package org.rpcproject.service;


import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.rpcproject.compiled_protobuf_classes.EconomicStateQuery;
import org.rpcproject.compiled_protobuf_classes.ExpensiveStateQuery;
import org.rpcproject.compiled_protobuf_classes.QueryRequest;
import org.rpcproject.compiled_protobuf_classes.ResponseToQuery;
import org.rpcproject.dao.RpcMongoDataRepository;
import org.rpcproject.utils.Environment;

import javax.print.Doc;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class EduCostService extends org.rpcproject.compiled_protobuf_classes.EduCostServiceGrpc.EduCostServiceImplBase {
    private RpcMongoDataRepository repository = new RpcMongoDataRepository(Environment.getInstance().getByKey("db.name"),
            Environment.getInstance().getByKey("db.collection_name"));

    public EduCostService() throws IOException {
    }

    @Override
    public void queryCost(QueryRequest request, StreamObserver<ResponseToQuery> responseObserver) {
        ResponseToQuery res = this.repository.getEduCost(request.getYear(),request.getState(),request.getType(), request.getLength()
                , request.getExpense());
        responseObserver.onNext(res);
        responseObserver.onCompleted();

        Document doc = new org.bson.Document("Cost",res.getCost());

        this.repository.createAndSaveDataToNewCollection("EduCostStatQueryOne",doc);
        super.queryCost(request, responseObserver);
    }

    @Override
    public void queryExpensiveStateList(ExpensiveStateQuery request, StreamObserver<org.rpcproject.compiled_protobuf_classes.ExpensiveStateResponse> responseObserver) {
        org.rpcproject.compiled_protobuf_classes.ExpensiveStateResponse res = this.repository.getExpensiveStatList(request.getYear(), request.getType(), request.getLength());
        responseObserver.onNext(res);
        responseObserver.onCompleted();

        Document doc = new Document("States",res.getStatesList());
        this.repository.createAndSaveDataToNewCollection("EduCostStatQueryTwo",doc);
        super.queryExpensiveStateList(request, responseObserver);
    }

    @Override
    public void queryEconomicStateList(EconomicStateQuery request, StreamObserver<org.rpcproject.compiled_protobuf_classes.EconomicStateResponseList> responseObserver) {
        List< org.rpcproject.compiled_protobuf_classes
                .EconomicStateResponse> economicStateResponseList = this.repository
                .queryEconomicStat(request.getYear(), request.getType(), request.getLength());

        org.rpcproject.compiled_protobuf_classes.EconomicStateResponseList
                .Builder responseList = org.rpcproject.compiled_protobuf_classes.EconomicStateResponseList
                .newBuilder()
                .addAllEconomicStatResponseList(economicStateResponseList);

//        int idx = 0;
//        for (org.rpcproject.compiled_protobuf_classes
//                .EconomicStateResponse economicStateResponseItem: economicStateResponseList) {
//            responseList.setEconomicStatResponseList(idx,economicStateResponseItem);
//            idx++;
//        }

        List<org.rpcproject.compiled_protobuf_classes.EconomicStateResponse> list= responseList.build().getEconomicStatResponseListList();
        for (org.rpcproject.compiled_protobuf_classes.EconomicStateResponse item : list) {
            Document doc = new Document();
            doc.append("Year",item.getYear());
            doc.append("Length",item.getLength());
            doc.append("Type",item.getType());
            doc.append("Expense",item.getExpense());

            this.repository.createAndSaveDataToNewCollection("EduCostStatQueryThree",doc);

        }
        responseObserver.onNext(responseList.build());
        responseObserver.onCompleted();

        super.queryEconomicStateList(request, responseObserver);
    }

    @Override
    public void queryHighestGrowthRateList(org.rpcproject.compiled_protobuf_classes.HighestGrowthRateQuery request, StreamObserver<org.rpcproject.compiled_protobuf_classes.HighestGrowthRateResponseList> responseObserver) {
        String[] yearArr = new String[request.getYearsList().size()];
        int idx = 0;
        for (String yearItem : request.getYearsList()) {
            yearArr[idx] = yearItem;
            idx++;
        }
        List<org.rpcproject.compiled_protobuf_classes.HighestGrowthRateResponse> responseList = this.repository
                .queryHighestGrowthRateList(yearArr, request.getType(), request.getLength());
        org.rpcproject.compiled_protobuf_classes.HighestGrowthRateResponseList
                .Builder responseList1 = org.rpcproject.compiled_protobuf_classes.HighestGrowthRateResponseList
                .newBuilder()
                .addAllHighestGrowthRateResponseList(responseList);

//        int idx2 = 0;
//        for (org.rpcproject.compiled_protobuf_classes.HighestGrowthRateResponse resItem : responseList) {
//            responseList1.setHighestGrowthRateResponseList(idx2,resItem);
//            idx2++;
//        }

        responseObserver.onNext(responseList1.build());
        responseObserver.onCompleted();

        Iterator itter = responseList1.build().getHighestGrowthRateResponseListList().iterator();

        while(itter.hasNext()) {
            org.rpcproject.compiled_protobuf_classes.HighestGrowthRateResponse itm = (org.rpcproject.compiled_protobuf_classes.HighestGrowthRateResponse) itter.next();
            Document doc = new Document();
            doc.append("Stat",itm.getState());
            this.repository.createAndSaveDataToNewCollection("EduCostStatQueryFour",doc);
        }

        super.queryHighestGrowthRateList(request, responseObserver);
    }

    @Override
    public void queryAggragateRegionsOverallExpense(org.rpcproject.compiled_protobuf_classes.AggragateRegionsOverallExpenseQuery request, StreamObserver<org.rpcproject.compiled_protobuf_classes.AggragateRegionsOverallExpenseResponse> responseObserver) {
        Document doc = new Document();
        doc.append("Expense",this.repository.queryRegionsAverageExpense(request.getType(), request.getLength(), request.getRegion(), request.getYear()));
//        this.repository.createAndSaveDataToNewCollection("EduCostStatQueryFive",doc);

        responseObserver.onNext(this.repository.queryRegionsAverageExpense(request.getType(), request.getLength(), request.getRegion(), request.getYear()));
        responseObserver.onCompleted();
        super.queryAggragateRegionsOverallExpense(request, responseObserver);
    }
}
