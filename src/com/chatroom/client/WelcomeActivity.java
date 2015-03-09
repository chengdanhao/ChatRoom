package com.chatroom.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends Activity {
	public String userName;
	public int REQUEST_CODE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);// 页面布局

		Button login = (Button) findViewById(R.id.btn_login);
		Button logout = (Button) findViewById(R.id.btn_logout);
		Button viewhistory = (Button) findViewById(R.id.btn_viewhistory); 

		login.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText username = (EditText) findViewById(R.id.et_username);
				if (username.getText().toString().equals("")) {
					Toast.makeText(WelcomeActivity.this, " 昵称不能为空，请重新输入 ", 3000).show();
				} else {
					userName = username.getText().toString();
					Intent intent = new Intent();
					intent.setClass(WelcomeActivity.this, ChatMainActivity.class);
					intent.putExtra("WelcomeActivity", userName);
					startActivityForResult(intent, REQUEST_CODE );
				}
			}
		});
		
		
		logout.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(WelcomeActivity.this, " 正在退出聊天室程序 ", 3000).show();
				finish();
			}
		});
		
		viewhistory.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(WelcomeActivity.this, HistoryActivity.class);
				startActivity(intent);
			}
		});
//		
//		MyDataBaseAdapter m_MyDataBaseAdapter = new MyDataBaseAdapter(this);
//		
//		/* 取得数据库对象 */
//		m_MyDataBaseAdapter.open();
//		m_MyDataBaseAdapter.insertData(99, "测试数据库数据" + 99);
//		m_MyDataBaseAdapter.close();
		

	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode , int resultCode , Intent data){
		
		if(requestCode == REQUEST_CODE){
			if(resultCode == RESULT_OK){
				Bundle extras = data.getExtras();
				TextView tv = (TextView)findViewById(R.id.textView1);
				tv.setText(extras.getString("ChatMainActivity"));
			}
			
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Toast.makeText(WelcomeActivity.this, " 正在退出聊天室程序 ", 3000).show();
			finish();
			
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

}


