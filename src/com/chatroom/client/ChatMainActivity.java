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
	public static ChatMainActivity instance;//�����ڲ�ͬ����֮�䴫��Context
	public static FrameLayout tabcontent_layout;//�����ڲ�ͬ����֮�䴫��FrameLayout�����ڶ�̬���textview
	public MsgInfo msginfo;
	public static Handler handler;
	public static Handler rbhd;
	public static Handler sbhd;
	public static Handler sthd;
	public static Handler cthd;
	//�������߳���Ҫǿ���ж�
	public ServerThread srvthread;
	public ReceiveBrocast recvbrocast;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);// ҳ�沼��
		instance = this;
		
		//��ȡ�û�������ǳ�
		Bundle extras = getIntent().getExtras();
		TextView history_title = (TextView) findViewById(R.id.tv_historytitle);
		history_title.setText(" �����¼   (�㵱ǰ�û����ǣ�" + extras.getString("WelcomeActivity") + ")");
		
		//��ʼ����һЩ����
		initSpinner();
		initTabhost();
		initFramelayout();
		
		UserInfo userdefault = new UserInfo();
		userdefault.setUserName("��ѡ��");
		userdefault.setIpAddress("0.0.0.0");
		initUser(userdefault);
		
		handler = new Handler();
		
		//�����û��Լ��������Ϣ
		userinfo = new UserInfo();
		userinfo.setUserName(extras.getString("WelcomeActivity"));
		userinfo.setIpAddress(getLocalIpAddress());
		
		bindButton();
		runThread();

	}

	// �����ؼ���Ӧ
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			intent.putExtra("ChatMainActivity", "���Ҫ���½��������ң������������û�����");
			setResult(RESULT_OK , intent);
			finish();
			stopThread();

			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	// ���ð�ť�ļ�������
	public void bindButton() {
		Button btn_send = (Button) findViewById(R.id.btn_send);

		btn_send.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//��ȡtab������
				int count = ChatMainActivity.getMyTabHost().getTabWidget().getTabCount();
				if (count < 1) {
					// TODO Auto-generated method stub
					Toast.makeText(ChatMainActivity.this, " Tab����Ϊ�գ�����ѡ����ϵ�� ",30000).show();
				}
				else{
					EditText et_msg = (EditText) findViewById(R.id.et_message);
					if (et_msg.getText().toString().equals("")) {
						Toast.makeText(ChatMainActivity.this, " ��Ϣ���ݲ���Ϊ�� ",30000).show();
					} else {
						ClientThread.setMsgbuffer(et_msg.getText().toString());
						et_msg.setText("");
						//msgready = true���ͻ����߳�ִ�ж��ı����ݵĴ���
						ClientThread.setMsgready(true);
					}
				}

			}
		});
	}

	public void initSpinner() {
		userlist = new ArrayList<UserInfo>();
		adapter = new ArrayAdapter<UserInfo>(this, android.R.layout.simple_spinner_item, userlist);
		// ����Item����ʽ
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		userSpinner = (Spinner) findViewById(R.id.spinner_onlineusers);
		userSpinner.setAdapter(adapter);
		userSpinner.setSelection(0, false);
        // ������Ŀ����Ӧ�¼�
		userSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,View view, int position, long id) {
						if (!parent.getSelectedItem().toString().equals("��ѡ��")) {
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
		/** ���ж�ֵ�Ƿ���ڣ������������ **/
		adapter.add(userinfo_temp);
		//��������ķ�����spinner�����ݽ���ֱ�Ӹı�
		//userinfo_temp.setUserName("hello::");
	}

	public void addTabs(UserInfo struct_temp, TabHost myTabHost) {
		Log.i("ChatMainActivity", "ChatMainActivity�������tabҳ");

		String ip = struct_temp.getIpAddress();
		String username = struct_temp.getUserName();
		for (int i = 0; i < tab_list.size(); i++) {
			// �ж��½��յ���ip�Ƿ���tab_list����
			if (tab_list.get(i).getTag().equals(ip)) {
				myTabHost.setCurrentTabByTag(ip);
				return;
			}
		}

		//û���ڵ�ǰ��tab_list�����ҵ���Ӧ��ip���½�һ��tab������ӵ�tabinfo_list����
		//�½�һ��textview��������ʾÿ���˵���������
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
        
		//��Tab��Ϣ������ List<TabInfo>����
        tab_list.add(temp);
        
    	//ͬʱ���뵽���ݿ⵱��
		MyDataBaseAdapter m_MyDataBaseAdapter = new MyDataBaseAdapter(ChatMainActivity.getInstance());
		m_MyDataBaseAdapter.open();
		m_MyDataBaseAdapter.insertData(username, "user");
		m_MyDataBaseAdapter.addTable(username);
		m_MyDataBaseAdapter.close();
		
		//spinner�ĵ���¼��������б�Ҫ��ת���µ�tabҳ��spinner���ص�����ѡ�񡱡�
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
		Log.i("ChatMainActivity","�ѷ��ͽ���SendBrocast�̵߳�����");
		
		recvbrocast.getDsreceive().close();
		Log.i("ChatMainActivity","�ѷ��ͽ���ReceiveBrocast�̵߳�����");
		
		//����serversocket.close�����߳�interrupt�¼�
		Log.i("ChatMainActivity","�ѷ��ͽ���ServerThread�̵߳�����");
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
		Log.i("ClientThread","�ѷ��ͽ���ClientThread�̵߳�����");
	}
	
	//��ȡ������ip��ַ
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

	//��ip��ַ�еĵ�ȥ����Ȼ��ת����int��
	public int getIpAsId(String str){
		return Integer.parseInt(str.replace(".", ""));
	}
	
	/**
	 * ��ر�����get��set�����������ڲ�ͬ����֮�䴫�ݱ���
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

