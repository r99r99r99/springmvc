package com.sdocean.red.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

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

import com.sdocean.common.model.HchartsPie;
import com.sdocean.frame.model.ConfigInfo;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.log.service.OperationLogService;
import com.sdocean.red.model.RedStatisModel;
import com.sdocean.red.service.RedService;
import com.sdocean.station.model.StationModel;
import com.sdocean.station.service.StationService;

@Controller
public class RedAction {

	private static Logger log = Logger.getLogger(RedAction.class);  
	@Autowired
	private OperationLogService logService;
	@Autowired
	private ConfigInfo info;
	@Autowired
	private RedService redService;
	@Autowired
	private StationService stationService;
	
	/*
	 * 跳转到公司管理页面
	 */
	@RequestMapping("info_redTide.do")
	public ModelAndView info_redTide(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/red/redtide_init");
	        return mav;  
	}
	
	
	@RequestMapping(value="getRedDetailByStation.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getRedDetailByStation(@ModelAttribute("model") StationModel model,HttpServletRequest request,
			HttpServletResponse response){
		HchartsPie result = redService.getRedDetailByStation(model);
		return JsonUtil.toJson(result);
	}
	
	
	/*
	 * 跳转到增养殖区水质统计页面
	 */
	@RequestMapping("info_redStatisQuery.do")
	public ModelAndView info_redStatisQuery(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/red/redStatisQuery_init");
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
	 * 水质统计中查询
	 */
	@RequestMapping(value="/showRedStat.do", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String showRedStat(@ModelAttribute("gmodel") RedStatisModel gmodel,
						Map<String, Object> model, HttpServletRequest request,
						HttpServletResponse response) {
		StationModel station = stationService.getStationById(gmodel.getStationId());
		gmodel = redService.getRedStatDataSearch(gmodel, station);
		
		return JsonUtil.toJson(gmodel);
	}
}
