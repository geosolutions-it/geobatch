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

package it.geosolutions.geobatch.egeos.types.util;


import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class JDOMUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(JDOMUtils.class);

    public static String getString(Element root, XPath xpath) throws JDOMException {
        Element e = (Element)xpath.selectSingleNode(root);
        return e==null?null:e.getText();
    }

    public static Integer getInt(Element root, XPath xpath) throws JDOMException {
        Element e = (Element)xpath.selectSingleNode(root);
        try {
            return new Integer(e.getText());
        } catch (NumberFormatException ex) {
            LOGGER.warn("Can't parse integer", ex);
            return null;
        } catch (NullPointerException ex) {
            LOGGER.warn("Can't parse integer", ex);
            return null;
        }
    }

    public static Double getDouble(Element root, XPath xpath) throws JDOMException {
        Element e = (Element)xpath.selectSingleNode(root);
        try {
            return new Double(e.getText());
        } catch (NumberFormatException ex) {
            LOGGER.warn("Can't parse integer", ex);
            return null;
        } catch (NullPointerException ex) {
            LOGGER.warn("Can't parse integer", ex);
            return null;
        }
    }

}
