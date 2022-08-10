#!/usr/bin/env rexx
/* Package:          CLR.CLS
 * Description:      ooRexx package (library) for making Microsoft's .NET Framework available in
                     ooRexx using jni4net: .NET classes and objects appear as ooRexx classes and
                     objects and one can send ooRexx messages to them. "CLR" is the acronym for
                     "Common Language Runtime" on which .Net (and Mono) is based on.

 * Requires:         BSF4ooRexx (bridge to/from Java) and jni4net.
 * Original author:  Manuel Raffl, October 2015

 * Changes:          - rgf, 20160613-30270703, 20160712:
                        - added .clr.dir (a Rexx directory stored in .local) now storing all "clr."-entries
                          in it (instead of .local)
                          - added .clr.dir~version entry in the form version*100.yyyymmdd

                        - employing ooRexx .ArgUtil to test types of arguments where appropriate

                        - class CLRLogger:
                          - moved to top to allow usage in other classes class constructors
                          - removed "setLevel", added "logLevel" attribute getter and setter instead
                            - logLevel getter returns the current logLevel as a string (not the internal number)
                          - added getter for retrieving a copy of the "logLevels" directory
                          - added doLogging attribute: if .true (other than "OFF"), then logging takes place (optimization)

                        - supplying .line value on logging messages to ease finding the line where the log was issued

                        - class CLR:
                          - method unknown: added looking up CLR fields to CLR's; distinguish
                                            whether class (type) or instance operation
                          - method string: indicate type of CLR instance, include ToString() result
                          - routine clr.import: logic moved to CLR's class method clr.import
                          - class method clr.import (implemented according to BSF.CLS' bsf.import) which
                            by default stores the fully qualified type name in .local, such that it
                            can be addressed within ooRexx by its environment symbol (dot immediately
                            followed by the type name); if second arg is .nil, then it will not be
                            stored in .local, any other name will be used as index to store the proxy
                            class in .local

                        - class CLR_CLASS:
                          - made subclass of CLR
                          - method init: add constructor, such that ~new can be sent to this proxy class,
                                         will reflect the CLR constructor and execute it
                          - method unknown: forwards to class (type) object, if not resolvable, forward
                                            to proxy ("instance") object

                        - added CLR_PROXY, a subclass of CLR: allows for proxying CLR objects returned
                          from the runtime

                        - made CLR_EVENT a subclass of CLR_PROXY

                        - added CLR_ENUM, a subclass of CLR, representing System.Enum values

                        - created Java helper class ("Helper4ooRexx4Net") to ease (and speed up)
                          boxing and unboxing, as well as looking up method names via reflection

                        - routine box(): now allows for boxing all primitive types, employs "Helper4ooRexx4net"

                        - routine unbox(): allows for unboxing boxed values, employs "Helper4ooRexx4net"

                        - renamed routine "clr.get_type_by_name" to "clr.getTypeByName"

                        - renamed routines "clr.findMatchingXyzName" to "clr.findXyzName" as the logic
                          is carried out in "Helper4ooRexx4net" except for "clr.findMatchingEvent", which
                          remains implemented in ooRexx (jni4net does not have a "System.Event" proxy by
                          default; implementing it via reflection not really necessary as the ooRexx
                          solution seems to be fast enough)

      hints:   - using dots in routine and some method names to inhibit name clashes with CLR (AFAIK
                 dots are prohibited as part of identifiers, whereas Rexx allows identifiers to contain
                 dots)

 *  license:
 *
 *  ------------------------ Apache Version 2.0 license -------------------------
 *     Copyright (C) 2015 Manuel Raffel
 *     Copyright (C) 2016 Rony G. Flatscher
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *  -----------------------------------------------------------------------------

 */

/** ooRexx package which requires the BSF.CLS (ooRexx Java bridge) ooRexx package and the Java package
*   "jni4net" in order to make all .Net classes on Windows (the Common Language Runtime, CLR) available
*   to ooRexx programmers.
*
*   ooRexx programmers will use the public ooRexx class "CLR" to instantiate any (fully qualified)
*   CLR type and send the ooRexx proxy merely ooRexx messages (comparable to exploiting the public
*   "BSF" class to interact with Java).
*
*  @author Manfred Raffel, original design and implementation, fall 2015
*  @author Rony G. Flatscher, updates, summer 2016
*  @since fall 2015
*  @version 100.20160812
*/


.CLRLogger~logLevel="OFF"        -- rgf, ooRexx-style: turn off logging by default ("OFF")
-- .CLRLogger~logLevel="trace"      -- rgf, ooRexx-style: turn off logging by default ("OFF")
-- .CLRLogger~logLevel="debug"      -- rgf, ooRexx-style: turn off logging by default ("OFF")
-- .CLRLogger~logLevel="debug"      -- rgf, ooRexx-style: turn off logging by default ("OFF")

-- TODO: remove after debugging
/*
say ".CLRLogger~logLevel=".CLRLogger~logLevel
.local~bShowRgf=.false
if .bShowRgf=.true then call "rgf_util2.rex"
*/

.local~clr.dir=.directory~new    -- directory for oorexx4net (CLR, common language runtime)
.clr.dir~version="100.20160812"


.clr.dir~bridge = BSF.import("net.sf.jni4net.Bridge")  -- get jni4net support
-- .clr.dir~bridge~setDebug(.true)    -- .true for debug output
.clr.dir~bridge~setVerbose(.false)    -- .true for debug output
.clr.dir~bridge~bsf.dispatch("init")  -- initialize jni4net support

.clr.dir~system.reflection.assembly = BSF.import("system.reflection.Assembly")

ooRexxAssembly = .clr.dir~system.reflection.assembly~LoadWithPartialName("oorexx.net")  -- get additional .NET proxies
.clr.dir~bridge~RegisterAssembly(ooRexxAssembly)

CALL clr.initAssemblies

   -- note: lowercase package name "system" indicates, that this proxy class gets imported from jni4net itself, not from CLR
.clr.dir~system.array  = BSF.import("system.Array")   -- preload frequently used classes
.clr.dir~system.enum   = BSF.import("system.Enum")
.clr.dir~system.object = BSF.import("system.Object")
.clr.dir~system.type   = BSF.import("system.Type")

   -- rgf, 20160613, TODO: adjust once the package gets changed for this Java class
.clr.dir~helper4rexx = bsf.loadClass("org.oorexx.clr.Helper4ooRexx4Net") -- rgf, GA-version: org.oorexx.clr.Helper4ooRexx4Net
.clr.dir~helper4rexx~setVerbose(.false)
.clr.dir~helper4rexx~setDebug(.false)

   -- setup directory to map fully qualified wrapper type names to clr_box-typeIndicator strings
TypeName2indicator=.directory~new
indicator2TypeName=.directory~new

boxableNames=.array~of(                                                                     -
         "System.Boolean", "System.Byte", "System.Char", "System.Decimal", "System.Double", -
         "System.Int16", "System.UInt16", "System.Int32", "System.UInt32",                  -
         "System.Int64", "System.UInt64", "System.SByte", "System.Single", "System.String")
indicatorTypes=.array~of(                            -
         "BO", "BY", "CHAR", "DE", "DO",             -
         "INT16", "UINT16", "INT32", "UINT32",       -
         "INT64", "UINT64", "SB", "SI", "ST" )

   -- note, leading blank needed for caselessWordPos usage in clr.box
.clr.dir~indicatorNamesLong=" Boolean Byte Character Decimal Double" -
         "Int16 UInt16 Int32 Uint32"               -
         "int64 Uint64 SByte Single String "

allTypeNames=""
allIndicatorNames=""
do i=1 to boxableNames~size   -- save TypeName with its indicator type for clr_box()
   typeName      = boxableNames[i]
   allTypeNames=allTypeNames typeName     -- create string of fully qualified wrapper types (for primitive types)

   indicatorType = indicatorTypes[i]
   allIndicatorNames=allIndicatorNames indicatorType -- create string of type names for error message infos (e.g. clr.box())

   typeName2indicator[typeName]=indicatorType            -- case sensitive index-entry
   typeName2indicator~setEntry(typeName, indicatorType)  -- uppercased index-entry

   indicator2TypeName[indicatorType]=typeName            -- index already in uppercase
end

.clr.dir~typeName2indicator=typeName2indicator    -- save in .clr.dir
.clr.dir~indicator2typeName=indicator2typeName    -- save in .clr.dir
.clr.dir~allWrapperTypeNames=allTypeNames~strip   -- strip leading blank and save in .clr.dir
.clr.dir~allIndicatorNames  =allIndicatorNames~strip


/** Gets BSF4ooRexx support, a Java bridge. */
::REQUIRES BSF.CLS  -- get BSF4ooRexx (Java-bridge) support


/* ====================================================================================================== */

   -- CLRLogger MUST be placed at top, such that log messages in class init methods can use it!

 /** Utility class to allow logging merely by sending the messages "FATAL", "ERROR", "WARN",
 *   "INFO", "DEBUG" or "TRACE" with the log text as argument to the class object <code>.CLRLogger</code>.
 *   All interaction is with the class object directly, there is no instance one is allowed to create.
 */
::CLASS CLRLogger PUBLIC

/** Inhibit creating instances of this class by making the instance constructor private and abstract.
*/
::method init private abstract

/** Constructor, defining the log levels "OFF", "FATAL", "ERROR", *   "WARN", "INFO", "DEBUG" and "TRACE",
*   setting the attribute <code>logLevel</code> to "OFF". Determines whether the ooRexx version in use
*   has the <code>.traceOutput</code> stream, if not the <code>.error</code> stream is used for log output
*   instead.
*/
 ::METHOD init PUBLIC CLASS
    EXPOSE logLevels logLevel doLogging logStream

      -- define log level names and numeric values
    logLevels = .directory~new
    logLevels["OFF"]   = 70
    logLevels["FATAL"] = 60
    logLevels["ERROR"] = 50
    logLevels["WARN"]  = 40
    logLevels["INFO"]  = 30
    logLevels["DEBUG"] = 20
    logLevels["TRACE"] = 10

    self~logLevel="OFF"    -- set logging off

    if .local~hasEntry("TRACEOUTPUT") then logStream=.traceOutput
                                      else logStream=.error


/** Getter attribute method that indicates whether log messages get processed.
* @return <code>.true</code> if logging is active, <code>.false</code> if not
*/
  ::ATTRIBUTE doLogging GET CLASS -- rgf

/** Getter attribute method that returns a copy of the log levels directory.
* @return copy of <code>logLevels</code> directory object
*/
  ::ATTRIBUTE logLevels GET CLASS -- rgf
    EXPOSE logLevels
    return logLevels~copy  -- return a copy, such that no one can fiddle with it

/** Getter attribute method that returns the current <code>logLevel</code> setting.
* @return one of the strings: "OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"
*/
  ::ATTRIBUTE logLevel GET CLASS -- rgf
    EXPOSE logLevel logLevels
    return logLevels~index(logLevel)   -- return the (meaningful) index value for current logLevel

/** Setter attribute method that returns a copy of the log levels directory. If set to "OFF", then
*   the attribute <code>doLogging</code> is set to <code>.false</code>, otherwise to <code>.true</code>.
* @param desiredLevel, one of "OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG" and "TRACE".
*/
  ::ATTRIBUTE logLevel SET CLASS -- rgf
    EXPOSE logLevel logLevels doLogging
    PARSE ARG desiredLevel

    signal on any
    if \logLevels~hasEntry(desiredLevel) then
       Raise syntax 40.900 array ("CLR.CLS/CLRLogger/""LOGLEVEL="" CLASS: desiredLevel="pp(desiredLevel) "unknown")

    logLevel = logLevels~entry(desiredLevel)
    doLogging=(desiredLevel~upper<>"OFF")  -- determine whether logging should be carried out
    return

any: raise propagate


/** Unknown method that handles the messages "OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE",
*   depending on the value of the attribute <code>logLevel</code>. The handling is determined by
*   the following relation: <code>"OFF" &lt; "FATAL" &lt; "ERROR" &lt; "WARN" &lt; "INFO" &lt; "DEBUG"
*   &lt; "TRACE"<code>. Hence, if the attribute <code>logLevel</code> is set to <code>&quot;OFF&quot;<code>
*   no log messages get displayed. By contrast, if <code>logLevel</code> is set to <code>&quot;TRACE&quot;<code>
*   all log messages get displayed.
*
* @param levelName the unknown message name
* @param args an array object containing the arguments supplied with the unknown message
*
*/
  ::METHOD unknown PUBLIC CLASS
    EXPOSE logLevel logLevels logStream
    USE ARG levelName, args

    signal on syntax
    if \logLevels~hasEntry(levelName) then
       Raise syntax 40.900 array ("CLR.CLS/CLRLogger/UNKNOWN CLASS: levelName="pp(levelName) "unknown")

       -- only process message if invoked log level is greater or equal log level limit
       -- ("OFF"=70, "TRACE"=10)
    IF logLevels[levelName] >= logLevel THEN
       logStream~SAY(pp(.dateTime~new) "|" pp(LEFT(levelName, 5)) "|" pp(args[1]))

    return

syntax: raise propagate



/* ====================================================================================================== */

/** Routine that makes sure that the fully qualified names of the exported types defined in the assemblies
*   "mscorlib" and "System" get cached with their assembly names in <code>.clr.dir~assemblyName</code>.
*/

::ROUTINE clr.initAssemblies PRIVATE
  if .CLRLogger~doLogging then -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.initAssemblies(): add ""mscorlib"" and ""System"" assemblies to .clr.dir~assemblyName")

  .clr.dir~assemblyName = .directory~new
  CALL clr.addAssembly "mscorlib"   -- Microsoft defined types
  CALL clr.addAssembly "System"     -- CLR System defined types

/* ====================================================================================================== */

/** Routine that queries all exported types defined in the supplied assembly and caches their full names
*   and assembly names in <code>.clr.dir~assemblyName</code>.
*  @param assemblyName the assembly name to work on
*/
::ROUTINE clr.addAssembly PUBLIC
  PARSE ARG assemblyName

  if .CLRLogger~doLogging then -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.addAssembly(), assemblyName="pp(assemblyName))

  assemblyTypes = .clr.dir~system.reflection.assembly~LoadWithPartialName(assemblyName)~GetExportedTypes  -- retrieve classes defined in assembly with given name

  dir=.clr.dir~assemblyName   -- fetch directory object
  DO i = 1 TO assemblyTypes~size
    dir~setEntry(assemblyTypes[i]~getFullName, assemblyName)
  END


/* ====================================================================================================== */

 /** Determine and return the assembly name from a fully qualified type name.
 * @param  typeName fully qualified type name
 * @return assembly name where the type gets exported
 */
::ROUTINE clr.findAssemblyName PRIVATE
  USE strict ARG typeName

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.findAssemblyName(), typeName="pp(typeName))

  .ArgUtil~validateClass(pp("typeName")||"="pp(typeName), typeName, .string)   -- check for correct type

  assemblyName = .clr.dir~assemblyName~entry(typeName)  -- lookup assembly name in known assemblies

  if assemblyName=.nil then   -- type not found ?
  do
     split = LASTPOS(".", typeName)
     assemblyName = SUBSTR(typeName, 1, (split-1))  -- extract assembly name from fully qualified class name
  end

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~debug(pp.line(.line) "CLR.CLS/clr.findAssemblyName(), typeName="pp(typeName)", returning:" pp(assemblyName))
  return assemblyName



/* ====================================================================================================== */


/** This class serves as a proxy for CLR/.Net objects of any type, returned from CLR/.Net. The routine
*   <code>clr.wrap()</code> will use this class, if a pure BSF (Java) object is supplied,
*   assuming it is from CLR/.Net.
*
* @author Rony G. Flatscher
* @since 2016-06-18
*/

::class "CLR_Proxy" subclass clr

/** Constructor.
*
*  @param argObj mandatory object, can be a CLR or BSF object
*  @param argType optional object, if supplied, must be a BSF object
*/

::method init
  use strict arg argObj, argType=.nil

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR_PROXY/INIT, argObj="pp(argObj)", argType="pp(argType))

  if argObj~isA(.clr) then     -- a CLR instance, create a proxy (a copy) of it
  do
     self~clr.object  = argObj~clr.object
     self~clr.type = argObj~clr.type
     return
  end


  .ArgUtil~validateClass("argObj", argObj, .bsf)    -- check for correct type
  if argObj~isA(.bsf) then    -- an unwrapped BSF object, assuming it is from CLR
  do
     self~clr.object=argObj
     if argType=.nil then     -- type not supplied, get it from CLR object
     do
        self~clr.type=argObj~getType
        return
     end

     .ArgUtil~validateClass("argType", argType, .bsf) -- check for correct type
     self~clr.type=argType
  end
  return




/* ====================================================================================================== */

/** Routine to import a CLR/.Net class, the imported class understands the NEW-message.
*
* @param typeName the fully qualified CLR/.net type name
* @param rexxName4local the name to store in .local, defaults to <code>.nil</code>,
*                       which inhibits the default entry of the CLR::CLR_IMPORT
*
* @return a Rexx class object, subclassing CLR_Class, proxying the CLR/.Net type object
*/

::ROUTINE clr.import PUBLIC
  USE strict ARG typeName, rexxName4local=.nil

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.import(), typeName="pp(typeName)", rexxName4local="pp(rexxName4local))

  return .clr~clr.import(typeName, rexxName4local)


/* ====================================================================================================== */

 /** Creates an instance of "system.EventHandler" with the Rexx event handler object to handle any
 *   events.
 *
 *   @param rexxEventHandler a Rexx object that handles the events
 *   @param rexxData an optional Rexx object that will be supplied with each event (last event argument,
 *                   appended by BSF4ooRexx, which is a Rexx directory object; the "rexxData" object
 *                   can be retrieved from it using as index the value "USERDATA"; cf. BSF4ooRexx)
 *
 *   @return an instance of "System.EventHandler", where the rexxEventHandler object will process the events
 */
::ROUTINE clr.createEventHandler PUBLIC
  USE strict ARG rexxEventHandler, rexxData = .nil

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.createEventHandler(), rexxEventHandler="pp(rexxEventHandler)", rexxData="pp(rexxData))

  prxEventHandler = BSFCreateRexxProxy(rexxEventHandler, rexxData)   -- box it as a Java object
  prxClass = bsf.createProxyClass("system.EventHandler") -- create an on-the-fly extension
  eventHandler = prxClass~new(prxEventHandler)  -- create instance and supply our boxed Rexx event handler to handle the events

  RETURN eventHandler            -- return the "System.Event" handler object

/* ====================================================================================================== */

/** Utility routine that allows creating a single dimensioned CLR array with the supplied type and capacity.
*
* @param typeName the fully qualified CLR type name
* @param capacity the capacity (a non-negative whole number)
*
* @return a System.Array instance (a CLR array object) of the specified type and capacity
*/
::ROUTINE clr.createArray PUBLIC
  use strict ARG typeName, capacity
  -- PARSE ARG className, capacity

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.createArray(), typeName="pp(typeName)", capacity="pp(capacity))

  signal on syntax
  .ArgUtil~validateClass("typeName="pp(typeName), typeName, .string) -- check for correct type

  if \datatype(capacity,"Whole") | capacity<0 then
     raise syntax 40.900 array ("CLR.CLS/clr.createArray, argument 'capacity'" pp(capacity) "is not a non-negative whole number")

  RETURN .clr.dir~system.array~CreateInstance(.clr.dir~system.type~GetType(typeName), capacity)

syntax: raise propagate   -- propagate to caller

/* ====================================================================================================== */

/**
 * Processes received parameter "value" depending on its type. Always returns a CLR.
 * If a CLR object which is a <ocde>system.Enum</code>, but not an instance of the
 * <code>CLR_Enum</code> class, then an instance of <code>CLR_Enum</code> will be created and
 * returned using the CLR object's <code>clr.object</code> and <code>clr.type</code>
 * attributes.
 *
 * <p>If a Rexx string is supplied which can be represented as a number, then a
 * CLR object representing a wrapped system.Int32, system.Int64 or system.Decimal
 * gets returned, depending on the value range. Otherwise a system.String object
 * gets returned
 *
 * <p>If a BSF (Java proxy) object is supplied it will be wrapped either as a
 * <code>CLR_Enum</code> or as a <code>CLR_Proxy</code> object.
 *
 * @param       value  a CLR, a BSF or a Rexx string object
 *
 * @return      a CLR object wrapping up the supplied <code>value</code>
 */
::ROUTINE clr.wrap public
  USE ARG value

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~debug(pp.line(.line) "CLR.CLS/clr.wrap(), value="pp(value) "value~class="pp(value~class))

  IF value~isA(.CLR) THEN  -- if "value" already is a CLR, nothing is to be done
  DO
     if \value~isA(.clr_Enum), value~clr.object~objectname~abbrev("system.Enum@") then
     do
        if .CLRLogger~doLogging then    -- if not OFF, produce information
          .CLRLogger~debug(pp.line(.line) "CLR.CLS/clr.wrap: rewrapping" pp(value) "as a .CLR_Enum")
        return .clr_Enum~new(value)
     end

     if .CLRLogger~doLogging then    -- if not OFF, produce information
       .CLRLogger~debug(pp.line(.line) "CLR.CLS/clr.wrap: returning" pp(value) "unchanged")
    RETURN value
  END
  ELSE IF value~isA(.string) THEN  -- if "value" is a string...
  DO
      --- rgf, 20160620: with numbers we need to default to int32, if value allows for it, as automatic
      --                 conversion of CLR/.Net parameters may not work out otherwise
      /*
       System.Decimal.MinValue: -79,228,162,514,264,337,593,543,950,335
       System.Decimal.MaxValue: +79,228,162,514,264,337,593,543,950,335.
      */
      numeric digits 29    -- a System.Decimal has 28 signifant digits, upon return from routine this will be reset to default
      if datatype(value, "Number") then
      do
            -- remark: boxing to System.Decimal may cause failure of finding (constructor) methods via CLR reflection
          if datatype(value, "Whole") then
          do
                  -- if possible, box as INT32
             if value>= -2147483648, value<2147483648 then
             do
               if .CLRLogger~doLogging then    -- if not OFF, produce information
                  .CLRLogger~debug(pp.line(.line) "CLR.CLS/clr.wrap: doing a clr.box("pp(value)") to a [System.Int32]")
                return clr.box("Int32",value)
             end

             if value>= -9223372036854775808, value<9223372036854775808 then
             do
               if .CLRLogger~doLogging then    -- if not OFF, produce information
                  .CLRLogger~debug(pp.line(.line) "CLR.CLS/clr.wrap: doing a clr.box("pp(value)") to a [System.Int64]")
                return clr.box("Int64",value)
             end
          end

               -- box as System.Decimal
          if .CLRLogger~doLogging then    -- if not OFF, produce information
             .CLRLogger~debug(pp.line(.line) "CLR.CLS/clr.wrap: doing a clr.box("pp(value)") to a [System.Decimal]")
          return clr.box("Decimal",value)
      end

      if .CLRLogger~doLogging then    -- if not OFF, produce information
         .CLRLogger~debug(pp.line(.line) "CLR.CLS/clr.wrap: doing a clr.box("pp(value)") to a [System.String]")
      return clr.box("ST",value)    -- return a System.String object
  END

  else if value~isA(.bsf) then   -- a BSF object?
  do
     if value~objectName~abbrev("system.Enum@") then  -- a CLR/.Net System.Enum instance?
     do
        if .CLRLogger~doLogging then    -- if not OFF, produce information
           .CLRLogger~debug(pp.line(.line) "CLR.CLS/clr.wrap: wrapping" pp(value) "as a [CLR_Enum] object")
        return .clr_enum~new(value)    -- wrap it as a CLR_ENUM type object to gain CLR's abilities
     end
     else
     do
        if .CLRLogger~doLogging then    -- if not OFF, produce information
           .CLRLogger~debug(pp.line(.line) "CLR.CLS/clr.wrap: wrapping" pp(value) "as a [CLR_Proxy] object")
        return .clr_proxy~new(value)   -- wrap it as a CLR_PROXY type object to gain CLR's abilities
     end
  end

  raise syntax 40.900 array ("CLR.CLS/clr.wrap, argument 'value'" pp(value) "cannot be wrapped for CLR")



/* ====================================================================================================== */

--rgf: using Java implementation for now, as this routine is heavily used, so we can speed up the lookup
/* Routine:     clr.MethodName
 * Description: Searches given "Type" object for given method name.
 * Arguments:   - clrType: a .NET "System.Type" defining its available methods
 *              - methodName: the name of the method to be searched for
 * Returns:     case sensitive name of the method or .nil if it could not be found
 */

/** Returns the mixed case method name matching caselessly the supplied method name in the supplied CLR type.
*
* @param clrType the CLR type to look for the supplied method name
* @param methodName the name of the method to look for in the supplied <code>clrType</code>
* @return .nil if no method found in the type by the given name (comparisons are caseless) or the correctly
*              spelled method name
*/
::ROUTINE clr.MethodName PRIVATE
  USE ARG clrType, methodName

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.MethodName(), clrType="pp(clrType)", seeking method named" pp(methodName))

  -- rgf, 2016-06: observation that in debug mode (with tons of toString()), sometimes
  --               jni4net returns a System.Array instead of a Java array object;
  --               fetching the correctly spelled name in Java seems to avoid this
   return .clr.dir~helper4rexx~getMethodName(clrType,methodName)

/*
  -- Rexx solution works, but compiled Java without bridging via BSF4ooRexx is faster
  typeMethods = clrType~GetMethods  -- retrieve methods available for this "Type"
  typeMethodsCount = typeMethods~size

  DO i = 1 TO typeMethodsCount
    IF typeMethods[i]~getName~caselessEquals(methodName) THEN  -- case insensitive comparison
    do
      RETURN typeMethods[i]~getName
    end
  END

  RETURN .nil  -- return .nil only if no matching method was found
*/

/* ====================================================================================================== */

/* Routine:     clr.PropertyName
 * Description: Searches given "Type" object for given property name.
 * Arguments:   - clrType: a .NET "System.Type" defining its available properties
 *              - propertyName: the name of the property to be searched for
 * Returns:     case sensitive name of the property or .nil if it could not be found
 */

/** Returns the mixed case property name matching caselessly the supplied property name in the supplied CLR type.
*
* @param clrType the CLR type to look for the supplied method name
* @param propertyName the name of the property to look for in the supplied <code>clrType</code>
* @return .nil if no property found in the type by the given name (comparisons are caseless) or the correctly
*              spelled property name
*/
::ROUTINE clr.PropertyName PRIVATE
  USE ARG clrType, propertyName

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.PropertyName(), clrType="pp(clrType)", seeking property named" pp(propertyName))
  -- rgf, 2016-06: observation that in debug mode (with tons of toString()), sometimes
  --               jni4net returns a System.Array instead of a Java array object;
  --               fetching the correctly spelled name in Java seems to avoid this
   return .clr.dir~helper4rexx~getPropertyName(clrType,propertyName)

/* --->
  -- Rexx solution works, but compiled Java without bridging via BSF4ooRexx is faster
  .CLRLogger~debug("CLR.CLS/clr.PropertyName(), searching property name for" pp(propertyName) "in" pp(clrType))

  typeProperties = clrType~getProperties  -- retrieve properties available for this "Type"
  typePropertiesCount = typeProperties~size

  DO i = 1 TO typePropertiesCount
    IF typeProperties[i]~getName~caselessEquals(propertyName) THEN  -- case insensitive comparison
      RETURN typeProperties[i]~getName
  END

  RETURN .nil  -- return .nil only if no matching property was found
<--- */

/* ====================================================================================================== */

/* Routine:     clr.FieldName
 * Description: Searches given "Type" object for given field name.
 * Arguments:   - clrType: a .NET "System.Type" defining its available fields
 *              - fieldName: the name of the field to be searched for
 * Returns:     case sensitive name of the field or .nil if it could not be found
 */

/** Returns the mixed case property name matching caselessly the supplied field name in the supplied CLR type.
*
* @param clrType the CLR type to look for the supplied field name
* @param fieldName the name of the field to look for in the supplied <code>clrType</code>
* @return .nil if no field found in the type by the given name (comparisons are caseless) or the correctly
*              spelled field name
*/
::ROUTINE clr.FieldName PRIVATE
  USE ARG clrType, fieldName

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.FieldName(), clrType="pp(clrType)", seeking field named" pp(fieldName))

  -- rgf, 2016-06: observation that in debug mode (with tons of toString()), sometimes
  --               jni4net returns a System.Array instead of a Java array object;
  --               fetching the correctly spelled name in Java seems to avoid this
   return .clr.dir~helper4rexx~getFieldName(clrType,fieldName)

/* --->
  -- Rexx solution works, but compiled Java without bridging via BSF4ooRexx is faster
  .CLRLogger~trace("Searching field name for" fieldName "in" clrType)

  typeFields = clrType~GetFields  -- retrieve fields available for this "Type"
  typeFieldsCount = typeFields~size

  DO i = 1 TO typeFieldsCount
    IF typeFields[i]~getName~caselessEquals(fieldName) THEN  -- case insensitive comparison
      RETURN typeFields[i]~getName
  END

  RETURN .nil  -- return .nil only if no matching field was found
<--- */


/* ====================================================================================================== */

-- rgf: using ooRexx as jni4net does not have EventInfo imported; as there
--      are not that many events, if any, it is ok to leave this in ooRexx
/* Routine:     clr.findMatchingEvent
 * Description: Searches given "Type" object for given event name.
 * Arguments:   - clrType: a .NET "System.Type" defining its available events
 *              - eventName: the name of the event to be searched for
 * Returns:     case sensitive name of the event or .nil if it could not be found
 */

/** Returns the mixed case event name matching caselessly the supplied event name in the supplied CLR type.
*
* @param clrType the CLR type to look for the supplied event name
* @param fieldName the name of the event to look for in the supplied <code>clrType</code>
* @return .nil if no event found in the type by the given name (comparisons are caseless) or the correctly
*              spelled event name
*/
::ROUTINE clr.findMatchingEvent PRIVATE
  USE ARG clrType, eventName

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.findMatchingEvent(): clrType="pp(clrType)", seeking event named" pp(eventName))

  do event over clrType~GetEvents
     if event~getName~caselessEquals(eventName) then
        return event~getName
  end

  RETURN .nil  -- return .nil as no matching event was found



/* ====================================================================================================== */

/** Boxes (wraps) a Rexx string representing a primitive type into a CLR object using a CLR wrapper class.
 *  Rexx strings are turned into CLR string objects (type: <code>System.String</code>).
 *
 *  @param typeIndicator one of the following strings:
 *                <ul>
 *                     <li>"System.String",    "STring"    </li>
 *                     <li>"System.Boolean",   "BOolean"   </li>
 *                     <li>"System.Byte",      "BYte"      </li>
 *                     <li>"System.SByte",     "SByte"     </li>
 *                     <li>"System.Char",      "CHAR"      </li>
 *                     <li>"System.Decimal",   "DEcimal"   </li>
 *                     <li>"System.Double",    "DOuble"    </li>
 *                     <li>"System.Int16",     "INT16"     </li>
 *                     <li>"System.UInt16",    "UINT16"    </li>
 *                     <li>"System.Int32",     "INT32"     </li>
 *                     <li>"System.UInt32",    "UINT32"    </li>
 *                     <li>"System.Int64",     "INT64"     </li>
 *                     <li>"System.UInt64",    "UINT64"    </li>
 *                     <li>"System.Single",    "SIngle"    </li>
 *                </ul>
 *
 * @param strValue the Rexx string containing the primitive value or plain string to box/wrap/convert
 * @return  the boxed CLR value, the CLR string value or <code>.nil</code> if <code>.nil</code> was supplied
 */

  -- rgf, using compiled Java
::ROUTINE clr.box PUBLIC
   USE strict ARG typeIndicator, strValue

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.box(): typeIndicator="pp(typeIndicator)", strValue="pp(strValue))

   signal on syntax
/*
   if \ typeIndicator~isA(.string) then
      raise syntax 40.900 array ("CLR.CLS/CLR.BOX: illegal value for argument 'typeIndicator'" pp(typeIndicator)", must be a Rexx string, instead it is of type" pp(typeIndicator~class))
*/

      -- check for correct type
   .ArgUtil~validateClass("CLR.CLS/CLR.BOX: illegal value for argument 'typeIndicator'="pp(typeIndicator), typeIndicator, .string)

   if \ .clr.dir~indicator2typeName~hasEntry(typeIndicator) then
   do
         -- typeIndicator not matching the beginning of words?
      if (.clr.dir~indicatorNamesLong)~caselessWordPos(" "typeIndicator)=0 then
         raise syntax 40.900 array ("CLR.CLS/CLR.BOX: illegal value for argument 'typeIndicator'" pp(typeIndicator)", must be a word out of" pp(.clr.dir~indicatorNamesLong)"or out of" pp(.clr.dir~allIndicatorNames~strip))
   end

   if \strValue~isA(.string) then
      raise syntax 40.900 array ("CLR.CLS/CLR.BOX: illegal value for argument 'strValue'" pp(strValue)", must be a Rexx string")

   tmpRes=.clr.dir~helper4rexx~clr_box(typeIndicator,strValue)  -- box the value

   if tmpRes<>strValue then   -- a new (.Net/CLR) value got returned, wrap it as a .clr object
   do
      if .CLRLogger~doLogging then    -- if not OFF, produce information
         .CLRLogger~trace(pp.line(.line) "clr.wrap: returning a CLR_PROXY")
      if value~objectName~abbrev("system.Enum@") then
          proxy=.clr_enum~new(tmpRes)  -- wrap it as a CLR_ENUM type object to gain CLR's abilities
      else
          proxy=.clr_proxy~new(tmpRes) -- wrap it as a CLR_PROXY type object to gain CLR's abilities

      return proxy
   end
   return tmpRes

syntax:
   raise propagate




/* ====================================================================================================== */

/* Routine:       clr.unbox
 * Description:   Converts a .NET primitive data type to a Rexx String
 * Argument:      A .NET primitive type
 * Returns:       The Rexx string representation of the passed argument
 */

/** Routine to turn values of boxed/wrapped <code>CLR</code> primitive types or System.String
 *  values to Rexx strings, otherwise returns argument unchanged.
 *
 * @param clrObject the CLR object to unbox or to turn into a Rexx string
 * @return a Rexx string or the argument unchanged, if <code>clrObject</code> does not represent
 *         a boxed CLR value or a CLR string
*/
  -- rgf, using compiled Java
::ROUTINE clr.unbox PUBLIC
   USE ARG clrObject -- save the passed argument as clrObject

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.unbox(): clrObject="pp(clrObject))

   if clrObject=.nil then return .nil     -- no need to process a .nil

   if clrObject~isA(.clr) then
   do
      o=.clr.dir~helper4rexx~clr_unbox(clrObject~clr.object)
   end
   else
   do
      o=.clr.dir~helper4rexx~clr_unbox(clrObject)   -- maybe a "pure" .bsf object representing a CLR object
   end

   if o~isA(.string) then return o     -- conversion worked

   return clrObject     -- return unchanged, whatever it was


/* ====================================================================================================== */

/* Class:       CLR
 * Description: Wraps a .NET proxy and handles initialization and method calls to it.
 */

/** The class that represents CLR objects and which is responsible for interacting with the peer CLR object
*   on behalf of the ooRexx programmer. An instance of this class holds a reference to the peer CLR object
*   and a reference to that object's type to ease reflective interactions.
*/
::CLASS CLR PUBLIC

/** Getter attribute method to get the peer <code>clr.object</code>.
* @return the CLR object (a BSF object) that is a peer of the CLR object it represents
*/
::attribute clr.object get
  expose clr.object
  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR/CLR.OBJECT GET: clr.object="pp(clr.object))
  return clr.object

/** Private setter attribute method to set the peer <code>clr.object</code>.
* @param the BSF (sic!) object that is the peer of the CLR object it represents
*/
::attribute clr.object set private
  expose clr.object
  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR/CLR.OBJECT SET: clr.object="pp(clr.object) "set to" pp(arg(1)))
  use strict arg clr.object

/** Getter attribute method to get the <code>clr.type</code> of the <code>clr.object</code>.
* @return the CLR type object (a BSF object) that is the type of the <code>clr.object</code>
*/
::attribute clr.type get
  expose clr.type
  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR/CLR.TYPE GET: clr.type="pp(clr.type))
  return clr.type

/** Private setter attribute method to set the peer <code>clr.type</code>.
* @param the BSF (sic!) object that is the CLR type of <code>clr.object</code>
*/
::attribute clr.type set private
  expose clr.type
  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR/CLR.TYPE SET: clr.type="pp(clr.type) "set to" pp(arg(1)))
  use strict arg clr.type


  /* Method:      init
   * Description: Constructor which creates an instance of CLR based on the supplied class name and
   *              parameters.
   * Arguments:   - typeName: name of the .NET class to be instantiated or a BSF proxy to be wrapped
   *              - param: first parameter for the class, default .nil
   *              - ...: additional parameters
   * Returns:     the freshly created CLR
   */

/** Constructor method.
*
* @param typeName the fully qualified name of a CLR type to instantiate
* @param param optionally one or more paramaters to pass on to the CLR constructor
*/
  ::METHOD init PUBLIC
    EXPOSE clr.object clr.type
    USE ARG typeName, param = .nil, ...

    if .CLRLogger~doLogging then    -- if not OFF, produce information
       .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR/INIT: creating new CLR instance of" pp(typeName) "with parameters" pp(param))

    signal on syntax
    .ArgUtil~validateClass(pp("typeName")||"="pp(typeName), typeName, .string)   -- check for correct type

    wrapperType=.clr.dir~typeName2indicator[typeName]      -- is type name actually a wrapper type for primitive values or a String?
    if wrapperType<>.nil THEN    -- use boxing instead
    DO
        if arg()>2 | \param~isA(.string) then
        do
           if arg()>2 then
              raise syntax 40.900 array ("CLR.CLS/CLR::INIT, wrapping a primitive value: only two arguments allowed, however there are" pp(arg()) "arguments supplied")
           else
              raise syntax 40.900 array ("CLR.CLS/CLR::INIT, wrapping a primitive value: second argument must be a Rexx string, found:" pp(param) "(instance of" pp(param~class)")")
        end

        clr.object=.clr.dir~helper4rexx~clr_box(wrapperType, param)  -- box the value
    END

    else -- all other classes can be done in an unified way
    DO
      assemblyName = clr.findAssemblyName(typeName)  -- find assembly of (fully qualified) class name
      argCount = arg() - 1

      IF argCount = 0 THEN  -- no arguments, can use default constructor
        clr.object = .clr.dir~system.reflection.assembly~LoadWithPartialName(assemblyName)~CreateInstance(typeName)
      ELSE  -- arguments need to be wrapped in an array to be passed to appropriate constructor
      DO
        argsList = bsf.createJavaArray(.clr.dir~system.object, argCount)

        DO i = 1 TO argCount
          if .CLRLogger~doLogging then    -- if not OFF, produce information
             .CLRLogger~debug(pp.line(.line) "CLR.CLS/CLR::INIT, parsing argument #" pp(i) "->" pp(arg(i+1)))
          argument = clr.wrap(arg(i+1))
          -- .CLRLogger~trace("Parsed argument" argument argument~clr.getType argument~clr.getInstance)
          if .CLRLogger~doLogging then    -- if not OFF, produce information
             .CLRLogger~debug(pp.line(.line) "CLR.CLS/CLR::INIT, parsed argument #" pp(i) "after clr.wrap()="pp(argument) "clr.object="pp(argument~clr.object) "clr.type="pp(argument~clr.type))
          -- argsList[i] = argument~clr.getInstance
          argsList[i] = argument~clr.object
        END

        clr.object = .clr.dir~system.reflection.assembly~LoadWithPartialName(assemblyName)~CreateInstance(typeName, .false, .nil, .nil, argsList, .nil, .nil)
      END
    END

    clr.type = clr.object~GetType
    if .CLRLogger~doLogging then    -- if not OFF, produce information
       .CLRLogger~debug(pp.line(.line) "CLR.CLS/CLR::INIT, created CLR instance of" pp(typeName) "with clr.object" pp(clr.object) "and clr.type" pp(clr.type))

    return

syntax:
   raise propagate



-- rgf, 2016-06-18
/* -------------------------------------------------------------
   class method allows for creating an ooRexx proxy class for a Java class
   which is made available to this process by placing it (optionally) into the
   .local environment, unless the second argument is explicitly .nil
   ------------------------------------------------------------- */
/** Class method to import a CLR type into ooRexx and allow that type to be treated as an
*   ooRexx class that possesses the <code>new</code> class method to create instances of
*   the CLR type it represents.
*
* @param typeName the fully qualified type name which should be imported into ooRexx as an ooRexx class proxy
* @param rexxName4Local optional string which gets used to store the imported class proxy in the
*        <code>.local</code> directory. Prepending a dot to that name (using it as an environment symbol)
*        will cause ooRexx to automatically search for this entry in <code>.nil</code>.
*        If this argument is given and set to <code>.nil</code>, then no entry will be created in the
*        ooRexx <code>.local</code> directory.
*
* @return the ooRexx class object representing the CLR type, i.e. an instance of class
*         <code>&quot;CLR_Class&quot;</code>
*/
::METHOD clr.import class
  USE strict ARG typeName, rexxName4Local=(typeName)

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR/CLR.IMPORT/CLASS: typeName="pp(typeName)", rexxName4Local="pp(rexxName4Local))

  signal on syntax name any

  .ArgUtil~validateClass(pp("typeName")||"="pp(typeName), typeName, .string)   -- check for correct type

  bNoEntry4local=(arg()=1 | rexxName4local=.nil | rexxName4local="")

   -- fetch the type from the assembly
  assemblyName = clr.findAssemblyName(typeName) -- find assembly name for the given typeName
  assembly = .clr.dir~system.reflection.assembly~LoadWithPartialName(assemblyName)  -- load the assembly (CLR class object)
  type = assembly~getType(typeName)             -- now load the type from the assembly (CLR class object)
  if type=.nil then
     RAISE syntax 40.900 array ("CLR.CLS/CLR/CLR.IMPORT/CLASS: typeName" pp(typeName) "not found in assembly named" pp(assemblyName))

      -- now create proxy class object
  new_class = .CLR_CLASS~subclass(typeName)  -- create a subclass, give it the name of the CLR type
  new_class~clr.typeName = typeName          -- remember typeName
  new_class~objectName   = type~objectname   -- now assign beanName for class object representing the CLR type
  new_class~clr.object   = type              -- set CLR proxy object
  new_class~clr.type     = type~getType      -- set CLR proxy object's type (class) object

   -- Note: unknown messages are forwarded to CLR's unknown message which tries to find
   --       the members (message, property, field, event) in the clr.type object; in
   --       this case however, where the type (class) object is directly addressed, we want
   --       to search in the type itself, which contains the static members, hence this
   --       class' clr.clzProxy will possess the same clr.object for the clr.type attribute
  new_class~clr.clzProxy = .CLR_PROXY~new(new_class~clr.object,new_class~clr.object)

      -- this proxy allows fetching instance methods like ToString() for types as well
  new_class~clr.proxy    = .CLR_PROXY~new(new_class~clr.object,new_class~clr.type)

  if bNoEntry4local then return new_class       -- no entry in .local desired, just return the Rexx class object

  .local~setentry(rexxName4Local, new_class)    -- put the new ooRexx proxy class into the local environment
  return new_class                              -- return the new CLR proxy Rexx class

any:
  raise propagate  -- raise the exception in caller/invoker on the Rexx side





  /* Method:      unknown
   * Description: Catches every message sent to this CLR and determines how it is to be treated.
   * Arguments:   - command: the message sent to this CLR
   *              - args: the arguments of the message
   * Returns:     depending on the command
   */

/** The unknown method tries to find methods, properties, fields or events (in that order) that match
*   the unknown message name. It will carry out the appropriate operation. The implementation allows
*   also to assign new values to CLR properties, if the ooRexx programmer employs them like an ooRexx
*   attribute. In addition it allows for adding an event handler to an event object by the name of the
*   message.
*
*   @param msgName the name of the message that was not understood (no method by that name was found)
*   @param msgArgs an array containing the arguments supplied with the message, if any
*   @return returns the appropriate object or .nil, if appropriate
*
*/

  ::METHOD unknown PUBLIC
    EXPOSE clr.object clr.type
    USE ARG msgName, msgArgs

    if .CLRLogger~doLogging then    -- if not OFF, produce information
       .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR/UNKNOWN: msgName="pp(msgName)", msgArgs="pp(msgArgs~toString(line,",") ) )

    signal on syntax

    IF RIGHT(msgName,1) = "=" THEN  -- check if it is an assignment
    DO
      IF msgArgs[1]~isA(.CLR_Event) THEN  -- check passed argument, might already be handled ("+=")
do
.CLRLogger~debug(pp.line(.line) "<---> TSK! <---> CLR.CLS/CLR/UNKNOWN: msgName="pp(msgName)", msgArgs="pp(msgArgs~toString(,",") ) "- msgArgs~items="pp(msgArgs~items) )

        RETURN msgArgs[1]
end

      PARSE VAR msgName property "=" .  -- extract property name (without "=")

      propertyName = clr.PropertyName(clr.type, property)  -- try to find given property in "Type"

      IF msgArgs~items = 1 THEN
        RETURN self~clr.setPropertyValue(propertyName, msgArgs[1])  -- set property value to given argument
    END
    ELSE  -- not an assignment, look for a method member
    DO
      methodName = clr.MethodName(clr.type, msgName)  -- try to find given method in "Type"

      IF methodName <> .nil THEN  -- aimed for a method
      DO
        typeList = bsf.createJavaArray(.clr.dir~system.type, msgArgs~items)
        argsList = bsf.createJavaArray(.clr.dir~system.object, msgArgs~items)

        IF msgArgs~items > 0 THEN  -- arguments need to be wrapped in an array to be passed to method
        DO i = 1 TO msgArgs~items
          if .CLRLogger~doLogging then    -- if not OFF, produce information
             .CLRLogger~debug(pp.line(.line) "CLR.CLS/CLR/UNKNOWN: parsing argument #" pp(i) "class:" pp(msgArgs[i]~class))
          argument = clr.wrap(msgArgs[i])
          if .CLRLogger~doLogging then    -- if not OFF, produce information
             .CLRLogger~debug(pp.line(.line) "CLR.CLS/CLR/UNKNOWN: parsed argument #" pp(i) "after clr.wrap()="pp(argument) "clr.object="pp(argument~clr.object) "clr.type="pp(argument~clr.type))

          typeList[i] = argument~clr.type
          argsList[i] = argument~clr.object
        END

        methObj= clr.type~GetMethod(methodName, typeList)
               -- if executing for a type (a static method), then the first argument is ignored
        returnedValue=methObj~Invoke(clr.object,argsList)

        IF returnedValue = .nil THEN
          RETURN .nil	       	    -- return the NIL object if the method did not return anything

        unboxedValue = clr.unbox(returnedValue)	-- else invoke clr.unbox routine and save the output in variable "unboxedValue"
        IF unboxedValue~Class = .string THEN -- check the class of this variable. If clr.unbox was successfull, it should be an object instance of the STRING class
          RETURN unboxedValue  -- return the Rexx value

        RETURN clr.wrap(unboxedValue) -- create a .CLR_PROXY or .CLR_ENUM object
      END
      ELSE  -- aimed for another member, look for a property member
      DO
        propertyName = clr.PropertyName(clr.type, msgName)  -- try to find given property in "Type"

        IF propertyName <> .nil THEN  -- aimed for a property
        DO
          if clr.object=clr.type then   -- a class (type) object proxy in hand, seach in type object first
          do
             propertyInfo  =  clr.object~GetProperty(propertyName)  -- already type in hand!
             propertyValue = .clr_proxy~new(propertyInfo~GetValue(clr.object, .nil))
          end
          else
          do
             propertyInfo = clr.type~GetProperty(propertyName)   -- use the object's type referred to by clr.type
             PropertyValue = .clr_proxy~new(propertyInfo~GetValue(clr.object, .nil))
          end

          unboxedValue=clr.unbox(propertyValue)
          if unboxedValue~isA(.string) then
             return unboxedValue

          RETURN clr.wrap(unboxedValue) -- create a .CLR_PROXY or .CLR_ENUM object
        END

        ELSE   -- aimed for another member, look for a field member
        DO
	       fieldName = clr.FieldName(clr.type, msgName)  -- try to find given field in "Type"
          IF fieldName <> .nil THEN  -- aimed for a field (enumeration)
          DO
             fieldInfo = clr.type~GetField(fieldName)
	          fieldValue=.clr_proxy~new(fieldInfo~GetValue(clr.object))  -- return value of field

             unboxedValue=clr.unbox(fieldValue)
             if unboxedValue~isA(.string) then
                return unboxedValue

             RETURN clr.wrap(unboxedValue) -- create a .CLR_PROXY or .CLR_ENUM object
          END

          ELSE  -- aimed for another member, look for an event member, only one left
          DO
     	      eventName = clr.findMatchingEvent(clr.type, msgName)  -- try to find given event in "Type"
            if eventName<>.nil then
            do
               RETURN .CLR_Event~new(self, eventName)  -- return new CLR_Event
            end
          END
        END
      END
    END

    -- no members found, raise an error
    -- if we arrive here, we were not able to resolve the message, so we do not understand it!
    -- raise syntax 97.1 array(self~string, arg(1))
    raise syntax 97.1 array(self~string, msgName)

syntax:

/* left for debugging oorexx4net and bsf4oorexx in the summer of 2016 (Java thread works, Rexx thread with reply does not!)
   -- TODO: rgf, 20160623, needed for spotting problem in BSF error message when running edited
   --       09-clock.rex and 04-forms-clr.rxj (multithreading from Rexx, using REPLY causing problems!)
if .bShowRgf=.true then
do
   cobj=condition('obj')
   say "line:" pp(.line~right(4)) "CLR::UNKNOWN(): condition:" ppCondition2(cobj)
   jthrow=cobj~additional[2]
   say "line:" pp(.line~right(4)) "CLR::UNKNOWN():" pp(jthrow) pp(jthrow~class)
   if jthrow~isA(.bsf) then
   do
      say "line:" pp(.line~right(4)) "CLR::UNKNOWN(): TSK, jthrowable ->" pp(jthrow)":" pp(jthrow~toString)
      say "line:" pp(.line~right(4)) "CLR::UNKNOWN(): TSK, jthrowable~getCause ->" pp(jthrow)":" pp(jthrow~getCause)":" pp(jthrow~getCause~toString)
   end
      say "line:" pp(.line~right(4)) "CLR::UNKNOWN(): before 'RAISE PROPAGATE'..."
end
*/
   raise propagate


  /* Method:      clr.dispatch
   * Description: Directly forwards a message to the "unknown" method of this CLR. This is needed if a
   *              method on .NET side is to be called which is named like a method of this CLR object.
   *              Examples are "start" or "init" (inherited from .Object).
   * Arguments:   - methodName: name of the method to be invoked
   * Returns:     the return value of the unknown method
   */

  /**   Directly forwards a message to the "unknown" method of this CLR. This is needed if a
   *    method on .NET side is to be called which is named like a method on the ooRexx side.
   *    Examples are the ooRexx methods "class", "start" or "init" (inherited from the ooRexx root class
   *    <code>.Object</code>).
   *
   * @param messageName the name of the .NET message
  */
  ::METHOD clr.dispatch PUBLIC
    PARSE ARG messageName

    -- RETURN self~unknown(messageName, arg(2, 'A'))  -- calls unknown method
   if .CLRLogger~doLogging then    -- if not OFF, produce information
      .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR/CLR.DISPATCH: messageName="pp(messageName)", arg()="pp(arg()) "->" pp(arg(2,"a")~toString(line,",") ) )

      -- rgf, forward to UNKNOWN method, rearrange arguments accordingly
    forward message ("unknown") arguments (.array~of(messageName,arg(2,'A')))


  /* Method:      string
   * Description: Overrides "string" method of .Object to enable certain CLR instances to output their
   *              actual content instead of their class name.
   * Returns:     the value of the contained proxy
   */

   /** The method that renders the CLR proxy object in a format that indicates its type by including the CLR type name.
   *
   * @return a string representing the CLR proxy object
   */
   ::METHOD string PUBLIC
    EXPOSE clr.object         -- access attribute directly

   if .CLRLogger~doLogging then    -- if not OFF, produce information
      .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR/STRING: clr.object~objectName="pp(clr.object~objectName))

    if self~isA(.clr_class) then -- CLR proxy object represents a CLR class ?
       RETURN "The" self~class~id "class" || pp(clr.object"->" || pp(clr.object~toString))  -- show objectname and its toString() value
    else
       RETURN "a" self~class~id || pp(clr.object"->" || pp(clr.object~toString))  -- show objectname and its toString() value


  /* Method:      clr.setPropertyValue
   * Description: Sets the specified property to the supplied value in the .NET object wrapped in
   *              this CLR.
   * Arguments:   - propertyName: the name of the property to be set
   *              - propertyValue: the value the property is to be set to
   * Returns:     self
   */

   /** Private method that sets the specified property to the supplied value in the .NET object
   *   proxied via this CLR proxy object.
   *
   * @param propertyName the name of the property to be set
   * @param propertyValue the value the property is to be set to
   * eturn  self
   */
  ::METHOD clr.setPropertyValue PRIVATE
    EXPOSE clr.object clr.type
    USE ARG propertyName, propertyValue

    if .CLRLogger~doLogging then    -- if not OFF, produce information
       .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR/clr.setPropertyValue: setting property named" pp(propertyName) "to" pp(propertyValue))

    IF propertyValue~isA(.CLR) THEN  -- if supplied value is a CLR, extract its wrapped instance
    do
      propertyValue = propertyValue~clr.object -- get CLR instance (object)
    end
    ELSE  -- else handle value according to its (expected) type
    DO
      propertyType = clr.type~GetProperty(propertyName)~GetPropertyType() -- get expected datatype for this property

      IF propertyType~isEnum = .true THEN  -- special handling for enums
      do
        propertyValue = .clr.dir~system.enum~Parse(propertyType, propertyValue, .true)
      end
      ELSE
      do
         -- create the property value with the help of the .CLR class, then extract and assign the CLR object
        propertyValue = .clr~new(propertyType~toString, propertyValue)~clr.object
      end
    END

    clr.type~GetProperty(propertyName)~SetValue(clr.object, propertyValue, .nil)  -- actually set value

    RETURN self


/* ====================================================================================================== */

--- rgf, 2016-06-18
-- class for imported types, they need a different constructor, hence overrides INIT
-- NOTE: importing "System.String" is not allowed

/** The proxy class representing an imported CLR type. When sending messages to the class proxy object, then
*   resolution starts with the static scope first, followed by the instance scope, if the static scope could
*   not be resolved. The proxy class can be used to instantiate CLR objects in the ooRexx style by merely
*   sending the proxy class the message "NEW", supplying the arguments, if any.
*/
::class "CLR_Class"    subclass clr private


/** Class attribute that stores the CLR assembly object that was used to load this type. */
::attribute clr.assembly class   -- the CLR assembly which we used to load this type

/** Class attribute that stores the CLR type name for which this class object is a peer. */
::attribute clr.typeName class   -- the CLR type name for which this class object is a peer

/** Class attribute that stores the proxy Java type object (an instance of BSF) that represents the CLR type.
*/
::attribute clr.object   class   -- CLR Java type object this Rexx class represents

/** Class attribute that stores the proxy Java type object (an instance of BSF) that represents
*   the type/class object of <code>clr.object</code>.
*/
::attribute clr.type     class   -- the CLR type's Java type (class) object


/** Class attribute that stores the CLR instance that represents this CLR type. Its
*   <code>clr.object</code> and <code>clr.type</code> attributes refer the <code>clr.object</code>
*   and <code>clr.type</code>, respectively. This allows the resolution of messages that are aimed
*   at <em>instance</em> members (methods, properties, fields, events) of the proxied CLR type.
*/
::attribute clr.proxy    class   -- the CLR proxy object

/** Class attribute that stores the CLR instance that represents this CLR type. Its
*   <code>clr.object</code> and <code>clr.type</code> attributes refer the <code>clr.object</code>
*   Java proxy, thereby allowing the resolution of messages that are aimed at <em>static</em>
*   members (methods, properties, fields, events) of the proxied CLR type.
*/
::attribute clr.clzProxy class   -- the CLR proxy object wrapping clr.object *and* clr.object as its type
                                 -- (this way all static members, methods, properties, fields, events
                                 -- are addressable) for serving as the forward target

   -- make sure to initialize the class attributes to .nil to ease debugging
/** Class constructor that initializes the class attributes by setting them to .nil.
*/
::method    init         class
  expose clr.typeName clr.object clr.type clr.proxy clr.clzProxy

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR_Class/INIT CLASS:" pp("The" self~objectname "class"))

  clr.typeName =.nil
  clr.object   =.nil
  clr.type     =.nil
  clr.clzProxy =.nil
  clr.proxy    =.nil

   -- forward all messages to clr.object such that CLR's instance method UNKNOWN handles this
   -- first clr.object is tried (clr.clzProxy), assuming a static member is meant; if this fails
   -- we use clr.type (clr.clzProxy)
/** Class unknown method that first forwards a message to <code>clr.clzProxy</code> which in effect
*   tries to resolve the static context. If this is not successful (the CLR unknown method raises
*   a condition), then another attempt is made by forwarding the message to <code>clr.proxy</code>
*   which resolves the instance context.
*/
::method    unknown      class
  expose clr.object clr.type clr.proxy clr.clzProxy

  if .CLRLogger~doLogging then    -- if not OFF, produce information
      .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR_Class/UNKNOWN CLASS: clr.clzProxy="pp(clr.clzProxy)", clr.proxy="pp(clr.proxy)", forwarding message" pp(arg(1) "to clr.clzProxy first (class/static scope)"))

   -- without the "continue" subkeyword we get no chance to intercept a raised condition in CLR's unknown method
   -- (FORWARD without will not return, even in the case of a raised condition)
  signal on syntax name syntax1
  forward to (clr.clzProxy) continue  -- try class proxy first (assuming a static member access)
  if var("RESULT") then return result -- was a result returned, if so, return it as well
  return

syntax1:
      -- ok, not a static, maybe an instance member (like "ToString" for a "System.String" type), let's try it
  signal on syntax name syntax2  -- try once more

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~debug(pp.line(.line) "CLR.CLS/CLR_Class/UNKNOWN CLASS: clr.clzProxy="pp(clr.clzProxy)", clr.proxy="pp(clr.proxy)": condition" pp(condition("C")) "for message" pp(arg(1))" occurred for clr.clzProxy, now forwarding to clr.proxy instead (instance scope)")

  forward to (clr.proxy)      -- try proxy (assuming an instance member access)

syntax2:                      -- oops, another condition got raised, inform the user/programmer
  raise propagate



  /* Method:      init
   * Description: Constructor which creates an instance of CLR based on the supplied class name and
   *              parameters.
   * Arguments:   - param: list of arguments for the CLR constructor
   */

/** Constructor that creates a CLR proxy object by means of reflection. Any supplied arguments
*   will be relayed to CLR.
*/
::METHOD init PUBLIC

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR_Class/INIT: arg()="pp(arg())", arg(1)="pp(arg(1)))

  clz=self~class
  typeName = clz~clr.typeName

  -- is type name actually a wrapper type for primitive values or a String?
  wrapperType=.clr.dir~typeName2indicator[typeName]
  -- ok, we directly create an instance of the wrapper class or System.String, so only one argument is allowed!
  if wrapperType<>.nil THEN    -- use boxing instead
  DO
      argValue=arg(1)
      if arg()>1 | \argValue~isA(.string) then
      do
         if arg()>1 then
          raise syntax 40.900 array ("CLR.CLS/CLR_CLASS::INIT, wrapping a primitive value: only one argument allowed, however there are" pp(arg()) "arguments supplied")

         raise syntax 40.900 array ("CLR.CLS/CLR_CLASS::INIT, wrapping a primitive value: argument must be a Rexx string, found:" pp(argValue) "(instance of" pp(argValue~class)")")
      end

      jObj=.clr.dir~helper4rexx~clr_box(wrapperType, argValue)  -- box the value
        -- set this instance's attributes accordingly
      self~clr.object =jObj
      self~clr.type=jObj~getType
      return
  END

  clrType = clz~clr.object  -- CLR type that gets instantiated
  argCount = arg()             -- number of supplied args for contructor, if any

  argsTypes=bsf.createJavaArray(.clr.dir~system.type, argCount)
  argsList=bsf.createJavaArray(.clr.dir~system.object, argCount)

  IF argCount = 0 THEN  -- no arguments, use default constructor
  do
    constructorInfo=clrType~GetConstructor(argsTypes)   -- get constructor
    jObj=constructorInfo~Invoke(argsList)
  end
  ELSE  -- arguments need to be wrapped in an array to be passed to appropriate constructor
  DO
    argsList = bsf.createJavaArray(.clr.dir~system.object, argCount)

    DO i = 1 TO argCount
      if .CLRLogger~doLogging then    -- if not OFF, produce information
         .CLRLogger~debug(pp.line(.line) "CLR.CLS/CLR_Class/INIT: parsing arg("i")" pp(arg(i)))
      a = clr.wrap(arg(i))     -- make sure argument is a CLR object

      .ArgUtil~validateClass("arg("i")", a, .clr)   -- check for correct type
      if .CLRLogger~doLogging then    -- if not OFF, produce information
         .CLRLogger~debug(pp.line(.line) "CLR.CLS/CLR_Class/INIT: parsed arg("i"):" "clr.object=" pp(a~clr.object) "clr.type="pp(a~clr.type))
      argsList[i]  = a~clr.object
      argsTypes[i] = a~clr.type
    END

    constructorInfo=clrType~GetConstructor(argsTypes)   -- get constructor
    jObj=constructorInfo~Invoke(argsList)
  END

  self~clr.object = jObj
  self~clr.type   = jObj~getType

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~debug(pp.line(.line) "CLR.CLS/CLR_Class/INIT: created CLR_CLASS instance of" pp(typeName) "with clr.object" pp(self~clr.object) "and clr.type" pp(self~clr.Type))


/* ====================================================================================================== */

/** Class to allow to wrap Enum values in order to ease comparing with Rexx strings. It implements the
*   Rexx comparison methods "=", "\=", "<>", and "><" which in turn accept as the "other" object either
*   another CLR Enumeration object (a proxy for an instance of a "System.Enum" type) or
*   a Rexx string representing the Enum string value.
*/

::CLASS "CLR_Enum" PRIVATE SUBCLASS CLR_PROXY

/** The equal comparison method. If "other" is a Rexx string then the comparison is carried out caselessly.
*
* @param other mandatory value, either a CLR proxy for a "System.Enum" instance or a Rexx string
*
* @return .true if the this value and "other" value are the same, .false else
*
*/
::method "="   -- override comparison method
  use strict arg otherValue

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR_Enum/""="": otherValue="pp(otherValue))

  jObj =self~clr.object     -- get BSF (Java) reference object

  if otherValue~isA(.clr) then      -- let CLR's System.Enum.Equals() carry out the comparison
     return jobj~equals(otherValue~clr.object)

      -- if a string, try to get the System.Enum object matching it, then use it to compare, otherwise return .false
  if otherValue~isA(.string) then
  do
     bIgnoreCase=.true
     otherEnumValue=jObj~Parse(self~clr.type, otherValue, bIgnoreCase)
     return jobj~equals(otherEnumValue)
  end

  return jobj~equals(otherValue)      -- maybe a .bsf reference; anyway System.Enum.Equals() will return .false, if not the same

/** The unequal comparison method. If "other" is a Rexx string then the comparison is carried out caselessly.
*
* @param other mandatory value, either a CLR proxy for a "System.Enum" instance or a Rexx string
*
* @return .true if the this value and "other" value are not the same, .false else
*/
::method "\="  -- override comparison method
  use strict arg otherValue

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR_Enum/""<>"": otherValue="pp(otherValue))

  jObj =self~clr.object     -- get BSF (Java) reference object

    if otherValue~isA(.clr) then      -- let CLR's System.Enum.Equals() carry out the comparison
       return \jobj~equals(otherValue~clr.object)

        -- if a string, try to get the System.Enum object matching it, then use it to compare, otherwise return .false
    if otherValue~isA(.string) then
    do
       bIgnoreCase=.true
       otherEnumValue=jObj~Parse(self~clr.type, otherValue, bIgnoreCase)
       return \jobj~equals(otherEnumValue)
    end
    return \jobj~equals(otherValue)      -- maybe a .bsf reference; anyway System.Enum.Equals() will return .false, if not the same

/** A synonym for method "\=", to which the invocation gets forwarded to. */
::method "<>"  -- override comparison method
  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR_Enum/""><""")
  forward message "\="

/** A synonym for method "\=", to which the invocation gets forwarded to. */
::method "><"  -- override comparison method
  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR_Enum/""\=""")
  forward message "\="



/* ====================================================================================================== */

/* Class:       CLREvent
 * Description: Handles assignment and deassignment of event handlers.
 */

/** A proxy class to ease adding and removing event handler to/from CLR objects.
*/
::CLASS "CLR_Event" PRIVATE SUBCLASS CLR_Proxy

/** Constructor.
* @param o the CLR object for which event handling should become possible
* @param eventName the event name for which adding and removing event handler should become possible
*/
  ::method init            -- constructor
    expose eventName
    use strict arg o, eventName

   if .CLRLogger~doLogging then    -- if not OFF, produce information
      .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR_Event/INIT, o="pp(o)", eventName="pp(eventName))

    forward class (super) array (o)  -- let superclass finalize initialisation


/** Attribute getter method to allow querying the event name for which this object allows to add or remove event handlers. */
::attribute eventName get

/** Method to add an event handler for the named event <code>eventName</code> of the CLR object.
*
*  @param eventHandler a Java Rexx proxy to be added as an event handler
*  @return self
*/
  ::METHOD "+" PUBLIC
    EXPOSE eventName
    USE ARG eventHandler

    if .CLRLogger~doLogging then    -- if not OFF, produce information
       .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR_Event/""+"": adding eventHandler" pp(eventHandler) "for eventName:" pp(eventName))

    addMethod = "add_" || eventName    -- jni4net reflects the method to add an event handler with "add_" and the event name
    self~send(addMethod, eventHandler) -- send the message
    RETURN self


  /* Method:      "-"
   * Description: Is called upon deassignment of an event handler with "-=".
   * Arguments:   - eventHandler: the event handler created by clr.createEventHandler
   * Returns:     self
   */
/** Method to remove an event handler for the named event <code>eventName</code> from the CLR object.
*
*  @param eventHandler a Java Rexx proxy serving as an event handler that should be removed
*  @return self
*/
  ::METHOD "-" PUBLIC
    EXPOSE eventName
    USE ARG eventHandler

    if .CLRLogger~doLogging then    -- if not OFF, produce information
       .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLR_Event/""-"": removing eventHandler" pp(eventHandler) "for eventName:" pp(eventName))

    removeMethod = "remove_" || eventName  -- jni4net reflects the method to remove an event handler with "remove_" and the event name
    self~send(removeMethod, eventHandler) -- send the message
    RETURN self


/* ====================================================================================================== */

/* Class:       CLRThread
 * Description: Provides the environment to start threads interacting with .NET objects. To use it,
 *              the ooRexx class needs to subclass CLRThread and override the "run" method. It must
 *              then be started with the "start" method to actually start a new thread.
 */

 /** A class that uses "java.lang.Thread" to create a thread for executing Rexx code in the context
 *   of CLR/.Net concurrently. To take advantage of this ooRexx class it is necessary to subclass it,
 *   and to implement the abstract method "run".
 */
::CLASS CLRThread PUBLIC

  /* Method:      start
   * Description: Creates a new thread which is able to flawlessly interact with .NET objects in
   *              different threads. Must be called to execute the "run" method in a new thread.
   * Arguments:   - rexxData: optional argument to be included in the proxies slotDir~userdata value
   */

/** This method creates a new thread.
*
* @param rexxData optional, if supplied this Rexx object gets supplied, if a Java method invocation
*                 causes any Rexx method of the subclassed Rexx class to be executed. In that case
*                 BSF4ooRexx will append a "slotDir" Rexx directory argument to the Java arguments,
*                 that will have an entry "USERDATA" returning this Rexx object "rexxData"
*
*/
  ::METHOD start PUBLIC
    USE strict ARG rexxData = .nil

    if .CLRLogger~doLogging then    -- if not OFF, produce information
       .CLRLogger~trace(pp.line(.line) "CLR.CLS/CLRThread/START: rexxData="pp(rexxData))

    rexxRunnableProxy = BsfCreateRexxProxy(self, rexxData, "java.lang.Runnable")
    rexxThread = .bsf~new("java.lang.Thread", rexxRunnableProxy)
    rexxThread~bsf.dispatch("start")

  /* Method:      run
   * Description: Needs to be overridden by specialising ooRexx class.
   */
/** Abstract method that needs to be implemented in the subclassed Rexx class.
*/
  ::METHOD run ABSTRACT

/** Private utility method to "pretty-print" ooRexx line numbers in debug statements. Intended
*   for taking advantage of the ooRexx class <code>CLRLogger</code> in this Rexx package.
*/
::routine pp.line private
  use strict arg lineNr
  return "line #" lineNr~right(4) "|"