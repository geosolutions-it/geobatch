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
package it.geosolutions.geobatch.geoserver.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.logging.Level;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Low level HTTP utilities.
 *
 * <P><B>TODO</B>: use apache.commons classes in all methods.
 */
public class HTTPUtils {
    private static final Logger LOGGER = Logger.getLogger(HTTPUtils.class);

	public static String get(String url) throws MalformedURLException {
        return get(url, null, null);
    }

	public static String get(String url, String username, String pw) throws MalformedURLException {

        GetMethod httpMethod = null;
		try {
            HttpClient client = new HttpClient();
            setAuth(client, url, username, pw);
			httpMethod = new GetMethod(url);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			int status = client.executeMethod(httpMethod);
			if(status == HttpStatus.SC_OK) {
                InputStream is = httpMethod.getResponseBodyAsStream();
				String response = IOUtils.toString(is);
				if(response.trim().equals("")) { // sometime gs rest fails
					LOGGER.warn("ResponseBody is empty");
					return null;
				} else {
                    return response;
                }
			} else {
				LOGGER.info("("+status+") " + HttpStatus.getStatusText(status) + " -- " + url );
			}
		} catch (ConnectException e) {
			LOGGER.info("Couldn't connect to ["+url+"]");
		} catch (IOException e) {
			LOGGER.info("Error talking to ["+url+"]", e);
		} finally {
            if(httpMethod != null)
                httpMethod.releaseConnection();
        }

		return null;
	}

    public static boolean put(String url, File file, String contentType, String username, String pw) {
        return put(url, new FileRequestEntity(file, contentType), username, pw);
    }

    public static boolean put(String url, String content, String contentType, String username, String pw) {
        try {
            return put(url, new StringRequestEntity(content, contentType, null), username, pw);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Cannot PUT " + url, ex);
            return false;
        }
    }

    public static boolean putXml(String url, String content, String username, String pw) {
        return put(url, content, "text/xml", username, pw);
    }

    public static boolean put(String url, RequestEntity requestEntity, String username, String pw) {
        return send(new PutMethod(url), url, requestEntity, username, pw);
    }



    public static boolean post(String url, String content, String contentType, String username, String pw) {
        try {
            return post(url, new StringRequestEntity(content, contentType, null), username, pw);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Cannot POST " + url, ex);
            return false;
        }
    }

    public static boolean postXml(String url, String content, String username, String pw) {
        return post(url, content, "text/xml", username, pw);
    }

    public static boolean post(String url, RequestEntity requestEntity, String username, String pw) {
        return send(new PostMethod(url), url, requestEntity, username, pw);
    }

    private static boolean send(final EntityEnclosingMethod httpMethod, String url, RequestEntity requestEntity, String username, String pw) {
        boolean res = false;

        try {
            HttpClient client = new HttpClient();
            setAuth(client, url, username, pw);
//			httpMethod = new PutMethod(url);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            if(requestEntity != null)
                httpMethod.setRequestEntity(requestEntity);
			int status = client.executeMethod(httpMethod);

			switch(status) {
				case HttpURLConnection.HTTP_OK:
				case HttpURLConnection.HTTP_CREATED:
				case HttpURLConnection.HTTP_ACCEPTED:
					String response = IOUtils.toString(httpMethod.getResponseBodyAsStream());
					LOGGER.info("HTTP "+ httpMethod.getStatusText()+": " + response);
					return true;
				default:
					LOGGER.warn("Bad response: code["+status+"]" +
							" msg["+httpMethod.getStatusText()+"]" +
							" url["+url+"]"
							);
					return false;
			}
		} catch (ConnectException e) {
			LOGGER.info("Couldn't connect to ["+url+"]");
        } catch (IOException e) {
            LOGGER.error("Error talking to " + url + " : " + e.getLocalizedMessage());
            res = false;
		} finally {
            if(httpMethod != null)
                httpMethod.releaseConnection();
        }
        return res;
    }

	public static boolean delete(String url, final String user, final String pw) {

    	DeleteMethod httpMethod = null;

		try {
            HttpClient client = new HttpClient();
            setAuth(client, url, user, pw);
            httpMethod = new DeleteMethod(url);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			int status = client.executeMethod(httpMethod);
			String response = "";
			if(status == HttpStatus.SC_OK) {
                InputStream is = httpMethod.getResponseBodyAsStream();
				response = IOUtils.toString(is);
				if(response.trim().equals("")) { // sometimes gs rest fails
					LOGGER.info("ResponseBody is empty (this may be not an error since we just performed a DELETE call)");
					return true;
				}
				LOGGER.debug("("+status+") " + httpMethod.getStatusText() + " -- " + url );
				return true;
			} else {
				LOGGER.info("("+status+") " + httpMethod.getStatusText() + " -- " + url );
				LOGGER.info("Response: '"+response+"'" );
			}
		} catch (ConnectException e) {
			LOGGER.info("Couldn't connect to ["+url+"]");
		} catch (IOException e) {
			LOGGER.info("Error talking to ["+url+"]", e);
		} finally {
            if(httpMethod != null)
                httpMethod.releaseConnection();
        }

		return false;
	}

    /**
     * @return true if the server response was an HTTP_OK
     */
	public static boolean httpPing(String url) {
        return httpPing(url, null, null);
    }

	public static boolean httpPing(String url, String username, String pw) {

        GetMethod httpMethod = null;

		try {
			HttpClient client = new HttpClient();
            setAuth(client, url, username, pw);
			httpMethod = new GetMethod(url);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(2000);
			int status = client.executeMethod(httpMethod);
            if(status != HttpStatus.SC_OK) {
                LOGGER.warn("PING failed at '"+url+"': ("+status+") " + httpMethod.getStatusText());
                return false;
            } else {
                return true;
            }

		} catch (ConnectException e) {
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
            if(httpMethod != null)
                httpMethod.releaseConnection();
        }
	}


    private static void setAuth(HttpClient client, String url, String username, String pw) throws MalformedURLException {
        URL u = new URL(url);
        if(username != null && pw != null) {
            Credentials defaultcreds = new UsernamePasswordCredentials(username, pw);
            client.getState().setCredentials(new AuthScope(u.getHost(), u.getPort()), defaultcreds);
            client.getParams().setAuthenticationPreemptive(true); // GS2 by default always requires authentication
        } else {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not setting credentials to access to " + url);
            }
        }
    }

}
