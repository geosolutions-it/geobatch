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

package it.geosolutions.geobatch.flow.event.consumer.file;

import java.util.ArrayList;
import java.util.List;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.impl.BaseConfiguration;


public class FileEventRule extends BaseConfiguration implements Cloneable
{


    private String regex;


    private int originalOccurrencies;


    private int actualOccurrencies;


    private boolean optional;


    private List<FileSystemEventType> acceptableNotifications;

//    public FileEventRule() {
//        super();
//    }

    public FileEventRule(String id, String name, String description, boolean dirty)
    {
        super(id, name, description, dirty);
    }

    /**
     * @return
     * @uml.property  name="regex"
     */
    public String getRegex()
    {
        return regex;
    }

    /**
     * @param regex
     * @uml.property  name="regex"
     */
    public void setRegex(String regex)
    {
        this.regex = regex;
    }

    /**
     * @return
     * @uml.property  name="originalOccurrencies"
     */
    public int getOriginalOccurrencies()
    {
        return originalOccurrencies;
    }

    /**
     * @param occurrencies
     * @uml.property  name="actualOccurrencies"
     */
    public void setActualOccurrencies(int occurrencies)
    {
        this.actualOccurrencies = occurrencies;
    }

    /**
     * @return
     * @uml.property  name="optional"
     */
    public boolean isOptional()
    {
        return optional;
    }

    /**
     * @param optional
     * @uml.property  name="optional"
     */
    public void setOptional(boolean optional)
    {
        this.optional = optional;
    }

    public List<FileSystemEventType> getAcceptableNotifications()
    {
        return acceptableNotifications;
    }

    public void setAcceptableNotifications(List<FileSystemEventType> acceptableNotifications)
    {
        this.acceptableNotifications = acceptableNotifications;
    }

    /**
     * @return
     * @uml.property  name="actualOccurrencies"
     */
    public int getActualOccurrencies()
    {
        return actualOccurrencies;
    }

    /**
     * @param originalOccurrencies
     * @uml.property  name="originalOccurrencies"
     */
    public void setOriginalOccurrencies(int originalOccurrencies)
    {
        this.originalOccurrencies = originalOccurrencies;
    }

    @Override
    public FileEventRule clone()
    { // throws CloneNotSupportedException {

        // final FileEventRule rule = new FileEventRule();
        final FileEventRule rule = (FileEventRule) super.clone();
        // rule.setId(getId());
        // rule.setName(getName());
        // rule.setDescription(getDescription());
        if (acceptableNotifications != null)
        {
            rule.setAcceptableNotifications(new ArrayList<FileSystemEventType>(
                    acceptableNotifications));
        }
        rule.setOptional(optional);
        rule.setRegex(regex);
        rule.setOriginalOccurrencies(originalOccurrencies);
        rule.setActualOccurrencies(actualOccurrencies);

        return rule;
    }

    // ??? why is it overridden?
    public void setServiceID(String serviceID)
    {

    }

    // ??? why is it overridden?
    public String getServiceID()
    {
        return null;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[");
        builder.append("id:");
        builder.append(getId());
        builder.append(", name:" + getName());
        builder.append(", regex:" + getRegex());
        builder.append("]");

        return builder.toString();
    }
}
