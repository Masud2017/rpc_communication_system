package org.rpcproject.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Environment {
    private static Properties properties;
    private static Environment instance;
    private Environment() throws IOException {

    }

    public static Environment getInstance() throws IOException {
        if (instance == null || properties == null) {
            properties = new Properties();
            FileInputStream fileInputStream = new FileInputStream("application.properties");
            properties.load(fileInputStream);

            instance = new Environment();
        }
        return instance;
    }

    public String getByKey(String key) {
        return this.properties.getProperty(key);
    }
}
