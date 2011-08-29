address="tcp:host=localhost,port=23000,family=ipv4;"
conn=.dbus~connect(address)   -- connect to given address

objectPath    ="/org/rexxla/oorexx/demo/Hello"
busName       ="org.rexxla.oorexx.demo.Hello"
o=conn~getObject(busName, objectPath)     -- get the DBus object

num1=12.345
num2=10.01
say num1"*"num2"="o~multiply(num1,num2)   -- use the multiply method

::requires "dbus.cls"         -- get dbus support for ooRexx
