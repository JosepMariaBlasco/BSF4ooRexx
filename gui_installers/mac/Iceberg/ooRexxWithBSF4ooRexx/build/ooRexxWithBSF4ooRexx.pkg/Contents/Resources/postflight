#!/bin/sh

# Name: BSF4ooRexx_install_postflight.sh
# Purpose: Install script for ooRexx and BSF4ooRexx
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


VER=4
REL=0
MOD=3
PREFIX=/System/Library/Frameworks/ooRexx.framework

echo $SCRIPT_NAME: linking files

echo $SCRIPT_NAME: linking libraries to /usr/lib
ln -sf $PREFIX/Libraries/libhostemu.$VER.$REL.$MOD.dylib   /usr/lib
ln -sf $PREFIX/Libraries/libhostemu.$VER.dylib       /usr/lib
ln -sf $PREFIX/Libraries/libhostemu.a             /usr/lib
ln -sf $PREFIX/Libraries/libhostemu.dylib         /usr/lib
ln -sf $PREFIX/Libraries/libhostemu.la            /usr/lib
ln -sf $PREFIX/Libraries/librexx.$VER.$REL.$MOD.dylib     /usr/lib
ln -sf $PREFIX/Libraries/librexx.$VER.dylib         /usr/lib
ln -sf $PREFIX/Libraries/librexx.a               /usr/lib
ln -sf $PREFIX/Libraries/librexx.dylib           /usr/lib
ln -sf $PREFIX/Libraries/librexx.la              /usr/lib
ln -sf $PREFIX/Libraries/librexxapi.$VER.$REL.$MOD.dylib  /usr/lib
ln -sf $PREFIX/Libraries/librexxapi.$VER.dylib      /usr/lib
ln -sf $PREFIX/Libraries/librexxapi.a            /usr/lib
ln -sf $PREFIX/Libraries/librexxapi.dylib        /usr/lib
ln -sf $PREFIX/Libraries/librexxapi.la           /usr/lib
ln -sf $PREFIX/Libraries/librexxutil.$VER.$REL.$MOD.dylib /usr/lib
ln -sf $PREFIX/Libraries/librexxutil.$VER.dylib     /usr/lib
ln -sf $PREFIX/Libraries/librexxutil.a           /usr/lib
ln -sf $PREFIX/Libraries/librexxutil.dylib       /usr/lib
ln -sf $PREFIX/Libraries/librexxutil.la          /usr/lib
ln -sf $PREFIX/Libraries/librxmath.$VER.$REL.$MOD.dylib   /usr/lib
ln -sf $PREFIX/Libraries/librxmath.$VER.dylib       /usr/lib
ln -sf $PREFIX/Libraries/librxmath.a             /usr/lib
ln -sf $PREFIX/Libraries/librxmath.dylib         /usr/lib
ln -sf $PREFIX/Libraries/librxmath.la            /usr/lib
ln -sf $PREFIX/Libraries/librxregexp.$VER.$REL.$MOD.dylib /usr/lib
ln -sf $PREFIX/Libraries/librxregexp.$VER.dylib     /usr/lib
ln -sf $PREFIX/Libraries/librxregexp.a           /usr/lib
ln -sf $PREFIX/Libraries/librxregexp.dylib       /usr/lib
ln -sf $PREFIX/Libraries/librxregexp.la          /usr/lib
ln -sf $PREFIX/Libraries/librxsock.$VER.$REL.$MOD.dylib   /usr/lib
ln -sf $PREFIX/Libraries/librxsock.$VER.dylib       /usr/lib
ln -sf $PREFIX/Libraries/librxsock.a             /usr/lib
ln -sf $PREFIX/Libraries/librxsock.dylib         /usr/lib
ln -sf $PREFIX/Libraries/librxsock.la            /usr/lib
ln -sf $PREFIX/Libraries/librxunixsys.$VER.$REL.$MOD.dylib   /usr/lib
ln -sf $PREFIX/Libraries/librxunixsys.$VER.dylib       /usr/lib
ln -sf $PREFIX/Libraries/librxunixsys.a             /usr/lib
ln -sf $PREFIX/Libraries/librxunixsys.dylib         /usr/lib
ln -sf $PREFIX/Libraries/librxunixsys.la            /usr/lib

echo $SCRIPT_NAME: linking programs to /usr/bin
ln -sf $PREFIX/Commands/csvStream.cls        /usr/bin
ln -sf $PREFIX/Commands/mime.cls     /usr/bin
ln -sf $PREFIX/Commands/oorexx-config        /usr/bin
ln -sf $PREFIX/Commands/rexx          /usr/bin
ln -sf $PREFIX/Commands/rexx.cat      /usr/bin
ln -sf $PREFIX/Commands/rexx.img      /usr/bin
ln -sf $PREFIX/Commands/rexxc         /usr/bin
ln -sf $PREFIX/Commands/rexximage     /usr/bin
ln -sf $PREFIX/Commands/rxapi         /usr/bin
ln -sf $PREFIX/Commands/rxftp.cls     /usr/bin
ln -sf $PREFIX/Commands/rxqueue       /usr/bin
ln -sf $PREFIX/Commands/rxregexp.cls  /usr/bin
ln -sf $PREFIX/Commands/rxsubcom      /usr/bin
ln -sf $PREFIX/Commands/smtp.cls     /usr/bin
ln -sf $PREFIX/Commands/socket.cls   /usr/bin
ln -sf $PREFIX/Commands/socket.cls    /usr/bin
ln -sf $PREFIX/Commands/streamsocket.cls     /usr/bin
ln -sf $PREFIX/Shared/ooRexx/rexxtry.rex /usr/bin

echo $SCRIPT_NAME: linking man pages to /usr/share/man/man1
ln -sf $PREFIX/Shared/man/man1/oorexx-config.1 /usr/share/man/man1
ln -sf $PREFIX/Shared/man/man1/rexx.1          /usr/share/man/man1
ln -sf $PREFIX/Shared/man/man1/rexxc.1         /usr/share/man/man1
ln -sf $PREFIX/Shared/man/man1/rxmigrate.1     /usr/share/man/man1
ln -sf $PREFIX/Shared/man/man1/rxqueue.1       /usr/share/man/man1
ln -sf $PREFIX/Shared/man/man1/rxsubcom.1      /usr/share/man/man1

echo $SCRIPT_NAME: linking headers to /usr/include
ln -sf $PREFIX/Headers/oorexxapi.h        /usr/include
ln -sf $PREFIX/Headers/oorexxerrors.h     /usr/include
ln -sf $PREFIX/Headers/rexx.h             /usr/include
ln -sf $PREFIX/Headers/rexxapidefs.h      /usr/include
ln -sf $PREFIX/Headers/rexxapitypes.h     /usr/include
ln -sf $PREFIX/Headers/rexxplatformapis.h /usr/include
ln -sf $PREFIX/Headers/rexxplatformdefs.h /usr/include

echo $SCRIPT_NAME:  linking Launch Daemon to /Library/LaunchDaemons
ln -sf $PREFIX/Commands/org.rexxla.oorexx.rxapid.plist /Library/LaunchDaemons

echo $SCRIPT_NAME:  stop/start rxapi Daemon
launchctl unload -w /Library/LaunchDaemons/org.rexxla.oorexx.rxapid.plist
killall rxapi
launchctl load -w /Library/LaunchDaemons/org.rexxla.oorexx.rxapid.plist

echo $SCRIPT_NAME: linking complete


# ----------------------------------------------------------------------


FRAMEWORK_SYS_DIR=/System/Library/Frameworks
FRAMEWORK=BSF4ooRexx.framework
BSF4OOREXX_FRAMEWORK=$FRAMEWORK_SYS_DIR/$FRAMEWORK

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

sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/BSF.CLS          $BIN_DIR
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/BSF_OnTheFly.cls $BIN_DIR
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/rexxj2.sh        $BIN_DIR
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/rexxj2.sh        $BIN_DIR/rexxj.sh
sudo ln -sf $BSF4OOREXX_FRAMEWORK/Commands/rgf_util2.rex    $BIN_DIR
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

OOORESULT=`lipo -info /Applications/OpenOffice.org.app/Contents/program/soffice | grep i386;`

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

LORESULT=`lipo -info /Applications/LibreOffice.app/Contents/program/soffice | grep i386;`

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

if [ -e $OOO_APP_DIR ]
    then
	echo ""
	echo "Installing OOo macros..."
	echo ""
	
	sudo cp -r $OOO_SCRIPTLIB_SRC/javascript/* $OOO_SCRIPTLIB_DIR/javascript
	sudo cp -r $OOO_SCRIPTLIB_SRC/python/* $OOO_SCRIPTLIB_DIR/python
	sudo cp -r $OOO_SCRIPTLIB_SRC/oorexx $OOO_SCRIPTLIB_DIR/
	sudo cp $BSF4OOREXX_FRAMEWORK/Commands/UNO_API_info.rxo $OOO_SCRIPTLIB_DIR/oorexx/wu_tools/
fi

if [ -e $LO_APP_DIR ]
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
