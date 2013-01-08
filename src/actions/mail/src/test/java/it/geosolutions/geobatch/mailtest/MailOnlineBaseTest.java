/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  https://github.com/nfms4redd/nfms-geobatch
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
package it.geosolutions.geobatch.mailtest;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.geotools.test.OnlineTestSupport;

/**
 * @author DamianoG
 *
 */
public abstract class MailOnlineBaseTest extends OnlineTestSupport{

    public MailOnlineBaseTest(){}

    @Override
    protected String getFixtureId() {
        return "geobatch/mail/sendmail";
    }
    
    public Map getParams() {
        Map params = new HashMap();
        params.put("mailSmtpHost", getFixture().getProperty("mailSmtpHost"));
        params.put("mailSmtpStarttlsEnable", getFixture().getProperty("mailSmtpStarttlsEnable"));
        params.put("mailSmtpAuth", getFixture().getProperty("mailSmtpAuth"));
        params.put("mailSmtpPort", getFixture().getProperty("mailSmtpPort"));
        params.put("mailAuthUsername", getFixture().getProperty("mailAuthUsername"));
        params.put("mailAuthPassword", getFixture().getProperty("mailAuthPassword"));
        params.put("mailToAddress", getFixture().getProperty("mailToAddress"));
        params.put("mailFromAddress", getFixture().getProperty("mailFromAddress"));
       
        return params;
    }
    
    @Override
    protected Properties createExampleFixture() {
        Properties ret = new Properties();
        ret.setProperty("mailSmtpHost","smtp.gmail.com");
        ret.setProperty("mailSmtpStarttlsEnable","true");   
        ret.setProperty("mailSmtpAuth","true");                       
        ret.setProperty("mailSmtpPort","587"); 
        ret.setProperty("mailAuthUsername","Insert a Valid Mail address here"); 
        ret.setProperty("mailAuthPassword","password for previous mail address");               
        ret.setProperty("mailToAddress","Insert a Valid Mail address here"); 
        ret.setProperty("mailFromAddress","example mail"); 
        return ret;
    }
    
}
