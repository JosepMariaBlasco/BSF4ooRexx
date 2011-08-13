#!/bin/sh
# run with a local version of libdbus
DBUSLIB=$HOME/lib/libdbus-1.so
# DBUSLIB=$HOME/lib/libdbus-1.so.3.5.7
echo $0 running "LD_PRELOAD=$DBUSLIB rexx $1"
LD_PRELOAD=$DBUSLIB rexx $1
