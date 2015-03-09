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
		Log.i("ServerThread", "�ڷ�����߳� ServerThread ��");
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
				Log.i("ServerThread","serversocket.colse�����쳣");
				e.printStackTrace();
				break;
			}

			if (receive != null) {
				String msguser = receive.getMsgUser();
				String msgip = receive.getMsgIp();
				String msgcontent = receive.getMsgContent();
				if (msgcontent != null) {
					//����Ϣ��ʾ����Ӧ��tab��ǩҳ����
					//���жϱ�ǩ�Ƿ���ڣ��������������ӱ�ǩ
					//Ȼ�����Ӧ����Ϣ������ӵ���Ӧ�ı�ǩҳ����
					UserInfo struct_temp = new UserInfo();
					struct_temp.setUserName(msguser);
					struct_temp.setIpAddress(msgip);
					addTabs(struct_temp);
					updateView(msgip, msgcontent);
					
					//ͬʱ���뵽���ݿ⵱��
					MyDataBaseAdapter m_MyDataBaseAdapter = new MyDataBaseAdapter(ChatMainActivity.getInstance());
					m_MyDataBaseAdapter.open();
					m_MyDataBaseAdapter.insertData(msgcontent, msguser);
					m_MyDataBaseAdapter.close();
				}
			}
			
		}
		Log.i("ServerThread", "ServerThread�߳��Ѿ�����");
	}
	
	
	// ��һ�����û�����ʱ�����һ��tabҳ
	//struct_temp�Ǵ�spinner�����ȡ���ġ�
	public void addTabs(UserInfo struct_temp) {
		final String ip = struct_temp.getIpAddress();
		final String username = struct_temp.getUserName();
		List<TabHost.TabSpec> tab_list = ChatMainActivity.gettab_list();
		for (int i = 0; i < tab_list.size(); i++) {
			//�ж��½��յ���ip�Ƿ���tab_list����
			if (tab_list.get(i).getTag().equals(ip)) {
				return;
			}
		}
		
		//û���ڵ�ǰ��tab_list�����ҵ���Ӧ��ip���½�һ��tab������ӵ�tabinfo_list����
		//�½�һ��textview��������ʾÿ���˵���������
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
		        Log.i("ServerThread", "���tabҳ");
			}
		});
        
		//��Tab��Ϣ������ List<TabInfo>����
		TabSpec tmp = ((TabHost) ChatMainActivity.getInstance().findViewById(R.id.tabhost)).newTabSpec(ip);
		tab_list.add(tmp);
		
		//ͬʱ�����ݿ��д������û��������¼��
		MyDataBaseAdapter m_MyDataBaseAdapter = new MyDataBaseAdapter(ChatMainActivity.getInstance());
		m_MyDataBaseAdapter.open();
		m_MyDataBaseAdapter.insertData(username, "user");
		m_MyDataBaseAdapter.addTable(username);
		m_MyDataBaseAdapter.close();
	}

	public void updateView(String ipadress, final String msgcontent){
		Log.i("ServerThread", "��ʼ����updateview����");
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
