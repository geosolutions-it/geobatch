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
package it.geosolutions.geobatch.actions.geonetwork.configuration;


import java.util.ArrayList;
import java.util.List;


/**
 * TODO: implement the clone() method
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class GeonetworkInsertConfiguration 
        extends GeonetworkConfiguration {
    
    public GeonetworkInsertConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

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

    /**
     * Operation provileges as erquired by GeoNetwork:
     * <UL>
     * <LI><TT>key</TT>: the id of the group these operations are related to.</LI>
     * <LI><TT>value</TT>: the set of operations allowed to this group for the inserted metadata.
     * <br/>Operations are defined as a string of digits, each representing a granted privilege: <UL>
     * <LI>0: view</LI>
     * <LI>1: download</LI>
     * <LI>2: editing</LI>
     * <LI>3: notify</LI>
     * <LI>4: dynamic</LI>
     * <LI>5: featured</LI>
       </UL></LI>
     * </UL>
     * e.g.:<br/>
     *  to assign the privileges "view" and "download" to group 5, this entry shall
     * be added to the Map:
     * <br/><pre>{@code         
     *      operations.put("5,"01"); }
     * </pre>
     */ 
//    private Map<Integer, String> privileges = new HashMap<Integer, String>();
    private List<Privileges> privileges = new ArrayList<Privileges>();
    
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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

//    public Map<Integer, String> getPrivileges() {
//        return privileges;
//    }
//
//    protected void setPrivileges(Map<Integer, String> privileges) {
//        this.privileges = privileges;
//    }
//    
//    public void addPrivileges(Integer groupCode, String ops) {
//        synchronized(this) {
//            if(privileges == null)
//                privileges = new HashMap<Integer, String>();
//        }
//
//        if(!ops.matches("0?1?2?3?4?5?")) {
//            throw new IllegalArgumentException("Unrecognized privileges set '"+ops+"'");
//        }
//        
//        privileges.put(groupCode, ops);
//    }

    public List<Privileges> getPrivileges() {
        return privileges;
    }

    protected void setPrivileges(List<Privileges> privileges) {
        this.privileges = privileges;
    }
    
    public void addPrivileges(Integer groupCode, String ops) {
        synchronized(this) {
            if(privileges == null)
                privileges = new ArrayList<Privileges>();
        }

        if(!ops.matches("0?1?2?3?4?5?")) {
            throw new IllegalArgumentException("Unrecognized privileges set '"+ops+"'");
        }
        
        privileges.add(new Privileges(groupCode, ops));
    }

    
    static public class Privileges {
        Integer group;
        String ops;

        public Privileges(Integer group, String ops) {
            this.group = group;
            this.ops = ops;
        }

        public Integer getGroup() {
            return group;
        }

        public void setGroup(Integer group) {
            this.group = group;
        }

        public String getOps() {
            return ops;
        }

        public void setOps(String ops) {
            this.ops = ops;
        }
    }
    
}
