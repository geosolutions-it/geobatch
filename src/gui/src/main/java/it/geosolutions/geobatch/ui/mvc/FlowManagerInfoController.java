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
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;

import java.util.Calendar;
import java.util.Comparator;
import java.util.EventObject;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * @author Alessio
 *
 */
public class FlowManagerInfoController extends AbstractController
{

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal
     * (javax.servlet .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
        HttpServletResponse response) throws Exception
    {
        Catalog catalog = (Catalog) getApplicationContext().getBean("catalog");

        String fmId = request.getParameter("fmId");

        ModelAndView mav = new ModelAndView("flowinfo");
        FileBasedFlowManager fm = catalog.getResource(fmId, FileBasedFlowManager.class);

        mav.addObject("flowManager", fm);
        
        Iterator<String> ecIt=fm.getEventConsumersId().iterator();
        
        TreeSet<BaseEventConsumer<EventObject, EventConsumerConfiguration>> tree=
            new TreeSet<BaseEventConsumer<EventObject, EventConsumerConfiguration>>(
                new Comparator<BaseEventConsumer<EventObject, EventConsumerConfiguration>>() {
            @Override
            public int compare(BaseEventConsumer<EventObject, EventConsumerConfiguration> o1,
                               BaseEventConsumer<EventObject, EventConsumerConfiguration> o2) {
                    Calendar cal = o1.getCreationTimestamp();
                    Calendar currentcal = o2.getCreationTimestamp();
                    if(cal.before(currentcal))
                            return 1;
                    else if(cal.after(currentcal))
                        return -1;
                    else
                            return 0;
            }

        });
        
        while (ecIt.hasNext()){
                tree.add((BaseEventConsumer)fm.getConsumer(ecIt.next()));
        }
        
        NavigableSet ecList=tree.descendingSet();
        mav.addObject("ecList", ecList.toArray(new BaseEventConsumer[]{}));
        

        return mav;
    }
}
