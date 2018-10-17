package com.sdocean.menu.action;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.sdocean.common.model.ZTreeModel;
import com.sdocean.frame.model.ConfigInfo;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.menu.model.CurrMenu;
import com.sdocean.menu.model.SysMenu;
import com.sdocean.menu.service.SysMenuService;
import com.sdocean.role.model.RoleModel;
import com.sdocean.users.model.SysUser;

@Controller
public class MenuAction {
	@Resource
	SysMenuService menuService;
	@Autowired
	private ConfigInfo info;
	
	/*
	 * 获得某角色下的菜单列表,以TREE的形式显示
	 */
	@RequestMapping(value="getMenus4Tree.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getMenus4Tree(@ModelAttribute("role") RoleModel role,HttpServletRequest request,
			HttpServletResponse response){
		List<ZTreeModel> tree = menuService.getMenus4Tree(role);
		return JsonUtil.toJson(tree);
	}
	/*
	 * 获得某角色下的首页菜单列表,以TREE的形式显示
	 */
	@RequestMapping(value="getFirstMenus4Tree.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getFirstMenus4Tree(@ModelAttribute("role") RoleModel role,HttpServletRequest request,
			HttpServletResponse response){
		List<ZTreeModel> tree = menuService.getFirstMenus4Tree(role);
		return JsonUtil.toJson(tree);
	}
	/*
	 * 根据平台编码,跳转到新网站
	 */
	@RequestMapping("turnPlotform.do")
	public ModelAndView turnPlotform(@RequestParam("menuCode")String menuCode,HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		HttpSession session = request.getSession();
		SysUser users = (SysUser) session.getAttribute("user");
		//设置该平台为session中的平台
		SysMenu plat = menuService.getMenuByCode(menuCode);
		session.setAttribute("plat",plat);
		//获得该用户该平台下的菜单列表
        List<SysMenu> menuList = menuService.getMenuListByUser(plat.getCode(),users);
        session.setAttribute("menuList", menuList);
		//初始化一个当前的列表
        SysMenu menu = menuService.getFirstMenu(users);
        
        if(menu!=null&&menu.getUrl()!=null&&menu.getUrl().length()>0){
        	
        }else {
        	menu = menuService.getFirstMent4Code(plat.getCode(), users);
        }
        
        CurrMenu cmenu = menuService.getCurrMenuById(menu.getCode());
        //将菜单信息保存到SESSION中
        session.setAttribute("currMenu", cmenu);
        
        String redirect = "redirect:"+menu.getUrl();
        return new ModelAndView(redirect);
	}
}
