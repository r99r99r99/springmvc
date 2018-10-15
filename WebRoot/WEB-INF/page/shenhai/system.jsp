<%@ page contentType="text/html; charset=UTF-8" %>
<%@page import="java.util.Iterator"%>  
<%@ page import="java.util.*" %>  
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>  
<%
  	//初始化
	String path = request.getContextPath();
%>
<!DOCTYPE html>
<html>
  <head>
	<title>${system.systemName }</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<link href="<%=path %>/system/css/style.css" rel="stylesheet" type="text/css" />
	<script language="JavaScript" src="<%=path %>/system/js/jquery.js"></script>
	
	<script language="javascript">
		$(function(){
		    $('.loginbox0').css({'position':'absolute','left':($(window).width()-810)/2});
			$(window).resize(function(){  
		    $('.loginbox0').css({'position':'absolute','left':($(window).width()-810)/2});
		    })  
		});
		
		function platformTurn(code){
			window.location.href="turnPlotform.do?menuCode="+code; 
		};
   </script>
  </head>

<body style="background-color:#1c77ac; background-image:url(<%=path %>/system/images/light1.png); background-repeat:no-repeat; background-position:center top; overflow:hidden;">


    
    <div class="loginbody1" align="center">
    
   <p class="systemlogo_logo" >${system.systemName }</p>
       
    <div class="loginbox0">
    
    <ul class="loginlist">
    <c:forEach var="plat" items="${platList}">
    	<li class="" onclick="platformTurn('${plat.code }')" ><a href="javascript:void(0);">
	       <img src="images/top/${plat.picture }" width="150px" height="112.5px"  alt="${plat.name }"/>
	       <p>${plat.name }<br />管理系统</p></a>
	    </li>
    </c:forEach>
    </ul>
    
    
    </div>
    
    </div>
    
    <div class="loginbm">Copyright © 2016-2018 山东深海海洋科技有限公司</div>
	
    
</body>
</html>