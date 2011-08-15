#!/bin/sh
# run with a local version of libdbus
# DBUSLIB=$HOME/lib/libdbus-1.so
# DBUSLIB=$HOME/lib/libdbus-1.so.3.5.7
DBUSLIB=$HOME/dev/bsf4oorexx/sandbox/rgf/misc/dbusoorexx/dev
DBUSPATH=$HOME/dev/bsf4oorexx/sandbox/rgf/misc/dbusoorexx
echo $0 running "DYLD_LIBRARY_PATH=$DBUSLIB PATH=$DBUSPATH rexx $1"
DYLD_LIBRARY_PATH=$DBUSLIB PATH=$DBUSPATH:$PATH rexx $1
