/*jslint nomen: true, node: true */
"use strict";

var express = require('express'),
    app = express(),
    cons = require('consolidate'),
    crypto = require('crypto'),
    redis = require('redis'),
    Syslog = require('node-syslog'),
    MongoClient = require('mongodb').MongoClient;

Syslog.init("oemapd", Syslog.LOG_PID | Syslog.LOG_ODELAY, Syslog.LOG_LOCAL0);

app.engine('html', cons.swig);
app.set('view engine', 'html');
app.set('views', __dirname + '/views');
app.configure(function () {
    app.use(express.bodyParser());
});


MongoClient.connect('mongodb://localhost:27017/oemap_test', function (err, db) {

    if (err) {
        Syslog.log(Syslog.LOG_ERR, "mongodb error", err);
        //throw err;
    }

    Syslog.log(Syslog.LOG_INFO, "created mongodb connection");


    var rclient = redis.createClient();
    Syslog.log(Syslog.LOG_INFO, "created redis client");

    //return a list of presences for the space
    app.get('/presence', function (req, res) {

        var spc = req.query.space,
            lat = req.query.lat,
            lon = req.query.lon,
            count = req.query.max,
            dist = req.query.dist,
            query = {"space": spc},
            geoq = {};

        if (lat) {
            lat = parseFloat(lat);
        }
        if (lon) {
            lon = parseFloat(lon);
        }
        if (!count) {
            count = 100;
        } else {
            count = parseInt(count, 10);
        }
        if (!dist) {
            dist = 1609 * 120;
        } else {
            dist = parseInt(dist, 10);
        }

        if (!spc) {
            res.statusCode = 400;
            return res.send('Error 400: No space requested');
        }

        if (lat && lon) {
            geoq = {
                $near: {
                    $geometry: {
                        type: "Point",
                        coordinates: [lon, lat]
                    },
                    $maxDistance: dist
                }
            };
            query.location = geoq;
        }

        db.collection('presences').find(query).limit(count).toArray(
            function (err, doc) {
                if (err) {
                    throw err;
                }

                if (!doc || doc.length < 1) {
                    res.statusCode = 404;
                    return res.send('Error 404: No presences found');
                }
                Syslog.log(Syslog.LOG_DEBUG, 'get got ' + doc.length +
                    ' presences for ' + spc + ' near ' + lat + "/" +
                    lon + " within " + dist + " meters");

                res.statusCode = 200;
                return res.json(doc);
            }
        );
        return;
    });

    //put a presence
    app.put('/presence', function (req, res) {

        if (!req.body) {
            Syslog.log(Syslog.LOG_DEBUG, "put missing req.body");
            res.statusCode = 400;
            return res.send('Error 400: No presence in put');
        }

        var pid = req.body.uid + '_' + req.body.space,
            ttl = req.body.ttl,
            label = req.body.label,
            now = new Date();

        req.body._id = pid;

        if (ttl === 0) {
            Syslog.log(Syslog.LOG_DEBUG, 'ttl expired for pid: ' +
                pid + ' ' + req.body.label);
            db.collection('presences').remove(
                function (err, doc) {
                    if (err) {
                        throw err;
                    }
                    //todo: handle err
                    //todo: make pid uid and overwrite map dupes
                }
            );
        } else {
            switch (ttl) {
            case 0: //remove presence
                break;
            case 1:
                req.body.short_ttl_start_time = now;
                break;
            case 2:
                req.body.medium_ttl_start_time = now;
                break;
            case 3:
                req.body.long_ttl_start_time = now;
                break;
            default:
                req.body.medium_ttl_start_time = now;
                break;
            }

            //
            //todo: exception.  what happens if redis is down?
            //
            //rclient.lpush('oemap_db_worker_in_queue', JSON.stringify(req.body));
            rclient.lpush('oemap_db_worker_in_queue', JSON.stringify(req.body),
                function (err) {
                    if (err) {
                        Syslog.log(Syslog.LOG_WARNING, "lpush error: %s", err);
                    }
                });

            //db.collection('presences').save(req.body,
            //    function (err, doc) {
            //        if (err) {
            //            console.log('upsert error: ' + err);
            //            //todo: handle err
            //            throw err;
            //        }
            //        if (!doc) {
            //            console.log('warning: no records modified ');
            //        } else {
            //            console.log('pid: "' + pid + '" for ' + label + ' updated');
            //        }
            //    }
            //    );
        }
        res.send(200);
    });

    app.get('*', function (req, res) {
        res.statusCode = 404;
        return res.send('OeMap Page Not Found', 404);
    });

    app.listen(8080);
    Syslog.log(Syslog.LOG_INFO, 'server started on port 8080');
});

