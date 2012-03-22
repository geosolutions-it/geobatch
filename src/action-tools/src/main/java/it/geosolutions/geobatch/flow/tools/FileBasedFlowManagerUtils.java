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
package it.geosolutions.geobatch.flow.tools;

import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.global.CatalogHolder;

import java.util.List;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public abstract class FileBasedFlowManagerUtils {

	/**
	 * clear all (COMPLETED, CANCELED, FAILED) instances of a flow
	 * registered into the catalog using the passed fmId.
	 *  
	 * @param fmId the flow id used to identify the flow manager instance into the catalog
	 */
	public static void clear(final String fmId){
		final FileBaseCatalog catalog = (FileBaseCatalog) CatalogHolder.getCatalog();
		if (fmId != null) {
			FileBasedFlowManager fm = catalog.getResource(fmId,FileBasedFlowManager.class);
			if (fm != null) {
			        fm.purgeConsumers(Integer.MAX_VALUE);
//				final List<FileBasedEventConsumer> consumers = fm.getEventConsumers();
//				synchronized (consumers) {
//					final int size = consumers.size();
//					for (int index=size-1; index >= 0; --index) {
//						final FileBasedEventConsumer consumer=consumers.get(index);
//						
//						if (fm != null && consumer != null) {
//							final EventConsumerStatus status = consumer.getStatus();
//							
//							if (status.equals(EventConsumerStatus.COMPLETED)
//									|| status.equals(EventConsumerStatus.CANCELED)
//									|| status.equals(EventConsumerStatus.FAILED)) {
//								
//								if (consumer instanceof FileBasedEventConsumer){
//									final FileBasedEventConsumer fileConsumer=(FileBasedEventConsumer)consumer;
//	
//									// start manually clear action instances and cumulators  
//									fileConsumer.clear();
//									
//									// dispose the object
//									fm.dispose(fileConsumer);
//								}
//								
//							}
//						}
//					}	
//				}
			}
		}

	}
}
