#!/bin/bash

#ejs todo: fix this abs path
#ejs todo: fix this abs path
#ejs todo: fix this abs path
#ejs todo: fix this abs path

echo "
    <resources>
    <string name=\"gitname\">`git describe --tags`</string>
    <string name=\"buildDate\">`date`</string>
    <string name=\"versionName\">`cat /home/esweeney/git/oemap/android/OeMap/src/main/AndroidManifest.xml | grep versionName | grep -Po '".*?"' | sed 's/\"//g'`</string>
    </resources>
    " > /home/esweeney/git/oemap/android/OeMap/src/main/res/values/gitinfo.xml

