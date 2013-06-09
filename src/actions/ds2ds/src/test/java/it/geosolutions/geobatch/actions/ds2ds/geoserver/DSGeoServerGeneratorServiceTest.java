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
package it.geosolutions.geobatch.actions.ds2ds.geoserver;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DSGeoServerGeneratorServiceTest {

	private static DSGeoServerConfiguration CONFIGURATION = new DSGeoServerConfiguration("id", "name", "description");
	private DSGeoServerGeneratorService generatorService = new DSGeoServerGeneratorService("Ds2dsGeoServerGeneratorService");

	@Test
	public void testConfigurationIsGenerated() {		
		assertTrue(generatorService.canCreateAction(
				CONFIGURATION));
		assertNotNull(generatorService.createAction(CONFIGURATION));
		assertTrue(generatorService.createAction(CONFIGURATION) instanceof DSGeoServerAction);
	}
}
