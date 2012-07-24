@echo off
rem settings for OpenOffice.org

rem Java URE classes
set ooo3ure=D:\Programme\OpenOffice.org 3\URE\java
set CLASSPATH=%CLASSPATH%;%ooo3ure%\juh.jar;%ooo3ure%\ridl.jar;%ooo3ure%\jurt.jar


rem Java OOo classes
set ooo3base=D:\Programme\OpenOffice.org 3\Basis\program\classes
set CLASSPATH=%CLASSPATH%;%ooo3base%\unoil.jar;%ooo3base%\ScriptFramework.jar


rem executables
set ooo3pgm=D:\Programme\OpenOffice.org 3\program

set CLASSPATH=%CLASSPATH%;%ooo3pgm%

set PATH=%PATH%;%ooo3pgm%

echo CLASSPATH=%CLASSPATH%
echo PATH=%PATH%
