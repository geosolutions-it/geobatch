package it.geosolutions.filesystemmonitor.monitorpolling;
/**
 * 
 */

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class FileSystemMonitorTest {


    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        File dir = new File("/home/carlo");
        FileFilter wildcardf = new WildcardFileFilter(".f*");
        File[] files = dir.listFiles(wildcardf);
        for (int i = 0; i < files.length; i++) {
          System.out.println(files[i]);
        }
        
        
        
        
        IOFileFilter wildCardFilter = new WildcardFileFilter("**");
        
//        IOFileFilter filter=FileFilterUtils.and(wildcardFilter,FileFilterUtils.notFileFilter(FileFilterUtils.prefixFileFilter(".")));

        File directory = new File(new File("/home/carlo/work/data/emsa/"), "in");
        FileAlterationObserver observer = new FileAlterationObserver(directory,wildCardFilter);
//        FileAlterationListener fal=new GBFileAlterationListener();
        
 //       observer.addListener(fal);
        observer.initialize();
        FileAlterationMonitor fam=new FileAlterationMonitor(1000, observer);
        System.out.println("Starting");
        fam.start();
        System.out.println("Started");
        Thread.sleep(100000);
        
        fam.stop();
        
        System.out.println("Stopped");
        
        observer.destroy();
    }

}