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
<%@ page language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<head><title>GeoBatch - Flow Manager</title></head>

<body>
   <h1>GeoBatch - Flow Manager</h1>
   
   <form method="post" action="newFlowManager.form">
   	<table width="600" border="0" cellpadding="2" cellspacing="1">
			<thead>
				<tr bgcolor="black" style="color: white;">
					<th width="50%">FIELD NAME</th>
					<th width="50%">FIELD VALUE</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>Flow Manager Descriptor:</td>
					<td>
				      <spring:bind path="flowManagerBean.descriptorId">
				         <select name="descriptorId">
				         	<c:forEach var="descriptor" items="${flowManagerBean.availableDescriptors}">
				         		<option><c:out value="${descriptor.id}"/></option>
				         	</c:forEach>
				         </select>
				         
				         <c:if test="${status.error}">
				            <font style="font-size: 13px; color: red">*</font>
				         </c:if>
				      </spring:bind>
					</td>
				</tr>

				<tr>
					<td>Flow Manager ID:</td>
					<td>
				      <spring:bind path="flowManagerBean.id">
				         <input type="text" name="id" value="${status.value}" />
				         
				         <c:if test="${status.error}">
				            <font style="font-size: 13px; color: red">*</font>
				         </c:if>
				      </spring:bind>
					</td>
				</tr>

				<tr>
					<td>Flow Manager Name:</td>
					<td>
				      <spring:bind path="flowManagerBean.name">
				         <input type="text" name="name" value="${status.value}" />
				         
				         <c:if test="${status.error}">
				            <font style="font-size: 13px; color: red">*</font>
				         </c:if>
				      </spring:bind>
					</td>
				</tr>

				<tr>
					<td>Input Directory:</td>
					<td>
				      <spring:bind path="flowManagerBean.inputDir">
				         <input type="text" name="inputDir" value="${status.value}" />
				         
				         <c:if test="${status.error}">
				            <font style="font-size: 13px; color: red">*</font>
				         </c:if>
				      </spring:bind>
					</td>
				</tr>

				<tr>
					<td>Output Directory:</td>
					<td>
				      <spring:bind path="flowManagerBean.outputDir">
				         <input type="text" name="outputDir" value="${status.value}" />
				         
				         <c:if test="${status.error}">
				            <font style="font-size: 13px; color: red">*</font>
				         </c:if>
				      </spring:bind>
					</td>
				</tr>

			</tbody>
		</table>      

	<font style="font-style: italic; font-size: 13px; color: red">
      <spring:hasBindErrors name="flowManagerBean">
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
      
      <input type="submit" value="Submit"/><input type="button" value="Cancel" onclick="javascript:window.location.href=('index.do')">
   </form>
</body>

</html>