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

import it.geosolutions.geobatch.tools.file.reader.TarReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Extract {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(Extract.class);
    
    
    private static MimeTypes mimeTypes=TikaConfig.getDefaultConfig().getMimeRepository();

    // compile the pattern
    private static Pattern p;
    
    static {
        /*
         * PATTERN
         */
        try {
            /*
             * The regular expression to compile use
             * ahead and backward substitution...
             */
            //p=compile("(.+(?:\\.tar)?)(?<!\\.tar)(\\..+)*"); // do not match simple dir name
            //p=compile("(.+)(?:(\\..+))(?<!$2)"); // do not match simple dir name [more general]
            //p=compile("(.+)(?:\\.(.+))");// do not match simple (not dotted) dir name [simpler]
            p=compile("(.+)(?:(\\..+))$|(.+)$"); // MATCH simple (not dotted) dir name in group(3)
        }
        catch (Exception e){
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Exception while initializing extractor regex pattern!");
        }
    } 
    
    /**
     * Try to compile the passed regex throwing an error if the
     * @param regex
     * @return the compiled Pattern
     * @throws Exception if the regex is empty or null
     * @throws PatternSyntaxException if the expression's syntax is invalid
     */
    private static Pattern compile(String regex)throws Exception{
        if (regex!=null && regex.length()>0){
            try {
                // compile the pattern
                return Pattern.compile(regex);
            }
            catch( PatternSyntaxException pse){
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(pse.getLocalizedMessage());    
                throw pse;
            }
        }
        else{
            final String message="Unable to compile the passed regular expression: \'"+regex+"\'";
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new Exception(message);
        }
    }
    
    /**
     * 
     * @param m the matcher
     * @return The name of the matching file or null (if name group do not match)
     */
    private static String getName(Matcher m){
        String ret=m.group(1);
        if (ret!=null)
            return ret;
        else
            return m.group(3);
    }
    
    /**
     * @note regex expression are tested using: http://www.regexplanet.com/simple/index.html
     * @param in_name
     * @return the matcher if it match the string
     * @see regex expression for group matching
     */
    protected static Matcher match(String in_name){
        // get the matcher
        Matcher m=p.matcher(in_name);
        // matches?
        if (m.matches()){
            return m;
        }
        else
            return null;
    }
    
    /**
     * Return a mediatype describing the input file mime
     * @note require Apache Tika
     * @param File the input file to check
     * 
     */
    public static MediaType getType(File in_file){
        MimeType type=mimeTypes.getMimeType(in_file);
        return type.getType();
    }
    
    /**
     * Mark the handled type
     * @see getEnumType
     * @see extract
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     *
     */
    static enum TYPE {
        NORMAL, // not tarred not compressed
        TAR,
        TAR_GZ,
        TAR_BZ2,
        ZIP, // not tarred
        GZIP, // not tarred
        BZIP2, // not tarred
        OTHER // UNSUPPORTED FORMAT
    };
    
    /**
     * @TODO this should be implemented using TIKA magic recognizing
     * to do so you have to implement Extractors as Tika.Parser
     * 
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     * 
     * Get type using mime
     * application/tar
     * application/x-gzip
     * application/x-bzip2
     * application/zip
     * @throws Exception 
     */
    private static TYPE getEnumType(final File in_file, boolean accurate) throws Exception{
        
            
            String type=null;
            
            if (accurate){
                /*
                 * https://issues.apache.org/jira/browse/TIKA-75
                 * We have a MimeUtils method that returns a MIME type based 
                 * solely on the name. It would be very helpful to also have
                 * a method that examines the header as well. I've added a method
                 * (patch coming) that does this. It opens a stream from the URL,
                 * reads the header, closes the stream, and then calls the existing method.
                 * This may not be usable in the course of parsing, since it violates
                 *  our decision to read a stream only once. However, it is very useful
                 *  as a way to test our MIME type determination, and as a non-parse
                 *  service to our users (as recently discussed on the forum).
                 *  Keith R. Bennett
                 */
                type=mimeTypes.getType(in_file.toURL());
            }
            else {
                final MediaType mt=getType(in_file);
                // subtype will be f.e.: 'tar'
                type=mt.getType();
            }
            
            if(LOGGER.isInfoEnabled()) {
                LOGGER.info("File type is: "+type);
            }
            if (type==null)
                throw new NullPointerException("Extract.getEnumType: returned type is null");
            
            if (type.compareTo("application/x-tar")==0){
                return TYPE.TAR;
            }
            else if (type.compareTo("application/x-gtar")==0){
                return TYPE.TAR;
            }
            else if (type.compareTo("application/x-bzip2")==0){
                return TYPE.BZIP2;
            }
            else if (type.compareTo("application/x-gzip")==0){
                return TYPE.GZIP;
            }
            else if (type.compareTo("application/zip")==0){ // its a gzipped or zipped
                return TYPE.ZIP;
            }
            else if (type.compareTo("application/x-bzip")==0){
                return TYPE.OTHER;  // UNSUPPORTED FORMAT
            }
            else
                return TYPE.NORMAL; // not tarred not compressed 
        
    }
    
    /**
     * @warning read the todo note
     * @TODO fix zip file extraction to make possible to extract not only 'simple' zip file
     * 
     * Check if it is a compressed file, if so it will uncompress the file into the same
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
    public static String extract(String in_name) throws Exception{
        File in_file=null;
        if (in_name!=null){
            in_file=new File(in_name);
            if (!in_file.exists())
                throw new FileNotFoundException("The path do not match to a file in the filesystem");
        }
        else {
            throw new Exception("Extract: cannot open null file path string");
        }
        // the new file-dir
        File end_file=null;
        // the new file-dir name
        String end_name=null;
        
        Matcher m=match(in_file.getAbsolutePath());
        if (m!=null){
            switch (getEnumType(in_file,true)){
                case TAR:
                    if(LOGGER.isInfoEnabled())
                        LOGGER.info("Input file is a tar file.");
                    
                    File tar_file=null;
                    if (in_name.endsWith("tar")){
                        tar_file=new File(in_name);
                        // filename
                        end_name=getName(m);
                        end_file=new File(end_name);
                    }
                    else {
                        tar_file=new File(in_name+".tar");
                        // filename
                        end_name=getName(m);
                        end_file=new File(in_name);
                    }
                    
                    // rename tar_file to filename.tar
                    if (!in_file.renameTo(tar_file)){
                        if(LOGGER.isInfoEnabled()) {
                            LOGGER.info("Unable to rename file to "+end_name+".tar");
                        }
                        throw new Exception("Unable to rename file to "+end_name+".tar");
                    }
                        
                    if(LOGGER.isInfoEnabled()) {
                        LOGGER.info("Untarring...");
                    }
                    
                    TarReader.readTar(tar_file,end_file);
                    
                    if(LOGGER.isInfoEnabled()) {
                        LOGGER.info("tar extracted to "+end_file.getAbsolutePath());
                    }
                    
                    end_name=extract(end_name);
                    
                break;
                case BZIP2:
                    if(LOGGER.isInfoEnabled())
                        LOGGER.info("Input file is a BZ2 compressed file.");

                    // Output filename
                    end_name=getName(m);
                    end_file=new File(end_name);
                    
                    // uncompress BZ2 to the tar file
                    Extractor.extractBz2(in_file,end_file);
                    
                    if(LOGGER.isInfoEnabled()) {
                        LOGGER.info("BZ2 uncompressed to "+end_file.getAbsolutePath());
                    }
                    
                    end_name=extract(end_name);
                break;
                case GZIP:
                    if(LOGGER.isInfoEnabled())
                        LOGGER.info("Input file is a Gz compressed file.");
                    
                    // Output filename
                    end_name=getName(m);
                    end_file=new File(end_name);
                    
                    // uncompress BZ2 to the tar file
                    Extractor.extractGzip(in_file,end_file);
        
                    if(LOGGER.isInfoEnabled()) {
                        LOGGER.info("GZ uncompressed to "+end_name);
                    }
                    
                    // recursion
                    end_name=extract(end_name);

                break;
                case ZIP:
                    
                    if(LOGGER.isInfoEnabled())
                        LOGGER.info("Input file is a ZIP compressed file. UnZipping...");
                    
                    // preparing path to extract to
                    end_name=getName(m);
                    //end_file=new File(end_name);
                    
    // TODO: if it is a zip file of a simple file???
    //see what is done for tar gz
                    
//                    if (!end_file.isDirectory())
//                        end_file.mkdir();
                    
                    //run the unzip method
//                    Extractor.unzipFlat(in_file,end_file);
                    Extractor.unZip(in_name,end_name);
                    
                    if(LOGGER.isInfoEnabled()) {
                        LOGGER.info("Zip file uncompressed to "+end_name);
                    }
                    
                    // recursion
                    end_name=extract(end_name);
                break;
                case NORMAL:
                    
                    end_name=m.group(0);
                    
                    if(LOGGER.isInfoEnabled())
                        LOGGER.info("Working on a not compressed file.");
                    break;
                default:
                    throw new Exception("bzip format file still not supported! Please try using bz2, gzip or zip");
            } //switch
        } // if match
        else {
            throw new Exception("File do not match regular expression");
        }
        
        /**
         * returning output file name
         */
        return end_name;
    }
}
