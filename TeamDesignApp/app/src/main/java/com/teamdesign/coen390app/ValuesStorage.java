package com.example.mystoragecenter;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by User on 2/28/2017.
 */

public class ValuesStorage extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME1 = "reading_table";

    private static final String COL1 = "ID";
    private static final String COL2 = "name";
    private static final String COL3 = "name2";
    private static final String COL4 = "name3";




    public ValuesStorage(Context context) {
        super(context, TABLE_NAME1, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable1 = "CREATE TABLE " + TABLE_NAME1 + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 +" TEXT,"+
                COL3 +" TEXT,"+
                COL4 +" TEXT)";

        /*
String createTable1 = "CREATE TABLE " + TABLE_NAME1 + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 +" INTEGER,"+
                COL3 +" INTEGER,"+
                COL4 +" INTEGER)";

*/

        db.execSQL(createTable1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME1);

        onCreate(db);
    }


    public boolean addValues(String item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, item);

        Log.d(TAG, "addData: Adding " + item + " to " + TABLE_NAME1);

        long result = db.insert(TABLE_NAME1, null, contentValues);

        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

// triple digit yo

    public boolean addValues(String item,String item2,String item3) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, item);
        contentValues.put(COL3, item2);
        contentValues.put(COL4, item3);

        Log.d(TAG, "addData: Adding " + item + " to " + TABLE_NAME1);

        long result = db.insert(TABLE_NAME1, null, contentValues);

        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }






    /**
     * Returns all the data from database
     * @return
     */
    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME1;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    /**
     * Returns all the value from database
     * @return
     */


    public Cursor getValue(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME1;
        Cursor data = db.rawQuery(query, null);
        return data;
    }



    /**
     * Returns only the ID that matches the name passed in
     * @param name
     * @return
     */
    public Cursor getItemID(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL1 + " FROM " + TABLE_NAME1 +
                " WHERE " + COL2 + " = '" + name + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    /**
     * Updates the name field
     * @param newName
     * @param id
     * @param oldName
     */
    public void updateName(String newName, int id, String oldName){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME1 + " SET " + COL2 +
                " = '" + newName + "' WHERE " + COL1 + " = '" + id + "'" +
                " AND " + COL2 + " = '" + oldName + "'";
        Log.d(TAG, "updateName: query: " + query);
        Log.d(TAG, "updateName: Setting name to " + newName);
        db.execSQL(query);
    }

    /**
     * Delete from database
     * @param id
     * @param name
     */
    public void deleteName(int id, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME1 + " WHERE "
                + COL1 + " = '" + id + "'" +
                " AND " + COL2 + " = '" + name + "'";
        Log.d(TAG, "deleteName: query: " + query);
        Log.d(TAG, "deleteName: Deleting " + name + " from database.");
        db.execSQL(query);
    }

   
    public void recycle(){
        SQLiteDatabase db = this.getWritableDatabase();
        String ALTER_TBL ="delete from " + TABLE_NAME1 +
                " where "+COL1 +" in (select "+ COL1 +" from "+ TABLE_NAME1+" order by _ID LIMIT 3);";

        db.delete(TABLE_NAME1, "ID > "+0, null);  // empty all pre-existing database

      
    }



}

