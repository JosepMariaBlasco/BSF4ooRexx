@echo off
rem  ------------------------ Apache Version 2.0 license -------------------------
rem     Copyright (C) 2001-2009 Rony G. Flatscher
rem
rem     Licensed under the Apache License, Version 2.0 (the "License")rem
rem     you may not use this file except in compliance with the License.
rem     You may obtain a copy of the License at
rem
rem         http://www.apache.org/licenses/LICENSE-2.0
rem
rem     Unless required by applicable law or agreed to in writing, software
rem     distributed under the License is distributed on an "AS IS" BASIS,
rem     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem     See the License for the specific language governing permissions and
rem     limitations under the License.
rem  -----------------------------------------------------------------------------


@echo on
@echo off
@rem ---rgf, wu wien, nt-machine
@rem ---rgf, 2002-09-05, 2003-04-16, laptop, Not-Compilierung
@rem ---rgf, 2009-05-02: tailored to the BSF4Rexx version for the 4.0 APIs


echo Setting variables for MS VS (VC98) ...
rem call "e:\Programme\Microsoft Visual Studio\VC98\Bin\VCVARS32.BAT"
rem 2006-11; all of a sudden XP does not resolve FAT8.3 names anymore? The following works:
rem 2008-10-21 rem call "e:\Programme\Microsoft Visual Studio\VC98\Bin\VCVARS32rgf20061006.BAT"
call "E:\Programme\Microsoft Visual Studio 9.0\Common7\Tools\vsvars32.bat"

rem 2008-08-25: for ooRexx-4.0 (according to Mark Miesfeld's posting on 2008-08-25, 17:38)
rem             not needed anymore (cf. MM's e-mail as of 2008-08-27)
rem Visual C++ 2008 == 9.0
rem Visual C++ 2005 == 8.0
rem Visual C++ 2003 == 7.0
rem Visual C++ 6.0  == 6.0
rem set MSVCVER=6.0


rem set win_rgf=e:\rony\dev\bsf\src\bin

rem rgf, 20090502: make sure this directory and ..\bin (BSF4ooRexx Rexx programs) are searched first
set path=.;%cd%;%cd%\..\bin;%path%;

echo PATH=%path%
echo .

rem set classpath to the BSF-version we want to work with
set classpath=e:\rony\dev\bsf\src;.;%classpath%;
echo CLASSPATH=%classpath%
echo .


