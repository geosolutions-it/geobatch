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

import it.geosolutions.geobatch.ui.mvc.data.FlowManagerDataBean;

import java.io.File;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Alessio Fabiani
 * 
 */
public class FlowManagerFormValidator implements Validator {
    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    public boolean supports(Class givenClass) {
        return givenClass.equals(FlowManagerDataBean.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.validation.Validator#validate(java.lang.Object,
     * org.springframework.validation.Errors)
     */
    public void validate(Object obj, Errors errors) {
        FlowManagerDataBean givenData = (FlowManagerDataBean) obj;

        if (givenData == null) {
            errors.reject("error.nullpointer", "Null data received");
        } else {
            /* Test givenData�s fields here */
            if ((givenData.getDescriptorId() == null)
                    || (givenData.getDescriptorId().trim().length() <= 0)) {
                errors.rejectValue("descriptorId", "error.code",
                        "Flow Manager Descriptor is mandatory.");
            }

            if ((givenData.getId() == null) || (givenData.getId().trim().length() <= 0)) {
                errors.rejectValue("id", "error.code", "Flow Manager ID is mandatory.");
            }

            if ((givenData.getName() == null) || (givenData.getName().trim().length() <= 0)) {
                errors.rejectValue("name", "error.code", "Flow Manager Name is mandatory.");
            }

            if ((givenData.getInputDir() == null) || (givenData.getInputDir().trim().length() <= 0)) {
                errors.rejectValue("inputDir", "error.code", "Input Directory is mandatory.");
            }

            if ((givenData.getInputDir() != null)
                    && !(new File(givenData.getInputDir()).isDirectory())) {
                errors.rejectValue("inputDir", "error.code",
                        "The specified Input Directory is not valid.");
            }

            if ((givenData.getOutputDir() == null)
                    || (givenData.getOutputDir().trim().length() <= 0)) {
                errors.rejectValue("workingDirectory", "error.code",
                        "Output Directory is mandatory.");
            }

            if ((givenData.getOutputDir() != null)
                    && !(new File(givenData.getOutputDir()).isDirectory())) {
                errors.rejectValue("workingDirectory", "error.code",
                        "The specified Output Directory is not valid.");
            }
        }
    }
}
