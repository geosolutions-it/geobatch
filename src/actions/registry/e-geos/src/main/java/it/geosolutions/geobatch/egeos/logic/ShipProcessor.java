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

import it.geosolutions.geobatch.egeos.types.dest.VesselRO;
import it.geosolutions.geobatch.egeos.types.src.ShipDetection;
import it.geosolutions.geobatch.egeos.types.src.ShipDetectionPackage;
import it.geosolutions.geobatch.egeos.types.src.AbstractListPackage.PackedElement;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class ShipProcessor extends PackageProcessor<ShipDetectionPackage, ShipDetection, VesselRO> {

    private String gmlBaseURI;

    public ShipProcessor(File path) {
        super(path);
    }

    @Override
    protected ShipDetectionPackage createPackage(File packFile) throws JDOMException, IOException {
        return new ShipDetectionPackage(packFile);
    }

    @Override
    protected ShipDetection createPackageElement(ShipDetectionPackage pkg, PackedElement packed,
            File dir) throws JDOMException, IOException {
        return ShipDetection.build(new File(dir, packed.getFilename()));
    }

    @Override
    protected VesselRO transform(ShipDetection e) {
        return ship2vessel(e);
    }

    public VesselRO ship2vessel(ShipDetection ship) {
        VesselRO ro = new VesselRO();
        ro.setId(ship.getId());
        ro.setTimeStamp(ship.getTimestamp());
        ro.setEnvelope(ship.getX0() - 0.00001d, ship.getX1() - 0.00001d, ship.getX0() + 0.00001d,
                ship.getX1() + 0.00001d);
        ro.setGmlFileName(ship.getGmlFileName());
        ro.setGmlBaseURI(gmlBaseURI);
        return ro;
    }

    /**
     * @param gmlBaseURI
     *            the gmlBaseURI to set
     */
    public void setGmlBaseURI(String gmlBaseURI) {
        this.gmlBaseURI = gmlBaseURI;
    }

    /**
     * @return the gmlBaseURI
     */
    public String getGmlBaseURI() {
        return gmlBaseURI;
    }
}
