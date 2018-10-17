package com.sdocean.station.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.sdocean.common.model.Result;
import com.sdocean.common.model.SelectTree;
import com.sdocean.dataQuery.model.DataQueryModel;
import com.sdocean.dictionary.model.PublicModel;
import com.sdocean.frame.model.ConfigInfo;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.page.model.PageResult;
import com.sdocean.page.model.UiColumn;
import com.sdocean.region.service.RegionService;
import com.sdocean.report.model.ReportModel;
import com.sdocean.report.service.ReportServer;
import com.sdocean.station.model.StationModel;
import com.sdocean.station.model.StationPictureModel;
import com.sdocean.station.service.stationInfoService;
import com.sdocean.users.model.SysUser;

@Controller
public class StationInfoAction {

	private static Logger log = Logger.getLogger(StationInfoAction.class);  
	@Autowired
	private ConfigInfo info;
	@Autowired
	private ReportServer reportServer;
	@Autowired
	private stationInfoService stationInfoService;
	
	/*
	 * 跳转到图片档案页面
	 */
	@RequestMapping("stationPic_info.do")
	public ModelAndView stationPic_info(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/station/stationPic_info");
	        return mav;  
	}
	
	/*
	 * 获取站点图片列表
	 */
	@RequestMapping(value="getStationPicListByStationType.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getStationPicListByStationType(@ModelAttribute("model") StationPictureModel model,HttpServletRequest request,
			HttpServletResponse response){
		List<StationPictureModel> result =  stationInfoService.getStationPicListByStationType(info,model);
		return JsonUtil.toJson(result);
	}
}
