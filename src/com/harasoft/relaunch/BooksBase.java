package com.harasoft.relaunch;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;
import ebook.EBook;
import ebook.Person;
import ebook.parser.InstantParser;
import ebook.parser.Parser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BooksBase {
	Context context;
	DbHelper dbHelper;
	public static SQLiteDatabase db;

	 private Pattern purgeBracketsPattern = Pattern.compile("\\[[\\s\\.\\-_]*\\]");
//	private Pattern purgeBracketsPattern = Pattern
//			.compile("\\[[\\[\\]\\s\\.\\-_]*\\]");

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
		this.context = context;
		dbHelper = new DbHelper(context);
		db = dbHelper.getWritableDatabase();
	}

	public BooksBase(Context context, String path) {
		this.context = context;
		String dbName = (path.replaceAll("\\/", "_")).concat(".db");
		dbHelper = new DbHelper(context, dbName);
		db = dbHelper.getWritableDatabase();
	}

	private void open() {
		if (!db.isOpen())
			db = SQLiteDatabase.openDatabase("library.db", null,
					SQLiteDatabase.OPEN_READWRITE);
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

	public String getEbookName(String fileName, String format) {
		EBook eBook;
		String file = fileName.substring(fileName.lastIndexOf('/') + 1,
				fileName.length());
		if ((!file.endsWith("fb2")) && (!file.endsWith("fb2.zip"))
				&& (!file.endsWith("epub")))
			return file;
		eBook = getBookByFileName(fileName);
		if (!eBook.isOk) {
			Parser parser = new InstantParser();
			eBook = parser.parse(fileName);
			if (eBook.isOk) {
				if ((eBook.sequenceNumber != null)
						&& (eBook.sequenceNumber.length() == 1))
					eBook.sequenceNumber = "0" + eBook.sequenceNumber;
				addBook(eBook);
			}
		}
		if (eBook.isOk) {
			String output = format;
			if (eBook.authors.size() > 0) {
				String author = "";
				if (eBook.authors.get(0).firstName != null)
					author += eBook.authors.get(0).firstName;
				if (eBook.authors.get(0).lastName != null)
					author += " " + eBook.authors.get(0).lastName;
				output = output.replace("%a", author);
			}
			if (eBook.title != null)
				output = output.replace("%t", eBook.title);
			if (eBook.sequenceName != null)
				output = output.replace("%s", eBook.sequenceName);
			else
				output = output.replace("%s", "");
			if (eBook.sequenceNumber != null)
				output = output.replace("%n", eBook.sequenceNumber);
			else
				output = output.replace("%n", "");
			output = output.replace("%f", fileName);
			output = purgeBracketsPattern.matcher(output).replaceAll("");
			output = output.replace("[", "");
			output = output.replace("]", "");
			return output;
		} else
			return file;
	}

	public String getEbookName(String dir, String file, String format) {
		return getEbookName(dir + "/" + file, format);
	}

	public void resetDb() {
		db.execSQL("delete from BOOKS");
		db.execSQL("delete from COVERS");
		db.execSQL("reindex INDEX1");
		db.execSQL("reindex INDEX3");
	}

}
