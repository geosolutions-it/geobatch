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
import java.util.logging.Level;
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
        //extends DefaultFtplet
        implements Ftplet  {

	private final static Logger LOGGER = Logger.getLogger(GeoBatchFtplet.class.getName());

	private FtpStatistics ftpStats;

    public void init(FtpletContext ftpletContext) throws FtpException {
		this.ftpStats = ftpletContext.getFtpStatistics();
	}

    public FtpletResult onConnect(FtpSession ftpSession) throws FtpException,
			IOException {
		LOGGER.log(Level.INFO, "FTP Stats: CONNECTIONS : {0} / {1} -- LOGINS : {2} / {3}",
                new Object[]{
                    this.ftpStats.getCurrentConnectionNumber(),
                    this.ftpStats.getTotalConnectionNumber(),
                    this.ftpStats.getCurrentLoginNumber(),
                    this.ftpStats.getTotalLoginNumber()});

		return FtpletResult.DEFAULT;
	}
    
	public FtpletResult onDisconnect(FtpSession session) throws FtpException,
			IOException {
		return null;
	}

	public FtpletResult beforeCommand(FtpSession session, FtpRequest request)
			throws FtpException, IOException {
		String command = request.getCommand().toUpperCase();

//		if ("DELE".equals(command)) {
//			return onDeleteStart(session, request);
//		} else if ("STOR".equals(command)) {
//			return onUploadStart(session, request);
//		} else if ("RETR".equals(command)) {
//			return onDownloadStart(session, request);
//		} else 
			if ("RMD".equals(command)) {
			return onRmdirStart(session, request);
		} else if ("MKD".equals(command)) {
			return onMkdirStart(session, request);
//		} else if ("APPE".equals(command)) {
//			return onAppendStart(session, request);
//		} else if ("STOU".equals(command)) {
//			return onUploadUniqueStart(session, request);
		} else if ("RNTO".equals(command)) {
			return onRenameStart(session, request);
//		} else if ("SITE".equals(command)) {
//			return onSite(session, request);
		} else {
			// TODO should we call a catch all?
			return null;
		}
	}

	public FtpletResult afterCommand(FtpSession session, FtpRequest request,
			FtpReply reply) throws FtpException, IOException {

		// the reply is ignored for these callbacks

		String command = request.getCommand().toUpperCase();

//		if ("PASS".equals(command)) {
//			return onLogin(session, request);
//		} else if ("DELE".equals(command)) {
//			return onDeleteEnd(session, request);
//		} else 
			if ("STOR".equals(command)) {
			return onUploadEnd(session, request, reply);
//		} else if ("RETR".equals(command)) {
//			return onDownloadEnd(session, request);
//		} else if ("RMD".equals(command)) {
//			return onRmdirEnd(session, request);
//		} else if ("MKD".equals(command)) {
//			return onMkdirEnd(session, request);
		} else if ("APPE".equals(command)) {
			return onAppendEnd(session, request, reply );
//		} else if ("STOU".equals(command)) {
//			return onUploadUniqueEnd(session, request);
//		} else if ("RNTO".equals(command)) {
//			return onRenameEnd(session, request);
		} else {
			// TODO should we call a catch all?
			return null;
		}
	}   
    
    public FtpletResult onUploadEnd(FtpSession session, FtpRequest request, FtpReply reply) throws FtpException, IOException {
    	if( reply.getCode() != FtpReply.REPLY_226_CLOSING_DATA_CONNECTION) { // There has been an error
    		if(LOGGER.isLoggable(Level.INFO))
	  			LOGGER.log(Level.INFO, "Upload of file '{0}' failed.", request.getArgument());
    		return FtpletResult.DEFAULT;    		
    	} else {
    		if(LOGGER.isLoggable(Level.INFO))
	  			LOGGER.log(Level.INFO, "Upload of file '{0}' completed.", request.getArgument());
    		return fireGeoBatchFileAdd(session, request);
    	}
    }
    
    public FtpletResult onAppendEnd(FtpSession session, FtpRequest request, FtpReply reply) throws FtpException, IOException {
	  	if( reply.getCode() != FtpReply.REPLY_226_CLOSING_DATA_CONNECTION) { // There has been an error
	  		if(LOGGER.isLoggable(Level.INFO))
	  			LOGGER.log(Level.INFO, "Append of file '{0}' failed.", request.getArgument());
	  		return FtpletResult.DEFAULT;    		
	  	} else {
    		if(LOGGER.isLoggable(Level.INFO))
	  			LOGGER.log(Level.INFO, "Append of file '{0}' completed.", request.getArgument());
	  		return fireGeoBatchFileAdd(session, request);
	  	}
    }
    
    protected FtpletResult fireGeoBatchFileAdd(FtpSession session, FtpRequest request) throws FtpException, IOException {
    	
        String userPath = session.getUser().getHomeDirectory();
        String currDirPath = session.getFileSystemView().getWorkingDirectory().getAbsolutePath();
        String filename = request.getArgument();

        File dirFile = new File(userPath, currDirPath);
        File targetFile = new File(dirFile, filename);

        String flowid = FilenameUtils.getName(currDirPath);

  		if(LOGGER.isLoggable(Level.INFO))
  			LOGGER.log(Level.INFO, "FTP upload ended - session working dir: ''{0}'' - file: ''{1}''", new Object[]{currDirPath, filename});

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
            LOGGER.log(Level.INFO, "No FlowManager ''{0}'' to notify about {1} -- {2}", new Object[]{flowid, targetFile, availFmSb});
        }

		return FtpletResult.DEFAULT;
    }

    public void destroy() {
//        super.destroy();
    }

    public FtpletResult onMkdirStart(FtpSession session, FtpRequest request) throws FtpException, IOException {
        session.write(new DefaultFtpReply(FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "No permission."));
        return FtpletResult.SKIP;
    }
    
    public FtpletResult onRmdirStart(FtpSession session, FtpRequest request) throws FtpException, IOException {
        session.write(new DefaultFtpReply(550, "No permission."));
        return FtpletResult.SKIP;
    }

    public FtpletResult onRenameStart(FtpSession session, FtpRequest request) throws FtpException, IOException {
        session.write(new DefaultFtpReply(553, "No permission."));
        return FtpletResult.SKIP;
    }

}
