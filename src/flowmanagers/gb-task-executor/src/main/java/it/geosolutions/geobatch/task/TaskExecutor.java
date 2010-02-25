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

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * Action to execute tasks such as invoking python scripts, gdal utilities and 
 * similar command lines.
 * 
 * @author Daniele Romagnoli, GeoSolutions S.a.S.
 */
public class TaskExecutor extends BaseAction<FileSystemMonitorEvent> implements Action<FileSystemMonitorEvent> {

	private final static Logger LOGGER = Logger.getLogger(TaskExecutor.class.toString());
	
    private TaskExecutorConfiguration configuration;

	public TaskExecutor(final TaskExecutorConfiguration configuration) throws IOException {
    	this.configuration = configuration;
    }

	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {
		
		 // looking for file
        if (events.size() != 1)
            throw new IllegalArgumentException("Wrong number of elements for this action: "+ events.size());
        
        if (configuration == null) {
            throw new IllegalStateException("DataFlowConfig is null.");
        }
        
        // get the first event
        final FileSystemMonitorEvent event = events.remove();
        final File inputFile = event.getSource();
        
        //Getting XSL file definition
        final String xslPath = configuration.getXsl();
        File xslFile = null;
		if (xslPath != null && xslPath.trim().length()>0){
        	 xslFile = IOUtils.findLocation(xslPath,
	                 new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
        }
		if (xslFile == null || !xslFile.exists())
			throw new IllegalArgumentException("The specified XSL file hasn't been found: "+xslPath);
		
		//Setup an XML source from the input XML file
		final Source xmlSource = new StreamSource(inputFile);
		InputStream is = null;
		try {
			is = new FileInputStream(xslFile);
	        if (is != null){
	        	
	        	//XML parsing to setup a command line
				final String argument = buildArgument(xmlSource, is);
				   
				final Project project = new Project();
				project.init();
		
				final ExecTask execTask = new ExecTask();
				execTask.setProject(project);
				
				// Setting environment variables coming from the configuration
				// as an instance: PATH, LD_LIBRARY_PATH and similar
				Map<String,String> variables = configuration.getVariables();
				if (variables != null && !variables.isEmpty()){
					for (String key: variables.keySet()){
						Variable var = new Variable();
						var.setKey(key);
						final String value = variables.get(key);
						if (value != null){
							var.setValue(variables.get(key));
							execTask.addEnv(var);
						}
					}
				}
				
				//Setting executable
				execTask.setExecutable(configuration.getExecutable());
				
				//Setting Error logging
				final String errorPath = configuration.getErrorFile();
				if (errorPath!=null && errorPath.trim().length()>0){
					File errorFile = IOUtils.findLocation(errorPath,
				                 new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
					if (errorFile != null){
						if(!errorFile.exists()){
							try{
								errorFile.createNewFile();
							} catch (Throwable t){
								if (LOGGER.isLoggable(Level.WARNING))
										LOGGER.warning(new StringBuilder("The specified errorFile doesn't exist.")
										.append(" Unable to create it due to:").append(t.getLocalizedMessage()).toString());
							}
						}
						if (errorFile.exists()){
							execTask.setLogError(true);
							execTask.setError(errorFile);
							execTask.setFailonerror(true);
						}
					}
				}
					
				//Setting the timeout
				Long timeOut = configuration.getTimeOut();
				if (timeOut!=null){
					execTask.setTimeout(timeOut);
				}
				
				//Setting command line argument
				execTask.createArg().setLine(argument);
				
				//Executing
				execTask.execute();
	         }
		} catch (BuildException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine(e.getLocalizedMessage());
		}finally{
			try{
				if (is!=null)
					is.close();
			}catch (Throwable t){
					//eat me
			}
		}
		return events;
	}

	private String buildArgument(final Source xmlSource, final InputStream is) throws TransformerException {
		//XML parsing to setup a command line
		final TransformerFactory f = TransformerFactory.newInstance();
		final StringWriter result = new StringWriter();
		final Templates transformation = f.newTemplates(new StreamSource(is));
		final Transformer transformer = transformation.newTransformer();
		transformer.transform(xmlSource, new StreamResult(result));
		final String argument = result.toString();
		return argument;
	}
	
}
