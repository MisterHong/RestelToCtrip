package com.supyuan.system.order;

public class OrderCheck {

	private String HotelCode;
	private String Start;
	private String end;
	private String RatePlanCode;
	private String RoomTypeCode;
	private String Quantity;	//房间数量
	private String Count;	//顾客数量
	private String child;	//儿童数量
	private String AgeQualifyingCode;	//成年人10 8是儿童
	
	public String getHotelCode() {
		return HotelCode;
	}
	public void setHotelCode(String hotelCode) {
		HotelCode = hotelCode;
	}
	public String getStart() {
		return Start;
	}
	public void setStart(String start) {
		Start = start;
	}
	public String getEnd() {
		return end;
	}
	public void setEnd(String end) {
		this.end = end;
	}
	public String getRatePlanCode() {
		return RatePlanCode;
	}
	public void setRatePlanCode(String ratePlanCode) {
		RatePlanCode = ratePlanCode;
	}
	public String getRoomTypeCode() {
		return RoomTypeCode;
	}
	public void setRoomTypeCode(String roomTypeCode) {
		RoomTypeCode = roomTypeCode;
	}
	public String getQuantity() {
		return Quantity;
	}
	public void setQuantity(String quantity) {
		Quantity = quantity;
	}
	public String getCount() {
		return Count;
	}
	public void setCount(String count) {
		Count = count;
	}
	public String getAgeQualifyingCode() {
		return AgeQualifyingCode;
	}
	public void setAgeQualifyingCode(String ageQualifyingCode) {
		AgeQualifyingCode = ageQualifyingCode;
	}
	public String getChild() {
		return child;
	}
	public void setChild(String child) {
		this.child = child;
	}
}
