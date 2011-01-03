/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.nurc.sem.rep10.shom;

import java.io.PipedWriter;
import java.util.concurrent.ExecutorService;

import it.geosolutions.geobatch.tools.file.Producer;

/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class NcMLFilter extends Producer {
    private SHOMConfiguration conf=null;
    
    public NcMLFilter(SHOMConfiguration c, ExecutorService e) {
        super(e);
        conf=c;
    }

    /**
     * Produce the output from an NcML file
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     * @see it.geosolutions.geobatch.tools.file.Producer#producer(java.io.PipedWriter)
     */
    @Override
    protected void producer(PipedWriter pw) throws Exception {
        /*
         * Merge data-model with template
         * elaborate template
         */
        conf.process(pw);
        pw.flush();
    }

}
