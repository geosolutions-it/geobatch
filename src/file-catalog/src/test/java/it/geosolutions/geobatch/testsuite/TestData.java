package it.geosolutions.geobatch.testsuite;

import java.io.File;

public class TestData {
    
    public static File file(Object listener, String path){
        return new File(path);
    }
    
    public static File temp(Object listener, String path){
        return new File(path);
    }

}
