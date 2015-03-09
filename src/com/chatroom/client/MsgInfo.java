package com.chatroom.client;

import java.io.Serializable;

public class MsgInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String msgUser;
	private String msgIp;
	private String msgContent;
	
	public String getMsgUser() {
		return msgUser;
	}
	public void setMsgUser(String msgUser) {
		this.msgUser = msgUser;
	}
	
	public String getMsgIp() {
		return msgIp;
	}
	public void setMsgIp(String msgIp) {
		this.msgIp = msgIp;
	}
	
	public String getMsgContent() {
		return msgContent;
	}
	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

}
