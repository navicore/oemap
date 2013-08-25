#!/usr/bin/python2
import os

filedir = os.path.dirname(os.path.realpath(__file__))

import requests
import time
import json

names = [line.strip().rstrip(' \xc2\xa0') for line in open(filedir + '/../tmp/names.txt')]
coords = []

#for p in [line.strip() for line in open(filedir + '/tmp/points.txt')]:
for line in open(filedir + '/../tmp/points.txt'):
    coord = line.split(',')
    coord = [float(coord[0]), float(coord[1])]
    coords.append(coord)

size = len(coords)
if (len(names) < len(coords)): 
    size = len(names)

#size = 10 #ejs test #ejs test #ejs test #ejs test

#print "creating %s records" % size

for i in range(size):
  presence = {}
  presence['uid'] = '1234567890_TEST_' + str(i)
  presence['label'] = names[i]
  presence['snippit'] = 'having fun in CA'
  presence['ttl'] = 2
  now = int(round(time.time() * 1000))
  presence['time'] = now
  presence['space'] = 'big test'
  presence['location'] = {}
  presence['location']['type'] = "Point"
  presence['location']['coordinates'] = [coords[i][1], coords[i][0]]

  print presence

  #headers = {'Content-Type': 'application/json'}
  #r = requests.put('http://oemap.onextent.com:8080/presence', data=json.dumps(presence), headers=headers)

  #print r.status_code
  #print r.text

