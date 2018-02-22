package java8.plain;

public class Test1 implements Interface3
{
    public         Integer    INT_i1   =111;     // field obscures Interface1.INT_I1
    private static int   INT_i3   =133;     // field obscures Interface3.INT_I3 ?

    public static  Integer    INT_T1   =101;
    public         String STRING_T1="TSTRING_101 (instance)";

    public int    INT_3           =3;

    public static boolean bool_static_1    = false;
    public static Boolean Boolean_static_1 = Boolean.FALSE;

    public boolean bool_1 = true;
    public Boolean Boolean_1 = Boolean.TRUE;


    static public String test1infoStatic()
    {
        return "test1infoStatic";
    }

    public String test1info()
    {
        return "test1info";
    }

    // --------------------------------------- public fields
    public static Boolean staticEins=true;
    public        boolean eins      =true;

    // -----------------------------------------------------------------------------
    // --------------------------------------- private fields with setter and getter
    private static Boolean staticZwei=true;
    public static Boolean getStaticZwei()
    {
        return staticZwei;
    }
    public static void setStaticZwei(Boolean newVal)
    {
        staticZwei=newVal;
    }
    // --------------------------------------- private fields with setter and getter
    private Boolean zwei=true;
    public  Boolean getZwei()
    {
        return zwei;
    }
    public void setZwei(Boolean newVal)
    {
        zwei=newVal;
    }

    // -----------------------------------------------------------------------------
    // --------------------------------------- private fields with setter and getter
    private static boolean staticDrei=true;
    public static boolean isStaticDrei()
    {
        return staticDrei;
    }
    public static void setStaticDrei(boolean newVal)
    {
        staticDrei=newVal;
    }
    // --------------------------------------- private fields with setter and getter
    private boolean drei=true;
    public  boolean isDrei()
    {
        return drei;
    }
    public void setDrei(boolean newVal)
    {
        drei=newVal;
    }

    // -----------------------------------------------------------------------------
    // --------------------------------------- private fields with setter and getter
    private static Boolean staticVier=true;
    public static Boolean hasStaticVier()
    {
        return staticVier;
    }
    public static void setStaticVier(Boolean newVal)
    {
        staticVier=newVal;
    }
    // --------------------------------------- private fields with setter and getter
    private boolean vier=true;
    public  boolean hasVier()
    {
        return vier;
    }
    public void setVier(boolean newVal)
    {
        vier=newVal;
    }





    public static void main (String args[]) {
        System.err.println(Test1.class+" in main() method...");

        Test1 obj1=new Test1();

        System.out.println("\nobj1="+obj1+":\n");

        try
        {
            Object objx=obj1.getClass().getField("INT_i2");
            System.err.println(Test1.class+": getField(\"INT_i2\")="+objx);
                // will not be found in the class, need to iterate over Interfaces "manually"
            objx=obj1.getClass().getDeclaredField("INT_T1");
            System.err.println(Test1.class+": getDeclaredField(\"INT_T1\")="+objx);
        }
        catch (Throwable t)
        {
            System.err.println("Throwable "+t+":");
         t.printStackTrace();
            System.exit(-1);
        }

        System.out.println();
        System.out.println("obj1.interface1defaultMethod()="+obj1.interface1defaultMethod());
        System.out.println("obj1.interface1staticMethod ()="+Interface1.interface1staticMethod ());
        System.out.println();
        System.out.println("obj1.interface2defaultMethod()="+obj1.interface2defaultMethod());
        System.out.println("obj1.interface2staticMethod ()="+Interface2.interface2staticMethod ());
        System.out.println();
        System.out.println("obj1.interface3defaultMethod()="+obj1.interface3defaultMethod());
        System.out.println("obj1.interface3staticMethod ()="+Interface3.interface3staticMethod ());



    }
}
