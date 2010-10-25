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

import it.geosolutions.geobatch.egeos.types.dest.OilSpillRO;
import it.geosolutions.geobatch.egeos.types.src.AbstractListPackage.PackedElement;
import it.geosolutions.geobatch.egeos.types.src.OilSpill;
import it.geosolutions.geobatch.egeos.types.src.OilSpillPackage;
import java.io.File;
import java.io.IOException;
import org.jdom.JDOMException;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class OilSpillProcessor extends PackageProcessor<OilSpillPackage, OilSpill, OilSpillRO>{

    private String baseGMLURL = null;

    public OilSpillProcessor(File path) {
        super(path);
    }

    public void setBaseGMLURL(String baseGMLURL) {
        this.baseGMLURL = baseGMLURL;
    }

    @Override
    protected OilSpillPackage createPackage(File packFile) throws JDOMException, IOException {
        return new OilSpillPackage(packFile);
    }

    @Override
    protected OilSpill createPackageElement(OilSpillPackage pkg, PackedElement packed, File dir) throws JDOMException, IOException {
        return OilSpill.build(packed.getIdentifier(), new File(dir, packed.getFilename()));
    }

    @Override
    protected OilSpillRO transform(OilSpill e) {
        return osn2ro(e);
    }

    public static OilSpillRO osn2ro(OilSpill osn) {
        OilSpillRO ro = new OilSpillRO();
        ro.setId(osn.getId());
        ro.setTimeStamp(osn.getTimestamp());
        if(osn.isEnvelopeSet()) {
            ro.setEnvelope(osn.getX0(), osn.getY0(), osn.getX1(), osn.getY1());
        }

        ro.setImageFileName(osn.getRefImageFileName());
        // ro.setImageBaseURI(null); // TODO

//        ro.setGmlBaseURI(null);  // TODO
//        ro.setGmlFileName(null);  // TODO
        return ro;
    }

}
