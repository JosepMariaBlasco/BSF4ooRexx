#!/bin/sh
# starter to allow for running OOo-BSF4ooRexx scripts without logoff/logon immediately after installation

if [ -f ./setEnvironment4BSF.sh ]; then . ./setEnvironment4BSF.sh ; else . /opt/BSF4ooRexx/install/setEnvironment4BSF.sh ; fi
if [ -f ./setEnvironment4OOo.sh ]; then . ./setEnvironment4OOo.sh ; else . /opt/BSF4ooRexx/install/setEnvironment4OOo.sh ; fi

# rexx $*

# MacOSX: rgf, 2011-03-02
if [ -f ./rexxj.sh ]; then ./rexxj.sh $* ; else /opt/BSF4ooRexx/install/rexxj.sh $* ; fi
