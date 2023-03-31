package org.rpcproject;

import org.rpcproject.dao.MongoDBDataImporter;
import org.rpcproject.service.Server;


import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException {
        Server server = new Server();
        server.startServer(4444);

//        MongoDBDataImporter importer = new MongoDBDataImporter();
//        importer.startImporting();
    }
}
