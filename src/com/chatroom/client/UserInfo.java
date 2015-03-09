package com.chatroom.client;

import java.io.Serializable;

public class UserInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private String userName;
	private String ipAddress;
	private int count = 0;

	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public int getCount() {
		return count;
	}

	public void resetCount() {
		count = 0;
	}
	
	public void incCount(){
		++count;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getUserName();
	}
		
}
