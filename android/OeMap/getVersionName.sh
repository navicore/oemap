#!/bin/bash

echo `cat /home/esweeney/git/oemap/android/OeMap/src/main/AndroidManifest.xml | grep versionName | grep -Po '".*?"' | sed 's/\"//g'`

