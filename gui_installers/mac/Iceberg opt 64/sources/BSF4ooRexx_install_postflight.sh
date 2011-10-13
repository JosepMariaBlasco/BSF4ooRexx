#!/bin/sh
# Name: BSF4ooRexx_install_postflight.sh
# Purpose: Install script for ooRexx and BSF4ooRexx
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
VER=4
REL=0
MOD=3
PREFIX=/opt/ooRexx

echo $SCRIPT_NAME: linking files
echo $SCRIPT_NAME: linking libraries to /usr/lib
ln -sf $PREFIX/lib/ooRexx/libhostemu.$VER.$REL.$MOD.dylib   /usr/lib
ln -sf $PREFIX/lib/ooRexx/libhostemu.$VER.dylib       /usr/lib
ln -sf $PREFIX/lib/ooRexx/libhostemu.a             /usr/lib
ln -sf $PREFIX/lib/ooRexx/libhostemu.dylib         /usr/lib
ln -sf $PREFIX/lib/ooRexx/libhostemu.la            /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexx.$VER.$REL.$MOD.dylib     /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexx.$VER.dylib         /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexx.a               /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexx.dylib           /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexx.la              /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexxapi.$VER.$REL.$MOD.dylib  /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexxapi.$VER.dylib      /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexxapi.a            /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexxapi.dylib        /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexxapi.la           /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexxutil.$VER.$REL.$MOD.dylib /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexxutil.$VER.dylib     /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexxutil.a           /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexxutil.dylib       /usr/lib
ln -sf $PREFIX/lib/ooRexx/librexxutil.la          /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxmath.$VER.$REL.$MOD.dylib   /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxmath.$VER.dylib       /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxmath.a             /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxmath.dylib         /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxmath.la            /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxregexp.$VER.$REL.$MOD.dylib /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxregexp.$VER.dylib     /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxregexp.a           /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxregexp.dylib       /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxregexp.la          /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxsock.$VER.$REL.$MOD.dylib   /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxsock.$VER.dylib       /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxsock.a             /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxsock.dylib         /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxsock.la            /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxunixsys.$VER.$REL.$MOD.dylib   /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxunixsys.$VER.dylib       /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxunixsys.a             /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxunixsys.dylib         /usr/lib
ln -sf $PREFIX/lib/ooRexx/librxunixsys.la            /usr/lib

echo $SCRIPT_NAME: linking programs to /usr/bin
ln -sf $PREFIX/bin/csvStream.cls	/usr/bin
ln -sf $PREFIX/bin/mime.cls	/usr/bin
ln -sf $PREFIX/bin/oorexx-config	/usr/bin
ln -sf $PREFIX/bin/rexx          /usr/bin
ln -sf $PREFIX/bin/rexx.cat      /usr/bin
ln -sf $PREFIX/bin/rexx.img      /usr/bin
ln -sf $PREFIX/bin/rexxc         /usr/bin
ln -sf $PREFIX/bin/rexximage     /usr/bin
ln -sf $PREFIX/bin/rxapi         /usr/bin
ln -sf $PREFIX/bin/rxftp.cls     /usr/bin
ln -sf $PREFIX/bin/rxqueue       /usr/bin
ln -sf $PREFIX/bin/rxregexp.cls  /usr/bin
ln -sf $PREFIX/bin/rxsubcom      /usr/bin
ln -sf $PREFIX/bin/smtp.cls	/usr/bin
ln -sf $PREFIX/bin/socket.cls	/usr/bin
ln -sf $PREFIX/bin/socket.cls    /usr/bin
ln -sf $PREFIX/bin/streamsocket.cls	/usr/bin
ln -sf $PREFIX/share/ooRexx/rexxtry.rex /usr/bin

echo $SCRIPT_NAME: linking man pages to /usr/share/man/man1
ln -sf $PREFIX/share/man/man1/oorexx-config.1 /usr/share/man/man1
ln -sf $PREFIX/share/man/man1/rexx.1          /usr/share/man/man1
ln -sf $PREFIX/share/man/man1/rexxc.1         /usr/share/man/man1
ln -sf $PREFIX/share/man/man1/rxmigrate.1     /usr/share/man/man1
ln -sf $PREFIX/share/man/man1/rxqueue.1       /usr/share/man/man1
ln -sf $PREFIX/share/man/man1/rxsubcom.1      /usr/share/man/man1

echo $SCRIPT_NAME: linking headers to /usr/include
ln -sf $PREFIX/include/oorexxapi.h        /usr/include
ln -sf $PREFIX/include/oorexxerrors.h     /usr/include
ln -sf $PREFIX/include/rexx.h             /usr/include
ln -sf $PREFIX/include/rexxapidefs.h      /usr/include
ln -sf $PREFIX/include/rexxapitypes.h     /usr/include
ln -sf $PREFIX/include/rexxplatformapis.h /usr/include
ln -sf $PREFIX/include/rexxplatformdefs.h /usr/include

echo $SCRIPT_NAME:  linking Launch Daemon to /Library/LaunchDaemons
ln -sf $PREFIX/bin/org.rexxla.oorexx.rxapid.plist /Library/LaunchDaemons

echo $SCRIPT_NAME:  stop/start rxapi Daemon
launchctl unload -w /Library/LaunchDaemons/org.rexxla.oorexx.rxapid.plist
killall rxapi
launchctl load -w /Library/LaunchDaemons/org.rexxla.oorexx.rxapid.plist

echo $SCRIPT_NAME: linking complete

BSF4OOREXX_FRAMEWORK=/opt/BSF4ooRexx

BIN_DIR=/usr/bin
JAVA_LIB_DIR=/usr/lib/java

OOO_SCRIPTLIB_SRC=$BSF4OOREXX_FRAMEWORK/Libraries/OOo_ScriptLibraries

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
echo "Creating symbolic links in $BIN_DIR..."
echo ""

sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/BSF.CLS                       $BIN_DIR
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/BSF_OnTheFly.cls              $BIN_DIR
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/rexxj2.sh                     $BIN_DIR
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/rexxj2.sh                     $BIN_DIR/rexxj.sh
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/rgf_util2.rex                 $BIN_DIR
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/UNO.CLS                       $BIN_DIR
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/UNO_XINTERFACES.RXO           $BIN_DIR
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/UNO_ReflectionUtil.cls        $BIN_DIR
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/UNO_CREATE_INTERFACE_LIST.RXO $BIN_DIR
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/UNO_API_info.rxo              $BIN_DIR

echo ""
echo "Creating symbolic links in $JAVA_LIB_DIR..."
echo ""

sudo ln -sf $BSF4OOREXX_FRAMEWORK/Libraries/libBSF4ooRexx.dylib $JAVA_LIB_DIR
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Libraries/libBSF4ooRexx.dylib /usr/lib

echo ""
echo "Creating symbolic links in $BSF4OOREXX_FRAMEWORK..."
echo ""

sudo ln -sf /Applications/ooRexx.app/Contents/samples/BSF4ooRexx $BSF4OOREXX_FRAMEWORK/Samples
sudo ln -sf /Applications/ooRexx.app/Contents/documentation/BSF4ooRexx $BSF4OOREXX_FRAMEWORK/Information
sudo ln -sf /Applications/ooRexx.app/Contents/utilities $BSF4OOREXX_FRAMEWORK/Utilities

sudo mkdir -p $BSF4OOREXX_FRAMEWORK/Install
sudo ln -sf /Applications/ooRexx.app/Contents/Resources/one_click_uninstall.sh $BSF4OOREXX_FRAMEWORK/Install/uninstall.sh
sudo ln -sf /Applications/ooRexx.app/Contents/Resources/one_click_reinstall.sh $BSF4OOREXX_FRAMEWORK/Install/reinstall.sh

OOORESULT=`lipo -info /Applications/OpenOffice.org.app/Contents/program/soffice | grep x86_64;`

if [[ -e $OOO_APP_DIR && ! -z $OOORESULT ]]
    then 
        echo ""
        echo "Adding OpenOffice JARs to CLASSPATH..."
        echo ""

        OOO_JUH=$OOO_JAVA_PATH/juh.jar
	OOO_JURT=$OOO_JAVA_PATH/jurt.jar
	OOO_RIDL=$OOO_JAVA_PATH/ridl.jar

        sudo echo "export CLASSPATH=\$CLASSPATH:$OOO_UNO_PATH:$OOO_JUH:$OOO_JURT:$OOO_RIDL" >> /etc/profile
        sudo echo "export CLASSPATH=\$CLASSPATH:$OOO_UNOIL_PATH/unoil.jar" >> /etc/profile
	sudo $OOO_UNO_PATH/unopkg add --shared $BSF4OOREXX_FRAMEWORK/Libraries/ScriptProviderForooRexx.oxt
fi

LORESULT=`lipo -info /Applications/LibreOffice.app/Contents/program/soffice | grep x86_64;`

if [[ -e $LO_APP_DIR && ! -z $LORESULT ]]
    then 
        echo ""
        echo "Adding LibreOffice JARs to CLASSPATH..."
        echo ""

        LO_JUH=$LO_JAVA_PATH/juh.jar
	LO_JURT=$LO_JAVA_PATH/jurt.jar
	LO_RIDL=$LO_JAVA_PATH/ridl.jar

        sudo echo "export CLASSPATH=\$CLASSPATH:$LO_UNO_PATH:$LO_JUH:$LO_JURT:$LO_RIDL" >> /etc/profile
        sudo echo "export CLASSPATH=\$CLASSPATH:$LO_UNOIL_PATH/unoil.jar" >> /etc/profile
	sudo $LO_UNO_PATH/unopkg add --shared $BSF4OOREXX_FRAMEWORK/Libraries/ScriptProviderForooRexx.oxt
fi

echo ""
echo "Adding BSF4ooRexx JARs to CLASSPATH..."
echo ""

sudo echo "export CLASSPATH=\$CLASSPATH:$BSF4OOREXX_FRAMEWORK/Classes/bsf-rexx-engine.jar" >> /etc/profile
sudo echo "export CLASSPATH=\$CLASSPATH:$BSF4OOREXX_FRAMEWORK/Classes/bsf-v400-20090910.jar" >> /etc/profile

if [[ -e $OOO_APP_DIR && ! -z $OOORESULT ]]
    then
	echo ""
	echo "Installing OOo macros..."
	echo ""
	
	sudo cp -r $OOO_SCRIPTLIB_SRC/javascript/* $OOO_SCRIPTLIB_DIR/javascript
	sudo cp -r $OOO_SCRIPTLIB_SRC/python/* $OOO_SCRIPTLIB_DIR/python
	sudo cp -r $OOO_SCRIPTLIB_SRC/oorexx $OOO_SCRIPTLIB_DIR/
	# wu_tools muss per hand ins Framework kopiert werden!
	sudo cp $BSF4OOREXX_FRAMEWORK/Commands/UNO_API_info.rxo $OOO_SCRIPTLIB_DIR/oorexx/wu_tools/
fi

if [[ -e $LO_APP_DIR && ! -z $LORESULT ]]
    then
	echo ""
	echo "Installing LibreOffice macros..."
	echo ""
	
	sudo cp -r $OOO_SCRIPTLIB_SRC/javascript/* $LO_SCRIPTLIB_DIR/javascript
	sudo cp -r $OOO_SCRIPTLIB_SRC/python/* $LO_SCRIPTLIB_DIR/python
	sudo cp -r $OOO_SCRIPTLIB_SRC/oorexx $LO_SCRIPTLIB_DIR/
	# wu_tools muss per hand ins Framework kopiert werden!
	sudo cp $BSF4OOREXX_FRAMEWORK/Commands/UNO_API_info.rxo $LO_SCRIPTLIB_DIR/oorexx/wu_tools/
fi

echo ""
echo "Cleaning up..."
echo ""

sudo /System/Library/Frameworks/CoreServices.framework/Versions/Current/Frameworks/LaunchServices.framework/Versions/Current/Support/lsregister -kill -r -domain system -domain local -domain
