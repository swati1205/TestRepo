<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	<form action="MyServlet" method="POST">
		<select name="app" id="app" onchange="this.form.submit()">
		    <option value=""></option>
			<option value="CAFE">CAFE</option>
			<option value="App1">App1</option>
			<option value="App2">App2</option>
			<option value="App3">App3</option>
		</select>
			<c:choose> 
			<c:when test="${not empty set}">
			<table>
			<c:forEach var="name" items="${set}">
			 <tr>
              <td><c:out value="${name}"></c:out></td>
          </tr>
           </c:forEach>
            </table>
            </c:when>
           <c:otherwise>
               <c:out value="There are no components to display"></c:out>
           </c:otherwise>
           </c:choose> 

	</form>


</body>
</html>