package com.sdocean.common.model;

import java.util.List;

public class HchartsPie {

	private String title ;
    private List<Hcharts> datas;
    
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<Hcharts> getDatas() {
		return datas;
	}
	public void setDatas(List<Hcharts> datas) {
		this.datas = datas;
	}
}
