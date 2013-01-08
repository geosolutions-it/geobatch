/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  https://github.com/nfms4redd/nfms-geobatch
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.mailtest;

import static org.junit.Assert.fail;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.mail.SendMailAction;
import it.geosolutions.geobatch.mail.SendMailConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.geotools.TestData;
import org.junit.Test;

/**
 * @author DamianoG
 *
 */
public class MailTest extends MailOnlineBaseTest{
    
    private final static Logger LOGGER = Logger.getLogger(MailTest.class);
    
    @Test
    public void testSendMail() throws IOException{
        
        SendMailConfiguration conf = new SendMailConfiguration("test","test","test");


        conf.setServiceID("SendMailGeneratorServiceTest");
        conf.setId("SendMailGeneratorServiceTest");
        conf.setDescription("SendMail action Test");
        conf.setName("SendMailConfigurationTest");
        conf.setMailSubject("TEST mail Action");
        
        Map<String,String> m = getParams();
        
        conf.setMailSmtpHost(m.get("mailSmtpHost"));
        conf.setMailSmtpStarttlsEnable(m.get("mailSmtpStarttlsEnable"));
        conf.setMailSmtpAuth(m.get("mailSmtpAuth"));              
        conf.setMailSmtpPort(m.get("mailSmtpPort"));
        conf.setMailAuthUsername(m.get("mailAuthUsername")); 
        conf.setMailAuthPassword(m.get("mailAuthPassword"));               
        conf.setMailToAddress(m.get("mailToAddress"));
        conf.setMailFromAddress(m.get("mailFromAddress"));
        
        conf.setMailContentHeader("this is a default content header.");
        File mailBody = TestData.file(this, "mailContent.txt");
            
        try {
            Queue<EventObject> inputQ = new LinkedList<EventObject>();
            inputQ.add(new FileSystemEvent(mailBody, FileSystemEventType.FILE_ADDED));
            SendMailAction sma = new SendMailAction(conf);
            Queue<EventObject> outputQ = sma.execute(inputQ);
        } catch (ActionException e) {
           LOGGER.error(e.getLocalizedMessage());
           fail(e.getLocalizedMessage());
        }
    }
    
    @Test
    public void testSendMailNegative() throws IOException{
        
        SendMailConfiguration conf = new SendMailConfiguration("test","test","test");

        conf.setFailIgnored(false);
        
        conf.setServiceID("SendMailGeneratorServiceTest");
        conf.setId("SendMailGeneratorServiceTest");
        conf.setDescription("SendMail action Test");
        conf.setName("SendMailConfigurationTest");
        conf.setMailSubject("TEST mail Action");
        
        Map<String,String> m = getParams();
        
        conf.setMailSmtpHost(m.get("mailSmtpHost"));
        conf.setMailSmtpStarttlsEnable(m.get("mailSmtpStarttlsEnable"));
        conf.setMailSmtpAuth(m.get("mailSmtpAuth"));              
        conf.setMailSmtpPort(m.get("mailSmtpPort"));
        conf.setMailAuthUsername(m.get("mailAuthUsername")); 
        conf.setMailAuthPassword(m.get("mailAuthPassword"));               
        conf.setMailToAddress("aWrongEmailAddress");
        conf.setMailFromAddress(m.get("mailFromAddress"));
        
        conf.setMailContentHeader("this is a default content header.");
        File mailBody = TestData.file(this, "mailContent.txt");
            
        try {
            Queue<EventObject> inputQ = new LinkedList<EventObject>();
            inputQ.add(new FileSystemEvent(mailBody, FileSystemEventType.FILE_ADDED));
            SendMailAction sma = new SendMailAction(conf);
            Queue<EventObject> outputQ = sma.execute(inputQ);
            fail("this test must return an exception because the mail used don't exists..."); 
        } catch (ActionException e) {
           
        }
    }

}
