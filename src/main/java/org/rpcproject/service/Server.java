package org.rpcproject.service;

import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Server {
    private Logger logger = LoggerFactory.getLogger(Server.class);
    public void startServer(int port) throws IOException, InterruptedException {
        io.grpc.Server server = ServerBuilder.forPort(port)
                .addService(new EduCostService())
                .build();

        this.logger.info("Started the server at port : "+port);

        server.start();
        server.awaitTermination();
    }
}
