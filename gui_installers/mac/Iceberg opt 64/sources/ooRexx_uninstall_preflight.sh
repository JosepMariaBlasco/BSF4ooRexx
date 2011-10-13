#!/bin/sh

# Name: ooRexx_uninstall_preflight.sh
# Purpose: Uninstall script for ooRexx
# Author: Juergen Hesse, Manuel Paar
# Version: 1.0
#
# ------------------------ Apache Version 2.0 license -------------------------
#    Copyright (C) 2011 Juergen Hesse, Manuel Paar
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#          http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
# -----------------------------------------------------------------------------


echo $SCRIPT_NAME: Removing links from /usr/lib.
rm -f /usr/lib/libhostemu.*
rm -f /usr/lib/librexx.*
rm -f /usr/lib/librexxapi.*
rm -f /usr/lib/librexxutil.*
rm -f /usr/lib/librxmath.*
rm -f /usr/lib/librxregexp.*
rm -f /usr/lib/librxsock.*
rm -f /usr/lib/librxunixsys.*

echo $SCRIPT_NAME: Removing links from /usr/bin.
rm -f /usr/bin/csvStream.cls
rm -f /usr/bin/mime.cls
rm -f /usr/bin/oorexx-config
rm -f /usr/bin/org.rexxla.oorexx.rxapid.plist
rm -f /usr/bin/rexx
rm -f /usr/bin/rexx.cat
rm -f /usr/bin/rexx.img
rm -f /usr/bin/rexxc
rm -f /usr/bin/rexximage
rm -f /usr/bin/rexxtry.rex
rm -f /usr/bin/rxdelipc
rm -f /usr/bin/rxftp.cls
rm -f /usr/bin/rxmigrate
rm -f /usr/bin/rxqueue
rm -f /usr/bin/rxregexp.cls
rm -f /usr/bin/rxsubcom
rm -f /usr/bin/smtp.cls
rm -f /usr/bin/socket.cls
rm -f /usr/bin/streamsocket.cls

echo $SCRIPT_NAME: Removing man pages from /usr/share/man/man1
rm -f /usr/share/man/man1/oorexx-config.1
rm -f /usr/share/man/man1/rexx.1
rm -f /usr/share/man/man1/rexxc.1
rm -f /usr/share/man/man1/rxmigrate.1
rm -f /usr/share/man/man1/rxqueue.1
rm -f /usr/share/man/man1/rxsubcom.1

echo $SCRIPT_NAME: Removing headers from /usr/include
rm -f /usr/include/oorexxapi.h
rm -f /usr/include/oorexxerrors.h
rm -f /usr/include/rexx.h
rm -f /usr/include/rexxapidefs.h
rm -f /usr/include/rexxapitypes.h
rm -f /usr/include/rexxplatformapis.h
rm -f /usr/include/rexxplatformdefs.h

echo $SCRIPT_NAME: Shutting down rxapi daemon
launchctl unload -w /Library/LaunchDaemons/org.rexxla.oorexx.rxapid.plist
killall rxapi

echo $SCRIPT_NAME: Removing rexx LaunchDeamon
rm -f /Library/LaunchDeamons/org.rexxla.oorexx.rxapid.plist
rm -f /usr/bin/rxapi

echo $SCRIPT_NAME: Removal of prior version of ooRexx complete.

sh /tmp/BSF4ooRexx_uninstall_preflight.sh $1
