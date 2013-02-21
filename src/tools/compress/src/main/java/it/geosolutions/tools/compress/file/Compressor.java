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
import it.geosolutions.tools.commons.file.Path;
import it.geosolutions.tools.io.file.Collector;
import it.geosolutions.tools.io.file.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Compressor {
	final static Logger LOGGER = LoggerFactory.getLogger(Compressor.class
			.toString());

	/**
	 * Zip all the files in the input directory which starts with the provided
	 * prefix.
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
	public static File deflate(final File outputDir,
			final String zipFileBaseName, final File[] files) {
		// Create the ZIP file
		final File outZipFile = new File(outputDir, zipFileBaseName + ".zip");
		if (outZipFile.exists()){
			if (LOGGER.isInfoEnabled())
				LOGGER.info("The output file already exists: "+outZipFile);
			return outZipFile;
		}
		return deflate(outputDir, outZipFile, files, false);
	}
	
	public static File deflate(final File outputDir,
			final File zipFile, final File[] files, boolean overwrite) {
		
		if (zipFile.exists() && overwrite){
			if (LOGGER.isInfoEnabled())
				LOGGER.info("The output file already exists: "+zipFile+" overvriting");
			return zipFile;
		}
		
		// Create a buffer for reading the files
		byte[] buf = new byte[Conf.getBufferSize()];
		
		ZipOutputStream out = null;
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(zipFile);
			bos = new BufferedOutputStream(fos);
			out = new ZipOutputStream(bos);

			// Compress the files
			for (File file : files) {
				FileInputStream in = null;
				try {
					in = new FileInputStream(file);
					if (file.isDirectory()){
						continue;
					} else {
						// Add ZIP entry to output stream.
						out.putNextEntry(new ZipEntry(file.getName()));
						// Transfer bytes from the file to the ZIP file
						int len;
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						out.flush();
					}
					
				} finally {
					try {
						// Complete the entry
						out.closeEntry();
					} catch (Exception e) {
					}
					IOUtils.closeQuietly(in);
				}

			}

		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			return null;
		} finally {

			// Complete the ZIP file
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(bos);
			IOUtils.closeQuietly(fos);
		}

		return zipFile;
	}
	
	/**
	 * @param outputDir
	 *            The directory where the zipfile will be created
	 * @param zipFileBaseName
	 *            The basename of hte zip file (i.e.: a .zip will be appended)
	 * @param folder
	 *            The folder that will be compressed
	 * @return The created zipfile, or null if an error occurred.
	 * @deprecated TODO UNTESTED
	 */
	public static File deflate(final File outputDir,
			final String zipFileBaseName, final File folder) {
		// Create a buffer for reading the files
		byte[] buf = new byte[4096];

		// Create the ZIP file
		final File outZipFile = new File(outputDir, zipFileBaseName + ".zip");
		if (outZipFile.exists()){
			if (LOGGER.isInfoEnabled())
				LOGGER.info("The output file already exists: "+outZipFile);
			return outZipFile;	
		}
		ZipOutputStream out = null;
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outZipFile);
			bos = new BufferedOutputStream(fos);
			out = new ZipOutputStream(bos);
			Collector c=new Collector(null);
			List<File> files=c.collect(folder);
			// Compress the files
			for (File file : files) {
				FileInputStream in = null;
				try {
					in = new FileInputStream(file);
					if (file.isDirectory()){
						out.putNextEntry(new ZipEntry(Path.toRelativeFile(folder,file).getPath()));
					} else {
						// Add ZIP entry to output stream.
						out.putNextEntry(new ZipEntry(FilenameUtils.getBaseName(file
								.getName())));
						// Transfer bytes from the file to the ZIP file
						int len;
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
					}
					
				} finally {
					try {
						// Complete the entry
						out.closeEntry();
					} catch (IOException e) {
					}
					IOUtils.closeQuietly(in);
				}

			}

		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			return null;
		} finally {

			// Complete the ZIP file
			IOUtils.closeQuietly(bos);
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(out);
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
	public static File zip(final File outputDir, final String zipFileBaseName,
			final File[] files) throws IOException {

		if (outputDir != null && files != null && zipFileBaseName != null) {

			// //////////////////////////////////////////
			// Create a buffer for reading the files
			// //////////////////////////////////////////
			final File outZipFile = new File(outputDir, zipFileBaseName
					+ ".zip");
			ZipOutputStream out = null;

			try {

				// /////////////////////////////////
				// Create the ZIP output stream
				// /////////////////////////////////

				out = new ZipOutputStream(new BufferedOutputStream(
						new FileOutputStream(outZipFile)));

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
				if (LOGGER.isErrorEnabled())
					LOGGER.error(e.getLocalizedMessage(), e);
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
	public static void zipDirectory(final File directory, final File base,
			final ZipOutputStream out) throws IOException {

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
						entry = new ZipEntry(base
								.getName()
								.concat("\\")
								.concat(files[i].getPath().substring(
										base.getPath().length() + 1)));
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
				if (LOGGER.isErrorEnabled())
					LOGGER.error(e.getLocalizedMessage(), e);
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
	public static void zipFile(final File file, final ZipOutputStream out)
			throws IOException {

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

				out.putNextEntry(new ZipEntry(FilenameUtils.getName(file
						.getAbsolutePath())));

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
				if (LOGGER.isErrorEnabled())
					LOGGER.error(e.getLocalizedMessage(), e);
				if (out != null)
					out.close();
			} finally {
				if (in != null)
					in.close();
			}

		} else
			throw new IOException("One or more input parameters are null!");
	}

}
