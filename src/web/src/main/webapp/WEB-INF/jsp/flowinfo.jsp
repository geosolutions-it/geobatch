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
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>

<c:set var="fm" value="${flowManager}"/>

<c:forEach var="ec" items="${fm.eventConsumers}">
    <c:out value="${ec.id}"/>

    <c:choose>
        <c:when test="${ec.paused}">
            <a href='consumerResume.do?fmId=${fm.id}&ecId=${ec.id}'><image src='img/play.png' border='0' title='resume instance' alt='resume' width='16' height='16'/></a>
        </c:when>
        <c:otherwise>
            <a href='consumerPause.do?fmId=${fm.id}&ecId=${ec.id}'><image src='img/pause.png' border='0' title='pause instance' alt='pause' width='16' height='16'/></a>
        </c:otherwise>
    </c:choose>

    <a href="consumerDispose.do?fmId=${fm.id}&ecId=${ec.id}"><image src='img/dispose.png' border='0' title='dispose instance' alt='dispose' width='16' height='16'/></a>
    <br/>
    <UL>
    <LI> - <B>created</B>: <fmt:formatDate value="${ec.creationTimestamp.time}" type="both" dateStyle="SHORT" timeStyle="FULL"/></LI>

    <LI> - <B>id</B>: <c:out value="${ec.id}"/></LI>
    <LI> - <B>name</B>: <c:out value="${ec.name}"/><br/></LI>
    <LI> - <B>description</B>:<c:out value="${ec.description}"/><br/></LI>
    <LI> - <B>status</B>:<c:out value="${ec.status}"/><br/></LI>
    <LI> - <B>toString</B>: <c:out value="${ec}"/></LI>
    </UL>
    <hr/>
</c:forEach>
