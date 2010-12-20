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
package it.geosolutions.geobatch.nurc.sem.rep10.shom;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.octave.tools.file.processor.FilterConfiguration;

import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class SHOMGeneratorService extends BaseService implements
//        ActionService<EventObject, SHOMConfiguration> {
    ActionService<EventObject, FilterConfiguration> {

    private final static Logger LOGGER = Logger.getLogger(SHOMGeneratorService.class
            .toString());

//    public SHOMAction createAction(SHOMConfiguration configuration) {
    public SHOMAction createAction(FilterConfiguration configuration) {
        try {
            return new SHOMAction(configuration);
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            return null;
        }
    }

//    public boolean canCreateAction(SHOMConfiguration configuration) {
    public boolean canCreateAction(FilterConfiguration configuration) {
        return true;
    }

}