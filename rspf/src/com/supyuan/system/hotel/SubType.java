package com.supyuan.system.hotel;

public class SubType {

	private String cod; //类型代码
	private String prr;	//结算价格
	private String pvp;	//代理商与供应商结算最低售价
	private String div;	//结算货币代码
	private String lin;	//lin数据
	
	public SubType(){}
	
	public SubType(String cod, String prr, String pvp, String div, String lin) {
		super();
		this.cod = cod;
		this.prr = prr;
		this.pvp = pvp;
		this.div = div;
		this.lin = lin;
	}

	public String getLin() {
		return lin;
	}

	public void setLin(String lin) {
		this.lin = lin;
	}

	public String getCod() {
		return cod;
	}
	public void setCod(String cod) {
		this.cod = cod;
	}
	public String getPrr() {
		return prr;
	}
	public void setPrr(String prr) {
		this.prr = prr;
	}
	public String getPvp() {
		return pvp;
	}
	public void setPvp(String pvp) {
		this.pvp = pvp;
	}
	public String getDiv() {
		return div;
	}
	public void setDiv(String div) {
		this.div = div;
	}
}
