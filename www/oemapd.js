var express = require('express'),
    app = express(),
    cons = require('consolidate'),
    crypto = require('crypto'),
    MongoClient = require('mongodb').MongoClient;

app.engine('html', cons.swig);
app.set('view engine', 'html');
app.set('views', __dirname + '/views');
app.configure(function(){
  app.use(express.bodyParser());
});

MongoClient.connect('mongodb://localhost:27017/oemap_test', function(err, db) {

    if(err) throw err;

    //return a list of presences for the space
    app.get('/presence', function(req, res) {
        
        var spc = req.query.space
        var lat = req.query.lat
	if (lat) {
            lat = parseFloat(lat)
        }
        var lon = req.query.lon
	if (lon) {
            lon = parseFloat(lon)
        }
        var count = req.query.max
	if (!count) {
            count = 100
        } else {
            count = parseInt(count, 10)
        }
        var dist = req.query.dist
	if (!dist) {
            dist = 1609 * 120
        } else {
            dist = parseInt(dist, 10)
        }
                           
        if (!spc) {
            res.statusCode = 400;
            return res.send('Error 400: No space requested');
        }
        console.log('get ' + count +' presences for ' + spc + ' near ' + lat + "/" + lon + " within " + dist + " meters")
      
        var query = {'space': spc} 

        if (lat && lon) {
             var geoq ={
                         $near: 
                           {
                             $geometry:
                               {
                                 type: "Point",
                                 coordinates: [lon,lat]
                               },
                             $maxDistance: dist
                           }
                       }
              query['location'] = geoq
        }

        db.collection('presences').find(query)
            .limit(count).toArray(function(err, doc) {
            if(err) throw err;

            if (!doc || doc.length < 1) {
                res.statusCode = 404;
                return res.send('Error 404: No presences found');
            }

            res.statusCode = 200;
            return res.json(doc);
        })
        return
    });

    //put a presence
    app.put('/presence', function(req, res) {

        var pid = req.body.uid + '_' + req.body.space
        req.body['_id'] = pid

        console.log('put presence pid: ' + pid + ' ' + req.body.label)
        if (!req.body) {
            res.statusCode = 400;
            return res.send('Error 400: No presence in put');
        }
        var ttl = req.body.ttl
        if (ttl == 0) {
          db.collection('presences').remove(
                //{"uid": req.body.uid, "space": req.body.space}, 
                {"_id": pid}, 
                function(err, doc) {
                    if(err) throw err;
                    //todo: handle err
                    //todo: make pid uid and overwrite map dupes
                })
        } else {
          var now = new Date()
          switch (ttl) {
            case 0: //remove presence
                break;
            case 1:
                req.body['short_ttl_start_time'] = now
                break;
            case 2:
                req.body['medium_ttl_start_time'] = now
                break;
            case 3:
                req.body['long_ttl_start_time'] = now
                break;
            default:
                req.body['medium_ttl_start_time'] = now
                break;
          }
          db.collection('presences').update(
                //{"uid": req.body.uid, "space": req.body.space}, 
                {"_id": pid}, 
                req.body, 
                {"upsert": true}, 
                function(err, doc) {
                    if(err) throw err;
                    //todo: handle err
                    //todo: make pid uid and overwrite map dupes
                }
          )
        }
        res.send(200);
    });

    app.get('*', function(req, res) {
        res.statusCode = 404;
        return res.send('OeMap Page Not Found', 404);
    });

    app.listen(8080);
    console.log('oemapd server started on port 8080');
});

