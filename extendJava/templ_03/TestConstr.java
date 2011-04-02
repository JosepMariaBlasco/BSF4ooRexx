import org.rexxla.bsf.engines.rexx.RexxProxy;

class TestConstr
{

    RexxProxy target=null;

    synchronized public void      setTargetRexxProxy(RexxProxy rexxProxy)
    {
        this.target=rexxProxy;
    }

    synchronized public RexxProxy getTargetRexxProxy()
    {
        return this.target;
    }



    public TestConstr(RexxProxy handler)
    {
        ;
    }

    TestConstr(boolean bo, byte by, char ch, short sh, int in, long lo, float fl, double dbl, String str)
    {
        ;
    }


    public void testAMethodWithManyArgs_1 (boolean bo, Object o1,
                                    byte by,    Object o2,
                                    short sh,   Object o3,
                                    int i_1,    Object o4,
                                    long l_2,   Object o5,
                                    float fl,   Object o6,
                                    double db,  Object o7)
    {
        Object arg[]={ java.lang.Boolean.valueOf(bo),

                       o1,

                       new java.lang.Byte(by),

                       o2,

                       new java.lang.Short(sh),

                       o3,

                       new java.lang.Integer(i_1),

                       o4,

                       new java.lang.Long(l_2),

                       o5,

                       new java.lang.Float(fl),

                       o6,

                       new java.lang.Double(db),

                       o7 };

        System.err.println("say aha!");
    }


    public void testAMethodWithManyArgs_2 (boolean bo, Object o1,
                                    byte by,    Object o2,
                                    short sh,   Object o3,
                                    int i_1,    Object o4,
                                    long l_2,   Object o5,
                                    float fl,   Object o6,
                                    double db,  Object o7)
    {
        Object arg[]=null;
        arg=new Object [14];

        // { bo, o1, by, o2, sh, o3, i_1, o4, l_2, o5, fl, o6, db, o7 };
        arg[ 0]= new java.lang.Boolean(bo);

        arg[ 1]= o1;

        arg[ 2]= new java.lang.Byte(by);

        arg[ 3]= o2;

        arg[ 4]= new java.lang.Short(sh);

        arg[ 5]= o3;

        arg[ 6]= new java.lang.Integer(i_1);

        arg[ 7]= o4;

        arg[ 8]= new java.lang.Long(l_2);

        arg[ 9]= o5;

        arg[10]= new java.lang.Float(fl);

        arg[11]= o6;

        arg[12]= new java.lang.Double(db);

        arg[13]= o7;

        // { bo, o1, by, o2, sh, o3, i_1, o4, l_2, o5, fl, o6, db, o7 };
        System.err.println("say aha!");
    }


    static public void aha_01 ()
    {
        System.err.println("TestConstr.java - aha_01");
    }

    static public Object aha_02 (Object o2)
    {
        System.err.println("TestConstr.java - aha_02, o2="+o2);
        return o2;
    }


}
