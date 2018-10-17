package com.sdocean.notice.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysql.cj.protocol.x.Notice;
import com.sdocean.common.model.Result;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.notice.model.NoticeModel;
import com.sdocean.station.dao.StationDao;
import com.sdocean.station.model.StationModel;
import com.sdocean.users.model.SysUser;

@Component
public class NoticeDao extends OracleEngine{
	
	@Autowired
	private StationDao stationDao;
	/*
	 * 查询通知列表
	 */
	public List<NoticeModel> getNoticeList(NoticeModel model){
		//初始化返回结果
		List<NoticeModel> list = new ArrayList<>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.stationid,c.title as stationname,a.title,a.text,a.userid,b.username,a.collecttime,a.type,");
		sql.append(" case a.type when 1 then '站点上报' when 2 then '中心下发' else '未知类型' end as typeName");
		sql.append(" from aiot_notice a,sys_user b,aiot_watch_point c");
		sql.append(" where a.userid = b.id");
		sql.append(" and a.stationid = c.id");
		//添加查询条件
		if(model!=null){
			if(model.getStationId()>10000){
				sql.append(" and a.stationid = ").append(model.getStationId());
			}
			if(model.getUserId()>0){
				sql.append(" and a.userid = ").append(model.getUserId());
			}
			if(model.getBeginTime()!=null&&model.getBeginTime().length()>0){
				sql.append(" and a.collecttime >= '").append(model.getBeginTime()).append("'");
			}
			if(model.getEndTime()!=null&&model.getEndTime().length()>0){
				sql.append(" and a.collecttime <= '").append(model.getEndTime()).append("'");
			}
			if(model.getType()>0){
				sql.append(" and a.type = ").append(model.getType());
			}
		}
		//添加排序
		sql.append(" order by a.collecttime desc,c.ordercode");
		list = this.queryObjectList(sql.toString(), NoticeModel.class);
		return list;
	}
	/*
	 * 根据ID查询单个通知
	 */
	public NoticeModel getNoticeById(int id){
		NoticeModel notice = new NoticeModel();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.stationid,c.title as stationname,a.title,a.text,a.userid,b.username,a.collecttime,a.type");
		sql.append(" from aiot_notice a,sys_user b,aiot_watch_point c");
		sql.append(" where a.userid = b.id");
		sql.append(" and a.stationid = c.id");
		sql.append(" and a.id = ").append(id);
		notice = this.queryObject(sql.toString(), NoticeModel.class);
		return notice;
	}
	/*
	 * 通知上报
	 * 一个站点上报给服务中心
	 */
	public Result saveNoticeUp(NoticeModel model){
		//初始化返回结果
		Result result = new Result();
		result.setDotype(result.ADD);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("上传成功");
		StringBuffer sql = new StringBuffer("");
		sql.append(" insert into aiot_notice(title,text,userid,type,stationid)");
		sql.append(" values(?,?,?,?,?)");
		Object[] params = new Object[]{
				model.getTitle(),model.getText(),
				model.getUserId(),1,model.getStationId()
		};
		try {
			this.update(sql.toString(), params);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("上传失败");
		}
		return result;
	}
	/*
	 * 通知下发
	 */
	public Result saveNoticeDown(NoticeModel model){
		System.out.println(JsonUtil.toJson(model));
		//初始化返回结果
		Result result = new Result();
		result.setDotype(result.ADD);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("下发成功");
		//获得站点的集合
		List<StationModel> stations = stationDao.getStationListByIds(model.getStationIds());
		if(stations==null||stations.size()<1){
			result.setResult(result.FAILED);
			result.setMessage("未上传有效的站点集合");
			return result;
		}
		StringBuffer sql = new StringBuffer("");
		sql.append(" insert into aiot_notice(title,text,userid,type,stationid)");
		sql.append(" values ");
		for(int i=0;i<stations.size();i++){
			if(i>0){
				sql.append(",");
			}
			sql.append("('").append(model.getTitle()).append("','").append(model.getText()).append("',");
			sql.append(model.getUserId()).append(",2,").append(stations.get(i).getId()).append(")");
		}
		try {
			this.update(sql.toString(), null);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("下发失败");
		}
		return result;
	}
	/*
	 * 删除通知
	 * 只能删除当前人生成的通知
	 * 只能再生成通知后的1小时内删除
	 */
	public Result deleNotice(NoticeModel model){
		//初始化返回结果
		Result result = new Result();
		result.setDotype(result.DELETE);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("删除成功");
		//根据ID,获得要删除的通知
		NoticeModel notice = new NoticeModel();
		notice = this.getNoticeById(model.getId());
		if(notice==null){
			result.setResult(result.FAILED);
			result.setMessage("找不到通知数据");
			return result;
		}
		//判断上报人与删除人员是否一致
		if(notice.getUserId()!=model.getUserId()){
			result.setResult(result.FAILED);
			result.setMessage("通知只能由生成人员操作!");
			return result;
		}
		//判断通知生成是否再一个小时内
		String collectTime = model.getCollectTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date collect = sdf.parse(collectTime);
			Date now = new Date();
			if((collect.getTime()+1000*60*60)<now.getTime()){
				result.setResult(result.FAILED);
				result.setMessage("该通知已经超过1个小时时间,不能删除!");
				return result;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//执行删除语句
		StringBuffer sql = new StringBuffer("");
		sql.append(" delete from aiot_notice where id = ").append(model.getId());
		try {
			this.update(sql.toString(), null);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("执行删除时错误!");
			return result;
		}
		return result;
	}
	
	/*
	 * 读取该人员未读通知的列表
	 */
	public List<NoticeModel> getNoReadNoticeListByUser(NoticeModel model,SysUser user){
		List<NoticeModel> list = new ArrayList<>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.noticeid,a.userid,a.ifread,a.createtime,");
		sql.append(" b.title,b.text,b.collecttime,b.stationid,b.type,");
		sql.append(" case b.type when 1 then '站点上报' when 2 then '中心下发' else '未知类型' end as typeName");
		sql.append(" from aiot_notice_user a,aiot_notice b");
		sql.append(" where a.noticeid = b.id");
		sql.append(" and a.userid = ").append(user.getId());
		sql.append(" and a.ifread = 0");
		if(model!=null&&model.getStationId()>10000){
			sql.append(" and b.stationid =").append(model.getStationId());
		}
		list = this.queryObjectList(sql.toString(), NoticeModel.class);
		return list;
	}
	
	/*
	 * 获取该人员第一条未读通知的记录
	 */
	public NoticeModel getLastNoReadNoticeListByUser(SysUser user){
		NoticeModel notice = new NoticeModel();
		//获得未读通知信息
		StringBuffer readSql = new StringBuffer("");
		readSql.append(" select a.id,a.noticeid,a.userid,a.ifread,a.createtime,");
		readSql.append(" b.title,b.text,b.collecttime,b.stationid,c.title as stationName,b.type,");
		readSql.append(" case b.type when 1 then '站点上报' when 2 then '中心下发' else '未知类型' end as typeName");
		readSql.append(" from aiot_notice_user a,aiot_notice b,aiot_watch_point c");
		readSql.append(" where a.noticeid = b.id");
		readSql.append(" and b.stationid = c.id");
		readSql.append(" and a.userid = ").append(user.getId());
		readSql.append(" and a.ifread = 0");
		readSql.append(" order by a.createtime limit 1");
		notice = this.queryObject(readSql.toString(), NoticeModel.class);
		//将未读通知信息标记为已读
		if(notice!=null&&notice.getId()>0){
			StringBuffer upSql = new StringBuffer("");
			upSql.append(" update aiot_notice_user set ifread = 1");
			upSql.append(" where id = ").append(notice.getId());
			this.execute(upSql.toString());
		}
		return notice;
	}
	
	/*
	 * 将该用户的所有未读信息设置为已读
	 */
	public void saveReadNoticeByUser(SysUser user){
		StringBuffer sql = new StringBuffer("");
		sql.append(" update aiot_notice_user set ifread = 1");
		sql.append(" where userid =").append(user.getId());
		try {
			this.execute(sql.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
