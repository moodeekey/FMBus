package com.moodeekey.fmbus.schedule_screen;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.moodeekey.fmbus.R;
import com.moodeekey.fmbus.data_handling.DatabaseAdapter;
import com.moodeekey.fmbus.features.utilities;
import com.moodeekey.fmbus.map_screen.MapScreen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * ScheduleScreen Activity Class, displays schedule and navigation drawer
 */
public class ScheduleScreen extends FragmentActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mRoutesTitles;

    MenuItem mUpdateButton;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_screen_layout);

        mTitle = mDrawerTitle = getTitle();

        DatabaseAdapter db;
        db = new DatabaseAdapter(getApplicationContext());
        db.createDatabase();
        db.open();

        List<String> route_list = db.getAllRouteStrings();//fills navigation menu with strings

        db.close();

        mRoutesTitles = route_list.toArray(new String[0]);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_element, mRoutesTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,
                 0,
                 0/* nav drawer image to replace 'Up' caret */

        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schedule_screen, menu);
        mUpdateButton = menu.findItem(R.id.action_update);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_show_route).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_show_route:
                Intent intent_map = new Intent(this, MapScreen.class);
                startActivity(intent_map);
                overridePendingTransition(R.anim.slide_down, R.anim.slide_up);
                return true;
            case R.id.action_update:
                startDownload();//updating images
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return true;
    }
    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    private void selectItem(int position) {
        // update the main content by replacing fragments


        Fragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putInt(ImageFragment.ARG_ROUTE_NUMBER, position);
        fragment.setArguments(args);

        android.app.FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mRoutesTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_down, R.anim.slide_up);
    }

    public void startDownload() {
        Context context = getApplicationContext();

        utilities utils = new utilities(getApplicationContext());

        if (utils.isNetworkAvailable()) {

            DownloadImages task = new DownloadImages(this);

            DatabaseAdapter db;
            db = new DatabaseAdapter(getApplicationContext());
            db.createDatabase();
            db.open();

            List<String> route_list = db.getAllRouteCodes();

            db.close();

            String[] route_array = route_list.toArray(new String[0]);

            task.execute(route_array);

            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("In progress...");
            progressDialog.setMessage("Downloading");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setIcon(R.drawable.ic_action_download_dark);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
        else
            Toast.makeText(context, R.string.network_error, utilities.durationLong).show();

    }

    /**
     * Downloads images and stores them on local storage
     */
    private class DownloadImages extends AsyncTask<String, Integer, String> {
        private Activity context;
        int file_count;
        private String input_path;
        private String output_path;
        int current_number = 0;
        private PowerManager.WakeLock mWakeLock;


        public DownloadImages(Activity context) {
            this.context = context;

            input_path = "url to db"+getDeviceDensity(this.context)+"/";
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/.fmbus");
            if(folder.exists() && folder.isDirectory()) {
                //skip
            }else
                folder.mkdirs();
            //Log.d("FILE",folder.getAbsolutePath());

            output_path = folder.toString()+"/";
        }

        private String getDeviceDensity(Context context) {
            float density = context.getResources().getDisplayMetrics().density;
            /*if (density >= 4.0) { //no pictures available in such density
                return "xxxhdpi";
            }
            */
            if (density >= 3.0) {
                return "xxhdpi";
            }
            if (density >= 2.0) {
                return "xhdpi";
            }
            if (density >= 1.5) {
                return "hdpi";
            }
            if (density >= 1.0) {
                return "mdpi";
            }
            return "ldpi";
        }

        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();

            mUpdateButton.setEnabled(false);
        }

        protected String doInBackground(String... urls) {
           file_count = urls.length;



            for (String url : urls) {
                downloadImage(url);

            }
          return null;
        }

        private void downloadImage(String urlString) {

            int count = 0;

            URL url;
            InputStream inputStream = null;
            BufferedOutputStream outputStream = null;

            try {
                current_number ++;
                url = new URL(input_path + urlString + ".jpg");
                //Log.d("WEBFILE",url.getPath());
                URLConnection connection = url.openConnection();

                int lengthOfFile = connection.getContentLength();

                inputStream = new BufferedInputStream(url.openStream());

                OutputStream output = new FileOutputStream(
                        output_path + urlString + ".jpg");


                outputStream = new BufferedOutputStream(output);

                byte data[] = new byte[512];
                long total = 0;

                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    /*publishing progress update on UI thread.
                    Invokes onProgressUpdate()*/
                    publishProgress((int)((total*100)/lengthOfFile));

                    // writing data to byte array stream
                    outputStream.write(data, 0, count);
                }
                outputStream.flush();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                try {
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0]);

                progressDialog.setMessage("Downloading " +
                        Integer.toString(current_number) + "/" +
                        Integer.toString(file_count));
            }


        protected void onPostExecute(String result) {
            mWakeLock.release();
            progressDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"Schedules downloaded", Toast.LENGTH_SHORT).show();
            mUpdateButton.setEnabled(true);
        }

        }

    }




