package com.supyuan.system.hotel;

import java.util.List;

public class Reg {

	private String rpCode;
	private String total;
	private boolean isBF;
	private boolean isBD;
	private boolean isLUN;
	private String cancelPenalty; //取消政策
	private List<Lin> linList;
	private String numberOfMeal;
	private String Deadline; //固定取消时间
	
	public String getRpCode() {
		return rpCode;
	}
	public void setRpCode(String rpCode) {
		this.rpCode = rpCode;
	}
	
	public List<Lin> getLinList() {
		return linList;
	}
	public void setLinList(List<Lin> linList) {
		this.linList = linList;
	}
	public boolean isBF() {
		return isBF;
	}
	public void setBF(boolean isBF) {
		this.isBF = isBF;
	}
	public boolean isBD() {
		return isBD;
	}
	public void setBD(boolean isBD) {
		this.isBD = isBD;
	}
	public boolean isLUN() {
		return isLUN;
	}
	public void setLUN(boolean isLUN) {
		this.isLUN = isLUN;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public String getCancelPenalty() {
		return cancelPenalty;
	}
	public void setCancelPenalty(String cancelPenalty) {
		this.cancelPenalty = cancelPenalty;
	}
	public String getNumberOfMeal() {
		return numberOfMeal;
	}
	public void setNumberOfMeal(String numberOfMeal) {
		this.numberOfMeal = numberOfMeal;
	}
	public String getDeadline() {
		return Deadline;
	}
	public void setDeadline(String deadline) {
		Deadline = deadline;
	}
	
}
