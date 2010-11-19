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

import java.io.File;
import java.io.IOException;
import org.jdom.JDOMException;

/**
 * <PRE>{@code
<csn:dataPackage
 xmlns:csn="http://www.emsa.europa.eu/csndc" xmlns:gml="http://www.opengis.net/gml" xmlns:ows="http://www.opengis.net/ows/1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://www.emsa.europa.eu/csndc ../XSD/csndc_pkg.xsd">
  <csn:packageInfo>
     <csn:packageId>219_RS1_20100827052208.047.SCN8.NEAR.1.00000_DER</csn:packageId>
     <csn:packageType>SAR_DERIVED</csn:packageType>
     <csn:operationType>TEST</csn:operationType>
     <csn:dataPackageDescription>SAR derived package</csn:dataPackageDescription>
  </csn:packageInfo>
     <csn:eoProduct>
        <csn:identifier>219_RS1_20100827052208.047.SCN8.NEAR.1.00000</csn:identifier>
     </csn:eoProduct>
   ...
  <csn:sarDerivedData>
     <csn:sarDerivedDataReference>
        <csn:sarDerivedFeature>WAVE</csn:sarDerivedFeature>
        <csn:fileName>SAR_wave.nc</csn:fileName>
     </csn:sarDerivedDataReference>
     <csn:sarDerivedDataReference>
        <csn:sarDerivedFeature>WIND</csn:sarDerivedFeature>
        <csn:fileName>SAR_wnf.nc</csn:fileName>
     </csn:sarDerivedDataReference>
  </csn:sarDerivedData>
</csn:dataPackage>
 * }</PRE>
 * @author ETj (etj at geo-solutions.it)
 */

public class SarDerivedDataPackage extends AbstractListPackage {

    public static enum PackageType {
        SAR_DERIVED;
    }

    public SarDerivedDataPackage(File srcFile) throws JDOMException, IOException {
        super(srcFile);
    }

    @Override
    protected String getCollectionName() {
        return "sarDerivedData";
    }

    @Override
    protected String getReferenceName() {
        return "sarDerivedDataReference";
    }

    /* (non-Javadoc)
     * @see it.geosolutions.geobatch.egeos.types.src.AbstractListPackage#getIdentifier()
     */
    @Override
    protected String getIdentifier() {
        return "sarDerivedFeature";
    }

    public PackageType getType() {
        return PackageType.valueOf(getPackageType());
    }

}
