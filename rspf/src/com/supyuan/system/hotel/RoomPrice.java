package com.supyuan.system.hotel;

public class RoomPrice {

	private String rtcode; //基础房型code
	private String rpcode;	//售卖房型code
	private String prr; 	//价格
	
	public RoomPrice(){}
	
	public RoomPrice(String _rtcode, String _rpcode, String _prr)
	{
		rtcode = _rtcode;
		rpcode = _rpcode;
		prr = _prr;
	}
	
	public String getRtcode() {
		return rtcode;
	}
	public void setRtcode(String rtcode) {
		this.rtcode = rtcode;
	}
	public String getRpcode() {
		return rpcode;
	}
	public void setRpcode(String rpcode) {
		this.rpcode = rpcode;
	}
	public String getPrr() {
		return prr;
	}
	public void setPrr(String prr) {
		this.prr = prr;
	}
}
