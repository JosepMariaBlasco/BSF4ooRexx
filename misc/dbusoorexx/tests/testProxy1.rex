#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c)
      date:       2011-07-16
      changes:    2014-07-24, rgf: explicitly close connection to stop message loop thread
      name:       testProxy1.rex
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

say "DbusVersion():" dbusVersion()
say

conn=.dbus~new("session")     -- get the "session" connection
say "DBUS service bus name:   " .dbus.dir~ServiceDBus
say "DBUS service object path:" .dbus.dir~PathDBus
say

say "Getting access to the remote DBUS service object:"
o=conn~getObject(.dbus.dir~ServiceDBus, .dbus.dir~PathDBus) -- get the DBus object
say "o~getID:" o~GetID
say

say "The following bus names are available (o~ListNames~sort):"
ln=o~ListNames~sort
do name over ln
   say "   " name
end
say "There are currently" ln~items "bus names in total."
say

str=o~Introspect
say "Running method Introspect on" .dbus.dir~PathDBus "yields:"
say str
say

say "Dumping the introspection data parse tree:"
call IDbus.dumpIDBus o~proxy.introspectRootNode
say

say "The following bus names are activatable (o~ListActivatableNames~sort):"
an=o~ListActivatableNames~sort
do name over an~sort
   say "  ["name"]"
end
say "There are currently" an~items "activatable bus names in total."

conn~close

::requires "dbus.cls"

