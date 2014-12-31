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
