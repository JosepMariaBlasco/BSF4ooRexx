#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c)
      date:       2011-08-14
      changed:    2014-08-07, rgf, - explicitly close connection, otherwise thread remains lingering
                                   - remove duplicate service names
      version:    1.1
      name:       listObjectPaths.rex
      purpose:    interrogate given bus name for the object paths it publishes and list them
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

parse arg bustype serviceName .  -- command line argument
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

if serviceName="" then     -- no service name given, list all known service names
do
   o=conn~getObject(.dbus.dir~ServiceDBus, .dbus.dir~PathDBus) -- get the DBus object
   say "available service/bus names:"
   allServiceNames=.set~new ~union(o~listNames) ~union(o~listActivatableNames)
   w=allServiceNames~items~length
   i=1
   tab="09"x
   do sn over allServiceNames~allIndexes~sort
      say "service name #" i~right(w)":"tab || sn
      i+=1
   end
   say
   say "there are" allServiceNames~items "unique service names (including activatable names) available"
   say
   say "usage:  testGetObjectPaths serviceName"
end
else                       -- get an array of published object paths
do
   objPaths=conn~getObjectPaths(serviceName)~sort

   say "There are" objPaths~items "object paths for service/bus name ["serviceName"]:"
   do i=1 to objPaths~items
      say "  #" i": ["objPaths[i]"]"
   end
end
conn~close


::requires 'dbus.cls'      -- load DBUS support for ooRexx
