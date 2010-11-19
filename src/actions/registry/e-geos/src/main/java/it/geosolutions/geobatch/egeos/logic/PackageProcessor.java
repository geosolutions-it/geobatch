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

import it.geosolutions.geobatch.egeos.types.src.AbstractListPackage;
import it.geosolutions.geobatch.egeos.types.src.AbstractListPackage.PackedElement;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jdom.JDOMException;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public abstract class PackageProcessor<P extends AbstractListPackage, PE, ROE> {

    private File dir;
    private P pkg = null;
    private List<PE> packedList = new ArrayList<PE>();

    public PackageProcessor(File path) {
        this.dir = path;
    }

    public P parsePackage() {
        try {
            File[] pckList = dir.listFiles((FilenameFilter) new SuffixFileFilter("PCK.xml"));
            if (pckList == null) {
                throw new RuntimeException("Can't read dir " + dir);
            } else if (pckList.length != 1) {
                throw new RuntimeException("Expecting one package file in " + dir + ", but found " + pckList.length);
            }
            File packFile = pckList[0];
            pkg = createPackage(packFile);

            for (PackedElement packedElement : pkg.getList()) {
                //File osnFile = new File(dir, packedElement.getFilename());
                PE osn = createPackageElement(pkg, packedElement, dir);
                packedList.add(osn);
            }

            return pkg;
        } catch (JDOMException ex) {
            throw new RuntimeException("Error processing OilSpillPackage", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Error processing OilSpillPackage", ex);
        }
    }

    public P getPackage() {
        return pkg;
    }

    public List<PE> getList() {
        return packedList;
    }

    public int size() {
        return packedList.size();
    }

    public ROE getRegistryObject(int i) {
        PE osn = packedList.get(i);
        return transform(osn);
    }

    protected abstract P createPackage(File packFile) throws JDOMException, IOException;
    protected abstract PE createPackageElement(P pkg, PackedElement packed, File dir) throws JDOMException, IOException;
    protected abstract ROE transform(PE e);
}
