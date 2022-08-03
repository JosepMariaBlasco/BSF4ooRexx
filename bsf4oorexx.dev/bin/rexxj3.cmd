@echo off
rem   Purpose: load and run Rexx script via Java, allow for supplying arguments to the Rexx script
rem   Author:  Rony G. Flatscher
rem   Date:    2006-01-02
rem   Changed: 2007-02-03, ---rgf, renamed file, changed year-range
rem            2010-06-10, ---rgf, honor environment variable 'BSF4Rexx.JavaStartupOptions', if set
rem            2022-08-03, ---rgf, simplify invocation
rem   Remark:  Rexx script can retrieve all arguments as passed to Java via the BSFRegistry, including
rem            the name of the Rexx script to be invoked/dispatched
rem
rem            Rexx:   args=bsf("lookupBean", "allCommandLineArguments")
rem            or
rem            ooRexx: args=.bsf~bsf.lookupBean("allCommandLineArguments")

rem   license:
rem   ------------------------ Apache Version 2.0 license -------------------------
rem      Copyright 2006-2022 Rony G. Flatscher

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

   rem environment variable defining Java startup parameters exists, show it to the user
   echo ... using environment variable 'BSF4Rexx_JavaStartupOptions' to configure Java: [%BSF4Rexx_JavaStartupOptions%]

:invokenormal
   rem  just use the default Java runtime settings
   java %BSF4Rexx_JavaStartupOptions% org.rexxla.bsf.RexxDispatcher %*
goto end

:end

