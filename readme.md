
1. to import cities to mongo db:
  add this:
  geonameid	name	asciiname	alternatenames	latitude	longitude	feature class	feature code	country code	cc2	admin1 code	admin2 code	admin3 code	admin4 code	population	elevation	dem	timezone	modification date
  to the top of the line
  
  (from: http://download.geonames.org/export/dump/readme.txt)
  then, execute command:
    mongoimport -d sunrise -c cities --type tsv --file cities15000.txt --headerline

  Then run script:
  db.eval(function() { 
    db.cities.find({}).forEach(function(e) {
        e.loc = {type: "Point", coordinates: [e.longitude, e.latitude]};
        db.cities.save(e);
    });
});

and then
db.cities.ensureIndex({"loc": "2dsphere"})
for indexing.

from mongo shell in order to use geospatial capabilities of mongodb.