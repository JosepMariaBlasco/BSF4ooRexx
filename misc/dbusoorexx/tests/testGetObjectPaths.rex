#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c)
      date:       2011-07-29, 2011-08-04
      version:    1.0
      name:       testGetObjectPaths.rex
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

parse arg bustype serviceName .
if servicename='' then
do
   serviceName=busType
   bustype="session"       -- default to 'session'
end

if wordpos(bustype,"session system")=0, pos(':',bustype)=0 then
do
   say "Error: bustype" pp2(bustype) "is not 'system', 'session', nor an address, aborting..."
   exit -1
end
conn=.Dbus~connect(bustype)

if serviceName="" then
do
   o=conn~getObject(.dbus.dir~ServiceDBus, .dbus.dir~PathDBus) -- get the DBus object
   say "available service/bus names:"
   do sn over .array~new ~union(o~listNames) ~union(o~listActivatableNames)~sort
      say "   " sn
   end
   say
   say "usage:  testGetObjectPaths serviceName"
end
else
do
   say "Object paths for service/bus name ["serviceName"]:"
   call dump2 conn~getObjectPaths(serviceName)
end


::requires 'dbus.cls'
::requires 'rgf_util2.rex'
