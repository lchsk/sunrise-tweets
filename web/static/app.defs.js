var countries_data = {
  'United Kingdom': [54, -2, 6],
  'United States': [40, -95, 4],
  'Poland': [52, 20, 6],
  'Europe': [53, 21, 4],
  'World': [23, 0, 2],
  'Australia': [-25, 133, 4],
  'The Caribbean': [16, -70, 5],
  'Italy': [41.5, 15, 6]
}

var GEO_NO_LOCATION = 0;
var GEO_COORDINATES = 1;
var GEO_TWEET_CONTENT = 2;
var GEO_USER_PROFILE = 3;
var GEO_IMAGE_COORDINATES = 4;

var MB_ATTR = 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
'<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
'Imagery © <a href="http://mapbox.com">Mapbox</a>';

var MB_URL = 'http://{s}.tiles.mapbox.com/v3/{id}/{z}/{x}/{y}.png';

var OSM_URL = 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
var OSM_ATTRIB = '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a> contributors';


var map;
var markers = new L.FeatureGroup();

var socket = io.connect('/');
