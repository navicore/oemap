#!/usr/bin/python2
import os
import json
from pymongo import MongoClient

filedir = os.path.dirname(os.path.realpath(__file__))

client = MongoClient()
db = client.oemap_test

for line in open(filedir + '/tmp/presences.json'):

    line = line.strip()

    rec = json.JSONDecoder().decode(line)
    try:
        db.presences.save(rec)
    except Exception as e:
        print e 

