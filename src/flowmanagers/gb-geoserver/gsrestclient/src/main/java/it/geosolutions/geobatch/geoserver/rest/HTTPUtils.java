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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
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
		HttpClient client = new HttpClient();
		try {
			GetMethod g = new GetMethod(url);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			int status = client.executeMethod(g);
			if(status == HttpStatus.SC_OK) {
                InputStream is = g.getResponseBodyAsStream();
				String response = IOUtils.toString(is);
				if(response.trim().equals("")) { // sometime gs rest fails
					LOGGER.warn("ResponseBody is empty");
					return null;
				}
				return response;
			} else {
				LOGGER.info("Got " + HttpStatus.getStatusText(status) + "("+status+") for " + url );
			}
		} catch (ConnectException e) {
			LOGGER.info("Couldn't connect to ["+url+"]");
		} catch (IOException e) {
			LOGGER.info("Error talking to ["+url+"]", e);
		}

		return null;
	}

    public static boolean put(URL restUrl, InputStream inputStream,
            String user, String pw) {
        boolean res = false;

        try {
            HttpURLConnection con = (HttpURLConnection) restUrl.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("PUT");

            final String login = user;
            final String password = pw;

            if ((login != null) && (login.trim().length() > 0)) {
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
            }

            OutputStream outputStream = con.getOutputStream();
            IOUtils.copy(inputStream, outputStream);

			switch(con.getResponseCode()) {
				case HttpURLConnection.HTTP_OK:
				case HttpURLConnection.HTTP_CREATED:
				case HttpURLConnection.HTTP_ACCEPTED:
					String response = IOUtils.toString(con.getInputStream());
					LOGGER.info("HTTP "+con.getResponseMessage()+": " + response);
					return true;
				default:
					LOGGER.warn("Bad response from GS: code["+con.getResponseCode()+"]" +
							" msg["+con.getResponseMessage()+"]" +
							" url["+restUrl+"]"
							);
					return false;
			}
        } catch (IOException e) {
            LOGGER.error("Error talking to " + restUrl + " : " + e.getLocalizedMessage());
            res = false;
        }
        return res;

    }

    /**
     * 
     * @param geoserverREST_URL
     * @param content
     * @param geoserverUser
     * @param geoserverPassword
     * @return true if the transaction completed successfully.
     */
    public static boolean put(URL restUrl,
								String content,
								String user, String pw) {
        boolean res = false;

        try {
            HttpURLConnection con = (HttpURLConnection) restUrl.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("PUT");

            final String login = user;
            final String password = pw;

            if ((login != null) && (login.trim().length() > 0)) {
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
            }

            OutputStreamWriter outReq = new OutputStreamWriter(con.getOutputStream());
            outReq.write(content);
            outReq.flush();
            outReq.close();

			switch(con.getResponseCode()) {
				case HttpURLConnection.HTTP_OK:
				case HttpURLConnection.HTTP_CREATED:
				case HttpURLConnection.HTTP_ACCEPTED:
					String response = IOUtils.toString(con.getInputStream());
					LOGGER.info("HTTP OK: " + response);
					res = true;
					break;
				default:
					LOGGER.warn("HTTP ERROR: " + con.getResponseCode()
								+ ":" + con.getResponseMessage()
								+ " -- URL("+restUrl+")");
					res = false;
					break;
            }
        } catch (IOException e) {
            LOGGER.error("Error talking to " + restUrl + " : " + e.getLocalizedMessage());
            res = false;
        }
        return res;

    }

	public static boolean delete(String url, final String user, final String pw) {
		HttpClient client = new HttpClient();

		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, pw.toCharArray());
			}
		});


		try {
			DeleteMethod httpMethod = new DeleteMethod(url);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			int status = client.executeMethod(httpMethod);
			String response = "";
			if(status == HttpStatus.SC_OK) {
                InputStream is = httpMethod.getResponseBodyAsStream();
				response = IOUtils.toString(is);
				if(response.trim().equals("")) { // sometimes gs rest fails
					LOGGER.warn("ResponseBody is empty");
					return true;
				}
				LOGGER.debug("Got " + HttpStatus.getStatusText(status) + "("+status+") for " + url );
				return true;
			} else {
				LOGGER.info("Got " + HttpStatus.getStatusText(status) + "("+status+") for " + url );
				LOGGER.info("'"+response+"'" );
			}
		} catch (ConnectException e) {
			LOGGER.info("Couldn't connect to ["+url+"]");
		} catch (IOException e) {
			LOGGER.info("Error talking to ["+url+"]", e);
		}

		return false;
	}

	public static boolean httpPing(String url) {
		try {
			HttpClient client = new HttpClient();
			GetMethod g = new GetMethod(url);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(2000);
			int status = client.executeMethod(g);
			return status == HttpStatus.SC_OK;

		} catch (ConnectException e) {
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}


}
