#!/usr/bin/python2
"""read presence from redis and write to push services, gcm, apple"""

from pymongo import MongoClient
import syslog
import requests
import redis
import json
import time
import datetime
from argparse import ArgumentParser

GCM_SERVER_URL = 'https://android.googleapis.com/gcm/send'

HEADERS = {'Content-Type': 'application/json',
           'Authorization': 'key=AIzaSyDY9ur-b23xZFfpKIkmlJHbW_HEB84mA20'}

INQNAME = "oemap_push_worker_in_queue"

class PushWorker():
    
    def __init__ (self):

        parser = ArgumentParser()
        parser.add_argument('-n', '--job', dest='job', action='store', 
                help='worker instance id')
        self.args = parser.parse_args()
        self.rhost = "127.0.0.1"
        self.rport = 6379
        
    def push_gcm(self, rec, rid_list):
        self.log_debug( "ejs got sendable rid %s" % ( rec['rid']))
        payload = {
                  "registration_ids": rid_list,
                  "delay_while_idle": False,
                  "data": rec,
                  "restricted_package_name": "com.onextent.oemap"
        }

        R = requests.post(GCM_SERVER_URL, data=json.dumps(payload), headers=HEADERS)
        self.log_debug("%s %s" % (R.status_code, R.text))

    def handle_gcm(self, rec):

        rid_list = [] 
        for p in self.db.presences.find(
                { 'space': 'santa cruz photowalk',
                    'rtp': 1 }):
                    if p['rid'] == rec['rid']: # skip self
                        continue
                    else:
                        rid_list.append(p['rid'])
        if rid_list:
            self.push_gcm(rec, rid_list)

    def handle_apple(self, rec):
        pass

    def handle(self, rec):
        spacename = rec['space']
        if 'rtp' in rec:
            remote_id_type = int(rec['rtp'])
            if remote_id_type == 1:
                self.handle_gcm(rec)
            elif remote_id_type == 2:
                self.handle_apple(rec)
            else:
                pass
            
    def run (self):

        while True:
            try:
                self.log_notice('%s Python impl starting queue %s' % ("test", INQNAME))
    
                rdis = redis.Redis(host=self.rhost, port=self.rport)
                client = MongoClient()
                self.db = client.oemap_test
    
                while True:
    
                    (_, msg) = rdis.brpop(keys=[INQNAME], timeout=600)
    
                    if msg == None: 
                        continue
                    
                    rec = json.loads(msg)

                    self.handle(rec)

            except Exception:
                self.handle_exception()
                time.sleep(1)
            except: # catch *all* exceptions
                self.handle_exception()
                time.sleep(1)
    
    def log_debug (self, msg):
        syslog.syslog(syslog.LOG_DEBUG, "%s %s" % (self.args.job, msg))

    def log_notice (self, msg):
        syslog.syslog(syslog.LOG_NOTICE, "%s %s" % (self.args.job, msg))

    def log_error (self, msg):
        syslog.syslog(syslog.LOG_ERR, "%s %s" % (self.args.job, msg))

    def handle_exception(self):
        import traceback
        formatted_lines = traceback.format_exc().splitlines()
        for line in formatted_lines:
            self.log_error(line)


        # get all presence recs for spacename
        # if remote_id_type is gcm, gcm push
        # if remote_id_type is apple, apple push
        # else noop

if __name__ == "__main__":

    PushWorker().run()

