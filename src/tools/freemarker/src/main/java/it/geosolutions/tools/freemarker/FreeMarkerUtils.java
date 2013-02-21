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
package it.geosolutions.tools.freemarker;

import it.geosolutions.tools.freemarker.filter.FreeMarkerFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import freemarker.template.TemplateModel;

/**
 * Set of static function to use FreeMaker
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public abstract class FreeMarkerUtils {

    /**
     * Using the root as DataModel (wrapped using the passed FreeMarkerFilter) to produce the output file 
     * @param root
     * @param filter
     * @param output the output file
     * @throws Exception
     */
    public static void freeMarker(Object root, FreeMarkerFilter filter, File output) throws Exception {
    
        final TemplateModel model = filter.wrapRoot(root);
    
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(output);
            bw = new BufferedWriter(fw);
            filter.process(model, bw);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                }
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Using the root as DataModel (wrapped using the passed FreeMarkerFilter) to produce the returned string
     * @param root
     * @param filter
     * @return a filtered string
     * @throws Exception
     */
    public static String freeMarkerToString(Object root, FreeMarkerFilter filter) throws Exception {
    
        final TemplateModel model = filter.wrapRoot(root);
    
        StringWriter sw = null;
        BufferedWriter bw = null;
        try {
            sw = new StringWriter();
            bw = new BufferedWriter(sw);
            filter.process(model, bw);
            return sw.toString();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                }
            }
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
}
