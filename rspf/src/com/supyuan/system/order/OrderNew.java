package com.supyuan.system.order;

public class OrderNew {

	private String UniqueID;
	private OrderRoomStay OrderRoomStay;
	private OrderGuest OrderGuest;
	private String Start;
	private String End;
	private String CardCode;
	private String CardNumber;
	private String CardType;
	private String CtripExpireDate;
	private String CardHolderName;
	private String TotalPay;
	private String CurrencyCode;
	private String GuestCount;
	private String ResID501;
	private String ResID507;
	
	public String getUniqueID() {
		return UniqueID;
	}
	public void setUniqueID(String uniqueID) {
		UniqueID = uniqueID;
	}
	
	public OrderRoomStay getOrderRoomStay() {
		return OrderRoomStay;
	}
	public void setOrderRoomStays(OrderRoomStay orderRoomStay) {
		OrderRoomStay = orderRoomStay;
	}
	public OrderGuest getOrderGuest() {
		return OrderGuest;
	}
	public void setOrderGuest(OrderGuest orderGuest) {
		OrderGuest = orderGuest;
	}
	public String getStart() {
		return Start;
	}
	public void setStart(String start) {
		Start = start;
	}
	public String getEnd() {
		return End;
	}
	public void setEnd(String end) {
		End = end;
	}
	public String getTotalPay() {
		return TotalPay;
	}
	public void setTotalPay(String totalPay) {
		TotalPay = totalPay;
	}
	public String getCurrencyCode() {
		return CurrencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		CurrencyCode = currencyCode;
	}
	public String getGuestCount() {
		return GuestCount;
	}
	public void setGuestCount(String guestCount) {
		GuestCount = guestCount;
	}
	public String getResID501() {
		return ResID501;
	}
	public void setResID501(String resID501) {
		ResID501 = resID501;
	}
	public String getResID507() {
		return ResID507;
	}
	public void setResID507(String resID507) {
		ResID507 = resID507;
	}
	public String getCardCode() {
		return CardCode;
	}
	public void setCardCode(String cardCode) {
		CardCode = cardCode;
	}
	public String getCardNumber() {
		return CardNumber;
	}
	public void setCardNumber(String cardNumber) {
		CardNumber = cardNumber;
	}
	public String getCardType() {
		return CardType;
	}
	public void setCardType(String cardType) {
		CardType = cardType;
	}
	public String getCtripExpireDate() {
		return CtripExpireDate;
	}
	public void setCtripExpireDate(String ctripExpireDate) {
		CtripExpireDate = ctripExpireDate;
	}
	public String getCardHolderName() {
		return CardHolderName;
	}
	public void setCardHolderName(String cardHolderName) {
		CardHolderName = cardHolderName;
	}
}
