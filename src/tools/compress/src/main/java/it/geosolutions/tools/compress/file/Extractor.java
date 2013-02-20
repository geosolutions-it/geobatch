/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.compress.file;

import it.geosolutions.tools.commons.Conf;
import it.geosolutions.tools.io.file.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * COMMENTED OUT: require ant-1.7.jar
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.util.FileUtils;
 import org.apache.tools.bzip2.CBZip2InputStream;
 */

/**
 * A Class container for Extractors methods.
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public final class Extractor {
    private final static Logger LOGGER = LoggerFactory.getLogger(Extractor.class.toString());

    /**
     * Unzips the files from a zipfile into a directory. All of the files will be put in a single
     * direcotry. If the zipfile contains a hierarchycal structure, it will be ignored.
     * 
     * @param zipFile
     *            The zipfile to be examined
     * @param destDir
     *            The direcotry where the extracted files will be stored.
     * @return The list of the extracted files, or null if an error occurred.
     * @throws IllegalArgumentException
     *             if the destination dir is not writeable.
     * @deprecated use Extractor.unZip instead which support complex zip structure
     */
    public static List<File> unzipFlat(final File zipFile, final File destDir) {
        // if (!destDir.isDirectory())
        // throw new IllegalArgumentException("Not a directory '" + destDir.getAbsolutePath()
        // + "'");

        if (!destDir.canWrite())
            throw new IllegalArgumentException("Unwritable directory '" + destDir.getAbsolutePath()
                    + "'");

        try {
            List<File> ret = new ArrayList<File>();
            ZipInputStream zipinputstream = new ZipInputStream(new FileInputStream(zipFile));

            for (ZipEntry zipentry = zipinputstream.getNextEntry(); zipentry != null; zipentry = zipinputstream
                    .getNextEntry()) {
                String entryName = zipentry.getName();
                if (zipentry.isDirectory())
                    continue;

                File outFile = new File(destDir, entryName);
                ret.add(outFile);
                FileOutputStream fileoutputstream = new FileOutputStream(outFile);

                org.apache.commons.io.IOUtils.copy(zipinputstream, fileoutputstream);
                fileoutputstream.close();
                zipinputstream.closeEntry();
            }

            zipinputstream.close();
            return ret;
        } catch (Exception e) {
            LOGGER.warn("Error unzipping file '" + zipFile.getAbsolutePath() + "'", e);
            return null;
        }
    }

    /**
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     * 
     * @param inputZipName
     *            - the input zip file
     * @param outputDirName
     *            - a directory with this name containing zip files will be created
     * 
     * @throws IOException
     * @throws CompressorException
     */
    public static void unZip(String inputZipName, String outputDirName) throws IOException,
    CompressorException {
        if (inputZipName==null || outputDirName==null) {
            throw new CompressorException("Unzip: with null parameters");
        }
        
    	final File outputDir = new File(outputDirName);
        final File inputZipFile = new File(inputZipName);
        
    	unZip(inputZipFile, outputDir);
    }
    
    public static void unZip(File inputZipFile, File outputDir) throws IOException,
    	CompressorException {
        if (inputZipFile==null || outputDir==null) {
            throw new CompressorException("Unzip: with null parameters");
        }
        
        if (!outputDir.exists()){
        	if (!outputDir.mkdirs()){
                throw new CompressorException("Unzip: Unable to create directory structure: "
                        + outputDir);
        	}
        }
    	
        final int BUFFER = Conf.getBufferSize();
        
        ZipInputStream zipInputStream=null;
        try {
            // Open Zip file for reading
            zipInputStream = new ZipInputStream(new FileInputStream(inputZipFile));
        } catch (FileNotFoundException fnf) {
            throw new CompressorException("Unzip: Unable to find the input zip file named: "
                    + inputZipFile);
        }
        
        // extract file if not a directory
        BufferedInputStream bis = new BufferedInputStream(zipInputStream);
        // grab a zip file entry
        ZipEntry entry = null;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            // Process each entry

            File currentFile = new File(outputDir,entry.getName());                
                
            FileOutputStream fos = null;
            BufferedOutputStream dest = null;
            try {
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                fos = new FileOutputStream(currentFile);
                dest = new BufferedOutputStream(fos, BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = bis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
            } catch (IOException ioe) {

            } finally {
                try {
                    if (dest != null) {
                        dest.flush();
                        dest.close();
                    }
                    if (fos != null)
                        fos.close();
                } catch (IOException ioe) {
                    throw new CompressorException("Unzip: unable to close the zipInputStream: "
                            + ioe.getLocalizedMessage());
                }
            }
        }
        try {
            if (zipInputStream!=null)
                zipInputStream.close();
        } catch (IOException ioe) {
            throw new CompressorException("Unzip: unable to close the zipInputStream: "
                    + ioe.getLocalizedMessage());
        }
        try {
            if (bis!=null)
                bis.close();
        } catch (IOException ioe) {
            throw new CompressorException("Unzip: unable to close the Buffered zipInputStream: "
                    + ioe.getLocalizedMessage());
        }
    }

    /**
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     * 
     */
    public static void extractBz2(File in_file, File out_file) throws CompressorException {
        FileOutputStream out = null;
        BZip2CompressorInputStream zIn = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            out = new FileOutputStream(out_file);
            fis = new FileInputStream(in_file);
            bis = new BufferedInputStream(fis);
            /*
             * int b = bis.read(); if (b != 'B') { throw new
             * CompressorException("Invalid bz2 file: "+in_file.getAbsolutePath()); } b =
             * bis.read(); if (b != 'Z') { throw new
             * CompressorException("Invalid bz2 file: "+in_file.getAbsolutePath()); }
             */
            zIn = new BZip2CompressorInputStream(bis);
            byte[] buffer = new byte[Conf.getBufferSize()];
            int count = 0;
            do {
                out.write(buffer, 0, count);
                count = zIn.read(buffer, 0, buffer.length);
            } while (count != -1);
        } catch (IOException ioe) {
            String msg = "Problem expanding bzip2 " + ioe.getMessage();
            throw new CompressorException(msg + in_file.getAbsolutePath());
        } finally {
            try {
                if (bis != null)
                    bis.close();
            } catch (IOException ioe) {
                throw new CompressorException("Error closing stream: " + in_file.getAbsolutePath());
            }
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException ioe) {
                throw new CompressorException("Error closing stream: " + in_file.getAbsolutePath());
            }
            try {
                if (out != null)
                    out.close();
            } catch (IOException ioe) {
                throw new CompressorException("Error closing stream: " + in_file.getAbsolutePath());
            }
            try {
                if (zIn != null)
                    zIn.close();
            } catch (IOException ioe) {
                throw new CompressorException("Error closing stream: " + in_file.getAbsolutePath());
            }
        }
    }

    /**
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     * 
     *         Extract a GZip file to a tar
     * @param in_file
     *            the input bz2 file to extract
     * @param out_file
     *            the output tar file to extract to
     */
    public static void extractGzip(File in_file, File out_file) throws CompressorException {
        FileOutputStream out = null;
        GZIPInputStream zIn = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            out = new FileOutputStream(out_file);
            fis = new FileInputStream(in_file);
            bis = new BufferedInputStream(fis, Conf.getBufferSize());
            zIn = new GZIPInputStream(bis);
            byte[] buffer = new byte[Conf.getBufferSize()];
            int count = 0;
            while ((count = zIn.read(buffer, 0, Conf.getBufferSize())) != -1) {
                out.write(buffer, 0, count);
            }
        } catch (IOException ioe) {
            String msg = "Problem uncompressing Gzip " + ioe.getMessage()+" ";
            throw new CompressorException(msg + in_file.getAbsolutePath());
        } finally {
            try {
                if (bis != null)
                    bis.close();
            } catch (IOException ioe) {
                throw new CompressorException("Error closing stream: " + in_file.getAbsolutePath());
            }
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException ioe) {
                throw new CompressorException("Error closing stream: " + in_file.getAbsolutePath());
            }
            try {
                if (out != null)
                    out.close();
            } catch (IOException ioe) {
                throw new CompressorException("Error closing stream: " + in_file.getAbsolutePath());
            }
            try {
                if (zIn != null)
                    zIn.close();
            } catch (IOException ioe) {
                throw new CompressorException("Error closing stream: " + in_file.getAbsolutePath());
            }
        }
    }

	/**
	 * Inflate the provided {@link ZipFile} in the provided output directory.
	 * 
	 * @param archive
	 *            the {@link ZipFile} to inflate.
	 * @param outputDirectory
	 *            the directory where to inflate the archive.
	 * @throws IOException
	 *             in case something bad happens.
	 * @throws FileNotFoundException
	 *             in case something bad happens.
	 */
	public static void inflate(ZipFile archive, File outputDirectory, String fileName)
	        throws IOException, FileNotFoundException {
	
	    final Enumeration<? extends ZipEntry> entries = archive.entries();
	    try {
	        while (entries.hasMoreElements()) {
	            ZipEntry entry = (ZipEntry) entries.nextElement();
	
	            if (!entry.isDirectory()) {
	                final String name = entry.getName();
	                final String ext = FilenameUtils.getExtension(name);
	                final InputStream in = new BufferedInputStream(archive.getInputStream(entry));
	                final File outFile = new File(outputDirectory,
	                        fileName != null ? new StringBuilder(fileName).append(".").append(ext)
	                                .toString() : name);
	                final OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
	
	                IOUtils.copyStream(in, out, true, true);
	            }
	        }
	    } finally {
	        try {
	            archive.close();
	        } catch (Throwable e) {
	            if (LOGGER.isTraceEnabled())
	                LOGGER.error("unable to close archive.\nMessage is: "+e.getMessage(),e);
	        }
	    }
	
	}

    /**
     * COMMENTED OUT: This method require ant-1.7.jar
     * 
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     * 
     *         Extract a BZ2 file to a tar
     * @param in_file
     *            the input bz2 file to extract
     * @param out_file
     *            the output tar file to extract to
     * 
     *            public static void extractBz2(File in_file, File out_file) throws BuildException{
     *            FileOutputStream out = null; BZip2InputStream zIn = null; FileInputStream fis =
     *            null; BufferedInputStream bis = null; try { out = new FileOutputStream(out_file);
     *            fis = new FileInputStream(in_file); bis = new BufferedInputStream(fis); int b =
     *            bis.read(); if (b != 'B') { throw new
     *            BuildException("Invalid bz2 file: "+in_file.getAbsolutePath()); } b = bis.read();
     *            if (b != 'Z') { throw new
     *            BuildException("Invalid bz2 file: "+in_file.getAbsolutePath()); } zIn = new
     *            CBZip2InputStream(bis); byte[] buffer = new byte[Conf.getBufferSize()]; int count
     *            = 0; do { out.write(buffer, 0, count); count = zIn.read(buffer, 0, buffer.length);
     *            } while (count != -1); } catch (IOException ioe) { String msg =
     *            "Problem expanding bzip2 " + ioe.getMessage(); throw new
     *            BuildException(msg+in_file.getAbsolutePath()); } finally { FileUtils.close(bis);
     *            FileUtils.close(fis); FileUtils.close(out); FileUtils.close(zIn); } }
     */

    /**
     * COMMENTED OUT: This method require ant-1.7.jar
     * 
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     * 
     *         Extract a GZip file to a tar
     * @param in_file
     *            the input bz2 file to extract
     * @param out_file
     *            the output tar file to extract to
     * 
     *            public static void extractGzip(File in_file, File out_file) throws BuildException{
     *            FileOutputStream out = null; GZIPInputStream zIn = null; FileInputStream fis =
     *            null; BufferedInputStream bis = null; try { out = new FileOutputStream(out_file);
     *            fis = new FileInputStream(in_file); bis = new
     *            BufferedInputStream(fis,Conf.getBufferSize()); zIn = new GZIPInputStream(bis);
     *            byte[] buffer = new byte[Conf.getBufferSize()]; int count = 0; while ((count =
     *            zIn.read(buffer, 0, Conf.getBufferSize()))!=-1){ out.write(buffer, 0, count); } }
     *            catch (IOException ioe) { String msg = "Problem uncompressing Gzip " +
     *            ioe.getMessage(); throw new BuildException(msg+in_file.getAbsolutePath()); }
     *            finally { FileUtils.close(bis); FileUtils.close(fis); FileUtils.close(out);
     *            FileUtils.close(zIn); } }
     */

}
