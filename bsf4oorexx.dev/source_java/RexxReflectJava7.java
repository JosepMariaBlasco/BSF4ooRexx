package org.rexxla.bsf.engines.rexx;

import org.apache.bsf.BSFException;
import org.apache.bsf.util.type.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Arrays;

import java.lang.reflect.*;

import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

    // allow easy references to the Enum values
import static org.rexxla.bsf.engines.rexx.InvocationType.*;

import        org.rexxla.bsf.engines.rexx.RexxReflectUtil.ReflectionType;
import static org.rexxla.bsf.engines.rexx.RexxReflectUtil.ReflectionType.*;

import        java.lang.reflect.Modifier;

/**
 * This class carries out reflections for Java starting with version 1.8/8. It uses the
 * infrastructure from the <code>java.lang.reflect</code> and the <code>java.lang.invoke</code>
 * (introduced with java 1.7/7) package. In order to be as flexible as possible it
 * employs reflection first and attempts to get {@link MethodHandle}s for them. If
 * there are exceptions while carrying out these steps, a candidate member will be
 * ignored and resolution continues with the next ancestor.
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
 * @version 1.04, 2018-11-01, 2018-11-03, 2018-11-09, 2018-11-13, 2019-08-16
 * @since BSF4ooRexx 600 (implementation started: 2018-01-05)
 * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
 */

/*
    concluded: 2018-02-22, ---rgf (version 1.0)

    changes:

        2018-08-15, rgf:    - fix Javadoc warnings

        2018-11-01, rgf:    - adjusting to new usage of RexxStrictArgument, which makes sure that its classObject
                              is used to strictly compare signatures; is used in picking a strictly matching
                              candidate field, method, constructor
                            - remove access to RexxReflectUtil.bTryCoercions as that field is not used anymore

        2018-11-03, rgf:    - reflectMethod(): special handling for primitive classes needed, because a primitive
                              class object does not have a superclass, so no instance methods can be invoked, which we want
                              for ooRexx users; hence manually setting the superclass to the java.lang.Object class

        2018-11-09, rgf:    - reflectMethod(): reorder logic for special handling of primitive and interface classes

        2018-06-11, rgf:    - employing "mh=mh.asFixedArity();" to make sure that MH invocations behave
                              the same as with comp.lang.reflect invocations for any varargs-argument;
                              solution as per Peter Levart's advice, cf. e-Mails of 2018-06-10/11 in
                              <mlvm-dev@openjdk.java.net>

        2018-11-13, rgf:    - corrected bug in processField() when accessing first element in the
                              returned value of coerceArgs(), which could also be the null value

        2019-08-16, ---rgf, correct Javadocs

*/


class RexxReflectJava7 implements RexxReflectInterface
{
    static final private boolean bDebug=false;  // enable debug output (quite verbose)

    /** If <code>true</code> cache the <code>java.lang.reflect</code> objects and their
     *  corresponding <code>MethodHandle</code>s;.
    */
    public boolean useCaching   =true;      // if true cache java.lang.reflect objects and their MethodHandles; if false do not use caches

    /** If <code>true</code> then <code>java.lang.reflect</code> object's (a {@link java.lang.reflect.Method},
     *  {@link java.lang.reflect.Field}, {@link java.lang.reflect.Constructor} <code>invoke</code> methods will get used,
     *  else the corresponding <code>MethodHandle</code>'s <code>invoke</code> method.
    */
    // rgf, 2018-03-09: ooRexxTry.rxj does not work with MethodHandle.invokeWithArguments(), hence defaulting to core reflection
    // public boolean useReflectInvoke=true;   // use java.lang.reflect object to invoke? if false, use MethodHandles instead
    // rgf, 2018-03-09: for finding out further problems while beta-testing, we use MH.iwA()
    public boolean useReflectInvoke=false; // true;   // use java.lang.reflect object to invoke? if false, use MethodHandles instead

    /** If <code>true</code> then use {@link java.lang.invoke.MethodHandles.Lookup} method <code>unreflect</code>, otherwise
     *  use the corresponding <code>find</code>* methods.
     */
    public boolean useUnreflect =true; // false;     // use MethodHandle.Lookup.unreflect(...) to create MethodHandle from the java.lang.reflect object,
                                            // otherwise use the MethodHandle.Lookup.findXXX(...) versions


/* --- tests, rgf, 2018-03-11

   public boolean useInvokeWithArguments=false; // true; // only supply the arguments, let MethodHandle do the rest

  not really possible from a dynamic language which has no exact type information available to it (it would be
  possible to come with the exact types via reflection, but why duplicate what Java does anyway in invoke() and invokeWithArguments() ?

--------------------------------------------------------------------- example error (cf. WrongMethodTypeException below)
                  ["BSF4ooRexx/routine/BSF(), error 3: Java exception occurred: [org.apache.bsf.BSFException: BSF4ooRexx subfunction ""getFieldValue"":
" || "0A09"x || "bean:        [org.rexxla.bsf.engines.rexx.ArrayWrapper@dc24521] --> type: <org.rexxla.bsf.engines.rexx.ArrayWrapper>" || "0A09"x || "
field:       [items] not found!" || "0A0A09"x || "-> check field name=[items] (caseless o.k., but correct spelling?)" || "0A"x || "]"]
                [org.apache.bsf.BSFException@7e0b0338 id#_-8796059571921]
                        --> exception: [org.apache.bsf.BSFException@7e0b0338 id#_-8796059571921]
                        --> caused by: [java.lang.invoke.WrongMethodTypeException: expected (ArrayWrapper)int but found (Object)Object]
---------------------------------------------------------------------


    public boolean useInvokeExact=false;    // if useInvokeWithArguments==false, then this flag determines whether to use
                                            // invoke() or invokeExact(), the latter inhibits MethodHandle to do its own
                                            // type juggling/coercion
-- */

    /** Allows for turning on and off debug output information.
     *  <dl>
     *  <dt>0
     *  <dd>no output
     *  <dt>1
     *  <dd>Throwable debug output while initially finding and invoking java.lang.reflect object/MethodHandle
     *  <dt>2
     *  <dd>show successful invocations of cached entries (string &quot;WOW&quot; in output message)
     *  <dt>3
     *  <dd>show successful invocations of cached entries with their return value, if any
     *  <dt>4
     *  <dd>show any Throwable while invoking cached entry
     *  </dl>
     *
     */
    public int     debugLevel=0;            // allow turning on (>0) some debug output
                                            // 0 ... no output,
                                            // 1 ... Throwable while finding, initially invoking reflected object/method handle
                                            // 2 ... show successful usage of cached entries ("WOW" output)
                                            // 3 ... show successful usages from cached entry with their return value, if any
                                            // 4 ... Throwable while invoking cached entry

    /** Allows for showing successful invocations and their result. */
    boolean bDebugSuccess=false;     // enable debug output on successful invocation of Constructor, Method or get/setField


    // publicLookup() works for public classes and its public members only; hence, if returning objects from this package
    // to Rexx, we cannot access public members in non-public classes from Rexx

    /** The <code>publicLookup</code> object. */
    final public MethodHandles.Lookup publicLookup=MethodHandles.publicLookup();    // only lookup public members

    /** The <code>lookup</code> object pertaining to this class (allowing access to all classes that this class has access to). */
    final public MethodHandles.Lookup lookup      =MethodHandles.lookup();          // will allow access to all objects from this class

        // must use the "normal" lookup method, as publicLookup() won't allow using objects returned from this package to Rexx
    /** Determines which lookup object to use.
     */
    private MethodHandles.Lookup thisLookup=lookup; // we start out and use by default the full lookup available to this class


// STRANGE: when using publicLookup, then clock-example works; if using fullLookup, then warning comes up???
/*
Java 9:

fullLookup yields warning!?!

    F:\work\svn\bsf4oorexx\trunk\samples>3-010_clock.rxj
    ... [2018-02-17T18:22:58.170000] Rexx main program, now waiting until JFrame gets closed ...
    WARNING: An illegal reflective access operation has occurred
    WARNING: Illegal reflective access by org.rexxla.bsf.engines.rexx.RexxReflectJava7 (file:/F:/work/svn/bsf4oorexx/trunk/) to method sun.java2d.SunGraph
    ics2D.setRenderingHint(java.awt.RenderingHints$Key,java.lang.Object)
    WARNING: Please consider reporting this to the maintainers of org.rexxla.bsf.engines.rexx.RexxReflectJava7
    WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
    WARNING: All illegal access operations will be denied in a future release
    ... [2018-02-17T18:23:11.492000] Rexx main program, JFrame got closed.


publicLookup yields o.k.!?!
    F:\work\svn\bsf4oorexx\trunk\samples>3-010_clock.rxj
    ... [2018-02-17T18:26:05.253000] Rexx main program, now waiting until JFrame gets closed ...
    // // :( RexxReflectJava7.processMethod(), thisLookup.findVirtual(...) caused "java.lang.IllegalAccessException: symbolic reference class is not acces
    sible: class sun.java2d.SunGraphics2D, from public Lookup", hence Method "class sun.java2d.SunGraphics2D"."public void sun.java2d.SunGraphics2D.setRen
    deringHint(java.awt.RenderingHints$Key,java.lang.Object)" not a candidate, returning
    // // :( RexxReflectJava7.processMethod(), thisLookup.findVirtual(...) caused "java.lang.IllegalAccessException: symbolic reference class is not acces
    sible: class sun.java2d.SunGraphics2D, from public Lookup", hence Method "class sun.java2d.SunGraphics2D"."public void sun.java2d.SunGraphics2D.setCol
    or(java.awt.Color)" not a candidate, returning
    // // :( RexxReflectJava7.processMethod(), thisLookup.findVirtual(...) caused "java.lang.IllegalAccessException: symbolic reference class is not acces
    sible: class sun.java2d.SunGraphics2D, from public Lookup", hence Method "class sun.java2d.SunGraphics2D"."public void sun.java2d.SunGraphics2D.drawOv
    al(int,int,int,int)" not a candidate, returning
    // // :( RexxReflectJava7.processMethod(), thisLookup.findVirtual(...) caused "java.lang.IllegalAccessException: symbolic reference class is not acces
    sible: class sun.java2d.SunGraphics2D, from public Lookup", hence Method "class sun.java2d.SunGraphics2D"."public void sun.java2d.SunGraphics2D.drawLi
    ne(int,int,int,int)" not a candidate, returning
    ... [2018-02-17T18:26:22.804000] Rexx main program, JFrame got closed.


// private MethodHandles.Lookup thisLookup=publicLookup;   // this makes the clock warning go away!
                                                // but makes it impossible to access public members in objects
                                                // that stem from non-public classes)
*/

/* Java 7 - problems (2018-02-21):

    // // :( RexxReflectJava7.processMethod(), thisLookup.findVirtual(...) caused "java.lang.IllegalAccessException: symbolic reference class is not public: class java.util.AbstractList$Itr, from org.rexxla.bsf.engines.rexx.RexxReflectJava7", hence Method "class java.util.AbstractList$Itr"."public boolean java.util.AbstractList$Itr.hasNext()" not a candidate, returning
    // // :( RexxReflectJava7.processMethod(), thisLookup.findVirtual(...) caused "java.lang.IllegalAccessException: symbolic reference class is not public: class java.util.Collections$UnmodifiableCollection, from org.rexxla.bsf.engines.rexx.RexxReflectJava7", hence Method "class java.util.Collections$UnmodifiableCollection"."public int java.util.Collections$UnmodifiableCollection.size()" not a candidate, returning
    // // :( RexxReflectJava7.processMethod(), thisLookup.findVirtual(...) caused "java.lang.IllegalAccessException: symbolic reference class is not public: class java.util.Collections$SynchronizedCollection, from org.rexxla.bsf.engines.rexx.RexxReflectJava7", hence Method "class java.util.Collections$SynchronizedCollection"."public java.util.Iterator java.util.Collections$SynchronizedCollection.iterator()" not a candidate, returning

    ---> therefore using RexxReflectJava6 instead of RexxReflectJava7, which adds MethodHandle support!

*/


    /** Returns the currently used lookup object.
     *  @return returns the currently used lookup object
     */
    public MethodHandles.Lookup getLookup()
    {
        return thisLookup;
    }

    /** This allows Rexx to set the lookup object. If clearing the cache using {@link #clearCache} and then using this  method
     *  to set lookup to {@link #publicLookup}, Rexx cannot access objects from this package anymore, which makes it impossible
     *  to use this method to reset to full lookup.
     *
     * @param mhl the {@link java.lang.invoke.MethodHandles.Lookup} object to use
     */
    public void setLookup(MethodHandles.Lookup mhl)
    {
        if (!lookupWasSetToPublic)
        {
            lookupWasSetToPublic=(mhl==publicLookup);   // if set to a public lookup, memorize it, such that switching rxReflector keeps that constraint
        }

        thisLookup=mhl;
    }



    /** If <code>true</code> then the <code>lookup</code> was set to <code>publicLookup</code> which will
     *  also be used for new <code>RexxAndJavaInterface</code> objects that may have been created afterwards
     *  via the BSF-subfunction 'rexxReflectObject', 'javaVersion'. <code>lookup</code> are introduced with
     *  Java 7 to find a {@link java.lang.invoke.MethodHandle}.
     */
    boolean lookupWasSetToPublic=false;

    /** Returns the current setting, which affects the reflection operation on Java 9 or higher.
     *
     * @return <code>false</code>, if no restriction to a {@link java.lang.invoke.MethodHandles.Lookup} <code>publicLookup</code> is in effect (cannot be
     *                reversed for a {@link RexxAndJava} instance, <code>true</code> else
     */
    public boolean isPublicLookup()
    {
        return lookupWasSetToPublic;
    }

    /** Will set the current {@link java.lang.invoke.MethodHandles.Lookup} lookup object to the <code>publicLookup</code>
     *  object, thereby restricting all opeartions to the reference class <code>java.lang.Object</code>
     *  for the current {@link RexxAndJava} instance.
     *  This operation cannot be reverted for security reasons.
     */
    public void    setPublicLookup()
    {
        if (!lookupWasSetToPublic)      // if not yet set to publicLookup
        {
            setLookup(publicLookup);
        }
    }



    /** The Java runtime version that this instance was created for.
     */
    public int javaVersion2Support=9;



        // allows to memorize MethodHandle type, needed when invoking
    /** Enum values for the different kinds of invocation. */
    static enum MH_Kind {
        METHOD,
        STATIC_METHOD,  // do not supply 'object' argument
        GETTER,
        STATIC_GETTER,  // do not supply 'object' argument
        SETTER,
        STATIC_SETTER   // do not supply 'object' argument
        ;
    }

    /** Empty {@link java.lang.Class} array field.  */
    static Class<?>[]emptyClassArray=new Class[0];  // empty array object of type Class for

    /** Class to hold cache entries for {@link java.lang.reflect.Field} and {@link java.lang.reflect.Method} objects together
     *  with their corresponding {@link java.lang.invoke.MethodHandle}s.
     */
    static class CachedFieldOrMethod    // used in ArrayList
    {
        /** The reflected {@link java.lang.reflect.Field} and {@link java.lang.reflect.Method} object. */
        Object        ro;           // java.lang.reflect object (Field or Method)
        /** Indicates whether the entry is stored in the opposite cache, i.e. if this is <code>true</code>, then
         *  an entry in {@link #cachedFields} is a reflected {@link java.lang.reflect.Method} object instead, and
         *  an entry in {@link #cachedMethods} is a reflected {@link java.lang.reflect.Field} object instead.
        */
        boolean       bOtherKind;   // if true, then cachedMethods entry is actually caching a Field object, and
                                    // a cachedFields entry is actually caching a (getter/setter) Method object;
                                    // needed when java.lang.reflect object invocation is desired
        /** The corresponding {@link java.lang.invoke.MethodHandle} */
        MethodHandle  mh;           // MethodHandle
        /** The kind of invocation for this {@link MethodHandle}. */
        MH_Kind       mhk;          // remember kind of method handle (see above)
        /** The types of argument to supply at invocation time. */
        Class<?>[] parameterTypes;  // remember parameterTypes to speed up coercions

        /** Constructor.
         * @param newRo the {@link java.lang.reflect.Field} and {@link java.lang.reflect.Method} to cache
         * @param newOtherKind if <code>true</code> the entry is stored in the opposite cache
         * @param newMH the corresponding {@link MethodHandle} to cache
         * @param newMHK the invocation kind
         * @param newParameterTypes the types to supply for invoking this cache entry
         */
        CachedFieldOrMethod (Object newRo, boolean newOtherKind, MethodHandle newMH, MH_Kind newMHK, Class<?>[] newParameterTypes)
        {
            this.ro            =newRo;
            this.bOtherKind    =newOtherKind;
            this.mh            =newMH;
            this.mhk           =newMHK;
            this.parameterTypes=newParameterTypes;
        }

        /** Returns a rendering of this instance to ease debugging.
         *
         * @return  a rendering of this instance
         */
        public String toString()
        {
            return "CachedFieldOrMethod[o="+this.ro+",bOtherKind="+this.bOtherKind+",mhk="+this.mhk+",mh="+this.mh+",parameterTypes="+Arrays.deepToString(parameterTypes)+"]";
        }
    }

    /** Class to hold cache entries for {@link java.lang.reflect.Constructor} objects together
     *  with their corresponding {@link MethodHandle}s.
     */
    static class CachedConstructor
    {
        Constructor     ro;             // java.lang.reflect object: Constructor
        MethodHandle    mh;             // MethodHandle
        Class<?>[]      parameterTypes; // remember parameterTypes to speed up coercions

        /** Constructor.
         * @param newRo the {@link java.lang.reflect.Constructor} to cache
         * @param newMH the corresponding {@link MethodHandle} to cache
         * @param newParameterTypes the types to supply for invoking this cache entry
         */
        CachedConstructor (Constructor newRo, MethodHandle newMH,  Class<?>[] newParameterTypes)
        {
            this.ro            =newRo;
            this.mh            =newMH;
            this.parameterTypes=newParameterTypes;
        }

        /** Returns a rendering of this instance to ease debugging.
         *
         * @return  a rendering of this instance
         */
        public String toString()
        {
            return "CachedConstructor[o="+this.ro+",mh="+this.mh+",parameterTypes="+Arrays.deepToString(parameterTypes)+"]";
        }
    }

        // Class<?> -> String (name of member)-> ArrayList of already resolved Methods|Fields & their MethodHandles
    /** The cache for reflected fields. */
    static ConcurrentHashMap<Class<?>,ConcurrentHashMap<String,ArrayList<CachedFieldOrMethod>>> cachedFields  = new ConcurrentHashMap() ;
    /** The cache for reflected methods. */
    static ConcurrentHashMap<Class<?>,ConcurrentHashMap<String,ArrayList<CachedFieldOrMethod>>> cachedMethods = new ConcurrentHashMap() ;

        // constructors have always the same name, hence no need to maintain their internal name "<init>"
    /** The cache for reflected constructors. */
    static ConcurrentHashMap<Class<?>,ArrayList<CachedConstructor>>                        cachedConstructors = new ConcurrentHashMap() ;

    /** Clears the cache, ie. {@link #cachedFields}, {@link #cachedMethods} and {@link #cachedConstructors}.
     */
    static public void clearCache()
    {
        cachedFields       = new ConcurrentHashMap() ;
        cachedMethods      = new ConcurrentHashMap() ;
        cachedConstructors = new ConcurrentHashMap() ;
    }


    /** Constructs an instance and memorizes the runtime Java version to support (for debugging purposes).
     *
     * @param javaVersion the Java runtime version that this instance was created for
     */
    RexxReflectJava7 (int javaVersion)
    {
        this.javaVersion2Support=javaVersion;
    }



        // ========================================================================================================
    /** Method that determines and invokes the needed reflection.
     *
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     */
    public void reflect(final RexxReflectUtil rru) throws BSFException // , IllegalAccessException (in Java 9)
    {
if (bDebug)
{
System.err.println("========================= > "+this+" < =============================="
                            +" useCaching="+useCaching+", useReflectInvoke="+useReflectInvoke+", useUnreflect="+useUnreflect);
        System.err.println(this+" -> reflect(rru): "+rru.debugString());
}

            // proceed according to reflectionType
        switch (rru.reflectionType)
        {
            case REFLECT_FIELD        :  reflectField (rru);
                                         return ;

            case REFLECT_METHOD       :  reflectMethod(rru);
                                         return;

            case REFLECT_CONSTRUCTOR  :  reflectConstructor(rru);
                                         return;
        }

        throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                                "Unexecpected reflection type '"+rru.reflectionType+"'");
    }


// SECTION: FIELD =================================================================================================

        // ========================================================================================================
    /**
     * Reflect a field. This method consults the cache, if {@link #useCaching} is set and tries to invoke every
     * cached method using either {@link #invokeFieldGet} or {@link #invokeFieldSet}, depending on the
     * cached {@link MH_Kind} value.
     * <br>
     * If no cache is found all fields of the bean are reflected and {@link #processField} will
     * get invoked. The reflection process will take all ancestors and interfaces into account.
     *
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     *
     * @throws BSFException if any of the invoked methods throw an error a {@link BSFException} gets thrown that wraps it
     */
    void reflectField(RexxReflectUtil rru) throws BSFException
    {
if (bDebug) System.out.println("... reflectField(): arrived, cachedFields.containsKey("+rru.beanClz+"): \""+cachedFields.containsKey(rru.beanClz)+"\"");
        // let us first check, whether we have a usable cache entry already
        if (useCaching && cachedFields.containsKey(rru.beanClz))
        {
                // fetch ArrayList of method handles for this field name
            ArrayList<CachedFieldOrMethod> al=cachedFields.get(rru.beanClz).get(rru.keyMemberName);

            if (al!=null)       // we found a list, check it!
            {
                for (CachedFieldOrMethod cfm : al)
                {
                    switch (cfm.mhk)    // only invoke, if MethodHandle is of the appropriate type
                    {
                        case GETTER:
                        case STATIC_GETTER:
                            if (rru.bFieldGet)
                            {
                                if (cfm.parameterTypes.length>0 || rru.funcArgs.length>0)    // not a get MethodHandle
                                {

// System.out.println("... ... reflectField(): GETTER :( from cache ["+cfm.ro.toString() +"] paramTypes.length>0 "+cfm.parameterTypes.length+", funcArgs.length>0 "+rru.funcArgs.length);
                                    continue;
                                }
                                invokeFieldGet(cfm,rru);
                            }
                            break;

                        case SETTER:
                        case STATIC_SETTER:
                            if (!rru.bFieldGet)
                            {
                                if (cfm.parameterTypes.length!=1 || rru.funcArgs.length!=1)    // not a set MethodHandle
                                {
// System.out.println("... ... reflectField(): SETTER :( from cache ["+cfm.ro.toString() +"] !=1: paramTypes.length "+cfm.parameterTypes.length+"!= funcArgs.length "+rru.funcArgs.length);
                                    continue;
                                }
                                invokeFieldSet(cfm,rru);
                            }
                            break;

                            // rgf, 20180210: a setter or getter method for a private field (ooRexx 'attribute' semantics)
                        case METHOD:
                        case STATIC_METHOD:

// System.out.println("... ... reflectField(): from cache, using \"invokeMethod(cfm,rru)\" ..");
                             invokeMethod(cfm,rru);
                            break;

                        default:
if (bDebug)
System.out.println("... ... reflectField(): UNKNOWN CACHE ENTRY !! :( ["+cfm.ro.toString()+"]");
                            continue;
                    }

                    if (rru.success)     // found, return
                    {
if (debugLevel>1)
System.err.println("... reflectField():  WOW: found cached Field/MethodHandle (field name: \""+rru.keyMemberName+"\") and could execute it appropriately! : ["+cfm.mh.toString()+"], result=["+rru.result+"]");
                        return;
                    }

                }
            }
        }


        boolean bStartLevel=true;   // we are about to start out with reflection
        Class   tmpClz=rru.beanClz; // start out class

if (bDebug) System.out.println("... reflectField(): ---> no appropriate cache found, starting reflection with rru.beanClz=\""+tmpClz+"\"");
        while (rru.success==false)   // as long as we have not found a matching Field object, keep processing
        {
            Field tmpFields[]=tmpClz.getDeclaredFields();
            for (int i=0; i<tmpFields.length; i++)
            {
                Field field=tmpFields[i];
                int   fieldMods=field.getModifiers();

                if ((fieldMods & Modifier.PUBLIC)==0)  // not PUBLIC: ignore
                {
                    continue;
                }

                if ( !rru.bFieldGet && ((fieldMods & Modifier.FINAL)!=0) ) // a SET operation, but field is FINAL, ignore
                {
                    continue;
                }

                if ((rru.bBeanIsClass || rru.bStatic ) && ( ((fieldMods & Modifier.STATIC)==0)) ) // looking for static, not STATIC, ignore
                {
                    continue;
                }

                // note: this is the first public Field; there may have been non-public fields in descendants that got ignored;
                //       javac allows for casting access to fields in ancestors and thereby directly accessing such a Field as well

                processField(field, rru);   // now process this Field
                if (rru.success)             // succesfully processed, return
                {
                    return;
                }
            }


            if (rru.bFieldGet)              // maybe a public static field in one of the Interfaces tmpClz implements ?
            {
                for (Class interf : tmpClz.getInterfaces()) // iterate over interfaces implemented by this class
                {
                    getAndProcessFieldFromInterface(interf, rru); // Interface members are always PUBLIC, fields are always STATIC
                    if (rru.success)         // succesfully processed, return result
                    {
                        return;
                    }
                }
            }

            tmpClz=tmpClz.getSuperclass();      // try next superclass
            if (tmpClz==java.lang.Object.class) // we are done, java.lang.Object has no fields
            {
                break;
            }
        }

        if (rru.bRecursing)     // we are already in try mode, return
        {
            return;
        }

        if (!rru.bStrict)       // not in strict mode, hence try getter and setter methods
        {
            // =============================== special additional service :)
            /* ooRexx-semantics: camouflage Java fields as ooRexx attributes; if Java fields are private, try
                                 the Java getter/setter pattern "setXXX(newValue)" and "getXXX()", in the case
                                 of boolean/Boolean getters also "isXXX()" and "hasXXX()"
            */

if (bDebug) System.err.println("... reflectField(): no field ["+rru.keyMemberName+"] found, still trying hard ;) ...");

            // Rexx might have supplied a private field: search for an appropriate getter or setter method and use that instead
            RexxReflectUtil tmpRru=rru.copy();          // create a copy of the current RexxReflectUtil

            tmpRru.bOtherKind = true;    // if found, then store in cachedFields instead of cachedMethods
            tmpRru.bRecursing = true;    // inhibit endless recursions

            // the following mimickries ooRexx attributes
            //          - assume fields, XXX is memberName
            //              - setter: setXXX, if one argument, cf. RexxAndJava, line # 3458
            if (!tmpRru.bFieldGet)      // try a setXXX
            {
                tmpRru.bFieldGet        = false;
                tmpRru.memberName       = "SET"+rru.keyMemberName;  // create name of appropriate setter method

if (bDebug) System.err.println("... reflectField(): tmpRru.bFieldGet=[false], now trying method name ["+tmpRru.memberName+"] ...");
                reflectMethod(tmpRru);
            }

            else        // try getXXX, isXXX, hasXXX in this order
    //          - assume fields, XXX is memberName
    //              - getter:   cf. RexxAndJava, line # 4175
    //                  - getXXX, if no argument
    //                  - isXXX | hasXXX, if no argument (Booleans may have "is" or "has" instead of "get" as prefix to getter name)
            {
                tmpRru.memberName       = "GET"+rru.keyMemberName;  // try getter method first

if (bDebug) System.err.println("... reflectField(): tmpRru.bFieldGet=[false], now trying method name ["+tmpRru.memberName+"] ...");
                reflectMethod(tmpRru);
                if (tmpRru.success)          // found & processed, return
                {
                    rru.success=true;
                    rru.result=tmpRru.result;
                    return;
                };

                tmpRru.memberName       = "IS" +rru.keyMemberName;  // try another getter method for Booleans
if (bDebug) System.err.println("... reflectField(): tmpRru.bFieldGet=[false], now trying method name ["+tmpRru.memberName+"] ...");
                reflectMethod(tmpRru);
                if (tmpRru.success)          // found & processed, return
                {
                    rru.success=true;
                    rru.result=tmpRru.result;
                    return;
                };

                tmpRru.memberName       = "HAS"+rru.keyMemberName;  // try another getter method for Booleans
if (bDebug) System.err.println("... reflectField(): tmpRru.bFieldGet=[false], now trying method name ["+tmpRru.memberName+"] ...");
                reflectMethod(tmpRru);
            }

            if (tmpRru.success)
            {
                rru.success=tmpRru.success;
                rru.result=tmpRru.result;
                return;
            }
        }

        // throw BSFException method not found, supply all information with it as in earlier versions of BSF4ooRexx
        rru.throwNotFoundBSFException();
    }


        // ========================================================================================================
        // process Interface class, all fields are static and public
    /**
     * This method will process all fields of an interface and {@link #processField} will
     * get invoked. The reflection process will take all interface ancestors into account.
     *
     * @param tmpClz the interface class object to process
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     */
    void getAndProcessFieldFromInterface(Class tmpClz, RexxReflectUtil rru)
    {
        Object result;

if (bDebug) System.err.println("\\\\// RexxReflectJava7.getAndProcessFieldFromInterface: tmpClz=["+tmpClz.toString()+"]");
        for (Field tmpField : tmpClz.getDeclaredFields())   // process all Fields in this Interface
        {
            processField(tmpField, rru);
            if (rru.success)         // succesfully processed, return
            {
                return;
            }
        }

        // not yet found, process super Interfaces recursively until found!
        for (Class superInterface : tmpClz.getInterfaces())
        {
            getAndProcessFieldFromInterface(superInterface, rru);
            if (rru.success)         // succesfully processed, return result
            {
                return;
            }
        }
    }


        // ========================================================================================================
        // Check name and carry out GET or SET operation, if GET, return value
    /** If a {@link java.lang.reflect.Field} is public and the Rexx arguments can be coerced to its parameter types
     *  the field becomes eligible. If a {@link MethodHandle} can be created for it successfully and the invocation
     *  succeeds without an error, then a cache entry will be created in {@link #cachedFields}.
     *
     * @param tmpField the {@link java.lang.reflect.Field} object to work on
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     */
    void processField (Field tmpField, RexxReflectUtil rru)
    {
        String fieldName=tmpField.getName();

if (bDebug)
System.err.println("// // // RexxReflectJava7.processField(), ARRIVED: -> \""+rru.choice+"\", tmpField=\""+tmpField.toString()+"\": rru.memberName=\""+rru.memberName+"\" in object=[rru.rexxArgs[1]=\""+rru.rexxArgs[1]+"\"/rru.bean=\""+rru.bean+"\"]");

        if (rru.bStrict)        // strict, then use name as is!
        {
            if (!fieldName.equals(rru.memberName))
            {
if (bDebug) System.err.println("// // // RexxReflectJava7.processField(), \"! \""+fieldName+"\".equals(\""+rru.memberName+"\")\": "+!fieldName.equalsIgnoreCase(rru.memberName));
                return;
            }
        }
        else if (!fieldName.equalsIgnoreCase(rru.memberName))
        {
if (bDebug) System.err.println("// // // RexxReflectJava7.processField(), \"! \""+fieldName+"\".equalsIgnoreCase(\""+rru.memberName+"\")\": "+!fieldName.equalsIgnoreCase(rru.memberName));
            return;
        }

if (bDebug)
System.err.println("         -> RexxReflectJava7.processField(): => ["+rru.choice+"]: FOUND candidate Field=["+rru.memberName+"] in object=[" + rru.rexxArgs[1]+ "/" + rru.bean + "]"+ (rru.bFieldGet==true ? "" : " newValue=["+rru.rexxArgs[3]+"/"+rru.funcArgs[0]+"]"));

        int   fieldMods=tmpField.getModifiers();    // get Field's modifiers
        boolean bStaticField=((fieldMods & Modifier.STATIC) !=0);

        MethodHandle mh=null;

            // fetch the MethodHandle, if a Throwable, then not an eligible java.lang.reflect object
        if (useUnreflect)      // use (simplest) the Lookup.unreflect(...) version
        {
            try {
                if (rru.bFieldGet)
                {
                    mh=thisLookup.unreflectGetter(tmpField);
                }
                else
                {
                    mh=thisLookup.unreflectSetter(tmpField);
                }
            }
            catch (Throwable t)
            {
if (debugLevel>0) System.err.println("// // :( RexxReflectJava7.processField(), thisLookup=\""+thisLookup+"\".unreflect(tmpMethod) caused \""+t
                                          +"\", hence Field \""+tmpField.getDeclaringClass().toString()+"\".\""+tmpField.toString()+ "\" not a candidate, returning");
                rru.throwable=t;    // save Throwable to allow passing it back as a cause

                return;
            }
        }
        else    // use Lookup.findXXX(...) versions
        {
            String strLookupName=null;
            try     // access via MethodHandle with the rights of rru.firstExportedClz
            {
                if (rru.bFieldGet)      // getter
                {
                    if (bStaticField)
                    {
                        strLookupName="findStaticGetter";
                        mh=thisLookup.findStaticGetter(tmpField.getDeclaringClass(), fieldName, tmpField.getType());
                    }
                    else    //instance
                    {
                        strLookupName="findGetter";
                        mh=thisLookup.findGetter(tmpField.getDeclaringClass(), fieldName, tmpField.getType());
                    }
                }
                else // setter
                {
                    if (bStaticField)
                    {
                        strLookupName="findStaticSetter";
                        mh=thisLookup.findStaticSetter(tmpField.getDeclaringClass(), fieldName, tmpField.getType());
                    }
                    else    // instance
                    {
                        strLookupName="findSetter";
                        mh=thisLookup.findSetter(tmpField.getDeclaringClass(), fieldName, tmpField.getType());
                    }
                }
            }
            catch (Throwable t)
            {
if (debugLevel>0) System.err.println("// // :( RexxReflectJava7.processField(), thisLookup=\""+thisLookup+"\"."+strLookupName+"(...) caused \""+t
                                         +"\", hence Field \""+tmpField.getDeclaringClass().toString()+"\".\""+tmpField.toString()+ "\" not a candidate, returning");
               rru.throwable=t;    // save Throwable to allow passing it back as a cause
               return; //  null;    // not successful :(
            }
        }
        mh=mh.asFixedArity(); // rgf, 2018-06-11 as per Peter Levart's advice, cf. e-Mails of 2018-06-10/11 in <mlvm-dev@openjdk.java.net>

if (debugLevel>0) System.err.println("// // :) RexxReflectJava7.processField(), tmpField=["+tmpField.toString()+"]");


        Object  newValue=null;
        Class  tmpFieldType=null;

        if (!rru.bFieldGet)     // processing a set operation, get new value and coerce it if necessary
        {
            newValue =rru.funcArgs[0];        // get new value to set the Field to; convFromRexx already carried out!
            tmpFieldType=tmpField.getType();

                // newValue can be null, a Java object (from the registry) or a string
            if ( newValue != null )
            {
                Object newValueArr[]=rru.rajo.coerceArgs( new Object[]{newValue}, new Class[]{tmpFieldType} );   // coerce newValue appropriately
                if (newValueArr==null)             // coercion did not work, not a proper argument for this Field, return
                {
                    return;
                }
                newValue=newValueArr[0];    // fetch value
            }
        }

            // invoke the method handle
        if (useReflectInvoke)     // use java.lang.reflect.Field to invoke; same arguments for static and instance invocations
        {
            if (rru.bFieldGet)
            {
if (bDebug) System.err.println("// // // -> RexxReflectJava7.processField(): GET-operation: tmpField=\""+tmpField.getName()+"\" tmpField.getDeclaringClass()=\""+tmpField.getDeclaringClass()+"\"<<<\n");
                try
                {
                    if (bStaticField)
                    {
                        rru.result=tmpField.get(null);      // get and return Field value
                    }
                    else
                    {
                        rru.result=tmpField.get(rru.bean);  // get and return Field value
                    }
                }
                catch (Throwable t1)
                {
if (debugLevel>0)
        System.err.println("// // :( -> RexxReflectJava7.processField(): GET invocation for Field \""
                                 +tmpField.getDeclaringClass().toString()+"\".\""+tmpField.toString()+"\""
                                 +" caused \""+t1+"\", not a candidate, returning");
                    rru.throwable=t1;       // save Throwable to allow passing it back as a cause
                    return;
                }

                rru.success=true;                           // indicate we have found a Field and got its value
            }
            else    // a set operation!
            {
if (bDebug)
{
    System.err.println("// // // -> RexxReflectJava7.processField(): SET-operation: tmpField=\""+tmpField.getName()+"\" tmpField.getDeclaringClass()=\""+tmpField.getDeclaringClass()+"\"<<<\n");
    System.err.println("// // //                                                    rru.funcArgs[0]=\""+rru.funcArgs[0]+"\"->newValue=\""+newValue+"\" | .getClass()=\""+newValue.getClass()+"\"; tmpFieldType=\""+tmpFieldType+"\"");
}

                // attempt assignment
                try {
                    tmpField.set(rru.bean, newValue);
                    rru.success=true;        // indicate it worked, return
                    return;
                }

                catch (Throwable t1)
                {
if (debugLevel>0)
            System.err.println("// // :( -> RexxReflectJava7.processField(): SET invocation for bean's =\""+rru.bean+"\" Field \""
                                         +tmpField.getDeclaringClass().toString()+"\".\""+tmpField.toString()+"\""
                                         +" to the valeu=\""+newValue+"\""
                                         +" caused \""+t1+"\", not a candidate, returning");
                    rru.throwable=t1;       // save Throwable to allow passing it back as a cause
                    return;
                }

            }
        }
        else    // invoke using MethodHandle
        {
            String strInfo=null;
            try {
                if (rru.bFieldGet)  // invoke getter MethodHandle
                {
                    if (bStaticField)
                    {
                        strInfo="GET (STATIC)";
                        rru.result=mh.invoke();         // no argument for a static getter ORIGINAL

                        // rru.result=mh.invokeWithArguments((Object []) null);    // no receiver for a static getter ORIGINAL
                    }
                    else
                    {
                        strInfo="GET";
                        rru.result=mh.invoke(rru.bean); // supply bean

                        // o.k. rru.result=mh.invokeWithArguments( rru.bean );    // no argument for a static setter
                        // rru.result=mh.invokeWithArguments( (Object []) new Object[]{rru.bean});    // receiver object
                    }
                }
                else    // setter MethodHandle
                {
                    if (bStaticField)
                    {
                        strInfo="SET (STATIC)";
                        mh.invoke(newValue);    // no argument for a static setter

                        // does not work? mh.invokeWithArguments( (Object []) new Object[]{newValue});    // no argument for a static setter

                        // mh.invokeWithArguments( (Object []) new Object[]{newValue});    // no receiver object for a static setter

                    }
                    else
                    {
                        strInfo="SET";
                        mh.invoke(rru.bean, newValue);  // supply bean
                        // does not work? mh.invokeWithArguments( (Object []) new Object[]{rru.bean, newValue});    // no argument for a static setter
                        // mh.invokeWithArguments( (Object []) new Object[]{rru.bean, newValue});    // no argument for a static setter
                    }
                }

            }
            catch (Throwable t)
            {
if (debugLevel>0)
System.err.println("// // :( -> RexxReflectJava7.processField(): MethodHandle "+strInfo+" invocation caused \""+t+"\", not a candidate, returning");
               rru.throwable=t;    // save Throwable to allow passing it back as a cause
               return;              // not successful :(
            }
        }
        rru.success=true;        // indicate we could carry out the desired operation

if (debugLevel>2 || bDebugSuccess)
             System.err.println("// // :) -> RexxReflectJava7.processField(): bean=\""+rru.bean+"\", ["
                                                +rru.choice+"] invocation using ["
                                                +(useReflectInvoke ? "java.lang.reflect.Field" : "MethodHandle")
                                                +"]" +" ["+tmpField.toString()+"]"
                                                +" SUCCESSFUL, rru.result=\""+rru.result+"\""
                                                +(rru.result==null ? "" : " .toString()=\""+rru.result.toString()+"\"")
                                                +(rru.bFieldGet==true ? "" : " - newValue=\""+newValue+"\""
                                                      +( newValue==null ? "" : " .toString()=\""+newValue.toString()+"\"")
                                                  )
                                                );

        if (useCaching)    // add Method object and corresponding MethodHandle to cache
        {
            if (rru.bFieldGet)      // getter logic
            {
                if (bStaticField)
                {
                    if (!rru.bOtherKind)
                    {
                        add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpField, rru.bOtherKind, mh, MH_Kind.STATIC_GETTER, emptyClassArray), cachedFields, rru);
                    }
                    else    // store with cachedMethods instead (ooRexx attribute pattern, getter)
                    {
if (bDebug) System.err.println("// // // -> RexxReflectJava7.processField(): FAKING field access using STATIC_METHOD instead");
                        add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpField, rru.bOtherKind, mh, MH_Kind.STATIC_METHOD,emptyClassArray), cachedMethods, rru);
                    }
                }
                else
                {
                    if (!rru.bOtherKind)     // store with cachedMethods inestead
                    {
                        add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpField, rru.bOtherKind, mh, MH_Kind.GETTER,emptyClassArray), cachedFields, rru);
                    }
                    else    // store with cachedMethods instead (ooRexx attribute pattern, getter)
                    {
if (bDebug) System.err.println("// // // -> RexxReflectJava7.processField(): FAKING field access using METHOD instead");
                        add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpField, rru.bOtherKind, mh, MH_Kind.METHOD,emptyClassArray), cachedMethods, rru);
                    }
                }
            }

            else        // setter logic
            {
                if (bStaticField)
                {
                    if (!rru.bOtherKind)
                    {
                        add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpField, rru.bOtherKind, mh, MH_Kind.STATIC_SETTER, new Class[]{tmpFieldType}), cachedFields, rru);
                    }
                    else    // store with cachedMethods instead (ooRexx attribute pattern, setter)
                    {
if (bDebug) System.err.println("// // // -> RexxReflectJava7.processField(): FAKING field access using STATIC_METHOD instead");
                        add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpField, rru.bOtherKind, mh, MH_Kind.STATIC_METHOD, new Class[]{tmpFieldType}), cachedMethods, rru);
                    }

                }
                else
                {
                    if (!rru.bOtherKind)     // store with cachedMethods inestead
                    {
                        add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpField, rru.bOtherKind, mh, MH_Kind.SETTER, new Class[]{tmpFieldType}), cachedFields, rru);
                    }
                    else    // store with cachedMethods instead (ooRexx attribute pattern, setter)
                    {
if (bDebug) System.err.println("// // // -> RexxReflectJava7.processField(): FAKING field access using METHOD instead");
                        add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpField, rru.bOtherKind, mh, MH_Kind.METHOD, new Class[]{tmpFieldType}), cachedMethods, rru);
                    }
                }
            }
        }

    }


        // ========================================================================================================
        // Check name and carry out GET or SET operation, if GET, return value
    /** The supplied cache entry will be used to carry out a field get invocation.
     *
     * @param cfm   the cache entry to use for invocation
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     *
     */
    void invokeFieldGet (CachedFieldOrMethod cfm, RexxReflectUtil rru)
    {
if (bDebug) System.err.println("...invokeFieldGet(): cfm="+cfm+" | rru.choice=["+rru.choice+"]");
        try     // invoke field get operation
        {
            if (cfm.mhk==MH_Kind.STATIC_GETTER)
            {
                if (useReflectInvoke)      // use java.lang.reflect object
                {
                    if (cfm.bOtherKind)     // this is a Method object instead!
                    {
                        rru.result=((Method) cfm.ro).invoke(rru.bean);
                    }
                    else
                    {
                        rru.result=((Field) cfm.ro).get(rru.bean);
                    }
                }
                else    // use MethodHandle
                {
                    rru.result=cfm.mh.invoke();         // no argument for a static getter
                }
            }
            else // instance get
            {
                if (useReflectInvoke)      // use java.lang.reflect object
                {
                    if (cfm.bOtherKind)     // this is a Method object instead!
                    {
                        rru.result=((Method) cfm.ro).invoke(rru.bean);
                    }
                    else
                    {
                        rru.result=((Field) cfm.ro).get(rru.bean);
                    }
                }
                else    // use MethodHandle
                {
                    rru.result=cfm.mh.invoke(rru.bean); // supply bean
                    // does not work ? rru.result=cfm.mh.invokeWithArguments( new Object[]{rru.bean});    // no argument for a static setter
                }
            }
        }
        catch (Throwable t)
        {
if (debugLevel>3) System.err.println("// // :( -> RexxReflectJava7.invokeFieldGet(), t=\""+t+"\"");
           rru.throwable=t;
           return;              // not successful :(
        }

        rru.success=true;        // we could successfully carry out the get operation!

if (debugLevel>2 || bDebugSuccess) System.err.println("// // :) -> RexxReflectJava7.invokeFieldGet(): bean=\""+rru.bean+"\",["
                                                +rru.choice+"] invocation using ["
                                                +(useReflectInvoke ? "java.lang.reflect.Field" : "MethodHandle")
                                                +"] successful, rru.result=\""+rru.result+"\""
                                                +(rru.result==null ? "" : " .toString()=\""+rru.result.toString()+"\"")
                                                );

    }


    // ========================================================================================================
    /** The supplied cache entry will be used to carry out a field set invocation.
     *
     * @param cfm   the cache entry to use for invocation
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     *
     */
    void invokeFieldSet (CachedFieldOrMethod cfm, RexxReflectUtil rru)
    {
if (bDebug)
{
    System.err.println("...invokeFieldSet().1: cfm=["+cfm+"] | rru.choice=["+rru.choice+"], newValue=rru.funcArgs[0]=["+rru.funcArgs[0]+"].getClass()=["+rru.funcArgs[0].getClass()+"]");
    System.err.println("                       : cfm.mh.type().parameterArray()=["+Arrays.deepToString(cfm.mh.type().parameterArray())+"]");
}

        Object newValue=rru.funcArgs[0];    // get new value to set the Field to; convFromRexx already carried out!
        try     // invoke field set operation
        {
            Class tmpFieldType=null;

            if ( newValue != null )         // newValue can be null, a Java object (from the registry)  or a string
            {
                // TODO: if coerceArgs() returns null, we could create a more informative Throwable ("newValue could not be coerced to ParameterTypes[0]")
                newValue=rru.rajo.coerceArgs( new Object[]{newValue}, cfm.parameterTypes)[0];   // coerce newValue appropriately
            }

            if (cfm.mhk==MH_Kind.STATIC_SETTER)
            {
                if (useReflectInvoke)      // use java.lang.reflect object
                {
                    if (cfm.bOtherKind)     // this is a Method object instead!
                    {
                        ((Method) cfm.ro).invoke(rru.bean, newValue);
                    }
                    else
                    {
                        ((Field) cfm.ro).set(rru.bean, newValue);
                    }
                }
                else    // use MethodHandle
                {
                    cfm.mh.invoke(newValue);            // set static field
                    // does not work ? cfm.mh.invokeWithArguments( (Object []) new Object[]{newValue});    // no argument for a static setter
                }
            }
            else  // instance
            {
                if (useReflectInvoke)      // use java.lang.reflect object
                {
                    if (cfm.bOtherKind)     // this is a Method object instead!
                    {
                        ((Method) cfm.ro).invoke(rru.bean, newValue);
                    }
                    else
                    {
                        ((Field) cfm.ro).set(rru.bean, newValue);
                    }
                }
                else    // use MethodHandle
                {
                    cfm.mh.invoke(rru.bean, newValue); // supply bean to set field for
                    // does not work ? cfm.mh.invokeWithArguments( (Object []) new Object[]{rru.bean, newValue});    // no argument for a static setter
                }
            }
        }
        catch (Throwable t)
        {
if (debugLevel>3) System.err.println("// // :( -> RexxReflectJava7.invokeFieldSet(), t=\""+t+"\"");
           rru.throwable=t;
           return;              // not successful :(
        }
if (debugLevel>2 || bDebugSuccess) System.err.println("// // :) -> RexxReflectJava7.invokeFieldSet(): bean=\""+rru.bean+"\",["
                                                +rru.choice+"] invocation using ["
                                                +(useReflectInvoke ? "java.lang.reflect.Field" : "MethodHandle")
                                                +"] successful"
                                                +"newValue=\""+newValue+"\""
                                                +(newValue==null ? "" : " \".toString()=\""+newValue.toString()+"\"")
                                             );

        rru.success=true;        // we could successfully carry out the get operation!
    }



// SECTION: METHOD ================================================================================================

        // ========================================================================================================
    /**
     * Reflect a method. This method consults the cache, if {@link #useCaching} is set and tries to invoke every
     * cached method using either {@link #invokeMethod}.
     * <br>
     * If no cache is found all methods of the bean are reflected and {@link #processField} will
     * get invoked. The reflection process will take all ancestors and interfaces into account.
     *
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     * @throws BSFException if any of the invoked methods throw an error a {@link BSFException} gets thrown that wraps it
     */
    void reflectMethod(RexxReflectUtil rru) throws BSFException
    {

if (bDebug)
{
    // to be removed, blank line to ease start of reflectMethod()
    System.out.println("\n... reflectMethod(): arrived, bean="+rru.bean+", seeking method \""+rru.keyMemberName+"\""
                       +"\n\trexxArgs=\""+Arrays.deepToString(rru.rexxArgs)+"\""
                       +"\n\tfuncArgs=\""+Arrays.deepToString(rru.funcArgs)+"\""
    );
    System.out.println("\tType of each funcArgs element:");
    for (int i=0; i<rru.funcArgs.length;i++)
    {
        System.out.println("\t\tfuncArgs["+i+"]: \""+rru.funcArgs[i]+"\" .getClass().toString() -> <"+
                   ( rru.funcArgs[i]==null ? null : rru.funcArgs[i].getClass().toString() )
                  +">");
    }
    System.out.println("*** los geht's... ***");
}

if (bDebug)
System.out.println("... reflectMethod(): arrived, cachedMethods.containsKey("+rru.beanClz+"): \""+cachedMethods.containsKey(rru.beanClz)+"\"");

        // let us first check, whether we have a usable cache entry already
        if (useCaching && cachedMethods.containsKey(rru.beanClz))
        {
                // fetch ArrayList of method handles for this method name
            ArrayList<CachedFieldOrMethod> al=cachedMethods.get(rru.beanClz).get(rru.keyMemberName);

            if (al!=null)       // we found a list, check it!
            {
                for (CachedFieldOrMethod cfm : al)
                {
if (bDebug) System.out.println("... ... reflectMethod(): from cache, trying cfm=\""+cfm+"\"");
                    switch (cfm.mhk)    // only invoke, if MethodHandle is of the appropriate type
                    {
                        case METHOD:
                        case STATIC_METHOD:

if (bDebug) System.out.println("... ... reflectMethod(): from cache, using \"invokeMethod(cfm,rru)\" ..");
                             invokeMethod(cfm,rru);
                            break;

                            // camouflaging Java fields as ooRexx attributes
                        case GETTER:
                        case STATIC_GETTER:
                            if (rru.bFieldGet)
                            {
                                invokeFieldGet(cfm,rru);
                            }
                            break;

                            // camouflaging Java fields as ooRexx attributes
                        case SETTER:
                        case STATIC_SETTER:
                            if (!rru.bFieldGet)
                            {
                                invokeFieldSet(cfm,rru);
                            }
                            break;

                        default:
                            continue;
                    }

                    if (rru.success)     // found, return
                    {
if (debugLevel>1)
System.err.println("... reflectMethod(): WOW, found cached Method/MethodHandle (method name=\""+rru.keyMemberName+"\") and could execute it appropriately! : ["+cfm.mh.toString()+"], result=["+rru.result+"]"
                           +(rru.result==null ? "" : " .toString()=\""+rru.result.toString()+"\"")
                         );
                        return; //  rru.result;
                    }
                }
            }
        }

        boolean bStartLevel=true;       // we are about to start out with reflection
        Class   tmpClz=rru.beanClz;     // start out with the current beanClz
        boolean bLastTwoRuns4Clz=false; // if bean is a class object and we are now doing the special last
                                        // two rounds (deviating to Class->Object) such that we also accept
                                        // instance methods for the class object

if (bDebug) System.out.println("... reflectMethod(): ---> no appropriate cache found, starting reflection with rru.beanClz=\""+tmpClz+"\"");

        while (rru.success==false)   // as long as we have not found a matching Method object, keep processing
        {
// System.err.println("---> rrj7.reflectMethod():"+"tmpClz=["+tmpClz.getName()+"@"+tmpClz.hashCode()+"]"+", .toString()=["+tmpClz.toString()+"]"+", .isPrimitive=["+tmpClz.isPrimitive()+"]"+", .isInterface=["+tmpClz.isInterface()+"]"+", .getSuperclass=["+tmpClz.getSuperclass()+"]");

            Method tmpMethods[]=tmpClz.getDeclaredMethods();
            for (int i=0; i<tmpMethods.length; i++)
            {
                Method method=tmpMethods[i];
                int   methodMods=method.getModifiers();

                if ((methodMods & Modifier.PUBLIC)==0)  // not PUBLIC: ignore
                {
                    continue;
                }

                // but allow if bean is a class and tmpClz is in its last two rounds (Class->Object)
                if ((rru.bBeanIsClass || rru.bStatic ) && ( !bLastTwoRuns4Clz && ((methodMods & Modifier.STATIC)==0)) ) // looking for static, not STATIC, ignore
                {
                    continue;
                }

                processMethod(method, rru);
                if (rru.success)         // succesfully processed, return
                {
                    return;
                }
            }

// TODO: 2018-11-24, rgf: default methods only resolved from interface class, if not implemented in any superclass
//                        e.g. cf. <https://www.journaldev.com/2752/java-8-interface-changes-static-method-default-method>
//

            if (rru.rajo.javaRuntimeVersion>=8)     // starting with Java 8 we have to consider default interface methods
            {
                for (Class interf : tmpClz.getInterfaces()) // iterate over interfaces implemented by this class
                {
                    getAndProcessMethodFromInterface(interf, rru); // Interface members are always PUBLIC, fields are always STATIC
                    if (rru.success)         // succesfully processed, return result
                    {
                        return;
                    }
                }
            }


            // special case: bean is a class, so if we arrive at Object, detour to look up Class first and then Object,
            // look for instance methods in these two classes
            if (rru.bBeanIsClass && tmpClz==Object.class && !bLastTwoRuns4Clz)
            {
                tmpClz=Class.class;         // deviate to process Class next
                bLastTwoRuns4Clz=true;      // now accept instance methods as well
            }

// TODO: rgf, 2018-11-09: also add to reflectField() and reflectConstructor()! Interface has no Superclass, hence treat as primitive?
            // 2018-11-03, rgf: special handling for primitive classes needed, because a primitive class object
            //                  does not have a superclass, so no instance methods can be invoked, which we want
            //                  for ooRexx users; hence manually setting the superclass to the java.lang.Object class
            // 2018-11-09: rgf: also handle interface classes as getSuperclass() returns null for them as well
            else
            {
                tmpClz=(tmpClz.isPrimitive() || tmpClz.isInterface() ? Object.class : tmpClz.getSuperclass());  // try next superclass
                if (tmpClz==null)               // no more superclass, we are done
                {
                    break;
                }
            }

        }

        if (rru.bRecursing)     // we are already in try mode, return
        {
            return;
        }

            // if not strict and we have 0 or 1 arguments, try other method names
        if (!rru.bStrict && rru.funcArgs.length<2) // not a candidate for the next guesses/attempts
        {
            // ============================= try a few other helpers ======================================
if (bDebug)
System.err.println("... reflectMethod(): no method ["+rru.keyMemberName+"] found, still trying hard ;) ...");

            /* ooRexx-semantics: if method is named MAKEARRAY or SUPPLIER and no argument is supplied,
                                 and if the Java object implements one of ne of the interfaces
                                 java.util.Enumeration, java.lang.Iterable, java.util.Iterator,
                                 java.util.Collection, java.util.Map, then iterate over it and
                                 create the appropriate Rexx array or supplier object and return it */

                // if no arguments and MAKEARRAY or SUPPLIER message
            if ( rru.funcArgs.length==0 && "MAKEARRAY SUPPLIER".indexOf(rru.keyMemberName)>=0)    //
            {
                rru.processMakearrayOrSupplier();   // carry out the work
                if (rru.success) return;             // o.k., we are done
            }

            /* ooRexx-semantics: camouflage access to Java fields as ooRexx attributes: XXX() may be a field get access,
                                 XXX(value) may be a field get access */


            //          - assume fields, XXX is memberName
            //              - setter: setXXX, if one argument, cf. RexxAndJava, line # 3458
            RexxReflectUtil tmpRru=rru.copy();  // create a copy of the current RexxReflectUtil
            tmpRru.bOtherKind = true;   // if found, then store MethodHandle in cachedFields instead of cachedMethods
                                        // to speed up lookup on the next invocation
            tmpRru.bRecursing = true;   // inhibit endless recursions

            // the following mimickries ooRexx attributes
            //          - assume fields, XXX is memberName
            //              - setter: setXXX, if one argument, cf. RexxAndJava, line # 3458
            if (rru.funcArgs.length==1)     // maybe a setXXX ?
            {

                tmpRru.bFieldGet        = false;
                tmpRru.memberName       = rru.keyMemberName;  // field name
if (bDebug)
System.err.println("... reflectMethod(): trying field GET, bFieldGet=[false], taking method name as field name ["+tmpRru.memberName+"] ...");

                reflectField(tmpRru);
                if (tmpRru.success)          // found & processed, return
                {
                    rru.success=true;
                    rru.result=tmpRru.result;
                    return;
                };

    // TODO: should we still return prematurely?       if (rru.keyMemberName.startsWith("SET")) return;    // we assume: already a setter method, hence we give up

                tmpRru.memberName       = "SET"+rru.keyMemberName;  // try setter method
                reflectMethod(tmpRru);
            }

            else    // try getXXX, isXXX, hasXXX in this order
            //          - assume fields, XXX is memberName
            //              - getter:   cf. RexxAndJava, line # 4175
            //                  - getXXX, if no argument
            //                  - isXXX | hasXXX, if no argument (Booleans may have "is" or "has" instead of "get" as prefix to getter name)
            {
                // method is actually for an ooRexx attribute message?
                tmpRru.bFieldGet        = true;
                tmpRru.memberName       = rru.keyMemberName;  // field name

if (bDebug)
System.err.println("... reflectMethod(): trying field GET, bFieldGet=[true], taking method name as field name ["+tmpRru.memberName+"] ...");

                reflectField(tmpRru);
                if (tmpRru.success)          // found & processed, return
                {
                    rru.success=true;
                    rru.result=tmpRru.result;
                    return;
                };


                tmpRru.bOtherKind = false;      // if found, then store MethodHandle in cachedMethods

    /*
    TODO: should we still return prematurely?

                if (rru.keyMemberName.startsWith("GET") ||              // we assume: already a getter method, hence we give up
                    rru.keyMemberName.startsWith("IS" ) ||
                    rru.keyMemberName.startsWith("HAS") )  return;
    */


                tmpRru.memberName       = "GET"+rru.keyMemberName;  // try getter method

if (bDebug)
System.err.println("... reflectMethod(): trying method, using method name ["+tmpRru.memberName+"] ...");

                reflectMethod(tmpRru);
                if (tmpRru.success)          // found & processed, return
                {
                    rru.success=true;
                    rru.result=tmpRru.result;
                    return;
                };

                tmpRru.memberName       = "IS" +rru.keyMemberName;  // try another getter method for Booleans

if (bDebug)
System.err.println("... reflectMethod(): trying method, using method name ["+tmpRru.memberName+"] ...");
                reflectMethod(tmpRru);
                if (tmpRru.success)          // found & processed, return
                {
                    rru.success=true;
                    rru.result=tmpRru.result;
                    return;
                };

                tmpRru.memberName       = "HAS"+rru.keyMemberName;  // try another getter method for Booleans
    if (bDebug)
    {
        System.err.println("... reflectMethod(): ---> GETTER: reflectField("+tmpRru.memberName+") ...");
        System.err.println("                                  using tmpRru: "+tmpRru.debugString());
    }

if (bDebug)
System.err.println("... reflectMethod(): trying method, using method name ["+tmpRru.memberName+"] ...");

                reflectMethod(tmpRru);
            }

            if (tmpRru.success)
            {
                rru.success=tmpRru.success;
                rru.result=tmpRru.result;
    if (bDebug) System.err.println("... reflectMethod(): ---> bFound=true, rru.result=["+rru.result+"]");
                return;
            }
        }

        // throw BSFException method not found, supply all information with it as in earlier versions of BSF4ooRexx
        rru.throwNotFoundBSFException();
    }


        // ========================================================================================================
        // process Interface class as starting with Java 8 there may be default interface methods
    /**
     * This method will process all default methods of an interface and {@link #processMethod} will
     * get invoked. The reflection process will take all interface ancestors into account.
     *
     * @param tmpClz the interface class object to process
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     */
    void getAndProcessMethodFromInterface(Class tmpClz, RexxReflectUtil rru)
    {
        Object result;

if (bDebug)
System.err.println("\\\\// RexxReflectJava7.getAndProcessMethodFromInterface: tmpClz=["+tmpClz.toString()+"]");
        for (Method tmpMethod : tmpClz.getDeclaredMethods())
        {
            processMethod(tmpMethod, rru);
            if (rru.success)         // succesfully processed, return result
            {
                return;
            }
        }

        // not yet found, process super Interfaces recursively
        for (Class superInterface : tmpClz.getInterfaces())
        {
            getAndProcessMethodFromInterface(superInterface, rru);
            if (rru.success)         // succesfully processed, return result
            {
                return; // result;
            }
        }
    }


        // ========================================================================================================
        // Check name and carry out invocation
    /** If a {@link java.lang.reflect.Method} is public and the Rexx arguments can be coerced to its parameter types
     *  the method becomes eligible. If a {@link MethodHandle} can be created for it successfully and the invocation
     *  succeeds without an error, then a cache entry will be created in {@link #cachedMethods}.
     *
     * @param tmpMethod the {@link java.lang.reflect.Method} object to work on
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     */
    void processMethod (Method tmpMethod, RexxReflectUtil rru)
    {
        String methodName=tmpMethod.getName();

if (bDebug)
System.err.println("// // // RexxReflectJava7.processMethod(), ARRIVED: -> \""+rru.choice+"\", tmpMethod=\""+tmpMethod.toString()+"\": rru.memberName=\""+rru.memberName+"\" in object=[rru.rexxArgs[1]=\""+rru.rexxArgs[1]+"\"/rru.bean=\""+rru.bean+"\"]");

        if (rru.bStrict)        // strict, then use name as is!
        {
            if (!methodName.equals(rru.memberName))
            {
                return;
            }
        }
        else if (!methodName.equalsIgnoreCase(rru.memberName))
        {
            return;
        }

        Class<?> parameterTypes[] = tmpMethod.getParameterTypes();

        // check number of parameterTypes matches number of Rexx arguments
        if (parameterTypes.length!=rru.funcArgs.length) // Method not a candidate for this invocation
        {
if (bDebug)
System.err.println("// // // RexxReflectJava7.processMethod(), parameterTypes.length="+parameterTypes.length
                                    +"!=funcArgs.length="+rru.funcArgs.length+", not a candidate returning");
            return;
        }

            // coerce arguments?
        Object [] coercedArgs= ( parameterTypes.length==0 ? rru.funcArgs
//                                                          : ( rru.bStrict ? rru.funcArgs // if bStrict, do not coerce arguments!
                                                                          : rru.rajo.coerceArgs(rru.funcArgs,parameterTypes)
//                                                                          )
                          );

if (bDebug)
{
    String tmpStrCoercedArgs= (coercedArgs==null ? null : Arrays.deepToString(coercedArgs)+".getClass().toString()="+coercedArgs.getClass().toString() );
    System.err.println("// // // RexxReflectJava7.processMethod(), coercedArgs=\""+tmpStrCoercedArgs+"\""
                                         +",\n\t\t parameterTypes=\""+Arrays.deepToString(parameterTypes)+"\" |.getClass().toString()=\""+parameterTypes.getClass().toString()+"\""
                                         +":,\n\t\t rru.funcArgs=\""+Arrays.deepToString(rru.funcArgs)+"\" | .getClass().toString()=\""+rru.funcArgs.getClass().toString()+"\""
                                         );
}

        if (coercedArgs==null)      // could not be coerced successfully, coerceArgs() returned null!
        {
if (bDebug)
System.err.println("// // :( RexxReflectJava7.processMethod(), coercedArgs==null :( :( :(; coercion was not successful!");
            return;
        }

if (debugLevel>0)       // Rexx arguments could be coerced successfully, show candidate tmpMethod
{
    System.err.println("// // // RexxReflectJava7.processMethod(), FOUND candidate method=\""
                                             +tmpMethod.getDeclaringClass().toString()+"\".\""+tmpMethod.toString()+ "\"...");
}

        // check whether artificial IS / HAS getter method has type boolean.class or Boolean.class
        if (rru.bOtherKind)
        {
                // a getter
            if ( parameterTypes.length==0 && (rru.keyMemberName.startsWith("IS") || rru.keyMemberName.startsWith("HAS")) )
            {
                Class tmpReturnType=tmpMethod.getReturnType();

System.err.println("// // T? RexxReflectJava7.processMethod(), IS or HAS getter (\""+rru.keyMemberName+"\"), return type boolean, or Boolean?: \""+tmpReturnType.toString()+"\".");
                if (! ( (tmpReturnType==boolean.class) || (tmpReturnType==Boolean.class)) )
                {
System.err.println("// // :( RexxReflectJava7.processMethod(), IS or HAS getter (\""+rru.keyMemberName+"\") not of type boolean, nor Boolean, instead: \""+tmpReturnType+"\", not eligible, returning");
                    return;     // prefix IS and HAS mandate a Boolean or boolean return type
                }
            }

        }


        int   methodMods=tmpMethod.getModifiers();    // get Method's modifiers
        boolean bStaticMethod= ((methodMods & Modifier.STATIC) !=0);    // determine whether static or instance method

        // do not invoke static interface methods while going up the inheritance tree (like "javac" does it);
        // we could also inhibit this in  getAndProcessMethodFromInterface() instead;
        // note: Rexx can invoke static interface methods by referring to the Interface class
        Class <?> tmpDeclaringClass=tmpMethod.getDeclaringClass();      // get declaring class object
        if (bStaticMethod && rru.beanClz!=tmpDeclaringClass && tmpDeclaringClass.isInterface())
        {
            return;
        }

// Java 1.7/7, yielding to an error in the end (no alternative found)
// // :( RexxReflectJava7.processMethod(), thisLookup.findVirtual(...) caused "java.lang.IllegalAccessException: symbolic reference class is not public: class java.util.AbstractList$Itr, from org.rexxla.bsf.engines.rexx.RexxReflectJava7", hence Method "class java.util.AbstractList$Itr"."public boolean java.util.AbstractList$Itr.hasNext()" not a candidate, returning
// // :( RexxReflectJava7.processMethod(), thisLookup.findVirtual(...) caused "java.lang.IllegalAccessException: symbolic reference class is not public: class java.util.Collections$UnmodifiableCollection, from org.rexxla.bsf.engines.rexx.RexxReflectJava7", hence Method "class java.util.Collections$UnmodifiableCollection"."public int java.util.Collections$UnmodifiableCollection.size()" not a candidate, returning
// // :( RexxReflectJava7.processMethod(), thisLookup.findVirtual(...) caused "java.lang.IllegalAccessException: symbolic reference class is not public: class java.util.Collections$SynchronizedCollection, from org.rexxla.bsf.engines.rexx.RexxReflectJava7", hence Method "class java.util.Collections$SynchronizedCollection"."public java.util.Iterator java.util.Collections$SynchronizedCollection.iterator()" not a candidate, returning

        MethodHandle mh=null;
        // find mathich MethodHandle
        if (useUnreflect)      // use (simplest) the Lookup.unreflect(...) version
        {
            try {
                mh=thisLookup.unreflect(tmpMethod);
            }
            catch (Throwable t)
            {
if (debugLevel>0)
System.err.println("// // :( RexxReflectJava7.processMethod(), thisLookup=\""+thisLookup+"\".unreflect(tmpMethod) caused \""+t
                                         +"\", hence Method \""+tmpMethod.getDeclaringClass().toString()+"\".\""+tmpMethod.toString()+ "\" not a candidate, returning");
                rru.throwable=t;
                return;
            }
        }
        else    // use Lookup.findXXX(...) versions
        {
            String strLookupName=null;
            try     // access via MethodHandle with the rights of rru.firstExportedClz
            {
                Class <?> returnType=tmpMethod.getReturnType();
                MethodType mt = MethodType.methodType(returnType, parameterTypes);

if (bDebug) System.err.println("// // // RexxReflectJava7.processMethod(), MethodType for Method ["+tmpMethod.toString()+"]: \""+mt+"\"");
                if (bStaticMethod)      // a static method in hand
                {
                    strLookupName="findStatic";
                    mh=thisLookup.findStatic(tmpDeclaringClass, methodName, mt);
                }
                else    // instance method
                {
                    strLookupName="findVirtual";
                    mh=thisLookup.findVirtual(tmpDeclaringClass, methodName, mt);
                }
            }
            catch (Throwable t)
            {
if (debugLevel>0)
System.err.println("// // :( RexxReflectJava7.processMethod(), thisLookup=\""+thisLookup+"\"."+strLookupName+"(...) caused \""+t
                   +"\", hence Method \""+tmpMethod.getDeclaringClass().toString()+"\".\""+tmpMethod.toString()+ "\" not a candidate, returning");

                rru.throwable=t;
                return;     // not an eligible Method
            }
        }
        mh=mh.asFixedArity(); // rgf, 2018-06-11 as per Peter Levart's advice, cf. e-Mails of 2018-06-10/11 in <mlvm-dev@openjdk.java.net>


if (debugLevel>0)
System.err.println("// // F? RexxReflectJava7.processMethod(), FOUND? try invoking - tmpMethod=["+tmpMethod.toString()+"], mh=["+mh+"]");

            // invoke the method
        if (useReflectInvoke)     // use java.lang.reflect.Method to invoke; same arguments for static and instance invocations
        {
            try
            {
                rru.result=tmpMethod.invoke(rru.bean,coercedArgs);      // get and return Method value
            }

// Java 1.6/6 only so far:
        // on Java 6, e.g. for "class java.util.AbstractList$Itr"."public boolean java.util.AbstractList$Itr.hasNext()",
        //                     "class java.util.Collections$UnmodifiableCollection"."public int java.util.Collections$UnmodifiableCollection.size()"
        //                     "class java.util.Collections$SynchronizedCollection"."public java.util.Iterator java.util.Collections$SynchronizedCollection.iterator()"
        //

/*
            catch (IllegalAccessException iae)
            {
    if (debugLevel>0)
    System.err.println("// // :( RexxReflectJava7.processMethod(), tmpMethod.invoke(...) for bean=\""+rru.bean+"\" Method \""
                        +tmpMethod.getDeclaringClass().toString()+"\".\""+tmpMethod.toString()+"\""
                        +" caused \""+iae+"\", re-trying after doing a \"setAccessible(true)\"...");
                try
                {
                    tmpMethod.setAccessible(true);
                    rru.result=tmpMethod.invoke(rru.bean,coercedArgs);      // get and return Method value
                }

                catch (Throwable t2)
                {
    if (debugLevel>0)
    System.err.println("// // :( RexxReflectJava7.processMethod(), tmpMethod.invoke(...) for bean=\""+rru.bean+"\" Method \""
                            +tmpMethod.getDeclaringClass().toString()+"\".\""+tmpMethod.toString()+"\""
                            +" caused \""+t2+"\", not a candidate, giving up, returning ...");
                    rru.throwable=t2;       // save Throwable to allow passing it back as a cause
                    return;
                }
            }
*/

            catch (Throwable t1)
            {
if (debugLevel>0)
    System.err.println("// // :( RexxReflectJava7.processMethod(), tmpMethod.invoke(...) for bean=\""+rru.bean+"\" Method \""
                        +tmpMethod.getDeclaringClass().toString()+"\".\""+tmpMethod.toString()+"\""
                        +" caused \""+t1+"\", not a candidate, returning");

                rru.throwable=t1;   // save Throwable to allow passing it back as a cause
                return;
            }


        }
        else    // use MethodHandle to invoke, need to distinguish between static and instance method invocation
        {
            if (bStaticMethod)     // a static method in hand
            {
                    try {
// System.err.println("// // // // :) RexxReflectJava7.processMethod(): STATIC, mh.invokeWithArguments(...), about to invoke, coercedArgs=\""+Arrays.deepToString(coercedArgs)+"\"");
                        rru.result=mh.invokeWithArguments( (Object []) coercedArgs); // ORIGINAL
                    }
                    catch (Throwable t)
                    {
if (debugLevel>0)
System.err.println("// // :( RexxReflectJava7.processMethod(): STATIC, mh.invokeWithArguments(...) caused Throwable \""+t+"\", SHOULD NOT OCCUR?");
                        rru.throwable=t;
                        return;
                    }

if (bDebug) System.err.println("// // // RexxReflectJava7.processMethod(), STATIC, mh.invokeWithArguments(...), rru.result=\""+rru.result+"\""+(rru.result==null ? "" : ".getClass()=\""+rru.result.getClass()+"\""));

            }
            else    // instance Method
            {
// if (bDebug)
// System.err.println("// // // RexxReflectJava7.processMethod(): INSTANCE, mh.bindTo(\""+rru.bean+"\"/\""+rru.bean.getClass().toString()+"\").invokeWithArguments(...),"
if (bDebug)
System.err.println("// // // RexxReflectJava7.processMethod(): INSTANCE, mh.bindTo(\""+rru.bean+"\").invokeWithArguments(...),"
                               +"\n\t-> coercedArgs=\""+Arrays.deepToString(coercedArgs)+"\""
                               +"\n\t-> Method     =\""+tmpMethod.toString()+"\""
                               +"\n\t-> mh         =\""+mh.toString()+"\""
                               );
                    try {
                        rru.result=mh.bindTo(rru.bean).invokeWithArguments( (Object []) coercedArgs); // bind to bean, invoke method ORIGINAL
                        // does not work, causes "WrongMethodTypeException": rru.result=mh.invokeWithArguments( (Object []) new Object [] {rru.bean, coercedArgs}); // bind to bean, invoke method
                    }
                    catch (Throwable t)
                    {
if (debugLevel>0)
System.err.println("// // :( RexxReflectJava7.processMethod(): INSTANCE, mh.invokeWithArguments(...) caused Throwable: \""+t+"\"");
// System.err.println("// // :( RexxReflectJava7.processMethod(): .... t.getCause()=\""+t.getCause()+"\" -> t.getCause().toString()=\""+t.getCause().toString()+"\"");
                        rru.throwable=t;
                        return;
                    }
if (bDebug) System.err.println("// // // RexxReflectJava7.processMethod(), INSTANCE, mh.invokeWithArguments(...), rru.result=\""+rru.result+"\""+(rru.result==null ? "" : ".getClass()=\""+rru.result.getClass()+"\""));
            }
        }

        rru.success=true;    // if we arrive here, invocation was successful, so indicate that fact

if (debugLevel>2 || bDebugSuccess)
System.err.println("// // :) -> RexxReflectJava7.processMethod(): bean=\""+rru.bean+"\",["
                                                +rru.choice+"] invocation using ["
                                                +(useReflectInvoke ? "java.lang.reflect.Method" : "MethodHandle")
                                                +"]" +" ["+tmpMethod.toString()+"]"
                                                +" SUCCESSFUL, rru.result=\""+rru.result+"\"");


        if (useCaching)    // add Method object and corresponding MethodHandle to cache
        {
            if (bStaticMethod)
            {
                    // a plain method access
                if (!rru.bOtherKind)
                {
                    add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpMethod, rru.bOtherKind, mh, MH_Kind.STATIC_METHOD, parameterTypes), cachedMethods, rru);
                }
                else    // use Field set and get instead (ooRexx attribute semantics)
                {
                    if (parameterTypes.length==0)   // a static getter
                    {
                        add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpMethod, rru.bOtherKind, mh, MH_Kind.STATIC_GETTER, parameterTypes), cachedFields, rru);
                    }
                    else    // a static setter
                    {
                        add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpMethod, rru.bOtherKind, mh, MH_Kind.STATIC_SETTER, parameterTypes), cachedFields, rru);
                    }
                }
            }
            else
            {
                    // a plain method access
                if (!rru.bOtherKind)
                {
                    add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpMethod, rru.bOtherKind, mh, MH_Kind.METHOD, parameterTypes), cachedMethods, rru);
                }
                else    // use Field set and get instead (ooRexx attribute semantics)
                {
                    if (parameterTypes.length==0)   // a getter
                    {
                        add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpMethod, rru.bOtherKind, mh, MH_Kind.GETTER, parameterTypes), cachedFields, rru);
                    }
                    else
                    {
                        add2cachedFieldsOrMethods(new CachedFieldOrMethod(tmpMethod, rru.bOtherKind, mh, MH_Kind.SETTER, parameterTypes), cachedFields, rru);
                    }
                }
            }
        }
    }


        // ========================================================================================================
        // Check name and carry out GET or SET operation, if GET, return value
    /** The supplied cache entry will be used to carry out the method invocation.
     *
     * @param cfm   the cache entry to use for invocation
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     * @throws BSFException if any of the invoked methods throw an error a {@link BSFException} gets thrown that wraps it
     */
    void invokeMethod (CachedFieldOrMethod cfm, RexxReflectUtil rru) throws BSFException
    {
if (bDebug) System.err.println("...invokeMethod(): cfm=["+cfm+"] | rru.choice=["+rru.choice+"] | rru.bean=["+rru.bean+"], rru.keyMemberName=["+rru.keyMemberName+"]");

        Class<?> [] parameterTypes=cfm.parameterTypes;  // fetch parameterTypes from cache entry
        if (parameterTypes.length!=rru.funcArgs.length) // arguments do not match parameter count?
        {
            return;
        }

            // coerce arguments?
        Object [] coercedArgs;
        if (parameterTypes.length==0)    // not expecting an argument or strict
        {
            coercedArgs=rru.funcArgs;       // empty Object array
        }
        else   // try to coerce to given parameterTypes
        {
            coercedArgs=rru.rajo.coerceArgs(rru.funcArgs,parameterTypes);
            if (coercedArgs==null)      // could not be coerced successfully, coerceArgs() returned null!
            {
                return;
            }
        }

        try     // now invoke
        {
            if (useReflectInvoke)       // use java.lang.reflect object
            {
                if (cfm.bOtherKind)         // this is a Field object instead!
                {
                    if (parameterTypes.length==0)   // a getter method
                    {
if (bDebug) System.err.println("...invokeMethod(): cfm.ro=\""+cfm.ro+"\".get(rru.bean=\""+rru.bean+"\") ...");
                        rru.result=((Field) cfm.ro).get(rru.bean);
                    }
                    else
                    {
if (bDebug) System.err.println("...invokeMethod(): cfm.ro=\""+cfm.ro+"\".set(rru.bean=\""+rru.bean+"\", \""+coercedArgs[0]+"\") ...");
                        ((Field) cfm.ro).set(rru.bean,coercedArgs[0]);
                    }
                }
                else
                {
if (bDebug) System.err.println("...invokeMethod(): cfm.ro=\""+cfm.ro+"\".set(rru.bean=\""+rru.bean+"\", \""+coercedArgs+"\") ...");
                    rru.result=((Method) cfm.ro).invoke(rru.bean, (Object []) coercedArgs);
                }
if (bDebug) System.err.println("...invokeMethod(): cfm.ro=\""+cfm.ro+"\", rru.result=\""+rru.result+"\"");

                rru.success=true;

if (debugLevel>2) System.err.println("// // :) -> RexxReflectJava7.invokeMethod(): bean=\""+rru.bean+"\",["
                                                +rru.choice+"] invocation using ["
                                                +(useReflectInvoke ? "java.lang.reflect.Method" : "MethodHandle")
                                                +"] successful, rru.result=\""+rru.result+"\"");

                return;
            }

            if (cfm.mhk==MH_Kind.STATIC_METHOD)
            {
                if (coercedArgs.length==0)
                {
                    rru.result=cfm.mh.invoke();;
                }
                else
                {
                    rru.result=cfm.mh.invokeWithArguments( (Object []) coercedArgs);
                }
            }
            else
            {
                if (coercedArgs.length==0)
                {
                    rru.result=cfm.mh.bindTo(rru.bean).invoke();  // bind to bean, invoke method
                    // does not work ? rru.result=cfm.mh.invokeWithArguments( (Object []) new Object [] {rru.bean}); // bind to bean, invoke method
                }
                else
                {
                    rru.result=cfm.mh.bindTo(rru.bean).invokeWithArguments( (Object []) coercedArgs); // bind to bean, invoke method

                    // does not work ? rru.result=cfm.mh.invokeWithArguments( (Object []) new Object [] {rru.bean, coercedArgs}); // bind to bean, invoke method
                }
            }
        }
        catch (Throwable t) // invocation threw exception
        {
if (debugLevel>3)  System.err.println("// // // :( -> RexxReflectJava7.invokeMethod(): Method=\""+cfm.ro+"\", caused Throwable: \""+t+"\"");
// t.printStackTrace();
            rru.throwable=t;
            return;
        }
        rru.success=true;        // we could successfully carry out the get operation!

if (bDebug) System.err.println("...mhMethodGetInvoke(): rru.memberName=\""+rru.memberName+"\"(...),  rru.result=\""+rru.result+"\"");
if (debugLevel>2 || bDebugSuccess) System.err.println("// // :) -> RexxReflectJava7.invokeMethod(): bean=\""+rru.bean+"\",["
                                                +rru.choice+"] invocation using ["
                                                +(useReflectInvoke ? "java.lang.reflect.Method" : "MethodHandle")
                                                +"] successful, rru.result=\""+rru.result+"\"");

    }





    // ========================================================================================================
    // add MethodHandle for Field or Method to cache
    /** Adds an entry to the cache.
     *
     * @param cfm the cache entry
     * @param cachedFieldsOrMethods the cache to use
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     *
     */
    void add2cachedFieldsOrMethods(CachedFieldOrMethod cfm,
                                          ConcurrentHashMap<Class<?>,ConcurrentHashMap<String,ArrayList<CachedFieldOrMethod>>>  cachedFieldsOrMethods,
                                          RexxReflectUtil rru)
    {
if (bDebug)
System.err.println("... add2cachedFieldsOrMethods(): SUCCESS rru.memberName=["+rru.memberName+"] -> rru.keyMemberName=["+rru.keyMemberName
                                  +"], rru.choice=["+rru.choice
                                  +"] --> cfm.ro.getClass()=\""+cfm.ro.getClass().toString()
                                  +"\", cfm=["+cfm+"]");

        ConcurrentHashMap<String, ArrayList<CachedFieldOrMethod> > str2al;
        ArrayList<CachedFieldOrMethod>                             alOfCfm;

        if (cachedFieldsOrMethods.containsKey(rru.beanClz))
        {
            str2al=cachedFieldsOrMethods.get(rru.beanClz);       // get HashMap for this class
            alOfCfm=str2al.get(rru.keyMemberName);           // get ArrayList for this memberName

            if (alOfCfm==null)                          // if no entry, create new one
            {
                alOfCfm=new ArrayList();
                str2al.put(rru.keyMemberName,alOfCfm);  // add ArrayList for this new memberName
            }
            alOfCfm.add(cfm);
            return;
        }

            // we need entries for everything
        alOfCfm=new ArrayList();                        // create and populate ArrayList
        alOfCfm.add(cfm);

        str2al =new ConcurrentHashMap();                // create and populate ConcurrentHashMap
        str2al.put(rru.keyMemberName,alOfCfm);
        cachedFieldsOrMethods.put(rru.beanClz,str2al);  // put new ConcurrentHashMap into cache
    }




// SECTION: CONSTRUCTOR ================================================================================================

        // ========================================================================================================
    /**
     * Reflect a method. This method consults the cache, if {@link #useCaching} is set and tries to invoke every
     * cached constructor using either {@link #invokeConstructor}.
     * <br>
     * If no cache is found all constructors of the bean are reflected and {@link #processConstructor} will
     * get invoked. The reflection process will take all ancestors and into account.
     *
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     *
     * @throws BSFException if any of the invoked methods throw an error a {@link BSFException} gets thrown that wraps it
     */
    void reflectConstructor(RexxReflectUtil rru) throws BSFException
    {
if (bDebug) System.out.println("... reflectConstructor(): arrived, cachedConstructors.containsKey("+rru.beanClz+"): \""+cachedConstructors.containsKey(rru.beanClz)+"\"");
        // let us first check, whether we have a usable cache entry already
        if (useCaching && cachedConstructors.containsKey(rru.beanClz))
        {
            ArrayList<CachedConstructor> al=cachedConstructors.get(rru.beanClz);

            if (al!=null)       // we found a list, check it!
            {
                for (CachedConstructor cc : al)
                {
if (bDebug) System.out.println("... reflectField(): checking from cache, cc=\""+cc+"\"");
                    if (cc.parameterTypes.length!=rru.funcArgs.length)  // skip, if not the same size
                    {
                        continue;
                    }

                    invokeConstructor(cc,rru);    // invoke the constructor MH
                    if (rru.success)     // found, return
                    {
if (debugLevel>1)
System.err.println("... reflectConstructor(): WOW, found cached Constructor/MethodHandle handle for class \""+rru.beanClz+"\" and could use it successfully: ["+cc.mh.toString()+"]");
                        return; //  rru.result;
                    }
                }
            }
        }

        boolean bStartLevel=true;   // we are about to start out with reflection
        Class   tmpClz=rru.beanClz; // start out with the current beanClz

        while (rru.success==false)   // as long as we have not found a matching Constructor object, keep processing
        {
            Constructor tmpConstructors[]=tmpClz.getDeclaredConstructors();
            for (int i=0; i<tmpConstructors.length; i++)
            {
                Constructor constructor=tmpConstructors[i];
                int   constructorMods=constructor.getModifiers();

                if ((constructorMods & Modifier.PUBLIC)==0)  // not PUBLIC: ignore
                {
                    continue;
                }

                processConstructor(constructor, rru);
                if (rru.success)         // succesfully processed, return
                {
                    return;
                }
            }

            tmpClz=tmpClz.getSuperclass();  // try next superclass
            if (tmpClz==null)               // we are done
            {
                break;
            }
        }

        // throw BSFException method not found, supply all information with it as in earlier versions of BSF4ooRexx
        rru.throwNotFoundBSFException();
    }


        // ========================================================================================================
        // Check name and carry out GET or SET operation, if GET, return value
    /** If a {@link java.lang.reflect.Constructor} is public and the Rexx arguments can be coerced to its parameter types
     *  the constructor becomes eligible. If a {@link MethodHandle} can be created for it successfully and the invocation
     *  succeeds without an error, then a cache entry will be created in {@link #cachedConstructors}.
     *
     * @param tmpConstructor the {@link java.lang.reflect.Constructor} object to work on
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     */
    void processConstructor (Constructor tmpConstructor, RexxReflectUtil rru)
    {
if (bDebug) System.err.println("// // // RexxReflectJava7.processConstructor(), ARRIVED: -> \""+rru.choice+"\", tmpConstructor=\""+tmpConstructor.toString()+"\": rru.memberName=\""+rru.memberName+"\" in object=[rru.rexxArgs[1]=\""+rru.rexxArgs[1]+"\"/\""+rru.bean+"\"]");

        Class<?> parameterTypes[] = tmpConstructor.getParameterTypes();

        // check number of parameterTypes matches number of Rexx arguments
        if (parameterTypes.length!=rru.funcArgs.length) // Constructor not a candidate for this invocation
        {
            return;
        }

            // coerce arguments?
        Object [] coercedArgs= ( parameterTypes.length==0 ? rru.funcArgs : rru.rajo.coerceArgs(rru.funcArgs,parameterTypes) );

        if (coercedArgs==null)      // could not be coerced successfully, coerceArgs() returned null!
        {
            return;
        }


if (debugLevel>0)       // Rexx arguments could be coerced successfully, show candidate tmpMethod
{
    System.err.println("// // // RexxReflectJava7.processConstructor(), FOUND candidate constructor=\""
                                             +tmpConstructor.getDeclaringClass().toString()+"\".\""+tmpConstructor.toString()+ "\"...");
}

        MethodHandle mh=null;

            // find the MethodHandle
        String strLookupName="";
        try {
            if (useUnreflect)      // use (simplest) the Lookup.unreflect(...) version
            {
                strLookupName="unreflect";
                mh=thisLookup.unreflectConstructor(tmpConstructor);
            }
            else
            {
                strLookupName="findConstructor";
                MethodType mt = MethodType.methodType(void.class, parameterTypes);
                mh=thisLookup.findConstructor(tmpConstructor.getDeclaringClass(), mt);
            }

            mh=mh.asFixedArity(); // rgf, 2018-06-11 as per Peter Levart's advice, cf. e-Mails of 2018-06-10/11 in <mlvm-dev@openjdk.java.net>
        }
        catch (Throwable t)
        {
if (debugLevel>0) System.err.println("// // :( RexxReflectJava7.processConstructor(), thisLookup=\""+thisLookup+"\"."+strLookupName+"(...) caused \""+t
                   +"\", hence Method \""+tmpConstructor.getDeclaringClass().toString()+"\".\""+tmpConstructor.toString()+ "\" not a candidate, returning");

             rru.throwable=t;
             return;        // not an eligible Constructor
        }

if (debugLevel>0) System.err.println("// // :) RexxReflectJava7.processConstructor(), tmpConstructor=["+tmpConstructor.toString()+"]");

            // invoke the constructor
        String tmpStr="";
        try {
            if (useReflectInvoke)     // use java.lang.reflect.Method to invoke; same arguments for static and instance invocations
            {
                tmpStr="tmpConstructor.newInstance(...)";
                rru.result=tmpConstructor.newInstance(coercedArgs);      // get and return Constructor value
            }
            else    // invoke via MethodHandle
            {
                tmpStr="mh.invokeWithArguments(...)";
                rru.result=mh.invokeWithArguments((Object []) coercedArgs); // invoke constructor
            }
        }

/*
        catch (IllegalAccessException iae)
        {
    if (debugLevel>0)
    System.err.println("// // :( RexxReflectJava7.processConstructor(), tmpConstructor.invoke(...) for bean's \""+rru.bean+"\" Constructor \""
                    +tmpConstructor.getDeclaringClass().toString()+"\".\""+tmpConstructor.toString()+"\""
                    +" caused \""+iae+"\", re-trying after doing a \"setAccessible(true)\"...");
            try
            {
                tmpConstructor.setAccessible(true);
                rru.result=tmpConstructor.newInstance(coercedArgs);      // get and return Constructor value
            }
            catch (Throwable t2)
            {
    if (debugLevel>0)
    System.err.println("// // :( RexxReflectJava7.processMethod(), tmpConstructor.invoke(...) for bean's \""+rru.bean+"\" Constructor \""
                        +tmpConstructor.getDeclaringClass().toString()+"\".\""+tmpConstructor.toString()+"\""
                        +" caused \""+t2+"\", not a candidate, giving up & returning");
                rru.throwable=t2;       // save Throwable to allow passing it back as a cause

                return;
            }
        }
*/

        catch (Throwable t)
        {
    if (debugLevel>0)
    {
        System.err.println("// // :( RexxReflectJava7.processConstructor(), "+tmpStr+" for Constructor "
                                 +"\""+tmpConstructor.getDeclaringClass().toString()+"\".\""+tmpConstructor.toString()+"\""
                                 +" caused \""+t+"\", hence not a candidate, tsk, ...");
        // t.printStackTrace();
    }
            rru.throwable=t;        // save Throwable to allow passing it back as a cause
            return;         // not an eligible constructor!
        }



        rru.success=true;    // if we arrive here, invocation was successful, so indicate that fact
if (debugLevel>2 || bDebugSuccess)
System.err.println("// // :) -> RexxReflectJava7.processConstructor(): bean=\""+rru.bean+"\",["
                                                +rru.choice+"] invocation using ["
                                                +(useReflectInvoke ? "java.lang.reflect.Constructor" : "MethodHandle")
                                                +"]" +" ["+tmpConstructor.toString()+"]"
                                                +" SUCCESSFUL, rru.result=\""+rru.result+"\"");


        if (useCaching)    // add Method object and corresponding MethodHandle to cache
        {
if (bDebug)
System.err.println("// // :) RexxReflectJava7.processConstructor(), SUCCESS adding constructor \""+tmpConstructor+"\" and its MH ["+mh+"] to cache!");

            ArrayList<CachedConstructor> alOfCC=cachedConstructors.get(rru.beanClz);           // get ArrayList for this memberName

            if (alOfCC==null)   // no ArrayList as of yet in cache
            {
                alOfCC=new ArrayList();
                cachedConstructors.put(rru.beanClz, alOfCC);    // add created ArrayList to cache
            }
            alOfCC.add(new CachedConstructor(tmpConstructor, mh, parameterTypes) );    // add MH to ArrayList
        }
    }


        // ========================================================================================================
        // Check name and carry out GET or SET operation, if GET, return value
    /** The supplied cache entry will be used to carry out the method invocation.
     *
     * @param cc   the cache entry to use for invocation
     * @param rru the {@link RexxReflectUtil} object that contains all the necessary information for the reflection
     */
    void invokeConstructor (CachedConstructor cc, RexxReflectUtil rru)
    {
if (bDebug) System.err.println("...invokeConstructor(): cc=["+cc+"] | rru.choice=["+rru.choice+"] | rru.beanClz=["+rru.beanClz+"], funcArgs="+Arrays.deepToString(rru.funcArgs));
        if (cc.parameterTypes.length!=rru.funcArgs.length) // arguments do not match parameter count?
        {
            return;
        }

            // coerce arguments
        Object [] coercedArgs;

        if (cc.parameterTypes.length!=rru.funcArgs.length)  // number of arguments do not match
        {
            return;
        }

        if (cc.parameterTypes.length==0)       // not expecting an argument (a static ConstructorHandle in hand then)
        {
            coercedArgs=rru.funcArgs;       // use rru.funcArgs
        }
        else
        {
            coercedArgs=rru.rajo.coerceArgs(rru.funcArgs,cc.parameterTypes);
            if (coercedArgs==null)      // could not be coerced successfully, coerceArgs() returned null!
            {
                return;
            }
        }

        try     // invoke
        {
            if (useReflectInvoke)     // use java.lang.reflect.Method to invoke; same arguments for static and instance invocations
            {
                rru.result=cc.ro.newInstance(coercedArgs);      // get and return Constructor value
            }
            else    // invoke via MethodHandle
            {
                rru.result=cc.mh.invokeWithArguments((Object []) coercedArgs); // invoke constructor
            }
        }
        catch (Throwable t)
        {
if (debugLevel>3) System.err.println("// // // :( -> RexxReflectJava7.invokeConstructor(): throwable: \""+t+"\"\n");
// t.printStackTrace();
           rru.throwable=t;
           return;              // not successful :(
        }
        rru.success=true;        // we could successfully carry out the get operation!

if (debugLevel>2 || bDebugSuccess) System.err.println("// // :) -> RexxReflectJava7.invokeConstructor(): bean=\""+rru.bean+"\",["
                                                +rru.choice+"] invocation using ["
                                                +(useReflectInvoke ? "java.lang.reflect.Constructor" : "MethodHandle")
                                                +"] successful, rru.result=\""+rru.result+"\"");
    }



        // ========================================================================================================
    static void main (String args[]) {
        System.err.println(RexxReflectJava7.class+": main() - REFLECT_METHOD="+REFLECT_METHOD);

        ReflectionType rt=REFLECT_FIELD;

        System.err.println(RexxReflectJava7.class+": main() - rt="+rt);

        System.err.println("\n"+RexxReflectJava7.class+": main() - all ReflectionType enums:");
        for (ReflectionType r : ReflectionType.values())
        {
            System.err.println("\t"+r);
        }

        System.err.println("\n"+RexxReflectJava7.class+": main() - all InvocationType enums:");
        for (InvocationType r : InvocationType.values())
        {
            System.err.println("\t"+r);
        }
    }

}


