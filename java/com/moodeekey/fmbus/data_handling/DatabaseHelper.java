package com.moodeekey.fmbus.data_handling;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Database helper ensures correct creation, usage and closing of DB.
 * Thanks to Yaqub Ahmad
 * http://stackoverflow.com/users/265167/yaqub-ahmad
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static String TAG = "DatabaseHelper"; // logcat tag
    //destination path (location) of our database on device
    private static String DB_PATH = "";
    private static String DB_NAME ="bus_schedule_db.sqlite";
    private SQLiteDatabase mDataBase;
    private final Context mContext;
    public static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        //make sure that correct db path instantiated
        if(android.os.Build.VERSION.SDK_INT >= 17){
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";

        }
        else
        {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
    }
    /** Copies database from assets if one does not already exist */
    public void createDatabase() throws IOException {
        boolean mDataBaseExist = checkDatabase();
        if(!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try{
                //Copy the database from assests
                copyDatabase();
               // Log.d(TAG,"Database is copied from assets folder");

            }
            catch (IOException mIOException)
            {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }
    /** Checks that the database exists */
    private  boolean checkDatabase(){
        File dbFile = new File(DB_PATH + DB_NAME);
        //Log.d("DB", dbFile + "   "+ dbFile.exists());
        return dbFile.exists();
    }
    /** Copies the database from assets */
    private void copyDatabase() throws IOException{
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0){
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }
    /** Returns SQLiteDB object that can be used by DabaseAdapter */
    public boolean openDatabase() throws SQLException {
        String mPath = DB_PATH + DB_NAME;
        //Log.d(TAG,"Opening DB " +mPath);
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }
    /** Closes SQLite DB */
    public synchronized void close() {
        if(mDataBase != null)
            mDataBase.close();
        super.close();
    }

    public void onCreate(SQLiteDatabase db){
        //blank
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //blank
    }

}
