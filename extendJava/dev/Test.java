public class Test
{
    public static void main (String args[]) {
        System.out.println("in Test.main(), static...");

        aha();
    }

    public static final void aha()
    {
        System.out.println("in Test.aha(), static...");
    }

    public void aha(int i)
    {
        System.out.println("in Test.aha(int i), instance: i="+i);
    }

    public void aha(Integer i)
    {
        System.out.println("in Test.aha(Integer i), instance: i="+i);
    }

    public int hashCode()
    {
        int hc=System.identityHashCode(this);
        System.out.println("in Test.hashCode(), instance: hashCode="+hc+"...");
        return hc;
    }


    public void mno(int i)
    {
        System.out.println("in Test.mno(int i), instance: i="+i);
    }

    public void xyz(Integer i)
    {
        System.out.println("in Test.xyz(Integer i), instance: i="+i);
    }


    static public void static_mno(int i) throws java.lang.RuntimeException
    {
        System.out.println("in Test.static_mno(int i), arg: i="+i);
    }

    static public void static_xyz(Integer i)
    {
        System.out.println("in Test.static_xyz(Integer i), arg: i="+i);
    }


}
