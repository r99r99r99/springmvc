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
import com.sdocean.warn.model.RangeModel;
import com.sdocean.warn.model.Warn4FirstModel;
import com.sdocean.warn.model.WarnModel;
import com.sdocean.warn.model.WarnValueModel;

@Component
public class RangeDao extends OracleEngine {
	
	/*
	 * 根据站点设备参数,查出他的量程范围
	 */
	public RangeModel getRangeByStationDeviceIndicator(int stationId,int deviceId,String indicatorCode) {
		RangeModel result = new RangeModel();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select id,stationid,deviceid,indicatorcode,mindata,maxdata");
		sql.append(" from aiot_range_data");
		sql.append(" where stationid = ").append(stationId);
		sql.append(" and deviceid = ").append(deviceId);
		sql.append(" and indicatorcode = '").append(indicatorCode).append("'");
		sql.append(" limit 1");
		result = this.queryObject(sql.toString(), RangeModel.class);
		return result;
	}
}
