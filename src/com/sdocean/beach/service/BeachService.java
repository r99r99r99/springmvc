package com.sdocean.beach.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sdocean.beach.dao.BeachDao;
import com.sdocean.beach.model.BeachAllConfig;
import com.sdocean.beach.model.BeachConfigModel;
import com.sdocean.beach.model.BeachDataModel;
import com.sdocean.beach.model.BeachDegreeModel;
import com.sdocean.beach.model.BeachGroupModel;
import com.sdocean.beach.model.BeachPointModel;
import com.sdocean.beach.model.BeachResultModel;
import com.sdocean.beach.model.BeachStatisModel;
import com.sdocean.beach.model.DataModel;
import com.sdocean.common.model.Hcharts;
import com.sdocean.dataQuery.model.StatData;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.station.model.StationModel;

@Service
@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRED)
public class BeachService {
	
	@Autowired
	private BeachDao beachDao;
	
	
	/*
	 * 获得该站点的海水浴场等级
	 */
	public BeachDegreeModel getBeachDetailByStation(StationModel station) {
		BeachDegreeModel result = new BeachDegreeModel();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
		String nowDate = df.format(new Date());
		List<BeachDataModel> beachDatas = beachDao.getLastBeachData(station,nowDate);
		
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
		
		//计算健康指数
		BeachPointModel point = beachDao.getHealthy(beachDatas);
		result.setHealthyPoint(point);
		return result;
	}
	
	/*
	 * 查询当前站点，当前时间段的数据导入
	 */
	public Map<String, Object> getBeachData4Station(BeachDataModel model) {
		Map<String, Object> result = new HashMap<>();
		List<BeachDataModel> datas = beachDao.getBeachData4Station(model);
		for(BeachDataModel data:datas) {
			DataModel dm = new DataModel();
			dm.setData(data.getData());
			dm.setTitle(data.getTitle());
			result.put(data.getCode(), dm);
		}
		return result;
	}
	/*
	 * 获得海水浴场所有的配置表
	 */
	public BeachAllConfig getBeachAllConfigList(){
		BeachAllConfig result = new BeachAllConfig();
		List<BeachConfigModel> list = new ArrayList<BeachConfigModel>();
		//水母
		String code = "sm";
		list = beachDao.getBeachConfigListByCode(code);
		result.setSmList(list);
		//赤潮
		code = "cc";
		list = beachDao.getBeachConfigListByCode(code);
		result.setCcList(list);
		//漂浮物藻类
		code = "pfwgu";
		list = beachDao.getBeachConfigListByCode(code);
		result.setPfwguList(list);
		//漂浮物油污
		code = "pfwoil";
		list = beachDao.getBeachConfigListByCode(code);
		result.setPfwoilList(list);
		//臭味
		code = "scw";
		list = beachDao.getBeachConfigListByCode(code);
		result.setScwList(list);
		//天气
		code = "weather";
		list = beachDao.getBeachConfigListByCode(code);
		result.setWeatherList(list);
		//油污
		code = "oilpu";
		list = beachDao.getBeachConfigListByCode(code);
		result.setOilpuList(list);
		//藻类
		code = "zao";
		list = beachDao.getBeachConfigListByCode(code);
		result.setZaoList(list);
		//垃圾
		code = "laji";
		list = beachDao.getBeachConfigListByCode(code);
		result.setLajiList(list);
		return result;
	}
	/*
	 * 查询出所有参数组
	 */
	public List<BeachGroupModel> getBeachGroupList(){
		return beachDao.getBeachGroupList();
	}
	/*
	 * 保存数据上报
	 */
	public String saveBeachImportData(String stationId,String collectTime,String param,int userId) {
		return beachDao.saveBeachImportData(stationId, collectTime, param, userId);
	}
	
	/*
	 * 获得水质统计
	 */
	public BeachStatisModel getBeachStat(BeachStatisModel model,StationModel station){
		List<BeachResultModel> lineList = beachDao.getBeachStatDataSearchLis(model, station);
		//获得饼形图结果以及水质统计列表数据
		List<StatData> pielist = beachDao.getData4pie(model, station, 1,lineList);
		for(BeachResultModel statData:lineList){
			model.xtimes.add(statData.getXtime());
			Hcharts hc = new Hcharts();
			hc.setY(Double.parseDouble(statData.getYdata()+""));
			hc.setName(statData.getYname());
			hc.setRemark(statData.getRemark());
			model.ydatas.add(hc);
		}
		model.setDatas(pielist);
		return model;
	}
}
