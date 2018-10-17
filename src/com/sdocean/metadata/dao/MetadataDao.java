package com.sdocean.metadata.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sdocean.dataQuery.model.DataImportModel;
import com.sdocean.device.model.DeviceModel;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.indicator.model.IndicatorModel;
import com.sdocean.metadata.model.DeviceCollectModel;
import com.sdocean.metadata.model.HalfHour;
import com.sdocean.metadata.model.MetadataModel;
import com.sdocean.metadata.model.MetadataTable;
import com.sdocean.station.model.StationModel;

@Component
public class MetadataDao extends OracleEngine{
	
	/*
	 * 将通过CSV导入的数据转换成metadata格式
	 */
	public List<MetadataModel> changeMetadata(List<DataImportModel> datas,int collect_type,String markCode){
		List<MetadataModel> list = new ArrayList<MetadataModel>();
		for(DataImportModel data:datas){
			MetadataModel mModel = new MetadataModel();
			mModel.setCollect_time(data.getCollect_time().replace("/", "-"));
			mModel.setCollect_type(collect_type);
			mModel.setWpId(data.getStationId());
			mModel.setDeviceId(data.getDeviceId());
			mModel.setIndicator_code(data.getIndicatorCode());
			mModel.setData(data.getData());
			mModel.setMarkCode(markCode);
			list.add(mModel);
		}
		return list;
	}
	
	/*
	 * 整合数据的采集时间
	 */
	public MetadataModel changeNewMetadata(MetadataModel model){
		DeviceCollectModel dcm = this.getCollectTimeByDevice(model.getWpId(),model.getDeviceId(), model.getCollect_time());
		String collectTime = dcm.getCollectTime();
		String createTime = dcm.getCreateTime();
		model.setCollect_time(collectTime);
		model.setCreateTime(createTime);
		return model;
	}
	
	/*
	 * 根据设备ID,以及传感器的得到结果的时间,得到采集时间
	 */
	public DeviceCollectModel getCollectTimeByDevice(int stationId,int deviceId,String createTime){
		DeviceCollectModel dcm = null;
		StringBuffer sql = new StringBuffer();
		sql.append(" select id,deviceid,starttime,endtime,collecttime,daynum");
		sql.append(" from aiot_device_collect");
		sql.append(" where deviceid = ").append(deviceId);
		sql.append(" and stationId = ").append(stationId);
		sql.append(" and starttime <= date_format('").append(createTime).append("','%H:%i:%s')");
		sql.append(" and endtime > date_format('").append(createTime).append("','%H:%i:%s')");
		sql.append(" limit 1");
		dcm = this.queryObject(sql.toString(), DeviceCollectModel.class);
		if(dcm!=null&&dcm.getCollectTime()!=null&&dcm.getCollectTime().length()>0){
			String day = createTime.substring(0, 11)+dcm.getCollectTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date collect = null;
			try {
				//对采集时间进行时间运算
				collect = new Date(sdf.parse(day).getTime()+dcm.getDaynum()*24*60*60*1000);
				String collectTime = sdf.format(collect);
				dcm.setCollectTime(collectTime);
				dcm.setCreateTime(createTime);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else{
			dcm = new DeviceCollectModel();
			dcm.setCollectTime(createTime);
			dcm.setCreateTime(createTime);
		}
		return dcm;
	}
	
	
	/*
	 * 将metadata存入到基础元数据表中
	 */
	public void saveMetaData(String tableName,MetadataModel bean){
		//保存数据到元数据表中
		StringBuffer sql = new StringBuffer("");
		sql.append("insert into ").append(tableName).append("(collect_time, collect_type, sensor_type_code, wpid, indicator_code, mark_code, data, createtime,deviceid,dataversion)");
		sql.append("values (?,?,?,?,?,?,?,?,?,?)").append(" ON DUPLICATE KEY UPDATE data=VALUES(data)");
		Object[] metaParams = new Object[]{
				bean.getCollect_time(),bean.getCollect_type(),bean.getSensorTypeCode(),
				bean.getWpId(),bean.getIndicator_code(),bean.getMarkCode(),bean.getData(),
				bean.getCreateTime(),bean.getDeviceId(),bean.getDataversion()
		};
		
		this.update(sql.toString(), metaParams);
	}
	
	/*
	 * 根据给出的元数据的时间,判断出在综合元数据中对应的时间
	 */
	public HalfHour getSynDateByMdate(String mdate){
		HalfHour hh = new HalfHour();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select id,starttime,endtime,collecttime,daynum");
		sql.append(" from aiot_halfhour_config");
		sql.append(" where starttime <date_format('").append(mdate).append("','%H:%i:%s')");
		sql.append(" and endtime >= date_format('").append(mdate).append("','%H:%i:%s')");
		sql.append(" limit 1");
		/*sql.append("select starttime,endtime from aiot_halfhour ");
		sql.append(" where starttime <'").append(mdate).append("'");
		sql.append(" and endtime >='").append(mdate).append("'");*/
		hh = this.queryObject(sql.toString(), HalfHour.class);
		String hhcollecttime = "";
		if(hh.getCollectTime().length()>8){
			hhcollecttime = hh.getCollectTime().substring(11, hh.getCollectTime().length());
		}
		String collectTime = mdate.substring(0, 11)+hhcollecttime;
		hh.setCollectTime(collectTime);
		return hh;
	}
	
	/*
	 * 将metadata存入到综合元数据表中
	 */
	public void saveSynData(String tableName,MetadataModel bean){
		StringBuffer syninsertsql = new StringBuffer("");
		//根据给出的元数据时间,判断出综合元数据中对应的时间
		HalfHour hh = new HalfHour();
		hh=this.getSynDateByMdate(bean.getCollect_time());
		//开始进阶SQL语句
		syninsertsql.append(" insert into ").append(tableName).append("(collect_time,data,indicator_code,wpid,collect_type,deviceid)");
		syninsertsql.append(" values('").append(hh.getCollectTime()).append("',").append(bean.getData()).append(",'").append(bean.getIndicator_code());
		syninsertsql.append("',").append(bean.getWpId()).append(",").append(bean.getCollect_type()).append(",").append(bean.getDeviceId()).append(")");
		syninsertsql.append(" ON DUPLICATE KEY UPDATE data=VALUES(data) ");
		/*syninsertsql.append(" select endtime,").append(bean.getData()).append(",'").append(bean.getIndicator_code());
		syninsertsql.append("',").append(bean.getWpId()).append(",").append(bean.getCollect_type()).append(",").append(bean.getDeviceId());
		syninsertsql.append(" from aiot_halfhour");
		syninsertsql.append(" where '").append(bean.getCollect_time()).append("' <= endtime and '") .append(bean.getCollect_time()).append("'  > starttime");
		syninsertsql.append(" ON DUPLICATE KEY UPDATE data=VALUES(data) ");*/
		this.execute(syninsertsql.toString());
	}
	
	/*
	 * 读取某站点,在特定数据表中的该参数的最后一条有效数据
	 */
	public MetadataModel getLastData4StationIndicator(StationModel station,IndicatorModel indicator,MetadataTable table) {
		MetadataModel result = new MetadataModel();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.wpid,a.deviceid,a.collect_time,a.indicator_code,a.data");
		sql.append(" from ").append(table.getTableName()).append(" a,view_stationid_deviceid_indicatorid b,dm_indicator c");
		sql.append(" where a.wpid = b.stationid and a.wpid = ").append(station.getId());
		sql.append(" and a.deviceid = b.deviceid");
		sql.append(" and b.indicatorid = c.id and a.indicator_code = c.code and a.indicator_code = '").append(indicator.getCode()).append("'");
		sql.append(" and a.collect_type = 1");
		sql.append(" order by collect_time desc limit 1");
		result = this.queryObject(sql.toString(), MetadataModel.class);
		return result;
	}
	
	public MetadataModel getLastData4StationDeviceIndicator(StationModel station,DeviceModel device,
			IndicatorModel indicator,MetadataTable table,String collectTime) {
		MetadataModel result = new MetadataModel();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.wpid,a.deviceid,a.collect_time,a.indicator_code,a.data");
		sql.append(" from ").append(table.getTableName()).append(" a,view_stationid_deviceid_indicatorid b,dm_indicator c");
		sql.append(" where a.wpid = b.stationid and a.wpid = ").append(station.getId());
		sql.append(" and a.deviceid = b.deviceid");
		sql.append(" and a.deviceid = ").append(device.getId());
		sql.append(" and b.indicatorid = c.id and a.indicator_code = c.code and a.indicator_code = '").append(indicator.getCode()).append("'");
		sql.append(" and a.collect_type = 1");
		sql.append(" and a.collect_time <= '").append(collectTime).append("'");
		sql.append(" order by collect_time desc limit 1");
		result = this.queryObject(sql.toString(), MetadataModel.class);
		return result;
	}
	
	/*
	 * 读取手动输入数据的最后一条数据
	 */
	public MetadataModel getLastData4Manual(StationModel station,IndicatorModel indicator,String beginTime,String endTime) {
		MetadataModel result = new MetadataModel();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select stationid as wpid,indicatorcode as indicator_code,data,collecttime as collect_time");
		sql.append(" from aiot_metadata_manual");
		sql.append(" where stationid = ").append(station.getId());
		sql.append(" and indicatorcode = '").append(indicator.getCode()).append("'");
		if(beginTime!=null&&beginTime.length()>0) {
			sql.append(" and collecttime >='").append(beginTime).append("'");
		}
		if(endTime!=null&&endTime.length()>0) {
			sql.append(" and collecttime <='").append(endTime).append("'");
		}
		sql.append(" order by collecttime desc limit 1");
		result = this.queryObject(sql.toString(), MetadataModel.class);
		return result ;
	}
}
