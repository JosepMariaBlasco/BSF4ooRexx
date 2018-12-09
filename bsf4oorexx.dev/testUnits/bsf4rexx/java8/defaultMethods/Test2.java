package java8.defaultMethods;

public class Test2 extends Base2 implements Interface
{
    public static void main (String args[])
    {
        System.out.println("Test2: Test2.staticMeth(\"from_Test2\")> "+staticMeth("from_Test2"));
        Test2 t2=new Test2();
        System.out.print  ("Test2: t2.instanceMeth().............> " + t2.instanceMeth());

    }

}
