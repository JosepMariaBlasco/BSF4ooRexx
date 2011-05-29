#!/usr/bin/rexx
/* <a href="http://www.ooRexx.org>Object Rexx</a> convenience class for making IBM's and
   Apache's Bean Scripting Framework (BSF) available in an OO-style as well: Java classes and
   Java objects appear as Object Rexx classes and objects and one can send Object Rexx
   messages to them.

   Object Rexx programmers should appreciate this little class ...
   ;)

   last change: $Revision: 927 $ $Author: Administrator $ $Date: 2010-12-26 18:34:19 +0100 (Sun, 26 Dec 2010) $

   needs: ooRexx 4.0.1 (cf. <http://www.ooRexx.org>) and BSF4ooRexx 4.0 or greater

   date:
         2011-02-21, rgf: - define new entries in .bsf4rexx
                             - "OPSYS" ... operating system name from "PARSE SOURCE opsys ." plus
                               "OPSYS1", "OPSYS2", "OPSYS3" with the first one, two, three letters in uppercase
                             - MacOSX: "APPLE_APPLICATION" the Apple application object representing the running Rexx program
                             - "BSF4OOREXX_{016|032|048|064|128|256}": if defined path to PNG-icon
                             - "OOREXX4OOO_{016|032|048|064|128|256}": if defined path to PNG-icon

                          - MacOSX: remove About and Preferences Apple menu entries
                          - MacOSX: define Dock icon
         2011-02-20, rgf: - adding MacOSX related properties
         2010-07-12, rgf: - .bsf.dialog::inputBox: now accepting a Java Object array for options
         2010-06-10, rgf: - changed environment variable from 'BSF4Rexx.JavaStartupOptions' to
                            'BSF4Rexx_JavaStartupOptions'; reason: bash cannot process
                            environment variables that contain a dot

         2010-06-08, rgf: - corrected BsfLoadJava to supply Java startup arguments, if given
                            in the environment variable named 'BSF4Rexx.JavaStartupOptions'

         2010-06-05, rgf: - added check for minimum version of ooRexx being "4.0.1"

         2010-05-11, rgf: - added routine bsf.getSlotDir() which returns the last (slotDir) item

         2010-05-08, rgf: - BSF::UNKNOWN::CLASS - little optimization: forwarding directly to bsf.jco
                          - create[Java]Array[Of] - forward all arguments verbatimely (let Java side optimize)
                          - remove usage of bsf.getJavaClassObjectProxy(...)
                          - removed method bsf.class, instead send "getClass" to Java object
                          - removed class method bsf.getJavaClassObjectProxy
                          - removed class attribute bsf.JavaClassObjectProxies
                          - removed class attribute bsf.JavaClassObjects
                          - added public routine "bsf.importClass" (matching "bsf.loadClass") to
                            supercede "bsf.import"
                          - added class method "bsf.importClass" to class "BSF" (matching
                            "bsf.loadClass") to supercede "bsf.import"

         2010-04-18, rgf: - added public routine bsf.createProxyClass(), adapted .BSF~bsf.import
                            accordingly

         2010-02-20, rgf: - added public routine bsf.isA(jobject, jclass)

         2009-10-19, rgf: - adjust for renaming the library to "BSF4ooRexx"

         2009-10-04, rgf: - added special handling for RexxProxy objects

         2009-09-10, rgf: - added class methods "bsf.createJavaArray" and "bsf.createJavaArrayOf" to
                            class "BSF" (synonyms for "bsf.createArray" and "bsf.createArrayOf")
                          - added public routines "bsf.createJavaArray" and "bsf.createJavaArrayOf"
                            (synonyms for "bsf.createArray" and "bsf.createArrayOf")

         2009-07-30, rgf: - class "BSF": created class method "bsf.createArrayOf"
                          - created public routine "bsf.createArrayOf"

         2009-06-27, rgf: - now raises syntax errors in case too many arguments are supplied for the
                            (class) methods or public routines:

                             - bsf.postEventText
                             - bsf.pollEventText
                             - bsf.import
                             - bsf.getFieldValueStrict
                             - bsf.setRexxNullString
                             - bsf.sleep

         2009-06-25, rgf: - added method "size" to class BSF_ARRAY_REFERENCE

         2009-06-21, rgf: - default BSfShowErrorMessage() to .false
                            (turn off displaying the Java exception message (leftover from the
                            very early BSF4Rexx days))

         2009-06-08, rgf: - removed method 'bsf.addEventListenerReturningEventInfos'
                          - removed routine 'bsf.getEventInfoObject'
                          - removed private class "bsf_array_proxy.assure_BSFRegistry_Removal"

         2009-05-15: - add bsf.wrap as a class method to .BSF; this way it can be assured to be
                       reachable on a callback from the 4.0 API, if .bsf is placed in .local

         2009-04-23: - started to create an ooRexx 4.x specific version of this library,
                       in order to allow for allowing Java proxies to callback into ooRexx
                       by addressing matching Rexx proxy objects

         targeted version number: 400
                    ///////////////////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
                    \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ ////////////////////////////////////////////////

          2009-04-04, rgf: - added public routine "bsf.haltThread(tid)" and class
                             method for .BSF "bsf.haltThread(tid)"

          2009-02-10, rgf: - bsf.wrap() now caters for unprefixed argument strings that
                             stem from RexxEngine by not raising an exception anymore,
                             but returning the received string unaltered

          2008-09-11, rgf: - boxing a Boolean: only accept the Rexx values:
                             .true, .false, "true" or "false"

          2008-09-09, rgf: - changed "bsfRegister[Strict]" by "new[Strict]" such that
                             error messages can be understood easier (it is self-evident
                             what "new[Strict]" means)

          2008-08-31, rgf: - removed "dir" class attribute from class "BSF"
                           - new "BSF4REXX" public class now behaves as a directory by
                             forwarding all unknown messages to its directory class
                             attribute; created "B4R" public class which acts as a
                             short-hand alias forwarding its unknown messages to
                             the "BSF4REXX" class

          2008-08-28, rgf: - moved ".bsf4rexx" directory object to the class attribute
                             "dir" of class ".BSF" and have ".bsf4rexx" and ".b4r"
                             just point to it; the code in here will be changed to use
                             only the BSF class attribute such that multiple interpreter
                             instances running in the same process do not influence that
                             content unadvertently (.local is shared in ooRexx < 4.0!)
                           - added "line.separator", "file.separator", "path.separator"
                             to .bsf~dir

          2008-07-20, rgf: - added dimensionality check to AT(), PUT(), and PUTSTRICT() in class
                             BSF_ARRAY_REFERENCE to correct possible misuse and as such increase
                             error checking and error reporting

          2008-07-09, rgf: - added public routine "bsf.version()" as pass-thru to BSF()-subfunction
                             "version"

          2008-06-23, rgf: - correcting further bugs related to the big change of yesterday

          2008-06-22, rgf: - found and removed a bug (changing RexxAndJava.java as well) that
                             mixed up strings and beans

          2008-06-16/17, rgf: - activating condition trapping in order to save BSF_ERROR_MESSAGE in the
                             .local directory environment such that it can be retrieved under the
                             environment symbol ".BSF_ERROR_MESSAGE"; before invoking any BSF()-function
                             that entry will be removed from .local

          2007-08-06, rgf: - renamed "BSF" class method "attachNewStrict" to "bsf.attachNewStrict"

          2007-05-23, rgf: - added class method getMessageType to BSF.DIALOG to allow correct
                             processing of supplying first character only

          2007-01-09, rgf: - added ability to supply Java startup options via the operating system
                             environment variable 'BSF4Rexx.JavaStartupOptions'; this allows one
                             to set e.g. the Java heap size etc.; either set the value in the
                             shell or a Rexx program that executes before 'BSF.CLS' (e.g. by
                             requiring such a Rexx program *before* requiring 'BSF.CLS'); the
                             public routine "parseJavaOptions(stringOfJavaStartupOptions)" returns
                             an array of parsed options, "createJavaLoadStatement(optionArray)"
                             creates the BsfLoadJava-statement supplying each option as an own
                             invocation argument (ooRexx 3.x cannot yet handle array arguments)

          2006-12-01, rgf: - added access to pre-registered java.lang.Thread class

          2006-11-29, rgf: - corrected a bug in method bsf.import, now class object is saved
                             in the private class attribute bsf.jco

          2006-10-17, rgf: - renamed method "dispatch" to "bsf.dispatch" to avoid name-clashes
                             with Java methods named "DISPATCH"

          2006-08-01, rgf: - BSF.WRAP() adjusted to process beanNames that have two
                             hexadecimal values after '@', delimited by '_': this has
                             been added to 'RexxAndJava.java' to handle constructors
                             for which Sun's Java prodcues identical (!) hashCode() values!

          2006-07-22, rgf: - cleaned program of commented code

          2006-07-11, rgf: - added method DISPATCH (as in OLEObject starting with ooRexx 3.1),
                             will be forwarded to method BSF.INVOKE

          2006-07-04, rgf: - added method "ENTRY" to "JavaStaticFields.Directory"

          2006-03-22, rgf: - added methods "[]", "AT", "[]=", "PUT" and making directory cache for retrieval
                             optionally to "JavaStaticFields.Directory"

          2006-03-21, rgf: - "JavaStaticFields.Directory:unknown": made sure that cached value is returned,
                             instead of looking up the Java side constantly

          2006-03-20, rgf: - removed a bug from "JavaStaticFields.Directory" reported by Lee Peedin
                             (Java class name was stored in an ooRexx class attribute instead of an
                             instance attribute); changed logic a bit, now that the Java calss name is
                             handled by an instance attribute (no need for dynamically setting the
                             unknown method)

          2006-03-08, rgf: - .bsf~bsf.import(): now allows storing references to already created
                             ooRexx proxy class objects under different names in .local

          2006-03-03, rgf: - fixed BSF's class method and also the public routine named "bsf.postEventText"
                             to use the optional priority value (0=low, 1=normal=default, 2=high)

          2006-03-02, rgf: - added routine "bsf.import(...)", a wrapper for ".bsf~bsf.import(...)"

          2006-02-24, rgf: - added method 'bsf.addEventListenerReturningEventInfos', routine
                             bsf.getEventInfoObject, private class "bsf_array_proxy.assure_BSFRegistry_Removal"
                             which makes sure that the eventObject gets removed from the BSFRegistry by
                             repeating "unregisterBean" until the Java object is removed from the registry

                           - added routine "bsf.getEventInfoObject" which must be called exactly once on
                             each eventText returned because of employing
                             'bsf.addEventListenerReturningEventInfos'; this will create a BSF proxy object
                             and takes care that the eventObject gets deregistered from the BSF registry

          2006-02-05, rgf: - allowing assignments to static Fields if wrapped with 'bsf.wrapStaticFields'

          2006-02-02, rgf: - added routine 'bsf.wrapStaticFields' which returns an instance
                             of "JavaStaticFields.Directory", which allows easy access to the
                             static fields of the indicated Java class/interface by merely
                             sending the names of the static fields or using the AT method
                             to access them

          2006-01-06, rgf: - now fixed bsf.import to use the routine bsf.loadClass(), changed the
                             code to use the new class method bsf.setBSF4RexxInfos to store the
                             Java class object proxy and save the fully qualified Java class name;
                             added public routines pp() and iif()

          2006-01-05, rgf: - now using the much better BSF-subfunction "loadClass" instead of the
                             bad variant "Class~forName()", added class method "bsf.loadClass" to "BSF",
                             and routine "bsf.loadClass()"

          2005-08-30, rgf: - now also assigning the beanName (index into BSF registry) as objectName
                             for Java class objects; this way no special consideration has to be
                             applied if JCOs are used as arguments; the ooRexx classes reveal the
                             fully qualified Java class name either via the defaultName-method, via
                             the class attribute "bsf.FullJavaClassName" or by sending them the
                             "toString()" message;
                             license change to Apache v2.0 and CPLv1.0

          2005-07-30, rgf: - fixed a bug in creation of array: now pre-registered names (entries in .bsf4rexx)
                             and any other Java class object proxy can be used to indicate component type for the
                             Java array

          2005-07-10, rgf: - added public routines bsf.lookupBean(), bsf.unregisterBean(),
                             bsf.pollEventText(), bsf.postEventText(), bsf.getStaticValue(),
                             bsf.getStaticValueStrict() and synonym bsf.getConstant(),
                             bsf.createArray(): makes it easier to code than the
                             versions ".bsf~bsf.xyz()"

          2005-07-06, rgf: - renamed "bsf.checkResult" into "bsf.wrap"

          2005-07-04, rgf: - corrected bug in MAKEARRAY: now the individual elements are wrapped
                             in BSF_REFERENCE, if they represent Java objects

          2005-06-07, rgf: - renamed "bsf.box" to "box" and "bsf.unbox" to "unbox"

          2005-06-06, rgf: - bsf.import changed: if second argument is .nil or the empty string,
                             then - and only then - there will be no entry in .local; if 2nd
                             argument is omitted, then the Java class name is used as the name
                             on ooRexx

          2005-06-05, rgf: - renamed BSF-pass through class methods to begin with "BSF.",
                           - using pre-registered Java-class proxies for box() and unbox(),
                           changed bsf.import logic to allow for dynamic allocating (if a
                           Java object proxy needs to access its Java class object):
                           - added instance method "bsf.class" to return the Java class object
                           proxy, which allows accessing the static methods and fields as well;


          2005-05-07, rgf: added bsf.getFieldValueStrict (strict case!),
                      methods having a trailing "=" are interpreted as setter methods, causing
                      the "setFieldValue" subfunction to be invoked using the method name without
                      the trailing equal sign as the name of the field

          2005-05-02, rgf, imported the primitive type wrapper classes into ooRexx
                           added box() and unbox(),
                           removed .box.true and .box.false (boxed as Booleans), -- 2005-05-03, rgf

          2004-03-04, rgf, test if BSFLoadJava() is registered before invoking it
          2004-02-22, rgf, trap exception on registering external Rexx-functions; needed
                      for Tobias Specht for debugging BWS on Konqueror;
                      partially removing a reported bug by TS (ORX not able handling literals
                      larger than 250 chars in INTERPRET)

          2003-11-19, rgf, added "RAISE PROPAGATE" statements in order for Rexx programs
                      being able to intercept exceptions which may occur in programs using
                      the proxy classes of this file

          2003-09-05, rgf, added "newStrict" floating constructor method for imported
                      Java classes and "addNewStrict" class method to attach it to
                      Object Rexx proxy classes by the "import" class method (i.e. setting
                      up an Object Rexx class as a proxy), to allow using strictly typed
                      arguments at instantiation time

          2003-06-01, rgf, added Apache license

          2003-05-10, rgf, changed "_new" to "Strict", changed arrays to never
                      use strict typing (not necessary as type can be inferred
                      from array itself)

          2003-05-07, rgf, using the new "invoke_new", which allows for not
                      using type information with the arguments

          2003-03-10, rgf, changed ".bsf.cls" to ".bsf4rexx" and ".JC" to ".class"
          2003-02-09, rgf, no dimensional limits on arrays anymore,
                           makearray(), supplier() now created at the Java side, merely re-using

          2003-01-29, Augsburg-->Vienna (train), adding direct JVM-loading support,
                      BSF_PROXY, .BSF.cls (for accessing pre-registered Java class objects)

          2003-01-28, Augsburg, renamed "BSF_ARRAY" to "BSF_ARRAY_PROXY" to denote its purpose
          2001-04-22, 2001-05-02 (on the airplane, returning home
                                  from the 12th International Rexx Symposium),
          2001-05-07 (fixed bug in bsf:init, if directly used to instantiate data)
          2001-05-26 (made method "bsf.exit" available at the instance level)
          2001-06-02 (BSF: made method "bsf.sleep" available at the instance level and
                      as "sleep" at the class level;
                      BSF_ARRAY_PROXY: MAKEARRAY and SUPPLIER now only return entries with
                                 values as does Object Rexx with native arrays)



   version:     4.0.6 (20110220)

   authors:     (c) 2001-2009 Rony G. Flatscher, University of Augsburg, University of Essen,
                    WU (pronounced: "vey-uh") Vienna;
                    Peter Kalender (October 2000 - February 2001), University of Essen, feasibility study

  If this program is part of a distribution of an Apache foundation project (e.g. BSF,
  the Bean Scripting Framework), then the immediately following Apache foundation license
  applies, if it gets distributed via the "http://www.ooRexx.org" project , then the
  CPL 1.0 license which follows thereafter.


------------------------ Apache Version 2.0 license -------------------------
   Copyright 2001-2011 Rony G. Flatscher

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



------------------------ Common Public License 1.0 --------------------------
   Copyright 2001-2011 Rony G. Flatscher

   This program and the accompanying materials are made available under the
   terms of the Common Public License v1.0 which accompanies this distribution.

   It may also be viewed at: http://www.opensource.org/licenses/cpl1.0.php
-----------------------------------------------------------------------------

*/

/*
   -- last change:  - Revision: 194  - Author: rony  - Date: 2007-02-01 00:22:00 +0100 (Thu, 01 Feb 2007)
   .local~revision.rev   ="$Rev: 927 $"
   .local~revision.date  ="$Date: 2010-12-26 18:34:19 +0100 (Sun, 26 Dec 2010) $"
   .local~revision.author="$Author: Administrator $"
   .local~revision.url   ="$URL: file:///cygdrive/e/svnrgf/bsf4rexx/trunk/BSF4ooRexx.dev/bin/BSF.CLS $"
   .local~revision.id    ="$Id: BSF.CLS 927 2010-12-26 17:34:19Z Administrator $"
*/

   signal on syntax
   parse version v .
   minimumVersion="4.0.1"
   if v < "REXX-ooRexx_"||minimumVersion then
   do
       parse source s . name  -- extract name of this module
       errString=name": needs ooRexx" minimumVersion "or higher"
       .error~say(errString", aborting...")
       raise syntax 3.900 array (errString)
   end

   parse source opsys .       -- get name of operating system, rgf, 2010-02-20

   if BsfInvokedBy()=0 then   -- JVM not yet loaded
   do
--      call BsfLoadJava
         -- process Java runtime args, if any
      jso=value('BSF4Rexx_JavaStartupOptions', ,"ENVIRONMENT")

      if opsys="MACOSX" then  -- as of 2010-02-20: if loading Java via Rexx, awt-event thread does not work
         jso=(jso "-Djava.awt.headless=true")~strip   -- make sure that Java hints at the cause of the problem

      arr=parseJavaOptions(jso)
      stmt=createJavaLoadStatement(arr)      -- create the BsfLoadJava-statement
/*
      if jso<>"" then      -- environment variable set, if so, show its value
         say "BSF.CLS: ... using environment variable 'BSF4Rexx_JavaStartupOptions' to configure Java:" pp(jso) "..."
*/
      interpret stmt       -- carry out the statement in the string
   end

   parse version "_" v "("          -- get ooRexx version, 2008-08-31, rgf
   .local~ooRexx.version=v          -- get and save ooRexx's version

   -- since 2008-06-22 (rgf) this forces RexxAndJava to prepend type indicators!
   call bsf "bsfPrefixReturnValue", .true

   call initialize.BSF.dir          -- set up .bsf4rexx and its synonym .b4r

   .bsf4rexx~version="406.20110525" -- set version
   .bsf4rexx~opSys  =opsys          -- save operating system name as supplied by Rexx' PARSE SOURCE
   .bsf4rexx~opSys1 =opsys~left(1)~upper  -- save operating system name's initial letter in uppercase
   .bsf4rexx~opSys2 =opsys~left(2)~upper  -- save operating system name's first two letters in uppercase
   .bsf4rexx~opSys3 =opsys~left(3)~upper  -- save operating system name's first two letters in uppercase

   .bsf.dialog~setDefaultComponent  -- Initialize the default component object (cannot be done in class' constructor)

   .local~bsf=.bsf      -- save .BSF in .local as well, to make it findable in callback situations

   call getAndSetPathToIcons

   if opsys="MACOSX" then
   do
      .java.lang.system~setProperty('apple.laf.useScreenMenuBar','true') -- just used on Macs: use Apple's menu dock, for JFrame-menus insteadof the window's menu bar
           -- by default hide "About" and "Preferences" menu in Apple's menu bar
      app=bsf.loadClass("com.apple.eawt.Application")~getApplication
      .bsf4rexx~apple_application=app   -- get and save Apple's application object to ease use in dependent programs
      app~removeAboutMenuItem
      app~removePreferencesMenuItem
      if .bsf4rexx~hasEntry("bsf4oorexx_256") then
         app~setDockIconImage(.bsf~new("javax.swing.ImageIcon",.bsf4rexx~bsf4oorexx_256)~getImage)
   end

   call BSfShowErrorMessage .false  -- turn off displaying the Java exception message (leftover from the very early BSF4Rexx days)

   return

syntax:
   raise propagate


::requires "BSF4ooRexx" library

::routine getAndSetPathToIcons	-- rgf, 2011-02-21: find path to icons, if found save icons
  opsys=.bsf4rexx~opsys~left(1)     -- get first letter of operating system (L=Linux, M=MacOSX, W=Windows, ...)

  parse source opsys +1 1 . kind path2this

  if opsys="W" then
  do
     paths=.array~of(filespec("Location", path2this)"install\images\linux\hicolor\")
  end
  else
  do
     paths=.array~of(filespec("Location", path2this)"install/images/linux/hicolor/", -  -- look in directory of this program (may be linked to somewhere else)
                     "/opt/BSF4ooRexx/install/images/linux/hicolor/",                -  -- fallback location
                     "/Applications/BSF4ooRexx.app/Current/Resources/",              -  -- hypothetic location on MacOSX (may need adjustment)
                     "/Users/rony/bsf4oorexx.201012/root201012/trunk/bsf4oorexx.dev/images/linux/hicolor/" - -- Rony's developer path (to be removed)
                     )
  end

  path2icon=""
  do entry over paths
     res=SysFileExists(entry)
     if res=1 then
     do
        path2icon=qualify(entry)
--        say "... BSF.CLS::getAndSetPathToIcons (debug): - path2icon found! ["path2icon"] ..."
     end
  end
  if path2icon="" then return     -- no path found

  path2icon=path2icon || .bsf4rexx~file.separator   -- append file separator

	-- create entries to exisiting icon files in the given resolutions
  imageNames=.array~of("bsf4oorexx", "oorexx4ooo")
  do res over .array~of("016", "032", "064", "048", "128", "256")
     do name over imageNames
        tmpName=name"_"res
        fullPath=path2icon || tmpName".png"
        if SysFileExists(fullPath) then
        do
           .bsf4rexx~setEntry(tmpName, fullPath)
--           say "... BSF.CLS::getAndSetPathToIcons (debug):" tmpName "->" fullPath "..."
        end
     end
  end

  return





/* Initialize BSF.DIR which is stored with the BSF class object, such that
   each interpreter instance is separated from each other w.r.t. the objects
   stored in there.
*/
::routine initialize.BSF.dir
--   bsf.dir=.bsf~dir        -- get .BSF's directory object
  bsf.dir=.bsf4rexx~dir        -- get .BSF's directory object
   /*
      pre-registered classes (in the BSF-registry) to make it easier to
      indicate the most important Java class-objects from the ooRexx side;

      just send the Java datatype name (bool, byte, char, int, long,
      float, double, or the Java class names representing the primitive datatypes
      as objects: boolean.class, byte.class, character.class, integer.class,
      long.class, float.class, double.class; in addtion the important Java class
      object names: class.class, object.class, method.class, array.class,
      string.class, system.class) to the directory ".bsf4rexx" and use the
      returned object as argument for the Java side
   */
   arr=.array~of( ,
      "Class.class",      'Class.class'        ,,   -- a Java Object class
      "Object.class",     'Object.class'       ,,   -- a Java Object class
      "Method.class",     'Method.class'       ,,   -- a Java Object class
       ,
      "Array.class",      'Array.class'        ,,   -- a Java Object class
      "String.class",     'String.class'       ,,   -- a Java Object class
      "System.class",     'System.class'       ,,   -- a Java Object class
      "Thread.class",     'Thread.class'       ,,   -- a Java Object class
       ,
      "boolean",          'boolean.class'      ,,   -- primitive
      "boolean.class",    'Boolean.class'      ,,   -- a Java Object class
      "byte",             'byte.class'         ,,   -- primitive
      "byte.class",       'Byte.class'         ,,   -- a Java Object class
      "char",             'char.class'         ,,   -- primitive
      "Character.class" , 'Character.class'    ,,   -- a Java Object class
      "double",           'double.class'       ,,   -- primitive
      "Double.class",     'Double.class'       ,,   -- a Java Object class
      "float",            'float.class'        ,,   -- primitive
      "Float.class",      'Float.class'        ,,   -- a Java Object class
      "int",              'int.class'          ,,   -- primitive
      "Integer.class",    'Integer.class'      ,,   -- a Java Object class
      "long",             'long.class'         ,,   -- primitive
      "Long.class",       'Long.class'         ,,   -- a Java Object class
      "short",            'short.class'        ,,   -- primitive
      "Short.class",      'Short.class'        ,,   -- a Java Object class
      "void",             'void.class'         ,,   -- primitive
      "Void.class",       'Void.class'         )    -- a Java Object class

  do i=1 to arr~items by 2    -- pre-registered classes (see above)
     bsf.dir~setentry(arr[i], .bsf_reference~new(arr[i+1]))
  end

      -- Java classes for "String" and those representing the primitive datatypes
      -- creates ooRexx proxy classes
   arr=.array~of( ,
      'java.lang.String'            ,,   -- a Java Object class
      'java.lang.Boolean'           ,,   -- a Java Object class
      'java.lang.Byte'              ,,   -- a Java Object class
      'java.lang.Character'         ,,   -- a Java Object class
      'java.lang.Double'            ,,   -- a Java Object class
      'java.lang.Float'             ,,   -- a Java Object class
      'java.lang.Integer'           ,,   -- a Java Object class
      'java.lang.Long'              ,,   -- a Java Object class
      'java.lang.Short'             ,,   -- a Java Object class
      'java.lang.System'            )    -- a Java Object class

  do i=1 to arr~items
      -- make these classes accessible as if they were ooRexx classes
      jClassName=arr[i]
     bsf.dir~setEntry(jClassName, .bsf~bsf.import(jClassName)) -- will also place in .local
  end


  do key over .list~of("line.separator", "file.separator", "path.separator")
     bsf.dir~setEntry(key,bsf.dir~java.lang.System~getProperty(key))
  end

  return





-- box primitive datatypes into their Java classes counterparts
-- BOolean, BYte, C, D, F, I, L, SHort, STring

::routine box  public
   parse upper arg type +2, val

   signal on syntax
   select
     when abbrev(type, "BO") then
          do
             if      val=.true  | val~caselessEquals("true")  then val="true"
             else if val=.false | val~caselessEquals("false") then val="false"
             else    -- raise a syntax error, in this case we expect a Boolean value!
             do
                raise syntax 88.900 array ("BSF.CLS - BOX('Boolean', string): argument 'string' does not represent an acceptable Boolean value. Acceptable values are: '0', '1', '.true', '.false', and in addition 'true', 'false'. Received: '"val"'")
             end

             return .java.lang.Boolean~new(val)
          end

     when abbrev(type, "BY") then return .java.lang.Byte~new(val)

     when abbrev(type, "C")  then return .java.lang.Character~new(arg(2)) -- use val verbatimly (retain mixed case)
     when abbrev(type, "D")  then return .java.lang.Double~new(val)

     when abbrev(type, "F")  then return .java.lang.Float~new(val)
     when abbrev(type, "I")  then return .java.lang.Integer~new(val)
     when abbrev(type, "L")  then return .java.lang.Long~new(val)
     when abbrev(type, "SH") then return .java.lang.Short~new(val)
     when abbrev(type, "ST") then return .java.lang.String~new(arg(2))    -- use val verbatimly (retain mixed case)

     otherwise return arg(2)
   end

syntax:
   raise propagate


-- unbox primitive datatype from their Java class counterpart, where they are embedded
-- BOolean, BYte, C, D, F, I, L, SHort, STring
::routine unbox public
   use arg o

     -- extract class name from proxy's objectName (e.g. "java.lang.String@a3b0cdef")
   parse upper value o~objectname with . "." . "." className "@" .

     -- if not a wrapper class return object unchanged
   if pos(className, "BOOLEAN BYTE CHARACTER DOUBLE FLOAT INTEGER LONG SHORT STRING")=0 then return o

     -- toString() would return instead of "1"/"0" the values "true"/"false"
   if abbrev(className, "BO")  then return o~booleanValue

   return o~toString -- Java string representation suffices for ooRexx


/* ---> rgf, 2010-06-08: superfluous routine
-- register BSF() external functions, assume that Java needs to be loaded
::routine registerBSFFunctions

   call rxFuncAdd "BsfLoadFuncs", "BSF4ooRexx", "BsfLoadFuncs"
   signal on syntax
   call BsfLoadFuncs    -- register all the BSF4ooRexx functions ...

syntax:
   -- signal off syntax
   if var("SIGL") then
   do
      say "BSF.CLS: SIGL="sigl "= line # with exception, line: ["sourceline(sigl)"]" -- nur bei exception
      say "         exception:" condition("C")
      say "         traceback:"
      do line over condition("o")~traceback
          say "                  " line
      end
   end

   if rxFuncQuery("BSFLOADJAVA") = 0 then -- load JVM only, if BSFLoadJava() is registered
   do
      -- .error~say( "... loading JVM ...")
      -- call BsfLoadJava     -- load JVM

      -- 2007-01-09, ---rgf
         -- try to retrieve Java startup options from host environment; if not defined, then
         -- an empty string is returned and the Java startup option array will have no entries
      arr=parseJavaOptions(value('BSF4Rexx.JavaStartupOptions', ,"ENVIRONMENT"))
      stmt=createJavaLoadStatement(arr)      -- create the BsfLoadJava-statement
      interpret stmt       -- carry out the statement in the string
   end
   else
   do
      .error~say("... cannot load the JVM, BSFLoadJava() not registered.")
   end

   return
<--- */

::routine createJavaLoadStatement public
  use arg a
  tmpArgs=.mutableBuffer~new
  do i=1 to a~items
     if i>1 then tmpArgs~append(", ")
     tmpArgs~append(escapeQuotes(a~at(i)))
  end

  return "CALL BsfLoadJava" tmpArgs~string

  escapeQuotes: procedure
     return '"'||changestr('"', arg(1), '""')||'"'

/*
   parse Java startup options which can be given via the operating system environment
   variable named 'BSF4Rexx.JavaStartupOptions'; returns an array with each option
   stored in its own slot
*/
::routine parseJavaOptions public
  parse arg options
  a=.array~new
  i=0
  deli=" '" || '"'         -- delimiters for options/option-values

  do mainLoop=0 while options<>""
     pos=pos("-", options)
     if pos=0 then         -- no more options available?
     do
        if i=0 then
        do
           i=1             -- first (and only) entry
           a[i]=options    -- remainder of value for previous option
        end
        else
           a[i]=a[i] || options  -- remainder of value for previous option
        return a
     end

     preVal=substr(options, 1, pos-1)  -- get string before dash, if any

     if preVal<>"" then
     do
        if preVal~length>1 then  -- more left than the delimiting blank char?
        do                    -- this value belongs to previous or first (?) option
           tmpVal=substr(preVal,1,length(preVal)-1)
           if i=0 then
           do
              i=1
              a[i]=tmpVal     -- some unknown first option?
           end
           else
              a[i]=a[i] || tmpVal   -- add to previous option's value
        end
     end

     -- new option to deal with
     tmpPos=pos
     i=i+1
     do findNextSwitch=1 while .true
        nextSwitchPos=pos(" -", options, tmpPos+1) -- get position of next switch
        if nextSwitchPos<>0 then             -- a new switch found ?
        do
           begQuot=pos('"', options, tmpPos+1)
           if begQuot<nextSwitchPos then     -- a quote, check whether " -" is part of it
           do
              endQuot=pos('"', options, begQuot+1)
              if endQuot>nextSwitchPos then  -- oops, " -" is part of string, ignore it
              do
                 tmpPos=endQuot              -- define new start for looking for next switch
                 iterate findNextSwitch      -- try to find next switch
              end
           end

           a[i]=substr(options, pos, nextSwitchPos-pos)
           options=substr(options, nextSwitchPos+1)
           iterate mainLoop
        end

        if nextSwitchPos=0 then  -- last option in hand
        do
           a[i]=substr(options, pos)
           return a
        end
     end

  end
  return a






/* -------------------------------------------------------------
   routine which turns an array into an argument string as expected
   by the Java side, starting with the indicated element
   ------------------------------------------------------------- */
::ROUTINE makeArgString
   use arg args

   tmpString    = ""
   do i=1 to args~items
      if args[i]=.nil then
         tmpString = tmpString ', "' || .bsf~bsf.null_string || '"'
      else
         tmpString = tmpString ', "' || args[i] || '"'
   END
   return tmpString


/* -------------------------------------------------------------
   routine which mimicks C/Java's "x ? a : b;"
   if arg(1) is true, returns second argument, else third
   ------------------------------------------------------------- */
::ROUTINE choose
   if arg(1)=.true then return arg(2)
                   else return arg(3)


/* -------------------------------------------------------------
   floating class method meant for attaching to imported Java
   classes (for which an Object Rexx proxy class is created)
   to construct an instance (a "bean") with strict arguments (i.e.
   type information are given for each argument; arguments are
   passed to BSF which uses them to create the bean instance
   from the given Java class, ---rgf, 2003-09-05
   ------------------------------------------------------------- */
::METHOD newStrict
  USE ARG javaClass, args

  instance=.bsf_reference~new("uninitialized BSF-object!")

        -- create an instance of javaClass and register it with BSF,
        -- leave beanName empty, this way we get a unique name from Java

   if args~class = .array then tmpArr=args            -- array of arguments
                          else tmpArr=arg(1, "Array") -- create an array from arguments

   -- code = 'beanName=BSF( "registerBeanStrict", , "' || self~bsf.FullJavaClassName || '"'
   code = 'beanName=BSF( "newStrict", , "' || self~bsf.FullJavaClassName || '"'
   do i=1 to tmpArr~items
      a.i=choose(tmpArr[i]=.nil, .bsf~bsf.null_string, tmpArr[i])
      code=code ", a."i
   end
   code=code ") ~substr(4)"  -- skip indicator string

  signal on syntax name any         -- trap any exceptions
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  interpret code        /* execute the code, calling bsf() with 'registerBean'  */

        -- assign beanName to object, use the Java supplied name
  instance~ObjectName=beanName

  return instance

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* -------------------------------------------------------------
   routine which checks whether data returned from the Java side
   represents a Java object; if so, an Object Rexx proxy is created
   and returned, else the received string value
   ------------------------------------------------------------- */
   -- 2009-02-10, rgf: prefixes are always set by BSF.CLS; however RexxEngine only prefixes
   --                  bean-names with the object identifier "<O>", but leaves all other
   --                  strings intact (if a string argument in RexxEngine starts with the
   --                  object identifier "<O>", then that string will be prefixed with the
   --                  string identifier "<S>", which gets processed by bsf.wrap() as
   --                  expected)
::ROUTINE bsf.wrap public
   parse arg indicator +3 data

-- .error~say( "bsf.wrap: arg(1)="pp(arg(1)) "indicator="pp(indicator) "data="pp(data) )

-- .error~say( "in bsf.wrap(): received="pp(arg(1)))
-- .error~say( "   indicator:" pp(indicator) "data:" pp(data))

   if indicator="<S>" then          -- genuine string
   do
      return data                   -- return string portion
   end
   else if indicator="<O>" then     -- Java object, i.e. index for BSFRegistry
   do
      if data=.bsf~bsf.null_string then
         return .nil

      dimension = verify(data, "[")-1  -- is it a Java array?
      if dimension>0 then   -- rgf, 2003-02-09, no upper limit anymore
         return .bsf_array_reference~new(data, dimension) -- return a BSF_ARRAY proxy

      return .bsf_reference~new(data)  -- return a BSF_REFERENCE
   end

-- .error~say( "in bsf.wrap(): no prefix:" pp(arg(1))) "(returning as is)"

   return arg(1)                    -- an unprefixed string argument from RexxEngine

/*
      -- if arriving here, we ran into a problem as we did not receive a proper indicator prefix
   raise syntax 98.900 array ("BSF.CLS - BSF.WRAP(): received an untyped string (not prefixed by '<S>' [plain string] nor by '<O>' [index into BSF-Registry]):" pp(arg(1)))
   -- return arg(1)           -- return value that we received unchanged
*/


/* -------------------------------------------------------------
   routine which checks whether data returned from the Java side
   represents a Java object; if so, an Object Rexx proxy is created
   and returned, else the received string value
   ------------------------------------------------------------- */
::ROUTINE bsf.wrap_original_old -- public
   parse arg data

   if data=.bsf~bsf.null_string then return .nil

        /* if an object was returned, it has been registered on the Java side;
           therefore reuse, if possible or, create an Object Rexx proxy of it;
           first check whether the name was constructed on the Java side        */
  if pos("@", data)>0 then      -- is this a beanName, created by Java?
  do
     -- parse var data before '@' after
     parse var data before '@' after "_" after2
     if before <> "" then
     do
        if datatype(after, "X") then    -- o.k. second part a hexadecimal value?
        do
           if pos(" ", after)=0 then    -- o.k. no space there?
           do
              -- create an Object Rexx proxy object for the Java object

              dimension = verify(data, "[")-1  -- is it a Java array?
              -- if dimension>0 & dimension<6 then
              if dimension>0 then   -- rgf, 2003-02-09, no upper limit anymore
              do
                 return .bsf_array_reference~new(data, dimension) -- return a BSF_ARRAY proxy
              end

              return .bsf_reference~new(data)   -- return a BSF_REFERENCE prox
           end
        end
     end
  end

  return data           -- assume the string of a primitive type



/* Used to be a directory entry in .local: however, since .local is shared among all
   ooRexx interpreter instances of a process in pre-4-ooRexx interpreters, we need to
   offload it into a public class (which are individually created for each interpreter
   instance) which acts like a directory:
*/
::CLASS BSF4Rexx  public
::method init     class
  expose dir
  dir    =.directory~new

::method unknown  class
  expose dir
  use arg name, args
  forward to (dir) message (name) arguments (args) -- re-direct message to directory

::attribute dir   class    -- accessor methods


/* Alias class for "BSF4REXX" (used to be an entry in .local, pointing to .bsf4rexx */
::CLASS B4R public
::method unknown  class
  use arg name, args

  forward to (.bsf4rexx~dir) message (name) arguments (args) -- re-direct message to directory



::attribute dir   class



/* *************************************************************
   Class:       IBM's "Bean Scripting Framework"
   Purpose:     the proxy super-class for the Java classes
   ************************************************************* */
::CLASS BSF PUBLIC

/* -------------------------------------------------------------
   class method which merely initializes the class attribute "bsf.null_string"
   to its default value
   ------------------------------------------------------------- */
::METHOD init           CLASS
  -- EXPOSE bsf.null_string bsf.FullJavaClassName bsf.JavaClassObjectProxies bsf.JavaClassObjects dir
  EXPOSE bsf.null_string bsf.FullJavaClassName


  bsf.null_string=".NIL"
  bsf.FullJavaClassName=.nil     -- this is an Object Rexx path, hence no fully
                         -- qualified Java class available

/* 20100508, rgf
  bsf.JavaClassObjectProxies=.directory~new
  bsf.JavaClassObjects      =.directory~new
  dir                   =.directory~new
*/

/* -------------------------------------------------------------
   class method to allow invoking static methods via ooRexx
   ------------------------------------------------------------- */
::method unknown class
  expose bsf.jco  -- 2010-05-08, rgf

  signal on syntax name any
  if self~bsf.FullJavaClassName=.nil then   -- no fully qualified Java class ("path") given ?
  do
      -- Object "object" does not understand message "message"
     raise syntax 97.1 array(self~string, arg(1))
  end

     -- forward unknown message to the BSF proxy object representing the Java class object,
     -- this way it becomes possible to access the Java static (class) methods quite easily

-- 2010-05-08, rgf   -- rgf, 2005-06-11: use the Java class proxy object to dispatch the message
-- 2010-05-08, rgf  forward to (.bsf~bsf.getJavaClassObjectProxy(self~bsf.FullJavaClassName)) MESSAGE (arg(1)) ARGUMENTS (arg(2))
-- say "unknown::class, forwarding to" pp(bsf.jco) "..."
  forward to (bsf.jco) MESSAGE (arg(1)) ARGUMENTS (arg(2))  -- 2010-05-08, rgf


any:
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



::method bsf.jco attribute class private -- contains the BSF proxy to the Java Class Object (JCO) in
                                         -- the BSF registry, such that it does not get garbage collected!


/* Utility method to allow storing the jco-object in an attribute as well as the full Java class name
 * this class represents.

 -->    new_class~bsf.setBSF4RexxInfos(new_class, jco, java_path)  -- rgf 2010-04-18
*/
::method bsf.setBSF4RexxInfos class
  expose bsf.jco bsf.FullJavaClassName
  if arg(1)<>self | arg()<>3 then return .false

  bsf.jco=arg(2)
  bsf.FullJavaClassName=arg(3)

  return .true


/* -------------------------------------------------------------
   class method allows for creating an ooRexx proxy class for a Java class
   which is made available to this process by placing it (optionally) into the
   .local environment, unless the second argument is explicitly .nil
   ------------------------------------------------------------- */
::METHOD bsf.import class
--  USE ARG classname, bsf.FullJavaClassName
  USE ARG java_path, classname, extendedJavaClz=.nil   -- 2005-06-05, changed argument sequence

  bNoClassName=(arg(2, "Omitted") | className="" | .nil=className)

  signal on syntax name any
  -- if arg()>2 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)

  if arg()>3 then
  do
  -- if arg()>2 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
     raise syntax 40.900 array ("BSF.CLS/class BSF/class method bsf.import, error 1: too many arguments (maximum: 3, found: "arg()")")
  end

/* 20100508, rgf
  o=.bsf~bsf.JavaClassObjects[java_path] -- try to get the ooRexx class object proxy
  if .nil=o then     -- not loaded yet, create ooRexx proxy class
  do
*/
     if extendedJavaClz=.nil then   -- let Java create the class object, which we can refer to
        jco=bsf.loadClass(java_path)   -- get Java class object (can be a BSF_Reference !)
     else                           -- use the passed Java class object
        jco=extendedJavaClz

-- 20100508, rgf     .bsf~bsf.JavaClassObjectProxies[java_path]=jco     -- save jco in directory

         -- now create proxy class object
     new_class = .BSF~subclass(java_path) -- create a subclass of .BSFClass (a proxy ooRexx class)
     new_class~bsf.setBSF4RexxInfos(new_class, jco, java_path)
     new_class~objectName=jco~objectname      -- now assign beanName

-- 20100508, rgf     .bsf~bsf.JavaClassObjects[java_path]=new_class    -- save the ooRexx Java class object proxy

     -- 20100419, rgf, TODO: check whether beanName is also stored in the class attribute
     --                      if so, UNO.CLS can be changed to use that instead of .bsf~bsf.JavaClassObject*()

         -- attach a "newStrict" constructor method to the Object Rexx class object
         -- a MUST: bsf.attachNewStrict supplies the attribute "bsf.FullJavaClassName" with a correct value
     -- if extendedJavaClz=.nil then    -- only add it to a normally loadable class, otherwise RexxAndJava's REGISTER_BEAN_STRICT needs to be updated
     new_class~bsf.attachNewStrict( java_path )


         -- create code for the INIT-method of the new class
/*
     if extendedJavaClz=.nil then
        method_txt = 'self~init:super("' || java_path || '", ARG(1,"A"))'
     else
        method_txt = 'self~init:super("' || extendedJavaClz~string || '", ARG(1,"A"))'
*/

     method_txt = 'self~init:super("' || jco~string || '", ARG(1,"A"))'
     new_class~define('init', method_txt)  -- define the constructor instance method "INIT"

/* 20100508, rgf
      -- TODO: determine whether this can ever run as a reference is always in .bsf~bsf.JavaClassObjects
      -- make sure cached Java class object gets removed from cache
     uninitCode='.bsf~bsf.JavaClassObjectProxies~remove("'java_path'");' -
                '.bsf~bsf.JavaClassObjects~remove("'java_path'");'
     new_class~setmethod('uninit', uninitCode)
*/

/* 20100508, rgf
  end
  else   -- we had loaded the Java class object already, make it ready to be returned ...
  do
     new_class=o
  end
*/

  if .nil=arg(2) then return new_class

  -- save the new BSF proxy class in .local to make it available as any other ooRexx class
  if bNoClassName then className=java_path   --- use the Java class name on the ooRexx side as well
  .local~setentry(className, new_class) -- put new ooRexx proxy class into the local environment

  return new_class

any:
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* -------------------------------------------------------------
   class method allows for creating an ooRexx proxy class for a Java class
   which is made available to this process by placing it (optionally) into the
   .local environment, unless the second argument is explicitly .nil
   ------------------------------------------------------------- */
::METHOD bsf.importClass class
  USE ARG java_path, classname, extendedJavaClz=.nil   -- 2005-06-05, changed argument sequence

  bNoClassName=(arg(2, "Omitted") | className="" | .nil=className)

  signal on syntax name any

  if arg()>3 then
  do
     raise syntax 40.900 array ("BSF.CLS/class BSF/class method bsf.import, error 1: too many arguments (maximum: 3, found: "arg()")")
  end

  if extendedJavaClz=.nil then   -- let Java create the class object, which we can refer to
     jco=bsf.loadClass(java_path)   -- get Java class object (can be a BSF_Reference !)
  else                           -- use the passed Java class object
     jco=extendedJavaClz

      -- now create proxy class object
  new_class = .BSF~subclass(java_path) -- create a subclass of .BSFClass (a proxy ooRexx class)
  new_class~bsf.setBSF4RexxInfos(new_class, jco, java_path)
  new_class~objectName=jco~objectname      -- now assign beanName

      -- attach a "newStrict" constructor method to the Object Rexx class object
      -- a MUST: bsf.attachNewStrict supplies the attribute "bsf.FullJavaClassName" with a correct value
  -- if extendedJavaClz=.nil then    -- only add it to a normally loadable class, otherwise RexxAndJava's REGISTER_BEAN_STRICT needs to be updated
  new_class~bsf.attachNewStrict( java_path )

      -- create code for the INIT-method of the new class
  method_txt = 'self~init:super("' || jco~string || '", ARG(1,"A"))'
  new_class~define('init', method_txt)  -- define the constructor instance method "INIT"

  if .nil=arg(2) then return new_class

  -- save the new BSF proxy class in .local to make it available as any other ooRexx class
  if bNoClassName then className=java_path   --- use the Java class name on the ooRexx side as well
  .local~setentry(className, new_class) -- put new ooRexx proxy class into the local environment

  return new_class

any:
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ---> rgf, 2010-04-18

/* -------------------------------------------------------------
   class method allows for creating an ooRexx proxy class for a Java class
   which is made available to this process by placing it (optionally) into the
   .local environment, unless the second argument is explicitly .nil
   ------------------------------------------------------------- */
::METHOD bsf.import class
--  USE ARG classname, bsf.FullJavaClassName
  USE ARG java_path, classname   -- 2005-06-05, changed argument sequence

  bNoClassName=(arg(2, "Omitted") | className="" | .nil=className)

  signal on syntax name any
  if arg()>2 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
     raise syntax 40.900 array ("BSF.CLS/class BSF/class method bsf.import, error 1: too many arguments (maximum: 2, found: "arg()")")

  o=.bsf~bsf.JavaClassObjects[java_path] -- try to get the ooRexx class object proxy
  if .nil=o then     -- not loaded yet, create ooRexx proxy class
  do
      -- let Java create the class object, which we can refer to
     jco=bsf.loadClass(java_path)  -- get Java class object (can be a BSF_Reference !)

     .bsf~bsf.JavaClassObjectProxies[java_path]=jco     -- save jco in directory

         -- now create proxy class object
     new_class = .BSF~subclass(java_path) -- create a subclass of .BSFClass (a proxy ooRexx class)
     new_class~bsf.setBSF4RexxInfos(new_class, jco, java_path)
     new_class~objectName=jco~objectname      -- now assign beanName

     .bsf~bsf.JavaClassObjects[java_path]=new_class    -- save the ooRexx Java class object proxy

         -- attach a "newStrict" constructor method to the Object Rexx class object
     new_class~bsf.attachNewStrict( java_path )

         -- create code for the INIT-method of the new class
     method_txt = 'self~init:super("' || java_path || '", ARG(1,"A"))'
     new_class~define('init', method_txt)  -- define the constructor instance method "INIT"
  end
  else   -- we had loaded the Java class object already, make it ready to be returned ...
  do
     new_class=o
  end

  if .nil=arg(2) then return new_class

  -- save the new BSF proxy class in .local to make it available as any other ooRexx class
  if bNoClassName then className=java_path   --- use the Java class name on the ooRexx side as well
  .local~setentry(className, new_class) -- put new ooRexx proxy class into the local environment

  return new_class

any:
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

<--- */




/* -------------------------------------------------------------
   allows to retrieve the fully qualified Java class name, if a
   Java class got imported (encapsulated in an Object Rexx class
   proxy); needs to be a method in order to be able to use Object's
   SETMETHOD
   ------------------------------------------------------------- */
::METHOD bsf.attachNewStrict class
  use arg java_path
  if self~superclasses[1]<>.bsf then return   -- not a direct subclass of "BSF",
                                              -- hence not from "IMPORT"
  self~setmethod("newStrict", .methods["NEWSTRICT"])
  if self~bsf.FullJavaClassName=.nil then    -- rgf, 2005-08-28
     self~bsf.FullJavaClassName=java_path    -- save fully qualified Java class name


/* -------------------------------------------------------------
   allows to retrieve the fully qualified Java class name, if a
   Java class got imported (encapsulated in an Object Rexx class
   proxy); not directly accessible from outside the class
   ------------------------------------------------------------- */
::method bsf.FullJavaClassName class  -- can be read from any object
  expose bsf.FullJavaClassName
  return bsf.FullJavaClassName

::method  "bsf.FullJavaClassName=" class private  -- can only be set via "self"
  expose  bsf.FullJavaClassName
  use arg bsf.FullJavaClassName



/* 20100508, rgf
/* -------------------------------------------------------------
   class method which returns the BSF proxy object wrapping
   a Java class object
   ------------------------------------------------------------- */
::method bsf.getJavaClassObjectProxy class
  use arg javaClassName

  signal on syntax name any  -- tryp syntax condition
  o=self~bsf.JavaClassObjectProxies[javaClassName]   -- try to get proxy for Java class object
  if o=.nil then           -- no proxy for Java class object yet, create one
  do
     .bsf~bsf.import(javaClassName) -- import the Java class
     o=self~bsf.JavaClassObjectProxies[javaClassName]   -- try to get proxy for Java class object
  end
  return o        -- return the BSF Java class proxy

any:
  raise propagate  -- raise the exception in caller/invoker on the Rexx side
*/


/* 20100508, rgf: not needed anymore
/* -------------------------------------------------------------
   directory of BSF proxy objects referring to the Java class object
   ------------------------------------------------------------- */
::method bsf.JavaClassObjectProxies  ATTRIBUTE CLASS

/* -------------------------------------------------------------
   directory of ooRexx BSF proxy objects classes having new() and
   newStrict() defined on them
   ------------------------------------------------------------- */
::method bsf.JavaClassObjects        ATTRIBUTE CLASS

*/

/* -------------------------------------------------------------
   class attribute method stores and allows access to the string
   which is used to indicate "null" for the Java side; upon return
   of values from Java, a Java "null" will be represented via
   Object Rexx' .nil object
   ------------------------------------------------------------- */
-- ::METHOD bsf.null_string    unguarded ATTRIBUTE CLASS  --- rgf, 20090523
::ATTRIBUTE bsf.null_string get unguarded CLASS
::ATTRIBUTE bsf.null_string set CLASS



/* -------------------------------------------------------------
   method to construct an instance (a "bean"); arguments are
   passed to BSF which uses them to create the bean instance
   from the given Java class
   ------------------------------------------------------------- */
::METHOD init
  USE ARG javaClass, args

        -- create an instance of javaClass and register it with BSF,
        -- leave beanName empty, this way we get a unique name from Java

   if args~class = .array then tmpArr=args            -- array of arguments
                          else tmpArr=arg(2, "Array") -- create an array from arguments

   -- code = 'beanName=BSF( "registerBean", , "' || javaClass || '"'
   code = 'beanName=BSF( "new", , "' || javaClass || '"'
   do i=1 to tmpArr~items
      a.i=choose(tmpArr[i]=.nil, .bsf~bsf.null_string, tmpArr[i])
      code=code ", a."i
   end
   code=code ") ~substr(4)"         -- get rid of type indicator "<?>", where "?" is either "O" or "S"

  signal on syntax name any         -- trap any exceptions
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  interpret code        /* execute the code, calling bsf() with 'registerBean'  */

        -- assign beanName to object, use the Java supplied name
  self~ObjectName=beanName

  return self

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

/* -------------------------------------------------------------
   destructor method (called by Object Rexx when object gets
   destroyed) to unregister the bean from the BSF registry; this
   then allows the JVM to garbage collect that Java object as well
   ------------------------------------------------------------- */
::METHOD uninit
-- .error~say( "*** BSF.CLS::BSF::UNINIT:" pp(self~objectname)"@"pp(self~identityHash) )
  signal on syntax name any         -- trap any exceptions
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  return BSF("unregisterBean", self~objectname)~substr(4)

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* -------------------------------------------------------------
   dispatch method, which the programmers should use if they desire to
   directly dispatch a method on the Java side; this way they are not
   forced to use UNKNOWN directly (unless, of course, there exists a
   Java method "DISPATCH", which will force the direct usage of UNKNWON);
   will pass all received arguments (starting with the second argument)
   as an array object to UNKNOWN
   ------------------------------------------------------------- */
::METHOD bsf.dispatch
  use ARG methodName
-- .error~say( "BSF.DISPATCH: methodName="pp(methodName) )
  forward message ("BSF.INVOKE")


/* -------------------------------------------------------------
   unknown method (gets called by Object Rexx whenever an unknown
   message was sent to an instance of this class); forwards the
   message to BSF including all of the received arguments
   ------------------------------------------------------------- */
::METHOD unknown
  USE ARG name, args

-- .error~say("BSF.CLS - BSF::UNKNOWN()" "self="pp(self~objectname) "name="pp(name) "args="pp(args~makestring("l",",")~left(40)))

  signal on syntax name any         -- trap any exceptions
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  if args~class=.array then
  do
     if (right(name,1)="=") then -- field assignment?
     do
        items=args~items
        parse var name tmpName "=" . -- extract message name without '='
        if items=1 then             -- arg(1)=value to assign to field
           return self~bsf.setFieldValue(tmpName, args[1])
        else if items=2 then        -- assume arg(1)=typeindicator, arg(2)=value
           return self~bsf.setFieldValueStrict(tmpName, args[1], args[2])
     end
  end

/* 2004-02-22: can be longer than 250 chars which causes Object Rexx to bomb!
   2006-08   : ooRexx 3.1 resolved this issue, but leaving tested code unchanged */

/*
   code = 'call BSF "invoke", "' || self~objectname || '", "' || name || '"'
   do i=1 to args~items
      a.i=choose(args[i]=.nil, .bsf~bsf.null_string, args[i])
      code=code ", a."i
   end

  interpret code        -- execute this dynamically created Rexx string

/*
tmp=result
.error~say("BSF.CLS - BSF::UNKNOWN()" pp(code) "->" pp(tmp))
return bsf.wrap(tmp)
*/

  if var("RESULT") then return bsf.wrap(result) -- check whether primitive type or Java object
                   else return .nil             -- nothing came back, i.e. Java returned a true "null"

*/

   -- rgf, 20091002: add special handling of RexxProxy objects

/*
   code = 'res=BSF( "invoke", "' || self~objectname || '", "' || name || '"'
   do i=1 to args~items
      a.i=choose(args[i]=.nil, .bsf~bsf.null_string, args[i])
      code=code ", a."i
   end
*/

  objName=self~objectName
  code = 'res=BSF( "invoke", "'||objName||'",'  '"' || name || '"'

  -- if adressing a RexxProxy using a variant of "sendMessage" make sure it uses this instance
  -- of the RexxEngine and the RexxAndJava interface, otherwise in multithreaded execution environments
  -- problems may occur, as a RexxProxy by default would use the RexxEngine that created it originally
  if objName~abbrev("org.rexxla.bsf.engines.rexx.RexxProxy@"), -
     "SENDMESSAGE0 SENDMESSAGE1 SENDMESSAGE2 SENDMESSAGE3 SENDMESSAGE4 SENDMESSAGE5 SENDMESSAGE"~caselessWordPos(name)>0 then
  do
      code=code ', "' || bsf('getRexxAndJava')~substr(4) || '"'      -- make sure this call
-- say "///---/// BSF.CLS - a RexxProxy, adapted the code fragment to -->" pp(code) "\\\---\\\"
  end

  do i=1 to args~items
     a.i=choose(args[i]=.nil, .bsf~bsf.null_string, args[i])
     code=code ", a."i
  end


  interpret code ")"       -- execute this dynamically created Rexx string
  return bsf.wrap(res)


any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message

  raise propagate  -- raise the exception in caller/invoker on the Rexx side




/* -------------------------------------------------------------
   the following methods implement the individual BSF-calls as
   methods, allowing for invoking them in the OO style with the
   help of messages;

   "registerBean" is not made available explicitly, it is
   implicitly part of the creation of a new proxy object (see
   "INIT" method above)

   "unregisterBean" is not made available explicitly, it is
   implicitly part of the destruction of objects (see the
   "UNINIT" methods above)

   "getStaticValue" is not made available to instances as they
   are able to get (and even set) public (static) fields via
   "getFieldValue" resp. "setFieldValue"
   ------------------------------------------------------------- */
--   instance methods

/* 2010-05-08, rgf: just send "getClass" to Java object instead
::METHOD bsf.class      -- method to return the BSF Java class object proxy of this Java object proxy
   PARSE VALUE self~objectname WITH java_path "@" .
   signal on syntax name any
   return .bsf~bsf.getJavaClassObjectProxy(java_path)

any:
  raise propagate  -- raise the exception in caller/invoker on the Rexx side
*/


::METHOD bsf.addEventListener  /* add event listener to the Java object*/
   use arg eventSetName, filter, text

   signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
   return BSF( "addEventListener", self~objectname, eventSetName, filter, text )~substr(4)

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate


/* rgf, 2009-06-08
::METHOD bsf.addEventListenerReturningEventInfos /* add event listener to the Java object */
   use arg eventSetName, filter, text, sendBackData

   signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
   if arg()=4 then
      return BSF( "addEventListenerReturningEventInfos", self~objectname, eventSetName, filter, text, sendBackData  )~substr(4)

   return BSF( "addEventListenerReturningEventInfos", self~objectname, eventSetName, filter, text)~substr(4)

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate
*/



::METHOD bsf.exit              /* exit Rexx                    */
   FORWARD MESSAGE ("BSF.EXIT") TO (self~class)

::METHOD bsf.invoke
   parse arg methodName
   -- create UNKNOWN format (first argument is unknown method name)
   return self~unknown(methodName, arg(2, 'A'))


::METHOD bsf.invokeStrict
  USE ARG name          -- name of method to be invoked
  args=arg(2, 'A')

   code = 'call BSF "invokeStrict", "' || self~objectname || '", "' || name || '"'
   do i=1 to args~items
      a.i=choose(args[i]=.nil, .bsf~bsf.null_string, args[i])
      code=code ", a."i
   end

  signal on syntax name any         -- trap any exceptions
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  interpret code        -- execute this dynamically created Rexx string
  if var("RESULT") then return bsf.wrap(result)   -- check whether primitive type or Java object
                   else return .nil                      -- nothing came back, i.e. Java returned a true "null"

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


::METHOD bsf.getFieldValue
   parse arg fieldName
   signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
   return bsf.wrap( bsf("getFieldValue", self~objectname, fieldName) )

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

::METHOD bsf.getFieldValueStrict -- case-sensitive fieldName !
   parse arg fieldName
   signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  if arg()>1 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
     raise syntax 40.900 array ("BSF.CLS/class BSF/method bsf.getFieldValueStrict, error 1: too many arguments (maximum: 1, found: "arg()")")

   return bsf.wrap( bsf("getFieldValueStrict", self~objectname, fieldName) )

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


::METHOD bsf.setFieldValue
   parse arg fieldName

   code="res=bsf('setFieldValue', '" || self~objectname || "', '" || fieldName || "'" ,
                 ", arg(2) )"

   signal on syntax name any        -- trap any exceptions
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
   interpret code
   return bsf.wrap(res)

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


::METHOD bsf.setFieldValueStrict
  parse arg fieldName

  code="res=bsf('setFieldValueStrict', '" || self~objectname || "', '" || fieldName || "'" ,
                ", arg(2)" choose(arg()>2, ", arg(3)", "") ")"

  signal on syntax name any        -- trap any exceptions
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  interpret code
  return bsf.wrap(res)

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


::METHOD bsf.getPropertyValue
  parse arg propertyName, index
  if arg(2, "Omitted") then index = "" -- no index given, set to empty string
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( bsf("getPropertyValue", self~objectname, propertyName, index))

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


::METHOD bsf.setPropertyValue
  parse arg propertyName, index
  if arg(2, "Omitted") then index = "" -- no index given, set to empty string

  code="res=bsf('setPropertyValue', '" || self~objectname || "', '" || propertyName || "', '" || index || "'"

  tmpArr=arg(3, 'Array')
  do i=1 to tmpArr~items
     a.i=choose(tmpArr[i]=.nil, .bsf~bsf.null_string, tmpArr[i])
     code=code ", a."i
  end
  code=code ")"

  signal on syntax name any         -- trap any exceptions
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  interpret code
  return bsf.wrap(res)

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


::METHOD bsf.setPropertyValueStrict
  parse arg propertyName, index
  if arg(2, "Omitted") then index = "" -- no index given, set to empty string

  code="res=bsf('setPropertyValueStrict', '" || self~objectname || "', '" || propertyName || "', '" ||,
                index || "'"
  tmpArr=arg(3, 'Array')
  do i=1 to tmpArr~items
     a.i=choose(tmpArr[i]=.nil, .bsf~bsf.null_string, tmpArr[i])
     code=code ", a."i
  end
  code=code ") "

  signal on syntax name any         -- trap any exceptions
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  interpret code
  return bsf.wrap(res)

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side




-- rgf, 2010-02-20
::METHOD bsf.isA
  -- use strict arg javaClass
  parse arg javaClass

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( bsf("isA", self~objectName, javaClass))

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side





--   class  methods

/* -------------------------------------------------------------
   class method shuts down the Java Virtual Machine (JVM), which started
   this instance of Rexx, this causes Rexx to be shut down as well; the
   Java side waits "waitTime" msecs before terminating the JVM
   ------------------------------------------------------------- */
::METHOD bsf.exit class
  use arg exitValue, waitTime

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if \datatype(exitValue, "Whole Number") then return BSF("exit")~substr(4)
  if \datatype(waitTime,  "W"           ) then return BSF("exit", exitValue)~substr(4)
  return BSF("exit", exitValue, waitTime)~substr(4)

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

/* -------------------------------------------------------------
   class method to have Java sleep for us "sleepTime" seconds; resolution
   to a msec is calculated, although the Java side may deviate up to 15/100 secs
   (we are not talking realtime here ;)
   ------------------------------------------------------------- */
::METHOD bsf.sleep class
  use arg sleepTime
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if arg()>1 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
     raise syntax 40.900 array ("BSF.CLS/class BSF/class method bsf.sleep, error 1: too many arguments (maximum: 1, found: "arg()")")

  return BSF("sleep", sleepTime)~substr(4)

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

::METHOD     bsf.loadClass                 class
  parse arg JavaClassName
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( bsf("loadClass", JavaClassName) )

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

::METHOD     bsf.lookupBean                class
  parse arg beanName
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( bsf("lookupBean", beanName) )

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

::METHOD     bsf.pollEventText             class
  parse arg timeOut

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if arg()>1 then    -- 2009-06-27, hint by Mr. Lex (author of testunits for BSF4Rexx)
     raise syntax 40.900 array ("BSF.CLS/class BSF/class method bsf.pollEventText, error 1: too many arguments (maximum: 1, found: "arg()")")

  if \datatype(timeOut, "Whole") then return bsf.wrap(bsf("pollEventText"))
  return bsf.wrap(bsf("pollEventText", timeout))    -- with timeout

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

::METHOD     bsf.getStaticValue            class
  parse arg className, fieldName
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( bsf("getStaticValue", className, fieldName))

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

::METHOD     bsf.getStaticValueStrict      class
  parse arg className, fieldName
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( bsf("getStaticValueStrict", className, fieldName))

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

::METHOD     bsf.postEventText              class
  parse arg eventText, priority
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  if arg()>2 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
     raise syntax 40.900 array ("BSF.CLS/class BSF/class method bsf.postEventText, error 1: too many arguments (maximum: 2, found: "arg()")")

  if arg()=1 then return bsf.wrap(bsf("postEventText", eventText))
  return bsf.wrap(bsf("postEventText", eventText, priority))

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

::METHOD     bsf.wrapArray                  unguarded class
  parse arg beanName
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( bsf("wrapArray", beanName) )

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


::METHOD     bsf.createArray                class
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

/* 2010-05-08, rgf
  co=arg(1)      -- default to first argument
  if co~hasmethod("BSF.FULLJAVACLASSNAME") then
  do
     co=.bsf~bsf.getJavaClassObjectProxy(co~bsf.FullJavaClassName)
  end

  tmp="return bsf.wrap(bsf('createArray' , '"co"'"
  do i=2 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
*/

  tmp="return bsf.wrap(bsf('createArray' "
  do i=1 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end

  tmp=tmp"))"

  interpret tmp     -- now carry out the Rexx statement

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* Create single dimensioned Java array, taking the first argument as componentType
   (either a Java class object or as a fully qualified Java class name). All (optional)
   following arguments are being taking
*/
::METHOD     bsf.createArrayOf              class  -- rgf, 2009-07-30
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  tmp="return bsf.wrap(bsf('createArrayOf'"

/* 2010-05-08, rgf
  if arg()>0 then
  do
     co=arg(1)      -- default to first argument
     if co~hasmethod("BSF.FULLJAVACLASSNAME") then
     do
        co=.bsf~bsf.getJavaClassObjectProxy(co~bsf.FullJavaClassName)
     end
     tmp=tmp || ", '"co"'"
  end

  do i=2 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
*/
  do i=1 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
  tmp=tmp"))"

  interpret tmp     -- now carry out the Rexx statement

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


::METHOD     bsf.createJavaArray                class          -- rgf, 2009-09-10
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

/* 2010-05-08, rgf
  co=arg(1)      -- default to first argument
  if co~hasmethod("BSF.FULLJAVACLASSNAME") then
  do
     co=.bsf~bsf.getJavaClassObjectProxy(co~bsf.FullJavaClassName)
  end

  tmp="return bsf.wrap(bsf('createJavaArray' , '"co"'"
  do i=2 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
*/
  tmp="return bsf.wrap(bsf('createJavaArray' "
  do i=1 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end

  tmp=tmp"))"

  interpret tmp     -- now carry out the Rexx statement

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* Create single dimensioned Java array, taking the first argument as componentType
   (either a Java class object or as a fully qualified Java class name). All (optional)
   following arguments are being taking
*/
::METHOD     bsf.createJavaArrayOf              class  -- rgf, 2009-09-10
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  tmp="return bsf.wrap(bsf('createJavaArrayOf'"
/* 2010-05-08, rgf
  if arg()>0 then
  do
     co=arg(1)      -- default to first argument
     if co~hasmethod("BSF.FULLJAVACLASSNAME") then
     do
        co=.bsf~bsf.getJavaClassObjectProxy(co~bsf.FullJavaClassName)
     end
     tmp=tmp || ", '"co"'"
  end

  do i=2 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
*/
  do i=1 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
  tmp=tmp"))"

  interpret tmp     -- now carry out the Rexx statement

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side




::METHOD     bsf.wrapEnumeration            class
  parse arg beanName
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( bsf("wrapEnumeration", beanName) )

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

   -- set the string which from now on represents the Java "null" on both sides,
   -- the Rexx and the Java side
::METHOD     bsf.setRexxNullString         class
  parse arg newValue
  .bsf~bsf.null_string = newValue                  -- set class attribute to the new value
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if arg()>1 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
     raise syntax 40.900 array ("BSF.CLS/class BSF/class method bsf.setRexxNullString, error 1: too many arguments (maximum: 1, found: "arg()")")


  return bsf.wrap(bsf("setRexxNullString", newValue))    -- set the Java side to the new value

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



::METHOD     bsf.haltThread class
  use strict arg tid

  signal on syntax name any
  return bsf.wrap(bsf("haltThread", tid)) -- set the Java side to the new value

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


::METHOD    bsf.wrap          unguarded class    -- 20090515: make routine BSF.WRAP() indirectly availabe (possible 4.0 API Problem)
  use arg obj
  return bsf.wrap(obj)



/* *************************************************************
   Class:       BSF_REFERENCE
   Purpose:     the proxy class for objects returned by the Java side;
                objects of this class, if destroyed, cause via its superclass
                to remove one from the appropriate instance counter of the
                BSF registry
   Restriction: Should only be used by classes/routines in this file

   ************************************************************* */
::CLASS BSF_REFERENCE subclass BSF

/* -------------------------------------------------------------
   method to construct an instance (a "bean"); as the object was returned
   from the Java side, just remember its name, but do not call BSF's init
   again! This proxy object merely allows for sending messages through it
   to the Java object as well. If it goes out of scope, it will be garbage
   collected by Object Rexx which in turn deregisters the object from the
   Java side, decrementing the reference counter there
   ------------------------------------------------------------- */
::METHOD init
   use arg beanName
   self~ObjectName=beanName
-- .error~say( "BSF.CLS: BSF_REFERENCE::INIT():" pp(beanName)"@"pp(self~identityHash) )

/*
::method copy
say "BSF.CLS: BSF_REFERENCE::COPY():" pp(beanName)"@"pp(self~identityHash) "<=== <== <=== !!! !!! !!!"
forward class (super)
*/



/* *************************************************************
   Class:       BSF_PROXY
   Purpose:     allows to wrap up Java objects as Object Rexx objects;
                if instances are destroyed, then no derigistration from
                the BSF registry takes place
   restriction: no check is done, whether the given beanName is really
                in the BSF-registry
   ************************************************************* */
::CLASS BSF_PROXY subclass BSF PUBLIC

/* -------------------------------------------------------------
   method to construct an instance (a "bean"); as the object was returned
   from the Java side, just remember its name, but do not call BSF's init
   again! This proxy object merely allows for sending messages through it
   to the Java object as well. If it goes out of scope, it will be garbage
   collected by Object Rexx which will *not* unregister the object from the
   Java side, decrementing the reference counter there
   ------------------------------------------------------------- */
::METHOD init
   use arg beanName
   self~ObjectName=beanName

   -- make sure that BSF's UNINIT method does not run (it'll deregister
   -- the bean from the BSF registry
::METHOD uninit

-- .error~say( "*** BSF.CLS::BSF_PROXY::UNINIT: NOP !" pp(self~objectname)"@"pp(self~identityHash))

  NOP




/* *************************************************************
   Class:       BSF_ARRAY
   Purpose:     the proxy class for array objects returned by the Java side
   ************************************************************* */
::CLASS BSF_ARRAY_REFERENCE     subclass BSF

/* -------------------------------------------------------------
   method to construct an instance (a "bean"); as the object was returned
   from the Java side, just remember its name, but do not call BSF's init
   again! This proxy object merely allows for sending messages through it
   to the Java object as well. If it goes out of scope, it will be garbage
   collected by Object Rexx which in turn unregisters the object from the
   Java side, decrementing the reference counter there
   ------------------------------------------------------------- */
::METHOD init
  expose items dimension indexArray   -- set access to object variable
  use arg beanName, dimension  -- assign "dimension" directly to exposed object variable

-- .error~say( "BSF.CLS: BSF_ARRAY_REFERENCE::INIT():" pp(beanName)"@"pp(self~identityHash) )

  self~ObjectName=beanName
  items=-1                     -- set and indicate, that not calculated as of yet

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
     -- "indexArray" allows for indexing at's and put's with a Java int array
     -- use exact signature

   -- set up "indexArray" attribute to match dimension and capacity of the base Java array
   -- wrapping up occurs in the statement after the next, hence no bsf.wrap() !
  -- bsf4rexx=.bsf~dir          -- 20080828, rgf
  indexArray=bsf("invokeStrict", .bsf4rexx~array.class, -
                                 "newInstance", "o",    -
                                 .bsf4rexx~int, "int", dimension)~substr(4)
  indexArray=.bsf_array_proxy~new(indexArray, 1, dimension)
  return

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


::METHOD dimension
  expose dimension dimensions

  if arg(1, "Omitted") then return dimension    -- return number of dimensions

  if VAR( dimensions ) = .false then self~getData  -- no data available, get it
  return dimensions[arg(1)]                     -- return entries in this dimension



      -- get the needed information from ArrayWrapper, make it available with Object Rexx attributes
::METHOD getData private
  expose dimensions items

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
      -- get the array wrapper of this array
  aw    =bsf.wrap(bsf('wrapArray', self~objectname))
  items =bsf('getFieldValue', aw, 'items') ~substr(4)       -- get calculated 'items' value
  dimArr=bsf.wrap(bsf('getFieldValue', aw, 'sizeOfDimension')) -- get the Java array containing the dimensions

  dimensions=.array~new                         -- create an Object Rexx array
  do i=1 to bsf('arrayLength', dimArr)~substr(4)          -- get length of 'dimensions' array
     dimensions[i]=bsf.wrap(bsf('arrayAt', dimArr, i-1))  -- use Java indexing (starting with '0')
  end
  return

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


  -- Note: This array-proxy is "1"-based (not 0-based as Java), because Object Rexx arrays
  -- are "1", based, therefore subscript adjustments are necessary, before calling the Java side
  -- The indices can be given individually in comma-separated form or in form of a Rexx array
::METHOD at
   expose indexArray  dimension
   -- use arg arg1
   use arg tmpArr

-- .error~say( "BSF.CLS: BSF_ARRAY_REFERENCE::AT():" pp(self~objectName)"@"pp(self~identityHash) "arg="pp(arg1) "indexArray="pp(indexArray))

   signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old


   if tmpArr~class<>.array then  -- if second argument is not an array, create one
      tmpArr=arg(1, "Array")     -- get arguments as an array

-- say "tmpArr~items:" tmpArr~items "<> dimension:" dimension
   if tmpArr~items<>dimension then  -- wrong number of subscripts ?
   do
      signal on syntax name syntax
      if tmpArr~items<dimension then            -- too few
          raise syntax 93.925 array (dimension)
      else                                      -- too many
          raise syntax 93.926 array (dimension)
   end

   do i=1 to tmpArr~items        -- adjust indices to Java-style (0-based indices)
      -- call bsf "arrayPutStrict", indexArray, "int", tmpArr[i]-1, i-1
      call bsf "arrayPut", indexArray, tmpArr[i]-1, i-1
   end


   return bsf.wrap( bsf('arrayAt', self~objectName, indexArray))

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

syntax: raise propagate /* propagate dimension/subscript exception */


  -- allow for referring to array elements as if it was an Object Rexx array
::METHOD "[]"
  FORWARD MESSAGE ("AT")        -- synonym for "AT", use its implementation



  -- Note: This array-proxy is "1"-based (not 0-based as Java), because Object Rexx arrays
  -- are "1", based, therefore subscript adjustments are necessary, before calling the Java side
  -- The indices can be given individually in comma-separated form or in form of a Rexx array
::METHOD put
  expose indexArray dimension
  use arg value, tmpArr

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if value=.nil then value=.bsf~bsf.null_string -- indicate to the Java side a "null"

  if tmpArr~class<>.array then  -- if second argument is not an array, create one
     tmpArr=arg(2, "Array")     -- get rest of arguments as an array

  if tmpArr~items<>dimension then  -- wrong number of subscripts ?
  do
     signal on syntax name syntax
     if tmpArr~items<dimension then            -- too few
         raise syntax 93.925 array (dimension)
     else                                      -- too many
         raise syntax 93.926 array (dimension)
  end

  do i=1 to tmpArr~items        -- adjust indices to Java-style (0-based indices)
     -- call bsf "arrayPutStrict", indexArray, "int", tmpArr[i]-1, i-1
     call bsf "arrayPut", indexArray, tmpArr[i]-1, i-1
  end

  call bsf 'arrayPut', self~objectName, value, indexArray
  return

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

syntax: raise propagate /* propagate dimension/subscript exception */


  -- Note: This array-proxy is "1"-based (not 0-based as Java), because Object Rexx arrays
  -- are "1", based, therefore subscript adjustments are necessary, before calling the Java side
  -- The indices can be given individually in comma-separated form or in form of a Rexx array
::METHOD putStrict
  expose indexArray dimension
  use arg type, value, tmpArr

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if value=.nil then value=.bsf~bsf.null_string -- indicate to the Java side a "null"

  if tmpArr~class<>.array then  -- if second argument is not an array, create one
     tmpArr=arg(3, "Array")     -- get rest of arguments as an array

  if tmpArr~items<>dimension then  -- wrong number of subscripts ?
  do
     signal on syntax name syntax
     if tmpArr~items<dimension then            -- too few
         raise syntax 93.925 array (dimension)
     else                                      -- too many
         raise syntax 93.926 array (dimension)
  end

  do i=1 to tmpArr~items        -- adjust indices to Java-style (0-based indices)
     call bsf "arrayPut", indexArray, tmpArr[i]-1, i-1
  end

  call bsf 'arrayPutStrict', self~objectName, type, value, indexArray
  return

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


syntax: raise propagate /* propagate dimension/subscript exception */


  -- allow for referring to array elements as if it was an Object Rexx array;
  -- using this syntax variant has the following implication: the valueType and the
  -- value itself must be given as *one* string value; the first word is the valueType
  -- the rest the value!    e.g.: xyz[1, 2, 3] = "String Hi, there!"
::METHOD "[]="
  FORWARD MESSAGE ("PUT") -- let the PUT-method do the work, 2003-05-10

  /* 2003-05-10, ---rgf
  -- synonym for "PUT", use its implementation
  expose dimension
  parse arg type value, .  -- for this variant extract the type from the rest of the value

  tmpArr=arg(2, "Array")   -- get the rest of the arguments in form of an array
  tmpArrItems=tmpArr~items
  newArr=.array~new
  newArr[1]=type           -- save type information
  newArr[2]=value          -- save value

  do i=3 to 9999           -- assign the index-arguments
     j=i-2
     if j>tmpArrItems then leave
     newArr[i]=tmpArr[j]
  end
  FORWARD MESSAGE ("PUT") ARGUMENTS (newArr) -- let the PUT-method do the work
  */


-- calculates (once) and returns number of individual elements in array
::METHOD items
  expose items  -- set access to object variable
  if items=-1 then self~getData     -- not yet set, get the data from the ArrayWrapper
  return items

-- Java arrays have a fixed size and their storage is pre-allocated: return same value as method items
::METHOD size
  return self~items



-- this method creates a supplier method
::METHOD  SUPPLIER

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  aw=bsf.wrap(bsf('wrapArray', self~objectname))
   -- wrap up the Supplier object, such that it will be kept in the BSF registry as long
   -- as the Object Rexx BSF proxy exists
  return bsf.wrap( bsf('invoke', aw, 'supplier', .true) )

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



-- this method is needed by "DO ... OVER"
::METHOD MAKEARRAY

-- .error~say( "BSF.CLS: BSF_ARRAY_REFERENCE::MAKEARRAY():" pp(self~objectName)"@"pp(self~identityHash))

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  aw=bsf.wrap(bsf('wrapArray', self~objectname) )   -- wrap up the array
  arr=bsf.wrap(bsf('invoke', aw, 'makearray', .false, .false)) -- get
  arr=bsf.wrap(bsf('arrayAt', arr, 0))              -- get the appropriate Java array

  rexxArr=.array~new
  do i=1 to bsf('arrayLength', arr)~substr(4)         -- get length of 'dimensions' array
     -- rexxArr[i]=bsf('arrayAt', arr, i-1)  -- use Java indexing (starting with '0')
     rexxArr[i]=bsf.wrap(bsf('arrayAt', arr, i-1))  -- use Java indexing (starting with '0')
  end
  return rexxArr

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side




   -- wraps the given Java object and allows for using the array methods on it;
   -- if the reference is not used anymore, it will *not* cause the Java object to be de-registered
   -- used in the initialisation of "BSF_ARRAY" to get primitive int array (for the dimensions) without initialising them as standalone BSF-arrays
   --
::CLASS BSF_ARRAY_PROXY SUBCLASS BSF_ARRAY_REFERENCE
::METHOD init
  use arg beanName, dimension, items         -- name of Java object as registered in the BSF registry

  self~objectName=beanName
  self~dimension =dimension
  self~items     =items

::method dimension attribute
::method items     attribute

::METHOD uninit            -- intercept destructor message, don't remove BSF registry entry
-- .error~say( "*** BSF.CLS::BSF_ARRAY_PROXY::UNINIT: NOP !" pp(self~objectname)"@"pp(self~identityHash) )

  NOP




::routine bsf.import                         public
   USE ARG java_path, className4LocalDir

   signal on syntax
   if arg()>2 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
      raise syntax 40.900 array ("BSF.CLS/routine/bsf.import(), error 1: too many arguments (maximum: 2, found: "arg()")")

   if arg()=1 then return .bsf~bsf.import(java_path)
   return .bsf~bsf.import(java_path, className4LocalDir)

syntax:
   raise propagate

::routine bsf.importClass                    public
   USE ARG java_path, className4LocalDir

   signal on syntax
   if arg()>2 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
      raise syntax 40.900 array ("BSF.CLS/routine/bsf.import(), error 1: too many arguments (maximum: 2, found: "arg()")")

   if arg()=1 then return .bsf~bsf.importClass(java_path)
   return .bsf~bsf.import(java_path, className4LocalDir)

syntax:
   raise propagate


::routine bsf.loadClass                      public
   parse arg JavaClassName
   return bsf.wrap( bsf("loadClass", JavaClassName) )



::routine bsf.lookupBean                     public
   parse arg beanName
   return bsf.wrap( bsf("lookupBean", beanName) )

::routine bsf.unregisterBean                 public
  parse arg beanName
  return bsf("unregisterBean", beanName)~substr(4)

::routine bsf.pollEventText                  public
   parse arg timeOut
   signal on syntax
   if arg()>1 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
      raise syntax 40.900 array ("BSF.CLS/routine/bsf.pollEventText(), error 1: too many arguments (maximum: 1, found: "arg()")")

   if \datatype(timeOut, "Whole") then return bsf.wrap( bsf("pollEventText"))
   return bsf.wrap( bsf("pollEventText", timeout))    -- with timeout

syntax:
   raise propagate


::routine bsf.postEventText                  public
   parse arg eventText, priority

   signal on syntax
   if arg()>2 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
      raise syntax 40.900 array ("BSF.CLS/routine/bsf.postEventText(), error 1: too many arguments (maximum: 2, found: "arg()")")

   if arg()=1 then return bsf.wrap(bsf("postEventText", eventText))
   return bsf.wrap(bsf("postEventText", eventText, priority))

syntax:
   raise propagate


::routine bsf.getStaticValue                 public
   parse arg className, fieldName
   return bsf.wrap( bsf("getStaticValue", className, fieldName))

::routine bsf.getStaticValueStrict           public
   parse arg className, fieldName
   return bsf.wrap( bsf("getStaticValueStrict", className, fieldName))

::routine bsf.getConstant                    public
  parse arg className, fieldName
  signal on syntax name any
  return bsf.wrap( bsf("getStaticValueStrict", className, fieldName))
any: raise propagate





::ROUTINE bsf.createArray                    public
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

/* 2010-05-08, rgf
  co=arg(1)      -- default to first argument
  if co~hasmethod("BSF.FULLJAVACLASSNAME") then
  do
     co=.bsf~bsf.getJavaClassObjectProxy(co~bsf.FullJavaClassName)
  end

  tmp="return bsf.wrap(bsf('createArray' , '"co"'"
  do i=2 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
*/

  tmp="return bsf.wrap(bsf('createArray' "
  do i=1 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end

  tmp=tmp"))"

  interpret tmp     -- now carry out the Rexx statement

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* Create single dimensioned Java array, taking the first argument as componentType
   (either a Java class object or as a fully qualified Java class name). All (optional)
   following arguments are being taking
*/
::ROUTINE bsf.createArrayOf                  public -- rgf, 2009-07-30
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  tmp="return bsf.wrap(bsf('createArrayOf'"

/* 2010-05-08, rgf
  if arg()>0 then
  do
     co=arg(1)      -- default to first argument
     if co~hasmethod("BSF.FULLJAVACLASSNAME") then
     do
        co=.bsf~bsf.getJavaClassObjectProxy(co~bsf.FullJavaClassName)
     end
     tmp=tmp || ", '"co"'"
  end

  do i=2 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
*/
  do i=1 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
  tmp=tmp"))"

  interpret tmp     -- now carry out the Rexx statement

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


::ROUTINE bsf.createJavaArray                public         -- rgf, 2009-09-10
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

/* 2010-05-08, rgf
  co=arg(1)      -- default to first argument
  if co~hasmethod("BSF.FULLJAVACLASSNAME") then
  do
     co=.bsf~bsf.getJavaClassObjectProxy(co~bsf.FullJavaClassName)
  end

  tmp="return bsf.wrap(bsf('createJavaArray' , '"co"'"
  do i=2 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
*/
  tmp="return bsf.wrap(bsf('createJavaArray' "
  do i=1 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end

  tmp=tmp"))"

  interpret tmp     -- now carry out the Rexx statement

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* Create single dimensioned Java array, taking the first argument as componentType
   (either a Java class object or as a fully qualified Java class name). All (optional)
   following arguments are being taking
*/
::ROUTINE bsf.createJavaArrayOf              public -- rgf, 2009-09-10
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  tmp="return bsf.wrap(bsf('createJavaArrayOf'"
/* 2010-05-08, rgf
  if arg()>0 then
  do
     co=arg(1)      -- default to first argument
     if co~hasmethod("BSF.FULLJAVACLASSNAME") then
     do
        co=.bsf~bsf.getJavaClassObjectProxy(co~bsf.FullJavaClassName)
     end
     tmp=tmp || ", '"co"'"
  end

  do i=2 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
*/
  do i=1 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
  tmp=tmp"))"

  interpret tmp     -- now carry out the Rexx statement

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side




/* 2010-05-08, rgf
::routine bsf.createArray                    public
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  co=arg(1)      -- default to first argument
-- say "bsf.createArray(): co="pp(co)
  if co~hasmethod("BSF.FULLJAVACLASSNAME") then
  do
     co=.bsf~bsf.getJavaClassObjectProxy(arg(1)~bsf.FullJavaClassName)
-- say "bsf.createArray(): co="pp(co)  "AFTER----"
  end

  tmp="return bsf.wrap(bsf('createArray' , '"co"'"

  do i=2 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
  tmp=tmp"))"

  interpret tmp     -- now carry out the Rexx statement
  return

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* Create single dimensioned Java array, taking the first argument as componentType
   (either a Java class object or as a fully qualified Java class name). All (optional)
   following arguments are being taking
*/
::routine bsf.createArrayOf               public   -- rgf, 2009-07-30
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  tmp="return bsf.wrap(bsf('createArrayOf'"
  if arg()>0 then
  do
     co=arg(1)      -- default to first argument
     if co~hasmethod("BSF.FULLJAVACLASSNAME") then
     do
        co=.bsf~bsf.getJavaClassObjectProxy(co~bsf.FullJavaClassName)
     end
     tmp=tmp || ", '"co"'"
  end

  do i=2 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
  tmp=tmp"))"

  interpret tmp     -- now carry out the Rexx statement

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


::routine bsf.createJavaArray                    public     -- rgf, 2009-09-10
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  co=arg(1)      -- default to first argument
  if co~hasmethod("BSF.FULLJAVACLASSNAME") then
  do
     co=.bsf~bsf.getJavaClassObjectProxy(arg(1)~bsf.FullJavaClassName)
  end

  tmp="return bsf.wrap(bsf('createJavaArray' , '"co"'"

  do i=2 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
  tmp=tmp"))"

  interpret tmp     -- now carry out the Rexx statement
  return

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* Create single dimensioned Java array, taking the first argument as componentType
   (either a Java class object or as a fully qualified Java class name). All (optional)
   following arguments are being taking
*/
::routine bsf.createJavaArrayOf               public   -- rgf, 2009-09-10
  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  tmp="return bsf.wrap(bsf('createJavaArrayOf'"
  if arg()>0 then
  do
     co=arg(1)      -- default to first argument
     if co~hasmethod("BSF.FULLJAVACLASSNAME") then
     do
        co=.bsf~bsf.getJavaClassObjectProxy(co~bsf.FullJavaClassName)
     end
     tmp=tmp || ", '"co"'"
  end

  do i=2 to arg()
     tmp=tmp ", """ || arg(i) || """"
  end
  tmp=tmp"))"

  interpret tmp     -- now carry out the Rexx statement

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side
*/



/* Routine for subfunction "version", rgf, 2008-07-09 */
::routine bsf.version public
   use strict arg kind=""

   signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if kind="" then
     return BSF( "version")~substr(4)
  else
     return BSF( "version", kind)~substr(4)

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate



/* Routine for subfunction "haltThread", rgf, 2009-04-04 */
::routine bsf.haltThread public
   use strict arg tid

   signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return BSF("haltThread", tid)~substr(4) -- remove type indicator

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate




/* 2010-02-20: allow testing whether a Java object is an instance of a given
               Java class (either the class object or a string); returns
               .true (1) or .false (0)
*/
::routine bsf.isA public
  use strict arg javaObject, javaClass

   signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return BSF("isA", javaObject, javaClass)~substr(4)   -- remove type indicator

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate

/* 2010-05-11: extract and return slotDir argument, which is always the last argument in
               the supplied arg-array; this way it is easy to get the slotDir back in
               a one liner
*/
::routine bsf.getSlotDir public
  use strict arg argArray

  signal on syntax name any
/*
  if \argArray~isA(.array) then
     raise syntax 88.914 array ("argumentArray", ".Array")

  if argArray~items=0 then
     raise syntax 88.900 array ('"argumentArray" is empty')
*/
  return argArray~at(argArray~last)   -- slotDir is always last item in argument array

/*
  slotDir=argArray~at(argArray~last)   -- slotDir is always last item in argument array
  if \slotDir~isA(.directory) then
     raise syntax 88.900 array ('Last item in "argumentArray" must be a directory, found: "'slotDir~string'"')
  return slotDir
*/

any:
  raise propagate



   /* Create and return an Object Rexx directory representing all static fields of the
    * given Java class or Java interface.
   */
::routine bsf.wrapStaticFields public
  parse arg JavaClassName, bNoCache
  return .JavaStaticFields.Directory~new(JavaClassName, bNoCache)

/*  This allows to wrap any Java class or interface as a directory of fields. This way
    Java field constants can be easily addressed/used via BSF. Any looked up Java constant
    field will be stored in the directory, speeding up future look-ups.
*/
::class  "JavaStaticFields.Directory"  subclass directory
::method JavaClassName   attribute -- memorize name of Java class
::method JavaClassObject attribute -- memorize Java class object
::method bUseCache       attribute -- memorize Java class object
::method init
  parse arg JavaClassName, bNoCache

  self~bUseCache= (bNoCache<>.false)    -- default to use ooRexx directory as cache

  if .local~hasentry(JavaClassName) then        -- if Java class exists already in the local environment, use it
     self~JavaClassObject=.local~entry(JavaClassName)
  else                                          -- load Java class and save the class object (string reference)
     self~JavaClassObject=bsf.loadClass(JavaClassName)

  self~JavaClassName=JavaClassName        -- save the Java class name


  -- this method will be used to deal with UNKNOWN messages; it will retrieve the unknown message name
  -- as a fieldName into the Java class object stored with this class
::method unknown private
  expose JavaClassObject bUseCache
  parse arg fieldName .

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  isPutMessage=(right(fieldName,1)='=')
  if isPutMessage then              -- put message
  do
     parse arg fieldName '='        -- get fieldName only
     newVal=arg(2)[1]               -- second argument (an array), first entry
     val=bsf.wrap(bsf("setFieldValue", JavaClassObject, fieldName, newVal))
     self~setentry(fieldName, newVal)  -- save the value in the directory (= update cache)
     return newVal                  -- return the looked up value
  end
  else   -- look up the Java class for the appropriate field value;
  do
     if bUseCache & self~hasEntry(fieldName) then  -- already in directory, if so return cached value
     do
        return self~entry(fieldName)
     end

         -- not yet cached, save it in directory object
     val=bsf.wrap(bsf("getStaticValue", JavaClassObject, fieldName))
     self~setentry(fieldName, val)  -- save the value in the directory
     return val                     -- return the looked up value
  end
  return

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



::method "[]"                       -- getter method
  forward message ("AT")

::method AT                         -- get static field, honor case of letters
  expose JavaClassObject bUseCache
  parse arg fieldName

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if bUseCache & self~hasindex(fieldName) then  -- already in directory, if so return cached value
  do
     return self~at:super(fieldName)
  end
      -- not yet cached, save it in directory object
  val=bsf.wrap(bsf("getStaticValueStrict", JavaClassObject, fieldName))
  self~put:super(val, fieldName)    -- save the value in the directory

  return val                        -- return the looked up value

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



::method "[]="                      -- setter method
  forward message ("PUT")


::method entry                      -- getter method, use uppercased name as index
  parse upper arg index
  forward message ("AT") array (index)


::method PUT                        -- set static field, honor case of letters
  expose JavaClassObject
  use arg newVal, fieldName

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  val=bsf.wrap(bsf("setFieldValueStrict", JavaClassObject, fieldName, newVal))
  self~put:super(newVal, fieldName) -- save the value in the directory (= update cache)
  return newVal                     -- return the looked up value

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side





   /* cheap man's "pretty print": encloses string value in square brackets */
::routine pp   public
  return "[" || arg(1)~string || "]"


   /* VBasic-like iif() function: if arg(1) is .true, then return arg(2), else arg(3)  */
::routine iif  public
  if arg(1)=.true then return arg(2)
  return arg(3)



/* rgf, 2009-06-08
   /*
   *  Retrieves the BSFRegistry key from a passed eventText string and returns the
   *  Java array object proxy, or .nil, if no such object can be retrieved.
   *
   *  This routine must be called only once per eventText containing a BSF bean reference key!
   */
::routine bsf.getEventInfoObject public
  parse arg "/*BSFBEAN/*" BSFRegistryKey "*/*/"       -- parse BSFRegistryKey

  obj=bsf.wrap(BSFRegistryKey)
  if obj~class=.bsf_array_reference then              -- could an array object be retrieved ?
  do
     return .bsf_array_proxy.assure_BSFRegistry_Removal~new(obj)  -- return monitor object
  end
  return .nil     -- indicate that no event object could be found



   -- private class: makes sure that Java object is removed from BSFRegistry
::class bsf_array_proxy.assure_BSFRegistry_Removal -- subclass BSF_ARRAY_PROXY


::method init
  expose obj
  obj=arg(1)            -- save object in attribute

::method obj attribute  -- attribute to store object


::method unknown        -- forward everything to BSF_ARRAY_PROXY
  expose obj
  use arg msg, args

  forward to (obj) message (msg) arguments (args)  -- forward message to obj


::method uninit         -- make sure that proxy is totally removed from the BSFRegistry
  expose obj

-- say "*** BSF.CLS::bsf_array_proxy.assure_BSFRegistry_Removal::UNINIT:" pp(obj~objectname)"@"pp(obj~identityHash)

  signal on syntax name any
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  remove=.true
  do while remove=.true
     remove=BSF("unregisterBean", obj~objectname)~substr(4)
  end
  return

any:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side




-- if not given, a runtime error will be thrown with ooRexx 3.0.1 !!
      -- Error 98 running E:\rony\dev\bsf\src\test\testAELrei.rex line 68:  Execution error
      -- Error 98.913:  Unable to convert object "a BSF_ARRAY_PROXY.ASSURE_BSFREGISTRY_REMOVAL" to a single-dimensional array value
::method makearray
  forward class (super)
*/



/* name:    BSF_UTIL.CLS
   authors: Lee Peedin, Rony G. Flatscher
   date:    2006-03-11
   version: 0.90
   license: CPL 1.0, APL 2.0: (see below)

   defines class "BSF.DIALOG" for easy creating message, option and input dialogs

   usage:   send messages to class object .BSF.DIALOG and the component used will be ".nil",
            else create an instance of this class supplying the component to use for the dialogs
            as its only argument

--------

class method messageBox(...): shows dialog, returns always .nil

   arguments:
      message
      message, title
      message, [title], messageType

--------

class method dialogBox(...): shows dialog, returns the number of the push button pressed (0=first key, ...)
                       or -1, if user closed dialog with other means
   arguments:
      message
      message, title
      message, [title], messageType
      message, [title], [messageType], optionType
      message, [title], [messageType], [optionType], [icon], textOfButtons[, defaultButton]

--------

class method inputBox(...): shows dialog, returns the entered/chosen value or .nil if user closed dialog
                      with other means

   arguments:
      message
      message, defaultValue
      message, [title], messageType
      message, [title], messageType, [icon], textOfOptions[, defaultValue]

--------

where messageType is one of:

      error                   - includes X icon
      information             - includes i icon
      plain                   - no icon included
      question                - includes ? mark icon
      warning                 - includes ! icon

where optionType is one of:

      OkCancel                - includes OK/Cancel buttons
      YesNo                   - includes Yes/No buttons
      YesNoCancel             - includes Yes/No/Cancel buttons
      default                 - includes OK/Cancel, uses messageType "question"


*/

/*


showConfirmDialog
    Used to require confirmation from the user
    Icons vary based on the messageType parameter
    Buttons vary based on the optionType parameter
    Return a value associated with the button selected or the dialog CLOSE button
    Displays in the middle of the users screen

showInputDialog
    Allows the user to enter a single value OR select from a pre-defined array of values
    Icons vary based on the messageType parameter
    Has 2 buttons (OK & Cancel)
    Return the value entered or selected by the user (.nil if Cancel or CLOSE)
    Displays in the middle of the users screen


    Valid messageType parameter

      error                   - includes X icon
      information             - includes i icon
      warning                 - includes ! icon
      question                - includes ? mark icon
      plain                   - no icon included

    Valid optionType parameter
      default                 - includes OK/Cancel, questionType
      OkCancel                - includes OK/Cancel buttons
      YesNo                   - includes Yes/No buttons
      YesNoCancel             - includes Yes/No/Cancel buttons

Button Return Values    -1  Dialog CLOSE
                         0  Yes or OK depending on the optionType
                         1  No
                         2  Cancel

*/



::class bsf.dialog public

/* class methods        */

::method init class
  expose dlgConstants javaDialogClass defaultInstance
   -- initialize attributes to .nil
  dlgConstants    = .nil
  javaDialogClass = .nil
  defaultInstance = .nil


::method getMessageType class    -- determine full name of message type using first character
  parse arg messageType .

  idx=pos(left(messageType~translate,1), "EIPQW")
  if idx>0 then                  -- extract full spelling and return it
     return word("error information plain question warning", idx)

  return arg(1)                  -- return unchanged argument


::method setDefaultComponent class
  expose defaultInstance
  defaultInstance=self~new(.nil) -- use .nil as default component


::method javaDialogClass class   -- the Java class object proxy for creating dialogs
  expose javaDialogClass

  if .nil=javaDialogClass then   -- not yet defined, get reference to it
     javaDialogClass=.bsf~bsf.import('javax.swing.JOptionPane', .nil)

  return javaDialogClass


::method dlgConstants class      -- defines all the dialog constants needed
  expose dlgConstants

  if .nil=dlgConstants then     -- not yet defined, create it
  do
       /* initialisation needed for this module */
          -- get easy access to dlgConstants
     o =  bsf.wrapStaticFields('javax.swing.JOptionPane')
          -- message types
     dlgConstants = .directory~new
     dlgConstants~error                  = o~error_message
     dlgConstants~error.title            = "Error"

     dlgConstants~information            = o~information_message
     dlgConstants~information.title      = "Information"

     dlgConstants~warning                = o~warning_message
     dlgConstants~warning.title          = "Warning"

     dlgConstants~question               = o~question_message
     dlgConstants~question.title         = "Question"

     dlgConstants~plain                  = o~plain_message
     dlgConstants~plain.title            = ""

       -- dialog option types
     dlgConstants~default                = o~default_option
     dlgConstants~default.buttonText     = "OK Cancel"

     dlgConstants~OkCancel               = o~ok_cancel_option
     dlgConstants~OkCancel.buttonText    = "OK Cancel"

     dlgConstants~YesNo                  = o~yes_no_option
     dlgConstants~YesNo.buttonText       = "Yes No"

     dlgConstants~YesNoCancel            = o~yes_no_cancel_option
     dlgConstants~YesNoCancel.buttonText = "Yes No Cancel"
  end

  return dlgConstants


::method unknown class     -- forward all unknown class messages to the defaultInstance
  expose defaultInstance
  use arg methodName, arrArgs
-- say "unknown::class: methodName:" pp(methodName) "defaultInstance:" pp(defaultInstance)
  forward message (methodName) to (defaultInstance) arguments (arrArgs)



/* instance methods     */

::method component attribute     -- the Java component for which the dialog should execute; will be modal for it!
::method init
  expose component
  use arg component              -- get component and save it


   -- method to make it simple to invoke a dialog with push buttons as options
   -- implementation uses javax.swing.JOptionPane
::method dialogBox
  expose component
  use arg message, title, messageType, optionType, icon, textOfButtons, defaultButton

  selfClz=self~class
  dlgConstants    = selfClz~dlgConstants     -- get dlgConstants
  javaDialogClass = selfClz~javaDialogClass  -- get Java class object

  call beep 500, 100    -- beep in an attention tone

  if arg()=1 then       -- only one argument given
  do
     return javaDialogClass~new~showConfirmDialog(component, message)
  end
  else
  do
     -- messageType
     messageType=selfClz~getMessageType(messageType)  -- make sure we have correct spelling
     swingMessageType=dlgConstants~entry(messageType) -- get the Java encoding of message type
     if .nil=swingMessageType then
     do
         messageType="plain"
         swingMessageType=dlgConstants~plain   -- default messageType
     end

     -- title omitted, get default title
     if arg(2, 'Omitted') then   -- title not given, use a default one
     do
        title=dlgConstants~entry(messageType~strip".TITLE")
        if title="" then title="Message"  -- plain, but indicate that this is a message!
     end

     -- optionType
     swingOptionType=dlgConstants~entry(optionType)
     if .nil=swingOptionType then
     do
        optionType="default"
        swingOptionType=dlgConstants~default   -- default optionType
     end

     if arg()<5 then    -- no more arguments (strings for buttons, and setting default button)
     do
        return javaDialogClass~new~showConfirmDialog(component,message,title,swingOptionType,swingMessageType)
     end

     -- optional icon
     if arg(5, 'Omitted') then
        icon=.nil

     -- optional button text collection and optional default button
     if arg(6, 'Omitted') then
        textOfButtons=dlgConstants~entry(optionType~space".ButtonText")~makearray(' ')

     javaArr=bsf.createArray(.bsf4rexx~object.class, textOfButtons~items)  -- create Java array of needed size
     i=0
     do entry over textOfButtons -- this way any ooRexx collection object can be used !
        i=i+1
        javaArr[i]=entry
     end

     if arg(7, 'Omitted') then   -- no default button
     do
        defaultButton=.nil
     end

     return javaDialogClass~new~showOptionDialog(component,message,title,swingOptionType,swingMessageType, icon, javaArr, defaultButton)
  end
  return -99  -- return impossible value



/*
   -- method to make it simple to invoke a dialog with input field or drop-down list to choose from
   -- implementation uses javax.swing.JOptionPane

   test ".bsfdialog~inputBox(...):'
       (message                                                             )
       (message, defaultValue                                               )
       (message, [title], messageType                                       )
       (message, [title], messageType, [icon], textOfOptions[, defaultValue])

        ... method returns the entered/chosen value or .nil (user closed dialog)
 */
::method inputBox
  expose component
  nrArgs=arg()          -- get number of arguments

  selfClz=self~class
  dlgConstants    = selfClz~dlgConstants     -- get dlgConstants
  javaDialogClass = selfClz~javaDialogClass  -- get Java class object

  if nrArgs=1 then       -- only one argument given
  do
     use arg message
     return javaDialogClass~new~showInputDialog(component, message)
  end
  else if nrArgs=2 then
  do
     use arg message, defaultValue
     return javaDialogClass~new~showInputDialog(component, message, defaultValue)
  end


  use arg message, title, messageType, icon=.nil, textOfOptions, defaultValue
  -- messageType
  messageType=selfClz~getMessageType(messageType)  -- make sure we have correct spelling
  swingMessageType=dlgConstants~entry(messageType) -- get the Java encoding of message type
  if .nil=swingMessageType then
  do
      messageType="plain"
      swingMessageType=dlgConstants~plain   -- default messageType
  end

  -- title omitted, get default title
  if arg(2, 'Omitted') then   -- title not given, use a default one
  do
     title=dlgConstants~entry(messageType~strip".TITLE")
     if title="" then title="Message"  -- plain, but indicate that this is a message!
  end


  if nrArgs=3 then
  do
     return javaDialogClass~new~showInputDialog(component, message, title, swingMessageType)
  end

/* rgf, 2010-07-13: now using the default value in USE ARG instead
  -- optional icon
  if arg(4, 'Omitted') then
     icon=.nil
*/

  -- option collection
  if textOptions~isA(.BSF_ARRAY_REFERENCE) | textOptions~isA(.UNO_ARRAY_PROXY) then
  do
     javaArr=textOptions      /* already a Java array in hands, just pass it through */
  end
  else
  do
     if arg(5)~hasmethod("PUT")=.false then -- not a collection ?
     do
        textOfOptions=space(textOfOptions)~makearray(' ')
     end

     -- optional button text collection and optional default button
     javaArr=bsf.createArray(.bsf4rexx~object.class, textOfOptions~items)  -- create Java array of needed size
     i=0
     do entry over textOfOptions -- this way any ooRexx collection object can be used !
        i=i+1
        javaArr[i]=entry
     end
  end

  if arg(6, 'Omitted') then   -- no default button
  do
     defaultValue=.nil
  end

  return javaDialogClass~new~showInputDialog(component,message,title,swingMessageType, icon, javaArr, defaultValue)


   -- class method to make it simple to invoke the desired functionality
   -- implementation uses javax.swing.JOptionPane
::method messageBox
  expose component
  use arg message, title, messageType

  selfClz=self~class
  dlgConstants    = selfClz~dlgConstants     -- get dlgConstants
  javaDialogClass = selfClz~javaDialogClass  -- get Java class object

/*
say "javaDialogClass~toString="pp(javaDialogClass~toString)
say "javaDialogClass="pp(javaDialogClass) "message:" pp(message) "title:" pp(title) "messageType:" pp(messageType)
*/


  call beep 1500, 100            -- beep in an attention tone
  if arg()=1 then       -- only one argument given
  do
     javaDialogClass~new~showMessageDialog(component, message)
  end
  else
  do
     -- default to plain message box
     messageType=selfClz~getMessageType(messageType)  -- make sure we have correct spelling
     if dlgConstants~hasentry(messageType)=.false then
     do
        messageType="plain"
     end

     swingMessageType=dlgConstants~entry(messageType) -- get the Java encoding of message type
     -- title omitted, get default title
     if arg(2, 'Omitted') then   -- title not given, use a default one
     do
        title=dlgConstants~entry(messageType~strip".TITLE")
        if title="" then title="Message"  -- plain, but indicate that this is a message!
     end

     javaDialogClass~new~showMessageDialog(component, message, title, swingMessageType)
  end
  return .nil     -- message box returns always .nil



/* rgf, 2010-04-18: allow creating Java classes that forward method invocations to RexxProxy objects.
                    The value for "defaultHandler" in the resulting class object should be set
                    by the caller using "setDefaultHandler(RexxProxy)".
                    Creating instances of the returned class must have as their very first argument
                    the RexxProxy object which proxies the Java object.
*/
::routine bsf.createProxyClass public
  use strict arg javaClzToProxy, newClzName=.nil, ...

  signal on syntax

  if javaClassToProxy~isA(.string) then
     javaClassToProxy=bsf.loadClass(javaClzToProxy)   -- load Java class

  clz=.bsf4rexx~entry("ProxiedJavaClassTool")
  if clz=.nil then
  do
-- say ".... bsf.createProxyClass(): need to load and save ProxiedJavaClassTool clz"
      clzName="org.rexxla.bsf.engines.rexx.ProxiedJavaClassTool"
      clz=bsf.loadClass(clzName)
      .bsf4rexx~setEntry(clzName, clz)
  end

  arg3=.nil
  if arg()>2 then    -- handle method names, turn them into a Java array
  do
      arg3=arg(3)
      if \arg3~isA(.bsf_array_reference) then   -- not a Java array ?
      do
         if \arg3~isA(.array) then
            arg3=arg(3, "A")              -- turn remaining arguments into an array

            -- create Java array and set it to the method names
          items=arg3~items

          -- rgf, 2010-11-27: inhibit proxying of "toString", "equals" and "hashCode" as this may break the JVM
          do i=1 to arg3~items
             val=arg3[i]
             if val~words>1 then val=val~word(val~words)
             val=val~strip
             p=pos(val~strip~upper, "TOSTRING EQUALS HASHCODE")
             if p>0 then                       -- illegal method found!
             do
                 raise syntax 88.900 array ("BSF.CLS/routine/bsf.createProxyClass, error 1:" -
                             "Java methods 'toString', 'equals' and 'hashCode' must not be proxied." -
                             'Found Java method: "'val'"')
             end
          end


          javaArray=bsf.createJavaArray("java.lang.String", items)
          do i=1 to items
             javaArray[i]=arg3[i]      -- save value in Java array
          end
          arg3=javaArray               -- use this now as argument
      end
  end

  newClz=clz~createProxiedJavaClass(javaClassToProxy, newClzName, arg3)

  return .bsf~bsf.import(newClz~getName, .nil, newClz)

syntax:
  raise propagate



