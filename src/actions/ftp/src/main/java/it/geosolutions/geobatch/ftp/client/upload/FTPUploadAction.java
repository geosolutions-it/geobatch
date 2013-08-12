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

package it.geosolutions.geobatch.ftp.client.upload;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.ftp.client.FTPHelperBare;
import it.geosolutions.geobatch.ftp.client.configuration.FTPActionConfiguration;
import it.geosolutions.geobatch.ftp.client.configuration.FTPBaseAction;
import it.geosolutions.tools.compress.file.Compressor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.WriteMode;

/**
 * This class represent an extended FTP action to upload local files or directory.
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * @author Ivano Picco
 */
public class FTPUploadAction extends FTPBaseAction<FileSystemEvent> {

    /**
     * The constructor of the upload action.
     * 
     * @param configuration
     *            The action configuration.
     * @throws IOException
     */
	public FTPUploadAction(FTPActionConfiguration configuration) throws IOException {
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

            final List<File> filesToSend = new ArrayList<File>();
            for (FileSystemEvent event : events) {
                final File input = event.getSource();
                if (input.exists() && input.isFile() && input.canRead())
                    filesToSend.add(input);
                else {
                    throw new IllegalStateException("No valid input file found for this data flow!");
                }
            }

            if (filesToSend.size() <= 0)
                throw new IllegalStateException("No valid file found for this Data Flow!");

            // ////////////////////////////////////////////////////////////////////
            //
            // Sending data to FtpServer via FTP.
            //
            // ////////////////////////////////////////////////////////////////////

            if (LOGGER.isInfoEnabled())
                LOGGER.info("Sending file to FtpServer ... " + ftpserverHost);

            // boolean sent = false;
            final FTPConnectMode connectMode = configuration.getConnectMode().toString()
                    .equalsIgnoreCase(FTPConnectMode.ACTIVE.toString()) ? FTPConnectMode.ACTIVE
                    : FTPConnectMode.PASV;
            final int timeout = configuration.getTimeout();

            boolean zipMe = configuration.isZipInput();
            String zipFileName = configuration.getZipFileName();

            String path = "path";

            if (zipMe) {

                // /////////////////////////////////////////////
                // Build the temporary directory to zip a files
                // /////////////////////////////////////////////

                // final File tempDir = new
                // File(System.getProperty("java.io.tmpdir"));
                final File tempDir = new File(configuration.getLocalTempDir());

                if (!tempDir.exists()) {
                    tempDir.mkdir();
                }

                if (!tempDir.exists() || !tempDir.canWrite())
                    throw new IllegalStateException("Unable to create temporary file");

                // ///////////////////////////////////////////////////
                // Zipping the files and directory before sending this
                // ///////////////////////////////////////////////////

                final File zippedFile = Compressor.zip(tempDir, zipFileName, filesToSend
                        .toArray(new File[filesToSend.size()]));

                FTPHelperBare.putBinaryFileTo(ftpserverHost, zippedFile.getAbsolutePath(), path,
                        ftpserverUSR, ftpserverPWD, ftpserverPort, WriteMode.OVERWRITE,
                        connectMode, timeout);
            } else {

                // /////////////////////////////////////////////////////////////////////////
                // Scanning the files to send array to distinguish files and
                // directories
                // /////////////////////////////////////////////////////////////////////////

                for (File file : filesToSend) {
                    if (file.isFile()) {
                        FTPHelperBare.putBinaryFileTo(ftpserverHost, file.getAbsolutePath(), path,
                                ftpserverUSR, ftpserverPWD, ftpserverPort, WriteMode.OVERWRITE,
                                connectMode, timeout);
                        // if(!sent)
                        // break;
                    } else {
                        // File dir = new
                        // File("C:/Users/tobaro/Desktop/prove gb ftp/test");
                        // sent = putDirectory(dir, path);
                        // sent =
                        putDirectory(file, path);

                        // if(!sent)
                        // break;
                    }
                }
            }

            // if (sent)
            if (LOGGER.isInfoEnabled())
                LOGGER.info("FTPUploadAction: file SUCCESSFULLY sent to FtpServer!");
            // else
            // if (LOGGER.isInfoEnabled())
            // LOGGER.info("FTPUploadAction: file was NOT sent to FtpServer due to connection errors!");

            listenerForwarder.completed();
            return events;

        } catch (Exception ex) {
            // if (LOGGER.isErrorEnabled())
            // LOGGER.error(ex.getLocalizedMessage(), ex); // not
            // logging rethrown exception
            if (LOGGER.isInfoEnabled())
                LOGGER
                        .info("FTPUploadAction: file was NOT sent to FtpServer due to connection errors: "
                                + ex.getMessage());
            listenerForwarder.failed(ex);
            throw new ActionException(this, ex.getMessage(), ex); // wrap
            // exception
        }
    }

    /**
     * This function perform the upload of the local directory recursively.
     * 
     * @param file
     *            The local file to upload.
     * @param path
     *            The remote path where uploading teh files or directory.
     * @return If true the upload operation has been successful.
     */
    private void putDirectory(final File file, final String path) throws IOException, FTPException {

        // boolean sent = false;

        final FTPConnectMode connectMode = configuration.getConnectMode().toString()
                .equalsIgnoreCase(FTPConnectMode.ACTIVE.toString()) ? FTPConnectMode.ACTIVE
                : FTPConnectMode.PASV;
        final int timeout = configuration.getTimeout();
        String ftpserverHost = configuration.getFtpserverHost();
        String ftpserverUSR = configuration.getFtpserverUSR();
        String ftpserverPWD = configuration.getFtpserverPWD();
        int ftpserverPort = configuration.getFtpserverPort();

        String dirName = file.getName();

        // //////////////////////////////////////////////////////
        // Build in the remote directory to upload this content
        // //////////////////////////////////////////////////////

        // sent =
        FTPHelperBare.createDirectory(ftpserverHost, dirName, path, ftpserverUSR, ftpserverPWD,
                ftpserverPort, FTPTransferType.BINARY, WriteMode.OVERWRITE, connectMode, timeout);

        // /////////////////////////////
        // Uploading the local content
        // /////////////////////////////

        // if(sent){
        String dirPath = path.concat("/" + dirName);

        File[] files = file.listFiles();

        for (int i = 0, n = files.length; i < n; i++) {
            if (files[i].isDirectory()) {
                // sent =
                putDirectory(files[i], dirPath);

                // if(!sent)
                // break;
            } else {
                FTPHelperBare.putBinaryFileTo(ftpserverHost, files[i].getAbsolutePath(), dirPath,
                        ftpserverUSR, ftpserverPWD, ftpserverPort, WriteMode.OVERWRITE,
                        connectMode, timeout);

                // if(!sent)
                // break;
            }
        }

        // if(sent)return true;
        // else return false;

        // }else return false;
    }
}
