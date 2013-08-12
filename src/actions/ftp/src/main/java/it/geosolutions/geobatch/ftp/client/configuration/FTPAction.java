/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
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
package it.geosolutions.geobatch.ftp.client.configuration;

import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.ftp.client.delete.FTPDeleteAction;
import it.geosolutions.geobatch.ftp.client.download.FTPDownloadAction;
import it.geosolutions.geobatch.ftp.client.upload.FTPUploadAction;

import java.io.IOException;
import java.util.EventObject;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Action(configurationClass=FTPActionConfiguration.class)
public class FTPAction extends BaseAction<EventObject> {

	private final static Logger LOGGER = LoggerFactory.getLogger(FTPAction.class);
	private FTPBaseAction action = null;

	public FTPAction(FTPActionConfiguration configuration) throws IOException {
		super(configuration);
		switch(configuration.getOperationId()){
			case Delete:
				action = new FTPDeleteAction(configuration);
				break;
			case Download:
				action = new FTPDownloadAction(configuration);
				break;
			case Upload:
				action = new FTPUploadAction(configuration);
				break;
		}
	}

    @Override
	@CheckConfiguration
	public boolean checkConfiguration(){
		if(action != null){
			return action.checkConfiguration();
		}
		return false;
	}

	@Override
	public Queue<EventObject> execute(Queue<EventObject> events)
			throws ActionException {
		if(action != null){
			return action.execute(events);
		}
		return null;
	}
}
