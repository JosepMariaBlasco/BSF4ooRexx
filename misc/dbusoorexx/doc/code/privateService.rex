   -- set up address and define default service to be added to client connections
address="tcp:host=localhost,port=23000,family=ipv4;"
defaultService =.HelloRexxService~new
server =.DBUSServer~new(address,.HelloRexxService~new)
server~startup          -- start up the private server

say "server~serverAddress:" pp2(server~serverAddress)

signal on halt          -- intercept ctl-c (jump to label "halt:")

say "Hit enter to stop server..."
parse pull answer

halt:                   -- a ctl-c causes a jump to this label
   say "shutdown server, closing all connections to clients..."
   server~shutdown      -- close connection, stops message loop


::requires "dbus.cls"   -- get dbus support for ooRexx

::class HelloRexxService subclass DBusServiceObject

::method init           -- constructor
   -- define introspect data; instead one could supply the full path to an introspect file
 idata= '<!DOCTYPE node PUBLIC "-//freedesktop//DTD D-BUS Object Introspection 1.0//EN"'  -
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
  self~init:super(idata)   -- let superclass (DBusServiceObject) initialize

::method Multiply          -- implementation of "orx.rexxla.oorexx.demo.Hello.Multiply"
   use arg number1, number2, slotDir
   say "Multiply-request received: sender-bus=["slotDir~sender"]" "at=["slotDir~dateTime"]"
   return number1*number2

