package com.supyuan.system.hotel;

import java.util.List;

public class RoomInfo {

	private String Hotel; //酒店code
	private String nom;	//酒店名称-英文
	private String pros; 	//酒店省
	private List<RoomType> roomtype;
	
	public RoomInfo(){}

	public RoomInfo(String hotel, String nom, String pros, List<RoomType> roomtype) {
		super();
		Hotel = hotel;
		this.nom = nom;
		this.pros = pros;
		this.roomtype = roomtype;
	}

	public String getHotel() {
		return Hotel;
	}

	public void setHotel(String hotel) {
		Hotel = hotel;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public List<RoomType> getRoomtype() {
		return roomtype;
	}

	public void setRoomtype(List<RoomType> roomtype) {
		this.roomtype = roomtype;
	}

	public String getPros() {
		return pros;
	}


	public void setPros(String pros) {
		this.pros = pros;
	}
}
