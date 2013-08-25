#!/usr/bin/python2
import os
import json

filedir = os.path.dirname(os.path.realpath(__file__))

import requests
import time
import json
from itertools import izip

with open(filedir + '/../tmp/names.txt') as namefile, open(filedir + '/../tmp/points.txt') as pointfile:
    
    counter = 0
    for name, c in izip(namefile, pointfile):

	counter += 1

        if (name == None): continue
        if (c == None): continue
        coord = c.split(',')
        coord = [float(coord[0]), float(coord[1])]

        presence = {}
        presence['uid'] = '1234567890_TEST_' + str(counter)
        presence['label'] = name
        presence['snippit'] = 'having fun in CA'
        presence['ttl'] = 2
        now = int(round(time.time() * 1000))
        presence['time'] = now
        presence['space'] = 'big test'
        presence['location'] = {}
        presence['location']['type'] = "Point"
        presence['location']['coordinates'] = coord

        print json.JSONEncoder().encode(presence)

