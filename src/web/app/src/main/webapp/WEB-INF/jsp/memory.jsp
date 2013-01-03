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
<%@ page contentType="text/html" import="java.sql.*, java.io.*, java.util.*, it.geosolutions.geobatch.catalog.*, it.geosolutions.geobatch.flow.event.action.*, it.geosolutions.geobatch.flow.event.listeners.status.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<jsp:useBean id="currentUser" class="it.geosolutions.geobatch.ui.security.CurrentUser"/>

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
	  <link rel="stylesheet" href="css/jpaginate.css" type="text/css" media="screen, projection" />
	  
	<link type="text/css" href="css/ui-lightness/jquery-ui-1.8.custom.css" rel="stylesheet" />	
	<script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="js/jquery-ui-1.8.custom.min.js"></script>
	<script type="text/javascript" src="js/jquery.paginate.js"></script>
	
	<script type="text/javascript">
		
		$(function(){

			// Accordion
			$("#accordion").accordion({
				active: false,
				header: "h3",
				collapsible: true,
				clearStyle: true,
				autoHeight: false,
				alwaysOpen: false
			});

			// Tabs
			$("#accordion div.tabs").tabs({
				ajaxOptions: {
					error: function(xhr, status, index, anchor) {
						$(anchor.hash).html("Couldn't load this tab. We'll try to fix this as soon as possible. If this wouldn't be a demo.");
					}
				}
			});

			// Refresh Button
			$("a.autorefresh").click(
				function() {
					$("#tab-instances-"+this.id).load(this.title);
				}
			);
			

		});
	</script>
	<style>
		.page{
			border: 1px solid #CCC;
			width:100%;
			margin:2px;
	        padding:50px 10px;
	        text-align:left;
			background-color:white;	
		}
	</style>
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
        	<img src="img/manageFlows-small.png" style="vertical-align: middle"/>
        	<a href="j_spring_security_logout"><img src="img/logout.png" title="Logout" alt="Logout" width="40" height="40" style="vertical-align: middle"/></a>
        </p>

<c:set var="nhmu" value="${nonHeapMemoryUsage}"/>

	<tr>
		<td colspan="2">
		<table border="0" width="100%" style="border: 1px #98AAB1 solid;">
			<tr>
				<td colspan="2" align="center"><b>Memory Non Heap usage</b></td>
			</tr>
			<tr>
				<td width="200">Init</td>
				<td><c:out value="${nhmu.init}"/></td>
			</tr>
			<tr>
				<td>Usage</td>
				<td><c:out value="${nhmu.used}"/></td>
			</tr>
			<tr>
				<td>Max</td>
				<td><c:out value="${nhmu.max}"/></td>
			</tr>
			<tr>
				<td>Committed</td>
				<td><c:out value="${nhmu.committed}"/></td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>
	
<c:set var="hmu" value="${heapMemoryUsage}" />
	<tr>
		<td colspan="2">
		<table border="0" width="100%" style="border: 1px #98AAB1 solid;">
			<tr>
				<td colspan="2" align="center"><b>Memory Heap usage</b></td>
			</tr>
			<tr>
				<td width="200">Init</td>
				<td><c:out value="${hmu.init}"/></td>
			</tr>
			<tr>
				<td>Usage</td>
				<td><c:out value="${hmu.used}"/></td>
			</tr>
			<tr>
				<td>Max</td>
				<td><c:out value="${hmu.max}"/></td>
			</tr>
			<tr>
				<td>Committed</td>
				<td><c:out value="${hmu.committed}"/></td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>
	
<c:set var="pf" value="${objectPendingFinalizationCount}" />
	<tr>
		<td colspan="2">
		<table border="0" width="100%" style="border: 1px #98AAB1 solid;">
			<tr>
				<td colspan="2" align="center"><b>Object Pending finalization</b></td>
			</tr>
			<tr>
				<td width="200">Number:</td>
				<td><c:out default="DEFAULT" value="${pf}"/></td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>

<c:forEach var="mpb" items="${memoryPoolBeansList}">
		<tr>
		<td colspan="2">
		<table border="0" width="100%" style="border: 1px #98AAB1 solid;">
			<tr>
				<td colspan="2" align="center"><b><c:out value="${mpb.name}"/></b></td>
			</tr>
			<tr>
				<td width="200">Type</td>
				<td><c:out value="${mpb.type}"/></td>
			</tr>
			<tr>
				<td>Usage</td>
				<td><c:out value="${mpb.usage}"/></td>
			</tr>
			<tr>
				<td>Peak Usage</td>
				<td><c:out value="${mpb.peakUsage}"/></td>
			</tr>
			<tr>
				<td>Collection Usage</td>
				<td><c:out value="${mpb.collectionUsage}"/></td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>
</c:forEach>

</table>
</body>
</html>
