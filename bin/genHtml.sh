#!/bin/sh
sed 's|app.js|web/target/scala-2.12/slimetrail-web-fastopt.js|' index.html > fast.html
sed 's|app.js|web/target/scala-2.12/slimetrail-web-opt.js|' index.html > full.html
