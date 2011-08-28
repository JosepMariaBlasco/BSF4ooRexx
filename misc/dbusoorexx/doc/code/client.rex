conn=.dbus~connect("session")    -- connect to the "session" bus

objectPath    ="/org/rexxla/oorexx/demo/Hello"
busName       ="org.rexxla.oorexx.demo.Hello"
interface     ="org.rexxla.oorexx.demo.Hello"

o=conn~getObject(busName, objectPath) -- get the DBus object
num1=12.345
num2=10.01
say num1"*"num2"="o~multiply(num1,num2)  -- use the multiply method

::requires "dbus.cls"         -- get dbus support for ooRexx
::requires "rgf_util2.rex"    -- installed with the BSF4ooRexx package
