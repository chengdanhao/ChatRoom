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
	/* ���ݿ���� */
	public SQLiteDatabase mSQLiteDatabase = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.historylist);
		m_ListView = (ListView) findViewById(R.id.lv_historylist);
		
		/* ����MyDataBaseAdapter���� */
		m_MyDataBaseAdapter = new MyDataBaseAdapter(this);
		
		/* ȡ�����ݿ���� */
		mSQLiteDatabase = this.openOrCreateDatabase("test.db", MODE_PRIVATE, null);
		m_MyDataBaseAdapter.open();
		UpdataAdapter();
		
		m_ListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				//��ȡѡ����Ŀ���ı�
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
				menu.setHeaderTitle("������ѡ��");
                menu.add(0, 0, 0, "			ɾ���������¼");
			}
		});

	}
	
	
	/* ����һ������ */
	public void UpData()
	{	
		m_MyDataBaseAdapter.updateData(miCount - 1, "�޸ĺ������" + miCount , "user");

		UpdataAdapter();
	}

	/* ��������һ������ */
	public void AddData()
	{
		m_MyDataBaseAdapter.insertData("�������ݿ�����" + miCount , "table1");
		miCount++;
		UpdataAdapter();
	}

	/* �ӱ���ɾ��ָ����һ������ */
	public void DeleteData(int pos) {

		//�ӱ�user��ɾ��
		mSQLiteDatabase.execSQL("DELETE FROM " + "user" + " WHERE _id=" + Integer.toString(pos));
		
		miCount--;
		if (miCount < 0) {
			miCount = 0;
		}
		UpdataAdapter();
	}
	
	/* ������ͼ��ʾ */
	public void UpdataAdapter()
	{
		// ��ȡ���ݿ�Phones��Cursor
		Cursor cur = m_MyDataBaseAdapter.fetchAllData("user");

		miCount = cur.getCount();
		if (cur != null && cur.getCount() >= 0)
		{
			// ListAdapter��ListView�ͺ�̨���ݵ�����
			ListAdapter adapter = new SimpleCursorAdapter(this,
			// ����List��ÿһ�е���ʾģ��
				// ��ʾÿһ�а�������������
				android.R.layout.simple_list_item_1,
				// ���ݿ��Cursor����
				cur,
				// �����ݿ��KEY_CONTENT��ȡ����
				new String[] {MyDataBaseAdapter.KEY_CONTENT },
				// ��NAME��NUMBER��Ӧ��Views
				new int[] { android.R.id.text1});

			/* ��adapter��ӵ�m_ListView�� */
			m_ListView.setAdapter(adapter);
		}
		
		startManagingCursor(cur);
	}
	
	/* �����¼����� */
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			/* �˳�ʱ����Ҫ���ǹر� */
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
		Toast.makeText(HistoryActivity.this, "��ɾ������Ŀ", 3000).show();
//		Toast.makeText(HistoryActivity.this, Integer.toString(pos), 3000).show();
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, Menu.FIRST, 0, "������������¼").setIcon(android.R.drawable.ic_menu_delete);
		
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Toast.makeText(HistoryActivity.this, "XXXXXX", 3000).show();
		return super.onOptionsItemSelected(item);
	}
}
