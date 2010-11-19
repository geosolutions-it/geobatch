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

import it.geosolutions.geobatch.egeos.types.dest.SarDerivedFeatrueRO;
import it.geosolutions.geobatch.egeos.types.src.SarDerivedDataPackage;
import it.geosolutions.geobatch.egeos.types.src.SarDerivedFeature;
import it.geosolutions.geobatch.egeos.types.src.AbstractListPackage.PackedElement;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class SarDerivedProcessor extends PackageProcessor<SarDerivedDataPackage, SarDerivedFeature, SarDerivedFeatrueRO> {

    private String gmlBaseURI;

    public SarDerivedProcessor(File path) {
        super(path);
    }

    @Override
    protected SarDerivedDataPackage createPackage(File packFile) throws JDOMException, IOException {
        return new SarDerivedDataPackage(packFile);
    }

    @Override
    protected SarDerivedFeature createPackageElement(SarDerivedDataPackage pkg, PackedElement packed,
            File dir) throws JDOMException, IOException {
        return SarDerivedFeature.build(pkg.getEoProductId(), packed.getIdentifier(), new File(dir, packed.getFilename()));
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

    @Override
    protected SarDerivedFeatrueRO transform(SarDerivedFeature sdrF) {
        SarDerivedFeatrueRO sdrFRO = new SarDerivedFeatrueRO();
        sdrFRO.setId(sdrF.getId());
        sdrFRO.setType(sdrF.getType());
        sdrFRO.setTimeStamp(sdrF.getTimeStamp());
        sdrFRO.setEnvelope(sdrF.getEnvelope()[0], sdrF.getEnvelope()[1], sdrF.getEnvelope()[2], sdrF.getEnvelope()[3]);
        sdrFRO.setGmlFileName(sdrF.getFileName());
 
        return sdrFRO;
    }
}
