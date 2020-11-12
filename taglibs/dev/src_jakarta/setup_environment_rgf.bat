rem 2020-09-03, point to 32-bit Java 8
set JAVA_HOME=C:\Program Files (x86)\Java\jre1.8.0_171

rem to compile the taglibs
rem "HAMMER" ;): set CLASSPATH=%CLASSPATH%;D:\Apache Software Foundation\Tomcat 9.0\lib\*
set TomcatBaseLib=D:\Apache Software Foundation\Tomcat 10.0\lib
set CLASSPATH=%CLASSPATH%;%TomcatBaseLib%\servlet-api.jar;%TomcatBaseLib%\jsp-api.jar

