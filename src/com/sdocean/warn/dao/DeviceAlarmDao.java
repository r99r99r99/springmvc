package com.sdocean.warn.dao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.sdocean.common.model.Result;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.station.dao.StationDao;
import com.sdocean.station.model.StationModel;
import com.sdocean.users.model.SysUser;
import com.sdocean.warn.model.DeviceAlarmModel;
import com.sdocean.warn.model.Warn4FirstModel;
import com.sdocean.warn.model.WarnModel;
import com.sdocean.warn.model.WarnValueModel;

@Component
public class DeviceAlarmDao extends OracleEngine {
	
	/*
	 * 获得站点  设备的报警值列表
	 */
	public List<DeviceAlarmModel> getDeviceAlarmList(DeviceAlarmModel model){
		List<DeviceAlarmModel> list = new ArrayList<>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.stationid,a.deviceid,a.configid,b.title as configname,");
		sql.append(" a.alarmdata,a.begintime,a.endtime");
		sql.append(" from aiot_device_alarm a,aiot_device_alarm_config b");
		sql.append(" where a.configid = b.id");
		//添加查询条件
		if(model!=null&&model.getStationId()>0) {
			sql.append(" and a.stationid = ").append(model.getStationId());
		}
		if(model!=null&&model.getDeviceId()>0) {
			sql.append(" and a.deviceid = ").append(model.getDeviceId());
		}
		if(model!=null&&model.getCollectTime()!=null&&model.getCollectTime().length()>0) {
			sql.append(" and a.beginTime <= '").append(model.getCollectTime()).append("'");
			sql.append(" and a.endTime >= '").append(model.getCollectTime()).append("'");
		}
		//增加排序
		sql.append(" order by a.stationid,a.deviceid,b.ordercode");
		list = this.queryObjectList(sql.toString(), DeviceAlarmModel.class);
		return list;
	}
}
