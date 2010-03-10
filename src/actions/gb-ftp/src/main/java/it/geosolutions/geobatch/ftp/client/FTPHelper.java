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

package it.geosolutions.geobatch.ftp.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPMessageCollector;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FileTransferClient;
import com.enterprisedt.net.ftp.WriteMode;


/**
 * EDTFTP based utility methods.
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * @author Ivano Picco
 */
public class FTPHelper {

    private static final Logger LOGGER = Logger.getLogger(FTPHelper.class.toString());

    /**
     * Function to put binary file.
     * 
     * @param ftpserverHost The remote server host.
     * @param binaryFile The file name.
     * @param ftpserverUser The user name.
     * @param ftpserverPassword The user password.
     * @param writeMode  The write mode.
     * @param connectMode The connection mode.
     * @param timeout The connection timeout.
     * @return If true the upload has been successful.
     */
    public static boolean putBinaryFileTo(String ftpserverHost, String binaryFile, String path,
            String ftpserverUser, String ftpserverPassword,int ftpserverPort, WriteMode writeMode, FTPConnectMode connectMode, int timeout ) {

    	if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("[FTP::PutFileTo]: " + "start");

        boolean res = putFile(ftpserverHost, binaryFile, path, ftpserverUser,
				ftpserverPassword, ftpserverPort,FTPTransferType.BINARY,writeMode,connectMode,timeout); 
         
         if(LOGGER.isLoggable(Level.INFO))
     		LOGGER.info("[FTP::PutFileTo]: " + "end");
         
         return res;         
    }
    
    /**
     * This function manage the FTP client connection.
     * 
     * @param ftpserverHost The remote server host.
     * @param ftpserverUser The user name.
     * @param ftpserverPassword The user password.
     * @param ftpserverPort The remote server port.
     * @param transferType The transfer type.
     * @param writeMode The write mode.
     * @param connectMode The connection mode.
     * @param timeout The connection timeout.
     * @return the FTP client.
     */
    private static FTPClient connectTo(String ftpserverHost, String ftpserverUser,
			String ftpserverPassword, int ftpserverPort, FTPTransferType transferType, 
			WriteMode writeMode, FTPConnectMode connectMode, int timeout){

        final String host = ftpserverHost;
        final String login = ftpserverUser;
        final String password = ftpserverPassword;
        final int port = ftpserverPort;

        FTPClient ftp = null;

        try {
            ftp = new FTPClient();
            ftp.setRemoteHost(host);
            ftp.setRemotePort(port);
			ftp.setTimeout(timeout);
            final FTPMessageCollector listener = new FTPMessageCollector();
            ftp.setMessageListener(listener);

            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Connecting");
            
			ftp.connect();
			
			if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Logging in");
			
			ftp.login(login, password);
            
			// /////////////////////////////////////
            // Transfer mode (ACTIVE vs PASSIVE)
			// /////////////////////////////////////
			
			ftp.setConnectMode(connectMode);	
			
			// //////////////////////////////////
            // Transfer type (BINARY vs ASCII)
			// //////////////////////////////////
			
            ftp.setType(transferType);
            
            return ftp;
            
        }catch(FTPException ftpe){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, ftpe.getLocalizedMessage(), ftpe);
            
        	// ///////////////////////////////
        	// Disconnect to the FTP server 
        	// ///////////////////////////////
        	
        	if(ftp != null && ftp.connected())
        		try{
        			ftp.quitImmediately();
        		}catch (Throwable t) {
                    if(LOGGER.isLoggable(Level.FINE))
                		LOGGER.log(Level.FINE,t.getLocalizedMessage(),t);
				}
        		
            return null;
        }catch(IOException ioe){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, ioe.getLocalizedMessage(), ioe);
            
        	// ///////////////////////////////
        	// Disconnect to the FTP server 
        	// ///////////////////////////////
        	
        	if(ftp != null && ftp.connected())
        		try{
        			ftp.quitImmediately();
        		}catch (Throwable t) {
                    if(LOGGER.isLoggable(Level.FINE))
                		LOGGER.log(Level.FINE,t.getLocalizedMessage(),t);
				}
        		
            return null;
        }
    }

	/**
	 * This function manage the FTP file upload. 
	 * 
	 * @param ftpserverHost The remote server host.
	 * @param binaryFile The binary file to upload. 
	 * @param ftpserverUser The user name. 
	 * @param ftpserverPassword The user password.
	 * @param ftpserverPort The remote server port.
	 * @param transferType The transfer type. 
	 * @param writeMode The write mode.
	 * @param connectMode The connection mode.
	 * @param timeout The timeout of teh FTP connection.
	 * @return true if the upload has been successful.
	 */
	private static boolean putFile(String ftpserverHost, String binaryFile, String path, String ftpserverUser,
			String ftpserverPassword, int ftpserverPort, FTPTransferType transferType, 
			WriteMode writeMode, FTPConnectMode connectMode, int timeout) {
		
		boolean res = false;

        String remoteFileName = null;
        FTPClient ftp = null;

        try {
            ftp = connectTo(ftpserverHost, ftpserverUser, ftpserverPassword, 
            		ftpserverPort, transferType, writeMode, connectMode, timeout);
            
            // ///////////////////////////////
            // Get the remote file name
            // ///////////////////////////////
            
            remoteFileName = binaryFile.replaceAll("\\\\", "/");
            remoteFileName = remoteFileName.substring(remoteFileName.lastIndexOf("/") + 1, remoteFileName.length());

            if(LOGGER.isLoggable(Level.INFO))
            		LOGGER.info("[FTP::PutFileTo]: " + "Connecting to :" + ftpserverHost + ":" + ftpserverPort);            

            if(LOGGER.isLoggable(Level.INFO))
            		LOGGER.info("[FTP::FileTo]: " + "sending: " + binaryFile + " to: " + remoteFileName);

            // /////////////////////////////////////////////
            // Checking to change remote working directory 
            // /////////////////////////////////////////////
            
            if(path.indexOf("/") != -1){
            	String[] pathArray = path.split("/");
            	
            	for(int h=0; h<pathArray.length; h++)
            		if(pathArray[h].indexOf("path") != -1) continue;
            		else ftp.chdir(pathArray[h]);
            }
        	
            // //////////////////////////
            // Uploading the local file 
            // //////////////////////////
            
            final String remoteFileNameReturned = ftp.put(binaryFile, remoteFileName);
            
            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("[FTP::FileTo]: " + "sent: " + binaryFile + " to: " + remoteFileNameReturned);
            
            if(remoteFileNameReturned != null)res = true;
            
        }catch (FTPException ftpe) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + ftpe.getLocalizedMessage(),ftpe);
            res = false;
        }catch (IOException ioe) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + ioe.getLocalizedMessage(),ioe);
            res = false;
        }catch (Throwable t) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + t.getLocalizedMessage(),t);
            res = false;
        }finally{
        	
        	// ///////////////////////////////
        	// Disconnect to the FTP server 
        	// ///////////////////////////////
        	
        	if(ftp != null && ftp.connected())
        		try{
        			ftp.quitImmediately();
        		}catch (Throwable t) {
                    if(LOGGER.isLoggable(Level.FINE))
                		LOGGER.log(Level.FINE,t.getLocalizedMessage(),t);
				}
        }
        
		return res;
	}

    /**
     * Function to put text file.
     * 
     * @param ftpserverHost The remote server host.
     * @param textFile The file name.
     * @param ftpserverUser The user name.
     * @param ftpserverPassword The user password.
     * @param writeMode The write mode.
     * @param connectMode The connection mode.
     * @param timeout The connection timeout.
     * @return If true the upload has been successful.
     */
	public static boolean putTextFileTo(String ftpserverHost, String textFile, String path,
	            String ftpserverUser, String ftpserverPassword, int ftpserverPort, WriteMode writeMode, FTPConnectMode connectMode, int timeout) {
		if(LOGGER.isLoggable(Level.INFO))
			LOGGER.info("[FTP::PutFileTo]: " + "start");
	
	    boolean res = putFile(ftpserverHost, textFile, path, ftpserverUser,
				ftpserverPassword, ftpserverPort,FTPTransferType.ASCII,writeMode,connectMode,timeout); 
	     
	     if(LOGGER.isLoggable(Level.INFO))
	 		LOGGER.info("[FTP::PutFileTo]: " + "end");
	     
	     return res;
	}
	
	/**
	 * This function create a remote directory.
	 * 
	 * @param ftpserverHost The remote server host.
	 * @param binaryFile The remote directory name.
	 * @param path The remote directory path.
	 * @param ftpserverUser The user name.
	 * @param ftpserverPassword The user password.
	 * @param ftpserverPort The remote server port.
	 * @param transferType The transfer type.
	 * @param writeMode The write mode.
	 * @param connectMode The connection mode.
	 * @param timeout The connection timeout.
	 * @return true if the remote directory has successfully created.
	 */
	public static boolean createDirectory(String ftpserverHost, String binaryFile, String path, String ftpserverUser,
			String ftpserverPassword, int ftpserverPort, FTPTransferType transferType, 
			WriteMode writeMode, FTPConnectMode connectMode, int timeout){
		
		boolean res = false;

        String remoteFileName = null;
        FTPClient ftp = null;

        try {
            ftp = connectTo(ftpserverHost, ftpserverUser, ftpserverPassword, 
            		ftpserverPort, transferType, writeMode, connectMode, timeout);
            
            // ///////////////////////////////
            // Get the remote file name
            // ///////////////////////////////
            
            remoteFileName = binaryFile.replaceAll("\\\\", "/");
            remoteFileName = remoteFileName.substring(remoteFileName.lastIndexOf("/") + 1, remoteFileName.length());

            if(LOGGER.isLoggable(Level.INFO))
            		LOGGER.info("[FTP::createDirectory]: " + "Connecting to :" + ftpserverHost + ":" + ftpserverPort);            

            if(LOGGER.isLoggable(Level.INFO))
            		LOGGER.info("[FTP::createDirectory]: " + "sending: " + binaryFile + " to: " + remoteFileName);

            // /////////////////////////////////////////////
            // Checking to change remote working directory 
            // /////////////////////////////////////////////
            
            if(path.indexOf("/") != -1){
            	String[] pathArray = path.split("/");
            	
            	for(int h=0; h<pathArray.length; h++)
            		if(pathArray[h].indexOf("path") != -1) continue;
            		else ftp.chdir(pathArray[h]);
            }
            
            // ////////////////////////////
            // Building the remote directory
            // ////////////////////////////

            ftp.mkdir(remoteFileName); 
            
            res = true;

        }catch (FTPException ftpe) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + ftpe.getLocalizedMessage(),ftpe);
            res = false;
        }catch (IOException ioe) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + ioe.getLocalizedMessage(),ioe);
            res = false;
        }catch (Throwable t) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + t.getLocalizedMessage(),t);
            res = false;
        }finally{
        	
        	// ///////////////////////////////
        	// Disconnect to the FTP server 
        	// ///////////////////////////////
        	
        	if(ftp != null && ftp.connected())
        		try{
        			ftp.quitImmediately();
        		}catch (Throwable t) {
                    if(LOGGER.isLoggable(Level.FINE))
                		LOGGER.log(Level.FINE,t.getLocalizedMessage(),t);
				}
        }
        
		return res;
	}
	
	/**
	 * This function download a file.
	 * 
	 * @param ftpserverHost The remote server host.
	 * @param localPath The local path.
	 * @param remotePath The remote path.
	 * @param remoteFile The remote file name.
	 * @param ftpserverUser The user name.
	 * @param ftpserverPassword The user password.
	 * @param ftpserverPort The remote server port.
	 * @param transferType The transfer type.
	 * @param writeMode The write mode. 
	 * @param connectMode The connection mode.
	 * @param timeout The connection timeout.
	 * @return true if the remote file has been successfully downloaded.
	 */
	public static boolean downloadFile(String ftpserverHost, String localPath, String remotePath, String remoteFile, String ftpserverUser,
			String ftpserverPassword, int ftpserverPort, FTPTransferType transferType, 
			WriteMode writeMode, FTPConnectMode connectMode, int timeout){
		
		boolean res = false;
        FTPClient ftp = null;

        try {
            ftp = connectTo(ftpserverHost, ftpserverUser, ftpserverPassword, 
            		ftpserverPort, transferType, writeMode, connectMode, timeout);

            if(LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("[FTP::downloadFile]: " + "Connecting to :" + ftpserverHost + ":" + ftpserverPort);            

            if(LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("[FTP::downloadFile]: " + "downloading: " + remoteFile + " from: " + remoteFile);

            // /////////////////////////////////////////////
            // Checking to change remote working directory 
            // /////////////////////////////////////////////
            
            if(remotePath.indexOf("/") != -1){
            	String[] pathArray = remotePath.split("/");
            	
            	for(int h=0; h<pathArray.length; h++)
            		if(pathArray[h].indexOf("path") != -1) continue;
            		else ftp.chdir(pathArray[h]);
            }
            
            // ///////////////////////////////
            // Get the remote file 
            // ///////////////////////////////
        	
            ftp.get(localPath, remoteFile);
            
            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("[FTP::downloadFile]: " + "downloaded: " + remoteFile + " from: " + remoteFile);
            
            res = true;
            
        }catch (FTPException ftpe) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + ftpe.getLocalizedMessage(),ftpe);
            res = false;
        }catch (IOException ioe) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + ioe.getLocalizedMessage(),ioe);
            res = false;
        }catch (Throwable t) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + t.getLocalizedMessage(),t);
            res = false;
        }finally{
        	
        	// ///////////////////////////////
        	// Disconnect to the FTP server 
        	// ///////////////////////////////
        	
        	if(ftp != null && ftp.connected())
        		try{
        			ftp.quitImmediately();
        		}catch (Throwable t) {
                    if(LOGGER.isLoggable(Level.FINE))
                		LOGGER.log(Level.FINE,t.getLocalizedMessage(),t);
				}
        }
        
		return res;		
	}
	
	/**
	 * Function to get the remote directory details. 
	 * 
	 * @param ftpserverHost The remote server host.
	 * @param dirName The remote directory name.
	 * @param remotePath The remote directory path.
	 * @param ftpserverUser The user name.
	 * @param ftpserverPassword The user password.
	 * @param ftpserverPort The remote server port.
	 * @param transferType The transfer type.
	 * @param writeMode The write mode.
	 * @param connectMode The connection mode.
	 * @param timeout The connection timeout.
	 * @return The content details of the remote directory.
	 */
	public static FTPFile[] dirDetails(String ftpserverHost, String dirName, String remotePath, String ftpserverUser,
			String ftpserverPassword, int ftpserverPort, FTPTransferType transferType, 
			WriteMode writeMode, FTPConnectMode connectMode, int timeout){
		
		boolean res = false;
		
		FTPFile[] ftpFiles = null;        
        FTPClient ftp = null;

        try {
            ftp = connectTo(ftpserverHost, ftpserverUser, ftpserverPassword, 
            		ftpserverPort, transferType, writeMode, connectMode, timeout);

            if(LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("[FTP::dirDetails]: " + "Connecting to :" + ftpserverHost + ":" + ftpserverPort);
      
            // /////////////////////////////////////////////
            // Checking to change remote working directory 
            // /////////////////////////////////////////////
            
            if(remotePath.indexOf("/") != -1){
            	String[] pathArray = remotePath.split("/");
            	
            	for(int h=0; h<pathArray.length; h++)
            		if(pathArray[h].indexOf("path") != -1) continue;
            		else ftp.chdir(pathArray[h]);
            }
            
            // /////////////////////////////////////
            // Getting the remote directory details 
            // /////////////////////////////////////
            
            ftpFiles = ftp.dirDetails(dirName);

            if(ftpFiles != null)res = true;
            
        }catch (FTPException ftpe) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + ftpe.getLocalizedMessage(),ftpe);
            res = false;
        }catch (IOException ioe) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + ioe.getLocalizedMessage(),ioe);
            res = false;
        }catch (Throwable t) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + t.getLocalizedMessage(),t);
            res = false;
        }finally{
        	
        	// ///////////////////////////////
        	// Disconnect to the FTP server 
        	// ///////////////////////////////
        	
        	if(ftp != null && ftp.connected())
        		try{
        			ftp.quitImmediately();
        		}catch (Throwable t) {
                    if(LOGGER.isLoggable(Level.FINE))
                		LOGGER.log(Level.FINE,t.getLocalizedMessage(),t);
				}
        }
        
        if(res)return ftpFiles;
        else return null;
	}
	
	/**
	 * Function to delete remote file or directory.
	 * 
	 * @param ftpserverHost The remote server host.
	 * @param remoteFile The remote file or directory name.
	 * @param isDir to distinguish file or directory.
	 * @param remotePath The file or directory remote path.
	 * @param ftpserverUser The user name.
	 * @param ftpserverPassword The user password.
	 * @param ftpserverPort The remote server port.
	 * @param connectMode The connection mode.
	 * @param timeout The connection timeout.
	 * @return If true the remote file or directory has been successfully deleted.
	 */
	public static boolean deleteFileOrDirectory(String ftpserverHost, String remoteFile, boolean isDir, String remotePath, String ftpserverUser,
			String ftpserverPassword, int ftpserverPort, FTPConnectMode connectMode, int timeout){
		
		boolean res = false;

        final String host = ftpserverHost;
        final String login = ftpserverUser;
        final String password = ftpserverPassword;
        final int port = ftpserverPort;

        FileTransferClient ftp = null;

        try {
            ftp = new FileTransferClient();
            ftp.setRemoteHost(host);
            ftp.setRemotePort(port);
			ftp.setTimeout(timeout);
			ftp.setPassword(password);
			ftp.setUserName(login);
			
            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Connecting");
            
			ftp.connect();
			
            if(LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("[FTP::deleteFileOrDirectory]: " + "Connecting to :" + host + ":" + port);            

            if(LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("[FTP::deleteFileOrDirectory]: " + "removing: " + remoteFile + " from: " + remoteFile);
        	
            // /////////////////////////////////////////////
            // Checking to change remote working directory 
            // /////////////////////////////////////////////
            
            if(remotePath.indexOf("/") != -1){
            	String[] pathArray = remotePath.split("/");
            	
            	for(int h=0; h<pathArray.length; h++)
            		if(pathArray[h].indexOf("path") != -1) continue;
            		else ftp.changeDirectory(pathArray[h]);
            }
            
            // //////////////////////////////////
            // Deleting remote file or directory
            // //////////////////////////////////
            
            if(isDir)
            	ftp.deleteDirectory(remoteFile);
            else
            	ftp.deleteFile(remoteFile);
            
            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("[FTP::deleteFileOrDirectory]: " + "removed: " + remoteFile + " from: " + remoteFile);
            
            res = true;
            
        }catch (FTPException ftpe) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + ftpe.getLocalizedMessage(),ftpe);
            res = false;
        }catch (IOException ioe) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + ioe.getLocalizedMessage(),ioe);
            res = false;
        }catch (Throwable t) {
        	if (LOGGER.isLoggable(Level.SEVERE))
        		LOGGER.log(Level.SEVERE,"FTP ERROR: " + t.getLocalizedMessage(),t);
            res = false;
        }finally{
        	
        	// ///////////////////////////////
        	// Disconnect to the FTP server 
        	// ///////////////////////////////
        	
        	if(ftp != null && ftp.isConnected())
        		try{
        			ftp.disconnect(true);
        		}catch (Throwable t) {
                    if(LOGGER.isLoggable(Level.FINE))
                		LOGGER.log(Level.FINE,t.getLocalizedMessage(),t);
				}
        }
        
		return res;		
	}
}
