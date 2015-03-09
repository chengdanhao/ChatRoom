package com.chatroom.client;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDataBaseAdapter
{
	// 表中一条数据的id，主键
	public static final String	KEY_ID		= "_id";												

	// 表中一条数据的内容
	public static final String	KEY_CONTENT		= "content";

	// 数据库名称为data
	private static final String	DB_NAME			= "test.db";
	
	// 数据库版本
	private static final int	DB_VERSION		= 1;

	// 本地Context对象
	private Context				mContext		= null;
	
	//创建一个表
	private static final String	DB_CREATE		= "CREATE TABLE " + "user" + " (" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CONTENT + " TEXT)";

	// 执行open（）打开数据库时，保存返回的数据库对象
	private SQLiteDatabase		mSQLiteDatabase	= null;

	// 由SQLiteOpenHelper继承过来
	private DatabaseHelper		mDatabaseHelper	= null;
	
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		/* 构造函数-创建一个数据库 */
		DatabaseHelper(Context context)
		{
			//当调用getWritableDatabase() 
			//或 getReadableDatabase()方法时
			//则创建一个数据库
			super(context, DB_NAME, null, DB_VERSION);
			
			
		}

		/* 创建一个表 */
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			// 数据库没有表时创建一个
			db.execSQL(DB_CREATE);
		}

		/* 升级数据库 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}
	
	/* 构造函数-取得Context */
	public MyDataBaseAdapter(Context context)
	{
		mContext = context;
	}


	// 打开数据库，返回数据库对象
	public void open() throws SQLException
	{
		mDatabaseHelper = new DatabaseHelper(mContext);
		mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
	}


	// 关闭数据库
	public void close()
	{
		mDatabaseHelper.close();
	}


	public void addTable(String name){
		String table = "CREATE TABLE " + name  + " (" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CONTENT + " TEXT)";
		mSQLiteDatabase.execSQL(table);
	}
	
	/* 插入一条数据 */
	public long insertData(String data, String tablename)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CONTENT, data);

		return mSQLiteDatabase.insert(tablename, KEY_ID, initialValues);
	}

	/* 删除一条数据 */
	public boolean deleteData(long rowId , String table)
	{
		return mSQLiteDatabase.delete(table, KEY_ID + "=" + rowId, null) > 0;
	}

	/* 通过Cursor查询所有数据 */
	public Cursor fetchAllData(String table)
	{
		Cursor cur = mSQLiteDatabase.query(table, new String[] { KEY_ID, KEY_CONTENT }, null, null, null, null, null);
		return cur;
	}

	/* 查询指定数据 */
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

	/* 更新一条数据 */
	public boolean updateData(long rowId, String data , String table)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_CONTENT, data);

		return mSQLiteDatabase.update(table, args, KEY_ID + "=" + rowId, null) > 0;
	}
	
}

