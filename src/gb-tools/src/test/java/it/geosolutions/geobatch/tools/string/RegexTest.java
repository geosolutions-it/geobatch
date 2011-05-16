/*
 *  Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
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

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class RegexTest extends TestCase {
    private final static Logger LOGGER = LoggerFactory.getLogger(RegexTest.class);
    
    
    public RegexTest() {
    }

    
    @Test
    public void test() {
        String template="${ROOT}/${PATH}/${TO}/${FILE}";
        final Map<String,String> map=new HashMap<String, String>();
        map.put("ROOT", "/opt");
        map.put("PATH", "program");
        map.put("AT", "url");
        map.put("FILE", "file.txt");
        
        String res=Regex.resolveDelimitedTemplate("\\$\\{","\\}",template, map);
        
        assertTrue(!map.containsKey("PATH"));
        assertTrue(map.containsKey("AT"));
        
        System.out.println(res);
        assertTrue(res.equals("/opt/program//file.txt"));
    }
    
}
