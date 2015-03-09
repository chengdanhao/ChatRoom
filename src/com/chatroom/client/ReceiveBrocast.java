package com.chatroom.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class ReceiveBrocast extends Thread {
	public volatile DatagramSocket dsreceive;
	public DatagramPacket dpreceive;
	public Spinner userSpinner;
	public boolean isuserexist;
	public static Handler handler = new Handler();
	public static Handler mahd;
	public static final int MAX_DATA_PACKET_LENGTH = 1024;
	public static final int BROADCAST_PORT = 20001;
	public static List<UserInfo> userinfo_list;
	public static ArrayAdapter<UserInfo>  adapter;

	// ѭ���������˵Ĺ㲥��Ȼ����ӵ��Լ��������û��б�
	@Override
	public void run() {
		Log.i("ReceiveBrocast", "���߳� ReceiveBrocast ��");
	
		/*
		public static void main(String[] args) throws IOException {  
	        DatagramSocket ds = new DatagramSocket(8081);  
	        byte[] buf = new byte[1024];  
	        DatagramPacket dp = new DatagramPacket(buf, buf.length);  
	        while (true) {  
	            System.out.println("for receive....");  
	            ds.receive(dp);  
	            Student s = (Student)b2o(dp.getData());  
	            System.out.println(s.name);  
	        }  
	  
	    }  
	    */
		
		isuserexist = false;
		userinfo_list = new ArrayList<UserInfo>();
		// �½�upd��socket
		try {
			dsreceive = new DatagramSocket(BROADCAST_PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while (true) {
			// �˴����շ����������ݱ���
			try {
				byte[] buf = new byte[MAX_DATA_PACKET_LENGTH];  
				dpreceive = new DatagramPacket(	buf, buf.length);
				dsreceive.receive(dpreceive);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.i("ReceiveBrocast", "datagramsocket.close�رո��߳�");
				e.printStackTrace();
				break;
			}

			executeReceiveData(dpreceive);
			Log.i("ReceiveBrocast", "�ѽ��յ��㲥");

			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.i("ReceiveBrocast", "ReceiveBrocast�߳��Ѿ�����");

	}

	// �����͹�������Ϣ
	public void executeReceiveData(DatagramPacket dprecv) {
		/*ServerThread
		hd = ChatMainActivity.getHandler();
		hd.post(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				((TextView) ChatMainActivity.getInstance().findViewById(id)).append(msgcontent);
			}
		});
		*/
		final UserInfo userinfo_temp = (UserInfo)b2o(dprecv.getData());
		adapter = ChatMainActivity.getAdapter();
		
		//�ж��û����Ƿ����б���
		for (int i = 0; i < userinfo_list.size(); i++) {
			isuserexist = userinfo_list.get(i).getIpAddress().equals(userinfo_temp.getIpAddress());
			if(isuserexist){
				break;
			}
		}
		
		//����û������б����棬����ӵ�������
		//ͬʱ��spinner��Ҳ���һ��
		if(!isuserexist){
			userinfo_list.add(userinfo_temp);
			//ʹ��handler-post��ʽ
			mahd = ChatMainActivity.getHandler();
			mahd.post(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					adapter.add(userinfo_temp);
				}
			});

		}
		
		//���͵�ǰ�㲥���û���������0
		//������Ƿ��͵�ǰ�㲥���û���������1
		for (int i = 0; i < userinfo_list.size(); i++) {
			if(userinfo_list.get(i).getIpAddress().equals(userinfo_temp.getIpAddress())){
				//���ip��ȣ����ֲ���ȣ��Ѿɵ��������µ����ִ���.
				if(!userinfo_list.get(i).getUserName().equals(userinfo_temp.getUserName())){
					userinfo_list.get(i).setUserName(userinfo_temp.getUserName());
				}
				userinfo_list.get(i).resetCount();
			}
			else{
				userinfo_list.get(i).incCount();
			}
		}

		//ĳ�û�����ʱ��û�з��͹㲥�����û��б��о�ɾ��
		for (int i = 0; i < userinfo_list.size(); i++) {
			if (userinfo_list.get(i).getCount() > 3) {
				final UserInfo temp = userinfo_list.get(i);
				userinfo_list.remove(i);
				mahd = ChatMainActivity.getHandler();
				mahd.post(new Runnable() {
					public void run() {
						// TODO Auto-generated method stub
						adapter.remove(temp);
					}
				});
			}
		}
		
	}
	
	//��byteת���ɶ���  
    public static Object b2o(byte[] buffer) {  
        Object obj = null;  

        ByteArrayInputStream buffers = new ByteArrayInputStream(buffer);  
        ObjectInputStream in;
		try {
			in = new ObjectInputStream(buffers);
			
	        try {
				obj = in.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	        
	        in.close(); 
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  

        return obj;  
    }  

	public static List<UserInfo> getUserinfo_list() {
		return userinfo_list;
	}

	public static void setUserinfo_list(List<UserInfo> userinfo_list) {
		ReceiveBrocast.userinfo_list = userinfo_list;
	}

	public static Handler getHandler() {
		return handler;
	}

	public static void setHandler(Handler handler) {
		ReceiveBrocast.handler = handler;
	}

	public DatagramSocket getDsreceive() {
		return dsreceive;
	}

	public void setDsreceive(DatagramSocket dsreceive) {
		this.dsreceive = dsreceive;
	}

	
	/*
	public UserInfo byte_to_struct(byte[] byte_tmp) {
		UserInfo struct_tmp = new UserInfo();
		int len = byte_tmp.length;
		int i;
		String username = null;
		String ipaddress = null;

		for (i = 0; i < len; i++) {
			if (byte_tmp[i] == '$') {
				break;
			} else {
				username += byte_tmp[i];
			}
		}

		for (i = i + 1; i < len; i++) {
			if (byte_tmp[i] == '$') {
				break;
			} else {
				ipaddress += byte_tmp[i];
			}
		}

		struct_tmp.setUserName(username);
		struct_tmp.setIpAddress(ipaddress);

		return struct_tmp;
	}
	 */
	

}
