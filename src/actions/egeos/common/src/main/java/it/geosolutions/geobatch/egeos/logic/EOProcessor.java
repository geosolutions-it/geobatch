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

package it.geosolutions.geobatch.egeos.logic;

import it.geosolutions.geobatch.egeos.types.dest.PlatformRO;
import it.geosolutions.geobatch.egeos.types.dest.SARProductRO;
import it.geosolutions.geobatch.egeos.types.src.EarthObservation;
import it.geosolutions.geobatch.egeos.types.src.EarthObservationPackage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jdom.JDOMException;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class EOProcessor {

    private File dir;
    private EarthObservationPackage pkg = null;
    private EarthObservation eo;

    public EOProcessor(File path) {
        this.dir = path;
    }

    public EarthObservationPackage parsePackage() {
        try {
            File[] pckList = dir.listFiles((FilenameFilter) new SuffixFileFilter("PCK.xml"));
            if (pckList == null) {
                throw new RuntimeException("Can't read dir " + dir);
            } else if (pckList.length != 1) {
                throw new RuntimeException("Expecting one package file in " + dir + ", but found " + pckList.length);
            }
            File packFile = pckList[0];
            pkg = new EarthObservationPackage(packFile);
            
            File eopFile = new File(dir, pkg.getEoFileName());
            eo = EarthObservation.build(eopFile);

            return pkg;
        } catch (JDOMException ex) {
            throw new RuntimeException("Error processing OilSpillPackage", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Error processing OilSpillPackage", ex);
        }
    }

    public EarthObservationPackage getPackage() {
        return pkg;
    }

    public EarthObservation getEO() {
        return eo;
    }


    public SARProductRO getSARProduct() {
        return eo2sar(eo);
    }

    public PlatformRO getPlatform() {
        return eo2platform(eo);
    }

    public static PlatformRO eo2platform(EarthObservation eo) {
        String id = createPlatformId(eo);
        PlatformRO ret = new PlatformRO();
        ret.setId(id);
        ret.setInstrShorName(eo.getEquipInstrumentName());
        ret.setSensorType(eo.getEquipSensorType());
        ret.setSensorResolution(eo.getEquipSensorResolution());

        return ret;
    }

    public String getPlatformId() {
        return createPlatformId(eo);
    }

    protected static String createPlatformId(EarthObservation eo) {
        return onlyalphanum(eo.getEquipPlatformName()) + "_"
                + onlyalphanum(eo.getEquipInstrumentName()) + "_"
                + onlyalphanum(eo.getEquipSensorType());
    }

    private static String onlyalphanum(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if(Character.isLetterOrDigit(c) || c=='-')
                sb.append(c);
        }
        return sb.toString();
    }

    public static SARProductRO eo2sar(EarthObservation eo) {
        SARProductRO sar = new SARProductRO();
        sar.setId(eo.getId());

        sar.setAcquisitionStation(eo.getAcquisitionStation());
        sar.setBeginTimePosition(eo.getTsBegin());
        sar.setEndTimePosition(eo.getTsEnd());
        sar.setFootprintListPos(eo.getFootprintPosList());
//        sar.setGmlBaseURI(eo.get); // TODO
//        sar.setGmlFileName(eo.get); // TODO
        sar.setOrbitDirection(eo.getOrbitDir());
        sar.setOrbitNumber(eo.getOrbitNum());
        sar.setPlatformId(createPlatformId(eo));
//        sar.set(eo.get);

        return sar;
    }

//    protected abstract ROE transform(PE e);
}
