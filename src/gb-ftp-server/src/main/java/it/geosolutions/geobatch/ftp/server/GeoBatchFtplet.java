/*
 * $Header: it.geosolutions.geobatch.ftp.server.GeoBatchFtplet,v. 0.1 13/ott/2009 22.43.14 created by giuseppe $
 * $Revision: 0.1 $
 * $Date: 13/ott/2009 22.43.14 $
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
package it.geosolutions.geobatch.ftp.server;

import it.geosolutions.geobatch.ftp.server.model.FtpUser;

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
public class GeoBatchFtplet implements Ftplet {

	private Logger logger = Logger.getLogger(GeoBatchFtplet.class.getName());

	private FtpStatistics ftpStatisticts;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.ftpserver.ftplet.Ftplet#afterCommand(org.apache.ftpserver.
	 * ftplet.FtpSession, org.apache.ftpserver.ftplet.FtpRequest,
	 * org.apache.ftpserver.ftplet.FtpReply)
	 */
	public FtpletResult afterCommand(FtpSession session, FtpRequest request,
			FtpReply arg2) throws FtpException, IOException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.ftpserver.ftplet.Ftplet#beforeCommand(org.apache.ftpserver
	 * .ftplet.FtpSession, org.apache.ftpserver.ftplet.FtpRequest)
	 */
	public FtpletResult beforeCommand(FtpSession ftpSession, FtpRequest ftpRequest)
			throws FtpException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.ftpserver.ftplet.Ftplet#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.apache.ftpserver.ftplet.Ftplet#init(org.apache.ftpserver.ftplet.
	 * FtpletContext)
	 */
	public void init(FtpletContext ftpletContext) throws FtpException {

		this.ftpStatisticts = ftpletContext.getFtpStatistics();

		FtpUser ftpUser = new FtpUser();
		ftpUser.setUserId("admin");
		ftpUser.setUserPassword("admin");
		ftpUser.setWritePermission(true);

		ftpletContext.getUserManager().save(ftpUser);
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
		logger.info("#######################TOTAL CONNECT##############"
				+ this.ftpStatisticts.getTotalConnectionNumber());

		return FtpletResult.DEFAULT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.ftpserver.ftplet.Ftplet#onDisconnect(org.apache.ftpserver.
	 * ftplet.FtpSession)
	 */
	public FtpletResult onDisconnect(FtpSession ftpSession) throws FtpException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
