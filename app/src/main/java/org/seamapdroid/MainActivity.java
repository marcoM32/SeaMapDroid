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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private WebView aWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aWebView = (WebView) findViewById(R.id.aWebView);
        aWebView.getSettings().setJavaScriptEnabled(true);

        if (((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null) {
            aWebView.loadUrl("file:///android_asset/index.html");

            aWebView.setWebViewClient(new WebViewClient() {

                public void onPageFinished(WebView view, String url) {
                    loadPreferances();
                }
            });
        } else {
            Toast.makeText(MainActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        loadPreferances();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_preferences:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_source:
                Intent aBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/marcoM32/SeaMapDroid"));
                startActivity(aBrowserIntent);
                return true;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadPreferances() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        aWebView.loadUrl("javascript:setLayerState(MAPNIK," + preferences.getBoolean(SettingsActivity.MAPNIK, true) + ");");
        aWebView.loadUrl("javascript:setLayerState(DEEPS," + preferences.getBoolean(SettingsActivity.DEEPS, false) + ");");
        aWebView.loadUrl("javascript:setLayerState(SEAMARK," + preferences.getBoolean(SettingsActivity.SEAMARK, false) + ");");
        aWebView.loadUrl("javascript:setLayerState(POIS," + preferences.getBoolean(SettingsActivity.POIS, false) + ");");
        aWebView.loadUrl("javascript:setLayerState(GRID," + preferences.getBoolean(SettingsActivity.GRID, false) + ");");
    }
}
