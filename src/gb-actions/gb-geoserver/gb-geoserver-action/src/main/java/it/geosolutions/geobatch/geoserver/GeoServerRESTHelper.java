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
package it.geosolutions.geobatch.geoserver;

import it.geosolutions.geobatch.tools.file.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Alessio Fabiani
 * @author (r0.2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version 0.2 date: 25/Feb/2011
 * 
 * @deprecated Use geoserver-manager REST library
 */
public class GeoServerRESTHelper {

    static public final String CRS = "crs";

    static public final String NATIVE_MINX = "nminx";

    static public final String NATIVE_MAXX = "nmaxx";

    static public final String NATIVE_MINY = "nminy";

    static public final String NATIVE_MAXY = "nmaxy";

    static public final String LATLON_MINX = "llminx";

    static public final String LATLON_MAXX = "llmaxx";

    static public final String LATLON_MINY = "llminy";

    static public final String LATLON_MAXY = "llmaxx";

    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoServerRESTHelper.class
            .toString());

    public static boolean putBinaryFileTo(URL geoserverREST_URL, InputStream inputStream,
            String geoserverUser, String geoserverPassword, final String[] returnedLayerName) {
        return putBinaryFileTo(geoserverREST_URL, inputStream, geoserverUser, geoserverPassword,
                null, null);
    }

    /**
     * 
     * @param geoserverREST_URL
     * @param inputStream
     * @param geoserverUser
     * @param geoserverPassword
     * @return
     */
    public static boolean putBinaryFileTo(URL geoserverREST_URL, InputStream inputStream,
            String geoserverUser, String geoserverPassword, final String[] returnedLayerName,
            final String contentType) {
        boolean res = false;

        try {
            HttpURLConnection con = (HttpURLConnection) geoserverREST_URL.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("PUT");
            if (contentType != null && contentType.trim().length() > 0)
                con.setRequestProperty("Content-Type", contentType);

            final String login = geoserverUser;
            final String password = geoserverPassword;

            if ((login != null) && (login.trim().length() > 0)) {
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
            }

            OutputStream outputStream = con.getOutputStream();
            copyInputStream(inputStream, outputStream);

            final int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStreamReader is = new InputStreamReader(con.getInputStream());
                String response = readIs(is);
                is.close();
                LOGGER.info("HTTP OK: " + response);
                res = true;
            } else if (responseCode == HttpURLConnection.HTTP_CREATED) {
                InputStreamReader is = new InputStreamReader(con.getInputStream());
                String response = readIs(is);
                is.close();

                extractContent(response, returnedLayerName);
                // if (returnedLayerName!=null && returnedLayerName.length>0)
                // returnedLayerName[0]=name;
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("GeoServerRESTHelper::putBinaryFileTo(): HTTP CREATED: " + response);

                res = true;
            } else {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("GeoServerRESTHelper::putBinaryFileTo(): HTTP ERROR:"
                            + "\nRequestMethod: " + con.getRequestMethod() + "\nResponseMessage: "
                            + con.getResponseMessage() + "\nCode: " + con.getResponseCode()
                            + "\nReadTimeout is (0 return implies that the option is disabled): "
                            + con.getReadTimeout());
                res = false;
            }
        } catch (MalformedURLException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(
                        "GeoServerRESTHelper::putBinaryFileTo(): MalformedURLException: "
                                + e.getLocalizedMessage(), e);
            res = false;
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(
                        "GeoServerRESTHelper::putBinaryFileTo(): IOException: "
                                + e.getLocalizedMessage(), e);
            res = false;
        }
        return res;

    }

    public static boolean putBinaryFileTo(URL geoserverREST_URL, InputStream inputStream,
            String geoserverUser, String geoserverPassword) {
        return putBinaryFileTo(geoserverREST_URL, inputStream, geoserverUser, geoserverPassword,
                null, null);

    }

    /**
     * 
     * @param geoserverREST_URL
     * @param inputStream
     * @param geoserverPassword
     * @param geoserverUser
     * @return
     */
    public static boolean putTextFileTo(URL geoserverREST_URL, InputStream inputStream,
            String geoserverPassword, String geoserverUser, final String[] returnedLayerName) {
        boolean res = false;

        try {
            HttpURLConnection con = (HttpURLConnection) geoserverREST_URL.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("PUT");
            // con.setRequestProperty("Content-Type", "text/xml") ;

            final String login = geoserverUser;
            final String password = geoserverPassword;

            if ((login != null) && (login.trim().length() > 0)) {
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
            }

            InputStreamReader inReq = new InputStreamReader(inputStream);
            OutputStreamWriter outReq = new OutputStreamWriter(con.getOutputStream());
            char[] buffer = new char[1024];
            int len;

            while ((len = inReq.read(buffer)) >= 0)
                outReq.write(buffer, 0, len);

            outReq.flush();
            outReq.close();
            inReq.close();

            final int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStreamReader is = new InputStreamReader(con.getInputStream());
                String response = readIs(is);
                is.close();
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("GeoServerRESTHelper::putTextFileTo(): HTTP OK: " + response);
                res = true;
            } else if (responseCode == HttpURLConnection.HTTP_CREATED) {
                InputStreamReader is = new InputStreamReader(con.getInputStream());
                String response = readIs(is);
                is.close();

                extractContent(response, returnedLayerName);

                if (LOGGER.isInfoEnabled())
                    LOGGER.info("GeoServerRESTHelper::putTextFileTo(): HTTP CREATED: " + response);

                res = true;
            } else {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("GeoServerRESTHelper::putTextFileTo(): HTTP ERROR:"
                            + "\nRequestMethod: " + con.getRequestMethod() + "\nResponseMessage: "
                            + con.getResponseMessage() + "\nCode: " + con.getResponseCode()
                            + "\nReadTimeout is (0 return implies that the option is disabled): "
                            + con.getReadTimeout());
                res = false;
            }
        } catch (MalformedURLException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(
                        "GeoServerRESTHelper::putBinaryFileTo(): MalformedURLException: "
                                + e.getLocalizedMessage(), e);
            res = false;
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(
                        "GeoServerRESTHelper::putBinaryFileTo(): IOException: "
                                + e.getLocalizedMessage(), e);
            res = false;
        }
        return res;
    }

    public static boolean putTextFileTo(URL geoserverREST_URL, InputStream inputStream,
            String geoserverPassword, String geoserverUser) {
        return putTextFileTo(geoserverREST_URL, inputStream, geoserverPassword, geoserverUser, null);
    }

    /**
     * 
     * @param geoserverREST_URL
     * @param content
     * @param geoserverUser
     * @param geoserverPassword
     * @return
     */
    public static boolean putContent(URL geoserverREST_URL, String content, String geoserverUser,
            String geoserverPassword, final String[] returnedLayerName, final String contentType) {
        boolean res = false;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("GeoServerRESTHelper::putContent(): URL is "
                    + geoserverREST_URL.toExternalForm().toString());
        }
        try {
            HttpURLConnection con = (HttpURLConnection) geoserverREST_URL.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("PUT");
            if (contentType != null && contentType.trim().length() > 0)
                con.setRequestProperty("Content-Type", contentType);

            final String login = geoserverUser;
            final String password = geoserverPassword;

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

            final int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStreamReader is = null;
                String response = null;
                try {
                    is = new InputStreamReader(con.getInputStream());
                    response = readIs(is);
                } finally {
                    if (is != null)
                        IOUtils.closeQuietly(is);
                }
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("GeoServerRESTHelper::putContent(): HTTP OK: " + response);
                res = true;
            } else if (responseCode == HttpURLConnection.HTTP_CREATED) {

                InputStreamReader is = null;
                String response = null;
                try {
                    is = new InputStreamReader(con.getInputStream());
                    response = readIs(is);
                } finally {
                    if (is != null)
                        IOUtils.closeQuietly(is);
                }
                if (returnedLayerName != null && response != null)
                    extractContent(response, returnedLayerName);
                // if (returnedLayerName!=null && returnedLayerName.length>0)
                // returnedLayerName[0]=name;
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("GeoServerRESTHelper::putContent(): HTTP CREATED: " + response);

                res = true;
            } else {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("GeoServerRESTHelper::putContent(): HTTP ERROR:"
                            + "\nRequestMethod: " + con.getRequestMethod() + "\nResponseMessage: "
                            + con.getResponseMessage() + "\nCode: " + con.getResponseCode()
                            + "\nReadTimeout is (0 return implies that the option is disabled): "
                            + con.getReadTimeout());
                res = false;
            }
        } catch (MalformedURLException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(
                        "GeoServerRESTHelper::putContent(): MalformedURLException: "
                                + e.getLocalizedMessage(), e);
            res = false;
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(
                        "GeoServerRESTHelper::putContent(): IOException: "
                                + e.getLocalizedMessage(), e);
            res = false;
        }
        return res;
    }

    public static boolean putContent(URL geoserverREST_URL, String content, String geoserverUser,
            String geoserverPassword) {
        return putContent(geoserverREST_URL, content, geoserverUser, geoserverPassword, null, null);
    }

    /**
     * 
     * @param geoserverREST_URL
     * @param inputStream
     * @param geoserverPassword
     * @param geoserverUser
     * @return
     */
    public static boolean postTextFileTo(URL geoserverREST_URL, InputStream inputStream,
            String geoserverPassword, String geoserverUser, final String contentType,
            final String[] returnedLayerName) {
        boolean res = false;

        try {
            HttpURLConnection con = (HttpURLConnection) geoserverREST_URL.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", contentType);

            final String login = geoserverUser;
            final String password = geoserverPassword;

            if ((login != null) && (login.trim().length() > 0)) {
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
            }

            InputStreamReader inReq = null;
            OutputStreamWriter outReq = new OutputStreamWriter(con.getOutputStream());
            if (inputStream != null) {
                inReq = new InputStreamReader(inputStream);
                char[] buffer = new char[1024];
                int len;

                while ((len = inReq.read(buffer)) >= 0)
                    outReq.write(buffer, 0, len);
            } else {
                outReq.write("");
            }
            outReq.flush();
            outReq.close();
            if (inReq != null)
                inReq.close();

            final int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStreamReader is = new InputStreamReader(con.getInputStream());
                String response = readIs(is);
                is.close();
                LOGGER.info("HTTP OK: " + response);
                res = true;
            } else if (responseCode == HttpURLConnection.HTTP_CREATED) {
                InputStreamReader is = new InputStreamReader(con.getInputStream());
                String response = readIs(is);
                is.close();
                final String name = extractName(response);
                extractContent(response, returnedLayerName);
                // if (returnedLayerName!=null && returnedLayerName.length>0)
                // returnedLayerName[0]=name;
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("HTTP CREATED: " + response);
                else if (LOGGER.isInfoEnabled())
                    LOGGER.info("HTTP CREATED: " + name);
                res = true;
            } else {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("GeoServerRESTHelper::postTextFileTo(): HTTP ERROR:"
                            + "\nRequestMethod: " + con.getRequestMethod() + "\nResponseMessage: "
                            + con.getResponseMessage() + "\nCode: " + con.getResponseCode()
                            + "\nReadTimeout is (0 return implies that the option is disabled): "
                            + con.getReadTimeout());
                res = false;
            }
        } catch (MalformedURLException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(
                        "GeoServerRESTHelper::postTextFileTo(): MalformedURLException: "
                                + e.getLocalizedMessage(), e);
            res = false;
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(
                        "GeoServerRESTHelper::postTextFileTo(): IOException: "
                                + e.getLocalizedMessage(), e);
            res = false;
        }
        return res;
    }

    public static boolean postTextFileTo(URL geoserverREST_URL, InputStream inputStream,
            String geoserverPassword, String geoserverUser, String contetType) {
        return postTextFileTo(geoserverREST_URL, inputStream, geoserverPassword, geoserverUser,
                contetType, null);
    }

    // ////////////////////////////////////////////////////////////////////////
    //
    // HELPER METHODS
    //
    // ////////////////////////////////////////////////////////////////////////

    public static String extractName(final String response) {
        String name = "";
        if (response != null && response.trim().length() > 0) {
            final int indexOfNameStart = response.indexOf("<name>");
            final int indexOfNameEnd = response.indexOf("</name>");
            try {
                name = response.substring(indexOfNameStart + 6, indexOfNameEnd);
            } catch (StringIndexOutOfBoundsException e) {
                name = response;
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("GeoServerRESTHelper::extractName(): " + e.getLocalizedMessage(), e);
            }
        }
        return name;
    }

    /**
     * 
     * @param response
     * @param result
     *            will contain the following elements: result[0]: the store name result[1]: the
     *            namespace result[2]: the layername
     */
    private static void extractContent(final String response, final String[] result) {
        if (response != null && response.trim().length() > 0) {
            final int indexOfName1Start = response.indexOf("<name>");
            final int indexOfName1End = response.indexOf("</name>");
            final int indexOfName2Start = response.indexOf("<name>", indexOfName1Start + 1);
            final int indexOfName2End = response.indexOf("</name>", indexOfName2Start + 1);
            final int indexOfWorkspaceStart = response.indexOf("<workspace>");
            try {
                if (indexOfName1Start < indexOfWorkspaceStart) {
                    result[2] = response.substring(indexOfName1Start + 6, indexOfName1End);
                    result[1] = response.substring(indexOfName2Start + 6, indexOfName2End);
                } else {
                    result[1] = response.substring(indexOfName1Start + 6, indexOfName1End);
                    result[2] = response.substring(indexOfName2Start + 6, indexOfName2End);
                }

            } catch (StringIndexOutOfBoundsException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(
                            "GeoServerRESTHelper::extractContent(): StringIndexOutOfBoundsException: "
                                    + e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * 
     * @param in
     * @param out
     */
    private static void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        // TODO!
        in.close();
        out.flush();
        out.close();
    }

    /**
     * 
     * @param is
     * @return
     */
    public static String readIs(InputStreamReader is) {
        char[] inCh = new char[1024];
        StringBuffer input = new StringBuffer();
        int r;

        try {
            while ((r = is.read(inCh)) > 0) {
                input.append(inCh, 0, r);
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("GeoServerRESTHelper::readIs(): " + e.getLocalizedMessage(), e);
        }

        return input.toString();
    }

    /**
     * Send data to GeoServer...
     * 
     * @param inputDataDir
     * @param data
     * @param isVectorialLayer
     * @param geoserverBaseURL
     * @param geoserverUID
     * @param geoserverPWD
     * @param originalCoverageStoreId
     * @param storeFilePrefix
     * @param queryParams
     * @param queryString
     * @param dataTransferMethod
     * @param type
     * @param geoserverVersion
     * @param dataStyles
     * @param defaultStyle
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    public static String[] send(final File data, File datafile, final boolean isVectorialLayer,
            final String geoserverBaseURL, final String geoserverUID, final String geoserverPWD,
            final String originalStoreId, final String storeFilePrefix,
            final Map<String, String> queryParams, final String queryString,
            final String dataTransferMethod, final String type, final String geoserverVersion,
            final List<String> dataStyles, final String defaultStyle)
            throws ParserConfigurationException, IOException, TransformerException {

        if (isVectorialLayer)
            return sendFeature(data, geoserverBaseURL, geoserverUID, geoserverPWD, originalStoreId,
                    storeFilePrefix, queryParams, queryString, dataTransferMethod, type,
                    geoserverVersion, dataStyles, defaultStyle);
        else
            return sendCoverage(data, datafile, geoserverBaseURL, geoserverUID, geoserverPWD,
                    originalStoreId, storeFilePrefix, queryParams, queryString, dataTransferMethod,
                    type, geoserverVersion, dataStyles, defaultStyle);
    }

    /**
     * Send Coverage to GeoServer
     * 
     * @param inputDataDir
     * @param data
     * @param geoserverBaseURL
     * @param timeStamp
     * @param coverageStoreId
     * @param storeFilePrefix
     * @param dataStyles
     * @param defaultStyle
     * @param queryParams
     * @throws TransformerException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static String[] sendCoverage(final File inputDataDir, final File data,
            final String geoserverBaseURL, final String geoserverUID, final String geoserverPWD,
            final String originalCoverageStoreId, final String storeFilePrefix,
            final Map<String, String> queryParams, final String queryString,
            final String dataTransferMethod, final String type, final String geoserverVersion,
            final List<String> dataStyles, final String defaultStyle)
            throws ParserConfigurationException, IOException, TransformerException {
        URL geoserverREST_URL = null;
        boolean sent = false;
        final String coverageStoreId = URLEncoder.encode(originalCoverageStoreId, "UTF-8");
        final String[] layer = new String[3];
        layer[0] = storeFilePrefix != null ? storeFilePrefix : coverageStoreId;
        String layerName = storeFilePrefix != null ? storeFilePrefix : coverageStoreId;
        final String suffix = (queryString != null && queryString.trim().length() > 0) ? "?"
                + queryString : "";

        if (geoserverVersion.equalsIgnoreCase("1.7.x")) {
            if ("DIRECT".equals(dataTransferMethod)) {
                geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL)
                        .append("/rest/folders/").append(coverageStoreId).append("/layers/")
                        .append(layerName).append("/file.").append(type).append(suffix).toString());
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("GeoServerRESTHelper::sendCoverage(): sending binary content to ... "
                            + geoserverREST_URL);
                sent = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL, new FileInputStream(
                        data), geoserverUID, geoserverPWD);
            } else if ("URL".equals(dataTransferMethod)) {
                geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL)
                        .append("/rest/folders/").append(coverageStoreId).append("/layers/")
                        .append(layerName).append("/url.").append(type).append(queryString)
                        .toString());
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("GeoServerRESTHelper::sendCoverage(): sending data "
                            + data.toURL().toExternalForm() + " to ... " + geoserverREST_URL);
                sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data.toURL()
                        .toExternalForm(), geoserverUID, geoserverPWD);
            } else if ("EXTERNAL".equals(dataTransferMethod)) {
                geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL)
                        .append("/rest/folders/").append(coverageStoreId).append("/layers/")
                        .append(layerName).append("/external.").append(type).append(queryString)
                        .toString());
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("GeoServerRESTHelper::sendCoverage(): sending PATH "
                            + data.toURL().toExternalForm() + " to ... " + geoserverREST_URL);
                sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data.toURL()
                        .toExternalForm(), geoserverUID, geoserverPWD);
            }
        } else {
            if ("DIRECT".equals(dataTransferMethod)) {
                geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL)
                        .append("/rest/workspaces/").append(queryParams.get("namespace"))
                        .append("/coveragestores/").append(coverageStoreId).append("/file.")
                        .append(type).append(suffix).toString());
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("GeoServerRESTHelper::sendCoverage(): sending binary content to ... "
                            + geoserverREST_URL);
                sent = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL, new FileInputStream(
                        data), geoserverUID, geoserverPWD, layer, null);
            } else if ("URL".equals(dataTransferMethod)) {
                geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL)
                        .append("/rest/workspaces/").append(queryParams.get("namespace"))
                        .append("/coveragestores/").append(coverageStoreId).append("/url.")
                        .append(type).append(suffix).toString());
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("GeoServerRESTHelper::sendCoverage(): sending data "
                            + data.toURL().toExternalForm() + " to ... " + geoserverREST_URL);
                sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data.toURL()
                        .toExternalForm(), geoserverUID, geoserverPWD, layer, null);
            } else if ("EXTERNAL".equals(dataTransferMethod)) {
                geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL)
                        .append("/rest/workspaces/").append(queryParams.get("namespace"))
                        .append("/coveragestores/").append(coverageStoreId).append("/external.")
                        .append(type).append(suffix).toString());
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("GeoServerRESTHelper::sendCoverage(): sending PATH "
                            + data.toURL().toExternalForm() + " to ... " + geoserverREST_URL);
                sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data.toURL()
                        .toExternalForm(), geoserverUID, geoserverPWD, layer, null);
            }
        }

        if (sent) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("GeoServerRESTHelper::sendCoverage(): coverage SUCCESSFULLY sent to GeoServer");
            configureLayer(queryParams, defaultStyle, geoserverBaseURL, geoserverUID, geoserverPWD,
                    layerName);
            // if (defaultStyle != null && defaultStyle.trim().length()>0)
            // configureStyles(layerName, defaultStyle, dataStyles,
            // geoserverBaseURL, geoserverUID, geoserverPWD);
            return layer;
        } else {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("GeoServerRESTHelper::sendCoverage(): "
                        + "coverage was NOT sent to GeoServer due to connection errors!");
            return null;
        }
    }

    /**
     * Send to GeoServer
     * 
     * @param inputDataDir
     * @param data
     * @param geoserverBaseURL
     * @param timeStamp
     * @param coverageStoreId
     * @param storeFilePrefix
     * @param dataStyles
     * @param defaultStyle
     * @param queryParams
     * @param geoserverVersion2
     * @throws TransformerException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static String[] sendFeature(final File data, final String geoserverBaseURL,
            final String geoserverUID, final String geoserverPWD, final String originalStoreId,
            final String storeFilePrefix, final Map<String, String> queryParams,
            final String queryString, final String dataTransferMethod, final String type,
            final String geoserverVersion, final List<String> dataStyles, final String defaultStyle)
            throws ParserConfigurationException, IOException, TransformerException {
        URL geoserverREST_URL = null;
        boolean sent = false;
        final String datastoreId = URLEncoder.encode(originalStoreId, "UTF-8");
        final String[] layer = new String[3];
        layer[0] = storeFilePrefix != null ? storeFilePrefix : datastoreId;
        String layerName = storeFilePrefix != null ? storeFilePrefix : datastoreId;
        final String suffix = (queryString != null && queryString.trim().length() > 0) ? "?"
                + queryString : "";

        if ("DIRECT".equals(dataTransferMethod)) {
            geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL)
                    .append("/rest/workspaces/").append(queryParams.get("namespace"))
                    .append("/datastores/").append(datastoreId).append("/file.").append(type)
                    .append(suffix).toString());
            FileInputStream inStream = null;
            try {
                inStream = new FileInputStream(data);
                sent = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL, inStream,
                        geoserverUID, geoserverPWD, layer, "application/zip");
            } finally {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (Throwable t) {
                        // eat me;
                    }
                }
            }

        } else if ("URL".equals(dataTransferMethod)) {
            geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL)
                    .append("/rest/workspaces/").append(queryParams.get("namespace"))
                    .append("/datastores/").append(datastoreId).append("/url.").append(type)
                    .append(suffix).toString());
            sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data.toURL().toExternalForm(),
                    geoserverUID, geoserverPWD, layer, null);
        } else if ("EXTERNAL".equals(dataTransferMethod)) {
            geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL)
                    .append("/rest/workspaces/").append(queryParams.get("namespace"))
                    .append("/datastores/").append(datastoreId).append("/external.").append(suffix)
                    .append(type).toString());
            sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data.toURL().toExternalForm(),
                    geoserverUID, geoserverPWD, layer, null);
        }

        if (sent) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("GeoServerRESTHelper::sendFeature(): coverage SUCCESSFULLY sent to GeoServer");

            configureLayer(queryParams, defaultStyle, geoserverBaseURL, geoserverUID, geoserverPWD,
                    layerName);

            // if (defaultStyle != null && defaultStyle.trim().length()>0)
            // configureStyles(layerName, defaultStyle, dataStyles,
            // geoserverBaseURL, geoserverUID, geoserverPWD);
            return layer;
        } else {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("GeoServerRESTHelper::sendFeature(): FeatureType was NOT sent to GeoServer due to connection errors!");
            return null;
        }
    }

    public static void configureLayer(final Map<String, String> queryParams,
            final String defaultStyle, final String geoserverBaseURL, final String geoserverUID,
            final String geoserverPWD, final String layerName) throws ParserConfigurationException,
            IOException, TransformerException {
        Map<String, String> configElements = new HashMap<String, String>(2);
        if (queryParams.containsKey("wmspath")) {
            // Configuring wmsPath
            final String wmsPath = queryParams.get("wmspath");
            configElements.put("path", wmsPath);
        }
        if (defaultStyle != null && defaultStyle.trim().length() > 0) {
            configElements.put("defaultStyle", defaultStyle);
        }
        if (!configElements.isEmpty()) {
            sendLayerConfiguration(configElements, geoserverBaseURL, geoserverUID, geoserverPWD,
                    layerName);
        }

    }

    /**
     * Check if a layer exixts.
     * 
     * @param geoserverBaseURL
     * @param geoserverUID
     * @param geoserverPWD
     * @param layerName
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    public static boolean checkLayerExistence(final String geoserverBaseURL,
            final String geoserverUID, final String geoserverPWD, final String layerName)
            throws ParserConfigurationException, IOException, TransformerException {
        boolean exists = false;

        String layer = URLEncoder.encode(layerName, "UTF-8");
        if (layer.contains("."))
            layer = layer + ".fake";
        final URL geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL)
                .append(!geoserverBaseURL.endsWith("/") ? "/" : "").append("rest/layers/")
                .append(layer).toString());

        HttpURLConnection con = (HttpURLConnection) geoserverREST_URL.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("GET");

        final String login = geoserverUID;
        final String password = geoserverPWD;

        if ((login != null) && (login.trim().length() > 0)) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(login, password.toCharArray());
                }
            });
        }

        final int responseCode = con.getResponseCode();

        exists = (responseCode == HttpURLConnection.HTTP_OK);

        return exists;
    }

    /**
     * Allows to configure some layer attributes such as WmsPath and DefaultStyle
     * 
     * @param configElements
     * @param geoserverBaseURL
     * @param geoserverUID
     * @param geoserverPWD
     * @param layerName
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    private static void sendLayerConfiguration(final Map<String, String> configElements,
            final String geoserverBaseURL, final String geoserverUID, final String geoserverPWD,
            final String layerName) throws ParserConfigurationException, IOException,
            TransformerException {

        String layer = URLEncoder.encode(layerName, "UTF-8");
        if (layer.contains("."))
            layer = layer + ".fake";
        final URL geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL)
                .append("/rest/layers/").append(layer).toString());
        File file = null;
        FileInputStream inStream = null;
        try {
            file = buildXMLConfiguration(configElements);
            inStream = new FileInputStream(file);
            final boolean send = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL, inStream,
                    geoserverUID, geoserverPWD, null, "text/xml");
            if (send) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("GeoServerRESTHelper::sendLayerConfiguration(): Layer SUCCESSFULLY configured");
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("GeoServerRESTHelper::sendLayerConfiguration(): Layer FAILED to be configured");
                }
            }
        } finally {
            if (file != null) {
                try {
                    file.delete();
                } catch (Throwable t) {
                    // Eat me
                }
            }
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Throwable t) {
                    // eat me;
                }
            }

        }

    }

    /**
     * Allows to configure some layer attributes such as WmsPath and DefaultStyle
     * 
     * @param configElements
     * @param geoserverBaseURL
     * @param geoserverUID
     * @param geoserverPWD
     * @param layerName
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    public static void sendCoverageConfiguration(final Map<String, String> coverageElements,
            final Map<String, String> metadataElements, final Map<String, String> configElements,
            final String geoserverBaseURL, final String geoserverUID, final String geoserverPWD,
            final String workspace, final String coverageStore, final String coverageName)
            throws ParserConfigurationException, IOException, TransformerException {

        String coveragest = URLEncoder.encode(coverageStore, "UTF-8");
        final URL geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL)
                .append("/rest/workspaces/").append(workspace).append("/coveragestores/")
                .append(coveragest).append("/coverages/").append(coverageName).append(".xml")
                .toString());
        File file = null;
        FileInputStream inStream = null;
        try {
            file = buildCoverageXMLConfiguration(coverageElements, metadataElements, configElements);
            inStream = new FileInputStream(file);
            final boolean send = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL, inStream,
                    geoserverUID, geoserverPWD, null, "text/xml");
            if (send) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("GeoServerRESTHelper::sendCoverageConfiguration(): Coverage SUCCESSFULLY configured!");
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("GeoServerRESTHelper::sendCoverageConfiguration(): Layer FAILED to be configured");
                }
            }
        } finally {
            if (file != null) {
                try {
                    file.delete();
                } catch (Throwable t) {
                    // Eat me
                }
            }
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Throwable t) {
                    // eat me;
                }
            }

        }

    }

    /**
     * Setup an XML file to be sent via REST to configure the Layer
     * 
     * @param configElements
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    private static File buildXMLConfiguration(final Map<String, String> configElements)
            throws ParserConfigurationException, IOException, TransformerException {
        final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();

        // Get the DocumentBuilder
        DocumentBuilder parser = dfactory.newDocumentBuilder();
        // Create blank DOM Document
        Document doc = parser.newDocument();
        Element root = doc.createElement("layer");
        doc.appendChild(root);

        Set<String> keys = configElements.keySet();
        Element enabledElement = doc.createElement("enabled");
        root.appendChild(enabledElement);
        enabledElement.insertBefore(doc.createTextNode("true"), null);

        for (String key : keys) {
            final String value = configElements.get(key);
            final Element element = doc.createElement(key);
            root.appendChild(element);
            element.insertBefore(doc.createTextNode(value), null);
        }

        final TransformerFactory factory = TransformerFactory.newInstance();
        final Transformer transformer = factory.newTransformer();
        final File file = File.createTempFile("config", ".xml");
        final Result result = new StreamResult(file);
        final Source xmlSource = new DOMSource(doc);
        transformer.transform(xmlSource, result);
        return file;
    }

    /**
     * Setup an XML file to be sent via REST to configure the Layer
     * 
     * @param configElements
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    private static File buildCoverageXMLConfiguration(final Map<String, String> coverageElements,
            final Map<String, String> metadataElements, final Map<String, String> configElements)
            throws ParserConfigurationException, IOException, TransformerException {
        final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();

        // Get the DocumentBuilder
        DocumentBuilder parser = dfactory.newDocumentBuilder();
        // Create blank DOM Document
        Document doc = parser.newDocument();
        Element root = doc.createElement("coverage");
        doc.appendChild(root);

        /*
         * <coverage> ... <nativeBoundingBox> <minx>15.685019493103</minx>
         * <maxx>20.8090171813965</maxx> <miny>39.5162200927734</miny> <maxy>43.3198089599609</maxy>
         * <crs>EPSG:4326</crs> </nativeBoundingBox> <latLonBoundingBox>
         * <minx>15.685019493103</minx> <maxx>20.8090171813965</maxx> <miny>39.5162200927734</miny>
         * <maxy>43.3198089599609</maxy> <crs>EPSG:4326</crs> </latLonBoundingBox> ... </coverage>
         
        Element nativeBB = doc.createElement("nativeBoundingBox");
        root.appendChild(nativeBB);

        Element minx = doc.createElement("minx");
        nativeBB.appendChild(minx);
        minx.insertBefore(doc.createTextNode(coverageElements.get(NATIVE_MINX)), null);

        Element maxx = doc.createElement("maxx");
        nativeBB.appendChild(maxx);
        maxx.insertBefore(doc.createTextNode(coverageElements.get(NATIVE_MAXX)), null);

        Element miny = doc.createElement("miny");
        nativeBB.appendChild(miny);
        miny.insertBefore(doc.createTextNode(coverageElements.get(NATIVE_MINY)), null);

        Element maxy = doc.createElement("maxy");
        nativeBB.appendChild(maxy);
        maxy.insertBefore(doc.createTextNode(coverageElements.get(NATIVE_MAXY)), null);
*/
        
/*
 *         TODO should be taken using optionally:
 *          - the prj                                   *better
 *          - recognized from the file                  *better
 *          - reading nativeCRS from the created layer  *good
 *          - change the config adding another crs      !NO!
 */
//        Element crs = doc.createElement("crs");
//        nativeBB.appendChild(crs);
//        crs.insertBefore(doc.createTextNode(coverageElements.get(CRS)), null);

        Element latLonBB = doc.createElement("latLonBoundingBox");
        root.appendChild(latLonBB);

        Element minx2 = doc.createElement("minx");
        latLonBB.appendChild(minx2);
        minx2.insertBefore(doc.createTextNode(coverageElements.get(LATLON_MINX)), null);

        Element maxx2 = doc.createElement("maxx");
        latLonBB.appendChild(maxx2);
        maxx2.insertBefore(doc.createTextNode(coverageElements.get(LATLON_MAXX)), null);

        Element miny2 = doc.createElement("miny");
        latLonBB.appendChild(miny2);
        miny2.insertBefore(doc.createTextNode(coverageElements.get(LATLON_MINY)), null);

        Element maxy2 = doc.createElement("maxy");
        latLonBB.appendChild(maxy2);
        maxy2.insertBefore(doc.createTextNode(coverageElements.get(LATLON_MAXX)), null);

        Element crs2 = doc.createElement("crs");
        latLonBB.appendChild(crs2);
        crs2.insertBefore(doc.createTextNode(coverageElements.get(CRS)), null);
        
        /*
         * <projectionPolicy>REPROJECT_TO_DECLARED</projectionPolicy>
         */
        Element projectionPolicy= doc.createElement("projectionPolicy");
        root.appendChild(projectionPolicy);
        projectionPolicy.insertBefore(doc.createTextNode("REPROJECT_TO_DECLARED"), null); //TODO
        
        /*
         * <coverage> ... <enabled>true</enabled> ... </coverage>
         */
        Set<String> keys = metadataElements.keySet();
        Element enabledElement = doc.createElement("enabled");
        root.appendChild(enabledElement);
        enabledElement.insertBefore(doc.createTextNode("true"), null);

        // METADATA

        Element parametersElement = doc.createElement("metadata");
        root.appendChild(parametersElement);

        Element entry = doc.createElement("entry");
        entry.setAttribute("key", "timeDimEnabled");
        entry.appendChild(doc.createTextNode(metadataElements.get("timeDimEnabled")));
        parametersElement.appendChild(entry);

        // TODO remove 'dirName'
        entry = doc.createElement("entry");
        entry.setAttribute("key", "dirName");
        entry.appendChild(doc.createTextNode(metadataElements.get("dirName")));
        parametersElement.appendChild(entry);

        entry = doc.createElement("entry");
        entry.setAttribute("key", "timePresentationMode");
        entry.appendChild(doc.createTextNode(metadataElements.get("timePresentationMode")));
        parametersElement.appendChild(entry);

        keys = configElements.keySet();
        parametersElement = doc.createElement("parameters");
        root.appendChild(parametersElement);

        for (String key : keys) {
            entry = doc.createElement("entry");
            parametersElement.appendChild(entry);

            Element string = doc.createElement("string");
            entry.appendChild(string);
            string.insertBefore(doc.createTextNode(key), null);

            Element string2 = doc.createElement("string");
            entry.appendChild(string2);
            final String value = configElements.get(key);
            string2.insertBefore(doc.createTextNode(value), null);
        }

        final TransformerFactory factory = TransformerFactory.newInstance();
        final Transformer transformer = factory.newTransformer();
        final File file = File.createTempFile("config", ".xml");
        final Result result = new StreamResult(file);
        final Source xmlSource = new DOMSource(doc);
        transformer.transform(xmlSource, result);
        return file;
    }

    /**
     * Set the default style and the associable styles for the layer.
     * 
     * @param layerName
     * @param defaultStyle
     *            the name of the style to configure as default style to the layer.
     * @param dataStyles
     *            the names of the styles to associate to the layer.
     * @param gsUrl
     *            Geoserver base URL
     * @param gsUser
     *            Geoserver admin username
     * @param gsPw
     *            Geoserver admin password
     * @return true if there were no errors in setting the styles.
     * @throws java.net.MalformedURLException
     * @throws java.io.FileNotFoundException
     */
    private static boolean configureStyles(String layerName, String defaultStyle,
            List<String> stylesList, String gsUrl, String gsUsername, String gsPassword)
            throws MalformedURLException, FileNotFoundException {

        boolean ret = true;
        URL restUrl = new URL(gsUrl + "/rest/sldservice/updateLayer/" + layerName);

        for (String styleName : stylesList) {
            if (GeoServerRESTHelper.putContent(restUrl, "<LayerConfig><Style>" + styleName
                    + "</Style></LayerConfig>", gsUsername, gsPassword)) {

                LOGGER.info("added style " + styleName + " for layer " + layerName);
            } else {
                LOGGER.warn("error adding style " + styleName + " for layer " + layerName);
                ret = false;
            }
        }

        ret &= GeoServerRESTHelper.putContent(restUrl, "<LayerConfig><DefaultStyle>" + defaultStyle
                + "</DefaultStyle></LayerConfig>", gsUsername, gsPassword);
        return ret;
    }
}
