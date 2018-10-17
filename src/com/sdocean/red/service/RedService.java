package com.sdocean.red.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sdocean.aquacu.model.AquacuStatisModel;
import com.sdocean.common.model.Hcharts;
import com.sdocean.common.model.HchartsPie;
import com.sdocean.dataQuery.model.StatData;
import com.sdocean.red.dao.RedDao;
import com.sdocean.red.model.RedStatisModel;
import com.sdocean.station.model.StationModel;

@Service
@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRED)
public class RedService {
	
	@Autowired
	private RedDao redhDao;
	
	
	/*
	 * 获得该站点的海水浴场等级
	 */
	public HchartsPie getRedDetailByStation(StationModel station) {
		return redhDao.getRedDetailByStation(station);
	}
	
	/*
	 * 获得增养殖区的等级统计
	 */
	public RedStatisModel getRedStatDataSearch(RedStatisModel model,StationModel station){
		//得到线性数据
		List<StatData> lineList = redhDao.getRedStatDataSearchList(model, station, 1);
		if(lineList==null||lineList.size()<1) {
			return null;
		}
		//获得饼形图结果以及水质统计列表数据
		List<StatData> pielist = redhDao.getData4pie(model, station, 1,lineList);
		for(StatData statData:lineList){
			model.xtimes.add(statData.getXtime());
			Hcharts hc = new Hcharts();
			hc.setY(Double.parseDouble(statData.getYdata()));
			hc.setName(statData.getName());
			model.ydatas.add(hc);
		}
		model.setDatas(pielist);
		return model;
	}
	
}
