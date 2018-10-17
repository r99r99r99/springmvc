package com.sdocean.beach.dao;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.sdocean.beach.model.BeachCodeModel;
import com.sdocean.beach.model.BeachConfigModel;
import com.sdocean.beach.model.BeachDataModel;
import com.sdocean.beach.model.BeachDegreeModel;
import com.sdocean.beach.model.BeachGroupModel;
import com.sdocean.beach.model.BeachPointModel;
import com.sdocean.beach.model.BeachResultModel;
import com.sdocean.beach.model.BeachStatisModel;
import com.sdocean.beach.model.BeachTargetModel;
import com.sdocean.dataQuery.model.StatData;
import com.sdocean.dataQuery.model.StatisModel;
import com.sdocean.device.model.DeviceModel;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.indicator.model.IndicatorModel;
import com.sdocean.metadata.dao.MetadataDao;
import com.sdocean.metadata.dao.MetadataTableDao;
import com.sdocean.metadata.model.MetadataModel;
import com.sdocean.metadata.model.MetadataTable;
import com.sdocean.station.model.StationModel;

@Component
public class BeachDao  extends OracleEngine{
	
	private int BeachDomainId = 11;  //获得海水浴场的功能区ID
	
	@Resource
	MetadataTableDao tableDao;
	@Resource
	MetadataDao dataDao;
	

	/*
	 * 获得评价标准中所有参数的列表
	 */
	public 	List<BeachCodeModel> getBeachCodes(){
		List<BeachCodeModel> list = new ArrayList<>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.code,a.name,a.type,a.unit,a.groupid,b.name as groupname");
		sql.append(" from aiot_beach_code a,aiot_beach_group b");
		sql.append(" where a.groupid = b.id;");
		list = this.queryObjectList(sql.toString(), BeachCodeModel.class);
		return list;
	}
	
	/*
	 * 得到某参数在手动上报表中的最后的数据
	 */
	public BeachDataModel getLastImportBeachDataByCode(StationModel station,BeachCodeModel beachCode,String collectTime) {
		BeachDataModel result = new BeachDataModel();
		StringBuffer sql = new StringBuffer("");
		if(beachCode.getType()==1) {
			sql.append(" select a.code,a.collecttime,a.data,concat(a.data,'").append(beachCode.getUnit()).append("') as dataValue");
			sql.append(",").append(beachCode.getType()).append(" as typeid,").append(beachCode.getGroupId()).append(" as groupid,'");
			sql.append(beachCode.getName()).append("' as name");
			sql.append(" from aiot_beach_data a");
			sql.append(" where a.stationid = ").append(station.getId());
			sql.append(" and code = '").append(beachCode.getCode()).append("'");
			sql.append(" and collecttime <= '").append(collectTime).append("'");
			sql.append(" order by collecttime desc");
			sql.append(" limit 1");
		}
		if(beachCode.getType()==2) {
			sql.append(" select a.code,a.collecttime,a.data,b.value as datavalue");
			sql.append(",").append(beachCode.getType()).append(" as typeid,").append(beachCode.getGroupId()).append(" as groupid,'");
			sql.append(beachCode.getName()).append("' as name");
			sql.append(" from aiot_beach_data a,aiot_beach_config b");
			sql.append(" where a.data = b.data");
			sql.append(" and a.stationid = ").append(station.getId());
			sql.append(" and a.code = '").append(beachCode.getCode()).append("'");
			sql.append(" and a.data = b.data");
			sql.append(" and a.code = b.code");
			sql.append(" and collecttime <= '").append(collectTime).append("'");
			sql.append(" order by collecttime desc");
			sql.append(" limit 1");
		}
		result = this.queryObject(sql.toString(), BeachDataModel.class);
		return result;
	}
	
	public BeachTargetModel getBeachTargetByBeachcode(StationModel station,BeachCodeModel beachCode) {
		BeachTargetModel result = new BeachTargetModel();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select deviceid,indicatorcode,tounit");
		sql.append(" from aiot_beach_target");
		sql.append(" where stationid = ").append(station.getId());
		sql.append(" and code = '").append(beachCode.getCode()).append("'");
		result = this.queryObject(sql.toString(), BeachTargetModel.class);
		return result;
	}
	
	
	/*
	 * 得到海水浴场所有参数的实效数据
	 */
	public List<BeachDataModel> getLastBeachData(StationModel station,String collectTime){
		//初始化自动数据需要查询的表
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
		String nowDate = df.format(new Date());
		MetadataTable table = tableDao.getOneTable(station, collectTime, 1);
		//初始化所有参数结果的集合
		List<BeachDataModel> beachDatas = new ArrayList<>();
				
		//获得评价标准中所有参数的列表
		List<BeachCodeModel> beachCodes = this.getBeachCodes();
		for(BeachCodeModel beachCode:beachCodes) {
			String unit = beachCode.getUnit();
			if(unit==null||unit.equals("-")) {
				 unit = "";
					beachCode.setUnit(unit);
			}
			//得到该参数在手动上报表中的最后的数据
			BeachDataModel importData = this.getLastImportBeachDataByCode(station, beachCode,collectTime);
					
			//得到该参数在自动数据系统中的映射
			BeachTargetModel target = this.getBeachTargetByBeachcode(station, beachCode);
			if(target!=null&&target.getDeviceId()>0&&target.getIndicatorCode()!=null) {
				DeviceModel device = new DeviceModel();
				device.setId(target.getDeviceId());
				IndicatorModel indicator = new IndicatorModel();
				indicator.setCode(target.getIndicatorCode());
				MetadataModel lastD = dataDao.getLastData4StationDeviceIndicator(station, device,indicator, table,collectTime);
				if(importData!=null) {
					try {
						if(df.parse(lastD.getCollect_time()).after(df.parse(importData.getCollectTime()))) {
							importData.setCollectTime(lastD.getCollect_time());
							importData.setData(lastD.getData());
							importData.setDataValue("data"+unit);
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else {
					if(lastD!=null) {
						importData = new BeachDataModel();
						importData.setCollectTime(lastD.getCollect_time());
						importData.setDataValue("data"+unit);
						importData.setData(lastD.getData());
						importData.setCode(beachCode.getCode());
						importData.setName(beachCode.getName());
						importData.setTypeId(beachCode.getType());
						importData.setGroupId(beachCode.getGroupId());
						importData.setGroupName(beachCode.getGroupName());
					}
				}
			}
			if(importData!=null) {
				beachDatas.add(importData);
			}
		}
		//查询每组数据的水质等级
		for(BeachDataModel beachData:beachDatas) {
			StringBuffer levelSql = new StringBuffer("");
			int levelid = 0;
			if(beachData.getTypeId()==1) {
				levelSql.append(" select a.levelid");
				levelSql.append(" from aiot_beach_threshold a");
				levelSql.append(" where a.code = '").append(beachData.getCode()).append("'");
				levelSql.append(" and case when mincal = 1 then ").append(beachData.getData()).append(" >= min");
				levelSql.append(" else ").append(beachData.getData()).append(" >min end");
				levelSql.append(" and case when maxcal = 1 then ").append(beachData.getData()).append(" <= max");
				levelSql.append(" else ").append(beachData.getData()).append(" < max end");
				levelSql.append(" order by a.levelid desc limit 1");
			}else {
				levelSql.append(" select a.levelid");
				levelSql.append(" from aiot_beach_threshold a");
				levelSql.append(" where a.code = '").append(beachData.getCode()).append("'");
				levelSql.append(" and data = ").append(beachData.getData());
				levelSql.append(" order by a.levelid desc limit 1");
			}
			levelid = this.queryForInt(levelSql.toString(), null);
			beachData.setLevelId(levelid);
		}
		
		return beachDatas;
	}
	
	/*
	 * 查询当前站点，当前时间段的数据导入
	 */
	public List<BeachDataModel> getBeachData4Station(BeachDataModel model) {
		List<BeachDataModel> result = new ArrayList<BeachDataModel>();
		StringBuffer sql = new StringBuffer("");
		sql.append("call getBeachData4Station(").append(model.getStationId()).append(",'").append(model.getCollectTime()).append("')");
		//sql.append(" call getBeachData4Station(10004,'2018-06-08 14')");
		result = this.pricForObjects(sql.toString(), BeachDataModel.class);
		return result;
	}
	
	/*
	 * 根据编码,获得海水浴场的配置表
	 */
	public List<BeachConfigModel> getBeachConfigListByCode(String code){
		List<BeachConfigModel> list = new ArrayList<>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" call getBeachConfigListByCode('").append(code).append("')");
		list = this.pricForObjects(sql.toString(), BeachConfigModel.class);
		return list;
	}
	
	/*
	 * 根据configGroupId得到下属的code编码
	 */
	public List<BeachCodeModel> getBeachCodeListByGroup(BeachGroupModel model){
		List<BeachCodeModel> list = new ArrayList<>();
		Object[] param = new Object[] {
				model.getId()
		};
		String sql = this.handleProcSql("getBeachCodeListByGroup", param);
		list = this.pricForObjects(sql, BeachCodeModel.class);
		return list;
	}
	
	/*
	 * 查询出所有参数组
	 */
	public List<BeachGroupModel> getBeachGroupList(){
		List<BeachGroupModel> result = new ArrayList<>();
		List<BeachGroupModel> list = new ArrayList<>();
		String sql = this.handleProcSql("getBeachGroupList", null);
		list = this.pricForObjects(sql, BeachGroupModel.class);
		for(BeachGroupModel group:list) {
			List<BeachCodeModel> codeList = this.getBeachCodeListByGroup(group);
			if(codeList!=null&&codeList.size()>0) {
				group.setCodes(codeList);
				result.add(group);
			}
		}
		return result;
	}
	
	/*
	 * 保存数据上报
	 */
	public String saveBeachImportData(String stationId,String collectTime,String param,int userId) {
		String result = "";
		Object[] params = new Object[] {
				stationId,collectTime,param,userId
		};
		String sql = this.handleProcSql("saveBeachImportData", params);
		Map<String, Object> ks = this.procQueryMap(sql);
		result = (String) ks.get("result");
		return result;
	}
	
	/*
	 * 计算健康指数
	 */
	public BeachPointModel getHealthy(List<BeachDataModel> beachDatas) {
		BeachPointModel point = new BeachPointModel();
		Double leftPoint = (double) 0;
		Double wpoint = (double) 0;
		//分别列出天气,粪大肠,肠球菌,水母,赤潮,漂浮物藻类,漂浮物油污的数值
		Double weatherdata = (double) 0;
		String weatherValue = "";
		//获得天气状况的值
		for(BeachDataModel beachData:beachDatas) {
			BeachPointModel p = new BeachPointModel(); 
			if(beachData.getCode().equals("fdcj")) {
				p = this.getPointByCodeData("fdcj", beachData.getData());
			}else if(beachData.getCode().equals("cqj")) {
				p = this.getPointByCodeData("cqj", beachData.getData());
			}else if(beachData.getCode().equals("sm")) {
				p = this.getPointByCodeData("sm", beachData.getData());
			}else if(beachData.getCode().equals("cc")) {
				p = this.getPointByCodeData("cc", beachData.getData());
			}else if(beachData.getCode().equals("pfwgu")) {
				p = this.getPointByCodeData("pfwgu", beachData.getData());
			}else if(beachData.getCode().equals("pfwoil")) {
				p = this.getPointByCodeData("pfwoil", beachData.getData());
			}else if(beachData.getCode().equals("weather")) {
				weatherdata = beachData.getData();
				weatherValue = beachData.getDataValue();
				//获得天气状况的评分
				if(weatherdata==1) {
					wpoint = (double) 40;
				}else if(weatherdata==2) {
					wpoint = (double) 60;
				}else if(weatherdata==3) {
					wpoint = (double) 80;
				}else if(weatherdata==4) {
					wpoint = (double) 100;
				}
			}
			
			if(p.getPoint()!=null&&p.getPoint()>leftPoint) {//如果当前数值比较大
				leftPoint = p.getPoint();
				point.setCode(beachData.getCode());
				point.setName(beachData.getName());
				point.setPointValue(beachData.getDataValue());
			}
		}
		
		
		
		if(wpoint>0) {
			point.setPoint(0.8*leftPoint + (0.2*weatherdata));
			point.setPointValue(weatherValue);
		}else {
			point.setPointValue(weatherValue);
		}
		return point;
	}
	
	/*
	 * 根据参数,以及数值获得评分
	 */
	public BeachPointModel getPointByCodeData(String code,Double data) {
		BeachPointModel point = new BeachPointModel();
		StringBuffer sql = new StringBuffer();
		sql.append(" select a.point");
		sql.append(" from aiot_beach_healthy a,aiot_beach_code b");
		sql.append(" where a.code = b.code");
		sql.append(" and a.code = '").append(code).append("'");
		sql.append(" and case b.type when 1 then    case mincal when 1 then ").append(data).append(" >= min else ").append(data).append(" > min end");
		sql.append(" and  case maxcal when 1 then ").append(data).append("<= max else ").append(data).append("< max end");
		sql.append("  when 2 then data =").append(data);
		sql.append("   end");
		point = this.queryObject(sql.toString(), BeachPointModel.class);
		return point;
	}
	
	/*
	 * 得到水质统计的线性数据
	 */
	public List<BeachResultModel> getBeachStatDataSearchLis(BeachStatisModel model,StationModel station){
		List<BeachResultModel> result = new ArrayList<>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.stationid,a.stattype,left(a.xtime,10) as xtime,a.ydata,a.remark,a.type,b.name as yname");
		sql.append(" from aiot_beach_result a,sys_domain_level b");
		sql.append(" where a.stationid = ").append(station.getId());
		sql.append(" and b.domainid = 11 and a.ydata = b.code");
		sql.append(" and a.stattype=").append(model.getStatType());
		sql.append(" and a.xtime between '").append(model.getBeginDate()).append("' and '").append(model.getEndDate()).append("'");
		sql.append(" and a.type = 0");
		result = this.queryObjectList(sql.toString(), BeachResultModel.class);
		return result;
	}
	
	/*
	 * 查询某个站点,当前统计口径,特定日期的水质等级
	 * 
	 */
	public StatData getBeachSingleStatData4Station(StationModel station,
			int collectType,String collectTime) {
		StatData sd = new StatData();
		BeachDegreeModel result = new BeachDegreeModel();
		List<BeachDataModel> beachDatas = this.getLastBeachData(station,collectTime);
		for(BeachDataModel beachdata: beachDatas) {
			if(beachdata.getLevelId()>result.getLevelId()) {
				result.setLevelId(beachdata.getLevelId());
				if(beachdata.getLevelId()>1) {
					result.setBadCodes(beachdata.getCode());
					result.setBadNames(beachdata.getName());
					if(beachdata.getTypeId()==1) {
						result.setBadReasons(beachdata.getName()+beachdata.getDataValue());
					}else {
						result.setBadReasons(beachdata.getDataValue());
					}
				}
			}else if(beachdata.getLevelId()==result.getLevelId()) {
				result.setBadCodes(result.getBadCodes()+","+beachdata.getCode());
				result.setBadNames(result.getBadNames()+","+beachdata.getName());
				if(beachdata.getTypeId()==1) {
					result.setBadReasons(result.getBadReasons()+","+beachdata.getName()+beachdata.getDataValue());
				}else {
					result.setBadReasons(result.getBadReasons()+","+beachdata.getDataValue());
				}
			}
			
		}
		return sd;
	}
	
	/*
	 * 为水质统计提供饼形图以及统计列表数据
	 */
	public List<StatData> getData4pie(BeachStatisModel model,StationModel station,int type,List<BeachResultModel> statDatas){
		List<StatData> list = new ArrayList<StatData>();
		List<StatData> result = new ArrayList<StatData>();
		//获得该统计口径下的总数
		int statCount = 0;
		try {
			statCount = statDatas.size();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//统计每个水质等级的天数,首要污染因子以及占比
		//初始化每个水质等级
		StringBuffer sql = new StringBuffer("");
		sql.append(" select code as standard_grade,name,color,").append(statCount).append(" as statcount,0 as y,'' as firstThing");
		sql.append(" from sys_domain_level");
		sql.append(" where domainid = 11");
		sql.append(" order by ordercode");
		
		list = this.queryObjectList(sql.toString(), StatData.class);
		//遍历线性数据的结果,并将结果统计到饼形图中
		for(BeachResultModel ad:statDatas){
			for(int i=0;i<list.size();i++){
				StatData li = list.get(i);
				if(ad.getYdata()==li.getStandard_grade()){
					//更新当前水质等级的总数
					li.setY(li.getY()+1);
				}
				
				list.set(i, li);
			}
		}
		//遍历结果,去掉没有数据的水质等级
		for(StatData li:list){
			if(li.getY()>0){
				result.add(li);
			}
		}
		
		return result;
	}
}
