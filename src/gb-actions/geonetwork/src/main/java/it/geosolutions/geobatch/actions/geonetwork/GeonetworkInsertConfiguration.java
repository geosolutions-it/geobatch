/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2011 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.actions.geonetwork;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;


/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class GeonetworkInsertConfiguration 
        extends ActionConfiguration 
        implements Configuration {
    
    public GeonetworkInsertConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    /**
     * URL where the GN services can be accessed.
     */
    private String geonetworkServiceURL;

    /**
     * Credential for accessing to GeoNetwork services.
     */
    private String loginUsername;
    /**
     * Credential for accessing to GeoNetwork services.
     */
    private String loginPassword;

    /**
     * The provided data only has the metadata information?
     * If true, then also category, group, stylesheet and validate must be set in configuration.
     */    
    private boolean onlyMetadataInput;

/*    
     * http://geonetwork-opensource.org/latest/developers/xml_services/metadata_xml_services.html#insert-metadata-metadata-insert
     * 
data: (mandatory) Contains the metadata record
group (mandatory): Owner group identifier for metadata
isTemplate: indicates if the metadata content is a new template or not. Default value: “n”
title: Metadata title. Only required if isTemplate = “y”
category (mandatory): Metadata category. Use “_none_” value to don’t assign any category
styleSheet (mandatory): Stylesheet name to transform the metadata before inserting in the catalog. Use “_none_” value to don’t apply any stylesheet
validate: Indicates if the metadata should be validated before inserting in the catalog. Values: on, off (default)    
     */

    /**
     * group (mandatory): Owner group identifier for metadata
     */    
    private String group;
    /**
     * category (mandatory): Metadata category. Use “_none_” value to don’t assign any category
     */    
    private String category;
    /**
     * styleSheet (mandatory): Stylesheet name to transform the metadata before inserting in the catalog. Use “_none_” value to don’t apply any stylesheet
     */    
    private String styleSheet;
    /**
     * validate: Indicates if the metadata should be validated before inserting in the catalog. Values: on, off (default)    
     */    
    private Boolean validate;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGeonetworkServiceURL() {
        return geonetworkServiceURL;
    }

    public void setGeonetworkServiceURL(String geonetworkServiceURL) {
        this.geonetworkServiceURL = geonetworkServiceURL;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public boolean isOnlyMetadataInput() {
        return onlyMetadataInput;
    }

    public void setOnlyMetadataInput(boolean onlyMetadataInput) {
        this.onlyMetadataInput = onlyMetadataInput;
    }

    public String getStyleSheet() {
        return styleSheet;
    }

    public void setStyleSheet(String styleSheet) {
        this.styleSheet = styleSheet;
    }

    public Boolean getValidate() {
        return validate;
    }

    public void setValidate(Boolean validate) {
        this.validate = validate;
    }
}
