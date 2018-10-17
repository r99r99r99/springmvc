package com.sdocean.beach.model;

public class BeachDegreeModel {

	private int levelId;      //游泳指数
	private String levelName; //游泳指数名称
	private String color;      //游泳指数配色
	private String badCodes;    //影响游泳指数的因素编码集合
	private String badNames;    //
	private String badReasons;  //原因提示
	private BeachPointModel healthyPoint;  //健康指数
	
	public BeachPointModel getHealthyPoint() {
		return healthyPoint;
	}
	public void setHealthyPoint(BeachPointModel healthyPoint) {
		this.healthyPoint = healthyPoint;
	}
	public int getLevelId() {
		return levelId;
	}
	public void setLevelId(int levelId) {
		this.levelId = levelId;
	}
	public String getLevelName() {
		return levelName;
	}
	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getBadCodes() {
		return badCodes;
	}
	public void setBadCodes(String badCodes) {
		this.badCodes = badCodes;
	}
	public String getBadNames() {
		return badNames;
	}
	public void setBadNames(String badNames) {
		this.badNames = badNames;
	}
	public String getBadReasons() {
		return badReasons;
	}
	public void setBadReasons(String badReasons) {
		this.badReasons = badReasons;
	}
}
