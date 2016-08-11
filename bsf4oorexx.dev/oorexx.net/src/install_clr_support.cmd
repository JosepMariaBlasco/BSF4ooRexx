@echo off
rem rgf, 2016-06-27, 28, v3, installation script for jni4net and oorexx.net

set jni4net_dll=jni4net.n-0.8.8.0.dll
set jni4net_dir_a=%windir%\assembly\GAC_MSIL\jni4net.n-0.8.8.0
set jni4net_dir_b=%windir%\assembly\GAC_MSIL\jni4net.n-0.8.8.0\0.8.8.0__134a23405600bab4

@echo "copying [%jni4net_dll%] to the global assembly cache (GAC) into [%jni4net_dir%]..."
@echo on
      md   %jni4net_dir_a%
      md   %jni4net_dir_b%
      copy %jni4net_dll% %jni4net_dir_b%
@echo off

set oorexx_dll=oorexx.net.dll
set oorexx_dir_a=%windir%\Microsoft.NET\assembly\GAC_MSIL\oorexx.net
set oorexx_dir_b=%windir%\Microsoft.NET\assembly\GAC_MSIL\oorexx.net\v4.0_0.0.0.0__795d24d1afecbdfa
@echo "copying [%oorexx_dll%] to the global assembly cache (GAC) into [%oorexx_dir%]..."
@echo on
      md   %oorexx_dir_a%
      md   %oorexx_dir_b%
      copy %oorexx_dll% %oorexx_dir_b%


