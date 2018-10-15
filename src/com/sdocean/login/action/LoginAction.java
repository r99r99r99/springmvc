package com.sdocean.login.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sdocean.frame.model.ConfigInfo;

@Controller
public class LoginAction {

	@Autowired
	private ConfigInfo info;
	
	@RequestMapping("login.do")
	public ModelAndView login(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		System.out.println(info.getImagePath());
		HttpSession session = request.getSession();
		session.setAttribute("system", info);
		return new ModelAndView("/"+info.getPageVision()+"/login");
	}
	
	@RequestMapping("index.do")
	public ModelAndView handleRequestInternal(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			//验证用户账号与密码是否对应   
	        username = username.trim();  
	       
	        
	        return null;
	}
}
