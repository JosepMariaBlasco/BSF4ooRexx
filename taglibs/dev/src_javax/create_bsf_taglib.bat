@echo off
rem 2020-09-08, rgf, Windows experiment
rem conf ... contains tld definition
rem src  ... contains source files
rem META-INF ... will get tld file
rem work ... will contain compiled Java programs from src
setlocal
rem clear attributes
rem cacls * /t /g everyone:f
cd dev
if not exist src_bsf    goto usage
if not exist src_jsr223 goto usage
if not exist %cd%\src_bsf\Expression.java goto usage
if not exist %cd%\src_bsf\Scriptlet.java  goto usage
if not exist %cd%\src_jsr223\Expression.java goto usage
if not exist %cd%\src_jsr223\Scriptlet.java  goto usage

set tgtDir=..
set jarname=%tgtDir%\javax.ScriptTagLibs.jar
set confDir=WEB-INF
rem make sure no leftovers

rd %confDir% /s /q
md %confDir%
copy src_bsf\*tld    %confDir%
copy src_jsr223\*tld %confDir%

copy src_bsf\*tld    %tgtDir%
copy src_jsr223\*tld %tgtDir%

echo before removing directories for compilation
dir

rd META-INF /s /q

rem md META-INF
rem copy %confDir%\* META-INF

rd work /s /q
md work

rem BSF4ooRexx' baseline is Java 1.6/6 as of fall 2020
:compile_src_bsf
javac -source 1.6 -target 1.6 src_bsf\*.java -d work
if %errorlevel% neq 0 goto compileError1

:compile_src_jsr223
javac -source 1.6 -target 1.6 src_jsr223\*.java -d work
if %errorlevel% neq 0 goto compileError2

jar -cvf %jarname% -C work %confDir%\* *

rem clean up
echo before removing directories for cleanup
dir
dir META-INF /s
echo removing META-INF
rd META-INF /s /q
rd work /s /q
echo removing confDir: %confDir%
rd %confDir% /s /q
goto done

:usage
  echo usage: you must run this script from a directory that contains Expression.java and Scriptlet.java in src_bsf and src_jsr223
  echo        your current directory: %cd%
  goto done

:compileError1
  echo compile error for src_bsf: javac set rc to %errorlevel%
  goto done

:compileError2
  echo compile error for jsr223: javac set rc to %errorlevel%
  goto done

:done
endlocal


