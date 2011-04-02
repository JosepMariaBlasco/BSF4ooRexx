#!/bin/sh
# starter to allow for running BSF4ooRexx scripts without logoff/logon immediately after installation
. /opt/BSF4ooRexx/install/setEnvironment4BSF.sh
rexx $*
