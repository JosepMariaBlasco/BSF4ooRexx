#!/usr/bin/rexx
/* Name: java_version.rex
   Purpose: Returns the Java version and bitness
   Author: Juergen Hesse, Manuel Paar
   Version: 1.0

   ------------------- Apache Version 2.0 license --------------------
     Copyright (C) 2011 Juergen Hesse, Manuel Paar
 
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
 
           http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
     implied.
     See the License for the specific language governing permissions and
     limitations under the License.
   -------------------------------------------------------------------
*/

if arg(1) = "bitness" then exit (getJavaBitness())

exit (getJavaVersion())

::routine getJavaVersion
   "java -version 2>&1 | rxqueue"
   java_ver=0
   do while queued()>0
      parse pull 'java version "' val1 "." val2 "."
      if val1>=1 & val2>=4 then java_ver=1
   end
   return java_ver

::routine getJavaBitness
   "java -version 2>&1 | rxqueue"
   bitness=0
   do while queued()>0
      parse pull "Java HotSpot(TM) " val " "
      if val="64-Bit" then bitness=64
   end
   return bitness
