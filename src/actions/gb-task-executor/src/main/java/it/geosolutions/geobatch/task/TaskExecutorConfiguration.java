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

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;


/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions S.a.S.
 */
public class TaskExecutorConfiguration extends ActionConfiguration implements
        Configuration {

	public String getXsl() {
		return xsl;
	}

	public void setXsl(String xsl) {
		this.xsl = xsl;
	}

	public String getExecutable() {
		return executable;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	public String getErrorFile() {
		return errorFile;
	}

	public void setErrorFile(String errorFile) {
		this.errorFile = errorFile;
	}

	public Long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(Long timeOut) {
		this.timeOut = timeOut;
	}

	public Map<String,String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String,String> variables) {
		this.variables = variables;
	}
	
	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
	private String workingDirectory;

	private String executable;
	
	private String errorFile;
	
	private Long timeOut;
	
//	private boolean spawn;
	
    private Map<String,String> variables;
    
    private String xsl;
	
    public TaskExecutorConfiguration() {
        super();
    }

    protected TaskExecutorConfiguration(String id, String name, String description,boolean dirty) {
        super(id, name, description, dirty);
    }
    
    public TaskExecutorConfiguration clone() throws CloneNotSupportedException {
    	try {
			return (TaskExecutorConfiguration) BeanUtils.cloneBean(this);
		} catch (IllegalAccessException e) {
			final CloneNotSupportedException cns= new CloneNotSupportedException();
			cns.initCause(e);
			throw cns;
		} catch (InstantiationException e) {
			final CloneNotSupportedException cns= new CloneNotSupportedException();
			cns.initCause(e);
			throw cns;
		} catch (InvocationTargetException e) {
			final CloneNotSupportedException cns= new CloneNotSupportedException();
			cns.initCause(e);
			throw cns;
		} catch (NoSuchMethodException e) {
			final CloneNotSupportedException cns= new CloneNotSupportedException();
			cns.initCause(e);
			throw cns;
		}
    }

}
