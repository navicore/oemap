#!/usr/bin/python2
import os
import json
from pymongo import MongoClient
import datetime

FIVE_MIN_IN_SECS = 60 * 5
ONE_HOUR_IN_SECS = 60 * 60
ONE_DAY_IN_SECS = ONE_HOUR_IN_SECS * 24

def setExpireTime(rec):
    now = datetime.datetime.now()
    ttl = rec['ttl']
    if ttl == 1:
        rec['exp_time'] = now + datetime.timedelta(0, FIVE_MIN_IN_SECS)
    elif ttl == 2:
        rec['exp_time'] = now + datetime.timedelta(0, ONE_HOUR_IN_SECS)
    elif ttl == 3:
        rec['exp_time'] = now + datetime.timedelta(0, ONE_DAY_IN_SECS)
    else:
        rec['exp_time'] = now  # ready for sweeper

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
            r = "10000+ per second"
        print "processed %s records. rate was %s." % (statC, r)
        statI = 0
        startTime = now

filedir = os.path.dirname(os.path.realpath(__file__))

client = MongoClient()
db = client.oemap_test

for line in open(filedir + '/tmp/presences.json'):

    line = line.strip()

    rec = json.JSONDecoder().decode(line)
    setExpireTime(rec)
    try:
        db.presences.save(rec)
        stats()
    except Exception as e:
        print e 

