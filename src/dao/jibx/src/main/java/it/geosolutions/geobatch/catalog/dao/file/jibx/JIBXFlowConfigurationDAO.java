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



package it.geosolutions.geobatch.catalog.dao.file.jibx;

import it.geosolutions.geobatch.catalog.dao.FlowManagerConfigurationDAO;
import it.geosolutions.geobatch.configuration.flow.FlowConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedCatalogConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

public class JIBXFlowConfigurationDAO
		extends JIBXDAO<FlowConfiguration>
		implements FlowManagerConfigurationDAO {

    public JIBXFlowConfigurationDAO(String directory) {
        super(directory);
    }

    public FileBasedFlowConfiguration find(FlowConfiguration exampleInstance, boolean lock) {
        return find(exampleInstance.getId(), lock);
    }

    public FileBasedFlowConfiguration find(String id, boolean lock) {
        try {
            final File entityfile = new File(getBaseDirectory(), id + ".xml");
            if (entityfile.exists() && entityfile.canRead()) {
                IBindingFactory bfact = BindingDirectory.getFactory(FileBasedCatalogConfiguration.class);
                IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
                FileBasedFlowConfiguration obj = (FileBasedFlowConfiguration) uctx.unmarshalDocument(new BufferedInputStream(new FileInputStream(entityfile)), null);
                return obj;

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}
