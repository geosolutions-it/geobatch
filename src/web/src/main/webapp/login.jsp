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
<%@ page contentType="text/html" import="java.sql.*, java.io.*, java.util.*, it.geosolutions.geobatch.catalog.*, it.geosolutions.geobatch.flow.event.action.*, it.geosolutions.geobatch.flow.event.listeners.status.*" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core_rt' %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <title>Login</title>
		
	  <style type="text/css">
        #form{
            /*width: 400px;
            height: 350px;*/
            background-color: #6699FF;
						border: 2px solid #666699;
            padding: 5px; 
						align:center;
        }
				
 		</style>
  </head>

  <body>
    <h1>GDS Access</h1>

    <c:if test="${not empty param.login_error}">
      <font color="red">
        Your login attempt was not successful, try again.<br/><br/>
        Reason: <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/>.
      </font>
    </c:if>

		<table width=100% >
			  <tr><td id="form" align="center">
        		<form name="f" action="<c:url value='j_spring_security_check'/>" method="post">
              <table>
                <tr><td style="font-weight: bold;">User:</td><td><input type='text' name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td></tr>
                <tr><td style="font-weight: bold;">Password:</td><td><input type='password' name='j_password'></td></tr>
        				
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				
                <tr><td><input type="checkbox" name="_spring_security_remember_me"></td><td style="font-weight: bold;">Don't ask for my password for two weeks</td></tr>
        				
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				
                <tr><td colspan='2'><input name="submit" style="font-weight: bold;" type="submit"></td></tr>
                <tr><td colspan='2'><input name="reset" style="font-weight: bold;" type="reset"></td></tr>
        				
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				<tr></tr>
        				
        				<tr><td colspan='2' align="center"><a href="http://www.ksat.no/"><img src="img/logo.png" alt="Kongsberg"/></a></td></tr>
              </table>
            </form>
    		</td></tr>
		</table>
    
  </body>
</html>
