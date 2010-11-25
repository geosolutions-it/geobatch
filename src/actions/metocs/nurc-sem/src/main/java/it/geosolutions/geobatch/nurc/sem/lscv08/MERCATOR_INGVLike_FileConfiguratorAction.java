/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.nurc.sem.lscv08;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.metocs.MetocActionConfiguration;
import it.geosolutions.geobatch.metocs.utils.io.Utilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFConverterUtilities;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.io.FilenameUtils;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

/**
 * 
 * Public class to transform lscv08::MERCATOR Model
 * 
 */
public class MERCATOR_INGVLike_FileConfiguratorAction extends INGVFileConfiguratorAction {

    protected MERCATOR_INGVLike_FileConfiguratorAction(MetocActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

    @Override
    protected File unzipMetocArchive(FileSystemMonitorEvent event, String fileSuffix, File outDir,
            File tempFile) throws IOException {
        return ("zip".equalsIgnoreCase(fileSuffix) || "tar".equalsIgnoreCase(fileSuffix)) ? Utilities
                .decompress("MERCATOR", event.getSource(), tempFile)
                : Utilities.createTodayPrefixedDirectory("MERCATOR", outDir);
    }

    @Override
    protected void createOutputFile(File outDir, String inputFileName) throws IOException {
        outputFile = new File(outDir, cruiseName + "_MERCATOR-Forecast-T" + new Date().getTime()
//        outputFile = new File(outDir, "lscv08_MERCATOR-Forecast-T" + new Date().getTime()
                + FilenameUtils.getBaseName(inputFileName).replaceAll("-", "") + ".nc");
        ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());
    }
    
    @Override
    protected int normalizingTimes(Array timeOriginalData, Dimension timeDim, Date timeOriginDate)
            throws ParseException, NumberFormatException {
        final Calendar cal = new GregorianCalendar(1970, 00, 01);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+0"));

        int nTimes = timeDim.getLength();
        timeData = NetCDFConverterUtilities.getArray(nTimes, timeDataType);
        for (int t = 0; t < nTimes; t++) {
        	double seconds = 0;
        	double timeOrigin = 0;
            if (timeDataType == DataType.FLOAT || timeDataType == DataType.DOUBLE) {
                double originalSecs = timeOriginalData
                        .getDouble(timeOriginalData.getIndex().set(t));
                timeOrigin = originalSecs + (timeOriginDate.getTime() / 1000); 
                seconds =  timeOrigin - (startTime / 1000);
            } else {
                long originalSecs = timeOriginalData.getLong(timeOriginalData.getIndex().set(t));
                timeOrigin = originalSecs + (timeOriginDate.getTime() / 1000); 
                seconds =  timeOrigin - (startTime / 1000);
            }
            timeData.setDouble(timeData.getIndex().set(t), seconds);
            timeOriginDate.setTime((long)(timeOrigin * 1000));
        }

        return 0;
    }

}