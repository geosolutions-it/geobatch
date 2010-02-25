<%
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
        <p><img src="img/manageFlows-small.png" /></p>
		<table width="100%" border="0" cellpadding="2" cellspacing="1">
			<thead>
				<tr bgcolor="black" style="color: white;">
					<th width="10%">ID</th>
					<th width="20%">DESCRIPTION</th>
					<th width="30%">INPUT DIR</th>
					<th width="30%">WORKING DIR</th>
					<th width="5%">STATUS</th>
					<th width="5%">ACTION</th>
				</tr>
			</thead>
			<tbody>
			<c:forEach var="fm" items="${flowManagers}">
				<tr >
					<td><c:out value="${fm.configuration.id}"/></td>
					<td><font style="font-style: italic; font-size: 12px"><c:out value="${fm.configuration.description}"/></font></td>
					<td><c:out value="${fm.configuration.eventGeneratorConfiguration.workingDirectory}"/></td>
					<td><c:out value="${fm.configuration.workingDirectory}"/></td>
					<td align="center">
						<c:choose> 
  							<c:when test="${fm.running}">
								<image src='img/green.png' border='0' title='running' alt='running' width='16' height='16'/>
							</c:when>
							<c:otherwise>
								<image src='img/red.png' border='0' title='paused' alt='paused' width='16' height='16'/>
							</c:otherwise>
						</c:choose>
					</td>
					<td align="center">
						<c:choose> 
  							<c:when test="${fm.running}">
  								<a href='pause.do?fmId=${fm.id}'><image src='img/pause.png' border='0' title='pause' alt='pause' width='16' height='16'/></a>
  							</c:when>
  							<c:otherwise>
  								<a href='resume.do?fmId=${fm.id}'><image src='img/play.png' border='0' title='resume' alt='resume' width='16' height='16'/></a>
  							</c:otherwise>
  						</c:choose>
						<a href="dispose.do?fmId=${fm.id}"><image src='img/dispose.png' border='0' title='dispose' alt='dispose' width='16' height='16'/></a>
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
  <p>Copyright &copy; 2005 - 2009 GeoSolutions.</p></center>
</body>
</html>