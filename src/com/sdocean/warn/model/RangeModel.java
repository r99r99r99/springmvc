package com.sdocean.warn.model;

public class RangeModel {

	private int id;
	private int stationId;
	private int deviceId;
	private String indicatorCode;
	private Double mindata;
	private Double maxdata;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public String getIndicatorCode() {
		return indicatorCode;
	}
	public void setIndicatorCode(String indicatorCode) {
		this.indicatorCode = indicatorCode;
	}
	public Double getMindata() {
		return mindata;
	}
	public void setMindata(Double mindata) {
		this.mindata = mindata;
	}
	public Double getMaxdata() {
		return maxdata;
	}
	public void setMaxdata(Double maxdata) {
		this.maxdata = maxdata;
	}
}
