@echo off
rem   Purpose: load and run Rexx script via Java, allow for supplying arguments to the Rexx script
rem            if JAVA_HOME is defined try to use its java.exe, fallback: java.exe (without path)
rem   Author:  Rony G. Flatscher
rem   Date:    2022-08-03
rem   Changed: 2024-01-04 rgf, give feedback to user as to which java.exe gets employed
rem            2026-01-08 rgf, only verbose, if BSF4Rexx_quiet <> 1
rem   Remark:  Rexx script can retrieve all arguments as passed to Java via the BSFRegistry, including
rem            the name of the Rexx script to be invoked/dispatched
rem
rem            Rexx:   args=bsf("lookupBean", "allCommandLineArguments")
rem            or
rem            ooRexx: args=.bsf~bsf.lookupBean("allCommandLineArguments")

rem   license:
rem   ------------------------ Apache Version 2.0 license -------------------------
rem      Copyright 2022-2026 Rony G. Flatscher

rem      Licensed under the Apache License, Version 2.0 (the "License");
rem      you may not use this file except in compliance with the License.
rem      You may obtain a copy of the License at

rem          http://www.apache.org/licenses/LICENSE-2.0

rem      Unless required by applicable law or agreed to in writing, software
rem      distributed under the License is distributed on an "AS IS" BASIS,
rem      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem      See the License for the specific language governing permissions and
rem      limitations under the License.
rem   -----------------------------------------------------------------------------

if not defined BSF4Rexx_JavaStartupOptions goto invokenormal

if not "%BSF4Rexx_quiet%" == "1" (
   rem environment variable defining Java startup parameters exists, show it to the user
   echo ... using environment variable 'BSF4Rexx_JavaStartupOptions' to configure JVM: [%BSF4Rexx_JavaStartupOptions%]
)

:invokenormal

setlocal enableDelayedExpansion

REM  if JAVA_HOME defined, use its Java binary
set JAVA_BINARY=
@rem enables referring via exclamation mark within blocks; from now on must not use endlocal
if not "%JAVA_HOME%" == "" (
   set jexe=!JAVA_HOME!\bin\java.exe
   if exist "!jexe!"  (
     set java_binary=!jexe!
     if not "%BSF4Rexx_quiet%" == "1" (
        echo ... using environment variable JAVA_HOME: [!JAVA_BINARY!]
     )
   ) else (
      set jexe=!JAVA_HOME!\jre\bin\java.exe
      if exist "!jexe!" (
        set java_binary=!jexe!
        if not "%BSF4Rexx_quiet%" == "1" (
           echo ... using environment variable JAVA_HOME: [!JAVA_BINARY!]
        )
      )
   )
)
@rem if original java.exe is not present anymore use just java as a last resort
if not exist "%JAVA_BINARY%" set JAVA_BINARY=java


if not "%BSF4Rexx_quiet%" == "1" (
   echo command: "%JAVA_BINARY%"  %BSF4Rexx_JavaStartupOptions% org.rexxla.bsf.RexxDispatcher %*
)
"%JAVA_BINARY%"  %BSF4Rexx_JavaStartupOptions% org.rexxla.bsf.RexxDispatcher %*

:end
