package it.geosolutions.filesystemmonitor.monitorpolling;

import java.io.File;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileListener implements org.quartz.jobs.FileScanListener {
     private static Log logger = LogFactory.getLog(FileListener.class);

     public void fileUpdated(String fileName) {
          File file = new File(fileName);
          Timestamp modified = new Timestamp(file.lastModified());
System.out.println(fileName + " was changed at " + modified );
          logger.info( fileName + " was changed at " + modified );
     }
}
