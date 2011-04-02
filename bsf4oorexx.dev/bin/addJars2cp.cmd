@echo off
rem set CLASSPATH=.;E:\rony\dev\bsf\src
set CLASSPATH=.
echo CLASSPATH=%CLASSPATH%
rem for %%i IN (*.jar) DO echo %i;SET CLASSPATH=%CLASSPATH%;%cd%\%%i
rem for %%i IN (*.jar) DO echo %%i ;SET CLASSPATH=%CLASSPATH%;%cd%\%%i
for %%i IN (*.jar) DO echo set CLASSPATH=%%CLASSPATH%%;%cd%\%%i

echo %CLASSPATH%
