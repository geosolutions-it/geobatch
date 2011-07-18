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
package it.geosolutions.geobatch.ui.mvc;

import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Base controller for action targeted to Consumer instances
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public abstract class ConsumerAbstractController extends AbstractController {
    /*
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Catalog catalog = (Catalog) getApplicationContext().getBean("catalog");
        String fmId = request.getParameter("fmId");
        String ecId = request.getParameter("ecId");

        BaseEventConsumer consumer = null;
        FileBasedFlowManager fm = null;

        if (fmId != null) {
            fm = catalog.getResource(fmId, FileBasedFlowManager.class);

            if (fm != null) {
                List<? extends EventConsumer> ecList = fm.getEventConsumers();
                for (EventConsumer eventConsumer : ecList) {
                    if (((BaseEventConsumer) eventConsumer).getId().equals(ecId)) {
                        consumer = (BaseEventConsumer) eventConsumer;
                        break;
                    }
                }
            }
        }

        ModelAndView mav = new ModelAndView("flows");
        mav.addObject("flowManagers", catalog.getFlowManagers(FileBasedFlowManager.class));

        if (consumer != null) {
            runStuff(mav, fm, consumer);
        } else {
            mav.addObject("error", "Flow instance '" + ecId + "' not found");
        }

        return mav;
    }

    protected abstract void runStuff(ModelAndView mav, FileBasedFlowManager fm,
            BaseEventConsumer consumer);
}
