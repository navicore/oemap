#!/usr/bin/python2

from pymongo import MongoClient
import sys
import syslog
import redis
import json
import time
import datetime
from argparse import ArgumentParser

#from riemann import RiemannClient, RiemannUDPTransport

#rmmonitor = RiemannClient(transport = RiemannUDPTransport, host=config.riemann['host'])

class DbWorker():


    def __init__ (self):

        parser = ArgumentParser()
        parser.add_argument('-n', '--job', dest='job', action='store', help='worker instance id')
        self.args = parser.parse_args()
        rhost = "127.0.0.1"
        rport = 6379
        self.rdis = redis.Redis(host=rhost, port=rport)
        self.inQName = "oemap_db_worker_in_queue"
        self.replyTo = "oemap_www_nodejs_in_queue"

        self.startTime = datetime.datetime.now()
        self.statC = 0
        self.statI = 0
        client = MongoClient()
        self.db = client.oemap_test

    def stats(self):
        self.statC = self.statC + 1
        self.statI = self.statI + 1
        if self.statI == 10000:
            now = datetime.datetime.now()
            dur = now - self.startTime
            r = ''
            if dur.seconds > 0:
                r = str(self.statI / dur.seconds) + " per second"
            else:
                r = "1000+ per second"
            self.logNotice("processed %s records. rate was %s." % (self.statC, r))
            self.statI = 0
            self.startTime = now
    
    def logNotice (self, msg):
            syslog.syslog(syslog.LOG_NOTICE, "%s %s" % (self.args.job, msg))

    def logErr (self, msg):
            syslog.syslog(syslog.LOG_ERR, "%s %s" % (self.args.job, msg))

    def run (self):
        self.logNotice('%s starting queue %s' % ("test", self.inQName))

        while True:

            response = {'status': 'ok'}

            try:
               
                (q, msg) = self.rdis.brpop(keys=[self.inQName], timeout=600);
                #self.logNotice('DbWorker got %s' % (str(msg)))

                #if msg == None: continue
                
                rec = json.loads(msg)
                rec = json.loads(rec)
                rec['_id'] = rec['uid'] + '_' + rec['space']
                try:
                    #self.logNotice('DbWorker updating pid %s' % (rec['_id']))
                    self.db.presences.save(rec)
                    self.stats()
                except Exception as e:
                    response = {'status': 'error', 'msg': str(e)}
                    self.logErr('mongodb exception %s' % (e))
                
                # reply to client
                self.rdis.lpush(self.replyTo, json.dumps(response));
            
            except Exception as e:
                self.logErr('exception %s' % (e))
            except: # catch *all* exceptions
                e = sys.exc_info()[0]
                self.logErr('exception %s (%s)' % (e.__class__, e))
                time.sleep(1)


if __name__ == "__main__":

    DbWorker().run()

