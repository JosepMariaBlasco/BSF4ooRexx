//   last change: $Revision: 567 $ $Author: rony $ $Date: 2010-02-16 21:48:59 +0100 (Tue, 16 Feb 2010) $
//   ---rgf, added infos on UNO_PROPERTY modifier values in the javadoc
//   ---rgf, adds datatype to individual constant values, cf. <com.sun.star.packages.zip.ZipConstants>

package org.oorexx.uno;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;

import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.comp.helper.Bootstrap;

import com.sun.star.container.XHierarchicalNameAccess;

import com.sun.star.frame.XComponentLoader;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XServiceName;
import com.sun.star.lang.XTypeProvider;

import com.sun.star.reflection.TypeDescriptionSearchDepth;
import com.sun.star.reflection.XIdlClass;
import com.sun.star.reflection.XIdlField2;
import com.sun.star.reflection.XIdlField;
import com.sun.star.reflection.XIdlMember;
import com.sun.star.reflection.XIdlMethod;
import com.sun.star.reflection.XIdlReflection;

import com.sun.star.reflection.XMethodParameter;

import com.sun.star.reflection.XCompoundTypeDescription;
import com.sun.star.reflection.XConstantTypeDescription;
import com.sun.star.reflection.XConstantsTypeDescription;
import com.sun.star.reflection.XEnumTypeDescription;
import com.sun.star.reflection.XIndirectTypeDescription;
import com.sun.star.reflection.XInterfaceAttributeTypeDescription;
import com.sun.star.reflection.XInterfaceMemberTypeDescription;
import com.sun.star.reflection.XInterfaceMemberTypeDescription;
import com.sun.star.reflection.XInterfaceMethodTypeDescription;
import com.sun.star.reflection.XInterfaceTypeDescription;
import com.sun.star.reflection.XModuleTypeDescription;
import com.sun.star.reflection.XPropertyTypeDescription;
import com.sun.star.reflection.XServiceTypeDescription;
import com.sun.star.reflection.XSingletonTypeDescription;
import com.sun.star.reflection.XTypeDescription;
import com.sun.star.reflection.XTypeDescriptionEnumeration;
import com.sun.star.reflection.XTypeDescriptionEnumerationAccess;

import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.TypeClass;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XCurrentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.uno.XNamingService;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;


/** This class allows reflecting UNO objects and types using the type descriptions and
 *  usually returns results as strings only (to facilitate interaction with
 *  scripting languages); originally devised for Open Object Rexx (cf.
 *  <a href="http://www.ooRexx.org">http://www.ooRexx.org</a>).
 *
 * <p>
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2006-2011 Rony G. Flatscher
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ----------------------------------------------------------------------------- </pre>
 *
 * @author Rony G. Flatscher
 * @version 1.0.3, 2011-02-15
 */


/*
    2007-07-07, ---rgf, fixed Java-type hints to correctly list the primitive Java type for
                        UNO primitive datatypes; for properties the primitive datatypes need
                        to be boxed, hence the fully qualified Java class names for boxing
                        the primitive datatypes are now given
                        "<"$rev">"

    2008-01-01, ---rgf, added option "" to add datatype to individual constant values,
                        as the datatype may differ among the constants of the same
                        constant group (cf. <com.sun.star.packages.zip.ZipConstants>) !

    2011-02-15, ---rgf, just added "this" to error message, such that the source of this
                        message can be immediately known;
                        made static field bDebug public, such that temporarily debug messgaes
                        can be turned on
    2011-02-22, ---rgf, needed to change "this" to "RgfReflectUNO.class", because
                        there is no "this" in static methods
*/

public class RgfReflectUNO // TestRgf
{
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static public String version = "103.20110215";

/*
    / ** If set to <code>true</code> (default value) then for each individual constant
     *  its datatype will be given (appending "|" after the constant value and denoting
     *  the type name).
     *
     * /
    static public boolean bSupplyDatatypeForEachConstant=true;
*/

    private RgfReflectUNO() {}; // make constructor private (no instances allowed)


    /** Controls issuing some debug messages, if set to <code>true</code>. Default value:
     *  <code>false</code>.
     */
    static public boolean bDebug = false;   // publically accessible field

    static private int maxModifierValue = 0;    // maximum modifier value

    static  XComponentContext      context=null;    // default context
    static boolean isContextAvailable=false;        // indicates whether context was made available already

    static  XMultiComponentFactory xmcf=null;

    static XIdlReflection                    theCoreReflection=null;
    static XMultiComponentFactory            theServiceManager=null;
    static XHierarchicalNameAccess           theTypeDescriptionManager=null;

    static Hashtable ht= new Hashtable();   // TypeClasses
    static Hashtable ht2j= new Hashtable();   // datatype TypeClasses mapping to Java datatypes
    static Hashtable ht2j_prop= new Hashtable();   // datatype TypeClasses mapping to Java datatypes
    static Hashtable pa= new Hashtable();   // PropertyAttributes

        // strings to use for encoding the following UNO information
    static private final String ENCODE_CONSTANTS_KEYWORD = "UNO_CONSTANTS";
    static private final String ENCODE_ENUM_KEYWORD      = "UNO_ENUM";
    static private final String ENCODE_EXCEPTION_KEYWORD = "UNO_EXCEPTION";
    static private final String ENCODE_INTERFACE_KEYWORD = "UNO_INTERFACE";
    static private final String ENCODE_MODULE_KEYWORD    = "UNO_MODULE";
    static private final String ENCODE_SERVICE_KEYWORD   = "UNO_SERVICE";
    static private final String ENCODE_SINGLETON_KEYWORD = "UNO_SINGLETON";
    static private final String ENCODE_STRUCT_KEYWORD    = "UNO_STRUCT";
    static private final String ENCODE_TYPEDEF_KEYWORD   = "UNO_TYPEDEF";

    static private final String ENCODE_ATTRIBUTE_KEYWORD = "UNO_ATTRIBUTE";
    static private final String ENCODE_METHOD_KEYWORD    = "UNO_METHOD";
    static private final String ENCODE_PROPERTY_KEYWORD  = "UNO_PROPERTY";


    static {
            // define TypeClasses, referrals are *not* of type int (UNO long), but are represented as Java objects!
        ht.put( TypeClass.ANY                   , "UNO_ANY"                  );
        ht.put( TypeClass.ARRAY                 , "UNO_ARRAY"                );
        ht.put( TypeClass.BOOLEAN               , "UNO_BOOLEAN"              );
        ht.put( TypeClass.BYTE                  , "UNO_BYTE"                 );
        ht.put( TypeClass.CHAR                  , "UNO_CHAR"                 );
        ht.put( TypeClass.CONSTANT              , "UNO_CONSTANT"             );
        ht.put( TypeClass.CONSTANTS             , "UNO_CONSTANTS"            );
        ht.put( TypeClass.DOUBLE                , "UNO_DOUBLE"               );
        ht.put( TypeClass.ENUM                  , "UNO_ENUM"                 );
        ht.put( TypeClass.EXCEPTION             , "UNO_EXCEPTION"            );
        ht.put( TypeClass.FLOAT                 , "UNO_FLOAT"                );
        ht.put( TypeClass.HYPER                 , "UNO_HYPER"                );     // 64 bits, Java: long
        ht.put( TypeClass.INTERFACE             , "UNO_INTERFACE"            );
        ht.put( TypeClass.INTERFACE_ATTRIBUTE   , "UNO_INTERFACE_ATTRIBUTE"  );
        ht.put( TypeClass.INTERFACE_METHOD      , "UNO_INTERFACE_METHOD"     );
        ht.put( TypeClass.LONG                  , "UNO_LONG"                 );     // 32 bits, Java: int
        ht.put( TypeClass.MODULE                , "UNO_MODULE"               );
        ht.put( TypeClass.PROPERTY              , "UNO_PROPERTY"             );
        ht.put( TypeClass.SEQUENCE              , "UNO_SEQUENCE"             );
        ht.put( TypeClass.SERVICE               , "UNO_SERVICE"              );
        ht.put( TypeClass.SHORT                 , "UNO_SHORT"                );     // 16 bits, Java: short
        ht.put( TypeClass.SINGLETON             , "UNO_SINGLETON"            );
        ht.put( TypeClass.STRING                , "UNO_STRING"               );
        ht.put( TypeClass.STRUCT                , "UNO_STRUCT"               );
        ht.put( TypeClass.TYPE                  , "UNO_TYPE"                 );
        ht.put( TypeClass.TYPEDEF               , "UNO_TYPEDEF"              );
        ht.put( TypeClass.UNION                 , "UNO_UNION"                );
        ht.put( TypeClass.UNKNOWN               , "UNO_UNKNOWN"              );
        ht.put( TypeClass.UNSIGNED_HYPER        , "UNO_UNSIGNED_HYPER"       );     // 64 bits, Java: long
        ht.put( TypeClass.UNSIGNED_LONG         , "UNO_UNSIGNED_LONG"        );     // 32 bits, Java: int
        ht.put( TypeClass.UNSIGNED_SHORT        , "UNO_UNSIGNED_SHORT"       );     // 16 bits, Java: short
        ht.put( TypeClass.VOID                  , "UNO_VOID"                 );

            // define TypeClasses, that have a Java primitive datatype counterpart
        // ht2j.put( TypeClass.ANY                   , "com.sun.star.uno.Any-or-java.lang.Object"  );  // com.sun.star.uno.Any or java.lang.Object
        ht2j.put( TypeClass.BOOLEAN               , "boolean"                  );
        ht2j.put( TypeClass.BYTE                  , "byte"                     );
        ht2j.put( TypeClass.CHAR                  , "char"                     );
        ht2j.put( TypeClass.DOUBLE                , "double"                   );
        ht2j.put( TypeClass.FLOAT                 , "float"                    );
        ht2j.put( TypeClass.HYPER                 , "long"                     );     // 64 bits, Java: long
        ht2j.put( TypeClass.LONG                  , "int"                      );     // 32 bits, Java: int
        ht2j.put( TypeClass.SHORT                 , "short"                    );     // 16 bits, Java: short
        ht2j.put( TypeClass.STRING                , "java.lang.String"         );
        ht2j.put( TypeClass.UNSIGNED_HYPER        , "long"                     );     // 64 bits, Java: long
        ht2j.put( TypeClass.UNSIGNED_LONG         , "int"                      );     // 32 bits, Java: int
        ht2j.put( TypeClass.UNSIGNED_SHORT        , "short"                    );     // 16 bits, Java: short
        ht2j.put( TypeClass.VOID                  , "void"                     );

            // define those property TypeClasses, that must be boxed in Java
        // ht2j_prop.put( TypeClass.ANY              , "java.lang.Object"         );  // com.sun.star.uno.Any or java.lang.Object
        ht2j_prop.put( TypeClass.BOOLEAN          , "java.lang.Boolean"        );
        ht2j_prop.put( TypeClass.BYTE             , "java.lang.Byte"           );
        ht2j_prop.put( TypeClass.CHAR             , "java.lang.Char"           );
        ht2j_prop.put( TypeClass.DOUBLE           , "java.lang.Double"         );
        ht2j_prop.put( TypeClass.FLOAT            , "java.lang.Float"          );
        ht2j_prop.put( TypeClass.HYPER            , "java.lang.Long"           );     // 64 bits, Java: long
        ht2j_prop.put( TypeClass.LONG             , "java.lang.Integer"        );     // 32 bits, Java: int
        ht2j_prop.put( TypeClass.SHORT            , "java.lang.Short"          );     // 16 bits, Java: short
        ht2j_prop.put( TypeClass.STRING           , "java.lang.String"         );
        ht2j_prop.put( TypeClass.UNSIGNED_HYPER   , "java.lang.Long"           );     // 64 bits, Java: long
        ht2j_prop.put( TypeClass.UNSIGNED_LONG    , "java.lang.Integer"        );     // 32 bits, Java: int
        ht2j_prop.put( TypeClass.UNSIGNED_SHORT   , "java.lang.Short"          );     // 16 bits, Java: short
        ht2j_prop.put( TypeClass.VOID             , "java.lang.Void"           );

/* from RGF's reference card:

    UNO Datatype                Java Datatype
    UNO_ANY                     com.sun.star.uno.Any or java.lang.Object
    UNO_VOID                    void
    UNO_BOOLEAN                 boolean
    UNO_BYTE (8-bit)            byte
    UNO_CHAR (16-bit)           char
    UNO_SHORT (16-bit)          short
    UNO_UNSIGNED_SHORT (16-bit) short
    UNO_LONG (32-bit)           int
    UNO_UNSIGNED_LONG (32-bit)  int
    UNO_HYPER (64-bit)          long
    UNO_UNSIGNED_HYPER (64-bit) long
    UNO_FLOAT                   float
    UNO_DOUBLE                  double
*/




            // define possible property attributes
        pa.put( new Integer(1  )  ,  "MAYBEVOID"      );
        pa.put( new Integer(2  )  ,  "BOUND"          );
        pa.put( new Integer(4  )  ,  "CONSTRAINED"    );
        pa.put( new Integer(8  )  ,  "TRANSIENT"      );
        pa.put( new Integer(16 )  ,  "READONLY"       );
        pa.put( new Integer(32 )  ,  "MAYBEAMBIGUOUS" );
        pa.put( new Integer(64 )  ,  "MAYBEDEFAULT"   );
        pa.put( new Integer(128)  ,  "REMOVEABLE"     );
        pa.put( new Integer(256)  ,  "OPTIONAL"       );
        maxModifierValue=256;
    }


/*
    / ** Uses reflection to set up the hashtable containing all possible property attributes. Hence,
     *  should there be an extension in the future, this class still would work.
     * /
    static private void setUpPropertyAttributesHashTable()
    {
            // get the definitions of the property attributes via reflection
        String propAttrDef="com.sun.star.beans.PropertyAttribute";


        // Object array having object o's XIdlClass or XTypeDescription at index [0],
        //         object o's TypeClass at index [1], received "o" at index [2], free slot at index [3]
        Object [] tcInfo=determineTypeClass(propAttrDef); // [0]...XIdlClass|XTypeDescription, [1]...TypeClass

        XTypeDescription xtd=(XTypeDescription) tcInfo[0];
        if (xtd!=null)      // no XTypeDescriptor, nothing to do
        {
                // define lead-in, denote fully qualifed name
            // sb.append("CONSTANTS"+"|").append(xtd.getName()).append("|");

            XConstantsTypeDescription xitd = (XConstantsTypeDescription)
                UnoRuntime.queryInterface(XConstantsTypeDescription.class, xtd);

            if (xitd!=null)         // constant type descriptions available
            {
                XConstantTypeDescription ximtd [] = xitd.getConstants();
                if (ximtd!=null)    // constants available
                {
                    Object tstObj=ximtd[0].getConstantValue();  // determine datatype
                    boolean isAny=(tstObj instanceof Any);

                    // -------------------->
                    String type="", constName="";
                    String constVal=null;
                    int intVal=0;
                    for (int i=0; i<ximtd.length; i++)
                    {
                        // name = ximtd[i].getName();
                        constName = getUnqualifiedName(ximtd[i].getName());

                        if (isAny)
                        {
                            try
                            {
                                Any anyObj=(Any) ximtd[i].getConstantValue();
                                intVal=AnyConverter.toInt(anyObj);
                                if (intVal>maxModifierValue)
                                {
                                    maxModifierValue=intVal;
                                }
                                constVal=""+AnyConverter.toInt(anyObj);    // get and convert value
                            }
                            catch (Exception e)
                            {
                                continue;       // work with next one
                            }
                        }
                        else
                        {
                            intVal=new Integer(ximtd[i].getConstantValue()+"").intValue();
                            if (intVal>maxModifierValue)
                            {
                                maxModifierValue=intVal;
                            }
                            constVal=""+ximtd[i].getConstantValue();
                        }
                        pa.put( new Integer(constVal), constName);
                    }
                }
            }
        }
    }
*/




    /** Returns the value of the current XComponentObject in use, or null, if not set yet.
     *
     * @return The component context object that is used for this class or <code>null</code>, if not set.
     */
    static public XComponentContext getContext()
    {
        return context;
    }


    /** Sets the component context and gets singletons and type descriptions from it.
     *  (Will invoke the private method <code>&quot;setUpPropertyAttributesHashTable()&quot;</code>.)
     *
     * @param ctxt component context object to be used for this class, if <code>null</code>, then
     *            a default component context is created using &quot;com.sun.star.comp.helper.Bootstrap.bootstrap()&quot;.
     *
     * @return The component context object that is used for this class.
     */
    static public XComponentContext setContext(XComponentContext ctxt)    // connect()
    {
        try     // create a default connection
        {
            if (ctxt==null && isContextAvailable==false)
            {
                context=Bootstrap.bootstrap();  // create and get the default context
            }
            else if (ctxt!=null)         // a context was received, use it
            {
                if (UnoRuntime.areSame(context, ctxt))   // already in hand, everything got set up already
                    return ctxt;
                context=ctxt;            // assign new context object
            }
            isContextAvailable=true;    // indicate that we have a context !

            xmcf=context.getServiceManager();       // get the XMultiComponentFactory from the context

                // get singletons from context
            theCoreReflection=(XIdlReflection) UnoRuntime.queryInterface(
                    XIdlReflection.class,
                    context.getValueByName("/singletons/com.sun.star.reflection.theCoreReflection"));

            theServiceManager=(XMultiComponentFactory) UnoRuntime.queryInterface(
                    XMultiComponentFactory.class,
                    context.getValueByName("/singletons/com.sun.star.lang.theServiceManager"));

            theTypeDescriptionManager=(XHierarchicalNameAccess) UnoRuntime.queryInterface(
                    XHierarchicalNameAccess.class,
                    context.getValueByName("/singletons/com.sun.star.reflection.theTypeDescriptionManager"));
        }
        catch (Exception e)
        {
            say(RgfReflectUNO.class+": sch... UNEXPECTED error, cannot bootstrap and/or retrieve singletons!");
            e.printStackTrace();
        }

        // setUpPropertyAttributesHashTable();     // set up the Hashtable containing all property attributes

        return context;
     }




    /** Determines the XIdlClass/XTypeDescription and TypeClass of the supplied UNO object/UNOIDL string.
     *
     * @param o UNO object or String fully qualifying the module name
     * @return Object array having object o's XIdlClass or XTypeDescription at index [0],
     *         object o's TypeClass at index [1], received "o" at index [2], free slot at index [3]
     *
     */
    static Object [] determineTypeClass(Object o)
    {
        Object [] res=new Object[4];
        res[2]=o;                       // save "o" in array

        XIdlClass xidl=null;            // XIdlClass of object
        boolean   bIsString=false;

        if (o==null)        // no object given
        {
            return res;
        }

        if (o instanceof String)        // received a string ?
        {
            Object obj=null;

            try
            {
                obj=theTypeDescriptionManager.getByHierarchicalName(check4reflection((String) o));
            }
            catch (Exception e)
            {
                return res;      // giving in: not found
            }

            XTypeDescription xtd=(XTypeDescription) UnoRuntime.queryInterface(XTypeDescription.class,obj);
            res[0]=xtd;                 // in this case a XTypeDescription in hand
            res[1]=xtd.getTypeClass();
        }
        else                            // received some object, probably an UNO object
        {
            xidl=theCoreReflection.getType(o);
            res[0]=xidl;

            // if an XInterface, then use the type provider to figure out whether it is a service or not !
            if (xidl instanceof XInterface)
            {
                    // a service object in hand ?
                XServiceInfo xsi=(XServiceInfo) UnoRuntime.queryInterface(XServiceInfo.class, o);
                if (xsi!=null)
                {
                    res[1]=TypeClass.SERVICE;       // UNOIDL TypeClass
                }
                else
                {
                    res[1]=xidl.getTypeClass();     // UNOIDL TypeClass
                }
            }
        }
        return res;
    }


    /** Returns string indicating the type of the supplied UNO object. All of the
     *  possible return values are documented at the end of {@link #getDefinition(Object o)}
     *  (&quot;&hellip;strings representing UNO-Types&hellip;&quot;).
     *
     * @param o UNO object or fully qualified string referring to an UNOIDL definition
     *
     * @return String spelling out the UNO type or empty string, if type cannot be determined
     */
    public static String getTypeName(Object o)
    {


        if (isContextAvailable==false)
            setContext(null);           // create the default context to work with

        if (o==null) { return ""; }     // return empty string

        Object [] tcInfo=determineTypeClass(o); // [0]...XIdlClass|XTypeDescription, [1]...TypeClass

        if (tcInfo[1]==null)
        {
            return "";
        }
        return (String) ht.get(tcInfo[1]);
    }



    /** Returns a blank delimited string containing the UNOIDL definitions of
     *  an UNO object (or a string denoting the fully qualified UNOIDL name).
     *  <br>Here are the encodings for the different UNOIDL types:
     *  <p>
     *

        <table border="1px" cellpadding="5px">
            <tr>
                <th>TypeClass
                <th>Encoding

            <tr>
                <td>UNO_CONSTANTS
                <td><code>
                        <span style="color: navy;">
                            UNO_CONSTANTS<span style="background-color:yellow;">|</span>fully-qualified-name<span style="background-color:
                            yellow;">|</span>datatype</span><span style="background-color:
                            rgb(255,230,255);">&emsp;<span style="color: blue;">member-name<span style="background-color: yellow;">|</span>value&hellip;</span>
                        </span>
                    </code>

                    <p>

                    <dl>
                        <em>Remark:</em> the following UNO <code>&quot;datatype&quot;</code>s are allowed for constants:
                    <dd>
                        <code>UNO_BOOLEAN</code>          (Java: <code>boolean</code>),
                        <code>UNO_BYTE</code>             (Java: <code>byte</code>),
                        <code>UNO_SHORT</code>            (Java: <code>short</code>),
                        <code>UNO_UNSIGNED_SHORT</code>   (Java: <code>short</code>),
                        <code>UNO_LONG</code>             (Java: <code>int</code>),
                        <code>UNO_UNSIGNED_LONG</code>    (Java: <code>int</code>),
                        <code>UNO_HYPER</code>            (Java: <code>long</code>),
                        <code>UNO_UNSIGNED_HYPER</code>   (Java: <code>long</code>),
                        <code>UNO_FLOAT</code>            (Java: <code>float</code>),
                        <code>UNO_DOUBLE</code>           (Java: <code>double</code>).
                    </dl>

            <tr>
                <td>UNO_ENUM
                <td><code>
                        <span style="color: navy;">
                            UNO_ENUM<span style="background-color: yellow;">|</span>fully-qualified-name<span style="background-color:
                            yellow;">|</span>default-value</span><span style="background-color:
                        rgb(255,230,255);">&emsp;<span style="color: blue;">member-name<span style="background-color: yellow;">|</span>value&hellip;</span>
                        </span>
                    </code>

                    <p>
                    <em>Remark:</em> this is always of UNO type <code>UNO_LONG</code> (ie. a 32-bit integer, Java type: <code>int</code>)

            <tr>
                <td>UNO_EXCEPTION
                <td><code>
                        <span style="color: navy;">
                            UNO_EXCEPTION<span style="background-color:
                            yellow;">|</span>fully-qualified-name</span><span style="background-color:
                            rgb(255,230,255);">&emsp;<span style="color:
                    blue;">member-name<span style="background-color:
                    yellow;">|</span>fullDatatype<span style="background-color:
                    yellow;">&hellip;</span>
                        </span>
                </code>

            <tr>
                <td>UNO_INTERFACE
                <td><code>
                        <span style="color: navy;">
                             UNO_INTERFACE<span style="background-color:
                             yellow;">|</span>fully-qualified-name</span><span style="background-color:
                        rgb(255,230,255);">&emsp;<span style="color: blue;">member-name<span style="background-color:
                        yellow;">|</span>member-definition&hellip;</span>
                        </span>

                         <p>where &quot;member-definition&quot; is one of:

                         <p>
                            <span style="color: blue;">
                                 <ul>
                                 <li>
                                     <span style="background-color: rgb(255,230,255);">UNO_ATTRIBUTE<span
                                     style="background-color: yellow;">|</span>[READONLY]<span
                                     style="background-color: yellow;">|</span>fullDatatype&hellip;</span>
                                     <p>

                                 <li>
                                     <span style="background-color: rgb(255,230,255);">UNO_METHOD<span
                                     style="background-color: yellow;">|</span>[ONEWAY]<span
                                     style="background-color: yellow;">|</span>return-value-fullDatatype<span
                                     style="background-color: yellow;">|</span>[argName<span
                                     style="background-color: yellow;">:</span>fullDatatype[<span
                                     style="background-color: yellow;">,</span>&hellip;]<span
                                     style="background-color: yellow;">|</span>[exception[<span
                                     style="background-color: yellow;">,</span>&hellip;]]</span>
                                 </ul>

                            </span>
                    </code>

            <tr>
                <td>UNO_MODULE
                <td><code>
                        <span style="color: navy;">
                            UNO_MODULE<span style="background-color:
                            yellow;">|</span>fully-qualified-name</span><span style="background-color:
                            rgb(255,230,255);">&emsp;<span style="color:
                            blue;">fully-qualified-member-name<span style="background-color:
                            yellow;">|</span>UNO-Type&hellip;</span>
                        </span>
                </code>

            <tr>
                <td>UNO_SERVICE
                <td><code>
                        <span style="color: navy;">
                             UNO_SERVICE<span style="background-color:
                             yellow;">|</span>fully-qualified-name<span style="background-color:
                             yellow;">|</span>[implementationName]&emsp;<span style="color:
                             blue;">member-name<span style="background-color:
                             yellow;">|</span>member-definition&hellip;</span>
                        </span>

                         <p>where &quot;member-definition&quot; is one of:

                         <p>
                            <span style="color: blue;">
                                 <ul>
                                 <li>
                                     <span style="background-color: rgb(255,230,255);">UNO_INTERFACE<span
                                     style="background-color: yellow;">|</span>[OPTIONAL]<span
                                     style="background-color: yellow;">|</span>defined_by_service</span>
                                     <p>

                                 <li>
                                     <span style="background-color: rgb(255,230,255);">UNO_SERVICE<span
                                     style="background-color: yellow;">|</span>[OPTIONAL]<span
                                     style="background-color: yellow;">|</span>defined_by_service</span>
                                     <p>

                                 <li>
                                     <span style="background-color: rgb(255,230,255);">UNO_PROPERTY<span
                                     style="background-color: yellow;">|</span>[modifier[,&hellip;]]<span
                                     style="background-color: yellow;">|</span>fullDatatype<span
                                     style="background-color: yellow;">|</span>defined_by_service</span>

                                 </ul>

                            </span>
                    </code>
                    <p>
                    <em>Remark:</em> if a service object is reflected that implements more than one
                    service definition, than the &quot;fully-qualified-name&quot; of that compound
                    service is created by concatenating all service names with the plus sign (+).
                    Each of these constituting service definitions (if available via reflection) is
                    then used to create the entire definition of that &quot;compound service&quot;
                    object in hand, documenting all defined interfaces, services and properties.

                    <p>
                    <em>Remark ad <code>&quot;UNO_PROPERTY&quot;</code>:</em> the modifier
                    values may be one or a (comma-delimited) combination of:

                        <code>&quot;MAYBEVOID&quot;, &quot;BOUND&quot;,
                        &quot;CONSTRAINED&quot;, &quot;TRANSIENT&quot;,
                        &quot;READONLY&quot;, &quot;MAYBEAMBIGUOUS&quot;,
                        &quot;MAYBEDEFAULT&quot;, &quot;REMOVEABLE&quot;,
                        &quot;OPTIONAL&quot;</code>.


                    <!--
                    <em>Remark:</em> if the data-type is derived from a TYPEDEF, then the TYPEDEF&apos;s
                    fully-qualified-name is given as the last item in the above encoded string.
                    -->

            <tr>
                <td>UNO_SINGLETON
                <td><code>
                        <span style="color: navy;">
                             UNO_SINGLETON<span style="background-color: yellow;">|</span>fully-qualified-name<span style="background-color: yellow;">|</span>[old-style-servicename]
                        </span>
                </code>

                <p>
                <em>Remark:</em> old-style-servicename or empty string if an instance of an interface

            <tr>
                <td>UNO_STRUCT
                <td><code>
                        <span style="color: navy;">
                            UNO_STRUCT<span style="background-color:
                            yellow;">|</span>fully-qualified-name</span><span style="background-color:
                            rgb(255,230,255);">&emsp;<span style="color:
                    blue;">member-name<span style="background-color:
                    yellow;">|</span>fullDatatype&hellip;</span></span>
                    </code>

            <tr>
                <td>UNO_TYPEDEF
                <td><code>
                        <span style="color: navy;">
                             UNO_TYPEDEF<span style="background-color:
                             yellow;">|</span>fully-qualified-name</span><span style="background-color:
                             rgb(255,230,255);">&emsp;<span style="color:
                             blue;">referenced-type<span style="background-color:
                             yellow;">|</span>UNO-Type</span>
                        </span>
                </code>

        </table>

        <p>
        <hr width="75%">
        <p>

        <dl>
            <dt><code>&quot;fullDatatype&quot;</code> is encoded as follows:
        <p>
            <dd>
                <code>fully-qualified-datatype-name<span style="background-color:
                            yellow;">:</span>UNO-TypeClass<span style="background-color:
                            yellow;">:</span>[referenced-type]<span style="background-color:
                            yellow;">:</span>[UNO-TypeClass]</code>

                <p>
                If a datatype is of type <code>UNO_TYPEDEF</code>,  then its
                &quot;referenced-type-name&quot; is given together with its
                respective UNO-TypeClass.

        </dl>


        <p>
        <hr width="75%">
        <p>


        <dl>
            <dt>The following strings representing UNO-Types (&quot;TypeClasses&quot;) may be encountered:<p>
            <dd>
                    <code>
                    &quot;UNO_ANY&quot;, &quot;UNO_ARRAY&quot;, &quot;UNO_BOOLEAN&quot;, &quot;UNO_BYTE&quot;, &quot;UNO_CHAR&quot;,
                    &quot;UNO_CONSTANT&quot;, &quot;UNO_CONSTANTS&quot;, &quot;UNO_DOUBLE&quot;, &quot;UNO_ENUM&quot;, &quot;UNO_EXCEPTION&quot;,
                    &quot;UNO_FLOAT&quot;, &quot;UNO_HYPER&quot;, &quot;UNO_INTERFACE&quot;, &quot;UNO_INTERFACE_ATTRIBUTE&quot;
                    &quot;UNO_INTERFACE_METHOD&quot;, &quot;UNO_LONG&quot;, &quot;UNO_MODULE&quot;, &quot;UNO_PROPERTY&quot;,
                    &quot;UNO_SEQUENCE&quot;, &quot;UNO_SERVICE&quot;, &quot;UNO_SHORT&quot;, &quot;UNO_SINGLETON&quot;,
                    &quot;UNO_STRING&quot;, &quot;UNO_STRUCT&quot;, &quot;UNO_TYPE&quot;, &quot;UNO_TYPEDEF&quot;,
                    &quot;UNO_UNION&quot;, &quot;UNO_UNKNOWN&quot;, &quot;UNO_UNSIGNED_HYPER&quot;, &quot;UNO_UNSIGNED_LONG&quot;,
                    &quot;UNO_UNSIGNED_SHORT&quot;, &quot;UNO_VOID&quot;</code>.

                <p>
                <em>Remark:</em> The UNO &quot;<code>TypeClass</code>&quot; constant names use
                the respective names above, but without the lead-in string
                &quot;<code>UNO_</code>&quot; (this makes the type information from methods of this
                class unambiguous).


        </dl>


        <p>
        <hr width="75%">
        <p>

        <dl>
            <dt>The UNO datatypes map to Java as follows:<p>
            <dd>
                <table border="1px" align="center" cellpadding="5px">
                    <tr>
                        <th>UNO Datatype
                        <th>Java Datatype

                    <tr style="background-color: yellow;">  <td> <code>UNO_ANY</code>                     <td> <code>com.sun.star.uno.Any</code> or <code>java.lang.Object</code>
                    <tr>                                    <td> <code>UNO_VOID</code>                    <td> <code>void</code>
                    <tr>                                    <td> <code>UNO_BOOLEAN</code>                 <td> <code>boolean</code>
                    <tr>                                    <td> <code>UNO_BYTE</code>           (8-bit)  <td> <code>byte</code>
                    <tr>                                    <td> <code>UNO_CHAR</code>           (16-bit) <td> <code>char</code>
                    <tr>                                    <td> <code>UNO_SHORT</code>          (16-bit) <td> <code>short</code>
                    <tr style="background-color: yellow;">  <td> <code>UNO_UNSIGNED_SHORT</code> (16-bit) <td> <code>short</code>
                    <tr style="background-color: yellow;">  <td> <code>UNO_LONG</code>           (32-bit) <td> <code>int</code>
                    <tr style="background-color: yellow;">  <td> <code>UNO_UNSIGNED_LONG</code>  (32-bit) <td> <code>int</code>
                    <tr style="background-color: yellow;">  <td> <code>UNO_HYPER</code>          (64-bit) <td> <code>long</code>
                    <tr style="background-color: yellow;">  <td> <code>UNO_UNSIGNED_HYPER</code> (64-bit) <td> <code>long</code>
                    <tr>                                    <td> <code>UNO_FLOAT</code>                   <td> <code>float</code>
                    <tr>                                    <td> <code>UNO_DOUBLE</code>                  <td> <code>double</code>
                </table>
                <p>


     *
     * @param o UNO service object or string fully qualifying a service definition in an UNOIDL module
     *
     * @return string containing the UNOIDL definitions, each element delimited with a blank, each element's
     *         definition parts delimited with a vertical bar (|).
     */
    public static String getDefinition(Object o)
    {
        if (isContextAvailable==false)
            setContext(null);           // create the default context to work with

        Object [] tcInfo=determineTypeClass(o); // [0]...XIdlClass|XTypeDescription, [1]...TypeClass
                                                // [2]..."o", [3] free slot

        if (tcInfo[1]==null)          // no TypeClass, hence return
        {
            return "";
        }

        tcInfo[3]=new StringBuffer();           //

        TypeClass tc=(TypeClass)tcInfo[1];      // get and assign TypeClass

        if (tc==TypeClass.INTERFACE)
        {
            encodeInterface(tcInfo);
        }
        else if (tc==TypeClass.SERVICE)
        {
            encodeService  (tcInfo);
        }
        else if (tc==TypeClass.CONSTANTS)
        {
            encodeConstants(tcInfo);
        }
        else if (tc==TypeClass.ENUM)
        {
            encodeEnum     (tcInfo);
        }
        else if (tc==TypeClass.EXCEPTION)
        {
            encodeCompound (tcInfo);
        }
        else if (tc==TypeClass.MODULE)
        {
            encodeModule   (tcInfo);
        }
        else if (tc==TypeClass.SINGLETON)
        {
            encodeSingleton(tcInfo);
        }
        else if (tc==TypeClass.STRUCT)
        {
            encodeCompound (tcInfo);
        }
        else if (tc==TypeClass.TYPEDEF)
        {
            encodeTypedef  (tcInfo);
        }

        return ((StringBuffer) tcInfo[3]).toString();
    }



    /** Returns a XTypeDescription object representing the received XIdlClass object.
     *
     * @param xic an XIdlClass object.
     *
     * @return a XTypeDescription object or null, if not found.
     */
    static XTypeDescription getXTypeDescription (XIdlClass xic )
    {
        if (xic==null)
        {
            return null;
        }

        Object obj=null;

        try
        {
            obj=theTypeDescriptionManager.getByHierarchicalName(check4reflection(xic.getName())); // get an XTypeDescription object
        }
        catch (Exception e)
        {
            return null;        // giving in: not found
        }

        XTypeDescription xtd=(XTypeDescription) UnoRuntime.queryInterface(XTypeDescription.class,obj);

        return xtd;
    }




    /** Appends the UNO compound (STRUCT, EXCEPTION) definitions to the StringBuffer received in the object array at index [3].
     *
     * @param Object [] tcInfo, contains an XIdlClass or XTypeDescription object at [0],
     *                           a TypeClass object at [1], the original object 'o' to analyze, and
     *                           a StringBuffer object at [3] to append the definition to.
     */
    private static void encodeCompound(Object [] tcInfo )
    {
        XTypeDescription xtd=
                 (tcInfo[0] instanceof XTypeDescription ? (XTypeDescription) tcInfo[0] :
                                                          getXTypeDescription((XIdlClass) tcInfo[0]));
        if (xtd==null)      // no XTypeDescriptor, nothing to do
        {
            return;
        }

        StringBuffer sb=(StringBuffer) tcInfo[3];       // get StringBuffer
        char delimiter=':';

            // define lead-in, denote fully qualifed name
        sb.append(
            // ((TypeClass)tcInfo[1]==TypeClass.STRUCT ? "STRUCT" : "EXCEPTION")
            ((TypeClass)tcInfo[1]==TypeClass.STRUCT ? ENCODE_STRUCT_KEYWORD : ENCODE_EXCEPTION_KEYWORD)
                                    +"|").append(xtd.getName());

        XCompoundTypeDescription xctd = (XCompoundTypeDescription)
            UnoRuntime.queryInterface(XCompoundTypeDescription.class, xtd);

        if (xctd==null)         // no constant type descriptions available
        {
            return;
        }

        XTypeDescription [] memTypes = xctd.getMemberTypes();
        String           [] memNames = xctd.getMemberNames();

        for (int i=0; i<memNames.length; i++)
        {
            // --->
            TypeClass tc=memTypes[i].getTypeClass();

            String tName=(String) ht2j.get(tc);              // get Java type name or null

            if (tName==null)    // not of a Java datatype
            {
                tName=memTypes[i].getName();  // get UNO's mixed-case name for this type
            }

            sb.append(" "+memNames[i]+"|"+tName.replace(' ', '_')
                         +delimiter+ht.get(tc)+delimiter);
            // <---

            // sb.append(" "+memNames[i]+"|"+memTypes[i].getName().replace(' ', '_')
            //             +delimiter+ht.get(memTypes[i].getTypeClass())+delimiter);

            // if (memTypes[i].getTypeClass()==TypeClass.TYPEDEF)
            if (tc==TypeClass.TYPEDEF)
            {
                encodeTypedefChunk(memTypes[i], sb, delimiter);
            }
            else
            {
                sb.append(delimiter);
            }
        }

    }




    /** Appends the UNO constants definitions to the StringBuffer received in the object array at index [3].
     *
     * @param Object [] tcInfo, contains an XIdlClass or XTypeDescription object at [0],
     *                           a TypeClass object at [1], the original object 'o' to analyze, and
     *                           a StringBuffer object at [3] to append the definition to.
     */
    private static void encodeConstants(Object [] tcInfo )
    {
        XTypeDescription xtd=
                 (tcInfo[0] instanceof XTypeDescription ? (XTypeDescription) tcInfo[0] :
                                                          getXTypeDescription((XIdlClass) tcInfo[0]));
        if (xtd==null)      // no XTypeDescriptor, nothing to do
        {
            return;
        }

        StringBuffer sb=(StringBuffer) tcInfo[3];       // get StringBuffer

            // define lead-in, denote fully qualifed name
        sb.append(ENCODE_CONSTANTS_KEYWORD+"|").append(xtd.getName());

        XConstantsTypeDescription xitd = (XConstantsTypeDescription)
            UnoRuntime.queryInterface(XConstantsTypeDescription.class, xtd);

        if (xitd==null)         // no constant type descriptions available
        {
            return;
        }

        XConstantTypeDescription ximtd [] = xitd.getConstants();
        if (ximtd==null)        // constants available
        {
            return;
        }

/*
        Object tstObj=ximtd[0].getConstantValue();
        boolean isAny=(tstObj instanceof Any);
        TypeClass tcConst=null;
        String typeName="";

        if (isAny)
        {
            tcConst= ((Any) tstObj).getType().getTypeClass();
            typeName = (String) ht.get( tcConst );
        }
        else    // a Java object
        {
            typeName=tstObj.getClass().getName();
        }

        sb.append("|").append(typeName);  // determine type
*/

        int i2=0;
        String type="", name="";
        for (i2=0; i2<ximtd.length; i2++)
        {
            processConstantValue(getUnqualifiedName(ximtd[i2].getName()),
                                 ximtd[i2].getConstantValue()           ,
                                 sb);
        }
    }


/** Determine constant's datatype and value, append value to string buffer.
 */
private static void processConstantValue(String name, Object constVal, StringBuffer sb)
{
    boolean isAny=(constVal instanceof Any);
    TypeClass tcConst=null;
    String typeName="";

    sb.append(" "+name+"|");    // append constant name to buffer

    if (isAny)
    {
        tcConst= ((Any) constVal).getType().getTypeClass();
        typeName = (String) ht.get( tcConst );

        try
        {
            Any anyObj=(Any) constVal;

                 if (tcConst==TypeClass.BOOLEAN        ) {  sb.append( AnyConverter.toBoolean      (anyObj)+""); }
            else if (tcConst==TypeClass.BYTE           ) {  sb.append( AnyConverter.toByte         (anyObj)+""); }
            else if (tcConst==TypeClass.CHAR           ) {  sb.append( AnyConverter.toChar         (anyObj)+""); }
            else if (tcConst==TypeClass.DOUBLE         ) {  sb.append( AnyConverter.toDouble       (anyObj)+""); }
            else if (tcConst==TypeClass.FLOAT          ) {  sb.append( AnyConverter.toFloat        (anyObj)+""); }
            else if (tcConst==TypeClass.LONG           ) {  sb.append( AnyConverter.toInt          (anyObj)+""); }
            else if (tcConst==TypeClass.HYPER          ) {  sb.append( AnyConverter.toLong         (anyObj)+""); }
            else if (tcConst==TypeClass.SHORT          ) {  sb.append( AnyConverter.toShort        (anyObj)+""); }
            else if (tcConst==TypeClass.UNSIGNED_HYPER ) {  sb.append( AnyConverter.toUnsignedLong (anyObj)+""); }
            else if (tcConst==TypeClass.UNSIGNED_LONG  ) {  sb.append( AnyConverter.toUnsignedInt  (anyObj)+""); }
            else if (tcConst==TypeClass.UNSIGNED_SHORT ) {  sb.append( AnyConverter.toUnsignedShort(anyObj)+""); }
            else
            {
                sb.append("UNKNOWN:"+anyObj);
            }
        }
        catch (Exception e)
        {
            sb.append("EXCEPTION:_"+e.toString().replaceAll(" ","_"));
        }
    }
    else    // a Java object, append String value of it
    {
        typeName=constVal.getClass().getName();
        sb.append(constVal+"");
    }

/*
    if (bSupplyDatatypeForEachConstant=true)
    {
        sb.append("|"+typeName);        // append datatype
    }
*/
    sb.append("|"+typeName);        // append datatype
}


    /** Appends the UNO constants definitions to the StringBuffer received in the object array at index [3].
     *
     * @param Object [] tcInfo, contains an XIdlClass or XTypeDescription object at [0],
     *                           a TypeClass object at [1], the original object 'o' to analyze, and
     *                           a StringBuffer object at [3] to append the definition to.
     */
    private static void encodeConstants_OLD_DEPRECATED(Object [] tcInfo )
    {
        XTypeDescription xtd=
                 (tcInfo[0] instanceof XTypeDescription ? (XTypeDescription) tcInfo[0] :
                                                          getXTypeDescription((XIdlClass) tcInfo[0]));
        if (xtd==null)      // no XTypeDescriptor, nothing to do
        {
            return;
        }

        StringBuffer sb=(StringBuffer) tcInfo[3];       // get StringBuffer

            // define lead-in, denote fully qualifed name
        sb.append(ENCODE_CONSTANTS_KEYWORD+"|").append(xtd.getName()).append("|");

        XConstantsTypeDescription xitd = (XConstantsTypeDescription)
            UnoRuntime.queryInterface(XConstantsTypeDescription.class, xtd);

        if (xitd==null)         // no constant type descriptions available
        {
            return;
        }

        XConstantTypeDescription ximtd [] = xitd.getConstants();
        if (ximtd==null)        // constants available
        {
            return;
        }

        Object tstObj=ximtd[0].getConstantValue();
        boolean isAny=(tstObj instanceof Any);
        TypeClass tcConst=null;
        String typeName="";

        if (isAny)
        {
            tcConst= ((Any) tstObj).getType().getTypeClass();
            typeName = (String) ht.get( tcConst );
        }
        else    // a Java object
        {
            typeName=tstObj.getClass().getName();
        }

        sb.append( typeName );  // determine type



        int i2=0;
        String type="", name="";
        for (i2=0; i2<ximtd.length; i2++)
        {
            name = getUnqualifiedName(ximtd[i2].getName());

            sb.append(" "+name+"|");
            if (isAny)
            {
                try
                {
                    Any anyObj=(Any) ximtd[i2].getConstantValue();

                         if (tcConst==TypeClass.BOOLEAN        ) {  sb.append( AnyConverter.toBoolean      (anyObj)+""); }
                    else if (tcConst==TypeClass.BYTE           ) {  sb.append( AnyConverter.toByte         (anyObj)+""); }
                    else if (tcConst==TypeClass.CHAR           ) {  sb.append( AnyConverter.toChar         (anyObj)+""); }
                    else if (tcConst==TypeClass.DOUBLE         ) {  sb.append( AnyConverter.toDouble       (anyObj)+""); }
                    else if (tcConst==TypeClass.FLOAT          ) {  sb.append( AnyConverter.toFloat        (anyObj)+""); }
                    else if (tcConst==TypeClass.LONG           ) {  sb.append( AnyConverter.toInt          (anyObj)+""); }
                    else if (tcConst==TypeClass.HYPER          ) {  sb.append( AnyConverter.toLong         (anyObj)+""); }
                    else if (tcConst==TypeClass.SHORT          ) {  sb.append( AnyConverter.toShort        (anyObj)+""); }
                    else if (tcConst==TypeClass.UNSIGNED_HYPER ) {  sb.append( AnyConverter.toUnsignedLong (anyObj)+""); }
                    else if (tcConst==TypeClass.UNSIGNED_LONG  ) {  sb.append( AnyConverter.toUnsignedInt  (anyObj)+""); }
                    else if (tcConst==TypeClass.UNSIGNED_SHORT ) {  sb.append( AnyConverter.toUnsignedShort(anyObj)+""); }
                    else
                    {
                        sb.append("UNKNOWN:"+anyObj);
                    }
                }
                catch (Exception e)
                {
                    sb.append("EXCEPTION"+e.toString());
                }
            }
            else
            {
                sb.append(ximtd[i2].getConstantValue());
            }
        }
    }




    /** Appends the UNO enum definitions to the StringBuffer received in the object array at index [3].
     *
     * @param Object [] tcInfo, contains an XIdlClass or XTypeDescription object at [0],
     *                           a TypeClass object at [1], the original object 'o' to analyze, and
     *                           a StringBuffer object at [3] to append the definition to.
     */
    private static void encodeEnum     (Object [] tcInfo )
    {
        XTypeDescription xtd=
                 (tcInfo[0] instanceof XTypeDescription ? (XTypeDescription) tcInfo[0] :
                                                          getXTypeDescription((XIdlClass) tcInfo[0]));
        if (xtd==null)      // no XTypeDescriptor, nothing to do
        {
            return;
        }

        StringBuffer sb=(StringBuffer) tcInfo[3];       // get StringBuffer

            // define lead-in, denote fully qualifed name
        // sb.append("ENUM"+"|").append(xtd.getName()).append("|");
        sb.append(ENCODE_ENUM_KEYWORD+"|").append(xtd.getName()).append("|");

        XEnumTypeDescription xetd = (XEnumTypeDescription)
            UnoRuntime.queryInterface(XEnumTypeDescription.class, xtd);

        if (xetd==null)         // no constant type descriptions available
        {
            return;
        }

        sb.append(xetd.getDefaultEnumValue());

        String [] enumNames =xetd.getEnumNames();
        int    [] enumValues=xetd.getEnumValues();

        for (int i=0; i<enumNames.length; i++)
        {
            sb.append(" "+enumNames[i]+"|"+enumValues[i]);
        }
    }




    /** Appends the UNO module definitions to the StringBuffer received in the object array at index [3].
     *
     * @param Object [] tcInfo, contains an XIdlClass or XTypeDescription object at [0],
     *                           a TypeClass object at [1], the original object 'o' to analyze, and
     *                           a StringBuffer object at [3] to append the definition to.
     */
    private static void encodeModule     (Object [] tcInfo )
    {
        XTypeDescription xtd=
                 (tcInfo[0] instanceof XTypeDescription ? (XTypeDescription) tcInfo[0] :
                                                          getXTypeDescription((XIdlClass) tcInfo[0]));
        if (xtd==null)      // no XTypeDescriptor, nothing to do
        {
            return;
        }

        StringBuffer sb=(StringBuffer) tcInfo[3];       // get StringBuffer

            // define lead-in, denote fully qualifed name
        // sb.append("MODULE"+"|").append(xtd.getName());
        sb.append(ENCODE_MODULE_KEYWORD+"|").append(xtd.getName());

        XModuleTypeDescription xmtd = (XModuleTypeDescription)
            UnoRuntime.queryInterface(XModuleTypeDescription.class, xtd);

        if (xmtd==null)         // no constant type descriptions available
        {
            return;
        }

        XTypeDescription [] members=xmtd.getMembers();

        for (int i=0; i<members.length; i++)
        {
            // -->
            TypeClass tc=members[i].getTypeClass();
            String tName=(String) ht2j.get(tc);              // get Java type name or null

            if (tName==null)    // not of a Java datatype
            {
                tName=members[i].getName();  // get UNO's mixed-case name for this type
            }
            sb.append(" "+getUnqualifiedName(tName)+"|"+ht.get(tc));
            // <---

            // sb.append(" "+getUnqualifiedName(members[i].getName())+"|"+ht.get(members[i].getTypeClass()));
        }
    }




    /** Appends the UNO SINGLETON definitions to the StringBuffer received in the object array at index [3].
     *
     * @param Object [] tcInfo, contains an XIdlClass or XTypeDescription object at [0],
     *                           a TypeClass object at [1], the original object 'o' to analyze, and
     *                           a StringBuffer object at [3] to append the definition to.
     */
    private static void encodeSingleton(Object [] tcInfo )
    {
        XTypeDescription xtd=
                 (tcInfo[0] instanceof XTypeDescription ? (XTypeDescription) tcInfo[0] :
                                                          getXTypeDescription((XIdlClass) tcInfo[0]));
        if (xtd==null)      // no XTypeDescriptor, nothing to do
        {
            return;
        }

        StringBuffer sb=(StringBuffer) tcInfo[3];       // get StringBuffer

            // define lead-in, denote fully qualifed name
        // sb.append("SINGLETON"+"|").append(xtd.getName()).append("|");
        sb.append(ENCODE_SINGLETON_KEYWORD+"|").append(xtd.getName()).append("|");


        XSingletonTypeDescription xstd = (XSingletonTypeDescription)
            UnoRuntime.queryInterface(XSingletonTypeDescription.class, xtd);

        if (xstd==null)         // no constant type descriptions available
        {
            return;
        }


        XServiceTypeDescription std=xstd.getService();
        if (std!=null) // could be null for an "interface-based singleton"
        {
            sb.append(std.getName());
        }
    }




    /** Appends the UNO TYPEDEF definitions to the StringBuffer received in the object array at index [3].
     *
     * @param Object [] tcInfo, contains an XIdlClass or XTypeDescription object at [0],
     *                           a TypeClass object at [1], the original object 'o' to analyze, and
     *                           a StringBuffer object at [3] to append the definition to.
     */
    private static void encodeTypedef  (Object [] tcInfo )
    {
        XTypeDescription xtd=
                 (tcInfo[0] instanceof XTypeDescription ? (XTypeDescription) tcInfo[0] :
                                                          getXTypeDescription((XIdlClass) tcInfo[0]));
        if (xtd==null)      // no XTypeDescriptor, nothing to do
        {
            return;
        }

        StringBuffer sb=(StringBuffer) tcInfo[3];       // get StringBuffer

            // define lead-in, denote fully qualifed name
        // sb.append("TYPEDEF"+"|").append(xtd.getName()+" ");
        sb.append(ENCODE_TYPEDEF_KEYWORD+"|").append(xtd.getName()+" ");
        encodeTypedefChunk(xtd, sb, ':');
    }



    /** Queries received TYPEDEF XTypeDescription object and encodes its definition in the received StringBuffer.
     *
     * @param xtd XTypeDescription object referring to a TypeClass.TYPEDEF object
     * @param sb  StringBuffer to encode referenced type into
     * @param delimiter character to use for delimiting the datatype from the TypeClass information
     */
    private static void encodeTypedefChunk(XTypeDescription xtd, StringBuffer sb, char delimiter)
    {
        XIndirectTypeDescription xitd = (XIndirectTypeDescription)
            UnoRuntime.queryInterface(XIndirectTypeDescription.class, xtd);

        if (xitd==null)         // no constant type descriptions available
        {
            return;
        }

        XTypeDescription td=xitd.getReferencedType();

        // --->
        TypeClass tc=td.getTypeClass();
        String tName=(String) ht2j.get(tc);              // get Java type name or null

        if (tName==null)    // not of a Java datatype
        {
            tName=td.getName();  // get UNO's mixed-case name for this type
        }
        sb.append((tName.replace(' ', '_'))+delimiter+ht.get(tc));
        // <---

        // sb.append((td.getName().replace(' ', '_'))+delimiter+ht.get(td.getTypeClass()));
    }






    /** Appends the UNO INTERFACE definitions to the StringBuffer received in the object array at index [3].
     *
     * @param Object [] tcInfo, contains an XIdlClass or XTypeDescription object at [0],
     *                           a TypeClass object at [1], the original object 'o' to analyze, and
     *                           a StringBuffer object at [3] to append the definition to.
     */
    private static void encodeInterface  (Object [] tcInfo )
    {
        XTypeDescription xtd=
                 (tcInfo[0] instanceof XTypeDescription ? (XTypeDescription) tcInfo[0] :
                                                          getXTypeDescription((XIdlClass) tcInfo[0]));
        if (xtd==null)      // no XTypeDescriptor, nothing to do
        {
            return;
        }

        StringBuffer sb=(StringBuffer) tcInfo[3];       // get StringBuffer
        char delimiter = ':';

            // define lead-in, denote fully qualifed name
        // sb.append("INTERFACE"+"|").append(xtd.getName());
        sb.append(ENCODE_INTERFACE_KEYWORD+"|").append(xtd.getName());

        XInterfaceTypeDescription xitd = (XInterfaceTypeDescription)
            UnoRuntime.queryInterface(XInterfaceTypeDescription.class, xtd);
        if (xitd==null)     // nothing to do anymore
        {
            return;
        }

        XInterfaceMemberTypeDescription ximtd [] = xitd.getMembers();
        String mTypeName="", name="";
        for (int i=0; i<ximtd.length; i++)
        {
            encode4InterfaceXIMTD(sb, ximtd[i], delimiter);

/*
            name = getUnqualifiedName(ximtd[i].getMemberName());

            TypeClass tcTmp=ximtd[i].getTypeClass();
            if (tcTmp==TypeClass.INTERFACE_METHOD)
            {
                mTypeName="METHOD";
            }
            else if (tcTmp==TypeClass.INTERFACE_ATTRIBUTE)
            {
                mTypeName="ATTRIBUTE";
            }
            else
            {
                mTypeName="???"+ht.get(tcTmp)+"???";
            }
            sb.append(" "+name+"|"+mTypeName);

            if (tcTmp==TypeClass.INTERFACE_ATTRIBUTE)   // get readonly and type
            {
                XInterfaceAttributeTypeDescription xiatd = (XInterfaceAttributeTypeDescription)
                    UnoRuntime.queryInterface(XInterfaceAttributeTypeDescription.class, ximtd[i]);

                sb.append("|"+ (xiatd.isReadOnly() ? "READONLY" : ""));

                XTypeDescription xtdAttr=xiatd.getType();    // get datatype of attribute

                sb.append("|"+xtdAttr.getName().replace(' ', '_')
                             +delimiter+ht.get(xtdAttr.getTypeClass())+delimiter);


                if (xtdAttr.getTypeClass()==TypeClass.TYPEDEF)
                {
                    encodeTypedefChunk(xtdAttr, sb, delimiter);
                }
                else
                {
                    sb.append(delimiter);
                }
            }

            else if(tcTmp==TypeClass.INTERFACE_METHOD)    // a method, get signature
            {
                XInterfaceMethodTypeDescription o2= (XInterfaceMethodTypeDescription) UnoRuntime.queryInterface(
                    XInterfaceMethodTypeDescription.class, ximtd[i]);
                if (o2==null)
                {
                    continue;         // nothing to do anymore
                }

                XMethodParameter params[]=o2.getParameters();
                XTypeDescription exc   []=o2.getExceptions();

                sb.append("|"+(o2.isOneway() ? "ONEWAY" : ""));

                    // return type
                XTypeDescription rt  =o2.getReturnType();
                TypeClass        rtTC=rt.getTypeClass();

                sb.append("|"+rt.getName().replace(' ', '_')+delimiter+ht.get(rtTC)+delimiter );

                if (rtTC==TypeClass.TYPEDEF)
                {
                    encodeTypedefChunk(rt, sb, delimiter);
                }
                else
                {
                    sb.append(delimiter);
                }

                    // arguments
                sb.append("|");

                for (int i3=0; i3<params.length; i3++)
                {
                    rt  =params[i3].getType();
                    rtTC=rt.getTypeClass();
                    sb.append(rt.getName().replace(' ','_')+delimiter+ht.get(rtTC)+delimiter);
                    if (rtTC==TypeClass.TYPEDEF)
                    {
                        encodeTypedefChunk(rt, sb, delimiter);
                    }
                    else
                    {
                        sb.append(delimiter);
                    }
                    if ((i3+1)!=params.length) sb.append(',');  // not last parameter?
                }

                    // exceptions
                sb.append("|");

                for (int i2=0; i2<exc.length; i2++)
                {
                    sb.append(exc[i2].getName());
                    if ((i2+1)!=exc.length) sb.append(',');  // not last parameter?
                }
            }
*/
        }
    }


    static void encode4InterfaceXIMTD(StringBuffer sb, XInterfaceMemberTypeDescription ximtd, char delimiter)
    {
        String mTypeName="",
               name=getUnqualifiedName(ximtd.getMemberName());

        TypeClass tcTmp=ximtd.getTypeClass();
        if (tcTmp==TypeClass.INTERFACE_METHOD)
        {
            mTypeName=ENCODE_METHOD_KEYWORD; // "METHOD";
        }
        else if (tcTmp==TypeClass.INTERFACE_ATTRIBUTE)
        {
            mTypeName=ENCODE_ATTRIBUTE_KEYWORD; // "ATTRIBUTE";
        }
        else
        {
            mTypeName="???"+ht.get(tcTmp)+"???";
        }
        sb.append(" "+name+"|"+mTypeName);

        if (tcTmp==TypeClass.INTERFACE_ATTRIBUTE)   // get readonly and type
        {
            XInterfaceAttributeTypeDescription xiatd = (XInterfaceAttributeTypeDescription)
                UnoRuntime.queryInterface(XInterfaceAttributeTypeDescription.class, ximtd);

            sb.append("|"+ (xiatd.isReadOnly() ? "READONLY" : ""));

            XTypeDescription xtdAttr=xiatd.getType();    // get datatype of attribute

            // --->
            TypeClass tc=xtdAttr.getTypeClass();
            String tName=(String) ht2j.get(tc);              // get Java type name or null

            if (tName==null)    // not of a Java datatype
            {
                tName=xtdAttr.getName();  // get UNO's mixed-case name for this type
            }
            sb.append("|"+tName.replace(' ', '_')
                         +delimiter+ht.get(tc)+delimiter);
            // <--

            // sb.append("|"+xtdAttr.getName().replace(' ', '_')
            //             +delimiter+ht.get(xtdAttr.getTypeClass())+delimiter);


            if (xtdAttr.getTypeClass()==TypeClass.TYPEDEF)
            {
                encodeTypedefChunk(xtdAttr, sb, delimiter);
            }
            else
            {
                sb.append(delimiter);
            }
        }

        else if(tcTmp==TypeClass.INTERFACE_METHOD)    // a method, get signature
        {
            XInterfaceMethodTypeDescription o2= (XInterfaceMethodTypeDescription) UnoRuntime.queryInterface(
                XInterfaceMethodTypeDescription.class, ximtd);
            if (o2==null)
            {
                return;         // nothing to do anymore
            }

            XMethodParameter params[]=o2.getParameters();
            XTypeDescription exc   []=o2.getExceptions();

            sb.append("|"+(o2.isOneway() ? "ONEWAY" : ""));

                // return type
            XTypeDescription rt  =o2.getReturnType();
            TypeClass        rtTC=rt.getTypeClass();

            // --->
            String tName=(String) ht2j.get(rtTC);              // get Java type name or null

            if (tName==null)    // not of a Java datatype
            {
                tName=rt.getName();  // get UNO's mixed-case name for this type
            }
            sb.append("|"+tName.replace(' ', '_')+delimiter+ht.get(rtTC)+delimiter );
            // <---
            // sb.append("|"+rt.getName().replace(' ', '_')+delimiter+ht.get(rtTC)+delimiter );

            if (rtTC==TypeClass.TYPEDEF)
            {
                encodeTypedefChunk(rt, sb, delimiter);
            }
            else
            {
                sb.append(delimiter);
            }

                // arguments
            sb.append("|");

            for (int i3=0; i3<params.length; i3++)
            {
                rt  =params[i3].getType();
                rtTC=rt.getTypeClass();

                // --->
                tName=(String) ht2j.get(rtTC);              // get Java type name or null
                if (tName==null)    // not of a Java datatype
                {
                    tName=rt.getName();  // get UNO's mixed-case name for this type
                }
                sb.append(tName.replace(' ','_')+delimiter+ht.get(rtTC)+delimiter);
                // <---
                // sb.append(rt.getName().replace(' ','_')+delimiter+ht.get(rtTC)+delimiter);

                if (rtTC==TypeClass.TYPEDEF)
                {
                    encodeTypedefChunk(rt, sb, delimiter);
                }
                else
                {
                    sb.append(delimiter);
                }
                if ((i3+1)!=params.length) sb.append(',');  // not last parameter?
            }

                // exceptions
            sb.append("|");

            for (int i2=0; i2<exc.length; i2++)
            {
                sb.append(exc[i2].getName());
                if ((i2+1)!=exc.length) sb.append(',');  // not last parameter?
            }
        }

    }


    /** Appends the UNO SERVICE definitions to the StringBuffer received in the object array at index [3].
     *
     * @param Object [] tcInfo, contains an XIdlClass or XTypeDescription object at [0],
     *                           a TypeClass object at [1], the original object 'o' to analyze at [2], and
     *                           a StringBuffer object at [3] to append the definition to.
     */
    private static void encodeService  (Object [] tcInfo )
    {
        String       name="", implName="";          // name of service (concatenated from all service names)
        String    [] services = {""};               // array of implemented services
        Object  [][] tcObjArr = new Object [1][];   // array of tcInfo-objects per service

                    // service object from a fully-qualified string
        if (tcInfo[0] instanceof XTypeDescription)  // o.k. a single service
        {
            services[0]=((XTypeDescription)tcInfo[0]).getName() ;
            name=services[0];
            tcObjArr[0]=tcInfo;        // save array, contains already everything we need...
        }
        else        // from an instantiated service object, a XIdlClass object in hand, can implement multiple services!
        {
            XServiceInfo xsi=(XServiceInfo) UnoRuntime.queryInterface(XServiceInfo.class, tcInfo[2]);

            if (xsi==null)      // oops, nothing to do !
            {
                return;
            }

            String [] servicesTmp=xsi.getSupportedServiceNames();    // get names of all implemented services
            implName=xsi.getImplementationName();       // get the implementation name

            services=new String [servicesTmp.length];      // make enough room in array of services
            tcObjArr=new Object [servicesTmp.length][];    // make sure we have enough room

            Object [] tmpObjArr=null;
            int i=0, j=0;

            for (i=0; i<servicesTmp.length;i++)
            {
                name=name+(i>0 ? "+" : "")+servicesTmp[i];
                tmpObjArr=determineTypeClass((Object) servicesTmp[i]);       // try to get a XTypeDescriptor
                if (tmpObjArr[0]!=null)
                {
                    services[j]=servicesTmp[i]; // save service name
                    tcObjArr[j]=tmpObjArr;      // save array
                    j++;
                }
                else        // could be an implementation service object which cannot be reflected via XTypeDescriptions
                {
                    if (bDebug==true)
                    {
                        say(RgfReflectUNO.class+": encodeService(): no XTypeDescriptor found for: "+pp(services[i])+"!");
                    }
                }
            }
        }

        StringBuffer sb=(StringBuffer) tcInfo[3];       // get StringBuffer

            // define lead-in, denote fully qualifed name
        // sb.append("SERVICE"+"|"+name+"|"+implName);
        sb.append(ENCODE_SERVICE_KEYWORD+"|"+name+"|"+implName);

        // loop over individual service descriptiosn
        for (int j=0; j<tcObjArr.length; j++)
        {
            if (tcObjArr[j]==null) continue;    // o.k. no XTypeDescriptor object available, finished work
            name=services[j];                   // get service name

            XServiceTypeDescription xstd=(XServiceTypeDescription) UnoRuntime.queryInterface(
                XServiceTypeDescription.class, tcObjArr[j][0]);

            if (xstd==null) continue;       // could not get the interface object, continue

            encode4ServiceXTDS(sb, xstd.getMandatoryServices()   , false, name);
            encode4ServiceXTDS(sb, xstd.getOptionalServices()    , true,  name);
            encode4ServiceXTDS(sb, xstd.getMandatoryInterfaces() , false, name);
            encode4ServiceXTDS(sb, xstd.getOptionalInterfaces()  , true,  name);

            XPropertyTypeDescription  [] props = xstd.getProperties();
            for (int i=0; i<props.length;i++)
            {
                // sb.append(" "+getUnqualifiedName(props[i].getName())+"|PROPERTY|");
                sb.append(" "+getUnqualifiedName(props[i].getName())+"|"+ENCODE_PROPERTY_KEYWORD+"|");
                encodePropModifier(sb, props[i].getPropertyFlags());

                XTypeDescription propTD=props[i].getPropertyTypeDescription(); // get the property's type description

                // --->
                TypeClass tc=propTD.getTypeClass();

                // String tName=(String) ht2j.get(tc);      // get Java type name or null
                String tName=(String) ht2j_prop.get(tc);    // get Java class type name or null

                if (tName==null)    // not of a Java datatype
                {
                    tName=propTD.getName();  // get UNO's mixed-case name for this type
                }
                sb.append("|"+tName.replace(' ', '_')+":"+ht.get(tc)+":");
                // <---

                // sb.append("|"+propTD.getName().replace(' ', '_')+":"    // datatype
                //           +ht.get(propTD.getTypeClass())+":");          // UNO TypeClass

                // if (propTD.getTypeClass()==TypeClass.TYPEDEF)
                if (tc==TypeClass.TYPEDEF)
                {
                    encodeTypedefChunk(propTD, sb, ':');
                }
                else
                {
                    sb.append(':');
                }
                sb.append("|"+name);
            }
        }
    }


    /** Encodes service and interface definitions for a service definition.
     *
     * @param sb StringBuffer to add information to
     * @param xtds an array of XTypeDescriptions
     * @param bOptional indicates whether xtds represents optional services/interfaces
     * @param definedIn service name in which this member got defined; only given, if information stems
     *        from an UNO implementation object
     */
    static private void encode4ServiceXTDS(StringBuffer sb, XTypeDescription [] xtds, boolean bOptional, String definedIn)
    {
        String hint=(bOptional==true ? "OPTIONAL" : "");

        for (int i=0; i<xtds.length; i++)
        {
            sb.append(" "+xtds[i].getName()+"|"+ht.get(xtds[i].getTypeClass())+"|"+hint+"|"+definedIn);
        }
    }



    /** Returns a blank delimited string of names of the provided interface types. Cf. UNO's
     *  <code>com.sun.star.lang.XTypeProvider</code> interface which is applied to the
     *  service object <code>o</code>.
     *
     *  @param o a service object which gets queried of its provided interface types using
     *  the XTypeProvider interface
     *
     *  @return blank delimited string of interface names, empty string if none available
     */
    public static String getXTypeProviderTypeNames(Object o)
    {
        if (isContextAvailable==false)
            setContext(null);           // create the default context to work with

        XTypeProvider xtp=(XTypeProvider) UnoRuntime.queryInterface(XTypeProvider.class, o);
        StringBuffer sb=new StringBuffer();

        if (xtp!=null)
        {
            Type [] t = xtp.getTypes();

            for (int i=0; i<t.length; i++)
            {
//                sb.append( (i>0 ? " " : "")+t[i].getTypeName() + "|" +  ht.get(t[i].getTypeClass()));
                sb.append( (i>0 ? " " : "")+t[i].getTypeName() );
            }
        }
        return sb.toString();
    }



    /** Looks up a (partially qualified) given interface name case-insensitively, carries out
     *  the appropriate <code>UnoRuntime.queryInterface()</code> and returns its result.
     *
     * @param name which may qualify the desired interface fully or partly, case
     *        does not matter either. If <code>name</code> denotes an existing Java class, that
     *        class is used to carry out the UnoRuntime.queryInterface(...). Otherwise an interface
     *        class is searched at runtime using <code>XTypeProvider</code> and possibly reflection.
     *
     * @param o is the object from which the interface should be queried from
     *
     * @param bExtendSearch <code>true</code>: if interface cannot be found in the
     *        <code>XTypeProvider</code> list, then extend search using full
     *        reflection; it could be the case that <code>XTypeProvider</code>
     *        does not provide all available interfaces! (as of OOo 2.0, 2005-12-20)
     *
     * @return the desired interface object, if successful, <code>null</code> else
     *
     */
    public static Object queryInterfaceObjectByName(Object o, String name, boolean bExtendSearch)
    {
        if (isContextAvailable==false)
            setContext(null);           // create the default context to work with

        if (name==null || name.length()==0)     //  name not given or empty string
        {
            return null;
        }

        try     // assume a valid Java class name was supplied
        {
            // Class clz=Class.forName(name);      // try to load the class object
            // Class clz=ClassLoader.getSystemClassLoader().loadClass(name);      // try to load the class object
            Class clz=null;

                // first try the context class loader
            try {
                clz=Thread.currentThread().getContextClassLoader().loadClass(name);      // try to load the class object
            }
            catch (Exception e0) {  // o.k. CTXCL did not work, try defining CL
                clz=Class.forName(name);    // uses class loader that loaded this class
            }
                // it does exis, hence use it verbatim to query the interface
            return UnoRuntime.queryInterface(clz, o);
        }
        catch (Exception e) // probably does not exist
        {}


        XTypeProvider xtp=(XTypeProvider) UnoRuntime.queryInterface(XTypeProvider.class, o);
        if (xtp!=null)
        {
            Type [] t = xtp.getTypes();
            String tmpName="";
            int    tmpLength=0;

            for (int i=0; i<t.length; i++)
            {
                if (t[i].getTypeClass()==TypeClass.INTERFACE)   // an interface type in hand ?
                {
                    if (compareRelaxed(name, t[i].getTypeName())==true)
                    {
                        return UnoRuntime.queryInterface(t[i], o);
                    }
                }
            }
        }

if (bDebug) say(RgfReflectUNO.class+": MEEEEH! queryInterfaceObjectByName() - interface "+pp(name)+" not found in XTypeProvider! bExtendSearch="+bExtendSearch);
            //
        if (bExtendSearch==true)        // use reflection
        {
            return extendSearch(o, name, QUERY_INTERFACE_OBJECT_BY_NAME);
        }

        return null;    // not found
    }


    /** Returns a blank delimited String of the interface names that are defined in UNOIDL
     *  for the service object.
     *
     *  @param o service object to analyze
     *  @return a blank delimited string of service names (or empty string)
     */
    static public String getInterfaceNamesViaReflection(Object o)
    {
        return (String) extendSearch(o, null, GET_INTERFACES_AS_STRING);
    }

    /* rgf: maybe too complex for some, keep String the "least common denominator"...

    / ** Returns a blank delimited String or String array of services that are defined in UNOIDL
     *  for the service object.
     *
     *  @param o service object to analyze
     *  @param bAsString if <code>true</code> a blank delimited string of service names
     *                  is returned (empty string, if none found), else a String array
     *                  object (or <code>null</code>, if none found) of service names
     *  @return a blank delimited string of service names (or empty string), or a String
     *          array of service names (or null)
     * /
    static public Object getInterfaceNamesViaReflection(Object o, boolean bAsString)
    {
        if (bAsString==true)
        {
            return extendSearch(o, null, GET_INTERFACES_AS_STRING);
        }
        return extendSearch(o, null, GET_INTERFACES_AS_ARRAY);
    }
    */


    /** Returns a blank delimited String of service names that are defined in UNOIDL
     *  for the service object.
     *
     *  @param o service object to analyze
     *  @return a blank delimited string of service names (or empty string)
     */
    static public String getServiceNamesViaReflection(Object o)
    {
        return (String) extendSearch(o, null, GET_SERVICES_AS_STRING);
    }

    /* rgf: maybe too complex for some, keep String the "least common denominator"...



    / ** Returns a blank delimited String or String array of services that are defined in UNOIDL
     *  for the service object.
     *
     *  @param o service object to analyze
     *  @param bAsString if <code>true</code> a blank delimited string of service names
     *                  is returned (empty string, if none found), else a String array
     *                  object (or <code>null</code>, if none found) of service names
     *  @return a blank delimited string of service names (or empty string), or a String
     *          array of service names (or null)
     * /
    static public Object getServiceNamesViaReflection(Object o, boolean bAsString)
    {
        if (bAsString==true)
        {
            return extendSearch(o, null, GET_SERVICES_AS_STRING);
        }
        return extendSearch(o, null, GET_SERVICES_AS_ARRAY);
    }
    */



    static final int QUERY_INTERFACE_OBJECT_BY_NAME = 1;
    static final int QUERY_INTERFACE_NAME           = 2;

    static final int USE_VECTOR                     = 30; // starting with this value, the Vector is to be used
    static final int GET_INTERFACES_AS_STRING       = 31;
    static final int GET_INTERFACES_AS_ARRAY        = 32;

            // service-related start with value 50 ! (makes it easier to do ifs in the code ...
    static final int SERVICE_NUMBERING              = 50;
    static final int GET_SERVICES_AS_STRING         = 51;
    static final int GET_SERVICES_AS_ARRAY          = 52;

    /** Analyzes all implemented services and processes found interfaces or services.
     *
     *  @param o service object to analyze
     *  @param needle name of an interface or service to look for
     *  @param kind indicates what kind of processing is desired, one of the constants
     *              <code>QUERY_INTERFACE_OBJECT_BY_NAME</code> (value: 1),
     *              <code>QUERY_INTERFACE_NAME</code> (value: 2),
     *              <code>GET_INTERFACES_AS_STRING</code> (value: 31),
     *              <code>GET_INTERFACES_AS_ARRAY</code> (value: 32),
     *              <code>GET_SERVICES_AS_STRING</code> (value: 51),
     *              <code>GET_SERVICES_AS_ARRAY</code> (value: 52).
     *
     * @return   depending on the kind an interface object, a string or a string array, null
     *           or empty string, if <code>needle</code> could not be found.
     */
    static Object extendSearch(Object o, String needle, int kind)
    {
        Object [] tcInfo=determineTypeClass(o); // [0]...XIdlClass|XTypeDescription, [1]...TypeClass

        XServiceInfo xsi=(XServiceInfo) UnoRuntime.queryInterface(XServiceInfo.class, tcInfo[2]);

        if (xsi==null)      // oops, nothing to do !
        {
            if (kind==QUERY_INTERFACE_OBJECT_BY_NAME) return null;

            if (kind==GET_INTERFACES_AS_ARRAY || kind==GET_SERVICES_AS_ARRAY)
            {
                return new String [0] ;
            }
            return "";
        }

        String [] services=xsi.getSupportedServiceNames();    // get names of all implemented services

        LinkedList ll=new LinkedList();     // Queue-like (cf. Rexx)
        for (int i=0; i<services.length; i++)
        {
            ll.add(services[i]);        // add service to array
        }

        Vector vec=null;
        if (kind>USE_VECTOR)            // do we need a Vector to work with ?
        {
            vec=new Vector();
        }

            // now iterate over services, analyze them and compare them
        String serviceName="", interfaceName="";
        HashSet hs=new HashSet();   // set to memorize service names that got analyzed already

        while (ll.size()>0)
        {
            serviceName=(String) ll.removeFirst();   // get service name
            if (hs.contains(serviceName))   // already analyzed, skip
            {
                continue;
            }
                // memorize that we analyzed this service
            hs.add(serviceName);

            if (kind>SERVICE_NUMBERING)
            {
                vec.add(serviceName);
            }


            Object           []  tcObj=null;
            Object           [] arrObj=new Object[2];
            XTypeDescription []    xtd=null;


                // not found, now reflect for other services, start out with mandatory ones
            tcObj=determineTypeClass(serviceName);

            XServiceTypeDescription xstd=(XServiceTypeDescription) UnoRuntime.queryInterface(
                            XServiceTypeDescription.class, tcObj[0]);

            if (xstd==null) continue;       // could not get an interface object, continue

            if (kind<SERVICE_NUMBERING)     // o.k. hunt interfaces
            {
                    // check for interface
                arrObj[0]=xstd.getMandatoryInterfaces();
                arrObj[1]=xstd.getOptionalInterfaces();

                for (int i=0; i<arrObj.length; i++)
                {
                    xtd= (XTypeDescription []) arrObj[i];
                    for (int k=0; k<xtd.length; k++)
                    {
                        interfaceName=xtd[k].getName();

                        if (kind<USE_VECTOR && compareRelaxed(needle, interfaceName)==true)
                        {
                            // BINGO !
                            try
                            {
                                if (kind==QUERY_INTERFACE_OBJECT_BY_NAME)
                                {
                                    // Class cl=Class.forName(check4reflection(interfaceName));    // load Java class
                                    // Class cl=ClassLoader.getSystemClassLoader().loadClass(check4reflection(interfaceName));    // load Java class

                                        // handle spelling bug under OOo 1.5 and OOo 2.0
                                    String interfaceNameC4R=check4reflection(interfaceName);
                                    Class cl=null;

                                    try {   // try CTXTCL first
                                        cl=Thread.currentThread().getContextClassLoader().loadClass(interfaceNameC4R);    // load Java class
                                    }
                                    catch (Exception e0) {  // now try defining CL
                                        cl=Class.forName(interfaceNameC4R);
                                    }

                                    return UnoRuntime.queryInterface(cl, o);
                                }

                                else if (kind==QUERY_INTERFACE_NAME)
                                {
                                    return interfaceName;       // return fully qualified name of interface
                                }
                            }
                            catch (Exception e)
                            {
                                continue;
                            }
                        }
                        else if (kind>USE_VECTOR)
                        {
                            vec.add(interfaceName);
                        }
                    }
                }
            }


                // check for additional services, that may contain
            arrObj[0]=xstd.getMandatoryServices();
            arrObj[1]=xstd.getOptionalServices();

            for (int i=0; i<arrObj.length; i++)
            {
                xtd= (XTypeDescription []) arrObj[i];
                for (int k=0; k<xtd.length; k++)
                {
                    ll.add(xtd[k].getName()) ;  // another service to analyze for services
                }
            }
        }

        if (kind==QUERY_INTERFACE_OBJECT_BY_NAME) return null;

        if (kind==GET_INTERFACES_AS_STRING || kind==GET_SERVICES_AS_STRING)
        {
            StringBuffer     sb=new StringBuffer();
            boolean      bAddBlank=false;
            for (int i=0; i<vec.size();i++)
            {
                if (bAddBlank==true)
                {
                    sb.append(" ");
                }
                else
                {
                    bAddBlank=true;
                }

                sb.append(vec.get(i));
            }
            return sb.toString();
        }

        if (kind==GET_INTERFACES_AS_ARRAY || kind==GET_SERVICES_AS_ARRAY)
        {
            return vec.toArray(new String [0]); // return vector elements as a String array
        }


        return "";
    }


    /*
    / **
     * @param name which may qualify the desired interface fully or partly, case
     *        does not matter either
     *
     * @param o is the object from which the interface should be queried from
     *
     * @param bReturnString if <code>true</code> then the string encoded INTERFACE information
     *        (cf. {@link #getDefinition(Object o)})
     *        is returned, otherwise the queried interface object is returned,
     *        which can be immediately addressed to invoke
     *        the member method or access the member attribute.
     *
     * @return either a string (&quot;bReturnString==true&quot;, can be empty, if no definitions were found) or
     *         the desired interface object (&quot;bReturnString==false&quot;, can be null <code>null</code>) depending
     *         on the value of parameter
     *
     * /
    public static Object queryInterface(String name, Object o, boolean bReturnString)
    {
        if (isContextAvailable==false)
            setContext(null);           // create the default context to work with

        if (name==null || name.length()==0)     //  name not given or empty string
        {
            return null;
        }

        Object       oRes=null;
        if (bReturnString==true) oRes=(Object) "";  // if string sought, default to empty string

        int    nameLength=name.length();        // remember length of name

        XTypeProvider xtp=(XTypeProvider) UnoRuntime.queryInterface(XTypeProvider.class, o);
        if (xtp!=null)
        {
            Type [] t = xtp.getTypes();
            String tmpName="";
            int    tmpLength=0;

            for (int i=0; i<t.length; i++)
            {
                if (t[i].getTypeClass()==TypeClass.INTERFACE)   // an interface type in hand ?
                {
                    if (compareRelaxed(name, t[i].getTypeName())==true)
                    {
                        if (bReturnString==true)
                        {
                                // get XTypeDescription for Interface
                            Object [] tcInfo=determineTypeClass(t[i].getTypeName()); // [0]...XIdlClass|XTypeDescription, [1]...TypeClass
                            StringBuffer  sb=new StringBuffer();
                            tcInfo[3]       =sb; // assign StringBuffer object);
                            encodeInterface(tcInfo);    // encode Interface information
                            return (Object) sb.toString();// turn StringBuffer to String
                        }
                        else
                        {
                            return UnoRuntime.queryInterface( t[i], o);
                        }
                    }
                }
            }
        }
        return oRes;    // return default values indicating not found ("" or null)
    }
    */


    private static void mistDumpTypes(Object o)
    {
        XTypeProvider xtp=(XTypeProvider) UnoRuntime.queryInterface(XTypeProvider.class, o);
        if (xtp!=null)
        {
            Type [] t = xtp.getTypes();

            for (int i=0; i<t.length; i++)
            {
                if (bDebug==true) say("\t\t---mistDumpTypes["+i+"]="+pp(t[i])+" - "+pp(ht.get(t[i].getTypeClass())));
            }
        }
    }



    /** Appends the comma-delimited property flags/attributes to StringBuffer.
     *
     * @param sb StringBuffer to append property flags/attributes to
     * @param pf flags/attributs of the property
     */
    static private void encodePropModifier(StringBuffer sb, short pf)
    {
        int     countFlag=0;

        for (int i=1; i<=maxModifierValue; i*=2)   // loop over property attributes
        {
            // say("---> i="+pp(""+i)+" | "+pp(""+pf)+"="+pp(""+(i&pf)));
            if ((i & pf) !=0)       // pa set?
            {
                if (countFlag>0)
                {
                    sb=sb.append(",");
                }
                else
                {
                    countFlag++;
                }

                sb.append(pa.get(new Integer(i)));
            }
        }
    }


    /** Creates and returns a blank delimited string of property definitions available for the
     *  service object <code>o</code>.
     *  To find out about all available interfaces in a service object at runtime use
     *  {@link #getXTypeProviderTypeNames(Object o)},
     *  to find the interface in which a member (a method or an attribute) got defined at runtime
     *  use {@link #findInterfaceWithMember(Object o, String needle, boolean bReturnString, int howMany, boolean bExtendSearch)}.
     *
     * @param o a service object
     * @return blank delimited string of property definitions in the form
     *         <code>property-name|full-datatype-name:UNO-TypeClass:[referenced-type]:[UNTO-TypeClass]</code>.
     */
    static public String getProperties(Object o)
    {
        if (isContextAvailable==false)
            setContext(null);           // create the default context to work with

        StringBuffer sb=new StringBuffer();

        XPropertySet xps=(XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, o);
        if (xps != null)
        {
            XPropertySetInfo xpsi=xps.getPropertySetInfo();
            if (xpsi!=null)
            {
                int cntProps=0;
                Property [] props=xpsi.getProperties();     // get all properties, if any
                for (int i=0; i<props.length; i++)          // iterate over properties
                {
                    if (cntProps>0)
                    {
                        sb.append(" ");
                    }
                    else        // indicate that at least one entry is available
                    {
                        cntProps++;
                    }

                    // sb.append(props[i].Name+"|PROPERTY|");
                    sb.append(props[i].Name+"|"+ENCODE_PROPERTY_KEYWORD+"|");
                    encodePropModifier(sb, props[i].Attributes);
                    sb.append("|");

                    TypeClass tc=props[i].Type.getTypeClass();
                    // String    tName=props[i].Type.getTypeName();    // datatype

                    // String tName=(String) ht2j.get(tc);              // get Java type name or null
                    String tName=(String) ht2j_prop.get(tc);              // get Java type name or null

                    if (tName==null)    // not of a Java datatype
                    {
                        tName=props[i].Type.getTypeName();  // get UNO's mixed-case name for this type
                    }
                    sb.append(tName.replace(' ', '_')+":"+ht.get(tc)+":");

                    if (tc==TypeClass.TYPEDEF)  // get referenced datatype
                    {
                        Object [] tcInfo=determineTypeClass(tName); // [0]...XIdlClass|XTypeDescription, [1]...TypeClass
                                                                    // [2]..."o", [3] free slot
                        XTypeDescription xtd=(XTypeDescription) tcInfo[0];
                        if (xtd!=null)
                        {
                            encodeTypedefChunk(xtd, sb, ':');
                        }
                    }
                    else
                    {
                        sb.append(':');
                    }
                }
            }
        }
        return sb.toString();
    }




    /** Looks for the interface in a service object (<code>o</code>) containing a member
     *  (a method or attribute) of the given <code>name</code>. This method uses the
     *  <code>XTypeProvider</code> interface to search the available interfaces at runtime.
     *  To find out about all properties of a
     *  service object at runtime use {@link #getProperties(Object o)}.
     *
     * @param o service object to analyze
     *
     * @param needle denotes the name to look for case-independently
     *
     * @param bReturnString if <code>true</code> then the string encoded INTERFACE
     *        information
     *        (cf. {@link #getDefinition(Object o)})
     *        is returned having the denoted member(s), otherwise the queried
     *        interface object is returned, which can be immediately addressed to invoke
     *        the member method or access the member attribute.
     *
     * @param howMany determines how many members matching <code>needle</code> should be searched
     *        and returned. Only relevant, if <code>bReturnString</code> is <code>true</code>.
     *
     interfaces that contain the sought for member should
     *                be returned. A number less than 1 indicates to return all matching
     *                interface definitions (separated by a newline character 0x0A).
     *
     * @param bExtendSearch <code>true</code>: if interface cannot be found in the
     *        <code>XTYpeProvider</code> list, then extend search using full
     *        reflection; it could be the case that <code>XTYpeProvider</code>
     *        does not provide all available interfaces! (as of OOo 2.0, 2005-12-20)
     *
     * @return either a string (<code>bReturnString==true</code>, can be empty, if no definitions
     *         were found) or the desired interface object (<code>bReturnString==false</code>,
     *         can be null <code>null</code>) depending on the value of parameter
     *
     */
    static public Object findInterfaceWithMember(Object o, String needle, boolean bReturnString, int howMany, boolean bExtendSearch)
    {
// if (bDebug==true) say("findInterfaceWithMember(): just entered.\n\to="+pp(o)+"\n\tneedle="+pp(needle)+", bReturnString="+bReturnString+", howMany="+howMany+", bExtendSearch="+bExtendSearch);

        if (isContextAvailable==false)
            setContext(null);           // create the default context to work with

        if (needle==null || needle.length()==0)     //  name not given or empty string
        {
            return null;
        }

        if (howMany<1)      // make sure all available types are sought for
            howMany=65536;

        int    needleLength=needle.length(),    // remember length of name
               countFound=0;                // counts the number of interfaces that contain the sought for member
        StringBuffer   sb=new StringBuffer();
        Object       oRes=null;

        if (bReturnString==true) oRes=(Object) "";  // if string sought, default to empty string

        XTypeProvider xtp=(XTypeProvider) UnoRuntime.queryInterface(XTypeProvider.class, o);

            // define vars
        Type              [] t=null;
        String         tmpName="", interfaceName="";
        int          tmpLength=0;
        boolean bLeadInWritten=false;
        HashSet             hs=new HashSet();   // set to memorize service names that got analyzed already

        if (xtp!=null)
        {
            t=xtp.getTypes();       // get the provided types

            for (int i=0; i<t.length && countFound<howMany; i++)
            {
        if (bDebug) say("\t**** i="+i+" countFound=("+countFound+") < ("+howMany+")=howMany; needle="+pp(needle));
                if (t[i].getTypeClass()==TypeClass.INTERFACE)   // an interface type in hand ?
                {
                    interfaceName=t[i].getTypeName();      // get the interface type's name

                    if (hs.contains(interfaceName))   // already analyzed, skip
                    {
//        if (bDebug) say("\t\t**** findInterfaceWithMembers(): already handled "+pp(interfaceName)+", skipping it ...");
                        continue;
                    }
                    else                            // memorize that we analyzed this service
                    {
        if (bDebug) say("\t\t.... findInterfaceWithMembers(): working on "+pp(interfaceName)+" ...");
                        hs.add(interfaceName);
                    }


                        // do the work
                    oRes=fIWM_worker(o, needle, interfaceName, bReturnString, sb, countFound, howMany);

                    if (oRes instanceof Integer)    // to return a string
                    {
                        countFound=countFound+((Integer)oRes).intValue(); // add hit count
                    }
                    else if (oRes!=null)    // o.k. found and queried the interface object, return it
                    {
                        return oRes;
                    }
                }
            }
        }

            // o.k. we need to extend search using reflection
        if (bExtendSearch==true && countFound<howMany)
        {
// if (bDebug==true) say("OOPS! findInterfaceWithMember(): bExtendSearch=true && countFound/"+countFound+" < howMany/"+howMany+"! Extending search");

                // get the reflected interfaces in the form of an array

//            String reflInterfaces[]=(String [])getInterfaceNamesViaReflection(o, false);
            String reflInterfaces[]=(String []) extendSearch(o, null, GET_INTERFACES_AS_ARRAY);

            for (int i=0; i<reflInterfaces.length && countFound<howMany; i++)
            {
                interfaceName=reflInterfaces[i];

                if (hs.contains(interfaceName))   // already analyzed, skip
                {
                    continue;
                }
                else                            // memorize that we analyzed this service
                {
                    hs.add(interfaceName);
                }

                    // do the work
                oRes=fIWM_worker(o, needle, interfaceName, bReturnString, sb, countFound, howMany);

                if (oRes instanceof Integer)    // to return a string
                {
                    countFound=countFound+((Integer)oRes).intValue(); // add hit count
                }
                else if (oRes!=null)    // o.k. found and queried the interface object, return it
                {
                    return oRes;
                }
            }
        }


            // finally!
        if (bReturnString==true)
        {
            return sb.toString();
        }
        else
        {
            return null;
        }
    }


    static Object fIWM_worker(Object o, String needle,
                              String       interfaceName,
                              boolean      bReturnString,
                              StringBuffer sb,
                              int          countFound,
                              int          howMany
                              )
    {
        // get XTypeDescription for Interface
        Object [] tcInfo=determineTypeClass(interfaceName); // [0]...XIdlClass|XTypeDescription, [1]...TypeClass

        XInterfaceTypeDescription xitd = (XInterfaceTypeDescription)
            UnoRuntime.queryInterface(XInterfaceTypeDescription.class, tcInfo[0]);
        if (xitd==null)     // nothing to do (no XInterfaceTypeDescriptions)
        {
            return null;
        }

        Object oRes=null;
        boolean bLeadInWritten=false;    // indicates whether interface encoding leadin was written or not
        int     hits=0;

        XInterfaceMemberTypeDescription ximtd [] = xitd.getMembers();
        String mTypeName="";

        for (int k=0; k<ximtd.length && countFound<howMany; k++)
        {
            if (compareRelaxed(needle, getUnqualifiedName(ximtd[k].getMemberName()))==true)
            {
                if (bReturnString==true)
                {
                    if (bLeadInWritten==false)
                    {
                        if (sb.length()>0)
                        {
                            sb.append("\n");
                        }
                        // sb.append("INTERFACE"+"|"+interfaceName+"|");
                        sb.append(ENCODE_INTERFACE_KEYWORD+"|"+interfaceName+"|");
                        countFound++;
                        hits++;
                        bLeadInWritten=true;
                    }

                    encode4InterfaceXIMTD(sb, ximtd[k], ':');
                }
                else
                {
                    try
                    {
                        // return UnoRuntime.queryInterface(Class.forName(interfaceName), o);
                        // return UnoRuntime.queryInterface(ClassLoader.getSystemClassLoader().loadClass(interfaceName), o);
                        Class clz=null;
                        try {   // try CTXTCL first
                            clz=Thread.currentThread().getContextClassLoader().loadClass(interfaceName);
                        }
                        catch (Exception e0) {  // now try defining class loader
                            clz=Class.forName(interfaceName);
                        }

                        return UnoRuntime.queryInterface(clz, o);
                    }
                    catch (Exception e)     // did not work out
                    {
                        return null;
                    }
                }
            }
        }

        return new Integer(hits);
    }





    /** Looks up a (partially qualified) given interface name case-insensitively and returns the
     *  fully-qualified mixed case interface name.
     *
     * @param o service object to analyze
     *
     * @param name denotes the name to look for case-independently
     *
     * @param bExtendSearch <code>true</code>: if interface cannot be found in the
     *        <code>XTYpeProvider</code> list, then extend search using full
     *        reflection; it could be the case that <code>XTYpeProvider</code>
     *        does not provide all available interfaces! (as of OOo 2.0, 2005-12-20)
     *
     * @return fully-qualified interface name or empty string, if not found.
     *
     */
    static public String queryInterfaceName(Object o, String name, boolean bExtendSearch)
    {
        if (isContextAvailable==false)
            setContext(null);           // create the default context to work with

        if (name==null || name.length()==0)     //  name not given or empty string
        {
            return "";
        }

        int    nameLength=name.length();        // remember length of name

        XTypeProvider xtp=(XTypeProvider) UnoRuntime.queryInterface(XTypeProvider.class, o);

        if (xtp!=null)
        {
            String   tmpName="";
            int    tmpLength=0;

            Type []        t= xtp.getTypes();

            for (int i=0; i<t.length; i++)
            {
                if (t[i].getTypeClass()==TypeClass.INTERFACE)   // an interface type in hand ?
                {
                    tmpName=t[i].getTypeName();
// say("\t...   i="+pp(i+": ) ")+"name="+pp(name)+", tmpName="+pp(tmpName)+" ...");
                    if (compareRelaxed(name, tmpName)==true)
                    {
                        return tmpName;
                    }
                }
            }
        }

if (bDebug==true) say(RgfReflectUNO.class+": MEEEEH! queryInterfaceName() - interface "+pp(name)+" not found in XTypeProvider! bExtendSearch="+bExtendSearch);
            //
        if (bExtendSearch==true)        // use reflection
        {
            return (String) extendSearch(o, name, QUERY_INTERFACE_NAME);
        }

        return "";  // not found return empty string to indicate this
    }


    /** Looks up a (partially qualified) given service name case-insensitively and returns the
     *  fully-qualified mixed case service name.
     *
     * @param o service object to analyze
     *
     * @param name denotes the name to look for case-independently
     *
     * @return fully-qualified service name or empty string, if not found.
     *
     */
    static public String queryServiceName(Object o, String name)
    {
        if (isContextAvailable==false)
            setContext(null);           // create the default context to work with

        if (name==null || name.length()==0)     //  name not given or empty string
        {
            return "";
        }
        int    nameLength=name.length();        // remember length of name

        XServiceInfo xsi=(XServiceInfo) UnoRuntime.queryInterface(XServiceInfo.class, o);
        if (xsi==null)      // oops, nothing to do !
        {
            return "";
        }

        String [] services=xsi.getSupportedServiceNames();    // get names of all implemented services
        String   tmpName="";
        int    tmpLength=0;

        // assume that getSupportedServiceName() contains our sought for service
        for (int i=0; i<services.length; i++)
        {
            if (compareRelaxed(name, services[i])==true)
            {
                // return "SERVICE|"+services[i];
                // return ENCODE_SERVICE_KEYWORD+"|"+services[i];
                return services[i];
            }
        }

            // not yet found: could be that service exists, but did not get reported, hence
            // go through all service-definitions and look for the service name, analyzing all
            // service definitions one encounters
        LinkedList ll=new LinkedList();     // Queue-like (cf. Rexx)
        for (int i=0; i<services.length; i++)
        {
            ll.add(services[i]);        // add service to array
        }

            // now iterate over services, analyze them and compare them
        String serviceName;
        boolean bCheck=false;
        int    counter=0;
        HashSet hs=new HashSet();   // set to memorize service names that got analyzed already

        while (ll.size()>0)
        {
            serviceName=(String) ll.removeFirst();   // get service name
            if (hs.contains(serviceName))   // already analyzed, skip
            {
                continue;
            }
            // memorize that we analyzed this service
            hs.add(serviceName);

            Object           []  tcObj=null;
            Object           [] arrObj=new Object[2];
            XTypeDescription []    xtd=null;

            if (bCheck==true)
            {
// if (bDebug) say("\t\t---> queryServiceName(): extended search;"+", checking serviceName="+pp(serviceName)+" for name="+pp(name));
                if (compareRelaxed(name, serviceName)==true)
                {
                    return serviceName;
                }
            }
            else    // skip those top-level services which we have already checked
            {
                counter++;
                bCheck=services.length<counter;

// if (bDebug) say("\t\t===> queryServiceName(): looking for "+pp(name)+", skipped comparison of "+pp(serviceName)+", services.length="+services.length+" < counter="+counter+": bCheck="+bCheck);
            }

                // not found, now reflect for other services, start out with mandatory ones
            tcObj=determineTypeClass(serviceName);

            XServiceTypeDescription xstd=(XServiceTypeDescription) UnoRuntime.queryInterface(
                            XServiceTypeDescription.class, tcObj[0]);

            if (xstd==null) continue;       // could not get an interface object, continue

            arrObj[0]=xstd.getMandatoryServices();
            arrObj[1]=xstd.getOptionalServices();

            for (int i=0; i<arrObj.length; i++)
            {
                xtd= (XTypeDescription []) arrObj[i];
                for (int k=0; k<xtd.length; k++)
                {
                    serviceName=xtd[k].getName();

// if (bDebug)  say("\t\t  => queryService(): checking "+pp(name)+" LinkedList item # i="+i+"/"+arrObj.length+", service # k="+k+"/"+xtd.length+", serviceName="+pp(serviceName));

                    if (compareRelaxed(name, serviceName)==true)
                    {
                        return serviceName;
                    }
                    ll.add(serviceName);    // another service to analyze for services
                }
            }
        }

        return "";  // not found return empty string to indicate this
    }



    /** Compares two strings in a &quot;relaxed&quot; manner, i.e.
     *  tests case-insensitively, whether the second argument
     *  <code>haystack</code> ends with the first argument <code>endName</code>
     *  string.
     *
     * @param endName the string which should end <code>haystack</code>
     * @param haystack the string to test <code>endName</code> against
     *
     * @return <code>true</code>, if <code>haystack</code> ends with the
     *         string <code>endName</code> (comparison carried out case-insensitively),
     *         <code>false</code> else
     */

    static boolean compareRelaxed(String endName, String haystack)
    {
        int endNameLength=endName.length(),
            tmpLength    =haystack.length();

        if (endNameLength>tmpLength)        // interface endName is shorter than the sought of one
        {
            return false;
        }
        else if (endNameLength!=tmpLength)  // cut off haystack from the right to match length of received endName
        {
            //             012345678
            //     abc=3   x.y.z.abc=9  9-3=6
            haystack=haystack.substring(tmpLength-endNameLength);    // cut off from the right
        }

        return endName.equalsIgnoreCase(haystack);
    }



    /** Convenience method, will print received string to <code>System.err</code>.
     */
    static void say (String s)
    {
        System.err.println(s);
    }

    /** Convenience method, returns argument enclosed in angle brackets, allows <code>null</code> as well.
     */
    static String pp (Object o)
    {
        if (o==null) return "<null>";
        return "<"+o+">";
    }

    /** Returns unqualified name (string after the last dot) from dotted string or string itself, if no dot in string.
     *
     * @param s String to extract unqualified name
     * @return returns unqualified name or s, if no dot in string
     */
    // static int rgfRun=0;
    static String getUnqualifiedName(String s)
    {
        int    lastPos=s.lastIndexOf('.');          // get position of last dot
        return lastPos==-1 ? s : s.substring(lastPos+1) ;
    }


    /** 2005-12-20: turns out that OOo 2.0 and 1.5 have the module name
     *  <code>&quot;com.sun.star.text.TextField.&quot;</code>
     *  spelled wrongly, <code>&quot;.TextField&quot;</code>
     *  should be spelled in lowercase: <code>&quot;.textfield.&quot;</code>.
     */
    static String check4reflection (String name)
    {
        if (name.startsWith("com.sun.star.text.TextField.")==true)
        {
            return "com.sun.star.text.textfield."+name.substring(28);
        }
        return name;
    }

}


