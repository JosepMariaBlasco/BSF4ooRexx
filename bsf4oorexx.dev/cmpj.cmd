@echo off
rem  ------------------------ Apache Version 2.0 license -------------------------
rem     Copyright (C) 2001-2010 Rony G. Flatscher
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
setlocal

rem set path2root=E:\rony\dev\bsf\src
set path2root=%cd%\..

rem set default path to org.rexxla.bsf, 2005-06-02, ---rgf
set path2engine=%path2root%\org\rexxla\bsf\engines\rexx

set path2rexxlaBsf=%path2root%\org\rexxla\bsf

set target=-target 1.4
set source=-source 1.4

rem default to Apache's BSF
set javaSrc=%cd%\source_java
echo Compiling for [%javaSrc%] ...

md %path2engine%
@echo off

echo path2root     =%path2root%
echo path2engine   =%path2engine%
echo path2rexxlaBsf=%path2rexxlaBsf%
echo javaSRc       =%javaSrc%
pause

@echo on
rem re-activated deletion to make sure that no leftovers of earlier classes mix-up the runtime, rgf, 2005-11-06
del %path2engine%\*.java
del %path2engine%\*.class

copy %javaSrc%\ArrayWrapper.java              %path2engine%
copy %javaSrc%\EnumerationWrapper.java        %path2engine%
copy %javaSrc%\Java4Rexx.java                 %path2engine%
copy %javaSrc%\RexxAndJava.java               %path2engine%
copy %javaSrc%\RexxEngine.java                %path2engine%
copy %javaSrc%\RexxException.java             %path2engine%
copy %javaSrc%\RexxProxy.java                 %path2engine%
copy %javaSrc%\Supplier.java                  %path2engine%

@rem 2009-07-31
copy %javaSrc%\RexxExtendClass.java           %path2engine%
copy %javaSrc%\RexxExtendingClassLoader.java  %path2engine%

@rem 2010-04-17
copy %javaSrc%\ProxiedJavaClassTool.java      %path2engine%


copy %javaSrc%\RexxDispatcher.java            %path2rexxlaBsf%


javac %source% %target% %javaSrc%\ArrayWrapper.java                 && copy %javaSrc%\ArrayWrapper*               %path2engine%
javac %source% %target% %javaSrc%\EnumerationWrapper.java           && copy %javaSrc%\EnumerationWrapper*         %path2engine%
javac %source% %target% %javaSrc%\Java4Rexx.java                    && copy %javaSrc%\Java4Rexx*                  %path2engine%
javac %source% %target% %javaSrc%\RexxAndJava.java                  && copy %javaSrc%\RexxAndJava*                %path2engine%
javac %source% %target% %javaSrc%\RexxEngine.java                   && copy %javaSrc%\RexxEngine*                 %path2engine%
javac %source% %target% %javaSrc%\RexxException.java                && copy %javaSrc%\RexxException*              %path2engine%
javac %source% %target% %javaSrc%\RexxProxy.java                    && copy %javaSrc%\RexxProxy*                  %path2engine%
javac %source% %target% %javaSrc%\Supplier.java                     && copy %javaSrc%\Supplier*                   %path2engine%

@rem 2009-07-31
javac %source% %target% %javaSrc%\RexxExtendClass.java              && copy %javaSrc%\RexxExtendClass*            %path2engine%
javac %source% %target% %javaSrc%\RexxExtendingClassLoader.java     && copy %javaSrc%\RexxExtendingClassLoader*   %path2engine%

javac %source% %target% %javaSrc%\RexxDispatcher.java               && copy %javaSrc%\RexxDispatcher*             %path2rexxlaBsf%

@rem 2010-04-17
javac %source% %target% %javaSrc%\ProxiedJavaClassTool.java         && copy %javaSrc%\ProxiedJavaClassTool*       %path2engine%

endlocal
