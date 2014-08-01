#!/usr/bin/rexx
/*
      author:  Rony G. Flatscher, (c) 2011
      name:    testPrivateClientPing.rex
      date:    2011-08-07, 2011-08-24
      license: 3.0, AL 2.0
      purpose: - test marshalling and unmarshalling; will unmarshal received arguments and
                 marshal them for the reply using the same signature

               - this sends any kind of arguments to the testRawPongServer.rex using

               - all DBus-methods directly (not taking advanatage of the class
                 DBusProxyObject

      usage:   rexx testPrivateClientPing.rex

      needs:   testPrivatePongServer.rex running and listening on the address this
               clients attaches to

      hint:    derived from testRawServerPong.rex, merely replaced "session" with the address
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

address1="unix:path=/tmp/dbus-test"
address2="tcp:host=localhost,port=23000,family=ipv4;" -- socket lingers after shutdown
address3="tcp:host=localhost,port=23000,family=ipv4;unix:path=/tmp/dbus-test;"

parse arg otherAddress doNotTestUNIX_FDs

if doNotTestUNIX_FDs<>"" then
   .local~client.unix_Fd=.false

/*
say ".client.unix_fd="pp2(.client.unix_fd) "doNotTestUNIX_FDs="pp2(doNotTestUNIX_FDs)
say "hit enter"
parse pull aha
*/

if otherAddress<>"" then
   address=otherAddress
else
   address=address3

say "creating a connection to the" pp2(busType) "message bus..."
say "creating a connection to" pp2(address) "message bus..."
conn=.dbus~new(address) -- get a connection

say "conn~uniqueBusName ="pp2(conn~busName("unique"))
say "conn~uniqueBusId   ="pp2(conn~uniqueBusId)
say "conn~busType       ="pp2(conn~busType)

signal on syntax

say "now sending the first test message to the service object, saying 'halli,hallo'..."
res=conn~message("call", busName, objectPath, interface, "nixinoxi", "s", "s", "halli,hallo")
say 'res=conn~message("call", busName, objectPath, interface, "nixinoxi", "s", "s", "halli,hallo":' pp2(res)
call dump2 res, "roundtrip, 'res'-content"
say "<--*--> "~copies(7)

--
say "now sending a test signal named 'OhA' ..."
signalObjectPath="/this/was/rony"
signalInterface ="org.rexxla.dbus.test.interface"
conn~message("signal", signalObjectPath, signalInterface, "OhA", "s", "date traveling with signal OhA, hopefully!")
say 'conn~message("signal", senderObjectPath, interface, "OhA", "s", "date traveling with signal OhA, hopefully!")'
say "<---***>"~copies(7)

call testSimpleTypes      conn, busName, objectPath

call testSimpleArrayTypes conn, busName, objectPath
call test2dimensionalArrayTypes conn, busName, objectPath
call test3dimensionalArrayTypes conn, busName, objectPath

call testByteArrays conn, busName, objectPath
call testMapType conn, busName, objectPath
call testStructTypes1 conn, busName, objectPath

call testVariantTypes1 conn, busName, objectPath
say "/*\ "~copies(10)
call testVariantTypes2 conn, busName, objectPath

say "--*--  "~copies(10)
say "activating makeReplySlotDir and reissuing message call:"
conn~makeReplySlotDir=.true
res=conn~message("call", busName, objectPath, interface, "nixinoxi", "s", "s", "hi")
say 'res=conn~message("call", busName, objectPath, interface, "nixinoxi", "s", "s", "hi":' pp2(res)
call dump2 res, "roundtrip, 'res'-content"
say
call dump2 res[res~last], "roundtrip, 'res["res~last"]'-content"
say

call dump2 .environment, ".environment"
call dump2 .local, ".local"
say "DbusVersion():" dbusVersion()
conn~close
call dumpConnectionStatistics conn
say "DbusVersion():" dbusVersion()
exit

halt:    -- stop the message loop thread
   say " ... ctl-c was pressed ..."
   conn~close
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
   conn~close
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
   use strict arg conn, busName, objectPath, interface="", methodName="someMethodName"
say center(" testSimpleTypes ", 50, "-")


   say "conn="pp2(conn) "busName="pp2(busName) "objectPath="pp2(objectPath)
   say "interface="pp2(interface) "methodName="pp2(methodName)


   if conn~query("typecode", "h"), .client.unix_Fd<>.false then    -- does the connection support UNIX_FD's ?
      order=.array~of("s","g","o","y","b","n","q","i","h","u","x","t","d")
   else
      order=.array~of("s","g","o","y","b","n","q","i","u","x","t","d")

   typeName=.directory~new
   typeName["s"]="STRING"
   typeName["g"]="SIGNATURE"
   typeName["o"]="OBJECTPATH"
   typeName["y"]="BYTE"
   typeName["b"]="BOOLEAN"
   typeName["n"]="INT16"
   typeName["q"]="UINT16"
   typeName["i"]="INT32"
   typeName["h"]="UNIX_FD"
   typeName["u"]="UINT32"
   typeName["x"]="INT64"
   typeName["t"]="UINT64"
   typeName["d"]="DOUBLE"

numeric digits 20
   values=.directory~new
      -- UTF-8: must not contain 8-Bit-characters or
   values["s"]=.array~of("", "hi", "aha", , .nil, "anton", "ueber", "berta")
   -- values["s"]=.array~of("", "hi", "aha",    , .nil, "anton", string.to.utf8("über"), "berta")
   values["g"]=.array~of("", "s",  "g",   "o", "oas", ,       .nil,                   "ay")
   values["o"]=.array~of("/", "/Aha", "/org/Aha", , .nil, "/org/aha/soso/Aha")

   -- values["y"]=.array~of(" ", "a", "A", , .nil, "U")  -- byte
   values["y"]=.array~of(" ", "a", "A", , .nil, "Ü")     -- byte

   values["b"]=.array~of(.true, .false, , .nil, 0, 1)    -- boolean
   -- values["n"]=.array~of(0, -1, , .nil, 1)               -- int16
   values["n"]=.array~of(0, -1, , .nil, 1, -32768, 32767) -- int16
   values["q"]=.array~of(0,     , .nil, 1, 65535)        -- uint16

   values["i"]=.array~of(0, -1, , .nil, 1, -2147483648, 2147483647)  -- int32
   values["h"]=.array~of(0,     , .nil, 1)               -- uint32: UNIX_FD: dbus does alter value
   values["u"]=.array~of(0,     , .nil, 1, 4294967295)   -- uint32: tests o.k.

   values["x"]=.array~of(0,     , .nil, 1, -9223372036854775808, 9223372036854775807)  -- int64

   values["t"]=.array~of(0,     , .nil, 1, 18446744073709551615)  -- uint64

   values["d"]=.array~of(0, -1, , .nil, 1)               -- double

   width=30
   yyy=4    -- 4=byte
   do k=1 to order~size --  yyy -- order~size -- items -- type over order
--   do k=yyy to yyy -- order~size --  yyy -- order~size -- items -- type over order
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


::routine testSimpleArrayTypes
   use strict arg conn, busName, objectPath, interface="", methodName="someMethodName"

say center(" testSimpleArrayTypes ", 50, "-")

   if conn~query("typecode", "h"), .client.unix_Fd<>.false then    -- does the connection support UNIX_FD's ?
      order=.array~of("s","g","o","y","b","n","q","i","h","u","x","t","d")
   else
      order=.array~of("s","g","o","y","b","n","q","i","u","x","t","d")

   typeName=.directory~new
   typeName["s"]="STRING"
   typeName["g"]="SIGNATURE"
   typeName["o"]="OBJECTPATH"
   typeName["y"]="BYTE"
   typeName["b"]="BOOLEAN"
   typeName["n"]="INT16"
   typeName["q"]="UINT16"
   typeName["i"]="INT32"
   typeName["h"]="UNIX_FD"
   typeName["u"]="UINT32"
   typeName["x"]="INT64"
   typeName["t"]="UINT64"
   typeName["d"]="DOUBLE"

numeric digits 20
   values=.directory~new
      -- UTF-8: must not contain 8-Bit-characters or
   -- values["s"]=.array~of("", "hi", "aha",    , .nil, "anton", string.to.utf8("über"), "berta")
   values["s"]=.array~of("hi", "aha", "",    , .nil, "anton", "ueber", "berta")
   values["g"]=.array~of("", "s",  "g",   "o", "oas", ,       .nil,                   "ay")
   values["o"]=.array~of("/", "/Aha", "/org/Aha", , .nil, "/org/aha/soso/Aha")

   -- values["y"]=.array~of(" ", "a", "A", , .nil, "U")  -- byte
   -- values["y"]=.array~of(" ", "a", "A", , .nil, "Ü")     -- byte
   -- values["y"]=.array~of(" ", "a", "A", , .nil, "U")     -- byte

   values["y"]=.array~of(" ", , "a", "A", .nil, "U")     -- byte
   -- values["y"]=" aA U" -- byte

   values["b"]=.array~of(.true, .false, , .nil, 0, 1)    -- boolean
   -- values["n"]=.array~of(0, -1, , .nil, 1)               -- int16
   values["n"]=.array~of(0, -1, , .nil, 1, -32768, 32767) -- int16
   values["q"]=.array~of(0,     , .nil, 1, 65535)        -- uint16

   values["i"]=.array~of(0, -1, , .nil, 1, -2147483648, 2147483647)  -- int32
   values["h"]=.array~of(0,     , .nil, 1)               -- uint32: UNIX_FD: dbus does alter value
   values["u"]=.array~of(0,     , .nil, 1, 4294967295)   -- uint32: tests o.k.

   values["x"]=.array~of(0,     , .nil, 1, -9223372036854775808, 9223372036854775807)  -- int64

   values["t"]=.array~of(0,     , .nil, 1, 18446744073709551615)  -- uint64

   values["d"]=.array~of(0, -1, , .nil, 1)               -- double

   conn~makeReplySlotDir=.true

   say "testing with empty single-dimensioned arrays ..."
   width=30
   arr=.array~new
   yyy=4 -- 4=byte
   do k=1 to order~size --  yyy -- order~size -- items -- type over order
   -- do k=yyy to yyy --- order~size --  yyy -- order~size -- items -- type over order
      type=order[k]
      say center("" type"/"typeName[type] "", width, "-")
      -- arr=values[type]  -- get values to send

      signature="a"type
      if arr~isA(.array) then
         oriValue= pp2(serializeArray(arr))    -- arr~makeString("L",","))
      else
         oriValue= pp2(arr)

      say "---" oriValue "signature="pp2(signature)

      resRaw=conn~message("call", busName, objectPath, interface, methodName"_"signature, signature, signature, arr)
      res=resRaw[1]     -- result value
      slotDir=resRaw[2]
      call dump2 slotDir "reply's slotDir for" pp2(signature)


      if \res~isA(.array) then
         say "===" oriValue "<--?-->" "res="pp2(res) "(did not return an array!)"
      else
         say "===" oriValue "<--?-->" "res="pp2(serializeArray(res))    -- res~makeString("L",","))

      say "~"~copies(10)

   end


   say "---"
/*
say "hit enter to continue..."
parse pull key
*/

   say "now testing with single-dimensioned arrays with values..."
   conn~makeReplySlotDir=.true      -- return an array with two elements: arr[1]=result, arr[2]=slotDir

   width=30
   yyy=4    -- 4=byte
   do k=1 to order~size --  yyy -- order~size -- items -- type over order
   -- do k=yyy to yyy --- order~size --  yyy -- order~size -- items -- type over order
      type=order[k]
      say center("" type"/"typeName[type] "", width, "-")
      arr=values[type]  -- get values to send

      signature="a"type
      if arr~isA(.array) then
         oriValue= pp2(serializeArray(arr))    -- arr~makeString("L",","))
      else
         oriValue= pp2(arr)


      say "---" oriValue "signature="pp2(signature)


      resRaw=conn~message("call", busName, objectPath, interface, methodName"_"signature, signature, signature, arr)
      res=resRaw[1]     -- result value
      slotDir=resRaw[2]
      call dump2 slotDir "reply's slotDir for" pp2(signature)


      if \res~isA(.array) then
         say "===" oriValue "<--?-->" "res="pp2(res) "(did not return an array!)"
      else
         say "===" oriValue "<--?-->" "res="pp2(serializeArray(res))   -- res~makeString("L",","))

      say "~"~copies(10)

   end

   say "-"~copies(79)


::routine test2dimensionalArrayTypes
   use strict arg conn, busName, objectPath, interface="", methodName="someMethodName"


   say center(" test2dimensionalArrayTypes ", 50, "-")

   if conn~query("typecode", "h"), .client.unix_Fd<>.false then    -- does the connection support UNIX_FD's ?
      order=.array~of("s","g","o","y","b","n","q","i","h","u","x","t","d")
   else
      order=.array~of("s","g","o","y","b","n","q","i","u","x","t","d")

   typeName=.directory~new
   typeName["s"]="STRING"
   typeName["g"]="SIGNATURE"
   typeName["o"]="OBJECTPATH"
   typeName["y"]="BYTE"
   typeName["b"]="BOOLEAN"
   typeName["n"]="INT16"
   typeName["q"]="UINT16"
   typeName["i"]="INT32"
   typeName["h"]="UNIX_FD"
   typeName["u"]="UINT32"
   typeName["x"]="INT64"
   typeName["t"]="UINT64"
   typeName["d"]="DOUBLE"

numeric digits 20
   values=.directory~new
      -- UTF-8: must not contain 8-Bit-characters or

   arr1=.array~new
   arr1[1,1]="/OneOne"           -- attention: needs to be also a syntactical correct object path!
   arr1[3,2]="/ThreeTwo"         -- attention: needs to be also a syntactical correct object path!
   values["s"]=arr1  -- string

   arr2=.array~new
   arr2[1,1]="s"
   arr2[3,2]="s"
   values["g"]=arr2  -- signature

   values["o"]=arr1  -- objectpath

   arr3=.array~new   -- byte
   arr3[1,1]=1
   arr3[3,2]=1
   values["y"]=arr3

   values["b"]=arr3  -- boolean
   values["n"]=arr3  -- int16
   values["q"]=arr3  -- uint16

   values["i"]=arr3  -- int32
   values["h"]=arr3  -- uint32: UNIX_FD: dbus does alter value
   values["u"]=arr3  -- uint32: tests o.k.

   values["x"]=arr3  -- int64

   values["t"]=arr3  -- uint64

   values["d"]=arr3  -- double

   conn~makeReplySlotDir=.true      -- return an array with two elements: arr[1]=result, arr[2]=slotDir

   width=30
   yyy=4 -- 4=byte
   do k=1 to order~size --  yyy -- order~size -- items -- type over order
   -- do k=yyy to yyy --- order~size --  yyy -- order~size -- items -- type over order
      type=order[k]
      say center("" type"/"typeName[type] "", width, "-")
      arr=values[type]  -- get values to send

      signature="aa"type
      if arr~isA(.array) then
         oriValue= pp2(arr~makeString("L",",")) "d="arr~dimension"/i="arr~items"/s="arr~size
      else
         oriValue= pp2(arr)

      say "---" oriValue "signature="pp2(signature)

      resRaw=conn~message("call", busName, objectPath, interface, methodName"_"signature, signature, signature, arr)
      res=resRaw[1]     -- result value
      slotDir=resRaw[2]
      call dump2 slotDir "reply's slotDir for" pp2(signature)


      if \res~isA(.array) then
         say "===" oriValue "<--?-->" "res="pp2(res) "(did not return an array!)"
      else
      do
         say "===" oriValue "<--?-->" "res="pp2(res~makeString("L",",")) "d="arr~dimension"/i="arr~items"/s="arr~size
         say "original array arr:"
         call dump2dArray arr
         say "res-array:"
         call dump2dArray res
         say "-- after dump2darray res."
      end

      say "~"~copies(10)
   end
   say "-"~copies(79)
   return


dump2dArray: procedure
  use arg arr
say "---> debug dump2dArray(): arr~dimension="arr~dimension "arr~items="arr~items

  -- idx=.array~new(arr~dimension)
  width=15
  do i=1 to arr~dimension(1)
     do k=1 to arr~dimension(2)
        .output~~charout(i","k": ")~~charout(arr[i,k]~string~left(width))~~charout( " | " )
     end
     say
  end
  return




::routine test3dimensionalArrayTypes
   use strict arg conn, busName, objectPath, interface="", methodName="someMethodName"

   say center(" test3dimensionalArrayTypes ", 50, "-")

   if conn~query("typecode", "h"), .client.unix_Fd<>.false then    -- does the connection support UNIX_FD's ?
      order=.array~of("s","g","o","y","b","n","q","i","h","u","x","t","d")
   else
      order=.array~of("s","g","o","y","b","n","q","i","u","x","t","d")

   typeName=.directory~new
   typeName["s"]="STRING"
   typeName["g"]="SIGNATURE"
   typeName["o"]="OBJECTPATH"
   typeName["y"]="BYTE"
   typeName["b"]="BOOLEAN"
   typeName["n"]="INT16"
   typeName["q"]="UINT16"
   typeName["i"]="INT32"
   typeName["h"]="UNIX_FD"
   typeName["u"]="UINT32"
   typeName["x"]="INT64"
   typeName["t"]="UINT64"
   typeName["d"]="DOUBLE"

numeric digits 20
   values=.directory~new
      -- UTF-8: must not contain 8-Bit-characters or

   arr1=.array~new
   arr1[1,1,1]="/OneOneOne"            -- attention: needs to be also a syntactical correct object path!
   arr1[2,2,2]="/TwoTwoTwoe"           -- attention: needs to be also a syntactical correct object path!
   arr1[3,2,3]="/ThreeTwoThree"        -- attention: needs to be also a syntactical correct object path!
   arr1[3,3,3]="/ThreeThreeThree"      -- attention: needs to be also a syntactical correct object path!
   values["s"]=arr1  -- string

   arr2=.array~new
   arr2[1,1,1]="s"
   arr2[2,2,2]="s"
   arr2[3,2,3]="s"
   arr2[3,3,3]="s"
   values["g"]=arr2  -- signature

   values["o"]=arr1  -- objectpath

   arr3=.array~new   -- byte
   arr3[1,1,1]=1
   arr3[2,2,2]=1
   arr3[3,2,3]=1
   arr3[3,3,3]=1
   values["y"]=arr3

   values["b"]=arr3  -- boolean
   values["n"]=arr3  -- int16
   values["q"]=arr3  -- uint16

   values["i"]=arr3  -- int32
   values["h"]=arr3  -- uint32: UNIX_FD: dbus does alter value
   values["u"]=arr3  -- uint32: tests o.k.

   values["x"]=arr3  -- int64

   values["t"]=arr3  -- uint64

   values["d"]=arr3  -- double

   conn~makeReplySlotDir=.true      -- return an array with two elements: arr[1]=result, arr[2]=slotDir


   width=30
   yyy=4    -- 4=byte
   do k=1 to order~size --  yyy -- order~size -- items -- type over order
   -- do k=yyy to yyy --- order~size --  yyy -- order~size -- items -- type over order
      type=order[k]
      say center("" type"/"typeName[type] "", width, "-")
      arr=values[type]  -- get values to send

      signature="aaa"type
      if arr~isA(.array) then
         oriValue= pp2(arr~makeString("L",",")) "d="arr~dimension"/i="arr~items"/s="arr~size
      else
         oriValue= pp2(arr)

      say "---" oriValue "signature="pp2(signature)

      resRaw=conn~message("call", busName, objectPath, interface, methodName"_"signature, signature, signature, arr)
      res=resRaw[1]     -- result value
      slotDir=resRaw[2]
      call dump2 slotDir "reply's slotDir for" pp2(signature) "res:" pp2(res)


      if \res~isA(.array) then
         say "===" oriValue "<--?-->" "res="pp2(res) "(did not return an array!)"
      else
      do
         say "===" oriValue "<--?-->" "res="pp2(res~makeString("L",",")) "d="arr~dimension"/i="arr~items"/s="arr~size
         say "original:"
         say
         call dump3dArray arr
         say
         say "returned:"
         say
         call dump3dArray res
      end

      say "~"~copies(10)
   end
   say "-"~copies(79)
   return


dump3dArray: procedure
  use arg arr

say "---> debug dump3dArray(): arr~dimension="arr~dimension "arr~items="arr~items
  -- idx=.array~new(arr~dimension)
  width=15
  do i=1 to arr~dimension(1)
     do k=1 to arr~dimension(2)
        do m=1 to arr~dimension(3)
           .output~~charout(i","k","m": ")~~charout(arr[i,k,m]~string~left(width))~~charout( " | " )
        end
        say
     end
     say
  end
  return

/*
dump3dArray: procedure
   use arg arr

   supp=arr~supplier
   do while supp~available
      say supp~index~makestring("L",",")":" supp~item
      supp~next
   end
   return
*/


::routine testMapType -- i.e. .directory

   use strict arg conn, busName, objectPath, interface="", methodName="someMethodName"
say center(" testMapTypes ", 50, "-")


   say "conn="pp2(conn) "busName="pp2(busName) "objectPath="pp2(objectPath)
   say "interface="pp2(interface) "methodName="pp2(methodName)
/*
say center('conn~query("typecode", "h")='pp2(conn~query("typecode", "h")), 100, '*')
say "supported typecodes:" pp2(conn~query("typecode"))
say "hit enter"
parse pull x
*/

   if conn~query("typecode", "h"), .client.unix_Fd<>.false then    -- does the connection support UNIX_FD's ?
      order=.array~of("s","g","o","y","b","n","q","i","h","u","x","t","d")
   else
      order=.array~of("s","g","o","y","b","n","q","i","u","x","t","d")

   typeName=.directory~new
   typeName["s"]="STRING"
   typeName["g"]="SIGNATURE"
   typeName["o"]="OBJECTPATH"
   typeName["y"]="BYTE"
   typeName["b"]="BOOLEAN"
   typeName["n"]="INT16"
   typeName["q"]="UINT16"
   typeName["i"]="INT32"
   typeName["h"]="UNIX_FD"
   typeName["u"]="UINT32"
   typeName["x"]="INT64"
   typeName["t"]="UINT64"
   typeName["d"]="DOUBLE"

numeric digits 20
   values=.directory~new
      -- UTF-8: must not contain 8-Bit-characters or
   values["s"]=.array~of("", "hi", "aha", "anton", "ueber", "berta")
   values["g"]=.array~of("s",  "g",   "o", "oas", "ay")
   values["o"]=.array~of("/", "/Aha", "/org/Aha", "/org/aha/soso/Aha")

   values["y"]=.array~of(" ", "a", "A", "Ü")     -- byte

   values["b"]=.array~of(.true, .false, 0, 1)    -- boolean
   values["n"]=.array~of(0, -1, , .nil, 1, -32768, 32767) -- int16
   values["q"]=.array~of(0,  1, 65535)        -- uint16

   values["i"]=.array~of(0, -1, , .nil, 1, -2147483648, 2147483647)  -- int32
   values["h"]=.array~of(0,  1)               -- uint32: UNIX_FD: dbus does alter value
   values["u"]=.array~of(0,  1, 4294967295)   -- uint32: tests o.k.

   values["x"]=.array~of(0,  1, -9223372036854775808, 9223372036854775807)  -- int64

   values["t"]=.array~of(0,  1, 18446744073709551615)  -- uint64

   values["d"]=.array~of(0, -1, 1)               -- double

   indices=.array~of("idx1", "idx2", "idx3", "idx4", "idx5", "idx6", "idx7", "idx8")

   conn~makeReplySlotDir=.true      -- return an array with two elements: arr[1]=result, arr[2]=slotDir

   width=30
   yyy=2
   do k=1 to order~size --  yyy -- order~size -- items -- type over order
--   do k=yyy to yyy -- order~size -- items -- type over order

      type="a{s"order[k]"}"      -- map type

      typeByOrder=order[k]

      say center("" type"/"typeName[typeByOrder] "", width, "-")

      arr=values[typeByOrder]  -- get values to send
      val=.directory~new

      size=arr~size -- items
      len=size~length
      do i=1 to arr~size
         val[indices[i]]=arr[i]     -- assign entries
      end

      say "arr~items:" arr~items "== dir~items:" val~items "?"

-- call dump2 val, "original map"

--         say "***" i~right(len)"/"size~right(len)":" pp2(val) pp2(val~string~c2x)
      tmpMethName=methodName"_dict_s"typeByOrder
      -- res=conn~message("call", busName, objectPath, interface, '"'methodName"_"type'"', type, type, val)
      res=conn~message("call", busName, objectPath, interface, tmpMethName, type, type, val)

      call dump2 val, "original map"
      call dump2 res[1], "resulting map"
      -- say "===" i~right(len)"/"size~right(len)":" pp2(val) "<--?-->" "res="pp2(res) "|" pp2(val~string~c2x) "<--?-->"  pp2(res~string~c2x)

      say "-"~copies(width)
   end

   say "-"~copies(79)
   return



::routine testStructTypes1 -- i.e. .directory

   use strict arg conn, busName, objectPath, interface="", methodName="someMethodName"
say center(" testStructTypes1 ", 50, "-")


   say "conn="pp2(conn) "busName="pp2(busName) "objectPath="pp2(objectPath)
   say "interface="pp2(interface) "methodName="pp2(methodName)

   signatures1=.array~of( "(s)",          - -- #1 - 1 argument (an array)
                          "(ss)")           -- #2 - 1 argument (an array)

   signatures3=.array~of( "s(s)s",        - -- #3 - 3 arguments (string, array, string)
                          "s(ss)s",       - -- #4 - 3 arguments (string, array, string)
                          "s(s(s)s)s",    - -- #5 - 3 arguments (string, array, string)
                          "s(ss(ss)ss)s")   -- #6 - 3 arguments (string, array, string)

   values1=.array~of( .array~of("hi"),    -     -- #1
                      .array~of("abc","def"))   -- #2

   -- make sure only three elements in first array !
   values3=.array~of( .array~of("abc", .array~of("mno"), "def"),                    - -- #3
                      .array~of("abc", .array~of("mno", "pqr"), "def"),                   - -- #4
                      .array~of("abc", .array~of("mno", .array~of("uvw"), "pqr"), "def"), - -- #5
                      .array~of("abc", .array~of("mno", "123", .array~of("uvw", "xyz"), "pqr", "456"), "def") - -- #6
                )

   conn~makeReplySlotDir=.true      -- return an array with two elements: arr[1]=result, arr[2]=slotDir

   width=30
   yyy=99
   -- do k=yyy to signatures1~size --  yyy -- order~size -- items -- type over order
   do k=1 to signatures1~size --  yyy -- order~size -- items -- type over order
   -- do k=yyy to yyy --  yyy -- order~size -- items -- type over order

      type=signatures1[k]
      val =values1[k]

      say center("" type "", width, "-")

      tmpMethName=methodName"_"k

      res=conn~message("call", busName, objectPath, interface, tmpMethName, type, type, val)

      call dump2 val, "original struct"
      call dump2 res[1], "resulting struct"

      say "-"~copies(width)
   end

say center(" testStructTypes1 with three args ", 50, "-")
   do k=1 to signatures3~size --  yyy -- order~size -- items -- type over order
   -- yyy=4
   -- do k=yyy to yyy --  yyy -- order~size -- items -- type over order

      type=signatures3[k]
      val =values3[k]

      say center("" type "", width, "-")

      tmpMethName=methodName"_"k

      res=conn~message("call", busName, objectPath, interface, tmpMethName, type, type, val[1], val[2], val[3])

      call dump2 val, "original struct"
      call dump2 res[1], "resulting struct"

      say "-"~copies(width)
   end


   say "-"~copies(79)


   return



::routine testVariantTypes1 -- i.e. .directory

   use strict arg conn, busName, objectPath, interface="", methodName="someMethodName"
say center(" testVariantTypes1 ", 50, "-")


   say "conn="pp2(conn) "busName="pp2(busName) "objectPath="pp2(objectPath)
   say "interface="pp2(interface) "methodName="pp2(methodName)

   signatures1=.array~of( "v")   -- #1 - 1 argument (an array)
   values1=.array~of( "hi")      -- #1


   signatures2=.array~of( "vv",         - -- # 1 - 2 argument (an array)
                          "vv",         - -- # 2 - 2 argument (an array)
                          "vv")           -- # 3 - 2 argument (an array)

   arr3=.array~new
   arr3[1,1,1]="/OneOneOne"            -- attention: needs to be also a syntactical correct object path!
   arr3[2,2,2]="/TwoTwoTwoe"           -- attention: needs to be also a syntactical correct object path!
   arr3[3,2,3]="/ThreeTwoThree"        -- attention: needs to be also a syntactical correct object path!
   arr3[3,3,3]="/ThreeThreeThree"      -- attention: needs to be also a syntactical correct object path!

   values2=.array~of( .array~of("abc","def"),    -  -- #1
                      .array~of("abc", .array~of("def", "ghi")), - -- #2
                      .array~of(.array~of("def", "ghi"), arr3))   -- #3


   width=30

--
   type="v"
   val ="hi"

   say center("" type "", width, "-")

   tmpMethName=methodName"_"type

   res=conn~message("call", busName, objectPath, interface, tmpMethName, type, type, val)

   call dump2 val, "original variant"
   call dump2 res, "resulting variant"

   say "-"~copies(width)
   say

---
   conn~makeReplySlotDir=.false     -- return an array with two elements: arr[1]=result, arr[2]=slotDir
say center(" testVariantTypes1 with two args ", 50, "-")
   do k=1 to signatures2~size --  yyy -- order~size -- items -- type over order
   -- yyy=4
   -- do k=yyy to yyy --  yyy -- order~size -- items -- type over order

      type=signatures2[k]
      val =values2[k]
--       call dump2 val, "original variant"

      say center("" type "", width, "-")

      tmpMethName=methodName"_"k

      res=conn~message("call", busName, objectPath, interface, tmpMethName, type, type, val[1], val[2])

      call dump2 val, "original variant"
      call dump2 res, "resulting variant"

      say "-"~copies(width)
   end
   say

--
   type="v"
   val=.directory~new
   val["eins"]="One"
   val~zwei="Two"

   say center("" type "", width, "-")

   tmpMethName=methodName"_"type

   res=conn~message("call", busName, objectPath, interface, tmpMethName, type, type, val)

   call dump2 val, "original variant"
   call dump2 res, "resulting variant"

   say "-"~copies(width)



   say "-"~copies(79)


   return



::routine testVariantTypes2 -- i.e. .directory, 2011-07-29: test new variantSignatures !

   use strict arg conn, busName, objectPath, interface="", methodName="someMethodName"
say center(" testVariantTypes2 ('useThisSignature=') / dbus.box(...) ", 79, "-")


   say "conn="pp2(conn) "busName="pp2(busName) "objectPath="pp2(objectPath)
   say "interface="pp2(interface) "methodName="pp2(methodName)

   signatures1=.array~of( "v")   -- #1 - 1 argument (an array)
   values1=.array~of( "hi")      -- #1


   signatures2=.array~of( "vv",         - -- # 1 - 2 argument (an array)
                          "vv",         - -- # 2 - 2 argument (an array)
                          "vv")           -- # 3 - 2 argument (an array)

   arr3=.array~new
   arr3[1,1,1]="/OneOneOne"            -- attention: needs to be also a syntactical correct object path!
   arr3[2,2,2]="/TwoTwoTwoe"           -- attention: needs to be also a syntactical correct object path!
   arr3[3,2,3]="/ThreeTwoThree"        -- attention: needs to be also a syntactical correct object path!
   arr3[3,3,3]="/ThreeThreeThree"      -- attention: needs to be also a syntactical correct object path!

   values2=.array~of( .array~of("abc","def"),    -  -- #1
                      .array~of("abc", .array~of("/def", "/ghi")), - -- #2
                      .array~of(.array~of("def", "ghi"), arr3))   -- #3

/*
   values2_new= .array~of( -
         .array~of( .array~of("useThisSignature=s", "abc"),                        - -- #1
                    .array~of("useThisSignature=o", "/def")),                      -
         .array~of(                                                                - -- #2
                   .array~of("useThisSignature=s",  "abc"),                        -
                   .array~of("useThisSignature=ao", .array~of("/def", "/ghi"))),   -
         .array~of(                                                                - -- #3
                   .array~of("useThisSignature=as",   .array~of("def", "ghi")),    -
                   .array~of("useThisSignature=aaas", arr3))                       -
                        )
*/
   values2_new= .array~of( -
         .array~of( dbus.box("s", "abc"),                        - -- #1
                    dbus.box("o", "/def")),                      -
         .array~of(                                                                - -- #2
                   dbus.box("s",  "abc"),                        -
                   dbus.box("ao", .array~of("/def", "/ghi"))),   -
         .array~of(                                                                - -- #3
                   dbus.box("as",   .array~of("def", "ghi")),    -
                   dbus.box("aaas", arr3))                       -
                        )

      -- alternatively (easier, more ledgible):
   values2_new= .array~of( -
         .array~of( dbus.box("s", "abc"), dbus.box("o", "/def")),                     - -- #1
         .array~of( dbus.box("s", "abc"), dbus.box("ao", .array~of("/def", "/ghi"))), - -- #2
         .array~of( dbus.box("as",.array~of("def", "ghi")), dbus.box("aaas", arr3))   - -- #3                      -
                        )

   width=30

--
   type="v"
   val ="hi"

   say center("" type "", width, "-")

   tmpMethName=methodName"_"type

   -- val_new=.array~of("useThisSignature=s",val)
   val_new=dbus.box("s",val)
   res=conn~message("call", busName, objectPath, interface, tmpMethName, type, type, val_new)

   call dump2 val, "original variant"
   call dump2 res, "resulting variant"
   say "---"

   say "-"~copies(width)
   say


---
   conn~makeReplySlotDir=.false     -- return an array with two elements: arr[1]=result, arr[2]=slotDir
say center(" testVariantTypes2 with two args ", 50, "-")
   do k=1 to signatures2~size --  yyy -- order~size -- items -- type over order
   -- yyy=4
   -- do k=yyy to yyy --  yyy -- order~size -- items -- type over order

      type=signatures2[k]
      val =values2[k]
--       call dump2 val, "original variant"

      say center("" type "", width, "-")

      tmpMethName=methodName"_"k

      res=conn~message("call", busName, objectPath, interface, tmpMethName, type, type, val[1], val[2])

      call dump2 val, "original variant ('val')"
      call dump2 res, "resulting variant"

      say "---"   -- now the same with explicit variantSignatures
      val_new=values2_new[k]
      res=conn~message("call", busName, objectPath, interface, tmpMethName, type, type, val_new[1], val_new[2])
      call dump2 val_new, "original variant ('val_new')"
      call dump2 res, "resulting variant"

      say "-"~copies(width)
   end
   say

--
   type="v"
   val=.directory~new
   val["eins"]="One"
   val~zwei="/Two"

   say center("" type "", width, "-")

   tmpMethName=methodName"_"type

   res=conn~message("call", busName, objectPath, interface, tmpMethName, type, type, val)

   call dump2 val, "original variant"
   call dump2 res, "resulting variant"

   say "-"~copies(width)

   say "---"   -- now the same with explicit variantSignatures

   tmpDir=.directory~new
   -- tmpDir["eins"]=.array~of("variantSignature=s", "One")
   tmpDir["eins"]="One"
   -- tmpDir~zwei   =.array~of("useThisSignature=o", "/Two")   -- define variant signature explicitly
   tmpDir~zwei   =dbus.box("o", "/Two")   -- define variant signature explicitly

   -- new_val=.array~of("useThisSignature=a{sv}", tmpDir)      -- create argument with explicit variant signature
   new_val=dbus.box("a{sv}", tmpDir)      -- create argument with explicit variant signature
   res=conn~message("call", busName, objectPath, interface, tmpMethName, type, type, new_val)

   call dump2 val, "original variant ('val_new')"
   call dump2 res, "resulting variant"


   say "-"~copies(79)


   return



::routine testByteArrays -- i.e. .directory

   use strict arg conn, busName, objectPath, interface="", methodName="someMethodName"
say center(" testByteArrays ", 50, "-")

   conn~makeReplySlotDir=.false     -- just return the result value

   width=60

/*
*/
   -- first "ay"
   arr1=.array~of("H", "e", "l", "l", "o", " ", "W", "o", "r", "l", "d", "!", "tsk")
   val1="Hello, World!"

   type="ay"
   -- tmpMethName=methodName"_"type
/*
*/
   say center("" type "(return as string) ", width, "-") "hence returned type:" pp2("s")
      -- supplying an array of bytes
   conn~unmarshalByteArrayAsString=.true     -- return as string
   res=conn~message("call", busName, objectPath, interface, tmpMethName"_"type, type, type, arr1)
   say "ori:" pp2(arr1~makeString('C')) "<--> res:" pp2(res)
   say "="~copies(width)
   say

   say center("" type "(return as array) ", width, "-")
   conn~unmarshalByteArrayAsString=.false    -- return as an array of bytes!
   res=conn~message("call", busName, objectPath, interface, tmpMethName"_"type, type, type, arr1)
   call dump2 arr1, "original arr1-array"
   say
   call dump2 res, "res-array (requested as array)"
   say "="~copies(width)
   say

      -- supplying a string to be transported as byte array
   say center("" type "(return as string) ", width, "-")  "hence returned type:" pp2("s")
   conn~unmarshalByteArrayAsString=.true     -- return as string
   res=conn~message("call", busName, objectPath, interface, tmpMethName"_"type, type, type, val1)
   say "ori:" pp2(val1) "<--> res:" pp2(res)
   say "="~copies(width)
   say

   say center("" type "(return as array) ", width, "-")
   conn~unmarshalByteArrayAsString=.false    -- return as an array of bytes!
   res=conn~message("call", busName, objectPath, interface, tmpMethName"_"type, type, type, val1)
   call dump2 val1, "original val1-string"
   say
   call dump2 res, "res-array (requested as array)"
   say
   say "="~copies(width)
   say


      -- 'aay'
   type='aaay'

   arr1=.array~of("a", "b", "c")
   arr3=.array~new
   call fill3dArray arr3, arr1      -- create and fill a 3-d-array object

   say center("" type "(return as string) ", width, "-")  "hence returned type:" pp2("aas")
   conn~unmarshalByteArrayAsString=.true     -- return as string
   res=conn~message("call", busName, objectPath, interface, tmpMethName"_"type, type, type, arr3)
   say "arr3-ori:"
   call dump3dArray arr3
   say
   say "res:"
   if res~dimension=2 then
   do
      call dump2dArray res
say "---> rgf-debug:"
s=res~supplier
do while s~available
    say " " pp2(s~index~makeString('L',','))": "pp2(s~item)
    s~next
end

   end
   else
      call dump3dArray res
   say "="~copies(width)
   say
/*
*/


   say center("" type "(return as array) ", width, "-")
   conn~unmarshalByteArrayAsString=.false    -- return as array
   res=conn~message("call", busName, objectPath, interface, tmpMethName"_"type, type, type, arr3)
   say "arr3-ori:"
   call dump3dArray arr3
   say
   say "res:"
   if res~dimension=2 then
      call dump2dArray res
   else
      call dump3dArray res
   say "="~copies(width)
   say
/*
*/


   say "="~copies(79)
   return

fill3dArray: procedure
   use arg arr,   -     -- 3-dimensional array object
           arrArg       -- single dimensioned array

   do idx over .list~of(.array~of(1,1), .array~of(2,2))
      x=idx[1]
      y=idx[2]
      do z=1 to arrArg~size
         arr[x,y,z]=arrArg[z]
      end
   end
   return





dump2dArray: procedure
  use arg arr
say "---> debug dump2dArray(): arr~dimension="arr~dimension "arr~items="arr~items

  -- idx=.array~new(arr~dimension)
  width=20
  do i=1 to arr~dimension(1)
     do k=1 to arr~dimension(2)
        .output~~charout(i","k": ")~~charout(arr[i,k]~string~left(width))~~charout( " | " )
     end
     say
  end
  return


dump3dArray: procedure
  use arg arr

say "---> debug dump3dArray(): arr~dimension="arr~dimension "arr~items="arr~items
  -- idx=.array~new(arr~dimension)
  width=20
  do i=1 to arr~dimension(1)
     do k=1 to arr~dimension(2)
        do m=1 to arr~dimension(3)
           .output~~charout(i","k","m": ")~~charout(arr[i,k,m]~string~left(width))~~charout( " | " )
        end
        say
     end
     say
  end
  return



::routine serializeArray
  use arg arr

  if arr~dimension>1 then return arr~makeString('L',',') "<hint: does not show empty slots>!"

  tmpStr=""
  deli=","
  bFirst=.true
  do k=1 to arr~size
     if arr~hasIndex(k) then val=arr[k]
                        else val=""
     if bFirst=.true then
     do
        tmpStr=val
        bFirst=.false
     end
     else
        tmpStr=tmpStr || deli || val
  end

  return tmpStr

