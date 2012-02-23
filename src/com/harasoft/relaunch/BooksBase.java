package com.harasoft.relaunch;

import ebook.EBook;
import ebook.Person;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BooksBase {
	DbHelper dbHelper;
	public SQLiteDatabase db;

	private class DbHelper extends SQLiteOpenHelper {
		final static int VERSION = 1;

		public DbHelper(Context context) {
			super(context, "library.db", null, VERSION);
		}

		public DbHelper(Context context, String name) {
			super(context, name, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table if not exists BOOKS ("
					+ "ID integer primary key autoincrement, "
					+ "FILE text unique, " + "TITLE text default '', "
					+ "FIRSTNAME text default '', "
					+ "LASTNAME text default '', " + "SERIES text default '', "
					+ "NUMBER text default '')");
			db.execSQL("create table if not exists COVERS ("
					+ "ID integer primary key autoincrement, "
					+ "BOOK integer unique, " + "COVER blob)");
			db.execSQL("create index if not exists INDEX1 on BOOKS(FILE)");
			db.execSQL("create index if not exists INDEX3 on COVERS(BOOK)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	public BooksBase(Context context) {
		dbHelper = new DbHelper(context);
		db = dbHelper.getWritableDatabase();
	}

	public BooksBase(Context context, String path) {
		String dbName = (path.replaceAll("\\/", "_")).concat(".db");
		dbHelper = new DbHelper(context, dbName);
		db = dbHelper.getWritableDatabase();
	}

	public long addBook(EBook book) {
		long bookId;
		ContentValues cv = new ContentValues();
		cv.put("FILE", book.fileName);
		cv.put("TITLE", book.title);
		if (book.authors.size() > 0) {
			cv.put("FIRSTNAME", book.authors.get(0).firstName);
			cv.put("LASTNAME", book.authors.get(0).lastName);
		}
		if (book.sequenceName != null) {
			cv.put("SERIES", book.sequenceName);
		}
		if (book.sequenceNumber != null) {
			cv.put("NUMBER", book.sequenceNumber);
		}
		bookId = db.insertOrThrow("BOOKS", null, cv);
//		if (book.authors.size() == 0) {
//			book.authors.add(new Person("Unknown Author"));
//		}
//		if (book.title == null)
//			book.title = "";
//		if (book.authors.get(0).firstName == null)
//			book.authors.get(0).firstName = "";
//		if (book.authors.get(0).lastName == null)
//			book.authors.get(0).lastName = "";
//		if (book.sequenceName == null)
//			book.sequenceName = "";
//		if (book.sequenceNumber == null)
//			book.sequenceNumber = "";
//		db.rawQuery("insert into BOOKS (FILE, TITLE, FIRSTNAME, LASTNAME, " +
//				"SERIES, NUMBER) values(?, ?, ?, ?, ?, ?)", new String[] {
//				book.fileName, book.title, book.authors.get(0).firstName, 
//				book.authors.get(0).lastName, book.sequenceName, book.sequenceNumber });

		return bookId;
	}

	@SuppressWarnings("unused")
	private long getAuthorIdByName(String name) {
		long id;
		Cursor cursor = db.rawQuery("select ID from AUTHORS where NAME=?",
				new String[] { name });
		if (cursor.moveToFirst())
			id = cursor.getLong(0);
		else
			id = -1;
		cursor.close();
		return id;
	}

	public long getBookIdByFileName(String fileName) {
		long id;
		Cursor cursor = db.rawQuery("select ID from BOOKS where FILE=?",
				new String[] { fileName });
		if (cursor.moveToFirst())
			id = cursor.getLong(0);
		else
			id = -1;
		cursor.close();
		return id;
	}

	public EBook getBookById(long id) {
		EBook book = new EBook();
		Person author = new Person();
		Cursor cursor = db.rawQuery("select * from BOOKS where id=?",
				new String[] { "" + id });
		if (cursor.moveToFirst()) {
			book.fileName = cursor.getString(cursor.getColumnIndex("FILE"));
			book.title = cursor.getString(cursor.getColumnIndex("TITLE"));
			author.firstName = cursor.getString(cursor
					.getColumnIndex("FIRSTNAME"));
			author.lastName = cursor.getString(cursor
					.getColumnIndex("LASTNAME"));
			book.authors.add(author);
			book.sequenceName = cursor.getString(cursor
					.getColumnIndex("SERIES"));
			book.sequenceNumber = cursor.getString(cursor
					.getColumnIndex("NUMBER"));
			book.isOk = true;
		} else
			book.isOk = false;
		cursor.close();
		return book;
	}

	public EBook getBookByFileName(String fileName) {
		EBook book = new EBook();
		Person author = new Person();
		Cursor cursor = db.rawQuery("select * from BOOKS where FILE=?",
				new String[] { fileName });
		if (cursor.moveToFirst()) {
			book.title = cursor.getString(2);
			author.firstName = cursor.getString(3);
			author.lastName = cursor.getString(4);
			book.authors.add(author);
			book.sequenceName = cursor.getString(5);
			book.sequenceNumber = cursor.getString(6);
			book.isOk = true;
		} else
			book.isOk = false;
		cursor.close();
		return book;
	}

	public byte[] getCoverByBookId(long id) {
		byte[] cover;
		Cursor cursor = db.rawQuery("select * from COVERS where BOOK=?",
				new String[] { "" + id });
		if (cursor.moveToFirst()) {
			cover = cursor.getBlob(cursor.getColumnIndex("COVER"));
		} else
			cover = null;
		cursor.close();
		return cover;
	}

}
