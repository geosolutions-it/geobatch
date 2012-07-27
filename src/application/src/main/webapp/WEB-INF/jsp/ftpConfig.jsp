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
      
      <link type="text/css" href="css/ui-lightness/jquery-ui-1.8.custom.css" rel="stylesheet" />
      <link rel="stylesheet" type="text/css" href="css/jquery.asmselect.css" />
	  <script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
	  <script type="text/javascript" src="js/jquery-ui-1.8.custom.min.js"></script>
	  <script type="text/javascript" src="js/jquery.asmselect.js"></script>
	  <script type="text/javascript" src="js/jquery.paginate.js"></script>
		
	  <script type="text/javascript">
	  	$(document).ready(function() {
			$("select[multiple]").asmSelect({
				addItemTarget: 'bottom',
				animate: true,
				highlight: true,
				sortable: true
			});
		});
      </script>
      
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
        <h1>FTP Server Configuration</h1>
        
        <form method="post" action="ftpConfig.form">
        <table>
          <tr>
		    <td>Server ID</td>
		    <td>
		    	<spring:bind path="ftpConfigDataBean.id">
					<input type="hidden" name="id" value="${status.value}" />
					${ftpConfigDataBean.id}
	      		</spring:bind>
	      	</td>
		  </tr>
		  <tr>
		    <td>FTP Base Dir</td>
		    <td>
		    	<spring:bind path="ftpConfigDataBean.ftpBaseDir">
					<input type="text" name="ftpBaseDir" value="${status.value}" />
	      		</spring:bind>
	      	</td>
		  </tr>
		  <tr>
		    <td>Port</td>
		    <td>
		    	<spring:bind path="ftpConfigDataBean.port">
					<input type="text" name="port" value="${status.value}" />
	      		</spring:bind>
	      	</td>
		  </tr>
		  <tr>
		    <td>SSL Enabled</td>
		    <td>
		    	<spring:bind path="ftpConfigDataBean.ssl">
					<input type="checkbox" name="ssl" value="true" <c:if test="${status.value}">checked</c:if> />
	      		</spring:bind>
		    
		    </td>
		  </tr>
		  <tr>
		    <td>Auto-Start</td>
		    <td>
		    	<spring:bind path="ftpConfigDataBean.autoStart">
					<input type="checkbox" name="autoStart" value="true" <c:if test="${status.value}">checked</c:if> />
	      		</spring:bind>
		    
		    </td>
		  </tr>
		  <tr>
		    <td>Anonimous Enabled</td>
		    <td>
		    	<spring:bind path="ftpConfigDataBean.anonEnabled">
					<input type="checkbox" name="anonEnabled" value="true" <c:if test="${status.value}">checked</c:if> />
	      		</spring:bind>
		    
		    </td>
		  </tr>
		  <tr>
		    <td>Max Anonim Logins</td>
		    <td>
		    	<spring:bind path="ftpConfigDataBean.maxAnonLogins">
					<input type="text" name="maxAnonLogins" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>Max Logins</td>
		    <td>
		    	<spring:bind path="ftpConfigDataBean.maxLogins">
					<input type="text" name="maxLogins" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>Max Login Failures</td>
		    <td>
		    	<spring:bind path="ftpConfigDataBean.maxLoginFailures">
					<input type="text" name="maxLoginFailures" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>Login Failure Delay</td>
		    <td>
		    	<spring:bind path="ftpConfigDataBean.loginFailureDelay">
					<input type="text" name="loginFailureDelay" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>

		</table>
		
		<font style="font-style: italic; font-size: 13px; color: red">
      <spring:hasBindErrors name="ftpConfigDataBean">
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
      		<input type="submit" value="Save" id="btnTxt"/><input type="button" value="Cancel" onclick="javascript:window.location.href=('ftp.do?view=status')"/>
      	</div>
   </form>

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