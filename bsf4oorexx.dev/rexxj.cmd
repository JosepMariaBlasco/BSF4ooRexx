@echo off
REM  created: created: "2015-08-15T19-52-58.498000" by user "Administrator"
REM  script to run "BSF4ooRexx" programs without prior installation ...

setlocal

REM  set BSF4REXX_HOME variable
set BSF4REXX_HOME=F:\work\svn\bsf4oorexx\trunk\bsf4oorexx.dev

REM  set PATH environment variable
set PATH=%BSF4REXX_HOME%;%PATH%

REM  set CLASSPATH environment variable
set CLASSPATH=.;%CLASSPATH%

REM  Java start command, allows passing arguments to Rexx script
"D:\WINDOWS\system32\java.exe"  -Djava.library.path="%BSF4REXX_HOME%" -classpath "%CLASSPATH%" %BSF4Rexx_JavaStartupOptions% org.rexxla.bsf.RexxDispatcher %*
