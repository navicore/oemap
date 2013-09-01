#!/usr/bin/python2
import os
import json
import datetime

filedir = os.path.dirname(os.path.realpath(__file__))

import time
from itertools import izip

snippit = 'good afternoon'
space = 'big test'
ttl = 2

with open(filedir + '/../tmp/names.txt') as namefile, open(filedir + '/../tmp/points.txt') as pointfile:
    
    counter = 0
    for name, c in izip(namefile, pointfile):

        counter += 1
        name = name.strip()
        c = c.strip()

        if (name == None): continue
        if (c == None): continue
        coord = c.split(',')
        coord = [float(coord[0]), float(coord[1])]
        lat = coord[0]
        lon = coord[1]

        presence = {}
        presence['uid'] = str(counter) + '_1234567890_TEST'
        presence['label'] = name
        presence['snippit'] = snippit
        presence['ttl'] = ttl
        now = str(datetime.datetime.now())
        presence['time'] = now
        if ttl == 1:
            ttl_idx = 'short_ttl_start_time'
        elif ttl == 2:
            ttl_idx = 'medium_ttl_start_time'
        else:
            ttl_idx = 'long_ttl_start_time'
        presence[ttl_idx] = now
        presence['space'] = space
        presence['location'] = {}
        presence['location']['type'] = "Point"
        presence['location']['coordinates'] = [lon, lat]

        print json.JSONEncoder().encode(presence)

