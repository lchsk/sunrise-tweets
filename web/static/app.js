socket.on('connect', function() {
  socket.emit('init', name);

  initMap();
});

socket.on('tweets', function(data){

  var template = '<div class="tpl-tweet-list" style="background-color: white; width: 22%; float: left; margin: 8px;"><a class="small" href="http://twitter.com/:screen_name" data-placement="left" title=":description"><b>:name</b> (:screen_name):</a><br /> <small>:text</small><br /><small><i>:created_at</i></small><br /><small><i>sunrise</i> word found in :sunrise_language</small> <br /><small>Location: :city_name :country_name (:sunrise_geo_type)<br />City w/in 30 km, based on coordinates: :large_city</small><br />:button :show_on_map</div>';

  var popup_template = '<a class="small" href="http://twitter.com/:screen_name" title=":description"><b>:name</b> (:screen_name):</a><br /> <small>:text</small><br /><small><i>:created_at</i></small><br /><small><i>sunrise</i> word found in :sunrise_language</small><br /><small>Location: :city_name :country_name (:sunrise_geo_type)<br />City w/in 30 km, based on coordinates: :large_city</small><br />:button';

  var added = 0;

  calculate_stats(data);
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

function showView(p_context)
{
  refreshMap();
  var context = $(p_context).attr('context');
  $(".mode-view").hide();
  $("#" + context).fadeIn();

  $('.btn-mode').removeClass('active');
  $(p_context).addClass('active');
}

function show()
{
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
}

function initApp()
{
  showView($("#btn-map"));
  show();
}


// on load of page
$(function(){
    $('[data-toggle="tooltip"]').tooltip();
    $('#date-from').datetimepicker({
      defaultDate: "01/01/2014",
      showToday: true
    });
    $('#date-to').datetimepicker({
      defaultDate: "31/12/2015",
      showToday: true
    });

  initApp();

  $("#nav-live").click(function(){
    if ($(this).attr('data-state') == 'on')
    {
      // turn OFF
      $(this).attr('data-state', 'off');
      $(this).html('<span class="glyphicon glyphicon-play"></span> Start system');

      try
      {
        socket.emit('live_turn_off');
        $('#alerts').append('<div class="alert alert-info">System was stopped.</div>');
      }
      catch(err)
      {
        $('#alerts').append('<div class="alert alert-error">Error when stopping the system: ' + err + '</div>');
      }

      window.setTimeout(function(){
        $('#alerts .alert').fadeOut();
      }, 5000);
    }
    else
    {
      // turn ON
      $(this).attr('data-state', 'on');
      $(this).html('<span class="glyphicon glyphicon-pause"></span> Stop system');

      try
      {
        socket.emit('live_turn_on');
        $('#alerts').append('<div class="alert alert-info">System was started.</div>');
      }
      catch(err)
      {
        $('#alerts').append('<div class="alert alert-error">Error when starting the system: ' + err + '</div>');
      }

      window.setTimeout(function(){
        $('#alerts .alert').fadeOut();
      }, 5000);
    }
  });

  $("#navbar li").click(function(d){
    $("#navbar li").removeClass('active');
    $(this).addClass('active');
  });


  $(".btn-mode").click(function(){
    showView(this);
  });



  // onclick action
  // top navbar: show country button
  // shows map view and centers on a chosen country
  $(".nav-location").click(function(){
    var country = $(this).attr('country');
    var data = countries_data[country];
    map.setView([data[0], data[1]], data[2]);
    showView($("#btn-map"));
  });

  $("#list").on('click', '.btn-show-on-map', function(){
    var long = $(this).attr('data-long');
    var lat = $(this).attr('data-lat');
    map.setView([lat, long], 7);
    showView($("#btn-map"));
  });


  $("#btn-submit").click(function(){
    show();
  });


  $(".mode-view").on("click", ".btn-show-img", function(){
    var url = $(this).attr('modal-image');
    $("#modal-img #modal-image").attr('src', url);
    $("#modal-img #modal-image").attr('width', '90%');
    $('#modal-img').modal('toggle');
  });



});
