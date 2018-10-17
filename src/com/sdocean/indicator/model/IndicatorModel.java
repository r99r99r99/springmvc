package com.sdocean.indicator.model;

import java.util.List;

import com.sdocean.dictionary.model.WaterQualityStandardModel;

public class IndicatorModel {
	
	private int id;
	private String code;
	private String title;
	private int groupId;
	private String groupName;
	private int unitId;
	private String unitName;
	private String description;
	private int isactive;
	private String isactiveName;
	private String orderCode;
	
	//标记参数的量程
	private Double minData;   //量程最小值
	private Double maxData;   //量程最大值
	
	//参数的水质等级信息列表
	private List<WaterQualityStandardModel> qualityList;
	
	public Double getMinData() {
		return minData;
	}
	public void setMinData(Double minData) {
		this.minData = minData;
	}
	public List<WaterQualityStandardModel> getQualityList() {
		return qualityList;
	}
	public void setQualityList(List<WaterQualityStandardModel> qualityList) {
		this.qualityList = qualityList;
	}
	public Double getMaxData() {
		return maxData;
	}
	public void setMaxData(Double maxData) {
		this.maxData = maxData;
	}
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public int getUnitId() {
		return unitId;
	}
	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}
	public String getUnitName() {
		return unitName;
	}
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getIsactive() {
		return isactive;
	}
	public void setIsactive(int isactive) {
		this.isactive = isactive;
	}
	public String getIsactiveName() {
		return isactiveName;
	}
	public void setIsactiveName(String isactiveName) {
		this.isactiveName = isactiveName;
	}
}
