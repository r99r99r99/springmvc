<%@ page contentType="text/html; charset=UTF-8" %>
<%@page import="java.util.Iterator"%>  
<%@ page import="java.util.*" %>  
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>  
<%
  	//初始化
	String path = request.getContextPath();
  	if (null != session.getAttribute("userId")
  			&& !"".equals(session.getAttribute("userId").toString()))
  		response.sendRedirect("index.do");
  
  	String prompt = "";
  	String verificationStatus = "";
  	if (null != request.getAttribute("prompt"))
  		prompt = request.getAttribute("prompt").toString();
  	if (null != request.getAttribute("verificationStatus"))
  		verificationStatus = request.getAttribute("verificationStatus")
  				.toString();
  	
  	String error = "";
  	if (null != request.getAttribute("error"))
  		error = request.getAttribute("error").toString();
%>
<!DOCTYPE html>
<html>
  <head>
	<title>${system.systemName }</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta charset="utf-8">
	<meta name="author" content="forework">
	<meta name="format-detection" content="telephone=no">
	<meta name="viewport" content="initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="mobile-web-app-capable" content="yes">
	<meta name="apple-mobile-web-app-status-bar-style" content="black">
	<meta name="msapplication-tap-highlight" content="no">
	<meta http-equiv="X-UA-Compatible" content="IE=Edge" />


	<!-- 页面基本设置禁止随意更改 -->
	<!-- 基础CSS类库可随意更改 -->
	<link rel="stylesheet" type="text/css" href="<%=path %>/shenhai/login/css/less.css">
	<link rel="stylesheet" type="text/css" href="<%=path %>/shenhai/login/css/basic.css">
	<!--[if IE 8]>
	<link rel="stylesheet" type="text/css" href="<%=path %>/shenhai/login/css/ie8.css">
	<![endif]-->
	<!--[if gte IE 9]> 
	<link rel="stylesheet" type="text/css" href="<%=path %>/shenhai/login/css/ie.css"> 
	<![endif]-->
	<!-- 基础CSS类库可随意更改 -->
	<!-- 基础js类库可随意更改 -->
	
	<!-- 基础js类库可随意更改 -->
	

	
	<script src="<%=path %>/resources/jquery-3.3.1.min"></script>
	
	<script language="javascript">
	$(function(){
	});  
	
	function refresh(obj){  
	    obj.src = "VerifyCodeServlet?" + Math.random();  
	}  
	 
	function mouseover(obj){  
	   obj.style.cursor = "pointer";  
	} 

	function init(){ 
		var prompt = "<%= prompt%>" ;
		if("" != prompt)
			alert(prompt);
	}
	$(document).ready(function(){
		
		var error = "<%= error%>" ;
		if("" != error){
			var objtip=$(".error-box");
			objtip.text(error);
		}
		
		init();
		$("#login-btn").click(function(){
			var partten = /(\d|\w){6,}/i;
			
			var user = $("#login-username");
			var pw = $("#login-password");
			var verification = $("#verification");
			<%	if("1".equals(verificationStatus)){ %>
			if(verification.val()==""){
				alert("verification!");
				return false;
			}
			<% } %>
			if(!partten.test(user.val())){
				//alert("输入用户名不合法！请输入6到14位用户名（包含数字或字母！）（例如111111）");
				//return false;
			}else if(!partten.test(pw.val())){
				//alert("输入密码不合法！请输入6到14位密码（包含数字或字母！）（例如111111）");
				//return false;
			}else{
			}	
				$("#login-btn").submit();

		});
		
	});
	function login(){
		var loginform = document.getElementById("login-form");
		loginform.submit();
	}
	function keyLogin(){
		 if (event.keyCode==13)  //回车键的键值为13
		   document.getElementById("login").click(); //调用登录按钮的登录事件
		}

  </script> 
  </head>

<body  onKeydown="keyLogin();">
<div class="wrapper" style="background-color: white;">
  <div class="login-top">
  <div style="height: 80px;background-color: white;">
	<div style="margin-left: 210px;padding-top:30px;">
		<h1 style="font-size:55px;font-family:STXingkai;color:#4463b5">${system.systemName } </h1>
	</div>
  </div>
    <div class="login-topBg">
      <div class="login-topBg1">
        
        <div class="login-topStyle" >
          <form id="login-form" action="<%=path %>/index.do" class="form" method="post">
          <!--在点击注册时出现样式login-topStyle3登录框，而login-topStyle2则消失-->
          <div class="login-topStyle3" id="loginStyle" style="margin-top: 75px;">
            <h3>用户平台登录</h3>
            <!--输入错误提示信息，默认是隐藏的，把display:none，变成block可看到-->
            <label><span class="error-box"></span></label>
            <div class="ui-form-item loginUsername">
              <input type="username" name="username" id="username_input" type="text"   placeholder="用户名">
            </div>
            <div class="ui-form-item loginPassword">
              <input name="password" id="pwd_input"  type="password" class="lock"   placeholder="密码">
            </div>
            <div class="login_reme">
              	<!-- <input type="checkbox">
             	 <a class="reme1">记住账号</a> <a class="reme2" href="password.html">忘记密码?</a>  -->
            </div>
            	<a id="login" class="btnStyle btn-register" onclick="login()"> 立即登录</a>
            	</div>
            </form>
        </div>
      </div>
    </div>
  </div>
  <div class="loginCen" style="margin-top: 55px;">
    <div class="login-center">
      <div class="loginCenter-moudle">
        <div class="loginCenter-moudleLeft" style="margin-right: 60px;"> &nbsp;</div>
        <div class="loginCenter-moudleRight" style="  width: 1067px;"> 
          <!--第一个--> 
          <a class="loginCenter-mStyle loginCenter-moudle1" id="moudleRemove" style=" display:block;width: 250px;">
           <span class="moudle-img"><img src="<%=path %>/shenhai/login/images/login_bg_01.png"></span>
            <span class="moudle-text"> 
            <span class="moudle-text1" style="font-family:'微软雅黑'">技术先进
</span> 
            <span class="moudle-text2" style="font-family:'微软雅黑'">Technology Advancement</span> 
            </span>
             </a> 
           <!--第二个--> 
          <a class="loginCenter-mStyle loginCenter-moudle2" style=" display:block;width: 250px;" id="moudleRemove2" > 
          <span class="moudle-img"><img src="<%=path %>/shenhai/login/images/login_bg_02.png"></span> 
           <span class="moudle-text">
            <span class="moudle-text1" style="font-family:'微软雅黑'"> 设备稳定 
</span>
            <span class="moudle-text2" style="font-family:'微软雅黑'">Equipment Stability</span> 
           </span>
             </a> 
            <!--第三个--> 
                 <a class="loginCenter-mStyle loginCenter-moudle3" style=" display:block;width: 250px;" id="moudleRemove3" > 
                 <span class="moudle-img"><img src="<%=path %>/shenhai/login/images/login_bg_04.png"></span> 
                   <span class="moudle-text"> 
                 <span class="moudle-text"> <span class="moudle-text1" style="font-family:'微软雅黑'">数据安全 
</span>
                  <span class="moudle-text2" style="font-family:'微软雅黑'">Data Security</span>
            </span>
            </span>
                    </a> 
         	<!--第四个个--> 
                 <a class="loginCenter-mStyle loginCenter-moudle3" style=" display:block;width: 250px;" id="moudleRemove4" > 
                 <span class="moudle-img"><img src="<%=path %>/shenhai/login/images/login_bg_03.png"></span> 
                   <span class="moudle-text"> 
                 <span class="moudle-text"> <span class="moudle-text1" style="font-family:'微软雅黑'">系统统一
</span>
                  <span class="moudle-text2" style="font-family:'微软雅黑'">System Uniformity</span>
            </span>
            </span>
                    </a> 
             </div>
      </div>
    </div>
  </div>
  <div class="footer"> <span class="footerText">Copyright © 2018-2019 山东东润仪表科技股份科技有限公司</span> </div>
</div>
</body>
</html>