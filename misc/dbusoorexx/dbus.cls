#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (C) 2011
      date:       2011-08-07  (2011-06-11, 2011-07-19)
      name:       dbus.cls
      purpose:    make the DBUS language binding for ooRexx available
      needs:      - dbus-message bus system installed (by default available on Linuxes)
                  - ooRexx 4.1.0 or higher, download from <http://www.oorexx.org>
                  - BSF4ooRexx, if using the utf8-conversion routines, download from
                    <https://sourceforge.net/projects/bsf4oorexx/>

      credits:    Mike F. Cowlishaw: for making his "utf8"-Rexx procedure available (cf. routine "stringToutf8")

      cf.:        dbus-specifications: <http://dbus.freedesktop.org/doc/dbus-specification.html> (as of 2011-07-14)

      version:    099.20110823

      changes:    - 2011-07-20, rgf; DBusProxyObject:
                    - added proxy.dispatch(methodName[,args...])-method to send messages that
                      would be otherwise executed on the Rexx side, ie. all of .Object's methods
                  - 2011-07-24, rgf, added class IntrospectHelper: allows Rexx programmers to
                     easily create introspect data (for development purposes, if using unpublished
                     interfaces etc.)
                  - 2001-07-25, rgf
                    - fixed error in setting up argument array ind DBusProxyObject's
                      unknown method ("do...over" inappropriate, rather "do i=1 to args~size" !)
                    - routine IDBus.dumpIDBus(): sort output by node, interface, methods
                  - 2011-07-27, rgf
                    - added helper class .IDBusPathMaker to make it easy to allow d-feet
                      et al to introspect for object paths in order to become able to really
                      query the introspect data of the object
                  - 2011-07-28, rgf
                     - fixed subtle bug in dealing with property Get()-calls in DBusServiceObject
                  - 2011-07-30, rgf
                     - added DBus.getObjectPaths method, returns an array of communicated object paths
                  - 2011-08-03, rgf
                     - added IDBus.count(kindName)
                  - 2011-08-04, rgf
                     - added routine 'raiseDBusError' which causes a DBus error reply from the service object
                  - 2011-08-05, rgf
                     - added to .DBUS: attributes 'collectStatistics' (boolean) and 'statistics' (directory)
                     - changed: .DBUS.getObjectPaths - will now attempt a busname derived object path, if the
                                standard approach does not yield result(s)
                  - 2011-08-07, rgf
                     - finalized the DBusServer functionality, which allows for creating prvate/standalone
                       dbus servers
                  - 2011-08-13, rgf
                     - added "watchConnections" method to DBusServer, which asynchroneously probes client
                       connections to see whether the clients are gone in the meantime; commented, because
                       there are impacts on the clients and dbus should work correctly by sending the
                       "Disconnected" signal on a client connection that is discarded; removed it as probing
                       has adverse effects
                  - 2011-08-14, rgf
                     - adding Mike F. Cowlishaw's "utf8" procedure for "stringToUtf8", in case BSF.CLS is
                       not required
                  - 2011-08-17, rgf
                     - re-activated "watchConnections" method, this time employing "dbus_connection_get_is_connected(conn)" instead
                  - 2011-08-18, rgf
                     - define attribute 'sendEmptyReply' on DBus, default: .true; if .true, then a method
                       call to a Rexx server object will reply, even if no return values are defined for it
                  - 2011-08-21, rgf
                      - added method "supports" to "DBus": currently returns all supported typecodes or tests
                        individual type codes (e.g. "h" cannot be sent on Windows and therefore is not supported there)
                  - 2011-08-23, rgf
                      - added attribute "haltAllThreadsOnUnexpectedError" to "DBus": if .true and an unexpected
                        Rexx condition is raised while servicing a request, then all Rexx threads will get halted
                        (intended to stop/abend program)
                  - 2011-08-26, rgf: close all connections if in the DBusServer watchLoop a halt-condition arises (via native code)


      license:    Apache License 2.0

------------------------ Apache Version 2.0 license -------------------------
   Copyright 2011 Rony G. Flatscher

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


/* Defining dbus shared constants, leaving the dbus names untouched, such that
   dbus-samples employing these constants can be easily transcribed to Rexx.
   Cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusShared.html>
*/
.local~dbus.dir=.directory~new

.dbus.dir~version="099.20110823"    -- current version of DBUS.CLS, released will start with: '100.yyyymmdd'

.dbus.dir~DBUS_SERVICE_DBUS            ="org.freedesktop.DBus"    -- "The bus name used to talk to the bus itself."
.dbus.dir~DBUS_PATH_DBUS               ="/org/freedesktop/DBus"   -- "The object path used to talk to the bus itself."
   -- same definition, but without category and C-style "_" between names:
.dbus.dir~     ServiceDBus             ="org.freedesktop.DBus"    -- "The bus name used to talk to the bus itself."
.dbus.dir~     PathDBus                ="/org/freedesktop/DBus"   -- "The object path used to talk to the bus itself."


.dbus.dir~DBUS_INTERFACE_DBUS          ="org.freedesktop.DBus"    -- "The interface exported by the object with DBUS_SERVICE_DBUS and DBUS_PATH_DBUS."
.dbus.dir~DBUS_INTERFACE_INTROSPECTABLE="org.freedesktop.DBus.Introspectable" -- "The interface supported by introspectable objects."
.dbus.dir~DBUS_INTERFACE_PROPERTIES    ="org.freedesktop.DBus.Properties"    -- "The interface supported by objects with properties."
.dbus.dir~DBUS_INTERFACE_PEER          ="org.freedesktop.DBus.Peer" -- "The interface supported by most dbus peers."
   -- same definition, but without category and C-style "_" between names:
.dbus.dir~               DBus          ="org.freedesktop.DBus"    -- "The interface exported by the object with DBUS_SERVICE_DBUS and DBUS_PATH_DBUS."
.dbus.dir~               Introspectable="org.freedesktop.DBus.Introspectable" -- "The interface supported by introspectable objects."
.dbus.dir~               Properties    ="org.freedesktop.DBus.Properties"    -- "The interface supported by objects with properties."
.dbus.dir~               Peer          ="org.freedesktop.DBus.Peer" -- "The interface supported by most dbus peers."

.dbus.dir~DBUS_NAME_FLAG_ALLOW_REPLACEMENT=1  -- "Allow another service to become the primary owner if requested."
.dbus.dir~DBUS_NAME_FLAG_REPLACE_EXISTING =2  -- "Request to replace the current primary owner."
.dbus.dir~DBUS_NAME_FLAG_DO_NOT_QUEUE     =4  -- "If we can not become the primary owner do not place us in the queue."
   -- same definition, but without category and C-style "_" between names:
.dbus.dir~               AllowReplacement =1  -- "Allow another service to become the primary owner if requested."
.dbus.dir~               ReplaceExisting  =2  -- "Request to replace the current primary owner."
.dbus.dir~               DoNotQueue       =4  -- "If we can not become the primary owner do not place us in the queue."

.dbus.dir~DBUS_REQUEST_NAME_REPLY_PRIMARY_OWNER=1   -- "Service has become the primary owner of the requested name."
.dbus.dir~DBUS_REQUEST_NAME_REPLY_IN_QUEUE     =2   -- "Service could not become the primary owner and has been placed in the queue."
.dbus.dir~DBUS_REQUEST_NAME_REPLY_EXISTS       =3   -- "Service is already in the queue."
.dbus.dir~DBUS_REQUEST_NAME_REPLY_ALREADY_OWNER=4   -- "Service is already the primary owner."
   -- same definition, but without category and C-style "_" between names:
.dbus.dir~                        PrimaryOwner =1   -- "Service has become the primary owner of the requested name."
.dbus.dir~                        InQueue      =2   -- "Service could not become the primary owner and has been placed in the queue."
.dbus.dir~                        Exists       =3   -- "Service is already in the queue."
.dbus.dir~                        AlreadyOwner =4   -- "Service is already the primary owner."

.dbus.dir~DBUS_RELEASE_NAME_REPLY_RELEASED    =1    -- "Service was released from the given name."
.dbus.dir~DBUS_RELEASE_NAME_REPLY_NON_EXISTENT=2    -- "The given name does not exist on the bus."
.dbus.dir~DBUS_RELEASE_NAME_REPLY_NOT_OWNER   =3    -- "Service is not an owner of the given name."
   -- same definition, but without category and C-style "_" between names:
.dbus.dir~                        Released    =1    -- "Service was released from the given name."
.dbus.dir~                        NonExistent =2    -- "The given name does not exist on the bus."
.dbus.dir~                        NotOwner    =3    -- "Service is not an owner of the given name."

.dbus.dir~DBUS_START_REPLY_SUCCESS        =1        -- "Service was auto started."
.dbus.dir~DBUS_START_REPLY_ALREADY_RUNNING=2        -- "Service was already running."
   -- same definition, but without category and C-style "_" between names:
.dbus.dir~                 Success        =1        -- "Service was auto started."
.dbus.dir~                 AlreadyRunning =2        -- "Service was already running."

   -- DBUS datatypes and a more human readable rendering
dataTypes=.directory~new
dataTypes[""] ="void"
dataTypes["y"]="byte"
dataTypes["b"]="boolean"
dataTypes["n"]="int16"
dataTypes["q"]="uint16"
dataTypes["i"]="int32"
dataTypes["u"]="uint32"
dataTypes["x"]="int64"
dataTypes["t"]="uint64"
dataTypes["d"]="double"
dataTypes["s"]="string"
dataTypes["o"]="objpath"
dataTypes["g"]="signature"
dataTypes["h"]="unix_fd"
dataTypes["a"]="array"     -- container
dataTypes["v"]="variant"   -- container
-- dataTypes["e"]="dict"      -- container: not directly used, rather "a{xy}"
-- dataTypes["r"]="struct"    -- container: not directly used, rather "(abc)"
.dbus.dir~dataTypes=dataTypes -- save the data type defs

-- try to require BSF4ooRexx dynamically
call dynamicRequiresBSF4ooRexx

::requires "dbusoorexx" LIBRARY  -- get access to the native routines and methods

::requires "rgf_util2.rex"    -- installed with the BSF4ooRexx package

::routine dynamicRequiresBSF4ooRexx -- BSF4ooRexx: needed need for UTF-8 conversion routines
  signal on any
  .context~package~loadPackage("BSF.CLS") -- if not found a syntax condition is raised and
                                          -- control transferred to (immediately following) "ANY:" label
any:
  return



/* =============================================================================== */
/* Define the DBus class, which allows connecting, getting objects to talk to,
   adding own servers on the bus.
*/
::class "DBus" public

/* --- class methods ------ */
::method init        class
  expose system session
  system=.nil
  session=.nil


   /* return version string, includes version of dbus library that gets used */
-- instead use public routine "DbusVersion()"
-- ::method version     class external "LIBRARY dbusoorexx DbusGetVersion"


   /* unique id for the computer                         */
::method machineID   class external "LIBRARY dbusoorexx DbusGetUniqueMachineId"

::method system      class    /* returns the shared system bus connection */
  expose system
  if system=.nil then         /* if not available yet, get and rember it   */
     system=self~connect("system")
  return system

::method session     class    /* returns the shared session bus connection */
  expose session
  if session=.nil then        /* if not available yet, get and rember it   */
     session=self~connect("session")
  return session


::method getSystemBus  class private external "LIBRARY dbusoorexx DbusNativeGetSystemBus"
::method getSessionBus class private external "LIBRARY dbusoorexx DbusNativeGetSessionBus"


::method connect     class    /* connect to an existing dbus connection    */
  -- use strict arg address -- "address" can also be "system", "session"
  forward message ("NEW")

/* Creates a new bus instance. */

::method nativeConnectPrivate private external "LIBRARY dbusoorexx DbusNativeConnectionOpenPrivate"

::method new         class
  expose system session
  use strict arg address

  signal on syntax
  lcAddress=address~strip~lower
  if wordpos(lcAddress, "session system")>0 then
  do
     if lcAddress="system" then
     do
        if system<>.nil then        /* shared message bus already cached, use it */
           return system

        bus=self~getSystemBus(self~new:.class)
        system=bus
     end
     else
     do
        if session<>.nil then       /* shared message bus already cached, use it */
           return session

        bus=self~getSessionBus(self~new:.class)
        session=bus
     end
  end
  else      -- address was given
  do
     bus=self~new:.class   -- return barely initialized
     bus~busType="private" -- rest will be initialized from native code

     if address<>"native" then
     do
         bus~nativeConnectPrivate(address) -- create a private connection to the address
     end

  end
  return bus

syntax: raise propagate    -- raise in caller

/* --- instance methods --- */
/* ::method init: everything is implemented in native code, including setting attributes
                  'address', 'uniqueBusName' and 'wellKnownBusName', if given
*/

/* controls whether a received message call without defined return value will return an empty message reply */
::attribute sendEmptyReply    get
::attribute sendEmptyReply    set       -- default: .true (append slotDir as last argument)
  expose sendEmptyReply
  use strict arg argSendEmptyReply=.true

  signal on syntax
  if \datatype(argSendEmptyReply, "O") then
     raise syntax 88.900 array ('Argument "argSendEmptyReply" must be a logical value, found "'argSendEmptyReply'"')

  sendEmptyReply=argSendEmptyReply
  return
syntax:
  raise propagate

   /* attribute controls whether the arguments from a signal or a remote method call get
      appended with a slotDir-directory supplying possibly useful information about the
      DBUS message; default: .true
   */
::attribute makeSlotDir      get
::attribute makeSlotDir      set       -- default: .true (append slotDir as last argument)
  expose makeSlotDir
  use strict arg argMakeSlotDir=.true

  signal on syntax
  if \datatype(argMakeSlotDir, "O") then
     raise syntax 88.900 array ('Argument "argMakeSlotDir" must be a logical value, found "'argMakeSlotDir'"')

  makeSlotDir=argMakeSlotDir
  return
syntax:
  raise propagate

   /* attribute controls whether the arguments from a reply (result of calling a message on
      a remote DBUS object) will get appended with a slotDir-directory supplying possibly
      useful information about the DBUS message; default: .false
   */
::attribute makeReplySlotDir get
::attribute makeReplySlotDir set       -- default: .false (do not append slotDir to result of a message call (turning result into an array object)
  expose makeReplySlotDir
  use strict arg argMakeReplySlotDir=.true

  signal on syntax
  if \datatype(argMakeReplySlotDir, "O") then
     raise syntax 88.900 array ('Argument "argMakeReplySlotDir" must be a logical value, found "'argMakeReplySlotDir'"')

  makeReplySlotDir=argMakeReplySlotDir
  return
syntax:
  raise propagate

   /* attribute controls whether byte arrays (signature 'ay') should be unmarshalled as
      Rexx string instead of a Rexx array with individual bytes
   */
::attribute unmarshalByteArrayAsString get
::attribute unmarshalByteArrayAsString set       -- default: .false (do not append slotDir to result of a message call (turning result into an array object)
  expose unmarshalByteArrayAsString
  use strict arg argUnmarshalByteArrayAsString=.true

  signal on syntax
  if \datatype(argUnmarshalByteArrayAsString, "O") then
     raise syntax 88.900 array ('Argument "argUnmarshalByteArrayAsString" must be a logical value, found "'argUnmarshalByteArrayAsString'"')

  unmarshalByteArrayAsString=argUnmarshalByteArrayAsString
  return
syntax:
  raise propagate

::attribute collectStatistics       -- default: .true (keep slotDirs of last received messages/errors/signals and their respective counters)
::attribute statistics              -- a directory
::attribute haltAllThreadsOnUnexpectedError  -- if set to .true, then an unexpected Rexx condition will cause all Rexx threads to be halted

::method init
  expose cself busType makeSlotDir makeReplySlotDir active -
         internalRegisteredServiceObjects internalSignalListeners -
         unmarshalByteArrayAsString -
         collectStatistics statistics server sendEmptyReply -
         haltAllThreadsOnUnexpectedError

  cself=.nil                  /* will be set by a native method   */
  busType=.nil                /* will be set by native method     */
  server=.nil                 /* if a client connection to a private Rexx server this will be set by the native method  */
  makeSlotDir=.true           /* by default create and pass slotDir as last argument on received signals or method calls */
  makeReplySlotDir=.false     /* by default do not create a slotDir for a regular message reply; if set to .true
                                 the return value, if it is not an array will be placed as the first value
                                 into a newly created array; the slotDir value will be appended to the array */
  active=.nil                 /* native code: sets to .true, if connection is being listened to, .false, if connection got closed */
  internalRegisteredServiceObjects=.directory~new
  internalSignalListeners         =.array~new
  unmarshalByteArrayAsString      =.false /* if .true, then return 'ay' as strings */

  sendEmptyReply                  =.true  /* if .true, then sends an empty reply upon a message call */

  haltAllThreadsOnUnexpectedError=.false  -- set to .true when all running threads should be halted, if the
                                          -- there is an unexpected Rexx condition while servicing a request

  collectStatistics               =.true  /* if .true, simple statistics are gathered and stored in 'statistics' */
  statistics                      =.directory~new
  statistics~started=.dateTime~new  -- remember this connection's creation time, define initial counter values
  statistics~countSentErrors      =0
  statistics~countSentMessages    =0
  statistics~countSentSignals     =0
  statistics~countReceivedErrors  =0
  statistics~countReceivedMessages=0
  statistics~countReceivedSignals =0


::method nativeStartMessageLoop   private external "LIBRARY dbusoorexx DbusMessageLoop"

::method waitOnMessageLoop private unguarded  -- block invocation until message loop stopped
  expose active
  use arg value

  guard on when active=value  -- resume, once the attribute gets the received Boolean value


::method messageLoop unguarded      -- query, start, stop messageLoop, waitUntilStopped
  expose active
  parse upper arg action .

-- say "self="pp2(self) "self~active="pp2(active)

  act=action~left(1)
  if pos(act,"AW")=0 then
     act=action~left(3) -- "STA", "STO"

  pos=wordpos(act, "A STA STO W")

  if pos=0 then
     raise syntax 88.916 array ('"action"', '"Sta[rt]", "Sto[p]", "A[ctive]", "W[ait]"', action)

  if act="A" then return active  -- messageLoop active/running? can be .true, .false

  if act="W" then
  do
      use strict arg nixi, value=.false
      if datatype(value,'o')=.false then  -- not a Boolean value ?
         raise syntax 88.900 array ('Argument "waitForValue" must be a logical value, found "'value'"')

      self~waitOnMessageLoop(value) -- block until attribute 'active' assumes 'value'
      return
  end

  if act="STO" then        -- stop message loop
  do
     if active=.true then
     do
        guard on
        active=.false
        guard off
     end
     return
  end

  if active=.true then return -- already running

   -- start message loop
  reply              -- start a new thread and start message loop
  signal on syntax
   -- attribute "active" must be .false at this time, will be set to .true in native code !
  self~nativeStartMessageLoop -- start message loop
  return

syntax: raise propagate


::method nativeCloseConnection    private EXTERNAL "LIBRARY dbusoorexx DbusNativeConnectionClose"
::method close             -- close (private) connection
  expose busType server statistics

  self~messageLoop("stop")    -- stop message loop, independent of the connection's type
  statistics~closed=.dateTime~new   -- remember time of closing
  if busType="private" then   -- if a private connection, then truly close it
  do
-- send the "Disconnected" signal to the server, doesn't work :(
-- self~message("signal", "/org/freedesktop/DBus/Local", "org.freedesktop.DBus.Local", "Disconnected", "")

     self~nativeCloseConnection
--     say self ":" "! ---> NATIVE CLOSE CONNECTION GOT CARRIED OUT <--- !" "busType:" pp2(busType) "self:" pp2(self) "server:" pp2(server)
  end

/* Fold functions/methods that query the state or abilities of a connection.
   "Authenticated" or "IsAuthenticated" are synonyms: returns .true, if the connection was authenticated, .false else
   "O" or "C" or "IsO" or "IsC" are synonyms: return .true, if the connection is open, .false else
   "Supports" or "TypeCode" are synonyms: if no second argument, returns all supported type codes; if
                                          second argument given, returns .true, if all type codes are supported, .false if any one is not supported
*/
::method query
  parse upper arg option +1 1 option3 +3 .      -- get first char and first three chars in uppercase

  optionPos=pos(option, "ACOT")
  if optionPos=0 then    -- option not identified just as of yet
  do
     option3Pos=wordPos(option3, "ISA ISC ISO")
     if option3Pos>0 then
     do
        option="AOO"~subChar(option3Pos)        -- set option
     end
     else
     do
        raise syntax 88.916 array ('"option"', '"A[uthenticated]" or "IsA[uthenticated]", "O[pen]" or "IsO[pen]" or "C[onnected]" or "IsC[onnected]", "T[ypecode]"', arg(1))
     end
  end
  else
  do
     if option="C" then option="O"  -- test "Open" functionality
  end

  if option="A" then return self~nativeIsAuthenticated
  if option="O" then return self~nativeIsConnected

  if arg()<=1 then return self~canSendTypeCode  -- return list of all supported type codes

  use strict arg nix, char  -- fetch arguments
  return self~canSendTypeCode(char)    -- test whether given type codes are supported


::method nativeIsConnected       private external "LIBRARY dbusoorexx DbusConnectionIsConnected"
::method nativeIsAuthenticated   private external "LIBRARY dbusoorexx DbusConnectionIsAuthenticated"
/*
::method isOpen            -- returns .true if connected (open), .false else; synonym for "isConnected"
  return self~nativeIsConnected

::method isConnected       -- returns .true if connected (open), .false else
  return self~nativeIsConnected

::method isAuthenticated
  return self~nativeIsAuthenticated
*/

-- keep these private so to not blow up the amount of methods one needs to be aware of
::method nativeCanSendType       private external "LIBRARY dbusoorexx DbusConnectionCanSendType"
/*
      supports() ... return "typeCodes={a,...,x}" ... returns string denoting available types
      supports("T[ypecode]", typecode)            ... returns .false/.true; typecode can be a string: only returns .true then, if all characters are supported type codes
*/
::method canSendTypeCode  private  -- this name is neutral enough to allow extensions, if other connection-related tests are needed
  if arg()=0 then
  do
     str=""
      -- get all indexes, remove "", sort array
     arr=.dbus.dir~dataTypes~allIndexes~~removeItem("")~sort
     do char over arr
        if self~nativeCanSendType(char) then    -- supported
        do
           if str<>"" then str=str","
           str=str||char         -- add supported type code
        end
     end
     --   return "typeCode={"str"}"
     return str
  end

  use strict arg char  -- fetch arguments

/*
  if kind~strip~left(1)~upper<>"T" then
     raise syntax 88.916 array ('"kind"', '"T[ypecode]"', action)
*/

  do i=1 to char~length          -- iterate over characters
     if self~nativeCanSendType(char~subChar(i))=.false then return .false
  end
  return .true



::method uninit
  expose busType cself

--  say self"::UNINIT ! cself="pp2(cself) "busType="pp2(busType) "self:" pp2(self)

  if cself<>.nil then      -- a DBUS connection pointer in hand ?
     self~close            -- make sure connection gets closed


   /* add listener object, syntax:  - a[dd] | r[emove], object [,[interface] [,signalname]]
                                    - g[etListeners]
   */
::method listener unguarded
  expose internalSignalListeners internalRegisteredServiceObjects

  use strict arg action, ...
  .ArgUtil~validateClass("action", action, .string)

  signal on syntax
  mode=action~strip~left(1)~upper
  if mode="A" then         -- add a listener
  do
     use strict arg action, object, interface=.nil, signalName=.nil

     if object~isA(.DBusSignalListener) then
        o=object
     else
        o=.DBUSSignalListener~new(object, interface, signalName)

     guard on
     internalSignalListeners~append(o)
     guard off

     self~messageLoop("start")   -- start message loop

     return .true
  end

  else if mode="R" then    -- remove all listeners that listen to the given interface and name
  do
     use strict arg action, object=.nil, interface=.nil, signalName=.nil
     count=0
     do i=internalSignalListeners~size to 1 by -1
        o=internalSignalListeners~at(i)

        if (object=.nil | o~listenerObject=Object), -    -- .nil=any listener object
           (interface=.nil | o~interface=interface), -   -- .nil=any interface
           (signalName=.nil | o~name=signalName) then    -- .nil=any signalName
        do
          internalSignalListeners~remove(i)
          count += 1
        end
     end

     guard on        -- how many listener and service objects are there ?
     nr=internalSignalListeners~items+internalRegisteredServiceObjects~items
     guard off
/* ??? RGF 2011-08-13
     if nr=0 then    -- no registered listener & server objects, make sure message loop is stopped
        self~messageLoop("stop")
*/

     return count
  end

  else if mode="G" then    -- get and return a copy of the listener array
  do
     return internalSignalListeners~copy
  end

  -- wrong action argument!
  raise syntax 88.916 array ('"action"', '"A[dd]", "R[emove]", "G[etListeners]"', action)

syntax:  raise propagate



   /* add service Rexx object, syntax:  - a[dd], objectPath, rexxObject
                                        - r[emove], objectPath
                                        - g[etRegisteredServiceObjects]
   */
::method serviceObject unguarded
  expose internalSignalListeners internalRegisteredServiceObjects -- a directory
  use strict arg action, objectPath=.nil, rexxObject=.nil

-- say "---> rexxObject="pp2(rexxObject) "rexxObject~isA(.DbusServiceObject):" pp2(rexxObject~isA(.DbusServiceObject)) "self~bustype="pp2(self~bustype) "self~server="pp2(self~server)

  signal on syntax
  if objectPath<>.nil, objectPath<>"default" then
  do
     str=DBusDataType(objectPath,"ObjectPath")  -- object path correctly formed?
     if str<>.true then
        raise syntax 88.917 array ('"objectPath" value "'objectPath'" is not a valid DBus object path')
  end


  parse upper arg action .
  mode=action~strip~left(1)~upper

  if pos(mode,"ARG")=0 then
     raise syntax 88.916 array ('"action"', '"A[dd]", "R[emove]", "G[etRegisteredServiceObjects]"', action)

  if mode="A" then         -- add a listener
  do
     use strict arg action, objectPath, rexxObject  -- now force strict args

     if rexxObject~isA(.DBusServiceObject) then
     do
        rexxObject~service.connection=self         -- set connection in DBusServiceObject
        rexxObject~service.objectPath=objectPath   -- save desired object-Path
     end

     guard on
     oldValue=internalRegisteredServiceObjects[objectPath]
     internalRegisteredServiceObjects[objectPath]=rexxObject
     guard off

     self~messageLoop("start")   -- start message loop

     return oldValue
  end

  else if mode="R" then       -- remove service object(s)
  do
     count=0
     if objectPath=.nil, rexxObject=.nil then   -- remove all object paths!
     do
         guard on
         count=internalRegisteredServiceObjects~items
         internalRegisteredServiceObjects~empty        -- now empty directory
         guard off
     end

     else if objectPath=.nil then     -- remove Rexx service object from all associated objectPaths
     do
        do until res=.nil        -- remove service object from all object paths it has served
           guard on
           res=internalRegisteredServiceObjects~removeItem(rexxObject)
           guard off
           if res<>.nil then count+=1
        end
     end

     else   -- just remove the given object path
     do
        guard on
        count=internalRegisteredServiceObjects~remove(objectPath)<>.nil
        guard off
     end

     guard on        -- how many listener and service objects are there ?
     nr=internalSignalListeners~items+internalRegisteredServiceObjects~items
     guard off

/* ??? RGF 2011-08-13
     if nr=0 then    -- no registered listener & server objects, make sure message loop is stopped
        self~messageLoop("stop")
*/
     return count
  end

  else if mode="G" then    -- get and return a copy of the listener array
  do
     guard on
     copy=internalRegisteredServiceObjects~copy
     guard off
     return copy
  end
  return

syntax:  raise propagate




::attribute cself                   /* native's representation of this dbus object, set in native code */
::attribute busType                 /* bustype for the dbus conneciton, if any;
                                       DBusBusType defines types that will be named
                                       "session", "system", "starter" and maybe "private" in Rexx*/
::attribute server                  /* if set, connection belongs to a DBusServer (i.e. a client connection) */

   /* unique id for the bus                              */
::method    nativeUniqueBusID   private external "LIBRARY dbusoorexx DbusGetUniqueBusId"
::method    uniqueBusID
   expose bustype
   if bustype="private" then return "private:rexx:"self~identityHash
   return self~nativeUniqueBusID


   -- folding into a method busName( "request" | {"release"| "yield"} | "hasOwner" | "uniqueName", name [, flags] )
   /* unique bus name for the dbus connection            */
::method  nativeUniqueBusName   private unguarded external "LIBRARY dbusoorexx DbusGetUniqueBusName"

::method  nativeRequestBusName  private unguarded external "LIBRARY dbusoorexx DbusBusNameRequest"
::method  nativeBusNameHasOwner private unguarded external "LIBRARY dbusoorexx DbusBusNameHasOwner"   -- deprecated!
::method  nativeReleaseBusName  private unguarded external "LIBRARY dbusoorexx DbusBusNameRelease"

   /* folding methods of the "busName" group into one parametrized method,
      cf <http://dbus.freedesktop.org/doc/dbus-specification.html#message-bus-names>

      possible request flags:
          DBUS_NAME_FLAG_ALLOW_REPLACEMENT	0x1
          DBUS_NAME_FLAG_REPLACE_EXISTING	   0x2
          DBUS_NAME_FLAG_DO_NOT_QUEUE	      0x4
          default, if omitted: 7 (all flags ored)

      possible request return values:
         DBUS_REQUEST_NAME_REPLY_PRIMARY_OWNER	1
         DBUS_REQUEST_NAME_REPLY_IN_QUEUE	      2
         DBUS_REQUEST_NAME_REPLY_EXISTS	      3
         DBUS_REQUEST_NAME_REPLY_ALREADY_OWNER	4

      -> constants directly available via .dbus.dir
   */
::method busName unguarded
  parse upper arg function .

  signal on syntax
  firstLetter=function~left(1)
  first3Letters=""
  if pos(firstLetter, "HYU")=0 then
  do
     first3Letters=function~left(3)
     if wordpos(first3Letters,"REQ REL")=0 then
        raise syntax 88.916 array ('"function"', '"REQ[uest]", "H[asOwner]", "REL[ease]" or the synonym "Y[ield]", "U[niqueName]"', function)

     if first3Letters="REQ" then    -- request
     do
         use strict arg nixi, busName, flags=7  -- 1=allowReplacement, 2=replaceExisting, 4=doNotQueue
         return self~nativeRequestBusName(busName, flags)
     end
     else                           -- release (same as Yield)
     do
         use strict arg nixi, busName
         return self~nativeReleaseBusName(busName)
     end
  end
  else
  do
      if firstLetter="Y" then       -- yield==release (same as RELease)
      do
         use strict arg nixi, busName
         return self~nativeReleaseBusName(busName)
      end
      else if firstLetter="H" then
      do
         use strict arg nixi, busName
         return self~nativeBusNameHasOwner(busName)
      end
      else if firstLetter="U" then
      do
         use strict arg nixi
         return self~nativeUniqueBusName
      end
  end
  return

syntax:
  raise propagate



::method  nativeAddMatch        private external "LIBRARY dbusoorexx DbusBusAddMatch"
::method  nativeRemoveMatch     private external "LIBRARY dbusoorexx DbusBusRemoveMatch"

   /* folding methods of the "match" group into one parametrized method */
::method match
  use strict arg action, filter, block=.false

  signal on syntax
  firstLetter=action~strip~left(1)~upper
  if pos(firstLetter,"AR")=0 then
     raise syntax 88.916 array ('"action"', '"A[dd]", "R[emove]"', action)

  if filter~isA(.string)=.false then
     raise syntax 88.914 array ('"filter"', "String")

  if datatype(block,"o")=.false then   -- not a Boolean
     raise syntax 88.917 array ('"block" value "'||block||'" is not of type logical (Boolean)')

  if firstLetter="A" then self~nativeAddMatch(filter, block)
                     else self~nativeRemoveMatch(filter, block)
  return

syntax:
  raise propagate



::method  nativeSignalMessage    private external "LIBRARY dbusoorexx DbusBusSignalMessage"
::method  nativeCallMessage      private external "LIBRARY dbusoorexx DbusBusCallMessage"

   /* folding methods of the "message" group into one parametrized method */
::method message        -- send message to service object
  use strict arg function, ...

  signal on syntax
  firstLetter=function~strip~left(1)~upper
  if pos(firstLetter,"CS")=0 then
     raise syntax 88.916 array ('"function"', '"C[all]", "S[ignal]"', function)

   if firstLetter="C" then       -- call a method
   do
      use strict arg nixi, busName, targetObjectPath, interface="", methodName, replySignature="", argSignature="", ...

      if signature="", arg()>7 then    -- arguments given, but no signature
         signature="s"~copies(arg()-7) -- only supply the String values of the objects

      if signature<>"" then   -- signature given, hence arguments to be expected
         args=arg(8, "a")     -- remaining arguments as an array
      else
         args=.array~new

      if replySignature="" then  -- no result expected
      do
         self~nativeCallMessage(busName, targetObjectPath, interface, methodName, replySignature, argSignature, args)
         return
      end
      else
         return self~nativeCallMessage(busName, targetObjectPath, interface, methodName, replySignature, argSignature, args)
   end


   else if firstLetter="S" then  -- emit a signal
   do
      use strict arg nixi, senderObjectPath, interface, signalName, signature="", ...

      if signature="", arg()>5 then    -- arguments given, but no signature
         signature="s"~copies(arg()-5) -- only supply the String values of the objects

      if signature<>"" then   -- signature given, hence arguments to be expected
         args=arg(6, "a")     -- remaining arguments as an array
      else
         args=.array~new

      self~nativeSignalMessage(senderObjectPath, interface, signalName, signature, args)
   end

  return

syntax:
  co=condition("o")
-- say "in message-syntax:" ppCondition2(co) "... methName="pp2(methName) "signalName="pp2(signalName)

  if co~code=91.999 then   -- "Message "NATIVECALLMESSAGE" did not return a result"
  do                       -- make error message more meaningful by supplying message that caused this instead:
     if interface="" then raise syntax 91.999 array (methodName)
                     else raise syntax 91.999 array (interface"."methodName)
  end

  raise propagate          -- raise any other error in caller



::method getObject                     -- request remote object, return a Rexx proxy object to allow interaction with it
  use strict arg busName, objectPath

  signal on syntax
  res=DBusDatatype(busName,"Busname")
  if res<>.true then                   -- erroneous busname?
     raise syntax 93.900 array("'busname':" res)

  res=DBusDatatype(objectPath,"ObjectPath")
  if res<>.true then                   -- erroneous busname?
     raise syntax 93.900 array("'objectPath':" res)

  return .DbusProxyObject~new(self, busName, objectPath)

syntax:
  raise propagate



::method getObjectPaths    -- return an array of object paths for the supplied bus name / service name
  use strict arg busName

  objectPathList=.array~new
  self~workerGetObjectPaths("/",busName,objectPathList)
  if objectPathList~items=0 then    -- no objects found, try a bus-derived object path
  do
      testObjPath="/" || busname~changeStr('.','/')   -- create object path from busname
      self~workerGetObjectPaths(testObjPath,busName,objectPathList)
  end

  return objectPathList



::method workerGetObjectPaths private     -- interrogates service possessing bus/service name for object paths (if that service supports that)
   use arg objPath, busName, objectPathList

   signal on syntax
   o=self~getObject(busName, objPath)  -- try to get the DBus object
   o~proxy.introspectData              -- get introspect data
   rootNode=o~proxy.introspectRootNode -- get rootNode of analyzed introspect data

   content=rootNode~content~sort       -- process root's subordinate nodes

   do n over content
      if n~isA(.IDBusNode) then     -- process next received fragment for building object path
      do
            -- an Introspection node for the object path in hand
         name=n~name
         if name="" | left(name,1)="/" then
         do
            if n~contains("interface") then     -- make sure the subtree has at least one 'interface' node
               objectPathList~append(objPath)   -- add object path to array
            iterate
         end

            -- o.k. add fragment to object path, retry
         if objPath="/" then self~workerGetObjectPaths(       "/"name,busName,objectPathList)
                        else self~workerGetObjectPaths(objPath"/"name,busName,objectPathList)
      end
   end

   return

syntax:  -- mostlikely: org.freedesktop.DBus.Error.Reply or org.freedesktop.DBus.Error.ServiceUnknown
/*
   co=condition('o')
   if co~additional~at(2)<>.nil then
   do
      say "---> error:" co~additional~at(2)  -- contains the substitution error message
      say "     (hint: you could retry immediately, maybe the service was not listening yet)"
   end
   say ppCondition2(co)
   say "---"
*/
   return



   /* ---------------------------------------------------------------------------------------- */
   /* as of 2011-07-05 ooRexx from trunk does not support UTF-8, hence depending on BSF4ooRexx */
::routine stringToUtf8 public    -- needs BSF4ooRexx
  parse arg str

  if .bsf=".BSF" then   -- no BSF available (need to uncomment the ::requires BSF.CLS statement above), hence using the Rexx conversion procedure
     return utf8(str)

  return BsfRawBytes(.java.lang.String~new(str)~getBytes("UTF-8"))


-- the following code was made available by Mike F. Cowlishaw in his e-mail on the ooRexx
-- Sourceforge developer list on 2011-07-05, many thanks!
/* --------------------------------------------------------------- */
/* UTF-8 encoder (for 00-FF only)                                  */
/* --------------------------------------------------------------- */
utf8: procedure
  parse arg data
  out=''
  do while data\==''                    -- generate escapes
    parse var data char +1 data
    d=c2d(char)
    if d>=128 then do
      bits=x2b(c2x(char))
      c1=x2c(b2x('110000'left(bits, 2)))
      c2=x2c(b2x('10'substr(bits, 3)))
      char=c1||c2
      end
    out=out||char
    end
  return out


   /* ---------------------------------------------------------------------------------------- */
::routine utf8ToString public    -- needs BSF4ooRexx
  parse arg str
  return BsfRawBytes(.java.lang.String~new(bsfRawBytes(str),"UTF-8")~getBytes)


   /* ---------------------------------------------------------------------------------------- */
::routine dbus.box       public     -- for variant types or overruling reply signatures, allow boxing
  use strict arg signature, argument=.nil

  signal on syntax

  res=dbusDataType(signature,"s")

  if res<>.true then
     raise syntax 93.900 array("'signature':" res)

  return .array~of("useThisSignature="signature,argument)
  -- return .array~of("variantSignature="signature,argument)

syntax: raise propagate



   /* ---------------------------------------------------------------------------------------- */
/* allow for sending error-messages instead of a reply back to the invoker, will get honored in
   the message loop of the DBus connection that invoked the Rexx method calling this routine   */
::routine raiseDBusError   public   -- this condition will be turned into an error-message in native code
  use arg errName="org.freedesktop.DBus.Error.RexxServiceRaised", errMessage=.nil
  signal on syntax
  raise syntax 93.900 array (errName, errMessage, "ERRORREPLY")

syntax: raise propagate


/* =============================================================================== */
/* ooRexx proxy for a DBus object, responsible for processing messages  */
::class "DBusProxyObject" public

::method init
  expose proxy.busname proxy.connection proxy.objectPath proxy.methods proxy.introspectData
  use strict arg proxy.connection, proxy.busname, proxy.objectPath

  proxy.introspectRootNode=.nil
  proxy.introspectData    =""

  signal on syntax                  -- query object for its introspection data, if any

  proxy.introspectData=proxy.connection~message("call", proxy.busName,        - -- .dbus.dir~serviceDbus,      - -- busName
                                          proxy.objectPath,             - -- .dbus.dir~pathDBus,         - -- path to DBus object
                                          .dbus.dir~Introspectable,     - -- Introspect's standard interface name
                                          "Introspect",                 - -- message name
                                          "s",                          - -- return signature
                                           "")                            -- arg signature (no args)
   -- create introspect parse tree and save it
  -- self~proxy.introspectRootNode=.IDBus~newIntrospection(introspectionData) -- create parse tree for introspection data
  self~proxy.parseIntrospectData(proxy.introspectData)
  return

syntax:  -- make sure a parse tree is available, as caller may intercept condition and proceed
  self~proxy.introspectRootNode=.IDBus~newIntrospection("")
  raise propagate -- raise condition in caller


::attribute proxy.busname get             /* object's bus name to use    */
::attribute proxy.connection get          /* object's connection to use  */
::attribute proxy.objectPath get          /* object's path on the dbus   */

::attribute proxy.introspectData get      /* make introspection data available       */
::attribute proxy.introspectRootNode get  /* make introspection parse tree available */

::attribute proxy.introspectRootNode set  /* make introspection parse tree available */
  expose proxy.methods proxy.introspectRootNode
  use arg proxy.introspectRootNode
     -- create directory with those names that we should proxy for the proxy object
  proxy.methods=IDBus.getMethodsForProxy(proxy.introspectRootNode)

::attribute proxy.methods  get            /* make introspection parse tree available */


::method proxy.parseIntrospectData      -- parse introspect data, memorize introspectData and introspectFileName, if any
  expose proxy.introspectData proxy.introspectFileName
  use strict arg introData

  signal on syntax
  .ArgUtil~validateClass("introspectData", introData, .string) -- check for correct type

  proxy.introspectFileName=""

      -- 'introData' can be an introspection filename or the file's bytes
  if introData~length<256, sysFileExists(introData) then
  do
     proxy.introspectFileName=introData
     proxy.introspectData    =charin(introData,1,chars(introData)) -- save data
  end
  else
  do
     proxy.introspectData=introData  -- save data
  end

   -- create introspect parse tree and save it
  self~proxy.introspectRootNode=.IDBus~newIntrospection(proxy.introspectData) -- create parse tree for introspection data

  return introData

syntax:  -- make sure a parse tree is available, as caller may intercept condition and proceed
  raise propagate -- raise condition in caller




::METHOD proxy.dispatch                -- allow sending messages that would be intercepted on the Rexx side (e.g. by .Object)
   parse arg methodName
   -- create UNKNOWN format (first argument is unknown method name)
   self~unknown(methodName, arg(2, 'A'))
   if var('RESULT') then return result -- if a return value given, return it
   return


::method unknown        -- implements sending message calls to remote object
  expose proxy.busname proxy.connection proxy.objectPath proxy.methods
  use arg msgName, msgArgs

  signal on syntax
  interface=""
  methName=msgName
  argSignature=""
  replySignature=""

  if proxy.methods<>.nil then          -- special property support (allow using properties as if they were Rexx attributes, i.e. allow sending just the name of the property to get its value)
  do
     bSetter=(msgName~right(1)="=")    -- a Rexx set attribute kind of usage?
     if bSetter then                   -- a setter, remove trailing '='
        msgName=substr(msgName,1, length(msgName)-1)

     m=proxy.methods~entry(msgName)    -- get IDBusMethod object

     if m<>.nil then                   -- method/property definition found, use it
     do
        methName      =m~name
        interface     =m~parent~name
        argSignature  =m~argSignature
        replySignature=m~replySignature

        if bSetter=.true | m~isA(.IDBusPropertyMethod) then  -- a "readwrite" property (matching a Rexx attribute) ?
        do
           tmpArgs=.array~of(interface,methName)  -- property interface & name as arguments
            -- now we can change the value of the interface variable
           interface=.dbus.dir~Properties         -- use the dbus Properties interface
           access=m~access             -- get access type of property
           if msgArgs~items=0, pos('read',access)>0 then
           do
               methName ="Get"                        -- use the "Get" method
               argSignature="ss"                      -- make sure no arguments
               replySignature="v"                     -- expected replySignature
           end
           else if msgArgs~items=1, pos('write',access)>0 then
           do
               methName ="Set"                        -- use the "Get" method
               argSignature="ssv"                     -- make sure no arguments
               replySignature=""                      -- expected replySignature
               tmpArgs~append(msgArgs[1])             -- append passed in argument
           end
           else   -- an error, raise exception with a meaningful error message!
           do
               nrArgs=msgArgs~items    -- number of supplied arguments
               str='Incorrect interaction with property "'m~parent~name'.'m~name'":'

               if nrArgs=0, pos('read',access)=0 then
                  str=str 'argument missing for a "write" property'
               else if nrArgs=1, pos('write',access)=0 then
                  str=str 'attempt to assign the value "'tmpArgs[1]'" to a "read"-only property'
               else
                  str=str 'the number of supplied arguments ('msgArgs~items') does not match the property''s "'access'" access definition'

               raise syntax 93.900 array (str)
           end

           msgArgs=tmpArgs    -- now use our temporary array
        end
     end
  end


  -- build array
  newArgs=.array~of("call", proxy.busName, proxy.objectPath, interface, methName, replySignature, argSignature)
  do i=1 to msgArgs~size
     newArgs~append(msgArgs[i])
  end

  proxy.connection~sendWith("message", newargs)

  if var('RESULT') then return result -- if a return value given, return it

  return

syntax: raise propagate   -- raise condition in caller




/* =============================================================================== */
/* Allows an ooRexx object to listen to DBus signals. Intentionally not public. Will
   be used by method 'listener' subfunction 'add' and will be retrieved by the native
   code.
*/
::class "DBUSSignalListener"

::method init
  expose listenerObject interface signalName
  use strict arg listenerObject, interface=.nil, signalName=.nil

::attribute listenerObject
::attribute interface
::attribute signalName




/* =============================================================================== */
/* Allows an ooRexx object to serve DBus method invocations in an easy manner, by
   adding the ability to e.g. dynamically determine replySignatures from the introspect
   data or make it rather easy for a service object to emit signals, etc.
   Definition of this class allows it to be used as a mixin.
*/
::class "DbusServiceObject" mixinclass object  public

::method init                 -- service.connection, service.objectPath and introData may be supplied later, using the respective methods
  expose  service.connection service.objectPath service.introspectRootNode service.methods
  use strict arg service.connection=.nil, service.objectPath=.nil, introData=.nil

  service.methods=.nil

  signal on syntax
  self~service.parseIntrospectData(introData)
-- say "---> ---> --->" self": introData:  ["introdata"]"
  return

syntax:  -- make sure a parse tree is available, as caller may intercept condition and proceed


  empty='<!DOCTYPE node PUBLIC "-//freedesktop//DTD D-BUS Object Introspection 1.0//EN" ' -
        '"http://www.freedesktop.org/standards/dbus/1.0/introspect.dtd">                ' -
        '<node/>                                                                        '
  self~service.parseIntrospectData(empty)
-- say self": supplying empty introData:  ["introdata"]"
  raise propagate -- raise condition in caller


::method service.parseIntrospectData      -- parse introspect data, memorize introspectData and introspectFileName, if any
  expose service.introspectData service.introspectFileName
  use strict arg introData

  signal on syntax
  .ArgUtil~validateClass("introspectData", introData, .string) -- check for correct type

  service.introspectFileName=""

      -- 'introData' can be an introspection filename or the file's bytes
  if introData~length<256, sysFileExists(introData) then
  do
     service.introspectFileName=introData
     service.introspectData    =charin(introData,1,chars(introData)) -- save data
  end
  else
  do
     service.introspectData=introData  -- save data
  end

   -- create introspect parse tree and save it
  self~service.introspectRootNode=.IDBus~newIntrospection(service.introspectData) -- create parse tree for introspection data

/*
say self"::service.parseIntrospectData:"
call idbus.dumpIDbus self~service.introspectRootNode
*/
  return introData

syntax:  -- make sure a parse tree is available, as caller may intercept condition and proceed
  raise propagate -- raise condition in caller



::method    Introspect              -- return introspection data to client
  expose    service.introspectData
  return dbus.box("s", service.introspectData)

::attribute service.connection               /* object's connection to use  */

::attribute service.objectPath get           /* object's path on the dbus   */
::attribute service.objectPath set           /* object's path on the dbus   */
   expose service.objectPath service.connection
   use arg oPath
   signal on syntax

      -- if this connection is to the client of a DBusServer, then allow resetting service.objectPath (cf. DBusServer:newConnection)
   if service.connection~busType="private", service.connection~server<>.nil, oPath=.nil then
   do
      service.objectPath=.nil
      return
   end

   if service.objectPath<>.nil then
      raise syntax 93.900 array ('Attribute "service.objectPath" is not ".nil" (current value: "'service.objectPath'"), therefore must not accept new value "'arg(1)'")')

   service.objectPath=oPath
   return
syntax: raise propagate


::attribute service.introspectRootNode get   /* make introspection parse tree available */
::attribute service.introspectData get
::attribute service.introspectFileName get

::attribute service.introspectRootNode set   /* make introspection parse tree available */
  expose service.methods service.introspectRootNode
  use arg service.introspectRootNode
     -- create directory with those names that we should proxy for the proxy object
  service.methods=IDBus.getMethodsForProxy(service.introspectRootNode)


::attribute service.methods  get             /* make introspection parse tree available */

::attribute service.replySignature get
  expose service.methods service.objectPath
  use arg member, interface=""

  if service.methods<>.nil then
  do
     if interface="" then
        m=service.methods~entry(member)   -- get IDBusMethod object
     else
        m=service.methods~entry(interface"."member)   -- get IDBusMethod object

     if m<>.nil then                      -- method/property definition found, use it
        return m~replySignature
  end
  return .nil           -- no signature/member found


::method service.sendSignal   -- utility method to ease emitting signals from the service object
  expose service.methods service.connection service.objectPath
  use strict arg member, interface, ...

  signature=""

  if service.methods<>.nil then
  do
     if interface="" then
        m=service.methods~entry(member)   -- get IDBusMethod object
     else
        m=service.methods~entry(interface"."member)   -- get IDBusMethod object

     if m<>.nil then                      -- method/property definition found, use it
     do
        member        =m~name
        interface     =m~parent~name
        signature     =m~argSignature
     end
  end

   -- build arguments for "self~message":
  args=.array~of("signal", service.objectPath, interface, member, signature)
  do i=3 to arg()       -- append arguments to the signal, if any
     args~append(arg(i))
  end

  signal on syntax
  service.connection~sendWith("message", args) -- send the message
  return
syntax:raise propagate

               /* service requests  */
::method unknown
  expose service.methods
  use arg msgName, msgArgs

  errorText='Object "'self'" does not understand message "'msgName'"'

  slotDir=.nil
  if msgArgs~isA(.array), msgArgs~size>0 then
     slotDir=msgArgs[msgArgs~last]  /* contains useful information about method invocation,
                                      like method name, method type, signature, etc.  */

  /* if an org.freedesktop.DBus.Properties Get() or Set() invocation, reroute them to the
     respective Rexx attribute get/set */
  bPropertyGetOrSet=.false

   -- check whether the 'org.freedesktop.DBus.Properties' interface was used and method is either 'Get' or 'Set'
  if slotDir<>.nil, slotDir~interface=.dbus.dir~Properties, pos(slotDir~member,"Get Set")>0 then
  do
      -- test whether the target method is really supposed to be regarded as a property method access
      bPropertyGetOrSet=service.methods~entry(msgArgs[1]"."msgArgs[2])~isA(.IDBusPropertyMethod)
  end

  if bPropertyGetOrSet=.true then
  do
      if slotDir~member="Get" then
      do
         attrName    =msgArgs[2]
         qualAttrName=msgArgs[1]"."msgArgs[2] -- fully qualified name

-- say "..." self": UNKNOWN, processing Get(), attrName="pp2(attrName)", qualAttrName="pp2(qualAttrName) "..."

         if self~hasMethod(qualAttrName) then   -- use the fully qualified name?
            attrName=qualAttrName

         if self~hasMethod(attrName) then       -- necessary for the unqualified name case
         do
            signal on syntax name syntax_get
--            return self~send(attrName, slotDir) -- send slotDir with the message, may not be allowed
               -- using the org.freedsktop.DBus.Properites.Get() which returns always a 'v', therefore
               -- returning a 'v', which Rexx renders as 's', 'as', 'a{ss}' for atomic, array or dict types
            return dbus.box("v", self~send(attrName,slotDir)) -- send slotDir with the message, may not be allowed
         end
      end

      else  -- "Set" method in the attribute fashion: attribute name plus trailing "="
      do
         attrName    =msgArgs[2] || "="
         qualAttrName=msgArgs[1]"."attrName -- fully qualified name

         if self~hasMethod(qualAttrName) then   -- use the fully qualified name
            attrName=qualAttrName
         else if self~hasMethod(attrName)=.false then -- not found ! try a plain method that may behave as getter/setter depending on an argument
         do
            attrName=msgArgs[2]
            qualAttrName=msgArgs[1]"."attrName
            if self~hasMethod(qualAttrName) then
               attrName=qualAttrName
            else if self~hasMethod(attrName)=.false then -- indicate we found no matching method!
               attrName=""
         end

-- say "..." self": UNKNOWN, processing Set(), attrName="pp2(attrName) "to" pp2(msgArgs[3]) "..."
         if self~hasMethod(attrName) then    -- necessary for the unqualified name case
         do
            signal on syntax name syntax_set
            self~send(attrName,msgArgs[3],slotDir)        -- supply the new value attribute is supposed to be set to plus slotDir (may not be allowed)
            return
         end
      end
  end

continue:
  signal on syntax
  if slotDir~isA(.directory), slotDir~hasEntry("OBJECTPATH") then -- a dbus slotDir in hand?
  do
     errorText=errorText -
            '(DBus message type "'slotDir~messageTypeName'": objectPath="'slotDir~objectPath'",' -
             'interface="'slotDir~interface'", member="'slotDir~member'",' -
             'signature="'slotDir~signature'", sender="'slotDir~sender'",' -
             'destination="'slotDir~destination'", at: "'slotDir~dateTime'")'
  end
  raise syntax 97.900 array (errorText)

syntax: raise propagate    -- raise condition in caller


syntax_get:       -- a syntax error occurred (due to wrong number of arguments?)
  co=condition('o')
  if co~code=93.902, co~additional[1]=0 then -- no arguments allowed !
  do
     signal on syntax name continue
     -- return self~send(attrName)
         -- using the org.freedsktop.DBus.Properites.Get() which returns always a 'v', therefore
         -- returning a 'v', which Rexx renders as 's', 'as', 'a{ss}' for atomic, array or dict types
-- say "... syntax_get: self~send("pp2(attrName)"):" pp2(self~send(attrName)) "..."
     return dbus.box("v", self~send(attrName))
  end
  signal continue


syntax_set:       -- a syntax error occurred (due to wrong number of arguments?)
  co=condition('o')
  if co~code=93.902, co~additional[1]=1 then -- only one argument (new value) allowed !
  do
     signal on syntax name continue
     self~send(attrName,msgArgs[3])
     return
  end
  signal continue




/* =============================================================================== */
/* ------------------------------------------------------------------------------------- */
/*  DbusDataType( value [,typeName])
    typeNames: B[usName], I[nterfaceName], M[emberName], O[bjectPath], S[ignature]

    modelled after Rexx' DataType(...)
*/
::routine DbusDataType public    -- check or determine whether dbus datatype
  parse arg value, type .

  signal on syntax

  if \value~isA(.string) then          -- check that we received a string
     raise syntax 88.909 array ('"'value'"')

   -- valid chars for name parts
  alphaChars= XRANGE("A","Z") || XRANGE("a","z")
  numberChars="0123456789"
  allChars=alphaChars || numberChars || "_"

  if type<>"" then
  do
     firstLetter=type~left(1)~upper    -- get first char in uppercase
     if pos(firstLetter, "IBMOS")=0 then
        raise syntax 88.916 array ('"type"', '"B[us]", "I[nterface]", "M[ember]", "O[bjectPath]", "S[ignature]', type)

     if type<>"O", value~length>255 then  -- bus/interface/member names too large
        return .false

     select
         when firstLetter='B' then return check_busname(value, alphaChars, numberChars, allChars)
         when firstLetter='I' then return check_interfaceName(value, alphaChars, numberChars, allChars)
         when firstLetter='M' then return check_memberName(value, alphaChars, numberChars, allChars)
         when firstLetter='O' then return check_objectPathName(value, alphaChars, numberChars, allChars)
         when firstLetter='S' then return check_signature(value, alphaChars, numberChars, allChars)
         otherwise NOP
     end
  end

  if check_objectPathName(value, alphaChars, numberChars, allChars)=.true then
     return "OBJECTPATH"

  if check_interfaceName(value, alphaChars, numberChars, allChars)=.true then
     return "INTERFACENAME"

  if check_busname(value, alphaChars, numberChars, allChars)=.true then
     return "BUSNAME"

  if check_memberName(value, alphaChars, numberChars, allChars)=.true then
     return "MEMBERNAME"

  return .nil                 -- indicate unknown type


----------------------- check bus names -------------------
check_busname: procedure      -- according to <dbus-specification.html>
  parse arg value 1 oriValue, alphaChars, numberChars, allChars

  allChars=allChars || "-"    -- a dash is allowed here in addition !
  deli="."                    -- delimiter character
  if pos(deli,value)=0 then   -- there must be at least one dot
  do
     return "busName ["value"] must have at least one dot"
  end

  firstChar=value~left(1)
  if firstChar=deli then      -- must not begin with a dot
  do
     return "busName ["value"] must not begin with a dot"
  end

  if pos(firstChar,numberChars)>0 then -- must not start with a digit
  do
     return "busName ["value"] must not start with a digit"
  end

  if firstChar=":" then       -- a unique connection name
  do
     value=substr(value,2)    -- check the remainder
     firstChar=value~left(1)
  end

  do while value<>""
     parse var value element (deli) value
     if element="" then
     do
        return "busName ["oriValue"] contains an empty element (no element between two dots)"
     end

     pos=verify(element,allChars)
     if pos<>0 then  -- element does not consist of valid chars
     do
        return "busName ["oriValue"] contains in element ["element"]" -
               "the illegal character '"substr(element,pos,1)"' at position" pos
     end
  end
  return .true                -- checked out o.k.


----------------------- check interface names -------------
check_interfaceName: procedure      -- according to <dbus-specification.html>
  parse arg value 1 oriValue, alphaChars, numberChars, allChars

  deli="."                    -- delimiter character
  if pos(deli,value)=0 then   -- there must be at least one dot
  do
     return "interfaceName ["value"] must have at least one dot"
  end

  firstChar=value~left(1)
  if firstChar=deli then      -- must not begin with a dot
  do
     return "interfaceName ["value"] must not begin with a dot"
  end

  if pos(firstChar,numberChars)>0 then -- must not start with a digit
  do
     return "interfaceName ["value"] must not start with a digit"
  end

  do while value<>""
     parse var value element (deli) value
     if element="" then
     do
        return "interfaceName ["oriValue"] contains an empty element (no element between two dots)"
     end

     pos=verify(element,allChars)
     if pos<>0 then  -- element does not consist of valid chars
     do
        return "interfaceName ["oriValue"] contains in element ["element"]" -
               "the illegal character '"substr(element,pos,1)"' at position" pos
     end
  end
  return .true                -- checked out o.k.


----------------------- check member names ----------------
check_memberName: procedure   -- according to <dbus-specification.html>
  parse arg value, alphaChars, numberChars, allChars

  if value="" then            -- at least one element must exist
  do
     return "memberName ["value"] must not be empty"
  end

  if pos(value~left(1),numberChars)>0 then   -- must not start with a digit
  do
     return "memberName ["value"] must not start with a digit"
  end

  pos=verify(value,allChars)
  if pos<>0 then -- member name value does not consist of valid chars
  do
     return "memberName ["value"] contains" -
            "the illegal character '"substr(value,pos,1)"' at position" pos
  end

  return .true                -- checked out o.k.


----------------------- check object path names -----------
check_objectPathName: procedure      -- according to <dbus-specification.html>
  parse arg value 1 oriValue, alphaChars, numberChars, allChars

  deli="/"                    -- delimiter character

  if value=deli then          -- a root path is always o.k.
     return .true

  firstChar=value~left(1)
  if firstChar<>deli then     -- must begin with a slash
  do
     return "objectPathName ["value"] must start with a '"deli"'"
  end

  if value~right(1)=deli then -- must not end with a slash
  do
     return "objectPathName ["value"] must not end with a '"deli"'"
  end

  value=substr(value,2)       -- cut off first deli

  do while value<>""
     parse var value element (deli) value
     if element="" then
     do
        return "objectPathName ["oriValue"] contains an empty element (no element between two '"deli"')"
     end

     pos=verify(element,allChars)
     if pos<>0 then  -- element does not consist of valid chars
     do
        return "objectPathName ["oriValue"] contains in element ["element"]" -
               "the illegal character '"substr(element,pos,1)"' at position" pos
     end
  end
  return .true                -- checked out o.k.


----------------------- check object path names -----------
check_signature: procedure      -- according to <dbus-specification.html>
  parse arg value 1 oriValue, alphaChars, numberChars, allChars

  allChars="ybnqiuxtdsoga()v{}h"

  if value="" then            -- o.k. to have an empty signature (e.g. no arguments, no return value)
     return .true

  pos=verify(value,allChars)
  if pos<>0 then              -- element does not consist of valid chars
  do
     return "signature ["value"] contains" -
            "unknown typecode '"substr(value,pos,1)"' at position" pos
  end

  arrayOpen=0                 -- number of nested array levels
  structOpen=0                -- number of open structs '('
  dictOpen  =0                -- number of open dicts '{'
  char=""

  do i=1 to value~length
     char=substr(value,i,1)
     if char="a" then
     do
        arrayOpen+=1
     end

     else if char="(" then
     do
        structOpen+=1
     end

     else if char=")" then
     do
        structOpen-=1
        if arrayOpen>0 then
           arrayOpen -=1
     end

     else if char="{" then
     do
        if i=1 then                          -- signature must not begin with a dict-entry (must be preceded by 'a')
        do
           return "signature ["value"] has dict entry not inside array (must be preceded by an 'a')"
           -- return .false
        end

        if substr(value,i-1,1)<>'a' then     -- dict-entry must be preceded by 'a'
        do
           return "signature ["value"] has dict entry not inside array (must be preceded by an 'a')"
           -- return .false
        end

        dictOpen+=1
     end
     else if char="}" then
     do
        dictOpen -=1
        if arrayOpen>0 then
           arrayOpen -=1                     -- closes array
     end
      -- no open nestings, a simple element type in hand: reset array count
     else if dictOpen=0, structOpen=0, arrayOpen>0 then
     do
        arrayOpen=0
     end

     if arrayOpen>32 then
         return "signature ["value"] exceeds maximum array recursion/nesting limit of 32"

     if structOpen>32 then
         return "signature ["value"] exceeds maximum struct recursion/nesting limit of 32"
  end

  if structOpen<>0 then        -- leftover open structs
  do
     if structOpen>0 then
        return "signature ["value"] struct started but not ended, ')' missing"
     else
        return "signature ["value"] struct ended but not started, '(' missing"
  end

  if dictOpen<>0 then          -- leftover open dicts
  do
     if dictOpen>0 then
        return "signature ["value"] dict entry started but not ended, '}' missing"
     else
        return "signature ["value"] dict entry ended but not started, '{' missing"
  end

/* wrong: array closing, if datatype found not accounted for, as DBUSValidate() is used, this is o.k.
  if arrayOpen<>0 then         -- arrays without element type !
  do
     return "signature ["value"] contains array without element type"
  end
*/

  return DbusValidate(value,"s")  -- now let us validate the signature through dbus
  -- return .true                -- checked out o.k.





syntax:
  raise propagate       -- raise in caller



/* as of 2011-07-29: not yet implemented in full in native code, hence not operational */

/* =============================== DBusServer ====================================================== */

/* ------------------------------ class definition ------------------------------ */
::class "DBusServer"   public
::method init
  expose cself address allowAnonymous defaultService defaultListener connections  -
         watchLoopActive watchlist timeoutLoopActive watchConnections
  use strict arg address, defaultService=.nil, defaultListener=.nil, allowAnonymous=.false

  .ArgUtil~validateClass("address", address, .string)

  if \datatype(allowAnonymous, "O") then
     raise syntax 88.900 array ('Argument "allowAnonymous" must be a logical value, found "'allowAnonymous'"')

  connections=.array~new
  watchConnections=.false
  internalRegisteredServiceObjects=.directory~new
  internalSignalListeners         =.array~new
  cself=.nil                           -- will be set by native code
  watchlist=.nil                       -- will be set by native code (a RexxPointer value)
  watchLoopActive=.false               -- set to .true if server message loop started; controlled by native code
  timeoutLoopActive=.false             -- set to .true when timeouts get requested by DBus by native code


::attribute cself                   /* native's representation of this dbus object, set in native code */
::attribute address           get      -- address of the server
::attribute allowAnonymous             -- allow anonymous clients to connect
::attribute defaultService             -- default Rexx service object, used, if no matching entry found in internalRegisteredServiceObjects
::attribute defaultListener            -- default Rexx listener object, used, if internalSignalListeners is empty
::attribute connections get            -- list of established connections
  expose connections
  return connections~copy              -- return a copy of the current connections

::attribute watchLoopActive  get       -- indicates whether the server message loop is active or not

::method newConnection     -- new connection to this server received (from native code)
  expose defaultService defaultListener connections watchConnections
  use strict arg conn

-- signal on syntax
-- say pp2(self)"::newConnection - just received a new connection conn="pp2(conn) "..." "defaultService~class="pp2(defaultService~class)

  connections~append(conn)             -- add new connection to our list of accepted connections
  if defaultService<>.nil then         -- add service object
  do
     if defaultService~isA(.DBusServiceObject) then
     do
        aCopy=defaultService~copy         -- each DBusServiceObject keeps its own connection to its client
        aCopy~service.connection=conn     -- assign this connection
        aCopy~service.objectpath=.nil     -- make sure no object path
        conn~serviceObject("add", "default", aCopy)   -- will start message loop automatically
     end
     else   -- plain object
     do
        conn~serviceObject("add", "default", defaultService)   -- will start message loop automatically
     end
  end

  if defaultListener<>.nil then        -- add listener object
  do
     conn~listener("add", defaultListener)                  -- will start message loop automatically
  end

  if watchConnections=.false then      -- start the watchConnections thread
     self~watchConnections

-- say pp2(self)"::newConnection - conn~messageLoop('active'):" pp2(conn~messageLoop('active'))
-- say pp2(self)"::newConnection -" pp2(conn) "about to leave."
  return

/*
syntax:
  co=condition('o')
  say pp2(self) ppCondition2(co)
  raise propagate
*/


::method disconnect                    -- invoked from native code (not reliably invoked all the time as of 1.6.4 on Ubuntu)
  expose connections
  use strict arg conn

-- say "..." self"::disconnect, 'conn'="pp2(conn)
  conn~messageLoop("stop")             -- stop the connection's message loop
  res=connections~removeItem(conn)     -- remove the disconnected connection from the client
  conn~close                           -- make sure we cannot use it anymore (it wouldn't work)


::method nativeStartServerWatchLoop   private external "LIBRARY dbusoorexx DbusServerWatchLoop"
::method nativeServerStartup      private external "LIBRARY dbusoorexx DbusNativeServerStartup"
::method startup                  unguarded  -- startup server
  expose allowAnonymous cself address

  if cself<>.nil then
     raise syntax 93.900 array (self": server was already started")

  -- return self~nativeServerID
-- say "address:" pp2(address) "cself="pp2(cself)
  self~nativeServerStartup(address) -- create server listening on given address

  reply                             -- create a new thread
  self~nativeStartServerWatchLoop   -- start the server message loop on the new thread



-- as of DBus 1.4.14, the private server does not receive a "Disconnected" signal
::method watchConnections  unguarded   -- check whether client connections are available, if not disconnect the stalled connecition
  expose connections watchConnections
  use strict arg sleepTime=.1 -- check every 1/10 second

  guard on
  watchConnections=.true      -- indicate that the watch Connections thread is started
  guard off
  reply

-- say "... ... probing" pp2(conn) "sleepTime:" pp2(sleepTime) "..."

  signal on halt
  do forever while connections~items>0 -- as long as client connections, probe them
     call sysSleep sleepTime  -- sleep
     guard on
     arr=connections~copy     -- get a copy of all connections
     guard off
     do conn over arr         -- iterate over all client connections and probe them
-- say "... ... testing" pp2(conn)": conn~isOpen:" pp2(conn~isOpen)  "..."
        if conn~query("open")=.false then
        do
-- say "... ... BINGO! " pp2(conn) "not connected/open anymore, DISCONNECTing connection !"
           self~disconnect(conn)
        end
     end
  end
  guard on
  watchConnections=.false
  guard off
  return

halt:       -- a HALT condition, make sure all known connections get closed
  guard off -- make sure guard is off
say "... DEBUG..." self": HALT in watchConnections, closing all known connections (and thereby their messageloops)..."
  do conn over connections
     conn~close
  end
  return




::method nativeStartServerTimeoutLoop   private external "LIBRARY dbusoorexx DbusServerTimeoutLoop"
::method timerLoop         unguarded         -- "START" or "STOP"
  expose timeoutLoopActive
  parse upper arg action +3

  if action="STO" then
  do
     guard on
     timeoutLoopActive=.false          -- this will stop the timeout loop in native code
     guard off
     return
  end

  if action<>"STA" then return         -- do nothing, if wrong argument

  if timeoutLoopActive=.true then return  -- already running

   -- now fetch RexxPointer value
  use strict arg action, timeoutDataPointer
-- say "... timeoutDataPointer:" pp2(timeoutDataPointer) copies("<-/\",10)
  reply                                -- create a new thread

  self~nativeStartServerTimeoutLoop(timeoutDataPointer)  -- start the timeout loop, pass (*data) via Rexx to native function




::method nativeServerShutdown       private external "LIBRARY dbusoorexx DbusNativeServerDisconnect"
::method shutdown                   unguarded   -- shutdown server
  expose connections cself watchLoopActive

-- say "..." .dateTime~new " arrived in shutdown, cself="pp2(cself) "..."
-- say "."~copies(50)
  if cself=.nil then
     raise syntax 93.900 array (self": shutdown not possible, as server was not started")

  guard on
  watchLoopActive=.false        -- indicate to serverLoop to stop
  guard off

  self~nativeServerShutdown      -- stop accepting connections

  do conn over connections
     conn~close                  -- close connection
-- say "shutdown: removing conn="pp2(conn)"..."
     connections~removeItem(conn)   -- remove connection
  end



::method nativeServerID             private external "LIBRARY dbusoorexx DbusNativeServerGetId"
::method serverID
  expose cself
  if cself=.nil then return .nil
  return self~nativeServerID


::method nativeActive               private external "LIBRARY dbusoorexx DbusNativeServerIsActive"
::method active
  expose cself
  if cself=.nil then return .false
  return self~nativeActive

   -- for debugging:
::method nativeServerAddress        private external "LIBRARY dbusoorexx DbusNativeServerGetAddress"
::method serverAddress
  expose cself
  if cself=.nil then return .nil
  return self~nativeServerAddress

/* --- end of DBusServer code */




/* =============================== Introspection related classes, routines ========================= */

/* cf. <http://standards.freedesktop.org/dbus/1.0/introspect.dtd> */

/* ------------------------------ class definition ------------------------------ */
::class "IDBus"               public   -- define base class, attributes name and content
::attribute name           -- name: name of the element, CDATA
::attribute parent         -- parent: node to which this one belongs to, if any
::attribute content        -- content: whatever elements an element contains in document order

::method compareTo         -- allow for sorting by name
  expose name
  use arg otherValue
  return (self~class~id name)~caselessCompareTo(otherValue~class~id otherValue~name)


::method init              -- constructor
  expose name content node
  use strict arg name=""
  parent=.nil
  content=.array~new

::method newIntrospection class     -- parses file/data, creates a parse tree and returns its root node
  use strict arg data               -- "data" can be a filename or the introspection data

  signal on syntax
  .ArgUtil~validateClass("data", str, .string) -- check for correct type

  -- chars can be an introspection filename or the file's bytes
  if chars~length<256, sysFileExists(data) then
     allChars=charin(data,1,chars(data))
  else
     allChars=data

  rootNode     =.IDBusNode~new("root")    -- create a rootNode
  currNode     =rootNode
  currInterface=.nil
  currMeth     =.nil
  currArg      =.nil
  lastEl       =rootNode      -- in case we see annotations, append them to last processed node

-- say "*** ----> ---> --> ->" self": newIntrospection # 1 ..."

  do while allChars<>""    -- process text
     parse var allChars '<'elName attribs'>' allChars

     select
         when elName="node" then    -- a new node
            do
               parse var attribs 'name="'name'"'                   -- use quotes
               if name="" then parse var attribs "name='"name"'"   -- use apostrophes
               currNode=.IDBusNode~new(name)
               rootNode~content~append(currNode)        -- append this node
               currNode~parent=rootNode
               currInterface=.nil
               if currMeth<>.nil then currMeth~createSignatures
               currMeth=.nil
               currArg=.nil
               lastEl=currNode
-- say "*** ----> ---> --> ->" self": node" pp2(name)
            end

         when elName="interface" then
            do
               parse var attribs 'name="'name'"'                   -- use quotes
               if name="" then parse var attribs "name='"name"'"   -- use apostrophes
               currInterface=.IDBusInterface~new(name)
               currNode~content~append(currInterface)   -- append this node
               currInterface~parent=currNode
               if currMeth<>.nil then currMeth~createSignatures
               currMeth=.nil
               currArg=.nil
               lastEl=currInterface
-- say "*** ----> ---> --> ->" self": interface" pp2(name)
            end

         when elName="method" then
            do
               parse var attribs 'name="'name'"'                   -- use quotes
               if name="" then parse var attribs "name='"name"'"   -- use apostrophes
               if currMeth<>.nil then currMeth~createSignatures
               currMeth=.IDBusCallMethod~new(name)
               currInterface~content~append(currMeth)
               currMeth~parent=currInterface
               currArg=.nil
               lastEl=currMeth
-- say "*** ----> ---> --> ->" self": method" pp2(name)
            end

         when elName="signal" then
            do
               parse var attribs 'name="'name'"'                   -- use quotes
               if name="" then parse var attribs "name='"name"'"   -- use apostrophes
               if currMeth<>.nil then currMeth~createSignatures
               currMeth=.IDBusSignalMethod~new(name)
               currInterface~content~append(currMeth)
               currMeth~parent=currInterface
               currArg=.nil
               lastEl=currMeth
-- say "*** ----> ---> --> ->" self": signal" pp2(name)
            end

         when elName="arg" then
            do
               parse var attribs 'name="'name'"'                   -- use quotes
               if name="" then parse var attribs "name='"name"'"   -- use apostrophes

               parse var attribs 'type="'type'"'                   -- use quotes
               if type="" then parse var attribs "type='"type"'"   -- use apostrophes

               parse var attribs 'direction="'direction'"'         -- use quotes
               if direction="" then parse var attribs "direction='"direction"'"   -- use apostrophes

                  -- default to 'in', cf. introspect.dtd; for signals: logically the arguments are 'in' as they are arguments for an event/signal handler!
               if direction="" | currMeth~isA(.IDBusSignalMethod) then
                  direction="in"

               currArg=.IDBusArg~new(name, type, direction)
               currMeth~content~append(currArg)
               currArg~parent=currMeth
               lastEl=currArg
-- say "*** ----> ---> --> ->" self": arg" pp2(name)
            end

         when elName="property" then
            do
               parse var attribs 'name="'name'"'                   -- use quotes
               if name="" then parse var attribs "name='"name"'"   -- use apostrophes

               parse var attribs 'type="'type'"'                   -- use quotes
               if type="" then parse var attribs "type='"type"'"   -- use apostrophes

               parse var attribs 'access="'access'"'                   -- use quotes
               if access="" then parse var attribs "access='"access"'"   -- use apostrophes


               currProp=.IDBusPropertyMethod~new(name,type,access)
               currInterface~content~append(currProp)
               currProp~parent=currInterface
               lastEl=currProp

               -- add argument notation to property's content to ease rendering in
               if pos("read", access)>0 then
                  currProp~content~append(.IDBusArg~new('replyValue', type, 'out'))

               if pos("write", access)>0 then
                  currProp~content~append(.IDBusArg~new('newValue', type, 'in'))

-- say "*** ----> ---> --> ->" self": property" pp2(name)
            end

         when elName="annotation" then
            do
               parse var attribs 'name="'name'"'                   -- use quotes
               if name="" then parse var attribs "name='"name"'"   -- use apostrophes

               parse var attribs 'value="'value'"'                   -- use quotes
               if value="" then parse var attribs "value='"value"'"   -- use apostrophes

               currAnno=.IDBusAnnotation~new(name, value)
               lastEl~content~append(currAnno)           -- append annotation to latest processed element
               currAnno~parent=lastEl
            end

         otherwise NOP        -- ignore all other markup (NOP=null operation, ie. "do nothing")
     end
  end

  if currMeth<>.nil then currMeth~createSignatures -- pending method?

-- say "*** ----> ---> --> ->" self": dumping tree using rootNode="pp2(rootNode)
-- call idbus.dumpIDBus rootNode

  return rootNode

syntax:

   say "*** ----> ---> --> ->" self": newIntrospection - SYNTAX-exception occurred ! ..."

  raise propagate       -- raise condition in caller

::method contains             -- returns .true if the supplied type exists in the tree
  parse upper arg kind +1 1 kind2 +2

  if pos(kind,"AIMNSP")=0 then
     raise syntax 88.916 array ('"type"', '"I[nterface]", "M[ethod]", "N[ode]", "S[ignal], "P[roperty]", "AR[gumgent]", "AN[notation]"', arg(1))

  select
     when kind="A" then
                   do
                       if kind2="AR" then type=.IDBusArg
                                     else type=.IDBusAnnotation
                   end
     when kind="I" then type=.IDBusInterface
     when kind="M" then type=.IDBusCallMethod
     when kind="N" then type=.IDBusNode
     when kind="S" then type=.IDBusSignalMethod
     when kind="P" then type=.IDBusPropertyMethod
     otherwise kind=.nil
  end
  if self~isA(type) then            -- object already of the desired type?
     return .true

  return workContains(self,type)

workContains: procedure                    -- return .true if type was found, .false else
  use arg o,type

  do el over o~content              -- iterate over subnodes
     if el~isA(type) then           -- element in hand of the desired type?
        return .true
     if el~content~items>0 then     -- recurse in content of element in hand
     do
        if workContains(el,type)then return .true
        -- if res=.true then return .true
     end
  end
  return .false

::method count             -- returns # of occurrences of given type
  parse upper arg kind +1 1 kind2 +2

  if pos(kind,"AIMNSP")=0 then
     raise syntax 88.916 array ('"type"', '"I[nterface]", "M[ethod]", "N[ode]", "S[ignal], "P[roperty]", "AR[gumgent]", "AN[notation]"', arg(1))

  select
     when kind="A" then
                   do
                       if kind2="AR" then type=.IDBusArg
                                     else type=.IDBusAnnotation
                   end
     when kind="I" then type=.IDBusInterface
     when kind="M" then type=.IDBusCallMethod
     when kind="N" then type=.IDBusNode
     when kind="S" then type=.IDBusSignalMethod
     when kind="P" then type=.IDBusPropertyMethod
     otherwise kind=.nil
  end
  count=(self~isA(type))

  return workCountType(self,type,count)

workCount: procedure                -- return .true if type was found, .false else
  use arg o,type,count

  do el over o~content              -- iterate over subnodes
     if el~isA(type) then           -- element in hand of the desired type?
        count+=1
     if el~content~items>0 then     -- recurse in content of element in hand
     do
        count=check(el,type,count)
        -- if res=.true then return .true
     end
  end
  return count



/* ------------------------------ class definition ------------------------------ */
::class "IDBusNode"           public subclass IDBus

/* ------------------------------ class definition ------------------------------ */
::class "IDBusInterface"      public subclass IDBus

/* ------------------------------ class definition ------------------------------ */
::class "IDBusMethod"         public subclass IDBus
::attribute argSignature
::attribute replySignature

::method init              -- constructor
  expose argSignature replySignature
  use strict arg name
  argSignature=""           -- default value
  replySignature=""
  forward class (super) -- array (name) -- continue   -- invoke superclass constructor

::method createSignatures  -- create signatures from IDbusArg definitions
  expose argSignature replySignature

  do c over self~content
     if c~isA(.IDBusArg) then
     do
         -- introspect.dtd suggests that signals should not have a 'direction' attribute, hence defaulting to 'out'
        if c~direction="in" then argSignature =argSignature  || c~type
                            else replySignature=replySignature || c~type
     end
  end

/* ------------------------------ class definition ------------------------------ */
::class "IDBusCallMethod"     public subclass IDBusMethod

/* ------------------------------ class definition ------------------------------ */
::class "IDBusSignalMethod"   public subclass IDBusMethod

/* ------------------------------ class definition ------------------------------ */
::class "IDBusPropertyMethod" public subclass IDBusMethod   -- properties are actually methods to get/set values
::attribute type           -- type: type code
::attribute access         -- access: read, readwrite, write

::method init              -- constructor
  expose type access
  use strict arg name, type, access

  forward class (super) array (name) continue   -- invoke superclass constructor

  if pos("read",  access)>0 then self~replySignature=type
  if pos("write", access)>0 then self~argSignature =type



/* ------------------------------ class definition ------------------------------ */
::class "IDBusArg"            public subclass IDBus
::attribute type           -- type: type code
::attribute direction

::method init              -- constructor
  expose type direction
  use strict arg name, type, direction

  forward class (super) array (name) -- continue   -- invoke superclass constructor


/* ------------------------------ class definition ------------------------------ */
::class "IDBusAnnotation"     public subclass IDBus
::attribute value          -- value: CDATA

::method init              -- constructor
  expose value
  use strict arg name, value
  forward class (super) array (name) -- continue   -- invoke superclass constructor


/* ========================== introspection related routines =========================== */

/* ------------------------------------------------------------------------------------- */
::routine IDBus.getMethodsForProxy  public  -- return a directory pointing to those methods we may send to a proxy object
  use arg rootNode

  methDir=.directory~new

  do n over rootNode~content
     do i over n~content   -- interfaces etc.
        if i~isA(.IDBusInterface) then
        do
           do m over i~content
              kind=""
              if      m~isA(.IDBusCallMethod)     then kind="method"
              else if m~isA(.IDBusSignalMethod)   then kind="signal"
              else if m~isA(.IDBusPropertyMethod) then kind="property"

              if kind<>"" then
              do
                 name=m~name
                 methDir~setEntry(name, m)                  -- save methName -> methObj
                 methDir~setEntry(m~parent~name"."name, m)  -- save fully qualified methName -> methObj
              end
           end
        end
     end
  end
  return methDir                    -- return methDir


/* ------------------------------------------------------------------------------------- */
::routine IDBus.analyzeIDBUSMethods    public   -- dump the tree, showing methods with their signatures
  use arg rootNode, bShowProgress=.false

  seenMethodNames=.directory~new

  if bShowProgress=.true then say "node:" pp2(rootNode~name)
  start=3
  step=3
  do n over rootNode~content
     if n~isA(.IDBusNode) then
     do
        indent=start
        strIndent=" "~copies(indent)
        if bShowProgress=.true then say strIndent "node:" pp2(n~name) -- "content~items:" pp2(n~content~items)
     end

     do i over n~content   -- interfaces etc.
        if i~isA(.IDBusInterface) then
        do
           indent=start+step
           strIndent=" "~copies(indent)
           if bShowProgress=.true then say strIndent "interface:" pp2(i~name)

           indent+=step
           strIndent=" "~copies(indent)

           do m over i~content
              kind=""
              if      m~isA(.IDBusCallMethod)     then kind="method"
              -- else if m~isA(.IDBusSignalMethod)   then kind="signal"    -- just for debugging: signals are not interesting for us, as we receive them
              else if m~isA(.IDBusPropertyMethod) then kind="property"
              if kind<>"" then
              do
                 if seenMethodNames~hasEntry(m~name) then
                 do
                    seen=seenMethodNames~entry(m~name)
                    say "   ***" pp2(m~parent~name) kind pp2(m~name) "already defined as" pp2(seen~name) "a" pp2(seen~class~id) pp2(seen~parent~name)
                 end
                 else
                    seenMethodNames~setEntry(m~name,m)   -- save seen method
              end
           end
        end
        if bShowProgress=.true then say
     end
     if bShowProgress=.true then say
  end


/* ------------------------------------------------------------------------------------- */
::routine IDBus.dumpIDBus  public -- dump the tree, showing methods with their signatures
  use arg rootNode

  dt=.dbus.dir~dataTypes         -- get datatype dir, "r" -> struct, "e" -> dict(entry)

  say "node:" pp2(rootNode~name)
  start=3
  step=3
  do n over rootNode~content~sort
     if n~isA(.IDBusNode) then
     do
        indent=start
        strIndent=" "~copies(indent)
        say strIndent "node:" pp2(n~name) -- "content~items:" pp2(n~content~items)
     end

     do i over n~content~sort   -- interfaces etc.
        if i~isA(.IDBusInterface) then
        do
           indent=start+step
           strIndent=" "~copies(indent)
           say strIndent "interface:" pp2(i~name)

           indent+=step
           strIndent=" "~copies(indent)

           minWidth=8   -- minimum replyValue width
           do m over i~content~sort   -- iterate over interface content
              bGetterSetter=.false
              if      m~isA(.IDBusCallMethod)     then kind="method"
              else if m~isA(.IDBusSignalMethod)   then kind="signal"
              else if m~isA(.IDBusPropertyMethod) then
              do
                  kind="property"
                  bGetterSetter=(m~access="readwrite")  -- a getter and a setter ?
              end
              else kind=.nil

              tmpStr=""
              if kind<>.nil then
              do a over m~content   -- iterate over
                 if a~isA(.IDbusArg) & a~direction="in" then
                 do
                    if tmpStr<>"" then tmpStr=tmpStr", "

                    t=a~type
                    if dt~hasIndex(t) then tmpStr=tmpStr || dt[t]
                                      else tmpStr=tmpStr || t

                    if a~name<>"" then
                       tmpStr=tmpStr a~name
                 end
              end

              if bGetterSetter<>.true then     -- could only be true for a property
              do
                 so=pp2(m~replySignature)
                 if so~length<minWidth then so=so~left(minWidth, ".")

                 say strIndent so kind~left(8) m~name "("tmpStr~strip")" "->" pp2(m~argSignature)
              end
              else   -- special handling for get&set properties: show both signatures separately
              do
                 so=pp2("")   -- first show the setter, which has no return value
                 if so~length<minWidth then so=so~left(minWidth, ".")
                 say strIndent so kind~left(8) m~name "("tmpStr~strip")" "->" pp2(m~argSignature)

                 so=pp2(m~replySignature) -- show the getter method
                 if so~length<minWidth then so=so~left(minWidth, ".")
                 say strIndent so kind~left(8) m~name "()" "->" pp2("")
              end

           end
        end
        say
     end
     say
  end
  return




/* =============================== Introspection related classes, routines ========================= */
/* There might be cases where Introspect data does not get supplied by the service object,
   although there are well defined interfaces to it. In case the Rexx programmer knows of
   them this class allows to define these and get a rendering that is suitable to submit
   to the .IDBus class ("rootNode=.IDBus~newIntrospection(introspectData") and assign the
   resulting rootNode to the DBusProxyObject ("proxObject~proxy.introspectRootNode(rootNode)").

   This is a simple implementation ignoring the names of arguments and dealing with
   DBus signatures only.
*/
/* cf. <http://standards.freedesktop.org/dbus/1.0/introspect.dtd> */

::class IntrospectHelper   PUBLIC         -- class to allow for creating interface definitions

::method init
  expose nodeName list
  use strict arg nodeName=""
  list=.array~new

::attribute list get
::attribute nodeName get
::method name           -- alias for nodeName to ease compareTo code
  expose nodeName
  return nodeName

::method compareTo      -- allow sorting
  expose nodeName
  use arg otherValue
  return nodeName~caselessCompareTo(otherValue~name)


::method addInterface
  expose list
  use strict arg interfaceName=""
  o=.IntrospectHelperInterface~new(interfaceName)
  list~append(o)
  return o

::method addNode
  expose list
  use strict arg nodeName=""
  o=.IntrospectHelper~new(nodeName)
  list~append(o)
  return o


::method makeString
  expose nodeName list
  lf  ='0a'x            -- LF
  lf2 ='0a0a'x          -- LF+LF
  tab=" "~copies(4)
  m=.MutableBuffer~new
   -- header text
  m~~append('<!DOCTYPE node PUBLIC ') ~~append(lf)
  m~~append(' "-//freedesktop//DTD D-BUS Object Introspection 1.0//EN" ') ~~append(lf)
  dtdUrl="http://standards.freedesktop.org/dbus/1.0/introspect.dtd"
  m~~append('"')~~append(dtdUrl)~~append('">') ~~append(lf)

  self~workMakeString(m,self,0, lf,lf2,tab)
  return m~string

::method workMakeString    -- worker method which can be recursively executed if multiple nodes
  use arg m, o, indent=0, lf, lf2, tab

  tab1=tab~copies(indent)
  tab2=tab1||tab
  tab3=tab2||tab

  isNode=o~isA(.IntrospectHelper)   -- a node in hand
  if isNode then
  do
     m~~append(tab1)~~append('<node name="')~~append(o~nodeName)
     if indent>0,o~list~items=0 then      -- an embedded empty node?
     do
        m~~append('"/>')~~append(lf)
        return
     end

     m~~append('">')~~append(lf)
  end

  do item over o~list~sort
     if item~isA(.IntrospectHelper) then
        item~workMakeString(m,item,indent+1, lf, lf2, tab)
     else   -- an interface object
        item~workMakeStringInterface(m,item,indent+1, lf, lf2, tab)
  end

  if isNode then
     m~~append(tab1)~~append('</node>')~~append(lf2)
  return



::class introspectHelperInterface      -- helper class

::method init
  expose interfaceName list
  use arg interfaceName
  list=.array~new


::method compareTo      -- allow sorting
  expose interfaceName
  use arg otherValue
  return interfaceName~caselessCompareTo(otherValue~name)


::attribute list get
::attribute interfaceName get
::method name           -- alias for interfaceName to ease compareTo code
  expose interfaceName
  return interfaceName

::method addMethod      -- name, signature of args, signature of return value
  expose list
  use strict arg name, in="", out=""
  signal on syntax
  str=DBusDataType(in, "S")
  if str<>.true then raise syntax 93.900 array("'in'-signature:" str)

  str=DBusDataType(out, "S")
  if str<>.true then raise syntax 93.900 array("'out'-signature:" str)
  list~append("method" name 'in=['in'] out=['out']')
  return
syntax: raise propagate    -- raise error in caller

::method addProperty    -- name, signature of type, access
  expose list
  use strict arg name, type, access
  signal on syntax
  str=DBusDataType(type,"S")
  if str<>.true then raise syntax 93.900 array("'type'-signature:" str)

  if pos(access, "read write readwrite")=0 then raise syntax 88.916 array('"access"', '"read", "write" or "readwrite"', access)
  list~append("property" name 'type=['type'] access=['access']')
  return
syntax: raise propagate    -- raise error in caller

::method addSignal      -- name, signature of arguments as one string
  expose list
  use strict arg name, arg=""
  signal on syntax
  str=DBusDataType(arg, "S")
  if str<>.true then raise syntax 93.900 array("'arg'-signature:" str)

  list~append("signal" name 'arg=['arg']')
  return
syntax: raise propagate    -- raise error in caller

::method makestring     -- create a string, sort ascendingly
  expose list interfaceName
  m=.MutableBuffer~new
  tab=" "~copies(4)
  lf ="0a"x
  lf2="0a0a"x
  self~workMakeStringInterface(m,self,0,lf,lf2,tab)
  return m~string

::method workMakeStringInterface    -- worker method which can be recursively executed if multiple nodes
  use arg m, o, indent=0, lf, lf2, tab

  tab1=tab~copies(indent)
  tab2=tab1||tab
  tab3=tab2||tab
  interfaceName=o~interfaceName     -- get interface name
  list=o~list                       -- get list of definitions

  m~~append(tab1)~~append('<interface name="'interfaceName'">')~~append(lf)

  do def over list~sort
     parse var def type name . 1 . '=['val1']' '=['val2']'
     m~~append(tab2)
     if type='method' then          -- name, val1=in, val2=out
     do
        if val1="",val2="" then  -- has no arguments
        do
           m~~append('<method name="')~~append(name)~~append('"/>')~~append(lf)
        end
        else   -- has arguments
        do
           m~~append('<method name="')~~append(name)~~append('">')~~append(lf)
           if val1<>"" then
           do
              call makeArgs m, val1, 'in'    -- create argument entries
              -- m~~append(tab3)~~append('<arg name="argname" type="')~~append(val1)~~append('" direction="in"/>')~~append(lf)
           end

           if val2<>"" then
           do
              call makeArgs m, val2, 'out'    -- create argument entries
              -- m~~append(tab3)~~append('<arg name="argname" type="')~~append(val2)~~append('" direction="out"/>')~~append(lf)
           end

           m~~append(tab2)~~append('</method>')~~append(lf)
        end

     end

     else if type='property' then   -- name, val1=type, val2=access
     do
        m~~append('<property name="')~~append(name)~~append('" type="')~~append(val1)
        m~~append('" access="')~~append(val2)~~append('"/>')~~append(lf)
     end

     else if type='signal' then     -- name, val1=in
     do
        m~~append('<signal name="')~~append(name)~~append('">')~~append(lf)
        if val1<>"" then
        do
           -- m~~append(tab3)~~append('<arg name="argname" type="')~~append(val1)~~append('"/>')~~append(lf)
           call makeArgs m, val1, 'in' -- create argument entries
        end

        m~~append(tab2)~~append('</signal>')~~append(lf)
     end
  end
  m~~append(tab1)~~append('</interface>')~~append(lf)
  return

  -- return m~string       -- return the string rendering


makeArgs: procedure expose lf lf2 tab1 tab2 tab3 -- we can be sure that signatures are valid
  use arg m, signature="", direction="in"
  argName ="arg_"direction
  do argCount=1 while signature<>""
     char=left(signature,1)   -- get first char
     tmpStr=char
     if char="(" then         -- a structure
        parse var signature 1 localSignature +(parseParen(signature,1,char)) signature

     else if char="a" then    -- an array !
     do
        pos=verify(signature,'a')   -- returns position of non-'a' char
        nextChar=signature~subChar(pos)
        if pos(nextChar,'({')>0 then   -- a struct or a dict/map
           parse var signature 1 localSignature +(parseParen(signature,pos,nextChar)) signature
        else   -- a simple complete type
           parse var signature 1 localSignature +(pos) signature
     end

     else   -- single complete type
        parse var signature localSignature +1 signature

     m~~append(tab3)~~append('<arg name="')~~append(argName)~~append(argCount)~~append('" type="')
     m~~append(localSignature)~~append('" direction="')~~append(direction)~~append('"/>')~~append(lf)
  end
  return

parseParen: procedure
  use arg signature, startPos=1, char='('

  if      char='(' then endChar=')'
  else if char='{' then endChar='}'

  count=1
  len=signature~length
  do i=startPos+1 to len until count=0
     subChar=signature~subChar(i)
     if      subChar=char    then count+=1
     else if subChar=endChar then count-=1
  end

  if i>len then         -- not found!
     return -1
  return i              -- position of ")" char


/* =============================================================================== */
/* Define a class that leads an Introspect finder to the available object path(s).
   An instance of this class may be registered with the connection's serviceObject-method
   using the special name "default" as a pseudo object path.
*/
::class IDBusPathMaker public

::method publishAllServiceObjects class      -- expects a DBus (connection) object, publishes all defined object paths
  use strict arg conn

  .ArgUtil~validateClass("conn", conn, .DBus) -- check for correct type
  a=.array~new
  s=conn~serviceObject("GetAll")~supplier
  do while s~available
     if DBusDataType(s~index,"ObjectPath")=.true, s~item~isA(.DBusServiceObject) then
        a~append(s~index)
     s~next
  end
  items=a~items
  conn~serviceObject("Remove", "default") -- remove entry, if available
  if items>0 then          -- add paths, if any
     conn~serviceObject("Add", "default", .IDBusPathMaker~new(a))

  return items             -- return number of object paths now available

syntax: raise propagate


::method init
  expose paths
  use arg objectPath          -- can also be a collection of paths

-- say "..." self"::init, objectPath="pp2(objectPath)

  signal on syntax
  paths=.relation~new

  if \objectPath~isA(.collection) then    -- turn single argument into a collection
     objectPath=.array~of(objectPath)

  do currPath over objectPath
     res=DBusDatatype(currPath,"ObjectPathName")   -- check whether a valic object path in hand
     if res<>.true then
        raise syntax 88.917 array ('"objectPath" value "'currObjPath'" is not a valid DBus object path')

     call parseObjPath currPath
  end
  return

syntax: raise propagate       -- raise in caller

parseObjPath: procedure expose paths   -- create paths and save in relation
  parse arg objPath

  tmpPath=substr(objPath,2)      -- remove leading '/'
  tmpNewPath="/"
  do while tmpPath<>""
     parse var tmpPath curr "/" tmpPath

     if paths~hasItem(curr,tmpNewPath)=.false then -- only add, if entry not available yet
        paths[tmpNewPath]=curr         -- point to next node
        -- next object path along the axis

     if tmpNewPath='/' then tmpNewPath=tmpNewPath || curr
                       else tmpNewPath=tmpNewPath"/"curr
  end

-- call dump2 paths, self": 'paths'-relation after set-up for" pp2(objPath)

  return


::method Introspect
  expose paths
  use arg slotDir

  node=.IntrospectHelper~new        -- create root node (could supply a name)

  objPath=slotDir~objectPath
  if paths~hasIndex(objPath) then   -- add hints as nodes
  do
     do path over paths~allAt(objPath)~sort
        node~addNode(path)
     end
  end
  return node~makeString   -- return introspect data

::attribute paths get      -- allow access to the relation with object path fragments pointing to next local fragment



