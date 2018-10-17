package com.sdocean.domain.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.sdocean.common.model.Result;
import com.sdocean.domain.model.DomainIndicator;
import com.sdocean.domain.model.DomainLevelModel;
import com.sdocean.domain.model.DomainModel;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.indicator.model.IndicatorModel;
import com.sdocean.metadata.dao.MetadataDao;
import com.sdocean.metadata.dao.MetadataTableDao;
import com.sdocean.metadata.model.MetadataTable;
import com.sdocean.station.model.StationModel;

@Component
public class DomainDao extends OracleEngine{
	
	@Resource
	MetadataTableDao tableDao;
	/*
	 * 展示功能区的列表
	 */
	public List<DomainModel> getDomainList(DomainModel model){
		//初始化返回结果
		List<DomainModel> list = new ArrayList<DomainModel>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.code,a.name,a.remark,a.isactive,");
		sql.append(" b.value as isactivename,a.ordercode");
		sql.append(" from aiot_domain a,sys_public b");
		sql.append(" where a.isactive = b.classid");
		sql.append(" and b.parentcode = '0004'");
		//添加查询条件
		if(model.getIsactive()<2){ //当isactive = 2时,代表查询所有功能区
			sql.append(" and a.isactive = ").append(model.getIsactive());
		}
		if(model.getName()!=null&&model.getName().length()>0){
			sql.append(" and a.name like '%").append(model.getName()).append("%' ");
		}
		//添加排序
		sql.append(" order by a.ordercode");
		list = this.queryObjectList(sql.toString(), DomainModel.class);
		return list;
	}
	
	/*
	 * 新增功能区
	 */
	public Result saveNewDomain(DomainModel model){
		//初始化返回结果
		Result result = new Result();
		result.setDotype(result.ADD);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("新增成功");
		//查询code  name是否重复
		StringBuffer checkSql = new StringBuffer("");
		checkSql.append(" select count(1) from aiot_domain where code = '").append(model.getCode()).append("' ");
		checkSql.append(" or name = '").append(model.getName()).append("'");
		int check = 0;
		try {
			check = this.queryForInt(checkSql.toString(), null);
		} catch (Exception e) {
			// TODO: handle exception
			result.setResult(result.FAILED);
			result.setMessage("查询唯一性时失败");
			return result;
		}
		if(check>0){
			result.setResult(result.FAILED);
			result.setMessage("编码或名称重复");
			return result;
		}
		
		StringBuffer sql = new StringBuffer("");
		sql.append(" insert into aiot_domain(code,name,remark,isactive,ordercode)");
		sql.append(" values(?,?,?,?,?)");
		Object[] params = new Object[]{
				model.getCode(),model.getName(),model.getRemark(),model.getIsactive(),model.getOrderCode()
		};
		try {
			this.update(sql.toString(), params);
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage("新增失败");
			result.setResult(result.FAILED);
		}
		return result;
	}
	
	/*
	 * 修改功能区
	 */
	public Result saveChangeDomain(DomainModel model){
		//初始化返回结果
		Result result = new Result();
		result.setDotype(result.ADD);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("新增成功");
		
		//查询code  name是否重复
		StringBuffer checkSql = new StringBuffer("");
		checkSql.append(" select count(1) from aiot_domain where (code = '").append(model.getCode()).append("' ");
		checkSql.append(" or name = '").append(model.getName()).append("') and id <>").append(model.getId());
		int check = 0;
		try {
			check = this.queryForInt(checkSql.toString(), null);
		} catch (Exception e) {
			// TODO: handle exception
			result.setResult(result.FAILED);
			result.setMessage("查询唯一性时失败");
			return result;
		}
		if(check>0){
			result.setResult(result.FAILED);
			result.setMessage("编码或名称重复");
			return result;
		}		
		StringBuffer sql = new StringBuffer("");
		sql.append(" update aiot_domain set code=?,name=?,remark=?,isactive=?,ordercode=? ");
		sql.append(" where id = ?");
		Object[] params = new Object[]{
				model.getCode(),model.getName(),model.getRemark(),model.getIsactive(),model.getOrderCode(),model.getId()
		};
		try {
			this.update(sql.toString(), params);
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage("新增失败");
			result.setResult(result.FAILED);
		}
		return result;
	}
	
	/*
	 * 停用功能区
	 */
	public Result deleDomain(DomainModel model){
		//初始化返回结果
		Result result = new Result();
		result.setDotype(result.DELETE);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("禁用成功");
		StringBuffer sql = new StringBuffer("");
		sql.append("update aiot_domain set isactive = 0 where id = ").append(model.getId());
		try {
			this.update(sql.toString(), null);
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage("停用失败");
			result.setResult(result.FAILED);
		}
		return result;
	}
	
	/*
	 * 保存功能区--站点--参数权限
	 */
	public Result saveDomainStationIndicator(DomainModel model){
		//初始化返回结果
		Result result = new Result();
		result.setDotype(result.ADD);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("保存成功");
		//删除功能区站点之间原有的记录
		StringBuffer deleStation = new StringBuffer("");
		deleStation.append("delete from sys_domain_station where domainid = ").append(model.getId());
		try {
			this.execute(deleStation.toString());
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("重置功能区站点时错误");
			return result;
		}
		//处理站点集合
		String[] stationids = model.getStationIds().split(",");
		StringBuffer stationValue = new StringBuffer("(0,0)");
		for(int i=0;i<stationids.length;i++){
			stationValue.append(",(").append(model.getId()).append(",").append(stationids[i]).append(")");
		}
		//执行插入语句
		StringBuffer addStation = new StringBuffer("");
		addStation.append(" insert into sys_domain_station(domainid,stationid)");
		addStation.append(" values").append(stationValue);
		addStation.append(" on duplicate  key update stationid = values(stationid)");
		try {
			this.execute(addStation.toString());
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("保存功能区站点时错误");
			return result;
		}
		
		//删除功能区参数之间原有的记录
		StringBuffer deleIndi = new StringBuffer("");
		deleIndi.append("delete from sys_domain_indicator where domainid = ").append(model.getId());
		try {
			this.execute(deleIndi.toString());
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("重置功能区参数时错误");
			return result;
		}
		String[] indicators = model.getIndicatorCodes().split(",");
		StringBuffer indiValue = new StringBuffer("(0,'0')");
		for(int i=0;i<indicators.length;i++){
			indiValue.append(",(").append(model.getId()).append(",'").append(indicators[i]).append("')");
		}
		//执行插入语句
		StringBuffer addIndicator = new StringBuffer("");
		addIndicator.append(" insert into sys_domain_indicator(domainid,indicatorcode)");
		addIndicator.append(" values ").append(indiValue);
		addIndicator.append(" on duplicate  key update indicatorcode = values(indicatorcode)");
		
		try {
			this.execute(addIndicator.toString());
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("保存功能区参数时错误");
			return result;
		}
		return result;
	}
	
	/*
	 * 根据站点获得该站点所属的功能区列表
	 */
	public List<DomainModel> getDomainModelsByStation(StationModel station){
		List<DomainModel> list = new ArrayList<>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.code,a.name,a.remark,a.isactive,a.ordercode");
		sql.append(" from aiot_domain a,sys_domain_station b");
		sql.append(" where a.id = b.domainid and a.isactive = 1");
		sql.append(" and stationid =").append(station.getId());
		sql.append(" order by a.ordercode");
		list = this.queryObjectList(sql.toString(), DomainModel.class);
		return list;
	}
	
	/*
	 * 获得该功能区下的关注的参数列表
	 */
	public List<IndicatorModel> getIndicatorsByDomain(DomainModel domain){
		//初始化返回结果
		List<IndicatorModel> list = new ArrayList<>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.code,a.title,a.groupid,a.unitid,b.logo as unitname,");
		sql.append(" a.description,a.isactive,a.ordercode");
		sql.append(" from dm_indicator a,g_unit b,sys_domain_indicator c");
		sql.append(" where a.code = c.indicatorcode and a.unitid = b.id");
		sql.append(" and a.isactive = 1 and c.domainid = ").append(domain.getId());
		sql.append(" order by a.ordercode");
		list = this.queryObjectList(sql.toString(), IndicatorModel.class);
		return list;
	}
	
	/*
	 * 根据站点以及参数获得实时数据
	 */
	public List<DomainIndicator> getDomainIndicator4Now(StationModel station,List<IndicatorModel> indicators){
		List<DomainIndicator> list = new ArrayList<DomainIndicator>();
		//获得当前时间
		Date nowDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String collect_time = sdf.format(nowDate);
		//得到需要查询的表
		MetadataTable table = tableDao.getOneTable(station, collect_time, 1);
		//遍历参数表,得到每个参数的最终结果
		for(IndicatorModel indicator:indicators){
			DomainIndicator dd = new DomainIndicator();
			StringBuffer sql = new StringBuffer("");
			sql.append(" select collect_time as collecttime,data");
			sql.append(" from ").append(table.getTableName());
			sql.append(" where wpid = ").append(station.getId());
			sql.append(" and indicator_code = '").append(indicator.getCode()).append("'");
			sql.append(" and data is not null and data <> 88888");
			sql.append(" order by collect_time desc limit 1");
			dd=this.queryObject(sql.toString(), DomainIndicator.class);
			if(dd!=null&&dd.getData()!=null&&dd.getData().length()>0){
				dd.setIndicatorCode(indicator.getCode());
				dd.setIndicatorName(indicator.getTitle());
				dd.setData(dd.getData()+"("+indicator.getUnitName()+")");
				list .add(dd);
			}
		}
		return list;
	}
	
	/*
	 * 获得某功能区下的等级列表
	 */
	public List<DomainLevelModel> getDomainLevelListByDomain(DomainLevelModel model){
		List<DomainLevelModel> list = new ArrayList<DomainLevelModel>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select id,domainid,code,name,remark,color,ordercode");
		sql.append(" from sys_domain_level");
		sql.append(" where 1=1");
		//添加查询条件
		if(model!=null&&model.getDomainId()>0) {
			sql.append(" and domainid = ").append(model.getDomainId());
		}
		sql.append(" order by ordercode");
		list = this.queryObjectList(sql.toString(), DomainLevelModel.class);
		return list;
	}
}
