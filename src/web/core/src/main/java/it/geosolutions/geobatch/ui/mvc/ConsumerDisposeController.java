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

import it.geosolutions.geobatch.flow.FlowManager;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;

import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class ConsumerDisposeController extends ConsumerAbstractController {

    private FlowManager fm;

    protected void handleFlowManager(ModelAndView mav, FlowManager fm) throws IllegalArgumentException {

        super.handleFlowManager(mav, fm);

        this.fm = fm;
    }

    @Override
    protected void handleConsumer(ModelAndView mav, EventConsumer consumer) throws IllegalArgumentException {
        if (consumer!=null){
            final EventConsumerStatus status = consumer.getStatus();
            disposeConsumer(status, fm, consumer);
        }
    }

    public static void disposeConsumer(final EventConsumerStatus status, final FlowManager fm,
                                       final EventConsumer consumer) throws IllegalArgumentException {
        if (status.equals(EventConsumerStatus.COMPLETED) || status.equals(EventConsumerStatus.CANCELED)
            || status.equals(EventConsumerStatus.FAILED)) {

            // dispose the object
            if (fm != null) {
                fm.disposeConsumer(consumer.getId());
            } else {
                throw new IllegalArgumentException("ERROR: Consumer instance '" + consumer.getId()
                                                   + "' not found");
            }

        }
    }

}
