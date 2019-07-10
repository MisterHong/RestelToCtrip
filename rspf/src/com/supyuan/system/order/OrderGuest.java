package com.supyuan.system.order;

import java.util.List;

public class OrderGuest {

	private String ArrivalTime;
	private List<Customer> customers;
	
	public String getArrivalTime() {
		return ArrivalTime;
	}
	public void setArrivalTime(String arrivalTime) {
		ArrivalTime = arrivalTime;
	}
	public List<Customer> getCustomers() {
		return customers;
	}
	public void setCustomers(List<Customer> customers) {
		this.customers = customers;
	}
	
}
