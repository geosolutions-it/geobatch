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

package it.geosolutions.geobatch.ftp.client.download;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.ftp.client.FTPHelper;
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
 * This class represent an extended FTP action to download remote files or directory.
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */
public class FTPDownloadAction extends FTPBaseAction<FileSystemEvent> {

    /**
     * The constructor of the download action.
     * 
     * @param configuration
     *            The action configuration.
     * @throws IOException
     */
	public FTPDownloadAction(FTPActionConfiguration configuration) throws IOException {
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
                throw new IllegalStateException("FtpServerHost is null.");
            }

            // //////////////////////////////////////////////
            // Retrive the added files from flow working dir
            // //////////////////////////////////////////////

            final List<File> filesToGet = new ArrayList<File>();
            for (FileSystemEvent event : events) {
                final File input = event.getSource();
                if (input.exists() && input.isFile() && input.canRead())
                    filesToGet.add(input);
                else {
                    throw new IllegalStateException("No valid input file found for this data flow!");
                }
            }

            if (filesToGet.size() <= 0)
                throw new IllegalStateException("No valid file found for this Data Flow!");

            // /////////////////////////////////////////
            //
            // Downloading files from remote FTP Server
            //
            // /////////////////////////////////////////

            if (LOGGER.isInfoEnabled())
                LOGGER.info("Downloading file from FtpServer ... " + ftpserverHost);

            boolean sent = false;
            final FTPConnectMode connectMode = configuration.getConnectMode().toString()
                    .equalsIgnoreCase(FTPConnectMode.ACTIVE.toString()) ? FTPConnectMode.ACTIVE
                    : FTPConnectMode.PASV;
            final int timeout = configuration.getTimeout();

            boolean zipOutput = configuration.isZipInput();
            String zipFileName = configuration.getZipFileName();

            String path = "path";
            String localTempDir = configuration.getLocalTempDir();

            // //////////////////////////////////////////////////////////////////
            // Build in the local temp directory to download
            // //////////////////////////////////////////////////////////////////

            File dir = new File(localTempDir);
            if (!dir.exists()) {
                dir.mkdir();
            }

            if (zipOutput) {

                // ////////////////////////////////////////////////////////////////////////
                // Build the real name of the remote zipped file before
                // downloading this
                // ////////////////////////////////////////////////////////////////////////

                String remoteZipFile = zipFileName.concat(".zip");

                // ///////////////////////////////////
                // Downloading the remote zipped file
                // ///////////////////////////////////

                sent = FTPHelper.downloadFile(ftpserverHost, localTempDir, path, remoteZipFile,
                        ftpserverUSR, ftpserverPWD, ftpserverPort, FTPTransferType.BINARY,
                        WriteMode.OVERWRITE, connectMode, timeout);
            } else {

                // /////////////////////////////////////////////////////////////////////////
                // Scanning the files to get array to distinguish files and
                // directories
                // /////////////////////////////////////////////////////////////////////////

                for (File file : filesToGet) {
                    if (file.isFile()) {
                        sent = FTPHelper.downloadFile(ftpserverHost, localTempDir, path, file
                                .getName(), ftpserverUSR, ftpserverPWD, ftpserverPort,
                                FTPTransferType.BINARY, WriteMode.OVERWRITE, connectMode, timeout);

                        if (!sent)
                            break;
                    } else {
                        // sent = getDirectory("test", path, localTempDir);
                        sent = getDirectory(file.getName(), path, localTempDir);

                        if (!sent)
                            break;
                    }
                }
            }

            // TODO: remove the "sent" var and trap errors via try/catch

            if (sent) {
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("FTPDownloadAction: file SUCCESSFULLY downloaded from FtpServer!");

                listenerForwarder.completed();
            } else {
                if (LOGGER.isInfoEnabled())
                    LOGGER
                            .info("FTPDownloadAction: file was NOT downloaded from FtpServer due to connection errors!");

                listenerForwarder.failed(null);
            }

            return events;

        } catch (Exception ex) {
            // if (LOGGER.isErrorEnabled())
            // LOGGER.error(ex.getLocalizedMessage(), ex); // not
            // logging rethrown exception
            listenerForwarder.failed(ex);
            throw new ActionException(this, ex.getMessage(), ex); // wrap
            // exception
        }

    }

    /**
     * This function perform the download of the remote directory recursively.
     * 
     * @param dirName
     *            The name of the remote directory to download
     * @param remotePath
     *            The remore path of the directory to download
     * @param localPath
     *            The local path where copying the downloaded directory
     * @return boolean If true the download operation has been successful
     */

    private boolean getDirectory(final String dirName, final String remotePath,
            final String localPath) {

        boolean sent = false;

        final FTPConnectMode connectMode = configuration.getConnectMode().toString()
                .equalsIgnoreCase(FTPConnectMode.ACTIVE.toString()) ? FTPConnectMode.ACTIVE
                : FTPConnectMode.PASV;
        final int timeout = configuration.getTimeout();
        String ftpserverHost = configuration.getFtpserverHost();
        String ftpserverUSR = configuration.getFtpserverUSR();
        String ftpserverPWD = configuration.getFtpserverPWD();
        int ftpserverPort = configuration.getFtpserverPort();

        // //////////////////////////////////////////////////////////////////
        // Build in the local sub directory to download
        // //////////////////////////////////////////////////////////////////

        File dir = new File(localPath.concat("\\").concat(dirName));
        if (!dir.exists()) {
            dir.mkdir();
        }

        // ////////////////////////////////////////////////////////
        // Get the remote directory details to download this content
        // ////////////////////////////////////////////////////////

        FTPFile[] ftpFiles = FTPHelper.dirDetails(ftpserverHost, dir.getName(), remotePath,
                ftpserverUSR, ftpserverPWD, ftpserverPort, FTPTransferType.BINARY,
                WriteMode.OVERWRITE, connectMode, timeout);

        // /////////////////////////////////////////
        // Downloading the remote directory content
        // /////////////////////////////////////////

        if (ftpFiles != null && ftpFiles.length >= 1) {
            String dirPath = remotePath.concat("/" + dirName);

            for (int i = 0, n = ftpFiles.length; i < n; i++) {
                if (ftpFiles[i].getName().indexOf(".") != -1) {
                    sent = FTPHelper.downloadFile(ftpserverHost, dir.getAbsolutePath(), dirPath,
                            ftpFiles[i].getName(), ftpserverUSR, ftpserverPWD, ftpserverPort,
                            FTPTransferType.BINARY, WriteMode.OVERWRITE, connectMode, timeout);

                    if (!sent)
                        break;
                } else {
                    sent = getDirectory(ftpFiles[i].getName(), dirPath, dir.getAbsolutePath());
                    if (!sent)
                        break;
                }
            }

            if (sent)
                return true;
            else
                return false;

        } else if (ftpFiles != null && ftpFiles.length < 1) {
            return true;
        } else
            return false;
    }
}
