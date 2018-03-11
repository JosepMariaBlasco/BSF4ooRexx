#!/usr/bin/env rexx
-- rgf, 2018-03-08: change hash-bang line in all Rexx scripts recursively
--                  will use LF for newlines to allow Unix to process the hashbang line

parse arg path             -- where to start?
if path="" then path="."   -- default to current directory
tmpPath=qualify(path)      -- turn into a fully qualified path
if \sysIsFileDirectory(path) then   -- check if it is a directory
do
   say pp(path) "is not a directory, aborting ..."
   exit -1
end
   -- determine which file delimiter to sue
parse source op_sys +1     -- get first letter
if op_sys="W" then slash="\"
              else slash="/"

   -- Rexx extensions to process
rexxExtensions=("rxo", "rxj", "rex", "cls", "testGroup", "testUnit", "jrexx", "frm")

do ext over rexxExtensions    -- iterate over the extensions
  filespec=tmpPath || slash || "*."ext -- create search pattern
  say "looking for Rexx scripts starting in:" pp(filespec)
  call sysFileTree filespec, "files.", "FSO"
  say "  " files.0 "file(s) found"
  .output~charout("   ")      -- give user feedback
  do i=1 to files.0
     .output~charout(".")
     call process files.i
  end
  say
end

::routine pp            -- enquote string in square brackets
  return "["arg(1)"]"

::routine process       -- replaces or inserts hashbang line
  parse arg fn

  signal on syntax
  newHashBang="#!/usr/bin/env rexx"
  stream=.stream~new(fn)~~open("read")
  lines=stream~arrayIn  -- get content as string array
  stream~close

  if lines[1]~abbrev("#!") then lines[1]=newHashBang
                           else lines~insert(newHashBang,.nil)

      -- replace old file
  stream=.stream~new(fn)~~open("write replace")
  str=lines~toString( , "0A"x)   -- turn into a string, use LF only (otherwise hashbang line does not work on Unix)
  stream~charout(str)
  stream~close
  return

syntax:
   co=condition("object")
   say
   say "syntax conditionraised!" .context~name": file" pp(fn)
   say "  position :" pp(co~position) "->" pp(sourceline(co~position))
   say "  message  :" pp(co~message)
   say "  skipping..."
   return