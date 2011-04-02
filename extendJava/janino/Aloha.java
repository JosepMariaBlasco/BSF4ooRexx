/// createProxiedJavaClass, created Java code in source:
package org.rexxla.bsf.language.onTheFly;

public class Aloha extends java.io.File
{
	private static          Object    classLock     =new Object();
	private static volatile Object defaultHandler=null;
	private                 Object target        =null;


	public static Object getDefaultHandler()
	{
		return defaultHandler;
	}


	public static void setDefaultHandler(Object Object)
	{
		synchronized (classLock)
		{
			defaultHandler=Object;
		}
	}


	// --- creating methods according to String array of methods to proxy (if any)

	// --- creating constructors from existing public constructors

	public Aloha(Object a0, java.lang.String a1)
	{
		super(a1);
		target=a0;
	}


	public Aloha(Object a0, java.net.URI a1)
	{
		super(a1);
		target=a0;
	}


	public Aloha(Object a0, java.io.File a1, java.lang.String a2)
	{
		super(a1, a2);
		target=a0;
	}


	public Aloha(Object a0, java.lang.String a1, java.lang.String a2)
	{
		super(a1, a2);
		target=a0;
	}


        public void nixinoxi()
        {
            System.out.println("hallo!");
        }
}

