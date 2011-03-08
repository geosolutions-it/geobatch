package it.geosolutions.geobatch.camel.test;

import java.util.Random;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JMSClientTest {
    
    private static String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }
    
    /**
     * TODO use an active broker
     */
    @Test(timeout=10000)
    public void goodTest(){
        /*
        //init 
        JMSClient.getJMSClient();
        
        Session s=JMSClient.getSession();
        if (s!=null){
            ObjectMessage objMessage;
            try {
                objMessage = s.createObjectMessage();
                objMessage.setObject(JMSClient.buildRequest("geotiff","src/test/resources/data/sample.tif"));
                objMessage.setJMSReplyTo(JMSClient.getTempDest());
                String correlationObjId = createRandomString();
                objMessage.setJMSCorrelationID(correlationObjId);
                JMSClient.getProducer().send(objMessage);
            } catch (JMSException e) {
                Assert.fail(e.getMessage());
            }
        }
        else
            System.out.println("Unable to get the session");
            */
    }
    
    @Test(timeout=10000)
    public void badTest(){
        /*
        //init 
        JMSClient.getJMSClient();
        
        //Now create the actual message you want to send
        TextMessage txtMessage;
        try {
            Session s=JMSClient.getSession();
            if (s!=null){
                txtMessage = s.createTextMessage();
                txtMessage.setText("MyProtocolMessage");
    
                //Set the reply to field to the temp queue you created above, this is the queue the server
                //will respond to
                txtMessage.setJMSReplyTo(JMSClient.getTempDest());
    
                //Set a correlation ID so when you get a response you know which sent message the response is for
                //If there is never more than one outstanding message to the server then the
                //same correlation ID can be used for all the messages...if there is more than one outstanding
                //message to the server you would presumably want to associate the correlation ID with this
                //message somehow...a Map works good
                String correlationId = createRandomString();
                txtMessage.setJMSCorrelationID(correlationId);
                JMSClient.getProducer().send(txtMessage);
            }
            else
                Assert.fail("Unable to initialize the client session");
        } catch (JMSException e) {
            Assert.fail(e.getMessage());
        }
        */
    }
    

    @Before
    public void setUp() throws Exception {
        //init
    }

    @After
    public void tearDown() throws Exception {
    }
}
