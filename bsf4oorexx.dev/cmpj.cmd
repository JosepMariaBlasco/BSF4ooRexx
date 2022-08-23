@echo off
rem  ------------------------ Apache Version 2.0 license -------------------------
rem     Copyright (C) 2001-2022 Rony G. Flatscher
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
rem 2016-11-04, rgf: now basing BSF4ooRexx on Java 1.6/6.0
rem 2022-08-10, rgf: now basing BSF4ooRexx on Java 1.8/8.0

@echo on
@echo off
setlocal

rem set path2root=E:\rony\dev\bsf\src
set path2root=%cd%\..

rem set default path to org.rexxla.bsf, 2005-06-02, ---rgf
set path2engine=%path2root%\org\rexxla\bsf\engines\rexx
set path2jsr223=%path2root%\org\rexxla\bsf\engines\rexx

set path2rexxlaBsf=%path2root%\org\rexxla\bsf

rem rgf, 20140927: make sure that the extension jar is not picked up (if bsf4oorexx-jar got installed there)
set javacDefines=-Djava.ext.dirs=""
set target=-target 8
set source=-source 8
@rem baseline is Java 1.6/6 (decided during 2017 as earlier Java version deployments cannot be located anymore)
set javaBaseLine=-source 8   -target 8

@rem set java7=-source 1.7 -target 1.7  --> baseline is Java 8 use 'java8' instead as a result

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
echo deleting .java and .class files from %path2engine% ...
del %path2engine%\*.java
del %path2engine%\*.class

echo deleting .java and .class files from %path2jsr223% ...
del %path2jsr223%\*.java
del %path2jsr223%\*.class

copy %javaSrc%\ArrayWrapper.java              %path2engine%
copy %javaSrc%\EnumerationWrapper.java        %path2engine%
copy %javaSrc%\Java4Rexx.java                 %path2engine%
copy %javaSrc%\RexxAndJava.java               %path2engine%
copy %javaSrc%\RexxEngine.java                %path2engine%
copy %javaSrc%\RexxException.java             %path2engine%
copy %javaSrc%\RexxProxy.java                 %path2engine%
copy %javaSrc%\Supplier.java                  %path2engine%
@rem 2010-04-17
copy %javaSrc%\RexxHandler.java               %path2engine%
copy %javaSrc%\RexxExitHandler.java           %path2engine%
copy %javaSrc%\RexxCommandHandler.java        %path2engine%
copy %javaSrc%\RexxRedirectingCommandHandler.java       %path2engine%
copy %javaSrc%\AbstractRedirectingCommandHandler.java   %path2engine%
copy %javaSrc%\AbstractDirectCommandHandler.java        %path2engine%
copy %javaSrc%\RexxConfiguration.java         %path2engine%

@rem 2009-07-31
rem copy %javaSrc%\RexxExtendClass.java           %path2engine%
rem copy %javaSrc%\RexxExtendingClassLoader.java  %path2engine%

@rem 2010-04-17
copy %javaSrc%\ClassAdapter.java              %path2engine%
copy %javaSrc%\RexxDispatcher.java            %path2rexxlaBsf%

copy %javaSrc%\RexxScriptEngine.java          %path2jsr223%
copy %javaSrc%\RexxScriptEngineFactory.java   %path2jsr223%
copy %javaSrc%\RexxCompiledScript.java        %path2jsr223%

@rem 2018-01-05/08
copy %javaSrc%\InvocationType.java            %path2engine%
copy %javaSrc%\RexxReflectInterface.java      %path2engine%
copy %javaSrc%\RexxReflectJava6.java          %path2engine%
copy %javaSrc%\RexxReflectJava7.java          %path2engine%
copy %javaSrc%\RexxReflectUtil.java           %path2engine%

@rem 2018-11-01
copy %javaSrc%\RexxStrictArgument.java        %path2engine%

@rem 2021-08-04
copy %javaSrc%\RexxCleanupRef.java            %path2engine%


@rem do the compilations

javac %javacDefines% %javaBaseLine%        %javaSrc%\InvocationType.java        && copy %javaSrc%\InvocationT*                %path2engine%
javac %javacDefines% %javaBaseLine%        %javaSrc%\RexxReflectInterface.java  && copy %javaSrc%\RexxReflectInterface*       %path2engine%
javac %javacDefines% %javaBaseLine%        %javaSrc%\RexxReflectJava6.java      && copy %javaSrc%\RexxReflectJava6*           %path2engine%
javac %javacDefines% %javaBaseLine%        %javaSrc%\RexxReflectJava7.java      && copy %javaSrc%\RexxReflectJava7*           %path2engine%
javac %javacDefines% %javaBaseLine%        %javaSrc%\RexxReflectUtil.java       && copy %javaSrc%\RexxReflectUtil*            %path2engine%


@rem 2010-04-17
javac %javacDefines% %javaBaseLine%    %javaSrc%\ArrayWrapper.java              && copy %javaSrc%\ArrayWrapper*               %path2engine%
javac %javacDefines% %javaBaseLine%    %javaSrc%\EnumerationWrapper.java        && copy %javaSrc%\EnumerationWrapper*         %path2engine%
javac %javacDefines% %javaBaseLine%    %javaSrc%\Java4Rexx.java                 && copy %javaSrc%\Java4Rexx*                  %path2engine%
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxAndJava.java               && copy %javaSrc%\RexxAndJava*                %path2engine%
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxEngine.java                && copy %javaSrc%\RexxEngine*                 %path2engine%
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxException.java             && copy %javaSrc%\RexxException*              %path2engine%
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxProxy.java                 && copy %javaSrc%\RexxProxy*                  %path2engine%
javac %javacDefines% %javaBaseLine%    %javaSrc%\Supplier.java                  && copy %javaSrc%\Supplier*                   %path2engine%

@rem 2009-07-31
rem javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxExtendClass.java           && copy %javaSrc%\RexxExtendClass*            %path2engine% && copy %javaSrc%\MethodForSuperData.class  %path2engine% && copy %javaSrc%\TransformationData.class  %path2engine%
rem javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxExtendingClassLoader.java  && copy %javaSrc%\RexxExtendingClassLoader*   %path2engine%

javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxDispatcher.java            && copy %javaSrc%\RexxDispatcher*             %path2rexxlaBsf%

@rem 2010-04-17
javac %javacDefines% %javaBaseLine%    %javaSrc%\ClassAdapter.java              && copy %javaSrc%\ClassAdapter*               %path2engine%

@rem 2010-04-17
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxHandler.java               && copy %javaSrc%\RexxHandler*                %path2engine%
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxExitHandler.java           && copy %javaSrc%\RexxExitHandler*            %path2engine%
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxCommandHandler.java        && copy %javaSrc%\RexxCommandHandler*         %path2engine%
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxRedirectingCommandHandler.java        && copy %javaSrc%\RexxRedirectingCommandHandler*         %path2engine%

javac %javacDefines% %javaBaseLine%    %javaSrc%\AbstractRedirectingCommandHandler.java    && copy %javaSrc%\AbstractRedirectingCommandHandler*         %path2engine%
javac %javacDefines% %javaBaseLine%    %javaSrc%\AbstractDirectCommandHandler.java         && copy %javaSrc%\AbstractDirectCommandHandler*         %path2engine%

javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxConfiguration.java         && copy %javaSrc%\RexxConfiguration*          %path2engine%


@rem 2015-05-11
rem 2016-11-04, rgf: changed Java baseline from 1.4 to 1.6
rem set target=-target 1.6
rem set source=-source 1.6

javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxScriptEngine*.java         && copy %javaSrc%\RexxScriptEngine*.*         %path2jsr223%
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxScriptEngineFactory.java   && copy %javaSrc%\RexxScriptEngineFactory*    %path2jsr223%
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxCompiledScript.java        && copy %javaSrc%\RexxCompiledScript*         %path2jsr223%

@rem 2018-11-01, 2018-11-22
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxStrictArgument.java        && copy %javaSrc%\RexxStrictArgument*         %path2engine%

@rem 2018-11-01, 2018-11-22
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxCleanupRef.java            && copy %javaSrc%\RexxCleanupRef*             %path2engine%

@rem 2022-04-21: needs Java 1.8 due to using Arrays.stream()
javac %javacDefines% %javaBaseLine%    %javaSrc%\RexxAnalyzeRegistry.java       && copy %javaSrc%\RexxAnalyzeRegistry*        %path2engine%

endlocal

