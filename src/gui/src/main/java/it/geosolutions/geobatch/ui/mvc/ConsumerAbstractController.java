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
package it.geosolutions.geobatch.ui.mvc;

import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.flow.FlowManager;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Base controller for action targeted to Consumer instances
 * 
 * @author ETj <etj at geo-solutions.it>
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public abstract class ConsumerAbstractController extends AbstractController {

    protected Catalog catalog;

    public ConsumerAbstractController() {
    }

    public static final String MAV_NAME_KEY = "flows";
    public static final String FLOW_MANAGER_ID_KEY = "fmId";
    public static final String CONSUMER_ID_KEY = "ecId";

    /*
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        catalog = (Catalog)getApplicationContext().getBean("catalog");
        
        final String fmId = request.getParameter(FLOW_MANAGER_ID_KEY);
        final String ecId = request.getParameter(CONSUMER_ID_KEY);

        if (fmId == null || fmId.isEmpty()) {
            throw new IllegalArgumentException("fmId parameter is null or empty");
        }
        if (ecId == null || ecId.isEmpty()) {
            throw new IllegalArgumentException("ecId parameter is null or empty");
        }

        final ModelAndView mav = new ModelAndView(MAV_NAME_KEY);
        mav.addObject("flowManagers", catalog.getFlowManagers(FileBasedFlowManager.class));

        final FileBasedFlowManager fm = catalog.getResource(fmId, FileBasedFlowManager.class);
        handleFlowManager(mav, fm);

        final EventConsumer consumer = fm.getConsumer(ecId);
        handleConsumer(mav, consumer);

        // final Collection<FileBasedEventConsumer>
        // consumers=fm.getEventConsumers();
        // final Iterator<FileBasedEventConsumer> it=consumers.iterator();
        // while (it.hasNext()){
        // final FileBasedEventConsumer consumer=it.next();
        // if (consumer == null)
        // {
        // mav.addObject("error", "Flow instance '" + consumer.getId() +
        // "' not found");
        // } else {
        // runStuff(mav, consumer);
        // }
        //
        // }

        return mav;
    }

    /**
     * 
     * Default handler for a flowManager which simply add it to the MAV using {@link ConsumerAbstractController#FLOW_MANAGER_ID_KEY}
     * 
     * @param mav
     * @param fm
     * @throws IllegalArgumentException if flow manager is null
     */
    protected void handleFlowManager(ModelAndView mav, FlowManager fm) throws IllegalArgumentException {
        if (fm == null) {
            throw new IllegalArgumentException("Unable to locate the FlowManager ID:" + fm.getId());
        }
        mav.addObject(FLOW_MANAGER_ID_KEY, fm);
    }
    
    /**
     * 
     * Default handler for a consumer which simply add it to the MAV using {@link ConsumerAbstractController#CONSUMER_ID_KEY}
     * 
     * @param mav
     * @param consumer
     * @throws IllegalArgumentException if consumer is null
     */
    protected void handleConsumer(ModelAndView mav, EventConsumer consumer) throws IllegalArgumentException {
        if (consumer == null) {
            throw new IllegalArgumentException("ERROR: Consumer instance '" + consumer.getId() + "' not found");
        }
        mav.addObject(CONSUMER_ID_KEY, consumer);
    }
}
