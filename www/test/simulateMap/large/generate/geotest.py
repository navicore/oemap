#!/usr/bin/python2
import math

R = 6378.1  #Radius of the Earth
brng = 1.57 #Bearing is 90 degrees converted to radians.
d = .1       #New Distance in km

coords = []

for p in [line.strip() for line in open('points.txt')]:
    coord = p.split(',')
    coord = [float(coord[0]), float(coord[1])]
    coords.append(coord)

for c in coords:
    lat1 = math.radians(c[0]) #Current lat point converted to radians
    lon1 = math.radians(c[1]) #Current long point converted to radians

    lat2 = math.asin( math.sin(lat1)*math.cos(d/R) +
                 math.cos(lat1)*math.sin(d/R)*math.cos(brng))

    lon2 = lon1 + math.atan2(math.sin(brng)*math.sin(d/R)*math.cos(lat1),
                         math.cos(d/R)-math.sin(lat1)*math.sin(lat2))

    lat2 = math.ceil(math.degrees(lat2) * 1000000) / 1000000
    lon2 = math.ceil(math.degrees(lon2) * 1000000) / 1000000
    
    #print "old lat: %s lon: %s new lat: %s lon: %s" % (c[0], c[1], lat2, lon2)
    print "coords = [[%s, %s], [%s,%s]]" % (c[0], c[1], lat2, lon2)
    #print "%s,%s" % (lat2, lon2)

