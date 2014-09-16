#!/usr/bin/rexx
/*
      author:  Rony G. Flatscher, (c) 2011
      name:    demoHelloClient.rex
      usage:   rexx demoHelloClient.rex
      purpose: - demonstrate how a Rexx program can interact with a dbus object, in
                 this case implemented in Rexx (cf. demoHelloService1.rex, demoHelloService2.rex,
                 demoHelloService3.rex)
               - demonstrates how to indicate that byte arrays should be unmarshalled as
                 plain Rexx strings
      needs:   - ooRexx dbus support package
      date:    2011-08-04
      changed: 2014-07-23, rgf: adapted logic to new infrastructure, hence a need to explicit stop
                                the message loop thread in order to allow ooRexx to stop
               2014-07-31, rgf: adjust to new DBusVersion() routine
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
say "DBusVersion():" DBusVersion()

conn=.dbus~connect("session")    -- connect to the "session" bus

objectPath    ="/org/rexxla/oorexx/demo/Hello"
busName       ="org.rexxla.oorexx.demo.Hello"
interface     ="org.rexxla.oorexx.demo.Hello"


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

if service>"RexxHelloService1" then
do
  if (service\="RexxHelloService4") then do   --sm: changed to use a new service
  originalValue=o~info
   say "This service has 'Info' and 'SetInfo' property methods:"
   say "   Querying 'Info' property:" originalValue
   str="*** From demoHelloClient at" pp2(.dateTime~new) "***"
   say "   Setting  'Info' property with 'SetInfo' ..."
   o~SetInfo(str)
   say "   ... waiting a bit to give the service a chance to set the new value!"
   call sysSleep .01
   say "   Querying 'Info' property:" o~info
   say "---"

   if service="RexxHelloService3" then
   do
      say "This service is able to treat a property as a readwrite property:"
      say "   Querying 'Info' property:" o~info
      say "   Resetting 'Info' property using 'Info(originalValue)' ..."
      o~info(originalValue)
      say "   Querying 'Info' property:" o~info
      say "---"
   end
   end
else do                                    --sm: demonstrates Get and Set
      say "demonstrates how to use Get and Set"
      say "   Querying 'Info' property with o~info:" o~info
      say "   Setting 'Info' property with o~info='test'" 
      o~info='test'
      say "   Querying 'Info' property with o~info:" o~info
      say "   Setting 'Info' property using 'Set(interface, info, anothervalue)' "
      say "   Could also use o~Set(interface, info, dbus.box('s','another value'))"
      o~Set("org.rexxla.oorexx.demo.Hello", "Info", dbus.box("s","another value"))
      say "   Querying 'Info' property with Get(interface, 'Info'):" -
      o~Get('org.rexxla.oorexx.demo.Hello', 'Info')
      say "---"
   end
end

conn~close           -- close, thereby terminating message loop thread

::requires "dbus.cls"         -- get dbus support for ooRexx
::requires "rgf_util2.rex"    -- installed with the BSF4ooRexx package