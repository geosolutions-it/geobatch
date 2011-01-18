package it.geosolutions.filesystemmonitor.monitorpolling;

import it.geosolutions.filesystemmonitor.neutral.monitorpolling.GBFileSystemMonitorJob;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class FileSystemMonitorTests {
    
    

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        try{
//        File dir = new File("/home/carlo/");
//        FileFilter wildcardf = new WildcardFileFilter(".f*");
//        File[] files = dir.listFiles(wildcardf);
//        for (int i = 0; i < files.length; i++) {
//          System.out.println(files[i]);
//        }
//        
//        
//        
//        
//        IOFileFilter wildcardFilter = new WildcardFileFilter(".f*");     
//        
//        IOFileFilter filter=FileFilterUtils.or(wildcardFilter,FileFilterUtils.notFileFilter(FileFilterUtils.prefixFileFilter(".")));
            
            //CAMEL
//            ApplicationContext c =
//                new ClassPathXmlApplicationContext("applicationContext.xml");
//            String s="";
//            for (String b: c.getBeanDefinitionNames())
//                s+=" "+b;
//            System.out.println(s);
//            //Starting the camel context
//            CamelContext camel = (CamelContext) c.getBean("camel");
//            
//            camel.start();
//            
//            // the bean that you want to inject
//            ConnectionFactory connectionFactory = new    ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
//            
//            camel.addComponent("test-jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

            
        Scheduler sched=StdSchedulerFactory.getDefaultScheduler();
        
        JobDetail jobDetail=new JobDetail("FSM", GBFileSystemMonitorJob.class);
//        jobDetail.getJobDataMap().put(GBFileSystemMonitorJob.ROOT_PATH_KEY, "/home/carlo/");
//        jobDetail.getJobDataMap().put(GBFileSystemMonitorJob.WILDCARD_KEY, ".*");
        
        SimpleTrigger st=new SimpleTrigger("FSM_TRIGGER", 100, 4000);
        sched.scheduleJob(jobDetail,st);
        
        JobDetail jobDetail2=new JobDetail("FSM2", GBFileSystemMonitorJob.class);
//        jobDetail2.getJobDataMap().put(GBFileSystemMonitorJob.ROOT_PATH_KEY, "/tmp/");
//protected        jobDetail2.getJobDataMap().put(GBFileSystemMonitorJob.WILDCARD_KEY, "*");
        
        SimpleTrigger st2=new SimpleTrigger("FSM_TRIGGER2", 100, 4000);
//        st.setJobName("FSM");
        System.out.println(
                sched.scheduleJob(jobDetail2,st2).toLocaleString());
        
        
        
        
//        File directory = new File(new File("/home/"), "carlo");        
//        FileAlterationObserver observer = new FileAlterationObserver(directory,new WildcardFileFilter("*"));//,FileFilterUtils.directoryFileFilter());
//        FileAlterationListener fal=new GBFileAlterationListener();
        
//        observer.addListener(fal);
//        observer.initialize();
//        FileAlterationMonitor fam=new FileAlterationMonitor(1000, observer);
//        System.out.println("Observing:");
//        System.out.println(observer.toString());

        System.out.println("Starting");
        sched.start();
        System.out.println("Started");
        
        Thread.sleep(80000);
        
        sched.pauseAll();
        System.out.println("pauseAll");
        
        Thread.sleep(1000);
        sched.resumeAll();
        System.out.println("resumeAll");
        
        Thread.sleep(20000);
        
        sched.standby();
        System.out.println("standby");
        Thread.sleep(1000);
        
        sched.start();
        System.out.println("restart");
        
        Thread.sleep(20000);
        
        sched.shutdown();
        System.out.println("shutdown");
    }
    catch (Throwable t){
        t.printStackTrace();
    }
        
//TODO into a trigger        observer.destroy();
    }

}
