@echo off
rem rgf, 2016-06-27,28, v3, uninstallation script for jni4net and oorexx.net

set jni4net_dir=%windir%\assembly\GAC_MSIL\jni4net.n-0.8.8.0
set oorexx_dir=%windir%\Microsoft.NET\assembly\GAC_MSIL\oorexx.net

@echo "removing [%oorexx_dir%] and [%jni4net_dir%]..."
@echo on
      rd /s /q %oorexx_dir%
      rd /s /q %jni4net_dir%
