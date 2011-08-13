#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher
      name:       testProxy2.rex
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

busName="org.freedesktop.Notifications"
objpath="/nixi/noxi"
objpath="/org/freedesktop/Notifications"

objpath="/"
objpath="/org"
objpath="/org/freedesktop"
objpath="/org/freedesktop/Notifications"

conn=.dbus~new("session")    -- get the "session" connection
say "Bus name:   " busName
say "Object path:" objPath
say

say "Getting access to the remote object on" busName":"
o=conn~getObject(busName, objpath) -- get the DBus object
say

say "Dumping the introspect data:"
say o~proxy.introspectData
say

say "Dumping the introspection data parse tree:"
call IDbus.dumpIDBus o~proxy.introspectRootNode
say

say "hasMethods:" o~proxy.introspectRootNode~contains("methods")
exit

say "Testing whether introspect parse tree has certain types:"
titles=.array~of("arguments", "interfaces", "methods", "signals", "properties")
rn=o~proxy.introspectRootNode
do kind over titles
   say ('  rootNode~contains("'kind'"):')~left(35) rn~contains(kind)
end
say

   /* check, wheter the introspection was empty; if so, try to load an
      introspection file locally and have it processed */
if o~proxy.introspectRootNode~contains("methods")=.false then  -- introspection failed?
do
    say "--> No valid introspection data found, trying to load from local file ..."
    fn=directory()"/"||busName||".xml"
    say "Supplying introspection data from file:" fn
    root=.IDBus~newIntrospection(fn)
    if root~contains("methods") then
    do
       say "Dumping introspection tree (from local file):"
       call IDbus.dumpIDBus root
       o~proxy.introspectRootNode=root
    end
    say
end

say "o~GetServerInformation:"
do i over o~GetServerInformation
   say "   " i
end
say

say "o~GetCapabilities~sort:"
do i over o~GetCapabilities~sort
   say "   " i
end
say

res=o~GetAll('')
say "o~GetAll(''): returned" res "with" res~items "items"
say




::requires "dbus.cls"
