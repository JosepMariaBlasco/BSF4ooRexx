package java8.plain;

public class Test2 extends Test1
{
    public static int    INT_T2   =201;
    public static String STRING_T2="TSTRING_202 (static)";

    private static int    INT_i3=202;    // obscures Interface3.INT_I3_static

    public        int     INT_T2_obj =203;

    static public String test2infoStatic()
    {
        return "test2infoStatic";
    }

    public String test2info()
    {
        return "test2info";
    }


    public static void main (String args[]) {
        System.err.println(Test2.class+" in main() method...");

        Test2 obj1=new Test2();

        System.out.println("\nobj1="+obj1+":\n");


        System.err.println("obj1.STRING_i1="+obj1.STRING_i1);
        System.err.println("obj1.STRING_i2="+obj1.STRING_i2);
        System.err.println("obj1.STRING_i3="+obj1.STRING_i3);
        System.err.println("obj1.INT_T1   ="+obj1.INT_T1);
        System.err.println("obj1.INT_i1   ="+obj1.INT_i1);
        System.err.println("obj1.INT_i2   ="+obj1.INT_i2);
        System.err.println("obj1.INT_i3   ="+obj1.INT_i3); // private ! does not compile
        System.err.println("obj1.INT_T2   ="+obj1.INT_T2);
        System.err.println("---");

        try
        {
            Object objx=obj1.getClass().getField("INT_i2");
            System.err.println(Test1.class+": getField(\"INT_i2\")="+objx+", declared in: "+((java.lang.reflect.Field)objx).getDeclaringClass());
                // will not be found in the class, need to iterate over Interfaces "manually"
            objx=obj1.getClass().getDeclaredField("INT_i3");    // at runtime: NoSuchFieldException
            System.err.println(Test1.class+": getDeclaredField(\"INT_i3\")="+objx);
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
