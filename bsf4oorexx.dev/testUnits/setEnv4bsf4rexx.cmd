@echo off
rem f:
rem cd F:\work\svn\oorexx\test\releases\4.0.0
rem rem point to root of testing framework and to the framework subdir
rem set PATH=%PATH%;%cd%;%cd%\framework
set PATH=%PATH%;%cd%;%cd%\ooRexxUnit

rem point to the root test cases for "bsf4rexx", needed for Java to find all Java classes compiled therein
set CLASSPATH=%CLASSPATH%;%cd%\bsf4rexx
