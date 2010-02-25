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
        <h1>Add new FTP User</h1>
        
        <form method="post" action="newFtpUser.form">
        <table>
		  <tr>
		    <td>USER_ID</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.userId">
					<input type="text" name="userId" value="${status.value}" />
	      		</spring:bind>
	      	</td>
		  </tr>
		  <tr>
		    <td>USER_PASSWORD</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.password">
					<input type="password" name="password" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>REPEAT_USER_PASSWORD</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.repeatPassword">
					<input type="password" name="repeatPassword" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>WRITE_PERMISSION</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.writePermission">
					<input type="checkbox" name="writePermission" value="true" <c:if test="${status.value}">checked</c:if>>

	      		</spring:bind>
		    
		    </td>
		  </tr>
		  <tr>
		    <td>UPLOAD_RATE</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.uploadRate">
					<input type="text" name="uploadRate" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>DOWNLOAD_RATE</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.downloadRate">
					<input type="text" name="downloadRate" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		</table>
		
		
		<font style="font-style: italic; font-size: 13px; color: red">
      <spring:hasBindErrors name="ftpUserDataBean">
         <p>There were ${errors.errorCount} error(s) in total:</p>
         <ul>
            <c:forEach var="errMsgObj" items="${errors.allErrors}">
               <li>
                  <spring:message code="${errMsgObj.code}" text="${errMsgObj.defaultMessage}"/>
               </li>
            </c:forEach>
         </ul>
      </spring:hasBindErrors>
     </font>    

		<div class="button-group selfclear">
      		<input type="submit" value="Save" id="btnTxt"/><input type="button" value="Cancel" onclick="javascript:window.location.href=('ftpUsers.do')">
      	</div>
   </form>
   

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