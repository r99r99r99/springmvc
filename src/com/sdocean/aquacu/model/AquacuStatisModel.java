package com.sdocean.aquacu.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.sdocean.dataQuery.model.StatData;

public class AquacuStatisModel {

	private int stationId;
	private String stationName;
	private int statType;   //1 按日统计 2 按月统计 3按周统计
	private String beginDate;
	private String endDate;
	private List<StatData> datas;
	public List xtimes = new LinkedList();	 //横坐标数据
	public List<Object> ydatas = new ArrayList<Object>();  //纵坐标数据
	private String indicatorIds;
	
	
	
	public List<StatData> getDatas() {
		return datas;
	}
	public void setDatas(List<StatData> datas) {
		this.datas = datas;
	}
	public List getXtimes() {
		return xtimes;
	}
	public void setXtimes(List xtimes) {
		this.xtimes = xtimes;
	}
	public List<Object> getYdatas() {
		return ydatas;
	}
	public void setYdatas(List<Object> ydatas) {
		this.ydatas = ydatas;
	}
	public String getIndicatorIds() {
		return indicatorIds;
	}
	public void setIndicatorIds(String indicatorIds) {
		this.indicatorIds = indicatorIds;
	}
	public int getStationId() {
		return stationId;
	}
	public void setStationId(int stationId) {
		this.stationId = stationId;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public int getStatType() {
		return statType;
	}
	public void setStatType(int statType) {
		this.statType = statType;
	}
	public String getBeginDate() {
		return beginDate;
	}
	public void setBeginDate(String beginDate) {
		this.beginDate = beginDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
}
