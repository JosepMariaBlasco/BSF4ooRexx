#!/usr/bin/env rexx
/*
   Name:    setupbsf4oorexx.rex
   Purpose: create the scripts "rexxj.{cmd|sh}" and "setenv2bsf4oorexx.{cmd|sh}"; if
            OpenOffice (AOO) or LibreOffice (LO) can be found, then make it available
   Usage:   rexx setupbsf4oorexx.rex
   Date:    2022-02-07
   Needs:   Java to get the UNO CLASSPATH definition from AOO/LO
   license:
    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2022 Rony G. Flatscher

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.
    -----------------------------------------------------------------------------
*/

.context~package~local~bDry  =.false -- .true    -- if .true then commands are shown only, otherwise they get executed
.context~package~local~bDebug=.false -- .true    -- if .true show debug output

parse source s +1    -- get current operating system: W[indows], L[inux], D[arwin]
if s="W" then
do
   .context~package~local~bIsWindows=.true
   .context~package~local~bIsUnix   =.false
   .context~package~local~bIsMacOS  =.false
end
else
do
   .context~package~local~bIsWindows=.false
   .context~package~local~bIsUnix   =.true
   .context~package~local~bIsMacOS  =(s='D') -- "Darwin"
end

_dateTime=.dateTime~new
_rexx_executable              =.rexxInfo~executable
_rexx_version                 =.rexxInfo~name
.context~package~local~pathSep=.rexxInfo~pathSeparator
.context~package~local~dirSep =.rexxinfo~directorySeparator
.context~package~local~bitness=.rexxinfo~architecture

   -- get location of this script, must be located in the unzipped root of the portable package
parse source . . fullpath
_location=filespec("location",fullpath)
.context~package~local~_location=_location

_bsf4oorexx_bin=_location"bin"

   -- collect jar-files (could be more than one over time)
_bsf4oorexx_classpath=""
fileSpec=_location"jars".dirSep"*.jar"
call sysFileTree fileSpec, files., "fos"
if .bDebug=.true then say "... #" .line" / jar files found from" filespec", files.0="files.0 "..."

do i=1 to files.0
   if i>1 then _bsf4oorexx_classpath=_bsf4oorexx_classpath || .pathSep || files.i
          else _bsf4oorexx_classpath=files.i
end

filespec=_bsf4oorexx_bin || .dirSep || "UNO*"
call sysfiletree fileSpec, files., "FO"
.context~package~local~bUno=(files.0>0)

if .bUno then        -- UNO-support present, try to find AOO/LO
do
   say "Setup UNO support? Checking existence of Apache OpenOffice (AOO/OOo) or LibreOffice (LO) ..."
   -- for fetching UNO CLASSPATH via unoinfo we need Java!
   .context~package~local~java_binary = get_java_binary()
   if .java_binary~isNil then       -- test whether Java is available
      say "'java' not present, cannot figure out UNO's CLASSPATH, skipping this step ..."
   else
   do
      if .bDebug then say '.java_binary:' .java_binary
      res=get_uno_classpath()
      if res<>"" then   -- add UNO_CLASSPATH
         _bsf4oorexx_classpath=_bsf4oorexx_classpath || .pathSep || res
      else
      do
         say "... UNO-Office (AOO or LO) not found, probably not installed"
         say "    (you may want to check out <https://www.openoffice.org/>)"
      end
   end
   say
end

if .bIsWindows=.true then
do
   _username=value("USERNAME", , "environment") -- get current user's user name
   _bsf4oorexx_lib=_location"bin"
   rexxj_code     =.resources~win_rexxj
   setenv2b4r_code=.resources~win_setenv2b4r
   rexxj_name     ="rexxj.cmd"
   setenv2b4r_name="setenv2bsf4oorexx.cmd"
end
else
do
   _username=value("USER"    , , "environment") -- get current user's user name
   _bsf4oorexx_lib=_location"lib"
   if .bIsMacOS=.true then
   do
      rexxj_code     =.resources~mac_rexxj
      setenv2b4r_code=.resources~mac_setenv2b4r
   end
   else
   do
      rexxj_code     =.resources~linux_rexxj
      setenv2b4r_code=.resources~linux_setenv2b4r
   end
   rexxj_name     ="rexxj.sh"
   setenv2b4r_name="setenv2bsf4oorexx.sh"
end

rexxj_code=rexxj_code~makeString
rexxj_code=rexxj_code~changeStr("@DATETIME@",             _datetime)
rexxj_code=rexxj_code~changeStr("@USERNAME@",             _username)
rexxj_code=rexxj_code~changeStr("@REXX_VERSION@",         _rexx_version)
rexxj_code=rexxj_code~changeStr("@REXX_EXECUTABLE@",      _rexx_executable)
rexxj_code=rexxj_code~changeStr("@BSF4OOREXX_BIN@",       _bsf4oorexx_bin)
rexxj_code=rexxj_code~changeStr("@BSF4OOREXX_LIB@",       _bsf4oorexx_lib)
rexxj_code=rexxj_code~changeStr("@BSF4OOREXX_CLASSPATH@", _bsf4oorexx_classpath )    -- includes UNO_CLASSPATH, if any

if .bDry then
do
   say rexxj_name":"
   say "---"
   say rexxj_code
   say "---"
end
else
do
   s=.stream~new(rexxj_name)~~open("write replace") -- remove existing
   s~charout(rexxj_code)     -- write
   s~close
   if .bIsUnix=.true then address system "chmod 775" rexxj_name
end

-- create setupenv4bsf4oorexx.{cmd|sh}
setenv2b4r_code=setenv2b4r_code~makeString
setenv2b4r_code=setenv2b4r_code~changeStr("@DATETIME@",             _datetime)
setenv2b4r_code=setenv2b4r_code~changeStr("@USERNAME@",             _username)
setenv2b4r_code=setenv2b4r_code~changeStr("@REXX_VERSION@",         _rexx_version)
setenv2b4r_code=setenv2b4r_code~changeStr("@REXX_EXECUTABLE@",      _rexx_executable)
setenv2b4r_code=setenv2b4r_code~changeStr("@BSF4OOREXX_BIN@",       _bsf4oorexx_bin)
setenv2b4r_code=setenv2b4r_code~changeStr("@BSF4OOREXX_LIB@",       _bsf4oorexx_lib)
setenv2b4r_code=setenv2b4r_code~changeStr("@BSF4OOREXX_CLASSPATH@", _bsf4oorexx_classpath )    -- includes UNO_CLASSPATH, if any
setenv2b4r_code=setenv2b4r_code~changeStr("@REXXJ@"               , rexxj_name)
setenv2b4r_code=setenv2b4r_code~changeStr("@SETENV2B4R@"          , setenv2b4r_name)

if .bDry then
do
   say
   say setenv2b4r_name":"
   say "---"
   say setenv2b4r_code
   say "---"
end
else
do
   s=.stream~new(setenv2b4r_name)~~open("write replace") -- remove existing
   s~charout(setenv2b4r_code)     -- write
   s~close
   if .bIsUnix=.true then address system "chmod 775" setenv2b4r_name
end

   -- display readme.txt content after having the scripts created
if .bDry<>.true then
do
   if .bIsUnix then "cat readme.txt"
               else "type readme.txt"
end





/** Try to locate java and return java command as string. Try to find in this order:
   <ul>
   <li>if <code>JAVA_HOME</code> present:
      <ul>
      <li>JAVA_HOME/bin/java
      <li>JAVA_HOME/jre/bin/java
      </ul>
   <li>'java'
   </ul>
   @return the command to run java via the commandline
*/
::routine get_java_binary
  arrOut=.array~new

  javaHome=value("JAVA_HOME",,"environment")
  if javaHome="" then signal plainJava
  java_binary=javaHome || .dirSep || 'bin' || .dirSep || 'java'
  signal on any name javaHome1
  address system java_binary "-version" with output using (arrOut) error using (arrOut)
  return java_binary

javaHome1:  -- if not found, try this location
  java_binary=javaHome || .dirSep || 'jre' || .dirSep || 'bin' || .dirSep || 'java'
  signal on any name plainJava
  address system java_binary "-version" with output using (arrOut) error using (arrOut)
  return java_binary

plainJava:  -- try plain 'java'
  java_binary='java'
  signal on any name noJava
  address system java_binary "-version" with output using (arrOut) error using (arrOut)
  return java_binary

noJava:     -- o.k. no 'java' available
  return .nil


/** Routine to locate an enventually installed Apache OpenOffice or LibreOffice. If found
   then use their "unopkg java" information for defining the needed CLASSPATH setting. If
   there are multiple installations available, use the one with "soffice[.exe]" having the
   latest date.<br>

   <ul>
   <li>On Windows: search an installed office in %ProgramFiles% (64-bit) or %ProgramFiles(x86)% (32-bit)

   <li>On Linux: try to find and resolve symbolic link in "/usr/.../bin/soffice" and use that
             as default version; if not found search "/opt/ *ffice*" for "soffice" and "unoinfo",
             use the newest one

   <li>On Apple: search "/Application/ *ffice*" for "soffice" and "unoinfo", use the newest one
   </ul>
   @return UNO CLASSPATH or empty string, if unoinfo not found
*/
::routine get_uno_classpath
  uno_classpath=""
  tab="09"x
  arrInfo=.array~new    -- array to store AOO/LO unoinof results
  outArr=.array~new     -- for fetching command's stdout
  /* we use the Java command line argument "-cp" with a quoted path to
     the location which has a trailing directory separator which we need to
     remove as otherwise Java may be a little bit mixed up :)
  */
  path2javatool=._location~left(._location~length-1)

  if .bIsWindows=.true then
  do
     if .bitness=32 then pgmDir=value("ProgramFiles(x86)", , "environment")
                    else pgmDir=value("ProgramFiles", , "environment")
      -- check whether we have Office directories
     fileSpec=pgmDir || .dirSep || "*ffice*"
     call sysFileTree fileSpec, "dirs.", "DO"
     if dirs.0=0 then return ""     -- no office found

     do i=1 to dirs.0      -- try to find "unoinfo.exe"
        fileSpec=dirs.i || .dirSep || "unoinfo.exe"
        call sysFileTree fileSpec, files., "FOS"
        if files.0=0 then iterate

        cmd=.java_binary "-cp" quote(path2javatool) "JavaInfo4BSF4RexxInstallation getclasspath4ooo-only" quote(files.1)
        address system cmd with output using (outArr) -- fetch output in array
        f=.file~new(files.1)
        arrInfo~append(f~lastModified || tab || f || tab || outArr[1])  -- allows sorting by unoinfo's modification dateTime
     end
  end

  else if .bIsMacOS=.true then   -- search "/Application/ *ffice*" for "soffice" and "unoinfo", use the newest one
  do
      -- check whether we have Office directories
     fileSpec="/Applications/*ffice*"
     call sysFileTree fileSpec, "dirs.", "DO"
     if .bDebug then say "... #".line "/ .bIsMac, filespec: ["filespec"], dirs.0:" dirs.0
     if dirs.0=0 then return ""     -- no office found

     do i=1 to dirs.0      -- try to find "unoinfo.exe"
        fileSpec=dirs.i || .dirSep || "unoinfo"
        call sysFileTree fileSpec, files., "FOS"
        if files.0=0 then iterate

        cmd=.java_binary "-cp" quote(path2javatool) "JavaInfo4BSF4RexxInstallation getclasspath4ooo-only" quote(files.1)
        address system cmd with output using (outArr) -- fetch output in array
        f=.file~new(files.1)
        arrInfo~append(f~lastModified || tab || f || tab || outArr[1])  -- allows sorting by unoinfo's modification dateTime
     end
  end

  else   -- Unix: try to find /usr/ ... bin/soffice, use its location, otherwise look in /opt
  do
     cmd="find /usr -name soffice -ls"
     address system cmd with output using (outArr)
     do path2soffice over outArr
         parse var path2soffice "->" p2s
         p2s=p2s~strip
         if p2s~endsWith("soffice") then  -- can we find unoinfo there? if so, use it
         do
             unoinfo=p2s~left(p2s~length-7)"unoinfo"
             if sysFileExists(unoinfo) then
             do
                cmd=.java_binary "-cp" quote(path2javatool) "JavaInfo4BSF4RexxInstallation getclasspath4ooo-only" quote(unoinfo)
                address system cmd with output using (outArr) -- fetch output in array
                f=.file~new(unoinfo)
                arrInfo~append(f~lastModified || tab || f || tab || outArr[1])  -- allows sorting by unoinfo's modification dateTime
                -- signal done   -- we are done
             end
         end
     end
      -- check whether we have Office directories
     fileSpec="/opt/*ffice*"
     call sysFileTree fileSpec, "dirs.", "DO"
     if dirs.0=0 then return ""     -- no office found

     do i=1 to dirs.0      -- try to find "unoinfo.exe"
        fileSpec=dirs.i || .dirSep || "unoinfo"
        call sysFileTree fileSpec, files., "FOS"
        if files.0=0 then iterate

        cmd=.java_binary "-cp" quote(path2javatool) "JavaInfo4BSF4RexxInstallation getclasspath4ooo-only" quote(files.1)
        address system cmd with output using (outArr) -- fetch output in array
        f=.file~new(files.1)
        arrInfo~append(f~lastModified || tab || f || tab || outArr[1])  -- allows sorting by unoinfo's modification dateTime
     end
  end

done:
  if .bDebug=.true then say "arr~items:" arrInfo~items "//" arrInfo

  items=arrInfo~items
  if items=0 then return ""
  arrInfo=arrInfo~sort     -- sort ascendingly, return latest AOO/LO (last item in array)
  tmp=""
  do i=1 to items          -- show fully qualified path to unoinfo with its modification dateTime
     parse value arrInfo[i] with dt (tab) uno_info_path (tab) uno_classpath
     if tmp=uno_info_path then iterate
     tmp=uno_info_path
     hint="  "
     if i=items then hint="->"   -- show which one got picked
     say "..." hint uno_info_path "("dt")"
  end
  return uno_classpath     -- return UNO CLASSPATH


::routine quote
  return '"' || arg(1) || '"'


/* ************************************************************************** */
/* rexxj.{cmd|sh}                                                             */
/* ************************************************************************** */

/* ========================================================================== */
::resource linux_rexxj
#!/bin/sh
# rexxj.sh: script to run "BSF4ooRexx" programs without prior installation ...
#           rexxj.sh testbsf4oorexx.rex
#
# created: '@DATETIME@' by '@USERNAME@'
# Rexx executable used to create this script: '@REXX_EXECUTABLE@', version: '@REXX_VERSION@'

# set PATH environment variable
export PATH=@BSF4OOREXX_BIN@:.:$PATH

# set CLASSPATH environment variable
export CLASSPATH=@BSF4OOREXX_CLASSPATH@:.:$CLASSPATH

# set LD_LIBRARY_PATH
TMP_LIB_PATH=@BSF4OOREXX_LIB@:/usr/java/packages/lib:/usr/lib64:/lib64:/lib:/usr/lib
[ "$LD_LIBRARY_PATH" != "" ] && LD_LIBRARY_PATH=$TMP_LIB_PATH:$LD_LIBRARY_PATH || LD_LIBRARY_PATH=$TMP_LIB_PATH

# PREPEND_LIBRARY_PATH optional environment variable, will be put at head of library path
[ "$PREPEND_LIBRARY_PATH" != "" ] && LD_LIBRARY_PATH=$PREPEND_LIBRARY_PATH:$LD_LIBRARY_PATH

export LD_LIBRARY_PATH

# Java start command, allows passing arguments to Rexx script
# if JAVA_HOME defined, use its java binary
if [ -e "${JAVA_HOME}/bin/java" ] ; then JAVA_BINARY=${JAVA_HOME}/bin/java ;
elif [ -e "${JAVA_HOME}/jre/bin/java"  ] ; then JAVA_BINARY=${JAVA_HOME}/jre/bin/java ;
elif [ -e "/usr/bin/java" ] ; then JAVA_BINARY=/usr/bin/java ;
else JAVA_BINARY=java
fi

# export LD_DEBUG=libs reloc statistics

"$JAVA_BINARY"  -Djava.library.path="$LD_LIBRARY_PATH"  $BSF4Rexx_JavaStartupOptions org.rexxla.bsf.RexxDispatcher $*
::END


/* ========================================================================== */
::resource mac_rexxj
#!/bin/sh
# rexxj.sh: script to run "BSF4ooRexx" programs without prior installation ...
#           rexxj.sh testbsf4oorexx.rex
#
# created: '@DATETIME@' by '@USERNAME@'
# Rexx executable used to create this script: '@REXX_EXECUTABLE@', version: '@REXX_VERSION@'

# set PATH environment variable
export PATH=@BSF4OOREXX_BIN@:$PATH

# set CLASSPATH environment variable
export CLASSPATH=@BSF4OOREXX_CLASSPATH@:.:$CLASSPATH

# set DYLD_LIBRARY_PATH
TMP_LIB_PATH=@BSF4OOREXX_LIB@:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:.
[ "$DYLD_LIBRARY_PATH" != "" ] && DYLD_LIBRARY_PATH=$DYLD_LIBRARY_PATH:$TMP_LIB_PATH || DYLD_LIBRARY_PATH=$TMP_LIB_PATH

# as of 2021-07-24 Apple's SIP (macOS Big Sur 11.4) clears DYLD_LIBRARY_PATH and even LD_LIBRARY_PATH environment variables from new shell sessions, use PREPEND_LIBRARY_PATH instead
# PREPEND_LIBRARY_PATH optional environment variable, will be put at head of library path
[ "$PREPEND_LIBRARY_PATH" != "" ] && DYLD_LIBRARY_PATH=$PREPEND_LIBRARY_PATH:$DYLD_LIBRARY_PATH

export DYLD_LIBRARY_PATH

# Java start command, allows passing arguments to Rexx script
# if JAVA_HOME defined, use its java binary
if [ -e "${JAVA_HOME}/bin/java" ] ; then JAVA_BINARY=${JAVA_HOME}/bin/java ;
elif [ -e "${JAVA_HOME}/jre/bin/java"  ] ; then JAVA_BINARY=${JAVA_HOME}/jre/bin/java ;
elif [ -e "/usr/bin/java" ] ; then JAVA_BINARY=/usr/bin/java ;
else JAVA_BINARY=java
fi

# export DYLD_PRINT_LIBRARIES=1
# export DYLD_PRINT_LIBRARIES_POST_LAUNCH=1
# export DYLD_PRINT_RPATHS=1
# export DYLD_PRINT_STATISTICS=1

"$JAVA_BINARY"  -Djava.library.path="$DYLD_LIBRARY_PATH"  $BSF4Rexx_JavaStartupOptions org.rexxla.bsf.RexxDispatcher $*
::END

/* ========================================================================== */
::resource win_rexxj
@echo off
REM rexxj.cmd: script to run "BSF4ooRexx" programs without prior installation, e.g.
REM            rexxj.cmd testbsf4oorexx.rex

REM created: '@DATETIME@' by '@USERNAME@'
REM Rexx executable version used to create this script: '@REXX_EXECUTABLE@', version: '@REXX_VERSION@'

setlocal enableDelayedExpansion

REM  set CLASSPATH environment variable
set CLASSPATH=@BSF4OOREXX_CLASSPATH@;.;%CLASSPATH%

REM  Java start command, allows passing arguments to Rexx script

if not "%PREPEND_LIBRARY_PATH%"=="" ( set TMP_LIB_PATH=!PREPEND_LIBRARY_PATH!;@BSF4OOREXX_LIB@
                             ) else ( set TMP_LIB_PATH=@BSF4OOREXX_LIB@)
set TMP_LIB_PATH=!TMP_LIB_PATH!;%PATH%

REM  if JAVA_HOME defined, use its java binary
@rem enables referring via exclamation mark within blocks; from now on must not use endlocal
if not "%JAVA_HOME%" == "" (
   set jexe=!JAVA_HOME!\bin\java.exe
   if exist "!jexe!"  (
     set java_binary=!jexe!
   ) else (
      set jexe=!JAVA_HOME!\jre\bin\java.exe
      if exist "!jexe!" (
        set java_binary=!jexe!
      )
   )
)
@rem if original java.exe is not present anymore use just java as a last resort
if not exist "%JAVA_BINARY%" set JAVA_BINARY=java

"%JAVA_BINARY%"  -Djava.library.path="%TMP_LIB_PATH%"  %BSF4Rexx_JavaStartupOptions% org.rexxla.bsf.RexxDispatcher %*
::END

/* ========================================================================== */

/* ************************************************************************** */
/* setenv2bsf4oorexx.{cmd|sh}                                                 */
/* ************************************************************************** */

/* ========================================================================== */
::resource linux_setenv2b4r
#!/bin/sh
# @SETENV2B4R@: script to allow "BSF4ooRexx" programs to run
#
# set environment:
#
#    . @SETENV2B4R@
#
# then e.g.
#
#    rexx      testbsf4oorexx.rex
#
# instead of:
#
#    @REXXJ@ testbsf4oorexx.rex

# created: "@DATETIME@" by user "@USERNAME@"
# Rexx executable version used to create this script: '@REXX_EXECUTABLE@', version: '@REXX_VERSION@'

# setting the CLASSPATH environment variable...
[ "$CLASSPATH" != "" ] && TMPCP=:$CLASSPATH || TMPCP=
export CLASSPATH=@BSF4OOREXX_CLASSPATH@:.$TMPCP
echo CLASSPATH=$CLASSPATH
echo

export PATH=@BSF4OOREXX_BIN@:$PATH
echo PATH=$PATH
echo

[ "$LD_LIBRARY_PATH" != "" ] && TMPLP=:$LD_LIBRARY_PATH || TMPLP=
export LD_LIBRARY_PATH=@BSF4OOREXX_LIB@${TMPLP}
echo LD_LIBRARY_PATH=$LD_LIBRARY_PATH
::END

/* ========================================================================== */
::resource mac_setenv2b4r
#!/bin/sh
# @SETENV2B4R@: script to allow "BSF4ooRexx" programs to run
#
# set environment:
#
#    . @SETENV2B4R@
#
# then e.g. (for headless, i.e. non-GUI programs)
#
#    rexx      testbsf4oorexx.rex
#
# instead of:
#
#    @REXXJ@ testbsf4oorexx.rex

# created: "@DATETIME@" by user "@USERNAME@"
# Rexx executable version used to create this script: '@REXX_EXECUTABLE@', version: '@REXX_VERSION@'

# setting the CLASSPATH environment variable...
[ "$CLASSPATH" != "" ] && TMPCP=:$CLASSPATH || TMPCP=
export CLASSPATH=@BSF4OOREXX_CLASSPATH@:.$TMPCP
echo CLASSPATH=$CLASSPATH
echo

export PATH=@BSF4OOREXX_BIN@:$PATH
echo PATH=$PATH
echo

[ "$DYLD_LIBRARY_PATH" != "" ] && TMPDLP=:$DYLD_LIBRARY_PATH || TMPDLP=
export DYLD_LIBRARY_PATH=@BSF4OOREXX_LIB@${TMPDLP}
echo DYLD_LIBRARY_PATH=$DYLD_LIBRARY_PATH
::END

/* ========================================================================== */
::resource win_setenv2b4r
@echo off
REM @SETENV2B4R@: script to allow "BSF4ooRexx" programs to run
REM
REM set environment:
REM
REM    @SETENV2B4R@
REM
REM then e.g.
REM
REM    rexx      testbsf4oorexx.rex
REM
REM instead of:
REM
REM    rexxj.cmd testbsf4oorexx.rex

REM  created: "@DATETIME@" by user "@USERNAME@"
REM  Rexx executable version used to create this script: '@REXX_EXECUTABLE@', version: '@REXX_VERSION@'

echo setting the CLASSPATH environment variable...
if "%CLASSPATH%"=="" (set CLASSPATH=@BSF4OOREXX_CLASSPATH@;.) else (set CLASSPATH=@BSF4OOREXX_CLASSPATH@;.;%CLASSPATH%)
echo CLASSPATH=%CLASSPATH%
echo.

REM  setting the PATH environment variable...
set PATH=@BSF4OOREXX_BIN@;%PATH%
echo PATH=%PATH%
echo.

echo "done (more Rexx language related information: http://www.RexxLA.org)"
::END

