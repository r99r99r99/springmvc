package com.sdocean.beach.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
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

import com.sdocean.beach.model.BeachAllConfig;
import com.sdocean.beach.model.BeachDataModel;
import com.sdocean.beach.model.BeachDegreeModel;
import com.sdocean.beach.model.BeachGroupModel;
import com.sdocean.beach.model.BeachStatisModel;
import com.sdocean.beach.service.BeachService;
import com.sdocean.dataQuery.model.StatisModel;
import com.sdocean.frame.model.ConfigInfo;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.log.service.OperationLogService;
import com.sdocean.station.model.StationModel;
import com.sdocean.station.service.StationService;
import com.sdocean.users.model.SysUser;

@Controller
public class BeachAction {

	private static Logger log = Logger.getLogger(BeachAction.class);  
	@Autowired
	private OperationLogService logService;
	@Autowired
	private ConfigInfo info;
	@Autowired
	private BeachService beachService;
	@Autowired
	StationService stationService;
	
	/*
	 * 跳转到公司管理页面
	 */
	@RequestMapping("info_beach.do")
	public ModelAndView info_beach(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/beach/beach_init");
	        return mav;  
	}
	
	
	@RequestMapping(value="getBeachDetailByStation.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getBeachDetailByStation(@ModelAttribute("model") StationModel model,HttpServletRequest request,
			HttpServletResponse response){
		BeachDegreeModel result = beachService.getBeachDetailByStation(model);
		return JsonUtil.toJson(result);
	}
	
	
	/*
	 * 跳转到数据导入界面
	 */
	@RequestMapping("info_beachImport.do")
	public ModelAndView info_beachImport(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/beach/beachImport_init");
		    
		    List<BeachGroupModel> result = beachService.getBeachGroupList();
		    mav.addObject("groupList", result);
		    
	        return mav;  
	}
	
	/*
	 * 初始化数据导入界面
	 */
	@RequestMapping(value="beachImport_info.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String beachImport_info(HttpServletRequest request,
			HttpServletResponse response){
		BeachDataModel btm = new BeachDataModel();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String collectTime = sdf.format(new Date());
		collectTime = collectTime.substring(0, 13);
		btm.setCollectTime(collectTime);
		return JsonUtil.toJson(btm);
	}
	
	/*
	 * 查询当前站点，当前时间段的海水浴场导入数据
	 */
	@RequestMapping(value="getBeachData4Station.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getBeachData4Station(@ModelAttribute("model") BeachDataModel model,HttpServletRequest request,
			HttpServletResponse response){
		Map<String, Object> result = beachService.getBeachData4Station(model);
		return JsonUtil.toJson(result);
	}
	
	/*
	 * 查询所有的配置信息
	 */
	@RequestMapping(value="getAllBeachConfig.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getAllBeachConfig(HttpServletRequest request,
			HttpServletResponse response){
		BeachAllConfig result = beachService.getBeachAllConfigList();
		return JsonUtil.toJson(result);
	}
	
	/*
	 * 得到配置表                                                             method = RequestMethod.POST,produces = "application/json;charset=UTF-8"
	 */
	@RequestMapping(value="getBeachGroupList.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getBeachGroupList(HttpServletRequest request,
			HttpServletResponse response){
		List<BeachGroupModel> result = beachService.getBeachGroupList();
		return JsonUtil.toJson(result);
	}
	
	/*
	 * 保存海水浴场上报数据 
	 */
	@RequestMapping(value="saveBeachImportData.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String saveBeachImportData(HttpServletRequest request,
			HttpServletResponse response){
		HttpSession session = request.getSession();
		SysUser user = (SysUser) session.getAttribute("user");
		//获得request中传递的时间以及站点信息
		String collectTime =  request.getParameter("collectTime");
		String stationId = request.getParameter("stationId");
		//初始化传输的参数
		StringBuffer param = new StringBuffer("");
		int i = 0;
	    Enumeration paramNames = request.getParameterNames();  
	    while (paramNames.hasMoreElements()) {  
	      String paramName = (String) paramNames.nextElement();  
	      if(paramName!=null&&!paramName.equals("collectTime")&&!paramName.equals("stationId")) {
	    	  String[] paramValues = request.getParameterValues(paramName);  
	    	  if (paramValues.length == 1) {  
	    		  String paramValue = paramValues[0];  
	    		  if (paramValue.length() != 0) {  
	    			  if(i>0) {
	    				  param.append(",");
	    			  }
	    			  param.append(paramName).append("#").append(paramValue);
	    			  i++;
	    		  }  
	    	  }
	      }
	    } 
	    String msg = beachService.saveBeachImportData(stationId, collectTime, param.toString(), user.getId());
		return msg;
	}
	
	/*
	 * 跳转到海水浴场的水质统计页面
	 */
	
	@RequestMapping("info_beachstatisquery.do")
	public ModelAndView info_beachstatisquery(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/beach/beachStatisquery_init");
		    
		    DateFormat beginDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			//默认开始时间为一个月以前
			calendar.add(Calendar.MONTH, -2);
			// 添加默认开始时间
			String time1 = beginDf.format(calendar.getTime()) ;
			// 添加默认结束时间
			String time2 = df.format(new Date());
			// 存放参数
			mav.addObject("beginDate", time1);
			mav.addObject("endDate", time2);
	        return mav;  
	}
	/*
	 * 水质统计页面的查询功能
	 */
	@RequestMapping(value="/showBeachStat.do", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String showBeachStat(@ModelAttribute("gmodel") BeachStatisModel gmodel,
						Map<String, Object> model, HttpServletRequest request,
						HttpServletResponse response) {
		StationModel station = stationService.getStationById(gmodel.getStationId());
		gmodel = beachService.getBeachStat(gmodel, station);
		return JsonUtil.toJson(gmodel);
	}
}
