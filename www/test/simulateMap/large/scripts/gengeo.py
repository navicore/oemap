#!/usr/bin/python2
import math
import os

filepath = os.path.dirname(os.path.realpath(__file__))

R = 6378.1  #Radius of the Earth
#brng_90 = 1.57 #Bearing is 90 degrees converted to radians.

brngs = [0.79, 0.61, 0.17, 3.14, 3.32, 3.67, 5.23, 2.62, 0.26, 1.05]

dists = [.1, .2, .5, .5, .7, .8, .9, 2, 6, 20]       #New Distance in km

coords = []

for z in [line.strip() for line in open(filepath + '/zipcodes.csv')]:
    rec = z.split(',')
    lat = rec[3]
    lat = lat.replace('\"','')
    lon = rec[4]
    lon = lon.replace('\"','')
    coord = [float(lat), float(lon)]
    coords.append(coord)

for c in coords:
    
    print "%s,%s" % (c[0], c[1]) # old coord

    for dist in dists:
        for brng in brngs:
            lat1 = math.radians(c[0]) #Current lat point converted to radians
            lon1 = math.radians(c[1]) #Current long point converted to radians

            lat2 = math.asin( math.sin(lat1)*math.cos(dist/R) +
                math.cos(lat1)*math.sin(dist/R)*math.cos(brng))

            lon2 = lon1 + math.atan2(math.sin(brng)*
                    math.sin(dist/R)*math.cos(lat1),
                    math.cos(dist/R)-math.sin(lat1)*math.sin(lat2))

            lat2 = math.ceil(math.degrees(lat2) * 1000000) / 1000000
            lon2 = math.ceil(math.degrees(lon2) * 1000000) / 1000000
    
            #print "coords = [[%s, %s], [%s,%s]]" % (c[0], c[1], lat2, lon2)
            print "%s,%s" % (lat2, lon2) # new coord

