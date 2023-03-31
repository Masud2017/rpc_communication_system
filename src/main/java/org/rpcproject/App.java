package org.rpcproject;

import org.rpcproject.dao.MongoDBDataImporter;
import org.rpcproject.service.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    private static Logger logger = LoggerFactory.getLogger(App.class);
    public static void main( String[] args ) throws IOException, InterruptedException {
//        Server server = new Server();
//        server.startServer(4444);

        MongoDBDataImporter importer = new MongoDBDataImporter();
        importer.startImporting();
    }
}
