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

import it.geosolutions.geobatch.flow.event.ProgressListener;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.geobatch.flow.event.listeners.cumulator.CumulatingProgressListener;
import it.geosolutions.geobatch.flow.event.listeners.status.StatusProgressListener;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class ConsumerInfoController extends ConsumerAbstractController {

	@Override
	protected void runStuff(ModelAndView mav, String fmId, BaseEventConsumer consumer) {
        mav.setViewName("consumerInfo");
		mav.addObject("consumer", consumer);

		List<String> eventlist = new ArrayList<String>();
		
		// Current Action Status
		BaseAction action = (BaseAction)consumer.getCurrentAction();
		if(action != null) {
			eventlist.add("Current action name:   " + action.getName() + " ["+action.getClass().getSimpleName()+"]");
			eventlist.add("Current action status: " + (action.isPaused() ? "PAUSED" : "RUNNING"));

			// try the most interesting information holder
			CumulatingProgressListener cpl = (CumulatingProgressListener)action.getProgressListener(CumulatingProgressListener.class);
			if(cpl != null) {
				eventlist.add("Current action eventlist: " + cpl.getMessages());
			} else {
				StatusProgressListener spl = (StatusProgressListener)action.getProgressListener(StatusProgressListener.class);
				if(spl != null) {
					eventlist.add("Current action status: " + spl.toString());
				} else {
					// get any pl
					ProgressListener anypl = (ProgressListener)action.getProgressListener(ProgressListener.class);
					if(anypl != null) {
						eventlist.add("Current action task: " + anypl.getTask());
						eventlist.add("Current action progress: " + anypl.getProgress()+"%");
					}
				}
			}
		}
		
		// Progress Logging
		CumulatingProgressListener cpl = (CumulatingProgressListener) consumer.getProgressListener(CumulatingProgressListener.class);
		if (cpl != null) {
			for (String msg : cpl.getMessages()) {
				eventlist.add("Consumer: " + msg);
			}
		} else {
			eventlist.add("NO CumulatingProgressListener found for " + consumer.getName());
			StatusProgressListener spl = (StatusProgressListener) consumer.getProgressListener(StatusProgressListener.class);
			if (spl != null) {
				eventlist.add("Consumer status: " + spl.toString());
			} else {
				// get any pl
				ProgressListener anypl = (ProgressListener) consumer.getProgressListener(ProgressListener.class);
				if (anypl != null) {
					eventlist.add("Consumer action task: " + anypl.getTask());
					eventlist.add("Consumer action progress: " + anypl.getProgress() + "%");
				} else {
					eventlist.add("NO ProgressListener found for " + consumer.getName());
				}
			}
		}
		
		mav.addObject("eventlist", eventlist);
	}
}
