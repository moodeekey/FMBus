package com.moodeekey.fmbus.saved_routes;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.moodeekey.fmbus.R;
import com.moodeekey.fmbus.data_handling.DatabaseAdapter;
import com.moodeekey.fmbus.data_handling.SpinnerDB;
import com.moodeekey.fmbus.features.SwipeDismissListViewTouchListener;
import com.moodeekey.fmbus.features.utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * SavedRoutes Activity, inflates listview(filled from SpinnersDB) on creation
 * */
public class SavedRoutesScreen extends ListActivity{

    //Arraylist of list items
    ArrayList<ListViewItem> listItems;
    //Arraylist of adapters
    ArrayAdapter<ListViewItem> adapter;

    ListView listView;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        //DB inconsistencies possible
        Toast.makeText(getApplicationContext(), "Errors Possible Please doublecheck with Schedule",
                utilities.durationShort).show();
    }
    public void onBackPressed() {
        //play animation
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_down, R.anim.slide_up);
    }
    /** Formats string to display  */
    private List<ListViewItem> enrichString(List<List<String>> str_list) {
        //check for day of the week
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);


        List<ListViewItem> result = new ArrayList<ListViewItem>();
        Resources res = getResources();
        List <String> item_ids = str_list.get(0);
        List <String> routes = str_list.get(1);
        List <String> stops = str_list.get(2);

        String text;

        for (int i = 0;i < routes.size() && i < stops.size();i++){
            if (dayOfWeek != 1) {

                text = String.format(res.getString(R.string.saved_route),
                        routes.get(i), stops.get(i),
                        getNextArrivalTime(routes.get(i), stops.get(i),dayOfWeek));
            }

            else{
                text = "No bus service on Sundays";
            }

            ListViewItem thing = new ListViewItem(Integer.parseInt(item_ids.get(i)),text);
            result.add(thing);

            //Integer.parseInt(getNextArrivalTime(routes.get(i), stops.get(i)).substring(0, 1))
        }
        return result;
    }

    /** Gets time of the next bus of particular Route/Stop instance */
    private String getNextArrivalTime(String route, String stop, int day) {

        String result;
        Long nextArrivalTime;
        DatabaseAdapter db;
        db = new DatabaseAdapter(getApplicationContext());
        db.createDatabase();
        db.open();
        String routeNumber = db.getRouteNumberByString(route);
        db.close();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String currentTime = sdf.format(new Date());

        String input = "Jan 1 1970 " + currentTime; //time from epoch
       Log.d("TIME", input);
        Date date = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd yyyy hh:mm a");
            date = simpleDateFormat.parse(input);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        long milliseconds = date.getTime();
       Log.d("TIME NOW ", Long.toString(milliseconds));

        switch (day) {
            case 7:
                db = new DatabaseAdapter(getApplicationContext());
                db.createDatabase();
                db.open();
                nextArrivalTime = db.getWeekendTime(routeNumber, stop, milliseconds);
                db.close();
                break;

            default:
                db = new DatabaseAdapter(getApplicationContext());
                db.createDatabase();
                db.open();
                nextArrivalTime = db.getWeekdayTime(routeNumber, stop, milliseconds);
                db.close();
                break;
        }
        DateFormat formatter;
        if (nextArrivalTime != 0L) {
            date = new Date(nextArrivalTime);
            formatter = new SimpleDateFormat("hh:mm aa");
            result = " at " + formatter.format(date);
        } else {
            result = " tommorow \n No more bus service today";
        }
        return result;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.saved_routes_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_add_route:
                Intent intent=new Intent(getApplicationContext(),AddRoutePopup.class);
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_in,0);
                return true;
            case R.id.action_clear_starred://deletes all entries
                SpinnerDB db = new SpinnerDB(
                        getApplicationContext());
                db.deleteAllEntries(db.getWritableDatabase());//e,pty bd itself
                String text = "All entries have been removed";
                Toast.makeText(getApplicationContext(),text,utilities.durationShort).show();
                //restarting to update listview
                finish();
                startActivity(getIntent());
                return true;

            case android.R.id.home:
                //for correct animation
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onPause(){
        super.onPause();
    }
    public void onResume(){
        //called when RouteSelector is closed,thus assuring listview is updated
        super.onResume();
        SpinnerDB db;
        db = new SpinnerDB(getApplicationContext());
        List<List<String>> entries = db.getAllEntries();

        listItems = new ArrayList<ListViewItem>(enrichString(entries));//get displayable strings
        setContentView(R.layout.saved_routes_screen_layout);
        adapter=new ArrayAdapter<ListViewItem>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);

        listView = getListView();
        //Google goody
        // Create a ListView-specific touch listener. ListViews are given special treatment because
        // by default they handle touches for their list items... i.e. they're in charge of drawing
        // the pressed state (the list selector), handling list item clicks, etc.
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        listView,
                        new SwipeDismissListViewTouchListener.OnDismissCallback() {
                            /**
                             * Called to determine whether the given position can be dismissed.
                             *
                             * @param position
                             */
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                SpinnerDB db;
                                db = new SpinnerDB(getApplicationContext());
                                for (int position : reverseSortedPositions) {
                                    ListViewItem ad = adapter.getItem(position);
                                    adapter.remove(adapter.getItem(position));
                                    db.deleteSingleEntry(ad.getItemId());
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener(touchListener.makeScrollListener());
    }
}