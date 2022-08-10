#!/usr/bin/env rexx
/*
   author:  Rony G. Flatscher
   date:    2005-04-18/19/20

   last change: $Revision$ $Author$ $Date$

   changed:
            2019-02-03, ---rgf, - now that bsf.import() does not accept abstract Java classes, use bsf.loadClass()
                                  for them in public routine UNO.LoadClass(), work now carried out in
                                  new private routine work.loadUnoClass()

            2018-06-07, ---rgf, - added method "util.setmethods" to class UNO_ENUM to allow setting methods
                                  (with "float" scope) to individual instances; needed for decorating
                                  UNO_ENUM instances (adding methods 'toString', 'name' and 'value')

            2018-06-06, ---rgf: - change to UNO_ENUM class: not placing the enum objects into the
                                  directory anymore (cf. comment 2010-07-15) caused the loss of the
                                  enhanced methods (eg. "name", "value"); this solution will request
                                  a new Enum object from UNO each time (inhibiting the DisposedException),
                                  but enhance the returned .BSF_Reference object with the methods defined
                                  in the new class attribute 'dirMethods' and return that instead; this
                                  way the pre-2010-07-15 behaviour of Enum instances having the methods
                                  "name" and "value" is reinstated

            2011-02-21, ---rgf, - Apple MacOSX-support added for displaying correct default icon in dock
            2010-07-24, ---rgf, - made the routine uno.determineScriptLocation() script language independent

            2010-07-20, ---rgf, - uno.createDesktop() now returns the XDesktop interface to the service

            2010-07-15, ---rgf, - changed the behaviour of the .UNO_ENUM class: whenever the enum value
                                  is requested, now the code will cause always a lookup via the Java class
                                  object representing the UNO_ENUM; otherwise it may be the case that
                                  Java UNO disposes the remote bridge to UNO such that the cached Java
                                  object representing the UNO_ENUM value will throw a java_remote_bridge
                                  "com.sun.star.lang.DisposedException" if used as an argument

            2010-07-12, ---rgf, - stop if UNO_Proxy's unknown method is about to recurse infinitively with "findInterfaceWithMember"
                                  for whatever reasons

            2010-07-11, ---rgf, - does not wrap already wrapped .UNO_PROXY objects anymore
            2010-07-10, ---rgf, - introduced a new .uno entry named "bShowErrorPopup", which by default
                                  is set to .true. If set to .false no UNO error-popup windows are
                                  created at all (which is advisable).

            2010-07-07, ---rgf, - routine uno.getScriptContext: added syntax handling, which returns
                                  .nil, if a non script-context argument was supplied

            2010-06-13, ---rgf, - changing names in directory returned by the public routine
                                  "uno.determineScriptLocation"

            2010-06-08, ---rgf, - enquoting "rgf_util2.rex" requires directive to allow it to
                                  work under Unix
            2010-06-07, ---rgf, - adding the public class "PARCEL" and its private class "ParcelScriptDefinition"
                                  to ease interaction with the script's parcel descriptors

                                - added public routine "::routine uno.determineScriptLocation" to
                                  ease (un)installing Rexx scripts/macros

                                - added class method "uno.wrap" to class .uno

            2010-05-27, ---rgf, - adapting to extension change (".rex" -> ".rxo")

            2010-05-08, ---rgf, - remove dependency on .bsf~bsf.getJavaClassObjectProxy(...)

            2010-02-28, ---rgf, - fixed a bug in UNO_PROXY.UNKNOWN; if non-interface members start
                                  with an "x", now will correctly process that member, if an interface
                                  of the given name does not exist (e.g. because members of a struct
                                  start with an x)

            2009-10-31, ---rgf, - added an optional argument "hashTable" to the public routines
                                  uno.getScriptContext(), uno.getScriptProviderVersion(),
                                  uno.getScriptFileName(), uno.getScriptMetaData(),
                                  uno.getScriptSourceModel(), uno.getScriptPath()
                                  This argument is available since BSF4ooRexx upon invocations of
                                  Rexx macros using the OOo dispatch mechanism and should be supplied
                                  to the above routines as an argument.
                                  (This argument contains all macro/script management data in a
                                  thread safe manner. Without that argument the BSFRegistry is
                                  consulted, but the BSFRegistry-values may have been changed
                                  in the meantime by the invocation of another script on a different
                                  thread.)

                                - uno.getScriptContext() does not set .uno~rgfReflectUNO context field
                                  anymore if that has been set to a value already; use the public
                                  routine UNO.resetRgfReflectUNO(ctxt) to force the setting to
                                  a different context

            2009-04-13, ---rgf, - now getting "line.separator", "file.separator", and "path.separator"
                                  from .bsf4rexx

            2009-02-15, ---rgf, - UNO.resetRgfReflectUNO(): now accepts an optional XComponentContext,
                                  if not supplied, and invoked via the ScriptingFramework, then
                                  use the scripting's XComponentContext, otherwise create a new
                                  connection to OOo and use that XComponentContext

            2009-02-09, ---rgf, - fixed bug in UNO_CONSTANTS: value contained "|" and trailing
                                  type name which got introduced after seeing an UNO CONSTANT
                                  group that had different types per value; now the
                                  type-information (individual value's typename) is stored as
                                  'name".TYPENAME"' and 'value".TYPENAME"'

                                - UNO.WRAP(): now does try a BSF.WRAP(), if a string with an
                                  object prefix ("<O>") is submitted

            2008-09-13, ---rgf, - sets BsfShowErrorMessages to .false, as this module will
                                  take over the error reporting; set it to .true to see
                                  what exceptions UNO.CLS takes on and is able to carry out

                                - if .uno~bAutoResolveQueryInterface=.true, then runtime will
                                  automatically carry out a queryInterface(), if possible

            2008-09-11/12, ---rgf, - added .uno~nil (represents a UNO-null via a ANY~void type)
                                - .uno~xPropertySetAutoCase: if .true, then case of property will be adjusted, if necessary

                                - .uno~xPropertySetAutoBox : if .true, setting values will be automatically boxed

                                - improved error-information handling to help end-user developers to find
                                  problems as quickly as possible

            2008-09-10, ---rgf, - added method "uno.getInterfaceName(['Unqualified'])" and
                                  public routine uno.getInterfaceName(object[, 'Unqualified'])

                                - added method "uno.supportsService(name)" and public routine
                                  "uno.supportsService(object, name)"

                                - added method "uno.supportsInterface(name)" and public routine
                                  "uno.supportsInterface(object, name)"

                                - added more helpful information, if a "setPropertyValue()" could
                                  not execute successfully (e.g. using a non-existent, wrongly cased
                                  property name, or a wrongly typed value)

            2008-08-31, ---rgf, - new "UNO" public class now behaves as a directory by forwarding
                                  all unknown messages to its directory class attribute
                                - removed "UNODIR" class

            2008-08-28, ---rgf, - moved ".uno" directory object to the class attribute
                                  "dir" of class ".UNODIR" and have ".uno" just point to it;
                                  the code in here will be changed to use only the UNODIR class
                                  attribute such that multiple interpreter instances running
                                  in the same process do not influence that content unadvertently
                                  (.local is shared in ooRexx < 4.0!)

            2008-08-20, ---rgf, - added debug information in case an OOo Basic like interaction
                                  with a property is taking place (e.g. because transcribing
                                  an OOo Basic macro 1:1 to ooRexx); the information informs
                                  the programmer which ooRexx code to create to allow for the
                                  intended behaviour; if one defines ".uno~bAutoResolve=.true",
                                  then execution continues for further debugging

            2008-08-18, ---rgf, - added debug information in case a method/attribute was not found; if
                                  possible to resolve path to method/attribute this information will be
                                  given in the UNO.CLS string encoding (almost self-defined);
                                  if resolvable the program may continue (interrupted by message boxes
                                  displaying the paths to resolvable methods/attributes), if one
                                  defines ".uno~bAutoResolve=.true"
            2008-07-20, ---rgf, - added public routine "uno.addProperty(name[,value=.nil])" to
                                  ease creating PropertyValue objects
            2008-06-27, ---rgf, - changed uno.getScriptPath to return script's path, if no
                                  argument is given and a script in hand; now also processes
                                  LocationPlaceHolder-values that are led in with "user"/"share"
            2008-06-24, ---rgf, - changed return value of "uno.addPath()" to return added path, or
                                  empty string "", if path could not be added (e.g. because it
                                  existed already)

                                - added public routine "uno.removePath([removePath][,envName])
                                  where "addPath" defaults to the script's path, if not a script,
                                  then the current directory; "var" defaults to "PATH"; returns
                                  removed path or empty string "" else (e.g. if 'removePath' did
                                  not exist); if 'envName' will be emptied by this operation, then
                                  it will be removed from the environment

                                - added public routine "uno.getScriptPath([scriptUri])"; returns
                                  the system path of the supplied 'scriptUri', and if omitted and
                                  a dispatched script the script's system path, else .nil

            2008-06-23, ---rgf, - added public routine "uno.addPath([addPath] [,envName] )
                                  where "addPath" defaults to the script's path, if not a script,
                                  then the current directory; "var" defaults to "PATH"

            2008-06-10, ---rgf, - wrapped ScriptProvider objects as UNO-proxy objects; added
                                  available script meta data methods as comments next to routine
                                  uno.getScriptMetaData()

            2008-03-24, ---rgf, - added public routine "UNO.resetRgfReflectUNO" which will
                                  create a new xContext and assign it to the "RgfReflectUNO"
                                  class; this allows to overcome situations in which the
                                  xContext gets invalidated by shut-down OOo instances, which
                                  would cause RgfReflectUNO to only return an empty string ("")
                                  instead of string renderings of the IDL definition

            2007-09-21, ---rgf, - added public methods uno.getScriptFileName(), and
                                  uno.getScriptMetaData(), which return values, if script was
                                  invoked by OOo, otherwise .nil
            2007-01-30, ---rgf, - added uno.convertToUrl() and uno.convertFromUrl() which should replace
                                  the old versions (not starting with "uno.")
            2007-01-20, ---rgf, - changed UNO_ENUM enum object's MAKESTRING method to return as
                                  a required string value a string in the form:

                                     a[n] FULLY_QUALIFIED_IDL_NAME[ENUM_NAME=ENUM_VALUE]

                                  representing the full information, easily parseable by Rexx
                                - changed the index' name for the default enum value got changed
                                  to "DEFAULT_ENUM_VALUE"
                                - added method MAKESTRING to UNO_ENUM which will return a required
                                  string value listing all enum values in an easy parseable form
            2007-01-19, ---rgf, - changed class UNO_ENUM to return the Java objects representing the
                                  enum value, but at the same time allowing to get at their name and
                                  value supplying the methods "NAME" and "VALUE"
            2007-01-14, ---rgf, - renamed the public routine "uno.getInterfaceNames" to
                                  "uno.getCachedInterfaceNames" (it processes up the XInterface-cache);
                                  this way the routine should not be intermixed with the UNO_PROXY
                                  method named "uno.getInterfaceNames" (which uses UNO reflection to
                                  return the well known full interface class names)
                                - "uno.connect()"  now accepts a second optional argument,
                                  an "XCcomponentContext" in case it got established already
            2007-01-12, ---rgf, - renamed UNO_PROXY-method "uno.getServiceNamesViaReflection" to
                                  "uno.getServiceNames" and "uno.getInterfaceNamesViaReflection" to
                                  "uno.getInterfaceNames"
                                - added UNO_PROXY-method "uno.getInterfaceNames"
            2007-01-06, ---rgf, - added optional replacement string to ppd(), changed logic to use Java's
                                  "line.separator" and also the changestr()-BIF;
                                - .uno now contains the platform dependent "line.separator", "file.separator"
                                   values from java.lang.System's properties
                                - removed method attribute "bsfObject", renamed "bsfObject" to to
                                  "uno.bsfObject" (changed all occurrences), added getter method "uno.bsfObject"
                                - UNO_DIRLIKE: changed attribute methods to make setters private,
                                  added new methods to retrieve a *copy* of "unoDirectory" and
                                  "unoNameQueue" for inspection
            2006-09-03, ---rgf, added public routine "uno.getInterfaceName": returns fully qualified
                                name of unqualified interface name; if duplicates are possible a blank
                                delimited string of the fully qualified interface names is returned
            2006-07-24, ---rgf, made UNO_CONSTANTS.encode() a little bit more flexible (now accepting
                                an arbitrary mix of constant-field-names and decimal values)
            2006-07-19, ---rgf, added/changed class UNO_DIRLIKE, UNO_ENUM and UNO_CONSTANTS, which
                                can be treated as directories, if retrieving field-names or their
                                numeric values; UNO_CONSTANTS has in addition the methods
                                ENCODE(blank-delimited-field-names) returning the encoded decimal
                                number; DECODE(decimal number) returning a blank-delimited-filed-names
            2006-07-17, ---rgf, added UNO.WRAPARRAY which determines whether to wrap up a BSF_ARRAY
                                object as an UNO_ARRAY_PROXY
            2006-07-16, ---rgf, added UNO_ARRAY_PROXY class; the public routine UNO.CreateArray()
                                which uses BSF.CreateArray(), but wraps it up as an UNO_ARRAY_PROXY
                                (making sure that its elements are wrapped up as UNO_PROXY objects)
            2006-07-12, ---rgf, uno.setCell() now allows letter-number-coordinates; added routine
                                uno.getCell(xSheet, colNr0, rowNr0) or uno.getCell(xSheet, LetterNumber)
                                returning a XCell object [you can use {g|s}etValue(), {g|s}etFormula(),
                                getError() or getType() with it]
            2006-07-10, ---rgf, in uno.getScriptContext() the RgfReflectUNO class gets explicitly a XComponentContext
                                set to; otherwise RgfReflectUNO will bootstrap hanging the OOo Scripting
                                Framework for ooRexx macros dispatched by it!
                                Just for congruency, also added same technique to UNO.connect().
                                Now fallback strategy in UNO_PROXY::UNKNOWN re-established: if unknown
                                interface name then reflection is used at runtime
            2006-06-04, ---rgf, fixed a bug in querying an interface object, if unqualified class name is
                                used more than once
            2006-02-03, ---rgf, commented fall-back-strategy to query interface object via RgfReflectUNO-class
                                in UNO_PROXY.UNKNOWN
            2006-01-06, ---rgf, removed public routines pp() and iif(), because they are now availble
                                in the required BSF.CLS module; added the public routines
                                uno.getScriptContext() and uno.getScriptContextVersion()

            2005-05-03, ---rgf, made sure that exceptions are propagated
            2005-06-05, ---rgf, uppercasing "OOo.cls" to "OOO.CLS", removing
            2005-07-03, ---rgf, changed logic to queryInterface(), now dealing with duplicate class names
                                gracefully; fixed a bug (do not wrap up BSF arrays)
            2005-07-04, ---rgf, optimized handling interface classes,
                                added OOo.getInterfaces(object[, sortByUnqualifiedClassName]),
                                added OOo.getServices(object)
            2005-07-05, ---rgf, removed unnecessary preloading of application classes
            2005-07-08, ---rgf, ooo.wrap(): now tries a bsf.wrap(), if ncecessary
            2005-07-30, ---rgf, adapted OOO.CLS to new array-behaviour in BSF.CLS
            2005-08-18, ---rgf, added public routines "ConvertToURL()", "ConvertFromURL()", which use
                                "encodeURL()", "decodeURL()"
            2005-08-31, ---rgf, renamed module to "UNO.CLS" and renamed stuff from "OOO" to "UNO" to
                                reflect the generic support for UNO; office is just one combination of
                                UNO components and UNO application; added license
            2005-12-15, ---rgf, added newly developed Java class "org.oorexx.uno.RgfReflectUNO",
                                added entry "version" in ".uno"
            2005-12-16, ---rgf, added wrapper routines for RgfReflectUNO methods which wrap returned
                                UNO objects into ooRexx UNO_PROXY objects;
            2005-12-21, ---rgf, adapted and added wrapper routines for RgfReflectionUNO
            2005-12-26, ---rgf, adapted UNO-routine names to new RgfReflectionUNO names, added
                                UNO.*-methods to UNO_PROXY
            2005-12-27, ---rgf, updated license part, added utility class "UNO_ENUM_OR_CONSTANTS"
            2005-12-28, ---rgf, generalized the "directoryfiying" of UNOIDL definitions for "UNO_ENUM",
                                "UNO_CONSTANTS" and "UNO_PROPERTIES" (removed "UNO_ENUM_OR_CONSTANTS")
            2005-12-29, ---rgf, "UNO_ENUM" now returns the Java object representing the enum-value,
                                "UNO_CONSTANTS" will return the constant's value (a number)
            2006-01-03, ---rgf, corrected inline documentation


   version: 1.3.8
   name:    UNO.CLS
   purpose: allow interfacing with UNO components (via Java), tested with OpenOffice.org, v1.1.x and up
   needs:   BSF4Rexx, BSF.CLS, UNO_XINTERFACES.RXO

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2005-2018 Rony G. Flatscher

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

call initialize.uno.dir          -- make sure .UNO's uno.dir object is initialized

if .bsf4rexx~opsys="MACOSX" then  -- on MacOSX display the standard ooRexx4OOo logo
do
           -- by default display the bsf4oorexx icon
    if .bsf4rexx~hasEntry("oorexx4ooo_256") then
    do
      .bsf4rexx~apple_application~setDockIconImage(.bsf~new("javax.swing.ImageIcon",.bsf4rexx~oorexx4ooo_256)~getImage)
-- say "... UNO.CLS found icon !!"
    end
end

   -- do not show BSFException messages as UNO.CLS will show them as popups, if necessary
call bsfShowErrorMessage .false

      -- define UNO's null value, represented by a void Any;
      -- use this value, if you want to set a property or a String argument to .nil!
.uno~nil=.uno~any~void        -- if using in comparisons, then use "uno.areSame(unoObject, .uno~nil))"
.uno~nil_name=.uno~any~objectname   -- index name of entry in BSFRegistry on the Java side

.uno~version="138.20190203"      -- set version number

   -- XPropertySet-behaviour with getPropertyValue and setPropertyValue
.uno~xPropertySetAutoCase=.true  -- if prop name not found pick a matching case, if possible
.uno~xPropertySetAutoBox =.true  -- autobox property values with "setPropertyValue"


::requires BSF.CLS                -- get BSF4Rexx support
::requires UNO_XINTERFACES.RXO    -- get the list of all of UNO XInterface classes
::requires "rgf_util2.rex"        -- quotes ensure case preservation (e.g. for Linux)

/* Initialize UNO.DIR which is stored with the UNO class object, such that
   each interpreter instance is separated from each other w.r.t. the objects
   stored in there.
*/
::routine initialize.uno.dir
   if .uno~items>0 then         -- already initialized? if so, return
      return

   uno.dir=.uno

   uno.dir~bShowErrorPopup=.true -- 2010-07-10, rgf: if this is set to .false no UNO-related error-popups will get generated at all

   uno.dir~bExtendSearch=.true      -- as of 2005-12-20: if interface not found via XTypeProvider,
                                 -- use full reflection (slow, but there seem to be bugs in XTypeProvider)

         -- operating system dependent characters/strings
/*  ---rgf, 2009-04-13: with ooRexx 4.0.0beta the following does not work repeatedly with RexxStart(...) !
   uno.dir~file.separator=.java.lang.system~getProperty("file.separator")   -- get info from Java
   uno.dir~line.separator=.java.lang.system~getProperty("line.separator")   -- get info from Java
   uno.dir~path.separator=.java.lang.system~getProperty("path.separator")   -- get info from Java
*/

   uno.dir~file.separator=.bsf4rexx~file.separator
   uno.dir~line.separator=.bsf4rexx~line.separator
   uno.dir~path.separator=.bsf4rexx~path.separator

   -- save class objects of UNO proxy classes defined in this module
   uno.dir~proxyclasses=.set~of(.uno_proxy, .uno_array_proxy)

   -- preload some important classes, just send their class name to the directory object ".UNO"
   l = .list~of( "com.sun.star.beans.PropertyValue"   , -
                 "com.sun.star.beans.XPropertySet"    , -
                 "org.oorexx.uno.RgfReflectUNO"       , -
                 "com.sun.star.comp.helper.Bootstrap" , -
                 "com.sun.star.uno.Any"               , -
                 "com.sun.star.uno.AnyConverter"      , -
                 "com.sun.star.uno.UnoRuntime"       )

   do c over l -- get Java class object and save it in .UNO by className
      call UNO.loadClass c
   end


-- trace ?all
-- trace ?i

      -- create empty array of property-value (needed quite often)
   uno.dir~noProps=.bsf~bsf.createArray(uno.dir~propertyValue, 0)

      -- list of Interface classes to be loaded at runtime (needs completion)
   l = UNO.getInterfaceList()    -- retuns > 950 UNO-interface classes in a list

   -- save interface class names with which we want to deal
   d=.directory~new  -- directory for all InterfaceTypes
   r=.relation~new   -- relation to store duplicates (if last word is used multiple times, keep full class name)
   do i over l
     idx=substr(i, lastpos(".", i)+1)
     d~setentry(i, i)    -- by fully qualified name

     if d~hasentry(idx) then  -- interface class name used already, use relation to store full name
     do
        r[idx~translate]=i    -- save full name with class name
     end
     else   -- new interface class name
     do
       d~setentry(idx, i)     -- by last word
     end
   end
   uno.dir~XInterfaces = d       -- save directory
   uno.dir~XInterfaces.dupes = r -- save relation
   return



      -- load UNO-class and save it in environment; "idx" determines the name which is used to
      -- store the class object in the directory ".UNO"
::routine UNO.loadClass public
     parse arg className, idx

     if idx="" then idx=substr(className, lastpos(".", className)+1) -- extract last word to serve as index
     if .uno~hasentry(idx)=.true then    -- e.g. last word is used as a class in a another package as well
        .error~say("---> UNO.loadClass():" pp(idx) "already used, in hand:" pp(className) "in .UNO:" pp(.uno~entry(idx)))

     tmpClass=.UNO_proxy~new(work.loadUnoClass(className))  -- rgf, 2019-02-03, make it look like an ooRexx class
     .uno~setentry(idx, tmpClass)   -- load the given Java class and save it in .UNO
     return tmpClass

::routine work.loadUnoClass     -- rgf, 2019-02-03: make sure that abstract classes do not get imported!
   use arg className
   signal on syntax
   jco=bsf.wrap( .bsf~rBSF~call("loadClass", className) )   -- rBSF from BSF.CLS

   if bsf.loadClass("java.lang.reflect.Modifier")~isAbstract(jco~getModifiers) then
      return jco
   return .bsf~bsf.import(className,.nil)   -- add class methods new and newStrict to ooRexx class object

syntax:
  raise propagate



/* If the OOo instance which is used for RgfReflectUNO is closed, then the reflection
   class will not work anymore, but return a blank string. To work again, a new
   xContext needs to be created and assigned to the RgfReflectUNO class.

   ---rgf, 2008-03-24
*/
::routine UNO.resetRgfReflectUNO public
  use arg xContext=.nil

  sc=.nil                           -- explicitly set to .nil
  if xContext=.nil then             -- no context given, get/create one
  do
     -- if invoked via the framework, use the supplied context:
     -- will set already .uno~rgfReflectUNO to the script's XComponentContext!
     sc=uno.getScriptContext()
     if sc<>.nil then
        xContext=sc~getComponentContext   -- get the supplied ComponentContext
     else                           -- nope, create a new connection to OOo
        xContext=.uno~Bootstrap~bootstrap -- get an initialized local UNO runtime environment
  end

  if sc<>.nil then   -- getScriptContext() would have set .uno~rgfReflectUNO to tis XContext
     .uno~rgfReflectUno~setContext(xContext)

  key="rRU.Ctxt.Set"                -- key to use for indicating that the context was set for RgfReflect
  .uno~setentry(key, xContext)      -- store context with key in .local


   /*
      connects to the UNO runtime environment and returns the XContext object;
      if passing a URL, then the remote object is returned
   */
::routine UNO.connect public
  parse arg unoURL

  key="rRU.Ctxt.Set"             -- key to use for indicating that the context was set
  if \.uno~hasentry(key) then
  do
     xContext=.uno~Bootstrap~bootstrap    -- get an initialized local UNO runtime environment
     .uno~rgfReflectUno~setContext(xContext)
     .uno~setentry(key, xContext)         -- store context
  end

  if arg()=0 then -- no arguments, hence return
  do
     return .uno~entry(key)      -- return stored context
  end

  if arg(2, "Omitted") then      -- optional argument 2
  do
     -- get a local context, that contains the service manager to load the UNOUrlResolver
     xContext=.uno~Bootstrap~createInitialComponentContext(.nil)
  end
  else      -- xContext supplied, retrieve it (we already have the unoURL)
  do
     use arg , xContext
  end

/* since 2007-01-14, part of the standard initialisation of this module
   -- make sure RgfReflectUNO() has XComponentContext available, so it does not need
   -- to bootstrap, which would hang in Java
   .uno~rgfReflectUno~setContext(xContext)
*/

  if unoURL="" then  -- no unoURL given, return initial component context
  do
     return xContext       -- return the context object (which can be used to get the service manager)
  end

  uur= xContext~getServiceManager~createInstanceWithContext("com.sun.star.bridge.UnoUrlResolver", xContext)
  remoteObject = uur~XUnoUrlResolver ~resolve(unoURL)    -- get the connection
  return remoteObject



      -- get a XContext, use it to create and to return the XDesktop interface object
::routine UNO.createDesktop public
  USE ARG xContext=.nil
-- say "UNO.createDesktop():"
-- trace i
  if arg()=0 | .nil=xContext then   -- no context given, create default (local) one
  do
     -- xContext=UNO.connect() -- since 2007-01-14 an incompletely initialized object gets returned
     xContext=.uno~Bootstrap~bootstrap    -- create a fully initialized local XComponentContext

      -- make sure RgfReflectUNO() has XComponentContext available, so it does not need
      -- to bootstrap, which would hang in Java
      key="rRU.Ctxt.Set"               -- key to use for indicating that the context was set
      if \.uno~hasentry(key) then
      do
         .uno~rgfReflectUno~setContext(xContext)
         .uno~setentry(key, xContext)         -- store context
      end
  end

  service="com.sun.star.frame.Desktop" -- OOo Desktop UNO component service
  return xContext~getServiceManager~createInstanceWithContext(service, xContext) ~XDesktop


   -- a routine for inserting values or formulas into cells; uses setFormula() which is language independent
::routine UNO.setCell public

   if arg()=4 then   -- address is by position, directly addressing an individual cell
   do
      use arg xSheet, x, y, content
   	xSheet~getCellByPosition(x, y) ~setFormula(content)
      return
   end

   -- cell was addressed by a nameAdress (ie. a "cellRange"), e.g. "E4"
   use arg xSheet, nameAddress, content
      -- get the cellRange and address its first cell at position (0,0)
   xSheet~getCellRangeByName(nameAddress)~getCellByPosition(0,0) ~setFormula(content)


   -- a routine for getting and returning a XCell object; understands setValue(), getValue(),
   -- getFormula(), setFormula(), getError(), getType()
::routine UNO.getCell public

   if arg()=3 then   -- address is by position, directly addressing an individual cel
   do
      use arg xSheet, x, y
   	return xSheet~getCellByPosition(x, y)
   end

      -- cell was addressed by a nameAdress (ie. a "cellRange"), e.g. "E4"
   use arg xSheet, nameAddress
      -- get the cellRange and address its first cell at position (0,0)
   return xSheet~getCellRangeByName(nameAddress)~getCellByPosition(0,0)



/* 2008-08-31, --rgf
*/
::CLASS UNO PUBLIC

::METHOD uno.wrap       unguarded class    -- 20100607: make routine UNO.WRAP() indirectly availabe
  use arg obj
  return uno.wrap(obj)

::method init     class
  expose dir
  dir    =.directory~new

::method unknown  class
  expose dir
  use arg name, args
  forward to (dir) message (name) arguments (args) -- re-direct message to directory



/* *************************************************************
   Class:       UNO_PROXY
   Purpose:     allows to wrap up Java objects as Object Rexx objects;
                if instances are destroyed, then no derigstration from
                the BSF registry takes place
   restriction: needed to make getting interface objects easier
   ************************************************************* */
::CLASS UNO_PROXY -- subclass BSF PUBLIC

-- ::Method uno.bsfObject ATTRIBUTE
::Method uno.bsfObject  -- only a getter method!
  expose uno.bsfObject
  return uno.bsfObject

::METHOD init
   expose  uno.bsfObject   -- direct access to BSF object
   use arg uno.bsfObject   -- assign object to attribute

      -- set objectName to beanName
   self~objectName=uno.bsfObject~objectName   -- use same objectname as BSF proxy (= idx into BSF-registry)

/* 2010-05-08, rgf
      -- if uno.bsfObject is an imported Java class object, then use its beanName directly
   if uno.bsfObject~string~left(4)~translate="THE " then
   do
call beep 100,150
say "UNO_PROXY::INIT, uno.bsfObject - should NEVER get in here !!! Please tell author... "
      signal on syntax name continue   -- if an error, not a JCO, hence use its "plain" objectname
      -- 20100507: self~objectname=.bsf~bsf.getJavaClassObjectProxy(uno.bsfObject~bsf.FullJavaClassName)~string
say "UNO_PROXY::INIT, uno.bsfObject       ="pp(uno.bsfObject) "self~objectname="pp(self~objectname)

      --  self~objectname=uno.bsfObject~bsf.fullJavaClassName   -- rgf, 20100508

say "                 now: self~objectname="pp(self~objectname)
   end

   return

continue:   -- catch exception
/*
   -- default objectname
   self~objectName=uno.bsfObject~objectName   -- use same objectname as BSF proxy (= idx into BSF-registry)
say "UNO.CLS ---> ---> self~objectName="self~objectname
*/
*/



::METHOD unknown
   expose arg uno.bsfObject -- direct access to BSF object
   parse upper arg xName 1 idx   -- make sure message name is in uppercase (if quoted)

   xName=xName~strip          -- strip surrounding whitespace

   if pos(".", idx)>0 then idx=substr(idx, lastpos(".", idx)+1)   -- getindex

   SIGNAL ON SYNTAX NAME ANY  -- intercept all exceptions

   if idx~left(1)="X" then    -- if an interface object is asked for, query and return it
   do
-- say "in UNKNOWN -0- xName="pp2(xName) "idx="pp2(idx)
      xClass=getInterfaceClass(xName, idx)

      if .nil<>xClass then    -- if an interface class could get loaded
      do
            -- query and return the interface object
         interface=.uno~unoRuntime~queryInterface(xClass, uno.bsfObject)
         if interface=.nil then  -- not found, maybe a duplicate simple class name ?
         do
            do fullName over .uno~XInterfaces.dupes~allAt(idx) -- iterate over all
               idx=substr(fullName, lastpos(".", fullName)+1)  -- getindex
               xClass=getInterfaceClass(fullName, idx)      -- rgf, 2006-06-04

               if .nil<>xClass then
                  interface=.uno~unoRuntime~queryInterface(xClass, uno.bsfObject)

               if .nil<>interface then
               do
                  return uno.wrap(interface)
                  -- return .UNO_proxy~new(interface)
               end
            end

            -- now try reflection (via "RgfReflectUNO"), maybe a new interface, not in UNO_XINTERFACES.REX
            res=.uno~RgfReflectUno~queryInterfaceObjectByName(uno.bsfObject, xName, .uno~bExtendSearch )

            if .nil<>res then
            do
               return uno.wrap(res)
               -- return .uno_proxy~new(res)
            end

            return .nil    -- no interface object available
         end


         return uno.wrap(interface)
         -- return .UNO_proxy~new(interface)
      end
   end


-- say "in UNKNOWN -1- xName="pp2(xName) "arg(2)="pp2(arg(2)) pp2(arg(2)~at(1)) pp2(arg(2)~at(2)) "self="pp2(self) "self~class="pp2(self~class)
   -- pass to BSF to handle it
   if uno.bsfObject~hasmethod(xName) then -- o.k. it's a BSF method (could be ooRexx OBJECT method as well!)
   do
-- say "in UNKNOWN -1a- forward message to uno.bsfObject, messageName="pp(xName) "uno.bsfObject="pp2(uno.bsfObject)  "uno.bsfObject~class="pp2(uno.bsfObject~class) "..."
      FORWARD TO (uno.bsfObject) MESSAGE (xName) ARGUMENTS (arg(2)) continue -- rgf, 20080911
   end
   else
   do
-- say "in UNKNOWN -1b- forward message to uno.bsfObject, messageName=arg(1)="pp(arg(1)) "uno.bsfObject="pp2(uno.bsfObject)  "uno.bsfObject~class="pp2(uno.bsfObject~class) "..."
      FORWARD TO (uno.bsfObject) CONTINUE -- let BSF handle this
   end

-- say "in UNKNOWN -2- uno.bsfObject="pp2(uno.bsfObject) "messageName=xName="pp2(xName) "arg(2)="pp2(arg(2)) pp2(arg(2)~at(1)) pp2(arg(2)~at(2)) "result="pp(result)
   if var("result") then         -- rgf, 20080911: wrap return value, if any
   do
      return uno.wrap(result)
   end
   return



ANY:  -- method not found or some other exception on the Java side
rgfcond=condition("o")
-- say "in *** ANY *** -1- xName="pp2(xName) pp(rgfcond~code) "propagated="pp(rgfcond~propagated) pp(rgfcond~errortext) pp(rgfcond~message)

      /* if recursing with method "findInterfaceWithMember", then we need to stop the recursion now */
   if xName~caselessEquals("FINDINTERFACEWITHMEMBER"), condition("o")~propagated then
      raise propagate

    -- define a few values for creating the error message for the popup dialog
   nl=.endOfLine         -- single LF
   nl2=nl||nl            -- two consecutive LFs
   hint ="   -> "
   hint2="      "
   infoStr=""

-- say "                 self ///---"pp(self)"---\\\" pp(rgf99);co=condition("o");
-- call dump2 co; say "traceback:";call dump2 co~traceback

   proxyType=self~uno.getInterfaceName      -- get the fully qualified Interface class name

      -- if XPropertySet interface, then maybe wrong property name or wrong value type
   if proxyType="com.sun.star.beans.XPropertySet", -
      xName~caselessEquals("setPropertyValue") | xName~caselessEquals("getPropertyValue") then
   do
      propName=arg(2)[1]               -- get property name from argument array
      props=uno.getProperties(uno.bsfObject) -- get all available properties

      if var("BSF_ERROR_MESSAGE") then
         infoStr="BSF exception occurred:" nl2 || bsf_error_message || nl || "---" nl2

         -- the following selection should never be takable, as it can be expected that
         -- a service object with no properties, won't have a XPropertySet interface!
      if props="" then                 -- no properties defined!
      do
         if .uno~bShowErrorPopup=.true then
         do
               -- show path to method/attribute, interrupt program
            title="No Properties Available"

            infoStr=infoStr || "UNO object in hand:" nl2 || hint uno.bsfObject~toString
            infoStr=infoStr || nl2 || "There are no properties available for this UNO object, let alone" pp(propName)"!"
            .error~say( "-"~copies(79))
            .error~say( title"," infoStr)
            .error~say( "^"~copies(79) nl)
            .bsf.dialog~messageBox(infoStr, title, "error")
         end

         raise propagate
      end

      -- check whether property (arg(1)) exists, if not check to see whether the
      -- case is misspelled, show caseless sorted list of property names
      tmpName=findPropName(propName, props)   -- get propertyName from argument array, look in props

-- say "in ANY -2- propName="pp2(propName)
      if tmpName="" then               -- property does not exist at all!
      do
         if .uno~bShowErrorPopup=.true then
         do
               -- show path to method/attribute, interrupt program
            title="Property Not Found"

            infoStr=infoStr || "UNO object in hand:" nl2 || hint uno.bsfObject~toString
            infoStr=infoStr || nl2 || "Cannot find property" pp(propName)"!"
            infoStr=infoStr || nl2 || "Available properties:" || nl2 || sortProps(props, hint2)
            .error~say( "-"~copies(79))
            .error~say( title"," infoStr)
            .error~say( "^"~copies(79) nl)
            .bsf.dialog~messageBox(infoStr, title, "error")
         end
         raise propagate
      end


         -- wrong case, replace first found correct case
      if tmpName<>propName then        -- case does not match!
      do
-- say "in ANY -3- tmpName"pp2(tmpName)"<>propName"pp2(propName)
         -- if prop name not found, but a single caseless matching one, then pick that property
         if .uno~xPropertySetAutoCase=.true, countPropNames(propName,props)=1  then
         do
            args=arg(2)       -- get array of arguments
            args[1]=tmpName   -- assign property name with a matching case
            FORWARD TO (uno.bsfObject) MESSAGE (xName) ARGUMENTS (args) CONTINUE
            return uno.wrap(result)
            -- return wrapResult(result)   --
         end

            -- show path to method/attribute, interrupt program
         title="Property Name Does Not Match Case!"

         infoStr=infoStr || "UNO object in hand:" nl2 || hint uno.bsfObject~toString
         infoStr=infoStr || nl2 || "Case of property" pp(propName) "does not match, did you mean:" pp(tmpName)"?"
         infoStr=infoStr || nl2 || "Available properties:" || nl2 || sortProps(props, hint2)
         .error~say( "-"~copies(79))
         .error~say( title"," infoStr)
         .error~say( "^"~copies(79) nl)
         .bsf.dialog~messageBox(infoStr, title, "error")

            -- try to resolve, if possible?
          if .uno~bAutoResolve<>.true | countPropNames(propName,props)<>1 then
             raise propagate    -- do not resolve, propagate condition (default)

            /* o.k., try to resolve on our own  */
         args=arg(2)       -- get array of arguments
         args[1]=tmpName   -- assign property name with a matching case
         FORWARD TO (uno.bsfObject) MESSAGE (xName) ARGUMENTS (args) CONTINUE
         return uno.wrap(result)
      end



         -- condition occurred while setting a new value, which is of a wrong type
      if xName~caselessEquals("setPropertyValue") then
      do
         newPropValue=arg(2)[2]     -- get the new value the property should be set to from the argument array

         signal on syntax name any_raise

         if .uno~xPropertySetAutoBox =.true then -- autobox property values with "setPropertyValue"
         do
             signal on syntax name PROPERTY_BOXING_CONDITION

             args=arg(2)      -- get array of arguments
               -- try to set property to the boxed value
             args[2]=getPropertyTypeOrBoxedValue(propName, props, newPropValue)
             FORWARD TO (uno.bsfObject) MESSAGE (xName) ARGUMENTS (args) CONTINUE
             return uno.wrap(result)
         end
         signal PROPERTY_BOXING_CONDITION
      end
   end


   -- assume: method/attribute not found, look for an interface that has it
   --         object, needle, returnString=.false, howManyResults (ignored), bExtendSearch
   --         returns a service object with the needed interface queried already or .nil else:
   o1=uno.findInterfaceWithMember(uno.bsfObject, xName, .false, 0, .true)

   if o1<>.nil then      -- o.k. there is an interface object that contains the method/attribute!
   do
-- say "in ANY -4- no interface found with desired member!"
         -- if we already have the correct interface object, then another error occurred
      if uno.getInterfaceName(o1)=uno.getInterfaceName(uno.bsfObject) then
      do
         if .uno~bShowErrorPopup=.true then
         do
            title="BSF Exception Occurred"

            if var("BSF_ERROR_MESSAGE") then
            do
               infoStr=bsf_error_message
               needle="getCause():"       -- if this exists, insert a newline after the needle
               parse var infoStr text (needle) msg
               if msg<>"" then            -- insert nl after needle
                  infoStr=text nl || hint2 needle nl || msg
            end
            else
               infoStr="No error information available."

            infoStr = infostr nl2 || "---" nl2 || "Object in hand:" nl || hint pp(uno.bsfObject~toString) -
                                           nl || "message:"        nl || hint pp(XName) -
                                           nl || arg(2)~items "argument(s):"  nl

            if arg(2)~items=0 then
               infoStr=infoStr || hint2 "n/a"
            do i over arg(2)        -- show
               infoStr=infoStr || hint2 pp(i) nl
            end

   -- infostr=infostr || nl2 || "line #" sigl":" pp(sourceline(sigl))

            .error~say( "-"~copies(79))
            .error~say( title"," infoStr)
            .error~say( "^"~copies(79) nl)
            .bsf.dialog~messageBox(infoStr, title, "error")
         end
         raise propagate
      end

      if .uno~bAutoResolveQueryInterface<>.true then
      do
-- say "in ANY -5- interface found with desired member, but autoresolving not configured!"
         if .uno~bShowErrorPopup=.true then
         do
               -- show path to method/attribute, interrupt program
            title="Method/Attribute Not Found!"
                -- get path to desired method/attribute
            s1=uno.findInterfaceWithMember(uno.bsfObject, xName, .true, 0, .true)
            s2=s1~changeStr(nl,"--- or ---")~changeStr(" ",nl || hint)
            if var("BSF_ERROR_MESSAGE") then
               infoStr="BSF exception occurred:" nl2 || bsf_error_message || nl || "---" nl2

            infoStr=infoStr || "UNO object in hand:" nl2 || hint uno.bsfObject~toString
            infoStr=infoStr || nl2 || "Path to desired method/attribute:" nl2 || hint || s2
            .error~say( "-"~copies(79))
            .error~say( title"," infoStr)
            .error~say( "^"~copies(79) nl)
            .bsf.dialog~messageBox(infoStr, title, "error")
         end

         if .uno~bAutoResolve<>.true then    -- if not behaving like OOo Basic, then show path & propagate the error
            raise propagate    -- do not resolve, propagate condition (default)
      end

      signal on syntax name any_raise
      FORWARD TO (o1) MESSAGE (xName) ARGUMENTS (arg(2)) CONTINUE
      return uno.wrap(result)
      -- return wrapResult(result)
   end
   raise propagate


/* boxing new value for setPropertyValue() did not work*/
PROPERTY_BOXING_CONDITION:

   -- o.k., so property name exists and case matches, hence probably an error in
   --       the supplied value
-- say "in ANY -6- PROPERTY_BOXING_CONDITION ..."
   signal on syntax name any_raise     -- make sure that we "raise propagate" if we get an exception in here
   propType=getPropertyTypeOrBoxedValue(propName, props)

   if .uno~bShowErrorPopup=.true then
   do
         -- show path to method/attribute, interrupt program
      title="Error Setting Property (Supplied Value Is of Wrong Type)"

      infoStr=infoStr || "UNO object in hand:" nl2 || hint uno.bsfObject~toString
      infoStr=infoStr || nl2 || "Supplied property name:"      || nl || hint pp(propName)
      infoStr=infoStr || nl  || "Expected type:"               || nl || hint pp(propType)
      infoStr=infoStr || nl  || "Supplied value (not matching expected type):" || nl || hint pp(newPropValue)

      .error~say( "-"~copies(79))
      .error~say( title"," infoStr)
      .error~say( "^"~copies(79) nl)
      .bsf.dialog~messageBox(infoStr, title, "error")
   end
   RAISE PROPAGATE    -- raise exception in caller


ANY_RAISE:            -- raise exception in caller
   RAISE PROPAGATE




-- local function
-- try to get Interface class object; if not available, load it ("lazy loading")
getInterfaceClass_original_position: procedure
   parse arg xName, idx

   if xName<>idx then xClass=.uno~entry(xName)  -- try to get interface class object, if already created
                 else xClass=.uno~entry(idx)    -- try to get interface class object, if already created

   if xClass=.nil then        -- no entry as of yet
   do
      if xName<>idx then      -- long name given, then use that
      do
         xClass=UNO.loadClass(.uno~XInterfaces~entry(xName), xName)
         if xClass<>.nil then -- o.k. also save with class name
         do
             .uno~setEntry(idx, xClass)
         end
      end
      else     -- only class name is given
      do
         xClass=UNO.loadClass(.uno~XInterfaces~entry(idx), idx)
         if xClass<>.nil then -- o.k., also save with full class name
         do
             .uno~setEntry(.uno~XInterfaces~entry(idx), xClass)
         end
      end
   end

   return xclass


/* Find given "propName" in encoded string of properties and return it.
   Ignores case when looking.
*/
findPropName: procedure
   parse arg propName, props

   pos=1                               -- start position
   do while pos<>0
      pos=props~caselessPos(propName"|", pos)   -- find propName
      if pos=1 then leave              -- found (first property in encoded string!)

         -- found if first character left of matching position is a blank (=delimiter)
      if pos>1, substr(props, pos-1, 1)=" " then leave
   end

   if pos=0 then return ""             -- indicate property not found

   parse var props =(pos) tmpName "|"  -- extract property name from encoded string
   return tmpName



/* Count number of times a given "propName" can be found with different case
   in encoded string of properties and return it.
*/
countPropNames: procedure
   parse arg propName, props

   count=0

   needle=propName"|"   -- make sure that we supply the trailing delimiter char "|"
   if props~caselessAbbrev(needle) then   -- very first property matches already!
      count+=1

   needle=" "||needle   -- from now on look for those that have a leading delimiter only
   count+=props~caselessCountStr(needle)
   return count



/* Extract property names from encoded string, return a (caseless) sorted,
   comma-delimited string of properties*/
sortProps: procedure
   parse arg props, hint

   tmpStr=""
   do while props<>""                     -- extract property names
      parse var props name "|" . props
      tmpStr=tmpStr name                  -- collect new property name
   end

   arr=tmpStr~strip~makearray(" ")        -- strip var, create array object
   arr~sortWith(.caselessComparator~new)  -- sort caselessly

   -- resStr=arr~toString(, ", ")            -- strip var, create array object
   resStr=arr~toString(, " ")            -- strip var, create array object


   tmpStr=""
   i=0
   maxWords=6                             -- maximum number of properties per line
   do while resStr<>""
      parse var resStr word resStr
      if tmpStr<>"" then
      do
         tmpStr=tmpStr", "
         if i=maxWords then
         do
            tmpStr=tmpStr || .endOfLine || hint
            i=0
         end
      end
      else tmpStr=hint

      tmpStr=tmpStr||word
      i=i+1
   end
   return tmpStr



/* Extract and return the given property's type, which is given in the encoded string.
   If optional argument "newValue" is given, then that value is boxed to the
   target Java class, if that class is a wrapper class for the primitive types.

   If ".nil" is supplied, then ".uno~nil" is returned; MAYBEVOID properties may be set to .nil,
   but if so, one must use UNO's any.VOID, i.e. ".uno~nil"
*/
getPropertyTypeOrBoxedValue: procedure
   use strict arg propName, props, newValue=.nil

   if arg(3,"e"), newValue=.nil then   -- supplied .nil, return UNO's .nil (any~void)
      return .uno~nil

      -- find property
   pos=1                               -- start position
   do while pos<>0
      pos=props~caselessPos(propName"|", pos)   -- find propName
      if pos=1 then leave              -- found (first property in encoded string!)

         -- found if first character left of matching position is a blank (=delimiter)
      if pos>1, substr(props, pos-1, 1)=" " then leave
   end

   if pos=0 then return "<PROPERTY NOT FOUND, CANNOT DETERMINE TYPE INFORMATION!>"   -- indicate property not found

   parse var props =(pos) tmpName "|" . "|" . "|" javaType ":" unoType ":" .  -- extract property name from encoded string

   -- if newValue="" then                 -- just return the type information?
   if arg(3,"o") then      -- no value given, then just return the type information
      return javaType -- "("unoType")"

   wrapperClasses="java.lang.Boolean java.lang.Byte    java.lang.Character" -
                  "java.lang.Short   java.lang.Integer java.lang.Long"      -
                  "java.lang.Float   java.lang.Double"

   if wordpos(javaType, wrapperClasses)=0 then  -- not of a boxable type
      return newValue

      -- extract unqualified name and use it for boxing the supplied value
   parse var javaType =(lastpos(".",javaType)+1) boxType
   return box(boxType, newValue)

-- try to get Interface class object; if not available, load it ("lazy loading")
::routine getInterfaceClass
   parse arg xName, idx

   signal on any

   if xName<>idx then xClass=.uno~entry(xName)  -- try to get interface class object, if already created
                 else xClass=.uno~entry(idx)    -- try to get interface class object, if already created

   if xClass=.nil then        -- no entry as of yet
   do
      if xName<>idx then      -- long name given, then use that
      do
         xClass=UNO.loadClass(.uno~XInterfaces~entry(xName), xName)
         if xClass<>.nil then -- o.k. also save with class name
         do
             .uno~setEntry(idx, xClass)
         end
      end
      else     -- only class name is given
      do
         xClass=UNO.loadClass(.uno~XInterfaces~entry(idx), idx)
         if xClass<>.nil then -- o.k., also save with full class name
         do
             .uno~setEntry(.uno~XInterfaces~entry(idx), xClass)
         end
      end
   end

   return xclass

any:
   return .nil







   -- interface to UnoRuntime.areSame(o1, o2)
::method uno.isSame
   use arg o
   return .uno~UnoRuntime~areSame(self, o) /* true if both reference the same UNO object   */


-- supply the UNO.-routines in form of methods as well, makes it easier to use them

   /* interface to RgfReflectUNO.findInterfaceWithMember(): can be a string or an object
      if "bReturnString=.false", then carry out a queryInterface() and return that object (or
      .nil); if a string should be returned ("bReturnString=.true"), then "howMany" determines
      after how many matches the search should be ended (default "-1", ie. try to find all
      matches)
   */
::method uno.findInterfaceWithMember
   use arg name, bReturnString, howMany
   return uno.findInterfaceWithMember(self, name, bReturnString, howMany)


   -- interface to RgfReflectUNO.getDefinition(): returns always a blank delimited string
::method uno.getDefinition
   return uno.getDefinition(self)


   -- 2008-09-09, ---rgf, return interface class name of current UNO proxy object
   --                     NOTE SINGULAR!
   --                     if "U"(nqualifeid is given as an argument, then the unqualified name is returned
::method uno.getInterfaceName
   parse arg unqualified
   return uno.getInterfaceName(self, unqualified)
/*
--   expose bsf.object
   use arg unqualified=""

   unqualified=(unqualified~strip~subChar(1)~upper="U")  -- get first character in uppercase

--   parse value bsf.object~toString with "Type[" interfaceClassName "]"
   parse value self~toString with "Type[" interfaceClassName "]"
   if \unqualified then       -- no args, return everything
      return interfaceClassName

      -- return unqualified name, if possible
   pos=lastpos(".", interfaceClassName)
   if pos>0 then
      return substr(interfaceClassName, pos+1)

   return interfaceClassName
*/


   -- interface to RgfReflectUNO.getInterfacesViaReflection(): returns always a blank delimited string
::method uno.getInterfaceNames
   return uno.getInterfaceNamesViaReflection(self)


   -- interface to RgfReflectUNO.getProperties(): returns always a blank delimited string
::method uno.getProperties
   return uno.getProperties(self)


   -- interface to RgfReflectUNO.getServicesViaReflection(): returns always a blank delimited string
::method uno.getServiceNames
   return uno.getServiceNamesViaReflection(self)


   -- interface to RgfReflectUNO.getType(): returns always a string
::method uno.getTypeName
   return uno.getTypeName(self)


   -- interface to RgfReflectUNO.getXTypeProviderTypes(): returns always a blank delimited string
::method uno.getXTypeProviderTypeNames
   return uno.getXTypeProviderTypeNames(self)


   -- interface to RgfReflectUNO.queryInterfaceName(): returns always a string
::method uno.queryInterfaceName
   use arg name
   return uno.queryInterfaceName(self, name)


   -- interface to RgfReflectUNO.queryInterfaceObjectByName(): returns the interface object or .nil
::method uno.queryInterfaceObjectByName
   use arg name
   return uno.queryInterfaceObjectByName(self, name)


   -- interface to RgfReflectUNO.queryServiceName(): returns always a string
::method uno.queryServiceName
   use arg name
   return uno.queryServiceName(self, name)



   -- 2008-09-09, rgf: add "supportsService" which is known to Basic programmers
   --                  "name" may be unqualified and in any case; returns .true/.false
::method  uno.supportsService
   use arg name
   return uno.supportsService(self, name)


   -- 2008-09-09, rgf: add "supportsInterface" for orthogonal reasons
   --                  "name" may be unqualified and in any case; returns .true/.false
::method  uno.supportsInterface public
   use arg name
   return uno.supportsInterface(self, name)



/* *************************************************************
   Class:      UNO_ARRAY_PROXY
   Purpose:    allows to wrap up a BSF_ARRAY object; this way we can make sure that
               its elements are wrapped up as uno proxy objects understanding the
               UNO_PROXY methods
   ************************************************************* */
::CLASS UNO_ARRAY_PROXY SUBCLASS UNO_PROXY
::method "[]"                    -- intercept access to array element
  forward message ("AT")         -- let AT do the work

::method at                      -- make sure to return a UNO_PROXY, such that we can send interface names as messages
  forward class (super) continue -- let superclass carry out its work (will be directed to BSF.CLS)
  return uno.wrap(result)        -- try to wrap up object as an UNO object

::method makearray               -- iterate over all elements and wrap them up as UNO_objects?
  forward class (super) continue -- let superclass carry out its work (will be directed to BSF.CLS)
  arr=result
  do i=1 to arr~items            -- iterate over all entries
     arr[i]=uno.wrap(arr[i])     -- wrap up each entry as an UNO proxy
  end

  return arr




/* *************************************************************
   Class:      UNO_DIRLIKE
   Purpose:    allows to make all UNO definitions for ENUM, CONSTANTS and PROPERTIES
               available via an ooRexx directory object, easying lookups considerably
   ************************************************************* */
::CLASS  UNO_DIRLIKE -- SUBCLASS Relation -- Directory
::method unoidl_name             -- fully qualified name of the enum/constant groupe
  expose unoidl_name
  return unoidl_name

::method "unoidl_name="          private -- setter, only accessible to this or subclasses
  expose  unoidl_name
  use arg unoidl_name

::method unoidl_definition       -- UNOIDL definition in form of a blank delimited string
  expose unoidl_definition
  return unoidl_definition

::method "unoidl_definition="    private -- setter, only accessible to this or subclasses
  expose  unoidl_definition
  use arg unoidl_definition

::method unoidl_typeName         -- UNOIDL typeName, will be .nil, if setup could not be carried out
  expose unoidl_typeName
  return unoidl_typeName

::method "unoidl_typeName="      private -- setter, only accessible to this or subclasses
  expose  unoidl_typeName
  use arg unoidl_typeName

::method unoDirectory            attribute private -- maps UNO_NAME->numValue, numValue->UNO_NAME, objectName->UNO_NAME

::method directory
  expose unoDirectory
  return unoDirectory~copy

::method unoNameQueue            attribute private -- collects UNO_NAMEs defined

::method nameQueue
  expose unoNameQueue
  return unoNameQueue~copy


::method init
       -- establish access to object attributes
    expose unoidl_name unoidl_definition unoidl_typeName unoDirectory unoNameQueue
    parse arg unoidl_name              -- parse argument as string

    unoidl_typeName=uno.getTypeName(unoidl_name)      -- get UNO type name

    unoDirectory=.directory~new
    unoNameQueue=.queue~new

    if self~setup=.false then  -- something went wrong, indicate it
    do
       -- drop unoidl_name unoidl_typeName               -- undefine
       unoidl_typeName=.nil            -- indicates that no content could be created
       unoidl_definition=pp(unoidl_name) "could not be successfully processed for this class" pp(self~class~string)
       return
    end


::method supplier    -- the supplier will show the enum-names in queue-order (starting out with the default value)
  expose unoDirectory unoNameQueue  -- enumRelation2NumericValues
  indexes=.array~new
  values =.array~new
  i=0
  do key over unoNameQueue
     i=i+1
     indexes[i]=key
     values[i] =unoDirectory~entry(key)
  end
  return .supplier~new(values,indexes)


::method makearray   -- the makearray will show the enum-names in queue-order (starting out with the default value)
  expose unoNameQueue   -- enumRelation2NumericValues
  return unoNameQueue~makearray

/*
  indexes=.array~new
  do i=1 to unoNameQueue~items
     indexes[i]=unoNameQueue[i]
  end
  return indexes
*/


::method "[]"
  forward message ("ENTRY")   -- let ENTRY method carry out the work


::method "AT"
  forward message ("ENTRY")   -- let ENTRY method carry out the work


::method entry                -- directory-like method
  expose unoDirectory
  parse arg index             -- make sure we have a string value
  forward to (unoDirectory) array (index)   -- let the directory object carry out the work


::method hasentry             -- directory-like method
  expose unoDirectory
  forward to (unoDirectory)   -- let the directory object carry out the work


::method unknown              -- directory-like method
  expose unoDirectory
  forward to (unoDirectory)   -- let the directory object carry out the work



/* USE BSF-routine "bsf.wrapStaticFields(strJavaClassName)" instead (available as of 2006-02-03) */

/* *************************************************************
   Class:      UNO_ENUM
   Purpose:    make all UNO_ENUMS available via an ooRexx directory object
   Remarks:    - each enum value is represented by a Java object
               - this implementation will return that Java object, enhanced with methods
                 that allow retrieving the name and the value (which is used for the required
                 string value)
               - there will be an additional entry "DEFAULT_ENUM_VALUE" created
               - Java-UNO expects enumeration values to be Java objects, which
                 are stored with the constant fields of the Java class representing
                 the UNOIDL-ENUM; this implementation is able to return the enum field name
                 representing the Java enum object;
               - sending the enum name or its numeric value will return the
                 appropriate Java-enum-object
               - to avoid DisposedExceptions for the enum values, each time an enum instance
                 is requested, it will be fetched from UNO transparently (hence using .directory
                 method objects to carry out that task) and enhanced with the methods
                 'name', 'value', 'obj' (the BSF_Reference object), 'string'; rgf, 2018-06-06

   ************************************************************* */
::CLASS UNO_ENUM SUBCLASS UNO_DIRLIKE PUBLIC
/* 2018-06-06, rgf: not placing the enum objects into the directory anymore (cf. comment 2010-07-15)
        caused the loss of the enhanced methods (eg. "name", "value"); this
        solution will request a new Enum object from UNO, but enhance the returned .BSF_Reference
        object with the methods defined in the new class attribute 'dirMethods' and return that
        enhanced object; this way the pre-2010-07-15 behaviour is reinstated and yet, no
        DisposedException should be caused;
*/
::attribute dirMethods CLASS    -- rgf, 20180606: methods for enhanced
::method init     class
  expose dirMethods
   -- define the methods an UNO_ENUM instance should get enhanced with
  dirMethods=.directory~new

  dirMethods~util.setup="expose  name value;             " - -- allow to fetch "name" and "value"
                        "use arg name, value;            "

  dirMethods~name      ="expose name;   return name;     "  -- access name
  dirMethods~value     ="expose value;  return value;    "  -- access value

  dirMethods~toString  ="expose name value; return self~objectName'['name'='value']'"  -- creates string value to indicate object

::method util.setMethods class      -- rgf, 20180607: set methods to instance, initialize method's attributes
  expose dirMethods
  use arg obj, nam, val
  do idx over dirMethods
     obj~util.setMethod(idx, dirMethods[idx], "FLOAT")   -- make sure attributes are in a separate scope
  end
  obj~util.setup(nam, val)
  return obj


/* Sets up the directory entries for this UNO_ENUM, will be invoked via UNO_DIRLIKE's constructor. */
::method setup    private   -- check and setup directory object
  if self~unoidl_typeName<>"UNO_ENUM" then          -- of correct UNO type ?
     return .false


  unoidl_name=self~unoidl_name
  self~unoidl_definition=uno.getDefinition(unoidl_name)  -- get the UNOIDL-definition

  enumDirectory=self~unoDirectory
  enumQueue    =self~unoNameQueue

  tmpArr=self~unoidl_definition~makearray(" ")        -- split string at blank character
  parse value tmpArr[1] with . "|" . "|" defVal rest  -- ENUM: default value, CONSTANTS: typeClass

  enumQueue~queue("DEFAULT_ENUM_VALUE")

  do i=2 to tmpArr~items                        -- iterate over ENUM values
     parse value tmpArr[i] with nam "|" val
       -- with UNO_ENUM use the Java object which is created by the Java UNO language binding
       -- and *not* the defined integer value
     /* rgf, 2010-07-15: make sure we always query the field value, otherwise it may be the case that
        Java UNO frees the bridge to UNO such that the cached Java object representing the UNO_ENUM value
        will throw a java_remote_bridge "com.sun.star.lang.DisposedException": */

     /* rgf, 20180606: not placing the enum objects into the directory anymore (cf. comment 2010-07-15)
        caused the loss of the enhanced methods (eg. "name", "value", "makestring"); instead each time
        an enum value gets requested the UNO Enumeration object will be requested as well and now enhanced
        with the methods 'obj' (returns .BSF object), 'name', 'value', 'string', and 'unknown' (forwarding
        unknown messages to the .BSF object)
        */
     -- methCode='return .object~enhanced(.uno_enum~dirMethods, bsf.getConstant("'unoidl_name'", "'nam'"), "'nam'", "'val'" )'
     methCode='return .uno_enum~util.setMethods(bsf.getConstant("'unoidl_name'", "'nam'"), "'nam'", "'val'" )'
     enumDirectory~setMethod(nam, methCode)     -- save method by Enum name; will lookup and return ENUM object at time of invocation
     enumDirectory~setMethod(val, methCode)     -- save method by Enum value; will lookup and return ENUM object at time of invocation

     enumQueue~queue(nam)                       -- queue enum-name
     if val=defVal then                         -- default value in hand?
     do
        enumDirectory~setMethod("DEFAULT_ENUM_VALUE", methCode)    -- save default value method in directory
     end
  end
  return .true                                  -- everything worked out o.k.


::method makestring                    -- supply a required string value
  m=.mutableBuffer~new
  m~~append(self~string)~~append("[")
  m~~append(self~unoidl_name)~~append("{")
  bComma=.false
  do idx over self~unoNameQueue
     if bComma then m~append(",")
               else bComma=.true       -- from now on insert a comma
     m~~append(idx)~~append("=")~~append(self~entry(idx)~value)
  end
  m~append("}]")
  return m~string



/* USE BSF-routine "bsf.wrapStaticFields(strJavaClassName)" instead (available as of 2006-02-03) */

/* *************************************************************
   Class:      UNO_CONSTANTS
   Purpose:    make all UNO_CONSTANTS name/value pairs available via an ooRexx directory object
   Remark:     - sending the constant name will return its (numeric) value
   ************************************************************* */
::CLASS UNO_CONSTANTS SUBCLASS UNO_DIRLIKE PUBLIC
::method setup private      -- check and setup directory object

  if self~unoidl_typeName<>"UNO_CONSTANTS" then     -- of correct UNO type ?
    return .false

  unoidl_name=self~unoidl_name
  self~unoidl_definition=uno.getDefinition(unoidl_name)  -- get the UNOIDL-definition

  enumDirectory=self~unoDirectory
  enumQueue    =self~unoNameQueue

  tmpArr=self~unoidl_definition~makearray(" ")

  do i=2 to tmpArr~items                        -- iterate over CONSTANT values
     parse upper value tmpArr[i] with name "|" val "|" typeName
     enumDirectory[name]=val
     enumDirectory[name".TYPENAME"]=typeName
     enumDirectory[val] =name
     enumDirectory[val".TYPENAME"] =typeName
     enumQueue~queue(name)                      -- queue enum-name
  end
  return .true                                  -- everything worked out o.k.


/* Decodes the received number (a result of adding up constant values of this class)
   into a string containing the constant field names that are set in the received number.
*/
::method decode
  parse arg number         -- retrieve the number, can consists of added constants
  maxDigits=20
  numeric digits maxDigits -- make sure that we have enough digits to exactly represent 2**64+1

     -- turn decimal number into hexadecimal value
  if number<0 then
     tmpVal=number~d2c(maxDigits)
  else
     tmpVal=number~d2c

  lenTV=length(tmpVal)     -- length of number

  tmpStr=""
  do key over self         -- iterate over all keys
     tmp=self~entry(key)               -- get decimal value for key
     if tmp<0 then tmpKey=tmp~d2c(maxDigits) -- turn decimal value into character
              else tmpKey=tmp~d2c

     lenTK=length(tmpKey)  -- lenght of key
     if lenTK>lenTV then   -- constant bigger than available number, leave
        leave

     if bitand(right(tmpVal, lenTK), tmpKey)=tmpKey then
        tmpStr=tmpStr key
  end
  return tmpStr~strip      -- remove leading/trailing blanks


/* Encodes given blank delimited list of constant field names into appropriate decimal number.
   Optionally, one can intermix the list of constant field names with decimal numbers (standing
   for a constant).
*/
::method encode
  parse arg list
  list=changestr("+", list, " ")       -- if plus-signs are supplied, replace them with blanks
  numeric digits 20
  res=0
  do while list<>""                    -- process blank-delimited list
     parse var list key list           -- extract constant field name/decimal value
     if datatype(key, "Numeric") then  -- if numeric, use value to add
        res=res+key
     else
        res=res+self~entry(key)
  end
  return res                           -- return resulting value


::method makestring                    -- supply a required string value
  m=.mutableBuffer~new
  m~~append(self~string)~~append("[")
  m~~append(self~unoidl_name)~~append("{")
  bComma=.false
  do idx over self~unoNameQueue
     if bComma then m~append(",")
               else bComma=.true       -- from now on insert a comma
     m~~append(idx)~~append("=")~~append(self~entry(idx))
  end
  m~append("}]")
  return m~string



/* *************************************************************
   Class:      UNO_PROPERTIES
   Purpose:    make all UNO_PROPERTY definitions via an ooRexx directory object
   Remark:     - sending the property name will return its encoded UNOIDL string, which
                 needs to be parsed for further processing
   ************************************************************* */
::CLASS UNO_PROPERTIES SUBCLASS UNO_DIRLIKE -- PUBLIC
::method setup       -- check and setup directory object

    if self~unoidl_typeName<>"" then                  -- of correct UNO type ?
       return .false

    encodedProps=self~unoidl_name                     -- get argument
    parse var encodedProps . "|" type "|" .           -- check for encoded properties
    if type<>"UNO_PROPERTY" then
       return .false

    self~unoidl_name=""                               -- indicate no UNOIDL name available
    self~unoidl_typeName="UNO_PROPERTY"               -- indicate kind of directory

    self~unoidl_definition=encodedProps               -- save encoded property string

    tmpArr=encodedProps~makearray(" ")

    do i=1 to tmpArr~items                            -- iterate over CONSTANT values
       parse value tmpArr[i] with name "|" .
       self~setentry(name, tmpArr[i])                 -- save encoded property info
    end
    return .true                                      -- everything worked out o.k.



  -- wrap argument into an UNO-proxy object, if it is a BSF object, else return argument unchanged
::routine UNO.WRAP public
/* Check value; if an instance of .BSF, then
      a) for a UNO-null value (.uno~nil), return .nil
      b) for a BSF_ARRAY, return a UNO_ARRAY_PROXY
      c) else return a UNO_PROXY
*/
-- wrapResult: procedure -- wrap up resulting object as a UNO_[ARRAY_]PROXY, if necessary
   use arg tmpObj

      -- observation: a .uno~any~void will always have the same ooRexx objectname
      --              as the BSF object (as on the Java side both are the same object);
      --              however this could be implementation dependent and may change
      --              hence using UNO itself to find out whether both objects are the same
      --              even if it slower than just comparing the OBJECTNAMEs, like in the
      --              next (commented) statement
   if tmpObj~objectName=.uno~nil_name then   -- if a UNO-null supplied, return an ooRexx .nil
      return .nil
/* The following version should work in any case, but slows down the process.
   if uno.areSame(tmpObj,.uno~nil) then   -- if a UNO-null supplied, return an ooRexx .nil
      return .nil
*/

   -- if an uno_proxy already in hand, then it is already wrapped, so just return it
   if tmpObj~isA(.uno_proxy) then
   do
--      say "*** BINGO-uno.wrap() - unchanged" "tmpObj:" pp2(tmpObj) "<--- "~copies(5)
      return tmpObj
   end

      -- if a string given that is led-in with the object indicator "<O>",
      -- then try to get a BSF-proxy for it
   if tmpObj~isA(.string), tmpObj~left(3)="<O>" then
   do
      tmpObj=bsf.wrap(tmpObj)
   end

   -- if a BSF-object, then wrap it up
   if tmpObj~isA(.bsf) then
   do
        -- if a BSF_ARRAY was returned wrap it up as an UNO_ARRAY_PROXY
         -- ".BSF_array_reference" is not a public class, hence depending on ID-string
      if tmpObj~class~id~caselessEquals("BSF_ARRAY_REFERENCE") then
         return uno.wrapArray(tmpObj)

      return .UNO_proxy~new(tmpObj)          -- plain BSF object, wrap it up
   end

   return tmpObj



/*
/* up to 2008-09-12, rgf */

  if .uno~proxyclasses~hasindex(arg(1)~class) then    -- already wrapped up
     return arg(1)

  if arg(1)~hasmethod("BSF.EXIT") then
  do
     if abbrev(arg(1)~class~id, "BSF_ARRAY") then
        return uno.wrapArray(arg(1))

     return .UNO_proxy~new(arg(1))        -- wrap it up as an UNO_PROXY
  end

/* 2008-06-22, rgf: probably not necessary anymore as a bsf.wrap() should have occurred already
  o=bsf.wrap(arg(1))    -- hmm, maybe not yet wrapped up as a BSF-object?
  if o~hasmethod("BSF.EXIT") then
  do
     if abbrev(o~class~id, "BSF_ARRAY") then
        return uno.wrapArray(o)

     return .UNO_proxy~new(o) -- o.k. a BSF object
  end
*/

  return arg(1)                                 -- return argument as is (unwrapped)
*/






/* Routine that determines whether a BSF array should be wrapped up as an UNO array object,
   which will cause any object that gets accessed from the array via UNO to be wrapped up
   itself as an UNO_PROXY object (allowing to send the name of interfaces to retrieve the
   desired interface object).

   It will not wrap up BSF arrays that have either primitive datatypes as their component
   type, or classes that start with "java" (which are regarded to belong to Java not to UNO).
*/
::routine UNO.WRAPARRAY
  use arg uno.bsfObject

  parse value uno.bsfObject~string with leadin "@" .   -- get lead-in
  pos=verify(leadin , "[")
  if pos>0 then   -- o.k. array symbols are there
  do
     if length(leadin)<>pos then   -- not a primitive dataype in hand
     do
        if abbrev(substr(leadin, pos), "Ljava")=.false then    -- maybe a UNO class, so wrap it up
        do
           return .uno_array_proxy~new(uno.bsfObject)  -- wrap it up as an UNO array object
        end
     end
  end

  return uno.bsfObject



   -- convert a file-name with path to URL notation as needed by UNO
::routine ConvertToUrl public    -- named after {Star|OOo}Basic
  parse arg str
  return uno.ConvertToUrl(str)

::routine uno.ConvertToUrl public   -- 2007-01, replaces "ConvertToUrl()"
  parse arg str
  return (iif(left(str,1)="/", "file://", "file:///") || encodeURL(str))~changestr("\", "/")


   -- convert a file-name with path encoded in the URL notation to an operating system compliant file with path
::routine ConvertFromUrl public  -- named after {Star|OOo}Basic
  parse arg str
  return uno.ConvertFromUrl(str)

::routine uno.ConvertFromUrl public  -- 2007-01, replaces "ConvertFromUrl()"
  parse arg str
  new=decodeURL(str)             -- decode URL (replace escapes with the escaped chars themselves

  if left(new,5)="file:" then
  do
     dosLike= (substr(new, 10,1)=":")  -- if 10th char a colon, assume DOS-like filename

     if dosLike=.true then
        new=substr(new, 9)
     else      -- leave initial slash
        new=substr(new, 8)

      -- if a DOS-like path and on a DOS-like system, translate slashes to backslashes
     if dosLike=.true | .uno~file.separator="\" then
        return changestr("/", new, "\")
  end
  return new



::routine encodeURL public
  parse arg str

  new=changestr("%", str, "%25")    -- escape escape char %

   -- determine chars in URLs that do not get encoded
  url.chars="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789/\?:=%'"".,+-"

  pos=verify(new, url.chars)     -- chars to escape in string?
  do while pos<>0
     char=substr(new, pos, 1)    -- extract char not in reference
     new=new~changestr(char, "%"||char~c2x)  -- escape character
     pos=verify(new, url.chars)
  end
  return new




   -- re-create chars from escape-sequences led in by % followed by two-digit hex value
::routine decodeUrl public    -- %20 --> " "
  parse arg str

  new=""
  do while str<>""
     parse var str before "%" +1 hexValue +2 str -- extract hex value
     new=new || before || hexValue~x2c
  end
  return new




   -- interface to UnoRuntime.areSame(o1, o2)
::routine uno.areSame public
   use arg o1, o2
   return .uno~UnoRuntime~areSame(o1, o2)     /* true if both reference the same UNO object   */


/* Pass-through routines for "org.oorexx.uno.RgfReflectUNO", wrapping up
   returned UNO objects into UNO_PROXY ooRexx objects */
   -- interface to RgfReflectUNO.findInterfaceWithMember(): can be a blank delimited string or an interface object
::routine uno.findInterfaceWithMember public
   use arg o, name, bREturnString, howMany

   if datatype(bReturnString, "Boolean")<>.true then
      bReturnString=.true                       -- default to return a blank delimited string

   if datatype(howMany, "Whole")<>.true then
      howMany=-1                                -- default to return all matches

      -- wrap up as an UNO proxy, if an object got returned
   return uno.wrap( .uno~rgfReflectUNO~findInterfaceWithMember( o, name, bReturnString, howMany, .uno~bExtendSearch) )


   -- interface to RgfReflectUNO.getDefinition(): returns always a blank delimited string
::routine uno.getDefinition public
   use arg o
-- trace i
-- say pp(.uno~rgfReflectUNO~toString)
   return .uno~rgfReflectUNO~getDefinition(o)


   -- 2008-09-09, ---rgf, return interface class name of current UNO proxy object
   --                     NOTE SINGULAR!
::routine uno.getInterfaceName public
   use arg o, unqualified=""

   unqualified=(unqualified~strip~subChar(1)~upper="U")  -- get first character in uppercase

   parse value o~toString with "Type[" interfaceClassName "]"

   if \unqualified then       -- no args, return everything
      return interfaceClassName

   parse var interfaceClassName =(lastpos('.',interfaceClassName)+1) unqualifiedName
   return unqualifiedName

/* 2008-09-12, rgf, old:
      -- return unqualified name, if possible
   pos=lastpos(".", interfaceClassName)

   if pos>0 then
      return substr(interfaceClassName, pos+1)

   return interfaceClassName
*/


   -- interface to RgfReflectUNO.getInterfacesViaReflection(): returns always a blank delimited string
::routine uno.getInterfaceNamesViaReflection public
   use arg o
   return .uno~rgfReflectUNO~getInterfaceNamesViaReflection(o)


   -- interface to RgfReflectUNO.getProperties(): returns always a blank delimited string
::routine uno.getProperties public
   use arg o
   return .uno~rgfReflectUNO~getProperties(o)


   -- interface to RgfReflectUNO.getServicesViaReflection(): returns always a blank delimited string
::routine uno.getServiceNamesViaReflection public
   use arg o
   return .uno~rgfReflectUNO~getServiceNamesViaReflection(o)


   -- interface to RgfReflectUNO.getType(): always a string
::routine uno.getTypeName public
   use arg o
   return .uno~rgfReflectUNO~getTypeName(o)


   -- interface to RgfReflectUNO.getXTypeProviderTypes(): returns always a blank delimited string
::routine uno.getXTypeProviderTypeNames public
   use arg o
   return .uno~rgfReflectUNO~getXTypeProviderTypeNames(o)


   -- interface to RgfReflectUNO.queryInterfaceName(): always a string
::routine uno.queryInterfaceName public
   use arg o, name
   return .uno~rgfReflectUNO~queryInterfaceName(o, name, .uno~bExtendSearch)-- always a string


   -- interface to RgfReflectUNO.queryInterfaceObjectByName(): returns the interface object or .nil
::routine uno.queryInterfaceObjectByName public
   use arg o, name
      -- wrap up as an UNO proxy, if an object got returned
   return uno.wrap( .uno~rgfReflectUNO~queryInterfaceObjectByName(o, name, .uno~bExtendSearch) )


   -- interface to RgfReflectUNO.queryServiceName(): always a string
::routine uno.queryServiceName public
   use arg o, name
   return .uno~rgfReflectUNO~queryServiceName(o, name)


   -- 2008-09-09, rgf: add "supportsService" which is known to Basic programmers
   --                  "name" may be unqualified and in any case; returns .true/.false
::routine uno.supportsService public
   use arg o, name
   return .uno~rgfReflectUNO~queryServiceName(o, name)<>""


   -- 2008-09-09, rgf: add "supportsInterface" for orthogonal reasons
   --                  "name" may be unqualified and in any case; returns .true/.false
::routine uno.supportsInterface public
   use arg o, name
   return .uno~rgfReflectUNO~queryInterfaceName(o, name, .uno~bExtendSearch)<>""



   -- "pretty print" strings from RgfReflectUNO: uses a blank to create an array from the string
   --                first entry is untoched, all others are delimited with CR-LF-TAB for indentation
::routine ppd public
  parse arg debugString, delimiter, replacement

  if arg(2, 'Omitted') then   -- default to a blank as delimiter
     delimiter=" "

  if arg(3, 'Omitted') then   -- add TAB character for indentation
     replacement=.uno~line.separator || "09"x

  return changestr(delimiter, debugString, replacement)




/*
    static String OOREXX_XSCRIPTCONTEXT_KEY = "OOo.xScriptContext";
    static String OOREXX_VERSION_KEY        = "OOo.ooRexxScriptProvider.version";
    static String OOREXX_FILENAME           = "OOo.fileName";         // 20070920
    static String OOREXX_SCRIPTMETADATA     = "OOo.ScriptMetaData";   // 20070920
*/
::routine uno.getScriptContext public     -- will be called from an ooRexx macro
   use arg hashTable=.nil           -- rgf, 20091031

   signal on syntax                 -- activate syntax handling
   if hashTable=.nil then
      scriptContext=uno.wrap(bsf.lookupBean("<BSFRegistry-kEy>OOo.xScriptContext"))
   else
      scriptContext=uno.wrap(hashTable~get("OOo.xScriptContext"))

   if .nil<>scriptContext then   -- make sure RgfReflectUNO has XComponentContext set
   do    -- use the XComponentContext received by the scripting framework, if not set already
      if .uno~rgfReflectUno~getContext=.nil then
         .uno~rgfReflectUNO~setContext(scriptContext~getComponentContext)
   end
   return scriptContext

syntax:
   return .nil

-- ::routine uno.getScriptContextVersion public
::routine uno.getScriptProviderVersion public   -- returns the OOo component file name
   use arg hashTable=.nil           -- rgf, 20091031

   if hashTable=.nil then
      return bsf.lookupBean("<BSFRegistry-kEy>OOo.ooRexxScriptProvider.version")
   else
      return hashTable~get("OOo.ooRexxScriptProvider.version")


::routine uno.getScriptFileName public          -- returns the OOo component file name
   use arg hashTable=.nil           -- rgf, 20091031

   if hashTable=.nil then
      return bsf.lookupBean("<BSFRegistry-kEy>OOo.fileName")
   else
      return hashTable~get("OOo.fileName")


/* metaData-methods (from source code as no IDL exists for it; rgf, 2008-06-10):

   getClassPath            ... returns an array of type java.net.URL

   getDescription          ... returns String (location: ScriptEntry)
   getLanguage             ... returns String (location: ScriptEntry)
   getLanguageName         ... returns String (location: ScriptEntry)
   getLanguageProperties   ... returns a java.util.Map (location: ScriptEntry)
   getLocation             ... returns String (location: ScriptEntry)
   getLogicalName          ... returns String (location: ScriptEntry)
   setLogicalName          ... sets String (location: ScriptEntry)

   getLocationPlaceHolder  ... returns String (location: ScriptMetaData)
   getParcelLocation       ... returns String (location: ScriptMetaData)
   getScriptFullURL        ... returns String (location: ScriptMetaData)

   getShortFormScriptURL   ... returns String (location: ScriptMetaData)]

   getSourceURL            ... returns a java.net.URL, has e.g. method "getFile()" (location: ScriptMetaData)
   hasSource               ... returns boolean (location: ScriptMetaData)

   loadSource              ... returns nothing(location: ScriptMetaData), needs
                               to be done in order for the following methods to function;
                               also: invoke this method if "hasSource()" returns .false
      getSource            ... returns String (location: ScriptMetaData)
      getSourceBytes       ... returns byte array (location: ScriptMetaData)
*/
::routine uno.getScriptMetaData public          -- returns an UNO object
   use arg hashTable=.nil           -- rgf, 20091031

   if hashTable=.nil then
      return uno.wrap(bsf.lookupBean("<BSFRegistry-kEy>OOo.ScriptMetaData"))
   else
      return uno.wrap(hashTable~get("OOo.ScriptMetaData"))


/* If a script was invoked via the OOo macro editor, then this routine will return the
   ScriptSourceModel() in charge, which has the following useful public routines:

   clearErrorLine          ... clear the error indicator
   indicateErrorLine(n)    ... set error indicator in editor to line "n" (1-based)
*/
::routine uno.getScriptSourceModel public
   use arg hashTable=.nil           -- rgf, 20091031

   if hashTable=.nil then
      return uno.wrap(bsf.lookupBean("<BSFRegistry-kEy>OOo.ScriptSourceModel"))
   else
      return uno.wrap(hashTable~get("OOo.ScriptSourceModel"))


/*
*/
::routine uno.getScriptPath public
  use strict arg uri=.nil, hashTable=.nil

  if uri=.nil then      -- o.k. no URI given, get the one stored in metaData
  do
     if hashTable=.nil then
        metaData=uno.getScriptMetaData()  -- will only work, if we are running for a script
     else
        metaData=uno.getScriptMetaData(hashTable)  -- will only work, if we are running for a script

     if metaData=.nil then             -- this is not running as a script
        return .nil

     location=metaData~getLocationPlaceHolder  -- e.g. "user", "share", "document"
     parse var location tmpLoc ":" .      -- if script from a parcel, then location may be compound

     if pos(tmpLoc, "user share")=0 then  -- not in a location that can be translated into a system path
        return .nil

     uri=metaData~getParcelLocation -- get this script's parcel location
  end

   -- now get the script's content
  xContext=uno.getScriptContext()~getComponentContext

  xme= xContext~getValueByName("/singletons/com.sun.star.util.theMacroExpander")~XMacroExpander
  tmpPath=xme~expandMacros(uri)              -- expand script uri
  expandUri="vnd.sun.star.expand:"
  if pos(expandUri, tmpPath)>0 then          -- an expand portion, if so get rid of it
     parse var tmpPath (expandUri) tmpPath   -- remove expandUri

  -- use UNO's file converter
  service="com.sun.star.ucb.FileContentProvider"
  xMCf = xContext~getServiceManager   -- retrieve XMultiComponentFactory
  xFileConverter=xMCF~createInstanceWithContext(service,xContext) ~XFileIdentifierConverter
  return xFileConverter~getSystemPathFromFileURL(tmpPath) -- convert to OpSys' convention



/* Allows to extend/set an environment variable. By default "PATH" is addressed, and if
   the first argument is omitted, then either the script's path is used, or if not a script,
   then the current directory is used.
*/
::routine uno.addPath public
  use strict arg addPath=.nil, environmentVar="PATH"

  if addPath="" then return ""      -- empty string, nothing to do

  if addPath=.nil then
  do
     addPath=directory()   -- default: use current directory as new path
     scriptPath=uno.getScriptPath() -- is this running for a script, if so use its path
     if scriptPath<>.nil then       -- script path available, use it
        addPath=scriptPath
  end

  oldValue=value(environmentVar, , "ENVIRONMENT")    -- get current value, if any
  tmpValue=oldValue

  pathSeparator=.uno~path.separator -- get path.separator from Java
  do while tmpValue<>""             -- test for existence, if so, prematurely return
     parse var tmpValue chunk (pathSeparator) tmpValue

     if pathSeparator=";" then      -- assume Windows, case independent file system
        bExistsAlready=(addPath~upper = chunk~upper)
     else                           -- do a case sensitive comparison
        bExistsAlready=(addPath=chunk)

     if bExistsAlready then         -- o.k. path is already on environment variable, don't add it another time
        return ""                   -- indicate that nothing got added
  end

  newValue= oldValue || pathSeparator || addPath     -- create new value, append path
  call value environmentVar, newValue, "ENVIRONMENT"  -- set new value

  -- return (oriValue <> value(environmentVar,,"ENVIRONMENT"))   -- return .true, if it worked, .false else
  -- return oldValue                   -- return old value as may be expected from the VALUE()-BIF
  return addPath                    -- return the effective path that got added



/* Allows to remove a path from an environment variable. By default "PATH" is addressed, and if
   the first argument is omitted, then either the script's path is used, or if not a script,
   then the current directory is used.

   If the environment variable would be empty after removing the path, then it gets deleted.
*/
::routine uno.removePath public
  use strict arg removePath=.nil, environmentVar="PATH"

  if removePath="" then return ""   -- empty string, nothing to do

  if removePath=.nil then
  do
     removePath=directory()   -- default: use current directory as new path
     scriptPath=uno.getScriptPath()    -- is this running for a script, if so use its path
     if scriptPath<>.nil then          -- script path available, use it
        removePath=scriptPath
  end

  oldValue=value(environmentVar, , "ENVIRONMENT")    -- get current value, if any
  tmpValue=oldValue

  pathSeparator=.uno~path.separator -- get path.separator from Java

  newPath=""
  chunks=oldValue~makeArray(pathSeparator)   -- create an array of paths
  bRemove=.false
  do chunk over chunks
     if pathSeparator=";" then      -- assume Windows, case independent file system
        bRemove=(removePath~upper = chunk~upper)
     else                           -- do a case sensitive comparison
        bRemove=(removePath=chunk)

     if \bRemove then
     do
        if newPath<>"" then         -- if not empty, append path separator
           newPath=newPath||pathSeparator
        newPath=newPath || chunk    -- add path to string
     end
  end

  if \bRemove then                  -- path was not removed
     return ""                      -- indicate that nothing got removed

  if newPath="" then                -- path gets empty, remove environment variable altogether
     call value environmentVar, .nil, "ENVIRONMENT"
  else
     call value environmentVar, newPath, "ENVIRONMENT"

  return removePath                 -- return effective path that got returned





/* Create a BSF-array and wrap it up as an UNO_ARRAY_PROXY adding the
   behaviour of wrapping up its elements as UNO_PROXY elements, such that
   messages starting with an "X" are interpreted as ones which query
   an interface object.
*/
::routine uno.createArray              public
      -- create and send message to .BSF class supplying the received arguments
   res=.message~new(.bsf, "BSF.CREATEARRAY", "ARRAY", arg(1, "ARRAY"))~send
   if abbrev(res~class~id, "BSF_ARRAY") then
      return uno.wrapArray(res)

   return res


/* Returns the fully qualified, mixed case name of the interface. Either pass in
   the case-independent unqualified or fully qualified name of the interface.

   If an unqualified interface name matches duplicates they are returned, delimited
   with the char(acter sequence) as denominated by the second argument. If the second
   argument is missing, then a single space character (" ") is used instead.

*/
::routine uno.getCachedInterfaceName   public   --rgf, 2006-09-03, ---rgf, 2007-01-14
  parse upper arg name, needle

  tmpStr=.uno~XInterfaces~entry(name)  -- unknown interface
  if .nil=tmpStr then return tmpStr

  if ARG(2, "O") then      -- Omitted, then default to CR-LF
  do
     needle=" "         -- default to single space
     -- needle="0d0a"x
  end

  do entry over .uno~XInterfaces.dupes~allAt(name)
     tmpStr=tmpStr || needle || entry
  end
  return tmpStr

/*


---
  a=.uno~xinterfaces
  .uno~XInterfaces

   if xClass=.nil then        -- no entry as of yet
   do
      if xName<>idx then      -- long name given, then use that
      do
         xClass=UNO.loadClass(.uno~XInterfaces~entry(xName), xName)
         if xClass<>.nil then -- o.k. also save with class name
         do
             .uno~setEntry(idx, xClass)
         end
      end
      else     -- only class name is given
      do
         xClass=UNO.loadClass(.uno~XInterfaces~entry(idx), idx)
         if xClass<>.nil then -- o.k., also save with full class name
         do
             .uno~setEntry(.uno~XInterfaces~entry(idx), xClass)
         end
      end
   end

   return xclass

---
   if idx~left(1)="X" then    -- if an interface object is asked for, query and return it
   do
      xClass=getInterfaceClass(xName, idx)

         -- query and return the interface object
      interface=.uno~unoRuntime~queryInterface(xClass, uno.bsfObject)
      if interface=.nil then  -- not found, maybe a duplicate simple class name ?
      do
         do fullName over .uno~XInterfaces.dupes~allAt(idx)
            idx=substr(fullName, lastpos(".", fullName)+1)   -- getindex
            xClass=getInterfaceClass(fullName, idx)      -- rgf, 2006-06-04

            if .nil<>xClass then
               interface=.uno~unoRuntime~queryInterface(xClass, uno.bsfObject)

            if .nil<>interface then
               return .UNO_proxy~new(interface)
         end

         -- now try reflection (via "RgfReflectUNO"), maybe a new interface, not in UNO_XINTERFACES.REX
         res=.uno~RgfReflectUno~queryInterfaceObjectByName(uno.bsfObject, xName, .uno~bExtendSearch )

         if .nil<>res then
            return .uno_proxy~new(res)

         return .nil    -- no interface object available
      end

      return .UNO_proxy~new(interface)
   end
*/




/* In case one needs to create a property value object and knows already the name
  (and optionally the value), then this routine eases the creation somewhat.
  ---rgf, 2008-07-20
*/
::routine uno.createProperty public
  use strict arg name, value=.nil

  prop=.uno~propertyValue~new          -- create PropertyValue object
  prop~name=name                       -- define name
  prop~value=value                     -- set value
  return prop                          -- return property






/* ------------------------------------------------------------------------------- */
   /* represents an OOo macro parcel-descriptor, rgf, 2010-06-05    */
   /* makes it easy for Rexx programmers to create a parcel-descriptor from scratch or
      from an existing file, manipulate it and regenerate the file's content using the
      'toString' method explicitly */
::class "Parcel" public
      -- class methods --
::method createScriptDefinition class  -- args: name[,scriptLanguageName], returns a script definition object
  forward message ("NEW") TO (.ParcelScriptDefinition)

::method fromXMLFile class    -- reads parcel-descriptor file and creates script definition objects
  use strict arg path2parcelDescriptor, scriptLanguageName="ooRexx"

  parcel=self~new          -- create a parcel
  if stream(path2parcelDescriptor, "C", "query exists")="" then
  do
     parcel~language=scriptLanguageName
     return parcel         -- return empty parcel
  end

  parcelScriptDefinitions=parcel~ParcelScriptDefinitions -- get script directory

      -- read entire file
  data=charin(path2parcelDescriptor,1,chars(path2parcelDescriptor))
  call stream path2parcelDescriptor, "C", "close"  -- make sure stream is closed

  parse var data "<parcel" parcelInfo ">" xmlParcelScriptDefinitions "</parcel>"
  parse var parcelInfo 'language="'language'"'
  parcel~language=language -- save parcel's language

  do while xmlParcelScriptDefinitions<>""
      -- extract script def
     parse var xmlParcelScriptDefinitions "<script" +0 xmlScriptDef "</script>" xmlParcelScriptDefinitions
     if xmlScriptDef<>"" then
     do
        so=.ParcelScriptDefinition~fromXML(xmlScriptDef) -- create a script object from definition
        parcelScriptDefinitions~put(so,so~functionName)  -- save script object in parcel's directory
     end
  end

  return parcel         -- return parcel


      -- instance methods --
::method init           -- constructor
  expose ParcelScriptDefinitions language
  use strict arg language="ooRexx"
  ParcelScriptDefinitions =.directory~new

::attribute ParcelScriptDefinitions -- directory of script descriptions
::attribute language                -- the parcel's script language

::method items                      -- return number of scripts in parcel
  expose ParcelScriptDefinitions
  forward to (ParcelScriptDefinitions)

::method addScriptDefinition        -- args: name, scriptDefinitionObject
  expose ParcelScriptDefinitions
  use arg scriptDefinitionObject
  forward message ("put") to (ParcelScriptDefinitions) array (scriptDefinitionObject, scriptDefinitionObject~functionName)

::method getScriptDefinition        -- args: name; returns script definition or .nil
  expose ParcelScriptDefinitions
  forward message ("AT") to (ParcelScriptDefinitions)

::method removeScriptDefinition     -- args: name; removes and returns script definition or .nil
  expose ParcelScriptDefinitions
  forward message ("REMOVE") to (ParcelScriptDefinitions)

::method toString                   -- create string representation
  expose ParcelScriptDefinitions language
  res=.MutableBuffer~new

  lf="0d0a"x

  tmp='<?xml version="1.0" encoding="UTF-8"?>'                      || lf ||  lf ||-
      '<parcel language="'language'" xmlns:parcel="scripting.dtd">' || lf
  res~append(tmp)       -- append prolog

      -- order scripts by name caselessly
  arr=ParcelScriptDefinitions~allIndexes~sortWith(.CaselessComparator~new)
  do i=1 to arr~items
     if i>1 then res~append(lf)     -- add blank line to make locating individual scripts in editors a little bit easier
     res~append(ParcelScriptDefinitions[arr[i]]~toString)   -- add script's string representation
  end

  res~append("</parcel>")
  return res~string     -- return rendered as string


/* ------------------------------------------------------------------------------- */
   /* represents a single script definition  */
::class "ParcelScriptDefinition"
      -- class methods --
::method fromXML class
  use strict arg xmlString

   -- extract functionName
  parse var xmlString '<functionname value="' functionName '"'


   -- extract script type
  parse var xmlString '<script' info '>'
  parse var info 'language="' scriptLanguage '"'

   -- create the Parcel itself
  -- scriptDef=.ParcelScriptDefinition~new(functionName, scriptLanguage)   -- create scriptDef instance
  scriptDef=self~new(functionName, scriptLanguage)   -- create scriptDef instance
  scriptDef~scriptLanguage=scriptLanguage

   -- extract lang, displayname, description, if any
  parse var xmlString '<locale' attr '>' info '</locale>'
  parse var attr      'lang="' lang '"'
  if lang<>"" then scriptDef~lang=lang
  parse var info      '<displayname value="' displayName '"'
  if displayName<>"" then scriptDef~displayName=displayName
  parse var info      '<description>' description '</description>'
  if description<>"" then
  do
     -- description=strip(description, 'Both')  -- strip blanks
     pos=verify(description, " "||"0d0a09"x)        -- remove leading CR-LF, if any
     if pos>0 then description=substr(description, pos)

     revDescr=reverse(description)
     pos=verify(revDescr, " "||"0d0a09"x)    -- remove trailing CR-LF, if any
     if pos>0 then revDescr=substr(revDescr, pos)

     scriptDef~description=reverse(strip(revDescr))
  end

   -- extract logicalName
  parse var xmlString '<logicalname value="' logicalName '"'
  if logicalName<>"" then scriptDef~logicalName=logicalName

  return scriptDef

      -- instance methods --
::method init        -- constructor
  expose scriptLanguage lang description displayName functionName logicalName
  use strict arg functionName="n/a.rxo", scriptLanguage="ooRexx"

  lang="en"          -- default: "en"glish (ISO abbreviation)

  lpos=functionName~lastPos(".")
  if lpos=0 then
  do
     displayName=functionName
     scriptExtension=""          -- no extension given
  end
  else
  do
     displayName    =functionName~substr(1,lpos-1)
     scriptExtension=functionName~substr(lpos)
  end

  description=functionName": a(n)" scriptLanguage "macro" "(generated:" .DateTime~new || ")"
  logicalName=displayName"."scriptLanguage


::attribute scriptLanguage       -- OOo scripting language name, default: "ooRexx"
::attribute lang                 -- ISO name of locale in use, default: "en"
::attribute description          -- description of script
::attribute displayName          -- display name (script name without extension)
::attribute functionName         -- script name with extension
::attribute logicalName          -- script name with value of scriptLanguage as extension (".ooRexx")


::method toString
  expose scriptLanguage lang description displayName functionName logicalName

  lf     ="0d0a"x
  indent ="    "
  indent2="        "
  indent3="            "

  res=.MutableBuffer~new
  res~append(indent  '<script language="'    || scriptLanguage  || '">'  || lf)
  res~append(indent2 '<locale lang="'        || lang            || '">'  || lf)
  res~append(indent3 '<displayname value="'  || displayName     || '"/>' || lf)
  res~append(indent3 '<description>'         || lf)
  res~append(indent3 '   ' description       || lf)
  res~append(indent3 '</description>'        || lf)
  res~append(indent2 '</locale>'             || lf)
  res~append(indent2 '<functionname value="' || functionName    || '"/>' || lf)
  res~append(indent2 '<logicalname value="'  || logicalName     || '"/>' || lf)
  res~append(indent  '</script>' lf)

  return res~string     -- return rendered as string


   /* determine system dependent paths for locating, installing, removing scripts
      in the 'user' (default) or 'share' location */
::routine uno.determineScriptLocation public
   use strict arg xContext, scriptLocation="user", scriptLanguageDir="oorexx", scriptLanguageName="ooRexx", tgtLibraryName=.nil, srcDir=.nil

   signal on syntax
   XMcf = xContext~getServiceManager   -- retrieve XMultiComponentFactory

   resDir=.directory~new            -- create result directory
   resDir~scriptLanguageDir=scriptLanguageDir
   resDir~scriptLanguageName=scriptLanguageName

   ufs=.uno~file.separator          -- get file separator for this platform


   oPathSubstitution=XMcf~createInstanceWithContext("com.sun.star.util.PathSubstitution", xContext)
   xPathSubstitution=oPathSubstitution~XStringSubstitution

   fragment=ufs || "Scripts" || ufs || scriptLanguageDir

   if scriptLocation="share" then key="inst"
                             else key="user" -- default! (no matter what 'scriptLocation' was given

   path=uno.convertFromUrl(xPathSubstitution~getSubstituteVariableValue("$("key")"))
   if scriptLocation="share" then      -- if "share", then explicitly add it
      path=path || ufs || scriptLocation

   resDir~Script_RootDirScripts=path || ufs || "Scripts"    -- define root dir for "Scripts"

   resDir~Script_RootDirScriptLanguage = path || fragment   -- define ooRexx root dir for "Scripts"

      -- save paths
   if srcDir<>.nil then             -- if source dir given, then edit & save it
   do
      if srcDir~right(1)=ufs then   -- make sure that source dir has no trailing file separator
         srcDir=strip(srcDir, "Trailing", ufs)
      resDir~sourceDir=srcDir                                     -- save sourceDir
      resDir~sourceDir_parcel=srcDir || ufs || "parcel-descriptor.xml"
   end


   if tgtLibraryName<>.nil then     -- if target fragment dir given, then edit & save it
   do
      if tgtLibraryName~right(1)=ufs then -- make sure that target dir has no trailing file separator
         tgtLibraryName=strip(tgtLibraryName, "Trailing", ufs)

      resDir~targetDir=path || fragment || ufs || tgtLibraryName  -- save targetDir

         -- save fully qualified filename for parcel file
      resDir~targetDir_parcel=resDir~targetDir || ufs || "parcel-descriptor.xml"
   end

   return resDir     -- return directory with the calculated system-dependent paths

syntax:
   raise propagate