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
/**
 *
 */
package it.geosolutions.geobatch.ui.mvc;

import it.geosolutions.geobatch.ftpserver.model.FtpServerConfig;
import it.geosolutions.geobatch.ftpserver.server.GeoBatchServer;
import it.geosolutions.geobatch.ui.mvc.data.FtpConfigDataBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.impl.DefaultFtpServer;
import org.apache.ftpserver.impl.DefaultFtpServerContext;
import org.apache.ftpserver.impl.FtpServerContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * @author Alessio Fabiani
 * 
 */
@SuppressWarnings("deprecation")
public class FTPConfigFormController extends SimpleFormController {

    private GeoBatchServer server;

    /**
     * @param server
     *            the server to set
     */
    public void setServer(GeoBatchServer server) {
        this.server = server;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject
     * (javax.servlet .http.HttpServletRequest)
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        FtpConfigDataBean backingObject = new FtpConfigDataBean();

        FtpServerConfig config = server.getLastConfig();
        if (config != null) {
            backingObject.setId(config.getId());

            backingObject.setAnonEnabled(config.isAnonEnabled());
            backingObject.setAutoStart(config.isAutoStart());
            backingObject.setFtpBaseDir(config.getFtpBaseDir());
            backingObject.setLoginFailureDelay(config.getLoginFailureDelay());
            backingObject.setMaxAnonLogins(config.getMaxAnonLogins());
            backingObject.setMaxLoginFailures(config.getMaxLoginFailures());
            backingObject.setMaxLogins(config.getMaxLogins());
            backingObject.setPort(config.getPort());
            backingObject.setSsl(config.isSsl());
        }

        return backingObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(java .lang.Object,
     * org.springframework.validation.BindException)
     */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        FtpConfigDataBean givenData = (FtpConfigDataBean) command;

        //LOGGER.debug(givenData.toString());

        FtpServerConfig config = new FtpServerConfig();
        config.setAnonEnabled(givenData.isAnonEnabled());
        config.setAutoStart(givenData.isAutoStart());
        config.setFtpBaseDir(givenData.getFtpBaseDir());
        config.setLoginFailureDelay(givenData.getLoginFailureDelay());
        config.setMaxAnonLogins(givenData.getMaxAnonLogins());
        config.setMaxLoginFailures(givenData.getMaxLoginFailures());
        config.setMaxLogins(givenData.getMaxLogins());
        config.setPort(givenData.getPort());
        config.setSsl(givenData.isSsl());

        server.getServerConfigDAO().save(config);
        server.setLastConfig(config);

        ModelAndView mav = new ModelAndView(getSuccessView());
        mav.addObject("ftpServer", server);
        mav.addObject("ftpConfig", server.getLastConfig());

        // add statistics
        FtpStatistics stats = null;
        final FtpServer ftp = server.getFtpServer();
        if (ftp instanceof DefaultFtpServer) {
            // get the context and check if the context is of the right type
            final FtpServerContext context = ((DefaultFtpServer) ftp).getServerContext();
            if (context instanceof DefaultFtpServerContext)
                stats = ((DefaultFtpServerContext) context).getFtpStatistics();
        }
        mav.addObject("ftpStats", stats);
//        logger.debug("Form data successfully submitted");
        return mav;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.web.servlet.mvc.BaseCommandController#onBindAndValidate
     * (javax.servlet.http.HttpServletRequest, java.lang.Object,
     * org.springframework.validation.BindException)
     */
    @Override
    protected void onBindAndValidate(HttpServletRequest request, Object command,
            BindException errors) throws Exception {

        FtpConfigDataBean givenData = (FtpConfigDataBean) command;
        if (givenData == null) {
            errors.reject("error.nullpointer", "Null data received");
        } else {
            /* VALIDATE ALL FIELDS */
            if (request.getParameter("ssl") == null)
                givenData.setSsl(false);

            if (request.getParameter("autoStart") == null)
                givenData.setAutoStart(false);

            if (request.getParameter("anonEnabled") == null)
                givenData.setAnonEnabled(false);

            if (givenData.getMaxLogins() < 0) {
                errors.rejectValue("maxLogins", "error.code",
                        "Ftp Max Logins must be greater than 0.");
            }

            if (givenData.getMaxLoginFailures() < 0) {
                errors.rejectValue("maxLoginFailures", "error.code",
                        "Ftp Max Logins Failuers must be greater than 0.");
            }

            if (givenData.getLoginFailureDelay() < 0) {
                errors.rejectValue("loginFailureDelay", "error.code",
                        "Ftp Login Failuers Delay must be greater than 0.");
            }

        }
    }
}
