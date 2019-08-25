package org.rexxla.bsf.engines.rexx;

/* Test command lines:

java org.rexxla.bsf.engines.rexx.ClassAdapter java.io.File Aloha getAbsoluteFile isFile deleteOnExit listFiles compareTo createTempFile
java org.rexxla.bsf.engines.rexx.ClassAdapter javax.swing.filechooser.FileFilter Aloha append oha nixiNAXI accept

java org.rexxla.bsf.engines.rexx.ClassAdapter java.awt.Frame Brumm getState getTitle setTitle "<init>" "java.awt.Container paint" "java.awt.Container <init>" "nixi.noxi.org oho"


java org.rexxla.bsf.engines.rexx.ClassAdapter TestAbstract Caesar

*/

/*
    2010-11-30, rgf:   - fixed bug in createConstructorsBase() which caused illegal class files to be created
    2010-05-31, rgf:   - use the ClassLoader of the class to proxy, otherwise dynamically created Java classes cannot be extended
    2014-08-30, rgf:   - subclasses are allowed to access protected methods in superclasses, adjust for this
    2014-08-31, rgf:   - if methods to proxy is null, then proxy all public and protected declared methods,
                         if methods is an empty array, then just proxy the constructors
    2016-11-01, rgf:   - do not use the ClassLoader from the target's class (e.g. javafx.* classes in Java 1.8/8
                         use their own class loaders which may confine them to see only javafx.*)
    2016-11-04, rgf:   - fixed bug that did not extend an abstract class, if not methods to proxy were supplied
                         (which will be the case when invoked by RexxProxy.newExtendedProxyInstance(...))

    2016-11-03, rgf:   - if no methods given, but an abstract class, then proxy all abstract (public or protected) methods only
    2016-11-05, rgf:   - basing on 1.6/6.0, get rid of generic warnings
    2016-11-07, rgf:   - remove reflective usage of Method.isSynthetic() as we can now rely it to be available
    2017-03-09, rgf:   - added option to implement any number of interface classes to the extended Java class
                         (cf. the changes to BSF.CLS' routine bsf.createProxyClass)
    2017-03-13, rgf:   - redo to allow full replacement of ASM, added functionality to create classes off interface(s) only
                       - changed "bss" to the self documenting name "bShowSource"
    2017-06-17, rgf:   - if the context class loader is null, make sure that the SimpleCompiler's parent class loader
                         gets set; this version uses the class loader for ClassAdapter.
    2018-02-19, rgf:   - when creating interface methods, make sure that only abstract interface methods get proxied;
                         starting with Java 1.8/8 there are concrete interface methods possible (static and default interface
                         methods)
    2019-08-13, rgf:   - corrected Javadoc warnings
    2019-08-21, rgf:   - renamed ProxiedJavaClassTool.java to ClassAdapter.java
    2019-08-24, rgf:   - there will always methods for public/protected abstract methods and constructors created
                         - if message name array is null or an empty Array, then
                           only abstract methods and constructors get created
                         - if message name array has a single entry "*", then
                           all declared public/protected methods get created
                         - else all array elements that refer to public/protected listed methods will be honored
    2019-08-25, rgf:    - supplying additional interface classes should be also honored for extending abstract classes
                        - fix bug that only honored one interface method (instead of honoring all)
*/

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;

import org.apache.bsf.BSFException;
import org.rexxla.bsf.engines.rexx.RexxProxy;
import org.rexxla.bsf.engines.rexx.RexxException;

import org.codehaus.janino.SimpleCompiler;

/**
  * This class creates a proxied Java class on the fly, which has concrete implementations for all
 *  abstract (inherited or not) methods. Additionally it allows for creating proxy methods for
 *  concrete methods and matching <&quot;>_forwardToSuper<&quot> methods to allow ooRexx to forward
 *  proxied Java method invocations to their original target on the Java side.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2010-2019 Rony G. Flatscher
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
  * @version 1.04, 2010-03-27, 2010-04-14, 2010-04-17, 2010-04-18, 2010-11-30, 2011-05-31, 2014-08-30, 2016-11-01, 2016-11-04/5, 2017-03-13, 2019-08-24
  * @since 2010
  * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu.ac.at">http://www.wu.ac.at</a>)
  */

public class ClassAdapter
{
    private final static String STR_REXX_PROXY="org.rexxla.bsf.engines.rexx.RexxProxy";
        // flags
    private final static int    METHOD_SIGNATURE=1;
    private final static int    THROWS_CLAUSE   =2;

        // switches
        // the bDebug switch should remain private and final: set to false should cause javac to remove any dependent code
    private final static boolean bDebug = false;    // true;

        // public to allow changes from anywhere
    public static boolean bTiming    = false; // true;
    public static boolean bShowSource= false; // true; // if set to .true, the generated source is printed to stderr

     /** Version information on this class. */
    static final public String version="104.20190825";

    /** Allow using this class from the command line to create an extended Java class and displaying the generated source.
     *  Example: <br>
     *  <code>java java org.rexxla.bsf.engines.rexx.ClassAdapter javax.swing.AbstractAction ronysClass "*"</code>
     *
     *  @param args array of blank delimited Strings in the form of &quot;fullJavaClassName [newClassName [methodToProxy [...]]&quot;. If there is
     *         a single method to proxy with a value of &quot;*&quot; (asterisk), then all declared methods will get proxied.
     */
    public static void main (String args[])
                    throws BSFException
    {
        if (args.length!=0)
        {
            // define defaults
            Class   javaClassToProxy   =null;
            String  newClzName         =null;
            String  []methodNames2proxy=null;

                // try to load class
            try
            {
                javaClassToProxy=ClassAdapter.class.getClassLoader().loadClass(args[0]);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                // System.err.println(e+"\n---");
                System.err.println("--- The (premature) end. ---");
                System.exit(-1);
            }

            if (args.length>1)
            {
                newClzName=args[1];
            }

            if (args.length>2)
            {
                int nr=args.length-2;   // number of remaining arguments

                methodNames2proxy=new String[nr];
                for (int k=0; k<nr; k++)
                {
                    methodNames2proxy[k]=args[k+2];
                }
            }

            try
            {
                bTiming=true;       // show timing information
                bShowSource= true;  // show generated source code
                createProxiedJavaClass( javaClassToProxy, newClzName, methodNames2proxy, null);
            }
            catch (Exception e)
            {
                System.err.println(e+"\n---");
            }
        }
    }


    /* Do not allow argument less instances of this class. */
    private ClassAdapter ()
    {}

    /** Creates and returns a proxy class in which Java method invocations get forwarded to
     *  a RexxProxy.
     *
     *  @param  javaClassToProxy Class object to use for proxying
     *  @param  newClzName <code>null</code> or name for proxy class to create
     *  @param  methodNames2proxy an array denoting the protected or public methods to proxy. Please note:
     *          each proxied class will get all public/protected abstract methods and constructors
     *          created (proxied).<br/>
     *          Each method name element denotes a public or protected single method which may be optionally
     *          preceeded by a blank and preceeded by the name of some Java superclass to be used for looking
     *          up the method definitions to proxy.
     *
     *          The following special cases apply for this argument:
     *          <ul>
     *          <li>if this argument is <code>null</code>, then only the public/protected abstract methods and
     *              constructors get proxied.
     *          </li>
     *
     *          <li>if this argument has no elements (its <code>length</code>field is <code>0</code>), then
     *              only the public/protected abstract methods and constructors get proxied.
     *          </li>
     *
     *          <li>if this argument has a single element with the value <code>"*"</code> (asterisk), then
     *              all public/protected methods and constructors get proxied.
     *          </li>
     *          </ul>
     *
     *  @param   interfaceClasses optional, the extended class should in addition implement all abstract
     *           methods of these interface classes
     *
     *  @return the class object representing the extended class
     */

// rgf, 2019-08-24:
//    java.security.MessageDigest.getInstance("SHA-256");
/*  from: https://www.baeldung.com/java-md5

    @Test
    public void givenPassword_whenHashing_thenVerifying()
      throws NoSuchAlgorithmException {
        String hash = "35454B055CC325EA1AF2126E27707052";
        String password = "ILoveJava";

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        String myHash = DatatypeConverter
          .printHexBinary(digest).toUpperCase();

        assertThat(myHash.equals(hash)).isTrue();
    }
*/

    public static Class createProxiedJavaClass(final Class    javaClassToProxy,
                                                     String   newClzName,
                                               final String[] methodNames2proxy,
                                               final Class[]  interfaceClasses
                                               )
                                        throws BSFException
    {
if (bDebug==true)
{
   System.err.println("... arrived in \"public static Class createProxiedJavaClass(...)\"");
   System.err.println("\targ #1, javaClassToProxy : ["+javaClassToProxy.toString()+"]");
   System.err.println("\targ #2, newClzName       : ["+newClzName                 +"]");
   System.err.println("\targ #3a, methodNames2proxy.length=["+methodNames2proxy.length+"]");
   System.err.println("\targ #3, methodNames2proxy: ["+(methodNames2proxy==null ? null : Arrays.toString(methodNames2proxy))+"]");
   System.err.println("\targ #4, interfaceClasses : ["+(interfaceClasses==null  ? null : Arrays.toString(interfaceClasses)) +"]");
}

        long crtSrcStartTime=0l, crtSrcEndTime=0l;
        if (bTiming==true) crtSrcStartTime=System.currentTimeMillis(); // System.nanoTime(); // currentTimeMillis();

            // process arguments
        if (javaClassToProxy==null)     // do not accept null !
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "ClassAdapter.createProxiedJavaClass(...): argument \"javaClassToProxy\" must not be null");
        }

        if (javaClassToProxy.isArray())     // do not accept Array classes
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "ClassAdapter.createProxiedJavaClass(...): argument \"javaClassToProxy\" must not be an array class");
        }

        int classModifiers=javaClassToProxy.getModifiers();

        if (!Modifier.isPublic(classModifiers))
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "ClassAdapter.createProxiedJavaClass(...): argument \"javaClassToProxy\", value=["+javaClassToProxy+"]: is not public");
        }

        if (Modifier.isFinal(classModifiers))  // only accept non-final classes
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "ClassAdapter.createProxiedJavaClass(...): argument \"javaClassToProxy\", value=["+javaClassToProxy+"]: must not be final");
        }

        String argNewClzName=newClzName;    // save argument value

        if (newClzName==null)   // no name for the proxied Java class given?
        {
            newClzName=javaClassToProxy.getName()+"_$RexxExtendClass$_"+(new Object()).hashCode();
        }

        String [] methods2proxy=null;
        boolean isAbstractClz=Modifier.isAbstract(classModifiers);

        // proxy all public and protected declared methods if there is a single method name argument with a
        // single asterisk character (*)
        boolean bProxyAllDeclaredMethods=( methodNames2proxy!=null &&
                                           methodNames2proxy.length==1 && methodNames2proxy[0].equals("*") );

// System.err.println("ClassAdapter: bProxyAllDeclaredMethods=["+bProxyAllDeclaredMethods+"]");

        if (bProxyAllDeclaredMethods)
        {
            Method meths[]=javaClassToProxy.getDeclaredMethods();
            HashSet<String> methNames= new HashSet<String>();
            for (int i=0; i<meths.length; i++)
            {
                int mods=meths[i].getModifiers();
                if (Modifier.isPublic(mods) || Modifier.isProtected(mods))
                {
                    String methName=meths[i].getName();

                        // do not proxy methods toString(), equals(), hashCode() by default as this may break the JVM
                    if (methName.equals("toString") || methName.equals("equals") || methName.equals("hashCode") )
                    {
                        continue;
                    }
                    else if (Modifier.isAbstract(mods)==false)  // only proxy method if not abstract (abstract methods will get processed later)
                    {
                        methNames.add(meths[i].getName());
                    }
                }
            }
            // turn into String array (toArray will create and return an appropriately dimensiond String array object)
            methods2proxy= (String[]) methNames.toArray(new String[0]);
        }
        else    // use supplied array of method names to proxy
        {
            methods2proxy=methodNames2proxy;
        }


            // create Java program, use a StringBuffer
        StringBuffer sb=new StringBuffer();

        RunInfos ri=new RunInfos();
            // save mapping of class name to its class object for later use
        ri.uCN2clz.put(javaClassToProxy.getName().toUpperCase(), javaClassToProxy);


// ---------------------------------------------------------------------------

        String packageName="org.rexxla.bsf.engines.rexx.onTheFly";
            // extract simple class name
        int lpos=newClzName.lastIndexOf('.');
        if (lpos>=0)
        {
            if (argNewClzName!=null)    // use argument's package name
            {
                packageName=newClzName.substring(0,lpos);
            }

            newClzName=newClzName.substring(lpos+1);
        }
        else
        {
            packageName=null;
        }

        String fullNewClzName=newClzName;
        if (packageName!=null)  // only supply a package name, if original class has one as well, otherwise loadClass() may experience problems later
        {
            sb.append("package ").append(packageName).append("; \n");
            fullNewClzName=packageName+"."+newClzName;
        }

// ---------------------------------------------------------------------------

        createProlog(sb, newClzName, javaClassToProxy, interfaceClasses, ri); // create prolog code

// ---------------------------------------------------------------------------

        createMethods(sb, newClzName,  javaClassToProxy, methods2proxy, ri);

// ---------------------------------------------------------------------------

        // if constructors were not created by demand of user, do not proxy them to the RexxProxy
        createConstructorsBase(sb, newClzName,  javaClassToProxy, false, ri); // create base constructors

// ---------------------------------------------------------------------------

            // create proxies for public constructors; if abstract class in hand, check whether superclass
            // is abstract as well and if so, proxy its public constructors and abstract methods too, such
            // that we can supply the RexxProxy-handler when creating an instance
        Class tmpClz=javaClassToProxy;
        do
        {
            if (bDebug==true) System.err.println("createProxiedJavaClass(): tmpClz=["+tmpClz+"]");

            // createConstructors(sb, newClzName,  javaClassToProxy, bProxyConstructors, isAbstractClz); // create addtional, constructors
            createConstructors(sb, newClzName,  tmpClz, false, ri); // create addtional, constructors

            // if an abstract class, check its superclass for abstractness; and if so
            // proxy its public constructors as well
            if (!Modifier.isAbstract(tmpClz.getModifiers()))
            {
                break;
            }

            sb.append("\n\t// --- creating methods for public and protected abstract methods in class ["+tmpClz.getName()+"], if not yet created \n");

            // create abstract methods
            Method []declMethods=null;
            if (ri.clz2declMethods.containsKey(tmpClz))     // declared methods for this class already available
            {
                declMethods=(Method []) ri.clz2declMethods.get(tmpClz);
            }
            else    // get declared methods, cache them, in case we need them for processing the method list later
            {
                declMethods=tmpClz.getDeclaredMethods();
                ri.clz2declMethods.put(tmpClz, declMethods);// save declared methods
            }

            for (int i=0; i<declMethods.length; i++)        // create implementations for abstract methods
            {
                Method m=declMethods[i];
                if (bDebug==true) System.err.println("createProxiedJavaClass(): declMethods, i=["+i+"], m=["+m.toString()+"]");

                int methModifiers=m.getModifiers();         // get Methods's modifiers
                // if ( Modifier.isAbstract(m.getModifiers())) // an abstract method in hand ?
                if ( Modifier.isAbstract(methModifiers))   // an abstract method in hand ?
                {
                    // only create proxy method if public or protected
                    if (Modifier.isPublic(methModifiers) || Modifier.isProtected(methModifiers) )
                    {
                        createMethod(sb, m, newClzName, ri);         // create method code, do not create forwardToSuper()
                    }
                }
            }

                // does the abstract class declare to implement interfaces? Might be, that
                // some of the interface methods are not implemented, implement then, if so
            for (Class tmpIClz : tmpClz.getInterfaces())    // iterate over declared interfaces and process them
            {
                createInterfaceMethods(sb, newClzName, tmpIClz, ri, tmpClz);
            }

            tmpClz=tmpClz.getSuperclass();
            if (tmpClz!=null && !Modifier.isAbstract(tmpClz.getModifiers()) )
            {
                break;
            }
        } while ( tmpClz!=null );


// ---------------------------------------------------------------------------

        // checking extending class with the supplied interface classes, create methods, if necessary
        if (bDebug) System.err.println("createProxiedJavaClass(): extending the concrete class with interfaceClasses=["+interfaceClasses+"], .length=["+(interfaceClasses!=null?interfaceClasses.length:null)+"]");

        if (interfaceClasses!=null)
        {
            for (Class tmpIClz : interfaceClasses)
            {
                createInterfaceMethods(sb, newClzName, tmpIClz, ri, javaClassToProxy);
            }
        }

// ---------------------------------------------------------------------------

        createEpilog(sb);                               // create epilog code (supply closing curly bracket)

// ---------------------------------------------------------------------------

        if (bShowSource)    // show the source before compiling
        {
            System.err.println("/// ClassAdapter.createProxiedJavaClass(...) ---> begin of generated code for class: ["+newClzName+"], now: ["+fullNewClzName+"]\n"+sb.toString()+"\n<--- end of generated code \\\\\\");
        }

        if (bTiming==true) crtSrcEndTime=System.currentTimeMillis(); // System.nanoTime(); // System.currentTimeMillis();


        SimpleCompiler sc=new SimpleCompiler();

        Class clz=null;

        long startTime=0l, endTime=0l, endOfLoadTime=0l;

        try {
            if (bTiming==true)  startTime=System.currentTimeMillis(); // System.nanoTime(); // System.currentTimeMillis();

            // 2017-06-17, rgf: if no context class loader make sure SimpleCompiler gets a parent class loader
            if (Thread.currentThread().getContextClassLoader()==null)
            {
                sc.setParentClassLoader(ClassAdapter.class.getClassLoader());
            }

            sc.cook(sb.toString());             // compile program

            if (bTiming==true) endTime=System.currentTimeMillis(); // System.nanoTime(); // System.currentTimeMillis();

            clz=sc.getClassLoader().loadClass(fullNewClzName);

            if (bTiming==true) endOfLoadTime=System.currentTimeMillis(); // System.nanoTime(); // System.currentTimeMillis();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new BSFException (BSFException.REASON_OTHER_ERROR, e.toString(), e);
        }

        if (bTiming==true)
        {
            System.err.println("clz=["+clz+"]");
            System.err.println("creating source  (in milliseconds): "+(crtSrcEndTime-crtSrcStartTime));

            System.err.println("compilation time (in milliseconds): "+(endTime-startTime));
            System.err.println("load time        (in milliseconds): "+(endOfLoadTime-endTime));
        }

        return clz;
    }




    // create import statements, class definition, fields and setter/getter for default RexxProxy handler
    // 2017-03-11, rgf: support Interface class(es) only
    /** Creates the prolog statements (import, class definition, fields, getter/setter for default
     *  RexxProxy handlers.
     *
     * @param sb the StringBuffer to append the statements to
     * @param newClzName the new name of the new class
     * @param javaClassToProxy the Java (concrete, abstract, interface) class that gets extended
     * @param interfaceClasses the extended class should implement
     * @param ri RunInfos runtime infos for managing the process
     */
    static void createProlog (StringBuffer sb, String newClzName, Class javaClassToProxy, Class[] interfaceClasses, RunInfos ri)
                throws BSFException
    {
        sb.append("import org.rexxla.bsf.engines.rexx.RexxProxy; \n");
        // sb.append("import org.rexxla.bsf.engines.rexx.RexxException; \n");
        sb.append("import org.apache.bsf.BSFException; \n");

        sb.append("\npublic class ");
        sb.append( newClzName );

        boolean isJavaClassToProxyAnInterface=javaClassToProxy.isInterface();
        if (!isJavaClassToProxyAnInterface)
        {
            sb.append(" extends ").append(javaClassToProxy.getName());
        }

            // determine whether javaClassToProxy is an interface class itself

        if ( (interfaceClasses!=null && interfaceClasses.length>0) || isJavaClassToProxyAnInterface)       // 2017-02-09, rgf: add "implements "
        {
            sb.append("\n\t\timplements");
            boolean bAddComma=false;

            if (isJavaClassToProxyAnInterface)  // indicate that this interface class is implemented by the concrete class
            {
                sb.append(' ').append(javaClassToProxy.getName());
                bAddComma=true;
            }

            if (interfaceClasses!=null)         // no array of interfaceClasses supplied
            {
                for (Class iclz: interfaceClasses)  // iterate over interface classes
                {
                    if (bAddComma)
                    {
                        sb.append(",");
                    }
                    sb.append(' ').append(iclz.getName());  // add fully qualified interface class name
                    if (bAddComma==false)
                    {
                        bAddComma=true;
                    }
                }
            }
        }
        sb.append("\n{\n");

        sb.append("\tpublic  static          Object    classLock     =new Object(); \n");
        sb.append("\tprivate static volatile RexxProxy defaultHandler=null;         \n");
        sb.append("\tprivate                 RexxProxy target        =null;         \n\n");


            // define getDefaultHandler()
        sb.append("\n\tpublic static RexxProxy getDefaultHandler() \n");
        sb.append("\t{\n");
        sb.append("\t\treturn defaultHandler; \n");
        sb.append("\t}\n\n");

        HashSet<String> hs=new HashSet<String>();   // memorize that we created a method with this signature
        hs.add("()");
        ri.uMN2setOfSignatures.put("getDefaultHandler", hs);


            // define setDefaultHandler()
        sb.append("\n\tpublic static void setDefaultHandler(RexxProxy rexxproxy) \n");
        sb.append("\t{\n");
        sb.append("\t\tsynchronized (classLock)    \n");
        sb.append("\t\t{                           \n");
        sb.append("\t\t\tdefaultHandler=rexxproxy; \n");
        sb.append("\t\t}                           \n");
        sb.append("\t}\n\n");

        hs=new HashSet<String>();           // memorize that we created a method with this signature
        hs.add("(org.rexxla.bsf.engines.rexx.RexxProxy)");
        ri.uMN2setOfSignatures.put("setDefaultHandler", hs);


            // define getTargetRexxProxy()
        sb.append("\n\tpublic synchronized RexxProxy getTargetRexxProxy() \n");
        sb.append("\t{\n");
        sb.append("\t\treturn target; \n");
        sb.append("\t}\n\n");

        hs=new HashSet<String>();           // memorize that we created a method with this signature
        hs.add("()");
        ri.uMN2setOfSignatures.put("getTargetRexxProxy", hs);


            // define setTargetRexxProxy()
        sb.append("\n\tpublic synchronized void setTargetRexxProxy(RexxProxy newTarget) \n");
        sb.append("\t{\n");
        sb.append("\t\ttarget=newTarget; \n");
        sb.append("\t}\n\n");

        hs=new HashSet<String>();           // memorize that we created a method with this signature
        hs.add("(org.rexxla.bsf.engines.rexx.RexxProxy)");
        ri.uMN2setOfSignatures.put("setTargetRexxProxy", hs);
    }



    /** Creates the epilog statement.
     *
     * @param sb the StringBuffer to append the statements to
     */
    static void createEpilog(StringBuffer sb)
    {
        sb.append("}\n");   // close class block
    }


    /** Creates the base constructor (the default constructor and the default constructor
     *  with a RexxProxy argument) statements, if possible.
     *
     * @param sb the StringBuffer to append the statements to
     * @param newClzName the new name of the new class
     * @param javaClassToProxy the Java (abstract) class that gets extended
     * @param bProxyConstructors determines whether constructor invocations get proxied to the RexxProxy (<code>true</code>)
     * @param ri RunInfos runtime infos for managing the process
     */
    static void createConstructorsBase(StringBuffer sb, String newClzName, Class<?> javaClassToProxy,
                              boolean bProxyConstructors, RunInfos ri)
    {
if (bDebug==true) System.err.println("createConstructorsBase(), begin...");

            // determine whether super's default constructor exists (if so, it needs to be invoked)
        String throwsClauses[]=new String[] {"", "throws BSFException"};    // default values

        if (javaClassToProxy.isInterface()) // implmenting a Java interface means that we have to implement the default constructor
        {
            sb.append("\n\t// --- creating base (default) constructor \n");

                // define default constructor
            sb.append("\n\tpublic "+newClzName+"() "+(bProxyConstructors ? throwsClauses[1] : throwsClauses[0])+"\n");
            sb.append("\t{\n");
            sb.append("\t\tsuper();                    \n");
            sb.append("\t\ttarget=defaultHandler;      \n");

            if (bProxyConstructors)
            {
                sb.append("\t\ttarget.invoke(this, \"<init>\", \"public "+newClzName+"()\", null);   \n");
            }
            sb.append("\t}\n\n");


            // define constructor with one RexxProxy (the target for handling invocations)
            sb.append("\n\tpublic "+newClzName+"(RexxProxy rexxproxy) "+(bProxyConstructors ? throwsClauses[1] : throwsClauses[0])+" \n");
            sb.append("\n");
            sb.append("\t{\n");
            sb.append("\t\tsuper();                 \n");
            sb.append("\t\ttarget=rexxproxy;        \n");
                // make sure defaultHandler gets set at least now that an instance gets created (may be
                // too late sometimes, hence programmer should make sure it got set before instantiating the class)
            sb.append("\t\tif (defaultHandler==null)   \n");
            sb.append("\t\t{                           \n");
            sb.append("\t\t\tdefaultHandler=rexxproxy; \n");
            sb.append("\t\t}                           \n");
            if (bProxyConstructors)
            {
                sb.append("\t\ttarget.invoke(this, \"<init>\", \"public "+newClzName+"(RexxProxy rexxproxy)\", new Object[] {rexxproxy});   \n");
            }
            sb.append("\t}\n\n");
            return;     // done for implementing this interface class
        }


        boolean bDefaultConstructorExistsInSuper=false;
        try
        {
            Constructor tmpConstr=javaClassToProxy.getDeclaredConstructor( new Class[] {} );
            // if constructor is private, cannot reach it, behave as if not existent
            // bDefaultConstructorExistsInSuper = Modifier.isPublic(tmpConstr.getModifiers());
            int mods=tmpConstr.getModifiers();
            bDefaultConstructorExistsInSuper = Modifier.isPublic(mods) || Modifier.isProtected(mods) ;

            if (bDefaultConstructorExistsInSuper==false)    // not reachable
            {
if (bDebug==true) System.err.println("createConstructorsBase(), default constructor not reachable, RETURNING prematurely.");
                return;
            }

                // return new String[] { throwsClauseOri, throwsClauseWithBSFException };
            throwsClauses=createThrowsClause(tmpConstr.getExceptionTypes());
        }
        catch (Exception e)
        {
if (bDebug==true) System.err.println("createConstructorsBase(), no default constructor defined in javaClassToProxy, hence cannot create these base constructors, returning.");
            return;
        }




        String strMethName="<INIT>";
        HashSet<String> hs=(HashSet<String>) ri.uMN2setOfSignatures.get(strMethName);
        if (hs==null)
        {
if (bDebug==true) System.err.println("createConstructorsBase(), no HashSet for logging created methods/constructors, had to CREATE one here <--- !");
            hs=new HashSet<String>();
            ri.uMN2setOfSignatures.put(strMethName, hs);
        }

if (bDebug==true)
{
    java.util.Iterator i=hs.iterator();

    while (i.hasNext()==true)
    {
        System.out.print("...["+i.next()+"] ");
    }
    System.out.println(" <--- <--- <---\n");

}


        String methSignature="public "+javaClassToProxy.getName()+"()";
        String methSignature2="()";   // for internal use

        // ------------------------ default constructor: () -------------------------
            // this constructor exists in super and was not created yet: create it!
            // ---rgf, 2010-11-30 if (bDefaultConstructorExistsInSuper && (!hs.contains(methSignature2)))
            // if a reachable default constructor exists in superclass, create one here as well
            //      note: an existing public constructor may already have been handled with injecting
            //            a RexxProxy as first (and single) argument
        if (bDefaultConstructorExistsInSuper)
        {
if (bDebug==true) System.err.println("createConstructorsBase(), creating: ["+methSignature2+"] ...");

            sb.append("\n\t// --- creating base (default) constructor \n");

                // define default constructor
            sb.append("\n\tpublic "+newClzName+"() "+(bProxyConstructors ? throwsClauses[1] : throwsClauses[0])+"\n");
            sb.append("\t{\n");

            if (bDefaultConstructorExistsInSuper)
            {
                sb.append("\t\tsuper();                    \n");
            }

            sb.append("\t\ttarget=defaultHandler;      \n");

            if (bProxyConstructors)
            {
                sb.append("\t\ttarget.invoke(this, \"<init>\", \""+methSignature+"\", null);   \n");
            }

            sb.append("\t}\n\n");

            // rgf, 2010-11-30a
            if (!hs.contains("()"))
            {
                Constructor tmpConstr=null;
                boolean bSingleRexxProxyConstructor_ExistsInSuper=true; // assume it exists in superclass
                try
                {
                    tmpConstr=javaClassToProxy.getDeclaredConstructor( new Class[] {RexxProxy.class} );
                    // if constructor is private, cannot reach it, behave as if not existent
                    if (Modifier.isPrivate(tmpConstr.getModifiers()))
                    {
                        tmpConstr=null;
                        throwsClauses=new String[] {"", "throws BSFException"};    // default values
                    }
                    else
                    {
                           // return new String[] { throwsClauseOri, throwsClauseWithBSFException };
                        throwsClauses=createThrowsClause(tmpConstr.getExceptionTypes());
                    }
                }
                catch (NoSuchMethodException nsme)  // if it does not exist yet, we must not invoke it
                {
                    bSingleRexxProxyConstructor_ExistsInSuper=false;
                }


/* rgf, 2010-11-30 --> */
if (bDebug==true) System.err.println("createConstructorsBase(), creating default constructor "+"[(RexxProxy rexxproxy)]"+" ...");

                sb.append("\n\tpublic "+newClzName+"(RexxProxy rexxproxy) "+(bProxyConstructors ? throwsClauses[1] : throwsClauses[0])+" \n");

                sb.append("\t{\n");
                if (tmpConstr==null)    // no constructor of this type declared as of yet
                {
                    if (bDefaultConstructorExistsInSuper)
                    {
                        sb.append("\t\tsuper();                 \n");
                    }
                }

                else if (bSingleRexxProxyConstructor_ExistsInSuper) // forward to superclass constructor with the same argument
                {
                    sb.append("\t\tsuper(rexxproxy);        \n");
                }

                sb.append("\t\ttarget=rexxproxy;        \n");

                    // make sure defaultHandler gets set at least now that an instance gets created (may be
                    // too late sometimes, hence programmer should make sure it got set before instantiating the class)
                sb.append("\t\tif (defaultHandler==null)   \n");
                sb.append("\t\t{                           \n");
                sb.append("\t\t\tdefaultHandler=rexxproxy; \n");
                sb.append("\t\t}                           \n");
/* ---rgf, 2010-11-30  */


                if (tmpConstr!=null)
                {
                    methSignature=tmpConstr.toString();
                }
                else
                {
                    methSignature="public "+javaClassToProxy.getName()+"("+STR_REXX_PROXY+")";
                }
                hs.add(methSignature2);     // memorize this

                if (bProxyConstructors)
                {
                    sb.append("\t\ttarget.invoke(this, \"<init>\", \""+methSignature+"\", new Object[] {rexxproxy});   \n");
                }
                sb.append("\t}\n\n");

            }

            // rgf, 2010-11-30b
            hs.add(methSignature2);     // memorize this
        }
else if (bDebug==true) System.err.println("createConstructorsBase(), creating: ["+methSignature2+"] - ALREADY CREATED!");


    }


    static void createConstructors(StringBuffer sb, String newClzName, Class javaClassToProxy,
                              boolean bProxyConstructors, RunInfos ri)
    {
if (bDebug==true) System.err.println("createConstructors(), begin...");
            // fetch set of methods created
        HashSet<String> hs=(HashSet<String>) ri.uMN2setOfSignatures.get("<INIT>");
        if (hs==null)
        {
            hs=new HashSet<String>();
            ri.uMN2setOfSignatures.put("<INIT>", hs);
        }


if (bDebug==true)
{
    java.util.Iterator i=hs.iterator();

    while (i.hasNext()==true)
    {
        System.out.print("...["+i.next()+"] ");
    }
    System.out.println(" <=== <=== <=== (1)\n");

}


        Constructor tmpConstr=null;
        // create proxy constructors for existing public constructors
        Constructor constr[]=javaClassToProxy.getDeclaredConstructors();
if (bDebug==true) System.err.println("createConstructors: constr[].length="+constr.length);

        if (constr.length>0)
        {
            sb.append("\n\t// --- creating constructors from existing public and protected constructors using ["+javaClassToProxy.getName()+"] (if any) \n");
        }

        for (int i=0; i<constr.length; i++)
        {
            tmpConstr=constr[i];

if (bDebug==true) System.err.println("---> createConstructors(): tmpConstr=["+tmpConstr.toString()+"]");

            int mods=tmpConstr.getModifiers();
            if (!( Modifier.isPublic(mods) || Modifier.isProtected(mods)) )   // only create for public and protected constructors
            {
                continue;
            }

            Class paramTypes[]=tmpConstr.getParameterTypes();

if (bDebug==true) System.err.println("---> createConstructors(): paramTypes=["+paramTypes+"]...");

                // construct pseudo-signature (without throws-clause)
            String methSignature2=createListOfClassNames(paramTypes, METHOD_SIGNATURE); // createMethodSignatureParen(paramTypes);
            if (hs.contains(methSignature2))    // constructor already created?
            {
if (bDebug==true) System.err.println("---> createConstructors(): methSignature2=["+methSignature2+"] already created!");
                continue;
            }

                // return new String[] { throwsClauseOri, throwsClauseWithBSFException };
            String throwsClauses[]=createThrowsClause(tmpConstr.getExceptionTypes());

            String paramArg[]=createArgumentHead(paramTypes, true, bProxyConstructors);


if (bDebug==true) System.err.println("///  createConstructors(): methSignature2=["+methSignature2+"], paramArg[0]=["+paramArg[0]+"] now created!");

            sb.append("\n\tpublic "+newClzName+paramArg[0]+" "+
                                    (bProxyConstructors ? throwsClauses[1] : throwsClauses[0])+"     \n");

            sb.append("\t{\n");
            sb.append("\t\tsuper"+paramArg[1]+";                    \n");
            sb.append("\t\ttarget=a0;      \n");    // fetch and save RexxProxy argument

                // make sure defaultHandler gets set at least now that an instance gets created (may be
                // too late sometimes, hence programmer should make sure it got set before instantiating the class)
            sb.append("\t\tif (defaultHandler==null) \n");
            sb.append("\t\t{                         \n");
            sb.append("\t\t\tdefaultHandler=a0;      \n");
            sb.append("\t\t}                         \n");

            if (bProxyConstructors)
            {
                sb.append("\t\t"+paramArg[2]+"\n");     // define arguments as "Object[] obj"
                sb.append("\t\ttarget.invoke(this, \"<init>\", \""+tmpConstr.toString()+"\", obj);   \n");
            }

            sb.append("\t}\n\n");
            hs.add(methSignature2);     // memorize that we created this constructor
        }


if (bDebug==true)
{
    java.util.Iterator i=hs.iterator();

    while (i.hasNext()==true)
    {
        System.out.print("...["+i.next()+"] ");
    }
    System.out.println(" <=== <=== <=== (2)\n");

}
if (bDebug==true) System.err.println("createConstructors(), END. <--");

    }


    /** Controls the creation of the abstract interface methods.
     *
     * @param sb the StringBuffer to append the statements to
     * @param newClzName the new name of the new class
     * @param ifClz an interface class which needs to processed together with its super interface classes if any
     * @param ri RunInfos runtime infos for managing the process
     * @param tmpClz <code>null</code> (create all methods) or a class object which will be checked
     *                (create proxy method only, if no concrete declaredMethod() of it exists)
     *
     */
// TODO: make sure we proxy also default interface methods, if "*" was supplied!
    static void createInterfaceMethods(StringBuffer sb, String newClzName, Class ifClz, RunInfos ri, Class<?> tmpClz)    // rgf, 2017-02-09: create methods for the interface classes
    {
        createInterfaceMethodsWorker(sb, newClzName, ifClz, ri, tmpClz);

        for (Class tmpIfClz : ifClz.getInterfaces())  // iterate over super interfaces if any
        {
            createInterfaceMethods(sb, newClzName, tmpIfClz, ri, tmpClz);   // recurse
        }
    }

    static void createInterfaceMethodsWorker(StringBuffer sb, String newClzName, Class ifClz, RunInfos ri, Class <?> tmpClz)
    {
        sb.append("\n\t// --- creating methods for interface ["+ifClz.getName()+"]" +
                  (tmpClz==null? "" : " for ["+tmpClz.getName()+"]") +
                  ", if not yet created \n");

        // create abstract methods
        Method []declMethods=null;
        if (ri.clz2declMethods.containsKey(ifClz))     // declared methods for this class already available
        {
            declMethods=(Method []) ri.clz2declMethods.get(ifClz);
        }
        else    // get declared methods, cache them, in case we need them for processing the method list later
        {
            declMethods=ifClz.getDeclaredMethods();
            ri.clz2declMethods.put(ifClz, declMethods);// save declared methods
        }

        for (int i=0; i<declMethods.length; i++)        // create implementations for abstract methods, if necessary
        {
            Method m=declMethods[i];
            if (bDebug==true) System.err.println("createInterfaceMethods(): declMethods, i=["+i+"], m=["+m.toString()+"]");

            // rgf, 2018-02-19: if interface method is default or static (starting with Java 1.8/8), then ignore
            if (!Modifier.isAbstract(m.getModifiers())) // only process abstract interface methods
            {
                if (bDebug==true) System.err.println("createInterfaceMethods(): skipping concrete interface method, i=["+i+"], m=["+m.toString()+"]");
                continue;
            }

            // check method against the abstract class object's declaredMethods; is there a concrete
            // (non-abstract) one available already? If so, do not create a proxy method for it.
            try
            {
                Method dm=tmpClz.getDeclaredMethod(m.getName(), m.getParameterTypes());
                // declared method is not abstract?
                int mods=dm.getModifiers();
                if (Modifier.isAbstract(mods))      // make sure we implement proxy method only, if
                {
                    createMethod(sb, m, newClzName, ri);         // create method code
                }
            }
            catch (NoSuchMethodException nsme)
            {
                createMethod(sb, m, newClzName, ri);         // create method code
            }
        }
    }



    /** Controls which public methods get created on the fly. All abstract methods get implemented, optionally methods
     * that are supplied in the <code>methods2proxy</code> argument. As each method must have a
     * unique signature, hence no method with a unique signature is created more than once.
     *
     * @param sb the StringBuffer to append the statements to
     * @param newClzName the new name of the new class
     * @param javaClassToProxy the Java (abstract) class that gets extended
     * @param methods2proxy    String list of method names to create proxies for; a method name
     *                         may be prepended with the name of a superclass delimited with a blank
     * @param ri RunInfos runtime infos for managing the process
     *
     */
    static void createMethods(StringBuffer sb, String newClzName, Class javaClassToProxy,
                              String [] methods2proxy, RunInfos ri) throws BSFException
    {
// boolean bDebug=true;
if (bDebug==true) System.err.println("createMethods(), begin...");

        if (methods2proxy==null || methods2proxy.length==0)    // nothing to proxy supplied
        {
if (bDebug==true) System.err.println("///\tmethods2proxy " + (methods2proxy==null ? "is null" : "is empty") +": no methods get created, returning" );
            return;
        }

        sb.append("\n\t// --- creating methods according to String array of methods to proxy (if any) \n");

        for (int i=0; i<methods2proxy.length; i++)
        {
if (bDebug==true) System.err.println("///---> next, i=["+i+"]\ncreateMethods(), i=["+i+"] methods2proxy[i]=["+methods2proxy[i]+"]");
            if (methods2proxy[i]==null || methods2proxy[i].compareTo("")==0)    // ignore null and empty strings
            {
                continue;
            }


            String [] arrM2P=parseMethod2Proxy(methods2proxy[i]);

            if (arrM2P==null)   // wrong string content (empty or too many words)
            {
                throw new BSFException("ClassAdapter.createMethods(): erroneous value for the "+
                                      (i+1)+(i==0 ? "st" : (i==1 ? "nd" : (i==2 ? "rd" : "th") ) )+
                                       " (1-based) entry in the supplied String array of methods to proxy: ["+methods2proxy[i]+"].");
            }

            Class tmpClz=javaClassToProxy;  // default to class to be extended

            if (arrM2P[0]!=null)        // an explicit class to be used, find the class object and check whether it is a superclass
            {
if (bDebug==true) System.err.println("createMethods(), arrM2P[0]=["+arrM2P[0]+"], arrM2P[1]=["+arrM2P[1]+"], arrM2P[2]=["+arrM2P[2]+"], arrM2P[3]=["+arrM2P[3]+"], tmpClz=["+tmpClz.toString()+"]");
                tmpClz=(Class) ri.uCN2clz.get(arrM2P[1]);   // try to get class object
                if (tmpClz==null)       // not yet in map
                {
                    for (Class wrkClz=javaClassToProxy.getSuperclass(); wrkClz!=null; wrkClz=wrkClz.getSuperclass() )
                    {
if (bDebug==true) System.err.println("createMethods(), wrkClz=["+wrkClz+"]=["+wrkClz.toString()+"]");

                        String clzName=wrkClz.getName().toUpperCase();
                        if (ri.uCN2clz.containsKey(clzName))   // already handled
                        {
                            continue;
                        }
                        ri.uCN2clz.put(clzName, wrkClz);    // save class object
                        if (clzName.compareTo(arrM2P[1])==0)  // found !
                        {
                            tmpClz=wrkClz;
                            newClzName=arrM2P[0];
                            break;
                        }
                    }
                }

                if (tmpClz==null)   // class not found, resp. not a superclass !
                {
                    throw new BSFException("ClassAdapter.createMethods(): erroneous value (class not found or not a superclass) for the "+
                                          (i+1)+(i==0 ? "st" : (i==1 ? "nd" : (i==2 ? "rd" : "th") ) )+
                                           " (1-based) entry in the supplied String array of methods to proxy: ["+methods2proxy[i]+"].");

                }
            }

            if (arrM2P[3].compareTo("<INIT>")==0)   // proxy constructors ?
            {
if (bDebug==true) System.err.println("---> BINGO !    <INIT>   BINGO! <---, newClzName=["+newClzName+"], tmpClz=["+tmpClz+"]");
                createConstructors(sb, newClzName, tmpClz, true, ri);   // create and proxy them!
                continue;
            }

            // TODO: get all declared methods, analyze them and if matching, create proxied method
            //       abstract method with now forwardTo, else forwardToSuper

            Method []meths=(Method []) ri.clz2declMethods.get(tmpClz);
            if (meths==null)    // no declared methods cached so far
            {
                meths=tmpClz.getDeclaredMethods();
                ri.clz2declMethods.put(tmpClz, meths);  // cache declared methods
            }
                // find method with the same name (can be multiple methods due to case and/or signature)
            boolean bMethodFound=false;
            for (int k=0; k<meths.length; k++)
            {
                if (meths[k].isSynthetic())     // skip synthetic (bridge) methods
                {
                    continue;
                }

                String tmpName=meths[k].getName().toUpperCase();
                if (tmpName.compareTo(arrM2P[3])!=0)        // not found?
                {
                    continue;
                }
                int mMods=meths[k].getModifiers();

                // rgf, 20140830: allow to override protected methods too as a subclass is allowed to invoke it
                // if ((mMods & (Modifier.FINAL | Modifier.PRIVATE | Modifier.PROTECTED)) != 0)
                if ((mMods & (Modifier.FINAL | Modifier.PRIVATE )) != 0)
                {
                    continue;   // don't proxy
                }

                bMethodFound=true;

if (bDebug==true) System.err.println("createMethods(), k="+k+", about to call createMethod, methName ["+meths[k].getName()+"] BINGO!!!");

                createMethod(sb, meths[k], newClzName, ri);
            }

            if (bMethodFound==false)    // no method by the given name found !
            {
                throw new BSFException("ClassAdapter.createMethods(): no public nor protected method definition to override found for the "+
                                       (i+1)+(i==0 ? "st" : (i==1 ? "nd" : (i==2 ? "rd" : "th") ) )+
                                       " (1-based) entry in the supplied String array of methods to proxy: ["+methods2proxy[i]+"].");
            }

        }
    }




    /** Creates a public method proxy for the supplied Method object. If a method with the same signature (matching
     *  method name and parameter types) got created already, it will not be created again.
     *
     * @param sb the StringBuffer to append the statements to
     * @param m a Method object which needs to get proxied
     * @param newClzName the new name of the new class
     * @param ri RunInfos runtime infos for managing the process
     */
    static void createMethod (StringBuffer sb, Method m, String newClzName, RunInfos ri)
    {
if (bDebug==true) System.err.println("---> createMethod() | 1 - method: "+m);

        Class paramTypes[]=m.getParameterTypes();

            // construct pseudo-signature (without throws-clause)
        String methSignature2=createListOfClassNames(paramTypes, METHOD_SIGNATURE); //  createMethodSignatureParen(paramTypes);

        String methName   =m.getName();

        HashSet<String> hs=(HashSet<String>) ri.uMN2setOfSignatures.get(methName);
        if (hs==null)
        {
            hs=new HashSet<String>();
            ri.uMN2setOfSignatures.put(methName, hs);
        }
        else if (hs.contains(methSignature2))    // method with this signature already created?
        {

            if (bDebug==true) System.err.println("---> createMethod() | 2 - already created signature: "+methSignature2);
            return;
        }

        String strMethName=methName.toUpperCase();

        int     iModifiers=m.getModifiers();

        // create forwardToSuper-method only, if not an abstract method
        boolean bCreateForwardToSuper=!(Modifier.isAbstract(iModifiers));

        String  strModifiers="public ";
        boolean bIsStatic=Modifier.isStatic(iModifiers);

        if (bIsStatic)
        {
            strModifiers=strModifiers+"static ";
        }


        Class returnType=m.getReturnType();
        // String strReturnType=returnType.getName();
        String strReturnType=getEditedClassName(returnType);

        strModifiers=strModifiers+strReturnType;    // add return type

            // return new String[] { throwsClauseOri, throwsClauseWithBSFException };
        String throwsClauses[]=createThrowsClause(m.getExceptionTypes());

        String paramArg[]=createArgumentHead(paramTypes, false, true);
        String retStmts  =createReturnStatements(returnType);

        sb.append("\n\t"+strModifiers+" "+methName+paramArg[0]+' '+throwsClauses[1]+"\n");
        sb.append("\t{\n");
        sb.append("\t\t"+paramArg[2]+"\n");     // define obj=....

            // invoke RexxProxy
        sb.append("\t\tObject    res=null;     \n");

        String strThis="this";  // default to this object
        if (bIsStatic)
        {
            sb.append("\t\tRexxProxy rpTarget=defaultHandler;     \n");
            strThis=newClzName+".class";    // supply the class object
        }
        else
        {
            sb.append("\t\tRexxProxy rpTarget=(target==null ? defaultHandler : target);     \n");
        }

        sb.append("\t\t\tres=rpTarget.invoke("+strThis+", \""+methName+"\", \""+strModifiers+" "+methName+methSignature2+"\", obj); \n");

        sb.append(retStmts);
        sb.append("\t}\n\n");

            // forward to super
        if ( bCreateForwardToSuper )
        {

if (bDebug==true) System.err.println("---> createMethod() |     created 'forwardToSuper' method as well");
            sb.append("\n\t"+strModifiers+" "+methName+
                            "_forwardToSuper"+
                             paramArg[0]+' '+throwsClauses[0]+"\n");

            sb.append("\t{\n");

                // determine whether returning a value is necessary
            String strReturnStmt="return ";
            if (returnType==Void.class || returnType==void.class)
            {
                strReturnStmt="";
            }

                // determine target of method invocation
            String strTarget="super";
            if (bIsStatic)
            {
                strTarget=getEditedClassName(m.getDeclaringClass());

            }

            sb.append("\t\t"+strReturnStmt+
                             strTarget+"."+methName+
                             paramArg[1]+";    \n");   // forward to super

            sb.append("\t}\n\n");
        }

        hs.add(methSignature2);     // memorize that we created a method with this signature
    }



    /** Parses a string from Rexx that consists of "[Java_Class_Name ]method_name".
     *
     * @param val a String value which denotes an optional Java class name, followed by a blank,
     *            followed by a mandatory method name. All methods with the same name in the given
     *            class will get proxied. If no class name is given, the method name relates to the
     *            extended Java class.
     * @return    <code>null</code>, if supplied argument is null or empty, else a
     *            String array, containing
     *            <dl>
     *            <dt>index <code>0</code>
     *            <dd>the mixed case Java class name or null, if missing
     *
     *            <dt>index <code>1</code>
     *            <dd>the upper case Java class name or null, if missing
     *
     *            <dt>index <code>2</code>
     *            <dd>the mixed case method name
     *
     *            <dt>index <code>3</code>
     *            <dd>the upper case Java method name
     *
     *            </dl>
     */
    static String [] parseMethod2Proxy (String val)
    {
        StringBuffer sb[]={new StringBuffer(), new StringBuffer()};

        int jIdx=-1,
            len =val.length();

        for (int i=0; i<len; i++)
        {
            char c=val.charAt(i);       // get char

            if (c==' ' || c=='\t')      // skip white-space
            {
                continue;
            }

            // a word char found!
            jIdx++;
            if (jIdx>1)         // more than two words in string, an error, indicate error condition!
            {
                return null;
            }

            sb[jIdx].append(c);
            i++;
            for (; i<len; i++)
            {
                c=val.charAt(i);   // get char

                if (c==' ' || c=='\t')  // break, if white-space
                {
                    break;
                }
                sb[jIdx].append(c);     // add char
            }
        }

        if (jIdx<0)         // no chars found
        {
            return null;
        }

        if (jIdx==0)        // only one word found
        {

            String s=sb[0].toString();
            return new String[] { null, null,           // no class name
                                  s, s.toUpperCase()    // method name
                                };
        }

        String s0=sb[0].toString(),
               s1=sb[1].toString();

        return new String[] { s0, s0.toUpperCase(),     // class name
                              s1, s1.toUpperCase()      // method name
                            };

    }


    /** Creates an argument list as a String from the supplied array of Classes representing the types.
     *  @param paramTypes array of Class objects representing the types
     *  @param bCreateForConstructor first String should inject as first argument the RexxProxy to forward to
     *  @param bCreateInvokeArg determines whether the invocations of constructors get forwarded to the RexxProxy
     *
     * @return a String array, where index <code>0</code> represents the parenthized argument list,
     *         index <code>1</code> represents the parenthized argument list without the RexxProxy argument,
     *         index <code>2</code> is either null or defines an array of type Object to allow the received
     *         arguments to be forwarded to the rexxProxy
     *
     */
    static String [] createArgumentHead (Class [] paramTypes, boolean bCreateForConstructor, boolean bCreateInvokeArg)
    {
        StringBuffer sb1=new StringBuffer(),    // first arg is RexxProxy, args have type information
                     sb2=new StringBuffer(),    // first arg is second arg (dropped RexxProxy)
                     sb3=null;                  // first arg is second arg, cast primitives to their wrapper type

        sb1.append("(");

        if (bCreateForConstructor)   // for constructors
        {
            sb1.append(STR_REXX_PROXY).append(" a0");
            if (paramTypes.length>0)
            {
                sb1.append(", ");
            }
        }

        sb2.append("(");

        if (bCreateInvokeArg)   // arguments for forwarding to RexxProxy
        {
            sb3=new StringBuffer();
            sb3.append("Object []obj=new Object[] { ");
        }

        for (int i=0; i<paramTypes.length; i++)
        {
            int argNr=i+1;                  // number to use for argument
            Class tmpClz=paramTypes[i];
            String tmpArg=getEditedClassName(tmpClz)+" "+"a"+argNr;

            if (i>0)
            {
                sb1.append(", ");
                sb2.append(", ");

                if (bCreateInvokeArg)
                {
                    sb3.append(", ");
                }
            }
            sb1.append(tmpArg);
            sb2.append("a"+argNr);

            if (bCreateInvokeArg)
            {
                String tmpStr=null;
                if (tmpClz.isPrimitive())   // wrap primitive types
                {
                    // as code should run on Java 1.4, do not rely on existence of "valueOf()"
                        if (tmpClz==boolean.class) { sb3.append("Boolean.valueOf(a"+argNr+")"); }
                   else if (tmpClz==byte.class   ) { sb3.append("new Byte(a"+argNr+")"       ); }
                   else if (tmpClz==char.class   ) { sb3.append("new Character(a"+argNr+")"  ); }
                   else if (tmpClz==short.class  ) { sb3.append("new Short(a"+argNr+")"      ); }
                   else if (tmpClz==int.class    ) { sb3.append("new Integer(a"+argNr+")"    ); }
                   else if (tmpClz==float.class  ) { sb3.append("new Float(a"+argNr+")"      ); }
                   else if (tmpClz==double.class ) { sb3.append("new Double(a"+argNr+")"     ); }
                   else if (tmpClz==long.class   ) { sb3.append("new Long(a"+argNr+")"       ); }
                }
                else    // just forward argument
                {
                    sb3.append("a"+argNr);
                }
            }
        }

        sb1.append(")");
        sb2.append(")");
        return new String [] {sb1.toString(), sb2.toString(), (sb3==null ? null : sb3.append(" };").toString())};
    }




    /** Creates the statements for returning the result of invoking the RexxProxy, possibly
     *  cast to primitive types. This method assumes that the result of invoking a method on
     *  a RexxProxy will be stored in a local variable named &quot;res&quot;.
     *
     * @param returnType the class object of the return type
     * @return returns a string representing the necessary statements to return the return value with the proper type
    */
    static String createReturnStatements(Class returnType)
    {
            // nothing to return?
        if (returnType==Void.class || returnType==void.class || returnType==null)
        {
            return "";
        }


        StringBuffer sb=new StringBuffer();
        // String returnTypeName=returnType.getName();
        String returnTypeName=getEditedClassName(returnType);

        if (!returnType.isPrimitive())  // return value cast to return type
        {
            if (returnType == Boolean.class   ||
                returnType == Byte.class      ||
                returnType == Character.class ||
                returnType == Short.class     ||
                returnType == Integer.class   ||
                returnType == Long.class      ||
                returnType == Float.class     ||
                returnType == Double.class    )
            {
                sb.append("\t\tif (res instanceof String)    \n")
                  .append("\t\t{ \n");

                if (returnType==Boolean.class)
                {
                    sb.append("\t\t\tif ( ((String) res).compareTo(\"1\")==0 ) \n");
                    sb.append("\t\t\t{                   \n");
                    sb.append("\t\t\t\tres=Boolean.TRUE; \n");
                    sb.append("\t\t\t}                   \n");
                    sb.append("\t\t\telse                \n");
                    sb.append("\t\t\t{                   \n");
                    sb.append("\t\t\t\tres= new "+returnTypeName+"((String) res); \n");
                    sb.append("\t\t\t}                   \n");
                }
                else if (returnType==Character.class)
                {
                    sb.append("\t\t\tres=((String) res).charAt(0); \n");
                }
                else
                {
                    sb.append("\t\t\tres= new "+returnTypeName+"((String) res); \n");
                }
                sb.append("\t\t} \n");
            }
            return sb.append("\t\treturn ("+returnTypeName+") res; \n").toString();
        }

        // TODO: primitive types
        String objTypeName=null;

             if (returnType==boolean.class) { objTypeName="Boolean";   }
        else if (returnType==byte.class   ) { objTypeName="Byte";      }
        else if (returnType==char.class   ) { objTypeName="Character"; }
        else if (returnType==short.class  ) { objTypeName="Short";     }
        else if (returnType==int.class    ) { objTypeName="Integer";   }
        else if (returnType==float.class  ) { objTypeName="Float";     }
        else if (returnType==double.class ) { objTypeName="Double";    }
        else if (returnType==long.class   ) { objTypeName="Long";      }


        sb.append("\t\tif (res==null)                   \n")
          .append("\t\t{ \n")
          .append("\t\t\tthrow new ClassCastException(\"Result returned from RexxProxy '\"+rpTarget+\"' is 'null', ")
          .append(      "which cannot be cast to the primitive return type '")
          .append(      returnTypeName+"'.\");    \n")
          .append("\t\t} \n");


        sb.append("\t\tif (res instanceof String)    \n")
          .append("\t\t{ \n");

        if (returnType==boolean.class)
        {
            sb.append("\t\t\tif ( ((String) res).compareTo(\"1\")==0 ) \n");
            sb.append("\t\t\t{                   \n");
            sb.append("\t\t\t\tres=Boolean.TRUE; \n");
            sb.append("\t\t\t}                   \n");
            sb.append("\t\t\telse                \n");
            sb.append("\t\t\t{                   \n");
            sb.append("\t\t\t\tres= new "+objTypeName+"((String) res); \n");
            sb.append("\t\t\t}                   \n");
        }
        else if (returnType==char.class)
        {
            sb.append("\t\t\tres=((String) res).charAt(0); \n");
        }
        else
        {
            sb.append("\t\t\tres= new "+objTypeName+"((String) res); \n");
        }
        sb.append("\t\t} \n");

        return sb.append("\t\treturn (("+objTypeName+") res)."+returnTypeName+"Value(); \n").toString();
    }



    /** Create comma-separated list of parameter types, enclosed in round parenthesis.
     * @param paramTypes array of Class objects
     * @param kind
    */
    // static String createMethodSignatureParen(Class [] paramTypes, boolean bMethodSignature)
    static String createListOfClassNames(Class [] paramTypes, int kind)
    {
        if (paramTypes==null || paramTypes.length==0)
        {
            if (kind==METHOD_SIGNATURE)
            {
                return "()";
            }
            else if (kind==THROWS_CLAUSE)
            {
                return null;
            }
            return "";
        }

        StringBuffer sb=new StringBuffer();

        if (kind==METHOD_SIGNATURE)
        {
            sb.append("(");
        }
        else if (kind==THROWS_CLAUSE)
        {
            sb.append("throws ");
        }

        for (int i=0; i<paramTypes.length; i++)
        {
            if (i>0)
            {
                sb.append(",");
            }
            sb.append(getEditedClassName(paramTypes[i]));
        }

        if (kind==METHOD_SIGNATURE)
        {
            sb.append(")");
        }

        return sb.toString();
    }




    /** Create  comma-separated list of parameter types, enclosed in round parenthesis.
     *
     * @param excTypes array of Class objects representing the exceptions that may be thrown
     * @return a String array with two elements, the first entry representing the original list of
     *         exceptions, the second entry extends this list by the <code>BSFException</cod>
     *
    */
    static String[] createThrowsClause(Class []excTypes)
    {
        // Class []excTypes=tmpConstr.getExceptionTypes();
        String throwsClauseOri=createListOfClassNames(excTypes, THROWS_CLAUSE);
        String throwsClause=throwsClauseOri;

        if (throwsClauseOri==null)
        {
            throwsClauseOri="";
        }

        if (throwsClause==null)
        {
            throwsClause="throws BSFException";
        }
        else
        {
                // BSFException already listed? If not, append it.
            boolean bFound=false;
            for (int t2=0; t2<excTypes.length && bFound==false; t2++)
            {
                bFound=(excTypes[t2]==RexxProxy.class);
            }

            if (bFound==false)      // not found, hence add it!
            {
                throwsClause=throwsClause+",BSFException";
            }
        }

        return new String[] { throwsClauseOri, throwsClause };
    }


    /** Creates and returns the name of the supplied class, taking array classes into consideration.
     * @param clz the class to create a name for
     * @return the class name
     */
    static String getEditedClassName(Class clz)
    {
        if (!clz.isArray())
        {
            return clz.getName();
        }

            // an array class in hand, get base component type, count dimensionality
        Class compType=clz;
        int i=0;
        for ( ; compType.isArray(); compType=compType.getComponentType())
        {
            i++;
        }

        StringBuffer sb=new StringBuffer();
        sb.append(compType.getName());      // get class name of component type
        for (int k=0; k<i; k++)
        {
            sb.append("[]");        // append
        }
        return sb.toString();
    }





    /** Inner class to make maintenance and access to management data structures easy.
    *
    *     uCN       ... uppercased class name
    *     uMN       ... uppercased method name
    *     clz       ... class object
    *
    */
    static class RunInfos
    {
        HashMap<String,HashSet<String>>   uMN2setOfSignatures=new HashMap<String,HashSet<String>>();    // maps uppercased method name
        HashMap<Class<?>,Method[]>        clz2declMethods    =new HashMap<Class<?>,Method[]>();    // maps class object to declared methods
        HashMap<String,Class<?>>          uCN2clz            =new HashMap<String,Class<?>>();    // maps uppercase class name to class object
    }

}
