package org.rexxla.bsf.engines.rexx;

/* Test command lines:

java ProxiedJavaClassTool java.io.File Aloha getAbsoluteFile isFile deleteOnExit listFiles compareTo createTempFile
java ProxiedJavaClassTool javax.swing.filechooser.FileFilter Aloha append oha nixiNAXI accept

java ProxiedJavaClassTool java.awt.Frame Brumm getState getTitle setTitle "<init>" "java.awt.Container paint" "java.awt.Container <init>" "nixi.noxi.org oho"


java ProxiedJavaClassTool TestAbstract Caesar

*/


import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
 *    Copyright (C) 2010 Rony G. Flatscher
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
  * @version 0.5.0, 2010-03-27, 2010-04-14, 2010-04-17
  * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu.ac.at">http://www.wu.ac.at</a>)
  */

public class ProxiedJavaClassTool
{

    private final static String STR_REXX_PROXY="org.rexxla.bsf.engines.rexx.RexxProxy";
        // flags
    private final static int    METHOD_SIGNATURE=1;
    private final static int    THROWS_CLAUSE   =2;

    // private final static boolean bDebug =false; // true;
    // private final static boolean bTiming=false; // true;
    public static boolean bDebug =false; // true;
    public static boolean bTiming=false; // true;

        // using Janino 1.5.2 and Java 6: this must be false, maybe later versions of Janino allow for that
    public  static boolean bDoNotProxySyntheticMethods=false;


// depending on the compiler in use and the target Java version, problems may occur with Sun's Java compiler handling generics, cf. e.g. java.io.File and compareTo(Object) up to and including Java 1.4 vs. Java 1.5 and up
    // try to get the isSynthetic method object, if available (starting with Java 1.5)
    private static Method IS_SYNTHETIC_METHOD=null;
    static {
        try
        {
            IS_SYNTHETIC_METHOD = Method.class.getDeclaredMethod("isSynthetic", new Class[] {} );
        }
        catch (Exception e)
        { }
    }



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
                javaClassToProxy=ProxiedJavaClassTool.class.getClassLoader().loadClass(args[0]);
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
                createProxiedJavaClass( javaClassToProxy, newClzName, methodNames2proxy);
            }
            catch (Exception e)
            {
                System.err.println(e+"\n---");
            }
        }
    }


    /* Do not allow instances of this class. */
    private ProxiedJavaClassTool ()
    {}

    /** Creates and returns a proxy class in which Java method invocations get forwarded to
     *  a RexxProxy.
     *
     *  @param  javaClassToProxy Class object to use for proxying
     *  @param  newClzName <code>null</code> or name for proxy class to create
     *  @param  methodNames2proxy <code>null</code> or array of Strings denoting the names of methods to proxy
     */
    public static Class createProxiedJavaClass(Class    javaClassToProxy,
                                               String   newClzName,
                                               String[] methodNames2proxy)
                                        throws BSFException
    {
        long crtSrcStartTime=0l, crtSrcEndTime=0l;
        if (bTiming==true) crtSrcStartTime=System.currentTimeMillis();

            // process arguments
        if (javaClassToProxy==null)     // do not accept null !
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "ProxiedJavaClassTool.createProxiedJavaClass(...): argument \"javaClassToProxy\" must not be null");
        }

        if (javaClassToProxy.isArray())     // do not accept Array classes
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "ProxiedJavaClassTool.createProxiedJavaClass(...): argument \"javaClassToProxy\" must not be an array class");
        }

        int classModifiers=javaClassToProxy.getModifiers();

        if (!Modifier.isPublic(classModifiers))
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "ProxiedJavaClassTool.createProxiedJavaClass(...): argument \"javaClassToProxy\", value=["+javaClassToProxy+"]: is not public");
        }

        if (Modifier.isInterface(classModifiers))
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "ProxiedJavaClassTool.createProxiedJavaClass(...): argument \"javaClassToProxy\", value=["+javaClassToProxy+"]: must not be an interface");
        }

        if (Modifier.isFinal(classModifiers))  // only accept non-final classes
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "ProxiedJavaClassTool.createProxiedJavaClass(...): argument \"javaClassToProxy\", value=["+javaClassToProxy+"]: must not be final");
        }

        if (newClzName==null)   // no name for the proxied Java class given?
        {
            newClzName=javaClassToProxy.getName()+"_$RexxExtendClass$_"+(new Object()).hashCode();
        }

            // if no method names to proxy given, then indicate by using an empty String array instead
        String [] methods2proxy= (methodNames2proxy==null ? new String[]{} : methodNames2proxy);

        boolean isAbstractClz=Modifier.isAbstract(classModifiers);

            // create Java program, use a StringBuffer
        StringBuffer sb=new StringBuffer();

        RunInfos ri=new RunInfos();
            // save mapping of class name to its class object for later use
        ri.uCN2clz.put(javaClassToProxy.getName().toUpperCase(), javaClassToProxy);


// ---------------------------------------------------------------------------
            // extract simple class name
        int lpos=newClzName.lastIndexOf('.');
        String packageName="org.rexxla.bsf.engines.rexx.onTheFly";
        if (lpos>=0)
        {
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

        createProlog(sb, newClzName, javaClassToProxy, ri); // create prolog code

        createMethods(sb, newClzName,  javaClassToProxy, methods2proxy, ri);

        // if constructors were not created by demand of user, do not proxy them to the RexxProxy
        createConstructorsBase(sb, newClzName,  javaClassToProxy, false, ri); // create base constructors

            // create proxies for public constructors; if abstract class in hand, check whether superclass
            // is abstract as well and if so, proxy its public constructors
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

                if ( Modifier.isAbstract(m.getModifiers())) // an abstract method in hand ?
                {
                    createMethod(sb, m, newClzName, ri);         // create method code, do not create forwardToSuper()
                }
            }

            tmpClz=tmpClz.getSuperclass();
            if (tmpClz!=null && !Modifier.isAbstract(tmpClz.getModifiers()) )
            {
                break;
            }
        } while ( tmpClz!=null );



        createEpilog(sb);                               // create epilog code (supply closing curly bracket)

        if (bTiming==true) crtSrcEndTime=System.currentTimeMillis();
        if (bDebug) System.err.println("/// ProxiedJavaClassTool.createProxiedJavaClass(...), created Java source code: \n"+sb);

        SimpleCompiler sc=new SimpleCompiler();
        Class clz=null;

        long startTime=0l, endTime=0l, endOfLoadTime=0l;

        try {
            if (bTiming==true)  startTime=System.currentTimeMillis();

            sc.cook(sb.toString());                         // compile program
            if (bTiming==true) endTime=System.currentTimeMillis();

            clz=sc.getClassLoader().loadClass(fullNewClzName);

            if (bTiming==true) endOfLoadTime=System.currentTimeMillis();
        }
        catch (Exception e) // TODO: do we need to catch at all ? And if so, let's make a BSFException out of it!
        {
            e.printStackTrace();
            throw new BSFException (BSFException.REASON_OTHER_ERROR, e.toString(), e);
        }

        if (bTiming==true)
        {
            System.out.println("clz=["+clz+"]");
            System.out.println("creating source  (in milliseconds): "+(crtSrcEndTime-crtSrcStartTime));

            System.out.println("compilation time (in milliseconds): "+(endTime-startTime));
            System.out.println("load time        (in milliseconds): "+(endOfLoadTime-endTime));
        }

        return clz;
    }


    // extract package name (part of new or proxied class name), return newClzName as simple name
    static String createPackageStatement (StringBuffer sb, String newClzName, Class javaClassToProxy)
    {
            // extract package name
        String packageName=null;
        int lpos=newClzName.lastIndexOf('.');
        if (lpos<0)     // no package name supplied, use the proxied class one
        {
            String tmpClzName=javaClassToProxy.getName();
            lpos=tmpClzName.lastIndexOf('.');
            if (lpos>=0)    // extract package name
            {
                packageName=tmpClzName.substring(0,lpos);
            }
        }
        else
        {
            packageName=newClzName.substring(0,lpos);
            newClzName=newClzName.substring(lpos+1);
        }
        if (packageName!=null)
        {
            sb.append("package ").append(packageName).append("; \n");
        }
        return newClzName;
    }


    // create import statements, class definition, fields and setter/getter for default RexxProxy handler
    static void createProlog (StringBuffer sb, String newClzName, Class javaClassToProxy, RunInfos ri)
                throws BSFException
    {
        sb.append("import org.rexxla.bsf.engines.rexx.RexxProxy; \n");
        sb.append("import org.rexxla.bsf.engines.rexx.RexxException; \n");

        sb.append("\npublic class ");
        sb.append( newClzName );
        sb.append(" extends ").append(javaClassToProxy.getName()).append("\n{\n");

        sb.append("\tpublic  static          Object    classLock     =new Object(); \n");
        sb.append("\tprivate static volatile RexxProxy defaultHandler=null;         \n");
        sb.append("\tprivate                 RexxProxy target        =null;         \n\n");


            // define getDefaultHandler()
        sb.append("\n\tpublic static RexxProxy getDefaultHandler() \n");
        sb.append("\t{\n");
        sb.append("\t\treturn defaultHandler; \n");
        sb.append("\t}\n\n");

        HashSet hs=new HashSet();   // memorize that we created a method with this signature
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

        hs=new HashSet();           // memorize that we created a method with this signature
        hs.add("(org.rexxla.bsf.engines.rexx.RexxProxy)");
        ri.uMN2setOfSignatures.put("setDefaultHandler", hs);


            // define getTargetRexxProxy()
        sb.append("\n\tpublic synchronized RexxProxy getTargetRexxProxy() \n");
        sb.append("\t{\n");
        sb.append("\t\treturn target; \n");
        sb.append("\t}\n\n");

        hs=new HashSet();           // memorize that we created a method with this signature
        hs.add("()");
        ri.uMN2setOfSignatures.put("getTargetRexxProxy", hs);


            // define setTargetRexxProxy()
        sb.append("\n\tpublic synchronized void setTargetRexxProxy(RexxProxy newTarget) \n");
        sb.append("\t{\n");
        sb.append("\t\ttarget=newTarget; \n");
        sb.append("\t}\n\n");

        hs=new HashSet();           // memorize that we created a method with this signature
        hs.add("(org.rexxla.bsf.engines.rexx.RexxProxy)");
        ri.uMN2setOfSignatures.put("setTargetRexxProxy", hs);
    }



    static void createEpilog(StringBuffer sb)
    {
        sb.append("}\n");   // close class block
    }


    static void createConstructorsBase(StringBuffer sb, String newClzName, Class javaClassToProxy,
                              boolean bProxyConstructors, RunInfos ri)
    {
if (bDebug==true) System.err.println("createConstructorsBase(), begin...");

        String strMethName="<INIT>";
        HashSet hs=(HashSet) ri.uMN2setOfSignatures.get(strMethName);
        if (hs==null)
        {
            hs=new HashSet();
            ri.uMN2setOfSignatures.put(strMethName, hs);
        }

            // determine whether super's default constructor exists (if so, it needs to be invoked)
        boolean bDefaultConstructorExistsInSuper=false;
        try
        {
            Constructor tmpConstr=javaClassToProxy.getDeclaredConstructor( new Class[] {} );
            // if constructor is private, cannot reach it, behave as if not existent
            bDefaultConstructorExistsInSuper = Modifier.isPublic(tmpConstr.getModifiers());
        }
        catch (Exception e)
        { }



        String methSignature="public "+javaClassToProxy.getName()+"()";
        String methSignature2="()";   // for internal use

            // this constructor exists in super and was not created yet: create it!
        if (bDefaultConstructorExistsInSuper && (!hs.contains(methSignature2)))
        {
            sb.append("\n\t// --- creating base (default) constructor \n");

                // define default constructor
            sb.append("\n\tpublic "+newClzName+"()     \n");
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

            hs.add(methSignature2);     // memorize this
        }


        // --------------------------------------------------
            // define constructor with a supplied RexxProxy
        methSignature2="("+STR_REXX_PROXY+")";  // for internal use
        if (hs.contains(methSignature2))   // this constructor got already created, hence return
        {
            return;
        }

        Constructor tmpConstr=null;
        String throwsClause="";
        boolean bSingleRexxProxyConstructor_ExistsInSuper=true; // assume it exists in superclass
        try
        {
            tmpConstr=javaClassToProxy.getDeclaredConstructor( new Class[] {RexxProxy.class} );
            // if constructor is private, cannot reach it, behave as if not existent
            if (Modifier.isPrivate(tmpConstr.getModifiers()))
            {
                tmpConstr=null;
            }
            else
            {
                throwsClause=createListOfClassNames(tmpConstr.getExceptionTypes(), THROWS_CLAUSE);
                if (throwsClause==null)
                {
                    throwsClause="";
                }
            }
        }
        catch (NoSuchMethodException nsme)  // if it does not exist yet, we must not invoke it
        {
            bSingleRexxProxyConstructor_ExistsInSuper=false;
        }

        sb.append("\n\tpublic "+newClzName+"(RexxProxy rexxproxy) "+throwsClause+" \n");
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


    static void createConstructors(StringBuffer sb, String newClzName, Class javaClassToProxy,
                              boolean bProxyConstructors, RunInfos ri)
    {
if (bDebug==true) System.err.println("createConstructors(), begin...");
            // fetch set of methods created
        HashSet hs=(HashSet) ri.uMN2setOfSignatures.get("<INIT>");
        if (hs==null)
        {
            hs=new HashSet();
            ri.uMN2setOfSignatures.put("<INIT>", hs);
        }

        Constructor tmpConstr=null;
        // create proxy constructors for existing public constructors
        Constructor constr[]=javaClassToProxy.getDeclaredConstructors();
if (bDebug==true) System.err.println("createConstructors: constr[].length="+constr.length);

        if (constr.length>0)
        {
            sb.append("\n\t// --- creating constructors from existing public constructors using ["+javaClassToProxy.getName()+"] \n");
        }

        for (int i=0; i<constr.length; i++)
        {
            tmpConstr=constr[i];

            if (!Modifier.isPublic(tmpConstr.getModifiers()))   // only create public constructors
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

            String paramArg[]=createArgumentHead(paramTypes, true, bProxyConstructors);
            String throwsClause=createListOfClassNames(tmpConstr.getExceptionTypes(), THROWS_CLAUSE);
            if (throwsClause==null)
            {
                throwsClause="";
            }

if (bDebug==true) System.err.println("///  createConstructors(): methSignature2=["+methSignature2+"], paramArg[0]=["+paramArg[0]+"] now created!");
            sb.append("\n\tpublic "+newClzName+paramArg[0]+" "+throwsClause+"     \n");
            sb.append("\t{\n");
            sb.append("\t\tsuper"+paramArg[1]+";                    \n");
            sb.append("\t\ttarget=a0;      \n");    // fetch and save RexxProxy argument
            if (bProxyConstructors)
            {
                // sb.append("\t\ttarget.invoke(this, \"<init>\", null, null);   \n");
                sb.append("\t\t"+paramArg[2]+"\n");

                // sb.append("\t\ttarget.invoke(this, \"<init>\", \""+tmpConstr.toString()+"\", obj);   \n");


                sb.append("\t\ttry { \n");
                // sb.append("\t\t\tres=rpTarget.invoke("+strThis+", \""+methName+"\", \""+strModifiers+" "+methName+methSignature2+"\", obj); \n");
                sb.append("\t\ttarget.invoke(this, \"<init>\", \""+tmpConstr.toString()+"\", obj);   \n");

                sb.append("\t\t} \n");
                sb.append("\t\tcatch (Throwable t) { throw new RuntimeException(\"Throwable caused by using 'RexxProxy.invoke(...)' with '\"+target+\"'\", t); } \n\n");

            }

            sb.append("\t}\n\n");
            hs.add(methSignature2);     // memorize that we created this constructor
        }

if (bDebug==true) System.err.println("createConstructors(), END. <--");
    }



    /** Creates public methods on the fly: if for an abstract class, then ignores any method names,
     *  but resolves superclasses until they are not abstract anymore.
     *
     *  Each method must have a unique signature, hence no method with a unique signature is created
     *  more than once.
     *
     * @param sb StringBuffer object to add Java code to
     * @param javaClassToProxy class object to work on
     * @param methods2proxy    String list of method names to create proxies for; a method name
     *                         may be prepended with the name of a superclass delimited with a blank
     *
     */
    static void createMethods(StringBuffer sb, String newClzName, Class javaClassToProxy,
                              String [] methods2proxy, RunInfos ri) throws BSFException
    {
if (bDebug==true) System.err.println("createMethods(), begin...");

        sb.append("\n\t// --- creating methods according to String array of methods to proxy (if any) \n");

        for (int i=0; i<methods2proxy.length; i++)
        {
if (bDebug==true) System.err.println("createMethods(), i=["+i+"] methods2proxy[i]=["+methods2proxy[i]+"]");
            if (methods2proxy[i]==null || methods2proxy[i].compareTo("")==0)    // ignore null and empty strings
            {
                continue;
            }


            String [] arrM2P=parseMethod2Proxy(methods2proxy[i]);

            if (arrM2P==null)   // wrong string content (empty or too many words)
            {
                throw new BSFException("ProxiedJavaClassTool.createMethods(): erroneous value for the "+
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
                    throw new BSFException("ProxiedJavaClassTool.createMethods(): erroneous value (class not found or not a superclass) for the "+
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
if (bDebug==true) System.err.println("createMethods(), k=["+k+"] meths[k]=["+meths[k]+"]; bDoNotProxySyntheticMethods=["+bDoNotProxySyntheticMethods+"]");

                // if running on Java 1.5 or higher, skip synthetic (bridged) methods?
                if (bDoNotProxySyntheticMethods==true && IS_SYNTHETIC_METHOD!=null)
                {
                    try {
                        Object obj=IS_SYNTHETIC_METHOD.invoke(meths[k], new Object []{} );

if (bDebug==true) System.err.println("!!! IS_SYNTHETIC_METHOD.invoke("+meths[k]+") --> "+obj);
                        if ( ((Boolean) obj).booleanValue()==true)
                        {
                            continue;
                        }
                    }
                    catch (Exception e)
                    { }
                }


                String tmpName=meths[k].getName().toUpperCase();
                if (tmpName.compareTo(arrM2P[3])!=0)        // not found?
                {
                    continue;
                }
                int mMods=meths[k].getModifiers();

                if ((mMods & (Modifier.FINAL | Modifier.PRIVATE | Modifier.PROTECTED)) != 0)
                {
                    continue;   // don't proxy
                }

                bMethodFound=true;
                createMethod(sb, meths[k], newClzName, ri);
            }

            if (bMethodFound==false)    // no method by the given name found !
            {
                throw new BSFException("ProxiedJavaClassTool.createMethods(): no public method definition found for the "+
                                       (i+1)+(i==0 ? "st" : (i==1 ? "nd" : (i==2 ? "rd" : "th") ) )+
                                       " (1-based) entry in the supplied String array of methods to proxy: ["+methods2proxy[i]+"].");
            }

        }
    }

    static void createMethod (StringBuffer sb, Method m, String newClzName, RunInfos ri)
    {
if (bDebug==true) System.err.println("---> createMethod() | 1 - method: "+m);

        int   iModifiers=m.getModifiers();
        boolean bCreateForwardToSuper=!(Modifier.isAbstract(iModifiers));

        String strModifiers="public ";
        boolean bIsStatic=Modifier.isStatic(iModifiers);

        if (bIsStatic)
        {
            strModifiers=strModifiers+"static ";
        }

        Class paramTypes[]=m.getParameterTypes();

            // construct pseudo-signature (without throws-clause)
        String methSignature2=createListOfClassNames(paramTypes, METHOD_SIGNATURE); //  createMethodSignatureParen(paramTypes);

        String methName   =m.getName();
        String strMethName=methName.toUpperCase();

        // HashSet hs=(HashSet) ri.uMN2setOfSignatures.get(strMethName);
        HashSet hs=(HashSet) ri.uMN2setOfSignatures.get(methName);
        if (hs==null)
        {
            hs=new HashSet();
//            ri.uMN2setOfSignatures.put(strMethName, hs);
            ri.uMN2setOfSignatures.put(methName, hs);
        }
        else if (hs.contains(methSignature2))    // method with this signature already created?
        {

if (bDebug==true) System.err.println("---> createMethod() | 2 - already created signature: "+methSignature2);
            return;
        }

        Class returnType=m.getReturnType();
        // String strReturnType=returnType.getName();
        String strReturnType=getEditedClassName(returnType);

        strModifiers=strModifiers+strReturnType;    // add return type

        Class []excTypes=m.getExceptionTypes();
        String throwsClause=createListOfClassNames(excTypes, THROWS_CLAUSE);
        if (throwsClause==null)
        {
            throwsClause="";
        }
/* --->
        {
            throwsClause="throws RexxException";
        }
        else
        {
                // need for appending the RexxException ?
            boolean bFound=false;
            for (int t2=0; t2<excTypes.length && bFound==false; t2++)
            {
                bFound=(excTypes[t2]==RexxProxy.class);
            }

            if (bFound==false)      // not found, hence add it!
            {
                throwsClause=throwsClause+",RexxException";
            }
        }
<--- */


        String paramArg[]=createArgumentHead(paramTypes, false, true);
        String retStmts  =createReturnStatements(returnType);


        // sb.append("\n\tpublic "+strReturnType+" "+methName+paramArg[0]+" throws org.apache.bsf.BSFException \n");
        // sb.append("\n\tpublic "+strReturnType+" "+methName+paramArg[0]+' '+throwsClause+"\n");
        sb.append("\n\t"+strModifiers+" "+methName+paramArg[0]+' '+throwsClause+"\n");
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

        sb.append("\t\ttry { \n");
        sb.append("\t\t\tres=rpTarget.invoke("+strThis+", \""+methName+"\", \""+strModifiers+" "+methName+methSignature2+"\", obj); \n");
        sb.append("\t\t} \n");
        sb.append("\t\tcatch (Throwable t) { throw new RuntimeException(\"Throwable caused by using 'RexxProxy.invoke(...)' with '\"+rpTarget+\"'\", t); } \n\n");

/* --->
        sb.append("\t\t\tres=rpTarget.invoke("+strThis+", \""+methName+"\", \""+strModifiers+" "+methName+methSignature2+"\", obj); \n");
<--- */


        sb.append(retStmts);
        sb.append("\t}\n\n");

            // forward to super
        if ( bCreateForwardToSuper )
        {

// TODO: what about "forwardToSuper[Static]Strict" method, where first argument gives the exact case of the method name?
//       Maybe an overkill, but the ASM implementation does it; in reality this is not necessary, if using
//       bsf.invokeStrict(), if indeed there are different cased method names with the same signature

if (bDebug==true) System.err.println("---> createMethod() |     created 'forwardToSuper' method as well");
            // sb.append("\n\tpublic "+strReturnType+" "+methName+"_forwardToSuper"+paramArg[0]+" throws org.apache.bsf.BSFException \n");
            // sb.append("\n\tpublic "+strReturnType+" "+methName+"_forwardToSuper"+paramArg[0]+' '+throwsClause+"\n");
            sb.append("\n\t"+strModifiers+" "+methName+
//                             "forwardToSuper"+(bIsStatic==true ? "Static" : "")+
                            "_forwardToSuper"+
                             paramArg[0]+' '+throwsClause+"\n");

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
            // sb1.append(STR_REXX_PROXY).append(" a0, ");
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
            // String tmpArg=tmpClz.toString()+" "+"a"+argNr;
            // String tmpArg=tmpClz.getName()+" "+"a"+argNr;
            String tmpArg=getEditedClassName(tmpClz)+" "+"a"+argNr;

            if (i>0)
            {
                sb1.append(", ");
                sb2.append(", ");

                if (bCreateInvokeArg)
                {
                    // sb3.append(", \n\t\t\t");
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

                if (returnType==Character.class)
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

        if (returnType==char.class)
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
     * @param bMethodSignature
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





    /* Inner class to make maintenance and access to management data structures easy.
    *
    *     uCN       ... uppercased class name
    *     uMN       ... uppercased method name
    *     clz       ... class object
    *
    */
    static class RunInfos
    {
        HashMap   uMN2setOfSignatures=new HashMap();    // maps uppercased method name
        HashMap   clz2declMethods    =new HashMap();    // maps class object to declared methods
        HashMap   uCN2clz            =new HashMap();    // maps uppercase class name to class object
    }

}
