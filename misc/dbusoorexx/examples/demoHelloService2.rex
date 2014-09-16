#!/usr/bin/rexx
/*
      author:  Rony G. Flatscher, (c) 2011
      name:    demoHelloService2.rex
      usage:   rexx demoHelloService2.rex
      purpose: - demonstrate a dbus Rexx service object with its introspection data
                 defined within this program (cf. init-method)
               - the Rexx method return values will be marshalled according to the
                 introspection data
               - the program demonstrates emitting signals
      needs:   - ooRexx dbus support package
      date:    2011-08-04, 2011-09-12
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
timeout=30     -- wait for 30 seconds (1 minute) for clients, then stop

objectPath    ="/org/rexxla/oorexx/demo/Hello"
busName       ="org.rexxla.oorexx.demo.Hello"
interface     ="org.rexxla.oorexx.demo.Hello"

conn=.dbus~session         -- get the session bus

signal on syntax name halt -- make sure message loop gets stopped
signal on halt             -- intercept ctl-c or closing terminal in which Rexx runs

res=conn~busName("request", busName)
if res<>.dbus.dir~primaryOwner & res<>.dbus.dir~alreadyOwner then   -- o.k., wait for clients
do
   say "res="res "problem with requesting the bus name" busName", aborting ..."
   exit -1
end

o=.HelloRexxServer~new
conn~serviceObject("add", objectPath, o)
.IDBusPathMaker~publishAllServiceObjects(conn)  -- necessary for DBus debuggers
say "o="pp2(o)
say "-- ** -- "~copies(5)
o~service.sendSignal(objectPath, interface, "Start", "started at:" .dateTime~new~string)
say "sleeping" timeout "secs ..."
call syssleep timeout


halt:
   o~service.sendSignal(objectPath, interface, "Exit", "exited at:" .dateTime~new~string)
   conn~close           -- close, thereby terminating message loop thread
   say "after closing connection (stopping message loop) ..."
   exit -1


::requires "dbus.cls"         -- get dbus support for ooRexx
::requires "rgf_util2.rex"    -- installed with the BSF4ooRexx package

::class HelloRexxServer subclass DBusServiceObject

::method init           -- constructor
  expose info
  use arg objectPath

  info=self": created at" .dateTime~new

   -- introspection data (could also be the name of a file that contains the following data)
  idata='<!DOCTYPE node PUBLIC "-//freedesktop//DTD D-BUS Object Introspection 1.0//EN"'  -
        '"http://www.freedesktop.org/standards/dbus/1.0/introspect.dtd">               '  -
        '<node>                                                                        '  -
        '  <interface name="org.freedesktop.DBus.Introspectable">                      '  -
        '    <method name="Introspect">                                                '  -
        '      <arg name="data" direction="out" type="s"/>                             '  -
        '    </method>                                                                 '  -
        '  </interface>                                                                '  -
        '  <interface name="org.rexxla.oorexx.demo.Hello">                             '  -
        '    <method name="Hello">                                                     '  -
        '      <arg name="result" direction="out" type="s"/>                           '  -
        '    </method>                                                                 '  -
        '    <method name="Hello1">                                                    '  -
        '      <arg name="result" direction="out" type="ay"/>                          '  -
        '    </method>                                                                 '  -
        '    <method name="Hello2">                                                    '  -
        '      <arg name="result" direction="out" type="as"/>                          '  -
        '    </method>                                                                 '  -
        '    <method name="Hello3">                                                    '  -
        '      <arg name="result" direction="out" type="ao"/>                          '  -
        '    </method>                                                                 '  -
        '    <method name="Hello4">                                                    '  -
        '      <arg name="result" direction="out" type="a{ss}"/>                       '  -
        '    </method>                                                                 '  -
        '    <signal name="Start">                                                     '  -
        '      <arg name="result" type="s"/>                                            '  -
        '    </signal>                                                                 '  -
        '    <signal name="Exit">                                                      '  -
        '      <arg name="result" type="s"/>                                            '  -
        '    </signal>                                                                 '  -
        '    <property name="ServiceName" access="read"  type="s"/>                    '  -
        '    <property name="Info" access="read"  type="s"/>                           '  -
        '    <property name="SetInfo" access="write" type="s"/>                        '  -
        '  </interface>                                                                '  -
        '</node>                                                                       '

  self~init:super(idata)  -- let DBusServiceObject initialize

::method serviceName       /* the name of this service object           */
  return "RexxHelloService2"

::method info        /* the property get method                         */
  expose info
  return info

::method setInfo     /* the property set method                         */
  expose info
  use arg info

::method Hello       /* the service method 'Hello' returns a string     */
  return "This is a 'Hello' from Rexx at" .dateTime~new"."

::method Hello1      /* the service method 'Hello1' returns a byte array
                        (cf. signature in introspection data above)     */
  return "This is a 'Hello' from Rexx at" .dateTime~new"."

::method Hello2      /* the service method 'Hello2' returns an array    */
  return .array~of("eins", "zwei", "drei")

::method Hello3      /* the service method 'Hello3' returns an array, forces it to
                        be marshalled as 'ao' (array of object paths)   */
  return .array~of("/eins", "/zwei", "/drei")

::method Hello4      /* the service method 'Hello4' returns a directory ("map"/"dict") */
  dict=.directory~new
  dict~eins="one"
  dict~zwei="two"
  dict~drei="three"
  return dict

