#!/usr/bin/env rexx
/*
   Motivation for this program (context), rgf, 20250828:

   With the introduction of FFM (foreign function and foreign memory) to Java
   the FFM developers came up with the idea to automatically warn users of
   modules that employ FFM as its wrong use may cause crashes. This may be
   likely, if Java programmers use FFM who do not have a deep knowledge of
   C or the problems related to direct memory access.

   This is very different for JNI programmers (Java native interface). They
   use C or C++ professionally and usually have a deep knowledge, experience
   and therefore insight.

   Examples are the Java developers themselves who need to program natively
   in order to access resources from the operating system and the like and
   who must use JNI. JNI is therefore systematically tested and checked
   during development to not cause any crashes.

   Unfortunately, the FFM developers put the danger of crashes using JNI at
   the same level as FFM and therefore force Java users to now run the JVM
   with the switch "--enable-native-access=ALL-UNNAMED" otherwise warnings
   occur starting with Java/OpenJDK 24 which in future versions of Java may
   even lead to Java evoking errors, such that perfectly well running Java
   applications stop to work for no good reason and even telling the untruth
   that (proofed, tested) JNI applications are dangerous!

   In the case of BSF4ooRexx850 the programmers and users usually do not
   have a thorough knowledge of such specifics of Java and become helpless
   from one day to the next if such a warning pops up after a Java update!
   Java tells them effectively that what they have been doing (using
   BSF4ooRexx850) for more than 20 years safely is dangerous, which is not!

   In order to inhibit this misleading warning one needs to start the JVM
   with the switch "--enable-native-access=".

   This program will check for the version of Java that gets used by default
   and if it is Java/OpenJDK 24 or newer (each six months there is a new
   version) then the BSF4ooRexx850 honored environment variables are set as
   follows:

      BSF4Rexx_quiet=1
      BSF4Rexx_JavaStartupOptions=[current_BSF4Rexx_JavaStartupOptions_settings ]--enable-native-access=ALL-UNNAMED

   The BSF4ooRexx850 various launchers and BSF.CLS have been honoring these
   environment variables which with these settings will not inform the user
   about using "BSF4Rexx_JavaStartupOptions" and the used value (if
   BSF4Rexx_JavaStartupOptions is set already the above value gets appended
   to it). These changes should make BSF4ooRexx850 programs to appear to
   behave at startup as they have been behaving for 25 (!) years.

author:  Rony G. Flatscher

purpose: allow programs to run without the wrong message that using BSF4ooRexx850
         would be dangerous and inhibit Java to force not using it in the future
         (BSF4ooRexx850 must use JNI like Java must use JNI to interface with
         the operating system)

usage:   rexxb4r.rex [-d] [-jh] [rexx|rexxj] rexx_pgm [arguments]

         ... "-d" debug, creates debug output

         ... "-jh" forces the use of Java according to the setting of the
             environment variable JAVA_HOME (using 'java' from its 'bin'
             subdirectory)

         ... optional "rexx" executable or generated "rexxj.cmd" or "rexxj.sh" (Unix)

         ... run program "rexx_pgm" and supplies it any supplied command
             line arguments; it will set BSF4Rexx_JavaStartupOptions to
             include "--enable-native-access=ALL-UNNAMED" starting with
             OpenJDK/Java >= 24, and removes it for OpenJDK/Java < 24

date:    2025-09-02, 2026-01-06

version: 1.1

license: Apache License 2.0 (see also at the end of this program)
*/

dirSep=.file~separator     -- / or \ depending on operating system
env        ="ENVIRONMENT"  -- operating system environment (vor value()-BIF)

java="java"    -- by default use any Java that is accessible

   -- note: take care of paths and arguments that have blanks in the supplied
   --       arguments (they need to be quoted), hence using .sysCargs
startIdx=1     -- default for index into .sysCargs for the program argument
parse arg switch pgm args      -- assume -jh, parse into program and into

if switch="" then    -- no arguments given, show usage/explanation
do
   .error~say(.resources~usage)
   exit -1
end

if .sysCargs~isNil then
do
   say "no arguments supplied via the command line, aborting ..."
   exit -2
end

.context~package~local~bDebug=.false
do i=1 to .sysCargs~items for 2
   if .sysCargs[i]~startsWith('-') then
   select case .sysCargs[i]~upper
      when "-D"  then
           do
              startIdx+=1
              .context~package~local~bDebug=.true
              .error~say(.line": using .File~searchPath() to locate 'java' executable:" pp(.file~searchPath("java" || (dirsep='\')~?(".exe","")) ))
           end
      when "-JH" then
           do
              startIdx+=1
              java_home=value("JAVA_HOME",,env)  -- fetch JAVA_HOME
              if java_home="" then
              do
                 say "switch ""-JH"" mandates explicitly the use of the environment variable JAVA_HOME which is not defined, aborting..."
                 exit -3
              end
              java=java_home || dirSep || "bin" || dirSep || "java"
              if .bDebug then .error~say(.line": JAVA_HOME="pp(java_home) "-> calculated java executable:" pp(java))
           end
      otherwise
           do
              if .sysCargs[i][1]="-" then
              do
                 say "switch #" i "(""".sysCargs[i]""") is not valid, aborting ..."
                 exit -4
              end
              startIdx=i
              leave
           end
   end
end



jVersion=getJavaVersion(java)    -- get Java main version number
if .bDebug then
   .error~say(.line": OpenJDK/Java version="pp(jVersion))

if jVersion~isNil then
do
   say "Cannot determine the Java version, is Java installed and available?"
   exit -5
end

enableNativeAccess="--enable-native-access=ALL-UNNAMED"
nameQ      ="BSF4Rexx_quiet"              -- if 1 no information about useing BJSO is given
nameQ_value=value(nameQ,,env)             -- get current value of environment variable
nameBJSO   ="BSF4Rexx_JavaStartupOptions" -- case dependent (it is for Java)!
nameBJSO_value=value(nameBJSO,,env)       -- get current value of environment variable
if .bDebug then
do
   .error~say(.line":" "--> BSF4Rexx_quiet             ="pp(nameQ_value))
   .error~say(.line":" "--> BSF4Rexx_JavaStartupOptions="pp(nameBJSO_value))
end

if jVersion>=24 then    -- starting with Java/OpenJDK 24 these stupid warnings get issued
do
   if nameQ_value="" then        -- set quiet: only change if BSF4_quiet is not currently set
      call value nameQ,.true,env -- do not show substitution value from BSF4Rexx_JavaStartupOptions

   if pos(enableNativeAccess,nameBJSO_value)=0 then      -- not yet set
      call value nameBJSO, strip(nameBJSO_value enableNativeAccess), env  -- set environment variable to new value
end
else     -- in case enableNativeAccess is set, we need to remove it for earlier versions of Java (inhibit errors)
do
   if pos(enableNativeAccess,nameBJSO_value)>0 then      -- not yet set
   do
      parse var nameBJSO_value before (enableNativeAccess) after
      call value nameBJSO, strip(before after), env   -- set environment variable to new value
   end
end

if .bDebug then
do
   .error~say(.line": OpenJDK/Java version="pp(jVersion) "<-- BSF4Rexx_quiet             ="pp(value(nameQ,   ,env)))
   .error~say(.line": OpenJDK/Java version="pp(jVersion) "<-- BSF4Rexx_JavaStartupOptions="pp(value(nameBJSO,,env)))
   address system "rexx -e ""call bsf.cls;say" .line"': using OpenJDK/Java from directory=['.java.lang.system~getProperty('java.home')']'"""
end

-- process command line information appropriately
cmd=.sysCargs[startIdx]          -- get command
if .sysCargs~items>startIdx then -- arguments supplied?
do
   res=processArgs(startIdx)     -- use .sysCargs to build program and argument strings
   cmd=(res[1]) res[2]           -- assemble command with its args
end

if .bDebug then
do
   .error~say(.line": command to execute:" pp(cmd))
   dt=.DateTime~new
end
address system cmd            -- execute command
if .bDebug then .error~say(.line": running command (program) lasted:" pp(dt~elapsed))


/* ========================================================================== */

::routine pp                  -- "pretty-print": enquote in square brackets
  return "["arg(1)"]"

/* ========================================================================== */

::routine getJavaVersion      -- get the Java version of the default java
   parse arg java

/* "java -version" may yield, e.g.

   E:\>java -version

   openjdk version "1.8.0_462"
   OpenJDK Runtime Environment (build 1.8.0_462-b11)
   OpenJDK 64-Bit Server VM (build 25.462-b11, mixed mode)

or maybe

   openjdk version "17.0.3.1" 2022-04-22 LTS
   OpenJDK Runtime Environment (build 17.0.3.1+2-LTS)
   OpenJDK 64-Bit Server VM (build 17.0.3.1+2-LTS, mixed mode, sharing)

or maybe

   openjdk version "24.0.2" 2025-07-15
   OpenJDK Runtime Environment (build 24.0.2+12)
   OpenJDK 64-Bit Server VM (build 24.0.2+12, mixed mode, sharing)

or maybe

   openjdk version "25" 2025-09-16 LTS
   OpenJDK Runtime Environment (build 25+37-LTS)
   OpenJDK 64-Bit Server VM (build 25+37-LTS, mixed mode, sharing)
*/

   v1=.nil                    -- default to .nil

   arr=.array~new             -- for fetchting output to 1 (stdout)
      -- command to get Java version, collect both, 2 (stderr) and 1 (stdout), via 1 (stdout)
   cmd=java "-version 2>&1"
   if .bDebug then dt=.DateTime~new
   address system cmd with output using (arr)
   if .bDebug then .error~say(.line": querying OpenJDK/Java version lasted:" pp(dt~elapsed))

      -- parse will trigger arr~makestring which creates a single string from all entries
   parse var arr 'version "' val '"'
   parse var val v1 "." v2 "."
   if v1=1 then               -- e.g. 1.8, then use major version 8
      v1=v2
   return v1


/* ========================================================================== */
::routine processArgs
  use arg pgmIdx=1         -- Index for program argument (can be 2 if "-jh" was given)

  pgm=.sysCargs[pgmIdx]    -- fetch program name
  if pgm~pos(" ")>0 then   -- program has a space, enquote it!
     pgm='"'pgm'"'

  args=""
  do i=pgmIdx+1 to .sysCargs~items  -- re-create arguments as a single string
     chunk=.sysCargs[i]
     if chunk~pos(" ")>0 then       -- argument has a space, enquote it!
        chunk='"'chunk'"'
     args=args chunk
  end
  return (strip(pgm), strip(args))


/* ========================================================================== */
::resource usage
purpose: allow programs to run without the wrong message that using BSF4ooRexx850
         would be dangerous and inhibit Java to force not using it in the future
         (BSF4ooRexx850 must use JNI like Java must use JNI)

usage:   rexxb4r.rex [-d] [-jh] [rexx|rexxj] rexx_pgm [arguments]

         ... "-d" debug, creates debug output

         ... "-jh" forces the use of Java according to the setting of the
             environment variable JAVA_HOME (using 'java' from its 'bin'
             subdirectory)

         ... optional: "rexx" executable or generated "rexxj.cmd" or "rexxj.sh" (Unix)

         ... run program "rexx_pgm" and supplies it any supplied command
             line arguments; it will set BSF4Rexx_JavaStartupOptions to
             include "--enable-native-access=ALL-UNNAMED" starting with
             OpenJDK/Java >= 24, and removes it for OpenJDK/Java < 24
::END


/*
      ------------------------ Apache Version 2.0 license -------------------------
         Copyright 2025-2026 Rony G. Flatscher

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

