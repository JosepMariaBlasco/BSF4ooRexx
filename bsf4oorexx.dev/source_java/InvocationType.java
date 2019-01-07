package org.rexxla.bsf.engines.rexx;

import java.util.HashMap;


/**
 * This Enum defines the invocation types and their corresponding Rexx name.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2018-2019 Rony G. Flatscher
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ----------------------------------------------------------------------------- </pre>
 *
 * @version 1.02, 2019-01-07
 * @since BSF4ooRexx 600
 * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
 */


 /*
     2018-11-01, rgf: - added enum value "CREATE_STRICT_ARGUMENT"
     2019-01-07, rgf: - added enum value "UNBOX"
*/


enum InvocationType {

    // define the enum values for the invocation functions available to Rexx programs, supplying the strings associated with them
    // order: most used invocation types first (speculation: HashMap will be able to efficiently store and retrieve them)

    GET_FIELD_VALUE             ( "getFieldValue"            ) , //=  8

    INVOKE                      ( "invoke"                   ) , //=  14
    REGISTER_BEAN               ( "registerBean"             ) , //=  19 ! SYNONYM added 2005-06-17: "new"


    CREATE_ARRAY_OBJECT         ( "createArray"              ) , //=  6  ! rgf, 2003-02-17, SYNONYM! 2009-09-19: "createJavaArray"
    ARRAY_AT                    ( "arrayAt"                  ) , //=  2
    ARRAY_PUT                   ( "arrayPut"                 ) , //=  4
    ARRAY_LENGTH                ( "arrayLength"              ) , //=  3
    WRAP_ARRAY_OBJECT           ( "wrapArray"                ) , //=  28  // rgf, 2003-02-09

    LOAD_CLASS                  ( "loadClass"                ) , //=  30  2006-01-05, ---rgf: new subfunction to get a thread-context loaded class
    UNREGISTER_BEAN             ( "unregisterBean"           ) , //=  27

    CREATE_STRICT_ARGUMENT      ( "strictArg"                ) , // rgf, 2018-11-01: create and return a RexxStrictArgument


    INCREASE_REF_COUNTER        ( "increaseRefCounter"       ) , //=  52  2015-08-04, ---rgf

    SET_FIELD_VALUE             ( "setFieldValue"            ) , //=  21

    UNBOX                       ( "unbox"                    ) , // 2019-01-07, rgf: unboxing a wrapped value

    INVOKE_JSO                  ( "invokeJSO"                ) , //=  44  2014-10-02, ---rgf
    GET_FIELD_VALUE_JSO         ( "getFieldValueJSO"         ) , //=  46  2014-10-04, ---rgf
    GET_STATIC_FIELD_VALUE_JSO  ( "getStaticValueJSO"        ) , //=  49  2014-10-04, ---rgf

    CREATE_ARRAY_OBJECT_OF      ( "createArrayOf"            ) , //=  36 ! 2009-07-30, ---rgf, SYNONYM created 2009-09-10: "createJavaArrayOf"

    GET_REXX_AND_JAVA           ( "getRexxAndJava"           ) , //=  37 ! 2009-10-02, ---rgf; SYNONYM created 2009-10-12: "getRAJO"
    GET_BSF_MANAGER             ( "getBSFManager"            ) , //=  32  2006-12-08, ---rgf
    GET_REXX_ENGINE             ( "getRexxEngine"            ) , //=  38  2009-10-07, ---rgf
    GET_RII_ID                  ( "get_RII_id"               ) , //=  51  2015-08-01, ---rgf

    LOOKUP_BEAN                 ( "lookupBean"               ) , //=  16

    GET_STATIC_FIELD_VALUE      ( "getStaticValue"           ) , //=  11

    RAW_REGISTER_BEAN           ( "rawRegisterBean"          ) , //=  34  2008-08-28, ---rgf

    ARRAY_PUT_STRICT            ( "arrayPutStrict"           ) , //=  5

    INVOKE_STRICT               ( "invokeStrict"             ) , //=  15
    INVOKE_STRICT_JSO           ( "invokeStrictJSO"          ) , //=  45  2014-10-04, ---rgf
    GET_FIELD_VALUE_STRICT      ( "getFieldValueStrict"      ) , //=  9   2005-05-06, ---rgf
    SET_FIELD_VALUE_STRICT      ( "setFieldValueStrict"      ) , //=  22  // rgf, 2003-05-10

    GET_STATIC_FIELD_VALUE_STRICT     ( "getStaticValueStrict"     ) , //=  12
    GET_FIELD_VALUE_STRICT_JSO  ( "getFieldValueStrictJSO"   ) , //=  47  2014-10-04, ---rgf
    GET_STATIC_FIELD_VALUE_STRICT_JSO ( "getStaticValueStrictJSO"  ) , //=  50  2014-10-04, ---rgf

    REGISTER_BEAN_STRICT        ( "registerBeanStrict"       ) , //=  20 ! // rgf, 2003-05-08; SYNONYM added 2005-06-17: "newStrict"

    WRAP_ENUMERATION_OBJECT     ( "wrapEnumeration"          ) , //=  29  // rgf, 2003-02-12

    EXIT                        ( "exit"                     ) , //=  7
    BSF_PREFIX_RETURN_VALUE     ( "bsfPrefixReturnValue"     ) , //=  33  2008-06-22, ---rgf

    ADD_EVENT_LISTENER          ( "addEventListener"         ) , //=  1
    CARRY_OUT_SYSTEM_EXIT       ( "carryOutSystemExit"       ) , //=  41  2011-02-23, ---rgf
    ENV_GET                     ( "getEnv"                   ) , //=  42  2014-03-29, ---rgf
    ENV_SET                     ( "setEnv"                   ) , //=  43  2014-03-29, ---rgf
    GET_PROPERTY_VALUE          ( "getPropertyValue"         ) , //=  10
    GET_PROPERTY_VALUE_JSO      ( "getPropertyValueJSO"      ) , //=  48  2014-10-04, ---rgf
    GET_VERSION                 ( "version"                  ) , //=  13 // rgf, 2003-03-10
    IS_INSTANCE                 ( "isA"                      ) , //=  40  2010-02-20, ---rgf
    KUDOS                       ( "kudos"                    ) , //=  39  2009-10-17, ---rgf
    POLL_EVENT_TEXT             ( "pollEventText"            ) , //=  17
    POST_EVENT_TEXT             ( "postEventText"            ) , //=  18
    REXX_HALT                   ( "haltThread"               ) , //=  35  2009-04-02, ---rgf
    SET_PROPERTY_VALUE          ( "setPropertyValue"         ) , //=  23
    SET_PROPERTY_VALUE_STRICT   ( "setPropertyValueStrict"   ) , //=  24  // rgf, 2003-05-10
    SET_REXX_NULL_STRING        ( "setRexxNullString"        ) , //=  25
    SLEEP                       ( "sleep"                    ) , //=  26

    REXX_REFLECT_OBJECT         ( "rexxReflectObject"        ) , //=  27 // rgf, 2018-02-17: return current rexxReflectObject; allows
                                                                         // querying & setting useCaching, useReflectInvoke, useUnreflect
    GET_BEAN_REFCOUNT           ( "getBeanRefCount"          ) , //=  28 // rgf, 2018-02-23: returns current refCount or -1 if not in registry
    TEST_PING                   ( "testPing"                 )   //=  29 // rgf, 2018-02-25: just returns: allows to test and time calls to the bridge
                               ;


    /**  HashMap that allows to retrieve the enum type given a string, irrespectible of the case the string was supplied.
     *   Cf. {@link static public InvocationType getInvocationType(String mixedCase)}.
     */
    final static HashMap<String,InvocationType> upperCase2InvocationType=new HashMap<String,InvocationType>(128);

    static   // use static block to add synonym strings to upperCase2InvocationType Map
    {
        // order: most used invocation types first (speculation: HashMap will be able to efficiently store and retrieve them)
        upperCase2InvocationType.put( "NEW"               , REGISTER_BEAN         ) ; //=  19 ! SYNONYM: 2005-06-17: "new"
        upperCase2InvocationType.put( "CREATEJAVAARRAY"   , CREATE_ARRAY_OBJECT   ) ; //=  6  ! rgf, 2003-02-17, SYNONYM! 2009-09-19: "createJavaArray"

        // add all types to HashMap (in declaration order)
         for (InvocationType it : InvocationType.values())
         {
             upperCase2InvocationType.put(it.upperCase, it);
         }

         // add synonyms                new name             enum type
         upperCase2InvocationType.put( "CREATEJAVAARRAYOF" , CREATE_ARRAY_OBJECT_OF) ; //=  36 ! 2009-07-30, ---rgf, SYNONYM! 2009-09-10: "createJavaArrayOf"
         upperCase2InvocationType.put( "NEWSTRICT"         , REGISTER_BEAN_STRICT  ) ; //=  20 ! // rgf, 2003-05-08; SYNONYM: 2005-06-17: "newStrict"
         upperCase2InvocationType.put( "GETRAJO"           , GET_REXX_AND_JAVA     ) ; //=  37 ! 2009-10-02, ---rgf; SYNONYM! 2009-10-12: "getRAJO"
    }


    final String mixedCase;
    /** Returns the String value in mixed case.
     *
     * @return  the String value in mixed case.
     */
    public String getMixedCase()
    {
        return mixedCase;
    }

    final String upperCase;
    /** Returns the String value in upper case.
     *
     * @return  the String value in upper case.
     */
    public String getUpperCase()
    {
        return upperCase;
    }

     // note: no access to static fields allowed in constructor (Enum values get created before static block
    InvocationType(String mixedCase)
    {
        this.mixedCase=mixedCase;
        this.upperCase=mixedCase.toUpperCase();
    }

    /** Returns the InvocationType corresponding to the supplied String.
     *
     * @param mixedCase a String value in any case corresponding to the invocation type from Rexx
     *
     * @return the enum InvocationType corresponding to the supplied String, <code>null</code> if not defined
     */
    static public InvocationType getInvocationType(String mixedCase)
    {
        if (mixedCase==null)
        {
            return null;
        }
        return upperCase2InvocationType.get(mixedCase.toUpperCase());
    }


    public static void main(String[] args) {
        System.err.println("... arrived in main, args.length="+args.length+", INVOKE="+INVOKE);
        System.err.println("... arrived in main, args.length="+args.length+", INVOKE.toString()="+INVOKE.toString());
        System.err.println("... arrived in main, args.length="+args.length+", getInvocationType(\"iNvOkE\")  ="+getInvocationType("iNvOkE"));
        System.err.println("... arrived in main, args.length="+args.length+", getInvocationType(\"newStrict\")="+getInvocationType("newStrict"));
    }

}

