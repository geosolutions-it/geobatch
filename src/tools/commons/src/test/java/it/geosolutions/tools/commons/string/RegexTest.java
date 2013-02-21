/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.commons.string;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

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
