#!/bin/sh
# starter to allow for running BSF4ooRexx scripts without logoff/logon immediately after installation

if [ -f ./setEnvironment4BSF.sh ]; then . ./setEnvironment4BSF.sh ; else . /opt/BSF4ooRexx/install/setEnvironment4BSF.sh ; fi

# rexx $*

# MacOSX: rgf, 2011-03-02
if [ -f ./rexxj.sh ]; then ./rexxj.sh $* ; else /opt/BSF4ooRexx/install/rexxj.sh $* ; fi
