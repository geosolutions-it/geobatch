/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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

package it.geosolutions.geobatch.flow.event.listeners.cumulator;

import it.geosolutions.geobatch.catalog.Descriptable;
import it.geosolutions.geobatch.catalog.Identifiable;
import it.geosolutions.geobatch.flow.event.ProgressListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

/**
 * Cumulates all event messages into an internal List.
 * 
 * <P>
 * You can retrieve all cumulated messages using {@link #getMessages()}
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class CumulatingProgressListener extends ProgressListener {

    
    private List<String> messages = new LinkedList<String>();

    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSSz");
    static {
        TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");
        DATEFORMAT.setTimeZone(TZ_UTC);
    }

    public CumulatingProgressListener(CumulatingProgressListenerConfiguration configuration, Identifiable owner) {
        super(configuration,owner);
    }

    /**
     * Retrieves all the event messages arrived so far.
     * 
     * @return the internal List<String> instance.
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * Removes all cumulated messages
     */
    public void clearMessages() {
        messages.clear();
    }

    protected void msg(String msg) {
        Calendar now = Calendar.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append(DATEFORMAT.format(now.getTime()))
                .append(' ')
                .append(msg)
                .append(' ')
                .append(getProgress()).append("% --")
                .append(getTask());
        Identifiable owner = getOwner();
        if (owner != null) {
            if(owner instanceof Descriptable) {
                Descriptable d = (Descriptable)owner;
                sb.append(" [").append(d.getName()).append(']');
//            } else {
//                sb.append(" [").append(owner.getId()).append(']');
            }
        }
        messages.add(sb.toString());
    }

    public void started() {
        msg("Started");
    }

    public void progressing() {
        msg("Progressing");
    }

    public void paused() {
        msg("Paused");
    }

    public void resumed() {
        msg("Resumed");
    }

    public void completed() {
        msg("Completed");
    }

    public void failed(Throwable exception) {
        msg("Failed for '" + exception + "'");
    }

    public void terminated() {
        msg("Terminated");
    }

}
