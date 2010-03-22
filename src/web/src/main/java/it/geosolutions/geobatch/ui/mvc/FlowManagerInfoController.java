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

import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.flow.event.ProgressListener;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.geobatch.flow.event.listeners.cumulator.CumulatingProgressListener;
import it.geosolutions.geobatch.flow.event.listeners.status.StatusProgressListener;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author Alessio
 * 
 */
public class FlowManagerInfoController extends AbstractController {
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet
     * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Catalog catalog = (Catalog) getApplicationContext().getBean("catalog");

        String fmId = request.getParameter("fmId");

//        if (fmId != null) {
//            FileBasedFlowManager fm = catalog.getResource(fmId, FileBasedFlowManager.class);
//
//            if ((fm != null) && fm.isRunning()) {
//                fm.pause();
//            }
//        }

        ModelAndView mav = new ModelAndView("flowinfo");
        FileBasedFlowManager fm = catalog.getResource(fmId, FileBasedFlowManager.class);
        mav.addObject("flowManager", fm);

        // we're printing out some info, but we could as well pass these info to
        // the web client in order to display whatever we like

        System.out.println("FLOWMANAGER: " + fm.getName());

        for (EventConsumer ec : fm.getEventConsumers()) {
            FileBasedEventConsumer fbec = (FileBasedEventConsumer)ec; //FIXME: we need some dynamic type recognition
            System.out.println("ec     : " + fbec);
            System.out.println("ec id  : " + fbec.getId());
            System.out.println("ec name: " + fbec.getName());
            System.out.println("ec desc: " + fbec.getDescription());
            System.out.println("ec stat: " + fbec.getStatus());

            // prints out Consumer listeners info
            {
                // try the most interesting information holder
                CumulatingProgressListener cpl = (CumulatingProgressListener)fbec.getProgressListener(CumulatingProgressListener.class);
                if(cpl != null) {
                    System.out.println("fbec eventlist: ");
                    for (String msg : cpl.getMessages()) {
                        System.out.println("   " + msg);
                    }
                } else {
                    System.out.println("NO CumulatingProgressListener found for " + fbec.getName());
                    StatusProgressListener spl = (StatusProgressListener)fbec.getProgressListener(StatusProgressListener.class);
                    if(spl != null) {
                        System.out.println("fbec status: " + spl.toString());
                    } else {
                        // get any pl
                        ProgressListener anypl = (ProgressListener)fbec.getProgressListener(ProgressListener.class);
                        if(anypl != null) {
                            System.out.println("fbec action task: " + anypl.getTask());
                            System.out.println("fbec action prgr: " + anypl.getProgress()+"%");
                        } else {
                            System.out.println("NO ProgressListener found for " + fbec.getName());
                        }
                    }
                }

            }
            BaseAction action = (BaseAction)fbec.getCurrentAction();
            if(action != null) {
                System.out.println("ec action name:   " + action.getName() + " ["+action.getClass().getSimpleName()+"]");
                System.out.println("ec action paused: " + (action.isPaused()));

                // try the most interesting information holder
                CumulatingProgressListener cpl = (CumulatingProgressListener)action.getProgressListener(CumulatingProgressListener.class);
                if(cpl != null) {
                    System.out.println("action eventlist: " + cpl.getMessages());
                } else {
                    StatusProgressListener spl = (StatusProgressListener)action.getProgressListener(StatusProgressListener.class);
                    if(spl != null) {
                        System.out.println("action status: " + spl.toString());
                    } else {
                        // get any pl
                        ProgressListener anypl = (ProgressListener)action.getProgressListener(ProgressListener.class);
                        if(anypl != null) {
                            System.out.println("ec action task: " + anypl.getTask());
                            System.out.println("ec action prgr: " + anypl.getProgress()+"%");
                        }
                    }
                }
//                System.out.println("ec act:  " + action.getL);
            }
        }

        return mav;
    }
}
