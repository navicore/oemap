#!/usr/bin/python2
import os
import json
from pymongo import MongoClient
import datetime

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
    rec['_id'] = rec['uid'] + '_' + rec['space']
    try:
        db.presences.save(rec)
        stats()
    except Exception as e:
        print e 

