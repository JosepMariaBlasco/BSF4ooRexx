@echo off
rem 20171223, rgf - path to ooRexxUnit must be set prior to running the run* scripts
rem f:
rem cd F:\work\svn\oorexx\test\releases\4.0.0
rem rem point to root of testing framework and to the framework subdir
rem set PATH=%PATH%;%cd%;%cd%\framework
rem set PATH=%PATH%;%cd%;%cd%\ooRexxUnit

rem point to the root test cases for "bsf4rexx", needed for Java to find all Java classes compiled therein
set CLASSPATH=%CLASSPATH%;%cd%\bsf4rexx
