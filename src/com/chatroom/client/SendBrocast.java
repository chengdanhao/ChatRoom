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
	
	// �����ߵ�ʱ���������������㲥�Լ���
	@Override
	public void run() {
		Log.i("SendBrocast", "���߳� SendBrocast ��");
		
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
		// �½�upd��socket
		try {
			dssend = new DatagramSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//��ȡ�û���welcome��������������Լ���ǰ�ͻ��˵�ip��ַ
		//���ṹ��
		UserInfo userinfo = ChatMainActivity.getUserinfo();

		while (true) {
			if(!runflag)
			{
				dssend.close();
				Log.i("SendBrocast", "SendBrocast�߳��Ѿ�����");
				break;
			}
			
			// ����̻߳������У���ô�ͷ���
			try {
				isa = new InetSocketAddress(InetAddress.getByName("255.255.255.255"),BROADCAST_PORT);

				//��ʼ�����ݰ�
				try {
					dpsend = new DatagramPacket(o2b(userinfo),o2b(userinfo).length, isa);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//�������ݰ�
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
			
			Log.i("SendBrocast", "�ѷ��͹㲥");
			
			try {
				sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	//������ת����byte���ͣ��Ա�udp����
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
		
		//�����ʾ�������û�����ip��ַ���ֿ�
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
