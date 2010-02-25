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

package it.geosolutions.geobatch.ftp.client.configuration;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.IOException;
import java.util.EventObject;
import java.util.logging.Logger;


/**
 * This class represent a basic FTP action.
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 */
public abstract class FTPBaseAction <T extends EventObject> extends BaseAction<T> implements Action<T> {
	
    /**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(FTPBaseAction.class.toString());

    protected final FTPActionConfiguration configuration;

    /**
     * Constructs a producer. The operation name will be the same than the parameter descriptor
     * name.
     * 
     * @param descriptor The parameters descriptor.
     * @throws IOException
     */
    public FTPBaseAction(FTPActionConfiguration configuration) throws IOException {
    	
        try {
			this.configuration = configuration.clone();
		} catch (CloneNotSupportedException e) {
			final IOException ioe = new IOException();
			ioe.initCause(e);
			throw ioe;
		} 
    }

    /**
     * @return The configuration of the action.
     */
    public ActionConfiguration getConfiguration() {
        return configuration;
    }

}
