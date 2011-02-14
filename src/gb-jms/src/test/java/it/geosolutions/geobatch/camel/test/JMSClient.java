package it.geosolutions.geobatch.camel.test;


import it.geosolutions.geobatch.camel.beans.JMSFlowRequest;
import it.geosolutions.geobatch.camel.beans.JMSFlowResponse;

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
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

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
    public static Session getSession() {
        if (initted && isOpen)
            return session;
        else
            return null;
    }

    public static synchronized String getBroker() {
        return broker;
    }

    public static synchronized void setBroker(String broker) {
        JMSClient.broker = broker;
    }

    public static synchronized int getAckMode() {
        return ackMode;
    }

    public static synchronized void setAckMode(int ackMode) {
        JMSClient.ackMode = ackMode;
    }

    public static synchronized String getQueueName() {
        return queueName;
    }

    public static void setqueueName(String clientQueueName) {
        JMSClient.queueName = clientQueueName;
    }

    private JMSClient() {}
    
    public static JMSClient getJMSClient(){
        if (singleton==null){
            if (lock.tryLock()){
                try {
                
                    if (singleton==null){
                        initClient();
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
    private synchronized static void initClient(){
        if (isOpen)
            close();
        singleton=new JMSClient();
        connectionFactory = new ActiveMQConnectionFactory(broker);
        listener=new JMSMessageListener();
        open();
        initted=true;
    }
    
    public synchronized static void open(){
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
    
    public synchronized static void close(){
        try {
            if (initted && isOpen){
                session.close();
                connection.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private static String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }
    
        
    public static void main(String[] args) throws JMSException {
        
        new JMSClient();
        
        //init
        JMSClient.getJMSClient();
        
        JMSFlowRequest fr = new JMSFlowRequest();
        fr.setFlowId("ELMFlow");
        List<String> files=new ArrayList<String>();
        files.add("/home/carlo/work/data/rep10workingdir/meteoam/elm/REELM_2010092000.tar");
        fr.setFiles(files);
        Session s=getSession();
        if (s!=null){
            ObjectMessage objMessage=getSession().createObjectMessage();
            objMessage.setObject(fr);
            objMessage.setJMSReplyTo(tempDest);
            String correlationObjId = createRandomString();
            objMessage.setJMSCorrelationID(correlationObjId);
            producer.send(objMessage);
            
            //Now create the actual message you want to send
            TextMessage txtMessage = session.createTextMessage();
            txtMessage.setText("MyProtocolMessage");
    
            //Set the reply to field to the temp queue you created above, this is the queue the server
            //will respond to
            txtMessage.setJMSReplyTo(tempDest);
    
            //Set a correlation ID so when you get a response you know which sent message the response is for
            //If there is never more than one outstanding message to the server then the
            //same correlation ID can be used for all the messages...if there is more than one outstanding
            //message to the server you would presumably want to associate the correlation ID with this
            //message somehow...a Map works good
            String correlationId = createRandomString();
            txtMessage.setJMSCorrelationID(correlationId);
            producer.send(txtMessage);
        }
        else
            System.out.println("Unable to get the session");

        //System.exit(0);
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
                        System.out.println("Message_"+i+": "+msg);
                }
                else
                    System.out.println("Wrong responses type recived");
            } catch (JMSException e) {
                //Handle the exception appropriately
    //            System.out.println("Exception: "+e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

}
