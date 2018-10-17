package com.sdocean.beach.model;

import java.util.List;

public class BeachGroupModel {

	private int id;
	private String name;
	private String orderCode;
	private List<BeachCodeModel> codes;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public List<BeachCodeModel> getCodes() {
		return codes;
	}
	public void setCodes(List<BeachCodeModel> codes) {
		this.codes = codes;
	}
}
