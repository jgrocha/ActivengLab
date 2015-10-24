#!/bin/bash

# cd ~/Android/Sdk/platform-tools
# ~/Android/Sdk/platform-tools/adb devices
# List of devices attached
# KBLJZTKVAICMEQ6H	device
# ./adb -d shell "run-as activeng.pt.activenglab ls /data/data/activeng.pt.activenglab/databases/"
# temperature.db
# temperature.db-journal
# Copy the database file from your application folder to your sd card.
~/Android/Sdk/platform-tools/adb -d shell "run-as activeng.pt.activenglab cat /data/data/activeng.pt.activenglab/databases/temperature.db > /sdcard/temperature.db"
~/Android/Sdk/platform-tools/adb pull /sdcard/temperature.db
sqlitebrowser temperature.db
