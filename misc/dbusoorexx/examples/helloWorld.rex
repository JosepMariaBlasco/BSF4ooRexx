#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c) 2011
      name:       testHelloWorld.rex
      date:       2011-08-04
      purpose:    demonstrate how to use a Rexx proxy to interact with a DBUS object
      license:    AL 2.0
            ------------------------ Apache Version 2.0 license -------------------------
               Copyright 2011 Rony G. Flatscher

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


o=.dbus~session~getObject("org.freedesktop.Notifications","/org/freedesktop/Notifications")

id=o~notify("Some Rexx App # 1", , , "This is summary info", "This is the body info", , , -1)
say "notification's id:" id
say "sleeping 3 secs ..."
call syssleep 3
say "creating another notification, reusing current notification ..."
id=o~notify("Another Important App", id, "oorexx", "This is summary info # 2", "This is the body info # 2", , , -1)
say "notification's id:" id


::requires "dbus.cls"         -- get access to DBus
