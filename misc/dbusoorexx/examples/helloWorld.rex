#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c) 2011
      name:       testHelloWorld.rex
      date:       2011-08-04
      purpose:    demonstrate how to use a Rexx proxy to interact with a DBUS object
      changed:    2014-07-23, rgf: adapted logic to new infrastructure, hence a need to explicit stop
                                   the message loop thread in order to allow ooRexx to stop (using the
                                   method 'close' on the connection object)
               2014-07-31, rgf: adjust to new DBusVersion() routine
      license:    AL 2.0
            ------------------------ Apache Version 2.0 license -------------------------
               Copyright 2011-2014 Rony G. Flatscher

               Licensed under the Apache License, Version 2.0 (the "License");
               you may not use this file except in compliance with the License.
               You may obtain a copy of the License at

                   http://www.apache.org/licenses/LICENSE-2.0

               Unless required by applicable law or agreed to in writing, software
               distributed under the License is distributed on an "AS IS" BASIS,
               WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
               See the License for the specific language governing permissions and
               limitations under the License.
            -----------------------------------------------------------------------------
*/

say "DBusVersion():" DBusVersion()

o=.dbus~session~getObject("org.freedesktop.Notifications","/org/freedesktop/Notifications")

id=o~notify("Some Rexx App # 1", , , "This is summary info", "This is the body info", , , -1)
say "notification's id:" id
say "sleeping 3 secs ..."
call syssleep 3
say "---"
say "creating another notification, reusing current notification ..."
id=o~notify("Another Important App", id, "oorexx", "This is summary info # 2", "This is the body info # 2", , , -1)
say "notification's id:" id

.dbus~session~close           -- close, thereby terminating message loop thread


::requires "dbus.cls"         -- get access to DBus
