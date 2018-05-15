#!/bin/sh
find ../slimetrail.scalajs -type d \( -name target -or -path "project/project" -or -name ".js" -or -name ".jvm" \) | xargs rm -frv
rm -f full.html fast.html