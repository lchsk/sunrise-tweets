function increment(p_map, p_key)
{
  if (p_map[p_key] == undefined)
    p_map[p_key] = 1;
  else
    p_map[p_key]++;
}

// replaces tags with a value obtained from the tweet
function fillWorker(p_tweet, p_template, p_key, p_placeholder)
{
  var value = "";
  if (p_tweet[p_key] != undefined)
    value = p_tweet[p_key];

  return replaceAll(p_template, ":" + p_placeholder, value);
}

// utility function for fillWorker
function fill(p_tweet, p_template, p_key)
{
  return fillWorker(p_tweet, p_template, p_key, p_key);
}

function replaceAll(p_where, p_source, p_target)
{
  var re = new RegExp(p_source, 'g');

  return p_where.replace(re, p_target);
}

// removes all values beginning with ':' (used as tags)
function removeTags(p_template)
{
  var re = new RegExp(":[a-z_]+", 'g');

  return p_template.replace(re, '');
}
