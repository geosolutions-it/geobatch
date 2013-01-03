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
        <h1>FTP User Management</h1>
        
        <form method="post" action="newFtpUser.form">
        <table>
          <tr>
		    <td>User ID</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.userId">
					<input type="hidden" name="userId" value="${status.value}" />
					${ftpUserDataBean.userId}
	      		</spring:bind>
	      	</td>
		  </tr>
		  <tr>
		    <td>User Name <i>(no spaces allowed)</i></td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.userName">
					<input type="text" name="userName" value="${status.value}" />
	      		</spring:bind>
	      	</td>
		  </tr>
		  <tr>
		    <td>Password</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.password">
					<input type="password" name="password" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>Repeat Password</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.repeatPassword">
					<input type="password" name="repeatPassword" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>Role</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.role">
					<select name="role" title="Select Role">
						<c:forEach var="role" items="${ftpUserDataBean.availableRoles}">
							<option value="${role}"><c:out value="${role}"/></option>
						</c:forEach>
					</select>
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>Write Enabled</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.writePermission">
					<input type="checkbox" name="writePermission" value="true" <c:if test="${status.value}">checked</c:if> />
	      		</spring:bind>
		    
		    </td>
		  </tr>
		  <tr>
		    <td>Upload Rate (Bytes)</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.uploadRate">
					<input type="text" name="uploadRate" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>Download Rate (Bytes)</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.downloadRate">
					<input type="text" name="downloadRate" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>Max Login per Ip</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.maxLoginPerIp">
					<input type="text" name="maxLoginPerIp" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>Max Login Number</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.maxLoginNumber">
					<input type="text" name="maxLoginNumber" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>Idle Time (ms)</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.idleTime">
					<input type="text" name="idleTime" value="${status.value}" />
	      		</spring:bind>
		    </td>
		  </tr>
		  <tr>
		    <td>Allowed Flows</td>
		    <td>
		    	<spring:bind path="ftpUserDataBean.allowedFlowManagers">
					<select multiple="multiple" name="allowedFlowManagers" title="Select Allowed Flow-Managers">
						<c:forEach var="fm" items="${ftpUserDataBean.availableFlowManagers}">
							<option value="${fm.configuration.id}" 
								<c:if test="${ftpUserDataBean.allowedFlowManagers != null}">
									<c:forEach var="fmA" items="${ftpUserDataBean.allowedFlowManagers}">
										<c:if test="${fmA == fm.configuration.id}">
											selected="selected"
										</c:if>
									</c:forEach>
								</c:if>
							><c:out value="${fm.configuration.id}"/></option>
						</c:forEach>
					</select>
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
      		<input type="submit" value="Save" id="btnTxt"/><input type="button" value="Cancel" onclick="javascript:window.location.href=('ftp.do?view=users')"/>
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