package org.rexxla.bsf.engines.rexx;

import java.util.HashMap;


/**
 * This Enum defines the invocation types and their corresponding Rexx name.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2018 Rony G. Flatscher
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
 * @version 1.0, 2018-01-08
 * @since BSF4ooRexx 600
 * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
 */


enum InvocationType {

        // define the enum values for the invocation functions available to Rexx programs, supplying the strings associated with them

           ADD_EVENT_LISTENER          ( "addEventListener"         ) , //=  1
           ARRAY_AT                    ( "arrayAt"                  ) , //=  2
           ARRAY_LENGTH                ( "arrayLength"              ) , //=  3
           ARRAY_PUT                   ( "arrayPut"                 ) , //=  4
           ARRAY_PUT_STRICT            ( "arrayPutStrict"           ) , //=  5
           BSF_PREFIX_RETURN_VALUE     ( "bsfPrefixReturnValue"     ) , //=  33  2008-06-22, ---rgf
           CARRY_OUT_SYSTEM_EXIT       ( "carryOutSystemExit"       ) , //=  41  2011-02-23, ---rgf
           CREATE_ARRAY_OBJECT         ( "createArray"              ) , //=  6  ! rgf, 2003-02-17, SYNONYM! 2009-09-19: "createJavaArray"
           CREATE_ARRAY_OBJECT_OF      ( "createArrayOf"            ) , //=  36 ! 2009-07-30, ---rgf, SYNONYM created 2009-09-10: "createJavaArrayOf"
           ENV_GET                     ( "getEnv"                   ) , //=  42  2014-03-29, ---rgf
           ENV_SET                     ( "setEnv"                   ) , //=  43  2014-03-29, ---rgf
           EXIT                        ( "exit"                     ) , //=  7
           GET_BSF_MANAGER             ( "getBSFManager"            ) , //=  32  2006-12-08, ---rgf
           GET_FIELD_VALUE             ( "getFieldValue"            ) , //=  8
           GET_FIELD_VALUE_JSO         ( "getFieldValueJSO"         ) , //=  46  2014-10-04, ---rgf
           GET_FIELD_VALUE_STRICT      ( "getFieldValueStrict"      ) , //=  9   2005-05-06, ---rgf
           GET_FIELD_VALUE_STRICT_JSO  ( "getFieldValueStrictJSO"   ) , //=  47  2014-10-04, ---rgf
           GET_PROPERTY_VALUE          ( "getPropertyValue"         ) , //=  10
           GET_PROPERTY_VALUE_JSO      ( "getPropertyValueJSO"      ) , //=  48  2014-10-04, ---rgf
           GET_REXX_AND_JAVA           ( "getRexxAndJava"           ) , //=  37 ! 2009-10-02, ---rgf; SYNONYM created 2009-10-12: "getRAJO"
           GET_REXX_ENGINE             ( "getRexxEngine"            ) , //=  38  2009-10-07, ---rgf
           GET_RII_ID                  ( "get_RII_id"               ) , //=  51  2015-08-01, ---rgf
           GET_STATIC_VALUE            ( "getStaticValue"           ) , //=  11
           GET_STATIC_VALUE_JSO        ( "getStaticValueJSO"        ) , //=  49  2014-10-04, ---rgf
           GET_STATIC_VALUE_STRICT     ( "getStaticValueStrict"     ) , //=  12
           GET_STATIC_VALUE_STRICT_JSO ( "getStaticValueStrictJSO"  ) , //=  50  2014-10-04, ---rgf
           GET_VERSION                 ( "version"                  ) , //=  13 // rgf, 2003-03-10
           INCREASE_REF_COUNTER        ( "increaseRefCounter"       ) , //=  52  2015-08-04, ---rgf
           INVOKE                      ( "invoke"                   ) , //=  14
           INVOKE_JSO                  ( "invokeJSO"                ) , //=  44  2014-10-02, ---rgf
           INVOKE_STRICT               ( "invokeStrict"             ) , //=  15
           INVOKE_STRICT_JSO           ( "invokeStrictJSO"          ) , //=  45  2014-10-04, ---rgf
           IS_INSTANCE                 ( "isA"                      ) , //=  40  2010-02-20, ---rgf
           KUDOS                       ( "kudos"                    ) , //=  39  2009-10-17, ---rgf
           LOAD_CLASS                  ( "loadClass"                ) , //=  30  2006-01-05, ---rgf: new subfunction to get a thread-context loaded class
           LOOKUP_BEAN                 ( "lookupBean"               ) , //=  16
           POLL_EVENT_TEXT             ( "pollEventText"            ) , //=  17
           POST_EVENT_TEXT             ( "postEventText"            ) , //=  18
           RAW_REGISTER_BEAN           ( "rawRegisterBean"          ) , //=  34  2008-08-28, ---rgf
           REGISTER_BEAN               ( "registerBean"             ) , //=  19 ! SYNONYM added 2005-06-17: "new"
           REGISTER_BEAN_STRICT        ( "registerBeanStrict"       ) , //=  20 ! // rgf, 2003-05-08; SYNONYM added 2005-06-17: "newStrict"
           REXX_HALT                   ( "haltThread"               ) , //=  35  2009-04-02, ---rgf
           SET_FIELD_VALUE             ( "setFieldValue"            ) , //=  21
           SET_FIELD_VALUE_STRICT      ( "setFieldValueStrict"      ) , //=  22  // rgf, 2003-05-10
           SET_PROPERTY_VALUE          ( "setPropertyValue"         ) , //=  23
           SET_PROPERTY_VALUE_STRICT   ( "setPropertyValueStrict"   ) , //=  24  // rgf, 2003-05-10
           SET_REXX_NULL_STRING        ( "setRexxNullString"        ) , //=  25
           SLEEP                       ( "sleep"                    ) , //=  26
           UNREGISTER_BEAN             ( "unregisterBean"           ) , //=  27
           WRAP_ARRAY_OBJECT           ( "wrapArray"                ) , //=  28  // rgf, 2003-02-09
           WRAP_ENUMERATION_OBJECT     ( "wrapEnumeration"          ) ; //=  29  // rgf, 2003-02-12

/*

// removed: 2009-06-07: use BsfCreateRexxProxy() instead
// static final int           ADD_EVENT_LISTENER_2        =  31 ;   // 2006-02-24, ---rgf: allows for retrieving event objects
*/

/*


        // define the constant strings for the function names passed from Rexx
private static final String STRING_ADD_EVENT_LISTENER             = "addEventListener"                     ;
// private static final String STRING_ADD_EVENT_LISTENER_2           = "addEventListenerReturningEventInfos"  ; // rgf, 2006-02-24
private static final String STRING_ARRAY_AT                       = "arrayAt"                              ;
private static final String STRING_ARRAY_LENGTH                   = "arrayLength"                          ;
private static final String STRING_ARRAY_PUT                      = "arrayPut"                             ;
private static final String STRING_ARRAY_PUT_STRICT               = "arrayPutStrict"                       ;
private static final String STRING_CREATE_ARRAY_OBJECT            = "createArray"                          ; // rgf, 2003-02-17
private static final String STRING_EXIT                           = "exit"                                 ;
private static final String STRING_GET_BSF_MANAGER                = "getBSFManager"                        ; // rgf, 2006-12-08
private static final String STRING_GET_FIELD_VALUE                = "getFieldValue"                        ;
private static final String STRING_GET_FIELD_VALUE_JSO            = "getFieldValueJSO"                     ; // rgf, 2014-10-04
private static final String STRING_GET_FIELD_VALUE_STRICT         = "getFieldValueStrict"                  ; // rgf, 2005-05-06
private static final String STRING_GET_FIELD_VALUE_STRICT_JSO     = "getFieldValueStrictJSO"               ; // rgf, 2014-10-04
private static final String STRING_GET_PROPERTY_VALUE             = "getPropertyValue"                     ;
private static final String STRING_GET_PROPERTY_VALUE_JSO         = "getPropertyValueJSO"                  ; // rgf, 2014-10-04
private static final String STRING_GET_STATIC_VALUE               = "getStaticValue"                       ;
private static final String STRING_GET_STATIC_VALUE_JSO           = "getStaticValueJSO"                    ; // rgf, 2014-10-04
private static final String STRING_GET_STATIC_VALUE_STRICT        = "getStaticValueStrict"                 ; // rgf, 2005-05-06
private static final String STRING_GET_STATIC_VALUE_STRICT_JSO    = "getStaticValueStrictJSO"              ; // rgf, 2014-10-04
private static final String STRING_GET_VERSION                    = "version"                              ; // rgf, 2003-03-10
private static final String STRING_INVOKE                         = "invoke"                               ;
private static final String STRING_INVOKE_JSO                     = "invokeJSO"                            ; // rgf, 2014-10-02
private static final String STRING_INVOKE_STRICT                  = "invokeStrict"                         ; // rgf, 2003-05-07
private static final String STRING_INVOKE_STRICT_JSO              = "invokeStrictJSO"                      ; // rgf, 2014-10-04
private static final String STRING_LOOKUP_BEAN                    = "lookupBean"                           ;
private static final String STRING_POLL_EVENT_TEXT                = "pollEventText"                        ;
private static final String STRING_POST_EVENT_TEXT                = "postEventText"                        ;
private static final String STRING_REGISTER_BEAN                  = "registerBean"                         ;
private static final String STRING_NEW                            = "new"                                  ; // rgf, 2005-06-17
private static final String STRING_REGISTER_BEAN_STRICT           = "registerBeanStrict"                   ; // rgf, 2003-05-08
private static final String STRING_NEW_STRICT                     = "newStrict"                            ; // rgf, 2005-06-17
private static final String STRING_SET_FIELD_VALUE                = "setFieldValue"                        ;
private static final String STRING_SET_FIELD_VALUE_STRICT         = "setFieldValueStrict"                  ; // rgf, 2003-05-10
private static final String STRING_SET_PROPERTY_VALUE             = "setPropertyValue"                     ;
private static final String STRING_SET_PROPERTY_VALUE_STRICT      = "setPropertyValueStrict"               ; // rgf, 2003-05-10
private static final String STRING_SET_REXX_NULL_STRING           = "setRexxNullString"                    ;
private static final String STRING_SLEEP                          = "sleep"                                ;
private static final String STRING_UNREGISTER_BEAN                = "unregisterBean"                       ;
private static final String STRING_WRAP_ARRAY_OBJECT              = "wrapArray"                            ; // rgf, 2003-02-09
private static final String STRING_WRAP_ENUMERATION_OBJECT        = "wrapEnumeration"                      ; // rgf, 2003-02-12
private static final String STRING_LOAD_CLASS                     = "loadClass"                            ; // rgf, 2006-01-05
private static final String STRING_BSF_PREFIX_RETURN_VALUE        = "bsfPrefixReturnValue"                 ; // rgf, 2008-06-22
private static final String STRING_RAW_REGISTER_BEAN              = "rawRegisterBean"                      ;
private static final String STRING_REXX_HALT                      = "haltThread"                           ; // rgf, 2009-04-02
private static final String STRING_CREATE_ARRAY_OBJECT_OF         = "createArrayOf"                        ; // rgf, 2009-07-30
private static final String STRING_CREATE_JAVA_ARRAY_OBJECT_OF    = "createJavaArrayOf"                    ; // rgf, 2009-09-10
private static final String STRING_CREATE_JAVA_ARRAY_OBJECT       = "createJavaArray"                      ; // rgf, 2009-09-10
private static final String STRING_GET_REXX_AND_JAVA              = "getRexxAndJava"                       ; // rgf, 2009-10-02
private static final String STRING_GET_REXX_ENGINE                = "getRexxEngine"                        ; // rgf, 2009-10-07
private static final String STRING_GET_RAJO                       = "getRAJO"                              ; // rgf, 2009-10-12

private static final String STRING_KUDOS                          = "kudos"                                ; // rgf, 2009-10-17

// private static final String STRING_IS_INSTANCE                    = "isInstance"                           ; // rgf, 2010-02-20
private static final String STRING_IS_A                           = "isA"                                  ; // rgf, 2010-02-20

private static final String STRING_CARRY_OUT_SYSTEM_EXIT          =  "carryOutSystemExit";   // 2011-02-23, ---rgf


private static final String STRING_ENV_GET                        =  "getEnv";               // 2014-03-29, ---rgf
private static final String STRING_ENV_SET                        =  "setEnv";               // 2014-03-29, ---rgf

private static final String STRING_GET_RII_ID                     =  "get_RII_id";           // 2015-08-01, ---rgf
private static final String STRING_INCREASE_REF_COUNTER           =  "increaseRefCounter";   // 2015-08-04, ---rgf




       boolean bStrongTyping=false; // set to true, if Java types preceed the individual Rexx arguments
       int choice = 0;

            if (args[0].equalsIgnoreCase( STRING_INVOKE                         )) choice = INVOKE                       ;
       else if (args[0].equalsIgnoreCase( STRING_INVOKE_JSO                     )) choice = INVOKE_JSO                   ; // rgf, 2010-10-02
       else if (args[0].equalsIgnoreCase( STRING_INVOKE_STRICT                  )) choice = INVOKE_STRICT                ;
       else if (args[0].equalsIgnoreCase( STRING_INVOKE_STRICT_JSO              )) choice = INVOKE_STRICT_JSO            ; // rgf, 2014-10-04

       else if (args[0].equalsIgnoreCase( STRING_REGISTER_BEAN                  )) choice = REGISTER_BEAN                ;
       else if (args[0].equalsIgnoreCase( STRING_NEW                            )) choice = NEW                          ;
       else if (args[0].equalsIgnoreCase( STRING_REGISTER_BEAN_STRICT           )) choice = REGISTER_BEAN_STRICT         ;
       else if (args[0].equalsIgnoreCase( STRING_NEW_STRICT                     )) choice = NEW_STRICT                   ;



       else if (args[0].equalsIgnoreCase( STRING_LOOKUP_BEAN                    )) choice = LOOKUP_BEAN                  ;
       else if (args[0].equalsIgnoreCase( STRING_UNREGISTER_BEAN                )) choice = UNREGISTER_BEAN              ;

       else if (args[0].equalsIgnoreCase( STRING_ADD_EVENT_LISTENER             )) choice = ADD_EVENT_LISTENER           ;
//       else if (args[0].equalsIgnoreCase( STRING_ADD_EVENT_LISTENER_2           )) choice = ADD_EVENT_LISTENER_2         ;
       else if (args[0].equalsIgnoreCase( STRING_POLL_EVENT_TEXT                )) choice = POLL_EVENT_TEXT              ;
       else if (args[0].equalsIgnoreCase( STRING_POST_EVENT_TEXT                )) choice = POST_EVENT_TEXT              ;

       else if (args[0].equalsIgnoreCase( STRING_GET_FIELD_VALUE                )) choice = GET_FIELD_VALUE              ;
       else if (args[0].equalsIgnoreCase( STRING_GET_FIELD_VALUE_JSO            )) choice = GET_FIELD_VALUE_JSO          ; // rgf, 2014-10-04
       else if (args[0].equalsIgnoreCase( STRING_GET_FIELD_VALUE_STRICT         )) choice = GET_FIELD_VALUE_STRICT       ; // rgf, 2005-05-06
       else if (args[0].equalsIgnoreCase( STRING_GET_FIELD_VALUE_STRICT_JSO     )) choice = GET_FIELD_VALUE_STRICT_JSO   ; // rgf, 2014-10-04
       else if (args[0].equalsIgnoreCase( STRING_GET_STATIC_VALUE               )) choice = GET_STATIC_VALUE             ;
       else if (args[0].equalsIgnoreCase( STRING_GET_STATIC_VALUE_JSO           )) choice = GET_STATIC_VALUE_JSO         ; // rgf, 2014-10-04
       else if (args[0].equalsIgnoreCase( STRING_GET_STATIC_VALUE_STRICT        )) choice = GET_STATIC_VALUE_STRICT      ;
       else if (args[0].equalsIgnoreCase( STRING_GET_STATIC_VALUE_STRICT_JSO    )) choice = GET_STATIC_VALUE_STRICT_JSO  ; // rgf, 2014-10-04

       else if (args[0].equalsIgnoreCase( STRING_EXIT                           )) choice = EXIT                         ;

       else if (args[0].equalsIgnoreCase( STRING_SET_FIELD_VALUE                )) choice = SET_FIELD_VALUE              ;
       else if (args[0].equalsIgnoreCase( STRING_SET_FIELD_VALUE_STRICT         )) choice = SET_FIELD_VALUE_STRICT       ;

       else if (args[0].equalsIgnoreCase( STRING_ARRAY_AT                       )) choice = ARRAY_AT                     ;
       else if (args[0].equalsIgnoreCase( STRING_ARRAY_LENGTH                   )) choice = ARRAY_LENGTH                 ;
       else if (args[0].equalsIgnoreCase( STRING_ARRAY_PUT                      )) choice = ARRAY_PUT                    ;
       else if (args[0].equalsIgnoreCase( STRING_ARRAY_PUT_STRICT               )) choice = ARRAY_PUT_STRICT             ;
       else if (args[0].equalsIgnoreCase( STRING_CREATE_ARRAY_OBJECT_OF         )) choice = CREATE_ARRAY_OBJECT_OF       ; // 20090730, ---rgf
       else if (args[0].equalsIgnoreCase( STRING_CREATE_JAVA_ARRAY_OBJECT_OF    )) choice = CREATE_ARRAY_OBJECT_OF       ; // 20090910, ---rgf, a synonym
       else if (args[0].equalsIgnoreCase( STRING_CREATE_ARRAY_OBJECT            )) choice = CREATE_ARRAY_OBJECT          ;
       else if (args[0].equalsIgnoreCase( STRING_CREATE_JAVA_ARRAY_OBJECT       )) choice = CREATE_ARRAY_OBJECT          ; // 20090910, ---rgf, a synonym

       else if (args[0].equalsIgnoreCase( STRING_GET_PROPERTY_VALUE             )) choice = GET_PROPERTY_VALUE           ;
       else if (args[0].equalsIgnoreCase( STRING_GET_PROPERTY_VALUE_JSO         )) choice = GET_PROPERTY_VALUE_JSO       ; // 20141004, ---rgf
       else if (args[0].equalsIgnoreCase( STRING_SET_PROPERTY_VALUE             )) choice = SET_PROPERTY_VALUE_STRICT    ;
       else if (args[0].equalsIgnoreCase( STRING_SET_PROPERTY_VALUE_STRICT      )) choice = SET_PROPERTY_VALUE_STRICT    ;

       else if (args[0].equalsIgnoreCase( STRING_SET_REXX_NULL_STRING           )) choice = SET_REXX_NULL_STRING         ;
       else if (args[0].equalsIgnoreCase( STRING_SLEEP                          )) choice = SLEEP                        ;

       else if (args[0].equalsIgnoreCase( STRING_WRAP_ARRAY_OBJECT              )) choice = WRAP_ARRAY_OBJECT            ;
       else if (args[0].equalsIgnoreCase( STRING_WRAP_ENUMERATION_OBJECT        )) choice = WRAP_ENUMERATION_OBJECT      ;

       else if (args[0].equalsIgnoreCase( STRING_GET_VERSION                    )) choice = GET_VERSION                  ;

       else if (args[0].equalsIgnoreCase( STRING_LOAD_CLASS                     )) choice = LOAD_CLASS                   ;
       else if (args[0].equalsIgnoreCase( STRING_GET_BSF_MANAGER                )) choice = GET_BSF_MANAGER              ;
       else if (args[0].equalsIgnoreCase( STRING_GET_REXX_AND_JAVA              )) choice = GET_REXX_AND_JAVA            ;
       else if (args[0].equalsIgnoreCase( STRING_GET_RAJO                       )) choice = GET_REXX_AND_JAVA            ;  // 2009-10-12, rgf
       else if (args[0].equalsIgnoreCase( STRING_GET_REXX_ENGINE                )) choice = GET_REXX_ENGINE              ;

       else if (args[0].equalsIgnoreCase( STRING_BSF_PREFIX_RETURN_VALUE        )) choice = BSF_PREFIX_RETURN_VALUE      ;

       else if (args[0].equalsIgnoreCase( STRING_RAW_REGISTER_BEAN              )) choice = RAW_REGISTER_BEAN            ;  // 20080828, rgf

       else if (args[0].equalsIgnoreCase( STRING_REXX_HALT                      )) choice = REXX_HALT                    ;  // 20090402, rgf

       else if (args[0].equalsIgnoreCase( STRING_KUDOS                          )) choice = KUDOS                        ;  // 20091017, rgf

       else if (args[0].equalsIgnoreCase( STRING_IS_A                           )) choice = IS_INSTANCE                  ;  // 20100220, rgf
//       else if (args[0].equalsIgnoreCase( STRING_IS_INSTANCE                    )) choice = IS_INSTANCE                  ;  // 20100220, rgf

       else if (args[0].equalsIgnoreCase( STRING_CARRY_OUT_SYSTEM_EXIT          )) choice = CARRY_OUT_SYSTEM_EXIT        ;  // 20110223, rgf

       else if (args[0].equalsIgnoreCase( STRING_ENV_GET                        )) choice = ENV_GET                      ;  // 20140329, rgf
       else if (args[0].equalsIgnoreCase( STRING_ENV_SET                        )) choice = ENV_SET                      ;  // 20140329, rgf

       else if (args[0].equalsIgnoreCase( STRING_GET_RII_ID                     )) choice = GET_RII_ID                   ;  // 20150801, rgf
       else if (args[0].equalsIgnoreCase( STRING_INCREASE_REF_COUNTER           )) choice = INCREASE_REF_COUNTER         ;  // 20150804, rgf

*/

       /**  HashMap that allows to retrieve the enum type given a string, irrespectible of the case the string was supplied.
        *   Cf. {@link static public InvocationType getInvocationType(String mixedCase)}.
        */
       final static HashMap<String,InvocationType> upperCase2InvocationType=new HashMap<String,InvocationType>(128);

       static   // use static block to add synonym strings to upperCase2InvocationType Map
       {
           // add all types to HashMap
            for (InvocationType it : InvocationType.values())
            {
                upperCase2InvocationType.put(it.upperCase, it);
            }

           // add synonyms                new name             enum type
           upperCase2InvocationType.put( "CREATEJAVAARRAY"   , CREATE_ARRAY_OBJECT   ) ; //=  6  ! rgf, 2003-02-17, SYNONYM! 2009-09-19: "createJavaArray"
           upperCase2InvocationType.put( "NEW"               , REGISTER_BEAN         ) ; //=  19 ! SYNONYM: 2005-06-17: "new"
           upperCase2InvocationType.put( "NEWSTRICT"         , REGISTER_BEAN_STRICT  ) ; //=  20 ! // rgf, 2003-05-08; SYNONYM: 2005-06-17: "newStrict"
           upperCase2InvocationType.put( "CREATEJAVAARRAYOF" , CREATE_ARRAY_OBJECT_OF) ; //=  36 ! 2009-07-30, ---rgf, SYNONYM! 2009-09-10: "createJavaArrayOf"
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

