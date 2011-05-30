<%@ page contentType="text/html"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page import="java.lang.management.*"%>
<%@ page import="java.sql.*, java.io.*, java.util.*, it.geosolutions.geobatch.catalog.*, it.geosolutions.geobatch.flow.event.action.*, it.geosolutions.geobatch.flow.event.listeners.status.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<script type="text/javascript">
		<% 
			List memoryPoolBeansList = (List)request.getAttribute("memoryPoolBeansList");
		%>
</script>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>GeoBatch: JVM Memory Monitor</title>
</head>
<body>
<html>

<body>
<c:forEach var="mpb" items="${memoryPoolBeansList}">
		<tr>
		<td colspan="2">
		<table border="0" width="100%" style="border: 1px #98AAB1 solid;">
			<tr>
				<td colspan="2" align="center"><b><c:out value="${mpb.name}"/></b></td>
			</tr>
			<tr>
				<td width="200">Type</td>
				<td><c:out value="${mpb.type}"/></td>
			</tr>
			<tr>
				<td>Usage</td>
				<td><c:out value="${mpb.usage}"/></td>
			</tr>
			<tr>
				<td>Peak Usage</td>
				<td><c:out value="${mpb.peakUsage}"/></td>
			</tr>
			<tr>
				<td>Collection Usage</td>
				<td><c:out value="${mpb.collectionUsage}"/></td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>
</c:forEach>

</table>
</body>
</html>
