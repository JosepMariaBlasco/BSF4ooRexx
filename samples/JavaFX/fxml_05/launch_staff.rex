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

   changed: 2025-08-29, ---rgf: edit say output to be more informative
*/

parse source . . fullPath
thisPgmName=filespec("name", fullPath)

parse source op_sys +1        -- get operating system
if op_sys="W" then dirSep="\"
              else dirSep="/"

   -- determine ControlsFX jar file to use
jVersion=getJavaVersion()
if jVersion~isNil then
do
   say thisProgram": Cannot determine the Java version, is Java installed and available?"
   exit -1
end
else if jVersion<>8, jVersion<11 then
do
   say thisProgram": Java version must be either 8 or 11 or higher, aborting ..."
   exit -2
end

if jVersion>8 then jarFile="jar"dirSep"controlsfx-11.1.1.jar"
              else jarFile="jar"dirSep"controlsfx-8.40.18.jar"

env="ENVIRONMENT"
if jVersion>=24 then    -- add --enable-native-access=ALL-UNNAMED
do
   needle="BSF4Rexx_JavaStartupOptions"
   option="--enable-native-access=ALL-UNNAMED"
   res=value(needle, , env)      -- get value, if present
   if res<>"" then res=res option
              else res=option
   call value needle, res, env   -- set new value
end

if op_sys="W" then   -- Windows
do
   cp=value("CLASSPATH",,env)             -- get current value
   newValue=jarFile";"cp
   call value "CLASSPATH", newValue, env  -- set new value
   cmd='rexx staff.rxj'
end
else  -- Unix
   cmd="CLASSPATH=$CLASSPATH:"jarFile "rexx staff.rxj"

say thisPgmName": Java version="pp(jVersion)", issuing command:" cmd     -- show command
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

::routine pp
  return "["arg(1)"]"



/*
      ------------------------ Apache Version 2.0 license -------------------------
         Copyright 2017-2025 Rony G. Flatscher

         Licensed under the Apache License, Version 2.0 (the "License");
         you may not use this file except in compliance with the License.
         You may obtain a copy of the License at

             http://www.apache.org/licenses/LICENSE-2.0

         Unless required by applicable law or agreed to in writing, software
         distributed under the License is distributed on an "AS IS" BASIS,
         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         See the License for the specific language governing permissions and
         limitations under the License.
      -----------------------------------------------------------------------------
*/

