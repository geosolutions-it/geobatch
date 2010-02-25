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



package it.geosolutions.geobatch.configuration.flow.file;

import it.geosolutions.geobatch.catalog.impl.BaseConfiguration;
import it.geosolutions.geobatch.configuration.CatalogConfiguration;

/**
 * <p>
 * A Configuration for the Catalog based on xml marshalled files.
 * </p>
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Alessio Fabiani, GeoSolutions
 */
public class FileBasedCatalogConfiguration extends BaseConfiguration implements
        CatalogConfiguration {

    // private List<FlowConfiguration> flowConfigurations;

    /**
     * workingDirectory: this attribute represents the configuring directory for this flow. It can
     * be relative to the catalog.xml directory or absolute.
     * 
     * Attention: the configuring directory should be different from the one containing the
     * configuration files.
     */
    private String workingDirectory;

    /**
     * Default Constructor.
     */
    public FileBasedCatalogConfiguration() {
        super();
    }

    /**
     * Getter for the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Setter for the workingDirectory.
     * 
     * @param workingDirectory
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "id:" + getId() + ", workingDirectory:"
                + getWorkingDirectory() + ", name:" + getName() + "]";
    }
}
