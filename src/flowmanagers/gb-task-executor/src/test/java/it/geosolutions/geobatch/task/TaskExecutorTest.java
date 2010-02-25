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

package it.geosolutions.geobatch.task;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class TaskExecutorTest {
	
	 @Test
	 @Ignore
	 public void testTaskExecution() throws Exception{
		TaskExecutorConfiguration configuration = new TaskExecutorConfiguration();
		configuration.setExecutable("c:/Python26/python.exe");
		configuration.setErrorFile("C:/errorlog.txt");
		configuration.setTimeOut(new Long(10000));
        final Map<String,String> variables = new HashMap<String, String>();
		variables.put("GDAL_DATA", "C:\\Python26\\DLLs\\gdalwin32-1.6\\data");
		variables.put("PATH", "C:\\Python26");
		configuration.setVariables(variables);
		TaskExecutor executor = new TaskExecutor(configuration);
		executor.execute(null);
	 }
	 

}
