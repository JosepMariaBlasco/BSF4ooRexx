#!/usr/bin/rexx
/** This program makes the Linux DBus infrastructure available to ooRexx in an easy to use manner. It is closely intertwined
*   with the accompanying native ooRexx library. Therefore do not change any code unless you <em>know</em> what you are doing! :)
*
*   These are the core classes that are defined in this package:

<ul>

<li>DBus
<br> Represents a DBus connection and among other things allows for registering ooRexx DBus signal listeners and
ooRexx service objects.
</li>

<li>DBusProxyObject
<br>Represents a remote DBus service object with all its defined properties, signals and messages.
</li>

<li>DBusServiceObject
<br>Makes it easy to use a Rexx object as a DBus service object.
</li>

<li>DBusServer
<br>Represents a private DBus server.
</li>

<li>IDBus
<br>This class ("introspect DBus") allows to introspect a DBus service object's introspection XML data.
</li>

<li>IntrospectHelper
<br>Allows to create introspect data programmatically, its <code>makeString</code> method will turn the definitions into a valid introspection XML-file
</li>

<li>IDBusPathMaker
<br>Allows to create the infrastructure to serve DBus object paths for service objects.
</li>

</ul>
*
* The <code>.local</code> environment contains an entry named <code>DBUS.DIR</code> which contains DBus related entries. Two entries control
whether this package outputs debug information or not, depending on the logical values of its entries named:
<ul>
<li><code>bDebug</code>
</li>

<ul>
<li><code>bDebugServer</code>
</li>

*
* @author Rony G. Flatscher
* @since 2011-06-11
* @version 200.20150421
*
*/


/*
      author:     Rony G. Flatscher (C) 2011-2015
      date:       2015-04-21 (2011-06-11, 2011-07-19, 2011-08-12)
      name:       dbus.cls
      purpose:    make the DBUS language binding for ooRexx available
      needs:      - dbus-message bus system installed (by default available on Linuxes)
                  - ooRexx ("open object Rexx") 4.2.0 or higher, cf. <http://www.ooRexx.org>
                  - ooRexx language bindings for ooRexx, cf. <https://sourceforge.net/projects/bsf4oorexx/files/GA/sandbox/dbusoorexx/>
                  - BSF4ooRexx, if using the utf8-conversion routines, download from
                    <https://sourceforge.net/projects/bsf4oorexx/>

                   ---
                     DBus for MacOSX: e.g. install via <https://www.macports.org/>
                     DBus for Windows: e.g. <http://wi.wu.ac.at/rgf/rexx/orx22/work/>
                   ---
                     DBus home/source: <http://www.freedesktop.org/wiki/Software/dbus/#index5h1>




      credits:    Mike F. Cowlishaw: for making his "utf8"-Rexx procedure available (cf. routine "stringToutf8")

      cf.:        dbus-specifications: <http://dbus.freedesktop.org/doc/dbus-specification.html> (as of 2011-07-14)

      version:    200.20150421

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
                  - 2011-08-29, rgf: change sequence of init-args for DBusServiceObject to reflect importance by usage pattern
                  - 2011-09-12, rgf: - DBus.close(): setting "cself=.nil", so cannot be used anymore
                                     - DBus.message(...): raise error, if connection is closed
                                     - DBus.listener() & DBus.serviceObject(): if no listeners and service objects, stop message loop
                      - DBusServiceObject: replaced "service.connection" with "service.connections" (a set);
                                           removed "service.objectPath"; changed method "service.signal" to
                                           accept the arguments in the following order "objectPath,interface,member, ...args...",
                                           a signal is sent to all connections the service object is registered with
                  - 2014-07, rgf: - rework in order to cater for libdbus-limitation "interaction only in thread
                                    that created the connection": making sure that all interaction with
                                    DBus occurs in the thread that established the connection to DBus; reshuffled
                                    and changed strategies in DBUS.CLS and oorexxdbus.cc (both are closely interwined)
                                    - Added mixin class Worker and WorkerMessage
                                    - make sure that all connection related interactions with DBus occurs
                                      only in the thread that was used to connect to DBus; this implies that
                                      stopping a message loop causes that connection to be not usable from
                                      ooRexx anymore, hence method messageLoop() gets deprecated!
                                    - if an error condition is raised in the message loop thread, it will get
                                      re-raised on the blocked thread upon return
                                  - added public routine DBusVersion() which includes the library and this package version
                  - 2014-08-05, rgf: - adding .DBus to .local such that in native code FindClass("DBUS") will
                                       remain able to find this class in the case that the reported bug
                                       <http://sourceforge.net/p/oorexx/bugs/1275> gets fixed
                  - 2014-08-07, rgf: - tidied up the code
                                     - removed dependency on rgf_util2.rex
                  - 2014-08-11 through 20140916, rgf: - add oorexxdoc comments
                  - 2015-01-11, rgf: - updating ooRexxDoc text
                  - 2015-04-07, rgf: - update version date (date of D-Bus presentations at the International Rexx Symposium 2015 in Vienna, Austria)
                  - 2015-04-21, rgf: - corrected minimum required ooRexx version to 4.2.0, added DBus-related links


      license:    Apache License 2.0

------------------------ Apache Version 2.0 license -------------------------
   Copyright 2011-2015 Rony G. Flatscher

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


.local~dbus=.dbus -- save .DBus class in .local to allow native code to use FindClass("DBUS") successfully
                  -- as of ooRexx 4.2.0 (Sept 2014) it may be the case that the native code finds this
                  -- class by mistake; putting it into .local makes sure that the native code will always
                  -- find it "legally" :)

/* Defining dbus shared constants, leaving the dbus names untouched, such that
   dbus-samples employing these constants can be easily transcribed to Rexx.
   Cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusShared.html>
*/
.local~dbus.dir=.directory~new

.dbus.dir~bDebug      =.false -- .true -- .false -- .true
.dbus.dir~bDebugServer=.false -- .true -- .false -- .true

.dbus.dir~version="200.20150421"    -- current version of DBUS.CLS, released will start with: '100.yyyymmdd'

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

-- TODO: do we want BSF4ooRexx loaded automatically or on request ?
-- try to require BSF4ooRexx dynamically
call dynamicRequiresBSF4ooRexx   -- requires BSF.CLS


::requires "dbusoorexx" LIBRARY  -- get access to the native routines and methods

/** This routine requires dynamically the ooRexx package <code>BSF.CLS</code> and
*   in the case it is not available allows to continue gracefully.
*/
::routine dynamicRequiresBSF4ooRexx -- BSF4ooRexx: needed need for UTF-8 conversion routines
  signal on any
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  .context~package~loadPackage("BSF.CLS") -- if not found a syntax condition is raised and
                                          -- control transferred to (immediately following) "ANY:" label
any:
  return



/* ============================================================================================= */
/* Define the DBus class, which allows connecting, getting objects to talk to,
   adding own servers on the bus.
*/
/** This class represents a DBus connection. It allows connecting, getting service objects
*   to send messages to, adding own service objects and listener objects on the bus.
*/
::class "DBus" public inherit Worker

/* --- class methods ------ */

/** Class constructor method for initializing class attributes.
*/
::method init        class
  expose system session tid2human
  tid2human=.directory~new    -- attribute that allows to translate TIDs into human readable form
  system=.nil
  session=.nil


  -- will be usually called from native DBusGetTID(), which allows one optional argument indicating the desired formatting
/** Class method that is being used by native code to turn the inconceivable thread ids into human legible values.
*
* @param tid the thread id number
* @param option H[uman readable] (default) or S[system] (thread id number)
* @return thread id according to the given option
*/
::method formatTID   unguarded class   -- unguarded (access to tid2human only in here)
  expose tid2human
  use strict arg tid, option="H"

  parse upper var option opt +1  -- "H[uman]", "S[ystem]", "A[ll]"
  signal on syntax
  if pos(opt,"HSA")=0 then
     raise syntax 88.916 array ('"option"', '"H[uman]", "S[ystem]", "A[ll]"', option)

  if \tid2human~hasIndex(tid) then
      tid2human[tid]="thread" (tid2human~items+1)  -- generate a human readable form and save it

  select
     when opt="H" then return tid2human[tid]
     when opt="S" then return tid
     otherwise return tid2human[tid]"/"tid
  end
  return

syntax: raise propagate



   /* unique id for the computer                         */
/** Gets and returns the machine's unique id according to DBus.
*
* @return machine's unique id according to DBus
*/
::method machineID   class external "LIBRARY dbusoorexx DBusGetUniqueMachineId"


/** Establishes and returns a connection to the <code>system</code> on DBus, and caches that connection.
*
* @return connection to the <code>system</code> bus on DBus
*/
::method system      class    /* returns the shared system bus connection */
  expose system
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "arg(1):" pp(arg(1))

  if arg(1)="CLEAR", arg(2)=system then   -- remove cached connection to force a new one to be created
  do
     system=.nil
     return
  end

  if system=.nil then         /* if not available yet, get and rember it   */
     system=self~new("system")
  return system


/** Establishes and returns a connection to the <code>session</code> on DBus, and caches that connection.
*
* @return connection to the <code>session</code> bus on DBus
*/
::method session     class    /* returns the shared session bus connection */
  expose session
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "arg(1):" pp(arg(1))

  if arg(1)="CLEAR", arg(2)=session then  -- remove cached connection to force a new one to be created
  do
     session=.nil
     return
  end

  if session=.nil then        /* if not available yet, get and rember it   */
     session=self~new("session")
  return session


/** Creates a new connection and returns it, using the <code>NEW</code> class method.
*
* @see NEW class method
*/
::method connect     class    /* connect to an existing dbus connection    */
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  forward message ("NEW")     -- let our version of NEW handle this


/** Creates a new connection and returns it.
*
* @param address the address to connect to; special address names are &quot;system&quot; and &quot;session&quot;
* @return instance of this class representing the connection
*
* @see session class method
* @see system class method
*
*/
::method new         class
  expose system session
  use strict arg address

if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  bus=self~new:.class   -- return barely initialized

  signal on syntax
  lcAddress=address~strip~lower
  if wordpos(lcAddress, "session system")>0 then
  do
     if lcAddress="system" then
     do
        if system<>.nil then        /* shared message bus already cached, use it */
           return system

        bus~connectToAddress(lcaddress)  -- will start message loop
        system=bus
     end
     else
     do
        if session<>.nil then       /* shared message bus already cached, use it */
           return session

        bus~connectToAddress(lcaddress)   -- will start message loop
        session=bus
     end
  end
  else      -- address was given
  do
     bus~busType="private" -- rest will be initialized from native code

     -- "native" sent by native code which will initialize the connection after
     -- receiving the new dbus object
    if address<>"native" then
    do
        bus~connectToAddress(address)    -- use address verbatimly
    end

  end
  return bus

syntax: raise propagate    -- raise in caller

/** The address to which the connection is established.
*
*/
::attribute address           get

/** Setter method for the native library to allow setting a pseudo address for private connections (to private servers).
*/
::attribute pseudoAddress     set      -- 20140728, rgf: allows private server to set a pseudo address for each connected client
  expose address
  use strict arg address               -- just assign whatever we receive
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

/* --- instance methods --- */
/* much is implemented in native code, including setting attributes 'address',
  'uniqueBusName' and 'wellKnownBusName', if given
*/

/** Constructor method that initializes attributes. Note, the native code will set the values of
*   some of the attributes!
*/
::method init
  expose cself busType address makeSlotDir makeReplySlotDir active -
         internalRegisteredServiceObjects internalSignalListeners -
         unmarshalByteArrayAsString -
         collectStatistics statistics server sendEmptyReply -
         haltAllThreadsOnUnexpectedError

if .dbus.dir~bDebug=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line)

  cself=.nil                  /* will be set by a native method   */
  busType=.nil                /* will be set by native method     */
  address=.nil
  server=.nil                 /* if a client connection to a private Rexx server this will be set by the native method  */
  makeSlotDir=.true           /* by default create and pass slotDir as last argument on received signals or method calls */
  makeReplySlotDir=.false     /* by default do not create a slotDir for a regular message reply; if set to .true
                                 the return value, if it is not an array will be placed as the first value
                                 into a newly created array; the slotDir value will be appended to the array */
  active=.nil                 /* native code: sets to .true, if connection is being listened to, .false, if message loop is not available */
  stopLoop=.false             /* native code: message loop will stop, when set to .true   */

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

  forward class (super)    -- let superclass initialize (Worker class)

/** Allows read access, returns <code>.true</code> if the connection's message loop thread is running,
*   <code>.false</code> else.
*   @since 2014-07-24
*/
::attribute active get

/* Creates or gets a new bus instance. */
::method nativeConnectPrivate private external "LIBRARY dbusoorexx DBusNativeConnectionOpenPrivate"
::method nativeGetSystemBus   private external "LIBRARY dbusoorexx DBusNativeGetSystemBus"
::method nativeGetSessionBus  private external "LIBRARY dbusoorexx DBusNativeGetSessionBus"

/** Connects to the given address and starts the message loop for processing DBus messages over this connection.
* @param address to connect to
*/
::method connectToAddress -- unguarded
   expose active
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  forward message (connectToAddress"_worker") continue
  guard on when active=.true     -- block (wait) until message queue is running, otherwise we get lock-ups

/** This method connects to DBus in a separate (message loop) thread which then remains active for the connection's
*   duration. This way the problems with libdbus (not fully multithreading safe) should be fixed.
*/
::method connectToAddress_worker -- rgf, 20140722
  expose active address
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  if active=.true then return    -- message loop already up and running

  reply              -- start a new thread which will start the message loop and block
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "after reply"

  use arg address    -- get desired address
  if      address="session" then
  do
        self~nativeGetSessionBus(self)
  end
  else if address="system" then
  do
        self~nativeGetSystemBus(self)
  end
  else if address="native" then     -- this comes from native code in the context of a privat Server,
  do
      NOP
  end
  else
  do
-- say "   -> self~nativeConnectPrivate(address)" pp(address) "-->"
        self~nativeConnectPrivate(address) -- create a private connection to the address
-- say "   <- self~nativeConnectPrivate(address)"
  end

  self~startMessageLoop       -- will start message loop in its own thread



/** Controls whether a received message call without defined return value will return an empty message reply */
::attribute sendEmptyReply    get
::attribute sendEmptyReply    set       -- default: .true (append slotDir as last argument)
  expose sendEmptyReply
  use strict arg argSendEmptyReply=.true
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  signal on syntax
  if \datatype(argSendEmptyReply, "O") then
     raise syntax 88.900 array ('Argument "argSendEmptyReply" must be a logical value, found "'argSendEmptyReply'"')

  sendEmptyReply=argSendEmptyReply
  return
syntax:
  raise propagate

   /** Attribute controls whether the arguments from a signal or a remote method call get
      appended with a slotDir-directory supplying possibly useful information about the
      DBus message; default: <code>.true</code>
   */
::attribute makeSlotDir      get
::attribute makeSlotDir      set       -- default: .true (append slotDir as last argument)
  expose makeSlotDir
  use strict arg argMakeSlotDir=.true
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  signal on syntax
  if \datatype(argMakeSlotDir, "O") then
     raise syntax 88.900 array ('Argument "argMakeSlotDir" must be a logical value, found "'argMakeSlotDir'"')

  makeSlotDir=argMakeSlotDir
  return
syntax:
  raise propagate

   /** Attribute controls whether the arguments from a reply (result of calling a message on
      a remote DBus object) will get appended with a slotDir-directory supplying possibly
      useful information about the DBUS message; default: <code>.false</code>
   */
::attribute makeReplySlotDir get
::attribute makeReplySlotDir set       -- default: .false (do not append slotDir to result of a message call (turning result into an array object)
  expose makeReplySlotDir
  use strict arg argMakeReplySlotDir=.false  -- changed, 201407 rgf
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  signal on syntax
  if \datatype(argMakeReplySlotDir, "O") then
     raise syntax 88.900 array ('Argument "argMakeReplySlotDir" must be a logical value, found "'argMakeReplySlotDir'"')

  makeReplySlotDir=argMakeReplySlotDir
  return
syntax:
  raise propagate

   /** Attribute controls whether byte arrays (signature 'ay') should be unmarshalled as
      Rexx strings instead of a Rexx array with individual bytes
   */
::attribute unmarshalByteArrayAsString get
::attribute unmarshalByteArrayAsString set       -- default: .true (append slotDir to result of a message call (turning result into an array object)
  expose unmarshalByteArrayAsString
  use strict arg argUnmarshalByteArrayAsString=.true
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  signal on syntax
  if \datatype(argUnmarshalByteArrayAsString, "O") then
     raise syntax 88.900 array ('Argument "argUnmarshalByteArrayAsString" must be a logical value, found "'argUnmarshalByteArrayAsString'"')

  unmarshalByteArrayAsString=argUnmarshalByteArrayAsString
  return
syntax:
  raise propagate

/** Determines whether statistics about the messages and signals should be maintained; default: <code>.true</code>.
*/
::attribute collectStatistics       -- default: .true (keep slotDirs of last received messages/errors/signals and their respective counters)

/** Storage for collected statistics, a Rexx directory.
*/
::attribute statistics              -- a directory

/** If set to <code>.true</code>, then an unexpected Rexx condition will cause all Rexx threads to be halted.
*/
::attribute haltAllThreadsOnUnexpectedError  -- if set to .true, then an unexpected Rexx condition will cause all Rexx threads to be halted


/** Method blocks until connection got closed and message loop thread stopped, turning attribute
*   <code>active</code> to <code>.false</code>.
*
* @since 2014-08
*/
::method waitOnConnectionClosed unguarded
  expose active
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  guard on when active=.false


::method nativeStartMessageLoop   private external "LIBRARY dbusoorexx DBusMessageLoop"

/** Do not use ! Starts a connection's message loop, will be automatically invoked when a connection is established.
*   Never invoke this method yourself.
*/
::method startMessageLoop unguarded
  expose active stopLoop address busType
if .dbus.dir~bDebug=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "arrived"

  signal on syntax
  if active=.true then
  do
      info="(bustype='"||bustype"'"
      if var("ADDRESS") then info=info", address='"address"'"
      info=info")"
      raise syntax 98.900 array ("Message loop for this connection" info "already active (running)")
  end

  if stopLoop=.true then   -- message loop got already stopped, we do not allow to restart it, a new conncection needs to be established
  do
      info="(bustype='"||bustype"'"
      if var("ADDRESS") then info=info", address='"address"'"
      info=info")"
      raise syntax 98.900 array ("Message loop for this connection" info "got stopped, cannot use it anymore")
  end

  reply              -- start a new thread and start message loop

if .dbus.dir~bDebug=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "BEFORE: self~nativeStartMessageLoop"
  self~nativeStartMessageLoop -- start message loop
if .dbus.dir~bDebug=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "AFTER : self~nativeStartMessageLoop"
  return

syntax: raise propagate


-- 2014-07-23: do not allow to start message loop by third parties!
/** Obsolete implementation, do not use anymore!
*
* @param action
<ul>
<li>&quot;<code>start</code>&quot;: automatically done at connection creation time</li>
<li>&quot;<code>stop</code>&quot;: automatically done in method <code>close</code></li>
<li>&quot;<code>wait</code>&quot;: replaced by method <code>waitOnConnectionClosed</code></li>
<li>&quot;<code>active</code>&quot;: replaced by the get attribute named <code>active</code></li>
</ul>
*
* @deprecated
* @since 2014-07-23 deprecated
*/
::method messageLoop unguarded      -- query, start, stop messageLoop, waitUntilStopped
  expose active stopLoop bustype
  parse upper arg action .

if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "action:" pp(action)

  msg="Method [messageLoop] not available anymore! 'start' is done at connection creation time,"
  msg=msg"'stop' is done in new method 'close', 'active' is replaced by the get attribute named 'active',"
  msg=msg"'wait' is replaced by method 'waitOnConnectionClosed'."

  signal on syntax
  -- raise syntax 88.900 array ('Argument "start" not  "stop" not available anymore, use method "close" instead"'value'"')
  raise syntax 88.900 array (msg)

syntax: raise propagate



-- 201407, rgf: added to make good for deprecated "messageLoop('wait')"
/** Block until connection (message loop) gets closed (attribute <code>active</code> turns <code>.false</code>. */
::method waitUntilClosed unguarded
  expose active
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  guard on when active=.false



::method nativeCloseConnection    private EXTERNAL "LIBRARY dbusoorexx DBusNativeConnectionClose"

/** Closes the connection (also shuts down connection's message loop thread) which cannot be used for exchanging DBus messages anymore. */
::method close -- unguarded   -- close (private) connection
  expose busType address statistics active stopLoop

if .dbus.dir~bDebug=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line)

  if active=.false then return  -- nothing to close anymore, do not raise an error

  signal on syntax

  -- close connection by shutting down message loop thread (which must be used for communication)
  stopLoop=.true  -- stop message loop, independent of the conncection's type
  guard on when active=.false -- wait until message loop is stopped (attribute will be set there in native code)
  statistics~closed=.dateTime~new   -- remember time of closing

  if busType="private" then   -- if a private connection, then truly close it
  do
if .dbus.dir~bDebug=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line)  "==> busType="pp(busType) "address="pp(address) "about to close connection!"
     self~nativeCloseConnection        -- close the connection for good, will set CSELF to .nil
  end
  return

syntax:  raise propagate




-- keep these private so to not blow up the amount of methods one needs to be aware of
::method nativeIsConnected       private external "LIBRARY dbusoorexx DBusConnectionIsConnected"
::method nativeIsAuthenticated   private external "LIBRARY dbusoorexx DBusConnectionIsAuthenticated"
::method nativeCanSendType       private external "LIBRARY dbusoorexx DBusConnectionCanSendType"

/** Allows to query some state information on the connection returning either <code>.true</code> or <code>.false</code>.
*
* @param option can be one of:
<ul>
<li><code>&quot;A[uthenticated]&quot;</code> or <code>&quot;IsA[uthenticated]&quot;</code>,</li>
<li><code>&quot;O[pen]&quot;</code> or <code>&quot;IsO[pened]&quot;</code> or
    <code>&quot;C[onnected]&quot;</code> or <code>&quot;IsC[onnected]&quot;</code>,</li>
<li><code>&quot;T[ypecode]&quot;</code>: this option allows for a second argument which must be one or more of the DBus type character codes; if the
second argument is omitted, then a comma separated list of all supported type (codes) gets returned, instead of a Boolean value</li>
</ul>
*
*/
::method query unguarded   -- reroute invocation through message loop thread
  expose active stopLoop address bustype
  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  signal on syntax
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

  if option="O" then    -- this (is connection open/connected) we can determine without using the connection
  do
      guard on          -- make sure that no one can change the values in the middle of the following tests
      return active
  end

  if stopLoop=.true then   -- message loop got already stopped, we do not allow to restart it, a new conncection needs to be established
  do
      signal on syntax
      info="(bustype='"||bustype"'"
      if var("ADDRESS") then info=info", address='"address"'"
      info=info")"
      raise syntax 98.900 array ("Message loop for this connection" info "got stopped, cannot use it anymore to query connection properties")
  end

      -- add "_worker" to message name, submit any received arguments, dispatch via Worker's infrastructure
  forward message ("POSTMESSAGE") array (.Message~new(self, .context~name"_worker", 'A', arg(1, "Array")))

syntax: raise propagate



/* Fold functions/methods that query the state or abilities of a connection.
   "Authenticated" or "IsAuthenticated" are synonyms: returns .true, if the connection was authenticated, .false else
   "O" or "C" or "IsO" or "IsC" are synonyms: return .true, if the connection is open, .false else
   "Supports" or "TypeCode" are synonyms: if no second argument, returns all supported type codes; if
                                          second argument given, returns .true, if all type codes are supported, .false if any one is not supported
*/
::method query_worker
  parse upper arg option +1 1 option3 +3 .      -- get first char and first three chars in uppercase
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  signal on syntax
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

syntax: raise propagate


/*
   Must only be invoked from a _worker-method as the functionality is connection-related, hence
   the message loop thread must be used!

      supports() ... return "typeCodes={a,...,x}" ... returns string denoting available types
      supports("T[ypecode]", typecode)            ... returns .false/.true; typecode can be a string: only returns .true then, if all characters are supported type codes
*/

/** Checks whether the connection supports exchanging values of the given type (codes). If omitting the typecode argument,
*   then a comma-delimited string of supported type codes gets returned.
*
*/
::method canSendTypeCode  private  -- this name is neutral enough to allow extensions, if other connection-related tests are needed
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
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
     return str
  end

  use strict arg char  -- fetch arguments

  do i=1 to char~length          -- iterate over characters
     if self~nativeCanSendType(char~subChar(i))=.false then return .false
  end
  return .true


/** Destructor method that makes sure that the connection gets closed, if still active.
*/
::method uninit            -- make sure we stop the message loop thread, if necessary
  expose busType active

if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "#1 (kick off loop)"

  -- if cself<>.nil then      -- a DBUS connection pointer in hand ?
  if active=.true then     -- is the message loop still active for this connection ?
  do
     self~close            -- make sure connection gets closed
  end



   /* add listener object, syntax:  - a[dd] | r[emove], object [,[interface] [,signalname]]
                                    - g[etListeners]
   */
/** Allows to add, remove or get all ooRexx signal listeners.
*
* @param action one of <code>&quot;a[dd]&quot;</code>, <code>&quot;r[emove]&quot;</code>, <code>&quot;g[etListeners]&quot;</code>
* @param listenerObject mandatory for adding, optional for removing (all listener objects qualify)
* @param interface optional, the DBus interface to listen to, if omitted any interface will be monitored
* @param signalName optional, the signal name to listen to, if omitted any signal will be handled
* @return <ul><li>add<br><code>.true</code></li>
              <li>remove<br>number of listener objects removed</li>
              <li>getListeners<br>a copy of the listener objects array</li>
          </ul>
*/
::method listener unguarded
  expose internalSignalListeners internalRegisteredServiceObjects

if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
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

     guard on     -- accessing attributes
     internalSignalListeners~append(o)
     guard off

     return .true
  end

  else if mode="R" then    -- remove all listeners that listen to the given interface and name
  do
     use strict arg action, object=.nil, interface=.nil, signalName=.nil
     count=0
     guard on     -- accessing attributes
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
     guard off

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
/** Allows to add, remove or get all ooRexx service objects for this connection. If the service
*   objects are subclasses of the class <code>DBusService</code> then the support for introspection
*   utilities like DFeet is set up automatically. For all other ooRexx service objects, one may
*   add that particular object path introspection support by using the special &quot;default&quot;
*   in place of the object path (second argument) and supplying an instance of <code>IDBusPathMaker</code>
*   as the third argument.
*
* @param action one of <code>&quot;a[dd]&quot;</code>, <code>&quot;r[emove]&quot;</code>, <code>&quot;g[etListeners]&quot;</code>
* @param objectPath mandatory for adding, optional for removing (all object paths qualify); if
*        the special string &quot;default&quot; is being used, then the third argument must be an
*        instance of @see <code>IDBusPathMaker</code>
* @param serviceObject mandatory for adding, optional for removing (all service objects qualify)
* @return <ul><li>add<br>old service object that got replaced, <code>.nil</code> else</li>
              <li>remove<br>number of service objects removed</li>
              <li>getListeners<br>a copy of the service objects directory
*/

::method serviceObject unguarded
  expose internalSignalListeners internalRegisteredServiceObjects -- a directory
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  use strict arg action, objectPath=.nil, rexxObject=.nil

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
        rexxObject~service.connections~put(self)   -- add this connection to the serviceObject connection set
     end

     guard on
     oldValue=internalRegisteredServiceObjects[objectPath]
     internalRegisteredServiceObjects[objectPath]=rexxObject
     guard off

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



/** Set by native code, stores the native connection pointer, if connection is active. */
::attribute cself                   /* native's representation of this dbus object, set in native code */

/** One of <code>&quot;session&quot;</code>, <code>&quot;system&quot;</code>, <code>&quot;native&quot;</code>, or <code>&quot;private&quot;</code>. */
::attribute busType                 /* bustype for the dbus connection, if any; DBusBusType defines types that will be named
                                       "session", "system", "native" and maybe "private" in Rexx */

/** If set, a (private) connection to the server.
    @see <code>DBusServer</code>
*/
::attribute server                  /* if set, connection belongs to a DBusServer (i.e. a client connection) */

   /* unique id for the bus                              */
::method    nativeUniqueBusID   private external "LIBRARY dbusoorexx DBusGetUniqueBusId"

/** Returns the uniqe bus id of this connection.
* @return unique bus id
*/
::method    uniqueBusID
  expose stopLoop address bustype
  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  if stopLoop=.true then   -- message loop got already stopped, we do not allow to restart it, a new conncection needs to be established
  do
      signal on syntax
      info="(bustype='"||bustype"'"
      if var("ADDRESS") then info=info", address='"address"'"
      info=info")"
      raise syntax 98.900 array ("Message loop for this connection" info "got stopped, cannot use it anymore")
  end

  guard off    -- from now on unguarded to not block anything

      -- add "_worker" to message name, submit any received arguments, dispatch via Worker's infrastructure
  forward message ("POSTMESSAGE") array (.Message~new(self, .context~name"_worker", 'A', arg(1, "Array")))

syntax: raise propagate

::method    uniqueBusID_worker unguarded
   expose bustype
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
   if bustype="private" then return "private:rexx:"self~identityHash
   return self~nativeUniqueBusID




   -- folding into a method busName( "request" | {"release"| "yield"} | "hasOwner" | "uniqueName", name [, flags] )
   /* unique bus name for the dbus connection            */
::method  nativeUniqueBusName   private external "LIBRARY dbusoorexx DBusGetUniqueBusName"
::method  nativeRequestBusName  private external "LIBRARY dbusoorexx DBusBusNameRequest"
::method  nativeBusNameHasOwner private external "LIBRARY dbusoorexx DBusBusNameHasOwner"   -- deprecated!
::method  nativeReleaseBusName  private external "LIBRARY dbusoorexx DBusBusNameRelease"

/** Allows to request and to release a bus name, query whether a bus name has an owner and query the unique bus name.
*
* @param function one of <code>&quot;req[uest]&quot;</code>, <code>&quot;h[asOwner]&quot;</code>, <code>&quot;rel[ease]&quot;</code>, <code>&quot;u[niqueName]&quot;</code>
* @param flags if <code>&quot;req[quest]&quot;</code> function, can be one or a combination of constant names (can be used against <code>.dbus.dir</code>)
* <ul>
* <li>1 - DBUS_NAME_FLAG_ALLOW_REPLACEMENT</li>
* <li>2 - DBUS_NAME_FLAG_REPLACE_EXISTING</li>
* <li>4 - DBUS_NAME_FLAG_DO_NOT_QUEUE</li>
* </ul>
* If these flags are omitted the default value used will be <code>7</code> (all options set), cf. <a href="http://dbus.freedesktop.org/doc/dbus-specification.html#message-bus-names">http://dbus.freedesktop.org/doc/dbus-specification.html#message-bus-names</a>
*
* @return in the case of function <code>&quot;req[uest]&quot;</code>:
* <ul>
* <li>1 - DBUS_REQUEST_NAME_REPLY_PRIMARY_OWNER</li>
* <li>2 - DBUS_REQUEST_NAME_REPLY_IN_QUEUE</li>
* <li>3 - DBUS_REQUEST_NAME_REPLY_EXISTS</li>
* <li>4 - DBUS_REQUEST_NAME_REPLY_ALREADY_OWNER</li>
* </ul>
* In the case of flag  <code>&quot;rel[ease]&quot;</code>:
* <ul>
* <li>1 - DBUS_RELEASE_NAME_REPLY_RELEASED</li>
* <li>2 - DBUS_RELEASE_NAME_REPLY_NON_EXISTENT</li>
* <li>3 - DBUS_RELEASE_NAME_REPLY_NOT_OWNER<li>
* </ul>
* else no value is returned
*
*/
::method busName           -- route invocation through the message loop thread
  expose stopLoop address bustype
  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  if stopLoop=.true then   -- message loop got already stopped, we do not allow to restart it, a new conncection needs to be established
  do
      signal on syntax
      info="(bustype='"||bustype"'"
      if var("ADDRESS") then info=info", address='"address"'"
      info=info")"
      raise syntax 98.900 array ("Message loop for this connection" info "got stopped, cannot use it anymore")
  end

  guard off    -- from now on unguarded to not block anything

      -- add "_worker" to message name, submit any received arguments, dispatch via Worker's infrastructure
  forward message ("POSTMESSAGE") array (.Message~new(self, .context~name"_worker", 'A', arg(1, "Array")))

syntax: raise propagate


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
::method busName_worker unguarded
  parse upper arg function .
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

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



::method  nativeAddMatch        private external "LIBRARY dbusoorexx DBusBusAddMatch"
::method  nativeRemoveMatch     private external "LIBRARY dbusoorexx DBusBusRemoveMatch"

/** Allows to add or remove a DBus match rule, cf. <a href="http://dbus.freedesktop.org/doc/dbus-specification.html#bus-messages-add-match">http://dbus.freedesktop.org/doc/dbus-specification.html#bus-messages-add-match</a>
*
* @param action <code>&quot;A[dd]&quot;</code> or <code>&quot;r[emove]&quot;</code>a match rule
* @param filter the match rule (a string) for matching DBus messages, cf. <a href="http://dbus.freedesktop.org/doc/dbus-specification.html#message-bus-routing-match-rules">http://dbus.freedesktop.org/doc/dbus-specification.html#message-bus-routing-match-rules</a>
* @param block  optional, determines whether the call should block or not, default: <code>.false</code>
*
*/
::method match -- reroute invocation through message loop thread
  expose stopLoop address bustype
  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "/// arg(5):" pp(arg(5))

  if stopLoop=.true then   -- message loop got already stopped, we do not allow to restart it, a new conncection needs to be established
  do
      signal on syntax
      info="(bustype='"||bustype"'"
      if var("ADDRESS") then info=info", address='"address"'"
      info=info")"
      raise syntax 98.900 array ("Message loop for this connection" info "got stopped, cannot use it anymore")
  end

  guard off    -- from now on unguarded to not block anything
      -- add "_worker" to message name, submit any received arguments, dispatch via Worker's infrastructure
  forward message ("POSTMESSAGE") array (.Message~new(self, .context~name"_worker", 'A', arg(1, "Array")))

syntax: raise propagate

   /* folding methods of the "match" group into one parametrized method */
::method match_worker
  use strict arg action, filter, block=.false
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

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

syntax: raise propagate



-- o.k. the following two native messages will be invoked in the message loop, cf. method "message"
::method  nativeSignalMessage    private external "LIBRARY dbusoorexx DBusBusSignalMessage"
::method  nativeCallMessage      private external "LIBRARY dbusoorexx DBusBusCallMessage"


   /* folding methods of the "message" group into one parametrized method */
/** Call a message or issue a signal message. The arguments differ, depending whether a called method or an issued signal is intended.
*
*
* @param function <code>&quot;C[all]&quot;</code> or <code>&quot;S[ignal]&quot;</code>
<br>For a DBus call message, the parameters are:
* @param busName a valid (existing) bus name
* @param targetObjectPath a valid (existing) path to the target object that the message is directed to
* @param interface optional, defaults to empty string
* @param methodName the name of the method to call
* @param replySignature optional, defaults to empty string, determines the signature for the return value
* @param argSignature optional, defaults to empty string, determines the signature for the arguments

<br>For a DBus signal message, the parameters are:
* @param busName
* @param senderObjectPath the object path of the signal sender
* @param interface
* @param signalName the name of the signal
* @param signature optional, defines the types of optional arguments
*
* @return whatever value a called message returns
*/
::method message -- send message to service object; reroute invocation through message loop thread
  expose stopLoop address bustype
  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "/// arg(5):" pp(arg(5))

  if stopLoop=.true then   -- message loop got already stopped, we do not allow to restart it, a new conncection needs to be established
  do
      signal on syntax
      info="(bustype='"||bustype"'"
      if var("ADDRESS") then info=info", address='"address"'"
      info=info")"
      raise syntax 98.900 array ("Message loop for this connection" info "got stopped, cannot use it anymore")
  end

  guard off    -- from now on unguarded to not block anything

      -- add "_worker" to message name, submit any received arguments, dispatch via Worker's infrastructure
  forward message ("POSTMESSAGE") array (.Message~new(self, .context~name"_worker", 'A', arg(1, "Array")))

syntax: raise propagate


::method message_worker          -- send message to service object
  expose cself active
  use strict arg function, ...

  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  signal on syntax

  if cself=.nil then    -- connection got closed, cannot use it anymore
     raise syntax 93.900 array ('Connection is closed, hence cannot be used anymore')

  if active=.false then -- connection got closed, cannot use it anymore
     raise syntax 93.900 array ('Message loop for connection is not active (not running yet/anymore)')

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

  if co~code=91.999 then   -- "Message "NATIVECALLMESSAGE" did not return a result"
  do                       -- make error message more meaningful by supplying message that caused this instead:
     signal on syntax name syntax2
     if interface="" then raise syntax 91.999 array (methodName)
                     else raise syntax 91.999 array (interface"."methodName)
  end
syntax2:
  raise propagate          -- raise any other error in caller


/** Request access to a remote object to interact with, returns a <code>DBusProxyObject</code>. The resulting proxy DBus object
*   is able to understand ooRexx messages and translates them transparently to the necessary DBus message calls &quot;behind the curtain&quot;,
*   making it very easy to interact with remote DBus objects.
*
* @param busName where the remote object is located
* @param objectPath path to the object
* @return a <code>DBusProxyObject</code> to which one can send ooRexx messages by the name of the DBus message the
*         remote object can understand
*/
::method getObject unguarded           -- request remote object, return a Rexx proxy object to allow interaction with it
  use strict arg busName, objectPath
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  signal on syntax
  res=DBusDatatype(busName,"Busname")
  if res<>.true then                   -- erroneous busname?
     raise syntax 93.900 array("'busname':" res)

  res=DBusDatatype(objectPath,"ObjectPath")
  if res<>.true then                   -- erroneous busname?
     raise syntax 93.900 array("'objectPath':" res)

  return .DBusProxyObject~new(self, busName, objectPath)

syntax:
  raise propagate


/** Queries and returns the object paths on a given bus name.
*
* @param busName the bus name to search for objects
* @return an array of object paths reachable via the supplied bus name
*
*/
::method getObjectPaths unguarded   -- return an array of object paths for the supplied bus name / service name
  use strict arg busName
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  objectPathList=.array~new
  self~workerGetObjectPaths("/",busName,objectPathList)
  if objectPathList~items=0 then    -- no objects found, try a bus-derived object path
  do
      testObjPath="/" || busname~changeStr('.','/')   -- create object path from busname
      self~workerGetObjectPaths(testObjPath,busName,objectPathList)
  end

  return objectPathList


::method workerGetObjectPaths unguarded private     -- interrogates service possessing bus/service name for object paths (if that service supports that)
   use arg objPath, busName, objectPathList
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

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
   if .dbus.dir~bDebug=.true then
   do
      co=condition('o')
      if co~additional~at(2)<>.nil then
      do
         say "---> error:" pp(co~condition) pp(co~errortext)
         say "           " pp(co~additional~at(2))  -- contains the substitution error message
         say "     (hint: you could retry immediately, maybe the service was not listening yet)" "line:" pp(.line)
      end
      if .local~hasentry("RGF.ALPHA.LOW") then -- assuming that "rgf_util2.rex" is available
          say ppCondition2(co)
      say "---"
   end
   return



   /* ---------------------------------------------------------------------------------------- */
   /* as of 2011-07-05 ooRexx from trunk does not support UTF-8, hence depending on BSF4ooRexx */
/** Routine that encodes a string to UTF-8. If <code>BSF4ooRexx</code> (a Sourceforge package to bridge ooRexx with Java) is available, then Java is used to carry out the encoding,
*   otherwise an algorithm from Mike F. Cowlishaw (&quot;father of Rexx&quot;) is employed instead.
*
* @param str string to encode
* @return UTF-8 encoded string
*
*/
::routine stringToUtf8 public    -- needs BSF4ooRexx
  parse arg str
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

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
/** This routine decodes UTF-8 using Java, therefore needs <code>BSFooRexx</code> (a Sourceforge package to bridge ooRexx with Java).
*
* @param str UTF-encoded string
* @return decoded string
*/
::routine utf8ToString public    -- needs BSF4ooRexx
  parse arg str
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  return BsfRawBytes(.java.lang.String~new(bsfRawBytes(str),"UTF-8")~getBytes)


   /* ---------------------------------------------------------------------------------------- */
/** Routine that forces encoding values according to the supplied signature. This makes it possible to force a certain signature on
*   values that some DBus applications mandate, eg. in the context of variants, where the marshalling of the variant itself must be
*   done according to the target application.
*
* @param signature a valid DBus signature
* @param argument optional, otherwise the argument(s) to marshall; if omitted the native marshalling code will create marshal values
*        that correspond to the 0-value or empty strings of the respective types
* @return a two-element array, the first entry containing the string &quot;useThisSignature=&quot; followed by the signature,
*        the second entry containing the argument(s); the native marshalling code will then marshal the arguments accordingly
*
*/
::routine dbus.box       public     -- for variant types or overruling reply signatures, allow boxing
  use strict arg signature, argument=.nil
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  signal on syntax

  res=dbusDataType(signature,"s")   -- check whether valid DBus signature

  if res<>.true then
     raise syntax 93.900 array("'signature':" res)

  return .array~of("useThisSignature="signature,argument)   -- native code looks for "useThisSignature="

syntax: raise propagate



   /* ---------------------------------------------------------------------------------------- */
/* allow for sending error-messages instead of a reply back to the invoker, will get honored in
   the message loop of the DBus connection that invoked the Rexx method calling this routine   */

/** This routine is meant to be called from an ooRexx service object while processing a DBus message in the case that an error reply should be
*   transported back to the caller.
*
* @param errName optional (default: <code>&quot;org.freedesktop.DBus.Error.RexxServiceRaised&quot;</code>)
* @param errMessage optional (default: .nil)
* @condition 93.900
*
*/
::routine raiseDBusError   public   -- this condition will be turned into an error-message in native code
  use arg errName="org.freedesktop.DBus.Error.RexxServiceRaised", errMessage=.nil
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  signal on syntax
  raise syntax 93.900 array (errName, errMessage, "ERRORREPLY")

syntax: raise propagate


   /* ---------------------------------------------------------------------------------------- */
/** Returns the version information that includes the library version and this package's version information.
*
* @return version string formatted as: <code>&quot;library dbusoorexx=[major*100+minor.yyyymmdd], compile-time dbus=[major.minor.micro], runtime dbus=[major.minor.micro], dbus.cls=[major*100+minor.yyymmdd]&quot;<code>
*/
::routine DBusVersion   public
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  return DBusVersionLibrary()", package dbus.cls=[".dbus.dir~version"]"


/* ============================================================================================= */
/* ooRexx proxy for a DBus object, responsible for processing messages  */

/** Proxy class representing a remote DBus object to interact with using ooRexx messages.
*/
::class "DBusProxyObject" public

/** Constructor method that initializes the attributes, introspects the remote DBus object using the result for transparent interaction with it (if avalable). An ooRexx programmer merely needs to
*   send ooRexx messages and supply arguments without type information! The proxy will transparently reformulate the ooRexx message into the appropriate DBus message call.
*
* @param proxy.connection the connection (a DBus object) to use
* @param proxy.busname the busname to use
* @param proxy.objectPath the object path to the remote DBus object
*/
::method init
  expose proxy.busname proxy.connection proxy.objectPath proxy.methods proxy.introspectData
  use strict arg proxy.connection, proxy.busname, proxy.objectPath

if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  proxy.introspectRootNode=.nil
  proxy.introspectData    =""

  signal on syntax                  -- query object for its introspection data, if any

  proxy.introspectData=proxy.connection~message("call", proxy.busName,        - -- .dbus.dir~serviceDBus,      - -- busName
                                          proxy.objectPath,             - -- .dbus.dir~pathDBus,         - -- path to DBus object
                                          .dbus.dir~Introspectable,     - -- Introspect's standard interface name
                                          "Introspect",                 - -- message name
                                          "s",                          - -- return signature
                                           "")                            -- arg signature (no args)
   -- create introspect parse tree and save it
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
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
     -- create directory with those names that we should proxy for the proxy object
  proxy.methods=IDBus.getMethodsForProxy(proxy.introspectRootNode)

::attribute proxy.methods  get            /* make introspection parse tree available */

/** Method that processes the introspect data or a file that contains the XML encoded introspection data for the remote object.
*
* @param introData either the introspect data or a file containing the XML encoded introspect data
* @return introData the method received as argument
*
*/
::method proxy.parseIntrospectData      -- parse introspect data, memorize introspectData and introspectFileName, if any
  expose proxy.introspectData proxy.introspectFileName
  use strict arg introData
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

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


/** Pass-through method for DBus messages that have ooRexx method implementations, which would therefore not be passed on. Any arguments
*   supplied to the message will be forwarded as arguments for the DBus method call.
*
* @param methodName DBus method name to be called
* @return the return value from the DBus message call, if any
*/
::METHOD proxy.dispatch                -- allow sending messages that would be intercepted on the Rexx side (e.g. by .Object)
   parse arg methodName
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
   -- create UNKNOWN format (first argument is unknown method name)
   self~unknown(methodName, arg(2, 'A'))
   if var('RESULT') then return result -- if a return value given, return it
   return


/** This method turns the received ooRexx message into the appropriate DBus message call
*   using the introspection data for the remote object. The support includes camouflaging
*   DBus properties as ooRexx attributes.
*/
::method unknown        -- implements sending message calls to remote object
  expose proxy.busname proxy.connection proxy.objectPath proxy.methods
  use arg msgName, msgArgs
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

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




/* ============================================================================================= */
/* Allows an ooRexx object to listen to DBus signals. Intentionally not public. Will
   be used by method 'listener' subfunction 'add' and will be retrieved by the native
   code.
*/
/** Class that gets used by the method <code>listener</code> in the class <code>DBus</code> to wrap an ooRexx object that should
*   get invoked whenever a signal is received.
*
* @see method <code>listener</code> in the class <code>DBus</code>
*/
::class "DBUSSignalListener"

/**
* @param listenerObject the ooRexx listener object
* @param interface optional, if present restricts the listening to signals from this DBus interface
* @param signalName optional, if present restricts the listening to this signal name
*/

::method init
  expose listenerObject interface signalName
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  use strict arg listenerObject, interface=.nil, signalName=.nil

::attribute listenerObject

::attribute interface

::attribute signalName


/* ============================================================================================= */
/* Allows an ooRexx object to serve DBus method invocations in an easy manner, by
   adding the ability to e.g. dynamically determine replySignatures from the introspect
   data or make it rather easy for a service object to emit signals, etc.
   Definition of this class allows it to be used as a mixin.
*/
/** Base class for allowing an ooRexx class to be used for implementing a DBus service.
*
*/
::class "DBusServiceObject" mixinclass object  public

/** Constructor method to initialize attributes and to process the optionally supplied introspection data that defines the services available to DBus clients.
*
* @param introData optional introspection data
*
*/
::method init                 -- intorData, service.connection, service.objectPath; introData may be supplied later, using the respective methods
  expose  service.connections service.introspectRootNode service.methods
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  use strict arg introData=.nil

  service.methods=.nil
  service.connections=.set~new   -- set of connections in which this service object gets used

  signal on syntax
  self~service.parseIntrospectData(introData)
  return

syntax:  -- make sure a parse tree is available, as caller may intercept condition and proceed


  empty='<!DOCTYPE node PUBLIC "-//freedesktop//DTD D-BUS Object Introspection 1.0//EN" ' -
        '"http://www.freedesktop.org/standards/dbus/1.0/introspect.dtd">                ' -
        '<node/>                                                                        '
  self~service.parseIntrospectData(empty)
  raise propagate -- raise condition in caller


/** Method that analyzes and sets up introspection data for this DBus service.
*
* @param introData introspection data to parse, can be a file name containing the XML data
* @return introData the received argument value
*/
::method service.parseIntrospectData      -- parse introspect data, memorize introspectData and introspectFileName, if any
  expose service.introspectData service.introspectFileName
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
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
call idbus.dumpIDBus self~service.introspectRootNode
*/
  return introData

syntax:  -- make sure a parse tree is available, as caller may intercept condition and proceed
  raise propagate -- raise condition in caller


/** Implements the DBus Introspect method to allow potential DBus service clients to learn about the published interfaces.
*
* @return the XML encoded introspect data
*/
::method    Introspect              -- return introspection data to client
  expose    service.introspectData
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  return dbus.box("s", service.introspectData)

::attribute service.connections              /* object's connection to use  */

::attribute service.introspectRootNode get   /* make introspection parse tree available */
::attribute service.introspectData get
::attribute service.introspectFileName get

/** Attribute setter method that allows to set the introspection root node attribute. In addition any methods in the introspection data get cached for faster lookup.
*
*/
::attribute service.introspectRootNode set   /* make introspection parse tree available */
  expose service.methods service.introspectRootNode
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  use arg service.introspectRootNode
     -- create directory with those names that we should proxy for the proxy object
  service.methods=IDBus.getMethodsForProxy(service.introspectRootNode)


::attribute service.methods  get             /* make introspection parse tree available */

::attribute service.replySignature get       /* returns the reply signature for the given member or .nil */
  expose service.methods service.objectPath
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
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


/** Utility method to make issuing a signal easy through all the connections that are currently served. If the service object has introspection methods
*   defined and cached, then this definition is looked up to determine the signal's interface and signature.
*
* @param objectPath optional, DBus object path of sender, defaults to <code>.nil</code>
* @param interface optional, DBus interface name, defaults to empty string
* @param member signal name
* @param signature optional, argument signature, defaults to empty string
* @param arg1...argN optional arguments to be sent with the signal
*
*/
::method service.sendSignal   -- utility method to ease emitting signals from the service object;
                              -- will emit the signal to all connections this service object is registered with
  expose service.methods service.connections
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  use strict arg objectPath=.nil, interface="", member, ...

  signature=""                            -- default to no arguments
   -- canonize (empty,left-out) values
  if interface=.nil then interface=""
  if objectPath=""  then objectPath=.nil

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
  args=.array~of("signal", objectPath, interface, member, signature)
  do i=4 to arg()       -- append arguments to the signal, if any
     args~append(arg(i))
  end

  signal on syntax

  do conn over service.connections  -- send signal to all connections this service object is being used
     if conn~cself=.nil then        -- connection closed?
        service.connections~remove(conn)  -- remove connection from set
     else
     do
        if objectPath=.nil then     -- no object path supplied, find an object path for this service object on the connection
        do
           opath=.nil
           s=conn~serviceObject("GetRegisteredServiceObjects")~supplier
           do while s~available     -- loop over all index/item pairs
              if s~item=self then
              do
                 if s~index~left(1)="/" then    -- found an object path index entry
                 do
                    opath=s~index               -- use this as object path, leave supplier loop
                    leave
                 end
                 else if s~index="default" then -- service object is a default entry
                 do
                    opath="/"                   -- supply a root path value as the default value
                 end
              end
              s~next             -- get next index/item pair
           end

           if opath=.nil then    -- service object is not registered with connection anymore, remove connection from service object as well
           do
              service.connections~remove(conn)
              iterate
           end

           args[2]=opath         -- set object path in signal message
        end

        conn~sendWith("message", args) -- send the signal message
     end
  end
  return
syntax:raise propagate


               /* service requests  */
/** This method turns the received ooRexx message into the appropriate DBus message call using the
* introspection data for the service object.
*/
::method unknown
  expose service.methods
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  use arg msgName, msgArgs
-- call dump2 msgArgs, pp2(self)"::UNKNOWN" pp(msgName)

  errorText='Object"' self '"does not understand message "'msgName'"'

  slotDir=.nil
  if msgArgs~isA(.array), msgArgs~size>0 then
  do
     slotDir=msgArgs[msgArgs~last]  /* contains useful information about method invocation,
                                      like method name, method type, signature, etc.  */
     if \slotDir~isA(.directory) then
        slotDir=.nil
  end

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




/* ============================================================================================= */
/* ------------------------------------------------------------------------------------- */
/*  DBusDataType( value [,typeName])
    typeNames: B[usName], I[nterfaceName], M[emberName], O[bjectPath], S[ignature]

    modelled after Rexx' DataType(...)
*/

/** Routine that determines which DBus datatype a value may be (or <code>.nil</code>, if not determinable) or
*   tests whether a supplied value is of the given DBus datatype.
*
*  @param value a string value representing a DBus value of type <code>&quot;B[usname]&quot;</code>,
*         <code>&quot;I[nterface]&quot;</code>, <code>&quot;M[ember]&quot;</code>,
*         <code>&quot;O[bjectPath]&quot;</code> or <code>&quot;S[ignature]&quot;</code>
*
*  @param type optional, one of <code>&quot;B[usname]&quot;</code>, <code>&quot;I[nterface]&quot;</code>,
*         <code>&quot;M[ember]&quot;</code>, <code>&quot;O[bjectPath]&quot;</code> or
*         <code>&quot;S[ignature]&quot;</code>
*
* @return <dl>
*         <dt>if only the first argument is supplied, then returns either
*         <dd><code>&quot;OBJECTPATH&quot;</code>, <code>&quot;INTERFACENAME&quot;</code>,
*             <code>&quot;BUSNAME&quot;</code>, <code>&quot;MEMBERNAME&quot;</code> or
*             <code>.nil</code>, if neither of these types can be determined
*
*         <dt>if both arguments are supplied, then the first argument is tested whether it
*             is of the DBus type expressed in the second argument, returning
*         <dd><code>.true</code> or <code>.false</code>, respectively
*         </dl>
*/
::routine DBusDataType public    -- check or determine whether dbus datatype
  parse arg value, type .
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

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

   -- so far, so good, signature looks good, now let us validate the signature through dbus:
  return DBusValidate(value,"s")


syntax:
  raise propagate       -- raise in caller



/* =============================== DBusServer ====================================================== */

-- ; connection to clients using .DBus

/* ------------------------------ class definition ------------------------------ */
/** This class implements the functionality of a private ooRexx DBus server which makes
*   it easy to use ooRexx to create private DBus servers, serving any clients via DBus,
*   independent of their location (usually communicating via the Internet) and
*   operating system (provided the DBus infrastructure is available on the client's computer).
*/
::class "DBusServer"   public
/** Constructor method that initializes attributes. Note, the native code will set the values of
* some of the attributes!
*
*   @param address a private DBus server address, this server should listen to
*   @param defaultService optional, a DBusService ooRexx object that supplies the default services
*          to any new connected client
*   @param defaultListener optional, an ooRexx object that will be used as the listener for any new
*          connected client
*   @param allowAnonymous optional, defaults to <code>.false</code>; if set to <code>.true</code>
*          then connections from anonymous clients are acceptable
*/
::method init
  expose cself address allowAnonymous defaultService defaultListener connections  -
         watchLoopActive watchlist timeoutLoopActive watchConnections
if .dbus.dir~bDebugServer=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

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

/** Getter definition for the attribute <code>connections</code>.
* @return a copy of the connection array
*/
::attribute connections get            -- list of established connections
  expose connections
if .dbus.dir~bDebugServer=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  return connections~copy              -- return a copy of the current connections

/** Getter definition for the attribute <code>watchLoopActive</code>.
* @return <code>.true</code>, if the server message loopo is active, <code>.false</code> else
*/
::attribute watchLoopActive  get       -- indicates whether the server message loop is active or not

/** Do not use ! Method gets invoked by the native code whenever a new client connects to this private DBus server, such
*   that the connection's message loop can be started, allowing the client to use it to interact with
*   this private DBus server.
*
*   @param the connection through which the client connected to this private DBus server
*/
::method newConnection     -- new connection to this server received (from callback in native code)
  expose defaultService defaultListener connections watchConnections
if .dbus.dir~bDebugServer=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  use strict arg conn

  conn~startMessageLoop    -- start message loop for this new connection

  connections~append(conn)             -- add new connection to our list of accepted connections
  if defaultService<>.nil then         -- add service object
  do
     if defaultService~isA(.DBusServiceObject) then
     do
        aCopy=defaultService~copy         -- each DBusServiceObject keeps its own connection to its client
        aCopy~service.connections~put(conn)     -- assign this connection
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

  return


/** Method gets invoked by the native code whenever a connection gets lost. The method
*   will close the connection and remove it from the <code>connections</code> attribute that maintains
*   all served connections.
*
*   @param connection through which the client is connected to this private DBus server
*/
::method disconnect                    -- invoked from native code (not reliably invoked all the time as of 1.6.4 on Ubuntu)
  expose connections
if .dbus.dir~bDebugServer=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  use strict arg conn

  conn~close                           -- make sure we cannot use it anymore (it wouldn't work)
  res=connections~removeItem(conn)     -- remove the disconnected connection from the client


::method nativeServerWatchLoop   private external "LIBRARY dbusoorexx DBusServerWatchLoop"
::method nativeServerStartup     private external "LIBRARY dbusoorexx DBusNativeServerStartup"


/** This method starts up the private server and the server's watch loop thread.
*
*  @condition if server was already started a syntax 93.900 condition will be raised
*/
::method startup                 unguarded  -- startup server
  expose allowAnonymous cself address
if .dbus.dir~bDebugServer=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  if cself<>.nil then
     raise syntax 93.900 array (self": server was already started")

if .dbus.dir~bDebugServer=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "about to: self~nativeServerStartup"
  self~nativeServerStartup(address) -- create server listening on given address

  reply                             -- create a new thread
if .dbus.dir~bDebugServer=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "BEFORE: self~nativeServerWatchLoop"
  self~nativeServerWatchLoop   -- start the server message loop on the new thread
if .dbus.dir~bDebugServer=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "AFTER : self~nativeServerWatchLoop"


-- as of DBus 1.4.14, the private server does not receive a "Disconnected" signal, hence testing the connections constantly
/** This method creates a Rexx thread which constantly watches connections to make sure
*   that any stalled connections get disconnected.
*
*   @param sleepTime optional argument (defaults to 0.1 second) that determines the sleeping time
*                    between checks
*/
::method watchConnections  unguarded   -- check whether client connections are available, if not disconnect the stalled connecition
  expose connections watchConnections
if .dbus.dir~bDebugServer=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  use strict arg sleepTime=.1 -- check every 1/10 second

  if arg()=0, .dbus.Dir~bDebugServer=.true then  -- if no args and in server debug mode, enlarge sleepTime to not have too much debug output
     sleepTime=1

  guard on
  watchConnections=.true      -- indicate that the watch Connections thread is started
  guard off
if .dbus.dir~bDebugServer=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "before reply"
  reply

if .dbus.dir~bDebugServer=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "after reply, BEFORE: entering the loop"

  signal on halt
  do forever while connections~items>0 -- as long as client connections, probe them
if .dbus.dir~bDebugServer=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "about to sleep" pp(sleepTime) "secs ..."

     call sysSleep sleepTime  -- sleep

if .dbus.dir~bDebugServer=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "awoke, connections~items="pp(connections~items)
     guard on
     arr=connections~copy     -- get a copy of all connections
     guard off
     do conn over arr         -- iterate over all client connections and probe them
if .dbus.dir~bDebugServer=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "conn~query('open': --> before, conn="pp(conn) conn~query('open')
        if conn~query("open")=.false then
        do
if .dbus.dir~bDebugServer=.true then say "!!!" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "conn~query('open': --> DISCONNECTING conn="pp(conn)
           self~disconnect(conn)             -- disconnect will remove closed conn from connections !
        end
     end
  end
if .dbus.dir~bDebugServer=.true then say "\\\" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "about to GUARD ON, to prepare to leave ..."
  guard on
  watchConnections=.false
  guard off

if .dbus.dir~bDebugServer=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "AFTER, about to return from method"
  return

halt:       -- a HALT condition, make sure all known connections get closed
  guard off -- make sure guard is off
if .dbus.dir~bDebugServer=.true then say "///" pp(.dateTime~new)", tid="pp(DBusGetTID()) "->" pp(self)":" pp(.context~executable) pp(.context~name) "line:" pp(.line) "HALT signal: closing all connections..."
  do conn over connections
     conn~close
  end
  return


::method nativeStartServerTimeoutLoop   private external "LIBRARY dbusoorexx DBusServerTimeoutLoop"

/** Do not use! Meant to be used from native code, if a timer callback is set or removed.
*
* @param &quotSTArt&quot; or &quot;STOp&quot
* @param timeoutDataPointer if first argument has the value &quotSTArt&quot; an opaque value from the native code (a Pointer)
*/
::method timerLoop         unguarded         -- "START" or "STOP"
  expose timeoutLoopActive
if .dbus.dir~bDebugServer=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "line:" pp(.line)
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
  reply                                -- create a new thread

if .dbus.dir~bDebugServer=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "after REPLY"
  self~nativeStartServerTimeoutLoop(timeoutDataPointer)  -- start the timeout loop, pass (*data) via Rexx to native function




::method nativeServerShutdown       private external "LIBRARY dbusoorexx DBusNativeServerDisconnect"

/** This method shuts down the private server by stopping the watch loop, shutting down the server and
*   disconnecting its clients.
*/
::method shutdown                   unguarded   -- shutdown server
  expose connections cself watchLoopActive
if .dbus.dir~bDebugServer=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  if cself=.nil then
     raise syntax 93.900 array (self": shutdown not possible, as server was not started")

  guard on
  watchLoopActive=.false        -- indicate to serverLoop to stop
  guard off

  self~nativeServerShutdown      -- stop accepting connections

  do conn over connections       -- close all connections to service
     conn~close                  -- close connection
     connections~removeItem(conn)   -- remove connection
  end



::method nativeServerID             private external "LIBRARY dbusoorexx DBusNativeServerGetId"
/** Returns this private server's ID from native code.
*
*   @return the server's ID
*/
::method serverID
  expose cself
if .dbus.dir~bDebugServer=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  if cself=.nil then return .nil
  return self~nativeServerID


::method nativeActive               private external "LIBRARY dbusoorexx DBusNativeServerIsActive"

/** Tests whether this server is still active (connected).
*
*   @return <code>.true</code>, if the server is active (connected), <ocde>.false</code> else
*/
::method active
  expose cself
if .dbus.dir~bDebugServer=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  if cself=.nil then return .false
  return self~nativeActive

   -- for debugging:
::method nativeServerAddress        private external "LIBRARY dbusoorexx DBusNativeServerGetAddress"
/** Returns this private server's address from native code.
*
*   @return the address this server is servicing
*/
::method serverAddress
  expose cself
if .dbus.dir~bDebugServer=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)
  if cself=.nil then return .nil
  return self~nativeServerAddress

/* --- end of DBusServer code */




/* =============================== Introspection related classes, routines ========================= */

/* cf. <http://standards.freedesktop.org/dbus/1.0/introspect.dtd> */

/* ------------------------------ class definition ------------------------------ */
/** This class parses DBus introspection data into a parse tree and makes its
*   definitions available to clients.
*
*  @see <http://standards.freedesktop.org/dbus/1.0/introspect.dtd>
*/
::class "IDBus"               public   -- define base class, attributes name and content
::attribute name           -- name: name of the element, CDATA
::attribute parent         -- parent: node to which this one belongs to, if any
::attribute content        -- content: whatever elements an element contains in document order

/** Caseless comparator method used by the array <code>sort</code> method. Allows to sort caselessly by name.
*
*   @return <code>1</code>, <code>0</code> or <code>-1</code> if this name is lexically larger,
            equal or smaller than the argument
*/
::method compareTo         -- allow for sorting by name
  expose name
  use arg otherValue
  return (self~class~id name)~caselessCompareTo(otherValue~class~id otherValue~name)

/** Constructor method, initializing attributes.
*
* @param name optional (defaults to empty string)
*/
::method init              -- constructor
  expose name content node
  use strict arg name=""
  parent=.nil
  content=.array~new

/** Class method for creating the instrospection tree.
*
*  @param data the introspection data adhering to the DBus Introspection DTD or
*         a file name containing the Introspection data to parse
*
*  @return the root node of the resulting introspection tree
*/
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
-- say "*** ----> ---> --> ->" self": node" pp(name)
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
-- say "*** ----> ---> --> ->" self": interface" pp(name)
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
-- say "*** ----> ---> --> ->" self": method" pp(name)
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
-- say "*** ----> ---> --> ->" self": signal" pp(name)
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
-- say "*** ----> ---> --> ->" self": arg" pp(name)
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

-- say "*** ----> ---> --> ->" self": property" pp(name)
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

-- say "*** ----> ---> --> ->" self": dumping tree using rootNode="pp(rootNode)
-- call idbus.dumpIDBus rootNode

  return rootNode

syntax:

   say "*** ----> ---> --> ->" self": newIntrospection - SYNTAX-exception occurred ! ..."

  raise propagate       -- raise condition in caller

/** Tests wheter the introspection tree contains a node of the supplied type.
*
*  @param kind optional, or one of <code>&quot;I[nterface]&quot;</code>,
*                     <code>&quot;M[ethod]&quot;</code>,
*                     <code>&quot;N[ode]&quot;</code>,
*                     <code>&quot;S[ignal]&quot;</code>,
*                     <code>&quot;P[roperty]&quot;</code>,
*                     <code>&quot;AR[gument]&quot;</code>,
*                     <code>&quot;AN[nnotation]&quot;</code>
*
*  @return <code>.true</code> if the supplied kind (type) is contained in the introspection tree,
*          <code>.false</code> else
*/
::method contains             -- returns .true if the supplied type exists in the tree
  parse upper arg kind +1 1 kind2 +2

  if pos(kind,"AIMNSP")=0 then
     raise syntax 88.916 array ('"type"', '"I[nterface]", "M[ethod]", "N[ode]", "S[ignal], "P[roperty]", "AR[gument]", "AN[notation]"', arg(1))

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

/** Counts the number of occurrences of the supplied type (kind) in the introspection tree.
*
*  @param kind optional, or one of <code>&quot;I[nterface]&quot;</code>,
*                     <code>&quot;M[ethod]&quot;</code>,
*                     <code>&quot;N[ode]&quot;</code>,
*                     <code>&quot;S[ignal]&quot;</code>,
*                     <code>&quot;P[roperty]&quot;</code>,
*                     <code>&quot;AR[gument]&quot;</code>,
*                     <code>&quot;AN[nnotation]&quot;</code>
*
*  @return number of occurrences of the supplied kind (type)
*/
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
/** This class represents an introspection node. */
::class "IDBusNode"           public subclass IDBus

/* ------------------------------ class definition ------------------------------ */
/** This class represents a DBus interface node. */
::class "IDBusInterface"      public subclass IDBus

/* ------------------------------ class definition ------------------------------ */
/** This class represents a DBus method node and defines method related attributes and methods. */
::class "IDBusMethod"         public subclass IDBus
::attribute argSignature
::attribute replySignature

/** Constructor method, initializing attributes.
*
* @param name optional (defaults to empty string)
*/
::method init              -- constructor
  expose argSignature replySignature
  use strict arg name
  argSignature=""           -- default value
  replySignature=""
  forward class (super) -- array (name) -- continue   -- invoke superclass constructor

/** Worker method that creates the argument's and reply signatures from its <code>IDBusArg</code> subnodes if any.
*/
::method createSignatures  -- create signatures from IDBusArg definitions
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
/** This class represents a DBus call method node. */
::class "IDBusCallMethod"     public subclass IDBusMethod

/* ------------------------------ class definition ------------------------------ */
/** This class represents a DBus signal method node. */
::class "IDBusSignalMethod"   public subclass IDBusMethod

/* ------------------------------ class definition ------------------------------ */
/** This class represents a DBus property method node. */
::class "IDBusPropertyMethod" public subclass IDBusMethod   -- properties are actually methods to get/set values
::attribute type           -- type: type code
::attribute access         -- access: read, readwrite, write

/** Constructor method, initializing attributes.
*
* @param name the name of the property
* @param type the type of the property
* @param access one or both of <code>&quot;read&quot;</code> and <code>&quot;write&quot;</code>
*
*/
::method init              -- constructor
  expose type access
  use strict arg name, type, access

  forward class (super) array (name) continue   -- invoke superclass constructor

  if pos("read",  access)>0 then self~replySignature=type
  if pos("write", access)>0 then self~argSignature =type



/* ------------------------------ class definition ------------------------------ */
/** This class represents a DBus argument node. */
::class "IDBusArg"            public subclass IDBus
::attribute type           -- type: type code
::attribute direction

/**  Constructor method, initializing attributes.
*
* @param name the name of the property
* @param type the type of the property
* @param direction one or both of <code>&quot;in&quot;</code> and <code>&quot;out&quot;</code>
*/
::method init              -- constructor
  expose type direction
  use strict arg name, type, direction

  forward class (super) array (name) -- continue   -- invoke superclass constructor


/* ------------------------------ class definition ------------------------------ */
/** This class represents a DBus annotation node. */
::class "IDBusAnnotation"     public subclass IDBus
::attribute value          -- value: CDATA

/**  Constructor method, initializing attributes.
*
* @param name the name of the property
* @param value the annotation text
*/
::method init              -- constructor
  expose value
  use strict arg name, value
  forward class (super) array (name) -- continue   -- invoke superclass constructor


/* ========================== introspection related routines =========================== */

/* ------------------------------------------------------------------------------------- */
/** A utility routine that searches the introspection tree for methods of type <em>call</em>,
*   <em>signal</em> or <em>property</em> and returns their names and method definitions in a
*   Rexx directory.
*
* @param rootNode the introspection's root node to use for the search
*
* @return a Rexx directory mapping the simple and fully qualified names of methods to their
*         method definition to ease sending messages to a service object
*/
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
/** Utility routine that analyzes the introspection tree and checks for duplicate definitions.
*   Optionally dumps the visitied nodes to <code>.output</code>.
*
* @param rootNode the introspection's root node to use for the search
* @param bShowProgress if <code>.true</code> then gives additional (debugging) output
*/
::routine IDBus.analyzeIDBUSMethods    public   -- analyze dump the tree, showing methods with their signatures
  use arg rootNode, bShowProgress=.false

  seenMethodNames=.directory~new

  if bShowProgress=.true then say "node:" pp(rootNode~name)
  start=3
  step=3
  do n over rootNode~content
     if n~isA(.IDBusNode) then
     do
        indent=start
        strIndent=" "~copies(indent)
        if bShowProgress=.true then say strIndent "node:" pp(n~name) -- "content~items:" pp(n~content~items)
     end

     do i over n~content   -- interfaces etc.
        if i~isA(.IDBusInterface) then
        do
           indent=start+step
           strIndent=" "~copies(indent)
           if bShowProgress=.true then say strIndent "interface:" pp(i~name)

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
                    say "   ***" pp(m~parent~name) kind pp(m~name) "already defined as" pp(seen~name) "a" pp(seen~class~id) pp(seen~parent~name)
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
/** Utility routine that dumps the introspection tree to <code>.output</code>, showing methods
*   with their signature.
*
* @param rootNode the introspection's root node to use for the search
*/
::routine IDBus.dumpIDBus  public -- dump the tree, showing methods with their signatures
  use arg rootNode

  dt=.dbus.dir~dataTypes         -- get datatype dir, "r" -> struct, "e" -> dict(entry)

  say "node:" pp(rootNode~name)
  start=3
  step=3
  do n over rootNode~content~sort
     if n~isA(.IDBusNode) then
     do
        indent=start
        strIndent=" "~copies(indent)
        say strIndent "node:" pp(n~name) -- "content~items:" pp(n~content~items)
     end

     do i over n~content~sort   -- interfaces etc.
        if i~isA(.IDBusInterface) then
        do
           indent=start+step
           strIndent=" "~copies(indent)
           say strIndent "interface:" pp(i~name)

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
                 if a~isA(.IDBusArg) & a~direction="in" then
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
                 so=pp(m~replySignature)
                 if so~length<minWidth then so=so~left(minWidth, ".")

                 say strIndent so kind~left(8) m~name "("tmpStr~strip")" "->" pp(m~argSignature)
              end
              else   -- special handling for get&set properties: show both signatures separately
              do
                 so=pp("")   -- first show the setter, which has no return value
                 if so~length<minWidth then so=so~left(minWidth, ".")
                 say strIndent so kind~left(8) m~name "("tmpStr~strip")" "->" pp(m~argSignature)

                 so=pp(m~replySignature) -- show the getter method
                 if so~length<minWidth then so=so~left(minWidth, ".")
                 say strIndent so kind~left(8) m~name "()" "->" pp("")
              end

           end
        end
        say
     end
     say
  end
  return




/* =============================== Introspection related classes, routines ========================= */
/** There might be cases where Introspect data does not get supplied by the service object,
   although there are well defined interfaces to it. In case the Rexx programmer knows of
   them this class allows to define these and get a rendering that is suitable to submit
   to the <code>.IDBus</code> class (<code>rootNode=.IDBus~newIntrospection(introspectData</code>) and assign the
   resulting rootNode to the <code>DBusProxyObject</code> (<code>proxObject~proxy.introspectRootNode(rootNode)</code>).
   <br/>
   This is a simple implementation ignoring the names of arguments and dealing with
   DBus signatures only.

   @see cf. <http://standards.freedesktop.org/dbus/1.0/introspect.dtd>
*/
::class IntrospectHelper   PUBLIC         -- class to allow for creating interface definitions

/**  Constructor method, initializing attributes.
*
* @param nodeName optional (defaults to empty string), the name of the node
*/
::method init
  expose nodeName list
  use strict arg nodeName=""
  list=.array~new

/** The list of introspection nodes (an array). */
::attribute list get

/** The name of the node. */
::attribute nodeName get

/** An alias for the attribute <code>nodeName</code> to ease the <code>compareTo</code> method.
*  @see <code>compareTo</code> method
*/
::method name           -- alias for nodeName to ease compareTo code
  expose nodeName
  return nodeName

/** Caseless comparator method used by the array <code>sort</code> method. Allows to sort caselessly by name.
*
*   @return <code>1</code>, <code>0</code> or <code>-1</code> if this name is lexically larger,
            equal or smaller than the argument
*/
::method compareTo      -- allow sorting
  expose nodeName
  use arg otherValue
  return nodeName~caselessCompareTo(otherValue~name)

/** Creates and adds an interface node to the introspection list.
*
* @param interfaceName optional (defaults to the empty string)
*/
::method addInterface
  expose list
  use strict arg interfaceName=""
  o=.IntrospectHelperInterface~new(interfaceName)
  list~append(o)
  return o

/** Creates and adds a node to the introspection list.
*
* @param nodeName optional (defaults to the empty string)
*/
::method addNode
  expose list
  use strict arg nodeName=""
  o=.IntrospectHelper~new(nodeName)
  list~append(o)
  return o


/** Creates a string rendering of the introspection data and returns it.
*
* @return a string rendering representing the currently defined introspection data
*/
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

/** Worker method that fills in the string renderings of the introspectin definitions.
*
* @param m the mutable buffer object to add string renderings to
* @param o an introspection object to be rendered as a string
* @param indent optional (defaults to <code>0</code>, determines the number of <code>tab</code> characters to use as indentation
* @param lf a single line feed character
* @param lf2 two line feed characters
* @param tab indentation character, a string of four blanks
*
*/
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



/* ============================================================================================= */
/** Utility class to ease debugging introspection data.
*
* @see <code>IntrospectionHelper</code>
*/
::class introspectHelperInterface      -- helper class

/**  Constructor method, initializing attributes.
*
* @param interfaceName the name of this DBus interface
*/
::method init
  expose interfaceName list
  use arg interfaceName
  list=.array~new


/** Caseless comparator method used by the array <code>sort</code> method. Allows to sort caselessly by interface name.
*
*   @return <code>1</code>, <code>0</code> or <code>-1</code> if this interface name is lexically larger,
            equal or smaller than the argument
*/
::method compareTo      -- allow sorting
  expose interfaceName
  use arg otherValue
  return interfaceName~caselessCompareTo(otherValue~name)


/** The list of introspection nodes (an array). */
::attribute list get

/** The interface name. */
::attribute interfaceName get
::method name           -- alias for interfaceName to ease compareTo code
  expose interfaceName
  return interfaceName

/** Adds the method definition string to the introspection list.
*
* @param name the name of the method
* @param in optional (defaults to the empty string), the signature for the arguments (parameters) for the method, if any
* @param out optional (defaults to the empty string), the signature for the return value, if any
*/
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

/** Adds the property definition string to the introspection list.
*
* @param name the name of the property
* @param type the signature representing the property
* @param access the access types, one of <code>read</code>, <code>write</code> or <code>readwrite</code>,
*/
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

/** Adds the signal definition string to the introspection list.
*
* @param name the name of the method
* @param arg optional (defaults to the empty string), the signature for the arguments (parameters) for the method, if any
*/
::method addSignal      -- name, signature of arguments as one string
  expose list
  use strict arg name, arg=""
  signal on syntax
  str=DBusDataType(arg, "S")
  if str<>.true then raise syntax 93.900 array("'arg'-signature:" str)

  list~append("signal" name 'arg=['arg']')
  return
syntax: raise propagate    -- raise error in caller


/** Creates a string rendering of the introspection data and returns it.
*
* @return a string rendering representing the currently defined introspection data
*/
::method makestring     -- create a string, sort ascendingly
  expose list interfaceName
  m=.MutableBuffer~new
  tab=" "~copies(4)
  lf ="0a"x
  lf2="0a0a"x
  self~workMakeStringInterface(m,self,0,lf,lf2,tab)
  return m~string


/** Worker method that fills in the string renderings of the introspection definitions.
*
* @param m the mutable buffer object to add string renderings to
* @param o an introspection object to be rendered as a string
* @param indent optional (defaults to <code>0</code>, determines the number of <code>tab</code> characters to use as indentation
* @param lf a single line feed character
* @param lf2 two line feed characters
* @param tab indentation character, a string of four blanks
*
*/
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




/* ============================================================================================= */
/** Define a class that leads an Introspect finder to the available object path(s).
   An instance of this class may be registered with the connection's serviceObject-method
   using the special name "default" as a pseudo object path.
*/
::class IDBusPathMaker public

/** Class method that creates object path introspection data to allow DBus service object clients
*   to find the published object paths the DBus service object services on the given connection.
*   The connection will get a <code>serviceObject</code> by the special name <code>&quot;default&quot;</code>
*   added, which replaces any other previously defined <code>IDBusPathMaker</code> object.
*
* @param conn the DBus connection which gets the object path fragments/segments added, such that DBus clients
*       become able to find all object paths that get serviced by this connection
*
* @return the number of objects that get serviced via their object paths
*/
::method publishAllServiceObjects class      -- expects a DBus (connection) object, publishes all defined object paths
  use strict arg conn

  .ArgUtil~validateClass("conn", conn, .DBus) -- check for correct type
  a=.array~new
  s=conn~serviceObject("GetRegisteredServiceObjects")~supplier
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


/**  Constructor method, initializing attributes.
*
* @param objectPath the object path, can also be a collection of paths, if the service object serves more than one
*/
::method init
  expose paths
  use arg objectPath          -- can also be a collection of paths

-- say "..." self"::init, objectPath="pp(objectPath)

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

-- call dump2 paths, self": 'paths'-relation after set-up for" pp(objPath)

  return

/** Create introspect data on the fly if the supplied object path is known, supply segments of the
*   object path as nodes, such that the client can use that to further introspect all of the service object's
*   object paths.
*
*  @param slotDir a Rexx directory containing all information of the DBus message directed at the DBus service object
*  @return introspection data containing all segments to all objects as nodes
*/
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


/** Allow access to the relation with object path fragments/segments pointing to next local fragment/segment. */
::attribute paths get      -- allow access to the relation with object path fragments pointing to next local fragment






-------------------------------------------------------------------------------------------------------------------------------
/** This class allows for collecting messages to be executed in the same thread. The
*   event loop needs to invoke the <code>executeQueuedMessages</code>.
*   To add a message to the message queue use the method <code>postMessage</code>. To
*   abort all queued messages use the method <code>abortQueuedMessages</code>.
*   This class can be inherited by any other class, because its baseclass is the ooRexx
*   root class <code>Object</code>.
*/

::class Worker mixinclass object

/** Constructor method, initializing the <code>queuedMessages</code> attribute. */
::method init
  expose queuedMessages
  queuedMessages=.queue~new
  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "WORKER"
  forward class (super)


/** This method sends all the messages in the message queue and sets information on the
*   WorkerMethod object to ease further processing. This method needs to be invoked from the
*   thread that established the dbus connection in order for libdbus to work correctly.
*/
::method executeQueuedMessages
  expose queuedMessages
-- Uncommented as this method gets constantly invoked by the native message loop yielding too many debug outputs
--  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "queuedMessages:" pp(queuedMessages~items)

  signal on any   -- trap any error condition to allow us to re-raise any of the trappable conditions in the blocked thread later

  do while queuedMessages~items>0
     workerMessage=queuedMessages~pull    -- pull workerMessage object
     if workerMessage~cancel=.false then  -- if not cancelled, send contained message
     do
        workerMessage~message~send  -- send contained ooRexx message
        workerMessage~hasResult=var("RESULT")   -- indicate whether message yielded a result
        workerMessage~isDone=.true  -- allow to unblock wait on this workerMessage
     end
  end
  return

any:
-- say "... executeQueuedMessages, ANY condition:" condition("C")
  workerMessage~hasError=.true         -- indicate an error, unblock waitForDone
if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "queuedMessages:" pp(queuedMessages~items) "SIGNAL ANY received, flagging workerMessage as error in execution..."


/** This method aborts all queued messages, setting the attribute <code>isAborted</code>
*   to <code>.true</code>. This method can be invoked from any thread.
*/
::method abortQueuedMessages
  expose queuedMessages
  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  do while queuedMessages~items>0
     workerMessage=queuedMessages~pull    -- pull workerMessage object
      -- if not done or cancelled, abort message
     if workerMessage~isDone=.false, workerMessage~cancel=.false then
        workerMessage~isAborted=.true
  end
  return



-- 2014-07-29, rgf: now added re-raising condition on blocked thread, if necessary
/** This method expects an ooRexx message object (cf. the ooRexx class <code>Message</code>)
*   which gets wrapped into a <code>WorkerMessage</code> object and then queued to the message queue
*   for later execution in the method <code>executeQueuedMessages</code>. This method can
*   be invoked from any thread. This is an unguarded method to not block concurrent invocation
*   of the methods <code>executeQueuedMessages</code> and <code>abortQueuedMessages</code> due
*   to this method being blocked on a <code>workerMessage</code> object. This method can be
*   invoked from any thread.
*
*   <p><em>Hint:</em> If an error condition is raised while the method is executing on another thread, upon return
*      this method will re-raise that very error condition, supplying the original condition object
*      as the last item in the <code>ADDITIONAL</code> array created for the re-raise error condition
*      to allow for further inspection, if necessary.
*
*  @param  message an instance of the ooRexx class <code>Message</code> that should be sent
*          in the dbus connection thread
*
*  @return if the message yielded a result it will be returned, otherwise no value will be returned
*/
::method postMessage unguarded
  expose queuedMessages
  use strict arg message
  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line)

  .ArgUtil~validateClass("message", message, .Message)  -- check for correct type
  workMsg=.WorkerMessage~new(message)
  guard on
  queuedMessages~queue(workMsg)-- queue message
  guard off

  workMsg~waitForDone      -- method will check for abort or error condition

   -- if no error and a result returned
  if message~hasError=.false then
  do
     if workMsg~hasResult then return message~result     -- access and return result
     return    -- message did not return a result
  end

   -- while executing the method an error condition was raised, re-raise it on this thread as well
      -- supply original error condition object for further inspection in addtional array
  co=message~errorCondition   -- fetch error condition
  condition=co~condition      -- fetch condition name for select
  if co~hasentry("ADDITIONAL") then addtl=co~additional
                               else addtl=.array~new
  addtl~append(co)            -- append condition object to additional array to allow it to be inspected

  descr=co~description        -- can be empty
  if descr<>"" then descr=descr "- "
  descr=co~description || "Hint: added original condition object as last item in ADDITIONAL array to allow for inspecting it"

signal on any
  select
     when condition="SYNTAX"        then
        raise syntax (co~code) additional (addtl) description (descr)

     when condition="ERROR"         then
        raise error (co~rc)    additional (addtl) description (descr)

     when condition="FAILURE"       then
        raise failure (co~rc)  additional (addtl) description (descr)

     when condition="HALT"          then
        raise halt             additional (addtl) description (descr)

     when condition="LOSTDIGITS"    then
        raise lostdigits       additional (addtl) description (descr)

     when condition="NOMETHOD"      then
        raise nomethod         additional (addtl) description (descr)

     when condition="NOSTRING"      then
        raise nostring         additional (addtl) description (descr)

     when condition="NOTREADY"      then
        raise notready         additional (addtl) description (descr)

     when condition="NOVALUE"       then
        raise novalue          additional (addtl) description (descr)

     when condition~word(1)="USER"  then     -- as of ooRexx 4.2.0 "usercondition" cannot be supplied in an expression :(
     do
         -- as of 4.2.0 we cannot raise a user condition directly, hence doing it via an interpret keyword statement instead
        stmt="raise" condition('c') "additional (arr) description (descr)"
        interpret stmt

        -- descr=condition "-" descr
        -- raise user surrogate_from_postMessage_method_see_description_in_condition_object additional (addtl) description (descr)
     end

     otherwise
     do
        arr=.array~of("No raise instruction for condition ["condition"], hence landed in otherwise branch giving this lousy error message!")
        arr~appendAll(addtl)
        raise syntax 98.900    additional (arr) description (descr)
     end
  end

any:
   raise propagate

-------------------------------------------------------------------------------------------------------------------------------
/** This class is needed by the <code>Worker</code> class and wraps up an ooRexx message
*   object that will be sent in the dbus connection thread. It adds attributes to communicate
*   various type of information about the embedded ooRexx message object.
*/
::class "WorkerMessage"    -- use terminology of Java's java.util.concurrent.Future
/** Constructor method, sets the attributes <code>cancel</code>, <code>isAborted</code>,
*  <code>isDone</code> and <code>hasResult</code> to <code>.false</code>.
*
*  @param  message an instance of the ooRexx class <code>Message</code>
*
*/
::method init
  expose cancel hasResult isAborted isDone message hasError
  use strict arg message
  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "WORKERMESSAGE"
  .ArgUtil~validateClass("message", message, .Message) -- check for correct type
  cancel=.false            -- allow to cancel sending/processing message
  isAborted=.false         -- message was aborted
  isDone=.false            -- message was not processed so far
  hasResult=.false         -- message yielded a result?
  hasError=.false          -- message caused an error condition

/** Attribute: <code>.false</code> if message was not cancelled, <code>.true</code> if message got cancelled. */
::attribute cancel

/** Attribute: <code>.true</code>, if message yielded an error condition, <code>.false</code> else. Needed to
*              allow a GUARD ON this attribute as well (@see method <code>waitForDone</code>.
*/
::attribute hasError       -- need to define in order to be able to guard on it

/** Attribute: <code>.true</code>, if message yielded a result available in the ooRexx message object.
    As of ooRexx 4.2 (2014) accessing the <code>result</code> attribute in the ooRexx message object when
    no result is available causes an ooRexx error condition to be raised. */
::attribute hasResult

/** Attribute: <code>.true</code> if <code>sendQueuedMessages</code> got aborted and this message
    was therefore not sent anymore. */
::attribute isAborted

/** Attribute: <code>.false</code> if message was not sent yet, <code>.true</code> if message got processed. */
::attribute isDone

/** Attribute: embedded ooRexx message object. */
::attribute message

/** Method that blocks until one of the <code>WorkerMessage</code> attributes <code>isDone</code>,
*   <code>cancel</code>, <code>isAborted</code> changes its value to <code>.true</code>. If the
*   message got aborted or the ooRexx message object indicates a condition object, an error condition
*   gets raised. If a condition object was returned with the ooRexx message it gets supplied as
*   an additional argument to the raised error condition.
*/
::method waitForDone unguarded
  expose cancel isAborted isDone hasError message
  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "before blocking" pp(self~identityHash)

  guard on when isDone=.true | cancel=.true | isAborted=.true | hasError=.true
  if .dbus.dir~bDebug=.true then say pp(DBusGetTID()) pp(.dateTime~new) pp(self) pp(.context~executable) pp(.context~name) "line:" pp(.line) "after blocking " pp(self~identityHash)

  signal on syntax
  if isAborted=.true then
     Raise syntax 98.900 array ("Message got aborted while still queued for execution.")
  return
syntax:
  raise propagate


/** Poor man's pretty pring, used in many code snippets.
*   @param o object
*   @param returns  <code>o</code>'s string value enclosed in square brackets without interleaving blanks
*/
::routine pp public
  parse arg o
  return "["o"]"
