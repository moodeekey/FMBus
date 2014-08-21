package com.moodeekey.fmbus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.moodeekey.fmbus.data_handling.DatabaseAdapter;
import com.moodeekey.fmbus.features.SimpleEula;
import com.moodeekey.fmbus.features.utilities;
import com.moodeekey.fmbus.map_screen.MapScreen;
import com.moodeekey.fmbus.saved_routes.SavedRoutesScreen;
import com.moodeekey.fmbus.schedule_screen.ScheduleScreen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainScreen extends ActionBarActivity implements View.OnClickListener {

    SharedPreferences prefs;

    ImageButton button_schedule;
    TextView label_schedule;
    ImageButton button_map;
    TextView label_map;
    ImageButton button_saved;
    TextView label_saved
            ;
    Animation anim_fade_in;
    Animation anim_fade_out;
    Animation anim_disappear;


    private ProgressDialog DownloadProgress;

    public static String DB_PATH = "";
    public static String DB_NAME ="bus_schedule_db.sqlite";




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        prefs = getSharedPreferences("SETTINGS", 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_layout);

        anim_fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        anim_fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        anim_disappear =AnimationUtils.loadAnimation(this, R.anim.disappear);
        anim_disappear.setFillAfter(true);

        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/computer_love.ttf");

        button_schedule = (ImageButton) findViewById(R.id.button_schedule);
        button_schedule.setOnClickListener(this);
        button_schedule.startAnimation(anim_fade_in);

        label_schedule = (TextView) findViewById(R.id.label_schedule);
        label_schedule.setTypeface(type);
        label_schedule.startAnimation(anim_disappear);

        button_map = (ImageButton) findViewById(R.id.button_map);
        button_map.setOnClickListener(this);
        button_map.startAnimation(anim_fade_in);

        label_map = (TextView) findViewById(R.id.label_map);
        label_map.setTypeface(type);
        label_map.startAnimation(anim_disappear);


        button_saved = (ImageButton) findViewById(R.id.button_saved);
        button_saved.setOnClickListener(this);
        button_saved.startAnimation(anim_fade_in);

        label_saved = (TextView) findViewById(R.id.label_saved);
        label_saved.setTypeface(type);
        label_saved.startAnimation(anim_disappear);

        new SimpleEula(this).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {

        case R.id.action_update:
            startDownload();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {


        view.startAnimation(anim_fade_out);

        if (prefs.getBoolean("IS_UPDATED", false)) {

            switch (view.getId()) {
                case R.id.button_schedule:

                    Intent intent_schedule = new Intent(this, ScheduleScreen.class);
                    startActivity(intent_schedule);
                    overridePendingTransition(R.anim.slide_down, R.anim.slide_up);
                    break;

                case R.id.button_saved:

                    File file = new File(DB_PATH + DB_NAME);


                    Intent intent_saved = new Intent(this, SavedRoutesScreen.class);
                    startActivity(intent_saved);
                    overridePendingTransition(R.anim.slide_down, R.anim.slide_up);
                    break;

                case R.id.button_map:

                    Intent intent_map = new Intent(this, MapScreen.class);
                    startActivity(intent_map);
                    overridePendingTransition(R.anim.slide_down, R.anim.slide_up);
                    break;
            }
        } else {
            Context context = getApplicationContext();
            Toast.makeText(context, R.string.db_error, utilities.durationShort).show();


        }
    }

        private void startDownload() {

            Context context = getApplicationContext();

            //called to create an empty database which will be owerwritten

            DatabaseAdapter db;
            db = new DatabaseAdapter(context);
            db.createDatabase();
            db.open();
            db.close();


            utilities utils = new utilities(getApplicationContext());

            if(utils.isNetworkAvailable()) {


                if (android.os.Build.VERSION.SDK_INT >= 17) {

                    DB_PATH = getApplicationContext().getApplicationInfo().dataDir + "/databases/";
                } else {
                    DB_PATH = "/data/data/" + getApplicationContext().getPackageName() + "/databases/";
                }

                String url = "url to db";//link to database

                try {
                    new DownloadDatabase(getApplicationContext()).execute(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else {
                Toast.makeText(context, R.string.network_error, utilities.durationLong).show();
            }
         }

    private class DownloadDatabase extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadDatabase(Context context) throws IOException {
            this.context = context;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();

            DownloadProgress = new ProgressDialog(MainScreen.this);
            DownloadProgress.setMessage("Updating");
            DownloadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            DownloadProgress.setCancelable(false);
            DownloadProgress.show();
        }

        @Override
        protected String doInBackground(String... args) {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(args[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Toast.makeText(context,"Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage(),utilities.durationLong).show();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(DB_PATH + DB_NAME);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            DownloadProgress.setIndeterminate(false);
            DownloadProgress.setMax(100);
            DownloadProgress.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            DownloadProgress.dismiss();
            if (result != null)
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("IS_UPDATED",true);
                editor.apply();
        }
    }
}



