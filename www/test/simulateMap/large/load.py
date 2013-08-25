#!/usr/bin/python2
import os

filedir = os.path.dirname(os.path.realpath(__file__))

import requests
import time

for line in open(filedir + '/tmp/presences.json'):

    line = line.strip()
    #print line

    headers = {'Content-Type': 'application/json'}
    r = requests.put('http://oemap.onextent.com:8080/presence', data=line, headers=headers)

    print r.status_code
    print r.text

