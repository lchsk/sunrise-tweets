function getBarChartData(p_map)
{
  var keys = [];
  var values = []
  for(var k in p_map)
  {
    keys.push(k);
    values.push(p_map[k]);
  }

  return {keys: keys, values: values};
}

function createCanvas(p_parent_id, p_canvas_id)
{
  $("#" + p_canvas_id).remove();

  var canv = document.createElement('canvas');
  canv.id = p_canvas_id;
  canv.height = 500;
  canv.width = $("#" + p_parent_id).width();
  document.getElementById(p_parent_id).appendChild(canv);
}

function drawBarChart(p_parent_id, p_canvas_id, p_map)
{
  var d = getBarChartData(p_map);
  var data = { labels: d.keys, datasets: [{ data: d.values }]};

  createCanvas(p_parent_id, p_canvas_id);
  var context = document.getElementById(p_canvas_id).getContext("2d");
  var chart = new Chart(context).Bar(data);
}

function calculate_stats(p_d)
{
  var countries = {};
  var tweet_languages = {};
  var sunrise_languages = {};
  var sunrise_geo_types = {};

  for (var i = 0; i < p_d.length; i++)
  {
    if (p_d[i]['country_full'] != undefined)
    {
      increment(countries, p_d[i]['country_full']);
    }

    if (p_d[i]['lang'] != undefined)
    {
      increment(tweet_languages, p_d[i]['lang']);
    }

    if (p_d[i]['sunrise_language'] != undefined)
    {
      increment(sunrise_languages, p_d[i]['sunrise_language']);
    }

    if (p_d[i]['sunrise_geo_type'] != undefined)
    {
      increment(sunrise_geo_types, p_d[i]['sunrise_geo_type']);
    }

  }

  drawBarChart('div-chart-countries', 'chart-countries', countries);
  drawBarChart('div-chart-t-langs', 'chart-t-langs', tweet_languages);
  drawBarChart('div-chart-s-langs', 'chart-s-langs', sunrise_languages);
  drawBarChart('div-chart-location', 'chart-location', sunrise_geo_types);
}
