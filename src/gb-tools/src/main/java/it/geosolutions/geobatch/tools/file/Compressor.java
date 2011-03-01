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
package it.geosolutions.geobatch.tools.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;

public final class Compressor {
    private final static Logger LOGGER = Logger.getLogger(Compressor.class.toString());

    /**
     * Zip all the files in the input directory which starts with the provided prefix.
     * 
     * @param inputDir
     *            directory where to get files from.
     * @param zipFilePrefix
     *            prefix to choose files.
     * @return a {@link File} that points to the generated zip file, or null.
     */
    public static File deflate(final File inputDir, final String zipFilePrefix) {
        if (inputDir == null || !inputDir.exists() || !inputDir.isDirectory())
            return null;

        // get files to zip
        final File[] files = inputDir
                .listFiles((FilenameFilter) new PrefixFileFilter(zipFilePrefix));

        return deflate(inputDir, zipFilePrefix, files);
    }

    /**
     * @param outputDir
     *            The directory where the zipfile will be created
     * @param zipFileBaseName
     *            The basename of hte zip file (i.e.: a .zip will be appended)
     * @param files
     *            The files that will be put into the zipfile
     * @return The created zipfile, or null if an error occurred.
     */
    public static File deflate(final File outputDir, final String zipFileBaseName,
            final File[] files) {
        // Create a buffer for reading the files
        byte[] buf = new byte[4096];

        final File outZipFile = new File(outputDir, zipFileBaseName + ".zip");
        try {
            // Create the ZIP file
            final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    new FileOutputStream(outZipFile)));

            // Compress the files
            for (File file : files) {
                final FileInputStream in = new FileInputStream(file);

                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(FilenameUtils.getName(file.getAbsolutePath())));

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                // Complete the entry
                out.closeEntry();
                in.close();
            }

            // Complete the ZIP file
            out.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        }

        return outZipFile;
    }

    /**
     * This function zip the input files.
     * 
     * @param outputDir
     *            The temporary directory where the zip files.
     * @param zipFileBaseName
     *            The name of the zip file.
     * @param files
     *            The array files to zip.
     * @return The zip file or null.
     * @throws IOException
     */
    public static File zip(final File outputDir, final String zipFileBaseName, final File[] files)
            throws IOException {

        if (outputDir != null && files != null && zipFileBaseName != null) {

            // //////////////////////////////////////////
            // Create a buffer for reading the files
            // //////////////////////////////////////////
            final File outZipFile = new File(outputDir, zipFileBaseName + ".zip");
            ZipOutputStream out = null;

            try {

                // /////////////////////////////////
                // Create the ZIP output stream
                // /////////////////////////////////

                out = new ZipOutputStream(
                        new BufferedOutputStream(new FileOutputStream(outZipFile)));

                // /////////////////////
                // Compress the files
                // /////////////////////

                for (File file : files) {
                    if (file.isDirectory()) {
                        zipDirectory(file, file, out);
                    } else {
                        zipFile(file, out);
                    }
                }

                out.close();
                out = null;

                return outZipFile;

            } catch (IOException e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                return null;
            } finally {
                if (out != null)
                    out.close();
            }

        } else
            throw new IOException("One or more input parameters are null!");
    }

    /**
     * This function zip the input directory.
     * 
     * @param directory
     *            The directory to be zipped.
     * @param base
     *            The base directory.
     * @param out
     *            The zip output stream.
     * @throws IOException
     */
    public static void zipDirectory(final File directory, final File base, final ZipOutputStream out)
            throws IOException {

        if (directory != null && base != null && out != null) {
            File[] files = directory.listFiles();
            byte[] buffer = new byte[4096];
            int read = 0;

            FileInputStream in = null;
            ZipEntry entry = null;

            try {
                for (int i = 0, n = files.length; i < n; i++) {
                    if (files[i].isDirectory()) {
                        zipDirectory(files[i], base, out);
                    } else {
                        in = new FileInputStream(files[i]);
                        entry = new ZipEntry(base.getName().concat("\\").concat(
                                files[i].getPath().substring(base.getPath().length() + 1)));
                        out.putNextEntry(entry);

                        while (-1 != (read = in.read(buffer))) {
                            out.write(buffer, 0, read);
                        }

                        // //////////////////////
                        // Complete the entry
                        // //////////////////////

                        out.closeEntry();

                        in.close();
                        in = null;
                    }
                }

            } catch (IOException e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                if (out != null)
                    out.close();
            } finally {
                if (in != null)
                    in.close();
            }

        } else
            throw new IOException("One or more input parameters are null!");
    }

    /**
     * This function zip the input file.
     * 
     * @param file
     *            The input file to be zipped.
     * @param out
     *            The zip output stream.
     * @throws IOException
     */
    public static void zipFile(final File file, final ZipOutputStream out) throws IOException {

        if (file != null && out != null) {

            // ///////////////////////////////////////////
            // Create a buffer for reading the files
            // ///////////////////////////////////////////

            byte[] buf = new byte[4096];

            FileInputStream in = null;

            try {
                in = new FileInputStream(file);

                // //////////////////////////////////
                // Add ZIP entry to output stream.
                // //////////////////////////////////

                out.putNextEntry(new ZipEntry(FilenameUtils.getName(file.getAbsolutePath())));

                // //////////////////////////////////////////////
                // Transfer bytes from the file to the ZIP file
                // //////////////////////////////////////////////

                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                // //////////////////////
                // Complete the entry
                // //////////////////////

                out.closeEntry();


            } catch (IOException e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                if (out != null)
                    out.close();
            } finally {
                if (in != null)
                    in.close();
            }

        } else
            throw new IOException("One or more input parameters are null!");
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
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.isLoggable(Level.FINE);
            }
        }
    
    }


}
