/// ProxiedJavaClassTool.createProxiedJavaClass(...), created Java source code:
package org.rexxla.bsf.engines.rexx.onTheFly;
import org.rexxla.bsf.engines.rexx.RexxProxy;
import org.apache.bsf.BSFException;

public class Dimension_$RexxExtendClass$_22772102 extends java.awt.Dimension
{
        public  static          Object    classLock     =new Object();
        private static volatile RexxProxy defaultHandler=null;
        private                 RexxProxy target        =null;


        public static RexxProxy getDefaultHandler()
        {
                return defaultHandler;
        }


        public static void setDefaultHandler(RexxProxy rexxproxy)
        {
                synchronized (classLock)
                {
                        defaultHandler=rexxproxy;
                }
        }


        public synchronized RexxProxy getTargetRexxProxy()
        {
                return target;
        }


        public synchronized void setTargetRexxProxy(RexxProxy newTarget)
        {
                target=newTarget;
        }


        // --- creating methods according to String array of methods to proxy (if any)

        public double getHeight() throws BSFException
        {
                Object []obj=new Object[] {  };
                Object    res=null;
                RexxProxy rpTarget=(target==null ? defaultHandler : target);
                        res=rpTarget.invoke(this, "getHeight", "public double getHeight()", obj);
                if (res==null)
                {
                        throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'double'.");
                }
                if (res instanceof String)
                {
                        res= new Double((String) res);
                }
                return ((Double) res).doubleValue();
        }


        public double getHeight_forwardToSuper()
        {
                return super.getHeight();
        }


        // --- creating constructors from existing public constructors using [java.awt.Dimension]

        public Dimension_$RexxExtendClass$_22772102(org.rexxla.bsf.engines.rexx.RexxProxy a0) throws BSFException
        {
                super();
                target=a0;
                if (defaultHandler==null)
                {
                        defaultHandler=a0;
                }
                Object []obj=new Object[] {  };
                target.invoke(this, "<init>", "public java.awt.Dimension()", obj);
        }


        public Dimension_$RexxExtendClass$_22772102(org.rexxla.bsf.engines.rexx.RexxProxy a0, java.awt.Dimension a1) throws BSFException
        {
                super(a1);
                target=a0;
                if (defaultHandler==null)
                {
                        defaultHandler=a0;
                }
                Object []obj=new Object[] { a1 };
                target.invoke(this, "<init>", "public java.awt.Dimension(java.awt.Dimension)", obj);
        }


        public Dimension_$RexxExtendClass$_22772102(org.rexxla.bsf.engines.rexx.RexxProxy a0, int a1, int a2) throws BSFException
        {
                super(a1, a2);
                target=a0;
                if (defaultHandler==null)
                {
                        defaultHandler=a0;
                }
                Object []obj=new Object[] { new Integer(a1), new Integer(a2) };
                target.invoke(this, "<init>", "public java.awt.Dimension(int,int)", obj);
        }


        public Dimension_$RexxExtendClass$_22772102(RexxProxy rexxproxy)
        {
                super();
                target=rexxproxy;
                if (defaultHandler==null)
                {
                        defaultHandler=rexxproxy;
                }
        }


        // --- creating constructors from existing public constructors using [java.awt.Dimension]
}
