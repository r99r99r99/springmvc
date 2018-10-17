package com.sdocean.dataQuery.action;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

import com.sdocean.common.model.Echarts;
import com.sdocean.common.model.HchartsLineData;
import com.sdocean.common.model.HchartsServieModel;
import com.sdocean.common.model.PlotLine;
import com.sdocean.common.model.SelectTree;
import com.sdocean.dataQuery.model.ComparisonData;
import com.sdocean.dataQuery.model.ComparisonModel;
import com.sdocean.dataQuery.model.DataQueryModel;
import com.sdocean.dataQuery.model.GraphModel;
import com.sdocean.dataQuery.model.PeriodContrastModel;
import com.sdocean.dataQuery.service.GraphQueryService;
import com.sdocean.dictionary.service.WaterQualityStandardService;
import com.sdocean.frame.model.ConfigInfo;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.indicator.model.IndicatorModel;
import com.sdocean.indicator.service.IndicatorService;
import com.sdocean.station.model.StationModel;
import com.sdocean.station.service.StationService;
import com.sdocean.users.model.SysUser;

@Controller
public class PeriodContrastAction {

	private static Logger log = Logger.getLogger(PeriodContrastAction.class); 
	
	@Autowired
	private ConfigInfo info;
	
	@Autowired
	StationService stationService;
	@Autowired
	IndicatorService indicatorService;
	
	@Autowired
	WaterQualityStandardService waterQualityStandardService;
	
	@RequestMapping("info_periodContrast.do")
	public ModelAndView info_periodContrast(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/dataquery/periodContrast_init");
	        return mav;  
	}
	
	
	/*
	 * 为查询条件初始化
	 */
	@RequestMapping(value="periodContrast_init.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String periodContrast_init(HttpServletRequest request,
			HttpServletResponse response){
		PeriodContrastModel model = new PeriodContrastModel();
		DateFormat beginDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(new Date());
	    
	    //设置标准结束时间
	    String standAfterTime = beginDf.format(calendar.getTime());
	    //设置标准开始时间
	    calendar.add(Calendar.DATE, -20);
	    String standBeforeTime = beginDf.format(calendar.getTime());
	    
	    //设置对比结束时间
	    try {
			calendar.setTime(beginDf.parse(standAfterTime));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    calendar.add(Calendar.YEAR, -1);
	    String contrastAfterTime = beginDf.format(calendar.getTime());
	    
	    //设置对比开始时间
	    try {
			calendar.setTime(beginDf.parse(standBeforeTime));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    calendar.add(Calendar.YEAR, -1);
	    String contrastBeforeTime = beginDf.format(calendar.getTime());
		
	    model.setStandBeforeTime(standBeforeTime);
	    model.setStandAfterTime(standAfterTime);
	    model.setContrastBeforeTime(contrastBeforeTime);
	    model.setContrastAfterTime(contrastAfterTime);
	    
		return JsonUtil.toJson(model);
	}
	
	/*
	 * 时段对比查询中,展示数据走势图
	 */
	
}
