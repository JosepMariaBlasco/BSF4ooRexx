/* Template class which codes the default behaviours which then need to be created
   dynamically at runtime for specializing (abstract) classes where the generated
   methods forward their invocations to the RexxProxy.

   The following methods will be always generated:

   - general:

            - public constructors; if no constructor is defined, two default constructors
              are created: one without an argument (then the class defined default RexxProxy
              object is used), one accepting the RexxProxy object to forward to

            - abstract methods

       - arguments could be supplied that denote the names of the methods to be proxied


    - idea:
            - allow additional interfaces to be denoted, for which proxying will be enabled



*/

import org.rexxla.bsf.engines.rexx.RexxProxy;

class TestTemplateShort
{

    /** Defines a class-level lock object to synchronize with. Whoever creates an instance
     *  of the dynamically created class is supposed to synchronize with this class object, then
     *  set the defaultHandler and then create an instance of the class.
    */
    static private Object classLock=new Object();

    /** Stores the "default" RexxProxy. This object will be used, in case
     *  a super-constructor invokes instance methods of the current class.
    */
    static volatile private RexxProxy defaultHandler=null;

    /** Getter for field 'defaultHandler'.
     * @return the default RexxProxy or <code>null</code>
    */
    static public RexxProxy getDefaultHandler()
    {
        return defaultHandler;
    }

    /** Setter for field 'defaultHandler'.
     * @param the new default RexxProxy object
    */
    static public void setDefaultHandler(RexxProxy newDefaultHandler)
    {
        // the constructors will synchronize on the class object as well; this way
        // instance methods that run because superclass constructors invoke them
        // are guaranteed to run against the RexxProxy that is supplied to the
        // constructor
        synchronized (classLock)
        {
            defaultHandler=newDefaultHandler;
        }
    }


    static public void staticDoNothingVoid() throws Throwable
    {
        String msgName="staticDoNothingVoid";
        Object []args=null;

        defaultHandler.invoke(msgName, args);
        return;
    }


    static public void staticDoNothingPrimitiveLong(long argObj) throws Throwable
    {
        String msgName="staticDoNothingPrimitiveLong";
        Object []args={
                         new java.lang.Long(argObj)
                      };

        defaultHandler.invoke(msgName, args);
        return;
    }


    static public void staticDoNothingLong(Long argObj) throws Throwable
    {
        String msgName="staticDoNothingLong";
        Object []args={
                         argObj
                      };

        defaultHandler.invoke(msgName, args);
        return;
    }


    static public long staticDoNothingPrimitiveLong_PrimitiveLong(long argObj) throws Throwable
    {
        String msgName="staticDoNothingPrimitiveLong_PrimitiveLong";
        Object []args={
                         new java.lang.Long(argObj)
                      };
        Object result=null,
               oriResult=null;

        result=defaultHandler.invoke(msgName, args);

        oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Long( (String) result);
        }

        if (! (result instanceof java.lang.Long) )
        {
            throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+Long.class+"\".");
        }

        return ((Long) result).longValue();   // now return primitive value
    }


    static public Long staticDoNothingLong_Long(Long argObj) throws Throwable
    {
        String msgName="staticDoNothingLong_Long";
        Object []args={
                         argObj
                      };

        Object result=null,
               oriResult=null;

        result=defaultHandler.invoke(msgName, args);

        oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Long( (String) result);
        }

        if (! (result instanceof java.lang.Long) )
        {
            throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+Long.class+"\".");
        }

        return (Long) result;  // return Long
    }


    /** The target RexxProxy used for carrying out all implemented methods.
    */
    RexxProxy target=null;


    // -----------------------------------------------------------------------------------

    // - create dynamically only those constructors, that exist in the denoted superclass
    // - create a default constructor, create a default constructor


    TestTemplateShort (RexxProxy rp)
    {
        super();        // default constructore

        target=rp;
    }


    TestTemplateShort ()
    {
        super();        // default constructore

        target=defaultHandler;
    }

    // -----------------------------------------------------------------------------------




    // -----------------------------------------------------------------------------------


    void testVoid() throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testVoid";
        Object []args=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    // -----------------------------------------------------------------------------------

    void testPrimitiveBooleanVoid_1 (boolean argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveBooleanVoid_1";
        Object []args={
                        java.lang.Boolean.valueOf(argObj)
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    boolean testPrimitiveBooleanPrimitiveBoolean_1 (boolean argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveBooleanPrimitiveBoolean_1";
        Object []args={
                        java.lang.Boolean.valueOf(argObj)
                      };

        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);

        if (result==null)
        {
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+boolean.class+'"');
        }

        Object oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Boolean( (String) result);
        }

        if (! (result instanceof java.lang.Boolean) )
        {
            throw new ClassCastException("Returned result type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+boolean.class+'"');
        }

        return ((Boolean) result).booleanValue();   // now return primitive value

    }


    // -----------------------------------------------------------------------------------

    // -----------------------------------------------------------------------------------


    void testPrimitiveFloatVoid_1 (float argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveFloatVoid_1";
        Object []args={
                        new java.lang.Float(argObj) // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    float testPrimitiveFloatPrimitiveFloat_1 (float argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveFloatPrimitiveFloat_1";
        Object []args={
                        new java.lang.Float(argObj) // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);

        if (result==null)
        {
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+float.class+'"');
        }

        Object oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Float( (String) result);
        }

        if (! (result instanceof java.lang.Float) )
        {
            throw new ClassCastException("Returned result type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+float.class+'"');
        }

        return ((Float) result).floatValue();   // now return primitive value

    }


    // -----------------------------------------------------------------------------------

    void testPrimitiveDoubleVoid_1 (double argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveDoubleVoid_1";
        Object []args={
                        new java.lang.Double(argObj) // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    double testPrimitiveDoublePrimitiveDouble_1 (double argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveDoublePrimitiveDouble_1";
        Object []args={
                        new java.lang.Double(argObj) // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);

        if (result==null)
        {
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+double.class+'"');
        }

        Object oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Double( (String) result);
        }

        if (! (result instanceof java.lang.Double) )
        {
            throw new ClassCastException("Returned result type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+double.class+'"');
        }

        return ((Double) result).doubleValue();   // now return primitive value

    }

    // -----------------------------------------------------------------------------------



    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // now non-primitive versions

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    void testBooleanVoid_1 (Boolean argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testBooleanVoid_1";
        Object []args={
                        argObj
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    Boolean testBooleanBoolean_1 (Boolean argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testBooleanBoolean_1";
        Object []args={
                        argObj
                      };

        Object result=null;


        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);

        if (result==null)
        {
            return null;
        }

        Object oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Boolean( (String) result);
        }

        if (! (result instanceof java.lang.Boolean) )
        {
            throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+Boolean.class+"\".");
        }

        return  (Boolean) result;               // now return  value

    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    void TestPrimitivesVoid(boolean bo, byte by, char ch, short sh, int in, long lo, float fl, double dbl, String str) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="TestPrimitivesVoid";
        Object []args={

                        java.lang.Boolean.valueOf(bo),

                        new java.lang.Byte(by),

                        new java.lang.Short(sh),

                        new java.lang.Integer(in),

                        new java.lang.Long(lo),

                        new java.lang.Float(fl),

                        new java.lang.Double(dbl),

                        str };


        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


/* geht erst ab Java 1.5/5, weil valueOf(prim) erst dann auch für >= Byte() verfügbar
    void TestPrimitivesVoid(boolean bo, byte by, char ch, short sh, int in, long lo, float fl, double dbl, String str) throws Throwable
    {
        String msgName="TestPrimitivesVoid";
        Object []args={

                        java.lang.Boolean.valueOf(bo),

                        java.lang.Byte.valueOf(by),

                        java.lang.Short.valueOf(sh),

                        java.lang.Integer.valueOf(in),

                        java.lang.Long.valueOf(lo),

                        java.lang.Float.valueOf(fl),

                        java.lang.Double.valueOf(dbl),

                        str };




        if (target==null)
        {
            defaultHandler.invoke(msgName, args);
        }
        else
        {
            target.invoke(msgName, args);
        }
    }
*/


    Object TestPrimitivesObject(boolean bo, byte by, char ch, short sh, int in, long lo, float fl, double dbl, String str) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="TestPrimitivesObject";
        Object []args={

                        java.lang.Boolean.valueOf(bo),

                        new java.lang.Byte(by),

                        new java.lang.Short(sh),

                        new java.lang.Integer(in),

                        new java.lang.Long(lo),

                        new java.lang.Float(fl),

                        new java.lang.Double(dbl),

                        str };


        if (handler==null)
        {
            handler=defaultHandler;
        }
        return handler.invoke(msgName, args);

    }


    public void testEmptyVoid () throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testEmptyVoid";

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, null);
    }


    public Object testEmptyObject () throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testEmptyObject";

        if (handler==null)
        {
            handler=defaultHandler;
        }
        return handler.invoke(msgName, null);
    }


    public void testOneObjectVoid (Object o) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testOneObjectVoid";
        Object []args={o};

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }

    public Object testOneObjectObject (Object o) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testOneObjectObject";
        Object []args={o};

        if (handler==null)
        {
            handler=defaultHandler;
        }
        return handler.invoke(msgName, args);
    }





}
