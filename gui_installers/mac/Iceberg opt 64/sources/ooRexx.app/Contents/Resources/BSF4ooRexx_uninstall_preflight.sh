#!/bin/sh

# Name: BSF4ooRexx_uninstall_preflight.sh
# Purpose: Uninstall script for BSF4ooRexx
# Author: Juergen Hesse, Manuel Paar
# Version: 1.0
#
# ------------------------ Apache Version 2.0 license -------------------------
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
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
# -----------------------------------------------------------------------------

BSF4OOREXX_FRAMEWORK=/opt/BSF4ooRexx

BIN_DIR=/usr/bin
JAVA_LIB_DIR=/usr/lib/java

OOO_APP_DIR=/Applications/OpenOffice.org.app
OOO_SCRIPTLIB_DIR=$OOO_APP_DIR/Contents/basis-link/share/Scripts
OOO_UNO_PATH=$OOO_APP_DIR/Contents/program
OOO_JAVA_PATH=$OOO_APP_DIR/Contents/basis-link/ure-link/share/java
OOO_UNOIL_PATH=$OOO_APP_DIR/Contents/basis-link/program/classes

LO_APP_DIR=/Applications/LibreOffice.app
LO_SCRIPTLIB_DIR=$LO_APP_DIR/Contents/basis-link/share/Scripts
LO_UNO_PATH=$LO_APP_DIR/Contents/program
LO_JAVA_PATH=$LO_APP_DIR/Contents/basis-link/ure-link/share/java
LO_UNOIL_PATH=$LO_APP_DIR/Contents/basis-link/program/classes

echo ""
echo "Removing executable files from $BIN_DIR..."
echo ""

sudo rm $BIN_DIR/BSF.CLS
sudo rm $BIN_DIR/BSF_OnTheFly.cls
sudo rm $BIN_DIR/rexxj2.sh
sudo rm $BIN_DIR/rexxj.sh
sudo rm $BIN_DIR/rgf_util2.rex
sudo rm $BIN_DIR/UNO.CLS
sudo rm $BIN_DIR/UNO_API_info.rxo
sudo rm $BIN_DIR/UNO_CREATE_INTERFACE_LIST.RXO
sudo rm $BIN_DIR/UNO_ReflectionUtil.cls
sudo rm $BIN_DIR/UNO_XINTERFACES.RXO

echo ""
echo "Removing framework directory $BSF4OOREXX_FRAMEWORK..."
echo ""

sudo rm -rf $BSF4OOREXX_FRAMEWORK

echo ""
echo "Removing dynamic link library from $JAVA_LIB_DIR..."
echo ""

sudo rm $JAVA_LIB_DIR/libBSF4ooRexx.dylib
sudo rm /usr/lib/libBSF4ooRexx.dylib

echo ""
echo "Removing CLASSPATH settings for OpenOffice..."
echo ""

OOORESULT=`lipo -info /Applications/OpenOffice.org.app/Contents/program/soffice | grep x86_64;`

if [[ -e $OOO_APP_DIR && ! -z $OOORESULT ]]
	then
		sudo grep -vx "export CLASSPATH=\$CLASSPATH:/Applications/OpenOffice.org.app/Contents/program:/Applications/OpenOffice.org.app/Contents/basis-link/ure-link/share/java/juh.jar:/Applications/OpenOffice.org.app/Contents/basis-link/ure-link/share/java/jurt.jar:/Applications/OpenOffice.org.app/Contents/basis-link/ure-link/share/java/ridl.jar
export CLASSPATH=\$CLASSPATH:/Applications/OpenOffice.org.app/Contents/basis-link/program/classes/unoil.jar" /etc/profile > /tmp/profile.tmp
		sudo $OOO_UNO_PATH/unopkg remove --shared ScriptProviderForooRexx.oxt
		sudo rm -rf $OOO_SCRIPTLIB_DIR/javascript/HowtoCreateApiInfo
		sudo rm -rf $OOO_SCRIPTLIB_DIR/python/pythonSamples
		sudo rm -rf $OOO_SCRIPTLIB_DIR/oorexx/ooRexx_examples
		sudo rm -rf $OOO_SCRIPTLIB_DIR/oorexx/wu_tools
fi

sudo cp /tmp/profile.tmp /etc/profile
sudo rm /tmp/profile.tmp

echo ""
echo "Removing CLASSPATH settings for LibreOffice..."
echo ""

LORESULT=`lipo -info /Applications/LibreOffice.app/Contents/program/soffice | grep x86_64;`

if [[ -e $LO_APP_DIR && ! -z $LORESULT ]]
	then
		sudo grep -vx "export CLASSPATH=\$CLASSPATH:/Applications/LibreOffice.app/Contents/program:/Applications/LibreOffice.app/Contents/basis-link/ure-link/share/java/juh.jar:/Applications/LibreOffice.app/Contents/basis-link/ure-link/share/java/jurt.jar:/Applications/LibreOffice.app/Contents/basis-link/ure-link/share/java/ridl.jar
export CLASSPATH=\$CLASSPATH:/Applications/LibreOffice.app/Contents/basis-link/program/classes/unoil.jar" /etc/profile > /tmp/profile.tmp
		sudo $LO_UNO_PATH/unopkg remove --shared ScriptProviderForooRexx.oxt
		sudo rm -rf $LO_SCRIPTLIB_DIR/javascript/HowtoCreateApiInfo
		sudo rm -rf $LO_SCRIPTLIB_DIR/python/pythonSamples
		sudo rm -rf $LO_SCRIPTLIB_DIR/oorexx/ooRexx_examples
		sudo rm -rf $LO_SCRIPTLIB_DIR/oorexx/wu_tools
fi

sudo cp /tmp/profile.tmp /etc/profile
sudo rm /tmp/profile.tmp

echo ""
echo "Removing CLASSPATH settings for BSF4ooRexx..."
echo ""

sudo grep -vx "export CLASSPATH=\$CLASSPATH:/opt/BSF4ooRexx/Classes/bsf-rexx-engine.jar
export CLASSPATH=\$CLASSPATH:/opt/BSF4ooRexx/Classes/bsf-v400-20090910.jar" /etc/profile > /tmp/profile.tmp

sudo cp /tmp/profile.tmp /etc/profile
sudo rm /tmp/profile.tmp

echo ""
echo "Removing Application Bundle..."
echo ""

sudo rm -rf /Applications/ooRexx.app

echo ""
echo "Removing the ooRexx directory..."
echo ""

sudo rm -rf /opt/ooRexx


echo ""
echo "Cleaning up..."
echo ""

sudo /System/Library/Frameworks/CoreServices.framework/Versions/Current/Frameworks/LaunchServices.framework/Versions/Current/Support/lsregister -kill -r -domain system -domain local -domain

sudo rm -rf /tmp/ooRexx_uninstall_preflight.sh

if [ $1 = "uninstall" ]
	then
		osascript -e 'tell app "System Events" to display dialog "ooRexx and BSF4ooRexx have successfully been removed!" buttons "OK" default button 1 with title "Success!" with icon 1'
fi

if [ $1 = "reinstall" ]
	then
		sudo sh /tmp/sources/reinstall.sh
		sudo cp -r /tmp/sources /Applications/ooRexx.app/Contents/
		sudo rm -rf /tmp/sources
fi

sudo rm $0
