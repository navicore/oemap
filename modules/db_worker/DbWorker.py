#!/usr/bin/python2

from pymongo import MongoClient
import sys
import syslog
import redis
import json
import time
import datetime
#from riemann import RiemannClient, RiemannUDPTransport

#rmmonitor = RiemannClient(transport = RiemannUDPTransport, host=config.riemann['host'])

class DbWorker():


    def __init__ (self):

        rhost = "127.0.0.1"
        rport = 6379
        self.rdis = redis.Redis(host=rhost, port=rport)
        self.inQName = "oemap_db_worker_01_in_queue" # todo: unique
        self.replyTo = "oemap_www_node_01_in_queue"

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
            syslog.syslog(syslog.LOG_NOTICE, "processed %s records. rate was %s." % (self.statC, r))
            self.statI = 0
            self.startTime = now
    
    def run (self):
        syslog.syslog(syslog.LOG_NOTICE, '%s starting queue %s' % ("test", self.inQName))

        while True:

            response = {'status': 'ok'}

            try:
               
                (q, msg) = self.rdis.brpop(keys=[self.inQName], timeout=600);
                #syslog.syslog(syslog.LOG_NOTICE, 'DbWorker got %s' % (str(msg)))
                
                rec = json.loads(msg)
                rec = json.loads(rec)
                rec['_id'] = rec['uid'] + '_' + rec['space']
                try:
                    #syslog.syslog(syslog.LOG_NOTICE, 'DbWorker updating pid %s' % (rec['_id']))
                    self.db.presences.save(rec)
                    self.stats()
                except Exception as e:
                    response = {'status': 'error', 'msg': str(e)}
                    syslog.syslog(syslog.LOG_ERR, 'mongodb exception %s' % (e))
                
                # reply to client
                self.rdis.lpush(self.replyTo, json.dumps(response));
            
            except Exception as e:
                syslog.syslog(syslog.LOG_ERR, 'exception %s' % (e))
            except: # catch *all* exceptions
                e = sys.exc_info()[0]
                syslog.syslog(syslog.LOG_ERR, 'exception %s (%s)' % (e.__class__, e))
                time.sleep(1)


if __name__ == "__main__":

    DbWorker().run()

