#!/usr/bin/python2
import os

filedir = os.path.dirname(os.path.realpath(__file__))

import requests
import time

#serverAddr = 'http://oemap.onextent.com:8080/presence'
serverAddr = 'http://localhost:8080/presence'

for line in open(filedir + '/tmp/presences.json'):

    line = line.strip()
    #print line

    headers = {'Content-Type': 'application/json'}
    r = requests.put(serverAddr, data=line, headers=headers)

    print r.status_code
    print r.text

