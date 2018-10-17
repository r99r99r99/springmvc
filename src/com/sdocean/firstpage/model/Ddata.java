package com.sdocean.firstpage.model;

import java.util.Date;

public class Ddata {
	private Double data;
	private String  lastTime;
	private String markCode;  
	private int isactive;   //数据状态,1代表正常数据  0代表审核未通过数据
	private String remark;  //添加备注
	
	public int getIsactive() {
		return isactive;
	}

	public void setIsactive(int isactive) {
		this.isactive = isactive;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getMarkCode() {
		return markCode;
	}

	public void setMarkCode(String markCode) {
		this.markCode = markCode;
	}

	public Double getData() {
		return data;
	}

	public void setData(Double data) {
		this.data = data;
	}

	public String getLastTime() {
		return lastTime;
	}

	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}
	
}
