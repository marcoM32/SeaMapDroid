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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final String MAP_PAGE_URL = "file:///android_asset/index.html";

    private final String ERROR_PAGE_URL = "file:///android_asset/error.html";

    private WebView aWebView;

    private LocationListener locationListener;

    private LocationManager locationManager;

    private FloatingActionButton floatingActionButton;

    private Boolean recording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        recording = Boolean.FALSE;

        aWebView = (WebView) findViewById(R.id.aWebView);
        aWebView.getSettings().setJavaScriptEnabled(Boolean.TRUE);
        aWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        aWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (aWebView.getUrl().equals(MAP_PAGE_URL))
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
                        "," + preferences.getBoolean(SettingsActivity.CENTER_MAP, Boolean.FALSE) + ");");
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
                if (aWebView.getUrl().equals(MAP_PAGE_URL))
                    aWebView.loadUrl("javascript:clearMap()");

                if (locationManager != null) {
                    locationManager.removeUpdates(locationListener);
                    recording = Boolean.FALSE;
                }

                Toast.makeText(getApplicationContext(), R.string.gps_not_available, Toast.LENGTH_LONG).show();
            }
        };

        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setEnabled(Boolean.FALSE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            floatingActionButton.setEnabled(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recording) {
                    if (locationManager != null) {
                        if (Build.VERSION.SDK_INT >= 23 && //
                                ContextCompat.checkSelfPermission(getApplicationContext(), //
                                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
                            recording = Boolean.TRUE;
                            Toast.makeText(getApplicationContext(), R.string.gps_on, Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    if (locationManager != null) {
                        if (aWebView.getUrl().equals(MAP_PAGE_URL))
                            aWebView.loadUrl("javascript:clearMap()");
                        locationManager.removeUpdates(locationListener);
                        recording = Boolean.FALSE;

                        Toast.makeText(getApplicationContext(), R.string.gps_off, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            aWebView.loadUrl(MAP_PAGE_URL);
        } else {
            aWebView.loadUrl(ERROR_PAGE_URL);
            Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_LONG).show();
        }

        checkPermission();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (aWebView.getUrl().equals(MAP_PAGE_URL)) {
            aWebView.loadUrl("javascript:clearMap()");
            loadPreferances();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
            recording = Boolean.FALSE;
        }
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
     * all granted otherwise it requires them
     */
    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 256);
        }
    }

    /**
     * Receiver class on connectivity service state
     */
    class NetworkBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                aWebView.loadUrl(MAP_PAGE_URL);
            } else {
                aWebView.loadUrl(ERROR_PAGE_URL);
                floatingActionButton.setEnabled(Boolean.FALSE);
                if (locationManager != null) {
                    locationManager.removeUpdates(locationListener);
                    recording = Boolean.FALSE;
                }
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
            if (locationManager != null && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
                floatingActionButton.setEnabled(Boolean.TRUE);
            } else {
                floatingActionButton.setEnabled(Boolean.FALSE);
                if (locationManager != null) {
                    locationManager.removeUpdates(locationListener);
                    recording = Boolean.FALSE;
                }
            }
        }
    }
}
