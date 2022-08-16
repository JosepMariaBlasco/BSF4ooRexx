/*
    - "rajo": RexxAndJava-object (Java object used to interface with Rexx)

    - locks used for serializing access to:
                   JRST_lock         to "environmentAttachToNew(&param)" and "environmentDetachFromNew(&param)"

                   RII_lock          RexxInterpreter instance lock
                   RII_creation_lock RexxInterpreter instance creation lock
                   REGISTRY_lock     access to ooRexx registry structures


remarks:

    - RexxCondition() is not documented, but is shown in "rexxpg.pdf"
    - there is no CallProgramFromData()! Instead use (RMG): NewRoutine()+CallRoutine()
    - RFE: function to list loaded libraries and packages; function to list all/specific global/public
      routines and public classes
    - using NULL instead of NULLOBJECT, as NULL more clearly indicates that nothing got returend
      (NULLOBJECT could be mistaken to mean .NIL)
    - native APIs "Condition*()", but "RaiseException()", inconsistent; makes it a little bit harder
      to understand! :(  [Terminology inkonsistent]

    - 2010-02-10: no means to test whether a Rexx interpreter instance is still available for
                  executing Rexx code (e.g. in callbacks from Java to Rexx)

   last change: $Revision: 888 $ $Author: Administrator $ $Date: 2010-08-16 16:02:21 +0200 (Mon, 16. Aug 2010) $

  * This DLL registers implements the Java-ooRexx-bridge in native code, using the ASF (Apache Software Foundation)
  * BSF 2.4 (Bean Scripting Framework)
  * it allows Java to call into Rexx using the new ooRexx 4.0 native APIs using JNI ("Java Native Interface").
  *
  * last change: $Revision: 888 $ $Author: Administrator $ $Date: 2010-08-16 16:02:21 +0200 (Mon, 16. Aug 2010) $

    History:    - TODO: remove dead (commented) code eventually ;)
                - TODO (at the end): remove #define USE_OREXX_REGISTRY_PACKAGE and code

inline jint impl_jniRexxHaltInterpreterInstance (JNIEnv *env, jobject jobj, jstring j_rii_ID)

    8.5.0   2022-08-11, rgf: - changed signature of _jniRexxSendMessageToRexxObject by a new
                               argument 'scope' which can be NULL, a String or a RexxProxy

                               - now checking whether receiver RexxProxy and messageName are
                                 supplied, otherwise throw a Java exception

                             - adjust for type change in getRexxEngine:
                               "getRexxEngine", "()Lorg/rexxla/bsf/engines/rexx/RexxEngine;"

    8.5.0   2022-08-10, rgf: - now based on minimum Java 8

    ===============================================================================================

    6.0.0   2022-08-07, rgf: - now using RexxAllocateMemory() instead of malloc()
                               JNU_JavaString2pRxString(...) for RXSTRING structures
                               as otherwise heap-guard errors occur on Windows

    6.0.0   2022-08-06, rgf: - add Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniExecuteSimpleProcedureOnOwnRII(...)
                               such that e.g. the ooRexx version string can be fetched by the RexxEngine
                               via RexxAndJava using a proper Rexx instance to not cause any
                               side-effects on the current RexxEngine, e.g. while initializing

    6.0.0   2022-08-02, rgf: - add "_DEBUG" to version information, if a debug compile (will
                               be used for the Windows version, cf. Makefile-64-windows)

    6.0.0   2022-07-24, rgf: - impl_jniRexxTerminateInterpreterInstance(), now returns -99
                               in case the instance got removed from the RII list right before
                               using it for termination;
                               adding new critical section to protect termination while
                               concurrently an instance lookup may take place

                             - RgfRemoveProxyObject(): now uses the root/primodal instance (no
                               need for an instance lookup in this case)

    6.0.0   2021-08-03, rgf: - remove #define USE_RII2OID_RELATION and code (not necessary to keep this information)
                             - clean up a little bit left-over (debug) statements

    6.0.0   2021-07-18, rgf: - when loading Java now also honor JAVA_HOME environment variable, if present
			     - search sequence:

                    Windows: JAVA_HOME -> unqualified (system searches)
                       JAVA_HOME\\bin\\server\\jvm.dll          Windows
                       JAVA_HOME\\jre\\bin\\server\\jvm.dll     Windows

                    Unix: /opt/BSF4ooRexx ->
                         JAVA_HOME ->
                         if Apple: "/System/Library/Frameworks/JavaVM.framework/JavaVM" (default) ->
                         operating system search (simple DLL/so/dylib name)

                       JAVA_HOME/lib/server/libjvm.dylib        MacOS
                       JAVA_HOME/jre/lib/server/libjvm.dylib    MacOS

                       JAVA_HOME/lib/server/libjvm.so           Linux
                       JAVA_HOME/jre/lib/server/libjvm.so       Linux

                 - if loading Java on Unix, 'java.library.path' gets set, unless it is passed as
                   "-Djava.library.path=..." via the command line; this way we can help Java locate
                   libBSF4ooRexx.so/libBSF4ooRexx.dylib

    6.0.0   2022-06-07, rgf: - correct synopsis (comment) for BsfContextVariables() (drop was wrong)

    6.0.0   2021-08-04, rgf: - added new include file org_rexxla_bsf_engines_rexx_RexxCleanupRef.h
                             - refactor three JNI functions to allow them to be used from RexxAndJava and
                               the new RexxCleanupRef Java class:

                               - inline impl_jniRexxHaltInterpreterInstance(...) invoked by
                                 - Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxHaltInterpreterInstance
                                 - Java_org_rexxla_bsf_engines_rexx_RexxCleanupRef_jniRexxHaltInterpreterInstance

                               - inline impl_jniRexxTerminateInterpreterInstance(...) invoked by
                                 - Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxTerminateInterpreterInstance
                                 - Java_org_rexxla_bsf_engines_rexx_RexxCleanupRef_jniRexxTerminateInterpreterInstance

                               - inline impl_jniUnregisterRexxObject(...) invoked by
                                 - Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniUnregisterRexxObject
                                 - Java_org_rexxla_bsf_engines_rexx_RexxCleanupRef_jniUnregisterRexxObject


    6.0.0   2021-03-20, rgf: - removed a warning by explicitly using strcmp():
                               MacOS: BSF4ooRexx.cc:8349:31: warning: result of comparison against a string literal is unspecified (use an explicit string comparison function
      instead) [-Wstring-compare]
        if ( options[i].option==LOAD_REQUIRED_LIBRARY )
                              ^ ~~~~~~~~~~~~~~~~~~~~~

    6.0.0   2020-06-28, rgf: - in _jniRexxSendMessageToRexxObject make debug output for args better legible

    6.0.0   2018-10-07, rgf: - fixed subtle bug in RgfJavaObject2RexxObject(): only do a ReleaseLocalReference(), if
                               bsf.wrap() returns a new Rexx object, otherwise ooRexx crashes may develop
                               as wrong Rexx object pointers get used!

    6.0.0   2018-10-02, rgf: - fixed subtle bug in RgfProcessJArgs(): only do a ReleaseLocalReference(), if
                               bsf.wrap() returns a new Rexx object, otherwise ooRexx crashes may develop
                               as wrong Rexx object pointers get used!

    6-ß-ß   2018-09-29, rgf: - RexxAllocateMemory() and RexxFreeMemory() got employed in the original BSF4Rexx
                               version that could run on other Rexx interpreters like Regina as well; BSF4ooRexx (two 'oo')
                               was created with ooRexx 4.0 that introduced new APIs that allowed new features to
                               be implemented; replacing the two APIs with malloc() and free() should remove overhead

    6.0.0   2018-09-21, rgf: - RgfProcessJArgs(): make sure that ReleaseLocalReference(clzBSF) gets executed
                               only if clzBSF is not NULL (older test units may have that condition)

    6.0.0   2018-03-04, rgf: - bsfDoUnregisterRexxObject: set default to "true", such that unregistering Rexx
                               objects takes place in all cases

    6.0.0   2018-02-24, rgf: - new external Rexx routine "BsfTestPing" which only returns; allows to test
                               and time calling external Rexx functions from Rexx scripts

    6.0.0   2018-02-23, rgf: - new function "BsfUninit4JavaBean(beanName)": called from UNINIT code in BSF,
                               version uses RexxAndJava class object, therefore the Rexx interpreter instance
                               used for uninits does not have a need for a peer RexxAndJava instance (ooRexx
                               uninits may be run on a newly created Rexx interpreter instance that is not
                               linked to Java otherwise)

                             - unregisterBean[4JNI] now return -1 for not registered, or remaining leftCount
                               (0=removed from registry)

    6.0.0   2018-02-19, rgf: - remove the JNI methods jniForceConstructorNewInstance, jniForceFieldGet,
                               jniForceFieldSet, jniForceMethodInvoke as they cannot be used to force the
                               intended operations on Java 9; as the problem got solved by rewriting the
                               reflection part in Java, these methods are not needed and get removed

    6.0.0   2018-01-19, rgf: - implement the four JNI methods named jniForceConstructorNewInstance,
                               jniForceFieldGet, jniForceFieldSet, jniForceMethodInvoke (these "force"
                               JNI routines are means of last resort, if Java 9 or later is not
                               allowing to access protected members in superclasses via reflection
                               unlike all the previous versions of Java)

    6.0.0   2018-01-13, rgf: - allow in addition a Routine object in BsfCreateRexxProxy as first argument

    6.0.0   2017-12-24, rgf: - putting and removing LOCAL_PRIMODAL_TID ("BSF.PRIMODALTID") in/from .environment instead of .local

    6.0.0   2017-12-23, rgf: implementing <https://sourceforge.net/p/bsf4oorexx/feature-requests/15/>:
                             - remove rgfGetEntryFromLocal(), rgfGetEntryFromEnvironment(): not needed anymore
                             - remove creation of "Slot.Argument" from native code, define as public class in BSF.CLS and
                               use that one for adding a trailing slot argument for messages from Java to Rexx objects
                               with optional context information
                             - corrected error message in external Rexx routine BSF(), which erroneously was numbered 3 and
                               referred to BSFCreateRexxProxy

    6.0.0   2017-11-19, rgf: - added a counter to BSF() and also tid on debug outputs to become able
                               in a multithreaded execution to identify the debug output

    6.0.0   2017-10-15, rgf: - BsfQueryRegisteredFunctions(): return external Rexx function names
                               BSFGetRIID() and BSFGetRTC()

    6.0.0   2017-10-05, rgf: - new BSFGetRTC():  allow Rexx to retrieve the Rexx thread context pointer as a string for debugging purposes

    6.0.0   2009-09-25, rgf: - new BSFGetRIID(): allow Rexx to retrieve the Rexx instance ID (the instance pointer as string) for debugging purposes

    6.0.0   2017-09-14, rgf: - BsfJavaException(): allow for omitting first argument; if omitted, then
                               defaulting to option "CHECK" which returns .true, if a Java exception is
                               pending, .false else

    6.0.0   2017-09-11, rgf: - BsfJavaException(): corrected leftover quick test that deleted the msg buffer,
                               which caused an illegal pointer to be used thereafter

    6.0.0   2017-08-27, rgf: - added undocumented function BsfApplyHotFixCrash(), which will be
                               removed; it allows to activate the "HotFix" in SendMessage() from Java
                               and runProgram from Java which yields the thread before attaching to
                               Rexx; this may help to overcome some crashes in the current ooRexx 5 beta;
                               usage: bsfApplyHotFixCrash( | .true | false); if no argument, the current
                                      setting will be returned

    6.0.0   2017-08-23, rgf: continue tidying up and refactoring to make code easier legible
                             - remove "RGF_INFO_1", "RGF_INFO", "RGF_BIT", "RGF_BIT2", "DEBUG3", "DEBUG40",
                               "RGF_ATTACH", "RGF_DETACH", "DEBUG_JAVA_ATTACH_DETACH"
                             - introduced "DEBUG_CREATE_REXX_EXCEPTION"
                             - renamed "DEBUG_LOADER_UNLOADER" to "DEBUG_BSF_LOADER_UNLOADER",
                                       "DEBUG_RGF_ATTACH_NEW" to "DEBUG_JAVA_ATTACH"
                             - remove unused "DEBUG_SHOW_STRUCTURES", "RGF_MUTEX4RXSIOTRC", "RGF_INVALID_ROUTINE",
                               "DEBUG_THREADS", "DEBUG_RGF_PROCESS_J_ARGS_UNO"
                             - remove TID2NODE-related strctures, not needed anymore
                             - replaced SNPRINTF with function snprinf(), now that MSC includes it as well under that name
                             - removed MSC_VER "#pragma warning(disable:4100)"

    6.0.0   2017-08-21, rgf: - allow (temporarily) to disable HOT_FIX_CRASH for testing purposes: RexxRoutine "BsfApplyHotFixCrash()"

    6.0.0   2017-08-20, rgf: continue tidying up and refactoring to make code easier legible
                             - remove definition "CONFIG_REXX_HANDLERS_AS_INTERFACES" as we have been using
                               Java interfaces only
                             - remove definitions DEBUG40_NIXI, DEBUG_RGF_PROBES, USE_OREXX, USE_REXXTRANS,
                               RGF_TRUE


    6.0.0   2017-08-19, rgf: continue tidying up and refactoring to make code easier legible
                             - added INFO_* statements for easying debugging calls from Java to Rexx to
                               help trace down cause of Rexx crashes
                             - added HOT_FIX_CRASH definition: sleeping/relinquisghing the thread before
                               doing a Rexx AttachThread() significantly reduces crashes, cf. new function
                               "hotfix_relinquish_thread()"; trying to remove the crashes by experimenting
                               with the "rgf_multihreaded" test units to determine number of relinquishes
                               needed to stabilize; still occassionally/rarely hangs occur


    6.0.0   2017-08-18, rgf: start tidying up and refactoring to make code easier legible (has become a "cemetery" of all versions
                             of bsf4rexx and bsf4oorexx in the past 16 years)

                             - rename "const char *" (and PSZ) -> CSTRING; removed PSZ definition
                             - removed trailing '\0' in format string "%s\n%s\0"
                             - rename typedef "VOID" to "void", "PVOID" to "POINTER" (rexx.h), "TRUE" to "1"
                             - remove outdated typedefs like VOID, PVOID, TRUE, APIRET, PFN, REXXPFN, PINT,
                               TID and the like
                             - replacing "TID" with "thread_id_t", removed typdef for TID and UNIX/WINDOWS conditionals
                             - replaced RgfAllocateMemory() with RexxAllocateMemory() and RgfFreeMemory() with
                               RexxFreeMemory(); removed obsolete RgfAllocateMemory() and RgfFreeMemory()
                             - remove RGF_USE_MUTEX define for Windows, just use it (forgo critical section variant)
                             - remove DEBUG_RAISE_CONDITION


    6.0.0   2017-08-15, rgf: - BSFCreateRexxProxy(): use "R[EXXOBJECT]" instead of "R[EXX]" to self-document how
                               the first argument will be  used if of type .string, .method or .array, which
                               by default will be used for the code to carry out for the dynamically created
                               unknown method; make sure that now JavaProxy is attempted if third argument
                               is "R[EXXOBJECT]"

    6.0.0   2017-08-14, rgf: - BSFCreateRexxProxy(): if first argument is a Rexx string, method or array and the third argument is
                               "R[EXX]", then the first argument is proxied as is (allowing Java to send
                               it all ooRexx messages) and not used as the code for a dynamically created UNKNOWN
                               method that gets proxied by the invoked "BSF_OnTheFly.cls" program

    6.0.0   2017-07-25, rgf: - allow an array as Rexx-code argument for BsfCreateRexxProxy()

    6.0.0   2017-06-06, rgf: - rename "rcoSlotArgument" to "rcoSlotArgumentClass" to indicate a Rexx class is referenced

    6.0.0   2017-04-15, rgf: add a directory subclass named "SLOT.ARGUMENT" to .environment; all slot arguments,
                             either from native or Java code must instantiate this class, such that Rexx programmers
                             become able to unambiguously identify the slot arguments (appended arguments as a
                             result of sending a message from Java)

    6.0.0   2017-04-04, rgf: make return values after raising Rexx exceptions dependent on DEBUG_RAISE_CONDITION

    6.0.0   2017-02-19, rgf: after invoking RaiseException() the return value will be ignored by Rexx; for debugging
                             purposes a RexxStringObject in the form of "BSF4ooRexx.cc-RaiseException-FFF-xx" will
                             be returned, where FFF denotes the function name and xx is a two digit number starting
                             with 01 and incremented for each RaiseException in the same function

    6.0.0   2017-02-14, rgf: BsfContextVariables(): if an operation succeeded (like SET or DROP) will return .true
                             instead of NULL which indicates no return value!

    6.0.0   2017-01-08, rgf: fixed conversion warning where returning NULL instead of 0 (-Wconversion-null).

    6.0.0   2016-12-20, rgf: in _jniRexxSendMessageToRexxObject: do not do a bsf.wrap with .true as the second
                             argument anymore; RexxEngine.java was changed to increase the refCount (on JavaObject
                             or MethodObject), such that bsf.wrap returned .BSF object can safely run unregisterBean
                             in its uninit method

                             in J_jniUnregisterRexxObject: from now on, again execute the unregisterRexxObject, if
                             Rexx started Java; on ooRexx 5.0 beta no problems have occurred in testing anymore

                             new external Rexx function BsfDoUnregisterRexxObject([.true|.false]); default: .false;
                             this controls whether RgfRemoveProxyObject(c_obj_ID) is carried out by the native function
                             Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniUnregisterRexxObject in the case Java
                             was loaded by Rexx.

    6.0.0   2016-12-08, rgf: corrected comment: BSF_PROXY -> BSF_REFERENCE

    6.0.0   2016-11-19, rgf: updated major version number to 6 to indicate new base level Java 1.6/6;
                            JNI_VERSION_1_6 should only be used, if we need to use the new 1.6/6 JNI function
                            "jobjectRefType GetObjectRefType(env,refObj)"

    4.5.2.  2016-03-28  jlf: (Jean-Louis Faucher): "Fix bug 28 overflow under Ubuntu 14.04";
                            cf. bug 28: <https://sourceforge.net/p/bsf4oorexx/bugs/28/>

    4.5.2.  2015-09-24  rgf: applying fix as described in <https://sourceforge.net/p/bsf4oorexx/bugs/25/>,
                             making sure that lineends are not a mixture of LF and CR-LF as reported in the
                             above bug report, using CR-LF

    4.5.2.  2015-08-20  rgf: added debug information to a Java runtime error, if the Java Throwable is not
                        able to create a String rendering of itself

    4.5.2.  2015-08-11  rgf: javaObjectBean and javaMethodObjectBean will get deregistered in RexxAndJava.call(), hence
                         increase refcounter in bsf.wrap, as the uninit-method of the BSF Rexx proxy will decrease it;


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
       Copyright 2001-2018 Rony G. Flatscher

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

// TODO: if requiring ooRexx 5.0 as the minimum level:
    -- add jni function to return package's 'local' directory (gives access to package local)
    -- add ability to add Java envrionment handler at runtime (not only on Rexx interpreter creation time)
*/


#ifdef __cplusplus
extern "C" {            // make sure no C++ name mangling comes into the way
#endif



// rgf, 2017-08-19: hot fix for CRASHes; will slow down flurry of invocations from Java as this adds at least a wait time of 1/100 second (10 ms)
// rgf, 2017-10-16: removed, seems RMG could fix the problem in the ooRexx kernel
// #define HOT_FIX_CRASH       // rgf, 2017-08-19: temporary hot fix for CRASHes (seems one needs to relinquish thread for 10ms before invoking AttachThread())

// rgf, 2017-08-19, add INFO_* definitions for helping debug crash, maybe it is related to creating and terminating Rexx interpreter instances
// rgf, 2017-10-16: removed, seems RMG found the error in nestActivity() in the ooRexx kernel and fixed it
// #define INFO_ALL_OPTIONS
#if defined (INFO_ALL_OPTIONS) // comment before && to activate all of the flags below
    #define INFO_PRIMODAL_RII   // rgf, 2017-10-04
    #define INFO_CREATE_RII     // rgf, 2017-08-19
    #define INFO_TERMINATE_RII  // rgf, 2017-08-19
    #define INFO_HALT_RII       // rgf, 2017-08-19
    #define INFO_ATTACH_THREAD  // rgf, 2017-10-04
    #define INFO_DETACH_THREAD  // rgf, 2017-10-04
    #define INFO_SEND_MESSAGE_TO_REXX_OBJECT // rgf, 2017-08-19
    #define INFO_REXX_RUN_PROGRAM   // rgf, 2017-08-19
//     #define DEBUG_BSF_FUNCTION  // rgf, 2017-11-19
#endif


// #define RGF_JENV_OBJECTS    // rgf, 20180122: debug new JNI methods, show whether MIDs were retrieved successfully


// #define DEBUG_SOME_OPTIONS
#if defined DEBUG_SOME_OPTIONS
    // rgf, 2017-07-16
    #define DEBUG_CREATE_HALT_TERMINATE
    #define DEBUG_TERMINATE_INTERPRETER // 2009-09-03
    #define DEBUG_SEND_MESSAGE_TO_REXX_OBJECT    // 2009-09-06
    #define DEBUG_REXX_PROXY        // 2009-06-10
    #define DEBUG_UNREGISTER_REXX_OBJECT_NOISY
    #define DEBUG_REGISTER_REXX_OBJECT
#endif


// #define DEBUG_ALL_OPTIONS
#if defined (DEBUG_ALL_OPTIONS) // comment before && to activate all of the flags below
    #define DEBUG
    #define DEBUG1
    #define DEBUG2

    #define DEBUG_JNI               // debug JNI invocations
    #define DEBUG_JNI_ENTRY         // rgf, 2009-10-10, debug JNI invocations: only entry into function

    #define DEBUG_REXX_ATTACH       // 2009-05-26, attaching/detaching to/from Rexx instances
    #define DEBUG_OREXX_REGISTRY    // 2009-05-28, setting up/tearing down OREXX_REGISTRY
    #define DEBUG_REXX_PROXY        // 2009-06-10
    #define DEBUG_REGISTER_REXX_OBJECT          // 2012-06-07
    #define DEBUG_UNREGISTER_REXX_OBJECT        // 2009-09-02
    #define DEBUG_UNREGISTER_REXX_OBJECT_NOISY  // 2009-09-03
    #define DEBUG_REXX_PROXY_BSF    // 2009-06-14
    #define DEBUG_RII_LIST          // 2009-05-28, adding/removing Rexx interpreter instances
    #define DEBUG_BSF_LOADER_UNLOADER       // 2009-06-05
    #define DEBUG_CREATE_HALT_TERMINATE // 2009-06-07, debug instantiation, halting and termination of Rexx interpreter instances
    #define DEBUG_TERMINATE_INTERPRETER // 2009-09-03

    #define DEBUG_BSF_FUNCTION          // 2009-06-21
    #define DEBUG_RETURN_VALUE_FOR_JAVA // 2009-06-22

    #define DEBUG_SEND_MESSAGE_TO_REXX_OBJECT   // 2009-09-06
    #define DEBUG_RGF_PROCESS_J_ARGS    // 2009-09-06

    #define RGF_COMMAND_HANDLER         // rgf, 2012-02-07
    #define RGF_SYSTEM_EXIT_HANDLER     // rgf, 2012-02-07
    #define DEBUG_RGF_PROCESS_J_ARG1    // rgf, 2012-02-07; renamed to RgfJavaObject2RexxObject, 2012-02-18

    #define DEBUG_ARRAY_ARGS            // rgf, 2012-02-21
    #define DEBUG_JAVA_ATTACH           // debug new attach/detach strategy to Java

    #define DEBUG_RII_ID                // 2015-08-13
    #define DEBUG_CONTEXT_VARS          // 2017-02-14

    #define DEBUG_CREATE_REXX_EXCEPTION // 2017-08-23
#endif

    // rgf, 2012-06-06
#define CONFIG_MAKE_OREXX_REGISTRY_ACCESSIBLE   // meant for debugging

//  // #define DEBUG_REXX_PROXY     // 2022-08-11, debugging sendMessageScoped(...)
//  #define DEBUG_REXX_PROXY_BSF
//  #define INFO_SEND_MESSAGE_TO_REXX_OBJECT
//  #define DEBUG_BSF_FUNCTION
//  #define DEBUG_SEND_MESSAGE_TO_REXX_OBJECT
//  #define DEBUG_CREATE_REXX_EXCEPTION


#if defined WINDOWS                     // meant for debugging
    // __asm Int 3;     // break
    // DebugBreak();    // break, popup to break will be shown
    // ----
    // alternative: debug single step, without popup to break:
    #define BREAK_HERE_SINGLESTEP() __asm __emit 0xF1
#endif




#if defined (__APPLE__) && NIXI      // only define, if rexx is started on the main thread and established a CFRunLoop (then awt's event dispatch thread needs to run on a separate thread)
    #define RGF_LOAD_JVM_IN_SEPARATE_THREAD
#endif

// TODO: 2017-08-23: really needed?
// #define RGF_UNO_WRAP     // rgf, 2010-06-08: use UNO.WRAP, if .UNO available



#define CONFIG_HANDLE_NETREXX_AS_STRING_4_REXX      // rgf, 2012-03-11

    // define the handler's name and signatures
#define CONFIG_REXX_COMMAND_HANDLER_NAME      "handleCommand"
#define CONFIG_REXX_COMMAND_HANDLER_SIGNATURE "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;"
#define CONFIG_REXX_EXIT_HANDLER_NAME         "handleExit"
#define CONFIG_REXX_EXIT_HANDLER_SIGNATURE    "(Ljava/lang/Object;II[Ljava/lang/Object;)I"

    // rgf, 2014-03-30, name of entry into .local for primodal TID (will allow one to attach even if no TID is known to the Rexx programmer)
#define PRIMODAL_TID                          "BSF.PRIMODALTID"

#include "oorexxapi.h"

#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>


// TODO: 2017-08-23: really needed?
    // if used from Java: if last Rexx interpreter instance terminates, then tear down the OREXX_REGISTRY
// #define RGF_TEAR_DOWN_OREXX_REGISTRY    // if causing troubles in rexx.exe, comment this line


#define ALLOW_LOADING_JVM   // allow dynamic loading of JVM by ooRexx

#ifdef UNIX
   #include <dlfcn.h>       // use the dynamic link (dl*) functions

      // under Linux and MacOSX not defined, ---rgf, 2003-04-05, 2017-08-18
    #ifndef FALSE
        #define FALSE 0
    #endif
#endif


    // compiling for 64-bit Windows with MSC++ "snprintf" is not known!
#ifdef WINDOWS // && defined (BSF4REXX_64_BIT)
   #ifdef BSF4REXX_64_BIT
       #ifndef snprintf
          #define snprintf _snprintf
       #endif
   #endif
#endif


// -------------------- JNI-interface related: these C++ functions are called from RexxAndJava.java and are implemented here
#include "org_rexxla_bsf_engines_rexx_RexxAndJava.h"    // rgf, 2005-06-02: Java native functions we must implement
#include "org_rexxla_bsf_engines_rexx_RexxCleanupRef.h" // rgf, 2021-08-04: new Java class needs access to some JNI functions


// -------------------- ooRexx 4.0 and later
// prototypes
int32_t RgfRemoveProxyObject2    (RexxThreadContext *rtc, RexxStringObject obj_id);

void    RgfSetupOrexxRegistry    (RexxThreadContext *rtc);
void    RgfTearDownOrexxRegistry (RexxThreadContext *rtc);

char *JNU_GetStringNativeChars(JNIEnv *env, jstring jstr);  // JNIEnv *env, jstring jstr)
RexxObjectPtr JNU_GetStringNativeCharsReturningRexxStringObject(JNIEnv *env, jstring jstr, RexxThreadContext *context);  // JNIEnv *env, jstring jstr)
    // only for debugging:
void JAVA_TO_STRING (JNIEnv *env, jobject jo, char * buffer, int bufSize);

char * RgfCreateRexxlikeErrorInfo (RexxThreadContext *rtc, RexxDirectoryObject condObj, CSTRING header);

    // RexxCreateRexxProxy() can have as its first argument a string, method or array object representing the
    // Rexx code to be executed when a method is invoked on the Java side; this Rexx program implements the
    // creation of the RexxProxy() defining an UNKNOWN method for that passed-in code
#define REXX_PROXY_STUB_PROGRAM "BSF_OnTheFly.cls"  // name of Rexx program to use for creating ad hoc RexxProxy object

#define USE_DEFINED_JNI_VERSION JNI_VERSION_1_6     // minimum JNI interface, we are basing on Java 1.6/6.0


// typedef jint (JNICALL *rgf_AttachCurrentThread)  (JavaVM  *, void **, void *);
typedef jint (JNICALL *rgf_JNI_CreateJavaVM)             (JavaVM **, void **, void *);
typedef jint (JNICALL *rgf_JNI_GetDefaultJavaVMInitArgs) (void    *);

thread_id_t  RgfGetTID();


#if defined (BSF4REXX_32_BIT)
    #if defined (BSF4REXX_DEBUG_VERSION)
        #define BSF_VERSION     "850.20220815 org/rexxla/bsf/engines/rexx_DEBUG 32-bit"  // version: "MajorNumber"."YYYYMMDD"
    #else
        #define BSF_VERSION     "850.20220815 org/rexxla/bsf/engines/rexx 32-bit"  // version: "MajorNumber"."YYYYMMDD"
    #endif
#elif defined (BSF4REXX_64_BIT)
    #if defined (BSF4REXX_DEBUG_VERSION)
        #define BSF_VERSION     "850.20220815 org/rexxla/bsf/engines/rexx_DEBUG 64-bit"  // version: "MajorNumber"."YYYYMMDD"
    #else
        #define BSF_VERSION     "850.20220815 org/rexxla/bsf/engines/rexx 64-bit"  // version: "MajorNumber"."YYYYMMDD"
    #endif
#else
    #if defined (BSF4REXX_DEBUG_VERSION)
        #define BSF_VERSION     "850.20220815 org/rexxla/bsf/engines/rexx_DEBUG n/a-bit"  // version: "MajorNumber"."YYYYMMDD"
    #else
        #define BSF_VERSION     "850.20220815 org/rexxla/bsf/engines/rexx n/a-bit"  // version: "MajorNumber"."YYYYMMDD"
    #endif
#endif

    // 2005-06-02, when BSF4ooRexx loads Java, the following Java class is used to initialize all necessary Java objects
#define JAVA_4_REXX     "org/rexxla/bsf/engines/rexx/Java4Rexx"     // can only be used from JNI as everything is private in it

#define DLLNAME         "BSF4ooRexx"      // name of DLL and name for "PARSE SOURCE"


// ---------------------------------------------------------------------------------------------------------
// globals, rgf, 2008-08-01
JavaVM    *currentJVM=NULL;     // JVM to use
int       bsfInvokedBy = 0;     // 0=noJVM, 1=byJava, 2=byRexx

    // ---rgf, 2006-11-19: define boolean and maximum length for Java's event trace
int       bShowErrorString = 1;         // default to showing (dumping) Java traceback in case of an error
#define BSF_ERROR_STRING  "BSF_ERROR_MESSAGE"    // variable name to use to save Java error message in Rexx variable

    // 2016-12-20: rgf, new global variable: if set to false and Java got invoked by Rexx, then no Rexx object -
    //             i.e. RgfRemoveProxyObject(c_obj_ID) - unregistering will take place as this callback from Java
    //             may address a Rexx interpreter which is not available anymore, causing an exception (has not
    //             occurred while running with the beta version of ooRexx 5.0);
    //             to be backwardly compatible, we default to false;
    //             cf. external Rexx function BsfDoUnregisterRexxObject([0|1])
logical_t bsfDoUnregisterRexxObject=true; // false;




// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------
// 2017-08-19: HOTFIX for crashes (invoking Rexx from native Java threads) by relinquishing the
// invoked thread for 10ms before doing an AttachThread()
#ifdef HOT_FIX_CRASH            // 2017-08-19
   logical_t bsfApplyHotFixCrash=0;    // default to NOT apply the HOT_FIX_CRASH

   inline void hotfix_relinquish_thread()
   {
       #ifdef WINDOWS
           Sleep(10);       // relinquish thread for at least 10ms (1/100th of a second)
       #else    // UNIX
           struct timespec sleepTime;
           sleepTime.tv_sec  = 0;
           sleepTime.tv_nsec = 10000000L;   // 10*1000*1000 = 10ms = 1/100th of a second
           nanosleep(&sleepTime, NULL);
       #endif
   }
#endif




// ----------------------------------------------------------------------------
// forward prototypes and inline functions

// === (BEGIN) ========================> maintain list of RexxInterpreterInstances, OREXX_REGISTRY ========>
    // define locks
#ifdef WINDOWS
   HANDLE JVM_setup_lock;       // JVM setup lock
   HANDLE RII_lock;             // RexxInterpreter instance creation lock
   HANDLE REGISTRY_lock;        // ooRexx REGISTRY lock
#else
   pthread_mutex_t JVM_setup_lock = PTHREAD_MUTEX_INITIALIZER;  // JVM setup lock
   pthread_mutex_t RII_lock       = PTHREAD_MUTEX_INITIALIZER;  // RexxInterpreter instance lock
   pthread_mutex_t REGISTRY_lock  = PTHREAD_MUTEX_INITIALIZER;  // ooRexx REGISTRY lock
#endif


// ----------------------------------------------------------------------------
//       DEBUG_HIT_ENTER_TO_CONTINUE: allow halting until user hits enter
#if defined (DEBUG_HIT_ENTER_TO_CONTINUE)
    inline void RgfDebugHitEnterToContinue()
    {
        fprintf(stderr, "\n[RgfDebugHitEnterToContinue(): Hit Enter to Continue...");fflush(stderr);
        char stuff[5];
        int a=fscanf(stdin, "%c", stuff);
        fprintf(stderr, "\n");fflush(stderr);
    }
#endif


//      make sure we do not exceed the passed maximum length, use ooRexx to carry out substr()
#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_REXX_OBJECT_TO_JAVA) || defined (DEBUG_ARRAY_ARGS)
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
#endif


// ----------------------------------------------------------------------------
// Allow serializing access to JVM_structure, RIIs or ooRexx REGISTRY related functions, hence
// argument can be on of: "JVM_setup_lock", "RII_lock", "REGISTRY_lock"
inline void  RgfAcquireLock2(
    #ifdef WINDOWS
        HANDLE
    #else  // UNIX
        pthread_mutex_t
    #endif
                          lock)
{

#if defined (DEBUG2)
    thread_id_t tid=RgfGetTID();

    fprintf(stderr, "*** DEBUG2: --> RgfAcquireLock2(lock): tid=[%lu], lock=[%p] ...\n", (unsigned long) tid, (void *)& lock);
    fflush(stderr);
#endif

#ifdef WINDOWS // WIN32
    WaitForSingleObject(lock, INFINITE);
#elif defined UNIX
    pthread_mutex_lock(&lock);
#endif

#if defined (DEBUG2)
    fprintf(stderr, "*** DEBUG2: <-- RgfAcquireLock2(lock): tid=[%lu], lock=[%p]  ... now ACQUIRED !\n", (unsigned long) tid, (void *) &lock);
    fflush(stderr);
#endif

}


// ----------------------------------------------------------------------------
// Allow serializing access to JVM_structure, RIIs or ooRexx REGISTRY related functions, hence
// argument can be on of: "JVM_setup_lock", "RII_lock", "REGISTRY_lock"
inline void  RgfReleaseLock2(
    #ifdef WINDOWS
        HANDLE
    #else  // UNIX
        pthread_mutex_t
    #endif
                          lock)
{
#if defined (DEBUG2)
    thread_id_t tid=RgfGetTID();
    fprintf(stderr, "*** DEBUG2: --> RgfReleaseLock2(lock): tid=[%lu], lock=[%p]  ...\n", (unsigned long) tid, (void *) & lock);
    fflush(stderr);
#endif

#if defined WINDOWS // WIN32
    ReleaseMutex(lock);
#else
    pthread_mutex_unlock(&lock);
#endif


#if defined (DEBUG2)
    fprintf(stderr, "*** DEBUG2: <-- RgfReleaseLock2(lock): tid=[%lu], lock=[%p]  ... now RELEASED !\n", (unsigned long) tid, (void *) & lock);
    fflush(stderr);
#endif
}


// Rexx Interpreter Instance =======================================================================================

    // rgf, 20090503: add list of RexxInterpreter instances
    // defines the list of "RexxInterpreter instances" (RII)
typedef struct _STRUCT_RII {
    struct _STRUCT_RII          *next;      // pointer to next block
    RexxInstance                *instance;  // the instance pointer, needed as anchor
    jobject                      rajo;      // 2012-02-07, save RexxAndJava object which created this instance
    jobject                      rexxconf;  // 2012-02-07, save RexxConfiguration object used for this RII
}  STRUCT_RII;

typedef STRUCT_RII   *PSTRUCT_RII;          // pointer to structure
long size_STRUCT_RII =sizeof(STRUCT_RII);   // get and memorize size of structure
PSTRUCT_RII pRoot_RII=NULL;                 // root of RII list


    // calculate the needed bytes for representing a pointer as hex-string with prefix "0x" (+2) and trailing \0 (+1), make it an even number of chars (+1)
const size_t RGF_POINTER_STRING_WIDTH=sizeof( POINTER )*2+2+1+1;
const size_t RGF_TID_STRING_WIDTH    = 80;  // allows for hex representation of 2**128 plus two bytes (one for \0, the other for evening)

// macros for encoding and decoding a pointer value to/from a string

// ----------------------------------------------------------------------------
    // 2009-10-11, rgf, adding "+1", such that Linux gcc 32-bit does not truncate the pointer value
    // 2016-03-27, jlf bug 28, removing "+1" because
    // a) "+1" added directly to the definition of RGF_POINTER_STRING_WIDTH to get a buffer with correct size
    // b) the difference of size with buffer's size raised a compile-time warning and a runtime abort "overflow" under Ubuntu 14.04 with gcc 4.8.4.
#define RgfPointer2String(ptr, str)     snprintf((char *)str, RGF_POINTER_STRING_WIDTH, "%p", (void *) ptr);   // macro

// ----------------------------------------------------------------------------
    // fetch pointer from encoded string
#define RgfString2Pointer(str, ptr)     sscanf(str, "%p", &ptr);    // macro


// ----------------------------------------------------------------------------
// Fetches RexxInstance pointer from supplied CSTRING
inline RexxInstance * RgfGetRexxInstanceFromString(CSTRING c_rii_ID)
{
    RexxInstance *ri=NULL;
    RgfString2Pointer(c_rii_ID, ri);
    return ri;
}


// ----------------------------------------------------------------------------
#if defined (DEBUG_RII_LIST)    // if debug Rexx interpreter instance list is defined
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
    #ifdef WINDOWS
        // if (st>10) {
        //     __asm Int 3;    // break
        //     DebugBreak();
        // }

        // or:
        // BREAK_HERE_SINGLESTEP();
    #endif

        }
        fprintf(stderr, "<--- RgfDumpRexxInstanceList():\n");
        fflush(stderr);
    }
#endif


// ----------------------------------------------------------------------------
    // add a new Rexx interpreter instance (RII) to the list
inline void RgfAddRexxInterpreterInstanceToList(RexxThreadContext *rtc, jobject rajo, jobject rexxconf)
{
#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "--> RgfAddRexxInterpreterInstanceToList(...): adding new Rexx instance c_rii_ID=[%p] to the list\n", rtc->instance);
    fflush(stderr);
#endif

    RexxInstance *ri=rtc->instance;

    PSTRUCT_RII newNode=(PSTRUCT_RII) malloc(size_STRUCT_RII);
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

}


// ----------------------------------------------------------------------------
    // remove supplied Rexx interpreter instance (RII) from list and return it, if not found, return NULL
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
        fprintf(stderr, "AttachThread() done. -------------->\n"); fflush(stderr);
    #endif

    #ifdef WINDOWS
        //     __asm Int 3;    // break
        //     DebugBreak();

        // or:
        // BREAK_HERE_SINGLESTEP();
    #endif

        if (rc)                     // if successful, go ahead
        {
            RgfTearDownOrexxRegistry(rtc);  // clear the OREXX_REGISTRYies

    #if defined (DEBUG_REXX_ATTACH)
        fprintf(stderr, "<-------------- ooRexx->DetachThread() - RgfRemoveRIIfromList... "); fflush(stderr);
    #endif

            rtc->DetachThread();    // detach thread from Interpreter instance

    #if defined (DEBUG_REXX_ATTACH)
        fprintf(stderr, "DetachThread() done. <--------------\n"); fflush(stderr);
    #endif

        }
        else
        {
    #if defined (DEBUG_RII_LIST)
        fprintf(stderr, "RgfRemoveRexxInterpreterInstanceFromList(...): 2) UNSUCCESSFUL to AttachThread(), prematurely returning! ri=[%p], bFound=[%d], pRoot_RII=[%p], pRoot_RII->instance=[%p], pRoot_RII->next=[%p]\n",
                                       ri, bFound, pRoot_RII, (pRoot_RII == NULL ? NULL : (pRoot_RII->instance == NULL ? NULL : pRoot_RII->instance)),  (pRoot_RII == NULL ? NULL : (pRoot_RII->next==NULL ? NULL : pRoot_RII->next)));
        fflush(stderr);
    #endif
            return NULL;            // indicate failure
        }
    }
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
    fprintf(stderr, "beFor 'free(oldNode)': oldNode=[%p], oldNode->instance=[%p], oldNode->next=[%p] ...\n",
                                                     oldNode, oldNode->instance, oldNode->next);
    fflush(stderr);
    RgfDumpRexxInstanceList();
#endif

        free(oldNode);

#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "naCh  'free(oldNode)' ...\n"); fflush(stderr);
    RgfDumpRexxInstanceList();
#endif

        return ri;              // return ri, indicates success
    }
    return NULL;                // indicate failure
}



// ----------------------------------------------------------------------------
    // returns supplied RexxInstance, if it exists, otherwise the root instance (could be NULL)
    // if argument is NULL, then root RexxInstance is returned
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

    RgfAcquireLock2(RII_lock);          // lock access to list of RexxInstance instances
    if (ri!=NULL && pRoot_RII!=NULL)    // if root RII available and ri given, look for it and return it
    {
        for (PSTRUCT_RII i=pRoot_RII; i!=NULL; i=i->next) // find node
        {

#if defined (DEBUG_RII_LIST)
    fprintf(stderr, "<>RgfGetRexxInterpreterInstanceFromList<>, st=[%lu]: pRoot_RII=[%p]: i=[%p], i->instance=[%p], i->next=[%p]; received ri=[%p]\n",
                                                              ++st, pRoot_RII, i, i->instance, i->next, ri);
    fflush(stderr);

    #if defined (WINDOWS)    // rgf, 2009-08-20
         if (st>10)
         {

             //     __asm Int 3;    // break
             DebugBreak();

             // or:
             // BREAK_HERE_SINGLESTEP();
         }
    #endif
#endif

            if (i->instance==ri)        // node found!
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

        if (bFound==1)          // o.k. Rexx interpreter instance is known, return it
        {
            RgfReleaseLock2(RII_lock);
            return tmpRi;
        }
    }

    if (pRoot_RII) {                    // root available, return its RexxInterpreter instance instead
        tmpRi=pRoot_RII->instance;
        RgfReleaseLock2(RII_lock);
        return tmpRi;                   // not found, return the root RexxInstance
    }

    RgfReleaseLock2(RII_lock);
    return NULL;                        // no RexxInterpreter instance available
}


// ----------------------------------------------------------------------------
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
             //     __asm Int 3;    // break
             DebugBreak();

             // or:
             // BREAK_HERE_SINGLESTEP();
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


// ----------------------------------------------------------------------------
// RII          Rexx Interpreter Instance
// OID          Object ID: the identityHashValue as a string

  // defines the registry for proxied Rexx objects
     // key: obj~identityHash;
RexxDirectoryObject OREXX_REGISTRY            = NULL;   // one entry per cached Rexx object
RexxDirectoryObject OREXX_REGISTRY_REFCOUNTER = NULL;   // refcounter per registered Rexx object (could be referenced multiple times)

// TODO: rgf, 2017-08-16: not needed anymore ?
// #define USE_OREXX_REGISTRY_PACKAGE      // 2009-06-10: use package object from RexxProxy

#if defined (USE_OREXX_REGISTRY_PACKAGE)
    RexxDirectoryObject OREXX_REGISTRY_PACKAGE= NULL;   // one entry per cached Rexx object containing the object's package object
#endif


// ----------------------------------------------------------------------------
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
    }
    RgfReleaseLock2(REGISTRY_lock);

#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "<-- RgfSetupOrexxRegistry(), finished, rii_id=[%p]... \n", rtc->instance);
    fflush(stderr);
#endif
}


// ----------------------------------------------------------------------------
    // free OREXX_REGISTRY and OREXX_REGISTRY_REFCOUNTER, do a ReleaseGlobalReference() on each
    // this will be called by the library unloader, i.e. at shutdown of BSF4Rexx,
    // hence no locking necessary anymore
inline void RgfTearDownOrexxRegistry(RexxThreadContext *rtc)
{

#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "--> RgfTearDownOrexxRegistry(), rii_id=[%p]... \n", rtc->instance);
    fflush(stderr);
#endif

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


#if defined (DEBUG_OREXX_REGISTRY)
    fprintf(stderr, "<-- RgfTearDownOrexxRegistry()..., finished, rii_id=[%p]... \n", rtc->instance);
    fflush(stderr);
#endif

}


// ----------------------------------------------------------------------------
    // put received Rexx object into the OREXX_REGISTRY and set reference counter to 1; if it exists already
    // just increase the reference counter
inline CSTRING RgfAddProxyObject(RexxThreadContext *rtc, RexxObjectPtr robj, RexxPackageObject rpo)
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

    CSTRING returnValue=rtc->CString(obj_id);
    rtc->ReleaseLocalReference(obj_id);
    return returnValue;
    // return rtc->CString((RexxStringObject) obj_id);
}





// =============================================================================================================
    // ----------------------------------------------------------------------------
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
    // RexxInstance *ri=RgfGetRexxInterpreterInstanceFromList(NULL);    // get any Rexx Interpreter instance
    // RgfReleaseLock2(RII_lock);      // lock access to list of RexxInstance instances

    // 2022-07-24: we can use any RII, use the root/primodal instance
    RexxInstance *ri;       // use root/primodal instance
    if ( (pRoot_RII==NULL) || ((ri=pRoot_RII->instance)==NULL) )   // no RexxInterpreter instance available?
    {
        return -100;        // indicate no running RexxInterpreter instance available
    }

    RexxThreadContext *rtc;
#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "--------------> ooRexx->AttachThread() - RgfRemoveProxyObject... "); fflush(stderr);
#endif

    logical_t rc=ri->AttachThread(&rtc);  // attach thread to Interpreter instance

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "AttachThread() done. rc=[%d]-------------->\n", (int) rc); fflush(stderr);
#endif


    if (!rc)                // if not successful, return, indicate error
    {
        return -101;        // indicate that we could not attach thread to RexxInstance successfully
    }

    RexxStringObject obj_id=rtc->String(c_obj_id);  // turn into a RexxString object

    refs=RgfRemoveProxyObject2(rtc, obj_id);         // now carry out the work
    rtc->ReleaseLocalReference(obj_id);

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "<-------------- ooRexx->DetachThread() - RgfRemoveProxyObject... "); fflush(stderr);
#endif

    rtc->DetachThread();    // detach thread from Interpreter instance

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "DetachThread() done. <--------------\n"); fflush(stderr);
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

    #if defined (USE_OREXX_REGISTRY_PACKAGE)
        fprintf(stderr, "\t\thasIndex([%s]): OREXX_REGISTRY=[%d], OREXX_REGISTRY_REFCOUNTER=[%d], OREXX_REGISTRY_PACKAGE=[%d]\n",
    #else
        fprintf(stderr, "\t\thasIndex([%s]): OREXX_REGISTRY=[%d], OREXX_REGISTRY_REFCOUNTER=[%d]\n",
    #endif
                    cstr_obj_id,
                    rtc->SendMessage1(OREXX_REGISTRY, "HASENTRY", obj_id)==orxTrue,
                    rtc->SendMessage1(OREXX_REGISTRY_REFCOUNTER, "HASENTRY", obj_id)==orxTrue
    #if defined (USE_OREXX_REGISTRY_PACKAGE)
                    , rtc->SendMessage1(OREXX_REGISTRY_PACKAGE, "HASENTRY", obj_id)==orxTrue
    #endif
                    );

    if (rtc->SendMessage1(OREXX_REGISTRY, "HASENTRY", obj_id)==orxTrue)
    {
        RexxObjectPtr dbgRop=rtc->SendMessage1(OREXX_REGISTRY, "ENTRY", obj_id);
        fprintf(stderr, "\t\tOREXX_REGISTRY~HASENTRY(obj_id)~string=[%s]\n", rtc->ObjectToStringValue(dbgRop));fflush(stderr);
    }

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


// ----------------------------------------------------------------------------
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
    CSTRING str=NULL;
    if (obj!=NULL)
        str=rtc->CString(rtc->ObjectToString(obj));

    fprintf(stderr, "<--- RgfGetProxyObject(), end:   obj_id=[%.256s], returning obj=[%p], obj~makeString=[%.256s], after releasing REGISTRY_lock \n",
                          rtc->CString(obj_id), obj, str );
    fflush(stderr);
#endif


    if (obj==NULL) {                    // no entry as nothing returned !
fprintf(stderr, "*** UNEXPECTED! UNEXPECTED! UNEXPECTED! RgfGetProxyObject(...), proxy object with obj_id=[%s] NOT FOUND !!!\n",
                rtc->CString(rtc->SendMessage0(obj_id        , "STRING")) );
fflush(stderr);

// TODO: rgf, 2015-08-06: shall we create an exception/condition in this case?
        return rtc->Nil();              // return the .nil object
    }
    return obj;
}



// ----------------------------------------------------------------------------
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
    CSTRING str=NULL;
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
    // ----------------------------------------------------------------------------
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

#elif defined WINDOWS // WIN32
   #define PATH_SEPARATOR ';'
   HANDLE JRST_lock;                   // rgf, 2009-06-03
#endif


        // rgf, 2009-10-03, 2012-02-07 (RexxConfiguration, RexxExitHandler, RexxCommandHandler added)
        // define a structure that collects and caches JVM dependent data, which should be available
        // to all its threads at all times
typedef struct _STRUCT_JVM {
    JavaVM                     *jvm;                // JVM in use
    jobject                     primodal_rajo;      // primodal "RexxAndJava" object (if Rexx had to load Java, otherwise it will be NULL

    jclass                      clz_Object;         // java.lang.Object class object

    jclass                      clz_RexxAndJava;    // org.rexxla.bsf.engines.rexx.RexxAndJava
    jmethodID                   mid_RexxAndJava_unregisterBean4JNI;     // 20180223, static method !
    jmethodID                   mid_RexxAndJava_javaTestPing;           // 20180226, rgf
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

    jclass                      clz_RexxException;      // "org/rexxla/bsf/engines/rexx/RexxException"
    jmethodID                   mid_RexxException_init; // constructor

    jclass                      clz_String;             // java.lang.String class object
    jmethodID                   mid_String_getBytes;    // "getBytes"-method
    jmethodID                   mid_String_initFromByteArray;   // <init> with byte-array
    jstring                     jstring_emptyUTFString; // Java empty string

    jclass                      clz_RexxProxy;          // org.rexxla.bsf.engines.rexx.RexxProxy class object
    jmethodID                   mid_RexxProxy_init;             // constructor
    jmethodID                   mid_RexxProxy_getRexxObjectID;  // RexxProxy.getRexxObjectID()
    jmethodID                   mid_RexxProxy_newJavaOrExtendedProxyInstanceFromRexx;
    jmethodID                   mid_RexxProxy_setBsfRegistryKey;    // RexxProxy.setBsfRegistry()

    jclass                      clz_Throwable;          // java.lang.Throwable class object

    // rgf, 2012-02-07
    jclass                      clz_RexxConfiguration;  // org.rexxla.bsf.engines.rexx.RexxConfiguration

                                                        // public RexxExitHandler getExitHandler(int function)
    jmethodID                   mid_RexxConfiguration_getExitHandler;
                                                        // public RexxCommandHandler getCommandHandler(String name)
    jmethodID                   mid_RexxConfiguration_getCommandHandler;

    // rgf, 2018-01-19
    jmethodID                   mid_Java_Lang_Reflect_Constructor_newInstance;
    jmethodID                   mid_Java_Lang_Reflect_Field_get;
    jmethodID                   mid_Java_Lang_Reflect_Field_set;
    jmethodID                   mid_Java_Lang_Reflect_Method_invoke;


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


    RgfAcquireLock2(JVM_setup_lock);    // 2012-12-30, rgf: lock access for setting up JVM structure

    if  (struct_JVM->jvm!=NULL)         // already initialized, don't reprocess
    {
        RgfReleaseLock2(JVM_setup_lock);
        return;
    }

    struct_JVM->jvm=jvm;        // save JVM pointer
    jclass tmpClz=NULL;

        // get class object for java.lang.Object
    struct_JVM->clz_Object=env->FindClass("java/lang/Object");      // get class object

        // get MID for the interface method in rajo
    tmpClz=env->FindClass("org/rexxla/bsf/engines/rexx/RexxAndJava");    // get class object
    struct_JVM->clz_RexxAndJava     =tmpClz;

    // 20180223, static method !
    struct_JVM->mid_RexxAndJava_unregisterBean4JNI =env->GetStaticMethodID( tmpClz, "unregisterBean4JNI",  "(Ljava/lang/String;)I");
    struct_JVM->mid_RexxAndJava_javaTestPing       =env->GetStaticMethodID( tmpClz, "javaTestPing",        "()V");

    struct_JVM->mid_RexxAndJava_convFromJNI        =env->GetMethodID( tmpClz, "convFromJNI",         "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
    struct_JVM->mid_RexxAndJava_convFromRexx       =env->GetMethodID( tmpClz, "convFromRexx",        "(Ljava/lang/String;)Ljava/lang/Object;");
    struct_JVM->mid_RexxAndJava_getRexxEngine      =env->GetMethodID( tmpClz, "getRexxEngine",       "()Lorg/rexxla/bsf/engines/rexx/RexxEngine;");
    struct_JVM->mid_RexxAndJava_hashCode           =env->GetMethodID( tmpClz, "hashCode",            "()I");
    struct_JVM->mid_RexxAndJava_javaCallFromBSF    =env->GetMethodID( tmpClz, "javaCallBSF",         "([Ljava/lang/String;)Ljava/lang/String;" );
    struct_JVM->mid_RexxAndJava_lookupBean4JNI     =env->GetMethodID( tmpClz, "lookupBean4JNI",      "(Ljava/lang/String;)Ljava/lang/Object;" );
    struct_JVM->mid_RexxAndJava_registerBean4JNI   =env->GetMethodID( tmpClz, "registerBean4JNI",    "(Ljava/lang/Object;)Ljava/lang/String;" );
    struct_JVM->mid_RexxAndJava_unregisterBean     =env->GetMethodID( tmpClz, "unregisterBean",      "(Ljava/lang/String;)I");
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

    // rgf, 2018-01-19
    tmpClz                    =env->FindClass("java/lang/reflect/Constructor");
    struct_JVM->mid_Java_Lang_Reflect_Constructor_newInstance=env->GetMethodID( tmpClz, "newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;" );
    tmpClz                    =env->FindClass("java/lang/reflect/Field");
    struct_JVM->mid_Java_Lang_Reflect_Field_get              =env->GetMethodID( tmpClz, "get"        , "(Ljava/lang/Object;)Ljava/lang/Object;" );
    struct_JVM->mid_Java_Lang_Reflect_Field_set              =env->GetMethodID( tmpClz, "set"        , "(Ljava/lang/Object;Ljava/lang/Object;)V" );
    tmpClz                    =env->FindClass("java/lang/reflect/Method");
    struct_JVM->mid_Java_Lang_Reflect_Method_invoke          =env->GetMethodID( tmpClz, "invoke"     , "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;" );

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

    struct_JVM->clz_RexxConfiguration =(jclass)  env->NewGlobalRef(struct_JVM->clz_RexxConfiguration);  // rgf, 2012-02-07


#if defined ( DEBUG_JNI )  || defined (RGF_JENV_OBJECTS)
   fprintf(stderr, "\ninit_STRUCT_JVM(): after setting up, struct_JVM->jvm=[%p], env->ExceptionCheck()=[%d]\n\n",
                   struct_JVM->jvm, env->ExceptionCheck());

   fprintf(stderr, "\tclz_Object       =[%p]\n", struct_JVM->clz_Object);

   fprintf(stderr, "\tclz_RexxAndJava  =[%p]\n", struct_JVM->clz_RexxAndJava);

   fprintf(stderr, "\t\tstruct_JVM->mid_RexxAndJava_unregisterBean4JNI =[%p]\n", struct_JVM->mid_RexxAndJava_unregisterBean4JNI);
   fprintf(stderr, "\t\tstruct_JVM->mid_RexxAndJava_javaTestPing       =[%p]\n", struct_JVM->mid_RexxAndJava_javaTestPing     );

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

   fprintf(stderr, "\n--- 2018-01-19, rgf:\n");
   fprintf(stderr, "\t\tstruct_JVM->mid_Java_Lang_Reflect_Constructor_newInstance=[%p]\n",   struct_JVM->mid_Java_Lang_Reflect_Constructor_newInstance);
   fprintf(stderr, "\t\tstruct_JVM->mid_Java_Lang_Reflect_Field_get              =[%p]\n",   struct_JVM->mid_Java_Lang_Reflect_Field_get              );
   fprintf(stderr, "\t\tstruct_JVM->mid_Java_Lang_Reflect_Field_set              =[%p]\n",   struct_JVM->mid_Java_Lang_Reflect_Field_set              );
   fprintf(stderr, "\t\tstruct_JVM->mid_Java_Lang_Reflect_Method_invoke          =[%p]\n",   struct_JVM->mid_Java_Lang_Reflect_Method_invoke          );

   fprintf(stderr, "---\n");
   fflush(stderr);

#endif

   RgfReleaseLock2(JVM_setup_lock);

}

    // uninitialize (free) JVM-dependent data we always need in all threads
inline void uninit_STRUCT_JVM(JNIEnv *env, PSTRUCT_JVM struct_JVM)
{
#if defined ( DEBUG_JNI )
   fprintf(stderr, "uninit_STRUCT_JVM(), 1 ...\n");
#endif

   RgfAcquireLock2(JVM_setup_lock);     // lock access for setting up JVM structure
   if (struct_JVM->jvm==NULL)           // no JVM, no uninits necessary
   {
       RgfReleaseLock2(JVM_setup_lock);
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

    env->DeleteGlobalRef(struct_JVM->clz_RexxConfiguration);    // rgf, 2012-02-07

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

    RgfReleaseLock2(JVM_setup_lock);
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
        free(c_keyValue);     // free memory

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
inline jint RgfUnregisterBean(JNIEnv *env, jobject rajo, jobject beanName)
{
    jint refCount=env->CallBooleanMethod(rajo,
                                  defaultJVM->mid_RexxAndJava_unregisterBean,
                                  beanName);
    return refCount;
}





    // create RexxProxy object and its package to the OREXX_REGISTRY, create RexxProxy
    // according to RMG (20090511) the package object is not meant to (and cannot be) used as a "context" under which an object executes
inline jobject RgfCreateRexxProxy (JNIEnv *env, jobject rajo,
                                        RexxThreadContext *rtc,
                                        RexxObjectPtr robj,
                                        RexxPackageObject rpo,
                                        RexxObjectPtr userData)
{

#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "\t--> RgfCreateRexxProxy(): env=[%p], rajo=[%p], robj=[%p], rpo=[%p], userData=[%p] ...\n",
                                               env, rajo, robj, rpo, userData);
    fflush(stderr);
#endif

        // get RexxEngine instance
    jobject j_rexxEngine=env->CallObjectMethod(rajo,
                                             defaultJVM->mid_RexxAndJava_getRexxEngine,
                                             NULL);

    CSTRING c_objID         =RgfAddProxyObject(rtc, robj, rpo);     // add to OREXX registry, save ID string

#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "\t--> RgfCreateRexxProxy(): after adding RgfAddProxyObject(), c_objID=[%.256s] ...\n", c_objID);
    fflush(stderr);
#endif

    CSTRING c_objID_userData=NULL;

    if (userData!=NULL)     // userData object given, put it into OREXX registry, save ID string
    {
        c_objID_userData=RgfAddProxyObject(rtc, userData, rpo); // add to OREXX registry, save ID string

#if defined (DEBUG_REXX_PROXY)
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

#if defined (DEBUG_REXX_PROXY)
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
                                       int32_t       reason,
                                       CSTRING       message
                                       )
{

#if defined(DEBUG_CREATE_REXX_EXCEPTION)
   fprintf(stderr, "\t-->\tRgfCreateRexxException4Java() - start ...\t env=[%p], rajo=[%p], rtc=[%p], conditionObject=[%p]\n",
                     env, rajo, rtc, conditionObject);
#endif

        // get class object for RexxException, locate its constructor
#if defined(DEBUG_CREATE_REXX_EXCEPTION)
   fprintf(stderr, "\t-->\tRgfCreateRexxException4Java() - create RgfCreateRexxProxy ...\n");
#endif

    jobject jConditionObject= (conditionObject==NULL ? NULL :
                                                       RgfCreateRexxProxy(env, rajo, rtc, conditionObject,
                                                                               NULL,    // packageObject
                                                                               NULL     // userData
                                                                          )
                               );

#if defined(DEBUG_CREATE_REXX_EXCEPTION)
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

#if defined(DEBUG_CREATE_REXX_EXCEPTION)
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

/* --- rgf, 2017-12-23: FindClass() only environment sequence, FindPackageClass() package plus environment sequence
#if defined(DEBUG_BSF_FUNCTION) || defined(DEBUG2)
    fprintf(stderr, "\n... <<<          (RgfValue4JavaIndicator): robj=[%p], str=[%s]; robj~class~string=[%s]; robj~IsOfType(\"BSF\")=[%i]; rtc->FindClass(\"BSF\")=[%p]; robj~IsOfType(\"STRING\")=[%i]",
                    robj,
                    rtc->ObjectToStringValue(robj),
                    rtc->ObjectToStringValue(rtc->SendMessage0(robj,"CLASS")),
                    (int) (rtc->IsOfType(robj, "BSF")),
                    rtc->FindClass("BSF"),
                    (int) (rtc->IsOfType(robj, "STRING"))
           );
    fflush(stderr);
#endif
--- */

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


// TODO: rgf, 2017-06-06: check whether prefixing with <O>, <S> necessary for the Java side (maybe String vs. BeanName indistinguishable)
//                        (check RexxEngine.java, RexxAndJava.java)
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
    RexxObjectPtr clzBSF =rtc->FindClass("BSF");    // get BSF class
    logical_t     bClzBsf=(clzBSF!=NULL);           // determine whether we have .BSF available

    if (!bClzBsf && bLoadBSF)       // if BSF.CLS was not required yet, require it now!
    {
#if defined(DEBUG_RGF_PROCESS_J_ARG1)
   fprintf(stderr, "... RgfJavaObject2RexxObject(...): riid=[%p], about to require BSF.CLS rtc->CheckCondition()=[%d], RUNTIME (PRE-)LOADING OF BSF.CLS ! | [1] ...\n", riid, (int) rtc->CheckCondition());fflush(stderr);
#endif
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
           free(msg);
    #endif

    }
#endif
        clzBSF =rtc->FindClass("BSF");  // get BSF class
        bClzBsf=(clzBSF!=NULL);         // determine whether we have .BSF available
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
        free(tmpStr);             // make sure we free the allocated memory
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
        CSTRING c_beanName    = env->GetStringUTFChars(j_beanName, JNI_FALSE);
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
        if (res!=rop)       // 2018-10-07, rgf: if the returned Rexx object is different, release the local reference for rop
        {
            rtc->ReleaseLocalReference(rop);
        }
        // res=(RexxStringObject) rtc->SendMessage1(clzBSF, "BSF.WRAP", res); // try to wrap it up as a Rexx proxy
    }

#if defined(DEBUG_RGF_PROCESS_J_ARG1)
    fprintf(stderr, "... RgfJavaObject2RexxObject(...): riid=[%p], at end, returning=[%p], ExceptionCheck()=[%d]...\n", riid, res, env->ExceptionCheck());fflush(stderr);
#endif

    rtc->ReleaseLocalReference(clzBSF);

    return res;
}




// TODO: 2017-04-29, ---rgf
//      - create an updated documentation
//      - bIncr only has an effect, if .BSF is available

    // process args for Rexx, return RexxArray for further processing
    // "j_args" contains either Strings (values or beanNames) or RexxProxy objects
    // "bLoadBSF" will only be TRUE, if this routine is invoked from ...JniRexxSendMessageToRexxObject(...)
    // 20150721, rgf: added "bIncr": if 1 then let BSF.WRAP run "rawRegister" to increase reference
    //                counter in BSF registry in use cases where jniRexxRunProgram() or
    //                jniRexxSendMessageToRexx() is employed (they register and unregister the Java objects;
    //                BSF_REFERENCE will by default not register, but unregister)
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
    RexxInstance *riid=rtc->instance;      // only for debugging to be able to suppley the Rexx interpreter instance ID

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
    fprintf(stderr, "\tj_args.length=[%ld]\n", (long int) env->GetArrayLength(j_args));

    jsize size=env->GetArrayLength(j_args);
    for (jsize i=0; i<size; i++)
    {
       fprintf(stderr, "\tj_args[%ld/%ld]: ", (long int) i, (long int) size); fflush(stderr);
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

    RexxObjectPtr clzBSF=rtc->FindClass("BSF"); // get BSF class
    logical_t    bClzBsf=(clzBSF!=NULL);        // determine whether we have .BSF available

#if defined(DEBUG_RGF_PROCESS_J_ARGS )
   fprintf(stderr, "**** //1// bClzBsf=[%lu], rtc->Nil()=[%p], clzBSF=[%p]\n", bClzBsf, rtc->Nil(), clzBSF);

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

        RexxRoutineObject rro=rtc->NewRoutine("FROM_BSF4ooRexx_DLL", "::requires BSF.CLS", 18);

        if (rro!=NULL)      // object got created, let us run it
        {
            RexxObjectPtr result_obj=NULL;
            result_obj=rtc->CallRoutine(rro, ra);   // call the program
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
   free(msg);
#endif

}
#endif

        clzBSF =rtc->FindClass("BSF");  // get BSF class
        bClzBsf=(clzBSF!=NULL);         // determine whether we have .BSF available
    }


#if defined (RGF_UNO_WRAP)     // TODO: remove altogether as not needed?
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
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], j_args=[%p], i=[%d]<len=[%ld]\n",
                    riid, j_args, i, (long int) len);fflush(stderr);
#endif

        jobject jobj=env->GetObjectArrayElement(j_args, i); // get array element

#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "... RgfProcessJArgs(...): riid=[%p], fetched jobj=[%p]\n", riid, jobj);fflush(stderr);
#endif

        RexxObjectPtr robj=NULLOBJECT;

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
            char * tmpStr= JNU_GetStringNativeChars(env, j_obj_ID);    // convert to native string
#if defined(DEBUG_RGF_PROCESS_J_ARGS)
    fprintf(stderr, "                       riid=[%p], tmpStr =[%.256s], ExceptionCheck()=[%d]    \n", riid, tmpStr, env->ExceptionCheck());fflush(stderr);
#endif

            robj=RgfGetProxyObject(rtc, rtc->String(tmpStr));  // get Rexx object
            env->DeleteLocalRef(j_obj_ID);
            free(tmpStr);             // make sure we free the allocated memory
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
                CSTRING c_beanName    = env->GetStringUTFChars(j_beanName, JNI_FALSE);
                robj=       rtc->String(c_beanName);    // create Rexx string
                env->ReleaseStringUTFChars(j_beanName, c_beanName);      // release
                env->DeleteLocalRef(j_beanName);
            }

            if (bClzBsf)    // .local~bsf available, use its bsf.wrap class method
            {
                    // try to wrap it up as a Rexx proxy;
                    // let BSF.CLS' bsf.wrap() return either a plain Rexx string or a .BSF proxy to a Java object
                RexxObjectPtr rop=rtc->SendMessage2(clzBSF,
                                      "BSF.WRAP",
                                      robj,
                                      (bIncr==1 ? rtc->True() : rtc->False())
                                     );

                // rgf, 2018-10-02: only do a ReleaseLocalReference(), if returned Rexx object is another object!
                //                  otherwise "mysterious" errors can occur (e.g. crashes in the BSF4ooRexx multithread stress tests)
                if (rop!=robj)  // bsf.wrap() returned the same RexxStringObject (no lead-in in Java string then)
                {
                    rtc->ReleaseLocalReference(robj);
                }
                robj=rop;
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

    rtc->ReleaseLocalReference(clzBSF);    // rgf, 20180920: should not be NULL anymore, just to be sure test for NULL ...
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
     CSTRING aha=env->GetStringUTFChars( (jstring) jobj, JNI_FALSE);
     RexxStringObject rso=rtc->String(aha);     // create RexxStringObject
     env->ReleaseStringUTFChars( (jstring) jobj, aha);

     env->DeleteLocalRef(jo);
     env->DeleteLocalRef(joc);
     env->DeleteLocalRef(jobj);

     return rso;
}



    // allocates and creates a Rexx-like error message
    // returned *msg must be freed
inline char * RgfCreateRexxlikeErrorInfo (RexxThreadContext *rtc, RexxDirectoryObject condObj, CSTRING header)
{
    rtc->ClearCondition();

    RexxCondition cond;
    rtc->DecodeConditionInfo(condObj, &cond);

    char *msg=(char *)malloc( 4096 );
    msg[0]=0;

        // format message according to exception type
    if (rtc->SendMessage1(cond.conditionName, "EQUALS", rtc->String("SYNTAX"))==rtc->True()) // SYNTAX condition ?
    {
        RexxObjectPtr tbRop=rtc->SendMessage0(rtc->DirectoryAt(condObj, "TRACEBACK"), "FIRSTITEM"); // first traceback line=error line
        CSTRING line1="Rexx traceback line not available from the Rexx condition object (Rexx condition may have been caused by a call or message from Java to Rexx)";

        if (tbRop!=rtc->Nil())     // a traceback entry available
        {
            line1=rtc->CString(tbRop) ; // first traceback line=error line
        }

        char *line2=new char[2048];

        if (cond.position!=0)
        {
            snprintf(line2, 2048, "Error %d running %.512s line %d:  %.1024s",
                                (int) cond.rc,      (NULL != cond.program   && rtc->Nil() != cond.program   ? rtc->CString( cond.program )  : "program n/a")   ,
                                (int) cond.position,(NULL != cond.errortext && rtc->Nil() != cond.errortext ? rtc->CString( cond.errortext ): "errortext n/a") );
        }
        else    // position may be 0 if Rexx message was sent from native code
        {
            snprintf(line2, 2048, "Error %d running %.512s line %s:  %.1024s",
                                (int) cond.rc,      (NULL != cond.program   && rtc->Nil() != cond.program   ? rtc->CString( cond.program )  : "program n/a")   ,
                                "n/a",              (NULL != cond.errortext && rtc->Nil() != cond.errortext ? rtc->CString( cond.errortext ): "errortext n/a") );
        }

        // char line3[2048]="";        // will only use 1024 of it
        char *line3=new char[2048];

        snprintf( line3, 2048, "Error %d.%d:  %.1024s", (int) (cond.code / 1000),
                                     (int) (cond.code % 1000),
                                     (NULL != cond.message && rtc->Nil() != cond.message ? rtc->CString( cond.message ) : "message n/a") );

        snprintf( msg, 4096, "%.16s%.512s:\n%.1024s\n%.1024s\n%.1024s", DLLNAME, header, line1, line2, line3);
fprintf(stderr, "---> BSF4ooRexx.cc %d %s:\n%s\n<---\n", __LINE__, __FUNCTION__, msg);fflush(stderr),

        delete[] line2;
        delete[] line3;
    }
    else
    {
        snprintf( msg, 4096, "%.16s%.512s: Rexx condition [%.512s] raised",
                                  DLLNAME, header,
                                  (NULL != cond.conditionName && rtc->Nil() != cond.conditionName ? rtc->CString(cond.conditionName) : "conditionName n/a") );
    }

    return msg;
}


// --------------------------------------------------------------------------------------------------------------------
    // Double-linked list of RAJOs ("RexxAndJava Java objects"
typedef struct _STRUCT_RAJO {
    jobject                     rajo;               // RexxAndJava instance to communicate with
    int32_t                     refCounter;         // counter, if 0, safe to delete this node
    void                       *riid;               // Rexx interpreter instance pointer value: allow for finding JRSTs created because of a particular RIID (can be used for cleanup purposes, if RI gets terminated)
    struct _STRUCT_RAJO        *next;               // next node, if any
    struct _STRUCT_RAJO        *prev;               // previous node, if any
}  STRUCT_RAJO;

typedef STRUCT_RAJO *PSTRUCT_RAJO;                  // pointer to structure
long size_STRUCT_RAJO=sizeof(STRUCT_RAJO);          // get and memorize size of structure

PSTRUCT_RAJO pRoot_RAJO=NULL;               // root of RAJO list



// --------------------------------------------------------------------------------------------------------------------
// ---rgf, 2014-05-16: new (simplified) attach/detach logic
    // structure for maintaining thread-related call information
typedef struct _STRUCT_ATTACH_PARAM {
    JNIEnv                       *env;              // current JNIEnv or NULL
    jobject                       rajo;             // current rajo or NULL
    void                         *riid;             // to Rexx instance ID, needed e.g. for looking up rajo
    logical_t                     bDetach;          // TRUE: use DetachCurrentThread(), FALSE: do nothing
    int                           error;            // OK==0
} STRUCT_ATTACH_PARAM;

typedef STRUCT_ATTACH_PARAM *PSTRUCT_ATTACH_PARAM;          // pointer to structure
long size_STRUCT_ATTACH_PARAM=sizeof(STRUCT_ATTACH_PARAM);  // get and memorize size of structure



// ============ functions for the above structures (2009-10-03, rgf)

// 20091003, rgf: using IsSameObject() for testing rajos, if too burdersome once could
//                use the rajo's hashCode

// prototoype
RexxStringObject RgfCreateRexxSyntaxConditionWithJavaThrowable (
        PSTRUCT_ATTACH_PARAM param,
        int                  isCallContext,
        void                *argCtxt,
        CSTRING              errorStringPattern);


// --------------------------------------------------------------------------------------------------------------------
    // Create the Rexx exception message, if something went wrong with environmentAttachTo
inline void createAttachErrorMessageNew(CSTRING title, char *buf, size_t bufLen, PSTRUCT_ATTACH_PARAM param)
{
    switch (param->error)
    {
    case 1 :    // no JVM
        snprintf( buf, bufLen, "%.16s/%.64s: attach error [%d], panic! No JVM available for this Rexx interpreter instance: [%p]", DLLNAME, title, param->error, param->riid );
        break;

    case 2 :    // no RAJO
        snprintf( buf, bufLen, "%.16s/%.64s: attach error [%d], panic! No Java-RexxAndJava (RAJO) object to interact with available for this Rexx interpreter instance: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->env );
        break;

    case -1 :   // JNI_ERR
        snprintf( buf, bufLen, "%.16s/%.64s: attach error [%d], JNI_ERR (\"unknown JNI error\"), Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->rajo, param->env );
        break;

    case -2 :   // JNI_EDETACHED
        snprintf( buf, bufLen, "%.16s/%.64s: attach error [%d], JNI_EDETACHED (\"thread detached from the JVM\"), Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->rajo, param->env );
        break;

    case -3 :   // JNI_EVERSION
        snprintf( buf, bufLen, "%.16s/%.64s: attach error [%d], JNI_EVERSION (\"JNI version error\"), Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->rajo, param->env );
        break;

    case -4 :   // JNI_EMEM
        snprintf( buf, bufLen, "%.16s/%.64s: attach error [%d], JNI_EMEM (\"out of memory\"), Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->rajo, param->env );
        break;

    case -5 :   // JNI_EEXIST
        snprintf( buf, bufLen, "%.16s/%.64s: attach error [%d], JNI_EEXIST (\"JVM already exists\"), Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->rajo, param->env );
        break;

    case -6 :   // JNI_EINVAL
        snprintf( buf, bufLen, "%.16s/%.64s: attach error [%d], JNI_EINVAL (\"arguments invalid\"), Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]", DLLNAME, title, param->error, param->riid, param->rajo, param->env );
        break;

    default:
        snprintf( buf, bufLen, "%.16s/%.64s: unknown attach error [%d], Rexx interpreter instance: [%p], rajo: [%p], jenv=[%p]",
                               DLLNAME, title, param->error, param->riid, param->rajo, param->env );
    }
}


// --------------------------------------------------------------------------------------------------------------------
    // Create the Rexx exception message, if something went wrong with environmentAttachToNew
inline RexxStringObject createAttachErrorMessageRSONew(RexxCallContext *rcc, CSTRING title, PSTRUCT_ATTACH_PARAM param)
{
    char buf[512];
    createAttachErrorMessageNew(title, buf, 512, param);   // create the message
    RexxStringObject rso=rcc->String(buf);              // turn it into a RSO
    rcc->SetContextVariable(BSF_ERROR_STRING, rso);     // set context error variable
    return rso;
}


// --------------------------------------------------------------------------------------------------------------------
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

#if defined (DEBUG_JAVA_ATTACH)
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

#if defined (DEBUG_JAVA_ATTACH)
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

        // is this thread already attached ?
    if ((param->error=defaultJVM->jvm->GetEnv((void **)&param->env, USE_DEFINED_JNI_VERSION))==JNI_EDETACHED)
    {
        // nope, not yet attached, try to attach, save success in "param->error":
        if ( (param->error=defaultJVM->jvm->AttachCurrentThread((void **) &param->env, (void *) &defaultJavaVMAttachArgs)) != JNI_OK) // attach to JVM
        {
#if defined (DEBUG_JAVA_ATTACH)
    fprintf(stderr, "environmentAttachToNew() - AttachCurrentThread() did NOT work ! :-( :-(, parm->error=[%d]\n", (int) param->error);
    fflush(stderr);
#endif
            return;     // JNI attach error, return prematurely
        }

#if defined (DEBUG_JAVA_ATTACH)
        fprintf(stderr, "environmentAttachToNew() - AttachCurrentThread() WORKED, bDetach=[true]! \n");
        fflush(stderr);
#endif
        param->bDetach=true;        // remember to detach as we carried out a successful AttachCurrentThread()
    }
#if defined (DEBUG_JAVA_ATTACH)
    else    // we are already attached to Java !
    {
        fprintf(stderr, "environmentAttachToNew() - GetEnv() WORKED, we were already attached to Java, hence bDetach=[false]! \n");
        fflush(stderr);
    }
#endif


    return;
}



// --------------------------------------------------------------------------------------------------------------------
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
    #if defined (DEBUG_JAVA_ATTACH)
        fprintf(stderr, "environmentDetachFromNew(), param->bDetach=[%lu]\n", param->bDetach);
        fflush(stderr);
    #endif

    if (param->bDetach==1)   // do we need to explicitly detach ? (This will detach this thread from the JVM.)
    {

        if (defaultJVM->jvm == NULL)    // no JVM available !
        {
    #if defined (DEBUG_JAVA_ATTACH)
        fprintf(stderr, "environmentDetachFromNew() - NO JVM! defaultJVM->jvm=[%p], defaultJVM->primodal_rajo=[%p]\n", defaultJVM->jvm, defaultJVM->primodal_rajo);
        fflush(stderr);
    #endif

            param->error=1;             // no JVM available, error !
            return;
        }
        param->error=defaultJVM->jvm->DetachCurrentThread();
    #if defined (DEBUG_JAVA_ATTACH)
        fprintf(stderr, "environmentDetachFromNew() - carried out DetachCurrentThread(), result=[%d]\n", param->error);
        fflush(stderr);
    #endif

    }
    return;
}




// ---------------------------------------------------------------------------------------
    // prototypes
void RexxEntry rgfInitLocks();     // rgf, 2009-04-25, initialize critical section
void RexxEntry rgfDestroyLocks();  //                  destroy/delete it


// rgf, 2012-02-07
int REXXENTRY rexx_exit_handler_entry (RexxExitContext *context, int exitNumber, int subfunction, void *parmBlock);
RexxObjectPtr RexxEntry rexx_command_handler_entry(RexxExitContext *context, RexxStringObject address, RexxStringObject command);



// INLINE definitions

// ---------------------------------------------------------------------------------------
    // only for debugging:
inline void JAVA_TO_STRING (JNIEnv *env, jobject jo, char * buffer, int bufSize)
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
        snprintf(buffer, bufSize, "JAVA EXCEPTION OCCURED, Java object=[%p], its class object=[%p], 'toString'-MID=[%p]\n", jo, joc, jmidName);
        env->DeleteLocalRef(joc);
        env->DeleteLocalRef(jobj);
        return;
    }

    CSTRING tmpStr=env->GetStringUTFChars( (jstring) jobj, JNI_FALSE);
    snprintf(buffer, bufSize, "%s", tmpStr);    // 2009-10-02, rgf
    env->ReleaseStringUTFChars( (jstring) jobj, tmpStr);

    env->DeleteLocalRef(joc);
    env->DeleteLocalRef(jobj);
}


// ---------------------------------------------------------------------------------------
    // query current TID and return it
inline thread_id_t RgfGetTID()
{
    return
        #ifdef UNIX
            pthread_self();
        #else // if defined WIN32
            GetCurrentThreadId();
        #endif
}


// ---------------------------------------------------------------------------------------
// This function allows to serializes access to "environmentAttachToNew(&param)" and "environmentDetachFromNew(&param)"
    // ===> 2008-07-23: MUTEX, blocking invocation, waits until lock was successfully acquired
    //                  - initializing/setting up of 'JRST_lock' is done in JNI_OnLoad()
inline void RgfAcquireLock()
{

#if defined (DEBUG1)
    thread_id_t tid=RgfGetTID();

    fprintf(stderr, "*** --> RgfAcquireLock(): tid=[%lu] ... \n", (unsigned long) tid);
    fflush(stderr);
#endif

#ifdef WINDOWS
    WaitForSingleObject(JRST_lock, INFINITE);
#elif defined UNIX
    pthread_mutex_lock(&JRST_lock);
#endif


#if defined (DEBUG1)
    fprintf(stderr, "*** <-- RgfAcquireLock(): tid=[%lu] ... now ACQUIRED !\n", (unsigned long) tid);
    fflush(stderr);
#endif

}


// ---------------------------------------------------------------------------------------
// This function allows to serializes access to "environmentAttachToNew(&param)" and "environmentDetachFromNew(&param)"
    // ===> 2008-07-23: MUTEX, release lock
inline void RgfReleaseLock()
{
#if defined (DEBUG1)
    thread_id_t tid=RgfGetTID();
    fprintf(stderr, "*** ---> RgfReleaseLock(): tid=[%lu] ...\n", (unsigned long) tid);
    fflush(stderr);
#endif

#if defined WINDOWS
    ReleaseMutex(JRST_lock);
#else
    pthread_mutex_unlock(&JRST_lock);
#endif

#if defined (DEBUG1)
    fprintf(stderr, "*** <--- RgfReleaseLock(): tid=[%lu] ... now RELEASED !\n", (unsigned long) tid);
    fflush(stderr);
#endif
}



// ---------------------------------------------------------------------------------------
    // cf. "The Java™ Native Interface Programmer’s Guide and Specification"
    // by Sheng Lian, downloaded from java.sun.org in July 2003 as PDF
    // code edited to make it runnable under this DLL
    // =======================
    // cf. "6.1.2 A Utility Function"
void JNU_ThrowByName(JNIEnv *env, CSTRING clzName, CSTRING msg)
{
    jclass jclz = env->FindClass(clzName);

    if (jclz != NULL)    // if clz is NULL, an exception has already been thrown
    {
        env->ThrowNew(jclz, msg);    // create the exception for the Java side
    }
}



// ---------------------------------------------------------------------------------------
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
        result   = (char *)malloc(len + 1);
        if (result == NULL)
        {
            env->DeleteLocalRef(jba);
            JNU_ThrowByName(env, "java/lang/OutOfMemoryError", 0);
            return NULL;
        }
        env->GetByteArrayRegion(jba, 0, len, (jbyte *)result);
        result[len] = 0; /* NULL-terminate */
    }
    else
    {
        fprintf(stderr, "BSF4ooRexx: JNU_GetStringNativeChars(): Exception active, panic, please report!\n"); fflush(stderr);
        env->ExceptionDescribe();
        env->ExceptionClear();      // ignore (very unlikely) exception
    }

    env->DeleteLocalRef(jba);
    return result;      // resulting native chars buffer must be explicitly freed!
}



// ---------------------------------------------------------------------------------------
// 2012-02-12, rgf, version working directly with a supplied JNIEnv and jstr;
//                  result is assigned to a PRXSTRING
void JNU_JavaString2pRxString(JNIEnv *env, jstring jstr, PRXSTRING rxstr)  // JNIEnv *env, jstring jstr)
{
    jbyteArray jba = (jbyteArray) env->CallObjectMethod(jstr, defaultJVM->mid_String_getBytes);

    if ( env->ExceptionCheck()==JNI_FALSE )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
        jint len = env->GetArrayLength(jba);
        if (len>255)    // do we need to allocate a bigger buffer?
        {
            RexxFreeMemory(rxstr->strptr);                  // free the supplied (256 byte) buffer
            rxstr->strptr=(char *)RexxAllocateMemory(len+1);
        }

        env->GetByteArrayRegion(jba, 0, len, (jbyte *)rxstr->strptr);
        rxstr->strptr[len] = 0; /* NULL-terminate */
        rxstr->strlength=len;    // save length in bytes with RXSTRING
    }
    else
    {
        env->ExceptionClear();      // ignore (very unlikely) exception
    }

    env->DeleteLocalRef(jba);
}



// ---------------------------------------------------------------------------------------
// --- this macro creates the RexxString object, allowing embedded NULs to be part of the string
RexxObjectPtr JNU_GetStringNativeCharsReturningRexxStringObject(JNIEnv *env, jstring jstr, RexxThreadContext *context)
{
    char *result = 0;
    RexxObjectPtr rop=NULL;

    jbyteArray jba = (jbyteArray) env->CallObjectMethod(jstr, defaultJVM->mid_String_getBytes);

    if ( env->ExceptionCheck()==JNI_FALSE )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
        jint len = env->GetArrayLength(jba);

        result   = (char *)malloc(len + 1);
        if (result == NULL)
        {
            JNU_ThrowByName(env, "java/lang/OutOfMemoryError", 0);
            env->DeleteLocalRef(jba);
            return NULL;
        }
        env->GetByteArrayRegion(jba, 0, len, (jbyte *)result);
        result[len] = 0; /* NULL-terminate */

        rop=context->NewString(result, len);  // get Rexx object off it
        free(result);        // make sure memory is freed
    }
    else    // a Java exception occurred
    {
        fprintf(stderr, "BSF4ooRexx: JNU_GetStringNativeCharsReturningRexxString(): Exception active, panic, please report!\n"); fflush(stderr);
        env->ExceptionDescribe();
        env->ExceptionClear();      // ignore exception (unlikely anyway)
    }

    env->DeleteLocalRef(jba);

    return rop;         // RexxObjectPointer/RexxString
}



// ---------------------------------------------------------------------------------------
    // rgf, 20100520: controlling creation of Java strings, this version allows NUL chars be embedded in string
jstring JNU_CreateJavaString(JNIEnv *env, RexxThreadContext *context, RexxStringObject rso)
{
        // create Java byte array from Rexx string: RexxString -> [B
        // this allows Rexx strings having embedded NUL chars to be fully converted to Java
    size_t  len=context->StringLength(rso); // get length
    CSTRING buf=context->CString(rso);

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

    return env->NewStringUTF("");   // rgf, 2012-05-25: if returning "defaultJVM->jstring_emptyUTFString"
                                    // then a "DeleteLocalRef()" in caller will cause this globally sheltered
                                    // jstring to be destroyed! (observed in Java 1.6)
}


// ---------------------------------------------------------------------------------------
    // rgf, 20120210: controlling creation of Java strings, this version allows NUL chars be embedded in string
jstring JNU_CreateJavaStringFromCONSTRXSTRING(JNIEnv *env, RexxThreadContext *context, CONSTRXSTRING *rxstring)
{
        // create Java byte array from Rexx string: RexxString -> [B
        // this allows Rexx strings having embedded NUL chars to be fully converted to Java
    size_t      len=rxstring->strlength;

    if (len>0)
    {
        CSTRING     buf=rxstring->strptr;
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



// ---------------------------------------------------------------------------------------
    // rgf, 2008-08-06: use Java for popup message box (available on every opsys)
void JNU_MessageBox (JNIEnv *env, CSTRING msg)
{
    jclass    jclz = env->FindClass("javax/swing/JOptionPane");
    jmethodID  mid = env->GetStaticMethodID(jclz, "showMessageDialog", "(Ljava/awt/Component;Ljava/lang/Object;)V");
    jstring j_msg = env->NewStringUTF(msg);
    env->CallStaticVoidMethod(jclz, mid, NULL, j_msg);
    env->DeleteLocalRef(j_msg);
    env->DeleteLocalRef(jclz);
}



// ---------------------------------------------------------------------------------------
    // this table contains all available functions/routines
CSTRING ApiFncTable[]=      // if invoked via Rexx, these are the functions we report
{
    "BSF"                         ,
//    "BsfAttachToTID"              , // ---rgf, 2003-08-06, 2014-05-15: do not report anymore (auto-attach implemented)
    "BsfContextVariables"         ,  // rgf, 2015-06-07
    "BsfCreateRexxProxy"          , // ---rgf, 2009-09-12
//    "BsfDetach"                   , // ---rgf, 2008-07-26, 2014-05-15: do not report anymore (auto-attach implemented)
    "BsfDoUnregisterRexxObject"   , // ---rgf, 2016-12-20: allow control over using RgfRemoveRexxProxy() in case Java was started by Rexx
    "BsfDropFuncs"                ,
    "BsfGetRIID"                  , // ---rgf, 2017-09-25: Rexx instance ID (pointer as string)
    "BsfGetRTC"                   , // ---rgf, 2017-1o-05: Rexx thread context (pointer as string)
    "BsfGetTID"                   , // ---rgf, 2003-08-06: Rexx thread ID
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
    "BsfShowErrorMessage"         , // rgf, 2006-11-19
    "BsfUninit4JavaBean"          , // ---rgf, 2018-02-23

#ifdef ALLOW_LOADING_JVM    //   ---rgf, 2005-08-23, #idef'ed - not for OS2 and not for Macintosh
    "BsfUnloadJava"               , // ---rgf, 2005-06-02, JNI would not unload
#endif
    "BsfVersion"
};

// ---------------------------------------------------------------------------------------
CSTRING ApiFncTable4Java[]=      // if invoked via Java, these are the functions we report
{
    "BSF"                         ,
//    "BsfAttachToTID"              , // ---rgf, 2003-08-06, 2014-05-15: do not report anymore (auto-attach implemented)
    "BsfContextVariables"         ,  // rgf, 2015-06-07
    "BsfCreateRexxProxy"          , // ---rgf, 2009-09-12
//    "BsfDetach"                   , // ---rgf, 2008-07-26, 2014-05-15: do not report anymore (auto-attach implemented)
    "BsfDoUnregisterRexxObject"   , // ---rgf, 2016-12-20: allow control over using RgfRemoveRexxProxy() in case Java was started by Rexx
    "BsfDropFuncs"                ,
    "BsfGetRIID"                  , // ---rgf, 2017-09-25: Rexx instance ID (pointer as string)
    "BsfGetRTC"                   , // ---rgf, 2017-1o-05: Rexx thread context (pointer as string)
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
    "BsfUninit4JavaBean"          , // ---rgf, 2018-02-23
//    "BsfUnloadJava"               , // ---rgf, 2005-06-02, JNI would not unload
    "BsfVersion"
};



// ---------------------------------------------------------------------------------------
    // short of a make_upper macro/function, this is a little hack, ---rgf, 2003-01-27
inline void make_upper (char * string)
{
    for (; *string; string++)   // loop over characters
    {
        *string=toupper(*string);   // translate characters to uppercase
    }
}


// ---------------------------------------------------------------------------------------
    // rgf, 2011-06-04
    // create error message text, wrap Java throwable
    // - create array and add both to it, supply array as "ADDITIONAL" array to RaiseException
    // - return error message text, such that caller can use it to set its environment
    // needs: - param (jni-env, rajo, etc.)
    //        - rtc
    //        - error message pattern containing "%16" (for DLLName) and "%s" (for error message), e.g.
    //          "%.16s/routine/BsfCreateRexxProxy(), error 4: Java exception occurred: [%s]"
// isCallContext: 0=ExitContext, 1=CallContext
inline RexxStringObject RgfCreateRexxSyntaxConditionWithJavaThrowable (
    PSTRUCT_ATTACH_PARAM  param,
    int                   isCallContext,
    void                 *argCtxt,
    CSTRING               errorStringPattern)
{
    RexxThreadContext *rtc = (isCallContext==0 ? ((RexxExitContext *) argCtxt)->threadContext
                                               : ((RexxCallContext *) argCtxt)->threadContext);

    jthrowable jo=param->env->ExceptionOccurred();    // get the Exception object

    if (bShowErrorString==1)                // print the stack?
    {
        param->env->ExceptionDescribe();    // print the stack
    }
    param->env->ExceptionClear();           // clear exception in the JVM

    RexxStringObject rso=NULL;              // RexxString used for error message
    jclass joc=param->env->GetObjectClass(jo);  // get Throwable's class object

    jmethodID jmidName=param->env->GetMethodID(joc, "toString", "()Ljava/lang/String;");    // get method ID
    jobject   jobj    =param->env->CallObjectMethod(jo, jmidName, NULL);    // invoke toString()

    if ( param->env->ExceptionCheck() )     // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
        jobject jo2=param->env->ExceptionOccurred();    // get the Exception object
        param->env->ExceptionDescribe();    // show stack trace
        param->env->ExceptionClear();

        char *error_line1=new char[1024];
        snprintf( error_line1, 1024,
                              "<Unfortunately, Java was not able to create a string from the Throwable, because it threw another exception while attempting to do so! \njo=[%p]=Throwable, joc=[%p] Throwable's class, jmidName=[%p] method id for \"toString()\", jobj=[%p] (string result). :( >",
                               jo, joc, jmidName, jobj);

        // now try to fetch infos on the second Throwable
        char *error_line2=new char[1024];
        jclass joc2=param->env->GetObjectClass(jo);     // get Throwable's class object
        jmethodID jmidName2=param->env->GetMethodID(joc2, "toString", "()Ljava/lang/String;");  // get method ID
        jobject   jobj2    =param->env->CallObjectMethod(jo2, jmidName, NULL);  // invoke toString()

        if ( param->env->ExceptionCheck() ) // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
        {
            param->env->ExceptionDescribe();
            param->env->ExceptionClear();

            snprintf( error_line2, 1024,
                                  "<Unfortunately, Java was not able to create a string from the second Throwable, because it threw another exception while attempting to do so! \njo2=[%p]=Throwable, joc2=[%p] Throwables's class, jmidName2=[%p] method id for \"toString()\", jobj2=[%p] (string result). :( :( >",
                                   jo2, joc2, jmidName2, jobj2);
        }
        else    // get the Throwable's error message
        {
            CSTRING aha=param->env->GetStringUTFChars( (jstring) jobj2, JNI_FALSE);
            snprintf( error_line2, 1024,
                                  "<While trying to get the String value of the original Throwable this new exception (Throwable) got raised by Java: [%.896s] :( >", aha);
            param->env->ReleaseStringUTFChars( (jstring) jobj2, aha);
        }

        char *error_info=new char[2560];
        snprintf( error_info, 2560,"%s\n%s", error_line1, error_line2);
        rso=rtc->String(error_info);

        delete [] error_line1;
        delete [] error_line2;
        delete [] error_info;
    }
    else   // copy Java string to RexxString
    {
        CSTRING aha=param->env->GetStringUTFChars( (jstring) jobj, JNI_FALSE);
        rso=rtc->String(aha);     // create RexxStringObject
        param->env->ReleaseStringUTFChars( (jstring) jobj, aha);
    }

       // ---> create full error message for Rexx
    size_t sz=rtc->StringLength(rso);   // get needed string length
    char * tmp=(char *) malloc(sz+256);

    snprintf( tmp, sz+256, errorStringPattern, DLLNAME, rtc->StringData(rso));
    rso=rtc->String(tmp);      // turn into a RexxString
    free(tmp);

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
    CSTRING          c_beanName = param->env->GetStringUTFChars(j_beanName, JNI_FALSE);
    RexxStringObject r_beanName = rtc->String(c_beanName);    // create Rexx string
    param->env->ReleaseStringUTFChars(j_beanName, c_beanName);      // release

    RexxObjectPtr bsfClz=rtc->FindClass("BSF"); // get .BSF, if available
    RexxObjectPtr rJavaThrowable=NULL;
    if (bsfClz!=NULL)   // class found, do a bsf.wrap() on the beanName
    {
        rJavaThrowable=rtc->SendMessage1(bsfClz, "BSF.WRAP", r_beanName);  // run bsf.wrap(), may return a BSF_REFERENCE object
    }
    else    // cast RexxString to RexxObject
    {
        rJavaThrowable=(RexxObjectPtr) r_beanName;
    }

       // --- now create RexxArray used for condition's "ADDITIONAL", first entry: substitution text, second entry: Java Throwable object
    RexxArrayObject ra=rtc->NewArray(2);
    rtc->ArrayPut(ra, rso, 1);     // save Throwable's string
    rtc->ArrayPut(ra, rJavaThrowable, 2);  // save wrapped Java Throwable object

   // use Rexx{Call|Exit}Context to set context variable
if (isCallContext==0)
{
    ((RexxExitContext *) argCtxt)->SetContextVariable("BSF_ERROR_OBJECT", rJavaThrowable);
}
else
{
    ((RexxCallContext *) argCtxt)->SetContextVariable("BSF_ERROR_OBJECT", rJavaThrowable);
}


        // --- make sure we remove local locks
    param->env->DeleteLocalRef(jo);
    param->env->DeleteLocalRef(joc);       // rgf, 2018-02-23: crash in JVM 8 ! ?
    param->env->DeleteLocalRef(jobj);      // rgf, 2018-02-23: crash in JVM 8 ! ?
    param->env->DeleteLocalRef(j_beanName);  // rgf, 2018-02-23: crash in JVM 8 ! ?

    rtc->ReleaseLocalReference(rJavaThrowable);
    rtc->ReleaseLocalReference(bsfClz);
    rtc->ReleaseLocalReference(r_beanName);

       // --- raise exception

//    rtc->RaiseException(Rexx_Error_Incorrect_call_user_defined, ra);
//    rtc->ReleaseLocalReference(ra);   // ---rgf, 20171223: seemed to have caused an exception in ooRexx!

/* --- not necessary */
       // use Rexx{Call|Exit}Context to set context variable
    if (isCallContext==0)
    {
        ((RexxExitContext *) argCtxt)->RaiseException(Rexx_Error_Incorrect_call_user_defined, ra);
    }
    else
    {
        ((RexxCallContext *) argCtxt)->RaiseException(Rexx_Error_Incorrect_call_user_defined, ra);
    }
/* --- */


    return rso;            // return error string, such that caller can decide to set it to the environment as well
}

// isCallContext: 0=ExitContext, 1=CallContext
inline RexxStringObject RgfCreateRexxSyntaxConditionWithJavaThrowable_temp_debug_20220814 (
    PSTRUCT_ATTACH_PARAM  param,
    int                   isCallContext,
    void                 *argCtxt,
    CSTRING               errorStringPattern)
{
    RexxCallContext *rcc = NULLOBJECT;
    RexxExitContext *rec = NULLOBJECT;
    if (isCallContext==1)
    {
        rcc=(RexxCallContext *) argCtxt;
    }
    else
    {
        rec=(RexxExitContext *) argCtxt;
    }

/* --
    RexxThreadContext *rtc = (isCallContext==0 ? ((RexxExitContext *) argCtxt)->threadContext
                                               : ((RexxCallContext *) argCtxt)->threadContext);
-- */

    jthrowable jo=param->env->ExceptionOccurred();    // get the Exception object

    if (bShowErrorString==1)                // print the stack?
    {
        param->env->ExceptionDescribe();    // print the stack
    }
    param->env->ExceptionClear();           // clear exception in the JVM

    RexxStringObject rso=NULL;              // RexxString used for error message
    jclass joc=param->env->GetObjectClass(jo);  // get Throwable's class object

    jmethodID jmidName=param->env->GetMethodID(joc, "toString", "()Ljava/lang/String;");    // get method ID
    jobject   jobj    =param->env->CallObjectMethod(jo, jmidName, NULL);    // invoke toString()

    if ( param->env->ExceptionCheck() )     // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
        jobject jo2=param->env->ExceptionOccurred();    // get the Exception object
        param->env->ExceptionDescribe();    // show stack trace
        param->env->ExceptionClear();

        char *error_line1=new char[1024];
        snprintf( error_line1, 1024,
                              "<Unfortunately, Java was not able to create a string from the Throwable, because it threw another exception while attempting to do so! \njo=[%p]=Throwable, joc=[%p] Throwable's class, jmidName=[%p] method id for \"toString()\", jobj=[%p] (string result). :( >",
                               jo, joc, jmidName, jobj);

        // now try to fetch infos on the second Throwable
        char *error_line2=new char[1024];
        jclass joc2=param->env->GetObjectClass(jo);     // get Throwable's class object
        jmethodID jmidName2=param->env->GetMethodID(joc2, "toString", "()Ljava/lang/String;");  // get method ID
        jobject   jobj2    =param->env->CallObjectMethod(jo2, jmidName, NULL);  // invoke toString()

        if ( param->env->ExceptionCheck() ) // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
        {
            param->env->ExceptionDescribe();
            param->env->ExceptionClear();

            snprintf( error_line2, 1024,
                                  "<Unfortunately, Java was not able to create a string from the second Throwable, because it threw another exception while attempting to do so! \njo2=[%p]=Throwable, joc2=[%p] Throwables's class, jmidName2=[%p] method id for \"toString()\", jobj2=[%p] (string result). :( :( >",
                                   jo2, joc2, jmidName2, jobj2);
        }
        else    // get the Throwable's error message
        {
            CSTRING aha=param->env->GetStringUTFChars( (jstring) jobj2, JNI_FALSE);
            snprintf( error_line2, 1024,
                                  "<While trying to get the String value of the original Throwable this new exception (Throwable) got raised by Java: [%.896s] :( >", aha);
            param->env->ReleaseStringUTFChars( (jstring) jobj2, aha);
        }

        char *error_info=new char[2560];
        snprintf( error_info, 2560,"%s\n%s", error_line1, error_line2);
        if (rcc!=NULL)
        {
            rso=rcc->String(error_info);
        }
        else
        {
            rso=rec->String(error_info);
        }

        delete [] error_line1;
        delete [] error_line2;
        delete [] error_info;
    }
    else   // copy Java string to RexxString
    {
        CSTRING aha=param->env->GetStringUTFChars( (jstring) jobj, JNI_FALSE);
        if (rcc!=NULL)
        {
            rso=rcc->String(aha);     // create RexxStringObject
        }
        else
        {
            rso=rec->String(aha);     // create RexxStringObject
        }
        param->env->ReleaseStringUTFChars( (jstring) jobj, aha);
    }

       // ---> create full error message for Rexx
    size_t sz=0;
    if (rcc!=NULL)
    {
        rcc->StringLength(rso);   // get needed string length
    }
    else
    {
        rec->StringLength(rso);   // get needed string length
    }

    char * tmp=(char *) malloc(sz+256);

    if (rcc!=NULL)
    {
        snprintf( tmp, sz+256, errorStringPattern, DLLNAME, rcc->StringData(rso));
        rso=rcc->String(tmp);      // turn into a RexxString
    }
    else
    {
        snprintf( tmp, sz+256, errorStringPattern, DLLNAME, rec->StringData(rso));
        rso=rec->String(tmp);      // turn into a RexxString
    }

    free(tmp);

       // use Rexx{Call|Exit}Context to set context variable
    if (rcc!=NULL)
    {
        rcc->SetContextVariable(BSF_ERROR_STRING, rso);    // set also the context variable to the error message
    }
    else
    {
        rec->SetContextVariable(BSF_ERROR_STRING, rso);    // set also the context variable to the error message
    }

       // ---> register Java throwable in BSFRegistry
    // create a bean of the Java object; if .BSF is available, then return a bsf.wrap() of it
    jstring j_beanName = (jstring) param->env->CallObjectMethod(param->rajo,
                                       defaultJVM->mid_RexxAndJava_registerBean4JNI,
                                       jo);

    // get char* beanName, do a bsf.wrap() if available; return that result
    CSTRING          c_beanName = param->env->GetStringUTFChars(j_beanName, JNI_FALSE);

    RexxStringObject r_beanName = NULLOBJECT;
    if (rcc!=NULL)
    {
         r_beanName = rcc->String(c_beanName);    // create Rexx string
    }
    else
    {
        r_beanName = rec->String(c_beanName);    // create Rexx string
    }

    param->env->ReleaseStringUTFChars(j_beanName, c_beanName);      // release

    RexxObjectPtr bsfClz=NULLOBJECT;
    if (rcc!=NULL)
    {
        bsfClz=rcc->FindClass("BSF"); // get .BSF, if available
    }
    else
    {
        bsfClz=rec->FindClass("BSF"); // get .BSF, if available
    }

    RexxObjectPtr rJavaThrowable=NULL;
    if (bsfClz!=NULL)   // class found, do a bsf.wrap() on the beanName
    {
        if (rcc!=NULL)
        {
            rJavaThrowable=rcc->SendMessage1(bsfClz, "BSF.WRAP", r_beanName);  // run bsf.wrap(), may return a BSF_REFERENCE object
        }
        else
        {
            rJavaThrowable=rec->SendMessage1(bsfClz, "BSF.WRAP", r_beanName);  // run bsf.wrap(), may return a BSF_REFERENCE object
        }
    }
    else    // cast RexxString to RexxObject
    {
        rJavaThrowable=(RexxObjectPtr) r_beanName;
    }

       // --- now create RexxArray used for condition's "ADDITIONAL", first entry: substitution text, second entry: Java Throwable object
    RexxArrayObject ra=NULLOBJECT;

    if (rcc!=NULL)
    {
        ra=rcc->NewArray(2);
        rcc->ArrayPut(ra, rso, 1);     // save Throwable's string
        rcc->ArrayPut(ra, rJavaThrowable, 2);  // save wrapped Java Throwable object
// temporarily:
        rcc->SetContextVariable("BSF_ERROR_OBJECT", rJavaThrowable);
    }
    else
    {
        ra=rec->NewArray(2);
        rec->ArrayPut(ra, rso, 1);     // save Throwable's string
        rec->ArrayPut(ra, rJavaThrowable, 2);  // save wrapped Java Throwable object
// temporarily:
        rec->SetContextVariable("BSF_ERROR_OBJECT", rJavaThrowable);
    }

        // --- make sure we remove local locks
    param->env->DeleteLocalRef(jo);
    param->env->DeleteLocalRef(joc);       // rgf, 2018-02-23: crash in JVM 8 ! ?
    param->env->DeleteLocalRef(jobj);      // rgf, 2018-02-23: crash in JVM 8 ! ?
    param->env->DeleteLocalRef(j_beanName);  // rgf, 2018-02-23: crash in JVM 8 ! ?

    if (rcc!=NULL)
    {
        rcc->ReleaseLocalReference(rJavaThrowable);
        rcc->ReleaseLocalReference(bsfClz);
        rcc->ReleaseLocalReference(r_beanName);
        rcc->RaiseException(Rexx_Error_Incorrect_call_user_defined, ra);
        // release local reference for ra?
    }
    else
    {
        rec->ReleaseLocalReference(rJavaThrowable);
        rec->ReleaseLocalReference(bsfClz);
        rec->ReleaseLocalReference(r_beanName);
        rec->RaiseException(Rexx_Error_Incorrect_call_user_defined, ra);
        // release local reference for ra?
    }

       // --- raise exception

//    rtc->RaiseException(Rexx_Error_Incorrect_call_user_defined, ra);
//    rtc->ReleaseLocalReference(ra);   // ---rgf, 20171223: seemed to have caused an exception in ooRexx!

/* --- not necessary */
       // use Rexx{Call|Exit}Context to set context variable
    if (rcc!=NULL)
    {
    }
    else
    {
        rec->RaiseException(Rexx_Error_Incorrect_call_user_defined, ra);
    }
/* --- */


    return rso;            // return error string, such that caller can decide to set it to the environment as well
}



// ---------------------------------------------------------------------------------------
// 2012-02-18, rgf: have an own function to turn a single RexxObjectPtr to the appropriate
//                  Java object and return that
inline jobject RgfRexxObject2JavaObject(
    PSTRUCT_ATTACH_PARAM param,
    RexxThreadContext   *context,
    RexxObjectPtr        tmpObj)
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
     fprintf(stderr, "-> RgfRexxObject2JavaObject(): AFTER creating RexxProxy jresult=[%p], CheckCondition()=[%lu]\n",jresult,context->CheckCondition());fflush(stderr);
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



// ---------------------------------------------------------------------------------------
// 2012-02-10, rgf: have an own function to process a Rexx (arg) array and turn it into
//                  a Java array; meant to be used by BSF(), but also in the exit/command handlers
jobjectArray RgfRexxArray2JavaArray(
    PSTRUCT_ATTACH_PARAM param,
    RexxThreadContext   *context,
    RexxArrayObject      argArray)
{
#if defined (DEBUG_ARRAY_ARGS)  // rgf, 20090820
    fprintf(stderr, "RgfRexxArray2JavaArray():\t 1 - just arrived, CheckCondition()=[%lu]...\n", context->CheckCondition());fflush(stderr);
#endif
    if (argArray==NULL)     // no object passed in, then return NULL as well
    {
        return NULL;
    }

    size_t numArgs=context->ArraySize(argArray);    // get number of arguments

    //convert arguments array to Java format
    jobjectArray jarr = param->env->NewObjectArray (
                              (jsize) numArgs,      // needed size (number of elements)
                              defaultJVM->clz_Object,   // (*env).FindClass("java/lang/Object"),
                              NULL
                              );

#if defined (DEBUG_ARRAY_ARGS) // rgf, 20090820
    fprintf(stderr, "RgfRexxArray2JavaArray():\t 2 - after creating Java array object 'jarr', CheckCondition()=[%lu]...\n", context->CheckCondition());fflush(stderr);
#endif


#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_BSF_FUNCTION) || defined (DEBUG_ARRAY_ARGS)
    thread_id_t tid=RgfGetTID();

    fprintf(stderr, "RgfRexxArray2JavaArray():\t 3, tid=[%lu], numArgs=[%lu]", (unsigned long) tid, numArgs);fflush(stderr);
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

#if defined (DEBUG_ARRAY_ARGS) // rgf, 20120221
    fprintf(stderr, "RgfRexxArray2JavaArray():\t 4 - before returning, CheckCondition()=[%lu]...\n", context->CheckCondition());fflush(stderr);
#endif

    return jarr;
}



// ---------------------------------------------------------------------------------------
// Utility function, needed for processing Rexx handlers which stem from classic Rexx
//
// 2012-02-10, rgf: old Rexx style (i.e. only Rexx strings or empty strings !)
//                  a Java array; meant to be used by BSF(), but also in the exit/command handlers
inline jobjectArray RgfConstrxStringArray2JavaArray(
    PSTRUCT_ATTACH_PARAM param,
    RexxThreadContext   *context,
    PCONSTRXSTRING       argv,
    unsigned short       argc)
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

#ifdef DEBUG_ARRAY_ARGS
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

#ifdef DEBUG_ARRAY_ARGS
    fprintf(stderr, "RgfConstrxStringArray2JavaArray():\t 2 - before returning String array object 'jarr'...\n");fflush(stderr);
#endif

    return jarr;
}



// ---------------------------------------------------------------------------------------
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
#ifdef DEBUG
    fprintf(stderr, "*** BsfInvokedBy() 1, bsfInvokedBy=[%d] ...\n", bsfInvokedBy);
    fflush(stderr);
#endif

    switch (bsfInvokedBy) {
        case  0:  return "0";   // no JVM present
        case  1:  return "1";   // JVM present, BSF4Rexx invoked by Java
        default:  return "2";   // JVM present, BSF4Rexx invoked by Rexx
    }
}


RexxRoutine0(logical_t, BsfCheckCondition) // 20220814, ---rgf, trying to debug missing condition
{
#if defined (DEBUG) || defined (DEBUG_BSF_FUNCTION) || defined (DEBUG_CREATE_REXX_EXCEPTION)
    fprintf(stderr, "*** BSF4ooRexx: %d %s: GetCondition, context->CheckCondition()=[%d] ...\n", __LINE__, __FUNCTION__, context->CheckCondition());
    fflush(stderr);
#endif
    return context->CheckCondition();
}



// ---------------------------------------------------------------------------------------
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
    void loadJVM4Apple(POINTER arguments)
    {
    	POINTER *args=(POINTER *) arguments;
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
    	POINTER args[] = {(void *) pjniEnv, (void *) pvm_args2, (void *) &res, (void *) rgfCreateJavaVM, NULL};


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




// ---------------------------------------------------------------------------------------
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
   snprintf( msg, 1024, "%.16s/routine/BsfLoadJava(), error 1.991: functionality not compiled into this version (symbol 'ALLOW_LOADING_JVM' was not defined at compile time)", DLLNAME);
   context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
   delete[] msg;
   return NULL;

#else   // defined, make code available for compilation
   jint            res;
   jclass          j_clz;
   jmethodID       mid;
   JNIEnv *jniEnv=NULL;

   // JVM already loaded?
    if (currentJVM != NULL)
    {
        snprintf( msg, 1024, "%.16s/routine/BsfLoadJava(), error 1.999: JVM is already loaded", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }

    // all JNI versions ------------->
    char *cp_env=getenv("CLASSPATH");   // get value of environment variable CLASSPATH (NULL, if not set)

    // JNI 1.2 and above ------------>
    JavaVMInitArgs vm_args2;           // startup argument structure

       // vars for function pointers
    rgf_JNI_CreateJavaVM             rgfCreateJavaVM=NULL;
    rgf_JNI_GetDefaultJavaVMInitArgs rgfGetDefaultJavaVMInitArgs=NULL;

    #if defined  (WINDOWS) //   ( WIN32 )
        HINSTANCE hVM=NULL;

        // 2021-07-18, rgf: use JAVA_HOME first, if it exists
        {
            char *java_home = getenv("JAVA_HOME");	// try to get environment variable
            if (java_home != NULL)
            {
                size_t len = strlen(java_home)+128;
                char *buf = (char *) malloc(len);
             // try the new directory layout (since Java 9)
                snprintf(buf, len, "%s%s", java_home, "\\bin\\server\\jvm.dll");
                hVM=LoadLibrary( buf );
// fprintf(stderr, "WINDOWS: hVM=[%p] buf=[%s] ...\n", hVM, buf);

                if (hVM==NULL)    // try older directory layout (up to and often including Java 8)
                {
                    snprintf(buf, len, "%s%s", java_home, "\\jre\\bin\\server\\jvm.dll");
                    hVM=LoadLibrary( buf );
// fprintf(stderr, "WINDOWS: hVM=[%p] buf=[%s] ...\n", hVM, buf);
                }

                free(buf);
            }
        }

        // 2021-07-18, rgf.

        if (hVM==NULL)      // default to Windows finding and loading the DLL
        {
            hVM=LoadLibrary( "jvm" );   // try to load the JavaVM
// fprintf(stderr, "WINDOWS: hVM=[%p] jvm ...\n", hVM);
        }


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
       POINTER hVM = NULL;

       // 2010-08-14, rgf: new strategy, now that we have a defined place on Unix ("/opt/BSF4ooRexx")
       //                  look first in /opt/BSF4ooRexx, and thereafter let the system find "libjvm.so"
       // 2011-01-06, rgf: on Unix-based systems the BSF4ooRexx installation may have put a link to the
       //                  JVM to be used into BSF4ooRexx home directory, so look there first
       // 2021-07-18, rgf: after /opt/BSF4ooRexx (on Apple before the old /System/.. location is searched),
       //                  honor JAVA_HOME environment variable, if set

        #ifdef __APPLE__
           hVM=dlopen("/opt/BSF4ooRexx/libjvm.dylib", RTLD_LAZY | RTLD_GLOBAL ); // try link to JVM (it may not exist)
// fprintf(stderr, "APPLE: hVM=[%p] /opt/BSF4ooRexx/libjvm.dylib ...\n", hVM);

           // 2021-07-18, rgf: honor JAVA_HOME
           if (hVM==NULL)   // not found, now look for environemnet variable JAVA_HOME next
           {
               char *java_home = getenv("JAVA_HOME");	// try to get environment variable
     	       if (java_home != NULL)
    	       {

                   size_t  len = strlen(java_home)+128;
                   char *buf = (char *) malloc(len);
    			// try the new directory layout (since Java 9)
                   snprintf(buf, len, "%s%s", java_home, "/lib/server/libjvm.dylib");
                   hVM=dlopen(buf, RTLD_LAZY | RTLD_GLOBAL );
// fprintf(stderr, "APPLE: hVM=[%p] buf=[%s] ...\n", hVM, buf);

       		   if (hVM==NULL)    // try older directory layout (up to and often including Java 8)
       		   {
                       snprintf(buf, len, "%s%s", java_home, "/jre/lib/server/libjvm.dylib");
                       hVM=dlopen(buf, RTLD_LAZY | RTLD_GLOBAL );
// fprintf(stderr, "APPLE: hVM=[%p] buf=[%s] ...\n", hVM, buf);
        	   }

                   free(buf);
    	       }
           }

           if (hVM==NULL)   // not found, now look in MacOSX system location
           {
		// 2021-07-18, rgf: surprisingly this still works on macOS BigSur 11.4 although it does not exist physically
               hVM=dlopen("/System/Library/Frameworks/JavaVM.framework/JavaVM", RTLD_LAZY | RTLD_GLOBAL );
// fprintf(stderr, "APPLE: hVM=[%p] /System/Library/Frameworks/JavaVM.framework/JavaVM ...\n", hVM);
           }

        #else  // ohter Unixes, e.g. Linux

           hVM=dlopen("/opt/BSF4ooRexx/libjvm.so", RTLD_LAZY | RTLD_GLOBAL );
           // 2021-07-18, rgf: honor JAVA_HOME
           if (hVM==NULL)   // not found, now look for environemnet variable JAVA_HOME
           {
               char *java_home = getenv("JAVA_HOME");	// try to get environment variable
     	       if (java_home != NULL)
    	       {

                   size_t  len = strlen(java_home)+128;
                   char *buf = (char *) malloc(len);
    			// try the new directory layout (since Java 9)
                   snprintf(buf, len, "%s%s", java_home, "/lib/server/libjvm.so");
                   hVM=dlopen(buf, RTLD_LAZY | RTLD_GLOBAL );
// fprintf(stderr, "LINUX: hVM=[%p] buf=[%s] ...\n", hVM, buf);

        	   if (hVM==NULL)    // try older directory layout (up to and often including Java 8)
        	   {
                       snprintf(buf, len, "%s%s", java_home, "/jre/lib/server/libjvm.so");
                       hVM=dlopen(buf, RTLD_LAZY | RTLD_GLOBAL );
// fprintf(stderr, "LINUX: hVM=[%p] buf=[%s] ...\n", hVM, buf);
        	   }

                   free(buf);
    	       }
           }

        #endif


           if (hVM==NULL)   // not found, now let the system search a "libjvm.so"
           {
        #ifdef DEBUG2
            fprintf(stderr, "hvm=[%p], now looking without an explicit path...\n", hVM);
        #endif

        #ifdef __APPLE__
               hVM=dlopen("libjvm.dylib", RTLD_LAZY | RTLD_GLOBAL );   // not found, now let the system find a "libjvm.so"...
// fprintf(stderr, "hVM=[%p] libjvm.dylib ...\n", hVM);

        #else
               hVM=dlopen("libjvm.so", RTLD_LAZY | RTLD_GLOBAL );   // not found, now let the system find a "libjvm.so"...
        #endif
           }

// fprintf(stderr, "hVM=[%p] end ...\n", hVM);

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
       snprintf( msg, 1024, "%.16s/routine/BsfLoadJava(), error 2.992: Java dynamic link/shared library \"jvm.dll\" not found", DLLNAME);
#elif defined ( __APPLE__)
       snprintf( msg, 1024, "%.16s/routine/BsfLoadJava(), error 2.992: Java dynamic link/shared library \"/opt/BSF4ooRexx/libjvm.dylib\" not found (also looking for \"libjvm.dylib\" without success)", DLLNAME);
#else
       snprintf( msg, 1024, "%.16s/routine/BsfLoadJava(), error 2.992: Java dynamic link/shared library \"/opt/BSF4ooRexx/libjvm.so\" not found (also looking for \"libjvm.so\" without success)", DLLNAME);
#endif

       context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
       delete[] msg;
       return NULL;
   }



#if defined  (WINDOWS) //  ( WIN32 )
   rgfCreateJavaVM             = (rgf_JNI_CreateJavaVM)             GetProcAddress(hVM, "JNI_CreateJavaVM"); // return the address
   rgfGetDefaultJavaVMInitArgs = (rgf_JNI_GetDefaultJavaVMInitArgs) GetProcAddress(hVM, "JNI_GetDefaultJavaVMInitArgs"); // return the address

#elif defined ( UNIX )
   rgfCreateJavaVM             = (rgf_JNI_CreateJavaVM)             dlsym(hVM, "JNI_CreateJavaVM") ;
   rgfGetDefaultJavaVMInitArgs = (rgf_JNI_GetDefaultJavaVMInitArgs) dlsym(hVM, "JNI_GetDefaultJavaVMInitArgs");
#endif


    if (rgfCreateJavaVM == NULL)    // oops, no entry point found, cannot load Java !
    {
        snprintf( msg, 1024, "%.16s/routine/BsfLoadJava(), error 3.992: Java dynamic link/shared library: entry point \"JNI_CreateJavaVM\" not found", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }

    vm_args2.version = USE_DEFINED_JNI_VERSION ;
    jint resVer = rgfGetDefaultJavaVMInitArgs(&vm_args2);

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
    logical_t bCPdefined=false;  // is a classpath defined in the arguments? If so, use it otherwise use CLASSPATH value, if defined
    CSTRING needle="-Djava.class.path=";    // CLASSPATH needle to look for in the Rexx arguments; if
                                            // supplied overrides any CLASSPATH set in the environment
    size_t  needleLength=strlen(needle);

        // 2021-07-18, rgf: define 'java.library.path' on Unix systems, if not supplied; on Windows
        //                  rely on Java setting it to PATH :)
    logical_t bJLPdefined=false;  // is a classpath defined in the arguments? If so, use it otherwise use CLASSPATH value, if defined
    CSTRING jvlpNeedle="-Djava.library.path="; // CLASSPATH needle to look for in the Rexx arguments; if
    size_t  jvlpNeedleLength=strlen(jvlpNeedle);

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
        CSTRING cTmpStrObj=context->CString(tmpStrObject);     // turn to ASCII-Z string

        // determine whether java.class.path is supplied via this Rexx argument, setting/overriding CLASSPATH
        bCPdefined= (bCPdefined || (strncmp(cTmpStrObj, needle, needleLength)==0));

#if defined( UNIX )     // java.library.path
        bJLPdefined= (bJLPdefined || (strncmp(cTmpStrObj, jvlpNeedle, jvlpNeedleLength)==0));
#endif

        // create and save supplied option
        size_t tmpLength=strlen(cTmpStrObj)+2;
        char *ch = (char *) malloc(tmpLength+1);   // copy Rexx argument
        memset(ch, 0, tmpLength+1);	    // make sure that initialized to 0x00

        strncpy(ch, cTmpStrObj, tmpLength);
        options[m].optionString = ch;      //
        options[m].extraInfo    = NULL;    // Florian's Schuld! :) (2004-05-06)
        m++;                   // increase counter to reflect number of available options
    }

    if ((bCPdefined==FALSE) &&      // "java.class.path" not given in Rexx arguments
        (cp_env!= NULL))            // CLASSPATH environment variable set
    {
        // create option for setting "java.class.path" to CLASSPATH
        size_t bufLen=strlen(cp_env)+needleLength+2;
        options[m].optionString = (char *) malloc(bufLen+1);    // get needed memory
        snprintf( options[m].optionString, bufLen+1, "%s%s", needle, cp_env);

        options[m].extraInfo=NULL;
        m++;                        // increase counter to reflect number of available options
    }

#if defined( UNIX )     // java.library.path
    if ( bJLPdefined==FALSE )    // "java.library.path" not given in Rexx arguments
    {
        // create option for setting "java.class.path" to CLASSPATH
        char strJLP[] = "/opt/BSF4ooRexx:/usr/lib:/usr/lib64:/usr/local/lib:/usr/local/lib64:.";
        size_t bufLen=strlen(strJLP)+needleLength+2;
        options[m].optionString = (char *) malloc(bufLen+1);    // get needed memory
        snprintf( options[m].optionString, bufLen+1, "%s%s", jvlpNeedle, strJLP);

        options[m].extraInfo=NULL;
        m++;                        // increase counter to reflect number of available options
    }
#endif


    vm_args2.options  = options;    // set options-array (containing classpath)
    vm_args2.nOptions = m;          // indicate how many entries in options array

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


    // rgf, 2011-01-12: free malloc'ated memory for the JVM options
    for (int m=0;m<vm_args2.nOptions;m++)
    {
        free(vm_args2.options[m].optionString);
    }

    free(vm_args2.options);     // now free option array's memory

    bsfInvokedBy=2;    // invoked by Rexx (which loaded Java)

    if (res<0)
    {
// jni.h defines the return code ("res" above) as one of:
//
//     #define JNI_OK           0                 /* success */
//     #define JNI_ERR          (-1)              /* unknown error */
//     #define JNI_EDETACHED    (-2)              /* thread detached from the VM */
//     #define JNI_EVERSION     (-3)              /* JNI version error */
//     #define JNI_ENOMEM       (-4)              /* not enough memory */
//     #define JNI_EEXIST       (-5)              /* VM already created */
//     #define JNI_EINVAL       (-6)              /* invalid arguments */
        snprintf( msg, 1024, "%.16s/routine/BsfLoadJava(), error 4.995: rc=%d (\"%s\"), cannot create Java VM",
                  DLLNAME, (int) res
                  , (res==-1 ? "JNI_ERR (unkown error)" :
                        (res==-2 ? "JNI_EDETACHED (thread detached from the VM)" :
                            (res==-3 ? "JNI_EVERSION (JNI version error)" :
                                (res==-4 ? "JNI_ENOMEM (not enough memory)" :
                                    (res==-5 ? "JNI_EEXIST (VM already created)" :
                                        (res==-6 ? "JNI_EINVAL (invalid arguments)" : "unknown JNI error, lookup 'jni.h' include file")
                                     )
                                 )
                             )
                         )
                     )
                  );

        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }

    /* Find the class "Java4Rexx" */
    j_clz = (*jniEnv).FindClass(JAVA_4_REXX);    // or: cls = jniEnv->FindClass(JAVA_4_REXX);
    if (j_clz == 0) {
        snprintf( msg, 1024, "%.16s/routine/BsfLoadJava(), error 5.994: initialization error, cannot find class '%.256s'", DLLNAME, JAVA_4_REXX);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }

    /* get static Method "createInterface4Rexx()" */
    mid = jniEnv->GetStaticMethodID(j_clz, "createInterface4Rexx", "()V");
    if (mid == 0) {
        snprintf( msg, 1024, "%.16s/routine/BsfLoadJava(), error 6.993: initialization error, cannot find method 'Java4Rexx.createInterface4Rexx()", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        jniEnv->DeleteLocalRef(j_clz);
        delete[] msg;
        return NULL;
    }

    // rgf, 20090523: get and save interpreter pointer, such that later invocations will be able to find it
    //                (this function gets only invoked, if Rexx had to load Java, hence only a RexxStart() is
    //                executing
    // call the static method, which in turn will initialize BSF4Rexx by creating
    // a BSFManager and having it load the "Rexx" (support);
    // it will also invoke jniInitialize4Rexx() which will set the defaultJVM->primodal_rajo
    jniEnv->CallStaticVoidMethod(j_clz, mid, NULL);      // invoke static method

    if ( jniEnv->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
            // get Java Throwable toString() as a RexxString; will clear the pending Java exception
        RexxStringObject rso=RgfGetJavaThrowableAsString (jniEnv, context->threadContext);

            // create error message for Rexx
        char *msg2=new char[4096];  // use "msg2" to not cover "msg" from outside
        size_t sz=context->StringLength(rso);   // get needed string length, default msg2 buffer is 4 KB
        if (sz>4000)
        {
            char * tmp=(char *) malloc(sz+256);
            snprintf( tmp, sz+256, "%.16s/routine/BsfLoadJava(), error 7.991: running static method 'Java4Rexx.createInterface4Rexx()' caused Java exception: [%s]", DLLNAME, context->StringData(rso));
            rso=context->String(tmp);           // turn into a RexxString
            free(tmp);
        }
        else
        {
            snprintf( msg2, 4096, "%.16s/routine/BsfLoadJava(), error 7.991: running static method 'Java4Rexx.createInterface4Rexx()' caused Java exception: [%s]", DLLNAME, context->StringData(rso));
            rso=context->String(msg2);           // turn into a RexxString
        }

        context->SetContextVariable(BSF_ERROR_STRING, rso);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, rso);

        jniEnv->DeleteLocalRef(j_clz);
        delete[] msg2;
        delete[] msg;   // do not forget to free this buffer as well!
        return NULL;
    }


    // rgf, 2014-05-17: "createInterface4Rexx() will save the primodal_rajo and run "init_STRUCT_JVM()";
    //                  this type of invocation cannot have a Java RII configuration object, hence NULL
    // rgf, 2015-08-04: the following statement will allow jniGetRexxRootInterpreterInstanceRoot() to work
    RgfAddRexxInterpreterInstanceToList(context->threadContext, defaultJVM->primodal_rajo, NULL);


    /* get static Method "set_rii_ids()", which uses jniGetRexxRootInterpreterInstanceRoot() to get the rii_IDs to assign */
    mid = jniEnv->GetStaticMethodID(j_clz, "set_rii_ids", "()V");
    if (mid == 0) {
        snprintf( msg, 1024, "%.16s/routine/BsfLoadJava(), error 6.994: initialization error, cannot find method 'Java4Rexx.set_rii_ids()", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        jniEnv->DeleteLocalRef(j_clz);
        delete[] msg;
        return NULL;
    }

    // rgf, 20090523: get and save interpreter pointer, such that later invocations will be able to find it
    //                (this function gets only invoked, if Rexx had to load Java, hence only a RexxStart() is
    //                executing
    // call the static method, which in turn will initialize BSF4Rexx by creating
    // a BSFManager and having it load the "Rexx" (support);
    // it will also invoke jniInitialize4Rexx() which will set the defaultJVM->primodal_rajo
    jniEnv->CallStaticVoidMethod(j_clz, mid, NULL);      // invoke static method

    if ( jniEnv->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
    {
            // get Java Throwable toString() as a RexxString; will clear the pending Java exception
        RexxStringObject rso=RgfGetJavaThrowableAsString (jniEnv, context->threadContext);

        // create error message for Rexx
        char *msg2=new char[4096];  // use "msg2" to not cover "msg" from outside
        size_t sz=context->StringLength(rso);   // get needed string length, default msg2 buffer is 4 KB
        if (sz>4000)
        {
            char * tmp=(char *) malloc(sz+256);
            snprintf( tmp, sz+256, "%.16s/routine/BsfLoadJava(), error 7.992: running static method 'Java4Rexx.set_rii_ids()' caused Java exception: [%s]", DLLNAME, context->StringData(rso));
            rso=context->String(tmp);           // turn into a RexxString
            free(tmp);
        }
        else
        {
            snprintf( msg2, 4096, "%.16s/routine/BsfLoadJava(), error 7.992: running static method 'Java4Rexx.set_rii_ids()' caused Java exception: [%s]", DLLNAME, context->StringData(rso));
            rso=context->String(msg2);           // turn into a RexxString
        }

        context->SetContextVariable(BSF_ERROR_STRING, rso);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, rso);

        jniEnv->DeleteLocalRef(j_clz);
        delete[] msg2;
        delete[] msg;   // do not forget to free this buffer as well!
        return NULL;
    }


#ifdef DEBUG        // ---rgf, 2003-04-30
    fprintf(stderr, "BsfLoadJava(): about to leave...\n\n");
#endif

    // rgf, 20140330: add TID to .local to save the primodal TID for this Rexx interpreter instance
    {
        RexxDirectoryObject globalDir = context->GetGlobalEnvironment();   // get .environment
        thread_id_t              tid = RgfGetTID();

        char *strTid=new char[RGF_TID_STRING_WIDTH];
        snprintf( strTid, RGF_TID_STRING_WIDTH, "%lu%c", ((unsigned long) tid), 0);  // create decimal number

#ifdef DEBUG
    fprintf(stderr, "*** BsfLoadJava(), tid=[%lu], %%s=[%s] ...\n", (unsigned long) tid, strTid);
    fflush(stderr);
#endif

        context->DirectoryPut(globalDir, context->String(strTid), PRIMODAL_TID);
        delete[] strTid;
    }

    // ------------------------------------
    jniEnv->DeleteLocalRef(j_clz);

    delete[] msg;
    return "0";
#endif
}





// ---------------------------------------------------------------------------------------
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

#ifdef DEBUG
    fprintf(stderr, "*** BsfUnloadJava() 1 ...\n");
    fflush(stderr);
#endif
    // char msg[1024]="";
    char *msg=new char[1024];

#ifndef ALLOW_LOADING_JVM    //   if not defined!

    /* raise exception */
    snprintf( msg, 1024, "%.16s/routine/BsfUnloadJava(), error 1.991: functionality not compiled into this version (symbol 'ALLOW_LOADING_JVM' was not defined at compile time)", DLLNAME);
    context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
    delete[] msg;
    return NULL;
#else
    if (bsfInvokedBy==0)    // JVM is not loaded, error!
    {
        snprintf( msg, 1024, "%.16s/routine/BsfUnloadJava(), error 2.998: no JVM loaded at all", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }

        // bsfInvokedBy: 0=noJVM, 1=byJava, 2=byRexx
    if (bsfInvokedBy == 1) // invoked by Java, do not unload Java!
    {
        snprintf( msg, 1024, "%.16s/routine/BsfUnloadJava(), error 3.999: JVM was not loaded by Rexx", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }


    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *)context->threadContext->instance, false, 0};

    RgfAcquireLock();

    environmentAttachToNew(&param);     // attach thread to Java

    if (param.error== -1)   // attaching to Java was not successful, we are in deep troubles !
    {
        snprintf( msg, 1024, "%.16s/routine/BsfUnloadJava(), error 4.997: cannot attach thread to Java (needed for unloading Java)", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        RgfReleaseLock();
        delete[] msg;
        return NULL;
    }


       // empty JavaVM structure
    JavaVM *tmpJVM=defaultJVM->jvm;             // save pointer to JVM
    uninit_STRUCT_JVM(param.env, defaultJVM);   // will DeleteGlobalRef(...), nullify structure


    // rgf, 20140330: remove primodal TID entry in .local for this Rexx interpreter instance
    {
        RexxDirectoryObject globalDir = context->GetGlobalEnvironment();   // get .environment

#ifdef DEBUG
    thread_id_t tid=RgfGetTID();

    char *strTid=new char[RGF_TID_STRING_WIDTH];
    snprintf( strTid, RGF_TID_STRING_WIDTH, "%lu%c", ((unsigned long) tid), 0);  // create decimal number

    fprintf(stderr, "*** BsfUnloadJava(), tid=[%lu], %%s=[%s] ...\n", (unsigned long) tid, strTid);
    fflush(stderr);

    delete[] strTid;
#endif

        context->DirectoryRemove(globalDir, PRIMODAL_TID); // remove entry in .local, not useful anymore
    }


    long ret=tmpJVM->DestroyJavaVM ( ); // destroy JVM

    currentJVM  =NULL;                      // set pointer to NULL
    memset(defaultJVM, 0, size_STRUCT_JVM); // clear the structure, rgf, 20091004
    bsfInvokedBy=0;                         // reset indicator
    RgfReleaseLock();

    char *retStr=new char[32];
    snprintf( retStr, 32, "%ld%c", ret, 0);    // create return value

    delete[] msg;
    RexxStringObject rso=context->String(retStr);
    delete [] retStr;

 #ifdef DEBUG        // ---rgf, 2003-04-30
     fprintf(stderr, "BsfUnloadJava(): about to leave...\n\n");
 #endif

     return rso;

#endif
}



// ---------------------------------------------------------------------------------------
    /*********************************************************************/
    /* load (register) all BSF-functions                                 */
    /* returns:                                                          */
    /*            0 ... registering went o.k.                            */
    /*********************************************************************/
RexxRoutine0(CSTRING, BsfLoadFuncs)  // 20090505, ---rgf, obsolete, only a stub for backward compatibility
{
#ifdef DEBUG
    fprintf(stderr, "*** BsfLoadFuncs() 1 ...\n");
    fflush(stderr);
#endif

  return "0";
}


// ---------------------------------------------------------------------------------------
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
#ifdef DEBUG
    fprintf(stderr, "*** BsfDropFuncs() 1 ...\n");
    fflush(stderr);
#endif

  return "0";
}





// ---------------------------------------------------------------------------------------
    /*********************************************************************/
    /* returns the version number                                        */
    /*                                                                   */
    /*********************************************************************/
RexxRoutine0(CSTRING, BsfVersion)    // 20090505, ---rgf, only a stub for backward compatibility
{

#ifdef DEBUG
    fprintf(stderr, "*** BsfVersion() 1, version=[%.256s] ...\n", BSF_VERSION);
    fflush(stderr);
#endif

  return BSF_VERSION;
}


// ===================> NEW VERSION

// ---------------------------------------------------------------------------------------
    /*********************************************************************/
    /* fills in a stem with the function names                           */
    /*********************************************************************/
RexxRoutine1(RexxStemObject, BsfQueryAllFunctions, OPTIONAL_RexxStemObject, stem)  // 20090505, ---rgf
{
#ifdef DEBUG
    fprintf(stderr, "*** BsfQueryAllFunctions() 1 ...\n");
    fflush(stderr);
#endif

    if (stem==NULL)     // argument not given
    {
        stem=context->NewStem("BsfQueryAllFunctions");
    }

    CSTRING *tmpTable=ApiFncTable;
    int32_t entries=sizeof(ApiFncTable)/sizeof(CSTRING);
    int32_t i=0, j=0;

    for (i=0; i<entries; i++) {
        context->SetStemArrayElement(stem, ++j, context->String(tmpTable[i]));
    }
    context->SetStemArrayElement(stem, 0, context->Int32ToObject(j));

    return stem;
}



// ---------------------------------------------------------------------------------------
    /***********************************************************************************************************/
    /* Queries or sets the logical value of the globalc variable bsfDoUnregisterRexxObject (since 2016-12-20). */
    /* If set to .true, then in the case that Rexx started Java the routine                                    */
    /* Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniUnregisterRexxObject will not carry out                 */
    /* RgfRemoveProxyObject(c_obj_ID); this may be needed, if there are errors caused by Rexx shutting down    */
    /* while Java still uses jniUnregisterRexxObject (cf. RexxProxy.java).                                     */
    /*                                                                                                         */
    /* returns old value, if argument left out, returns current value                                          */
    /***********************************************************************************************************/
//                 which can be changed by the new external BSF-function BsfDoUnregisterRexxObject
RexxRoutine1(logical_t, BsfDoUnregisterRexxObject, OPTIONAL_logical_t, newValue)  // 20161220, ---rgf
{
#ifdef DEBUG
    fprintf(stderr, "*** BsfDoUnregisterRexxObject() 1 ...\n");
    fflush(stderr);
#endif

    logical_t oldValue=bsfDoUnregisterRexxObject;

    if (argumentExists(1))          // was argument supplied ?
    {
        if (newValue!=oldValue)     // set to new value
        {
            bsfDoUnregisterRexxObject=newValue;
        }
    }

    return oldValue;
}

#ifdef HOT_FIX_CRASH     // allow hot fix do be disabled!

    // ---------------------------------------------------------------------------------------
        /***********************************************************************************************************/
        /* Queries or sets the logical value of the globalc variable bsfApplyHotFixCrash (since 2017-08-21).       */
        /* Setting controls whether applying HOT_FXI_CRASH is done (1) or not (0).                                 */
        /* returns old value, if argument left out, returns current value                                          */
        /***********************************************************************************************************/
    //                 which can be changed by the new external BSF-function BsfApplyHotFixCrash
    RexxRoutine1(logical_t, BsfApplyHotFixCrash, OPTIONAL_logical_t, newValue)  // 20161220, ---rgf
    {
    #ifdef DEBUG
        fprintf(stderr, "*** BsfApplyHotFixCrash() 1 ...\n");
        fflush(stderr);
    #endif

        logical_t oldValue=bsfApplyHotFixCrash;

        if (argumentExists(1))          // was argument supplied ?
        {
            if (newValue!=oldValue)     // set to new value
            {
                bsfApplyHotFixCrash=newValue;
            }
        }

        return oldValue;
    }
#endif


// ---------------------------------------------------------------------------------------
    /*********************************************************************/
    /* fills in a stem with the names of the registered functions        */
    /*********************************************************************/
RexxRoutine1(RexxStemObject, BsfQueryRegisteredFunctions, OPTIONAL_RexxStemObject, stem)  // 20090505, ---rgf
{
#ifdef DEBUG
    fprintf(stderr, "*** BsfQueryRegisteredFunctions() 1 ...\n");
    fflush(stderr);
#endif

    if (stem==NULL)     // argument not given
    {
        stem=context->NewStem("BsfQueryRegisteredFunctions");
    }

    CSTRING   *tmpTable=NULL;
    int32_t entries=0;

#ifndef ALLOW_LOADING_JVM       // no Java un/loading available
    tmpTable=ApiFncTable4Java;
    entries=sizeof(ApiFncTable4Java)/sizeof(CSTRING);
#else
    if (bsfInvokedBy==1)
    {
        tmpTable=ApiFncTable4Java;
        entries=sizeof(ApiFncTable4Java)/sizeof(CSTRING);
    }
    else
    {
        tmpTable=ApiFncTable;
        entries=sizeof(ApiFncTable)/sizeof(CSTRING);
    }
#endif

    int32_t i=0, j=0;

    for (i=0; i<entries; i++) {
        context->SetStemArrayElement(stem, ++j, context->String(tmpTable[i]));
    }
    context->SetStemArrayElement(stem, 0, context->Int32ToObject(j));

    return stem;
}




// ---------------------------------------------------------------------------------------
    // ---rgf, 2006-11-19: allow to query and set behaviour in case of a Java exception
    //                     "1" (default)...show error string,
    //                     "0": do not show error string, instead place it as "BSF_ERROR_STRING" into the variable pool
    //                     if nor argument, only setting is returned
RexxRoutine1(CSTRING, BsfShowErrorMessage, OPTIONAL_CSTRING, strNewVal)    // 20090505, ---rgf, only a stub for backward compatibility
{

#ifdef DEBUG
    fprintf(stderr, "*** BsfShowErrorMessage() 1 ...\n");
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
            char *msg=new char[1024];

            snprintf( msg, 1024, "%.16s/routine/BsfShowErrorMessage(), error 1: illegal argument value '%.512s' (valid: '0' or '1').", DLLNAME, strNewVal);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
            return NULL;
        }
    }
    return bShowErrorString==0 ? "0" : "1";
}



// ---------------------------------------------------------------------------------------
    // ---rgf, 2003-08-06: allow Rexx to retrieve its TID (i.e. Java's "JNIEnv" object)
RexxRoutine0(RexxStringObject, BsfGetTID)    // 20090505, ---rgf, only a stub for backward compatibility
{
   thread_id_t tid=RgfGetTID();
   char    *strTid=new char[RGF_TID_STRING_WIDTH];
   snprintf( strTid, RGF_TID_STRING_WIDTH, "%lu%c", ((unsigned long) tid), 0);  // create decimal number

#ifdef DEBUG
    fprintf(stderr, "*** BsfGetTID() 1, tid=[%lu], %%s=[%s] ...\n", (unsigned long) tid, strTid);
    fflush(stderr);
#endif

   RexxStringObject rso=context->String(strTid);
   delete[] strTid;

   return rso;
}



// ---------------------------------------------------------------------------------------
    // ---rgf, 2014-05-17: not needed anymore (deprecated), return true to allow older programs to continue to run
RexxRoutine1(logical_t, BsfAttachToTID, OPTIONAL_CSTRING, toTID)    // 20090505, ---rgf, only a stub for backward compatibility
{
    return true;
}


// ---------------------------------------------------------------------------------------
    // ---rgf, 2014-05-17: not needed anymore (deprecated), return true to allow older programs to continue to run
RexxRoutine0(logical_t, BsfDetach)    // 20090505, ---rgf, only a stub for backward compatibility
{
    return true;
}




// ---------------------------------------------------------------------------------------
    // ---rgf, 2009-09-25: allow Rexx to retrieve the Rexx instance ID (the instance pointer as string)
RexxRoutine0(RexxStringObject, BsfGetRIID)
{
    RexxInstance *ri=context->threadContext->instance;  // get the RexxThreadContext pointer
#ifdef DEBUG
    fprintf(stderr, "*** BsfGetRIID(): %%lu=[%lu], %%p=[%p] \n", (long unsigned int) ri, ri);
    fflush(stderr);
#endif

   // char strRIID[32]="";         // define a string long enough to receive an integer
   char *strRIID=new char[RGF_POINTER_STRING_WIDTH];
   // sprintf(strRIID, "%p%c", ((unsigned long) ri), 0);  // create decimal number
   snprintf( strRIID, RGF_POINTER_STRING_WIDTH, "%p%c", ri, 0);  // create decimal number

   RexxStringObject rso=context->String(strRIID);
   delete  [] strRIID;
   return rso;
}

// ---------------------------------------------------------------------------------------

    // for debugging
    // ---rgf, 2017-10-05: allow Rexx to retrieve the Rexx thread context pointer as a string
RexxRoutine0(RexxStringObject, BsfGetRTC)
{
    RexxInstance *ri=context->threadContext->instance;  // get the RexxThreadContext pointer
#ifdef DEBUG
    fprintf(stderr, "*** BsfGetRIID(): %%lu=[%lu], %%p=[%p] \n", (long unsigned int) ri, ri);
    fflush(stderr);
#endif

   // char strRIID[32]="";         // define a string long enough to receive an integer
   char *strRTC=new char[RGF_POINTER_STRING_WIDTH];
   // sprintf(strRIID, "%p%c", ((unsigned long) ri), 0);  // create decimal number
   snprintf( strRTC, RGF_POINTER_STRING_WIDTH, "%p%c", ri, 0);  // create decimal number

   RexxStringObject rso=context->String(strRTC);
   delete  [] strRTC;
   return rso;
}




// ---------------------------------------------------------------------------------------
//    // rgf, 2009-05-09
// / * creates a RexxProxy on the Java side, caches Rexx object and optional user data object
//
//            // used to create a RexxProxy
//        arg1=RexxObject or CodeString
//        arg2=optional, userData (any Rexx object)
//
//            // if given, then create and return a JavaProxy object
//        arg3=optional, Java class (string, clz, jobj), ... if "REXXOBJECT" then wrap arg1 for Java
//        argn=          - " -
//
//   returns BSF Registry index name, either for RexxProxy or JavaProxy object
// * /
RexxRoutine1(RexxObjectPtr, BsfCreateRexxProxy, ARGLIST, argArray)    // 20090505, ---rgf, only a stub for backward compatibility
{
    thread_id_t tid=RgfGetTID();    // current thread

#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "*** BsfCreateRexxProxy() 1, BEGIN: tid=[%lu] ...\n",
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
        snprintf( msg, 4096, "%.16s/routine/BsfCreateRexxProxy(), error 1: first argument (a Rexx object or a string) is missing", DLLNAME);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete [] c_rii_ID;
        delete [] msg;
        return NULL;
    }

       // get the package object from caller
    RexxPackageObject rpo=(RexxPackageObject) context->SendMessage0(context->GetCallerContext(),"PACKAGE");

    RexxObjectPtr r_obj     =context->ArrayAt(argArray,1);

    // an undocumented feature: if the first argument is a Rexx object of type "STRING", "METHOD" or "ARRAY"
    // then the companion Rexx program "BSF_OnTheFly.cls" is used to create a Rexx object on the fly with
    // a single unknown method whose code is this first argument, unless the third argument is "REXX[OBJECT]";
    // in general the Rexx object in the first argumetn will get proxied for Java to become able to interact
    // with it directly: this allows implementing Java interfaces and abstract classes in Rexx (and even
    // extend Java classes with Rexx classes)
    logical_t bCreateJavaProxy=true;    // by default create a JavaProxy, if more than two arguments are given
    if (context->IsOfType(r_obj, "STRING") || context->IsOfType(r_obj, "METHOD")  || context->IsOfType(r_obj, "ROUTINE")  || context->IsOfType(r_obj, "ARRAY") )  // a string object in hand? If so, it's the code to be run!
    {
        logical_t bUseArg1ForRexxCode=true;      // use argument as Rexx code for BSF_OnTheFly.rex
        RexxObjectPtr arg3=context->ArrayAt(argArray,3);    // get third argument if any
        if (arg3!=NULL && context->IsOfType(arg3,"STRING"))        // is third argument "R[EXXOBJECT]", then Rexx object in arg1 is supposed to be proxied
        {
            RexxObjectPtr res=context->SendMessage1(context->String("REXXOBJECT"), "CASELESSABBREV", arg3);
            bUseArg1ForRexxCode= (res==context->False());      // use BSF_OnTheFly using arg1 as code for unknown method and proxy returned object instead
            bCreateJavaProxy=bUseArg1ForRexxCode;
        }


        if (bUseArg1ForRexxCode)        // use arg1 as code argument for BSF_onTheFly.rex
        {
            r_obj=context->CallProgram(REXX_PROXY_STUB_PROGRAM, context->ArrayOfOne(r_obj));
            if (context->CheckCondition()) // oops, something went wrong, let the condition prevail
            {
                char *tmpStr=new char[1024];
                snprintf(tmpStr, 1024, "/routine/BsfCreateRexxProxy(), error 2: [Rexx raised condition while calling \"%s\" to attach code to dynamically]", REXX_PROXY_STUB_PROGRAM);

                    // get msg with embedded condition info
                char * tmpMsg=RgfCreateRexxlikeErrorInfo (context->threadContext,
                                                       context->GetConditionInfo(),
                                                       tmpStr);
                                                       // "/routine/BsfCreateRexxProxy() error 2: [Rexx raised condition while calling \"BSF_OnTheFly.cls\" to attach code to]");
                context->ClearCondition();

                    // now raise new exception
                context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(tmpMsg));
                free(tmpMsg);        // free memory
                delete [] tmpStr;
                delete [] c_rii_ID;
                delete [] msg;
                return NULL;
            }
        }
    }

    RexxObjectPtr r_userData=(context->ArrayAt(argArray, 2)!=NULL ? context->ArrayAt(argArray,2) : NULL);

    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *)context->threadContext->instance, false, 0};

    RgfAcquireLock();
    environmentAttachToNew(&param); // attach to Java

    if (param.error!= 0)    // problem attaching, cannot proceed
    {
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined,
                                 createAttachErrorMessageRSONew(context, "routine/BsfCreateRexxProxy(), error 3", &param)
                                 );
        RgfReleaseLock();
        delete [] c_rii_ID;
        delete [] msg;
        return NULL;
    }
    RgfReleaseLock();

        // create a Java RexxProxy
    jobject j_rexxProxy=RgfCreateRexxProxy (param.env, param.rajo,
                                            context->threadContext, // ThreadContext, RMG: 20090514
                                            r_obj,              // Rexx object
                                            rpo,                // Package object
                                            r_userData          // userData (slot)
                                            );

    // process received arguments
    size_t numArgs=context->ArraySize(argArray);   // get number of arguments

    if (numArgs>2 && bCreateJavaProxy)      // o.k. creation of a JavaProxy is requested, third argument is not "R[EXXOBJECT]"
    {
        numArgs=numArgs-2;
        //convert arguments array to Java format
        jobjectArray jarr = param.env->NewObjectArray (
                                  (jsize) numArgs,  // needed size (number of elements)
                                  defaultJVM->clz_Object,       // type
                                  // a_JRST->penv->FindClass("java/lang/Object"),    // type
                                  NULL              // initial value
                                  );

        RexxObjectPtr bsfClz=context->FindClass("BSF"); // get .BSF, if available

            // fill the Java argument array with the passed in objects
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

                    if (bsfClz!=NULL && context->IsInstanceOf(tmpObj, (RexxClassObject) bsfClz))
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
            environmentDetachFromNew(&param);
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
    RexxObjectPtr r_result=(RexxObjectPtr) context->String(c_bsfRegistryKey);

        // if returned string looks like BSF.CLS is active, run BSF.WRAP to return a BSF_REFERENCE instead of the name only
    if (c_bsfRegistryKey[0]=='<' && c_bsfRegistryKey[2]=='>')   // looks like BSF.CLS induced prefix "<S>" or "<O>" is active, hence run bsf.wrap()
    {
        RexxClassObject clz=context->FindClass("BSF");  // try to get the BSF class
        if (clz!=NULL)  // class found?
        {
            r_result=context->SendMessage1(clz, "BSF.WRAP", r_result);  // run bsf.wrap(), may return a BSF_REFERENCE object
        }
        else
        {
            r_result=(RexxObjectPtr) context->String(c_bsfRegistryKey+3);
        }
    }

    free(c_bsfRegistryKey);

    param.env->DeleteLocalRef(j_rexxProxy);
    param.env->DeleteLocalRef(j_bsfRegistryKey);    // 2018-02-23, crash in JVM 8 ! ?

    RgfAcquireLock();
    environmentDetachFromNew(&param);
    RgfReleaseLock();

#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "*** BsfCreateRexxProxy() 2, END  : tid=[%lu] ...\n",
            (unsigned long) tid
            );
    fflush(stderr);
#endif

    delete [] c_rii_ID;
    delete [] msg;
    return r_result;        // return Rexx object
}





// ---------------------------------------------------------------------------------------
    // ---rgf, 2009-09-06: usage BsfRexxProxy(proxy [, "ooRexxObject" | "userData" | "refCount"])
    //      argument 1:
    //          proxy   ... must be a RexxProxy object, i.e. a BSF-wrapped Java RexxProxy object
    //      argument 2:
    //          "ooRexxObject" (default)    ... return ooRexx object that gets proxied
    //          "userData"                  ... return ooRexx object representing the supplied "userData" object at creation time, or .nil
    //          "refCount"                  ... return actual number of references for this RexxProxy
RexxRoutine2(RexxObjectPtr, BsfRexxProxy, RexxObjectPtr, proxy, OPTIONAL_CSTRING, arg2val)
{

#if defined (DEBUG_REXX_PROXY)
    thread_id_t tid=RgfGetTID();    // current thread
#endif

#ifdef DEBUG_REXX_PROXY
    fprintf(stderr, "*** BsfRexxProxy() 1, tid=[%lu] ... AO\n", (unsigned long) tid);
    fflush(stderr);
#endif

    // process argument 1, check whether proxy is a RexxProxy indeed
    // 1) instance of BSF or a subclass ? (a must)
    RexxStringObject rexxObjectID=NULL;
    logical_t flag=context->IsOfType(proxy, "BSF");

    if (flag==1)        // o.k., proceed
    {

#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "*** *** BsfRexxProxy(): [1] 'flag=1' - rrid=[%p], flag=[%d], condition? [%d] ... \n",
                    context->threadContext->instance, (int) flag, (int) context->CheckCondition());
    fflush(stderr);
#endif

        // try to get the RexxObjectID
        rexxObjectID=(RexxStringObject) context->SendMessage0(proxy, "GETREXXOBJECTID");   // get RexxObjectID
    }

#if defined (DEBUG_REXX_PROXY)
    else
    {
        fprintf(stderr, "*** *** BsfRexxProxy(): [1] 'flag=1' - rrid=[%p], flag=[%d], condition? [%d], BSF~string=[%s], proxy~string=[%s] ... \n",
                        context->threadContext->instance, (int) flag, (int) context->CheckCondition(),
                        context->ObjectToStringValue(context->DirectoryAt(context->GetGlobalEnvironment(), "BSF")),
                        context->ObjectToStringValue(proxy)
                        );
        fflush(stderr);
    }
#endif


#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "*** *** BsfRexxProxy(): [2] 'getRexxObjectId' - rrid=[%p], flag=[%d], rexxObjectID=[%p], condition? [%d] ... \n",
                    context->threadContext->instance, (int) flag, rexxObjectID, (int) context->CheckCondition());
    fflush(stderr);


    fprintf(stderr, "*** *** BsfRexxProxy(), exceptional state? tid=[%lu], flag=[%d], rexxObjectID=[%p], condition? [%d] ... ZO\n",
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

        if (bCondition)  // clear a pending condition
        {
            RexxDirectoryObject condObj=context->GetConditionInfo();
            char * msg=RgfCreateRexxlikeErrorInfo (context->threadContext, condObj, "");
            snprintf( cond_msg, 2048, " [underlying Rexx condition: '%.1536s']", msg);
            free(msg);

            context->ClearCondition();
        }

        // context->InvalidRoutine();
        char *msg=new char[4096];
        if (flag==0 || bCondition==1)
        {
            snprintf( msg, 4096, "%.16s/routine/BsfRexxProxy(), error 1: argument 1 must be RexxProxy object, received '%.1536s' of type '%s' '%.2048s'",
                         DLLNAME,
                         context->ObjectToStringValue(proxy),
                         context->ObjectToStringValue( context->SendMessage0(proxy, "CLASS") ),
                         cond_msg
                         );
        }
        else    // rexxObjectId == NULL
        {
            snprintf( msg, 4096, "%.16s/routine/BsfRexxProxy(), error 2: argument 1 does not embed a Rexx object (returns \"NULL\"), supplied RexxProxy object: '%.1536s' of type '%s' ",
                         DLLNAME,
                         context->ObjectToStringValue(proxy),
                         context->ObjectToStringValue( context->SendMessage0(proxy, "CLASS") )
                         );
        }

        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete [] cond_msg;
        delete [] msg;
        return NULL;
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
        char *msg=new char[4096];
        snprintf( msg, 4096, "%.16s/routine/BsfRexxProxy(), error 3: illegal argument 2 value '%.3900s' (valid: 'ooRexxObject', 'userData', 'refCount').", DLLNAME, arg2val);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete [] msg;
        return NULL;
    }
    return rop;
}





// ---------------------------------------------------------------------------------------
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
    thread_id_t tid=RgfGetTID();    // current thread

#ifdef DEBUG
    fprintf(stderr, "*** BsfRawBytes() 1, tid=[%lu] ... AO\n", (unsigned long) tid);
    fflush(stderr);
#endif

    // process argument 1, check whether proxy is a RexxProxy indeed
    // 1) instance of BSF or a subclass ? (a must)
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
        char *msg=new char[1024];
        snprintf( msg, 1024, "%.16s/routine/BsfRawBytes(), error 1: argument neither a .String nor a .BSF object (representing a single dimensioned primitive byte array). Received argument=[%.512s]",
                             DLLNAME,
                             context->CString(context->ObjectToString(data))
                             );
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }


    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *)context->threadContext->instance, false, 0};
    RgfAcquireLock();
    environmentAttachToNew(&param); // attach to Java

#ifdef DEBUG2
    if (param.error!=0)
    {
        fprintf(stderr, "---> BSFRawBytes() // 1--> param.error=[%d]\n", param.error); fflush(stderr);
    }
#endif

    if (param.error!=0)    // rgf, 2014-03-30, problem attaching, cannot proceed
    {
        RgfReleaseLock();
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined,
                                 createAttachErrorMessageRSONew
                                 (context, "routine/BsfRawBytes(), error 2", &param)
                                 );
        return NULL;
    }
    RgfReleaseLock();

    RexxObjectPtr result_Obj=NULL;

    if (kind==1)                // RexxString -> [B
    {
        // create [B from RexxString, put into BSFRegistry, return it
        // 2010-05-23, rgf: could be that internally it is not (yet) a pure RexxStringObject
        RexxStringObject rso_data= context->IsString(data) ? (RexxStringObject) data
                                                           : context->ObjectToString(data) ;

        size_t      len=context->StringLength(rso_data); // get length

        if (argumentExists(2))      // lengthToUse argument given
        {
            if (len<lengthToUse)    // Rexx string not long enough, raise error
            {
                errorKind=1;        // indicate error

                char *msg=new char[1024];
                snprintf( msg, 1024, "%.16s/routine/BsfRawBytes(), error 3: argument 2 ('lengthToUse') value (%zu) larger than maximum length (%zu)",
                                     DLLNAME,
                                     lengthToUse, len
                                     );
                context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                delete[] msg;
                result_Obj=NULL;
            }
            else
            {
                len=lengthToUse;    // use supplied length
            }
        }

        if (errorKind==0)   // no error, o.k. to proceed
        {
            CSTRING buf=context->CString(rso_data);
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
                    CSTRING          c_beanName=param.env->GetStringUTFChars(j_beanName, JNI_FALSE);
                    RexxStringObject r_beanName=context->String(c_beanName);    // create Rexx string
                    param.env->ReleaseStringUTFChars(j_beanName, c_beanName);         // release
                    param.env->DeleteLocalRef(j_beanName);

                    RexxObjectPtr bsfClz=context->FindClass("BSF"); // get .BSF, if available
                    if (bsfClz!=NULL)  // class found, do a bsf.wrap() on the beanName
                    {
                        result_Obj=context->SendMessage1(bsfClz, "BSF.WRAP", r_beanName);  // run bsf.wrap(), may return a BSF_REFERENCE object
                    }
                    else    // just return the supplied beanName (a String)
                    {
                        result_Obj=r_beanName;
                    }
                }
            }
            param.env->DeleteLocalRef(jba);     // rgf, 2018-02-23, causes crash in JVM 8 ! ?
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
            char *msg=new char[4096];
            snprintf( msg, 4096, "%.16s/routine/BsfRawBytes(), error 4: supplied argument (primitive Java byte array) with the beanName=[%s] cannot be found in the BSFRegistry)", DLLNAME, objectname);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
            result_Obj=NULL;
        }
        else                // now process the jbyteArray
        {
            size_t len=(size_t) param.env->GetArrayLength(jba);

            if (argumentExists(2))      // lengthToUse argument given
            {
                if (len<lengthToUse)    // Rexx string not long enough, raise error
                {
                    errorKind=1;        // indicate error

                    char *msg=new char[1024];
                    snprintf( msg, 1024, "%.16s/routine/BsfRawBytes(), error 3: argument 2 ('lengthToUse') value (%zu) larger than maximum length (%zu)",
                                         DLLNAME,
                                         lengthToUse, len
                                         );
                    context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                    delete[] msg;
                    result_Obj=NULL;
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
                    char *buf=(char *) malloc( len );
                    param.env->GetByteArrayRegion(jba, 0, (jsize) len, (jbyte *) buf);

                    if ( ! param.env->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
                    {
                        result_Obj=context->NewString(buf, len);
                    }
                    free(buf);
                }
            }
        }
        param.env->DeleteLocalRef(jba);         // rgf, 2018-02-23, causes crash in JVM 8 ! ?
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
    environmentDetachFromNew(&param);
    RgfReleaseLock();

    return result_Obj;
}




// ---------------------------------------------------------------------------------------
//
// * External Rexx function "BSF". This function allows Rexx programs to call into Java.
//
RexxRoutine1(RexxObjectPtr, BSF, ARGLIST, argArray)    // 20090505, ---rgf, only a stub for backward compatibility
{
    thread_id_t tid=RgfGetTID();
    char *msg=new char[4096];       // error message buffer

#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_BSF_FUNCTION)
    static int countBSF=0;
    countBSF++;
    fprintf(stderr, "*** BSF() 1, tid=[%lu]/countBSF=[%d], ", (unsigned long) tid, countBSF);
    fflush(stderr);
#endif

    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *)context->threadContext->instance, false, 0};
    RgfAcquireLock();
    environmentAttachToNew(&param); // attach to Java

    if (param.error!= 0)    // problem attaching, cannot proceed
    {
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined,
                                 createAttachErrorMessageRSONew
                                 (context, "routine/Bsf(), error 1", &param)
                                 );
        RgfReleaseLock();
        delete[] msg;
        return NULL;
    }
    RgfReleaseLock();

    size_t numArgs=context->ArraySize(argArray);   // get number of arguments

    //convert arguments array to Java format
    jobjectArray jarr = param.env->NewObjectArray (
                              (jsize) numArgs,      // needed size (number of elements)
                              defaultJVM->clz_String,   // (*env).FindClass("java/lang/String"),
                              defaultJVM->jstring_emptyUTFString// (*env).NewStringUTF("")
                              );

    // 4.0: if creating RexxProxy objects, they get registered in BSFRegistry, hence
    //      we need to remove them from the BSFRegistry after the BSF-invocation; this
    //      array will remember the beanNames of these
    jobjectArray jarrToRemove=param.env->NewObjectArray(
                               (jsize) numArgs,     // the maximum number of entries
                               defaultJVM->clz_String,  // String type
                               NULL                 // use NULL for initialization
                               );

    size_t nrToRemove=0;                        // memorize nr of entries to remove


#if defined (DEBUG_REXX_PROXY_BSF) || defined (DEBUG_BSF_FUNCTION)
    fprintf(stderr, "--- BSF() 1, tid=[%lu]/countBSF=[%d], numArgs=[%lu]", (unsigned long) tid, countBSF, numArgs);fflush(stderr);
    if (numArgs>0)
    {

    // rgf, 20090820
    fprintf(stderr, "\n"); fflush(stderr);
    for (size_t i=0; i<numArgs; i++)
    {
       RexxObjectPtr tmpObj=context->ArrayAt(argArray, i+1);

       fprintf(stderr, "\tBSF() 1, tid=[%lu]/countBSF=[%d] | i=%lu, tmpObj: %%lu=%lu %%p=%p, %%s=[%.256s]",
                              (unsigned long) tid, countBSF,
                              i+1,
                              (unsigned long) tmpObj,
                              tmpObj,
                              ( tmpObj==NULL ? "<null>"
                                             : context->CString(context->ObjectToString(tmpObj))
                              )
              );
       fflush(stderr);

/* --- rgf, 20171223
       // FindClass(): searches environment sequence; FindPackageClass() searches package plus environment sequence
       // RgfValue4JavaIndicator() checks RexxObject and returns:
       //             -2 for .nil,
       //             -1 for NULL,
       //             >=0 a RexxObject:
       //                   0 any RexxObject that is not one of:
       //                     1 for a RexxString or
       //                     2 for a beanname (Java object resides in BSF registry)
       int tind=RgfValue4JavaIndicator(context->threadContext, tmpObj);
       fprintf(stderr, ", typeIndicator=%d | BSF() 1, tid=[%lu]/countBSF=[%d]\n", tind, (unsigned long) tid, countBSF);
       fflush(stderr);

       RexxObjectPtr ctxt=context->GetCallerContext();
       RexxObjectPtr pkg =context->SendMessage0(ctxt, "PACKAGE");
       RexxObjectPtr clzBSF=context->SendMessage1(pkg, "FINDPUBLICCLASS", context->NewStringFromAsciiz("BSF"));
       RexxObjectPtr findClzBSF=context->FindClass("BSF");

       fprintf(stderr, "\n\t ... ->| ctxt=[%p], pkg=[%p], clzBSF=[%p], str=[%s]; tmpObj=[%s] isA(\"BSF\")=[%i]; FindClass(\"BSF\")=[%p], str=[%s] \n",
                       ctxt, pkg, clzBSF,
                       context->ObjectToStringValue(clzBSF),
                       context->ObjectToStringValue(tmpObj),
                       (int) (context->IsOfType(tmpObj, "BSF")),
                       findClzBSF,
                       (findClzBSF==NULL ? "<null>" : context->ObjectToStringValue(findClzBSF))
               );
       fflush(stderr);
--- */

    }

/* ---- rgf, 20171119
        for (size_t i=0; i<numArgs; i++)   // iterate over all Rexx arguments
        {
            RexxObjectPtr tmpObj=context->ArrayAt(argArray, i+1);
            int tind=RgfValue4JavaIndicator(context->threadContext, tmpObj);
            fprintf(stderr, "[%lu]=", i+1); fflush(stderr);
            fprintf(stderr, "[%.256s]", (tind<0?"<null>":context->CString(tmpObj))); fflush(stderr);
            // fprintf(stderr, " | [%u]=[%.256s]", i+1, context->CString(context->ArrayAt(argArray, i+1)));
        }
---- */

    }
    fprintf(stderr, " --- tid=[%lu]/countBSF=[%d]\n", (unsigned long) tid, countBSF);
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
#endif

                    // get the ooRexx String class object
                if (typeIndicator==1)   // a .STRING
                {

#if defined (DEBUG_REXX_PROXY_BSF)
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
                else    // this is a non-String Rexx object, create a proxy for Java !
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

#if defined (DEBUG_BSF_FUNCTION)
    fprintf(stderr, "*** *** DEBUG_BSF_FUNCTION: BSF() 4 - <==> (BEFORE -> CallObjectMethod(), tid=[%lu]/countBSF=[%d] ... ", (unsigned long) tid, countBSF);
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

#if defined (DEBUG_BSF_FUNCTION)
    fprintf(stderr, "*** *** DEBUG_BSF_FUNCTION: BSF() 5 - <==> (AFTER  -> CallObjectMethod(), tid=[%lu]/countBSF=[%d], j_returnValue=[%p] ...)\n", (unsigned long) tid, countBSF, j_returnValue);
    fflush(stderr);
#endif

                // pending exception ?
        if ( param.env->ExceptionCheck() )    // lighter (jboolean: JNI_TRUE, if pending exception), does not create exception object
        {
#if defined (DEBUG_BSF_FUNCTION)
    fprintf(stderr, "*** *** DEBUG_BSF_FUNCTION: BSF() 5b: Java exception, now: (RgfCreateRexxSyntaxConditionWithJavaThrowable()) \n");
    fflush(stderr);
#endif
            RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
                 1,         // RexxCallContext
                 context,
                 "%.16s/routine/BSF(), error 3: Java exception occurred: [%s]");

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
            environmentDetachFromNew(&param);
            RgfReleaseLock();
            delete[] msg;

#if defined (DEBUG_BSF_FUNCTION)
    // fprintf(stderr, "*** *** DEBUG_BSF_FUNCTION: BSF() 5c: about to return with NULL: context->CheckCondition()=%d | context->threadContext->CheckCondition()=%d\n", context->CheckCondition(), context->threadContext->CheckCondition());
    fprintf(stderr, "*** *** DEBUG_BSF_FUNCTION: BSF() 5c: about to return with NULL: context->CheckCondition()=%d \n", context->CheckCondition());
    fflush(stderr);
#endif

            return NULL;
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
            environmentDetachFromNew(&param);
            RgfReleaseLock();

#if defined (DEBUG_BSF_FUNCTION)
    fprintf(stderr, "<-- BSF() 6, tid=[%lu]/countBSF=[%d], upon return to Rexx, Java returned NULL, we return \"\", context->CheckCondition()=[%d] (all o.k., should be 0)\n\n",
                                (unsigned long) tid,  countBSF,
                                (int) context->CheckCondition()
                                );
    fflush(stderr);
#endif

            delete[] msg;
            return context->NullString();
        }

        RexxObjectPtr result=JNU_GetStringNativeCharsReturningRexxStringObject(param.env, j_returnValue, context->threadContext);       // rgf, 2009-04-20, 20090515
        param.env->DeleteLocalRef(j_returnValue);

        RgfAcquireLock();
        environmentDetachFromNew(&param);
        RgfReleaseLock();

#if defined (DEBUG_BSF_FUNCTION)
    fprintf(stderr, "<-- BSF() 6, tid=[%lu]/countBSF=[%d], upon return to Rexx with a result from Java, context->CheckCondition()=[%d] (all o.k., should be 0), result=[%.256s]\n\n",
                            (unsigned long) tid, countBSF,
                            (int) context->CheckCondition(),
                            ( result==NULL ? "<null>"
                                           : context->CString(context->ObjectToString(result))
                            )
                            );
    fflush(stderr);
#endif

        delete[] msg;
        return result;
}



// ---------------------------------------------------------------------------------------
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
RexxRoutine2(RexxObjectPtr, BsfJavaException, OPTIONAL_CSTRING, option, OPTIONAL_RexxObjectPtr, exceptionInfo)
{
    thread_id_t tid=RgfGetTID();    // current thread

#if defined (DEBUG_REXX_PROXY)
    fprintf(stderr, "*** BsfJavaException() 1, BEGIN: tid=[%lu] ...\n",
            (unsigned long) tid
            );
    fflush(stderr);
#endif

    int flag=0;     // 1=throw,throwableObj, 2=check, 3=clear, 4=object/occurred, 5=fatalError, msg

    if (argumentExists(1))
    {
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
    }
    else
    {
        flag=2;    // default to checking for a pending Java exception; returns 1, if pending Java exception, 0 else
    }


    char *msg=new char[4096];

    if (flag==0)        // no valid option given
    {
        // do not document "fatalError" in error message; should not be known to regular script user
        // as 'fatalError' will usually bring down the entire JVM
        snprintf( msg, 4096, "%.16s/routine/BsfJavaException(), error 1: argument 1 value '%s' is invalid; valid values: 'CH[eck]', 'CL[ear]', 'O[ccurred]', 'T[hrow]'", DLLNAME, option);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }


    char *c_beanNameOrMsg   =NULL;  // the beanName of the exceptionProxy
    RexxObjectPtr bsfClz=context->FindClass("BSF"); // get .BSF, if available

    if (flag==1)        // throw exception in Java, needs second argument
    {
        if (exceptionInfo==NULL)    // omitted exceptionObject ?
        {
            snprintf( msg, 4096, "%.16s/routine/BsfJavaException(), error 2: argument 2 (a Java Throwable object) missing", DLLNAME);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
            return NULL;
        }

        // check argument, get beanName as a "char *" ------------------------------------------------------
        RexxStringObject rso=NULL;  // RexxStringObject

        if (bsfClz!=NULL && context->IsInstanceOf(exceptionInfo, (RexxClassObject) bsfClz))
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
            snprintf( msg, 4096, "%.16s/routine/BsfJavaException(), error 2: argument 1 (supplied option: 'FatalError', Sun's JNI documents: \"Raises a fatal error and does not expect the VM to recover. This function does not return.\") needs a mandatory second argument (the fatal error message)", DLLNAME);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
            return NULL;
        }

        c_beanNameOrMsg=(char *) context->ObjectToStringValue(exceptionInfo);   // get char* rendering
    }

    // ------------------------------------------------------------------------------------
    // attach to Java thread
    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *)context->threadContext->instance, false, 0};
    RgfAcquireLock();
    environmentAttachToNew(&param);     // attach to Java

    // - param->error:  -1 ... Attach-error, attachResult holds Java error code, raise an exception in caller
    //                  -2 ... no rajo found, raise an exception in caller
    //                  -3 ... cannot safely use the given RAJO in TID, as actual RAJO is being used in multiple Rexx threads
    //                  -4 ... cannot attach to target TID
    //                 -99 ... no JVM available !
    if (param.error!= 0)    // problem attaching, cannot proceed
    {
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined,
                                 createAttachErrorMessageRSONew
                                 (context, "routine/BsfJavaException(), error 3", &param)
                                 );
        RgfReleaseLock();
        delete[] msg;
        return NULL;
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

            RgfAcquireLock();
            environmentDetachFromNew(&param);
            RgfReleaseLock();
            delete[] msg;
            return NULL;
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
            snprintf( msg, 4096, "%.16s/routine/BsfJavaException(), error 5: supplied argument (Java Throwable object) with the beanName=[%s] cannot be found in the BSFRegistry)", DLLNAME, c_beanNameOrMsg);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            return NULL;
        }
        else if (param.env->IsInstanceOf(jobj, defaultJVM->clz_Throwable)==JNI_FALSE)   // not a Throwable object !
        {
            snprintf( msg, 4096, "%.16s/routine/BsfJavaException(), error 6: supplied argument with the beanName=[%s] is not a sbuclass of 'java.lang.Throwable'", DLLNAME, c_beanNameOrMsg);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            param.env->DeleteLocalRef(jobj);
            result_Obj=NULL;
        }
        else
        {
            param.env->Throw((jthrowable) jobj);     // now raise the exception in Java
            result_Obj=context->True();
        }
    }

    else if (flag==2)                // check for pending Java exception
    {
        result_Obj= (param.env->ExceptionCheck()==1 ? context->True() : context->False() );
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
            CSTRING          c_beanName=param.env->GetStringUTFChars(j_beanName, JNI_FALSE);
            RexxStringObject r_beanName=context->String(c_beanName);    // create Rexx string
            param.env->ReleaseStringUTFChars(j_beanName, c_beanName);         // release

            param.env->DeleteLocalRef(j_beanName);

            if (bsfClz!=NULL)  // class found, do a bsf.wrap() on the beanName
            {
                result_Obj=context->SendMessage1(bsfClz, "BSF.WRAP", r_beanName);  // run bsf.wrap(), may return a BSF_REFERENCE object
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
    environmentDetachFromNew(&param);
    RgfReleaseLock();
    delete[] msg;
    return result_Obj;
}



// ---------------------------------------------------------------------------------------
#if defined (CONFIG_MAKE_OREXX_REGISTRY_ACCESSIBLE)

    // ---------------------------------------------------------------------------------------
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

       return rdo;
    }
#endif




// ---------------------------------------------------------------------------------------
    // ---rgf, 2015-06-04 - 2015-06-07: allow to get, set and drop context variables, usage
    //                    allows to get, set and drop context variables in caller, usage
    //
    //                     BsfContextVariables( [get [, name]] | set, nameOrDir[, value]] | drop, name])
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
    //                                      "name" ... name of variable to drop
    // ---rgf, 2017-02-14: if an operation succeeded (like SET or DROP) will return .true instaed of NULL which indicates no return value!
    //
RexxRoutine3( RexxObjectPtr, BsfContextVariables, OPTIONAL_CSTRING, argSwitch, OPTIONAL_RexxObjectPtr, nameOrDir, OPTIONAL_RexxObjectPtr, value)    // 2015-06-07
{

#if defined ( DEBUG_CONTEXT_VARS )
    fprintf(stderr, "*** BsfContextVariables() 1 ...\n");
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


#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), 2 flag=[%c] ...\n", flag);
    fflush(stderr);
#endif

    if (flag==0)    // no valid flag supplied, raise condition
    {
        char *msg=new char[1024];
        snprintf( msg, 1024, "%.16s/routine/BsfContextVariables(), error 1: first argument ('flag'), illegal argument value '%.256s' (valid: 'G[et]', 'S[et]', 'D[rop]')", DLLNAME, argSwitch);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;

#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), INVALID flag\n");
    fflush(stderr);
#endif

        return NULL;
    }

    logical_t isArg2aString= ( nameOrDir==NULL ? false : context->IsString(nameOrDir) );

    if (argumentExists(3))  // single SET variable
    {
        if ( flag!='S' )    // third argument only exists for SET operation
        {
            char *msg=new char[1024];
            snprintf( msg, 1024, "%.16s/routine/BsfContextVariables(), error 2: third argument only allowed for flag 'S[et]', supplied flag='[%.256s]'", DLLNAME, argSwitch);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), 3 INVALID 3rd argument (not a SET operation)\n");
    fflush(stderr);
#endif

            return NULL;
        }

        if (!isArg2aString) // second argument must be a string for the variable name
        {
           char *msg=new char[1024];
           snprintf( msg, 1024, "%.16s/routine/BsfContextVariables(), error 3: 'S[et]' usage, third argument only allowed, if second argument (variable's name) is a string denoting the variable name", DLLNAME);
           context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
           delete[] msg;
#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), 4 INVALID 2nd argument (must be a string)\n");
    fflush(stderr);
#endif
           return NULL;
        }

        // set single variable
        context->SetContextVariable(context->CString((RexxStringObject) nameOrDir), value);
#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), 5 SET operation succeeded, returning .true\n");
    fflush(stderr);
#endif
        return context->True();
    }

    // only a maximum of two arguments left
    if (argumentOmitted(2))     // only possible for 'G[ETall]'
    {
        if (flag != 'G')
        {
            char *msg=new char[1024];
            snprintf( msg, 1024, "%.16s/routine/BsfContextVariables(), error 4: [%.256s] usage, second argument missing", DLLNAME, argSwitch);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), 6 INVALID omission of 2nd argument (not a GET operation)\n");
    fflush(stderr);
#endif
            return NULL;
        }

#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), 7 GET operation succeeded, returning result of GetAllContextVariables()\n");
    fflush(stderr);
#endif
        return context->GetAllContextVariables();   // return all currently defined variables
    }


    if (flag=='G')           // get single variable name
    {
        if (!isArg2aString)
        {
            char *msg=new char[1024];
            snprintf( msg, 1024, "%.16s/routine/BsfContextVariables(), error 5: 'G[et]' usage, second argument must be a string", DLLNAME);
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), 8 INVALID 2nd argument, must be String (GET operation)\n");
    fflush(stderr);
#endif

            return NULL;
        }
            // fetch variable value
        RexxObjectPtr rop=context->GetContextVariable(context->CString((RexxStringObject) nameOrDir));
        if (rop==NULL)  // variable name not found, create error hinting at VAR()-, VALLUE()-BIFs
        {
            char *msg=new char[1024];
            snprintf( msg, 1024, "%.16s/routine/BsfContextVariables(), error 6: 'G[et]' usage, variable [%.256s] not defined, use VAR()-BIF (built-in-function) to test existence of variable beforehand", DLLNAME, context->ObjectToStringValue(nameOrDir));
            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
            delete[] msg;
#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), 9 INVALID variable not found (GET operation)\n");
    fflush(stderr);
#endif
            return NULL;
        }
#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), 10 GET operation succeeded, returning result of single GetContextVariable()\n");
    fflush(stderr);
#endif
        return rop;

    }

    if (flag=='D' && isArg2aString) // drop variable by name
    {
        context->DropContextVariable(context->CString((RexxStringObject) nameOrDir));   // drop variable
#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), 11 DROP operation carried out, returning .true\n");
    fflush(stderr);
#endif

        return context->True();
    }

        // if not a string, it must be either a .Directory, or starting with ooRexx 5.0 a .StringTable
    if (! (context->IsOfType(nameOrDir, "Directory") || context->IsOfType(nameOrDir, "StringTable")) )
    {
        char *msg=new char[1024];

        snprintf( msg, 1024, "%.16s/routine/BsfContextVariables(), error 7: [%.256s] usage, argument # 2 is not of type 'Directory', neither of type 'StringTable'", DLLNAME, argSwitch);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), 12 INVALID 2nd argument, neither a string nor a Directory/StringTable (SET or DROP operation)\n");
    fflush(stderr);
#endif

        return NULL;
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

#ifdef DEBUG_CONTEXT_VARS
    fprintf(stderr, "*** BsfContextVariables(), 13 SET or DROP with Directory/StringTable succeeded, returning .true\n");
    fflush(stderr);
#endif

    return context->True();
}



// ---------------------------------------------------------------------------------------
    // ---rgf, 2018-02-22: run UNINIT directly against the RexxAndJava class object
RexxRoutine1(size_t, BsfUninit4JavaBean, RexxObjectPtr, bean)
{
    size_t refCount=-1;

// fprintf(stderr, "BsfUninit4JavaBean(), bean=[%p] ", bean); fflush(stderr);

    if (bean==NULL)
    {
        return refCount;
    }

    RexxStringObject rso=context->ObjectToString(bean);

// fprintf(stderr, "| rso=[%s] ", context->CString(rso)); fflush(stderr);


    JNIEnv *jenv=NULL;

        // is this thread already attached ? If so reuse it, but do not detach
    logical_t needToAttach=((defaultJVM->jvm->GetEnv((void **)&jenv, USE_DEFINED_JNI_VERSION))==JNI_EDETACHED);

    if (needToAttach)
    {
        // nope, not yet attached, attach, but detach before leaving this routine
        if ( (defaultJVM->jvm->AttachCurrentThread((void **) &jenv, (void *) &defaultJavaVMAttachArgs)) != JNI_OK) // attach to JVM
        {
            return -99;         // JNI attach error, return prematurely, indicate by returning -99
        }
    }

    if (jenv==NULL)         // attaching did not work!
    {
        return -99;
    }

    jstring jstrBeanName=JNU_CreateJavaString(jenv, context->threadContext, rso);
    refCount=jenv->CallStaticBooleanMethod(defaultJVM->clz_RexxAndJava,
                                 defaultJVM->mid_RexxAndJava_unregisterBean4JNI,
                                 jstrBeanName);

    jenv->DeleteLocalRef(jstrBeanName);

    if (needToAttach)   // we were not attached, hence we did an AttachCurrentThread such that we need to release it
    {
        defaultJVM->jvm->DetachCurrentThread();
    }

// fprintf(stderr, " success, returning true!\n"); fflush(stderr);

    return refCount;        // returns remaining refCount
}



// ---------------------------------------------------------------------------------------
    // ---rgf, 2018-02-24: a test routine to allow measuring various invocation times of a native function from Rexx
RexxRoutine1(RexxObjectPtr, BsfTestPing, OPTIONAL_size_t,  repetitions)
{
    if (argumentOmitted(1) || repetitions==0)
    {
        return NULL;
    }

// fprintf(stderr, "BsfTestPing(), bean=[%p] ", bean); fflush(stderr);

    JNIEnv *jenv=NULL;

        // is this thread already attached ? If so reuse it, but do not detach
    logical_t needToAttach=((defaultJVM->jvm->GetEnv((void **)&jenv, USE_DEFINED_JNI_VERSION))==JNI_EDETACHED);

    if (needToAttach)
    {
        // nope, not yet attached, attach, but detach before leaving this routine
        if ( (defaultJVM->jvm->AttachCurrentThread((void **) &jenv, (void *) &defaultJavaVMAttachArgs)) != JNI_OK) // attach to JVM
        {
            fprintf(stderr, "BSF4ooRexx.BsfTestPing([repetitions=[%lu]]), cannot attach to Java!\n",repetitions);
            return context->String("-99");         // JNI attach error, return prematurely, indicate by returning -99
        }
    }

    if (jenv==NULL)         // attaching did not work!
    {
        fprintf(stderr, "BSF4ooRexx.BsfTestPing([repetitions=[%lu]]), cannot attach to Java!\n",repetitions);
        return context->String("-99");
    }

    for (size_t i=0; i<repetitions; i++)    // now call into Java
    {
        jenv->CallStaticVoidMethod(defaultJVM->clz_RexxAndJava, defaultJVM->mid_RexxAndJava_javaTestPing);
    }

    if (needToAttach)   // we were not attached, hence we did an AttachCurrentThread such that we need to release it
    {
        defaultJVM->jvm->DetachCurrentThread();
    }

// fprintf(stderr, " success!\n"); fflush(stderr);

    return NULL;

}




// ---------------------------------------------------------------------------------------
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
#if defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)

     thread_id_t  tid=RgfGetTID();   // get current TID

     char *title=new char[512];
     snprintf( title, 512, "*** 1 ..._jniSetupNativeJVMPointer(): tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], bsfInvokedBy=[%d], about to setup stuff ...",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, bsfInvokedBy);

     fprintf(stderr, "%s\n", title); fflush(stderr);

     delete [] title;
#endif

    RgfAcquireLock2(JVM_setup_lock);      // lock access for setting up JVM structure

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
    }

    RgfReleaseLock2(JVM_setup_lock);
 }


// ----------------------------------------------------------------------------------------------
/*
 * This may be called from Java, pertains to Object Rexx only (as of 2003-01-04);
 * should wait until all cleanup within Object Rexx has been done. ---rgf, 2002-12-07, 2003-01-04
 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxWaitForTermination
  (JNIEnv *env, jobject obj)
{

#if defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "*** ..._jniRexxWaitForTermination() 1 ... \n");
    fflush(stderr);
#endif

#if defined (INFO_TERMINATE_RII)        // rgf, 2017-08-19
    static int countRII=0;
    fprintf(stderr, "---> arrived in _jniRexxWaitForTermination(),           countRII=[%5d], tid=[%6lu]\n",
                    ++countRII, (unsigned long) RgfGetTID());
    fflush(stderr);
#endif

#ifdef DEBUG        // ---rgf, 2003-04-30
    fprintf(stderr, "..._jniRexxWaitForTermination(), just arrived\n");
#endif

#ifdef DEBUG2        // ---rgf, 2003-04-30
    fprintf(stderr, "    ...just about to call RexxWaitForTermination()...\n");
#endif

    RexxWaitForTermination();       // wait for Object Rexx to re-synch ... (2003-02-25, ---rgf: doesn't really have any effect


#if defined (DEBUG2)   || defined (RGF_JNI)  // ---rgf, 2003-04-30
    fprintf(stderr, "..._jniRexxWaitForTermination(): about to leave...\n\n"); fflush(stderr);
#endif


#if defined (INFO_TERMINATE_RII)        // rgf, 2017-08-19
    fprintf(stderr, "<--- _jniRexxWaitForTermination(),           countRII=[%5d], tid=[%6lu]\n",
                    countRII, (unsigned long) RgfGetTID());
    fflush(stderr);
#endif


    return (jint) 1;               // just return something
}



// ----------------------------------------------------------------------------------------------
/*
 * This is a pass-thru for RexxDidRexxTerminate(), returns 0 if Object Rexx still runs,
 * 1 if Object Rexx has terminated, ---rgf, 2002-12-13
 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxDidRexxTerminate
                       (JNIEnv *env, jobject obj)
{

#if defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "*** ..._jniRexxDidRexxTerminate() 1 ... \n"); fflush(stderr);
#endif

#ifdef DEBUG2        // ---rgf, 2003-04-30
    fprintf(stderr, "..._jniRexxDidRexxTerminate(), just arrived\n");
#endif

#if defined (DEBUG2) || defined (RGF_JNI)      // ---rgf, 2003-04-30
    fprintf(stderr, "    ...about to call and return ..._jniRexxDidRexxTerminate() ...\n\n");
#endif

    return (jint) RexxDidRexxTerminate();       // ask Object Rexx, if it is done
}



// ----------------------------------------------------------------------------------------------
/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniRexxGetTID
 * Signature: ()Ljava/lang/String;
   Purpose:   return BsfGetTID()-value as a string to Java
 */
JNIEXPORT jstring JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxGetTID
  (JNIEnv *env, jobject obj)
{
    thread_id_t tid=RgfGetTID();

#if defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "*** ..._jniRexxGetTID() 1, tid=[%lu] ...\n", (unsigned long) tid);
    fflush(stderr);
#endif

   char *strTid=new char[RGF_TID_STRING_WIDTH];
   snprintf( strTid, RGF_TID_STRING_WIDTH, "%lu%c", ((unsigned long) tid), 0);  // create decimal number
   jstring jstr=env->NewStringUTF( strTid );    // create Java string from the C string and return it
   delete[] strTid;

#if defined (RGF_JNI)
    fprintf(stderr, "*** ..._jniRexxGetTID() 2, tid=[%lu], about to return ...\n", (unsigned long) tid); fflush(stderr);
#endif

   return jstr;
}



// ----------------------------------------------------------------------------------------------
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


#if defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)
    thread_id_t thisTid=RgfGetTID();
    fprintf(stderr, "*** ..._jniRexxSetHalt() 1, thisTid=[%lu], just arrived ...\n", (unsigned long) thisTid); fflush(stderr);
#endif

#if defined (INFO_HALT_RII)        // rgf, 2017-08-19
    static int countRII=0;
    fprintf(stderr, "---> arrived in _jniRexxSetHalt(), countRII=[%5d], tid=[%6lu]\n",
                    ++countRII, (unsigned long) RgfGetTID()); fflush(stderr);
#endif

    unsigned long tid=0L;
    CSTRING tmpStr = env->GetStringUTFChars(jstrTid, JNI_FALSE);
    sscanf(tmpStr, "%lu", &tid);  // get decimal number
    env->ReleaseStringUTFChars(jstrTid, tmpStr);

#if defined (RGF_JNI)
    fprintf(stderr, "*** ..._jniRexxSetHalt() 2, thisTid=[%lu], halted tid=[%lu], about to carry out & return ...\n",
                    (unsigned long) thisTid, (unsigned long) tid);
    fflush(stderr);
#endif

    int intResult = RexxSetHalt(
#ifdef WINDOWS
        GetCurrentProcessId(), (thread_id_t) tid
#else
        getpid(), (pthread_t) tid   // Unix
#endif
                             );

#if defined (INFO_HALT_RII)        // rgf, 2017-08-19
    fprintf(stderr, "<--- _jniRexxSetHalt(),            countRII=[%5d], tid=[%6lu], intResult=%d\n", countRII, (unsigned long) RgfGetTID(), intResult); fflush(stderr);
#endif

    return (jint) intResult;

}



// ----------------------------------------------------------------------------------------------
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
    thread_id_t tid=RgfGetTID();

    RgfAcquireLock2(JVM_setup_lock);      // 2012-12-30, rgf: lock access for setting up JVM structure

#if defined (RGF_JNI)  || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "*** ..._jniInitialize4Rexx() 1, tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p] ... \n",
                       (unsigned long) tid, currentJVM, defaultJVM->jvm);
    fflush(stderr);
#endif

    if (currentJVM==NULL)   // invoked by Java, save JVM-pointer for later use
    {
#if defined (RGF_JNI)  || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "*** ..._jniInitialize4Rexx() 1-1, tid=[%lu], currentJVM==NULL ... \n", (unsigned long) tid);fflush(stderr);
#endif
        env->GetJavaVM( & currentJVM );
        bsfInvokedBy=2;     // Java invoked by Rexx !

        // save and initialize JVM related data
fprintf(stderr, "//// ---> _jniInitialize4Rexx, #1 BEFORE init_STRUCT_JVM ...\n");fflush(stderr);
        init_STRUCT_JVM(env, defaultJVM, currentJVM);    // 20091003, initialize default structure
fprintf(stderr, "//// ---> _jniInitialize4Rexx, #1 AFTER init_STRUCT_JVM, about to return ...\n");fflush(stderr);
    }

    else if (defaultJVM->jvm==NULL)
    {

#if defined (RGF_JNI)  || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "*** ..._jniInitialize4Rexx() 1-1, tid=[%lu], defaultJVM->jvm==NULL ... \n",(unsigned long) tid);fflush(stderr);
#endif
#if defined (RGF_JNI)  || defined (DEBUG_JNI_ENTRY)
   fprintf(stderr, "//// ---> _jniInitialize4Rexx, #2 BEFORE init_STRUCT_JVM ...\n");fflush(stderr);
#endif

        // initialize and save JVM related data
        init_STRUCT_JVM(env, defaultJVM, currentJVM);    // 20091003, initialize default structure

#if defined (RGF_JNI)  || defined (DEBUG_JNI_ENTRY)
   fprintf(stderr, "//// ---> _jniInitialize4Rexx, #2 AFTER init_STRUCT_JVM, about to return ...\n");fflush(stderr);
#endif
    }

    // 20091003, save primodal_rajo as a global reference
    defaultJVM->primodal_rajo=env->NewGlobalRef(raj);

#if defined (RGF_JNI)
    fprintf(stderr, "*** ..._jniInitialize4Rexx() 2, tid=[%lu], rajo=[%p], primodal_rajo=[%p], pRoot_RAJO=[%p], defaultJVM->jvm=[%p] ... \n",
                       (unsigned long) tid,
                        raj,
                        defaultJVM->primodal_rajo,
                        pRoot_RAJO,
                        defaultJVM->jvm
                       );
    fflush(stderr);
#endif

        // 20091005: this is loaded via Rexx, keep this
    JNIEnv *daemEnv;
    RgfAcquireLock();
    int res=defaultJVM->jvm->AttachCurrentThreadAsDaemon((void **) &daemEnv, (void *) &defaultJavaVMAttachArgs);
    RgfReleaseLock();

#if defined (RGF_JNI)
    fprintf(stderr, "*** ..._jniInitialize4Rexx() 3, tid=[%lu], primodal_rajo=[%p], pRoot_RAJO=[%p] ... \n",
                       (unsigned long) tid,
                        defaultJVM->primodal_rajo,
                        pRoot_RAJO
                       );
    fflush(stderr);
#endif


#if defined (DEBUG2)  || defined (RGF_JNI)       // ---rgf, 2003-04-30
    fprintf(stderr, "<-- ..._jniInitialize4Rexx() 3, tid=[%lu], about to leave...\n\n",
            (unsigned long) tid);
    fflush(stderr);
#endif

    RgfReleaseLock2(JVM_setup_lock);
    return (jint) 1;              // just return something which can be thought of being true
}




// ----------------------------------------------------------------------------------------------
    // now called (JNI_OnLoad was misspelled!), 2006-01-04, ---rgf // never called! 2003-07-24
    // Note: this is likely never called (depends on the class loader that loaded this library
    //       to be garbage collected; maybe in Webserver environments, who knows ;-) ...)
JNIEXPORT void JNICALL  JNI_OnUnload(JavaVM *vm, void *reserved)
{

#if defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)
    thread_id_t thisTid=RgfGetTID();
    fprintf(stderr, "*** JNI_OnUnLoad() 1, tid=[%lu] ... <== <=== <==== !!!\n", (unsigned long) thisTid); fflush(stderr);
#endif
}


// ----------------------------------------------------------------------------------------------
JNIEXPORT jint JNICALL  JNI_OnLoad  (JavaVM *vm, void *reserved)
{
         // 2008-07-23: initialize data structure --->
#if defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)
    thread_id_t thisTid=RgfGetTID();
    fprintf(stderr, "*** JNI_OnLoad() 1, tid=[%lu] ...\n", (unsigned long) thisTid);
    fflush(stderr);
#endif

    rgfInitLocks();        // Initialize locks

#if defined ( DEBUG )
   fprintf(stderr, "---> returning a jint: [%x] [%d]\n", USE_DEFINED_JNI_VERSION, USE_DEFINED_JNI_VERSION);
#endif

   return (jint) USE_DEFINED_JNI_VERSION;
}



// ----------------------------------------------------------------------------------------------
// This function may be called from "JNI_OnLoad()" or/and from "bsfLoader()": makes sure that
// that initialization is carried out only once.
void RexxEntry rgfInitLocks()
{
#if defined (DEBUG2)
    fprintf(stderr, "/// in rgfInitLocks(), About to initialize/create mutex locks ...\n");
    fflush(stderr);
#endif

    // initialize critical section semaphore
#ifdef WINDOWS
            // HANDLE WINAPI CreateMutex(
            //  __in          LPSECURITY_ATTRIBUTES lpMutexAttributes,
            //  __in          BOOL bInitialOwner,
            //  __in          LPCTSTR lpName
            // );
    JRST_lock      = CreateMutex(NULL, FALSE, NULL) ;
    RII_lock       = CreateMutex(NULL, FALSE, NULL)  ;
    REGISTRY_lock  = CreateMutex(NULL, FALSE, NULL)  ;
    JVM_setup_lock = CreateMutex(NULL, FALSE, NULL)  ;
#else
    pthread_mutex_init(&JRST_lock, NULL);
    pthread_mutex_init(&RII_lock , NULL);
    pthread_mutex_init(&REGISTRY_lock , NULL);
    pthread_mutex_init(&JVM_setup_lock , NULL);
#endif

#if defined (DEBUG2)
    // fprintf(stderr, " now: bInitialize=[%d]\n", bInitialize);
    fprintf(stderr, "/// in rgfInitLocks(), leaving ...\n");
    fflush(stderr);
#endif

}



// ----------------------------------------------------------------------------------------------
    // destroy/free/close lock resources
void RexxEntry rgfDestroyLocks()
{
#if defined (DEBUG2)
    fprintf(stderr, "/// in rgfDestroyLocks(), About to destroy/delete  mutex locks ...\n");
    fflush(stderr);
#endif

    // destroy (uninitialize) critical section var
#ifdef WINDOWS
    CloseHandle(JRST_lock);
    CloseHandle(RII_lock);
    CloseHandle(REGISTRY_lock);
    CloseHandle(JVM_setup_lock);
#elif defined UNIX
    pthread_mutex_destroy(&JRST_lock);
    pthread_mutex_destroy(&RII_lock);
    pthread_mutex_destroy(&REGISTRY_lock);
    pthread_mutex_destroy(&JVM_setup_lock);
#endif

#ifdef DEBUG1
    fprintf(stderr, "/// in rgfDestroyLocks(), Leaving destroy/delete  mutex locks ...\n");
    fflush(stderr);
#endif
}



// ----------------------------------------------------------------------------------------------
// typedef void (RexxEntry *RexxPackageLoader)(RexxThreadContext *);
// as of ooRexx 4.0: the library gets loaded once and is available globally to all RexxInstances,
//                   hece it runs once per process only
void RexxEntry bsfLoader (RexxThreadContext *rtc)
{

#if defined (DEBUG_BSF_LOADER_UNLOADER )
    thread_id_t tid=RgfGetTID();

    fprintf(stderr, "/// ===> ===> ===> in bsfLoader(), tid=[%lu], c_rii_ID=[%p] ...\n", (unsigned long) tid, rtc->instance);
    fflush(stderr);
#endif

    rgfInitLocks();        // Initialize critical section

#if defined (INFO_PRIMODAL_RII)        // rgf, 2017-10-04
    fprintf(stderr, "===> ===> ===>\n===> bsfLoader(): PRIMODAL REXX INTERPRETER INSTANCE (RII), PRIMODAL RII: tid=[%6lu], rii_ID=[%p], rtc=[%p] <===\n<=== <=== <===\n\n",
                        (unsigned long) RgfGetTID(), rtc->instance, rtc);
    fflush(stderr);
#endif

}




// ----------------------------------------------------------------------------------------------
// typedef void (RexxEntry *RexxPackageUnloader)(RexxThreadContext *);
// as of ooRexx 4.0: the library gets loaded once and is available globally to all RexxInstances,
//                   hece it runs once per process only
void RexxEntry bsfUnloader (RexxThreadContext * rtc)
{
    thread_id_t tid=RgfGetTID();
#if defined (DEBUG_BSF_LOADER_UNLOADER )
    fprintf(stderr, "\\\\\\  <=== <=== <=== in bsfUnloader() tid=[%lu], c_rii_ID=[%p], defaultJVM->jvm=[%p] ...\n", (unsigned long) tid, rtc->instance, defaultJVM->jvm);
    fflush(stderr);
#endif

    if (defaultJVM->jvm == NULL) return;    // 20150924, fix suggested by Jean-Louis Faucher (jlf)

    RgfAcquireLock();

    JNIEnv *jenv=NULL;

        // get JNIEnv
    defaultJVM->jvm->GetEnv((void **) &jenv, USE_DEFINED_JNI_VERSION);  // this will indicate that we are not attached as of yet
    if (jenv==NULL)
    {
        defaultJVM->jvm->AttachCurrentThread((void **) &jenv, (void *) &defaultJavaVMAttachArgs);
    }
    JavaVM *tmpJVM=defaultJVM->jvm;


#if defined (DEBUG_BSF_LOADER_UNLOADER )
    fprintf(stderr, "---> bsfUnloader(): jenv=[%p], tmpJVM=[%p]==defaultJVM->jvm=[%p]\n", jenv, tmpJVM, defaultJVM->jvm);fflush(stderr);
#endif

    // empty all RAJO lists, uninit defaultJVM data
    uninit_STRUCT_JVM(jenv, defaultJVM);   // uninitialize data structures, remove global refs, nullify structure

    if (defaultJVM->jvm!=NULL)
    {
        defaultJVM->jvm->DetachCurrentThread();   // detach this thread from JVM as well

        // don't explicitly destroy JVM, if Rexx threads that are attached to Java
        // got killed, then they can never detach from Java obviously, causing DestroyJavaVM()
        // to wait forever.
/*
        if (bsfInvokedBy==2)     // loaded by Rexx, o.k. to destroy the JVM
        {
            long ret=tmpJVM->DestroyJavaVM ( ); // destroy JVM
        }
*/

    }

    RgfReleaseLock();   // finally release the lock
    rgfDestroyLocks();  // destroy/delete/close lock resources
}



// ----------------------------------------------------------------------------------------------
// External functions if loaded via Rexx
    // Build the list of entry points for the routines:
    RexxRoutineEntry bsf_external_functions[] =
    {
        REXX_TYPED_ROUTINE( BSF                         , BSF                         ),
        REXX_TYPED_ROUTINE( BsfAttachToTID              , BsfAttachToTID              ),
        REXX_TYPED_ROUTINE( BsfCreateRexxProxy          , BsfCreateRexxProxy          ),
        REXX_TYPED_ROUTINE( BsfRexxProxy                , BsfRexxProxy                ),
        REXX_TYPED_ROUTINE( BsfDetach                   , BsfDetach                   ),

        REXX_TYPED_ROUTINE( BsfDoUnregisterRexxObject   , BsfDoUnregisterRexxObject   ),    // rgf, 2016-12-20

        REXX_TYPED_ROUTINE( BsfDropFuncs                , BsfDropFuncs                ),
        REXX_TYPED_ROUTINE( BsfGetRIID                  , BsfGetRIID                  ),
        REXX_TYPED_ROUTINE( BsfGetRTC                   , BsfGetRTC                   ),  // rgf, 2017-10-05: for debugging only
        REXX_TYPED_ROUTINE( BsfGetTID                   , BsfGetTID                   ),
        REXX_TYPED_ROUTINE( BsfInvokedBy                , BsfInvokedBy                ),
 // REXX_TYPED_ROUTINE( BsfCheckCondition           , BsfCheckCondition           ),
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

#ifdef HOT_FIX_CRASH     // allow hot fix do be disabled! (HOT_FIX_CRASH is meant as a temporary fix for the JavaFX GUI-thread crashes only)
        REXX_TYPED_ROUTINE( BsfApplyHotFixCrash         , BsfApplyHotFixCrash         ),    // rgf, 2017-08-21: temporarily allows controlling applying the hot fix
#endif

        REXX_TYPED_ROUTINE( BsfUninit4JavaBean          , BsfUninit4JavaBean          ),    // rgf, 2018-02-23
        REXX_TYPED_ROUTINE( BsfTestPing                 , BsfTestPing                 ),    // rgf, 2018-02-24

        REXX_LAST_ROUTINE()
    };

// ----------------------------------------------------------------------------------------------
    RexxPackageEntry bsf_package_entry =
    {
        STANDARD_PACKAGE_HEADER
        REXX_INTERPRETER_5_0_0,              // anything including and after 5.0.0 will work
        "bsf",                               // name of the package
        "8.0",                               // package version information
        bsfLoader,                           // load function
        bsfUnloader,                         // unload function
        bsf_external_functions,              // the exported functions
        NULL                                 // no exported methods
    };

// ----------------------------------------------------------------------------------------------
    // The package loading stub.  OOREXX_GET_PACKAGE() is a macro that, basically,
    // defines a function whose name is RexxGetPackage().  On Windows, this
    // function, RexxGetPackage(), must be in the exports list of the .def file used
    // when linking this library.
    OOREXX_GET_PACKAGE(bsf);







// rgf, 20090503: new functionality, needing ooRexx 4.0 APIs
// ----------------------------------------------------------------------------------------------
/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniRexxCreateInterpreterInstance
 * Signature: ([Ljava/lang/Object)Ljava/lang/String;
    returns:  the rii_ID (RexxInterpreter instance ID) or NULL, if RexxInterpreter instance could not be created
 */

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
#if defined(RGF_JNI_RII) || defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    thread_id_t thisTid=RgfGetTID();

    fprintf(stderr, "--> arrived: .._jniRexxCreateInterpreterInstance(...), thisTid=[%lu]\n", (unsigned long) thisTid);
    fflush(stderr);
#endif

#if defined (INFO_CREATE_RII)           // rgf, 2017-08-19
    static int countRII=0;
    fprintf(stderr, "\n===> arrived in _jniRexxCreateInterpreterInstance(), countRII=[%8d], tid=[%6lu]\n",
                                ++countRII, (unsigned long) RgfGetTID());
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
    RexxOption *options=(RexxOption *) malloc(sizeOptions);
    memset(options,0,sizeOptions);

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
    CSTRING c_external_call_extensions=NULL;
    if (j_external_call_extensions==NULL)       // o.k. not supplied by Java, set original default
    {
        c_external_call_extensions = ".rxj,.rxo,.rxjo,.jrexx";  //  ".jrexx,.rexxj,.rexxjo";
    }
    else
    {
        // resulting value needs to get freed at the end !
        c_external_call_extensions=JNU_GetStringNativeChars(env, j_external_call_extensions);   // convert to native string
    }
    options[1].option    = c_external_call_extensions;

#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): idx=[1], .optionName=[%s], .option=[%.80s]\n",
                    options[1].optionName, (CSTRING) options[1].option);
#endif

    long idx=2;     // already two options, another option needs to be the third one

        // now process possible Java options

        // INITIAL_ADDRESS_ENVIRONMENT
    CSTRING c_initial_address_environment=NULL;
    if (j_initial_address_environment!=NULL)
    {
        // resulting value needs to get freed at the end !
        c_initial_address_environment=(char *) JNU_GetStringNativeChars(env, j_initial_address_environment);   // convert to native string

        options[idx].optionName=INITIAL_ADDRESS_ENVIRONMENT;
        options[idx].option=c_initial_address_environment;

#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): idx=[%ld], .optionName=[%s], .option=[%.80s]\n",
                    idx, options[idx].optionName, (CSTRING) options[idx].option);
#endif

        idx++;  // increase index
    }

        // EXTERNAL_CALL_PATH
    CSTRING c_external_call_path=NULL;
    if (j_external_call_path!=NULL)
    {
        c_external_call_path=(char *) JNU_GetStringNativeChars(env, j_external_call_path);   // convert to native string

        options[idx].optionName=EXTERNAL_CALL_PATH;
        options[idx].option=c_external_call_path;
#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): idx=[%ld], .optionName=[%s], external call path=[%s]\n",
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

#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): required library, idx=[%ld], .optionName=[%s] | i=[%d]: .option=[%s]\n",
                    idx, options[idx].optionName, i, (CSTRING) options[idx].option);
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
        exits=(RexxContextExit *) malloc(tmpSize);
        memset(exits,0,tmpSize);

        jint * tmpArrExits=env->GetIntArrayElements(j_direct_exits,NULL);
        for (int i=0; i<nr_direct_exits; i++)
        {
            exits[i].handler=(RexxContextExitHandler *) rexx_exit_handler_entry;   // point to generic exit handler
            exits[i].sysexit_code=tmpArrExits[i];
#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): exit # [%d], sysexit_code=[%d]\n",
                    i, exits[i].sysexit_code);
#endif
        }
        env->ReleaseIntArrayElements(j_direct_exits, tmpArrExits, JNI_ABORT);  // do not copy elements back to Java

        exits[nr_direct_exits].sysexit_code=0;     // indicate list has ended

        options[idx].optionName=DIRECT_EXITS;
        options[idx].option=(void *)exits;
#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): idx=[%ld], .optionName=[%s]\n",
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
        environments=(RexxContextEnvironment *) malloc(tmpSize);
        memset(environments,0,tmpSize);

        for (int i=0; i<nr_direct_environments; i++)
        {
            environments[i].handler=rexx_command_handler_entry;   // point to generic exit handler
            jstring jstr=(jstring) env->GetObjectArrayElement(j_direct_environments,i);
            environments[i].name=JNU_GetStringNativeChars(env, jstr);
            env->DeleteLocalRef(jstr);

#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): environment # [%d], name=[%s]\n",
                    i, environments[i].name);
#endif
        }
        environments[nr_direct_environments].name=NULL;     // indicate list has ended

        options[idx].optionName=DIRECT_ENVIRONMENTS;
        options[idx].option=(void *)environments;
#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "---> _jniRexxCreateInterpreterInstance(): idx=[%ld], .optionName=[%s]\n",
                    idx, options[idx].optionName);
#endif
        idx++;  // increase options index
    }

        // indicate end of options
    options[idx].optionName=NULL;


#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "\n===> _jniRexxCreateInterpreterInstance(): iterating over all options, display option names, idx=[%ld], optionCount=[%d]:\n", idx, countOptions);

    {
        for (int i=0; i<countOptions;i++)
        {
            fprintf(stderr, "\toption[%d].optionName=[%s]\n", i, options[i].optionName);
        }
    }
#endif


#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "\n===> _jniRexxCreateInterpreterInstance(): before creating RII, iniEnv=[%s], callPath=[%s], callExt=[%s], #opts=[%d], #req_lib=[%lu], #exits=[%lu], #env=[%lu]\n",
            c_initial_address_environment, c_external_call_path, c_external_call_extensions, countOptions,
            nr_load_required_library, nr_direct_exits, nr_direct_environments);
#endif



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
#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
        fprintf(stderr, "===---=== .._jniRexxCreateInterpreterInstance(...), thisTid=[%lu], CREATED: c_rii_ID={%p]\n", (unsigned long) thisTid, instance);
        fflush(stderr);
#endif

#if defined (INFO_CREATE_RII)           // rgf, 2017-08-19
    fprintf(stderr, "     SUCCESS in _jniRexxCreateInterpreterInstance(), countRII=[%8d], tid=[%6lu], rii_ID=[%p], rtc=[%p]\n",
                    countRII, (unsigned long) RgfGetTID(), instance, threadContext);
    fflush(stderr);
#endif


        // rgf, 20140330: add TID to .local to save the primodal TID for this Rexx interpreter instance
        {
            thread_id_t tid=RgfGetTID();
            char *strTid=new char[RGF_TID_STRING_WIDTH];
            snprintf( strTid, RGF_TID_STRING_WIDTH, "%lu%c", ((unsigned long) tid), 0);  // create decimal number

#ifdef DEBUG_JNI
    fprintf(stderr, "*** in _jniRexxCreateInterpreterInstance(), tid=[%lu], %%s=[%s] ...\n", (unsigned long) tid, strTid);
    fflush(stderr);
#endif

            RexxDirectoryObject globalDir = threadContext->GetGlobalEnvironment();   // get .local
            threadContext->DirectoryPut(globalDir, threadContext->String(strTid), PRIMODAL_TID);
            threadContext->ReleaseLocalReference(globalDir);

            delete[] strTid;
        }
        // ----------------------------------------------------------


            // rgf, 2012-02-07, save RexxAndJava (rajo) and RexxConfiguration (rexxconf) object with RII-structure
        RgfAddRexxInterpreterInstanceToList(threadContext,
                                            env->NewGlobalRef(jobj),                                    // rajo
                                            env->NewGlobalRef(env->GetObjectArrayElement(joptions,0))   // rexxconfig
                                            );

        RgfPointer2String(instance, c_rii_ID);  // create a string

#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
        fprintf(stderr, "===---=== .._jniRexxCreateInterpreterInstance(...), thisTid=[%lu], CREATED: c_rii_ID=[%p]=[%s]\n", (unsigned long) thisTid, instance, c_rii_ID);
        fflush(stderr);
#endif

        j_rii_ID=env->NewStringUTF( c_rii_ID ); // create Java string
    }

#if defined (INFO_CREATE_RII)           // rgf, 2017-08-19
    else
    {
        fprintf(stderr, "     FAILURE in _jniRexxCreateInterpreterInstance(), countRII=[%8d], tid=[%6lu]\n",
                                    countRII, (unsigned long) RgfGetTID());
        fflush(stderr);
    }
#endif


        // free memory allocated in this routine
    if ( j_initial_address_environment!=NULL )
    {
       free((void *)c_initial_address_environment);
       env->DeleteLocalRef(j_initial_address_environment);
    }

    if ( j_external_call_extensions   !=NULL )
    {
        free((void *)c_external_call_extensions);
        env->DeleteLocalRef(j_external_call_extensions);
    }

    if ( j_external_call_path         !=NULL )
    {
        free((void *)c_external_call_path);
        env->DeleteLocalRef(j_external_call_path);
    }

    if ( exits!=NULL ) {free(exits);}

    if ( environments!=NULL)
    {
        for (int i=0;i<nr_direct_environments;i++) {
            free((void *)environments[i].name);
        }
        free(environments);
    }

        // delete required library names, if any
    for (int i=2; i<(countOptions-1);i++) {
        if ( strcmp(options[i].option, LOAD_REQUIRED_LIBRARY)==0 )
        {
            free((void *)options[i].optionName);
        }
    }

    free(options);

#if defined(DEBUG_JNI) || defined (RGF_JNI) ||  defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY) || defined (RGF_COMMAND_HANDLER)
    fprintf(stderr, "<-- returning from: .._jniRexxCreateInterpreterInstance(...), thisTid=[%lu], c_rii_ID=[%.256s]\n",
                        (unsigned long) thisTid, c_rii_ID);
    fflush(stderr);
#endif

#if defined (INFO_CREATE_RII)           // rgf, 2017-08-19
    fprintf(stderr, "<=============  _jniRexxCreateInterpreterInstance(), countRII=[%8d], tid=[%6lu], rii_ID=[%p], rtc=[%p]\n\n",
                                countRII, (unsigned long) RgfGetTID(), instance, threadContext);
    fflush(stderr);
#endif


    if (j_load_required_library!=NULL) {env->DeleteLocalRef(j_load_required_library);}
    if (j_load_required_library!=NULL) {env->DeleteLocalRef(j_direct_exits);         }
    if (j_load_required_library!=NULL) {env->DeleteLocalRef(j_direct_environments);  }

    if (!bSuccess)      // if Rexx interpreter instance could not be created, throw a Java exception
    {
        char *msg=new char[1024];
        snprintf( msg, 1024, "%.16s/routine/jniCreateInterpreterInstance(), error 1.001: could not create RexxInterpreter instance", DLLNAME);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
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



// ----------------------------------------------------------------------------------------------
/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniGetRexxInterpreterInstanceRoot
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetRexxInterpreterInstanceRoot
  (JNIEnv *env, jobject raj)
{
#if defined(DEBUG_JNI) || defined (RGF_JNI) || defined (DEBUG_JNI_ENTRY)
    thread_id_t thisTid=RgfGetTID();
    fprintf(stderr, "--> arrived: .._jniGetRexxInterpreterInstanceRoot(...), thisTid=[%lu]\n", (unsigned long) thisTid);
    fflush(stderr);
#endif

    if (pRoot_RII==NULL)        // no Rexx interpreter instance as of yet!
    {
        return NULL;
    }

    char *c_rii_ID=new char[RGF_POINTER_STRING_WIDTH];
    RgfPointer2String(pRoot_RII->instance, c_rii_ID);     // get Root node's charater value
    jstring j_rii_ID=env->NewStringUTF( c_rii_ID ); // create Java string

#if defined(DEBUG_JNI) || defined (RGF_JNI)
    fprintf(stderr, "<-- returning from: .._jniGetRexxInterpreterInstanceRoot(...), thisTid=[%lu], c_rii_ID=[%.256s]\n",
                        (unsigned long) thisTid, c_rii_ID);
    fflush(stderr);
#endif

    delete [] c_rii_ID;
    return j_rii_ID;    // return jstring of rii_ID
}



// ----------------------------------------------------------------------------------------------
/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniRexxRunProgram
 * Signature: (Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;

    // invocationType:
    //   1: execute Rexx program from file, use the 4.0 API <code>CallProgram</code>
    //   2: execute Rexx program from buffer, uses the 4.0 API <code>NewRoutine</code> and <code>CallRoutine</code>
    //   3: load Rexx package (and run prolog) from file, use the 4.0 API <code>LoadPackage</code>
    //   4: load Rexx package (and run prolog) from buffer, use the 4.0 API <code>LoadPackageFromData</code>
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxRunProgram

  (JNIEnv *env, jobject raj, jstring j_rii_ID,
                             jint invocationType,   // 1=CallProgram, 2=CallProgramFromData, 3=LoadPackage, 4=LoadPackageFromData
                             jstring j_fileName,
                             jstring j_programData, // if given, use this as code and "fileName" as its name
                             jobjectArray j_args)
{
    // rgf, 2009-05-24, begin
    thread_id_t tid=RgfGetTID();   // get current TID

#if defined(DEBUG_JNI) || defined (DEBUG_JNI_ENTRY) || defined (DEBUG_RII_ID)
    fprintf(stderr, "--> arrived: .._jniRexxRunProgram(...), tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], defaultJVM->clz_Object=[%p]\n",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, defaultJVM->clz_Object);
    if (j_rii_ID==NULL)     // rgf, 20150813
    {
        fprintf(stderr, "             .._jniRexxRunProgram(...), PANIC, PANIC, tid=[%lu], j_rii_ID==NULL [%p]!!!\n",
                                        (unsigned long) tid,
                                        j_rii_ID);
    }
    fflush(stderr);
#endif

        // get interpreter instance from argument
    char         *c_rii_ID=NULL;
    RexxInstance *instance=NULL;

    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string
#if defined(DEBUG_JNI)
    fprintf(stderr, "                                        using c_rii_ID=[%.256s]\n", c_rii_ID);fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string

#if defined (DEBUG_RII_ID)
    fprintf(stderr, "             .._jniRexxRunProgram(...), tid=[%lu], j_rii_ID=[%p], c_rii_ID=[%p], instance=[%p]\n",
                                    (unsigned long) tid, j_rii_ID, c_rii_ID, instance);
    fflush(stderr);
#endif
    free(c_rii_ID);

#if defined(DEBUG_JNI)
{
    char tmpBuf[1024];

    JAVA_TO_STRING( env, raj, tmpBuf, 1024);
    fprintf(stderr, "*** *** DEBUG_JNI: ..._jniRexxRunProgram() 1a - tid=[%lu],          raj=[%p] [%s]\n",
                                     (unsigned long) tid,
                                     raj,
                                     tmpBuf
                                     );
    fflush(stderr);
}
#endif

    RexxThreadContext   *rtc=NULL;
    RexxObjectPtr result_obj;           // Rexx object returned from Rexx, NULL[OBJECT], if none

    logical_t argOK= ((invocationType==1 || invocationType==3) && j_fileName!=NULL && j_programData==NULL) ||
                     ((invocationType==2 || invocationType==4) && j_fileName!=NULL && j_programData!=NULL);

        // check arguments
    if (!argOK)    // error in the supplied arguments
    {
        char *msg=new char[1024];

        if (invocationType<1 || invocationType>4)  // wrong invocationType
        {
            snprintf( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 2: argument 'invocationType' can only be one of '1', '2', '3' or '4', found: [%d]", DLLNAME, (int) invocationType);
        }
        else if (j_fileName==NULL)
        {
            snprintf( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 3: argument 'fileName' must not be 'null'", DLLNAME);
        }
        else if ((invocationType==2 || invocationType==4) && j_programData==NULL)
        {
            snprintf( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 4: argument 'invocationType' is '%d', therefore 'programData' must not be 'null'", DLLNAME, (int) invocationType);
        }
        else if ((invocationType==1 || invocationType==3) && j_programData!=NULL)
        {
            snprintf( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 5: argument 'invocationType' is '%d', therefore 'programData' must be 'null'", DLLNAME, (int) invocationType);
        }
        else
        {
            snprintf( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 6: invocation type arguments in error", DLLNAME);
        }

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: .._jniRexxRunProgram(...): tid=[%lu], error in arguments, problematic invocationType=[%d]\n",
                                (unsigned long) tid, (int) invocationType);
    fflush(stderr);
#endif

        return NULL;
    }

    RexxInstance *tmpInstance=NULL;
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

#ifdef HOT_FIX_CRASH
    if (bsfApplyHotFixCrash==1)
    {
        hotfix_relinquish_thread();    // relinquish thread for at least 10ms (1/100th of a second)

/*
            // running the rgf_multithreaded test units may still exhibit a crash, so relinquishing another time
        hotfix_relinquish_thread();    // relinquish thread again for at least 10ms (1/100th of a second)

            // running the rgf_multithreaded test units may still exhibit a crash, so relinquishing another time
        hotfix_relinquish_thread();    // relinquish thread again for at least 10ms (1/100th of a second)

            // running the rgf_multithreaded test units may still exhibit a crash, so relinquishing another time
        hotfix_relinquish_thread();    // relinquish thread again for at least 10ms (1/100th of a second)

            // running the rgf_multithreaded test units may still exhibit a crash, so relinquishing another time
        hotfix_relinquish_thread();    // relinquish thread again for at least 10ms (1/100th of a second)
*/
    }
#endif

    // invocationType:
    //   1: execute Rexx program from file, use the 4.0 API <code>CallProgram</code>
    //   2: execute Rexx program from buffer, uses the 4.0 API <code>NewRoutine</code> and <code>CallRoutine</code>
    //   3: load Rexx package (and run prolog) from file, use the 4.0 API <code>LoadPackage</code>
    //   4: load Rexx package (and run prolog) from buffer, use the 4.0 API <code>LoadPackageFromData</code>
#if defined (INFO_REXX_RUN_PROGRAM)        // rgf, 2017-08-19
    static int count=0;
    count++;

    switch (invocationType)
    {
    case 1:
        fprintf(stderr, "|--> arrived in _jniRexxRunProgram() -----------------> count=[%8d], tid=[%6lu] / type=[%d]: %s\n",
                        count, (unsigned long) RgfGetTID(), invocationType, "CallProgram()");
        break;
    case 2:
        fprintf(stderr, "|--> arrived in _jniRexxRunProgram() -----------------> count=[%8d], tid=[%6lu] / type=[%d]: %s\n",
                        count, (unsigned long) RgfGetTID(), invocationType, "NewRoutine() + CallRoutine()");
        break;
    case 3:
        fprintf(stderr, "|--> arrived in _jniRexxRunProgram() -----------------> count=[%8d], tid=[%6lu] / type=[%d]: %s\n",
                        count, (unsigned long) RgfGetTID(), invocationType, "LoadPackage()");
        break;
    case 4:
        fprintf(stderr, "|--> arrived in _jniRexxRunProgram() -----------------> count=[%8d], tid=[%6lu] / type=[%d]: %s\n",
                        count, (unsigned long) RgfGetTID(), invocationType, "LoadPackageFromData()");
        break;
    default:
        fprintf(stderr, "|--> arrived in _jniRexxRunProgram() -----------------> count=[%8d], tid=[%6lu] / type=[%d]: %s\n",
                        count, (unsigned long) RgfGetTID(), invocationType, "UNKNOWN - PANIC !");
    }
    fflush(stderr);
#endif


#if defined (DEBUG_RII_ID)
    fprintf(stderr, "             .._jniRexxRunProgram(...), tid=[%lu], instance=[%p], tmpInstance=[%p] (after RgfGetRexxInterpreterInstanceFromList(instance)\n",
                                    (unsigned long) tid, instance, tmpInstance);
    fflush(stderr);
#endif

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
        char *msg=new char[1024];
        snprintf( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 7: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: .._jniRexxRunProgram(...): tid=[%lu], error, cannot retrieve c_rii_ID=p=[%p], received=tmpInstance=[%p] instead\n",
                    (unsigned long) tid, instance, tmpInstance);
    fflush(stderr);
#endif

        return NULL;
    }

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "--------------> ooRexx->AttachThread() - jniRexxRunProgram tid=[%lu] ... ", (unsigned long) tid); fflush(stderr);
#endif

#if defined (INFO_ATTACH_THREAD)        // rgf, 2017-10-04
    fprintf(stderr, "            ... _jniRexxRunProgram()                    count=[%8d], tid=[%6lu], rii_ID=[%p]->AttachThread(): about...\n",
                    count, (unsigned long) RgfGetTID(), instance );
    fflush(stderr);
#endif



    // rgf, 2013-10-07: no need to do a rtc->ReleaseLocalReference(ro) as these references get released upon rtc->DetachThread() !
    if (!instance->AttachThread(&rtc))          // get a ThreadContext
    {

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "AttachThread() UNSUCCESSFUL!  tid=[%lu]  :-( \n", (unsigned long) tid); fflush(stderr);
#endif
        char *msg=new char[1024];
        snprintf( msg, 1024, "%.16s/routine/jniRexxRunProgram(), error 8: could not AttachThread() to Rexx interpreter instance with the ID '%p'", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: .._jniRexxRunProgram(...): tid=[%lu], error, cannot do an instance->AttachThread()!\n",
                          (unsigned long) tid);
    fflush(stderr);
#endif

        return NULL;
    }

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "AttachThread() done. --------------> tid=[%lu] \n", (unsigned long) tid); fflush(stderr);
#endif

#if defined (INFO_ATTACH_THREAD)        // rgf, 2017-08-19
    fprintf(stderr, "            ... _jniRexxRunProgram(),                   count=[%8d], tid=[%6lu], rii_ID=[%p]->AttachThread(): done, rtc=[%p].\n",
                    count, (unsigned long) RgfGetTID(), instance, rtc );
    fflush(stderr);
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

    switch (invocationType)
    {
        // -----------------------------------------------------
    case 1:     // CallProgram()

#if defined (INFO_REXX_RUN_PROGRAM)        // rgf, 2017-08-19
    fprintf(stderr, "            ... _jniRexxRunProgram()-CallProgram(...),  count=[%8d]: tid=[%6lu], rii_ID=[%p], rtc=[%p]: %s(\"%s\")\n",
                           count, (unsigned long) RgfGetTID(), instance, rtc,
                    "CallProgram", c_fileName); fflush(stderr);
#endif
        result_obj=rtc->CallProgram(c_fileName, ra);    // run the program from file
        break;

        // -----------------------------------------------------
    case 3:     // LoadPackage()

#if defined (INFO_REXX_RUN_PROGRAM)
    fprintf(stderr, "            ... _jniRexxRunProgram()-LoadPackage(...),  count=[%8d]: tid=[%6lu], rii_ID=[%p], rtc=[%p]: %s(\"%s\")\n",
                           count, (unsigned long) RgfGetTID(), instance, rtc,
                    "LoadPackage", c_fileName); fflush(stderr);
#endif
        result_obj=rtc->LoadPackage(c_fileName);        // load & run the program
        break;

        // -----------------------------------------------------
    case 2:
    case 4:     // LoadPackageFromData()break;
                // get programData, if any
            size_t len=0;

            if (j_programData!=NULL)
            {
                c_programData=(char *) JNU_GetStringNativeChars(env, j_programData);   // convert to native string
                len=strlen(c_programData);          // get length
            }

            // -----------------------------------------------------
            if (invocationType==2)
            {
#if defined (INFO_REXX_RUN_PROGRAM)
    fprintf(stderr, "            ... _jniRexxRunProgram()-NewRoutine(...),   count=[%8d]: tid=[%6lu], rii_ID=[%p], rtc=[%p]: %s(\"%s\", \"%.25s\"...)\n",
                           count, (unsigned long) RgfGetTID(), instance, rtc,
                    "NewRoutine", c_fileName, c_programData); fflush(stderr);
#endif

                RexxRoutineObject rro=rtc->NewRoutine(c_fileName, c_programData, len);

                if (rro==NULL)          // something went wrong, condition will be set
                {
                    break;
                }

#if defined (INFO_REXX_RUN_PROGRAM)
    fprintf(stderr, "            ... _jniRexxRunProgram()-CallRoutine(...),  count=[%8d]: tid=[%6lu], rii_ID=[%p], rtc=[%p]: %s(...)\n",
                           count, (unsigned long) RgfGetTID(), instance, rtc,
                    "CallRoutine"); fflush(stderr);
#endif
                result_obj=rtc->CallRoutine(rro, ra);   // call the program
            }
            // -----------------------------------------------------
            else   // invocationType==4
            {
#if defined (INFO_REXX_RUN_PROGRAM)
    fprintf(stderr, "    ... _jniRexxRunProgram()-LoadPackageFromData(...),  count=[%8d]: tid=[%6lu], rii_ID=[%p], rtc=[%p]: %s(\"%s\", \"%.25s\"...)\n",
                           count, (unsigned long) RgfGetTID(), instance, rtc,
                    "LoadPackageFromData", c_fileName, c_programData); fflush(stderr);

#endif
                result_obj=rtc->LoadPackageFromData(c_fileName, c_programData, len);    // load & run the program
            }
    }

    free(c_fileName);     // make sure we free the allocated memory
    free(c_programData);  // make sure we free the allocated memory

#if defined (INFO_REXX_RUN_PROGRAM)
    fprintf(stderr, "            ... _jniRexxRunProgram(): CheckCondition(), count=[%8d]: tid=[%6lu], rii_ID=[%p], rtc=[%p]->CheckCondition() -> [%d] ...)\n",
                           count, (unsigned long) RgfGetTID(), instance,
                           rtc, (int) rtc->CheckCondition()
            );
                    fflush(stderr);

    fflush(stderr);
#endif

    // check for exception; if so, create RexxException, supply condition object as a RexxProxy; clear exception in thread
    if (rtc->CheckCondition())      // a Rexx condition raised?
    {
        RexxDirectoryObject condObj=rtc->GetConditionInfo();
        char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "/routine/jniRexxRunProgram(), error 9");

#if defined (INFO_REXX_RUN_PROGRAM)
    fprintf(stderr, "            ... _jniRexxRunProgram(...): BEFORE RgfCreateRexxException4Java(...) count=[%8d], tid=[%lu]\n", count, (unsigned long) tid);
    fflush(stderr);
#endif
        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             rtc, // rtc,
                                                             condObj, // conditionObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "<-------------- ooRexx->DetachThread() - jniRexxRunProgram tid=[%lu]... ", (unsigned long) tid); fflush(stderr);
#endif

#if defined (INFO_DETACH_THREAD)
    fprintf(stderr, "            ... _jniRexxRunProgram(), RexxCondition! count=[%8d]:  tid=[%6lu], rii_ID=[%p], rtc=[%p]->DetachThread(), about...\n",
                           count, (unsigned long) RgfGetTID(), instance, rtc); fflush(stderr);
#endif

        rtc->DetachThread();

#if defined (INFO_DETACH_THREAD)
    fprintf(stderr, "|<---           _jniRexxRunProgram(), RexxCondition!  count=[%8d]:  tid=[%6lu], rii_ID=[%p], rtc=[%p]->DetachThread(), done.\n\n",
                           count, (unsigned long) RgfGetTID(), instance, rtc); fflush(stderr);
#endif


#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "DetachThread() done. <-------------- tid=[%lu] \n", (unsigned long) tid); fflush(stderr);
#endif

        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: .._jniRexxRunProgram(...): tid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",
                       (unsigned long) tid, msg);
    fflush(stderr);
#endif

        free(msg);
        return NULL;
    }

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning from: .._jniRexxRunProgram(...), tid=[%lu], normal execution\n", (unsigned long) tid);
    fflush(stderr);
#endif

#if defined (INFO_REXX_RUN_PROGRAM)
    fprintf(stderr, "      returning _jniRexxRunProgram(): ................. count=[%8d]: tid=[%6lu], rii_ID=[%p], rtc=[%p]] | result_obj=[%p]\n",
                           count, (unsigned long) RgfGetTID(), instance, rtc, result_obj); fflush(stderr);
#endif

    jobject jresult=RgfProcessReturnValue4Java(env, raj, rtc, result_obj, NULL);

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "<-------------- ooRexx->DetachThread() - jniRexxRunProgram tid=[%lu] ... ", (unsigned long) tid); fflush(stderr);
#endif

#if defined (INFO_DETACH_THREAD)
    fprintf(stderr, "            ... _jniRexxRunProgram(),                   count=[%8d]: tid=[%6lu], rii_ID=[%p], rtc=[%p]->DetachThread(), about...\n",
                           count, (unsigned long) RgfGetTID(), instance, rtc); fflush(stderr);
#endif

    rtc->DetachThread();

#if defined (INFO_DETACH_THREAD)
    fprintf(stderr, "|<------------- _jniRexxRunProgram(),                   count=[%8d]: tid=[%6lu], rii_ID=[%p], rtc=[%p]->DetachThread(), done.\n\n",
                           count, (unsigned long) RgfGetTID(), instance, rtc); fflush(stderr);
#endif

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "DetachThread() done. <-------------- tid=[%lu] \n", (unsigned long) tid); fflush(stderr);
#endif

    return jresult;
}




// ----------------------------------------------------------------------------------------------
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

 // 2015-08-11, rgf: javaObjectBean and javaMethodObjectBean will get deregistered in RexxAndJava.call(), hence
//                   increase refcounter in bsf.wrap, as the uninit-method of the BSF Rexx proxy will decrease it
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxSendMessageToRexxObject
  (JNIEnv *env, jobject jobj,
                  jstring j_rii_ID,
                  jstring j_obj_ID,
                  jstring j_userData_ID,        // 4.0, 20090517
                  jstring j_javaObjectBean,     // since 2009-07-11
                  jstring j_methodObjectBean,   // since 2009-06-27
                  jstring j_messageName,        // the Java (mixed) name or null (= not create and append a slot arg!)
                  jstring j_methodDescription,  // since 2009-07-10
                  jobjectArray j_args,
                  jstring j_returnType,         // since 2009-08-27
                  jstring j_scope               // since 2022-08-11
                  )
{

#if defined(DEBUG_JNI) || defined (RGF_JNI) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT) || defined (DEBUG_JNI_ENTRY) || defined (DEBUG_RII_ID)
    thread_id_t thisTid=RgfGetTID();

    char strRajo[256];
    JAVA_TO_STRING(env, jobj, strRajo, 256);

    fprintf(stderr, "--> arrived: .._jniRexxSendMessageToRexxObject(...), thisTid=[%lu], rajo=[%s]\n\tj_rii_ID=[%p], j_obj_ID=[%p], j_userData_ID=[%p], j_methodObjectBean=[%p], j_messageName=[%p], j_methodDescription=[%p], j_args=[%p], j_returnType=[%p], j_scope=[%p]\n",
                          (unsigned long) thisTid,
                          strRajo,
                          j_rii_ID,
                          j_obj_ID,
                          j_userData_ID,
                          j_methodObjectBean,
                          j_messageName,
                          j_methodDescription,
                          j_args,
                          j_returnType,
                          j_scope
            );
    fflush(stderr);

    {   // make arguments better legible for debugging, rgf, 2020-06-28
        char strUserData_ID[256]="<null>";
// fprintf(stderr, "\n   ---> (__LINE__=%d) <---\n", __LINE__);fflush(stderr);
        if (j_userData_ID!=NULL) {
            JAVA_TO_STRING(env, j_userData_ID, strUserData_ID, 256);
        }

        char strJavaObjectBean[256]="<null>";
        if (j_javaObjectBean!=NULL) {
            JAVA_TO_STRING(env, j_javaObjectBean, strJavaObjectBean, 256);
        }

        char strMethodObjectBean[256]="<null>";
        if (j_methodObjectBean!=NULL) {
            JAVA_TO_STRING(env, j_methodObjectBean, strMethodObjectBean, 256);
        }

        char strMessageName[1024]="<null>";
        if (j_messageName!=NULL) {
            JAVA_TO_STRING(env, j_messageName, strMessageName, 1024);
        }

        char strMethodDescription[1024]="<null>";
        if (j_methodDescription!=NULL) {
            JAVA_TO_STRING(env, j_methodDescription, strMethodDescription, 1024);
        }

        int arrayLength=-1;
        if (j_args != NULL) {
            arrayLength=env->GetArrayLength( j_args );
        }

        char strScope[256]="<null>";
        if (j_scope!=NULL) {
            JAVA_TO_STRING(env, j_scope, strScope, 256);
        }

        fprintf(stderr, "\n// -->\t\t\\\\ j_messageName=[%s] //, j_args.length=[%d]\n\t\t// -->j_javaObjectBean=[%s], j_methodObjectBean=[%s], j_methodDescription=[%s], j_userData_ID=[%s], j_scope=[%s]\n",
                              (char *) strMessageName,
                                       arrayLength,
                              (char *) strJavaObjectBean,
                              (char *) strMethodObjectBean,
                              (char *) strMethodDescription,
                              (char *) strUserData_ID,
                              (char *) strScope
                );
        fflush(stderr);
    }
#endif

    // 2022-08-11: make sure RexxProxy and message to it is supplied
    if (j_obj_ID==NULL || j_messageName==NULL)
    {
        char *msg=new char[1024];
        if (j_obj_ID==NULL)
        {
            snprintf( msg, 1024, "%.16s/routine/jniRexxSendMessageToRexxObject(), error 1a: argument RexxProxy is NULL (not supplied from Java)", DLLNAME);
        }
        else
        {
            snprintf( msg, 1024, "%.16s/routine/jniRexxSendMessageToRexxObject(), error 1b: argument messageName is NULL (not supplied from Java)", DLLNAME);
        }

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                              NULL, // rtc,
                                                              NULL, // conditionObject
                                                              REASON_INVALID_ARGUMENT,   // reason
                                                              msg   // message
                                                         );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

        return NULL;
    }


    thread_id_t          tid=RgfGetTID();   // rgf, 2009-05-24, get current TID
    RexxThreadContext   *rtc=NULL;
    RexxObjectPtr result_obj;           // Rexx object returned from Rexx, NULL[OBJECT], if none

        // get interpreter instance
    char *c_rii_ID=NULL;
    RexxInstance *instance=NULL, *tmpInstance=NULL;

    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string
    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string

#if defined(DEBUG_RII_ID)
    fprintf(stderr, "-->          .._jniRexxSendMessageToRexxObject(...), thisTid=[%lu], j_rii_ID=[%p], c_rii_ID=[%s/%p], instance=[%p]\n",
                                       (unsigned long) thisTid,
                                       j_rii_ID,
                                       c_rii_ID,
                                       c_rii_ID,
                                       instance);
    fflush(stderr);
#endif

    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

#ifdef HOT_FIX_CRASH
    if (bsfApplyHotFixCrash==1)
    {
        hotfix_relinquish_thread();    // relinquish thread for at least 10ms (1/100th of a second)
    }
#endif

#if defined (INFO_SEND_MESSAGE_TO_REXX_OBJECT)        // rgf, 2017-08-19
    static int count=0;
    {
        // 2017-11-13, rgf: show Rexx-related values here in case AttachThread() blocks, such that we can see the message name beforehand

        char *c_rgf_msg   =(char *) JNU_GetStringNativeChars(env, j_messageName);   // convert to native string
        char *c_rgf_obj_ID=(char *) JNU_GetStringNativeChars(env, j_obj_ID);        // convert to native string

        if (instance!=tmpInstance)
        {
            fprintf(stderr, "\n-------------->\n---> arrived in _jniRexxSendMessageToRexxObject(),      count=[%8d], tid=[%6lu], rii_ID=[%p] <> rii_ID=[%p] (NOT OK!) | rii_id=[%s]~%s\n",
                            ++count, (unsigned long) RgfGetTID(), instance, tmpInstance, c_rgf_obj_ID, c_rgf_msg);
        }
        else
        {
            fprintf(stderr, "\n-------------->\n---> arrived in _jniRexxSendMessageToRexxObject(),      count=[%8d], tid=[%6lu], rii_ID=[%p] | rii_id=[%s]~%s\n",
                            ++count, (unsigned long) RgfGetTID(), instance, c_rgf_obj_ID, c_rgf_msg );
        }
        fflush(stderr);

        free(c_rgf_msg);          // make sure we free the allocated memory
        free(c_rgf_obj_ID);       // make sure we free the allocated memory
    }

#endif


#if defined(DEBUG_RII_ID)
    fprintf(stderr, "-->          .._jniRexxSendMessageToRexxObject(...), thisTid=[%lu], instance=[%p], tmpInstance=[%p]\n",
                                    (unsigned long) thisTid,
                                    instance,
                                    tmpInstance);
    fflush(stderr);
#endif
    // ooRexx can process Rexx objects with any RII, however .local is different for each RII,
    // hence prohibiting sending a message to a Rexx object from a different RII
    if (instance != tmpInstance)    // error (do not use a different Rexx Interpreter instance!)
    {
       char *msg=new char[1024];
       snprintf( msg, 1024, "%.16s/routine/jniRexxSendMessageToRexxObject(), error 2: Rexx interpreter instance with the ID '%.256s' could not be found", DLLNAME, c_rii_ID);

       jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
       delete[] msg;
       free(c_rii_ID);        // make sure we free the allocated memory

       env->Throw(j_rexxException);  // throw the exception in the JVM
       return NULL;
    }




#if defined (INFO_ATTACH_THREAD)        // rgf, 2017-10-04
    fprintf(stderr, "            ... _jniRexxSendMessageToRexxObject(),      count=[%8d], tid=[%6lu], rii_ID=[%p]->AttachThread(): about...\n",
                    count, (unsigned long) RgfGetTID(), instance );
    fflush(stderr);
#endif

    // rgf, 2013-10-07: no need to do a rtc->ReleaseLocalReference(ro) as these references get released upon rtc->DetachThread() !
    if (!instance->AttachThread(&rtc))          // get a ThreadContext
    {
#if defined (INFO_ATTACH_THREAD)        // rgf, 2017-08-19
    fprintf(stderr, "            ... _jniRexxSendMessageToRexxObject(),      count=[%8d], tid=[%6lu], rii_ID=[%p]->AttachThread(): PANIC, did not work!\n",
                    count, (unsigned long) RgfGetTID(), instance);
    fflush(stderr);
#endif

        char *msg=new char[1024];
        snprintf(msg, 1024, "%.16s/routine/jniRexxSendMessageToRexxObject(), error 3: could not AttachThread() to Rexx interpreter instance with the ID '%.256s'", DLLNAME, c_rii_ID);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        free(c_rii_ID);       // make sure we free the allocated memory

        env->Throw(j_rexxException);  // throw the exception in the JVM
        return NULL;
    }

#if defined (INFO_ATTACH_THREAD)        // rgf, 2017-08-19
    fprintf(stderr, "            ... _jniRexxSendMessageToRexxObject(),      count=[%8d], tid=[%6lu], rii_ID=[%p]->AttachThread(): done, rtc=[%p].\n",
                    count, (unsigned long) RgfGetTID(), instance, rtc );
    fflush(stderr);
#endif

        // get Rexx object
    char *c_obj_ID=(char *) JNU_GetStringNativeChars(env, j_obj_ID);    // convert to native string
    RexxStringObject rso=rtc->String(c_obj_ID);
    RexxObjectPtr ro=RgfGetProxyObject(rtc, rso);     // get Rexx object

    // rgf, 2009-06-22: creating a directory will cause the creation of the additional (last) argument  containing
    // the original method name (case preserving) and the TID in which the RexxProxy being used got created
    RexxDirectoryObject slotDir=NULL; // rgf, 2009-06-22 this will always get added as the last argument

    char *c_msg=NULL;           // will receive the message name from the Java argument
    c_msg      =(char *) JNU_GetStringNativeChars(env, j_messageName);    // convert to native string

    if ((j_userData_ID!=NULL) ||
        (j_methodObjectBean!=NULL) ||
        (j_methodDescription!=NULL))      // build slotArgument (an ooRexx directory object) ?
    {
        // rgf, 2017-04-15, use new rcoSlotArgumentClass (a subclass of directory to allow unambiguous testing)
        RexxObjectPtr clzSlotArgument=rtc->FindClass("Slot.Argument");  // defined in BSF.CLS since 20171223, stored in .environment too
        if (clzSlotArgument==NULL)      // oops, not there?
        {
            char *msg=new char[1024];
            snprintf(msg, 1024, "%.16s/routine/jniRexxSendMessageToRexxObject(), error 4: public Rexx class 'Slot.Argument' from 'BSF.CLS' not found, cannot create slot argument; Rexx class Slot.Argument=[%p], BSF=[%p]", DLLNAME, rtc->FindClass("Slot.Argument"), rtc->FindClass("BSF"));

            jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                                 NULL, // rtc,
                                                                 NULL, // conditionObject
                                                                 REASON_EXECUTION_ERROR,   // reason
                                                                 msg   // message
                                                            );
            delete[] msg;
            rtc->DetachThread();

            free(c_rii_ID);       // make sure we free the allocated memory
            free(c_msg   );       // make sure we free the allocated memory
            free(c_obj_ID);       // make sure we free the allocated memory

            env->Throw(j_rexxException);  // throw the exception in the JVM
            return NULL;
        }

        slotDir=(RexxDirectoryObject) rtc->SendMessage0(clzSlotArgument, "NEW");    // create "SLOT.ARGUMENT" instance, ooRexx directory object

        if (j_userData_ID!=NULL)    // userData is given, hence create directory containing it (will get supplied as last argument)
        {
            char *c_userData_ID=(char *) JNU_GetStringNativeChars(env, j_userData_ID);    // convert to native string
            rso=rtc->String(c_userData_ID);
            RexxObjectPtr ro_userData=RgfGetProxyObject(rtc, rso);  // get userData Rexx object
            rtc->DirectoryPut(slotDir, ro_userData, "USERDATA");            // make userData available
            free(c_userData_ID);      // make sure we free the allocated memory
        }

        if (j_methodObjectBean!=NULL)    // userData is given, hence create directory containing it (will get supplied as last argument)
        {
            char *c_methodObjectBean=(char *) JNU_GetStringNativeChars(env, j_methodObjectBean);    // convert to native string
            RexxObjectPtr ro_methodObject=rtc->String(c_methodObjectBean);  // turn native string into a RexxString
            free(c_methodObjectBean);     // make sure we free the allocated memory

            RexxObjectPtr clzBSF=rtc->FindClass("BSF");
            if (clzBSF != NULL ) // BSF.CLS got loaded ?
            {
                // 20161220, rgf: RexxEngine.java does increase the refCount (faster) as the .BSF unknown method will do an unregisterBean as well
                ro_methodObject=(RexxStringObject) rtc->SendMessage1(clzBSF, "BSF.WRAP", ro_methodObject);
            }

            rtc->DirectoryPut(slotDir, ro_methodObject, "METHODOBJECT");    // save it in callback directory
        }

        if (! rtc->CheckCondition())    // so far no problems? then proceed
        {
            if (j_javaObjectBean!=NULL)    // 2009-07-11: if dealing with an object from a dynamically created Java class (i.e. extending an abstract Java class)
            {
                char *c_javaObjectBean=(char *) JNU_GetStringNativeChars(env, j_javaObjectBean);    // convert to native string
                RexxObjectPtr ro_javaObject=rtc->String(c_javaObjectBean);  // turn native string into a RexxString
                free(c_javaObjectBean);     // make sure we free the allocated memory

                RexxObjectPtr clzBSF=rtc->FindClass("BSF"); // BSF.CLS available, if so, wrap it up as a BSF proxy
                if (clzBSF != NULL ) // BSF.CLS got loaded ?
                {
                    // 20161220, rgf: RexxEngine.java does increase the refCount (faster) as the .BSF unknown method will do an unregisterBean as well
                    ro_javaObject=(RexxStringObject) rtc->SendMessage1(clzBSF, "BSF.WRAP", ro_javaObject);
                }

                rtc->DirectoryPut(slotDir, ro_javaObject, "JAVAOBJECT");    // save it in callback directory
            }

            if (j_methodDescription!=NULL)     // 2009-07-10
            {
                char *c_msgDesc   =(char *) JNU_GetStringNativeChars(env, j_methodDescription);    // convert to native string
                RexxStringObject rso=rtc->String(c_msgDesc);
                rtc->DirectoryPut(slotDir, rso, "METHODDESCRIPTOR");   // make original method name available

                free(c_msgDesc);      // make sure we free the allocated memory
            }

            RexxStringObject rso=rtc->String(c_msg);
            rtc->DirectoryPut(slotDir, rso, "METHODNAME");   // make original method name available
        }
    }


    // if scope supplied check whether it is a class object
    RexxClassObject rcoScope = NULL;    // scope override
    if ( ! rtc->CheckCondition() && j_scope!=NULL )
    {
        char *c_scope = (char *) JNU_GetStringNativeChars(env, j_scope);    // convert to native string
        RexxStringObject rso=rtc->String(c_scope);      // if string is an object_ID then get its object
        RexxObjectPtr ro=RgfGetProxyObject(rtc, rso);     // get Rexx object
        if (ro==rtc->Nil())             // o.k. not in registry, use string argument as class name to look-up
        {
            rcoScope=rtc->FindClass(c_scope);   // NULL(OBJECT) or class object
// fprintf(stderr, "/// BSF4ooRexx # %d: ro==rtc->Nil() : rcoScope=[%p], c_scope=[%s]\n", __LINE__, rcoScope, c_scope);fflush(stderr);

        }
        else
        {
            if (rtc->IsOfType(ro,"CLASS"))
            {
                rcoScope=(RexxClassObject) ro;
            }
// fprintf(stderr, "/// BSF4ooRexx # %d:  ro=[%p] : rcoScope=[%p], c_scope=[%s]\n", __LINE__, ro, rcoScope, c_scope);fflush(stderr);
        }

        if (rcoScope==NULL)     // error: scope does not resolve to a class object!
        {
            char *msg=new char[1024];
            snprintf( msg, 1024, "%.16s/routine/jniRexxSendMessageToRexxObject(), error 4b: argument SCOPE=[%s] does not resolve to an ooRexx class object", DLLNAME, c_scope);

            jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                                  NULL, // rtc,
                                                                  NULL, // conditionObject
                                                                  REASON_INVALID_ARGUMENT,   // reason
                                                                  msg   // message
                                                             );

            delete[] msg;
            free(c_scope);

            free(c_rii_ID);       // make sure we free the allocated memory
            free(c_msg   );       // make sure we free the allocated memory
            free(c_obj_ID);       // make sure we free the allocated memory

            rtc->DetachThread();

            env->Throw(j_rexxException);  // throw the exception in the JVM
            return NULL;
        }
        free(c_scope);
    }


    if (! rtc->CheckCondition())    // so far no problems? then proceed
    {
#if defined (DEBUG_40) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
   RexxStringObject rxStrObj=(RexxStringObject) rtc->SendMessage0(ro, "STRING"); // debug: get string name of object that receives the message
#endif

        if (j_args==NULL)              // no args: hence message with a slotDir or without arguments altogether?
        {

#if defined (INFO_SEND_MESSAGE_TO_REXX_OBJECT)        // rgf, 2017-08-19
                // 2017-10-05: in case ObjectToStringValue() would block, show object pointer first
    fprintf(stderr, "       -------> _jniRexxSendMessageToRexxObject(), send!count=[%8d], tid=[%6lu], rii_ID=[%p], rtc=[%p]: 1a. [%p]~%s\n",
                             count, (unsigned long) RgfGetTID(), instance, rtc
                             , ro, c_msg
                             );
    fflush(stderr);
    fprintf(stderr, "       -------> _jniRexxSendMessageToRexxObject(), send!count=[%8d], tid=[%6lu], rii_ID=[%p], rtc=[%p]: 1b. [%s]~%s\n",
                             count, (unsigned long) RgfGetTID(), instance, rtc
                             , rtc->ObjectToStringValue(ro), c_msg
                             );
    fflush(stderr);
#endif

            if (slotDir==NULL)
            {
                if (rcoScope==NULL)
                {
                    result_obj=rtc->SendMessage0(ro, c_msg);   // no arguments, no slotArgument
                }
                else
                {
// fprintf(stderr, "*** BSF4ooRexx.cc:# %d _jniRexxSendMessageToRexxObject: SendMessageScoped(), NO ARGS ", __LINE__); fflush(stderr);
                    result_obj=rtc->SendMessageScoped(ro, c_msg, rcoScope, rtc->NewArray(0));   // scope override
                }
            }
            else    // supply the slotDir
            {
                if (rcoScope==NULL)
                {
                    result_obj=rtc->SendMessage1(ro, c_msg, slotDir);   // no arguments; but supply callback directory
                }
                else
                {
// fprintf(stderr, "*** BSF4ooRexx.cc:# %d _jniRexxSendMessageToRexxObject: SendMessageScoped(), SINGLE SLOTDIR ", __LINE__); fflush(stderr);
                    result_obj=rtc->SendMessageScoped(ro, c_msg, rcoScope,  // scope override
                                                      rtc->ArrayOfOne(slotDir) );
                }
            }
        }
        else
        {
                // process Java argument array, get matching RexxArrayObject
                // 20150721, have BSF registry reference counter increased
            RexxArrayObject ra=RgfProcessJArgs(env, rtc, j_args, slotDir, 1, 1);

#if defined (DEBUG_40) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "...jniRexxSendMessageToRexxObject(): thisTid=[%lu]... ro=[%p] [%.64s], SendMessage: c_msg=[%.256s], ra=[%p], size=[%u] ...\n",
                                       (unsigned long) thisTid, ro, rtc->CString(rxStrObj), c_msg, ra, (unsigned) rtc->ArraySize(ra));
    fflush(stderr);
#endif


#if defined (INFO_SEND_MESSAGE_TO_REXX_OBJECT)        // rgf, 2017-08-19
                // 2017-10-05: in case ObjectToStringValue() would block, show object pointer first
    fprintf(stderr, "       -------> _jniRexxSendMessageToRexxObject(), send!count=[%8d], tid=[%6lu], rii_ID=[%p], rtc=[%p]: 2a. [%p]~%s\n",
                             count, (unsigned long) RgfGetTID(), instance, rtc
                             , ro, c_msg
                             );
    fflush(stderr);
    fprintf(stderr, "       -------> _jniRexxSendMessageToRexxObject(), send!count=[%8d], tid=[%6lu], rii_ID=[%p], rtc=[%p]: 2b. [%s]~%s(...)\n",
                             count, (unsigned long) RgfGetTID(), instance, rtc
                             , ( rtc->IsArray(ro) ? "an Array" : rtc->ObjectToStringValue(ro) )
                             , c_msg
                             );
    fflush(stderr);
#endif

            if (rcoScope==NULL)
            {
                result_obj=rtc->SendMessage(ro, c_msg, ra); // with arguments
            }
            else
            {
// fprintf(stderr, "*** BSF4ooRexx.cc:# %d _jniRexxSendMessageToRexxObject: SendMessageScoped(), with arguments ", __LINE__); fflush(stderr);
                result_obj=rtc->SendMessageScoped(ro, c_msg, rcoScope, ra); // scope override
            }

        }
#if defined (INFO_SEND_MESSAGE_TO_REXX_OBJECT)        // rgf, 2017-08-19
    fprintf(stderr, "       <------- _jniRexxSendMessageToRexxObject(), retu!count=[%8d], tid=[%6lu], rii_ID=[%p], rtc=[%p]: result_obj=[%p]\n",
                             count, (unsigned long) RgfGetTID(), instance, rtc, result_obj);
    fflush(stderr);
#endif
    }

    if (c_msg!=NULL)            // did we use it, if so, free allocated memory
    {
        free(c_msg);          // make sure we free the allocated memory
    }

    free(c_rii_ID);           // make sure we free the allocated memory
    free(c_obj_ID);           // make sure we free the allocated memory

    // check for exception; if so, create RexxException, supply condition object as a RexxProxy; clear exception in thread
    if (rtc->CheckCondition())      // a Rexx condition raised?
    {
        RexxDirectoryObject condObj=rtc->GetConditionInfo();
        char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "/routine/jniRexxSendMessageToRexxObject(), error 5");
        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             rtc, // rtc,
                                                             condObj, // conditionObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );

#if defined (INFO_DETACH_THREAD)        // rgf, 2017-08-19
    fprintf(stderr, "            ... _jniRexxSendMessageToRexxObject(), CheckCondition!     count=[%8d], tid=[%6lu], rtc=[%p]->DetachThread(): about...\n",
                    count, (unsigned long) RgfGetTID(), rtc );
    fflush(stderr);
#endif

        rtc->DetachThread();

#if defined (INFO_DETACH_THREAD)        // rgf, 2017-08-19
    fprintf(stderr, "            ... _jniRexxSendMessageToRexxObject(), CheckCondition!     count=[%8d], tid=[%6lu], rtc=[%p]->DetachThread(): done.\n",
                    count, (unsigned long) RgfGetTID(), rtc );
    fflush(stderr);
#endif

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "done. <--------------\n"); fflush(stderr);
#endif

        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG_REXX_PROXY)  || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "<-- returning: ...jniRexxSendMessageToRexxObject(...): thisTid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",(unsigned long) tid, msg);fflush(stderr);
#endif

        free(msg);
        return NULL;
    }


#if defined(DEBUG_JNI) || defined (DEBUG_40) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_SEND_MESSAGE_TO_REXX_OBJECT)
    fprintf(stderr, "<-- returning from: .._jniRexxSendMessageToRexxObject(...): thisTid=[%lu], everything o.k.\n",(unsigned long) thisTid);fflush(stderr);
#endif

    jobject jresult=RgfProcessReturnValue4Java(env, jobj, rtc, result_obj, j_returnType);

#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "<-------------- ooRexx->DetachThread() - jniRexxSendMessageToRexxObject... "); fflush(stderr);
#endif

#if defined (INFO_DETACH_THREAD)        // rgf, 2017-08-19
    fprintf(stderr, "            ... _jniRexxSendMessageToRexxObject(),      count=[%8d]: tid=[%6lu], rii_ID=[%p], rtc=[%p]->DetachThread(), about...\n",
                           count, (unsigned long) RgfGetTID(), instance, rtc); fflush(stderr);
    fflush(stderr);
#endif

    rtc->DetachThread();

#if defined (INFO_DETACH_THREAD)        // rgf, 2017-08-19
    fprintf(stderr, "<-------------- _jniRexxSendMessageToRexxObject(),      count=[%8d]: tid=[%6lu], rii_ID=[%p], rtc=[%p]->DetachThread(): done.\n<--------------\n\n",
                           count, (unsigned long) RgfGetTID(), instance, rtc); fflush(stderr);
    fflush(stderr);
#endif


#if defined (DEBUG_REXX_ATTACH)
    fprintf(stderr, "done. <--------------\n"); fflush(stderr);
#endif

    return jresult;
}




// ======== 2021-08-04, rgf: refactor to allow RexxAndJava and RexxCleanupRef to use this JNI function ===>
inline jint impl_jniRexxHaltInterpreterInstance (JNIEnv *env, jobject jobj, jstring j_rii_ID)
{
#if defined(DEBUG_JNI) || defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_JNI_ENTRY)
    thread_id_t thisTid=RgfGetTID();

    fprintf(stderr, "--> arrived: .._jniRexxHaltInterpreterInstance(...), thisTid=[%lu]\n", (unsigned long) thisTid);
    fflush(stderr);
#endif

    char *c_rii_ID=NULL;
    RexxInstance *instance=NULL, *tmpInstance=NULL;

    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string
    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    free(c_rii_ID);               // make sure we free the allocated memory

    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

#if defined (INFO_HALT_RII)        // rgf, 2017-08-19
    static int countRII=0;

    if (instance!=tmpInstance)
    {
        fprintf(stderr, "---> arrived in _jniRexxHaltInterpreterInstance,        count=[%8d], tid=[%6lu], rii_ID=[%p] <> rii_ID=[%p] (NOT OK!)\n",
                        ++countRII, (unsigned long) RgfGetTID(), instance, tmpInstance);
    }
    else
    {
        fprintf(stderr, "---> arrived in _jniRexxHaltInterpreterInstance,        count=[%8d], tid=[%6lu], rii_ID=[%p]\n",
                        ++countRII, (unsigned long) RgfGetTID(), instance );
    }


    fflush(stderr);
#endif

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
       char *msg=new char[1024];
       snprintf(msg, 1024, "%.16s/routine/jniRexxHaltInterpreterInstance(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException); // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: .._jniRexxHaltInterpreterInstance(...): thisTid=[%lu], error, cannot retrieve c_rii_ID=instance=[%p], received=tmpInstance=[%p] instead\n",
                     (unsigned long) thisTid, instance, tmpInstance);
    fflush(stderr);
#endif

        return (jint) -1;
    }
    else
    {
#if defined(DEBUG_JNI)
    fprintf(stderr, "--> .._jniRexxHaltInterpreterInstance(...): thisTid=[%lu], c_rii_ID=p=[%p], NOW 'instance->Halt()'\n",
                     (unsigned long) thisTid, instance);
    fflush(stderr);
#endif


#if defined (DEBUG_CREATE_HALT_TERMINATE)
        fprintf(stderr, "===---=== .._jniRexxHaltInterpreterInstance(...): thisTid=[%lu], HALTING: c_rii_ID={%p]\n", (unsigned long) thisTid, instance);
        fflush(stderr);
#endif


#ifdef WINDOWS
    // __asm Int 3;    // break
    // DebugBreak();

    // fprintf(stderr, ".._jniRexxHaltInterpreterInstance(...): BREAK_HERE! \n");fflush(stderr);
    // BREAK_HERE_SINGLESTEP();
#endif

        instance->Halt();           // signal HALT to all interpreter threads; a void

#if defined (INFO_HALT_RII)        // rgf, 2017-08-19
    fprintf(stderr, "... done:  _jniRexxHaltInterpreterInstance,        count=[%8d], tid=[%6lu], rii_ID=[%p]\n",
                    countRII, (unsigned long) RgfGetTID(), instance );
    fflush(stderr);
#endif

    }


#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning from: .._jniRexxHaltInterpreterInstance(...), thisTid=[%lu]\n",  (unsigned long) thisTid);
    fflush(stderr);
#endif

    return (jint) 0;
}
// <======== 2021-08-04, rgf =============================================================================


// ----------------------------------------------------------------------------------------------
/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniRexxHaltInterpreterInstance
 * Signature: (Ljava/lang/String;)I
 */

JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxHaltInterpreterInstance

  (JNIEnv *env, jobject jobj, jstring j_rii_ID)
{
    return impl_jniRexxHaltInterpreterInstance(env, jobj, j_rii_ID);
}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxCleanupRef
 * Method:    jniRexxHaltInterpreterInstance
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxCleanupRef_jniRexxHaltInterpreterInstance
  (JNIEnv *env, jobject jobj, jstring j_rii_ID)
{
    return impl_jniRexxHaltInterpreterInstance(env, jobj, j_rii_ID);
}
// ----------------------------------------------------------------------------------------------


// ======== 2021-08-04, rgf: refactor to allow RexxAndJava and RexxCleanupRef to use this JNI function ===>
//          2022-07-24, rgf: cater for concurrent terminations of the same RII
/*
    returns: -2  ... indicate primodal RII in hand, this RII will never be terminated!
             -99 ... indicate that RII could not get deleted from list, could have been concurrently terminated() in between!
*/
inline jint impl_jniRexxTerminateInterpreterInstance (JNIEnv *env, jobject jobj, jstring j_rii_ID)
{

#if defined(DEBUG_JNI) || defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_TERMINATE_INTERPRETER) || defined (DEBUG_JNI_ENTRY)
    thread_id_t thisTid=RgfGetTID();

    fprintf(stderr, "--> arrived: .._jniRexxTerminateInterpreterInstance(...), thisTid=[%lu]\n", (unsigned long) thisTid);
    fflush(stderr);
#endif

    char *c_rii_ID=NULL;
    RexxInstance *instance=NULL, *tmpInstance=NULL;

    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

#if defined(DEBUG_JNI) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(1)  .._jniRexxTerminateInterpreterInstance(...), thisTid=[%lu], c_rii_ID=[%.256s]\n", (unsigned long) thisTid, c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

#if defined (INFO_TERMINATE_RII)        // rgf, 2017-08-19
    static int countRII=0;

    if (instance!=tmpInstance)
    {
        fprintf(stderr, "\n===> PROBLEM in _jniRexxTerminateInterpreterInstance(),   count=[%8d], tid=[%6lu], rii_ID=[%p] <> rii_ID=[%p] (NOT OK!)\n",
                        ++countRII, (unsigned long) RgfGetTID(), instance, tmpInstance);
        fflush(stderr);
    }
    else
    {
        fprintf(stderr, "\n===> arrived in _jniRexxTerminateInterpreterInstance(), count=[%8d], tid=[%6lu], rii_ID=[%p]\n",
                        ++countRII, (unsigned long) RgfGetTID(), instance);
        fflush(stderr);
    }

#endif

#if defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(2)  .._jniRexxTerminateInterpreterInstance(...), thisTid=[%lu], c_rii_ID=[%p]: after 'RgfGetRexxInterpreterInstanceFromList(instance)': tmpInstance=[%p] \n", (unsigned long) thisTid, instance, tmpInstance);
    fflush(stderr);
#endif

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {

#if defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, ".._jniRexxTerminateInterpreterInstance(...): thisTid=[%lu], error, instance p=[%p]<>tmpInstance=[%p] instead\n",
                    (unsigned long) thisTid, instance, tmpInstance);
    fflush(stderr);
#endif

        char *msg=new char[1024];
        snprintf(msg, 1024, "%.16s/routine/jniRexxTerminateInterpreterInstance(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;

        env->Throw(j_rexxException); // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "<-- returning: .._jniRexxTerminateInterpreterInstance(...): thisTid=[%lu], error, cannot retrieve instance p=[%p], received=[%p] instead\n",
                    (unsigned long) thisTid, instance, tmpInstance);
    fflush(stderr);
#endif

        free(c_rii_ID);               // make sure we free the allocated memory
        return (jint) -1;
    }

    else
    {

#if defined(DEBUG_JNI) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "/// .._jniRexxTerminateInterpreterInstance(...): thisTid=[%lu], (priModalInstance=[%p] == instance p=[%p]) ? [%d]\n",
                    (unsigned long) thisTid, pRoot_RII->instance, instance, (pRoot_RII->instance==instance));
    fflush(stderr);
#endif
        if (pRoot_RII->instance == instance)    // do not terminate primodal RII !
        {
            return -2;
        }

#ifdef WINDOWS
    // __asm Int 3;    // break
    // DebugBreak();

    // fprintf(stderr, ".._jniRexxTerminateInterpreterInstance(...): BREAK_HERE! \n");fflush(stderr);
    // BREAK_HERE_SINGLESTEP();
#endif

#if defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(3a) .._jniRexxTerminateInterpreterInstance(...): removing Rexx instance from list, thisTid=[%lu], tmpInstance=[%p]\n", (unsigned long) thisTid, tmpInstance);
    fflush(stderr);
#endif

        // 2022-07-24: critical section, begin
        RgfAcquireLock2(RII_lock);      // lock access to list of RexxInstance instances

        // remove instance from list
        // 2012-02-07: get RII node, DeleteGlobalRef() for rajo and rexxconf, before structure gets freed
        PSTRUCT_RII struRii=RgfGetRexxInterpreterInstanceStructFromList(tmpInstance);
        jobject riiRajo=NULL;
        jobject riiRexxconf=NULL;
        if (struRii!=NULL) {        // get jobject references
            riiRajo=struRii->rajo;
            riiRexxconf=struRii->rexxconf;
        }

            // remove RII from list, will free RII structure, if found
        if (RgfRemoveRexxInterpreterInstanceFromList(tmpInstance) == NULL)
        {
            // 2022-07-24: critical section, end
            RgfReleaseLock2(RII_lock);      // lock access to list of RexxInstance instances
            return -99;         // 2022-07-24: indicate that instance got terminated in between concurrently
        }

            // o.k. now delete global references to rajo and rexxconf stored with RII structure
        if (riiRajo!=NULL) {
            env->DeleteGlobalRef(riiRajo);
        }
        if (riiRexxconf!=NULL) {
            env->DeleteGlobalRef(riiRexxconf);
        }

        // remove instance from list
#if defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(3b) .._jniRexxTerminateInterpreterInstance(...): after*** Rexx instance from list, thisTid=[%lu], tmpInstance=[%p]\n", (unsigned long) thisTid, tmpInstance);
    fflush(stderr);
#endif


#if defined (DEBUG_CREATE_HALT_TERMINATE) || defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(4a) .._jniRexxTerminateInterpreterInstance(...): before Terminate(), thisTid=[%lu], c_rii_ID=tmpInstance=[%p]\n", (unsigned long) thisTid, tmpInstance);
    fflush(stderr);
#endif

        tmpInstance->Terminate();    // wait until all interpreter threads terminated

        // 2022-07-24: critical section, end
        RgfReleaseLock2(RII_lock);      // lock access to list of RexxInstance instances

#if defined (DEBUG_TERMINATE_INTERPRETER)
    fprintf(stderr, "(4b) .._jniRexxTerminateInterpreterInstance(...): AFTER Terminate(), thisTid=[%lu], tmpInstance=[%p]\n", (unsigned long) thisTid, tmpInstance);
    fflush(stderr);
#endif

    }

    free(c_rii_ID);               // make sure we free the allocated memory

#if defined(DEBUG_JNI) || defined (DEBUG_TERMINATE_INTERPRETER)

    fprintf(stderr, "<-- returning from: .._jniRexxTerminateInterpreterInstance(...), thisTid=[%lu]\n", (unsigned long) thisTid);
    fflush(stderr);
#endif

    return (jint) 0;
}

// ----------------------------------------------------------------------------------------------
/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniRexxTerminateInterpreterInstance
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniRexxTerminateInterpreterInstance

  (JNIEnv *env, jobject jobj, jstring j_rii_ID)
{
    return impl_jniRexxTerminateInterpreterInstance(env, jobj, j_rii_ID);
}

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxCleanupRef
 * Method:    jniRexxTerminateInterpreterInstance
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxCleanupRef_jniRexxTerminateInterpreterInstance

  (JNIEnv *env, jobject jobj, jstring j_rii_ID)
{
    return impl_jniRexxTerminateInterpreterInstance(env, jobj, j_rii_ID);
}
// ----------------------------------------------------------------------------------------------





// ======== 2021-08-04, rgf: refactor to allow RexxAndJava and RexxCleanupRef to use this JNI function ===>
/*
    returns value received from RgfRemoveProxyObject():

          >0  still references left
          =0  no more references left, entries got removed from registry
          -1  Rexx proxy object could not be found
          -2  Java was invoked by Rexx, do not free proxies, see comment inline
        -100  No RexxInterpreter instance could be found
        -101  thread could not be attached to RexxInterpreter instance
*/

inline jint impl_jniUnregisterRexxObject (JNIEnv *env, jobject jobj, jstring obj_ID)
{
#if defined(DEBUG_JNI) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_UNREGISTER_REXX_OBJECT) || defined (DEBUG_JNI_ENTRY)
    thread_id_t thisTid=RgfGetTID();
    fprintf(stderr, "*** --> arrived: .._jniRexxUnregisterRexxObject(...), thisTid=[%lu]\n",  (unsigned long) thisTid);
    fflush(stderr);
#endif

//  rgf, 20161220: on ooRexx 5.0 beta no problem; creating a global boolean variable bsfDoUnregisterRexxObject
//                 which can be changed by the new external BSF-function BsfDoUnregisterRexxObject in case
//                 traps occur because of this at the end of a Rexx program (when a Java RexxProxy gets finalized
//                 and this callback gets used
    // global variable: int bsfInvokedBy = 0;     // 0=noJVM, 1=byJava, 2=byRexx
    if (bsfInvokedBy==2 &&  bsfDoUnregisterRexxObject==false)    // BSF4Rexx invoked by Rexx (Java loaded via Rexx) ?
    {
        return -2;                  // indicate that not processed
    }

    int32_t result=0;

    if (obj_ID==NULL)   // create Java exception !
    {
        // char msg[1024]="";
        char *msg=new char[1024];
        // sprintf(msg, "%.16s/routine/jniUnregisterRexxObject(), error 1: object ID of Rexx proxy must not be 'NULL'", DLLNAME);
        snprintf(msg, 1024, "%.16s/routine/jniUnregisterRexxObject(), error 1: object ID of Rexx proxy must not be 'NULL'", DLLNAME);

        // JNU_ThrowByName(env, "org.apache.bsf.BSFException", msg);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, jobj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException); // throw the exception in the JVM

#if defined(DEBUG_JNI) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_UNREGISTER_REXX_OBJECT)
    fprintf(stderr, "*** <-- returning: .._jniRexxUnregisterRexxObject(...): thisTid=[%lu], error, obj_ID=[NULL] !\n",
                                        (unsigned long) thisTid);
    fflush(stderr);
#endif

        return (jint) -1;
    }
    else
    {
        // make a copy of the Java string containing the Rexx script to be executed
        CSTRING       str=env->GetStringUTFChars(obj_ID, JNI_FALSE);
        char    *c_obj_ID =(char *) malloc(strlen(str)+1) ; // allocate memory
        strcpy(c_obj_ID, str);                      // copy data from JNI area
        env->ReleaseStringUTFChars(obj_ID, str);    // release str

        result=RgfRemoveProxyObject(c_obj_ID);      // not found: -1; else remaining refs: >=0

if (result==-1)
{
    fprintf(stderr, "*** UNEXPECTED, UNEXPECTED, UNEXPECTED: while removing obj_ID=[%s], Rexx object could not be found in BSF4ooRexx registry! UNEXPECTED, UNEXPECTED, UNEXPECTED ***\n",
                     c_obj_ID);
    fflush(stderr);
}

        free(c_obj_ID) ;                  // free the memory again
    }

#if defined(DEBUG_JNI) || defined (DEBUG_REXX_PROXY) || defined (DEBUG_UNREGISTER_REXX_OBJECT)
    fprintf(stderr, "*** <-- returning from: .._jniRexxUnregisterRexxObject(...), thisTid=[%lu]\n",  (unsigned long) thisTid);
    fflush(stderr);
#endif

    return (jint) result;       // return reference counter value
}





// ----------------------------------------------------------------------------------------------
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
    return impl_jniUnregisterRexxObject (env, jobj, obj_ID);
}


/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxCleanupRef
 * Method:    jniUnregisterRexxObject
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_rexxla_bsf_engines_rexx_RexxCleanupRef_jniUnregisterRexxObject

  (JNIEnv *env, jobject jobj, jstring obj_ID)
{
    return impl_jniUnregisterRexxObject (env, jobj, obj_ID);
}
// ----------------------------------------------------------------------------------------------






// ----------------------------------------------------------------------------------------------
// rgf, 2012-02-07: exit and command handlers, cooperating with RexxConfiguration

// ----------------------------------------------------------------------------------------------
// Generic Rexx exit handler function, which will attach to Java, fetch the matching Java exit handler
// and invoke it.
int REXXENTRY rexx_exit_handler_entry (RexxExitContext *context, int exitNumber, int subfunction, void *parmBlock)
{
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
    fprintf(stderr, "*** rexx_exit_handler_entry() 1, exitNumber=[%d], subfunction=[%d] ---> ...\n",
                    exitNumber, subfunction
            );
    fflush(stderr);
#endif

    char *msg=new char[1024];

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

        snprintf(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 1: panic - cannot find Rexx interpreter instance related Java interface objects, exitNumber=[%d], subfunction=[%d] for RexxInterpreterInstance=[%p]",
                            DLLNAME, exitNumber, subfunction, ri);
        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return RXEXIT_RAISE_ERROR;
    }

    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *) context->threadContext->instance, false, 0};
    RgfAcquireLock();
    environmentAttachToNew(&param);    // attach to Java

    if (param.error != 0)   // could not attach to Java
    {
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
    if (exitNumber==RXINI || exitNumber==RXTER)
    {
        fprintf(stderr,  "C++ >> >> >> 2 << << << 'rexx_exit_handler_entry()', [%s] - returning prematurely!\n",(exitNumber==RXINI ? "RXINI" : "RXTER"));fflush(stderr);
    }
#endif
        RgfReleaseLock();
        snprintf(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 2: panic - cannot attach to Java, exit number=[%d], subfunction=[%d] for RexxInterpreterInstance=[%p]",
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
        snprintf(msg, 1024, "%%.16s/internal/rexx_exit_handler_entry(), error 3: Java exception while attempting to fetch the Java exit handler for system exit number=[%d], subfunction=[%d] - [%%s]",
                            exitNumber, subfunction);

        RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
             0,         // RexxExitContext
             context,
             msg);
        RgfAcquireLock();
        environmentDetachFromNew(&param);
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
        environmentDetachFromNew(&param);
        RgfReleaseLock();

#if defined ( RGF_SYSTEM_EXIT_HANDLER )
        fprintf(stderr, "*** rexx_exit_handler_entry() 1b, exitNumber=[%d], subfunction=[%d] - CURRENTLY NOT HANDLED BY JAVA, RETURNING \"RXEXIT_NOT_HANDLED\" <--- ...\n",
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

#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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

#if defined ( RGF_SYSTEM_EXIT_HANDLER )
                RXCMDHST_PARM *pb =(RXCMDHST_PARM *) parmBlock;
                fprintf(stderr, "---> RXCMD=3: [%d], subfunction: [%d], address=[%.32s], dllname=[%.32s], command=[%.64s]\n",
                                    exitNumber,
                                    subfunction,
                                    pb->rxcmd_address,
                                    pb->rxcmd_dll,
                                    pb->rxcmd_command.strptr
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

#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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

#if defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d], RXSIOSAY=[%d]\n",
                                    exitNumber,subfunction, RXSIOSAY);
#endif

                if (subfunction==RXSIOSAY)   // 1
                {
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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
    #if defined ( RGF_SYSTEM_EXIT_HANDLER )
            {
                fprintf(stderr, "---> RXINI=9, exitNumber: [%d], subfunction: [%d]\n",
                                 exitNumber, subfunction);
            }
    #endif
            break;


            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
            // nothing to setup, so just break
        case RXTER:       // system exit # 10, "termination processing"
    #if defined ( RGF_SYSTEM_EXIT_HANDLER )
            {
                fprintf(stderr, "---> RXTER=10, exitNumber: [%d], subfunction: [%d]\n",
                                 exitNumber, subfunction);
            }
    #endif
            break;

            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        case RXNOVAL:       // system exit # 13, "processes a Rexx NOVALUE condition"
            {
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
                fprintf(stderr, "---> RXNOVAL=13, exitNumber: [%d], subfunction: [%d]\n",
                                 exitNumber, subfunction);
#endif

                if (subfunction==RXNOVALCALL)   // 1
                {
                        RXVARNOVALUE_PARM *pb=(RXVARNOVALUE_PARM *) parmBlock;
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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

#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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
            environmentDetachFromNew(&param);
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
        environmentDetachFromNew(&param);
        RgfReleaseLock();
        delete[] msg;
        return RXEXIT_RAISE_ERROR;
    }
        // public abstract Object handleCommand(String address, String command, Object[] slot);
    jobject jresult=NULL;
    int j_exit_rc=0;

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
        environmentDetachFromNew(&param);
        RgfReleaseLock();

        snprintf(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 4: Java exception, cannot find the Java Rexx exit handler method named [%s], needed for processing the Rexx system exit number=[%d], subfunction=[%d]",
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
        snprintf(msg, 1024, "%%.16s/internal/rexx_exit_handler_entry(), error 5: Java exception while running the Java exit handler for system exit number=[%d], subfunction=[%d] - [%%s]",
                            exitNumber, subfunction);

        RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
             0,         // RexxExitContext
             context,
             msg);

        param.env->DeleteLocalRef(jhandler);
        param.env->DeleteLocalRef(jslot);
        param.env->DeleteLocalRef(jpb);

        RgfAcquireLock();
        environmentDetachFromNew(&param);
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
        environmentDetachFromNew(&param);
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

    #if defined ( RGF_SYSTEM_EXIT_HANDLER )
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
                            snprintf(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 6: Java RXFNC=2 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object",
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

    #if defined ( RGF_SYSTEM_EXIT_HANDLER )
                    RXCMDHST_PARM *pb =(RXCMDHST_PARM *) parmBlock;
                    fprintf(stderr, "<--- RXCMD=3: [%d], subfunction: [%d], address=[%.32s], dllname=[%.32s], command=[%.64s]\n",
                                        exitNumber,
                                        subfunction,
                                        pb->rxcmd_address,
                                        pb->rxcmd_dll,
                                        pb->rxcmd_command.strptr
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
                            snprintf(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 7: Java RXCMD=3 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object",
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

    #if defined ( RGF_SYSTEM_EXIT_HANDLER )
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

                            snprintf(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 8: Java RXMSQ=4 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object",
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
                            snprintf(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 9: Java RXMSQ=4 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a value (current size of queue)",
                                                DLLNAME, exitNumber, subfunction);
                            context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                        }

                            // an error
                            // TODO: could get Java object's String value and return that ?
                        else  if ( param.env->IsInstanceOf(jresult, defaultJVM->clz_String)==JNI_FALSE )
                        {
                            param.env->DeleteLocalRef(jresult);

                            snprintf(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 10: Java RXMSQ=4 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object",
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
                                snprintf(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 11: Java RXMSQ=4 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object representing a valid number (\"size_t\")",
                                                    DLLNAME, exitNumber, subfunction);
                                context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
                            }
                            else    // conversion went o.k.
                            {
                                pb->rxmsq_size=size;
                            }
                            free(c_result);
                        }
                    }
                }
                break;


                // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            case RXSIO:    // system exit # 5, "standard input and output"
                {

    #if defined ( RGF_SYSTEM_EXIT_HANDLER )
                    fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d]\n",
                                        exitNumber,subfunction);
    #endif

// no post-processing, so just for debugging
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
                    if (subfunction==RXSIOSAY)   // 1
                    {
                    fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d] -> RXSIOSAY\n",
                                        exitNumber,subfunction);
                    }
                    else if (subfunction==RXSIOTRC)   // 2
                    {
    #if defined ( RGF_SYSTEM_EXIT_HANDLER )
                    fprintf(stderr, "---> RXSIO=2, exitNumber: [%d], subfunction: [%d] -> RXSIOTRC\n",
                                        exitNumber,subfunction);
    #endif
                    }
                    else
#endif

                    if (subfunction==RXSIOTRD)   // 3
                    {
    #if defined ( RGF_SYSTEM_EXIT_HANDLER )
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

                            snprintf(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 12: Java RXFNC=2 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object",
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
    #if defined ( RGF_SYSTEM_EXIT_HANDLER )
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

                            snprintf(msg, 1024, "%.16s/internal/rexx_exit_handler_entry(), error 13: Java RXFNC=2 exit handler (exitNumber: [%d], subfunction: [%d]) did not return a Java String object",
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
    #if defined ( RGF_SYSTEM_EXIT_HANDLER )
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
    #if defined ( RGF_SYSTEM_EXIT_HANDLER )
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
        #if defined ( RGF_SYSTEM_EXIT_HANDLER )

                // nothing to setup
            case RXINI:       // system exit # 9, "initialization processing"
                {
                    fprintf(stderr, "<--- RXINI=9, exitNumber: [%d], subfunction: [%d]\n",
                                     exitNumber, subfunction);
                }
                break;
        #endif

                // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        #if defined ( RGF_SYSTEM_EXIT_HANDLER )

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
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
                        fprintf(stderr, "<--- RXNOVAL=13, exitNumber: [%d], subfunction: [%d], RXEXIT_HANDLED\n",
                                          exitNumber, subfunction);
#endif

                        if (subfunction==RXNOVALCALL)   // 1
                        {
                            RXVARNOVALUE_PARM *pb=(RXVARNOVALUE_PARM *) parmBlock;
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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

#if defined ( RGF_SYSTEM_EXIT_HANDLER )
                        fprintf(stderr, "<--- RXEXF=12/RXOFNC=15, exitNumber: [%d], subfunction: [%d], RXEXIT_HANDLED\n",
                                           exitNumber,
                                           subfunction);
#endif

                        if (subfunction==RXOFNCCAL || subfunction==RXEXFCAL)   // 1
                        {
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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

        #if defined ( RGF_SYSTEM_EXIT_HANDLER )
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
            }   // end switch

        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // ================== END:   system exit related special handling, before returning to Rexx

    param.env->DeleteLocalRef(jhandler);
    param.env->DeleteLocalRef(jslot);
    param.env->DeleteLocalRef(jpb);

    RgfAcquireLock();
    environmentDetachFromNew(&param);
    RgfReleaseLock();

// -----------------------------------------
#if defined ( RGF_SYSTEM_EXIT_HANDLER )
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

#if defined ( RGF_SYSTEM_EXIT_HANDLER )
    fprintf(stderr, "*** rexx_exit_handler_entry() 2, exitNumber=[%d], subfunction=[%d], actionCode=[%d] <--- ...\n",
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
#if defined ( RGF_COMMAND_HANDLER ) // || defined ( RGF_COMMAND_HANDLER )
    fprintf(stderr, "*** rexx_command_handler_entry() 1 ---> ...\n");
    fflush(stderr);
#endif

    char *msg=new char[1024];

        // get the RII structure to get access to
    RexxInstance *ri=context->threadContext->instance;
    PSTRUCT_RII struRii=RgfGetRexxInterpreterInstanceStructFromList(ri);
    if (struRii==NULL || struRii->rajo==NULL || struRii->rexxconf==NULL) {
        snprintf(msg, 1024, "%.16s/internal/rexx_command_handler_entry(), error 1: panic - cannot find Rexx interpreter instance related Java interface objects, address=[%s], command=[%s] for RexxInterpreterInstance=[%p]",
                            DLLNAME, context->CString(address), context->CString(command), ri );

        context->RaiseException1(Rexx_Error_Incorrect_call_user_defined, context->String(msg));
        delete[] msg;
        return NULL;
    }

    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={0, 0, (void *)context->threadContext->instance, false, 0};
    RgfAcquireLock();
    environmentAttachToNew(&param);     // attach to Java

    if (param.error== -1)    // could not attach to Java
    {
        RgfReleaseLock();
        snprintf(msg, 1024, "%.16s/internal/rexx_command_handler_entry(), error 2: panic - cannot attach to Java, address=[%s], command=[%s] for RexxInterpreterInstance=[%p]",
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
        snprintf(msg, 1024, "%%.16s/internal/rexx_command_handler_entry(), error 3: Java exception while attempting to fetch the Java command handler for command name/address=[%s] - [%%s]",
                            context->CString(address));

        RexxStringObject rso=RgfCreateRexxSyntaxConditionWithJavaThrowable (&param,
             0,         // RexxExitContext
             context,
             msg);

        param.env->DeleteLocalRef(jaddress);
        param.env->DeleteLocalRef(jhandler);

        RgfAcquireLock();
        environmentDetachFromNew(&param);
        RgfReleaseLock();
        delete[] msg;
        return NULL;
    }

        // check whether jhandler==NULL, if so raise appropriate RexxCondtion
    if (jhandler==NULL) {
        param.env->DeleteLocalRef(jaddress);

        RgfAcquireLock();
        environmentDetachFromNew(&param);
        RgfReleaseLock();

        snprintf(msg, 1024, "%.16s/internal/rexx_command_handler_entry(), error 4: cannot find Java command handler (returned \"NULL\") for command name/address=[%s]",
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
        environmentDetachFromNew(&param);
        RgfReleaseLock();

        snprintf(msg, 1024, "%.16s/internal/rexx_command_handler_entry(), error 5: Java exception, cannot find the Java Rexx exit handler method named [%s], needed for processing the command addressed to: [%s]",
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

        // check for Java exception
    if (param.env->ExceptionCheck())
    {
            // raise Rexx condition with Java Throwable
        snprintf(msg, 1024, "%%.16s/internal/rexx_command_handler_entry(), error 6: Java exception while running the Java command handler for command name/address=[%s], command=[%s] - [%%s]",
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
        environmentDetachFromNew(&param);
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
        environmentDetachFromNew(&param);
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
    environmentDetachFromNew(&param);
    RgfReleaseLock();

#if defined ( RGF_COMMAND_HANDLER ) // || defined ( RGF_COMMAND_HANDLER )
    fprintf(stderr, "*** rexx_command_handler_entry() - TheEnd - , address=[%s], command=[%s] <--- ...\n",
                            context->CString(address),
                            context->CString(command)
            );
    fflush(stderr);
#endif

    delete[] msg;
    return res;
}



// ----------------------------------------------------------------------------------------------
    // 2012-02-08, rgf: call backs for exit and command handlers
        // 2012-02-08, rgf: utility inline functions
inline void * rgfUnwrapPointer (JNIEnv *env, jstring j_encoded_pointer)
{
    char *c_pointer=(char *) JNU_GetStringNativeChars(env, j_encoded_pointer);  // convert to native string
    void *raw_pointer=NULL;

#if defined(DEBUG_JNI)
    fprintf(stderr, "... rgfUnwrapPointer: using c_context=[%.256s]\n", c_pointer);
    fflush(stderr);
#endif

    RgfString2Pointer(c_pointer, raw_pointer);  // get pointer from string
    free(c_pointer);
    return raw_pointer;
}



// ----------------------------------------------------------------------------------------------
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



// ----------------------------------------------------------------------------------------------
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



// ----------------------------------------------------------------------------------------------
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



// ----------------------------------------------------------------------------------------------
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


// ----------------------------------------------------------------------------------------------
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



// ----------------------------------------------------------------------------------------------
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

    free(c_variableName);

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



// ----------------------------------------------------------------------------------------------
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

    free(c_variableName);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> --> rop_value=[%s]\n", context->ObjectToStringValue(rop_value));

    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniSetContextVariable <== <==\n");
#endif
}



// ----------------------------------------------------------------------------------------------
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



// ----------------------------------------------------------------------------------------------
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



// ----------------------------------------------------------------------------------------------
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

    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={env, rajo, 0, false, 0};
    jobject jobj=RgfRexxObject2JavaObject(&param,context->threadContext,rop);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 3/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetConditionInfo <== <==\n");
#endif

    return jobj;    // return Rexx condition object as a (Java) RexxProxy
}




// ----------------------------------------------------------------------------------------------
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


// ----------------------------------------------------------------------------------------------
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




// ----------------------------------------------------------------------------------------------
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
    free(c_variableName);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniDropContextVariable <== <==\n");
#endif

}



// ----------------------------------------------------------------------------------------------
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
    free(c_variableName);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniSetContextVariableToNil <== <==\n");
#endif

}



// ----------------------------------------------------------------------------------------------
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

    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={env, rajo, 0, false, 0};

    jobject jobj=RgfRexxObject2JavaObject(&param,context->threadContext,rop);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetCallerContext <== <==\n");fflush(stderr);
#endif
    return jobj;
}



// ----------------------------------------------------------------------------------------------
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

    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={env, rajo, 0, false, 0};
    jobject jobj=RgfRexxObject2JavaObject(&param,context->threadContext,rop);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetAllContextVariables <== <==\n");fflush(stderr);
#endif
    return jobj;
}



// ----------------------------------------------------------------------------------------------
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

    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={env, rajo, 0, false, 0};
    jobject jobj=RgfRexxObject2JavaObject(&param,context->threadContext,rop);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetLocalEnvironment <== <==\n");fflush(stderr);
#endif
    return jobj;
}



// ----------------------------------------------------------------------------------------------
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

    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={env, rajo, 0, false, 0};
    jobject jobj=RgfRexxObject2JavaObject(&param,context->threadContext,rop);

#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetGlobalEnvironment <== <==\n");fflush(stderr);
#endif
    return jobj;
}



// ----------------------------------------------------------------------------------------------
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

    // STRUCT_ATTACH_PARAM param={env,rajo,riid,bDetach,error};
    STRUCT_ATTACH_PARAM param={env, rajo, 0, false, 0};
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



// ----------------------------------------------------------------------------------------------
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



// ----------------------------------------------------------------------------------------------
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
    fprintf(stderr, "--> invocation type=[%ld], c_EnvName=[%s], envBuffer=[%s], c_NewValue=[%s] <--\n",
                       (long int) j_InvocationType, c_EnvName, envBuffer, c_NewValue);
    fflush(stderr);
#endif

        // make sure we free memory
    free(c_EnvName);

    if (c_NewValue!=NULL)
    {
        free(c_NewValue);
    }


#if defined (DEBUG_JNI)
    fprintf(stderr, "==> ==> 2/ Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniProcEnvironment <== <==\n");fflush(stderr);
#endif

    // create Java string from the C string and return it
#ifdef UNIX
    return ( envBuffer==NULL ? NULL : env->NewStringUTF( envBuffer ) );
#else   // WINDOWS
    return ( envBuffer==NULL || envBuffer[0]==NULL ? NULL : env->NewStringUTF( envBuffer ) );
#endif
}



// ----------------------------------------------------------------------------------------------
// ------ 2015-05-09, rgf

/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniGetLocalEnvironment0
 * Signature: (Ljava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetLocalEnvironment0
  (JNIEnv *env, jobject raj, jstring  j_rii_ID)
{
    thread_id_t tid=RgfGetTID();   // get current TID

#if defined(DEBUG_JNI) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "--> arrived: ..._jniGetLocalEnvironment(), tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], defaultJVM->clz_Object=[%p]\n",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, defaultJVM->clz_Object);
    fprintf(stderr, "\t\tenv=[%p], raj=[%p], j_rii_ID=[%p]\n", env, raj, j_rii_ID);
    fflush(stderr);
#endif

        // get interpreter instance from argument
    char         *c_rii_ID=NULL;
    RexxInstance *instance=NULL;
    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

#if defined(DEBUG_JNI)
    fprintf(stderr, "                                        using c_rii_ID=[%.256s]\n", c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    free(c_rii_ID);

    RexxThreadContext   *rtc=NULL;
    RexxObjectPtr result_obj;           // Rexx object returned from Rexx, NULL[OBJECT], if none

    RexxInstance *tmpInstance=NULL;
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
        char *msg=new char[1024];
        snprintf( msg, 1024, "%.16s/routine/jniGetLocalEnvironment(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
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
        snprintf( msg, 1024, "%.16s/routine/jniGetLocalEnvironment(), error 2: could not AttachThread() to Rexx interpreter instance with the ID '%p'", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
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
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        rtc->DetachThread();
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: ..._jniGetLocalEnvironment(): tid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",
                       (unsigned long) tid, msg);
    fflush(stderr);
#endif

        free(msg);
        return NULL;
    }


#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning from: .._jniRexxRunProgram(...), tid=[%lu], normal execution\n", (unsigned long) tid);
    fflush(stderr);
#endif

    jobject jresult=RgfProcessReturnValue4Java(env, raj, rtc, result_obj, NULL);

    rtc->DetachThread();

    return jresult;
}



// ----------------------------------------------------------------------------------------------
/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniGetGlobalEnvironment0
 * Signature: (Ljava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniGetGlobalEnvironment0
  (JNIEnv *env, jobject raj, jstring  j_rii_ID)
{
    thread_id_t tid=RgfGetTID();   // get current TID

#if defined(DEBUG_JNI) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "--> arrived: ..._jniGetGlobalEnvironment0(), tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], defaultJVM->clz_Object=[%p]\n",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, defaultJVM->clz_Object);
    fprintf(stderr, "\t\tenv=[%p], raj=[%p], j_rii_ID=[%p]\n", env, raj, j_rii_ID);
    fflush(stderr);
#endif

        // get interpreter instance from argument
    char         *c_rii_ID=NULL;
    RexxInstance *instance=NULL;
    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

#if defined(DEBUG_JNI)
    fprintf(stderr, "                                        using c_rii_ID=[%.256s]\n", c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    free(c_rii_ID);

    RexxThreadContext   *rtc=NULL;
    RexxObjectPtr result_obj;           // Rexx object returned from Rexx, NULL[OBJECT], if none

    RexxInstance *tmpInstance=NULL;
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
        char *msg=new char[1024];
        snprintf( msg, 1024, "%.16s/routine/jniGetGlobalEnvironment0(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
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
        snprintf( msg, 1024, "%.16s/routine/jniGetGlobalEnvironment0(), error 2: could not AttachThread() to Rexx interpreter instance with the ID '%p'", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
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
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        rtc->DetachThread();
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: ..._jniGetGlobalEnvironment0(): tid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",
                       (unsigned long) tid, msg);
    fflush(stderr);
#endif

        free(msg);
        return NULL;
    }

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning from: .._jniRexxRunProgram(...), tid=[%lu], normal execution\n", (unsigned long) tid);
    fflush(stderr);
#endif

    jobject jresult=RgfProcessReturnValue4Java(env, raj, rtc, result_obj, NULL);

    rtc->DetachThread();

    return jresult;

}



// ----------------------------------------------------------------------------------------------
/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniNil0
 * Signature: (Ljava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniNil0
  (JNIEnv *env, jobject raj, jstring  j_rii_ID)
{
    thread_id_t tid=RgfGetTID();   // get current TID

#if defined(DEBUG_JNI) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "--> arrived: ..._jniNil0(), tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], defaultJVM->clz_Object=[%p]\n",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, defaultJVM->clz_Object);
    fprintf(stderr, "\t\tenv=[%p], raj=[%p], j_rii_ID=[%p]\n", env, raj, j_rii_ID);
    fflush(stderr);
#endif

        // get interpreter instance from argument
    char         *c_rii_ID=NULL;
    RexxInstance *instance=NULL;
    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

#if defined(DEBUG_JNI)
    fprintf(stderr, "                                        using c_rii_ID=[%.256s]\n", c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    free(c_rii_ID);

    RexxThreadContext   *rtc=NULL;
    RexxObjectPtr result_obj;           // Rexx object returned from Rexx, NULL[OBJECT], if none

    RexxInstance *tmpInstance=NULL;
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
        char *msg=new char[1024];
        snprintf( msg, 1024, "%.16s/routine/jniNil0(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
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
        snprintf( msg, 1024, "%.16s/routine/jniNil0(), error 2: could not AttachThread() to Rexx interpreter instance with the ID '%p'", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
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
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        rtc->DetachThread();
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: ..._jniNil0(): tid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",
                       (unsigned long) tid, msg);
    fflush(stderr);
#endif

        free(msg);
        return NULL;
    }

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning from: .._jniRexxRunProgram(...), tid=[%lu], normal execution\n", (unsigned long) tid);
    fflush(stderr);
#endif

        // make sure that we explicitly return a RexxProxy with .nil embedded!
    jobject jresult=RgfCreateRexxProxy(env, raj, rtc, result_obj, NULL, NULL);
    rtc->DetachThread();

    return jresult;
}



// ----------------------------------------------------------------------------------------------
/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniInterpreterVersion0
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniInterpreterVersion0
  (JNIEnv *env, jobject raj, jstring  j_rii_ID)
{
    thread_id_t tid=RgfGetTID();   // get current TID

#if defined(DEBUG_JNI) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "--> arrived: ...__jniInterpreterVersion0(), tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], defaultJVM->clz_Object=[%p]\n",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, defaultJVM->clz_Object);
    fprintf(stderr, "\t\tenv=[%p], raj=[%p], j_rii_ID=[%p]\n", env, raj, j_rii_ID);
    fflush(stderr);
#endif

        // get interpreter instance from argument
    char         *c_rii_ID=NULL;
    RexxInstance *instance=NULL;
    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

#if defined(DEBUG_JNI)
    fprintf(stderr, "                                        using c_rii_ID=[%.256s]\n", c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    free(c_rii_ID);

    RexxThreadContext   *rtc=NULL;

    RexxInstance *tmpInstance=NULL;
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
        char *msg=new char[1024];
        snprintf( msg, 1024, "%.16s/routine/_jniInterpreterVersion0(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: ...__jniInterpreterVersion0(): tid=[%lu], error, cannot retrieve c_rii_ID=p=[%p], received=tmpInstance=[%p] instead\n",
                    (unsigned long) tid, instance, tmpInstance);
    fflush(stderr);
#endif

        return 0; // return NULL;
    }

    // no need to do a rtc->ReleaseGlobalReference(ro) as these references get released upon rtc->DetachThread() !
    if (!instance->AttachThread(&rtc))          // get a ThreadContext
    {
        char *msg=new char[1024];
        snprintf( msg, 1024, "%.16s/routine/_jniInterpreterVersion0(), error 2: could not AttachThread() to Rexx interpreter instance with the ID '%p'", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: ...__jniInterpreterVersion0(): tid=[%lu], error, cannot do an instance->AttachThread()!\n",
                          (unsigned long) tid);
    fflush(stderr);
#endif

        return 0; // return NULL;
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
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        rtc->DetachThread();
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: ...__jniInterpreterVersion0(): tid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",
                       (unsigned long) tid, msg);
    fflush(stderr);
#endif

        free(msg);
        return 0; // return NULL;
    }


#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning from: ..__jniInterpreterVersion0(), tid=[%lu], normal execution\n", (unsigned long) tid);
    fflush(stderr);
#endif

    rtc->DetachThread();

    return res;
}



// ----------------------------------------------------------------------------------------------
/*
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniLanguageLevel0
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniLanguageLevel0
  (JNIEnv *env, jobject raj, jstring  j_rii_ID)
{
    thread_id_t tid=RgfGetTID();   // get current TID

#if defined(DEBUG_JNI) || defined (DEBUG_JNI_ENTRY)
    fprintf(stderr, "--> arrived: ...__jniLanguageVersion0(), tid=[%lu], currentJVM=[%p], defaultJVM->jvm=[%p], defaultJVM->clz_Object=[%p]\n",
                    (unsigned long) tid, currentJVM, defaultJVM->jvm, defaultJVM->clz_Object);
    fprintf(stderr, "\t\tenv=[%p], raj=[%p], j_rii_ID=[%p]\n", env, raj, j_rii_ID);
    fflush(stderr);
#endif

        // get interpreter instance from argument
    char         *c_rii_ID=NULL;
    RexxInstance *instance=NULL;
    c_rii_ID=(char *) JNU_GetStringNativeChars(env, j_rii_ID);  // convert to native string

#if defined(DEBUG_JNI)
    fprintf(stderr, "                                        using c_rii_ID=[%.256s]\n", c_rii_ID);
    fflush(stderr);
#endif

    RgfString2Pointer(c_rii_ID, instance);  // get pointer from string
    free(c_rii_ID);

    RexxThreadContext   *rtc=NULL;

    RexxInstance *tmpInstance=NULL;
    tmpInstance=RgfGetRexxInterpreterInstanceFromList(instance);    // try to get given Rexx Interpreter instance

    if (instance != tmpInstance)    // error (do not halt another Rexx Interpreter instance!)
    {
        char *msg=new char[1024];
        snprintf( msg, 1024, "%.16s/routine/_jniLanguageVersion0(), error 1: Rexx interpreter instance with the ID '%p' could not be found", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_INVALID_ARGUMENT,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: ...__jniLanguageVersion0(): tid=[%lu], error, cannot retrieve c_rii_ID=p=[%p], received=tmpInstance=[%p] instead\n",
                    (unsigned long) tid, instance, tmpInstance);
    fflush(stderr);
#endif

        return 0; // return NULL;
    }

    // no need to do a rtc->ReleaseGlobalReference(ro) as these references get released upon rtc->DetachThread() !
    if (!instance->AttachThread(&rtc))          // get a ThreadContext
    {
        char *msg=new char[1024];
        snprintf( msg, 1024, "%.16s/routine/_jniLanguageVersion0(), error 2: could not AttachThread() to Rexx interpreter instance with the ID '%p'", DLLNAME, instance);

        jthrowable j_rexxException=RgfCreateRexxException4Java(env, raj,
                                                             NULL, // rtc,
                                                             NULL, // conditionObject
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        delete[] msg;
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: ...__jniLanguageVersion0(): tid=[%lu], error, cannot do an instance->AttachThread()!\n",
                          (unsigned long) tid);
    fflush(stderr);
#endif

        return 0; // return NULL;
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
                                                             REASON_EXECUTION_ERROR,   // reason
                                                             msg   // message
                                                        );
        rtc->DetachThread();
        env->Throw(j_rexxException);  // throw the exception in the JVM

#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning: ...__jniLanguageVersion0(): tid=[%lu], Rexx runtime error occurred, throwing Java exception!\n\tmsg=[%.256s]\n",
                       (unsigned long) tid, msg);
    fflush(stderr);
#endif

        free(msg);
        return 0; // return NULL;
    }


#if defined(DEBUG_JNI)
    fprintf(stderr, "<-- returning from: .._jniLanguageVersion0(), tid=[%lu], normal execution\n", (unsigned long) tid);
    fflush(stderr);
#endif

    rtc->DetachThread();

    return res;
}


// ----------------------------------------------------------------------------------------------
/*  -- rgf, 2018-02-26: just for testing/debugging calls from Java to native code
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniTestPing
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniTestPing
  (JNIEnv *, jobject)
{
    return;
}



// ----------------------------------------------------------------------------------------------
/* --rgf, 2022-08-06: allow for running simple Rexx scripts from Java using a separate RII,
 *                    optionally with a single string argument; will return the result as
 *                    a string only; if a Rexx Condition got raised the Rexx like error
 *                    string gets returned, led-in by "BSF4ooRexx/jniExecuteSimpleProcedureOnOwnRII()"
 *
 *
 *
 * Class:     org_rexxla_bsf_engines_rexx_RexxAndJava
 * Method:    jniExecuteSimpleProcedureOnOwnRII
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_rexxla_bsf_engines_rexx_RexxAndJava_jniExecuteSimpleProcedureOnOwnRII
  (JNIEnv *env, jclass clzRAJ, jstring jrexxCode, jstring jrexxArgument)
{
    jstring jres = NULL;
    {
        RexxInstance      *instance;
        RexxThreadContext *rtc;
        int bSuccess=RexxCreateInterpreter(&instance, &rtc, NULL);
        if (bSuccess)
        {
            if (jrexxCode!=NULL)
            {
                char *rexxCode=(char *) JNU_GetStringNativeChars(env, jrexxCode);  // convert to native string
// fprintf(stderr, "BSF4ooRexx.cc: Line # %d %s, \n\trexxCode=[%s]\n", __LINE__, __FUNCTION__, rexxCode);fflush(stderr);
                RexxRoutineObject rro    = rtc->NewRoutine("BSF4ooRexx.jniExecuteSimpleProcedureOnOwnRII", rexxCode, strlen(rexxCode));

                if (rtc->CheckCondition())      // a Rexx condition raised?
                {
                    RexxDirectoryObject condObj=rtc->GetConditionInfo();
                    char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "/jniExecuteSimpleProcedureOnOwnRII().NewRoutine(), error 1");
                    jres = env->NewStringUTF( msg ) ;
                    rtc->ClearCondition();
// fprintf(stderr, "BSF4ooRexx.cc: Line # %d %s, \n\tterminating instance\n", __LINE__, __FUNCTION__ );fflush(stderr);
                    instance->Terminate();
                    free(msg);
                    free(rexxCode);
                    return jres;
                }
                free(rexxCode);

                // invoke routine
                // rexxArgument
                RexxArrayObject arrArg=rtc->NewArray(0);
                if (jrexxArgument!=NULL)
                {
                    char *rexxArgument=(char *) JNU_GetStringNativeChars(env, jrexxArgument);  // convert to native string
// fprintf(stderr, "BSF4ooRexx.cc: Line # %d %s, \n\trexxArgument=[%s]\n", __LINE__, __FUNCTION__, rexxArgument);fflush(stderr);

                    rtc->ArrayAppend(arrArg, rtc->CString(rexxArgument));
                    free(rexxArgument);
                }

                RexxObjectPtr result_obj=rtc->CallRoutine(rro, arrArg);   // call the routine
// fprintf(stderr, "BSF4ooRexx.cc: Line # %d %s, \n\tresult_obj: [%p]\n", __LINE__, __FUNCTION__, result_obj);fflush(stderr);

                if (rtc->CheckCondition())      // a Rexx condition raised?
                {
                    RexxDirectoryObject condObj=rtc->GetConditionInfo();
                    char * msg=RgfCreateRexxlikeErrorInfo (rtc, condObj, "/jniExecuteSimpleProcedureOnOwnRII().CallRoutine(), error 2");
                    jres = env->NewStringUTF( msg ) ;
                    rtc->ClearCondition();
// fprintf(stderr, "BSF4ooRexx.cc: Line # %d %s, \n\tterminating instance\n", __LINE__, __FUNCTION__ );fflush(stderr);
                    instance->Terminate();
                    free(msg);
                    return jres;
                }

                if (result_obj != NULL)
                {
                    jres = env->NewStringUTF( rtc->CString(result_obj) ) ;
// fprintf(stderr, "BSF4ooRexx.cc: Line # %d %s, \n\tresult to return: [%s]\n", __LINE__, __FUNCTION__, rtc->CString(result_obj) );fflush(stderr);
                }
            }
// fprintf(stderr, "BSF4ooRexx.cc: Line # %d %s, \n\tterminating instance\n", __LINE__, __FUNCTION__ );fflush(stderr);
            instance->Terminate();
        }
    }
    return jres;
}


#ifdef __cplusplus
}               // closing bracket
#endif


