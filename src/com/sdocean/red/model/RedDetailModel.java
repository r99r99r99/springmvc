package com.sdocean.red.model;

public class RedDetailModel {

	private int levelId;
	private String levelName;
	private String indicatorCode;
	private String indicatorName;
	private int minCal;
	private Double min;
	private int maxCal;
	private Double max;
	
	public int getMinCal() {
		return minCal;
	}
	public void setMinCal(int minCal) {
		this.minCal = minCal;
	}
	public Double getMin() {
		return min;
	}
	public void setMin(Double min) {
		this.min = min;
	}
	public int getMaxCal() {
		return maxCal;
	}
	public void setMaxCal(int maxCal) {
		this.maxCal = maxCal;
	}
	public Double getMax() {
		return max;
	}
	public void setMax(Double max) {
		this.max = max;
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
}
