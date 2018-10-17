package com.sdocean.station.action;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.sdocean.common.model.Result;
import com.sdocean.common.model.SelectTree;
import com.sdocean.frame.model.ConfigInfo;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.log.service.OperationLogService;
import com.sdocean.page.model.PageResult;
import com.sdocean.page.model.UiColumn;
import com.sdocean.station.model.StationDeviceComm;
import com.sdocean.station.model.StationInfo;
import com.sdocean.station.model.StationModel;
import com.sdocean.station.service.StationCommService;

@Controller
public class StationCommAction {
	@Resource
	StationCommService stationCommService;
	@Resource
	OperationLogService logService;
	@Autowired
	private ConfigInfo info;
	
	@RequestMapping("info_stationComm.do")
	public ModelAndView info_mainedit(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/station/stationCommInfo");
	        return mav;  
	}
	/*
	 * 跳转到站点信息编辑页面
	 */
	@RequestMapping("info_stationInfo.do")
	public ModelAndView info_stationInfo(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/station/stationInfo_init");
	        return mav;  
	}
	/*
	 * 跳转到站点信息展示页面
	 */
	@RequestMapping("info_stationShow.do")
	public ModelAndView info_stationShow(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/station/stationShow_init");
		    HttpSession session = request.getSession();
		    StationModel station = (StationModel) session.getAttribute("station");
		    StationInfo model = new StationInfo();
		    model.setStationId(station.getId());
		    
		    
		    StationInfo result = new StationInfo();
			result = stationCommService.getStationInfoByWpId(model);
			mav.addObject("infomation", result.getInfomation());
	        return mav;  
	}
	/*
	 * 跳转到站点设备展示页面
	 */
	@RequestMapping("info_stationDeviceShow.do")
	public ModelAndView info_stationDeviceShow(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/station/stationDeviceShow_init");
		  /*  HttpSession session = request.getSession();
		    StationModel station = (StationModel) session.getAttribute("station");
		    StationInfo model = new StationInfo();
		    model.setStationId(station.getId());
		    
		    
		    StationInfo result = new StationInfo();
			result = stationCommService.getStationInfoByWpId(model);
			mav.addObject("infomation", result.getInfomation());*/
	        return mav;  
	}
	
	/*
	 * 展示站点维护列表
	 */
	@RequestMapping(value="showStationCommList.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String showStationCommList(@ModelAttribute("model") StationDeviceComm model,HttpServletRequest request,
			HttpServletResponse response){
		PageResult page = new PageResult();
		//得到展示表格的表头
		List<UiColumn> cols = stationCommService.getCols4TypeList();
		//得到展示表格的结果集
		List<StationDeviceComm> rows = stationCommService.getStationCommList(model);
		page.setCols(cols);
		page.setRows(rows);
		return JsonUtil.toJson(page);
	}
	/*
	 * 保存修改
	 */
	@RequestMapping(value="saveStationCommChange.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String saveStationCommChange(@ModelAttribute("model") StationDeviceComm model,HttpServletRequest request,
			HttpServletResponse response){
		Result result = new Result();
		result = stationCommService.saveStationCommChange(model);
		//保存操作记录
		logService.saveOperationLog(result, request);
		return result.getMessage();
	}
	/*
	 * 保存新增
	 */
	@RequestMapping(value="saveNewStationComm.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String saveNewStationComm(@ModelAttribute("model") StationDeviceComm model,HttpServletRequest request,
			HttpServletResponse response){
		Result result = new Result();
		result = stationCommService.saveNewStationComm(model);
		//保存操作记录
		logService.saveOperationLog(result, request);
		return result.getMessage();
	}
	/*
	 * 得到该站点下的设备列表,并选中已经查询的设备
	 */
	@RequestMapping(value="getDevciesByStation4Tree.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getDevciesByStation4Tree(@ModelAttribute("model") StationModel model,HttpServletRequest request,
			HttpServletResponse response){
		List<SelectTree> trees = new ArrayList<SelectTree>();
		trees = stationCommService.getDevciesByStation4Tree(model);
		return JsonUtil.toJson(trees);
	}
	
	
	/*
	 * 初始化站点信息页面
	 */
	@RequestMapping(value="init_getStationInfo.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String init_getStationInfo(@ModelAttribute("model") StationInfo model,HttpServletRequest request,
			HttpServletResponse response){
		StationInfo result = new StationInfo();
		result = stationCommService.getStationInfoByWpId(model);
		return JsonUtil.toJson(result);
	}
	/*
	 * 保存,更新站点信息
	 */
	@RequestMapping(value="saveStationInfo.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String saveStationInfo(@ModelAttribute("model") StationInfo model,HttpServletRequest request,
			HttpServletResponse response){
		Result result = new Result();
		result = stationCommService.saveStationInfomation(model);
		//保存操作信息
		logService.saveOperationLog(result, request);
		
		return result.getMessage();
	}
	
	/*
	 * 删除站点信息
	 */
	@RequestMapping(value="deleStationComm.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String deleStationComm(@ModelAttribute("model") StationDeviceComm model,HttpServletRequest request,
			HttpServletResponse response){
		Result result = new Result();
		result = stationCommService.deleStationComm(model);
		//保存操作记录
		logService.saveOperationLog(result, request);
		return result.getMessage();
	}
}
