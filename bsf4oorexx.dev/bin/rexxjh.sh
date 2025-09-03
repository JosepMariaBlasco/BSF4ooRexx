#!/bin/sh
#    Purpose: load and run Rexx script via Java, allow for supplying arguments to the Rexx script,
#             tries to use $JAVA_HOME, if defined, else depending on environment
#    Author:  Rony G. Flatscher
#    Date:    2022-08-03
#    Changed: 2024-01-04 rgf, give feedback to user as to which java.exe gets employed
#             2024-05-09, ---rgf, special version for ooRexxShell, honor PREPEND_LIBRARY_PATH
#                                 (needed for macOS' to circumvent SIP)
#             2024-06-15, ---rgf, if PREPEND_LIBRARY_PATH is set export it to [DY]LD_LIBRARY_PATH
#                          - if environment variable "BSF4Rexx_quiet" is set to 1, then do not
#                            display informative (debug) information
#
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
   if  [ "$BSF4Rexx_quiet" != "1" ] ; then
      echo "... using environment variable 'JAVA_HOME', hence 'JAVA_BINARY': [$JAVA_BINARY]"
   fi
elif [ -e "${JAVA_HOME}/jre/bin/java"  ] ; then JAVA_BINARY=${JAVA_HOME}/jre/bin/java ;
   if  [ "$BSF4Rexx_quiet" != "1" ] ; then
      echo "... using environment variable 'JAVA_HOME', hence 'JAVA_BINARY': [$JAVA_BINARY]"
   fi
elif [ -e "/usr/bin/java" ] ; then JAVA_BINARY=/usr/bin/java ;
   if  [ "$BSF4Rexx_quiet" != "1" ] ; then
      echo "... using 'JAVA_BINARY': [$JAVA_BINARY]" ;
   fi
elif [ -e "/usr/local/bin/java" ] ; then JAVA_BINARY=/usr/local/bin/java ;
   if  [ "$BSF4Rexx_quiet" != "1" ] ; then
      echo "... using 'JAVA_BINARY': [$JAVA_BINARY]" ;
   fi
else JAVA_BINARY=java
   if  [ "$BSF4Rexx_quiet" != "1" ] ; then
      echo "... using 'JAVA_BINARY': [$JAVA_BINARY]" ;
   fi
fi

if  [ "$BSF4Rexx_quiet" != "1" ]
then
   if  [ "$BSF4Rexx_JavaStartupOptions" != "" ]
   then
      echo "... using environment variable 'BSF4Rexx_JavaStartupOptions' to configure JVM: [$BSF4Rexx_JavaStartupOptions]"
   fi
fi

# desiring a single script, hence exporting for both, Linux and macOS
if  [ "$PREPEND_LIBRARY_PATH" != "" ]
then
   # PREPEND_LIBRARY_PATH gets defined and set by setupoorexx.rex
   # echo "... 'PREPEND_LIBRARY_PATH': [$PREPEND_LIBRARY_PATH]"
   if  [ "$LD_LIBRARY_PATH" != "" ]
   then
      export LD_LIBRARY_PATH=$PREPEND_LIBRARY_PATH:$LD_LIBRARY_PATH
   else
      export LD_LIBRARY_PATH=$PREPEND_LIBRARY_PATH
   fi

   if  [ "$DYLD_LIBRARY_PATH" != "" ]
   then
      export DYLD_LIBRARY_PATH=$PREPEND_LIBRARY_PATH:$DYLD_LIBRARY_PATH
   else
      export DYLD_LIBRARY_PATH=$PREPEND_LIBRARY_PATH
   fi
   # show user the exports taking place
   if  [ "$BSF4Rexx_quiet" != "1" ]
   then
      echo "export LD_LIBRARY_PATH=$LD_LIBRARY_PATH"
      echo "export DYLD_LIBRARY_PATH=$DYLD_LIBRARY_PATH"
   fi
fi

# show user effective command
if  [ "$BSF4Rexx_quiet" != "1" ]
then
   echo command: \"$JAVA_BINARY\" $BSF4Rexx_JavaStartupOptions org.rexxla.bsf.RexxDispatcher $*
fi

# export LD_DEBUG=libs reloc statistics

"$JAVA_BINARY" $BSF4Rexx_JavaStartupOptions org.rexxla.bsf.RexxDispatcher $*
