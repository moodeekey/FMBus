package com.moodeekey.fmbus.map_screen;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.moodeekey.fmbus.R;
import com.moodeekey.fmbus.data_handling.DatabaseAdapter;
import com.moodeekey.fmbus.features.utilities;

import java.util.List;
/**
 * Google Maps activity, Shows routes (using encoded polylines) on the map
 *
 */
public class MapScreen extends Activity {
    //Fargo/Moorhead    LOCATION (default camera position)
   final double far_lat = 46.874979;
   final double far_lang = -96.789604;
    // Google Map
    private GoogleMap googleMap;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_screen_layout);
        try {
            // Loading map
            initilizeMap();
        } catch (Exception error) {
            //Listen kids, generic exceptions are bad.
            //but God knows what can happen...
            error.printStackTrace();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_screen, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_routes_display:
                Intent intent = new Intent(getApplicationContext(), RouteSelector.class);
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_in,0);
                return true;
            case R.id.action_center_at_fargo:
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(far_lat, far_lang), 12));
                return true;
            case android.R.id.home:
                //home != back, but to ensure correct animation we do it this way
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onDestroy() {
        super.onDestroy();
        //saving camera info and store them in shared prefs
        CameraPosition camera = googleMap.getCameraPosition();
        double longitude = camera.target.longitude;
        double latitude = camera.target.latitude;
        float zoom_lvl = camera.zoom;

        SharedPreferences settings = getSharedPreferences("SAVED_INSTANCE", 0);
        SharedPreferences.Editor editor = settings.edit();

        this.putDouble(editor, "longitude", longitude);
        this.putDouble(editor, "latitude", latitude);
        editor.putFloat("zoom_level", zoom_lvl);

        editor.commit();
    }
    /**
     * function to load map. If map is not created it will create it for you
     */
    private void initilizeMap() {
        SharedPreferences settings = getSharedPreferences("SAVED_INSTANCE", 0);
        double latitude = this.getDouble(settings, "latitude", far_lat);
        double longitude = this.getDouble(settings, "longitude", far_lang);
        float zoom = settings.getFloat("zoom_level", 12);

        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Unable to show map", utilities.durationShort)
                        .show();
            }
            googleMap.clear();
            googleMap.setMyLocationEnabled(true);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);//MAP_TYPE_NORMAL
                                                            //MAP_TYPE_HYBRID
                                                            //MAP_TYPE_SATELLITE
                                                            //MAP_TYPE_TERRAIN

            showMultipleRoutes();
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
    }
    /** Gets all routes from DB and display those that are selected by user in RouteSelector */
    public void showMultipleRoutes() {
        DatabaseAdapter db;
        db = new DatabaseAdapter(getApplicationContext());
        db.createDatabase();
        db.open();

        List<List<String>> polylineList = db.getAllSelectedRoutes();

        db.close();

        List<String> polylines = polylineList.get(0);
        List<String> colors = polylineList.get(1);


        for (int i = 0; i < polylines.size() && i < colors.size();i++) {
            String code = polylines.get(i);
            List<LatLng> decodedPath = PolyUtil.decode(code);//decoding polyline
            //getting integer color by route name
            int my_color = getResources().getIdentifier(colors.get(i), "color", getPackageName());
            googleMap.addPolyline(new PolylineOptions().
                    addAll(decodedPath).
                    color(getResources().getColor(my_color)));
        }
    }
    public void onPause(){
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        googleMap.clear();
        showMultipleRoutes();
    }
    public void onBackPressed() {
        super.onBackPressed();
        //trigger animation
        overridePendingTransition(R.anim.slide_down, R.anim.slide_up);
    }

    /** Used for easier putting of double to Shared prefs */
    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit,
                                       final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }
    /** Used for easier getting of double to Shared prefs */
    double getDouble(final SharedPreferences prefs, final String key,
                     final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }
}