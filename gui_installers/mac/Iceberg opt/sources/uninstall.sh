#!/bin/sh

# Name: uninstall.sh
# Purpose: Uninstall script for ooRexx and BSF4ooRexx
# Author: Juergen Hesse, Manuel Paar
# Version: 1.0
#
# ------------------- Apache Version 2.0 license --------------------
#    Copyright (C) 2011 Juergen Hesse, Manuel Paar
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#          http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
#    implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
# -------------------------------------------------------------------

# First uninstalls ooRexx and BSF4ooRexx and then reinstalls them
if [ $1 = "reinstall" ]
	then
		osascript -e "do shell script \"cp -r /Applications/ooRexx.app/Contents/sources /tmp/; cp /Applications/ooRexx.app/Contents/Resources/ooRexx_uninstall_preflight.sh /tmp/; cp /Applications/ooRexx.app/Contents/Resources/BSF4ooRexx_uninstall_preflight.sh /tmp/; sh /tmp/ooRexx_uninstall_preflight.sh reinstall\" with administrator privileges"
fi

# Just uninstalls ooRexx and BSF4ooRexx
if [ $1 = "uninstall" ]
	then osascript -e "do shell script \"cp /Applications/ooRexx.app/Contents/Resources/ooRexx_uninstall_preflight.sh /tmp/; cp /Applications/ooRexx.app/Contents/Resources/BSF4ooRexx_uninstall_preflight.sh /tmp/; sh /tmp/ooRexx_uninstall_preflight.sh uninstall\" with administrator privileges"
fi
