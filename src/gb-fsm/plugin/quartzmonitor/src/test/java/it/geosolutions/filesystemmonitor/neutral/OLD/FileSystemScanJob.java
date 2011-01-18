/**
 * 
 */
package it.geosolutions.filesystemmonitor.neutral.OLD;

import it.geosolutions.filesystemmonitor.monitor.impl.GBEventConsumer;

import java.io.File;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * 
 * This class wrap the Tree to execute the update from
 * the QuartzScheduler
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class FileSystemScanJob implements StatefulJob {
    private Tree fileSystem=null;
    
    public FileSystemScanJob(final String filter, final File root, final GBEventConsumer consumer) {
        fileSystem=new Tree(filter, root, consumer);
    }


    public void execute(JobExecutionContext context) throws JobExecutionException {
        try{
            fileSystem.update();
            // check and notify
            fileSystem.check();
        }
        catch (Exception e){
            throw new JobExecutionException(e);
        }
    }
    
    /**
     * @TODO this method should be deprecated since it's not handled by interface
     * anyway tree should be cleared on the job dispose... 
     */
    public void clear(){
        fileSystem.clear();
    }
}
