package com.sdocean.report.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sdocean.common.model.Result;
import com.sdocean.firstpage.model.WaterStandard;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.report.model.ReportModel;

@Component
public class ReportDao extends OracleEngine{
	
	/*
	 * 查询以前的报表信息
	 */
	public ReportModel getReport(ReportModel model){
		ReportModel report = new ReportModel();
		int type = model.getType();
		String format = "";
		if(type==1){
			format = "'%Y-%m-%d'";
		}else if(type==2){
			format = "'%Y-%m'";
		}else{
			return null;
		}
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.stationid,b.title as stationname,a.type,");
		sql.append(" case a.type when 1 then '日报' when 2 then '月报' else '未知类型' end as typename,");
		sql.append(" a.reportdate,a.collectdate,a.reporttitle,a.reporttext,a.userid,c.realname as username");
		sql.append(" from aiot_report a left join sys_user c on a.userid = c.id,aiot_watch_point b");
		sql.append(" where a.stationid = b.id");
		sql.append(" and a.stationid = ").append(model.getStationId());
		sql.append(" and date_format(a.reportdate,").append(format).append(") = date_format('").append(model.getReportDate()).append("',");
		sql.append(format).append(")");
		sql.append(" and a.type = ").append(model.getType());
		sql.append(" limit 1");
		report = this.queryObject(sql.toString(), ReportModel.class);
		return report;
	}
	 
	
	/*
	 * 保存报表信息
	 */
	public Result saveReport(ReportModel model){
		//初始化返回结果
		Result result = new Result();
		result.setDotype(result.ADD);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("保存成功");
		StringBuffer sql = new StringBuffer("");
		sql.append(" insert into aiot_report(stationid,type,reportdate,collectdate,reporttitle,reporttext,userid)");
		sql.append(" values (?,?,?,?,?,?,?) on duplicate key update collectdate=values(collectdate),");
		sql.append(" reporttitle=values(reporttitle),reporttext=values(reporttext),userid=values(userid)");
		Object[] objects = new Object[]{
				model.getStationId(),model.getType(),model.getReportDate(),
				model.getCollectDate(),model.getReportTitle(),model.getReportText(),model.getUserId()
		};
		try {
			this.update(sql.toString(), objects);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return result;
	}
	
	/*
	 * 获得报表查询的内容
	 */
	public List<ReportModel> getRows4ReportList(ReportModel model){
		List<ReportModel> list = new ArrayList<>();
		//获得这次查询的类型  1 查询日报  2 查询月报
		String format = "";
		int type = model.getType();
		if(type==1){
			format = "'%Y-%m-%d'";
		}else if(type==2){
			format = "'%Y-%m'";
		}else{
			return list;
		}
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.stationid,b.title as stationname,a.type,");
		sql.append(" case a.type when 1 then '日报' when 2 then '月报' else '未知类型' end as typeName,");
		sql.append(" a.reportDate,a.collectdate,a.reporttitle,a.reporttext,");
		sql.append(" a.userid,c.realname as username");
		sql.append(" from aiot_report a,aiot_watch_point b,sys_user c");
		sql.append(" where a.stationid = b.id");
		sql.append(" and a.stationid = ").append(model.getStationId());
		sql.append(" and a.userid = c.id");
		sql.append(" and a.type = ").append(type);
		sql.append(" and date_format(a.reportdate,'%Y-%m-%d') between ");
		sql.append(" date_format('").append(model.getBeginDate()).append("',").append(format).append(")");
		sql.append(" and ");
		sql.append(" date_format('").append(model.getEndDate()).append("',").append(format).append(")");
		//增加排序
		sql.append(" order by a.reportDate desc");
		list = this.queryObjectList(sql.toString(), ReportModel.class);
		return list;
	}
	
	/*
	 * 根据ID获得报表内容
	 */
	public ReportModel getReportById(ReportModel model){
		ReportModel report = new ReportModel();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.stationid,b.title as stationname,a.type,");
		sql.append(" case a.type when 1 then '日报' when 2 then '月报' else '未知类型' end as typeName,");
		sql.append(" a.reportDate,a.collectdate,a.reporttitle,a.reporttext,");
		sql.append(" a.userid,c.realname as username");
		sql.append(" from aiot_report a,aiot_watch_point b,sys_user c");
		sql.append(" where a.stationid = b.id");
		sql.append(" and a.id = ").append(model.getId());
		sql.append(" and a.userid = c.id");
		
		report = this.queryObject(sql.toString(), ReportModel.class);
		return report;
	}
	
	/*
	 * 系统自动生成每日日报
	 */
	public ReportModel getAutoDailyReport(ReportModel model){
		ReportModel report = new ReportModel();
		Object[] param = new Object[] {
				model.getStationId(),
				model.getReportDate()
		};
		String sql = this.handleProcSql("getAutoDailyReport", param);
		report = this.procQueryObject(sql.toString(),ReportModel.class);
		report.setUserName("系统自动生成");
		return report;
	}
	
	/*
	 * 系统自动生成月报
	 */
	public ReportModel getAutoMonthReport(ReportModel model){
		ReportModel report = new ReportModel();
		Object[] param = new Object[] {
				model.getStationId(),
				model.getReportDate()
		};
		String sql = this.handleProcSql("getAutoMonthReport", param);
		report = this.procQueryObject(sql.toString(),ReportModel.class);
		report.setUserName("系统自动生成");
		return report;
	}
}
