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
package it.geosolutions.tool.errorhandling;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides facility methods for handle the exceptions inside an Action
 * 
 * @author DamianoG
 * 
 */
public class ActionExceptionHandler {

    protected final static Logger LOGGER = LoggerFactory.getLogger(ActionExceptionHandler.class);

    protected final static String NULL_CONF_ERROR = "";

    protected final static String UNKNOWN_CLASS = "unknown class";

    protected final static String UNKNOWN_MSG = "An error occurred. No details are avaiable.";

    /**
     * This is a facility method to use inside an action when an abnormal state occurs.
     * This method check the failIgnored flag inside the ActionConfiguration object provided:
     * If failIgnored = true it only logs a message
     * If failIgnored = false logs a massage, setup the ProgressListenerForwarder and throws an ActionException
     * 
     * Notice that in the first case is the caller that must decide how to handle the situation, usually is done using a continue statement so the event is skipped.
     * 
     *  example:
     *  
     *  if(somewhatThatMustNotBeNull == null){
     *         checkError(this.getConfiguration(), this, "An error message"); // if failIgnored = true only logs "An error message" otherwise throw an ActionException 
     *         continue;  
     *  }
     * 
     * @param conf The Configuration of the action. Must be not null otherwise an IllegalArgumentException is thrown
     * @param owner The action that call this method
     * @param msg the message that will be logged and displayed on the GUI
     * @throws ActionException if
     */
    public static void handleError(ActionConfiguration conf, BaseAction owner, String msg)
            throws ActionException {
        if (conf == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(NULL_CONF_ERROR);
            }
            throw new IllegalArgumentException(NULL_CONF_ERROR);
        }
        final String ownerMsg = (owner == null) ? UNKNOWN_CLASS : owner.getClass().getName();
        if (msg == null || msg.isEmpty()){
            msg = UNKNOWN_MSG;
        }
        StringBuilder fullMsg = new StringBuilder();
        fullMsg.append("Error in Class: ").append(ownerMsg).append(" - Message is: ")
                .append(msg);
        if (!conf.isFailIgnored()) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(fullMsg.toString());
            }
            ActionException ex = new ActionException(owner, msg);
            if(owner != null){
                Iterator iter = owner.getListeners().iterator();
                while (iter.hasNext()) {
                    Object el = iter.next();
                    if (el instanceof ProgressListenerForwarder) {
                        ((ProgressListenerForwarder) el).failed(ex);
                    }
                }
            }
            throw ex;
        } else {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(fullMsg.toString());
            }
        }
    }
}
