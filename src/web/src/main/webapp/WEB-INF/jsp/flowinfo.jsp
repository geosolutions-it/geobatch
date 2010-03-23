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

<script type="text/javascript">
	$(function(){
		// Dialogs
		$("a.actions").click(
			function() {
				var url = this.href;
				var dialog = $('<div id="dialog-modal" style="display:hidden"></div>').appendTo('body');
				
               	// load remote content
               	dialog.load(
                       url, 
                       {},
                       function (responseText, textStatus, XMLHttpRequest) {
                       	dialog.dialog({
                           	title: 'Instance actions...',
                           	width: 600,
                   			height: 700,
                   			modal: true
                   		});

                       }
               );

               //prevent the browser to follow the link
               return false;
       	});

		
	});
</script>

<c:set var="fm" value="${flowManager}"/>

<c:forEach var="ec" items="${fm.eventConsumers}">
    <h5>
    	<c:out value="${ec.id}"/>
	    <c:choose>
	        <c:when test="${ec.paused}">
	            <a href='consumerResume.do?fmId=${fm.id}&ecId=${ec.id}'><image src='img/control_play.png' border='0' title='resume instance' alt='resume' width='16' height='16'/></a>
	        </c:when>
	        <c:otherwise>
	            <a href='consumerPause.do?fmId=${fm.id}&ecId=${ec.id}'><image src='img/control_pause.png' border='0' title='pause instance' alt='pause' width='16' height='16'/></a>
	        </c:otherwise>
	    </c:choose>
	    <a href="consumerDispose.do?fmId=${fm.id}&ecId=${ec.id}"><image src='img/cancel.png' border='0' title='cancel instance' alt='cancel' width='16' height='16'/></a>
	    <a class="actions" href="consumerInfo.do?fmId=${fm.id}&ecId=${ec.id}"><image src='img/page_white_text.png' border='0' title='instance logs' alt='logs' width='16' height='16'/></a>
    </h5>
    <UL>
	    <LI> - <B>name</B>: <c:out value="${ec.name}"/><br/></LI>
	    <LI> - <B>description</B>:<c:out value="${ec.description}"/><br/></LI>
	    <LI> - <B>created</B>: <fmt:formatDate value="${ec.creationTimestamp.time}" type="both" dateStyle="SHORT" timeStyle="FULL"/></LI>
	    <LI> - <B>status</B>:
	    	<c:choose> 
				<c:when test="${ec.status == 'FAILED'}">
					<font style="font-style: italic; font-weight: bold; font-size: 12px; color: red">
				</c:when>
				<c:otherwise>
					<font style="font-style: italic; font-weight: bold; font-size: 12px; color: green">
				</c:otherwise>
			</c:choose>
	    	<c:out value="${ec.status}"/></font>
	    </LI>
    </UL>
    <hr/>
</c:forEach>
