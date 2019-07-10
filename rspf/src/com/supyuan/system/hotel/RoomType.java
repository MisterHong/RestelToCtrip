package com.supyuan.system.hotel;

import java.util.List;

public class RoomType {

	private String cod;
	private String desc;
	private List<SubType> subTypeList;
	
	public RoomType(){}
	
	public RoomType(String cod, String desc, List<SubType> subTypeList) {
		super();
		this.cod = cod;
		this.desc = desc;
		this.subTypeList = subTypeList;
	}
	
	public String getCod() {
		return cod;
	}
	public void setCod(String cod) {
		this.cod = cod;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public List<SubType> getSubTypeList() {
		return subTypeList;
	}
	public void setSubTypeList(List<SubType> subTypeList) {
		this.subTypeList = subTypeList;
	}
}
