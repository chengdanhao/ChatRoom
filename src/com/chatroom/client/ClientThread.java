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
		Log.i("ClientThread", "在客户端线程 ClientThread 中");
		
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
					//刚开始没有添加任何tab页的时候退出来
					//加一个条件判断，以免发生异常
					if (this_to_others != null) {
						to_others_stream.close();
						out.close();
						this_to_others.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i("ClientThread", "ClientThread线程已经结束");
				break;
			}
			
			//用户是否输入了消息内容，若是，执行对消息内容的处理
			if (msgready) {
				// 这里对方的ip可以根据tab标签页所在的结构体得出来。
				boolean isuseronline = false;
				String tabtag = ChatMainActivity.getMyTabHost().getCurrentTabTag();
				int pos = ChatMainActivity.getMyTabHost().getCurrentTab();
				String tabindicator = (String) ((TextView)ChatMainActivity.getMyTabHost().getTabWidget().getChildAt(pos).findViewById(android.R.id.title)).getText();
				try {
					this_to_others = new Socket(tabtag, TCP_PORT);//ip地址就在标签页的tag中
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
					//如果对方下线了，则关闭socket并且关闭标签页
					List<UserInfo> userinfo_list = ReceiveBrocast.getUserinfo_list();
					for (int i = 0; i < userinfo_list.size(); i++) {
						//判断当前的tab页的用户是否仍然在线
						if (userinfo_list.get(i).getIpAddress().equals(tabtag)) {
							MsgInfo struct_temp = new MsgInfo();
							struct_temp.setMsgUser(ChatMainActivity.getUserinfo().getUserName());
							struct_temp.setMsgIp(ChatMainActivity.getUserinfo().getIpAddress());
							struct_temp.setMsgContent(formateMsg(ClientThread.getMsgbuffer()));
							
							//先显示出来
							updateView(tabtag, formateMsg(ClientThread.getMsgbuffer()));
							
							//再写入数据库
							MyDataBaseAdapter m_MyDataBaseAdapter = new MyDataBaseAdapter(ChatMainActivity.getInstance());
							m_MyDataBaseAdapter.open();
							m_MyDataBaseAdapter.insertData(formateMsg(ClientThread.getMsgbuffer()), tabindicator);
							m_MyDataBaseAdapter.close();

							// 最后发送出去
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

					//用户已经不再链表里面了，表示已经下线
					if (!isuseronline) {
						//删除当前tab标签页，删除socklist当中的对应项
						//并关闭相关socket和stream
						delTabs();
						try {
							to_others_stream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;//跳出循环，关掉socket
					}

				}
				
				setMsgready(false);
			}
		}
		
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
	
	public String formateMsg(String msg){
		String name = ChatMainActivity.getUserinfo().getUserName();
		//获取系统的时间
		Calendar calendar = Calendar.getInstance();
		String time = calendar.get(Calendar.HOUR_OF_DAY) + ":"
					+ calendar.get(Calendar.MINUTE) + ":"
					+ calendar.get(Calendar.SECOND);
		
		return name + " " + time + "\n" + msg + "\n";
	}
	
	public void delTabs() {
		Log.i("ClientThread", "删除tab页");
		Toast.makeText(ChatMainActivity.getInstance(), "对方已经下线", 3000).show();
		
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

	//将ip地址中的点去掉，然后转换成int型
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