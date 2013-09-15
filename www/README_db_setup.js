//
// make sure to run these on every db init
// mongo oemap_test < ./README_db_setup.js
//
//

db.presences.ensureIndex(
  {"exp_time": 1}, {"expireAfterSeconds" : 0,
                    "background" : true,
                    "safe" : true}
);

//support query 'get the nearest n items in map'
//ejs todo: run explain, is this redundant with the indexes below???
db.presences.ensureIndex({"space": 1, "location": "2dsphere"});

//support query 'get the nearest n unique maps'
db.presences.ensureIndex({"location": "2dsphere"});
db.presences.ensureIndex({"space": 1});

