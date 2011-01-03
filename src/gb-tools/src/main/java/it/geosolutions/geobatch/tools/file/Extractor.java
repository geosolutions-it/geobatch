/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.tools.file;


import it.geosolutions.geobatch.tools.Conf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
/*
 * COMMENTED OUT: require ant-1.7.jar
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.bzip2.CBZip2InputStream;
*/

/**
 * A Class container for Extracotrs methods.
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public final class Extractor {
    private final static Logger LOGGER = Logger.getLogger(Extractor.class.toString());
    
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
     */
    public static List<File> unzipFlat(final File zipFile, final File destDir) {
        if (!destDir.isDirectory())
            throw new IllegalArgumentException("Not a directory '" + destDir.getAbsolutePath()
                    + "'");

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
            LOGGER.log(Level.WARNING, "Error unzipping file '" + zipFile.getAbsolutePath() + "'",e);
            return null;
        }
    }
    
    /**
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     * 
     */
    public static void extractBz2(File in_file, File out_file) throws CompressorException{
        FileOutputStream out = null;
        BZip2CompressorInputStream zIn = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            out = new FileOutputStream(out_file);
            fis = new FileInputStream(in_file);
            bis = new BufferedInputStream(fis);
/*            int b = bis.read();
            if (b != 'B') {
                throw new CompressorException("Invalid bz2 file: "+in_file.getAbsolutePath());
            }
            b = bis.read();
            if (b != 'Z') {
                throw new CompressorException("Invalid bz2 file: "+in_file.getAbsolutePath());
            }
            */
            zIn = new BZip2CompressorInputStream(bis);
            byte[] buffer = new byte[Conf.getBufferSize()];
            int count = 0;
            do {
                out.write(buffer, 0, count);
                count = zIn.read(buffer, 0, buffer.length);
            } while (count != -1);
        }
        catch (IOException ioe) {
            String msg = "Problem expanding bzip2 " + ioe.getMessage();
            throw new CompressorException(msg+in_file.getAbsolutePath());
        }
        finally {
            try {
                if (bis!=null)
                    bis.close();
            }
            catch (IOException ioe){
                throw new CompressorException("Error closing stream: "+in_file.getAbsolutePath());
            }
            try {
                if (fis!=null)
                    fis.close();
            }
            catch (IOException ioe){
                throw new CompressorException("Error closing stream: "+in_file.getAbsolutePath());
            }
            try {
                if (out!=null)
                    out.close();
            }
            catch (IOException ioe){
                throw new CompressorException("Error closing stream: "+in_file.getAbsolutePath());
            }
            try {
                if (zIn!=null)
                    zIn.close();
            }
            catch (IOException ioe){
                throw new CompressorException("Error closing stream: "+in_file.getAbsolutePath());
            }
        }
    }

    /**
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     * 
     * Extract a GZip file to a tar
     * @param in_file the input bz2 file to extract
     * @param out_file the output tar file to extract to
     */
    public static void extractGzip(File in_file, File out_file) throws CompressorException{
        FileOutputStream out = null;
        GZIPInputStream zIn = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            out = new FileOutputStream(out_file);
            fis = new FileInputStream(in_file);
            bis = new BufferedInputStream(fis,Conf.getBufferSize());
            zIn = new GZIPInputStream(bis);
            byte[] buffer = new byte[Conf.getBufferSize()];
            int count = 0;
            while ((count = zIn.read(buffer, 0, Conf.getBufferSize()))!=-1){
                out.write(buffer, 0, count);
            }
        } catch (IOException ioe) {
            String msg = "Problem uncompressing Gzip " + ioe.getMessage();
            throw new CompressorException(msg+in_file.getAbsolutePath());
        } finally {
            try {
                if (bis!=null)
                    bis.close();
            }
            catch (IOException ioe){
                throw new CompressorException("Error closing stream: "+in_file.getAbsolutePath());
            }
            try {
                if (fis!=null)
                    fis.close();
            }
            catch (IOException ioe){
                throw new CompressorException("Error closing stream: "+in_file.getAbsolutePath());
            }
            try {
                if (out!=null)
                    out.close();
            }
            catch (IOException ioe){
                throw new CompressorException("Error closing stream: "+in_file.getAbsolutePath());
            }
            try {
                if (zIn!=null)
                    zIn.close();
            }
            catch (IOException ioe){
                throw new CompressorException("Error closing stream: "+in_file.getAbsolutePath());
            }
        }
    }

    /**
     * COMMENTED OUT: This method require ant-1.7.jar
     * 
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     * 
     * Extract a BZ2 file to a tar
     * @param in_file the input bz2 file to extract
     * @param out_file the output tar file to extract to
     *
    public static void extractBz2(File in_file, File out_file) throws BuildException{
        FileOutputStream out = null;
        BZip2InputStream zIn = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            out = new FileOutputStream(out_file);
            fis = new FileInputStream(in_file);
            bis = new BufferedInputStream(fis);
            int b = bis.read();
            if (b != 'B') {
                throw new BuildException("Invalid bz2 file: "+in_file.getAbsolutePath());
            }
            b = bis.read();
            if (b != 'Z') {
                throw new BuildException("Invalid bz2 file: "+in_file.getAbsolutePath());
            }
            zIn = new CBZip2InputStream(bis);
            byte[] buffer = new byte[Conf.getBufferSize()];
            int count = 0;
            do {
                out.write(buffer, 0, count);
                count = zIn.read(buffer, 0, buffer.length);
            } while (count != -1);
        } catch (IOException ioe) {
            String msg = "Problem expanding bzip2 " + ioe.getMessage();
            throw new BuildException(msg+in_file.getAbsolutePath());
        } finally {
            FileUtils.close(bis);
            FileUtils.close(fis);
            FileUtils.close(out);
            FileUtils.close(zIn);
        }
    }
    */
    
    /**
     * COMMENTED OUT: This method require ant-1.7.jar
     * 
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     * 
     * Extract a GZip file to a tar
     * @param in_file the input bz2 file to extract
     * @param out_file the output tar file to extract to
     
    public static void extractGzip(File in_file, File out_file) throws BuildException{
        FileOutputStream out = null;
        GZIPInputStream zIn = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            out = new FileOutputStream(out_file);
            fis = new FileInputStream(in_file);
            bis = new BufferedInputStream(fis,Conf.getBufferSize());
            zIn = new GZIPInputStream(bis);
            byte[] buffer = new byte[Conf.getBufferSize()];
            int count = 0;
            while ((count = zIn.read(buffer, 0, Conf.getBufferSize()))!=-1){
                out.write(buffer, 0, count);
            }
        } catch (IOException ioe) {
            String msg = "Problem uncompressing Gzip " + ioe.getMessage();
            throw new BuildException(msg+in_file.getAbsolutePath());
        } finally {
            FileUtils.close(bis);
            FileUtils.close(fis);
            FileUtils.close(out);
            FileUtils.close(zIn);
        }
    }
    */
    
}
