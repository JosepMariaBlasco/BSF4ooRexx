rem Name: createInstaller.cmd
rem Purpose: Creates 32 and 64 bit installer for Windows
rem Author: Juergen Hesse, Manuel Paar
rem Version: 1.0
rem
rem ------------------- Apache Version 2.0 license --------------------
rem    Copyright (C) 2011 Juergen Hesse, Manuel Paar
rem
rem    Licensed under the Apache License, Version 2.0 (the "License");
rem    you may not use this file except in compliance with the License.
rem    You may obtain a copy of the License at
rem
rem          http://www.apache.org/licenses/LICENSE-2.0
rem
rem    Unless required by applicable law or agreed to in writing, software
rem    distributed under the License is distributed on an "AS IS" BASIS,
rem    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
rem    implied.
rem    See the License for the specific language governing permissions and
rem    limitations under the License.
rem -------------------------------------------------------------------


set prog="C:\Program Files (x86)\NSIS\makensis.exe"

%prog% BSF4ooRexx_x32_reinstall.nsi
%prog% BSF4ooRexx_x32.nsi
%prog% BSF4ooRexx_x64_reinstall.nsi
%prog% BSF4ooRexx_x64.nsi
del BSF4ooRexx_Setup_x32_reinstall.exe
del BSF4ooRexx_Setup_x64_reinstall.exe
