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
package it.geosolutions.tools.commons.reader;

import it.geosolutions.tools.commons.generics.IntegerCaster;
import it.geosolutions.tools.commons.generics.SetComparator;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

import junit.framework.Assert;

import org.geotools.test.TestData;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVReaderTest {
    Logger LOGGER = LoggerFactory.getLogger(CSVReaderTest.class);

    File csv, csvOut;

    @Before
    public void setUp() throws Exception {
        csv = TestData.file(CSVReader.class, "test.csv");
    }
    @Test
    public void toMapTest() throws IllegalArgumentException, IOException {
        TreeSet<Object[]> data = CSVReader.readCsv(LOGGER, csv, ",", new SetComparator<Integer>(new IntegerCaster(),2,false), false, false);

        
        Object[] first = data.first();// matches 23
        Assert.assertNotNull(first);
        Assert.assertNotNull(first[2].equals("23"));
        Assert.assertTrue(first[3].equals("23_airtmp_mn.tif"));

        Object[] last = data.last();
        Assert.assertNotNull(last);
        Assert.assertNotNull(last[2].equals("38213"));
        Assert.assertTrue(last[4].equals("SOLAW:lw_scar"));

    }

}
