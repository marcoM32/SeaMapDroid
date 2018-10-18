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

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class LegendActivity extends AppCompatActivity {

    private static HashMap<LegendCategory, ArrayList<LegendItem>> hashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legend);
        ExpandableListView listView = (ExpandableListView) findViewById(android.R.id.list);
        BaseExpandableListAdapter adapter = new BaseExpandableListAdapter() {

            @Override
            public Object getGroup(int groupPosition) {
                return LegendCategory.values()[groupPosition];
            }

            @Override
            public int getGroupCount() {
                return LegendCategory.values().length;
            }

            @Override
            public long getGroupId(int groupPosition) {
                return hashMap.get(LegendCategory.values()[groupPosition]).hashCode();
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = LegendActivity.this.getLayoutInflater().inflate(R.layout.activity_legend_group, null);

                LegendCategory category = (LegendCategory) getGroup(groupPosition);
                ((CheckedTextView) convertView).setText(getString(category.getIdName()));
                ((CheckedTextView) convertView).setChecked(isExpanded);
                return convertView;
            }

            @Override
            public Object getChild(int groupPosition, int childPosition) {
                return hashMap.get(LegendCategory.values()[groupPosition]).get(childPosition);
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                return hashMap.get(LegendCategory.values()[groupPosition]).size();
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return hashMap.get(LegendCategory.values()[groupPosition]).get(childPosition).hashCode();
            }

            @Override
            public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                final LegendItem children = (LegendItem) getChild(groupPosition, childPosition);

                if (convertView == null)
                    convertView = LegendActivity.this.getLayoutInflater().inflate(R.layout.activity_legend_details, null);

                TextView textView = (TextView) convertView.findViewById(R.id.detailsTextView);
                textView.setCompoundDrawablesWithIntrinsicBounds(children.getIdIcon(), 0, 0, 0);
                textView.setText(getString(children.getIdName()));

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(LegendActivity.this, getString(children.getIdName()), Toast.LENGTH_SHORT).show();
                    }
                });
                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return Boolean.FALSE;
            }

            @Override
            public boolean hasStableIds() {
                return Boolean.FALSE;
            }
        };

        listView.setAdapter(adapter);

        // Back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(Boolean.TRUE);
    }

    /**
     * The enum represent the list category types
     */
    enum LegendCategory {

        HARBOUR(R.string.harbour), //
        SEAMARKS(R.string.seamarks), //
        LIGHTS(R.string.lights), //
        LOCK(R.string.lock);

        private final Integer idName;

        LegendCategory(Integer idName) {
            this.idName = idName;
        }

        public Integer getIdName() {
            return idName;
        }
    }

    /**
     * The representation of a list item
     */
    static class LegendItem {

        private final Integer idName;

        private final Integer idIcon;

        LegendItem(Integer idName, Integer idIcon) {
            this.idName = idName;
            this.idIcon = idIcon;
        }

        public Integer getIdName() {
            return idName;
        }

        public Integer getIdIcon() {
            return idIcon;
        }
    }

    /**
     * This static block populate the hash map with resources id's of the elements of the list
     */
    static {

        ArrayList<LegendItem> items = new ArrayList<>();
        items.add(new LegendItem(R.string.legend_harbour, R.drawable.legend_harbour));
        items.add(new LegendItem(R.string.legend_fishing_harbour, R.drawable.legend_fishing_harbour));
        items.add(new LegendItem(R.string.legend_marina, R.drawable.legend_marina));
        items.add(new LegendItem(R.string.legend_anchorage, R.drawable.legend_anchorage));
        items.add(new LegendItem(R.string.legend_pier, R.drawable.legend_pier));
        items.add(new LegendItem(R.string.legend_crane, R.drawable.legend_crane));
        items.add(new LegendItem(R.string.legend_slipway, R.drawable.legend_slipway));
        items.add(new LegendItem(R.string.legend_harbour_master, R.drawable.legend_harbour_master));
        items.add(new LegendItem(R.string.legend_waste_disposal, R.drawable.legend_waste_disposal));
        hashMap.put(LegendCategory.HARBOUR, items);

        items = new ArrayList<>();
        items.add(new LegendItem(R.string.legend_safe_water, R.drawable.legend_lateral_safe_water));
        items.add(new LegendItem(R.string.legend_starboard_mark, R.drawable.legend_lateral_green));
        items.add(new LegendItem(R.string.legend_lateral_port, R.drawable.legend_lateral_red));
        items.add(new LegendItem(R.string.legend_lateral_pref_starboard, R.drawable.legend_lateral_pref_starboard));
        items.add(new LegendItem(R.string.legend_lateral_pref_port, R.drawable.legend_lateral_pref_port));
        items.add(new LegendItem(R.string.legend_cardinal_north, R.drawable.legend_cardinal_north));
        items.add(new LegendItem(R.string.legend_cardinal_east, R.drawable.legend_cardinal_east));
        items.add(new LegendItem(R.string.legend_cardinal_south, R.drawable.legend_cardinal_south));
        items.add(new LegendItem(R.string.legend_cardinal_west, R.drawable.legend_cardinal_west));
        items.add(new LegendItem(R.string.legend_isolated_danger, R.drawable.legend_cardinal_single));
        items.add(new LegendItem(R.string.legend_special_purpose, R.drawable.legend_special_purpose));
        hashMap.put(LegendCategory.SEAMARKS, items);

        items = new ArrayList<>();
        items.add(new LegendItem(R.string.legend_lighthouse, R.drawable.legend_lighthouse_major));
        items.add(new LegendItem(R.string.legend_beacon_green, R.drawable.legend_light_beacon_green));
        items.add(new LegendItem(R.string.legend_beacon_red, R.drawable.legend_light_beacon_red));
        items.add(new LegendItem(R.string.legend_beacon_white, R.drawable.legend_light_beacon_white));
        hashMap.put(LegendCategory.LIGHTS, items);

        items = new ArrayList<>();
        items.add(new LegendItem(R.string.legend_lock_gate, R.drawable.legend_lock_gate));
        items.add(new LegendItem(R.string.legend_lock, R.drawable.legend_lock));
        items.add(new LegendItem(R.string.legend_wier_small, R.drawable.legend_wier_small));
        items.add(new LegendItem(R.string.legend_wier_big, R.drawable.legend_wier_big));
        hashMap.put(LegendCategory.LOCK, items);

    }
}