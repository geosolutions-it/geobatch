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
package it.geosolutions.tools.dyntokens.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class StringDynToken extends DynToken {

    private final static Logger LOGGER = LoggerFactory.getLogger(StringDynToken.class);

    private String compose;

    public String getCompose() {
        return compose;
    }

    public void setCompose(String compose) {
        this.compose = compose;
    }

    @Override
    String resolve(String resolvedBase, Map<String, String> resolvedTokens) {

        Map<String, String> clonedTokens = new HashMap<String, String>(resolvedTokens);
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(resolvedBase);
        if(m.matches()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                String group = m.group(i);
                clonedTokens.put(Integer.toString(i), group);

                if(LOGGER.isDebugEnabled())
                    LOGGER.debug("Adding group " + i + ": '"+group+"'");
            }
        } else {
            if(LOGGER.isWarnEnabled())
                LOGGER.warn("RegEx '"+regex+"' not matching '"+resolvedBase+"'");
        }

        return expandTokens(compose, clonedTokens);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "["
                + "name=" + name
                + ", base=" + base
                + ", regex=" + regex
                + ", compose=" + compose
                + ']';
    }

}
