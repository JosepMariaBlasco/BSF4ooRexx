#!/usr/bin/env rexx
/*
   author:  Rony G. Flatscher
   date:    2010-08-23
   version: 1.04
   name:    setupJava.rex
   purpose: BSF4ooRexx must be able to dynamically load 'libjvm.so';
            Linux systems may have plenty of alternative Javas installed and
            therefore 'libjvm.so' seems not to be linked into the usual lib
            directories;
            for the purpose of BSF4ooRexx the current active Java's 'libjvm.so'
            gets linked to '/opt/BSF4ooRexx';
            this utility also allows the user to change the default Java to use,
            which also causes its 'libjvm.so' to be linked int '/opt/BSF4ooRexx'
            instead

   usage:   rexx setupJava.rex

   needs:   ---

   changed:
            2011-01-03, ---rgf, - fixed bug in parsing the output of a "ls -al", thanks to Uli Zinngrebe for reporting on 2010-12-27!
            2011-03-27, ---rgf, - link libjvm.so only symbollically to /opt/BSF4ooRexx in
                                  order for Java to find its home
            2012-02-25, ---rgf, - Unix: remove "-n" flag from "ldconfig"
                                - Unix: removed target lib-determination, not needed here
            2020-08-17, ---rgf, - tidying up a little bit

   remark:  supercedes the previously available version (total rewrite)

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2010-2021 Rony G. Flatscher

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

bDebug=.false              -- debug flag

parse source . . s         -- get fully qualified path to this program
thisName=filespec("Name",s)

ver=SysVersion()           -- get operating system version
if bDebug=.true then say pp("ver", ver)
if ver~left(1)~upper="W" then
do
   say thisName": program needs Unix, running on ["ver"] instead, aborting..."
   if bDebug<>.true then
      exit -1
end

path2JavaInfoClass=getPath2JavaInfoClass()
if bDebug=.true then say  pp("path2JavaInfoClass", path2JavaInfoClass)
if path2JavaInfoClass=.nil then
do
   say thisName": cannot find [JavaInfo4BSF4RexxInstallation.class] in current or parent directory, aborting..."
   exit -10
end

   -- set Java to use
res=update_alternatives()
if bDebug=.true then say  pp("res", res)
if res=.false then
do
    say thisName": error occurred running 'update-alternatives --config java', aborting..."
    if bDebug<>.true then
       exit -20
end

javaHome=getJavaHome(path2JavaInfoClass)
if bDebug=.true then say  pp("javaHome", javaHome)
if javaHome=.nil then
do
   say thisName": there seems to be no Java installed/available on this system, aborting..."
   exit -30
end

   /* try to find 'libjvm.so' */
call sysFileTree javaHome"/libjvm.so", "libFile.", "FOS"
bitness=getRexxBitness()   -- can be .nil if not determinable, else "32" or "64"

if bDebug=.true then say pp("bitness", bitness)
if bDebug=.true then say pp("# 'libjvm.so' found", libFile.0)
if libFile.0=0 then
do
    say thisName": no Java library [libjvm.so] found in subdirectories from ["javaHome"], aborting..."
    exit -40
end

if libFile.0=1 then     -- easy, we just found one 'libjvm.so' only, so we need to use this one
   path2lib=libFile.1
else                    -- find the 'libjvm.so' that
do
      -- link to found libjvm.so in the following order: client, server, any other version
   do chunk over .array~of("/client/libjvm.so", "/server/libjvm.so", "/libjvm.so")
      clients=.array~new
      do i=1 to libFile.0

         if libFile.i~pos(chunk)>0 then
         do
             if bitness=32, libFile.i~pos("/i386/")>0 then        -- oops, a 64-bit Intel lib
                clients~append(libFile.i)
             else if bitness=64, libFile.i~pos("/amd64/")>0 then  -- oops, a 32-bit Intel lib
                clients~append(libFile.i)
             else if bitness=.nil then       -- no bitness available, accept any bitness
                clients~append(libFile.i)
         end
      end

      if clients~items>0 then    -- o.k. found, take first one
      do
          path2lib=clients[1]
          leave
      end
   end

   if clients~items=0 then
   do
       say thisName": no suitable Java library [libjvm.so] found, aborting..."
       exit -41
   end
end

if bDebug=.true then say  pp("path2lib", path2lib)
fn=stream(path2lib, "C", "query exists")
if fn="" then
do
    say thisName": Java library ["path2lib"] not found, aborting..."
    exit -41
end

cmd="sudo ln -fsv" path2lib "/opt/BSF4ooRexx/libjvm.so"
say thisName": linking ["cmd"]..."
cmd

-- cmd="sudo ldconfig -n /opt/BSF4ooRexx"    --
cmd="sudo ldconfig /opt/BSF4ooRexx"    --
say thisName": updating cache to shared libraries ["cmd"]..."
cmd
say thisName": done."
say
say thisName": Please hit enter to continue"
parse pull answer
exit 0

::routine update_alternatives    -- only returns .true, if no syntax or failure condition occurred
  signal on any
  cmd="sudo update-alternatives --config java"
  cmd                      -- send cmd to environment
  return .true             -- no error occurred

any:
  return .false            -- error occurred


::routine getPath2JavaInfoClass  -- return path
  fn=stream("JavaInfo4BSF4RexxInstallation.class", "C", "Query Exists")
  if fn<>"" then return "."      -- return current directory

  fn=stream("../JavaInfo4BSF4RexxInstallation.class", "C", "Query Exists")
  if fn="" then return .nil      -- indicate not found
  return ".."                    -- return parent directory


::routine getJavaHome            -- return currently activated Java home-directory, if any set
  signal on syntax
  use strict arg path2JavaInfoClass

  do while queued()>0            -- make sure we empty the system queue, if there's still something in it
     parse pull .                -- pull element from queue
  end
                  -- queue output to standard Rexx queu
  "java -cp" path2JavaInfoClass "JavaInfo4BSF4RexxInstallation | rxqueue"
  javaHome=.nil
  do while queued()>0
     parse pull pName "=[" pVal "]" -- parse line, get property name and property value
     if pName="java.home" then   -- property "java.home" found !
     do
        javaHome=pVal
        leave
     end
  end

  do while queued()>0            -- make sure we empty the system queue, if there's still something in it
     parse pull .                -- pull element from queue
  end
  return javaHome

syntax:
  return .nil

::routine pp
  parse arg description, val
  return description~left(25,'.') "["val"]"

-- return bitness of Rexx interpreter (e.g. 32, 64, etc.)
::routine getRexxBitness

  -- do not use ADDRESS CMD as on Linux this causes an error :(
   "rexx -v 2>&1 | rxqueue"
   bitness=.nil                  -- default value
   do while queued()>0
      parse pull hint ":" val .
      if hint="Addressing Mode" then bitness=val
   end
   return bitness

::routine qc                        -- quote for Unix
  return "'"  || arg(1) || "'"    /* Unix     */
