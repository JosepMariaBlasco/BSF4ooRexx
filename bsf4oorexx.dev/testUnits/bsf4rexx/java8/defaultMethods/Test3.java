package java8.defaultMethods;

public class Test3 extends Base3 implements Interface
{
    public static void main (String args[])
    {
        System.out.println("Test3: Interface.staticMeth(\"from_Test3\")> "+Interface.staticMeth("from_Test3"));
        Test3 t3=new Test3();
        System.out.print  ("Test3: t3.instanceMeth().................> " + t3.instanceMeth());

    }

}
