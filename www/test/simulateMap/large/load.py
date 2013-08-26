#!/usr/bin/python2
import requests
import os
import sys
import datetime

startTime = datetime.datetime.now()
statC = 0
statI = 0
def stats():
    global statC, statI, startTime
    statC = statC + 1
    statI = statI + 1
    if statI == 1000:
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

if len(sys.argv) < 2:
    raise Exception('no host')
host = sys.argv[1]

if len(sys.argv) > 2:
    port = sys.argv[2]
else:
    port = 8080

filedir = os.path.dirname(os.path.realpath(__file__))

serverAddr = 'http://' + host + ':' + str(port) + '/presence'

for line in open(filedir + '/tmp/presences.json'):

    line = line.strip()
    #print line

    headers = {'Content-Type': 'application/json'}
    r = requests.put(serverAddr, data=line, headers=headers)

    #print "%s %s" % (r.status_code, r.text)
    stats()

