package com.teamdesign.coen390app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NumberStorage extends SQLiteOpenHelper {

  private static final String TAG = "DatabaseHelper";
  private static final String TABLE_NAME = "people_table1";
  private static final String COL1 = "ID";
  private static final String COL2 = "name";

  public NumberStorage(Context context) {
    super(context, TABLE_NAME, null, 1);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    String createTable1 =
      "CREATE TABLE " +
      TABLE_NAME +
      " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
      COL2 +
      " INTEGER)";

    db.execSQL(createTable1);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int i, int i1) {
    db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
    onCreate(db);
  }

  public boolean addNumber(int item) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put(COL2, item);
    long result = db.insert(TABLE_NAME, null, contentValues);

    if (result == -1) {
      return false;
    } else {
      return true;
    }
  }

  public void recycle() {
    SQLiteDatabase db = this.getWritableDatabase();
    db.delete(TABLE_NAME, "ID > " + 0, null);
  }

  public Cursor getData() {
    SQLiteDatabase db = this.getWritableDatabase();
    String query = "SELECT * FROM " + TABLE_NAME;
    Cursor data = db.rawQuery(query, null);
    return data;
  }

  public Cursor getItemID(String name) {
    SQLiteDatabase db = this.getWritableDatabase();
    String query =
      "SELECT " +
      COL1 +
      " FROM " +
      TABLE_NAME +
      " WHERE " +
      COL2 +
      " = '" +
      name +
      "'";
    Cursor data = db.rawQuery(query, null);
    return data;
  }

  public void updateName(String newName, int id, String oldName) {
    SQLiteDatabase db = this.getWritableDatabase();
    String query =
      "UPDATE " +
      TABLE_NAME +
      " SET " +
      COL2 +
      " = '" +
      newName +
      "' WHERE " +
      COL1 +
      " = '" +
      id +
      "'" +
      " AND " +
      COL2 +
      " = '" +
      oldName +
      "'";
    db.execSQL(query);
  }

  public void deleteName(int id, String name) {
    SQLiteDatabase db = this.getWritableDatabase();
    String query =
      "DELETE FROM " +
      TABLE_NAME +
      " WHERE " +
      COL1 +
      " = '" +
      id +
      "'" +
      " AND " +
      COL2 +
      " = '" +
      name +
      "'";
    db.execSQL(query);
  }
}
