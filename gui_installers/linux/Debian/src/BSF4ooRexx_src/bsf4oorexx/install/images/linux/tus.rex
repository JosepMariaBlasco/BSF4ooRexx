call sysFileTree "*.png", "files.", "FOS"
say "found" files.0 "files"
do i=1 to files.0
   loc =filespec("loc", files.i)
   name=filespec("name", files.i)
   parse var name iconName "_" size "." type
   --say files.i
   size=size+0 -- get rid of leading 0
   sizeDir=size"x"size
   say sysFileExists(loc)":" loc name "|" sizeDir"\"iconName"."type

   if sysFileExists(loc||sizeDir)=.false then
   do
      say "---"
      call SysMkDir loc || sizeDir
   end
   cmd="copy" files.i loc||sizeDir"\"iconName"."type
   cmd
end
