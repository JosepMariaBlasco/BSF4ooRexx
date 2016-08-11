@echo off
rem rgf, 2016-08-11 - compile Java classes, create jar file which should replace the one in ..\bin

set bindir=..\bin
set jni4net_jar=%bindir%\jni4net.j-0.8.8.0.jar
set tgt_jar=oorexx.net.jar

set jni4net_dir=jni4net\java
set helper_dir=Helper4ooRexx4Net

echo on
javac -source 1.5 -target 1.5 -cp %jni4net_jar% %jni4net_dir%\system\*java
javac -source 1.5 -target 1.5 -cp %jni4net_jar% %helper_dir%\org\oorexx\clr\*java

jar -cvf %tgt_jar% -C %jni4net_dir% .
jar -uvf %tgt_jar% -C %helper_dir% .

echo to delete sourcefiles -    zip -d %tgt_jar% *.java
echo to copy to bindir     -    copy %tgt_jar% %bindir%
