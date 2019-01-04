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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        WebView aboutWebView = (WebView) findViewById(R.id.aboutWebView);
        aboutWebView.loadData(getString(R.string.about_content), "text/html", "UTF-8");
        aboutWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if ("https://www.gnu.org/licenses/gpl-3.0.html".equals(url)) {
                    displayLicense(getString(R.string.app_license));
                } else if("https://opensource.org/licenses/MIT".equals(url)) {
                    displayLicense(getString(R.string.logo_license));
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
                return Boolean.TRUE;
            }
        });

        // Back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(Boolean.TRUE);
        actionBar.setSubtitle("v" + BuildConfig.VERSION_NAME);
    }

    /**
     * Displays a license text in an AlertDialog.
     */
    private void displayLicense(String text) {
        View view = getLayoutInflater().inflate(R.layout.activity_about_licence, null);

        TextView textView = (TextView) view.findViewById(R.id.licenceTextView);
        textView.setText(Html.fromHtml(text));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setNeutralButton(R.string.ok, null);
        alert.setCancelable(Boolean.FALSE);
        alert.setView(view);
        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }
}
