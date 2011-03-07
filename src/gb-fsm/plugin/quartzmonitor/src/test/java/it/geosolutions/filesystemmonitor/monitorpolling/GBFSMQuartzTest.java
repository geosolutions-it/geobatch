package it.geosolutions.filesystemmonitor.monitorpolling;
import static org.junit.Assert.*;
import it.geosolutions.filesystemmonitor.neutral.monitorpolling.GBFileSystemMonitorJob;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.impl.QuartzServer;


public class GBFSMQuartzTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testGBFileSystemMonitorJob() {
        JobDetail jd=new JobDetail("TEST", GBFileSystemMonitorJob.class);
       //TODO
        JobExecutionContext context=null;
        GBFileSystemMonitorJob job=new GBFileSystemMonitorJob();
        //job.execute(context);
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testExecute() {
        fail("Not yet implemented"); // TODO
    }

}
