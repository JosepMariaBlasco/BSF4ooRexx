#!/bin/sh
#    Purpose: load and run Rexx script via Java, allow for supplying arguments to the Rexx script,
#             tries to use $JAVA_HOME, if defined, else depending on environment
#    Author:  Rony G. Flatscher
#    Date:    2022-08-03
#    Changed: --
#    Changed: 2024-01-04 rgf, give feedback to user as to which java.exe gets employed
#    Remark:  Rexx script can retrieve all arguments as passed to Java via the BSFRegistry, including
#             the name of the Rexx script to be invoked/dispatched
#
#             Rexx:   args=bsf("lookupBean", "allCommandLineArguments")
#             or
#             ooRexx: args=.bsf~bsf.lookupBean("allCommandLineArguments")
#
#    license:
#    ------------------------ Apache Version 2.0 license -------------------------
#       Copyright 2022-2024 Rony G. Flatscher
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


# if JAVA_HOME defined, use its java binary
if [ -e "${JAVA_HOME}/bin/java" ] ; then JAVA_BINARY=${JAVA_HOME}/bin/java ;
   echo "... using environment variable 'JAVA_HOME': [$JAVA_BINARY]"
elif [ -e "${JAVA_HOME}/jre/bin/java"  ] ; then JAVA_BINARY=${JAVA_HOME}/jre/bin/java ;
   echo "... using environment variable 'JAVA_HOME': [$JAVA_BINARY]"
elif [ -e "/usr/bin/java" ] ; then JAVA_BINARY=/usr/bin/java ;
   echo "... using: [$JAVA_BINARY]"
elif [ -e "/usr/local/bin/java" ] ; then JAVA_BINARY=/usr/local/bin/java ;
   echo "... using: [$JAVA_BINARY]"
else JAVA_BINARY=java
   echo "... using: [$JAVA_BINARY]"
fi

if  [ "$BSF4Rexx_JavaStartupOptions" != "" ]
then
   echo "... using environment variable 'BSF4Rexx_JavaStartupOptions' to configure JVM: [$BSF4Rexx_JavaStartupOptions]"
fi

# export LD_DEBUG=libs reloc statistics

echo command: \"$JAVA_BINARY\" $BSF4Rexx_JavaStartupOptions org.rexxla.bsf.RexxDispatcher $*
"$JAVA_BINARY" $BSF4Rexx_JavaStartupOptions org.rexxla.bsf.RexxDispatcher $*


