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


echo Setting variables for MS VS (VC98) ...
rem 2009-09-07 call "E:\Programme\Microsoft Visual Studio 9.0\Common7\Tools\vsvars32.bat"
call "E:\Programme\Microsoft Visual Studio 9.0\VC\vcvarsall.bat" x86_amd64

rem SDK
rem call "E:\Programme\Microsoft Platform SDK for Windows Server 2003 R2\SetEnv.Cmd" /XP64 /DEBUG
call "E:\Programme\Microsoft Platform SDK for Windows Server 2003 R2\SetEnv.Cmd" /XP64 /RETAIL

rem 2008-08-25: for ooRexx-4.0 (according to Mark Miesfeld's posting on 2008-08-25, 17:38)
rem             not needed anymore (cf. MM's e-mail as of 2008-08-27)
rem Visual C++ 2008 == 9.0
rem Visual C++ 2005 == 8.0
rem Visual C++ 2003 == 7.0
rem Visual C++ 6.0  == 6.0
rem set MSVCVER=6.0


set win_rgf=e:\rony\dev\bsf\src\bin

rem set path=.;%win_rgf%;%path%;
set path=.;%cd%;%cd%\..\bin;%path%;

echo PATH=%path%
echo .

rem set classpath to the BSF-version we want to work with
set classpath=e:\rony\dev\bsf\src;.;%classpath%;
echo CLASSPATH=%classpath%
echo .


