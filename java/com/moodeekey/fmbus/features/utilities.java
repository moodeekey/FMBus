package com.moodeekey.fmbus.features;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Class contains values and methods that are used by multiple classes
 */
public class utilities {

    Context mContext;

    //values for toast duration
    public static int durationLong = Toast.LENGTH_LONG;
    public static int durationShort = Toast.LENGTH_SHORT;

    // constructor
    public utilities(Context context){
        this.mContext = context;
    }
    /** Checks for network connection, returns boolean */
    public  boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
