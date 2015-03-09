package com.chatroom.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.Handler;
import android.util.Log;

public class SendBrocast extends Thread {

	UserInfo userinfo;
	public DatagramSocket dssend;
	public DatagramPacket dpsend;
	public InetSocketAddress isa;
	public static Handler handler = new Handler();
	public static boolean runflag;
	public static final int MAX_DATA_PACKET_LENGTH = 1024;
	public static final int BROADCAST_PORT = 20001;
	
	// 刚上线的时候向整个局域网广播自己。
	@Override
	public void run() {
		Log.i("SendBrocast", "在线程 SendBrocast 中");
		
		/*
		public static void main(String[] args) throws IOException {  
	        InetSocketAddress isa = new InetSocketAddress(InetAddress .getByName("192.168.1.102"), 8081);  
	        DatagramSocket ds = new DatagramSocket();  
	        Student student = new Student();  
	        student.name = "denghong";  
	        DatagramPacket dp = new DatagramPacket(o2b(student), o2b(student).length, isa);  
	        ds.send(dp);  
	    }  
	    */
		runflag = true;
		// 新建upd的socket
		try {
			dssend = new DatagramSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//获取用户在welcome界面输入的姓名以及当前客户端的ip地址
		//填充结构体
		UserInfo userinfo = ChatMainActivity.getUserinfo();

		while (true) {
			if(!runflag)
			{
				dssend.close();
				Log.i("SendBrocast", "SendBrocast线程已经结束");
				break;
			}
			
			// 如果线程还在运行，那么就发送
			try {
				isa = new InetSocketAddress(InetAddress.getByName("255.255.255.255"),BROADCAST_PORT);

				//初始化数据包
				try {
					dpsend = new DatagramPacket(o2b(userinfo),o2b(userinfo).length, isa);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//发送数据包
				try {
					dssend.send(dpsend);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Log.i("SendBrocast", "已发送广播");
			
			try {
				sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	//将对象转换成byte类型，以便udp发送
    public static byte[] o2b(UserInfo struct_temp) {  
        ByteArrayOutputStream buffers = new ByteArrayOutputStream();  
        ObjectOutputStream out;
        
		try {
			out = new ObjectOutputStream(buffers);
			out.writeObject(struct_temp);  
		    out.close();  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
       
        return buffers.toByteArray();  
    }

	public static Handler getHandler() {
		return handler;
	}

	public static void setHandler(Handler handler) {
		SendBrocast.handler = handler;
	}

	public static boolean isRunflag() {
		return runflag;
	}

	public static void setRunflag(boolean runflag) {
		SendBrocast.runflag = runflag;
	} 

    
/*
	public byte[] struct_to_byte(UserInfo struct_tmp){
		byte[] temp_buffer = new byte[MAX_DATA_PACKET_LENGTH];
		
		System.arraycopy(
				struct_tmp.getUserName().getBytes(),
				0, 
				temp_buffer, 
				0, 
				struct_tmp.getUserName().length());
		
		//插入标示符，把用户名和ip地址区分开
		System.arraycopy("$", 0, temp_buffer, struct_tmp.getUserName().length(), 1);
		
		System.arraycopy(
				struct_tmp.getIpAddress().getBytes(), 
				0, 
				temp_buffer, 
				struct_tmp.getUserName().length()+1, 
				struct_tmp.getIpAddress().length());
		
		System.arraycopy("$", 0, temp_buffer, struct_tmp.getIpAddress().length() + struct_tmp.getUserName().length() + 1, 1);
		
		return temp_buffer;
	}
*/
}
