#!/usr/bin/env rexx
/* UNPUBLISHED APIs, changed with OOo 2.2 and yet has changed again !
*/

#!/usr/bin/rexx
/*
   author:  Rony G. Flatscher
   date:    2006-06-25
   version: 1.0.3
   name:    add_ooRexxPackage.rex
   purpose: adds package "ScriptProviderForooRexx.jar" for all OOo users ("shared" context)
   needs:   UNO.CLS

   changed: 2006-08-09, ---rgf: changed logic to create a new connection each time
                                the package manager is used (turns out that on Linux
                                FC5 the UNO bridge breaks on OOo 2.0.2, if a package
                                manager exception occurs, inhibiting successive actions
                                to be successful)

            2007-07-07, ---rgf: unfortunately, OOo 2.2.x all of a sudden mandates an
                                XCommandEnvironment argument; hence addressing the
                                environment to issue command-line "unopkg" commands
                                instead; leaving the original code as an example of how
                                to use UNO for future releases

            2010-03-20, ---rgf: make sure that no removing messages or remove errors get displayed

   remark:  one can add or remove a package via the command-line as well using the
            executable "unopkg[.exe]" from the OOo "program" directory

               unopkg list --shared
                  ... lists all deployed "shared" (=global) packages

               unopkg add -v --shared ScriptProviderForooRexx.jar
                  ... adds the ooRexx script (macro) provider support package globally

               unopkg remove -v --shared ScriptProviderForooRexx.jar
                  ... removes the ooRexx script (macro) provider support package globally

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2006-2010 Rony G. Flatscher

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

/* rgf, 2010-03-20
parse upper arg switch +1     -- argument "REMOVE" removes, otherwise install

if sysVersion()~left(1)="W" then -- Windows?
   nullDevice="2>&1 >nul"
else                             -- Unix
   nullDevice="2>&1 >/dev/null"

-- this loop makes sure that the precursing "ScriptProviderForRexx.jar" is removed as well!

do package over .list~of("ScriptProviderForRexx.jar", "ScriptProviderForooRexx.jar")
   -- package="ScriptProviderForooRexx.jar"     -- package name
   res=doit("unopkg remove" package nullDevice) -- make sure (older?) deinstalled from user
   if res=1 then say "Package" pp(package) "successfully removed from 'user' context"

   res=doit("unopkg remove --shared" package nullDevice) -- make sure (older?) deinstalled from user
   if res=1  then say "Package" pp(package) "successfully removed from 'shared' context"
end

if switch<>"R" then           -- if not Remove switch given, install the package
do
   res=doit("unopkg add    --shared" package)   -- now add as a shared package
   if res=1  then say "Package" pp(package) "successfully added as a 'shared' package!"
             else say "ERROR! Package" pp(package) "could *NOT* be added as a 'shared' package!"
end


::routine pp      -- enclose argument in square brackets
  parse arg s     -- get argument
  return "["s"]"  -- return argument enclosed in square brackets

::routine doit    -- try to carry out action
  parse arg command
  signal on any   -- intercept all conditions
  command         -- pass command to environment
  return rc=0     -- return .true, if return code from command was 0 (usually: success)

any:              -- something went wrong
  return .false

-- */

-- /* ---------------------------------> OLD CODE (up to: 2007-07-07) - BEGIN -->>

/*
   rexx add_ooRexxPackage > {/dev/null|nul}

   LInux:   "/dev/null"
   Windows: "nul"

*/

bInstall=(arg()=0)      -- install, if no argument given, otherwise de-install only

packageName = "ScriptProviderForooRexx.jar"
mediaType   = "application/vnd.sun.star.uno-component;type=Java"

tmpName     = stream(packageName, "C", "QUERY EXISTS")   -- get fully qualified filename
if tmpName  = "" then
do
   .error~say("Cannot find" pp(packageName)", aborting...")
   exit -1
end
urlName     = ConvertToUrl(tmpName) -- turn into ucp compliant URL

xpmf=getNewConnectionToPackageManagerFactory()

ro=.foolPackageManager~new    /* create an instance of this ooRexx class      */
   /* create a JavaProxy representing the needed interfaces, delegating the method
      invocations to the ooRexx object */
slotDir=.directory~new
xCommandEnvironment=BSFCreateRexxProxy(ro, slotDir, "com.sun.star.ucb.XCommandEnvironment", -
                                                    "com.sun.star.task.XInteractionHandler", -
                                                    "com.sun.star.ucb.XProgressHandler")
slotDir~mighty=xCommandEnvironment

/*
list=.list~of("com.sun.star.ucb.XCommandEnvironment", -
           "com.sun.star.task.XInteractionRequest", -
           "com.sun.star.ucb.XProgressHandler")
prclz=XCommandEnvironment~getClass
do li over list
   clz=bsf.loadClass(li)
   say "   " pp(li)": isProxyClass()?" pp(prclz~isProxyClass(clz)) pp(clz)
end

clzs=xCommandEnvironment~getClass~getClasses
say "clzs      ="pp(clzs)
say "clzs~items="pp(clzs~items)


i=1
do clz over clzs -- xCommandEnvironment~getClass~getClasses
   say i"." pp(clz~toSTring)
   i+=1
end
*/

say "xCommandEnvironment~toString:" pp(xCommandEnvironment~toString)

   -- make sure package is removed from "user" context
call removePackage xpmf~getPackageManager("user"), packageName, xCommandEnvironment

   -- work on "shared" (global) package context
xpmShared=getNewConnectionToPackageManagerFactory()~getPackageManager("shared")
call removePackage xpmShared, packageName, xCommandEnvironment             -- remove old package

if bInstall then  -- install package ?
do
   xpmShared=getNewConnectionToPackageManagerFactory()~getPackageManager("shared")
   -- xpmShared~addPackage(urlName, mediaType, .nil, .nil)  -- add package
say "urlName  ="pp(urlName)
say "mediaType="pp(mediaType)
   xpmShared~addPackage(urlName, mediaType, .nil, xCommandEnvironment)  -- add package

   dp=getDeployedPackage(xpmShared, packageName)         -- query package
   if .nil<>dp then  -- package object available?
      say "Is ["packageName"] registered?" "["dp~isRegistered(.nil,.nil)~value~value"]"
   else
   do
      say "["packageName"] could not be added!"
      say
      say "You may want to try: 'unopkg remove/add --shared ScriptProviderForooRexx.jar'"
      say "from the command line. 'unopkg' is located in the 'program' directory of"
      say "OpenOffice.org/StarOffice."
   end
end



::requires UNO.CLS    -- get UNO support for ooRexx

::routine getNewConnectionToPackageManagerFactory  -- rgf, 2006-08-09
      /* initialize connection to server, get XContext */
   xContext    = UNO.connect()  -- connect to server and retrieve the XContext object
   XMcf        = xContext~getServiceManager   -- retrieve XMultiComponentFactory

      /* get the pre-registered package manager factory singleton object   */
   pmName="/singletons/com.sun.star.deployment.thePackageManagerFactory"
   xpmf=xContext~getValueByName(pmName)~XPackageManagerFactory

   return xpmf



::routine removePackage       -- remove a given package
  use arg xpmf, packageName, xCommandEnvironment

  signal on any      -- activate execption handling
  -- xpmf~removePackage(packageName, .nil, .nil)
  -- xpmf~removePackage(packageName, packageName, .nil, xCommandEnvironment)

  pkg=getDeployedPackage(xpmf, packageName, xCommandEnvironment)
say "---> pkg="pp(pkg)
say "     pkg~getIdentifier="pkg~getIdentifier "pkg~getName="pp(pkg~getName)

  xpmf~removePackage(.nil, packageName, .nil, xCommandEnvironment)

  any: return        -- just return

::routine getDeployedPackage  -- get and return the deployed package object
  use arg xpm, packageName

  signal on any      -- activate exception handling
  return xpm~getDeployedPackage(packageName, .nil, xCommandEnvironment)

  any: return .nil   -- exception occurred return .nil



/* This is an ooRexx class which is used to serve the XCommandEnvironment, the XInteractionHandler
   and the XPorgressHandler methods. Only the XCommandEnvironment methods are implemented, the others
   are just intercepted with the unknown-method and then ignored.
*/
::class foolPackageManager
::method unknown        /* this method will get invoked, if sending an unknwon message */

::method getInteractionHandler   /* from the XCommandEnvironment interface, return self   */
say "in getInteractionHandler()..."
say arg(arg())~userData~mighty
   return arg(arg())~userData~mighty
   -- return self

::method getProgressHandler      /* from the XCommandEnvironment interface, return self   */
say "in getProgressHandler()..."
say arg(arg())~userData~mighty
   return arg(arg())~userData~mighty


-- <<-------------------------------> OLD CODE (up to: 2007-07-07) - END ---- */