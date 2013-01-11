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
        	<a href="ftp.do?view=status"><img src="img/cog.png" title="Status" alt="Status" style="vertical-align: middle"/></a>
        	<a href="j_spring_security_logout"><img src="img/logout.png" title="Logout" alt="Logout" width="40" height="40" style="vertical-align: middle"/></a>
        </p>
        <br /><br />

        <p><a href="newFtpUser.form"><img src="img/add.png" />Add new user</a></p>
		<table width="100%" border="0" cellpadding="2" cellspacing="1">
			<thead>
				<tr bgcolor="black" style="color: white;">
					<th width="8%">USERNAME</th>
					<th width="5%">ROLE</th>
					<th width="5%">WRITE PERMISSION</th>
					<th width="5%">UPLOAD RATE (Bytes)</th>
					<th width="5%">DOWNLOAD RATE (Bytes)</th>
					<th width="5%">MAX LOGIN PER IP</th>
					<th width="5%">MAX LOGIN NUMBER</th>
					<th width="5%">MAX IDLE TIME (ms)</th>
					<th width="5%">ACTIONS</th>
				</tr>
			</thead>
			<tbody>
			<c:forEach var="us" items="${ftpUsers}">
				<tr >
					<td><c:out value="${us.name}"/></td>
					<td><c:out value="${us.sourceUser.role}"/></td>
					<td><c:out value="${us.writePermission}"/></td>
					<td><c:out value="${us.uploadRate}"/></td>
					<td><c:out value="${us.downloadRate}"/></td>
					<td><c:out value="${us.maxLoginPerIp}"/></td>
					<td><c:out value="${us.maxLoginNumber}"/></td>
					<td><c:out value="${us.maxIdleTime}"/></td>
					<td align="center">
						<a href='deleteFtpUser.do?userId=${us.id}' onclick="javascript:return confirm('Delete user ${us.name}?');"><image src='img/dispose.png' border='0' title='delete' alt='delete' width='16' height='16'/></a>
						<a href='updateFtpUser.form?userId=${us.id}'><image src='img/hammer_screwdriver.png' border='0' title='modify' alt='modify' width='16' height='16'/></a>
					</td>
				</tr>
			</c:forEach>
			
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