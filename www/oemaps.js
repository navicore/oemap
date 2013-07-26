var express = require('express');
var app = express();
app.use(express.bodyParser());

app.get('/', function(req, res) {
  res.type('text/plain');
  res.send('i am a beautiful butterfly');
});

var presences = {};

function findBySpace(spacename) {
    var ret = new Array();
    for (var id in presences) {
        if (presences.hasOwnProperty(id)) {
            var p = presences[id];
            if (p.hasOwnProperty("spaces")) {
                var spaces = p["spaces"];
                for (var i in spaces) {
                    if (spaces[i] == spacename)
                        ret.push(p);
                }
            }
        }
    }
    return ret;
}

//list spaces by geo query
app.get('/spaces/:id', function(req, res) {
 
    // todo: find spaces by location + nearness 
    res.json("{}"); //'none' is not an error
});

//get space by name
app.get('/space/:id', function(req, res) {
  
    var m = findBySpace(req.params.id);
    if (!m)
        return res.send('Error 404: No space found');
    res.json(m);
});

//for testing only, all presence queries by real applications are by space name
app.get('/presence/:id', function(req, res) {

    var p = presences[req.params.id];
    if (!p)
        return res.send('Error 404: No presence found');
    res.json(p);
  
});

app.put('/presence/:id', function(req, res) {

  presences[req.params.id] = req.body;
  //res.json(true);
  res.send(200);
});

app.delete('/presence/:id', function(req, res) {

  delete presences[req.params.id];
  //res.json(true);
  res.send(200);
});

console.log("starting...");
app.listen(process.env.PORT || 4730);

