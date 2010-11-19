/*
 *  Copyright (C) 2007 - 2010 GeoSolutions S.A.S.
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

package it.geosolutions.geobatch.egeos.types.src;

import it.geosolutions.geobatch.metocs.utils.io.METOCSActionsIOUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.jdom.JDOMException;
import org.jdom.Namespace;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * <PRE>
 * @code
 * ...
 * }
 * </PRE>
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class SarDerivedFeature {

    public final static Namespace NS = Namespace.getNamespace("http://www.emsa.europa.eu/csndc");
    public final static Namespace NS_GML = Namespace.getNamespace("gml", "http://www.opengis.net/gml");

    private final String id;
    private final String type;
    private final String fileName;
    private final double l0, l1, u0, u1;
    private final String timeStamp;
    
    public SarDerivedFeature(String id, String type, String fileName, double[] envelope, String timeStamp) {
        this.id = id;
        this.type = type;
        this.fileName = fileName;
        this.timeStamp = timeStamp;
        this.l0 = envelope[0];
        this.l1 = envelope[1];
        this.u0 = envelope[2];
        this.u1 = envelope[3];
    }

    public static SarDerivedFeature build(String id, String sarDerivedFeature, File srcFile) throws JDOMException, IOException {
        NetcdfFile ncGridFile = NetcdfFile.open(srcFile.getAbsolutePath());
        
        // input dimensions
        final Dimension ra_size = ncGridFile.findDimension("ra_size");

        final Dimension az_size = ncGridFile.findDimension("az_size");

        // final Dimension n_partitions = ncGridFile.findDimension("n_partitions");

        // input VARIABLES
        final Variable lonOriginalVar = ncGridFile.findVariable("longitude");

        final Variable latOriginalVar = ncGridFile.findVariable("latitude");

        final Array lonOriginalData = lonOriginalVar.read();
        final Array latOriginalData = latOriginalVar.read();

        // building envelope
        double[] bbox = METOCSActionsIOUtils.computeExtrema(latOriginalData, lonOriginalData, az_size, ra_size);

        Attribute referenceTime = ncGridFile.findGlobalAttributeIgnoreCase("SOURCE_ACQUISITION_UTC_TIME");
        // e.g. 20100902211637.870628
        final SimpleDateFormat toSdf = new SimpleDateFormat("yyyyMMddHHmmss");
        toSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final SimpleDateFormat toTimeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        toTimeStamp.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date timeOriginDate;
        try {
            timeOriginDate = toSdf
                    .parse(referenceTime.getStringValue().trim().toLowerCase());
        } catch (ParseException e) {
            throw new IOException(e.getLocalizedMessage());
        } finally {
            ncGridFile.close();
        }

        return new SarDerivedFeature(id, sarDerivedFeature, srcFile.getAbsolutePath(), bbox, toTimeStamp.format(timeOriginDate));
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the timeStamp
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    public double[] getEnvelope() {
        if (l0 == Double.NaN || l1 == Double.NaN || u0 == Double.NaN || u1 == Double.NaN)
            return null;
        
        return new double[] {l0, l1, u0, u1};
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
}
