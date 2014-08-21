package com.moodeekey.fmbus.saved_routes;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.moodeekey.fmbus.R;
import com.moodeekey.fmbus.data_handling.DatabaseAdapter;
import com.moodeekey.fmbus.data_handling.SpinnerDB;
import com.moodeekey.fmbus.features.utilities;

import java.util.List;

public class AddRoutePopup extends Activity implements
        AdapterView.OnItemSelectedListener {
    // Route Spinner
    Spinner routeSpinner;
    // Stop Spinner
    Spinner stopSpinner;

    ImageButton okButton;

    Animation anim_fading;//button click animation

    protected void onCreate(Bundle savedInstanceState) {
// TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //floating window
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_route_popup_layout);
        // Spinner for routes
        routeSpinner = (Spinner) findViewById(R.id.spinner_route);
        // Spinner for stops
        stopSpinner = (Spinner) findViewById(R.id.spinner_stop);

        okButton = (ImageButton) findViewById(R.id.button_add_fav);
        anim_fading = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Spinner click listener
        routeSpinner.setOnItemSelectedListener(this);
        // Loading spinner data from database
        loadSpinnerData();

        //Entry Add Button Click listener
        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                okButton.startAnimation(anim_fading);//let user know it was actually clicked
                String selectedRoute = routeSpinner.getSelectedItem().toString();
                String selectedStop = stopSpinner.getSelectedItem().toString();
                //make sure something is in spinners
                if (selectedRoute.trim().length() > 0 && selectedStop.trim().length() > 0) {
                    // database handler
                    SpinnerDB db = new SpinnerDB(
                            getApplicationContext());
                    // adding new entry to DB
                    db.addSingleEntry(selectedRoute, selectedStop);
                    // Hiding the keyboard
                    InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mInputMethodManager.hideSoftInputFromWindow(routeSpinner.getWindowToken(), 0);

                } else {
                    Toast.makeText(getApplicationContext(), "Select a Route and a Stop",
                            utilities.durationShort).show();
                }
            }
        });
    }
    /**
     * Loads the spinner data from SQLite database
     */
    private void loadSpinnerData() {

        DatabaseAdapter db;
        db = new DatabaseAdapter(getApplicationContext());
        db.createDatabase();
        db.open();
        List<String> routeList = db.getAllRoutes();
        db.close();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, routeList);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//std
        // attaching data adapter to spinner
        routeSpinner.setAdapter(dataAdapter);
    }
    /**
     * Handles Selection of Route, loads Stops spinner with appropriate values
     */
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        // On selecting a spinner item
        String routeString = parent.getItemAtPosition(position).toString();

        DatabaseAdapter db;
        db = new DatabaseAdapter(getApplicationContext());
        db.createDatabase();
        db.open();
        String routeNumber = db.getRouteNumberByString(routeString);
        db.close();


        db = new DatabaseAdapter(getApplicationContext());
        db.createDatabase();
        db.open();
        List<String> stops = db.getAllStops(routeNumber);
        db.close();

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, stops);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        stopSpinner.setAdapter(dataAdapter);
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // if nothing selected we do nothing
    }
}

