#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c)
      date:       2011-08-14
      changed:    2014-08-07, rgf, - explicitly close connection, otherwise thread remains lingering
                                   - remove duplicate service names
                  2015-04-21, rgf, - renamed to dbusListObjectPaths.rex, such that the name indicates the dbus connection
                                   - changed logic to list the object paths of a given service name on the given bus;
                                     if the service name is omitted, then all service names on the given bus gets listed
                  2015-04-22, rgf, - fixed wrong usage of caselessPos()
                  2015-04-26, rgf, - renamed pp2() to pp() which is defined in 'dbus.cls'
      version:    102.20150426
      name:       dbusListObjectPaths.rex
      purpose:    interrogate and list all object paths on the given busname and on the
                  given service name; if the service name is omitted then all service names
                  published on the bus will be listed instead

      license:    AL 2.0
            ------------------------ Apache Version 2.0 license -------------------------
               Copyright 2011-2015 Rony G. Flatscher

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

parse source . . fn     -- get fully qualified name to this program
fname=filespec('n',fn)  -- get name of this program

parse arg a
if a="" | pos("?",a)>0 | a~caselessPos('help')>0 then  -- show usage
do
   say "usage:" fname "session|system|busaddress [serviceName]"
   say "        ... lists all obejct paths for the given serviceName on the given bus"
   say "        ... if no serviceName is given, all service names on the given bus get listed"
   exit -1
end

parse arg busType serviceName .  -- parse command line argument

if wordpos(busType,"session system")=0, pos(':',busType)=0 then
do
   say "Error: busType" pp(busType) "is not 'system', 'session', nor a bus address, aborting..."
   exit -2
end
conn=.Dbus~connect(busType)

if serviceName="" then     -- no service name given, list all known service names
do
   o=conn~getObject(.dbus.dir~ServiceDBus, .dbus.dir~PathDBus) -- get the DBus object
   say "available service names on bus ["||busType"]:"
   allServiceNames=.set~new ~union(o~listNames) ~union(o~listActivatableNames)
   w=allServiceNames~items~length
   i=1
   tab="09"x
   do sn over allServiceNames~allIndexes~sort
      say tab"service name #" i~right(w)":"tab || sn
      i+=1
   end
   say
   say "there are a total of" allServiceNames~items "unique service names (including activatable names) available on the ["||busType"] bus"
   say
   say "usage:" fname busname "serviceName"
   say "        ... to list all object paths of a particular listed service name"
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
