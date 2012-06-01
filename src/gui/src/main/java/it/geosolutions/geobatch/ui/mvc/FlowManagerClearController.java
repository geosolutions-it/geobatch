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
/**
 *
 */
package it.geosolutions.geobatch.ui.mvc;

import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.flow.tools.FileBasedFlowManagerUtils;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class FlowManagerClearController extends AbstractController {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal
     * (javax.servlet .http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        Catalog catalog = (Catalog)getApplicationContext().getBean("catalog");

        final String fmId = request.getParameter("fmId");

        FileBasedFlowManagerUtils.clear(fmId);

        ModelAndView mav = new ModelAndView("flows");

        if (fmId != null) {
            final FileBasedFlowManager fm = catalog.getResource(fmId, FileBasedFlowManager.class);
            if (fm != null) {
                final Collection<EventConsumer> consumers = fm.getEventConsumers();
                synchronized (consumers) {
                    final Iterator<EventConsumer> it = consumers.iterator();
                    while (it.hasNext()) {
                        EventConsumer ec = it.next();
                        ConsumerDisposeController.disposeConsumer(ec.getStatus(), fm, ec);
                    }
                }
            }
        }

        mav.addObject("flowManagers", catalog.getFlowManagers(FileBasedFlowManager.class));

        return mav;

    }
}
