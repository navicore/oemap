/*jslint nomen: true, node: true */
"use strict";

var redis = require('redis'),
    workerId = 'unknown',
    Syslog = require('node-syslog'),
    starttime = new Date(),
    stati = 0,
    statc = 0,
    MongoClient = require('mongodb').MongoClient;

process.argv.forEach(function (val, index, array) {
    console.log(index + ': ' + val);
    if (index === 2) {
        workerId = val;
    }
    Syslog.init(workerId, Syslog.LOG_PID | Syslog.LOG_ODELAY, Syslog.LOG_LOCAL0);

    MongoClient.connect('mongodb://localhost:27017/oemap_test', function (err, db) {

        if (err) {
            Syslog.log(Syslog.LOG_ERR, "mongodb error", err);
            //throw err;
        }

        Syslog.log(Syslog.LOG_INFO, "db worker nodejs impl created mongodb connection");

        var rclient = redis.createClient();
        Syslog.log(Syslog.LOG_INFO, "db worker nodejs impl created redis client");

        function stats() {
            stati += 1;
            statc += 1;
            if (stati >= 10000) {
                var now = new Date(),
                    dur = now.getTime() - starttime.getTime(),
                    secs = 0,
                    rate = 0;
                if (dur > 1000) {
                    secs = dur / 1000;
                    rate = Number((stati / secs).toFixed(0)) + " per second";
                } else {
                    rate = '10000+ per second';
                }

                Syslog.log(Syslog.LOG_DEBUG, "processed " + statc + " records. rate was " + rate);
                stati = 0;
                starttime = new Date();
            }
        }

        function save(doc) {
            db.collection('presences').save(doc,
                function (err, count) {
                    if (err) {
                        Syslog.log(Syslog.LOG_ERR, "upsert error: " + err);
                        //todo: handle err
                    }
                    if (!count) {
                        Syslog.log(Syslog.LOG_WARNING, "no docs upserted");
                    } else {
                        Syslog.log(Syslog.LOG_DEBUG, 'pid: "' + doc._id + '" for ' + doc.label + ' updated');
                        stats();
                    }
                }
                );
        }

        function lrpop() {
            rclient.brpop('oemap_db_worker_in_queue', 0,
                function (err, data) {
                    save(JSON.parse(data[1]));

                    setTimeout(
                        function () { lrpop(); },
                        0
                    );
                }
                );
        }
        lrpop();
    });

});

