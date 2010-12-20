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
import java.util.zip.GZIPInputStream;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

public final class Files {
    private final static Logger LOGGER = Logger.getLogger(Files.class.toString());
    
    private static int BUFFER_SIZE=1024*8*100; //100 Kbyte
    
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
            byte[] buffer = new byte[BUFFER_SIZE];
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
     * Extract a GZip file to a tar
     * @param in_file the input bz2 file to extract
     * @param out_file the output tar file to extract to
     */
    public static void extractGzip(File in_file, File out_file) throws BuildException{
        FileOutputStream out = null;
        GZIPInputStream zIn = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            out = new FileOutputStream(out_file);
            fis = new FileInputStream(in_file);
            bis = new BufferedInputStream(fis,BUFFER_SIZE);
            zIn = new GZIPInputStream(bis);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count = 0;
            while ((count = zIn.read(buffer, 0, BUFFER_SIZE))!=-1){
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
    
    /**
     * A method to read tar file:
     * extract its content to the 'dest' file (which could be
     * a directory or a file depending on the srcF file content)
     */
    protected static void readTar(File srcF, File dest) {
        
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
                 */
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
                 */
                if (te.isDirectory()) {
                    if (!dest.isDirectory())
                        dest.mkdir();
                    /*
                     * Assume directories are stored parents first then
                     * children.
                     */ 
                    (curr_dest=new File(dest,te.getName())).mkdir();
                }
                
                if (curr_dest!=null){
                    byte[] buf = new byte[BUFFER_SIZE];
                    BufferedOutputStream bos_inner=
                        new BufferedOutputStream(new FileOutputStream(curr_dest),BUFFER_SIZE);
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
    
    /**
     * Return a mediatype describing the input file mime
     * @note require Apache Tika
     * @param File the input file to check
     * 
     */
    public static MediaType getType(File in_file){
        MimeTypes mt=TikaConfig.getDefaultConfig().getMimeRepository();
        MimeType type=mt.getMimeType(in_file);
        return type.getType();
    }
    
    /**
     * @note regex expression are tested using: http://www.regexplanet.com/simple/index.html
     * @param in_name
     * @return
     */
    protected static Matcher match(String in_name){
        final String regex="(.+(?:\\.tar)?)(?<!\\.tar)(\\..+)+";
        //"(.+)((\\.[Zz][Ii][Pp])|((\\.[Tt][Aa][Rr])((\\.[Gg][Zz]([Ii][Pp])?)|(\\.[Bb][Zz]
        // ([Ii][Pp])?2))?)|(\\.[Tt]?[Gg][Zz])|(\\.[Tt][Gg][Zz]([Ii][Pp])?)|(\\.[Tt][Bb][Zz]([Ii][Pp])?2))";
        Pattern p=Pattern.compile(regex);
                
        Matcher m=p.matcher(in_name);
        if (m.matches())
            return m;
        else
            return null;
        
    }
    
    /**
     * check if it is a compressed file, if so it will uncompress the file into the same
     * dir using the input file name as base for the output name
     * @param in_name the name of the file to decompress, it could be:
     * a directory
     * a file
     * a bz2, bzip2, tbz2
     * a gz, gzip, tgz
     * a tar file
     * a zip file
     * @return the output dir where files are extracted
     * @throws Exception 
     */
    public static String uncompress(String in_name) throws Exception{
        File in_file=null;
        if (in_name!=null){
            in_file=new File(in_name);
        }
        else {
            throw new Exception("Uncompress cannot handle null file string");
        }
        // the new file-dir
        File end_file=null;
        // the new file-dir name
        String end_name=in_name;
        

        /**
         * Get type using mime
         * application/x-tar
         * application/x-gzip
         * application/x-bzip2
         * application/zip
         */
        MediaType mt=getType(in_file);
        // subtype will be f.e.: 'tar'
        String type=mt.getSubtype();
        if(LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Input file mime subtype is: "+type);
        if (type.compareTo("x-tar")==0){
            Matcher m=match(in_name);
            if (m!=null){
                // filename
                String fileName=m.group(1);
                if(LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Input file is a tar file.");
                
                // preparing path to extract to
                end_name=fileName;
                end_file=new File(end_name);
                
                // read the tar file into the dir
                readTar(in_file,end_file);
            }
            else
                throw new Exception("File do not match regular expression");
        } // endif tar
        else if (type.compareTo("x-bzip2")==0){
            Matcher m=match(in_name);
            if (m!=null){
                // filename
                String fileName=m.group(1);
                if(LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Input file is a BZ2 compressed file.");
                // the de_compressor output file  
                File tar_file=new File(fileName+".tar");
                // uncompress BZ2 to the tar file
                extractBz2(in_file,tar_file);
                
                if(LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("BZ2 uncompressed to "+tar_file.getAbsolutePath());
                    LOGGER.info("Untarring...");
                }
                
                // preparing path to extract to
                end_name=fileName;
                end_file=new File(end_name);
                
                // read the tar file into the dir
                readTar(tar_file,end_file);
            }
            else
                throw new Exception("File do not match regular expression");
        } // endif bzip2
        else if (type.compareTo("x-gzip")==0){
            Matcher m=match(in_name);
            if (m!=null){
                // filename
                String fileName=m.group(1);
                if(LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Input file is a Gz compressed file.");
                // the de_compressor output file  
                File tar_file=new File(fileName+".tar");
                // uncompress BZ2 to the tar file
                extractGzip(in_file,tar_file);
    
                if(LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("GZ uncompressed to "+tar_file.getAbsolutePath());
                    LOGGER.info("Untarring...");
                }

                // preparing path to extract to
                end_name=fileName;
                end_file=new File(end_name);
                
                // read the tar file into the dir
                readTar(tar_file,end_file);
            }
            else
                throw new Exception("File do not match regular expression");
        }
        else if (type.compareTo("zip")==0){ // its a gzipped or zipped
            Matcher m=match(in_name);
            if (m!=null){
                // filename
                String fileName=m.group(1);
                
                if(LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Input file is a ZIP compressed file. UnZipping...");
                
                // preparing path to extract to
                end_name=fileName;
                end_file=new File(end_name);
                
                //run the unzip method
                IOUtils.unzipFlat(in_file,end_file);
            }
            else
                throw new Exception("File do not match regular expression");
        } // endif gzip or zip
        else if (type.compareTo("x-bzip")==0)
            throw new Exception("bzip format file still not supported! Please try using bz2, gzip or zip");
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
