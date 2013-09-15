#!/usr/bin/python2
"""worker to read presence records from redis and write to mongodb"""

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

#ok_response = {'status': 'ok'}

INQNAME = "oemap_db_worker_in_queue"
REPLYTO = "oemap_www_nodejs_in_queue"

class DbWorker():
    
    def __init__ (self):

        parser = ArgumentParser()
        parser.add_argument('-n', '--job', dest='job', action='store', 
                help='worker instance id')
        self.args = parser.parse_args()
        self.rhost = "127.0.0.1"
        self.rport = 6379
        self.starttime = datetime.datetime.now()
        self.statc = 0
        self.stati = 0
        self.database = None
        
    def stats(self):
        self.statc = self.statc + 1
        self.stati = self.stati + 1
        if self.stati == 10000:
            now = datetime.datetime.now()
            dur = now - self.starttime
            rate = ''
            if dur.seconds > 0:
                rate = str(self.stati / dur.seconds) + " per second"
            else:
                rate = "1000+ per second"
            self.log_notice("processed %s records. rate was %s." % 
                    (self.statc, rate))
            self.stati = 0
            self.starttime = now
   
    FIVE_MIN_IN_SECS = 60 * 5
    ONE_HOUR_IN_SECS = 60 * 60
    ONE_DAY_IN_SECS = ONE_HOUR_IN_SECS * 24

    def setExpireTime(self, rec):
        now = datetime.datetime.now()
        ttl = rec['ttl']
        if ttl == 1:
            rec['exp_time'] = now + datetime.timedelta(0, FIVE_MIN_IN_SECS)
        elif ttl == 2:
            rec['exp_time'] = now + datetime.timedelta(0, ONE_HOUR_IN_SECS)
        elif ttl == 3:
            rec['exp_time'] = now + datetime.timedelta(0, ONE_DAY_IN_SECS)
        else:
            rec['exp_time'] = now  # ready for sweeper

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
                    
                    self.log_debug("updating %s for %s" % (rec['_id'], 
                        rec['label']))

                    self.setExpireTime(rec)
                    
                    self.database.presences.save(rec)
                    
                    self.stats()
                    
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

if __name__ == "__main__":

    DbWorker().run()

