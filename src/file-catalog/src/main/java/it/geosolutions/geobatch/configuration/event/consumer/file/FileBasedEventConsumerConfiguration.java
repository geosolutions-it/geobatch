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

package it.geosolutions.geobatch.configuration.event.consumer.file;

import it.geosolutions.geobatch.catalog.impl.BaseConfiguration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;

import java.util.ArrayList;
import java.util.List;


/**
 * Conf for the event consumers based on xml marshalled files.
 * *
 * @author Simone Giannecchini, GeoSolutions
 */
public class FileBasedEventConsumerConfiguration extends BaseConfiguration implements EventConsumerConfiguration
{

    /**
     * List of configurable actions that will be sequentially performed at the end of event consumption.
     * @uml.property  name="actions"
     * @uml.associationEnd  multiplicity="(0 -1)" elementType="it.geosolutions.geobatch.configuration.event.action.ActionConfiguration"
     */
    private List<? extends ActionConfiguration> actions;

    /**
     * workingDirectory: this attribute represents the configuring directory for this flow.
     * It can be relative to the catalog.xml directory or absolute.
     * The configuring directory. This is the directory where the consumer will store the input data.
     */
    private String workingDirectory;

    /**
     * Do we remove input files and put them on a backup directory?
     * @uml.property  name="performBackup"
     */
    private boolean performBackup;


    private boolean preserveInput;

    /**
     * The id of the Listener Conf. <BR> They are needed for a post-load binding; loader logic will put the proper listener configurations into  {@link #listenerConfigurations} .
     * @uml.property  name="listenerIds"
     */
    private List<String> listenerIds = new ArrayList<String>();

    /**
     * These configs are filled by the loader, dereferencing the listenersId.
     * @uml.property  name="listenerConfigurations"
     * @uml.associationEnd  multiplicity="(0 -1)" elementType="it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration"
     */
    protected List<ProgressListenerConfiguration> listenerConfigurations =
        new ArrayList<ProgressListenerConfiguration>();

    /**
     * do not remove ContextDirectory when consumer is disposed
     */
    private boolean keepContextDir = false;


    /**
     * Default Constructor.
     */
    protected FileBasedEventConsumerConfiguration(String id, String name, String description)
    {
        super(id, name, description);
    }

    /**
     * @return the keepContextDir
     */
    public boolean isKeepContextDir()
    {
        return keepContextDir;
    }

    /**
     * @param keepContextDir the keepContextDir to set
     */
    public void setKeepContextDir(boolean keepContextDir)
    {
        this.keepContextDir = keepContextDir;
    }

    /**
     * Getter for the consumer actions.
     *
     * @return actions
     */
    public List<? extends ActionConfiguration> getActions()
    {
        return this.actions;
    }

    /**
     * Setter for the consumer actions.
     *
     * @param actions
     */
    public void setActions(List<? extends ActionConfiguration> actions)
    {
        this.actions = new ArrayList<ActionConfiguration>(actions);
    }

    /**
     * Getter for the configuring directory attribute.
     * @return  workingDirectory
     * @uml.property  name="workingDirectory"
     */
    public String getWorkingDirectory()
    {
        return workingDirectory;
    }

    /**
     * Setter for the configuring directory attribute.
     * @param  workingDirectory
     * @uml.property  name="workingDirectory"
     */
    public void setWorkingDirectory(String workingDirectory)
    {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Is the backup of the input data enabled?
     * @return  performBackup
     * @uml.property  name="performBackup"
     */
    public boolean isPerformBackup()
    {
        return performBackup;
    }

    /**
     * Setter for the perform backup option.
     * @param  performBackup
     * @uml.property  name="performBackup"
     */
    public void setPerformBackup(boolean performBackup)
    {
        this.performBackup = performBackup;
    }

    /**
     * @return
     * @uml.property  name="preserveInput"
     */
    public boolean isPreserveInput()
    {
        return this.preserveInput;
    }

    /**
     * @param preserveInput
     * @uml.property  name="preserveInput"
     */
    public void setPreserveInput(boolean preserveInput)
    {
        this.preserveInput = preserveInput;
    }

    public List<String> getListenerIds()
    {
        return listenerIds;
    }

    public void setListenerId(List<String> ids)
    {
        this.listenerIds = ids;
    }

    public void addListenerConfiguration(ProgressListenerConfiguration plc)
    {
        synchronized (this)
        {
            if (listenerConfigurations == null) // this may happen when
            {
                // loading via XStream
                listenerConfigurations = new ArrayList<ProgressListenerConfiguration>();
            }
        }
        listenerConfigurations.add(plc);
    }

    public List<ProgressListenerConfiguration> getListenerConfigurations()
    {
        synchronized (this)
        {
            if (listenerConfigurations == null) // this may happen when
            {
                // loading via XStream
                listenerConfigurations = new ArrayList<ProgressListenerConfiguration>();
            }
        }

        return listenerConfigurations;
    }

    @Override
    public FileBasedEventConsumerConfiguration clone()
    { // throws

        // CloneNotSupportedException
        // {

        // clone object
        final FileBasedEventConsumerConfiguration object = new FileBasedEventConsumerConfiguration(
                super.getId(), super.getName(), super.getDescription());
        object.setDirty(super.isDirty());
        object.setPerformBackup(performBackup);
        object.setPreserveInput(preserveInput);
        object.setWorkingDirectory(workingDirectory);


        final List<ActionConfiguration> clonedActions = new ArrayList<ActionConfiguration>(actions.size());
        for (ActionConfiguration action : actions)
        {
            clonedActions.add(action.clone());
        }
        object.setActions(clonedActions);

        // clone listeners
        if (listenerConfigurations != null) // tricks from xstream
        {
            for (ProgressListenerConfiguration progressListenerConfiguration : listenerConfigurations)
            {
                object.addListenerConfiguration(progressListenerConfiguration);
            }
        }

        return object;

    }

}
