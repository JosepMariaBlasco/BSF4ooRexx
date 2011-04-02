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

class TestTemplate
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




    /** The target RexxProxy used for carrying out all implemented methods.
    */
    RexxProxy target=null;


    // -----------------------------------------------------------------------------------

    // - create dynamically only those constructors, that exist in the denoted superclass
    // - create a default constructor, create a default constructor


    TestTemplate (RexxProxy rp)
    {
        super();        // default constructore

        target=rp;
    }


    TestTemplate ()
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
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+boolean.class+"\"");
        }

        Object oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Boolean( (String) result);
        }

        if (! (result instanceof java.lang.Boolean) )
        {
            throw new ClassCastException("Returned result type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+boolean.class+"\"");
        }

        return ((Boolean) result).booleanValue();   // now return primitive value

    }


    // -----------------------------------------------------------------------------------

    void testPrimitiveByteVoid_1 (byte argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveByteVoid_1";
        Object []args={
                        new java.lang.Byte(argObj)      // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    byte testPrimitiveBytePrimitiveByte_1 (byte argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveBytePrimitiveByte_1";
        Object []args={
                        new java.lang.Byte(argObj)      // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);

        if (result==null)
        {
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+byte.class+"\"");
        }

        Object oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Byte( (String) result);
        }

        if (! (result instanceof java.lang.Byte) )
        {
            throw new ClassCastException("Returned result type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+byte.class+"\"");
        }

        return ((java.lang.Byte) result).byteValue();   // now return primitive value
    }



    // -----------------------------------------------------------------------------------

    void testPrimitiveCharacterVoid_1 (char argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveCharacterVoid_1";
        Object []args={
                        new java.lang.Character(argObj)      // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    char testPrimitiveCharacterPrimitiveCharacter_1 (char argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveCharacterPrimitiveCharacter_1";
        Object []args={
                        new java.lang.Character(argObj)      // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);

        if (result==null)
        {
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+char.class+"\"");
        }

        Object oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Character( ((String) result).charAt(0));   // <=== "Character-anomaly" !!! (also in valueOf() in 1.5 or higher!)
        }

        if (! (result instanceof java.lang.Character) )
        {
            throw new ClassCastException("Returned type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+char.class+"\"");
        }

        return ((java.lang.Character) result).charValue();   // now return primitive value
    }



    // -----------------------------------------------------------------------------------

    void testPrimitiveShortVoid_1 (short argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveShortVoid_1";
        Object []args={
                        new java.lang.Short(argObj) // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);

    }


    short testPrimitiveShortPrimitiveShort_1 (short argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveShortPrimitiveShort_1";
        Object []args={
                        new java.lang.Short(argObj) // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        Object result=null;


        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);

        if (result==null)
        {
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+short.class+"\"");
        }

        Object oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Short( (String) result);
        }

        if (! (result instanceof java.lang.Short) )
        {
            throw new ClassCastException("Returned result type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+short.class+"\"");
        }

        return ((Short) result).shortValue();   // now return primitive value

    }


    // -----------------------------------------------------------------------------------

    void testPrimitiveIntegerVoid_1 (int argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveIntegerVoid_1";
        Object []args={
                        new java.lang.Integer(argObj) // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    int testPrimitiveIntegerPrimitiveInteger_1 (int argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveIntegerPrimitiveInteger_1";
        Object []args={
                        new java.lang.Integer(argObj) // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);

        if (result==null)
        {
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+int.class+"\"");
        }

        Object oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Integer( (String) result);
        }

        if (! (result instanceof java.lang.Integer) )
        {
            throw new ClassCastException("Returned result type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+int.class+"\"");
        }

        return ((Integer) result).intValue();   // now return primitive value

    }


    // -----------------------------------------------------------------------------------

    void testPrimitiveLongVoid_1 (long argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveLongVoid_1";
        Object []args={
                        new java.lang.Long(argObj) // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    long testPrimitiveLongPrimitiveLong_1 (long argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testPrimitiveLongPrimitiveLong_1";
        Object []args={
                        new java.lang.Long(argObj) // Java 1.4 ! else "java.lang.XXX.valueOf()" !
                      };

        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);

        if (result==null)
        {
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+long.class+"\"");
        }

        Object oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Long( (String) result);
        }

        if (! (result instanceof java.lang.Long) )
        {
            throw new ClassCastException("Returned result type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+long.class+"\"");
        }

        return ((Long) result).longValue();   // now return primitive value

    }


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
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+float.class+"\"");
        }

        Object oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Float( (String) result);
        }

        if (! (result instanceof java.lang.Float) )
        {
            throw new ClassCastException("Returned result type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+float.class+"\"");
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

        Object result=null;

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
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+double.class+"\"");
        }

        Object oriResult=result;

        if (result instanceof String)
        {
            result=new java.lang.Double( (String) result);
        }

        if (! (result instanceof java.lang.Double) )
        {
            throw new ClassCastException("Returned result type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+double.class+"\"");
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

    void testByteVoid_1 (Byte argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testByteVoid_1";
        Object []args={
                        argObj
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    Byte testByteByte_1 (Byte argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testByteByte_1";
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
            result=new java.lang.Byte( (String) result);
        }

        if (! (result instanceof java.lang.Byte) )
        {
            throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+Byte.class+"\".");
        }

        return  (Byte) result;               // now return  value

    }

    // -----------------------------------------------------------------------------------

    void testCharacterVoid_1 (Character argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testCharacterVoid_1";
        Object []args={
                        argObj
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    Character testCharacterCharacter_1 (Character argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testCharacterCharacter_1";
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
            result=new java.lang.Character( ((String) result).charAt(0));
        }

        if (! (result instanceof java.lang.Character) )
        {
            throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+Character.class+"\".");
        }

        return  (Character) result;               // now return  value

    }

    // -----------------------------------------------------------------------------------

    void testIntegerVoid_1 (Integer argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testIntegerVoid_1";
        Object []args={
                        argObj
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    Integer testIntegerInteger_1 (Integer argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testIntegerInteger_1";
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
            result=new java.lang.Integer( (String) result);
        }

        if (! (result instanceof java.lang.Integer) )
        {
            throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+Integer.class+"\".");
        }

        return  (Integer) result;               // now return  value

    }

    // -----------------------------------------------------------------------------------

    void testLongVoid_1 (Long argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testLongVoid_1";
        Object []args={
                        argObj
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    Long testLongLong_1 (Long argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testLongLong_1";
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
            result=new java.lang.Long( (String) result);
        }

        if (! (result instanceof java.lang.Long) )
        {
            throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+Long.class+"\".");
        }

        return  (Long) result;               // now return  value

    }


    // -----------------------------------------------------------------------------------

    void testFloatVoid_1 (Float argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testFloatVoid_1";
        Object []args={
                        argObj
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    Float testFloatFloat_1 (Float argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testFloatFloat_1";
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
            result=new java.lang.Float( (String) result);
        }

        if (! (result instanceof java.lang.Float) )
        {
            throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+Float.class+"\".");
        }

        return  (Float) result;               // now return  value

    }


    // -----------------------------------------------------------------------------------

    void testDoubleVoid_1 (Double argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testDoubleVoid_1";
        Object []args={
                        argObj
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    Double testDoubleDouble_1 (Double argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testDoubleDouble_1";
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
            result=new java.lang.Double( (String) result);
        }

        if (! (result instanceof java.lang.Double) )
        {
            throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+Double.class+"\".");
        }

        return  (Double) result;               // now return  value

    }


    // -----------------------------------------------------------------------------------

    void testStringVoid_1 (String argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testStringVoid_1";
        Object []args={
                        argObj
                      };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    String testStringString_1 (String argObj) throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testStringString_1";
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

        if (! (result instanceof java.lang.String) )
        {
            throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+String.class+"\".");
        }

        return  (String) result;               // now return  value

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


    public void testOneIntVoid (int i)  throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testOneIntVoid";
        Object []args={ new java.lang.Integer(i) };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }


    public Object testOneIntObject (int i)  throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testOneObjectObject";
        Object []args={ new java.lang.Integer(i) };
        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        return handler.invoke(msgName, args);
    }


    public Double testOneDblDouble_1 (double i)  throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testOneDblDouble_1";
        Object []args={ new java.lang.Double(i) };
        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);

        if (result!=null)
        {
            Object oriResult=result;        // save reference to original object

            if (result instanceof String)   // turn double into a Double object
            {
                result=new java.lang.Double( (String) result);
            }

            if (! (result instanceof Double) )
            {
                throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+Double.class+"\"");
            }
        }

        return (Double) result;
    }



    public Integer testOneIntInteger_2 (int i)  throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testOneIntInteger_2";
        Object []args={ new java.lang.Integer(i) };
        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);

        if (result!=null)
        {
            Object oriResult=result;        // save reference to original object

            if (result instanceof String)   // turn double into a Integer object
            {
                result=new java.lang.Integer( (String) result);
            }

            if (! (result instanceof Integer) )
            {
                throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+Integer.class+"\"");
            }
        }

        return (Integer) result;
    }




    public Object testOneIntInt_2 (int i, Object o)   throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testOneIntInt_2";
        Object []args={ new java.lang.Integer(i),
                        o};
        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);
        return result;
    }



    public int testOneIntInt_1 (int i)   throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testOneIntInt_1";
        Object []args={ new java.lang.Integer(i) };
        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);


        if (result==null)
        {
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+int.class+"\"");
        }

        Object oriResult=result;

        if (result instanceof String)   // turn double into a Double object
        {
            result=java.lang.Integer.valueOf( (String) result);
        }

        if (! (result instanceof Integer) )
        {
            throw new ClassCastException("Returned result type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+int.class+"\"");
        }

        return ((Integer) result).intValue();   // now return primitive value
    }


    public void testOneIntVoid_1 (int i)   throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testOneIntVoid_1";
        Object []args={ new java.lang.Integer(i) };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }

    public double testOneDoubleDouble_1 (double d)   throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testOneDoubleDouble_1";
        Object []args={ new java.lang.Double(d) };
        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(msgName, args);

        if (result==null)
        {
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+int.class+"\"");
        }

        Object oriResult=result;

        if (result instanceof String)   // turn double into a Double object
        {
            result=new java.lang.Double( (String) result);
        }

        if (! (result instanceof Double) )
        {
            throw new ClassCastException("Result type \""+oriResult.getClass()+"\" cannot be cast to return type \""+Double.class+"\"");
        }

        return ((Double) result).doubleValue();   // now return primitive value
    }


    public void testOneDoubleVoid_1 (double d)   throws Throwable
    {
        RexxProxy handler=target;
        String msgName="testOneInt_1";
        Object []args={ new java.lang.Double(d) };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(msgName, args);
    }



}
