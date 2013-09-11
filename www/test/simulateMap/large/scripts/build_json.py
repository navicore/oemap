#!/usr/bin/python2
import os
import json
import datetime
import pytz
from itertools import izip

MY_DIR = os.path.dirname(os.path.realpath(__file__))
NAMES_FILE = MY_DIR + '/../tmp/names.txt'
POINTS_FILE = MY_DIR + '/../tmp/points.txt'


SNIPPIT = 'Photowalking'
SPACE = "2e8e9773-973e-418f-bc16-6bc96ee75089"
TTL = 2

def utc_now():
    return datetime.datetime.now(
            pytz.timezone('UTC')).strftime('%Y-%m-%dT%H:%M:%S.%fZ')


# pylint: disable=C0321
with open(NAMES_FILE) as namefile, open(POINTS_FILE) as pointfile:
# pylint: enable=C0321
    
    # pylint: disable=C0103
    counter = 0
    # pylint: enable=C0103
    for name, c in izip(namefile, pointfile):

        counter += 1
        name = name.strip()
        c = c.strip()

        if (name == None): 
            continue
        if (c == None): 
            continue
        coord = c.split(',')
        coord = [float(coord[0]), float(coord[1])]
        lat = coord[0]
        lon = coord[1]

        presence = {}
        presence['uid'] = str(counter) + '_1234567890_TEST'
        presence['label'] = name
        presence['snippit'] = SNIPPIT
        presence['ttl'] = TTL
        
        now = utc_now()

        presence['time'] = now
        if TTL == 1:
            ttl_idx = 'short_ttl_start_time'
        elif TTL == 2:
            ttl_idx = 'medium_ttl_start_time'
        else:
            ttl_idx = 'long_ttl_start_time'
        presence[ttl_idx] = now
        presence['space'] = SPACE
        presence['location'] = {}
        presence['location']['type'] = "Point"
        presence['location']['coordinates'] = [lon, lat]

        print json.JSONEncoder().encode(presence)

