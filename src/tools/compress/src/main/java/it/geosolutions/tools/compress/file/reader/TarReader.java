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
package it.geosolutions.tools.compress.file.reader;

import it.geosolutions.tools.commons.Conf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;

/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public abstract class TarReader{
    
    /**
     * A method to read tar file:
     * extract its content to the 'destDir' file (which could be
     * a directory or a file depending on the srcF file content)
     * @throws CompressorException 
     */
    public static void readTar(File srcF, File destDir) throws Exception {
    	if (destDir==null)
    		throw new IllegalArgumentException("Unable to extract to a null destination dir");
    	if (!destDir.canWrite() && !destDir.mkdirs())
    		throw new IllegalArgumentException("Unable to extract to a not writeable destination dir: "+destDir);

    	FileInputStream fis = null;
        TarArchiveInputStream tis = null;
        BufferedInputStream bis=null;
        try {
            fis = new FileInputStream(srcF);
            bis = new BufferedInputStream(fis);
            tis = new TarArchiveInputStream(bis);

            TarArchiveEntry te = null;
            while ((te = tis.getNextTarEntry()) != null) {
            	
            	File curr_dest=new File(destDir,te.getName());
                if (te.isDirectory()){
                    // create destination folder
                    if (!curr_dest.exists())
                        curr_dest.mkdirs();
                } else {
                    writeFile(curr_dest, tis);
                }
            }
        } finally {
            if (tis!=null){
                try {
                    tis.close();
                } catch (IOException ioe){}
            }
            if (bis!=null){
                try {
                    bis.close();
                } catch (IOException ioe){}
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe){}
            }
            
        }
    }
    
    private static void writeFile(File curr_dest, TarArchiveInputStream tis) throws IOException{
        byte[] buf = new byte[Conf.getBufferSize()];
        FileOutputStream fos=null;
        BufferedOutputStream bos_inner=null;
        try {
            fos=new FileOutputStream(curr_dest);
            bos_inner=new BufferedOutputStream(fos,Conf.getBufferSize());
            int read_sz=0;
            while ((read_sz=tis.read(buf))!=-1){
                bos_inner.write(buf, 0, read_sz);
                bos_inner.flush();
            }
//        } catch (IOException e){
        } finally {
            if (bos_inner!=null){
                try {
                    bos_inner.close();
                } catch (IOException e){}
            }
            if (fos!=null){
                try {
                    fos.close();
                } catch (IOException e){}
            }
        }
    }
    
    /**
     * COMMENTED OUT: This method require ant-1.7.jar
     * 
     * A method to read tar file:
     * extract its content to the 'dest' file (which could be
     * a directory or a file depending on the srcF file content)
     *
    public static void readTar(File srcF, File dest) {
        
        FileInputStream fis = null;
        TarInputStream tis = null;
        try {
            fis = new FileInputStream(srcF);
            tis = new TarInputStream(new BufferedInputStream(fis));
            TarEntry te = null;
            
            // actual output file name
            File curr_dest=null;
            
            while ((te = tis.getNextEntry()) != null) {
                /*
                 * check if the first file name in the tar 
                 * is the same as the 'dest' one, if so,
                 * it could be a single file.
                 * Otherwise it's a directory.
                 *
                if (te.getName().compareTo(dest.getName())!=0){
                    if (!dest.isDirectory())
                        dest.mkdir();
                    curr_dest=new File(dest,te.getName());
                }
                else {
                    curr_dest=dest;
                }
                    
                /*
                 * if content file is a sub dir dest is
                 * a directory containing subdirs 
                 
                if (te.isDirectory()) {
                    if (!dest.isDirectory())
                        dest.mkdir();
                    /*
                     * Assume directories are stored parents first then
                     * children.
                     
                    (curr_dest=new File(dest,te.getName())).mkdir();
                }
                
                if (curr_dest!=null){
                    byte[] buf = new byte[Conf.getBufferSize()];
                    BufferedOutputStream bos_inner=
                        new BufferedOutputStream(new FileOutputStream(curr_dest),Conf.getBufferSize());
                    int read_sz=0;
                    while ((read_sz=tis.read(buf))!=-1){
                        bos_inner.write(buf, 0, read_sz);
                    }
                    bos_inner.flush();
                    bos_inner.close();
                    curr_dest=null;
                }
            }
        } catch (IOException ioe) {
            throw new BuildException("Error while expanding " + srcF.getPath()+
                    "\nMessage is: "+ioe.getLocalizedMessage());
        } finally {
            FileUtils.close(tis);
            if (tis == null) {
                FileUtils.close(fis);
            }
            
        }
    }
*/
}
