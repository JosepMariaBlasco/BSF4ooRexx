@rem according to <https://cedanet.com.au/ceda/downloads/java/jni-example.php>, 2023-02-25
 rem 2023-02-06 - simplify; adjust
@ECHO OFF
setlocal

SET SAVED_DIR=%CD%
SET BUILD=%SAVED_DIR%/build

rem -------------------------------------------
rem *** local, adjust accordingly or remove if access to compiler and cmake is established

rem directory where BSF4ooRexx.cc is located:
SET SRCDIR=%SAVED_DIR%/..

rem make compiler available (here MSC)
SET WINDOWS_COMPILER="cl.exe"
echo WINDOWS_COMPILER: %WINDOWS_COMPILER%
call "e:\Programme\Microsoft Visual Studio 14.0\VC\vcvarsall.bat" amd64

rem make cmake accessible
set PATH=%programFiles%\CMake\bin;%PATH%
rem -------------------------------------------


rem remove quietly the build directory
rd "%BUILD%" /s /q 2>nul
rem create build directory (creates intermediate directories as needed)
MKDIR "%BUILD%\x64-Release" 2>nul

CD "%BUILD%\x64-Release"
echo running cmake from: %cd%
echo.

cmake -G "NMake Makefiles" ^
-DCMAKE_INSTALL_PREFIX:PATH="%BUILD%\install\x64-Release" ^
-DCMAKE_WINDOWS_EXPORT_ALL_SYMBOLS="TRUE" ^
-DCMAKE_CXX_COMPILER=%WINDOWS_COMPILER% ^
-DCMAKE_BUILD_TYPE=RelWithDebInfo ^
-DCMAKE_MAKE_PROGRAM=nmake ^
"%SRCDIR%"

cmake --build .

CD %SAVED_DIR%
COPY "%BUILD%\x64-Release\BSF4ooRexx850.dll" .
echo %CD%
dir /od "%saved_dir%"

