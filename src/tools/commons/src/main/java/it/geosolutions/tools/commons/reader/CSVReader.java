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

import it.geosolutions.tools.commons.generics.SetComparator;
import it.geosolutions.tools.commons.writer.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;

/**
 * 
 * @author carlo cancellieri - GeoSolutions
 * 
 */
public abstract class CSVReader {

    /**
     * Reads a CSV into a Set of Arrays. The Set can be a TreeSet(Ordered) or a Set
     * 
     * @param LOGGER the logger (can be null, no log will be performed)
     * @param csv the file to read
     * @param separator the separator
     * @param comparator the {@link SetComparator} (can be null)
     * @param randomRowSize define if rows different in size may be accepted (true) or skipped (false) 
     * @param failsOnError define if process may end on errors
     * @return Set of Object[]. The Set can be a TreeSet(Ordered) or a Set
     * @throws IllegalArgumentException if errors occurs
     * @throws IOException if error occurs reading file
     * @see {@link CSVWriter}
     */
    public static <T, D extends Set<Object[]>> D readCsv(final Logger LOGGER, final File csv,
            final String separator, final SetComparator<T> comparator, final boolean randomRowSize,
            final boolean failsOnError) throws IllegalArgumentException, IOException {

        if (csv == null || separator == null)
            throw new IllegalArgumentException("Unable to run with null csv file or separator");
        if (!csv.isFile())
            throw new IllegalArgumentException("Unable to run: passed csv is not a file: " + csv);

        FileReader fr = null;
        BufferedReader reader = null;
        try {
            fr = new FileReader(csv);
            reader = new BufferedReader(fr);

            // return readLines(LOGGER, csv, separator, keyIndex, comp, failsOnError, reader);
            D ret = null;
            String line = reader.readLine();
            int size = -1;
            if (line != null) {
                final String[] values = line.split(separator);
                size = values.length;
                if (size < 2) {
                    throw new IllegalArgumentException(
                            "Unable to use an ordered map column size is less than 2.");
                    // TODO debug log
                }
                if (comparator != null) {
                    ret = (D) new TreeSet<Object[]>(comparator);
                } else {
                    if (failsOnError) {
                        throw new IllegalArgumentException(
                                "Unable to use an ordered map, some arguments are not valid.");
                        // TODO debug log
                    }

                    if (LOGGER != null && LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Unable to use an ordered map continuing with a LinkedHashSet.");
                    }
                    ret = (D) new LinkedHashSet<Object[]>();

                }

            } else {
                throw new IllegalArgumentException("Unable read lines.");
                // TODO debug log
            }
            while (line != null) {

                if (LOGGER != null && LOGGER.isInfoEnabled()) {
                    LOGGER.info("Reading line: " + line);
                }
                Object[] row = line.split(separator);
                if (row.length == size || randomRowSize){
                    ret.add(row);
                }else {
                    if (failsOnError) {
                        throw new IllegalArgumentException("Wrong row size: " + row.length
                                + " should be: " + size);
                        // TODO debug log
                    }

                    if (LOGGER != null && LOGGER.isWarnEnabled()) {
                        LOGGER.warn("SKIPPING: Wrong row size: " + row.length + " should be: "
                                + size);
                    }
                }

                line = reader.readLine();
            }
            return ret;

        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
            }
            try {
                if (fr != null)
                    fr.close();
            } catch (IOException e) {
            }
        }
    }

    // private static <T> Set<Object[]> readLines(final Logger LOGGER, final File csv,
    // final String separator, Integer keyIndex, final Comparator<Object[]> comp,
    // final boolean failsOnError, final BufferedReader reader)
    // throws IllegalArgumentException, IOException {
    // Set<Object[]> ret = null;
    // String line = reader.readLine();
    // int size = -1;
    // if (line != null) {
    // final String[] values = line.split(separator);
    // size = values.length;
    // if (size < 2) {
    // throw new IllegalArgumentException(
    // "Unable to use an ordered map column size is less than 2.");
    // // TODO debug log
    // }
    // if (comp != null && keyIndex != null && keyIndex > 0 && keyIndex < size) {
    // ret = new TreeSet<Object[]>(comp);
    // } else {
    // if (failsOnError) {
    // throw new IllegalArgumentException(
    // "Unable to use an ordered map, some arguments are not valid.");
    // // TODO debug log
    // }
    //
    // if (LOGGER != null && LOGGER.isWarnEnabled()) {
    // LOGGER.warn("Unable to use an ordered map continuing with a LinkedHashSet.");
    // }
    // ret = new LinkedHashSet<Object[]>();
    //
    // if (keyIndex == null || keyIndex > size) {
    // keyIndex = 0;
    // if (LOGGER != null && LOGGER.isWarnEnabled()) {
    // LOGGER.warn("keyIndex value not valid, using '0'");
    // }
    // }
    //
    // }
    //
    // } else {
    // throw new IllegalArgumentException("Unable read lines.");
    // // TODO debug log
    // }
    // while (line != null) {
    //
    // if (LOGGER != null && LOGGER.isInfoEnabled()) {
    // LOGGER.info("Reading line: " + line);
    // }
    // ret.add(line.split(separator));
    // line = reader.readLine();
    // }
    // return ret;
    // }

}
