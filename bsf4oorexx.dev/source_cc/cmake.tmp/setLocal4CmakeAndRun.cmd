@rem according to <https://cedanet.com.au/ceda/downloads/java/jni-example.php>, 2023-02-25
rem @ECHO OFF
setlocal

@REM SET MGF=%CD%
SET SAVED_DIR=%CD%
SET SRCDIR=%SAVED_DIR%/../source_cc
SET BUILD=%SAVED_DIR%/build

call "e:\Programme\Microsoft Visual Studio 14.0\VC\vcvarsall.bat" amd64
set include=%include%;C:\Program Files (x86)\Microsoft SDKs\Windows\v7.1A\Include
set PATH=%programFiles%\CMake\bin;%PATH%

set INCLUDE=%INCLUDE%;%REXX_HOME%\api
echo INCLUDE=%include%

set LIB=%LIB%;%REXX_HOME%\api
echo LIB    =%lib%
echo.

SET WINDOWS_COMPILER="cl.exe"
::SET WINDOWS_COMPILER="C:/Program Files/LLVM/bin/clang-cl.exe"

rem remove quietly the build directory
rd "%BUILD%" /s /q

rem create build directory (creates intermediate directories as needed)
MKDIR "%BUILD%\x64-Release"

CD "%BUILD%\x64-Release"

rem add ooRexx related paths: include and lib
rem echo a) INCLUDE=%INCLUDE%
rem set INCLUDE=%INCLUDE%;%REXX_HOME%\api
rem echo b) INCLUDE=%INCLUDE%
rem echo.
rem echo a) LIB=%LIB%
rem set LIB=%LIB%;%REXX_HOME%\api
rem echo b) LIB=%LIB%
rem echo.

echo before cmake: %cd%


rem -DCMAKE_TOOLCHAIN_FILE="%VCPKG_ROOT_DIR%/scripts/buildsystems/vcpkg.cmake" ^

cmake -G "NMake Makefiles" ^
-DCMAKE_INSTALL_PREFIX:PATH="%BUILD%\install\x64-Release" ^
-DCMAKE_WINDOWS_EXPORT_ALL_SYMBOLS="TRUE" ^
-DCMAKE_CXX_COMPILER=%WINDOWS_COMPILER% ^
-DCMAKE_C_COMPILER=%WINDOWS_COMPILER% ^
-DCMAKE_BUILD_TYPE=RelWithDebInfo ^
-DCMAKE_MAKE_PROGRAM=nmake ^
"%SRCDIR%"

rem cmake --build . -v
nmake

@REM CD %MGF%
CD %SAVED_DIR%
@REM COPY "%BUILD%\x64-Release\BSF4ooRexx850.dll" .
COPY "%BUILD%\x64-Release\BSF4ooRexx850.dll" "%saved_DIR%"
echo %CD%
dir /od "%saved_dir%"
PAUSE

