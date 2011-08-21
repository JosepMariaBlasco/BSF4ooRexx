#!/usr/bin/rexx
/*
      author:  Rony G. Flatscher, (c) 2011
      name:    testPrivateClientPingSimple.rex
      date:    2011-08-07/08
      license: 3.0, AL 2.0
      purpose: - just a few invocations, such that the amount of generated debug messages,
                 if in debug mode, remains manageable

               - test marshalling and unmarshalling; will unmarshal received arguments and
                 marshal them for the reply using the same signature

               - this sends any kind of arguments to the testRawPongServer.rex using

               - all DBus-methods directly (not taking advanatage of the class
                 DBusProxyObject)

               - test testing whether closing a private connection

      usage:   rexx testPrivateClientPingSimple.rex

      needs:   testPrivatePongServer.rex running and listening on the address this
               clients attaches to

      hint:    derived from testRawClientPing.rex, merely replaced "session" with the address
               where the server is listening for clients


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

say "DbusVersion():" dbusVersion()

objectPath="/org/rexxla/oorexx/dbus/test/Pong"
busName   ="org.rexxla.oorexx.dbus.test.pong"
interface =""

/*
busType="session"                  -- default to session bus
say "creating a connection to the" pp2(busType) "message bus..."
conn=.dbus~new(busType) -- get a connection
*/

address1="unix:path=/tmp/dbus-test"
address2="tcp:host=localhost,port=23000,family=ipv4;" -- socket lingers after shutdown
address3="unix:path=/tmp/dbus-test;tcp:host=localhost,port=23000,family=ipv4;"

parse arg otherAddress .
if otherAddress<>"" then
   address=otherAddress
else
   address=address3

say "creating a connection to" pp2(address) "message bus..."
conn=.dbus~new(address) -- get a connection

say "conn~uniqueBusName ="pp2(conn~busName("unique"))
say "conn~uniqueBusId   ="pp2(conn~uniqueBusId)
say "conn~busType       ="pp2(conn~busType)

signal on syntax

/*
say "now sending the first test message to the service object, saying 'halli,hallo'..."
res=conn~message("call", busName, objectPath, interface, "nixinoxi", "s", "s", "halli,hallo")
say 'res=conn~message("call", busName, objectPath, interface, "nixinoxi", "s", "s", "halli,hallo":' pp2(res)
call dump2 res, "roundtrip, 'res'-content"
*/
say "<--*--> "~copies(7)

--
say "now sending a test signal named 'OhA' ..."
signalObjectPath="/this/was/rony"
signalInterface ="org.rexxla.dbus.test.interface"
conn~message("signal", signalObjectPath, signalInterface, "OhA", "s", "data traveling with signal OhA, hopefully!")
say 'conn~message("signal", senderObjectPath, interface, "OhA", "s", "data traveling with signal OhA, hopefully!")'
say "<---***>"~copies(7)

-- call testSimpleTypes      conn, busName, objectPath, (address1~left(5)<>'unix:')   -- e.g. 'tcp:' does not allow UNIX_FD transport!

say "--*--  "~copies(10)
say "activating makeReplySlotDir and reissuing message call:"
conn~makeReplySlotDir=.true
res=conn~message("call", busName, objectPath, interface, "nixinoxi", "s", "s", "hi")
say 'res=conn~message("call", busName, objectPath, interface, "nixinoxi", "s", "s", "hi":' pp2(res)
call dump2 res, "roundtrip, 'res'-content"
say
call dump2 res[res~last], "roundtrip, 'res["res~last"]'-content"
say

say "DbusVersion():" dbusVersion()
say "conn~isOpen:  " conn~isOpen
conn~close
say "conn~isOpen:  " conn~isOpen "(after closing)"

call dumpConnectionStatistics conn


/*
opath="/org/freedesktop/DBus/Local"
iface="org.freedesktop.DBus.Local"
say "trying to query for object paths from" pp2(iface)
res=conn~getObjectPaths(iface)
call dump2 res, "conn~getObjectPaths('"iface"')"
res=conn~message("call", iface, objectPath, interface, "Introspect", "s", "")
say "res:" pp2(res)
*/


exit

halt:    -- stop the message loop thread
   say " ... ctl-c was pressed ..."
   call dumpConnectionStatistics conn
   exit -1

syntax:
   cond=condition('o')
   say "exception occurred, showing dumpCondition2(condition('o'))..."
   say ppCondition2(cond)
   say
   arr=cond~additional
   call dump2 arr, "additional"
   say
   call dump2 arr[arr~last], "additional[last]"  -- error slotDir
   call dumpConnectionStatistics conn
   exit -2


::requires "dbus.cls"         -- get DBus support for ooRexx
::requires "rgf_util2.rex"    -- get access to the rgf2 utilities
::requires "testDumpStats.rex"



::class testServiceObject
::method unknown              -- this method will service all invocations
  use arg methName, methArgs

  title=pp2(self)": methName="pp2(methName) "methArgs="pp2(methArgs)
  say title

  call dump2 methArgs, title "methArgs"

  if methArgs~size>0 then
     call dump2 methArgs[methArgs~items], title "slotDir"

   -- TODO: process arguments, create reply (return) value, by using the same arguments (without slotDir argument) with the same signature

  say "---/\--- "~copies(5)
  say


::routine testSimpleTypes
   use strict arg conn, busName, objectPath, bSupportsUnifxFd=.false

   interface=""
   methodName="someMethodNameTestingSimpleTypes"
say center(" testSimpleTypes ", 50, "-")


   say "conn="pp2(conn) "busName="pp2(busName) "objectPath="pp2(objectPath)
   say "interface="pp2(interface) "methodName="pp2(methodName)


   order=.array~of("s","g","o","y","b","n","q","i","h","u","x","t","d")

   typeName=.directory~new
   typeName["s"]="STRING"
   typeName["g"]="SIGNATURE"
   typeName["o"]="OBJECTPATH"
   typeName["y"]="BYTE"
   typeName["b"]="BOOLEAN"
   typeName["n"]="INT16"
   typeName["q"]="UINT16"
   typeName["i"]="INT32"

   if bSupportsUnifxFd=.true then
      typeName["h"]="UNIX_FD"

   typeName["u"]="UINT32"
   typeName["x"]="INT64"
   typeName["t"]="UINT64"
   typeName["d"]="DOUBLE"

numeric digits 20
   values=.directory~new
      -- UTF-8: must not contain 8-Bit-characters or
   values["s"]=.array~of("", "hi")                       -- string
   -- values["s"]=.array~of("", "hi", "aha",    , .nil, "anton", string.to.utf8("über"), "berta")

   values["g"]=.array~of("", "s",)        -- signature
   values["o"]=.array~of("/", "/Aha")     -- object path

   values["y"]=.array~of(" ", "a")        -- byte

   values["b"]=.array~of(.true, .false)   -- boolean

   values["n"]=.array~of(0, -1)           -- int16
   values["q"]=.array~of(0,  1)           -- uint16

   values["i"]=.array~of(0, -1)           -- int32

   if bSupportsUnifxFd=.true then
      values["h"]=.array~of(0,)           -- uint32: UNIX_FD: dbus does alter value

   values["u"]=.array~of(0,1)             -- uint32: tests o.k.

   values["x"]=.array~of(0,-1)            -- int64

   values["t"]=.array~of(0,1)             -- uint64

   values["d"]=.array~of(0, -1)           -- double

   width=30
   max=order~size
   max=1
   do k=1 to max        -- order~size
      type=order[k]
      say center("" type"/"typeName[type] "", width, "-")
      arr=values[type]  -- get values to send
      size=arr~size -- items
      len=size~length
      do i=1 to arr~size
         val=arr[i]
--         say "***" i~right(len)"/"size~right(len)":" pp2(val) pp2(val~string~c2x)
            res=conn~message("call", busName, objectPath, interface, methodName"_"type, type, type, val)

         say "===" i~right(len)"/"size~right(len)":" pp2(val) "<--?-->" "res="pp2(res) "|" pp2(val~string~c2x) "<--?-->"  pp2(res~string~c2x)
      end
      say "-"~copies(width)
   end

   say "-"~copies(79)
