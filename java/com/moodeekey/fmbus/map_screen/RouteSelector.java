package com.moodeekey.fmbus.map_screen;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;

import com.moodeekey.fmbus.R;
import com.moodeekey.fmbus.data_handling.DatabaseAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * Popup Selector Class.Activity that is used to select which routes are to be displayed
 */
public class RouteSelector extends Activity {

    MyCustomAdapter dataAdapter = null;
    private List<List<String>> recievedData;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set to be floating popup-like dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.map_route_selector_layout);
        //Generate list View from ArrayList
        displayListView();
        //assign onClick listener to a button
        checkButtonClick();

    }
    /** Fills containers with data from DB and inflates ListView */
    private void displayListView() {
        ArrayList<MapViewItem> routeList = new ArrayList<MapViewItem>();
        MapViewItem route;
        DatabaseAdapter db;
        db = new DatabaseAdapter(getApplicationContext());
        db.createDatabase();
        db.open();
        recievedData = db.getListOfRoutes();
        db.close();

        List<String> routeStringsList = recievedData.get(0);
        List<String> routeCodesList = recievedData.get(1);
        List<String> routeSelectionState = recievedData.get(2);

        //creating MapViewItems from obtained data
        boolean dummy;
        for (int i = 0;
             i < routeStringsList.size() && i < routeCodesList.size() && i < routeSelectionState.size();
             i++) {

            dummy = (Integer.parseInt(routeSelectionState.get(i)) != 0); //get boolean from integer value
            route = new MapViewItem(routeStringsList.get(i), routeCodesList.get(i), dummy);
            routeList.add(route);
        }
        //pass all routes to inflate adapter
        dataAdapter = new MyCustomAdapter(this, R.layout.mapview_item_layout, routeList);
        ListView listView = (ListView) findViewById(R.id.routeSelectorListView);
        // Assigning adapter to ListView
        listView.setAdapter(dataAdapter);
    }
    /** Assigns OnClick Listenter to a Button, which click event updates DB */
    private void checkButtonClick() {
        Button myButton = (Button) findViewById(R.id.saveSelected);//instantiate button
        myButton.getBackground().setColorFilter(0xFF4285F4, PorterDuff.Mode.MULTIPLY);
        myButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ArrayList<MapViewItem> routeList = dataAdapter.listOfEntries;

                DatabaseAdapter db = new DatabaseAdapter(getApplicationContext());
                db.createDatabase();
                db.open(true);//open writable DB
                db.updateData(routeList);//calling update method and pass all entries
                db.close();
            }
        });
    }
    /**
     * Private Custom ArrayAdapter. Inflates all views and checks for click events
     */
    private class MyCustomAdapter extends ArrayAdapter<MapViewItem> {
        private ArrayList<MapViewItem> listOfEntries;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<MapViewItem> adapterList) {
            super(context, textViewResourceId, adapterList);
            this.listOfEntries = new ArrayList<MapViewItem>();
            this.listOfEntries.addAll(adapterList); //to local scope
        }
        /** Handles items of listview */
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;//holds color circle and check box

            if (convertView == null) {
                LayoutInflater inflator = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                //each item is a view with layout
                convertView = inflator.inflate(R.layout.mapview_item_layout, null);
                //Filling soulless holder with stuffs
                holder = new ViewHolder();
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
                holder.colorBox = (ImageView) convertView.findViewById(R.id.color_box);
                convertView.setTag(holder);//tag used to mark selected holder
                //listen for click events on holder
                holder.checkBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        CheckBox cb = (CheckBox) view;
                        MapViewItem item = (MapViewItem) cb.getTag();
                        item.setSelected(cb.isChecked());//checkbox ticked
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            MapViewItem item = listOfEntries.get(position);
            String color_name = item.getColor();
            //getting color from resources by color name (just a route name)
            int my_color= getResources().getIdentifier(color_name,"color",getPackageName());
            //holder needs name and color
            holder.checkBox.setText(item.getName());
            holder.checkBox.setChecked(item.isSelected());
            holder.checkBox.setTag(item);
            //color is white by default, PorterDuff used to 'color' it
            holder.colorBox.setColorFilter(getResources().getColor(my_color), PorterDuff.Mode.MULTIPLY);
            return convertView;
        }
        /**
         * ViewHolder.
         */
        private class ViewHolder {
            ImageView colorBox;
            CheckBox checkBox;
        }
    }
}