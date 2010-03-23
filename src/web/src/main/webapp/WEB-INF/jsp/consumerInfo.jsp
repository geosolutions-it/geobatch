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

<head>
	<link type="text/css" href="css/ui-lightness/jquery-ui-1.8.custom.css" rel="stylesheet" />	
	<script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="js/jquery-ui-1.8.custom.min.js"></script>
	
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
                            	width: 800,
                    			height: 600,
                    			modal: true
                    		});

                        }
                );

                //prevent the browser to follow the link
                return false;
        	});

			
		});
	</script>
</head>

<c:set var="ec" value="${consumer}"/>

<h5>
	Consumer Eventlist: <c:out value="${ec.id}"/>
</h5>
<UL>
	<c:forEach var="event" items="${eventlist}">
		<LI><c:out value="${event}"/></LI>
	</c:forEach>
</UL>
