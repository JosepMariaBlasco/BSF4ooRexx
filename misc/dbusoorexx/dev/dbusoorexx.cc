/** This is the ooRexx native library for interfacing with the DBus message infrastructure that
 *  has been developed for Linux and has been employed by many Linux distributions. As ports to
 *  Windows and MacOSX of DBus exist, those platforms are supported as well.
 *
 *  This library needs 'dbus.cls' which is tightly integrated as classes and methods of that ooRexx
 *  package are being used from the native code.
 *
 *  The following scenarios are supported:
 *
 * <ul>
 * <li> DBus interaction with DBus services. In this scenario first a connection to the DBus
 *      has to be established (e.g. 'session', 'system', or any other 'private address'), which
 *      kicks off a proper message loop (c.f. the RexxMethod DbusMessageLoop(...), which
 *      fulfills the libdbus requirement that messages get received and sent within the same
 *      thread. Code in the 'dbus.cls' package makes sure that any message to be sent via DBus
 *      will be dispatched within the message loop thread.
 *
 * <li> ooRexx DBus singal listeners and ooRexx service objects. In this scenario any messages
 *      directed to ooRexx signal listeners and ooRexx service objects get dispatched via the
 *      message loop for the connection which is used to filter signals or which was used to
 *      register the DBus service.
 *
 * <li> Private DBus servers. In this scenario an ooRexx program running on any operating system
 *      can serve any DBus clients from any other operating systems. In this scenario for each
 *      ooRexx private DBus server a watch loop is created in its own thread that dispatches
 *      client connections, whenever a private DBus client tries to establish a connection. For
 *      each new connection a proper message loop is established to receive and send DBus messages.
 *      It is possible to add ooRexx DBus service objects and add ooRexx listener objects on that
 *      connection as is the case in the first two scenarios above.
 *
 * </ul>
 *
 *  @author Rony G. Flatscher
 *  @version 200.20150418
 *
 */

/*
    author:     Rony G. Flatscher (c) 2011
    date:       2011-07-17 (2011-06-11, 2011-07-17)
    purpose:    DBUS language binding for ooRexx
    needs:      - a <http://www.freedesktop.org/wiki/Software/dbus>-compliant dbus infrastructure
                - matching Rexx package "dbus.cls"

    remarks:    - if a 'v' (variant) is passed, then
                  - only the string value will be marshalled (as if 's' was given instead)
                  - if a Rexx array, then it will be marshalled 'a*s' where '*' stands for the dimensions
                  - if a 'v' needs to be encoded with a specific signature use "dbus.box(signature,args)"

                - IsString(obj) does not yield necessarily the same as IsOfType(obj,"STRING"), hence
                  using the latter: RMG informed on the developer list on 2011-07-12, that IsString(obj)
                  will not return .true for "hidden integers" and "NumberString" objects; reason being,
                  that the String*()-APIs are only safe to use if IsString() yields .true; on 2011-07-13
                  RMG assured that ObjectToString(obj) will always return a RexxStringObject for which the
                  String*()-APIs are safe to use.

    changed:    2011-07-25, rgf: - changed handling errors if array or dict were left out
                2011-08-03, rgf: - handle "ERRORREPLY" condition in message loop: will reply an error
                                 - added ability to force stopping all Rexx threads HALT_ALL_REXX_THREADS()
                2011-08-04, rgf: - added statistics support
                2011-08-07, rgf: - finalized private/standalone dbus ability
                2011-08-17, rgf: - rename variable 'interface' to 'interfaceName' as the MSCPP-compiler reserves 'interface'
                2011-08-18, rgf: - send empty reply messages by default, unless DBus' attribute 'sendEmptyReply' is not .true
                2011-08-21, rgf: - if UNIX_FD is not defined (e.g. on Windows), then the respective un/marshalling
                                   code will not be compiled
                                 - adjust Windows version: 32-bit MS compiler abends at runtime if "%zu" is used in fprintf/_snprintf
                                   - changed all debug fprintf's from "%zu" to "%lu" to not cause exceptions when debugging on Windows with MSC
                2011-08-22, rgf: - add ability to check whether type code is supported on the connection
                                 - make the message, server and timeout loop aware of Rexx conditions; if a condition is active, leave the loop
                                   setting the loop control variable to .false to reflect this fact in ooRexx code as well
                2011-08-25, rgf: - add entry "CONNECTION" to slotDir; this allows service and listener objects
                                   to distinguish the connections they got registered with
                2014-07-23, rgf: - change strategies to cater for the information, that libdbus is
                                   not truly multi-threading safe; each connection needs to be used on
                                   its own thread for any messages to be sent over it, otherwise runtime
                                   errors (e.g. no results delivered from messages, DBus inable to fetch replies etc.)
                                   may occur
                2014-07-27, rgf: - marshalling and unmarshalling of dictionaries with multiple entries fixed
                2014-07-31, rgf: - added routine DbusGetTID() to ease debugging
                2014-08-05, rgf: - adding ReleaseLocalReference() where methods/routines may be running forever,ie.
                                   in the various message/server loops and the functions that may be invoked from
                                   within (ooRexx releases local references when returning from native
                                   Rexx methods/routines); added a missing dbus_server_ref()-call (caused abend
                                   on Windows)
                                 - ooRexx 4.2.0 now required (because of using .context~name in 'dbus.cls' package
                2014-08-07, rgf: - tidied up the source
         		2014-08-11, rgf: - adding doclet-kind documentation
                2014-10-20, rgf: - changed version to 2.00, 2014-10-20, ie. "200.20141020"
                2015-04-18, rgf: - updating version for official release


    possible todos: - add globals for Nil(), True(), Flase(), .Array, .DateTime, .DBus, "1", "", as well
                      as index name in dateTime directory entries
                    - change attribute name CSELF (probably misleading as it is not the same as CSELF in an
                      ooRexx native method or routine argument type); if so, don't forget dbus.cls

    version:    200.20150418
    license:      Apache License 2.0

------------------------ Apache Version 2.0 license -------------------------
   Copyright 2011-2014 Rony G. Flatscher

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


#ifdef __cplusplus
extern "C" {            // make sure no C++ name mangling comes into the way
#endif

/*
#ifdef _MSC_VER
   #pragma comment(lib, "dbus-1.lib")  // as per the hint of Pontus Carlsson in an e-mail from 2011-08-19
#endif
*/

#define DBUS_API_SUBJECT_TO_CHANGE
#include <dbus/dbus.h>

#ifndef DBUS_VERSION_STRING     // on Windows an older version of dbus has not defined it yet
    #define DBUS_VERSION_STRING  "<n/a>"
#endif

#ifndef  DBUS_TYPE_UNIX_FD      // e.g. not defined for Windows, still incorporate it
    #define DBUS_TYPE_UNIX_FD ((int) 'h')
#endif


#include <stdio.h>
#include <stdlib.h>

    // rgf, 2011-06-11
#include "oorexxapi.h"


#include <string.h>     // will allow access to memset() et.al.

#ifdef _MSC_VER
# pragma warning(disable:4100)
          // MS does not define a C99 "snprintf()" function
    #define SNPRINTF _snprintf
#else
    #define SNPRINTF snprintf
#endif



#ifdef DBUSOOREXX_32
    #define DBUS_REXXVERSION    "200.20150418 32-bit"  // version: "MajorNumber"."YYYYMMDD"
#elif defined (DBUSOOREXX_64)
    #define DBUS_REXXVERSION    "200.20150418 64-bit"  // version: "MajorNumber"."YYYYMMDD"
#else
    #define DBUS_REXXVERSION    "200.20150418 n/a-bit"  // version: "MajorNumber"."YYYYMMDD"
#endif

#ifndef TID
    #define TID thread_id_t
#endif

#define DLLNAME                 "dbusoorexx"


// =================== debug defines ...
#define REXX_LOADER             // needed for dbus_threads_init_default(), cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusThreads.html#ga33b6cf3b8f1e41bad5508f84758818a7>


// TODO: 2014-07, rgf: remove auto-disconnect-code!
    // if defined, then a "Disconnected" signal on a private server's client connection will
    // cause a "disconnect" message to be sent to the private server automatically
    // as of 2011-09-12 (DBus 1.5.7) the "Disconnected" signal is not sent on private server connections;
    // to test stalled connections the Rexx code checks for that and disconnects, if the connection is not open anymore,
    // hence the following def is not really needed
// #define REXX_AUTO_DISCONNECT_FROM_PRIVATE_SERVER


// #define  DEBUG_OOREXX_FIND_CLASS    // attempt to help RMG to debug FindClass() finding class that is not in .local or .environment
// #define  DEBUG_HIT_ENTER_TO_CONTINUE // will stop and wait for a CR using fscanf()

// #define DEBUG_RGF           // for temporary debug output
// #define DEBUG_MESSAGELOOP
// #define DEBUG_CONNECTION

/*
#define DEBUG_VARIANT
#define DEBUG_VARIANT_2
#define DEBUG_ARRAY
#define DEBUG_STRUCT
#define DEBUG_DICT
#define DEBUG_DICT_2

#define DEBUG_MARSHAL
#define DEBUG_MARSHAL_2
#define DEBUG_UNMARSHAL

#define DEBUG_STRUCT_MARSHAL_PRINT
*/


#define DEBUG_TIMEOUTLOOP

/*
    // for debugging private server
#define DEBUG_PRIVATE_SERVER
#define DEBUG_WATCHLOOP
#define DEBUG_TIMEOUTLOOP
*/


/*
// #define REXX_UNLOADER
// #define DEBUG_REXX_HALT      // 2011-08-03


// #define DEBUG_STRUCT_MARSHAL_PRINT

#define REXX_LOADER_UNLOADER
#define DEBUG_LOADER_UNLOADER

#define DEBUG_ARRAY

#define DEBUG_MARSHAL
#define DEBUG_MARSHAL_2
#define DEBUG_UNMARSHAL

#define DEBUG_STRUCT
#define DEBUG_DICT
// #define DEBUG_DICT_2
#define DEBUG_VARIANT
#define DEBUG_VARIANT_2
#define DEBUG_MESSAGELOOP
#define DEBUG_FUNCTIONS
#define DEBUG_METHODS

#define DEBUG_WATCHLOOP
#define DEBUG_TIMEOUTLOOP
#define DEBUG_PRIVATE_SERVER

#define DEBUG_CONNECTION        // 2014-08-05, rgf: debug whether connection has a value or not

*/


// if commented, then the dbus malloc routines are used
#define MALLOC_USE_REXX     // 2011-06-26: originally defined to trace down a segmentation fault using dbus' malloc, test using Rexx allocations


const size_t RGF_TID_STRING_WIDTH    = 80;  // allows for hex representation of 2**128 plus two bytes (one for \0, the other for evening)

    // structure to ease argument passing while marshalling the Rexx arguments for DBUS
typedef struct _STRUCT_MARSHAL {
    RexxThreadContext   *context;       // allow interaction with ooRexx

    char                *signature;     // pointer to signature
    size_t               signatureLength;

    char                *cursor;        // cursor to signature's current type code
    char                *elementStart;  // start of current element in signature (if array, points to array's type)
    char                *elementEnd;    // end of current element in signature (if array, points to array's type)

    char                *arrStart;      // start of current array element in signature
    char                *arrEnd;        // end of current array element in signature (same as arrStart, if single array)
    size_t              dimensions;     // dimensionality of the array to process

    char                *errorPosInSignature;   // points to the element in the signature, that caused an error
    logical_t           dbusError;          // set, if a dbus method caused an error
    logical_t           rexxError;          // set, if a Rexx exception has been raised

    char                *msgChunk256;

    RexxClassObject      arrayClass;    // for unmarshalling convenience
    logical_t            is_ay;         // special handling of byte array for Rexx active ?,
                                        //   unmarshalling: "ay" -> "s"
    logical_t            unmarshal_ay_as_string;    // determined by connection attribute: true ay->string, ay->ay else
    logical_t            marshal_ay_as_string; // determined in marshal(...)'s array branch

    logical_t            is_dict;       // special handling of byte array for Rexx active ?,
                                        //   marshalling:   .directory->"a{sY}"
                                        //   unmarshalling: "a{xY}" -> .directory object !

}  STRUCT_MARSHAL;

typedef STRUCT_MARSHAL *PSTRUCT_MARSHAL;            // pointer to structure
long size_STRUCT_MARSHAL=sizeof(STRUCT_MARSHAL);    // get and memorize size of structure


#if (defined (DEBUG_MARSHAL) || defined (DEBUG_UNMARSHAL)) && defined (DEBUG_STRUCT_MARSHAL_PRINT)   // rgf, 20140726
void dump_struct_marshal(PSTRUCT_MARSHAL pm)
{
    fprintf(stderr, " ---prr---> PSTRUCT_MARSHAL() --->: RTC=[%p],signature=[%p], cursor=[%p], elStart=[%p], elEnd=[%p], arrStart=[%p], arrEnd=[%p], dimensions=[%lu] ",
                     pm->context,
                     pm->signature,
            pm->context,
            pm->elementStart,
            pm->elementEnd,
            pm->arrStart,
            pm->arrEnd,
            pm->dimensions
                     );
    fflush(stderr);

    fprintf(stderr, "\n\t---prr---> signature=[%s]/length=[%lu] ",
                     pm->signature, pm->signatureLength
                     );
    fflush(stderr);

    fprintf(stderr, "cursor=[%c]/pos=[%d] ",
                     *pm->cursor,

                     (int)(pm->cursor - pm->signature)
                     );
    fflush(stderr);

    fprintf(stderr, "arrStart=[%c]/pos=[%ld] ",
                    pm->arrStart==NULL ? '\0' : *pm->arrStart,
                    pm->arrStart==NULL ? -1 : (pm->arrStart - pm->signature));
    fflush(stderr);

    fprintf(stderr, "arrEnd=[%c]/pos=[%ld] ",
                    pm->arrEnd==NULL ? '\0' : *pm->arrEnd,
                    pm->arrEnd==NULL ? -1 : (pm->arrEnd - pm->signature));
    fflush(stderr);

    fprintf(stderr, "elStart=[%c]/pos=[%d] ",
                    pm->elementStart==NULL ? '\0' : *pm->elementStart,
                    pm->elementStart==NULL ? -1 : (int)(pm->elementStart - pm->signature) );
    fflush(stderr);

    fprintf(stderr, "elEnd=[%c]/pos=[%d] ",
                    pm->elementEnd==NULL ? '\0' : *pm->elementEnd,
                    pm->elementEnd==NULL ? -1 : (int)(pm->elementEnd - pm->signature));
    fflush(stderr);


    fprintf(stderr, "\n\t---prr---> is_ay=[%d], is_dict=[%d], marsh_ay_as_s=[%d], unmarsh_ay_as_s=[%d]\n",
                     (int) pm->is_ay, (int) pm->is_dict,
                     (int) pm->marshal_ay_as_string, (int) pm->unmarshal_ay_as_string
                     );
    fflush(stderr);
}
#endif


    // needed for the dbus callback function: enables us to get in touch with the Rexx interpreter
    // ------------------------------------------------------------------------------------------
    // define structure for timeout (use a structure, in case sometimes in the future more information to keep becomes necessary)
typedef struct _STRUCT_TIMEOUT {
    DBusTimeout                 *timeout;       // DBusTimeout
    RexxInstance                *rxInstance;    // the instance pointer, needed as anchor
    RexxObjectPtr                rxDbusServer;  // the DBusServer-Rexx-Object we serve
    int                          currentInterval;   // timeout interval
    bool                         enabled;       // status of the timer
}  STRUCT_TIMEOUT;
typedef STRUCT_TIMEOUT *PSTRUCT_TIMEOUT;                // pointer to structure
long size_STRUCT_TIMEOUT=sizeof(STRUCT_TIMEOUT);        // get and memorize size of structure

    // ------------------------------------------------------------------------------------------
    // define structure for DBusWatch-List
typedef struct _STRUCT_WATCH {
    DBusWatch                  *watch;              // DBusWatch
    struct _STRUCT_WATCH       *next;               // next watch/node, if any
}  STRUCT_WATCH;
typedef STRUCT_WATCH *PSTRUCT_WATCH;                   // pointer to structure
long size_STRUCT_WATCH=sizeof(STRUCT_WATCH);           // get and memorize size of structure


typedef struct _WATCH_LIST  {
    PSTRUCT_WATCH                watchList;     // list of DBusWatches
} STRUCT_WATCH_LIST;
typedef STRUCT_WATCH_LIST  *PSTRUCT_WATCH_LIST;         // pointer to structure
long size_STRUCT_WATCH_LIST=sizeof(STRUCT_WATCH_LIST);  // get and memorize size of structure


    // ------------------------------------------------------------------------------------------
    // define structure for call backs (new connection, add/remove watch)
typedef struct _CALLBACK_DATA {
    RexxInstance                *rxInstance;    // the instance pointer, needed as anchor
    RexxObjectPtr                rxDbusServer;  // the DBusServer-Rexx-Object we serve
//    PSTRUCT_WATCH                watchList;     // list of DBusWatches
} STRUCT_CALLBACK_DATA;
typedef STRUCT_CALLBACK_DATA  *PSTRUCT_CALLBACK_DATA;           // pointer to structure
long size_STRUCT_CALLBACK_DATA=sizeof(STRUCT_CALLBACK_DATA);    // get and memorize size of structure


    // ------------------------------------------------------------------------------------------
    // forward references
logical_t marshal     (DBusMessageIter *iter, char typeCode, RexxObjectPtr rop, PSTRUCT_MARSHAL pmarsh);
logical_t marshalArray(DBusMessageIter *iter, size_t level, RexxObjectPtr anyRop, RexxArrayObject arrIdx, PSTRUCT_MARSHAL pmarsh);
logical_t marshalDict(DBusMessageIter *iter, RexxObjectPtr rxMap, PSTRUCT_MARSHAL pmarsh);
logical_t marshalStruct(DBusMessageIter *iter, RexxArrayObject rao, PSTRUCT_MARSHAL pmarsh);
logical_t marshalVariant(DBusMessageIter *iter, RexxObjectPtr rop, PSTRUCT_MARSHAL pmarsh);

logical_t unmarshal(DBusMessageIter *iter, char typeCode, RexxArrayObject rxResult, PSTRUCT_MARSHAL pmarsh);
logical_t unmarshalArray(DBusMessageIter *iter, size_t level, RexxArrayObject rao, RexxArrayObject arrIdx, PSTRUCT_MARSHAL pmarsh);
logical_t unmarshalDict(DBusMessageIter *iter, RexxDirectoryObject rxMap, PSTRUCT_MARSHAL pmarsh);
logical_t unmarshalStruct(DBusMessageIter *iter, RexxArrayObject rao, PSTRUCT_MARSHAL pmarsh);
logical_t unmarshalVariant(DBusMessageIter *iter, RexxArrayObject rop, PSTRUCT_MARSHAL pmarsh);


#define REXX_ERROR_MESSAGE_CONDITION_RAISED "org.freedesktop.DBus.Error.RexxConditionRaised"
#define REXX_ERROR_MESSAGE_NO_RETURN_VALUE  "org.freedesktop.DBus.Error.RexxNoReturnValue"

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



#ifdef DEBUG_HIT_ENTER_TO_CONTINUE      // rgf, 20140808
// ----------------------------------------------------------------------------
//       DEBUG_HIT_ENTER_TO_CONTINUE

/** Stops execution until user hits return/enter (actually presses any key).
*/
inline void RgfDebugHitEnterToContinue()
{
    fprintf(stderr, "\n[RgfDebugHitEnterToContinue(): Hit Enter to Continue...");fflush(stderr);
    char stuff[5];
    int a=fscanf(stdin, "%c", stuff);
    fprintf(stderr, "\n");fflush(stderr);
}
#endif



    // from BSF4ooRexx.cc
    // allocates and creates a Rexx-like error message
    // returned *msg must be freed
/** Create a Rexx-like/formatted error message (origin: BSF4ooRexx.cc.
*
*   @param condition (ooRexx condition object, an ooRexx directory)
*   @param header (a string)
 *  @return errorString must be freed by receiver
*/
inline char * RgfCreateRexxlikeErrorInfo (RexxThreadContext *rtc, RexxDirectoryObject condObj, const char * header)
{
    RexxCondition cond;
    rtc->DecodeConditionInfo(condObj, &cond);

    RexxObjectPtr        rxNil=rtc->Nil();
    RexxObjectPtr       rxTrue=rtc->True();
    RexxStringObject rsoSyntax=rtc->String("SYNTAX");

    char *msg=(char *)RexxAllocateMemory( 4096 );
    msg[0]=0;

        // format message according to exception type
    RexxObjectPtr ropRes=rtc->SendMessage1(cond.conditionName, "EQUALS", rsoSyntax);
    if (ropRes==rxTrue) // SYNTAX condition ?
    {
        RexxObjectPtr ropTraceback=rtc->DirectoryAt(condObj, "TRACEBACK");
        RexxObjectPtr ropTmpRes=rtc->SendMessage0(ropTraceback, "FIRSTITEM");
        char const *line1=rtc->CString(ropTmpRes) ; // first traceback line=error line

        char line2[2048]="";        // will only use 1024 of it
        SNPRINTF(line2, 2048, "Error %d running %.512s line %d:  %.1024s",
                            (int) cond.rc,      (NULL != cond.program   && rxNil != cond.program   ? rtc->CString( cond.program )  : "program n/a")   ,
                            (int) cond.position,(NULL != cond.errortext && rxNil != cond.errortext ? rtc->CString( cond.errortext ): "errortext n/a") );

        char line3[2048]="";        // will only use 1024 of it
        SNPRINTF( line3, 2048, "Error %d.%d:  %.1024s", (int) (cond.code / 1000),
                                     (int) (cond.code % 1000),
                                     (NULL != cond.message && rxNil != cond.message ? rtc->CString( cond.message ) : "message n/a") );

        SNPRINTF( msg, 4096, "%.16s%.512s:\n%.1024s\n%.1024s\n%.1024s", DLLNAME, header, line1, line2, line3);

        rtc->ReleaseLocalReference(ropTraceback);
        rtc->ReleaseLocalReference(ropTmpRes);
    }
    else
    {
        SNPRINTF( msg, 4096, "%.16s%.512s: Rexx condition [%.512s] raised",
                                  DLLNAME, header,
                                  (NULL != cond.conditionName && rtc->Nil() != cond.conditionName ? rtc->CString(cond.conditionName) : "conditionName n/a") );
    }
    rtc->ReleaseLocalReference(ropRes);

    rtc->ReleaseLocalReference(rxTrue);
    rtc->ReleaseLocalReference(rxNil);

    return msg;
}




#if defined (REXX_LOADER_UNLOADER) || defined (REXX_LOADER)
    // as of ooRexx 4.0: the library gets loaded once and is available globally to all RexxInstances,
    //                   hece it runs once per process only
/** Runs when ooRexx loads this native library. Could be used e.g. for creating ooRexx constant objects to
*   be saved in gloabal variables.
*/
void RexxEntry dbusLoader (RexxThreadContext *rtc)
{
#if defined (DEBUG_LOADER_UNLOADER )
    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            tid=RgfGetTID();

    fprintf(stderr, "/// ===> ===> ===> in dbusLoader(), tid=[%lu], c_rii_ID=[%p] ...\n", (unsigned long) tid, rtc->instance);
    fflush(stderr);
#endif

#if defined (DEBUG_LOADER_UNLOADER )    // just to save warnings
    int res=
#endif
    dbus_threads_init_default();    // initialize threading support, cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusThreads.html#ga33b6cf3b8f1e41bad5508f84758818a7>

#if defined (DEBUG_LOADER_UNLOADER )
    fprintf(stderr, "///                in dbusLoader(), dbus_threads_init_default()=[%d] ...\n", res);
    fflush(stderr);
#endif

}
#endif




#if defined (REXX_LOADER_UNLOADER) || defined (REXX_UNLOADER)
// as of ooRexx 4.0: the library gets loaded once and is available globally to all RexxInstances,
//                   hece it runs once per process only

/** Runs at ooRexx shutdown time.
*/
void RexxEntry dbusUnloader (RexxThreadContext * rtc)
{
    #ifdef UNIX
        pthread_t
    #else   // WIN32
        TID
    #endif
            tid=RgfGetTID();

#if defined (DEBUG_LOADER_UNLOADER )
        fprintf(stderr, "\\\\\\  <=== <=== <=== in dbusUnloader() tid=[%lu], c_rii_ID=[%p] ...\n", (unsigned long) tid, rtc->instance);
        fflush(stderr);
#endif

    }
#endif





    // ********************************************************************
    // *                   --- Routines ---                               *
    // ********************************************************************


// get and return various version information as a string
/** Returns version information about this library and the DBus versions used for compilation and at runtime.
*
*   @return version string in the format &quot;library dbusoorexx=[major*100+minor.yyyymmdd], compile-time dbus=[major.minor.micro], runtime dbus=[major.minor.micro]&quot;&quot;
*/
RexxRoutine0(RexxStringObject, DbusVersionLibrary)
{
#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: DbusVersion() 1, version=[%.256s] ...\n", DBUS_REXXVERSION);
    fflush(stderr);
#endif

  int major, minor, micro;
  dbus_get_version(&major, &minor, &micro);

  char msg[256]="";     // buffer for error message
  SNPRINTF(msg, 256, "library %s=[%s], compile-time dbus=[%s], runtime dbus=[%d.%d.%d]",
                    DLLNAME, DBUS_REXXVERSION, DBUS_VERSION_STRING,
                    major, minor, micro
                    );

  return context->String(msg);
}


    // ---rgf, 2003-08-06: allow Rexx to retrieve its TID (i.e. Java's "JNIEnv" object)
/** Returns the thread identification string. Can be used for debugging thread-related activations.
*
* @param option optional string, by default a 'H'uman conceivable string ('java n', where 'n' is a
*         consecutively numbered value), otherwise 'S'ystem (numeric) value gets returned
*
* @return threadInfoString
*/
RexxRoutine1(RexxStringObject, DbusGetTID, OPTIONAL_CSTRING, option)    // 20090505, ---rgf, only a stub for backward compatibility
{
#ifdef UNIX
    pthread_t
#else   // WIN32
    TID
#endif
        tid=RgfGetTID();

   char *strTid=new char[RGF_TID_STRING_WIDTH]; // create buffer
   SNPRINTF( strTid, RGF_TID_STRING_WIDTH, "%lu%c", ((unsigned long) tid), 0);  // create decimal number

#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: DbusGetTID() 1, tid=[%lu], %%s=[%s] ...\n", (unsigned long) tid, strTid);
    fflush(stderr);
#endif

    RexxStringObject          rso=context->String(strTid);
    delete[] strTid;    // free buffer

    RexxClassObject           rco=context->FindContextClass("DBUS");    // get class
    RexxStringObject    rsoOption=context->String(option==NULL ? "H" : option );
    RexxStringObject rsoFormatted=(RexxStringObject) context->SendMessage2(rco, "FORMATTID", rso, rsoOption);  // get DBus (bus connection) object

    return rsoFormatted;
}


// external Rexx function for validating values; maybe there will be a dbus_validate_bus_name(),
// dbus_validate_object_path(); maybe check out "dbus-marshal-validate.c"
/** Validates a a DBus value using DBus. As DBUS 1.4.x only supports validating signatures,
 * initially only signatures get evaluated.
 *
 * @param strValue the string to be validated
 * @param flag optional, currently only 'S[ignature]'
 *
 * @return <code>.true</code> or <code>.false</code>
 */
RexxRoutine2(RexxObjectPtr, DbusValidate, CSTRING, strValue, OPTIONAL_CSTRING, flag)
{
#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: DbusValidateSignature() 1, version=[%.256s] ...\n", DBUS_REXXVERSION);
    fflush(stderr);
#endif

    if (flag==NULL || *flag=='s' || *flag=='S' )
    {
        RexxStringObject rso=NULL;

            // validate signature
        DBusError       err;
        dbus_error_init(&err);      // initialise the error value

        logical_t sigOK=dbus_signature_validate(strValue, &err);
        if (sigOK)
        {
            rso=context->String("1");   // return .true
        }
        else    // we got an evaluation error, return error information as a string
        {
           char msg[1024]="";   // buffer for error message
           SNPRINTF( msg, 1024, "signature [%s] %s", strValue, err.message);
           rso=context->String(msg);
        }

        dbus_error_free(&err);  // not needed anymore
        return rso;
    }

    return context->Nil();
}




// --------------------------------------------------------------------------------
// allows to halt all Rexx threads of current interpreter; experimental
/** Sends a halt signal to all ooRexx threads to allow for stopping them.
*/
RexxRoutine0(RexxStringObject, DbusHaltRexx)    // create Halt condition in all threads
{
    context->threadContext->instance->Halt();   // raise the Halt condition in all threads of the current Rexx interpreter
    return NULL;                                // no return value
}






    // ********************************************************************
    // *                   --- Methods ---                                *
    // ********************************************************************


// return a connection to the system bus
/** Returns a connection to the system bus, if available. Remark: it may be the case that on some DBus implementation
*   the system bus is not set up/defined in which case the underlying DBus implementation will raise an error condition
*   which gets forwarded to the calling Rexx program.
*/
RexxMethod1(RexxObjectPtr, DbusNativeGetSystemBus, RexxObjectPtr, genericBusObject)
{
#if defined (DEBUG_METHODS)
   fprintf(stderr, "===> arrived in DbusNativeGetSystemBus\n");
   fflush(stderr);
#endif

    DBusConnection *conn=NULL;
    DBusError       err;
    dbus_error_init(&err);      // initialise the error value

        // connect to the DBUS system bus, and check for errors
    conn = dbus_bus_get(DBUS_BUS_SYSTEM, &err);

    char msg[1024]="";     // buffer for error message

    if (dbus_error_is_set(&err))
    {
       SNPRINTF( msg, 1024, "%.16s/method/DbusNativeGetSystemBus(), connection error 1: error.name=[%s], error.message=[%s]",
                             DLLNAME, err.name, err.message);
       dbus_error_free(&err);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;           // return nothing
    }

    if (NULL == conn)
    {
        // raise exception
       SNPRINTF( msg, 1024, "%.16s/method/DbusNativeGetSystemBus(), connection error 2: no connection returned", DLLNAME);
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;           // return nothing
    }

        // save conn in a Rexx pointer object and save it as CSELF
    RexxPointerObject rpo = context->NewPointer(conn);
    context->SendMessage1(genericBusObject, "CSELF=",         rpo); // set CSELF

        // set well known bus name
    context->SendMessage1(genericBusObject, "BUSTYPE=", context->String("system"));

    return genericBusObject;
}


// return a connection to the session bus
/** Returns a connection to the session bus, if available. Remark: it may be the case that on some DBus implementation
*   the session bus is not set up/defined in which case the underlying DBus implementation will raise an error condition
*   which gets forwarded to the calling Rexx program.
*/
RexxMethod1(RexxObjectPtr, DbusNativeGetSessionBus, RexxObjectPtr, genericBusObject)
{
#if defined (DEBUG_METHODS)
   fprintf(stderr, "===> arrived in DbusNativeGetSessionBus\n");
   fflush(stderr);
#endif
    DBusConnection *conn=NULL;
    DBusError       err;
    dbus_error_init(&err);      // initialise the error value

        // connect to the DBUS system bus, and check for errors
    conn = dbus_bus_get(DBUS_BUS_SESSION, &err);

    char msg[1024]="";     // buffer for error message

    if (dbus_error_is_set(&err))
    {
       SNPRINTF( msg, 1024, "%.16s/method/DbusNativeGetSessionBus(), connection error 1: error.name=[%s], error.message=[%s]",
                             DLLNAME, err.name, err.message);
       dbus_error_free(&err);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;           // return nothing
    }

    if (NULL == conn)
    {
        // raise exception
       SNPRINTF( msg, 1024, "%.16s/method/DbusNativeGetSessionBus(), connection error 2: no connection returned", DLLNAME);
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;           // return nothing
    }

        // save conn in a Rexx pointer object and save it as CSELF
    RexxPointerObject rpo = context->NewPointer(conn);
    context->SendMessage1(genericBusObject, "CSELF=",         rpo); // set CSELF

        // set well known bus name
    context->SendMessage1(genericBusObject, "BUSTYPE=", context->String("session"));

    return genericBusObject;
}


// get and return unique bus name (.Dbus instance method)
/** Returns the unique bus name of the connection.
*
* @return unique bus name of current object's connection pointer as a Rexx string
*/
RexxMethod1(RexxStringObject, DbusGetUniqueBusName, CSELF, connPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusGetUniqueBusName, connPtr=[%p]\n", connPtr);
   fflush(stderr);
#endif
    if (connPtr==NULL)
    {
        {
           char msg[1024]="";     // buffer for error message
           SNPRINTF( msg, 1024, "%.16s/method/DbusGetUniqueBusName(), connection error 1: not connected (NULL)", DLLNAME);

            // raise exception
           context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
           return NULLOBJECT;           // return nothing
        }
    }

    DBusConnection *conn=(DBusConnection *) connPtr;

    const char *tmpStr = dbus_bus_get_unique_name(conn);
    RexxStringObject rso = context->String( (tmpStr==NULL ? "" : tmpStr) );
    return rso;
}


// get and return unique machine id
/** Returns this machine's unique id as defined by DBus.
*
*  @return unique machine id as a Rexx string
*/
RexxMethod0(RexxStringObject, DbusGetUniqueMachineId)
{
#if defined (DEBUG_METHODS)
   fprintf(stderr, "===> arrived in DbusGetUniqueMachineId\n");
   fflush(stderr);
#endif
    char *tmpStr = dbus_get_local_machine_id();
    RexxStringObject rso= context->String( (tmpStr==NULL ? "" : tmpStr) );
    dbus_free(tmpStr);
    return rso;
}


// get and return unique bus id (.DBus instance method)
/** Returns this object's connection pointers unique bus id as defined by DBus.
*
* @return unique bus id of this connection
*/
RexxMethod1(RexxObjectPtr, DbusGetUniqueBusId, CSELF, connPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusGetUniqueBusId\n");
   fflush(stderr);
#endif
    DBusConnection *conn=(DBusConnection *) connPtr;
    DBusError       err;
    dbus_error_init(&err);      // initialise the error value

        // get and set unique id (unique bus name)
    char *tmpStr = dbus_bus_get_id(conn, &err);
    if (dbus_error_is_set(&err))
    {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusGetUniqueBusId(), connection error 1: error.name=[%s], error.message=[%s]",
                             DLLNAME, err.name, err.message);
       dbus_error_free(&err);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;           // return nothing
    }
    RexxStringObject rso = context->String( (tmpStr==NULL ? "" : tmpStr) );
    dbus_free(tmpStr);
    return rso;
}


// if open, returns true, false else; cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusConnection.html#ga611ae94556af36fe30bfb547366ca4e1>
/** Determines whether this connection is connected to DBus.
*
* @return <code>.true</code> if connected, <code>.false</code> else
*/
RexxMethod1(RexxObjectPtr, DbusConnectionIsConnected, CSELF, connPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusConnectionIsConnected\n");
   fflush(stderr);
#endif
    DBusConnection *conn=(DBusConnection *) connPtr;
    if (conn!=NULL && connPtr!=context->Nil() && dbus_connection_get_is_connected(conn))
    {
        return context->True();
    }
    return context->False();
}


// expects a single char; if that is a supported type code returns .true, otherwise .false
/** Determines whether the given type code can be sent with this DBus implementation.
* Hint: on Windows unix FD cannot be sent via DBus.
*
* @param typeCode a Rexx string denoting one of the official DBus type codes
* @return <code>.true</code> if <em>typeCode</em> values can be sent, <code>.false</code> else
*/
RexxMethod2(RexxObjectPtr, DbusConnectionCanSendType, CSTRING, typeCode, CSELF, connPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusConnectionCanSendType\n");
   fflush(stderr);
#endif
    DBusConnection *conn=(DBusConnection *) connPtr;
    if (conn!=NULL && connPtr!=context->Nil() && dbus_connection_can_send_type(conn, *typeCode))
    {
        return context->True();
    }
    return context->False();
}


// if authenticated, returns true, false else
/** Determines whether the connection is authenticated.
*
* @return <code>.true</code> if <em>typeCode</em> connection is authenticated, <code>.false</code> else
*/
RexxMethod1(RexxObjectPtr, DbusConnectionIsAuthenticated, CSELF, connPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusConnectionIsAuthenticated\n");
   fflush(stderr);
#endif
    DBusConnection *conn=(DBusConnection *) connPtr;
    if (conn!=NULL && connPtr!=context->Nil() && dbus_connection_get_is_authenticated(conn))
    {
        return context->True();
    }
    return context->False();
}



// ============================================================================= //
// busname related --->

// request a unique bus name (.Dbus instance method)
/** Request a (unique) bus name from DBus.
*
* @param busName the desired bus name DBus should reserve
* @param flags optional flags (default: <code>1</code>=<code>DBUS_NAME_FLAG_ALLOW_REPLACEMENT</code>,
*              <code>2</code>=<code>DBUS_NAME_FLAG_REPLACE_EXISTING</code>
*
* @return return code from DBus function <code>dbus_bus_request_name()</code>
*/
RexxMethod3(RexxObjectPtr, DbusBusNameRequest, CSTRING,       busName,
                                               OPTIONAL_RexxObjectPtr, flags,
                                               CSELF,         connPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusBusNameRequest\n");
   fflush(stderr);
#endif

    uint32_t  requestFlags=3;   // default to: 1=DBUS_NAME_FLAG_ALLOW_REPLACEMENT +
                                //             2=DBUS_NAME_FLAG_REPLACE_EXISTING

    if (flags!=NULL)    // argument given, then process it
    {
       logical_t bSuccess=context->ObjectToUnsignedInt32(flags, &requestFlags);
       if (bSuccess==0)                // conversion yielded an error
       {
           char msg[1024]="";     // buffer for error message
           SNPRINTF( msg, 1024, "%.16s/method/DbusBusNameRequest(), error 1: argument # 2='flags', value='%.64s' cannot be converted to an unsigned integer",
                                 DLLNAME,
                                 context->CString(context->ObjectToString(flags))
                                 );
            // raise exception
           context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
           return NULLOBJECT;           // return nothing
       }
    }

        // get self and carry out the
    DBusConnection *conn=(DBusConnection *) connPtr;
    DBusError       err;

    dbus_error_init(&err);      // initialise the error value

        // get and set unique id (unique bus name)
    int res = dbus_bus_request_name (conn, busName, requestFlags, &err);

    if (dbus_error_is_set(&err))
    {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusBusNameRequest(), error 2: error.name=[%s], error.message=[%s]",
                             DLLNAME, err.name, err.message);
       dbus_error_free(&err);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;           // return nothing
    }

#if defined (DEBUG_METHODS)
   fprintf(stderr, "<=== about to leave DbusBusNameRequest\n");
   fflush(stderr);
#endif

    return context->Int32ToObject((int32_t) res);
}


// test whether bus name has an owner (.Dbus instance method)
// @deprecated because of race-conditions, cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusBus.html#ga5331b172dd8ed00eec130e894c5c2a0b>
/** Test whether bus name has an owner.
*
* @deprecated because of race-conditions, cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusBus.html#ga5331b172dd8ed00eec130e894c5c2a0b>
*
* @param busName to test
* @return <code>.true</code> if <em>typeCode</em> busname has an owner, <code>.false</code> else
*/
RexxMethod2(RexxObjectPtr, DbusBusNameHasOwner, CSTRING, busName,
                                                CSELF,   connPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusBusNameHasOwner\n");
   fflush(stderr);
#endif
        // get self and carry out the
    DBusConnection *conn=(DBusConnection *) connPtr;
    DBusError       err;
    dbus_error_init(&err);      // initialise the error value

    dbus_bool_t bSuccess= dbus_bus_name_has_owner(conn, busName, &err);

    if (dbus_error_is_set(&err))
    {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusBusNameHasOwner(), error 1: error.name=[%s], error.message=[%s]",
                             DLLNAME, err.name, err.message);
       dbus_error_free(&err);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;           // return nothing
    }

    return context->LogicalToObject(bSuccess);
}



        // release given bus name has an owner (.Dbus instance method)
/** Release supplied bus name.
*
* @param busName the name to be released
* @return return code from DBus function <code>dbus_bus_release_name()</code>
*/
RexxMethod2(RexxObjectPtr, DbusBusNameRelease, CSTRING, busName,
                                               CSELF,   connPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusBusNameRelease\n");
   fflush(stderr);
#endif
        // get self and carry out the
    DBusConnection *conn=(DBusConnection *) connPtr;
    DBusError       err;
    dbus_error_init(&err);      // initialise the error value

    int res= dbus_bus_release_name(conn, busName, &err);

    if (dbus_error_is_set(&err))
    {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusBusNameRelease(), error 1: error.name=[%s], error.message=[%s]",
                             DLLNAME, err.name, err.message);
       dbus_error_free(&err);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;           // return nothing
    }

    return context->Int32ToObject((int32_t) res);
}



// ============================================================================= //
// match rule related --->

// add match rule
/** Add a DBus match rule.
*
* @param rule the match rule to be added to this connection
* @param block optional, if <code>.true</code> (default), the call will be blocking, otherwise non-blocking
*/
RexxMethod3(RexxObjectPtr, DbusBusAddMatch, CSTRING, rule,
                                            OPTIONAL_RexxObjectPtr, block,
                                            CSELF,   connPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusBusAddMatch [%s] \n", rule);
   fflush(stderr);
#endif
    logical_t bBlock=TRUE;      // default to TRUE

    if (block!=NULL)             // argument given, process it
    {
        logical_t bSuccess=TRUE;
        bSuccess=context->ObjectToLogical(block, &bBlock);

        if (bSuccess==0)            // conversion yielded an error
        {
            char msg[1024]="";      // buffer for error message
            SNPRINTF( msg, 1024, "%.16s/method/DbusAddMatch(), error 1: argument # 1='block', value='%.64s' cannot be converted to a logical value",
                                  DLLNAME,
                                  context->CString(context->ObjectToString(block))
                                  );
             // raise exception
            context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
            return NULLOBJECT;           // return nothing
        }
    }

        // get self and carry out the
    DBusConnection *conn=(DBusConnection *) connPtr;
    DBusError       err;
    dbus_error_init(&err);      // initialise the error value

        // README: the API documents that if using NULL instead of err, then the call won't block, cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusBus.html> (as of: 2011-07-18)
    if (bBlock==TRUE)           // blocking call
    {
        dbus_bus_add_match(conn, rule, &err);
    }
    else                        // non-blocking call
    {
        dbus_bus_add_match(conn, rule, NULL);
        dbus_connection_flush(conn);    // cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusBus.html#ga4eb6401ba014da3dbe3dc4e2a8e5b3ef> (as of: 2011-07-18)
    }

    if (dbus_error_is_set(&err))
    {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusBusAddMatch(), error 2: error.name=[%s], error.message=[%s]",
                             DLLNAME, err.name, err.message);
       dbus_error_free(&err);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;           // return nothing
    }

    return NULLOBJECT;
}


// removes the most recent added rule, if found
/** Remove a DBus match rule.
*
* @param rule the match rule to be removed from this connection
* @param block optional, if <code>.true</code> (default), the call will be blocking, otherwise non-blocking
*/

RexxMethod3(RexxObjectPtr, DbusBusRemoveMatch, CSTRING, rule,
                                               OPTIONAL_RexxObjectPtr, block,
                                               CSELF,   connPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusBusRemoveMatch [%s] \n", rule);
   fflush(stderr);
#endif
    logical_t bBlock=TRUE;      // default to TRUE

    if (block!=NULL)             // argument given, process it
    {
        logical_t bSuccess=TRUE;
        bSuccess=context->ObjectToLogical(block, &bBlock);

        if (bSuccess==0)            // conversion yielded an error
        {
            char msg[1024]="";      // buffer for error message
            SNPRINTF( msg, 1024, "%.16s/method/DbusRemoveMatch(), error 1: argument # 1='block', value='%.64s' cannot be converted to a logical value",
                                  DLLNAME,
                                  context->CString(context->ObjectToString(block))
                                  );
             // raise exception
            context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
            return NULLOBJECT;           // return nothing
        }
    }

        // get self and carry out the
    DBusConnection *conn=(DBusConnection *) connPtr;
    DBusError       err;
    dbus_error_init(&err);      // initialise the error value

    if (bBlock==TRUE)           // blocking call
    {
        dbus_bus_remove_match(conn, rule, &err);
    }
    else                        // non-blocking call
    {
        dbus_bus_remove_match(conn, rule, NULL);
    }

    if (dbus_error_is_set(&err))
    {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusBusRemoveMatch(), error 2: error.name=[%s], error.message=[%s]",
                             DLLNAME, err.name, err.message);
       dbus_error_free(&err);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;           // return nothing
    }

    return NULLOBJECT;
}



// --------------------------------------------------------------------------------
// -------------------------------- helper functions ------------------------------

// --------------------------------------------------------------------------------
/** This function tests the string for the value "useThisSignature=" caselessly, which
*   gets used in the pbulic 'dbus.cls' pacakge routine "DBus.box()".
*
*   @param fel signature string
*   @return <code>true</code> if signature string is led in with &quot;useThisSignature=&quot;, <code>false</code> otherwise
*/
inline bool OVERRIDE_SIGNATURE_GIVEN(CSTRING fel)
{
    return
        (fel[ 0]=='u' || fel[ 0]=='U') &&
        (fel[ 1]=='s' || fel[ 1]=='S') &&
        (fel[ 2]=='e' || fel[ 2]=='E') &&
        (fel[ 3]=='t' || fel[ 3]=='T') &&
        (fel[ 4]=='h' || fel[ 4]=='H') &&
        (fel[ 5]=='i' || fel[ 5]=='I') &&
        (fel[ 6]=='s' || fel[ 6]=='S') &&
        (fel[ 7]=='s' || fel[ 7]=='S') &&
        (fel[ 8]=='i' || fel[ 8]=='I') &&
        (fel[ 9]=='g' || fel[ 9]=='G') &&
        (fel[10]=='n' || fel[10]=='N') &&
        (fel[11]=='a' || fel[11]=='A') &&
        (fel[12]=='t' || fel[12]=='T') &&
        (fel[13]=='u' || fel[13]=='U') &&
        (fel[14]=='r' || fel[14]=='R') &&
        (fel[15]=='e' || fel[15]=='E') &&
        (fel[16]=='=' || fel[16]=='=')
             ;
}

// --------------------------------------------------------------------------------
    // return RexxArrayObject's content as a comma-separated CSTRING
/** Turns a Rexx array object into a comma separated CSTRING and returns it.
*
* @param arrIdx Rexx array object
* @param context the RexxThreadContext to interact with ooRexx
* @return a CSTRING containing the array elements as comma separated values
*/
inline CSTRING ARR_AS_STRING( RexxArrayObject arrIdx , RexxThreadContext *context)
{
    RexxObjectPtr arg1= context->NewString("L",1);
    RexxObjectPtr arg2= context->NewString(",",1);
    RexxObjectPtr rop = context->SendMessage2(arrIdx, "MAKESTRING", arg1, arg2);

    context->ReleaseLocalReference(arg1);
    context->ReleaseLocalReference(arg2);

    // return context->ObjectToStringValue(rop);
    CSTRING tmpValue=context->ObjectToStringValue(rop);
    context->ReleaseLocalReference(rop);

    return tmpValue;
}


// --------------------------------------------------------------------------------
    // convenience inline function to query the items in a given dimension
/** Queries the array's dimension size and returns it.
*
* @param rao the Rexx array object
* @param dimension the dimension to check
* @param context the RexxThreadContext to interact with ooRexx
* @return the size of the array object in the given dimension
*/
inline size_t GET_ARRAY_SIZE_AT_DIMENSION(RexxArrayObject rao, int32_t dimension, RexxThreadContext *context)
{
    RexxObjectPtr ropLevel=context->Int32ToObject(dimension);

    RexxObjectPtr ropDim=context->SendMessage1(rao, "DIMENSION", ropLevel);
    size_t    val=0;
    context->ObjectToStringSize(ropDim, &val);

    context->ReleaseLocalReference(ropLevel);
    context->ReleaseLocalReference(ropDim);

    return val;
}


// --------------------------------------------------------------------------------
    // return number of top-level arguments needed for given signature (needs to be syntactically correct)
/** Return the number of top-level arguments needed for the given signature, which needs to be syntactically correct.
*
* @param signature the signature to analyze
* @return number of top-level arguments this signature mandates
*/
inline size_t GET_NUMBER_OF_REXX_ARGS_FOR_SIGNATURE(char * signature)
{
    char *cursor=signature;
    char openContainer, closeContainer;
    size_t count=0;

    while (*cursor)
    {
        count++;
        if (*cursor=='a')   // found an array munch it
        {
            while (*(++cursor)=='a' && *cursor) ;
        }

        if (*cursor=='(' || *cursor=='{')   // a container, find closing one
        {
            openContainer=*cursor;
            closeContainer=(openContainer=='(' ? ')' : '}');  // define matching closing container

            size_t open=1;
            while (open>0 && *cursor)
            {
                cursor++;
                if (*cursor==openContainer)
                {
                    open++;
                }
                else if (*cursor==closeContainer)
                {
                    open--;
                }
            }
        }
        cursor++;
    }
    return count;       // return # of Rexx arguments needed to match the given signature
}



// --------------------------------------------------------------------------------
/** Creates an returns an instance of the ooRexx <code>DateTime</code> class.
*
* @param context The RexxThreadContext to interact with ooRexx
* @return a <code>DateTime</code> instance
*/
inline RexxStringObject GET_REXX_DATE_TIME_AS_REXX_STRING(RexxThreadContext *context)
{
    RexxClassObject dtClz=context->FindClass("DATETIME");       // get the DateTime class
    RexxObjectPtr   dt   =context->SendMessage0(dtClz, "NEW");  // get an instance
    RexxStringObject  res=context->ObjectToString(dt);

    context->ReleaseLocalReference(dtClz);
    context->ReleaseLocalReference(dt);

    return res;                        // return string rendering
}

// --------------------------------------------------------------------------------
    // convenience inline function to query the items in a given dimension
/** Queries and returns the number of items in a given dimension in the Rexx array object.
*
* @param arrIdx the Rexx array object
* @param index the dimension's index
* @param context The RexxThreadContext to interact with ooRexx
* @return the number of items in the given dimension
*/
// inline size_t GET_UNMARSHAL_ARRAY_VALUE_AT_INDEX(RexxArrayObject arrIdx, int32_t index, RexxThreadContext *context)
inline size_t GET_UNMARSHAL_ARRAY_VALUE_AT_INDEX(RexxArrayObject arrIdx, size_t index, RexxThreadContext *context)
{

    size_t    val=0;
    // logical_t flag;

#ifdef DEBUG_ARRAY
fprintf(stderr, "     /// GET_UNMARSHAL_ARRAY..., ArrayAt(index=[%d)=[%p]\n", index, context->ArrayAt(arrIdx, index));
#endif

        // fetch value at arrIdx' index and return it as a number
    RexxObjectPtr rop=context->ArrayAt(arrIdx, index);
    if (rop!=NULL)
    {
        RexxObjectPtr ropVal=context->ArrayAt(arrIdx, index);
        context->ObjectToStringSize(ropVal, &val);
        context->ReleaseLocalReference(ropVal);
    }

    context->ReleaseLocalReference(rop);
    return val;
}


// --------------------------------------------------------------------------------
    // convenience inline function to set the index array to the desired value
/** Sets a numerical value at the supplied index in the Rexx array object.
*
* @param arrIdx the Rexx array object
* @param idx the index to store the value at
* @param numVal the numeric value to be stored
* @param context The RexxThreadContext to interact with ooRexx
*/
inline void SET_ARRAY_IDX(RexxArrayObject arrIdx, size_t idx, size_t numVal, RexxThreadContext *context)
{
    RexxObjectPtr rop=context->StringSizeToObject(numVal);
    context->ArrayPut(arrIdx, rop, idx);
    context->ReleaseLocalReference(rop);
}


// --------------------------------------------------------------------------------
    // convenience inline function to initialize a new (sub) STRUCT_MARSHAL
/** Initialize a <code>STRUCT_MARSHAL</code> structure with another one.
*
* @param pnewM pointer to a new <code>STRUCT_MARSHAL</code> structure
* @param localSignature the new signature to use
* @param pmarsh pointer to the existing (parent signature) <code>STRUCT_MARSHAL</code> structure
*/
inline void SET_NEW_STRUCT_MARSHAL (PSTRUCT_MARSHAL pnewM, char *localSignature, PSTRUCT_MARSHAL pmarsh)
{

    memset(pnewM, 0, size_STRUCT_MARSHAL);  // make sure structure is set to \0

    pnewM->signature              = localSignature;
    pnewM->signatureLength        = strlen(localSignature);
    pnewM->cursor                 = pnewM->signature;
    // pnewM->arrEnd                 = 0;
    // pnewM->arrStart               = 0;
    // pnewM->elementEnd             = localSignature+(pmarsh->elementEnd   - pmarsh->arrEnd);
    pnewM->msgChunk256            = pmarsh->msgChunk256;
    pnewM->context                = pmarsh->context;
    pnewM->dbusError              = pmarsh->dbusError;
    pnewM->rexxError              = pmarsh->rexxError;

    pnewM->arrayClass             = pmarsh->arrayClass;
    pnewM->is_ay                  = pmarsh->is_ay;         // simple byte array in hand ?

    pnewM->marshal_ay_as_string   = pmarsh->marshal_ay_as_string;  // indicate we need to marshal the string elements as byte arrays
    pnewM->unmarshal_ay_as_string = pmarsh->unmarshal_ay_as_string;
    pnewM->is_dict                = pmarsh->is_dict;       // a DICT in hand ?
}




// --------------------------------------------------------------------------------
// main unmarshal() control
/** Entry point for unmarshalling.
*
* @param iter the DBus iteration structure
* @param typeCode the type code to use for unmarshalling
* @param rxResult the Rexx result array
* @param pmarsh the structure that controls unmarshalling
* @return <code>true</code> if unmarshalling was carried out without errors, <code>false</code> else
*/
logical_t unmarshal(DBusMessageIter *iter, char typeCode, RexxArrayObject rxResult, PSTRUCT_MARSHAL pmarsh)
{
#ifdef DEBUG_MARSHAL
    fprintf(stderr, "---> arrived in unmarshal(...), typeCode=[%c], cursor=[%lu], signature=[%s], iter=[%p], rxResult=[%p] \n",
                       typeCode,
                       (unsigned long) (pmarsh->cursor - pmarsh->signature),
                       pmarsh->signature,
                       iter,
                       rxResult
                       );
#endif

    int       resDbus=1;            // "0": dbus-error (out of memory)
    logical_t orxConvFlag=true;

    pmarsh->errorPosInSignature=pmarsh->cursor; // memorize cursor, in case we get an error


    int currentType=0;
    while (pmarsh->cursor <= pmarsh->elementEnd &&
           (currentType = dbus_message_iter_get_arg_type (iter)) != DBUS_TYPE_INVALID)
    {

        RexxObjectPtr currRexxObj=NULL;
        switch (currentType)
        {
        case DBUS_TYPE_STRING :     // 's', STRING (UTF-8): must not contain nul bytes, but must end wiht a nul byte
            {
                CSTRING str;
                dbus_message_iter_get_basic(iter, &str);
                currRexxObj=pmarsh->context->String(str);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any

#ifdef DEBUG_UNMARSHAL
fprintf(stderr, ".... unmarshal() - 's', cursor now: [%lu], end-element: [%lu], cursor>=end-element? [%d] - signature=[%s], value=[%s] /str=[%s] \n",
            (unsigned long) (pmarsh->cursor     - pmarsh->signature),
            (unsigned long) (pmarsh->elementEnd - pmarsh->signature),
            (int) (pmarsh->cursor >= pmarsh->elementEnd),
            pmarsh->signature,
            pmarsh->context->ObjectToStringValue(currRexxObj),
            str
    );
#endif

            }
            break;

        case DBUS_TYPE_SIGNATURE :      // 'g'
            {
                CSTRING str;
                dbus_message_iter_get_basic(iter, &str);
                currRexxObj=pmarsh->context->String(str);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        case DBUS_TYPE_OBJECT_PATH :    // 'o'
            {
                CSTRING str;
                dbus_message_iter_get_basic(iter, &str);
                currRexxObj=pmarsh->context->String(str);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        case DBUS_TYPE_BYTE :           // 'y'
            {
                char str[]="X";     // single 'X' (byte) will be overwritten by the fetched byte value
                dbus_message_iter_get_basic(iter, str);

                if (str==NULL)
                {
#ifdef DEBUG_UNMARSHAL
    fprintf(stderr, "unmarshal(BYTE), # 1 NullString() ... \n");
#endif
                    currRexxObj=pmarsh->context->NullString();   // turn into Rexx string
                }
                else
                {
                    currRexxObj=pmarsh->context->String(str, 1);   // turn into Rexx string
                }

                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        case DBUS_TYPE_BOOLEAN :        // 'b'
            {
                dbus_bool_t val=false;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->LogicalToObject((logical_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        case DBUS_TYPE_INT16 :          // 'n'
            {
                dbus_int16_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->Int32ToObject((int32_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        case DBUS_TYPE_UINT16 :         // 'q'
            {
                dbus_uint16_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->UnsignedInt32ToObject((uint32_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;


        case DBUS_TYPE_INT32 :          // 'i'
            {
                dbus_int32_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->Int32ToObject((int32_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;


#if defined ( DBUS_TYPE_UNIX_FD )       // e.g. not defined for Windows
        case DBUS_TYPE_UNIX_FD :        // 'h', UNIX_FD: a 32-bit unsigned integer accordig to dbus-specification.html
            {
                dbus_uint32_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->UnsignedInt32ToObject((uint32_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;
#endif

        case DBUS_TYPE_UINT32 :         // 'u'
            {
                dbus_uint32_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->UnsignedInt32ToObject((uint32_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        case DBUS_TYPE_INT64 :          // 'x'
            {
                dbus_int64_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->Int64ToObject((int64_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        case DBUS_TYPE_UINT64 :         // 't'
            {
                dbus_uint64_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->UnsignedInt64ToObject((uint64_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        case DBUS_TYPE_DOUBLE :         // 'd', DOUBLE (IEEE 754)
            {
                double val=0;
                dbus_message_iter_get_basic(iter, &val);
                // rgf, 2014-07-26: need to use DoubleToObjectWithPrecision such that ooRexx uses 20 digits and not 9 only!
                //                  20 digits are enough for IEEE double (and also for uint64, ie 2**64)
                currRexxObj=pmarsh->context->DoubleToObjectWithPrecision(val, 20);   // turn into Rexx string

                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;


            // containers -------------------------------------
        case DBUS_TYPE_ARRAY :      // 'a', ARRAY
            {
#if defined (DEBUG_ARRAY)
   fprintf(stderr,"/unmarshal/DBUS_TYPE_ARRAY, 01, switch-top: ");
   #if defined (DEBUG_STRUCT_MARSHAL_PRINT)
     dump_struct_marshal(pmarsh);
   #endif
   fflush(stderr);
#endif
                    // calc dimensionality, set array and element positions
                pmarsh->arrStart=pmarsh->cursor;
                char *tmpCursor=pmarsh->cursor;

                while (*tmpCursor=='a')
                {
                    tmpCursor++;
                }
                pmarsh->dimensions = tmpCursor - pmarsh->arrStart; // dimensions
                pmarsh->arrEnd=tmpCursor-1;    // end of array

                pmarsh->elementStart=tmpCursor;

                if (*tmpCursor=='(' || *tmpCursor=='{')   // a struct or dict ?
                {
                    char   ch = *tmpCursor;
                    char   chEnd;               // end character to look for
                    if (ch=='(')
                    {
                        chEnd=')';
                    }
                    else
                    {
                        chEnd='}';
                    }

                    size_t open=1;
                    while (open>0 && *tmpCursor)   // as long as not closed and not end of string
                    {
                        if (*(++tmpCursor)==ch)
                        {
                            open++;                 // opening (another of same type)
                        }
                        else if (*tmpCursor==chEnd)
                        {
                            open--;                 // closing
                        }
                    }
                    pmarsh->elementEnd=tmpCursor;
                }
                else
                {
                    pmarsh->elementEnd=tmpCursor;
                }

#ifdef DEBUG_ARRAY
    {
            char   *tmp=pmarsh->signature;
            size_t arrStartPos=pmarsh->arrStart - tmp;
            size_t arrEndPos  =pmarsh->arrEnd   - tmp, arrLen=arrEndPos - arrStartPos+1;

            size_t elStartPos=pmarsh->elementStart - tmp;
            size_t elEndPos  =pmarsh->elementEnd   - tmp, elLen=elEndPos - elStartPos +1;

            size_t cursorPos = pmarsh->cursor - pmarsh->signature;

            fprintf(stderr, "     /--> unmarshal(): DBUS_TYPE_ARRAY - signature=[%s], cursorPos=[%lu|%c]: arrStartPos=[%lu|%c], arrEndPos=[%lu|%c], arrLen=[%lu] | elStartPos=[%lu|%c], elEndPos=[%lu|%c], elLen=[%lu]\n",
                                         tmp,  (unsigned long) cursorPos, *(tmp+cursorPos),
                                         (unsigned long) arrStartPos, *(tmp+arrStartPos), (unsigned long) arrEndPos, *(tmp+arrEndPos),
                                         (unsigned long) arrLen,
                                         (unsigned long) elStartPos, *(tmp+elStartPos), (unsigned long) elEndPos, *(tmp+elEndPos),
                                         (unsigned long) elLen);

            fflush(stderr);
    }
#endif

                pmarsh->is_ay   = (*pmarsh->elementStart=='y');     // byte array in hand?
                pmarsh->is_dict = (*pmarsh->elementStart=='{');   // dict in hand?

#ifdef DEBUG_ARRAY
    fprintf( stderr, "!!!!!!!!!! signature=[%s], pmarsh->is_ay=[%d] (pmarsh->unmarshal_ay_as_string=[%d]), pmarsh->is_dict=[%d], pmarsh->elementStart=[%s]\n",
                         pmarsh->signature,
                         (int) pmarsh->is_ay,
                         (int) pmarsh->unmarshal_ay_as_string,
                         (int) pmarsh->is_dict,
                         pmarsh->elementStart);
#endif


                    // create array object to receive data, an index array to address array elements, let
                size_t nrDims = pmarsh->dimensions -
                                (pmarsh->is_ay ? pmarsh->unmarshal_ay_as_string : 0); //  pmarsh->is_ay;

#ifdef DEBUG_ARRAY
    fprintf(stderr, "     /--> unmarshal(): DBUS_TYPE_ARRAY - signature=[%s], about to create array with [%lu] dimensions\n",
                               pmarsh->signature, (unsigned long) nrDims); fflush(stderr);

  #if defined (DEBUG_ARRAY)
     fprintf(stderr,"/unmarshal/DBUS_TYPE_ARRAY, 02, before creating ooRexx array: ");
     #if defined (DEBUG_STRUCT_MARSHAL_PRINT)
       dump_struct_marshal(pmarsh);
     #endif
     fflush(stderr);
  #endif
#endif

                RexxArrayObject arrIdx   =pmarsh->context->NewArray(nrDims);
                for (size_t i=1; i<=nrDims; i++)    // set each index to '0', will be used to initialize each dimension with a size of 0
                {
                    SET_ARRAY_IDX(arrIdx, i, 0, pmarsh->context);
                }

                    // create a new dimensioned array using plain Rexx messages
                RexxArrayObject rxWithArgs=pmarsh->context->NewArray(nrDims);    // array for message name plus message argument array

                for (size_t tmpIdx=1;tmpIdx<=nrDims;tmpIdx++)
                {
                    RexxObjectPtr rop=pmarsh->context->ArrayAt(arrIdx, tmpIdx);
                    pmarsh->context->ArrayAppend(rxWithArgs, rop);
                    pmarsh->context->ReleaseLocalReference(rop);
                }

                RexxStringObject rso=pmarsh->context->String("NEW");
                currRexxObj=pmarsh->context->SendMessage2(pmarsh->arrayClass, "SENDWITH", rso, rxWithArgs);
                pmarsh->context->ReleaseLocalReference(rso);

#ifdef DEBUG_ARRAY
   fprintf(stderr, "     unmarshal(...), 'a' - currRexxObj=[%p]~dimension=[%lu];  pmarsh->dimensions=[%lu] - nrDims=[%lu]; | ~size=[%lu], ~items=[%lu]\n",
                         currRexxObj,
                         (unsigned long) pmarsh->context->ArrayDimension((RexxArrayObject) currRexxObj),
                         (unsigned long) pmarsh->dimensions,
                         (unsigned long) nrDims,
                         (unsigned long) pmarsh->context->ArraySize((RexxArrayObject) currRexxObj),
                         (unsigned long) pmarsh->context->ArrayItems((RexxArrayObject) currRexxObj)
                         );

   fprintf(stderr, "     unmarshal(...), 'a' - p->is_ay=[%lu], p->unmarshal_ay_as_string=[%lu], arrIdx=[%s], p->signature=[%s]\n",
                         (unsigned long) pmarsh->is_ay,
                         (unsigned long) pmarsh->unmarshal_ay_as_string,
                         ARR_AS_STRING(arrIdx, pmarsh->context),
                         pmarsh->signature
                         );
#endif

                    // unmarshal array
                unmarshalArray(iter, 1, (RexxArrayObject) currRexxObj, arrIdx, pmarsh);

#ifdef DEBUG_ARRAY
    {
        size_t cursorPos = pmarsh->cursor - pmarsh->signature;

        fprintf(stderr, "     /<-- signature=[%s], cursorPos=[%lu|%c]: end-of-array-processing #1. currRexxObj: dims=[%lu], items=[%lu], size=[%lu] \n",
                pmarsh->signature,  (unsigned long) cursorPos, *(pmarsh->signature+cursorPos),
                (unsigned long) pmarsh->context->ArrayDimension((RexxArrayObject) currRexxObj),
                (unsigned long) pmarsh->context->ArrayItems((RexxArrayObject) currRexxObj),
                (unsigned long) pmarsh->context->ArraySize((RexxArrayObject) currRexxObj)
                );
    }
#endif


                if ((pmarsh->is_dict)     // element type a dictionary? then return it
                    || (pmarsh->dimensions==1 &&
                        pmarsh->is_ay &&
                        pmarsh->unmarshal_ay_as_string) // a single byte array to be unmarshalled as a string?
                    )
                {

                    RexxObjectPtr tmpObj=pmarsh->context->ArrayAt((RexxArrayObject)currRexxObj, 1);
                    if (tmpObj!=NULL)           // if no entry == NULL, then return currRexxObj
                    {
                        if (currRexxObj!=NULLOBJECT)
                        {
                            pmarsh->context->ReleaseLocalReference(currRexxObj);
                        }
                        currRexxObj=tmpObj;
                    }

#ifdef DEBUG_ARRAY
   fprintf(stderr, "     unmarshal(...), DICT-ARRAY or STRING for 'ay' -> currRexxObj=[%p]=[%s] !\n",
                                      currRexxObj,
                                      (currRexxObj == NULL ? NULL : pmarsh->context->ObjectToStringValue(currRexxObj))
                                      );
#endif
                }

                pmarsh->context->ReleaseLocalReference(arrIdx);
                pmarsh->context->ReleaseLocalReference(rxWithArgs);

                    // move cursor to next char in signature
                pmarsh->cursor=pmarsh->elementEnd+1;       // let us point to the next typeCode if any
            }
            break;


        case DBUS_TYPE_STRUCT :      // '(', STRUCT - begin ("r")
            {
#ifdef DEBUG_STRUCT
    fprintf(stderr, "*** unmarshal(...): *** STRUCT - begin !\n");
#endif

                // determine elementStart and elementEnd
                pmarsh->elementStart=pmarsh->cursor;

                char *tmpCursor=pmarsh->cursor+1; // point to char immediately following '('
                char   chOpen = '(';
                char   chEnd  = ')';               // end character to look for

                size_t open=1;
                while (open>0 && *tmpCursor)   // as long as not closed and not end of string
                {
                    if (*(++tmpCursor)==chOpen)
                    {
                        open++;                 // opening (another of same type)
                    }
                    else if (*tmpCursor==chEnd)
                    {
                        open--;                 // closing
                    }
                }

                        // determine signature to use for opening a containter
#ifdef MALLOC_USE_REXX
                char *localSignature=(char *) RexxAllocateMemory(pmarsh->signatureLength+1);  // that should be enough
                memset(localSignature, 0, pmarsh->signatureLength+1);
#else
                char *localSignature=(char *) dbus_malloc0(pmarsh->signatureLength+1);  // that should be enough
#endif

                strncpy(localSignature, pmarsh->cursor+1, tmpCursor - pmarsh->cursor - 1);

                STRUCT_MARSHAL localM;
                SET_NEW_STRUCT_MARSHAL (&localM, localSignature, pmarsh);   // initialize new STRUCT_MARSH
                localM.elementStart=localM.signature;
                localM.elementEnd=localM.signature+localM.signatureLength-1;

#ifdef DEBUG_STRUCT
   fprintf(stderr, "... unmarshal(...): *** STRUCT: BEFORE - localM.signature=[%s], localM.cursor=[%s], localM.elementStart=[%s], localM.elementEnd=[%s]\n",
                        localM.signature, localM.cursor, localM.elementStart, localM.elementEnd);

   fprintf(stderr, "... unmarshal(...): *** STRUCT: BEFORE - localM.signature=[%s], localM.cursor=[%lu], localM.elementStart=[%lu], localM.elementEnd=[%lu]\n",
                        localM.signature,
                        (unsigned long) (localM.cursor - localM.signature),
                        (unsigned long) (localM.elementStart - localM.signature),
                        (unsigned long) (localM.elementEnd - localM.signature)
                        );
#endif

                    // do the unmarshalling
                currRexxObj=pmarsh->context->NewArray(0);       // the member of a struct are stored in an array in sequence
                unmarshalStruct(iter, (RexxArrayObject) currRexxObj, &localM);

                    // move cursor to next char in signature (after struct)
                pmarsh->cursor=tmpCursor+1;

#ifdef DEBUG_STRUCT
   fprintf(stderr, "... unmarshal(...): *** STRUCT: AFTER - pmarsh->signature=[%s], pmarsh->cursor=[%s], pmarsh->elementStart=[%s], pmarsh->elementEnd=[%s]\n",
                        pmarsh->signature, pmarsh->cursor, pmarsh->elementStart, pmarsh->elementEnd);

   // fprintf(stderr, "... unmarshal(...): *** STRUCT: AFTER - pmarsh->signature=[%s], pmarsh->cursor=[%zu], pmarsh->elementStart=[%zu], pmarsh->elementEnd=[%zu]\n",
   fprintf(stderr, "... unmarshal(...): *** STRUCT: AFTER - pmarsh->signature=[%s], pmarsh->cursor=[%lu], pmarsh->elementStart=[%lu], pmarsh->elementEnd=[%lu]\n",
                        pmarsh->signature,
                        (unsigned long) (pmarsh->cursor - pmarsh->signature),
                        (unsigned long) (pmarsh->elementStart - pmarsh->signature),
                        (unsigned long) (pmarsh->elementEnd - pmarsh->signature)
                        );
#endif


#ifdef MALLOC_USE_REXX
                RexxFreeMemory(localSignature);  // that should be enough
#else
                dbus_free(localSignature);
#endif

            }
            break;


        case 'v' :      // VARIANT
            {
#ifdef DEBUG_VARIANT
    fprintf(stderr, "*** unmarshal(...): *** VARIANT - begin !\n");
#endif
                currRexxObj=pmarsh->context->NewArray(0);       // the member of a struct are stored in an array in sequence
                unmarshalVariant(iter, (RexxArrayObject) currRexxObj, pmarsh);
                currRexxObj=pmarsh->context->ArrayAt((RexxArrayObject) currRexxObj,1);

                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;


        case DBUS_TYPE_DICT_ENTRY :      // '{', DICT_ENTRY - begin ("e"): invoked via unmarshalArray(...)
            {
                fprintf(stderr, "%s: *** unmarshal(...): DICT ENTRY - begin: unmarshal() - ERROR !! SHOULD NEVER ARRIVE HERE!\n", DLLNAME);
                pmarsh->cursor=pmarsh->elementEnd+2;       // let us point to the next typeCode if any
            }
            break;

        default:
    fprintf(stderr, "%s: *** unmarshal(...): ??? default-branch ???: SHOULD NEVER ARRIVE HERE!\n", DLLNAME);
             break;
        }

            // now save current Rexx object for Rexx result
        pmarsh->context->SendMessage1(rxResult, "APPEND", currRexxObj);     // append current Rexx object to array

#ifdef DEBUG_UNMARSHAL
    fprintf(stderr, "\nrxResult=[%p], currRexxObj=[%p], currRexxObj~class=[%s], size=[%lu]\n",
                   rxResult,
                   currRexxObj,
                   (currRexxObj==NULL ? NULL : pmarsh->context->ObjectToStringValue(pmarsh->context->SendMessage0(currRexxObj, "CLASS"))),
                   (unsigned long) (currRexxObj==NULL ? 0 : (pmarsh->context->IsArray(currRexxObj) ? pmarsh->context->ArraySize((RexxArrayObject) currRexxObj) : 0 ))
         );
    fprintf(stderr, "... end of unmarshal(), rxResult [%p] appending currRexxObj=[%p]=[%s]: dims=[%lu], items=[%lu], size=[%lu]\n",
                   rxResult,
                   currRexxObj,
                   pmarsh->context->ObjectToStringValue(currRexxObj),
                   (unsigned long) pmarsh->context->ArrayDimension((RexxArrayObject) rxResult),
                   (unsigned long) pmarsh->context->ArrayItems((RexxArrayObject) rxResult),
                   (unsigned long) pmarsh->context->ArraySize((RexxArrayObject) rxResult)
        );
#endif

        pmarsh->context->ReleaseLocalReference(currRexxObj);

            // check for errors, return with false, if any
        pmarsh->dbusError=(resDbus==0);     // a dbus-error occurred ?
        pmarsh->rexxError=(orxConvFlag==false); // a Rexx conversion error occurred ?

        if (pmarsh->dbusError || pmarsh->rexxError)
        {
            if (pmarsh->dbusError)      // give a textual hint/reason for user
            {
                SNPRINTF(pmarsh->msgChunk256, 256, ": DBus-API returned 'no memory' while appending message argument");
            }
            return false;
        }

        dbus_message_iter_next (iter);  // move to next argument
    }

    return true;    // unmarshalling worked
}



// --------------------------------------------------------------------------------
// unmarshall an array
/** Code to unmarshall a DBus array.
*
* @param iter the DBus iteration structure
* @param level recursion level
* @param rao the Rexx array to hold the converted values
* @param arrIdx the Rexx array containing the index values to use
* @param pmarsh the structure that controls unmarshalling
* @return <code>true</code> if unmarshalling was carried out without errors, <code>false</code> else
*
*/
logical_t unmarshalArray(DBusMessageIter *iter, size_t level, RexxArrayObject rao, RexxArrayObject arrIdx, PSTRUCT_MARSHAL pmarsh)
{
#ifdef DEBUG_ARRAY
       fprintf(stderr, "---> arrived in unmarshalArray(...): level=[%lu], rao=[%s], rao~items=[%lu], arrIdx=[%s]\n",
                         (unsigned long) level,
                         ARR_AS_STRING(rao, pmarsh->context),
                         pmarsh->context->ArrayItems(rao),
                         ARR_AS_STRING(arrIdx, pmarsh->context) );

       fprintf(stderr, "                unmarshalArray(...): signature=[%s], dims=[%lu] - cursor=[%lu], arrStart=[%lu], arrEnd=[%lu], elStart=[%lu], elEnd=[%lu]\n",
                         pmarsh->signature,
                         (unsigned long) pmarsh->dimensions,
                         (unsigned long) (pmarsh->cursor   - pmarsh->signature    ) ,
                         (unsigned long) (pmarsh->arrStart - pmarsh->signature    ) ,
                         (unsigned long) (pmarsh->arrEnd   - pmarsh->signature    ) ,
                         (unsigned long) (pmarsh->elementStart - pmarsh->signature) ,
                         (unsigned long) (pmarsh->elementEnd   - pmarsh->signature)
           );
#endif

    DBusMessageIter iter_array;

#ifdef DEBUG_ARRAY
  fprintf(stderr, "                unmarshalArray(...): 0! 0! 0!  \n\t--> ");

  #if defined (DEBUG_STRUCT_MARSHAL_PRINT)
    dump_struct_marshal(pmarsh);
  #endif
  fflush(stderr);
  fprintf(stderr, "                unmarshalArray(...): 1! 1! 1!  pmarsh->signatureLength+1=[%d] - before allocate memory\n",
                                     (int) pmarsh->signatureLength+1); fflush(stderr);
#endif

        // determine signature to use for opening a container
#ifdef MALLOC_USE_REXX
    char *localSignature=(char *) RexxAllocateMemory(pmarsh->signatureLength+1);  // that should be enough
    memset(localSignature, 0, pmarsh->signatureLength+1);
#else
    char *localSignature=(char *) dbus_malloc0(pmarsh->signatureLength+1);  // that should be enough
#endif

    char *start=pmarsh->arrStart+1;

#ifdef DEBUG_ARRAY
  size_t  brr=pmarsh->elementEnd-start+1;
  fprintf(stderr, "                unmarshalArray(...): 2! 2! 2! start=[%p]/*start=[%c], pmarsh->elementEnd-start+1=[%ld], brr=[%ld] - before strncpy\n",
                                   start, start==NULL ? '\0' : *start,
                                   (long)(pmarsh->elementEnd-start+1), brr);
  fflush(stderr);
#endif

    strncpy(localSignature, start, pmarsh->elementEnd-start+1 );

#ifdef DEBUG_ARRAY
    fprintf(stderr, "     - level=[%lu], strncpy(): localSignature=[%s], rao=[%p] \n", (unsigned long) level, localSignature, rao); fflush(stderr);
    fprintf(stderr, "    unmarshalArray(...): OPEN container ---> ---> ---> signature=[%s]\n", localSignature);
#endif

        // open array
    dbus_message_iter_recurse(iter, &iter_array);       // open container

    // set this dimension's index appropriately
    size_t items=GET_UNMARSHAL_ARRAY_VALUE_AT_INDEX(rao, level, pmarsh->context); // get maximum number of items in this dimension

    SET_ARRAY_IDX(arrIdx, level, items+1, pmarsh->context); // add 1 to maximum items

        // if array container is empty, then return
    if (dbus_message_iter_get_arg_type(&iter_array)==DBUS_TYPE_INVALID)
    {
#ifdef DEBUG_ARRAY
    fprintf(stderr, "    [unmarshalArray(...): container with localSignature=[%s] is empty, returning...]\n", localSignature);
#endif
        if (*localSignature=='{')    // a dict-entry signature?
        {
#ifdef DEBUG_ARRAY
    fprintf(stderr, "    [unmarshalArray(...): putting an empty directory object for return...]\n");
#endif
            // create an empty directory, append it, return
            RexxDirectoryObject rdo=pmarsh->context->NewDirectory();
            pmarsh->context->SendMessage2(rao, "PUT", rdo, arrIdx);  // save "ay" as "s" at its position
            pmarsh->context->ReleaseLocalReference(rdo);
        }


#ifdef MALLOC_USE_REXX
        RexxFreeMemory(localSignature);  // that should be enough
#else
        dbus_free(localSignature);
#endif
        return true;  // this argument handled in full already
    }


       // handle special case "ay"->"s" (shall the byte array be automatically unmarshalled as a Rexx string?)
    if (pmarsh->is_ay && level==pmarsh->dimensions) // "ay" -> "s"
    {
#ifdef DEBUG_ARRAY
   fprintf(stderr, "    unmarshalArray(...): BYTE-ARRAY-HANDLING ! localSignature=[%s], p->signature=[%s], level=[%lu], p->dimensions=[%lu], p->is_ay=[%d], p->unmarshal_ay_as_string=[%d]\n",
                        localSignature,
                        pmarsh->signature,
                        (unsigned long) level,
                        (unsigned long) pmarsh->dimensions,
                        (int) pmarsh->is_ay,
                        (int) pmarsh->unmarshal_ay_as_string
                        );

#endif
        int  arrCount=0;
        char *array;                // will receive pointer to byte array

            // determine number of indices
        size_t nrDims=pmarsh->dimensions-pmarsh->unmarshal_ay_as_string;

        RexxObjectPtr tmpObj=pmarsh->context->Nil();    // default to .nil

        dbus_message_iter_get_fixed_array (&iter_array, &array, &arrCount);

#ifdef DEBUG_ARRAY
   fprintf(stderr, "    unmarshalArray(...): arrCount=[%d], arrIdx=[%s],  nrDims=[%lu], level=[%lu] \n",
                             arrCount,
                             ARR_AS_STRING(arrIdx, pmarsh->context),
                             (unsigned long) nrDims,
                             (unsigned long) level
                        );

   fprintf(stderr, "    unmarshalArray(...): arrIdx~size=[%lu], nrDims=[%lu] \n",
                             (unsigned long) pmarsh->context->ArraySize(arrIdx),
                             (unsigned long) nrDims );


#endif
            // adjust index array (to not have too many dimensions!)
        if (pmarsh->context->ArraySize(arrIdx)>nrDims)
        {
            RexxObjectPtr rop1=pmarsh->context->String("1");
            RexxObjectPtr rop2=pmarsh->context->StringSizeToObject(nrDims);
            RexxArrayObject tmpRao=(RexxArrayObject) pmarsh->context->SendMessage2(arrIdx, "SECTION", rop1, rop2);

            pmarsh->context->ReleaseLocalReference(arrIdx);
            arrIdx=tmpRao;

            pmarsh->context->ReleaseLocalReference(rop1);
            pmarsh->context->ReleaseLocalReference(rop2);

#ifdef DEBUG_ARRAY
   fprintf(stderr, "    unmarshalArray(...): must adjust arrIdx (too many indices!), now arrIdx=[%s] as nrDims=[%lu] \n ",
                  ARR_AS_STRING(arrIdx, pmarsh->context), (unsigned long) nrDims);
#endif
        }


        if (pmarsh->dimensions==1)  // special case: it is truly a single dimensioned byte array ('ay')
        {
            SET_ARRAY_IDX(arrIdx, 1, 1, pmarsh->context); // set index at this dimension to 1
        }

        if (arrCount>0)         // byte array was read
        {
            if (tmpObj!=NULLOBJECT)
            {
                pmarsh->context->ReleaseLocalReference(tmpObj);
            }
            if (pmarsh->is_ay && pmarsh->unmarshal_ay_as_string) // turn byte array into string
            {
                tmpObj=pmarsh->context->String(array, arrCount); // turn into Rexx string
                pmarsh->context->SendMessage2(rao, "PUT", tmpObj, arrIdx);  // save "ay" as "s" at its position
                pmarsh->context->ReleaseLocalReference(tmpObj);

            }
            else    // create a Rexx array with individual bytes
            {
                for (int i=1; i<=arrCount; i++)
                {
                    SET_ARRAY_IDX(arrIdx, nrDims, (size_t) i, pmarsh->context);   // set index at this dimension to 1

#ifdef DEBUG_ARRAY
   fprintf(stderr, "    unmarshalArray(...): as BYTE-ARRAY, arrIdx=[%s] \n", ARR_AS_STRING(arrIdx, pmarsh->context));
#endif
                    tmpObj=pmarsh->context->String((CSTRING)array+i-1, 1);      // extract byte and turn it into a Rexx string
                    pmarsh->context->SendMessage2(rao, "PUT", tmpObj, arrIdx);  // save "ay" as "s" at its position
                    pmarsh->context->ReleaseLocalReference(tmpObj);
                }
            }
        }

#ifdef MALLOC_USE_REXX
        RexxFreeMemory(localSignature);  // that should be enough
#else
        dbus_free(localSignature);
#endif
        return true;  // this argument handled in full already
    }

        // create a new copy of the STRUCT_MARSHAL to isolate the caller's one from changes in marshal(...)
    STRUCT_MARSHAL localM;
    SET_NEW_STRUCT_MARSHAL (&localM, localSignature, pmarsh);   // initialize new STRUCT_MARSH
    localM.elementStart=localM.signature;
    localM.elementEnd=localM.signature+localM.signatureLength-1;
    localM.dimensions=pmarsh->dimensions;   // keep original dimensions in all recursive invocations to eventually match the recurse level

        // maintain array data for current (local) signature
    if (*localM.elementStart=='a')   // still an array, calculate respective fields
    {
#ifdef DEBUG_ARRAY
   fprintf(stderr, " ............... analyze signature, set arrStart/arrEnd  elStart/elEnd\n");
#endif
        localM.arrStart=localM.signature;
        localM.arrEnd  =localM.signature;

        while (*(localM.arrEnd+1)=='a')     // determine position of ending 'a' char
        {
            localM.arrEnd++;
        }

#ifdef DEBUG_ARRAY
    fprintf(stderr, " ............... analyze signature, set arrStart/arrEnd  elStart/elEnd, dims=[%lu], calcDims=[%lu], lM.signature=[%s], lm.arrStart=[%c], lm.arrEnd=[%c]\n",
                  (unsigned long) localM.dimensions,
                  (unsigned long) (localM.arrEnd - localM.arrStart + 1),
                  localM.signature,
                  *localM.arrStart,
                  *localM.arrEnd
                  );
#endif

    }

#ifdef DEBUG_ARRAY
   fprintf(stderr, "      ... unmarshalArray(): DBUS_TYPE_ARRAY - signature=[%s], level=[%ld], p->dimensions=[%ld] ...\n",
                                   pmarsh->signature, (long int) level, (long int) pmarsh->dimensions);
                                   fflush(stderr);
#endif


    if (level==pmarsh->dimensions)      // o.k. arrived at element
    {
        if (*localSignature=='{')            // an array of DICTs
        {
            // a dict (directory) to process !
#ifdef DEBUG_ARRAY
   fprintf(stderr, "     /--> unmarshalArray(): DBUS_TYPE_ARRAY - signature=[%s], branch 'DICT ARRAY' ...\n", pmarsh->signature); fflush(stderr);
#endif
            RexxDirectoryObject dirRexxObj=pmarsh->context->NewDirectory();

                // unmarshal, if not empty
            if (dbus_message_iter_get_arg_type (&iter_array) != DBUS_TYPE_INVALID)
            {
#if defined (DEBUG_DICT) && defined (DEBUG_UNMARSHAL)
                fprintf(stderr, "\n     /--> unmarshalArray(): ===> before calling unmarshalDict(): ===> localM\n\t--> ");
  #if defined (DEBUG_STRUCT_MARSHAL_PRINT)
     dump_struct_marshal(&localM);
  #endif
                fflush(stderr);
#endif

                unmarshalDict(&iter_array, dirRexxObj, &localM);

#if defined (DEBUG_DICT) && defined (DEBUG_UNMARSHAL)
                fprintf(stderr, "     /--> unmarshalArray(): <=== AFTER  calling unmarshalDict(): <=== localM \n\t--> ");
  #if defined (DEBUG_STRUCT_MARSHAL_PRINT)
     dump_struct_marshal(&localM);
  #endif
                fprintf(stderr,"\n");
                fflush(stderr);
#endif
            }

#ifdef DEBUG_ARRAY
    fprintf(stderr, "     /<-- unmarshalArray(): DBUS_TYPE_ARRAY - signature=[%s], branch 'DICT ARRAY', dirRexxObj~itesm=[%s] \n",
               pmarsh->signature,
               pmarsh->context->ObjectToStringValue(pmarsh->context->SendMessage0(dirRexxObj, "ITEMS") )
               ); fflush(stderr);
#endif

            SET_ARRAY_IDX(arrIdx, level, 1, pmarsh->context); // add 1 to maximum items
            pmarsh->context->SendMessage2(rao, "PUT", dirRexxObj, arrIdx);
            pmarsh->context->ReleaseLocalReference(dirRexxObj);

#ifdef DEBUG_ARRAY
    fprintf(stderr, "     /<-- unmarshalArray(): DBUS_TYPE_ARRAY - dirRexxObj=[%p] stored at arrIdx=[%s], branch 'DICT ARRAY', done. \n",
                      dirRexxObj, ARR_AS_STRING(arrIdx, pmarsh->context));
#endif

            return true;    // done, return

        }
        else
        {
            size_t intIdx=0;                // start out with index at 1

            while (dbus_message_iter_get_arg_type(&iter_array)!=DBUS_TYPE_INVALID)
            {
                intIdx++;                   // increase index pointer
                SET_ARRAY_IDX(arrIdx, level, intIdx, pmarsh->context); // add 1 to maximum items

                RexxArrayObject tmpArr=pmarsh->context->NewArray(0); // NULL;

#ifdef DEBUG_ARRAY
         CSTRING currArrIdx=ARR_AS_STRING(arrIdx, pmarsh->context);
#endif

                unmarshal(&iter_array, *localSignature, tmpArr, &localM);       // assuming 'rop' will get built in called function

#ifdef DEBUG_ARRAY
    fprintf(stderr, "----> Ticker # 7b, about to put value=[%s] at arrIdx=[%s] \n", pmarsh->context->ObjectToStringValue(pmarsh->context->ArrayAt(tmpArr,1)), currArrIdx);
#endif

                RexxObjectPtr rop=pmarsh->context->ArrayAt(tmpArr,1);
                pmarsh->context->SendMessage2(rao, "PUT", rop, arrIdx);

#ifdef DEBUG_ARRAY
    fprintf(stderr, "----> Ticker # 7c, stored value [%s] at arrIdx=[%s] \n",
                           pmarsh->context->ObjectToStringValue(pmarsh->context->SendMessage1(rao,"AT",arrIdx)),
                           currArrIdx
                           );

    fprintf(stderr, "----> Ticker # 7d, signature=[%s], cursor=[%lu], elementStart=[%lu] \n",
                    localM.signature,
                    (unsigned long) (localM.cursor - localM.signature),
                    (unsigned long) (localM.elementStart - localM.signature) );
#endif

                pmarsh->context->ReleaseLocalReference(rop);
                pmarsh->context->ReleaseLocalReference(tmpArr);

                localM.cursor=localM.signature;     // reset cursor
            }
        }
    }
    else        // recurse into next dimension!
    {
#ifdef DEBUG_ARRAY
    fprintf(stderr, "----> Ticker # 8-, level=[%lu], rao=[%p], arrIdx=[%s], &iter_array(arg_type=[%d])==[%d] (DBUS_ARRAY_TYPE) \n",
                              (unsigned long) level, rao, ARR_AS_STRING(arrIdx, pmarsh->context),
                              dbus_message_iter_get_arg_type(&iter_array), DBUS_TYPE_ARRAY
                              );
#endif

        size_t indexLevel=1;    // start out with this index value
        while (dbus_message_iter_get_arg_type(&iter_array)!=DBUS_TYPE_INVALID)
        {
            SET_ARRAY_IDX(arrIdx, level, indexLevel, pmarsh->context); // reset next higher dimension's index !
            SET_ARRAY_IDX(arrIdx, level+1, 0, pmarsh->context); // reset next higher dimension's index !

#ifdef DEBUG_ARRAY
    fprintf(stderr, "----> Ticker # 8a, recursing with values, level+1=[%lu], rao=[%p], arrIdx=[%s] \n", (unsigned long) level+1, rao, ARR_AS_STRING(arrIdx, pmarsh->context));
#endif

            unmarshalArray(&iter_array, level+1, rao, arrIdx, &localM);

#ifdef DEBUG_ARRAY
    fprintf(stderr, "<---- Ticker # 8b, current level again: [%lu] \n", (unsigned long) level);
#endif
            indexLevel++;
            // reset next dimension's item count value to 0
            dbus_message_iter_next(&iter_array);
        }
#ifdef DEBUG_ARRAY
    fprintf(stderr, "<---- Ticker # 9, back at level [%lu] \n", (unsigned long) level);
#endif
    }

#ifdef MALLOC_USE_REXX
    RexxFreeMemory(localSignature);  // that should be enough
#else
    dbus_free(localSignature);
#endif
    return true;  // this argument handled in full already
}



// -----------------------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------------------
// unmarshall a dict
/** Unmarshall a dictionary (a map).
*
* @param iter_array the DBus iteration structure
* @param rxMap the Rexx directory object to hold the converted values
* @param pmarsh the structure that controls unmarshalling
* @return <code>true</code> if unmarshalling was carried out without errors, <code>false</code> else
*/
logical_t unmarshalDict(DBusMessageIter *iter_array, RexxDirectoryObject rxMap, PSTRUCT_MARSHAL pmarsh)
{

#if defined (DEBUG_DICT) && defined (DEBUG_UNMARSHAL)
   fprintf(stderr, "---> ARRIVED in unmarshalDict(...), rxMap=[%p], signature=[%s] \n", rxMap, pmarsh->signature);
   fprintf(stderr, "                =-> unmarshalDict(...): 8! 8! 8!  \n\t--> ");

  #if defined (DEBUG_STRUCT_MARSHAL_PRINT)
     dump_struct_marshal(pmarsh);
  #endif
   fflush(stderr);
#endif


        // determine signature to use for opening a containter, must include brackets !
#ifdef MALLOC_USE_REXX
    char *localKeySignature=(char *) RexxAllocateMemory(pmarsh->signatureLength+1);  // that should be enough
    memset(localKeySignature, 0, pmarsh->signatureLength+1);

    char *localItemSignature=(char *) RexxAllocateMemory(pmarsh->signatureLength+1);  // that should be enough
    memset(localItemSignature, 0, pmarsh->signatureLength+1);
#else
    char *localKeySignature=(char *) dbus_malloc0(pmarsh->signatureLength+1);  // that should be enough
    char *localItemSignature=(char *) dbus_malloc0(pmarsh->signatureLength+1);  // that should be enough
#endif

        // define localKeySignature:
    *localKeySignature=*(pmarsh->signature+1);  // save simple type code as whole signature

        // use signature withing parenthesis
    strncpy(localItemSignature, pmarsh->elementStart+2,         // skip '{' and simple type code for key
                pmarsh->elementEnd - pmarsh->elementStart -2    // length
                );

#if defined (DEBUG_DICT) && defined (DEBUG_UNMARSHAL)
    fprintf(stderr, "     unmarshalDict() - signature=[%s] -> localKeySignature=[%s] & localItemSignature=[%s]\n",
                          pmarsh->signature, localKeySignature, localItemSignature); fflush(stderr);
#endif

        // create a new copy of the DICT_MARSHAL to isolate the caller's one from changes in marshal(...)
    STRUCT_MARSHAL localKeyM;
    SET_NEW_STRUCT_MARSHAL (&localKeyM, localKeySignature, pmarsh);   // initialize new DICT_MARSH
        // do not include brackets
    localKeyM.elementStart=localKeySignature;
    localKeyM.elementEnd  =localKeySignature+localKeyM.signatureLength-1;
    localKeyM.cursor=localKeyM.elementStart;  // let cursor point to element's start

    STRUCT_MARSHAL localItemM;
    SET_NEW_STRUCT_MARSHAL (&localItemM, localItemSignature, pmarsh);   // initialize new DICT_MARSH
        // do not include brackets
    localItemM.elementStart=localItemSignature;
    localItemM.elementEnd  =localItemSignature+localItemM.signatureLength-1;
    localItemM.cursor=localItemM.elementStart;  // let cursor point to element's start

#if defined (DEBUG_DICT) && defined (DEBUG_UNMARSHAL)
   fprintf(stderr, "ooo 000 ooo --> localItemM: cursor=[%p]/[%c], signature=[%p]/[%c], arrStart=[%p]/[%c], elStart=[%p]/[%c] elEnd=[%p]/[%c] <-- *** *** ***\n",
                localItemM.cursor,
                *localItemM.cursor,
                localItemM.signature,
                *localItemM.signature,
                localItemM.arrStart==NULL ? NULL :localItemM.arrStart,
                localItemM.arrStart==NULL ? '\0' :*localItemM.arrStart,
                localItemM.elementStart,
                *localItemM.elementStart,
                localItemM.elementEnd,
                *localItemM.elementEnd
                ); fflush(stderr);
#endif

    // loop over iter_array unmarshal key and unmarshal value to store with directory
#if defined (DEBUG_DICT) && defined (DEBUG_UNMARSHAL)
  size_t tmpI=0;
#endif
    DBusMessageIter iter_dict;
    while (dbus_message_iter_get_arg_type (iter_array) != DBUS_TYPE_INVALID)
    {
#if defined (DEBUG_DICT) && defined (DEBUG_UNMARSHAL)
  tmpI++;
  fprintf(stderr, "             unmarshalDict(...), while (top) iteration #=[%lu], before RECURSING into iter_dict...\n",tmpI); fflush(stderr);
  #if defined (DEBUG_STRUCT_MARSHAL_PRINT)
     dump_struct_marshal(pmarsh);
  #endif
  fflush(stderr);

#endif
        dbus_message_iter_recurse (iter_array, &iter_dict);
        int dtype=0;  // rgf, 20140727, debug
        if ((dtype=dbus_message_iter_get_arg_type (&iter_dict)) != DBUS_TYPE_INVALID)
        {
            RexxArrayObject key=pmarsh->context->NewArray(0);
            localKeyM.cursor=localKeyM.signature;     // let cursor point to its signature start

#if defined (DEBUG_DICT) && defined (DEBUG_UNMARSHAL)
      char * localSign=dbus_message_iter_get_signature(&iter_dict);
      fprintf(stderr, "             unmarshalDict(...), #-1 localKeySignature=[%s], localSignatureByDbus=[%s] ==> localKeyM ",
                                    localKeySignature, localSign);
  #if defined (DEBUG_STRUCT_MARSHAL_PRINT)
     dump_struct_marshal(&localKeyM);
  #endif
      fflush(stderr);
      free(localSign);
#endif
            unmarshal(&iter_dict, *localKeySignature, key, &localKeyM); // process

            RexxArrayObject item=pmarsh->context->NewArray(0);
            localItemM.cursor=localItemM.signature;  // let cursor point to its signature start

#if defined (DEBUG_DICT) && defined (DEBUG_UNMARSHAL)
      localSign=dbus_message_iter_get_signature(&iter_dict);
      fprintf(stderr, "             unmarshalDict(...), #-2 localItemSignature=[%s], localSignatureByDbus=[%s] ==> localItemM ",
                                    localItemSignature, localSign);
  #if defined (DEBUG_STRUCT_MARSHAL_PRINT)
     dump_struct_marshal(&localItemM);
  #endif

      fflush(stderr);
      free(localSign);
#endif
            unmarshal(&iter_dict, *localItemSignature, item, &localItemM); // process

#if defined (DEBUG_DICT) && defined (DEBUG_UNMARSHAL)
    fprintf(stderr, "     unmarshalDict() - about to add dict entry: key=[%s] -> item=[%s] ///---\\\\\\ ...\n",
                          pmarsh->context->ObjectToStringValue(pmarsh->context->ArrayAt(key,1)),
                          pmarsh->context->ObjectToStringValue((pmarsh->context->ArrayItems(item)==1 ?
                                                  pmarsh->context->ArrayAt(item,1) :
                                                  item )
                                                               )
                          );
    fflush(stderr);
#endif
            // add to directory
            RexxObjectPtr ropKey=pmarsh->context->ArrayAt(key,1);
            if (pmarsh->context->ArrayItems(item)==1)
            {
                RexxObjectPtr ropItem=pmarsh->context->ArrayAt(item,1);
                pmarsh->context->SendMessage2(rxMap, "PUT", ropItem, ropKey);
                pmarsh->context->ReleaseLocalReference(ropItem);
            }
            else
            {
                pmarsh->context->SendMessage2(rxMap, "PUT", item, ropKey);
            }

            pmarsh->context->ReleaseLocalReference(key);
            pmarsh->context->ReleaseLocalReference(item);
            pmarsh->context->ReleaseLocalReference(ropKey);
        }

#if defined (DEBUG_DICT) && defined (DEBUG_UNMARSHAL)
        fprintf(stderr, "     unmarshalDict() - dbus_type=[%d], tmpI=[%lu] -> ",
                              dtype,tmpI);
        fflush(stderr);

        switch (dtype)
        {
        case DBUS_TYPE_ARRAY:
            fprintf(stderr, "DBUS_TYPE_ARRAY");
            break;
        case DBUS_TYPE_DICT_ENTRY:
            fprintf(stderr, "DBUS_TYPE_DICT_ENTRY");
            break;

        case DBUS_TYPE_STRUCT:
            fprintf(stderr, "DBUS_TYPE_STRUCT");
            break;

        case DBUS_TYPE_STRING:
            fprintf(stderr, "DBUS_TYPE_STRING");
            break;

        case DBUS_TYPE_VARIANT:
            fprintf(stderr, "DBUS_TYPE_VARIANT");
            break;

        case DBUS_TYPE_INVALID:
            fprintf(stderr, "DBUS_TYPE_INVALID !!!! <===> !!!!");
            break;
        default:
            fprintf(stderr, "DBUS_TYPE NOT in switch statement!");
        }

  #if defined (DEBUG_STRUCT_MARSHAL_PRINT)
     dump_struct_marshal(pmarsh);
  #endif
        fflush(stderr);
        fprintf(stderr,"\n");fflush(stderr);
#endif


        dbus_message_iter_next(iter_array);      // move to next element
    }


#ifdef MALLOC_USE_REXX
    RexxFreeMemory(localKeySignature);
    RexxFreeMemory(localItemSignature);
#else
    dbus_free(localKeySignature);
    dbus_free(localItemSignature);
#endif

#if defined (DEBUG_DICT) && defined (DEBUG_UNMARSHAL)
   fprintf(stderr, "<--- leaving unmarshalDict(...), # of iterations=[%lu].\n",tmpI); fflush(stderr);
#endif

    return true;
}


// ---------------------------------------------------------------------------------------------------------
// unmarshalStruct()
// - expects: elementStart to point at one after opening '(' and elementEnd at one before closing ')' !
/** Unmarshall a structure (elements stored in an array). Expects <code>elementStart</code> to point at one char
*   after the structure opening char <code>'('</code> and <code>elementEnd</code> at one before the closing char
*   <code>')'</code> in the <code>pmarsh</code> structure.
*
* @param iter the DBus iteration structure
* @param rao the Rexx array object to hold the converted values
* @param pmarsh the structure that controls unmarshalling
* @return <code>true</code> if unmarshalling was carried out without errors, <code>false</code> else
*/

logical_t unmarshalStruct(DBusMessageIter *iter, RexxArrayObject rao, PSTRUCT_MARSHAL pmarsh)
{

#ifdef DEBUG_STRUCT
   fprintf(stderr, "---> arrived in unmarshalStruct(...), rao=[%p], signature=[%s] \n", rao, pmarsh->signature);
#endif


        // determine signature to use for opening a containter
#ifdef MALLOC_USE_REXX
    char *localSignature=(char *) RexxAllocateMemory(pmarsh->signatureLength+1);  // that should be enough
    memset(localSignature, 0, pmarsh->signatureLength+1);
#else
    char *localSignature=(char *) dbus_malloc0(pmarsh->signatureLength+1);  // that should be enough
#endif

        // use signature withing parenthesis
    strncpy(localSignature, pmarsh->elementStart,               // start position
                pmarsh->elementEnd - pmarsh->elementStart +1    // length
                );

#ifdef DEBUG_STRUCT
    fprintf(stderr, "     unmarshalStruct(...): - localSignature=[%s]\n", localSignature); fflush(stderr);
#endif

    DBusMessageIter iter_struct;
    dbus_message_iter_recurse(iter, &iter_struct);       // open container

        // create a new copy of the STRUCT_MARSHAL to isolate the caller's one from changes in marshal(...)
    STRUCT_MARSHAL localM;
    SET_NEW_STRUCT_MARSHAL (&localM, localSignature, pmarsh);   // initialize new STRUCT_MARSH
    localM.elementStart=localM.signature;
    localM.elementEnd=localM.signature+localM.signatureLength-1;

    unmarshal(&iter_struct, *localSignature, rao, &localM); // process

#ifdef DEBUG_VARIANT_2
   fprintf(stderr, "     unmarshalStruct(...): before return: signature=[%s]\n", localSignature);
#endif

#ifdef MALLOC_USE_REXX
    RexxFreeMemory(localSignature);  // that should be enough
#else
    dbus_free(localSignature);
#endif

#ifdef DEBUG_VARIANT_2
   fprintf(stderr, "     unmarshalStruct(...): about to return <--- <--- <---\n");
#endif

    return true;
}




// -----------------------------------------------------------------------------------------------------
// unmarshalVariant()
/** Unmarshals a variant.
*
* @param iter the DBus iteration structure
* @param rop the Rexx array object to hold the converted values
* @param pmarsh the structure that controls unmarshalling
* @return <code>true</code> if unmarshalling was carried out without errors, <code>false</code> else
*/
logical_t unmarshalVariant(DBusMessageIter *iter, RexxArrayObject rop, PSTRUCT_MARSHAL pmarsh)
{

#ifdef DEBUG_VARIANT
   fprintf(stderr, "---> arrived in unmarshalVariant(...), rop=[%p], signature=[%s] \n", rop, pmarsh->signature);
#endif

    DBusMessageIter iter_variant;
    dbus_message_iter_recurse (iter, &iter_variant);
    char *localSignature=dbus_message_iter_get_signature(&iter_variant); // get variant's signature

#ifdef DEBUG_VARIANT
    fprintf(stderr, "     unmarshalVariant(...): - localSignature=[%s]\n", localSignature); fflush(stderr);
#endif

        // create a new copy of the DICT_MARSHAL to isolate the caller's one from changes in marshal(...)
    STRUCT_MARSHAL localM;
    SET_NEW_STRUCT_MARSHAL (&localM, localSignature, pmarsh);   // initialize new DICT_MARSH
    localM.elementStart=localSignature;
    localM.elementEnd  =localSignature+localM.signatureLength-1;
    localM.cursor=localM.elementStart;  // let cursor point to element's start

        // process variant
    unmarshal(&iter_variant, *localSignature, rop, &localM);

#ifdef DEBUG_VARIANT_2
   fprintf(stderr, "     unmarshalVariant(...): about to return <--- <--- <--- signature=[%s]\n", localSignature);
#endif


    dbus_free(localSignature);

    return true;
}



// --------------------------------------------------------------------------------
/** Entry point for marshalling.
*
* @param iter the DBus iteration structure
* @param typeCode the type code to use for marshalling
* @param rop the Rexx array containing the values to be marshalled
* @param pmarsh the structure that controls marshalling
* @return <code>true</code> if marshalling was carried out without errors, <code>false</code> else
*/

logical_t marshal(DBusMessageIter *iter, char typeCode, RexxObjectPtr rop, PSTRUCT_MARSHAL pmarsh)
{
#ifdef DEBUG_MARSHAL
    // fprintf(stderr, "---> arrived in marshal(...), typeCode=[%c], cursor=[%zu], signature=[%s], iter=[%p] \n",
    fprintf(stderr, "---> arrived in marshal(...), typeCode=[%c], cursor=[%lu], signature=[%s], iter=[%p] \n",
                       typeCode,
                       (unsigned long) (pmarsh->cursor - pmarsh->signature),
                       pmarsh->signature,
                       iter
                       );
#endif

    int       resDbus=1;            // "0": dbus-error (e.g. out of memory, wrong type, etc.)
    logical_t orxConvFlag=true;

    logical_t bRopIsNull= (rop==NULLOBJECT || (pmarsh->context->Nil()==rop)) ;  // a Rexx .nil, resp. NULLOBJECT in hands ?
    pmarsh->errorPosInSignature=pmarsh->cursor; // memorize cursor, in case we get an error

#ifdef DEBUG_MARSHAL_2
    fprintf(stderr, "        marshal() - before switch, typeCode=[%c], bRopIsNull=[%d], rop=[%p]\n", typeCode, (int) bRopIsNull, rop);
#endif

    switch (typeCode)
    {
    case 's' :      // STRING (UTF-8): must not contain nul bytes, but must end wiht a nul byte
        {
            if (bRopIsNull)
            {
                CSTRING cstr="";
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_STRING, &cstr);
#ifdef DEBUG_MARSHAL
   fprintf(stderr, "        marshall() - 's' - 1 (NULL) - typeCode=[%c], string value=[%s]\n", typeCode, cstr);
#endif
            }
            else
            {
                CSTRING   cstr=pmarsh->context->ObjectToStringValue(rop);
#ifdef DEBUG_MARSHAL
   fprintf(stderr, "        marshall() - 's' - 2 (rop!) - typeCode=[%c], string value=[%s]\n", typeCode, cstr);
#endif
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_STRING, &cstr);
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;

    case 'g' :      // SIGNATURE
        {
#ifdef DEBUG_MARSHAL_2
            fprintf(stderr, "        marshall() - 'g' - 1 \n");
#endif
            if (bRopIsNull)
            {
#ifdef DEBUG_MARSHAL_2
            fprintf(stderr, "        marshall() - 'g' - 1a \n");
#endif
                CSTRING cstr="";
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_SIGNATURE, &cstr);
            }
            else
            {
#ifdef DEBUG_MARSHAL_2
            fprintf(stderr, "        marshall() - 'g' - 1b \n");
#endif
                CSTRING   cstr=pmarsh->context->ObjectToStringValue(rop);
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_SIGNATURE, &cstr);
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;

    case 'o' :      // OBJECT_PATH (like STRING)
        {
#ifdef DEBUG_MARSHAL_2
            fprintf(stderr, "        marshall() - 'o' - 1 \n");
#endif
            if (bRopIsNull)
            {
#ifdef DEBUG_MARSHAL_2
            fprintf(stderr, "        marshall() - 'o' - 1a \n");
#endif
                CSTRING cstr="/";
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_OBJECT_PATH, &cstr);
            }
            else
            {
#ifdef DEBUG_MARSHAL_2
            fprintf(stderr, "        marshall() - 'o' - 1b \n");
#endif
                CSTRING   cstr=pmarsh->context->ObjectToStringValue(rop);

#ifdef DEBUG_MARSHAL_2
            fprintf(stderr, "        marshall() - 'o' - 1c [%s]\n",cstr); fflush(stderr);
#endif

                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_OBJECT_PATH, &cstr);
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;

    case 'y' :      // BYTE
        {
#ifdef DEBUG_MARSHAL_2
            fprintf(stderr, "        marshall() - 'y'  \n");
#endif
            if (bRopIsNull)
            {
#ifdef DEBUG_MARSHAL_2
            fprintf(stderr, "        marshall() - 'y' - 1a \n");
#endif
                char ch='\0';
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_BYTE, &ch);
            }
            else
            {
#ifdef DEBUG_MARSHAL_2
            fprintf(stderr, "        marshall() - 'y' - 1b \n");
#endif
                stringsize_t n;
                if (!pmarsh->context->IsOfType(rop, "STRING"))
                {
                    SNPRINTF(pmarsh->msgChunk256, 256, " not a string (value=[%.32s]), hence cannot convert to a byte value", pmarsh->context->ObjectToStringValue(rop));
                    orxConvFlag=false;
                    pmarsh->rexxError=true;
                    break;
                }

                // make sure "rop" is a "real" String object (and not a "hidden Integer" or "NumberString", such that StringLength() works reliably !
                rop=pmarsh->context->ObjectToString(rop);
                n=pmarsh->context->StringLength((RexxStringObject) rop);

                if (n==1)       // everything o.k. so far
                {
                    char ch = (pmarsh->context->CString(rop))[0];   // get first byte and turn it into a BYTE
                    resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_BYTE, &ch);
                    if (resDbus==0)
                    {
                        SNPRINTF(pmarsh->msgChunk256, 256, "%s", ": 'dbus_message_iter_append_basic(...)' returned '0', maybe no more DBUS memory");
                        pmarsh->dbusError=true;
                    }
                }
                else
                {
                    if (n==0)       // empty string
                    {

#ifdef _MSC_VER     // "%zu" creates runtime errors with "Microsoft (R) 32-bit C/C++ Optimizing Compiler Version 15.00.30729.01 for 80x86"
                        SNPRINTF(pmarsh->msgChunk256, 256, ": cannot convert an empty Rexx String to a byte value, string length: [%lu]", (unsigned long) n);
#else
                        SNPRINTF(pmarsh->msgChunk256, 256, ": cannot convert an empty Rexx String to a byte value, string length: [%zu]", n);
#endif
                        pmarsh->rexxError=true;
                    }
                    else            // too large of a string
                    {
#ifdef _MSC_VER     // "%zu" creates runtime errors with "Microsoft (R) 32-bit C/C++ Optimizing Compiler Version 15.00.30729.01 for 80x86"
                        SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to a byte value failed, string length: [%lu]", pmarsh->context->ObjectToStringValue(rop), (unsigned long) n);
#else
                        SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to a byte value failed, string length: [%zu]", pmarsh->context->ObjectToStringValue(rop), n);
#endif

                        pmarsh->rexxError=true;
                    }
                }
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;

    case 'b' :      // BOOLEAN
        {
            logical_t val=false;

            if (bRopIsNull)
            {
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_BOOLEAN, &val);
            }
            else
            {
                orxConvFlag=pmarsh->context->ObjectToLogical(rop, &val);
                if (orxConvFlag)    // conversion worked
                {
                    resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_BOOLEAN, &val);
                }
                else       // something went wrong in conversion
                {
                    SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to a Boolean value failed", pmarsh->context->ObjectToStringValue(rop));
                }
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;

    case 'n' :      // INT16
        {
            int32_t val=0;
            if (bRopIsNull)
            {
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_INT16, &val);
            }
            else
            {
                orxConvFlag=pmarsh->context->ObjectToInt32(rop, &val);

                if (orxConvFlag && (val>= SHRT_MIN && val<=SHRT_MAX) )    // conversion worked ?
                {
                    resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_INT16, &val);
                }
                else       // something went wrong in conversion
                {
                    if (!orxConvFlag)
                    {

                        SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to a SHORT (INT16) value failed", pmarsh->context->ObjectToStringValue(rop));
                    }
                    else
                    {
                        orxConvFlag=false;
                        SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to a SHORT (INT16) value failed (supplied Integer value too %s)",
                                                    pmarsh->context->ObjectToStringValue(rop),
                                                    val<0 ? "small" : "large"
                                                    );
                    }
                }
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;

    case 'q' :      // UINT16
        {
            uint32_t val=0;
            if (bRopIsNull)
            {
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_UINT16, &val);
            }
            else
            {
                orxConvFlag=pmarsh->context->ObjectToUnsignedInt32(rop, &val);

                if (orxConvFlag && val<=USHRT_MAX )    // conversion worked ?
                {
                    resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_UINT16, &val);
                }
                else       // something went wrong in conversion
                {
                    if (!orxConvFlag)
                    {
                        SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to an UNSIGNED SHORT (UINT16) value failed", pmarsh->context->ObjectToStringValue(rop));
                    }
                    else
                    {
                        orxConvFlag=false;
                        SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to an UNSIGNED SHORT (UINT16) value failed (supplied Integer value too large)",
                                                    pmarsh->context->ObjectToStringValue(rop) );
                    }
                }
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;


    case 'i' :      // INT32
        {
            int32_t val=0;

            if (bRopIsNull)
            {
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_INT32, &val);
            }
            else
            {
                orxConvFlag=pmarsh->context->ObjectToInt32(rop, &val);

                if (orxConvFlag)        // conversion worked ?
                {
                    resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_INT32, &val);
                }
                else       // something went wrong in conversion
                {
                    SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to an INT32 value failed", pmarsh->context->ObjectToStringValue(rop));
                }
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;


#if defined ( DBUS_TYPE_UNIX_FD )      // e.g. not defined for Windows
    case 'h' :      // UNIX_FD: a 32-bit unsigned integer accordig to dbus-specification.html
        {
            uint32_t val=0;
            if (bRopIsNull)
            {
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_UNIX_FD, &val);
            }
            else
            {
                orxConvFlag=pmarsh->context->ObjectToUnsignedInt32(rop, &val);

                if (orxConvFlag) // conversion worked ?
                {
                    resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_UNIX_FD, &val);
                }
                else       // something went wrong in conversion
                {
                    orxConvFlag=false;
                    SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to an UNSIGNED INT32 value failed", pmarsh->context->ObjectToStringValue(rop));
                }
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;
#endif

    case 'u' :      // UINT32
        {
            uint32_t val=0;
            if (bRopIsNull)
            {
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_UINT32, &val);
            }
            else
            {
                orxConvFlag=pmarsh->context->ObjectToUnsignedInt32(rop, &val);

                if (orxConvFlag) // conversion worked ?
                {
                    resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_UINT32, &val);
                }
                else       // something went wrong in conversion
                {
                    orxConvFlag=false;
                    SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to an UNSIGNED INT32 value failed", pmarsh->context->ObjectToStringValue(rop));
                }
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;

    case 'x' :      // INT64
        {
            int64_t val=0;
            if (bRopIsNull)
            {
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_INT64, &val);
            }
            else
            {
                orxConvFlag=pmarsh->context->ObjectToInt64(rop, &val);

                if (orxConvFlag)        // conversion worked ?
                {
                    resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_INT64, &val);
                }
                else       // something went wrong in conversion
                {
                    SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to an INT64 value failed", pmarsh->context->ObjectToStringValue(rop));
                }
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;

    case 't' :      // UINT64
        {
            uint64_t val=0;
            if (bRopIsNull)
            {
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_UINT64, &val);
            }
            else
            {
                orxConvFlag=pmarsh->context->ObjectToUnsignedInt64(rop, &val);

                if (orxConvFlag) // conversion worked ?
                {
                    resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_UINT64, &val);
                }
                else       // something went wrong in conversion
                {
                    orxConvFlag=false;
                    SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to an UNSIGNED INT64 value failed", pmarsh->context->ObjectToStringValue(rop));
                }
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;

    case 'd' :      // DOUBLE (IEEE 754)
        {
            double val=0.0;
            if (bRopIsNull)
            {
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_DOUBLE, &val);
            }
            else
            {
                orxConvFlag=pmarsh->context->ObjectToDouble(rop, &val);

                if (orxConvFlag)        // conversion worked ?
                {
                    resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_DOUBLE, &val);
                }
                else       // something went wrong in conversion
                {
                    orxConvFlag=false;
                    SNPRINTF(pmarsh->msgChunk256, 256, ": conversion (value=[%.32s]) to a DOUBLE value failed", pmarsh->context->ObjectToStringValue(rop));
                }
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;

        // containers -------------------------------------
    case 'a' :      // ARRAY
        {

#ifdef DEBUG_ARRAY
{
            fprintf(stderr, "*** marshal(...): *** ARRAY - begin, IsArray(rop)=[%d], rop=[%p], rop~string=[%s] ",
                     (bRopIsNull==1 ? 0 : (int) pmarsh->context->IsArray(rop) ),
                     rop,
                     (bRopIsNull==1 ? "<NULL>" : pmarsh->context->ObjectToStringValue(rop) )
                     );
    fflush(stderr);

        // assume array object was supplied for marshalling
    if (bRopIsNull!=1 && pmarsh->context->IsArray(rop))
    {
        RexxArrayObject tmpRao= ( rop==NULL ? NULL : (RexxArrayObject) rop );
        if (tmpRao!=NULL)
        {
            // fprintf(stderr, "rop~dimension=[%zu], rop~size=[%zu], rop~items=[%zu]",
            fprintf(stderr, "rop~dimension=[%lu], rop~size=[%lu], rop~items=[%lu]",
                            (unsigned long) pmarsh->context->ArrayDimension(tmpRao),
                            (unsigned long) pmarsh->context->ArraySize(tmpRao),
                            (unsigned long) pmarsh->context->ArrayItems(tmpRao)
                    );
        }
    }
    fprintf(stderr, "\n");
}
#endif

                // calc dimensionality, set array and element positions
            pmarsh->arrStart=pmarsh->cursor;
            char *tmpCursor=pmarsh->cursor;

            while (*tmpCursor=='a')
            {
                tmpCursor++;
            }
            pmarsh->dimensions = tmpCursor - pmarsh->arrStart; // dimensions
            pmarsh->arrEnd=tmpCursor-1;    // end of array

            pmarsh->elementStart=tmpCursor;

            if (*tmpCursor=='(' || *tmpCursor=='{')   // a struct or dict ?
            {
                char   ch = *tmpCursor;
                char   chEnd;               // end character to look for
                if (ch=='(')                    // round parenthesis (a structure)
                {
                    chEnd=')';
                }
                else
                {
                    chEnd='}';                  // curly parenthesis (a dictionary)
                }

                // signature already checked, hence we cannot run off the end
                size_t open=1;
                while (open>0 && *tmpCursor)   // as long as not closed and not end of string
                {
                    if (*(++tmpCursor)==ch)
                    {
                        open++;                 // opening (another of same type)
                    }
                    else if (*tmpCursor==chEnd)
                    {
                        open--;                 // closing
                    }
                }
                pmarsh->elementEnd=tmpCursor;
            }
            else
            {
                pmarsh->elementEnd=tmpCursor;
            }

#ifdef DEBUG_ARRAY
    {

    char   *tmp=pmarsh->signature;
    size_t arrStartPos=pmarsh->arrStart - tmp;
    size_t arrEndPos  =pmarsh->arrEnd   - tmp, arrLen=arrEndPos - arrStartPos+1;

    size_t elStartPos=pmarsh->elementStart - tmp;
    size_t elEndPos  =pmarsh->elementEnd   - tmp, elLen=elEndPos - elStartPos +1;

    size_t cursorPos = pmarsh->cursor - pmarsh->signature;

    fprintf(stderr, "     /--> signature=[%s], cursorPos=[%lu|%c]: arrStartPos=[%lu|%c], arrEndPos=[%lu|%c], arrLen=[%lu] | elStartPos=[%lu|%c], elEndPos=[%lu|%c], elLen=[%lu]\n",
                                     tmp,  (unsigned long) cursorPos, *(tmp+cursorPos),
                                     (unsigned long) arrStartPos, *(tmp+arrStartPos), (unsigned long) arrEndPos, *(tmp+arrEndPos),
                                     (unsigned long) arrLen,
                                     (unsigned long) elStartPos, *(tmp+elStartPos), (unsigned long) elEndPos, *(tmp+elEndPos),
                                     (unsigned long) elLen);
    fflush(stderr);
    }
#endif

                    // is this an array transporting bytes? If so indicate as Rexx can supply the values as string (= Rexx byte-array)
            pmarsh->is_ay=(pmarsh->elementStart==pmarsh->elementEnd &&      // simple complete type ?
                           *pmarsh->elementStart=='y');                     // type BYTE ?

            RexxArrayObject rao=NULL;
            bool bReleaseLocalReferenceRao=false;

            if (bRopIsNull) // o.k. Rexx argument not available, supply another empty Rexx array as argument array
            {
#ifdef DEBUG_ARRAY
    fprintf(stderr, "     marshal(...), 'a': bRopIsNull, *pmarsh->elementStart=[%c]",*pmarsh->elementStart);
#endif
                if (*pmarsh->elementStart!='{')     // make sure not a DICT expected!
                {
                    rao=pmarsh->context->NewArray(0);
                    bReleaseLocalReferenceRao=true;
#ifdef DEBUG_ARRAY
    fprintf(stderr, ", creating an empty Array object! \n");
#endif
                }
#ifdef DEBUG_ARRAY
    fprintf(stderr, "\n");
#endif

            }
            else            // object got supplied
            {

                if (pmarsh->context->IsArray(rop))  // an array object ?
                {
                    rao=(RexxArrayObject) rop;      // cast it accordingly
                }
                else if (pmarsh->context->IsOfType(rop, "ORDEREDCOLLECTION"))   // an orderable collection, if so turn it into an array
                {
                    rao=(RexxArrayObject) pmarsh->context->SendMessage0(rop, "ALLITEMS");   // get all items as an array
                    bReleaseLocalReferenceRao=true;
                }
                else        // raise error
                {
                    if (pmarsh->context->IsOfType(rop,"STRING") &&     // argument a string ?
                        pmarsh->dimensions==1 &&                        // one-dimensional array
                        pmarsh->is_ay                                   // byte array ?
                    )
                    {

                        pmarsh->marshal_ay_as_string=true;  // indicate we need to marshal the string elements as byte arrays
#ifdef DEBUG_ARRAY
    fprintf(stderr, "     marshal(...), 'a': a STRING in hand, no coercion done, object=[%s] ! \n" ,
                          pmarsh->context->ObjectToStringValue(rop) );
#endif
                    }
                    else if (*pmarsh->elementStart!='{') // if not a DICT in hand, we have a problem
                    {
                        orxConvFlag=false;
                        SNPRINTF(pmarsh->msgChunk256, 256, ": cannot convert supplied argument to a Rexx array object");
                        break;
                    }

#ifdef DEBUG_ARRAY
                    else
                    {
                        fprintf(stderr, "     marshal(...), 'a': a DICT in hand, no coercion done, object=[%s] ! \n" ,
                                          pmarsh->context->ObjectToStringValue(rop) );
                    }
#endif

                }
            }

                // start out with level "1" (first dimension), create and supply an index array object
            RexxArrayObject raoIdx=pmarsh->context->NewArray(pmarsh->dimensions);

            if (rao==NULL)      // a DICT in hand
            {
                marshalArray(iter, 1, rop, raoIdx, pmarsh);
            }
            else                // process regular array
            {
                if (pmarsh->dimensions != pmarsh->context->ArrayDimension(rao))
                {
                    // make sure it is not a byte array for which a Rexx string is supplied
                    if (!pmarsh->is_ay || (pmarsh->is_ay && (pmarsh->dimensions-1 != pmarsh->context->ArrayDimension(rao))) )
                    {
                        orxConvFlag=false;
#ifdef _MSC_VER     // "%zu" creates runtime errors with "Microsoft (R) 32-bit C/C++ Optimizing Compiler Version 15.00.30729.01 for 80x86"
                        SNPRINTF(pmarsh->msgChunk256, 256, ": marshalling of Rexx array not possible as Rexx array possesses [%lu] dimension(s), but the supplied DBUS array signature ([%s]) has [%lu] dimension(s)",
                                          (unsigned long) pmarsh->context->ArrayDimension(rao),
                                          pmarsh->signature,
                                          (unsigned long) pmarsh->dimensions
                                      );
#else
                        SNPRINTF(pmarsh->msgChunk256, 256, ": marshalling of Rexx array not possible as Rexx array possesses [%zu] dimension(s), but the supplied DBUS array signature ([%s]) has [%zu] dimension(s)",
                                          pmarsh->context->ArrayDimension(rao),
                                          pmarsh->signature,
                                          pmarsh->dimensions
                                      );
#endif
                        break;
                    }

                    pmarsh->marshal_ay_as_string=true;  // indicate we need to marshal the string elements as byte arrays
                }

                marshalArray(iter, 1, rao, raoIdx, pmarsh);
            }

                // move cursor to next char in signature
            pmarsh->cursor=pmarsh->elementEnd+1;       // let us point to the next typeCode if any

#ifdef DEBUG_ARRAY
    {
    size_t cursorPos = pmarsh->cursor - pmarsh->signature;

    fprintf(stderr, "     /<-- signature=[%s], cursorPos=[%lu|%c]: end-of-array-processing #2. \n",
            pmarsh->signature,  (unsigned long) cursorPos, *(pmarsh->signature+cursorPos));
    }
#endif
            if (bReleaseLocalReferenceRao==true)
            {
                pmarsh->context->ReleaseLocalReference(rao);
            }
        }
        break;


    case '(' :      // STRUCT - begin ("r")
        {
            // determine elementStart and elementEnd
            pmarsh->elementStart=pmarsh->cursor+1;  // point to char immediately following '('
            char *tmpCursor=pmarsh->elementStart;
            char   ch = typeCode;
            char   chEnd = ')';               // end character to look for

            size_t open=1;
            while (open>0 && *tmpCursor)   // as long as not closed and not end of string
            {
                if (*(++tmpCursor)==ch)
                {
                    open++;                 // opening (another of same type)
                }
                else if (*tmpCursor==chEnd)
                {
                    open--;                 // closing
                }
            }
            pmarsh->elementEnd=tmpCursor-1; // point to last char before closing paren ')'

            RexxArrayObject rao=NULL;
            bool bReleaseLocalReferenceRao=false;

            if (bRopIsNull) // o.k. Rexx argument not available, supply another empty Rexx array as argument array
            {
               rao=pmarsh->context->NewArray(0);
            }
            else
            {
                if (pmarsh->context->IsArray(rop))  // an array object in hand ?
                {
                    rao=(RexxArrayObject) rop;      // cast it
                }
                else if (pmarsh->context->IsOfType(rop, "ORDEREDCOLLECTION"))   // an orderable collection, if so turn it into an array
                {
                    rao=(RexxArrayObject) pmarsh->context->SendMessage0(rop, "ALLITEMS");   // get all items as an array
                    bReleaseLocalReferenceRao=true;
                }
                else        // raise error
                {
                    orxConvFlag=false;
                    SNPRINTF(pmarsh->msgChunk256, 256, ": cannot convert supplied argument to a Rexx array object containing the structure's elements");
                    break;
                }
            }

            marshalStruct(iter, rao, pmarsh);

                // move cursor to next char in signature
            pmarsh->cursor=pmarsh->elementEnd+2;       // let us point to the next typeCode if any

            if (bReleaseLocalReferenceRao==true)
            {
                pmarsh->context->ReleaseLocalReference(rao);
            }
        }

        break;

    case ')' :      // STRUCT - end (should never get here!
fprintf(stderr, "%s: *** marshal(...): *** STRUCT - end: - ERROR !! SHOULD NEVER ARRIVE HERE, please report!\n", DLLNAME);
        break;

    case 'v' :      // VARIANT
        {
#ifdef DEBUG_VARIANT
    fprintf(stderr, "*** marshal(...): *** VARIANT - begin !\n");
#endif

            marshalVariant(iter, rop, pmarsh);

            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
         break;


    case '{' :      // DICT_ENTRY - begin ("e"): invoked via marshalArray(...)
        {
            // determine elementStart and elementEnd
            pmarsh->elementStart=pmarsh->cursor+1;  // point to char immediately following '('
            char *tmpCursor=pmarsh->elementStart;
            char   ch = typeCode;
            char   chEnd = '}';               // end character to look for

            size_t open=1;
            while (open>0 && *tmpCursor)   // as long as not closed and not end of string
            {
                if (*(++tmpCursor)==ch)
                {
                    open++;                 // opening (another of same type)
                }
                else if (*tmpCursor==chEnd)
                {
                    open--;                 // closing
                }
            }
            pmarsh->elementEnd=tmpCursor-1; // point to last char before closing paren ')'

            RexxObjectPtr rxMap=NULL;
            bool bReleaseLocalReferenceRxMap=false;


            if (bRopIsNull) // o.k. Rexx argument not available, supply another empty Rexx array as argument array
            {
#ifdef DEBUG_DICT
    fprintf(stderr, "     marshal(): DICT - bRopIsNull, creating an empty Directory !\n");
#endif
               rxMap=pmarsh->context->NewDirectory();
               bReleaseLocalReferenceRxMap=true;
            }
            else
            {
                if (pmarsh->context->IsOfType(rop, "MAPCOLLECTION"))    // a mapped collection
                {
#ifdef DEBUG_DICT
    fprintf(stderr, "     marshal(): DICT - is of Type MapCollection, great!\n");
#endif
                    rxMap=rop;
                }
                else        // raise error
                {
                    orxConvFlag=false;
                    SNPRINTF(pmarsh->msgChunk256, 256, ": cannot convert supplied argument to a Rexx MapCollection object, which is needed to marshal a DICT");
                    break;
                }
            }

            // logical_t res=
            marshalDict(iter, rxMap, pmarsh);

                // move cursor to next char in signature
            pmarsh->cursor=pmarsh->elementEnd+2;       // let us point to the next typeCode if any

            if (bReleaseLocalReferenceRxMap==true)
            {
                pmarsh->context->ReleaseLocalReference(rxMap);
            }
        }
        break;

    case '}' :      // DICT_ENTRY - end - end (should never get here!
fprintf(stderr, "%s: *** marshal(...): DICT ENTRY - end: marshal() - ERROR !! SHOULD NEVER ARRIVE HERE, please report!\n", DLLNAME);
        break;

    // case '\0':      // INVALID (end-marker)
    default:
fprintf(stderr, "%s: *** marshal(...): ??? switch' default: ??? typeCode=[%c], signature=[%s]: SHOULD NEVER ARRIVE HERE, please report!\n",
                DLLNAME, typeCode, pmarsh->signature);
         break;
    }
        // determine whether errors have occurred, if so report them
    pmarsh->dbusError=(pmarsh->dbusError || resDbus==0);       // a dbus-error occurred ?
    pmarsh->rexxError=(pmarsh->rexxError || orxConvFlag==false);  // a Rexx conversion error occurred ?

    if (pmarsh->dbusError || pmarsh->rexxError)
    {
        if (pmarsh->dbusError)      // give a textual hint/reason for user
        {
            SNPRINTF(pmarsh->msgChunk256, 256, ": DBus-API returned 'no memory' while appending message argument");
        }

        return false;
    }

    return true;    // marshalling worked
}


// --------------------------------- helper function ---------------------------
// marshal an array, can be a multidimensional array, hence recursive function
/** Marsahll an array, can be a multidimensional array, hence a recursive function.
*
* @param iter the DBus iteration structure
* @param level recursion level
* @param anyRop a Rexx array object or a Rexx dictionary (Map) or a Rexx string
* @param arrIdx the Rexx array index containing the values to be marshalled
* @param pmarsh the structure that controls marshalling
* @return <code>true</code> if marshalling was carried out without errors, <code>false</code> else
*/
logical_t marshalArray(DBusMessageIter *iter, size_t level, RexxObjectPtr anyRop, RexxArrayObject arrIdx, PSTRUCT_MARSHAL pmarsh)
{
#ifdef DEBUG_ARRAY
   fprintf(stderr, "---> arrived in marshalArray(...): signature=[%s] - cursor=[%lu], arrStart=[%lu], arrEnd=[%lu], elStart=[%lu], elEnd=[%lu]\n",
                         pmarsh->signature,
                         (unsigned long) (pmarsh->cursor   - pmarsh->signature    ) ,
                         (unsigned long) (pmarsh->arrStart - pmarsh->signature    ) ,
                         (unsigned long) (pmarsh->arrEnd   - pmarsh->signature    ) ,
                         (unsigned long) (pmarsh->elementStart - pmarsh->signature) ,
                         (unsigned long) (pmarsh->elementEnd   - pmarsh->signature)
           );
#endif

    DBusMessageIter iter_array;
    RexxArrayObject rao=NULL;

    if (anyRop != NULL && pmarsh->context->IsArray(anyRop))   // an array object (could be a MapCollection for processing a DICT
    {
        rao=(RexxArrayObject) anyRop;
    }

        // determine signature to use for opening a containter
#ifdef MALLOC_USE_REXX
    char *localSignature=(char *) RexxAllocateMemory(pmarsh->signatureLength+1);  // that should be enough
    memset(localSignature, 0, pmarsh->signatureLength+1);
#else
    char *localSignature=(char *) dbus_malloc0(pmarsh->signatureLength+1);  // that should be enough
#endif
    char *start=pmarsh->arrStart+level;
    strncpy(localSignature, start, pmarsh->elementEnd-start+1);

#ifdef DEBUG_ARRAY
    fprintf(stderr, "     - level=[%lu], localSignature=[%s], anyRop=[%p]=[%s], p->dimensions=[%lu] <-> arrIdx=[%s]\n",
                            (unsigned long) level, localSignature,
                            anyRop,
                            (anyRop==NULL ? "NULL" : pmarsh->context->ObjectToStringValue(anyRop) ),
                            (unsigned long) pmarsh->dimensions,
                                ARR_AS_STRING(arrIdx, pmarsh->context)
                            ); fflush(stderr);
   fprintf(stderr, "    marshalArray(...): OPEN container ---> ---> ---> signature=[%s]\n", localSignature);
#endif

        // open_container
    if (!dbus_message_iter_open_container(iter, DBUS_TYPE_ARRAY, localSignature, &iter_array))
    {
        pmarsh->dbusError=true;
        SNPRINTF(pmarsh->msgChunk256, 256, ": DBus-API returned 'no memory' while opening an array container for marshalling");

#ifdef MALLOC_USE_REXX
        RexxFreeMemory(localSignature);  // that should be enough
#else
        dbus_free(localSignature);
#endif
        return false;
    }

    if (level==pmarsh->dimensions)
    {
            // create a new copy of the STRUCT_MARSHAL to isolate the caller's one from changes in marshal(...)
        STRUCT_MARSHAL localM;
        SET_NEW_STRUCT_MARSHAL (&localM, localSignature, pmarsh);   // initialize new STRUCT_MARSH

        if (*localSignature=='{')       // a DICT in hands?
        {
#ifdef DEBUG_ARRAY
        fprintf(stderr, "       ... branch # 0: level==pmarsh->dimensions, a DICT in hand!\n");
#endif
            if (!marshal(&iter_array, *localM.cursor, anyRop, &localM))  // single type, typecode from cursor
            {                                                               // an error has occurred !
                pmarsh->dbusError           = localM.dbusError;
                pmarsh->rexxError           = localM.rexxError;
                pmarsh->errorPosInSignature = pmarsh->elementStart+(localM.errorPosInSignature-localM.elementStart);
#ifdef MALLOC_USE_REXX
                RexxFreeMemory(localSignature);  // that should be enough
#else
                dbus_free(localSignature);
#endif
                return false;
            }
        }


/*
    two cases: simple element 'y': arg==STRING; if multidimensional array, then current array-element a STRING

*/
        else if (pmarsh->marshal_ay_as_string)  //
        {
            RexxStringObject rso=NULL;
            bool bReleaseLocalReferenceRso=false;

            // if (pmarsh->context->IsString(anyRop))
            if (pmarsh->context->IsOfType(anyRop, "STRING"))
            {
                rso=(RexxStringObject) anyRop;
            }
            else if (rao!=NULL)         // an array in hand: get its entry and that string value
            {
                // get stored value, marshal it
                RexxObjectPtr tmpRop=pmarsh->context->SendMessage1(rao, "AT", arrIdx);
                if (tmpRop==NULL)
                {
                    rso=pmarsh->context->NullString();     // empty string
                }
                else
                {
                    rso=pmarsh->context->ObjectToString(tmpRop);
                    bReleaseLocalReferenceRso=true;
                    pmarsh->context->ReleaseLocalReference(tmpRop);
                }
            }
            else        // rao==NULL !
            {
                rso=pmarsh->context->NullString();     // empty string
            }

            size_t  stringLength=pmarsh->context->StringLength(rso);  // get length of the Rexx string, which may contain NULLs
            CSTRING stringValue =pmarsh->context->CString(rso);  // get a CSTRING

#ifdef DEBUG_ARRAY
    fprintf(stderr, "       ... branch # 1b: level==pmarsh->dimensions, a STRING in hand, size=[%lu], value=[%.64s]!\n",
                                    (unsigned long) stringLength, stringValue);
#endif

            if (bReleaseLocalReferenceRso==true)
            {
                pmarsh->context->ReleaseLocalReference(rso);
            }

            // 2014-10-19, rgf: add cast to int to remove warning on 64-bit
            if (!dbus_message_iter_append_fixed_array(&iter_array, DBUS_TYPE_BYTE, &stringValue, (int) stringLength ))
            {
                pmarsh->dbusError = true;
                SNPRINTF(pmarsh->msgChunk256, 256, ": DBus-API returned 'no memory' while marshalling a byte array using the 'dbus_message_iter_append_fixed_array()'-API");

#ifdef MALLOC_USE_REXX
                RexxFreeMemory(localSignature);  // that should be enough
#else
                dbus_free(localSignature);
#endif
                return false;
            }

        }

        else    // process array-specifically
        {
            size_t neededRexxArgs=GET_NUMBER_OF_REXX_ARGS_FOR_SIGNATURE((char *) localSignature);

#ifdef DEBUG_ARRAY
        fprintf(stderr, "       ... branch # 2: level==pmarsh->dimensions == [%lu], neededRexxArgs=[%lu]\n",
                        (unsigned long) level, (unsigned long) neededRexxArgs);
#endif

                // process all array items
            // 2014-10-19, rgf: add cast (int32_t) to stop warning on 64-bit MSC
            size_t items=GET_ARRAY_SIZE_AT_DIMENSION(rao, (int32_t) level, pmarsh->context);

#ifdef DEBUG_ARRAY
       fprintf(stderr, "       ...ma(...): rao=[%s], has rao-size/items at level=[%lu]/dim|items: [%lu] | arrIdx=[%s]\n",
                                     ARR_AS_STRING( rao , pmarsh->context),
                                     (unsigned long) level,
                                     (unsigned long) items,
                                      ARR_AS_STRING( arrIdx , pmarsh->context)
                                     );
#endif

            for (size_t i=1; i<=items; i++)     // process each array entry, which itself may have a signature
            {
#ifdef DEBUG_ARRAY
    fprintf(stderr, "          marshalArray(...): processing array (level=[%lu]) item # i=[%lu]/items=[%lu]\n",
                               (unsigned long) level, (unsigned long) i, (unsigned long) items);
#endif

                localM.cursor=localM.signature;     // reset cursor

#ifdef DEBUG_ARRAY
   fprintf(stderr, "          ... ma(...): localM.cursor=[%s], *localM.cursor=[%c] | arrIdx=[%p]=[%s]\n",
                            localM.cursor,
                           *localM.cursor,
                            arrIdx,
                            ( arrIdx==NULL ? NULL : (ARR_AS_STRING(arrIdx,pmarsh->context)) )
                            );
#endif

                // set this dimension's index appropriately
                SET_ARRAY_IDX(arrIdx, level, i, pmarsh->context);

#ifdef DEBUG_ARRAY
   fprintf(stderr, "          ... ma(...): arrIdx=[%s]\n",  ARR_AS_STRING( arrIdx , pmarsh->context));
   fprintf(stderr, "          ... ma(...): rao=[%p]", rao);
   if (rao!=NULL)
   {
       fprintf(stderr, "          - dims=[%lu], items=[%lu], size=[%lu] \n",
                     (unsigned long) pmarsh->context->ArrayDimension(rao),
                     (unsigned long) pmarsh->context->ArrayItems    (rao),
                     (unsigned long) pmarsh->context->ArraySize     (rao));
   }
#endif

                // get stored value, marshal it
                RexxObjectPtr rop=pmarsh->context->SendMessage1(rao, "AT", arrIdx);

#ifdef DEBUG_ARRAY
    fprintf(stderr, "          ... ma(...): rao AT - arrIdx=[%s], value=[%s]\n",
                         ARR_AS_STRING( arrIdx , pmarsh->context),
                         (rop==NULL ? "NULL" : pmarsh->context->ObjectToStringValue(rop))
                         );
#endif

                if (pmarsh->context->IsOfType(rop, "STRING") &&     // argument a string ?
                    pmarsh->elementStart==pmarsh->elementEnd &&     // simple complete type ?
                    *pmarsh->elementStart=='y' )                    // type BYTE ?
                {
                    // make sure "rop" is a "real" String object (and not a "hidden Integer" or "NumberString", such that StringLength() works reliably !
                    RexxObjectPtr oriRop=rop;
                    rop=pmarsh->context->ObjectToString(rop);
                    size_t  stringLength=pmarsh->context->StringLength((RexxStringObject) rop);  // get length of the Rexx string, which may contain NULLs
                    CSTRING stringValue =pmarsh->context->CString(rop);  // get a CSTRING

#ifdef DEBUG_ARRAY
            fprintf(stderr, "       ... branch # 2:  BYTE-ARRAY / level==pmarsh->dimensions, a STRING in hand, stringLength=[%lu], stringValue=[%.64s]!\n",
                                            (unsigned long) stringLength, stringValue);
        if (pmarsh->context->IsArray(rop))
        {
            fprintf(stderr, "       ... branch # 2b: BYTE-ARRAY / an array in hand, value=[%.64s]!\n",
                            ARR_AS_STRING( (RexxArrayObject) rop , pmarsh->context) );
        }

#endif
                    pmarsh->context->ReleaseLocalReference(rop);
                    pmarsh->context->ReleaseLocalReference(oriRop);


                    // 2014-10-19, rgf: add cast (int) to stop warning on 64-bit MSC
                    if (!dbus_message_iter_append_fixed_array(&iter_array, DBUS_TYPE_BYTE, &stringValue, (int) stringLength ))
                    {
                        pmarsh->dbusError = true;
                        SNPRINTF(pmarsh->msgChunk256, 256, ": DBus-API returned 'no memory' while marshalling a byte array using the 'dbus_message_iter_append_fixed_array()'-API");
        #ifdef MALLOC_USE_REXX
                        RexxFreeMemory(localSignature);  // that should be enough
        #else
                        dbus_free(localSignature);
        #endif
                        return false;
                    }
                    // DONE !
                    pmarsh->context->ReleaseLocalReference(rop);
                    continue;
                }


                if (neededRexxArgs>1)   // more than one arg needed, Rexx object must be an array object or NULL
                {
                    RexxArrayObject rao=NULL;
                    if (rop == NULLOBJECT || rop==pmarsh->context->Nil())
                    {
                        rao=pmarsh->context->NewArray(0);   // empty array object
                    }
                    else if (pmarsh->context->IsOfType(rop, "ORDEREDCOLLECTION"))   // an orderable collection, if so turn it into an array
                    {
                        rao=(RexxArrayObject) pmarsh->context->SendMessage0(rop, "ALLITEMS");   // get all items as an array
                    }
                    else        // raise error
                    {

                        SNPRINTF(pmarsh->msgChunk256, 256, ": ARRAY signature, cannot convert supplied argument to a Rexx array object containing the needed elements");
                        pmarsh->rexxError=true;
#ifdef MALLOC_USE_REXX
                        RexxFreeMemory(localSignature);  // that should be enough
#else
                        dbus_free(localSignature);
#endif
                        pmarsh->context->ReleaseLocalReference(rop);
                        return false;
                    }

                    if (neededRexxArgs<pmarsh->context->ArraySize(rao))    // too many Rexx arguments ?
                    {
                        char msg[128]="";     // buffer for error message
#ifdef _MSC_VER     // "%zu" creates runtime errors with "Microsoft (R) 32-bit C/C++ Optimizing Compiler Version 15.00.30729.01 for 80x86"
                        SNPRINTF( msg, 128, "localSignature [%s] mandates at most [%lu] Rexx arguments, however Rexx supplies too many arguments [%lu]",
                                             localSignature, (unsigned long) neededRexxArgs, (unsigned long) items);
#else
                        SNPRINTF( msg, 128, "localSignature [%s] mandates at most [%zu] Rexx arguments, however Rexx supplies too many arguments [%zu]",
                                             localSignature, neededRexxArgs, items);
#endif

                        SNPRINTF(pmarsh->msgChunk256, 256, ": marshalling an ARRAY - %s", msg);
                        pmarsh->rexxError=true;
#ifdef MALLOC_USE_REXX
                        RexxFreeMemory(localSignature);  // that should be enough
#else
                        dbus_free(localSignature);
#endif
                        if (rao!=NULL)
                        {
                            pmarsh->context->ReleaseLocalReference(rao);
                        }
                        pmarsh->context->ReleaseLocalReference(rop);
                        return false;
                    }

                    for (size_t idx=1; idx<=neededRexxArgs; idx++)   // iterate over signature's types
                    {
                        localM.cursor=localM.signature;       // reset cursor, such that each array element is processed with the same local signature
                        RexxObjectPtr currRop=pmarsh->context->ArrayAt(rao, idx);       // get array's entry

#ifdef DEBUG_ARRAY
        fprintf(stderr, "    marshalArray(...): processing array item # [%lu]/[%lu] | subelement idx=[%lu]/[%lu] single type=[%c], value=[%s] ***\n",
                                            (unsigned long) i, (unsigned long) items,
                                            (unsigned long) idx, (unsigned long) neededRexxArgs,
                                            *localM.cursor,
                                            (currRop!=NULL ? pmarsh->context->ObjectToStringValue(currRop) : NULL)
                                            );
#endif

                        if (!marshal(&iter_array, *localM.cursor, currRop, &localM))  // single type, typecode from cursor
                        {                                                               // an error has occurred !
                            pmarsh->dbusError           = localM.dbusError;
                            pmarsh->rexxError           = localM.rexxError;
                            pmarsh->errorPosInSignature = pmarsh->elementStart+(localM.errorPosInSignature-localM.elementStart);
#ifdef MALLOC_USE_REXX
                            RexxFreeMemory(localSignature);  // that should be enough
#else
                            dbus_free(localSignature);
#endif
                            if (rao!=NULL)
                            {
                                pmarsh->context->ReleaseLocalReference(rao);
                            }
                            pmarsh->context->ReleaseLocalReference(currRop);
                            pmarsh->context->ReleaseLocalReference(rop);
                            return false;
                        }
                    }
                    if (rao!=NULL)
                    {
                        pmarsh->context->ReleaseLocalReference(rao);
                    }
                    pmarsh->context->ReleaseLocalReference(rop);
                }
                else
                {
#ifdef DEBUG_ARRAY
    fprintf(stderr, "    marshalArray(...): neededRexxArgs<=1: processing level=[%lu], array item # i=[%lu]/items=[%lu]: single type=[%c]; arrIdx=[%s]: value=[%s] §§§\n",
                         (unsigned long) level,
                         (unsigned long) i, (unsigned long) items, *localM.cursor,
                         ARR_AS_STRING(arrIdx, pmarsh->context),
                         (rop==NULL ? NULL : pmarsh->context->ObjectToStringValue(rop))
                         );
#endif
                    if (!marshal(&iter_array, *localM.cursor, rop, &localM)) // single type, typecode from cursor
                    {                                                               // an error has occurred !
                        pmarsh->dbusError           = localM.dbusError;
                        pmarsh->rexxError           = localM.rexxError;
                        pmarsh->errorPosInSignature = pmarsh->elementStart+(localM.errorPosInSignature-localM.elementStart);
#ifdef MALLOC_USE_REXX
                        RexxFreeMemory(localSignature);  // that should be enough
#else
                        dbus_free(localSignature);
#endif
                        pmarsh->context->ReleaseLocalReference(rop);
                        return false;
                    }
                }

                pmarsh->context->ReleaseLocalReference(rop);
            }
        }
    }

    else        // recurse into next dimension of array
    {
#ifdef DEBUG_ARRAY
    fprintf(stderr, "       ... branch: recursing into next level \n");
#endif

        logical_t bRetValue=false;

        // 2014-10-19, rgf: add cast (int32_t) to stop warning on 64-bit MSC
        size_t items=GET_ARRAY_SIZE_AT_DIMENSION(rao, (int32_t) level, pmarsh->context);
        if (items==0)   // e.g. if .nil was supplied; we still need to create the array containers, even if they remain empty
        {
            items=1;
        }

        for (size_t i=1; i<=items; i++)
        {
            // set this dimension's index appropriately
            SET_ARRAY_IDX(arrIdx, level, i, pmarsh->context);

#ifdef DEBUG_ARRAY
    fprintf(stderr, "    ...ma(...): recursing with level=[%lu] with [%lu] items, arrIdx=[%s]\n",
                            (unsigned long) level, (unsigned long) items, ARR_AS_STRING( arrIdx , pmarsh->context));
#endif

                // recurse into the next dimension
            bRetValue=marshalArray(&iter_array, level+1, rao, arrIdx, pmarsh);

            if (!bRetValue)
            {
#ifdef MALLOC_USE_REXX
                RexxFreeMemory(localSignature);  // that should be enough
#else
                dbus_free(localSignature);
#endif
                return false;
            }
        }
    }

        // close_container
    dbus_message_iter_close_container (iter, &iter_array);

#ifdef DEBUG_ARRAY
fprintf(stderr, "    marshalArray(...): CLOSE container <--- <--- <---, signature=[%s]\n", localSignature);
#endif

#ifdef MALLOC_USE_REXX
    RexxFreeMemory(localSignature);  // that should be enough
#else
    dbus_free(localSignature);
#endif
    return true;
}



// -----------------------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------------------
// marshalDict()
// - expects: elementStart to point at one after opening '(' and elementEnd at one before closing ')' !
/** Marsahll a dictionary (MapCollection). Expects <code>elementStart</code> to point at one char after
*              the opening <code>'('</code> char and <code>elementEnd</code> at one char before the
*              closing <code>')'</code> char.
*
* @param iter the DBus iteration structure
* @param rxMap a Rexx directory (MapCollection) object
* @param pmarsh the structure that controls marshalling
* @return <code>true</code> if marshalling was carried out without errors, <code>false</code> else
*/

logical_t marshalDict(DBusMessageIter *iter, RexxObjectPtr rxMap, PSTRUCT_MARSHAL pmarsh)
{

#if defined (DEBUG_DICT) && defined (DEBUG_MARSHAL)
   fprintf(stderr, "---> arrived in marshalDict(...), rxMap=[%p]~class: type=[%s] ~items=[%s], signature=[%s] \n",
                   rxMap, pmarsh->context->ObjectToStringValue(rxMap),
                   pmarsh->context->ObjectToStringValue(pmarsh->context->SendMessage0(rxMap,"ITEMS")),
                   pmarsh->signature);
#endif

    DBusMessageIter iter_dict;

        // determine signature to use for opening a containter, must include brackets !
#ifdef MALLOC_USE_REXX
    char *localSignature=(char *) RexxAllocateMemory(pmarsh->signatureLength+1);  // that should be enough
    memset(localSignature, 0, pmarsh->signatureLength+1);
#else
    char *localSignature=(char *) dbus_malloc0(pmarsh->signatureLength+1);  // that should be enough
#endif

        // use signature withing parenthesis
    strncpy(localSignature, pmarsh->elementStart,               // start position
                pmarsh->elementEnd - pmarsh->elementStart +1    // length
                );

#if defined (DEBUG_DICT) && defined (DEBUG_MARSHAL)
    fprintf(stderr, "     - localSignature=[%s]\n", localSignature); fflush(stderr);
#endif

        // create a new copy of the DICT_MARSHAL to isolate the caller's one from changes in marshal(...)
    STRUCT_MARSHAL localM;
    SET_NEW_STRUCT_MARSHAL (&localM, localSignature, pmarsh);   // initialize new DICT_MARSH
        // do not include brackets
    localM.elementStart=localSignature;
    localM.elementEnd  =localSignature+localM.signatureLength-1;
    localM.cursor=localM.elementStart;  // let cursor point to element's start

        // create & iterate over supplier
    RexxSupplierObject rso = (RexxSupplierObject) pmarsh->context->SendMessage0(rxMap, "SUPPLIER");  // get a supplier

#ifdef DEBUG_DICT_2
        fprintf(stderr, "     marshallDict() - rxMap=[%s], rxMap~items=[%s]\n",
                              pmarsh->context->ObjectToStringValue(rxMap),
                              pmarsh->context->ObjectToStringValue(pmarsh->context->SendMessage0(rxMap, "ITEMS")));

        fprintf(stderr, "     marshallDict() - supplier Object 'rso'=[%p]/[%s], available=[%d] ...\n",
                               rso,
                               pmarsh->context->ObjectToStringValue(rso),
                               (int) pmarsh->context->SupplierAvailable(rso)
                               );
#endif

    for ( ; pmarsh->context->SupplierAvailable(rso); pmarsh->context->SupplierNext(rso) )
    {
        localM.elementStart=localSignature; // rgf, 20140727
        localM.cursor=localM.elementStart;  // reset to start of respective signature

#ifdef DEBUG_DICT_2
   fprintf(stderr, "     marshalDict(...): OPEN container ---> ---> ---> local signature=[%s], cursor=[%c], rso~index=[%s]\n",
                         localSignature,
                        *localM.cursor,
                        pmarsh->context->ObjectToStringValue(pmarsh->context->SupplierIndex(rso))
                        );
   fflush(stderr);
#endif

            // open_container
        if (!dbus_message_iter_open_container(iter, DBUS_TYPE_DICT_ENTRY, NULL, &iter_dict))
        {
            pmarsh->dbusError=true;
            SNPRINTF(pmarsh->msgChunk256, 256, ": DBus-API returned 'no memory' while marshalling a dicitonary (a \"DICT\") using the 'dbus_message_iter_open_container()'-API");
#ifdef MALLOC_USE_REXX
            RexxFreeMemory(localSignature);  // that should be enough
#else
            dbus_free(localSignature);
#endif
            pmarsh->context->ReleaseLocalReference(rso);
            return false;
        }

            // process index
        RexxObjectPtr ropIndex=pmarsh->context->SupplierIndex(rso);
        if (!marshal(&iter_dict, *localM.cursor, ropIndex, &localM))   // an error has occurred !
        {
            pmarsh->dbusError           = localM.dbusError;
            pmarsh->rexxError           = localM.rexxError;
            pmarsh->errorPosInSignature = pmarsh->elementStart+(localM.errorPosInSignature-localM.elementStart);
#ifdef MALLOC_USE_REXX
            RexxFreeMemory(localSignature);  // that should be enough
#else
            dbus_free(localSignature);
#endif
            pmarsh->context->ReleaseLocalReference(ropIndex);
            pmarsh->context->ReleaseLocalReference(rso);
            return false;
        }
        pmarsh->context->ReleaseLocalReference(ropIndex);

            // process item
        RexxObjectPtr ropItem=pmarsh->context->SupplierItem(rso);
        if (!marshal(&iter_dict, *localM.cursor, ropItem, &localM))   // an error has occurred !
        {
            pmarsh->dbusError           = localM.dbusError;
            pmarsh->rexxError           = localM.rexxError;
            pmarsh->errorPosInSignature = pmarsh->elementStart+(localM.errorPosInSignature-localM.elementStart);
#ifdef MALLOC_USE_REXX
            RexxFreeMemory(localSignature);  // that should be enough
#else
            dbus_free(localSignature);
#endif
            pmarsh->context->ReleaseLocalReference(ropItem);
            pmarsh->context->ReleaseLocalReference(rso);
            return false;
        }
        pmarsh->context->ReleaseLocalReference(ropItem);

            // close_container
        dbus_message_iter_close_container (iter, &iter_dict);
#ifdef DEBUG_DICT_2
    fprintf(stderr, "     marshalDict(...): CLOSE container <--- <--- <--- signature=[%s]\n", localSignature);
#endif
    }


#ifdef MALLOC_USE_REXX
    RexxFreeMemory(localSignature);  // that should be enough
#else
    dbus_free(localSignature);
#endif


#if defined (DEBUG_DICT) && defined (DEBUG_MARSHAL)
   fprintf(stderr, "<--- returning from marshalDict(...).\n");
#endif

    pmarsh->context->ReleaseLocalReference(rso);
    return true;
}


// ---------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------
// marshalStruct()
// - expects: elementStart to point at one after opening '(' and elementEnd at one before closing ')' !
/** Marsahll a dictionary (MapCollection).
*
* @param iter the DBus iteration structure
* @param rao a Rexx array containing the elements of the structure to be marshalled
* @param pmarsh the structure that controls marshalling
* @return <code>true</code> if marshalling was carried out without errors, <code>false</code> else
*/
logical_t marshalStruct(DBusMessageIter *iter, RexxArrayObject rao, PSTRUCT_MARSHAL pmarsh)
{

#ifdef DEBUG_STRUCT
   fprintf(stderr, "---> arrived in marshalStruct(...) \n");
#endif

    DBusMessageIter iter_struct;

        // determine signature to use for opening a containter
#ifdef MALLOC_USE_REXX
    char *localSignature=(char *) RexxAllocateMemory(pmarsh->signatureLength+1);  // that should be enough
    memset(localSignature, 0, pmarsh->signatureLength+1);
#else
    char *localSignature=(char *) dbus_malloc0(pmarsh->signatureLength+1);  // that should be enough
#endif

        // use signature within parenthesis
    strncpy(localSignature, pmarsh->elementStart,               // start position
                pmarsh->elementEnd - pmarsh->elementStart +1    // length
                );

#ifdef DEBUG_STRUCT
    fprintf(stderr, "     - localSignature=[%s]\n", localSignature); fflush(stderr);
#endif

        // do we have enough Rexx arguments ?
    size_t items = (rao==NULLOBJECT ? 0 : pmarsh->context->ArraySize(rao) );
    size_t neededRexxArgs=GET_NUMBER_OF_REXX_ARGS_FOR_SIGNATURE((char *) localSignature);

    if (neededRexxArgs<items)
    {
        char msg[128]="";     // buffer for error message
#ifdef _MSC_VER     // "%zu" creates runtime errors with "Microsoft (R) 32-bit C/C++ Optimizing Compiler Version 15.00.30729.01 for 80x86"
        SNPRINTF( msg, 128, "localSignature [%s] mandates at most [%lu] Rexx arguments, however Rexx supplies too many arguments [%lu]",
                             localSignature, (unsigned long) neededRexxArgs, (unsigned long) items);
#else
        SNPRINTF( msg, 128, "localSignature [%s] mandates at most [%zu] Rexx arguments, however Rexx supplies too many arguments [%zu]",
                             localSignature, neededRexxArgs, items);
#endif

        SNPRINTF(pmarsh->msgChunk256, 256, ": marshalling a STRUCT - %s", msg);
        pmarsh->rexxError=true;
#ifdef MALLOC_USE_REXX
        RexxFreeMemory(localSignature);  // that should be enough
#else
        dbus_free(localSignature);
#endif
        return false;
    }


#ifdef DEBUG_STRUCT
   fprintf(stderr, "     marshalStruct(...): OPEN container ---> ---> ---> signature=[%s]\n", localSignature);
#endif

        // open_container
    if (!dbus_message_iter_open_container(iter, DBUS_TYPE_STRUCT, NULL, &iter_struct))
    {
        pmarsh->dbusError=true;
        SNPRINTF(pmarsh->msgChunk256, 256, ": DBus-API returned 'no memory' while marshalling a structure (a \"STRUCT\") using the 'dbus_message_iter_open_container()'-API");
#ifdef MALLOC_USE_REXX
        RexxFreeMemory(localSignature);  // that should be enough
#else
        dbus_free(localSignature);
#endif
        return false;
    }

        // create a new copy of the STRUCT_MARSHAL to isolate the caller's one from changes in marshal(...)
    STRUCT_MARSHAL localM;
    SET_NEW_STRUCT_MARSHAL (&localM, localSignature, pmarsh);   // initialize new STRUCT_MARSH

    // loop over local signature, supplying array-elements as arguments to marshal(...) !
    for (size_t idx=1; *localM.cursor; idx++)   // cursor will be forwarded by marshal(...)
    {
        if (!marshal(&iter_struct, *(localM.cursor), pmarsh->context->ArrayAt(rao,idx), &localM))   // an error has occurred !
        {
            pmarsh->dbusError           = localM.dbusError;
            pmarsh->rexxError           = localM.rexxError;
            pmarsh->errorPosInSignature = pmarsh->elementStart+(localM.errorPosInSignature-localM.elementStart);
#ifdef MALLOC_USE_REXX
            RexxFreeMemory(localSignature);  // that should be enough
#else
            dbus_free(localSignature);
#endif
            return false;
        }
#ifdef DEBUG_STRUCT
   fprintf(stderr, "     success: done idx=[%lu], localM.cursor=[%s] ...\n", (unsigned long) idx, localM.cursor);
#endif
    }

        // close_container
    dbus_message_iter_close_container (iter, &iter_struct);

#ifdef DEBUG_STRUCT
   fprintf(stderr, "     marshalStruct(...): CLOSE container <--- <--- <--- signature=[%s]\n", localSignature);
#endif


#ifdef MALLOC_USE_REXX
            RexxFreeMemory(localSignature);  // that should be enough
#else
    dbus_free(localSignature);
#endif
    return true;
}



// -----------------------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------------------
// marshalVariant()
// - expects: elementStart to point at one after opening '(' and elementEnd at one before closing ')' !

// check args: NULL -> "s"
//             array -> "a*s", where "*" is dimension()
//             OrderedCollection -> "as"
//             MapCollection -> "a{ss}"
/** Marsahll a variant type.
*
* @param iter the DBus iteration structure
* @param rop a Rexx object to be marshalled
* @param pmarsh the structure that controls marshalling
* @return <code>true</code> if marshalling was carried out without errors, <code>false</code> else
*/

logical_t marshalVariant(DBusMessageIter *iter, RexxObjectPtr rop, PSTRUCT_MARSHAL pmarsh)
{

#ifdef DEBUG_VARIANT
   fprintf(stderr, "---> arrived in marshalVariant(...), rop=[%p] \n", rop);
#endif

    DBusMessageIter iter_variant;
    bool bReleaseLocalReferenceRop=false;

    logical_t bRopIsNull= (rop==NULLOBJECT || (pmarsh->context->Nil()==rop)) ;  // a Rexx .nil, resp. NULLOBJECT in hands ?
    char *localSignature=NULL;
    size_t dimensions=0;

    if (bRopIsNull)     // use signature "s"
    {
#ifdef DEBUG_VARIANT
   fprintf(stderr, "     marshalVariant(...), branch: bRopIsNull \n");
#endif

#ifdef MALLOC_USE_REXX
        localSignature=(char *) RexxAllocateMemory(2);
        memset(localSignature, 0, 2);
#else
        localSignature=(char *) dbus_malloc0(2);  // that should be enough
#endif
        strcpy(localSignature, "s");                // copy signature
    }

        // either encode as a*s or use explicitly supplied variant signature
    else if (pmarsh->context->IsArray(rop))   // an array in hand ? "a*s"
    {
#ifdef DEBUG_VARIANT
   fprintf(stderr, "     marshalVariant(...), branch: IsArray(rop) \n");
#endif
        RexxArrayObject rao=(RexxArrayObject) rop;
        size_t dimension=pmarsh->context->ArrayDimension(rao);

        // check for variant signature, if available, use it
        if (  dimension                        == 1 &&
              pmarsh->context->ArraySize(rao)  == 2 &&
              pmarsh->context->ArrayItems(rao) == 2
           )
        {
            // this branch expects .array~of("replySignature=x", returnValueMatchingSignature)

            // if single-dimensioned array and first element starts with "variantSignature="
            // then use string immediately following '=' as an explicit variant signature,
            // and remaining array element as the return value; this should allow to deal
            // with infrastructures like "telepathy" which expect some variant dict entries
            // to be encoded strictly with predefined types (tsk!)
            // CSTRING fel= pmarsh->context->ObjectToStringValue(pmarsh->context->ArrayAt(rao, 1));
            RexxObjectPtr ropTmp=pmarsh->context->ArrayAt(rao, 1);
            CSTRING fel= pmarsh->context->ObjectToStringValue(ropTmp);
            pmarsh->context->ReleaseLocalReference(ropTmp);

#ifdef DEBUG_VARIANT
   fprintf(stderr, "     marshalVariant(...), variantSignature supplied ? fel=[%s]\n",fel);
#endif

            if (OVERRIDE_SIGNATURE_GIVEN(fel))
            {
                size_t offset=17;   // offset from where to pickup signature

                while (fel[offset]==' ' || fel[offset]=='\t') // skip empty
                {
                    offset++;
                }

                CSTRING tmpLocalSignature=fel+offset;

                logical_t sigOK=dbus_signature_validate(tmpLocalSignature, NULL); // not interested in error message name, hence NULL

#ifdef DEBUG_VARIANT
    fprintf(stderr, "     marshalVariant(...), variantSignature supplied ? ---> fel+17=tmpLocalSignature=[%s], sigOK=[%d] <--- <-- \n",
                    tmpLocalSignature, (int) sigOK);
#endif

                if (sigOK)  // first array element is a valid signature!
                {
            #ifdef MALLOC_USE_REXX
                    localSignature=(char *) RexxAllocateMemory( 256 );
                    memset(localSignature, 0, 256);
            #else
                    localSignature=(char *) dbus_malloc0(256);  // that should be enough
            #endif
                    SNPRINTF(localSignature, 256, "%s", tmpLocalSignature);
                        // use second element of returned array object as return value matching the signature
#ifdef DEBUG_VARIANT
    fprintf(stderr, "     marshalVariant(...), variantSignature supplied ! variantSignature=[%s]\n",localSignature);
#endif
                    rop=pmarsh->context->ArrayAt(rao, 2);   // this is the real argument to marshal in this case
                    bReleaseLocalReferenceRop=true;
                }
            }
        }

        if (localSignature==NULL)   // array without signature, use 'a*s', where '*' is number of dimensions
        {
#ifdef DEBUG_VARIANT
   fprintf(stderr, "     marshalVariant(...), no variant signature, encode array with 'a*s' \n");
#endif
            size_t dimensions=pmarsh->context->ArrayDimension((RexxArrayObject) rop);
            if (dimensions==0)  // at least one dimension, if empty array object
            {
                dimensions=1;
            }

#ifdef MALLOC_USE_REXX
            localSignature=(char *) RexxAllocateMemory( dimensions+2 );
            memset(localSignature, 0, dimensions+2);
#else
            localSignature=(char *) dbus_malloc0(dimensions+2);  // that should be enough
#endif
            for (size_t i=0; i<dimensions; i++)
            {
                *(localSignature+i)='a';
            }
            *(localSignature+dimensions)='s';
        }
    }

    else if (pmarsh->context->IsOfType(rop, "COLLECTION"))  // a collection in hand
    {
#ifdef DEBUG_VARIANT
   fprintf(stderr, "     marshalVariant(...), branch: IsOfType('COLLECTION') \n");
#endif
        if (pmarsh->context->IsOfType(rop, "ORDEREDCOLLECTION"))
        {
#ifdef MALLOC_USE_REXX
            localSignature=(char *) RexxAllocateMemory(3);
            memset(localSignature, 0, 3);
#else
            localSignature=(char *) dbus_malloc0(3);  // that should be enough
#endif
            strcpy(localSignature, "as");                // copy signature
        }
        else                // map collection (DICT)
        {
#ifdef MALLOC_USE_REXX
            localSignature=(char *) RexxAllocateMemory(6);
            memset(localSignature, 0, 6);
#else
            localSignature=(char *) dbus_malloc0(6);  // that should be enough
#endif
            strcpy(localSignature, "a{ss}");                // copy signature
        }
    }

    else                // treat as a string, "s"
    {
#ifdef DEBUG_VARIANT
   fprintf(stderr, "     marshalVariant(...), branch: else (marshal as plain string) \n");
#endif
#ifdef MALLOC_USE_REXX
        localSignature=(char *) RexxAllocateMemory( dimensions+2 );
        memset(localSignature, 0, dimensions+2);
#else
        localSignature=(char *) dbus_malloc0(dimensions+2);  // that should be enough
#endif
        for (size_t i=0; i<dimensions; i++, localSignature++)
        {
            *localSignature='a';
        }
        *localSignature='s';
    }


#ifdef DEBUG_VARIANT
    fprintf(stderr, "     - localSignature=[%s], bRopIsNull=[%d]\n", localSignature, (int) bRopIsNull); fflush(stderr);
#endif

        // create a new copy of the DICT_MARSHAL to isolate the caller's one from changes in marshal(...)
    STRUCT_MARSHAL localM;
    SET_NEW_STRUCT_MARSHAL (&localM, localSignature, pmarsh);   // initialize new DICT_MARSH
        // do not include brackets
    localM.elementStart=localSignature;
    localM.elementEnd  =localSignature+localM.signatureLength-1;
    localM.cursor=localM.elementStart;  // let cursor point to element's start

#ifdef DEBUG_VARIANT_2
   fprintf(stderr, "     marshalVariant(...): OPEN container ---> ---> ---> local signature=[%s]\n", localSignature);
#endif

        // open_container
    if (!dbus_message_iter_open_container(iter, DBUS_TYPE_VARIANT, localSignature, &iter_variant))
    {
        pmarsh->dbusError=true;
        SNPRINTF(pmarsh->msgChunk256, 256, ": DBus-API returned 'no memory' while marshalling a variant (a \"VARIANT\") using the 'dbus_message_iter_open_container()'-API");
#ifdef MALLOC_USE_REXX
        RexxFreeMemory(localSignature);  // that should be enough
#else
        dbus_free(localSignature);
#endif
        if (bReleaseLocalReferenceRop==true)
        {
            pmarsh->context->ReleaseLocalReference(rop);
        }
        return false;
    }

        // process variant
    if (!marshal(&iter_variant, *localM.cursor, rop, &localM))   // an error has occurred !
    {
        pmarsh->dbusError           = localM.dbusError;
        pmarsh->rexxError           = localM.rexxError;
        pmarsh->errorPosInSignature = pmarsh->elementStart+(localM.errorPosInSignature-localM.elementStart);
#ifdef MALLOC_USE_REXX
        RexxFreeMemory(localSignature);  // that should be enough
#else
        dbus_free(localSignature);
#endif
        if (bReleaseLocalReferenceRop==true)
        {
            pmarsh->context->ReleaseLocalReference(rop);
        }
        return false;
    }

        // close_container
    dbus_message_iter_close_container (iter, &iter_variant);
#ifdef DEBUG_VARIANT_2
    fprintf(stderr, "     marshalVariant(...): CLOSE container <--- <--- <--- signature=[%s]\n", localSignature);
#endif

#ifdef MALLOC_USE_REXX
    RexxFreeMemory(localSignature);  // that should be enough
#else
    dbus_free(localSignature);
#endif

    if (bReleaseLocalReferenceRop==true)
    {
        pmarsh->context->ReleaseLocalReference(rop);
    }

    return true;
}







// -----------------------------------------------------------------------------------------------------
// --------------------------------- helper function ---------------------------
// append arguments onto msg args
// let the helper function do the work (and raise an error, if necessary)
/** Append arguments onto the message arguments.
*
* @param dbusMsg the DBusMessage to work with
* @param context the RexxThreadContext to interact with ooRexx
* @param argUnmarshalAyAsString if <code>true</code> unmarshal byte arrays as Rexx strings
* @param errorConditionText in case of an error the error text to use
* @return Rexx array object containing the unmarshalled argument values
*/
RexxArrayObject helperUnmarshalMessageArgs( DBusMessage  *dbusMsg,       // DBUS message
                                       RexxThreadContext *context,       // allow callee to raise condition
                                       logical_t          argUnmarshalAyAsString,   // connection's attribute "unmarshalByteArrayAsString"
                                       CSTRING            errorConditionText // e.g.: "%.16s/method/DbusBusSignalMessage(), error 2: %s"    // base condition message
                                      )
{
#if defined (DEBUG_FUNCTIONS) || defined (DEBUG_MARSHAL)
   fprintf(stderr, "---> arrived in helperUnmarshalMessageArgs(...) \n");
#endif
    RexxArrayObject rxResult=NULLOBJECT;

    DBusMessageIter   iter;                     // define iterator
    if (!dbus_message_iter_init (dbusMsg, &iter))  // returns NULL, if no return value
    {
#ifdef DEBUG_UNMARSHAL
    fprintf(stderr, " ...  helperUnmarshalMessageArgs(...): no return value as indicated by dbus_message_iter_init() - RETURNING, rxResult=[%p] !\n", rxResult);fflush(stderr);
#endif

        return rxResult;
    }

    rxResult=context->NewArray(0);

    CSTRING signature=dbus_message_get_signature(dbusMsg);  // fetch signature from reply message

    char      msgChunk[256]="";    // passed on to marshal(...) for filling in error text

    STRUCT_MARSHAL marsh;       // create and initialize marshalling structure
    memset(&marsh, 0, size_STRUCT_MARSHAL);

    marsh.context  =context;
    marsh.signature=(char *) signature;
    marsh.signatureLength=strlen(marsh.signature);
    marsh.cursor   =marsh.signature;    // start out with first char of signature
    marsh.elementEnd=marsh.signature+marsh.signatureLength-1;
    marsh.dbusError=false;
    marsh.rexxError=false;

    marsh.unmarshal_ay_as_string=argUnmarshalAyAsString;    // save for use in unmarshalling byte arrays

    marsh.msgChunk256=(char *) msgChunk;

    marsh.arrayClass=context->FindClass("ARRAY");   // fetch and store the array class object

#ifdef DEBUG_UNMARSHAL
    fprintf(stderr, "\n ...  helperUnmarshalMessageArgs(...): about to start unmarshalling, signature=[%s] !\n", signature);fflush(stderr);
#endif

    if (!unmarshal(&iter, *signature, rxResult, &marsh))   // an error has occurred !
    {
        char      msgChunk2[512]="";
        SNPRINTF(msgChunk2, 512, "Rexx unmarshalling error at position (1-based): [%ld] in full signature: [%s]",
                                  (long int) (marsh.errorPosInSignature-marsh.signature+1),
                                  marsh.signature
                );

        char      msgChunk3[1024]="";
        SNPRINTF( msgChunk3, 1024, errorConditionText, DLLNAME, msgChunk2);
         // raise exception
        context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msgChunk3));

        if (rxResult!=NULLOBJECT)
        {
            context->ReleaseLocalReference(rxResult);
        }

        return NULLOBJECT ;     // return prematurely
    }

#ifdef DEBUG_UNMARSHAL
    fprintf(stderr, "\n ...  helperUnmarshalMessageArgs(...): unmarshalling SUCCESS, RxResult=[%p], dims=[%lu], items=[%lu], size=[%lu] !\n",
                             rxResult,
                             (unsigned long) context->ArrayDimension(rxResult),
                             (unsigned long) context->ArrayItems    (rxResult),
                             (unsigned long) context->ArraySize     (rxResult)

           );

    RexxObjectPtr tmpO=context->ArrayAt(rxResult,1);
    fprintf(stderr, "\tentry[1]=[%p]=[%s]", tmpO, context->ObjectToStringValue(tmpO));

    if (context->IsArray(tmpO))
    {
        fprintf(stderr, ", dims=[%lu], items=[%lu], size=[%lu]",
                           (unsigned long) context->ArrayDimension((RexxArrayObject) tmpO),
                           (unsigned long) context->ArrayItems    ((RexxArrayObject) tmpO),
                           (unsigned long) context->ArraySize     ((RexxArrayObject) tmpO) );
    }
    fprintf(stderr, "\n");
    fflush(stderr);
#endif

#if defined (DEBUG_FUNCTIONS) || defined (DEBUG_MARSHAL)
    fprintf(stderr, "<=== about to return from helperUnmarshalMessageArgs(): rxResult ! \n");
#endif

    return rxResult;            // return unmarshalled return arguments
}



// -----------------------------------------------------------------------------------------------------
    // append arguments onto msg args
        // let the helper function do the work (and raise an error, if necessary)
/** Append arguments onto the message arguments.
*
* @param dbusMsg the DBusMessage to work with
* @param iter the DBus iteration structure
* @param signature the signature to use for marshalling
* @param neededNumberOfRexxArgs number of arguments (to marshal)
* @param rexxArgs a Rexx array containing the arguments to marshal
* @param context the RexxThreadContext to interact with ooRexx
* @param errorConditionText in case of an error the error text to use
*/
void helperMarshalMessageArgs( DBusMessage      *dbusMsg,       // DBUS message
                              DBusMessageIter   *iter,          // DBUS args pointer
                              CSTRING            signature,     // signature for arguments, if any
                              size_t             neededNumberOfRexxArgs,    // number of Rexx arguments to process matching signature
                              RexxArrayObject    rexxArgs,      // rexx arguments
                              RexxThreadContext *context,       // allow callee to raise condition
                              CSTRING            errorConditionText // e.g.: "%.16s/method/DbusBusSignalMessage(), error 2: %s"    // base condition message
                              )
{
#if defined (DEBUG_FUNCTIONS) || defined (DEBUG_MARSHAL)
   fprintf(stderr, "---> arrived in helperMarshalMessageArgs(...), signature=[%s], rexxArgs=[%p] \n", signature, rexxArgs);

   if (context->IsArray(rexxArgs))
   {
       fprintf(stderr, "     rexxArgs=[%p], size=[%lu], items=[%lu]\n",
                          rexxArgs,
                         (unsigned long) (rexxArgs==NULL ? 0 : context->ArraySize(rexxArgs)),
                         (unsigned long) (rexxArgs==NULL ? 0 : context->ArrayItems(rexxArgs))
              );
   }
   else
   {
       fprintf(stderr, "     rexxArgs is NOT an array! rexxArgs~class=[%s] \n",
                         (rexxArgs==NULL ? "n/a" :
                         context->ObjectToStringValue(context->SendMessage0(rexxArgs,"CLASS") )
                         )
                         );
   }

#endif

    if (signature==NULL)  // no arguments to process
    {
        return;
    }

    if (rexxArgs==NULL)      // no arguments given, supply an empty array instead
    {
        rexxArgs=context->NewArray(0);
    }


    size_t    rai=1;    // RexxArrayIndex
    size_t    sigLen=strlen(signature);
    char      msgChunk[256]="";    // passed on to marshal(...) for filling in error text

    STRUCT_MARSHAL marsh;       // create and initialize marshalling structure
    memset(&marsh, 0, size_STRUCT_MARSHAL);

    marsh.context  =context;
    marsh.signature=(char *) signature;
    marsh.signatureLength=strlen(marsh.signature);
    marsh.cursor   =marsh.signature;    // start out with first char of signature
    marsh.dbusError=false;
    marsh.rexxError=false;
    marsh.msgChunk256=(char *) msgChunk;

    for (rai=1; rai<=neededNumberOfRexxArgs && rai<=sigLen; rai++)   // iterate over all arguments
    {
        char typeCode=*marsh.cursor;      // cursor will be moved in marshal code
        RexxObjectPtr rop=context->ArrayAt(rexxArgs,rai);   // can return Nil(), but also NULL|NULLOBJECT

#if defined (DEBUG_FUNCTIONS) || defined (DEBUG_MARSHAL)
            fprintf(stderr, " ...  helperMarshalMessageArgs(...):  rai=%lu/%lu, typeCode=[%c], rop=[%p]=[%s]\n",
                           (unsigned long) rai,
                           (unsigned long) neededNumberOfRexxArgs,
                           typeCode,
                           rop,
                           (rop==NULL ? NULL : context->ObjectToStringValue(rop))
                           );
                           fflush(stderr);
#endif

        if (!marshal(iter, typeCode, rop, &marsh))   // an error has occurred !
        {
            char      msgChunk2[512]="";

#ifdef _MSC_VER     // "%zu" creates runtime errors with "Microsoft (R) 32-bit C/C++ Optimizing Compiler Version 15.00.30729.01 for 80x86"
            SNPRINTF(msgChunk2, 512, "Rexx message argument # [%lu], typeCode=[%c]%s / error position (1-based): [%ld] in full signature: [%s] (maybe typeCode not supported on platform?)",
                                      (unsigned long) rai,
                                      typeCode,
                                      msgChunk,
                                      (long int) (marsh.errorPosInSignature-marsh.signature+1),
                                      marsh.signature
                                      );
#else
            SNPRINTF(msgChunk2, 512, "Rexx message argument # [%zu], typeCode=[%c]%s / error position (1-based): [%ld] in full signature: [%s] (maybe typeCode not supported on platform?)",
                                      rai,
                                      typeCode,
                                      msgChunk,
                                      (long int) (marsh.errorPosInSignature-marsh.signature+1),
                                      marsh.signature
                                      );
#endif

            char      msgChunk3[1024]="";
            SNPRINTF( msgChunk3, 1024, errorConditionText, DLLNAME, msgChunk2);
             // raise exception
            context->ReleaseLocalReference(rop);
            context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msgChunk3));
            return;     // return prematurely
        }
        context->ReleaseLocalReference(rop);
    }
}


// -----------------------------------------------------------------------
// adds some information about the message
/** Add DBus message related information to the supplied Rexx directory object which gets used as
*   the <code>slotDir</code> argument.
*
* @param context the RexxThreadContext to interact with ooRexx
* @param slotDir a Rexx directory object
* @param dbusMsg the DBusMessage to work with
* @param conn the DBus connection to use
*/
void helperCreateSlotDirEntries(RexxThreadContext *context, RexxDirectoryObject slotDir, DBusMessage *dbusMsg, RexxObjectPtr conn)
{
    const char   *str=NULL;
    RexxObjectPtr rop=NULL;
    RexxObjectPtr ropIndex=NULL;

    ropIndex=context->CString("AUTOSTART");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex,
                            (dbus_message_get_auto_start(dbusMsg) == TRUE ? context->True() : context->False()) );
    context->ReleaseLocalReference(ropIndex);

    ropIndex=context->CString("CONNECTION");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex,   conn );
    context->ReleaseLocalReference(ropIndex);


    str=dbus_message_get_destination(dbusMsg);
    if (str==NULL)
    {
        rop=context->NullString();
    }
    else
    {
        rop=context->CString(str);
    }
    ropIndex=context->CString("DESTINATION");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex,  rop );
    context->ReleaseLocalReference(ropIndex);
    context->ReleaseLocalReference(rop);


    RexxClassObject dtClz=context->FindClass("DATETIME");       // get the DateTime class
    RexxObjectPtr   dt   =context->SendMessage0(dtClz, "NEW");  // get an instance
    ropIndex=context->CString("DATETIME");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex, dt);
    //                      GET_REXX_DATE_TIME_AS_REXX_STRING(context) );
    context->ReleaseLocalReference(ropIndex);
    context->ReleaseLocalReference(dt);
    context->ReleaseLocalReference(dtClz);


    str=dbus_message_get_interface(dbusMsg);
    if (str==NULL)
    {
        rop=context->NullString();
    }
    else
    {
        rop=context->CString(str);
    }
    ropIndex=context->CString("INTERFACE");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex,    rop );
    context->ReleaseLocalReference(ropIndex);
    context->ReleaseLocalReference(rop);


    str=dbus_message_get_member(dbusMsg);
    if (str==NULL)
    {
        rop=context->NullString();
    }
    else
    {
        rop=context->CString(str);
    }
    ropIndex=context->CString("MEMBER");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex,       rop );
    context->ReleaseLocalReference(ropIndex);
    context->ReleaseLocalReference(rop);


    int msgType=dbus_message_get_type(dbusMsg);
    rop=context->Int32ToObject(msgType);
    ropIndex=context->CString("MESSAGETYPE");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex,   rop);
    context->ReleaseLocalReference(ropIndex);
    context->ReleaseLocalReference(rop);


    str=dbus_message_type_to_string(msgType);
    if (str==NULL)
    {
        rop=context->NullString();
    }
    else
    {
        rop=context->CString(str);
    }
    ropIndex=context->CString("MESSAGETYPENAME");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex, rop );
    context->ReleaseLocalReference(ropIndex);
    context->ReleaseLocalReference(rop);


    ropIndex=context->CString("NOREPLY");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex,
                          (dbus_message_get_no_reply(dbusMsg) == TRUE ? context->True() : context->False()) );
    context->ReleaseLocalReference(ropIndex);

    str=dbus_message_get_path(dbusMsg);
    if (str==NULL)
    {
        rop=context->NullString();
    }
    else
    {
        rop=context->CString(str);
    }
    rop=(str==NULL ? context->NullString() : context->CString(str));
    ropIndex=context->CString("OBJECTPATH");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex,   rop );
    context->ReleaseLocalReference(ropIndex);
    context->ReleaseLocalReference(rop);


    str=dbus_message_get_sender(dbusMsg);
    if (str==NULL)
    {
        rop=context->NullString();
    }
    else
    {
        rop=context->CString(str);
    }
    ropIndex=context->CString("SENDER");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex,       rop );
    context->ReleaseLocalReference(ropIndex);
    context->ReleaseLocalReference(rop);


    rop=context->UnsignedInt32ToObject(dbus_message_get_serial(dbusMsg));
    ropIndex=context->CString("SERIAL");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex,     rop);
    context->ReleaseLocalReference(ropIndex);
    context->ReleaseLocalReference(rop);


    str=dbus_message_get_signature(dbusMsg);
    if (str==NULL)
    {
        rop=context->NullString();
    }
    else
    {
        rop=context->CString(str);
    }
    ropIndex=context->CString("SIGNATURE");
    context->SendMessage2(slotDir, "SETENTRY", ropIndex,    rop );
    context->ReleaseLocalReference(ropIndex);
    context->ReleaseLocalReference(rop);
}



// -----------------------------------------------------------------------------------------------------
    // ============================================================================= //
    // - set up and run a message loop for this connection instance
    // - processes received signals and received method calls to registered ooRexx server objects
    // - this routine will call DBus.executeQueuedMessages() to make sure that DBus message calls are
    //   executed in this very thread (otherwise libdbus does not work correctly, the reason for the
    //   major rewrite in July 2014)
    // - wrapped up as a Rexx method, hence easy to run it on a separate thread
/** Set up and run a message loop for this connection instance, process received signals and received
*   method calls to registeed Rexx server objects. The message loop will call <code>executeQueuedMessages()</code>
*   to allow dispatching DBus messages in this message loop thread (libdbus messages must be received and
*   sent in the same thread, otherwise runtime errors may occur). This method will be called from
*   Rexx code which will create an own thread for it to run.
*/
RexxMethod2(RexxObjectPtr, DbusMessageLoop, CSELF, connPtr, OSELF, self)
{
#if defined (DEBUG_MESSAGELOOP) || defined (DEBUG_CONNECTION)
    fprintf(stderr, "... MessageLoop(): just arrived ! ...\n"); fflush(stderr);
#endif

    context->SetGuardOn();
    if (context->GetObjectVariable("active") == context->True())     // already running ?
    {
        context->SetGuardOff();
         // raise exception
        context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String("Message loop is already active (running)"));
        return NULLOBJECT;      // return, do not start another message loop thread
    }

        // values we might need
    RexxObjectPtr rxTrue =context->True();
    RexxObjectPtr rxFalse=context->False();
    RexxObjectPtr rxNil  =context->Nil();


#if defined (REXX_AUTO_DISCONNECT_FROM_PRIVATE_SERVER)  // if auto-disconnect from private server
        // determine, whether this is a connection to a private Rexx server running in this instance
    RexxObjectPtr tmpRop=context->GetObjectVariable("server");
    bool bClientForPrivateRexxServer= (tmpRop!=NULL && tmpRop!=rxNil);
#endif
        // Rexx objects that work as servers on the bus
    RexxDirectoryObject serviceObjects = (RexxDirectoryObject) context->GetObjectVariable("internalRegisteredServiceObjects");

        // registered Rexx listener objects for signals
    RexxArrayObject signalListeners = (RexxArrayObject) context->GetObjectVariable("internalSignalListeners");
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "... MessageLoop(): serviceObjects=[%p], signalListeners=[%p]..\n", serviceObjects, signalListeners); fflush(stderr);
#endif

    context->SetGuardOff();

    // DBUS loop to process received messages (signals, calls to ServerObjects)
    // - signals get dispatched with internalSignalListeners
    // - calls to ServerObjects including replies from our Rexx object, unless no interest in reply

    DBusConnection*  conn=(DBusConnection *) connPtr;


    DBusMessage     *dbusMsg;
    int              milliTimeout=1;    // timeout in milliseconds

    context->SetGuardOn();
    context->SetObjectVariable("ACTIVE", rxTrue); // context->True());  // indicate message processing is active
    context->SetGuardOff();

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "... MessageLoop(): about to enter message loop, changed instance variable 'active' to .true: [%s], self=[%s] self~identityHash=[%s] ...\n",
                    context->ObjectToStringValue(context->GetObjectVariable("active")),
                    context->ObjectToStringValue(self),
                    context->ObjectToStringValue(context->SendMessage0(self,"IDENTITYHASH"))
            ); fflush(stderr);
    fprintf(stderr, "... MessageLoop(): conn=[%p], connPtr=[%p] ...\n", conn, connPtr); fflush(stderr);

    size_t count=0, limit=100/milliTimeout;
#endif

        // allow loop thread to be ended by Rexx, e.g. checking the status of the "active" attribute (if .false return from message loop)
    while (dbus_connection_read_write(conn,milliTimeout) )
    {
#ifdef DEBUG_MESSAGELOOP
        if (++count>=limit)
        {
            fprintf(stderr, "... MessageLoop(): at top, 'active'=[%p]...\n", context->GetObjectVariable("active")); fflush(stderr);
            count=0;
        }
#endif

        logical_t bLeave=(context->GetObjectVariable("stopLoop") == rxTrue);

        if (bLeave)
        {
            break;
        }

#ifdef DEBUG_MESSAGELOOP
        if (++count>=limit)
        {
            fprintf(stderr, "... MessageLoop(): looped [%lu] times...\n", (unsigned long) limit); fflush(stderr);
            count=0;
        }
#endif

        dbusMsg=dbus_connection_pop_message(conn);     // segmentation fault on Ubuntu DBus 1.4.6 ! (2011-07-28)

        if (dbusMsg==NULL)      // no message retrieved
        {
            context->SendMessage0(self,"EXECUTEQUEUEDMESSAGES");    // if any messages for DBus queued, execute them!
            continue;
        }

        int msgType=dbus_message_get_type(dbusMsg);

#if defined (DEBUG_RGF)    // show all messages we receive on this connection, look for the "Disconnected" signal
// --->
fprintf(stderr, "/// ==//==>>>  ... message: type=[%d] (%s), path=[%s], interface=[%s], member=[%s] | signature=[%s] <===//===\\\\\\.......\n",
    msgType,
    dbus_message_type_to_string(msgType),
    dbus_message_get_path(dbusMsg),
    dbus_message_get_interface(dbusMsg),
    dbus_message_get_member(dbusMsg),
    dbus_message_get_signature(dbusMsg)
    ); fflush(stderr);

    if (bClientForPrivateRexxServer)
    {
fprintf(stderr, "--- --- --- bClientForPrivateRexxServer=[%d]\n", bClientForPrivateRexxServer);
    }

    CSTRING tmpInterface=dbus_message_get_interface(dbusMsg);
    CSTRING tmpMember=dbus_message_get_member(dbusMsg);

    if (msgType==DBUS_MESSAGE_TYPE_SIGNAL)
    {
        if (dbus_message_is_signal(dbusMsg,DBUS_INTERFACE_LOCAL,"Disconnected") )
        {
            fprintf(stderr, "----> SIGNAL-RECEIVED: BINGO BINGO - DISCONNECT received: interface=[%s], member=[%s]\n",
                    dbus_message_get_interface(dbusMsg), dbus_message_get_member(dbusMsg));
        }
        else
        {
            fprintf(stderr, "----> SIGNAL-RECEIVED:  interface=[%s], member=[%s]\n",
                    dbus_message_get_interface(dbusMsg), dbus_message_get_member(dbusMsg));
        }
    }

// <----
#endif


            // only process signal and call message types, ignore
        if (msgType!=DBUS_MESSAGE_TYPE_SIGNAL && msgType!=DBUS_MESSAGE_TYPE_METHOD_CALL)
        {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is not a SIGNAL or a METHOD_CALL, msgType=[%d], continue ...\n", msgType); fflush(stderr);
#endif

            dbus_message_unref(dbusMsg);    // free the message in hand
            context->SendMessage0(self,"EXECUTEQUEUEDMESSAGES");    // if any messages for DBus queued, execute them!
            continue;
        }

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message: type=[%d] (%s), path=[%s], interface=[%s], member=[%s] | sender=[%s] -> destination=[%s] | signature=[%s] ... aha!\n",
                    msgType,
                    dbus_message_type_to_string(msgType),
                    dbus_message_get_path(dbusMsg),
                    dbus_message_get_interface(dbusMsg),
                    dbus_message_get_member(dbusMsg),
                    dbus_message_get_sender(dbusMsg),
                    dbus_message_get_destination(dbusMsg),
                    dbus_message_get_signature(dbusMsg)
                    ); fflush(stderr);
#endif

            // a signal, but we have no listeners, ignore
        if (msgType==DBUS_MESSAGE_TYPE_SIGNAL )
        {

#if defined (REXX_AUTO_DISCONNECT_FROM_PRIVATE_SERVER)  // if auto-disconnect from private server
                // if a Disconnect signal on a server client connection, then send a "disconnect"
                // message to the server supplying this connection as an argument (shuts down client connection in Rexx)
            if (bClientForPrivateRexxServer) // if so, check for the "Disconnect" signal and handle it
            {
#if defined (DEBUG_RGF)     // debug private server connection to client
fprintf(stderr, "--- --- --- a SIGNAL, bClientForPrivateRexxServer=[%d], interface=[%s], member=[%s]\n",
                             bClientForPrivateRexxServer,
                             dbus_message_get_interface(dbusMsg),
                             dbus_message_get_member(dbusMsg)
                             );
#endif
                // if (dbus_message_is_signal(dbusMsg,"org.freedesktop.DBus.Local", "Disconnected") )
                if (dbus_message_is_signal(dbusMsg,DBUS_INTERFACE_LOCAL,"Disconnected") )
                {
#if defined (DEBUG_RGF)     // debug private server connection to client
fprintf(stderr, "--- --- --- a SIGNAL, BINGO BINGO BINOG - DISCONNECT received: interface=[%s], member=[%s]\n",tmpInterface, tmpMember);
#endif
                    context->SendMessage1(context->SendMessage0(self, "SERVER"), "DISCONNECT", self);
                    dbus_message_unref(dbusMsg);    // free the message in hand
                    continue;
                }
            }
#endif

                // check whether Rexx listeners are registered, if not discard signal and go ahead
            if (context->ArrayItems(signalListeners)==0)
            {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a SIGNAL, but no signal handlers registered ...\n"); fflush(stderr);
#endif
                dbus_message_unref(dbusMsg);    // free the message in hand

#ifdef DEBUG_MESSAGELOOP
fprintf(stderr, "    ... message is a SIGNAL, but no signal handlers registered, after dbus_message_unref(), before continue ...\n"); fflush(stderr);
#endif
                context->SendMessage0(self,"EXECUTEQUEUEDMESSAGES");    // if any messages for DBus queued, execute them!
                continue;
            }



        }
        else    // received a message call, but no registered servers, we just call executeQueuedMessages() to allow for processing queued messages in this particular thread...
        {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a SIGNAL, about to send ooRexx ITEMS message ...\n"); fflush(stderr);
#endif
            RexxObjectPtr rop=context->SendMessage0(serviceObjects, "ITEMS");

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a SIGNAL, RETURNED from send ooRexx ITEMS message.\n"); fflush(stderr);
#endif

            size_t    n=0;
            logical_t flag=false;
            flag = context->ObjectToStringSize(rop, &n);

            if (flag==false || n==0)
            {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a METHOD_CALL, but no server objects registered ...\n"); fflush(stderr);
#endif
                dbus_message_unref(dbusMsg);    // free the message in hand
                context->SendMessage0(self,"EXECUTEQUEUEDMESSAGES");    // if any messages for DBus queued, execute them!
                context->ReleaseLocalReference(rop);
                continue;
            }
            context->ReleaseLocalReference(rop);
        }

            // create the slotDir argument, supply the message's interesting information with it
        RexxDirectoryObject slotDir=NULL;

        RexxObjectPtr rop1=context->GetObjectVariable("MAKESLOTDIR");
        bool bMakeSlotDir = rop1==rxTrue;
        context->ReleaseLocalReference(rop1);

        rop1=context->GetObjectVariable("COLLECTSTATISTICS");
        bool bCollectStatistics= rop1==rxTrue;
        context->ReleaseLocalReference(rop1);

        if ( bMakeSlotDir || bCollectStatistics) // should we create a slotDir-argument containing message related information?
        {
            slotDir=context->NewDirectory();
            helperCreateSlotDirEntries(context->threadContext, slotDir, dbusMsg, self);  // adds some information about the message
        }

            // unmarshal arguments
        RexxArrayObject arrArgs=NULL;
        arrArgs=helperUnmarshalMessageArgs(dbusMsg,
                       context->threadContext,
                       (context->GetObjectVariable("UNMARSHALBYTEARRAYASSTRING")==rxTrue), // context->True()),
                       "%.16s/method/DbusMessageLoop(), error 99: %s"    // base condition message
                      );

        if (arrArgs==NULL)  // make sure we have at least an empty array
        {
            arrArgs=context->NewArray(0);
        }

        if (bMakeSlotDir && slotDir!=NULL)
        {
            context->SendMessage1(arrArgs, "APPEND", slotDir);  // append slotDir
        }

            // get Interface and Member
        CSTRING interfaceName=dbus_message_get_interface(dbusMsg);

        RexxStringObject rxInterface=( interfaceName==NULL ? context->NullString() : context->String(interfaceName) ) ;

        CSTRING member   =dbus_message_get_member(dbusMsg);
        RexxStringObject rxMember= (member==NULL ? context->NullString() : context->String(member));

        RexxArrayObject arr2=context->NewArray(2);   // arguments for

            // a signal, then invoke all listeners
        if (msgType==DBUS_MESSAGE_TYPE_SIGNAL)
        {
            size_t items=context->ArraySize(signalListeners);
            context->SetGuardOn();      // exclusive access to object variables/attributes
            for (size_t i=1; i<= items; i++)
            {

                RexxObjectPtr rop=context->ArrayAt(signalListeners,i);  // fetch listener structure
                if (rop==NULL)      // no object at array's position ?
                {
                    continue;
                }

                RexxObjectPtr rxTmp=context->SendMessage0(rop, "INTERFACE");    // get interface
                if (rxTmp!=rxNil)   // given, hence must match!
                {
                    RexxObjectPtr ropRes=context->SendMessage1(rxTmp, "equals", rxInterface);
                    if (ropRes!=rxTrue)
                    {
                        context->ReleaseLocalReference(ropRes);
                        continue;
                    }
                    context->ReleaseLocalReference(ropRes);
                }

                rxTmp=context->SendMessage0(rop, "SIGNALNAME");   // get interface
                if (rxTmp!=rxNil)   // given, hence must match!
                {
                    if (context->SendMessage1(rxTmp, "equals", rxMember)!=rxTrue)
                    {
                        context->ReleaseLocalReference(rxTmp);
                        continue;
                    }
                }
                context->ReleaseLocalReference(rxTmp);

                // synchroneously send signal to Rexx listener
                rxTmp=context->SendMessage0(rop, "LISTENEROBJECT");   // get interface

                context->ArrayPut(arr2, rxMember, 1);
                context->ArrayPut(arr2, arrArgs,  2);


#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a SIGNAL, dispatching to Rexx ...\n"); fflush(stderr);
#endif

                if (bCollectStatistics)     // determined at top of message loop, once it's clear we need to handle the message
                {
                    RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");

                    CSTRING sentKind ="RECEIVED";
                    CSTRING kind     ="LASTRECEIVEDSIGNAL";
                    CSTRING countKind="COUNTRECEIVEDSIGNALS";
                    context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available

                    RexxObjectPtr ropTmp=context->DirectoryAt(slotDir,"DATETIME");
                    context->DirectoryPut(rdo,ropTmp,sentKind);   // adjust "RECEIVED" datetime-stamp

                        // increase kind's count
                    RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                    RexxObjectPtr ropIncr1=context->Int32ToObject(1);
                    count=context->SendMessage1(count, "+", ropIncr1);
                    context->DirectoryPut(rdo,count,countKind);

                    context->ReleaseLocalReference(count);
                    context->ReleaseLocalReference(ropIncr1);
                    context->ReleaseLocalReference(ropTmp);
                    context->ReleaseLocalReference(rdo);
                }

                // dispatch asynchroneously (no result expected), allows concurrent communication via the connection!
                context->SendMessage(rxTmp, "STARTWITH", arr2);   // one-way call, hence not interested in any results

                context->ReleaseLocalReference(rxTmp);
                context->ReleaseLocalReference(rop);
            }
            context->SetGuardOff();     // release exclusive access to object variables/attributes


#if defined (REXX_AUTO_DISCONNECT_FROM_PRIVATE_SERVER)  // if auto-disconnect from private server
                // if a Disconnect signal on a server client connection, then send a "disconnect"
                // message to the server supplying this connection as an argument
            if (bClientForPrivateRexxServer)
            {
                if (dbus_message_is_signal(dbusMsg,DBUS_INTERFACE_LOCAL,"Disconnected") )
                {
#if defined (DEBUG_RGF)     // debug private server connection to client
    fprintf(stderr, "---- BINGO BINGO #2 - DISCONNECT received: interfaceName=[%s], member=[%s]\n",interfaceName, member);
#endif
                        // send DISCONNECT message to server, supplying this connection instance as an argument
                    RexxObjectPtr ropRes=context->SendMessage0(self, "SERVER");
                    context->SendMessage1(ropRes, "DISCONNECT", self);
                    context->ReleaseLocalReference(ropRes);
                }
            }
#endif
        }

        // a message call, then invoke the message on the service object
        else
        {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "\n    ... message is a CALL ...\n"); fflush(stderr);
#endif
            // maybe create condition in listener objects and waiting server objects, if any ?
            CSTRING objectPath   =dbus_message_get_path(dbusMsg);

            dbus_uint32_t serial = 0;

            context->SetGuardOn();      // exclusive access to object variables/attributes
            RexxObjectPtr rop=context->DirectoryAt(serviceObjects, objectPath);  // fetch Rexx service object
            if (rop==NULL || rop==rxNil)     // no service object by object path name found, try to fetch the "default" service object
            {
                rop=context->DirectoryAt(serviceObjects, "default"); // fetch Rexx default service object, if any
            }
            context->SetGuardOff();     // release exclusive access to object variables/attributes

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a CALL, objectPath=[%p]: rop=[%p] rop==.nil=[%d] ...\n", objectPath, rop, rop==rxNil); fflush(stderr);
#endif

            if (rop==NULL || rop==rxNil)     // error, service object does not exist !
            {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a CALL, service object for object path [%s] does not exist! \n", objectPath); fflush(stderr);
#endif
                char errMsg[512]="";
                SNPRINTF(errMsg, 512, "Rexx service object [%s] does not exist (you intended to invoke member=[%s] in the interfaceName=[%s]",
                                      objectPath,
                                      member,
                                      interfaceName);

                DBusMessage *errReply=dbus_message_new_error(dbusMsg,
                                            "org.freedesktop.DBus.Error.ServiceUnknown",
                                            errMsg
                                           );

                // send the reply && flush the connection
                if (!dbus_connection_send(conn, errReply, &serial))
                {
                   fprintf(stderr, "%s/DbusMessageLoop: out of memory while sending DBUS error return message!\n", DLLNAME);
                }
                dbus_connection_flush(conn);

                if (bCollectStatistics)     // determined at top of message loop, once it's clear we need to handle the message
                {
                    context->SetGuardOn();          // update statistics on send signal
                    RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");
                    RexxDirectoryObject slotDir=context->NewDirectory();
                    helperCreateSlotDirEntries(context->threadContext, slotDir, errReply, self);   // setup slotDir
                    CSTRING errName=dbus_message_get_error_name(errReply);
                    if (errName!=NULL)
                    {
                        RexxObjectPtr rop2=context->String(errName);
                        context->DirectoryPut(slotDir,rop2,"ERRORNAME");
                        context->ReleaseLocalReference(rop2);
                    }

                    CSTRING sentKind ="SENT";
                    CSTRING kind     ="LASTSENTERROR";
                    CSTRING countKind="COUNTSENTERRORS";
                    context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available

                    RexxObjectPtr rop=context->DirectoryAt(slotDir,"DATETIME");
                    context->DirectoryPut(rdo,rop,sentKind);   // adjust "RECEIVED" datetime-stamp
                    context->ReleaseLocalReference(rop);

                        // increase kind's count
                    RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                    RexxObjectPtr rop1=context->Int32ToObject(1);
                    count=context->SendMessage1(count, "+", rop1);
                    context->ReleaseLocalReference(rop1);
                    context->DirectoryPut(rdo,count,countKind);
                    context->SetGuardOff();

                    context->ReleaseLocalReference(count);
                    context->ReleaseLocalReference(slotDir);
                    context->ReleaseLocalReference(rdo);
                }

                dbus_message_unref(errReply);
            }
            else    // call Rexx method, if no reply intended, dispatch on separate thread, else execute and create answer and reply it
            {
                if (bCollectStatistics)     // determined at top of message loop, once it's clear we need to handle the message
                {
                    context->SetGuardOn();
                    RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");

                    CSTRING sentKind ="RECEIVED";
                    CSTRING kind     ="LASTRECEIVEDMESSAGE";
                    CSTRING countKind="COUNTRECEIVEDMESSAGES";
                    context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available

                    RexxObjectPtr ropTmp=context->DirectoryAt(slotDir,"DATETIME");
                    context->DirectoryPut(rdo,ropTmp,sentKind);   // adjust "RECEIVED" datetime-stamp
                    context->ReleaseLocalReference(ropTmp);

                        // increase kind's count
                    RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                    RexxObjectPtr rop1=context->Int32ToObject(1);
                    count=context->SendMessage1(count, "+", rop1);
                    context->ReleaseLocalReference(rop1);

                    context->DirectoryPut(rdo,count,countKind);
                    context->SetGuardOff();

                    context->ReleaseLocalReference(count);
                    context->ReleaseLocalReference(rdo);
                }

                RexxObjectPtr rxResult=NULL;
                logical_t bMarshallingError=false;
                logical_t bRexxCondition=false;

                context->ArrayPut(arr2, rxMember, 1);
                context->ArrayPut(arr2, arrArgs,  2);

                // if no return value in signature, do not return anything, but dispatch asynchroneously
                if (dbus_message_get_no_reply(dbusMsg)) // dispatch via separate thread, we don't care about Rexx conditions there ...
                {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "\n    ... message is a CALL, 1 - sending message [%s] (interfaceName=[%s]) to service object for [%s] ASYNCHRONEOUSLY (no reply expected) ... \n", member, interfaceName, objectPath); fflush(stderr);
#endif

                    context->SendMessage(rop, "STARTWITH", arr2);

                    // send an empty reply-message, controlled by connection's attribute 'sendEmptyReply'
                    if (context->GetObjectVariable("SENDEMPTYREPLY")==rxTrue)
                    {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "\n    ... message is a CALL, 1 - return EMPTY REPLY for message [%s] (interfaceName=[%s]) to service object for [%s] ASYNCHRONEOUSLY (controlled by attribute \"sendEmptyReply\") ... \n", member, interfaceName, objectPath); fflush(stderr);
#endif
                        DBusMessage *returnMsg=dbus_message_new_method_return(dbusMsg);
                        dbus_uint32_t serial = 0;
                        if (!dbus_connection_send(conn, returnMsg, &serial))
                        {
                           fprintf(stderr, "%s/DbusMessageLoop: out of memory while sending DBUS return message!\n", DLLNAME);
                        }
                        dbus_connection_flush(conn);

                        if (bCollectStatistics) // allows us to guard on only, if statistics should be collected !
                        {
                            context->SetGuardOn();          // update statistics on call method
                            RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");
                            RexxDirectoryObject slotDir=context->NewDirectory();
                            helperCreateSlotDirEntries(context->threadContext, slotDir, returnMsg, self);   // setup slotDir

                            CSTRING sentKind ="SENT";
                            CSTRING kind     ="LASTSENTMESSAGE";
                            CSTRING countKind="COUNTSENTMESSAGES";
                            context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available

                            RexxObjectPtr ropTmp=context->DirectoryAt(slotDir,"DATETIME");
                            context->DirectoryPut(rdo,ropTmp,sentKind);   // adjust "RECEIVED" datetime-stamp
                            context->ReleaseLocalReference(ropTmp);

                                // increase kind's count
                            RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                            RexxObjectPtr rpo1=context->Int32ToObject(1);
                            count=context->SendMessage1(count, "+", rpo1);
                            context->DirectoryPut(rdo,count,countKind);
                            context->SetGuardOff();

                            context->ReleaseLocalReference(count);
                            context->ReleaseLocalReference(slotDir);
                            context->ReleaseLocalReference(rdo);
                        }
                        dbus_message_unref(returnMsg);
                    }
                }

                else                                    // dispatch synchroneously
                {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "\n    ... message is a CALL, 2 - about to send message [%s] (interfaceName=[%s]) to service object for [%s] ... \n", member, interfaceName, objectPath); fflush(stderr);
#endif

                    rxResult=context->SendMessage(rop, "SENDWITH", arr2);

                    if (context->CheckCondition())      // oops a Rexx condition got raised
                    {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a CALL, 2a - message [%s] (interfaceName=[%s]) to service object for [%s], caused a Rexx CONDITION :-( ... \n", member, interfaceName, objectPath); fflush(stderr);
#endif

                        RexxDirectoryObject condObj=context->GetConditionInfo();  // condition object !
                        bRexxCondition=true;

                        bool bSpecialReply=false;   // if true, then it got invoeked via dbus.cls' routine "raiseDBusError"
                        RexxArrayObject rao=(RexxArrayObject) context->SendMessage0(condObj,"ADDITIONAL");
                        if (rao!=NULL && rao!=rxNil)
                        {
                            RexxObjectPtr rop3=context->ArrayAt(rao,3);  // get third entry
                            if (rop3!=NULL && rop3!=rxNil)
                            {
                                CSTRING fel=context->ObjectToStringValue(rop3);
                                bSpecialReply= ( (fel[ 0]=='E' || fel[ 0]=='e') &&
                                                 (fel[ 1]=='R' || fel[ 1]=='r') &&
                                                 (fel[ 2]=='R' || fel[ 2]=='r') &&
                                                 (fel[ 3]=='O' || fel[ 3]=='o') &&
                                                 (fel[ 4]=='R' || fel[ 4]=='r') &&
                                                 (fel[ 5]=='R' || fel[ 5]=='r') &&
                                                 (fel[ 6]=='E' || fel[ 6]=='e') &&
                                                 (fel[ 7]=='P' || fel[ 7]=='p') &&
                                                 (fel[ 8]=='L' || fel[ 8]=='l') &&
                                                 (fel[ 9]=='Y' || fel[ 9]=='y')
                                );
                            }
                            context->ReleaseLocalReference(rop3);
                        }

                        DBusMessage *errReply=NULL;

                        if (bSpecialReply)      // o.k. pass on received error name and error message
                        {
                            context->ClearCondition();     // let the service object continue to run
                            CSTRING errName=NULL;
                            CSTRING errMsg =NULL;
                            RexxObjectPtr rop1=context->ArrayAt(rao,1);  // get first entry
                            if (rop1!=NULL)
                            {
                                errName=context->ObjectToStringValue(rop1);
                            }

                            RexxObjectPtr rop2=context->ArrayAt(rao,2);  // get first entry
                            if (rop2!=NULL && rop2!=rxNil)  // only if a value supplied for error message
                            {
                                errMsg=context->ObjectToStringValue(rop2);
                            }
                            errReply=dbus_message_new_error(dbusMsg,errName,errMsg);

                            context->ReleaseLocalReference(rop2);
                            context->ReleaseLocalReference(rop1);
                        }
                        else    // an unexpected Rexx error has occurred, communicate it to the client
                        {
                            char errMsg[512]="";
                            SNPRINTF(errMsg, 512, "Rexx service object [%s] running method=[%s] in the interfaceName=[%s] raised a Rexx condition",
                                                  objectPath,
                                                  member,
                                                  interfaceName);

                             char *rexxLikeErrorMsg=RgfCreateRexxlikeErrorInfo (context->threadContext, condObj, errMsg);

                             errReply=dbus_message_new_error(dbusMsg,
                                  REXX_ERROR_MESSAGE_CONDITION_RAISED,
                                  rexxLikeErrorMsg
                                  );

                             RexxFreeMemory(rexxLikeErrorMsg);  // make sure we free the error message buffer
                        }

                             // send the error reply && flush the connection
                         if (!dbus_connection_send(conn, errReply, &serial))
                         {
                            fprintf(stderr, "%s/DbusMessageLoop: out of memory while sending DBUS error return message!\n", DLLNAME);
                            fflush(stderr);
                         }
                         dbus_connection_flush(conn);

                         if (bCollectStatistics)     // determined at top of message loop, once it's clear we need to handle the message
                         {
                             context->SetGuardOn();          // update statistics on send signal
                             RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");
                             RexxDirectoryObject slotDir=context->NewDirectory();
                             helperCreateSlotDirEntries(context->threadContext, slotDir, errReply, self);   // setup slotDir
                             CSTRING errName=dbus_message_get_error_name(errReply);
                             if (errName!=NULL)
                             {
                                 RexxObjectPtr rpoTmp=context->String(errName);
                                 context->DirectoryPut(slotDir,rpoTmp,"ERRORNAME");
                                 context->ReleaseLocalReference(rpoTmp);
                             }

                             CSTRING sentKind ="SENT";
                             CSTRING kind     ="LASTSENTERROR";
                             CSTRING countKind="COUNTSENTERRORS";
                             context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available

                             RexxObjectPtr rpoTmp=context->DirectoryAt(slotDir,"DATETIME");
                             context->DirectoryPut(rdo,rpoTmp,sentKind);   // adjust "RECEIVED" datetime-stamp
                             context->ReleaseLocalReference(rpoTmp);

                                 // increase kind's count
                             RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                             RexxObjectPtr rop1=context->Int32ToObject(1);
                             count=context->SendMessage1(count, "+", rop1);
                             context->DirectoryPut(rdo,count,countKind);
                             context->SetGuardOff();

                             context->ReleaseLocalReference(rop1);
                             context->ReleaseLocalReference(slotDir);
                             context->ReleaseLocalReference(rdo);
                         }

                         dbus_message_unref(errReply);

                         context->ClearCondition();   // rgf, 2014-07-30, new strategy

                         context->ReleaseLocalReference(rao);
                         context->ReleaseLocalReference(condObj);
                    }

                    else if (rxResult==NULLOBJECT) //  || rxResult==rxNil)   // no return value, do not reply
                    {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a CALL, 2b - message [%s] (interfaceName=[%s]) to service object for [%s], no condition, no return value, PROBLEM as caller expects one! :( \n", member, interfaceName, objectPath); fflush(stderr);
#endif
                        char errMsg[512]="";
                        SNPRINTF(errMsg, 512, "Rexx service object [%s] running method=[%s] in the interfaceName=[%s] did not return a value, although client expects one",
                                              objectPath,
                                              member,
                                              (interfaceName==NULL ? "" : interfaceName)
                                              );

                         DBusMessage *errReply=dbus_message_new_error(dbusMsg,
                                                     REXX_ERROR_MESSAGE_NO_RETURN_VALUE, // "org.freedesktop.DBus.Error.RexxNoReturnValue",
                                                     errMsg // rexxLikeErrorMsg
                                                    );

                             // send the error reply && flush the connection
                         if (!dbus_connection_send(conn, errReply, &serial))
                         {
                            fprintf(stderr, "%s/DbusMessageLoop: out of memory while sending DBUS error return message!\n", DLLNAME);
                         }
                         dbus_connection_flush(conn);

                         if (bCollectStatistics)     // determined at top of message loop, once it's clear we need to handle the message
                         {
                             context->SetGuardOn();          // update statistics on send signal
                             RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");
                             RexxDirectoryObject slotDir=context->NewDirectory();
                             helperCreateSlotDirEntries(context->threadContext, slotDir, errReply, self);   // setup slotDir
                             CSTRING errName=dbus_message_get_error_name(errReply);
                             if (errName!=NULL)
                             {
                                 RexxObjectPtr rpoTmp=context->String(errName);
                                 context->DirectoryPut(slotDir,rpoTmp,"ERRORNAME");
                                 context->ReleaseLocalReference(rpoTmp);
                             }

                             CSTRING sentKind ="SENT";
                             CSTRING kind     ="LASTSENTERROR";
                             CSTRING countKind="COUNTSENTERRORS";
                             context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available

                             RexxObjectPtr rpoTmp=context->DirectoryAt(slotDir,"DATETIME");
                             context->DirectoryPut(rdo,rpoTmp,sentKind);   // adjust "RECEIVED" datetime-stamp
                             context->ReleaseLocalReference(rpoTmp);

                                 // increase kind's count
                             RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                             RexxObjectPtr rpo1=context->Int32ToObject(1);
                             count=context->SendMessage1(count, "+", rpo1);
                             context->DirectoryPut(rdo,count,countKind);
                             context->SetGuardOff();

                             context->ReleaseLocalReference(rpo1);
                             context->ReleaseLocalReference(slotDir);
                             context->ReleaseLocalReference(rdo);
                         }

                         dbus_message_unref(errReply);
                    }


                    else    // process Rexx result and reply message call
                    {

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a CALL, 3 - message [%s] (interfaceName=[%s]) to service object for [%s], no condition, a return value to be processed! :( \n", member, interfaceName, objectPath); fflush(stderr);

    fprintf(stderr, "    ... message is a CALL, 3i - rxResult=[%p], rxResult~class=[%s], if array, arr[1]=[%s], arr[2]=[%s]\n",
                             rxResult,
                             context->ObjectToStringValue(context->SendMessage0(rxResult, "CLASS")),
                             (context->IsArray(rxResult) ? (context->ObjectToStringValue(context->ArrayAt((RexxArrayObject) rxResult,1)))  : NULL),
            (context->IsArray(rxResult) ? (context->ObjectToStringValue(context->ArrayAt((RexxArrayObject) rxResult,2)))  : NULL)
            );
#endif

                        DBusMessage *returnMsg=dbus_message_new_method_return(dbusMsg);

                        DBusMessageIter iter;
                        dbus_message_iter_init_append(returnMsg, &iter);  // initialize iterator

                        char signature[256]="";
                        RexxArrayObject arrRxResult=NULL;


                        size_t dimension=0;
                        if (context->IsArray(rxResult))
                        {
                            dimension=context->ArrayDimension((RexxArrayObject)rxResult);
                        }

                        // if a method returns the result with replySignature, then that replySignature
                        // overrules every other replySignature that may possibly exist (e.g. in
                        // introspection definitions)

                            // no reply signature? determine signature for return value, if possible
                        if (!*signature &&                          // no reply signature as of yet
                              dimension                                          >  0 &&    // an array in hand
                              dimension                                          == 1 &&
                              context->ArraySize((RexxArrayObject)rxResult)      == 2 &&
                              context->ArrayItems((RexxArrayObject)rxResult)     == 2
                           )
                        {
                            // this branch expects .array~of("replySignature=x", returnValueMatchingSignature)

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a CALL, IsArray(rxResult): message [%s] (interfaceName=[%s]) to service object for [%s], no condition, returned an array object ... \n", member, interfaceName, objectPath); fflush(stderr);
#endif
                            dimension=context->ArrayDimension((RexxArrayObject) rxResult);

                            // if single-dimensioned array and first element starts with "replySignature="
                            // then use string immediately following '=' as reply signature, and remaining
                            // array element as the return value
                            // CSTRING fel= context->ObjectToStringValue(context->ArrayAt((RexxArrayObject) rxResult, 1));
                            RexxObjectPtr rpoTmp=context->ArrayAt((RexxArrayObject) rxResult, 1);
                            CSTRING fel= context->ObjectToStringValue(rpoTmp);
                            context->ReleaseLocalReference(rpoTmp);

                            if (OVERRIDE_SIGNATURE_GIVEN(fel))
                            {
                                RexxObjectPtr arr1=context->ArrayAt((RexxArrayObject) rxResult, 1);
                                if (context->IsString(arr1))
                                {
                                    size_t offset=17;   // offset from where to pickup signature

                                    while (fel[offset]==' ' || fel[offset]=='\t') // skip empty
                                    {
                                        offset++;
                                    }

                                    CSTRING localSignature=fel+offset;

                                    logical_t sigOK=dbus_signature_validate(localSignature, NULL); // not interested in error message name, hence NULL

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "... messageLoop-TESTING: --> ---> fel+15=localSignature=[%s], sigOK=[%d] <--- <-- \n",
                    localSignature, (int) sigOK);
#endif

                                    if (sigOK)  // first array element is a valid signature!
                                    {
                                        SNPRINTF(signature, 256, "%s", localSignature);
                                            // use second element of returned array object as return value matching the signature

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a CALL, message [%s] (interfaceName=[%s]) to service object for [%s] | signature=[%s] -> BINGO-SUPER-BINGO ! \n", member, interfaceName, objectPath, signature); fflush(stderr);
#endif

                                            // create new array with array arguments
                                        // determine hwo rxResult is to be sent for marshalling
                                        rxResult=context->ArrayAt((RexxArrayObject) rxResult, 2);

                                        size_t nrArgs=GET_NUMBER_OF_REXX_ARGS_FOR_SIGNATURE(signature);

                                        if (context->IsArray(rxResult))
                                        {
                                            arrRxResult=(RexxArrayObject) rxResult;

                                                // special case: only one argument to marshal, but array with multiple items supplied: box array as one arg
                                                //               (only if single argument is an array or a struct)
                                            if (nrArgs==1 &&
                                                (*signature=='a' || *signature=='(') &&
                                                context->ArrayItems(arrRxResult)>1)
                                            {
                                                context->ReleaseLocalReference(arrRxResult);
                                                arrRxResult=context->ArrayOfOne(rxResult);
                                            }
                                        }
                                        else
                                        {
                                            arrRxResult=context->ArrayOfOne(rxResult);
                                        }

                                    }
                                }
                                context->ReleaseLocalReference(arr1);
                            }
                        }


                        // if Rexx object is of type .DBusServiceObject, then try to get the
                        // replySignature from its introspection data, if any defined
                        if (!*signature && context->IsOfType(rop, "DBUSSERVICEOBJECT"))
                        {
                            RexxStringObject rso=(RexxStringObject) context->SendMessage2(rop, "SERVICE.REPLYSIGNATURE", rxMember, rxInterface);

                            if (context->CheckCondition())  // oops, error carrying out the message
                            {
                                // just ignore this (could be that user has its own class named DBUSSERVICEOBJECT with a different protocol)
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "grrrr DbusMessageLoop(): - DBusServiceObject-branch, sending 'SERVICE.REPLYSIGNATURE' to service object [%s] failed, now using 's' as safe fallback\n", context->ObjectToStringValue(rop));
#endif
                                context->ClearCondition();
                            }

                            else if (rso!=context->Nil() && rso!=NULL)
                            {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a CALL, 3a - DBusServiceObject-branch, rso=[%p]=[%s], CheckCondition=[%d] - message [%s] (interfaceName=[%s]) to service object for [%s], no condition, a return value to be processed! :( \n",
                                     rso,
                                     (rso==NULL ? NULL : context->ObjectToStringValue(rso)),
                                     (int) context->CheckCondition(),
                                     member, interfaceName, objectPath); fflush(stderr);
#endif

                                // get and check signature
                                CSTRING tmpSignature=context->ObjectToStringValue(rso);

                                DBusError       err;
                                dbus_error_init(&err);      // initialise the error value

                                logical_t sigOK=dbus_signature_validate(tmpSignature, &err);

                                if (dbus_error_is_set(&err) || !sigOK)  // !sigOK implies setting error according to implementation as of 2011-06-21
                                {
                                   char msg[1024]="";     // buffer for error message
                                   SNPRINTF( msg, 1024, "%.16s/DbusMessageLoop, panic! Rexx service object [%s] supplied a faulty reply signature=[%s]: error.name=[%s], error.message=[%s]",
                                                         DLLNAME,
                                                         context->ObjectToStringValue(rop),
                                                         signature, err.name, err.message);

                                   dbus_message_unref(dbusMsg);    // make sure we free the message in hand
                                }
                                dbus_error_free(&err);      // not needed anymore

                                SNPRINTF(signature, 256, "%s", tmpSignature);     // accept the signature
                            }

                              // determine hwo rxResult is to be sent for marshalling
                            size_t nrArgs=GET_NUMBER_OF_REXX_ARGS_FOR_SIGNATURE(signature);

                            if (context->IsArray(rxResult))
                            {
                                context->ReleaseLocalReference(arrRxResult);
                                arrRxResult=(RexxArrayObject) rxResult;

                                    // special case: only one argument to marshal, but array with multiple items supplied: box array as one arg
                                    //               (only if single argument is an array or a struct)
                                if (nrArgs==1 &&
                                    (*signature=='a' || *signature=='(') &&
                                    context->ArrayItems(arrRxResult)>1)
                                {
                                    context->ReleaseLocalReference(arrRxResult);
                                    arrRxResult=context->ArrayOfOne(rxResult);
                                }
                            }
                            else
                            {
                                context->ReleaseLocalReference(arrRxResult);
                                arrRxResult=context->ArrayOfOne(rxResult);
                            }

                            context->ReleaseLocalReference(rso);
                        }

#ifdef DEBUG_MESSAGELOOP
else
{
   fprintf(stderr, "    ... message is a CALL, 3b - rop=[%p]=[%s] is not of type 'DBUSSERVICEOBJECT' or no signature=[%s] ! \n",
        rop,
        (rop==NULL ? NULL : context->ObjectToStringValue(rop)),
        signature
        );
}
#endif

                        if (!*signature)    // signature not yet set, hence encode all elements as strings
                        {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a CALL, message [%s] (interfaceName=[%s]) to service object for [%s], no explicit return signature given ... \n", member, interfaceName, objectPath); fflush(stderr);
#endif
                            if (dimension>0 && dimension<254)       // an array in hand, return it as a*s
                            {
                                char *localSignature=(char *) RexxAllocateMemory( dimension+2 );
                                memset(localSignature, 0, dimension+2);

                                for (size_t i=0; i<dimension; i++)
                                {
                                    *(localSignature+i)='a';
                                }
                                *(localSignature+dimension)='s';

                                SNPRINTF(signature, 256, "%s", localSignature);
                                RexxFreeMemory(localSignature);

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... ... determined signature as=[%s] (return array) \n", signature); fflush(stderr);
#endif
                            }
                            else if (context->IsDirectory(rxResult))    // marshal as a dict
                            {
                                SNPRINTF(signature, 256, "%s", "a{ss}");
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... ... determined signature as=[%s] (return dict) \n", signature); fflush(stderr);
#endif
                            }

                            else    // return result marshalled with its string value
                            {
                                SNPRINTF(signature, 256, "%s", "s");
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... ... determined signature as=[%s] (return as plain string, whatever it is) \n", signature); fflush(stderr);
#endif
                            }

                            // wrap it up
                            context->ReleaseLocalReference(arrRxResult);
                            arrRxResult=context->ArrayOfOne(rxResult);
                        }

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a CALL, message [%s] (interfaceName=[%s]) to service object for [%s], MARSHALLING return value with signature [%s] ...\n", member, interfaceName, objectPath, signature); fflush(stderr);
#endif
                                // now marshal return value in rxResult with the given signature
                                // let the helper function do the work (and raise an error, if necessary)
                        helperMarshalMessageArgs( returnMsg,      // DBUS message
                                                     &iter,  // DBUS args pointer
                                                      signature,    // argSignature for arguments, if any

                                                   // neededRexxArgs, // number of Rexx arguments to process, matching argSignature
                                                      GET_NUMBER_OF_REXX_ARGS_FOR_SIGNATURE((char *) signature),

                                                        // if result was not an array, turn it into an array
                                                      // (context->IsArray(rxResult) ? (RexxArrayObject) rxResult : context->ArrayOfOne(rxResult) ),     // rexx arguments

                                                      arrRxResult, // return value to be marshalled

                                                      context->threadContext,   // allow callee to raise condition
                                                      "%.16s/method/DbusMessageLoop(), error 987: %s"    // base condition message
                                                      );

                        if (context->CheckCondition())   // was a Rexx condition raised while marshalling?
                        {
                            bMarshallingError=true;     // send error message to client, then raise error here
                            context->ClearCondition();
                        }

                            // send the return message && flush the connection
                        if (bRexxCondition==false && bMarshallingError==false)
                        {
                            if (!dbus_connection_send(conn, returnMsg, &serial))
                            {
                               fprintf(stderr, "%s: out of memory while sending DBUS return message!\n", DLLNAME);
                            }
                            dbus_connection_flush(conn);
                            dbus_message_unref(returnMsg);
                        }
                        else   // a processing error while running Rexx or marshalling has occurred !
                        {
                            // if bRexxCondition is set, then the RexxCondition() is not cleared and will cause the respective condition in Rexx to be raised upon return

                            if(bMarshallingError) // ==true)   // create error message to inform caller that something went wrong in marshalling the return value !
                            {
                                char errMsg[512]="";
                                SNPRINTF(errMsg, 512, "Rexx service object [%s] supplied a return value that could not be marshalled with signature [%s]",
                                                      objectPath, signature);

                                DBusMessage *errReply=dbus_message_new_error(dbusMsg,
                                                            "org.freedesktop.DBus.Error.MarshallingReturnValue",
                                                            errMsg
                                                           );

                                    // send the return message && flush the connection
                                if (!dbus_connection_send(conn, errReply, &serial))
                                {
                                   fprintf(stderr, "%s: out of memory while sending DBUS return message!\n", DLLNAME);
                                }
                                dbus_connection_flush(conn);

                                if (bCollectStatistics)     // determined at top of message loop, once it's clear we need to handle the message
                                {
                                    context->SetGuardOn();          // update statistics on send signal
                                    RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");
                                    RexxDirectoryObject slotDir=context->NewDirectory();
                                    helperCreateSlotDirEntries(context->threadContext, slotDir, errReply, self);   // setup slotDir
                                    CSTRING errName=dbus_message_get_error_name(errReply);
                                    if (errName!=NULL)
                                    {
                                        RexxObjectPtr ropTmp=context->String(errName);
                                        context->DirectoryPut(slotDir,ropTmp,"ERRORNAME");
                                        context->ReleaseLocalReference(ropTmp);
                                    }

                                    CSTRING sentKind ="SENT";
                                    CSTRING kind     ="LASTSENTERROR";
                                    CSTRING countKind="COUNTSENTERRORS";
                                    context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available

                                    RexxObjectPtr ropTmp=context->DirectoryAt(slotDir,"DATETIME");
                                    context->DirectoryPut(rdo,ropTmp,sentKind);   // adjust "RECEIVED" datetime-stamp
                                    context->ReleaseLocalReference(ropTmp);

                                        // increase kind's count
                                    RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                                    RexxObjectPtr rop1=context->Int32ToObject(1);
                                    count=context->SendMessage1(count, "+", rop1);
                                    context->DirectoryPut(rdo,count,countKind);
                                    context->SetGuardOff();

                                    context->ReleaseLocalReference(rop1);
                                    context->ReleaseLocalReference(count);
                                    context->ReleaseLocalReference(slotDir);
                                    context->ReleaseLocalReference(rdo);
                                }

                                dbus_message_unref(errReply);

                                // -----------------------------------------
                                // now raise condition in Rexx
                                dbus_message_unref(dbusMsg);    // make sure we free the message in hand
                            }
                        }
                        context->ReleaseLocalReference(arrRxResult);
                        context->ReleaseLocalReference(rxResult);

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a CALL, message [%s] (interfaceName=[%s]) to service object for [%s]: ***SUCCESS*** (RETURN MESSAGE GOT SUCCESSFULLY SENT)!\n", member, interfaceName, objectPath); fflush(stderr);
#endif
                    }
                }
                context->ReleaseLocalReference(rxResult);
            }
            context->ReleaseLocalReference(rop);
        }

        dbus_message_unref(dbusMsg);    // free the message in hand
        context->SendMessage0(self,"EXECUTEQUEUEDMESSAGES");    // if any messages for DBus queued, execute them!
    }

    // o.k. here to indicate that messageloop is inactive, no communication possible anymore
    context->SetObjectVariable("active", rxFalse); // context->False()); // indicate message processing has stopped

    context->SendMessage0(self,"ABORTQUEUEDMESSAGES");  // if any messages for DBus queued, abort them!


    // TODO: drain messages in incoming queue !? Question was not answered on DBus mailing list, hence not doing it
    //     20140722, rgf, cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusConnection.html#ga580d8766c23fe5f49418bc7d87b67dc6>
    //                    so we are supposed to read the message queue empty ?
/* maybe like this? though tests show it is not working reliably on DBus 1.4.18 on 64-bit Ubuntu
fprintf(stderr, "TICKER TICKER TICKER # 1\n");fflush(stderr);
    DBusError       err;
    dbus_error_init(&err);      // initialise the error value
    for (dbusMsg=dbus_connection_pop_message(conn);dbusMsg!=NULL;dbus_connection_pop_message(conn))
    {
        if (dbus_error_is_set(&err))  // !sigOK implies setting error according to implementation as of 2011-06-21
        {
fprintf(stderr, "\tTICKER TICKER TICKER # // - DBUS_ERROR: name=[%s], message=[%s], breaking...\n", err.name, err.message);fflush(stderr);
            break;
        }
fprintf(stderr, "\tTICKER TICKER TICKER # 2a - about to dbus_message_unref, dbusMsg=[%p]\n", dbusMsg);fflush(stderr);

        dbus_message_unref(dbusMsg);    // free the message in hand

        if (dbus_error_is_set(&err))  // !sigOK implies setting error according to implementation as of 2011-06-21
        {
fprintf(stderr, "\tTICKER TICKER TICKER # \\\\ - DBUS_ERROR: name=[%s], message=[%s], breaking...\n", err.name, err.message);fflush(stderr);
            break;
        }
fprintf(stderr, "\tTICKER TICKER TICKER # 2b - AFTER to dbus_message_unref\n");fflush(stderr);
    }
    dbus_error_free(&err);      // not needed anymore
fprintf(stderr, "TICKER TICKER TICKER # 3\n");fflush(stderr);

 */


#if defined (DEBUG_MESSAGELOOP) || defined (DEBUG_CONNECTION)
    fprintf(stderr, "... MessageLoop():  left message loop, changed instance variable 'active' to .false ...\n"); fflush(stderr);
#endif

    return NULLOBJECT;
}



// -----------------------------------------------------------------------------------------------------
    // ============================================================================= //
    // send a signal message over the bus (a broadcast message)
/** Send a signal (one-way broadcast) message over the bus.
*
* @param senderOjbectPath the object path of the sender DBus object
* @param signalInterfaceName the interface name for the signal
* @param signalName the signal's name
* @param signature the signature for the signal's arguments
* @param rexxArgs a Rexx array object containing the singal's arguments
*/
RexxMethod7(RexxObjectPtr, DbusBusSignalMessage, CSTRING,         senderObjectPath,
                                                 CSTRING,         signalInterfaceName,
                                                 CSTRING,         signalName,
                                                 CSTRING,         signature,
                                                 RexxArrayObject, rexxArgs,
                                                 CSELF,           connPtr,
                                                 OSELF,           self)
{
    // check Rexx arguments first
    size_t items = (rexxArgs==NULLOBJECT ? 0 : context->ArraySize(rexxArgs)) ;
    size_t sigLen=strlen(signature);

#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusBusSignalMessage: rao=[%p], items=[%lu], signature=[%s]\n", rexxArgs, (unsigned long) items, signature);fflush(stderr);
#endif

    if (items>255)      // at most 256 arguments, because signature string can't be any longer
    {
        char msg[1024]="";     // buffer for error message

#ifdef _MSC_VER     // "%zu" creates runtime errors with "Microsoft (R) 32-bit C/C++ Optimizing Compiler Version 15.00.30729.01 for 80x86"
        SNPRINTF( msg, 1024, "%.16s/method/DbusBusSignalMessage(), error 1: too many Rexx message arguments supplied (%lu), exceeds maximum possible signature size of 255 bytes)",
                             DLLNAME, (unsigned long) items);
#else
        SNPRINTF( msg, 1024, "%.16s/method/DbusBusSignalMessage(), error 1: too many Rexx message arguments supplied (%zu), exceeds maximum possible signature size of 255 bytes)",
                             DLLNAME, items);
#endif

         // raise exception
        context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
        return NULLOBJECT;
    }

    if (sigLen==0 && items>0)   // if arguments given, then signature must be given
    {
        char msg[1024]="";      // buffer for error message
        SNPRINTF( msg, 1024, "%.16s/method/DbusBusSignalMessage(), error 2: Rexx message arguments supplied, but no 'signature' string given",
                             DLLNAME);
         // raise exception
        context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
        return NULLOBJECT;
    }

    // else: if no Rexx args, then assume they are all .nil

        // validate signature
    DBusError       err;
    dbus_error_init(&err);      // initialise the error value

    logical_t sigOK=dbus_signature_validate(signature, &err);

    if (dbus_error_is_set(&err) || !sigOK)  // !sigOK implies setting error according to implementation as of 2011-06-21
    {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusBusSignalMessage(), error 3: signature=[%s], error.name=[%s], error.message=[%s]",
                             DLLNAME, signature, err.name, err.message);
       dbus_error_free(&err);
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;
    }
    dbus_error_free(&err);      // not needed anymore


        // do we have enough Rexx arguments ?
    size_t neededRexxArgs=GET_NUMBER_OF_REXX_ARGS_FOR_SIGNATURE((char *) signature);
    if (neededRexxArgs<items)
    {
        char msg[1024]="";     // buffer for error message
#ifdef _MSC_VER     // "%zu" creates runtime errors with "Microsoft (R) 32-bit C/C++ Optimizing Compiler Version 15.00.30729.01 for 80x86"
        SNPRINTF( msg, 1024, "%.16s/method/DbusBusSignalMessage(), error 4: signature [%s] mandates at most [%lu] Rexx arguments, however Rexx supplies too many arguments [%lu]",
                             DLLNAME, signature, (unsigned long) neededRexxArgs, (unsigned long) items);
#else
        SNPRINTF( msg, 1024, "%.16s/method/DbusBusSignalMessage(), error 4: signature [%s] mandates at most [%zu] Rexx arguments, however Rexx supplies too many arguments [%zu]",
                             DLLNAME, signature, neededRexxArgs, items);
#endif

         // raise exception
        context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
        return NULLOBJECT;
    }

    //  hint: we have safe .nil-defaults, hence we allow fewer Rexx arguments than needed

    // now the code for sending the signal message
    DBusMessage     *dbusMsg;
    DBusMessageIter  dbusMsgArgs;
    DBusConnection*  conn=(DBusConnection *) connPtr;
    dbus_uint32_t    serial = 0;


    // create a signal & check for errors
    dbusMsg = dbus_message_new_signal(senderObjectPath,     // object name of the signal
                                      signalInterfaceName,  // interfaceName name of the signal
                                      signalName );         // name of the signal

#if defined (DEBUG_METHODS)
    fprintf(stderr, "... DBusSignalMessage(): sending DbusBusSignalMessage(): dbusMsg=[%p] - [%s], [%s], [%s] ... \n",
                    dbusMsg, senderObjectPath, signalInterfaceName, signalName);
#endif

    if (NULL == dbusMsg)
    {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusBusSignalMessage(), error 5: cannot create signal message, 'dbus_message_new_signal()' returned NULL", DLLNAME);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;           // return nothing
    }

    // append arguments onto signal
    dbus_message_iter_init_append(dbusMsg, &dbusMsgArgs);

        // let the helper function do the work (and raise an error, if necessary)
    helperMarshalMessageArgs(  dbusMsg,      // DBUS message
                             &dbusMsgArgs,  // DBUS args pointer
                              signature,    // signature for arguments, if any
                              neededRexxArgs, // number of Rexx arguments to process, matching signature
                              rexxArgs,     // rexx arguments
                              context->threadContext,   // allow callee to raise condition
                              "%.16s/method/DbusBusSignalMessage(), error 6: %s"    // base condition message
                              );

    if (context->CheckCondition())   // was a Rexx condition raised? if so return
    {
        dbus_message_unref(dbusMsg);    // free the message resources
        return NULLOBJECT;              // return nothing
    }
    // ----------------------

       // send the message and flush the connection
    if (!dbus_connection_send(conn, dbusMsg, &serial)) {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusBusSignalMessage(), error 7: could not send signal message, 'dbus_connection_send()()' returned NULL", DLLNAME);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       dbus_message_unref(dbusMsg); // free the message resources
       return NULLOBJECT;           // return nothing
    }
    dbus_connection_flush(conn);

    context->SetGuardOn();          // update statistics on send signal
    if (context->GetObjectVariable("COLLECTSTATISTICS")==context->True())
    {
        RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");
        RexxDirectoryObject slotDir=context->NewDirectory();
        helperCreateSlotDirEntries(context->threadContext, slotDir, dbusMsg, self);   // setup slotDir

        CSTRING sentKind ="SENT";
        CSTRING kind     ="LASTSENTSIGNAL";
        CSTRING countKind="COUNTSENTSIGNALS";
        context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available
        context->DirectoryPut(rdo,context->DirectoryAt(slotDir,"DATETIME"),sentKind);   // adjust "RECEIVED" datetime-stamp
            // increase kind's count
        RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
        count=context->SendMessage1(count, "+", context->Int32ToObject(1));
        context->DirectoryPut(rdo,count,countKind);
    }
    context->SetGuardOff();

    dbus_message_unref(dbusMsg);    // free the message resources

    return NULLOBJECT; // or NULL: indicates no return value!
}


// -----------------------------------------------------------------------------------------------------
    // ============================================================================= //
// call a method
//      use strict arg nixi, busName, receiverObjectPath, interfaceName="", methodName, signature="", ...
/** Sending a DBus message.
*
* @param busName the bus name to use for the message
* @param targetObjectPath the target object path to send the message to
* @param callInterfaceName the interface name for the message
* @param callMemberName the member name in the interface (i.e. the message name)
* @param replySignature the replySignature for this message
* @param argSignature the signature for the arguments that get sent with this message
* @param rexxArgs the Rexx array carrying the arguments for the message
* @return a Rexx object representing the result of the message, if any
*/
RexxMethod9(RexxObjectPtr, DbusBusCallMessage, CSTRING,         busName,
                                               CSTRING,         targetObjectPath,
                                               CSTRING,         callInterfaceName,
                                               CSTRING,         callMemberName,
                                               CSTRING,         replySignature,     // if empty/NULL, do not wait on reply (ignore it)
                                               CSTRING,         argSignature,
                                               RexxArrayObject, rexxArgs,
                                               CSELF,           connPtr,
                                               OSELF,           self)

{
    // check Rexx arguments first
    size_t items = (rexxArgs==NULLOBJECT ? 0 : context->ArraySize(rexxArgs)) ;
    size_t argSignatureLength=strlen(argSignature);

#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusBusCallMessage: rao=[%p], items=[%lu], argSignature=[%s] | replySignature=[%s]\n", rexxArgs, (unsigned long) items, argSignature, replySignature);
   fprintf(stderr, "===>                                ... busName=[%s], targetObjectPath=[%s], callInterfaceName=[%s], callMemberName=[%s]\n",
                         busName, targetObjectPath, callInterfaceName, callMemberName);

   fflush(stderr);
#endif

    if (items>255)      // at most 256 arguments, because signature string can't be any longer
    {
        char msg[1024]="";     // buffer for error message
#ifdef _MSC_VER     // "%zu" creates runtime errors with "Microsoft (R) 32-bit C/C++ Optimizing Compiler Version 15.00.30729.01 for 80x86"
        SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 1: too many Rexx message arguments supplied (%lu), exceeds maximum possible argSignature size of 255 bytes)",
                             DLLNAME, (unsigned long) items);
#else
        SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 1: too many Rexx message arguments supplied (%zu), exceeds maximum possible argSignature size of 255 bytes)",
                             DLLNAME, items);
#endif

         // raise exception
        context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
        return NULLOBJECT;
    }

    if (argSignatureLength==0 && items>0)   // if arguments given, then signature must be given
    {
        char msg[1024]="";      // buffer for error message
        SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 2: Rexx message arguments supplied, but no 'argSignature' string given",
                             DLLNAME);
         // raise exception
        context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
        return NULLOBJECT;
    }

    RexxObjectPtr      rxResult=NULLOBJECT;     // Rexx variable to return
    RexxDirectoryObject slotDir=NULL;           // slotDir-directory

        // validate signature
    DBusError       err;
    dbus_error_init(&err);      // initialise the error value

    logical_t sigOK=false;

    // we allow to supply a return signature and use it for unmarshalling

    if (replySignature!=NULL)   // a reply signature given, check it
    {
        sigOK=dbus_signature_validate(replySignature, &err);

        if (dbus_error_is_set(&err) || !sigOK)  // !sigOK implies setting error according to implementation as of 2011-06-21
        {
           char msg[1024]="";     // buffer for error message
           SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 3: replySignature=[%s], error.name=[%s], error.message=[%s]",
                                 DLLNAME, replySignature, err.name, err.message);
           dbus_error_free(&err);
           context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
           return NULLOBJECT;
        }
    }

    sigOK=dbus_signature_validate(argSignature, &err);
    if (dbus_error_is_set(&err) || !sigOK)  // !sigOK implies setting error according to implementation as of 2011-06-21
    {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 4: argSignature=[%s], error.name=[%s], error.message=[%s]",
                             DLLNAME, argSignature, err.name, err.message);
       dbus_error_free(&err);
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;
    }
    dbus_error_free(&err);      // not needed anymore


        // do we have enough Rexx arguments ?
    size_t neededRexxArgs=GET_NUMBER_OF_REXX_ARGS_FOR_SIGNATURE((char *) argSignature);
    if (neededRexxArgs<items)
    {
        char msg[1024]="";     // buffer for error message

#ifdef _MSC_VER     // "%zu" creates runtime errors with "Microsoft (R) 32-bit C/C++ Optimizing Compiler Version 15.00.30729.01 for 80x86"
        SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 5: argSignature [%s] mandates at most [%lu] Rexx arguments, however Rexx supplies too many arguments [%lu]",
                             DLLNAME, argSignature, (unsigned long) neededRexxArgs, (unsigned long) items);
#else
        SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 5: argSignature [%s] mandates at most [%zu] Rexx arguments, however Rexx supplies too many arguments [%zu]",
                             DLLNAME, argSignature, neededRexxArgs, items);
#endif
         // raise exception
        context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
        return NULLOBJECT;
    }

    //  hint: we have save NULL/.nil-defaults, hence we allow fewer Rexx arguments than needed
    RexxObjectPtr rxTrue   =context->True();
    bool bMakeSlotDir      =context->GetObjectVariable("MAKEREPLYSLOTDIR")==rxTrue;
    bool bCollectStatistics=context->GetObjectVariable("COLLECTSTATISTICS")==rxTrue;

    // now the code for sending the call message
    DBusMessage     *dbusMsg;
    DBusMessageIter  dbusMsgArgs;
    DBusConnection*  conn=(DBusConnection *) connPtr;
    dbus_uint32_t    serial = 0;

    // create a call message & check for errors
    dbusMsg= dbus_message_new_method_call(  busName,   // busName to address (destination)
                                            targetObjectPath,   // object name of the call
                                            (!*callInterfaceName ? NULL : callInterfaceName),  // interfaceName name of the call
                                            callMemberName );   // name of the call

    if (NULL == dbusMsg)
    {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 6: cannot create call message 'dbus_message_new_method_call()' returned NULL; check spelling of bus name=[%s], object path=[%s], interfaceName=[%s], method=[%s]",
                             DLLNAME, busName, targetObjectPath, callInterfaceName, callMemberName);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;           // return nothing
    }

    // append arguments onto call
    dbus_message_iter_init_append(dbusMsg, &dbusMsgArgs);

        // let the helper function do the work (and raise an error, if necessary)
    helperMarshalMessageArgs(  dbusMsg,      // DBUS message
                             &dbusMsgArgs,  // DBUS args pointer
                              argSignature,    // argSignature for arguments, if any
                              neededRexxArgs, // number of Rexx arguments to process, matching argSignature
                              rexxArgs,     // rexx arguments
                              context->threadContext,   // allow callee to raise condition
                              "%.16s/method/DbusBusCallMessage(), error 7: %s"    // base condition message
                              );

    if (context->CheckCondition())   // was a Rexx condition raised? if so return
    {
        dbus_message_unref(dbusMsg);    // free the message resources
        return NULLOBJECT;              // return nothing
    }
    // ----------------------


    if (replySignature==NULL || !*replySignature)   // no reply expected
    {
#if defined (DEBUG_METHODS)
   fprintf(stderr, "         ... in DbusBusCallMessage: branch NO REPLY expected (replySignature=[%s])\n", replySignature);
   fflush(stderr);
#endif

        dbus_message_set_no_reply(dbusMsg, true);   // indicate, we do not expect a reply ("one-way" message)
           // send the message and flush the connection
        if (!dbus_connection_send(conn, dbusMsg, &serial))
        {
           char msg[1024]="";     // buffer for error message
           SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 8: could not send call message, 'dbus_connection_send()()' returned NULL", DLLNAME);

            // raise exception
           context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
           dbus_message_unref(dbusMsg); // free the message resources
           return NULLOBJECT;           // return nothing
        }


        if (bCollectStatistics)         // allows us to guard on only, if statistics should be collected !
        {
            context->SetGuardOn();          // update statistics on call method
            RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");
            RexxDirectoryObject slotDir=context->NewDirectory();
            helperCreateSlotDirEntries(context->threadContext, slotDir, dbusMsg, self);   // setup slotDir

            CSTRING sentKind ="SENT";
            CSTRING kind     ="LASTSENTMESSAGE";
            CSTRING countKind="COUNTSENTMESSAGES";
            context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available
            context->DirectoryPut(rdo,context->DirectoryAt(slotDir,"DATETIME"),sentKind);   // adjust "RECEIVED" datetime-stamp
                // increase kind's count
            RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
            count=context->SendMessage1(count, "+", context->Int32ToObject(1));
            context->DirectoryPut(rdo,count,countKind);
            context->SetGuardOff();
        }
    }
    else    // send with reply
    {

#if defined (DEBUG_METHODS)
   fprintf(stderr, "         ... in DbusBusCallMessage: branch WITH reply (replySignature=[%s])\n", replySignature);
   fflush(stderr);
#endif

        DBusPendingCall *pendingMsg=NULL;
            // send message and get a handle for a reply
        if (!dbus_connection_send_with_reply (conn, dbusMsg, &pendingMsg, -1))
        {
            char msg[1024]="";     // buffer for error message
            SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 9: could not send call message, 'dbus_connection_send_with_reply()' returned NULL", DLLNAME);

            dbus_message_unref(dbusMsg); // free the message resources

             // raise exception
            context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
            return NULLOBJECT;           // return nothing
        }

        if (pendingMsg==NULL)
        {
            char msg[1024]="";     // buffer for error message
            SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 10: could not receive a valid pending message object from 'dbus_connection_send_with_reply()' (maybe you attempted to send Unix file descriptors and this connection does not support them?)", DLLNAME);
            // raise exception
            context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));

            dbus_message_unref(dbusMsg);     // we do not need this message resource anymore
            return NULLOBJECT;           // return nothing
        }
        dbus_connection_flush(conn);

        if (bCollectStatistics)         // allows us to guard on only, if statistics should be collected !
        {
            context->SetGuardOn();          // update statistics on call method
            RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");
            RexxDirectoryObject slotDir=context->NewDirectory();
            helperCreateSlotDirEntries(context->threadContext, slotDir, dbusMsg, self);   // setup slotDir

            CSTRING sentKind ="SENT";
            CSTRING kind     ="LASTSENTMESSAGE";
            CSTRING countKind="COUNTSENTMESSAGES";
            context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available
            context->DirectoryPut(rdo,context->DirectoryAt(slotDir,"DATETIME"),sentKind);   // adjust "RECEIVED" datetime-stamp
                // increase kind's count
            RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
            count=context->SendMessage1(count, "+", context->Int32ToObject(1));
            context->DirectoryPut(rdo,count,countKind);
            context->SetGuardOff();
        }

        dbus_pending_call_block(pendingMsg);   // wait for reply

        DBusMessage *replyMsg=NULL;
        replyMsg = dbus_pending_call_steal_reply(pendingMsg);   // get the reply msg

        if (replyMsg==NULL)
        {
            dbus_pending_call_unref(pendingMsg);
            dbus_message_unref(dbusMsg);     // we do not need this message resource anymore

            char msg[1024]="";     // buffer for error message
            SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 11: could not receive a valid reply message object from 'dbus_connection_send_with_reply()'", DLLNAME);
            // raise exception
            context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
            return NULLOBJECT;           // return nothing
        }

#if defined (DEBUG_METHODS)
    fprintf(stderr, "         ... in DbusBusCallMessage, replyMsg: type=[%d], typename=[%s] \n",
                            dbus_message_get_type(replyMsg),
                            dbus_message_type_to_string(dbus_message_get_type(replyMsg)) );
    fflush(stderr);
#endif

        rxResult=helperUnmarshalMessageArgs(replyMsg,
                                         context->threadContext,
                                         (context->GetObjectVariable("UNMARSHALBYTEARRAYASSTRING")==context->True()),
                                         "%.16s/method/DbusBusCallMessage(), error 99: %s"    // base condition message
                                         );

        // if only one element, then extract and return it
        if (rxResult!=NULL &&
            context->ArrayDimension((RexxArrayObject)rxResult)==1 &&
            context->ArraySize     ((RexxArrayObject)rxResult)==1 )
        {
            rxResult=context->ArrayAt((RexxArrayObject) rxResult,1);
        }


        int msgType=dbus_message_get_type(replyMsg);    // determine message type: reply or error
            // create the slotDir argument, supply the message's interesting information with it
        if (msgType==DBUS_MESSAGE_TYPE_ERROR         // if an error message, create a slot Dir to pass on
            || bMakeSlotDir
            || bCollectStatistics
            ) // should we create a slotDir-argument containing message related information?
        {
            slotDir=context->NewDirectory();
            helperCreateSlotDirEntries(context->threadContext, slotDir, replyMsg, self);  // adds some information about the message
        }

        if (msgType==DBUS_MESSAGE_TYPE_ERROR) // reply was an error message, raise condition in caller and inform him !
        {
            char msg[1024]="";     // buffer for error message
            logical_t isNoReturnValueError=dbus_message_is_error(replyMsg, REXX_ERROR_MESSAGE_NO_RETURN_VALUE);
            if (!isNoReturnValueError)
            {
                SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 12: error.name=[%s], error.message=[%s]",
                                    DLLNAME,
                                    dbus_message_get_error_name(replyMsg),
                                    (rxResult==NULL ? "" : context->ObjectToStringValue(rxResult))
                                    );
            }
            else    // just the error name, no error message
            {
                SNPRINTF( msg, 1024, "%.16s/method/DbusBusCallMessage(), error 13: error.name=[%s] (error arguments, if any, can be fetched via 'errorargs=condition(\"o\")~additional[2]' in a Rexx condition handler)",
                                    DLLNAME,
                                    dbus_message_get_error_name(replyMsg)
                                    );
            }

            dbus_message_unref(replyMsg);        // free the message resources
            dbus_pending_call_unref(pendingMsg); // free the message resources
            dbus_message_unref(dbusMsg);         // free the message resources

            // raise exception
            RexxArrayObject ra=context->NewArray(2);        // create array to supply "additional" array
            size_t idx=1;

            context->ArrayPut(ra, context->String(msg), idx++);   // save error string

            if (!isNoReturnValueError && rxResult!=NULL)    // if a resutl was supplied
            {
                context->ArrayPut(ra, rxResult, idx++);     // error result arguments: errRes=condition('0')~additional~at(2)
            }

            if (slotDir!=NULL)                  // was a slotDir created, add it to the additional array
            {
                context->ArrayPut(ra, slotDir, idx++);      // error result arguments: errRes=condition('0')~additional~at(2)
            }

            if (bCollectStatistics)         // allows us to guard on only, if statistics should be collected !
            {
                context->SetGuardOn();
                RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");
                context->DirectoryPut(rdo,slotDir,"LASTRECEIVEDERROR");     // make error messages slotDir available
                context->DirectoryPut(rdo,context->DirectoryAt(slotDir,"DATETIME"),"RECEIVED");   // adjust "RECEIVED" datetime-stamp

                CSTRING errName=dbus_message_get_error_name(replyMsg);
                if (errName!=NULL)          // add err.name to slotDir
                {
                    context->DirectoryPut(slotDir,context->String(errName),"ERRORNAME");
                }

                    // increase error count
                CSTRING receivedKind ="RECEIVED";
                CSTRING kind     ="LASTRECEIVEDERROR";
                CSTRING countKind="COUNTRECEIVEDERRORS";
                context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available
                context->DirectoryPut(rdo,context->DirectoryAt(slotDir,"DATETIME"),receivedKind);   // adjust "RECEIVED" datetime-stamp

                    // increase kind's count
                RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                count=context->SendMessage1(count, "+", context->Int32ToObject(1));
                context->DirectoryPut(rdo,count,countKind);
                context->SetGuardOff();
            }

            context->RaiseException(Rexx_Error_Incorrect_method_user_defined, ra);

            return NULLOBJECT;
        }

        // free reply and close connection
        dbus_message_unref(replyMsg);
        dbus_pending_call_unref(pendingMsg); // free the message resources
    }

    dbus_connection_flush(conn);    // probably not needed, but we leave it, as it does no harm, but assures flushing
    dbus_message_unref(dbusMsg);    // free the message resources

    if (bMakeSlotDir)
    {
        RexxArrayObject rao=context->NewArray(2);
        context->ArrayPut(rao, rxResult, 1);
        context->ArrayPut(rao, slotDir,  2);
        return rao;
    }

    return rxResult;                // or NULL: indicates no return value!
}





    // ============================================================================= //
// --------------------------- server & connection related ---------------------------
// -----------------------------------------------------------------------------------
        // open a private connection
/** Oben a private connection to the supplied address.
*
* @param address the address to connect to
*/
RexxMethod1(RexxObjectPtr, DbusNativeConnectionOpenPrivate, CSTRING, address)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusConnectionOpenPrivate: address=[%p] \n", address);
   fflush(stderr);
#endif

   char msg[1024]="";       // buffer for error message
   DBusError       err;
   dbus_error_init(&err);   // initialise the error value

   DBusConnection *conn=dbus_connection_open_private(address, &err);

   if (conn==NULL)          // oops, something went wrong !
   {
      SNPRINTF( msg, 1024, "%.16s/method/DbusNativeConnectionOpenPrivate(), connection error 1: error.name=[%s], error.message=[%s]",
                            DLLNAME, err.name, err.message);
      dbus_error_free(&err);

       // raise exception
      context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
      return NULLOBJECT;
   }
   dbus_error_free(&err);  // not needed anymore

        // save conn in a Rexx pointer object and save it in attribute CSELF
   RexxPointerObject rpo = context->NewPointer(conn);
   context->SetObjectVariable("CSELF", rpo); // set object's CSELF attribute

   return NULLOBJECT;          // return nothing
}


// -----------------------------------------------------------------------------------
        // close a private connection
/** Close a connection.
*/
RexxMethod1(RexxObjectPtr, DbusNativeConnectionClose, CSELF, connPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusNativeConnectionClose: connPtr=[%p], .nil=[%p] \n", connPtr, context->Nil());
   fflush(stderr);
#endif
        // get self and carry out the
    DBusConnection *conn=(DBusConnection *) connPtr;

    dbus_connection_close(conn);
    dbus_connection_unref(conn);

    // not a valid connection anymore, hence set CSELF attribute to .nil
    context->SetObjectVariable("CSELF", context->Nil());

    return NULLOBJECT;          // return nothing
}





// -------------- DBusServer and connection by address ... ---------------------------------------------
// --------------------------------------------------------------------------------
// callbacks needed for dbus_server, which gets invoked if a new connection is attempted ...

// --------------------------------------------------------------------------------
/** Callback for a (private) DBus server, that gets invoked whenever a new connection is made to the DBus server.
*
* @param server the DBusServer to which a connection is attempted
* @param conn   the connection to the client
* @param data   the data structure for the callback function to e.g. get access to a RexxThreadContext to interact with ooRexx
*/
void new_connection_callback (DBusServer *server, DBusConnection *conn, void *data) // callback on new connection
{
#if defined (DEBUG_METHODS) || defined (DEBUG_PRIVATE_SERVER) || defined (DEBUG_OOREXX_FIND_CLASS) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in \"new_connection_callback(...)\", connPtr=[%p], data=[%p} \n", conn, data);
   fflush(stderr);
#endif

    PSTRUCT_CALLBACK_DATA cbd=(PSTRUCT_CALLBACK_DATA) data;
    RexxThreadContext    *rtc=NULL;
    logical_t              rc=cbd->rxInstance->AttachThread(&rtc);  // attach to Rexx interpreter instance
    if (rc==false)
    {
        fprintf(stderr, "%.16s/\"new_connection_callback(...)\", fatal error # 1: cannot attach thread to saved Rexx interpreter instance, aborting with 'exit(-1)'\n", DLLNAME);
        fflush(stderr),
        exit(-1);
    }

        // can the connection be anonymous ?
    if (rtc->SendMessage0(cbd->rxDbusServer, "ALLOWANONYMOUS") == rtc->True())
    {
        dbus_connection_set_allow_anonymous(conn, true);
    }

    dbus_connection_ref(conn);  // let this connection be kept open

        // define pseudo-address for connection
    RexxStringObject rsoDT=GET_REXX_DATE_TIME_AS_REXX_STRING(rtc);
    RexxStringObject rsoAddr=rtc->String("native:connectionFromclient@");
    RexxStringObject rsoAddrWithDT=(RexxStringObject) rtc->SendMessage1(rsoAddr, "||", rsoDT);


#ifdef DEBUG_HIT_ENTER_TO_CONTINUE
    fprintf(stderr, "... connection_callback(...): about to do FindClass(\"DBUS\")...\n");fflush(stderr);
    RgfDebugHitEnterToContinue();
#endif
        // create an instance of DBus (a connection)
    RexxClassObject      rco=rtc->FindClass("DBUS");    // get class

#ifdef DEBUG_HIT_ENTER_TO_CONTINUE
    fprintf(stderr, "... connection_callback(...): after doing FindClass(\"DBUS\"), rco=[%p]\n",rco);fflush(stderr);
    RgfDebugHitEnterToContinue();
#endif


#if defined (DEBUG_METHODS) || defined (DEBUG_PRIVATE_SERVER) || defined (DEBUG_OOREXX_FIND_CLASS)
   fprintf(stderr, "             in \"new_connection_callback(...)\", rco=[%p] \n", rco); fflush(stderr);
#endif

    RexxStringObject rsoNative=rtc->String("native");
    RexxObjectPtr rxDBusConn=rtc->SendMessage1(rco, "NEW", rsoNative);  // get DBus (bus connection) object

    rtc->SendMessage1(rxDBusConn, "PSEUDOADDRESS=", rsoAddrWithDT); // set CSELF

        // save conn in a Rexx pointer object and save it as CSELF
    RexxPointerObject rpo = rtc->NewPointer(conn);
    rtc->SendMessage1(rxDBusConn, "CSELF=",         rpo); // set CSELF
    rtc->SendMessage1(rxDBusConn, "SERVER=", cbd->rxDbusServer);        // set server this connection belongs to

        // let Rexx code setup (save to connections, assigning default service and listener objects)
    rtc->SendMessage1(cbd->rxDbusServer, "NEWCONNECTION", rxDBusConn);

#if defined (DEBUG_RGF)
    fprintf(stderr, "===>         in \"new_connection_callback(...)\", rtc->CheckCondition()=[%d] \n",
            (bool) rtc->CheckCondition());
#endif

    rtc->ReleaseLocalReference(rsoDT);
    rtc->ReleaseLocalReference(rsoAddr);
    rtc->ReleaseLocalReference(rsoAddrWithDT);
    rtc->ReleaseLocalReference(rco);
    rtc->ReleaseLocalReference(rsoNative);
    rtc->ReleaseLocalReference(rxDBusConn);
    rtc->ReleaseLocalReference(rpo);

    rtc->DetachThread();    // detach thread from Rexx interpreter instance

#if defined (DEBUG_METHODS)  || defined (DEBUG_PRIVATE_SERVER)
   fprintf(stderr, "<--- end of new_connection_callback(...).\n"); fflush(stderr);
#endif
}


// --------------------------------------------------------------------------------
/** The add watch callback function, cf. DBus documentation.
*/
dbus_bool_t add_watch_callback (DBusWatch *watch, void *data) // callback on new connection
{
#if defined (DEBUG_METHODS)  || defined (DEBUG_PRIVATE_SERVER)
   fprintf(stderr, "===> arrived in \"add_watch_callback(...)\", data=[%p}, watch=[%p], fd=[%d] \n",
                         data, watch, dbus_watch_get_socket(watch));
   fflush(stderr);
#endif

    // create node for new watch
   PSTRUCT_WATCH psw=NULL;
#ifdef MALLOC_USE_REXX
   psw=(PSTRUCT_WATCH) RexxAllocateMemory(size_STRUCT_WATCH);
   memset(psw, 0, size_STRUCT_WATCH);
#else
   psw=(PSTRUCT_WATCH) dbus_malloc0(size_STRUCT_WATCH);
#endif
   psw->watch=watch;       // save watch in node

   PSTRUCT_WATCH_LIST wl=(PSTRUCT_WATCH_LIST) data;    // cast toe able to use the watch list

   psw->next=wl->watchList;    // let new node point to current root node
   wl->watchList=psw;          // make new node the root of the list

#if defined (DEBUG_METHODS)  || defined (DEBUG_PRIVATE_SERVER)
   fprintf(stderr, "<--- end of add_watch_callback(...).\n"); fflush(stderr);
#endif
    return true;
}


// --------------------------------------------------------------------------------
/** The remove watch callback function, cf. DBus documentation.
*/
void remove_watch_callback (DBusWatch *watch, void *data) // callback on new connection
{
#if defined (DEBUG_METHODS) || defined (DEBUG_PRIVATE_SERVER)
   fprintf(stderr, "===> arrived in \"remove_watch_callback(...)\", data=[%p}, watch=[%p], fd=[%d] \n",
                         data, watch, dbus_watch_get_socket(watch));
   fflush(stderr);
#endif

   PSTRUCT_WATCH_LIST wl=(PSTRUCT_WATCH_LIST) data;    // cast toe able to use the watch list
   PSTRUCT_WATCH pw=wl->watchList;
   PSTRUCT_WATCH tmpwatch=NULL;

   if (pw)      // root node available
   {
       if (pw->watch==watch)    // root node contains watch
       {
           tmpwatch=pw;         // remember node to delete
           wl->watchList=tmpwatch->next;   // make second node root
       }
       else                     // find watch in list
       {
           while (pw)           // as long as a node in hand
           {
               if (pw->next->watch==watch)  // next node is the one to be removed
               {
                   tmpwatch=pw->next;   // remember node to delete
                   pw->next=tmpwatch->next; // adjust node pointer
                   break;
               }
               pw=pw->next;     // get next node
           }
       }
   }

   if (tmpwatch)               // did we find the watch node in the list ?
   {
       // remove watch node
#ifdef MALLOC_USE_REXX
        RexxFreeMemory(tmpwatch);
#else
        dbus_free(tmpwatch);
#endif
   }
   else
   {
       fprintf(stderr,  "... PANIC ... remove_watch_callback(...): watch=[%p], fd=[%d] not found in list!!! \n",
                        watch, dbus_watch_get_socket(pw->watch));
   }

#if defined (DEBUG_METHODS) || defined (DEBUG_PRIVATE_SERVER)
   fprintf(stderr, "<--- end of remove_watch_callback(...).\n"); fflush(stderr);
#endif

}



// --------------------------------------------------------------------------------
/** The add timeout callback function, cf. DBus documentation.
*/
dbus_bool_t add_timeout_callback (DBusTimeout *timeout, void *data) // callback on new connection
{
#if defined (DEBUG_TIMEOUTLOOP) || defined (DEBUG_METHODS)
   fprintf(stderr, "===> arrived in \"add_timeout_callback(...)\", timeout=[%p] \n", timeout);
   fflush(stderr);
#endif

   // save timeout value for later usage ?
   int newTimeout=dbus_timeout_get_interval(timeout);
   PSTRUCT_TIMEOUT pto=(PSTRUCT_TIMEOUT) data;
   pto->currentInterval=newTimeout;
   pto->enabled=true;

   RexxThreadContext    *rtc=NULL;
   logical_t              rc=pto->rxInstance->AttachThread(&rtc);  // attach to Rexx interpreter instance
   if (rc==false)
   {
       fprintf(stderr, "%.16s/\"add_timeout_callback(...)\", fatal error # 1: cannot attach thread to saved Rexx interpreter instance, aborting with 'exit(-1)'\n", DLLNAME);
       fflush(stderr),
       exit(-2);
   }

   RexxPointerObject rpo=rtc->NewPointer(data); // wrap
       // start the timer loop via Rexx on an own thread, submit *data pointer wrapped in a Rexx ojbect
   RexxObjectPtr ropStart=rtc->String("START");
   rtc->SendMessage2(pto->rxDbusServer, "TIMERLOOP", ropStart, rpo);
   rtc->ReleaseLocalReference(rpo);
   rtc->ReleaseLocalReference(ropStart);

#if defined (DEBUG_TIMEOUTLOOP) || defined (DEBUG_METHODS)
   fprintf(stderr, "<--- end of add_timeout_callback(...), interval=[%d].\n", pto->currentInterval); fflush(stderr);
#endif

    return true;
}


// --------------------------------------------------------------------------------
/** The remove timeout callback function, cf. DBus documentation.
*/
void remove_timeout_callback (DBusTimeout *timeout, void *data) // callback on new connection
{
#if defined (DEBUG_TIMEOUTLOOP) || defined (DEBUG_METHODS)
   fprintf(stderr, "===> arrived in \"remove_timeout_callback(...)\", timeout=[%p], data=[%p} \n",
                         timeout, data);
   fflush(stderr);
#endif

   PSTRUCT_TIMEOUT pto=(PSTRUCT_TIMEOUT) data;

   RexxThreadContext    *rtc=NULL;
   logical_t              rc=pto->rxInstance->AttachThread(&rtc);  // attach to Rexx interpreter instance
   if (rc==false)
   {
       fprintf(stderr, "%.16s/\"remove_timeout_callback(...)\", fatal error # 1: cannot attach thread to saved Rexx interpreter instance, aborting with 'exit(-1)'\n", DLLNAME);
       fflush(stderr),
       exit(-2);
   }

   // save timeout value for later usage ?
   pto->timeout        =timeout;
   pto->currentInterval=dbus_timeout_get_interval(timeout);
   pto->enabled        =false;

   // stop timeout thread
   RexxObjectPtr ropStop=rtc->String("STOP");
   rtc->SendMessage1(pto->rxDbusServer, "TIMERLOOP", ropStop);
   rtc->ReleaseLocalReference(ropStop);

#if defined (DEBUG_TIMEOUTLOOP) || defined (DEBUG_METHODS)
   fprintf(stderr, "<--- end of remove_timeout_callback(...).\n"); fflush(stderr);
#endif
}



// -----------------------------------------------------------------------------------------------------
    // ============================================================================= //
    // set up and run a message loop for this connection instance
    // processes signals and method calls to registered ooRexx server objects
    // wrapped up as a Rexx method, hence easy to run it on a separate thread
RexxMethod3(RexxObjectPtr, DbusServerTimeoutLoop, RexxObjectPtr, timeoutDataPointer, CSELF, connPtr, OSELF, self)
{
#if defined (DEBUG_TIMEOUTLOOP) || defined (DEBUG_METHODS)  || defined (DEBUG_CONNECTION)
    fprintf(stderr, "... TimeoutLoop(): just arrived ! ...\n"); fflush(stderr);
#endif

    if (!context->IsPointer(timeoutDataPointer))
    {
        char msg[1024]="";
        SNPRINTF( msg, 1024, "%.16s/method/DbusServerTimeoutLoop(), error 1: callback buffer 'timeoutDataPointer' is not a RexxPointer object, cannot create timeout loop",
                              DLLNAME);

         // raise exception
        context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
        return NULLOBJECT;
    }

    RexxObjectPtr rxTrue =context->True();
    RexxObjectPtr rxFalse =context->False();
    CSTRING timeoutLoopActive="TIMEOUTLOOPACTIVE";      // as long as this variable is .true, the loop will get executed

    context->SetGuardOn();
    if (context->GetObjectVariable(timeoutLoopActive) == rxTrue)    // already running ?
    {
        context->SetGuardOff();
        return NULLOBJECT;      // return, do not start another message loop thread
    }

    context->SetObjectVariable(timeoutLoopActive, rxTrue);  // indicate timeout loop is active
    context->SetGuardOff();


    // get pointer and cast it
    PSTRUCT_TIMEOUT pto=(PSTRUCT_TIMEOUT) context->PointerValue((RexxPointerObject) timeoutDataPointer);

#if defined (DEBUG_TIMEOUTLOOP)
    fprintf(stderr, "... TimeoutLoop(): timeout_ms=[%d] timeout enabled=[%d] \n",
                         dbus_timeout_get_interval(pto->timeout),
                         dbus_timeout_get_enabled(pto->timeout) );
    size_t counter=0;
#endif

        // allow loop thread to be ended by Rexx, e.g. checking the status of the "active" attribute (if .false return from message loop)
    RexxObjectPtr ropTimeoutLoopActive=context->GetObjectVariable(timeoutLoopActive);
    while (ropTimeoutLoopActive == rxTrue) // while watch loop active, go ahead
    {
        if  (context->CheckCondition()) // is this thread affected by a Halt-condition? If so, leave
        {
            context->SetGuardOn();
            context->SetObjectVariable(timeoutLoopActive,rxFalse);
            context->SetGuardOff();
            break;
        }

#if defined (DEBUG_TIMEOUTLOOP)
    counter++;
#endif
            // always get new timeout value (in milliseconds)
        int timeout_ms=dbus_timeout_get_interval(pto->timeout);

        struct timeval tv;
        tv.tv_sec  = timeout_ms / 1000;         //  timeout_milliseconds / 1000;
        tv.tv_usec = (timeout_ms % 1000)*1000;  // (timeout_milliseconds % 1000) * 1000;

#if defined (DEBUG_TIMEOUTLOOP)
        int res=
#endif
        select(0,NULL,NULL,NULL,&tv);   // now sleep

#if defined (DEBUG_TIMEOUTLOOP)
        if (counter % 10000 == 1)
        {
            fprintf(stderr, "... TimeoutLoop(): after timeout, res=[%d], counter=[%lu], timeout_ms=[%d] timeout enabled=[%d]  \n",
                             res,
                             (unsigned long) counter,
                             dbus_timeout_get_interval(pto->timeout),
                             dbus_timeout_get_enabled(pto->timeout) );
        }
#endif

        if (dbus_timeout_get_enabled(pto->timeout))
        {
#if defined (DEBUG_TIMEOUTLOOP)
    if (counter % 1000 == 1)        // every second
    {
    fprintf(stderr, "... TimeoutLoop(): after timeout, branch 'enabled', counter=[%lu], about to call 'dbus_timeout_handle()' ... \n", (unsigned long) counter);
    }
#endif
            dbus_timeout_handle(pto->timeout);  // let DBus act ...
        }

        context->ReleaseLocalReference(ropTimeoutLoopActive);
        ropTimeoutLoopActive=context->GetObjectVariable(timeoutLoopActive);
    }
    context->ReleaseLocalReference(ropTimeoutLoopActive);


#if defined (DEBUG_TIMEOUTLOOP) || defined (DEBUG_METHODS) || defined (DEBUG_CONNECTION)
    fprintf(stderr, "... TimeoutLoop(): about to leav.\n"); fflush(stderr);
#endif

    return NULL;    // do not return anything
}







// --------------------------------------------------------------------------------
// startup a DBus server instance
/** Startup a DBus server instance, will set up watch and timeout callbacks.
*
* @param address to serve
*/
RexxMethod2(RexxObjectPtr, DbusNativeServerStartup,
                                  CSTRING,  address,
                                  OSELF,    self)
{

#if defined (DEBUG_METHODS) || defined (DEBUG_PRIVATE_SERVER)
   fprintf(stderr, "===> arrived in DbusNativeServerStartup(...) \n");
   fflush(stderr);
#endif


    char msg[1024]="";     // buffer for error message
    DBusError       err;
    dbus_error_init(&err);      // initialise the error value

    DBusServer *server=dbus_server_listen(address, &err);
    if (server==NULL)       // oops, something went wrong !
    {
       SNPRINTF( msg, 1024, "%.16s/method/DbusNativeServerStartup(), server startup error 1: error.name=[%s], error.message=[%s]",
                             DLLNAME, err.name, err.message);
       dbus_error_free(&err);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;
    }
    dbus_error_free(&err);  // not needed anymore

    dbus_server_ref(server);    // increase ref counter

        // save server's pointer
    RexxPointerObject rpo = context->NewPointer(server);
    context->SendMessage1(self, "CSELF=", rpo); // set CSELF

        // create callback data structure
    PSTRUCT_CALLBACK_DATA ccb=NULL; // structure to be passed to the new_connection function,
                                    // allowing to interact with Rexx and this server object

#ifdef MALLOC_USE_REXX
    ccb=(PSTRUCT_CALLBACK_DATA) RexxAllocateMemory(size_STRUCT_CALLBACK_DATA);   // that should be enough
    memset(ccb, 0, size_STRUCT_CALLBACK_DATA);
#else
    ccb=(PSTRUCT_CALLBACK_DATA) dbus_malloc0(size_STRUCT_CALLBACK_DATA);         // that should be enough
#endif

    ccb->rxInstance  =context->threadContext->instance;    // save Rexx interpreter instance
    ccb->rxDbusServer=self;                 // save this Rexx object's pointer

        // inform server about the new connection function
    dbus_server_set_new_connection_function(server,                     // DBus server
                                            new_connection_callback,    // callback function
                                            ccb,                        // *data
                                                // free memory function:
#ifdef MALLOC_USE_REXX
                                            (void (*)(void*)) RexxFreeMemory  // that should be enough
#else
                                            dbus_free
#endif
                                            );

    // create callback data structure
    PSTRUCT_WATCH_LIST wl=NULL;
#ifdef MALLOC_USE_REXX
    wl=(PSTRUCT_WATCH_LIST) RexxAllocateMemory(size_STRUCT_WATCH_LIST); // that should be enough
    memset(wl, 0, size_STRUCT_WATCH_LIST);
#else
    wl=(PSTRUCT_WATCH_LIST) dbus_malloc0(size_STRUCT_WATCH_LIST);       // that should be enough
#endif
    context->SetObjectVariable("WATCHLIST", context->NewPointer(wl));    // save pointer with the attribute "cbd" (callback buffer)

    dbus_server_set_watch_functions(server,
                                    add_watch_callback,
                                    remove_watch_callback,
                                    NULL,                   // no toggle-watch function
                                    wl,                     // watch list callback data structure
#ifdef MALLOC_USE_REXX
                                    (void (*)(void*)) RexxFreeMemory  // that should be enough
#else
                                    dbus_free
#endif
                                   );


    // create callback data structure
    PSTRUCT_TIMEOUT timeoutData=NULL;
#ifdef MALLOC_USE_REXX
    timeoutData=(PSTRUCT_TIMEOUT) RexxAllocateMemory(size_STRUCT_TIMEOUT); // that should be enough
    memset(timeoutData, 0, size_STRUCT_TIMEOUT);
#else
    timeoutData=(PSTRUCT_TIMEOUT) dbus_malloc0(size_STRUCT_TIMEOUT);       // that should be enough
#endif
    dbus_server_set_timeout_functions(server,
                                      add_timeout_callback,
                                      remove_timeout_callback,
                                      NULL,                 // no toggle-timeout function
                                      timeoutData,          // timeout callback data structure
#ifdef MALLOC_USE_REXX
                                      (void (*)(void*)) RexxFreeMemory  // that should be enough
#else
                                      dbus_free
#endif
                                      );


#if defined (DEBUG_METHODS) || defined (DEBUG_PRIVATE_SERVER)
   fprintf(stderr, "<--- end of DbusNativeServerStartup(...).\n"); fflush(stderr);
#endif

    context->ReleaseLocalReference(rpo);

    return NULL;
}


// -----------------------------------------------------------------------------------------------------
    // ============================================================================= //
    // set up and run a message loop for this connection instance
    // processes signals and method calls to registered ooRexx server objects
    // wrapped up as a Rexx method, hence easy to run it on a separate thread
/** Set up and run the server watch loop.
*/
RexxMethod2(RexxObjectPtr, DbusServerWatchLoop, CSELF, connPtr, OSELF, self)
{
#if defined (DEBUG_WATCHLOOP) || defined (DEBUG_CONNECTION)
    fprintf(stderr, "... WatchLoop(): just arrived ! ...\n"); fflush(stderr);
#endif

    RexxObjectPtr rxNil  =context->Nil();
    RexxObjectPtr rxTrue =context->True();
    RexxObjectPtr rxFalse=context->False();
    CSTRING watchLoopActive="WATCHLOOPACTIVE";      // as long as this variable is .true, the loop will get executed

    context->SetGuardOn();
    if (context->GetObjectVariable(watchLoopActive) == rxTrue)     // already running ?
    {
        context->SetGuardOff();
        return NULLOBJECT;      // return, do not start another message loop thread
    }

        // fetch Rexx pointer object to get access to the callback structure
    RexxPointerObject rpo = (RexxPointerObject) context->GetObjectVariable("WATCHLIST");
    context->SetGuardOff();

    if (rpo==NULL || rpo==rxNil)
    {
        char msg[1024]="";
        SNPRINTF( msg, 1024, "%.16s/method/DbusWatchLoop(), error 1: callback buffer 'cbd' not set, cannot create watch loop",
                              DLLNAME);

         // raise exception
        context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
        return NULLOBJECT;
    }

    if (!context->IsPointer(rpo))
    {
        char msg[1024]="";
        SNPRINTF( msg, 1024, "%.16s/method/DbusWatchLoop(), error 2: callback buffer 'cbd' is not a RexxPointer object, cannot create watch loop",
                              DLLNAME);

         // raise exception
        context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
        return NULLOBJECT;
    }

    PSTRUCT_WATCH_LIST wl=(PSTRUCT_WATCH_LIST)  context->PointerValue(rpo);    // cast to be able to use the watch list

    // DBUS watch loop to select() on DBusWatches
    int microTimeout= 10000;    // timeout in microseconds (1/100 sec): if shutdown requested, reaction time is faster for humans
#ifdef DEBUG_WATCHLOOP
    microTimeout=500000;        // timeout in microseconds (1/2 sec); will not create too much debug noise
#endif

    context->SetGuardOn();
    context->SetObjectVariable(watchLoopActive, context->True());  // indicate message processing is active
    context->SetGuardOff();

#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): about to enter message loop, changed instance variable 'active' to .true: [%s], self=[%s] self~identityHash=[%s] ...\n",
                    context->ObjectToStringValue(context->GetObjectVariable(watchLoopActive)),
                    context->ObjectToStringValue(self),
                    context->ObjectToStringValue(context->SendMessage0(self,"IDENTITYHASH"))
            ); fflush(stderr);
#endif

        // allow loop thread to be ended by Rexx, e.g. checking the status of the "active" attribute (if .false return from message loop)
    while (context->GetObjectVariable(watchLoopActive) == rxTrue)   // while watch loop active, go ahead
    {
        if  (context->CheckCondition()) // is this thread affected by a Halt-condition? If so, leave
        {
            context->SetGuardOn();
            context->SetObjectVariable(watchLoopActive,rxFalse);
            context->SetGuardOff();
            break;
        }

            // create Select() attributes
        fd_set rfds, wfds, efds;        // read, write, error fd sets
        int maxfd = 0;
        struct timeval tv;

        FD_ZERO (&rfds);
        FD_ZERO (&wfds);
        FD_ZERO (&efds);

            // loop over list of DBusWatches
        for (PSTRUCT_WATCH pw=wl->watchList; pw && pw->watch; pw=pw->next )
        {
            if (dbus_watch_get_enabled(pw->watch))  // enabled watch ?
            {
                // TODO: dbus_warch_get_unfix_fd better? (if doing a get_socket on Windows as the source code implies)
                int tmpfd=dbus_watch_get_socket(pw->watch);
                int flags=dbus_watch_get_flags(pw->watch);

                if (flags & DBUS_WATCH_READABLE)
                {
                    FD_SET (tmpfd, &rfds);
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): ---> adding watch [%p], fd=[%d] to 'rfds ...' \n", pw->watch, tmpfd);
#endif
                }

                if (flags & DBUS_WATCH_WRITABLE)
                {
                    FD_SET (tmpfd, &wfds);
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): ---> adding watch [%p], fd=[%d] to 'rfds ...' \n", pw->watch, tmpfd);
#endif
                }

#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): ---> adding watch [%p], fd=[%d] to 'efds.' \n", pw->watch, tmpfd);
#endif
                FD_SET (tmpfd, &efds);

                maxfd = (maxfd>tmpfd ? maxfd : tmpfd);
            }
        }

#ifdef DEBUG_WATCHLOOP
        tv.tv_sec  = 10;            //  timeout every ten seconds
        tv.tv_usec = 0;             // (timeout_milliseconds % 1000) * 1000;
#else
        tv.tv_sec  = 0;             //  timeout_milliseconds / 1000;
        tv.tv_usec = microTimeout;  // (timeout_milliseconds % 1000) * 1000;
#endif

        if (maxfd==0)   // just do a sleep, waiting on watches get added in the meantime
        {
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): before select() #1 ... \n");
    int res=select(0,NULL,NULL,NULL,&tv);
#else
            select(0,NULL,NULL,NULL,&tv);
#endif

#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): after select(0,NULL,NULL,NULL,&tv) #1 res=[%d]... \n", res);
#endif
        }
        else            // do a select
        {
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): before select()... \n");

    int res = select (maxfd + 1, &rfds, &wfds, &efds, &tv); // always timout
#else
            select (maxfd + 1, &rfds, &wfds, &efds, &tv); // always timout
#endif

#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): after select(maxfd+1,...) #2, res=[%d]... \n", res);
#endif

            RexxObjectPtr ropWatchLoopActive=context->GetObjectVariable(watchLoopActive);
            if (ropWatchLoopActive != rxTrue)  // stop watch loop ?
            {
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): after select() #2, learned we need to stop watch loop, breaking! \n");
#endif
                context->ReleaseLocalReference(ropWatchLoopActive);
                break;
            }
            context->ReleaseLocalReference(ropWatchLoopActive);

                // loop over list of DBusWatches
            for (PSTRUCT_WATCH pw=wl->watchList; pw && pw->watch; pw=pw->next )
            {
                // TODO: dbus_warch_get_unfix_fd better? (if doing a get_socket on Windows as the source code implies)
                int tmpfd=dbus_watch_get_socket(pw->watch);

                if ( FD_ISSET(tmpfd,&rfds) )
                {
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): ---> dbus_watch_handle watch [%p], fd=[%d] from 'rfds ...' (READABLE) \n", pw->watch, tmpfd);
#endif
                    dbus_watch_handle(pw->watch, DBUS_WATCH_READABLE);
                }

                if ( FD_ISSET(tmpfd,&wfds) )
                {
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): ---> dbus_watch_handle watch [%p], fd=[%d] from 'wfds ...' (WRITABLE) \n", pw->watch, tmpfd);
#endif
                    dbus_watch_handle(pw->watch, DBUS_WATCH_WRITABLE);
                }

                if ( FD_ISSET(tmpfd,&efds) )
                {
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): ---> dbus_watch_handle watch [%p], fd=[%d] from 'efds ...' (ERROR) \n", pw->watch, tmpfd);
#endif
                    dbus_watch_handle(pw->watch, DBUS_WATCH_ERROR);  // TODO: really to pass that flag?
                }
            }
        }
    }       // end while

#if defined (DEBUG_WATCHLOOP) || defined (DEBUG_CONNECTION)
    fprintf(stderr, "... WatchLoop(): about to leave.\n"); fflush(stderr);
#endif

    return NULL;    // do not return anything
}



// --------------------------------------------------------------------------------
        // test whether server instance is active
/** Determines whether server is active.
*
* @return <code>true</code>, if server is active, <code>false</code> else
*/
RexxMethod1(logical_t, DbusNativeServerIsActive, CSELF, serverPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_PRIVATE_SERVER) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusNativeServerIsActive: serverPtr=[%p] \n", serverPtr);
   fflush(stderr);
#endif
        // get self and carry out the
    DBusServer *server=(DBusServer *) serverPtr;
    return dbus_server_get_is_connected(server);
}


// --------------------------------------------------------------------------------
        // get and return the server's id
/** Get server's id.
*
* @return server's id
*/
RexxMethod1(RexxObjectPtr, DbusNativeServerGetId, CSELF, serverPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_PRIVATE_SERVER) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusNativeServerGetId: serverPtr=[%p] \n", serverPtr);
   fflush(stderr);
#endif
    DBusServer *server=(DBusServer *) serverPtr;

        // get and set unique id (unique bus name)
    char *tmpStr = dbus_server_get_id(server);
    if (tmpStr==NULL)
    {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusNativeServerGetId(), error 1: dbus_server_get_id() returned NULL (= 'no memory')", DLLNAME);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;
    }
    RexxStringObject rso = context->String(tmpStr);
    dbus_free(tmpStr);
    return rso;
}


// --------------------------------------------------------------------------------
        // get and return server's address
/** Get server's address.
*
* @return server's address
*/
RexxMethod1(RexxObjectPtr, DbusNativeServerGetAddress, CSELF, serverPtr)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_PRIVATE_SERVER) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusNativeServerGetAddress: serverPtr=[%p] \n", serverPtr);
   fflush(stderr);
#endif
    DBusServer *server=(DBusServer *) serverPtr;

        // get and set unique id (unique bus name)
    char *tmpStr = dbus_server_get_address(server);
    if (tmpStr==NULL)
    {
       char msg[1024]="";     // buffer for error message
       SNPRINTF( msg, 1024, "%.16s/method/DbusNativeServerGetAddress(), error 1: dbus_server_get_id() returned NULL (= 'no memory')", DLLNAME);

        // raise exception
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return NULLOBJECT;
    }
    RexxStringObject rso = context->String(tmpStr);
    dbus_free(tmpStr);
    return rso;
}


// --------------------------------------------------------------------------------
        // disconnect the server
/** Disconnect the server, no clients can be served anymore.
*/
RexxMethod2(RexxObjectPtr, DbusNativeServerDisconnect, CSELF, serverPtr, OSELF, self)
{
#if defined (DEBUG_METHODS) || defined (DEBUG_PRIVATE_SERVER) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "===> arrived in DbusNativeServerDisconnect: serverPtr=[%p] \n", serverPtr);
   fflush(stderr);
#endif

    DBusServer *server=(DBusServer *) serverPtr;
    dbus_server_disconnect(server);

    dbus_server_unref(server);      // TODO: rgf, 20140805: the 2011 private build of dbus for 32-bit Windows will abend here;

    context->SendMessage1(self, "CSELF=", context->Nil()); // set CSELF

#if defined (DEBUG_METHODS) || defined (DEBUG_PRIVATE_SERVER) || defined (DEBUG_CONNECTION)
   fprintf(stderr, "                DbusNativeServerDisconnect: serverPtr=[%p], about to leave\n", serverPtr);
   fflush(stderr);
#endif
    return NULLOBJECT;
}



/*
        // allow some tests
RexxRoutine0(int, RgfTestStuff)
{
    double d1=1234567890123.9876;
    fprintf(stderr, " d1=[%f]\n", d1);

    RexxObjectPtr rd1=context->DoubleToObject(d1);
    fprintf(stderr, "rd1=[%s]\n", context->ObjectToStringValue(rd1));

    stringsize_t numDigits=context->GetContextDigits();

    RexxObjectPtr rd12=context->DoubleToObjectWithPrecision(d1, numDigits); // 20);

    fprintf(stderr, "rd12=[%s]\n", context->ObjectToStringValue(rd12));

    CSTRING sd2="1234567890123.9876";
    RexxObjectPtr rd2=context->NewString(sd2,strlen(sd2));
    fprintf(stderr, "rd2=[%s]\n", context->ObjectToStringValue(rd2));

    double d2;
    logical_t flag;
    flag=context->ObjectToDouble(rd2, &d2);
    fprintf(stderr, " d2=[%f], flag=[%d]\n", d2, (int) flag);

    fprintf(stderr, "----------- now testing 64-bit int ------------\n");


    int64_t      val=0;
    RexxObjectPtr rop=NULL;

    for (int64_t i=2147483645; i<2147483660; i++)
    {
        rop=context->Int64ToObject(i);
        flag=false;
        val=0;
        flag=context->ObjectToInt64(rop, &val);
        fprintf(stderr, "int64: [%lld], rop~string=[%s], flag=[%d], val=[%lld]\n",
                                   i, context->ObjectToStringValue(rop), (int) flag, val);
        fflush(stderr);
    }

    fprintf(stderr,"\n");
    for (int64_t i=4294967295; i<4294967299; i++)
    {
        rop=context->Int64ToObject(i);
        flag=false;
        val=0;
        flag=context->ObjectToInt64(rop, &val);
        fprintf(stderr, "int64: [%lld], rop~string=[%s], flag=[%d], val=[%lld]\n",
                                   i, context->ObjectToStringValue(rop), flag, val);
        fflush(stderr);
    }
    fprintf(stderr,"\n");

    fprintf(stderr, "----------- now testing conversion of ooRexx number to 64-bit int ------------\n");
    CSTRING testVal="2147483660";
    rop=context->String(testVal);

    flag=false;
    val=0;
    flag=context->ObjectToInt64(rop, &val);
    fprintf(stderr, "int64: rop~string=[%s], flag=[%d], val=[%lld]\n",
                               context->ObjectToStringValue(rop), flag, val);
    fflush(stderr);

    return 0;
}

*/

// -----------------------------------------------------------------------------------------------------
// -------------------- Rexx package definitions ----------------------

// External functions if loaded via Rexx
    // Build the list of entry points for the routines:
    RexxRoutineEntry dbus_external_functions[] =
    {
        REXX_TYPED_ROUTINE( DbusVersionLibrary      , DbusVersionLibrary          ),
        REXX_TYPED_ROUTINE( DbusValidate            , DbusValidate                ),
        REXX_TYPED_ROUTINE( DbusHaltRexx            , DbusHaltRexx                ),

        REXX_TYPED_ROUTINE( DbusGetTID              , DbusGetTID                  ),
//        REXX_TYPED_ROUTINE( RgfTestStuff            , RgfTestStuff                ),

        REXX_LAST_ROUTINE()
    };

// External functions if loaded via Rexx
    // Build the list of entry points for the routines:
    RexxMethodEntry dbus_external_methods [] =
    {
        REXX_METHOD( DbusNativeGetSystemBus         , DbusNativeGetSystemBus          ),
        REXX_METHOD( DbusNativeGetSessionBus        , DbusNativeGetSessionBus         ),


        REXX_METHOD( DbusGetUniqueBusName           , DbusGetUniqueBusName            ),
        REXX_METHOD( DbusGetUniqueBusId             , DbusGetUniqueBusId              ),
        REXX_METHOD( DbusGetUniqueMachineId         , DbusGetUniqueMachineId          ),

        REXX_METHOD( DbusBusNameRequest             , DbusBusNameRequest              ),
        REXX_METHOD( DbusBusNameHasOwner            , DbusBusNameHasOwner             ),
        REXX_METHOD( DbusBusNameRelease             , DbusBusNameRelease              ),

        REXX_METHOD( DbusBusAddMatch                , DbusBusAddMatch                 ),
        REXX_METHOD( DbusBusRemoveMatch             , DbusBusRemoveMatch              ),


        REXX_METHOD( DbusBusSignalMessage           , DbusBusSignalMessage            ),
        REXX_METHOD( DbusBusCallMessage             , DbusBusCallMessage              ),

        REXX_METHOD( DbusMessageLoop                , DbusMessageLoop                 ),


           // server related, 2011-07-21
        REXX_METHOD( DbusNativeConnectionOpenPrivate, DbusNativeConnectionOpenPrivate ),
        REXX_METHOD( DbusNativeConnectionClose      , DbusNativeConnectionClose       ),
        REXX_METHOD( DbusNativeServerStartup        , DbusNativeServerStartup         ),
        REXX_METHOD( DbusNativeServerIsActive       , DbusNativeServerIsActive        ),
        REXX_METHOD( DbusNativeServerGetId          , DbusNativeServerGetId           ),
        REXX_METHOD( DbusNativeServerGetAddress     , DbusNativeServerGetAddress      ),
        REXX_METHOD( DbusNativeServerDisconnect     , DbusNativeServerDisconnect      ),
           // server related, 2011-08-06
        REXX_METHOD( DbusServerWatchLoop            , DbusServerWatchLoop             ),
                    // 2011-08-11
        REXX_METHOD( DbusServerTimeoutLoop          , DbusServerTimeoutLoop           ),
                    // 2011-08-17
        REXX_METHOD( DbusConnectionIsConnected      , DbusConnectionIsConnected       ),
                    // 2011-08-22
        REXX_METHOD( DbusConnectionCanSendType      , DbusConnectionCanSendType       ),
        REXX_METHOD( DbusConnectionIsAuthenticated  , DbusConnectionIsAuthenticated   ),


        REXX_LAST_METHOD()
    };

    RexxPackageEntry dbus_package_entry =
    {
        STANDARD_PACKAGE_HEADER
        REXX_INTERPRETER_4_2_0,             // anything including and after 4.2.0 will work
        "dbus",                             // name of the package
        "2.00",                             // package version information

#if defined(REXX_LOADER_UNLOADER) || defined (REXX_LOADER)
        dbusLoader,
#else
        NULL,                               // no loader function
#endif

#if defined(REXX_LOADER_UNLOADER) || defined (REXX_UNLOADER)
        dbusUnloader,
#else
        NULL,                               // no unloader function
#endif

        dbus_external_functions,            // the exported functions
        dbus_external_methods               // no exported methods
    };
    // The package loading stub.  OOREXX_GET_PACKAGE() is a macro that, basically,
    // defines a function whose name is RexxGetPackage().  On Windows, this
    // function, RexxGetPackage(), must be in the exports list of the .def file used
    // when linking this library.
    OOREXX_GET_PACKAGE(dbus);


#ifdef __cplusplus
}               // closing bracket
#endif
