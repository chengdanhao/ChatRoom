package com.chatroom.client;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class HistoryContentActivity extends Activity{
	MyDataBaseAdapter m_MyDataBaseAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.historycontent);
		
		TextView title = (TextView) findViewById(R.id.tv_hctitle);
		TextView content = (TextView) findViewById(R.id.tv_hccontent);
		
		Bundle extras = getIntent().getExtras();
		title.setText("�����û� " + extras.getString("HistoryActivity") + " ��������ʷ��¼");
		
		m_MyDataBaseAdapter = new MyDataBaseAdapter(this);
		m_MyDataBaseAdapter.open();
		//�����ݿ��������ʾ����
		Cursor cur = m_MyDataBaseAdapter.fetchAllData(extras.getString("HistoryActivity"));

    	if(cur.moveToFirst()){
    		do{
				String cont = cur.getString(1);
				content.append(cont);
			}while(cur.moveToNext());
    	}
		
		startManagingCursor(cur);
	}
}
