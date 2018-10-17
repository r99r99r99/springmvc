package com.sdocean.report.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sdocean.common.model.Result;
import com.sdocean.dataQuery.dao.StatisQueryDao;
import com.sdocean.dataQuery.model.StatData;
import com.sdocean.dataQuery.model.StatisModel;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.page.model.UiColumn;
import com.sdocean.report.dao.ReportDao;
import com.sdocean.report.model.ReportModel;
import com.sdocean.station.dao.StationDao;
import com.sdocean.station.model.StationModel;

@Service
@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRED)
public class ReportServer {
	
	@Autowired
	ReportDao reportDao;
	@Autowired
	StatisQueryDao statisQueryDao;
	@Autowired
	StationDao stationDao;
	/*
	 * 得到日报查询
	 */
	public ReportModel getDailyReport(ReportModel model){
		ReportModel report = new ReportModel();
		//获得站点信息
		StationModel station = stationDao.getStationById(model.getStationId());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//判断是否有以前的历史记录
		report = reportDao.getReport(model);
		if(report==null||report.getId()==0){//系统自动生成
			report = new ReportModel();
			try {
				String reportDate = sdf.format(sdf.parse(model.getReportDate()));
				model.setReportDate(reportDate);
				report = reportDao.getAutoDailyReport(model);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			report.setUserName("系统自动生成");
			
		}else{
			report.setUserName(report.getUserName()+"于"+report.getCollectDate()+"上报");
		}
		return report;
	}
	
	/*
	 * 得到月报查询
	 */
	public ReportModel getMonthReport(ReportModel model){
		System.out.println(JsonUtil.toJson(model));
		ReportModel report = new ReportModel();
		//获得站点信息
		StationModel station = stationDao.getStationById(model.getStationId());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		//判断是否有以前的历史记录
		report = reportDao.getReport(model);
		if(report==null||report.getId()==0){//系统自动生成
			report = new ReportModel();
			try {
				String reportDate = sdf.format(sdf.parse(model.getReportDate()));
				model.setReportDate(reportDate);
				report = reportDao.getAutoMonthReport(model);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			report.setUserName("系统自动生成");
			
		}else{
			report.setUserName(report.getUserName()+"于"+report.getCollectDate()+"上报");
		}
		return report;
	}
	
	/*
	 * 保存报表信息
	 */
	public Result saveReport(ReportModel model){
		return reportDao.saveReport(model);
	}
	
	/*
	 * 获得报表查询的表头
	 */
	public List<UiColumn> getCols4ReportList(){
		List<UiColumn> cols = new ArrayList<UiColumn>();
		
		UiColumn col1 = new UiColumn("id", "id", false, "*");
		UiColumn col2 = new UiColumn("stationId", "stationId", false, "*");
		UiColumn col3 = new UiColumn("站点", "stationName", true, "*");
		UiColumn col4 = new UiColumn("上报时间", "reportDate", true, "*");
		UiColumn col5 = new UiColumn("标题", "reportTitle", true, "*");
		UiColumn col6 = new UiColumn("上报人", "userName", true, "*");
		UiColumn col7 = new UiColumn("创建时间", "collectDate", true, "*");
		cols.add(col1);
		cols.add(col2);
		cols.add(col3);
		cols.add(col4);
		cols.add(col5);
		cols.add(col6);
		cols.add(col7);
		return cols;
	}
	
	/*
	 * 获得报表查询的内容
	 */
	public List<ReportModel> getRows4ReportList(ReportModel model){
		return reportDao.getRows4ReportList(model);
	}
	
	/*
	 * 根据ID获得报表内容
	 */
	public ReportModel getReportById(ReportModel model){
		return reportDao.getReportById(model);
	}
	
	/*
	 * 系统自动生成每日日报
	 */
	public ReportModel getAutoDailyReport(ReportModel model){
		return reportDao.getAutoDailyReport(model);
	}
	
	/*
	 * 系统自动生成月报
	 */
	public ReportModel getAutoMonthReport(ReportModel model){
		return reportDao.getAutoMonthReport(model);
	}
}
