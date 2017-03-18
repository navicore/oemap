#!/bin/bash

#ejs todo: fix this abs path
#ejs todo: fix this abs path
#ejs todo: fix this abs path
#ejs todo: fix this abs path

echo "
    <resources>
    <string name=\"gitname\">`cd ~/git/navicore/oemap/android/OeMap && git describe --tags`</string>
    <string name=\"buildDate\">`date`</string>
    <string name=\"versionName\">`cat ~/git/navicore/oemap/android/OeMap/src/main/AndroidManifest.xml | grep versionName | grep -Po '".*?"' | sed 's/\"//g'`</string>
    </resources>
    " > ~/git/navicore/oemap/android/OeMap/src/main/res/values/gitinfo.xml

