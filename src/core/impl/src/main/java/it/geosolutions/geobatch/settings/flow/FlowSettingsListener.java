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

package it.geosolutions.geobatch.settings.flow;

import it.geosolutions.geobatch.settings.GBSettings;
import it.geosolutions.geobatch.settings.GBSettingsDAO;
import it.geosolutions.geobatch.settings.GBSettingsListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class FlowSettingsListener extends GBSettingsListener<FlowSettings> {
	private static Logger LOGGER = LoggerFactory
			.getLogger(FlowSettingsListener.class);

	@Override
	public void onStartup(GBSettingsDAO settingsDAO) {
		FlowSettings settings = null;
		try {
			final GBSettings loaded = settingsDAO.find("FLOW");
			settings = (FlowSettings) loaded;
		} catch (Exception ex) {
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("Could not read FLOW settings.", ex);
		}

		if (settings == null) {
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Using default FLOW settings");
			settings = new FlowSettings();
			settingsDAO.save(settings);
		}

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Initializing FLOW settings");
		applyFlowProperties(settings);
	}

	@Override
	public void beforeSave(FlowSettings settings) {
		// Move along, nothing to see here!
	}

	@Override
	public void afterSave(FlowSettings settings, boolean success) {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("Applying new FLOW settings");
		applyFlowProperties(settings);
	}

	private void applyFlowProperties(FlowSettings FLOW) {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("FLOW is set as following: " + FLOW.toString());
	}

}
