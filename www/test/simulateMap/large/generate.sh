#!/bin/bash

if [ -d "./tmp" ]; then
    rmdir --ignore-fail-on-non-empty ./tmp
fi
mkdir -p ./tmp

# gen from seed list
./generate/grow.py > tmp/tmp_names.txt

# dedupe
sort -u ./tmp/tmp_names.txt > ./tmp/names.txt

# create a bunch of nearby points in the US
./generate/gengeo.py > ./tmp/tmp_points.txt

# dedupe
sort -u ./tmp/tmp_points.txt > ./tmp/points.txt

