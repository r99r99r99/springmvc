package com.sdocean.page.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
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
import com.sdocean.dataQuery.model.DataQueryModel;
import com.sdocean.device.model.DeviceModel;
import com.sdocean.frame.model.ConfigInfo;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.log.service.OperationLogService;
import com.sdocean.notice.model.NoticeModel;
import com.sdocean.notice.service.NoticeService;
import com.sdocean.page.model.PageResult;
import com.sdocean.page.model.UiColumn;
import com.sdocean.station.model.StationModel;
import com.sdocean.users.model.SysUser;

@Controller
public class NoticeAction {

	private static Logger log = Logger.getLogger(NoticeAction.class);  
	@Autowired
	private NoticeService noticeService;
	@Resource
	OperationLogService logService;
	@Autowired
	private ConfigInfo info;
	
	/*
	 * 跳转到通知上报页面
	 */
	@RequestMapping("info_noticeUp.do")
	public ModelAndView info_noticeUp(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/notice/noticeUp_Info");
	        return mav;  
	}
	/*
	 * 跳转到通知下发页面
	 */
	@RequestMapping("info_noticeDown.do")
	public ModelAndView info_noticeDown(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/notice/noticeDown_Info");
	        return mav;  
	}
	/*
	 * 跳转到通知查询页面
	 */
	@RequestMapping("info_noticeList.do")
	public ModelAndView info_noticeList(HttpServletRequest request,  
	        HttpServletResponse response)throws Exception{
		    ModelAndView mav = new ModelAndView("/"+info.getPageVision()+"/notice/noticeListInfo");
	        return mav;  
	}
	/*
	 * 初始化查询
	 */
	@RequestMapping(value="init_notice.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String init_notice(HttpServletRequest request,
			HttpServletResponse response){
		NoticeModel model = new NoticeModel();
		DateFormat beginDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(new Date());
	    
	    //设置结束时间
	    String endDate = beginDf.format(calendar.getTime());
	    //设置开始时间
	    calendar.add(Calendar.MONTH, -1);
	    String beginDate = beginDf.format(calendar.getTime());
	    
		model.setBeginTime(beginDate);
		model.setEndTime(endDate);
		return JsonUtil.toJson(model);
	}
	
	/*
	 * 查询通知列表
	 */
	@RequestMapping(value="showNoticeList.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String showNoticeList(@ModelAttribute("model") NoticeModel model,HttpServletRequest request,
			HttpServletResponse response){
		PageResult result = new PageResult();
		//为查询结果增加表头
		List<UiColumn> cols = noticeService.getCols4NoticeList();
		result.setCols(cols);
		List<NoticeModel> rows = noticeService.getNoticeList(model);
		result.setRows(rows);
		return JsonUtil.toJson(result);
	}
	/*
	 * 上报通知
	 */
	@RequestMapping(value="saveNoticeUp.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String saveNoticeUp(@ModelAttribute("model") NoticeModel model,HttpServletRequest request,
			HttpServletResponse response){
		//获得操作员的人员信息
		HttpSession session = request.getSession();
		SysUser user = (SysUser) session.getAttribute("user");
		model.setUserId(user.getId());
		Result result = new Result();
		result = noticeService.saveNoticeUp(model);
		logService.saveOperationLog(result,request);
		return result.getMessage();
	}
	/*
	 * 下发通知
	 */
	@RequestMapping(value="saveNoticeDown.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String saveNoticeDown(@ModelAttribute("model") NoticeModel model,HttpServletRequest request,
			HttpServletResponse response){
		HttpSession session = request.getSession();
		SysUser user = (SysUser) session.getAttribute("user");
		model.setUserId(user.getId());
		Result result = new Result();
		result = noticeService.saveNoticeDown(model);
		logService.saveOperationLog(result,request);
		return result.getMessage();
	}
	/*
	 * 删除通知
	 */
	@RequestMapping(value="deleNotice.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String deleNotice(@ModelAttribute("model") NoticeModel model,HttpServletRequest request,
			HttpServletResponse response){
		//获得操作员的人员信息
		HttpSession session = request.getSession();
		SysUser user = (SysUser) session.getAttribute("user");
		model.setUserId(user.getId());
				
		Result result = new Result();
		result = noticeService.deleNotice(model);
		logService.saveOperationLog(result,request);
		return result.getMessage();
	}
	
	/*
	 * 获得当前用户没有读过的通知列表
	 */
	@RequestMapping(value="getNoReadNoticeListByUser.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getNoReadNoticeListByUser(@ModelAttribute("model") NoticeModel model,HttpServletRequest request,
			HttpServletResponse response){
		//获得操作员的人员信息
		HttpSession session = request.getSession();
		SysUser user = (SysUser) session.getAttribute("user");
		List<NoticeModel> list = noticeService.getNoReadNoticeListByUser(model, user);
		return JsonUtil.toJson(list);
	}
	/*
	 * 获取该人员第一条未读通知的记录
	 */
	@RequestMapping(value="getLastNoReadNoticeListByUser.do", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getLastNoReadNoticeListByUser(HttpServletRequest request,
			HttpServletResponse response){
		//获得操作员的人员信息
		HttpSession session = request.getSession();
		SysUser user = (SysUser) session.getAttribute("user");
		NoticeModel  notice = noticeService.getLastNoReadNoticeListByUser(user);
		return JsonUtil.toJson(notice);
	}
}
