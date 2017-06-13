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
var lat=42.500;
var lon=12.500;
var zoom=5;

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

	// Mapnik (Base map)
	var mapnik = new OpenLayers.Layer.OSM("OpenStreetMap (Mapnik)");
	// Water Depth
	var deeps = new OpenLayers.Layer.WMS("deeps_gwc", "http://osm.franken.de:8080/geoserver/gwc/service/wms",{layers: "gebco_2014", format:"image/png"},{isBaseLayer: false, visibility: true});
	// Seamark
	var seamark = new OpenLayers.Layer.TMS ( "seamarks", "http://t1.openseamap.org/seamark/", { numZoomLevels: 18, type: 'png', getURL:getTileURL, isBaseLayer:false, displayOutsideMaxExtent:true });
	// POI-Layer for harbours
	var pois = new OpenLayers.Layer.Vector("pois", { projection: new OpenLayers.Projection("EPSG:4326"), visibility: true, displayOutsideMaxExtent:true});
	// Grid WGS
	var grid = new OpenLayers.Layer.GridWGS("coordinateGrid", {visibility: true, zoomUnits: zoomUnits});
	deeps.setOpacity(0.8);
	pois.setOpacity(0.8);
	// Add layers on the base map
	map.addLayers([mapnik, deeps, seamark, pois, grid]);

	var lonLat = new OpenLayers.LonLat(lon ,lat)
	.transform(
		new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
		map.getProjectionObject() // to Spherical Mercator Projection
	);

	map.setCenter (lonLat, zoom);

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
