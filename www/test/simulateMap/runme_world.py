#!/usr/bin/python2

import requests
import time
import json

names = [line.strip().rstrip(' \xc2\xa0') for line in open('names.txt')]
coords = []

for p in [line.strip() for line in open('points_world.txt')]:
    coord = p.split(',')
    coord = [float(coord[0]), float(coord[1])]
    coords.append(coord)

size = len(coords)
if (len(names) < len(coords)): 
    size = len(names)

print "sending %s records" % size

for i in range(size):
  presence = {}
  presence['uid'] = '1234567890_TEST_' + str(i)
  presence['label'] = names[i]
  presence['snippit'] = 'having a world of fun?'
  presence['ttl'] = 2
  now = int(round(time.time() * 1000))
  presence['time'] = now
  presence['space'] = 'test photowalk'
  presence['location'] = {}
  presence['location']['type'] = "Point"
  presence['location']['coordinates'] = [coords[i][1], coords[i][0]]

  print presence

  headers = {'Content-Type': 'application/json'}
  r = requests.put('http://oemap.onextent.com:8080/presence', data=json.dumps(presence), headers=headers)

  print r.status_code
  print r.text

