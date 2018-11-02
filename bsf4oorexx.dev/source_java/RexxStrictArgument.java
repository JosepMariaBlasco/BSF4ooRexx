package org.rexxla.bsf.engines.rexx;

import org.apache.bsf.BSFException;


/**
 * This class represents values which type must strictly match with the type in a signature.
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
 * @version 1.0, 2018-11-01, 2018-11-02
 * @since BSF4ooRexx 600 (implementation started: 2018-01-05)
 * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
 */


public class RexxStrictArgument
{
    /** The Class that needs to match strictly. */
    Class <?> classObject=null;
    /** The value to use as argument value. */
    Object    value=null;

    /** Returns the class object which needs to be strictly matched. */
    public Class<?> getClassObject()
    {
        return classObject;
    }

    /** Returns the Java object to be used as the argument value. */
    public Object getValue()
    {
        return value;
    }

    /** This constructor accepts any Java object which class object will be queried and stored.
     * @param obj the Java object which class will have to be strictly matched
     */
    public RexxStrictArgument (Object value) throws BSFException
    {
        if (value==null)
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "RexxScriptArgument: argument \"obj\" must not be \"null\"");
        }

        this.value =value;
        classObject=value.getClass();
    }


    /** This constructor allows for casting a Java object to the supplied class object which must be strictly matched.
     *  If the value is <code>null</code>, then the <code>clz2CastTo</code> argument may be of any Class.
     *
     * @param type the Class object to strictly match (can also be a class to cast to)
     * @param obj  the Java object to
     */
    public RexxStrictArgument (Class<?> type, Object value) throws BSFException
    {
        if (type==null)
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "RexxScriptArgument: argument \"type\" must not be \"null\"");
        }

        if (value==null)    // in any case o.k.
        {
            classObject=type;
            return;
        }

        if ( !type.isAssignableFrom(value.getClass()) )
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "RexxScriptArgument: argument \"value\"=["+value+"] (class=["+value.getClass()+"]) cannot be cast to argument \"type\"=["+type+"]");
        }

        classObject=type;       // now only this type gets used
        this.value =value;
    }


    /** This constructor accepts any Rexx type indicator (a String value) with its value supplied as a String:
       Converts the string from Rexx to the indicated type.

            @param rajo the <code<RexxAndJava</code> object to use for testing against the Rexx string representation for <code>null</code>

            @param type primitive type, one of
                        &quot;<strong>BO</strong>olean&quot; (i.e. '0' or '1'),
                        &quot;<strong>BY</strong>te&quot;,
                        &quot;<strong>C</strong>har&quot;,
                        &quot;<strong>D</strong>ouble&quot;,
                        &quot;<strong>F</strong>loat&quot;,
                        &quot;<strong>I</strong>nt&quot;,
                        &quot;<strong>L</strong>ong&quot;,
                        &quot;<strong>SH</strong>ort&quot;, and
                        &quot;<strong>ST</strong>ring&quot;.

            @param strValue the value to use for creating the desired type

            @param bPrimitive if <code>true</code> the primitive class object, otherwise the primitive's wrapper class object should be used

            @return value the value from Rexx (a string) to be converted to the Java type

     */
    RexxStrictArgument (RexxAndJava rajo, String type, String strValue, boolean bPrimitive) throws BSFException // this one gets used via RexxAndJava only
    {
         if (type!=null)        // only check further, if primitive type is given
         {
             switch ( Character.toUpperCase(type.charAt(0)) )
             {

                 case 'S':
                            if (type.length() == 1) break;    // leave switch
                            switch (Character.toUpperCase(type.charAt(1)))
                            {
                                case 'T':    // String
                                         value      =strValue;
                                         classObject=String.class;
                                         return;

                               case 'H':    // short: Short value
                                         value      =Short.valueOf(strValue) ;
                                         classObject=( bPrimitive ? short.class : Short.class) ;   // primitive class
                                         return;

                               default:  break;     // unknown type
                            }

                 case 'I':  // int: Integer value
                           value      =Integer.valueOf(strValue);
                           classObject=( bPrimitive ? int.class : Integer.class) ;   // primitive class
                           return;

                 case 'C':
                            // char/Character - deal with the special case that an empty string is supplied, probably
                            //                  because programmer supplied 0x00 on the Rexx side which gets transformed
                            //                  to an empty string in JNI's NewStringUTF()

                            classObject=( bPrimitive ? char.class : Character.class) ; // primitive class
                            if (strValue==null || strValue.length()==0)  // 0x00 from the Rexx side or empty string: deal with as 0x00
                            {
                                value = Character.valueOf((char)0);       // create 0x00 char
                                return;
                            }
                            value=Character.valueOf(strValue.charAt(0)) ;
                            return;

                 case 'B':  // need second character
                            if (type.length() == 1) break;    // leave switch
                            switch (Character.toUpperCase(type.charAt(1)))
                            {
                               case 'O':    // Boolean: anything but "1" or "true" is regarded to be false
                                         classObject=( bPrimitive ? boolean.class : Boolean.class); // primitive class
                                         if (strValue!=null && (strValue.equals("1") || strValue.equalsIgnoreCase("true")))
                                             value=Boolean.TRUE;
                                         else
                                             value=Boolean.FALSE;
                                         return;

                               case 'Y':    // Byte
                                         value      =Byte.valueOf( strValue );
                                         classObject=( bPrimitive ? byte.class : Byte.class );    // primitive class
                                         return;

                               default:  break;     // unknown type
                            }

                 case 'F':  value      =Float.valueOf(strValue);
                            classObject=( bPrimitive ? float.class  : Float.class );    // primitive class
                            return;

                 case 'L':  value      =Long.valueOf(strValue) ;
                            classObject=( bPrimitive ? long.class   : Long.class );     // primitive class
                            return;

                 case 'D':  value      =Double.valueOf(strValue);
                            classObject=( bPrimitive ? double.class : Double.class );     // primitive class
                            return;
             }
         }

            // if we arrive here, we have a problem ...
         throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
             "RexxStrictArgument: argument \"type\": unknown ["+type+"] or argument \"strValue\" ["+strValue+"] not compatible");
    }

    /** Return String representation, showing values of fields.
     */
    public String toString()
    {
        return "RexxStrictArgument[classObject=\""+classObject+"\",value=\""+value+"\"]";
    }

    /** Return the value part in a form that is appropriate for ooRexx, ie.
     *  Boolean values as &quot;0&quot; or &quot;1&quot;, the primitive (and String object)
     *  with their String values, any other object unedited.
     */
    public Object getValue4Rexx()
    {
        if (classObject.isPrimitive())
        {
            if (classObject==boolean.class || classObject==Boolean.class)   // do not return "false" or "true" which would be the case if applying toString()
            {
                return ((Boolean) value).booleanValue() ? "1" : "0";
            }

            return value.toString();    // return String value to Rexx
        }
        return value;   // return value unchanged
    }

}
