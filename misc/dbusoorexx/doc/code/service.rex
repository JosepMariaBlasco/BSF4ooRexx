objectPath    ="/org/rexxla/oorexx/demo/Hello"
busName       ="org.rexxla.oorexx.demo.Hello"

conn=.dbus~session            -- get the session bus

signal on halt                -- intercept ctl-c (jump to label "halt:")

res=conn~busName("request", busName)   -- request a bus name
if res<>.dbus.dir~primaryOwner & res<>.dbus.dir~alreadyOwner then -- o.k., wait for clients?
do
   say "res="res "problem with requesting the bus name" busName", aborting ..."
   exit -1
end

service=.HelloRexxService~new -- create an instance of the Rexx service
conn~serviceObject("add", objectPath, service)  -- add service object to connection

say "Hit enter to stop server..."
parse pull answer

halt:                   -- a ctl-c causes a jump to this label
   say "closing connection."
   conn~close           -- close connection, stops message loop


::requires "dbus.cls"         -- get dbus support for ooRexx

::class HelloRexxService

::method Introspect        /* return the introspection data for this service object */
 return '<!DOCTYPE node PUBLIC "-//freedesktop//DTD D-BUS Object Introspection 1.0//EN"'  -
        '"http://www.freedesktop.org/standards/dbus/1.0/introspect.dtd">               '  -
        '<node>                                                                        '  -
        '  <interface name="org.freedesktop.DBus.Introspectable">                      '  -
        '    <method name="Introspect">                                                '  -
        '      <arg name="data" direction="out" type="s"/>                             '  -
        '    </method>                                                                 '  -
        '  </interface>                                                                '  -
        '  <interface name="org.rexxla.oorexx.demo.Hello">                             '  -
        '    <method name="Multiply">                                                  '  -
        '      <arg name="number1" direction="in"  type="d"/>                          '  -
        '      <arg name="number2" direction="in"  type="d"/>                          '  -
        '      <arg name="result"  direction="out" type="d"/>                          '  -
        '    </method>                                                                 '  -
        '  </interface>                                                                '  -
        '</node>                                                                       '

::method Multiply       -- implementation of "orx.rexxla.oorexx.demo.Hello.Multiply"
   use arg number1, number2, slotDir
   say "Multiply-request received: sender-bus=["slotDir~sender"]" "at=["slotDir~dateTime"]"
   return number1*number2

