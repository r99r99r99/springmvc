package com.sdocean.station.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sdocean.common.model.Result;
import com.sdocean.common.model.SelectTree;
import com.sdocean.device.model.DeviceModel;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.station.model.StationDeviceComm;
import com.sdocean.station.model.StationInfo;
import com.sdocean.station.model.StationModel;

@Component
public class StationCommDao extends OracleEngine {
	
	/*
	 * 查询符合条件的站点配置
	 */
	public List<StationDeviceComm> getStationCommList(StationDeviceComm model){
		List<StationDeviceComm> list = new ArrayList<StationDeviceComm>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.ip,a.port,a.cronExp,a.stationid,b.title as stationname,a.betweenTime,");
		sql.append(" a.lastDataTime,a.functionname,a.writeTimeOut,a.readTimeOut,c.lastMetaDate,");
		sql.append(" m.ProtocalVersion,m.TemplateFile,a.deviceid,d.name as deviceName");
		sql.append(" from aiot_station_device_comm a left join");
		sql.append(" (select wpid,max(endtime) as lastMetaDate from aiot_meta_table where type = 1 and isactive = 1 group by wpid) c");
		sql.append("  on a.stationid = c.wpid");
		sql.append(" left join aiot_metadata_converter_template m on a.stationId = m.stationId,");
		sql.append(" aiot_watch_point b ,device_catalog d");
		sql.append(" where a.stationid = b.id");
		sql.append(" and a.deviceid = d.id");
		
		/*sql.append(" select a.id,a.ip,a.port,a.cronExp,a.stationId,b.title as stationName,");
		sql.append(" a.lastDataTime,a.FunctionName,a.writeTimeOut,a.readTimeOut,a.mainCount,c.lastMetaDate,");
		sql.append(" m.ProtocalVersion,m.TemplateFile,group_concat(n.name,'|')  as DataItem,a.ifsms,case a.ifsms when 0 then '不发送' else '发送' end as ifsmsname");
		sql.append(" from aiot_station_comm a left join");
		sql.append(" (select wpid,max(endtime) as lastMetaDate from aiot_meta_table where type = 1 and isactive = 1 group by wpid) c ");
		sql.append(" on a.stationId = c.wpid");
		sql.append(" left join aiot_metadata_converter_template m on a.stationId = m.stationId");
		sql.append(" left join (select j.stationid,k.name from aiot_station_device_comm j,device_catalog k where j.deviceid = k.id) n on a.stationid = n.stationid");
		sql.append(" ,aiot_watch_point b");
		sql.append(" where a.stationId = b.id and b.isactive = 1");*/
		//添加查询条件
		if(model!=null&&model.getStationId()>10000){
			sql.append(" and a.stationId = ").append(model.getStationId());
		}
		list = this.queryObjectList(sql.toString(), StationDeviceComm.class);
		return list;
	}
	
	/*
	 * 保存站点配置修改
	 */
	public Result saveStationCommChange(StationDeviceComm model){
		//初始化返回结果
		Result result = new Result();
		result.setDotype(result.UPDATE);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("修改成功");
		//判断是否违背站点唯一原则
		StringBuffer checkSql = new StringBuffer("");
		checkSql.append("select count(1) from aiot_station_device_comm where stationid=").append(model.getStationId()).append(" and deviceid=").append(model.getDeviceId()).append(" and id<>").append(model.getId());
		int cou = 0;
		try {
			cou = this.queryForInt(checkSql.toString(), null);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("检查站点唯一性时失败");
			return result;
		}
		if(cou>0){
			result.setResult(result.FAILED);
			result.setMessage("当前站点已有配置");
			return result;
		}
		//保存基本配置
		StringBuffer sql = new StringBuffer("");
		sql.append(" update aiot_station_device_comm set stationid=?,deviceid=?,betweentime=?,lastdatatime=?,ip=?,port=?,cronExp=?,functionName=?,writetimeout=?,readtimeOut=?");
		sql.append(" where id=?");
		Object[] params = new Object[]{
				model.getStationId(),model.getDeviceId(),model.getBetweenTime(),
				model.getLastDataTime(),model.getIp(),model.getPort(),model.getCronExp(),
				model.getFunctionName(),model.getWriteTimeOut(),model.getReadTimeOut(),
				model.getId()
		};
		int res = 0;
		try {
			res = this.update(sql.toString(), params);
		} catch (Exception e) {
			e.printStackTrace();
			result.setResult(result.FAILED);
			result.setMessage("保存基本配置时失败");
			return result;
		}
		
		return result;
	}
	
	/*
	 * 保存新站点配置
	 */
	public Result saveNewStationComm(StationDeviceComm model){
		//初始化返回结果
		Result result = new Result();
		result.setDotype(result.ADD);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("保存成功");
		//判断是否违背站点唯一性原则
		StringBuffer checkSql = new StringBuffer("");
		checkSql.append("select count(1) from aiot_station_device_comm where stationid=").append(model.getStationId()).append(" and deviceid=").append(model.getDeviceId());
		int cou = 0;
		try {
			cou = this.queryForInt(checkSql.toString(), null);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("检查站点唯一性时失败");
			return result;
		}
		if(cou>0){
			result.setResult(result.FAILED);
			result.setMessage("当前站点已有配置");
			return result;
		}
		
		StringBuffer sql = new StringBuffer("");
		sql.append(" insert into aiot_station_device_comm(stationid,deviceid,betweentime,lastdatatime,ip,port,cronExp,functionName,writetimeout,readtimeOut)");
		sql.append(" values(?,?,?,?,?,?,?,?,?,?)");
		Object[] params = new Object[]{
				model.getStationId(),model.getDeviceId(),model.getBetweenTime(),
				model.getLastDataTime(),model.getIp(),model.getPort(),model.getCronExp(),
				model.getFunctionName(),model.getWriteTimeOut(),model.getReadTimeOut()
		};
		int res = 0;
		try {
			res = this.update(sql.toString(), params);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("保存基本信息时失败");
		}
		
		return result;
	}
	
	/*
	 * 得到该站点下的设备列表,并选中已经查询的设备
	 */
	public List<SelectTree> getDevciesByStation4Tree(StationModel model){
		List<SelectTree> list = new ArrayList<SelectTree>();
		//得到第一层数据
		SelectTree first = new SelectTree();
		first.setName("站点列表");
		first.setIsExpanded(true);
		first.setIsActive(true);
		//读取该站点已经查询的数据
		StringBuffer msql = new StringBuffer("");
		msql.append(" select deviceid as id from aiot_station_device_comm where stationid = ").append(model.getId());
		List<DeviceModel> devices = this.queryObjectList(msql.toString(), DeviceModel.class);
		String item = "0";
		for(DeviceModel device:devices){
			item = item + ',' + device.getId();
		}
		
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id as id ,a.name,'true' as isExpanded,");
		sql.append("  case when a.id in (").append(item).append(") then 'true' else 'false' end as selected");
		sql.append(" from device_catalog a,map_awp_device_catalog b");
		sql.append(" where b.device_catalog_id = a.id");
		sql.append(" and b.aiot_watch_point_id =").append(model.getId());
		List<SelectTree> childlist = new ArrayList<SelectTree>();
		childlist = this.queryObjectList(sql.toString(), SelectTree.class);
		first.setChildren(childlist);
		list.add(first);
		return list;
	}
	
	/*
	 * 根据站点读取当前站点的信息
	 */
	public StationInfo getStationInfoByWpId(StationInfo model){
		StationInfo result = new StationInfo();
		StringBuffer sql = new StringBuffer("");
		sql.append("select id,stationid,infomation from aiot_station_info where stationid = ").append(model.getStationId());
		result = this.queryObject(sql.toString(), StationInfo.class);
		if(result==null){
			result = new StationInfo();
			result.setStationId(model.getStationId());
		}
		return result;
	}
	
	/*
	 * 保存站点的信息展示
	 */
	public Result saveStationInfomation(StationInfo model){
		//初始化返回结果
		Result result = new Result();
		result.setDotype(result.UPDATE);
		result.setModel(JsonUtil.toJson(model));
		result.setMessage("保存成功");
		result.setResult(result.SUCCESS);
		StringBuffer sql = new StringBuffer();
		sql.append("insert into aiot_station_info(stationid,infomation) values(?,?) on DUPLICATE KEY UPDATE infomation=values(infomation)");
		Object[] params = new Object[]{
				model.getStationId(),model.getInfomation()
		};
		int res = 0;
		try {
			res = this.update(sql.toString(), params);
		} catch (Exception e) {
			result.setMessage("保存失败");
			result.setResult(result.FAILED);
		}
		return result;
	}
	/*
	 * 删除站点信息
	 */
	public Result deleStationComm(StationDeviceComm model){
		//初始化返回参数
		Result result = new Result();
		result.setDotype(Result.DELETE);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(Result.SUCCESS);
		result.setMessage("删除成功");
		//删除操作
		StringBuffer sql = new StringBuffer("");
		sql.append("delete from aiot_station_device_comm where id = ").append(model.getId());
		int res = 0;
		try {
			res = this.update(sql.toString(), null);
		} catch (Exception e) {
			result.setResult(Result.FAILED);
			result.setMessage("删除失败");
		}
		return result;
	}
}
