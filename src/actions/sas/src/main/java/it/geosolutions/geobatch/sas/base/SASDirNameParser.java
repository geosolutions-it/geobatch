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
 * Input names are in the form: DATE_missionXX_LegXXXX_CHANNEL
 * As an instance: DATE=090316 and CHANNEL=port
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class SASDirNameParser {

    private Date date;
    private String mission;
    private String leg;
    private String channel;
    private static Pattern MISSIONPATTERN = Pattern.compile("_mission(.*)_Leg(.*)_(.*)");
    private static Pattern DATEPATTERN = Pattern.compile("(.*)-(\\d+)");
    private static DateFormat DATEFRM = new SimpleDateFormat("yyyyMMdd");

    public static SASDirNameParser parse(String fileName) {
        SASDirNameParser ret = new SASDirNameParser();
        Matcher m = MISSIONPATTERN.matcher(fileName);
        if (!m.find()) {
            Logger.getLogger(SASDirNameParser.class.getName()).log(Level.SEVERE, "Can't parse '" + fileName + "'");
            return null;
        }
        try {
        	Matcher d = DATEPATTERN.matcher(m.group(1));
        	d.lookingAt();
            ret.date = (Date) DATEFRM.parse(d.group(2));
        } catch (ParseException ex) {
            Logger.getLogger(SASDirNameParser.class.getName()).log(Level.SEVERE, "Can't parse date for '" + fileName + "'", ex);
        }
        ret.mission = m.group(1);
        ret.leg = m.group(2);
        ret.channel = m.group(3);

        return ret;
    }

//	private static String buildWmsPath(final String name) {
//		if (name==null || name.trim().length()==0)
//			return "";
//		final int missionIndex = name.indexOf("_");
//        final String timePrefix = name.substring(0,missionIndex);
//        final int legIndex = name.indexOf("_Leg");
//        String missionPrefix = name.substring(missionIndex+1,legIndex);
////        final int indexOfMissionNumber = missionPrefix.lastIndexOf("_");
////        missionPrefix = new StringBuffer("mission").append(missionPrefix.substring(indexOfMissionNumber+1)).toString();
//        final String legPath = name.substring(legIndex+1);
//        final String wmsPath = new StringBuilder("/").append(timePrefix).append("/").append(missionPrefix).append("/").append(legPath.replace("_","/")).toString();
//        return wmsPath;
//	}
    private SASDirNameParser() {
    }

    public String getChannel() {
        return channel;
    }

    public Date getDate() {
        return date;
    }

    public String getLeg() {
        return leg;
    }

    public String getMission() {
        return mission;
    }
}
