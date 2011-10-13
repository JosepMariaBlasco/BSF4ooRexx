#!/bin/sh

# Name: makeRPM.sh
# Purpose: Shell script for creating the BSF4ooRexx RPM package.
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

BSF4OOREXXZIPDIR=../Downloads
BSF4OOREXXZIPFILE=BSF4ooRexx_install.zip
BSF4OOREXXDISTRIBUTION=$BSF4OOREXXZIPDIR/$BSF4OOREXXZIPFILE

SPECSFILE=SPECS/bsf4oorexx.spec

echo
echo "Cleaning build directory..."
echo

rm -r BUILD/BSF4ooRexxInstall

echo
echo "Copying source files..."
echo 

cp $BSF4OOREXXDISTRIBUTION SOURCES/

echo
echo "Extracting sources..."
echo

unzip SOURCES/$BSF4OOREXXZIPFILE -d /tmp/bsftemp/

echo 
echo "Retrieving version information..."
echo

rexx getBSFversion.rex
BSFVERSION=`cat BSFVersion.txt`
BSFRELEASE=`cat BSFRelease.txt`

echo
echo "Building .spec file..."
echo

echo "Name:           bsf4oorexx" > $SPECSFILE
echo "Version:        $BSFVERSION" >> $SPECSFILE
echo "Release:        $BSFRELEASE" >> $SPECSFILE
cat bsf4oorexx.spec.template >> $SPECSFILE

echo
echo "Building RPM package..."
echo

rpmbuild -ba $SPECSFILE

echo 
echo "Done."
echo
