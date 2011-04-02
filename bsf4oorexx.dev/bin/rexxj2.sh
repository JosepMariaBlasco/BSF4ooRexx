#    Purpose: load and run Rexx script via Java, allow for supplying arguments to the Rexx script
#    Author:  Rony G. Flatscher
#    Date:    2006-01-02
#    Changed: 2007-02-03, ---rgf, renamed file, changed year-range
#             2010-06-10, ---rgf, honor environment variable 'BSF4Rexx.JavaStartupOptions', if set
#    Remark:  Rexx script can retrieve all arguments as passed to Java via the BSFRegistry, including
#             the name of the Rexx script to be invoked/dispatched
#
#             Rexx:   args=bsf("lookupBean", "allCommandLineArguments")
#             or
#             ooRexx: args=.bsf~bsf.lookupBean("allCommandLineArguments")
#
#    license:
#    ------------------------ Apache Version 2.0 license -------------------------
#       Copyright 2006-2010 Rony G. Flatscher
#
#       Licensed under the Apache License, Version 2.0 (the "License");
#       you may not use this file except in compliance with the License.
#       You may obtain a copy of the License at
#
#           http://www.apache.org/licenses/LICENSE-2.0
#
#       Unless required by applicable law or agreed to in writing, software
#       distributed under the License is distributed on an "AS IS" BASIS,
#       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#       See the License for the specific language governing permissions and
#       limitations under the License.
#    -----------------------------------------------------------------------------

if [ "$BSF4Rexx_JavaStartupOptions" == "" ]
then
   java org.rexxla.bsf.RexxDispatcher $*
else
   echo "... using environment variable 'BSF4Rexx_JavaStartupOptions' to configure Java: [$BSF4Rexx_JavaStartupOptions]"
   java $BSF4Rexx_JavaStartupOptions org.rexxla.bsf.RexxDispatcher $*
fi

echo "aha $BSF4Rexx_JavaStartupOptions"
