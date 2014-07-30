#!/usr/bin/rexx
/*
      author:  Rony G. Flatscher, (c) 2011
      name:    testErroneousClient.rex
      usage:   rexx testErroneousClient.rex
      purpose: - test the errors the service object can raise
      needs:   - ooRexx dbus support package
      date:    2011-08-03/04
      changes:    2014-07-24, rgf: explicitly close connection to stop message loop thread
      specs:   dbus-specifications: <http://dbus.freedesktop.org/doc/dbus-specification.html> (as of 2011-07-14)
      license: AL 2.0 (Apache License 2.0)

            ------------------------ Apache Version 2.0 license -------------------------
               Copyright 2011-2014 Rony G. Flatscher

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

conn=.dbus~connect("session")    -- connect to the "session" bus

objectPath    ="/org/rexxla/oorexx/test/Hello"
busName       ="org.rexxla.oorexx.test.errors"
interface     ="org.rexxla.oorexx.test.Hello"


o=conn~getObject(busName, objectPath) -- get the DBus object
service=o~ServiceName
say "Talking to the service" pp2(service)
say "o~Hello:" o~Hello
say

call dump2 o~Hello1, "o~Hello1 (returns a byte array, signature: 'ay')"
say
say "instructing oorexxdbus to treat 'ay' as 's': (conn~unmarshalByteArrayAsString=.true)"
conn~unmarshalByteArrayAsString=.true
say "o~Hello1 now received as a Rexx string:" pp2(o~Hello1)
say
say "---"

call dump2 o~Hello2, "o~Hello2"
call dump2 o~Hello3, "o~Hello3"
call dump2 o~Hello4, "o~Hello4"

originalValue=o~info
say "This service has 'Info' and 'SetInfo' property methods:"
say "   Querying 'Info' property:" originalValue
str="*** From RexxHelloClient at" pp2(.dateTime~new) "***"
say "   Setting  'Info' property with 'SetInfo' ..."
o~SetInfo(str)
say "   ... waiting a bit to give the service a chance to set the new value!"
call sysSleep .01
say "   Querying 'Info' property:" o~info
say "---"
say "o~proxy.introspectData:"
say "["o~proxy.introspectData"]"
say "---"
say "call IDbus.dumpIDBus o~proxy.introspectRootNode:"
say pp2(o~proxy.introspectRootNode)
call IDbus.dumpIDBus o~proxy.introspectRootNode
say "---"

say "now testing the errors..."
call testError2a   o
call testError2b   o
call testError2c   o
call testError2d   o
call testError1    o     -- this tears down the server

call dumpConnectionStatistics conn
conn~close

::requires "testDumpStats.rex"
::requires "dbus.cls"         -- get dbus support for ooRexx
::requires "rgf_util2.rex"    -- installed with the BSF4ooRexx package


::routine testError1
  use arg o
  signal on syntax
  name="testError1"
  say "in routine" name "..."
  o~send(name)
  return
syntax:
  co=condition("object")
  say name "- syntax condition:"  "co="pp2(co)
  -- say ppCondition2(co)
  call dump2 co~additional, "co~additional"
  say ppCondition2(co)
  say "---"
  return

::routine testError2a
  use arg o
  signal on syntax
  name="testError2a"
  say "in routine" name "..."
  o~send(name)
  say "in routine" name "..." "AFTER - returned ERROR message!!"
  return
syntax:
  co=condition("object")
  say name "- syntax condition:" "co="pp2(co)
  -- say ppCondition2(co)
  call dump2 co~additional, "co~additional"
  say "---"
  return

::routine testError2b
  use arg o
  signal on syntax
  name="testError2b"
  say "in routine" name "..."
say "testError2b ... 0"
  o~send(name)
say "testError2b ... 1"
  return
syntax:
  co=condition("object")
say "testError2b ... 3"
  say name "- syntax condition:" "co="pp2(co)
  -- say ppCondition2(co)
say "testError2b ... 4"
  call dump2 co~additional, "co~additional"
  say "---"
  return

::routine testError2c
  use arg o
  signal on syntax
  name="testError2c"
  say "in routine" name "..."
  o~send(name)
  return
syntax:
  co=condition("object")
  say name "- syntax condition:" "co="pp2(co)
  -- say ppCondition2(co)
  call dump2 co~additional, "co~additional"
  say "---"
  return

::routine testError2d
  use arg o
  signal on syntax
  name="testError2d"
  say "in routine" name "..."
  o~send(name)
  return
syntax:
  co=condition("object")
  say name "- syntax condition:" "co="pp2(co)
  -- say ppCondition2(co)
  call dump2 co~additional, "co~additional"
  say "---"
  return


