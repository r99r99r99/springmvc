package com.sdocean.dataQuery.model;

import java.util.List;
import java.util.Map;

import com.sdocean.common.model.SelectTree;
import com.sdocean.device.model.DeviceModel;


public class DataQueryModel {
	
	private int stationId;
	private int deviceId;
	private String deviceIds;
	private String beginDate;
	private String endDate;
	private List<SelectTree> indicatorTree;
	private String indicatorIds;
	private List<DeviceModel> devices;
	private List<SelectTree> stationTree;
	private String queryType;
	
	public String getQueryType() {
		return queryType;
	}
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	public List<SelectTree> getStationTree() {
		return stationTree;
	}
	public void setStationTree(List<SelectTree> stationTree) {
		this.stationTree = stationTree;
	}
	public List<DeviceModel> getDevices() {
		return devices;
	}
	public void setDevices(List<DeviceModel> devices) {
		this.devices = devices;
	}
	public String getIndicatorIds() {
		return indicatorIds;
	}
	public void setIndicatorIds(String indicatorIds) {
		this.indicatorIds = indicatorIds;
	}
	public List<SelectTree> getIndicatorTree() {
		return indicatorTree;
	}
	public void setIndicatorTree(List<SelectTree> indicatorTree) {
		this.indicatorTree = indicatorTree;
	}
	public String getDeviceIds() {
		return deviceIds;
	}
	public void setDeviceIds(String deviceIds) {
		this.deviceIds = deviceIds;
	}
	public int getStationId() {
		return stationId;
	}
	public void setStationId(int stationId) {
		this.stationId = stationId;
	}
	public int getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
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
