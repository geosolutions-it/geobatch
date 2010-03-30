/*
 *
 * ====================================================================
 *
 * Copyright (C) 2007-2008 GeoSolutions S.A.S.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. 
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.geobatch.ftpserver.server;


import java.io.IOException;
import java.util.logging.Logger;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;

/**
 * @author giuseppe
 * 
 */
public class GeoBatchFtplet implements Ftplet /* extends DefaultFtplet */  {

	private final static Logger LOGGER = Logger.getLogger(GeoBatchFtplet.class.getName());

	private FtpStatistics ftpStats;

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.apache.ftpserver.ftplet.Ftplet#init(org.apache.ftpserver.ftplet.
	 * FtpletContext)
	 */
	public void init(FtpletContext ftpletContext) throws FtpException {
//        super.init(ftpletContext);
		this.ftpStats = ftpletContext.getFtpStatistics();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.ftpserver.ftplet.Ftplet#onConnect(org.apache.ftpserver.ftplet
	 * .FtpSession)
	 */
	public FtpletResult onConnect(FtpSession ftpSession) throws FtpException,
			IOException {
//        super.onConnect(ftpSession);

		LOGGER.info("#######################  CONNECTIONS : "
                + this.ftpStats.getCurrentConnectionNumber()
                + " / "
				+ this.ftpStats.getTotalConnectionNumber());
		LOGGER.info("#######################  LOGINS : "
                + this.ftpStats.getCurrentLoginNumber()
                + " / "
				+ this.ftpStats.getTotalLoginNumber());

		return FtpletResult.DEFAULT;
	}



//    @Override
//    public FtpletResult onLogin(FtpSession session, FtpRequest request) throws FtpException, IOException {
//        LOGGER.info("onLogin (arg): " + request.getArgument());
//        LOGGER.info("onLogin (cmd): " + request.getCommand());
//        LOGGER.info("onLogin (rql): " + request.getRequestLine());
//
//		return FtpletResult.DEFAULT;
//    }
//
//    @Override
//    public FtpletResult onUploadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
//        super.onUploadEnd(session, request);
//        LOGGER.info("FTP upload ended (arg): " + request.getArgument());
//        LOGGER.info("FTP upload ended (cmd): " + request.getCommand());
//        LOGGER.info("FTP upload ended (rql): " + request.getRequestLine());
//
//		return FtpletResult.DEFAULT;
//    }

    public void destroy() {
    }

    public FtpletResult beforeCommand(FtpSession session, FtpRequest request) throws FtpException, IOException {
        return null;
//		return FtpletResult.DEFAULT;
    }

//    public FtpletResult afterCommand(FtpSession session, FtpRequest request, FtpReply reply) throws FtpException, IOException {
//		return FtpletResult.DEFAULT;
//    }
	public FtpletResult afterCommand(FtpSession session, FtpRequest request,
			FtpReply arg2) throws FtpException, IOException {
		return null;
	}
    public FtpletResult onDisconnect(FtpSession session) throws FtpException, IOException {
		return FtpletResult.DEFAULT;
    }

}
