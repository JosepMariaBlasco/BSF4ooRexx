#!/usr/bin/env rexx
call sysFileTree "*.rxo", "files.", "FO"
do i=1 to files.0
   name=filespec("Name", files.i)
   say '    <dt><a href="'name'">'name'</a>'
   say '    <dd>'
   say
end

