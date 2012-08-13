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
		<% 
			int flowsPerPage = 10;
			List flowManagers = (List)request.getAttribute("flowManagers");
			int pages = Math.round(flowManagers.size() / (float)flowsPerPage);
			pages = (pages == 0 ? 1 : pages+1);
		%>

		function showLoading() {
		  $("#loaded").hide();
		  $("#loading").show();
		}

		function hideLoading() {
		  $("#loading").hide();
		  $("#loaded").show();
		}
		
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
					showLoading();
					$("#tab-instances-"+this.id).load(this.title);
					hideLoading();
				}
			);
					
			// Pagination
			$("#pagination1").paginate({
				count 					: <%= pages %>,
				start 					: 1,
				display     			: <%= flowsPerPage %>,
				border					: false,
				text_color  			: '#79B5E3',
				background_color    	: 'none',	
				text_hover_color  		: '#2573AF',
				background_hover_color	: 'none', 
				images					: false,
				mouse					: 'press',
				onChange     			: function(page){
											$('._current','#paginationdemo').removeClass('_current').hide();
											$('#p'+page).addClass('_current').show();
										  }
			});

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
			<!-- Accordion -->
			<div id="accordion">
				<!-- Pagination -->
				<% 
					int i = 0;
					int pageNum = 1;
				%>
				<div id="paginationdemo" class="demo">
					<c:forEach var="fm" items="${flowManagers}">
						<%
						  if(i % flowsPerPage == 0) {
						%>
						<div id="p<%= pageNum++ %>" class="page <%= (i == 0 ? "_current" : "") %>" style="<%= (i != 0 ? "display:none;" : "") %>">
						<%
						  }
						%>
								<h3>
									<a href="#">
										<c:choose> 
				  							<c:when test="${fm.running}">
												<image src='img/green.png' border='0' title='running' alt='running' width='16' height='16'/>
											</c:when>
											<c:otherwise>
												<image src='img/red.png' border='0' title='paused' alt='paused' width='16' height='16'/>
											</c:otherwise>
										</c:choose>
										<c:out value="${fm.configuration.id}"/>
									</a>
								</h3>
								<div>
									<font style="font-style: italic; font-size: 12px"><c:out value="${fm.configuration.description}"/></font>
									<div class="tabs">
										<ul>
											<li><a href="#tab-config-<%= i %>" style="cursor: default">Configuration</a></li>
											<li>
												<a href="flowinfo.do?fmId=${fm.id}" title="tab-instances-<%= i %>" style="cursor: default">Instances</a> <a class="autorefresh" id="<%= i %>" title="flowinfo.do?fmId=${fm.id}"><img id="loading" src="img/arrow_refresh.png" style="display:none;cursor: wait"/><img id="loaded" src="img/arrow_refresh.png" style="cursor: pointer; cursor: hand"/></a>
											</li>
										</ul>
										<div id="tab-config-<%= i %>">
											<p>
												<strong>Input directory:</strong> <c:out value="${fm.configuration.eventGeneratorConfiguration.watchDirectory}"/><br/>
												<!-- removed working dir here -->
												<strong>Status:</strong>
												<c:choose> 
					  								<c:when test="${fm.running}">
					  									Running
					  									<c:forEach var="role" items="${currentUser.grantedAuthorities}">
					  										<c:if test="${role.authority == 'ROLE_ADMIN' || role.authority == 'ROLE_POWERUSER'}">
							  									<a href='pause.do?fmId=${fm.id}'><image src='img/control_pause.png' border='0' title='pause' alt='pause' width='16' height='16'/></a>
							  									<a href='pause.do?fmId=${fm.id}&full=true'><image src='img/control_stop.png' border='0' title='complete freeze' alt='full' width='16' height='16'/></a>
					  										</c:if>
					  									</c:forEach>
					  								</c:when>
					  								<c:otherwise>
					  									Stopped
					  									<c:forEach var="role" items="${currentUser.grantedAuthorities}">
					  										<c:if test="${role.authority == 'ROLE_ADMIN' || role.authority == 'ROLE_POWERUSER'}">
					  											<a href='resume.do?fmId=${fm.id}'><image src='img/control_play.png' border='0' title='resume' alt='resume' width='16' height='16'/></a>
					  										</c:if>
					  									</c:forEach>
					  								</c:otherwise>
					  							</c:choose>
			  									<c:forEach var="role" items="${currentUser.grantedAuthorities}">
			  										<c:if test="${role.authority == 'ROLE_ADMIN'}">
														<a href="dispose.do?fmId=${fm.id}"><image src='img/cancel.png' border='0' title='cancel' alt='cancel' width='16' height='16'/></a>
													</c:if>
													<a href='flowManagerClear.do?fmId=${fm.id}'><image src='img/erase.png' border='0' title='clear all consumer instances' alt='clear all consumers' width='16' height='16'/></a>
												</c:forEach>						
											</p>
										</div>
										<div id="tab-instances-<%= i %>">
											Instances...
										</div>
									</div>
								</div>
							<% 
								i++;
							%>
						<%
						  if(i % flowsPerPage == 0) {
						%>
						</div>
						<%
						  }
						%>
					</c:forEach>
				</div>
				<div id="pagination1"></div>
			</div>
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