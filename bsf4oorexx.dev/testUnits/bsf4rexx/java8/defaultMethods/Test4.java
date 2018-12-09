package java8.defaultMethods;

public class Test4 extends Base4 implements Interface
{
    public static void main (String args[])
    {
        System.out.println("Test4: Interfac3.staticMeth(\"from_Test4\")> "+Interface.staticMeth("from_Test4"));
        Test4 t4=new Test4();
        System.out.print  ("Test4: t4.instanceMeth().................> " + t4.instanceMeth());
    }

}
