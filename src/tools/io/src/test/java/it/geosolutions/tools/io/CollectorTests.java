/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.geosolutions.tools.io.file.Collector;

import java.io.File;
import java.util.List;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.geotools.test.TestData;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 * 
 */
public class CollectorTests {
    final static int FILES_IN_TEST = 6;

    private final static Logger LOGGER = LoggerFactory.getLogger(CollectorTests.class);

    @Test
    public final void testCollect() throws Exception {
        Collector c = new Collector(FileFilterUtils.or(new WildcardFileFilter("*_PCK.xml",
                IOCase.INSENSITIVE), new WildcardFileFilter("*_PRO", IOCase.INSENSITIVE)));

        File location = TestData.file(this, "collector");

        LOGGER.info("Location: " + location.getAbsoluteFile());

        assertNotNull(location);

        assertTrue(location.exists());

        List<File> list = c.collect(location);

        assertNotNull(list);

        LOGGER.info("Number of files..." + list.size());

        for (File f : list) {
            LOGGER.info("FILE: " + f.getAbsolutePath());
        }

        assertEquals("Wrong number of files...", FILES_IN_TEST, list.size());

    }
}
