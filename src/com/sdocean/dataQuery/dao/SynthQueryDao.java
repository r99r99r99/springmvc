package com.sdocean.dataQuery.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.sdocean.dataQuery.model.DataQueryModel;
import com.sdocean.device.model.DeviceModel;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.indicator.dao.IndicatorDao;
import com.sdocean.indicator.model.IndicatorModel;
import com.sdocean.metadata.dao.MetadataTableDao;
import com.sdocean.metadata.model.MetadataTable;
import com.sdocean.page.model.UiColumn;
import com.sdocean.station.dao.StationDeviceDao;
import com.sdocean.station.model.StationDeviceModel;
import com.sdocean.station.model.StationModel;
import com.sdocean.warn.dao.DeviceAlarmDao;
import com.sdocean.warn.dao.RangeDao;
import com.sdocean.warn.model.DeviceAlarmModel;
import com.sdocean.warn.model.RangeModel;

@Component
public class SynthQueryDao extends OracleEngine{
	
	@Resource
	MetadataTableDao tableDao;
	@Resource
	IndicatorDao indicatorDao;
	@Resource
	StationDeviceDao stationDeviceDao;
	@Resource
	RangeDao rangeDao;
	@Resource
	DeviceAlarmDao deviceAlarmDao;
	/*
	 * 为综合查询添加表头
	 */
	public List<UiColumn> getCols4SynQuery(DataQueryModel model){
		
		List<DeviceModel> devices = model.getDevices();
		
		
		List<UiColumn> cols = new ArrayList<UiColumn>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select '时间' as displayName,'collect_time' as field,'true' as visible,'*' as width");
		for(DeviceModel device:devices){
			//遍历设备内的参数
			StringBuffer indicatorSql = new StringBuffer("(0");
			for(IndicatorModel indicator:device.getIndicators()){
				indicatorSql.append(",").append(indicator.getId());
			}
			indicatorSql.append(")");
			sql.append(" union all ");
			sql.append(" select case when b.logo is null or length(b.logo)=0 then a.title else concat(a.title,'(',b.logo,')') end  as displayName,");
			sql.append(" concat(a.code,c.catalogid) as field,'true' as visible,'*' as width");
			sql.append(" from dm_indicator a left join g_unit b ");
			sql.append(" on a.unitid = b.id ");
			sql.append(" ,device_catalog_indicator c,device_catalog d");
			sql.append(" where a.id = c.indicatorid");
			sql.append(" and c.catalogid = d.id");
			sql.append(" and a.isactive = 1");
			sql.append(" and c.catalogid =").append(device.getId());
			sql.append(" and a.id in ").append(indicatorSql);
		}
		cols = this.queryObjectList(sql.toString(), UiColumn.class);
		return cols;
	}
	
	/*
	 * 为综合查询查询结果
	 */
	public List<Map<String, Object>> getRows4SynQuery(DataQueryModel model){
		List<Map<String, Object>> rows = null;
		StationModel station = new StationModel();
		station.setId(model.getStationId());
		//根据起始时间判断出需要查询的表名的集合
		List<MetadataTable> tables = tableDao.getTables4Meta(station,model.getBeginDate(), model.getEndDate(), 2);
		//定义总的查询语句
		StringBuffer sql = new StringBuffer("");
		//定义select部分
		StringBuffer selectSql = new StringBuffer(" select collect_time ");
		//定义where部分
		StringBuffer whereSql = new StringBuffer(" where ");
		whereSql.append(" wpid = ").append(model.getStationId()).append(" and (1=0 ");
		//定义groupby部分
		StringBuffer groupSql = new StringBuffer(" group by collect_time");
		//定义having部分
		//StringBuffer havingSql = new StringBuffer(" having 1=0 ");
		//定义排序部分
		StringBuffer orderSql = new StringBuffer(" order by collect_time desc");
		List<DeviceModel> devices = model.getDevices();
		for(DeviceModel device:devices){
			//得到该站点\设备的有效数字位数
			int pointNum = 2;
			StationDeviceModel sdmodel = new StationDeviceModel();
			sdmodel.setStationId(model.getStationId());
			sdmodel.setDeviceId(device.getId());
			StationDeviceModel sdm = stationDeviceDao.getStationDeviceByWidDid(sdmodel);
			if(sdm!=null){
				pointNum = sdm.getPointNum();
			}
			
			//获得该站点 该设备的设备报警码信息
			DeviceAlarmModel alarmModel = new DeviceAlarmModel();
			alarmModel.setStationId(model.getStationId());
			alarmModel.setDeviceId(model.getDeviceId());
			List<DeviceAlarmModel> alarmList = deviceAlarmDao.getDeviceAlarmList(alarmModel);
			List<IndicatorModel> indicators = device.getIndicators();
			StringBuffer indicatorCodes = new StringBuffer("'0'");
			for(IndicatorModel indicator:indicators){
				StringBuffer dataSql = new StringBuffer("");
				dataSql.append(" ROUND(sum(if(indicator_code='").append(indicator.getCode()).append("' and deviceid = ").append(device.getId()).append(",data,0)) ,").append(pointNum).append(")");
				selectSql.append(",case when 1=0 then '0'");
				if(model.getQueryType().equals("0")){
					for(DeviceAlarmModel alarm:alarmList){
						selectSql.append(" when ").append(dataSql).append("=").append(alarm.getAlarmData());
						selectSql.append(" and collect_time >= '").append(alarm.getBeginTime()).append("'");
						selectSql.append(" and collect_time <= '").append(alarm.getEndTime()).append("'");
						selectSql.append(" then '").append(alarm.getConfigName()).append("'");
					}
					//获得该站点\设备\参数的量程信息
					RangeModel range = rangeDao.getRangeByStationDeviceIndicator(model.getStationId(), device.getId(), indicator.getCode());
					if(range!=null){
						selectSql.append(" when ").append(dataSql).append("<").append(range.getMindata());
						selectSql.append(" then '").append("<").append(range.getMindata()).append("'");
						selectSql.append(" when ").append(dataSql).append(">").append(range.getMaxdata());
						selectSql.append(" then '").append(">").append(range.getMaxdata()).append("'");
					}
				}
				selectSql.append(" else cast(").append(dataSql).append(" as char) end as ");
				selectSql.append(indicator.getCode()).append(device.getId());
				
				
				//selectSql.append(", sum(if(indicator_code='").append(indicator.getCode()).append("' and deviceid = ").append(device.getId()).append(",data,0)) as ");
				//selectSql.append(indicator.getCode()).append(device.getId());
				
				indicatorCodes.append(",'").append(indicator.getCode()).append("'");
				
				//havingSql.append("||sum(if(indicator_code='").append(indicator.getCode()).append("' and deviceid =").append(device.getId()).append(",data,0)) <> 0");
			}
			whereSql.append(" or ( deviceid = ").append(device.getId()).append(" and indicator_code in (").append(indicatorCodes).append("))");
		}
		whereSql.append(")");
		
		//添加时间查询条件
		//增加时间参数.
		if(model.getBeginDate()!=null){
			whereSql.append(" and collect_time >= '").append(model.getBeginDate()).append("'");
		}
		if(model.getEndDate()!=null){
			whereSql.append(" and collect_time <= '").append(model.getEndDate()).append("'");
		}	
		
		//遍历TABLELIST组成查询语句
		for(int i=0;i<tables.size();i++){
			String tablename = tables.get(i).getTableName();
			String fromSql = " from "+tablename;
			sql.append(selectSql).append(fromSql).append(whereSql).append(groupSql);//.append(havingSql);
			if(i<tables.size()-1){
				sql.append(" union all ");
			}
		}
		sql.append(orderSql);
		rows = this.queryForList(sql.toString());
		return rows;
	}
	
}
