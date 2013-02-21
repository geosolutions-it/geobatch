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
package it.geosolutions.tools.commons.writer;

import it.geosolutions.tools.commons.reader.CSVReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;

/**
 * 
 * @author carlo cancellieri - GeoSolutions
 *
 */
public abstract class CSVWriter {

    /**
     * Writes a CSV from a Set of Arrays. The map can be a TreeSet(Ordered) or a Set
     * 
     * @param LOGGER the logger (can be null, no log will be performed)
     * @param data the set of Array to write
     * @param csv the file to write
     * @param separator the separator to use
     * @param failsOnError define if process may fail on errors
     * @throws IllegalArgumentException if errors occurs
     * @throws IOException if error occurs reading file
     * @see {@link CSVReader}
     */
    public static <T, D extends Set<Object[]>> void writeCsv(
            final Logger LOGGER, final D data, final File csv, final String separator,
            final boolean failsOnError) throws IllegalArgumentException, IOException {

        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(csv);
            writer = new BufferedWriter(fw);

            final Iterator<Object[]> it = data.iterator();
            while (it.hasNext()) {
                Object[] entry = it.next();
                writeLine(LOGGER, entry, separator, writer);
            }

        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
            }
            try {
                if (fw != null)
                    fw.close();
            } catch (IOException e) {
            }
        }
    }

    private static <T> void writeLine(final Logger LOGGER, Object[] rowData,
            String separator, BufferedWriter writer) throws IOException {
        if (LOGGER != null && LOGGER.isInfoEnabled())
            LOGGER.info("writing values : " + Arrays.toString(rowData));
        if (rowData.length == 0)
            return;
        writer.write(rowData[0] != null ? rowData[0].toString() : "");
        for (int i = 1; i < rowData.length; ++i) {
            writer.write(separator);
            writer.write(rowData[i] != null ? rowData[i].toString() : "");
        }
        writer.write("\n");
    }

}
