package com.chatroom.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class ClientThread extends Thread{
	public Socket this_to_others;
	public ObjectOutputStream to_others_stream;
	public OutputStream out;
	public Handler hd;
	public static String msgbuffer = "";
	public static Handler handler = new Handler();
	public static boolean runflag;
	public static boolean msgready;
	public static final int TCP_PORT = 30001;
	
	
	@Override
	public void run(){
		Log.i("ClientThread", "�ڿͻ����߳� ClientThread ��");
		
		/*
		Socket s = new Socket("localhost", 8189);
        OutputStream out = s.getOutputStream();
        ObjectOutputStream objectout = new ObjectOutputStream(out);
        packhead headheart = new packhead();
		objectout.writeObject(headheart);
		 */
		
		java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
		java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");

		
		runflag = true;
		msgready = false;
		while (true) {
			if(!runflag)
			{
				try {
					//�տ�ʼû������κ�tabҳ��ʱ���˳���
					//��һ�������жϣ����ⷢ���쳣
					if (this_to_others != null) {
						to_others_stream.close();
						out.close();
						this_to_others.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i("ClientThread", "ClientThread�߳��Ѿ�����");
				break;
			}
			
			//�û��Ƿ���������Ϣ���ݣ����ǣ�ִ�ж���Ϣ���ݵĴ���
			if (msgready) {
				// ����Է���ip���Ը���tab��ǩҳ���ڵĽṹ��ó�����
				boolean isuseronline = false;
				String tabtag = ChatMainActivity.getMyTabHost().getCurrentTabTag();
				int pos = ChatMainActivity.getMyTabHost().getCurrentTab();
				String tabindicator = (String) ((TextView)ChatMainActivity.getMyTabHost().getTabWidget().getChildAt(pos).findViewById(android.R.id.title)).getText();
				try {
					this_to_others = new Socket(tabtag, TCP_PORT);//ip��ַ���ڱ�ǩҳ��tag��
					out = this_to_others.getOutputStream();
					to_others_stream = new ObjectOutputStream(out);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!ClientThread.getMsgbuffer().equals("")) {
					//if
					//����Է������ˣ���ر�socket���ҹرձ�ǩҳ
					List<UserInfo> userinfo_list = ReceiveBrocast.getUserinfo_list();
					for (int i = 0; i < userinfo_list.size(); i++) {
						//�жϵ�ǰ��tabҳ���û��Ƿ���Ȼ����
						if (userinfo_list.get(i).getIpAddress().equals(tabtag)) {
							MsgInfo struct_temp = new MsgInfo();
							struct_temp.setMsgUser(ChatMainActivity.getUserinfo().getUserName());
							struct_temp.setMsgIp(ChatMainActivity.getUserinfo().getIpAddress());
							struct_temp.setMsgContent(formateMsg(ClientThread.getMsgbuffer()));
							
							//����ʾ����
							updateView(tabtag, formateMsg(ClientThread.getMsgbuffer()));
							
							//��д�����ݿ�
							MyDataBaseAdapter m_MyDataBaseAdapter = new MyDataBaseAdapter(ChatMainActivity.getInstance());
							m_MyDataBaseAdapter.open();
							m_MyDataBaseAdapter.insertData(formateMsg(ClientThread.getMsgbuffer()), tabindicator);
							m_MyDataBaseAdapter.close();

							// ����ͳ�ȥ
							try {
								to_others_stream.writeObject(struct_temp);
								to_others_stream.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							isuseronline = true;
							break;
						}
					}

					//�û��Ѿ��������������ˣ���ʾ�Ѿ�����
					if (!isuseronline) {
						//ɾ����ǰtab��ǩҳ��ɾ��socklist���еĶ�Ӧ��
						//���ر����socket��stream
						delTabs();
						try {
							to_others_stream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;//����ѭ�����ص�socket
					}

				}
				
				setMsgready(false);
			}
		}
		
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
	
	public String formateMsg(String msg){
		String name = ChatMainActivity.getUserinfo().getUserName();
		//��ȡϵͳ��ʱ��
		Calendar calendar = Calendar.getInstance();
		String time = calendar.get(Calendar.HOUR_OF_DAY) + ":"
					+ calendar.get(Calendar.MINUTE) + ":"
					+ calendar.get(Calendar.SECOND);
		
		return name + " " + time + "\n" + msg + "\n";
	}
	
	public void delTabs() {
		Log.i("ClientThread", "ɾ��tabҳ");
		Toast.makeText(ChatMainActivity.getInstance(), "�Է��Ѿ�����", 3000).show();
		
		hd = ChatMainActivity.getHandler();
		hd.post(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				List<TabHost.TabSpec> tab_list = ChatMainActivity.gettab_list();
				TabHost myTabHost = ChatMainActivity.getMyTabHost();
				int pos = ChatMainActivity.getMyTabHost().getCurrentTab();
				tab_list.remove(pos);
				int nTabIndex = 0;
				myTabHost.setCurrentTab(0);
				myTabHost.clearAllTabs(); // clear all tabs from the tabhost
				for (TabHost.TabSpec spec : tab_list) {
					myTabHost.addTab(spec);
					myTabHost.setCurrentTab(nTabIndex++);
				}
			}
		});

	}

	//��ip��ַ�еĵ�ȥ����Ȼ��ת����int��
	public int getIpAsId(String str){
		return Integer.parseInt(str.replace(".", ""));
	}
	
	public static String getMsgbuffer() {
		return msgbuffer;
	}

	public static void setMsgbuffer(String msgbuffer) {
		ClientThread.msgbuffer = msgbuffer;
	}

	public static Handler getHandler() {
		return handler;
	}

	public static void setHandler(Handler handler) {
		ClientThread.handler = handler;
	}

	public static boolean isRunflag() {
		return runflag;
	}

	public static void setRunflag(boolean runflag) {
		ClientThread.runflag = runflag;
	}

	public static boolean isMsgready() {
		return msgready;
	}

	public static void setMsgready(boolean msgready) {
		ClientThread.msgready = msgready;
	}

}