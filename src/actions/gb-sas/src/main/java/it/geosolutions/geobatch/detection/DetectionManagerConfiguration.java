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

package it.geosolutions.geobatch.detection;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;


/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class DetectionManagerConfiguration extends ActionConfiguration implements
        Configuration {

	private String workingDirectory;
	
	private List<String> variables;
	
	private String crsDefinitionsDir;
	
	private String detectionConverterPath;
    
    private String detectionsOutputDir;

    private String detectionsErrorLog;
    
    private String detectionStyle;
    
    private String loggingDir;
    
    private String executablePath;
    
    private String xslPath;
    
    private String path;
    
    private String gdalData;
    
	private String geoserverURL;

    private String geoserverUID;

    private String geoserverPWD;

    private String geoserverUploadMethod;
    
    private String defaultNamespace;
    
    private String wmsPath;
    
    private long converterTimeout;
    
    public void setExecutablePath(String executablePath) {
		this.executablePath = executablePath;
	}

	public void setXslPath(String xslPath) {
		this.xslPath = xslPath;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public String getXslPath() {
		return xslPath;
	}

	public void setGdalData(String gdalData) {
		this.gdalData = gdalData;
	}

	public String getGdalData() {
		return gdalData;
	}

	public String getExecutablePath() {
		return executablePath;
	}

	public List<String> getVariables() {
		return variables;
	}

	public void setVariables(List<String> variables) {
		this.variables = variables;
	}
    
    public void setWmsPath(String wmsPath) {
		this.wmsPath = wmsPath;
	}

	public String getWmsPath() {
		return wmsPath;
	}
    
    public String getDefaultNamespace() {
		return defaultNamespace;
	}

	public void setDefaultNamespace(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}

	public String getGeoserverURL() {
        return geoserverURL;
    }

    public void setGeoserverURL(String geoserverURL) {
        this.geoserverURL = geoserverURL;
    }

    public String getGeoserverUID() {
        return geoserverUID;
    }

    public void setGeoserverUID(String geoserverUID) {
        this.geoserverUID = geoserverUID;
    }

    public String getGeoserverPWD() {
        return geoserverPWD;
    }

    public void setGeoserverPWD(String geoserverPWD) {
        this.geoserverPWD = geoserverPWD;
    }

    public String getGeoserverUploadMethod() {
        return geoserverUploadMethod;
    }

    public void setGeoserverUploadMethod(String geoserverUploadMethod) {
        this.geoserverUploadMethod = geoserverUploadMethod;
    }

    public String getDetectionsErrorLog() {
		return detectionsErrorLog;
	}

	public void setLoggingDir(String loggingDir) {
		this.loggingDir = loggingDir;
	}

	public String getLoggingDir() {
		return loggingDir;
	}

	public void setDetectionsErrorLog(String detectionsErrorLog) {
		this.detectionsErrorLog = detectionsErrorLog;
	}
	
	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
    
    public DetectionManagerConfiguration() {
        super();
    }

    protected DetectionManagerConfiguration(String id, String name, String description,
            boolean dirty) {
        super(id, name, description, dirty);
    }

    public String getCrsDefinitionsDir() {
		return crsDefinitionsDir;
	}

	public void setCrsDefinitionsDir(String crsDefinitionsDir) {
		this.crsDefinitionsDir = crsDefinitionsDir;
	}

	public void setConverterTimeout(long converterTimeout) {
		this.converterTimeout = converterTimeout;
	}

	public long getConverterTimeout() {
		return converterTimeout;
	}

	public String getDetectionConverterPath() {
		return detectionConverterPath;
	}

	public String getDetectionStyle() {
		return detectionStyle;
	}

	public void setDetectionStyle(String detectionStyle) {
		this.detectionStyle = detectionStyle;
	}

	public void setDetectionConverterPath(String detectionConverterPath) {
		this.detectionConverterPath = detectionConverterPath;
	}

	public String getDetectionsOutputDir() {
		return detectionsOutputDir;
	}

	public void setDetectionsOutputDir(String detectionsOutputDir) {
		this.detectionsOutputDir = detectionsOutputDir;
	}


    public DetectionManagerConfiguration clone() throws CloneNotSupportedException {
    	try {
			return (DetectionManagerConfiguration) BeanUtils.cloneBean(this);
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
