package com.sdocean.menu.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sdocean.common.model.ZTreeModel;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.log.model.SysLoginLogModel;
import com.sdocean.menu.model.CurrMenu;
import com.sdocean.menu.model.SysMenu;
import com.sdocean.role.model.RoleModel;
import com.sdocean.users.model.SysUser;

@Component
public class SysMenuDao extends OracleEngine{
	
	/*
	 * 通过用户,获得该用户拥有的所有的权限内的后台的3级菜单
	 */
	public List<SysMenu> getMenuListByUser(String pcode,SysUser user){
		List<SysMenu> menuList = new ArrayList<SysMenu>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select distinct a.code,a.pcode,a.type,a.name,a.level,a.url,a.picture,a.isactive,a.ordercode ");
		sql.append(" from sys_menu a,sys_role_menu b,sys_role_user c ,sys_role d");
		sql.append(" where d.type = 1 and d.id = c.role_id ");    //d.type=1 代表的是菜单角色
		sql.append(" and c.role_id = b.role_id");
		sql.append(" and b.menu_id = a.code and a.level = 3 and a.isactive = 1");   //a.level=3 代表菜单等级
		sql.append(" and c.user_id = ").append(user.getId());
		sql.append(" and a.pcode = '").append(pcode).append("'");
		sql.append(" order by orderCode ");
		menuList = this.queryObjectList(sql.toString(), SysMenu.class);
		
		return menuList;
	}
	
	/*
	 * 通过父类菜单,获得该用户权限内的所有子菜单
	 */
	public SysMenu getMenuByPmenu(SysMenu pmenu,SysUser user){
		List<SysMenu> childList = new ArrayList<SysMenu>();
		//开始拼接SQL语句
		StringBuffer sql = new StringBuffer("");
		sql.append(" select distinct a.code,a.pcode,a.type,a.name,a.level,a.url,a.picture,a.isactive,a.ordercode");
		sql.append(" from sys_menu a,sys_role_menu b,sys_role_user c,sys_role d");
		sql.append(" where d.type = 1 and d.isactive = 1 and c.role_id = d.id");
		sql.append(" and b.role_id = c.role_id and a.code = b.menu_id");
		sql.append(" and a.isactive = 1 and a.pcode ='").append(pmenu.getCode()).append("'");
		sql.append(" and c.user_id =").append(user.getId());
		sql.append(" order by ordercode ");
		childList = this.queryObjectList(sql.toString(), SysMenu.class);
		if(childList!=null&&childList.size()>0){
			for(SysMenu child:childList){
				child = this.getMenuByPmenu(child,user);
			}
		}
		pmenu.setChildMenu(childList);  //将子菜单列表存入到父菜单中
		return pmenu;
	}
	
	/*
	 * 通过子类id获得菜单信息以及父类菜单信息
	 */
	public CurrMenu getCurrMenuById(String menuId){
		CurrMenu menu = new CurrMenu();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.code as cMenuId,a.name as cMenuName,a.url as curl,a.pCode as pMenuId,b.name as pMenuName");
		sql.append(" from sys_menu a ,sys_menu b where a.pcode = b.code");
		sql.append(" and a.code = '").append(menuId).append("'");
		menu = this.queryObject(sql.toString(), CurrMenu.class);
		return menu;
	}
	
	/*
	 * 查询当前角色的菜单列表,并通过TREE的形式展现
	 */
	public List<ZTreeModel> getMenus4Tree(RoleModel role){
		List<ZTreeModel> tree = new ArrayList<ZTreeModel>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.code as id,a.pcode as pId,a.name,case when b.menu_id is null then 'false' else 'true' end as checked,case level when 3 then 'true' else 'true' end as open");
		sql.append(" from sys_menu a ");
		sql.append(" left join sys_role_menu b on a.code = b.menu_id and b.role_id = ").append(role.getId());
		
		tree = this.queryObjectList(sql.toString(), ZTreeModel.class);
		return tree;
	}
	 /*
	  * 查询当前菜单的首页菜单权限 ,并通过TREE的形式展示
	  */
	public List<ZTreeModel> getFirstMenus4Tree(RoleModel role){
		List<ZTreeModel> tree = new ArrayList<ZTreeModel>();
		
		StringBuffer sql = new StringBuffer();
		sql.append(" select a.code as id,a.pcode as pId,a.name,case when b.menu_id is null then 'false' else 'true' end as checked,case level when 3 then 'true' else 'true' end as open,");
		sql.append(" case  when length(a.url) > 0 and type =1 then 'false' else 'true' end as nocheck ");
		sql.append(" from sys_menu a ");
		sql.append(" left join sys_role_firstmenu b on a.code = b.menu_id and b.role_id = ").append(role.getId());
		
		tree = this.queryObjectList(sql.toString(), ZTreeModel.class);
		return tree;
	}
	/*
	 * 查询当前用户的首页菜单权限
	 */
	public SysMenu getFirstMenu(SysUser user){
		SysMenu menu = new SysMenu();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select distinct a.code,a.pcode,a.type,a.name,a.level,a.url,a.picture,a.isactive,a.ordercode");
		sql.append(" from sys_menu a,sys_role_firstmenu b,sys_role c,sys_role_user d");
		sql.append(" where a.code = b.menu_id");
		sql.append(" and b.role_id = c.id and c.isactive = 1");
		sql.append(" and c.id = d.role_id and c.type = 3");
		sql.append(" and d.user_id =").append(user.getId());
		sql.append(" limit 1");
		menu = this.queryObject(sql.toString(), SysMenu.class);
		return menu;
	}
	
	/*
	 * 获得当前用户拥有的展示平台的列表
	 */
	public List<SysMenu> getPlatMenu(SysUser user){
		List<SysMenu> list = new ArrayList<>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select distinct a.code,a.pcode,a.type,a.name,a.level,a.url,a.picture,a.isactive,a.ordercode");
		sql.append(" from sys_menu a,sys_role_menu b,sys_role c,sys_role_user d");
		sql.append(" where a.code = b.menu_id");
		sql.append(" and b.role_id = c.id and c.isactive = 1");
		sql.append(" and c.type = 1");
		sql.append(" and c.id = d.role_id and user_id =").append(user.getId());
		sql.append(" and a.pcode = '000002'");
		sql.append(" order by ordercode");
		
		list = this.queryObjectList(sql.toString(), SysMenu.class);
		return list;
	}
	
	/*
	 * 获得用户在该平台内的第一个菜单
	 */
	public SysMenu getFirstMent4Code(String pcode,SysUser user) {
		SysMenu first = new SysMenu();
		//获得第一层菜单的集合
		List<SysMenu> menus = new ArrayList<SysMenu>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.code,a.pcode,a.type,a.name,a.level,a.url,a.picture,a.isactive,a.ordercode");
		sql.append(" from sys_menu a,sys_role_menu b,sys_role c,sys_role_user d");
		sql.append(" where a.code = b.menu_id");
		sql.append(" and a.isactive = 1 and a.pcode = '").append(pcode).append("'");
		sql.append(" and b.role_id = c.id ");
		sql.append(" and c.type = 1 and c.isactive = 1");
		sql.append(" and c.id = d.role_id and d.user_id =").append(user.getId());
		sql.append(" order by ordercode");
		menus = this.queryObjectList(sql.toString(), SysMenu.class);
		Boolean iftrue = false;
		for(SysMenu menu:menus) {
			if(menu.getUrl()!=null&&menu.getUrl().length()>0) {
				iftrue = true;
				first = menu;
			}
			if(iftrue) {
				break;
			}
		}
		
		if(!iftrue) {
			for(SysMenu menu:menus) {
				first = this.getFirstMent4Code(menu.getCode(), user);
			}
		}
		return first;
	}
	
	public SysMenu getMenuByCode(String code){
		SysMenu menu = new SysMenu();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select distinct a.code,a.pcode,a.type,a.name,a.level,a.url,a.picture,a.isactive,a.ordercode ");
		sql.append(" from sys_menu a");
		sql.append(" where a.code = '").append(code).append("'");
		menu = this.queryObject(sql.toString(), SysMenu.class);
		return menu;
	}
}
