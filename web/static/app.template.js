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
