#!/usr/bin/python2
import os
import json
import datetime
import pytz
from itertools import izip

MY_DIR = os.path.dirname(os.path.realpath(__file__))
NAMES_FILE = MY_DIR + '/../tmp/names.txt'
POINTS_FILE = MY_DIR + '/../tmp/points.txt'


SNIPPIT = 'More Photowalking'
SPACES = ["2e8e9773-973e-418f-bc16-6bc96ee75089", "photowalking", "bored", "hungry", "frisbee"]
TTL = 3

NEXT_SP_POS = 0
def nextSpace():
    global NEXT_SP_POS
    r = SPACES[NEXT_SP_POS]
    NEXT_SP_POS = NEXT_SP_POS + 1
    if NEXT_SP_POS == len(SPACES):
        NEXT_SP_POS = 0
    return r

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
        
        presence['space'] = nextSpace()
        presence['location'] = {}
        presence['location']['type'] = "Point"
        presence['location']['coordinates'] = [lon, lat]

        print json.JSONEncoder().encode(presence)

