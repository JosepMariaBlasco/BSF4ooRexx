#!/usr/bin/rexx
/*
      author:  Rony G. Flatscher, (c) 2011
      name:    testPong.rex
      date:    2011-07-03
      purpose: - test marshalling and unmarshalling; will unmarshal received arguments and
                 marshal them for the reply using the same signature

               - if invoked will register a service object and wait for call messages to reply

      usage:   testPong [bustype=session|system]

      dbus-specifications: <http://dbus.freedesktop.org/doc/dbus-specification.html> (as of 2011-07-14)


      author:  Rony G. Flatscher, (c) 2011
      name:    testPing.rex
      date:    2011-07-03
      purpose: - test marshalling and unmarshalling; will unmarshal received arguments and
                 marshal them for the reply using the same signature

               - this receives (unmarshals) arguments from testRawPingClient.rex and
                 returns (marshalled) them exactly as received

               - all DBus-methods directly (not taking advanatage of the class
                 DBusServiceObject

      usage:   rexx testRawPongClient.rex
      needs:   testRawPongServer.rex running

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

objectPath    ="/org/rexxla/oorexx/dbus/test/Pong"
busName       ="org.rexxla.oorexx.dbus.test.pong"
thisInterface =""

busType="session"       -- default to session bus
say "creating a connection to the" pp2(busType) "message bus..."
conn=.dbus~new(busType) -- get a connection

res=conn~busName("request", busName, 7)
say 'res=conn~busName("request",' busName'):' pp2(res)   -- rgf, 2011-07-29: moved here to cater for Ubuntu DBus 1.4.6 bug
if res<>1 & res<>4 then signal halt    -- stop !

listener=.testSignalObject~new
conn~listener("add", listener)
say "just added a listener object" pp2(listener) "to connection."

say "conn~uniqueBusName ="pp2(conn~busName("unique"))
say "conn~uniqueBusId   ="pp2(conn~uniqueBusId)
say "conn~busType       ="pp2(conn~busType)

oldServiceObject=conn~serviceObject("add", objectPath, .testServiceObject~new)
say 'oldServiceObject=conn~serviceObject("add", objectPath, .testServiceObject~new):' pp2(oldServiceObject)

call dump2 conn~serviceObject("get"), "serviceObjects"
say "---"


signal on syntax
signal on halt
-- matchRule="interface='org.rexxla.dbus.test.interface'"
matchRule="interface='org.rexxla.dbus.test.interface'"
matchRule="type='signal'"
say "adding match rule (filter):" pp2(matchRule)
conn~match("add", matchRule)

say "Hit enter to end program ..."
parse pull x

conn~messageLoop("stop")   -- stop message loop or we hang

say "DbusVersion()  ="pp2(DbusVersion())
call dumpConnectionStatistics conn
exit

halt:    -- stop the message loop thread

   say " ... ctl-c was pressed, stopping messageLoop thread..."
   if conn~messageLoop("active")<>.false then
      conn~messageLoop("stop")
   else
      say "message loop was stopped already."

say "DbusVersion()  ="pp2(DbusVersion())
call dumpConnectionStatistics conn
   exit -1


syntax:  -- stop the message loop thread !
   say ":-( "~copies(10)
   say "a SYNTAX error has occurred, stopping the message loop ..."
   co=condition('o')
   if conn~messageLoop("active")<>.false then
      conn~messageLoop("stop")

   say
   say "the Rexx condition object contains the following information:"
   say ppCondition2(co)
   say ":-( "~copies(10)

say "DbusVersion()  ="pp2(DbusVersion())
call dumpConnectionStatistics conn
   exit -2


::requires "dbus.cls"         -- get DBus support for ooRexx
::requires "rgf_util2.rex"    -- get access to the rgf2 utilities
::requires "testDumpStats.rex"



::class testServiceObject
::method unknown              -- this method will service all invocations
  use arg methName, methArgs

-- signal on syntax
say center(" PONG::unknown, methName:" pp2(methName) "args:" pp2(methArgs) pp2(methArgs~items) "", 100, "*")

  slotDir=methArgs[methArgs~items]
-- say "*** PONG::unknown, slotDir:" pp2(slotDir) "slotDir~class:" pp2(slotDir~class)
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

say "PONG: about to create a section, methArgs~size-1:" methArgs~size-1", useThisSignature="slotDir~signature

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

