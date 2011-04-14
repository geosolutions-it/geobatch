/*
 *  Copyright (C) 2007 - 2010 GeoSolutions S.A.S.
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

package it.geosolutions.geobatch.egeos.connection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class RemoteFileCache {

    private final static Logger LOGGER = LoggerFactory.getLogger(RemoteFileCache.class);

    private final Map<URL, File> cache = new HashMap<URL, File>();

    public File get(URL url) {
        File ret = cache.get(url);
        if(ret == null)
            LOGGER.info("URL " + url + " not cached");
        return ret;
    }

    public File add(URL url) {
        File file = getFile(url);
        if(file != null) {
            synchronized(cache) {
                Object old = cache.put(url, file);
                if(old != null)
                    LOGGER.warn("Overwriting URL " + url);
            }
        } else {
            LOGGER.error("URL not added " + url);
        }
        return file;
    }

    private File getFile(URL url) {

        GetMethod httpMethod = null;
        File file = null;
        boolean err = true;

		try {
            HttpClient client = new HttpClient();
//            setAuth(client, url, username, pw);
			httpMethod = new GetMethod(url.toString());
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			int status = client.executeMethod(httpMethod);
			if(status == HttpStatus.SC_OK) {
                InputStream is = httpMethod.getResponseBodyAsStream();
                file = File.createTempFile("cswconn", ".wsdl");
                OutputStream os = FileUtils.openOutputStream(file);
                IOUtils.copy(is, os);
                os.flush();
                os.close();
                is.close();
                err = false;
			} else {
				LOGGER.info("("+status+") " + HttpStatus.getStatusText(status) + " -- " + url );
			}
		} catch (ConnectException e) {
			LOGGER.info("Couldn't connect to ["+url+"]");
		} catch (IOException e) {
			LOGGER.info("Error storing wsdl file ["+url+"]", e);
		} finally {
            if(httpMethod != null)
                httpMethod.releaseConnection();

            try {
                if (err) {
                    if (file != null) {
                        FileUtils.forceDelete(file);

                    }
                } else {
                    FileUtils.forceDeleteOnExit(file);
                }
            } catch (IOException e) {
                LOGGER.info("Error deleting tmp wsdl file", e);
            }
        }

		return err ? null : file;
	}
}
