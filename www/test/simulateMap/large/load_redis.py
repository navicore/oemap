#!/usr/bin/python2
import os
import datetime
import redis
import json

outQName = "oemap_db_worker_in_queue"
inQName = "oemap_www_nodejs_in_queue"

rhost = "127.0.0.1"
rport = 6379
rdis = redis.Redis(host=rhost, port=rport)

startTime = datetime.datetime.now()
statC = 0
statI = 0
def stats():
    global statC, statI, startTime
    statC = statC + 1
    statI = statI + 1
    if statI == 10000:
        now = datetime.datetime.now()
        dur = now - startTime
        r = ''
        if dur.seconds > 0:
            r = str(statI / dur.seconds) + " per second"
        else:
            r = "1000+ per second"
        print "processed %s records. rate was %s." % (statC, r)
        statI = 0
        startTime = now

filedir = os.path.dirname(os.path.realpath(__file__))

for line in open(filedir + '/tmp/presences.json'):

    line = line.strip()
    rdis.lpush(outQName, json.dumps(line));
    (q, msg) = rdis.brpop(keys=[inQName], timeout=600);
    rec = json.loads(msg)
    #rec = json.loads(rec)
    if (rec['status'] != 'ok'): print rec
    stats()

