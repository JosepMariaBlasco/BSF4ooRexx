#!/usr/bin/env rexx
/*
   Name:    kickoff.rex
   Author:  Rony G. Flatscher
   Date:    2010-08-30

   Usage:   rexx kickoff install|reinstall|uninstall
            ... will try to run "..\setupAllAndRun.rex" elevated, if this causes
                an error, just runs it without; this way starting with Windows2000/XP
                elevation is attempted, the fallback is to run re/un/installation as is

   Version: 1.02
   Changes: - 2014-04-26 rgf: use "elevate{32|64}.exe" on Windows Vista and higher
                         to run the [re|un]install elevated

            - 2014-12-12 rgf: use fully qualified path to setupAllAndRun.rex as elevation
                         may change the current directory to the Windows home directory

            - 2015-08-14, rgf: replace the external elevation package with a scripting
                         solution that has become possible again, thanks to Gil Barmwater

            - 2015-08-20, rgf: do not show wait-countdown on uninstall

            - 2015-08-24, gab: for uninstall, add commands to delete the directory tree
                          after this script ends as the script cannot do it; remove
                          wait option as it is not needed

   Info-URLs:  <http://technet.microsoft.com/en-us/magazine/2007.06.utilityspotlight.aspx>
               <http://technet.microsoft.com/en-us/magazine/2008.06.elevation.aspx>
               <http://forums.techarena.in/vista-administration/977482.htm>
               <http://blogs.technet.com/b/heyscriptingguy/archive/2005/10/31/how-can-i-start-an-application-from-an-hta.aspx>

               ad elevate-tool, cf: <http://jpassing.com/2007/12/08/launch-elevated-processes-from-the-command-line/>,
               licensed under the MIT-license (see file "license_mit.txt" or
               <http://opensource.org/licenses/mit-license.php>)


   Last svn check-in: $Revision: 912 $ $Author: Administrator $ $Date: 2010-08-23 16:49:55 +0200 (Mon, 23 Aug 2010) $

   License:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2014-2015 Rony G. Flatscher

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

parse source . . s

parse arg args .
if args="" | wordPos(args~upper, "INSTALL REINSTALL UNINSTALL")=0 then
do
   parse source . . s
   .error~say(s": no argument or unknown argument (one of 'install', 'reinstall', or 'uninstall') given, aborting ...")
   exit -1
end

-- build argument to be passed to Rexx
if args~upper="UNINSTALL" then     -- add commands to delete directory tree
   args ="..\setupAllAndRun.rex" args "&& cd ..\..\.. && rd /s /q bsf4oorexx"
else
   args ="..\setupAllAndRun.rex" args
call elevate.rex args
