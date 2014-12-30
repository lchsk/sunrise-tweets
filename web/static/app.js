var countries_data = {
  'United Kingdom': [54, -2, 6],
  'United States': [40, -95, 4],
  'Poland': [52, 20, 6],
  'Europe': [53, 21, 4],
  'World': [23, 0, 2],
  'United Kingdom': [54, -2, 6],
  'United Kingdom': [54, -2, 6]
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

socket.on('connect', function() {
  socket.emit('init', name);

  initMap();


});

function initMap()
{
  map = L.map('map').setView([23, 0], 2);
  L.tileLayer(MB_URL, {attribution: MB_ATTR, id: 'examples.map-i875mjb7'}).addTo(map);
  map.addLayer(markers);
}

function refreshMap()
{
  if (map != undefined)
    L.Util.requestAnimFrame(map.invalidateSize, map, false, map._container);
}

function fillWorker(p_tweet, p_template, p_key, p_placeholder)
{
  var value = "";
  if (p_tweet[p_key] != undefined)
    value = p_tweet[p_key];

  return replaceAll(p_template, ":" + p_placeholder, value);
}

function replaceAll(p_where, p_source, p_target)
{
  var re = new RegExp(p_source, 'g');

  return p_where.replace(re, p_target);
}

function fill(p_tweet, p_template, p_key)
{
  return fillWorker(p_tweet, p_template, p_key, p_key);
}

function applyTemplateLocation(p_template, p_d)
{
  if (p_d['sunrise_geo_type'] == GEO_COORDINATES)
  {
    p_template = replaceAll(p_template, ':sunrise_geo_type', "Based on tweet's coordinates");

    var place = p_d.place;
    var closest_city = p_d.closest_city;

    if (place != undefined)
    {
      p_template = fillWorker(place, p_template, 'name', 'city_name');
      p_template = fillWorker(p_d, p_template, 'country_full', 'country_name');
    }

    if (closest_city != undefined)
    {
      p_template = fillWorker(closest_city, p_template, 'name', 'large_city');
    }
  }
  else if (p_d['sunrise_geo_type'] == GEO_TWEET_CONTENT || p_d['sunrise_geo_type'] == GEO_USER_PROFILE)
  {
    p_template = replaceAll(p_template, ':sunrise_geo_type', "Based on tweet analysis");

    var place = p_d.sunrise_geo_identified;
    if (place != undefined)
    {
      p_template = fillWorker(place, p_template, 'name', 'city_name');
      p_template = fillWorker(place, p_template, 'country code', 'country_name');
    }
  }
  else if (p_d['sunrise_geo_type'] == GEO_NO_LOCATION)
  {
    p_template = replaceAll(p_template, ':sunrise_geo_type', "No location found");
  }


  return p_template;
}

function removeTags(p_template)
{
  var re = new RegExp(":[a-z_]+", 'g');

  return p_template.replace(re, '');
}

function applyTemplate(p_template, p_d)
{
  var t = fill(p_d, p_template, "text");

  if (p_d['user'] != undefined)
  {
    var user = p_d['user'];
    t = fill(user, t, "name");
    t = fill(user, t, "screen_name");
    t = fill(user, t, "description");
  }

  t = fill(p_d, t, "created_at");
  t = fill(p_d, t, "sunrise_language");
  // t = fill(p_d, t, "sunrise_geo_type");

  if (p_d['retweet_count'] > 0)
  {
    t = t.replace('white', '#E0FFFF');
  }
  if (p_d['entities']['media'] != undefined)
  {
    var url = p_d['entities']['media'][0]['media_url'];
    // var id_str = p_d['entities']['media'][0]['id_str'];
    t = t.replace(':button', "<br /><a style='color: white;' modal-image='" + url + "' class='btn btn-primary btn-sm btn-show-img' href='#'>Show photo</a>");
  }
  else
  {
    t = t.replace(':button', "");
  }

  t = applyTemplateLocation(t, p_d);

  t = removeTags(t);

  return t;
}

socket.on('tweets', function(data){

  var template = '<div style="background-color: white; width: 22%; float: left; margin: 8px;"><a class="small" href="http://twitter.com/:screen_name" data-placement="left" title=":description"><b>:name</b> (:screen_name):</a><br /> <small>:text</small><br /><small><i>:created_at</i></small><br /><small><i>sunrise</i> word found in :sunrise_language</small> <small>Location: :city_name :country_name (:sunrise_geo_type)<br />Closest large city: :large_city</small><br />:button</div>';

  var popup_template = '<a class="small" href="http://twitter.com/:screen_name" title=":description"><b>:name</b> (:screen_name):</a><br /> <small>:text</small><br /><small><i>:created_at</i></small><br /><small><i>sunrise</i> word found in :sunrise_language</small>:button :sunrise_geo_type';

  var added = 0;


  markers.clearLayers();


  for (var i = 0; i < data.length; i++)
  {
    if (data[i]['text'] == undefined) continue;
    if (data[i]['possibly_sensitive'] == true) continue;

    var data_box = applyTemplate(template, data[i]);
    var data_map = applyTemplate(popup_template, data[i]);

    try{
    if (data[i]['sunrise_geo_type'] == GEO_COORDINATES)
    {
      var coords = data[i]['coordinates']['coordinates'];
      if (coords != undefined)
      {
          var marker = L.marker([coords[1], coords[0]]);
          marker.bindPopup(data_map, {showOnMouseOver: true}).openPopup();
          markers.addLayer(marker);
      }
    }
    else if (data[i]['sunrise_geo_type'] == GEO_TWEET_CONTENT || data[i]['sunrise_geo_type'] == GEO_USER_PROFILE)
    {
      var place_data = data[i]['sunrise_geo_identified'];
      if (place_data != undefined)
      {
        var coords = [place_data.longitude, place_data.latitude];

        if (coords != undefined)
        {
          var marker = L.marker([coords[1], coords[0]]);
          marker.bindPopup(data_map, {showOnMouseOver: true}).openPopup();
          markers.addLayer(marker);
        }
      }
    }
  }
  catch(err)
{
  console.log('Error adding a tweet to the map: ' + err);
}

    $("#list").append(data_box);
    added++;
    if (added % 4 == 0)
      $("#list").append("<div class='clearfix'></div>");
  }
});


// on load of page
$(function(){
    $('[data-toggle="tooltip"]').tooltip();
    $('#date-from').datetimepicker({
      defaultDate: "01/01/2014"
    });
    $('#date-to').datetimepicker({
      defaultDate: "31/12/2015"
    });

  showView($("#btn-list"));

  $("#navbar li").click(function(d){
    $("#navbar li").removeClass('active');
    $(this).addClass('active');
  });

function showView(p_context)
{
  refreshMap();
  var context = $(p_context).attr('context');
  $(".mode-view").hide();
  $("#" + context).fadeIn();

  $('.btn-mode').removeClass('active');
  $(p_context).addClass('active');
}

  $(".btn-mode").click(function(){
    showView(this);
  });

  $(".nav-location").click(function(){
    var country = $(this).attr('country');
    var data = countries_data[country];
    map.setView([data[0], data[1]], data[2]);
    showView($("#btn-map"));
  });

  $("#btn-submit").click(function(){
    var photos_only = $("#form-photos-only").is(':checked');
    var tweet_geolocation = $("#form-tweet-coords").is(':checked');
    var analysis_geolocation = $("#form-analysis-coords").is(':checked');
    var no_location = $("#form-no-location").is(':checked');

    var date_from = $("#date-from .form-control");

    if (date_from != undefined)
      date_from = Date.parseExact(date_from.val(), 'dd/MM/yyyy HH:mm').getTime();

    var date_to = $("#date-to .form-control");
    if (date_to != undefined)
      date_to = Date.parseExact(date_to.val(), 'dd/MM/yyyy HH:mm').getTime();

    var data = {
      from: date_from,
      to: date_to,
      photos_only: photos_only,
      tweet_geolocation: tweet_geolocation,
      analysis_geolocation: analysis_geolocation,
      no_location: no_location
    };

    $("#list").empty();
    socket.emit('init', data);
  });


  $(".mode-view").on("click", ".btn-show-img", function(){
    var url = $(this).attr('modal-image');
    $("#modal-img #modal-image").attr('src', url);
    $("#modal-img #modal-image").attr('width', '90%');
    $('#modal-img').modal('toggle');
  });

});
