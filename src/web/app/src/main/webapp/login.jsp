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
<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core_rt' %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title wicket:id="pageTitle">GeoBatch Login</title>
      <link href="img/favicon.ico" rel="shortcut icon"/>
      <link rel="stylesheet" href="css/blueprint/screen.css" type="text/css" media="screen, projection" />
      <link rel="stylesheet" href="css/blueprint/print.css" type="text/css" media="print" />
	  <link rel="stylesheet" href="css/app.css" type="text/css" media="screen, projection" />
      <!--[if IE]>
        <link rel="stylesheet" href="css/blueprint/ie.css" type="text/css" media="screen, projection" />
	    <link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection" />
      <![endif]-->

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
      <h2><a wicket:id="home" class="pngfix" href="."><span wicket:id="label">GeoBatch</span></a></h2>
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
		<h1>GeoBatch Access</h1>
      </div>
      <div class="page-pane selfclear">
		<form name="f" action="<c:url value='j_spring_security_check'/>" method="post">
		  <table class="page">
		    <tr><td style="font-weight: bold;">User:</td><td><input type='text' name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td></tr>
		    <tr><td style="font-weight: bold;">Password:</td><td><input type='password' name='j_password'></td></tr>
		    <tr><td><input type="checkbox" name="_spring_security_remember_me"></td><td style="font-weight: bold;">Don't ask for my password for two weeks</td></tr>
		    <tr><td colspan='2'><input name="submit" style="font-weight: bold;" type="submit"></td></tr>
		    <tr><td colspan='2'><input name="reset" style="font-weight: bold;" type="reset"></td></tr>
		    <tr><td colspan='2'>
		   		<c:if test="${not empty param.login_error}">
					<font color="red">
					  Your login attempt was not successful, try again.<br/><br/>
					  Reason: <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/>.
					</font>
				</c:if>
		    </td></tr>
		  </table>
		</form>
      </div>
    </div><!-- /#page -->
    </div><!-- /.wrap> -->
  </div><!-- /#main -->
  <center><p><img src="img/geoSolutions-logo.png" /></p>
  <p>Copyright &copy; 2005 - 2009 GeoSolutions.</p></center>
</body>
</html>  