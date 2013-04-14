/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
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

package it.geosolutions.geobatch.beam;

import it.geosolutions.geobatch.beam.netcdf.BeamNetCDFWriter;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import javax.media.jai.Interpolation;

public class BeamGeorectifierConfiguration extends ActionConfiguration {

    enum OutputFormat {
        NETCDF {
            BeamFormatWriter getFormatWriter() {
                return new BeamNetCDFWriter();
            }
        };
        
        abstract BeamFormatWriter getFormatWriter();
    }
    
    protected BeamGeorectifierConfiguration() {
        super("XSTREAM PROBLEM!", "XSTREAM PROBLEM!", "XSTREAM PROBLEM!");
    }


    public BeamGeorectifierConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    private long JAICapacity;

    private String wildcardString = "*.*";

    private String dimensions;

    private boolean logNotification = true;

    private String filterVariables;

    private boolean filterInclude = true;

    private boolean geophysics = false;

    private String outputFolder;

    private String outputFormat = "NETCDF"; // default 
   
    public static BeamFormatWriter getFormatWriter(String storeType) {
        if (storeType.equalsIgnoreCase(OutputFormat.NETCDF.toString())) {
            return OutputFormat.NETCDF.getFormatWriter();
        }
        
        return null;
        
    }
    /**
     *
     * Interpolation method used througout all the program.
     *
     * @TODO make the interpolation method customizable from the user perpsective.
     *
     */
    private int interp = Interpolation.INTERP_NEAREST;

    public long getJAICapacity() {
        return JAICapacity;
    }

    public void setJAICapacity(long JAICapacity) {
        this.JAICapacity = JAICapacity;
    }

    public String getWildcardString() {
        return wildcardString;
    }

    public void setWildcardString(String wildcardString) {
        this.wildcardString = wildcardString;
    }

    public int getInterp() {
        return interp;
    }

    public void setInterp(int interp) {
        this.interp = interp;
    }

    public boolean isLogNotification() {
        return logNotification;
    }

    public void setLogNotification(boolean logNotification) {
        this.logNotification = logNotification;
    }

    @Override
    public BeamGeorectifierConfiguration clone() {
        final BeamGeorectifierConfiguration configuration = (BeamGeorectifierConfiguration) super.clone();
        return configuration;
    }

    public String getFilterVariables() {
        return filterVariables;
    }

    public void setFilterVariables(String filterVariables) {
        this.filterVariables = filterVariables;
    }

    public boolean isFilterInclude() {
        return filterInclude;
    }

    public void setFilterInclude(boolean filterInclude) {
        this.filterInclude = filterInclude;
    }

    public boolean isGeophysics() {
        return geophysics;
    }

    public void setGeophysics(boolean geophysics) {
        this.geophysics = geophysics;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }
}
