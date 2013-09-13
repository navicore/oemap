#!/usr/bin/python2
"""read presence from redis and write to push services, gcm, apple"""

from pymongo import MongoClient
import syslog
import redis
import json
import time
import datetime
from argparse import ArgumentParser

#from riemann import RiemannClient, RiemannUDPTransport

#rmmonitor = RiemannClient(transport = RiemannUDPTransport,
#host=config.riemann['host'])

INQNAME = "oemap_push_worker_in_queue"

class DbWorker():
    
    def __init__ (self):

        parser = ArgumentParser()
        parser.add_argument('-n', '--job', dest='job', action='store', 
                help='worker instance id')
        self.args = parser.parse_args()
        self.rhost = "127.0.0.1"
        self.rport = 6379
        
    def run (self):

        while True:
            try:
                self.log_notice('%s Python impl starting queue %s' % ("test", INQNAME))
    
                rdis = redis.Redis(host=self.rhost, port=self.rport)
                client = MongoClient()
                self.database = client.oemap_test
    
                while True:
    
                    (_, msg) = rdis.brpop(keys=[INQNAME], timeout=600)
    
                    if msg == None: 
                        continue
                    
                    rec = json.loads(msg)

                    push(rec)

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

    def push_gcm(self, rec):
        print "ejs sending presence to gcm"
        pass

    def push_apple(self, rec):
        pass

    def push(self, rec):
        spacename = rec['space']
        remote_id_type = int(rec['rtp'])
        if remote_id_type == 1:
            push_gcm(rec)
        elif remote_id_type == 2:
            push_apple(rec)
        else:
            pass
            

        # get all presence recs for spacename
        # if remote_id_type is gcm, gcm push
        # if remote_id_type is apple, apple push
        # else noop

if __name__ == "__main__":

    DbWorker().run()

