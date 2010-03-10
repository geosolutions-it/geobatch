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



package it.geosolutions.geobatch.imagemosaic;

import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.GeoServerRESTHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ ImageMosaicConfiguratorAction.java $ Revision: 0.1 $ 12/feb/07 12:07:06
 */

public abstract class ImageMosaicConfiguratorAction<T extends EventObject>
	extends BaseAction<T> {
	/**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(ImageMosaicConfiguratorAction.class.toString());

    protected final ImageMosaicActionConfiguration configuration;

    /**
     * Constructs a producer.
	 * The operation name will be the same than the parameter descriptor name.
     *
     * @throws IOException
     */
    public ImageMosaicConfiguratorAction(ImageMosaicActionConfiguration configuration) {
        this.configuration = configuration;
        // //
        //
        // get required parameters
        //
        // //

		if ((configuration.getGeoserverURL() == null) || "".equals(configuration.getGeoserverURL())) {
			LOGGER.log(Level.SEVERE, "GeoServerURL is null.");
			throw new IllegalStateException("GeoServerURL is null.");
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
	 * Configures the styles associated in this class' GeoServerActionConfiguration
	 * to the layer passed as parameter.
	 *
	 * @param layerName the layer to associate to the given styles.
	 * @return true if there were no errors in setting the styles.
	 * @throws java.net.MalformedURLException
	 * @throws java.io.FileNotFoundException
	 */
	protected boolean configureStyles(String layerName)
			throws MalformedURLException, FileNotFoundException {
		return configureStyles(layerName,
								getConfiguration().getDefaultStyle(),
								getConfiguration().getStyles(),
								getConfiguration().getGeoserverURL(),
								getConfiguration().getGeoserverUID(),
								getConfiguration().getGeoserverPWD());
	}

	/**
	 * Set the default style and the associable styles for the layer.
	 *
	 * @param layerName
	 * @param defaultStyle the name of the style to configure as default style to the layer.
	 * @param dataStyles the names of the styles to associate to the layer.
	 * @param gsUrl Geoserver base URL
	 * @param gsUser Geoserver admin username
	 * @param gsPw Geoserver admin password
	 * @return true if there were no errors in setting the styles.
	 * @throws java.net.MalformedURLException
	 * @throws java.io.FileNotFoundException
	 */
	public static boolean configureStyles(String layerName,
			String defaultStyle, List<String> stylesList,
			String gsUrl, String gsUsername, String gsPassword)
		throws MalformedURLException, FileNotFoundException	{

		boolean ret = true;
		URL restUrl = new URL(gsUrl + "/rest/sldservice/updateLayer/" + layerName);

		for (String styleName : stylesList) {

			if(GeoServerRESTHelper.putContent(restUrl,
												"<LayerConfig><Style>" +
													styleName +
												"</Style></LayerConfig>",
												gsUsername, gsPassword)) {

				LOGGER.info("added style " + styleName + " for layer " + layerName);
			} else {
				LOGGER.warning("error adding style " + styleName + " for layer " + layerName);
				ret = false;
			}
		}

		ret &= GeoServerRESTHelper.putContent(restUrl,
												"<LayerConfig><DefaultStyle>" +
													defaultStyle +
												"</DefaultStyle></LayerConfig>",
												gsUsername,
												gsPassword);
		return ret;
	}


    public ImageMosaicActionConfiguration getConfiguration() {
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
