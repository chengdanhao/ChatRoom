package com.chatroom.client;

import java.util.HashMap;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryActivity extends Activity
{
	private static int			miCount			= 0;
	ListView					m_ListView		= null;
	MyDataBaseAdapter m_MyDataBaseAdapter;
	/* 数据库对象 */
	public SQLiteDatabase mSQLiteDatabase = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.historylist);
		m_ListView = (ListView) findViewById(R.id.lv_historylist);
		
		/* 构造MyDataBaseAdapter对象 */
		m_MyDataBaseAdapter = new MyDataBaseAdapter(this);
		
		/* 取得数据库对象 */
		mSQLiteDatabase = this.openOrCreateDatabase("test.db", MODE_PRIVATE, null);
		m_MyDataBaseAdapter.open();
		UpdataAdapter();
		
		m_ListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				//获取选中项目的文本
				String str = (String) ((TextView)arg1).getText();

				Intent intent = new Intent();
				intent.setClass(HistoryActivity.this, HistoryContentActivity.class);
				intent.putExtra("HistoryActivity", str);
				startActivity(intent);
				
			}
			
		});
		
		m_ListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				// TODO Auto-generated method stub
				menu.setHeaderTitle("请慎重选择");
                menu.add(0, 0, 0, "			删除该聊天记录");
			}
		});

	}
	
	
	/* 更新一条数据 */
	public void UpData()
	{	
		m_MyDataBaseAdapter.updateData(miCount - 1, "修改后的数据" + miCount , "user");

		UpdataAdapter();
	}

	/* 向表中添加一条数据 */
	public void AddData()
	{
		m_MyDataBaseAdapter.insertData("测试数据库数据" + miCount , "table1");
		miCount++;
		UpdataAdapter();
	}

	/* 从表中删除指定的一条数据 */
	public void DeleteData(int pos) {

		//从表user中删除
		mSQLiteDatabase.execSQL("DELETE FROM " + "user" + " WHERE _id=" + Integer.toString(pos));
		
		miCount--;
		if (miCount < 0) {
			miCount = 0;
		}
		UpdataAdapter();
	}
	
	/* 更行试图显示 */
	public void UpdataAdapter()
	{
		// 获取数据库Phones的Cursor
		Cursor cur = m_MyDataBaseAdapter.fetchAllData("user");

		miCount = cur.getCount();
		if (cur != null && cur.getCount() >= 0)
		{
			// ListAdapter是ListView和后台数据的桥梁
			ListAdapter adapter = new SimpleCursorAdapter(this,
			// 定义List中每一行的显示模板
				// 表示每一行包含两个数据项
				android.R.layout.simple_list_item_1,
				// 数据库的Cursor对象
				cur,
				// 从数据库的KEY_CONTENT中取数据
				new String[] {MyDataBaseAdapter.KEY_CONTENT },
				// 与NAME和NUMBER对应的Views
				new int[] { android.R.id.text1});

			/* 将adapter添加到m_ListView中 */
			m_ListView.setAdapter(adapter);
		}
		
		startManagingCursor(cur);
	}
	
	/* 按键事件处理 */
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			/* 退出时，不要忘记关闭 */
			m_MyDataBaseAdapter.close();
			mSQLiteDatabase.close();
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo(); 
		int pos = (int) m_ListView.getAdapter().getItemId(menuInfo.position);
		DeleteData(pos);
		
		Cursor cur = (Cursor) m_ListView.getAdapter().getItem(menuInfo.position);
		int index = cur.getColumnIndex("content");
	    String name = cur.getString(index);
		mSQLiteDatabase.execSQL("DROP TABLE " + name);
		
		startManagingCursor(cur);
		Toast.makeText(HistoryActivity.this, "已删除该项目", 3000).show();
//		Toast.makeText(HistoryActivity.this, Integer.toString(pos), 3000).show();
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, Menu.FIRST, 0, "清空所有聊天记录").setIcon(android.R.drawable.ic_menu_delete);
		
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Toast.makeText(HistoryActivity.this, "XXXXXX", 3000).show();
		return super.onOptionsItemSelected(item);
	}
}
