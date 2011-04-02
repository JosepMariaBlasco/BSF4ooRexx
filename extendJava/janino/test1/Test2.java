
public class Test2 extends Test
{
    public String aha_forwardToSuper()
    {
        System.out.println("Test2.aha()...");
        return(super.aha());

    }

    public static void main (String args[])
    {
        Test2 t2=new Test2();

        t2.aha_forwardToSuper();
    }
}
