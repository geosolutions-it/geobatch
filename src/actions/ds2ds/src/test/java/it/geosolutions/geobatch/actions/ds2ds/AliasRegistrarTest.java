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

package it.geosolutions.geobatch.actions.ds2ds;

import static org.junit.Assert.assertTrue;
import it.geosolutions.geobatch.registry.AliasRegistry;

import java.util.Map.Entry;

import org.junit.Test;

public class AliasRegistrarTest {
	private AliasRegistry registry = new AliasRegistry();

	@Test
	public void testDs2DsAliasIsRegistered() {
		new Ds2dsAliasRegistrar(registry);
		
		assertTrue(containsAlias(registry, "Ds2dsConfiguration"));
		assertTrue(Ds2dsConfiguration.class.isAssignableFrom(getAlias(registry, "Ds2dsConfiguration")));
	}

	private Class<?> getAlias(AliasRegistry registry, String alias) {
		for(Entry<String,Class<?>> entry : registry) {
			if(entry.getKey().equals(alias)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private boolean containsAlias(AliasRegistry registry, String alias) {
		for(Entry<String,Class<?>> entry : registry) {
			if(entry.getKey().equals(alias)) {
				return true;
			}
		}
		return false;
	}
}
