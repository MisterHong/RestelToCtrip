package com.supyuan.system.hotel;

import java.util.List;

public class RomeStay {

	private String rtCode;
	private List<Reg> regList;
	private String GuestCount;

	public String getRtCode() {
		return rtCode;
	}
	public void setRtCode(String rtCode) {
		this.rtCode = rtCode;
	}
	public List<Reg> getRegList() {
		return regList;
	}
	public void setRegList(List<Reg> regList) {
		this.regList = regList;
	}
	public String getGuestCount() {
		return GuestCount;
	}
	public void setGuestCount(String guestCount) {
		GuestCount = guestCount;
	}
	
}
