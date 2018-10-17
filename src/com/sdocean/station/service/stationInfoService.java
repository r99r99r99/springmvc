package com.sdocean.station.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sdocean.common.model.Result;
import com.sdocean.common.model.SelectTree;
import com.sdocean.frame.model.ConfigInfo;
import com.sdocean.page.model.UiColumn;
import com.sdocean.station.dao.StationCommDao;
import com.sdocean.station.dao.stationInfoDao;
import com.sdocean.station.model.StationDeviceComm;
import com.sdocean.station.model.StationInfo;
import com.sdocean.station.model.StationModel;
import com.sdocean.station.model.StationPictureModel;

@Service
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
public class stationInfoService {

	@Resource
	stationInfoDao stationInfoDao;
	
	/*
	 * 获得站点  的图片列表
	 */
	public List<StationPictureModel> getStationPicListByStationType(ConfigInfo info,StationPictureModel model){
		return stationInfoDao.getStationPicListByStationType(info,model);
	}
}
