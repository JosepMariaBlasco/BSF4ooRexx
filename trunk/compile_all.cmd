@rem 2006-12-08, 2009-02-10, 2010-12-28, 2010-12-30, ---rgf
@rem needs junit (for BSF tests) and Xalan (for BSF-xslt-engine) on classpath
setlocal

rem TODO: for compiling the script provider need to add "ScriptFramework.jar" to CLASSPATH, e.g.
rem          D:\Programme\OpenOffice 4\program\classes\ScriptFramework.jar
rem       rgf, 2014-02-10


goto end_01
@echo setting environment to point to asm and janino explicitly ...
@rem ATTENTION: must change into directory such that %cd% there points to the right location!
cd jars\asm
call set2asm320.cmd
cd ..\janino
call set2janino.cmd
cd ..\..

:end_01

@echo setting environment to Java-based scripting languages ...
call e:\java\scriptJars\addEngines2cp.cmd

@echo setting environment to OOo
set OOO_pgms=D:\Programme\OpenOffice.org 3\program
set PATH=%PATH%;%OOO_pgms%

@REM  set CLASSPATH environment variable ...
set CLASSPATH=.;%CLASSPATH%;D:\Programme\OpenOffice.org 3\program\;D:\Programme\OpenOffice.org 3\Basis\program\classes\unoil.jar;D:\Programme\OpenOffice.org 3\URE\java\ridl.jar;D:\Programme\OpenOffice.org 3\URE\java\jurt.jar;D:\Programme\OpenOffice.org 3\URE\java\juh.jar;D:\Programme\OpenOffice.org 3\Basis\program\classes\ScriptFramework.jar

@echo adding Xalan to CLASSPATH
@rem set cp=-classpath "%CLASSPATH%;F:\download\java\junit\junit3.8.1\junit.jar;F:\download\java\Xalan\work\xalan.jar"
set cp=-classpath "%CLASSPATH%;%cd%\jars\Xalan\junit.jar;%cd%\jars\Xalan\xalan.jar"

@echo PATH and CLASSPATH set to:
@echo "PATH:"
PATH
@echo "CLASSPATH:"
set CLASSPATH
@echo "cp:"
set cp
@echo on
pause

@set source=1.4
@set target=1.4

javac %cp% -source %source% -target %target% org/apache/bsf/*.java

javac %cp% -source %source% -target %target% org/apache/bsf/util/*.java
javac %cp% -source %source% -target %target% org/apache/bsf/util/cf/*.java
javac %cp% -source %source% -target %target% org/apache/bsf/util/event/adapters/*.java
javac %cp% -source %source% -target %target% org/apache/bsf/util/event/generator/*.java
javac %cp% -source %source% -target %target% org/apache/bsf/util/type/*.java

javac %cp% -source %source% -target %target% org/apache/bsf/test/*.java
javac %cp% -source %source% -target %target% org/apache/bsf/test/engineTests/*.java
javac %cp% -source %source% -target %target% org/apache/bsf/test/utilTests/*.java

javac %cp% -source %source% -target %target% org/apache/bsf/engines/jacl/*.java
javac %cp% -source %source% -target %target% org/apache/bsf/engines/java/*.java
javac %cp% -source %source% -target %target% org/apache/bsf/engines/javaclass/*.java
javac %cp% -source %source% -target %target% org/apache/bsf/engines/javascript/*.java
javac %cp% -source %source% -target %target% org/apache/bsf/engines/jython/*.java
javac %cp% -source %source% -target %target% org/apache/bsf/engines/netrexx/*.java
javac %cp% -source %source% -target %target% org/apache/bsf/engines/xslt/*.java

javac %cp% -source %source% -target %target% org/rexxla/bsf/engines/rexx/*.java

rem javac %cp% -source %source% -target %target% bsf4oorexx.dev/bin/*.java
javac %cp% -source %source% -target %target% bsf4oorexx.dev/bin/JavaInfo4BSF4RexxInstallation.java

javac %cp% -source %source% -target %target% com/sun/star/script/framework/provider/oorexx/*.java


rem @echo .
rem @echo Not compiling: org/oorexx/datergf/, org/oorexx/misc/, org/oorexx/uno/
rem @echo (to compile, merely comment the "gote end" statement)
rem goto end

javac %cp% -source %source% -target %target% org/oorexx/datergf/*.java
javac %cp% -source %source% -target %target% org/oorexx/misc/*.java
javac %cp% -source %source% -target %target% org/oorexx/uno/*.java

:end

@echo .
endlocal
