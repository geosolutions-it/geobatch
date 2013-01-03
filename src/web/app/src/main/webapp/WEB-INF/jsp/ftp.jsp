<%
/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
%>
<%@ page contentType="text/html" import="java.sql.*, java.io.*, java.util.*, it.geosolutions.geobatch.catalog.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title wicket:id="pageTitle">GeoBatch</title>
      <link href="img/favicon.ico" rel="shortcut icon"/>
      <link rel="stylesheet" href="css/blueprint/screen.css" type="text/css" media="screen, projection" />
      <link rel="stylesheet" href="css/blueprint/print.css" type="text/css" media="print" />
	  <link rel="stylesheet" href="css/app.css" type="text/css" media="screen, projection" />
      <!--[if IE]>
        <link rel="stylesheet" href="css/blueprint/ie.css" type="text/css" media="screen, projection" />
	    <link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection" />
      <![endif]-->
</head>
<body>
  <div id="header">
    <div class="wrap">
     <h2><a class="pngfix" href="index.html"><span>GeoBatch</span></a></h2>
      <div class="button-group selfclear">


      </div>
    </div><!-- /.wrap -->
  </div><!-- /#header -->
  <div id="main">
    <div class="wrap selfclear">
    <!--div id="sidebar">


    </div--><!-- /#sidebar -->
    <div id="page" class="selfclear">
      <div class="page-header">

        <div class="header-panel"></div>
        <p>
        	<img src="img/manageFTP-small.png" style="vertical-align: middle"/>
        	<a href="ftp.do?view=users"><img src="img/group.png" title="Manage users" alt="Manage users" style="vertical-align: middle"/></a>
        	<a href="j_spring_security_logout"><img src="img/logout.png" title="Logout" alt="Logout" width="40" height="40" style="vertical-align: middle"/></a>
        </p>
        <br /><br />

        <P/><h2>FTP Server status</h2>
        <c:if test="${errMsg!=null}">
            <div class="feedbackPanelWARNING">
                <c:out value="${errMsg}"/>
            </div>
        </c:if>

		<table width="100%" border="0" cellpadding="2" cellspacing="1">
			<thead>
				<tr bgcolor="black" style="color: white;">
					<th width="60%">STATUS</th>
					<th width="40%">ACTIONS</th>
				</tr>
			</thead>
			<tbody>
				<tr >
                    <c:choose>
                        <c:when test="${ftpServer.stopped}">
                            <td>
                                <b style="color: red">STOPPED</b>
                                (suspended:<c:out value="${ftpServer.suspended}"/>, stopped:<c:out value="${ftpServer.stopped}"/>)</td>
                            <td>
                                <a href='ftp.do?action=start&view=status'><image src='img/control_play.png' border='0' title='start/resume instance' alt='start' width='16' height='16'/></a>
                            </td>
                        </c:when>
                        <c:when test="${ftpServer.suspended}">
                            <td><b style="color: orange">SUSPENDED</b>
                                (suspended:<c:out value="${ftpServer.suspended}"/>, stopped:<c:out value="${ftpServer.stopped}"/>)</td>
                            <td>
                                <a href='ftp.do?action=start&view=status'><image src='img/control_play.png' border='0' title='start/resume instance' alt='start' width='16' height='16'/></a>
                                <a href='ftp.do?action=stop&view=status'><image src='img/control_stop.png' border='0' title='Stop instance' alt='Stop' width='16' height='16'/></a>
                            </td>
                        </c:when>
                        <c:otherwise>
                            <td><b  style="color: green">RUNNING</b>
                                (suspended:<c:out value="${ftpServer.suspended}"/>, stopped:<c:out value="${ftpServer.stopped}"/>)</td>
                            <td>
                                <a href='ftp.do?action=pause&view=status'><image src='img/control_pause.png' border='0' title='Pause instance' alt='Pause' width='16' height='16'/></a>
                                <a href='ftp.do?action=stop&view=status'><image src='img/control_stop.png' border='0' title='Stop instance' alt='Stop' width='16' height='16'/></a>
                            </td>
                        </c:otherwise>
                    </c:choose>


                </tr>
			</tbody>
		</table>

        <P/>
        <h2>
        	FTP Server config
	        <c:choose>
				<c:when test="${ftpServer.stopped}">
					<a href="ftpConfig.form"><img src="img/hammer_screwdriver.png" title="Configure" alt="Configure" style="vertical-align: middle"/></a>
				</c:when>
			</c:choose>
        </h2>
		<table width="100%" border="0" cellpadding="2" cellspacing="1">
			<thead>
				<tr bgcolor="black" style="color: white;">
					<th width="10%">PORT</th>
					<th width="10%">IMPLICIT SSL</th>
					<th width="10%">MAX LOGINS</th>
					<th width="10%">MAX LOGIN FAILURES</th>
					<th width="10%">LOGIN FAILURE DELAY</th>
					<th width="10%">ANONYMOUS ENABLED</th>
					<th width="10%">MAX ANONYM LOGINS</th>
					<th width="10%">AUTO-START</th>
				</tr>
			</thead>
			<tbody>
				<tr >
					<td><c:out value="${ftpConfig.port}"/></td>
					<td><c:out value="${ftpConfig.ssl}"/></td>
					<td><c:out value="${ftpConfig.maxLogins}"/></td>
					<td><c:out value="${ftpConfig.maxLoginFailures}"/></td>
					<td><c:out value="${ftpConfig.loginFailureDelay}"/></td>
					<td><c:out value="${ftpConfig.anonEnabled}"/></td>
					<td><c:out value="${ftpConfig.maxAnonLogins}"/></td>
					<td><c:out value="${ftpConfig.autoStart}"/></td>
				</tr>
			</tbody>
		</table>

        <P/>
        <h2>FTP Server stats</h2>
        Start time <c:out value="${ftpStats.startTime}"/>
        <br/>
        <table class="statsTable" border="0" cellpadding="2" cellspacing="1" >
			<thead>
				<tr bgcolor="black" style="color: white;">
                    <th></th>
                    <th></th>
                    <th></th>
                    <th></th>
				</tr>
			</thead>
			<tbody>
                <tr>
                    <td>Total uploaded files</td>
                    <td><c:out value="${ftpStats.totalUploadNumber}"/></td>
                    <td>Total uploaded bytes</td>
                    <td><c:out value="${ftpStats.totalUploadSize}"/></td>
                </tr>
				<tr>
                    <td>Total downloaded files</td>
                    <td><c:out value="${ftpStats.totalDownloadNumber}"/></td>
                    <td>Total downloaded bytes</td>
                    <td><c:out value="${ftpStats.totalDownloadSize}"/></td>
                </tr>
				<tr>
                    <td>Total connections</td>
                    <td><c:out value="${ftpStats.totalConnectionNumber}"/></td>
                    <td>Current connections</td>
                    <td><c:out value="${ftpStats.currentConnectionNumber}"/></td>
                </tr>
				<tr>
                    <td>Total logins(failed)</td>
                    <td><c:out value="${ftpStats.totalLoginNumber}"/>
                    (<c:out value="${ftpStats.totalFailedLoginNumber}"/>)</td>
                    <td>Current logins</td>
                    <td><c:out value="${ftpStats.currentLoginNumber}"/></td>
                </tr>

            </tbody>
        </table>


	</div>
      <div class="page-pane selfclear">

      </div>
    </div><!-- /#page -->
    </div><!-- /.wrap> -->
  </div><!-- /#main -->
  <center><p><img src="img/geoSolutions-logo.png" /></p>
  <p>Copyright &copy; 2005 - 2012 GeoSolutions.</p></center>
</body>
</html>