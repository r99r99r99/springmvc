package com.sdocean.dataQuery.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.sdocean.common.model.HchartsLineData;
import com.sdocean.common.model.HchartsServieModel;
import com.sdocean.dataQuery.model.ComparisonData;
import com.sdocean.dataQuery.model.ComparisonModel;
import com.sdocean.dataQuery.model.GraphModel;
import com.sdocean.device.dao.DeviceDao;
import com.sdocean.device.model.DeviceModel;
import com.sdocean.dictionary.dao.WaterQualityStandardDao;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.indicator.dao.IndicatorDao;
import com.sdocean.indicator.model.IndicatorModel;
import com.sdocean.metadata.dao.MetadataTableDao;
import com.sdocean.metadata.model.MetadataTable;
import com.sdocean.station.model.StationModel;
import com.sdocean.warn.dao.DeviceAlarmDao;
import com.sdocean.warn.dao.RangeDao;
import com.sdocean.warn.model.DeviceAlarmModel;
import com.sdocean.warn.model.RangeModel;

@Component
public class GraphQueryDao extends OracleEngine{
	
	@Resource
	MetadataTableDao tableDao;
	@Resource
	IndicatorDao indicatorDao;
	@Resource
	DeviceDao deviceDao;
	@Resource
	WaterQualityStandardDao wasDao;
	@Resource
	RangeDao rangeDao;
	@Resource
	DeviceAlarmDao deviceAlarmDao;
	
	/*
	 * 得到参数的趋势走势
	 * 屏蔽设备错误代码的数据
	 * 超过监测参数量程的数据,按照量程最大值\最小值来计算
	 * 
	 */
	public List<HchartsServieModel> getEcharts4Graph(GraphModel model){
		//初始化时间格式
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StationModel station = new StationModel();
		station.setId(model.getStationId());
		//初始化返回结果
		List<HchartsServieModel>  list = new ArrayList<>();
		//根据起始时间判断出需要查询的表名的集合
		List<MetadataTable> tables = tableDao.getTables4Meta(station,model.getBeginDate(), model.getEndDate(), 1);
		//得到查询条件中的设备的集合
		List<DeviceModel> devices = model.getDevices();
		//便利设备集合
		for(DeviceModel device:devices){
			//得到查询条件中的参数的集合
			List<IndicatorModel> indicators = device.getIndicators();
			//读取该站点设备的报警码信息
			//获得该站点 该设备的设备报警码信息
			DeviceAlarmModel alarmModel = new DeviceAlarmModel();
			alarmModel.setStationId(model.getStationId());
			alarmModel.setDeviceId(device.getId());
			List<DeviceAlarmModel> alarmList = deviceAlarmDao.getDeviceAlarmList(alarmModel);
			StringBuffer alarmSql = new StringBuffer("");
			for(DeviceAlarmModel alarm:alarmList){
				alarmSql.append(" and !(data = ").append(alarm.getAlarmData());
				alarmSql.append(" and collect_time >='").append(alarm.getBeginTime()).append("'");
				alarmSql.append(" and collect_time <='").append(alarm.getEndTime()).append("'");
				alarmSql.append(" )");
			}
			
			//根据站点\设备\参数,依次查询出数据结果
			for(IndicatorModel indicator:indicators){
				//获得该站点\设备\参数的量程信息
				RangeModel range = rangeDao.getRangeByStationDeviceIndicator(model.getStationId(),device.getId(), indicator.getCode());
				StringBuffer dataSql = new StringBuffer("");
				dataSql.append(" case when 1=0 then 0");
				if(range!=null){
					dataSql.append(" when data < ").append(range.getMindata()).append(" then ").append(range.getMindata());
					dataSql.append(" when data > ").append(range.getMaxdata()).append(" then ").append(range.getMaxdata());
				}
				dataSql.append(" else data end as ydata");
				
				HchartsServieModel servie = new HchartsServieModel();
				servie.setResult(indicator);
				//开始拼接查询数据的SQL语句
				StringBuffer sql = new StringBuffer();
				//定义SELECT语句
				StringBuffer selectSql = new StringBuffer("");
				selectSql.append(" select collect_time as xtime,").append(dataSql);
				StringBuffer whereSql = new StringBuffer(" where 1=1");
				//添加查询条件
				whereSql.append(" and wpid = ").append(station.getId());
				whereSql.append(" and deviceid = ").append(device.getId());
				whereSql.append(" and indicator_code='").append(indicator.getCode()).append("'");
				whereSql.append(" and collect_time>='").append(model.getBeginDate()).append("'");
				whereSql.append(" and collect_time<='").append(model.getEndDate()).append("'");
				//whereSql.append(" and (mark_code is null||length(mark_code)=0)");
				whereSql.append(" and isactive = 1");
				whereSql.append(alarmSql);
				//定义排序语句
				StringBuffer ordersql = new StringBuffer(" order by xtime desc");
				for(int i=0;i<tables.size();i++){
					MetadataTable table = tables.get(i);
					String tableName = table.getTableName();
					StringBuffer fromSql = new StringBuffer(" from ");
					
					fromSql.append(tableName);
					//将每个表的sql语句添加到总语句中
					sql.append(selectSql).append(fromSql).append(whereSql);
					
					if(i<tables.size()-1){
						sql.append(" union all ");
					}
				}
				sql.append(ordersql);
				//初始化单个参数的结果集
				List<HchartsLineData> lds = new ArrayList<>();
				lds = this.queryObjectList(sql.toString(), HchartsLineData.class);
				List<double[]> resu = new ArrayList<>();
				for(HchartsLineData ld:lds){
					try {
						Date da = sdf.parse(ld.getXtime());
						Calendar addTime = Calendar.getInstance();
						addTime.setTime(da);
						addTime.add(Calendar.HOUR, 8);
						da = addTime.getTime();
						double[] res = {da.getTime(),ld.getYdata()};
						resu.add(res);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				servie.setData(resu);
				
				list.add(servie);
			}
		}
		return list;
	}
	
	/*
	 * 为综合对比查询查询提供数据
	 */
	public ComparisonModel getComparisonResult(ComparisonModel model){
		//得到查询条件中的站点列表
		List<StationModel> stations = model.getStations();
		//得到查询条件中的参数
		IndicatorModel indicator = model.getIndicator();
		//初始化select语句
		StringBuffer selectSql = new StringBuffer("select collect_time as xtime,");
		StringBuffer whereSql = new StringBuffer("");
		whereSql.append(" where indicator_code = '").append(indicator.getCode()).append("'");
		whereSql.append(" and collect_time >= '").append(model.getBeginDate()).append("'");
		whereSql.append(" and collect_time <= '").append(model.getEndDate()).append("'");
		whereSql.append(" and collect_type = 1");
		//whereSql.append(" and (mark_code is null||length(mark_code)=0)");
		whereSql.append(" and isactive = 1 ");
		whereSql.append(" and wpid = ");
		StringBuffer orderSql = new StringBuffer(" order by xtime");
		List<ComparisonData> data = new ArrayList<ComparisonData>();
		//遍历站点列表
		for(StationModel station:stations){
			//根据站点  参数得到该站点监测该参数的所有设备的列表
			List<DeviceModel> devices = deviceDao.getDevciesByStationIndicator(station.getId(), indicator.getCode());
			//屏蔽设备报警码
			StringBuffer alarmSql = new StringBuffer("");
			//量程
			StringBuffer rangeSql = new StringBuffer("");
			rangeSql.append(" case when 1=0 then 0");
			for(DeviceModel device:devices){
				//获得该站点 该设备的设备报警码信息
				DeviceAlarmModel alarmModel = new DeviceAlarmModel();
				alarmModel.setStationId(station.getId());
				alarmModel.setDeviceId(device.getId());
				List<DeviceAlarmModel> alarmList = deviceAlarmDao.getDeviceAlarmList(alarmModel);
				for(DeviceAlarmModel alarm:alarmList){
					alarmSql.append(" and !( deviceid = ").append(alarm.getDeviceId());
					alarmSql.append(" and collect_time >='").append(alarm.getBeginTime()).append("'");
					alarmSql.append(" and collect_time <='").append(alarm.getEndTime()).append("'");
					alarmSql.append(" and data = ").append(alarm.getAlarmData());
					alarmSql.append(" )");
				}
				
				//获得该站点 设备 以及参数的量程范围
				RangeModel range = rangeDao.getRangeByStationDeviceIndicator(station.getId(),device.getId(), indicator.getCode());
				if(range!=null){
					rangeSql.append(" when data < ").append(range.getMindata());
					rangeSql.append(" and deviceid = ").append(range.getDeviceId());
					rangeSql.append(" and indicator_code ='").append(range.getIndicatorCode()).append("'");
					rangeSql.append(" then ").append(range.getMindata());
					rangeSql.append(" when data > ").append(range.getMaxdata());
					rangeSql.append(" and deviceid = ").append(range.getDeviceId());
					rangeSql.append(" and indicator_code ='").append(range.getIndicatorCode()).append("'");
					rangeSql.append(" then ").append(range.getMaxdata());
				}
				
			}
			rangeSql.append(" else data end as ydata");
			
			ComparisonData stationData = new ComparisonData();
			stationData.setStation(station);
			//根据站点以及开始结束时间,查询出需要查询的表
			List<MetadataTable> tables = tableDao.getTables4Meta(station, model.getBeginDate(), model.getEndDate(), 1);
			StringBuffer sql = new StringBuffer("select * from (");
			for(int i=0;i<tables.size();i++){
				if(i>0){
					sql.append(" union all ");
				}
				StringBuffer fromSql = new StringBuffer("");
				fromSql.append(" from ").append(tables.get(i).getTableName());
				sql.append(selectSql).append(rangeSql).append(fromSql).append(whereSql).append(station.getId()).append(alarmSql);
			}
			//sql.append(orderSql);
			sql.append(" ) a ").append(orderSql);
			List<HchartsLineData> chartdatas = new ArrayList<HchartsLineData>();
			chartdatas = this.queryObjectList(sql.toString(), HchartsLineData.class);
			stationData.setDatas(chartdatas);
			data.add(stationData);
		}
		model.setData(data);
		return model;
	}
}
