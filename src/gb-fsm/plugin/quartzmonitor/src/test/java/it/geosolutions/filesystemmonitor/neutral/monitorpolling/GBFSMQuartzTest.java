package it.geosolutions.filesystemmonitor.neutral.monitorpolling;
import static org.junit.Assert.fail;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;

import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;


public class GBFSMQuartzTest {

    @Test
    public final void testGBFileSystemMonitorJob() {
        JobDetail jd=new JobDetail("NAME", GBFileSystemMonitorJob.class);
        jd.getJobDataMap().put(FileSystemMonitorSPI.SOURCE_KEY, "src/test/resources/data");
        jd.getJobDataMap().put(FileSystemMonitorSPI.WILDCARD_KEY, ".*");
        
        final Scheduler sched;
        try {
            sched = StdSchedulerFactory.getDefaultScheduler();
            SimpleTrigger st=new SimpleTrigger("FSM_TRIGGER", 100, 4000);
            sched.scheduleJob(jd,st);
            // TODO intercept some events!
        } catch (SchedulerException e) {
            fail(e.getMessage());
        }
    }
}
