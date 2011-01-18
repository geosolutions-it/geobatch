package it.geosolutions.filesystemmonitor.monitorpolling;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.jobs.FileScanJob;

public class FileSystemWatcher {
     private static Log logger = LogFactory.getLog(FileSystemWatcher.class);

         public static void main(String[] args) {
             FileSystemWatcher example = new FileSystemWatcher();

              try {
                   Scheduler scheduler = example.createScheduler();
                   example.scheduleJob(scheduler);
                   scheduler.start();

              } catch (SchedulerException ex) {
                   logger.error(ex);
                   System.out.println(ex);
              }
        }

        protected Scheduler createScheduler() throws
             SchedulerException {

             return StdSchedulerFactory.getDefaultScheduler();
        }

        protected void scheduleJob(Scheduler scheduler) throws
             SchedulerException {

             // Store the FileScanListener instance
             scheduler.getContext().put("FileListener",new FileListener());

             // Create a JobDetail for the FileScanJob
             JobDetail jobDetail = new JobDetail("FileScanJob", null,
                       FileScanJob.class);
             

             // The FileScanJob needs some parameters
             JobDataMap jobDataMap = new JobDataMap();
             jobDataMap.put(FileScanJob.FILE_NAME,"/home/carlo/work/data/emsa/in");
             jobDataMap.put(FileScanJob.FILE_SCAN_LISTENER_NAME,"FileListener");
             jobDetail.setJobDataMap(jobDataMap);

             // Create a Trigger and register the Job
             Trigger trigger = TriggerUtils.makeSecondlyTrigger(1);
             trigger.setName("SimpleTrigger");
             trigger.setStartTime(new Date());

             scheduler.scheduleJob(jobDetail, trigger);
        }
    
}
