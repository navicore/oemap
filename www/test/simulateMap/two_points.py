#!/usr/bin/python2

import requests
import time
import json
import sys

if len(sys.argv) < 2:
    raise Exception('no host')
host = sys.argv[1]

if len(sys.argv) > 2:
    port = sys.argv[2]
else:
    port = 8080

names = ['Mike Angelo','Ben Friedman']
coords = [[37.807509, -122.366881], [37.80751,-122.365743]]

for p in [line.strip() for line in open('points.txt')]:
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
  presence['snippit'] = 'having fun in CA'
  presence['ttl'] = 2
  now = int(round(time.time() * 1000))
  presence['time'] = now
  presence['space'] = 'geotest'
  presence['location'] = {}
  presence['location']['type'] = "Point"
  presence['location']['coordinates'] = [coords[i][1], coords[i][0]]

  print presence

  headers = {'Content-Type': 'application/json'}
  r = requests.put('http://' + host + ':' + str(port) + '/presence', data=json.dumps(presence), headers=headers)

  print r.status_code
  print r.text

