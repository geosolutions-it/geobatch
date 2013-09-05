/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  https://github.com/nfms4redd/nfms-geobatch
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
package it.geosolutions.geobatch.annotations;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the annotated Class as an Action to execute aliasing and action processing on it
 *
 * @author	Sandro Salari
 * @param 	configurationClass	an {@link ActionConfiguration} class that implements action logic
 * @param  	configurationAlias 	an alias String used in configuration XML to identify the configuration class
 * @see     ActionServicePostProcessor
 * @see     BaseAction
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Action {
	Class<? extends ActionConfiguration> configurationClass();
	String configurationAlias() default "";
    Class[] aliases() default {};
    String[] implicitCollections() default {};
}
