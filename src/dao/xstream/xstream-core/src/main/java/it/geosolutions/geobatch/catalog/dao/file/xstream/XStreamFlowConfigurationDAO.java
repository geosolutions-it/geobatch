/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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

package it.geosolutions.geobatch.catalog.dao.file.xstream;

import it.geosolutions.geobatch.catalog.dao.FlowManagerConfigurationDAO;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;
import it.geosolutions.geobatch.configuration.flow.FlowConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.tools.file.IOUtils;
import it.geosolutions.geobatch.xstream.Alias;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

public class XStreamFlowConfigurationDAO extends XStreamDAO<FlowConfiguration> implements
        FlowManagerConfigurationDAO {

    private final static Logger LOGGER = LoggerFactory.getLogger(XStreamFlowConfigurationDAO.class
            .toString());

    public XStreamFlowConfigurationDAO(String directory, Alias alias) {
        super(directory, alias);
    }

    public FileBasedFlowConfiguration find(FlowConfiguration exampleInstance, boolean lock)
            throws IOException {
        return find(exampleInstance.getId(), lock);
    }

    public FileBasedFlowConfiguration find(String id, boolean lock) throws IOException {
        final File entityfile = new File(getBaseDirectory(), id + ".xml");
        if (entityfile.canRead() && !entityfile.isDirectory()) {
            XStream xstream = new XStream();
            alias.setAliases(xstream);

            InputStream inStream = null;
            try {
                inStream = new FileInputStream(entityfile);
                FileBasedFlowConfiguration obj = (FileBasedFlowConfiguration) xstream
                        .fromXML(new BufferedInputStream(inStream));

                if (obj.getEventConsumerConfiguration() == null)
                    LOGGER.error("FileBasedFlowConfiguration " + obj
                            + " does not have a ConsumerCfg");

                if (obj.getEventGeneratorConfiguration() == null)
                    LOGGER.error("FileBasedFlowConfiguration " + obj
                            + " does not have a GeneratorCfg");

                resolveReferences(obj);

                return obj;
            } catch (Throwable e) {
                final IOException ioe = new IOException("Unable to load flow config:" + id);
                ioe.initCause(e);
                throw ioe;
            } finally {
                if (inStream != null)
                    IOUtils.closeQuietly(inStream);
            }
        }
        return null;
    }

    /**
     * Resolve references in the flow configuration.
     * 
     * <P>
     * At the moment only listeners are cross referenced. <BR>
     * XStream may handle references on its own (using xpath), but this way we can refactor the file
     * format with less problems.
     * 
     * @param obj
     */
    private void resolveReferences(FileBasedFlowConfiguration obj) {
        // === resolve listener references

        // caches listeners locally
        Map<String, ProgressListenerConfiguration> listenersMap = new HashMap<String, ProgressListenerConfiguration>();
        if (obj.getProgressListenerConfigurations() != null) {
            for (ProgressListenerConfiguration plc : obj.getProgressListenerConfigurations()) {
                String plcId = plc.getId();
                if (plcId == null) {
                    LOGGER.error("FileBasedFlowConfiguration " + obj
                            + " declares a Listener with no id: " + plc);
                    continue; // skip the listener definition
                }

                listenersMap.put(plcId, plc);
            }
        }

        // resolve consumer listener
        EventConsumerConfiguration ecc = obj.getEventConsumerConfiguration();
        if (ecc.getListenerIds() != null) {
            for (String listenerId : ecc.getListenerIds()) {
                if (listenerId != null) {
                    if (listenersMap.containsKey(listenerId)) {
                        ecc.addListenerConfiguration(listenersMap.get(listenerId));
                    } else {
                        LOGGER.error("FileBasedFlowConfiguration " + obj
                                + " declares an invalid listener in the ConsumerConfiguration '"
                                + listenerId + "'");
                    }
                }
            }
        }

        // resolve actions listener
        if (ecc.getActions()!=null){
	        for (ActionConfiguration ac : ecc.getActions()) {
	            if (ac.getListenerConfigurations() == null) { // this happens in
	                // XStream...
	                ac.setListenerConfigurations(new ArrayList<ProgressListenerConfiguration>());
	            }
	
	            if (ac.getListenerIds() != null) {
	                for (String actionListenerId : ac.getListenerIds()) {
	                    if (actionListenerId != null) {
	                        if (listenersMap.containsKey(actionListenerId)) {
	                            ac.addListenerConfiguration(listenersMap.get(actionListenerId));
	                        } else {
	                        	if (LOGGER.isErrorEnabled())
	                        		LOGGER.error("FlowConfiguration " + obj
	                                    + " declares an invalid listener in an action configuration '"
	                                    + actionListenerId + "'");
	                        }
	                    }
	                }
	            }
	        }
        }
        else {
        	final String message="FlowConfiguration do not declare any Action!";
        	if (LOGGER.isErrorEnabled())
        		LOGGER.error(message);
        }
    }
}
