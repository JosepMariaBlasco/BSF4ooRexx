import org.rexxla.bsf.engines.rexx.RexxProxy;
import org.apache.bsf.BSFException;

class TestConstrFromSubclass2 extends TestConstr
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


/*
    TestConstrFromSubclass()
    {
        // does not work, as super() does not have a default constructor
        super();                // invoke super's default constructor
        this.handler=defaultHandler;
    }
*/

    // - create dynamically only those constructors, that exist in the denoted superclass
    // - create a default constructor, create a default constructor

    TestConstrFromSubclass2(RexxProxy handler)
    {
        super(handler);
        this.target=handler;
    }

    TestConstrFromSubclass2(RexxProxy handler, boolean bo, byte by, char ch, short sh, int in, long lo, float fl, double dbl, String str)
    {
        super(bo, by, ch, sh, in, lo, fl, dbl, str);
        this.target=handler;
    }


    static public void aha_01 ()
    {
        System.err.println("TestConstrFromSubclass2.java - aha_01");
        TestConstr.aha_01();
    }

    static public Object aha_02 (Object o2)
    {
        System.err.println("TestConstrFromSubclass2.java - aha_02, o2="+o2);
        return TestConstr.aha_02(o2);
    }



    void TestPrimitivesVoid(boolean bo, byte by, char ch, short sh, int in, long lo, float fl, double dbl, String str) throws BSFException
    {
        RexxProxy handler=target;
        String msgName="TestPrimitivesVoid";
        String msgDesc="(ZBCSIJLFD)V";
        Object []args={

                        java.lang.Boolean.valueOf(bo),

                        new java.lang.Byte(by),

                        new java.lang.Character(ch),

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
        handler.invoke(this, msgName, msgDesc, args);
    }


/* geht erst ab Java 1.5/5, weil valueOf(prim) erst dann auch für >= Byte() verfügbar
    void TestPrimitivesVoid(boolean bo, byte by, char ch, short sh, int in, long lo, float fl, double dbl, String str) throws BSFException
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
            defaultHandler.invoke(this, msgName, msgDesc, args);
        }
        else
        {
            target.invoke(this, msgName, msgDesc, args);
        }
    }
*/


    Object TestPrimitivesObject(boolean bo, byte by, char ch, short sh, int in, long lo, float fl, double dbl, String str) throws BSFException
    {
        RexxProxy handler=target;
        String msgName="TestPrimitivesObject";
        String msgDesc="(msgDesc)R";
        Object []args={

                        java.lang.Boolean.valueOf(bo),

                        new java.lang.Byte(by),

                        new java.lang.Character(ch),

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
        return handler.invoke(this, msgName, msgDesc, args);

    }


    public void testEmptyVoid () throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testEmptyVoid";
        String msgDesc="(msgDesc)R";
        Object []args=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(this, msgName, msgDesc, args);
    }


    public Object testEmptyObject () throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testEmptyObject";
        String msgDesc="(msgDesc)R";
        Object []args=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        return handler.invoke(this, msgName, msgDesc, args);
    }


    public void testOneObjectVoid (Object o) throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testOneObjectVoid";
        String msgDesc="(msgDesc)R";
        Object []args={o};

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(this, msgName, msgDesc, args);
    }

    public Object testOneObjectObject (Object o) throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testOneObjectObject";
        String msgDesc="(msgDesc)R";
        Object []args={o};

        if (handler==null)
        {
            handler=defaultHandler;
        }
        return handler.invoke(this, msgName, msgDesc, args);
    }


    public void testOneIntVoid (int i)  throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testOneIntVoid";
        String msgDesc="(msgDesc)R";
        Object []args={ new java.lang.Integer(i) };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(this, msgName, msgDesc, args);
    }


    public Object testOneIntObject (int i)  throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testOneObjectObject";
        String msgDesc="(msgDesc)R";
        Object []args={ new java.lang.Integer(i) };
        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        return handler.invoke(this, msgName, msgDesc, args);
    }


    public Double testOneDblDouble_1 (double i)  throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testOneDblDouble_1";
        String msgDesc="(msgDesc)R";
        Object []args={ new java.lang.Double(i) };
        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(this, msgName, msgDesc, args);

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



    public Integer testOneIntInteger_2 (int i)  throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testOneIntInteger_2";
        String msgDesc="(msgDesc)R";
        Object []args={ new java.lang.Integer(i) };
        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(this, msgName, msgDesc, args);

        if (result!=null)
        {
            Object oriResult=result;        // save reference to original object

            if (result instanceof String)   // turn int into a Integer object
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




    public Object testOneIntInt_2 (int i, Object o)   throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testOneIntInt_2";
        String msgDesc="(msgDesc)R";
        Object []args={ new java.lang.Integer(i),
                        o};
        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(this, msgName, msgDesc, args);

        return result;
    }



    public int testOneIntInt_1 (int i)   throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testOneIntInt_1";
        String msgDesc="(msgDesc)R";
        Object []args={ new java.lang.Integer(i) };
        Object result=null;

        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(this, msgName, msgDesc, args);

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


    public void testOneIntVoid_1 (int i)   throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testOneIntVoid_1";
        String msgDesc="(msgDesc)R";
        Object []args={ new java.lang.Integer(i) };


        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(this, msgName, msgDesc, args);
    }

    public double testOneDoubleDouble_1 (double d)   throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testOneDoubleDouble_1";
        String msgDesc="(msgDesc)R";
        Object []args={ new java.lang.Double(d) };
        Object result=null;


        if (handler==null)
        {
            handler=defaultHandler;
        }
        result=handler.invoke(this, msgName, msgDesc, args);

        if (result==null)
        {
            throw new ClassCastException("Returned result is 'null', which cannot be cast to the primitive return type: \""+double.class+"\"");
        }

        Object oriResult=result;

        if (result instanceof String)   // turn double into a Double object
        {
            result=new java.lang.Double( (String) result);
        }

        if (! (result instanceof Double) )
        {
            throw new ClassCastException("Returned type \""+oriResult.getClass()+"\" cannot be cast to the primitive return type: \""+double.class+"\"");
        }

        return ((Double) result).doubleValue();   // now return primitive value
    }


    public void testOneDoubleVoid_1 (double d)   throws BSFException
    {
        RexxProxy handler=target;
        String msgName="testOneInt_1";
        String msgDesc="(msgDesc)R";
        Object []args={ new java.lang.Double(d) };

        if (handler==null)
        {
            handler=defaultHandler;
        }
        handler.invoke(this, msgName, msgDesc, args);
    }



}
