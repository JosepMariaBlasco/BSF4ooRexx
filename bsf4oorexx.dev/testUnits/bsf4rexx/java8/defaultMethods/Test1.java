package java8.defaultMethods;

public class Test1 extends Base1 implements Interface
{
    public static void main (String args[])
    {
        System.out.println("Test1: Test1.staticMeth(\"from_Test1\")> "+staticMeth("from_Test1"));
        Test1 t1=new Test1();
        System.out.print  ("Test1: t1.instanceMeth().............> " + t1.instanceMeth());
    }

}
