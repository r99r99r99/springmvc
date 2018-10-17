package com.sdocean.aquacu.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sdocean.aquacu.dao.AquacuDao;
import com.sdocean.aquacu.model.AquacuMetaModel;
import com.sdocean.aquacu.model.AquacuStatisModel;
import com.sdocean.common.model.Hcharts;
import com.sdocean.common.model.HchartsPie;
import com.sdocean.dataQuery.model.StatData;
import com.sdocean.dataQuery.model.StatisModel;
import com.sdocean.dictionary.dao.WaterQualityStandardDao;
import com.sdocean.dictionary.model.WaterQualityStandardModel;
import com.sdocean.indicator.model.IndicatorModel;
import com.sdocean.station.model.StationModel;

@Service
@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRED)
public class AquacuService {
	
	@Autowired
	private AquacuDao aquacuDao;
	
	@Autowired
	private WaterQualityStandardDao watehQualityStandardDao;
	
	
	/*
	 * 获得该站点的海水浴场等级
	 */
	public HchartsPie getAquacuDetailByStation(StationModel station) {
		return aquacuDao.getAquacuDetailByStation(station);
	}
	
	
	/*
	 * 获得增养殖区的等级统计
	 */
	public AquacuStatisModel getAquacuStatDataSearch(AquacuStatisModel model,StationModel station){
		
		int NV = 0; //未达到质量标准要求的环境要素数量
		int NVCOUNT = 0;  //拟评价环境要素的总数
		
		int NTCOUNT = 0; //所有拟评价环境要素的总测定次数
		int NT = 0; //未达到质量标准要求的测定次数
		
		//获得监测海域所有的可统计参数
		List<IndicatorModel> indicatorList = watehQualityStandardDao.getIndicator4StandByStation(station);
		//遍历参数,查询该参数在查询时间内的水质状况
		for(IndicatorModel indicator:indicatorList){
			//获得该参数的水质标准值
			WaterQualityStandardModel stand = watehQualityStandardDao.getStandardByStationIndicator(station, indicator);
			
			NVCOUNT++;  //评价要素的总数加一
			Boolean ifstand = true;
			
			//得到该站点参数的所有的数据的水质情况
			List<AquacuMetaModel> metaList = aquacuDao.getAquacuMetaList4StationIndicator(station, indicator, model.getBeginDate(), model.getEndDate());
			for(AquacuMetaModel meta:metaList){
				NTCOUNT++; //所有拟评价环境要素的总测定次数加一
				if(meta.getLevelId()>2){
					ifstand = false;
					NT++; //未达到质量标准要求的测定次数加一
				}
			}
			
			if(!ifstand){
				NV++;
			}
		}
		
		
		
		//得到线性数据
		List<StatData> lineList = aquacuDao.getAquacuStatDataSearchList(model, station, 1);
		//获得饼形图结果以及水质统计列表数据
		List<StatData> pielist = aquacuDao.getData4pie(model, station, 1,lineList);
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
