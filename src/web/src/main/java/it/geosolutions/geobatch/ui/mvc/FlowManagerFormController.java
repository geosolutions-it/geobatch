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
import it.geosolutions.geobatch.ui.mvc.data.FlowManagerDataBean;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * @author Alessio Fabiani
 * 
 */
public class FlowManagerFormController extends SimpleFormController {
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet
     * .http.HttpServletRequest)
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        FlowManagerDataBean backingObject = new FlowManagerDataBean();
        Catalog catalog = (Catalog) getApplicationContext().getBean("catalog");

        /*
         * The backing object should be set up here, with data for the initial values of the form�s
         * fields. This could either be hard-coded, or retrieved from a database, perhaps by a
         * parameter, eg. request.getParameter(�primaryKey�)
         */
        // backingObject.setAvailableDescriptors(catalog.getFlowManagers(FileBasedCatalogConfiguration.class));
        logger.info("Returning backing object");

        return backingObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(java.lang.Object,
     * org.springframework.validation.BindException)
     */
    @Override
    protected ModelAndView onSubmit(Object command, BindException errors) throws Exception {
        FlowManagerDataBean givenData = (FlowManagerDataBean) command;
        Catalog catalog = (Catalog) getApplicationContext().getBean("catalog");

        /*
         * The givenData object now contains the successfully validated data from the form, so can
         * be written to a database, or whatever.
         */
        // FileBasedCatalogConfiguration fmDescriptor =
        // catalog.getFlowManager(givenData.getDescriptorId(), FileBasedCatalogConfiguration.class);
        // if (fmDescriptor == null) {
        // errors.rejectValue("descriptorId", "error.code",
        // "The Flow Manager Descriptor specified is not valid.");
        //
        // return new ModelAndView(getFormView(), errors.getModel());
        // }
        //
        // FlowManager fm = catalog.getResource(givenData.getId(), FlowManager.class);
        //
        // if (fm != null) {
        // errors.rejectValue("id", "error.code", "The Flow Manager ID specified already exists.");
        //
        // return new ModelAndView(getFormView(), errors.getModel());
        // }
        // fm = fmDescriptor.getFlowManagerService().createFlowManager(fmDescriptor);
        // ((FileBasedFlowManagerImpl) fm).setId(givenData.getId());
        // ((FileBasedFlowManagerImpl) fm).setName(givenData.getName());
        // ((FileBasedFlowManagerImpl) fm).setInputDir(new File(givenData.getInputDir()));
        // ((FileBasedFlowManagerImpl) fm).setOutputDir(new File(givenData.getOutputDir()));
        // catalog.add(fm);
        // catalog.getResourceThreadPool().execute((FileBasedFlowManagerImpl) fm);
        logger.info("Form data successfully submitted");

        return new ModelAndView(getSuccessView());
    }
}
