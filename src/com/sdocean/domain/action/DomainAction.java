package com.sdocean.domain.action;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.sdocean.common.model.Result;
import com.sdocean.domain.model.DomainLevelModel;
import com.sdocean.domain.model.DomainModel;
import com.sdocean.domain.model.DomainResult;
import com.sdocean.domain.server.DomainService;
import com.sdocean.frame.model.ConfigInfo;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.log.service.OperationLogService;
import com.sdocean.page.model.PageResult;
import com.sdocean.page.model.UiColumn;
import com.sdocean.station.model.StationModel;

@Controller
public class DomainAction {

	private static Logger log = Logger.getLogger(DomainAction.class);  
	@Autowired
	private DomainService domainService;
	@Resource
	OperationLogService logService;
	@Autowired
	private ConfigInfo info;
	
	
	@RequestMapping("info_domain.do")
	public ModelAndView info_domain(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/domain/domainInfo");
	        return mav;  
	}
	
	/*
	 * 查看功能区列表
	 */
	@RequestMapping(value="showDomainList.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String showDomainList(@ModelAttribute("model") DomainModel model,HttpServletRequest request,
			HttpServletResponse response){
		PageResult result = new PageResult();
		//为查询结果增加表头
		List<UiColumn> cols = domainService.getCols4DomainList();
		result.setCols(cols);
		
		List<DomainModel> rows = domainService.getDomainList(model);
		result.setRows(rows);
		return JsonUtil.toJson(result);
	}
	
	/*
	 * 保存新增的功能区
	 */
	@RequestMapping(value="saveNewDomain.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String saveNewDomain(@ModelAttribute("model") DomainModel model,HttpServletRequest request,
			HttpServletResponse response){
		Result result = new Result();
		result = domainService.saveNewDomain(model);
		logService.saveOperationLog(result,request);
		
		return result.getMessage();
	}
	
	/*
	 * 保存新增的功能区
	 */
	@RequestMapping(value="saveChangeDomain.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String saveChangeDomain(@ModelAttribute("model") DomainModel model,HttpServletRequest request,
			HttpServletResponse response){
		Result result = new Result();
		result = domainService.saveChangeDomain(model);
		logService.saveOperationLog(result,request);
		
		return result.getMessage();
	}
	

	/*
	 * 停用 功能区
	 */
	@RequestMapping(value="deleDomain.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String deleDomain(@ModelAttribute("model") DomainModel model,HttpServletRequest request,
			HttpServletResponse response){
		Result result = new Result();
		result = domainService.deleDomain(model);
		logService.saveOperationLog(result,request);
		
		return result.getMessage();
	}
	
	/*
	 * 保存功能区配置
	 */
	@RequestMapping(value="saveDomainStationIndicator.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String saveDomainStationIndicator(@ModelAttribute("model") DomainModel model,HttpServletRequest request,
			HttpServletResponse response){
		Result result = new Result();
		result = domainService.saveDomainStationIndicator(model);
		logService.saveOperationLog(result,request);
		
		return JsonUtil.toJson(result.getMessage());
	}
	
	/*
	 * 获得首页的站点功能区展示
	 */
	
	@RequestMapping(value="getDomainResultsByStation.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getDomainResultsByStation(@ModelAttribute("model") StationModel model,HttpServletRequest request,
			HttpServletResponse response){
		List<DomainResult> results = domainService.getDomainResultsByStation(model);
		
		return JsonUtil.toJson(results);
	}
	
	/*
	 * 获得某功能区下的等级列表
	 */
	@RequestMapping(value="getDomainLevelListByDomain.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getDomainLevelListByDomain(@ModelAttribute("model") DomainLevelModel model,HttpServletRequest request,
			HttpServletResponse response){
		List<DomainLevelModel> results = domainService.getDomainLevelListByDomain(model);
		
		return JsonUtil.toJson(results);
	}
}