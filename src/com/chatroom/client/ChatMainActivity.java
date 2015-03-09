package com.chatroom.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class ChatMainActivity extends Activity {
	/** Called when the activity is first created. */
	public List<UserInfo> userlist;
	public Spinner userSpinner;
	public static UserInfo userinfo;
	public static List<TabHost.TabSpec> tab_list;
	public static TabHost myTabHost;
	public static  ArrayAdapter<UserInfo> adapter;
	public static ChatMainActivity instance;//用于在不同的类之间传递Context
	public static FrameLayout tabcontent_layout;//用于在不同的类之间传递FrameLayout，用于动态添加textview
	public MsgInfo msginfo;
	public static Handler handler;
	public static Handler rbhd;
	public static Handler sbhd;
	public static Handler sthd;
	public static Handler cthd;
	//这两个线程需要强行中断
	public ServerThread srvthread;
	public ReceiveBrocast recvbrocast;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);// 页面布局
		instance = this;
		
		//获取用户输入的昵称
		Bundle extras = getIntent().getExtras();
		TextView history_title = (TextView) findViewById(R.id.tv_historytitle);
		history_title.setText(" 聊天记录   (你当前用户名是：" + extras.getString("WelcomeActivity") + ")");
		
		//初始化的一些工作
		initSpinner();
		initTabhost();
		initFramelayout();
		
		UserInfo userdefault = new UserInfo();
		userdefault.setUserName("请选择：");
		userdefault.setIpAddress("0.0.0.0");
		initUser(userdefault);
		
		handler = new Handler();
		
		//设置用户自己的相关信息
		userinfo = new UserInfo();
		userinfo.setUserName(extras.getString("WelcomeActivity"));
		userinfo.setIpAddress(getLocalIpAddress());
		
		bindButton();
		runThread();

	}

	// 处理返回键响应
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			intent.putExtra("ChatMainActivity", "如果要重新进入聊天室，请重新输入用户名：");
			setResult(RESULT_OK , intent);
			finish();
			stopThread();

			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	// 设置按钮的监听程序
	public void bindButton() {
		Button btn_send = (Button) findViewById(R.id.btn_send);

		btn_send.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//获取tab的数量
				int count = ChatMainActivity.getMyTabHost().getTabWidget().getTabCount();
				if (count < 1) {
					// TODO Auto-generated method stub
					Toast.makeText(ChatMainActivity.this, " Tab不能为空，请先选择联系人 ",30000).show();
				}
				else{
					EditText et_msg = (EditText) findViewById(R.id.et_message);
					if (et_msg.getText().toString().equals("")) {
						Toast.makeText(ChatMainActivity.this, " 消息内容不能为空 ",30000).show();
					} else {
						ClientThread.setMsgbuffer(et_msg.getText().toString());
						et_msg.setText("");
						//msgready = true，客户端线程执行对文本内容的处理
						ClientThread.setMsgready(true);
					}
				}

			}
		});
	}

	public void initSpinner() {
		userlist = new ArrayList<UserInfo>();
		adapter = new ArrayAdapter<UserInfo>(this, android.R.layout.simple_spinner_item, userlist);
		// 设置Item的样式
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		userSpinner = (Spinner) findViewById(R.id.spinner_onlineusers);
		userSpinner.setAdapter(adapter);
		userSpinner.setSelection(0, false);
        // 单击条目的响应事件
		userSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,View view, int position, long id) {
						if (!parent.getSelectedItem().toString().equals("请选择：")) {
							addTabs((UserInfo)parent.getSelectedItem(), myTabHost);
						}
					}

					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
					}

				});
	}

	public void initTabhost(){
		myTabHost = (TabHost) findViewById (R.id.tabhost);
		myTabHost.setup();
		tab_list= new ArrayList<TabHost.TabSpec>();
	}
	
	public void initFramelayout(){
		tabcontent_layout = (FrameLayout)findViewById(android.R.id.tabcontent);
	}
	
	public void initUser(UserInfo userinfo_temp) {
		/** 先判断值是否存在，不存在再添加 **/
		adapter.add(userinfo_temp);
		//调用下面的方法后，spinner的内容将会直接改变
		//userinfo_temp.setUserName("hello::");
	}

	public void addTabs(UserInfo struct_temp, TabHost myTabHost) {
		Log.i("ChatMainActivity", "ChatMainActivity里面添加tab页");

		String ip = struct_temp.getIpAddress();
		String username = struct_temp.getUserName();
		for (int i = 0; i < tab_list.size(); i++) {
			// 判断新接收到的ip是否在tab_list当中
			if (tab_list.get(i).getTag().equals(ip)) {
				myTabHost.setCurrentTabByTag(ip);
				return;
			}
		}

		//没有在当前的tab_list里面找到对应的ip，新建一个tab，并添加到tabinfo_list当中
		//新建一个textview，用于显示每个人的聊天内容
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
        
        TabSpec temp = myTabHost.newTabSpec(ip).setIndicator(username).setContent(ll.getId());
        myTabHost.addTab(temp);
        
		//将Tab信息保存在 List<TabInfo>里面
        tab_list.add(temp);
        
    	//同时插入到数据库当中
		MyDataBaseAdapter m_MyDataBaseAdapter = new MyDataBaseAdapter(ChatMainActivity.getInstance());
		m_MyDataBaseAdapter.open();
		m_MyDataBaseAdapter.insertData(username, "user");
		m_MyDataBaseAdapter.addTable(username);
		m_MyDataBaseAdapter.close();
		
		//spinner的点击事件，这里有必要跳转到新的tab页，spinner跳回到“请选择”。
		myTabHost.setCurrentTabByTag(struct_temp.getIpAddress());
		userSpinner.setSelection(0);
	}

	public void runThread(){
		SendBrocast sendbrocast = new SendBrocast();
		sendbrocast.start();
		
		recvbrocast = new ReceiveBrocast();
		recvbrocast.start();
		
		//ServerThread srvthread = new ServerThread();
		srvthread = new ServerThread();
		srvthread.start();
		
		ClientThread clientthread = new ClientThread();
		clientthread.start();
	}
	
	public void stopThread(){
		sbhd = SendBrocast.getHandler();
		sbhd.post(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				SendBrocast.setRunflag(false);
			}
		});
		Log.i("ChatMainActivity","已发送结束SendBrocast线程的请求");
		
		recvbrocast.getDsreceive().close();
		Log.i("ChatMainActivity","已发送结束ReceiveBrocast线程的请求");
		
		//发送serversocket.close触发线程interrupt事件
		Log.i("ChatMainActivity","已发送结束ServerThread线程的请求");
		try {
			srvthread.getOthers_to_me().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cthd = ClientThread.getHandler();
		cthd.post(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				ClientThread.setRunflag(false);
			}
		});
		Log.i("ClientThread","已发送结束ClientThread线程的请求");
	}
	
	//获取本机的ip地址
	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {

		}
		return null;
	}

	//将ip地址中的点去掉，然后转换成int型
	public int getIpAsId(String str){
		return Integer.parseInt(str.replace(".", ""));
	}
	
	/**
	 * 相关变量的get和set方法，用于在不同的类之间传递变量
	 * @return
	 */
	public static ArrayAdapter<UserInfo> getAdapter() {
		return adapter;
	}

	public void setAdapter(ArrayAdapter<UserInfo> adapter) {
		this.adapter = adapter;
	}
	
	public static TabHost getMyTabHost() {
		return myTabHost;
	}

	public void setMyTabHost(TabHost myTabHost) {
		this.myTabHost = myTabHost;
	}

	public static List<TabHost.TabSpec> gettab_list() {
		return tab_list;
	}

	public static void settab_list(List<TabHost.TabSpec> tab_list) {
		ChatMainActivity.tab_list = tab_list;
	}

	public static ChatMainActivity getInstance() {
		return instance;
	}

	public static void setInstance(ChatMainActivity instance) {
		ChatMainActivity.instance = instance;
	}

	public static FrameLayout getTabcontent_layout() {
		return tabcontent_layout;
	}

	public static void setTabcontent_layout(FrameLayout tabcontent_layout) {
		ChatMainActivity.tabcontent_layout = tabcontent_layout;
	}

	public static Handler getHandler() {
		return handler;
	}

	public static void setHandler(Handler handler) {
		ChatMainActivity.handler = handler;
	}

	public static UserInfo getUserinfo() {
		return userinfo;
	}

	public static void setUserinfo(UserInfo userinfo) {
		ChatMainActivity.userinfo = userinfo;
	}

}

