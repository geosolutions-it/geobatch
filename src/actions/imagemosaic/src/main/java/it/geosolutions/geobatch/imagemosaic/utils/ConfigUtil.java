/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2013 GeoSolutions S.A.S.
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

package it.geosolutions.geobatch.imagemosaic.utils;

import it.geosolutions.geobatch.imagemosaic.ImageMosaicConfiguration;
import it.geosolutions.geobatch.imagemosaic.config.DomainAttribute;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class ConfigUtil {
    protected final static Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

    public static DomainAttribute getAttribute(ImageMosaicConfiguration cfg, String dimName) {
        if(cfg.getDomainAttributes() == null)
            return null;

        for (DomainAttribute attr : cfg.getDomainAttributes()) {
            if(attr.getDimensionName().equals(dimName)) {
                return attr;
            }
        }
        return null;
    }

    public static DomainAttribute getTimeAttribute(ImageMosaicConfiguration cfg) {
        return getAttribute(cfg, DomainAttribute.DIM_TIME);
    }

    public static DomainAttribute getElevationAttribute(ImageMosaicConfiguration cfg) {
        return getAttribute(cfg, DomainAttribute.DIM_ELEV);
    }

    public static List<String> getCustomDimensionNames(ImageMosaicConfiguration cfg) {
        List<String> ret = new ArrayList<String>();
        for (DomainAttribute attr : cfg.getDomainAttributes()) {
            String dimName = attr.getDimensionName();
            if( ! DomainAttribute.DIM_TIME.equals(dimName) &&
                ! DomainAttribute.DIM_ELEV.equals(dimName)) {
                ret.add(dimName);
            }
        }
        return ret;
    }

    public static List<DomainAttribute> getCustomDimensions(ImageMosaicConfiguration cfg) {
        List<DomainAttribute> ret = new ArrayList<DomainAttribute>();
        for (DomainAttribute attr : cfg.getDomainAttributes()) {
            String dimName = attr.getDimensionName();
            if( ! DomainAttribute.DIM_TIME.equals(dimName) &&
                ! DomainAttribute.DIM_ELEV.equals(dimName)) {
                ret.add(attr);
            }
        }
        return ret;
    }

    public static boolean hasDimension(ImageMosaicConfiguration cfg, String dimName) {
        for (DomainAttribute attr : cfg.getDomainAttributes()) {
            if(dimName.equals(attr.getDimensionName()))
                return true;
        }
        return false;
    }

    /**
     * DO NOT apply this method before the imagemosaiccommand/imagemosaicconfig merging.
     * 
     */
    public static void sanitize(ImageMosaicConfiguration configuration) {

        // sanitize attrs
        for (DomainAttribute attr : configuration.getDomainAttributes()) {
            if(attr.getDimensionName() == null)
                throw new NullPointerException("Null dimension name: " + attr);

            if(attr.getAttribName() == null) {
                LOGGER.warn("Missing attribute name for dimension " + attr.getDimensionName() + " -- Will default to dimension name.");
                attr.setAttribName(attr.getDimensionName());
            }

            if(attr.getType() == null) {
                DomainAttribute.TYPE type = DomainAttribute.TYPE.STRING;

                // some sane defaults
                if(attr.getDimensionName().equals(DomainAttribute.DIM_TIME)) {
                    type = DomainAttribute.TYPE.DATE;
                }
                if(attr.getDimensionName().equals(DomainAttribute.DIM_ELEV)) {
                    type = DomainAttribute.TYPE.DOUBLE;
                }

                LOGGER.error("Missing type for dimension " + attr.getDimensionName() + " -- Will default to " + type + " type.");
                attr.setType(type);
            }
        }
    }

}
