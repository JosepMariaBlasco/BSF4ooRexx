#!/usr/bin/rexx
/*
      author:  Rony G. Flatscher, (c) 2011
      name:    testPrivateServerPong.rex
      date:    2011-08-07
      purpose:
               - test private DBus connection
               - test marshalling and unmarshalling; will unmarshal received arguments and
                 marshal them for the reply using the same signature
               - if invoked will create a private DBUS server and register a service object
                 and listener object, and wait for call messages to reply
               - this receives (unmarshals) arguments from testPrivatePingClient.rex and
                 returns (marshalled) them exactly as received
               - all DBus-methods directly (not taking advanatage of the class
                 DBusServiceObject

      usage:   testPrivateServerPong.rex

      hint:    derived almost 1:1 from testRawServerPong.rex, only the initialization (main)
               part of the program got changed

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

say "DbusVersion()  ="pp2(DbusVersion())

address1="unix:path=/tmp/dbus-test"
address2="tcp:host=localhost,port=23000,family=ipv4;" -- socket lingers after shutdown
address3="unix:path=/tmp/dbus-test;tcp:host=localhost,port=23000,family=ipv4;"
address=address1

defaultService =.testServiceObject~new
defaultListener=.testSignalObject~new
say "starting up server, listening for connections on:" pp2(address)
server =.DBUSServer~new(address,defaultService,defaultListener)
server~startup
say "server~active (after  startup):" pp2(server~active) "cself="pp2(server~cself)
say "server~serverId:               " pp2(server~serverId)
say "server~address:                " pp2(server~address)
say "server~serverAddress:          " pp2(server~serverAddress)


signal on syntax
signal on halt

say "DbusVersion()="pp2(DbusVersion())
say "Hit enter to end program ..."
parse pull x

say "showing statistics on each served connection:"
do conn over server~connections
   call dumpConnectionStatistics conn
end

say "about to do a server~shutdown..."
server~shutdown
say "server~active (after shutdown):   " pp2(server~active) -- "server2:" pp2(server2~active)
say "DbusVersion()  ="pp2(DbusVersion())

exit

halt:    -- stop the message loop thread
   say " ... ctl-c was pressed ..."
   say "showing statistics on each served connection, if any active:"
   do conn over server~connections
      call dumpConnectionStatistics conn
   end
   say "about to do a server~shutdown..."
   if server~active=.true then
       server~shutdown
   say "server~active (after shutdown):   " pp2(server~active)

   say "DbusVersion()  ="pp2(DbusVersion())
   exit -1



syntax:  -- stop the message loop thread !
   say "... server, arrived in syntax-condition ..."
   co=condition('o')
   say "about to do a server~shutdown..."
   if server~active=.true then
      server~shutdown
   say "server~active (after shutdown):   " pp2(server~active)

   say ":-( "~copies(10)
   say "a SYNTAX error has occurred, stopping the message loop ..."
   say "the Rexx condition object contains the following information:"
   say ppCondition2(co)
   say ":-( "~copies(10)

   say "DbusVersion()  ="pp2(DbusVersion())
   exit -2



::requires "dbus.cls"         -- get DBus support for ooRexx
::requires "rgf_util2.rex"    -- get access to the rgf2 utilities
::requires "testDumpStats.rex"



::class testServiceObject
::method unknown              -- this method will service all invocations
  use arg methName, methArgs

-- signal on syntax
say center(" PONG::unknown, methName:" pp2(methName) "args:" pp2(methArgs) pp2(methArgs~items) "", 100, "*")

  slotDir=methArgs[methArgs~last]
-- say "*** PONG::unknown, slotDir:" pp2(slotDir) "slotDir~class:" pp2(slotDir~class) "methArgs~items="pp2(methArgs~items)
  title=pp2(self)": methName="pp2(methName) "methArgs="pp2(methArgs) "methArgs[1]="pp2(methArgs[1]) "value:" pp2(methArgs[1]~string~c2x) pp2(slotdir~signature)
  say "PONG::"title

  call dump2 methArgs, title "methArgs"

   do i=1 to methArgs~size - 1
      call dump2 methArgs[i], "argument #" i
      say
   end

   call dump2 slotDir, title "slotDir"

   say "    --> --> --> now creating return value ---"

/*
   -- process arguments, create reply (return) value, by using the same arguments (without slotDir argument) with the same signature
  resArr=.array~new~~append("useThisSignature="slotDir~signature)   -- first element a key word followed by signature
  do i=1 to methArgs~size-1         -- append all other arguments in the order received
     resArr~append(methArgs[i])
  end
*/

-- say "PONG: about to create a section, methArgs~size-1:" methArgs~size-1", useThisSignature="slotDir~signature

  resArr=.array~of("useThisSignature="slotDir~signature)
  resArr~append(methArgs~section(1,methArgs~size-1)) -- leave out the supplemental slotDir argument

/*
  if methArgs~items>2 then       -- two or more args
     resArr~append(methArgs~section(1,methArgs~size-1)) -- leave out the supplemental slotDir argument
  else                           -- one or no arg
     resArr~append(methArgs[1])
*/


call dump2 resArr, "PONG - resArr:"
say "PONG --> now returning ..."

  say "---/\--- "~copies(5)
  say
  return resArr



syntax:
  co=condition('o')
  say "*** PONG PONG PONG syntax ERROR!"
  call dump2 co, "syntax condition PONG"
  -- say dumpCondition2(co)


::class testSignalObject
::method unknown              -- this method will service all invocations
  use arg methName, methArgs


  slotDir=methArgs[methArgs~items]
  -- title=pp2(self)": methName="pp2(methName) "methArgs="pp2(methArgs) "methArgs[1]="pp2(methArgs[1]) "value:" pp2(methArgs[1]~c2x) pp2(slotdir~signature)
  title=pp2(self)": methName="pp2(methName) "methArgs="pp2(methArgs) "methArgs[1]="pp2(methArgs[1]) pp2(slotdir~signature)
  say title

  call dump2 methArgs, title "methArgs"

  if methArgs~items>0 then
     call dump2 slotDir, title "slotDir"

   -- TODO: process arguments, create reply (return) value, by using the same arguments (without slotDir argument) with the same signature

  say "--\/==\/-- "~copies(5)
  say

--  return methArgs[1]       -- will return as a string
--  return .array~of(slotDir~signature, methArgs[1])       -- will return a string


::method NameOwnerChanged  -- demo how to directly intercept a signal
  use arg one, two, three, slotDir
  say "==>" pp2(slotDir~messageTypeName) pp2(slotDir~interface) pp2(slotDir~member) pp2(slotDir~dateTime)":" pp2(one)"," pp2(two)"," pp2(three)

