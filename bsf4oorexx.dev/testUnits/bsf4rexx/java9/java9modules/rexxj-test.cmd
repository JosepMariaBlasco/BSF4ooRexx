@echo off
REM  created: created: "2018-01-03T19-00-24.467000" by user "Administrator"
REM  script to run "BSF4ooRexx" programs without prior installation ...

setlocal

REM  set BSF4REXX_HOME variable
rem 20180210, rgf  set BSF4REXX_HOME=C:\Program Files (x86)\BSF4ooRexx

REM  set PATH environment variable
rem 20180210, rgf  set PATH=%BSF4REXX_HOME%;%PATH%

REM  set CLASSPATH environment variable
rem 20180210, rgf  set CLASSPATH=%BSF4REXX_HOME%\bsf4ooRexx-v600-20180101-bin.jar;%BSF4REXX_HOME%\jni4net.j-0.8.8.0.jar;%BSF4REXX_HOME%\oorexx.net.jar;.;%CLASSPATH%

set MODULEPATH=%cd%\out\test-modules-abc.jar


REM  Java start command, allows passing arguments to Rexx script
"C:\ProgramData\Oracle\Java\javapath\java.exe"  -Djava.library.path="%BSF4REXX_HOME%" -classpath "%CLASSPATH%" -p %MODULEPATH% %BSF4Rexx_JavaStartupOptions% org.rexxla.bsf.RexxDispatcher %*
