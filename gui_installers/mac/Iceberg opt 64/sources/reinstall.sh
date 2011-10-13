#!/bin/sh

# Name: reinstall.sh
# Purpose: Reinstall script for ooRexx and BSF4ooRexx
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

SOURCES_DIR=/tmp/sources

BSF4OOREXX_FRAMEWORK=/opt/BSF4ooRexx

APP_PATH=/Applications

echo ""
echo "Reinstalling ooRexx..."
echo ""

sudo cp -r $SOURCES_DIR/ooRexx /opt/

echo ""
echo "Reinstalling BSF4ooRexx..."
echo ""

sudo cp -r $SOURCES_DIR/BSF4ooRexx /opt/

echo ""
echo "Reinstalling Application..."
echo ""

sudo cp -r $SOURCES_DIR/ooRexx.app $APP_PATH/

echo ""
echo "Running scripts..."
echo ""

sudo sh $SOURCES_DIR/BSF4ooRexx_install_postflight.sh

osascript -e 'tell app "System Events" to display dialog "ooRexx and BSF4ooRexx have successfully been reinstalled!" buttons "OK" default button 1 with title "Success!" with icon 1'

