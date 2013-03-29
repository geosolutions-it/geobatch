/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.commons.logging;

import java.util.Enumeration;

//import org.apache.log4j.LogManager;
import org.slf4j.Logger;

public class Log4JCheck {

	/**
	 * Returns true if it appears that log4j have been previously configured.
	 * This code checks to see if there are any appenders defined for log4j
	 * which is the definitive way to tell if log4j is already initialized
	 * see also:
	 * http://www.basilv.com/psd/blog/2007/how-to-add-logging-to-ant-builds
	 *
	public static boolean isConfigured() {
		Enumeration<Logger> appenders = LogManager.getRootLogger().getAllAppenders();
		if (appenders.hasMoreElements()) {
			return true;
		} else {
			Enumeration<Logger> loggers = LogManager.getCurrentLoggers();
			while (loggers.hasMoreElements()) {
				Logger c = (Logger) loggers.nextElement();
				return true;
			}
		}
		return false;
	}*/
	
	/**
	 * print info about registered appenders
	 *
	public static void printInfo() {
		final Enumeration<Logger> appenders = LogManager.getRootLogger().getAllAppenders();
		if (appenders.hasMoreElements()) {
			info(appenders);
		} else {
			info(LogManager.getCurrentLoggers());
		}
	}*/
	
	private static void info(final Enumeration<Logger> loggers){
		while (loggers.hasMoreElements()) {
			Logger c = (Logger) loggers.nextElement();
			if (c.isInfoEnabled()){
				c.info("Configured logger: "+c.getName()+" -> "+c.toString());
			}
		}
	}
}
