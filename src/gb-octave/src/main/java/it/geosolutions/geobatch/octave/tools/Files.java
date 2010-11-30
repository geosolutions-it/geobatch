package it.geosolutions.geobatch.octave.tools;

import it.geosolutions.geobatch.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

public final class Files {
    private final static Logger LOGGER = Logger.getLogger(Files.class.toString());
    
    /**
     * Extract a BZ2 file to a tar
     * @param in_file the input bz2 file to extract
     * @param out_file the output tar file to extract to
     */
    public static void extractBz2(File in_file, File out_file) throws BuildException{
        FileOutputStream out = null;
        CBZip2InputStream zIn = null;
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
            byte[] buffer = new byte[8 * 1024];
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
    
    /**
     * A method to read tar file
     */
    protected static void readTar(File srcF, File dir) {
        
        FileInputStream fis = null;
        TarInputStream tis = null;
        try {
            fis = new FileInputStream(srcF);
            tis = new TarInputStream(new BufferedInputStream(fis));
            TarEntry te = null;
            while ((te = tis.getNextEntry()) != null) {
                if (te.isDirectory()) {
                    // Assume directories are stored parents first then
                    // children.
                    (new File(dir, te.getName())).mkdir();
                }
                else {
                    int size=(int) te.getSize();
                    byte[] buf = new byte[size];
                    File newFile = new File(dir, te.getName());
                    BufferedOutputStream bos_inner=new BufferedOutputStream(
                            new FileOutputStream(newFile),size);
                    while ((size = tis.read(buf)) >= 0){
                        bos_inner.write(buf, 0, size);
                    }
                    bos_inner.flush();
                    bos_inner.close();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new BuildException("Error while expanding " + srcF.getPath());
        } finally {
            FileUtils.close(tis);
            if (tis == null) {
                FileUtils.close(fis);
            }
            
        }
    }
    
    /**
     * check if it is a compressed file, if so it will uncompress the file into the same
     * dir using the input file name as base for the output name
     * @param in_name the name of the file to decompress, it could be:
     * a directory
     * a bz2 file
     * a gz file
     * a tar file
     * a zip file
     * @return the output dir where files are extracted
     * @note regex expression are tested using:
     * http://www.regexplanet.com/simple/index.html
     * Pattern p=Pattern.compile("(.+)((\\.[Zz][Ii][Pp])|((\\.[Tt][Aa][Rr])((\\.[Gg][Zz]([Ii][Pp])?)|(\\.[Bb][Zz]([Ii][Pp])?2))?))");
     * Test Target String   matches()                                     group(0)                             group(1)                  gr(2)   gr(3)   gr(4)      gr(5)   gr(6)
     * 1    /NETTUNO_CNMCA_2010092000.tar.bz2    Yes      Yes     Yes     /NETTUNO_CNMCA_2010092000.tar.bz2    /NETTUNO_CNMCA_2010092000 .tar.bz2        .tar.bz2   .tar    .bz2
     * 2    /NETTUNO_CNMCA_2010092000.tar        Yes      Yes     Yes     /NETTUNO_CNMCA_2010092000.tar        /NETTUNO_CNMCA_2010092000 .tar            .tar       .tar
     * 3    /NETTUNO_CNMCA_2010092000.tar.gz     Yes      Yes     Yes     /NETTUNO_CNMCA_2010092000.tar.gz     /NETTUNO_CNMCA_2010092000 .tar.gz         .tar.gz    .tar    .gz
     * 4    /NETTUNO_CNMCA_2010092000.zip        Yes      Yes     Yes     /NETTUNO_CNMCA_2010092000.zip        /NETTUNO_CNMCA_2010092000 .zip    .zip
     * 5    /NETTUNO_CNMCA_2010092000.tar.bzip2  Yes      Yes     Yes     /NETTUNO_CNMCA_2010092000.tar.bzip2  /NETTUNO_CNMCA_2010092000 .tar.bzip2      .tar.bzip2 .tar    .bzip2
     */
    public static String uncompress(String in_name){
        File in_file=new File(in_name);
        // the new file-dir
        File end_file=null;
        // the new file-dir name
        String end_name=in_name;
        
        Pattern p=Pattern.compile("(.+)((\\.[Zz][Ii][Pp])|((\\.[Tt][Aa][Rr])((\\.[Gg][Zz]([Ii][Pp])?)|(\\.[Bb][Zz]([Ii][Pp])?2))?))");
        Matcher m=p.matcher(in_name);
        
        if (m.matches()){ // we have a compressed file
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Input file is a compressed file.");
            // filename
            String fileName=m.group(1);
            // is a zip or a tar
//                        boolean tar=(m.group(5)!="")?true:false;
            // is a tar (gz | gzip | bz2 | bzip2)
            boolean compr_tar=(m.group(6)!="")?true:false;
            // store (.gz | .gzip | .bz2 | .bzip2)
//                        String type=m.group(6);
            // is a gz|gzip
            boolean gz=(m.group(7)!="")?true:false;
            // is a bz2|bzip2
            boolean bz2=(m.group(9)!="")?true:false;
            
            
            if (compr_tar){// implicit -> (tar && compr_tar) 
                File tar_file=null;
                
                if (bz2){
                    if(LOGGER.isLoggable(Level.INFO))
                        LOGGER.info("Input file is a BZ2 compressed file.");
                    tar_file=new File(fileName+".tar");
                    // uncompress BZ2
                    extractBz2(in_file,tar_file);
                    if(LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("BZ2 uncompressed to "+tar_file.getAbsolutePath());
                        LOGGER.info("Untarring...");
                    }
                    // preparing path to extract to
                    end_name=fileName+File.separator;
                    end_file=new File(end_name);
                    // make the output dir
                    end_file.mkdir();
                    // read the tar file into the dir
                    readTar(tar_file,end_file);
                    
                }
                else if (gz){ // its a gzipped
                    if(LOGGER.isLoggable(Level.INFO))
                        LOGGER.info("Input file is a GZ compressed file.");
                    tar_file=in_file;

                    // preparing path to extract to
                    end_name=fileName+File.separator;
                    end_file=new File(end_name);
                    // make the output dir
                    end_file.mkdir();
                    if(LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("UnZipping...");
                    }
                    //run the unzip method
                    IOUtils.unzipFlat(tar_file,end_file);
                }
            }
            else {
                // preparing path to extract to
                end_name=fileName+File.separator;
                end_file=new File(end_name);
                // make the output dir
                end_file.mkdir();
                if(LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("UnZipping...");
                }
                // run the unzip method
                IOUtils.unzipFlat(in_file,end_file);
            }
        }
        else {
            if(LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Working on a not compressed file.");
        }

        /**
         * returning output file name
         */
        return end_name;
    }
}
