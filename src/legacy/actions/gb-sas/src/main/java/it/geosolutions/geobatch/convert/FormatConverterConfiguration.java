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



package it.geosolutions.geobatch.convert;

import it.geosolutions.geobatch.base.BaseImageProcessingConfiguration;
import it.geosolutions.geobatch.catalog.Configuration;

/**
 * Format Converter configuration
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class FormatConverterConfiguration extends BaseImageProcessingConfiguration implements
        Configuration {

	/** Output Format to convert input data */
	private String outputFormat;
    
	/** Supported input formats */
    private String inputFormats;
    
    /** Where to store converted data */
    private String outputDirectory;
    
    // When converted, output data is ingested to geoserver
    private String geoserverURL;

    private String geoserverUID;

    private String geoserverPWD;

    private String geoserverUploadMethod;
    
    private String geowebcacheWatchingDir;

    public String getGeowebcacheWatchingDir() {
		return geowebcacheWatchingDir;
	}

	public void setGeowebcacheWatchingDir(String geowebcacheWatchingDir) {
		this.geowebcacheWatchingDir = geowebcacheWatchingDir;
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

    public FormatConverterConfiguration() {
        super();
    }

    protected FormatConverterConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
	}
    
    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getInputFormats() {
        return inputFormats;
    }

    public void setInputFormats(String inputFormats) {
        this.inputFormats = inputFormats;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

	@Override
	public FormatConverterConfiguration clone() throws CloneNotSupportedException {
		final FormatConverterConfiguration configuration= 
			new FormatConverterConfiguration(getId(),getName(),getDescription(),isDirty());
		configuration.setCompressionRatio(getCompressionRatio());
		configuration.setCompressionScheme(getCompressionScheme());
		configuration.setDownsampleStep(getDownsampleStep());
		configuration.setInputFormats(inputFormats);
		configuration.setNumSteps(getNumSteps());
		configuration.setOutputDirectory(outputDirectory);
		configuration.setOutputFormat(outputFormat);
		configuration.setServiceID(getServiceID());
		configuration.setTileH(getTileH());
		configuration.setTileW(getTileW());
		configuration.setWorkingDirectory(getWorkingDirectory());
		return configuration;
	}
}
