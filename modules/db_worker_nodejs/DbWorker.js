/*jslint nomen: true, node: true */
"use strict";

var FIVE_MINUTES = 1000 * 60 * 5,
    SHORT_IN_MILLIS = FIVE_MINUTES,
    ONE_HOUR = 12 * FIVE_MINUTES,
    MEDIUM_IN_MILLIS = ONE_HOUR,
    ONE_DAY = 24 * ONE_HOUR,
    LONG_IN_MILLIS = ONE_DAY;

var redis = require('redis'),
    workerId = 'unknown',
    Syslog = require('node-syslog'),
    starttime = new Date(),
    stati = 0,
    statc = 0,
    MongoClient = require('mongodb').MongoClient;

process.argv.forEach(function (val, index, array) {
    if (index === 2) { //ejs todo: fix this idiotic hack with proper arg processing
        workerId = val;
    Syslog.init(workerId, Syslog.LOG_PID | Syslog.LOG_ODELAY, Syslog.LOG_LOCAL0);

    MongoClient.connect('mongodb://localhost:27017/oemap_test?auto_reconnect=true', function (err, db) {

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
        
        function setExpTime(doc) {

            var now = Date.now();
            var ttl = doc.ttl;

            switch (ttl) {
                case 0: //remove presence
                    Syslog.log(Syslog.LOG_DEBUG, 'setting ttl to zero');
                    break;
                case 1:
                    Syslog.log(Syslog.LOG_DEBUG, 'setting short ttl');
                    doc.exp_time = new Date(now + SHORT_IN_MILLIS);
                    break;
                case 2:
                    Syslog.log(Syslog.LOG_DEBUG, 'setting medium ttl');
                    doc.exp_time = new Date(now + MEDIUM_IN_MILLIS);
                    break;
                case 3:
                    Syslog.log(Syslog.LOG_DEBUG, 'setting medium ttl');
                    doc.exp_time = new Date(now + LONG_IN_MILLIS);
                    break;
                default:
                    Syslog.log(Syslog.LOG_WARNING, 
                            'invalid ttl. setting ttl to zero');
                    doc.exp_time = new Date();
                    break;
            }
        }

        function save(doc) {

            setExpTime(doc);

            db.collection('presences').save(doc,
                function (err, count) {
                    if (err) {
                        Syslog.log(Syslog.LOG_ERR, "upsert error: " + err);
                        //todo: handle err
                    }
                    if (!count) {
                        Syslog.log(Syslog.LOG_WARNING, "no docs upserted");
                    } else {
                        Syslog.log(Syslog.LOG_DEBUG, 'pid: "' + doc._id + '" for ' + doc.label + ' updated. ttl: ' + doc.ttl);
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

        }
});

