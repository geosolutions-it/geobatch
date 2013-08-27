/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
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
/**
 *
 */
package it.geosolutions.geobatch.ui.mvc;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.geosolutions.geobatch.ftpserver.ftp.FtpUser;
import it.geosolutions.geobatch.ftpserver.server.GeoBatchServer;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.impl.DefaultFtpServer;
import org.apache.ftpserver.impl.DefaultFtpServerContext;
import org.apache.ftpserver.impl.FtpServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 *
 */
public class FTPManagerController extends AbstractController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowManagerController.class.getName());

    private GeoBatchServer server;

    /**
     * @param server
     *            the server to set
     */
    public void setServer(GeoBatchServer server)
    {
        this.server = server;
    }

    /*
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
        HttpServletResponse response) throws Exception
    {

        // Selecting output page
        String viewName;
        String view = request.getParameter("view");
        if ("users".equalsIgnoreCase(view))
        {
            viewName = "ftpUsers";
        }
        else if ("status".equalsIgnoreCase(view))
        {
            viewName = "ftp";
        }
        else
        {
            viewName = "ftp"; // default view
        }

        ModelAndView mav = new ModelAndView(viewName);

        String errMsg = null;

        // Selecting action, if any
        String action = request.getParameter("action");
        if ("stop".equalsIgnoreCase(action))
        {
            LOGGER.info("Requested STOP for ftp server.");
            if (server.isStopped())
            {
                errMsg = "Server is not running";
            }
            else
            {
                server.stop();
            }
        }
        else if ("start".equalsIgnoreCase(action))
        {
            LOGGER.info("Requested START for ftp server.");
            if (server.isStopped())
            {
                server.start();
            }
            else if (server.isSuspended())
            {
                server.resume();
            }
            else
            {
                errMsg = "Server is already running";
            }
        }
        else if ("pause".equalsIgnoreCase(action))
        {
            LOGGER.info("Requested PAUSE for ftp server.");
            if (server.isSuspended())
            {
                errMsg = "Server is already suspended";
            }
            else if (server.isStopped())
            {
                errMsg = "Server is not running";
            }
            else
            {
                server.suspend();
            }
        }

        List<FtpUser> ftpUsers = server.getUserManager().getAllUsers();
        mav.addObject("ftpUsers", ftpUsers);
        mav.addObject("ftpServer", server);
        mav.addObject("ftpConfig", server.getLastConfig());

        // add statistics
        FtpStatistics stats = null;
        final FtpServer ftp = server.getFtpServer();
        if (ftp instanceof DefaultFtpServer)
        {
            // get the context and check if the context is of the right type
            final FtpServerContext context = ((DefaultFtpServer) ftp).getServerContext();
            if (context instanceof DefaultFtpServerContext)
            {
                stats = ((DefaultFtpServerContext) context).getFtpStatistics();
            }
        }
        mav.addObject("ftpStats", stats);

        if (errMsg != null)
        {
            mav.addObject("errMsg", errMsg);
            LOGGER.info(errMsg);
        }

        return mav;
    }
}
