/*
 */

package it.geosolutions.geobatch.sas.event;

import java.io.File;
import java.util.Date;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class SASTileEvent extends SASEvent {

    public static enum Channel {
        PORT, STBD;
    }

    protected Date date;
    protected Channel channel;


    public SASTileEvent(File source) {
        super(source);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
