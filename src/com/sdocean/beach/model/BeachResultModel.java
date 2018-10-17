package com.sdocean.beach.model;

public class BeachResultModel {

	private int id;
	private int stationId;
	private String stationName;
	private int stattpe;
	private String xtime;
	private int ydata;
	private String yname;
	private String remark;
	private int type;  //0代表游泳指数
	
	public String getYname() {
		return yname;
	}
	public void setYname(String yname) {
		this.yname = yname;
	}
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
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public int getStattpe() {
		return stattpe;
	}
	public void setStattpe(int stattpe) {
		this.stattpe = stattpe;
	}
	public String getXtime() {
		return xtime;
	}
	public void setXtime(String xtime) {
		this.xtime = xtime;
	}
	public int getYdata() {
		return ydata;
	}
	public void setYdata(int ydata) {
		this.ydata = ydata;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
