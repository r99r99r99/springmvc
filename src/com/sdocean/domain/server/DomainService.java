package com.sdocean.domain.server;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sdocean.common.model.Result;
import com.sdocean.dictionary.dao.PublicDao;
import com.sdocean.dictionary.model.PublicModel;
import com.sdocean.domain.dao.DomainDao;
import com.sdocean.domain.model.DomainIndicator;
import com.sdocean.domain.model.DomainLevelModel;
import com.sdocean.domain.model.DomainModel;
import com.sdocean.domain.model.DomainResult;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.indicator.model.IndicatorModel;
import com.sdocean.page.model.UiColumn;
import com.sdocean.station.model.StationModel;

@Service
@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRED)
public class DomainService {
	
	@Autowired
	private DomainDao domainDao;
	
	
	public List<UiColumn> getCols4DomainList(){
		List<UiColumn> cols = new ArrayList<UiColumn>();
		UiColumn col1 = new UiColumn("id", "id", false, "*");
		UiColumn col2 = new UiColumn("编码", "code", true, "*");
		UiColumn col3 = new UiColumn("名称", "name", true, "*");
		UiColumn col4 = new UiColumn("状态", "isactiveName", true, "*");
		cols.add(col1);
		cols.add(col2);
		cols.add(col3);
		cols.add(col4);
		return cols;
	}
	
	/*
	 * 展示功能区的列表
	 */
	public List<DomainModel> getDomainList(DomainModel model){
		return domainDao.getDomainList(model);
	}
	
	/*
	 * 新增功能区
	 */
	public Result saveNewDomain(DomainModel model){
		return domainDao.saveNewDomain(model);
	}
	
	/*
	 * 修改功能区
	 */
	public Result saveChangeDomain(DomainModel model){
		return domainDao.saveChangeDomain(model);
	}
	/*
	 * 停用功能区
	 */
	public Result deleDomain(DomainModel model){
		return domainDao.deleDomain(model);
	}
	
	/*
	 * 保存功能区--站点--参数权限
	 */
	public Result saveDomainStationIndicator(DomainModel model){
		return domainDao.saveDomainStationIndicator(model);
	}
	
	/*
	 * 获得该站点的功能区的信息
	 */
	public List<DomainResult> getDomainResultsByStation(StationModel station){
		//初始化返回结果
		List<DomainResult> results = new ArrayList<DomainResult>();
		//根据站点获得该站点分属的功能区列表
		List<DomainModel> domains = domainDao.getDomainModelsByStation(station);
		//遍历功能区列表
		for(DomainModel domain:domains){
			DomainResult dr = new DomainResult();
			//获得该功能区下的关注的参数列表
			List<IndicatorModel> indicators = domainDao.getIndicatorsByDomain(domain);
			//根据站点以及参数列表获得关注的参数的结果集
			List<DomainIndicator> dis = domainDao.getDomainIndicator4Now(station, indicators);
			if(dis!=null&&dis.size()>0){
				dr.setDomain(domain);
				dr.setDomainIndicators(dis);
				results.add(dr);
			}
		}
		return results;
	}
	/*
	 * 获得某功能区下的等级列表
	 */
	public List<DomainLevelModel> getDomainLevelListByDomain(DomainLevelModel model){
		return domainDao.getDomainLevelListByDomain(model);
	}
}
