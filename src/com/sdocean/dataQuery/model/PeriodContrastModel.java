package com.sdocean.dataQuery.model;

public class PeriodContrastModel {

	private int stationId;
	private String stationName;
	private int deviceId;
	private String deviceName;
	private String indicatorCode;
	private String indicatorName;
	private String standBeforeTime;
	private String standAfterTime;
	private String contrastBeforeTime;
	private String contrastAfterTime;
	
	private String indicatorIds ;  //查询条件中选中的参数
	
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
	public int getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getIndicatorCode() {
		return indicatorCode;
	}
	public void setIndicatorCode(String indicatorCode) {
		this.indicatorCode = indicatorCode;
	}
	public String getIndicatorName() {
		return indicatorName;
	}
	public void setIndicatorName(String indicatorName) {
		this.indicatorName = indicatorName;
	}
	public String getStandBeforeTime() {
		return standBeforeTime;
	}
	public void setStandBeforeTime(String standBeforeTime) {
		this.standBeforeTime = standBeforeTime;
	}
	public String getStandAfterTime() {
		return standAfterTime;
	}
	public void setStandAfterTime(String standAfterTime) {
		this.standAfterTime = standAfterTime;
	}
	public String getContrastBeforeTime() {
		return contrastBeforeTime;
	}
	public void setContrastBeforeTime(String contrastBeforeTime) {
		this.contrastBeforeTime = contrastBeforeTime;
	}
	public String getContrastAfterTime() {
		return contrastAfterTime;
	}
	public void setContrastAfterTime(String contrastAfterTime) {
		this.contrastAfterTime = contrastAfterTime;
	}
}
