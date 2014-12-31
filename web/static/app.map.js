function initMap()
{
  // what is shown by default
  var world = countries_data['World'];
  map = L.map('map').setView([world[0], world[1]], world[2]);
  L.tileLayer(MB_URL, {attribution: MB_ATTR, id: 'examples.map-i875mjb7'}).addTo(map);
  map.addLayer(markers);
}

function refreshMap()
{
  if (map != undefined)
    L.Util.requestAnimFrame(map.invalidateSize, map, false, map._container);
}
