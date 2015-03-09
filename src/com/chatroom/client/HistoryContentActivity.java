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
		title.setText("您与用户 " + extras.getString("HistoryActivity") + " 的聊天历史记录");
		
		m_MyDataBaseAdapter = new MyDataBaseAdapter(this);
		m_MyDataBaseAdapter.open();
		//将数据库的内容显示出来
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
