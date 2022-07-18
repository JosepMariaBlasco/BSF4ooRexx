@echo off
rem 2020-09-03, point to 32-bit Java 8
rem set JAVA_HOME=C:\Program Files (x86)\Java\jre1.8.0_171
rem 2022-07-18: 'jar' command in create_bsf_taglib does not work with later jar versions
e:\j08path-64-LTS.cmd

rem to compile the taglibs
rem "HAMMER" ;): set CLASSPATH=%CLASSPATH%;D:\Apache Software Foundation\Tomcat 9.0\lib\*
set TomcatBaseLib=D:\Apache Software Foundation\Tomcat 9.0\lib
set CLASSPATH=%CLASSPATH%;%TomcatBaseLib%\servlet-api.jar;%TomcatBaseLib%\jsp-api.jar

