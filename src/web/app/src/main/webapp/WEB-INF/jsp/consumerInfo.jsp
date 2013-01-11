<%
/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2013 GeoSolutions S.A.S.
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
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>

<c:set var="ec" value="${consumer}"/>

<h5> Consumer info </h5>
<UL>
    <LI>Id: <c:out value="${ec.id}"/></LI>
    <LI>Status: <c:out value="${ec.status}"/></LI>
    <LI>Dump: <c:out value="${ec}"/>

    </LI>
</UL>

<h5>
	Consumer eventlist
</h5>
<UL>
	<c:forEach var="event" items="${eventlist}">
		<LI><c:out value="${event}"/></LI>
	</c:forEach>
</UL>

<h5>
	Current/last action
</h5>
<UL>
   <LI>Type: <c:out value="${action.class.simpleName}"/></LI>
   <LI>Name: <c:out value="${action.name}"/></LI>
   <c:if test="${action.paused}">
       <LI>Status: paused</LI>
   </c:if>
</UL>
