/*
 *  GeoServer-Manager - Simple Manager Library for GeoServer
 *  
 *  Copyright (C) 2010 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.actions.geonetwork.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Low level HTTP utilities.
 */
public class HTTPUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPUtils.class);

    private final String username;
    private final String pw;

    /**
     * This instance is shared among the various calls, so that the 
     * state (mainly cookies) will be preserved.
     */
    private HttpClient client = new HttpClient();   

    /**
     * Some apps may require application/xml, so you can set it to whatever is needed.
     */
    private String xmlContentType = "text/xml";
    
    private int lastHttpStatus;
    private boolean ignoreResponseContentOnSuccess = false;
    
    public HTTPUtils() {
        this(null, null);
    }

    public HTTPUtils(String userName, String password) {
        this.username = userName;
        this.pw = password;
    }

    public void setXmlContentType(String xmlContentType) {
        this.xmlContentType = xmlContentType;
    }

    public int getLastHttpStatus() {
        return lastHttpStatus;
    }

    public boolean isIgnoreResponseContentOnSuccess() {
        return ignoreResponseContentOnSuccess;
    }

    public void setIgnoreResponseContentOnSuccess(boolean ignoreResponseContentOnSuccess) {
        this.ignoreResponseContentOnSuccess = ignoreResponseContentOnSuccess;
    }
                    
    
    /**
     * Performs an HTTP GET on the given URL.
     *
     * @param url       The URL where to connect to.
     * @return          The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     */
	public  String get(String url) throws MalformedURLException {

        GetMethod httpMethod = null;
		try {            
            setAuth(client, url, username, pw);
			httpMethod = new GetMethod(url);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			lastHttpStatus = client.executeMethod(httpMethod);
			if(lastHttpStatus == HttpStatus.SC_OK) {
                InputStream is = httpMethod.getResponseBodyAsStream();
				String response = IOUtils.toString(is);
				if(response.trim().length()==0) { // sometime gs rest fails
					LOGGER.warn("ResponseBody is empty");
					return null;
				} else {
                    return response;
                }
			} else {
				LOGGER.info("("+lastHttpStatus+") " + HttpStatus.getStatusText(lastHttpStatus) + " -- " + url );
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

    //==========================================================================
    //=== PUT
    //==========================================================================
    
    /**
     * PUTs a String representing an XML document to the given URL.
     *
     * @param url       The URL where to connect to.
     * @param content   The XML content to be sent as a String.
     * @return          The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    public  String putXml(String url, String content) {
        return put(url, content, xmlContentType);
    }
    
    /**
     * PUTs a File to the given URL.
     *
     * @param url       The URL where to connect to.
     * @param file      The File to be sent.
     * @param contentType The content-type to advert in the PUT.
     * @return          The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    public  String put(String url, File file, String contentType) {
        return put(url, new FileRequestEntity(file, contentType));
    }

    /**
     * PUTs a String to the given URL.
     *
     * @param url       The URL where to connect to.
     * @param content   The content to be sent as a String.
     * @param contentType The content-type to advert in the PUT.
     * @return          The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    public  String put(String url, String content, String contentType) {
        try {
            return put(url, new StringRequestEntity(content, contentType, null));
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Cannot PUT " + url, ex);
            return null;
        }
    }

    /**
     * Performs a PUT to the given URL.
     *
     * @param url       The URL where to connect to.
     * @param requestEntity The request to be sent.
     * @return          The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    public  String put(String url, RequestEntity requestEntity) {
        return send(new PutMethod(url), url, requestEntity);
    }

    //==========================================================================
    //=== POST
    //==========================================================================
    
    /**
     * POSTs a String representing an XML document to the given URL.
     *
     * @param url       The URL where to connect to.
     * @param content   The XML content to be sent as a String.
     * @return          The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    public  String postXml(String url, String content) {
        return post(url, content, xmlContentType);
    }
    
    /**
     * POSTs a Stream content representing an XML document to the given URL.
     *
     * @param url       The URL where to connect to.
     * @param content   The content to be sent as an InputStream.
     * @return          The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    public  String postXml(String url, InputStream content) {
        return post(url, content, xmlContentType);
    }
    
    /**
     * POSTs a File to the given URL.
     *
     * @param url       The URL where to connect to.
     * @param file      The File to be sent.
     * @param contentType The content-type to advert in the POST.
     * @return          The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    public  String post(String url, File file, String contentType) {
        return post(url, new FileRequestEntity(file, contentType));
    }

    /**
     * POSTs a String to the given URL.
     *
     * @param url       The URL where to connect to.
     * @param content   The content to be sent as a String.
     * @param contentType The content-type to advert in the POST.
     * @return          The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    public  String post(String url, String content, String contentType) {
        try {
            return post(url, new StringRequestEntity(content, contentType, null));
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Cannot POST " + url, ex);
            return null;
        }
    }
    
    /**
     * POSTs a Stream content to the given URL.
     *
     * @param url       The URL where to connect to.
     * @param content   The content to be sent as an InputStream.
     * @param contentType The content-type to advert in the POST.
     * @return          The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    public  String post(String url, InputStream content, String contentType) {
        return post(url, new InputStreamRequestEntity(content, contentType));
    }

    /**
     * Performs a POST to the given URL.
     * <BR>Basic auth is used if both username and pw are not null.
     *
     * @param url       The URL where to connect to.
     * @param requestEntity The request to be sent.
     * @return          The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    public  String post(String url, RequestEntity requestEntity) {
        return send(new PostMethod(url), url, requestEntity);
    }
    
    //==========================================================================
    //=== HTTP requests
    //==========================================================================

    /**
     * Send an HTTP request (PUT or POST) to a server.
     * <BR>Basic auth is used if both username and pw are not null.
     * <P>
     * Only <UL>
     *  <LI>200: OK</LI>
     *  <LI>201: ACCEPTED</LI>
     *  <LI>202: CREATED</LI>
     * </UL> are accepted as successful codes; in these cases the response string will be returned.
     *
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    protected  String send(final EntityEnclosingMethod httpMethod, String url, RequestEntity requestEntity) {

        try {
            setAuth(client, url, username, pw);

			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            if(requestEntity != null)
                httpMethod.setRequestEntity(requestEntity);
            
			lastHttpStatus = client.executeMethod(httpMethod);

			switch(lastHttpStatus) {
				case HttpURLConnection.HTTP_OK:
				case HttpURLConnection.HTTP_CREATED:
				case HttpURLConnection.HTTP_ACCEPTED:
					LOGGER.info("HTTP "+ httpMethod.getStatusText());
                    if(ignoreResponseContentOnSuccess)
                        return "";
					String response = IOUtils.toString(httpMethod.getResponseBodyAsStream());
					return response;
				default:
					LOGGER.warn("Bad response: code["+lastHttpStatus+"]" +
							" msg["+httpMethod.getStatusText()+"]" +
							" url["+url+"]" +
                            " method["+httpMethod.getClass().getSimpleName()+"]: " +
                            IOUtils.toString(httpMethod.getResponseBodyAsStream())
							);
					return null;
			}
		} catch (ConnectException e) {
			LOGGER.info("Couldn't connect to ["+url+"]");
    		return null;
        } catch (IOException e) {
            LOGGER.error("Error talking to " + url + " : " + e.getLocalizedMessage());
    		return null;
		} finally {
            if(httpMethod != null)
                httpMethod.releaseConnection();
        }
    }

	public  boolean delete(String url) {

    	DeleteMethod httpMethod = null;

		try {
//            HttpClient client = new HttpClient();
            setAuth(client, url, username, pw);
            httpMethod = new DeleteMethod(url);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			lastHttpStatus = client.executeMethod(httpMethod);
			String response = "";
			if(lastHttpStatus == HttpStatus.SC_OK) {
                if(LOGGER.isDebugEnabled())
                    LOGGER.debug("("+lastHttpStatus+") " + httpMethod.getStatusText() + " -- " + url );
                                
                if( ! ignoreResponseContentOnSuccess) {
                    InputStream is = httpMethod.getResponseBodyAsStream();
                    response = IOUtils.toString(is);
                    if(response.trim().equals("")) { 
                        if(LOGGER.isDebugEnabled())
                            LOGGER.debug("ResponseBody is empty (this may be not an error since we just performed a DELETE call)");
                    }
                }
				return true;
			} else {
				LOGGER.info("("+lastHttpStatus+") " + httpMethod.getStatusText() + " -- " + url );
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
	public  boolean httpPing(String url) {

        GetMethod httpMethod = null;

		try {
//			HttpClient client = new HttpClient();
            setAuth(client, url, username, pw);
			httpMethod = new GetMethod(url);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(2000);
			lastHttpStatus = client.executeMethod(httpMethod);
            if(lastHttpStatus != HttpStatus.SC_OK) {
                LOGGER.warn("PING failed at '"+url+"': ("+lastHttpStatus+") " + httpMethod.getStatusText());
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

    /**
     * Used to query for REST resources.
     *
     * @param url The URL of the REST resource to query about.
     * @param username
     * @param pw
     * @return true on 200, false on 404.
     * @throws RuntimeException on unhandled status or exceptions.
     */
	public  boolean exists(String url) {

        GetMethod httpMethod = null;

		try {
//			HttpClient client = new HttpClient();
            setAuth(client, url, username, pw);
			httpMethod = new GetMethod(url);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(2000);
			lastHttpStatus = client.executeMethod(httpMethod);
            switch(lastHttpStatus) {
                case HttpStatus.SC_OK:
                    return true;
                case HttpStatus.SC_NOT_FOUND:
                    return false;
                default:
                    throw new RuntimeException("Unhandled response status at '"+url+"': ("+lastHttpStatus+") " + httpMethod.getStatusText());
            }
		} catch (ConnectException e) {
            throw new RuntimeException(e);
		} catch (IOException e) {
            throw new RuntimeException(e);
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
