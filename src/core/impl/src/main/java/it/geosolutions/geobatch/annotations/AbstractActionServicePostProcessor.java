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

import java.lang.reflect.Method;

public abstract class AbstractActionServicePostProcessor {

	public AbstractActionServicePostProcessor() {
		super();
	}

	/**
	 * Returns true if annotation is present on the bean
	 * @param annotation
	 * @param bean
	 * @return
	 */
	protected boolean isAnnotationPresent(Class clazz, Object bean){

		//check all the methods of bean
		Method[] methods = bean.getClass().getMethods();

		for (Method method : methods)
			if(!isAnnotationPresent(method, clazz))
				continue;
			else
				return true;

		return false;
	}

	protected boolean isAnnotationPresent(Method method, Class clazz){

		return method.getAnnotation(clazz) != null ? true: false;
	}

	protected void justChecking(){

		isAnnotationPresent(Action.class, null);
	}

}
