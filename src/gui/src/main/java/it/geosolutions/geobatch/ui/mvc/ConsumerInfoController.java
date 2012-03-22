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
/**
 *
 */
package it.geosolutions.geobatch.ui.mvc;

import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.ProgressListener;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.geobatch.flow.event.listeners.cumulator.CumulatingProgressListener;
import it.geosolutions.geobatch.flow.event.listeners.status.StatusProgressListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.web.servlet.ModelAndView;


/**
 *
 * @author ETj <etj at geo-solutions.it>
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class ConsumerInfoController extends ConsumerAbstractController
{

    @Override
    protected void handleConsumer(ModelAndView mav, EventConsumer consumer)
    {
        mav.setViewName("consumerInfo");

        List<String> eventlist = new ArrayList<String>();

        // Progress Logging...
        Collection<IProgressListener> coll= consumer.getListeners();
        for (IProgressListener listener: coll){
            if (listener == null){
                continue;
                // TODO warn
            }
            if (listener instanceof CumulatingProgressListener){
                CumulatingProgressListener cpl = (CumulatingProgressListener) listener;
                for (String msg : cpl.getMessages())
                {
                    eventlist.add("Consumer: " + msg);
                }
            } else if (listener instanceof StatusProgressListener){
                StatusProgressListener spl = (StatusProgressListener) listener;
                eventlist.add("Consumer status: " + spl.toString());
            } else {
                // get any pl
                ProgressListener anypl = (ProgressListener) listener;
                eventlist.add("Consumer action task: " + anypl.getTask());
                eventlist.add("Consumer action progress: " + anypl.getProgress() + "%");
            }
        }

        // Current Action Status...
        BaseAction<?> action = (BaseAction) ((FileBasedEventConsumer)consumer).getCurrentAction(); // TODO BETTER USE OF CONSUMER!!!
        if (action != null)
        {
            eventlist.add("Current action name:   " + action.getName() + " [" +
                action.getClass().getSimpleName() + "]");
            eventlist.add("Current action status: " +
                (action.isPaused() ? "SUSPENDED" : "ACTIVE"));

            // try the most interesting information holder
            Collection<IProgressListener> collAction= action.getListeners();
            for (IProgressListener listener: collAction){
                if (listener == null){
                    continue;
                    // TODO warn
                }
                if (listener instanceof CumulatingProgressListener){
                    CumulatingProgressListener cpl = (CumulatingProgressListener) listener;
                    for (String msg : cpl.getMessages())
                    {
                        eventlist.add("Current action event: " + msg);
                    }
                } else if (listener instanceof StatusProgressListener){
                    StatusProgressListener spl = (StatusProgressListener) listener;
                    eventlist.add("Current action status: " + spl.toString());
//                    mav.addObject("acStatus", spl);
                } else {
                    // get any pl
                    ProgressListener anypl = (ProgressListener) listener;
                    eventlist.add("Current action task: " + anypl.getTask());
                    eventlist.add("Current action progress: " + anypl.getProgress() + "%");
                }
            }

        }

        eventlist.add(consumer.toString());

        mav.addObject("consumer", consumer);
        mav.addObject("eventlist", eventlist);
    }
}
