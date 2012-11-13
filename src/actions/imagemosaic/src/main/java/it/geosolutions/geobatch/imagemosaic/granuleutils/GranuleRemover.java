/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.imagemosaic.granuleutils;

import it.geosolutions.geobatch.imagemosaic.ImageMosaicCommand;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TimeZone;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Enrich an ImageMosaicCommand with del entries.
 * <p/>
 * Mandatory params:<ul>
 * <li>{@link #setDaysAgo(java.lang.Integer) setDaysAgo(Integer)}</li>
 * </ul>
 *
 * You can customize some parameter:
 * <ul>
 * <li>{@link #setTypeName(java.lang.String) setTypeName(String)}</li>
 * <li>{@link #setDatastoreFileName(java.lang.String) setDatastoreFileName(String)}</li>
 * <li>{@link #setTimeAttributeName(java.lang.String) setTimeAttributeName(String)}</li>
 * <li>{@link #setLocationAttributeName(java.lang.String) setLocationAttributeName(String)}</li>
 * <li>{@link #setBaseDate(java.util.Calendar) setBaseDate(Calendar)}</li>
 * </ul>
 *
 * @author ETj
 */
public class GranuleRemover {
	protected final static Logger LOGGER = LoggerFactory.getLogger(GranuleRemover.class);

    protected GranuleSelector selector = new GranuleSelector();

    private String   timeAttributeName = "ingestion";
    private Integer  daysAgo = null;
    private Calendar baseDate = null;

    public GranuleRemover() {
    }

    /**
     * Set the number of days that should be past before an entry is added to the IMC deletion list.
     * <br/>
     * Delta is computed between the current date and the date read from the
     * {@link #setTimeAttributeName(java.lang.String) granule time attribute}.
     *
     * <p/>This is a <b>mandatory</b> parameter.
     */
    public void setDaysAgo(Integer days) {
        this.daysAgo = days;
    }

    /**
     * Set the base date where the daysAgo delta will be applied to.
     * <br/>
     * If not set, the date of the call to {@link #enrich(it.geosolutions.geobatch.imagemosaic.ImageMosaicCommand) enrich(...)} will be used.
     */
    public void setBaseDate(Calendar baseDate) {
        this.baseDate = baseDate;
    }

    /**
     * Set the feature type name.
     * 
     * <p/>If not set the base name of the mosaicDir will be used.
     */
    public void setTypeName(String typeName) {
        selector.setTypeName(typeName);
    }

    /**
     * Customize the datastore file name.
     * <br/>Default is "<code>datastore.properties</code>".
     */
    public void setDatastoreFileName(String datastoreFileName) {
        selector.setDatastoreFileName(datastoreFileName);
    }

    /**
     * Customize the locationAttributeName file name.
     * <br/>Default is "<code>location</code>".
     */
    public void setLocationAttributeName(String locationAttributeName) {
        selector.setLocationAttributeName(locationAttributeName);
    }

    /**
     * Customize the timeAttributeName.
     * <br/>Default is "<code>ingestion</code>".
     */
    public void setTimeAttributeName(String timeAttributeName) {
        this.timeAttributeName = timeAttributeName;
    }


    public void enrich(ImageMosaicCommand imc) throws IOException, IllegalStateException {
        if(timeAttributeName == null)
            throw new IllegalStateException("timeAttributeName is not set");
        if(daysAgo == null)
            throw new IllegalStateException("daysAgo is not set");

        selector.setFilter(createBeforeNdaysAgoFilter(daysAgo));

        Set<File> filesToBeRemoved = selector.getFiles(imc.getBaseDir());

        if(filesToBeRemoved != null) {
            if(imc.getDelFiles() == null) {
                imc.setDelFiles(new ArrayList(filesToBeRemoved));
            }
            else {
                imc.getDelFiles().addAll(filesToBeRemoved);
            }
        }
    }

    protected Filter createBeforeNdaysAgoFilter(int daysAgo) {

        final SimpleDateFormat utcFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        final Calendar cal;
        if(baseDate == null) {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("No base date set; current date will be used");
            cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        } else {
            cal = (Calendar)baseDate.clone();
        }

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Base date set to " + utcFormatter.format(cal.getTime()) + " -- delta is " + daysAgo);
        }

        cal.add(Calendar.DAY_OF_MONTH, -daysAgo);
        Date date = cal.getTime();
        String utc = utcFormatter.format(date);

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Filter date is   " + utc);
        }

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

        Filter out =
                ff.lessOrEqual(
                    ff.property(timeAttributeName),
//                    ff.literal(utc));
                    ff.literal(cal));

        return out;
    }
}