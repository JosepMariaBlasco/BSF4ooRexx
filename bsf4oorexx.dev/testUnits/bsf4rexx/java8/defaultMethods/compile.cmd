@echo off
set jversion=-source 8 -target 8
echo compiling...
@echo on
javac %jversion% -cp ..\..;. *.java
