package com.moodeekey.fmbus.data_handling;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.format.Time;
import android.util.Log;

import com.moodeekey.fmbus.map_screen.MapViewItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database adapter is an interface between classes and DB Helper, all DB queries go here
 */
public class DatabaseAdapter {
    private static boolean IS_WRITABLE;

    private static final String TABLE_ROUTES = "routes";
    private static final String TABLE_STOPS = "stops";
    private static final String TABLE_ARRIVALS = "arrivals";

    private static final String KEY_ID = "_id";

    private static final String ROUTE_CODE = "code";
    private static final String ROUTE_STRING = "string";
    private static final String ROUTE_NUMBER = "number";
    private static final String ENCODED_VALUE = "encoded_line";
    private static final String ROUTE_IS_SELECTED = "is_selected";

    private static final String STOP_NAME = "name";


    private static final String ARRIVAL_STOP_ID = "stop_id";
    private static final String ARRIVAL_ROUTE_NUM = "route_num";
    private static final String ARRIVAL_TIME = "arr_time";


    //protected static final String TAG = "DataAdapter"; //logcat tag

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DatabaseHelper mDbHelper;

    public DatabaseAdapter(Context context) {
        this.mContext = context;
        mDbHelper = new DatabaseHelper(mContext);
    }
    /** Creates a new DatabaseHelper object */
    public DatabaseAdapter createDatabase() throws SQLException {
        try {
            //Log.d(TAG,"Creating DB");
            mDbHelper.createDatabase();
        }
        catch (IOException mIOException){
            //Log.e(TAG, mIOException.toString() + "  Database creation failure");
            throw new Error("DB creation failure");
        }
        return this;
    }
    /** Opens readable database */
    public DatabaseAdapter open() throws SQLException {
        try{
            //Log.d(TAG,"Opening DB");
            mDbHelper.openDatabase();
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        }
        catch (SQLException mSQLException) {
            //Log.e(TAG, "Readable DB opening failed : "+ mSQLException.toString());
        }
        return this;
    }
    /** Opens writable database */
    public DatabaseAdapter open(boolean isWritable){
        IS_WRITABLE = isWritable;
        try {
            //Log.d(TAG,"Opening writable DB");
            mDbHelper.openDatabase();
            mDbHelper.close();
            mDb = mDbHelper.getWritableDatabase();

        } catch (SQLException mSQLException) {
            //Log.e(TAG, "Writable DB opening failed" + mSQLException.toString());
        }
        return this;
    }
    /** Updates DB. Changes selection state of Routes Visible on the MapScreen*/
    public void updateData(ArrayList<MapViewItem> routeList) throws SQLException {

        if(DatabaseAdapter.IS_WRITABLE){
            ContentValues mContentValues = new ContentValues();
            //Log.d(TAG,"Beginning update");
            mDb.beginTransaction(); //to avoid block of main thread, boosts update speed

            for (MapViewItem route : routeList) {
               // Log.d(TAG,"Updating " + route.getName());
                mContentValues.put(ROUTE_IS_SELECTED, route.switch_state());  //change selection state
                mDb.update(TABLE_ROUTES, mContentValues, ROUTE_STRING +" = '"+ route.getName() +"'", null);
                mContentValues.clear();
            }
            mDb.setTransactionSuccessful();
            mDb.endTransaction();//letting know DB that we are done
            mDb.close();
        }
    }

    public void close(){
        mDbHelper.close();
    }

    /** Gets all routes from DB*/
    public List<String> getAllRoutes() {
        List<String> result = new ArrayList<String>();
        try {
            //Log.d(TAG,"Retrieving all routes");
            String selectQuery = "SELECT " + ROUTE_STRING + " FROM " + TABLE_ROUTES +
                    //City Map has id 100, and used only on ScheduleScreen
                    " WHERE " + ROUTE_NUMBER + "!= 100" +
                    " ORDER BY " + KEY_ID;
            Cursor cursor = mDb.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    result.add(cursor.getString(cursor.getColumnIndex(ROUTE_STRING)));
                } while (cursor.moveToNext());
            }
            cursor.close();
            mDb.close();
        } catch (SQLiteException mSQLiteException) {
            //Log.e(TAG,"Query failed " + mSQLiteException.toString());
        }
        return result;
    }
    /** Gets all routes from DB*/
    public String getRouteNumberByString(String routeString) {
        String result = null;
        try {
           // Log.d(TAG,"Retrieving route number by String");
            String selectQuery = "SELECT " + ROUTE_NUMBER + " FROM " + TABLE_ROUTES +
                    " WHERE " + ROUTE_STRING + " LIKE '%"
                    + routeString +"%'";
            Cursor cursor = mDb.rawQuery(selectQuery, null);
            if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    result = cursor.getString(0);
                //Log.d("DB","INSIDE AND GOT " + result);
            }
            cursor.close();
            mDb.close();
        } catch (SQLiteException mSQLiteException) {
            //Log.e(TAG,"Query failed " + mSQLiteException.toString());
        }
        return result;
    }
    /** Gets all stops of the Route from DB*/
    public List<String> getAllStops(String route){
        List<String> result = new ArrayList<String>();
        try {
            //Log.d(TAG,"Retrieving all stops by route" + route);
            //TODO optimize query
            String selectQuery = "SELECT " + TABLE_STOPS + '.' + STOP_NAME +
                    " FROM " + TABLE_STOPS + " WHERE " + TABLE_STOPS + '.' + KEY_ID +
                    " IN " +
                    "(SELECT " + ARRIVAL_STOP_ID + " FROM " + TABLE_ARRIVALS +
                    " WHERE " + TABLE_ARRIVALS + '.' + ARRIVAL_ROUTE_NUM +
                    "=" + route + ") ORDER BY " + KEY_ID;

            Cursor cursor = mDb.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    result.add(cursor.getString(cursor.getColumnIndex(STOP_NAME)));
                } while (cursor.moveToNext());
            }
            cursor.close();
            mDb.close();
        } catch (SQLiteException mSQLiteException) {
            //Log.e(TAG,"Query failed " + mSQLiteException.toString());
        }
        return result;
    }
    /** Gets arrival time of selected route and stop Monday - Friday*/
    public Long getWeekdayTime(String route, String stop, Long time){
        Long result = null;
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();

        try {

            String selectQuery = "SELECT MIN(" + ARRIVAL_TIME + ") FROM " + TABLE_ARRIVALS +
                    " WHERE " + ARRIVAL_TIME + " > " + Long.toString(time) +  " AND " + ARRIVAL_ROUTE_NUM
                    + " = " + route +" AND " + ARRIVAL_STOP_ID +
                    " IN " +
                    "(SELECT " + KEY_ID + " FROM " + TABLE_STOPS + " WHERE " + STOP_NAME + " LIKE '%"
                    + stop +"%') ORDER BY " + KEY_ID;

            Cursor cursor = mDb.rawQuery(selectQuery, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = cursor.getLong(0);
            }

            else
                return 0L;
            cursor.close();
            mDb.close();
            Log.d("DB",Long.toString(result));
        }catch(SQLiteException e){
            //Log.e(TAG, e.toString());
        }

        return result;

    }

    /** Gets arrival time of selected route and stop Saturday*/
    public Long getWeekendTime(String route, String stop, Long time){
        Long result = null;
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();

        try {
            //what a mess you might say...and you will be right
            String selectQuery = "SELECT MIN(" + ARRIVAL_TIME + ") FROM " + TABLE_ARRIVALS +
                    " WHERE " + ARRIVAL_TIME + " > " + Long.toString(time) +  " AND " + ARRIVAL_ROUTE_NUM
                    + " = " + route + " AND no_saturday = 0" + " AND " + ARRIVAL_STOP_ID +
                    " IN " +
                    "(SELECT " + KEY_ID + " FROM " + TABLE_STOPS + " WHERE " + STOP_NAME + " LIKE '%"
                    + stop +"%') ORDER BY " + KEY_ID;

            Cursor cursor = mDb.rawQuery(selectQuery, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = cursor.getLong(0);
            }
            else
                result = 0L;
            cursor.close();
            mDb.close();
        }catch(SQLiteException e){
            //Log.e(TAG, e.toString());
        }

        return result;

    }
    /** Gets all routes that are selected by user to display on the MapScreen*/
    public List<List<String>> getAllSelectedRoutes() {

        List<List<String>> result = new ArrayList<List<String>>();
        List<String> values = new ArrayList<String>();
        List<String> routes = new ArrayList<String>();


        try {

            //Log.d(TAG, "Retrieving all selected routes");
            String selectQuery = "SELECT " + ENCODED_VALUE + ", " + ROUTE_CODE +
                    " FROM " + TABLE_ROUTES +
                    " WHERE " + ROUTE_IS_SELECTED + " = 1";

            Cursor cursor = mDb.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                   // Log.e(TAG, "Query Not Empty");
                    values.add(cursor.getString(cursor.getColumnIndex(ENCODED_VALUE)));
                    routes.add(cursor.getString(cursor.getColumnIndex(ROUTE_CODE)));

                } while (cursor.moveToNext());
            }
            result.add(values);
            result.add(routes);
            // closing connection
            cursor.close();
            mDb.close();
        }
        catch (SQLiteException mSQLiteException) {
           // Log.e(TAG, "Query failed " + mSQLiteException.toString());
        }

        return result;
    }
    /** Gets all routes that are selected by user to display on the MapScreen*/
    public  List<List<String>> getListOfRoutes() throws SQLException {

        List<List<String>> result = new ArrayList<List<String>>();


        List<String> route_strings = new ArrayList<String>();
        List<String> route_codes = new ArrayList<String>();
        List<String> truth_values = new ArrayList<String>();

        //Log.e(TAG, "getting_list_of_routes");

        try {
            String select_query = "SELECT " + ROUTE_STRING +
                    ", " + ROUTE_CODE + ", " + ROUTE_IS_SELECTED +
                    " FROM " + TABLE_ROUTES + " WHERE " + ROUTE_NUMBER + "!= 100" +
                    " ORDER BY " + KEY_ID;

            Cursor cursor = mDb.rawQuery(select_query, null);

            if (cursor.moveToFirst()) {

               // Log.e(TAG, "retrieving data");

                do {

                    route_strings.add(cursor.getString(cursor.getColumnIndex(ROUTE_STRING)));
                    route_codes.add(cursor.getString(cursor.getColumnIndex(ROUTE_CODE)));
                    truth_values.add(cursor.getString(cursor.getColumnIndex(ROUTE_IS_SELECTED)));

                } while (cursor.moveToNext());
            }

        } catch (SQLException error) {
            //Log.e(TAG, "While getListOfRoutes" + error.toString());
        }
        result.add(route_strings);
        result.add(route_codes);
        result.add(truth_values);

        return result;
    }

    public List<String> getAllRouteCodes() {
        List<String> result = new ArrayList<String>();
        try {

            String selectQuery = "SELECT " + ROUTE_CODE + " FROM " + TABLE_ROUTES +
                    " ORDER BY " + KEY_ID;
            Cursor cursor = mDb.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    result.add(cursor.getString(0));//cursor.getColumnIndex())
                } while (cursor.moveToNext());
            }

            cursor.close();
            mDb.close();
        } catch (SQLiteException e) {
           // Log.e(TAG, e.toString());
        }
        return result;

    }

    public List<String> getAllRouteStrings() {
        List<String> result = new ArrayList<String>();
        try {

            String selectQuery = "SELECT " + ROUTE_STRING + " FROM " + TABLE_ROUTES +
                    " ORDER BY " + KEY_ID;
            Cursor cursor = mDb.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    result.add(cursor.getString(0));//cursor.getColumnIndex())
                } while (cursor.moveToNext());
            }

            cursor.close();
            mDb.close();
        } catch (SQLiteException e) {
           // Log.e(TAG, e.toString());
        }
        return result;

    }


}

