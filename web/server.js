var express = require('express');
var _ = require('underscore');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io').listen(server);
var mongo = require('mongodb').MongoClient, format = require('util').format;

var GEO_NO_LOCATION = 0;
var GEO_COORDINATES = 1;
var GEO_TWEET_CONTENT = 2;
var GEO_USER_PROFILE = 3;
var GEO_IMAGE_COORDINATES = 4;

// settings
db_host = 'localhost';
db_port = 27017;
db_name = 'sunrise';
db_collection = 'tweets';
server_port = 3000;

server.listen(process.env.PORT || server_port);
app.set('view engine', 'ejs');
app.set('view options', { layout: false });
app.use(express.methodOverride());
app.use(express.bodyParser());
app.use(app.router);
app.use('/static', express.static('static'));

app.get('/', function (req, res) {
  res.render('index');
});

app.get('/about', function (req, res){
  res.render('about');
});

io.sockets.on('connection', function(socket){

	socket.on('init', function(data){

    mongo.connect('mongodb://' + db_host + ':' + db_port + '/' + db_name, function(err, db) {
      if(err)
        throw err;

      var collection = db.collection(db_collection);

      var condition = {
        created_at:
        { $gt: new Date(data.from),
          $lt: new Date(data.to)
        }
      };

      var geo_types = [];

      if (data.tweet_geolocation)
        geo_types.push(GEO_COORDINATES);

      if (data.analysis_geolocation)
      {
        geo_types.push(GEO_TWEET_CONTENT);
        geo_types.push(GEO_USER_PROFILE);
      }

      if (data.no_location)
        geo_types.push(GEO_NO_LOCATION);

      // if (data.photos_only)
      {
        condition['entities.media.0.media_url'] = { $exists: data.photos_only }
      }

      if (geo_types.length > 0)
        condition['sunrise_geo_type'] = {'$in': geo_types};

      collection.find(condition). toArray(function(err, results) {
        socket.emit('tweets', results);
        db.close();
      });
    });
	});
});