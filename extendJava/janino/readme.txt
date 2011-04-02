As of 2010-04-17 ONLY the version in "bsf4oorexx/source_java" is the actual one !
---rony



--------------

By coincidence read about janino in the NetRexx list.

Although it is much huger than asm, it is much easier to use and to maintain,
hence experimenting with it.


        - defining a static method createProxiedJavaClass(Class javaClassToProxy, String newClzName, String[] methodNames2proxy)

                ... if newClzName==null, then: newClzName=javaClassToProxy+"_$RexxExtendClass$_"+(new Object()).hashCode();

                - if abstract class: create proxy methods for all abstract methods, including inherited (!)
                                     abstract methods

                - format of methodNames2proxy, if not null or empty:

                                "<init>"        ... proxy constructors
                                "methodName"    ... name of a method to proxy: case & signature irrelevant

                  -> name of method may be preceded by a fully qualified class name and a blank: in this case
                     the method name relates to the methods defined in that class


        - created proxy class is compatible with the asm created one, i.e. defines

                package org.rexxla.bsf.engines.rexx;
                import org.rexxla.bsf.engines.rexx.RexxProxy;
                import org.rexxla.bsf.engines.rexx.RexxException;


                public static RexxProxy getDefaultHandler()
                public static void      setDefaultHandler(RexxProxy rp), setting being synchronized on classLock

                private static Object   classLock=newObject();
                private static volatile RexxProxy defaultHandler=null;
                RexxProxy target=null;

                public RexxProxy        getTargetRexxProxy()


                public void             setTargetRexxProxy(RexxProxy rp)




---rgf, 2010-03-27
