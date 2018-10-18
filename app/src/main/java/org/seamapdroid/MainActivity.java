/*
 * SeaMapDroid - An Android application to consult the libre online maps OpenSeaMap (map.openseamap.org)
 * Copyright (C) 2017 Marco Magliano
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.seamapdroid;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.redinput.compassview.CompassView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private final String MAP_PAGE_URL = "file:///android_asset/index.html";

    private final String ERROR_PAGE_URL = "file:///android_asset/error.html";

    private WebView aWebView;

    private LocationListener locationListener;

    private LocationManager locationManager;

    private FloatingActionButton floatingActionButton;

    private CompassView compass;

    private Boolean recording;

    private Boolean gpsState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recording = Boolean.FALSE;
        gpsState = Boolean.FALSE;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        aWebView = (WebView) findViewById(R.id.aWebView);
        aWebView.getSettings().setJavaScriptEnabled(Boolean.TRUE);
        aWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        if (isNetworkAvailable()) {
            aWebView.loadUrl(MAP_PAGE_URL);
        } else {
            aWebView.loadUrl(ERROR_PAGE_URL);
            Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_LONG).show();
        }

        aWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (MAP_PAGE_URL.equals(url))
                    loadPreferances();
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            registerReceiver(new NetworkBroadcast(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            registerReceiver(new LocationBroadcast(), new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                aWebView.loadUrl("javascript:setUserPosition(" + location.getLatitude() + "," + location.getLongitude() + //
                        "," + preferences.getBoolean(SettingsActivity.CENTER_MAP, Boolean.FALSE) + //
                        "," + preferences.getBoolean(SettingsActivity.TRACE_ROUTE, Boolean.FALSE) + ");");

                if(compass != null && location.hasBearing())
                    compass.setDegrees(location.getBearing());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(getApplicationContext(), R.string.gps_ready, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                disconnectGPS();
                Toast.makeText(getApplicationContext(), R.string.gps_not_available, Toast.LENGTH_LONG).show();
            }
        };

        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            gpsState = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gpsState) {
                    if (!recording) {
                        if (locationManager != null) {
                            if (checkPermission()) {
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
                                recording = Boolean.TRUE;
                                Toast.makeText(getApplicationContext(), R.string.gps_on, Toast.LENGTH_LONG).show();
                            } else {
                                requirePermission();
                            }
                        }
                    } else {
                        disconnectGPS();
                        Toast.makeText(getApplicationContext(), R.string.gps_off, Toast.LENGTH_LONG).show();
                    }
                } else {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }
        });

        compass = (CompassView) findViewById(R.id.compass);

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, //
                toolbar, R.string.about, R.string.about);
        actionBarDrawerToggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_search:
                        if (isNetworkAvailable()) {
                            searchCities();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_LONG).show();
                        }
                        break;
                    case R.id.nav_legend:
                        startActivity(new Intent(MainActivity.this, LegendActivity.class));
                        break;
                    case R.id.nav_quit:
                        disconnectGPS();
                        Process.killProcess(Process.myPid());
                        break;
                }

                drawerLayout.closeDrawers();
                return Boolean.TRUE;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (aWebView.getUrl().equals(MAP_PAGE_URL)) {
            aWebView.loadUrl("javascript:clearAllMap()");
            loadPreferances();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnectGPS();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return Boolean.TRUE;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_preferences:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return Boolean.TRUE;
            case R.id.action_source:
                Intent aBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/marcoM32/SeaMapDroid"));
                startActivity(aBrowserIntent);
                return Boolean.TRUE;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return Boolean.TRUE;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method disconnect the GPS listener
     */
    private void disconnectGPS() {
        if (locationManager != null) {
            if (aWebView.getUrl().equals(MAP_PAGE_URL)) {
                aWebView.loadUrl("javascript:clearUserMarker()");
                aWebView.loadUrl("javascript:clearUserTrace()");
                aWebView.loadUrl("javascript:clearUserTracePoints()");
            }
            locationManager.removeUpdates(locationListener);
            recording = Boolean.FALSE;
        }
        resetCompass();
    }

    /**
     * This method reset the compass degrees to 0
     */
    private void resetCompass() {
        if(compass != null) {
            compass.setDegrees(0);
        }
    }

    /**
     * This method displays an alert dialog to allow the user to search a city by the OSM api
     */
    private void searchCities() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.search);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        input.setSingleLine(Boolean.TRUE);

        alert.setView(input);
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String result = input.getText().toString();
                if (!result.isEmpty()) {
                    SearchTask searchTask = new SearchTask(result);
                    searchTask.execute();
                } else {
                    Toast.makeText(MainActivity.this, R.string.wrong_query, //
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                closeContextMenu();
            }
        });

        alert.show();
    }

    /**
     * The method load the user preferences in the map; is called
     * every time that the layers are turned on and off.
     */
    public void loadPreferances() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        aWebView.loadUrl("javascript:setLayerState(MAPNIK," + preferences.getBoolean(SettingsActivity.MAPNIK, Boolean.TRUE) + ");");
        aWebView.loadUrl("javascript:setLayerState(DEEPS," + preferences.getBoolean(SettingsActivity.DEEPS, Boolean.FALSE) + ");");
        aWebView.loadUrl("javascript:setLayerState(SEAMARK," + preferences.getBoolean(SettingsActivity.SEAMARK, Boolean.FALSE) + ");");
        aWebView.loadUrl("javascript:setLayerState(POIS," + preferences.getBoolean(SettingsActivity.POIS, Boolean.FALSE) + ");");
        aWebView.loadUrl("javascript:setLayerState(GRID," + preferences.getBoolean(SettingsActivity.GRID, Boolean.FALSE) + ");");
    }

    /**
     * The method check if the app permissions is
     * all granted
     */
    public Boolean checkPermission() {
        return (Build.VERSION.SDK_INT >= 23 && //
                ContextCompat.checkSelfPermission(getApplicationContext(), //
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * The method check if the app permissions is
     * all granted otherwise it requires them
     */
    public void requirePermission() {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(MainActivity.this, //
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 256);
        }
    }

    /**
     * The method check if the device is connected on internet
     *
     * @return True if the network is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Receiver class on connectivity service state
     */
    class NetworkBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkAvailable()) {
                aWebView.loadUrl(MAP_PAGE_URL);
            } else {
                aWebView.loadUrl(ERROR_PAGE_URL);
                gpsState = Boolean.FALSE;
                disconnectGPS();
            }
        }
    }

    /**
     * Receiver class on location service state
     */
    class LocationBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsState = Boolean.TRUE;
            } else {
                gpsState = Boolean.FALSE;
                disconnectGPS();
            }
        }
    }

    /**
     * This class provides a simple AsyncTask to query the OSM api
     */
    class SearchTask extends AsyncTask<String, String, Class<Void>> {

        private String query;

        public SearchTask(String query) {
            this.query = query;
        }

        @Override
        protected Class<Void> doInBackground(String... params) {
            HttpsURLConnection urlConnection = null;
            try {
                URL url = new URL("https://nominatim.openstreetmap.org/search/" + query + "?format=json&limit=1");
                urlConnection = (HttpsURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    StringBuilder stringBuilder = new StringBuilder();

                    BufferedReader bufferedReader = null;
                    try {
                        InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                        bufferedReader = new BufferedReader(inputStreamReader);

                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line + "\n");
                        }
                    } catch (RuntimeException ex) {
                        ex.printStackTrace();
                    } finally {
                        if (bufferedReader != null)
                            bufferedReader.close();
                    }

                    JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                    if (jsonArray.length() == 1) {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        final Double latitude = jsonObject.getDouble("lat");
                        final Double longitude = jsonObject.getDouble("lon");
                        aWebView.post(new Runnable() {
                            @Override
                            public void run() {
                                aWebView.loadUrl("javascript:setPoiPosition(" + latitude + "," + longitude + ");");
                            }
                        });
                    } else {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.no_result_found, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return Void.class;
        }
    }
}
