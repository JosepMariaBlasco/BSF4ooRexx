#!/usr/bin/env rexx
/* ---rgf, 2003-04-07, idea from OS/2 where I did use the WPS for the same purpose ! */

windir=value("windir",,"ENVIRONMENT")  /* get windows directory   */
explorer="explorer.exe"

   /* get full path name as explorer.exe may not be reachable via PATH alone */
tmp=stream(windir"\"explorer, "c", "QUERY EXISTS")
if tmp="" then tmp=stream(windir"\system32\"explorer, "c", "QUERY EXISTS")
if tmp="" then
do
   call beep 500,500
   say "Could not find ["explorer"] in ["windir"], aborting..."
   exit -1
end
"@"tmp "/n,/e," || '"'||directory()|| '"'
