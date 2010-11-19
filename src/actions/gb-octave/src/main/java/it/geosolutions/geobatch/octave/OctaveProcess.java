/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
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

package it.geosolutions.geobatch.octave;

import java.util.Queue;
import java.util.logging.Logger;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
/**
 * @TODO this class should implement a MessageDrivenPOJO's interface
 * to add messages ('OctaveSheet' to the octaveEnv environment which is
 * handled by the consumer OctaveThread.
 * @note A future work can consists in implementing a proxy or gateway pattern
 * to define number of running octave processes.  
 * 
 * ...
 * @TODO complete description
 * 
 * @author Carlo Cancellieri, ccancellieri AT geo-solutions.it, GeoSolutions
 */
public class OctaveProcess{

    private final static Logger LOGGER = Logger.getLogger(OctaveProcess.class.toString());
    
    private final OctaveConfiguration conf;
    
    private final OctaveThread octave;
    
    /**
     * Constructor
     * @param actionConfiguration configuration for this action.
     */
    public OctaveProcess(OctaveConfiguration configuration) {
        conf=configuration;
        octave=new OctaveThread(configuration.getEnv());
        new Thread(octave).start();
    }
    
    public final OctaveEnv getEnv(){
        return conf.getEnv();
    }
    
    
    /*****************************
     * 
     */
    private JmsTemplate jmsTemplate;
    private Destination dest;

    public void setConnectionFactory(ConnectionFactory cf) {
        this.jmsTemplate = new JmsTemplate(cf);
    }

    public void setQueue(Destination destination) {
        this.dest= destination;
    }

    public void simpleSend() {
        this.jmsTemplate.send(this.dest, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
              return session.createTextMessage("hello queue world");
            }
        });
    }
    
    /**
     * @note: commented out since this is no more needed
     * 
     * anyway still persists: 
// TODO: check... should we add this member to the BaseAction?
     * get configuration
     * @return configuration of this action
    protected final OctaveConfiguration getConfig(){
        return config;
    }
    */
}
