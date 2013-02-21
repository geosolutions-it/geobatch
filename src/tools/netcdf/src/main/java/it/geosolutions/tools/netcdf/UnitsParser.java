/*
 *  Copyright (C) 2007 - 2012 GeoSolutions S.A.S.
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
package it.geosolutions.tools.netcdf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse a units string.
 *
 * @see #parse(java.lang.String)
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class UnitsParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(UnitsParser.class);

    private long secondsMultiplier;
    private Units units;
    private Date date;

    static final private SimpleDateFormat INPUTFORMAT = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss" ,Locale.ROOT);
    static{
        INPUTFORMAT.setLenient(true);
        INPUTFORMAT.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
    }
    
    static final private SimpleDateFormat TOSTRINGFORMAT = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss" ,Locale.ROOT);
    static {
        TOSTRINGFORMAT.setLenient(true);
        TOSTRINGFORMAT.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
    }


    /** parsable units so far */
    public enum Units {
        seconds(1),
        minutes(60),
        hours(60*60),
        days(60*60*24);

        private long multiplier;

        private Units(long mult) {
            this.multiplier = mult;
        }

        /**
         * The units multiplier to transform this unit into seconds:
         * e.g. 1 for seconds, 60 for minuts, ...
         * @return
         */
        public long getMultiplier() {
            return multiplier;
        }
    }

    /**
     * Parse a units string.
     * <br/>Format is: <pre>
     * "TIMEUNIT' since 'DATE",
     * </pre>
     * where DATE is in the format<pre>
     *  DATE = year':'month':'day' 'hour':'min':'sec
     * </pre>
     * year, month, hour, min, sec may be expressed with 1 or 2 digits.
     *
     *
     * @return true if parsing was successful, false otherwise
     */
    public boolean parse(String input) {
        String[] split = input.split(" ");
        
        if(split.length != 4) {
            if(LOGGER.isInfoEnabled())
                LOGGER.info("Could not parse '"+input+"': 4 tokens expected");
            return false;
        }

        String u = split[0];
        try {
            units = Units.valueOf(u.toLowerCase());
            secondsMultiplier = units.getMultiplier();
        } catch (Exception e) {
            if(LOGGER.isInfoEnabled())
                LOGGER.info("Unknown time unit '"+u+"': " + e.getMessage());
            return false;
        }

        if(! "since".equals(split[1])) {
            if(LOGGER.isInfoEnabled())
                LOGGER.info("Missing 'since' keyword");
            return false;
        }


        String datetime = split[2] + " " + split[3];
        try {
            date = INPUTFORMAT.parse(datetime);
        } catch (ParseException ex) {
            if(LOGGER.isInfoEnabled())
                LOGGER.info("Can't parse datetime '"+datetime+"': " + ex.getMessage());
            return false;
        }

        return true;
    }



    /**
     * @return the time multiplier (1 for seconds, 60 for minutes, ...
     */
    public long getSecondsMultiplier() {
        return secondsMultiplier;
    }

    /**
     * return the base date
     */
    public Date getDate() {
        return date;
    }

    public Units getUnits() {
        return units;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "["+units+ " since " + TOSTRINGFORMAT.format(date) +", seconds x "+secondsMultiplier+"]";
    }

}
