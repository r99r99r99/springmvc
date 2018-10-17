package com.sdocean.report.action;

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
import com.sdocean.users.model.SysUser;

@Controller
public class ReportAction {

	private static Logger log = Logger.getLogger(ReportAction.class);  
	@Autowired
	private ConfigInfo info;
	@Autowired
	private ReportServer reportServer;
	
	/*
	 * 跳转到日报查询页面
	 */
	@RequestMapping("dailyReport_info.do")
	public ModelAndView dailyReport_info(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/report/dailyReport_info");
	        return mav;  
	}
	
	
	/*
	 * 初始化日报上报
	 */
	@RequestMapping(value="dailyReport_init.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String dailyReport_init(HttpServletRequest request,
			HttpServletResponse response){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String reportDate = sdf.format(new Date());
		ReportModel report = new ReportModel();
		report.setReportDate(reportDate);
		
		return JsonUtil.toJson(report);
	}
	
	/*
	 * 查询报表
	 */
	@RequestMapping(value="getReport.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getReport(@ModelAttribute("model") ReportModel model,HttpServletRequest request,
			HttpServletResponse response){
		ReportModel report = new ReportModel();
		int type = model.getType();
		if(type==1){
			report = reportServer.getDailyReport(model);
		}else if(type==2){
			report = reportServer.getMonthReport(model);
		}
		
		return JsonUtil.toJson(report);
	}
	/*
	 * 查询报表
	 */
	@RequestMapping(value="saveReport.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String saveReport(@ModelAttribute("model") ReportModel model,HttpServletRequest request,
			HttpServletResponse response){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String collectDate = sdf.format(new Date());
		model.setCollectDate(collectDate);
		//获得用户的信息
		HttpSession session = request.getSession();
		SysUser user = (SysUser) session.getAttribute("user");
		model.setUserId(user.getId());
		Result result = reportServer.saveReport(model);
		return result.getMessage();
	}
	
	/*
	 * 跳转到月报查询页面
	 */
	@RequestMapping("monthReport_info.do")
	public ModelAndView monthReport_info(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/report/monthReport_info");
	        return mav;  
	}
	
	/*
	 * 初始化月报上报
	 */
	@RequestMapping(value="monthReport_init.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String monthReport_init(HttpServletRequest request,
			HttpServletResponse response){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		String reportDate = sdf.format(new Date());
		ReportModel report = new ReportModel();
		report.setReportDate(reportDate);
		
		return JsonUtil.toJson(report);
	}
	
	/*
	 * 跳转到报表查询页面
	 */
	@RequestMapping("reportQuery_info.do")
	public ModelAndView reportQuery_info(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/report/reportQuery_info");
	        return mav;  
	}
	
	/*
	 * 初始化报表查询页面
	 */
	@RequestMapping(value="reportQuery_init.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String reportQuery_init(HttpServletRequest request,
			HttpServletResponse response){
		ReportModel report = new ReportModel();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(new Date());
	  //设置结束时间
	    String endDate = sdf.format(calendar.getTime());
	    //设置开始时间
	    calendar.add(Calendar.MONTH, -1);
	    String beginDate = sdf.format(calendar.getTime());
	    report.setBeginDate(beginDate);
	    report.setEndDate(endDate);
	    report.setType(1);
		return JsonUtil.toJson(report);
	}
	/*
	 * 查询日报结果
	 */
	@RequestMapping(value="showReportQueryList.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String showReportQueryList(@ModelAttribute("model") ReportModel model,HttpServletRequest request,
			HttpServletResponse response){
		PageResult result = new PageResult();
		//为查询结果增加表头
		List<UiColumn> cols = reportServer.getCols4ReportList();
		result.setCols(cols);
		
		List<ReportModel> rows = reportServer.getRows4ReportList(model);
		result.setRows(rows);
		return JsonUtil.toJson(result);
	}
	
	/*
	 * 根据ID获得报表上报记录
	 */
	@RequestMapping(value="getReportById.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getReportById(@ModelAttribute("model") ReportModel model,HttpServletRequest request,
			HttpServletResponse response){
		ReportModel result = reportServer.getReportById(model);
		return JsonUtil.toJson(result);
	}
	
	/*
	 * 系统生产日报
	 */
	@RequestMapping(value="getAutoDailyReport.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getAutoDailyReport(@ModelAttribute("model") ReportModel model,HttpServletRequest request,
			HttpServletResponse response){
		ReportModel result = reportServer.getAutoDailyReport(model);
		return JsonUtil.toJson(result);
	}
	
	/*
	 * 系统生成月报
	 */
	@RequestMapping(value="getAutoMonthReport.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getAutoMonthReport(@ModelAttribute("model") ReportModel model,HttpServletRequest request,
			HttpServletResponse response){
		ReportModel result = reportServer.getAutoMonthReport(model);
		return JsonUtil.toJson(result);
	}
}
