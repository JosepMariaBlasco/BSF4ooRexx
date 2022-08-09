#!/usr/bin/env rexx
/* This sample uses third party jars (ControlsFX) for which two different
   jar files are available, one for Java/OpenJDK 8 and one for the modular
   Java/OpenJDK 11 and later.

   Therefore we need to determine the Java version, set CLASSPATH accordingly
   before running "staff.rxj".

   Note: we cannot use BSF4ooRexx to figure out the Java version as that will
         cause the JVM to get loaded into the process space using the CLASSPATH
         value present at runtime of this Rexx script, such that the changes
         to the environment variables by this Rexx program will not get seen
         by the already loaded JVM.
*/

parse source op_sys +1        -- get operating system
if op_sys="W" then dirSep="\"
              else dirSep="/"

   -- determine ControlsFX jar file to use
jVersion=getJavaVersion()
if jVersion~isNil then
do
   say "Cannot determine the Java version, is Java installed and available?"
   exit -1
end
else if jVersion<>8, jVersion<11 then
do
   say "Java version must be either 8 or 11 or higher, aborting ..."
   exit -2
end

if jVersion>8 then jarFile="jar"dirSep"controlsfx-11.1.1.jar"
              else jarFile="jar"dirSep"controlsfx-8.40.18.jar"

if op_sys="W" then   -- Windows
do
   cp=value("CLASSPATH",,"ENVIRONMENT")            -- get current value
   newValue=jarFile";"cp
   call value "CLASSPATH", newValue, "ENVIRONMENT" -- set new value
   cmd='rexx staff.rxj'
end
else  -- Unix
   cmd="CLASSPATH=$CLASSPATH:"jarFile "rexx staff.rxj"

say "cmd:" cmd    -- show command
address "" cmd    -- run the command


::routine getJavaVersion
/* "java -version" may yield, e.g.

   E:\>java -version
   java version "1.6.0_45"
   Java(TM) SE Runtime Environment (build 1.6.0_45-b06)
   Java HotSpot(TM) 64-Bit Server VM (build 20.45-b01, mixed mode)

or maybe

   F:\work\svn\bsf4oorexx\trunk\samples\JavaFX\fxml_05>java -version
   openjdk version "17.0.3.1" 2022-04-22 LTS
   OpenJDK Runtime Environment (build 17.0.3.1+2-LTS)
   OpenJDK 64-Bit Server VM (build 17.0.3.1+2-LTS, mixed mode, sharing)

*/

   signal on syntax
   v1=.nil                    -- default to .nil
   -- command to get Java version
   cmd="java -version 2>&1 | rxqueue"  -- let the output be sent to the Rexx session queue
   ADDRESS "" cmd             -- run the command
   arr=.array~new
   do while queued()>0        -- fetch the output from the Rexx session queue
      parse pull line
      arr~append(line)
   end
   -- parse works on strings only so the array will be automatically turned into a string
   parse var arr 'version "' v1 "." v2 "."
   if v1=1 then v1=v2
   return v1
syntax:                       -- if anything goes wrong
   return v1
