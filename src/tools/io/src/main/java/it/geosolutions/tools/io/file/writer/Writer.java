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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public final class Writer {

	/**
	 * Open 'destination' file in append mode and append content of the
	 * 'toAppend' file
	 * 
	 * @param toAppend
	 * @param destination
	 * @throws IOException
	 */
	public static void appendFile(File toAppend, File destination)
			throws IOException {
		FileWriter fw = null;
		BufferedWriter bw = null;
		LineIterator it = null;
		try {
			fw = new FileWriter(destination, true);
			bw = new BufferedWriter(fw);
			it = FileUtils.lineIterator(toAppend);
			while (it.hasNext()) {
				bw.append(it.nextLine());
				bw.newLine();
			}
			bw.flush();
		} finally {
			if (it != null) {
				it.close();
			}
			if (bw !=null){
				IOUtils.closeQuietly(bw);
			}
			if (fw != null) {
				IOUtils.closeQuietly(fw);
			}
		}
	}
}
