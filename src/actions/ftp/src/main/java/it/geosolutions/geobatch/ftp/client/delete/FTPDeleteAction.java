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
package it.geosolutions.geobatch.ftp.client.delete;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.ftp.client.FTPHelper;
import it.geosolutions.geobatch.ftp.client.FTPHelperBare;
import it.geosolutions.geobatch.ftp.client.configuration.FTPActionConfiguration;
import it.geosolutions.geobatch.ftp.client.configuration.FTPBaseAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.WriteMode;

/**
 * This class represent an extended FTP action to delete remote files or directory.
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */
public class FTPDeleteAction extends FTPBaseAction<FileSystemEvent> {

    /**
     * The constructor of the delete action.
     * 
     * @param configuration
     *            The action configuration.
     * @throws IOException
     */
	public FTPDeleteAction(FTPActionConfiguration configuration) throws IOException {
        super(configuration);
    }

	@Override
	@CheckConfiguration
	public boolean checkConfiguration() {
		// TODO Auto-generated method stub
		return true;
	}
	
    /**
     * Method to launch the action operations when a file system monitor event occurred.
     * 
     * @param events
     *            The events queue.
     * @throws IOException
     */
    public Queue<FileSystemEvent> execute(Queue<FileSystemEvent> events)
            throws ActionException {

        try {
            listenerForwarder.started();
            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////

            if (configuration == null) {
                throw new IllegalStateException("DataFlowConfig is null.");
            }

            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////

            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////

            String ftpserverHost = configuration.getFtpserverHost();
            String ftpserverUSR = configuration.getFtpserverUSR();
            String ftpserverPWD = configuration.getFtpserverPWD();
            int ftpserverPort = configuration.getFtpserverPort();

            if ((ftpserverHost == null) || "".equals(ftpserverHost)) {
                throw new IllegalStateException("configuration.getFtpserverHost() is null.");
            }

            // //////////////////////////////////////////////
            // Retrive the added files from flow working dir
            // //////////////////////////////////////////////

            final List<File> filesToDelete = new ArrayList<File>();
            for (FileSystemEvent event : events) {
                final File input = event.getSource();
                if (input.exists() && input.isFile() && input.canRead()) {
                    filesToDelete.add(input);

                } else {
                    throw new IllegalStateException("No valid input file found for this data flow!");
                }
            }

            if (filesToDelete.size() <= 0) {
                throw new IllegalStateException("No valid file found for this data flow!");

                // /////////////////////////////////////////
                //
                // Deleting files from remote FTP Server
                //
                // /////////////////////////////////////////

            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Deleting file from FtpServer ... " + configuration.getFtpserverHost());

            }
            boolean ok = false;
            final FTPConnectMode connectMode = configuration.getConnectMode().toString()
                    .equalsIgnoreCase(FTPConnectMode.ACTIVE.toString()) ? FTPConnectMode.ACTIVE
                    : FTPConnectMode.PASV;
            final int timeout = configuration.getTimeout();

            boolean zipOutput = configuration.isZipInput();
            String zipFileName = configuration.getZipFileName();

            String path = "path";

            if (zipOutput) {

                // ////////////////////////////////////////////////////////////////////
                // Build the real name of the remote zipped file before deleting
                // this
                // ////////////////////////////////////////////////////////////////////

                String remoteZipFile = zipFileName.concat(".zip");

                // /////////////////////////////////
                // Deleting the remote zipped file
                // /////////////////////////////////

                FTPHelperBare.deleteFileOrDirectory(ftpserverHost, remoteZipFile, false, path,
                        ftpserverUSR, ftpserverPWD, ftpserverPort, connectMode, timeout);
            } else {

                // /////////////////////////////////////////////////////////////////////////
                // Scanning the files to delete array to distinguish files and
                // directories
                // /////////////////////////////////////////////////////////////////////////

                for (File file : filesToDelete) {
                    if (file.isFile()) {
                        FTPHelperBare.deleteFileOrDirectory(ftpserverHost, file.getName(), false,
                                path, ftpserverUSR, ftpserverPWD, ftpserverPort, connectMode,
                                timeout);

                        // if(!ok)
                        // break;
                    } else {
                        // sent = deleteDirectory("test", path);
                        ok = deleteDirectory(file.getName(), path);

                        if (!ok) {
                            break;

                        } else {
                            // sent =
                            // FTPHelper.deleteFileOrDirectory(configuration.getFtpserverHost(),
                            // "test", true, path, ftpserverUSR, ftpserverPWD,
                            // ftpserverPort, connectMode, timeout);
                            FTPHelperBare.deleteFileOrDirectory(ftpserverHost, file.getName(),
                                    true, path, ftpserverUSR, ftpserverPWD, ftpserverPort,
                                    connectMode, timeout);

                            // if (!ok) {
                            // break;
                            // }
                        }
                    }
                }
            }

            // TODO: restruct previous calls so that if we are here, all went
            // well.
            // i.e. "ok" var should be useless

            if (ok) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("FTPDeleteAction: file SUCCESSFULLY deleted from FtpServer!");
                }
                listenerForwarder.completed();
            } else {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER
                            .info("FTPDeleteAction: file was NOT deleted from FtpServer due to connection errors!");
                }
                listenerForwarder.failed(null);
            }

            return events;

        } catch (Exception ex) {
            // if (LOGGER.isErrorEnabled())
            // LOGGER.error(t.getLocalizedMessage(), t); // not
            // logging rethrown exception
            listenerForwarder.failed(ex);
            throw new ActionException(this, ex.getMessage(), ex); // wrap
            // exception
        }
    }

    /**
     * This function perform the elimination of the remote directory recursively.
     * 
     * @param dirName
     *            The directory name to delete.
     * @param remotePath
     *            The remote directory path to delete
     * @return boolean If true the deleting operation has been successful
     */
    private boolean deleteDirectory(final String dirName, final String remotePath) {

        boolean sent = false;

        final FTPConnectMode connectMode = configuration.getConnectMode().toString()
                .equalsIgnoreCase(FTPConnectMode.ACTIVE.toString()) ? FTPConnectMode.ACTIVE
                : FTPConnectMode.PASV;
        final int timeout = configuration.getTimeout();
        String ftpserverHost = configuration.getFtpserverHost();
        String ftpserverUSR = configuration.getFtpserverUSR();
        String ftpserverPWD = configuration.getFtpserverPWD();
        int ftpserverPort = configuration.getFtpserverPort();

        // ////////////////////////////////////////////////////////
        // Get the remote directory details to delete this content
        // ////////////////////////////////////////////////////////

        FTPFile[] ftpFiles = FTPHelper.dirDetails(ftpserverHost, dirName, remotePath, ftpserverUSR,
                ftpserverPWD, ftpserverPort, FTPTransferType.BINARY, WriteMode.OVERWRITE,
                connectMode, timeout);

        // //////////////////////////////////////
        // Deleting the remote directory content
        // //////////////////////////////////////

        if (ftpFiles != null && ftpFiles.length >= 1) {
            String dirPath = remotePath.concat("/" + dirName);

            for (int i = 0, n = ftpFiles.length; i < n; i++) {
                if (ftpFiles[i].isDir()) {
                    sent = deleteDirectory(ftpFiles[i].getName(), dirPath);

                    if (!sent) {
                        break;

                    } else {
                        sent = FTPHelper.deleteFileOrDirectory(ftpserverHost,
                                ftpFiles[i].getName(), true, dirPath, ftpserverUSR, ftpserverPWD,
                                ftpserverPort, connectMode, timeout);

                        if (!sent) {
                            break;

                        }
                    }
                } else {
                    sent = FTPHelper.deleteFileOrDirectory(ftpserverHost, ftpFiles[i].getName(),
                            false, dirPath, ftpserverUSR, ftpserverPWD, ftpserverPort, connectMode,
                            timeout);

                    if (!sent) {
                        break;

                    }
                }
            }

            if (sent) {
                return true;

            } else {
                return false;

            }
        } else if (ftpFiles != null && ftpFiles.length < 1) {
            return true;
        } else {
            return false;

        }
    }
}
