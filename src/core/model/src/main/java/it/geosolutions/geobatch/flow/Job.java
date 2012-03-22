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
package it.geosolutions.geobatch.flow;

/**
 * Anything that can be run, paused and resumed.
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public interface Job {

    /**
     * Pause the execution of a Job.<BR>
     * Implementations may also completely ignore the <TT>pause()</TT> call -- for instance the job
     * may not be split in execution steps, loop iterations and so on; in this case the
     * <TT>pause()</TT> invocation may simply return <TT>false</TT>, indicating to the caller that
     * the job was not paused.
     * 
     * @return true if the Job has been successfully paused.
     */

    public boolean pause();

    /**
     * Pause the execution of a Job and optionally all of its subjobs. <BR>
     * A <TT>Job</TT> may control other subjobs running asynchronally. <BR>
     * When <TT>sub</TT> is true, also the subjobs will be paused.
     * 
     * <P>
     * <TT>pause(false)</TT> is equivalent to <TT>pause()</TT>
     * 
     * @param sub
     *            when <TT>true</TT>, also subtasks will be paused.
     * 
     * @return <TT>true</TT> if the Job has been successfully paused, disregarding any return values
     *         from its subjobs.
     */
    public boolean pause(boolean sub);

    /**
     * Resume a previously paused job. <BR>
     * If the instance does not support pausing or it was not paused, nothing will happen.
     */
    public void resume();

    public boolean isPaused();

}
