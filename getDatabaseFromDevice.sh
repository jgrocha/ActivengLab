#!/bin/bash

# cd ~/Android/Sdk/platform-tools
# ~/Android/Sdk/platform-tools/adb devices
# List of devices attached
# KBLJZTKVAICMEQ6H	device
#
adb shell "run-as activeng.pt.activenglab chmod 666 /data/data/activeng.pt.activenglab/databases/temperature.db"
adb shell "mkdir -p /sdcard/tempDB"
adb shell "cp /data/data/activeng.pt.activenglab/databases/temperature.db /sdcard/tempDB"
adb pull "/sdcard/tempDB/temperature.db"
adb shell "rm -rf sdcard/tempDB"
sqlitebrowser temperature.db