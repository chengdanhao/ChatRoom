package com.chatroom.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class ServerThread extends Thread{
	public volatile ServerSocket others_to_me;
	public Socket others = null;
	public InputStream in;
	public ObjectInputStream objectin;
	public static final int TCP_PORT = 30001;
	public static MsgInfo receive;
	public static Handler hd;
	
	@Override
	public void run(){
		Log.i("ServerThread", "在服务端线程 ServerThread 中");
		/*
		ServerSocket server = new ServerSocket(8189);
		Socket s = server.accept();
		InputStream in = s.getInputStream();
		ObjectInputStream objectin = new ObjectInputStream(in);
		packhead receive = (packhead)objectin.readObject();
		byte[] from = receive.from;
		byte[] to = receive.to;
		int ntype = receive.ntype;
		int nlength = receive.nlength;
		String fromString = new String(from);
		String toString = new String(to);
		s.close();
		 */
		java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
		java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");

		try {
			others_to_me = new ServerSocket(TCP_PORT);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while (true) {
			try {
				others = others_to_me.accept();
				in = others.getInputStream();
				objectin = new ObjectInputStream(in);
				receive = (MsgInfo) objectin.readObject();
			} catch (OptionalDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.i("ServerThread","serversocket.colse出发异常");
				e.printStackTrace();
				break;
			}

			if (receive != null) {
				String msguser = receive.getMsgUser();
				String msgip = receive.getMsgIp();
				String msgcontent = receive.getMsgContent();
				if (msgcontent != null) {
					//将消息显示在相应的tab标签页里面
					//先判断标签是否存在，如果不存在先添加标签
					//然后把响应的消息内容添加到对应的标签页当中
					UserInfo struct_temp = new UserInfo();
					struct_temp.setUserName(msguser);
					struct_temp.setIpAddress(msgip);
					addTabs(struct_temp);
					updateView(msgip, msgcontent);
					
					//同时插入到数据库当中
					MyDataBaseAdapter m_MyDataBaseAdapter = new MyDataBaseAdapter(ChatMainActivity.getInstance());
					m_MyDataBaseAdapter.open();
					m_MyDataBaseAdapter.insertData(msgcontent, msguser);
					m_MyDataBaseAdapter.close();
				}
			}
			
		}
		Log.i("ServerThread", "ServerThread线程已经结束");
	}
	
	
	// 和一个新用户聊天时，添加一个tab页
	//struct_temp是从spinner里面获取到的。
	public void addTabs(UserInfo struct_temp) {
		final String ip = struct_temp.getIpAddress();
		final String username = struct_temp.getUserName();
		List<TabHost.TabSpec> tab_list = ChatMainActivity.gettab_list();
		for (int i = 0; i < tab_list.size(); i++) {
			//判断新接收到的ip是否在tab_list当中
			if (tab_list.get(i).getTag().equals(ip)) {
				return;
			}
		}
		
		//没有在当前的tab_list里面找到对应的ip，新建一个tab，并添加到tabinfo_list当中
		//新建一个textview，用于显示每个人的聊天内容
		hd = ChatMainActivity.getHandler();
		hd.post(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				FrameLayout fl= ChatMainActivity.getTabcontent_layout();
				LinearLayout ll = new LinearLayout(ChatMainActivity.getInstance());
				ll.setId(getIpAsId(ip));
				ScrollView sv = new ScrollView(ChatMainActivity.getInstance());
				sv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
				TextView tv=new TextView(ChatMainActivity.getInstance());
				tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
				
				sv.addView(tv);
		        ll.addView(sv);
		        fl.addView(ll);
		        
		        TabHost myTabHost = ChatMainActivity.getMyTabHost();
		        TabSpec temp = myTabHost.newTabSpec(ip).setIndicator(username).setContent(ll.getId());
		        myTabHost.addTab(temp);
		        Log.i("ServerThread", "添加tab页");
			}
		});
        
		//将Tab信息保存在 List<TabInfo>里面
		TabSpec tmp = ((TabHost) ChatMainActivity.getInstance().findViewById(R.id.tabhost)).newTabSpec(ip);
		tab_list.add(tmp);
		
		//同时在数据库中创建该用户的聊天记录表
		MyDataBaseAdapter m_MyDataBaseAdapter = new MyDataBaseAdapter(ChatMainActivity.getInstance());
		m_MyDataBaseAdapter.open();
		m_MyDataBaseAdapter.insertData(username, "user");
		m_MyDataBaseAdapter.addTable(username);
		m_MyDataBaseAdapter.close();
	}

	public void updateView(String ipadress, final String msgcontent){
		Log.i("ServerThread", "开始进入updateview函数");
		final int id = getIpAsId(ipadress);
		hd = ChatMainActivity.getHandler();
		hd.post(new Runnable() {
			public void run() {
					LinearLayout ll = (LinearLayout) ChatMainActivity.getInstance().findViewById(id);
					ScrollView sv = (ScrollView) ll.getChildAt(0);
					((TextView) sv.getChildAt(0)).append(msgcontent);
				}
		});
	}
	
	public int getIpAsId(String str){
		return Integer.parseInt(str.replace(".", ""));
	}

	public ServerSocket getOthers_to_me() {
		return others_to_me;
	}


	public void setOthers_to_me(ServerSocket others_to_me) {
		this.others_to_me = others_to_me;
	}
}
