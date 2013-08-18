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
        
        console.log('get presence')
        var space = req.query.space
                           
        if (!space)
            return res.send('Error 404: No space requested');
        //todo: get from mongo
        var m = ""

        if (!m)
            return res.send('Error 404: No space found');
        res.json(m);
    });

    //put a presence
    app.put('/presence', function(req, res) {

        console.log('put presence: ' + req.body)
        if (!req.body) {
            return res.send('Error 404: No presence in put');
        }
        db.collection('presences').insert(req.body, function(err, doc) {
            //todo: handle err
            //todo: make pid uid and overwrite map dupes
        })
        res.send(200);
    });

    app.get('*', function(req, res) {
        return res.send('OeMap Page Not Found', 404);
    });

    app.listen(8080);
    console.log('oemapd server started on port 8080');
});
