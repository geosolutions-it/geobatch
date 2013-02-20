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
package it.geosolutions.tools.dyntokens;

import it.geosolutions.tools.dyntokens.model.DynToken;
import it.geosolutions.tools.dyntokens.model.DynTokenList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class DynTokenResolver {

    private final static Logger LOGGER = LoggerFactory.getLogger(DynTokenResolver.class);

    private DynTokenList tokenList;
    
    private Map<String, String> baseTokens = new HashMap<String, String>();

    private Map<String, String> resolvedTokens = new HashMap<String, String>();

    public DynTokenResolver(DynTokenList tokenList) {
        this.tokenList = tokenList;
    }

    public void setBaseToken(String name, String value) {
        baseTokens.put(name, value);
    }

    public void resolve() {
        resolvedTokens = new HashMap<String, String>(baseTokens);

        if(tokenList == null) {
            if(LOGGER.isInfoEnabled())
                LOGGER.info("No dynamic tokens configured");
            return;
        }

        for (DynToken token : tokenList) {
            String resolved = token.resolve(resolvedTokens);
            if(resolved == null) {
                if(LOGGER.isWarnEnabled())
                    LOGGER.warn("Could not resolve token '"+token.getName()+"'");
            } else {
                resolvedTokens.put(token.getName(), resolved);
                if(LOGGER.isInfoEnabled())
                    LOGGER.info("Token '"+token.getName()+"' resolved into '"+resolved+"'");
            }
        }
    }

    public Map<String, String> getResolvedTokens() {
        return resolvedTokens;
    }

}
