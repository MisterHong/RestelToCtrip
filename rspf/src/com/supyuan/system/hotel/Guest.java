package com.supyuan.system.hotel;

public class Guest {

	private String ageCode;
	private String count;
	
	public Guest() {}
	
	public Guest(String code,String ct) {
		ageCode = code;
		count = ct;
	}
	
	public String getAgeCode() {
		return ageCode;
	}
	public void setAgeCode(String ageCode) {
		this.ageCode = ageCode;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
}
