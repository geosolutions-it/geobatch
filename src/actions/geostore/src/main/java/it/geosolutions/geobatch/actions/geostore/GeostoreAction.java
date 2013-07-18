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
package it.geosolutions.geobatch.actions.geostore;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.actions.geostore.model.ResourceList;
import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geostore.core.model.Resource;
import it.geosolutions.geostore.core.model.StoredData;
import it.geosolutions.geostore.services.dto.ShortResource;
import it.geosolutions.geostore.services.dto.search.SearchFilter;
import it.geosolutions.geostore.services.rest.GeoStoreClient;
import it.geosolutions.geostore.services.rest.model.RESTResource;
import it.geosolutions.geostore.services.rest.model.ShortResourceList;
import it.geosolutions.geostore.services.rest.utils.GeoStoreJAXBContext;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Luca Paolino - luca.paolino@geo-solutions.it
 * 
 */
@XmlSeeAlso(ResourceList.class)
@Action(configurationClass=GeostoreActionConfiguration.class)
public class GeostoreAction extends BaseAction<FileSystemEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(GeostoreAction.class);

    private final GeoStoreClient geostore = new GeoStoreClient();
    private final GeostoreOperation.Operation operation;

    private static final Unmarshaller unmarshaller;
    static {

        Unmarshaller tmp = null;
        try {
            List<Class> classList = GeoStoreJAXBContext.getGeoStoreClasses();
            classList.add(ResourceList.class);
            JAXBContext context = JAXBContext.newInstance(classList.toArray(new Class[classList.size()]));
            tmp = context.createUnmarshaller();
        } catch (JAXBException ex) {
            LOGGER.error("Can't create GeoStore context: " + ex.getMessage(), ex);
        }
        unmarshaller = tmp;
    }

    private ResourceList resourceList = new ResourceList();

    private List<ShortResource> shortResourceList = null;

    /**
     * configuration
     */
    private final GeostoreActionConfiguration conf;

    public GeostoreAction(GeostoreActionConfiguration configuration) {
        super(configuration);
        conf = configuration;

        // init geostore parameter connection
        geostore.setGeostoreRestUrl(conf.getUrl());

        geostore.setUsername(conf.getUser());
        geostore.setPassword(conf.getPassword());

        // init parameters from configuration
        operation = conf.getOperation();
    }

    public List<ShortResource> getShortResourceList() {
        return shortResourceList;
    }

    public void nothing() {

    }

    public List<Resource> getResourceList() {
        return resourceList.getResourceList();
    }
    
    @Override
	@CheckConfiguration
	public boolean checkConfiguration() {
		// TODO Auto-generated method stub
		return false;
	}

    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events) throws ActionException {
        final Queue<FileSystemEvent> ret = new LinkedList<FileSystemEvent>();

        while (events.size() > 0) {
            final EventObject ev;

            try {
                if ((ev = events.remove()) != null) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER
                            .trace("GeostoreAction.execute(): working on incoming event: " + ev.getSource());
                    }
                    FileSystemEvent fileEvent = (FileSystemEvent)ev;
                    File inputFile = fileEvent.getSource();

                    File outFile = null;
                    LOGGER.debug("Running " + operation.name());
                    switch (operation) {
                    case SEARCH:
                        outFile = doSearch(inputFile);
                        break;
                    case INSERT:
                        outFile = doInsert(inputFile);
                        break;
                    case UPDATEDATA:
                        outFile = this.doUpdateData(inputFile);
                        break;
                    case DELETE:
                        this.doDelete(inputFile);
                        break;
                    default:
                        throw new ActionException(this, "Unknown operation " + operation);
                    }

                    if (outFile != null) {
                        FileSystemEvent fileSystemInsertEvent = new FileSystemEvent(
                                                                                    outFile,
                                                                                    FileSystemEventType.FILE_ADDED);
                        ret.add(fileSystemInsertEvent);
                    }

                } else {
                    LOGGER.error("Encountered a NULL event: SKIPPING...");
                    continue;
                }
            } catch (ActionException ex) {
                throw ex; // pass through local exceptions

            } catch (Exception ex) {

                final String message = "SearchAction.execute(): Unable to produce the output: "
                                       + ex.getLocalizedMessage();
                // LOGGER.error(message, ioe);
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message, ex);
                throw new ActionException(this, message);
            }
        }

        return ret;
    }

    protected void doDelete(File xmlResourceFile) throws JAXBException, IOException {
        RESTResource restResource = (RESTResource)unmarshaller.unmarshal(xmlResourceFile);
        geostore.deleteResource(restResource.getId());
    }

    protected File doInsert(File xmlResourceFile) throws JAXBException, IOException {

        RESTResource restResource = (RESTResource)unmarshaller.unmarshal(xmlResourceFile);
        long resourceId = geostore.insert(restResource);

        Resource resource = geostore.getResource(resourceId);
        File outputInsertFile = File.createTempFile("gstinsert_", xmlResourceFile.getName(), getTempDir());

        JAXB.marshal(resource, outputInsertFile);
        return outputInsertFile;
    }

    protected File doUpdateData(File xmlResourceFile) throws JAXBException, IOException {

        RESTResource restResource = (RESTResource)unmarshaller.unmarshal(xmlResourceFile);
        long id = restResource.getId();
        String data = restResource.getStore().getData();
        geostore.setData(id, data);
        Resource resource = geostore.getResource(id);
        File outputInsertFile = File.createTempFile("gstupdate_", xmlResourceFile.getName(), getTempDir());
        JAXB.marshal(resource, outputInsertFile);
        return outputInsertFile;
    }

    protected File doSearch(File file) throws JAXBException, IOException, ConnectException {
        boolean isShortResourceList = conf.isShortResource();
        // read searchFilter from passed file
        SearchFilter searchFilter = (SearchFilter)unmarshaller.unmarshal(file);

        ShortResourceList list = geostore.searchResources(searchFilter);
        // String context = getRunningContext();
        File outputFile = File.createTempFile("gstsearch_", file.getName(), getTempDir());

        if (list == null || list.getList() == null || list.getList().isEmpty()) {
            return outputFile; // TODO: fixme: the file will be empty!
        }
        shortResourceList = list.getList();
        if (isShortResourceList) {
            JAXB.marshal(list, outputFile);
        } else {

            for (ShortResource shortResource : shortResourceList) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("ID " + shortResource.getId());
                Resource resource = geostore.getResource(shortResource.getId());
                try {
                    String data = geostore.getData(shortResource.getId());
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("Data " + data);
                    StoredData storedData = new StoredData();
                    storedData.setData(data);
                    resource.setData(storedData);
                } catch (Exception e) {
                    LOGGER.warn("Error reading GeoStore resource" + shortResource + ": " + e.getMessage(), e);
                }
                resourceList.add(resource);
            }
            JAXB.marshal(resourceList, outputFile);
        }

        return outputFile;
    }

}
