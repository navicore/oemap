//
// make sure to run these on every db init
//
//
//

//TODO: ejs get rid uid and make _id calculated by client making 
//it a PID (presence id)

//primary compound key
//db.presences.ensureIndex({"uid": 1, "space": 1}, {"unique": true})

// ttls are bunched into 3 types, no fine granularity needed.  ttl
// is different from expire time / quit time.  ttl stops old
// presences from being used in the case a phone is shut off or
// out of range.

// 5 minutes
db.presences.ensureIndex(
  {"short_ttl_start_time": 1}, {"expireAfterSeconds" : 5 * 60} 
)

// 1 hour
db.presences.ensureIndex(
  {"medium_ttl_start_time": 1}, {"expireAfterSeconds" : 60 * 60}
)

// 1 day
db.presences.ensureIndex(
  {"long_ttl_start_time": 1}, {"expireAfterSeconds" : 24* 60 * 60} 
)

//support query 'get the nearest n items in map'
//db.presences.ensureIndex({"space": 1, "location": "2dsphere"})

//support query 'get the nearest n unique maps'
db.presences.ensureIndex({"location": "2dsphere"})

