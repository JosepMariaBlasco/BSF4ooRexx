REM  created: created: "2010-08-29T00-08-45.358000" by user "Administrator"
REM  script to run "BSF4ooRexx" programs without prior installation ...

setlocal

REM  set BSF4REXX_HOME variable
rem set BSF4REXX_HOME=E:\rony\dev\bsf\src\BSF4ooRexx.dev
set BSF4REXX_HOME=%cd%

REM  set PATH environment variable
set PATH=%BSF4REXX_HOME%;%PATH%

REM  set CLASSPATH environment variable
set CLASSPATH=.;%CLASSPATH%

REM  Java start command, allows passing arguments to Rexx script
"D:\WINDOWS\system32\java.exe" -Djava.library.path="%BSF4REXX_HOME%" -classpath "%CLASSPATH%" org.rexxla.bsf.RexxDispatcher %*

endlocal
