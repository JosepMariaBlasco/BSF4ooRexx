/*

remarks:

    - RexxCondition() is not documented, but is shown in "rexxpg.pdf"
    - there is no CallProgramFromData()! Instead use (RMG): NewRoutine()+CallRoutine()
    - RFE: function to list loaded libraries and packages; function to list all/specific global/public
      routines and public classes
    - using NULL instead of NULLOBJECT, as NULL more clearly indicates that nothing got returend
      (NULLOBJECT could be mistaken to mean .NIL)
    - native APIs "Condition*()", aber "RaiseException()", inkonsistent; macht's wieder ein Stückchen
      schwieriger zu verstehen! :(
      [Begrifflichkeiten inkonsistent]

    - 2010-02-10: no means to test whether a Rexx interpreter instance is still available for
                  executing Rexx code (e.g. in callbacks from Java to Rexx)

   last change: $Revision: 888 $ $Author: Administrator $ $Date: 2010-08-16 16:02:21 +0200 (Mon, 16. Aug 2010) $


  * This DLL registers implements the Java-ooRexx-bridge in native code, using the ASF (Apache Software Foundation)
  * BSF 2.4 (Bean Scripting Framework)
  * it allows Java to call into Rexx using the new ooRexx 4.0 native APIs using JNI ("Java Native Interface").
  *
  * last change: $Revision: 888 $ $Author: Administrator $ $Date: 2010-08-16 16:02:21 +0200 (Mon, 16. Aug 2010) $

    4.5.2.  2015-07-21 - RgfProcessJArgs(): now expects a new (trailing) argument that determines, whether
                         Java object references in the BSF registry should be increased when wrapped as a
                         BSF reference object;

                         reason: jniRexxRunProgram() and jniRexxSendMessageToRexxObject() in RexxEngine.java
                         will register the Java objects before invoking these functions via RexxAndJava.java,
                         and unregister them upon return; if a BSF reference object survives the invocation it
                         will not represent the Java object thereafter anymore, hence the need to increase
                         the BSF registry reference counter in these use cases

    4.5.2.  2015-07-20 - BsfRexxProxy(): give more detailed condition information to ease debuggin

    4.5.2.  2015-06-08 - report new external Rexx function BsfContextVariables() in BsfQueryRegisteredFunctions()

    4.5.2.  2015-06-04 - 2015-06-07 -rgf: added external Rexx function BsfContextVariables() which
                           allows to get, set and drop context variables in caller, usage

                           BsfContextVariables( [get [, name]] | set, nameOrDir[, value]] | drop, nameOrDir[, value]])
                                 ... optional, return directory of all context variables

                           "g"   ... "get", return value of context variable "name";
                                     if optional "name" is omitted, return directory of all context variables

                           "s"   ... "set", set context variable
                                            "nameOrDir" ... if string, then "newValue" must be given
                                                            else must be a .Directory or .StringTable (anticipating ooRexx 5.0)

                           "d"   ... "drop", drop context variable
                                            "nameOrDir" ... if string, then drop that variable
                                                            else must be a .Directory or .StringTable (anticipating ooRexx 5.0)

    4.5.1   2015-05-09, - rgf: added JNI methods that allow a RexxEngine.java instance to get information aobut it
                               - _RexxAndJava_jniGetGlobalEnvironment0 ... .environment
                               - _RexxAndJava_jniGetLocalEnvironment0  ... .local
                               - _RexxAndJava_jniNil0                  ... .nil
                               - _RexxAndJava_jniInterpreterVersion0   ...  least signifant three bytes encode this information
                               - _RexxAndJava_jniLanguageVersion0      ...  least signifant two bytes encode this information

    4.5.0   2014-10-20, - rgf: updated package information in RexxPackageEntry structure
    4.5.0   2014-05-17, - simplified attaching/detaching to the JVM, a side effect will be a slight performance
                          improvement (appr. 5%)
                        - replace deprecated BSFAttachToTID() and BSFDetach() with body that always returns true

    4.5.0   2014-05-15, - do not report deprecated BSFAttachToTID() and BSFDetach() anymore

    4.5.0   2014-04-03, - make sure that BSFDetach() does not detach from the primodal thread

    4.5.0   2014-03-31 ---rgf
                        - rewrite all "const char*" types with "char*" types as the latest Clang
                          on MacOSX creates bus errors, if forcing "char*" on "const char*" (which is fine);
                          replacing:
                              char bla[nnn]="";
                          with
                                 char *bla=new char[nnn];
                              and
                                 delete [] bla;

    4.5.0   2014-03-30 ---rgf, major addition, hence augmenting version to 4.5 !
                         - save primodal TID as BSF.PRIMODALTID for each Rexx interpreter instance;
                           this allows to do a BSFAttachToTID() without supplying a TID, added code
                           to BSFLoadJava(...), jniCreateRexxInterpreterInstance(...)

                         - now possible to do an "auto-attach" to the Java interface taking advantage
                           of this new information, implemented for the RexxRoutines (sending messages
                           from Java to Rexx will always occur on an established Java connection):

                           - BSFAttachTo(): argument now optional, in order to remain backwardly
                                  compatible, if a programmer uses this routine for an already
                                  attached ooRexx thread, the same condition will be raised such
                                  that Rexx code expecting this behaviour keeps on working as designed

                           - BSFCreateRexxProxy()
                           - BSFJavaException()
                           - BSFRawBytes()
                           - BSF()

    4.2.0   2014-03-29 ---rgf,
                       - add jniProcEnvironment(...): this allows to get, set and unset the
                         process environment from Java; reason: AOO 4.x removes the PATH environment
                         variable (seems to be unintended, hence an error), which inhibits the
                         Rexx interpreter to initialize successfully; this jni function is intended
                         to allow as a last resort to set the missing PATH environment variable;
                         the subfunctions "getEnv" and "setEnv" will not be explicitly documented

    4.1.2   2013-10-07 - removing DeleteLocalReference(...) from:

                            - Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxRunProgram(...)
                            - Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxSendMessageToRexxObject(...)

                         otherwise rtc->DetachThread() may cause an exception in native code,
                         cf. <http://sourceforge.net/p/bsf4oorexx/bugs/19/>

    4.1.1   2013-06-30 - removed last compiler warning (-Wwrite-strings)

    4.1.1   2013-06-29 - applying ReleaseLocalReference() wherever a Rexx thread context may have returned
                         a RexxObjectPtr for which the thread context creates a local reference to protect
                         it to be garbage collected; a ReleaseLocalReference() therefore has to be issued to
                         inhibit memory leaks

                       - removed compiler warnings

    4.1.1   2013-06-27 - slight tidying up (removed an obsolete function)

    4.1.1   2013-06-10 - changed logic in Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxTerminateInterpreterInstance
                         to really terminate Rexx interpreter instances (RII) with the exception of the
                         primodal RII, which needs to stay alive due to using it for maintaining RexxProxy
                         objects with Rexx collection objects in native code

    4.1.-20 2012-12-30 - serializing initializations of JVM-dependent structures to fix bug #
                         <https://sourceforge.net/tracker/?func=detail&atid=1660873&aid=3591015&group_id=400586>

    4.1.-19 2012-06-07 - changed logic in RgfAddRexxProxy() such that if the package object is NULL to
                         not add it to the directory (it won't be removable in ooRexx 4.1.1)

    4.1.-18 2012-06-06 - added an external Rexx function named BsfGetRegistryObjects() for debugging
                         purposes only; to compile the flag CONFIG_MAKE_OREXX_REGISTRY_ACCESSIBLE must be defined

    4.1.-17 2012-05-26 - fixed errors because of wrongly placed DeleteLocalRef() (and in one instance
                         a GlobalRef'd Java object became illegal due to excercising DeleteLocalRef()
                         on it

    4.1.-16 2012-05-24 - changed "JNU_GetStringNativeCharsReturningRexxObjectPtr" to
                                 "JNU_GetStringNativeCharsReturningRexxStringObject"
                       - fixed OOM (out of memory) error reported by Erik Dujis by explicitly
                         invoking DeleteLocalRef() on any jobject; bug report:
                         <https://sourceforge.net/tracker/?func=detail&atid=1660873&aid=3525814&group_id=400586>
                       - tidying up a little bit by deleting some outdated commented code

    4.1.-15 2012-04-19 - fixed a bug in free()ing options array in BsfLoadJava()-routine, reported
                         by JLF, cf. <https://sourceforge.net/mailarchive/forum.php?thread_name=4F900B1A.8000104%40wu-wien.ac.at&forum_name=bsf4oorexx-devel>

    4.1.-14 2012-03-18 - fixed a bug in processing the return value for Rexx exits RXEXF and RXOFNC
                       - corrected the needed size of the parmBlock array for the RXHLT and RXVALUE exits

    4.1.-13 2012-03-11 - added support for NetRexx Rexx' class for return values from handlers
                         (if NetRexx Rexx' value, return its string value to Rexx)

    4.1.-12 2012-02-25 - added native functions jniNil(), jniInterpreterVersion(), jniLanguageLevel()
                       - RgfRexxObject2JavaObject(): now wraps up .nil as an own RexxProxy! This
                         will allow Rexx handlers to also distinguish between "nothing returned" (NULL)
                         and returning .nil

    4.1.-11 2012-02-24 - added native functions jniGetLocalEnvironment(), jniGetGlobalEnvironment()

    4.1.-10 2012-02-19 - added native function _jniGetCallerContext()
                       - RgfCreateRexxlikeErrorInfo(): set informative Java message, if no traceback line
                         available (because a result of a Java call into Rexx)

    4.1.-9 2012-02-18 - added inline function RgfRexxObject2JavaObject() to have a single spot
                        where the basic conversion occurs (became necessary because of new execution
                        paths causing "copy-duplication" of code)
                      - renamed RgfProcessJArg1() to RgfJavaObject2RexxObject()

    4.1.-8 2012-02-17 - adapted to exploit command and exit handlers that implement the *interfaces*
                        RexxExitHandler.java and RexxCommandHandler.java (they used to be defined
                        in an abstract fashion); much easier to implement for the Java programmers,
                        may incur a small overhead as now getting the class object and the
                        jmid-lookup needs to be done on each exit invocation; implemented with the
                        CONFIG_REXX_HANDLERS_AS_INTERFACES directive

    4.1.-7 2012-02-12 - created the JNI-function _jniDropContextVariable() for Java handlers to be able to use them
                      - added code for processing RXFNC and RXSIO-exit


    4.1.-6 2012-02-11 - created the following JNI-functions for Java handlers to be able to use them:
                        _jniCheckCondition(), _jniClearCondition(), _jniGetConditionInfo(),
                        _jniSetThreadTrace(), _jniHaltThread()

    4.1.-5 2012-02-09 - added new case to RgfProcessJArgs() as now the implementors of Java exit and
                        command handlers might return/send a non-string Java object to Rexx (implementation
                        taken from RgfProcessJArgs(), causing also a need to fetach the rajo object from
                        the expanded RII_struct

    4.1.-4 2012-02-08 - honoring Java options, create Rexx interpreter with them
                      - finished rexx_command_handler_entry()
                      - created the following JNI-functions for Java handlers to be able to use them:
                        _jniRaiseCondition(), _jniRaiseException(), _jniRaiseException0(),
                        _jniRaiseException1(), _jniRaiseException2(), _jniGetContextVariable(),
                        _jniSetContextVariable()

    4.1.-3 2012-02-07 - added access to RexxConfiguration (getExitHandler(),getCommandHandler()),
                        RexxExitHandler (handleExit()) and RexxCommandHandler (handleCommand())
                      - added rexx_command_handler_entry(), rexx_exit_handler_entry()
                      - added RgfProcessJArg1() which processes single Java objects for passing
                        them on to Rexx

    4.1.-2 2012-02-06 - corrected some misleading (leftover from previous versions) comments
                      - changed signature of _jniRexxCreateInterpreterInstance to cater for new joptions argument

    4.1.-1 2012-02-04 - start of reworking native code to allow for starting Rexx interpreter instances
                        with the options: INITIAL_ADDRESS_ENVIRONMENT, EXTERNAL_CALL_PATH,
                        EXTERNAL_CALL_EXTENSIONS, LOAD_REQUIRED_LIBRARY, DIRECT_EXITS,
                        DIRECT_ENVIRONMENTS

    // -------------------------------------------------------- before 4.1.0 (started February 2012)

    4.0.8 2011-06-05 - rgf, make Java Throwable object available via ADDITIONAL array if doing a RaiseException()

    4.0.7 2011-03-19 - fix Apple compiler problem: make environment{AttachTo|DetachFrom} a true function (inline
                       causes global structure's elements being NULL; this happens with "i686-apple-darwin10-gcc-4.2.1 (GCC) 4.2.1 (Apple Inc. build 5664)")
                     - RgfCreateRexxlikeErrorInfo(): check whether RexxCondition RexxStringObject are NULL or Nil

	4.0.6 2011-01-12 - free VMArgs options (string and options array)
		                 - memset malloc'ated VMArgs option memory to 0x00

        4.0.6 2011-02-09 - forcing ooRexx minimum level 4.1.0
        4.0.6 2011-02-07 - fixing error message to reflect platforms if [lib]jvm.{dll|so|dylib} not found
        4.0.6 2011-01-09 - fixing MacOSX Java pecularity, using ["JAVA_STARTED_ON_FIRST_THREAD_%d", getpid()] as
                           per <http://lists.apple.com/archives/java-dev/2009/Oct/msg00511.html>

    	4.0.6 2011-01-02/07 - fixing MacOSX g++ warnings and errors, allow loading JVM via Rexx as well on MacOSX

        4.0.5 2010-08-14 - new stratety on Unix to load 'libjvm.so': first try '/opt/BSF4ooRexx/libjvm.so',
                           if not successful, then look for 'libjvm.so' and let the system find it, if setup

        4.0.4 2010-05-27 - added optional second "lengthToUse" argument to BsfRawBytes(); "lenghToUse"
                           determines the number of bytes to use for the conversion

        4.0.3 2010-05-22 - fixed potential errors in assuming that a Rexx string object is indeed
                           a RexxStringObject in the native layer; could be an internal, optimized
                           Integer object!
                           Cf. <https://sourceforge.net/tracker/?func=detail&atid=684730&aid=3005462&group_id=119701>

        4.0.2 2010-05-20 - added new JNU_GetStringNativeCharsReturningRexxObjectPtr(...) macro which
                           returns a RexxObjectPtr (RexxString) that may contain embedded NUL chars
                         - return values/arguments from the Java side now process strings that have
                           NUL chars embedded;
                         - hint: if in need of supplying a Rexx string with embedded NUL chars to Java use:
                                 b=BsfRawBytes(RexxString)  -- turn Rexx string into Java byte array
                                 s=.java.lang.String~new(b) -- use Java byte array to construct Java String

        4.0.1 2010-03-14 - applied Jean-Louis Faucher's fixes for debug-mode (e-mail 2010-03-14):
                                "I got a crash with the debug version of bsf4oorex
                                (bsf4oorexx-20091031-beta2_sources) It's because of the memory pattern
                                stored by the heap manager when in debug version : 0xfeeefeee The next
                                node must be read before the remove (I see you fixed already the code, but
                                not everywhere).  Search for JLF in the attached file, you will see where
                                I changed the code."
                         - added ".rxo" (for OpenOffice scripts); if Rexx is started via Java it will now
                           look for the additional Rexx extensions: ".rxj,.rxo,.rxjo,.jrexx"

        4.0.1 2010-02-10 - added BsfRawBytes(rexxString|jbyteArray) to allow direct exchange of
                           bytes between Rexx and Java without any code page translations that
                           may take place when sending a Rexx string to Java

        4.0.0 2009-10-19 - renaming from "BSF4Rexx" to "BSF4ooRexx"

        4.0.0 2009-10-18 - if Rexx is started via Java it will now look for the Rexx extensions:
                           ".rxj,.rxjo,.jrexx"
                         - added "BsfJavaException(option[, arg)" to allow throwing Java exceptions (needed
                           e.g. for some OpenOffice vetoing patterns)

        4.0.0 2009-10-11 - if Rexx is started via Java it will also look for the Rexx extensions ".jrexx,.jrexxo"

        4.0.0 2009-10-06 - 10: - using and testing the new thread infrastructure functions

        4.0.0 2009-10-03: - finished creating new thread infrastructure functions;

        4.0.0 2009-10-01: - renamed RgfGetJRST_for_new_jniRexxStart(...) to RgfGetJRST_or_create_new
                          - changed multithreading strategy: if a new program/routine or SendMessage, then
                            force creation of a new JRST; if for a TID a JRST exists already, then ...
                          - added RgfRemoveJRST_By_RIID(void *rii);

        4.0.0 2009-09-25: - added BsfGetRIID(), returning the Rexx instance pointer as a string (using %p)
                          - using snprintf(...) [C99-compilers] or sprintf_s(...) [MSC-compiler] instead
                            of sprintf(...) to make the program fully buffer overrun safe in those corners;
                            also replaced strcat(...) with SNPRINTF(...)

        4.0.0 2009-09-12: - list "BsfCreateRexxProxy" and "BsfRexxProxy" in "BsfQuery{All|Registered}Functions"
                          - made sure no buffer-overruns in sprintf()

        4.0.0 2009-09-10: - version string now contains the address mode, added as a third word in the form
                            of bits"-bit", where "bits" is "32" or "64"

        4.0.0 2009-09-06: - added new external function: "BsfRexxProxy(proxy [, 'ooRexxObject' | 'userData' | 'refCount'])"
                          - added new internal inline function:  "RgfGetProxyObjectRefCount(RexxThreadContext, RexxStringObject)"

        4.0.0 2009-09-03: - adding "RgfRemoveAllProxyObjectsByRIID[2]()" to allow freeing cached Rexx proxy
                            objects, if BSF4Rexx got invoked via Java and a terminate() is executed on the
                            RexxEngine instance
                          - _jniRexxTerminateInterpreterInstance(): will invoke "RgfRemvoeAllProxyObjectsByRIID(...)",
                            but only, if BSF4Rexx was invoked by Java!
                          - _jniRexxUnregisterRexxObject(): will not free cached Rexx objects, if BSF4Rexx
                            was invoked by Rexx; this will avoid the potential (intermittent) occurrence of
                            exceptions while running Java object finalizers, after the Rexx program has
                            terminated and the Rexx interpreter removes the infrastructure without notice

        4.0.0 2009-08-27: - adding a j_resultType argument to  _jniRexxSendMessageToRexxObject
                          - _jniRexxSendMessageToRexxObject(): now marshalling return value to given (primitive) return type

        4.0.0 2009-08-20: - edit to remove gcc 64-Bit compiler warnings on Linux

        4.0.0 2009-07-11: - added ability to retrieve and pass on "javaObject" to
                            _jniRexxSendMessageToRexxObject

        4.0.0 2009-07-10: - added ability to retrieve and pass on "messageDescription" in
                            internal Java form to _jniRexxSendMessageToRexxObject

        4.0.0 2009-06-27: - changed error message format to:

                            fileName '/' { 'routine' | 'class' clzName } '/' { routineName | ['class'] 'method' methodName} ',' 'error' nr ':' errorMessage

                           - jniRexxSendMessage: if Java method object is supplied, store it under
                             'METHODOBJECT' in the callback directory

        4.0.0 2009-06-23: - removed argument "creationTID" (from RexxProxy.java, not needed anymore)
                          - removed external function "BsfProcessCallbackArgs()", it is not really needed
                            and therefore just clutters the collection of external functions

        4.0.0 2009-06-22: - .._jniSendMessageToRexxObject(): now there will be always an argument added as the
                            last argument which contains a Rexx directory  with the entries "METHODNAME" and
                            "CREATIONTID"; if a user_data object was given as the second argument in
                            BsfCreateRexxProxy(), then that object will be in the added callback argument
                            under the name "USERDATA"

        4.0.0 2009-06-08:
                        - removed "Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxStart()"
                        - removed RXSIOSAY exit, as with the 4.0 APIs it has become possible to get at the
                          Rexx error message via the API, hence no such hack necessary anymore

                        - added "Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxCreateInterpreterInstance"
                        - added "Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetRexxInterpreterInstanceRoot"
                        - added "Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxHaltInterpreterInstance"
                        - added "Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxTerminateInterpreterInstance"
                        - added "Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniUnregisterRexxObject"
                        - added "Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxRunProgram"
                        - added "Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxSendMessageToRexxObject"

                        - added external Rexx function "BsfCreateRexxProxy()": creates a Java proxy for an
                          ooRexx object, which allows sending or forwarding messages from Java to ooRexx; syntax:

                                BsfCreateRexxProxy(code|object [, [userData] [, JavaInterface[name]...]), where

                                code|object ... Rexx code to execute each time a Java message is sent (code will
                                                be used for the UNKNOWN method); or a Rexx object to which the
                                                Java message gets sent to
                                userData ... any Rexx object (e.g. a directory); if given, then this will cause
                                             an additional directory object to be created which will contain an
                                             entry "USERDATA" referring to this user supplied object (this callback
                                             argument will always be the last argument and contains in addition
                                             the entires "METHODNAME", which preserves the exact case of the Java
                                             method name, and "CREATIONID", the TID in which the proxy got created);
                                             if userData is omitted, no such callback argument will be created
                                JavaInterfaceName ... one or more, comma-separated, optional Java interface names
                                             or Java interface class objects, or Java objects which classes implement
                                             Java interfaces (then those interfaces are used as well), which
                                             will cause an instance of java.lang.reflect.Proxy to be created;
                                             if these arguments are given, then the Java Proxy object is returned
                                             instead, which will forward all Java method invocations to the RexxProxy
                                             object;
                                             if this argument is not given, then the RexxProxy object is returned;
                                             that RexxProxy possesses a public method "newJavaProxyInstance(Object[] interfaces)"
                                             which can be invoked from Rexx as well; the Java Object[] array
                                             elements may in this case be the same as the "JavaInterfaceName" above

                        - added external Rexx function "BsfProcessCallbackArgs()": accepts either an argument array
                                (e.g. Arg(1,"A)) or directly the callback directory and sets its elements in the
                                caller with the stem defined by REXX_CALLBACK_ARG_STEM_NAME ("BSFARG."); at the
                                time of this writing the defined entries are: "USERDATA", "METHODNAME", "CREATIONTID";
                                returns .true, if callback directory could be found and processed, .false else



        3.9.1 2009-04-23: - started to create an ooRexx 4.x specific version of this library,
                            in order to allow for allowing Java proxies to callback into ooRexx
                            by addressing matching Rexx proxy objects;
                          - started to change all external Rexx function to the new ooRexx 4.0 APIs

        ///////////////////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
        \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ ////////////////////////////////////////////////

        2.8.2 2009-04-20: - added definitions to allow this program to be also compiled under
                            ooRexx 4.0; kudos go to Jean-Louis Faucher <jfaucher@csc.com> who
                            tracked down the needed changes and documented it in the patch
                            files at <https://sourceforge.net/tracker/?func=detail&aid=2486432&group_id=68188&atid=520353>

                            This way this file can be compiled on all versions of ooRexx

        2.8.1 2009-04-02: - remove deregistering of the RXSIO exit, such that multiple nested
                            invocations via Java does not cause failures in the exit processing

                          - added ability for Java to query the thread ID such that
                            Java is able to indicate a thread halt supplying the BsfGetTID()
                            value

        2.8.0 2008-09-03: - if nested invocation (invoking a new Rexx interpreter in the same
                            thread where an instance of a Rexx interpreter is already running)
                            of the Rexx interpreter, do not wait for termination in the nested
                            invocation (ooRexx 3.2.0 waits forever)

               2008-09-01: - removed superfluous variables in BsfUnloadJava()

         2.7.9 2008-08-20: - added RexxDidRexxTerminate() and RexxWaitForTermination()
                             to jni...RexxStart(), such that concurrently running ooRexx
                             threads can continue to run
                           - RXSIO buffer is now not globally allocated/freed

         2.7.8 2008-08-14: - added ability to nest jniRexxStart()-calls with the restriction
                             that only one ooRexx thread may be using the nested one, otherwise
                             a Java exception will be raised

         2.7.7 2008-08-10: - fixed error (prematurely freeing rsb.-buffer)
                           - cleaned up a little bit (removing most of the obsolete code)

  *      2.7.6 2008-08-06: - RgfFreeMemory() of RXSIO buffer upon successful deregistration
  *
  *      2.7.5 2008-07-23 bis 2008-08-06:
  *

  *      -- major revamp of JNI management (new structs, new data structures,
  *         new inline functions, new flow of control) for calling back into Java

         -- now possible to call into Java from different ooRexx threads, giving, that
            they attach to the ooRexx thread that established the BSF4Rexx bridge
  *
  *     2.7.4 2008-07-13 bis 2008-07-14:
  *     --- CLEANUP: getting rid of JNI 1.1 & OS/2 support
  *                      - BSFUnloadJava: now uses JNI to unload Java, oldGlobal-vars removed
  *                      - changed to C++ syntax "(env*)." to "env->"
  *                      - using ExceptionCheck() in lieu of ExceptionOccurred() to test for pending exceptions
  *                      - activating (and debugging) DetachThread()
  *
  *
  *     2.7.3 2008-07-01, rgf, experimental: using Throwable.toString() instead of
  *                       Throwable.getMessage() to get not only the message, but also
                          the type of Exception
  *
        2.7.2 2008-06-14, rgf, honoring source-location argument for script from Java
  *           2008-06-09, rgf, found an argument sequence error in commented code (rgfDetachCurrentThread())

  *     2.7.1 2007-09-26, rgf, corrected tests for DEBUG level, such that compilation
  *                       is possible with all DEBUG levels (DEBUG, DEBUG1, DEBUG2,
  *                       DEBUG3, DEBUG_THREADS); pointed out by Rene Jansen
  *
  *     2.7.0 2006-11-19, rgf, added function BsfDoNotShowErrorString()
  *
  *     2.6.0 2006-02-xx, removed all JNI 1.1 code
  *
  *     2.5.9 2006-01-28, changed logic for RXSIO exit to allow compiling under Linux [of min() function
  *                       not available on gcc], also inserted an elipsis (...) if truncating a Rexx
  *                       stderr string to fit into 512 bytes per line);
  *                       fixed conversion omission: now converts JavaString in Java-passed-in arguments
  *                       to a native string (byte array translated according to local codepage) for the
  *                       Rexx arguments
  *
  *     2.5.8 2006-01-27, created a RXSIO exit, which memorizes stderr-outputs from Rexx; in case of
  *                       an execution error, the last three lines (containing the Rexx error message)
  *                       are used to create a Java exception (org.apache.bsf.BSFException)
  *
  *     2.5.7 2006-01-22, BSF() now returns the result translated to a native string using the current
  *                       codepage
  *
  *     2.5.6 2006-01-06, if Rexx arguments do not set java.class.path and the CLASSPATH environment
  *                       variable is set, then use that value for starting up the JVM
  *
  *     2.5.6 2006-01-04, changed loading JVM, with JNI >= 1_2 now all Rexx arguments are passed through,
  *                       started to enclose JNI_1_1 code in "#ifdef SUPPORT_JNI_1_1" to eventually
  *                       get rid of it
  *
  *     2.5.4 2005-08-30, changed license to Apache v2.0, CPLv1.0;
  *                       adjusted version number to match the Java support program ones
  *
  *     2.1.3 2005-06-02, moved RexxEngine to 'org.rexxla.bsf.engines', code adopted,
  *                       removed dependency on "com.ibm.bsf" and "org.apache.bsf": will be addressed by the Rexx engine
  *
  *     2.1.2 2003-08-14, Linux-version works (with Java 1.4); needed to
  *                       remove DetachCurrentThread() to prohibit exceptions
  *                       in Sun's native "java" code; also removes occassional traps
  *                       on Windows for Java 1.3 and 1.4
  *
  *     2.1.1 2003-08-11, started to adapt to Linux and OS/2 (eComStation)
  *
  *     2.1.1 2003-08-09, started to add ability to use this DLL for JNI 1.1 (Java 1.1) *and* 1.2 (Java 1.2, 1.3, 1.4)
  *                       (some remarks: on Java 1.1 - global references in Hashtable will return *local* references;
  *                       on Java 1.2 - UTF-strings need to be pinned down with global references;
  *                       on Java 1.2, 1.3, 1.4 - sometimes JNI looses thread-infox it seems; "healed"
  *                       by attaching again, although env was received by JNI!)
  *
  *     2.1.0 2003-08-08, takes care of threaded Object Rexx programs calling
  *                       back at the same time, but potentially using different physical
  *                       threads (under control of the Object Rexx thread-dispatcher)
  *
  *     2.0.0 2003-04-30, as BSFManager caches the RexxEngine it is important to make
  *                       sure that the BSF-functions are registered before the Rexx
  *                       script gets executed by the Rexx interpreter, ---rgf
  *                       now lists the BSFFunctions in alphabetical order
  *
  *     2.0.0 2003-04-16, final version to be introduced at the 2003 International Rexx Symposium
  *                       caters for IBM (BSF 2.2) and Apache (BSF 2.3 and higher)
  *
  *     1.9.4 2003-04-14, adjusted Rexx memory to what it is supposed to be, ie.:
  *                       - never release memory allocated by Rexx
  *                       - if allocating memory, then freeing it is o.k.
  *                       - Rexx creates RXSTRINGS with a buffer size of 256L (RXAUTOBUFLEN)
  *                         if size is not sufficient, create a larger buffer, but do
  *                         NOT free Rexx'
  *
  *     1.9.3 2003-04-05, adjusted for Linux (RH 7.2)
  *     1.9.2 2003-02-19, switched arguments for BsfLoadJava, now arg(1)=classpath,
  *          arg(2)=Append/Prepend;
  *     1.9.1 2003-02-12, adjusting for JNI 1.1 *and* 1.2 (as starting with
  *          Java 1.4 the JNI 1.1 interface does not work anymore (just bombs)
  *
  *     1.9.0 2003-01-27, ---rgf, added ability to load JVM from the Rexx side,
  *          initializing BSF4Rexx from the Java side as well, such that all of
  *          BSF4Rexx' functionality is available to Rexx programs started by Rexx
  *
  *     1.2.1 2003-01-06, ---rgf, finally found bug causing Object Rexx to bomb (but also Regina, it turned out!)
  *                    reason: freeing memory for Rexx-code with wrong method !
  *
  *     2003-01-05, ---rgf, adapted to Mark Hessling's great "Rexx/Trans"
  *           (cf. <http://rexxtrans.sourceforge.net/>), which allows for
  *           generic support for any supported Rexx interpreter, e.g.:
  *
  *              Regina <http://regina-rexx.sourceforge.net/>,
  *              IBM Object Rexx <http://www.software.ibm.com/ad/obj-rexx/>,
  *              Quercus Systems Personal Rexx <http://www.quercus-sys.com/>,
  *              Enterprise Rexx <http://www.WinREXX.com/>
  *
  *     2001-06-11, ---rgf, runs with JNI 1 (does *not* need JRE 1.2 or higher
  *             anymore, so OS/2 users with 1.1.8 can use this package as well)
  *
  * author:  Rony G. Flatscher, WU (pronounced: "vey-u") Vienna University of Economics
  *          and Business Administration
  *
  *  LICENSE:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright 2001-2015 Rony G. Flatscher

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


/*  */

#ifdef __cplusplus
extern "C" {            // make sure no C++ name mangling comes into the way
#endif

/* */


// Java 1.1 o.k., Java 1.2 trap-popup-window, Java 1.3 shows DLLs does NOT create log-file,
// Java 1.4 just shows there is "Another exception while handling last error ..

// #define DEBUG_HIT_ENTER_TO_CONTINUE     // ---rgf, 2009-09-12: allows to stop execution to inspect the debug output before proceeding
// #define DEBUG
// #define DEBUG1
// #define DEBUG2
// #define DEBUG3
// #define DEBUG_THREADS

// #define RGF_INFO    // 2008-07-23, for debugging new JNI-interface implementation
// #define RGF_INFO_1  // 2008-07-23, for debugging new JNI-interface implementation

// #define RGF_ATTACH  // 2008-08-05, for debugging Attaching
// #define RGF_DETACH  // 2008-08-05, for debugging Detaching

// #define RGF_BIT     // 2008-08-06, for debugging individual points; use JPane-popups

// #define RGF_BIT2    // 2008-08-24


// #define RGF_MUTEX4RXSIOTRC  // 2008-08-06, testing RXSIOTRC in multithreading mode, undefine for production
// #define RGF_INVALID_ROUTINE     // 2008-08-12, show Java error after call to BSF()

// #define RXSIO_USE_RGF_ALLOC     // 2008-08-14, rgf


// #define DEBUG40        // 2009-04-25: debug new functionality developed with ooRexx 4.0 APIs
// #define DEBUG_JNI      // debug JNI invocations
// #define DEBUG_JNI_ENTRY   // rgf, 2009-10-10, debug JNI invocations: only entry into function

// #define DEBUG_REXX_ATTACH       // 2009-05-26, attaching/detaching to/from Rexx instances
// #define DEBUG_OREXX_REGISTRY    // 2009-05-28, setting up/tearing down OREXX_REGISTRY
// #define DEBUG_REXX_PROXY        // 2009-06-10
// #define DEBUG_REGISTER_REXX_OBJECT // 2012-06-07
// #define DEBUG_UNREGISTER_REXX_OBJECT    // 2009-09-02
// #define DEBUG_UNREGISTER_REXX_OBJECT_NOISY   // 2009-09-03
// #define DEBUG_REXX_PROXY_BSF    // 2009-06-14
// #define DEBUG_RII_LIST          // 2009-05-28, adding/removing Rexx interpreter instances
// #define DEBUG_LOADER_UNLOADER      // 2009-06-05
// #define DEBUG_CREATE_HALT_TERMINATE // 2009-06-07, debug instantiation, halting and termination of Rexx interpreter instances
// #define DEBUG_TERMINATE_INTERPRETER // 2009-09-03

// #define DEBUG_BSF_FUNCTION          // 2009-06-21
// #define DEBUG_RETURN_VALUE_FOR_JAVA // 2009-06-22

// #define DEBUG_SEND_MESSAGE_TO_REXX_OBJECT    // 2009-09-06
// #define DEBUG_RGF_PROCESS_J_ARGS     // 2009-09-06
// #define DEBUG_RGF_PROCESS_J_ARG1    // rgf, 2012-02-07; renamed to RgfJavaObject2RexxObject, 2012-02-18
// #define DEBUG_ARRAY_ARGS

//    #define DEBUG_ARRAY_ARGS            // rgf, 2012-02-21

// #define DEBUG_SHOW_STRUCTURES    // ---rgf, 2009-10-06
// #define DEBUG_JAVA_ATTACH_DETACH    // ---rgf, 2009-10-07


// #define DEBUG_TID2JRST      // ---rgf, 2009-09-28
// #define DEBUG_JRST          // ---rgf, 2009-09-28

// #define DEBUG_FIND_RAJO         // rgf, 2011-03-12
// #define DEBUG_RAJO              // rgf, 2011-03-12

// #define RGF_TRUE      // ---rgf, 2010-02-10



// #define RGF_COMMAND_HANDLER         // rgf, 2012-02-07
// #define RGF_SYSTEM_EXIT_HANDLER     // rgf, 2012-02-07
// #define DEBUG_JNI

// #define DEBUG_RGF_PROCESS_J_ARG1    // rgf, 2012-02-07; renamed to RgfJavaObject2RexxObject, 2012-02-18
// #define DEBUG_RGF_PROCESS_J_ARGS


// #define DEBUG_REXX_OBJECT_TO_JAVA   // rgf, 2012-02-19
// #define DEBUG_ARRAY_ARGS            // rgf, 2012-02-21

// #define RGF_JENV_OBJECTS        // rgf, 2012-03-12, show Java object values in structure (cache)

// #define RGF_JNI_RII     // rgf, 2012-12-29, debugging crash documented by TestConcurrencyCrash.java


// #define DEBUG_REXX_PROXY        // 2009-06-10
// #define DEBUG_BSF_FUNCTION          // 2009-06-21

// #define DEBUG_REXX_PROXY_BSF // 2015-07/8
// #define DEBUG_SEND_MESSAGE_TO_REXX_OBJECT
// #define DEBUG_RGF_PROCESS_J_ARGS
// #define DEBUG_REXX_PROXY


// #if defined (UNIX)  && defined (NIXI_NOXI) // comment before && to activate all of the flags below
#if defined (WINDOWS) && defined (NIXI_NOXI) // comment before && to activate all of the flags below
    #define DEBUG
    #define DEBUG1
    #define DEBUG2
    #define DEBUG3
    #define DEBUG_THREADS

    #define RGF_INFO    // 2008-07-23, for debugging new JNI-interface implementation
    #define RGF_INFO_1  // 2008-07-23, for debugging new JNI-interface implementation

    #define RGF_ATTACH  // 2008-08-05, for debugging Attaching
    #define RGF_DETACH  // 2008-08-05, for debugging Detaching

    #define DEBUG40        // 2009-04-25: debug new functionality developed with ooRexx 4.0 APIs
    #define DEBUG_JNI      // debug JNI invocations
    #define DEBUG_JNI_ENTRY   // rgf, 2009-10-10, debug JNI invocations: only entry into function

    #define DEBUG_REXX_ATTACH       // 2009-05-26, attaching/detaching to/from Rexx instances
    #define DEBUG_OREXX_REGISTRY    // 2009-05-28, setting up/tearing down OREXX_REGISTRY
    #define DEBUG_REXX_PROXY        // 2009-06-10
    #define DEBUG_REGISTER_REXX_OBJECT // 2012-06-07
    #define DEBUG_UNREGISTER_REXX_OBJECT    // 2009-09-02
    #define DEBUG_UNREGISTER_REXX_OBJECT_NOISY    // 2009-09-03
    #define DEBUG_REXX_PROXY_BSF    // 2009-06-14
    #define DEBUG_RII_LIST          // 2009-05-28, adding/removing Rexx interpreter instances
    #define DEBUG_LOADER_UNLOADER      // 2009-06-05
    #define DEBUG_CREATE_HALT_TERMINATE // 2009-06-07, debug instantiation, halting and termination of Rexx interpreter instances
    #define DEBUG_TERMINATE_INTERPRETER // 2009-09-03

    #define DEBUG_BSF_FUNCTION          // 2009-06-21
    #define DEBUG_RETURN_VALUE_FOR_JAVA // 2009-06-22

    #define DEBUG_SEND_MESSAGE_TO_REXX_OBJECT    // 2009-09-06
    #define DEBUG_RGF_PROCESS_J_ARGS     // 2009-09-06

    #define DEBUG_SHOW_STRUCTURES    // ---rgf, 2009-10-06
    #define DEBUG_JAVA_ATTACH_DETACH    // ---rgf, 2009-10-07

    #define RGF_COMMAND_HANDLER         // rgf, 2012-02-07
    #define RGF_SYSTEM_EXIT_HANDLER     // rgf, 2012-02-07
    #define DEBUG_RGF_PROCESS_J_ARG1    // rgf, 2012-02-07; renamed to RgfJavaObject2RexxObject, 2012-02-18

    #define DEBUG_ARRAY_ARGS            // rgf, 2012-02-21
    #define DEBUG_RGF_ATTACH_NEW        // debug new attach/detach strategy
#endif

#if defined (__APPLE__) && NIXI      // only define, if rexx is started on the main thread and established a CFRunLoop (then awt's event dispatch thread needs to run on a separate thread)
    #define RGF_LOAD_JVM_IN_SEPARATE_THREAD
#endif

// #define RGF_UNO_WRAP     // rgf, 2010-06-08: use UNO.WRAP, if .UNO available
// #define DEBUG_RGF_PROCESS_J_ARGS_UNO


    // 2012-02-17, rgf
    // if the following flag is defined, then RexxCommandHandler.java and
    // RexxExitHandler. java are defined as interface classes, hence:
    // comment out all code that assumes that Java command and exit handlers
    // are implemented by extending an abstract class RexxCommandHandler.java
    // and RexxExitHandler.java (which might run faster, as the method handle
    // lookup only needs to be done for the abstract classes, rather than
    // querying the class object and its Rexx handler method each time a
    // command or exit gets invoked)

// rgf, 2012-02-17
#define CONFIG_REXX_HANDLERS_AS_INTERFACES

// rgf, 2012-03-11
#define CONFIG_HANDLE_NETREXX_AS_STRING_4_REXX

    // define the handler's name and signatures
#define CONFIG_REXX_COMMAND_HANDLER_NAME      "handleCommand"
#define CONFIG_REXX_COMMAND_HANDLER_SIGNATURE "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;"
#define CONFIG_REXX_EXIT_HANDLER_NAME         "handleExit"
#define CONFIG_REXX_EXIT_HANDLER_SIGNATURE    "(Ljava/lang/Object;II[Ljava/lang/Object;)I"

    // rgf, 2012-06-06
#define CONFIG_MAKE_OREXX_REGISTRY_ACCESSIBLE   // meant for debugging

    // rgf, 2014-03-30, name of entry into .local for primodal TID (will allow one to attach even if no TID is known to the Rexx programmer)
#define LOCAL_PRIMODAL_TID                    "BSF.PRIMODALTID"


    // rgf, 2014-05-16, prepare and use new attach/detach to/from Java logic
#define RGF_ATTACH_NEW
// #define DEBUG_RGF_ATTACH_NEW        // debug new attach/detach strategy

    // from: <http://ccollomb.free.fr/blog/?p=30>, rgf, 2009-06-02
// __asm Int 3;    // break, popup to break will be shown
// DebugBreak();   // break, popup to break will be shown
                    // debug single step, without popup to break:
#define BREAK_HERE_SINGLESTEP() __asm __emit 0xF1


#include "oorexxapi.h"

    // ---> needed for code was created on pre ooRexx-4.0 and used typedefs, that may not be present anymore
    #define RGF_ITS_OOREXX_4    // indicate we deal with ooRexx 4 or higher

    #ifndef TID
        #define TID thread_id_t
    #endif

    #ifndef APIRET
        #define APIRET RexxReturnCode
    #endif

    #ifndef PFN
        #define PFN REXXPFN
    #endif
    // <--- needed for code was created on pre ooRexx-4.0 and used typedefs, that may not be present anymore


#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>

    // if used from Java: if last Rexx interpreter instance terminates, then tear down the OREXX_REGISTRY
// #define RGF_TEAR_DOWN_OREXX_REGISTRY    // if causing troubles in rexx.exe, comment this line

#define USE_OREXX_REGISTRY_PACKAGE      // 2009-06-10: use package object from RexxProxy

#ifdef WINDOWS
   #define ALLOW_LOADING_JVM
   #define RGF_USE_MUTEX        // use MUTEX instead of critical sections
#endif


#ifdef UNIX
   #include <dlfcn.h>       // use the dynamic link functions

// rgf, 20090404
   #include <sys/types.h>
   #include <unistd.h>

   #ifndef __APPLE__    // not for Apple (would need to split off the JVM loading part)
       #define ALLOW_LOADING_JVM    //   ---rgf, 2005-08-23, #idef'ed - not for OS2 and not for Macintosh
   #endif

#define ALLOW_LOADING_JVM // rgf 2011-01-02, temporarily TODO: remove this definition

   // rgf, 2009-08-13
   #ifndef VOID
      typedef void VOID;
      typedef void *PVOID;
   #endif

   #ifndef PCHAR
       typedef char *PCHAR;
   #endif

   #ifndef PINT
       typedef int *PINT;
   #endif

   #ifndef TRUE
       #define TRUE 1
   #endif

   #ifndef PSZ
      typedef const char *PSZ;
   #endif


#endif

// #undef ALLOW_LOADING_JVM // test, rgf, 20090507

      // under Linux gcc RH 7.3 not defined, ---rgf, 2003-04-05
#ifndef FALSE
    #define FALSE 0
#endif

#ifndef INT
    #define INT int
#endif
    // end (Linux RH 7.2)


#ifdef _MSC_VER
# pragma warning(disable:4100)
          // MS does not define a C99 "snprintf()" function
    #define SNPRINTF _snprintf
#else
    #define SNPRINTF snprintf
#endif


// -------------------- JNI-interface related
#include "org_rexxla_bsf_engines_rexx_RexxAndJava.h" // rgf, 2005-06-02




// -------------------- ooRexx 4.0 and later
// prototypes
int32_t RgfRemoveProxyObject2    (RexxThreadContext *rtc, RexxStringObject obj_id);
int32_t RgfRemoveAllProxyObjectsByRIID2(RexxThreadContext *rtc, RexxStringObject rii_id);
void    RgfSetupOrexxRegistry    (RexxThreadContext *rtc);
void    RgfTearDownOrexxRegistry (RexxThreadContext *rtc);

char *JNU_GetStringNativeChars(JNIEnv *env, jstring jstr);  // JNIEnv *env, jstring jstr)
RexxObjectPtr JNU_GetStringNativeCharsReturningRexxStringObject(JNIEnv *env, jstring jstr, RexxThreadContext *context);  // JNIEnv *env, jstring jstr)
    // only for debugging:
VOID JAVA_TO_STRING (JNIEnv *env, jobject jo, char * buffer, int bufSize);

char * RgfCreateRexxlikeErrorInfo (RexxThreadContext *rtc, RexxDirectoryObject condObj, const char * header);


#define REXX_PROXY_STUB_PROGRAM "BSF_OnTheFly.cls"  // name of Rexx program to use for creating ad hoc RexxProxy object
// #define REXX_CALLBACK_ARG_STEM_NAME "BSFARG."       // stem name to be used, if setting the callback directory entries in the caller



// #define USE_DEFINED_JNI_VERSION     JNI_VERSION_1_2 //  JNI_VERSION_1_4
#define USE_DEFINED_JNI_VERSION     JNI_VERSION_1_4


// typedef jint (JNICALL *rgf_AttachCurrentThread)  (JavaVM  *, void **, void *);
typedef jint (JNICALL *rgf_JNI_CreateJavaVM)             (JavaVM **, void **, void *);
typedef jint (JNICALL *rgf_JNI_GetDefaultJavaVMInitArgs) (void    *);


#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
RgfGetTID();


#if defined (BSF4REXX_32_BIT)
    #define BSF_VERSION     "452.20150806 org/rexxla/bsf/engines/rexx 32-bit"  // version: "MajorNumber"."YYYYMMDD"
#elif defined (BSF4REXX_64_BIT)
    #define BSF_VERSION     "452.20150806 org/rexxla/bsf/engines/rexx 64-bit"  // version: "MajorNumber"."YYYYMMDD"
#else
    #define BSF_VERSION     "452.20150806 org/rexxla/bsf/engines/rexx n/a-bit"  // version: "MajorNumber"."YYYYMMDD"
#endif

#define JAVA_4_REXX     "org/rexxla/bsf/engines/rexx/Java4Rexx"     // 2005-06-02, supportive Java4Rexx-class


#define DLLNAME         "BSF4ooRexx"      // name of DLL and name for "PARSE SOURCE"
// #define JAVA_CALL_BSF   "javaCallBSF"   // name of the Java method that carries out the BSF()-call

#define  INVALID_ROUTINE 40            /* Rexx: raise Rexx error # 40   */
#define  VALID_ROUTINE    0            /* Rexx: successful completion   */


// globals, rgf, 2008-08-01
JavaVM    *currentJVM=NULL;     // JVM to use
int       bsfInvokedBy = 0;     // 0=noJVM, 1=byJava, 2=byRexx

    // calculate the needed bytes for representing a pointer as hex-string with trailing \0
const int CONST_LENGTH_OF_POINTER_0 = sizeof( PVOID )*2+1;

    // ---rgf, 2006-11-19: define boolean and maximum length for Java's event trace
int       bShowErrorString = 1;         // default to showing (dumping) Java traceback in case of an error
#define BSF_ERROR_STRING  "BSF_ERROR_MESSAGE"    // variable name to use to save Java error message in Rexx variable

#ifdef OBSOLETE
const int lenBsfErrorString = (int) strlen(BSF_ERROR_STRING);   // calc length once and forever
#endif


// forward prototypes
    PVOID RgfAllocateMemory(size_t size);
    void  RgfFreeMemory (void * memory);


// === (BEGIN) ========================> maintain list of RexxInterpreterInstances, OREXX_REGISTRY ========>
    // define locks
#ifdef UNIX
   pthread_mutex_t RII_lock      = PTHREAD_MUTEX_INITIALIZER;  // rgf, 2009-05-03, RexxInterpreter instance lock
   pthread_mutex_t RII_creation_lock = PTHREAD_MUTEX_INITIALIZER;  // rgf, 20121230, RexxInterpreter instance creation lock
   pthread_mutex_t REGISTRY_lock = PTHREAD_MUTEX_INITIALIZER;  // rgf, 2009-05-03, OREXX_REGISTRY* lock

#elif defined WINDOWS
   #if defined (RGF_USE_MUTEX)
       HANDLE RII_lock;                     // rgf, 2009-06-03
       HANDLE RII_creation_lock;                     // rgf, 20121230
       HANDLE REGISTRY_lock;                // rgf, 2009-06-03
   #else
       CRITICAL_SECTION RII_lock;                               // rgf, 2009-05-03
       CRITICAL_SECTION RII_creation_lock;  // rgf, 20121230
       CRITICAL_SECTION REGISTRY_lock;                          // rgf, 2009-05-03
   #endif
#endif


// ----------------------------------------------------------------------------
//       DEBUG_HIT_ENTER_TO_CONTINUE
inline void RgfDebugHitEnterToContinue()
{
    fprintf(stderr, "\n[RgfDebugHitEnterToContinue(): Hit Enter to Continue...");fflush(stderr);
    char stuff[5];
    int a=fscanf(stdin, "%c", stuff);
    fprintf(stderr, "\n");fflush(stderr);
}


inline RexxStringObject RgfDebug_GetMaxStringValue(RexxThreadContext *context, RexxObjectPtr rop, size_t max_len)
{
    RexxStringObject rso=context->ObjectToString(rop);  // get String value

    size_t strLen=context->StringLength(rso);

    if (strLen>max_len)
    {
        rso=(RexxStringObject) context->SendMessage2(rso, "SUBSTR",
                                     (RexxStringObject) context->StringSizeToObject(1),
                                     (RexxStringObject) context->StringSizeToObject(max_len));

        rso=(RexxStringObject) context->SendMessage1(rso, "||", context->String(" [...]"));
    }
    return rso;

}

// ----------------------------------------------------------------------------
inline VOID  RgfAcquireLock2(
    #ifdef WINDOWS
       #if defined (RGF_USE_MUTEX)
            HANDLE
       #else
            CRITICAL_SECTION
       #endif
    #else  // UNIX
        pthread_mutex_t
    #endif
                          lock)
    // ===> 2008-07-23: MUTEX, blocking invocation, waits until lock was successfully acquired
    //                  - initializing/setting up of 'JRST_lock' is done in JNI_OnLoad()
// inline VOID RgfAcquireLock()
{

#if defined (RGF_INFO) || defined (RGF_ATTACH) || defined (RGF_DETACH) || defined (DEBUG_REXX_ATTACH)
    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            tid=RgfGetTID();

    fprintf(stderr, "*** RGF_INFO: --- RgfAcquireLock2(lock): tid=[%lu], lock=[%p] ...\n", (unsigned long) tid, (void *)& lock);
    fflush(stderr);
#endif

#ifdef WINDOWS // WIN32
    #if defined (RGF_USE_MUTEX)
        WaitForSingleObject(lock, INFINITE);
    #else
        EnterCriticalSection (&lock);
    #endif

#elif defined UNIX
    pthread_mutex_lock(&lock);
#endif


#if defined (RGF_INFO) || defined (RGF_ATTACH) || defined (RGF_DETACH) || defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "*** RGF_INFO: --> RgfAcquireLock2(lock): tid=[%lu], lock=[%p]  ... now ACQUIRED !\n", (unsigned long) tid, (void *) &lock);
    fflush(stderr);
#endif

}


inline VOID  RgfReleaseLock2(                  // prototype definition
    #ifdef WINDOWS
       #if defined (RGF_USE_MUTEX)
            HANDLE
       #else
            CRITICAL_SECTION
       #endif
    #else  // UNIX
        pthread_mutex_t
    #endif
                          lock)
{
#if defined (RGF_INFO) || defined (RGF_ATTACH) || defined (RGF_DETACH) || defined (DEBUG_REXX_ATTACH)
    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            tid=RgfGetTID();
    fprintf(stderr, "*** RGF_INFO: --- RgfReleaseLock2(lock): tid=[%lu], lock=[%p]  ...\n", (unsigned long) tid, (void *) & lock);
    fflush(stderr);
#endif

#if defined WINDOWS // WIN32
    #if defined (RGF_USE_MUTEX)
        ReleaseMutex(lock);
    #else
        LeaveCriticalSection (&lock);
    #endif
#else
    pthread_mutex_unlock(&lock);
#endif

#if defined (RGF_INFO) || defined (RGF_ATTACH) || defined (RGF_DETACH) || defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "*** RGF_INFO: <-- RgfReleaseLock2(lock): tid=[%lu], lock=[%p]  ... now RELEASED !\n", (unsigned long) tid, (void *) & lock);
    fflush(stderr);
#endif
}


// Rexx Interpreter Instance =======================================================================================

    // rgf, 20090503: add list of RexxInterpreter instances
    // defines the list of "RexxInterpreter instances" (RII)
    // TODO: maybe add references to RexxEngine, RexxAndJava and its MID for "String makeString4Rexx(Object)"
    //                         RexxProxy constructor MID and MID for static newJavaProxyInstance,
    //                         RexxException with MIDs for its constructors

typedef struct _STRUCT_RII {
    struct _STRUCT_RII          *next;      // pointer to next block
    RexxInstance                *instance;  // the instance pointer, needed as anchor
    jobject                      rajo;      // 2012-02-07, save RexxAndJava object which created this instance
    jobject                      rexxconf;  // 2012-02-07, save RexxConfiguration object used for this RII
}  STRUCT_RII;

typedef STRUCT_RII   *PSTRUCT_RII;          // pointer to structure
long size_STRUCT_RII =sizeof(STRUCT_RII);   // get and memorize size of structure
PSTRUCT_RII pRoot_RII=NULL;                 // root of RII list


    // calculate the needed bytes for representing a pointer as hex-string with trailing \0, make it an even number of chars
const size_t RGF_POINTER_STRING_WIDTH=sizeof( PVOID )*2+2;
const size_t RGF_TID_STRING_WIDTH    = 80;  // allows for hex representation of 2**128 plus two bytes (one for \0, the other for evening)

// #define RgfPointer2String(ptr, str)     sprintf((char *)str, "%p", (void *) ptr);   // macro

// 2009-10-11, rgf, adding "+1", such that Linux gcc 32-bit does not truncate the pointer value
#define RgfPointer2String(ptr, str)     SNPRINTF((char *)str, RGF_POINTER_STRING_WIDTH+1, "%p", (void *) ptr);   // macro

/*
inline void RgfPointer2String (void * ptr, char * str)
{
    ... bla, bla
}
*/

#define RgfString2Pointer(str, ptr)     sscanf(str, "%p", &ptr);                    // macro


inline void RgfDumpRexxInstanceList()
{

    fprintf(stderr, "\n---> RgfDumpRexxInstanceList():\n");
    fflush(stderr);
    int st=0;

    for (PSTRUCT_RII i=pRoot_RII; i!=NULL; i=i->next)
    {

        st++;
        fprintf(stderr, "\t# [%d]: pRoot_RII=[%p]: i=[%p], i->instance=[%p], i->next=[%p]\n",
                                               st, pRoot_RII, i, i->instance, i->next);

// if (st>10) { DebugBreak(); }

    }
    fprintf(stderr, "<--- RgfDumpRexxInstanceList():\n");
    fflush(stderr);
}



inline RexxInstance * RgfGetRexxInstanceFromString(const char * c_rii_ID)
{
    RexxInstance *ri=NULL;
    RgfString2Pointer(c_rii_ID, ri);
    return ri;
}


// add a new Rexx interpreter instance to the list
// caller needs to set the lock
// inline logical_t RgfAddRexxInterpreterInstanceToList(RexxThreadContext *rtc, const char * pString)
inline logical_t RgfAddRexxInterpreterInstanceToList(RexxThreadContext *rtc, jobject rajo, jobject rexxconf)
{
#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "--> RgfAddRexxInterpreterInstanceToList(...): adding new Rexx instance c_rii_ID=[%p] to the list\n", rtc->instance);
    fflush(stderr);
#endif

    RexxInstance *ri=rtc->instance;
//    RgfPointer2String(ri, pString);



// fprintf(stderr, "---> AFTER \n"); fflush(stderr);

    PSTRUCT_RII newNode=(PSTRUCT_RII) RgfAllocateMemory(size_STRUCT_RII);
    memset(newNode,0,size_STRUCT_RII);

    newNode->instance=ri;           // save RexxInstance
        // 2012-02-07, rgf: adding fast support for new exit and command handlers, hence storing
        //                  rajo and rexxconf with RII; both are NULL if Java had to be loaded by Rexx in
        //                  which case no exit and command handler options were used for creating the RII
    newNode->rajo    =rajo;         // only for use in exit and command handlers !
    newNode->rexxconf=rexxconf;     // only for use in exit and command handlers !

#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "(0)  RgfAddRexxInterpreterInstanceToList(...): newNode=[%p]/->instance=[%p],ri=[%p], ->next=[%p] | pRoot_RII=[%p].\n",
                                                                   newNode, newNode->instance, ri, newNode->next, pRoot_RII);
    fflush(stderr);

    RgfDumpRexxInstanceList();
#endif

    logical_t rc=true;              // default to true

    RgfAcquireLock2(RII_lock);      // lock access to list of RexxInstance instances
    if (pRoot_RII==NULL)            // empty list as of yet?
    {
        pRoot_RII=newNode;          // new node is at the same time the root node
        RgfReleaseLock2(RII_lock);

        // make sure we have an OOREXX_REGISTRY set up
        RgfSetupOrexxRegistry(rtc);

    }
    else                            // find last node, add new one to last one
    {
        for (PSTRUCT_RII i=pRoot_RII; i!=NULL; i=i->next)
        {
#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "(1a) RgfAddRexxInterpreterInstanceToList(...): loop: i=[%p], i->instance=[%p], i->next=[%p]\n",
                                                                           i, i->instance, i->next);
    fflush(stderr);
#endif


            if (i->next==NULL)      // last node found, insert new RexxInterpreter instance
            {
#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "(2)  RgfAddRexxInterpreterInstanceToList(...): FOUND (i->next==NULL), insert newNode=[%p]\n",
                                                                           newNode);
    fflush(stderr);
#endif
                i->next=newNode;
                break;
            }
        }
        RgfReleaseLock2(RII_lock);
    }


#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "<-- RgfAddRexxInterpreterInstanceToList(...): finished adding new Rexx instance c_rii_ID=[%p]; newNode=[%p]/instance=[%p]/next=[%p] | pRoot_RII=[%p].\n",
                                                                   rtc->instance, newNode, newNode->instance, newNode->next, pRoot_RII);
    fflush(stderr);
#endif

    return rc;          // indicate success
}


// remove given instance from list, if not found, return NULL
// caller needs to set the lock
inline RexxInstance * RgfRemoveRexxInterpreterInstanceFromList(RexxInstance *ri)
{
#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "--> RgfRemoveRexxInterpreterInstanceFromList(...): 1a) ri=[%p], pRoot_RII=[%p], pRoot_RII->instance=[%p], pRoot_RII->next=[%p]\n",
                                   ri, pRoot_RII, (pRoot_RII->instance == NULL ? NULL : pRoot_RII->instance), (pRoot_RII->next==NULL ? NULL : pRoot_RII->next));
    fflush(stderr);
    RgfDumpRexxInstanceList();
#endif

    RgfAcquireLock2(RII_lock);      // lock access to list of RexxInstance instances
    if (pRoot_RII==NULL)    // no interpreter instance available
    {
        RgfReleaseLock2(RII_lock);
        return NULL;
    }

    int bFound   =0;
    // RgfAcquireLock2(RII_lock);       // lock access to list of RexxInstance instances

    PSTRUCT_RII i=NULL,
                oldNode=pRoot_RII;  // start out with root node

    if (pRoot_RII->instance==ri)    // root node represents ri !
    {

#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "--//RgfRemoveRexxInterpreterInstanceFromList(...):  BEFORE REMOVE 1st ELEMENT   ri=[%p], pRoot_RII=[%p], pRoot_RII->instance=[%p], pRoot_RII->next=[%p]\n",
            ri, pRoot_RII, (pRoot_RII->instance == NULL ? NULL : pRoot_RII->instance), (pRoot_RII->next==NULL ? NULL : pRoot_RII->next));
    fflush(stderr);
#endif

        pRoot_RII=pRoot_RII->next;  // let root point to next node (or NULL, if none)

#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "--//RgfRemoveRexxInterpreterInstanceFromList(...):  AFTER  REMOVE 1st ELEMENT   ri=[%p], pRoot_RII=[%p], pRoot_RII->instance=[%p], pRoot_RII->next=[%p]\n",
                    ri,
                    pRoot_RII,
                    (pRoot_RII==NULL ? NULL : (pRoot_RII->instance == NULL ? NULL : pRoot_RII->instance)),
                    (pRoot_RII==NULL ? NULL : (pRoot_RII->next==NULL ? NULL : pRoot_RII->next))
                    );
    fflush(stderr);
#endif

        bFound=1;
    }
    else
    {
        // for (i=pRoot_RII->next; i!=NULL; i=i->next)
        for (i=pRoot_RII; i!=NULL; i=i->next)
        {
            if (i->instance==ri)    // node found, remove it
            {
                oldNode->next=i->next;  // let previous node point to the next node
                oldNode=i;
                bFound=1;
                break;
            }
            oldNode=i;
        }
    }

#if defined (DEBUG_RII_LIST)
    RgfDumpRexxInstanceList();
#endif

    RgfReleaseLock2(RII_lock);


#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "RgfRemoveRexxInterpreterInstanceFromList(...): 1b) ri=[%p], bFound=[%d], pRoot_RII=[%p], pRoot_RII->instance=[%p], pRoot_RII->next=[%p]\n",
                                   ri, bFound, pRoot_RII, (pRoot_RII == NULL ? NULL : (pRoot_RII->instance == NULL ? NULL : pRoot_RII->instance)),  (pRoot_RII == NULL ? NULL : (pRoot_RII->next==NULL ? NULL : pRoot_RII->next)));
    fflush(stderr);
#endif



#if defined ( RGF_TEAR_DOWN_OREXX_REGISTRY )
        // last Rexx interpreter instance found and about to remove from list?
    if (bFound==1 && pRoot_RII==NULL)   // node found, but it was the last Rexx interpreter instance, do clean-up
    {
                // make sure we clear the OOREXX_REGISTRYies
        RexxThreadContext *rtc;

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "--------------> ooRexx->AttachThread() - RgfRemoveRIIfromList... "); fflush(stderr);
#endif

        logical_t rc=ri->AttachThread(&rtc);    // attach thread to Interpreter instance

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "done. -------------->\n"); fflush(stderr);
#endif

// DebugBreak();

        if (rc)                     // if successful, go ahead
        {
            RgfTearDownOrexxRegistry(rtc);  // clear the OREXX_REGISTRYies

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "<-------------- ooRexx->DetachThread() - RgfRemoveRIIfromList... "); fflush(stderr);
#endif

            rtc->DetachThread();    // detach thread from Interpreter instance

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "done. <--------------\n"); fflush(stderr);
#endif

        }
        else
        {
#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "RgfRemoveRexxInterpreterInstanceFromList(...): 2) UNSUCCESSFUL to AttachThread(), prematurely returning! ri=[%p], bFound=[%d], pRoot_RII=[%p], pRoot_RII->instance=[%p], pRoot_RII->next=[%p]\n",
                                   ri, bFound, pRoot_RII, (pRoot_RII == NULL ? NULL : (pRoot_RII->instance == NULL ? NULL : pRoot_RII->instance)),  (pRoot_RII == NULL ? NULL : (pRoot_RII->next==NULL ? NULL : pRoot_RII->next)));
    fflush(stderr);
#endif
            // RgfReleaseLock2(RII_lock);
            return NULL;            // indicate failure
        }
    }

    // RgfReleaseLock2(RII_lock);
#endif



#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "<-- RgfRemoveRexxInterpreterInstanceFromList(...): 3) bFound=[%d], ri=[%p], pRoot_RII=[%p], pRoot_RII->instance=[%p], pRoot_RII->next=[%p]\n",
                                   bFound, ri, pRoot_RII,
                                   (pRoot_RII!=NULL ? (pRoot_RII->instance == NULL ? NULL : pRoot_RII->instance) : NULL),
                                   (pRoot_RII!=NULL ? (pRoot_RII->next==NULL ? NULL : pRoot_RII->next) : NULL)
                                   );
    fflush(stderr);
#endif


    if (bFound==1)              // if node found, delete it now
    {

#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "beFor 'RgfFreeMemory(oldNode)': oldNode=[%p], oldNode->instance=[%p], oldNode->next=[%p] ...\n",
                                                     oldNode, oldNode->instance, oldNode->next);
    fflush(stderr);
    RgfDumpRexxInstanceList();
#endif

        RgfFreeMemory(oldNode);

#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "naCh  'RgfFreeMemory(oldNode)' ...\n"); fflush(stderr);
    RgfDumpRexxInstanceList();
#endif

        return ri;              // return ri, indicates success
    }
    return NULL;                // indicate failure
}



    // returns supplied RexxInstance, if it exists, otherwise the root instance (could be NULL)
    // if argument is NULL, then root RexxInstance is returned
    // caller needs to set the lock
inline RexxInstance * RgfGetRexxInterpreterInstanceFromList(RexxInstance *ri)
{

#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "--> RgfGetRexxInterpreterInstanceFromList(...): ri=[%p], pRoot_RII=[%p], pRoot_RII->instance=[%p], pRoot_RII->next=[%p]\n",
                                   ri, pRoot_RII, (pRoot_RII->instance == NULL ? NULL : pRoot_RII->instance), (pRoot_RII->next==NULL ? NULL : pRoot_RII->next));
    fflush(stderr);
    RgfDumpRexxInstanceList();
#endif

    int bFound   =0;
    RexxInstance *tmpRi=ri;
    size_t st=0;


    RgfAcquireLock2(RII_lock);      // lock access to list of RexxInstance instances
    if (ri!=NULL && pRoot_RII!=NULL)        // no RexxInstance given, return (any) root RexxInstance
    {
//        RgfAcquireLock2(RII_lock);        // lock access to list of RexxInstance instances
        // for (PSTRUCT_RII i=pRoot_RII->next; i!=NULL; i=i->next) // find node
        for (PSTRUCT_RII i=pRoot_RII; i!=NULL; i=i->next) // find node
        {

#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "<>RgfGetRexxInterpreterInstanceFromList<>, st=[%lu]: pRoot_RII=[%p]: i=[%p], i->instance=[%p], i->next=[%p]; received ri=[%p]\n",
                                                              ++st, pRoot_RII, i, i->instance, i->next, ri);
    fflush(stderr);

    #if defined (WINDOWS)    // rgf, 2009-08-20
         if (st>10)
         {
             DebugBreak();
         }
    #endif

#endif

            if (i->instance==ri)        // node found, remove it
            {

#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "<>RgfGetRexxInterpreterInstanceFromList<>, st=[%lu]: i=[%p], i->instance=[%p], ri=[%p] FOUND/FOUND/FOUND!\n",
                                                              ++st, i, i->instance, ri);
    fflush(stderr);
#endif

                bFound=1;
                break;
            }
        }
//        RgfReleaseLock2(RII_lock);

        if (bFound==1)          // o.k. Rexx interpreter instance is known, return it
        {
            RgfReleaseLock2(RII_lock);
            return tmpRi;
        }
    }

    if (pRoot_RII) {                    // root available, return its RexxInterpreter instance instead
        tmpRi=pRoot_RII->instance;
        RgfReleaseLock2(RII_lock);
        // return pRoot_RII->instance;     // not found, return the root RexxInstance
        return tmpRi;                   // not found, return the root RexxInstance
    }

    RgfReleaseLock2(RII_lock);
    return NULL;                        // no RexxInterpreter instance available
}


    // rgf, 2012-02-07: returns the PSTRUCT_RII, such that exit and command handlers can get
    //                  get a fast handle on rajo and rexxconf for calling into Java handlers;
    //                  if supplied RII or no root RII exits, returns NULL
inline PSTRUCT_RII RgfGetRexxInterpreterInstanceStructFromList(RexxInstance *ri)
{

#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "--> RgfGetRexxInterpreterInstanceStructFromList(...): ri=[%p], pRoot_RII=[%p], pRoot_RII->instance=[%p], pRoot_RII->next=[%p]\n",
                                   ri, pRoot_RII, (pRoot_RII->instance == NULL ? NULL : pRoot_RII->instance), (pRoot_RII->next==NULL ? NULL : pRoot_RII->next));
    fflush(stderr);
    RgfDumpRexxInstanceList();
#endif

    if (ri==NULL || pRoot_RII==NULL)
    {
        return NULL;
    }

    int bFound   =0;
    RexxInstance *tmpRi=ri;
    size_t st=0;

    PSTRUCT_RII i=NULL;

    RgfAcquireLock2(RII_lock);      // lock access to list of RexxInstance instances
    for (i=pRoot_RII; i!=NULL; i=i->next) // find node
    {

#if defined (DEBUG_RII_LIST)
fprintf(stderr, "<>RgfGetRexxInterpreterInstanceStructFromList<>, st=[%lu]: pRoot_RII=[%p]: i=[%p], i->instance=[%p], i->next=[%p]; received ri=[%p]\n",
                                                          ++st, pRoot_RII, i, i->instance, i->next, ri);
fflush(stderr);

#if defined (WINDOWS)    // rgf, 2009-08-20
     if (st>10)
     {
         DebugBreak();
     }
#endif

#endif

        if (i->instance==ri)        // node found, return it
        {

#if defined (DEBUG_RII_LIST)
fprintf(stderr, "<>RgfGetRexxInterpreterInstanceStructFromList<>, st=[%lu]: i=[%p], i->instance=[%p], ri=[%p] FOUND/FOUND/FOUND!\n",
                                                          ++st, i, i->instance, ri);
fflush(stderr);
#endif

            bFound=1;
            break;
        }
    }
    RgfReleaseLock2(RII_lock);

    if (bFound==1)          // o.k. Rexx interpreter instance is known, return it
    {
        return i;
    }

    return NULL;                        // no RexxInterpreter instance available
}





// OREXX_REGISTRY ============================================================================================
  // some helpful inline functions

    // if not a RexxThreadContext available, use: "context->threadContext" instead
/*
inline RexxClassObject NIXI_STRING_CLASS(RexxThreadContext *rtc)
{
    if (STRING_CLZ==NULL)   // Rexx String class object not retrieved, hence get and save it
    {
        STRING_CLZ=(RexxClassObject) rtc->SendMessage0(rtc->String("0"), "CLASS");
    }
    return STRING_CLZ;      // return .String
}
*/


    // return
inline RexxObjectPtr rgfGetEntryFromLocal (RexxThreadContext *rtc, CSTRING entry)
{
    RexxObjectPtr rop1=rtc->GetLocalEnvironment();
    RexxObjectPtr rop2=rtc->SendMessage0(rop1, entry);
    rtc->ReleaseLocalReference(rop1);
    return rop2;
    // return rtc->SendMessage0(rtc->GetLocalEnvironment(), entry);
}

inline RexxObjectPtr rgfGetEntryFromEnvironment (RexxThreadContext *rtc, CSTRING entry)
{
    RexxObjectPtr rop1=rtc->GetGlobalEnvironment();
    RexxObjectPtr rop2=rtc->SendMessage0(rop1, entry);
    rtc->ReleaseLocalReference(rop1);
    return rop2;
    // return rtc->SendMessage0(rtc->GetGlobalEnvironment(), entry);
}




// RII          Rexx Interpreter Instance
// OID          Object ID: the identityHashValue as a string

  // defines the registry for proxied Rexx objects
     // key: obj~identityHash;
RexxDirectoryObject OREXX_REGISTRY            = NULL;   // one entry per cached Rexx object

#if defined (USE_OREXX_REGISTRY_PACKAGE)
    RexxDirectoryObject OREXX_REGISTRY_PACKAGE    = NULL;   // one entry per cached Rexx object containing the object's package object
#endif

RexxDirectoryObject OREXX_REGISTRY_REFCOUNTER = NULL;   // refcounter per registered Rexx object (could be referenced multiple times)

#define USE_RII2OID_RELATION

#ifdef USE_RII2OID_RELATION
   RexxObjectPtr OREXX_RII2_OID_RELATION = NULL;       // rii->obj_ID; allows to free all rii-related proxies, if an rii ends
#endif

    // create the OREXX_REGISTRY and OREXX_REGISTRY_REFCOUNTER, do a RequestGlobalReference() on each
inline void RgfSetupOrexxRegistry(RexxThreadContext *rtc)
{
#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "--> RgfSetupOrexxRegistry(), rii_id=[%p]... \n", rtc->instance);
    fflush(stderr);
#endif

    RgfAcquireLock2(REGISTRY_lock);     // make sure, no one else can interfere
    if (OREXX_REGISTRY == NULL)         // still NULL, hence no other thread could interfere before the locking!
    {
        OREXX_REGISTRY=rtc->NewDirectory();             // create new directory object
        rtc->RequestGlobalReference(OREXX_REGISTRY);    // globally lock it (all its collected object will be protected from gc as well)


#if defined (USE_OREXX_REGISTRY_PACKAGE)
        OREXX_REGISTRY_PACKAGE=rtc->NewDirectory();             // create new directory object
        rtc->RequestGlobalReference(OREXX_REGISTRY_PACKAGE);    // globally lock it (all its collected object will be protected from gc as well)
#endif

        OREXX_REGISTRY_REFCOUNTER=rtc->NewDirectory();          // create new directory object
        rtc->RequestGlobalReference(OREXX_REGISTRY_REFCOUNTER); // globally lock it (all its collected object will be protected from gc as well)

#ifdef USE_RII2OID_RELATION
        // get .Relation, create an instance of it and assign it
   RexxObjectPtr rop1=rtc->GetGlobalEnvironment();
   RexxObjectPtr rop2=rtc->SendMessage0(rop1, "RELATION");
   OREXX_RII2_OID_RELATION = rtc->SendMessage0(rop2, "NEW");
   rtc->ReleaseLocalReference(rop2);
   rtc->ReleaseLocalReference(rop1);
   // OREXX_RII2_OID_RELATION = rtc->SendMessage0(rtc->SendMessage0(rtc->GetGlobalEnvironment(), "RELATION"), "NEW");

   rtc->RequestGlobalReference(OREXX_RII2_OID_RELATION); // globally lock it (all its collected object will be protected from gc as well)
#endif

    }
    RgfReleaseLock2(REGISTRY_lock);

#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "<-- RgfSetupOrexxRegistry(), finished, rii_id=[%p]... \n", rtc->instance);
    fflush(stderr);
#endif
}


    // free OREXX_REGISTRY and OREXX_REGISTRY_REFCOUNTER, do a ReleaseGlobalReference() on each
    // this will be called by the library unloader, i.e. at shutdown of BSF4Rexx,
    // hence no locking necessary anymore
inline VOID RgfTearDownOrexxRegistry(RexxThreadContext *rtc)
{

#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "--> RgfTearDownOrexxRegistry(), rii_id=[%p]... \n", rtc->instance);
    fflush(stderr);
#endif

//    const char c_rii_ID[ RGF_POINTER_STRING_WIDTH ]="";
//    RgfPointer2String(rtc->instance, c_rii_ID);     // get Root node's charater value
// fprintf(stderr, "c_rii_ID=[%s]\n", c_rii_ID);
// fflush(stderr);



    if (OREXX_REGISTRY!=NULL)                       // if in debug/testing mode, it could be that it's not initialized
    {
#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "RgfTearDownOrexxRegistry()..., SendMessage0(): emptying OREXX_REGISTRY\n");
    fflush(stderr);
#endif

        rtc->SendMessage0(OREXX_REGISTRY, "EMPTY");     // empty all objects to give them a chance to uninit

#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "RgfTearDownOrexxRegistry()..., ReleaseGlobalReference(): OREXX_REGISTRY\n");
    fflush(stderr);
#endif

        rtc->ReleaseGlobalReference(OREXX_REGISTRY);    // allow directory object to be garbage collected as well
        OREXX_REGISTRY=NULL;
    }


#if defined (USE_OREXX_REGISTRY_PACKAGE)
    if (OREXX_REGISTRY_PACKAGE!=NULL)
    {
#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "RgfTearDownOrexxRegistry()..., SendMessage0(): emptying OREXX_REGISTRY_PACKAGE\n");
    fflush(stderr);
#endif

        rtc->SendMessage0(OREXX_REGISTRY_PACKAGE, "EMPTY");     // empty all objects to give them a chance to uninit

#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "RgfTearDownOrexxRegistry()..., ReleaseGlobalReference(): OREXX_REGISTRY_PACKAGE\n");
    fflush(stderr);
#endif

        rtc->ReleaseGlobalReference(OREXX_REGISTRY_PACKAGE);    // allow directory object to be garbage collected as well
        OREXX_REGISTRY_PACKAGE=NULL;
    }
#endif


    if (OREXX_REGISTRY_REFCOUNTER!=NULL)            // if in debug/testing mode, it could be that it's not initialized
    {
#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "RgfTearDownOrexxRegistry()..., SendMessage0(): emptying OREXX_REGISTRY_REFCOUNTER\n");
    fflush(stderr);
#endif

        rtc->SendMessage0(OREXX_REGISTRY_REFCOUNTER, "EMPTY");  // empty all objects to give them a chance to uninit

#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "RgfTearDownOrexxRegistry()..., ReleaseGlobalReference(): OREXX_REGISTRY_REFCOUNTER\n");
    fflush(stderr);
#endif

        rtc->ReleaseGlobalReference(OREXX_REGISTRY_REFCOUNTER); // allow directory object to be garbage collected as well
        OREXX_REGISTRY_REFCOUNTER=NULL;
    }

#ifdef USE_RII2OID_RELATION
    if (OREXX_RII2_OID_RELATION!=NULL)            // if in debug/testing mode, it could be that it's not initialized
    {
#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "RgfTearDownOrexxRegistry()..., SendMessage0(): emptying OREXX_RII2_OID_RELATION\n");
    fflush(stderr);
#endif

        rtc->SendMessage0(OREXX_RII2_OID_RELATION, "EMPTY");  // empty all objects to give them a chance to uninit

#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "RgfTearDownOrexxRegistry()..., ReleaseGlobalReference(): OREXX_RII2_OID_RELATION\n");
    fflush(stderr);
#endif
        rtc->ReleaseGlobalReference(OREXX_RII2_OID_RELATION); // allow relation object to be garbage collected as well
        OREXX_RII2_OID_RELATION=NULL;
    }
#endif


#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "<-- RgfTearDownOrexxRegistry()..., finished, rii_id=[%p]... \n", rtc->instance);
    fflush(stderr);
#endif

}


    // put received Rexx object into the OREXX_REGISTRY and set reference counter to 1; if it exists already
    // just increase the reference counter
inline const char * RgfAddProxyObject(RexxThreadContext *rtc, RexxObjectPtr robj, RexxPackageObject rpo)
{
#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "---> RgfAddProxyObject(), begin: robj=[%p], robj~makeString=[%.256s], robj~class~id=[%.256s]\n",
                                           robj,
                                           rtc->CString(rtc->ObjectToString(robj)),
                                           rtc->CString((rtc->SendMessage0( (rtc->SendMessage0(robj, "CLASS")), "ID")) )

                                           );
    fflush(stderr);
#endif

    RexxStringObject obj_id=(RexxStringObject) rtc->SendMessage0(robj, "IDENTITYHASH");   // get HashIdentity value

    RgfAcquireLock2(REGISTRY_lock);      // make sure, no one else can interfere
    if (rtc->SendMessage1(OREXX_REGISTRY, "HASENTRY", obj_id)==rtc->False())    // not yet in registry
    {
        rtc->SendMessage2(OREXX_REGISTRY,            "PUT", robj, obj_id);  // save object

#if defined (USE_OREXX_REGISTRY_PACKAGE)
        if (rpo!=NULL)  // 2012-06-07, rgf: a NULL gets accepted, but then the entry can never be deleted in ooRexx 4.1.1
        {
            rtc->SendMessage2(OREXX_REGISTRY_PACKAGE,    "PUT", rpo,  obj_id);  // save package object
        }

    #if defined (DEBUG_REGISTER_REXX_OBJECT)
        fprintf(stderr, "** RgfAddProxyObject: cstr_obj_id=[%s], rpo=[%s], ~items=[%s] <-- <-- <--\n",
                        obj_id==NULL ? NULL : rtc->ObjectToStringValue(obj_id),
                        rpo   ==NULL ? NULL : rtc->ObjectToStringValue(rpo),
                        OREXX_REGISTRY_PACKAGE==NULL ? NULL : rtc->ObjectToStringValue(rtc->SendMessage0(OREXX_REGISTRY_PACKAGE, "ITEMS")));
        fflush(stderr);
    #endif
#endif

        RexxStringObject rso=rtc->String("1");
        rtc->SendMessage2(OREXX_REGISTRY_REFCOUNTER, "PUT", rso, obj_id); // add counter entry, set to "1"
        // rtc->SendMessage2(OREXX_REGISTRY_REFCOUNTER, "PUT", rtc->String("1"), obj_id); // add counter entry, set to "1"
        rtc->ReleaseLocalReference(rso);

#ifdef USE_RII2OID_RELATION
        // const char c_rii_ID [ RGF_POINTER_STRING_WIDTH+1 ] = "";
        char *c_rii_ID=new char[RGF_POINTER_STRING_WIDTH];

        RgfPointer2String(rtc->instance, c_rii_ID);  // create a string representation of interpreter instance pointer

        rso=rtc->String(c_rii_ID);
        rtc->SendMessage2(OREXX_RII2_OID_RELATION,   "PUT", obj_id, rso);  // save object
        // rtc->SendMessage2(OREXX_RII2_OID_RELATION,   "PUT", obj_id, rtc->String(c_rii_ID));  // save object
        rtc->ReleaseLocalReference(rso);
        delete [] c_rii_ID;
#endif
    }
    else    // entry exists already, just adjust the ref-counter
    {
        int32_t refs=0;
        RexxObjectPtr rop=rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "AT", obj_id);
        logical_t flag=rtc->ObjectToInt32(rop, &refs);
        rtc->ReleaseLocalReference(rop);
        // logical_t flag=rtc->ObjectToInt32(rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "AT", obj_id), &refs);

        refs++;     // increase ref-counter by one
        rop=rtc->Int32ToObject(refs);
        // RexxObjectPtr rop=rtc->Int32ToObject(refs);
        rtc->SendMessage2(OREXX_REGISTRY_REFCOUNTER, "PUT", rop, obj_id); // add counter entry, set to "1"
        // rtc->SendMessage2(OREXX_REGISTRY_REFCOUNTER, "PUT", rtc->Int32ToObject(refs), obj_id); // add counter entry, set to "1"
        rtc->ReleaseLocalReference(rop);
    }
    RgfReleaseLock2(REGISTRY_lock);


#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "<--- RgfAddProxyObject(), end:   robj=[%p], returning obj_id=[%.256s] (identityHash)\n", robj, rtc->CString((RexxStringObject) obj_id));
    fflush(stderr);
#endif

    const char *returnValue=rtc->CString(obj_id);
    rtc->ReleaseLocalReference(obj_id);
    return returnValue;
    // return rtc->CString((RexxStringObject) obj_id);
}



#ifdef USE_RII2OID_RELATION
    // =============================================================================================================
        // remove Rexx object from the JNI registry if counter is 1, otherwise just reduce reference counter by 1;
        // returns number of Rexx proxy objects removed
        //          -1 if not found
    inline int32_t RgfRemoveAllProxyObjectsByRIID(RexxInstance *ri, char * c_rii_id)
    {
    #if defined (DEBUG_REXX_PROXY) || defined (DEBUG_UNREGISTER_REXX_OBJECT)
        fprintf(stderr, "---> RgfRemoveAllProxyObjectsByRIID(), begin: c_rii_id=[%.256s] (Rexx interpreter instance)\n", c_rii_id);
        fflush(stderr);
    #endif

        RexxThreadContext *rtc;
    #if defined (DEBUG_REXX_ATTACH) || defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
        fprintf(stderr, "--------------> ooRexx->AttachThread() - RgfRemoveAllProxyObjectsByRIID... "); fflush(stderr);
    #endif

        logical_t rc=ri->AttachThread(&rtc);  // attach thread to Interpreter instance

    #if defined (DEBUG_REXX_ATTACH) || defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
        fprintf(stderr, "done. rc=[%d]-------------->\n", (int) rc); fflush(stderr);
    #endif

        if (!rc)                // if successful, go ahead
        {
            return -101;        // indicate that we could not attach thread to RexxInstance successfully
        }

        RexxStringObject rii_id=rtc->String(c_rii_id);  // turn into a RexxString object

        int32_t refs=RgfRemoveAllProxyObjectsByRIID2(rtc, rii_id);    // now carry out the work
        rtc->ReleaseLocalReference(rii_id);

    #if defined (DEBUG_REXX_ATTACH) || defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
        fprintf(stderr, "<-------------- ooRexx->DetachThread() - RgfRemoveAllProxyObjectsByRIID... "); fflush(stderr);
    #endif

        rtc->DetachThread();    // detach thread from Interpreter instance

    #if defined (DEBUG_REXX_ATTACH) || defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
        fprintf(stderr, "done. <--------------\n"); fflush(stderr);
    #endif


    #if defined (DEBUG_REXX_PROXY) || defined (DEBUG_UNREGISTER_REXX_OBJECT)
        fprintf(stderr, "<--- RgfRemoveAllProxyObjectsByRIID(), end:   c_rii_id=[%.256s], remaining references=[%d]\n", c_rii_id, refs);
        fflush(stderr);
    #endif


        return refs;
    }


        // worker function
    inline int32_t RgfRemoveAllProxyObjectsByRIID2(RexxThreadContext *rtc, RexxStringObject rii_id)
    {
        int32_t refs=0;

    #if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
        fprintf(stderr, "     ---> RgfRemoveAllProxyObjectsByRIID2, begin: rtc=[%p], rii_id=[%p]\n", rtc, rii_id); fflush(stderr);
    #endif

        RgfAcquireLock2(REGISTRY_lock);      // make sure, no one else can interfere

    #if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
        fprintf(stderr, "          RgfRemoveAllProxyObjectsByRIID2, acquired REGISTRY_lock, before rtc->SendMessage1(...), rtc=[%p], rii_id=[%p], OREXX_RII2_OID_RELATION=[%p] ...\n", rtc, rii_id, OREXX_RII2_OID_RELATION); fflush(stderr);
    #endif

        // get all objects associated with given RII_ID
        RexxArrayObject rao=(RexxArrayObject) rtc->SendMessage1(OREXX_RII2_OID_RELATION, "ALLAT", rii_id);
        size_t items=rtc->ArrayItems(rao);      // get number of items returned

        if (items>0)
        {
            for (size_t i=0; i<items; i++)
            {
                RexxObjectPtr obj_id=rtc->ArrayAt(rao, i+1);    // get object to process

                // get reference count, decrease it by one, remove rii_id/obj_id from relation
                int32_t refCount=0;
                RexxObjectPtr rop=rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "AT", obj_id);
                logical_t flag=rtc->ObjectToInt32(rop, &refCount);
                rtc->ReleaseLocalReference(rop);
                // logical_t flag=rtc->ObjectToInt32(rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "AT", obj_id), &refCount);

                refCount--;     // decrease ref-counter by one
                if (refCount>0) // not yet at 0, save actual refCount
                {
                    rtc->SendMessage2(OREXX_REGISTRY_REFCOUNTER, "PUT", rtc->Int32ToObject(refCount), obj_id); // add counter entry, set to "1"
                }
                else    // if refcount is 0 or lower, remove object from refCount and object registry in addition
                {
                    rop=rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "REMOVE", obj_id); // remove counter entry
                    rtc->ReleaseLocalReference(rop);
                    rop=rtc->SendMessage1(OREXX_REGISTRY,            "REMOVE", obj_id); // remove object from proxy cache altogether
                    rtc->ReleaseLocalReference(rop);
                    // rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "REMOVE", obj_id); // remove counter entry
                    // rtc->SendMessage1(OREXX_REGISTRY,            "REMOVE", obj_id); // remove object from proxy cache altogether

        #if defined (USE_OREXX_REGISTRY_PACKAGE)
                    rop=rtc->SendMessage1(OREXX_REGISTRY_PACKAGE,    "REMOVE", obj_id); // package object
                    rtc->ReleaseLocalReference(rop);
        #endif
                }

                // remove rii_id/obj_id pair from relation
                rop=rtc->SendMessage2(OREXX_RII2_OID_RELATION,   "REMOVEITEM", obj_id, rii_id);  // remove object
                rtc->ReleaseLocalReference(rop);
                // rtc->SendMessage2(OREXX_RII2_OID_RELATION,   "REMOVEITEM", obj_id, rii_id);  // remove object

                rtc->ReleaseLocalReference(obj_id);
            }
            refs= (int32_t) items;      // return number of objets we have freed
        }
        else    // entry does not exist
        {
            refs= -1;           // indicate that entry does not exist
        }

        RgfReleaseLock2(REGISTRY_lock);     // make sure lock gets released

    #if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
        fprintf(stderr, "     <--- RgfRemoveAllProxyObjectsByRIID2, end: refs=[%d]\n", refs); fflush(stderr);
    #endif

        return refs;            // return number of references (if negative: error)
    }


#endif





// =============================================================================================================
    // remove Rexx object from the JNI registry if counter is 1, otherwise just reduce reference counter by 1;
    // returns references left: >= 0
    //          -1 if not found
    //        -100 if no RexxInterpreter instance is available
    //        -101 could not attach to RexxInterpreter instance
inline int32_t RgfRemoveProxyObject(char * c_obj_id)
{
#if defined (DEBUG_REXX_PROXY) || defined (DEBUG_UNREGISTER_REXX_OBJECT)
    fprintf(stderr, "---> RgfRemoveProxyObject(), begin: c_obj_id=[%.256s] (identityHash)\n", c_obj_id);
    fflush(stderr);
#endif

    int32_t refs=0;

    // RgfAcquireLock2(RII_lock);      // lock access to list of RexxInstance instances
    RexxInstance *ri=RgfGetRexxInterpreterInstanceFromList(NULL);    // get any Rexx Interpreter instance
    // RgfReleaseLock2(RII_lock);      // lock access to list of RexxInstance instances


    if (ri==NULL)           // no RexxInterpreter instance available?
    {
        return -100;        // indicate no running RexxInterpreter instance available
    }

    RexxThreadContext *rtc;
#if defined (DEBUG_REXX_ATTACH) || defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
    fprintf(stderr, "--------------> ooRexx->AttachThread() - RgfRemoveProxyObject... "); fflush(stderr);
#endif

    logical_t rc=ri->AttachThread(&rtc);  // attach thread to Interpreter instance

#if defined (DEBUG_REXX_ATTACH) || defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
    fprintf(stderr, "done. rc=[%d]-------------->\n", (int) rc); fflush(stderr);
#endif


    if (!rc)                // if not successful, return, indicate error
    {
        return -101;        // indicate that we could not attach thread to RexxInstance successfully
    }

    RexxStringObject obj_id=rtc->String(c_obj_id);  // turn into a RexxString object

    refs=RgfRemoveProxyObject2(rtc, obj_id);         // now carry out the work
    rtc->ReleaseLocalReference(obj_id);

#if defined (DEBUG_REXX_ATTACH) || defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
    fprintf(stderr, "<-------------- ooRexx->DetachThread() - RgfRemoveProxyObject... "); fflush(stderr);
#endif

    rtc->DetachThread();    // detach thread from Interpreter instance

#if defined (DEBUG_REXX_ATTACH) || defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
    fprintf(stderr, "done. <--------------\n"); fflush(stderr);
#endif


#if defined (DEBUG_REXX_PROXY) || defined (DEBUG_UNREGISTER_REXX_OBJECT)
    fprintf(stderr, "<--- RgfRemoveProxyObject(), end:   c_obj_id=[%.256s], remaining references=[%d]\n", c_obj_id, refs);
    fflush(stderr);
#endif


    return refs;
}

    // worker function
inline int32_t RgfRemoveProxyObject2(RexxThreadContext *rtc, RexxStringObject obj_id)
{
    int32_t refs=0;

#if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
    RexxObjectPtr orxTrue=rtc->True();
    CSTRING cstr_obj_id = obj_id==NULL ? NULL : rtc->ObjectToStringValue(obj_id);
    fprintf(stderr, "     ---> RgfRemoveProxyObject2, begin: rtc=[%p], obj_id=%%p=[%p], %%s=[%s]\n", rtc, obj_id, cstr_obj_id); fflush(stderr);
#endif

    RgfAcquireLock2(REGISTRY_lock);      // make sure, no one else can interfere

#if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
    fprintf(stderr, "          RgfRemoveProxyObject2, acquired REGISTRY_lock, before rtc->SendMessage1(\"HASENTRY\",[%s]) ...\n",cstr_obj_id); fflush(stderr);

    fprintf(stderr, "\t\thasIndex([%s]): OREXX_REGISTRY=[%d], OREXX_REGISTRY_REFCOUNTER=[%d], OREXX_REGISTRY_PACKAGE=[%d]\n",
                    cstr_obj_id,
                    rtc->SendMessage1(OREXX_REGISTRY, "HASENTRY", obj_id)==orxTrue,
                    rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "HASENTRY", obj_id)==orxTrue,
                    rtc->SendMessage1(OREXX_REGISTRY_PACKAGE, "HASENTRY", obj_id)==orxTrue);


    #if defined (USE_OREXX_REGISTRY_PACKAGE)
            fprintf(stderr, "\t\t\thasItem([%s])=OREXX_RII2_OID_RELATION=[%d]\n",
                            cstr_obj_id,
                            rtc->SendMessage1(OREXX_RII2_OID_RELATION,"HASITEM", obj_id)==orxTrue);

    #endif
    fflush(stderr);


#endif

    if (rtc->SendMessage1(OREXX_REGISTRY, "HASENTRY", obj_id)==rtc->True())     // not yet in registry
    {

#if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
    fprintf(stderr, "          RgfRemoveProxyObject2, before getting count: OREXX_REGISTRY_REFCOUNTER=[%p]", OREXX_REGISTRY_REFCOUNTER); fflush(stderr);
    fprintf(stderr, ", rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, \"AT\", obj_id=%%s=[%s]), result: %%p[%p]\n", cstr_obj_id, rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "AT", obj_id)); fflush(stderr);
#endif

        RexxObjectPtr rop=rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "AT", obj_id);
        logical_t flag=rtc->ObjectToInt32(rop, &refs);
        rtc->ReleaseLocalReference(rop);
        // logical_t flag=rtc->ObjectToInt32(rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "AT", obj_id), &refs);

        refs--;     // decrease ref-counter by one

#if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
    fprintf(stderr, "          RgfRemoveProxyObject2, retrieved 'refs'=[%d]\n", refs);
#endif

        if (refs>0) // still references there, then adjust ref counter entry
        {
#if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
    fprintf(stderr, "          RgfRemoveProxyObject2, before storing new 'refs'-value in OREXX_REGISTRY_REFCOUNTER...\n");  fflush(stderr);
#endif
            rtc->SendMessage2(OREXX_REGISTRY_REFCOUNTER, "PUT", rtc->Int32ToObject(refs), obj_id); // add counter entry, set to "1"
        }
        else        // remove object and matching refcounter entry
        {

#if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
    fprintf(stderr, "          RgfRemoveProxyObject2, before removing from OREXX_REGISTRY~remove(obj_id=[%s])...\n", cstr_obj_id);  fflush(stderr);
#endif
            RexxObjectPtr rop=rtc->SendMessage1(OREXX_REGISTRY, "REMOVE", obj_id); // object
            rtc->ReleaseLocalReference(rop);
            // rtc->SendMessage1(OREXX_REGISTRY,            "REMOVE", obj_id); // object

#if defined (USE_OREXX_REGISTRY_PACKAGE)
    #if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
        size_t items_before;
        rtc->ObjectToStringSize(rtc->SendMessage0(OREXX_REGISTRY_PACKAGE,"ITEMS"),&items_before);
        CSTRING hasIndex_before=rtc->ObjectToStringValue(rtc->SendMessage1(OREXX_REGISTRY_PACKAGE,"HASINDEX",obj_id));
    #endif
            rtc->SendMessage1(OREXX_REGISTRY_PACKAGE,    "REMOVE", obj_id); // package object

    #if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
        size_t items_after;
        rtc->ObjectToStringSize(rtc->SendMessage0(OREXX_REGISTRY_PACKAGE,"ITEMS"),&items_after);
        CSTRING hasIndex_after=rtc->ObjectToStringValue(rtc->SendMessage1(OREXX_REGISTRY_PACKAGE,"HASINDEX",obj_id));
        fprintf(stderr, "/////// obj_id=[%s], removed-result=[%s], items-before=[%lu]|hasIndex=[%s], after=[%lu]|hasIndex=[%s]\n",
                        rtc->CString(obj_id),
                        rop==NULL ? NULL : rtc->CString(rop),
                        items_before,
                        hasIndex_before,
                        items_after,
                        hasIndex_after
                        );
        fflush(stderr);
    #endif

#endif



#if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
    fprintf(stderr, "          RgfRemoveProxyObject2, before removing from OREXX_REGISTRY_REFCOUNTER=[%p]...\n", OREXX_REGISTRY_REFCOUNTER);  fflush(stderr);
#endif
            rop=rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "REMOVE", obj_id); // add counter entry, set to "1"
            rtc->ReleaseLocalReference(rop);
            // rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "REMOVE", obj_id); // add counter entry, set to "1"


#ifdef USE_RII2OID_RELATION
    #if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
        fprintf(stderr, "          RgfRemoveProxyObject2, before removing from OREXX_RII2_OID_RELATION=[%p]...\n", OREXX_RII2_OID_RELATION);  fflush(stderr);
    #endif

            // const char c_rii_ID [ RGF_POINTER_STRING_WIDTH+1 ] = "";
            char *c_rii_ID=new char[RGF_POINTER_STRING_WIDTH];

            RgfPointer2String(rtc->instance, c_rii_ID);  // create a string representation of interpreter instance pointer
            rtc->SendMessage2(OREXX_RII2_OID_RELATION,   "REMOVEITEM", obj_id, rtc->String(c_rii_ID));  // remove object
            delete [] c_rii_ID;
#endif

        }
    }
    else    // entry does not exist
    {
        refs= -1;           // indicate that entry does not exist
    }

    RgfReleaseLock2(REGISTRY_lock);     // make sure lock gets released

#if defined (DEBUG_UNREGISTER_REXX_OBJECT_NOISY)
    fprintf(stderr, "     <--- RgfRemoveProxyObject2, end: refs=[%d]\n", refs); fflush(stderr);
#endif

    return refs;            // return number of references (if negative: error)
}



    // get and return the ooRexx objects stored with 'obj_id' in the registry
inline RexxObjectPtr RgfGetProxyObject(RexxThreadContext *rtc, RexxStringObject obj_id)
{
#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "---> RgfGetProxyObject(), begin: obj_id=[%.256s]\n", rtc->CString(obj_id));
    fflush(stderr);
#endif


    RgfAcquireLock2(REGISTRY_lock);     // make sure, no one else can interfere

#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "---> RgfGetProxyObject(), 2a), acquired REGISTRY_lock, SendMessage1(OREXX_REGISTRY, \"AT\"), OREXX_REGISTRY=[%p], obj_id=[%s]...\n", OREXX_REGISTRY, rtc->CString(obj_id));
    fflush(stderr);

    fprintf(stderr, "---> RgfGetProxyObject(), 2a),         .nil  ~string=[%s}...\n", rtc->CString(rtc->SendMessage0(rtc->Nil()    , "STRING")) );
    fflush(stderr);

    fprintf(stderr, "---> RgfGetProxyObject(), 2a),         obj_id~string=[%s}...\n", rtc->CString(rtc->SendMessage0(obj_id        , "STRING")) );
    fflush(stderr);

    fprintf(stderr, "---> RgfGetProxyObject(), 2a), OREXX_REGISTRY~string=[%s}...\n", rtc->CString(rtc->SendMessage0(OREXX_REGISTRY, "STRING")) );
    fflush(stderr);
#endif

    RexxObjectPtr obj=rtc->SendMessage1(OREXX_REGISTRY, "AT", obj_id);


#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "---> RgfGetProxyObject(), 2b), AFTER, SendMessage1(OREXX_REGISTRY, \"AT\"), obj=[%p]=[%s]...\n",
                       obj, rtc->ObjectToStringValue(obj));
    fflush(stderr);
#endif

    RgfReleaseLock2(REGISTRY_lock);     // make sure lock gets released

#if defined (DEBUG_REXX_PROXY)
    const char *str=NULL;
    if (obj!=NULL)
        str=rtc->CString(rtc->ObjectToString(obj));

    fprintf(stderr, "<--- RgfGetProxyObject(), end:   obj_id=[%.256s], returning obj=[%p], obj~makeString=[%.256s], after releasing REGISTRY_lock \n",
                          rtc->CString(obj_id), obj, str );
    fflush(stderr);
#endif


    if (obj==NULL) {                    // no entry as nothing returned !
fprintf(stderr, "*** PANIC! PANIC! PANIC! RgfGetProxyObject(...), proxy object with obj_id=[%s] NOT FOUND !!!\n",
                rtc->CString(rtc->SendMessage0(obj_id        , "STRING")) );
fflush(stderr);

// TODO: rgf, 2015-08-06: create an exception/condition ?
        return rtc->Nil();              // return the .nil object
    }
    return obj;
}



    // get and return the ooRexx objects stored with 'obj_id' in the registry
inline RexxObjectPtr RgfGetProxyObjectRefCount(RexxThreadContext *rtc, RexxStringObject obj_id)
{
#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "---> RgfGetProxyObjectRefCount(), begin: obj_id=[%.256s]\n", rtc->CString(obj_id));
    fflush(stderr);
#endif


    RgfAcquireLock2(REGISTRY_lock);     // make sure, no one else can interfere
    RexxObjectPtr obj=rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "AT", obj_id);
    RgfReleaseLock2(REGISTRY_lock);     // make sure lock gets released

#if defined (DEBUG_REXX_PROXY)
    const char *str=NULL;
    if (obj!=NULL)
        str=rtc->CString(rtc->ObjectToString(obj));

    fprintf(stderr, "<--- RgfGetProxyObjectRefCount(), end:   obj_id=[%.256s], returning obj=[%p], obj~makeString=[%.256s]\n",
                          rtc->CString(obj_id), obj, str );
    fflush(stderr);
#endif


    if (obj==NULL) {                    // no entry as nothing returned !
        return rtc->Nil();              // return the .nil object
    }
    return obj;
}




#if defined (USE_OREXX_REGISTRY_PACKAGE)
        // get and return the ooRexx objects stored with 'obj_id' in the registry
    inline RexxObjectPtr RgfGetProxyObjectPackage(RexxThreadContext *rtc, RexxStringObject obj_id)
    {
        RgfAcquireLock2(REGISTRY_lock);     // make sure, no one else can interfere
        RexxObjectPtr obj=rtc->SendMessage1(OREXX_REGISTRY_PACKAGE, "AT", obj_id);
        RgfReleaseLock2(REGISTRY_lock);     // make sure lock gets released

        if (obj==NULL) {                    // no entry as nothing returned !
            return rtc->Nil();              // return the .nil object
        }
        return obj;
    }
#endif



// TODO: RgfAcquire_Lock2 obsoleted?
    // ===> 20090503: MUTEX, blocking invocation, waits until lock was successfully acquired
inline VOID RgfAcquire_Lock2(
    #ifdef WINDOWS
       #if defined (RGF_USE_MUTEX)
            HANDLE
       #else
            CRITICAL_SECTION
       #endif
    #else  // UNIX
        pthread_mutex_t
    #endif
                          lock)
{

#if defined (RGF_INFO)
    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            tid=RgfGetTID();

    fprintf(stderr, "*** RGF_INFO: RgfAcquire_Lock2(lock): tid=[%lu] ... \n", (unsigned long) tid);
    fflush(stderr);
#endif

#ifdef WINDOWS // WIN32
    #if defined (RGF_USE_MUTEX)
        WaitForSingleObject(lock, INFINITE);
    #else
        EnterCriticalSection (&lock);
    #endif

#elif defined UNIX
    pthread_mutex_lock(&lock);    // TODO: potentially deadlock on nested invocation right before return ?
#endif


#if defined (RGF_INFO)
    fprintf(stderr, "*** RGF_INFO: RgfAcquire_Lock2(lock): tid=[%lu] ... now ACQUIRED !\n", (unsigned long) tid);
    fflush(stderr);
#endif

}



// TODO: RgfRelease_Lock2 obsoleted?
    // ===> 2009-05-03: MUTEX, release lock
inline VOID RgfRelease_Lock2(
    #ifdef WINDOWS
       #if defined (RGF_USE_MUTEX)
            HANDLE
       #else
            CRITICAL_SECTION
       #endif
    #else  // UNIX
        pthread_mutex_t
    #endif
                          lock)
{
#if defined (RGF_INFO)
    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            tid=RgfGetTID();
    fprintf(stderr, "*** RGF_INFO: RgfRelease_Lock2(lock): tid=[%lu] ...\n", (unsigned long) tid);
    fflush(stderr);
#endif

#if defined WINDOWS // WIN32

    #if defined (RGF_USE_MUTEX)
        ReleaseMutex(lock);
    #else
        LeaveCriticalSection (&lock);
    #endif
#else
    pthread_mutex_unlock(&lock);
#endif

#if defined (RGF_INFO)
    fprintf(stderr, "*** RGF_INFO: RgfRelease_Lock2(): tid=[%lu] ... now RELEASED !\n", (unsigned long) tid);
    fflush(stderr);
#endif
}
// <=== (END) ========================> maintain list of RexxInterpreterInstances, OREXX_REGISTRY <========





// JRST related ===========================================================================================
// 2008-07-23, rgf >------------------------------------------------------------------------------------
// 2008-07-23: new structures for revamped logic
//
//  JRST    thread of jniRexxStart
//  RT      main thread of Rexx instance
//  BSFT    thread of current BSF() invocation
//  BLJT    thread of Rexx instance in which LoadJava() is invoked
//
//  RAJO    RexxAndJava instance (used to communicate with the Java side as well)

#ifdef UNIX
   #define PATH_SEPARATOR ':'
   pthread_mutex_t JRST_lock  = PTHREAD_MUTEX_INITIALIZER;  // rgf, 2008-07-23
//   pthread_mutex_t RXSIO_lock = PTHREAD_MUTEX_INITIALIZER;  // rgf, 2008-08-06

#elif defined WINDOWS // WIN32
   #define PATH_SEPARATOR ';'
   #if defined (RGF_USE_MUTEX)
        HANDLE JRST_lock;                   // rgf, 2009-06-03
//        HANDLE RXSIO_lock;                  // rgf, 2009-06-03
   #else
        CRITICAL_SECTION JRST_lock;                              // rgf, 2008-07-23
//        CRITICAL_SECTION RXSIO_lock;                             // rgf, 2008-08-06
   #endif

#endif


        // rgf, 2009-10-03, 2012-02-07 (RexxConfiguration, RexxExitHandler, RexxCommandHandler added)
        // define a structure that collects and caches JVM dependent data, which should be available
        // to all its threads at all times
typedef struct _STRUCT_JVM {
    JavaVM                     *jvm;                // JVM in use
    jobject                     primodal_rajo;      // primodal "RexxAndJava" object (if Rexx had to load Java, otherwise it will be NULL

    jclass                      clz_Object;         // java.lang.Object class object

    jclass                      clz_RexxAndJava;    // org.rexxla.bsf.engines.rexx.RexxAndJava
    jmethodID                   mid_RexxAndJava_convFromJNI;
    jmethodID                   mid_RexxAndJava_convFromRexx;
    jmethodID                   mid_RexxAndJava_getRexxEngine;
    jmethodID                   mid_RexxAndJava_hashCode;
    jmethodID                   mid_RexxAndJava_javaCallFromBSF;
    jmethodID                   mid_RexxAndJava_lookupBean4JNI;         // 20091018, rgf
    jmethodID                   mid_RexxAndJava_makeString4Rexx;        // 20091012, rgf
    jmethodID                   mid_RexxAndJava_registerBean4JNI;       // 20091018, rgf
    jmethodID                   mid_RexxAndJava_unregisterBean;

#if defined (CONFIG_HANDLE_NETREXX_AS_STRING_4_REXX)
    jclass                      clz_NetRexx;            // "netrexx/lang/Rexx", 20120311, rgf
    jmethodID                   mid_NetRexx_toString;   // "netrexx/lang/Rexx", 20120311, rgf
#endif

    jclass                      clz_RexxException;  // "org/rexxla/bsf/engines/rexx/RexxException"
    jmethodID                   mid_RexxException_init;     // constructor

    jclass                      clz_String;         // java.lang.String class object
    jmethodID                   mid_String_getBytes;// "getBytes"-method
    jmethodID                   mid_String_initFromByteArray;  // <init> with byte-array
    jstring                     jstring_emptyUTFString; // Java empty string

    jclass                      clz_RexxProxy;      // org.rexxla.bsf.engines.rexx.RexxProxy class object
    jmethodID                   mid_RexxProxy_init;             // constructor
    jmethodID                   mid_RexxProxy_getRexxObjectID;  // RexxProxy.getRexxObjectID()
    jmethodID                   mid_RexxProxy_newJavaOrExtendedProxyInstanceFromRexx;
    jmethodID                   mid_RexxProxy_setBsfRegistryKey;   // RexxProxy.setBsfRegistry()

    jclass                      clz_Throwable;      // java.lang.Throwable class object

    // rgf, 2012-02-07
    jclass                      clz_RexxConfiguration;  // org.rexxla.bsf.engines.rexx.RexxConfiguration
    // public RexxExitHandler getExitHandler(int function)
    jmethodID                   mid_RexxConfiguration_getExitHandler;
    // public RexxCommandHandler getCommandHandler(String name)
    jmethodID                   mid_RexxConfiguration_getCommandHandler;



#ifndef CONFIG_REXX_HANDLERS_AS_INTERFACES // rgf, 2012-02-17

    jclass                      clz_RexxExitHandler;    // org.rexxla.bsf.engines.rexx.RexxExitHandler
    // public abstract int handleExit(int exitNumber, int subFunction, Object[] parmBlock, Object[] slot);
    jmethodID                   mid_RexxExitHandler_handleExit;

    jclass                      clz_RexxCommandHandler; // org.rexxla.bsf.engines.rexx.RexxCommandHandler
    // public abstract Object handleCommand(String address, String command, Object[] slot);
    jmethodID                   mid_RexxCommandHandler_handleCommand;
#endif

} STRUCT_JVM;

typedef STRUCT_JVM *PSTRUCT_JVM;                    // pointer to structure
long size_STRUCT_JVM=sizeof(STRUCT_JVM);            // get and memorize size of structure


    // define a default value, not specifying thread name and not specifying thread group
JavaVMAttachArgs defaultJavaVMAttachArgs={USE_DEFINED_JNI_VERSION, NULL, NULL};

STRUCT_JVM       varDefaultJVM;         // define as a global variable (assuming: one JVM per process)
PSTRUCT_JVM      defaultJVM=&varDefaultJVM;

    // initialize JVM-dependent data we always need in all threads
inline void init_STRUCT_JVM(JNIEnv *env, PSTRUCT_JVM struct_JVM, JavaVM *jvm)
{

#if defined ( DEBUG_JNI )
   fprintf(stderr, "init_STRUCT_JVM(): just arrived - env->ExceptionCheck()=[%d]\n", env->ExceptionCheck());
#endif


    RgfAcquireLock2(RII_creation_lock);      // 2012-12-30, rgf: lock access to list of RexxInstance instances to this thread

    if  (struct_JVM->jvm!=NULL)     // already initialized, don't reprocess
    {
        RgfReleaseLock2(RII_creation_lock);      // 2012-12-30, rgf: unlock
        return;
    }

    struct_JVM->jvm=jvm;        // save JVM pointer
    jclass tmpClz=NULL;

        // get class object for java.lang.Object
    struct_JVM->clz_Object=env->FindClass("java/lang/Object");      // get class object

        // get MID for the interface method in rajo
    tmpClz=env->FindClass("org/rexxla/bsf/engines/rexx/RexxAndJava");    // get class object
    struct_JVM->clz_RexxAndJava     =tmpClz;
    struct_JVM->mid_RexxAndJava_convFromJNI        =env->GetMethodID( tmpClz, "convFromJNI",         "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
    struct_JVM->mid_RexxAndJava_convFromRexx       =env->GetMethodID( tmpClz, "convFromRexx",        "(Ljava/lang/String;)Ljava/lang/Object;");
    struct_JVM->mid_RexxAndJava_getRexxEngine      =env->GetMethodID( tmpClz, "getRexxEngine",       "()Lorg/apache/bsf/BSFEngine;");
    struct_JVM->mid_RexxAndJava_hashCode           =env->GetMethodID( tmpClz, "hashCode",            "()I");
    struct_JVM->mid_RexxAndJava_javaCallFromBSF    =env->GetMethodID( tmpClz, "javaCallBSF",         "([Ljava/lang/String;)Ljava/lang/String;" );
    struct_JVM->mid_RexxAndJava_lookupBean4JNI     =env->GetMethodID( tmpClz, "lookupBean4JNI",      "(Ljava/lang/String;)Ljava/lang/Object;" );
    struct_JVM->mid_RexxAndJava_registerBean4JNI   =env->GetMethodID( tmpClz, "registerBean4JNI",    "(Ljava/lang/Object;)Ljava/lang/String;" );
    struct_JVM->mid_RexxAndJava_unregisterBean     =env->GetMethodID( tmpClz, "unregisterBean",      "(Ljava/lang/String;)Z");
    struct_JVM->mid_RexxAndJava_makeString4Rexx    =env->GetMethodID( tmpClz, "makeString4Rexx",     "(Ljava/lang/Object;)Ljava/lang/String;");

// 2012-03-11, rgf: get static field (type class) "nlR_class", may be NULL, if no NetRexx available
//                  get toString() mid as well, needed for RgfJavaObject2RexxObject(...)
#if defined (CONFIG_HANDLE_NETREXX_AS_STRING_4_REXX)
    {
        jfieldID fid=env->GetStaticFieldID(tmpClz, "nlR_class", "Ljava/lang/Class;");
        jclass   j_nrxclz=(jclass) env->GetStaticObjectField(tmpClz, fid);
        if (j_nrxclz!=NULL)  // NetRexx class available, get its toString() method
        {
            struct_JVM->clz_NetRexx=j_nrxclz;           // assign NetRexx class object
            struct_JVM->mid_NetRexx_toString=env->GetMethodID( j_nrxclz, "toString", "()Ljava/lang/String;" );
        }
    }
#endif


       // get MID for RexxException
    tmpClz                       =env->FindClass("org/rexxla/bsf/engines/rexx/RexxException");
    struct_JVM->clz_RexxException=tmpClz;
    struct_JVM->mid_RexxException_init=env->GetMethodID(tmpClz, "<init>", "(ILjava/lang/String;Lorg/rexxla/bsf/engines/rexx/RexxProxy;)V");

        // get java.lang.String related objects
    tmpClz                    =env->FindClass("java/lang/String");
    struct_JVM->clz_String    =tmpClz;
    struct_JVM->mid_String_initFromByteArray=env->GetMethodID(tmpClz, "<init>", "([B)V");    // get constructor
    struct_JVM->mid_String_getBytes         =env->GetMethodID(tmpClz, "getBytes", "()[B");         // getBytes()

    struct_JVM->jstring_emptyUTFString=env->NewStringUTF("");  // was: (jstring) env->NewGlobalRef( env->NewStringUTF("") );

        // get org.rexxla.bsf.engines.rexx.RexxProxy related objects
    tmpClz=env->FindClass("org/rexxla/bsf/engines/rexx/RexxProxy");
    struct_JVM->clz_RexxProxy                =tmpClz;
    struct_JVM->mid_RexxProxy_init           =env->GetMethodID(tmpClz, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lorg/rexxla/bsf/engines/rexx/RexxEngine;)V");
    struct_JVM->mid_RexxProxy_getRexxObjectID=env->GetMethodID(tmpClz, "getRexxObjectID", "()Ljava/lang/String;");
    struct_JVM->mid_RexxProxy_newJavaOrExtendedProxyInstanceFromRexx=env->GetMethodID(tmpClz, "newJavaOrExtendedProxyInstanceFromRexx", "([Ljava/lang/Object;)Ljava/lang/Object;");
    struct_JVM->mid_RexxProxy_setBsfRegistryKey =env->GetMethodID(tmpClz, "setBsfRegistryKey", "(Ljava/lang/String;)V");

        // create Throwable class object reference
    struct_JVM->clz_Throwable=env->FindClass("java/lang/Throwable");    // get class object

    // rgf, 2012-02-07 -- begin
    tmpClz=env->FindClass("org/rexxla/bsf/engines/rexx/RexxConfiguration");    // get class object
    struct_JVM->clz_RexxConfiguration=tmpClz;  // org.rexxla.bsf.engines.rexx.RexxConfiguration
        // public RexxExitHandler getExitHandler(int function)
    struct_JVM->mid_RexxConfiguration_getExitHandler=env->GetMethodID(tmpClz, "getExitHandler", "(I)Lorg/rexxla/bsf/engines/rexx/RexxExitHandler;") ;
        // public RexxCommandHandler getCommandHandler(String name)
    struct_JVM->mid_RexxConfiguration_getCommandHandler=env->GetMethodID(tmpClz, "getCommandHandler", "(Ljava/lang/String;)Lorg/rexxla/bsf/engines/rexx/RexxCommandHandler;")  ;

#ifndef CONFIG_REXX_HANDLERS_AS_INTERFACES    // rgf, 2012-02-17
    tmpClz=env->FindClass("org/rexxla/bsf/engines/rexx/RexxExitHandler");    // get class object
    struct_JVM->clz_RexxExitHandler=tmpClz;    // org.rexxla.bsf.engines.rexx.RexxExitHandler
        // public abstract int handleExit(int exitNumber, int subFunction, Object[] parmBlock, Object[] slot);
    struct_JVM->mid_RexxExitHandler_handleExit=env->GetMethodID(tmpClz,
                                                     CONFIG_REXX_EXIT_HANDLER_NAME,
                                                     CONFIG_REXX_EXIT_HANDLER_SIGNATURE);
           // "handleExit", "(II[Ljava/lang/Object;[Ljava/lang/Object;)I");

    tmpClz=env->FindClass("org/rexxla/bsf/engines/rexx/RexxCommandHandler");    // get class object
    struct_JVM->clz_RexxCommandHandler=tmpClz;
        // public abstract Object handleCommand(String address, String command, Object[] slot);
    struct_JVM->mid_RexxCommandHandler_handleCommand=env->GetMethodID(tmpClz,
                                                     CONFIG_REXX_COMMAND_HANDLER_NAME,
                                                     CONFIG_REXX_COMMAND_HANDLER_SIGNATURE);
           // "handleCommand", "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
    // rgf, 2012-02-07 --end
#endif

        // create global references for these important objects
    struct_JVM->clz_Object       =(jclass)  env->NewGlobalRef(struct_JVM->clz_Object);
    struct_JVM->clz_RexxAndJava  =(jclass)  env->NewGlobalRef(struct_JVM->clz_RexxAndJava);

#if defined (CONFIG_HANDLE_NETREXX_AS_STRING_4_REXX)
    if (struct_JVM->clz_NetRexx!=NULL)
    {
        struct_JVM->clz_NetRexx=(jclass) env->NewGlobalRef(struct_JVM->clz_NetRexx);
    }
#endif

    struct_JVM->clz_RexxException=(jclass)  env->NewGlobalRef(struct_JVM->clz_RexxException);
    struct_JVM->clz_RexxProxy    =(jclass)  env->NewGlobalRef(struct_JVM->clz_RexxProxy);
    struct_JVM->clz_String       =(jclass)  env->NewGlobalRef(struct_JVM->clz_String);
    struct_JVM->clz_Throwable    =(jclass)  env->NewGlobalRef(struct_JVM->clz_Throwable);

    struct_JVM->jstring_emptyUTFString=(jstring) env->NewGlobalRef(struct_JVM->jstring_emptyUTFString);

    // rgf, 2012-02-07 --begin
    struct_JVM->clz_RexxConfiguration =(jclass)  env->NewGlobalRef(struct_JVM->clz_RexxConfiguration);

#ifndef CONFIG_REXX_HANDLERS_AS_INTERFACES    // rgf, 2012-02-17
    struct_JVM->clz_RexxExitHandler   =(jclass)  env->NewGlobalRef(struct_JVM->clz_RexxExitHandler);
    struct_JVM->clz_RexxCommandHandler=(jclass)  env->NewGlobalRef(struct_JVM->clz_RexxCommandHandler);
#endif
    // rgf, 2012-02-07 --end


#if defined ( DEBUG_JNI )  || defined (RGF_JENV_OBJECTS)
   fprintf(stderr, "\ninit_STRUCT_JVM(): after setting up, struct_JVM->jvm=[%p], env->ExceptionCheck()=[%d]\n\n",
                   struct_JVM->jvm, env->ExceptionCheck());

   fprintf(stderr, "\tclz_Object       =[%p]\n", struct_JVM->clz_Object);

   fprintf(stderr, "\tclz_RexxAndJava  =[%p]\n", struct_JVM->clz_RexxAndJava);
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxAndJava_convFromJNI        =[%p]\n", struct_JVM->mid_RexxAndJava_convFromJNI      );
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxAndJava_convFromRexx       =[%p]\n", struct_JVM->mid_RexxAndJava_convFromRexx     );
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxAndJava_getRexxEngine      =[%p]\n", struct_JVM->mid_RexxAndJava_getRexxEngine    );
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxAndJava_hashCode           =[%p]\n", struct_JVM->mid_RexxAndJava_hashCode         );
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxAndJava_javaCallFromBSF    =[%p]\n", struct_JVM->mid_RexxAndJava_javaCallFromBSF  );
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxAndJava_lookupBean4JNI     =[%p]\n", struct_JVM->mid_RexxAndJava_lookupBean4JNI   );
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxAndJava_makeString4Rexx    =[%p]\n", struct_JVM->mid_RexxAndJava_makeString4Rexx  );
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxAndJava_registerBean4JNI   =[%p]\n", struct_JVM->mid_RexxAndJava_registerBean4JNI );
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxAndJava_unregisterBean     =[%p]\n", struct_JVM->mid_RexxAndJava_unregisterBean   );

#if defined (CONFIG_HANDLE_NETREXX_AS_STRING_4_REXX)
   fprintf(stderr, "\tclz_NetRexx      =[%p]\n", struct_JVM->clz_NetRexx);
   fprintf(stderr, "\t\tstruct_JVM->mid_NetRexx_toString   =[%p]\n", struct_JVM->mid_NetRexx_toString );
#endif

   fprintf(stderr, "\tclz_RexxException=[%p]\n", struct_JVM->clz_RexxException);
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxException_init         =[%p]\n", struct_JVM->mid_RexxException_init         );

   fprintf(stderr, "\tclz_RexxProxy    =[%p]\n", struct_JVM->clz_RexxProxy);
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxProxy_init           =[%p]\n",                        struct_JVM->mid_RexxProxy_init);
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxProxy_getRexxObjectID=[%p]\n",                        struct_JVM->mid_RexxProxy_getRexxObjectID);
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxProxy_newJavaOrExtendedProxyInstanceFromRexx=[%p]\n", struct_JVM->mid_RexxProxy_newJavaOrExtendedProxyInstanceFromRexx);
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxProxy_setBsfRegistry =[%p]\n",                        struct_JVM->mid_RexxProxy_setBsfRegistryKey );

   fprintf(stderr, "\tclz_String       =[%p]\n", struct_JVM->clz_String);
   fprintf(stderr, "\t\tstruct_JVM->mid_String_initFromByteArray=[%p]\n", struct_JVM->mid_String_initFromByteArray);
   fprintf(stderr, "\t\tstruct_JVM->mid_String_getBytes         =[%p]\n", struct_JVM->mid_String_getBytes         );

   fprintf(stderr, "\tclz_Throwable    =[%p]\n", struct_JVM->clz_Throwable);

   fprintf(stderr, "\n\temptyUTFString   =[%p]\n", struct_JVM->jstring_emptyUTFString);

   fprintf(stderr, "\n--- 2012-02-07, rgf:\n");
   fprintf(stderr, "\tclz_RexxConfiguration =[%p]\n", struct_JVM->clz_RexxConfiguration);
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxConfiguration_getExitHandler   =[%p]\n",   struct_JVM->mid_RexxConfiguration_getExitHandler);
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxConfiguration_getCommandHandler=[%p]\n",   struct_JVM->mid_RexxConfiguration_getCommandHandler);

#ifndef CONFIG_REXX_HANDLERS_AS_INTERFACES    // rgf, 2012-02-17
   fprintf(stderr, "\tclz_RexxExitHandler   =[%p]\n", struct_JVM->clz_RexxExitHandler);
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxExitHandler_handleExit         =[%p]\n",   struct_JVM->mid_RexxExitHandler_handleExit);

   fprintf(stderr, "\tclz_RexxCommandHandler=[%p]\n", struct_JVM->clz_RexxCommandHandler);
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxCommandHandler_handleCommand   =[%p]\n",   struct_JVM->mid_RexxCommandHandler_handleCommand);
#endif

   fprintf(stderr, "---\n");
   fflush(stderr);

#endif

   RgfReleaseLock2(RII_creation_lock);      // 2012-12-30, rgf: unlock

}

    // uninitialize (free) JVM-dependent data we always need in all threads
inline void uninit_STRUCT_JVM(JNIEnv *env, PSTRUCT_JVM struct_JVM)
{
#if defined ( DEBUG_JNI )
   fprintf(stderr, "uninit_STRUCT_JVM(), 1 ...\n");
#endif

   RgfAcquireLock2(RII_creation_lock);      // 2012-12-30, rgf: lock access to list of RexxInstance instances to this thread
   if (struct_JVM->jvm==NULL)   // no JVM, no uninits necessary
   {
       RgfReleaseLock2(RII_creation_lock);      // 2012-12-30, rgf: unlock
       return;
   }

        // free global references for these important objects
    env->DeleteGlobalRef(struct_JVM->clz_Object);
    env->DeleteGlobalRef(struct_JVM->clz_RexxAndJava);
    env->DeleteGlobalRef(struct_JVM->clz_RexxException);

#if defined (CONFIG_HANDLE_NETREXX_AS_STRING_4_REXX)
    if (struct_JVM->clz_NetRexx!=NULL)
    {
        env->DeleteGlobalRef(struct_JVM->clz_NetRexx);
    }
#endif

    env->DeleteGlobalRef(struct_JVM->clz_RexxProxy);
    env->DeleteGlobalRef(struct_JVM->clz_String);
    env->DeleteGlobalRef(struct_JVM->clz_Throwable);

    env->DeleteGlobalRef(struct_JVM->jstring_emptyUTFString);

    // rgf, 2012-02-07 --begin
    env->DeleteGlobalRef(struct_JVM->clz_RexxConfiguration);

#ifndef CONFIG_REXX_HANDLERS_AS_INTERFACES    // rgf, 2012-02-17
    env->DeleteGlobalRef(struct_JVM->clz_RexxExitHandler);
    env->DeleteGlobalRef(struct_JVM->clz_RexxCommandHandler);
#endif
    // rgf, 2012-02-07 --end


    if (struct_JVM->primodal_rajo!=NULL)    //
    {
        env->DeleteGlobalRef(struct_JVM->primodal_rajo);
    }

        // set whole structure to NULL
    memset(struct_JVM, 0,size_STRUCT_JVM);  // set everything to 0

    currentJVM=NULL;                        // global var: reset
    bsfInvokedBy=0;                         // global var: reset indicator

#if defined ( DEBUG_JNI )
    fprintf(stderr, "uninit_STRUCT_JVM(), 2: env->ExceptionCheck()=[%d]\n", env->ExceptionCheck());
#endif

    RgfReleaseLock2(RII_creation_lock);      // 2012-12-30, rgf: unlock
}





// RexxProxy ===========================================================================================

        // store ProxyObject in BSFRegistry, fetch the String value that was used as the key and return it
        // if 'bReturnWithPrefix'==1, then return optional "<O>", if present
inline jstring RgfStoreRexxProxyInBsfRegistry(JNIEnv *env, jobject rajo, jobject proxy, int bReturnWithPrefix)
{
        // create beanname and register bean with BSF registry
    jstring j_keyValue=(jstring) env->CallObjectMethod(rajo,
                                                       defaultJVM->mid_RexxAndJava_makeString4Rexx,
                                                       proxy);
    jstring j_keyValueWithoutPrefix=j_keyValue;


    // a RexxProxy, save its registry key with the instance
    if (!env->ExceptionCheck() &&
         env->IsInstanceOf(proxy, defaultJVM->clz_RexxProxy) )
    {
        char * c_keyValue=NULL;
        c_keyValue=(char *) JNU_GetStringNativeChars(env, j_keyValue);

        if (c_keyValue[0]=='<' && c_keyValue[2]=='>')   // type indicator in hand, remove it
        {
            j_keyValueWithoutPrefix=env->NewStringUTF((char *) c_keyValue+3);
        }
        RexxFreeMemory(c_keyValue);     // free memory

            // save generated beanname with RexxProxy
        env->CallObjectMethod(proxy,
                              defaultJVM->mid_RexxProxy_setBsfRegistryKey,
                              j_keyValueWithoutPrefix);
    }

    if (bReturnWithPrefix==1)
    {
        return j_keyValue;
    }

    return j_keyValueWithoutPrefix;
}



    // allows to unregister a bean from the BSFRegistry
inline jboolean RgfUnregisterBean(JNIEnv *env, jobject rajo, jobject beanName)
{
    jboolean bRes=env->CallBooleanMethod(rajo,
                                  defaultJVM->mid_RexxAndJava_unregisterBean,
                                  beanName);
    return bRes;
}





    // create RexxProxy object and its package to the OREXX_REGISTRY, create RexxProxy
    // according to RMG (20090511) the package object is not meant to (and cannot be) used as a "context" under which an object executes
inline jobject RgfCreateRexxProxy (JNIEnv *env, jobject rajo,
                                        RexxThreadContext *rtc,
                                        RexxObjectPtr robj,
                                        RexxPackageObject rpo,
                                        RexxObjectPtr userData)
{

#if defined (DEBUG40) || defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "\t--> RgfCreateRexxProxy(): env=[%p], rajo=[%p], robj=[%p], rpo=[%p], userData=[%p] ...\n",
                                               env, rajo, robj, rpo, userData);
    fflush(stderr);
#endif

        // get RexxEngine instance
    jobject j_rexxEngine=env->CallObjectMethod(rajo,
                                             defaultJVM->mid_RexxAndJava_getRexxEngine,
                                             NULL);

    const char * c_objID         =RgfAddProxyObject(rtc, robj, rpo);     // add to OREXX registry, save ID string

#if defined (DEBUG40) || defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "\t--> RgfCreateRexxProxy(): after adding RgfAddProxyObject(), c_objID=[%.256s] ...\n", c_objID);
    fflush(stderr);
#endif

    const char * c_objID_userData=NULL;

    if (userData!=NULL)     // userData object given, put it into OREXX registry, save ID string
    {
        c_objID_userData=RgfAddProxyObject(rtc, userData, rpo); // add to OREXX registry, save ID string

#if defined (DEBUG40) || defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "\t--> RgfCreateRexxProxy(): after adding RgfAddProxyObject(), c_objID_userData=[%.256s] ...\n", c_objID_userData);
    fflush(stderr);
#endif

    }

    // now create Java RexxProxy object!
    // const char c_rii_ID [ RGF_POINTER_STRING_WIDTH+1 ] = "";
    char *c_rii_ID=new char[RGF_POINTER_STRING_WIDTH];

    RgfPointer2String(rtc->instance, c_rii_ID);  // create a string representation of interpreter instance pointer
    jstring j_rii_ID=env->NewStringUTF(c_rii_ID);
    jstring j_objID =env->NewStringUTF(c_objID);
    jstring j_objID_userData=env->NewStringUTF(c_objID_userData);

    jobject j_rexxProxy=env->NewObject(defaultJVM->clz_RexxProxy,
                                     defaultJVM->mid_RexxProxy_init,
                                               j_rii_ID,
                                               j_objID,
                                               j_objID_userData,
                                               j_rexxEngine);

    env->DeleteLocalRef(j_rii_ID);
    env->DeleteLocalRef(j_objID);
    env->DeleteLocalRef(j_objID_userData);

        // now register RexxProxy object with BSFRegistry and save beanName with the RexxProxy
    env->DeleteLocalRef(j_rexxEngine);

    delete [] c_rii_ID;

#if defined (DEBUG40) || defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "\t<-- RgfCreateRexxProxy(): returning j_rexxProxy=[%p]\n", j_rexxProxy);
    fflush(stderr);
#endif

    return j_rexxProxy;
}


// RexxException ===========================================================================================
        // values from org.apache.bsf.BSFException
const int32_t REASON_INVALID_ARGUMENT    = 0;
const int32_t REASON_IO_ERROR            = 10;
const int32_t REASON_UNKNOWN_LANGUAGE    = 20;
const int32_t REASON_EXECUTION_ERROR     = 100;
const int32_t REASON_UNSUPPORTED_FEATURE = 499;
const int32_t REASON_OTHER_ERROR         = 500;

    // create a Java RexxException
inline jthrowable RgfCreateRexxException4Java (JNIEnv *env, jobject rajo,
                                       RexxThreadContext *rtc,
                                       RexxObjectPtr conditionObject,
                                       // RexxPackageObject rpo,
                                       int32_t      reason,
                                       const char * message
                                       )
{

#if defined(DEBUG40)
   fprintf(stderr, "\t-->\tRgfCreateRexxException4Java() - start ...\t env=[%p], rajo=[%p], rtc=[%p], conditionObject=[%p]\n",
                     env, rajo, rtc, conditionObject);
#endif

        // get class object for RexxException, locate its constructor
#if defined(DEBUG40)
   fprintf(stderr, "\t-->\tRgfCreateRexxException4Java() - create RgfCreateRexxProxy ...\n");
#endif

    jobject jConditionObject= (conditionObject==NULL ? NULL :
                                                       RgfCreateRexxProxy(env, rajo, rtc, conditionObject,
                                                                               NULL,    // packageObject
                                                                               NULL     // userData
                                                                          )
                               );

#if defined(DEBUG40)
   fprintf(stderr, "\t-->\tRgfCreateRexxException4Java() - create RexxException ...\n");
   fprintf(stderr, "\t-->\tRgfCreateRexxException4Java() - \tclz=[%p], mid=[%p], reason=[%d], message=[%.256s], jConditionObject=[%p].\n",
    defaultJVM->clz_RexxException, defaultJVM->mid_RexxException_init, reason, message, jConditionObject);
//                             clz, mid, reason, message, jConditionObject);

   fprintf(stderr, "\t-->\tRgfCreateRexxException4Java() - \tmessage=<<<[%.256s]>>>\n",message);

   fflush(stderr);
#endif

        // public RexxException (int reason, String message, RexxProxy rexxCondition)
    jstring jMessage=env->NewStringUTF(message);
    jthrowable thr=(jthrowable) env->NewObject(defaultJVM->clz_RexxException,
                                               defaultJVM->mid_RexxException_init,
                                                          jint(reason),
                                                          jMessage,
                                                          jConditionObject
                                               );

    env->DeleteLocalRef(jMessage);
    env->DeleteLocalRef(jConditionObject);

#if defined(DEBUG40)
   fprintf(stderr, "\t-->\tRgfCreateRexxException4Java() - end ...\n<-------\n");
   fflush(stderr);
#endif
    return thr;     // return throwable
}


    // RgfValue4JavaIndicator() checks RexxObject and returns:
    //             -2 for .nil,
    //             -1 for NULL,
    //             >=0 a RexxObject:
    //                   0 any RexxObject that is not one of:
    //                     1 for a RexxString or
    //                     2 for a beanname (Java object resides in BSF registry)
inline int RgfValue4JavaIndicator(RexxThreadContext *rtc, RexxObjectPtr robj)    // <=== <=== <===
{
#if defined( DEBUG2 )
    fprintf(stderr, "... >>> arrived: (RgfValue4JavaIndicator): rtc=[%p], robj=[%p]\n", rtc, robj); fflush(stderr);
#endif
    if (robj==NULL)
    {
#if defined( DEBUG2 )
    fprintf(stderr, "... <<<          (RgfValue4JavaIndicator): returning '-1' -> NULL !\n");fflush(stderr);
#endif
        return -1;  // indicate NULL
    }

    if (robj==rtc->Nil())
    {
#if defined( DEBUG2 )
    fprintf(stderr, "... <<<          (RgfValue4JavaIndicator): returning '-2' -> .nil !\n");fflush(stderr);
#endif
        return -2;  // indicate .nil
    }

    if (rtc->IsOfType(robj, "STRING"))
    {

#if defined( DEBUG2 )
    fprintf(stderr, "... <<<          (RgfValue4JavaIndicator): returning '1' -> plain string\n");fflush(stderr);
#endif
        return 1;   // indicate plain string
    }

    if (   rtc->IsOfType(robj, "BSF"      ) ||
           rtc->HasMethod(robj, "UNO.GETDEFINITION") ||   // an UNO_PROXY ?
         ( rtc->IsOfType(robj, "CLASS") && rtc->HasMethod(robj, "BSF.LOADCLASS") )
       )
    {
#if defined( DEBUG2 )
    fprintf(stderr, "... <<<          (RgfValue4JavaIndicator): returning '2' -> use beanName for lookup\n");fflush(stderr);
#endif
        return 2;   // use objectname (=beanName for BSFRegistry)
    }

#if defined( DEBUG2 )
    fprintf(stderr, "... <<<          (RgfValue4JavaIndicator): returning '0' -> create RexxProxy()]\n");fflush(stderr);
#endif

    return 0;       // indicates a RexxProxy should be created
}



    // process return value to Java: if a RexxStringObject in hand, return a jstring,
    //                               if NULL, NULLOBJECT or .nil, return NULL
    //                               else create and return a RexxProxy
inline jobject RgfProcessReturnValue4Java(JNIEnv *env, jobject rajo,
                                       RexxThreadContext *rtc,
                                       RexxObjectPtr     robj,
                                       jstring           j_returnType
                                       )
{
    // get the indicator value; returns "2" (BSF/UNO_PRoxy), "1" (String), "0" (RexxProxy),
    // "-1" (NULL), "-2" (.nil)
    int typeIndicator=RgfValue4JavaIndicator(rtc, robj);

    if (typeIndicator<0)        // NULL or .nil
    {
#if defined ( DEBUG_RETURN_VALUE_FOR_JAVA )
   fprintf(stderr, "... RgfProcessReturnValue4Java(): returning NULL \n"); fflush(stderr);
#endif
        return NULL;            // will be turned into .nil
    }


    // if RexxObject is an instance of the .String class, then return a jstring,
    // else return a RexxProxy

    if (typeIndicator==1)       // a .String object in hand
    {
#if defined ( DEBUG_RETURN_VALUE_FOR_JAVA )
   fprintf(stderr, "... RgfProcessReturnValue4Java(): a String in hand, returning it \n"); fflush(stderr);
#endif

        jstring jStringValue=env->NewStringUTF(rtc->CString((RexxStringObject) robj));
        if (j_returnType==NULL)     // no (primitive/wrapper) return type defined
        {
            return jStringValue;    // return a jstring
        }

        jobject   jobj=env->CallObjectMethod(rajo,
                                             defaultJVM->mid_RexxAndJava_convFromJNI,
                                             j_returnType, jStringValue);  // do the conversion

        env->DeleteLocalRef(jStringValue);
        return jobj;
    }


        // treat BSF4Rexx Java proxies as strings (RexxAndJava handles them appropriately)
    if (typeIndicator>1)
    {
#if defined ( DEBUG_RETURN_VALUE_FOR_JAVA )
   fprintf(stderr, "... RgfProcessReturnValue4Java(): a BSF or UNO_PROXY in hand, returning bean_name=[%.256s] \n",
                   rtc->CString((RexxStringObject) rtc->SendMessage0(robj, "OBJECTNAME")));
   fflush(stderr);

#endif

        jstring jbeanName=env->NewStringUTF(rtc->CString((RexxStringObject) rtc->SendMessage0(robj, "OBJECTNAME")));
        jobject   jobj=env->CallObjectMethod(rajo,
                                             defaultJVM->mid_RexxAndJava_convFromRexx,
                                             jbeanName);  // invoke toString()
        env->DeleteLocalRef(jbeanName);

        // TODO: check whether clearing exception is truly o.k.
        if ( env->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
        {
            env->ExceptionDescribe();
            env->ExceptionClear();
        }
        return jobj;
    }

#if defined ( DEBUG_RETURN_VALUE_FOR_JAVA )
   fprintf(stderr, "... RgfProcessReturnValue4Java(): creating a RexxProxy and returning it \n"); fflush(stderr);
#endif


        // any other type of Object Rexx object will cause a RexxProxy to be created and returned;
        // this will allow Java to interact with the proxied Rexx object
    return RgfCreateRexxProxy(env, rajo, rtc, robj, NULL, NULL);
}



// 2012-02-107, rgf: handle single jobject value to be turned into RexxObjectPtr value
inline RexxObjectPtr RgfJavaObject2RexxObject (JNIEnv *env, jobject rajo,     // RexxAndJava object, needed for registering Java objects
                                                  RexxThreadContext *rtc,   // RexxThreadContext
                                                  jobject j_obj,    // Java object to turn into a RexxObjectPtr
                                                  int bLoadBSF      // if no .BSF class is found, should we load it?
                                    )
{
    RexxInstance *riid=rtc->instance;

#if defined(DEBUG_RGF_PROCESS_J_ARG1)
    fprintf(stderr, "RgfJavaObject2RexxObject(...): riid=[%p]; ExceptionCheck()=[%d]...\n", riid, (int) env->ExceptionCheck()   );fflush(stderr);
#endif
    RexxObjectPtr res=NULL;

    if (j_obj==NULL) {  // if NULL return NULL
        return res;
    }

        // determine whether BSF.CLS got loaded
    RexxObjectPtr clzBSF=rgfGetEntryFromLocal (rtc, "BSF");  // .local~BSF
    logical_t  bClzBsf=(clzBSF!=rtc->Nil());    // determine whether we have .BSF available

    if (!bClzBsf && bLoadBSF)       // if BSF.CLS was not required yet, require it now!
    {
#if defined(DEBUG_RGF_PROCESS_J_ARG1)
   fprintf(stderr, "... RgfJavaObject2RexxObject(...): riid=[%p], about to require BSF.CLS rtc->CheckCondition()=[%d], RUNTIME (PRE-)LOADING OF BSF.CLS ! | [1] ...\n", riid, (int) rtc->CheckCondition());fflush(stderr);
#endif
        rtc->ReleaseLocalReference(clzBSF);

        RexxObjectPtr result_obj=NULL;
        // RexxRoutineObject rro=rtc->NewRoutine("FROM_BSF4ooRexx_DLL", "::requires BSF.CLS", 18);
        char code[]="::requires BSF.CLS";
        RexxRoutineObject rro=rtc->NewRoutine("FROM_BSF4ooRexx_RgfJavaObject2RexxObject", code, strlen(code));
        if (rro!=NULL)      // object got created, let us run it
        {
            result_obj=rtc->CallRoutine(rro, NULL);   // call the program ?? NULL correct ??
            rtc->ReleaseLocalReference(rro);
            rtc->ReleaseLocalReference(result_obj);
        }

#if defined(DEBUG_RGF_PROCESS_J_ARG1)
    if (rtc->CheckCondition())
    {
       RexxDirectoryObject condObj=rtc->GetConditionInfo();
       char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "RgfJavaObject2RexxObject(), DEBUG_RGF_PROCESS_J_ARG1, error 789");

                // make a Java error message out of it
    #if defined (DEBUG_RGF_PROCESS_J_ARG1)
           fprintf(stderr, "... RgfJavaObject2RexxObject(...): riid=[%p], condition()=[%s].\n", riid, msg);fflush(stderr);
           RexxFreeMemory(msg);
    #endif

    }
#endif
        clzBSF=rgfGetEntryFromLocal (rtc, "BSF");  // .local~BSF
        bClzBsf=(clzBSF!=rtc->Nil());
    }

#if defined(DEBUG_RGF_PROCESS_J_ARG1)
    fprintf(stderr, "... RgfJavaObject2RexxObject(...): riid=[%p], after getting .BSF, ExceptionCheck()=[%d], bClzBsf=[%d]...\n", riid, (int) env->ExceptionCheck(), (int) bClzBsf);fflush(stderr);
    fprintf(stderr, "... RgfJavaObject2RexxObject(...): riid=[%p], j_obj=[%p]\n", riid, j_obj);fflush(stderr);
#endif

        // a Java RexxProxy, then return RexxObjectPtr itself
    if (env->IsInstanceOf(j_obj, defaultJVM->clz_RexxProxy)==JNI_TRUE)  // a Rexx proxy, get the real object and supply it
    {

#if defined(DEBUG_RGF_PROCESS_J_ARG1)
    fprintf(stderr, "... RgfJavaObject2RexxObject(...): riid=[%p]: a RexxProxy object !\n", riid);fflush(stderr);
#endif
        char *tmpStr=NULL;
        jstring j_obj_ID=(jstring) env->CallObjectMethod(j_obj,
                                               defaultJVM->mid_RexxProxy_getRexxObjectID,
                                               NULL); // get obj_ID

        tmpStr=(char *) JNU_GetStringNativeChars(env, j_obj_ID);    // convert to native string

#if defined(DEBUG_RGF_PROCESS_J_ARG1)
    fprintf(stderr, "                       riid=[%p], tmpStr =[%.256s], ExceptionCheck()=[%d]    \n", riid, tmpStr, env->ExceptionCheck());fflush(stderr);
#endif

        RexxStringObject rso=rtc->String(tmpStr);
        res=RgfGetProxyObject(rtc, rso);  // get Rexx object
        rtc->ReleaseLocalReference(rso);
        rtc->ReleaseLocalReference(clzBSF);
        // res=RgfGetProxyObject(rtc, rtc->String(tmpStr));  // get Rexx object

        env->DeleteLocalRef(j_obj_ID);
        RexxFreeMemory(tmpStr);             // make sure we free the allocated memory
        return res;
    }


// 2012-03-11, rgf: check for NetRexx object in hand; if so replace it with its toString() value
#if defined (CONFIG_HANDLE_NETREXX_AS_STRING_4_REXX)
    if (defaultJVM->clz_NetRexx!=NULL)
    {
        if (env->IsInstanceOf(j_obj, defaultJVM->clz_NetRexx)==JNI_TRUE)
        {
            j_obj=env->CallObjectMethod(j_obj, defaultJVM->mid_NetRexx_toString);
        }
    }
#endif

        // if a jstring, get the RexxString value
    if (env->IsInstanceOf(j_obj, defaultJVM->clz_String)==JNI_TRUE )  // ---rgf, 2012-02-07
    {
        res=JNU_GetStringNativeCharsReturningRexxStringObject(env, (jstring) j_obj, rtc);

#if defined(DEBUG_RGF_PROCESS_J_ARG1)
    fprintf(stderr, "... RgfJavaObject2RexxObject(...): riid=[%p], .nil=[%p], res=[%p], res~string=[%.256s]:  ExceptionCheck()=[%d]\n", riid, rtc->Nil(), res, rtc->CString(rtc->ObjectToString(res)),env->ExceptionCheck());fflush(stderr);
#endif
    }

    else // a Java object, process it via RexxAndJava
    {
        if (rajo==NULL)     // not supplied ?
        {
                // get the RII structure to get access to
            RexxInstance *ri=rtc->instance;
            PSTRUCT_RII struRii=RgfGetRexxInterpreterInstanceStructFromList(ri);
            rajo=struRii->rajo;

#if defined(DEBUG_RGF_PROCESS_J_ARGS)
     fprintf(stderr, "... RgfJavaObject2RexxObject(...): !! rajo was null, now set to: [%p]\n", rajo);
#endif
        }

            // create a bean of the Java object
        jstring j_beanName = (jstring) env->CallObjectMethod(rajo,
                                           defaultJVM->mid_RexxAndJava_registerBean4JNI,
                                           j_obj);

            // get char* beanName, do a bsf.wrap() if available; return that result
        const char * c_beanName    = env->GetStringUTFChars(j_beanName, JNI_FALSE);
        res=       rtc->String(c_beanName);    // create Rexx string
        env->ReleaseStringUTFChars(j_beanName, c_beanName);      // release
        env->DeleteLocalRef(j_beanName);
    }


    if (bClzBsf)    // .local~bsf available, use its bsf.wrap class method
    {
#if defined(DEBUG_RGF_PROCESS_J_ARG1)
    fprintf(stderr, "... RgfJavaObject2RexxObject(...): riid=[%p], ClzBsf~string=[%.256s]\n", riid, rtc->CString(rtc->ObjectToString(clzBSF)));fflush(stderr);
    fprintf(stderr, "... RgfJavaObject2RexxObject(...): riid=[%p], .local~bsf~bsf.null_string=[%.256s] \n",riid,rtc->CString(rtc->SendMessage0(clzBSF, "BSF.NULL_STRING")));fflush(stderr);
    fprintf(stderr, "... RgfJavaObject2RexxObject(...): riid=[%p]: .local~bsf~bsf.wrap(obj) \n", riid);fflush(stderr);
#endif

            // blocked; unguarded class method; probably rtc->SetGuardOff() would have worked as well
        RexxObjectPtr rop=res;
        res=(RexxStringObject) rtc->SendMessage1(clzBSF, "BSF.WRAP", rop); // try to wrap it up as a Rexx proxy
        rtc->ReleaseLocalReference(rop);
        // res=(RexxStringObject) rtc->SendMessage1(clzBSF, "BSF.WRAP", res); // try to wrap it up as a Rexx proxy
    }

#if defined(DEBUG_RGF_PROCESS_J_ARG1)
    fprintf(stderr, "... RgfJavaObject2RexxObject(...): riid=[%p], at end, returning=[%p], ExceptionCheck()=[%d]...\n", riid, res, env->ExceptionCheck());fflush(stderr);
#endif

    rtc->ReleaseLocalReference(clzBSF);

    return res;
}




    // process args for Rexx, return RexxArray for further processing
    // "j_args" contains either Strings (values or beanNames) or RexxProxy objects
    // "bLoadBSF" will only be TRUE, if this routine is invoked from ...JniRexxSendMessageToRexxObject(...)
    // 20150721, rgf: added "bIncr": if 1 then let BSF.WRAP run "rawRegister" to increase reference
    //                counter in BSF registry in use cases where jniRexxRunProgram() or
    //                jniRexxSendMessageToRexx() is employed (they register and unregister the Java objects;
    //                BSF_PROXY will by default not register, but unregister)
inline RexxArrayObject RgfProcessJArgs (JNIEnv *env, RexxThreadContext *rtc,
                                                     jobjectArray j_args,           // args from Java
                                                     RexxDirectoryObject slotDir,    // slotDir with userData & methodName in original case
                                                     int bLoadBSF,      // if no .BSF class is found, should we load it?
                                                     int bIncr          // let BSF.WRAP increase the BSF registry ref counter?
                                                     )
{
#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "\n---- just arrived in RgfProcessJArgs(...): env=[%p], rtc=[%p], j_args=[%p], slotDir=[%p], bLoadBSF=[%d], bIncr=[%d]\n",
                                                                  env,      rtc,      j_args,      slotDir,      bLoadBSF,      bIncr);
    fflush(stderr);
#endif

    jint len          = (j_args==NULL ? 0 : env->GetArrayLength(j_args));

    RexxInstance *riid=rtc->instance;

#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "RgfProcessJArgs(...): riid=[%p], len=[%d] + [%d]; ExceptionCheck()=[%d]...\n", riid, (int) len, (slotDir==NULL ? 0 : 1), (int) env->ExceptionCheck()   );fflush(stderr);
#endif
    RexxArrayObject ra=rtc->NewArray(len+ (slotDir==NULL ? 0 : 1) );

    if (j_args==NULL && slotDir==NULL)  // return empty arg, if no args, nor slotDir given
    {
#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "RgfProcessJArgs(...): no j_args nor slotDir given, return empty array!\n");
#endif

        return ra;      // for jniRexxRunProgram; TODO: really necessary or returning NULL/OBJECT sufficient already?
    }



#if defined(DEBUG_RGF_PROCESS_J_ARGS )
    fprintf(stderr, "RgfProcessJArgs(...): riid=[%p]\n\t---> len=[%d], len=[%d], len+(slotDir==NULL ? 0 : 1)=[%d], [%lu]\n\tCheckCondition=[%d] <---\n",
                    riid, (int) len, (int) len, (int) len+(slotDir==NULL ? 0 : 1) ,(size_t) (len+ (slotDir==NULL ? 0 : 1)),(int) rtc->CheckCondition());fflush(stderr);
    fprintf(stderr, "\triid=[%p] ---> ra~size=[%lu], ra~items=[%lu]\n",riid, rtc->ArraySize(ra),rtc->ArrayItems(ra));fflush(stderr);
    fprintf(stderr, "\tj_args.length=[%d]\n", env->GetArrayLength(j_args));

    jsize size=env->GetArrayLength(j_args);
    for (jsize i=0; i<size; i++)
    {
       fprintf(stderr, "\tj_args[%d/%d]: ", i, size); fflush(stderr);
       jobject jo=env->GetObjectArrayElement(j_args, i);
       if (jo==NULL)
       {
           fprintf(stderr, "(null)\n");
       }
       else
       {
           char *str=new char[256];
           JAVA_TO_STRING(env, jo, str, 256);
           fprintf(stderr, "[%.256s]\n", str);
           delete[] str;
       }
    }
    fprintf(stderr, "<---TheEndOf: j_args--->\n");
    fflush(stderr);
#endif

    RexxObjectPtr clzBSF=rgfGetEntryFromLocal (rtc, "BSF");  // .local~BSF
    logical_t  bClzBsf=(clzBSF!=rtc->Nil());    // determine whether we have .BSF available

#if defined(DEBUG_RGF_PROCESS_J_ARGS )
   fprintf(stderr, "**** //1// bClzBsf=[%d], rtc->Nil()=[%p], clzBSF=[%p]\n", bClzBsf, rtc->Nil(), clzBSF);

   if (clzBSF!=NULL)
   {
       fprintf(stderr, "**** //1a// clzBSF~string=[%s]\n", rtc->CString(rtc->SendMessage0(clzBSF,"STRING")));
       fflush(stderr);

    // as of 2012-02-09, rgf: bombs! not anymore with 4.1.1, 20110222
       fprintf(stderr, "**** //1b// clzBSF~string=[%s] (->ObjectToStringValue)\n", rtc->ObjectToStringValue(clzBSF));
       fprintf(stderr, "**** //1c// clzBSF~string=[%s] (->ObjectToString)\n", rtc->CString(rtc->ObjectToString(clzBSF)));
       fflush(stderr);
   }
#endif

    if (!bClzBsf && bLoadBSF)       // rgf, 2009-09-26: if BSF.CLS was not required yet, require it now!
    {

#if defined(DEBUG_RGF_PROCESS_J_ARGS)
   fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], rtc->checkCondition()=[%d], RUNTIME (PRE-)LOADING OF BSF.CLS ! | [1] ...\n", riid, (int) rtc->CheckCondition());fflush(stderr);
#endif

        RexxObjectPtr result_obj=NULL;
        RexxRoutineObject rro=rtc->NewRoutine("FROM_BSF4ooRexx_DLL", "::requires BSF.CLS", 18);
        if (rro!=NULL)      // object got created, let us run it
        {
            result_obj=rtc->CallRoutine(rro, ra);   // call the program
            rtc->ReleaseLocalReference(result_obj);
            rtc->ReleaseLocalReference(rro);
        }

#if defined(DEBUG_RGF_PROCESS_J_ARGS)
if (rtc->CheckCondition())
{
   RexxDirectoryObject condObj=rtc->GetConditionInfo();
   char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "RgfProcessJArgs(), DEBUG_RGF_PROCESS_J_ARGS, error 789");
            // make a Java error message out of it
#if defined (DEBUG_RGF_PROCESS_J_ARGS)

   fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], condition()=[%s].\n", riid, msg);fflush(stderr);
   RexxFreeMemory(msg);
#endif

}
#endif

        clzBSF=rgfGetEntryFromLocal (rtc, "BSF");  // .local~BSF
        bClzBsf=(clzBSF!=rtc->Nil());
    }


#if defined (RGF_UNO_WRAP)
    logical_t  bClzUno  =false;
    RexxObjectPtr clzUNO=NULL;

        #if defined(DEBUG_RGF_PROCESS_J_ARGS_UNO)
        fprintf(stderr, "... RgfProcessJArgs(...): 1 - riid=[%p], bClzBsf=[%d], bClzUno=[%d]\n", riid, bClzBsf, bClzUno);fflush(stderr);
        #endif

    if (bClzBsf)            // if BSF there, by any chance is there also UNO there? If so, use UNO.WRAP instead of BSF.WRAP
    {
        clzUNO=rtc->FindClass("UNO");

        fprintf(stderr, "... RgfProcessJArgs(...): -1b riid=[%p], clzUNO=[%p]\n",  riid, clzUNO);
        fflush(stderr);

        bClzUno=(clzUNO!=NULL);          // determine whether we have .UNO available
        if (clzUNO==NULL)
        {
            clzUNO=rtc->Nil();
        }

        fprintf(stderr, "... RgfProcessJArgs(...): -1c riid=[%p], clzUNO~string=[%.256s]\n", riid,
                             rtc->CString(rtc->ObjectToString(clzUNO)) );
        fflush(stderr);
        fprintf(stderr, "... RgfProcessJArgs(...): -1d riid=[%p], clzUNO=[%p]\n",  riid, clzUNO);
        fflush(stderr);
    }
        #if defined(DEBUG_RGF_PROCESS_J_ARGS_UNO)
    fprintf(stderr, "... RgfProcessJArgs(...): 2 - riid=[%p], bClzBsf=[%d], bClzUno=[%d]\n", riid, bClzBsf, bClzUno);fflush(stderr);
        #endif
#endif

// -- if bClzBsf==true, then:
//    rgf, 2010-06-07: TODO ? if .BSF available, check for .UNO, if available, run "uno.wrap" instead of "bsf.wrap"
//                            created class method "uno.wrap" to allow for future enhancement

#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], jint len=[%d], ExceptionCheck()=[%d], bClzBsf=[%d]...\n", riid, (int) len, (int) env->ExceptionCheck(), (int) bClzBsf);fflush(stderr);
#endif

    jobject rajo=NULL;  // 2012-02-09, rgf: new possibility: "pure" Java-objects (e.g. by handlers) which
                        //                  need to be registered in the BSF registry via RexxAndJava;
                        //                  only retrieved via RII structure, if needed

    for (int i=0; i<len; ++i)
    {

#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], j_args=[%p], i=[%d]<len=[%d]\n",
                    riid, j_args, i, len);fflush(stderr);
#endif

        jobject jobj=env->GetObjectArrayElement(j_args, i); // get array element

#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], fetched jobj=[%p]\n", riid, jobj);fflush(stderr);
#endif

        RexxObjectPtr robj;

        if (jobj==NULL)      // not given, hence omitted; if null, then ".NIL" will be sent anyway
        {

#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], arg#=[%d]: jobj==NULL, using .nil\n", riid, i);fflush(stderr);
#endif
            robj=rtc->Nil();                // use .nil
        }

        // a Rexx proxy? use the embedded Rexx object
        else if (env->IsInstanceOf(jobj, defaultJVM->clz_RexxProxy)==JNI_TRUE)
        {

#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], arg#=[%d]: a RexxProxy object\n", riid, i);fflush(stderr);
#endif
                // get RexxObjectID (a jstring representing the identity-hash
            jstring j_obj_ID=(jstring) env->CallObjectMethod(jobj,
                                                   defaultJVM->mid_RexxProxy_getRexxObjectID,
                                                   NULL); // get obj_ID

                // turn into Rexx string, lookup and return the Rexx object
            char *tmpStr=(char *) JNU_GetStringNativeChars(env, j_obj_ID);    // convert to native string
#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "                       riid=[%p], tmpStr =[%.256s], ExceptionCheck()=[%d]    \n", riid, tmpStr, env->ExceptionCheck());fflush(stderr);
#endif

            robj=RgfGetProxyObject(rtc, rtc->String(tmpStr));  // get Rexx object
            env->DeleteLocalRef(j_obj_ID);
            RexxFreeMemory(tmpStr);             // make sure we free the allocated memory
        }

        else // 2012-02-09, rgf: either a Java string, possibly denoting a bean, or a Java object
        {
//-------------------------------
#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], arg#=[%d]: NOT a RexxProxy!\n", riid, i);fflush(stderr);
#endif
                // if a jstring, get the RexxString value
            if (env->IsInstanceOf(jobj, defaultJVM->clz_String)==JNI_TRUE )  // ---rgf, 2012-02-07
            {
#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], arg#=[%d]: a Java String!\n", riid, i);fflush(stderr);
#endif
                robj=JNU_GetStringNativeCharsReturningRexxStringObject(env, (jstring) jobj, rtc);

            }

            else // a Java object, process it via RexxAndJava
            {
#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], arg#=[%d]: a Java object!\n", riid, i);fflush(stderr);
#endif
                if (rajo==NULL)     // not yet fetched ?
                {
#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], arg#=[%d]: a Java object, rajo not fetched yet, going after it...\n", riid, i);fflush(stderr);
#endif
                        // get the RII structure to get access to
                    RexxInstance *ri=rtc->instance;
                    PSTRUCT_RII struRii=RgfGetRexxInterpreterInstanceStructFromList(ri);
                    rajo=struRii->rajo;

#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): !! \trajo was null, now set to: [%p]\n", rajo);
#endif
                }

                    // create a bean of the Java object
                jstring j_beanName = (jstring) env->CallObjectMethod(rajo,
                                                   defaultJVM->mid_RexxAndJava_registerBean4JNI,
                                                   jobj);

                    // get char* beanName, do a bsf.wrap() if available; return that result
                const char * c_beanName    = env->GetStringUTFChars(j_beanName, JNI_FALSE);
                robj=       rtc->String(c_beanName);    // create Rexx string
                env->ReleaseStringUTFChars(j_beanName, c_beanName);      // release
                env->DeleteLocalRef(j_beanName);
            }

            if (bClzBsf)    // .local~bsf available, use its bsf.wrap class method
            {
                    // try to wrap it up as a Rexx proxy;
                    // blocked; unguarded class method; probably rtc->SetGuardOff() would have worked as well
                RexxStringObject rso=(RexxStringObject) rtc->SendMessage2(clzBSF,
                                      "BSF.WRAP",
                                      robj,
                                      (bIncr==1 ? rtc->True() : rtc->False())
                                     );

                rtc->ReleaseLocalReference(robj);
                robj=(RexxStringObject) rso;
                // robj=(RexxStringObject) rtc->SendMessage1(clzBSF, "BSF.WRAP", robj); // try to wrap it up as a Rexx proxy
            }

#if defined(DEBUG_RGF_PROCESS_J_ARG1)
    RexxObjectPtr res=robj;
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], .nil=[%p], res=[%p], res~string=[%.256s]:  ExceptionCheck()=[%d]\n", riid, rtc->Nil(), res, rtc->CString(rtc->ObjectToString(res)),env->ExceptionCheck());fflush(stderr);
#endif
        }

            // set array entry
        rtc->ArrayPut(ra, robj, i+1);
        rtc->ReleaseLocalReference(robj);
        env->DeleteLocalRef(jobj);
    }

    if (slotDir!=NULL)  // was a slotDir supplied ?
    {

#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], adding slotDir=[%.256s], len=[%d]\n", riid, rtc->ObjectToStringValue(slotDir), (int) len);fflush(stderr);
#endif

        rtc->ArrayPut(ra, slotDir, len+1);        // always save slot Directroy as last argument
    }


#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], at end, returning=[%p], ExceptionCheck()=[%d]...\n", riid, ra, env->ExceptionCheck());fflush(stderr);
    fprintf(stderr, "\triid=[%p], ---> ra~size=[%lu]\n", riid, rtc->ArraySize(ra));fflush(stderr);
    fprintf(stderr, "\triid=[%p], ---> ra~items=[%lu]\n",riid, rtc->ArrayItems(ra));fflush(stderr);
#endif

    rtc->ReleaseLocalReference(clzBSF);
    return ra;
}




    // create error message text from Java Throwable and return it
    // using RexxStringObject return value (this way memory gets automatically reclaimed at destruction time)
inline RexxStringObject RgfGetJavaThrowableAsString (JNIEnv *env, RexxThreadContext *rtc)
{
     jthrowable jo=env->ExceptionOccurred();    // get the Exception object

     if (bShowErrorString==1)       // print the stack?
     {
         env->ExceptionDescribe();  // print the stack
     }
     env->ExceptionClear();         // clear exception in the JVM


     jclass joc=env->GetObjectClass(jo);    // get Throwable's class object
     // get method ID
     jmethodID jmidName=env->GetMethodID(joc, "toString", "()Ljava/lang/String;");
     jobject   jobj=env->CallObjectMethod(jo, jmidName, NULL);  // invoke toString()

     if ( env->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
     {
         env->ExceptionDescribe();
         env->ExceptionClear();
     }

        // get Java string
     const char *aha=env->GetStringUTFChars( (jstring) jobj, JNI_FALSE);
     RexxStringObject rso=rtc->String(aha);     // create RexxStringObject
     env->ReleaseStringUTFChars( (jstring) jobj, aha);

     env->DeleteLocalRef(jo);
     env->DeleteLocalRef(joc);
     env->DeleteLocalRef(jobj);

     return rso;
}



    // allocates and creates a Rexx-like error message
    // returned *msg must be freed
inline char * RgfCreateRexxlikeErrorInfo (RexxThreadContext *rtc, RexxDirectoryObject condObj, const char * header)
{
    rtc->ClearCondition();

    RexxCondition cond;
    rtc->DecodeConditionInfo(condObj, &cond);

    // char *msg=(char *)RexxAllocateMemory(1024);
    char *msg=(char *)RexxAllocateMemory( 4096 );
    msg[0]=0;

        // format message according to exception type
    if (rtc->SendMessage1(cond.conditionName, "EQUALS", rtc->String("SYNTAX"))==rtc->True()) // SYNTAX condition ?
    {
        RexxObjectPtr tbRop=rtc->SendMessage0(rtc->DirectoryAt(condObj, "TRACEBACK"), "FIRSTITEM"); // first traceback line=error line
        const char *line1="Rexx traceback line not available from the Rexx condition object (Rexx condition may have been caused by a call or message from Java to Rexx)";

        if (tbRop!=rtc->Nil())     // a traceback entry available
        {
            line1=(const char *) rtc->CString(tbRop) ; // first traceback line=error line
        }

        // char line2[2048]="";        // will only use 1024 of it
        char *line2=new char[2048];

        SNPRINTF(line2, 2048, "Error %d running %.512s line %d:  %.1024s",
                            (int) cond.rc,      (NULL != cond.program   && rtc->Nil() != cond.program   ? rtc->CString( cond.program )  : "program n/a")   ,
                            (int) cond.position,(NULL != cond.errortext && rtc->Nil() != cond.errortext ? rtc->CString( cond.errortext ): "errortext n/a") );

        // char line3[2048]="";        // will only use 1024 of it
        char *line3=new char[2048];

        SNPRINTF( line3, 2048, "Error %d.%d:  %.1024s", (int) (cond.code / 1000),
                                     (int) (cond.code % 1000),
                                     (NULL != cond.message && rtc->Nil() != cond.message ? rtc->CString( cond.message ) : "message n/a") );

        SNPRINTF( msg, 4096, "%.16s%.512s:\n%.1024s\n%.1024s\n%.1024s", DLLNAME, header, line1, line2, line3);

        delete[] line2;
        delete[] line3;
    }
    else
    {
        SNPRINTF( msg, 4096, "%.16s%.512s: Rexx condition [%.512s] raised",
                                  DLLNAME, header,
                                  (NULL != cond.conditionName && rtc->Nil() != cond.conditionName ? rtc->CString(cond.conditionName) : "conditionName n/a") );
    }

    return msg;
}





    // Double-linked list of RAJOs
typedef struct _STRUCT_RAJO {
    jobject                     rajo;               // RexxAndJava instance to communicate with
    int32_t                     refCounter;        // counter, if 0, safe to delete this node
    void                       *riid;               // Rexx interpreter instance pointer value: allow for finding JRSTs created because of a particular RIID (can be used for cleanup purposes, if RI gets terminated)
    struct _STRUCT_RAJO        *next;               // next node, if any
    struct _STRUCT_RAJO        *prev;               // previous node, if any
}  STRUCT_RAJO;

typedef STRUCT_RAJO *PSTRUCT_RAJO;                  // pointer to structure
long size_STRUCT_RAJO=sizeof(STRUCT_RAJO);          // get and memorize size of structure

PSTRUCT_RAJO pRoot_RAJO=NULL;               // root of RAJO list



    // Double-linked list of NODEs for TID2NODE list
typedef struct _STRUCT_NODE {
    void                             *belongsToTID2NODE;    // (void *) to PSTRUCT_TID2NODE to which this NODE belongs to
    PSTRUCT_RAJO                      rajoRef;      // reference to RAJO object
    int32_t                           nodeRefCounter;   // counter, if 0, safe to delete this node
    void                             *riid;         // Rexx interpreter instance pointer value: allow for finding JRSTs created because of a particular RIID (can be used for cleanup purposes, if RI gets terminated)
    struct _STRUCT_NODE              *next;         // pointer to next node, if any
    struct _STRUCT_NODE              *prev;         // pointer to previous node, if any

        // if this node depends on another TID's one (BsfAttachToTID(tid)
    struct _STRUCT_NODE              *dependsOn;    // pointer to node this depends on (... BsfAttachToTID(TID))
    struct _STRUCT_NODE              *nextDependent;// pointer to next dependent node, if any
    struct _STRUCT_NODE              *prevDependent;// pointer to previous dependent node, if any

        // this node has dependent nodes, point to first in (double linked) list
    struct _STRUCT_NODE              *firstDependent;   // pointer to next dependent node, if any
}  STRUCT_NODE;

typedef STRUCT_NODE *PSTRUCT_NODE;        // pointer to structure
long size_STRUCT_NODE=sizeof(STRUCT_NODE);// get and memorize size of structure



    // Double-linked list of TID2NODE mappings
typedef struct _STRUCT_TID2NODE {
#ifdef UNIX
    pthread_t                     tid;
#else
    TID                           tid;                // current TID
#endif
    PSTRUCT_NODE                  firstNode;          // associated with RAJO
    struct _STRUCT_TID2NODE      *next;              // pointer to next block, if any
    struct _STRUCT_TID2NODE      *prev;              // pointer to next block, if any
}  STRUCT_TID2NODE;

typedef STRUCT_TID2NODE *PSTRUCT_TID2NODE;          // pointer to structure
long size_STRUCT_TID2NODE=sizeof(STRUCT_TID2NODE);  // get and memorize size of structure
PSTRUCT_TID2NODE pRoot_TID2NODE=NULL;       // root of TID2NODE list

#ifndef RGF_ATTACH_NEW
        // structure for maintaining thread-related call information
    typedef struct _STRUCT_PARAM {
    #ifdef UNIX
        pthread_t
    #else
        TID
    #endif
                                      tid;              // current TID or NULL
        JNIEnv                       *env;              // current JNIEnv or NULL
        jobject                       rajo;             // current rajo or NULL
        void                         *riid;             // to Rexx instance ID

        int                           bDetach;          // TRUE: use DetachCurrentThread(), FALSE: do nothing
        jint                          attachResult;     // JNI_OK (0), JNI_EDETACHED or JNI_EVERSION
        int                           error;            // OK==0
    #ifdef UNIX
        pthread_t
    #else
        TID
    #endif
                                      attach2tid;       // TID to attach to or NULL
    }  STRUCT_PARAM;

    typedef STRUCT_PARAM *PSTRUCT_PARAM;          // pointer to structure
    long size_STRUCT_PARAM=sizeof(STRUCT_PARAM);  // get and memorize size of structure
#endif

#ifdef RGF_ATTACH_NEW       // ---rgf, 2014-05-16: new (simplified) attach/detach logic
        // structure for maintaining thread-related call information
    typedef struct _STRUCT_ATTACH_PARAM {
        JNIEnv                       *env;              // current JNIEnv or NULL
        jobject                       rajo;             // current rajo or NULL
        void                         *riid;             // to Rexx instance ID, needed e.g. for looking up rajo
        bool                          bDetach;          // TRUE: use DetachCurrentThread(), FALSE: do nothing
        int                           error;            // OK==0
    } STRUCT_ATTACH_PARAM;

    typedef STRUCT_ATTACH_PARAM *PSTRUCT_ATTACH_PARAM;          // pointer to structure
    long size_STRUCT_ATTACH_PARAM=sizeof(STRUCT_ATTACH_PARAM);  // get and memorize size of structure
#endif


// ============ functions for the above structures (2009-10-03, rgf)

// 20091003, rgf: using IsSameObject() for testing rajos, if too burdersome once could
//                use the rajo's hashCode


// prototypes:

// void RgfCreateRexxSyntaxConditionWithJavaThrowable (PSTRUCT_PARAM param, RexxThreadContext *rtc, const char * errorStringPattern); // rgf, 2011-06-04
// RexxStringObject RgfCreateRexxSyntaxConditionWithJavaThrowable (PSTRUCT_PARAM param, RexxThreadContext *rtc, const char * errorStringPattern); // rgf, 2011-06-04
// RexxStringObject RgfCreateRexxSyntaxConditionWithJavaThrowable (PSTRUCT_PARAM param, RexxCallContext *rcc, const char * errorStringPattern); // rgf, 2011-06-04
RexxStringObject RgfCreateRexxSyntaxConditionWithJavaThrowable (
    #ifdef RGF_ATTACH_NEW
        PSTRUCT_ATTACH_PARAM
    #else
        PSTRUCT_PARAM
    #endif
        param, int isCallContext, void *argCtxt, const char * errorStringPattern);


#ifndef RGF_ATTACH_NEW
    void removeRAJO(JNIEnv *env, PSTRUCT_RAJO node);
    void removeNODE(JNIEnv *env, PSTRUCT_NODE node);
    void removeTID2NODE(JNIEnv *env, PSTRUCT_TID2NODE node);

    void cleanTID2NODE (JNIEnv *env);
    void cleanRAJO (JNIEnv *env);

    PSTRUCT_RAJO findRAJO (
        #ifdef UNIX
            pthread_t
        #else
            TID
        #endif
                 tid       // thread ID
        );

    PSTRUCT_NODE findFirstNODE(
        #ifdef UNIX
            pthread_t
        #else
            TID
        #endif
                 tid       // thread ID
        );

    PSTRUCT_TID2NODE addRajoToTID2NODE (PSTRUCT_PARAM param, int bNewGroup);

    int subtractRajoFromTID2NODE (JNIEnv *env,
                                #ifdef UNIX
                                    pthread_t
                                #else
                                    TID
                                #endif
                                         tid,
                                  int bCloseGroup,
                                  int bKeepPrimodalGroup
                             );


    PSTRUCT_TID2NODE findTID2NODE(
        #ifdef UNIX
            pthread_t
        #else
            TID
        #endif
                 tid       // thread ID
        );



        // ---------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------

        // dumpPARAM
    inline void dumpPARAM(const char *title, PSTRUCT_PARAM param)
    {
        fprintf(stderr, "+++ dumpPARAM(), %s tid=[%lu], env=[%p], rajo=[%p], riid=[%p], bDetach=[%d], error=[%d], attachResult=[%d], attachToTID=[%lu]\n", title,
                        (unsigned long) param->tid,
                        param->env,
                        param->rajo,
                        param->riid,
                        param->bDetach,
                        param->error,
                        (int) param->attachResult,
                        (unsigned long) param->attach2tid
                        );
    }

        // dump RAJO-list
    inline void dumpRAJO(const char *title)
    {
        fprintf(stderr, "+++ dumpRAJO(), %s pRoot_RAJO=[%p]\n", title, pRoot_RAJO); fflush(stderr);

        if (pRoot_RAJO!=NULL)
        {
            for (PSTRUCT_RAJO pr=pRoot_RAJO; pr!=NULL; pr=pr->next)
            {
                fprintf(stderr, "\tRAJO [%p]={prev=[%p], next=[%p], refCounter=[%d], rajo=[%p], riid=[%p]}\n",
                                         pr,
                                         pr->prev,
                                         pr->next,
                                         pr->refCounter,
                                         pr->rajo, pr->riid
                                        );
                fflush(stderr);
            }
            fprintf(stderr, "\n"); fflush(stderr);
        }
    }

        // dump TID2NODE-list
    inline void dumpTID2NODE(const char *title)
    {
        fprintf(stderr, "+++ dumpTID2NODE(), %s pRoot_TID2NODE=[%p]\n", title, pRoot_TID2NODE); fflush(stderr);

        if (pRoot_TID2NODE!=NULL)
        {
            for (PSTRUCT_TID2NODE t2n=pRoot_TID2NODE; t2n!=NULL; t2n=t2n->next)
            {
                fprintf(stderr, "\tTID2NODE [%p]={tid=[%lu], prev=[%p], next=[%p], firstNode=[%p]}\n",
                                         t2n,
                                         (unsigned long) t2n->tid,
                                         t2n->prev,
                                         t2n->next,
                                         t2n->firstNode
                                        );
                fflush(stderr);

                    // list node-list
                for (PSTRUCT_NODE pn=t2n->firstNode; pn!=NULL; pn=pn->next)
                {
                    fprintf(stderr, "\t\t  NODE\t[%p]={nodeRefCount=[%d], belongsT2N=[%p], dependsOn=[%p],\n\t\t\tprev=[%p], next=[%p], rajoRef=[%p], riid=[%p],\n\t\t\tfirstDependent=[%p], prevDependent=[%p], nextDependent=[%p]}\n",
                                    pn,
                                    pn->nodeRefCounter,
                                    pn->belongsToTID2NODE,
                                    pn->dependsOn,
                                    pn->prev,
                                    pn->next,
                                    pn->rajoRef,
                                    pn->riid,
                                    pn->firstDependent,
                                    pn->prevDependent,
                                    pn->nextDependent
                            );
                    fflush(stderr);

                        // list dependent list
                    if (pn->firstDependent!=NULL)
                    {
                        fprintf(stderr, "\t\t\tdependents:\n");
                        fflush(stderr);
                        for (PSTRUCT_NODE i=pn->firstDependent; i!=NULL; i=i->nextDependent)
                        {
                            fprintf(stderr, "\t\t\t\t dependency-NODE [%p]={nodeRefCount=[%d], belongsT2N=[%p] (tid=[%lu]), dependsOn=[%p], rajoRef=[%p], riid=[%p],\n\t\t\t\t\t prevDependent=[%p], nextDependent=[%p]}\n",
                                    i,
                                    i->nodeRefCounter,
                                    i->belongsToTID2NODE,
                                    (unsigned long) ((PSTRUCT_TID2NODE) i->belongsToTID2NODE)->tid,
                                    i->dependsOn,
                                    i->rajoRef,
                                    i->riid,
                                    i->prevDependent,
                                    i->nextDependent);
                        }
                        fflush(stderr);

                    }
                }
            }
            fprintf(stderr, "\n"); fflush(stderr);
        }
    }



        // ---------------------------------------------------------------------------------------
        // empty all RAJO/TID2NODE structures (e.g. when unloading Java)
    inline void empty_RAJO_TID2NODE_structures(JNIEnv *env)
    {

    #if defined ( RGF_INFO_1 )
        fprintf(stderr, "*** *** RGF_INFO_1: empty_RAJO_TID2NODE_structures() 1, removing pRoot_TID2NODE=[%p] with all NODEs...\n", pRoot_TID2NODE);
        fflush(stderr);
    #endif
           // remove all TID2NODE nodes
        //for (PSTRUCT_TID2NODE t2n=pRoot_TID2NODE; t2n=NULL; )
        for (PSTRUCT_TID2NODE t2n=pRoot_TID2NODE; t2n!=NULL; ) // JLF fix the test (typo error)
        {
            PSTRUCT_TID2NODE nextT2N_node=t2n->next;    // save next node, as current t2n node pointed will be removed in the process
            for (PSTRUCT_NODE i=t2n->firstNode; i!=NULL;)
            {
                PSTRUCT_NODE nextNode=i->next;          // save next node, as current node will be deleted
                removeNODE(env, i);     // remove NODE, will also remove all dependents
                i=nextNode;
            }
            removeTID2NODE(env, t2n);
            t2n=nextT2N_node;
        }
        pRoot_TID2NODE=NULL;

    #if defined ( RGF_INFO_1 )
        fprintf(stderr, "*** *** RGF_INFO_1: empty_RAJO_TID2NODE_structures() 2, removing pRoot_RAJO=[%p] nodes ...\n", pRoot_RAJO);
        fflush(stderr);
    #endif
            // make sure all RAJO nodes are removed as well !
        for (PSTRUCT_RAJO pr=pRoot_RAJO; pr!=NULL; )
        {
            PSTRUCT_RAJO nextNode=pr->next;
            removeRAJO(env, pRoot_RAJO);
            pr=nextNode;
        }
        pRoot_RAJO=NULL;
    }


        // Create the Rexx exception message, if something went wrong with environmentAttachTo
        // - param->error:  -1 ... Attach-error, attachResult holds Java error code, raise an exception in caller
        //                  -2 ... no rajo found, raise an exception in caller
        //                  -3 ... cannot safely use the given RAJO in TID, as actual RAJO is being used in multiple Rexx threads
        //                  -4 ... cannot attach to target TID
        //                 -99 ... no JVM available !
    inline void createAttachErrorMessage(const char *title, char *buf, size_t bufLen, PSTRUCT_PARAM param)
    {
        if (param->error == -1)         // JNI attach error
        {
            SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], thread [%lu] cannot 'AttachCurrentThread(...)' to JVM=[%p], Java's return code=[%d] ('0'=JNI_OK, '-1'=JNI_ERR, '-2'=JNI_EDETACHED, '-3'=JNI_EVERSION, '-4'=JNI_ENOMEM, '-5'=JNI_EEXIST, '-6'=JNI_EINVAL)",
                          DLLNAME, title, param->error, (unsigned long) param->tid, defaultJVM->jvm, (int) param->attachResult);
        }
        else if (param->error == -2)    // no rajo associated with this TID!
        {
            SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], cannot attach to thread [%lu] (not yet|no more set up for Java interaction, 'rajo' not found)",
                          DLLNAME, title, param->error, (unsigned long) param->tid);
        }
        else if (param->error == -3)    // TID has Rexx threads using different 'rajo', dangerous, hence an error!
        {

            SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], thread's (tid=[%lu]) interface to Java already used by multiple Rexx threads, cannot (temporarily) overload Java interface with a different Java object as addressee therefore.",
                          DLLNAME, title, param->error, (unsigned long) param->tid);
        }
        else if (param->error == -4)    // target TID is not set up with a 'rajo'
        {
            SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], cannot attach to thread [%lu] (not yet|no more set up for Java interaction)",
                          DLLNAME, title, param->error, (unsigned long) param->tid);
        }
        else if (param->error == -99)   // no JVM available!
        {
            SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], panic! No JVM available", DLLNAME, title, param->error);
        }

        else                        // unknown error !
        {
            SNPRINTF( buf, bufLen, "%.16s/%.64s: unknown ATTACH ERROR: tid=[%lu], env=[%p], rajo=[%p], riid=[%p], bDetach=[%d], attachResult=[%d], error=[%d]",
                          DLLNAME, title,
                              (unsigned long) param->tid,
                              param->env,
                              param->rajo,
                              param->riid,
                              param->bDetach,
                              (int) param->attachResult,
                              param->error
                          );
        }
    }


        // Create the Rexx exception message, if something went wrong with environmentAttachTo
        // - param->error:  -1 ... Attach-error, attachResult holds Java error code, raise an exception in caller
        //                  -2 ... no rajo found, raise an exception in caller
        //                  -3 ... cannot safely use the given RAJO in TID, as actual RAJO is being used in multiple Rexx threads
        //                  -4 ... cannot attach to target TID
        //                 -99 ... no JVM available !
    inline RexxStringObject createAttachErrorMessageRSO(RexxCallContext *rcc, const char *title, PSTRUCT_PARAM param)
    {
        char buf[512];
        createAttachErrorMessage(title, buf, 512, param);   // create the message
        RexxStringObject rso=rcc->String(buf);              // turn it into a RSO
        rcc->SetContextVariable(BSF_ERROR_STRING, rso);     // set context error variable
        return rso;
    }




        // ---------------------------------------------------------------------------------------
        // - get JNIEnv, if necessary do a AttachCurrentThread() and remember that we need a (bDetach=TRUE)
        // - if rajo is NULL get rajo; if a rajo then in any case use addRajoToTID2NODE(), else return NULL
        // - param->error:  -1 ... Attach-error, attachResult holds Java error code, raise an exception in caller
        //                  -2 ... no rajo found, raise an exception in caller
        //                  -3 ... cannot safely use the given RAJO in TID, as actual RAJO is being used in multiple Rexx threads
        //                  -4 ... cannot attach to target TID
        //                 -99 ... no JVM available !
        // - bNewGroup:     TRUE=increase counters by 2 (instead of 1) to keep the nodes around after doing
        //                  an environmentDetach
        // NOTE: if an AttachCurrentThread() was carried out, and an error (-2 or -3) occurs, then
        //       the caller is responsible to carry out a DetachCurrentThread()!
        //       This way this function can be used for attaching, such that upon return interactions with
        //       Java are possible.

    // rgf, 2011-03-19: all of a sudden inline will cause a problem as global variable defaultJVM is
    //                  not addressed correctly anymore, removing inline remedies the situation!
    // (i686-apple-darwin10-gcc-4.2.1 (GCC) 4.2.1 (Apple Inc. build 5664))
    #ifndef __APPLE__
        inline
    #endif
    void environmentAttachTo(PSTRUCT_PARAM param, int bNewGroup)
    {

        if (defaultJVM->jvm == NULL)    // no JVM available !
        {
    #if defined (DEBUG_JAVA_ATTACH_DETACH)
        fprintf(stderr, "environmentAttachTo() - NO JVM? defaultJVM->jvm=[%p], defaultJVM->primodal_rajo=[%p]\n", defaultJVM->jvm, defaultJVM->primodal_rajo);
        fflush(stderr);
    #endif

            param->error=-99;
            return;
        }

        // if (param->tid==NULL)           // no tid given, then get current TID
        if (param->tid==0)              // no tid given, then get current TID
        {
            param->tid=RgfGetTID();
        }

        if (param->env==NULL)           // no JNIEnv object yet, get one
        {
                // is this thread already attached, if so get it
            if ((param->attachResult=defaultJVM->jvm->GetEnv((void **)&param->env, USE_DEFINED_JNI_VERSION))==JNI_EDETACHED)
            {
                // nope, not yet attached, try to attach
                if ( (param->attachResult=defaultJVM->jvm->AttachCurrentThread((void **) &param->env, (void *) &defaultJavaVMAttachArgs)) != JNI_OK) // attach to JVM
                {
    #if defined (DEBUG_JAVA_ATTACH_DETACH)
        fprintf(stderr, "environmentAttachTo() - AttachCurrentThread did NOT work ! :-( :-(, attachResult=[%d]\n", (int) param->attachResult);
        fflush(stderr);
    #endif
                    param->error=-1;    // JNI attach error
                    return;
                }
                param->bDetach=TRUE;    // indicate we need an explicit DetachCurrentThread() in this case

    #if defined (DEBUG_JAVA_ATTACH_DETACH)
        fprintf(stderr, ":) :) :) environmentAttachTo(), tid=[%lu], AttachCurrentThread(...) successful.\n", (unsigned long) param->tid);
        fflush(stderr);
    #endif
            }
            else if (param->attachResult!=JNI_OK)   // a version error ?
            {
    #if defined (DEBUG_JAVA_ATTACH_DETACH)
        fprintf(stderr, "environmentAttachTo() - GetEnv(...) did NOT work ! :-( :-(, attachResult=[%d]\n", (int) param->attachResult);
        fflush(stderr);
    #endif
                param->error=-1;        // JNI attach error
                return;
            }
            else
            {
    #if defined (DEBUG_JAVA_ATTACH_DETACH)
        fprintf(stderr, ":) :) :) environmentAttachTo(), tid=[%lu], GetEnv() successful.\n", (unsigned long) param->tid);
        fflush(stderr);
    #endif
            }
        }

        if (param->rajo==NULL)          // no rajo, try to get one
        {
    //        PSTRUCT_RAJO pr=findRAJO(param->attach2tid==NULL ? param->tid : param->attach2tid);
            PSTRUCT_RAJO pr=findRAJO(param->attach2tid==0 ? param->tid : param->attach2tid);
            if (pr==NULL)               // no rajo found, an error, return !
            {
                param->error=-2;        // no rajo found error, caller must detach
                return;
            }
            else    // get and save rajo
            {
                param->rajo=pr->rajo;
            }
        }

        // if (param->attach2tid==NULL)    // regular operation
        if (param->attach2tid==0)   // regular operation
        {
                // check, whether we may attach to this TID
            PSTRUCT_NODE node=findFirstNODE(param->tid);

            if (node!=NULL && param->env->IsSameObject(node->rajoRef->rajo, param->rajo)!=TRUE &&
                node->firstDependent!=NULL
               )
            {
                param->error=-3;        // cannot use this rajo as it would cover a different rajo needed by other Rexx threads!
                return;
            }
        }

            // add new node or increase existing node's counter
        // addRajoToTID2NODE(param->env, param->tid, param->rajo, param->riid, bNewGroup);
        addRajoToTID2NODE(param, bNewGroup);
        return;
    }

        // ---------------------------------------------------------------------------------------
        // - subtractRajoFromTID
        // - detach from JNIEnv if bDetach==TRUE
        // - bCloseGroup: if TRUE subtract 2 instead of 1 to close group, i.e. nodes may be reclaimed (if not used by other TIDs)

    // rgf, 2011-03-19: all of a sudden inline will cause a problem as global variable defaultJVM is
    //                  not addressed correctly anymore, removing inline remedies the situation!
    //                  (i686-apple-darwin10-gcc-4.2.1 (GCC) 4.2.1 (Apple Inc. build 5664)); just make sure this function works correctly under Apple as well
    #ifndef __APPLE__
       inline
    #endif
    void environmentDetachFrom(PSTRUCT_PARAM param, int bCloseGroup)
    {
        // if (param->error==NULL)     // maintain structures
        if (param->error==0)     // maintain structures
        {
            int res=subtractRajoFromTID2NODE(param->env, param->tid, bCloseGroup, 0); // do not indicate to keep primodal group (only necessary for BSFDetach())
        }

        if (param->bDetach==TRUE)   // doe we need to explicitly detach ?
        {
            if (defaultJVM->jvm == NULL)    // no JVM available !
            {
                param->error=-99;
                return;
            }
    #if defined (DEBUG_JAVA_ATTACH_DETACH)
        fprintf(stderr, "environmentDetachFrom(), tid=[%lu], DetachCurrentThread, param->bDetach=[%d]\n", (unsigned long) param->tid, param->bDetach);
        fflush(stderr);
    #endif
            defaultJVM->jvm->DetachCurrentThread();
        }
        return;
    }

#endif

#ifdef RGF_ATTACH_NEW

    // Create the Rexx exception message, if something went wrong with environmentAttachTo
inline void createAttachErrorMessageNew(const char *title, char *buf, size_t bufLen, PSTRUCT_ATTACH_PARAM param)
{
    switch (param->error)
    {
    case 1 :    // no JVM
        SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], panic! No JVM available for this Rexx interpreter instance: [%p]", DLLNAME, title, param->error, param->riid );
        break;

    case 2 :    // no RAJO
        SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], panic! No Java-RexxAndJava (RAJO) object to interact with available for this Rexx interpreter instance: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->env );
        break;

    case -1 :   // JNI_ERR
        SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], JNI_ERR (\"unknown JNI error\"), Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->rajo, param->env );
        break;

    case -2 :   // JNI_EDETACHED
        SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], JNI_EDETACHED (\"thread detached from the JVM\"), Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->rajo, param->env );
        break;

    case -3 :   // JNI_EVERSION
        SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], JNI_EVERSION (\"JNI version error\"), Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->rajo, param->env );
        break;

    case -4 :   // JNI_EMEM
        SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], JNI_EMEM (\"out of memory\"), Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->rajo, param->env );
        break;

    case -5 :   // JNI_EEXIST
        SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], JNI_EEXIST (\"JVM already exists\"), Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->rajo, param->env );
        break;

    case -6 :   // JNI_EINVAL
        SNPRINTF( buf, bufLen, "%.16s/%.64s: attach error [%d], JNI_EINVAL (\"arguments invalid\"), Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->rajo, param->env );
        break;

    default:
        SNPRINTF( buf, bufLen, "%.16s/%.64s: unknown attach error [%d], Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]",
                               DLLNAME, title, param->error, param->riid, param->rajo, param->env );
    }
}

    // Create the Rexx exception message, if something went wrong with environmentAttachToNew
inline RexxStringObject createAttachErrorMessageRSONew(RexxCallContext *rcc, const char *title, PSTRUCT_ATTACH_PARAM param)
{
    char buf[512];
    createAttachErrorMessageNew(title, buf, 512, param);   // create the message
    RexxStringObject rso=rcc->String(buf);              // turn it into a RSO
    rcc->SetContextVariable(BSF_ERROR_STRING, rso);     // set context error variable
    return rso;
}


    // attaching the current thread to the JVM (attaching does not nest)
        // JNI_OK           0                 / * success * /
        // JNI_ERR          (-1)              / * unknown error * /
        // JNI_EDETACHED    (-2)              / * thread detached from the VM * /
        // JNI_EVERSION     (-3)              / * JNI version error * /
        // JNI_ENOMEM       (-4)              / * not enough memory * /
        // JNI_EEXIST       (-5)              / * VM already created * /
        // JNI_EINVAL       (-6)              / * invalid arguments * /
        //                  +1                / * no JVM available
        //                  +2                / * no RAJO found for RII * /

    // rgf, 2011-03-19: all of a sudden inline will cause a problem as global variable defaultJVM is
    //                  not addressed correctly anymore, removing inline remedies the situation!
    // (i686-apple-darwin10-gcc-4.2.1 (GCC) 4.2.1 (Apple Inc. build 5664))
    #ifndef __APPLE__
        inline
    #endif
    void environmentAttachToNew(PSTRUCT_ATTACH_PARAM param)
    {

        if (defaultJVM->jvm == NULL)    // no JVM available !
        {

    #if defined (DEBUG_RGF_ATTACH_NEW)
        fprintf(stderr, "environmentAttachToNew() - NO JVM! defaultJVM->jvm=[%p], defaultJVM->primodal_rajo=[%p]\n", defaultJVM->jvm, defaultJVM->primodal_rajo);
        fflush(stderr);
    #endif

            param->error=1;             // no JVM available, error !
            return;
        }

            // try to find this riid's rajo
        for (PSTRUCT_RII i=pRoot_RII; i!=NULL; i=i->next)
        {
            if (param->riid==i->instance)
            {
                param->rajo=i->rajo;

    #if defined (DEBUG_RGF_ATTACH_NEW)
        fprintf(stderr, "environmentAttachToNew() - found riid=[%p] with rajo=[%p}\n", i->instance, i->rajo);
        fflush(stderr);
    #endif
                break;
            }
        }

        if (param->rajo == NULL)        // no rajo available, error !
        {
            param->error=2;
            return;
        }

            // is this thread already attached, if so get it
        if ((param->error=defaultJVM->jvm->GetEnv((void **)&param->env, USE_DEFINED_JNI_VERSION))==JNI_EDETACHED)
        {
            // nope, not yet attached, try to attach
            if ( (param->error=defaultJVM->jvm->AttachCurrentThread((void **) &param->env, (void *) &defaultJavaVMAttachArgs)) != JNI_OK) // attach to JVM
            {
    #if defined (DEBUG_RGF_ATTACH_NEW)
        fprintf(stderr, "environmentAttachToNew() - AttachCurrentThread did NOT work ! :-( :-(, parm->error=[%d]\n", (int) param->error);
        fflush(stderr);
    #endif
                // JNI attach error, return prematurely
                return;
            }

    #if defined (DEBUG_RGF_ATTACH_NEW)
            fprintf(stderr, "environmentAttachToNew() - AttachCurrentThread() WORKED, bDetach=[true]! \n");
            fflush(stderr);
    #endif
            param->bDetach=true;        // Carry out DetachCurrentThread() at the end, if not a RII primodal thread
        }
    #if defined (DEBUG_RGF_ATTACH_NEW)
        else
        {
            fprintf(stderr, "environmentAttachToNew() - GetEnv() WORKED, bDetach=[false]! \n");
            fflush(stderr);
        }
    #endif


        return;
    }


    // ---------------------------------------------------------------------------------------
    // - detach from JNIEnv, if bDetach==TRUE

// rgf, 2011-03-19: all of a sudden inline will cause a problem as global variable defaultJVM is
//                  not addressed correctly anymore, removing inline remedies the situation!
//                  (i686-apple-darwin10-gcc-4.2.1 (GCC) 4.2.1 (Apple Inc. build 5664)); just make sure this function works correctly under Apple as well
#ifndef __APPLE__
   inline
#endif
void environmentDetachFromNew(PSTRUCT_ATTACH_PARAM param)
{
    #if defined (DEBUG_RGF_ATTACH_NEW)
        fprintf(stderr, "environmentDetachFromNew(), param->bDetach=[%d]\n", param->bDetach);
        fflush(stderr);
    #endif

    if (param->bDetach==true)   // do we need to explicitly detach ? (This will detach this thread from the JVM.)
    {

        if (defaultJVM->jvm == NULL)    // no JVM available !
        {
    #if defined (DEBUG_RGF_ATTACH_NEW)
        fprintf(stderr, "environmentDetachFromNew() - NO JVM! defaultJVM->jvm=[%p], defaultJVM->primodal_rajo=[%p]\n", defaultJVM->jvm, defaultJVM->primodal_rajo);
        fflush(stderr);
    #endif

            param->error=1;             // no JVM available, error !
            return;
        }
        param->error=defaultJVM->jvm->DetachCurrentThread();
    #if defined (DEBUG_RGF_ATTACH_NEW)
        fprintf(stderr, "environmentDetachFromNew() - carried out DetachCurrentThread(), result=[%d]\n", param->error);
        fflush(stderr);
    #endif

    }
    return;
}



#endif


#ifndef RGF_ATTACH_NEW

    /* 20091006, rgf
        // ---------------------------------------------------------------------------------------
        // worker for BsfAttachToTID(tid)
        // - returns in param->error:   -4 ... if target TID was not found
        //                  -3 ... cannot safely use the given RAJO in TID, as actual RAJO is being used in multiple Rexx threads
    inline PSTRUCT_TID2NODE attachToTID_2(PSTRUCT_PARAM param,
                                     #ifdef UNIX
                                            pthread_t
                                        #else
                                            TID
                                        #endif
                                            attachToTID
    )
    {
        param->error=0;             // make sure error is reset

        PSTRUCT_RAJO pr=findRAJO(param->tid);   // find RAJO node

    dumpPARAM(   "BsfUnloader(): param:", param);

    fprintf(stderr, "/// aTT2 marker (1), tid=[%lu], RAJO=[%p]...\n", (unsigned long) param->tid, pr);fflush(stderr);


        if (pr==NULL)               // no RAJO node for tid
        {
    fprintf(stderr, "/// aTT2 marker (2)...\n");fflush(stderr);
            param->error=-4;        // indicate not target rajo found
            return NULL;
        }

            // open a new group
    fprintf(stderr, "/// aTT2 marker (3)...\n");fflush(stderr);
        return addRajoToTID2NODE(param->env, param->tid, pr->rajo, param->riid, TRUE, attachToTID);
    }


        // ---------------------------------------------------------------------------------------
        // worker for BsfDetach()
        // returns 1=TRUE, 0=FALSE, -1=not found
    inline int detachFromTID_2(PSTRUCT_PARAM param)
    {
            // only deduct one, an environmentDetach will subtract another one, which balances attachToTID_2
        return subtractRajoFromTID2NODE(param->env, param->tid, FALSE);
    }

    20091006, rgf */

        // ---------------------------------------------------------------------------------------
        // remove a given node unconditionally
    inline void removeTID2NODE(JNIEnv *env, PSTRUCT_TID2NODE node)
    {
        if (node->prev!=NULL)       // a previous node exists, adjusts its pointer
        {
            node->prev->next=node->next;    // let the previous node point to this node's next
    //        node->prev=NULL;                 // clear
        }
        else    // no previous node (hence this is the root node), make next node the root node
        {
            pRoot_TID2NODE=node->next;      // let root point to next node
        }

        if (node->next!=NULL)       // a next node exists, let it point to this node's previous node
        {
            node->next->prev=node->prev;
    //        node->next=NULL;                 // clear
        }

        // remove all NODEs of this TID2NODE list (mutating!)
        while (node->firstNode!=NULL)
        {
            removeNODE(env, node->firstNode);
        }
        RgfFreeMemory(node);        // free memory of node
        return;
    }


        // ---------------------------------------------------------------------------------------
        // remove a given node unconditionally
    inline void removeRAJO(JNIEnv *env, PSTRUCT_RAJO node)
    {
        if (node->prev!=NULL)       // a previous node exists, adjusts its pointer
        {
            node->prev->next=node->next;    // let the previous node point to this node's next
    //        node->prev=NULL;    // clear
        }
        else    // no previous node (hence this is the root node), make next node the root node
        {
            pRoot_RAJO=node->next;      // let root point to next node
        }

        if (node->next!=NULL)       // a next node exists, let it point to this node's previous node
        {
            node->next->prev=node->prev;
    //        node->next=NULL;    // clear
        }

        env->DeleteGlobalRef(node->rajo);   // delete global reference for this cached rajo

        RgfFreeMemory(node);        // free memory of node
        return;
    }


        // ---------------------------------------------------------------------------------------
        // remove a given node unconditionally, if last node in TID2NODE, remove TID2NODE as well !
    inline void removeNODE(JNIEnv *env, PSTRUCT_NODE node)
    {
    // fprintf(stderr, "/// marker (3a), removeNODE(), node=[%p]...\n", node);fflush(stderr);

            // if this node is in a list of dependent nodes, remove it from the list
        if (node->dependsOn!=NULL)      // we are dependent
        {
            PSTRUCT_NODE master=node->dependsOn;    // node, we depend on

    /*
            PSTRUCT_NODE tmpLastNodeInList=NULL;    // tmp var

                // if an own dependency list, get
            if (node->firstDependent!=NULL)
            {
                    // get last node in dependent list
                for (tmpLastNodeInList=node->firstDependent; tmpLastNodeInList->next!=NULL; tmpLastNodeInList=tmpLastNodeInList->next)
                {
                    ;
                }
            }
    */


            if (master->firstDependent==node)       // we are the first node in the dependent list
            {
                master->firstDependent=node->nextDependent; // remove ourselves from the dependent list
                node->nextDependent=NULL;           // reset dependent pointer
            }
            else    // we are somewhere in the middle of the dependent list
            {
                if (node->prevDependent!=NULL)      // if the previous node exists, let it point ahead
                {
                    node->prevDependent->nextDependent=node->nextDependent;    // let previous node point to next dependent node
                }

                if (node->nextDependent!=NULL)      // if next dependent node exists, let it point back
                {
                    node->nextDependent->prevDependent=node->prevDependent;
                }
                node->nextDependent=NULL;           // reset dependent pointer
                node->prevDependent=NULL;           // reset dependent pointer
            }
            node->dependsOn=NULL;   // clear info
        }

    // fprintf(stderr, "/// marker (3b), removeNODE(), node=[%p]...\n", node);fflush(stderr);

    /*
            // remove dependent nodes, if any (cf. BsfAttachToTID())
        if (node->firstDependent!=NULL)
        {
            for (PSTRUCT_NODE i=node->firstDependent; i!=NULL; )
            {
                PSTRUCT_NODE tmpNode=i;
                i=i->nextDependent;                     // memorize next node in dependent list as 'i'-pointer won't be available after the next call
    // fprintf(stderr, "/// marker (3b-1a), removeNODE(), node=[%p]...\n", node);fflush(stderr);
                removeNODE(env, tmpNode);               // recurse
    // fprintf(stderr, "/// marker (3b-1b), removeNODE(), node=[%p]...\n", node);fflush(stderr);
            }
            node->firstDependent=NULL;  // clear info
        }
    */

            // 20091007, rgf, new strategy: make first dependent new master, adjust other dependents to point to its T2N instead
        if (node->firstDependent!=NULL)
        {
            PSTRUCT_TID2NODE newMaster=(PSTRUCT_TID2NODE) node->firstDependent->belongsToTID2NODE;
            newMaster->firstNode->dependsOn=NULL;       // clear dependsOn
                // let newMaster node's dependent list start with the nextDependent in the old list
            newMaster->firstNode->firstDependent=node->firstDependent->nextDependent;

            // now adjust dependent nodes to point to newMaster instead
            for (PSTRUCT_NODE i=node->firstDependent->nextDependent; i!=NULL; i=i->nextDependent)
            {
                i->dependsOn=newMaster->firstNode;
            }

            // remove dependent list from node's TID2NODE
            node->firstDependent=NULL;  // clear info
            node->dependsOn=NULL;       // clear info
        }

    // fprintf(stderr, "/// marker (3c), removeNODE(), node=[%p]...\n", node);fflush(stderr);

            // adjust counters
        node->rajoRef->refCounter-=node->nodeRefCounter;    // deduct nr of references
        node->nodeRefCounter=0;

    // fprintf(stderr, "/// marker (3d), removeNODE(), node=[%p]...\n", node);fflush(stderr);
        PSTRUCT_TID2NODE t2n=(PSTRUCT_TID2NODE) node->belongsToTID2NODE;    // pointer to TID2NODE this node belongs to

        if (t2n->firstNode==node)           // first node
        {
            t2n->firstNode=node->next;      // let TID2NODE point to node's next node

            if (node->next!=NULL)           // if the next node exists, let it point to previous node
            {
                node->next->prev=node->prev;// let the next node point to this node's previous node

    //            node->next=NULL;    // clear
            }


    // fprintf(stderr, "/// marker (3f), removeNODE(), node=[%p]...\n", node);fflush(stderr);
        }
        else    // not the first node in the list
        {
    // fprintf(stderr, "/// marker (3g), removeNODE(), node=[%p]...\n", node);fflush(stderr);
            if (node->prev!=NULL)           // if previous node exists, let it point ahead
            {
                node->prev->next=node->next;// let the previous node point to the node's next node
    //            node->prev=NULL;    // clear
            }

            if (node->next!=NULL)           // if next node exists, let it point back
            {
    // fprintf(stderr, "/// marker (3h), removeNODE(), node=[%p]...\n", node);fflush(stderr);
                node->next->prev=node->prev;// let the next node point to the node's previous node
    //            node->next=NULL;    // clear
            }
        }


    // fprintf(stderr, "/// marker (3j), removeNODE(), node=[%p]...\n", node);fflush(stderr);
        RgfFreeMemory(node);                // free memory of node
    // fprintf(stderr, "/// marker (3k), removeNODE(), node=[%p]...\n", node);fflush(stderr);
        return;
    }


    // TODO: ausgelagerte Löschroutinen

            // check whether RAJO-nocde has dropped to 0, if so remove it
    /*
        if (node->rajoRef->refCounter==0)   // no more references for this RAJO, delete node
        {
            removeRAJO(env, node->rajoRef);
        }

        if (t2n->firstNode==NULL)
        {
            removeTID2NODE(env, t2n);   // remove TID2NODE node
        }
    */



        // ---------------------------------------------------------------------------------------
        // remove all nodes unconditionally, that got created for the given Rexx instance (riid)
    inline void removeAll_RIID_nodes(JNIEnv *env, void *riid)
    {
            // iterate through all TID2NODE and remove NODEs that refer to the given riid
    // fprintf(stderr, "/// marker (2a), pRoot_TID2NODE=[%p], riid=[%p]...\n", pRoot_TID2NODE, riid);fflush(stderr);
        for (PSTRUCT_TID2NODE t2n=pRoot_TID2NODE; t2n!=NULL; )
        {
            PSTRUCT_TID2NODE tmpNext=t2n->next; // remember next node (makes it immune from deleting the node)

                // iterate through all the NODEs
            for (PSTRUCT_NODE i=t2n->firstNode; i!=NULL; )
            {
                PSTRUCT_NODE iNext=i->next;     // remember next node (makes it immune from deleting the node)
                if (i->riid==riid)      // a node to remove found
                {
    // fprintf(stderr, "/// rarn - marker (1a), removeNODE(), i=[%p]...\n", i);fflush(stderr);
                    removeNODE(env, i); // remove this node
    // fprintf(stderr, "/// rarn - nach (1b), removeNODE(), i=[%p], iNext=[%p]...\n", i, iNext);fflush(stderr);
                }
                i=iNext;
            }

    // fprintf(stderr, "/// rarn - marker (2a), t2n=[%p]...\n", t2n);fflush(stderr);
            if (t2n->firstNode==NULL)    // 20091006, rgf: made it conditional: only delete if no more nodes associated with this T2N
            {
                removeTID2NODE(env, t2n);
            }
    // fprintf(stderr, "/// rarn - nach (2b), tmpNext=[%p]...\n", tmpNext);fflush(stderr);

            t2n=tmpNext;
        }


    // fprintf(stderr, "/// rarn - marker (3a), pRoot_RAJO=[%p], riid=[%p]...\n", pRoot_RAJO, riid);fflush(stderr);
            // make sure we have no RIID RAJO leftover nodes (there should be none)
        for (PSTRUCT_RAJO r=pRoot_RAJO; r!=NULL; )
        {
            PSTRUCT_RAJO rNext=r->next;         // remember next node (makes it immune from deleting the node)
            if (r->riid==riid)      // a node to remove found
            {
                removeRAJO(env, r); // remove this node
            }

            r=rNext;
        }
    // fprintf(stderr, "/// rarn - marker (3b), pRoot_TID2NODE=[%p], riid=[%p]...\n", pRoot_TID2NODE, riid);fflush(stderr);
        return;
    }


        // ---------------------------------------------------------------------------------------
        // remove all nodes unconditionally, that got created for the given Rexx instance (riid)
    inline void removeAll_nodes(JNIEnv *env)
    {
            // iterate through all TID2NODE and remove NODEs that refer to the given riid
        //for (PSTRUCT_TID2NODE t2n=pRoot_TID2NODE; t2n!=NULL; t2n=t2n->next)
        for (PSTRUCT_TID2NODE t2n=pRoot_TID2NODE; t2n!=NULL;) // JLF
        {
            PSTRUCT_TID2NODE t2nNext=t2n->next;
            removeTID2NODE(env, t2n);
            t2n=t2nNext;
        }

            // make sure we have no RIID RAJO leftover nodes (there should be none)
        //for (PSTRUCT_RAJO r=pRoot_RAJO; r!=NULL; r=r->next)
        for (PSTRUCT_RAJO r=pRoot_RAJO; r!=NULL;) // JLF
        {
            PSTRUCT_RAJO rNext=r->next;
            removeRAJO(env, r); // remove this node
            r=rNext;
        }
        return;
    }


        // ---------------------------------------------------------------------------------------
        // walk the RAJO list, remove nodes where counter < 1
    inline void cleanRAJO (JNIEnv *env)
    {
        //for (PSTRUCT_RAJO r=pRoot_RAJO; r!=NULL; r=r->next)
        for (PSTRUCT_RAJO r=pRoot_RAJO; r!=NULL;) // JLF
        {
            PSTRUCT_RAJO rNext=r->next;
            if (r->refCounter<1)      // a node to remove found
            {
                if (r->refCounter<0)
                {
                    fprintf(stderr, "BSF4ooRexx.cc: cleanRAJO(), refCounter<0, i.e. [%d]\n", r->refCounter);
                    fflush(stderr);
                }
                removeRAJO(env, r); // remove this node
            }
            r=rNext;
        }
    }

        // ---------------------------------------------------------------------------------------
        // walk the TID2NODE list, remove nodes where counter < 1
    inline void cleanTID2NODE (JNIEnv *env)
    {
        //for (PSTRUCT_TID2NODE r=pRoot_TID2NODE; r!=NULL; r=r->next)
        for (PSTRUCT_TID2NODE r=pRoot_TID2NODE; r!=NULL;) // JLF
        {
            PSTRUCT_TID2NODE rNext=r->next;
            if (r->firstNode==NULL)     // no nodes, hence safe to remove
            {
                removeTID2NODE(env, r); // remove this node
            }
            r=rNext;
        }
    }


    inline PSTRUCT_NODE createNODE (PSTRUCT_RAJO rajoRef, void *riid)
    {
        PSTRUCT_NODE newNode=(PSTRUCT_NODE) RgfAllocateMemory(size_STRUCT_NODE);
        memset(newNode,0,size_STRUCT_NODE);

        newNode->rajoRef=rajoRef;
        newNode->riid   =riid;
        return newNode;
    }


        // ---------------------------------------------------------------------------------------
        // create a node and return it
    inline PSTRUCT_TID2NODE createTID2NODE (
                                        #ifdef UNIX
                                            pthread_t
                                        #else
                                            TID
                                        #endif
                                                 tid       // thread ID
                                    )
    {
        PSTRUCT_TID2NODE newNode=(PSTRUCT_TID2NODE) RgfAllocateMemory(size_STRUCT_TID2NODE);
        memset(newNode,0,size_STRUCT_TID2NODE);

        newNode->tid=tid;

        if (pRoot_TID2NODE!=NULL)
        {
            newNode->next       =pRoot_TID2NODE;// let newNode point to current root node
            pRoot_TID2NODE->prev=newNode;       // let the root node point back to newNode
        }

        pRoot_TID2NODE=newNode;
        return newNode;
    }


        // ---------------------------------------------------------------------------------------
        // get a RAJO node and return it;
        // if not found, create a new one, insert it at the root of the list and return it
    inline PSTRUCT_RAJO getOrCreateRAJO(JNIEnv *env, jobject rajo, void *riid)
    {
        for (PSTRUCT_RAJO i=pRoot_RAJO; i!=NULL; i=i->next)
        {
            if (env->IsSameObject(i->rajo, rajo)==JNI_TRUE) // found ?
            {
                return i;
            }
        }

        // not found, create a new node, initialize it, insert and return it
        PSTRUCT_RAJO newNode=(PSTRUCT_RAJO) RgfAllocateMemory(size_STRUCT_RAJO);
        memset(newNode,0,size_STRUCT_RAJO);
        newNode->rajo=env->NewGlobalRef(rajo);  // save rajo in form of a global reference
        newNode->riid=riid;

        if (pRoot_RAJO!=NULL)   // need to adjust pRoot_RAJO ?
        {
            newNode->next   =pRoot_RAJO;        // let newNode point to current root node
            pRoot_RAJO->prev=newNode;           // let the root node point back to newNode
        }

        pRoot_RAJO=newNode;
        return newNode;
    }


        // ---------------------------------------------------------------------------------------
        // add a rajo to the TID2NODE list, if necessary or increase the node's counter
        // - get tid, if not supplied
        // - locate tid2rajo
        //    - if there, look at first node, if same rajo, increase counter
        //    - else create (rajo, tid2rajo, node, whatever necessary)
        // - bNewGroup:     TRUE=increase counters by 2 (instead of 1) to keep the nodes around after doing
        //                  an environmentDetach
        //
        // caller needs to have blocked access to low level structures
    inline PSTRUCT_TID2NODE addRajoToTID2NODE (PSTRUCT_PARAM param, int bNewGroup)
    {
        // if (param->tid==NULL)          // no tid given, get this TID
        if (param->tid==0)             // no tid given, get this TID
        {
            param->tid=RgfGetTID();
        }

    #if defined (DEBUG_RAJO)
       fprintf(stderr, "...+... addRajoToTID2NODE(): param->tid=[%lu]", (unsigned long) param->tid); fflush(stderr);
    #endif

        PSTRUCT_RAJO   rajoListNode=NULL;
        PSTRUCT_NODE           node=NULL;
        PSTRUCT_TID2NODE t2ListNode=NULL;
        PSTRUCT_TID2NODE targetT2L =NULL;
        targetT2L=t2ListNode;           // remember node in case we are attaching to it

            // find tid in tid2jrst
        for (t2ListNode=pRoot_TID2NODE; t2ListNode!=NULL; t2ListNode=t2ListNode->next)
        {
    // fprintf(stderr, "/// a2n marker (1), t2ListNode=[%p]...\n", t2ListNode);fflush(stderr);
            if (t2ListNode->tid == param->tid)    // tid found !
            {
    // fprintf(stderr, "/// a2n marker (2a)...\n");fflush(stderr);
                // o.k. found !
                if (param->env->IsSameObject(t2ListNode->firstNode->rajoRef->rajo, param->rajo)==JNI_TRUE)
                {
    // fprintf(stderr, "/// a2n marker (2b)...\n");fflush(stderr);
                    // maintain counters and return
                    t2ListNode->firstNode->rajoRef->refCounter++;
                    t2ListNode->firstNode->nodeRefCounter++;
                    if (bNewGroup==TRUE)
                    {
                        t2ListNode->firstNode->rajoRef->refCounter++;
                        t2ListNode->firstNode->nodeRefCounter++;
                    }
    #if defined (DEBUG_RAJO)
       fprintf(stderr, "...+... addRajoToTID2NODE(): param->tid=[%lu], a node exists already, increased counter.\n", (unsigned long) param->tid); fflush(stderr);
    #endif

                    return t2ListNode;
                }
                else    // not a node with the given rajo, create a new node, push it
                {
    #if defined (DEBUG_RAJO)
       fprintf(stderr, "...+... addRajoToTID2NODE(): param->tid=[%lu], need to create new node.\n", (unsigned long) param->tid); fflush(stderr);
    #endif

                    break;  // found TID, but firstNode is not referring same rajo
                }
            }
        }

        // if t2ListNode==NULL, no TID entry found, create one and use that
        if (t2ListNode==NULL)
        {
    // fprintf(stderr, "/// a2n marker (3--plain)...\n");fflush(stderr);
            t2ListNode=createTID2NODE(param->tid);
        }

        // find in rajo list to get at rajoRef or create new node
        rajoListNode=getOrCreateRAJO(param->env, param->rajo, param->riid);


    // fprintf(stderr, "/// a2n marker (4)...\n");fflush(stderr);
        // create new PSTRUCT_NODE
        node=createNODE (rajoListNode, param->riid);

        node->belongsToTID2NODE=(void *)t2ListNode; // save pointer to TID2NODE this node belongs to

        node->next=t2ListNode->firstNode;   // point to next node
        t2ListNode->firstNode=node;         // now let t2n point to newly created node

        if (node->next!=NULL)               // if a next node exists, let it point back
        {
            node->next->prev=node;          // let new node's next node point back to it
        }

        // maintain counters
        rajoListNode->refCounter++;
        t2ListNode->firstNode->nodeRefCounter++;

        if (bNewGroup==TRUE)
        {
            rajoListNode->refCounter++;
            t2ListNode->firstNode->nodeRefCounter++;
        }

        // 20091006, rgf: if attaching to, we need to set up the dependency data
        // if (param->attach2tid!=NULL)
        if (param->attach2tid!=0)
        {
            PSTRUCT_TID2NODE tgt_T2L =NULL;
    // fprintf(stderr, "/// a2n marker (5-1), tgt_T2L=[%p], node=[%p]...\n", tgt_T2L, node);fflush(stderr);
            // search target's T2L
            for (tgt_T2L=pRoot_TID2NODE; tgt_T2L!=NULL; tgt_T2L=tgt_T2L->next)
            {
                if (tgt_T2L->tid == param->attach2tid)
                {
                    // 20091007, rgf: if node to attach to dependsOn another, use that node's T2N instead !
                    if (tgt_T2L->firstNode->dependsOn!=NULL)
                    {
                        tgt_T2L=(PSTRUCT_TID2NODE) tgt_T2L->firstNode->dependsOn->belongsToTID2NODE;
                    }

                    break;
                }
            }
    // fprintf(stderr, "/// a2n marker (5-2a), tgt_T2L=[%p], node=[%p]...\n", tgt_T2L, node);fflush(stderr);
    // fprintf(stderr, "/// a2n marker (5-2b), tgt_T2L->firstNode=[%p] ...\n", tgt_T2L->firstNode);fflush(stderr);

            PSTRUCT_NODE tgtNode=tgt_T2L->firstNode;  // get first node in target list

            node->dependsOn=tgtNode;        // memorize that this node depends on another TID2NODE node

            if (tgtNode->firstDependent==NULL)  // no dependent node yet?
            {
    // fprintf(stderr, "/// a2n marker (5a-NEW!)...\n");fflush(stderr);
                tgtNode->firstDependent=node;   // that was easy
            }
            else                            // dependent list exists already, maintain it
            {
    // fprintf(stderr, "/// a2n marker (5b)...\n");fflush(stderr);
                node->nextDependent=tgtNode->firstDependent;    // let node point to current firstDependent
                tgtNode->firstDependent->prevDependent=node;    // let firstDependent point back to this node
                tgtNode->firstDependent=node;   // make this node the firstDependent
            }
        }


    // fprintf(stderr, "/// a2n marker (6)...\n");fflush(stderr);
        return t2ListNode;
    }



        // ---------------------------------------------------------------------------------------
        // subtract NODE counter from matching TID2NODE; if counter drops to 0, remove NODE
        // (returns TRUE=1 if removed, FALSE=0 if counter not yet 0, NOTFOUND=-1 if TID not in list)
        //
        // caller needs to have blocked access to low level structures
        // - bCloseGroup: if TRUE subtract 2 instead of 1 to close group, i.e. nodes may be reclaimed (if not used by other TIDs)
    inline int subtractRajoFromTID2NODE (JNIEnv *env,
                                #ifdef UNIX
                                    pthread_t
                                #else
                                    TID
                                #endif
                                         tid,
                                int bCloseGroup,
                                int bKeepPrimodalGroup
                             )
    {
        // if (tid==NULL)          // no tid given, get this TID
        if (tid==0)             // no tid given, get this TID
        {
            tid=RgfGetTID();
        }
    #if defined (DEBUG_RAJO)
       fprintf(stderr, "...-... subtractRajoFromTID2NODE(): param->tid=[%lu]", (unsigned long) tid); fflush(stderr);
    #endif

        PSTRUCT_RAJO   rajoListNode=NULL;
        PSTRUCT_NODE           node=NULL;
        PSTRUCT_TID2NODE t2ListNode=NULL;


    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # -1 tid=[%lu], bKeepPrimodalGroup=[%d], pRoot_TID2NODE=[%p]\n",(unsigned long) tid, bKeepPrimodalGroup, pRoot_TID2NODE);fflush(stderr);
            // find tid in tid2jrst
        for (t2ListNode=pRoot_TID2NODE; t2ListNode!=NULL; t2ListNode=t2ListNode->next)
        {
    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 0a tid=[%lu], t2ListNode=[%p]\n",(unsigned long) tid, t2ListNode);fflush(stderr);
    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 0b tid=[%lu], t2ListNode->tid=[%lu], t2ListNode->next=[%p]\n",(unsigned long) tid, (unsigned long) t2ListNode->tid, t2ListNode->next);fflush(stderr);
            if (t2ListNode->tid == tid)     // tid found !
            {

    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 01 tid=[%lu], bKeepPrimodalGroup=[%d], t2ListNode->firstNode->nodeRefCounter=[%d]\n",(unsigned long) tid, bKeepPrimodalGroup, t2ListNode->firstNode->nodeRefCounter);fflush(stderr);

                    // decrease counters, unless we should keep the primodal group (if invoked via BSFDetach()) and counter>2
                    // make sure that we have at least two references, such that BSFDetach() cannot detach from primodal thread
                if (bKeepPrimodalGroup==0 || (bKeepPrimodalGroup!= 0 && t2ListNode->firstNode->nodeRefCounter>2) )
                {
                        // maintain counters and return
                    t2ListNode->firstNode->rajoRef->refCounter--;
                    t2ListNode->firstNode->nodeRefCounter--;
    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 02 tid=[%lu], bKeepPrimodalGroup=[%d], t2ListNode->firstNode->nodeRefCounter=[%d]\n",(unsigned long) tid, bKeepPrimodalGroup, t2ListNode->firstNode->nodeRefCounter);fflush(stderr);

                    if (bCloseGroup==TRUE)  // BsfDetach() as BsfAttachToTID() make sure that counters are '1' higher than usual to keep the node
                    {
    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 03 tid=[%lu], bKeepPrimodalGroup=[%d], t2ListNode->firstNode->nodeRefCounter=[%d]\n",(unsigned long) tid, bKeepPrimodalGroup, t2ListNode->firstNode->nodeRefCounter);fflush(stderr);
                            // decrease counters, unless we should keep the primodal group (if invoked via BSFDetach()) and counter>2
                        if (bKeepPrimodalGroup==0 || (bKeepPrimodalGroup!= 0 && t2ListNode->firstNode->nodeRefCounter>2) )
                        {
                            t2ListNode->firstNode->rajoRef->refCounter--;
                            t2ListNode->firstNode->nodeRefCounter--;

    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 04 tid=[%lu], bKeepPrimodalGroup=[%d], t2ListNode->firstNode->nodeRefCounter=[%d]\n",(unsigned long) tid, bKeepPrimodalGroup, t2ListNode->firstNode->nodeRefCounter);fflush(stderr);
                        }
                    }
                }

                // if (t2ListNode->firstNode->nodeRefCounter==0)   // remove node
                if (t2ListNode->firstNode->nodeRefCounter<1)    // remove node
                {
    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 05a tid=[%lu], bKeepPrimodalGroup=[%d], t2ListNode->firstNode->nodeRefCounter=[%d]\n",(unsigned long) tid, bKeepPrimodalGroup, t2ListNode->firstNode->nodeRefCounter);fflush(stderr);
                    removeNODE(env, t2ListNode->firstNode);
    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 05b tid=[%lu], bKeepPrimodalGroup=[%d], t2ListNode->firstNode=[%p]\n",(unsigned long) tid, bKeepPrimodalGroup, t2ListNode->firstNode);fflush(stderr);

                    if (t2ListNode->firstNode==NULL)        // 20091006, no more nodes, remove TID2NODE as well
                    {
    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 06a tid=[%lu], bKeepPrimodalGroup=[%d], t2ListNode->firstNode=[%p]\n",(unsigned long) tid, bKeepPrimodalGroup, t2ListNode->firstNode);fflush(stderr);
                        removeTID2NODE(env, t2ListNode);
    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 06b tid=[%lu], bKeepPrimodalGroup=[%d], t2ListNode->firstNode=[%p]\n",(unsigned long) tid, bKeepPrimodalGroup, t2ListNode->firstNode);fflush(stderr);
                    }

    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 09a returning TRUE ! tid=[%lu], bKeepPrimodalGroup=[%d], t2ListNode->firstNode=[%p]\n",(unsigned long) tid, bKeepPrimodalGroup, t2ListNode->firstNode);fflush(stderr);
                    return TRUE;        // signal node got removed
                }

    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 09b returning FALSE ! tid=[%lu], bKeepPrimodalGroup=[%d], t2ListNode->firstNode->nodeRefCounter=[%d]\n",(unsigned long) tid, bKeepPrimodalGroup, t2ListNode->firstNode->nodeRefCounter);fflush(stderr);
                return FALSE;           // signal node was not yet removed (still references to it)
            }
        }
    // fprintf(stderr, "...-... subtractRajoFromTID2NODE(): # 09c returning -1 ! tid=[%lu], bKeepPrimodalGroup=[%d]\n",(unsigned long) tid, bKeepPrimodalGroup);fflush(stderr);
        return -1;      // TID not in TID2NODE list
    }


        // ---------------------------------------------------------------------------------------
        // find rajo for this TID, if none found return NULL
    inline PSTRUCT_RAJO findRAJO (
                                #ifdef UNIX
                                    pthread_t
                                #else
                                    TID
                                #endif
                                         tid       // thread ID
                                 )
    {
        // if (tid==NULL)          // no tid given, get this TID
        if (tid==0)             // no tid given, get this TID
        {
            tid=RgfGetTID();
        }

    #if defined (DEBUG_FIND_RAJO) || defined (DEBUG_RAJO)  // RGF, 2011-03-12, TODO
        fprintf(stderr, "findRAJO - start, tid=[%lu], pRoot_TID2NODE=[%p]\n", (unsigned long) tid, pRoot_TID2NODE );fflush(stderr);
    #endif

            // find tid in tid2jrst
        for (PSTRUCT_TID2NODE t2ListNode=pRoot_TID2NODE; t2ListNode!=NULL; t2ListNode=t2ListNode->next)
        {
    #if defined (DEBUG_FIND_RAJO) // RGF, 2011-03-12, TODO
        fprintf(stderr, "findRAJO -     , t2ListNode->tid=[%lu], tid=[%lu]\n", (unsigned long) t2ListNode->tid, (unsigned long) tid );fflush(stderr);
    #endif

            if (t2ListNode->tid == tid)    // tid found !
            {
                return t2ListNode->firstNode->rajoRef;
            }
        }
    #if defined (DEBUG_FIND_RAJO) // RGF, 2011-03-12, TODO
        fprintf(stderr, "findRAJO - end, tid=[%lu] not found!\n", (unsigned long) tid );fflush(stderr);
    #endif

        return NULL;    // not found, return NULL
    }


        // ---------------------------------------------------------------------------------------
        // find TID2NODE->firstNode for this TID, if none found return NULL
    inline PSTRUCT_NODE findFirstNODE(
        #ifdef UNIX
            pthread_t
        #else
            TID
        #endif
                 tid       // thread ID
        )
    {
        // if (tid==NULL)          // no tid given, get this TID
        if (tid==0)             // no tid given, get this TID
        {
            tid=RgfGetTID();
        }

            // find tid in tid2jrst
        for (PSTRUCT_TID2NODE t2ListNode=pRoot_TID2NODE; t2ListNode!=NULL; t2ListNode=t2ListNode->next)
        {
            if (t2ListNode->tid == tid)    // tid found !
            {
                return t2ListNode->firstNode;
            }
        }

        return NULL;    // not found, return NULL
    }



        // ---------------------------------------------------------------------------------------
        // find TID2NODE->firstNode for this TID, if none found return NULL
    inline PSTRUCT_TID2NODE findTID2NODE(
        #ifdef UNIX
            pthread_t
        #else
            TID
        #endif
                 tid       // thread ID
        )
    {
        // if (tid==NULL)          // no tid given, get this TID
        if (tid==0)             // no tid given, get this TID
        {
            tid=RgfGetTID();
        }

            // find tid in tid2jrst
        for (PSTRUCT_TID2NODE t2ListNode=pRoot_TID2NODE; t2ListNode!=NULL; t2ListNode=t2ListNode->next)
        {
            if (t2ListNode->tid == tid)    // tid found !
            {
                return t2ListNode;
            }
        }

        return NULL;    // not found, return NULL
    }

#endif


    void RexxEntry rgfInitLocks();     // rgf, 2009-04-25, initialize critical section
    void RexxEntry rgfDestroyLocks();  //                  destroy/delete it


// rgf, 2012-02-07
int REXXENTRY rexx_exit_handler_entry (RexxExitContext *context, int exitNumber, int subfunction, void *parmBlock);
RexxObjectPtr RexxEntry rexx_command_handler_entry(RexxExitContext *context, RexxStringObject address, RexxStringObject command);



// INLINE definitions

    // only for debugging:
inline VOID JAVA_TO_STRING (JNIEnv *env, jobject jo, char * buffer, int bufSize)
{
    jclass         joc=env->GetObjectClass(jo);
    jmethodID jmidName=env->GetMethodID(joc, "toString", "()Ljava/lang/String;");
    jobject       jobj=env->CallObjectMethod(jo, jmidName, NULL);

    if ( env->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
        fprintf(stderr, "!!!    JAVA_TO_STRING_ENV(), exception: jo=[%p], joc[%p]\n", jo, joc);
        fflush(stderr);
        env->ExceptionDescribe();
        env->ExceptionClear();
        SNPRINTF(buffer, bufSize, "JAVA EXCEPTION OCCURED, Java object=[%p], its class object=[%p], 'toString'-MID=[%p]\n", jo, joc, jmidName);
        env->DeleteLocalRef(joc);
        env->DeleteLocalRef(jobj);
        return;
    }

    const char *tmpStr=env->GetStringUTFChars( (jstring) jobj, JNI_FALSE);
    // strcpy(buffer, tmpStr);
    SNPRINTF(buffer, bufSize, "%s", tmpStr);    // 2009-10-02, rgf
    env->ReleaseStringUTFChars( (jstring) jobj, tmpStr);

    env->DeleteLocalRef(joc);
    env->DeleteLocalRef(jobj);
}



    // query current TID and return it
inline
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
RgfGetTID()
{
    return
        #ifdef UNIX
            pthread_self();
        #else // if defined WIN32
            GetCurrentThreadId();
        #endif
}







    // ===> 2008-07-23: MUTEX, blocking invocation, waits until lock was successfully acquired
    //                  - initializing/setting up of 'JRST_lock' is done in JNI_OnLoad()
inline VOID RgfAcquireLock()
{

#if defined (RGF_INFO) || defined (RGF_ATTACH) || defined (RGF_DETACH)
    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            tid=RgfGetTID();

    fprintf(stderr, "*** RGF_INFO: RgfAcquireLock(): tid=[%lu] ... \n", (unsigned long) tid);
    fflush(stderr);
#endif

#ifdef WINDOWS // WIN32
    #if defined (RGF_USE_MUTEX)
        WaitForSingleObject(JRST_lock, INFINITE);
    #else
        EnterCriticalSection (&JRST_lock);
    #endif

#elif defined UNIX
    pthread_mutex_lock(&JRST_lock);    // TODO: potentially deadlock on nested invocation right before return ?
#endif


#if defined (RGF_INFO) || defined (RGF_ATTACH) || defined (RGF_DETACH)
    fprintf(stderr, "*** RGF_INFO: RgfAcquireLock(): tid=[%lu] ... now ACQUIRED !\n", (unsigned long) tid);
    fflush(stderr);
#endif

}



    // ===> 2008-07-23: MUTEX, release lock
inline VOID RgfReleaseLock()
{
#if defined (RGF_INFO) || defined (RGF_ATTACH) || defined (RGF_DETACH)
    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            tid=RgfGetTID();
    fprintf(stderr, "*** RGF_INFO: RgfReleaseLock(): tid=[%lu] ...\n", (unsigned long) tid);
    fflush(stderr);
#endif

#if defined WINDOWS // WIN32
    #if defined (RGF_USE_MUTEX)
        ReleaseMutex(JRST_lock);
    #else
        LeaveCriticalSection (&JRST_lock);
    #endif
#else
    pthread_mutex_unlock(&JRST_lock);
#endif

#if defined (RGF_INFO) || defined (RGF_ATTACH) || defined (RGF_DETACH)
    fprintf(stderr, "*** RGF_INFO: RgfReleaseLock(): tid=[%lu] ... now RELEASED !\n", (unsigned long) tid);
    fflush(stderr);
#endif
}




    // inline function for reserving memory disposable for Rexx (guessing how Rexx is allocating it)
            // usage, e.g.: PSZ mem = (PSZ) RgfAllocateMemory(3451l);
// inline PVOID RgfAllocateMemory(long size)
inline PVOID RgfAllocateMemory(size_t size)
{

#if defined (USE_REGINA) || defined (USE_REXXTRANS)     // 2003-01-04, ---rgf, added defined (USE_REXXTRANS)
    #ifdef DEBUG2       // ---rgf, 2003-04-30
        fprintf(stderr, ">>> RgfAllocateMemory(): 'USE_REGINA' or 'USE_REXXTRANS' defined, RexxFreeMemory()\n");
    #endif
    return RexxAllocateMemory( size );       // RexxAllocMemory( nrOfBytes );

#elif defined WINDOWS // WIN32
    #ifdef DEBUG2       // ---rgf, 2003-04-30
        fprintf(stderr, ">>> RgfAllocateMemory(): 'WINDOWS' defined, GlobalFree()\n");
    #endif

    // #define GMEM_FIXED 0x0000       // according to docs: if GMEM_FIXED is set, then a pointer to memory is returned
    return GlobalLock( GlobalAlloc( GMEM_FIXED, size) );

#else       // o.k., use malloc()
    #ifdef DEBUG2       // ---rgf, 2003-04-30
        fprintf(stderr,">>> RgfAllocateMemory():  using malloc()\n");
    #endif
    return malloc( size );
#endif
}




        // inline function for taking care of the different ways of freeing Rexx-allocated memory
inline void RgfFreeMemory (void * memory)
{

#if defined (USE_REGINA) || defined (USE_REXXTRANS)     // 2003-01-04, ---rgf, added defined (USE_REXXTRANS)
    #ifdef DEBUG2       // ---rgf, 2003-04-30
       fprintf(stderr, ">>> RgfFreeMemory(): 'USE_REGINA' or 'USE_REXXTRANS' defined, RexxFreeMemory()\n");
    #endif
    RexxFreeMemory( memory );

#elif defined WINDOWS // WIN32
    #ifdef DEBUG2       // ---rgf, 2003-04-30
       fprintf(stderr, ">>> RgfFreeMemory(): 'WINDOWS' defined, GlobalFree()\n");
    #endif
    GlobalFree( memory );

#else       // o.k., use free()
    #ifdef DEBUG2       // ---rgf, 2003-04-30
       fprintf(stderr, ">>> RgfFreeMemory():  using free()\n");
    #endif
    free( memory );
#endif

}




    // cf. "The Java™ Native Interface Programmer’s Guide and Specification"
    // by Sheng Lian, downloaded from java.sun.org in July 2003 as PDF
    // code edited to make it runnable under this DLL
    // =======================
    // cf. "6.1.2 A Utility Function"
void JNU_ThrowByName(JNIEnv *env, const char *clzName, const char *msg)
{
    jclass jclz = env->FindClass(clzName);

    if (jclz != NULL)    // if clz is NULL, an exception has already been thrown
    {
        env->ThrowNew(jclz, msg);    // create the exception for the Java side
    }
}



    // =======================
    // cf "8.2.2 Translating jstrings to Native Strings"

// version working directly with a supplied JNIEnv and jstr
// resulting native chars buffer must be explicitly freed!
char *JNU_GetStringNativeChars(JNIEnv *env, jstring jstr)  // JNIEnv *env, jstring jstr)
{
    char *result = 0;
    jbyteArray jba = (jbyteArray) env->CallObjectMethod(jstr, defaultJVM->mid_String_getBytes);

    if ( env->ExceptionCheck()==JNI_FALSE )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
        jint len = env->GetArrayLength(jba);
        result   = (char *)RexxAllocateMemory(len + 1);
        if (result == NULL)
        {
            env->DeleteLocalRef(jba);
            JNU_ThrowByName(env, "java/lang/OutOfMemoryError", 0);
            return NULL;
        }
        env->GetByteArrayRegion(jba, 0, len, (jbyte *)result);
        result[len] = 0; /* NULL-terminate */
    }
#if defined(DEBUG40)
    else
    {
        fprintf(stderr, "JNU_GetStringNativeChars(): Exception active, panic!\n"); fflush(stderr);
        env->ExceptionDescribe();
    }
#endif

    env->DeleteLocalRef(jba);
    return result;      // resulting native chars buffer must be explicitly freed!
}
// ------------- the end of copy & pasted functions form the above JNI Guide and Specification book



// 2012-02-12, rgf, version working directly with a supplied JNIEnv and jstr;
//                  result is assigned to a PRXSTRING
void JNU_JavaString2pRxString(JNIEnv *env, jstring jstr, PRXSTRING rxstr)  // JNIEnv *env, jstring jstr)
{
    char *result = 0;

    jbyteArray jba = (jbyteArray) env->CallObjectMethod(jstr, defaultJVM->mid_String_getBytes);

    if ( env->ExceptionCheck()==JNI_FALSE )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
        jint len = env->GetArrayLength(jba);
        rxstr->strptr   =result;

        result   = (char *)RexxAllocateMemory(len + 1);
        // TODO: check for memory problem (result==NULL) and raise an error ?

        if (result != NULL)
        {
            env->GetByteArrayRegion(jba, 0, len, (jbyte *)result);
            result[len] = 0; /* NULL-terminate */
            rxstr->strlength=len;    // save length in bytes with RXSTRING
            rxstr->strptr   =result;
        }
    }
    env->DeleteLocalRef(jba);
}



// --- this macro creates the RexxString object, allowing embedded NULs to be part of the string
RexxObjectPtr JNU_GetStringNativeCharsReturningRexxStringObject(JNIEnv *env, jstring jstr, RexxThreadContext *context)
{
    char *result = 0;
    RexxObjectPtr rop=NULL;

    jbyteArray jba = (jbyteArray) env->CallObjectMethod(jstr, defaultJVM->mid_String_getBytes);

    if ( env->ExceptionCheck()==JNI_FALSE )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
        jint len = env->GetArrayLength(jba);

        result   = (char *)RexxAllocateMemory(len + 1);
        if (result == NULL)
        {
            JNU_ThrowByName(env, "java/lang/OutOfMemoryError", 0);
            env->DeleteLocalRef(jba);
            return NULL;
        }
        env->GetByteArrayRegion(jba, 0, len, (jbyte *)result);
        result[len] = 0; /* NULL-terminate */

        rop=context->NewString(result, len);  // get Rexx object off it
        RexxFreeMemory(result);        // make sure memory is freed
    }
#if defined(DEBUG40)
    else
    {
        fprintf(stderr, "JNU_GetStringNativeCharsReturningRexxString(): Exception active, panic!\n"); fflush(stderr);
        env->ExceptionDescribe();
    }
#endif

    env->DeleteLocalRef(jba);

    return rop;         // RexxObjectPointer/RexxString
}



    // rgf, 20100520: controlling creation of Java strings, this version allows NUL chars be embedded in string
jstring JNU_CreateJavaString(JNIEnv *env, RexxThreadContext *context, RexxStringObject rso)
{
        // create Java byte array from Rexx string: RexxString -> [B
        // this allows Rexx strings having embedded NUL chars to be fully converted to Java
    size_t      len=context->StringLength(rso); // get length
    const char *buf=context->CString(rso);

    if (len>0)
    {
        jbyteArray  jba=env->NewByteArray((jsize) len);     // create Java primitive byte array
        env->SetByteArrayRegion(jba, 0, (jsize) len, (jbyte *) buf);
        jstring jstr=(jstring) env->NewObject(defaultJVM->clz_String,
                            defaultJVM->mid_String_initFromByteArray,
                            jba);
        env->DeleteLocalRef(jba);
        return jstr;
    }

    // return defaultJVM->jstring_emptyUTFString;
    return env->NewStringUTF("");   // rgf, 2012-05-25: if returning "defaultJVM->jstring_emptyUTFString"
                                    // then a "DeleteLocalRef()" in caller will cause this globally sheltered
                                    // jstring to be destroyed! (observed in Java 1.6)
}

    // rgf, 20120210: controlling creation of Java strings, this version allows NUL chars be embedded in string
jstring JNU_CreateJavaStringFromCONSTRXSTRING(JNIEnv *env, RexxThreadContext *context, CONSTRXSTRING *rxstring)
{
        // create Java byte array from Rexx string: RexxString -> [B
        // this allows Rexx strings having embedded NUL chars to be fully converted to Java
    size_t      len=rxstring->strlength;

    if (len>0)
    {
        const char *buf=rxstring->strptr;
        jbyteArray  jba=env->NewByteArray((jsize) len);     // create Java primitive byte array
        env->SetByteArrayRegion(jba, 0, (jsize) len, (jbyte *) buf);
        jstring jstr=(jstring) env->NewObject(defaultJVM->clz_String,
                            defaultJVM->mid_String_initFromByteArray,
                            jba);
        env->DeleteLocalRef(jba);
        return jstr;
    }

    // return defaultJVM->jstring_emptyUTFString;
    return env->NewStringUTF("");   // rgf, 2012-05-25: if returning "defaultJVM->jstring_emptyUTFString"
                                    // then a "DeleteLocalRef()" in caller will cause this globally sheltered
                                    // jstring to be destroyed! (observed in Java 1.6)
}



    // rgf, 2008-08-06: use Java for popup message box (available on every opsys)
void JNU_MessageBox (JNIEnv *env, const char *msg)
{
    jclass    jclz = env->FindClass("javax/swing/JOptionPane");
    jmethodID  mid = env->GetStaticMethodID(jclz, "showMessageDialog", "(Ljava/awt/Component;Ljava/lang/Object;)V");
    // env->CallStaticVoidMethod(clz, mid, NULL, env->NewStringUTF(msg));
    jstring j_msg = env->NewStringUTF(msg);
    env->CallStaticVoidMethod(jclz, mid, NULL, j_msg);
    env->DeleteLocalRef(j_msg);
    env->DeleteLocalRef(jclz);
}



    // this table contains all available functions/routines
PSZ ApiFncTable[]=      // if invoked via Rexx, these are the functions we report
{
    "BSF"                         ,
//    "BsfAttachToTID"              , // ---rgf, 2003-08-06, 2014-05-15: do not report anymore (auto-attach implemented)
    "BsfContextVariables"         ,  // rgf, 2015-06-07
    "BsfCreateRexxProxy"          , // ---rgf, 2009-09-12
//    "BsfDetach"                   , // ---rgf, 2008-07-26, 2014-05-15: do not report anymore (auto-attach implemented)
    "BsfDropFuncs"                ,
    "BsfGetTID"                   , // ---rgf, 2003-08-06
    "BsfInvokedBy"                ,
    "BsfJavaException"            , // ---rgf, 2009-10-18
    "BsfLoadFuncs"                ,
#ifdef ALLOW_LOADING_JVM    //   ---rgf, 2005-08-23, #idef'ed - not for OS2 and not for Macintosh
    "BsfLoadJava"                 ,
#endif
    "BsfQueryAllFunctions"        ,
    "BsfQueryRegisteredFunctions" ,
    "BsfRawBytes"                 , // ---rgf, 2010-02-10
    "BsfRexxProxy"                , // ---rgf, 2009-09-12
    "BsfShowErrorMessage"         ,  // rgf, 2006-11-19

#ifdef ALLOW_LOADING_JVM    //   ---rgf, 2005-08-23, #idef'ed - not for OS2 and not for Macintosh
    "BsfUnloadJava"               , // ---rgf, 2005-06-02, JNI would not unload
#endif
    "BsfVersion"
};

PSZ ApiFncTable4Java[]=      // if invoked via Java, these are the functions we report
{
    "BSF"                         ,
//    "BsfAttachToTID"              , // ---rgf, 2003-08-06, 2014-05-15: do not report anymore (auto-attach implemented)
    "BsfContextVariables"         ,  // rgf, 2015-06-07
    "BsfCreateRexxProxy"          , // ---rgf, 2009-09-12
//    "BsfDetach"                   , // ---rgf, 2008-07-26, 2014-05-15: do not report anymore (auto-attach implemented)
    "BsfDropFuncs"                ,
    "BsfGetTID"                   , // ---rgf, 2003-08-06
    "BsfInvokedBy"                ,
    "BsfJavaException"            , // ---rgf, 2009-10-18
    "BsfLoadFuncs"                ,
//    "BsfLoadJava"                 ,
    "BsfQueryAllFunctions"        ,
    "BsfQueryRegisteredFunctions" ,
    "BsfRawBytes"                 , // ---rgf, 2010-02-10
    "BsfRexxProxy"                , // ---rgf, 2009-09-12
    "BsfShowErrorMessage"         ,  // rgf, 2006-11-19
//    "BsfUnloadJava"               , // ---rgf, 2005-06-02, JNI would not unload
    "BsfVersion"
};





    // short of a make_upper macro/function, this is a little hack, ---rgf, 2003-01-27
inline void make_upper (char * string)
{
    for (; *string; string++)   // loop over characters
    {
        *string=toupper(*string);   // translate characters to uppercase
    }
}




    // rgf, 2011-06-04
    // create error message text, wrap Java throwable
    // - create array and add both to it, supply array as "ADDITIONAL" array to RaiseException
    // - return error message text, such that caller can use it to set its environment
    // needs: - param (jni-env, rajo, etc.)
    //        - rtc
    //        - error message pattern containing "%16" (for DLLName) and "%s" (for error message), e.g.
    //          "%.16s/routine/BsfCreateRexxProxy(), error 4: Java exception occurred: [%s]"
// inline void RgfCreateRexxSyntaxConditionWithJavaThrowable (PSTRUCT_PARAM param, RexxThreadContext *rtc, const char * errorStringPattern)
// inline RexxStringObject RgfCreateRexxSyntaxConditionWithJavaThrowable (PSTRUCT_PARAM param, RexxThreadContext *rtc, const char * errorStringPattern)
// JNIEnv *jniEnv
// inline RexxStringObject RgfCreateRexxSyntaxConditionWithJavaThrowable (PSTRUCT_PARAM param, RexxCallContext *rcc, const char * errorStringPattern)

// jobject -> rajo

// inline RexxStringObject RgfCreateRexxSyntaxConditionWithJavaThrowable (PSTRUCT_PARAM param, RexxCallContext *rcc, const char * errorStringPattern)
// isCallContext: 0=ExitContext, 1=CallContext
inline RexxStringObject RgfCreateRexxSyntaxConditionWithJavaThrowable (
    #ifdef RGF_ATTACH_NEW
    PSTRUCT_ATTACH_PARAM
    #else
    PSTRUCT_PARAM
    #endif
    param, int isCallContext, void *argCtxt, const char * errorStringPattern)
{
    // RexxThreadContext *rtc = rcc->threadContext;
    RexxThreadContext *rtc = (isCallContext==0 ? ((RexxExitContext *) argCtxt)->threadContext
                                               : ((RexxCallContext *) argCtxt)->threadContext);

    jthrowable jo=param->env->ExceptionOccurred();    // get the Exception object

    if (bShowErrorString==1)       // print the stack?
    {
        param->env->ExceptionDescribe();    // print the stack
    }
    param->env->ExceptionClear();           // clear exception in the JVM

    RexxStringObject rso=NULL;             // RexxString used for error message
    jclass joc=param->env->GetObjectClass(jo);    // get Throwable's class object

    jmethodID jmidName=param->env->GetMethodID(joc, "toString", "()Ljava/lang/String;");    // get method ID
    jobject   jobj    =param->env->CallObjectMethod(jo, jmidName, NULL);  // invoke toString()

    if ( param->env->ExceptionCheck() )     // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
        param->env->ExceptionDescribe();
        param->env->ExceptionClear();
        rso=rtc->String("<Unfortunately, Java was not able to create a string from the Throwable, because it threw another exception while attempting to do so! :( >");
    }
    else   // copy Java string to RexxString
    {
        const char *aha=param->env->GetStringUTFChars( (jstring) jobj, JNI_FALSE);
        rso=rtc->String(aha);     // create RexxStringObject
        param->env->ReleaseStringUTFChars( (jstring) jobj, aha);
    }

       // ---> create full error message for Rexx
    size_t sz=rtc->StringLength(rso);   // get needed string length
    char * tmp=(char *) RexxAllocateMemory(sz+256);

    // SNPRINTF( tmp, sz+256, "%.16s/routine/BsfCreateRexxProxy(), error 4: Java exception occurred: [%s]", DLLNAME, context->StringData(rso));
    SNPRINTF( tmp, sz+256, errorStringPattern, DLLNAME, rtc->StringData(rso));
    rso=rtc->String(tmp);      // turn into a RexxString
    RexxFreeMemory(tmp);

       // use Rexx{Call|Exit}Context to set context variable
    if (isCallContext==0)
    {
        ((RexxExitContext *) argCtxt)->SetContextVariable(BSF_ERROR_STRING, rso);    // set also the context variable to the error message
    }
    else
    {
        ((RexxCallContext *) argCtxt)->SetContextVariable(BSF_ERROR_STRING, rso);    // set also the context variable to the error message
    }


       // ---> register Java throwable in BSFRegistry
    // create a bean of the Java object; if .BSF is available, then return a bsf.wrap() of it
    jstring j_beanName = (jstring) param->env->CallObjectMethod(param->rajo,
                                       defaultJVM->mid_RexxAndJava_registerBean4JNI,
                                       jo);

    // get char* beanName, do a bsf.wrap() if available; return that result
    const char * c_beanName    = param->env->GetStringUTFChars(j_beanName, JNI_FALSE);
    RexxStringObject r_beanName=       rtc->String(c_beanName);    // create Rexx string
    param->env->ReleaseStringUTFChars(j_beanName, c_beanName);      // release

    RexxObjectPtr bsfClz=rgfGetEntryFromLocal (rtc, "BSF"); // get .BSF, if available
    RexxObjectPtr rJavaThrowable=NULL;
    if (bsfClz!=rtc->Nil())  // class found, do a bsf.wrap() on the beanName
    {
        rJavaThrowable=rtc->SendMessage1(bsfClz, "BSF.WRAP", r_beanName);  // run bsf.wrap(), may return a BSF_PROXY object
    }
    else    // cast RexxString to RexxObject
    {
        rJavaThrowable=(RexxObjectPtr) r_beanName;
    }

       // --- now create RexxArray used for condition's "ADDITIONAL", first entry: substitution text, second entry: Java Throwable object
    RexxArrayObject ra=rtc->NewArray(2);
    rtc->ArrayPut(ra, rso, 1);     // save Throwable's string
    rtc->ArrayPut(ra, rJavaThrowable, 2);  // save wrapped Java Throwable object

        // --- make sure we remove local locks
    param->env->DeleteLocalRef(jo);
    param->env->DeleteLocalRef(joc);
    param->env->DeleteLocalRef(jobj);
    param->env->DeleteLocalRef(j_beanName);

    rtc->ReleaseLocalReference(rJavaThrowable);
    rtc->ReleaseLocalReference(bsfClz);
    rtc->ReleaseLocalReference(r_beanName);

       // --- raise exception
    rtc->RaiseException(Rexx_Error_Incorrect_call_user_defined, ra);
    rtc->ReleaseLocalReference(ra);

    return rso;            // return error string, such that caller can decide to set it to the environment as well
}


// --------------------------------------------
// 2012-02-18, rgf: have an own function to turn a single RexxObjectPtr to the appropriate
//                  Java object and return that
inline jobject RgfRexxObject2JavaObject(
#ifdef RGF_ATTACH_NEW
    PSTRUCT_ATTACH_PARAM
#else
    PSTRUCT_PARAM
#endif
            param, RexxThreadContext *context, RexxObjectPtr tmpObj)
{
    // get the indicator value; returns "2" (BSF/UNO_PRoxy), "1" (String), "0" (RexxProxy),
    // "-1" (NULL), "-2" (.nil)
    int typeIndicator=RgfValue4JavaIndicator(context, tmpObj);

    jobject jresult=NULL;   // default for -1, -2

#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_REXX_OBJECT_TO_JAVA) || defined (DEBUG_ARRAY_ARGS)
    RexxStringObject strTmpObj= ( tmpObj==NULL ? NULL : RgfDebug_GetMaxStringValue(context, tmpObj,90) ) ;
    fprintf(stderr, "... RgfRexxObject2JavaObject():b: typeIndicator=[%d], maxStringValue=[%.256s]\n",
                         typeIndicator,
                         ( strTmpObj==NULL ? "NULL" : context->CString(strTmpObj)));
#endif


    if (typeIndicator!=-1)     // a RexxString, BSF/UNO_Proxy or any other RexxObject
    {

        if (typeIndicator==1)   // a .STRING
        {

#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_REXX_OBJECT_TO_JAVA) || defined (DEBUG_ARRAY_ARGS)
    // fprintf(stderr, "-> BSF(): \targ[%u] STRING       val=[%.256s]\n",i, context->CString(context->ObjectToString(tmpObj))); fflush(stderr);
    fprintf(stderr, "-> RgfRexxObject2JavaObject():c: \tSTRING       val=[%.256s]\n",
       context->CString(RgfDebug_GetMaxStringValue(context, tmpObj, 180))); fflush(stderr);
#endif

            // param->env->SetObjectArrayElement(arr, (jsize) i,
            jresult=JNU_CreateJavaString(param->env,
                              context,
                              context->IsString(tmpObj) ? (RexxStringObject) tmpObj
                                                        : context->ObjectToString(tmpObj) // needed for internal strings (e.g. for numbers!)
                                         );
        }
        else    // this is a non-String Rexx object, create a proxy for Java !
        {
                // if .BSF or .UNO_proxy objects, they are registered in the BSFRegistry by their objectname
            if (typeIndicator>1)    // a BSF/UNO_PROXY object in hand, Java object resides in BSF registry
            {
#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_REXX_OBJECT_TO_JAVA) || defined (DEBUG_ARRAY_ARGS)
    fprintf(stderr, "-> RgfRexxObject2JavaObject():d: \tBSF/UNO_PROXY val=[%.256s]\n",context->CString(RgfDebug_GetMaxStringValue(context, tmpObj, 90)) ); fflush(stderr);
#endif
                // lookup the Java object from the BSF registry and return that Java object, i.e.
                // get bean from the Java side --------------------------------------------------
                jstring jstr=param->env->NewStringUTF(context->CString(context->ObjectToString(tmpObj)));
                jresult=param->env->CallObjectMethod(param->rajo,
                                       defaultJVM->mid_RexxAndJava_lookupBean4JNI,
                                       jstr);
                param->env->DeleteLocalRef(jstr);
            }
            else    // any other Rexx object, wrap it up as a (Java) RexxProxy
            {

#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_REXX_OBJECT_TO_JAVA) || defined (DEBUG_ARRAY_ARGS)
    char *tmpStrObject=(char *) context->CString(context->ObjectToString(tmpObj));
    fprintf(stderr, "-> RgfRexxObject2JavaObject(): debug! \tis a non-String, non-BSF/UNO RexxObject, type=[%.256s]: creating a RexxProxy for it!\n\t~string=[%.256s]\n",
                     context->CString(context->ObjectToString(context->SendMessage0(tmpObj,"CLASS"))),
                     tmpStrObject);
    fflush(stderr);
#endif
                    // create RexxProxy for it
                jresult=RgfCreateRexxProxy (param->env, param->rajo,
                                            context,    // ThreadContext, RMG: 20090514
                                            tmpObj,     // Rexx object
                                            NULL,       // Package object
                                            NULL        // userData (slot)
                                            );

#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_REXX_OBJECT_TO_JAVA) || defined (DEBUG_ARRAY_ARGS)
     fprintf(stderr, "-> RgfRexxObject2JavaObject(): AFTER creating RexxProxy jresult=[%p], CheckCondition()=[%d]\n",jresult,context->CheckCondition());fflush(stderr);
#endif
            }
        }
    }

#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_REXX_OBJECT_TO_JAVA) || defined (DEBUG_ARRAY_ARGS)
    else
    {
        fprintf(stderr, "-> RgfRexxObject2JavaObject():e:  before returning jresult=[%p], typeIndicator=[%d]\n", jresult, typeIndicator);
        fflush(stderr);
    }
#endif

    return jresult;
}



// 2012-02-10, rgf: have an own function to process a Rexx (arg) array and turn it into
//                  a Java array; meant to be used by BSF(), but also in the exit/command handlers
// TODO: after testing with handlers, use it also in BSF(), such that we have a single implementation
//  -- arrToRemove is optional (hence may be set to NULL)
jobjectArray RgfRexxArray2JavaArray(
#ifdef RGF_ATTACH_NEW
    PSTRUCT_ATTACH_PARAM
#else
    PSTRUCT_PARAM
#endif
            param, RexxThreadContext *context, RexxArrayObject argArray)
{
// *** >>>> ????
#if defined (RGF_INFO_1) || defined (DEBUG_ARRAY_ARGS) // rgf, 20090820
    fprintf(stderr, "RgfRexxArray2JavaArray():\t 1 - just arrived, CheckCondition()=[%d]...\n", context->CheckCondition());fflush(stderr);
#endif
    if (argArray==NULL)     // no object passed in, then return NULL as well
    {
        return NULL;
    }

    // size_t numArgs=context->ArrayItems(argArray);   // get number of arguments
    size_t numArgs=context->ArraySize(argArray);   // get number of arguments

    //convert arguments array to Java format
    jobjectArray jarr = param->env->NewObjectArray (
                              (jsize) numArgs,      // needed size (number of elements)
                              // defaultJVM->clz_String,   // (*env).FindClass("java/lang/String"),
                              defaultJVM->clz_Object,   // (*env).FindClass("java/lang/String"),
                              NULL
                              // defaultJVM->jstring_emptyUTFString// (*env).NewStringUTF("")
                              );
#if defined (RGF_INFO_1) || defined (DEBUG_ARRAY_ARGS) // rgf, 20090820
    fprintf(stderr, "RgfRexxArray2JavaArray():\t 2 - after creating Java array object 'jarr', CheckCondition()=[%d]...\n", context->CheckCondition());fflush(stderr);
#endif


#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_BSF_FUNCTION) || defined (DEBUG_ARRAY_ARGS)

    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
        tid=RgfGetTID();

    fprintf(stderr, "--- RGF_INFO: RgfRexxArray2JavaArray(): 3, tid=[%lu], numArgs=[%lu]", (unsigned long) tid, numArgs);fflush(stderr);
    if (numArgs>0)
    {

    // rgf, 20090820
    fprintf(stderr, "\n"); fflush(stderr);
    for (size_t i=0; i<numArgs; i++)
    {
       RexxObjectPtr tmpObj=context->ArrayAt(argArray, i+1);
       fprintf(stderr, "\ti=%lu, tmpObj: %%lu=%lu %%p=%p", i+1, (unsigned long) tmpObj, tmpObj);
       fflush(stderr);

       int tind=RgfValue4JavaIndicator(context, tmpObj);
       fprintf(stderr, ", typeIndicator=%d\n", tind);
       fflush(stderr);
    }

        for (size_t i=0; i<numArgs; i++)   // iterate over all Rexx arguments
        {
            RexxObjectPtr tmpObj=context->ArrayAt(argArray, i+1);
            int tind=RgfValue4JavaIndicator(context, tmpObj);
            context->ReleaseLocalReference(tmpObj);

            fprintf(stderr, "[%lu]=", i+1); fflush(stderr);
            fprintf(stderr, "[%.256s]", (tind<0?"<null>":context->CString(tmpObj))); fflush(stderr);
            // fprintf(stderr, " | [%u]=[%.256s]", i+1, context->CString(context->ArrayAt(argArray, i+1)));
        }
    }
    fprintf(stderr, " ---\n");
    fflush(stderr);
#endif

        // fill the Java argument array with the passed in objects
    for (size_t i=0; i<numArgs; i++)   // iterate over all Rexx arguments
    {
        RexxObjectPtr tmpObj=context->ArrayAt(argArray, i+1);    // get Object from ooRexx argument array list
        jobject jobj=RgfRexxObject2JavaObject(param,context,tmpObj);
        context->ReleaseLocalReference(tmpObj);
        param->env->SetObjectArrayElement(jarr, (jsize) i, jobj);
        param->env->DeleteLocalRef(jobj);
    }

#if defined (RGF_INFO_1) || defined (DEBUG_ARRAY_ARGS) // rgf, 20120221
    fprintf(stderr, "RgfRexxArray2JavaArray():\t 4 - before returning, CheckCondition()=[%d]...\n", context->CheckCondition());fflush(stderr);
#endif

    return jarr;
}




/*
// 2012-02-10, rgf: have an own function to process a Rexx (arg) array and turn it into
//                  a Java array; meant to be used by BSF(), but also in the exit/command handlers
// TODO: after testing with handlers, use it also in BSF(), such that we have a single implementation
//  -- arrToRemove is optional (hence may be set to NULL)
jobjectArray RgfRexxArray2JavaArray__(PSTRUCT_PARAM param, RexxThreadContext *context, RexxArrayObject argArray, jobjectArray arrToRemove)
{
// *** >>>> ????
    if (argArray==NULL)     // no object passed in, then return NULL as well
    {
        return NULL;
    }

    // size_t numArgs=context->ArrayItems(argArray);   // get number of arguments
    size_t numArgs=context->ArraySize(argArray);   // get number of arguments

    //convert arguments array to Java format
    jobjectArray arr = param->env->NewObjectArray (
                              (jsize) numArgs,      // needed size (number of elements)
                              // defaultJVM->clz_String,   // (*env).FindClass("java/lang/String"),
                              defaultJVM->clz_Object,   // (*env).FindClass("java/lang/String"),
                              NULL
                              // defaultJVM->jstring_emptyUTFString// (*env).NewStringUTF("")
                              );
#ifdef RGF_INFO_1 // rgf, 20090820
    fprintf(stderr, "RgfRexxArray2JavaArray():\t 1 - after creating Java array object 'arr'...\n");fflush(stderr);
#endif

    // 4.0: if creating RexxProxy objects, they get registered in BSFRegistry, hence
    //      we need to remove them from the BSFRegistry after the BSF-invocation; this
    //      array will remember the beanNames of these
    if (arrToRemove!=NULL) {
           arrToRemove=param->env->NewObjectArray(
                               (jsize) numArgs,     // the maximum number of entries
                               defaultJVM->clz_String,  // String type
                               NULL                 // use NULL for initialization
                               );
#ifdef RGF_INFO_1 // rgf, 20090820
    fprintf(stderr, "RgfRexxArray2JavaArray():\t 2 - after creating Java array object 'arrToRemove'...\n");fflush(stderr);
#endif
    }

    size_t nrToRemove=0;                        // memorize nr of entries to remove


#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_BSF_FUNCTION)

    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
        tid=RgfGetTID();

    fprintf(stderr, "--- RGF_INFO: RgfRexxArray2JavaArray(): 1, tid=[%lu], numArgs=[%lu]", (unsigned long) tid, numArgs);fflush(stderr);
    if (numArgs>0)
    {

    // rgf, 20090820
    fprintf(stderr, "\n"); fflush(stderr);
    for (size_t i=0; i<numArgs; i++)
    {
       RexxObjectPtr tmpObj=context->ArrayAt(argArray, i+1);
       fprintf(stderr, "\ti=%lu, tmpObj: %%lu=%lu %%p=%p", i+1, (unsigned long) tmpObj, tmpObj);
       fflush(stderr);

       int tind=RgfValue4JavaIndicator(context, tmpObj);
       fprintf(stderr, ", typeIndicator=%d\n", tind);
       fflush(stderr);
    }

        for (size_t i=0; i<numArgs; i++)   // iterate over all Rexx arguments
        {
            RexxObjectPtr tmpObj=context->ArrayAt(argArray, i+1);
            int tind=RgfValue4JavaIndicator(context, tmpObj);
            fprintf(stderr, "[%lu]=", i+1); fflush(stderr);
            fprintf(stderr, "[%.256s]", (tind<0?"<null>":context->CString(tmpObj))); fflush(stderr);
            // fprintf(stderr, " | [%u]=[%.256s]", i+1, context->CString(context->ArrayAt(argArray, i+1)));
        }
    }
    fprintf(stderr, " ---\n");
    fflush(stderr);
#endif


        // fill the Java argument array with the passed in objects
    for (size_t i=0; i<numArgs; i++)   // iterate over all Rexx arguments
    {
        RexxObjectPtr tmpObj=context->ArrayAt(argArray, i+1);    // get Object from ooRexx argument array list


        // get the indicator value; returns "2" (BSF/UNO_PRoxy), "1" (String), "0" (RexxProxy),
        // "-1" (NULL), "-2" (.nil)
        int typeIndicator=RgfValue4JavaIndicator(context, tmpObj);

        if (typeIndicator<0)        // NULL (argument missing) or .nil
        {
#if defined (DEBUG_REXX_PROXY_BSF)
    fprintf(stderr, "-> RgfRexxArray2JavaArray():a: \targ[%lu] NULLOBJECT \n",(unsigned long) i); fflush(stderr);
#endif

            param->env->SetObjectArrayElement(arr, (jsize) i, NULL);
        }
        else    // either a string object or another ooRexx object in hand
        {

#if defined (DEBUG_REXX_PROXY_BSF)
    RexxStringObject strTmpObj= RgfDebug_GetMaxStringValue(context, tmpObj, 90) ;
    fprintf(stderr, "... RgfRexxArray2JavaArray():b: arg[%lu]=[%.256s]\n", (unsigned long) i, context->CString(strTmpObj) );

    // fprintf(stderr, "... BSF(): arg[%u]=[%.256s]\n", i, context->CString(context->SendMessage0(tmpObj, "STRING")));
#endif

                // get the ooRexx String class object
            // if (context->IsInstanceOf(tmpObj, STRING_CLASS(context->threadContext)))     // is the object an instance of it?
            // if (context->IsOfType(tmpObj, "STRING"))      // is the object an instance of .String ?
            if (typeIndicator==1)   // a .STRING
            {

#if defined (DEBUG_REXX_PROXY_BSF)
    // fprintf(stderr, "-> BSF(): \targ[%u] STRING       val=[%.256s]\n",i, context->CString(context->ObjectToString(tmpObj))); fflush(stderr);
    fprintf(stderr, "-> RgfRexxArray2JavaArray():c: \targ[%lu] STRING       val=[%.256s]\n",(unsigned long) i,
       context->CString(RgfDebug_GetMaxStringValue(context, tmpObj, 180))); fflush(stderr);
#endif

                param->env->SetObjectArrayElement(arr, (jsize) i,
                                JNU_CreateJavaString(param->env,
                                        context,
                                        context->IsString(tmpObj) ? (RexxStringObject) tmpObj
                                                                  : context->ObjectToString(tmpObj)
                                       )
                                   );

//                     char *tmpStrObject=(char *) context->CString(context->ObjectToString(tmpObj));
//                     param.env->SetObjectArrayElement(arr, (jsize) i, param.env->NewStringUTF(tmpStrObject));

            }
            else    // TODO: this is a non-String Rexx object, create a proxy for Java !
            {
                    // if .BSF or .UNO_proxy objects, they are registered in the BSFRegistry by their objectname
                // if ( context->IsOfType(tmpObj, "BSF") || context->IsOfType(tmpObj, "UNO_PROXY") ||
                //      (context->IsOfType(tmpObj, "CLASS") && context->HasMethod(tmpObj, "BSF.LOADCLASS"))
                // )
                if (typeIndicator>1)    // a BSF/UNO_PROXY object in hand
                {
#if defined (DEBUG_REXX_PROXY_BSF)
    fprintf(stderr, "-> RgfRexxArray2JavaArray():d: \targ[%lu] BSF/UNO_PROXY val=[%.256s]\n",(unsigned long) i, context->CString(RgfDebug_GetMaxStringValue(context, tmpObj, 90)) ); fflush(stderr);
#endif
                    char *tmpStrObject=(char *) context->CString(context->ObjectToString(tmpObj));
                    param->env->SetObjectArrayElement(arr, (jsize) i, param->env->NewStringUTF(tmpStrObject));
                }
                else    // any other Rexx object, submit as a RexxProxy argument
                {

#if defined (DEBUG_REXX_PROXY_BSF)
    char *tmpStrObject=(char *) context->CString(context->ObjectToString(tmpObj));
    fprintf(stderr, "-> RgfRexxArray2JavaArray(): debug! \targ[%lu] is a non-String, non-BSF/UNO RexxObject, type=[%.256s]: creating a RexxProxy for it!\n\t~string=[%.256s]",
                     (unsigned long) i,
                     context->CString(context->ObjectToString(context->SendMessage0(tmpObj,"CLASS"))),
                     tmpStrObject);
    fflush(stderr);
#endif
                        // create RexxProxy for it
                    jobject j_rexxProxy=RgfCreateRexxProxy (param->env, param->rajo,
                                                            context,    // ThreadContext, RMG: 20090514
                                                            tmpObj,     // Rexx object
                                                            NULL,       // Package object
                                                            NULL        // userData (slot)
                                                            );

// TODO: rgf, 2012-02-11: probably no need to save Java Rexx Proxy in BSF registry
//                        as the Java object is returned to Java
                    if (arrToRemove!=NULL) // keep old (as of 2012-02-11) behaviour
                    {
                            // store beanName (without prefix) in BSFRegistry, hence we can keep the String[] signature
                        jstring j_bsfRegistryKey=RgfStoreRexxProxyInBsfRegistry(param->env, param->rajo, j_rexxProxy, 0);

                            // save the beanName with the array
                        param->env->SetObjectArrayElement(arr, (jsize) i, j_bsfRegistryKey);

                            // add to remove list (bean needs to removed from BSFRegistry after the run)
                        param->env->SetObjectArrayElement(arrToRemove, (jsize) nrToRemove++, j_bsfRegistryKey);
                    }
                    else
                    {
                            // save the j_rexxProxy with the array
                        param->env->SetObjectArrayElement(arr, (jsize) i, j_rexxProxy);
                    }
                }
            }
        }
    }

    // rgf, 2008-07-28: TODO check whether needed, if test-phase over
// a_JRST->penv->MonitorEnter(a_JRST->rajo);    // make sure other threads accessing the same object are blocked

#if defined(RGF_INFO_1) || defined (DEBUG_BSF_FUNCTION) // || defined (RGF_BIT2)
    fprintf(stderr, "*** *** RGF_INFO_1: RgfRexxArray2JavaArray(): 4 - <==> (BEFORE -> CallObjectMethod(), tid=[%lu] ... ", (unsigned long) tid);
    fflush(stderr);


    if ( param->env->ExceptionCheck() )
    {
        fprintf(stderr, "Sch..., ExceptionCheck() yields JNI_TRUE!");
        // rgf, 20090912: gcc thinks the following is not available in this scope ?
        // fprintf(stderr, "Sch..., ExceptionCheck()=[%d]", a_JRST->penv>ExceptionCheck());
    }

    fprintf(stderr, "\n");
    fflush(stderr);
#endif



#if defined( DEBUG_REXX_PROXY_BSF )

// inline VOID JAVA_TO_STRING (JNIEnv *env, jobject jo, char * buffer)
{
    char tmpBuf[1024];
    JAVA_TO_STRING( param->env, param->rajo, tmpBuf, 1024);
    fprintf(stderr, "*** *** DEBUG_REXX_PROXY_BSF: RgfRexxArray2JavaArray(): 1 - tid=[%lu], a_JRST->rajo=[%p] [%s]\n",
                                     (unsigned long) tid,
                                     param->rajo,
                                     tmpBuf
                                     );
    fflush(stderr);
}

#endif
    return arr;

// *** <<< ???

}
*/


// 2012-02-10, rgf: have an own function to process a Rexx (arg) array and turn it into
//                  a Java array; meant to be used by BSF(), but also in the exit/command handlers
// TODO: after testing with handlers, use it also in BSF(), such that we have a single implementation
//  -- arrToRemove is optional (hence may be set to NULL)

// 2012-02-10, rgf: old Rexx style (i.e. only Rexx strings or empty strings !)
//                  a Java array; meant to be used by BSF(), but also in the exit/command handlers
inline jobjectArray RgfConstrxStringArray2JavaArray(
#ifdef RGF_ATTACH_NEW
    PSTRUCT_ATTACH_PARAM
#else
    PSTRUCT_PARAM
#endif
        param, RexxThreadContext *context, PCONSTRXSTRING argv, unsigned short argc)
{
    if (argc==0 || argv==NULL)      // no arguments, no pointer to args
    {
        return NULL;
    }

    //convert arguments array to Java format
    jobjectArray jarr = param->env->NewObjectArray (
                              (jsize) argc,      // needed size (number of elements)
                              defaultJVM->clz_String,   // (*env).FindClass("java/lang/String"),
                              defaultJVM->jstring_emptyUTFString// (*env).NewStringUTF("")
                              );

#ifdef RGF_INFO_1
    fprintf(stderr, "RgfConstrxStringArray2JavaArray():\t 1 - after creating Java array object 'jarr'...\n");fflush(stderr);
#endif


        // fill the Java argument array with the passed in objects
    for (size_t i=0; i<argc; i++)   // iterate over all Rexx arguments
    {
        if (argv[i].strlength>0) // set Java String element to an empty Java string
        {
            jstring jstr=JNU_CreateJavaStringFromCONSTRXSTRING(param->env, context, &argv[i]);
            param->env->SetObjectArrayElement(jarr, (jsize) i, jstr);
            param->env->DeleteLocalRef(jstr);
        }
    }

#ifdef RGF_INFO_1
    fprintf(stderr, "RgfConstrxStringArray2JavaArray():\t 2 - before returning String array object 'jarr'...\n");fflush(stderr);
#endif

    return jarr;
}




    /*********************************************************************/
    /* Queries how BSF4Rexx was invoked and whether JVM is present       */
    /*    returns: 0 ... no JVM present                                  */
    /*             1 ... JVM present, bsf4rexx invoked by Java           */
    /*             2 ... JVM present, bsf4rexx invoked by Rexx as an     */
    /*                   external function package                       */
    /*                                                                   */
    /* ---rgf, 2003-01-21                                                */
    /*********************************************************************/
RexxRoutine0(CSTRING, BsfInvokedBy) // 20090505, ---rgf
{
#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfInvokedBy() 1, bsfInvokedBy=[%d] ...\n", bsfInvokedBy);
    fflush(stderr);
#endif

    switch (bsfInvokedBy) {
        case  0:  return "0";   // no JVM present
        case  1:  return "1";   // JVM present, BSF4Rexx invoked by Java
        default:  return "2";   // JVM present, BSF4Rexx invoked by Rexx
    }
}







    /*********************************************************************/
    /* 2011-01-06/07: APPLE version: load JVM in its own thread          */
    /*                                                                   */
    /*********************************************************************/

/*

from: <http://lists.apple.com/archives/java-dev/2009/Oct/msg00511.html>, <http://lists.apple.com/archives/java-dev/2009/Jan/msg00019.html>

char firstThreadEnvVariable[80];
sprintf(firstThreadEnvVariable, "JAVA_STARTED_ON_FIRST_THREAD_%d", getpid());
setenv(firstThreadEnvVariable, "1", 1);

---

*/


#ifdef __APPLE__   // rgf, 2011-01-06, 2011-01-07, 2011-01-12


/*

// call back for dummy source used to make sure the CFRunLoop doesn't exit right away
// This callback is called when the source has fired.
void sourceCallBack (  void *info  ) {}



inline int createAndRunLoop()
{

    CFRunLoopSourceContext sourceContext;

    // Create a a sourceContext to be used by our source that makes

    // sure the CFRunLoop doesn't exit right away
    sourceContext.version = 0;
    sourceContext.info = NULL;
    sourceContext.retain = NULL;
    sourceContext.release = NULL;
    sourceContext.copyDescription = NULL;
    sourceContext.equal = NULL;
    sourceContext.hash = NULL;
    sourceContext.schedule = NULL;
    sourceContext.cancel = NULL;
    sourceContext.perform = &sourceCallBack;

    // Create the Source from the sourceContext
    CFRunLoopSourceRef sourceRef = CFRunLoopSourceCreate (NULL, 0, &sourceContext);

    // Use the constant kCFRunLoopCommonModes to add the source to the set of objects

    // monitored by all the common modes
    CFRunLoopAddSource (CFRunLoopGetCurrent(),sourceRef,kCFRunLoopCommonModes);

    // Park this thread in the runloop
    CFRunLoopRun();

    return 0;
}
*/

//----------------------



    // according to Apple we must load the JVM in another thread such that the Java awt-thread is not created on the main thread
    // which would interfere with Apple's event dispatch loop; cf. <http://developer.apple.com/library/mac/#technotes/tn2005/tn2147.html>,
    // which links to <http://developer.apple.com/library/mac/#samplecode/simpleJavaLauncher/Introduction/Intro.html>

    // however: swt seems to need to run in main (first) thread

    // on pthreads: <http://www.cognitus.net/html/howto/pthreadSemiFAQ_4.html>, rgf, 2011-01-09

// still problem on 2011-01-10, researching:
//     <http://web.archiveorange.com/archive/v/Hd19lJzSPWTFf9btyoAx>
//     <http://www.crap4j.org/news/?cat=1>
//     <https://bugs.eclipse.org/bugs/show_bug.cgi?id=211625>, some interesting pieces of infos

    /* load the JVM, detach before ending thread */
// void * loadJVM4Apple(PVOID arguments)
void loadJVM4Apple(PVOID arguments)
{
	PVOID *args=(PVOID *) arguments;
	JNIEnv          **pjniEnv=(JNIEnv **)        args[0];
	JavaVMInitArgs *pvm_args2=(JavaVMInitArgs *) args[1];
	int		     *res=(int *)            args[2];
        rgf_JNI_CreateJavaVM rgfCreateJavaVM = (rgf_JNI_CreateJavaVM) args[3];

#if not defined (RGF_LOAD_JVM_IN_SEPARATE_THREAD)
        // rgf, 2011-01-09, from: <http://lists.apple.com/archives/java-dev/2009/Oct/msg00511.html>
        char firstThreadEnvVariable[80];
        sprintf(firstThreadEnvVariable, "JAVA_STARTED_ON_FIRST_THREAD_%d", getpid());
        setenv(firstThreadEnvVariable, "1", 1);
#endif


#ifdef DEBUG
{
	JavaVMInitArgs vm_args2=*pvm_args2;

	fprintf(stderr, "loadJVM4Apple(): __APPLE__, vm_args2.options=[%p], vm_args2.nOptions=[%d]\n", vm_args2.options, (int) vm_args2.nOptions);
#ifdef DEBUG3
	for (int m=0;m<vm_args2.nOptions;m++)
	{
    		fprintf(stderr, "\t---> options[%d]=[%.256s] \n", m, vm_args2.options[m].optionString);
	}
#endif
    	fprintf(stderr, "\t--- --- --- \n" ); 	fflush(stderr);
}
#endif



#ifdef DEBUG
	fprintf(stderr, "loadJVM4Apple(): __APPLE__, about to invoke 'rgfCreateJavaVM(...)\n"); fflush(stderr);
        fprintf(stderr, "               pjniEnv=[%p], pvm_args2=[%p], rgfCreateJavaVM=[%p]\n", pjniEnv, pvm_args2, rgfCreateJavaVM); fflush(stderr);
#endif

	*res = (int) rgfCreateJavaVM( &currentJVM, (void **) pjniEnv, pvm_args2);

#ifdef DEBUG
    fprintf(stderr, "loadJVM4Apple(): __APPLE__, *res=[%d] \n", *res);

    fprintf(stderr, "loadJVM4Apple(): __APPLE__, #3b, before FindClass(\"java/lang/System\"), jniEnv=[%p]...\n", *pjniEnv); fflush(stderr);

    jclass clzTest=(*pjniEnv)->FindClass("java/lang/System");
    fprintf(stderr, "loadJVM4Apple(): __APPLE__, #4b, after  FindClass(\"java/lang/System\"(), clzTest=[%p]...\n", clzTest); fflush(stderr);
#endif

	currentJVM->DetachCurrentThread();

#ifdef DEBUG
    fprintf(stderr, "loadJVM4Apple(): __APPLE__, #55, after DetachCurrentThread()\n");fflush(stderr);
#endif


// 	int resDet=pthread_detach(pthread_self());   // rgf, just for testing purposes



// will hang on loading java.awt.Frame
// TODO: check if not in headless mode then create an awt object to force creation of the awt thread
/*
fprintf(stderr, "loadJVM4Apple(): __APPLE__, #5a, before FindClass(\"java/awt/Frame\") ...\n"); fflush(stderr);
jclass clzFrame=(*pjniEnv)->FindClass("java/awt/Frame");
fprintf(stderr, "loadJVM4Apple(): __APPLE__, #5a, after  FindClass(), clzFrame=[%p]...\n", clzFrame); fflush(stderr);

	int resDet=pthread_detach(pthread_self());   // rgf, just for testing purposes
*/



#ifdef RGF_TEST_LOOP_SLEEP

 	int resDet=pthread_detach(pthread_self());   // rgf, just for testing purposes
fprintf(stderr, "loadJVM4Apple(): __APPLE__, #99, after getpid=[%d], pthread_self()=[%lu], pthread_detach(), resDat=[%d] ...\n", getpid(), (unsigned long) pthread_self(), resDet); fflush(stderr);


// let us keep this thread alive eternally (for testing purposes)
	int tmpVal=1;
        unsigned long ul=0lu;
	while (tmpVal==1)
        {
		struct timeval timeout;
		timeout.tv_sec = 1;
		timeout.tv_usec = 0; // 100000;
fprintf(stderr, "loadJVM4Apple(): __APPLE__, [%lu] #222, before sleeping ...\n", ++ul);fflush(stderr);

		select( 0, NULL, NULL, NULL, & timeout );

fprintf(stderr, "loadJVM4Apple(): __APPLE__, [%lu] #333, after sleeping.\n", ul);fflush(stderr);

	}
fprintf(stderr, "loadJVM4Apple(): __APPLE__, #999, after 'ETERNAL LOOP' in thread !\n");fflush(stderr);

#endif

}




    /* main function that controls how the JVM gets loaded (on current thread or separate thread) */
jint loadJVM4AppleMain (JNIEnv **pjniEnv, JavaVMInitArgs *pvm_args2,  rgf_JNI_CreateJavaVM rgfCreateJavaVM)
{
	int   res=-999;
	PVOID args[] = {(void *) pjniEnv, (void *) pvm_args2, (void *) &res, (void *) rgfCreateJavaVM, NULL};


#ifdef RGF_LOAD_JVM_IN_SEPARATE_THREAD
        /* Have the JVM loaded in its own thread, get the JVM-loading result and return it. */

	/* Start the thread that runs the VM. */
	pthread_t vmthread;

	/* create a new pthread copying the stack size of the primordial pthread */
	struct rlimit limit;
	size_t stack_size = 0;

	int rc = getrlimit(RLIMIT_STACK, &limit);

	if (rc == 0)
	{
		if (limit.rlim_cur != 0LL)
		{
			stack_size = (size_t)limit.rlim_cur;
		}
	}

	pthread_attr_t thread_attr;
	pthread_attr_init(&thread_attr);

        pthread_attr_setdetachstate( & thread_attr, PTHREAD_CREATE_JOINABLE );   // rgf: allow to join this thread

	pthread_attr_setscope(&thread_attr, PTHREAD_SCOPE_SYSTEM);
//	pthread_attr_setdetachstate(&thread_attr, PTHREAD_CREATE_DETACHED);
	if (stack_size > 0)
	{
		pthread_attr_setstacksize(&thread_attr, stack_size);
	}


#ifdef DEBUG
    fprintf(stderr, "loadJVMinNewThread(): __APPLE__, getpid()=[%d], pthread_self()=[%lu], ...\n", getpid(), (unsigned long) pthread_self()); fflush(stderr);
#endif

	/* Start the thread that we will start the JVM on. */
	int res1=pthread_create(&vmthread, &thread_attr, loadJVM4Apple, args);


#ifdef DEBUG
	fprintf(stderr, "loadJVMinNewThread(): __APPLE__, after pthread_create (...)\n");
        fprintf(stderr, "                      res=[%d], res1=[%d] \n", (int)res, res1);  fflush(stderr);
#endif
	pthread_attr_destroy(&thread_attr);

	int res2=pthread_join(vmthread, NULL);	// wait until JVM creation thread ends, such that its return code is available via 'res'

/*
fprintf(stderr, "loadJVMinNewThread(): __APPLE__, BEFORE createAndRunLoop ...\n"); fflush(stderr);

        int res99=createAndRunLoop();

fprintf(stderr, "loadJVMinNewThread(): __APPLE__, AFTER createAndRunLoop.\n"); fflush(stderr);
*/


#ifdef DEBUG
	fprintf(stderr, "loadJVMinNewThread(): __APPLE__, after pthread_join(...)\n");
        fprintf(stderr, "                      res=[%d], res2=[%d] \n", (int)res, res2);  fflush(stderr);
#endif

	if (res2==0) return (jint) res;

	return (jint) -1;     // indicate that something went wrong joining




#else   // just load the VM on current thread
	loadJVM4Apple(args);
        return (jint) res;
#endif

}

#endif



    /* 2006-01-04, ---rgf: changed handling of arguments for JNI_1_2 and up:
       - arguments are passed on to the JVM as is; this way one can use the
         "-D<name>=<value>", "-verbose[:{class|gc|jni|X-non-standard-name}[,...]]
    */


    /*********************************************************************/
    /* load Java                                                         */
    /*                                                                   */
    /* ALL NEW BEHAVIOR FOR JNI_1_2 and up since 2006-01-04:             */
    /*                                                                   */
    /*    if Rexx-arguments are given, they are passed along to Java     */
    /*    as startup arguments (e.g. "-Djava.class.path=.")              */
    /*                                                                   */
    /*    if no Rexx argument sets 'java.class.path' and the environment */
    /*    variable CLASSPATH is given, then that value is used to pass   */
    /*    it on to the JVM (a "-Djava.class.path=$CLASSPATH"             */
    /*                                                                   */
    /*                                                                   */
    /* returns:                                                          */
    /*            0 ... JVM got loaded                                   */
    /*            1 ... JVM got loaded in earlier runs, merely made      */
    /*                  it available again                               */
    /*              ... else, whatever JNI returned                      */
    /*         -999 ... if JVM already loaded                            */
    /*         -998 ... first argument not Append nor Prepend            */
    /*         -997 ... argument #1 exceeds 1023 bytes                   */
    /*         -996 ... length of Java-classpath plus the addition of    */
    /*                  argument #1 (including PATH separator)           */
    /*                  exceeds 1023 bytes                               */
    /*         -995 ... could not create JVM                             */
    /*         -994 ... init error: could not find 'Java4Rexx.class'     */
    /*         -993 ... init error: could not find static method         */
    /*                  'Java4Rexx.createInterface4Rexx()'               */
    /*         -992 ... init error: could not find javai/jvm-DLL/so      */
    /*         -991 ... LoadJava()-code not compiled into this version (20090507) */
    /*         -990 ... initilization via 'Java4Rexx.createInterface4Rexx()' not successful (20091003) */
    /*                                                                   */
    /* ---rgf, 2003-01-15,21; switched args on 2003-02-23                */
    /* ---rgf, 2003-08-11; dynamically loads JVM independent of version  */
    /*********************************************************************/
RexxRoutine1(CSTRING, BsfLoadJava, ARGLIST, argArray)    // 20090505, ---rgf, only a stub for backward compatibility
{

#ifdef DEBUG        // ---rgf, 2003-04-30
   fprintf(stderr, "BsfLoadJava(): just arrived\n");    // show values for global variables
#endif

   // char msg[1024]="";     // buffer for error message
   char *msg=new char[1024];

#ifndef ALLOW_LOADING_JVM    //   if not defined!
   /* raise exception */
   SNPRINTF( msg, 1024, "%.16s/routine/BsfLoadJava(), error 1.991: functionality not compiled into this version (symbol 'ALLOW_LOADING_JVM' was not defined at compile time)", DLLNAME);
   context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
   delete[] msg;
   return "-991";

#else   // defined, make code available for compilation
   jint            res;
   jclass          j_clz;
   jmethodID       mid;
   JNIEnv *jniEnv=NULL;

   // JVM already loaded?
    if (currentJVM != NULL)
    {
        SNPRINTF( msg, 1024, "%.16s/routine/BsfLoadJava(), error 1.999: JVM is already loaded", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return "-999";
    }

    // all JNI versions ------------->
    char *cp_env=getenv("CLASSPATH");   // get value of environment variable CLASSPATH (NULL, if not set)

    // JNI 1.2 and above ------------>
    JavaVMInitArgs vm_args2;           // startup argument structure

       // vars for function pointers
    rgf_JNI_CreateJavaVM             rgfCreateJavaVM=NULL;
    rgf_JNI_GetDefaultJavaVMInitArgs rgfGetDefaultJavaVMInitArgs=NULL;

#if defined  (WINDOWS) //   ( WIN32 )
    HINSTANCE hVM=LoadLibrary( "jvm" );   // try to load the JavaVM

    #if defined( DEBUG )
        if (hVM!= NULL)
        {
            // char fileName[256]="";
            char *fileName=new char[256];
            GetModuleFileName(hVM, fileName, 256);
            fprintf(stderr, "module's filename=[%.256s]\n", fileName);
            delete[] fileName;
        }
    #endif

#elif defined ( UNIX )
   PVOID hVM = NULL;

   // 2010-08-14, rgf: new strategy, now that we have a defined place on Unix ("/opt/BSF4ooRexx")
   //                  look first in /opt/BSF4ooRexx, and thereafter let the system find "libjvm.so"
   // 2011-01-06, rgf: on Unix-based systems the BSF4ooRexx installation may have put a link to the
   //                  JVM to be used into BSF4ooRexx home directory, so look there first

    #ifdef __APPLE__
       hVM=dlopen("/opt/BSF4ooRexx/libjvm.dylib", RTLD_LAZY | RTLD_GLOBAL );
       if (hVM==NULL)   // not found, now look in system's location
       {
           hVM=dlopen("/System/Library/Frameworks/JavaVM.framework/JavaVM", RTLD_LAZY | RTLD_GLOBAL );
       }
    #else
       hVM=dlopen("/opt/BSF4ooRexx/libjvm.so", RTLD_LAZY | RTLD_GLOBAL );
    #endif

       if (hVM==NULL)   // not found, now let the system search a "libjvm.so"
       {
#ifdef DEBUG2
    fprintf(stderr, "hvm=[%p], now looking without an explicit path...\n", hVM);
#endif

    #ifdef __APPLE__
           hVM=dlopen("libjvm.dylib", RTLD_LAZY | RTLD_GLOBAL );   // not found, now let the system find a "libjvm.so"...
    #else
           hVM=dlopen("libjvm.so", RTLD_LAZY | RTLD_GLOBAL );   // not found, now let the system find a "libjvm.so"...
    #endif
       }
#endif


#ifdef DEBUG2
   #if   defined (WINDOWS) // ( WIN32 )
       fprintf(stderr, "hVM=[%p],  JNI_CreateJavaVM-proc=[%p]\n", hVM, GetProcAddress(hVM, "JNI_CreateJavaVM"));
   #elif defined ( UNIX )
       fprintf(stderr, "dlerror() ---> [%s]\n", dlerror());

       fprintf(stderr, "hVM=[%p], JNI_CreateJavaVM-proc=[%p]\n", hVM, dlsym(hVM, "JNI_CreateJavaVM"));
       fprintf(stderr, "dlerror() ---> [%s]\n", dlerror());

       if (hVM!=NULL)   // rgf, 2011-03-12
       {
              Dl_info info;
              fprintf(stderr, "1) before dladdr(...)\n");fflush(stderr);
              int rc=dladdr((void *) hVM, (Dl_info *) &info);
              fprintf(stderr, "2) after  dladdr(...), rc=[%d]\n", rc);fflush(stderr);

              if (rc!=0)  // rc of 0 in this function indicates error! :-(
              {
              fprintf(stderr, "dladdr(), rc=[%d], dli_fname=[%s]\n", rc, info.dli_fname);fflush(stderr);

              }
              fprintf(stderr, "3) after: dlerror() ---> [%s]\n", dlerror());fflush(stderr);
       }

       #ifdef __APPLE__
           fprintf(stderr, "__APPLE__\n");
       #else
           fprintf(stderr, "UNIX\n");
       #endif
   #endif
#endif

   if ( hVM == NULL)  // oops, no Java DLL/so found
   {
       // rgf, 2011-02-08
#if defined ( WINDOWS )
       SNPRINTF( msg, 1024, "%.16s/routine/BsfLoadJava(), error 2.992: Java dynamic link/shared library \"jvm.dll\" not found", DLLNAME);
#elif defined ( __APPLE__)
       SNPRINTF( msg, 1024, "%.16s/routine/BsfLoadJava(), error 2.992: Java dynamic link/shared library \"/opt/BSF4ooRexx/libjvm.dylib\" not found (also looking for \"libjvm.dylib\" without success)", DLLNAME);
#else
       SNPRINTF( msg, 1024, "%.16s/routine/BsfLoadJava(), error 2.992: Java dynamic link/shared library \"/opt/BSF4ooRexx/libjvm.so\" not found (also looking for \"libjvm.so\" without success)", DLLNAME);
#endif

       context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
       delete[] msg;
       return "-992";
   }



#if defined  (WINDOWS) //  ( WIN32 )
   rgfCreateJavaVM             = (rgf_JNI_CreateJavaVM)             GetProcAddress(hVM, "JNI_CreateJavaVM"); // return the address
   rgfGetDefaultJavaVMInitArgs = (rgf_JNI_GetDefaultJavaVMInitArgs) GetProcAddress(hVM, "JNI_GetDefaultJavaVMInitArgs"); // return the address

#elif defined ( UNIX )
   rgfCreateJavaVM             = (rgf_JNI_CreateJavaVM)             dlsym(hVM, "JNI_CreateJavaVM") ;
   rgfGetDefaultJavaVMInitArgs = (rgf_JNI_GetDefaultJavaVMInitArgs) dlsym(hVM, "JNI_GetDefaultJavaVMInitArgs");
#endif


#ifdef DEBUG2
   fprintf(stderr, "after finding procAddresses; h1=[%p], h2=[%p], leaving...\n",
           rgfCreateJavaVM, rgfGetDefaultJavaVMInitArgs);

    #ifdef DEBUG_HIT_ENTER_TO_CONTINUE
        RgfDebugHitEnterToContinue();
    #endif
#endif

    if (rgfCreateJavaVM == NULL)    // oops, no entry point found, cannot load Java !
    {
        // sprintf(msg, "%.16s/routine/BsfLoadJava(), error 3.992: Java dynamic link/shared library not found", DLLNAME);
        SNPRINTF( msg, 1024, "%.16s/routine/BsfLoadJava(), error 3.992: Java dynamic link/shared library: entry point \"JNI_CreateJavaVM\" not found", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return "-992";
    }

#ifdef DEBUG2
    fprintf(stderr, "BsfLoadJava(): found DLL/so, now getting the default init args.\n");
    #ifdef DEBUG_HIT_ENTER_TO_CONTINUE
        RgfDebugHitEnterToContinue();
    #endif
#endif

    vm_args2.version = USE_DEFINED_JNI_VERSION ;
    jint resVer = rgfGetDefaultJavaVMInitArgs(&vm_args2);

#ifdef DEBUG1        // ---rgf, 2003-04-30
    fprintf(stderr, "BsfLoadJava(): after rgf_JNI_GetDefaultJavaVMInitArgs, resVer=[%d]\n", (int) resVer);
    #ifdef DEBUG_HIT_ENTER_TO_CONTINUE
        RgfDebugHitEnterToContinue();
    #endif
#endif

/* as of 2006-01-04 rules have changed: only supply options, if arguments are supplied (and if so
                    pass them on verbatimely!
   as of 2006-01-06: if Rexx-arg contains -Djava.class.path=, then use it; if not given and
                     CLASSPATH environment is given, use that instead; else do not set any java.class.path
*/

           // default: no options !
    JavaVMOption *options=NULL;
    vm_args2.version = USE_DEFINED_JNI_VERSION ; // set version, >= JNI_VERSION_1_4
    vm_args2.options = NULL;                // set options-array (containing classpath)
    vm_args2.nOptions = 0;                  // indicate how many entries in array
    vm_args2.ignoreUnrecognized = FALSE;    // report unrecognized values

    int m=0;
    size_t numArgs=context->ArraySize(argArray);    // if arguments supplied, honor them
    // reserve enough space (make sure one entry is available for defining java.lang.classpath)
    options=(JavaVMOption *) malloc( sizeof( JavaVMOption ) * (numArgs+1) );
    bool bCPdefined=FALSE;  // is a classpath defined in the arguments? If so, use it otherwise use CLASSPATH value, if defined
    const char *needle="-Djava.class.path="; // CLASSPATH needle to look for in the Rexx arguments; if
                                             // supplied overrides any CLASSPATH set in the environment
    size_t  needleLength=strlen(needle);

    for (size_t i=1; i<=numArgs; i++)   // iterate over all Rexx arguments
    {
        RexxObjectPtr tmpObj=context->ArrayAt(argArray, i);    // get Object
        if (tmpObj==NULL)              // no entry, ignore
        {
            continue;
        }
        // get String value
        RexxStringObject tmpStrObject=context->ObjectToString(tmpObj);
        if (context->StringLength(tmpStrObject)==0)    // empty string, ignore
        {
            continue;
        }

        // turn supplied argument to CString for comparison with needle value
        const char * cTmpStrObj=context->CString(tmpStrObject);     // turn to ASCII-Z string

        // determine whether java.class.path is supplied via this Rexx argument, setting/overriding CLASSPATH
        bCPdefined= (bCPdefined || (strncmp(cTmpStrObj, needle, needleLength)==0));
#ifdef DEBUG3
    fprintf(stderr, "BsfLoadJava(): Rexx arg # [%lu]=[%.256s]\n", i, cTmpStrObj);
    fprintf(stderr, "               bCPDefined=[%d], strncmp=[%d]\n", bCPdefined, strncmp(cTmpStrObj, needle, needleLength));
#endif
        // create and save supplied option
        size_t tmpLength=strlen(cTmpStrObj);
        char *ch = (char *) malloc(tmpLength+1);   // copy Rexx argument
        memset(ch, 0, tmpLength+1);	    // make sure that initialized to 0x00

        strncpy(ch, cTmpStrObj, tmpLength);
        options[m].optionString = ch;      //
        options[m].extraInfo    = NULL;    // Florian's Schuld! :) (2004-05-06)
#ifdef DEBUG3
    fprintf(stderr, "BsfLoadJava(): added Rexx argument [%.256s] as option # [%d]\n", options[m].optionString, m);
#endif
        m++;                   // increase counter to reflect number of available options
    }

#ifdef DEBUG3
    if (bCPdefined!=FALSE)
    {
         fprintf(stderr, "BsfLoadJava(): CLASSPATH set by a REXX argument [%s].\n",needle);
    }
    else
    {
         fprintf(stderr, "BsfLoadJava(): CLASSPATH was *NOT* set by a REXX argument.\n");
    }
#endif

    if ((bCPdefined==FALSE) &&      // "java.class.path" not given in Rexx arguments
        (cp_env!= NULL))            // CLASSPATH environment variable set
    {
        // create option for setting "java.class.path" to CLASSPATH
        size_t bufLen=strlen(cp_env)+needleLength;
        options[m].optionString = (char *) malloc(bufLen+1);    // get needed memory
        SNPRINTF( options[m].optionString, bufLen+1, "%s%s", needle, cp_env);

        options[m].extraInfo=NULL;
        m++;                        // increase counter to reflect number of available options
#ifdef DEBUG3
    fprintf(stderr, "BsfLoadJava(): using CLASSPATH environment variable for JVM, retrieved and use value:    [%.256s]\n",
                 options[m-1].optionString);
#endif
    }

    vm_args2.options  = options;    // set options-array (containing classpath)
    vm_args2.nOptions = m;          // indicate how many entries in options array

#ifdef DEBUG3
    fprintf(stderr, "BsfLoadJava(): # of options for loading JVM is: [%d]\n\n", (int) vm_args2.nOptions);
#endif


    // according to Apple we must load the JVM in another thread such that the Java awt-thread is not created on the main thread
    // which would interfere with Apple's event dispatch loop; cf. <http://developer.apple.com/library/mac/#technotes/tn2005/tn2147.html>,
    // which links to <http://developer.apple.com/library/mac/#samplecode/simpleJavaLauncher/Introduction/Intro.html>
#ifdef __APPLE__
    #ifdef DEBUG
    	fprintf(stderr, "BSFLoadJava(): __APPLE__, about to invoke 'loadJVMinNewThread(...)\n");
        fprintf(stderr, "               &jniEnv=[%p], &vm_args2=[%p], rgfCreateJavaVM=[%p]\n", &jniEnv, &vm_args2, rgfCreateJavaVM);  fflush(stderr);
    #endif

	// create JVM
    res = loadJVM4AppleMain  ((JNIEnv **) &jniEnv, (JavaVMInitArgs *)  &vm_args2, rgfCreateJavaVM);

    #ifdef DEBUG
        fprintf(stderr, "BSFLoadJava(): __APPLE__, #3a, before FindClass(), jniEnv=[%p]...\n", jniEnv); fflush(stderr);

    #endif

    // --- rgf, 2011-03-16: maybe wrong assumption that interferes with the rgf attach logic
       // on APPLE: adapt, we need to attach to JVM as this is now running in a different thread
    jint resAttach=currentJVM->GetEnv((void **) &jniEnv, USE_DEFINED_JNI_VERSION);  // this will indicate that we are not attached as of yet
    #ifdef DEBUG
    	fprintf(stderr, "BSFLoadJava(): __APPLE__, #1, resAttach=[%d], JNI_EDETACHED=[%d]\n", (int) resAttach, JNI_EDETACHED); fflush(stderr);
    #endif

    if (resAttach==JNI_EDETACHED)   // not attached, attach this thread
    {
         // nope, not yet attached, try to attach
        resAttach=currentJVM->AttachCurrentThread((void **) &jniEnv, (void *) &defaultJavaVMAttachArgs);
    #ifdef DEBUG
    	fprintf(stderr, "BSFLoadJava(): __APPLE__, #2, resAttach=[%d], JNI_OK=[%d]\n", (int) resAttach, JNI_OK); fflush(stderr);
    #endif
        res=resAttach; 	// rgf, use return-code from attachment
    }

    #ifdef DEBUG
        fprintf(stderr, "BSFLoadJava(): __APPLE__, #3b, before FindClass(), jniEnv=[%p]...\n", jniEnv); fflush(stderr);
        jclass clzTest=jniEnv->FindClass("java/lang/System");
        fprintf(stderr, "BSFLoadJava(): __APPLE__, #4b, after  FindClass(), clzTest=[%p]...\n", clzTest); fflush(stderr);
    #endif

#else
    res = rgfCreateJavaVM( &currentJVM, (void **) &jniEnv, &vm_args2);
#endif


#ifdef DEBUG3
{
	fprintf(stderr, "BSFLoadJava() - after JVM created, vm_args2.options=[%p], vm_args2.nOptions=[%d], dumping:\n", vm_args2.options, (int) vm_args2.nOptions);
	for (int m=0;m<vm_args2.nOptions;m++)
	{
    		fprintf(stderr, "\t---> options[%d]=[%.256s] \n", m, vm_args2.options[m].optionString);
	}
    fprintf(stderr, "\t--- --- --- \n" );fflush(stderr);
}
#endif

    // rgf, 2011-01-12: free malloc'ated memory for the JVM options
#ifdef DEBUG3
    fprintf(stderr, "BSFLoadJava() - after JVM created, numArgs>0: vm_args2.options=[%p], vm_args2.nOptions=[%d], about to free memory...\n", vm_args2.options, (int) vm_args2.nOptions);
#endif
    for (int m=0;m<vm_args2.nOptions;m++)
    {
#ifdef DEBUG3
        fprintf(stderr, "\tabout to free ---> options[%d]=[%.256s] \n", m, vm_args2.options[m].optionString);  fflush(stderr);
#endif
        free(vm_args2.options[m].optionString);
    }

#ifdef DEBUG3
    fprintf(stderr, "\tabout to free vm_args2.options=[%p] \n",vm_args2.options ); fflush(stderr);
#endif
    free(vm_args2.options);     // now free option array's memory

    bsfInvokedBy=2;    // invoked by Rexx (which loaded Java)

#ifdef DEBUG2        // ---rgf, 2003-04-30
   fprintf(stderr, "BsfLoadJava(): AFTER create a JVM, res=[%d]\n", (int) res);
#endif

    if (res<0)
    {
        // sprintf(msg, "%.16s/routine/BsfLoadJava(), error 4.995: rc=%d, cannot create Java VM", DLLNAME, res);
        SNPRINTF( msg, 1024, "%.16s/routine/BsfLoadJava(), error 4.995: rc=%d, cannot create Java VM", DLLNAME, (int) res);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return "-995";
    }

    /* Find the class "Java4Rexx" */
#ifdef DEBUG
     fprintf(stderr, "jniEnv=[%p], JAVA_4_REXX=[%.256s] \n", jniEnv, JAVA_4_REXX );
#endif
    j_clz = (*jniEnv).FindClass(JAVA_4_REXX);    // or: cls = jniEnv->FindClass(JAVA_4_REXX);
#ifdef DEBUG
     fprintf(stderr, "jniEnv=[%p], JAVA_4_REXX=[%.256s], j_clz=[%p]\n", jniEnv, JAVA_4_REXX, j_clz);
#endif
    if (j_clz == 0) {
        SNPRINTF( msg, 1024, "%.16s/routine/BsfLoadJava(), error 5.994: initialization error, cannot find class '%.256s'", DLLNAME, JAVA_4_REXX);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return "-994";
    }

    /* get static Method "createInterface4Rexx()" */
    mid = jniEnv->GetStaticMethodID(j_clz, "createInterface4Rexx", "()V");
    if (mid == 0) {
        SNPRINTF( msg, 1024, "%.16s/routine/BsfLoadJava(), error 6.993: initialization error, cannot find method 'Java4Rexx.createInterface4Rexx()", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        jniEnv->DeleteLocalRef(j_clz);
        delete[] msg;
        return "-993";
    }

    // rgf, 20090523: get and save interpreter pointer, such that later invocations will be able to find it
    //                (this function gets only invoked, if Rexx had to load Java, hence only a RexxStart() is
    //                executing
    // rgf, 2012-02-07: add NULL for rajo and rexxconf as this call path does not
    //                  define exit and command handlers for the RII this is safe
//    RgfAddRexxInterpreterInstanceToList(context->threadContext, NULL, NULL);

    // call the static method, which in turn will initialize BSF4Rexx by creating
    // a BSFManager and having it load the "Rexx" (support);
    // it will also invoke jniInitialize4Rexx() which will set the defaultJVM->primodal_rajo
    jniEnv->CallStaticVoidMethod(j_clz, mid, NULL);      // invoke static method

    if ( jniEnv->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
            // get Java Throwable toString() as a RexxString; will clear the pending Java exception
        RexxStringObject rso=RgfGetJavaThrowableAsString (jniEnv, context->threadContext);

        // create error message for Rexx
        // char msg[4096];
        char *msg2=new char[4096];  // use "msg2" to not cover "msg" from outside
        size_t sz=context->StringLength(rso);   // get needed string length, default msg2 buffer is 4 KB
        if (sz>4000)
        {
            char * tmp=(char *) RexxAllocateMemory(sz+256);
            SNPRINTF( tmp, sz+256, "%.16s/routine/BsfLoadJava(), error 7.991: running static method 'Java4Rexx.createInterface4Rexx()' caused Java exception: [%s]", DLLNAME, context->StringData(rso));
            rso=context->String(tmp);           // turn into a RexxString
            RexxFreeMemory(tmp);
        }
        else
        {
            SNPRINTF( msg2, 4096, "%.16s/routine/BsfLoadJava(), error 7.991: running static method 'Java4Rexx.createInterface4Rexx()' caused Java exception: [%s]", DLLNAME, context->StringData(rso));
            rso=context->String(msg2);           // turn into a RexxString
        }

        context->SetContextVariable(BSF_ERROR_STRING, rso);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, rso);

        jniEnv->DeleteLocalRef(j_clz);
        delete[] msg2;

        delete[] msg;   // do not forget to free this buffer as well!
        return "-990";  // return prematurely
    }


    // rgf, 2014-05-17: "createInterface4Rexx() will save the primodal_rajo and run "init_STRUCT_JVM()";
    //                  this type of invocation cannot have a Java RII configuration object, hence NULL
    // rgf, 2015-08-04: the following statement will allow jniGetRexxRootInterpreterInstanceRoot() to work
    RgfAddRexxInterpreterInstanceToList(context->threadContext, defaultJVM->primodal_rajo, NULL);

// TODO: already done? rgf, 20140517
//    init_STRUCT_JVM(jniEnv, defaultJVM, currentJVM);    // 20091003, initialize default structure


    /* get static Method "set_rii_ids()", which uses jniGetRexxRootInterpreterInstanceRoot() to get the rii_IDs to assign */
    mid = jniEnv->GetStaticMethodID(j_clz, "set_rii_ids", "()V");
    if (mid == 0) {
        SNPRINTF( msg, 1024, "%.16s/routine/BsfLoadJava(), error 6.994: initialization error, cannot find method 'Java4Rexx.set_rii_ids()", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        jniEnv->DeleteLocalRef(j_clz);
        delete[] msg;
        return "-994";
    }

    // rgf, 20090523: get and save interpreter pointer, such that later invocations will be able to find it
    //                (this function gets only invoked, if Rexx had to load Java, hence only a RexxStart() is
    //                executing
    // rgf, 2012-02-07: add NULL for rajo and rexxconf as this call path does not
    //                  define exit and command handlers for the RII this is safe
//    RgfAddRexxInterpreterInstanceToList(context->threadContext, NULL, NULL);

    // call the static method, which in turn will initialize BSF4Rexx by creating
    // a BSFManager and having it load the "Rexx" (support);
    // it will also invoke jniInitialize4Rexx() which will set the defaultJVM->primodal_rajo
    jniEnv->CallStaticVoidMethod(j_clz, mid, NULL);      // invoke static method

    if ( jniEnv->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
            // get Java Throwable toString() as a RexxString; will clear the pending Java exception
        RexxStringObject rso=RgfGetJavaThrowableAsString (jniEnv, context->threadContext);

        // create error message for Rexx
        // char msg[4096];
        char *msg2=new char[4096];  // use "msg2" to not cover "msg" from outside
        size_t sz=context->StringLength(rso);   // get needed string length, default msg2 buffer is 4 KB
        if (sz>4000)
        {
            char * tmp=(char *) RexxAllocateMemory(sz+256);
            SNPRINTF( tmp, sz+256, "%.16s/routine/BsfLoadJava(), error 7.992: running static method 'Java4Rexx.set_rii_ids()' caused Java exception: [%s]", DLLNAME, context->StringData(rso));
            rso=context->String(tmp);           // turn into a RexxString
            RexxFreeMemory(tmp);
        }
        else
        {
            SNPRINTF( msg2, 4096, "%.16s/routine/BsfLoadJava(), error 7.992: running static method 'Java4Rexx.set_rii_ids()' caused Java exception: [%s]", DLLNAME, context->StringData(rso));
            rso=context->String(msg2);           // turn into a RexxString
        }

        context->SetContextVariable(BSF_ERROR_STRING, rso);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, rso);

        jniEnv->DeleteLocalRef(j_clz);
        delete[] msg2;

        delete[] msg;   // do not forget to free this buffer as well!
        return "-990";  // return prematurely
    }




#ifdef DEBUG        // ---rgf, 2003-04-30
    fprintf(stderr, "BsfLoadJava(): about to leave...\n\n");
#endif

    // rgf, 20140330: add TID to .local to save the primodal TID for this Rexx interpreter instance
    {
        RexxDirectoryObject localDir = context->GetLocalEnvironment();   // get .local

        #ifdef UNIX
            pthread_t
        #else   // WINDOWS
            TID
        #endif
                tid=RgfGetTID();

        // char strTid[32]="";         // define a string long enough to receive an integer
        char *strTid=new char[RGF_TID_STRING_WIDTH];
        // sprintf(strTid, "%lu%c", ((unsigned long) tid), 0);  // create decimal number
        // SNPRINTF( strTid, 32, "%lu%c", ((unsigned long) tid), 0);  // create decimal number
        SNPRINTF( strTid, RGF_TID_STRING_WIDTH, "%lu%c", ((unsigned long) tid), 0);  // create decimal number

        #ifdef RGF_INFO
            fprintf(stderr, "*** RGF_INFO: BsfLoadJava(), tid=[%lu], %%s=[%s] ...\n", (unsigned long) tid, strTid);
            fflush(stderr);
        #endif

        context->DirectoryPut(localDir, context->String(strTid), LOCAL_PRIMODAL_TID);
        delete[] strTid;
    }

    // ------------------------------------
    jniEnv->DeleteLocalRef(j_clz);

    delete[] msg;
    return "0";
#endif
}





    /*********************************************************************/
    /* unload Java, according to tutorial: does not really work on       */
    /*              Java 1.1, will always return an error                */
    /*                                                                   */
    /* returns:                                                          */
    /*         0    ... indicate everything o.k.                         */
    /*         -999 ... JVM was not loaded by Rexx, aborting             */
    /*         -998 ... JVM is not loaded at all, hence cannot unload    */
    /*         deprecated (20091004): -997 ... this thread cannot attach to a JVM               */
    /*         -991 ... UnloadJava()-code not compiled into this version (20090507) */
    /*                                                                   */
    /* ---rgf, 2003-01-15,21, 26                                         */
    /*********************************************************************/
RexxRoutine0(RexxStringObject, BsfUnloadJava)  // 20090505, ---rgf, only a stub for backward compatibility
{

#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfUnloadJava() 1 ...\n");
    fflush(stderr);
#endif
    // char msg[1024]="";
    char *msg=new char[1024];

#ifndef ALLOW_LOADING_JVM    //   if not defined!

    /* raise exception */
    // sprintf(msg, "%.16s/routine/BsfUnloadJava(), error 1.991: functionality not compiled into this version (symbol 'ALLOW_LOADING_JVM' was not defined at compile time)", DLLNAME);
    SNPRINTF( msg, 1024, "%.16s/routine/BsfUnloadJava(), error 1.991: functionality not compiled into this version (symbol 'ALLOW_LOADING_JVM' was not defined at compile time)", DLLNAME);
    context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
    delete[] msg;
    return context->String("-991");

#else


    if (bsfInvokedBy==0)    // JVM is not loaded, error!
    {
        // sprintf(msg, "%.16s/routine/BsfUnloadJava(), error 2.998: no JVM loaded at all", DLLNAME);
        SNPRINTF( msg, 1024, "%.16s/routine/BsfUnloadJava(), error 2.998: no JVM loaded at all", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return context->String("-998");
    }


        // bsfInvokedBy: 0=noJVM, 1=byJava, 2=byRexx
        // gracefully ignore BsfUnload() ?
    if (bsfInvokedBy == 1) // invoked by Java, do not unload Java!
    {
        SNPRINTF( msg, 1024, "%.16s/routine/BsfUnloadJava(), error 3.999: JVM was not loaded by Rexx", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return context->String("-999");
    }


#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *)context->threadContext->instance, false, 0};
#else
    // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error};
    STRUCT_PARAM param={0, 0, 0, (void *)context->threadContext->instance, 0, 0, 0, 0};
#endif

    RgfAcquireLock();

#ifdef RGF_ATTACH_NEW
    environmentAttachToNew(&param);     // attach thread to Java
#else
    environmentAttachTo(&param, FALSE);    // attach to Java
#endif

    if (param.error== -1)   // attaching to Java was not successful, we are in deep troubles !
    {
        SNPRINTF( msg, 1024, "%.16s/routine/BsfUnloadJava(), error 4.997: cannot attach thread to Java (needed for unloading Java)", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        RgfReleaseLock();
        delete[] msg;
        return context->String("-997");
    }

// TODO: RAJO_TID2NODE structures needed anymore ?

#ifndef RGF_ATTACH_NEW
       // emtpy RAJO/TID2NODE structures
    empty_RAJO_TID2NODE_structures(param.env);
#endif

       // empty JavaVM structure
    JavaVM *tmpJVM=defaultJVM->jvm;     // save pointer to JVM
    uninit_STRUCT_JVM(param.env, defaultJVM); // will DeleteGlobalRef(...), nullify structure
    // defaultJVM=NULL;  // rgf, 2011-03-19

#if defined ( RGF_INFO_1 )
    fprintf(stderr, "*** *** RGF_INFO_1: RgfUnloadJava() 2, now calling DestroyJava() ...\n");
    fflush(stderr);
#endif


    // rgf, 20140330: remove primodal TID entry in .local for this Rexx interpreter instance
    {
        RexxDirectoryObject localDir = context->GetLocalEnvironment();   // get .local

#ifdef RGF_INFO

    #ifdef UNIX
        pthread_t
    #else   // WINDOWS
        TID
    #endif
            tid=RgfGetTID();

    char *strTid=new char[RGF_TID_STRING_WIDTH];
    SNPRINTF( strTid, RGF_TID_STRING_WIDTH, "%lu%c", ((unsigned long) tid), 0);  // create decimal number

    fprintf(stderr, "*** RGF_INFO: BsfUnloadJava(), tid=[%lu], %%s=[%s] ...\n", (unsigned long) tid, strTid);
    fflush(stderr);

    delete[] strTid;
#endif

        context->DirectoryRemove(localDir, LOCAL_PRIMODAL_TID); // remove entry in .local, not useful anymore
    }


    long ret=tmpJVM->DestroyJavaVM ( ); // destroy JVM

#if defined ( RGF_INFO_1 )
    fprintf(stderr, "*** *** RGF_INFO_1: RgfUnloadJava() 3, AFTER calling DestroyJava(), ret=[%d] ...\n", (int) ret);
    fflush(stderr);
#endif

    currentJVM  =NULL;                      // set pointer to NULL
    memset(defaultJVM, 0, size_STRUCT_JVM); // clear the structure, rgf, 20091004
    bsfInvokedBy=0;                         // reset indicator
    RgfReleaseLock();

    // char retStr [32] = "";
    char *retStr=new char[32];
    SNPRINTF( retStr, 32, "%ld%c", ret, 0);    // create return value


 #ifdef DEBUG        // ---rgf, 2003-04-30
     fprintf(stderr, "BsfUnloadJava(): about to leave...\n\n");
 #endif

    delete[] msg;
    RexxStringObject rso=context->String(retStr);
    delete [] retStr;
    return rso;
#endif
}





    /*********************************************************************/
    /* load (register) all BSF-functions                                 */
    /* returns:                                                          */
    /*            0 ... registering went o.k.                            */
    /*********************************************************************/
RexxRoutine0(CSTRING, BsfLoadFuncs)  // 20090505, ---rgf, only a stub for backward compatibility
{
#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfLoadFuncs() 1 ...\n");
    fflush(stderr);
#endif

  return "0";
}


    /*********************************************************************/
    /* unregisters all BSF-functions                                     */
    /*                                                                   */
    /* returns:                                                          */
    /*                                                                   */
    /*            0 ... deregistering went o.k.                          */
    /*         -998 ... at least one deregistering function caused a     */
    /*                  non-zero return code, hence abort                */
    /*                                                                   */
    /*********************************************************************/

RexxRoutine0(CSTRING, BsfDropFuncs)  // 20090505, ---rgf, only a stub for backward compatibility
{
#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfDropFuncs() 1 ...\n");
    fflush(stderr);
#endif

  return "0";
}





    /*********************************************************************/
    /* returns the version number                                        */
    /*                                                                   */
    /*********************************************************************/
RexxRoutine0(CSTRING, BsfVersion)    // 20090505, ---rgf, only a stub for backward compatibility
{

#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfVersion() 1, version=[%.256s] ...\n", BSF_VERSION);
    fflush(stderr);
#endif

  return BSF_VERSION;
}


// ===================> NEW VERSION

    /*********************************************************************/
    /* fills in a stem with the function names                           */
    /*********************************************************************/
RexxRoutine1(RexxStemObject, BsfQueryAllFunctions, OPTIONAL_RexxStemObject, stem)  // 20090505, ---rgf
{
#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfQueryAllFunctions() 1 ...\n");
    fflush(stderr);
#endif

    if (stem==NULL)     // argument not given
    {
        stem=context->NewStem("BsfQueryAllFunctions");
    }

    PSZ *tmpTable=ApiFncTable;
    int32_t entries=sizeof(ApiFncTable)/sizeof(PSZ);
    int32_t i=0, j=0;

    for (i=0; i<entries; i++) {
        context->SetStemArrayElement(stem, ++j, context->String(tmpTable[i]));
    }
    context->SetStemArrayElement(stem, 0, context->Int32ToObject(j));

    return stem;
}




    /*********************************************************************/
    /* fills in a stem with the names of the registered functions        */
    /*********************************************************************/
RexxRoutine1(RexxStemObject, BsfQueryRegisteredFunctions, OPTIONAL_RexxStemObject, stem)  // 20090505, ---rgf
{
#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfQueryRegisteredFunctions() 1 ...\n");
    fflush(stderr);
#endif

    if (stem==NULL)     // argument not given
    {
        stem=context->NewStem("BsfQueryRegisteredFunctions");
    }

    PSZ   *tmpTable=NULL;
    int32_t entries=0;

#ifndef ALLOW_LOADING_JVM       // no Java un/loading available
    tmpTable=ApiFncTable4Java;
    entries=sizeof(ApiFncTable4Java)/sizeof(PSZ);
#else
    if (bsfInvokedBy==1)
    {
        tmpTable=ApiFncTable4Java;
        entries=sizeof(ApiFncTable4Java)/sizeof(PSZ);
    }
    else
    {
        tmpTable=ApiFncTable;
        entries=sizeof(ApiFncTable)/sizeof(PSZ);
    }
#endif

    int32_t i=0, j=0;

    for (i=0; i<entries; i++) {
        context->SetStemArrayElement(stem, ++j, context->String(tmpTable[i]));
    }
    context->SetStemArrayElement(stem, 0, context->Int32ToObject(j));

    return stem;
}




    // ---rgf, 2006-11-19: allow to query and set behaviour in case of a Java exception
    //                     "1" (default)...show error string,
    //                     "0": do not show error string, instead place it as "BSF_ERROR_STRING" into the variable pool
    //                     if nor argument, only setting is returned
RexxRoutine1(CSTRING, BsfShowErrorMessage, OPTIONAL_CSTRING, strNewVal)    // 20090505, ---rgf, only a stub for backward compatibility
{

#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfShowErrorMessage() 1 ...\n");
    fflush(stderr);
#endif

    if (strNewVal != NULL)      // if NULL, just return current setting
    {
        if (strcmp(strNewVal,"0")==0)       //
        {
            bShowErrorString=0;
        }
        else if (strcmp(strNewVal,"1")==0)
        {
            bShowErrorString=1;
        }
        else
        {
            // context->InvalidRoutine();
            // char msg[1024]="";
            char *msg=new char[1024];

            // sprintf(msg, "%.16s/routine/BsfShowErrorMessage(), error 1: illegal argument value '%.512s' (valid: '0' or '1').", DLLNAME, strNewVal);
            SNPRINTF( msg, 1024, "%.16s/routine/BsfShowErrorMessage(), error 1: illegal argument value '%.512s' (valid: '0' or '1').", DLLNAME, strNewVal);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
            return "0";
        }
    }
    return bShowErrorString==0 ? "0" : "1";
}



    // ---rgf, 2003-08-06: allow Rexx to retrieve its TID (i.e. Java's "JNIEnv" object)
RexxRoutine0(RexxStringObject, BsfGetTID)    // 20090505, ---rgf, only a stub for backward compatibility
{
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();

   // char strTid[32]="";         // define a string long enough to receive an integer
   // sprintf(strTid, "%lu%c", ((unsigned long) tid), 0);  // create decimal number
   // SNPRINTF( strTid, 32, "%lu%c", ((unsigned long) tid), 0);  // create decimal number
   char *strTid=new char[RGF_TID_STRING_WIDTH];
   SNPRINTF( strTid, RGF_TID_STRING_WIDTH, "%lu%c", ((unsigned long) tid), 0);  // create decimal number


#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfGetTID() 1, tid=[%lu], %%s=[%s] ...\n", (unsigned long) tid, strTid);
    fflush(stderr);
#endif

    RexxStringObject rso=context->String(strTid);
    delete[] strTid;

   return rso;
}



    // ---rgf, 2014-05-17: not needed anymore (deprecated), return true to allow older programs to continue to run
RexxRoutine1(logical_t, BsfAttachToTID, OPTIONAL_CSTRING, toTID)    // 20090505, ---rgf, only a stub for backward compatibility
{
    return true;
}


    // ---rgf, 2014-05-17: not needed anymore (deprecated), return true to allow older programs to continue to run
RexxRoutine0(logical_t, BsfDetach)    // 20090505, ---rgf, only a stub for backward compatibility
{
    return true;
}


#ifndef RGF_ATTACH_NEW
        // ---rgf, 2003-08-06: allow Rexx to use the Java environment of another Rexx thread
    RexxRoutine1(logical_t, BsfAttachToTID_Old, OPTIONAL_CSTRING, toTID)    // 20090505, ---rgf, only a stub for backward compatibility
    {

        #ifdef UNIX
            pthread_t
        #else   // WIN32
            TID
        #endif
                tid=RgfGetTID();

    #ifdef RGF_INFO
        fprintf(stderr, "*** RGF_INFO: BsfAttachToTID() 1, tid=[%lu], toTID=[%.256s] ...\n",
                (unsigned long) tid, toTID
                );
        fflush(stderr);
    #endif

            // extract "tid" value from Rexx string; TODO: check for MacOSX
        unsigned long  attachToTID=0;
        bool bUsingPrimodialTID=false;

        if (toTID==NULL)    // rgf, 20140330: no TID given, try to use BSF.PRIMTODALTID
        {
            RexxDirectoryObject localDir = context->GetLocalEnvironment();   // get .local
            RexxObjectPtr obj = context->DirectoryAt(localDir, LOCAL_PRIMODAL_TID);

            if (obj==NULL) // NULLOBJECT)
            {
                char msg[256];
                SNPRINTF(msg, 256, "%.16s/routine/BsfAttachToTID(), error 3: no TID given, nor %s defined in .local", DLLNAME, LOCAL_PRIMODAL_TID);
                context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                return FALSE;
            }

            toTID=(char *) context->CString(context->ObjectToString(obj));
            bUsingPrimodialTID=true;
        }

        sscanf(toTID, "%lu", &attachToTID);    // get integer value from string

        int bAlreadyAttached=FALSE;
        int bTargetTIDMissing=TRUE;
        RgfAcquireLock();
            // find tid in tid2jrst
        for (PSTRUCT_TID2NODE t2ListNode=pRoot_TID2NODE; t2ListNode!=NULL; t2ListNode=t2ListNode->next)
        {
    // fprintf(stderr, "/// ba2i marker (1), t2ListNode=[%p], t2ListNode->tid=[%p], attachToTID=[%p]...\n", t2ListNode, t2ListNode->tid, attachToTID);fflush(stderr);
            if (t2ListNode->tid == tid)    // tid exists already !
            {
                bAlreadyAttached=TRUE;
    // fprintf(stderr, "/// ba2i marker (2a), t2ListNode=[%p]...\n", t2ListNode);fflush(stderr);
                break;
            }

            if ((unsigned long) t2ListNode->tid == attachToTID)   // target TID exists!
            {
    // fprintf(stderr, "/// ba2i marker (2b), t2ListNode=[%p]...\n", t2ListNode);fflush(stderr);
                bTargetTIDMissing=FALSE;
            }
        }
        RgfReleaseLock();

        if (bAlreadyAttached==TRUE)     // raise an error in caller
        {
            char msg[256];
            SNPRINTF(msg, 256, "%.16s/routine/BsfAttachToTID(), error 1: current thread [%lu] is already attached", DLLNAME, (unsigned long)tid);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            return FALSE;
        }

        if (bTargetTIDMissing==TRUE)
        {
            char msg[256];
            if (bUsingPrimodialTID==true)
            {
                SNPRINTF(msg, 256, "%.16s/routine/BsfAttachToTID(), error 2: target (.%s) thread [%lu] does not exist or has currently no established interface to Java", DLLNAME, LOCAL_PRIMODAL_TID, (unsigned long) attachToTID);
            }
            else
            {
                SNPRINTF(msg, 256, "%.16s/routine/BsfAttachToTID(), error 2: target thread [%lu] does not exist or has currently no established interface to Java", DLLNAME, (unsigned long) attachToTID);
            }
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            return FALSE;
        }


       // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error,attach2tid};
       STRUCT_PARAM param={tid, 0, 0, (void *)context->threadContext->instance, 0, 0, 0,
    (
    #ifdef UNIX
        pthread_t
    #else
        TID
    #endif
    )
                           attachToTID};
       RgfAcquireLock();

    #if defined ( DEBUG_SHOW_STRUCTURES )
        dumpPARAM(   "BsfAttachToTID(), before environmentAttachTo(...): param:", &param);
        dumpRAJO(    "BsfAttachToTID(), before environmentAttachTo(...), 0: ");
        dumpTID2NODE("BsfAttachToTID(), before environmentAttachTo(...), 0: ");
    #endif

       environmentAttachTo(&param, TRUE);       // attach to Java, force staying around after next environmentDetachFrom() !

    #if defined ( DEBUG_SHOW_STRUCTURES )
        dumpPARAM(   "BsfAttachToTID(), AFTER environmentAttachTo(...): param:", &param);
        dumpRAJO(    "BsfAttachToTID(), AFTER environmentAttachTo(...):, 1: ");
        dumpTID2NODE("BsfAttachToTID(), AFTER environmentAttachTo(...):, 1: ");
    #endif

       // - param->error:  -1 ... Attach-error, attachResult holds Java error code, raise an exception in caller
       //                  -2 ... no rajo found, raise an exception in caller
       //                  -3 ... cannot safely use the given RAJO in TID, as actual RAJO is being used in multiple Rexx threads
       //                  -4 ... cannot attach to target TID
       //                 -99 ... no JVM available !
       if (param.error== -1)    // could not attach to Java
       {
           context->RaiseException1(Rexx_Error_Incorrect_call_user_defined,
                                    createAttachErrorMessageRSO(context, "routine/BsfAttachToTID(), error 3", &param)
                                    );
           RgfReleaseLock();
           return FALSE;
       }

       // 20091006: attaching is now done in environmentAttach(...), if param has a value defined for attach2TID
       // attachToTID_2(&param, attachToTID);       // carry out the attachment

       if (param.error==-4)         // target TID not found, raise an exception!
       {
           context->RaiseException1(Rexx_Error_Incorrect_call_user_defined,
                                    createAttachErrorMessageRSO(context, "routine/BsfAttachToTID(), error 2", &param)
                                    );
           environmentDetachFrom(&param, TRUE);     // close group
           RgfReleaseLock();
           return FALSE;
       }

       param.bDetach=FALSE;     // make sure we remain attached to this thread
       environmentDetachFrom(&param, FALSE);    // do not close group

    #if defined ( DEBUG_SHOW_STRUCTURES )
        dumpPARAM(   "BsfAttachToTID(), after environmentDetachFrom(): param:", &param);
        dumpRAJO(    "BsfAttachToTID(), after environmentDetachFrom(): ");
        dumpTID2NODE("BsfAttachToTID(), after environmentDetachFrom(): ");
    #endif
        RgfReleaseLock();

       // return (param.error==NULL);      // return .true, if no error, .false else
       return (param.error==0);         // return .true, if no error, .false else
    }


        // rgf, 2008-07-26
    /* Removes the TID2JRST entry with current TID. If removal was possible, return .true,
       else .false (indicating that no Java environment was attached to this TID).

        arg1=RexxObject or CodeString
        arg2=optional, userData (any Rexx object)
        arg3=optional, Java class (string, clz, jobj), ...
        argn=          - " -
    */

    // APIRET APIENTRY BsfDetach      ( PSZ name, LONG numargs, RXSTRING args[], PSZ queuename, PRXSTRING retstr )
    RexxRoutine0(logical_t, BsfDetach_Old)    // 20090505, ---rgf, only a stub for backward compatibility
    {
        #ifdef UNIX
            pthread_t
        #else
            TID
        #endif
                tid=RgfGetTID();    // current thread

    #if defined ( RGF_INFO )
        fprintf(stderr, "*** RGF_INFO: BsfDetach() 1, tid=[%lu] ...\n",
                (unsigned long) tid
                );
        fflush(stderr);
    #endif

        // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error};
        STRUCT_PARAM param={tid, 0, 0, (void *)context->threadContext->instance, 0, 0, 0, 0};

        RgfAcquireLock();

    // fprintf(stderr, "BsfDetach() 1a, tid=[%lu],       environmentAttachTo()\n", (unsigned int) tid); fflush(stderr);

        environmentAttachTo(&param, FALSE); // attach to Java

    // fprintf(stderr, "BsfDetach() 1b, tid=[%lu], after environmentAttachTo()\n", (unsigned int) tid); fflush(stderr);

    #if defined ( DEBUG_SHOW_STRUCTURES )
        dumpPARAM(   "BsfDetach(), after environmentAttachTo(...): param:", &param);
        dumpRAJO(    "BsfDetach(), after environmentAttachTo(...), 1: ");
        dumpTID2NODE("BsfDetach(), after environmentAttachTo(...), 1: ");
    #endif

        if (param.error== -1)    // could not attach to Java
        {
            RgfReleaseLock();
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined,
                                     createAttachErrorMessageRSO(context, "routine/BsfDetach(), error 1", &param)
                                     );
            return FALSE;
        }
        // detachFromTID_2(&param);       // carry out the detachment


        // fetch .bsf.primodalTID, if current make sure that it does not get removed
        int  bIsPrimodalThread=0;
        {
            RexxDirectoryObject localDir = context->GetLocalEnvironment();   // get .local
            RexxObjectPtr obj = context->DirectoryAt(localDir, LOCAL_PRIMODAL_TID);

            if (obj==NULL) // NULLOBJECT)
            {
                char msg[256];
                SNPRINTF(msg, 256, "%.16s/routine/BsfDetach(), error 2: no %s defined in .local, cannot execute function", DLLNAME, LOCAL_PRIMODAL_TID);
                context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                return FALSE;
            }

            char *toTid=(char *) context->CString(context->ObjectToString(obj));

            unsigned long  ulPrimodalTID=0;
            sscanf(toTid, "%lu", &ulPrimodalTID);    // get integer value from string
            unsigned long ulTID= (unsigned long) tid;
            // sscanf(tid, "%lu", &ulTID);    // get integer value from string

            bIsPrimodalThread= (ulTID==ulPrimodalTID ? 1 : 0);

    // fprintf(stderr, "toTid=[%lu], ulPrimodalTID=[%lu]\n", ulTID, ulPrimodalTID); fflush(stderr);
        }

        // determine whether we run in the primodal thread, if so make sure that we never detach by user demand

        int res=subtractRajoFromTID2NODE(param.env, param.tid, FALSE, bIsPrimodalThread);    // carry out the detachment

    // fprintf(stderr, "BsfDetach() 1a, tid=[%lu],       environmentDetachFrom()\n", (unsigned int) tid); fflush(stderr);

        param.bDetach=TRUE;      // make sure we detached from this thread
        environmentDetachFrom(&param, FALSE);    // do not close group

    // fprintf(stderr, "BsfDetach() 1b, tid=[%lu],       environmentDetachFrom()\n", (unsigned int) tid); fflush(stderr);

    #if defined ( DEBUG_SHOW_STRUCTURES )
        dumpPARAM(   "BsfDetach(), after environmentDetach(): param:", &param);
        dumpRAJO(    "BsfDetach(), after environmentDetach(): ");
        dumpTID2NODE("BsfDetach(), after environmentDetach(): ");
    #endif
        RgfReleaseLock();

        if (bIsPrimodalThread==1 && res==FALSE)
        {
            return FALSE;
        }

        // return (param.error==NULL);      // no error, hence worked
        return (param.error==0);         // no error, hence worked
    }

#endif









    // ---rgf, 2009-09-25: allow Rexx to retrieve the Rexx instance ID (the instance pointer as string)
RexxRoutine0(RexxStringObject, BsfGetRIID)    // 20090505, ---rgf, only a stub for backward compatibility
{
    RexxInstance *ri=context->threadContext->instance;  // get the RexxThreadContext pointer
#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfGetRIID(): %%lu=[%lu], %%p=[%p] \n", (long unsigned int) ri, ri);
    fflush(stderr);
#endif

   // char strRIID[32]="";         // define a string long enough to receive an integer
   char *strRIID=new char[RGF_POINTER_STRING_WIDTH];
   // sprintf(strRIID, "%p%c", ((unsigned long) ri), 0);  // create decimal number
   SNPRINTF( strRIID, RGF_POINTER_STRING_WIDTH, "%p%c", ri, 0);  // create decimal number

   RexxStringObject rso=context->String(strRIID);
   delete  [] strRIID;
   return rso;
}




//    // rgf, 2009-05-09
// / * creates a RexxProxy on the Java side, caches Rexx object and optional user data object
//
//            // used to create a RexxProxy
//        arg1=RexxObject or CodeString
//        arg2=optional, userData (any Rexx object)
//
//            // if given, then create and return a JavaProxy object
//        arg3=optional, Java class (string, clz, jobj), ...
//        argn=          - " -
//
//   returns BSF Registry index name, either for RexxProxy or JavaProxy object
// * /
RexxRoutine1(RexxObjectPtr, BsfCreateRexxProxy, ARGLIST, argArray)    // 20090505, ---rgf, only a stub for backward compatibility
{
    #ifdef UNIX
        pthread_t
    #else
        TID
    #endif
            tid=RgfGetTID();    // current thread

#if defined (RGF_INFO) || defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "*** RGF_INFO: BsfCreateRexxProxy() 1, BEGIN: tid=[%lu] ...\n",
            (unsigned long) tid
            );
    fflush(stderr);
#endif

    // const char c_rii_ID[ RGF_POINTER_STRING_WIDTH+1 ]="";
    char *c_rii_ID= new char[RGF_POINTER_STRING_WIDTH];

    // char msg[4096]="";
    char *msg=new char[4096];

    if (context->ArrayAt(argArray, 1)==NULL)        // first argument omitted ?
    {
        // sprintf(msg, "%.16s/routine/BsfCreateRexxProxy(), error 1: first argument (a Rexx object or a string) is missing", DLLNAME);
        SNPRINTF( msg, 4096, "%.16s/routine/BsfCreateRexxProxy(), error 1: first argument (a Rexx object or a string) is missing", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete [] c_rii_ID;
        delete [] msg;
        return context->Nil();
    }

       // get the package object from caller
    RexxPackageObject rpo=(RexxPackageObject) context->SendMessage0(context->GetCallerContext(),"PACKAGE");

    RexxObjectPtr r_obj     =context->ArrayAt(argArray,1);
       // if object is string, it's code; attach it as UNKNOWN !
    if (context->IsOfType(r_obj, "STRING") || context->IsOfType(r_obj, "METHOD") )  // a string object in hand? If so, it's the code to be run!
    {

// fprintf(stderr, "BSF_OnTheFly.cls !!! ...\n"); fflush(stderr);

        r_obj=context->CallProgram(REXX_PROXY_STUB_PROGRAM, context->ArrayOfOne(r_obj));
        if (context->CheckCondition()) // oops, something went wrong, let the condition prevail
        {
            // char tmpStr[1024]="";
            char *tmpStr=new char[1024];
            SNPRINTF(tmpStr, 1024, "/routine/BsfCreateRexxProxy(), error 2: [Rexx raised condition while calling \"%s\" to attach code to dynamically]", REXX_PROXY_STUB_PROGRAM);

                // get msg with embedded condition info
            char * tmpMsg=RgfCreateRexxlikeErrorInfo (context->threadContext,
                                                   context->GetConditionInfo(),
                                                   tmpStr);
                                                   // "/routine/BsfCreateRexxProxy() error 2: [Rexx raised condition while calling \"BSF_OnTheFly.cls\" to attach code to]");
            context->ClearCondition();

                // now raise new exception
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(tmpMsg));
            RexxFreeMemory(tmpMsg);        // free memory
            delete [] tmpStr;

            delete [] c_rii_ID;
            delete [] msg;
            return context->Nil();      // indicate with return value, that something went wrong
        }
    }


    RexxObjectPtr r_userData=(context->ArrayAt(argArray, 2)!=NULL ? context->ArrayAt(argArray,2) : NULL);


#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *)context->threadContext->instance, false, 0};
#else
    // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error};
    STRUCT_PARAM param={tid, 0, 0, (void *)context->threadContext->instance, 0, 0, 0, 0};
#endif

    RgfAcquireLock();

#ifdef RGF_ATTACH_NEW
    environmentAttachToNew(&param); // attach to Java

    if (param.error!= 0)    // problem attaching, cannot proceed
    {
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined,
                                 createAttachErrorMessageRSONew(context, "routine/BsfCreateRexxProxy(), error 3", &param)
                                 );
        RgfReleaseLock();
        delete [] c_rii_ID;
        delete [] msg;
        return FALSE;
    }
    RgfReleaseLock();

#else
    environmentAttachTo(&param, FALSE); // attach to Java

        // - param->error:  -1 ... Attach-error, attachResult holds Java error code, raise an exception in caller
        //                  -2 ... no rajo found, raise an exception in caller
        //                  -3 ... cannot safely use the given RAJO in TID, as actual RAJO is being used in multiple Rexx threads
        //                  -4 ... cannot attach to target TID
        //                 -99 ... no JVM available !

    #ifdef DEBUG_JAVA_ATTACH_DETACH
        fprintf(stderr, "---> BSFCreateRexxProxy() // 1--> param.error=[%d]\n", param.error); fflush(stderr);
    #endif
        if (param.error==-2)    // ---rgf, 2014-03-30: thread not attached, use primodal TID
        {
            RgfReleaseLock();

            bool bUsingPrimodialTID=false;

            RexxDirectoryObject localDir = context->GetLocalEnvironment();   // get .local
            RexxObjectPtr obj = context->DirectoryAt(localDir, LOCAL_PRIMODAL_TID);

            if (obj==NULL) // NULLOBJECT)
            {
                // char msg[256];
                char *msg2=new char[256];
                SNPRINTF(msg2, 256, "%.16s/routine/BsfAttachToTID(), error 3: no TID given, nor %s defined in .local", DLLNAME, LOCAL_PRIMODAL_TID);
                context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg2));
                delete [] msg2;

                delete [] c_rii_ID;
                delete [] msg;
                return FALSE;
            }

            char *toTID=(char *) context->CString(context->ObjectToString(obj));
            bUsingPrimodialTID=true;

            unsigned long attachToTID=0;

            sscanf(toTID, "%lu", &attachToTID);    // get integer value from string

            // now try to attach
                      // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error,attach2tid};
            STRUCT_PARAM param2={tid, 0, 0, (void *)context->threadContext->instance, 0, 0, 0,
                (
                #ifdef UNIX
                    pthread_t
                #else
                    TID
                #endif
                )
                attachToTID };

            RgfAcquireLock();
            environmentAttachTo(&param2, TRUE); // attach to Java, keep attached

            param=param2;   // copy param2 fields to param to let the checking go on

    #ifdef DEBUG_JAVA_ATTACH_DETACH
        fprintf(stderr, "---> BSFCreateRexxProxy() // 2 --> used primodal TID! param.error=[%d], param2.error=[%d]\n", param.error, param2.error); fflush(stderr);
    #endif
        }


        if (param.error!= 0)    // problem attaching, cannot proceed
        {
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined,
                                     createAttachErrorMessageRSO(context, "routine/BsfCreateRexxProxy(), error 3", &param)
                                     );
            if (param.error != -1)  // if attaching worked, we need to make sure that we detach as ell
            {
                environmentDetachFrom(&param, FALSE);   // detach from Java
            }
            RgfReleaseLock();
            delete [] c_rii_ID;
            delete [] msg;
            return FALSE;
        }
        RgfReleaseLock();


    // <--- old-attach
#endif


        // create a Java RexxProxy
    jobject j_rexxProxy=RgfCreateRexxProxy (param.env, param.rajo,
                                            context->threadContext, // ThreadContext, RMG: 20090514
                                            r_obj,              // Rexx object
                                            rpo,                // Package object
                                            r_userData          // userData (slot)
                                            );

    // process received arguments
    // size_t numArgs=context->ArrayItems(argArray);   // get number of arguments
    size_t numArgs=context->ArraySize(argArray);   // get number of arguments

    if (numArgs>2)      // o.k. creation of a JavaProxy is requested
    {
        numArgs=numArgs-2;
        //convert arguments array to Java format
        jobjectArray jarr = param.env->NewObjectArray (
                                  (jsize) numArgs,  // needed size (number of elements)
                                  defaultJVM->clz_Object,       // type
                                  // a_JRST->penv->FindClass("java/lang/Object"),    // type
                                  NULL              // initial value
                                  );

        RexxObjectPtr bsfClz=rgfGetEntryFromLocal (context->threadContext, "BSF"); // get .BSF, if available

            // fill the Java argument array with the passed in ojbects
        for (size_t i=0; i<numArgs; i++)   // iterate over all Rexx arguments
        {
            RexxObjectPtr tmpObj=context->ArrayAt(argArray, i+3);    // get Object from ooRexx argument array list

            if (tmpObj==NULL)              // NULL (argument missing)
            {
                param.env->SetObjectArrayElement(jarr, (jsize) i, NULL);
            }
            else    // either a string object or another ooRexx object in hand
            {
                    // get the ooRexx String class object
                // if (context->IsInstanceOf(tmpObj, STRING_CLASS(context->threadContext)))     // is the object an instance of it?
                if (context->IsOfType(tmpObj, "STRING"))      // is the object an instance of .String ?
                {
                    char *tmpStrObject=(char *) context->CString(context->ObjectToString(tmpObj));
                    jstring j_str=param.env->NewStringUTF(tmpStrObject);
                    param.env->SetObjectArrayElement(jarr, (jsize) i, j_str);
                    param.env->DeleteLocalRef(j_str);
                }
                else
                {
                    RexxStringObject rso=NULL;

                    if (bsfClz!=context->Nil() && context->IsInstanceOf(tmpObj, (RexxClassObject) bsfClz))
                    {
                        rso=(RexxStringObject) context->SendMessage0(tmpObj, "OBJECTNAME"); // get beanName
                    }

                       // if UNO_PROXY object, then hasMethod("UNO.BSFOBJECT") is true
                    else if (context->HasMethod(tmpObj, "UNO.BSFOBJECT"))
                    {
                        rso=(RexxStringObject) context->SendMessage0(context->SendMessage0(tmpObj, "UNO.BSFOBJECT"), "OBJECTNAME");
                    }
                    else    // use the string value instead
                    {
                        // if instance of .BSF or .UNO, use bsf's objectName, else raise an exception
                        rso=(RexxStringObject) context->ObjectToString(tmpObj);
                    }
                        // set Java array value, turn RexxStringObject to CString
                    jstring j_str=param.env->NewStringUTF(context->CString(rso));
                    param.env->SetObjectArrayElement(jarr, (jsize) i, j_str);
                    param.env->DeleteLocalRef(j_str);
                }
            }
        }



        // create JavaProxy
        //-----------------

            // run method and replace RexxProxy with JavaProxy
        j_rexxProxy=(jstring) param.env->CallObjectMethod(j_rexxProxy,
                                                             defaultJVM->mid_RexxProxy_newJavaOrExtendedProxyInstanceFromRexx,
                                                             jarr); // create JavaProxy

        if ( param.env->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
        {

            RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
                     1,         // RexxCallContext
                     context,
                     "%.16s/routine/BsfCreateRexxProxy(), error 4: Java exception occurred: [%s]");

            param.env->DeleteLocalRef(j_rexxProxy);
            param.env->DeleteLocalRef(jarr);

            RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
            environmentDetachFromNew(&param);
#else
            environmentDetachFrom(&param, FALSE);
#endif

            RgfReleaseLock();

            delete [] c_rii_ID;
            delete [] msg;
            return context->Nil();  // return prematurely
        }
        param.env->DeleteLocalRef(jarr);
    }

        // register with BSFRegistry
    jstring j_bsfRegistryKey=RgfStoreRexxProxyInBsfRegistry(param.env, param.rajo, j_rexxProxy, 1); // return prefix, if active

    if ( param.env->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
        RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
             1,         // RexxCallContext
             context,
             "%.16s/routine/BsfCreateRexxProxy(), error 5: Java exception occurred: [%s]");
    }

        // get native string from jstring
    char *c_bsfRegistryKey=JNU_GetStringNativeChars(param.env, j_bsfRegistryKey);

    // RexxObjectPtr r_result=(RexxObjectPtr) context->String(c_bsfRegistryKey);
    RexxObjectPtr r_result=(RexxObjectPtr) context->String(c_bsfRegistryKey);

        // if returned string looks like BSF.CLS is active, run BSF.WRAP to return a BSF_PROXY instead of the name only
    if (c_bsfRegistryKey[0]=='<' && c_bsfRegistryKey[2]=='>')   // looks like BSF.CLS induced prefix "<S>" or "<O>" is active, hence run bsf.wrap()
    {
        RexxClassObject clz=context->FindClass("BSF");  // try to get the BSF class
        if (clz!=NULL)  // class found?
        {
            r_result=context->SendMessage1(clz, "BSF.WRAP", r_result);  // run bsf.wrap(), may return a BSF_PROXY object
        }
        else
        {
            r_result=(RexxObjectPtr) context->String(c_bsfRegistryKey+3);
        }
    }

    RexxFreeMemory(c_bsfRegistryKey);

    param.env->DeleteLocalRef(j_rexxProxy);
    param.env->DeleteLocalRef(j_bsfRegistryKey);

    RgfAcquireLock();

#ifdef RGF_ATTACH_NEW
    environmentDetachFromNew(&param);
#else
    environmentDetachFrom(&param, FALSE);
#endif

    RgfReleaseLock();

#if defined (RGF_INFO) || defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "*** RGF_INFO: BsfCreateRexxProxy() 2, END  : tid=[%lu] ...\n",
            (unsigned long) tid
            );
    fflush(stderr);
#endif

    delete [] c_rii_ID;
    delete [] msg;
    return r_result;        // return Rexx object
}





    // ---rgf, 2009-09-06: usage BsfRexxProxy(proxy [, "ooRexxObject" | "userData" | "refCount"])
    //      argument 1:
    //          proxy   ... must be a RexxProxy object, i.e. a BSF-wrapped Java RexxProxy object
    //      argument 2:
    //          "ooRexxObject" (default)    ... return ooRexx object that gets proxied
    //          "userData"                  ... return ooRexx object representing the supplied "userData" object at creation time, or .nil
    //          "refCount"                  ... return actual number of references for this RexxProxy
RexxRoutine2(RexxObjectPtr, BsfRexxProxy, RexxObjectPtr, proxy, OPTIONAL_CSTRING, arg2val)
{

#if defined (RGF_INFO) || defined (DEBUG_REXX_PROXY)

    #ifdef UNIX
        pthread_t
    #else
        TID
    #endif
            tid=RgfGetTID();    // current thread
#endif


#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfRexxProxy() 1, tid=[%lu] ... AO\n", (unsigned long) tid);
    fflush(stderr);
#endif

    // process argument 1, check whether proxy is a RexxProxy indeed
    // 1) instance of BSF or a subclass ? (a must)
    RexxStringObject rexxObjectID=NULL;
    logical_t flag=context->IsOfType(proxy, "BSF");

    if (flag==1)        // o.k., proceed
    {

#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "*** *** RGF_INFO: BsfRexxProxy(): [1] 'flag=1' - rrid=[%p], flag=[%d], condition? [%d] ... \n",
                    context->threadContext->instance, (int) flag, (int) context->CheckCondition());
    fflush(stderr);
#endif

        // try to get the RexxObjectID
        rexxObjectID=(RexxStringObject) context->SendMessage0(proxy, "GETREXXOBJECTID");   // get RexxObjectID
    }

#if defined (DEBUG_REXX_PROXY)
    else
    {
        fprintf(stderr, "*** *** RGF_INFO: BsfRexxProxy(): [1] 'flag=1' - rrid=[%p], flag=[%d], condition? [%d], BSF~string=[%s], proxy~string=[%s] ... \n",
                        context->threadContext->instance, (int) flag, (int) context->CheckCondition(),
                        context->ObjectToStringValue(context->DirectoryAt(context->GetLocalEnvironment(), "BSF")),
                        context->ObjectToStringValue(proxy)
                        );
        fflush(stderr);
    }
#endif


#if defined (RGF_INFO) || defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "*** *** RGF_INFO: BsfRexxProxy(): [2] 'getRexxObjectId' - rrid=[%p], flag=[%d], rexxObjectID=[%p], condition? [%d] ... \n",
                    context->threadContext->instance, (int) flag, rexxObjectID, (int) context->CheckCondition());
    fflush(stderr);


    fprintf(stderr, "*** *** RGF_INFO: BsfRexxProxy(), exceptional state? tid=[%lu], flag=[%d], rexxObjectID=[%p], condition? [%d] ... ZO\n",
            (unsigned long) tid, (int) flag, rexxObjectID, (int) context->CheckCondition()
            );
    fflush(stderr);
#endif

    // if (flag==0 || rexxObjectID==NULLOBJECT || context->CheckCondition())    // not a RexxProxy object that points to a valid (cached) Rexx object!
    if (flag==0 || rexxObjectID==NULL || context->CheckCondition())    // not a RexxProxy object that points to a valid (cached) Rexx object!
    {

// get and supply condition message
        logical_t bCondition= context->CheckCondition();
        char *cond_msg=new char[2048];

        // if (context->CheckCondition())  // clear a pending condition
        if (bCondition)  // clear a pending condition
        {

// #if defined (DEBUG_REXX_PROXY)
// if (context->CheckCondition())
//        {
           RexxDirectoryObject condObj=context->GetConditionInfo();
           char * msg=RgfCreateRexxlikeErrorInfo (context->threadContext, condObj, "");
        //   fprintf(stderr, "... BsfRexxProxy(...): riid=[%p], condition()=[%s].\n", context->threadContext->instance, msg);fflush(stderr);
           SNPRINTF( cond_msg, 2048, " [underlying Rexx condition: '%.1536s']", msg);

           RexxFreeMemory(msg);

//        }
// #endif

            context->ClearCondition();
        }

        // context->InvalidRoutine();
        // char msg[2048]="";
        char *msg=new char[4096];
        if (flag==0 || bCondition==TRUE)
        {
            // sprintf(msg, "%.16s/routine/BsfRexxProxy(), error 1: argument 1 must be RexxProxy object, received '%.1024s' of type '%.512s'.",
            SNPRINTF( msg, 4096, "%.16s/routine/BsfRexxProxy(), error 1: argument 1 must be RexxProxy object, received '%.1536s' of type '%s' '%.2048s'",
                         DLLNAME,
                         context->ObjectToStringValue(proxy),
                         context->ObjectToStringValue( context->SendMessage0(proxy, "CLASS") ),
                         cond_msg
                         );
        }
        else    // rexxObjectId == NULL
        {
            SNPRINTF( msg, 4096, "%.16s/routine/BsfRexxProxy(), error 2: argument 1 does not embed a Rexx object (returns \"NULL\"), supplied RexxProxy object: '%.1536s' of type '%s' ",
                         DLLNAME,
                         context->ObjectToStringValue(proxy),
                         context->ObjectToStringValue( context->SendMessage0(proxy, "CLASS") )
                         );
        }

        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete [] cond_msg;
        delete [] msg;
        return context->Nil();
    }

    RexxObjectPtr rop=NULL;
    // o.k. so far, process argument 2
    if (arg2val==NULL || arg2val[0]=='o' || arg2val[0]=='O')    // get and return proxied Rexx object
    {
        rop=RgfGetProxyObject(context->threadContext, rexxObjectID);
    }
    else if (arg2val[0]=='u' || arg2val[0]=='U')                // get and return userData, or .nil, if none supplied
    {
        RexxObjectPtr rexxUserDataID=context->SendMessage0(proxy, "GETREXXUSERDATAID");   // get RexxObjectID
        if (rexxUserDataID==context->Nil())     // none defined, returns .nil
        {
            return rexxUserDataID;              // return .nil
        }
        rop=RgfGetProxyObject(context->threadContext, (RexxStringObject) rexxUserDataID);
    }
    else if (arg2val[0]=='r' || arg2val[0]=='R')                // get and return current refCount value
    {
        rop=RgfGetProxyObjectRefCount(context->threadContext, rexxObjectID);
    }
    else
    {
        // context->InvalidRoutine();
        // char msg[4096]="";
        char *msg=new char[4096];
        // sprintf(msg, "%.16s/routine/BsfRexxProxy(), error 2: illegal argument 2 value '%.3900s' (valid: 'ooRexxObject', 'userData', 'refCount').", DLLNAME, arg2val);
        SNPRINTF( msg, 4096, "%.16s/routine/BsfRexxProxy(), error 3: illegal argument 2 value '%.3900s' (valid: 'ooRexxObject', 'userData', 'refCount').", DLLNAME, arg2val);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete [] msg;
        return context->Nil();
    }
    return rop;
}





    // ---rgf, 2009-09-06: usage BsfRexxProxy(proxy [, "ooRexxObject" | "userData" | "refCount"])
    // ---rgf, 2010-02-09: usage BsfRawBytes( RexxStringObject | JavaPrimitiveByteArray-proxy )
    //
    //      argument 1:
    //          data      ... a RexxStringObject
    //                        (returns primitive Java byte array proxy)
    //                or
    //                        a BSF proxy representing a primitive, single-dimensioned Java byte array
    //                        (returns a RexxStringObject9
    //      argument 2:
    //          lengthToUser ... unsigned integer (size_t), if given, determines how many bytes of the
    //                           array or string should be converted

// argumentExists(n) or argumentOmitted(n), 1-based


RexxRoutine2(RexxObjectPtr, BsfRawBytes, RexxObjectPtr, data, OPTIONAL_size_t, lengthToUse)
{
    #ifdef UNIX
        pthread_t
    #else
        TID
    #endif
            tid=RgfGetTID();    // current thread

#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfRawBytes() 1, tid=[%lu] ... AO\n", (unsigned long) tid);
    fflush(stderr);
#endif

    // process argument 1, check whether proxy is a RexxProxy indeed
    // 1) instance of BSF or a subclass ? (a must)
    // RexxStringObject rexxObjectID=NULL;
    // logical_t flag=context->IsOfType(data, "BSF");
    size_t  kind=0;         // kind of desired conversion
    size_t  errorKind=0;    // kind of error
    CSTRING objectname=NULL;// objectname of bean


    if (context->IsOfType(data, "STRING"))  // convert from RexxString -> [B
    {
        kind=1;             // indicate 'data' is a RexxStringObject
    }
    else if (context->IsOfType(data, "BSF"))// convert from [B -> RexxString ?
    {
        kind=2;             // indicate 'data' is a BSF object
        objectname=context->CString((RexxStringObject) context->SendMessage0(data, "OBJECTNAME"));
            // a
        if (! (objectname[0]=='[' && objectname[1]=='B' && objectname[2]=='@') )
        {
            errorKind=1;        // no single dimensioned primitive Java byte array object
        }
    }
    else
    {
        errorKind=99;           // datatype that cannot be handled, neither RexxString, nor [B
    }

    if (errorKind>0)            // an argument error
    {
        // char msg[1024]="";
        char *msg=new char[1024];

        // sprintf(msg, "%.16s/routine/BsfCreateRexxProxy(), error 1: first argument (a Rexx object or a string) is missing", DLLNAME);
        SNPRINTF( msg, 1024, "%.16s/routine/BsfRawBytes(), error 1: argument neither a .String nor a .BSF object (representing a single dimensioned primitive byte array). Received argument=[%.512s]",
                             DLLNAME,
                             context->CString(context->ObjectToString(data))
                             );
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return context->Nil();
    }


    // ??? TODO: attach to Java, carry out conversions, return result
#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *)context->threadContext->instance, false, 0};
#else
    // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error};
    STRUCT_PARAM param={tid, 0, 0, (void *)context->threadContext->instance, 0, 0, 0, 0};
#endif


    RgfAcquireLock();

#ifdef RGF_ATTACH_NEW
    environmentAttachToNew(&param); // attach to Java
#else
    environmentAttachTo(&param, FALSE); // attach to Java

    if (param.error==-2)    // ---rgf, 2014-03-30: thread not attached, use primodal TID
    {
        RgfReleaseLock();

        bool bUsingPrimodialTID=false;

        RexxDirectoryObject localDir = context->GetLocalEnvironment();   // get .local
        RexxObjectPtr obj = context->DirectoryAt(localDir, LOCAL_PRIMODAL_TID);

        if (obj==NULL) // NULLOBJECT)
        {
            char msg[256];
            SNPRINTF(msg, 256, "%.16s/routine/BsfAttachToTID(), error 3: no TID given, nor %s defined in .local", DLLNAME, LOCAL_PRIMODAL_TID);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            return FALSE;
        }

        char *toTID=(char *) context->CString(context->ObjectToString(obj));
        bUsingPrimodialTID=true;

        unsigned long attachToTID=0;

        sscanf(toTID, "%lu", &attachToTID);    // get integer value from string

        // now try to attach
                  // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error,attach2tid};
        STRUCT_PARAM param2={tid, 0, 0, (void *)context->threadContext->instance, 0, 0, 0,
            (
            #ifdef UNIX
                pthread_t
            #else
                TID
            #endif
            )
            attachToTID};

        RgfAcquireLock();
        environmentAttachTo(&param2, TRUE); // attach to Java, keep attached

        param=param2;   // copy param2 fields to param to let the checking go on

#ifdef DEBUG_JAVA_ATTACH_DETACH
    fprintf(stderr, "---> BSFRawBytes() // 2 --> used primodal TID! param.error=[%d], param2.error=[%d]\n", param.error, param2.error); fflush(stderr);
#endif
    }

#endif


#ifdef DEBUG_JAVA_ATTACH_DETACH
    fprintf(stderr, "---> BSFRawBytes() // 1--> param.error=[%d]\n", param.error); fflush(stderr);
#endif


    // if (param.error== -1)    // could not attach to Java
    if (param.error!=0)    // rgf, 2014-03-30, problem attaching, cannot proceed
    {
        RgfReleaseLock();
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined,
#ifdef RGF_ATTACH_NEW
                                 createAttachErrorMessageRSONew
#else
                                 createAttachErrorMessageRSO
#endif
                                 (context, "routine/BsfRawBytes(), error 2", &param)
                                 );
        return context->Nil();      // not attached, hence no detaching !
    }
    RgfReleaseLock();

    RexxObjectPtr result_Obj=NULL;

    if (kind==1)                // RexxString -> [B
    {
        // create [B from RexxString, put into BSFRegistry, return it
        // 2010-05-23, rgf: could be that internally it is not (yet) a pure RexxStringObject,
        //                  cf.
        RexxStringObject rso_data= context->IsString(data) ? (RexxStringObject) data
                                                           : context->ObjectToString(data) ;

        size_t      len=context->StringLength(rso_data); // get length

        if (argumentExists(2))      // lengthToUse argument given
        {
            if (len<lengthToUse)    // Rexx string not long enough, raise error
            {
                errorKind=1;        // indicate error

                // char msg[1024]="";
                char *msg=new char[1024];
                SNPRINTF( msg, 1024, "%.16s/routine/BsfRawBytes(), error 3: argument 2 ('lengthToUse') value (%zu) larger than maximum length (%zu)",
                                     DLLNAME,
                                     lengthToUse, len
                                     );
                context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                delete[] msg;
            }
            else
            {
                len=lengthToUse;    // use supplied length
            }
        }

        if (errorKind==0)   // no error, o.k. to proceed
        {
            const char *buf=context->CString(rso_data);
            jbyteArray   jba=param.env->NewByteArray((jsize) len);   // create Java primitive byte array

            if ( ! param.env->ExceptionCheck() )   // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
            {
                if (len>0)
                {
                    param.env->SetByteArrayRegion(jba, 0, (jsize) len, (jbyte *) buf);
                }

                if ( ! param.env->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
                {
                    // create a bean of the Java object; if .BSF is available, then return a bsf.wrap() of it
                    jstring j_beanName = (jstring) param.env->CallObjectMethod(param.rajo,
                                                       defaultJVM->mid_RexxAndJava_registerBean4JNI,
                                                       jba);

                    // get char* beanName, do a bsf.wrap() if available; return that result
                    const char * c_beanName = param.env->GetStringUTFChars(j_beanName, JNI_FALSE);
                    RexxStringObject r_beanName=context->String(c_beanName);    // create Rexx string
                    param.env->ReleaseStringUTFChars(j_beanName, c_beanName);         // release
                    param.env->DeleteLocalRef(j_beanName);

                    RexxObjectPtr bsfClz=rgfGetEntryFromLocal (context->threadContext, "BSF"); // get .BSF, if available
                    if (bsfClz!=context->Nil())  // class found, do a bsf.wrap() on the beanName
                    {
                        result_Obj=context->SendMessage1(bsfClz, "BSF.WRAP", r_beanName);  // run bsf.wrap(), may return a BSF_PROXY object
                    }
                    else    // just return the supplied beanName (a String)
                    {
                        result_Obj=r_beanName;
                    }
                }
                param.env->DeleteLocalRef(jba);
            }
        }
    }
    else                        // [B -> RexxString
    {
        // get bean from the Java side ----------------------------------------------------------------
        jstring jObjectName=param.env->NewStringUTF(objectname);
        jbyteArray jba=(jbyteArray) param.env->CallObjectMethod(param.rajo,
                                                  defaultJVM->mid_RexxAndJava_lookupBean4JNI,
                                                  jObjectName
                                                  );
        param.env->DeleteLocalRef(jObjectName);


        if (jba==NULL)     // no bean found by the given beanName
        {
            // char msg[4096]="";
            char *msg=new char[4096];
            SNPRINTF( msg, 4096, "%.16s/routine/BsfRawBytes(), error 4: supplied argument (primitive Java byte array) with the beanName=[%s] cannot be found in the BSFRegistry)", DLLNAME, objectname);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            result_Obj=context->False();
            delete[] msg;
        }
        else                // now process the jbyteArray
        {
            size_t len=(size_t) param.env->GetArrayLength(jba);

            if (argumentExists(2))      // lengthToUse argument given
            {
                if (len<lengthToUse)    // Rexx string not long enough, raise error
                {
                    errorKind=1;        // indicate error

                    // char msg[1024]="";
                    char *msg=new char[1024];
                    SNPRINTF( msg, 1024, "%.16s/routine/BsfRawBytes(), error 3: argument 2 ('lengthToUse') value (%zu) larger than maximum length (%zu)",
                                         DLLNAME,
                                         lengthToUse, len
                                         );
                    context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                    delete[] msg;
                }
                else
                {
                    len=lengthToUse;    // use supplied length
                }
            }

            if (errorKind==0)   // no error, o.k. to proceed
            {
                if (len==0)     // easy, empty array, return an empty string
                {
                    result_Obj=context->NewString("", 0);
                }
                else            // get bytes in Java array, create a Rexx string of it
                {
                    char *buf=(char *) RexxAllocateMemory( len );
                    param.env->GetByteArrayRegion(jba, 0, (jsize) len, (jbyte *) buf);

                    if ( ! param.env->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
                    {
                        result_Obj=context->NewString(buf, len);
                    }
                    RexxFreeMemory(buf);
                }
            }
            param.env->DeleteLocalRef(jba);
        }
    }


    if (errorKind==0 && param.env->ExceptionCheck())    // if a pending Java exception, handle it and create a Rexx exception
    {
        RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
             1,         // RexxCallContext
             context,
             "%.16s/routine/BsfRawBytes(), error 5: caused Java exception: [%s]");

        result_Obj=context->Nil();
    }

    RgfAcquireLock();

#ifdef RGF_ATTACH_NEW
    environmentDetachFromNew(&param);
#else
    environmentDetachFrom(&param, FALSE);    // do not close group
#endif

    RgfReleaseLock();

    return result_Obj;
}






    // rgf, 2009-05-23
/* Sets the variables bsf.userData, bsf.methodName, bsf.creationTID in the caller from the supplied argument.

   The argument may be an array of arguments, in which case the last argument is expected to be
   the callback directory, otherwise the argument may be the callback directory itself.

   If the callback directory cannot be found .false is returned, otherwise the entries of the callback
   directory are made available in the caller's context via the local stem named "BSFARG."

   returns .true if it worked, .false if callback directory could not be found
*/
/*
20090623, rgf: not really needed anymore; it is very easy to get at the directory (last callback argument)
               and then fetch whatever entry one needs;
               hence removing this external function in order to simplify again

RexxRoutine1(RexxObjectPtr, BsfProcessCallbackArgs, RexxObjectPtr, arg)
{

#if defined (RGF_INFO) || defined (DEBUG_40)
    fprintf(stderr, "*** RGF_INFO: BsfRetrieveArgs()  ...\n");
    fflush(stderr);
#endif

    char msg[4096]="";

    RexxObjectPtr dir=arg;      // directory, default to arg

    if (context->IsOfType(arg, "ARRAY"))        // if an argument array, fetch last item
    {
            // get last item in array
        dir=context->SendMessage1(arg, "AT", context->SendMessage0(arg, "SIZE"));
    }

    if (context->IsOfType(dir, "DIRECTORY"))    // o.k., maybe a callback directory?
    {
         RexxObjectPtr tmp=context->DirectoryAt((RexxDirectoryObject) dir, "METHODNAME");

         if (tmp==NULL || tmp==context->Nil())  // not available, hence not a callback directory
         {
             return context->False();
         }

         RexxDirectoryObject tmpDir=(RexxDirectoryObject) dir;  // save us constant casting

         // TODO: check whether stem exists, if so, does SetContextVariable() drop an existing one?
         //       think about allowing the stem name as an optional second argument
         context->SetContextVariable(REXX_CALLBACK_ARG_STEM_NAME, context->Nil()); // set stem variable and default value
         RexxStemObject st=(RexxStemObject) context->GetContextVariable(REXX_CALLBACK_ARG_STEM_NAME);   // retrieve stem variable

         RexxArrayObject indices=(RexxArrayObject) context->SendMessage0(tmpDir, "MAKEARRAY");

         size_t nr=context->ArrayItems(indices);        // get number of indices

         const char *  dirIdx=NULL;
         RexxObjectPtr    rop=NULL;

         for (size_t i=1; i<=nr; i++)           // iterate over all entries
         {
             dirIdx=(const char *) context->CString((RexxStringObject) context->ArrayAt(indices, i));      // get index
             rop   =context->DirectoryAt(tmpDir, dirIdx);

                // now set the stem element
             context->SetStemElement(st, dirIdx, rop);      // set BSF.CREATIONTID
         }
         return context->True();
    }

    return context->False();
}
*/




// / *
// * External Rexx function "BSF". This function allows Rexx programs to call into Java.
//  * /

// --------------------------------->
RexxRoutine1(RexxObjectPtr, BSF, ARGLIST, argArray)    // 20090505, ---rgf, only a stub for backward compatibility
{
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();
    // char msg[4096]="";      // error message buffer
    char *msg=new char[4096];

#if defined(RGF_INFO) || defined (RGF_BIT2) || defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_BSF_FUNCTION)
    fprintf(stderr, "*** RGF_INFO: BSF() 1, tid=[%lu], ", (unsigned long) tid);
    fflush(stderr);
#endif

/*
{   // mist, rgf, 2014-03-30, temporary (ad-hoc) debug
    size_t numArgs=context->ArraySize(argArray);   // get number of arguments

    for (size_t i=1; i<=numArgs; i++)
    // if (numArgs>0)
    {
        fprintf(stderr, "\t numArgs=[%lu] argArray[%lu]=[%s]\n",
                        numArgs, i,
                        context->CString(context->ArrayAt(argArray,i)));

    }
}
*/

#ifdef RGF_ATTACH_NEW
   // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
   STRUCT_ATTACH_PARAM param={0, 0, (void *)context->threadContext->instance, false, 0};
#else
    // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error};
    STRUCT_PARAM param={tid, 0, 0, (void *)context->threadContext->instance, 0, 0, 0, 0};
#endif

    RgfAcquireLock();

#ifdef RGF_ATTACH_NEW
    environmentAttachToNew(&param); // attach to Java
#else
    environmentAttachTo(&param, FALSE); // attach to Java

        // - param->error:  -1 ... Attach-error, attachResult holds Java error code, raise an exception in caller
        //                  -2 ... no rajo found, raise an exception in caller
        //                  -3 ... cannot safely use the given RAJO in TID, as actual RAJO is being used in multiple Rexx threads
        //                  -4 ... cannot attach to target TID
        //                 -99 ... no JVM available !

        // error '-99' may may happen, if BsfUnloadJava() was called and UNINIT operations on BSF proxies
        // get triggered, which would unregister the counterpart Java objects from the BSFRegistry;
        // this lets Rexx shutdown gracefully

    #ifdef DEBUG_JAVA_ATTACH_DETACH
        fprintf(stderr, "---> BSF() // 1 --> param.error=[%d]\n", param.error); fflush(stderr);
    #endif

        if (param.error==-2)    // ---rgf, 2014-03-30: thread not attached, use primodal TID
        {
            RgfReleaseLock();

            bool bUsingPrimodialTID=false;

            RexxDirectoryObject localDir = context->GetLocalEnvironment();   // get .local
            RexxObjectPtr obj = context->DirectoryAt(localDir, LOCAL_PRIMODAL_TID);

            if (obj==NULL) // NULLOBJECT)
            {
                // char msg2[256];
                char *msg2=new char[256];
                SNPRINTF(msg2, 256, "%.16s/routine/BsfAttachToTID(), error 3: no TID given, nor %s defined in .local", DLLNAME, LOCAL_PRIMODAL_TID);
                context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg2));
                delete[] msg2;

                delete[] msg;
                return FALSE;
            }

            char *toTID=(char *) context->CString(context->ObjectToString(obj));
            bUsingPrimodialTID=true;

            unsigned long attachToTID=0;

            sscanf(toTID, "%lu", &attachToTID);    // get integer value from string

            // now try to attach
                      // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error,attach2tid};
            STRUCT_PARAM param2={tid, 0, 0, (void *)context->threadContext->instance, 0, 0, 0,
                (
                #ifdef UNIX
                    pthread_t
                #else
                    TID
                #endif
                )
                attachToTID};

            RgfAcquireLock();
            environmentAttachTo(&param2, TRUE); // attach to Java, keep attached

            param=param2;   // copy param2 fields to param to let the checking go on

    #ifdef DEBUG_JAVA_ATTACH_DETACH
        fprintf(stderr, "---> BSF() // 2 --> used primodal TID! param.error=[%d], param2.error=[%d]\n", param.error, param2.error); fflush(stderr);
    #endif

        }

#endif


    if (param.error!= 0)    // problem attaching, cannot proceed
    {
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined,
#ifdef RGF_ATTACH_NEW
                                 createAttachErrorMessageRSONew
#else
                                 createAttachErrorMessageRSO
#endif
                                 (context, "routine/BsfCreateRexxProxy(), error 3", &param)
                                 );
        if (param.error != -1)  // if attaching worked, we need to make sure that we detach as well
        {
#ifndef RGF_ATTACH_NEW
            environmentDetachFrom(&param, FALSE);   // detach from Java
#endif
        }
        RgfReleaseLock();
        delete[] msg;
        return FALSE;
    }
    RgfReleaseLock();

// *** >>>> ????
    // size_t numArgs=context->ArrayItems(argArray);   // get number of arguments
    size_t numArgs=context->ArraySize(argArray);   // get number of arguments

    //convert arguments array to Java format
    jobjectArray jarr = param.env->NewObjectArray (
                              (jsize) numArgs,      // needed size (number of elements)
                              defaultJVM->clz_String,   // (*env).FindClass("java/lang/String"),
                              defaultJVM->jstring_emptyUTFString// (*env).NewStringUTF("")
                              );
#ifdef RGF_INFO_1 // rgf, 20090820
    fprintf(stderr, "\t\t\t 1 - after creating Java array object 'jarr'...\n");fflush(stderr);
#endif

    // 4.0: if creating RexxProxy objects, they get registered in BSFRegistry, hence
    //      we need to remove them from the BSFRegistry after the BSF-invocation; this
    //      array will remember the beanNames of these
    jobjectArray jarrToRemove=param.env->NewObjectArray(
                               (jsize) numArgs,     // the maximum number of entries
                               defaultJVM->clz_String,  // String type
                               NULL                 // use NULL for initialization
                               );

#ifdef RGF_INFO_1 // rgf, 20090820
    fprintf(stderr, "\t\t\t 2 - after creating Java array object 'jarrToRemove'...\n");fflush(stderr);
#endif

    size_t nrToRemove=0;                        // memorize nr of entries to remove


#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_BSF_FUNCTION)
    fprintf(stderr, "--- RGF_INFO: BSF() 1, tid=[%lu], numArgs=[%lu]", (unsigned long) tid, numArgs);fflush(stderr);
    if (numArgs>0)
    {

    // rgf, 20090820
    fprintf(stderr, "\n"); fflush(stderr);
    for (size_t i=0; i<numArgs; i++)
    {
       RexxObjectPtr tmpObj=context->ArrayAt(argArray, i+1);
       fprintf(stderr, "\ti=%lu, tmpObj: %%lu=%lu %%p=%p", i+1, (unsigned long) tmpObj, tmpObj);
       fflush(stderr);

       int tind=RgfValue4JavaIndicator(context->threadContext, tmpObj);
       fprintf(stderr, ", typeIndicator=%d\n", tind);
       fflush(stderr);
    }

        for (size_t i=0; i<numArgs; i++)   // iterate over all Rexx arguments
        {
            RexxObjectPtr tmpObj=context->ArrayAt(argArray, i+1);
            int tind=RgfValue4JavaIndicator(context->threadContext, tmpObj);
            fprintf(stderr, "[%lu]=", i+1); fflush(stderr);
            fprintf(stderr, "[%.256s]", (tind<0?"<null>":context->CString(tmpObj))); fflush(stderr);
            // fprintf(stderr, " | [%u]=[%.256s]", i+1, context->CString(context->ArrayAt(argArray, i+1)));
        }
    }
    fprintf(stderr, " ---\n");
    fflush(stderr);
#endif

            // fill the Java argument array with the passed in objects
        for (size_t i=0; i<numArgs; i++)   // iterate over all Rexx arguments
        {
            RexxObjectPtr tmpObj=context->ArrayAt(argArray, i+1);    // get Object from ooRexx argument array list

            // get the indicator value; returns "2" (BSF/UNO_PRoxy), "1" (String), "0" (RexxProxy),
            // "-1" (NULL), "-2" (.nil)
            int typeIndicator=RgfValue4JavaIndicator(context->threadContext, tmpObj);

            if (typeIndicator<0)        // NULL (argument missing) or .nil
            {
#if defined (DEBUG_REXX_PROXY_BSF)
    fprintf(stderr, "-> BSF()a: \targ[%lu] NULL[OBJECT] \n",(unsigned long) i); fflush(stderr);
#endif
                param.env->SetObjectArrayElement(jarr, (jsize) i, NULL);
            }
            else    // either a string object or another ooRexx object in hand
            {

#if defined (DEBUG_REXX_PROXY_BSF)
    RexxStringObject strTmpObj= RgfDebug_GetMaxStringValue(context->threadContext, tmpObj, 90) ;
    fprintf(stderr, "... BSF()b: arg[%lu]=[%.256s]\n", (unsigned long) i, context->CString(strTmpObj) );

    // fprintf(stderr, "... BSF(): arg[%u]=[%.256s]\n", i, context->CString(context->SendMessage0(tmpObj, "STRING")));
#endif

                    // get the ooRexx String class object
                if (typeIndicator==1)   // a .STRING
                {

#if defined (DEBUG_REXX_PROXY_BSF)
    // fprintf(stderr, "-> BSF(): \targ[%u] STRING       val=[%.256s]\n",i, context->CString(context->ObjectToString(tmpObj))); fflush(stderr);
    fprintf(stderr, "-> BSF()c: \targ[%lu] STRING       val=[%.256s]\n",(unsigned long) i,
       context->CString(RgfDebug_GetMaxStringValue(context->threadContext, tmpObj, 180))); fflush(stderr);
#endif

                    jobject jobj=JNU_CreateJavaString(param.env,
                                                      context->threadContext,
                                                      context->IsString(tmpObj) ? (RexxStringObject) tmpObj
                                                                                : context->ObjectToString(tmpObj)
                                                               );

                    param.env->SetObjectArrayElement(jarr, (jsize) i, jobj);
                    param.env->DeleteLocalRef(jobj);
                }
                else    // TODO: this is a non-String Rexx object, create a proxy for Java !
                {
                        // if .BSF or .UNO_proxy objects, they are registered in the BSFRegistry by their objectname
                    if (typeIndicator>1)    // a BSF/UNO_PROXY object in hand
                    {
#if defined (DEBUG_REXX_PROXY_BSF)
    fprintf(stderr, "-> BSF()d: \targ[%lu] BSF/UNO_PROXY val=[%.256s]\n",(unsigned long) i, context->CString(RgfDebug_GetMaxStringValue(context->threadContext, tmpObj, 90)) ); fflush(stderr);
#endif
                        char *tmpStrObject=(char *) context->CString(context->ObjectToString(tmpObj));
                        jstring jStr=param.env->NewStringUTF(tmpStrObject);
                        param.env->SetObjectArrayElement(jarr, (jsize) i, jStr);
                        param.env->DeleteLocalRef(jStr);
                    }
                    else    // any other Rexx object, submit as a RexxProxy argument
                    {

#if defined (DEBUG_REXX_PROXY_BSF)
    char *tmpStrObject=(char *) context->CString(context->ObjectToString(tmpObj));
    fprintf(stderr, "->BSF()e: debug! \targ[%lu] is a non-String, non-BSF/UNO RexxObject, type=[%.256s]: creating a RexxProxy for it!\n\t~string=[%.256s]",
                     (unsigned long) i,
                     context->CString(context->ObjectToString(context->SendMessage0(tmpObj,"CLASS"))),
                     tmpStrObject);
    fflush(stderr);
#endif
                            // create RexxProxy for it
                        jobject j_rexxProxy=RgfCreateRexxProxy (param.env, param.rajo,
                                                                context->threadContext, // ThreadContext, RMG: 20090514
                                                                tmpObj,                 // Rexx object
                                                                NULL,                   // Package object
                                                                NULL                    // userData (slot)
                                                                );

                            // store beanName (without prefix) in BSFRegistry, hence we can keep the String[] signature
                        jstring j_bsfRegistryKey=RgfStoreRexxProxyInBsfRegistry(param.env, param.rajo, j_rexxProxy, 0);

                            // save the beanName with the array
                        param.env->SetObjectArrayElement(jarr, (jsize) i, j_bsfRegistryKey);

                            // add to remove list (bean needs to removed from BSFRegistry after the run)
                        param.env->SetObjectArrayElement(jarrToRemove, (jsize) nrToRemove++, j_bsfRegistryKey);

                        param.env->DeleteLocalRef(j_rexxProxy);
                        param.env->DeleteLocalRef(j_bsfRegistryKey);
                    }
                }
            }

        }

#if defined(RGF_INFO_1) || defined (DEBUG_BSF_FUNCTION) // || defined (RGF_BIT2)
    fprintf(stderr, "*** *** RGF_INFO_1: BSF() 4 - <==> (BEFORE -> CallObjectMethod(), tid=[%lu] ... ", (unsigned long) tid);
    fflush(stderr);


    if ( param.env->ExceptionCheck() )
    {
        fprintf(stderr, "Sch..., ExceptionCheck() yields JNI_TRUE!");
        // rgf, 20090912: gcc thinks the following is not available in this scope ?
        // fprintf(stderr, "Sch..., ExceptionCheck()=[%d]", a_JRST->penv>ExceptionCheck());
    }

    fprintf(stderr, "\n");
    fflush(stderr);
#endif



#if defined( DEBUG_REXX_PROXY_BSF )

// inline VOID JAVA_TO_STRING (JNIEnv *env, jobject jo, char * buffer)
{
    char tmpBuf[1024];
    JAVA_TO_STRING( param.env, param.rajo, tmpBuf, 1024);
    fprintf(stderr, "*** *** DEBUG_REXX_PROXY_BSF: BSF() 1 - tid=[%lu], a_JRST->rajo=[%p] [%s]\n",
                                     (unsigned long) tid,
                                     param.rajo,
                                     tmpBuf
                                     );
    fflush(stderr);
}

#endif

        // call Java method and create Rexx return value
        jstring j_returnValue = (jstring) param.env->CallObjectMethod(// obj, globMid, arr);
                                 param.rajo,
                                 defaultJVM->mid_RexxAndJava_javaCallFromBSF,
                                 jarr);

#if defined(RGF_INFO_1) || defined (DEBUG_BSF_FUNCTION)  // || defined (RGF_BIT2)
    fprintf(stderr, "*** *** RGF_INFO_1: BSF() 5 - <==> (AFTER  -> CallObjectMethod(), tid=[%lu], j_returnValue=[%p] ...)\n", (unsigned long) tid, j_returnValue);
    fflush(stderr);
#endif

                // pending exception ?
        if ( param.env->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
        {
            RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
                 1,         // RexxCallContext
                 context,
                 "%.16s/routine/BSF(), error 3: Java exception occurred: [%s]");

#if defined ( RGF_INVALID_ROUTINE_NIXINOXI )
    // char tmpStr[2560]=0;"\n<<< BSF(...): ExceptionCheck(), exception occurred:\n[%2048s] >>>\n\n";
    char tmpStr[2560];
    // sprintf(tmpStr, "\n<<< BSF(...): ExceptionCheck(), exception occurred:\n[%2048s] >>>\n\n", context->CString(rso));
    // sprintf(tmpStr, "<<< RGF_INVALID_ROUTINE, RaiseException1(), now context->CheckCondition()=[%d] >>>\n<<< BSF(...): ExceptionCheck(), exception occurred:\n[%.2048s] >>>\n\n",
    SNPRINTF( tmpStr, 2560, "<<< RGF_INVALID_ROUTINE, RaiseException1(), now context->CheckCondition()=[%d] >>>\n<<< BSF(...): ExceptionCheck(), exception occurred:\n[%.2048s] >>>\n\n",
                    context->CheckCondition(), context->CString(rso));
    fprintf(stderr, tmpStr);fflush(stderr);
//    JNU_MessageBox(a_JRST->penv, tmpStr);
#endif

            // make sure we remove those RexxProxies we registered on this invocation dynamically
            for (size_t z=0; z<nrToRemove; z++)
            {
                jobject jobj=param.env->GetObjectArrayElement(jarrToRemove, (jsize) z);
                RgfUnregisterBean(param.env, param.rajo, (jstring) jobj);
                param.env->DeleteLocalRef(jobj);
            }

            param.env->DeleteLocalRef(jarr);
            param.env->DeleteLocalRef(jarrToRemove);
            param.env->DeleteLocalRef(j_returnValue);

            RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
            environmentDetachFromNew(&param);
#else
            environmentDetachFrom(&param, FALSE);    // do not close group
#endif

            RgfReleaseLock();

            delete[] msg;
            return context->String("-1");
        }

        // make sure we remove those RexxProxies we registered on this invocation dynamically
        for (size_t z=0; z<nrToRemove; z++)
        {
            jobject jobj=param.env->GetObjectArrayElement(jarrToRemove, (jsize) z);
            RgfUnregisterBean(param.env, param.rajo, (jstring) jobj);
            param.env->DeleteLocalRef(jobj);
        }
        param.env->DeleteLocalRef(jarr);
        param.env->DeleteLocalRef(jarrToRemove);

        if (j_returnValue==NULL)          // null returned from Java
        {
            // int rndRes=RgfNewDetach();  // detach from thread
            RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
            environmentDetachFromNew(&param);
#else
            environmentDetachFrom(&param, FALSE);    // do not close group
#endif
            RgfReleaseLock();

#if defined (DEBUG_BSF_FUNCTION)
    fprintf(stderr, "<-- BSF(), upon return to Rexx, Java returned NULL, we return \"\", context->CheckCondition()=[%d] (all o.k., should be 0)\n", (int) context->CheckCondition());
    fflush(stderr);
#endif

            delete[] msg;
            return context->NullString();
        }

        RexxObjectPtr result=JNU_GetStringNativeCharsReturningRexxStringObject(param.env, j_returnValue, context->threadContext);       // rgf, 2009-04-20, 20090515
        param.env->DeleteLocalRef(j_returnValue);

        RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
            environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif
        RgfReleaseLock();

#if defined (DEBUG_BSF_FUNCTION)
    fprintf(stderr, "<-- BSF(), upon return to Rexx with a result from Java, context->CheckCondition()=[%d] (all o.k., should be 0)\n", (int) context->CheckCondition());
    fflush(stderr);
#endif

        delete[] msg;
        return result;
}





//    // rgf, 2009-10-18
//  Function to allow to tackle the Java exception facility, syntax:
//       BsfJavaException( "throw", exceptionObject )
//       ... throws a Java exception using the supplied 'exceptionObject'
//
//  following commands may be implemented: "check" (.true/.false), "clear",
//            "object|occurred" (Throwable), "fatalError"
//
//  - if exceptionProxy is of type .BSF or .UNO_PROXY use its objectName, else
//    use string value of object as the beanName to look up in the BSF registry
//
//  - check whether a Java exception is already pending, if so return .false
//
//  - lookup beanName in the BSF-registry, must exist and an instance of java.lang.Throwable
//
//    - throw the given Java exception;
//      - if an error occured, then report it to ooRexx in form of a Rexx condition (and return .false)
//      - else return .true
//
RexxRoutine2(RexxObjectPtr, BsfJavaException, CSTRING, option, OPTIONAL_RexxObjectPtr, exceptionInfo)
{
    #ifdef UNIX
        pthread_t
    #else
        TID
    #endif
            tid=RgfGetTID();    // current thread

#if defined (RGF_INFO) || defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "*** RGF_INFO: BsfJavaException() 1, BEGIN: tid=[%lu] ...\n",
            (unsigned long) tid
            );
    fflush(stderr);
#endif

    int flag=0;     // 1=throw,throwableObj, 2=check, 3=clear, 4=object/occurred, 5=fatalError, msg

    if (option[0]=='t' || option[0]=='T')     // throw exception ?
    {
        flag=1;         // throw exception in Java
    }
    else if ( option[0]=='c' || option[0]=='C' )
    {
        if ( option[1]=='h' || option[1]=='H' )
        {
            flag=2;     // check for pending Java exception
        }
        else if (option[1]=='l' || option[1]=='L')
        {
            flag=3;     // clear a pending Java exception
        }
    }
    else if (option[0]=='o' || option[0]=='O')
    {
        flag=4;         // get Throwable for pending Java exception
    }
    else if (option[0]=='f' || option[0]=='F')
    {
        flag=5;         // raise a fatal error in Java
    }


    // char msg[4096]="";
    char *msg=new char[4096];
    delete[] msg;

    if (flag==0)        // no valid option given
    {
        // do not document "fatalError" in error message; should not be known to regular script user
        // as 'fatalError' will usually bring down the entire JVM
        SNPRINTF( msg, 4096, "%.16s/routine/BsfJavaException(), error 1: argument 1 value '%s' is invalid; valid values: 'CH[eck]', 'CL[ear]', 'O[ccurred]', 'T[hrow]'", DLLNAME, option);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return context->False();
    }


    char *c_beanNameOrMsg   =NULL;  // the beanName of the exceptionProxy
    RexxObjectPtr bsfClz=rgfGetEntryFromLocal (context->threadContext, "BSF"); // get .BSF, if available

    if (flag==1)        // throw exception in Java, needs second argument
    {
        if (exceptionInfo==NULL)    // omitted exceptionObject ?
        {
            SNPRINTF( msg, 4096, "%.16s/routine/BsfJavaException(), error 2: argument 2 (a Java Throwable object) missing", DLLNAME);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
            return context->False();
        }

        // check argument, get beanName as a "char *" ------------------------------------------------------
        RexxStringObject rso=NULL;  // RexxStringObject

        if (bsfClz!=context->Nil() && context->IsInstanceOf(exceptionInfo, (RexxClassObject) bsfClz))
        {
            rso=(RexxStringObject) context->SendMessage0(exceptionInfo, "OBJECTNAME"); // get beanName
        }

           // if UNO_PROXY object, then hasMethod("UNO.BSFOBJECT") is true
        else if (context->HasMethod(exceptionInfo, "UNO.BSFOBJECT"))
        {
            rso=(RexxStringObject) context->SendMessage0(context->SendMessage0(exceptionInfo, "UNO.BSFOBJECT"), "OBJECTNAME");
        }
        else    // use the string value instead
        {
            // if instance of .BSF or .UNO, use bsf's objectName, else raise an exception
            rso=(RexxStringObject) context->ObjectToString(exceptionInfo);
        }
        c_beanNameOrMsg=(char *) context->ObjectToStringValue(rso);   // get char* rendering
    }
    else if (flag==5)           // fatalError, needs second argument
    {
        if (exceptionInfo==NULL)    // omitted fatalError message ?
        {
            SNPRINTF( msg, 4096, "%.16s/routine/BsfJavaException(), error 2: argument 1 (supplied option: 'FatalError', Sun's JNI documents: \"Raises a fatal error and does not expect the VM to recover. This function does not return.\") needs a mandatory second argument (the fatal error message)", DLLNAME);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
            return context->False();
        }

        c_beanNameOrMsg=(char *) context->ObjectToStringValue(exceptionInfo);   // get char* rendering
    }


    // ------------------------------------------------------------------------------------
    // attach to Java thread
#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *)context->threadContext->instance, false, 0};
#else
    // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error};
    STRUCT_PARAM param={tid, 0, 0, (void *)context->threadContext->instance, 0, 0, 0, 0};
#endif

    RgfAcquireLock();

#ifdef RGF_ATTACH_NEW
    environmentAttachToNew(&param);     // attach to Java
#else
    environmentAttachTo(&param, FALSE); // attach to Java

    #ifdef DEBUG_JAVA_ATTACH_DETACH
        fprintf(stderr, "---> BSFJavaException() // 1 --> param.error=[%d]\n", param.error); fflush(stderr);
    #endif

        if (param.error==-2)    // ---rgf, 2014-03-30: thread not attached, use primodal TID
        {
            RgfReleaseLock();

            bool bUsingPrimodialTID=false;

            RexxDirectoryObject localDir = context->GetLocalEnvironment();   // get .local
            RexxObjectPtr obj = context->DirectoryAt(localDir, LOCAL_PRIMODAL_TID);

            if (obj==NULL) // NULLOBJECT)
            {
                // char msg2[256];
                char *msg2=new char[256];
                SNPRINTF(msg2, 256, "%.16s/routine/BsfAttachToTID(), error 3: no TID given, nor %s defined in .local", DLLNAME, LOCAL_PRIMODAL_TID);
                context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg2));
                delete[] msg2;

                delete[] msg;
                return FALSE;
            }

            char *toTID=(char *) context->CString(context->ObjectToString(obj));
            bUsingPrimodialTID=true;

            unsigned long attachToTID=0;

            sscanf(toTID, "%lu", &attachToTID);    // get integer value from string

            // now try to attach
                      // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error,attach2tid};
            STRUCT_PARAM param2={tid, 0, 0, (void *)context->threadContext->instance, 0, 0, 0,
                (
                #ifdef UNIX
                    pthread_t
                #else
                    TID
                #endif
                )
                attachToTID};

            RgfAcquireLock();
            environmentAttachTo(&param2, TRUE); // attach to Java, keep attached

            param=param2;   // copy param2 fields to param to let the checking go on

    #ifdef DEBUG_JAVA_ATTACH_DETACH
        fprintf(stderr, "---> BSFJavaException() // 2 --> used primodal TID! param.error=[%d], param2.error=[%d]\n", param.error, param2.error); fflush(stderr);
    #endif
        }
#endif



    // - param->error:  -1 ... Attach-error, attachResult holds Java error code, raise an exception in caller
    //                  -2 ... no rajo found, raise an exception in caller
    //                  -3 ... cannot safely use the given RAJO in TID, as actual RAJO is being used in multiple Rexx threads
    //                  -4 ... cannot attach to target TID
    //                 -99 ... no JVM available !
    if (param.error!= 0)    // problem attaching, cannot proceed
    {
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined,
#ifdef RGF_ATTACH_NEW
                                 createAttachErrorMessageRSONew
#else
                                 createAttachErrorMessageRSO
#endif
                                 (context, "routine/BsfJavaException(), error 3", &param)
                                 );
#ifndef RGF_ATTACH_NEW
        if (param.error != -1)  // if attaching worked, we need to make sure that we detach as ell
        {
            environmentDetachFrom(&param, FALSE);   // detach from Java
        }
#endif
        RgfReleaseLock();
        delete[] msg;
        return FALSE;
    }

    RgfReleaseLock();


    if (flag==1 || flag==5)     // we cannot throw an exception or fatal error, report as a condition
    {
        // pending exception in Java, if so report it to Rexx !
        if ( param.env->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
        {
            RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
                 1,         // RexxCallContext
                 context,
                 "%.16s/routine/BsfJavaException(), error 4: unhandled Java exception pending: [%s], cannot throw a new Java exception or a fatal error");
/*
                // get Java Throwable toString() as a RexxString; will clear the pending Java exception
            RexxStringObject rso=RgfGetJavaThrowableAsString (param.env, context->threadContext);

            // create error message for Rexx
            size_t sz=context->StringLength(rso);   // get needed string length, default msg buffer is 4 KB
            if (sz>4000)
            {
                char * tmp=(char *) RexxAllocateMemory(sz+256);
                SNPRINTF( tmp, sz+256, "%.16s/routine/BsfJavaException(), error 4: unhandled Java exception pending: [%s], cannot throw a new Java exception or a fatal error", DLLNAME, context->StringData(rso));
                rso=context->String(tmp);           // turn into a RexxString
                RexxFreeMemory(tmp);
            }
            else
            {
                SNPRINTF( msg, 4096, "%.16s/routine/BsfJavaException(), error 4: unhandled Java exception pending: [%s], cannot throw a new Java exception or a fatal error", DLLNAME, context->StringData(rso));
                rso=context->String(msg);           // turn into a RexxString
            }

            context->SetContextVariable(BSF_ERROR_STRING, rso);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, rso);
*/
            RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
            environmentDetachFromNew(&param);
#else
            environmentDetachFrom(&param, FALSE);
#endif

            RgfReleaseLock();

            delete[] msg;
            return context->False();  // return prematurely
        }
    }

    // 1=throw,throwableObj, 2=check, 3=clear, 4=object/occurred, 5=fatalError, msg

    RexxObjectPtr result_Obj=NULL;
    if (flag==1)        // throw a Java exception
    {
        // get bean from the Java side ----------------------------------------------------------------
        jstring j_beanNameOrMsg=param.env->NewStringUTF(c_beanNameOrMsg);
        jobject jobj = param.env->CallObjectMethod(param.rajo,
                                                   defaultJVM->mid_RexxAndJava_lookupBean4JNI,
                                                   j_beanNameOrMsg
                                                   );
        param.env->DeleteLocalRef(j_beanNameOrMsg);

        if (jobj==NULL)     // no bean found by the given beanName
        {
            SNPRINTF( msg, 4096, "%.16s/routine/BsfJavaException(), error 5: supplied argument (Java Throwable object) with the beanName=[%s] cannot be found in the BSFRegistry)", DLLNAME, c_beanNameOrMsg);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            result_Obj=context->False();
        }
        else if (param.env->IsInstanceOf(jobj, defaultJVM->clz_Throwable)==JNI_FALSE)   // not a Throwable object !
        {
            SNPRINTF( msg, 4096, "%.16s/routine/BsfJavaException(), error 6: supplied argument with the beanName=[%s] is not a sbuclass of 'java.lang.Throwable'", DLLNAME, c_beanNameOrMsg);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            result_Obj=context->False();
            param.env->DeleteLocalRef(jobj);
        }
        else
        {
            param.env->Throw((jthrowable) jobj);     // now raise the exception in Java
            result_Obj=context->True();
        }
    }

    else if (flag==2)                // check for pending Java exception
    {
        result_Obj= (param.env->ExceptionCheck()==TRUE ? context->True() : context->False() );
    }

    else if (flag==3)               // clear pending Java exception
    {
        param.env->ExceptionClear();
    }

        // the following is only useful for testing, whether one own Java exception has been
        // raised indeed, allowing to access the Throwable information
    else if (flag==4)               // get and return an existing Java Throwable as a BSF reference, or .nil
    {
        jthrowable jo=param.env->ExceptionOccurred();
        if (jo==NULL)               // no pending Java exception, hence no Throwable object
        {
            result_Obj=context->Nil();
        }
        else    // create a bean of the Java object; if .BSF is available, then return a bsf.wrap() of it
        {
            jstring j_beanName = (jstring) param.env->CallObjectMethod(param.rajo,
                                               defaultJVM->mid_RexxAndJava_registerBean4JNI,
                                               jo);

            // get char* beanName, do a bsf.wrap() if available; return that result
            const char * c_beanName = param.env->GetStringUTFChars(j_beanName, JNI_FALSE);
            RexxStringObject r_beanName=context->String(c_beanName);    // create Rexx string
            param.env->ReleaseStringUTFChars(j_beanName, c_beanName);         // release

            param.env->DeleteLocalRef(j_beanName);

            if (bsfClz!=context->Nil())  // class found, do a bsf.wrap() on the beanName
            {
                result_Obj=context->SendMessage1(bsfClz, "BSF.WRAP", r_beanName);  // run bsf.wrap(), may return a BSF_PROXY object
            }
            else    // just return the supplied beanName (a String)
            {
                result_Obj=r_beanName;
            }
        }
        param.env->DeleteLocalRef(jo);
    }

    else if (flag==5)               // raise a fatalError in Java
    {
        param.env->FatalError(c_beanNameOrMsg);
        result_Obj=context->True();
    }

    RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
            environmentDetachFromNew(&param);
#else
    environmentDetachFrom(&param, FALSE);   // detach from Java
#endif
    RgfReleaseLock();
    delete[] msg;
    return result_Obj;
}



#if defined (CONFIG_MAKE_OREXX_REGISTRY_ACCESSIBLE)

    // rgf, 2012-06-06: routine to help debugging, in case a need arises; hence only compiled, if flag set
    //                  returns a directory containing the available ooRexx registry objects

    RexxRoutine0(RexxObjectPtr, BsfGetRegistryObjects)
    {
        RexxDirectoryObject rdo=context->NewDirectory();    // create new directory
        context->DirectoryPut(rdo, OREXX_REGISTRY, "OREXX_REGISTRY");

    #if defined (USE_OREXX_REGISTRY_PACKAGE)
        context->DirectoryPut(rdo, OREXX_REGISTRY_PACKAGE, "OREXX_REGISTRY_PACKAGE");
    #endif

        context->DirectoryPut(rdo, OREXX_REGISTRY_REFCOUNTER, "OREXX_REGISTRY_REFCOUNTER");

    #ifdef USE_RII2OID_RELATION
        context->DirectoryPut(rdo, OREXX_RII2_OID_RELATION, "OREXX_RII2_OID_RELATION");
    #endif

       return rdo;
    }
#endif




    // ---rgf, 2015-06-04 - 2015-06-07: allow to get, set and drop context variables, usage
    //                    allows to get, set and drop context variables in caller, usage
    //
    //                     BsfContextVariables( [get [, name]] | set, nameOrDir[, value]] | drop, nameOrDir[, value]])
    //                           ... optional, return directory of all context variables
    //
    //                     "g"   ... "get", return value of context variable "name";
    //                               if optional "name" is omitted, return directory of all context variables
    //
    //                     "s"   ... "set", set context variable
    //                                      "nameOrDir" ... if string, then "newValue" must be given
    //                                                      else must be a .Directory or .StringTable (anticipating ooRexx 5.0)
    //
    //                     "d"   ... "drop", drop context variable
    //                                      "nameOrDir" ... if string, then drop that variable
    //                                                      else must be a .Directory or .StringTable (anticipating ooRexx 5.0)

RexxRoutine3( RexxObjectPtr, BsfContextVariables, OPTIONAL_CSTRING, argSwitch, OPTIONAL_RexxObjectPtr, nameOrDir, OPTIONAL_RexxObjectPtr, value)    // 2015-06-07
{

#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: BsfContextVariables() 1 ...\n");
    fflush(stderr);
#endif

    char flag=0;
    if (argSwitch==NULL || argSwitch[0]=='g' || argSwitch[0]=='G')  // G[ET]
    {
        flag='G';
    }
    else if (argSwitch[0]=='s' || argSwitch[0]=='S')
    {
        flag='S';
    }
    else if (argSwitch[0]=='d' || argSwitch[0]=='D')
    {
        flag='D';
    }

    if (flag==0)    // no valid flag supplied, raise condition
    {
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/BsfContextVariables(), error 1: first argument ('flag'), illegal argument value '%.256s' (valid: 'G[et]', 'S[et]', 'D[rop]')", DLLNAME, argSwitch);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return context->Nil();
    }

    logical_t isArg2aString= ( nameOrDir==NULL ? false : context->IsString(nameOrDir) );

    if (argumentExists(3))  // single SET variable
    {
        if ( flag!='S' )    // third argument only exists for SET operation
        {
            char *msg=new char[1024];
            SNPRINTF( msg, 1024, "%.16s/routine/BsfContextVariables(), error 2: third argument only allowed for flag 'S[et]', supplied flag='[%.256s]'", DLLNAME, argSwitch);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
            return context->Nil();
        }

        if (!isArg2aString) // second argument must be a string for the variable name
        {
           char *msg=new char[1024];
           SNPRINTF( msg, 1024, "%.16s/routine/BsfContextVariables(), error 3: 'S[et]' usage, third argument only allowed, if second argument (variable's name) is a string denoting the variable name", DLLNAME);
           context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
           delete[] msg;
           return context->Nil();
        }

        // set single variable
        context->SetContextVariable(context->CString((RexxStringObject) nameOrDir), value);
        return NULL; // NULLOBJECT
    }

    // only a maximum of two arguments left
    if (argumentOmitted(2))     // only possible for 'G[ETall]'
    {
        if (flag != 'G')
        {
            char *msg=new char[1024];
            SNPRINTF( msg, 1024, "%.16s/routine/BsfContextVariables(), error 4: [%.256s] usage, second argument missing", DLLNAME, argSwitch);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
            return context->Nil();
        }

        return context->GetAllContextVariables();   // return all currently defined variables
    }

    //
    if (flag=='G')           // get single variable name
    {
        if (!isArg2aString)
        {
            char *msg=new char[1024];
            SNPRINTF( msg, 1024, "%.16s/routine/BsfContextVariables(), error 5: 'G[et]' usage, second argument must be a string", DLLNAME);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
            return context->Nil();
        }
            // fetch variable value
        RexxObjectPtr rop=context->GetContextVariable(context->CString((RexxStringObject) nameOrDir));
        if (rop==NULL)  // variable name not found, create error hinting at VAR()-, VALLUE()-BIFs
        {
            char *msg=new char[1024];
            SNPRINTF( msg, 1024, "%.16s/routine/BsfContextVariables(), error 6: 'G[et]' usage, variable [%.256s] not defined, use VAR()-BIF (built-in-function) to test existence of variable beforehand", DLLNAME, context->ObjectToStringValue(nameOrDir));
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
            return context->Nil();
        }
        return rop;

    }

    if (flag=='D' && isArg2aString) // drop variable by name
    {
        context->DropContextVariable(context->CString((RexxStringObject) nameOrDir));   // drop variable
        return NULL; // NULLOBJECT
    }

        // if not a string, it must be either a .Directory, or starting with ooRexx 5.0 a .StringTable
    if (! (context->IsOfType(nameOrDir, "Directory") || context->IsOfType(nameOrDir, "StringTable")) )
    {
        char *msg=new char[1024];

        SNPRINTF( msg, 1024, "%.16s/routine/BsfContextVariables(), error 7: [%.256s] usage, argument # 2 is not of type 'Directory', neither of type 'StringTable'", DLLNAME, argSwitch);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return context->Nil();
    }

    // use a supplier to {Set|Drop}ContextVariable(...)
    RexxSupplierObject rxSupplier=(RexxSupplierObject) context->SendMessage0(nameOrDir,"SUPPLIER");
    while (context->SupplierAvailable(rxSupplier))
    {
        if (flag=='S')
        {
            context->SetContextVariable(
                context->CString((RexxStringObject) context->SupplierIndex(rxSupplier)),    // always a string
                context->SupplierItem(rxSupplier)
                );
        }
        else    // dropping mode
        {
            context->DropContextVariable(context->CString((RexxStringObject) context->SupplierIndex(rxSupplier)));
        }

        context->SupplierNext(rxSupplier);
    }

    return NULL; // NULLOBJECT
}





// <---------------------------------



/* Original purpose:

 * This will be called from Java at initialization time and makes sure
 * that the external Rexx functions named &quot;BsfInvokedBy()&quot; and
 * &quot;BSF()&quot; will get
 * registered. This way, Rexx programs being invoked from the Java side
 * have &quot;BSF&quot; available already (no need to register this function by
 * called Rexx program). Changed to "jint" on 2001-05-25.
 */
//    JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRegisterBSF
//                       (JNIEnv *env, jobject obj)

/* 2012-02-06, rgf
 * Setup C++ structures to allow to refer to the JVM, invoked from the Java side before a
 * Rexx interpreter instance gets created; should get renamed to void jniSetupNativeJVMPointer()
*/
    JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniSetupNativeJVMPointer
                       (JNIEnv *env, jobject obj)

     {
#if defined( RGF_INFO ) || defined (RGF_INFO_1)  || defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY) // || defined (RGF_BIT)

    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            tid=RgfGetTID();   // get current TID

         // char title[512]="";
         char *title=new char[512];
         SNPRINTF( title, 512, "*** RGF_INFO: 1 ..._jniSetupNativeJVMPointer(): tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], bsfInvokedBy=[%d], pRoot_TID2NODE=[%p], about to setup stuff ...",
                        (unsigned long) tid, currentJVM, defaultJVM->jvm, bsfInvokedBy, pRoot_TID2NODE);

         fprintf(stderr, "%s\n", title);
         fflush(stderr);

    #ifdef RGF_BIT
         JNU_MessageBox(env, title);
    #endif
         delete [] title;
#endif

        RgfAcquireLock2(RII_creation_lock);      // 2012-12-30, rgf: lock access to list of RexxInstance instances to this thread

        if (currentJVM==NULL)       // invoked by Java, save JVM-pointer
        {
           env->GetJavaVM( & currentJVM );   // get and save JVM-pointer
#if defined(RGF_JNI_RII)
    fprintf(stderr, "---> ---> ---> _jniSetupNativeJVMPointer, currentJVM=NULL, now=[%p]\n", currentJVM);fflush(stderr);
#endif
           init_STRUCT_JVM(env, defaultJVM, currentJVM);
#if defined(RGF_JNI_RII)
   fprintf(stderr, "<--- <--- <--- _jniSetupNativeJVMPointer, currentJVM=NULL, AFTER init_STRUCT_JVM\n", currentJVM);fflush(stderr);
#endif
           bsfInvokedBy=1;     // invoked by Java
        }

        // save and initialize JVM related data, just use supplied env
        if (defaultJVM->jvm==NULL)
        {
            JavaVM *tmpJVM;
            env->GetJavaVM( & tmpJVM );
            init_STRUCT_JVM(env, defaultJVM, tmpJVM);    // 20091004, initialize default structure

#if defined(RGF_JNI_RII)
   fprintf(stderr, "===> ===> ===> _jniSetupNativeJVMPointer, defaultJVM=NULL, now=[%p]\n", defaultJVM);fflush(stderr);
#endif

#if defined( RGF_INFO_1 )  || defined (RGF_JNI)  // || defined (RGF_BIT)
    fprintf(stderr, "*** *** RGF_INFO_1: _jniSetupNativeJVMPointer(): 2 tid=[%lu], AFTER init_STRUCT_JVM(...), about to return 1...\n",
                                       (unsigned long) tid);
    fflush(stderr);

    dumpRAJO    ("_jniSetupNativeJVMPointer():");     // what's the RAJO list as of now ?
    dumpTID2NODE("_jniSetupNativeJVMPointer():");
#endif
        }

        RgfReleaseLock2(RII_creation_lock);      // 2012-12-30, rgf: unlock

     }


/*
 * This may be called from Java, pertains to Object Rexx only (as of 2003-01-04);
 * should wait until all cleanup within Object Rexx has been done. ---rgf, 2002-12-07, 2003-01-04
 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxWaitForTermination
  (JNIEnv *env, jobject obj)
     {

#if defined (RGF_INFO)  || defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "*** RGF_INFO: ..._jniRexxWaitForTermination() 1 ... \n");
    fflush(stderr);
#endif

#ifdef DEBUG        // ---rgf, 2003-04-30
     fprintf(stderr, "..._jniRexxWaitForTermination(), just arrived\n");
#endif


#if defined (USE_OREXX) || defined( USE_REXXTRANS )
    #ifdef DEBUG2        // ---rgf, 2003-04-30
        fprintf(stderr, "    ...just about to call RexxWaitForTermination()...\n");
    #endif

        RexxWaitForTermination();       // wait for Object Rexx to re-synch ... (2003-02-25, ---rgf: doesn't really have any effect
#endif


#if defined (DEBUG2)   || defined (RGF_JNI)       // ---rgf, 2003-04-30
    fprintf(stderr, "..._jniRexxWaitForTermination(): about to leave...\n\n");
#endif

        return (jint) 1;               // just return something

     }
  ;


/*
 * This is a pass-thru for RexxDidRexxTerminate(), returns 0 if Object Rexx still runs,
 * 1 if Object Rexx has terminated, ---rgf, 2002-12-13
 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxDidRexxTerminate
                       (JNIEnv *env, jobject obj)
     {

#if defined (RGF_INFO)  || defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "*** RGF_INFO: ..._jniRexxDidRexxTerminate() 1 ... \n");
    fflush(stderr);
#endif

#ifdef DEBUG2        // ---rgf, 2003-04-30
    fprintf(stderr, "..._jniRexxDidRexxTerminate(), just arrived\n");
#endif

#if defined (USE_OREXX) || defined( USE_REXXTRANS )  || defined (RGF_JNI)
    #if defined (DEBUG2) || defined (RGF_JNI)      // ---rgf, 2003-04-30
        fprintf(stderr, "    ...about to call and return ..._jniRexxDidRexxTerminate() ...\n\n");
    #endif

        return (jint) RexxDidRexxTerminate();       // ask Object Rexx, if it is done

#else
    #if defined (DEBUG2)  || defined (RGF_JNI)       // ---rgf, 2003-04-30
        fprintf(stderr, "..._jniRexxDidRexxTerminate(), about to leave...\n\n");
    #endif

        return (jint) 1;        // default for non-Object Rexx is the integer value '1' for 'true'
#endif
     }
  ;


// -----------> Anfang
/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniRexxGetTID
 * Signature: ()Ljava/lang/String;

   Purpose:   return BsfGetTID()-value as a string to Java
 */
JNIEXPORT jstring JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxGetTID
  (JNIEnv *env, jobject obj)
{
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();

#if defined (RGF_INFO)  || defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "*** RGF_INFO: ..._jniRexxGetTID() 1, tid=[%lu] ...\n", (unsigned long) tid);
    fflush(stderr);
#endif

   // char strTid[32]="";         // define a string long enough to receive an integer
   // sprintf(strTid, "%lu%c", ((unsigned long) tid), 0);  // create decimal number
   // SNPRINTF( strTid, 32, "%lu%c", ((unsigned long) tid), 0);  // create decimal number

   char *strTid=new char[RGF_TID_STRING_WIDTH];
   SNPRINTF( strTid, RGF_TID_STRING_WIDTH, "%lu%c", ((unsigned long) tid), 0);  // create decimal number

#if defined (RGF_INFO)  || defined (RGF_JNI)
    fprintf(stderr, "*** RGF_INFO: ..._jniRexxGetTID() 2, tid=[%lu], about to return ...\n", (unsigned long) tid);
    fflush(stderr);
#endif

            // create Java string from the C string and return it
   jstring jstr=env->NewStringUTF( strTid );

   delete[] strTid;
   return jstr;
}


/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniRexxSetHalt
 * Signature: (Ljava/lang/String;)I

   Purpose:   allow halting/terminating an ooRexx thread or an ooRexx interpreter instance
              with all its threads; return whatever we get from ooRexx
 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxSetHalt
  (JNIEnv *env, jobject obj, jstring jstrTid)
{


#if defined (RGF_INFO)  || defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)

    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            thisTid=RgfGetTID();


    fprintf(stderr, "*** RGF_INFO: ..._jniRexxSetHalt() 1, thisTid=[%lu], just arrived ...\n",
                    (unsigned long) thisTid, (unsigned long) thisTid);
    fflush(stderr);
#endif


    unsigned long tid=0L;

    const char * tmpStr = env->GetStringUTFChars(jstrTid, JNI_FALSE);
    sscanf(tmpStr, "%lu", &tid);  // get decimal number

    env->ReleaseStringUTFChars(jstrTid, tmpStr);


    // int rc=(int) RexxSetHalt((PID) GetCurrentProcessId(), (TID) GetCurrentThreadId());

#if defined (RGF_INFO)  || defined (RGF_JNI)
    fprintf(stderr, "*** RGF_INFO: ..._jniRexxSetHalt() 2, thisTid=[%lu], halted tid=[%lu], about to carry out & return ...\n",
                    (unsigned long) thisTid, (unsigned long) tid);
    fflush(stderr);
#endif


    return (jint) RexxSetHalt(
#ifdef WINDOWS
        GetCurrentProcessId(), (TID) tid
#else
        getpid(), (pthread_t) tid   // Unix
#endif
                             );


}
// <----------- Ende






/*
 * Class:     com_ibm_bsf_engines_rexx_RexxAndJava
 * Method:    jniInitialize4Rexx
 * Signature: ()I
 *
 * This is the target from the Java side. Will be called upon initializing
 * the JVM from the Rexx side.
 *
 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniInitialize4Rexx

  (JNIEnv *env, jobject raj)
{
    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            tid=RgfGetTID();

    RgfAcquireLock2(RII_creation_lock);      // 2012-12-30, rgf: lock access to list of RexxInstance instances to this thread
// fprintf(stderr, "//// ---> _jniInitialize4Rexx, BEFOR init_STRUCT_JVM, about to return ...\n");fflush(stderr);

#if defined (RGF_INFO)  || defined (RGF_JNI)  || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "*** RGF_INFO: ..._jniInitialize4Rexx() 1, tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p] ... \n",
                       (unsigned long) tid, currentJVM, defaultJVM->jvm);
    fflush(stderr);
#endif

    if (currentJVM==NULL)   // invoked by Java, save JVM-pointer for later use
    {
#if defined (RGF_INFO)  || defined (RGF_JNI)  || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "*** RGF_INFO: ..._jniInitialize4Rexx() 1-1, tid=[%lu], currentJVM==NULL ... \n", (unsigned long) tid);fflush(stderr);
#endif
        env->GetJavaVM( & currentJVM );
        bsfInvokedBy=2;     // Java invoked by Rexx !

        // save and initialize JVM related data
fprintf(stderr, "//// ---> _jniInitialize4Rexx, #1 BEFORE init_STRUCT_JVM ...\n");fflush(stderr);
        init_STRUCT_JVM(env, defaultJVM, currentJVM);    // 20091003, initialize default structure
fprintf(stderr, "//// ---> _jniInitialize4Rexx, #1 AFTER init_STRUCT_JVM, about to return ...\n");fflush(stderr);
    }
//    else if (defaultJVM==NULL)  // rgf, 2011-03-18
    else if (defaultJVM->jvm==NULL)
    {
#if defined (RGF_INFO)  || defined (RGF_JNI)  || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "*** RGF_INFO: ..._jniInitialize4Rexx() 1-1, tid=[%lu], defaultJVM->jvm==NULL ... \n",(unsigned long) tid);fflush(stderr);
#endif
        // save and initialize JVM related data
#if defined (RGF_INFO)  || defined (RGF_JNI)  || defined (DEBUG_JNI_ENTRY)
   fprintf(stderr, "//// ---> _jniInitialize4Rexx, #2 BEFORE init_STRUCT_JVM ...\n");fflush(stderr);
#endif

        init_STRUCT_JVM(env, defaultJVM, currentJVM);    // 20091003, initialize default structure

#if defined (RGF_INFO)  || defined (RGF_JNI)  || defined (DEBUG_JNI_ENTRY)
   fprintf(stderr, "//// ---> _jniInitialize4Rexx, #2 AFTER init_STRUCT_JVM, about to return ...\n");fflush(stderr);
#endif
    }


    // 20091003, save primodal_rajo as a global reference
    defaultJVM->primodal_rajo=env->NewGlobalRef(raj);

#if defined (RGF_INFO)  || defined (RGF_JNI)
    fprintf(stderr, "*** RGF_INFO: ..._jniInitialize4Rexx() 2, tid=[%lu], rajo=[%p], primodal_rajo=[%p], pRoot_RAJO=[%p], pRoot_TID2NODE=[%p], defaultJVM->jvm=[%p] ... \n",
                       (unsigned long) tid,
                        raj,
                        defaultJVM->primodal_rajo,
                        pRoot_RAJO,
                        pRoot_TID2NODE,
                        defaultJVM->jvm
                       );
    fflush(stderr);
#endif

        // 20091005: this is loaded via Rexx, keep this
    JNIEnv *daemEnv;
    RgfAcquireLock();
    int res=defaultJVM->jvm->AttachCurrentThreadAsDaemon((void **) &daemEnv, (void *) &defaultJavaVMAttachArgs);
    RgfReleaseLock();

/*
    // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error};
    STRUCT_PARAM param={tid, daemEnv, defaultJVM->primodal_rajo, NULL, 0, 0, 0, 0};
#if defined (RGF_INFO)  || defined (RGF_JNI)
    dumpPARAM("... _jniInitialize4Rexx #1, after initializing param-structure", &param);
#endif

    if (pRoot_RII!=NULL && pRoot_RII->next==NULL)   // if only a single RIID available, then use that one
    {
        param.riid=(void *)pRoot_RII->instance;
    }

    RgfAcquireLock();

// TODO: rgf, 20140517, add #ifdef RGF_ATTACH_NEW !!

    environmentAttachTo(&param, TRUE);      // attach to Java, force staying around after next environmentDetachFrom() !

#if defined (RGF_INFO)  || defined (RGF_JNI)
    fprintf(stderr, "... _jniInitialize4Rexx #2 pre: defaultJVM->jvm=[%p]\n", defaultJVM->jvm);fflush(stderr);
    dumpPARAM("... _jniInitialize4Rexx #2 (after environmentAttachTo(&param, TRUE))", &param);
#endif

    environmentDetachFrom(&param, FALSE);   // don't close group
#if defined (RGF_INFO)  || defined (RGF_JNI)
    fprintf(stderr, "... _jniInitialize4Rexx #3 pre: defaultJVM->jvm=[%p]\n", defaultJVM->jvm);fflush(stderr);
    dumpPARAM("... _jniInitialize4Rexx #3 (after environmentDetachFrom(&param, FALSE))", &param);
#endif

    RgfReleaseLock();
*/


#if defined (RGF_INFO)  || defined (RGF_JNI)
    fprintf(stderr, "*** RGF_INFO: ..._jniInitialize4Rexx() 3, tid=[%lu], primodal_rajo=[%p], pRoot_RAJO=[%p], pRoot_TID2NODE=[%p] ... \n",
                       (unsigned long) tid,
                        defaultJVM->primodal_rajo,
                        pRoot_RAJO,
                        pRoot_TID2NODE
                       );
    fflush(stderr);
#endif

#if defined ( DEBUG_SHOW_STRUCTURES )
    dumpPARAM   ("..._jniInitialize4Rexx(), after setup & environmentDetachFrom(): ", &param);     // what's the RAJO list as of now ?
    dumpRAJO    ("..._jniInitialize4Rexx(), after setup & environmentDetachFrom(): ");     // what's the RAJO list as of now ?
    dumpTID2NODE("..._jniInitialize4Rexx(), after setup & environmentDetachFrom(): ");
#endif

#if defined (DEBUG2)  || defined (RGF_JNI)       // ---rgf, 2003-04-30
    // zeigeGlobs();    // show values for global variables
    fprintf(stderr, "<-- ..._jniInitialize4Rexx() 3, tid=[%lu], about to leave...\n\n",
            (unsigned long) tid);
    fflush(stderr);
#endif


    RgfReleaseLock2(RII_creation_lock);      // 2012-12-30, rgf: unlock

    return (jint) 1;              // just return something which can be thought of being true
}



/*
 * This will be called from Java to invoke the Rexx interpreter. "source" contains
 * the Rexx program, "array" the arguments meant for the Rexx program.
 */

/*
JNIEXPORT jstring JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxStart
    ( JNIEnv         *env,
      jobject         obj,
      jstring         scriptSourceLocation,
      jstring         source,
      jobjectArray    array)
{
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();   // get current TID

#if defined (RGF_INFO) || defined (RGF_ATTACH) || defined (RGF_DETACH) || defined (DEBUG40)  || defined (RGF_JNI)   // || defined (RGF_BIT2)
     {
         fprintf(stderr, "*** RGF_INFO: jniRexxStart() 1, tid=[%lu] ... \n",
                         (unsigned long) tid);
         fflush(stderr);

         char msg[4096]="";  // define a buffer, big enough
         char title[512]="";
         sprintf(title, "...jniRexxStart(): 1 - tid=[%lu], pRoot_TID2JRST=[%p], about to setup and call Rexx...",
                        (unsigned long) tid, pRoot_TID2JRST);

         RgfAcquireLock();
         RgfShowTID2JRSTlist(title, msg);
         RgfReleaseLock();
    #ifdef RGF_BIT
         JNU_MessageBox(env, msg);
    #elif defined(RGF_BIT2)
         fprintf(stderr, "[%.256s]\n", msg);
         fflush(stderr);
    #endif
     }
#endif


    PSTRUCT_JRST a_JRST;
    a_JRST=RgfNewAttachToTID(tid); // try to attach: if possible, then this is an attempt for a nested invocation via a new BSFManager on the same thread


#if defined (RGF_BIT) || defined (RGF_BIT2)
     {
         char msg2[4096]="";  // define a buffer, big enough
         char title2  [512]="";
         sprintf(title2, "...jniRexxStart(), 1b (starting): tid=[%lu] --> a_JRST=[%p], pRoot_TID2JRST=[%p], pRoot_SAVE_NESTED_JRST=[%p]; AFTER ATTACHING\n",
                        (unsigned long) tid, a_JRST, pRoot_TID2JRST, pRoot_SAVE_NESTED_JRST);

         RgfAcquireLock();
         RgfShowTID2JRSTlist(title2, msg2);
         RgfReleaseLock();

#if defined (RGF_BIT2)
         fprintf(stderr, "[%.256s]\n", msg2);
         fflush(stderr);
#elif defined (RGF_BIT)
         JNU_MessageBox(env, msg2);
#endif
     }
#endif

        // nested invocation: this TID already is used by a previous jniRexxStart invocation!
    int bNestedInvocation=(a_JRST!=NULL);   // TRUE, if nested, FALSE else

        // we only allow nested invocations, if the previous invocation runs only on
        // one ooRexx thread; if more ooRexx threads are attached to the previous invocation,
        // then we create a Java exception and prematurely return
    if (bNestedInvocation) {

        RgfAcquireLock();
        int iCount=RgfCountUsageOfJRST(tid
#ifdef RGF_BIT
        , env
#endif
        );    // get number of threads using JRST of this tid

#if defined (RGF_BIT)
     {
         char title2  [512]="";
         sprintf(title2, "...jniRexxStart(), NESTED!  tid=[%lu], iCount=[%d], a_JRST=[%p], pRoot_TID2JRST=[%p], pRoot_SAVE_NESTED_JRST=[%p]\n",
                        (unsigned long) tid, iCount, a_JRST, pRoot_TID2JRST, pRoot_SAVE_NESTED_JRST);
         JNU_MessageBox(env, title2);
     }
#endif



#if defined (RGF_BIT) || defined (RGF_BIT2)
     {
         char msg2   [4096]="";
         char title2  [256]="";
         sprintf(title2, "...jniRexxStart(): BEGIN NESTED invocation! tid=[%lu], iCount=[%d], pRoot_TID2JRST=[%p], pRoot_SAVE_NESTED_JRST=[%p]\n",
                        (unsigned long) tid, iCount, pRoot_TID2JRST, pRoot_SAVE_NESTED_JRST);
         RgfShowTID2JRSTlist(title2, msg2);    // add TID2JRST list for debugging
      #if defined (RGF_BIT)
         JNU_MessageBox(env, msg2);
      #endif
     }
#endif

        if (iCount>1) {     // more than one ooRexx threads exploiting previous invocation, abort...
            char msg[4096]="";  // define a buffer, big enough
            char title[512]="";
            sprintf(title, "BSF4Rexx.cc: \n...jniRexxStart(): Illegal attempt to nest Rexx script invocations via BSF (more than one Rexx thread (%d) active on nested invocation: -> panic).", iCount);

            RgfShowTID2JRSTlist(title, msg);    // add TID2JRST list for debugging

            // JNU_ThrowByName(env, "org/apache/bsf/BSFException", msg);
            jthrowable rexxException=RgfCreateRexxException4Java(env, obj,
                                                                 NULL, // rtc,
                                                                 NULL, // conditionObject
                                                                 // NULL, // packageObject
                                                                 REASON_INVALID_ARGUMENT,   // reason
                                                                 msg   // message
                                                            );
            env->Throw(rexxException); // throw the exception in the JVM

    // fprintf(stderr, "--> ..._RexxAndJava...: 'BSF4Rexx.cc:...': msg=[%.256s]\n", msg);
    // fflush(stderr);

            RgfReleaseLock();

#if  defined (RGF_JNI)
            fprintf(stderr, "...jniRexxStart(): ERROR 1 - tid=[%lu], pRoot_TID2JRST=[%p], created Java Exception, returning NULL\n\tmsg=[%.256s]...",
                            tid, pRoot_TID2JRST, msg);
            fflush(stderr);

#endif
            return NULL;
        }

        RgfSaveTID2JRST(tid);       // save the nested TID2JRST node
        RgfReleaseLock();
    }

        // create the appropriate JRST node (including GlobalReferences etc.)
    a_JRST=RgfGetJRST_or_create_new (tid, env, obj); // 2008-07-23, rgf

#if defined (RGF_BIT) || defined (RGF_BIT2)
     {
         char msg2[4096]="";  // define a buffer, big enough
         char title2  [512]="";
         sprintf(title2, "...jniRexxStart(), 1c (starting): tid=[%lu] --> a_JRST=[%p], pRoot_TID2JRST=[%p], pRoot_SAVE_NESTED_JRST=[%p]; after NEW JRST CREATION\n",
                        (unsigned long) tid, a_JRST, pRoot_TID2JRST, pRoot_SAVE_NESTED_JRST);

         RgfAcquireLock();
         RgfShowTID2JRSTlist(title2, msg2);
         RgfReleaseLock();

#if defined (RGF_BIT2)
         fprintf(stderr, "[%.256s]\n", msg2);
         fflush(stderr);
#elif defined (RGF_BIT)
         JNU_MessageBox(env, msg2);
#endif
     }
#endif



    const char *      strLoc=NULL;
    char       *sourceStrLoc=NULL;

    if (scriptSourceLocation==NULL)
    {
        // char * tmpStr="n/a";
        char * tmpStr="a_BSF4Rexx_script_without_supplied_source_location";
        sourceStrLoc = (char *) RgfAllocateMemory(strlen(tmpStr)+1) ;
        strcpy(sourceStrLoc, tmpStr);
    }
    else   // make a copy of the Java string containing the Rexx script to be executed
    {
        strLoc       = env->GetStringUTFChars(scriptSourceLocation, 0);
        sourceStrLoc = (char *) RgfAllocateMemory(strlen(strLoc)+1) ;

        strcpy(sourceStrLoc, strLoc);
        env->ReleaseStringUTFChars(scriptSourceLocation, strLoc);
    }

    // make a copy of the Java string containing the Rexx script to be executed
    const char      *str = env->GetStringUTFChars(source, 0);
    char      *sourceStr = (char *) RgfAllocateMemory(strlen(str)+1) ;

    strcpy(sourceStr, str);
    env->ReleaseStringUTFChars(source, str);

    // create argument array for Rexx
    PRXSTRING argList = NULL;       // pointer to array of RXSTRINGS
    jsize   arrNum = 0;
    long    numArgs= 0l;

    if (array != NULL)              // an Object array available?
    {
        arrNum    =  env->GetArrayLength(array);
        numArgs   = (long) arrNum;
    }

    if (arrNum>0)
    {
        // allocate enough memory for argument array of RXSTRINGs
        argList = (PRXSTRING) RgfAllocateMemory( sizeof(RXSTRING) * arrNum );

        for (int i = 0; i < arrNum; ++i)
        {
            if ((env->GetObjectArrayElement(array, i))==NULL) // no argument
            {
                argList[i].strptr   =NULL;
                argList[i].strlength=0;
            }
            else        // a string argument in hand, give it to Rexx
            {
                jstring        javaStr = (jstring) env->GetObjectArrayElement(array, i);

                // this will convert the jstring to a local single-byte string using the current locale of Java
                // const char *tmpString=JNU_GetStringNativeChars(a_JRST, javaStr);                // rgf, 2006-01-28
                char *tmpString=JNU_GetStringNativeChars(a_JRST->penv, javaStr);              // rgf, 2009-04-20, 20090515
                size_t      len = strlen(tmpString);

                MAKERXSTRING( argList[i], tmpString, len); // save arg for Rexx, Rexx will free tmpString eventually

            }
        }
    }

    // Rexx program to execute
    RXSTRING        RXsourceStr[2];
    MAKERXSTRING   (RXsourceStr[0], sourceStr, strlen(sourceStr));
    PRXSTRING       Instore = &RXsourceStr[0];
    MAKERXSTRING   (RXsourceStr[1], NULL, 0);

    SHORT           ReturnCode;
    LONG            ReturnValue;

    // let Rexx allocate buffer, if needed for a result
    LONG            rc;
    RXSTRING        Result;
    MAKERXSTRING   (Result, NULL, 0);

    // PSZ             ProgramName = DLLNAME ; // rgf, 2008-08-06, not needed anymore

    // rgf, 2006-01-27: add Rexx exit handler to intercept error msg

    RGF_STDERR_BUFFER   *rsbarr[2];
    RGF_STDERR_BUFFER   rsb;

    INT tmpUInt[2]={ 0, 0 };                           // 0 count, next line at offset 0
    rsb.intBuffer =tmpUInt;

#ifdef RXSIO_USE_RGF_ALLOC
    fprintf(stderr, "...allocating RXSIO buffer, before RexxStart()...\n");
    fflush(stderr);

    rsb.strBuffer =(PCHAR) RgfAllocateMemory(2048);    // four lines à 512 bytes including \0
#else
    char tmpBuf[2048]="";           // accommodate four lines à 512 bytes including \0
    rsb.strBuffer = tmpBuf;         // assign buffer
#endif

#ifdef DEBUG2
    fprintf(stderr, "-> rsb.strBuffer=[%p] (before RexxStart())\n", rsb.strBuffer);
    fflush(stderr);
#endif



    strcpy(rsb.strBuffer, "..._jniRexxStart(): Rony was here...");

    rsbarr[0]=&rsb;            // interested data
    rsbarr[1]=NULL;            // second four bytes set to NULL

    rc=RexxRegisterExitExe(         RGF_RXSIO_EXIT_NAME_STRING,    // name of the exit function
                           (PFN)    RGF_RXSIO_EXIT_NAME,
//                           (PUCHAR) rsbarr               // user data for this exit instance
#ifdef RGF_ITS_OOREXX_4
                           (const char *)
#else
                           (PUCHAR)
#endif
                                    rsbarr               // user data for this exit instance
                          );          // address of the function


#ifdef DEBUG
    fprintf(stderr, "    ...after RexxRegisterExitExe(), tid=[%lu], rc=[%d].\n", (unsigned long) tid, rc);
    fflush(stderr);
#endif

    // now define the exit_list
    RXSYSEXIT exit_list[2];

    // set exit handler infos
    exit_list[0].sysexit_name=RGF_RXSIO_EXIT_NAME_STRING;      // C function name to invoke
    exit_list[0].sysexit_code=RXSIO;                           // exit function

    // indicate no more exit handlers
    exit_list[1].sysexit_name=NULL;
    exit_list[1].sysexit_code=RXENDLST;



         //start Rexx
         ReturnValue = RexxStart(
             (LONG)       arrNum,         // LONG         ArgCount

#ifdef RGF_ITS_OOREXX_4
             (PCONSTRXSTRING)  argList,   // PRXSTRING    ArgList   // ooRexx 4.x
#else
             (PRXSTRING)  argList,        // PRXSTRING    ArgList   // ooRexx 3.x
#endif
             (PSZ)        sourceStrLoc,   // PSZ          ProgramName
             (PRXSTRING)  Instore,        // PRXSTRING    Instore
             (PSZ)        "BSF",          // NULL,           // PSZ          EnvName
             (LONG)       RXFUNCTION,     // LONG         CallType
             (PRXSYSEXIT) exit_list,     // PRXSYSEXIT   Exits
             (PSHORT)     &ReturnCode,    // PUSHORT      ReturnCode
             (PRXSTRING)  &Result         // PRXSTRING    Result
             );


#if defined (RGF_INFO_1) || defined (RGF_BIT2)
         fprintf(stderr, "*** *** RGF_INFO_1: jniRexxStart() 2 - after RexxStart(): tid=[%lu], ReturnValue=[%ld] ============================\n",
                                  (unsigned long) tid, ReturnValue);

    #if defined (USE_OREXX) || defined( USE_REXXTRANS )
         fprintf(stderr, "*** *** RGF_INFO_1: jniRexxStart() 2b - after RexxStart(): tid=[%lu], RexxDidRexxTerminate()=[%d] ============================\n",
                                  (unsigned long) tid, (int) RexxDidRexxTerminate());
    #endif
         fflush(stderr);
#endif

#ifdef DEBUG2
    fprintf(stderr, "-> rsb.strBuffer=[%p] (after RexxStart() - 01)\n", rsb.strBuffer);
    fflush(stderr);
#endif



#if defined (USE_OREXX) || defined( USE_REXXTRANS ) || defined (RGF_BIT2)


// TODO: THINK! if nested invocation, then "RexxWaitForTermination()" may wait forever! :-(
//       possible strategies: do not wait for termination if nested,
//                            wait like 1/100 second if nested before returning
//

    if (!bNestedInvocation &&  !RexxDidRexxTerminate()) {      // ooRexx has not terminated yet

    #if defined ( RGF_INFO_1 ) || defined (RGF_BIT2)
             fprintf(stderr, "*** *** RGF_INFO_1: jniRexxStart() 2c - tid=[%lu], BEFORE RexxDidRexxTerminate()=[%d] ============================\n",
                                      (unsigned long) tid, (int) RexxDidRexxTerminate());
             fflush(stderr);
    #endif

        RexxWaitForTermination();       // wait for ooRexx to terminate (may have threads concurrently running)

    #if defined ( RGF_INFO_1 ) || defined (RGF_BIT2)
             fprintf(stderr, "*** *** RGF_INFO_1: jniRexxStart() 2d - tid=[%lu], AFTER  RexxDidRexxTerminate()=[%d] ============================\n",
                                      (unsigned long) tid, (int) RexxDidRexxTerminate());
             fflush(stderr);
    #endif
    }
#endif

#ifdef DEBUG2
    fprintf(stderr, "-> rsb.strBuffer=[%p] (after RexxStart() - 02)\n", rsb.strBuffer);
    fflush(stderr);
#endif




    // fflush(stdin); TODO: evaluate necessity, 2008-07-29
    fflush(stdout);
    fflush(stderr);

/ *
// rgf, 2009-04-02: removed to stop Object REXX, ooRexx 3.x from failure in system service,
//                  if a Rexx invocation invokes another Rexx program via Java;
//                  RMG answer to a question on the ooRexx developer list clarified
//                  that RexxDeregisterExit() would indeed deregister "globally"; not
//                  deregistering should not have a negative impact on any resources,
//                  hence removing the deregistering statement seems to be o.k.

    rc=RexxDeregisterExit( RGF_RXSIO_EXIT_NAME_STRING,  // name of the exit function
                           NULL // DLLNAME              // it's this DLL
                       );


#ifdef RGF_INFO_1
    fprintf(stderr, "*** *** RGF_INFO_1: jniRexxStart(), RexxDeregisterExit(), rc=[%d], RXEXIT_OK=[%d]) \n",
                                         rc, RXEXIT_OK);
    fflush(stderr);
#endif

* /


    if (arrNum>0)   // free allocated memory, which was needed for Rexx arguments
    {
        for (int i = 0; i < arrNum; ++i)        // loop over Rexx args
        {
            RgfFreeMemory ( argList[i].strptr );// free argument array RXSTRING buffers
        }
        RgfFreeMemory ( argList );              // free argument array RXSTRING buffer itself
    }

    RgfFreeMemory(sourceStr);       // ---rgf, 2003-04-14, free memory reserved for the Rexx source code
    RgfFreeMemory(sourceStrLoc);    // ---rgf, 2008-06-14


    // error occurred, fetch Rexx error message
    if (ReturnValue != 0l)     // error occurred, return null
    {
        // int i=0, j=0;
        int    i=0;
        size_t j=0;
        PCHAR threeLines[3];
        PCHAR msgBuffer=NULL;

#ifdef RGF_MUTEX4RXSIOTRC
    RgfAcquireLockRXSIO();
#endif
        if (rsb.intBuffer[0]>0)         // o.k., lines captured get the last three in order
        {
            {
                if (rsb.intBuffer[1]==0)                // next line to write to, so all four lines were used already
                {
                    threeLines[0]=rsb.strBuffer+ 512;       // 2nd line
                    threeLines[1]=rsb.strBuffer+1024;       // 3rd line
                    threeLines[2]=rsb.strBuffer+1536;       // 4th line
                }
                else if (rsb.intBuffer[1]==1)            // next line to write to
                {
                    threeLines[0]=rsb.strBuffer+1024;       // 2nd line
                    threeLines[1]=rsb.strBuffer+1536;       // 3rd line
                    threeLines[2]=rsb.strBuffer     ;       // 4th line
                }
                else if (rsb.intBuffer[1]==2)            // next line to write to
                {
                    threeLines[0]=rsb.strBuffer+1536;       // 2nd line
                    threeLines[1]=rsb.strBuffer     ;       // 3rd line
                    threeLines[2]=rsb.strBuffer+ 512;       // 4th line
                }
                else if (rsb.intBuffer[1]==3)            // next line to write to
                {
                    threeLines[0]=rsb.strBuffer     ;       // 2nd line
                    threeLines[1]=rsb.strBuffer+ 512;       // 3rd line
                    threeLines[2]=rsb.strBuffer+1024;       // 4th line
                }
            }

            // int iSize=0;                    // calc needed bytes for buffer
            // int iSizeArr[3];
            size_t iSize=0;                    // calc needed bytes for buffer
            size_t iSizeArr[3];

            for (i=0; i<3 && i<rsb.intBuffer[0]; i++)       // [0]...count, [1]...next write pos
            {
                iSizeArr[i]=strlen(threeLines[i]);
                iSize=iSize+iSizeArr[i]+1;
            }

            msgBuffer=(PCHAR) RgfAllocateMemory(iSize+1);   // account for trailing \0
            for (i=0, j=0; i<3 && i<rsb.intBuffer[0]; i++)  // [0]...count, [1]...next write pos
            {
                strncpy(msgBuffer+j, threeLines[i], iSizeArr[i]);
                j=j+iSizeArr[i];        // position to write a newline char
                msgBuffer[j]='\n';      // insert a newline character
                j++;                    // next position to copy to
            }
            msgBuffer[iSize-1]='\0';    // no pending \n character
        }
        else        // error condition, but no text captured in the RXSIO exit !
        {
            msgBuffer=(PCHAR) RgfAllocateMemory(1024);
            sprintf(msgBuffer, "..._jniRexxStart(): Rexx execution error has occurred (Rexx reported error # [%ld])!", (long) ReturnValue);
        }
#ifdef RGF_MUTEX4RXSIOTRC
    RgfReleaseLockRXSIO();
#endif

#if defined (RGF_INFO) || defined (RGF_ATTACH) || defined (RGF_DETACH)  // || defined (DEBUG40)
         fprintf(stderr, "*** *** RGF_INFO_1: jniRexxStart() 3 - tid=[%lu], a_JRST=[%p], pRoot_TID2JRST=[%p] === about to return with exception! ===\n",
                                  (unsigned long) tid, a_JRST, pRoot_TID2JRST );
         fflush(stderr);
#endif


        // throw Java exception
        // JNU_ThrowByName(env, "org/apache/bsf/BSFException", msgBuffer);
        jthrowable rexxException=RgfCreateRexxException4Java(env, obj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msgBuffer   // message
                                                        );
        env->Throw(rexxException); // throw the exception in the JVM

        RgfFreeMemory(msgBuffer);       // make sure memory is freed

        RgfAcquireLock();
        int tmpRes=RgfRemoveTID2JRST(a_JRST, TRUE); // make sure, JRST and all dependents get removed

#if defined (RGF_ATTACH) || defined (RGF_DETACH) || defined (RGF_BIT2)  // || defined (DEBUG40) // || defined (RGF_BIT)
       {
        char t2j_list[4096]="";  // define a buffer, big enough
        char title[512]="";
        sprintf(title, "tid=[%lu], pRoot_TID2JRST=[%p], regular end of function, ... EXCEPTION",
                       (unsigned long) tid, pRoot_TID2JRST);
        RgfShowTID2JRSTlist(title, t2j_list);

        fprintf(stderr, "*** *** RGF_INFO_1: jniRexxStart() 4a  - tid=[%lu], TID2JRST-list=[%.256s] ... EXCEPTION has occurred!\n",
                                 (unsigned long) tid, t2j_list);
        fflush(stderr);
    #ifdef RGF_BIT
         JNU_MessageBox(env, t2j_list);
    #endif
       }
#endif

        // relies that RgfRemoveTID2JRST(jrst,TRUE) was executed beforehand
        if (bNestedInvocation) {    // restore saved (uncover nested) TID2JRST
            RgfRestoreTID2JRST(tid);
            RgfNewDetach();         // make sure counter gets decreased (got increased inadvertently at the top)

#if defined ( RGF_BIT ) || defined (RGF_BIT2)
     {
         char msg2   [4096]="";
         char title2  [256]="";
         sprintf(title2, "...jniRexxStart(): EXECUTION ERROR in nested invocation! tid=[%lu], pRoot_TID2JRST=[%p], pRoot_SAVE_NESTED_JRST=[%p], RESTORED to PREVIOUS JRST\n",
                        (unsigned long) tid, pRoot_TID2JRST, pRoot_SAVE_NESTED_JRST);
         RgfShowTID2JRSTlist(title2, msg2);    // add TID2JRST list for debugging

#if defined(RGF_BIT2)
         fprintf(stderr, "\t[%.256s]\n", msg2);
         fflush(stderr);
#elif defined (RGF_BIT)
         JNU_MessageBox(env, msg2);
#endif

     }
#endif
        }


#if defined (RGF_ATTACH) || defined (RGF_DETACH) || defined (RGF_BIT2) || defined (RGF_JNI) // || defined (RGF_BIT)
       {
        char t2j_list[4096]="";  // define a buffer, big enough
        char title[512]="";

        sprintf(title, "tid=[%lu], pRoot_TID2JRST=[%p], Rexx execution error, about to throw Java exception...",
                        (unsigned long) tid, pRoot_TID2JRST);
        RgfShowTID2JRSTlist(title, t2j_list);

        fprintf(stderr, "*** *** RGF_INFO_1: jniRexxStart() 3b - tid=[%lu], TID2JRST-list=[%.256s] \n        === about to return with exception! ===\n",
                                 (unsigned long) tid, t2j_list);
        fflush(stderr);

    #ifdef RGF_BIT
         JNU_MessageBox(env, t2j_list);
    #endif
       }
#endif
       RgfReleaseLock();


#ifdef RXSIO_USE_RGF_ALLOC
        if (rc==RXEXIT_OK) {    // deregistering worked, save to free buffer
            fprintf(stderr, "... FREEING    RXSIO buffer, before RexxStart()...\n");
            fflush(stderr);

            RgfFreeMemory(rsb.strBuffer);   // four lines à 512 bytes including \0
        }
#endif


        return NULL;                    // return to Java
    }

#ifdef RXSIO_USE_RGF_ALLOC
    if (rc==RXEXIT_OK) {    // deregistering worked, save to free buffer
        fprintf(stderr, "... FREEING    RXSIO buffer, before RexxStart()...\n");
        fflush(stderr);

        RgfFreeMemory(rsb.strBuffer);   // four lines à 512 bytes including \0
    }
#endif

    // --- get result and return it to Java
    jstring jResult=NULL;           // define Java String to return
    if ( Result.strptr != NULL )    // a result was returned from Rexx
    {
        if (Result.strlength==0)   // empty string?
        {
            // jResult=JAVA_EMPTY_UTF_STRING;
            jResult=a_JRST->emptyUTFString;
        }
        else                       // valid non-empty string
        {
            // jResult=env->NewStringUTF( Result.strptr );
            jResult=a_JRST->penv->NewStringUTF( Result.strptr );
        }
    }

    RgfAcquireLock();
    int tmpRes=RgfRemoveTID2JRST(a_JRST, TRUE); // make sure, JRST and all dependents get removed
    RgfReleaseLock();

#if defined (RGF_ATTACH) || defined (RGF_DETACH) || defined (RGF_BIT2)  // || defined (RGF_BIT)
       {
        char t2j_list[4096]="";  // define a buffer, big enough
        char title[512]="";
        sprintf(title, "tid=[%lu], pRoot_TID2JRST=[%p], regular end of function, about to return a jResult [%p]...",
                       (unsigned long) tid, pRoot_TID2JRST, jResult);
        RgfShowTID2JRSTlist(title, t2j_list);

        fprintf(stderr, "*** *** RGF_INFO_1: jniRexxStart() 4b-1  - tid=[%lu], TID2JRST-list=[%.256s] \n        === about to return normally ===\n",
                                 (unsigned long) tid, t2j_list);
        fflush(stderr);
    #ifdef RGF_BIT
         JNU_MessageBox(env, t2j_list);
    #endif
       }
#endif

    // relies that RgfRemoveTID2JRST(jrst,TRUE) was executed beforehand
    if (bNestedInvocation) {    // restore saved (uncover nested) TID2JRST

#if defined (RGF_BIT2)
    fprintf(stderr, "... before RgfRestoreTID2JRST(tid): tid=[%lu]\n", (unsigned long) tid);
    fflush(stderr);
#endif
        RgfRestoreTID2JRST(tid);

#if defined (RGF_BIT2)
    fprintf(stderr, "... before RgfNewDetach(): tid=[%lu]\n", (unsigned long) tid);
    fflush(stderr);
#endif

        RgfNewDetach();         // make sure JRST counter gets decreased (got increased inadvertently at the top)

#if defined(RGF_BIT) || defined(RGF_BIT2)
     {
         char msg2   [4096]="";
         char title2  [256]="";


#if defined (RGF_BIT2)
    fprintf(stderr, "... before RgfShowTID2JRSTlist: tid=[%lu] #1\n", (unsigned long) tid);
    fflush(stderr);
#endif

         sprintf(title2, "...jniRexxStart(): END of nested invocation! tid=[%lu], pRoot_TID2JRST=[%p], pRoot_SAVE_NESTED_JRST=[%p]\n",
                        (unsigned long) tid, pRoot_TID2JRST, pRoot_SAVE_NESTED_JRST);


#if defined (RGF_BIT2)
    fprintf(stderr, "... before RgfShowTID2JRSTlist: tid=[%lu] #2\n", (unsigned long) tid);
    fflush(stderr);
#endif



        RgfShowTID2JRSTlist(title2, msg2);    // add TID2JRST list for debugging

        #ifdef RGF_BIT
         JNU_MessageBox(env, msg2);
        #else
          fprintf(stderr, "%.256s\n", msg2);
          fflush(stderr);
        #endif
     }
#endif

    }
// TODO: rgf, 20080903, moved to top    RgfReleaseLock();


#if defined (RGF_BIT) || defined (RGF_JNI)
     {
         char title2  [512]="";
         sprintf(title2, "...jniRexxStart(), 2 (leaving) : tid=[%lu], a_JRST=[%p], pRoot_TID2JRST=[%p], pRoot_SAVE_NESTED_JRST=[%p]\n",
                        (unsigned long) tid, a_JRST, pRoot_TID2JRST, pRoot_SAVE_NESTED_JRST);
         JNU_MessageBox(env, title2);
     }
#endif


#if defined (RGF_INFO) || defined (RGF_ATTACH) || defined (RGF_DETACH)  || defined (DEBUG40) || defined (RGF_JNI)
         fprintf(stderr, "*** *** RGF_INFO_1: jniRexxStart() 4b-2 - tid=[%lu], a_JRST=[%p], pRoot_TID2JRST=[%p], jresult=[%p] === about to return ========\n",
                                  (unsigned long) tid, a_JRST, pRoot_TID2JRST, jResult );
         fflush(stderr);
#endif
    return jResult;         // return a Java String
}
*/








/*  rgf, 2009-06-07: removed, not necessary anymore
        // ===>
        // ---rgf, 2006-01-27: use a RXSIO exit to capture the error-strings and return them in form of a Java exception
        // <===
// APIRET APIENTRY RGF_RXSIO_EXIT_NAME ( LONG exitNumber, LONG subFunction, PEXIT parmBlock )
LONG APIENTRY RGF_RXSIO_EXIT_NAME ( LONG exitNumber, LONG subFunction, PEXIT parmBlock )
{
#if defined (DEBUG) // || defined (RGF_BIT)
    fprintf(stderr, "\n---\ndebug - %.256s(): just arrived, exitNumber=[%d], subFunction=[%d], parmBlock=[%p]\n",
            RGF_RXSIO_EXIT_NAME_STRING,
            exitNumber, subFunction, parmBlock);
    fflush(stderr);
#endif

#ifdef RGF_MUTEX4RXSIOTRC
    RgfAcquireLockRXSIO();
#endif

        // define variables
    USHORT            query_flag=0;     // for RexxQueryExit(): RXEXIT_OK or RXEXIT_NOTREG
    RGF_STDERR_BUFFER *user_info[2];
    LONG              rc=0;

#if defined (DEBUG) // || defined (RGF_BIT)
         // fprintf(stderr, "\n---\ndebug - %.256s(): just arrived, exitNumber=[%d], subFunction=[%d], parmBlock=[%p]\n",
         //                 RGF_RXSIO_EXIT_NAME_STRING,
         //                 exitNumber, subFunction, parmBlock);
         // fflush(stderr);

         RXSIOTRC_PARM *tparm_rgf = ( RXSIOTRC_PARM * ) parmBlock;
         fprintf(stderr, "\n    --> ---> [%.256s]\n", tparm_rgf->rxsio_string.strptr);
         fflush(stderr);
#endif


    if (subFunction!=RXSIOTRC)      // only intercept trace/error output from Rexx
    {
#ifdef RGF_MUTEX4RXSIOTRC
        RgfReleaseLockRXSIO();
#endif
        return RXEXIT_NOT_HANDLED;  // Rexx will now write to stdout: instead of stderr: !
    }

        // save string in a round-robin fashion in the user data, first get it!
    rc=RexxQueryExit(RGF_RXSIO_EXIT_NAME_STRING,
                     NULL,                  // area anchor from Rexx
                     &query_flag,
#ifdef RGF_ITS_OOREXX_4
                     (char *)
#else
                     (PUCHAR)
#endif
                              user_info);

    RGF_STDERR_BUFFER   rsb = *(user_info[0]);      // get access to our data structure

    if (subFunction==RXSIOTRC)
    {
        RXSIOTRC_PARM *tparm = ( RXSIOTRC_PARM * ) parmBlock;

#if defined (DEBUG) // || defined (RGF_BIT)
    fprintf(stderr, "debug - RXIOTRC: %.256s(), rc=[%d]: \n    --> ---> [%.256s]\n", RGF_RXSIO_EXIT_NAME_STRING,
                    rc,
                    tparm->rxsio_string.strptr);
    fprintf(stderr, "===> iCount == rsb.intBuffer[0]=[%d] | iNext == rsb.intBuffer[1]=[%d]\n",
                    rsb.intBuffer[0], rsb.intBuffer[1]);
    fflush(stderr);
#endif

            // truncate lines that are too long (> 511 bytes)
       // int iLength=min(511, tparm->rxsio_string.strlength  );
       // min-function not available on Linux gcc, hence this way

        int iTmpLength=(int)tparm->rxsio_string.strlength;
        int iLength=511;
        if (iLength>iTmpLength)
            iLength=iTmpLength;

        int iStartPos=rsb.intBuffer[1]*512;         // write position in buffer

        strncpy( rsb.strBuffer+iStartPos , tparm->rxsio_string.strptr, iLength);

        PCHAR p=rsb.strBuffer+iStartPos+iLength;    // position on last character
        *p=0;                                       // set ending 0
        if (iLength==511)   // too long? if so replace last three chars with " ..." to indicate truncation (ellipsis)
        {
             *--p=('.'); *--p=('.'); *--p=('.');    // *--p=' '; // add an ellipsis if too long
        }

        rsb.intBuffer[0]++;     // increase counter
        if (rsb.intBuffer[0]>2000000)   // in the unlikely event that this exit is called 2,000,000 times, reset counter
        {
            rsb.intBuffer[0]=5;         // just needs to be larger than maximum number of available lines (= 4)
        }

        rsb.intBuffer[1]++;     // increase next write position
        if (rsb.intBuffer[1]>3)         // roll over
        {
            rsb.intBuffer[1]=0;         // next line will be written to start of buffer
        }
#if defined (DEBUG) // || defined (RGF_BIT)
    fprintf(stderr, "===> startPos=[%d], length=[%d]: [%.256s]\n\n", iStartPos, iLength, rsb.strBuffer+iStartPos);
#endif

    }

#ifdef RGF_MUTEX4RXSIOTRC
    RgfReleaseLockRXSIO();
#endif
    return RXEXIT_NOT_HANDLED;  // Rexx will now write to stdout: instead of stderr: !
}

*/




/*
#ifdef NIXI_RGF     // don't include
    // o.k. under Windows be verbose about process&thread de/creation ..., rgf, 2003-08-06
    #if defined (DEBUG_THREADS) && defined (WIN32)

        // We provide a DLL entry function. Look at the standard documentation
        BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD Reason, LPVOID reserved)
        {
           fprintf(stderr, "DllMain(): hinstDLL=[%p], ", hinstDLL);
           fprintf(stderr, "Reason=[%p], reserved=[%p]\n", Reason, reserved);

           // if (Reason == DLL_THREAD_ATTACH || Reason == DLL_THREAD_DETACH) {
           JNIEnv *env=NULL;       // to receive the thread specific env-pointer/TID
           if (Reason == DLL_THREAD_DETACH || Reason == DLL_PROCESS_DETACH)
           {
                        // get the JVM-pointer
                   JavaVM *vm= (globJvm==NULL ? globJavaVM : globJvm);    // get the JVM interface pointer
                       // get correct JNIEnv object for this thread !

                   rgfAttachCurrentThread(vm, env);
                   // fprintf(stderr, "DllMain(): Java-THREAD=[%p] <=== <=== <===\n", env);

#ifdef USE_DETACH_CURRENT_THREAD
               rgfDetachCurrentThread(vm, env);  // ---rgf, 2008-06-09, switched args!
#endif
           }

           switch (Reason) {
              case DLL_PROCESS_ATTACH:
                 fprintf(stderr, "    DllMain(): DLL_PROCESS_ATTACH <===\n");
                 // Two things to do
                 break;
              case DLL_PROCESS_DETACH:
                  fprintf(stderr, "    DllMain(): DLL_PROCESS_DETACH; TID=[%p] <===\n", env);
                  MT_Java_env_remove(env);
                  fprintf(stderr, "            did remove entry [%p] from HASHTABLE.\n", env);
                 break;
              case DLL_THREAD_ATTACH:
                  fprintf(stderr, "    DllMain(): DLL_THREAD_ATTACH  <===\n");
                 break;
              case DLL_THREAD_DETACH:
                  fprintf(stderr, "    DllMain(): DLL_THREAD_DETACH  TID=[%p] <===\n", env);
                  MT_Java_env_remove(env);
                  fprintf(stderr, "            did remove entry [%p] from HASHTABLE.\n", env);
                 break;
           }

           return(TRUE);
        }
    #endif
#endif
*/

    // now called (JNI_OnLoad was misspelled!), 2006-01-04, ---rgf // never called! 2003-07-24
    // Note: this is likely never called (depends on the class loader that loaded this library
    //       to be garbage collected; maybe in Webserver environments, who knows ;-) ...)
JNIEXPORT void JNICALL  JNI_OnUnload(JavaVM *vm, void *reserved)
     {

#if defined (RGF_INFO) || defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)  // || defined (DEBUG40)

    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            thisTid=RgfGetTID();

         fprintf(stderr, "*** RGF_INFO: JNI_OnUnLoad() 1, tid=[%lu] ... <== <=== <==== !!!\n", (unsigned long) thisTid);
         fflush(stderr);
#endif
     }



JNIEXPORT jint JNICALL  JNI_OnLoad  (JavaVM *vm, void *reserved)
     {

         // 2008-07-23: initialize data structure --->
#if defined ( RGF_INFO ) || defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)  // || defined (DEBUG40)
    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            thisTid=RgfGetTID();

         fprintf(stderr, "*** RGF_INFO: JNI_OnLoad() 1, tid=[%lu] ...\n", (unsigned long) thisTid);
         fflush(stderr);
#endif

         rgfInitLocks();        // Initialize critical section

#if defined ( DEBUG )
   fprintf(stderr, "---> returning a jint: [%x] [%d]\n", USE_DEFINED_JNI_VERSION, USE_DEFINED_JNI_VERSION);
#endif

         return (jint) USE_DEFINED_JNI_VERSION;
     }





// This function may be called from "JNI_OnLoad()" or/and from "bsfLoader()": makes sure that
// that initialization is carried out only once.
void RexxEntry rgfInitLocks()
{
#if defined (DEBUG2) || defined (DEBUG40)
    fprintf(stderr, "/// in rgfInitLocks(), About to initialize CRITICAL SECTIION ...\n");
    fflush(stderr);
#endif

    // initialize critical section semaphore
#ifdef WINDOWS
    #if defined (RGF_USE_MUTEX)
                // HANDLE WINAPI CreateMutex(
                //  __in          LPSECURITY_ATTRIBUTES lpMutexAttributes,
                //  __in          BOOL bInitialOwner,
                //  __in          LPCTSTR lpName
                // );
        JRST_lock     = CreateMutex(NULL, FALSE, NULL) ;
        RII_lock      = CreateMutex(NULL, FALSE, NULL)  ;
        REGISTRY_lock = CreateMutex(NULL, FALSE, NULL)  ;
        RII_creation_lock = CreateMutex(NULL, FALSE, NULL)  ;   // 20121230, rgf
    #else
        InitializeCriticalSection (&JRST_lock);
        InitializeCriticalSection (&RII_lock );
        InitializeCriticalSection (&REGISTRY_lock );
        InitializeCriticalSection (&RII_creation_lock );        // 20121230, rgf
    #endif

#elif defined UNIX
    pthread_mutex_init(&JRST_lock, NULL);
    pthread_mutex_init(&RII_lock , NULL);
    pthread_mutex_init(&REGISTRY_lock , NULL);
    pthread_mutex_init(&RII_creation_lock , NULL);              // 20121230, rgf
#endif

#if defined (DEBUG2) || defined (DEBUG40)
    // fprintf(stderr, " now: bInitialize=[%d]\n", bInitialize);
    fprintf(stderr, "/// in rgfInitLocks(), leaving ...\n");
    fflush(stderr);
#endif

}



    // destroy/free/close lock resources
void RexxEntry rgfDestroyLocks()
{
#if defined (DEBUG2) || defined (DEBUG40)
    fprintf(stderr, "/// in rgfDestroyLocks(), About to destroy/delete CRITICAL SECTIION ...\n");
    fflush(stderr);
#endif

    // destroy (uninitialize) critical section var
#ifdef WINDOWS
    #if defined (RGF_USE_MUTEX)
        CloseHandle(JRST_lock    );
        CloseHandle(RII_lock     );
        CloseHandle(REGISTRY_lock);
        CloseHandle(RII_creation_lock     );        // 20121230, rgf
    #else
        DeleteCriticalSection(&JRST_lock);
        DeleteCriticalSection(&RII_lock);
        DeleteCriticalSection(&REGISTRY_lock);
        DeleteCriticalSection(&RII_creation_lock);  // 20121230, rgf
    #endif
#elif defined UNIX
    pthread_mutex_destroy(&JRST_lock);
    pthread_mutex_destroy(&RII_lock);
    pthread_mutex_destroy(&REGISTRY_lock);
    pthread_mutex_destroy(&RII_creation_lock);      // 20121230, rgf
#endif

#ifdef DEBUG1
//     fprintf(stderr, " now: bDestroy=[%d]\n", bDestroy);
    fprintf(stderr, "/// in rgfDestroyLocks(), Leaving destroy/delete CRITICAL SECTIION ...\n");
    fflush(stderr);
#endif
}



// typedef void (RexxEntry *RexxPackageLoader)(RexxThreadContext *);
// as of ooRexx 4.0: the library gets loaded once and is available globally to all RexxInstances,
//                   hece it runs once per process only
void RexxEntry bsfLoader (RexxThreadContext *rtc)
{
#if defined(DEBUG1) || defined (DEBUG40) || defined (DEBUG_LOADER_UNLOADER ) || defined (DEBUG_JNI)
    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            tid=RgfGetTID();

    fprintf(stderr, "/// ===> ===> ===> in bsfLoader(), tid=[%lu], c_rii_ID=[%p] ...\n", (unsigned long) tid, rtc->instance);
    fflush(stderr);
#endif

    rgfInitLocks();        // Initialize critical section
}




// typedef void (RexxEntry *RexxPackageUnloader)(RexxThreadContext *);
// as of ooRexx 4.0: the library gets loaded once and is available globally to all RexxInstances,
//                   hece it runs once per process only
void RexxEntry bsfUnloader (RexxThreadContext * rtc)
{
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();

#if defined(DEBUG1) || defined (DEBUG40) || defined (DEBUG_LOADER_UNLOADER ) || defined (DEBUG_JNI)
    fprintf(stderr, "\\\\\\  <=== <=== <=== in bsfUnloader() tid=[%lu], c_rii_ID=[%p] ...\n", (unsigned long) tid, rtc->instance);
    fflush(stderr);
#endif


// fprintf(stderr, "/// marker (1)...\n");fflush(stderr);


#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, 0, false, 0};
#else
    STRUCT_PARAM param={tid, 0, 0, NULL, 0, 0, 0, 0};
#endif
    // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error};

    RgfAcquireLock();


#ifdef RGF_ATTACH_NEW
    JNIEnv *jenv=NULL;
    jint res1=0, res2=0;

// fprintf(stderr, "===> BSFUnloader(): before GetEnv(), currentJVM=[%p], defaultJVM=[%p]... \n", currentJVM, defaultJVM); fflush(stderr);
    res1=defaultJVM->jvm->GetEnv((void **) &jenv, USE_DEFINED_JNI_VERSION);  // this will indicate that we are not attached as of yet
    if (jenv==NULL)
    {
// fprintf(stderr, "===> BSFUnloader(): before AttachCurrentThread(), res1=[%d]... \n",res1); fflush(stderr);
        res2=defaultJVM->jvm->AttachCurrentThread((void **) &jenv, (void *) &defaultJavaVMAttachArgs);
    }

    param.env=jenv;

// fprintf(stderr, "===> BSFUnloader(): res1=[%d], res2=[%d], param.env=[%p], jenv=[%p]\n", res1, res2, param.env, jenv); fflush(stderr);


#else
    environmentAttachTo(&param, FALSE); // attach to Java
#endif

#if defined ( DEBUG_SHOW_STRUCTURES )
    dumpPARAM(   "bsfUnloader(), after environmentAttachTo(...): param:", &param);
    dumpRAJO(    "bsfUnloader(), after environmentAttachTo(...), begin: ");
    dumpTID2NODE("bsfUnloader(), after environmentAttachTo(...), begin: ");
#endif



    // - param->error:  -1 ... Attach-error, attachResult holds Java error code, raise an exception in caller
    //                  -2 ... no rajo found, raise an exception in caller
    //                  -3 ... cannot safely use the given RAJO in TID, as actual RAJO is being used in multiple Rexx threads
    //                  -4 ... cannot attach to target TID
    //                 -99 ... no JVM available !

    // error '-99' may may happen, if BsfUnloadJava() was called and UNINIT operations on BSF proxies
    // get triggered, which would unregister the counterpart Java objects from the BSFRegistry;
    // this lets Rexx shutdown gracefully

    JavaVM *tmpJVM=defaultJVM->jvm;
    int     tmpBsfInvokedBy=bsfInvokedBy;   // 2...loaded by Rexx!

    // empty all RAJO and TID2NODE lists, uninit defaultJVM data
    if (param.error==0 || (param.error > -1 && param.error < -99) )
    {
// fprintf(stderr, "/// marker (2), pRoot_RAJO=[%p]...\n", pRoot_RAJO);fflush(stderr);
#ifndef RGF_ATTACH_NEW
        removeAll_nodes(param.env);
#endif

// fprintf(stderr, "/// marker (3)...\n");fflush(stderr);
        uninit_STRUCT_JVM(param.env, defaultJVM);   // uninitialize data structures, remove global refs, nullify structure
    }

    if (tmpJVM!=NULL)
    {
        int res=tmpJVM->DetachCurrentThread();   // detach this thread from JVM as well
// fprintf(stderr, "/// marker (4), res=[%d]...\n", res);fflush(stderr);

        // don't explicitly destroy JVM, if Rexx threads that are attached to Java
        // got killed, then they can never detach from Java obviously, causing DestroyJavaVM()
        // to wait forever.
/*
        if (tmpBsfInvokedBy==2)     // loaded by Rexx, o.k. to destroy the JVM
        {
fprintf(stderr, "/// marker (5a)...\n");fflush(stderr);

            long ret=tmpJVM->DestroyJavaVM ( ); // destroy JVM

fprintf(stderr, "///  marker (5b), res=[%d]...\n", ret);fflush(stderr);
        }
*/

    }


#if defined ( DEBUG_SHOW_STRUCTURES )
    dumpPARAM(   "bsfUnloader(), at the end: param:", &param);
    dumpRAJO(    "bsfUnloader(), at the end: ");
    dumpTID2NODE("bsfUnloader(), at the end: ");
#endif

    RgfReleaseLock();   // finally release the lock
    rgfDestroyLocks();  // destroy/delete/close lock resources
}



// External functions if loaded via Rexx
    // Build the list of entry points for the routines:
    RexxRoutineEntry bsf_external_functions[] =
    {
        REXX_TYPED_ROUTINE( BSF                         , BSF                         ),
        REXX_TYPED_ROUTINE( BsfAttachToTID              , BsfAttachToTID              ),
//        REXX_TYPED_ROUTINE( BsfProcessCallbackArgs      , BsfProcessCallbackArgs      ),
        REXX_TYPED_ROUTINE( BsfCreateRexxProxy          , BsfCreateRexxProxy          ),
        REXX_TYPED_ROUTINE( BsfRexxProxy              , BsfRexxProxy              ),
        REXX_TYPED_ROUTINE( BsfDetach                   , BsfDetach                   ),
        REXX_TYPED_ROUTINE( BsfDropFuncs                , BsfDropFuncs                ),
        REXX_TYPED_ROUTINE( BsfGetRIID                  , BsfGetRIID                  ),
        REXX_TYPED_ROUTINE( BsfGetTID                   , BsfGetTID                   ),
        REXX_TYPED_ROUTINE( BsfInvokedBy                , BsfInvokedBy                ),
        REXX_TYPED_ROUTINE( BsfJavaException            , BsfJavaException            ),
        REXX_TYPED_ROUTINE( BsfLoadFuncs                , BsfLoadFuncs                ),
        REXX_TYPED_ROUTINE( BsfLoadJava                 , BsfLoadJava                 ),
        REXX_TYPED_ROUTINE( BsfQueryAllFunctions        , BsfQueryAllFunctions        ),
        REXX_TYPED_ROUTINE( BsfQueryRegisteredFunctions , BsfQueryRegisteredFunctions ),
        REXX_TYPED_ROUTINE( BsfShowErrorMessage         , BsfShowErrorMessage         ),
        REXX_TYPED_ROUTINE( BsfUnloadJava               , BsfUnloadJava               ),
        REXX_TYPED_ROUTINE( BsfVersion                  , BsfVersion                  ),
        REXX_TYPED_ROUTINE( BsfRawBytes                 , BsfRawBytes                 ),    // rgf, 2010-02-09

        REXX_TYPED_ROUTINE( BsfContextVariables         , BsfContextVariables         ),    // rgf, 2015-06-04

#if defined (CONFIG_MAKE_OREXX_REGISTRY_ACCESSIBLE)
        REXX_TYPED_ROUTINE( BsfGetRegistryObjects       , BsfGetRegistryObjects       ),    // rgf, 2010-06-06
#endif

        REXX_LAST_ROUTINE()
    };

    RexxPackageEntry bsf_package_entry =
    {
        STANDARD_PACKAGE_HEADER
        REXX_INTERPRETER_4_1_0,              // anything including and after 4.1.0 will work
        "bsf",                               // name of the package
        "4.5",                               // package version information
        bsfLoader,                           // no load function
        bsfUnloader,                         // no unload function
        bsf_external_functions,              // the exported functions
        NULL                                 // no exported methods
    };

    // The package loading stub.  OOREXX_GET_PACKAGE() is a macro that, basically,
    // defines a function whose name is RexxGetPackage().  On Windows, this
    // function, RexxGetPackage(), must be in the exports list of the .def file used
    // when linking this library.
    OOREXX_GET_PACKAGE(bsf);







// rgf, 20090503: new functionality, needing 4.0 APIs



/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniRexxCreateInterpreterInstance
 * Signature: ([Ljava/lang/Object)Ljava/lang/String;

    returns:  the rii_ID (RexxInterpreter instance ID) or NULL, if RexxInterpreter instance could not be created
 */
// JNIEXPORT jstring JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxCreateInterpreterInstance
//   (JNIEnv *env, jobject jobj)



//  joptions as of 2012-02-06, either NULL or Object[] with:
//        RexxConfiguration object            // [0]: Rexx configuration object in use for easying exit and command handler callback
//        initial_address_environment,        // [1]: INITIAL_ADDRESS_ENVIRONMENT (null or String)
//        external_call_path,                 // [2]: EXTERNAL_CALL_PATH (null or String)
//        external_call_extensions,           // [3]: EXTERNAL_CALL_EXTENSIONS (null or String)
//        getRequiredLibrary4JNI(),           // [4]: LOAD_REQUIRED_LIBRARY  (null or String[])
//        getExitHandlers4JNI(),              // [5]: DIRECT_EXITS  (null or Object[]={int[],RexxExitHandler[]})
//        getCommandHandlers4JNI()            // [6]: DIRECT_ENVIRONMENTS  (null or Object[]={String[],RexxCommandHandler[]})

JNIEXPORT jstring JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxCreateInterpreterInstance
  (JNIEnv *env, jobject jobj, jobjectArray joptions)
{
#if defined(RGF_JNI_RII) || defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)

    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            thisTid=RgfGetTID();

    fprintf(stderr, "--> arrived: .._jniRexxCreateInterpreterInstance(...), thisTid=[%lu]\n", (unsigned long) thisTid);
    fflush(stderr);
#endif


    // check # of  needed options, allocate necessary memory
        // option[0]: REGISTER_LIBRARY
        // option[1]: EXTERNAL_CALL_EXTENSIONS (if not supplied use default)
        // option[...]
        // option[n]: for indicating end of options by: optionName=NULL
    int    countOptions=3;
    jstring j_initial_address_environment = NULL;   // [1]
    jstring j_external_call_path          = NULL;   // [2]
    jstring j_external_call_extensions    = NULL;   // [3]

    jobjectArray j_load_required_library  = NULL;   // [4]
    long    nr_load_required_library      = 0;              // number of libraries to call

    jintArray j_direct_exits              = NULL;   // [5]
    long    nr_direct_exits               = 0;              // number of exits

    jobjectArray  j_direct_environments   = NULL;   // [6]
    long    nr_direct_environments        = 0;              // number of commands

    if (joptions!=NULL)     // options from Java ?
    {
            // INITIAL_ADDRESS_ENVIRONMENT, jstring
        j_initial_address_environment = (jstring) env->GetObjectArrayElement(joptions,1);
        if ( j_initial_address_environment != NULL) {
             countOptions++;
        }

            // EXTERNAL_CALL_PATH, jstring
        j_external_call_path          = (jstring) env->GetObjectArrayElement(joptions,2);
        if (j_external_call_path!=NULL) {
            countOptions++;
        }

            // EXTERNAL_CALL_EXTENSION (already counted), jstring
        j_external_call_extensions= (jstring) env->GetObjectArrayElement(joptions,3);


            // LOAD_REQUIRED_LIBRARY (multiples allowed, one option for each), jstring[]
        j_load_required_library = (jobjectArray) env->GetObjectArrayElement(joptions,4);
        if ( j_load_required_library != NULL) {
            nr_load_required_library=env->GetArrayLength( j_load_required_library );
            countOptions+=nr_load_required_library;
        }

            // DIRECT_EXITS (one option with a RexxContextExit[]), int[]
        j_direct_exits = (jintArray) env->GetObjectArrayElement(joptions,5);
        if ( j_direct_exits != NULL) {
            nr_direct_exits=env->GetArrayLength( j_direct_exits );
            countOptions++;
        }

            // DIRECT_ENVIRONMENTS (one option with a RexxContextEnvironment[]), jstring[]
        j_direct_environments = (jobjectArray) env->GetObjectArrayElement(joptions,6);
        if ( j_direct_environments != NULL) {
            nr_direct_environments=env->GetArrayLength( j_direct_environments );
            countOptions++;
        }
    }


        // allocate necessary memory for RexxOption items
    long sizeOptions=countOptions*sizeof(RexxOption);
    RexxOption *options=(RexxOption *) RgfAllocateMemory(sizeOptions);
    memset(options,0,sizeOptions);

    // const char c_rii_ID[ RGF_POINTER_STRING_WIDTH+1 ]="";
    char *c_rii_ID=new char[RGF_POINTER_STRING_WIDTH];

    RexxInstance      *instance;
    RexxThreadContext *threadContext;
    // RexxOption         options[3];


    jstring            j_rii_ID=NULL;   // Rexx interpreter instance (RII) pointer encoded as a jstring

        // load the external functions from the defined RexxPackageEntry from this very dynamic Rexx library
    RexxLibraryPackage pkg;
    pkg.registeredName   = "BSF4ooRexx";
    pkg.table            = &bsf_package_entry;

    options[0].optionName= REGISTER_LIBRARY;
    options[0].option    = (void *) &pkg; // &bsf_external_functions;  //

        // 2009-10-11, rgf, add extensions for BSF.CLS
        // 2012-02-08, rgf, process possible argument from Java
    options[1].optionName= EXTERNAL_CALL_EXTENSIONS;
    const char* c_external_call_extensions=NULL;    // TODO: rgf, 20130630 - test change from "char *" to "const char *" !
    if (j_external_call_extensions==NULL)       // o.k. not supplied by Java, set original default
    {
        c_external_call_extensions = ".rxj,.rxo,.rxjo,.jrexx";  //  ".jrexx,.rexxj,.rexxjo";
    }
    else
    {
        // resulting value needs to get freed at the end !
        c_external_call_extensions=JNU_GetStringNativeChars(env, j_external_call_extensions);   // convert to native string
//        c_external_call_extensions=(char *) JNU_GetStringNativeChars(env, j_external_call_extensions);   // convert to native string
    }
    options[1].option    = c_external_call_extensions;

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): idx=[1], .optionName=[%s], .option=[%s]\n",
                    options[1].optionName, options[1].option);
#endif

    long idx=2;     // already two options, another option needs to be the third one

        // now process possible Java options

        // INITIAL_ADDRESS_ENVIRONMENT
    const char * c_initial_address_environment=NULL;
    if (j_initial_address_environment!=NULL)
    {
        // resulting value needs to get freed at the end !
        c_initial_address_environment=(char *) JNU_GetStringNativeChars(env, j_initial_address_environment);   // convert to native string

        options[idx].optionName=INITIAL_ADDRESS_ENVIRONMENT;
        options[idx].option=c_initial_address_environment;

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): idx=[%d], .optionName=[%s], .option=[%s]\n",
                    idx, options[idx].optionName, options[idx].option);
#endif

        idx++;  // increase index
    }

        // EXTERNAL_CALL_PATH
    const char * c_external_call_path=NULL;
    if (j_external_call_path!=NULL)
    {
        c_external_call_path=(char *) JNU_GetStringNativeChars(env, j_external_call_path);   // convert to native string

        options[idx].optionName=EXTERNAL_CALL_PATH;
        options[idx].option=c_external_call_path;
#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): idx=[%d], .optionName=[%s], external call path=[%s]\n",
                    idx, options[idx].optionName, c_external_call_path);
#endif

        idx++;  // increase index
    }

        // LOAD_REQUIRED_LIBRARY: jstring[] - for each library set one option
    if (j_load_required_library!=NULL)
    {
        for (int i=0; i<nr_load_required_library; i++) {
            options[idx].optionName=LOAD_REQUIRED_LIBRARY;
            jstring jstr=(jstring) env->GetObjectArrayElement(j_load_required_library,i);
            options[idx].option=JNU_GetStringNativeChars(env, jstr);
            env->DeleteLocalRef(jstr);

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): required library, idx=[%d], .optionName=[%s] | i=[%d]: .option=[%s]\n",
                    idx, options[idx].optionName, i, options[idx].option);
#endif
            idx++;  // increase options index
        }
    }

        // DIRECT_EXITS: int[] - for each library set one option
    RexxContextExit *exits=NULL;
    if (j_direct_exits!=NULL)
    {
            // allocate necessary memory for RexxContextExits
        long tmpSize=(nr_direct_exits+1)*sizeof(RexxContextExit);
        exits=(RexxContextExit *) RgfAllocateMemory(tmpSize);
        memset(exits,0,tmpSize);

        jint * tmpArrExits=env->GetIntArrayElements(j_direct_exits,NULL);
        for (int i=0; i<nr_direct_exits; i++)
        {
            exits[i].handler=(RexxContextExitHandler *) rexx_exit_handler_entry;   // point to generic exit handler
            exits[i].sysexit_code=tmpArrExits[i];
#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): exit # [%d], sysexit_code=[%d]\n",
                    i, exits[i].sysexit_code);
#endif
        }
        env->ReleaseIntArrayElements(j_direct_exits, tmpArrExits, JNI_ABORT);  // do not copy elements back to Java

        exits[nr_direct_exits].sysexit_code=0;     // indicate list has ended

        options[idx].optionName=DIRECT_EXITS;
        options[idx].option=(void *)exits;
#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): idx=[%d], .optionName=[%s]\n",
                    idx, options[idx].optionName);
#endif

        idx++;  // increase options index
    }

    // DIRECT_ENVIRONMENTS: jstring[] - for each library set one option
    RexxContextEnvironment *environments=NULL;
    if (j_direct_environments!=NULL)
    {
            // allocate necessary memory for RexxContextEnvironment
        long tmpSize=(nr_direct_environments+1)*sizeof(RexxContextEnvironment);
        environments=(RexxContextEnvironment *) RgfAllocateMemory(tmpSize);
        memset(environments,0,tmpSize);

        for (int i=0; i<nr_direct_environments; i++)
        {
            environments[i].handler=rexx_command_handler_entry;   // point to generic exit handler
            jstring jstr=(jstring) env->GetObjectArrayElement(j_direct_environments,i);
            environments[i].name=JNU_GetStringNativeChars(env, jstr);
            env->DeleteLocalRef(jstr);

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): environment # [%d], name=[%s]\n",
                    i, environments[i].name);
#endif
        }
        environments[nr_direct_environments].name=NULL;     // indicate list has ended

        options[idx].optionName=DIRECT_ENVIRONMENTS;
        options[idx].option=(void *)environments;
#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): idx=[%d], .optionName=[%s]\n",
                    idx, options[idx].optionName);
#endif
        idx++;  // increase options index
    }

        // indicate end of options
    options[idx].optionName=NULL;


#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "\n===> _jniRexxCreateInterpreterInstance(): iterating over all options, display option names, idx=[%d], optionCount=[%d]:\n", idx, countOptions);

    {
        for (int i=0; i<countOptions;i++)
        {
            fprintf(stderr, "\toption[%d].optionName=[%s]\n", i, options[i].optionName);
        }
    }
#endif


#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "\n===> _jniRexxCreateInterpreterInstance(): before creating RII, iniEnv=[%s], callPath=[%s], callExt=[%s], #opts=[%d], #req_lib=[%lu], #exits=[%lu], #env=[%lu]\n",
            c_initial_address_environment, c_external_call_path, c_external_call_extensions, countOptions,
            nr_load_required_library, nr_direct_exits, nr_direct_environments);
#endif


    // if (RexxCreateInterpreter(&instance, &threadContext, options))

#if defined(RGF_JNI_RII) || defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "\n===> _jniRexxCreateInterpreterInstance(): BEFORE creating RII, thisTid=[%lu]\n", (unsigned long) thisTid);fflush(stderr);
#endif

    int bSuccess=RexxCreateInterpreter(&instance, &threadContext, options);

#if defined(RGF_JNI_RII) || defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "\n===> _jniRexxCreateInterpreterInstance(): AFTER  creating RII, thisTid=[%lu], bSuccess=[%d]\n", (unsigned long) thisTid,bSuccess);fflush(stderr);
#endif

    if (bSuccess)
    {

        // RgfPointer2String(instance, c_rii_ID);  // create a string
#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
        fprintf(stderr, "===---=== .._jniRexxCreateInterpreterInstance(...), thisTid=[%lu], CREATED: c_rii_ID={%p]\n", (unsigned long) thisTid, instance);
        fflush(stderr);
#endif


        // rgf, 20140330: add TID to .local to save the primodal TID for this Rexx interpreter instance
        {
            #ifdef UNIX
                pthread_t
            #else   // WINDOWS
                TID
            #endif
                    tid=RgfGetTID();

            // char strTid[32]="";         // define a string long enough to receive an integer
            // sprintf(strTid, "%lu%c", ((unsigned long) tid), 0);  // create decimal number
            // SNPRINTF( strTid, 32, "%lu%c", ((unsigned long) tid), 0);  // create decimal number

            char *strTid=new char[RGF_TID_STRING_WIDTH];
            SNPRINTF( strTid, RGF_TID_STRING_WIDTH, "%lu%c", ((unsigned long) tid), 0);  // create decimal number

            #ifdef RGF_INFO
                fprintf(stderr, "*** RGF_INFO: BsfLoadJava(), tid=[%lu], %%s=[%s] ...\n", (unsigned long) tid, strTid);
                fflush(stderr);
            #endif

            RexxDirectoryObject localDir = threadContext->GetLocalEnvironment();   // get .local
            threadContext->DirectoryPut(localDir, threadContext->String(strTid), LOCAL_PRIMODAL_TID);
            threadContext->ReleaseLocalReference(localDir);

            delete[] strTid;
        }
        // ----------------------------------------------------------


            // rgf, 2012-02-07, save RexxAndJava (rajo) and RexxConfiguration (rexxconf) object with RII-structure
        RgfAddRexxInterpreterInstanceToList(threadContext,
                                            env->NewGlobalRef(jobj),                                    // rajo
                                            env->NewGlobalRef(env->GetObjectArrayElement(joptions,0))   // rexxconfig
                                            );


        RgfPointer2String(instance, c_rii_ID);  // create a string

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
        fprintf(stderr, "===---=== .._jniRexxCreateInterpreterInstance(...), thisTid=[%lu], CREATED: c_rii_ID={%p]=[%s]\n", (unsigned long) thisTid, instance, c_rii_ID);
        fflush(stderr);
#endif


        j_rii_ID=env->NewStringUTF( c_rii_ID ); // create Java string

    }

        // free memory allocated in this routine
    if ( j_initial_address_environment!=NULL )
    {
       RexxFreeMemory((void *)c_initial_address_environment);
       env->DeleteLocalRef(j_initial_address_environment);
    }

    if ( j_external_call_extensions   !=NULL )
    {
        RexxFreeMemory((void *)c_external_call_extensions);
        env->DeleteLocalRef(j_external_call_extensions);
    }

    if ( j_external_call_path         !=NULL )
    {
        RexxFreeMemory((void *)c_external_call_path);
        env->DeleteLocalRef(j_external_call_path);
    }

    if ( exits!=NULL ) {RexxFreeMemory(exits);}

    if ( environments!=NULL)
    {
        for (int i=0;i<nr_direct_environments;i++) {
            RexxFreeMemory((void *)environments[i].name);
        }
        RexxFreeMemory(environments);
    }

        // delete required library names, if any
    for (int i=2; i<(countOptions-1);i++) {
        if ( options[i].option==LOAD_REQUIRED_LIBRARY )
        {
            RexxFreeMemory((void *)options[i].optionName);
        }
    }

    RexxFreeMemory(options);

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "<-- returning from: .._jniRexxCreateInterpreterInstance(...), thisTid=[%lu], c_rii_ID=[%.256s]\n",
                        (unsigned long) thisTid, c_rii_ID);
    fflush(stderr);
#endif

    if (j_load_required_library!=NULL) {env->DeleteLocalRef(j_load_required_library);}
    if (j_load_required_library!=NULL) {env->DeleteLocalRef(j_direct_exits);         }
    if (j_load_required_library!=NULL) {env->DeleteLocalRef(j_direct_environments);  }

    if (!bSuccess)      // if Rexx interpreter instance could not be created, throw a Java exception
    {
        // char msg[1024]="";
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/jniCreateInterpreterInstance(), error 1.001: could not create RexxInterpreter instance", DLLNAME);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete [] msg;

#if defined(RGF_JNI_RII)
   fprintf(stderr, " ===> PANIC! PANIC! PANIC! could not create Rexx interpreter instance! <=== \n"); fflush(stderr);
#endif

        env->Throw(j_rexxException); // throw the exception in the JVM
    }

    delete [] c_rii_ID;
    return j_rii_ID;    // return jstring of rii_ID
}





/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniGetRexxInterpreterInstanceRoot
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetRexxInterpreterInstanceRoot
  (JNIEnv *env, jobject raj)
{
#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)

    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            thisTid=RgfGetTID();


    fprintf(stderr, "--> arrived: .._jniGetRexxInterpreterInstanceRoot(...), thisTid=[%lu]\n", (unsigned long) thisTid);
    fflush(stderr);
#endif



    if (pRoot_RII==NULL)        // no Rexx interpreter instance as of yet!
    {
        return NULL;
    }

    // const char c_rii_ID[ RGF_POINTER_STRING_WIDTH+1 ]="";
    char *c_rii_ID=new char[RGF_POINTER_STRING_WIDTH];

    RgfPointer2String(pRoot_RII->instance, c_rii_ID);     // get Root node's charater value

    jstring j_rii_ID=env->NewStringUTF( c_rii_ID ); // create Java string

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (RGF_JNI)
    fprintf(stderr, "<-- returning from: .._jniGetRexxInterpreterInstanceRoot(...), thisTid=[%lu], c_rii_ID=[%.256s]\n",
                        (unsigned long) thisTid, c_rii_ID);
    fflush(stderr);
#endif

    delete [] c_rii_ID;
    return j_rii_ID;    // return jstring of rii_ID

}




/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniRexxRunProgram
 * Signature: (Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxRunProgram
  (JNIEnv *env, jobject raj, jstring j_rii_ID,
                             jint invocationType,   // 1=CallProgram, 2=CallProgramFromData, 3=LoadPackage, 4=LoadPackageFromData
                             jstring j_fileName,
                             jstring j_programData, // if given, use this as code and "fileName" as its name
                             jobjectArray j_args)
{
    // rgf, 2009-05-24, begin
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();   // get current TID

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "--> arrived: .._jniRexxRunProgram(...), tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], defaultJVM->clz_Object=[%p]\n",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, defaultJVM->clz_Object);
    fflush(stderr);
#endif


        // get interpreter instance from argument
    char         *c_rii_ID=NULL;
    RexxInstance *instance=NULL;

    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string
#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "                                        using c_rii_ID=[%.256s]\n", c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    RexxFreeMemory(c_rii_ID);

/* ---> rgf, 2014-05-17: no need for attaching/detaching to JVM as we are invoked in that context!
    // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error};
    STRUCT_PARAM param={tid, env, raj, instance, 0, 0, 0, 0};
    RgfAcquireLock();
    environmentAttachTo(&param, FALSE); // attach to Java
    RgfReleaseLock();

    // - param->error:  -1 ... Attach-error, attachResult holds Java error code, raise an exception in caller
    //                  -2 ... no rajo found, raise an exception in caller
    //                  -3 ... cannot safely use the given RAJO in TID, as actual RAJO is being used in multiple Rexx threads
    //                  -4 ... cannot attach to target TID
    //                 -99 ... no JVM available !

    // error '-99' may may happen, if BsfUnloadJava() was called and UNINIT operations on BSF proxies
    // get triggered, which would unregister the counterpart Java objects from the BSFRegistry;
    // this lets Rexx shutdown gracefully
    if (param.error== -3)    // problem attaching, cannot proceed
    {
        // char msg[1024]="";
        char *msg=new char[1024];
        createAttachErrorMessage("routine/jniRexxRunProgram(), error 1", msg, 1024, &param);

        jthrowable rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: .._jniRexxRunProgram(...): tid=[%lu], error, Java interface with multiple Rexx threads exists!\n",
                    (unsigned long) tid);
    fflush(stderr);
#endif
        return NULL;
    }
<-- */


#if defined(DEBUG_JNI) || defined (DEBUG40)

{
    char tmpBuf[1024];

    JAVA_TO_STRING( env, raj, tmpBuf, 1024);
    fprintf(stderr, "*** *** DEBUG_JNI: ..._jniRexxRunProgram() 1a - tid=[%lu],          raj=[%p] [%s]\n",
                                     (unsigned long) tid,
                                     raj,
                                     tmpBuf
                                     );
/* ---> rgf, 2014-05-17
    JAVA_TO_STRING( param.env, param.rajo, tmpBuf, 1024);
    fprintf(stderr, "*** *** DEBUG_JNI: ..._jniRexxRunProgram() 1b - tid=[%lu], param.rajo=[%p] [%s]\n",
                                     (unsigned long) tid,
                                     param.rajo,
                                     tmpBuf
                                     );
<-- */
    fflush(stderr);
}
#endif


    RexxThreadContext   *rtc=NULL;
    RexxObjectPtr result_obj;           // Rexx object returned from Rexx, NULL[OBJECT], if none

    bool argOK= ((invocationType==1 || invocationType==3) && j_fileName!=NULL && j_programData==NULL) ||
                ((invocationType==2 || invocationType==4) && j_fileName!=NULL && j_programData!=NULL);

        // check arguments
    if (!argOK)    // error in the supplied arguments
    {
        // char msg[1024]="";
        char *msg=new char[1024];

        if (invocationType<1 || invocationType>4)  // wrong invocationType
        {
            // sprintf(msg, "%.16s/routine/jniRexxRunProgram(), error 1: argument 'invocationType' can only be one of '1', '2', '3' or '4', found: [%d]", DLLNAME, invocationType);
            SNPRINTF( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 2: argument 'invocationType' can only be one of '1', '2', '3' or '4', found: [%d]", DLLNAME, (int) invocationType);
        }
        else if (j_fileName==NULL)
        {
            // sprintf(msg, "%.16s/routine/jniRexxRunProgram(), error 2: argument 'fileName' must not be 'null'", DLLNAME);
            SNPRINTF( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 3: argument 'fileName' must not be 'null'", DLLNAME);
        }
        else if ((invocationType==2 || invocationType==4) && j_programData==NULL)
        {
            // sprintf(msg, "%.16s/routine/jniRexxRunProgram(), error 3: argument 'invocationType' is '%d', therefore 'programData' must not be 'null'", DLLNAME, invocationType);
            SNPRINTF( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 4: argument 'invocationType' is '%d', therefore 'programData' must not be 'null'", DLLNAME, (int) invocationType);
        }
        else if ((invocationType==1 || invocationType==3) && j_programData!=NULL)
        {
            // sprintf(msg, "%.16s/routine/jniRexxRunProgram(), error 4: argument 'invocationType' is '%d', therefore 'programData' must be 'null'", DLLNAME, invocationType);
            SNPRINTF( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 5: argument 'invocationType' is '%d', therefore 'programData' must be 'null'", DLLNAME, (int) invocationType);
        }
        else
        {
            // sprintf(msg, "%.16s/routine/jniRexxRunProgram(), error 5: other error", DLLNAME);
            SNPRINTF( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 6: invocation type arguments in error", DLLNAME);
        }

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM


#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: .._jniRexxRunProgram(...): tid=[%lu], error in arguments, problematic invocationType=[%d]\n",
                                (unsigned long) tid, (int) invocationType);
    fflush(stderr);
#endif

/* ---> rgf, 2014-05-17
        RgfAcquireLock();
        environmentDetachFrom(&param, FALSE);
        RgfReleaseLock();
<--- */

        return NULL;
    }


    RexxInstance *tmpInstance=NULL;
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
        // char msg[1024]="";
        char *msg=new char[1024];
        // sprintf(msg, "%.16s/routine/jniRexxRunProgram(), error 6: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);
        SNPRINTF( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 7: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: .._jniRexxRunProgram(...): tid=[%lu], error, cannot retrieve c_rii_ID=p=[%p], received=tmpInstance=[%p] instead\n",
                    (unsigned long) tid, instance, tmpInstance);
    fflush(stderr);
#endif

/* ---> rgf, 2014-05-17
        RgfAcquireLock();
        environmentDetachFrom(&param, FALSE);
        RgfReleaseLock();
<--- */

        return NULL;
    }

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "--------------> ooRexx->AttachThread() - jniRexxRunProgram tid=[%lu] ... ", (unsigned long) tid); fflush(stderr);
#endif

    // rgf, 2013-10-07: no need to do a rtc->ReleaseLocalReference(ro) as these references get released upon rtc->DetachThread() !
    if (!instance->AttachThread(&rtc))          // get a ThreadContext
    {

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "UNSUCCESSFUL!  tid=[%lu]  :-( \n", (unsigned long) tid); fflush(stderr);
#endif
        // char msg[1024]="";
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 8: could not AttachThread() to Rexx interpreter instance with the ID '%p'", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: .._jniRexxRunProgram(...): tid=[%lu], error, cannot do an instance->AttachThread()!\n",
                          (unsigned long) tid);
    fflush(stderr);
#endif

        // RexxFreeMemory(c_rii_ID);       // make sure we free the allocated memory

/* ---> rgf, 2014-05-17
        RgfAcquireLock();
        environmentDetachFrom(&param,FALSE);
        RgfReleaseLock();
<-- */
        return NULL;
    }

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "done. --------------> tid=[%lu] \n", (unsigned long) tid); fflush(stderr);
#endif


        // set fileName
    char *c_fileName=(char *) JNU_GetStringNativeChars(env, j_fileName);    // convert to native string

    char *c_programData=NULL;

        // process Java argument array, get matching RexxArrayObject;
        // 20150721, have BSF registry reference counter increased
    RexxArrayObject ra=RgfProcessJArgs(env, rtc, j_args, NULL, FALSE, 1);

    // invocationType:
    //   1: execute Rexx program from file, use the 4.0 API <code>CallProgram</code>
    //   2: execute Rexx program from buffer, uses the 4.0 API <code>NewRoutine</code> and <code>CallRoutine</code>
    //   3: load Rexx package (and run prolog) from file, use the 4.0 API <code>LoadPackage</code>
    //   4: load Rexx package (and run prolog) from buffer, use the 4.0 API <code>LoadPackageFromData</code>
// fprintf(stderr, "C++ invocationType=[%d]\n", invocationType);fflush(stderr);

    switch (invocationType)
    {
    case 1:     // CallProgram()
        result_obj=rtc->CallProgram(c_fileName, ra);    // run the program from file
        break;

    case 3:     // LoadPackage()
        result_obj=rtc->LoadPackage(c_fileName);        // load & run the program
        break;

    case 2:
    case 4:     // LoadPackageFromData()break;
                // get programData, if any
            size_t len=0;

            if (j_programData!=NULL)
            {
                c_programData=(char *) JNU_GetStringNativeChars(env, j_programData);   // convert to native string
                len=strlen(c_programData);          // get length
            }

            if (invocationType==2)
            {
                RexxRoutineObject rro=rtc->NewRoutine(c_fileName, c_programData, len);
                if (rro==NULL)          // something went wrong, condition will be set
                {
                    break;
                }

#if defined (DEBUG40_NIXI)
fprintf(stderr, "..._jniRexxRunProgram(...): riid=[%p], tid=[%lu], rtc->NewRoutine(...)~identityHash=[%s]\n",
                      rtc->instance, (unsigned long) tid,
                      rtc->CString(rtc->ObjectToString(rtc->SendMessage0(rro, "IDENTITYHASH") )));
fflush(stderr);
#endif

                result_obj=rtc->CallRoutine(rro, ra);   // call the program
                // rgf, 201310 rtc->ReleaseLocalReference(rro);
            }
            else
            {
                result_obj=rtc->LoadPackageFromData(c_fileName, c_programData, len);    // load & run the program
            }
    }
    // rgf, 201310 rtc->ReleaseLocalReference(ra);

    RexxFreeMemory(c_fileName);      // make sure we free the allocated memory
    RexxFreeMemory(c_programData);      // make sure we free the allocated memory

#if defined (DEBUG40)
    fprintf(stderr, "..._jniRexxRunProgram(...): rtc->CheckCondition()=[%d] /////  tid=[%lu] \n",
                    (int) rtc->CheckCondition(), (unsigned long) tid);
    fflush(stderr);
#endif

    // check for exception; if so, create RexxException, supply condition object as a RexxProxy; clear exception in thread
    if (rtc->CheckCondition())      // a Rexx condition raised?
    {
        RexxDirectoryObject condObj=rtc->GetConditionInfo();
        char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "/routine/jniRexxRunProgram(), error 9");

#if defined (DEBUG40)
    fprintf(stderr, "..._jniRexxRunProgram(...): BEFORE RgfCreateRexxException4Java(...) tid=[%lu]\n", (unsigned long) tid);
    fflush(stderr);
#endif
        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             rtc, // rtc,
                                                             condObj, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "<-------------- ooRexx->DetachThread() - jniRexxRunProgram tid=[%lu]... ", (unsigned long) tid); fflush(stderr);
#endif
        // rgf, 201310 rtc->ReleaseLocalReference(result_obj);
        // rgf, 201310 rtc->ReleaseLocalReference(condObj);
        rtc->DetachThread();

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "done. <-------------- tid=[%lu] \n", (unsigned long) tid); fflush(stderr);
#endif

        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: .._jniRexxRunProgram(...): tid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",
                       (unsigned long) tid, msg);
    fflush(stderr);
#endif

        RexxFreeMemory(msg);
/* ---> rgf, 2014-05-17
        RgfAcquireLock();
        environmentDetachFrom(&param, FALSE);
        RgfReleaseLock();
<--- */
        return NULL;
    }


#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning from: .._jniRexxRunProgram(...), tid=[%lu], normal execution\n", (unsigned long) tid);
    fflush(stderr);
#endif

    jobject jresult=RgfProcessReturnValue4Java(env, raj, rtc, result_obj, NULL);

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "<-------------- ooRexx->DetachThread() - jniRexxRunProgram tid=[%lu] ... ", (unsigned long) tid); fflush(stderr);
#endif

    // rgf, 201310 rtc->ReleaseLocalReference(result_obj);
    rtc->DetachThread();

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "done. <-------------- tid=[%lu] \n", (unsigned long) tid); fflush(stderr);
#endif

/* ---> rgf, 2014-05-17
    RgfAcquireLock();
    environmentDetachFrom(&param, FALSE);
    RgfReleaseLock();
<--- */

    return jresult;
}



/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniRexxSendMessageToRexxObject
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;

   Note (rgf, 20090828): this function will create a slot argument and pass it as an additional (last)
                         argument to the Rexx method, if one of j_userData_ID, j_methodObjectBean or
                         j_methodDescription is given, otherwise (if all these fields are NULL) no such
                         additional slot argument will be created and supplied.

                         This way sending individual Rexx messages to Rexx objects which employ
                         the USE ARG STRICT variant succeed (otherwise they may raise an error, because
                         of too many arguments.

                         In all ohter cases that a Rexx proxy implements Java interfaces, abstract methods and
                         extend a Java class proxying the Java method invocations to the RexxProxy, the
                         slot argument is created and appended as an additional (positioned last) argument.
                         This way the invoked Rexx method can introspect the Java method's signature and
                         in the case of an extended Java class may even get a handle to the peer Java object
                         to become able to interact with it directly (e.g. for forwarding methods to its
                         superclass implementations).
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxSendMessageToRexxObject
  (JNIEnv *env, jobject jobj,
                  jstring j_rii_ID,
                  jstring j_obj_ID,
                  jstring j_userData_ID,        // 4.0, 20090517
//                  jstring j_creationTID,      // 4.0, 20090523 // removed 2009-06-23
                  jstring j_javaObjectBean,     // since 2009-07-11
                  jstring j_methodObjectBean,   // since 2009-06-27
                  jstring j_messageName,        // the Java (mixed) name or null (= not create and append a slot arg!)
                  jstring j_methodDescription,  // since 2009-07-10
                  jobjectArray j_args,
                  jstring j_returnType          // since 2009-08-27
                  )
{

#if defined (RGF_TRUE) || defined(DEBUG_JNI) || defined (DEBUG_40) || defined (RGF_JNI) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT) || defined (DEBUG_JNI_ENTRY)

    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            thisTid=RgfGetTID();

    char strRajo[256];
    JAVA_TO_STRING(env, jobj, strRajo, 256);

        #if defined (RGF_TRUE) /// <============
            // if (env->ExceptionCheck())
            fprintf(stderr, "\\\\\\\\ 01 - tid=[%lu], env->ExceptionCheck()=[%d] //// \n", (unsigned long) thisTid, env->ExceptionCheck()); fflush(stderr);
        #endif                 /// <============

    fprintf(stderr, "--> arrived: .._jniRexxSendMessageToRexxObject(...), thisTid=[%lu], rajo=[%s]\n\tj_rii_ID=[%p], j_obj_ID=[%p], j_userData_ID=[%p], j_methodObjectBean=[%p], j_messageName=[%p], j_methodDescription=[%p], j_args=[%p], j_returnType=[%p]\n",
                          (unsigned long) thisTid,
                          strRajo,
                          j_rii_ID,
                          j_obj_ID,
                          j_userData_ID,
                          j_methodObjectBean,
                          j_messageName,
                          j_methodDescription,
                          j_args,
                          j_returnType
            );
    fflush(stderr);
#endif



    // rgf, 2009-05-24, begin ?
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();   // get current TID
    // rgf, 2009-05-24, end.

    RexxThreadContext   *rtc=NULL;
    RexxObjectPtr result_obj;           // Rexx object returned from Rexx, NULL[OBJECT], if none

        // get interpreter instance
    char *c_rii_ID=NULL;
    RexxInstance *instance=NULL, *tmpInstance=NULL;

    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

        #if defined (RGF_TRUE) /// <============
            // if (env->ExceptionCheck())
            fprintf(stderr, "\\\\\\\\ 02 - tid=[%lu], env->ExceptionCheck()=[%d] //// \n", (unsigned long) thisTid, env->ExceptionCheck()); fflush(stderr);
        #endif                 /// <============

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string


/* ---> , rgf, 2014-05-17, no need
    // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error};
    STRUCT_PARAM param={tid, env, jobj, instance, 0, 0, 0, 0};
    RgfAcquireLock();
    environmentAttachTo(&param, FALSE); // attach to Java
    RgfReleaseLock();

    // - param->error:  -1 ... Attach-error, attachResult holds Java error code, raise an exception in caller
    //                  -2 ... no rajo found, raise an exception in caller
    //                  -3 ... cannot safely use the given RAJO in TID, as actual RAJO is being used in multiple Rexx threads
    //                  -4 ... cannot attach to target TID
    //                 -99 ... no JVM available !

    // error '-99' may may happen, if BsfUnloadJava() was called and UNINIT operations on BSF proxies
    // get triggered, which would unregister the counterpart Java objects from the BSFRegistry;
    // this lets Rexx shutdown gracefully
    if (param.error== -3)    // problem attaching, cannot proceed
    {
        // char msg[1024]="";
        char *msg=new char[1024];
        createAttachErrorMessage("routine/jniRexxSendMessageToRexxObject(), error 1", msg, 1024, &param);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined (RGF_TRUE) || defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: .._jniRexxRunProgram(...): tid=[%lu], error, Java interface with multiple Rexx threads exists!\n",
                    (unsigned long) tid);
    fflush(stderr);
#endif
        return NULL;
    }
<--- */

    // RgfAcquireLock2(RII_lock);      // lock access to list of RexxInstance instances
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance
    // RgfReleaseLock2(RII_lock);      // lock access to list of RexxInstance instances

    if (instance != tmpInstance)    // error (do not use a different Rexx Interpreter instance!)
    {
       // char msg[1024]="";
       char *msg=new char[1024];
       // sprintf(msg, "%.16s/routine/jniRexxSendMessageToRexxObject(), error 1: Rexx interpreter instance with the ID '%.256s' could not be found", DLLNAME, c_rii_ID);
       SNPRINTF( msg, 1024, "%.16s/routine/jniRexxSendMessageToRexxObject(), error 2: Rexx interpreter instance with the ID '%.256s' could not be found", DLLNAME, c_rii_ID);

       jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
       delete[] msg;
       env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined (RGF_TRUE) || defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_REXX_PROXY)|| defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "<-- returning: .._jniRexxSendMessageToRexxObject(...): thisTid=[%lu], error, cannot retrieve c_rii_ID=[%.256s]/p=[%p], received=[%p] instead\n",
                    (unsigned long) tid, c_rii_ID, instance, tmpInstance);
    fflush(stderr);
#endif

        RexxFreeMemory(c_rii_ID);        // make sure we free the allocated memory

/* ---> rgf, 20140517
        RgfAcquireLock();
        environmentDetachFrom(&param, FALSE);
        RgfReleaseLock();
<-- */

        return NULL;
    }

#if defined (RGF_TRUE) || defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "--------------> ooRexx->AttachThread() - jniRexxSendMessageToRexxObject... "); fflush(stderr);
#endif

    // rgf, 2013-10-07: no need to do a rtc->ReleaseLocalReference(ro) as these references get released upon rtc->DetachThread() !
    if (!instance->AttachThread(&rtc))          // get a ThreadContext
    {

#if defined (RGF_TRUE) || defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "UNSUCCESSFUL! :-( \n"); fflush(stderr);
#endif

        // char msg[1024]="";
        char *msg=new char[1024];
        // sprintf(msg, "%.16s/routine/jniRexxSendMessageToRexxObject(), error 2: could not AttachThread() to Rexx interpreter instance with the ID '%.256s'", DLLNAME, c_rii_ID);
        SNPRINTF(msg, 1024, "%.16s/routine/jniRexxSendMessageToRexxObject(), error 3: could not AttachThread() to Rexx interpreter instance with the ID '%.256s'", DLLNAME, c_rii_ID);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM


#if defined (RGF_TRUE) || defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_REXX_PROXY)|| defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "<-- returning: .._jniRexxSendMessageToRexxObject(...): thisTid=[%lu], error, cannot do an instance->AttachThread()!\n",
                         (unsigned long) tid);
    fflush(stderr);
#endif

        RexxFreeMemory(c_rii_ID);       // make sure we free the allocated memory

/* ---> rgf, 20140517
        RgfAcquireLock();
        environmentDetachFrom(&param, FALSE);
        RgfReleaseLock();
<--- */

        return NULL;
    }

#if defined (RGF_TRUE) || defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "done. -------------->\n"); fflush(stderr);
#endif


        // get Rexx object
    char *c_obj_ID=(char *) JNU_GetStringNativeChars(env, j_obj_ID);    // convert to native string
        #if defined (RGF_TRUE) /// <============
            // if (env->ExceptionCheck())
            fprintf(stderr, "\\\\\\\\ 03 - tid=[%lu], env->ExceptionCheck()=[%d] //// \n", (unsigned long) thisTid, env->ExceptionCheck()); fflush(stderr);
        #endif                 /// <============

    RexxStringObject rso=rtc->String(c_obj_ID);
    RexxObjectPtr ro=RgfGetProxyObject(rtc, rso);     // get Rexx object
    // rgf, 201310 rtc->ReleaseLocalReference(rso);
    // RexxObjectPtr ro=RgfGetProxyObject(rtc, rtc->String(c_obj_ID));     // get Rexx object
    // RexxObjectPtr ro=RgfGetProxyObjectPackage(rtc, rtc->String(c_obj_ID);


    // rgf, 2009-06-22: creating a directory will cause the creation of the additional (last) argument  containing
    // the original method name (case preserving) and the TID in which the RexxProxy being used got created
    RexxDirectoryObject slotDir=NULL; // rgf, 2009-06-22 this will always get added as the last argument

    char *c_msg=NULL;           // will receive the message name from the Java argument

// fprintf(stderr, "RAJ-callBack(), BEVOR: j_messageName=[%p], ExceptionCheck()=[%d]\n", j_messageName, env->ExceptionCheck());fflush(stderr);

    c_msg        =(char *) JNU_GetStringNativeChars(env, j_messageName);    // convert to native string
        #if defined (RGF_TRUE) /// <============
            // if (env->ExceptionCheck())
            fprintf(stderr, "\\\\\\\\ 04 - tid=[%lu], env->ExceptionCheck()=[%d] //// \n", (unsigned long) thisTid, env->ExceptionCheck()); fflush(stderr);
        #endif                 /// <============

// fprintf(stderr, "RAJ-callBack(), DANACH: j_messageName=[%p], c_msg=[%.256s], ExceptionCheck()=[%d]\n", j_messageName, c_msg, env->ExceptionCheck());fflush(stderr);

    if ((j_userData_ID!=NULL) ||
        (j_methodObjectBean!=NULL) ||
        (j_methodDescription!=NULL))      // build slotArgument (an ooRexx directory object) ?
    {
        slotDir=rtc->NewDirectory();    // create ooRexx directory object

        if (j_userData_ID!=NULL)    // userData is given, hence create directory containing it (will get supplied as last argument)
        {
            char *c_userData_ID=(char *) JNU_GetStringNativeChars(env, j_userData_ID);    // convert to native string
        #if defined (RGF_TRUE) /// <============
            // if (env->ExceptionCheck())
            fprintf(stderr, "\\\\\\\\ 05 - tid=[%lu], env->ExceptionCheck()=[%d] //// \n", (unsigned long) thisTid, env->ExceptionCheck()); fflush(stderr);
        #endif                 /// <============

            rso=rtc->String(c_userData_ID);
            RexxObjectPtr ro_userData=RgfGetProxyObject(rtc, rso);  // get userData Rexx object
            // rgf, 201310 rtc->ReleaseLocalReference(rso);
            // RexxObjectPtr ro_userData=RgfGetProxyObject(rtc, rtc->String(c_userData_ID));      // get userData Rexx object
            rtc->DirectoryPut(slotDir, ro_userData, "USERDATA");            // make userData available
            RexxFreeMemory(c_userData_ID);      // make sure we free the allocated memory
            // rgf, 201310 rtc->ReleaseLocalReference(ro_userData);
        }


        if (j_methodObjectBean!=NULL)    // userData is given, hence create directory containing it (will get supplied as last argument)
        {
            char *c_methodObjectBean=(char *) JNU_GetStringNativeChars(env, j_methodObjectBean);    // convert to native string
        #if defined (RGF_TRUE) /// <============
            // if (env->ExceptionCheck())
            fprintf(stderr, "\\\\\\\\ 06 - tid=[%lu], env->ExceptionCheck()=[%d] //// \n", (unsigned long) thisTid, env->ExceptionCheck()); fflush(stderr);
        #endif                 /// <============
            RexxObjectPtr ro_methodObject=rtc->String(c_methodObjectBean);  // turn native string into a RexxString
            RexxFreeMemory(c_methodObjectBean);     // make sure we free the allocated memory

            RexxObjectPtr clzBSF=rgfGetEntryFromLocal (rtc, "BSF");
            if (clzBSF != rtc->Nil() ) // BSF.CLS got loaded ?
            {
                RexxObjectPtr rop=ro_methodObject;
                ro_methodObject=(RexxStringObject) rtc->SendMessage1(clzBSF, "BSF.WRAP", rop);
                // rgf, 201310 rtc->ReleaseLocalReference(rop);
            }

            rtc->DirectoryPut(slotDir, ro_methodObject, "METHODOBJECT");    // save it in callback directory
            // rgf, 201310 rtc->ReleaseLocalReference(clzBSF);
            // rgf, 201310 rtc->ReleaseLocalReference(ro_methodObject);
        }

        if (! rtc->CheckCondition())    // so far no problems? then proceed
        {
            if (j_javaObjectBean!=NULL)    // 2009-07-11: if dealing with an object from a dynamically created Java class (i.e. extending an abstract Java class)
            {
                char *c_javaObjectBean=(char *) JNU_GetStringNativeChars(env, j_javaObjectBean);    // convert to native string
        #if defined (RGF_TRUE) /// <============
            // if (env->ExceptionCheck())
            fprintf(stderr, "\\\\\\\\ 07 - tid=[%lu], env->ExceptionCheck()=[%d] //// \n", (unsigned long) thisTid, env->ExceptionCheck()); fflush(stderr);
        #endif                 /// <============
                RexxObjectPtr ro_methodObject=rtc->String(c_javaObjectBean);  // turn native string into a RexxString
                RexxFreeMemory(c_javaObjectBean);     // make sure we free the allocated memory

                RexxObjectPtr clzBSF=rgfGetEntryFromLocal (rtc, "BSF"); // BSF.CLS available, if so, wrap it up as a BSF proxy
                if (clzBSF != rtc->Nil() ) // BSF.CLS got loaded ?
                {
                    RexxObjectPtr rop=ro_methodObject;
                    // ro_methodObject=(RexxStringObject) rtc->SendMessage1(clzBSF, "BSF.WRAP", ro_methodObject);
                    ro_methodObject=(RexxStringObject) rtc->SendMessage1(clzBSF, "BSF.WRAP", rop);
                    // rgf, 201310 rtc->ReleaseLocalReference(rop);
                }

                rtc->DirectoryPut(slotDir, ro_methodObject, "JAVAOBJECT");    // save it in callback directory
                // rgf, 201310 rtc->ReleaseLocalReference(clzBSF);
                // rgf, 201310 rtc->ReleaseLocalReference(ro_methodObject);
            }


            if (j_methodDescription!=NULL)     // 2009-07-10
            {
                char *c_msgDesc   =(char *) JNU_GetStringNativeChars(env, j_methodDescription);    // convert to native string
        #if defined (RGF_TRUE) /// <============
            // if (env->ExceptionCheck())
            fprintf(stderr, "\\\\\\\\ 08 - tid=[%lu], env->ExceptionCheck()=[%d] //// \n", (unsigned long) thisTid, env->ExceptionCheck()); fflush(stderr);
        #endif                 /// <============
                RexxStringObject rso=rtc->String(c_msgDesc);
                rtc->DirectoryPut(slotDir, rso, "METHODDESCRIPTOR");   // make original method name available
                // rgf, 201310 rtc->ReleaseLocalReference(rso);
                // rtc->DirectoryPut(slotDir, rtc->String(c_msgDesc), "METHODDESCRIPTOR");   // make original method name available

                RexxFreeMemory(c_msgDesc);      // make sure we free the allocated memory
            }

            RexxStringObject rso=rtc->String(c_msg);
            rtc->DirectoryPut(slotDir, rso, "METHODNAME");   // make original method name available
            // rgf, 201310 rtc->ReleaseLocalReference(rso);
            // rtc->DirectoryPut(slotDir, rtc->String(c_msg), "METHODNAME");   // make original method name available
        }
    }


    if (! rtc->CheckCondition())    // so far no problems? then proceed
    {

#if defined (RGF_TRUE) || defined (DEBUG_40) || defined (DEBUG_REXX_PROXY)|| defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "...jniRexxSendMessageToRexxObject(): thisTid=[%lu]... ro=[%.256s] ...\n",
                                       (unsigned long) thisTid, rtc->ObjectToStringValue(ro));
    fflush(stderr);
#endif


        if (j_args==NULL)              // no args: hence message with a slotDir or without arguments altogether?
        {

            if (slotDir==NULL)
            {
#if defined (RGF_TRUE) || defined (DEBUG_40) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "...jniRexxSendMessageToRexxObject(), NOARGS-branch: thisTid=[%lu], ... ro=[%p], SendMessage0: c_msg=[%.256s] (slotDir=[%p])...\n",
                              (unsigned long) thisTid, ro, c_msg, slotDir);
    fflush(stderr);
#endif

                result_obj=rtc->SendMessage0(ro, c_msg);   // no arguments, no slotArgument
            }
            else    // supply the slotDir
            {
#if defined (RGF_TRUE) || defined (DEBUG_40) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "...jniRexxSendMessageToRexxObject(), NOARGS-branch: thisTid=[%lu], ... ro=[%p], SendMessage1: c_msg=[%.256s], slotDir=[%p]...\n",
                              (unsigned long) thisTid, ro, c_msg, slotDir);
    fflush(stderr);
#endif

                result_obj=rtc->SendMessage1(ro, c_msg, slotDir);   // no arguments; but supply callback directory
            }


#if defined (RGF_TRUE) || defined (DEBUG_40) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "...jniRexxSendMessageToRexxObject(), NOARGS-branch: thisTid=[%lu], ... SendMessage{0|1}: c_msg=[%.256s], result_obj=[%p] ...\n",
                              (unsigned long) thisTid, c_msg, result_obj);
    fflush(stderr);
#endif
        }
        else
        {

// fprintf(stderr, "...jniRexxSendMessageToRexxObject(): ARGS-branch...\n"); fflush(stderr);

                // process Java argument array, get matching RexxArrayObject
                // 20150721, have BSF registry reference counter increased
            RexxArrayObject ra=RgfProcessJArgs(env, rtc, j_args, slotDir, TRUE, 1);
        #if defined (RGF_TRUE) /// <============
            // if (env->ExceptionCheck())
            fprintf(stderr, "\\\\\\\\ 09 - tid=[%lu], env->ExceptionCheck()=[%d] //// \n", (unsigned long) thisTid, env->ExceptionCheck()); fflush(stderr);
        #endif                 /// <============


// fprintf(stderr, "...jniRexxSendMessageToRexxObject(): ARGS-branch, AFTER RgfProcessJArgs(...), ra=[%p]...\n", ra); fflush(stderr);

#if defined (RGF_TRUE) || defined (DEBUG_40) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "...jniRexxSendMessageToRexxObject(): thisTid=[%lu]... ro=[%p], SendMessage: c_msg=[%.256s], ra=[%p], size=[%u] ...\n",
                                       (unsigned long) thisTid, ro, c_msg, ra, (unsigned) rtc->ArraySize(ra));
    fflush(stderr);
#endif

#if defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
   fprintf(stderr, "...jniRexxSendMessageToRexxObject(): BEFORE SendMessage(), ro=[%p], c_msg=[%.256s], ra=[%p], size=[%u]...\n",ro, c_msg, ra, rtc->ArraySize(ra));fflush(stderr);
#endif

            result_obj=rtc->SendMessage(ro, c_msg, ra); // with arguments
            // rgf, 201310 rtc->ReleaseLocalReference(ra);

#if defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
   fprintf(stderr, "...jniRexxSendMessageToRexxObject(): AFTER SendMessage(),...\n");fflush(stderr);
#endif

#if defined (RGF_TRUE) || defined (DEBUG_40) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "...jniRexxSendMessageToRexxObject(): thisTid=[%lu]... SendMessage: c_msg=[%.256s], ra=[%p], size=[%u]; result_obj=[%p] ...\n",
                                       (unsigned long) thisTid, c_msg, ra, (unsigned) rtc->ArraySize(ra), result_obj);
    fflush(stderr);
#endif
        }
    }

// fprintf(stderr, "...jniRexxSendMessageToRexxObject(): BEFORE freeing 'c_msg'...\n"); fflush(stderr);

    if (c_msg!=NULL)                    // did we use it, if so, free allocated memory
    {
        RexxFreeMemory(c_msg);          // make sure we free the allocated memory
    }

    RexxFreeMemory(c_rii_ID);           // make sure we free the allocated memory
    RexxFreeMemory(c_obj_ID);           // make sure we free the allocated memory

    // check for exception; if so, create RexxException, supply condition object as a RexxProxy; clear exception in thread
    if (rtc->CheckCondition())      // a Rexx condition raised?
    {

#if defined (RGF_TRUE) || defined (DEBUG40)  || defined (DEBUG_REXX_PROXY) || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "...jniRexxSendMessageToRexxObject(): thisTid=[%lu]... oops, pending Rexx exception, about to throw a Java exception' ...\n",
                                    (unsigned long) tid);
    fflush(stderr);
#endif

        RexxDirectoryObject condObj=rtc->GetConditionInfo();
        char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "/routine/jniRexxSendMessageToRexxObject(), error 4");
        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             rtc, // rtc,
                                                             condObj, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        // rgf, 201310 rtc->ReleaseLocalReference(condObj);
        // rgf, 201310 rtc->ReleaseLocalReference(result_obj);
        // rgf, 201310 rtc->ReleaseLocalReference(ro);
        // rgf, 201310 rtc->ReleaseLocalReference(slotDir);

#if defined (RGF_TRUE) || defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "<-------------- ooRexx->DetachThread() - jniRexxSendMessageToRexxObject... "); fflush(stderr);
#endif

        rtc->DetachThread();

#if defined (RGF_TRUE) || defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "done. <--------------\n"); fflush(stderr);
#endif

        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined (RGF_TRUE) || defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_REXX_PROXY)  || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "<-- returning: ...jniRexxSendMessageToRexxObject(...): thisTid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",(unsigned long) tid, msg);fflush(stderr);
#endif

        RexxFreeMemory(msg);

/* ---> rgf, 20140517
        RgfAcquireLock();
        environmentDetachFrom(&param, FALSE);
        RgfReleaseLock();
<--- */

        return NULL;
    }


#if defined (RGF_TRUE) || defined(DEBUG_JNI) || defined (DEBUG_40) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "<-- returning from: .._jniRexxSendMessageToRexxObject(...): thisTid=[%lu], everything o.k.\n",(unsigned long) thisTid);fflush(stderr);
#endif

    jobject jresult=RgfProcessReturnValue4Java(env, jobj, rtc, result_obj, j_returnType);
        #if defined (RGF_TRUE) /// <============
            // if (env->ExceptionCheck())
            fprintf(stderr, "\\\\\\\\ 10 - tid=[%lu], env->ExceptionCheck()=[%d] //// \n", (unsigned long) thisTid, env->ExceptionCheck()); fflush(stderr);
        #endif                 /// <============

#if defined (RGF_TRUE) || defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "<-------------- ooRexx->DetachThread() - jniRexxSendMessageToRexxObject... "); fflush(stderr);
#endif

    // rgf, 201310 rtc->ReleaseLocalReference(result_obj);
    // rgf, 201310 rtc->ReleaseLocalReference(ro);
    // rgf, 201310 rtc->ReleaseLocalReference(slotDir);
    rtc->DetachThread();

#if defined (RGF_TRUE) || defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "done. <--------------\n"); fflush(stderr);
#endif

/* ---> rgf, 20140517
    RgfAcquireLock();
    environmentDetachFrom(&param, FALSE);
    RgfReleaseLock();
<--- */

    return jresult;
}







/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniRexxHaltInterpreterInstance
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxHaltInterpreterInstance
  (JNIEnv *env, jobject jobj, jstring j_rii_ID)
{
#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY)

    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            thisTid=RgfGetTID();

    fprintf(stderr, "--> arrived: .._jniRexxHaltInterpreterInstance(...), thisTid=[%lu]\n", (unsigned long) thisTid);
    fflush(stderr);
#endif

    char *c_rii_ID=NULL;
    RexxInstance *instance=NULL, *tmpInstance=NULL;

    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string
    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    RexxFreeMemory(c_rii_ID);               // make sure we free the allocated memory

    // RgfAcquireLock2(RII_lock);      // lock access to list of RexxInstance instances
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance
    // RgfReleaseLock2(RII_lock);      // lock access to list of RexxInstance instances

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
       // char msg[1024]="";
       char *msg=new char[1024];
       // sprintf(msg, "%.16s/routine/jniRexxHaltInterpreterInstance(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);
       SNPRINTF(msg, 1024, "%.16s/routine/jniRexxHaltInterpreterInstance(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException); // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: .._jniRexxHaltInterpreterInstance(...): thisTid=[%lu], error, cannot retrieve c_rii_ID=instance=[%p], received=tmpInstance=[%p] instead\n",
                     (unsigned long) thisTid, instance, tmpInstance);
    fflush(stderr);
#endif

        return (jint) -1;
    }
    else
    {
#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "--> .._jniRexxHaltInterpreterInstance(...): thisTid=[%lu], c_rii_ID=p=[%p], NOW 'instance->Halt()'\n",
                     (unsigned long) thisTid, instance);
    fflush(stderr);
#endif


#if defined (DEBUG_CREATE_HALT_TERMINATE)
        fprintf(stderr, "===---=== .._jniRexxHaltInterpreterInstance(...): thisTid=[%lu], HALTING: c_rii_ID={%p]\n", (unsigned long) thisTid, instance);
        fflush(stderr);
#endif


// __asm Int 3;    // break
// DebugBreak();


// fprintf(stderr, ".._jniRexxHaltInterpreterInstance(...): BREAK_HERE! \n");fflush(stderr);
// BREAK_HERE_SINGLESTEP();

        instance->Halt();           // signal HALT to all interpreter threads; a void
    }

// TODO: check & clear conditions ?

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning from: .._jniRexxHaltInterpreterInstance(...), thisTid=[%lu]\n",  (unsigned long) thisTid);
    fflush(stderr);
#endif

    return (jint) 0;
}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniRexxTerminateInterpreterInstance
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxTerminateInterpreterInstance
  (JNIEnv *env, jobject jobj, jstring j_rii_ID)
{

#if defined(DEBUG_JNI) || defined (DEBUG40) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_TERMINATE_INTERPRETER) || defined (DEBUG_JNI_ENTRY)
    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            thisTid=RgfGetTID();

    fprintf(stderr, "--> arrived: .._jniRexxTerminateInterpreterInstance(...), thisTid=[%lu]\n", (unsigned long) thisTid);
    fflush(stderr);
#endif

// fprintf(stderr, "\n- - - - - - waiting for input, press a key! tid=[%lu]\n", (unsigned long)thisTid); fflush(stderr);
// char mist=fgetc(stdin);
// fscanf(stdin, "%c", &mist);
// fprintf(stderr, "\n- - - - - - OK, continuing, tid=[%lu], mist=[%d]\n",(unsigned long)thisTid, mist); fflush(stderr);

    char *c_rii_ID=NULL;
    RexxInstance *instance=NULL, *tmpInstance=NULL;

    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(1)  .._jniRexxTerminateInterpreterInstance(...), thisTid=[%lu], c_rii_ID=[%.256s]\n", (unsigned long) thisTid, c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

#if defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(2)  .._jniRexxTerminateInterpreterInstance(...), thisTid=[%lu], c_rii_ID=[%p]: after 'RgfGetRexxInterpreterInstanceFromList(instance)': tmpInstance=[%p] \n", (unsigned long) thisTid, instance, tmpInstance);
    fflush(stderr);
#endif

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {

#if defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, ".._jniRexxTerminateInterpreterInstance(...): thisTid=[%lu], error, instance p=[%p]<>tmpInstance=[%p] instead\n",
                    (unsigned long) thisTid, instance, tmpInstance);
    fflush(stderr);
#endif

        // char msg[1024]="";
        char *msg=new char[1024];
        SNPRINTF(msg, 1024, "%.16s/routine/jniRexxTerminateInterpreterInstance(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;

        env->Throw(j_rexxException); // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "<-- returning: .._jniRexxTerminateInterpreterInstance(...): thisTid=[%lu], error, cannot retrieve instance p=[%p], received=[%p] instead\n",
                    (unsigned long) thisTid, instance, tmpInstance);
    fflush(stderr);
#endif

        RexxFreeMemory(c_rii_ID);               // make sure we free the allocated memory
        return (jint) -1;
    }

// TODO: if primodalRII return (which is needed for maintaining Rexx objects this program uses for managing ProxyObjects for all RIIs)

    else
    {

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "/// .._jniRexxTerminateInterpreterInstance(...): thisTid=[%lu], (priModalInstance=[%p] == instance p=[%p]) ? [%d]\n",
                    (unsigned long) thisTid, pRoot_RII->instance, instance, (pRoot_RII->instance==instance));
    fflush(stderr);
#endif
        if (pRoot_RII->instance == instance)    // do not terminate primodal RII !
        {
            return -2;
        }

// TODO: lock
// RgfAcquireLock2(RII_lock);       // lock access to list of RexxInstance instances
// __asm Int 3;    // break
// DebugBreak();


// fprintf(stderr, ".._jniRexxTerminateInterpreterInstance(...): BREAK_HERE! \n");fflush(stderr);
// BREAK_HERE_SINGLESTEP();


        // remove instance from list
#if defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(3a) .._jniRexxTerminateInterpreterInstance(...): removing Rexx instance from list, thisTid=[%lu], tmpInstance=[%p]\n", (unsigned long) thisTid, tmpInstance);
    fflush(stderr);
#endif

        // 2012-02-07: get RII node, DeleteGlobalRef() for rajo and rexxconf, before structure gets freed
        PSTRUCT_RII struRii=RgfGetRexxInterpreterInstanceStructFromList(tmpInstance);
        jobject riiRajo=NULL;
        jobject riiRexxconf=NULL;
        if (struRii!=NULL) {        // get jobject references
            riiRajo=struRii->rajo;
            riiRexxconf=struRii->rexxconf;
        }

            // remove RII from list, will free RII structure, if found
        RgfRemoveRexxInterpreterInstanceFromList(tmpInstance);

            // o.k. now delete global references to rajo and rexxconf stored with RII structure
        if (riiRajo!=NULL) {
            env->DeleteGlobalRef(riiRajo);
        }
        if (riiRexxconf!=NULL) {
            env->DeleteGlobalRef(riiRexxconf);
        }

        // remove instance from list
#if defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(3b) .._jniRexxTerminateInterpreterInstance(...): after*** Rexx instance from list, thisTid=[%lu], tmpInstance=[%p]\n", (unsigned long) thisTid, tmpInstance);
    fflush(stderr);
#endif

    // ---rgf, 20090903: remove all created Rexx proxy objects created in the given RII_ID
    // global variable: int bsfInvokedBy = 0;     // 0=noJVM, 1=byJava, 2=byRexx
// TODO: removing RII's proxy objects independent of how BSF4ooRexx got started
//    if (bsfInvokedBy==1)    // BSF4Rexx invoked by Java, then o.k. to remove the cached Rexx objects
    {

#if defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)
        fprintf(stderr, "---> ---> ---> ");
        fprintf(stderr, "(3c) .._jniRexxTerminateInterpreterInstance(...): BEFORE** RgfRemoveAllProxyObjectsByRIID...\n");
        fflush(stderr);
#endif

        int32_t nrFreed=RgfRemoveAllProxyObjectsByRIID(tmpInstance, c_rii_ID);

#ifdef RGF_ATTACH_NEW
        defaultJVM->jvm->DetachCurrentThread();     // speculation: once returned from the JNI invocation this thread is not attached to the JVM anymore
#endif

#if defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)
        fprintf(stderr, "---> ---> ---> ");
        fprintf(stderr, "(3c) .._jniRexxTerminateInterpreterInstance(...): after*** RgfRemoveAllProxyObjectsByRIID, nrFreed=[%d] (cached ooRexx objects)\n", nrFreed);
        fflush(stderr);
#endif


//        return -2;                  // indicate that not processed
    }



#if defined (DEBUG40) || defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(4a) .._jniRexxTerminateInterpreterInstance(...): before Terminate(), thisTid=[%lu], c_rii_ID=tmpInstance=[%p]\n", (unsigned long) thisTid, tmpInstance);
    fflush(stderr);
#endif

    tmpInstance->Terminate();    // wait until all interpreter threads terminated

#if defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(4b) .._jniRexxTerminateInterpreterInstance(...): AFTER Terminate(), thisTid=[%lu], tmpInstance=[%p]\n", (unsigned long) thisTid, tmpInstance);
    fflush(stderr);
#endif


            // now take care of removing the JRST and all dependants of this instance
        #ifdef UNIX
            pthread_t
        #else   // WIN32
            TID
        #endif
                tid=RgfGetTID();   // get current TID

#if defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(6a) .._jniRexxTerminateInterpreterInstance(...), thisTid=[%lu], c_rii_ID=[%p], before removeALL_RIID_nodes ...\n",
                    (unsigned long) thisTid, instance ); //, a_JRST);
    fflush(stderr);
#endif

#ifndef RGF_ATTACH_NEW
        RgfAcquireLock();
        removeAll_RIID_nodes(env, (void *) instance);
        RgfReleaseLock();
#endif


#if defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(6b) .._jniRexxTerminateInterpreterInstance(...), thisTid=[%lu], c_rii_ID=[%p], after removeALL_RIID_nodes .\n",
                    (unsigned long) thisTid, instance); // , tmpRes);
    fflush(stderr);
#endif

    }

    RexxFreeMemory(c_rii_ID);               // make sure we free the allocated memory

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_TERMINATE_INTERPRETER)

    fprintf(stderr, "<-- returning from: .._jniRexxTerminateInterpreterInstance(...), thisTid=[%lu]\n", (unsigned long) thisTid);
    fflush(stderr);
#endif

// TODO: ---rgf, 20140518detach thread from JVM at this point? Only if primodal thread?

    return (jint) 0;
}





/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniUnregisterRexxObject
 * Signature: (Ljava/lang/String;)I

    returns value received from RgfRemoveProxyObject():

          >0  still references left
          =0  no more references left, entries got removed from registry
          -1  Rexx proxy object could not be found
          -2  Java was invoked by Rexx, do not free proxies, see comment inline
        -100  No RexxInterpreter instance could be found
        -101  thread could not be attached to RexxInterpreter instance

 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniUnregisterRexxObject
  (JNIEnv *env, jobject jobj, jstring obj_ID)
{
#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_UNREGISTER_REXX_OBJECT) || defined (DEBUG_JNI_ENTRY)

    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            thisTid=RgfGetTID();


    fprintf(stderr, "*** --> arrived: .._jniRexxUnregisterRexxObject(...), thisTid=[%lu]\n",  (unsigned long) thisTid);
    fflush(stderr);
#endif


    // ---rgf, 20090903: unfortunately, if a Rexx program has been finished, cleaning up cached RexxProxy
    //                   objects may cause exceptions in the JVM when running Java object's finalizer
    //                   methods (as if ooRexx 4.0.0 would go away without warning in the middle of processing);
    //
    //          -- new logic for the time being: if BSF4Rexx was invoked by Rexx, then do not free cached
    //             Rexx objects to avoid the above situation
    //
    //          -- if Java invoked BSF4Rexx, then upon using RexxEngine.terminate(), all cached Rexx objects
    //             created by that RexxEngine's Rexx interpreter instance will get freed, such that those
    //             Rexx objects can be freed (and their uninit's, if any, could be run); this will allow
    //             a rather conservative usage of Rexx object resources in long running Java hosts
    //
    //  TODO: - check on future version of ooRexx (> 4.0.0), if that behaviour can be circumvented/changed
    //        - it would be possible to define a public ooRexx routine to free those cached Rexx objects in
    //          case those Rexx programs are long running as well and employing a lot of RexxProxy objects
    //          (maybe quite unlikely, hence not implementing at the time of this writing)
    //
    // global variable: int bsfInvokedBy = 0;     // 0=noJVM, 1=byJava, 2=byRexx
    if (bsfInvokedBy==2)    // BSF4Rexx invoked by Rexx (Java loaded via Rexx) ?
    {
        return -2;                  // indicate that not processed
    }


    int32_t result=0;

    if (obj_ID==NULL)   // create Java exception !
    {
        // char msg[1024]="";
        char *msg=new char[1024];
        // sprintf(msg, "%.16s/routine/jniUnregisterRexxObject(), error 1: object ID of Rexx proxy must not be 'NULL'", DLLNAME);
        SNPRINTF(msg, 1024, "%.16s/routine/jniUnregisterRexxObject(), error 1: object ID of Rexx proxy must not be 'NULL'", DLLNAME);

        // JNU_ThrowByName(env, "org.apache.bsf.BSFException", msg);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException); // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_UNREGISTER_REXX_OBJECT)
    fprintf(stderr, "*** <-- returning: .._jniRexxUnregisterRexxObject(...): thisTid=[%lu], error, obj_ID=[NULL] !\n",
                                        (unsigned long) thisTid);
    fflush(stderr);
#endif

        return (jint) -1;
    }
    else
    {
        // make a copy of the Java string containing the Rexx script to be executed
        const char      *str = env->GetStringUTFChars(obj_ID, JNI_FALSE);
        char      *c_obj_ID  = (char *) RexxAllocateMemory(strlen(str)+1) ; // allocate memory
        strcpy(c_obj_ID, str);                      // copy data from JNI area
        env->ReleaseStringUTFChars(obj_ID, str);    // release str

        result=RgfRemoveProxyObject(c_obj_ID);      // not found: -1; else remaining refs: >=0
if (result==-1)
{
    fprintf(stderr, "*** PANIC, PANIC, PANIC: while removing obj_ID=[%s], Rexx object could not be fournd PANIC, PANIC, PANIC ***\n",
                     c_obj_ID);
    fflush(stderr);
}

        RexxFreeMemory(c_obj_ID) ;                  // free the memory again
    }

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_UNREGISTER_REXX_OBJECT)
    fprintf(stderr, "*** <-- returning from: .._jniRexxUnregisterRexxObject(...), thisTid=[%lu]\n",  (unsigned long) thisTid);
    fflush(stderr);
#endif

    return (jint) result;       // return reference counter value
}



// rgf, 2012-02-07: exit and command handlers, cooperating with RexxConfiguration

// ----------------------------------------------------------------------------------------------
// Generic Rexx exit handler function, which will attach to Java, fetch the matching Java exit handler
// and invoke it.
int REXXENTRY rexx_exit_handler_entry (RexxExitContext *context, int exitNumber, int subfunction, void *parmBlock)
{
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
    fprintf(stderr, "*** RGF_INFO: rexx_exit_handler_entry() 1, exitNumber=[%d], subfunction=[%d] ---> ...\n",
                    exitNumber, subfunction
            );
    fflush(stderr);
#endif

    // char msg[1024]="";
    char *msg=new char[1024];


// TODO: rgf, 20140517, do we really still need this?
        // get the RII structure to get access to
    RexxInstance *ri=context->threadContext->instance;
    PSTRUCT_RII struRii=RgfGetRexxInterpreterInstanceStructFromList(ri);
    if (struRii==NULL || struRii->rajo==NULL)
    {

#if defined ( RGF_SYSTEM_EXIT_HANDLER )
    if (exitNumber==RXINI || exitNumber==RXTER)
    {
        fprintf(stderr,  "C++ >> >> >> 1 << << << 'rexx_exit_handler_entry()', [%s] - returning prematurely!\n",(exitNumber==RXINI ? "RXINI" : "RXTER"));fflush(stderr);
    }
#endif

        SNPRINTF(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 1: panic - cannot find Rexx interpreter instance related Java interface objects, exitNumber=[%d], subfunction=[%d] for RexxInterpreterInstance=[%p]",
                            DLLNAME, exitNumber, subfunction, ri);


        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return RXEXIT_RAISE_ERROR;
    }

    // TODO: attach to Java, get RexxConfiguration, use getExitHandler(int), invoke handler, if not null & check result
// -----------------------------------------
#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *) context->threadContext->instance, false, 0};
#else
    // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error};
    STRUCT_PARAM param={0, 0, struRii->rajo, (void *)ri, 0, 0, 0, 0};
#endif

    RgfAcquireLock();

#ifdef RGF_ATTACH_NEW
    environmentAttachToNew(&param);    // attach to Java
#else
    environmentAttachTo(&param, FALSE); // attach to Java
#endif

#if defined ( DEBUG_SHOW_STRUCTURES )
    dumpPARAM(   "rexx_command_exit_entry(), after environmentAttachTo(...): param:", &param);
    #ifdef RGF_ATTACH_NEW
        dumpRAJO(    "rexx_command_exit_entry(), after environmentAttachTo(...), 1: ");
        dumpTID2NODE("rexx_command_exit_entry(), after environmentAttachTo(...), 1: ");
    #endif
#endif

#ifdef RGF_ATTACH_NEW
    if (param.error != 0)   // could not attach to Java
#else
    if (param.error== -1)   // could not attach to Java
#endif
    {
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
    if (exitNumber==RXINI || exitNumber==RXTER)
    {
        fprintf(stderr,  "C++ >> >> >> 2 << << << 'rexx_exit_handler_entry()', [%s] - returning prematurely!\n",(exitNumber==RXINI ? "RXINI" : "RXTER"));fflush(stderr);
    }
#endif
        RgfReleaseLock();
        SNPRINTF(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 2: panic - cannot attach to Java, exit number=[%d], subfunction=[%d] for RexxInterpreterInstance=[%p]",
                            DLLNAME, exitNumber, subfunction, ri );

        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return RXEXIT_RAISE_ERROR;
    }
    RgfReleaseLock();

        // get Java command handler object, if any
    jobject jhandler=param.env->CallObjectMethod(struRii->rexxconf,
                                      defaultJVM->mid_RexxConfiguration_getExitHandler,
                                      (jint) exitNumber);

    if (param.env->ExceptionCheck())
    {
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
    if (exitNumber==RXINI || exitNumber==RXTER)
    {
        fprintf(stderr,  "C++ >> >> >> 3 << << << 'rexx_exit_handler_entry()', [%s] - returning prematurely!\n",(exitNumber==RXINI ? "RXINI" : "RXTER"));fflush(stderr);
    }
#endif
            // raise Rexx condition with Java Throwable
        SNPRINTF(msg, 1024, "%%.16s/internal/rexx_exit_handler_entry(), error 3: Java exception while attempting to fetch the Java exit handler for system exit number=[%d], subfunction=[%d] - [%%s]",
                            exitNumber, subfunction);

        RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
             0,         // RexxExitContext
             context,
             msg);
        RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
        environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif
        RgfReleaseLock();
        delete[] msg;
        return RXEXIT_RAISE_ERROR;
    }

        // check whether jhandler==NULL, if so return RXEXIT_NOT_HANDLED (Java side may have nullified jhandler entry)
    if (jhandler==NULL) {

#if defined ( RGF_SYSTEM_EXIT_HANDLER )
    if (exitNumber==RXINI || exitNumber==RXTER)
    {
        fprintf(stderr,  "C++ >> >> >> 4 (NULL-handler!) << << << 'rexx_exit_handler_entry()', [%s] - returning prematurely!\n",(exitNumber==RXINI ? "RXINI" : "RXTER"));fflush(stderr);
    }
#endif
        RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
        environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif
        RgfReleaseLock();

#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
        fprintf(stderr, "*** RGF_INFO: rexx_exit_handler_entry() 1b, exitNumber=[%d], subfunction=[%d] - CURRENTLY NOT HANDLED BY JAVA, RETURNING \"RXEXIT_NOT_HANDLED\" <--- ...\n",
                        exitNumber, subfunction
                );
#endif

        delete[] msg;
        return RXEXIT_NOT_HANDLED;
    }


        // invoke command handler
            // create slot array argument
    jobjectArray jslot = param.env->NewObjectArray (
                              (jsize) 4,                // needed size (number of elements)
                              defaultJVM->clz_Object,   // type
                              NULL                      // initial value
                              );

        // index [0]: save rajo that created this Rexx interpreter instance
    param.env->SetObjectArrayElement(jslot, (jint) 0, struRii->rajo);
        // index [1]: save RexxConfiguration object of this RII
    param.env->SetObjectArrayElement(jslot, (jint) 1, struRii->rexxconf);

        // index [2]: save RexxExitContext pointer as a jstring
    // const char c_context [ RGF_POINTER_STRING_WIDTH+1 ] = "";
    char *c_context=new char[RGF_POINTER_STRING_WIDTH];

    RgfPointer2String(context, c_context);  // create a string representation of RexxExitContext

    jstring jStr=param.env->NewStringUTF(c_context);
    param.env->SetObjectArrayElement(jslot, (jint) 2, jStr);

        // index [3]: add context pointer type: 0=ThreadContext, 1=MethodContext, 2=FunctionContext, 3=ExitContext
    jStr=param.env->NewStringUTF("3");
    param.env->SetObjectArrayElement(jslot, (jint) 3, jStr);

    param.env->DeleteLocalRef(jStr);

    delete [] c_context;
    jobjectArray jpb=NULL;    // Java version of the parmBlock (Object [])



    // ================== BEGIN: system exit related special handling, before invoking the Java exit handler
    {
        switch (exitNumber)
        {
            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        case RXFNC:    // system exit # 2, "processes calls to external functions"
            {

#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                RXFNCCAL_PARM *pb =(RXFNCCAL_PARM *) parmBlock;
                fprintf(stderr, "---> RXFNC=2: [%d], subfunction: [%d], rxfnc_name=[%.256s]\n",
                                    exitNumber,
                                    subfunction,
                                    pb->rxfnc_name
                                    );
#endif

                if (subfunction==RXFNCCAL)   // 1
                {
                    RXFNCCAL_PARM *pb=(RXFNCCAL_PARM *) parmBlock;

                    jpb = param.env->NewObjectArray (
                                   (jsize) 5,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );

                        // process flags for Java
                    jbooleanArray jflags= param.env->NewBooleanArray((jsize) 3); // needed size (number of elements)
                    jboolean * tmpFlags=param.env->GetBooleanArrayElements(jflags,NULL);
                    tmpFlags[0]=pb->rxfnc_flags.rxfferr;
                    tmpFlags[1]=pb->rxfnc_flags.rxffnfnd;
                    tmpFlags[2]=pb->rxfnc_flags.rxffsub;
                    param.env->ReleaseBooleanArrayElements(jflags, tmpFlags, 0);  // copy back the content, free buffer
                    param.env->SetObjectArrayElement(jpb, (jint) 0, jflags);
                    param.env->DeleteLocalRef(jflags);

                        // process function name
                    CONSTRXSTRING function_name;
                    function_name.strptr   =pb->rxfnc_name;
                    function_name.strlength=pb->rxfnc_namel;
                    jobject jrxfnc_name=JNU_CreateJavaStringFromCONSTRXSTRING(param.env,
                                                 context->threadContext,
                                                &function_name);
                    param.env->SetObjectArrayElement(jpb, (jint) 1, jrxfnc_name);
                    param.env->DeleteLocalRef(jrxfnc_name);

                        // process current queue name
                    CONSTRXSTRING curr_queue_name;
                    curr_queue_name.strptr   =pb->rxfnc_que;
                    curr_queue_name.strlength=pb->rxfnc_quel;
                    jobject jque_name=JNU_CreateJavaStringFromCONSTRXSTRING(param.env,
                                                 context->threadContext,
                                                &curr_queue_name);
                    param.env->SetObjectArrayElement(jpb, (jint) 2, jque_name);
                    param.env->DeleteLocalRef(jque_name);

                        // process arguments
                    jobjectArray jargs=RgfConstrxStringArray2JavaArray(&param, context->threadContext, pb->rxfnc_argv, pb->rxfnc_argc);
                    param.env->SetObjectArrayElement(jpb, (jint) 3, jargs);
                    param.env->DeleteLocalRef(jargs);

                       // index [4]: may be set by the exit handler to a return String (!) value

                }
            }
            break;


            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        case RXCMD:    // system exit # 3, "Process calls to subcommand handlers"
            {

#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                RXCMDHST_PARM *pb =(RXCMDHST_PARM *) parmBlock;
                fprintf(stderr, "---> RXCMD=3: [%d], subfunction: [%d], address=[%.32s], dllname=[%.32s], command=[%.64s]\n",
                                    exitNumber,
                                    subfunction,
                                    pb->rxcmd_address,
                                    pb->rxcmd_dll,
                                    pb->rxcmd_command
                                    );
#endif

                if (subfunction==RXCMDHST)   // 1 - Calls a named subcommand handler
                {
                    RXCMDHST_PARM *pb=(RXCMDHST_PARM *) parmBlock;

                    jpb = param.env->NewObjectArray (
                                   (jsize) 5,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );

                        // process flags for Java
                    jbooleanArray jflags= param.env->NewBooleanArray((jsize) 2); // needed size (number of elements)
                    jboolean * tmpFlags=param.env->GetBooleanArrayElements(jflags,NULL);
                    tmpFlags[0]=pb->rxcmd_flags.rxfcfail;
                    tmpFlags[1]=pb->rxcmd_flags.rxfcerr;
                    param.env->ReleaseBooleanArrayElements(jflags, tmpFlags, 0);  // copy back the content, free buffer
                    param.env->SetObjectArrayElement(jpb, (jint) 0, jflags);
                    param.env->DeleteLocalRef(jflags);


                        // process address name
                    CONSTRXSTRING address_name;
                    address_name.strptr   =pb->rxcmd_address;
                    address_name.strlength=pb->rxcmd_addressl;
                    jstring jaddress=JNU_CreateJavaStringFromCONSTRXSTRING(param.env,
                                                 context->threadContext,
                                                &address_name);
                    param.env->SetObjectArrayElement(jpb, (jint) 1, jaddress);
                    param.env->DeleteLocalRef(jaddress);


                        // process dllname name
                    CONSTRXSTRING dllname_name;
                    dllname_name.strptr   =pb->rxcmd_dll;
                    dllname_name.strlength=pb->rxcmd_dll_len;
                    jstring jdllname=JNU_CreateJavaStringFromCONSTRXSTRING(param.env,
                                                 context->threadContext,
                                                &dllname_name);
                    param.env->SetObjectArrayElement(jpb, (jint) 2, jdllname);
                    param.env->DeleteLocalRef(jdllname);


                        // process command
                    jstring jcommand=JNU_CreateJavaStringFromCONSTRXSTRING(param.env,
                                                 context->threadContext,
                                                &pb->rxcmd_command);
                    param.env->SetObjectArrayElement(jpb, (jint) 3, jcommand);
                    param.env->DeleteLocalRef(jcommand);

                       // index [4]: may be set by the exit handler to a return String (!) value

                }
            }
            break;


            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        case RXMSQ:   // system exit # 4, "External data queue exit"
            {

#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXMSQ=4: [%d], subfunction: [%d]\n", exitNumber, subfunction);
#endif

                if (subfunction==RXMSQPLL)   // 1 - Pulls a line from the external data queue
                {
                    RXMSQPLL_PARM *pb=(RXMSQPLL_PARM *) parmBlock;

                    jpb = param.env->NewObjectArray (
                                   (jsize) 1,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );

                        // index [0]: may be set by the exit handler to return any value

                }

                else if (subfunction==RXMSQPSH) // 2 - Places a line in the external data queue.
                {
                    RXMSQPSH_PARM *pb=(RXMSQPSH_PARM *) parmBlock;

                    jpb = param.env->NewObjectArray (
                                   (jsize) 2,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );

                        // process flags for Java
                    jbooleanArray jflags= param.env->NewBooleanArray((jsize) 1); // needed size (number of elements)
                    jboolean * tmpFlags=param.env->GetBooleanArrayElements(jflags,NULL);
                    tmpFlags[0]=pb->rxmsq_flags.rxfmlifo;

                    param.env->ReleaseBooleanArrayElements(jflags, tmpFlags, 0);  // copy back the content, free buffer
                    param.env->SetObjectArrayElement(jpb, (jint) 0, jflags);
                    param.env->DeleteLocalRef(jflags);

                        // process value to push
                    jobject jvalue=JNU_CreateJavaStringFromCONSTRXSTRING(param.env,
                                                 context->threadContext,
                                                &pb->rxmsq_value);
                    param.env->SetObjectArrayElement(jpb, (jint) 1, jvalue);
                    param.env->DeleteLocalRef(jvalue);
                }

                else if (subfunction==RXMSQSIZ)     // 3 - Returns the number of lines in the external data queue
                {
                    RXMSQSIZ_PARM *pb=(RXMSQSIZ_PARM *) parmBlock;

                    jpb = param.env->NewObjectArray (
                                   (jsize) 1,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );

                        // index [0]: should be set by the Java handler, a String containing the number
                }

                else if (subfunction==RXMSQNAM)     // 20 - Sets the name of the active external data queue
                {
                    RXMSQNAM_PARM *pb=(RXMSQNAM_PARM *) parmBlock;

                    jpb = param.env->NewObjectArray (
                                   (jsize) 1,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );

                        // index [0]: new queue name; be nice and use type CONSTRXSTRING for RXSTRING, although they have the same structure but different const defs
                    CONSTRXSTRING new_queue_name;
                    new_queue_name.strptr   =pb->rxmsq_name.strptr;
                    new_queue_name.strlength=pb->rxmsq_name.strlength;

                    jobject jnewQueueName=JNU_CreateJavaStringFromCONSTRXSTRING(param.env,
                                                 context->threadContext,
                                                &new_queue_name);
                    param.env->SetObjectArrayElement(jpb, (jint) 0, jnewQueueName);
                    param.env->DeleteLocalRef(jnewQueueName);
                }
            }
            break;



            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        case RXSIO:    // system exit # 5, "standard input and output"
            {

#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d], RXSIOSAY=[%d]\n",
                                    exitNumber,subfunction, RXSIOSAY);
#endif

                if (subfunction==RXSIOSAY)   // 1
                {
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d] -> RXSIOSAY\n",
                                    exitNumber,subfunction);
#endif
                    RXSIOSAY_PARM *pb=(RXSIOSAY_PARM *) parmBlock;

                    jpb = param.env->NewObjectArray (
                                   (jsize) 1,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );

                        // process SAY-string
                    jobject jrxsio_string=JNU_CreateJavaStringFromCONSTRXSTRING(param.env,
                                                 context->threadContext,
                                                &pb->rxsio_string);
                    param.env->SetObjectArrayElement(jpb, (jint) 0, jrxsio_string);
                    param.env->DeleteLocalRef(jrxsio_string);
                }

                else if (subfunction==RXSIOTRC)   // 2
                {
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d] -> RXSIOTRC\n",
                                    exitNumber,subfunction);
#endif
                    RXSIOTRC_PARM *pb=(RXSIOTRC_PARM *) parmBlock;

                    jpb = param.env->NewObjectArray (
                                   (jsize) 1,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );

                        // process SAY-string
                    jobject jrxsio_string=JNU_CreateJavaStringFromCONSTRXSTRING(param.env,
                                                 context->threadContext,
                                                &pb->rxsio_string);
                    param.env->SetObjectArrayElement(jpb, (jint) 0, jrxsio_string);
                    param.env->DeleteLocalRef(jrxsio_string);
                }


                else if (subfunction==RXSIOTRD)   // 3
                {
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d] -> RXSIOTRD\n",
                                    exitNumber,subfunction);
#endif
                        // one element for return value (must be a String)
                    jpb = param.env->NewObjectArray (
                                   (jsize) 1,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );
                }

                else if (subfunction==RXSIODTR)   // 4
                {
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d] -> RXSIODTR\n",
                                    exitNumber,subfunction);
#endif
                        // one element for return value (must be a String)
                    jpb = param.env->NewObjectArray (
                                   (jsize) 1,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );
                }

            }
            break;


            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        case RXHLT:    // system exit # 7, "HALT condition processing"
            {
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXHLT=7: [%d], subfunction: [%d]\n", exitNumber, subfunction);
#endif

                if (subfunction==RXHLTTST ||    // 2
                    subfunction==RXHLTCLR)      // 1 - also has a parmBlock (unclear documentation) !!
                {
                    RXHLTTST_PARM *pb=(RXHLTTST_PARM *) parmBlock;
                    jpb = param.env->NewObjectArray (
                                   (jsize) 1,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );

                        // process flag for Java
                    jbooleanArray jflags= param.env->NewBooleanArray((jsize) 1); // needed size (number of elements)
                    jboolean * tmpFlags=param.env->GetBooleanArrayElements(jflags,NULL);
                    tmpFlags[0]=pb->rxhlt_flags.rxfhhalt;
                    param.env->ReleaseBooleanArrayElements(jflags, tmpFlags, 0);  // copy back the content, free buffer
                    param.env->SetObjectArrayElement(jpb, (jint) 0, jflags);
                    param.env->DeleteLocalRef(jflags);
                }
            }
            break;


            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        case RXTRC:    // system exit # 8, "Tests the external trace indicator."
            {
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXTRC=8: [%d], subfunction: [%d]\n", exitNumber, subfunction);
#endif

                if (subfunction==RXTRCTST)      // 1
                {
                    RXTRCTST_PARM *pb =(RXTRCTST_PARM *) parmBlock;
                    jpb = param.env->NewObjectArray (
                                   (jsize) 1,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );

                        // process flag for Java
                    jbooleanArray jflags= param.env->NewBooleanArray((jsize) 1); // needed size (number of elements)
                    jboolean * tmpFlags=param.env->GetBooleanArrayElements(jflags,NULL);
                    tmpFlags[0]=pb->rxtrc_flags.rxftrace;
                    param.env->ReleaseBooleanArrayElements(jflags, tmpFlags, 0);  // copy back the content, free buffer
                    param.env->SetObjectArrayElement(jpb, (jint) 0, jflags);
                    param.env->DeleteLocalRef(jflags);
                }
            }
            break;



            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
            // nothing to setup, so just break
        case RXINI:       // system exit # 9, "initialization processing"
    #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
            {
                fprintf(stderr, "---> RXINI=9, exitNumber: [%d], subfunction: [%d]\n",
                                 exitNumber, subfunction);
            }
    #endif
            break;


            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
            // nothing to setup, so just break
        case RXTER:       // system exit # 10, "termination processing"
    #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
            {
                fprintf(stderr, "---> RXTER=10, exitNumber: [%d], subfunction: [%d]\n",
                                 exitNumber, subfunction);
            }
    #endif
            break;

            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        case RXNOVAL:       // system exit # 13, "processes a Rexx NOVALUE condition"
            {
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXNOVAL=13, exitNumber: [%d], subfunction: [%d]\n",
                                 exitNumber, subfunction);
#endif

                if (subfunction==RXNOVALCALL)   // 1
                {
                        RXVARNOVALUE_PARM *pb=(RXVARNOVALUE_PARM *) parmBlock;
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXNOVAL=13, exitNumber: [%d], subfunction: [%d], variable_name=[%s]\n",
                                 exitNumber, subfunction, context->CString(pb->variable_name));
#endif

                        jpb = param.env->NewObjectArray (
                                       (jsize) 2,                // needed size (number of elements)
                                       defaultJVM->clz_Object,   // type
                                       NULL                      // initial value
                                       );

                        jstring jstr=param.env->NewStringUTF(context->CString(pb->variable_name));
                        param.env->SetObjectArrayElement(jpb, (jint) 0, jstr);
                        param.env->DeleteLocalRef(jstr);
                }
            }
            break;



            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        case RXEXF:     // system exit # 12, "processes calls to external functions, if not found", OO version
        case RXOFNC:    // system exit # 15, "processes calls to external functions, before search starts", OO version
            {
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXEXF=12/RXOFNC=15: [%d], subfunction: [%d], rxfnc_name=[%.256s]\n",
                                    exitNumber,
                                    subfunction,
                                    exitNumber==RXOFNC ?
                                               ((RXOFNCCAL_PARM *) parmBlock)->rxfnc_name.strptr :
                                               ((RXEXFCAL_PARM *)  parmBlock)->rxfnc_name.strptr
                                    );
#endif

                if (subfunction==RXOFNCCAL || subfunction==RXEXFCAL)   // 1
                {
                    CONSTRXSTRING function_name;
                    size_t         argc=0;
                    RexxObjectPtr *argv=NULL;
                    if (exitNumber==RXOFNC)
                    {
                        RXOFNCCAL_PARM *pb=(RXOFNCCAL_PARM *) parmBlock;
                        function_name=pb->rxfnc_name;
                        argc         =pb->rxfnc_argc;
                        argv         =pb->rxfnc_argv;
                    }
                    else
                    {
                        RXEXFCAL_PARM *pb=(RXEXFCAL_PARM *) parmBlock;
                        function_name=pb->rxfnc_name;
                        argc         =pb->rxfnc_argc;
                        argv         =pb->rxfnc_argv;
                    }

                    jpb = param.env->NewObjectArray (
                                   (jsize) 4,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );

                        // process flags for Java
                    jbooleanArray jflags= param.env->NewBooleanArray((jsize) 3); // needed size (number of elements)
                    jboolean * tmpFlags=param.env->GetBooleanArrayElements(jflags,NULL);
                    if (exitNumber==RXOFNC)
                    {
                        RXOFNC_FLAGS flags=((RXOFNCCAL_PARM *) parmBlock)->rxfnc_flags;
                        tmpFlags[0]=flags.rxfferr;
                        tmpFlags[1]=flags.rxffnfnd;
                        tmpFlags[2]=flags.rxffsub;
                    }
                    else
                    {
                        RXEXF_FLAGS flags=((RXEXFCAL_PARM *) parmBlock)->rxfnc_flags;
                        tmpFlags[0]=flags.rxfferr;
                        tmpFlags[1]=flags.rxffnfnd;
                        tmpFlags[2]=flags.rxffsub;
                    }
                    param.env->ReleaseBooleanArrayElements(jflags, tmpFlags, 0);  // copy back the content, free buffer
                    param.env->SetObjectArrayElement(jpb, (jint) 0, jflags);
                    param.env->DeleteLocalRef(jflags);

                        // process function name
                    jobject jrxfnc_name=JNU_CreateJavaStringFromCONSTRXSTRING(param.env, context->threadContext,
                                                &function_name);

                    param.env->SetObjectArrayElement(jpb, (jint) 1, jrxfnc_name);
                    param.env->DeleteLocalRef(jrxfnc_name);

                        // create RexxArrayObject from RexxObjectPtr list and store it as a Java array object
                    RexxArrayObject rao=context->NewArray(argc);
                    for (size_t i=0; i<argc; i++)
                    {
                        context->ArrayPut(rao, argv[i], i+1);
                    }
                    jobjectArray jao=RgfRexxArray2JavaArray(&param, context->threadContext, rao);
                    param.env->SetObjectArrayElement(jpb, (jint) 2, jao);
                    param.env->DeleteLocalRef(jao);

                    // fourth element remains NULL, needs to be set by handler, if a result is returned
                }
            }
            break;



            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        case RXVALUE:   // system exit # 14, "extends the environments available to the VALUE() built-in function."
            {
                RXVALCALL_PARM *pb=(RXVALCALL_PARM *) parmBlock;

#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXVALUE=14: [%d], subfunction: [%d] - selector=[%s], variable_name=[%s], valuep=[%p]\n",
                                    exitNumber, subfunction,
                                    context->CString(pb->selector),
                                    context->CString(pb->variable_name),
                                    pb->value
                                    );
#endif

                if (subfunction==RXVALUECALL )   // 1
                {
                    jpb = param.env->NewObjectArray (
                                   (jsize) 3,                // needed size (number of elements)
                                   defaultJVM->clz_Object,   // type
                                   NULL                      // initial value
                                   );

                        // process selector
                    jstring jstr=param.env->NewStringUTF(context->CString(pb->selector));
                    param.env->SetObjectArrayElement(jpb, (jint) 0, jstr);
                    param.env->DeleteLocalRef(jstr);

                        // process variable_name
                    jstr=param.env->NewStringUTF(context->CString(pb->variable_name));
                    param.env->SetObjectArrayElement(jpb, (jint) 1, jstr);
                    param.env->DeleteLocalRef(jstr);

                        // process value
                    jobject jo= RgfRexxObject2JavaObject(&param,context->threadContext,pb->value);
                    param.env->SetObjectArrayElement(jpb, (jint) 2,jo);
                    param.env->DeleteLocalRef(jo);
                }
            }
            break;


        default:            // unknown handler, return immediately with RXEXIT_NOT_HANDLED

#if defined ( RGF_SYSTEM_EXIT_HANDLER )
    if (exitNumber==RXINI || exitNumber==RXTER)
    {
        fprintf(stderr,  "C++ >> >> >> 5 (default:) << << << 'rexx_exit_handler_entry()', [%s] - returning prematurely!\n",(exitNumber==RXINI ? "RXINI" : "RXTER"));fflush(stderr);
    }
#endif
            param.env->DeleteLocalRef(jhandler);
            param.env->DeleteLocalRef(jslot);
            param.env->DeleteLocalRef(jpb);

            RgfAcquireLock();

#ifdef RGF_ATTACH_NEW
            environmentDetachFromNew(&param);
#else
            environmentDetachFrom(&param, FALSE);    // do not close group
#endif

            RgfReleaseLock();
            delete[] msg;
            return RXEXIT_NOT_HANDLED;
        }
    }
    // ================== END:   system exit related special handling, before invoking the Java exit handler



        // a Rexx condition raised while preparing data for the Java handler? If so, return immediately
    if (context->CheckCondition()) {
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
    if (exitNumber==RXINI || exitNumber==RXTER)
    {
        fprintf(stderr,  "C++ >> >> >> 6 (Rexx-condition) << << << 'rexx_exit_handler_entry()', [%s] - returning prematurely!\n",(exitNumber==RXINI ? "RXINI" : "RXTER"));fflush(stderr);
    }
#endif
        RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
        environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif
        RgfReleaseLock();
        delete[] msg;
        return RXEXIT_RAISE_ERROR;
    }



        // public abstract Object handleCommand(String address, String command, Object[] slot);
    jobject jresult=NULL;

    int j_exit_rc=0;

#ifndef CONFIG_REXX_HANDLERS_AS_INTERFACES    // rgf, 2012-02-17
    j_exit_rc=(jint) param.env->CallIntMethod(jhandler,
                                      defaultJVM->mid_RexxExitHandler_handleExit,
                                      jslot,
                                      exitNumber,
                                      subfunction,
                                      jpb
                                      );
#else       // invoke the handler method in the object, if not raise syntax error
    jmethodID jmid=param.env->GetMethodID(param.env->GetObjectClass(jhandler),
                                          CONFIG_REXX_EXIT_HANDLER_NAME,
                                          CONFIG_REXX_EXIT_HANDLER_SIGNATURE
                                         );

    if (param.env->ExceptionCheck())
    {
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
    if (exitNumber==RXINI || exitNumber==RXTER)
    {
        fprintf(stderr,  "C++ >> >> >> 7 (Java-condition) << << << 'rexx_exit_handler_entry()', [%s] - returning prematurely!\n",(exitNumber==RXINI ? "RXINI" : "RXTER"));fflush(stderr);
    }
#endif

        param.env->ExceptionClear();    // clear the Java exception

        param.env->DeleteLocalRef(jhandler);
        param.env->DeleteLocalRef(jslot);
        param.env->DeleteLocalRef(jpb);

        RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
        environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif
        RgfReleaseLock();

        SNPRINTF(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 4: Java exception, cannot find the Java Rexx exit handler method named [%s], needed for processing the Rexx system exit number=[%d], subfunction=[%d]",
                            DLLNAME, CONFIG_REXX_EXIT_HANDLER_NAME, exitNumber, subfunction);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));

        delete[] msg;
        return RXEXIT_RAISE_ERROR;
    }

    j_exit_rc=(jint) param.env->CallIntMethod(jhandler,
                                      jmid, // mid_RexxExitHandler_handleExit
                                      jslot,
                                      exitNumber,
                                      subfunction,
                                      jpb
                                      );

#endif

        // check for Java exception
    if (param.env->ExceptionCheck())
    {
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
    if (exitNumber==RXINI || exitNumber==RXTER)
    {
        fprintf(stderr,  "C++ >> >> >> 8 (Java-condition) << << << 'rexx_exit_handler_entry()', [%s] - returning prematurely!\n",(exitNumber==RXINI ? "RXINI" : "RXTER"));fflush(stderr);
    }
#endif
            // raise Rexx condition with Java Throwable
        SNPRINTF(msg, 1024, "%%.16s/internal/rexx_exit_handler_entry(), error 5: Java exception while running the Java exit handler for system exit number=[%d], subfunction=[%d] - [%%s]",
                            exitNumber, subfunction);

        RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
             0,         // RexxExitContext
             context,
             msg);

        param.env->DeleteLocalRef(jhandler);
        param.env->DeleteLocalRef(jslot);
        param.env->DeleteLocalRef(jpb);

        RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
        environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif
        RgfReleaseLock();
        delete[] msg;
        return RXEXIT_RAISE_ERROR;
    }


        // a Rexx condition raised by the Java handler? If so, return immediately
    if (context->CheckCondition()) {
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
    if (exitNumber==RXINI || exitNumber==RXTER)
    {
        fprintf(stderr,  "C++ >> >> >> 9 (Rexx-condition) << << << 'rexx_exit_handler_entry()', [%s] - returning prematurely!\n",(exitNumber==RXINI ? "RXINI" : "RXTER"));fflush(stderr);
    }
#endif

        param.env->DeleteLocalRef(jhandler);
        param.env->DeleteLocalRef(jslot);
        param.env->DeleteLocalRef(jpb);

        RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
        environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif
        RgfReleaseLock();
        delete[] msg;
        return RXEXIT_RAISE_ERROR;
    }



    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    if (j_exit_rc != RXEXIT_NOT_HANDLED)    // o.k. exit handler says it handled exit; do we need to do something ?
    {

        // ================== BEGIN: system exit related special handling, before returning to Rexx
        {
            switch (exitNumber)
            {
                // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            case RXFNC:    // system exit # 2, "processes calls to external functions"
                {
                    RXFNCCAL_PARM *pb =(RXFNCCAL_PARM *) parmBlock;

    #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                    fprintf(stderr, "<--- RXFNC=2: [%d], subfunction: [%d], rxfnc_name=[%.256s]\n",
                                        exitNumber,
                                        subfunction,
                                        pb->rxfnc_name
                                        );
    #endif
                    if (subfunction==RXFNCCAL)   // 1
                    {
                        RXFNCCAL_PARM *pb=(RXFNCCAL_PARM *) parmBlock;

                            // get flags from Java and assign their values to Rexx
                        jbooleanArray jflags= (jbooleanArray) param.env->GetObjectArrayElement(jpb, (jint) 0);
                        jboolean * tmpFlags=param.env->GetBooleanArrayElements(jflags,NULL);
                        pb->rxfnc_flags.rxfferr =tmpFlags[0];
                        pb->rxfnc_flags.rxffnfnd=tmpFlags[1];
                        pb->rxfnc_flags.rxffsub =tmpFlags[2];
                        param.env->ReleaseBooleanArrayElements(jflags, tmpFlags, 0);  // copy back the content, free buffer
                        param.env->DeleteLocalRef(jflags);

                            // get result, if any, turn it into a RexxObjectPtr
                        jobject jresult=param.env->GetObjectArrayElement(jpb, (jint) 4);
                        if (jresult==NULL )     // return an empty Rexx string
                        {
                            pb->rxfnc_retc.strlength=0;
                        }

                            // an error
                            // TODO: could get Java object's String value and return that ?
                        else  if ( param.env->IsInstanceOf(jresult, defaultJVM->clz_String)==JNI_FALSE )
                        {
                            param.env->DeleteLocalRef(jresult);

                            pb->rxfnc_flags.rxfferr =1; // indicate an error occurred
                            SNPRINTF(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 6: Java RXFNC=2 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object",
                                                DLLNAME, exitNumber, subfunction);
                            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                        }

                        else    // o.k., process Java String object
                        {
                            JNU_JavaString2pRxString(param.env, (jstring) jresult, &pb->rxfnc_retc);
                            param.env->DeleteLocalRef(jresult);
                        }
                    }
                }
                break;


                // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            case RXCMD:    // system exit # 3, "Process calls to subcommand handlers"
                {

    #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                    RXCMDHST_PARM *pb =(RXCMDHST_PARM *) parmBlock;
                    fprintf(stderr, "<--- RXCMD=3: [%d], subfunction: [%d], address=[%.32s], dllname=[%.32s], command=[%.64s]\n",
                                        exitNumber,
                                        subfunction,
                                        pb->rxcmd_address,
                                        pb->rxcmd_dll,
                                        pb->rxcmd_command
                                        );
    #endif

                    if (subfunction==RXCMDHST)   // 1 - Calls a named subcommand handler
                    {
                        RXCMDHST_PARM *pb=(RXCMDHST_PARM *) parmBlock;

                            // get flags from Java and assign their values to Rexx
                        jbooleanArray jflags= (jbooleanArray) param.env->GetObjectArrayElement(jpb, (jint) 0);
                        jboolean * tmpFlags=param.env->GetBooleanArrayElements(jflags,NULL);
                        pb->rxcmd_flags.rxfcfail =tmpFlags[0];
                        pb->rxcmd_flags.rxfcerr  =tmpFlags[1];

                        param.env->ReleaseBooleanArrayElements(jflags, tmpFlags, 0);  // copy back the content, free buffer
                        param.env->DeleteLocalRef(jflags);

                            // get result, if any, turn it into a RexxObjectPtr
                        jobject jresult=param.env->GetObjectArrayElement(jpb, (jint) 4);
                        if (jresult==NULL )     // return an empty Rexx string
                        {
                            pb->rxcmd_retc.strlength=0;
                        }

                            // an error
                            // TODO: could get Java object's String value and return that ?
                        else  if ( param.env->IsInstanceOf(jresult, defaultJVM->clz_String)==JNI_FALSE )
                        {
                            param.env->DeleteLocalRef(jresult);

                            pb->rxcmd_flags.rxfcerr =1; // indicate an error occurred
                            SNPRINTF(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 7: Java RXCMD=3 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object",
                                                DLLNAME, exitNumber, subfunction);
                            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                        }

                        else    // o.k., process Java String object
                        {
                            JNU_JavaString2pRxString(param.env, (jstring) jresult, &pb->rxcmd_retc);
                            param.env->DeleteLocalRef(jresult);
                        }
                    }
                }
                break;



                // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            case RXMSQ:   // system exit # 4, "External data queue exit"
                {

    #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                    fprintf(stderr, "<--- RXMSQ=4: [%d], subfunction: [%d]\n", exitNumber, subfunction);
    #endif

                    if (subfunction==RXMSQPLL)   // 1 - Pulls a line from the external data queue
                    {
                        RXMSQPLL_PARM *pb=(RXMSQPLL_PARM *) parmBlock;

                            // get result, if any, turn it into a RexxObjectPtr
                        jobject jresult=param.env->GetObjectArrayElement(jpb, (jint) 0);
                        if (jresult==NULL )     // return an empty Rexx string
                        {
                            pb->rxmsq_retc.strlength=0;
                        }

                            // an error
                            // TODO: could get Java object's String value and return that ?
                        else  if ( param.env->IsInstanceOf(jresult, defaultJVM->clz_String)==JNI_FALSE )
                        {
                            param.env->DeleteLocalRef(jresult);

                            SNPRINTF(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 8: Java RXMSQ=4 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object",
                                                DLLNAME, exitNumber, subfunction);
                            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                        }

                        else    // o.k., process Java String object
                        {
                            JNU_JavaString2pRxString(param.env, (jstring) jresult, &pb->rxmsq_retc);
                            param.env->DeleteLocalRef(jresult);
                        }
                    }

                    else if (subfunction==RXMSQSIZ)     // 3 - Returns the number of lines in the external data queue
                    {
                        RXMSQSIZ_PARM *pb=(RXMSQSIZ_PARM *) parmBlock;

                            // get result, if any, turn it into a RexxObjectPtr
                        jobject jresult=param.env->GetObjectArrayElement(jpb, (jint) 0);
                        if (jresult==NULL )     // return an empty Rexx string
                        {
                            pb->rxmsq_size=0;
                            SNPRINTF(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 9: Java RXMSQ=4 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a value (current size of queue)",
                                                DLLNAME, exitNumber, subfunction);
                            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                        }

                            // an error
                            // TODO: could get Java object's String value and return that ?
                        else  if ( param.env->IsInstanceOf(jresult, defaultJVM->clz_String)==JNI_FALSE )
                        {
                            param.env->DeleteLocalRef(jresult);

                            SNPRINTF(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 10: Java RXMSQ=4 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object",
                                                DLLNAME, exitNumber, subfunction);
                            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                        }

                        else    // o.k., process Java String object, return Rexx string
                        {
                                // turn Java value into size_t
                            char * c_result=(char *) JNU_GetStringNativeChars(param.env, (jstring) jresult);   // convert to native string
                            param.env->DeleteLocalRef(jresult);

                            size_t size=0;
                                // turn into RexxString, then create a size_t from it
                            size_t res=context->ObjectToStringSize(context->String(c_result), &size);
                            if (res==0)     // conversion error occurred !
                            {
                                SNPRINTF(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 11: Java RXMSQ=4 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object representing a valid number (\"size_t\")",
                                                    DLLNAME, exitNumber, subfunction);
                                context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                            }
                            else    // conversion went o.k.
                            {
                                pb->rxmsq_size=size;
                            }
                            RexxFreeMemory(c_result);
                        }
                    }
                }
                break;




                // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            case RXSIO:    // system exit # 5, "standard input and output"
                {

    #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                    fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d]\n",
                                        exitNumber,subfunction);
    #endif

// no post-processing, so just for debugging
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                    if (subfunction==RXSIOSAY)   // 1
                    {
                    fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d] -> RXSIOSAY\n",
                                        exitNumber,subfunction);
                    }
                    else if (subfunction==RXSIOTRC)   // 2
                    {
    #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                    fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d] -> RXSIOTRC\n",
                                        exitNumber,subfunction);
    #endif
                    }
                    else
#endif


                    if (subfunction==RXSIOTRD)   // 3
                    {
    #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                    fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d] -> RXSIOTRD\n",
                                        exitNumber,subfunction);
    #endif
                        RXSIOTRD_PARM *pb=(RXSIOTRD_PARM *) parmBlock;

                            // get result, if any, turn it into a RexxObjectPtr
                        jobject jresult=param.env->GetObjectArrayElement(jpb, (jint) 0);
                        if (jresult==NULL )     // return an empty Rexx string
                        {
                            pb->rxsiotrd_retc.strlength=0;
                        }

                            // an error, if not a String
                            // TODO: could get Java object's String value and return that ?
                        else  if ( param.env->IsInstanceOf(jresult, defaultJVM->clz_String)==JNI_FALSE )
                        {
                            param.env->DeleteLocalRef(jresult);

                            SNPRINTF(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 12: Java RXFNC=2 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object",
                                                DLLNAME, exitNumber, subfunction);
                            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                        }

                        else    // o.k., process Java String object
                        {
                            JNU_JavaString2pRxString(param.env, (jstring) jresult, &pb->rxsiotrd_retc);
                            param.env->DeleteLocalRef(jresult);
                        }
                    }

                    else if (subfunction==RXSIODTR)   // 4
                    {
    #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                    fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d] -> RXSIODTR\n",
                                        exitNumber,subfunction);
    #endif
                        RXSIODTR_PARM *pb=(RXSIODTR_PARM *) parmBlock;

                            // get result, if any, turn it into a RexxObjectPtr
                        jobject jresult=param.env->GetObjectArrayElement(jpb, (jint) 0);
                        if (jresult==NULL )     // return an empty Rexx string
                        {
                            pb->rxsiodtr_retc.strlength=0;
                        }

                            // an error, if not a String
                            // TODO: could get Java object's String value and return that ?
                        else  if ( param.env->IsInstanceOf(jresult, defaultJVM->clz_String)==JNI_FALSE )
                        {
                            param.env->DeleteLocalRef(jresult);

                            SNPRINTF(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 13: Java RXFNC=2 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object",
                                                DLLNAME, exitNumber, subfunction);
                            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                        }

                        else    // o.k., process Java String object
                        {
                            JNU_JavaString2pRxString(param.env, (jstring) jresult, &pb->rxsiodtr_retc);
                            param.env->DeleteLocalRef(jresult);
                        }
                    }

                }
                break;


                // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            case RXHLT:    // system exit # 7, "HALT condition processing"
                {
    #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                    fprintf(stderr, "<--- RXHLT=7: [%d], subfunction: [%d]\n", exitNumber, subfunction);
    #endif

                    if (subfunction==RXHLTTST ||    // 2
                        subfunction==RXHLTCLR)      // 1 - TODO: does this have a parmBlock ??
                    {
                        RXHLTTST_PARM *pb=(RXHLTTST_PARM *) parmBlock;

                            // get flags from Java and assign their values to Rexx
                        jbooleanArray jflags= (jbooleanArray) param.env->GetObjectArrayElement(jpb, (jint) 0);
                        jboolean * tmpFlags=param.env->GetBooleanArrayElements(jflags,NULL);
                        pb->rxhlt_flags.rxfhhalt =tmpFlags[0];
                        param.env->ReleaseBooleanArrayElements(jflags, tmpFlags, 0);  // copy back the content, free buffer
                        param.env->DeleteLocalRef(jflags);
                    }
                }
                break;


                // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            case RXTRC:    // system exit # 8, "Tests the external trace indicator"
                {
    #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                    fprintf(stderr, "<--- RXTRC=8: [%d], subfunction: [%d]\n", exitNumber, subfunction);
    #endif
                    if (subfunction==RXTRCTST)      // 1 - Tests the external trace indicator
                    {
                        RXTRCTST_PARM *pb =(RXTRCTST_PARM *) parmBlock;

                            // get flags from Java and assign their values to Rexx
                        jbooleanArray jflags= (jbooleanArray) param.env->GetObjectArrayElement(jpb, (jint) 0);
                        jboolean * tmpFlags=param.env->GetBooleanArrayElements(jflags,NULL);
                        pb->rxtrc_flags.rxftrace =tmpFlags[0];
                        param.env->ReleaseBooleanArrayElements(jflags, tmpFlags, 0);  // copy back the content, free buffer
                        param.env->DeleteLocalRef(jflags);
                    }
                }
                break;


                // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )

                // nothing to setup
            case RXINI:       // system exit # 9, "initialization processing"
                {
                    fprintf(stderr, "<--- RXINI=9, exitNumber: [%d], subfunction: [%d]\n",
                                     exitNumber, subfunction);
                }
                break;
        #endif


                // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )

                // nothing to post process
            case RXTER:       // system exit # 10, "termination processing"
                {
                    fprintf(stderr, "<--- RXTER=10, exitNumber: [%d], subfunction: [%d]\n",
                                     exitNumber, subfunction);
                }
                break;
        #endif


                    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                case RXNOVAL:   // system exit # 13, "processes a Rexx NOVALUE condition"
                    {
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                        fprintf(stderr, "<--- RXNOVAL=13, exitNumber: [%d], subfunction: [%d], RXEXIT_HANDLED\n",
                                          exitNumber, subfunction);
#endif

                        if (subfunction==RXNOVALCALL)   // 1
                        {
                            RXVARNOVALUE_PARM *pb=(RXVARNOVALUE_PARM *) parmBlock;
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                        fprintf(stderr, "<--- RXNOVAL=13, exitNumber: [%d], subfunction: [%d], RXEXIT_HANDLED: variable_name=[%s]\n",
                                          exitNumber, subfunction, context->CString(pb->variable_name));
#endif

                            jobject jvalue= param.env->GetObjectArrayElement(jpb, (jint) 1);

                            RexxObjectPtr rop=RgfJavaObject2RexxObject (param.env,
                                                       NULL,      // RexxAndJava object, if NULL will be fetched in routine
                                                       context->threadContext,    // RexxThreadContext
                                                       jvalue,            // Java object to turn into a RexxObjectPtr
                                                       FALSE              // if FALSE, do not load .BSF, if not found
                                                       );
                            param.env->DeleteLocalRef(jvalue);

                            if (rop==NULL)  // Java returned NULL, so we return .nil
                            {
                                rop=context->Nil();
                            }
                            pb->value=rop;
                        }
                    }
                    break;



                // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                case RXEXF:     // system exit # 12, "processes calls to external functions, if not found", OO version
                case RXOFNC:    // system exit # 15, "processes calls to external functions, before search starts", OO version
                    {

#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                        fprintf(stderr, "<--- RXEXF=12/RXOFNC=15, exitNumber: [%d], subfunction: [%d], RXEXIT_HANDLED\n",
                                           exitNumber,
                                           subfunction);
#endif

                        if (subfunction==RXOFNCCAL || subfunction==RXEXFCAL)   // 1
                        {
#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
       fprintf(stderr, "C++ ");

                        fprintf(stderr, "<--- RXEXF=12/RXOFNC=15, exitNumber: [%d], subfunction: [%d], RXEXIT_HANDLED, rxfnc_name=[%.256s], j_exit_rc=[%d]=[%s]\n",
                                           exitNumber,
                                           subfunction,
                                           exitNumber==RXOFNC ?
                                                      ((RXOFNCCAL_PARM *) parmBlock)->rxfnc_name.strptr :
                                                      ((RXEXFCAL_PARM *)  parmBlock)->rxfnc_name.strptr
                                           , j_exit_rc
                                           , (j_exit_rc==RXEXIT_HANDLED ? "RXEXIT_HANDLED" : "RXEXIT_RAISE_ERROR")
                                           );
#endif

                            RexxObjectPtr retc=NULL;

                            size_t         argc=0;
                            RexxObjectPtr *argv=NULL;

                                // get flags from Java and assign their values to Rexx
                            jbooleanArray jflags= (jbooleanArray) param.env->GetObjectArrayElement(jpb, (jint) 0);
                            jboolean * tmpFlags=param.env->GetBooleanArrayElements(jflags,NULL);

                            if (exitNumber==RXOFNC)
                            {
                                RXOFNC_FLAGS *flags=&((RXOFNCCAL_PARM *) parmBlock)->rxfnc_flags;
                                flags->rxfferr =tmpFlags[0];
                                flags->rxffnfnd=tmpFlags[1];
                            }
                            else
                            {
                                RXEXF_FLAGS *flags=&((RXEXFCAL_PARM *) parmBlock)->rxfnc_flags;
                                flags->rxfferr =tmpFlags[0];
                                flags->rxffnfnd=tmpFlags[1];
                            }
                            param.env->ReleaseBooleanArrayElements(jflags, tmpFlags, 0);  // copy back the content, free buffer
                            param.env->DeleteLocalRef(jflags);


                                // get result, if any, turn it into a RexxObjectPtr
                            jobject jresult=param.env->GetObjectArrayElement(jpb, (jint) 3);

                            RexxObjectPtr rop=RgfJavaObject2RexxObject (param.env,
                                                       NULL,      // RexxAndJava object, if NULL will be fetched in routine
                                                       context->threadContext,    // RexxThreadContext
                                                       jresult,           // Java object to turn into a RexxObjectPtr
                                                       FALSE              // if FALSE, do not load .BSF, if not found
                                                       );
                            param.env->DeleteLocalRef(jresult);

                            if (rop==NULL)  // Java returned NULL, so we return .nil
                            {
                                rop=context->Nil();
                            }

                            if (exitNumber==RXOFNC)
                            {
                                RXOFNCCAL_PARM *pb=(RXOFNCCAL_PARM *) parmBlock;
                                pb->rxfnc_retc=rop;
                            }
                            else
                            {
                                RXEXFCAL_PARM *pb=(RXEXFCAL_PARM *) parmBlock;
                                pb->rxfnc_retc=rop;
                            }
                        }

                    }
                    break;


                    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                case RXVALUE:   // system exit # 14, "extends the environments available to the VALUE() built-in function."
                    {
                        RXVALCALL_PARM *pb=(RXVALCALL_PARM *) parmBlock;

        #if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
                        fprintf(stderr, "<--- RXVALUE=14: [%d], subfunction: [%d] - selector=[%s], variable_name=[%s], valuep=[%p]\n",
                                            exitNumber, subfunction,
                                            context->CString(pb->selector),
                                            context->CString(pb->variable_name),
                                            pb->value
                                            );
        #endif

                        if (subfunction==RXVALUECALL )   // 1
                        {
                                // get result, if any, turn it into a RexxObjectPtr
                            jobject jresult=param.env->GetObjectArrayElement(jpb, (jint) 2);
                            if (jresult!=NULL)
                            {
                                RexxObjectPtr rop=
                                     RgfJavaObject2RexxObject (param.env,
                                                      NULL,      // RexxAndJava object, if NULL will be fetched in routine
                                                      context->threadContext,    // RexxThreadContext
                                                      jresult,           // Java object to turn into a RexxObjectPtr
                                                      FALSE              // if FALSE, do not load .BSF, if not found
                                                      );
                                param.env->DeleteLocalRef(jresult);

                                if (rop==NULL ) // Java returned NULL, so we return .nil
                                {
                                    rop=context->Nil();
                                }
                                pb->value=rop;  // save value for Rexx
                            }
                        }
                    }
                    break;

            }

            // case :
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // ================== END:   system exit related special handling, before returning to Rexx


    param.env->DeleteLocalRef(jhandler);
    param.env->DeleteLocalRef(jslot);
    param.env->DeleteLocalRef(jpb);


    RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
    environmentDetachFromNew(&param);
#else
    environmentDetachFrom(&param, FALSE);    // do not close group
#endif
    RgfReleaseLock();

// -----------------------------------------


#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
    //
    if (context->CheckCondition())
    {
        fprintf(stderr, "-----> PANIC! <----- post-processing system exit # [%d] caused a pending Rexx condition !!! !!! !!!\n", exitNumber);
    }

    if (exitNumber==13) // RXNOVAL
    {
        RXVARNOVALUE_PARM *pb=(RXVARNOVALUE_PARM *) parmBlock;
        // fprintf(stderr, "---> RXNOVAL, variable_name=[%s]\n", context->CString(((RXVARNOVALUE_PARM *) parmBlock)->variable_name));
        fprintf(stderr, "---> RXNOVAL, variable_name=[%s]\n", context->CString(pb->variable_name));
    }
#endif


#if defined ( RGF_INFO ) || defined ( RGF_SYSTEM_EXIT_HANDLER )
    fprintf(stderr, "*** RGF_INFO: rexx_exit_handler_entry() 2, exitNumber=[%d], subfunction=[%d], actionCode=[%d] <--- ...\n",
                    exitNumber, subfunction, j_exit_rc
            );
    fflush(stderr);
#endif

   delete[] msg;
   return j_exit_rc;    // return whatever the handler returned
}


// ----------------------------------------------------------------------------------------------
// Generic Rexx command handler function, which will attach to Java, fetch the matching Java command handler
// and invoke it.
RexxObjectPtr RexxEntry rexx_command_handler_entry(RexxExitContext *context, RexxStringObject address, RexxStringObject command)
{
#if defined ( RGF_INFO ) // || defined ( RGF_COMMAND_HANDLER )
    fprintf(stderr, "*** RGF_INFO: rexx_command_handler_entry() 1 ---> ...\n");
    fflush(stderr);
#endif

    // char msg[1024]="";
    char *msg=new char[1024];

        // get the RII structure to get access to
    RexxInstance *ri=context->threadContext->instance;
    PSTRUCT_RII struRii=RgfGetRexxInterpreterInstanceStructFromList(ri);
    if (struRii==NULL || struRii->rajo==NULL || struRii->rexxconf==NULL) {
        SNPRINTF(msg, 1024, "%.16s/internal/rexx_command_handler_entry(), error 1: panic - cannot find Rexx interpreter instance related Java interface objects, address=[%s], command=[%s] for RexxInterpreterInstance=[%p]",
                            DLLNAME, context->CString(address), context->CString(command), ri );

        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }

#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *)context->threadContext->instance, false, 0};
#else
    // STRUCT_PARAM param={tid,env,rajo,riid,bDetach,attachResult,error};
    STRUCT_PARAM param={0, 0, struRii->rajo, (void *)ri, 0, 0, 0, 0};
#endif

    RgfAcquireLock();

#ifdef RGF_ATTACH_NEW
    environmentAttachToNew(&param);     // attach to Java
#else
    environmentAttachTo(&param, FALSE); // attach to Java
#endif

#if defined ( DEBUG_SHOW_STRUCTURES )
    dumpPARAM(   "rexx_command_handler_entry(), after environmentAttachTo(...): param:", &param);
    #ifdef RGF_ATTACH_NEW
        dumpRAJO(    "rexx_command_handler_entry(), after environmentAttachTo(...), 1: ");
        dumpTID2NODE("rexx_command_handler_entry(), after environmentAttachTo(...), 1: ");
    #endif
#endif

    if (param.error== -1)    // could not attach to Java
    {
        RgfReleaseLock();
        SNPRINTF(msg, 1024, "%.16s/internal/rexx_command_handler_entry(), error 2: panic - cannot attach to Java, address=[%s], command=[%s] for RexxInterpreterInstance=[%p]",
                            DLLNAME, context->CString(address), context->CString(command), ri );

        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }
    RgfReleaseLock();


        // get Java command handler object, if any
    jstring jaddress=param.env->NewStringUTF(context->CString(address));
    jobject jhandler=param.env->CallObjectMethod(struRii->rexxconf,
                                      defaultJVM->mid_RexxConfiguration_getCommandHandler,
                                      jaddress);

        // check for Java exception
    if (param.env->ExceptionCheck())
    {
            // raise Rexx condition with Java Throwable
        SNPRINTF(msg, 1024, "%%.16s/internal/rexx_command_handler_entry(), error 3: Java exception while attempting to fetch the Java command handler for command name/address=[%s] - [%%s]",
                            context->CString(address));

        RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
             0,         // RexxExitContext
             context,
             msg);

        param.env->DeleteLocalRef(jaddress);
        param.env->DeleteLocalRef(jhandler);

        RgfAcquireLock();

#ifdef RGF_ATTACH_NEW
        environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif

        RgfReleaseLock();
        delete[] msg;
        return NULL;
    }

        // check whether jhandler==NULL, if so raise appropriate RexxCondtion
    if (jhandler==NULL) {
        param.env->DeleteLocalRef(jaddress);

        RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
        environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif
        RgfReleaseLock();

        SNPRINTF(msg, 1024, "%.16s/internal/rexx_command_handler_entry(), error 4: cannot find Java command handler (returned \"NULL\") for command name/address=[%s]",
                            DLLNAME, context->CString(address));
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }


        // invoke command handler
            // create slot array argument
    jobjectArray jslot = param.env->NewObjectArray (
                              (jsize) 4,                // needed size (number of elements)
                              defaultJVM->clz_Object,   // type
                              NULL                      // initial value
                              );

        // index [0]: save rajo that created this Rexx interpreter instance
    param.env->SetObjectArrayElement(jslot, (jint) 0, struRii->rajo);

        // index [1]: save RexxConfiguration object of this RII
    param.env->SetObjectArrayElement(jslot, (jint) 1, struRii->rexxconf);

        // index [2]: save RexxExitContext pointer as a jstring
    // const char c_context [ RGF_POINTER_STRING_WIDTH+1 ] = "";
    char *c_context=new char[RGF_POINTER_STRING_WIDTH];

    RgfPointer2String(context, c_context);  // create a string representation of RexxExitContext

    jstring jstr=param.env->NewStringUTF(c_context);
    param.env->SetObjectArrayElement(jslot, (jint) 2, jstr);
    param.env->DeleteLocalRef(jstr);

        // index [3]: add context pointer type: 0=ThreadContext, 1=MethodContext, 2=FunctionContext, 3=ExitContext
    jstr=param.env->NewStringUTF("3");
    param.env->SetObjectArrayElement(jslot, (jint) 3, jstr);
    param.env->DeleteLocalRef(jstr);

    delete [] c_context;

        // public abstract Object handleCommand(Object slot, String address, String command);

    jobject jresult=NULL;

    jstring jcommand=param.env->NewStringUTF(context->CString(command));

#ifndef CONFIG_REXX_HANDLERS_AS_INTERFACES    // rgf, 2012-02-17

    jresult=param.env->CallObjectMethod(jhandler,
                                  defaultJVM->mid_RexxCommandHandler_handleCommand,
                                  jslot,
                                  jaddress,
                                  // param.env->NewStringUTF(context->CString(command))
                                  jcommand
                                  );

#else       // invoke the handler method in the object, if not raise syntax error
    jmethodID jmid=param.env->GetMethodID(param.env->GetObjectClass(jhandler),
                                          CONFIG_REXX_COMMAND_HANDLER_NAME,
                                          CONFIG_REXX_COMMAND_HANDLER_SIGNATURE
                                         );

    if (param.env->ExceptionCheck())
    {
        param.env->ExceptionClear();    // clear the Java exception

        param.env->DeleteLocalRef(jaddress);
        param.env->DeleteLocalRef(jhandler);
        param.env->DeleteLocalRef(jslot);
        param.env->DeleteLocalRef(jresult);
        param.env->DeleteLocalRef(jcommand);

        RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
        environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif
        RgfReleaseLock();

        SNPRINTF(msg, 1024, "%.16s/internal/rexx_command_handler_entry(), error 5: Java exception, cannot find the Java Rexx exit handler method named [%s], needed for processing the command addressed to: [%s]",
                            DLLNAME, CONFIG_REXX_EXIT_HANDLER_NAME, context->CString(address));
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }

    jresult=param.env->CallObjectMethod(jhandler,
                                  jmid,
                                  jslot,
                                  jaddress,
                                  // param.env->NewStringUTF(context->CString(command))
                                  jcommand
                                  );

#endif

        // check for Java exception
    if (param.env->ExceptionCheck())
    {
            // raise Rexx condition with Java Throwable
        SNPRINTF(msg, 1024, "%%.16s/internal/rexx_command_handler_entry(), error 6: Java exception while running the Java command handler for command name/address=[%s], command=[%s] - [%%s]",
                            context->CString(address), context->CString(command));

        RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
             0,         // RexxExitContext
             context,
             msg);

        param.env->DeleteLocalRef(jaddress);
        param.env->DeleteLocalRef(jhandler);
        param.env->DeleteLocalRef(jcommand);
        param.env->DeleteLocalRef(jslot);
        param.env->DeleteLocalRef(jresult);

        RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
        environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif
        RgfReleaseLock();
        delete[] msg;
        return NULL;
    }


        // a Rexx condition raised by the Java handler? If so, return immediately
    if (context->CheckCondition()) {
        param.env->DeleteLocalRef(jaddress);
        param.env->DeleteLocalRef(jhandler);
        param.env->DeleteLocalRef(jcommand);
        param.env->DeleteLocalRef(jslot);
        param.env->DeleteLocalRef(jresult);

        RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
        environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif
        RgfReleaseLock();
        delete[] msg;
        return NULL;
    }


        // turn Java object to Rexx object
    RexxObjectPtr res=RgfJavaObject2RexxObject (param.env,
                                     struRii->rajo,     // RexxAndJava object, needed for registering Java objects
                                     context->threadContext,    // RexxThreadContext
                                     jresult,           // Java object to turn into a RexxObjectPtr
                                     FALSE              // if FALSE, do not load .BSF, if not found
                                     );

    param.env->DeleteLocalRef(jaddress);
    param.env->DeleteLocalRef(jhandler);
    param.env->DeleteLocalRef(jcommand);
    param.env->DeleteLocalRef(jslot);
    param.env->DeleteLocalRef(jresult);

    RgfAcquireLock();
#ifdef RGF_ATTACH_NEW
        environmentDetachFromNew(&param);
#else
        environmentDetachFrom(&param, FALSE);    // do not close group
#endif
    RgfReleaseLock();

#if defined ( RGF_INFO ) // || defined ( RGF_COMMAND_HANDLER )
    fprintf(stderr, "*** RGF_INFO: rexx_command_handler_entry() - TheEnd - , address=[%s], command=[%s] <--- ...\n",
                            context->CString(address),
                            context->CString(command)
            );
    fflush(stderr);
#endif

    delete[] msg;
    return res;
}


    // 2012-02-08, rgf: call backs for exit and command handlers
        // 2012-02-08, rgf: utility inline functions
inline void * rgfUnwrapPointer (JNIEnv *env, jstring j_encoded_pointer)
{
    char *c_pointer=(char *) JNU_GetStringNativeChars(env, j_encoded_pointer);  // convert to native string
    void *raw_pointer=NULL;

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "... rgfUnwrapPointer: using c_context=[%.256s]\n", c_pointer);
    fflush(stderr);
#endif

    RgfString2Pointer(c_pointer, raw_pointer);  // get pointer from string
    RexxFreeMemory(c_pointer);
    return raw_pointer;
}


/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    raiseCondition
 * Signature: ([Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseCondition
  (JNIEnv *env, jobject rajo, jobjectArray j_slot, jstring j_conditionName, jstring j_description, jobjectArray j_additional, jobject j_result)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseCondition <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));


        // get condition name in Rexx
    RexxStringObject rsoConditionName= (j_conditionName==NULL ? NULL :
                (RexxStringObject) JNU_GetStringNativeCharsReturningRexxStringObject(env, j_conditionName, context->threadContext)
                          );

    RexxStringObject rsoDescription= (j_description==NULL ? NULL :
                (RexxStringObject) JNU_GetStringNativeCharsReturningRexxStringObject(env, j_description, context->threadContext)
                          );

    RexxArrayObject rao_additional=RgfProcessJArgs (env, context->threadContext,
                                                     j_additional, // jobjectArray
                                                     NULL,         // no slotDir
                                                     FALSE,           // if FALSE, do not load .BSF, if not found
                                                     0             // 20150721: do not increase BSF registry reference counter
                                                     );

    RexxObjectPtr rop_result = ( j_result == NULL ? NULL :
                     RgfJavaObject2RexxObject (env, rajo,     // RexxAndJava object, needed for registering Java objects
                                           context->threadContext,   // RexxThreadContext
                                           j_result,    // Java object to turn into a RexxObjectPtr
                                           FALSE            // if FALSE, do not load .BSF, if not found
                                    ));

    context->RaiseCondition(context->CString(rsoConditionName),
                            rsoDescription,
                            rao_additional,
                            rop_result);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseCondition <== <==\n");
#endif
}



/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    raiseException
 * Signature: ([Ljava/lang/Object;J[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseException
  (JNIEnv *env, jobject rajo, jobjectArray j_slot, jlong j_definedErrorNumber, jobjectArray j_substitutions)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseException <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));

    RexxArrayObject rao_substitutions=RgfProcessJArgs (env, context->threadContext,
                                                     j_substitutions, // jobjectArray
                                                     NULL,            // no slotDir
                                                     FALSE,           // if FALSE, do not load .BSF, if not found
                                                     0                // 20150721: do not increase BSF registry reference counter
                                                     );

    context->RaiseException((size_t)j_definedErrorNumber,rao_substitutions);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseException <== <==\n");
#endif
}


/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    raiseException0
 * Signature: ([Ljava/lang/Object;J)V
 */
JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseException0
  (JNIEnv *env, jobject rajo, jobjectArray j_slot, jlong j_definedErrorNumber)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseException0 <== <==\n");
#endif
        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    context->RaiseException0((size_t)j_definedErrorNumber);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseException0 <== <==\n");
#endif
}


/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    raiseException1
 * Signature: ([Ljava/lang/Object;JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseException1
  (JNIEnv *env, jobject rajo, jobjectArray j_slot, jlong j_definedErrorNumber, jstring j_substitution1)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseException1 <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));

    RexxStringObject rso_substitution1= (j_substitution1==NULL ? NULL :
                (RexxStringObject) JNU_GetStringNativeCharsReturningRexxStringObject(env, j_substitution1, context->threadContext));

    context->RaiseException1((size_t)j_definedErrorNumber,rso_substitution1);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseException1 <== <==\n");
#endif
}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    raiseException2
 * Signature: ([Ljava/lang/Object;JLjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseException2
  (JNIEnv *env, jobject rajo, jobjectArray j_slot, jlong j_definedErrorNumber, jstring j_substitution1, jstring j_substitution2)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseException2 <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));

    RexxStringObject rso_substitution1= (j_substitution1==NULL ? NULL :
                (RexxStringObject) JNU_GetStringNativeCharsReturningRexxStringObject(env, j_substitution1, context->threadContext));

    RexxStringObject rso_substitution2= (j_substitution2==NULL ? NULL :
                (RexxStringObject) JNU_GetStringNativeCharsReturningRexxStringObject(env, j_substitution2, context->threadContext));

    context->RaiseException2((size_t)j_definedErrorNumber,rso_substitution1,rso_substitution2);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRaiseException2 <== <==\n");
#endif
}


/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    getContextVariable
 * Signature: ([Ljava/lang/Object;Ljava/lang/String;)V
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetContextVariable
  (JNIEnv *env, jobject rajo, jobjectArray j_slot, jstring j_variableName)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetContextVariable <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));

    RexxStringObject rso_variableName= (j_variableName==NULL ? NULL :
                (RexxStringObject) JNU_GetStringNativeCharsReturningRexxStringObject(env, j_variableName, context->threadContext));

    char * c_variableName=(char *) JNU_GetStringNativeChars(env, j_variableName);   // convert to native string

    RexxObjectPtr rop_result=context->GetContextVariable(c_variableName);

    if (rop_result==NULL)   // variable by that name not defined, return variable name in uppercase
    {
        make_upper(c_variableName);
        rop_result=context->String(c_variableName);
    }

    RexxFreeMemory(c_variableName);

    jobject jresult=RgfProcessReturnValue4Java(env, rajo,
                                               context->threadContext,  // supply threadContext
                                               rop_result,  // Rexx value to process
                                               NULL         // any return type is o.k.
                                               );

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetContextVariable <== <==\n");
#endif

    return jresult;        // return jobject

}


/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    setContextVariable
 * Signature: ([Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniSetContextVariable
  (JNIEnv *env, jobject rajo, jobjectArray j_slot, jstring j_variableName, jobject j_variableValue)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniSetContextVariable <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));

    RexxStringObject rso_variableName= (j_variableName==NULL ? NULL :
                (RexxStringObject) JNU_GetStringNativeCharsReturningRexxStringObject(env, j_variableName, context->threadContext));

    char * c_variableName=(char *) JNU_GetStringNativeChars(env, j_variableName);   // convert to native string

    RexxObjectPtr rop_value = ( j_variableValue == NULL ? NULL :
                     RgfJavaObject2RexxObject (env, rajo,     // RexxAndJava object, needed for registering Java objects
                                           context->threadContext,   // RexxThreadContext
                                           j_variableValue, // Java object to turn into a RexxObjectPtr
                                           FALSE            // if FALSE, do not load .BSF, if not found
                                    ));

    context->SetContextVariable(c_variableName, rop_value);

    RexxFreeMemory(c_variableName);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> --> rop_value=[%s]\n", context->ObjectToStringValue(rop_value));

    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniSetContextVariable <== <==\n");
#endif
}



// 2012-02-11, rgf
/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniCheckCondition
 * Signature: ([Ljava/lang/Object;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniCheckCondition
  (JNIEnv *env, jobject rajo, jobjectArray j_slot)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetCheckCondition <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    return (jboolean) context->CheckCondition();

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetCheckCondition <== <==\n");
#endif
    return true;
}


/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniClearCondition
 * Signature: ([Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniClearCondition
  (JNIEnv *env, jobject rajo, jobjectArray j_slot)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetClearCondition <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    context->ClearCondition();

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetClearCondition <== <==\n");
#endif

}


/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniGetConditionInfo
 * Signature: ([Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetConditionInfo
  (JNIEnv *env, jobject rajo, jobjectArray j_slot)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetConditionInfo <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    RexxObjectPtr rop=context->GetConditionInfo();

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetConditionInfo rop=[%p] <== <==\n",rop);
#endif


#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={env, rajo, 0, false, 0};
#else
    STRUCT_PARAM param={0, env, rajo, 0, 0, 0, 0, 0};
#endif

    jobject jobj=RgfRexxObject2JavaObject(&param,context->threadContext,rop);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 3/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetConditionInfo <== <==\n");
#endif

    // return env->GetObjectArrayElement(joa, (jint) 0);  // return Java object matching Rexx

    return jobj;    // return Rexx condition object as a (Java) RexxProxy
}



/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniSetThreadTrace
 * Signature: ([Ljava/lang/Object;Z)V
 */
JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniSetThreadTrace
  (JNIEnv *env, jobject rajo, jobjectArray j_slot, jboolean j_state)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniSetThreadTrace <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    context->threadContext->SetThreadTrace(j_state);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniSetThreadTrace <== <==\n");
#endif
}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniHaltThread
 * Signature: ([Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniHaltThread
  (JNIEnv *env, jobject rajo, jobjectArray j_slot)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniHaltThread <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    context->threadContext->HaltThread();

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniHaltThread <== <==\n");
#endif
}



/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniDropContextVariable
 * Signature: ([Ljava/lang/Object;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniDropContextVariable
  (JNIEnv *env, jobject rajo, jobjectArray j_slot, jstring j_variableName)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniDropContextVariable <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));

    RexxStringObject rso_variableName= (j_variableName==NULL ? NULL :
                (RexxStringObject) JNU_GetStringNativeCharsReturningRexxStringObject(env, j_variableName, context->threadContext));

    char * c_variableName=(char *) JNU_GetStringNativeChars(env, j_variableName);   // convert to native string
    context->DropContextVariable(c_variableName);
    RexxFreeMemory(c_variableName);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniDropContextVariable <== <==\n");
#endif

}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniSetContextVariableToNil
 * Signature: ([Ljava/lang/Object;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniSetContextVariableToNil
  (JNIEnv *env, jobject rajo, jobjectArray j_slot, jstring j_variableName)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniSetContextVariableToNil <== <==\n");
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));

    RexxStringObject rso_variableName= (j_variableName==NULL ? NULL :
                (RexxStringObject) JNU_GetStringNativeCharsReturningRexxStringObject(env, j_variableName, context->threadContext));

    char * c_variableName=(char *) JNU_GetStringNativeChars(env, j_variableName);   // convert to native string
    context->SetContextVariable(c_variableName, context->Nil());
    RexxFreeMemory(c_variableName);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniSetContextVariableToNil <== <==\n");
#endif

}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniGetCallerContext
 * Signature: ([Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetCallerContext
  (JNIEnv *env, jobject rajo, jobjectArray j_slot)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetCallerContext <== <==\n");fflush(stderr);
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    RexxObjectPtr rop=context->GetCallerContext();

#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={env, rajo, 0, false, 0};
#else
    STRUCT_PARAM param={0, env, rajo, 0, 0, 0, 0, 0};
#endif

    jobject jobj=RgfRexxObject2JavaObject(&param,context->threadContext,rop);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetCallerContext <== <==\n");fflush(stderr);
#endif
    return jobj;
}


/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniGetAllContextVariables
 * Signature: ([Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetAllContextVariables
  (JNIEnv *env, jobject rajo, jobjectArray j_slot)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetAllContextVariables <== <==\n");fflush(stderr);
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    RexxObjectPtr rop=context->GetAllContextVariables();

#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={env, rajo, 0, false, 0};
#else
    STRUCT_PARAM param={0, env, rajo, 0, 0, 0, 0, 0};
#endif

    jobject jobj=RgfRexxObject2JavaObject(&param,context->threadContext,rop);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetAllContextVariables <== <==\n");fflush(stderr);
#endif
    return jobj;
}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniGetLocalEnvironment
 * Signature: ([Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetLocalEnvironment
  (JNIEnv *env, jobject rajo, jobjectArray j_slot)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetLocalEnvironment <== <==\n");fflush(stderr);
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    RexxObjectPtr rop=context->GetLocalEnvironment();

#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={env, rajo, 0, false, 0};
#else
    STRUCT_PARAM param={0, env, rajo, 0, 0, 0, 0, 0};
#endif

    jobject jobj=RgfRexxObject2JavaObject(&param,context->threadContext,rop);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetLocalEnvironment <== <==\n");fflush(stderr);
#endif
    return jobj;
}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniGetGlobalEnvironment
 * Signature: ([Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetGlobalEnvironment
  (JNIEnv *env, jobject rajo, jobjectArray j_slot)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetGlobalEnvironment <== <==\n");fflush(stderr);
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    RexxObjectPtr rop=context->GetGlobalEnvironment();

#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={env, rajo, 0, false, 0};
#else
    STRUCT_PARAM param={0, env, rajo, 0, 0, 0, 0, 0};
#endif

    jobject jobj=RgfRexxObject2JavaObject(&param,context->threadContext,rop);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetGlobalEnvironment <== <==\n");fflush(stderr);
#endif
    return jobj;
}


/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniNil
 * Signature: ([Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniNil
  (JNIEnv *env, jobject rajo, jobjectArray j_slot)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniNil <== <==\n");fflush(stderr);
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    RexxObjectPtr rop=context->Nil();

#ifdef RGF_ATTACH_NEW
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={env, rajo, 0, false, 0};
#else
    STRUCT_PARAM param={0, env, rajo, 0, 0, 0, 0, 0};
#endif

    jobject jobj=RgfRexxObject2JavaObject(&param,context->threadContext,rop);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniNil <== <==\n");fflush(stderr);
#endif
    return jobj;
}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniInterpreterVersion
 * Signature: ([Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniInterpreterVersion
  (JNIEnv *env, jobject rajo, jobjectArray j_slot)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniInterpreterVersion <== <==\n");fflush(stderr);
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    size_t res=context->InterpreterVersion();

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniInterpreterVersion <== <==\n");fflush(stderr);
#endif
    return res;
}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniLanguageLevel
 * Signature: ([Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniLanguageLevel
  (JNIEnv *env, jobject rajo, jobjectArray j_slot)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniLanguageLevel <== <==\n");fflush(stderr);
#endif

        // get RexxExitContext from opaque slot argument
    RexxExitContext  *context=(RexxExitContext *) rgfUnwrapPointer(env, (jstring) env->GetObjectArrayElement(j_slot, (jint) 2));
    size_t res=context->LanguageLevel();

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniLanguageLevel <== <==\n");fflush(stderr);
#endif
    return res;
}


/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniProcEnvironment
 * Signature: (ILjava/lang/String;Ljava/lang/String;)Ljava/lang/String;

        invocationType: 1=get, 2=set; if jNewValue is NULL, then remove envName

        returns value of jEnvName

        ---rgf, 2014-03-29
 */
JNIEXPORT jstring JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniProcEnvironment
  (JNIEnv *env, jobject rajo, jint j_InvocationType, jstring j_EnvName, jstring j_NewValue)
{
#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 1/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniProcEnvironment <== <==\n");fflush(stderr);
#endif

    char * c_EnvName=(char *) JNU_GetStringNativeChars(env, j_EnvName);   // convert to native string

    char * c_NewValue=NULL;
    if (j_NewValue!=NULL)       // could be NULL for removing j_EnvName from process environment
    {
        c_NewValue=(char *) JNU_GetStringNativeChars(env, j_NewValue);   // convert to native string
    }

#ifdef UNIX

     char *envBuffer= getenv(c_EnvName);   // get current value, if any

     if (j_InvocationType==2)   // setenv or unsetenv ?
     {
         int res= j_NewValue==NULL ? unsetenv(c_EnvName)                :
                                       setenv(c_EnvName, c_NewValue, 1)   ;
         // if res<>0, then errno would indicate error, not needed in this context
     }

#else   // WINDOWS
     const int MAX_LENGTH=32767;
     char envBuffer[MAX_LENGTH];   // pointer to hold environment value

     DWORD nr_chars = GetEnvironmentVariable(c_EnvName, (LPTSTR) envBuffer, MAX_LENGTH);
     if (nr_chars==0)       // no chars returned, probably not set (could test:  GetLastError returns ERROR_ENVVAR_NOT_FOUND, but not needed in this context)
     {
         envBuffer[0]=NULL;
     }

     if (j_InvocationType==2)   // setenv or unsetenv ?
     {
         SetEnvironmentVariable(c_EnvName, (LPTSTR) c_NewValue );    // maybe type now: LPCTSTR
     }
#endif


#if defined (DEBUG_JNI)
    fprintf(stderr, "--> invocation type=[%d], c_EnvName=[%s], envBuffer=[%s], c_NewValue=[%s] <--\n",
                       j_InvocationType, c_EnvName, envBuffer, c_NewValue);
    fflush(stderr);
#endif



        // make sure we free memory
    RexxFreeMemory(c_EnvName);

    if (c_NewValue!=NULL)
    {
        RexxFreeMemory(c_NewValue);
    }


#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniProcEnvironment <== <==\n");fflush(stderr);
#endif

    // create Java string from the C string and return it
//    return ( envBuffer==NULL || envBuffer[0]==NULL ? NULL : env->NewStringUTF( envBuffer ) );
#ifdef UNIX
    return ( envBuffer==NULL ? NULL : env->NewStringUTF( envBuffer ) );
#else   // WINDOWS
    return ( envBuffer==NULL || envBuffer[0]==NULL ? NULL : env->NewStringUTF( envBuffer ) );
#endif
}


// ------ 2015-05-09, rgf

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniGetLocalEnvironment0
 * Signature: (Ljava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetLocalEnvironment0
  // (JNIEnv *, jobject, jstring);
  (JNIEnv *env, jobject raj, jstring  j_rii_ID)
{
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();   // get current TID

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "--> arrived: ..._jniGetLocalEnvironment(), tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], defaultJVM->clz_Object=[%p]\n",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, defaultJVM->clz_Object);
    fprintf(stderr, "\t\tenv=[%p], raj=[%p], j_rii_ID=[%p]\n", env, raj, j_rii_ID);
    fflush(stderr);
#endif

        // get interpreter instance from argument
    char         *c_rii_ID=NULL;
    RexxInstance *instance=NULL;
    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "                                        using c_rii_ID=[%.256s]\n", c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    RexxFreeMemory(c_rii_ID);

    RexxThreadContext   *rtc=NULL;
    RexxObjectPtr result_obj;           // Rexx object returned from Rexx, NULL[OBJECT], if none

    RexxInstance *tmpInstance=NULL;
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/jniGetLocalEnvironment(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ..._jniGetLocalEnvironment(): tid=[%lu], error, cannot retrieve c_rii_ID=p=[%p], received=tmpInstance=[%p] instead\n",
                    (unsigned long) tid, instance, tmpInstance);
    fflush(stderr);
#endif

        return NULL;
    }

    // no need to do a rtc->ReleaseLocalReference(ro) as these references get released upon rtc->DetachThread() !
    if (!instance->AttachThread(&rtc))          // get a ThreadContext
    {
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/jniGetLocalEnvironment(), error 2: could not AttachThread() to Rexx interpreter instance with the ID '%p'", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ..._jniGetLocalEnvironment(): tid=[%lu], error, cannot do an instance->AttachThread()!\n",
                          (unsigned long) tid);
    fflush(stderr);
#endif

        return NULL;
    }

    result_obj=rtc->GetLocalEnvironment();      // get local environment

    // check for exception; if so, create RexxException, supply condition object as a RexxProxy; clear exception in thread
    if (rtc->CheckCondition())      // a Rexx condition raised?
    {
        RexxDirectoryObject condObj=rtc->GetConditionInfo();
        char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "/routine/jniRexxRunProgram(), error 9");

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             rtc, // rtc,
                                                             condObj, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        rtc->DetachThread();
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ..._jniGetLocalEnvironment(): tid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",
                       (unsigned long) tid, msg);
    fflush(stderr);
#endif

        RexxFreeMemory(msg);
        return NULL;
    }


#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning from: .._jniRexxRunProgram(...), tid=[%lu], normal execution\n", (unsigned long) tid);
    fflush(stderr);
#endif

    jobject jresult=RgfProcessReturnValue4Java(env, raj, rtc, result_obj, NULL);

    rtc->DetachThread();

    return jresult;
}

// ------ > neu!

// ---------- neuer

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniGetGlobalEnvironment0
 * Signature: (Ljava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetGlobalEnvironment0
  // (JNIEnv *, jobject, jstring);
  (JNIEnv *env, jobject raj, jstring  j_rii_ID)
{
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();   // get current TID

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "--> arrived: ..._jniGetGlobalEnvironment0(), tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], defaultJVM->clz_Object=[%p]\n",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, defaultJVM->clz_Object);
    fprintf(stderr, "\t\tenv=[%p], raj=[%p], j_rii_ID=[%p]\n", env, raj, j_rii_ID);
    fflush(stderr);
#endif

        // get interpreter instance from argument
    char         *c_rii_ID=NULL;
    RexxInstance *instance=NULL;
    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "                                        using c_rii_ID=[%.256s]\n", c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    RexxFreeMemory(c_rii_ID);

    RexxThreadContext   *rtc=NULL;
    RexxObjectPtr result_obj;           // Rexx object returned from Rexx, NULL[OBJECT], if none

    RexxInstance *tmpInstance=NULL;
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/jniGetGlobalEnvironment0(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ..._jniGetGlobalEnvironment0(): tid=[%lu], error, cannot retrieve c_rii_ID=p=[%p], received=tmpInstance=[%p] instead\n",
                    (unsigned long) tid, instance, tmpInstance);
    fflush(stderr);
#endif

        return NULL;
    }

    // no need to do a rtc->ReleaseGlobalReference(ro) as these references get released upon rtc->DetachThread() !
    if (!instance->AttachThread(&rtc))          // get a ThreadContext
    {
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/jniGetGlobalEnvironment0(), error 2: could not AttachThread() to Rexx interpreter instance with the ID '%p'", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ..._jniGetGlobalEnvironment0(): tid=[%lu], error, cannot do an instance->AttachThread()!\n",
                          (unsigned long) tid);
    fflush(stderr);
#endif

        return NULL;
    }

    result_obj=rtc->GetGlobalEnvironment();      // get Global environment

    // check for exception; if so, create RexxException, supply condition object as a RexxProxy; clear exception in thread
    if (rtc->CheckCondition())      // a Rexx condition raised?
    {
        RexxDirectoryObject condObj=rtc->GetConditionInfo();
        char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "/routine/jniRexxRunProgram(), error 9");

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             rtc, // rtc,
                                                             condObj, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        rtc->DetachThread();
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ..._jniGetGlobalEnvironment0(): tid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",
                       (unsigned long) tid, msg);
    fflush(stderr);
#endif

        RexxFreeMemory(msg);
        return NULL;
    }


#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning from: .._jniRexxRunProgram(...), tid=[%lu], normal execution\n", (unsigned long) tid);
    fflush(stderr);
#endif

    jobject jresult=RgfProcessReturnValue4Java(env, raj, rtc, result_obj, NULL);

    rtc->DetachThread();

    return jresult;

}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniNil0
 * Signature: (Ljava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniNil0
  // (JNIEnv *, jobject, jstring);
  (JNIEnv *env, jobject raj, jstring  j_rii_ID)
{
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();   // get current TID

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "--> arrived: ..._jniNil0(), tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], defaultJVM->clz_Object=[%p]\n",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, defaultJVM->clz_Object);
    fprintf(stderr, "\t\tenv=[%p], raj=[%p], j_rii_ID=[%p]\n", env, raj, j_rii_ID);
    fflush(stderr);
#endif

        // get interpreter instance from argument
    char         *c_rii_ID=NULL;
    RexxInstance *instance=NULL;
    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "                                        using c_rii_ID=[%.256s]\n", c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    RexxFreeMemory(c_rii_ID);

    RexxThreadContext   *rtc=NULL;
    RexxObjectPtr result_obj;           // Rexx object returned from Rexx, NULL[OBJECT], if none

    RexxInstance *tmpInstance=NULL;
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/jniNil0(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ..._jniNil0(): tid=[%lu], error, cannot retrieve c_rii_ID=p=[%p], received=tmpInstance=[%p] instead\n",
                    (unsigned long) tid, instance, tmpInstance);
    fflush(stderr);
#endif

        return NULL;
    }

    // no need to do a rtc->ReleaseGlobalReference(ro) as these references get released upon rtc->DetachThread() !
    if (!instance->AttachThread(&rtc))          // get a ThreadContext
    {
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/jniNil0(), error 2: could not AttachThread() to Rexx interpreter instance with the ID '%p'", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ..._jniNil0(): tid=[%lu], error, cannot do an instance->AttachThread()!\n",
                          (unsigned long) tid);
    fflush(stderr);
#endif

        return NULL;
    }

    result_obj=rtc->Nil();      // get .nil

    // check for exception; if so, create RexxException, supply condition object as a RexxProxy; clear exception in thread
    if (rtc->CheckCondition())      // a Rexx condition raised?
    {
        RexxDirectoryObject condObj=rtc->GetConditionInfo();
        char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "/routine/jniRexxRunProgram(), error 9");

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             rtc, // rtc,
                                                             condObj, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        rtc->DetachThread();
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ..._jniNil0(): tid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",
                       (unsigned long) tid, msg);
    fflush(stderr);
#endif

        RexxFreeMemory(msg);
        return NULL;
    }


#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning from: .._jniRexxRunProgram(...), tid=[%lu], normal execution\n", (unsigned long) tid);
    fflush(stderr);
#endif

        // make sure that we explicitly return a RexxProxy with .nil embedded!
    jobject jresult=RgfCreateRexxProxy(env, raj, rtc, result_obj, NULL, NULL);

    rtc->DetachThread();

    return jresult;
}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniInterpreterVersion0
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniInterpreterVersion0
  // (JNIEnv *, jobject, jstring);
  (JNIEnv *env, jobject raj, jstring  j_rii_ID)
{
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();   // get current TID

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "--> arrived: ...__jniInterpreterVersion0(), tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], defaultJVM->clz_Object=[%p]\n",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, defaultJVM->clz_Object);
    fprintf(stderr, "\t\tenv=[%p], raj=[%p], j_rii_ID=[%p]\n", env, raj, j_rii_ID);
    fflush(stderr);
#endif

        // get interpreter instance from argument
    char         *c_rii_ID=NULL;
    RexxInstance *instance=NULL;
    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "                                        using c_rii_ID=[%.256s]\n", c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    RexxFreeMemory(c_rii_ID);

    RexxThreadContext   *rtc=NULL;

    RexxInstance *tmpInstance=NULL;
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/_jniInterpreterVersion0(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ...__jniInterpreterVersion0(): tid=[%lu], error, cannot retrieve c_rii_ID=p=[%p], received=tmpInstance=[%p] instead\n",
                    (unsigned long) tid, instance, tmpInstance);
    fflush(stderr);
#endif

        return NULL;
    }

    // no need to do a rtc->ReleaseGlobalReference(ro) as these references get released upon rtc->DetachThread() !
    if (!instance->AttachThread(&rtc))          // get a ThreadContext
    {
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/_jniInterpreterVersion0(), error 2: could not AttachThread() to Rexx interpreter instance with the ID '%p'", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ...__jniInterpreterVersion0(): tid=[%lu], error, cannot do an instance->AttachThread()!\n",
                          (unsigned long) tid);
    fflush(stderr);
#endif

        return NULL;
    }

    size_t res=rtc->InterpreterVersion();

    // check for exception; if so, create RexxException, supply condition object as a RexxProxy; clear exception in thread
    if (rtc->CheckCondition())      // a Rexx condition raised?
    {
        RexxDirectoryObject condObj=rtc->GetConditionInfo();
        char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "/routine/jniRexxRunProgram(), error 9");

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             rtc, // rtc,
                                                             condObj, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        rtc->DetachThread();
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ...__jniInterpreterVersion0(): tid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",
                       (unsigned long) tid, msg);
    fflush(stderr);
#endif

        RexxFreeMemory(msg);
        return NULL;
    }


#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning from: ..__jniInterpreterVersion0(), tid=[%lu], normal execution\n", (unsigned long) tid);
    fflush(stderr);
#endif

        // make sure that we explicitly return a RexxProxy with .nil embedded!

    rtc->DetachThread();

    return res;
}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniLanguageLevel0
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniLanguageLevel0
  // (JNIEnv *, jobject, jstring);
  (JNIEnv *env, jobject raj, jstring  j_rii_ID)
{

#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();   // get current TID

#if defined(DEBUG_JNI) || defined (DEBUG40) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "--> arrived: ...__jniLanguageVersion0(), tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], defaultJVM->clz_Object=[%p]\n",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, defaultJVM->clz_Object);
    fprintf(stderr, "\t\tenv=[%p], raj=[%p], j_rii_ID=[%p]\n", env, raj, j_rii_ID);
    fflush(stderr);
#endif

        // get interpreter instance from argument
    char         *c_rii_ID=NULL;
    RexxInstance *instance=NULL;
    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "                                        using c_rii_ID=[%.256s]\n", c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    RexxFreeMemory(c_rii_ID);

    RexxThreadContext   *rtc=NULL;

    RexxInstance *tmpInstance=NULL;
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/_jniLanguageVersion0(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ...__jniLanguageVersion0(): tid=[%lu], error, cannot retrieve c_rii_ID=p=[%p], received=tmpInstance=[%p] instead\n",
                    (unsigned long) tid, instance, tmpInstance);
    fflush(stderr);
#endif

        return NULL;
    }

    // no need to do a rtc->ReleaseGlobalReference(ro) as these references get released upon rtc->DetachThread() !
    if (!instance->AttachThread(&rtc))          // get a ThreadContext
    {
        char *msg=new char[1024];
        SNPRINTF( msg, 1024, "%.16s/routine/_jniLanguageVersion0(), error 2: could not AttachThread() to Rexx interpreter instance with the ID '%p'", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ...__jniLanguageVersion0(): tid=[%lu], error, cannot do an instance->AttachThread()!\n",
                          (unsigned long) tid);
    fflush(stderr);
#endif

        return NULL;
    }

    size_t res=rtc->LanguageLevel();

    // check for exception; if so, create RexxException, supply condition object as a RexxProxy; clear exception in thread
    if (rtc->CheckCondition())      // a Rexx condition raised?
    {
        RexxDirectoryObject condObj=rtc->GetConditionInfo();
        char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "/routine/jniRexxRunProgram(), error 9");

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             rtc, // rtc,
                                                             condObj, // conditionObject
                                                             // NULL, // packageObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        rtc->DetachThread();
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning: ...__jniLanguageVersion0(): tid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",
                       (unsigned long) tid, msg);
    fflush(stderr);
#endif

        RexxFreeMemory(msg);
        return NULL;
    }


#if defined(DEBUG_JNI) || defined (DEBUG40)
    fprintf(stderr, "<-- returning from: .._jniLanguageVersion0(), tid=[%lu], normal execution\n", (unsigned long) tid);
    fflush(stderr);
#endif

    rtc->DetachThread();

    return res;
}






#ifdef __cplusplus
}               // closing bracket
#endif


