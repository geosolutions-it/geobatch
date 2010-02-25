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
package it.geosolutions.geobatch.metocs;

import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.IOException;
import java.util.EventObject;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ MetocConfigurationAction.java $ Revision: 0.1 $ 23/oct/09 17:14:23
 */

public abstract class MetocConfigurationAction<T extends EventObject>
	extends BaseAction<T> {
	/**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(MetocConfigurationAction.class.toString());

    protected final MetocActionConfiguration configuration;

    /**
     * Constructs a producer.
	 * The operation name will be the same than the parameter descriptor name.
     *
     * @throws IOException
     */
    public MetocConfigurationAction(MetocActionConfiguration configuration) {
        this.configuration = configuration;
        // //
        //
        // get required parameters
        //
        // //

		if ((configuration.getMetocDictionaryPath() == null) || "".equals(configuration.getMetocHarvesterXMLTemplatePath())) {
			LOGGER.log(Level.SEVERE, "MetcoDictionaryPath || MetocHarvesterXMLTemplatePath is null.");
			throw new IllegalStateException("MetcoDictionaryPath || MetocHarvesterXMLTemplatePath is null.");
		}

    }

    /**
     * @param queryParams
     * @return
     */
    protected static String getQueryString(Map<String, String> queryParams) {
        StringBuilder queryString = new StringBuilder();

        if (queryParams != null)
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
				if(queryString.length() > 0)
					queryString.append("&");
				queryString.append(entry.getKey()).append("=").append(entry.getValue());
            }

        return queryString.toString();
    }

    /**
     * 
     * @return
     */
    public MetocActionConfiguration getConfiguration() {
        return configuration;
    }

	@Override
	public String toString() {
		return getClass().getSimpleName()
				+ "["
				+ "cfg:"+getConfiguration()
				+ "]";
	}

}
