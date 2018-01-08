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

// Initial position of the map
const LAT = 42.500;
const LON = 12.500;
const ZOOM = 5;

var layer_mapnik;
var layer_deeps;
var layer_seamark;
var layer_pois;
var layer_grid;

const MAPNIK = 1;
const DEEPS = 2;
const SEAMARK = 3;
const POIS = 4;
const GRID = 5;

// Function for initialize the map
function init() {

	// Map object
	map = new OpenLayers.Map ("map", {
		controls:[
			new OpenLayers.Control.Navigation(),
			new OpenLayers.Control.ScaleLine()
		],
		projection: new OpenLayers.Projection("EPSG:900913"),
		displayProjection: new OpenLayers.Projection("EPSG:4326")
	});

	// Checking the zoom level
	map.isValidZoomLevel = function(zoomLevel) {
		if(zoomLevel != null) {
			return (zoomLevel <= 17);
		} else {
			return false;
		}
    }

    user_markers = new OpenLayers.Layer.Markers( "Markers" );

	// Mapnik (Base map)
	layer_mapnik = new OpenLayers.Layer.OSM("OpenStreetMap (Mapnik)");
	// Water Depth
	layer_deeps = new OpenLayers.Layer.WMS("deeps_gwc", "http://osm.franken.de:8080/geoserver/gwc/service/wms",{layers: "gebco_2014", format:"image/png"},{isBaseLayer: false, visibility: false});
	// Seamark
	layer_seamark = new OpenLayers.Layer.TMS ( "seamarks", "http://t1.openseamap.org/seamark/", { numZoomLevels: 18, type: 'png', getURL:getTileURL, isBaseLayer:false, displayOutsideMaxExtent:true , visibility: false});
	// POI-Layer for harbours
	layer_pois = new OpenLayers.Layer.Vector("pois", { projection: new OpenLayers.Projection("EPSG:4326"), visibility: false, displayOutsideMaxExtent:true});
	// Grid WGS
	layer_grid = new OpenLayers.Layer.GridWGS("coordinateGrid", {visibility: false, zoomUnits: zoomUnits});
	layer_deeps.setOpacity(0.8);
	layer_pois.setOpacity(0.8);
	// Add layers on the base map
	map.addLayers([user_markers, layer_mapnik, layer_deeps, layer_seamark, layer_pois, layer_grid]);

	var lonLat = new OpenLayers.LonLat(LON ,LAT)
	.transform(
		new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
		map.getProjectionObject() // to Spherical Mercator Projection
	);

	map.setCenter(lonLat, ZOOM);

}

// OpenSeaMap layer
function getTileURL(bounds) {
	var res = this.map.getResolution();
	var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
	var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
	var z = this.map.getZoom();
	var limit = Math.pow(2, z);

	if (y < 0 || y >= limit) {
		return null;
	} else {
		x = ((x % limit) + limit) % limit;
		url = this.url;
		path= z + "/" + x + "/" + y + "." + this.type;
	if (url instanceof Array) {
		url = this.selectUrl(path, url);
	}
		return url+path;
	}
}

function setLayerState(layer, boolean) {
	if(layer == MAPNIK)
		layer_mapnik.setVisibility(boolean);
	else if(layer == DEEPS)
		layer_deeps.setVisibility(boolean);
	else if(layer == SEAMARK)
		layer_seamark.setVisibility(boolean);
	else if(layer == POIS)
		layer_pois.setVisibility(boolean);
	else if(layer == GRID)
		layer_grid.setVisibility(boolean);
}

function setUserPosition(lat, lon, follow) {
    clearMap();

    // Add a new actual position mark
    var pos = new OpenLayers.LonLat(lon, lat) //
        .transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));
    this.user_markers.addMarker(new OpenLayers.Marker(pos));

    if(follow) {
        this.map.setCenter(pos, this.map.getZoom());
    }
}

function clearMap() {
    // I clean the map
    this.user_markers.clearMarkers();
}