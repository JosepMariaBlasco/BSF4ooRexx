import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

import java.util.Hashtable;

public class TestSub extends Test
{
    public static void main (String argsMain[]) {
        System.out.println("in TestSub.main()...");
        testForwardToSuper();
        testForwardToSuperStrict();

        testForwardToSuperStatic();         // new 2009-07-28
        testForwardToSuperStaticStrict();
    }

    static void testForwardToSuper()
    {
        System.out.println("in testForwardToSuper()...\n");

        TestSub ts=new TestSub();

        System.out.println("args=null;");
        Object args[]=null;
        try
        {
            ts.forwardToSuper("aha", args);
        }
        catch (Exception e) { e.printStackTrace(); }
        System.out.println("---");


        System.out.println("args=new Object [0];");
        args=new Object [0];
        try
        {
            ts.forwardToSuper("aha", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");


        System.out.println("aha(Integer), args=new Object [1];");
        args=new Object [1];
        args[0]=new Integer(17);
        try
        {
            ts.forwardToSuper("aha", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("aha(StringNumber), args=new Object [1];");
        args=new Object [1];
        args[0]="18";
        try
        {
            ts.forwardToSuper("aha", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");


        System.out.println("aha(Integer), args=new Object [1];");
        args=new Object [1];
        args[0]=new Integer(19);    // no autoboxing in 1.4
        try
        {
            ts.forwardToSuper("aha", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("---\n---");


        System.out.println("mno(Integer), args=new Object [1];");
        args=new Object [1];
        args[0]=new Integer(33);    // no autoboxing in 1.4
        try
        {
            ts.forwardToSuper("MNO", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("mno(StringNumber), args=new Object [1];");
        args=new Object [1];
        args[0]="34"; // new Integer(33);
        try
        {
            ts.forwardToSuper("MNO", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");


        System.out.println("xyz(Integer), args=new Object [1];");
        args=new Object [1];
        args[0]=new Integer(44);
        try
        {
            ts.forwardToSuper("xYz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");


        System.out.println("xyz(StringNumber), args=new Object [1];");
        args=new Object [1];
        args[0]="45";
        try
        {
            ts.forwardToSuper("xYz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("-------------------------------------------------------------\n");
    }



    static void testForwardToSuperStrict()
    {
        System.out.println("in testForwardToSuperStrict()...\n");

        TestSub ts=new TestSub();

        System.out.println("args=null;");
        Object args[]=null;
        try
        {
            ts.forwardToSuperStrict("aha", args);
        }
        catch (Exception e) { e.printStackTrace(); }
        System.out.println("---");


        System.out.println("args=new Object [0];");
        args=new Object [0];
        try
        {
            ts.forwardToSuperStrict("aha", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");


        System.out.println("aha(int), args=new Object [2];");
        args=new Object [2];
        args[0]="int";
        args[1]=new Integer(17);
        try
        {
            ts.forwardToSuperStrict("aha", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("aha(StringNumber), args=new Object [2];");
        args=new Object [2];
        args[0]="int";
        args[1]="18";
        try
        {
            ts.forwardToSuperStrict("aha", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");


        System.out.println("aha(Integer), args=new Object [2];");
        args=new Object [2];
        args[0]="Obj";
        args[1]=new Integer(19);
        try
        {
            ts.forwardToSuperStrict("aha", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("aha(StringNumber), args=new Object [2];");
        args=new Object [2];
        args[0]="int";
        args[1]="20";
        try
        {
            ts.forwardToSuperStrict("aha", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("---\n---");


        System.out.println("mno(Integer), args=new Object [2];");
        args=new Object [2];
        args[0]="Obj";
        args[1]=new Integer(33);
        try
        {
            ts.forwardToSuperStrict("mno", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("mno(int), args=new Object [2];");
        args=new Object [2];
        args[0]="int";
        args[1]=new Integer(34);
        try
        {
            ts.forwardToSuperStrict("mno", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("mno(StringNumber), args=new Object [2];");
        args=new Object [2];
        args[0]="int";
        args[1]="35";
        try
        {
            ts.forwardToSuperStrict("mno", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");


        System.out.println("xyz(int), args=new Object [2];");
        args=new Object [2];
        args[0]="int";
        args[1]=new Integer(44);
        try
        {
            ts.forwardToSuperStrict("xyz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");


        System.out.println("xyz(Integer), args=new Object [2];");
        args=new Object [2];
        args[0]="obj";
        args[1]=new Integer(45);
        try
        {
            ts.forwardToSuperStrict("xyz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("xyz(StringNumber), args=new Object [2];");
        args=new Object [2];
        args[0]="int";
        args[1]="46";
        try
        {
            ts.forwardToSuperStrict("xyz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("-------------------------------------------------------------");
    }




    // 2009-07-28, ---rgf: ----------------------
    static void testForwardToSuperStatic()
    {
        System.out.println("in testForwardToSuperStatic()...\n");

        TestSub ts=new TestSub();

        System.out.println("args=null;");
        Object args[]=null;
        try
        {
            ts.forwardToSuperStatic("aha", args);
        }
        catch (Exception e) { e.printStackTrace(); }
        System.out.println("---");



        System.out.println("args=null; #2");
        try
        {
            forwardToSuperStatic("aha", args);
        }
        catch (Exception e) { e.printStackTrace(); }
        System.out.println("---");


        System.out.println("---\n---");


        System.out.println("static_mno(Integer), args=new Object [1];");
        args=new Object [1];
        args[0]=new Integer(33);    // no autoboxing in 1.4
        try
        {
            ts.forwardToSuperStatic("STATIC_MNO", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("static_mno(Integer), args=new Object [1]; #2");
        args=new Object [1];
        args[0]=new Integer(33);    // no autoboxing in 1.4
        try
        {
            forwardToSuperStatic("STATIC_MNO", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

            // --

        System.out.println("static_mno(StringNumber), args=new Object [1];");
        args=new Object [1];
        args[0]="34"; // new Integer(33);
        try
        {
            ts.forwardToSuperStatic("STATIC_MNO", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("static_mno(StringNumber), args=new Object [1]; #2");
        args=new Object [1];
        args[0]="34"; // new Integer(33);
        try
        {
            forwardToSuperStatic("STATIC_MNO", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        // --

        System.out.println("static_xyz(Integer), args=new Object [1];");
        args=new Object [1];
        args[0]=new Integer(44);
        try
        {
            ts.forwardToSuperStatic("sTaTiC_xYz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("static_xyz(Integer), args=new Object [1]; #2");
        args=new Object [1];
        args[0]=new Integer(44);
        try
        {
            forwardToSuperStatic("sTaTiC_xYz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        // --


        System.out.println("static_xyz(StringNumber), args=new Object [1];");
        args=new Object [1];
        args[0]="45";
        try
        {
            ts.forwardToSuperStatic("sTaTiC_xYz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");


        System.out.println("static_xyz(StringNumber), args=new Object [1]; #2");
        args=new Object [1];
        args[0]="45";
        try
        {
            forwardToSuperStatic("sTaTiC_xYz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("-------------------------------------------------------------\n");
    }



    // 2009-07-28, ---rgf: ----------------------

    static void testForwardToSuperStaticStrict()
    {
        System.out.println("in testForwardToSuperStaticStrict()...\n");

        TestSub ts=new TestSub();

        System.out.println("args=null;");
        Object args[]=null;
        try
        {
            ts.forwardToSuperStaticStrict("aha", args);
        }
        catch (Exception e) { e.printStackTrace(); }
        System.out.println("---");


        System.out.println("args=null; #2");
        try
        {
            forwardToSuperStaticStrict("aha", args);
        }
        catch (Exception e) { e.printStackTrace(); }
        System.out.println("---");


        System.out.println("---\n---");


        System.out.println("static_mno(Integer), args=new Object [2];");
        args=new Object [2];
        args[0]="Obj";
        args[1]=new Integer(33);
        try
        {
            ts.forwardToSuperStaticStrict("static_mno", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("static_mno(Integer), args=new Object [2]; #2");
        args=new Object [2];
        args[0]="Obj";
        args[1]=new Integer(33);
        try
        {
            forwardToSuperStaticStrict("static_mno", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        // --

        System.out.println("static_mno(int), args=new Object [2];");
        args=new Object [2];
        args[0]="int";
        args[1]=new Integer(34);
        try
        {
            ts.forwardToSuperStaticStrict("static_mno", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("static_mno(int), args=new Object [2]; #2");
        args=new Object [2];
        args[0]="int";
        args[1]=new Integer(34);
        try
        {
            forwardToSuperStaticStrict("static_mno", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        // --

        System.out.println("static_mno(StringNumber), args=new Object [2];");
        args=new Object [2];
        args[0]="int";
        args[1]="35";
        try
        {
            ts.forwardToSuperStaticStrict("static_mno", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("static_mno(StringNumber), args=new Object [2]; #2");
        args=new Object [2];
        args[0]="int";
        args[1]="35";
        try
        {
            forwardToSuperStaticStrict("static_mno", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        // --

        System.out.println("static_xyz(int), args=new Object [2];");
        args=new Object [2];
        args[0]="int";
        args[1]=new Integer(44);
        try
        {
            ts.forwardToSuperStaticStrict("static_xyz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("static_xyz(int), args=new Object [2]; #2");
        args=new Object [2];
        args[0]="int";
        args[1]=new Integer(44);
        try
        {
            forwardToSuperStaticStrict("static_xyz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        // --

        System.out.println("static_xyz(Integer), args=new Object [2];");
        args=new Object [2];
        args[0]="obj";
        args[1]=new Integer(45);
        try
        {
            ts.forwardToSuperStaticStrict("static_xyz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        System.out.println("static_xyz(Integer), args=new Object [2]; #2");
        args=new Object [2];
        args[0]="obj";
        args[1]=new Integer(45);
        try
        {
            forwardToSuperStaticStrict("static_xyz", args);
        }
        catch (Exception e) {e.printStackTrace(); }
        System.out.println("---");

        // --

        System.out.println("static_xyz(StringNumber), args=new Object [2];");
        args=new Object [2];
        args[0]="int";
        args[1]="46";
        try
        {
            ts.forwardToSuperStaticStrict("static_xyz", args);
        }
        catch (Exception e) {e.printStackTrace(); }

        System.out.println("static_xyz(StringNumber), args=new Object [2]; #2");
        args=new Object [2];
        args[0]="int";
        args[1]="46";
        try
        {
            forwardToSuperStaticStrict("static_xyz", args);
        }
        catch (Exception e) {e.printStackTrace(); }


        System.out.println("-------------------------------------------------------------");
    }




// =====================================================================


    public Object forwardToSuper(String methodName, Object [] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        Object []coercedArgs=new Object [args==null?0:args.length];
            // iterate over all (super)classes to locate the public method
        for (Class clz=this.getClass().getSuperclass(); clz!=null; clz=clz.getSuperclass() )
        {
            Method m[]=clz.getDeclaredMethods();                    // get all declared methods of this class
            for (int i=0; i<m.length; i++)
            {
                if (m[i].getName().equalsIgnoreCase(methodName) )   // a method of the correct name
                {
                    if (Modifier.isPublic(m[i].getModifiers()))     // a public method
                    {
                        Class params[]=m[i].getParameterTypes();
                        boolean ok=false;
                        if (args==null)     // no args given, need a method without params
                        {
                            ok=(params.length==0&&args==null);
                        }
                        else                // args given, check whether correct number of params
                        {
                            ok=(params.length==args.length);
                        }

                        for (int k=0; k<params.length && ok; k++)
                        {
                            if (params[k].isAssignableFrom(args[k].getClass()))  // ok!
                            {
                                coercedArgs[k]=args[k];
                                continue;
                            }

                                // supplied argument assignable to primitive ?
                            if (params[k].isPrimitive())
                            {
                                if (((Class) prim2obj$.get(params[k])).isAssignableFrom(args[k].getClass()))
                                {
                                    coercedArgs[k]=args[k];
                                    continue;       // o.k., assignable
                                }

                            }

                                // one of the corresponding types?
                            if (prim2obj$.containsKey(params[k]))
                            {
                                Object tmpObj=null;
                                try
                                {
                                    coercedArgs[k]=convertFromStringTo$((String) args[k],
                                                                       (Class) prim2obj$.get(params[k]));
                                    continue;
                                }
                                catch (Exception e) {}

                            }

                            // ok=ok && (params[k].isAssignableFrom(args[k].getClass()));
                            ok=false;
                        }

                        if (ok)             // method found, invoke it!
                        {
                            try
                            {
                                return m[i].invoke(this, coercedArgs);
                            }
                            catch (IllegalAccessException e)    // maybe an IllegalAccessException?
                            {
                                m[i].setAccessible(true);      // allow unconditional access
                                return m[i].invoke(this, coercedArgs);
                            }
                        }
                    }
                }
            }
        }
        throw new NoSuchMethodException("cannot find method named \""+methodName+"\" with matching argument(s)");
    }


    /* Arguments are pairs of "typeIndicator", "value".
    */

    public Object forwardToSuperStrict(String methodName, Object [] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {

        Object [] argsCoerced=null;
        if (args!=null)
        {
            argsCoerced=new Object [args.length/2] ;  // create a matching array
        }

            // iterate over all (super)classes to locate the public method
        for (Class clz=this.getClass().getSuperclass(); clz!=null; clz=clz.getSuperclass() )
        {
            Method m[]=clz.getDeclaredMethods();                    // get all declared methods of this class
            for (int i=0; i<m.length; i++)
            {
                if (m[i].getName().equals(methodName) )     // a method of the correct name
                {
                    if (Modifier.isPublic(m[i].getModifiers()))     // a public method
                    {
                        Class params[]=m[i].getParameterTypes();
                        boolean ok=false;
                        if (args==null)     // no args given, need a method without params
                        {
                            ok=(params.length==0&&args==null);
                        }
                        else                // args given, check whether correct number of params
                        {
                            ok=((params.length*2)==args.length);    // argument are pairs of (typeIndicator, value)
                        }

                        for (int k=0; k<params.length && ok; k++)
                        {
                            Object argObj=args[k*2+1];
                            argsCoerced[k]=argObj;          // assume we need no conversion

                            if (params[k].isAssignableFrom(argObj.getClass()))  // ok!
                            {
                                continue;
                            }

                                // supplied argument assignable to primitive ?
                            if (params[k].isPrimitive() || prim2obj$.containsKey(params[k]) )
                            {
                                    // let us get the type argument, translate it to uppercase
                                String argType=((String) args[k*2]).toUpperCase();
                                    // matching argument type?
                                if (argType.startsWith((String) prim2ind$.get(params[k])))
                                {
                                    if (argObj instanceof String)   // o.k. we need to convert value from String
                                    {
                                            // convert string value to primitive's corresponding object type
                                        argsCoerced[k]=convertFromStringTo$((String) argObj,
                                                                           (Class) prim2obj$.get(params[k]));
                                    }
                                    continue;
                                }
                                continue;
                            }

                            // ok=ok && (params[k].isAssignableFrom(args[k].getClass()));
                            ok=false;
                        }

                        if (ok)             // method found, invoke it!
                        {
                            try
                            {
                                return m[i].invoke(this, argsCoerced);
                            }
                            catch (IllegalAccessException e)    // maybe an IllegalAccessException?
                            {
                                m[i].setAccessible(true);      // allow unconditional access
                                return m[i].invoke(this, argsCoerced);
                            }
                        }
                    }
                }
            }
        }
        throw new NoSuchMethodException("cannot find method named \""+methodName+"\" with matching argument(s)");
    }


    /* Convert a string encoded primitive value to an instance of the corresponding type. */
    static Object convertFromStringTo$(String str, Class clz)
    {
        if (clz==Boolean.class)
        {
            if (str.equals("1") || str.equalsIgnoreCase("true"))
            {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }

        if (clz==Byte.class)      return Byte.valueOf(str);
        if (clz==Character.class) return new Character(str.charAt(0)); // not available in 1.4: Character.valueOf(str.charAt(0));
        if (clz==Short.class)     return Short.valueOf(str);
        if (clz==Integer.class)   return Integer.valueOf(str);
        if (clz==Long.class)      return Long.valueOf(str);
        if (clz==Float.class)     return Float.valueOf(str);
        return Double.valueOf(str);
    }

    static Hashtable prim2obj$=null;
    static Hashtable prim2ind$=null;

    static
    {
        // mapping: primitive type to corresponding object type
            // process primitive types
        prim2obj$=new Hashtable();
        prim2obj$.put(boolean.class,   Boolean.class   );
        prim2obj$.put(byte.class,      Byte.class      );
        prim2obj$.put(char.class,      Character.class );
        prim2obj$.put(short.class,     Short.class     );
        prim2obj$.put(int.class,       Integer.class   );
        prim2obj$.put(long.class,      Long.class      );
        prim2obj$.put(float.class,     Float.class     );
        prim2obj$.put(double.class,    Double.class    );

            // also process corresponding types
        prim2obj$.put(Boolean.class,   Boolean.class   );
        prim2obj$.put(Byte.class,      Byte.class      );
        prim2obj$.put(Character.class, Character.class );
        prim2obj$.put(Short.class,     Short.class     );
        prim2obj$.put(Integer.class,   Integer.class   );
        prim2obj$.put(Long.class,      Long.class      );
        prim2obj$.put(Float.class,     Float.class     );
        prim2obj$.put(Double.class,    Double.class    );



        // mapping: primitive type to string type indicator
            // process primitive types
        prim2ind$=new Hashtable();
        prim2ind$.put(boolean.class,  "BO"             );
        prim2ind$.put(byte.class   ,  "BY"             );
        prim2ind$.put(short.class  ,  "SH"             );
        prim2ind$.put(int.class    ,  "I"              );
        prim2ind$.put(long.class   ,  "L"              );
        prim2ind$.put(float.class  ,  "F"              );
        prim2ind$.put(double.class ,  "D"              );

            // also process corresponding types
        prim2ind$.put(Boolean.class,  "BO"             );
        prim2ind$.put(Byte.class   ,  "BY"             );
        prim2ind$.put(Short.class  ,  "SH"             );
        prim2ind$.put(Integer.class,  "I"              );
        prim2ind$.put(Long.class   ,  "L"              );
        prim2ind$.put(Float.class  ,  "F"              );
        prim2ind$.put(Double.class ,  "D"              );
    }



    static public Object forwardToSuperStatic(String methodName, Object [] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        Object []coercedArgs=new Object [args==null?0:args.length];
            // iterate over all (super)classes to locate the public method
        for (Class clz=TestSub.class.getSuperclass(); clz!=null; clz=clz.getSuperclass() )
        {
            Method m[]=clz.getDeclaredMethods();                    // get all declared methods of this class
            for (int i=0; i<m.length; i++)
            {
                if (m[i].getName().equalsIgnoreCase(methodName) )   // a method of the correct name
                {
                    int modifiers=m[i].getModifiers();              // get method's modifiers
                    if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))     // a public method
                    {
                        Class params[]=m[i].getParameterTypes();
                        boolean ok=false;
                        if (args==null)     // no args given, need a method without params
                        {
                            ok=(params.length==0&&args==null);
                        }
                        else                // args given, check whether correct number of params
                        {
                            ok=(params.length==args.length);
                        }

                        for (int k=0; k<params.length && ok; k++)
                        {
                            if (params[k].isAssignableFrom(args[k].getClass()))  // ok!
                            {
                                coercedArgs[k]=args[k];
                                continue;
                            }

                                // supplied argument assignable to primitive ?
                            if (params[k].isPrimitive())
                            {
                                if (((Class) prim2obj$.get(params[k])).isAssignableFrom(args[k].getClass()))
                                {
                                    coercedArgs[k]=args[k];
                                    continue;       // o.k., assignable
                                }

                            }

                                // one of the corresponding types?
                            if (prim2obj$.containsKey(params[k]))
                            {
                                Object tmpObj=null;
                                try
                                {
                                    coercedArgs[k]=convertFromStringTo$((String) args[k],
                                                                       (Class) prim2obj$.get(params[k]));
                                    continue;
                                }
                                catch (Exception e) {}

                            }

                            // ok=ok && (params[k].isAssignableFrom(args[k].getClass()));
                            ok=false;
                        }

                        if (ok)             // method found, invoke it!
                        {
                            try
                            {
                                return m[i].invoke(null, coercedArgs);
                            }
                            catch (IllegalAccessException e)    // maybe an IllegalAccessException?
                            {
                                m[i].setAccessible(true);      // allow unconditional access
                                return m[i].invoke(null, coercedArgs);
                            }
                        }
                    }
                }
            }
        }
        throw new NoSuchMethodException("cannot find method named \""+methodName+"\" with matching argument(s)");
    }



    /* Arguments are pairs of "typeIndicator", "value".
    */

    static public Object forwardToSuperStaticStrict(String methodName, Object [] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {

        Object [] argsCoerced=null;
        if (args!=null)
        {
            argsCoerced=new Object [args.length/2] ;  // create a matching array
        }

            // iterate over all (super)classes to locate the public method
        for (Class clz=TestSub.class.getSuperclass(); clz!=null; clz=clz.getSuperclass() )
        {
            Method m[]=clz.getDeclaredMethods();                    // get all declared methods of this class
            for (int i=0; i<m.length; i++)
            {
                if (m[i].getName().equals(methodName) )     // a method of the correct name
                {
                    int modifiers=m[i].getModifiers();              // get method's modifiers
                    if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))     // a public method
                    {
                        Class params[]=m[i].getParameterTypes();
                        boolean ok=false;
                        if (args==null)     // no args given, need a method without params
                        {
                            ok=(params.length==0&&args==null);
                        }
                        else                // args given, check whether correct number of params
                        {
                            ok=((params.length*2)==args.length);    // argument are pairs of (typeIndicator, value)
                        }

                        for (int k=0; k<params.length && ok; k++)
                        {
                            Object argObj=args[k*2+1];
                            argsCoerced[k]=argObj;          // assume we need no conversion

                            if (params[k].isAssignableFrom(argObj.getClass()))  // ok!
                            {
                                continue;
                            }

                                // supplied argument assignable to primitive ?
                            if (params[k].isPrimitive() || prim2obj$.containsKey(params[k]) )
                            {
                                    // let us get the type argument, translate it to uppercase
                                String argType=((String) args[k*2]).toUpperCase();
                                    // matching argument type?
                                if (argType.startsWith((String) prim2ind$.get(params[k])))
                                {
                                    if (argObj instanceof String)   // o.k. we need to convert value from String
                                    {
                                            // convert string value to primitive's corresponding object type
                                        argsCoerced[k]=convertFromStringTo$((String) argObj,
                                                                           (Class) prim2obj$.get(params[k]));
                                    }
                                    continue;
                                }
                                continue;
                            }

                            // ok=ok && (params[k].isAssignableFrom(args[k].getClass()));
                            ok=false;
                        }

                        if (ok)             // method found, invoke it!
                        {
                            try
                            {
                                return m[i].invoke(null, argsCoerced);
                            }
                            catch (IllegalAccessException e)    // maybe an IllegalAccessException?
                            {
                                m[i].setAccessible(true);      // allow unconditional access
                                return m[i].invoke(null, argsCoerced);
                            }
                        }
                    }
                }
            }
        }
        throw new NoSuchMethodException("cannot find method named \""+methodName+"\" with matching argument(s)");
    }


}
