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


import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.global.CatalogHolder;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import org.apache.ftpserver.ftplet.DefaultFtplet;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class GeoBatchFtplet 
        extends DefaultFtplet
        implements Ftplet  {

	private final static Logger LOGGER = Logger.getLogger(GeoBatchFtplet.class.getName());

	private FtpStatistics ftpStats;

    @Override
	public void init(FtpletContext ftpletContext) throws FtpException {
        super.init(ftpletContext);
		this.ftpStats = ftpletContext.getFtpStatistics();
	}

    @Override
	public FtpletResult onConnect(FtpSession ftpSession) throws FtpException,
			IOException {
        super.onConnect(ftpSession);

		LOGGER.info("FTP Stats: CONNECTIONS : "
                + this.ftpStats.getCurrentConnectionNumber()
                + " / "
				+ this.ftpStats.getTotalConnectionNumber()
                + " -- LOGINS : "
                + this.ftpStats.getCurrentLoginNumber()
                + " / "
				+ this.ftpStats.getTotalLoginNumber());

		return FtpletResult.DEFAULT;
	}

    @Override
    public FtpletResult onUploadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        super.onUploadEnd(session, request);

        String spath = session.getFileSystemView().getWorkingDirectory().getAbsolutePath();
        String filename = request.getArgument();
        File targetFile = new File(spath, filename);

        String flowid = FilenameUtils.getName(spath);

        LOGGER.info("FTP upload ended - session working dir: '" + spath + "' - file: '"+filename+"'");

        Catalog catalog = CatalogHolder.getCatalog();

        // CHECKME FIXME next call won't work: flowid are set as the file name, not the config id
//        FileBasedFlowManager fm = catalog.getFlowManager(flowid, FileBasedFlowManager.class); // CHECKME TODO this has to be made more general
        FileBasedFlowManager fm = null;
        StringBuilder availFmSb = new StringBuilder("Available FlowManagers: ");
        for (FileBasedFlowManager fmloop : catalog.getFlowManagers(FileBasedFlowManager.class)) {            
            availFmSb.append('(').append(fmloop.getId()).append(',').append(fmloop.getName()).append(')');

            if(fmloop.getConfiguration().getId().equals(flowid)) {
                fm = fmloop;
            }            
        }

        if(fm != null) {
            LOGGER.info("Firing FILEADDED event to " + fm);
            fm.postEvent(new FileSystemMonitorEvent(targetFile, FileSystemMonitorNotifications.FILE_ADDED));
        } else {
            LOGGER.info("No FlowManager '"+flowid+"' to notify about " + targetFile  + " -- " + availFmSb);
        }

		return FtpletResult.DEFAULT;
    }

    public void destroy() {
        super.destroy();
    }

    @Override
    public FtpletResult onMkdirStart(FtpSession session, FtpRequest request) throws FtpException, IOException {
        session.write(new DefaultFtpReply(FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "No permission."));
        return FtpletResult.SKIP;
    }
    
    @Override
    public FtpletResult onRmdirStart(FtpSession session, FtpRequest request) throws FtpException, IOException {
        session.write(new DefaultFtpReply(550, "No permission."));
        return FtpletResult.SKIP;
    }

    @Override
    public FtpletResult onRenameStart(FtpSession session, FtpRequest request) throws FtpException, IOException {
        session.write(new DefaultFtpReply(553, "No permission."));
        return FtpletResult.SKIP;
    }

}
