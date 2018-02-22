@echo off
set jversion=-source 8 -target 8
echo compiling...
javac %jversion% -cp ..\..;. *.java
echo done, to run:
echo java -cp ..\..;. java8.plain.TestEnum
echo java -cp ..\..;. java8.plain.Test1
echo java -cp ..\..;. java8.plain.Test2
echo java -cp ..\..;.             UseTest2
