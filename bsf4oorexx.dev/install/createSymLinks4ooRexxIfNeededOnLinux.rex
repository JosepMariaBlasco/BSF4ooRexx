#!/usr/bin/env rexx
/*
   author:  Rony G. Flatscher
   date:    2017-02-05
   version: 1.0
   name:    createSymLinks4ooRexxIfNeededOnLinux.rex
   purpose: on Linux it may be the case that beta versions of ooRexx may miss
            symbolic links to earlier major version numbers for some ooRexx shared
            libraries; this utilitiy will print to stdout all symbolic link
            statements necessary to add symbolic links to missing major library
            versions; just copy and paste the output to create the links; if all
            symbolic links are in place there will be no link commands generated

   usage:   rexx createSymLinks4ooRexxIfNeededOnLinux.rex

   needs:   ---

   changed: ---

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2017 Rony G. Flatscher

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


parse upper source opsys +1
if opsys<>"L" then
do
   say "# this is a utilitiy for Linux only, aborting..."
   exit -2
end

-- maxLibVersionNr=5    -- ooRexx 5.0beta
parse version "_" maxLibVersionNr "."  -- e.g. ooRexx 5.x would return "5"

   -- (base name, since version == minimum_version)
libs=.array~of(         -
         .array~of("librexx.so"     , 2)       , -
         .array~of("librexxapi.so"  , 2)       , -
         .array~of("librexxutil.so" , 2)       , -
         .array~of("librxmath.so"   , 4)       , -
         .array~of("librxregexp.so" , 2)       , -
         .array~of("librxsock.so"   , 2)       , -
         .array~of("librxunixsys.so", 4)       , -
              )

   -- find "librexx.so"
call sysFileTree "/usr/librexx.so", "file.", "FOS"
if file.0=0 then
do
   say "# 'librexx.so' not found, aborting..."
   exit -1
end

do i=1 to file.0
   say "# i="i"/"file.0 "- found:" pp(file.i)   -- use a comment lead-in such that copy and paste will work with this output as well
   tgtLibDir=filespec("Location", file.i)       -- "Location" returns full path with the trailing slash

   cmd="sudo ln -sfv"                     -- symbolic (force, verbose) link command
   do lib over libs
      baseName=tgtLibDir || lib[1]        -- fully qualified name of base library name
      if sysFileExists(baseName) then     -- base library exists
      do i=lib[2] to maxLibVersionNr      -- not even assuming that max version number exists
          newLink=tgtLibDir || lib[1]"."i -- fully qualified name of needed symbolic link
          if sysFileExists(newLink) then iterate   -- link exists already
          say cmd baseName newLink        -- show command to create the needed symbolic link for copy and paste
      end
   end
   say
end

::routine pp
  return "["arg(1)"]"