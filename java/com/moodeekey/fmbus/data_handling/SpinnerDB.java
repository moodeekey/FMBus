package com.moodeekey.fmbus.data_handling;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Database class that is used for storage of Route/Stop entries
 */
public class SpinnerDB extends SQLiteOpenHelper {
    //private static String TAG = "SpinnerDB"; // logcat tag
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "fav_routes_db.sqlite";

    // Labels table name
    private static final String TABLE_LABELS = "FAV_BUS";

    // Labels Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_ROUTE = "route_number";
    private static final String KEY_STOP = "stop_name";

    public SpinnerDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    /** Creates DB for storing stuff*/
    public void onCreate(SQLiteDatabase db) {
        //create query
        String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_LABELS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ROUTE + " TEXT, " + KEY_STOP + " TEXT)";
        try {
         //   Log.d(TAG, "Creating DB " + db.getPath());
            db.execSQL(CREATE_CATEGORIES_TABLE);
        }
        catch(SQLiteException mSQLiteException){
          //  Log.e(TAG,mSQLiteException.toString());
        }
    }
    /** Updates DB aka recreating it */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
          //  Log.d(TAG, "Upgrading DB");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LABELS);
            onCreate(db);
        }
        catch (SQLiteException mSQLiteException){
          //  Log.e(TAG, mSQLiteException.toString());
        }
    }
    /** Empties DB same as above, but no need to pass versions */
    public void deleteAllEntries(SQLiteDatabase db) {
        try {
          //  Log.d(TAG, "Deleting all entries");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LABELS);
            onCreate(db);
        }
        catch (SQLiteException mSQLiteException){
          //  Log.e(TAG, mSQLiteException.toString());
        }
    }
    /** Inserts a new Route/Stop entry to DB */
    public void addSingleEntry(String route, String stop) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        try {
          //  Log.d(TAG, "Inserting " + route + " and " + stop);
            values.put(KEY_ROUTE, route);
            values.put(KEY_STOP, stop);
            // Inserting Row
            db.insert(TABLE_LABELS, null, values);
        }
        catch (SQLiteException mSQLiteException){
           // Log.e(TAG, mSQLiteException.toString());
        }
        finally {
            db.close(); // Closing database connection
        }
    }
    /** Gets all entries from DB */
    public List<List<String>> getAllEntries() {
        List<String> routes = new ArrayList<String>();
        List<String> stops = new ArrayList<String>();
        List<String> itemIds = new ArrayList<String>();
        List<List<String>> result= new ArrayList<List<String>>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LABELS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                itemIds.add(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                routes.add(cursor.getString(cursor.getColumnIndex(KEY_ROUTE)));
                stops.add(cursor.getString(cursor.getColumnIndex(KEY_STOP)));
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        db.close();
        //ensure correct order
        result.add(0,itemIds);
        result.add(1, routes);
        result.add(2, stops);
        return result;
    }
    /** Deletes entry by its id */
    public void deleteSingleEntry(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
          //  Log.d(TAG, "Deleting entry " + Integer.toString(id));
            db.delete(TABLE_LABELS, KEY_ID + "=" + Integer.toString(id), null);
        }
        catch (SQLiteException mSQLiteException){
            //Log.e(TAG, mSQLiteException.toString());
        }
        finally {
            db.close();
        }
    }
}
