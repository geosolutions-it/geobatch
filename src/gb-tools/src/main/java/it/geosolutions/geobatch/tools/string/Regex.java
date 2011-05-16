/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.tools.string;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class Regex {
    
    /**
     * resolve the 'template' String looking for keys found into the passed map substituting their
     * values into the template. The keys into the template should be into the form: ${KEY}<br>
     * 
     * NOTE: only [A-z] characters are permitted for keys.<br>
     * 
     * F.E.:<br>
     * template -> ${ROOT}/${PATH}/${TO}/${FILE} <br>
     * map: <br>
     * ROOT, /opt<br>
     * PATH, program<br>
     * AT, url<br>
     * FILE, file.txt<br>
     * returns:<br>
     * The string: /opt/program//file.txt<br>
     * modify map removing all but the not found keys:<br>
     * AT, url<br>
     * 
     * @param template
     * @param map
     * @return modify the Map<String,String> leaving only not found keys and return the resolved template string or
     *         null if any arguments are null with no changes to the map
     */
    public static final String resolveTemplate(final String template, final Map<String, String> map) {
        if (template == null || map == null) {
            return null;
        }

        String buffer = new String(template);
        synchronized (map) {
            final Set<Entry<String, String>> set = map.entrySet();
            final Iterator<Entry<String, String>> it = set.iterator();
            while (it.hasNext()) {
                final Entry<String, String> entry = it.next();
                final StringBuilder key = new StringBuilder("\\$\\{").append(entry.getKey())
                        .append("\\}");
                final Pattern pattern=Pattern.compile(key.toString());
                final Matcher matcher=pattern.matcher(buffer);
                
                // check if the buffer contains the KEY
                if (matcher.find()) {
                    buffer = matcher.replaceAll(entry.getValue());
                    it.remove();
                }
            }
            // replace all the ${KEYs} found into the template with an empty string
            buffer = buffer.replaceAll("\\$\\{[A-z]+\\}", "");
        }

        return buffer;
    }

}
