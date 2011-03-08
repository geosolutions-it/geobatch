package it.geosolutions.geobatch.camel.test;

import it.geosolutions.geobatch.camel.beans.JMSFlowRequest;
import it.geosolutions.geobatch.camel.beans.JMSFlowResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Assert;

public class JMSClient {
    private static Lock lock=new ReentrantLock();
    private static JMSClient singleton=null;
    
    private static int ackMode;
    private static String queueName;
    private static String broker;
    private static boolean transacted;
    private static MessageProducer producer;
    private static ActiveMQConnectionFactory connectionFactory;
    
    private static JMSMessageListener listener;

    // status
    private static boolean isOpen=false;
    private static boolean initted=false;

    //open
    private static Connection connection = null;
    private static Session session = null;

    private static Destination tempDest = null;

    private static MessageConsumer responseConsumer = null;

    static {
        queueName = "fileSevice";
        ackMode = Session.AUTO_ACKNOWLEDGE;
        broker="tcp://localhost:61611";
        transacted = false;
    }
    
    protected static Session getSession() {
        if (initted && isOpen)
            return session;
        else
            return null;
    }

    protected static MessageProducer getProducer() {
        return producer;
    }

    protected static Destination getTempDest() {
        return tempDest;
    }

    protected static synchronized String getBroker() {
        return broker;
    }

    protected static synchronized void setBroker(String broker) {
        JMSClient.broker = broker;
    }

    protected static synchronized int getAckMode() {
        return ackMode;
    }

    protected static synchronized void setAckMode(int ackMode) {
        JMSClient.ackMode = ackMode;
    }

    protected static synchronized String getQueueName() {
        return queueName;
    }

    protected static void setqueueName(String clientQueueName) {
        JMSClient.queueName = clientQueueName;
    }
    
    public static void main(String args[]){
        if (args.length>=5){
            call((String)args[0], args[1], args[2], args[3], args[4]);
        }
        else
            System.out.println("Unable to run the client:\n" +
            		"1- broker the broker address\n" +
            		"2- inQueue the GeoBatch queue name (if null it is set to: fileService)\n" +
            		"3- resQueue the temp jms queue\n" +
            		"4- flowId the GeoBatch flow id\n" +
            		"5- args the list of file to pass to the flow\n");
    }
    
    /**
     * 
     * @param broker the broker address
     * @param inQueue the GeoBatch queue name (if null it is set to: fileService)
     * @param resQueue the temp jms queue
     * @param flowId the GeoBatch flow id 
     * @param args the list of file to pass to the flow
     */
    public static void call(String broker, String inQueue ,String resQueue ,String flowId ,String... args){
        //init 
        JMSClient.getJMSClient(broker,inQueue);
        
        Session s=JMSClient.getSession();
        if (s!=null){
            ObjectMessage objMessage;
            try {
                objMessage = s.createObjectMessage();
                objMessage.setObject(JMSClient.buildRequest(flowId,args));
                objMessage.setJMSReplyTo(JMSClient.getTempDest());
                String correlationObjId = (resQueue!=null)?resQueue:createRandomString();
                objMessage.setJMSCorrelationID(correlationObjId);
                JMSClient.getProducer().send(objMessage);
            } catch (JMSException e) {
                System.out.println(e.getMessage());
            }
        }
        else
            System.out.println("Unable to get the session");
    }
    
    public static JMSFlowRequest buildRequest(String flowId,String... args) throws JMSException {
        JMSFlowRequest fr = new JMSFlowRequest();
        fr.setFlowId(flowId);
        List<String> files=new ArrayList<String>();
        if (args.length>0){
            for (int i=0; i<args.length; i++){
                File file=new File(args[i]);
                if (file.exists()){
                    files.add(file.getAbsolutePath());
                }
                else {
                    System.out.println("The file: "+file+" do not exists! skipping...");   
                }
            }
        }
        
        fr.setFiles(files);
        return fr;
    }
    
    protected static JMSClient getJMSClient(String broker, String inQueue){
        if (singleton==null){
            if (lock.tryLock()){
                try {
                
                    if (singleton==null){
                        initClient(broker, inQueue);
                    }
                }
                finally{
                    lock.unlock();
                }
            }
        }
        return singleton;
    }
    
    // called once
    private synchronized static void initClient(String broker, String inQueue){
        if (isOpen)
            close();
        if (broker!=null){
            JMSClient.broker=broker;
        }
        if (inQueue!=null){
            JMSClient.queueName=inQueue;
        }
        
        singleton=new JMSClient();
        connectionFactory = new ActiveMQConnectionFactory(broker);
        listener=new JMSMessageListener();
        open();
        initted=true;
    }
    
    protected synchronized static void open(){
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(transacted, ackMode);
            Destination adminQueue = session.createQueue(queueName);

            //Setup a message producer to send message to the queue the server is consuming from
            producer = session.createProducer(adminQueue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            //Create a temporary queue that this client will listen for responses on then create a consumer
            //that consumes message from this temporary queue...for a real application a client should reuse
            //the same temp queue for each message to the server...one temp queue per client
            tempDest = session.createTemporaryQueue();
            responseConsumer = session.createConsumer(tempDest);

            //This class will handle the messages to the temp queue as well
            responseConsumer.setMessageListener(listener);
            
            isOpen=true;
        } catch (JMSException e) {
            //Handle the exception appropriately
            if (connection!=null)
                try {
                    connection.stop();
                } catch (JMSException inn_e) {
                    inn_e.printStackTrace();
                }
            e.printStackTrace();
        }
    }
    
    protected synchronized static void close(){
        try {
            if (initted && isOpen){
                session.close();
                connection.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    
    private static class JMSMessageListener implements MessageListener {
        public JMSMessageListener(){
            
        }
        
        public void onMessage(Message message) {
            JMSFlowResponse response = null;
            try {
                if (message instanceof ObjectMessage) {
                    ObjectMessage objMessage = (ObjectMessage) message;
                    response = (JMSFlowResponse) objMessage.getObject();
                    System.out.println("Result = " + response.getStatus());
                    int i=0;
                    for (String msg : response.getResponses())
                        System.out.println("Message_"+(++i)+": "+msg);
                }
                else {
                    System.out.println("Wrong responses type recived");
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }
    
    private JMSClient() {}

}
