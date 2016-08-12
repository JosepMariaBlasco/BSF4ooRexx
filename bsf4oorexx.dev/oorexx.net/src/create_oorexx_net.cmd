@echo off
rem rgf, 2016-08-11 - compile Java classes, create jar file which should replace the one in ..\bin
rem rgf, 2016-08-12 - compile the csharp dll and exe, files should replace the ones in ..\bin

rem adjust path to your MS Visual Projecte's "msbuild.exe"
set msbuild_cmd="c:\Program Files (x86)\MSBuild\14.0\bin\msbuild.exe"


echo ----------------------------------------------------------------
rem general: bin directory location
set bindir=..\bin

rem Java related definitions
rem "oorexx.net.jar"

set jni4net_jar=%bindir%\jni4net.j-0.8.8.0.jar
set java_compile_opts=-source 1.5 -target 1.5
set tgt_jar=oorexx.net.jar

set jni4net_dir=jni4net\java
set helper_dir=Helper4ooRexx4Net

@rem ----------------------------------------------------------------
rem csharp related definitions (MS Visual Studio), adapt to your configuration

rem assembly "oorexx.net.dll"
set sln_oorexx_net_dll=jni4net\csharp\oorexx.net.sln
set tgt_oorexx_net_dll=jni4net\csharp\bin\Release\oorexx.net.dll

@rem ------------------
rem assembly "gacutilrgf.exe"
set sln_gacutilrgf_exe=gacutilrgf\GAC_rgf.sln
set tgt_gacutilrgf_exe=gacutilrgf\GAC_rgf\bin\Release\*exe*



@rem ----------------------------------------------------------------

@echo copying CLR.CLS to %bindir% ...
copy /y CLR.CLS %bindir%

@echo ---------


@echo compiling Java files and building "oorexx.net.jar" ...
echo on
javac %java_compile_opts% -cp %jni4net_jar% %jni4net_dir%\system\*java
javac %java_compile_opts% -cp %jni4net_jar% %helper_dir%\org\oorexx\clr\*java

jar -cvf %tgt_jar% -C %jni4net_dir% .
jar -uvf %tgt_jar% -C %helper_dir% .

echo to delete sourcefiles -    zip -d %tgt_jar% *.java
copy /y %tgt_jar% %bindir%

@echo ---------

@echo compiling and building assembly "oorexx.net.dll" ...
%msbuild_cmd% /t:Clean %sln_file%
%msbuild_cmd% /t:Build /p:Configuration=Release %sln_oorexx_net_dll%
copy /y %tgt_oorexx_net_dll% %bindir%

@echo ---------


@echo compiling and building assembly "gacutilrgf.exe" ...
%msbuild_cmd% /t:Clean %sln_gacutilrgf_exe%
%msbuild_cmd% /t:Build /p:Configuration=Release %sln_gacutilrgf_exe%
copy /y %tgt_gacutilrgf_exe% %bindir%

@echo ---------

@echo make sure to delete all security attributes from the ..\bin files
@echo by issuing on the command line:
@echo     for German  Windows: "cacls ..\bin\* /t /g jeder:f"
@echo     for Englsih Windows: "cacls ..\bin\* /t /g everyone:f"
