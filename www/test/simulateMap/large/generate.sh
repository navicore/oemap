#!/bin/bash

if [ -d "./tmp" ]; then
    rmdir --ignore-fail-on-non-empty ./tmp
fi
mkdir -p ./tmp

# gen from seed list
./scripts/grow.py > tmp/tmp_names.txt

# dedupe
sort -u ./tmp/tmp_names.txt > ./tmp/names_sorted.txt
# randomize so that everyone near you does't have the same first name
awk 'BEGIN {srand()} {print int(rand()*1000000) "\t" $0}' ./tmp/names_sorted.txt | sort -n | cut -f 2- > ./tmp/names.txt

# create a bunch of nearby points in the US
./scripts/gengeo.py > ./tmp/tmp_points.txt

# dedupe
sort -u ./tmp/tmp_points.txt > ./tmp/points.txt

# gen the json presence messages
./scripts/build_json.py > ./tmp/presences.json

