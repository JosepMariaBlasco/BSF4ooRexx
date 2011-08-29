#!/usr/bin/rexx
/*
      author:  Rony G. Flatscher, (c) 2011
      name:    testPrivatePropertiesServerPong.rex
      date:    2011-07-27
      purpose: - demonstrate how to realize a Rexx server that allows access to its
                 attributes via the org.freedesktop.DBus.Properties methods Get() and Set()
               - demonstrates how to create the introspect definitions on the fly

      usage:   rexx testPrivatePropertiesServerPong.rex

      dbus-specifications: <http://dbus.freedesktop.org/doc/dbus-specification.html> (as of 2011-07-14)

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
signal on syntax
signal on halt

say "DbusVersion()  ="pp2(DbusVersion())

objectPath    ="/org/rexxla/oorexx/dbus/test/Pong"
busName       ="org.rexxla.oorexx.dbus.test.pong.properties"

address1="unix:path=/tmp/dbus-test"
address2="tcp:host=localhost,port=23000,family=ipv4;" -- socket lingers after shutdown
address3="unix:path=/tmp/dbus-test;tcp:host=localhost,port=23000,family=ipv4;"

parse arg otherAddress .
if otherAddress<>"" then
   address=otherAddress
else
   address=address3

tso=.testServiceObject~new
say "starting up server, listening for connections on:" pp2(address)
-- note: if a default service listener object is defined for a private server,
--       then accepted connections will communicate with a copy (!) of the
--       default service object (reason: the service object remembers the connection
--       for being able to send signals back to its client)
server =.DBUSServer~new(address,tso)   -- supply default server
server~startup
say "server~active (after  startup):" pp2(server~active) "cself="pp2(server~cself)
say "server~serverId:               " pp2(server~serverId)
say "server~address:                " pp2(server~address)
say "server~serverAddress:          " pp2(server~serverAddress)

-- just doing this, such that there is no need to change the if-code
conn=.dbus~connect(address)   -- connect to private server
say "(server) conn~busType:" pp2(conn~busType)

/*
conn=.dbus~connect("session") -- get a connection, also: .dbus~new("session") or .dbus~session

   -- rgf, 2011-07-30: bus name request should be issued after registering service object to
   --      make sure the message loop is operational, before the bus name gets changed to the
   --      the service name for which others may be waiting already;
   --      not possible with 1.4.6, hence not adjusting this test program accordingly

res=conn~busName("request", busName, 7)
say 'res=conn~busName("request",' busName'):' pp2(res)
if res<>1 & res<>4 then signal halt    -- stop !
*/


r=random(1,3, time("sec"))    -- setup version for serviceObjects
r=3   -- force version to use (publishes four object paths)

if r=1 then       -- create one service object, publish its object path
do
   say "version #" r": creating one serviceObject, publishing its object path ..."
   -- tso=.testServiceObject~new
   conn~serviceObject("add", objectPath, tso)
      -- create a path maker entry to allow d-feet et.al. to find our object path
   conn~serviceObject("add", "default", .IDBusPathMaker~new(objectPath))
end

else if r=2 then  -- create four objectPath entries with different serviceObjects
do
   say "version #" r": creating four serviceObjects, but publishing only object path ["objectPath"] ..."
   arr=.array~of(objectPath, "/org/rexxla/rgf/ha1", "/org/rexxla/rgf/ha2", "/org/rexxla/abc/Hicks")
   conn~serviceObject("add", "default", .IDBusPathMaker~new(objectPath))
   do path over arr
      conn~serviceObject("add", path, .testServiceObject~new)
   end
end

else              -- create four objectPath entries with different serviceObjects, publish all of them
do
   say "version #" r": creating four serviceObjects, publishing all object paths ..."
   arr=.array~of(objectPath, "/org/rexxla/rgf/ha1", "/org/rexxla/rgf/ha2", "/org/rexxla/abc/Hicks")
   do path over arr
      conn~serviceObject("add", path, .testServiceObject~new)
   end
   .IDBusPathMaker~publishAllServiceObjects(conn) -- publish all service objects
end

say
call dump2 conn~serviceObject("getAll"), 'conn~serviceObject("getAll")'
/*
*/

say "version #" r "..."


sleepTime=30
-- say "press enter to end program ..."
-- parse pull x            -- no parse pull logic, if running as a service daemon (kbd not available!)
say "sleeping" sleepTime "seconds..."
call syssleep sleepTime
say "back from sleeping ..."

if r=1 then    -- only in version # 1 do we have 'tso' defined
do
   say "ending the program, attributes have the following values:"
   say "   rony1RO:" pp2(tso~Rony1ro)
   say "   rony2WO:" pp2(tso~Rony2wo)
   say "   rony3RW:" pp2(tso~rony3rw)
   say
end

connections=server~connections   -- get client connections
say "shutting down server ..."
server~shutdown      -- shutdown server, will close client connections and stop their message loops as well
do c over connections            -- iterate over client connections and show their stats
   call dumpConnectionStatistics c
end

say "DbusVersion()  ="pp2(DbusVersion())
exit

halt:    -- stop the message loop thread
   say " ... ctl-c was pressed, stopping messageLoop thread..."
   conn~messageLoop("stop")      -- otherwise process hangs
   say "DbusVersion()  ="pp2(DbusVersion())
   exit -1


syntax:  -- stop the message loop thread !
   say ":-( "~copies(10)
   say "a SYNTAX error has occurred, stopping the message loop ..."
   co=condition('o')
   conn~messageLoop("stop")      -- otherwise process hangs
   say
   say "the Rexx condition object contains the following information:"
   say ppCondition2(co)
   say ":-( "~copies(10)

   say "DbusVersion()  ="pp2(DbusVersion())
call dump2 co~additional[3]

   exit -2


::requires "dbus.cls"         -- get DBus support for ooRexx
::requires "rgf_util2.rex"    -- get access to the rgf2 utilities (installed by BSF4ooRexx)
::requires "testDumpStats.rex"


::class testServiceObject subclass DBusServiceObject
::method init
  expose introspectData Rony1ro Rony2wo Rony3rw

   -- define Introspect data
  node=.IntrospectHelper~new       -- create root node (could supply a name)
      -- tell that org.freedesktop.DBus.Introspectable.Introspect() is available
  if=node~addInterface("org.freedesktop.DBus.Introspectable")
  if~addMethod("Introspect",,"s")  -- name, in-signature, out-signature

      -- tell that property org.freedesktop.DBus.Properties.Get() and org.freedesktop.DBus.Properties.Set() is available
  if=node~addInterface("org.freedesktop.DBus.Properties")
  if~addMethod("Get","ss","v")
  if~addMethod("Set","ssv","")

      -- tell about the properties that are available (implemented as Rexx attributes)
  if=node~addInterface("org.rexxla.rgf.test")
  if~addProperty("Rony1ro", "s", "read")
  if~addProperty("Rony2wo", "s", "write")
  if~addProperty("Rony3rw", "s", "readwrite")

  introspectData=node~makeString       -- turn definitions into the appropriate xml-string
  self~init:super(introspectData)      -- make sure we define the interface data for the clients (will be returned by Introspect)

  say "init: created on the fly the following introspect data:"
  say introspectData
  say "-"~copies(3)

   -- define initial values for our attributes
  Rony1ro="rony1RO, created at:" .DateTime~new
  Rony2wo="rony2WO, created at:" .DateTime~new
  rony3rw="rony3RW, created at:" .DateTime~new
  say "attributes have the following initial values:"
  say "   rony1RO:" pp2(self~Rony1ro)
  say "   rony2WO:" pp2(self~Rony2wo)
  say "   rony3RW:" pp2(self~rony3rw)
  say " --- end of init ---"

   -- attributes we allow access to
::attribute Rony1ro     -- get/set (locally); interface description allows get only!


  -- a method implementation: if called locally, no slotDir is added as the last argument
  --                          if called via DBus, a sltoDir gets added!
::method Rony2wo        -- full access (locally); interface description allows set only!
  expose rony2wo

  bGet=(arg()=0)        -- local getter call?
  if \bGet, arg()=1, arg1~isA(.directory), arg1~hasEntry("MESSAGENAMETYPE") then
     bGet=.true

  if bGet then return rony2wo -- getter

  rony2wo=arg(1)        -- assign first argument as new value
  return

::attribute Rony3rw     -- get/set; interface description allows both, get and set

