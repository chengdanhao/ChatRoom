package com.chatroom.client;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDataBaseAdapter
{
	// ����һ�����ݵ�id������
	public static final String	KEY_ID		= "_id";												

	// ����һ�����ݵ�����
	public static final String	KEY_CONTENT		= "content";

	// ���ݿ�����Ϊdata
	private static final String	DB_NAME			= "test.db";
	
	// ���ݿ�汾
	private static final int	DB_VERSION		= 1;

	// ����Context����
	private Context				mContext		= null;
	
	//����һ����
	private static final String	DB_CREATE		= "CREATE TABLE " + "user" + " (" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CONTENT + " TEXT)";

	// ִ��open���������ݿ�ʱ�����淵�ص����ݿ����
	private SQLiteDatabase		mSQLiteDatabase	= null;

	// ��SQLiteOpenHelper�̳й���
	private DatabaseHelper		mDatabaseHelper	= null;
	
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		/* ���캯��-����һ�����ݿ� */
		DatabaseHelper(Context context)
		{
			//������getWritableDatabase() 
			//�� getReadableDatabase()����ʱ
			//�򴴽�һ�����ݿ�
			super(context, DB_NAME, null, DB_VERSION);
			
			
		}

		/* ����һ���� */
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			// ���ݿ�û�б�ʱ����һ��
			db.execSQL(DB_CREATE);
		}

		/* �������ݿ� */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}
	
	/* ���캯��-ȡ��Context */
	public MyDataBaseAdapter(Context context)
	{
		mContext = context;
	}


	// �����ݿ⣬�������ݿ����
	public void open() throws SQLException
	{
		mDatabaseHelper = new DatabaseHelper(mContext);
		mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
	}


	// �ر����ݿ�
	public void close()
	{
		mDatabaseHelper.close();
	}


	public void addTable(String name){
		String table = "CREATE TABLE " + name  + " (" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CONTENT + " TEXT)";
		mSQLiteDatabase.execSQL(table);
	}
	
	/* ����һ������ */
	public long insertData(String data, String tablename)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CONTENT, data);

		return mSQLiteDatabase.insert(tablename, KEY_ID, initialValues);
	}

	/* ɾ��һ������ */
	public boolean deleteData(long rowId , String table)
	{
		return mSQLiteDatabase.delete(table, KEY_ID + "=" + rowId, null) > 0;
	}

	/* ͨ��Cursor��ѯ�������� */
	public Cursor fetchAllData(String table)
	{
		Cursor cur = mSQLiteDatabase.query(table, new String[] { KEY_ID, KEY_CONTENT }, null, null, null, null, null);
		return cur;
	}

	/* ��ѯָ������ */
	public Cursor fetchData(long rowId , String table) throws SQLException
	{

		Cursor mCursor =

		mSQLiteDatabase.query(true, table, new String[] { KEY_ID, KEY_CONTENT }, KEY_ID + "=" + rowId, null, null, null, null, null);

		if (mCursor != null)
		{
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	/* ����һ������ */
	public boolean updateData(long rowId, String data , String table)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_CONTENT, data);

		return mSQLiteDatabase.update(table, args, KEY_ID + "=" + rowId, null) > 0;
	}
	
}

