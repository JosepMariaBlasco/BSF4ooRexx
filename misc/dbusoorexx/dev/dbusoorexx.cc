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

    version:    099.20110821
    license:      Apache License 2.0

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


#ifdef __cplusplus
extern "C" {            // make sure no C++ name mangling comes into the way
#endif

#ifdef _MSC_VER
   #pragma comment(lib, "dbus-1.lib")  // as per the hint of Pontus Carlsson in an e-mail from 2011-08-19
#endif

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


   // rgf, 2011-06-11, cf. <>http://gcc.gnu.org/gcc-4.3/porting_to.html>
   // (needed for old dbus-example to be able to compile unaltered)
#include <cstring>    // ? maybe removable, once old code is replaced ?

#ifdef _MSC_VER
# pragma warning(disable:4100)
          // MS does not define a C99 "snprintf()" function
    #define SNPRINTF _snprintf
#else
    #define SNPRINTF snprintf
#endif



#ifdef DBUSOOREXX_32
    #define DBUS_REXXVERSION    "099.20110822 32-bit"  // version: "MajorNumber"."YYYYMMDD"
#elif defined (DBUSOOREXX_64)
    #define DBUS_REXXVERSION    "099.20110822 64-bit"  // version: "MajorNumber"."YYYYMMDD"
#else
    #define DBUS_REXXVERSION    "099.20110822 n/a-bit"  // version: "MajorNumber"."YYYYMMDD"
#endif

#ifndef TID
    #define TID thread_id_t
#endif

#define DLLNAME                 "dbusoorexx"


// =================== debug defines ...
#define REXX_LOADER             // needed for dbus_threads_init_default(), cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusThreads.html#ga33b6cf3b8f1e41bad5508f84758818a7>


// #define DEBUG_RGF           // for temporary debug output


/*
// #define REXX_UNLOADER
// #define DEBUG_REXX_HALT      // 2011-08-03

#define REXX_LOADER_UNLOADER
#define DEBUG_LOADER_UNLOADER

#define DEBUG_ARRAY

#define DEBUG_MARSHALLING
#define DEBUG_MARSHALLING_2
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
*/


// if commented, then the dbus malloc routines are used
#define MALLOC_USE_REXX     // 2011-06-26: originally defined to trace down a segmentation fault using dbus' malloc, test using Rexx allocations


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
                                        //   marshalling:   "s"->"ay", if "s" input and "ay" output!
                                        //   unmarshalling: "aay" -> "as"
    logical_t            unmarshal_ay_as_string;    // determined by connection attribute: true ay->string, ay->ay else
    logical_t            marshal_ay_as_string; // determined in marshal(...)'s array branch

    logical_t            is_dict;       // special handling of byte array for Rexx active ?,
                                        //   marshalling:   .directory->"a{sY}"
                                        //   unmarshalling: "a{xY}" -> .directory object !


}  STRUCT_MARSHAL;

typedef STRUCT_MARSHAL *PSTRUCT_MARSHAL;            // pointer to structure
long size_STRUCT_MARSHAL=sizeof(STRUCT_MARSHAL);    // get and memorize size of structure



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

    // from BSF4ooRexx.cc
    // allocates and creates a Rexx-like error message
    // returned *msg must be freed
inline char * RgfCreateRexxlikeErrorInfo (RexxThreadContext *rtc, RexxDirectoryObject condObj, const char * header)
{
    RexxCondition cond;
    rtc->DecodeConditionInfo(condObj, &cond);

    char *msg=(char *)RexxAllocateMemory( 4096 );
    msg[0]=0;

        // format message according to exception type
    if (rtc->SendMessage1(cond.conditionName, "EQUALS", rtc->String("SYNTAX"))==rtc->True()) // SYNTAX condition ?
    {
        char const *line1=rtc->CString(rtc->SendMessage0(rtc->DirectoryAt(condObj, "TRACEBACK"), "FIRSTITEM")) ; // first traceback line=error line

        char line2[2048]="";        // will only use 1024 of it
        SNPRINTF(line2, 2048, "Error %d running %.512s line %d:  %.1024s",
                            (int) cond.rc,      (NULL != cond.program   && rtc->Nil() != cond.program   ? rtc->CString( cond.program )  : "program n/a")   ,
                            (int) cond.position,(NULL != cond.errortext && rtc->Nil() != cond.errortext ? rtc->CString( cond.errortext ): "errortext n/a") );

        char line3[2048]="";        // will only use 1024 of it
        SNPRINTF( line3, 2048, "Error %d.%d:  %.1024s", (int) (cond.code / 1000),
                                     (int) (cond.code % 1000),
                                     (NULL != cond.message && rtc->Nil() != cond.message ? rtc->CString( cond.message ) : "message n/a") );

        SNPRINTF( msg, 4096, "%.16s%.512s:\n%.1024s\n%.1024s\n%.1024s", DLLNAME, header, line1, line2, line3);
    }
    else
    {
        // sprintf(msg, "%.16s%.512s: Rexx condition [%.512s] raised", DLLNAME, header, rtc->CString(cond.conditionName));
        SNPRINTF( msg, 4096, "%.16s%.512s: Rexx condition [%.512s] raised",
                                  DLLNAME, header,
                                  (NULL != cond.conditionName && rtc->Nil() != cond.conditionName ? rtc->CString(cond.conditionName) : "conditionName n/a") );
    }

    return msg;
}




#if defined (REXX_LOADER_UNLOADER) || defined (REXX_LOADER)
    // typedef void (RexxEntry *RexxPackageLoader)(RexxThreadContext *);
    // as of ooRexx 4.0: the library gets loaded once and is available globally to all RexxInstances,
    //                   hece it runs once per process only
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

//    rgfInitLocks();        // Initialize critical section (from BSF4ooRexx)
}
#endif




#if defined (REXX_LOADER_UNLOADER) || defined (REXX_UNLOADER)
// typedef void (RexxEntry *RexxPackageUnloader)(RexxThreadContext *);
// as of ooRexx 4.0: the library gets loaded once and is available globally to all RexxInstances,
//                   hece it runs once per process only
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
RexxRoutine0(RexxStringObject, DbusVersion)
{
#ifdef RGF_INFO
    fprintf(stderr, "*** RGF_INFO: DbusVersion() 1, version=[%.256s] ...\n", DBUS_REXXVERSION);
    fflush(stderr);
#endif

  int major, minor, micro;
  dbus_get_version(&major, &minor, &micro);

  char msg[256]="";     // buffer for error message
  SNPRINTF(msg, 256, "%s=[%s], compile-time dbus=[%s], runtime dbus=[%d.%d.%d]",
                    DLLNAME, DBUS_REXXVERSION, DBUS_VERSION_STRING,
                    major, minor, micro
                    );

  return context->String(msg);
}


// external Rexx function for validating values; as DBUS 1.4.x only supports validating signatures,
// initially only signatures get evaluated
//
// validate signature: if o.k. return .true, else the error message
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
// TODO: decide whether to make it available
// allows to halt all Rexx threads of current interpreter; experimental
RexxRoutine0(RexxStringObject, DbusHaltRexx)    // create Halt condition in all threads
{
    context->threadContext->instance->Halt();   // raise the Halt condition in all threads of the current Rexx interpreter
    return NULL;                                // no return value
}


    // ********************************************************************
    // *                   --- Methods ---                                *
    // ********************************************************************

RexxMethod0(RexxStringObject, DbusGetVersion)
{
#if defined (DEBUG_METHODS)
   fprintf(stderr, "===> arrived in DbusGetVersion\n");
   fflush(stderr);
#endif

    int major, minor, micro;
    dbus_get_version(&major, &minor, &micro);

  char msg[256]="";     // buffer for error message
  SNPRINTF(msg, 256, "%s=[%s], compile-time dbus=[%s], runtime dbus=[%d.%d.%d]",
                    DLLNAME, DBUS_REXXVERSION, DBUS_VERSION_STRING,
                    major, minor, micro
                    );
  return context->String(msg);
}




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
       return context->Nil();       // return .nil
    }

    if (NULL == conn)
    {
        // raise exception
       SNPRINTF( msg, 1024, "%.16s/method/DbusNativeGetSystemBus(), connection error 2: no connection returned", DLLNAME);
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return context->Nil();       // return .nil
    }

        // save conn in a Rexx pointer object and save it as CSELF
    RexxPointerObject rpo = context->NewPointer(conn);
    context->SendMessage1(genericBusObject, "CSELF=",         rpo); // set CSELF

        // set well known bus name
    context->SendMessage1(genericBusObject, "BUSTYPE=", context->String("system"));

    return genericBusObject;
}


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
       return context->Nil();       // return .nil
    }

    if (NULL == conn)
    {
        // raise exception
       SNPRINTF( msg, 1024, "%.16s/method/DbusNativeGetSessionBus(), connection error 2: no connection returned", DLLNAME);
       context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
       return context->Nil();       // return .nil
    }

        // save conn in a Rexx pointer object and save it as CSELF
    RexxPointerObject rpo = context->NewPointer(conn);
    context->SendMessage1(genericBusObject, "CSELF=",         rpo); // set CSELF

        // set well known bus name
    context->SendMessage1(genericBusObject, "BUSTYPE=", context->String("session"));

    return genericBusObject;
}


        // get and return unique bus name (.Dbus instance method)
RexxMethod1(RexxStringObject, DbusGetUniqueBusName, CSELF, connPtr)
{
#if defined (DEBUG_METHODS)
   fprintf(stderr, "===> arrived in DbusGetUniqueBusName\n");
   fflush(stderr);
#endif
    DBusConnection *conn=(DBusConnection *) connPtr;

    const char *tmpStr = dbus_bus_get_unique_name(conn);
    RexxStringObject rso = context->String( (tmpStr==NULL ? "" : tmpStr) );
    return rso;
}

        // get and return unique machine id
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


        // get and return unique bus id (.Dbus instance method)
RexxMethod1(RexxObjectPtr, DbusGetUniqueBusId, CSELF, connPtr)
{
#if defined (DEBUG_METHODS)
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
       return context->Nil();       // return .nil
    }
    RexxStringObject rso = context->String( (tmpStr==NULL ? "" : tmpStr) );
    dbus_free(tmpStr);
    // context->SendMessage1(genericBusObject, "UNIQUEBUSNAME=", rso);
    return rso;
}


    // if open, returns true, false else; cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusConnection.html#ga611ae94556af36fe30bfb547366ca4e1>
RexxMethod1(RexxObjectPtr, DbusConnectionIsConnected, CSELF, connPtr)
{
#if defined (DEBUG_METHODS)
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



    // if open, returns true, false else; cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusConnection.html#ga611ae94556af36fe30bfb547366ca4e1>
    // expects one char; if that is a supported type code returns .true, otherwise .false
RexxMethod2(RexxObjectPtr, DbusConnectionCanSendType, CSTRING, typeCode, CSELF, connPtr)
{
#if defined (DEBUG_METHODS)
   fprintf(stderr, "===> arrived in DbusConnectionCanSendType\n");
DbusConnectionCanSendType
   fflush(stderr);
#endif
    DBusConnection *conn=(DBusConnection *) connPtr;
    if (conn!=NULL && connPtr!=context->Nil() && dbus_connection_can_send_type(conn, *typeCode))
    {
        return context->True();
    }
    return context->False();
}





    // ============================================================================= //
    // busname related --->

        // request a unique bus name (.Dbus instance method)
RexxMethod3(RexxObjectPtr, DbusBusNameRequest, CSTRING,       busName,
                                               OPTIONAL_RexxObjectPtr, flags,
                                               CSELF,         connPtr)
{
#if defined (DEBUG_METHODS)
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
           // return context->Nil();       // return .nil
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
        // deprecated because of race-conditions, cf. <http://dbus.freedesktop.org/doc/api/html/group__DBusBus.html#ga5331b172dd8ed00eec130e894c5c2a0b>
RexxMethod2(RexxObjectPtr, DbusBusNameHasOwner, CSTRING, busName,
                                                CSELF,   connPtr)
{
#if defined (DEBUG_METHODS)
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
RexxMethod2(RexxObjectPtr, DbusBusNameRelease, CSTRING, busName,
                                               CSELF,   connPtr)
{
#if defined (DEBUG_METHODS)
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

        // removes the most recent added rule, if found
RexxMethod3(RexxObjectPtr, DbusBusAddMatch, CSTRING, rule,
                                            OPTIONAL_RexxObjectPtr, block,
                                            CSELF,   connPtr)
{
#if defined (DEBUG_METHODS)
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
            // return context->Nil();       // return .nil
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
RexxMethod3(RexxObjectPtr, DbusBusRemoveMatch, CSTRING, rule,
                                               OPTIONAL_RexxObjectPtr, block,
                                               CSELF,   connPtr)
{
#if defined (DEBUG_METHODS)
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
            // return context->Nil();       // return .nil
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
    // return array's content as a comma-separated string
inline CSTRING ARR_AS_STRING( RexxArrayObject arrIdx , RexxThreadContext *context)
{
    RexxObjectPtr arg1= context->NewString("L",1);
    RexxObjectPtr arg2= context->NewString(",",1);
    RexxObjectPtr rop = context->SendMessage2(arrIdx, "MAKESTRING", arg1, arg2);
    return context->ObjectToStringValue(rop);
}


// --------------------------------------------------------------------------------
    // convenience inline function to query the items in a given dimension
inline size_t GET_ARRAY_SIZE_AT_DIMENSION(RexxArrayObject rao, int32_t dimension, RexxThreadContext *context)
{
    RexxObjectPtr ropLevel=context->Int32ToObject(dimension);

    size_t    val=0;
    logical_t flag;

    flag=context->ObjectToStringSize(context->SendMessage1(rao, "DIMENSION", ropLevel), &val);
    return val;
}


// --------------------------------------------------------------------------------
    // return number of top-level arguments needed for given signature (needs to be syntactically correct)
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
inline RexxStringObject GET_REXX_DATE_TIME_AS_REXX_STRING(RexxThreadContext *context)
{
    RexxClassObject dtClz=context->FindClass("DATETIME");       // get the DateTime class
    RexxObjectPtr   dt   =context->SendMessage0(dtClz, "NEW");  // get an instance
    return context->ObjectToString(dt);                         // return string rendering
}

// --------------------------------------------------------------------------------
    // convenience inline function to query the items in a given dimension
inline size_t GET_UNMARSHAL_ARRAY_VALUE_AT_INDEX(RexxArrayObject arrIdx, int32_t index, RexxThreadContext *context)
{

    size_t    val=0;
    logical_t flag;

#ifdef DEBUG_ARRAY
fprintf(stderr, "     /// GET_UNMARSHAL_ARRAY..., ArrayAt(index=[%d)=[%p]\n", index, context->ArrayAt(arrIdx, index));
#endif

        // fetch value at arrIdx' index and return it as a number
    RexxObjectPtr rop=context->ArrayAt(arrIdx, index);
    if (rop!=NULL)
    {
        flag=context->ObjectToStringSize(context->ArrayAt(arrIdx, index), &val);
    }
    return val;
}

// --------------------------------------------------------------------------------
    // convenience inline function to set the index array to the desired value
inline void SET_ARRAY_IDX(RexxArrayObject arrIdx, size_t idx, size_t numVal, RexxThreadContext *context)
{
    context->ArrayPut(arrIdx, context->StringSizeToObject(numVal), idx);
}

// --------------------------------------------------------------------------------
    // convenience inline function to initialize a new (sub) STRUCT_MARSHAL
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
logical_t unmarshal(DBusMessageIter *iter, char typeCode, RexxArrayObject rxResult, PSTRUCT_MARSHAL pmarsh)
{
#ifdef DEBUG_MARSHALLING
    // fprintf(stderr, "---> arrived in unmarshal(...), typeCode=[%c], cursor=[%zu], signature=[%s], iter=[%p], rxResult=[%p] \n",
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
    while (pmarsh->cursor<=pmarsh->elementEnd &&
           (currentType = dbus_message_iter_get_arg_type (iter)) != DBUS_TYPE_INVALID)
    {

        RexxObjectPtr currRexxObj=NULL;
        switch (currentType)
        {
        // case 's' :      // STRING (UTF-8): must not contain nul bytes, but must end wiht a nul byte
        case DBUS_TYPE_STRING :     // STRING (UTF-8): must not contain nul bytes, but must end wiht a nul byte
            {
                CSTRING str;
                dbus_message_iter_get_basic(iter, &str);
                currRexxObj=pmarsh->context->String(str);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any

#ifdef DEBUG_UNMARSHAL
// fprintf(stderr, ".... unmarshal() - 's', cursor now: [%zu], end-element: [%zu], cursor>=end-element? [%d] - signature=[%s], value=[%s] /str=[%s] \n",
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

        // case 'g' :      // SIGNATURE
        case DBUS_TYPE_SIGNATURE :
            {
                CSTRING str;
                dbus_message_iter_get_basic(iter, &str);
                currRexxObj=pmarsh->context->String(str);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        // case 'o' :      // OBJECT_PATH (like STRING)
        case DBUS_TYPE_OBJECT_PATH :
            {
                CSTRING str;
                dbus_message_iter_get_basic(iter, &str);
                currRexxObj=pmarsh->context->String(str);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        // case 'y' :      // BYTE
        case DBUS_TYPE_BYTE :
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

        // case 'b' :      // BOOLEAN
        case DBUS_TYPE_BOOLEAN :
            {
                dbus_bool_t val=false;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->LogicalToObject((logical_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        // case 'n' :      // INT16
        case DBUS_TYPE_INT16 :
            {
                dbus_int16_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->Int32ToObject((int32_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        // case 'q' :      // UINT16
        case DBUS_TYPE_UINT16 :
            {
                dbus_uint16_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->UnsignedInt32ToObject((uint32_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;


        // case 'i' :      // INT32
        case DBUS_TYPE_INT32 :
            {
                dbus_int32_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->Int32ToObject((int32_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;


#if defined ( DBUS_TYPE_UNIX_FD )      // e.g. not defined for Windows
        // case 'h' :      // UNIX_FD: a 32-bit unsigned integer accordig to dbus-specification.html
        case DBUS_TYPE_UNIX_FD :
            {
                dbus_uint32_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->UnsignedInt32ToObject((uint32_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;
#endif

        // case 'u' :      // UINT32
        case DBUS_TYPE_UINT32 :
            {
                dbus_uint32_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->UnsignedInt32ToObject((uint32_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        // case 'x' :      // INT64
        case DBUS_TYPE_INT64 :
            {
                dbus_int64_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->Int64ToObject((int64_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        // case 't' :      // UINT64
        case DBUS_TYPE_UINT64 :
            {
                dbus_uint64_t val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->UnsignedInt64ToObject((uint64_t) val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;

        // case 'd' :      // DOUBLE (IEEE 754)
        case DBUS_TYPE_DOUBLE :
            {
                double val=0;
                dbus_message_iter_get_basic(iter, &val);
                currRexxObj=pmarsh->context->DoubleToObject(val);   // turn into Rexx string
                pmarsh->cursor++;       // let us point to the next typeCode if any
            }
            break;


            // containers -------------------------------------
        // case 'a' :      // ARRAY
        case DBUS_TYPE_ARRAY :      // ARRAY
            {
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


            // fprintf(stderr, "     /--> unmarshal(): DBUS_TYPE_ARRAY - signature=[%s], cursorPos=[%zu|%c]: arrStartPos=[%zu|%c], arrEndPos=[%zu|%c], arrLen=[%zu] | elStartPos=[%zu|%c], elEndPos=[%zu|%c], elLen=[%zu]\n",
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
    // fprintf(stderr, "     /--> unmarshal(): DBUS_TYPE_ARRAY - signature=[%s], about to create array with [%zu] dimensions\n",
    fprintf(stderr, "     /--> unmarshal(): DBUS_TYPE_ARRAY - signature=[%s], about to create array with [%lu] dimensions\n",
                               pmarsh->signature, (unsigned long) nrDims); fflush(stderr);
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
                    pmarsh->context->ArrayAppend(rxWithArgs, pmarsh->context->ArrayAt(arrIdx, tmpIdx));
                }

                currRexxObj=pmarsh->context->SendMessage2(pmarsh->arrayClass, "SENDWITH", pmarsh->context->String("NEW"), rxWithArgs);

#ifdef DEBUG_ARRAY
   // fprintf(stderr, "     unmarshal(...), 'a' - currRexxObj=[%p]~dimension=[%zu];  pmarsh->dimensions=[%zu] - nrDims=[%zu]; | ~size=[%zu], ~items=[%zu]\n",
   fprintf(stderr, "     unmarshal(...), 'a' - currRexxObj=[%p]~dimension=[%lu];  pmarsh->dimensions=[%lu] - nrDims=[%lu]; | ~size=[%lu], ~items=[%lu]\n",
                         currRexxObj,
                         (unsigned long) pmarsh->context->ArrayDimension((RexxArrayObject) currRexxObj),
                         (unsigned long) pmarsh->dimensions,
                         (unsigned long) nrDims,
                         (unsigned long) pmarsh->context->ArraySize((RexxArrayObject) currRexxObj),
                         (unsigned long) pmarsh->context->ArrayItems((RexxArrayObject) currRexxObj)
                         );

   // fprintf(stderr, "     unmarshal(...), 'a' - p->is_ay=[%zu], p->unmarshal_ay_as_string=[%zu], arrIdx=[%s], p->signature=[%s]\n",
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

        // fprintf(stderr, "     /<-- signature=[%s], cursorPos=[%zu|%c]: end-of-array-processing #1. currRexxObj: dims=[%zu], items=[%zu], size=[%zu] \n",
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
                        currRexxObj=tmpObj;
                    }

#ifdef DEBUG_ARRAY
   fprintf(stderr, "     unmarshal(...), DICT-ARRAY or STRING for 'ay' -> currRexxObj=[%p]=[%s] !\n",
                                      currRexxObj,
                                      (currRexxObj == NULL ? NULL : pmarsh->context->ObjectToStringValue(currRexxObj))
                                      );
#endif
                }

                    // move cursor to next char in signature
                pmarsh->cursor=pmarsh->elementEnd+1;       // let us point to the next typeCode if any
            }
            break;


        // case '(' :      // STRUCT - begin ("r")
        case DBUS_TYPE_STRUCT :      // STRUCT - begin ("r")
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

   // fprintf(stderr, "... unmarshal(...): *** STRUCT: BEFORE - localM.signature=[%s], localM.cursor=[%zu], localM.elementStart=[%zu], localM.elementEnd=[%zu]\n",
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


        // case '{' :      // DICT_ENTRY - begin ("e"): invoked via unmarshalArray(...)
        case DBUS_TYPE_DICT_ENTRY :      // DICT_ENTRY - begin ("e"): invoked via unmarshalArray(...)
            {
                fprintf(stderr, "%s: *** unmarshal(...): DICT ENTRY - begin: unmarshal() - ERROR !! SHOULD NEVER ARRIVE HERE!\n", DLLNAME);
                pmarsh->cursor=pmarsh->elementEnd+2;       // let us point to the next typeCode if any
            }
            break;

        // case '\0':      // INVALID (end-marker)
        default:
    fprintf(stderr, "%s: *** unmarshal(...): ??? default-branch ???: SHOULD NEVER ARRIVE HERE!\n", DLLNAME);
             break;
        }

            // now save current Rexx object for Rexx result
        pmarsh->context->SendMessage1(rxResult, "APPEND", currRexxObj);     // append current Rexx object to array

#ifdef DEBUG_UNMARSHAL
    // fprintf(stderr, "\nrxResult=[%p], currRexxObj=[%p], currRexxObj~class=[%s], size=[%zu]\n",
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
logical_t unmarshalArray(DBusMessageIter *iter, size_t level, RexxArrayObject rao, RexxArrayObject arrIdx, PSTRUCT_MARSHAL pmarsh)
{
#ifdef DEBUG_ARRAY
       // fprintf(stderr, "---> arrived in unmarshalArray(...): level=[%zu], rao=[%s], arrIdx=[%s]\n",
       fprintf(stderr, "---> arrived in unmarshalArray(...): level=[%lu], rao=[%s], arrIdx=[%s]\n",
                         (unsigned long) level,
                         ARR_AS_STRING(rao, pmarsh->context),
                         ARR_AS_STRING(arrIdx, pmarsh->context) );

       // fprintf(stderr, "                unmarshalArray(...): signature=[%s], dims=[%zu] - cursor=[%zu], arrStart=[%zu], arrEnd=[%zu], elStart=[%zu], elEnd=[%zu]\n",
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

        // determine signature to use for opening a container
#ifdef MALLOC_USE_REXX
    char *localSignature=(char *) RexxAllocateMemory(pmarsh->signatureLength+1);  // that should be enough
    memset(localSignature, 0, pmarsh->signatureLength+1);
#else
    char *localSignature=(char *) dbus_malloc0(pmarsh->signatureLength+1);  // that should be enough
#endif

    char *start=pmarsh->arrStart+1;
    strncpy(localSignature, start, pmarsh->elementEnd-start+1);

#ifdef DEBUG_ARRAY
    // fprintf(stderr, "     - level=[%zu], localSignature=[%s], rao=[%p] \n", level, localSignature, rao); fflush(stderr);
    fprintf(stderr, "     - level=[%lu], localSignature=[%s], rao=[%p] \n", (unsigned long) level, localSignature, rao); fflush(stderr);
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
   // fprintf(stderr, "    unmarshalArray(...): BYTE-ARRAY-HANDLING ! localSignature=[%s], p->signature=[%s], level=[%zu], p->dimensions=[%zu], p->is_ay=[%d], p->unmarshal_ay_as_string=[%d]\n",
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
   // fprintf(stderr, "    unmarshalArray(...): arrCount=[%d], arrIdx=[%s],  nrDims=[%zu], level=[%zu] \n",
   fprintf(stderr, "    unmarshalArray(...): arrCount=[%d], arrIdx=[%s],  nrDims=[%lu], level=[%lu] \n",
                             arrCount,
                             ARR_AS_STRING(arrIdx, pmarsh->context),
                             (unsigned long) nrDims,
                             (unsigned long) level
                        );

   // fprintf(stderr, "    unmarshalArray(...): arrIdx~size=[%zu], nrDims=[%zu] \n",
   fprintf(stderr, "    unmarshalArray(...): arrIdx~size=[%lu], nrDims=[%lu] \n",
                             (unsigned long) pmarsh->context->ArraySize(arrIdx),
                             (unsigned long) nrDims );


#endif
            // adjust index array (to not have too many dimensions!)
        if (pmarsh->context->ArraySize(arrIdx)>nrDims)
        {

            arrIdx=(RexxArrayObject) pmarsh->context->SendMessage2(arrIdx, "SECTION",
                                         pmarsh->context->String("1"),
                                         pmarsh->context->StringSizeToObject(nrDims) );

#ifdef DEBUG_ARRAY
   // fprintf(stderr, "    unmarshalArray(...): must adjust arrIdx (too many indices!), now arrIdx=[%s] as nrDims=[%zu] \n ",
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
            if (pmarsh->is_ay && pmarsh->unmarshal_ay_as_string) // turn byte array into string
            {
                tmpObj=pmarsh->context->String(array, arrCount); // turn into Rexx string
                pmarsh->context->SendMessage2(rao, "PUT", tmpObj, arrIdx);  // save "ay" as "s" at its position
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
    // fprintf(stderr, " ............... analyze signature, set arrStart/arrEnd  elStart/elEnd, dims=[%zu], calcDims=[%zu], lM.signature=[%s], lm.arrStart=[%c], lm.arrEnd=[%c]\n",
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
    // if (localM.dimensions==0)      // o.k. arrived at element
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
                unmarshalDict(&iter_array, dirRexxObj, &localM);
            }

            // pmarsh->cursor=pmarsh->elementEnd+1;    // let us point to the next typeCode if any
#ifdef DEBUG_ARRAY
    fprintf(stderr, "     /<-- unmarshalArray(): DBUS_TYPE_ARRAY - signature=[%s], branch 'DICT ARRAY', dirRexxObj~itesm=[%s] \n",
               pmarsh->signature,
               pmarsh->context->ObjectToStringValue(pmarsh->context->SendMessage0(dirRexxObj, "ITEMS") )
               ); fflush(stderr);
#endif

            SET_ARRAY_IDX(arrIdx, level, 1, pmarsh->context); // add 1 to maximum items
            pmarsh->context->SendMessage2(rao, "PUT", dirRexxObj, arrIdx);

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

                pmarsh->context->SendMessage2(rao, "PUT", pmarsh->context->ArrayAt(tmpArr,1), arrIdx);

#ifdef DEBUG_ARRAY
    fprintf(stderr, "----> Ticker # 7c, stored value [%s] at arrIdx=[%s] \n",
                           pmarsh->context->ObjectToStringValue(pmarsh->context->SendMessage1(rao,"AT",arrIdx)),
                           currArrIdx
                           );

    // fprintf(stderr, "----> Ticker # 7d, signature=[%s], cursor=[%zu], elementStart=[%zu] \n",
    fprintf(stderr, "----> Ticker # 7d, signature=[%s], cursor=[%lu], elementStart=[%lu] \n",
                    localM.signature,
                    (unsigned long) (localM.cursor - localM.signature),
                    (unsigned long) (localM.elementStart - localM.signature) );
#endif

                localM.cursor=localM.signature;     // reset cursor
            }
        }
    }
    else        // recurse into next dimension!
    {
#ifdef DEBUG_ARRAY
    // fprintf(stderr, "----> Ticker # 8-, level=[%zu], rao=[%p], arrIdx=[%s], &iter_array(arg_type=[%d])==[%d] (DBUS_ARRAY_TYPE) \n",
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
    // fprintf(stderr, "----> Ticker # 8a, recursing with values, level+1=[%zu], rao=[%p], arrIdx=[%s] \n", level+1, rao, ARR_AS_STRING(arrIdx, pmarsh->context));
    fprintf(stderr, "----> Ticker # 8a, recursing with values, level+1=[%lu], rao=[%p], arrIdx=[%s] \n", (unsigned long) level+1, rao, ARR_AS_STRING(arrIdx, pmarsh->context));
#endif

            unmarshalArray(&iter_array, level+1, rao, arrIdx, &localM);

#ifdef DEBUG_ARRAY
    // fprintf(stderr, "<---- Ticker # 8b, current level again: [%zu] \n", level);
    fprintf(stderr, "<---- Ticker # 8b, current level again: [%lu] \n", (unsigned long) level);
#endif
            indexLevel++;
            // reset next dimension's item count value to 0
            dbus_message_iter_next(&iter_array);
        }
#ifdef DEBUG_ARRAY
    // fprintf(stderr, "<---- Ticker # 9, back at level [%zu] \n", level);
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
logical_t unmarshalDict(DBusMessageIter *iter_array, RexxDirectoryObject rxMap, PSTRUCT_MARSHAL pmarsh)
{

#ifdef DEBUG_DICT
   fprintf(stderr, "---> arrived in unmarshalDict(...), rxMap=[%p], signature=[%s] \n", rxMap, pmarsh->signature);
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

#ifdef DEBUG_DICT
    fprintf(stderr, "     unmarshalDict() - signature=[%s] -> localKeySignature=[%s] & localItemSignature[%s]\n",
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

    // loop over iter_array unmarshal key and unmarshal value to store with directory
    DBusMessageIter iter_dict;
    while (dbus_message_iter_get_arg_type (iter_array) != DBUS_TYPE_INVALID)
    {
        dbus_message_iter_recurse (iter_array, &iter_dict);
        if (dbus_message_iter_get_arg_type (&iter_dict) != DBUS_TYPE_INVALID)
        {
            RexxArrayObject key=pmarsh->context->NewArray(0);
            localKeyM.cursor=localKeyM.elementStart;  // let cursor point to element's start
            unmarshal(&iter_dict, *localKeySignature, key, &localKeyM); // process

            RexxArrayObject item=pmarsh->context->NewArray(0);
            localItemM.cursor=localItemM.elementStart;  // let cursor point to element's start
            unmarshal(&iter_dict, *localKeySignature, item, &localItemM); // process

            // add to directory
#ifdef DEBUG_DICT
    fprintf(stderr, "     unmarshalDict() - about to add dict entry: key=[%s] -> item=[%s] ...\n",
                          pmarsh->context->ObjectToStringValue(pmarsh->context->ArrayAt(key,1)),
                          pmarsh->context->ObjectToStringValue((pmarsh->context->ArrayItems(item)==1 ?
                                                  pmarsh->context->ArrayAt(item,1) :
                                                  item )
                                                               )
                          );
    fflush(stderr);
#endif

            pmarsh->context->SendMessage2(rxMap, "PUT",
                                         (pmarsh->context->ArrayItems(item)==1 ?
                                                  pmarsh->context->ArrayAt(item,1) :
                                                  item
                                                  ),
                                          pmarsh->context->ArrayAt(key,1)
                                          );
        }

        dbus_message_iter_next(iter_array);      // move to next element
    }


#ifdef MALLOC_USE_REXX
    RexxFreeMemory(localKeySignature);
    RexxFreeMemory(localItemSignature);
#else
    dbus_free(localKeySignature);
    dbus_free(localItemSignature);
#endif

#ifdef DEBUG_DICT
   fprintf(stderr, "<--- leaving unmarshalDict(...).\n"); fflush(stderr);
#endif

    return true;
}


// ---------------------------------------------------------------------------------------------------------
// unmarshalStruct()
// - expects: elementStart to point at one after opening '(' and elementEnd at one before closing ')' !
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
// - expects: elementStart to point at one after opening '(' and elementEnd at one before closing ')' !
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
logical_t marshal(DBusMessageIter *iter, char typeCode, RexxObjectPtr rop, PSTRUCT_MARSHAL pmarsh)
{
#ifdef DEBUG_MARSHALLING
    // fprintf(stderr, "---> arrived in marshal(...), typeCode=[%c], cursor=[%zu], signature=[%s], iter=[%p] \n",
    fprintf(stderr, "---> arrived in marshal(...), typeCode=[%c], cursor=[%lu], signature=[%s], iter=[%p] \n",
                       typeCode,
                       (unsigned long) (pmarsh->cursor - pmarsh->signature),
                       pmarsh->signature,
                       iter
                       );
#endif

    int       resDbus=1;            // "0": dbus-error (out of memory)
    logical_t orxConvFlag=true;

    logical_t bRopIsNull= (rop==NULLOBJECT || (pmarsh->context->Nil()==rop)) ;  // a Rexx .nil, resp. NULLOBJECT in hands ?
    pmarsh->errorPosInSignature=pmarsh->cursor; // memorize cursor, in case we get an error

#ifdef DEBUG_MARSHALLING_2
    fprintf(stderr, "        marshal() - before switch, typeCode=[%c], bRopIsNull=[%d]\n", typeCode, (int) bRopIsNull);
#endif

    switch (typeCode)
    {
    case 's' :      // STRING (UTF-8): must not contain nul bytes, but must end wiht a nul byte
        {
            if (bRopIsNull)
            {
                CSTRING cstr="";
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_STRING, &cstr);
#ifdef DEBUG_MARSHALLING
   fprintf(stderr, "        marshall() - 's' - 1 (NULL) - typeCode=[%c], string value=[%s]\n", typeCode, cstr);
#endif
            }
            else
            {
                CSTRING   cstr=pmarsh->context->ObjectToStringValue(rop);
#ifdef DEBUG_MARSHALLING
   fprintf(stderr, "        marshall() - 's' - 2 (rop!) - typeCode=[%c], string value=[%s]\n", typeCode, cstr);
#endif
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_STRING, &cstr);
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;

    case 'g' :      // SIGNATURE
        {
#ifdef DEBUG_MARSHALLING_2
            fprintf(stderr, "        marshall() - 'g' - 1 \n");
#endif
            if (bRopIsNull)
            {
#ifdef DEBUG_MARSHALLING_2
            fprintf(stderr, "        marshall() - 'g' - 1a \n");
#endif
                CSTRING cstr="";
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_SIGNATURE, &cstr);
            }
            else
            {
#ifdef DEBUG_MARSHALLING_2
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
#ifdef DEBUG_MARSHALLING_2
            fprintf(stderr, "        marshall() - 'o' - 1 \n");
#endif
            if (bRopIsNull)
            {
#ifdef DEBUG_MARSHALLING_2
            fprintf(stderr, "        marshall() - 'o' - 1a \n");
#endif
                CSTRING cstr="/";
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_OBJECT_PATH, &cstr);
            }
            else
            {
#ifdef DEBUG_MARSHALLING_2
            fprintf(stderr, "        marshall() - 'o' - 1b \n");
#endif
                CSTRING   cstr=pmarsh->context->ObjectToStringValue(rop);
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_OBJECT_PATH, &cstr);
            }
            pmarsh->cursor++;       // let us point to the next typeCode if any
        }
        break;

    case 'y' :      // BYTE
        {
#ifdef DEBUG_MARSHALLING_2
            fprintf(stderr, "        marshall() - 'y'  \n");
#endif
            if (bRopIsNull)
            {
#ifdef DEBUG_MARSHALLING_2
            fprintf(stderr, "        marshall() - 'y' - 1a \n");
#endif
                char ch='\0';
                resDbus=dbus_message_iter_append_basic(iter, DBUS_TYPE_BYTE, &ch);
            }
            else
            {
#ifdef DEBUG_MARSHALLING_2
            fprintf(stderr, "        marshall() - 'y' - 1b \n");
#endif
                stringsize_t n;
                // if (!pmarsh->context->IsString(rop))
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
                     (int) pmarsh->context->IsArray(rop),
                     rop,
                     (rop==NULL ? NULL : pmarsh->context->ObjectToStringValue(rop) )
                     );
    fflush(stderr);

        // assume array object was supplied for marshalling
    if (pmarsh->context->IsArray(rop))
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

        fprintf(stderr, "\n");
    }
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


    // fprintf(stderr, "     /--> signature=[%s], cursorPos=[%zu|%c]: arrStartPos=[%zu|%c], arrEndPos=[%zu|%c], arrLen=[%zu] | elStartPos=[%zu|%c], elEndPos=[%zu|%c], elLen=[%zu]\n",
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

            if (bRopIsNull) // o.k. Rexx argument not available, supply another empty Rexx array as argument array
            {
#ifdef DEBUG_ARRAY
    fprintf(stderr, "     marshal(...), 'a': bRopIsNull");
#endif
                if (*pmarsh->elementStart!='{')     // make sure not a DICT expected!
                {
                    rao=pmarsh->context->NewArray(0);
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

    // fprintf(stderr, "     /<-- signature=[%s], cursorPos=[%zu|%c]: end-of-array-processing #2. \n",
    fprintf(stderr, "     /<-- signature=[%s], cursorPos=[%lu|%c]: end-of-array-processing #2. \n",
            pmarsh->signature,  (unsigned long) cursorPos, *(pmarsh->signature+cursorPos));
    }
#endif

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
                }
                else        // raise error
                {
                    orxConvFlag=false;
                    SNPRINTF(pmarsh->msgChunk256, 256, ": cannot convert supplied argument to a Rexx array object containing the structure's elements");
                    break;
                }
            }
            // logical_t res=
            marshalStruct(iter, rao, pmarsh);

                // move cursor to next char in signature
            pmarsh->cursor=pmarsh->elementEnd+2;       // let us point to the next typeCode if any
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


            if (bRopIsNull) // o.k. Rexx argument not available, supply another empty Rexx array as argument array
            {
#ifdef DEBUG_DICT
    fprintf(stderr, "     marshal(): DICT - bRopIsNull, creating an empty Directory !\n");
#endif
               rxMap=pmarsh->context->NewDirectory();
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
        }
        break;

    case '}' :      // DICT_ENTRY - end - end (should never get here!
fprintf(stderr, "%s: *** marshal(...): DICT ENTRY - end: marshal() - ERROR !! SHOULD NEVER ARRIVE HERE, please report!\n", DLLNAME);
        break;

    // case '\0':      // INVALID (end-marker)
    default:
fprintf(stderr, "%s: *** marshal(...): ??? default-branch ??? typeCode=[%c], signature=[%s]: SHOULD NEVER ARRIVE HERE, please report!\n",
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
logical_t marshalArray(DBusMessageIter *iter, size_t level, RexxObjectPtr anyRop, RexxArrayObject arrIdx, PSTRUCT_MARSHAL pmarsh)
{
#ifdef DEBUG_ARRAY
   // fprintf(stderr, "---> arrived in marshalArray(...): signature=[%s] - cursor=[%zu], arrStart=[%zu], arrEnd=[%zu], elStart=[%zu], elEnd=[%zu]\n",
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
    if (pmarsh->context->IsArray(anyRop))   // an array object (could be a MapCollection for processing a DICT
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
    // fprintf(stderr, "     - level=[%zu], localSignature=[%s], anyRop=[%p]=[%s], p->dimensions=[%zu] <-> arrIdx=[%s]\n",
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
            // if (!marshalDict(&iter_array, rao, &localM))    // process DICTs
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
                }
            }
            else        // rao==NULL !
            {
                rso=pmarsh->context->NullString();     // empty string
            }

            size_t  stringLength=pmarsh->context->StringLength(rso);  // get length of the Rexx string, which may contain NULLs
            CSTRING stringValue =pmarsh->context->CString(rso);  // get a CSTRING

#ifdef DEBUG_ARRAY
    // fprintf(stderr, "       ... branch # 1b: level==pmarsh->dimensions, a STRING in hand, size=[%zu], value=[%.64s]!\n",
    fprintf(stderr, "       ... branch # 1b: level==pmarsh->dimensions, a STRING in hand, size=[%lu], value=[%.64s]!\n",
                                    (unsigned long) stringLength, stringValue);
#endif

            if (!dbus_message_iter_append_fixed_array(&iter_array, DBUS_TYPE_BYTE, &stringValue, stringLength ))
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
        // fprintf(stderr, "       ... branch # 2: level==pmarsh->dimensions == [%zu], neededRexxArgs=[%zu]\n",
        fprintf(stderr, "       ... branch # 2: level==pmarsh->dimensions == [%lu], neededRexxArgs=[%lu]\n",
                        (unsigned long) level, (unsigned long) neededRexxArgs);
#endif

                // process all array items
            size_t items=GET_ARRAY_SIZE_AT_DIMENSION(rao, level, pmarsh->context);

#ifdef DEBUG_ARRAY
       // fprintf(stderr, "       ...ma(...): rao=[%s], has rao-size/items at level=[%zu]/dim|items: [%zu] | arrIdx=[%s]\n",
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
    // fprintf(stderr, "          marshalArray(...): processing array (level=[%zu]) item # i=[%zu]/items=[%zu]\n", level, i, items);
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
       // fprintf(stderr, "          - dims=[%zu], items=[%zu], size=[%zu] \n",
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
                    rop=pmarsh->context->ObjectToString(rop);
                    size_t  stringLength=pmarsh->context->StringLength((RexxStringObject) rop);  // get length of the Rexx string, which may contain NULLs
                    CSTRING stringValue =pmarsh->context->CString(rop);  // get a CSTRING

#ifdef DEBUG_ARRAY
            // fprintf(stderr, "       ... branch # 2:  BYTE-ARRAY / level==pmarsh->dimensions, a STRING in hand, stringLength=[%zu], stringValue=[%.64s]!\n",
            fprintf(stderr, "       ... branch # 2:  BYTE-ARRAY / level==pmarsh->dimensions, a STRING in hand, stringLength=[%lu], stringValue=[%.64s]!\n",
                                            (unsigned long) stringLength, stringValue);
        if (pmarsh->context->IsArray(rop))
        {
            fprintf(stderr, "       ... branch # 2b: BYTE-ARRAY / an array in hand, value=[%.64s]!\n",
                            ARR_AS_STRING( (RexxArrayObject) rop , pmarsh->context) );
        }

#endif

                    if (!dbus_message_iter_append_fixed_array(&iter_array, DBUS_TYPE_BYTE, &stringValue, stringLength ))
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
                        return false;
                    }

                    for (size_t idx=1; idx<=neededRexxArgs; idx++)   // iterate over signature's types
                    {
                        localM.cursor=localM.signature;       // reset cursor, such that each array element is processed with the same local signature
                        RexxObjectPtr currRop=pmarsh->context->ArrayAt(rao, idx);       // get array's entry

#ifdef DEBUG_ARRAY
        // fprintf(stderr, "    marshalArray(...): processing array item # [%zu]/[%zu] | subelement idx=[%zu]/[%zu] single type=[%c], value=[%s] ***\n",
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
                            return false;
                        }
                    }
                }
                else
                {
#ifdef DEBUG_ARRAY
    // fprintf(stderr, "    marshalArray(...): neededRexxArgs<=1: processing level=[%zu], array item # i=[%zu]/items=[%zu]: single type=[%c]; arrIdx=[%s]: value=[%s] §§§\n",
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
                        return false;
                    }
                }
            }
        }
    }

    else        // recurse into next dimension of array
    {
#ifdef DEBUG_ARRAY
    fprintf(stderr, "       ... branch: recursing into next level \n");
#endif

        logical_t bRetValue=false;
        size_t items=GET_ARRAY_SIZE_AT_DIMENSION(rao, level, pmarsh->context);
        if (items==0)   // e.g. if .nil was supplied; we still need to create the array containers, even if they remain empty
        {
            items=1;
        }

        for (size_t i=1; i<=items; i++)
        {
            // set this dimension's index appropriately
            SET_ARRAY_IDX(arrIdx, level, i, pmarsh->context);

#ifdef DEBUG_ARRAY
    // fprintf(stderr, "    ...ma(...): recursing with level=[%zu] with [%zu] items, arrIdx=[%s]\n",  level, items, ARR_AS_STRING( arrIdx , pmarsh->context));
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
logical_t marshalDict(DBusMessageIter *iter, RexxObjectPtr rxMap, PSTRUCT_MARSHAL pmarsh)
{

#ifdef DEBUG_DICT
   fprintf(stderr, "---> arrived in marshalDict(...), rxMap=[%p], signature=[%s] \n", rxMap, pmarsh->signature);
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

#ifdef DEBUG_DICT
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
        localM.cursor=localM.elementStart;  // reset to start of respective signature

#ifdef DEBUG_DICT_2
   fprintf(stderr, "     marshalDict(...): OPEN container ---> ---> ---> local signature=[%s]\n", localSignature);
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
            return false;
        }

            // process index
        if (!marshal(&iter_dict, *localM.cursor, pmarsh->context->SupplierIndex(rso), &localM))   // an error has occurred !
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

            // process item
        if (!marshal(&iter_dict, *localM.cursor, pmarsh->context->SupplierItem(rso), &localM))   // an error has occurred !
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
    return true;
}


// ---------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------
// marshalStruct()
// - expects: elementStart to point at one after opening '(' and elementEnd at one before closing ')' !
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

        // use signature withing parenthesis
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
   // fprintf(stderr, "     success: done idx=[%zu], localM.cursor=[%s] ...\n", idx, localM.cursor);
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
logical_t marshalVariant(DBusMessageIter *iter, RexxObjectPtr rop, PSTRUCT_MARSHAL pmarsh)
{

#ifdef DEBUG_VARIANT
   fprintf(stderr, "---> arrived in marshalVariant(...), rop=[%p] \n", rop);
#endif

    DBusMessageIter iter_variant;

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
            // then use string immediately following '=' as an explicit variang signature,
            // and remaining array element as the return value; this should allow to deal
            // with infrastructures like "telepathy" which expect some variant dict entries
            // to be encoded strictly with predefined types (tsk!)
            CSTRING fel= pmarsh->context->ObjectToStringValue(pmarsh->context->ArrayAt(rao, 1));

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
    return true;
}







// -----------------------------------------------------------------------------------------------------
// --------------------------------- helper function ---------------------------
// append arguments onto msg args
// let the helper function do the work (and raise an error, if necessary)
RexxArrayObject helperUnmarshalMessageArgs( DBusMessage  *dbusMsg,       // DBUS message
                                       RexxThreadContext *context,       // allow callee to raise condition
                                       logical_t          argUnmarshalAyAsString,   // connection's attribute "unmarshalByteArrayAsString"
                                       CSTRING            errorConditionText // e.g.: "%.16s/method/DbusBusSignalMessage(), error 2: %s"    // base condition message
                                      )
{
#ifdef DEBUG_FUNCTIONS
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

    // pmarsh->unmarshal_ay_as_string=pmarsh->context->GetObjectVariable("UNMARSHALBYTEARRAYASSTRING")==pmarsh->context->True();
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
        return NULLOBJECT ;     // return prematurely
    }

#ifdef DEBUG_UNMARSHAL
    // fprintf(stderr, "\n ...  helperUnmarshalMessageArgs(...): unmarshalling SUCCESS, RxResult=[%p], dims=[%zu], items=[%zu], size=[%zu] !\n",
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
        // fprintf(stderr, ", dims=[%zu], items=[%zu], size=[%zu]",
        fprintf(stderr, ", dims=[%lu], items=[%lu], size=[%lu]",
                           (unsigned long) context->ArrayDimension((RexxArrayObject) tmpO),
                           (unsigned long) context->ArrayItems    ((RexxArrayObject) tmpO),
                           (unsigned long) context->ArraySize     ((RexxArrayObject) tmpO) );
    }
    fprintf(stderr, "\n");
    fflush(stderr);
#endif

#ifdef DEBUG_FUNCTIONS
    fprintf(stderr, "<=== about to return from helperUnmarshalMessageArgs(): rxResult ! \n");
#endif

    return rxResult;            // return unmarshalled return arguments
}



// -----------------------------------------------------------------------------------------------------
    // append arguments onto msg args
        // let the helper function do the work (and raise an error, if necessary)
void helperMarshalMessageArgs( DBusMessage      *dbusMsg,       // DBUS message
                              DBusMessageIter   *iter,          // DBUS args pointer
                              CSTRING            signature,     // signature for arguments, if any
                              size_t             neededNumberOfRexxArgs,    // number of Rexx arguments to process matching signature
                              RexxArrayObject    rexxArgs,      // rexx arguments
                              RexxThreadContext *context,       // allow callee to raise condition
                              CSTRING            errorConditionText // e.g.: "%.16s/method/DbusBusSignalMessage(), error 2: %s"    // base condition message
                              )
{
#ifdef DEBUG_FUNCTIONS
   fprintf(stderr, "---> arrived in helperMarshalMessageArgs(...), signature=[%s], rexxArgs=[%p] \n", signature, rexxArgs);

   if (context->IsArray(rexxArgs))
   {
       // fprintf(stderr, "     rexxArgs=[%p], size=[%zu], items=[%zu]\n",
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

#ifdef DEBUG_FUNCTIONS
    // fprintf(stderr, " ...  helperMarshalMessageArgs(...):  rai=%zu/%zu, typeCode=[%c], rop=[%p]=[%s]\n",
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
            context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msgChunk3));
            return;     // return prematurely
        }
    }
}


// -----------------------------------------------------------------------
// adds some information about the message
void helperCreateSlotDirEntries(RexxThreadContext *context, RexxDirectoryObject slotDir, DBusMessage *dbusMsg)
{
    const char   *str=NULL;
    RexxObjectPtr rop=NULL;

    context->SendMessage2(slotDir, "SETENTRY", context->CString("AUTOSTART"),
                            (dbus_message_get_auto_start(dbusMsg) == TRUE ? context->True() : context->False()) );

    str=dbus_message_get_destination(dbusMsg);
    rop=(str==NULL ? context->NullString() : context->CString(str));
    context->SendMessage2(slotDir, "SETENTRY", context->CString("DESTINATION"),  rop );

    RexxClassObject dtClz=context->FindClass("DATETIME");       // get the DateTime class
    RexxObjectPtr   dt   =context->SendMessage0(dtClz, "NEW");  // get an instance
    context->SendMessage2(slotDir, "SETENTRY", context->CString("DATETIME"),
                                   dt);
    //                      GET_REXX_DATE_TIME_AS_REXX_STRING(context) );



    str=dbus_message_get_interface(dbusMsg);
    rop=(str==NULL ? context->NullString() : context->CString(str));
    context->SendMessage2(slotDir, "SETENTRY", context->CString("INTERFACE"),    rop );

    str=dbus_message_get_member(dbusMsg);
    rop=(str==NULL ? context->NullString() : context->CString(str));
    context->SendMessage2(slotDir, "SETENTRY", context->CString("MEMBER"),       rop );

    int msgType=dbus_message_get_type(dbusMsg);
    context->SendMessage2(slotDir, "SETENTRY", context->CString("MESSAGETYPE"),  context->Int32ToObject(msgType) );

    str=dbus_message_type_to_string(msgType);
    rop=(str==NULL ? context->NullString() : context->CString(str));
    context->SendMessage2(slotDir, "SETENTRY", context->CString("MESSAGETYPENAME"), rop );

    context->SendMessage2(slotDir, "SETENTRY", context->CString("NOREPLY"),
                          (dbus_message_get_no_reply(dbusMsg) == TRUE ? context->True() : context->False()) );

    str=dbus_message_get_path(dbusMsg);
    rop=(str==NULL ? context->NullString() : context->CString(str));
    context->SendMessage2(slotDir, "SETENTRY", context->CString("OBJECTPATH"),   rop );

    str=dbus_message_get_sender(dbusMsg);
    rop=(str==NULL ? context->NullString() : context->CString(str));
    context->SendMessage2(slotDir, "SETENTRY", context->CString("SENDER"),       rop );

    context->SendMessage2(slotDir, "SETENTRY", context->CString("SERIAL"),       context->UnsignedInt32ToObject(dbus_message_get_serial(dbusMsg)) );

    str=dbus_message_get_signature(dbusMsg);
    rop=(str==NULL ? context->NullString() : context->CString(str));
    context->SendMessage2(slotDir, "SETENTRY", context->CString("SIGNATURE"),    rop );
}



// -----------------------------------------------------------------------------------------------------
    // ============================================================================= //
    // set up and run a message loop for this connection instance
    // processes signals and method calls to registered ooRexx server objects
    // wrapped up as a Rexx method, hence easy to run it on a separate thread
RexxMethod2(RexxObjectPtr, DbusMessageLoop, CSELF, connPtr, OSELF, self)
{
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "... MessageLoop(): just arrived ! ...\n"); fflush(stderr);
#endif

    context->SetGuardOn();
    if (context->GetObjectVariable("active") == context->True())     // already running ?
    {
        context->SetGuardOff();
        return NULLOBJECT;      // return, do not start another message loop thread
    }


        // values we might need
    RexxObjectPtr rxTrue =context->True();
    RexxObjectPtr rxFalse=context->False();
    RexxObjectPtr rxNil  =context->Nil();

        // determine, whether this is a connection to a private Rexx server running in this instance
    RexxObjectPtr tmpRop=context->GetObjectVariable("server");
    bool bClientForPrivateRexxServer= (tmpRop!=NULL && tmpRop!=rxNil);

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
    context->SetObjectVariable("ACTIVE", context->True());  // indicate message processing is active
    context->SetGuardOff();

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "... MessageLoop(): about to enter message loop, changed instance variable 'active' to .true: [%s], self=[%s] self~identityHash=[%s] ...\n",
                    context->ObjectToStringValue(context->GetObjectVariable("active")),
                    context->ObjectToStringValue(self),
                    context->ObjectToStringValue(context->SendMessage0(self,"IDENTITYHASH"))
            ); fflush(stderr);
    fprintf(stderr, "... MessageLoop(): conn=[%p], connPtr=[%p] ...\n", conn, connPtr); fflush(stderr);

    size_t count=0, limit=10000/milliTimeout;
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

        if  (context->CheckCondition()) // is this thread affected by a Halt-condition? If so, leave
        {
            context->SetGuardOn();
            context->SetObjectVariable("ACTIVE",rxFalse);
            context->SetGuardOff();
            break;
        }

        logical_t bLeave=(context->GetObjectVariable("active") == rxFalse);

        if (bLeave)
        {
            break;
        }

#ifdef DEBUG_MESSAGELOOP
        if (++count>=limit)
        {
            // fprintf(stderr, "... MessageLoop(): looped [%zu] times...\n", limit); fflush(stderr);
            fprintf(stderr, "... MessageLoop(): looped [%lu] times...\n", (unsigned long) limit); fflush(stderr);
            count=0;
        }
#endif

        dbusMsg=dbus_connection_pop_message(conn);     // segmentation fault on Ubuntu DBus 1.4.6 ! (2011-07-28)

        if (dbusMsg==NULL)      // no message retrieved
        {
            continue;
        }

        int msgType=dbus_message_get_type(dbusMsg);

#if defined (DEBUG_RGF)     // show all messages we receive on this connection, look for the "Disconnected" signal
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
/*
    if (   tmpInterface!=NULL && tmpMember!= NULL
        && !strncmp("org.freedesktop.DBus.Local", tmpInterface, 26) // a client on a private server is disconnected
        && !strncmp("Disconnected",         tmpMember,    12)
    )
*/

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
            continue;
        }

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message: type=[%d] (%s), path=[%s], interface=[%s], member=[%s] | sender=[%s] -> destination=[%s] | signature=[%s] ...\n",
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
                continue;
            }

        }
        else    // received a message call, but if no registered servers, we do nothing ...
        {
            RexxObjectPtr rop=context->SendMessage0(serviceObjects, "ITEMS");
            size_t    n=0;
            logical_t flag=false;
            flag = context->ObjectToStringSize(rop, &n);

            if (flag==false || n==0)
            {
#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a METHOD_CALL, but no server objects registered ...\n"); fflush(stderr);
#endif
                dbus_message_unref(dbusMsg);    // free the message in hand
                continue;
            }
        }

            // create the slotDir argument, supply the message's interesting information with it
        RexxDirectoryObject slotDir=NULL;

        bool bMakeSlotDir      =context->GetObjectVariable("MAKESLOTDIR")==rxTrue;
        bool bCollectStatistics=context->GetObjectVariable("COLLECTSTATISTICS")==rxTrue;

        if ( bMakeSlotDir || bCollectStatistics) // should we create a slotDir-argument containing message related information?
        {
            slotDir=context->NewDirectory();
            helperCreateSlotDirEntries(context->threadContext, slotDir, dbusMsg);  // adds some information about the message
        }

            // unmarshal arguments
        RexxArrayObject arrArgs=NULL;
        // {
        arrArgs=helperUnmarshalMessageArgs(dbusMsg,
                       context->threadContext,
                       (context->GetObjectVariable("UNMARSHALBYTEARRAYASSTRING")==context->True()),
                       "%.16s/method/DbusMessageLoop(), error 99: %s"    // base condition message
                      );

        if (arrArgs==NULL)  // make sure we have at least an empty array
        {
            arrArgs=context->NewArray(0);
        }
        // }

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
                    if (context->SendMessage1(rxTmp, "equals", rxInterface)!=rxTrue)
                    {
                        continue;
                    }
                }

                rxTmp=context->SendMessage0(rop, "SIGNALNAME");   // get interface
                if (rxTmp!=rxNil)   // given, hence must match!
                {
                    if (context->SendMessage1(rxTmp, "equals", rxMember)!=rxTrue)
                    {
                        continue;
                    }
                }

                // asynchroneously send signal to Rexx listener
                rxTmp=context->SendMessage0(rop, "LISTENEROBJECT");   // get interface

                context->ArrayPut(arr2, rxMember, 1);
                context->ArrayPut(arr2, arrArgs,  2);



#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a SIGNAL, dispatching to Rexx asynchroneously ...\n"); fflush(stderr);
#endif

                if (bCollectStatistics)     // determined at top of message loop, once it's clear we need to handle the message
                {
                    RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");

                    CSTRING sentKind ="RECEIVED";
                    CSTRING kind     ="LASTRECEIVEDSIGNAL";
                    CSTRING countKind="COUNTRECEIVEDSIGNALS";
                    context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available
                    context->DirectoryPut(rdo,context->DirectoryAt(slotDir,"DATETIME"),sentKind);   // adjust "RECEIVED" datetime-stamp
                        // increase kind's count
                    RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                    count=context->SendMessage1(count, "+", context->Int32ToObject(1));
                    context->DirectoryPut(rdo,count,countKind);
                }

                // dispatch asynchroneously (no result expected)
                context->SendMessage(rxTmp, "STARTWITH", arr2);   // one-way call, hence not interested in any results
            }
            context->SetGuardOff();     // release exclusive access to object variables/attributes


                // if a Disconnect signal on a server client connection, then send a "disconnect"
                // message to the server supplying this connection as an argument
            if (bClientForPrivateRexxServer)
            {
/*
                if (   interface!=NULL && member!= NULL
                    && !strncmp("org.freedesktop.DBus.Local", interface, 26) // a client on a private server is disconnected
                    && !strncmp("Disconnected",               member,    12)
                )
*/
                if (dbus_message_is_signal(dbusMsg,DBUS_INTERFACE_LOCAL,"Disconnected") )
                {
#if defined (DEBUG_RGF)     // debug private server connection to client
    fprintf(stderr, "---- BINGO BINGO #2 - DISCONNECT received: interfaceName=[%s], member=[%s]\n",interfaceName, member);
#endif
                        // send DISCONNECT message to server, supplying this connection instance as an argument
                    context->SendMessage1(context->SendMessage0(self, "SERVER"), "DISCONNECT", self);
                }
            }
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
                    helperCreateSlotDirEntries(context->threadContext, slotDir, errReply);   // setup slotDir
                    CSTRING errName=dbus_message_get_error_name(errReply);
                    if (errName!=NULL)
                    {
                        context->DirectoryPut(slotDir,context->String(errName),"ERRORNAME");
                    }

                    CSTRING sentKind ="SENT";
                    CSTRING kind     ="LASTSENTERROR";
                    CSTRING countKind="COUNTSENTERRORS";
                    context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available
                    context->DirectoryPut(rdo,context->DirectoryAt(slotDir,"DATETIME"),sentKind);   // adjust "RECEIVED" datetime-stamp
                        // increase kind's count
                    RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                    count=context->SendMessage1(count, "+", context->Int32ToObject(1));
                    context->DirectoryPut(rdo,count,countKind);
                    context->SetGuardOff();
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
                    context->DirectoryPut(rdo,context->DirectoryAt(slotDir,"DATETIME"),sentKind);   // adjust "RECEIVED" datetime-stamp
                        // increase kind's count
                    RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                    count=context->SendMessage1(count, "+", context->Int32ToObject(1));

                    context->DirectoryPut(rdo,count,countKind);
                    context->SetGuardOff();
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
                            helperCreateSlotDirEntries(context->threadContext, slotDir, returnMsg);   // setup slotDir

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
                         }
                         dbus_connection_flush(conn);

                         if (bCollectStatistics)     // determined at top of message loop, once it's clear we need to handle the message
                         {
                             context->SetGuardOn();          // update statistics on send signal
                             RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");
                             RexxDirectoryObject slotDir=context->NewDirectory();
                             helperCreateSlotDirEntries(context->threadContext, slotDir, errReply);   // setup slotDir
                             CSTRING errName=dbus_message_get_error_name(errReply);
                             if (errName!=NULL)
                             {
                                 context->DirectoryPut(slotDir,context->String(errName),"ERRORNAME");
                             }

                             CSTRING sentKind ="SENT";
                             CSTRING kind     ="LASTSENTERROR";
                             CSTRING countKind="COUNTSENTERRORS";
                             context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available
                             context->DirectoryPut(rdo,context->DirectoryAt(slotDir,"DATETIME"),sentKind);   // adjust "RECEIVED" datetime-stamp
                                 // increase kind's count
                             RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                             count=context->SendMessage1(count, "+", context->Int32ToObject(1));
                             context->DirectoryPut(rdo,count,countKind);
                             context->SetGuardOff();
                         }

                         dbus_message_unref(errReply);

                         if (!bSpecialReply)    // an unexpected Rexx condition got raised
                         {
                             // context->ClearCondition();     // let the service object continue to run
                             // otherwise: stop message loop, such that Rexx program can fully stop
                             // context->SetObjectVariable("active", rxFalse);
                             context->SetGuardOn();
                             context->SetObjectVariable("ACTIVE", rxFalse);  // stop message loop thread
                             context->SetGuardOff();
                         }
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
                             helperCreateSlotDirEntries(context->threadContext, slotDir, errReply);   // setup slotDir
                             CSTRING errName=dbus_message_get_error_name(errReply);
                             if (errName!=NULL)
                             {
                                 context->DirectoryPut(slotDir,context->String(errName),"ERRORNAME");
                             }

                             CSTRING sentKind ="SENT";
                             CSTRING kind     ="LASTSENTERROR";
                             CSTRING countKind="COUNTSENTERRORS";
                             context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available
                             context->DirectoryPut(rdo,context->DirectoryAt(slotDir,"DATETIME"),sentKind);   // adjust "RECEIVED" datetime-stamp
                                 // increase kind's count
                             RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                             count=context->SendMessage1(count, "+", context->Int32ToObject(1));
                             context->DirectoryPut(rdo,count,countKind);
                             context->SetGuardOff();
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
                            CSTRING fel= context->ObjectToStringValue(context->ArrayAt((RexxArrayObject) rxResult, 1));

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
                                                arrRxResult=context->ArrayOfOne(rxResult);
                                            }
                                        }
                                        else
                                        {
                                            arrRxResult=context->ArrayOfOne(rxResult);
                                        }

                                    }
                                }
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
                                   dbus_error_free(&err);
                                   context->SetObjectVariable("active", context->False()); // indicate message processing has stopped
                                   context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
                                   return NULLOBJECT;
                                }
                                dbus_error_free(&err);      // not needed anymore

                                SNPRINTF(signature, 256, "%s", tmpSignature);     // accept the signature
                            }

                              // determine hwo rxResult is to be sent for marshalling
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
                                    arrRxResult=context->ArrayOfOne(rxResult);
                                }
                            }
                            else
                            {
                                arrRxResult=context->ArrayOfOne(rxResult);
                            }
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
                                    helperCreateSlotDirEntries(context->threadContext, slotDir, errReply);   // setup slotDir
                                    CSTRING errName=dbus_message_get_error_name(errReply);
                                    if (errName!=NULL)
                                    {
                                        context->DirectoryPut(slotDir,context->String(errName),"ERRORNAME");
                                    }

                                    CSTRING sentKind ="SENT";
                                    CSTRING kind     ="LASTSENTERROR";
                                    CSTRING countKind="COUNTSENTERRORS";
                                    context->DirectoryPut(rdo,slotDir,kind);     // make error messages slotDir available
                                    context->DirectoryPut(rdo,context->DirectoryAt(slotDir,"DATETIME"),sentKind);   // adjust "RECEIVED" datetime-stamp
                                        // increase kind's count
                                    RexxObjectPtr count=context->DirectoryAt(rdo,countKind);
                                    count=context->SendMessage1(count, "+", context->Int32ToObject(1));
                                    context->DirectoryPut(rdo,count,countKind);
                                    context->SetGuardOff();
                                }

                                dbus_message_unref(errReply);

                                // -----------------------------------------
                                // now raise condition in Rexx
                                dbus_message_unref(dbusMsg);    // make sure we free the message in hand

                                    // Rexx: try to raise a syntax exception
                                char msg[1024]="";     // buffer for error message

                                    /* raise exception */
                                SNPRINTF( msg, 1024, "%.16s/DbusMessageLoop, panic! Unexpected marshalling error for return value [%.64s] with signature [%.256s] by service object [%s] after returning from Rexx method [%s] (interfaceName=[%s])",
                                                 DLLNAME,
                                                 context->ObjectToStringValue(rxResult),
                                                 signature,
                                                 objectPath,
                                                 member,
                                                 interfaceName
                                                 );

                                context->SetObjectVariable("active", context->False()); // indicate message processing has stopped
                                context->RaiseException1(Rexx_Error_Incorrect_method_user_defined, context->String(msg));
                                return NULLOBJECT;
                            }
                        }


#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "    ... message is a CALL, message [%s] (interfaceName=[%s]) to service object for [%s]: ***SUCCESS*** (RETURN MESSAGE GOT SUCCESSFULLY SENT)!\n", member, interfaceName, objectPath); fflush(stderr);
#endif
                    }
                }
            }
        }

        dbus_message_unref(dbusMsg);    // free the message in hand
    }

    // TODO: drain messages in incoming queue !? Question was not answered on DBus mailing list, hence not doing it

    context->SetObjectVariable("active", context->False()); // indicate message processing has stopped

#ifdef DEBUG_MESSAGELOOP
    fprintf(stderr, "... MessageLoop():  left message loop, changed instance variable 'active' to .false ...\n"); fflush(stderr);
#endif

    return NULLOBJECT;
}



// -----------------------------------------------------------------------------------------------------
    // ============================================================================= //
    // send a signal message over the bus (a broadcast message)
RexxMethod6(RexxObjectPtr, DbusBusSignalMessage, CSTRING,         senderObjectPath,
                                                 CSTRING,         signalInterfaceName,
                                                 CSTRING,         signalName,
                                                 CSTRING,         signature,
                                                 RexxArrayObject, rexxArgs,
                                                 CSELF,           connPtr)
{
    // check Rexx arguments first
    size_t items = (rexxArgs==NULLOBJECT ? 0 : context->ArraySize(rexxArgs)) ;
    size_t sigLen=strlen(signature);

#if defined (DEBUG_METHODS)
   // fprintf(stderr, "===> arrived in DbusBusSignalMessage: rao=[%p], items=[%zu], signature=[%s]\n", rexxArgs, items, signature);fflush(stderr);
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
fprintf(stderr, "... sending DbusBusSignalMessage(): dbusMsg=[%p] - [%s], [%s], [%s] ... \n",
                dbusMsg, senderObjectPath, signalInterfaceName, signalName);

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
        helperCreateSlotDirEntries(context->threadContext, slotDir, dbusMsg);   // setup slotDir

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
RexxMethod8(RexxObjectPtr, DbusBusCallMessage, CSTRING,         busName,
                                               CSTRING,         targetObjectPath,
                                               CSTRING,         callInterfaceName,
                                               CSTRING,         callMemberName,
                                               CSTRING,         replySignature,     // if empty/NULL, do not wait on reply (ignore it)
                                               CSTRING,         argSignature,
                                               RexxArrayObject, rexxArgs,
                                               CSELF,           connPtr)

{
    // check Rexx arguments first
    size_t items = (rexxArgs==NULLOBJECT ? 0 : context->ArraySize(rexxArgs)) ;
    size_t argSignatureLength=strlen(argSignature);

#if defined (DEBUG_METHODS)
   // fprintf(stderr, "===> arrived in DbusBusCallMessage: rao=[%p], items=[%zu], argSignature=[%s] | replySignature=[%s]\n", rexxArgs, items, argSignature, replySignature);
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


        // if (context->GetObjectVariable("COLLECTSTATISTICS")==context->True())
        if (bCollectStatistics)         // allows us to guard on only, if statistics should be collected !
        {
            context->SetGuardOn();          // update statistics on call method
            RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");
            RexxDirectoryObject slotDir=context->NewDirectory();
            helperCreateSlotDirEntries(context->threadContext, slotDir, dbusMsg);   // setup slotDir

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

        // if (context->GetObjectVariable("COLLECTSTATISTICS")==context->True())
        if (bCollectStatistics)         // allows us to guard on only, if statistics should be collected !
        {
            context->SetGuardOn();          // update statistics on call method
            RexxDirectoryObject rdo=(RexxDirectoryObject) context->GetObjectVariable("STATISTICS");
            RexxDirectoryObject slotDir=context->NewDirectory();
            helperCreateSlotDirEntries(context->threadContext, slotDir, dbusMsg);   // setup slotDir

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
            helperCreateSlotDirEntries(context->threadContext, slotDir, replyMsg);  // adds some information about the message
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
        // close a private connection
RexxMethod1(RexxObjectPtr, DbusNativeConnectionOpenPrivate, CSTRING, address)
{
#if defined (DEBUG_METHODS)
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

        // save conn in a Rexx pointer object and save it as CSELF
   RexxPointerObject rpo = context->NewPointer(conn);
   context->SetObjectVariable("CSELF", rpo); // set object's CSELF

   return NULLOBJECT;          // return nothing
}


// -----------------------------------------------------------------------------------
        // close a private connection
RexxMethod1(RexxObjectPtr, DbusNativeConnectionClose, CSELF, connPtr)
{
#if defined (DEBUG_METHODS)
   fprintf(stderr, "===> arrived in DbusNativeConnectionClose: connPtr=[%p] \n", connPtr);
   fflush(stderr);
#endif
        // get self and carry out the
    DBusConnection *conn=(DBusConnection *) connPtr;
    dbus_connection_close(conn);
    dbus_connection_unref(conn);
        // not a valid connection anymore, hence set CSELF to .nil
    context->SetObjectVariable("CSELF", context->Nil());

    return NULLOBJECT;          // return nothing
}





// -------------- DBusServer and connection by address ... ---------------------------------------------
// --------------------------------------------------------------------------------
// callbacks needed for dbus_server, which gets invoked if a new connection is attempted ...

// --------------------------------------------------------------------------------
void new_connection_callback (DBusServer *server, DBusConnection *conn, void *data) // callback on new connection
{
#if defined (DEBUG_METHODS)
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

        // create an instance of DBus (a connection)
    RexxClassObject      rco=rtc->FindClass("DBUS");    // get class
    RexxObjectPtr rxDBusConn=rtc->SendMessage1(rco, "NEW", rtc->String("native"));  // get DBus (bus connection) object
    // RexxObjectPtr rxDBusConn=rtc->SendMessage0(rco, "NEW"); // get DBus (bus connection) object

        // save conn in a Rexx pointer object and save it as CSELF
    RexxPointerObject rpo = rtc->NewPointer(conn);
    rtc->SendMessage1(rxDBusConn, "CSELF=",         rpo); // set CSELF
    // rtc->SendMessage1(rxDBusConn, "BUSTYPE=", rtc->String("private"));  // set bus type
    rtc->SendMessage1(rxDBusConn, "SERVER=", cbd->rxDbusServer);        // set server this connection belongs to

        // let Rexx code setup (save to connections, assigning default service and listener objects)
    rtc->SendMessage1(cbd->rxDbusServer, "NEWCONNECTION", rxDBusConn);

#if defined (DEBUG_RGF)
    fprintf(stderr, "===>         in \"new_connection_callback(...)\", rtc->CheckCondition()=[%d] \n",
            (bool) rtc->CheckCondition());
#endif

    rtc->DetachThread();    // detach thread from Rexx interpreter instance

#if defined (DEBUG_METHODS)
   fprintf(stderr, "<--- end of new_connection_callback(...).\n"); fflush(stderr);
#endif
}


// --------------------------------------------------------------------------------
dbus_bool_t add_watch_callback (DBusWatch *watch, void *data) // callback on new connection
{
#if defined (DEBUG_METHODS)
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

   // PSTRUCT_CALLBACK_DATA wl=(PSTRUCT_CALLBACK_DATA) data;  // cast to be able to use the watch list
   PSTRUCT_WATCH_LIST wl=(PSTRUCT_WATCH_LIST) data;    // cast toe able to use the watch list

   psw->next=wl->watchList;    // let new node point to current root node
   wl->watchList=psw;          // make new node the root of the list

#if defined (DEBUG_METHODS)
   fprintf(stderr, "<--- end of add_watch_callback(...).\n"); fflush(stderr);
#endif
    return true;
}


// --------------------------------------------------------------------------------
void remove_watch_callback (DBusWatch *watch, void *data) // callback on new connection
{
#if defined (DEBUG_METHODS)
   fprintf(stderr, "===> arrived in \"remove_watch_callback(...)\", data=[%p}, watch=[%p], fd=[%d] \n",
                         data, watch, dbus_watch_get_socket(watch));
   fflush(stderr);
#endif
   // PSTRUCT_CALLBACK_DATA wl=(PSTRUCT_CALLBACK_DATA) data;  // cast to be able to use the watch list
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

#if defined (DEBUG_METHODS)
   fprintf(stderr, "<--- end of remove_watch_callback(...).\n"); fflush(stderr);
#endif

}



// --------------------------------------------------------------------------------
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
   rtc->SendMessage2(pto->rxDbusServer, "TIMERLOOP", rtc->String("START"), rpo);


#if defined (DEBUG_TIMEOUTLOOP) || defined (DEBUG_METHODS)
   fprintf(stderr, "<--- end of add_timeout_callback(...), interval=[%d].\n", pto->currentInterval); fflush(stderr);
#endif
    return true;
}


// --------------------------------------------------------------------------------
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
   rtc->SendMessage1(pto->rxDbusServer, "TIMERLOOP", rtc->String("STOP"));

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
#if defined (DEBUG_TIMEOUTLOOP) || defined (DEBUG_METHODS)
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
    while (context->GetObjectVariable(timeoutLoopActive) == rxTrue) // while watch loop active, go ahead
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
            // fprintf(stderr, "... TimeoutLoop(): after timeout, res=[%d], counter=[%zu], timeout_ms=[%d] timeout enabled=[%d]  \n",
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
    //    fprintf(stderr, "... TimeoutLoop(): after timeout, branch 'enabled', counter=[%zu], about to call 'dbus_timeout_handle()' ... \n",counter);
    fprintf(stderr, "... TimeoutLoop(): after timeout, branch 'enabled', counter=[%lu], about to call 'dbus_timeout_handle()' ... \n", (unsigned long) counter);
    }
#endif
            dbus_timeout_handle(pto->timeout);  // let DBus act ...
        }
    }


#if defined (DEBUG_TIMEOUTLOOP) || defined (DEBUG_METHODS)
    fprintf(stderr, "... TimeoutLoop(): about to leav.\n"); fflush(stderr);
#endif

    return NULL;    // do not return anything
}







// --------------------------------------------------------------------------------
// startup a DBus server instance
RexxMethod2(RexxObjectPtr, DbusNativeServerStartup,
                                  CSTRING,  address,
                                  OSELF,    self)
{

#if defined (DEBUG_METHODS)
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


#if defined (DEBUG_METHODS)
   fprintf(stderr, "<--- end of DbusNativeServerStartup(...).\n"); fflush(stderr);
#endif

    return NULL;
}


// -----------------------------------------------------------------------------------------------------
    // ============================================================================= //
    // set up and run a message loop for this connection instance
    // processes signals and method calls to registered ooRexx server objects
    // wrapped up as a Rexx method, hence easy to run it on a separate thread
RexxMethod2(RexxObjectPtr, DbusServerWatchLoop, CSELF, connPtr, OSELF, self)
{
#ifdef DEBUG_WATCHLOOP
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
        int res = 0;

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
#endif
            res=select(0,NULL,NULL,NULL,&tv);
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): after select() #1 ... \n");
#endif
        }
        else            // do a select
        {
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): before select()... \n");
#endif
            res = select (maxfd + 1, &rfds, &wfds, &efds, &tv); // always timout
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): after select() #2, res=[%d]... \n", res);
#endif

            if (context->GetObjectVariable(watchLoopActive) != rxTrue)  // stop watch loop ?
            {
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): after select() #2, learned we need to stop watch loop, breaking! \n");
#endif
                break;
            }

                // loop over list of DBusWatches
            for (PSTRUCT_WATCH pw=wl->watchList; pw && pw->watch; pw=pw->next )
            {
                // TODO: dbus_warch_get_unfix_fd better? (if doing a get_socket on Windows as the source code implies)
                int tmpfd=dbus_watch_get_socket(pw->watch);

                if ( FD_ISSET(tmpfd,&rfds) )
                {
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): ---> dbus_watch_handle watch [%p], fd=[%d] from 'rfds ...' \n", pw->watch, tmpfd);
#endif
                    dbus_watch_handle(pw->watch, DBUS_WATCH_READABLE);
                }

                if ( FD_ISSET(tmpfd,&wfds) )
                {
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): ---> dbus_watch_handle watch [%p], fd=[%d] from 'wfds ...' \n", pw->watch, tmpfd);
#endif
                    dbus_watch_handle(pw->watch, DBUS_WATCH_WRITABLE);
                }

                if ( FD_ISSET(tmpfd,&efds) )
                {
#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): ---> dbus_watch_handle watch [%p], fd=[%d] from 'efds ...' \n", pw->watch, tmpfd);
#endif
                    dbus_watch_handle(pw->watch, DBUS_WATCH_ERROR);  // TODO: really to pass that flag?
                }
            }
        }
    }       // end while

#ifdef DEBUG_WATCHLOOP
    fprintf(stderr, "... WatchLoop(): about to leave.\n"); fflush(stderr);
#endif

    return NULL;    // do not return anything
}



// --------------------------------------------------------------------------------
        // test whether server instance is active
RexxMethod1(logical_t, DbusNativeServerIsActive, CSELF, serverPtr)
{
#if defined (DEBUG_METHODS)
   fprintf(stderr, "===> arrived in DbusNativeServerIsActive: serverPtr=[%p] \n", serverPtr);
   fflush(stderr);
#endif
        // get self and carry out the
    DBusServer *server=(DBusServer *) serverPtr;
    return dbus_server_get_is_connected(server);
}


// --------------------------------------------------------------------------------
        // get and return the server's id
RexxMethod1(RexxObjectPtr, DbusNativeServerGetId, CSELF, serverPtr)
{
#if defined (DEBUG_METHODS)
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
       return NULL;
    }
    RexxStringObject rso = context->String(tmpStr);
    dbus_free(tmpStr);
    return rso;
}


// --------------------------------------------------------------------------------
        // get and return server's address
RexxMethod1(RexxObjectPtr, DbusNativeServerGetAddress, CSELF, serverPtr)
{
#if defined (DEBUG_METHODS)
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
       return NULL;
    }
    RexxStringObject rso = context->String(tmpStr);
    dbus_free(tmpStr);
    return rso;
}


// --------------------------------------------------------------------------------
        // disconnect the server
RexxMethod2(RexxObjectPtr, DbusNativeServerDisconnect, CSELF, serverPtr, OSELF, self)
{
#if defined (DEBUG_METHODS)
   fprintf(stderr, "===> arrived in DbusNativeServerDisconnect: serverPtr=[%p] \n", serverPtr);
   fflush(stderr);
#endif

    DBusServer *server=(DBusServer *) serverPtr;
    dbus_server_disconnect(server);
    dbus_server_unref(server);

    context->SendMessage1(self, "CSELF=", context->Nil()); // set CSELF

    return NULLOBJECT;
}


// -----------------------------------------------------------------------------------------------------
// -------------------- Rexx package definitions ----------------------

// External functions if loaded via Rexx
    // Build the list of entry points for the routines:
    RexxRoutineEntry dbus_external_functions[] =
    {
        REXX_TYPED_ROUTINE( DbusVersion             , DbusVersion                 ),
        REXX_TYPED_ROUTINE( DbusValidate            , DbusValidate                ),
        REXX_TYPED_ROUTINE( DbusHaltRexx            , DbusHaltRexx                ),

        REXX_LAST_ROUTINE()
    };

// External functions if loaded via Rexx
    // Build the list of entry points for the routines:
    RexxMethodEntry dbus_external_methods [] =
    {
        REXX_METHOD( DbusNativeGetSystemBus         , DbusNativeGetSystemBus          ),
        REXX_METHOD( DbusNativeGetSessionBus        , DbusNativeGetSessionBus         ),

//        REXX_METHOD( DbusGetVersion                 , DbusGetVersion                  ),

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

        REXX_LAST_METHOD()
    };

    RexxPackageEntry dbus_package_entry =
    {
        STANDARD_PACKAGE_HEADER
        REXX_INTERPRETER_4_1_0,             // anything including and after 4.1.0 will work
        "dbus",                             // name of the package
        "0.95",                             // package information

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
