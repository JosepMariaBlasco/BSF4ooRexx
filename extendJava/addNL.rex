level=0
indent="   "
parse arg inFile
do while chars(inFile)>0
   line=linein(inFile)
   if line="}" then level=-1

   if level>0, pos("mv.visitLabel(",line)=0 then
      .output~say(indent~copies(level) || line)
   else
      .output~say(line)

   if line="}" then
      .output~say

   if line="{" then level=+1
end
call stream inFile, "C", "close"
