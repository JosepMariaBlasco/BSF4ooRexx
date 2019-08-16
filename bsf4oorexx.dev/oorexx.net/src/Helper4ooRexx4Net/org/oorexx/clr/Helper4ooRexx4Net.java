/*
   developed with: jni4net.j-0.8.8.0.jar, cf. <https://sourceforge.net/projects/jni4net/> or
   <https://github.com/jni4net/jni4net> (newer)

        F:\download\java\jni4net\bin\samples\helloWorldFromJVM\java
        F:\work\svn\bsf4oorexx\trunk\bsf4oorexx.dev\oorexx.net\
        F:\tmp\bsf4oorexx\20160530-baginski\v4rgf\samples

        CLASSPATH=%CLASSPATH%;F:\work\svn\bsf4oorexx\trunk\bsf4oorexx.dev\oorexx.net\jni4net.j-0.8.8.0.jar;F:\work\svn\bsf4oorexx\trunk\bsf4oorexx.dev\oorexx.net\oorexx.net.jar;

        F:\download\java\jni4net\bin\samples\helloWorldFromJVM\java

    suggested package to store: oorexx.net.jar

------------------------ Apache Version 2.0 license -------------------------
   Copyright 2016-2019 Rony G. Flatscher

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

    static methods:

    clr_unbox(system.Object o)   ... returns the string value (including from a System.String) of
                                a boxed value; unboxing uses a culture neutral IFormatProvider

    clr_box(String typeIndicator, String value) ... returns boxed value of typeIndicator type,
                                where typeIndicator one of: "BOolean", "BYte", "SByte", "Char",
                                "Int16", "I16", "UInt16", "UI16", "Int32", "I32", "UInt32", "UI32",
                                "Int64", "I64", "UInt64", "UI64", "SIngle", "DOuble", "DEcimal";
                                parsing to boxed value uses a culture neutral IFormatProvider

    -- not anymore! clr_box(system.Object enumType, String enumValue) ... returns an Enum value from enumType matching
                                supplied enumValue-name or enumValue-numeric value

    [Quite tricky to escape the default culture-dependent parsing and formatting of (decimal) numbers!
    Field IFormatProvider ifpCultureNeutral: used in Parse(val, ifp) and ToString(val, ifp) methods for
                                System.Decimal and for System.-primitive (not for System.Boolean and
                                System.Char) types; changing this instance will immediately have effect
                                on the Parse() and ToString() variants this utility class uses.]

    isVerbose(), setVerbose(boolean)    ... if set then each invocation of a method will cause an informal
                                message

    getPrimitiveWrapperClasses() ... returns an unmodifiable Map of the CLR wrapper types (classes)
    getCultureNeutralParseMethods() ... returns an unmodifiable Map of the Parse()-method info objects
    getCultureNeutralToStringMethods() ... returns an unmodifiable Map of the ToString()-method info objects

    ... to speed up, implemented in Java rather than in Rexx:
    getProperMethodName(system.Type t, String argName)   ... returns the properly spelled name or null
    getProperPropertyName(system.Type t, String argName) ... returns the properly spelled name or null
    getProperFieldName(system.Type t, String argName)    ... returns the properly spelled name or null

    no getProperEventName(...) as jni4net does not have direct support in the form of system.reflection.EventInfo
                               and doing it from Rexx is sufficient

    date:       2016-07-02
    version:    1.00
    changed:    2019-08-15, rgf: fixes for javadoc warnings
    author:     Rony G. Flatscher, started 2016-06-13

*/

/** Class to support dynamically typed (and maybe interpreted) languages for interacting with
 *  &quot;common language runtime (CLR)&quot; maybe a little bit easier and/or more efficient.
 *
 */

    // rgf, 20160702; "clr" for "common language runtime" in anticipation for multi-platform support
package org.oorexx.clr;

    // plain Java classes
import java.io.IOException;
import java.lang.String;
import java.util.Collections;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;

    // jni4net classes
import net.sf.jni4net.Bridge;

import system.*;        // needed to allow Java compiler to also find system.String which cannot be imported on javac 8 otherwise
import system.Decimal;
import system.Enum;
import system.IFormatProvider;
import system.Object;
import system.Type;

// import system.ValueType;
import system.reflection.Assembly;
import system.reflection.MethodInfo;
import system.reflection.PropertyInfo;
import system.reflection.FieldInfo;


/** This class helps the ooRexx dynamic language coders to box and unbox numeric values into
 *  the common language runtime (CLR) wrapper classes. This class can be used for any other such
 *  dynamic language that uses strings to represent numeric values.
 *
 *  @author Rony G. Flatscher
 *  @since 2016-06-13
 *  @version 001.201600613
 */

public class Helper4ooRexx4Net {

        /** Version field, major version before dot, date of version after dot.
         */
        public static final String version="100.20190815";

        /** Boolean field that controls whether information is given, defaults to <code>false</code>.
         */
        private static boolean bVerbose=false;

        /** Getter method for field {@link #bVerbose}.
         *
         * @return whether verbose mode is active
         */
        public static boolean isVerbose() {
            return bVerbose;
        }

        /** Setter method for field {@link #bVerbose}.
         *
         * @param verboseMode to use from now on
         */
        public static void setVerbose(boolean verboseMode) {
            bVerbose=verboseMode;
        }

        private static boolean bDebug=false;

        /** Getter method for field {@link #bDebug}.
         *
         * @return whether Debug mode is active
         */
        public static boolean isDebug() {
            return bDebug;
        }

        /** Setter method for field {@link #bDebug}.
         *
         * @param DebugMode to use from now on
         */
        public static void setDebug(boolean DebugMode) {
            bDebug=DebugMode;
        }


        /** Stores the <code>System.Type</code> objects for <code>System.Decimal</code> and
         *  all primitive Wrapper classes, ie.:
         *      <code>System.Boolean</code>,
         *      <code>System.Byte</code>,
         *      <code>System.SByte</code>,
         *      <code>System.Char</code>,
         *      <code>System.Int16</code>,
         *      <code>System.UInt16</code>,
         *      <code>System.Int32</code>,
         *      <code>System.UInt32</code>,
         *      <code>System.Int64</code>,
         *      <code>System.UInt64</code>,
         *      <code>System.Single</code>,
         *      <code>System.Double</code>.
         */
        private static Hashtable tiPrimitiveWrapperClasses                    = null;

        /** Returns an unmodifiable <code>Map</code> of {@link #tiPrimitiveWrapperClasses} to
         *  allow inspection and usage.
         *
         *  @return unmodifiable    <code>Map</code> of {@link #tiPrimitiveWrapperClasses}
         */
        public static Map getPrimitiveWrapperClasses() {
                // make sure Wrapper classes and the IFormatProvider versions of the Parse() and ToString() methods are loaded
            if (tiPrimitiveWrapperClasses==null)
            {
                loadParse_String_Methods_for_Primitives();  // load Parse(String) methods for primitive wrapper classes
            }
            return Collections.unmodifiableMap(tiPrimitiveWrapperClasses);
        }


        /** Stores the <code>System.Reflection.MethodInfo</code> objects for parsing
         *  (<code>Parse(System.String, System.IFormatProvider)</code>)
         *  decimal and primitive values in a culture neutral form for the respective
         *  wrapper class.
         */
        private static Hashtable miCultureNeutralParseMethods = null;

        /** Returns an unmodifiable <code>Map</code> of {@link #miCultureNeutralParseMethods} to
         *  allow inspection and usage.
         *
         *  @return unmodifiable    <code>Map</code> of {@link #miCultureNeutralParseMethods}
         */
        public static Map getCultureNeutralParseMethods() {
                // make sure Wrapper classes and the IFormatProvider versions of the Parse() and ToString() methods are loaded
            if (tiPrimitiveWrapperClasses==null)    // Parse(String) methods not yet loaded?
            {
                loadParse_String_Methods_for_Primitives();  // load Parse(String) methods for primitive wrapper classes
            }
            return Collections.unmodifiableMap(miCultureNeutralParseMethods);
        }


        /** Stores the <code>System.Reflection.MethodInfo</code> objects for creating strings
         *  (<code>ToString(System.IFormatProvider)</code>)
         *  from wrapped decimal and primitive values in a culture neutral form for the respective
         *  wrapper class.
         */
        private static Hashtable miCultureNeutralToStringMethods               = null;

        /** Returns an unmodifiable <code>Map</code> of {@link #miCultureNeutralToStringMethods} to
         *  allow inspection and usage.
         *
         *  @return unmodifiable    <code>Map</code> of {@link #miCultureNeutralToStringMethods}
         */
        public static Map getCultureNeutralToStringMethods() {
                // make sure Wrapper classes and the IFormatProvider versions of the Parse() and ToString() methods are loaded
            if (tiPrimitiveWrapperClasses==null)    // Parse(String) methods not yet loaded?
            {
                loadParse_String_Methods_for_Primitives();  // load Parse(String) methods for primitive wrapper classes
            }
            return Collections.unmodifiableMap(miCultureNeutralToStringMethods);
        }




        // cf. <https://msdn.microsoft.com/en-us/library/system.globalization.numberformatinfo(v=vs.110).aspx>, 20160612
        /** Culture neutral <code>IFormatProvider</code> for allowing to parse and create Rexx decimal numbers using
         *  the point (dot) character for the decimal point. Affects boxing and unboxing of "System.Decimal",
         *  "System.Single" and and "System.Double".
         *
        */
        public static IFormatProvider ifpCultureNeutral = null;


        /** <code>main</code> method.
         *
         * @param args command line arguments, not used
         * @throws IOException raised if a problem occurs while initializing the <code>Bridge</code>.
         *
         */
	public static void main(String[] args) throws IOException {
		Bridge.setVerbose(true);
//		Bridge.setDebug(true);
		Bridge.init();          // make sure bridge is initialized

                bVerbose=true;          // show methods that we use

		Console.WriteLine(Helper4ooRexx4Net.class+": version=["+version+"]\n");
                Object oha=null;

                Console.WriteLine("main(): clr_unbox(null)=["+clr_unbox(null)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("boOlean", "1");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("bo", "trUe");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("bo", "0");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("bo", "false");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");


                oha=clr_box("Byte", "255");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("SByte", "-13");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
                oha=clr_box("SByte", "13");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
                oha=clr_box("SB", "-128");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
                oha=clr_box("SB", "127");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("CHar", "$");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("Int16", "3322");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
                oha=clr_box("I16", "3322");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("UInt16", "65535");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
                oha=clr_box("UI16", "65535");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");


                oha=clr_box("Int32", "443322");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
                oha=clr_box("I32", "443322");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("UInt32", "443322");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
                oha=clr_box("UI32", "443322");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("Int64", "554433221100");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
                oha=clr_box("I64", "554433221100");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("UInt64", "554433221100");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
                oha=clr_box("UI64", "554433221100");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("Do", "1234567890.987654321");  // language neutral
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("Double", "1234567890,987654321");  // non-dot usage!
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                String strVal="123456.7890987654321";
                Console.WriteLine("main(): strVal        =["+strVal        +"]");
                oha=clr_box("decimal", strVal);
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
                Console.WriteLine("main(): oha.ToString()=["+oha.ToString()+"]");
                Console.WriteLine("main(): Decimal.ToSingle((Decimal)oha)=["+Decimal.ToSingle((Decimal)oha)+"]");
                Console.WriteLine("main(): Decimal.ToDouble((Decimal)oha)=["+Decimal.ToDouble((Decimal)oha)+"]");
                Console.WriteLine("main(): Decimal.ToUInt64((Decimal)oha)=["+Decimal.ToUInt64((Decimal)oha)+"]");
                Console.WriteLine("main(): Decimal.ToInt64((Decimal)oha)=["+Decimal.ToInt64((Decimal)oha)+"]");
                Console.WriteLine("main(): Decimal.ToInt32((Decimal)oha)=["+Decimal.ToInt32((Decimal)oha)+"]");
System.out.println("\\\\\\ --- ///");

                // Decimal d=(Decimal) clr_box("zwei", "from main(): eins");
                Decimal d=(Decimal) clr_box("De", "123987");
                Console.WriteLine("main(): clr_box(a,b)=["+d+"], d.getClass()=["+d.getClass()+"], d.GetType()=["+d.GetType()+"]");
                Console.WriteLine("main(): clr_unbox(d)=["+clr_unbox(d)+"]");
                Console.WriteLine("--- now casting to Int32:");
                int di = Decimal.ToInt32(d);
                Console.WriteLine("main(): Decimal.ToInt32(d)=di=["+di+"]");
                // Console.WriteLine("main(): clr_unbox(d)=["+clr_unbox(new java.lang.Integer(Decimal.ToInt32(d)))+"]");
System.out.println("\\\\\\ --- ///");


                oha=clr_box("string", "oh, la, la!  <---");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

                oha=clr_box("string", null);
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");
                oha=clr_box("string", "");
                Console.WriteLine("main(): clr_unbox(oha)=["+clr_unbox(oha)+"]");
System.out.println("\\\\\\ --- ///");

	}



// * Utility method for &quot;oorexx.net&quot; to turn .values of net primitive types and strings (either
// * values or system.Enum values) to plain strings, otherwise returns argument unchanged.

        /** Utility method for &quot;oorexx.net&quot; to turn values of <code>clr</code> primitive types and or System.String
         *  values to plain strings, otherwise returns argument unchanged.
         *
         * @param o a .net object of type <code>system.Object</code>
         * @return the primitive .net value or .net string as a Java String, or the argument unchanged
         *
        */
        public static java.lang.Object clr_unbox(system.Object o)
        {
            if (bVerbose)
            {
                System.err.println("<Java> ["+Helper4ooRexx4Net.class+"] -> clr_unbox(): o.toString()=["+(o==null ? null : o.toString())+"], o.GetType()=["+ (o==null ? null : o.GetType())+"]");
            }

            if (o == null)
            {
                return null;
            }

            if (o instanceof system.String)     // render system.String to string
            {
                return o.toString();
            }

            Type oType=o.GetType();             //
            String strType = oType.toString();

                // a boxed primitive value ?
            if (oType.isPrimitive() || strType.equals("System.Decimal"))
            {
                    // make sure Wrapper classes and the IFormatProvider versions of the Parse() and ToString() methods are loaded
                if (tiPrimitiveWrapperClasses==null)    // Parse(String) methods not yet loaded?
                {
                    loadParse_String_Methods_for_Primitives();  // load Parse(String) methods for primitive wrapper classes
                }

                // if (oType.toString().equals("System.Boolean"))
                if (strType.equals("System.Boolean"))
                {
                    if (o.toString().equals("True"))
                    {
                        return "1";             // ooRexx .true
                    }
                    return "0";                 // ooRexx .false value
                }
                else if (strType.equals("System.Char"))
                {
                    return o.toString();
                }

                // invariant (culture neutral) formatting
                system.Object argList1[] = new system.Object[] { (Object) ifpCultureNeutral };  // create argument array
                MethodInfo toStringMethod = (MethodInfo) miCultureNeutralToStringMethods.get(strType);
                return toStringMethod.Invoke(o, argList1).toString();   // return result
            }

            return o;                           // return received object
        }


        /** Create and return an instance of the <code>typeIndicator</code> Wrapper class representing
         *  the <code>value</code> or turn a <code>java.lang.String</code> into a <code>System.String</code>
         *  and return it.
         *
         * @param typeIndicator one of (only capital letters need to be given) "STring" (return the value
         *                  as string), or return a boxed <code>value</code> according to the
         *                  following table:
         *
         * <table align="center" border="1">
         *    <caption align="top">Values for <code>typeIndicator</code> argument and the CLR class
         *                  to which to box.</caption>
         *    <tr>
         *       <th>typeIndicator</th>
         *       <th>returns boxed value of type</th>
         *    </tr>
         *                <tr><td> &quot;BOolean&quot; </td><td> &quot;System.Boolean&quot; </td></tr>
         *                <tr><td> &quot;BYte&quot;    </td><td> &quot;System.Byte&quot;    </td></tr>
         *                <tr><td> &quot;Char&quot;    </td><td> &quot;System.Char&quot;    </td></tr>
         *                <tr><td> &quot;DEcimal&quot; </td><td> &quot;System.Decimal&quot; </td></tr>
         *                <tr><td> &quot;DOuble&quot;  </td><td> &quot;System.Double&quot;  </td></tr>
         *                <tr><td> &quot;I16&quot;     </td><td> &quot;System.Int16&quot;   </td></tr>
         *                <tr><td> &quot;I32&quot;     </td><td> &quot;System.Int32&quot;   </td></tr>
         *                <tr><td> &quot;I64&quot;     </td><td> &quot;System.Int64&quot;   </td></tr>
         *                <tr><td> &quot;INT16&quot;   </td><td> &quot;System.Int16&quot;   </td></tr>
         *                <tr><td> &quot;INT32&quot;   </td><td> &quot;System.Int32&quot;   </td></tr>
         *                <tr><td> &quot;INT64&quot;   </td><td> &quot;System.Int64&quot;   </td></tr>
         *                <tr><td> &quot;SByte&quot;   </td><td> &quot;System.SByte&quot;   </td></tr>
         *                <tr><td> &quot;SIngle&quot;  </td><td> &quot;System.Single&quot;  </td></tr>
         *                <tr><td> &quot;STring&quot;  </td><td> &quot;System.String&quot;  </td></tr>
         *                <tr><td> &quot;UI16&quot;    </td><td> &quot;System.UInt16&quot;  </td></tr>
         *                <tr><td> &quot;UI32&quot;    </td><td> &quot;System.UInt32&quot;  </td></tr>
         *                <tr><td> &quot;UI64&quot;    </td><td> &quot;System.UInt64&quot;  </td></tr>
         *                <tr><td> &quot;UINT16&quot;  </td><td> &quot;System.UInt16&quot;  </td></tr>
         *                <tr><td> &quot;UINT32&quot;  </td><td> &quot;System.UInt32&quot;  </td></tr>
         *                <tr><td> &quot;UINT64&quot;  </td><td> &quot;System.UInt64&quot;  </td></tr>
         * </table>
         *
         * @param value a string containing the value to be boxed
         *
         * @return returns boxed value, in the case of a string the value as a <code>system.String</code> object, <code>null</code> if
         *                 <code>null</code> was supplied as the <code>value</code> parameter or the boxed
         *                 object.
         *
         */
        public static Object clr_box(String typeIndicator, String value)
        {
            if (bVerbose || bDebug)
            {
                System.err.println("<Java> ["+Helper4ooRexx4Net.class+"] -> clr_box(): typeIndicator=["+typeIndicator+"], value=["+value+"]");
            }
/*
            ST[ring]                                "System.String"

            BO[oolean]                              "System.Boolean"
               ... make sure that .false=="0"=="False" and .true=="1"=="True" get all processed

            DE[cimal]                               "System.Decimal"
            BY[te]                                  "System.Byte"
            SB[yte]                                 "System.SByte"
            C[har]                                  "System.Char"
            INT16  | I16                            "System.Int16"
            UINT16 | UI16                           "System.UInt16"
            INT32  | I32                            "System.Int32"
            UINT32 | UI32                           "System.UInt32"
            INT64  | I64                            "System.Int64"
            UINT64 | UI64                           "System.UInt64"
            SI[ngle]                                "System.Single"
            ST[ring]                                "System.String"
            DO[uble]                                "System.Double"
*/
            if (value==null)                        // pass back
            {
                return null;
            }

            String strTypeIndicator=typeIndicator.toUpperCase();
            system.String csValue = Bridge.convert(value);   // turn value into a system.String

            if (strTypeIndicator.startsWith("ST"))    // convert to a "System.String"
            {
                return csValue;                      // return a "System.String"
            }

                // make sure Wrapper classes and the IFormatProvider versions of the Parse() and ToString() methods are loaded
            if (tiPrimitiveWrapperClasses==null)    // Parse(String) methods not yet loaded?
            {
                loadParse_String_Methods_for_Primitives();  // load Parse(String) methods for primitive wrapper classes
            }

            MethodInfo parseMethod  = null;         // the static Parse(String) method of a primitive Wrapper type
            system.Object argList2[] = new system.Object[] { csValue, (Object) ifpCultureNeutral }; // create argument array
            String   strWrapperType = null;

// System.out.println("strTypeIndicator=["+strTypeIndicator+"], .startsWith(\"DE\")=["+strTypeIndicator.startsWith("DE")+"]");

            if (strTypeIndicator.startsWith("DE"))    // box and return as a "System.Decimal"
            {
                strWrapperType="System.Decimal";
            }

            else if (strTypeIndicator.startsWith("BO"))   // box and return as a "System.Boolean"
            {
                parseMethod = (MethodInfo) miCultureNeutralParseMethods.get("System.Boolean");
                if (value.equals("0"))  // the ooRexx .false value
                {
                    return parseMethod.Invoke(null, new system.Object[] { Bridge.convert("False") });
                }

                if (value.equals("1"))  // the ooRexx .true value
                {
                    return parseMethod.Invoke(null, new system.Object[] { Bridge.convert("True") });
                }

                // let System.Boolean do the checking
                return parseMethod.Invoke(null, new system.Object[] {csValue});
            }

            else if (strTypeIndicator.startsWith("BY"))   // box and return as a "System.Byte"
            {
                strWrapperType="System.Byte";
            }

            else if (strTypeIndicator.startsWith("SB"))   // box and return as a "System.SByte"
            {
                strWrapperType="System.SByte";
            }

            else if (strTypeIndicator.startsWith("C"))     // box and return as a "System.Char"
            {
                parseMethod = (MethodInfo) miCultureNeutralParseMethods.get("System.Char");
                return parseMethod.Invoke(null, new system.Object[] {csValue});
            }

            else if (strTypeIndicator.equals("INT16") || strTypeIndicator.equals("I16"))     // box and return as a "System.Ínt16"
            {
                strWrapperType="System.Int16";
            }

            else if (strTypeIndicator.equals("UINT16") || strTypeIndicator.equals("UI16"))   // box and return as a "System.UÍnt16"
            {
                strWrapperType="System.UInt16";
            }

            else if (strTypeIndicator.equals("INT32") || strTypeIndicator.equals("I32"))     // box and return as a "System.Ínt32"
            {
                strWrapperType="System.Int32";
            }

            else if (strTypeIndicator.equals("UINT32") || strTypeIndicator.equals("UI32"))   // box and return as a "System.UÍnt32"
            {
                strWrapperType="System.UInt32";
            }

            else if (strTypeIndicator.equals("INT64") || strTypeIndicator.equals("I64"))     // box and return as a "System.Ínt64"
            {
                strWrapperType="System.Int64";
            }

            else if (strTypeIndicator.equals("UINT64") || strTypeIndicator.equals("UI64"))   // box and return as a "System.UÍnt64"
            {
                strWrapperType="System.UInt64";
            }

            else if (strTypeIndicator.startsWith("SI"))     // box and return as a "System.Single"
            {
                strWrapperType="System.Single";
            }

            else if (strTypeIndicator.startsWith("DO"))     // box and return as a "System.Double"
            {
                strWrapperType="System.Double";
            }
                // fetch appropriate static Parse(String) method
// System.out.println("strWrapperType=["+strWrapperType+"] | typeIndicator=["+typeIndicator+"], value=["+value+"]");

            if (strWrapperType==null || !miCultureNeutralParseMethods.containsKey(strWrapperType))       // unknown type !
            {
                throw new RuntimeException("illegal typeIndicator: ["+typeIndicator+"] (supplied value: ["+value+"])");
            }

// System.out.println("   -->  method=["+miCultureNeutralParseMethods.get(strWrapperType)+"]");

            parseMethod = (MethodInfo) miCultureNeutralParseMethods.get(strWrapperType);
            return parseMethod.Invoke(null, argList2);   // return result
        }

/*
        / ** Returns the <code>Enum</code> object representing the <code>enumValue</code> from the supplied
         *  <code>enumType</code>.
         *
         * @param enumType the <code>System.Enum</code> type to use
         * @param enumValue the name or numeric value as defined in the type
         *
         * @return the <code>System.Enum</code> value representing the supplied name or numeric value
         * /
        public static Object clr_box(system.Object enumType, String enumValue)
        {
            if (bVerbose)
            {
                System.err.println("<Java> ["+Helper4ooRexx4Net.class+"] -> clr_box(): enumType=["+enumType+"], enumValue=["+enumValue+"]");
            }

            Type argEnumType =  null;
            if (enumType instanceof Type)               // a type object, cast it accordingly
            {
                argEnumType=(Type) enumType;
            }
            else if (enumType instanceof system.Enum)   // a System.Enum instance, get its type object
            {
                argEnumType=enumType.GetType();
            }
            else
            {
                // oops, not System.Enum, hence an error !
                throw new RuntimeException("illegal enumType: ["+enumType+"] (supplied enumValue: ["+enumValue+"]) - neither a System.Enum, nor a System.Enum.GetType() value");
            }

            return Enum.Parse(argEnumType, enumValue, true);    // ignore case
        }
*/

        /**  Loads all .Net primitive Wrapper types' methods Parse(System.String) and stores them
         *   in a <code>Hashtable</code> by their fully qualified type name.
         */
        private static void loadParse_String_Methods_for_Primitives()
        {
            if (bVerbose)
            {
                System.err.println("<Java> ["+Helper4ooRexx4Net.class+"] -> loadParse_String_Methods_for_Primitives()");
            }
            tiPrimitiveWrapperClasses                    = new Hashtable();    // create Hashtable to store Type objects
            miCultureNeutralParseMethods = new Hashtable();    // create Hashtable to store MethodInfo objects
            miCultureNeutralToStringMethods               = new Hashtable();

            String primClassNames[]=new String[] {  // define primitive Wrapper types to support
                "System.Boolean",
                "System.Byte",  "System.SByte",
                "System.Char",
                "System.Int16", "System.UInt16",
                "System.Int32", "System.UInt32",
                "System.Int64", "System.UInt64",
                "System.Single",
                "System.Double",
                "System.Decimal"
            };

            Type           st = system.Object.typeof();
            system.Object  so = st.getAssembly().CreateInstance("System.Globalization.NumberFormatInfo");
            ifpCultureNeutral = Bridge.cast(so, IFormatProvider.class);

            // System.Boolean, System.Char
            Type typeListParse1[]= new Type[] { system.String.typeof() }; // type list for arguments for the method we need
            Type typeListToString1[] = new Type[] {};

            // all other primitives have IFormatProvider versions of Parse() and ToString() which we need for
            // culture neutral processing of Rexx values
            Type typeListParse2[]= new Type[] { system.String.typeof(), ((system.Object) ifpCultureNeutral).GetType() }; // type list for arguments for the method we need
            Type typeListToString2[] = new Type[] { ((system.Object) ifpCultureNeutral).GetType() };

            for (int i=0; i<primClassNames.length; i++)
            {
                Type tmpType=st.GetType(primClassNames[i]);

                    // save Type object
                tiPrimitiveWrapperClasses.put(primClassNames[i], tmpType);

                    // get MethodInfo for this primitive Wrapper type's Parse(String) method
                    // not using generics in order to allow compilation for Java 1.4 (sic! as of 2016-06-12)
                    // Hashtable will create an exception if key or value are null
                try {
                    miCultureNeutralParseMethods.put(primClassNames[i],
                                                              tmpType.GetMethod("Parse", typeListParse2));

                    miCultureNeutralToStringMethods.put(primClassNames[i],
                                                              tmpType.GetMethod("ToString", typeListToString2));
                }
                catch (java.lang.Exception e)  // System.Boolean, System.Char
                {
                    miCultureNeutralParseMethods.put(primClassNames[i],
                                                              tmpType.GetMethod("Parse", typeListParse1));
                    miCultureNeutralToStringMethods.put(primClassNames[i],
                                                              tmpType.GetMethod("ToString", typeListToString1));
                }
            }
        }



        /** Searches a method in the supplied type which matches caselessly the supplied needle and returns
         *  the spelling found in the <code>MethodInfo</code> object.
         *
         * @param t the <code>system.Type</code> object to look for the method's name
         * @param argName the name of a method we seek
         *
         * @return the properly spelled name of the method or <code>null</code> if none was found
         */
        public static java.lang.String getMethodName (system.Type t, String argName)
        {
            if (bVerbose)
            {
                System.err.print("<Java> ["+Helper4ooRexx4Net.class+"] -> getMethodName(): t=["+t+"] for ["+argName+"]");
                if (bDebug)     // displaying all object's names we compare to?
                {
                    System.err.println();
                }
            }

            if (argName==null)
            {
                return null;
            }

            String strName  = null;
            MethodInfo mi[] = t.GetMethods();

            for (int i=0; i<mi.length; i++)
            {
                if (bDebug)
                {
                    System.err.println("Java. seeking ... argName=["+argName+"] i=["+i+"] ... getMethodName()=["+mi[i]+"]");
                }

                strName=mi[i].getName();
                if (argName.equalsIgnoreCase(strName))    // equals (caseless) spelling ?
                {
                    if (bDebug)
                    {
                        System.err.println("Java. ... FOUND! argName=["+argName+"] ... getMethodName()=mi["+i+"], strName=["+strName+"] FOUND!\n");
                    }
                    else if (bVerbose)
                    {
                        System.err.println(", returning: ["+strName+"]\n");
                    }
                    return strName; // return method name in the found case
                }
            }

            if (bVerbose || bDebug)
            {
                if (bDebug)
                {
                    System.err.println("Java. ... NOT found! argName=["+argName+"] ... NOT FOUND, returning [null]\n");
                }
                else  // bVerbose only
                {
                    System.err.println(", returning method name: ["+null+"]\n");
                }
            }

            return null;        // not found
        }



        /** Searches a property in the supplied type which matches caselessly the supplied needle and returns
         *  the spelling found in the <code>PropertyInfo</code> object.
         *
         * @param t the <code>system.Type</code> object to look for the property's name
         * @param argName the name of a property we seek
         *
         * @return the properly spelled name of the property or <code>null</code> if none was found
         */
        public static java.lang.String getPropertyName (system.Type t, String argName)
        {
            if (bVerbose)
            {
                System.err.print("<Java> ["+Helper4ooRexx4Net.class+"] -> getPropertyName(): t=["+t+"] for ["+argName+"]");
                if (bDebug)     // displaying all object's names we compare to?
                {
                    System.err.println();
                }
            }

            if (argName==null)
            {
                return null;
            }

            String strName  = null;
            PropertyInfo mi[] = t.GetProperties();

            for (int i=0; i<mi.length; i++)
            {
                if (bDebug)
                {
                    System.err.println("Java. seeking ... argName=["+argName+"] i=["+i+"] ... getPropertyName()=["+mi[i]+"]");
                }

                strName=mi[i].getName();
                if (argName.equalsIgnoreCase(strName))    // equals (caseless) spelling ?
                {
                    if (bDebug)
                    {
                        System.err.println("Java. ... FOUND! argName=["+argName+"] ... getPropertyName()=mi["+i+"], strName=["+strName+"] FOUND!\n");
                    }
                    else if (bVerbose)
                    {
                        System.err.println(", returning: ["+strName+"]\n");
                    }
                    return strName; // return method name in the found case
                }
            }

            if (bVerbose || bDebug)
            {
                if (bDebug)
                {
                    System.err.println("Java. ... NOT found! argName=["+argName+"] ... NOT FOUND, returning [null]\n");
                }
                else
                {
                    System.err.println(", returning property name: ["+null+"]\n");
                }
            }

            return null;        // not found
        }


        /** Searches a field in the supplied type which matches caselessly the supplied needle and returns
         *  the spelling found in the <code>FieldInfo</code> object.
         *
         * @param t the <code>system.Type</code> object to look for the field's name
         * @param argName the name of a field we seek
         *
         * @return the properly spelled name of the field or <code>null</code> if none was found
         */
        public static java.lang.String getFieldName (system.Type t, String argName)
        {
            if (bVerbose)
            {
                System.err.print("<Java> ["+Helper4ooRexx4Net.class+"] -> getFieldName(): t=["+t+"] for ["+argName+"]");
                if (bDebug)     // displaying all object's names we compare to?
                {
                    System.err.println();
                }
            }

            if (argName==null)
            {
                return null;
            }

            String strName  = null;
            FieldInfo mi[] = t.GetFields();

            for (int i=0; i<mi.length; i++)
            {
                if (bDebug)
                {
                    System.err.println("Java. seeking ... argName=["+argName+"] i=["+i+"] ... getFieldName()=["+mi[i]+"]");
                }

                strName=mi[i].getName();
                if (argName.equalsIgnoreCase(strName))    // equals (caseless) spelling ?
                {
                    if (bDebug)
                    {
                        System.err.println("Java. ... FOUND! argName=["+argName+"] ... getFieldName()=mi["+i+"], strName=["+strName+"] FOUND!\n");
                    }
                    else if (bVerbose)
                    {
                        System.err.println(", returning: ["+strName+"]\n");
                    }
                    return strName; // return method name in the found case
                }
            }

            if (bVerbose || bDebug)
            {
                if (bDebug)
                {
                    System.err.println("Java. ... NOT found! argName=["+argName+"] ... NOT FOUND, returning [null]\n");
                }
                else
                {
                    System.err.println(", returning field name: ["+null+"]\n");
                }
            }

            return null;        // not found
        }

}


