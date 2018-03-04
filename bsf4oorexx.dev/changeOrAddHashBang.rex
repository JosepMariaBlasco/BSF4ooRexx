-- rgf, 2018-03-04: change hash-bang line in all Rexx scripts

rexxExtensions=("rxo", "rxj", "rex", "cls", "testGroup", "testUnit", "jrexx", "frm")

do ext over rexxExtensions
  say "looking for Rexx scripts with extension:" pp(ext)
  call sysFileTree "*."ext, "files.", "FSO"
  say "  " files.0 "file(s) found"
  .output~charout("   ")
  do i=1 to files.0
     .output~charout(".")
     call process files.i
  end
  say
end

::routine pp
  return "["arg(1)"]"

::routine process
  parse arg fn

  newHashBang="#!/usr/bin/env rexx"
  stream=.stream~new(fn)~~open("read")
  lines=stream~arrayIn  -- get content as string array
  stream~close

  if lines[1]~abbrev("#!") then lines[1]=newHashBang
                           else lines~insert(newHashBang,.nil)

  stream=.stream~new(fn)~~open("write replace")
  stream~arrayout(lines)
  stream~close
  return
