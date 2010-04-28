/*
 */

package it.geosolutions.geobatch.sas.base;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Files are named like this:
 * muscle_col2_090316_1_2_p_5790_5962_40_150.tif
 *
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class SASFileNameParser {
    public static enum Channel {
        PORT, STBD;
    }
    
    private String origName;
    
    private String mission;
    private String cruise;
    private Date date;
    private int missNum;
    private int leg;
    private Channel channel;
    private int pingStart;
    private int pingEnd;
    private int rangeStart;
    private int rangeEnd;

    private static Pattern PATTERN = Pattern.compile("(.*)_(.*)_(\\d\\d\\d\\d\\d\\d)_(\\n+)_(\\n+)_(p|s)_(\\n+)_(\\n+)_(\\n+)_(\\n+)");
    private static DateFormat DATEFRM = new SimpleDateFormat("yyMMdd");

    public static SASFileNameParser parse(String fileName) {
        try {
            SASFileNameParser ret = new SASFileNameParser();
            ret.origName = fileName;
            Matcher m = PATTERN.matcher(fileName);
            if (!m.find()) {
                Logger.getLogger(SASFileNameParser.class.getName()).log(Level.SEVERE, "Can't parse '"+fileName+"'");
                return null;
            }

            ret.mission = m.group(1);
            ret.cruise = m.group(2);
            ret.date = (Date) DATEFRM.parse(m.group(3));
            ret.missNum = Integer.parseInt(m.group(4));
            ret.leg = Integer.parseInt(m.group(5));
            String type = m.group(6);
            ret.channel = "p".equals(type) ? Channel.PORT : Channel.STBD;
            ret.pingStart = Integer.parseInt(m.group(7));
            ret.pingEnd = Integer.parseInt(m.group(8));
            ret.rangeStart = Integer.parseInt(m.group(9));
            ret.rangeEnd = Integer.parseInt(m.group(10));

            return ret;

        } catch (ParseException ex) {
            Logger.getLogger(SASFileNameParser.class.getName()).log(Level.SEVERE, "Can't parse date for '"+fileName+"'", ex);
            return null;
        }
    }

    private SASFileNameParser(){}

    public String getCruise() {
        return cruise;
    }

    public Date getDate() {
        return date;
    }

    public int getLeg() {
        return leg;
    }

    public int getMissNum() {
        return missNum;
    }

    public String getMission() {
        return mission;
    }

    public String getOrigName() {
        return origName;
    }

    public int getPingEnd() {
        return pingEnd;
    }

    public int getPingStart() {
        return pingStart;
    }

    public int getRangeEnd() {
        return rangeEnd;
    }

    public int getRangeStart() {
        return rangeStart;
    }

    public Channel getChannel() {
        return channel;
    }

}
