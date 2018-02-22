@echo off
set jversion=-source 1.6 -target 1.6
echo compiling...
javac %jversion% -cp ..\..;. *.java
echo done, to run:
echo java -cp ..\..;. java6.plain.Test1
echo java -cp ..\..;. java6.plain.Test2
echo java -cp ..\..;.             UseTest2
