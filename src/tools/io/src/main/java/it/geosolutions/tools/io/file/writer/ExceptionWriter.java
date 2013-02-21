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
package it.geosolutions.tools.io.file.writer;

import it.geosolutions.tools.io.file.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public final class ExceptionWriter {

	/**
	 * Append the StackTrace to a file
	 * 
	 * @param file
	 * @param inExc
	 * @throws Exception
	 *             problem writing the file (its cause is initted with the
	 *             input passed Exception)
	 */
	public static void appendStack(final File file, final Exception inExc)
			throws Exception {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			StackTraceElement[] stackTrace=inExc.getStackTrace();
			for (StackTraceElement element: stackTrace){
				bw.append(element.toString());
				bw.newLine();
			}
			bw.flush();
		} catch (Exception e) {
			e.initCause(inExc);
			throw e;
		} finally {
			if (bw !=null){
				IOUtils.closeQuietly(bw);
			}
			if (fw != null) {
				IOUtils.closeQuietly(fw);
			}
		}
	}
}
