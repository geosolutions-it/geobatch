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
package it.geosolutions.geobatch.flow.event.listeners.cumulator;

import it.geosolutions.geobatch.catalog.Identifiable;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerService;

/**
 * @author ETj <etj at geo-solutions.it>
 */
public class CumulatingProgressListenerService extends BaseService implements
        ProgressListenerService<CumulatingProgressListener,CumulatingProgressListenerConfiguration>{
    
    public CumulatingProgressListenerService(String id, String name, String description) {
        super(id, name, description);
    }

    // implements Service<FileSystemEvent,
    // GeoTiffOverviewsEmbedderConfiguration> {

    // private CumulatingProgressListenerService() {
    // }

//    private final static Logger LOGGER = LoggerFactory.getLogger(CumulatingProgressListenerService.class
//            .toString());


    public CumulatingProgressListener createProgressListener(CumulatingProgressListenerConfiguration configuration,Identifiable owner) {
        return new CumulatingProgressListener(configuration,owner);
    }
}
